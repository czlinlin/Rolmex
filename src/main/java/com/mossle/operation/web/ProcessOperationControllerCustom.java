package com.mossle.operation.web;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mossle.api.custom.CustomConnector;
import com.mossle.api.humantask.HumanTaskConnector;
import com.mossle.api.humantask.HumanTaskDTO;
import com.mossle.api.keyvalue.FormParameter;
import com.mossle.api.tenant.TenantHolder;
import com.mossle.common.utils.Exceptions;
import com.mossle.common.utils.FileUploadAPI;
import com.mossle.common.utils.StringUtils;
import com.mossle.common.utils.WebAPI;
import com.mossle.core.MultipartHandler;
import com.mossle.core.annotation.Log;
import com.mossle.core.auth.CurrentUserHolder;
import com.mossle.core.auth.CustomPasswordEncoder;
import com.mossle.core.mapper.JsonMapper;
import com.mossle.core.spring.MessageHelper;
import com.mossle.humantask.persistence.domain.TaskInfo;
import com.mossle.humantask.persistence.domain.TaskInfoCopy;
import com.mossle.humantask.persistence.manager.TaskInfoCopyManager;
import com.mossle.humantask.persistence.manager.TaskInfoManager;
import com.mossle.internal.store.persistence.domain.StoreInfo;
import com.mossle.operation.persistence.domain.CustomEntity;
import com.mossle.operation.persistence.domain.CustomEntityDTO;
import com.mossle.operation.persistence.domain.CustomPre;
import com.mossle.operation.persistence.domain.CustomWorkEntityDTO;
import com.mossle.operation.persistence.manager.CustomManager;
import com.mossle.operation.persistence.manager.CustomPreManager;
import com.mossle.operation.service.CustomService;
import com.mossle.operation.service.CustomWorkService;
import com.mossle.operation.service.OperationService;
import com.mossle.user.persistence.domain.AccountCredential;
import com.mossle.user.persistence.domain.AccountInfo;
import com.mossle.user.persistence.domain.PersonInfo;
import com.mossle.user.persistence.domain.UpdatePerson;
import com.mossle.user.persistence.manager.AccountCredentialManager;
import com.mossle.user.persistence.manager.AccountInfoManager;
import com.mossle.user.persistence.manager.UpdatePersonManager;
import com.mossle.user.support.ChangePasswordResult;
/**
 * @author chengze:
 * @version 创建时间：2017年9月14日 下午6:53:56
 * 自定义申请
 */
@Component
@Controller
@RequestMapping("operationCustom")
@Path("operationCustom")
public class ProcessOperationControllerCustom {


    private static Logger logger = LoggerFactory.getLogger(ProcessOperationController.class);
    
    public static final int STATUS_DRAFT_PROCESS = 0;
    public static final int STATUS_DRAFT_TASK = 1;
    public static final int STATUS_RUNNING = 2;
    
    private OperationService operationService;
    private MessageHelper messageHelper;
    private CurrentUserHolder currentUserHolder;
    private HumanTaskConnector humanTaskConnector;
    private MultipartResolver multipartResolver;
    private TenantHolder tenantHolder;
    private AccountCredentialManager accountCredentialManager;
    private CustomManager customManager;
    private TaskInfoManager taskInfoManager;
    private CustomPreManager customPreManager;
    private FileUploadAPI fileUploadAPI;
    private WebAPI webAPI;
    private AccountInfoManager accountInfoManager;
    private CustomPasswordEncoder customPasswordEncoder;
    private CustomService customService;
    private CustomConnector customConnector;

    private UpdatePersonManager updatePersonManager ;
    private JsonMapper jsonMapper = new JsonMapper();
    @Autowired
    private CustomWorkService customWorkService;
    /**
     * 跳转到自定义申请的发起表单.
     * @throws Exception 
     */
    @RequestMapping("custom-apply-list")
    public String customApplyForm(@RequestParam("userName") String userName,Model model) throws Exception {

    	String userId = currentUserHolder.getUserId();
    	//生成受理单编号
    	String code =  operationService.CreateApplyCode(userId);
       
    	model.addAttribute("userName",userName);
    	model.addAttribute("code",code);
        return "operation/custom-apply-list";
    }
    
    /**
     * 发起流程.
     */
    @RequestMapping("custom-startProcessInstance")  
    @Log(desc = "发起流程", action = "startProcess", operationDesc = "流程中心-我的流程-发起流程-自定义")
    public String startProcessInstance(HttpServletRequest request, @ModelAttribute CustomEntityDTO customEntityDTO,String area,
            @RequestParam("bpmProcessId") String bpmProcessId, 
            @RequestParam("businessKey") String businessKey, 
            @RequestParam(value = "files", required = false) MultipartFile[] files,
            Model model) throws Exception {
    	CustomEntity customEntity = new CustomEntity();
    	
    	//copyUser(customEntityDTO, customEntity);
    	
    	this.customService.StartProcessCustom(request, customEntityDTO,area, files,customEntity);
    	
    	return "operation/process-operation-startProcessInstance";
    }
    /**
     * 处理抄送   暂时不用
     * @param customEntityDTO
     * @param customEntity
     */
	private void copyUser(CustomEntityDTO customEntityDTO,
			CustomEntity customEntity) {
		String ccnos = customEntityDTO.getCcnos();
    	String ccName = customEntityDTO.getCcName();
    	String copyIds = "";
        String copyNames = "";
        //ckx  add 2018/8/30  抄送包含岗位
        if(null != ccnos && !"".equals(ccnos) && !"null".equals(ccnos)){
        	String[] splitCopyId = ccnos.split(",");
            String[] splitCopyName = ccName.split(",");
            for (int i = 0; i < splitCopyId.length; i++) {
            	String strCopyId = splitCopyId[i];
            	String strCopyName = splitCopyName[i];
            	if(strCopyId.contains("岗位:")){
    				//岗位查询人员
            		strCopyId = strCopyId.replaceAll("岗位:", "");
    	    		
            		/*Map<String, Object> queryCopyMap = jdbcTemplate.queryForMap("select * from party_entity e LEFT JOIN party_struct s ON e.ID = s.PARENT_ENTITY_ID where s.CHILD_ENTITY_ID = '"+strCopyId+"';");
    	    		String strCopyNameS = StringUtil.toString(queryCopyMap.get("NAME"));*/
            		String lastPost = customWorkService.getLastPost(strCopyId);
    	    		copyIds += strCopyId+",";
    				copyNames += lastPost+strCopyName+",";
    			}else{
    				copyIds += strCopyId+",";
    				copyNames += strCopyName+",";
    			}
    		}
            
            if(StringUtils.isNotBlank(copyIds)){
        		String str = copyIds.substring(copyIds.length()-1,copyIds.length() );
        		if(",".equals(str)){
        			copyIds = copyIds.substring(0, copyIds.length()-1);
        		}
        		String strName = copyNames.substring(copyNames.length()-1,copyNames.length() );
        		if(",".equals(strName)){
        			copyNames = copyNames.substring(0, copyNames.length()-1);
        		}
        	}
            //customEntity.setCcnos(copyIds);
            //customEntity.setCcName(copyNames);
        }
	}

    // ~ ======================================================================
    /**
     * 根据表单中选择的下一步审批人，查下该人是否已经审批过该条申请，避免重复审批
     */
    @GET
    @Path("isConfirm")
    public int isConfirm(@QueryParam("leaderId")  String leaderId,@QueryParam("formID") String formID){
	   
    	String sql = "from CustomPre where previous=? and formID =? ";
        
        List<CustomPre> c = customPreManager.find(sql,leaderId,formID);
        
        if(c.size()>0){
			return 1;
		}else {
			return 0;
		}
    }
    
    // ~ ============================================================================================================================================    
    /**
     * 完成任务.
     */
    @GET
    @RequestMapping("custom-completeTask")
    @Log(desc = "审批流程", action = "confirmProcess", operationDesc = "流程中心-我的审批-待办审批-自定义")
    public String completeTask(HttpServletRequest request
    		,@ModelAttribute CustomEntityDTO customEntityDTO,
    		@ModelAttribute CustomWorkEntityDTO customWorkEntityDTO,
            RedirectAttributes redirectAttributes,
            @RequestParam("processInstanceId") String processInstanceId, 
            @RequestParam(value = "files", required = false) MultipartFile[] files,
            @RequestParam(value = "iptdels", required = false) String iptdels,
            @RequestParam(value = "iptresart", required = false) String iptresart,
            @RequestParam(value = "iptoldid", required = false) String iptoldid,
            String flag
           ) throws Exception {
    	//isConfirm
    	//messageHelper.addFlashMessage(redirectAttributes, "core.success.publish", strTempName);
    	String hsql="from TaskInfo where processInstanceId=? and status='active' and catalog='normal'";
    	List<TaskInfo> taskInfoList = taskInfoManager.find(hsql,processInstanceId);
    	if(taskInfoList==null||taskInfoList.size()<1){
    		logger.info("查询不到审批步骤");
            messageHelper.addFlashMessage(redirectAttributes, "core.success.save", "找不到该流程或者流程已结束");
            return "redirect:/humantask/workspace-personalTasks.do";
    		//return messageHelper.addFlashMessage(redirectAttributes, "core.success.publish", "");
    	}
    	else{
    		if(!currentUserHolder.getUserId().equals(taskInfoList.get(0).getAssignee())){
    			logger.info("processInstanceId:"+processInstanceId+"，重复操作");
                messageHelper.addFlashMessage(redirectAttributes, "core.success.save", "你无权限审核或则已审核过该流程");
                return "redirect:/humantask/workspace-personalTasks.do";
    		}
    	}
    	/*try {
    		Thread.sleep(5000);
    		} catch (InterruptedException e) {
    		e.printStackTrace();
    		}*/
    	try {
	    	this.customService.CompleteTaskCustomPC(request, customEntityDTO,customWorkEntityDTO,
					processInstanceId, files, iptdels, flag);
    	} catch(Exception ex) {
    		logger.info("==============processInstanceId:" + processInstanceId);
    		logger.error(ex.getMessage(), ex);
    		
    		String errStr = Exceptions.getExceptionAllinformation(ex);
    		if (StringUtils.inString(errStr, "org.hibernate.exception.LockAcquisitionException") || 
    				StringUtils.inString(errStr, "com.mysql.jdbc.exceptions.jdbc4.MySQLTransactionRollbackException")) {
    			messageHelper.addFlashMessage(redirectAttributes, "core.success.save", "系统繁忙或网络异常，请稍后重试！");
    		} else {
    			messageHelper.addFlashMessage(redirectAttributes, "core.success.save", "系统内部发生错误，请联系管理员！");
    		}
            return "redirect:/humantask/workspace-personalTasks.do";
    	}
    	 return "operation/task-operation-completeTask";
    }
	
	// ~ ======================================================================
    
    //验证密码是否正确
    @GET
    @Path("custom-verifyPassword")
    public int VerifyPassword(@QueryParam("pwd")  String pwd){
	   Long accountId = Long.parseLong(currentUserHolder.getUserId());
	   AccountInfo accountInfo = accountInfoManager.get(accountId);
	   String hql = "from AccountCredential where accountInfo=? and catalog='default'";
	   AccountCredential accountCredential = accountCredentialManager.findUnique(hql, accountInfo);
	   ChangePasswordResult changePasswordResult = new ChangePasswordResult();
	   
	   String p = accountCredential.getPassword();
	   
	   if (!isPasswordValid(pwd, accountCredential.getOperationPassword())) {
            changePasswordResult.setCode("user.user.input.passwordnotcorrect");
            changePasswordResult.setMessage("密码错误");
            return 0;
        }else {
        	return 1;
        }
    }
    
    public boolean isPasswordValid(String rawPassword, String encodedPassword) {
        if (customPasswordEncoder != null) {
            return customPasswordEncoder.matches(rawPassword, encodedPassword);
        } else {
            return rawPassword.equals(encodedPassword);
        }
    }
	
	/**
     * 申请单详情页
	 * @throws IOException 
     * */
    @RequestMapping("custom-detail")
    @Log(desc = "查看详情页", action = "processDetail", operationDesc = "流程中心-自定义-详情")
    public String formDetail( @RequestParam("processInstanceId") String processInstanceId,
    		@RequestParam(value = "isPrint", required = false) boolean isPrint,
    		@RequestParam(value = "viewBack", required = false) boolean viewBack,
    		 Model model,HttpServletRequest request) throws IOException{
    	CustomEntity customEntity=customManager.get(Long.parseLong(processInstanceId));
    	model.addAttribute("customEntity", customEntity);
    	
    	//取附件
   	 	model.addAttribute("picUrl", webAPI.getViewUrl());
        List<StoreInfo> list;
		try {
			list = fileUploadAPI.getStore("operation/CustomApply", processInstanceId);
			model.addAttribute("StoreInfos", list);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       
    	// 审批记录
        List<HumanTaskDTO> logHumanTaskDtos = humanTaskConnector
                .findHumanTasksForPositionByProcessInstanceId(processInstanceId);
        //获得审核时长
        logHumanTaskDtos = operationService.settingAuditDuration(logHumanTaskDtos);
        model.addAttribute("logHumanTaskDtos", logHumanTaskDtos);
        model.addAttribute("isPrint", isPrint);
        model.addAttribute("viewBack", viewBack);
        
        HumanTaskDTO humanTaskDto=logHumanTaskDtos.get(0);
        Map<String,Object> approverMap=customConnector.findAuditorHtml(humanTaskDto.getBusinessKey(), currentUserHolder.getUserId());
        
        model.addAttribute("approver",approverMap.get("approver"));
      //判断若是花名册的流程，将添加人员或者修改花名册的标识取出来带回页面
    	String personTypeID = "";
    	UpdatePerson updatePerson=updatePersonManager.findUniqueBy("applyCode", customEntity.getApplyCode());
  		String id = "";
  		String partyEntityId = "";
  		
  		if(updatePerson!=null){
  			//model.addAttribute("applyCode", updatePerson.getApplyCode());
  			personTypeID = updatePerson.getTypeID();
  			if((updatePerson.getTypeID().equals("personadd")||updatePerson.getTypeID().equals("personUpdate"))){
  	  			Object succesResponse = JSON.parse(updatePerson.getJsonContent());    //先转换成Object
  				Map map = (Map)succesResponse;
  				PersonInfo personInfo=jsonMapper.fromJson(map.get("personInfo").toString(),PersonInfo.class);
  				id = personInfo.getId()==null?"": personInfo.getId().toString();
  				partyEntityId = map.get("parentPartyEntityId")==null?"":map.get("parentPartyEntityId").toString();
  				
  	    	}
  	  		else if((updatePerson.getTypeID().equals("changePost"))){
  	  			id = updatePerson.getEmployeeNo();
  	  			//model.addAttribute("personInfoId", id);
  	  		}
  		}
  	
  		model.addAttribute("personInfoId",id);
  		model.addAttribute("partyEntityId",partyEntityId);
  		model.addAttribute("personTypeID", personTypeID);
  		operationService.copyMsgUpdate(processInstanceId);
    	return "operation/custom-apply-showForm";
    }
	
	
    /**
     * 通过multipart请求构建formParameter.
     */
    public FormParameter buildFormParameter(MultipartHandler multipartHandler) {
        FormParameter formParameter = new FormParameter();
        formParameter.setMultiValueMap(multipartHandler.getMultiValueMap());
        formParameter.setMultiFileMap(multipartHandler.getMultiFileMap());
        formParameter.setBusinessKey(multipartHandler.getMultiValueMap()
                .getFirst("businessKey"));
        formParameter.setBpmProcessId(multipartHandler.getMultiValueMap()
                .getFirst("bpmProcessId"));
        formParameter.setHumanTaskId(multipartHandler.getMultiValueMap()
                .getFirst("humanTaskId"));
        formParameter.setComment(multipartHandler.getMultiValueMap().getFirst(
                "comment"));

        return formParameter;
    }
    
    /**
     * 把数据先保存到keyvalue里.
     */
    public FormParameter doSaveRecord(HttpServletRequest request)
            throws Exception {
        String userId = currentUserHolder.getUserId();
        String tenantId = tenantHolder.getTenantId();

        MultipartHandler multipartHandler = new MultipartHandler(
                multipartResolver);
        FormParameter formParameter = null;

        try {
            multipartHandler.handle(request);
            logger.debug("multiValueMap : {}",
                    multipartHandler.getMultiValueMap());
            logger.debug("multiFileMap : {}",
                    multipartHandler.getMultiFileMap());

            formParameter = this.buildFormParameter(multipartHandler);
            
            formParameter.setBpmProcessId("-1");   // 模拟流程
            formParameter.setBusinessKey("");
            String businessKey = operationService.saveDraft(userId, tenantId,
                    formParameter);

            if ((formParameter.getBusinessKey() == null)
                    || "".equals(formParameter.getBusinessKey().trim())) {
                formParameter.setBusinessKey(businessKey);
            }
            // TODO zyl 2017-11-16 外部表单不需要保存prop
            /*Record record = keyValueConnector.findByCode(businessKey);

            record = new RecordBuilder().build(record, multipartHandler,
                    storeConnector, tenantId);

            keyValueConnector.save(record);*/
        } finally {
            multipartHandler.clear();
        }

        return formParameter;
    }
    
    
    // ~ ======================================================================
    @Resource
    public void setMessageHelper(MessageHelper messageHelper) {
        this.messageHelper = messageHelper;
    }

    @Resource
    public void setCurrentUserHolder(CurrentUserHolder currentUserHolder) {
        this.currentUserHolder = currentUserHolder;
    }

    @Resource
    public void setOperationService(OperationService operationService) {
        this.operationService = operationService;
    }
    
    @Resource
    public void setHumanTaskConnector(HumanTaskConnector humanTaskConnector) {
        this.humanTaskConnector = humanTaskConnector;
    }

    @Resource
    public void setMultipartResolver(MultipartResolver multipartResolver) {
        this.multipartResolver = multipartResolver;
    }

    @Resource
    public void setTenantHolder(TenantHolder tenantHolder) {
        this.tenantHolder = tenantHolder;
    }

 // ~ ======================================================================
    @Resource
    public void setCustomManager(CustomManager customManager) {
        this.customManager = customManager;
    }

    @Resource
    public void setTaskInfoManager(TaskInfoManager taskInfoManager) {
        this.taskInfoManager = taskInfoManager;
    }
    
    @Resource
    public void setCustomPreManager(CustomPreManager customPreManager) {
        this.customPreManager = customPreManager;
    }
    
    @Resource
    public void setFileUploadAPI(FileUploadAPI fileUploadAPI) {
        this.fileUploadAPI = fileUploadAPI;
    }
    
    @Resource
    public void setWebAPI(WebAPI webAPI) {
        this.webAPI = webAPI;
    }
    
    @Resource
    public void setAccountInfoManager(AccountInfoManager accountInfoManager) {
        this.accountInfoManager = accountInfoManager;
    }
    
    @Resource
    public void setAccountCredentialManager(AccountCredentialManager accountCredentialManager) {
        this.accountCredentialManager = accountCredentialManager;
    }
    @Resource
	public void setCustomPasswordEncoder(CustomPasswordEncoder customPasswordEncoder) {
		this.customPasswordEncoder = customPasswordEncoder;
	}

	@Resource
	public void setCustomService(CustomService customService) {
		this.customService = customService;
	}
    
    @Resource
    public void setCustomConnector(CustomConnector customConnector){
    	this.customConnector=customConnector;
    }
    
    
    @Resource
	public void setUpdatePersonManager(UpdatePersonManager updatePersonManager) {
		this.updatePersonManager = updatePersonManager;
	}
       
}
