package com.mossle.operation.web;

import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import com.mossle.api.form.FormConnector;
import com.mossle.api.form.FormDTO;
import com.mossle.api.humantask.HumanTaskConnector;
import com.mossle.api.humantask.HumanTaskDTO;
import com.mossle.api.humantask.HumanTaskDefinition;
import com.mossle.api.keyvalue.FormParameter;
import com.mossle.api.keyvalue.KeyValueConnector;
import com.mossle.api.keyvalue.Prop;
import com.mossle.api.keyvalue.Record;
import com.mossle.api.keyvalue.RecordBuilder;
import com.mossle.api.process.ProcessConnector;
import com.mossle.api.process.ProcessDTO;
import com.mossle.api.store.StoreConnector;
import com.mossle.api.store.StoreDTO;
import com.mossle.api.tenant.TenantHolder;
import com.mossle.api.user.UserConnector;
import com.mossle.base.persistence.domain.BusinessDetailEntity;
import com.mossle.base.persistence.manager.BusinessDetailManager;
import com.mossle.bpm.rs.BpmResource;
//import com.mossle.base.persistence.domain.ReturnShopDetailEntity;
//import com.mossle.base.persistence.manager.ReturnShopDetailManager;
import com.mossle.button.ButtonDTO;
import com.mossle.button.ButtonHelper;
import com.mossle.common.utils.FileUploadAPI;
import com.mossle.common.utils.WebAPI;
import com.mossle.core.MultipartHandler;
import com.mossle.core.annotation.Log;
import com.mossle.core.auth.CurrentUserHolder;
import com.mossle.core.auth.CustomPasswordEncoder;
import com.mossle.core.hibernate.HibernateEntityDao;
import com.mossle.core.mapper.JsonMapper;
import com.mossle.core.spring.MessageHelper;
import com.mossle.core.store.MultipartFileDataSource;
import com.mossle.humantask.persistence.domain.TaskInfo;
import com.mossle.humantask.persistence.manager.TaskInfoManager;
import com.mossle.internal.store.persistence.domain.StoreInfo;
import com.mossle.operation.persistence.domain.Freeze;
import com.mossle.operation.persistence.domain.LllegalFreeze;
import com.mossle.operation.persistence.domain.LllegalFreezeDTO;
import com.mossle.operation.persistence.manager.LllegalFreezeManager;
import com.mossle.operation.service.CustomWorkService;
import com.mossle.operation.service.LllegalFreezeService;
import com.mossle.operation.service.OperationService;
import com.mossle.user.persistence.domain.AccountCredential;
import com.mossle.user.persistence.domain.AccountInfo;
import com.mossle.user.persistence.manager.AccountCredentialManager;
import com.mossle.user.persistence.manager.AccountInfoManager;
import com.mossle.user.support.ChangePasswordResult;
import com.mossle.xform.Xform;
import com.mossle.xform.XformBuilder;

import org.apache.commons.lang3.StringUtils;
//import org.activiti.web.simple.webapp.model.Leave;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


/**
 * 流程操作.
 * 冻结/解冻（违规）流程
 * 
 */
@Component
@Controller
@RequestMapping("processLllegalFreeze")
@Path("processLllegalFreeze")
public class ProcessOperationLllegalFreezeController {
	
    private static Logger logger = LoggerFactory.getLogger(ProcessOperationLllegalFreezeController.class);
    
    public static final int STATUS_DRAFT_PROCESS = 0;
    public static final int STATUS_DRAFT_TASK = 1;
    public static final int STATUS_RUNNING = 2;
    
    
    private OperationService operationService;
    
    private KeyValueConnector keyValueConnector;
    private MessageHelper messageHelper;
    private CurrentUserHolder currentUserHolder;
    private ProcessConnector processConnector;
    private HumanTaskConnector humanTaskConnector;
    private MultipartResolver multipartResolver;
    private StoreConnector storeConnector;
    private ButtonHelper buttonHelper = new ButtonHelper();
    private FormConnector formConnector;
    private JsonMapper jsonMapper = new JsonMapper();
    private TenantHolder tenantHolder;
    private UserConnector userConnector;
    //private HumanTaskDTO humanTaskDTO;
    
    private LllegalFreezeManager lllegalFreezeManager;
    private LllegalFreezeService lllegalFreezeService;
    private FileUploadAPI fileUploadAPI;
    private ProcessOperationController processOperationController;
    private AccountInfoManager accountInfoManager;
    private AccountCredentialManager accountCredentialManager;
    private CustomPasswordEncoder customPasswordEncoder;
    private WebAPI webAPI;
    private BusinessDetailManager businessDetailManager;
    @Resource
    private TaskInfoManager taskInfoManager;

    @Resource
    private BpmResource bpmResource;
    @Autowired
    private CustomWorkService customWorkService;
    /**
     * 发起流程.
     */
    @RequestMapping("process-operationLllegalFreeze-startProcessInstance")
    @Log(desc = "发起流程", action = "startProcess", operationDesc = "流程中心-我的审批-待办审批-违规冻结/解冻")
    public String startProcessInstance(HttpServletRequest request, @ModelAttribute LllegalFreeze lllegalFreezeDTO,String businessDetailId,String areaId,String companyId,String systemName,
            @RequestParam("bpmProcessId") String bpmProcessId, @RequestParam(value = "files", required = false) MultipartFile[] files,
            @RequestParam("businessKey") String businessKey,Model model) throws Exception {
        
    	// String aa = getUserInfo();
        String userId = currentUserHolder.getUserId();
        String tenantId = tenantHolder.getTenantId();
       
        copyUser(lllegalFreezeDTO);
        
        lllegalFreezeService.saveLllegalFreeze(request,lllegalFreezeDTO,businessDetailId, areaId,companyId, userId, businessKey, tenantId, systemName, bpmProcessId, files);
        
        return "operation/process-operation-startProcessInstance";
    }


    /**
     * 处理抄送人
     * @param lllegalFreezeDTO
     */
	private void copyUser(LllegalFreeze lllegalFreezeDTO) {
		//ckx add 2018/9/7
		String copyUserValue = lllegalFreezeDTO.getCopyUserValue();
		String cc = lllegalFreezeDTO.getCc();
		String copyIds = "";
		String copyNames = "";
		if(!"".equals(copyUserValue)){
			if(copyUserValue.contains("岗位:")){
	    		String[] splitCopyId = copyUserValue.split(",");
	    		String[] splitCopyName = cc.split(",");
	    		for (int i = 0; i < splitCopyId.length; i++) {
	    			String strCopyId = splitCopyId[i];
	            	String strCopyName = splitCopyName[i];
	            	if(strCopyId.contains("岗位:")){
	    				//岗位查询人员
	            		strCopyId = strCopyId.replaceAll("岗位:", "");
	    	    		
	            		//查询岗位上一级
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
    		//lllegalFreezeDTO.setCopyUserValue(copyIds);
			lllegalFreezeDTO.setCc(copyNames);
    	}
	}
    
    
    
    /** 审批各节点获取外部申请单数据
     * @throws Exception 
     * */ 
    @GET
    @Path("getLllegalFreezeInfo")
   public List<LllegalFreezeDTO> getLllegalFreezeById(@QueryParam("id")String id) throws Exception{
	   List<LllegalFreeze> lllegalFreezeInfo = lllegalFreezeManager.findBy("processInstanceId", id);
	   List <LllegalFreezeDTO> lllegalFreezeDTOList = new ArrayList<LllegalFreezeDTO>(); 
	   for(LllegalFreeze getInfo : lllegalFreezeInfo){
		   LllegalFreezeDTO lllegalFreeze = new LllegalFreezeDTO(); 
		   lllegalFreeze.setId(getInfo.getId());
		   lllegalFreeze.setSubmitTimes(getInfo.getSubmitTimes());
		   lllegalFreeze.setApplyCode(getInfo.getApplyCode());
		   lllegalFreeze.setTheme(getInfo.getTheme());
		   lllegalFreeze.setCc(getInfo.getCc());
		   lllegalFreeze.setBusinessType(getInfo.getBusinessType());
		   lllegalFreeze.setBusinessDetail(getInfo.getBusinessDetail());
		   lllegalFreeze.setBusinessLevel(getInfo.getBusinessLevel());
		   lllegalFreeze.setInitiator(getInfo.getInitiator());
		   lllegalFreeze.setUcode(getInfo.getUcode());
		   lllegalFreeze.setName(getInfo.getName());
		   lllegalFreeze.setWelfareLevel(getInfo.getWelfareLevel());
		   lllegalFreeze.setQualificationsStatus(getInfo.getQualificationsStatus());
		   lllegalFreeze.setSystem(getInfo.getSystem());
		   lllegalFreeze.setContact(getInfo.getContact());
		   lllegalFreeze.setArea(getInfo.getArea());
		   lllegalFreeze.setCompany(getInfo.getCompany());
		   lllegalFreeze.setIdNumber(getInfo.getIdNumber());
		   lllegalFreeze.setAboveBoard(getInfo.getAboveBoard());
		   lllegalFreeze.setDirectorContact(getInfo.getDirectorContact());
		   lllegalFreeze.setApplyMatter(getInfo.getApplyMatter());
		   lllegalFreeze.setApplyContent(getInfo.getApplyContent());
		   
		   lllegalFreeze.setEnclosure(getInfo.getEnclosure());
		   lllegalFreeze.setPath(getInfo.getPath());
		   lllegalFreeze.setUserId(getInfo.getUserId());
		   lllegalFreeze.setProcessInstanceId(getInfo.getProcessInstanceId());
		   //查询附件
		   List<StoreInfo> list = fileUploadAPI.getStore("OA/process", Long.toString(lllegalFreeze.getId()));
		   lllegalFreeze.setStoreInfos(list);
		   
		   lllegalFreezeDTOList.add(lllegalFreeze);
		   
		   
	   }
		return lllegalFreezeDTOList;
	}
    
   
    
    /**
     * 完成任务.
     */
    @RequestMapping("process-operationLllegalFreezeApproval-completeTask")
    @Log(desc = "审批流程", action = "confirmProcess", operationDesc = "流程中心-我的审批-待办审批-违规冻结/解冻")
    public String completeTask(HttpServletRequest request,  @RequestParam(value = "files", required = false) MultipartFile[] files,
            RedirectAttributes redirectAttributes,
            @RequestParam("processInstanceId") String processInstanceId, 
            @RequestParam("humanTaskId") String humanTaskId,
            @RequestParam(value = "iptdels", required = false) String iptdels,
            String flag
           ) throws Exception {
    	
    	try {
    		lllegalFreezeService.saveReLllegal(request, redirectAttributes, processInstanceId, humanTaskId, files, iptdels);
        } catch (IllegalStateException ex) {
            logger.error(ex.getMessage(), ex);
            messageHelper.addFlashMessage(redirectAttributes, "任务不存在");

            return "redirect:/humantask/workspace-personalTasks.do";
        }
    	
       return "operation/task-operation-completeTask";
    	
    }
    //-----------------------------------------------------------------------------------------------
  //验证密码是否正确
    @GET
    @Path("lllegalFreeze-verifyPassword")
    public int VerifyPassword(@QueryParam("pwd")  String pwd){
	   Long accountId = Long.parseLong(currentUserHolder.getUserId());
	   AccountInfo accountInfo = accountInfoManager.get(accountId);
	   String hql = "from AccountCredential where accountInfo=? and catalog='default'";
	   AccountCredential accountCredential = accountCredentialManager.findUnique(hql, accountInfo);
	   ChangePasswordResult changePasswordResult = new ChangePasswordResult();
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
     * */
    @RequestMapping("form-detail")
    @Log(desc = "查看详情页", action = "processDetail", operationDesc = "流程中心-违规冻结/解冻-详情")
    public String formDetail( @RequestParam("processInstanceId") String processInstanceId,
    		@RequestParam(value = "isPrint", required = false) boolean isPrint,
    		@RequestParam(value = "viewBack", required = false) boolean viewBack,
    		Model model){
    	try {
    		LllegalFreeze lllegalFreeze = lllegalFreezeManager.findUniqueBy("processInstanceId", processInstanceId);
    		model.addAttribute("lllegalFreeze", lllegalFreeze);
			Long id = lllegalFreeze.getId();
    		//取附件
    		model.addAttribute("picUrl", webAPI.getViewUrl());
    		List<StoreInfo> lllegalFreezeList = fileUploadAPI.getStore("OA/process", Long.toString(id));
    		model.addAttribute("StoreInfos", lllegalFreezeList);
    		//审批记录
            List<HumanTaskDTO> logHumanTaskDtos = humanTaskConnector
                    .findHumanTasksForPositionByProcessInstanceId(processInstanceId);
            //获得审核时长
            logHumanTaskDtos = operationService.settingAuditDuration(logHumanTaskDtos);
            model.addAttribute("logHumanTaskDtos", logHumanTaskDtos);
            model.addAttribute("isPrint", isPrint);
            model.addAttribute("viewBack", viewBack);
            operationService.copyMsgUpdate(processInstanceId);
		} catch (Exception e) {
			e.printStackTrace();
		}
    	return "operation/process/LllegalFreezeFormDetail";
    }
    /**
     * 回退任务(即驳回事件)，前一个任务.
     * @throws Exception 
     */
   /* @RequestMapping("task-operation-rollbackPreviousReturn")
    public String rollbackPrevious(HttpServletRequest request,
            @RequestParam("humanTaskId") String humanTaskId,String comment) throws Exception {
    	//this.getReturnById(id);
        humanTaskConnector.rollbackPrevious(humanTaskId,comment);
        HumanTaskDTO humanTaskDTO = humanTaskConnector.findHumanTask(humanTaskId,comment);//该方法已删除
        return "redirect:/humantask/workspace-personalTasks.do";
    }*/
    
    
   
    
   
    @RequestMapping("task-operation-ReturnDisagree")
    public String disagree(HttpServletRequest request, @RequestParam("humanTaskId") String humanTaskId,String activityId,String comment){
    	humanTaskConnector.skip("end", activityId, "");
    	return "redirect:/humantask/workspace-personalTasks.do";
    }
    
    /**
     * 实际确认发起流程.
     */
    public String doConfirmStartProcess(FormParameter formParameter, Model model) {
        humanTaskConnector.configTaskDefinitions(
                formParameter.getBusinessKey(),
                formParameter.getList("taskDefinitionKeys"),
                formParameter.getList("taskAssignees"));

        model.addAttribute("businessKey", formParameter.getBusinessKey());
        model.addAttribute("nextStep", formParameter.getNextStep());
        model.addAttribute("bpmProcessId", formParameter.getBpmProcessId());

        return "operation/process-operation-confirmStartProcess";
    }

    /**
     * 实际显示开始表单.
     */
    public String doViewStartForm(FormParameter formParameter, Model model,
            String tenantId) throws Exception {
        model.addAttribute("formDto", formParameter.getFormDto());
        model.addAttribute("bpmProcessId", formParameter.getBpmProcessId());
        model.addAttribute("businessKey", formParameter.getBusinessKey());
        model.addAttribute("nextStep", formParameter.getNextStep());

        List<ButtonDTO> buttons = new ArrayList<ButtonDTO>();
        buttons.add(buttonHelper.findButton("saveDraft"));
        buttons.add(buttonHelper.findButton(formParameter.getNextStep()));
        model.addAttribute("buttons", buttons);

        model.addAttribute("formDto", formParameter.getFormDto());

        String json = this.findStartFormData(formParameter.getBusinessKey());

        if (json != null) {
            model.addAttribute("json", json);
        }

        Record record = keyValueConnector.findByCode(formParameter
                .getBusinessKey());
        FormDTO formDto = formConnector.findForm(formParameter.getFormDto()
                .getCode(), tenantId);

        if (record != null) {
            Xform xform = new XformBuilder().setStoreConnector(storeConnector)
                    .setUserConnector(userConnector)
                    .setContent(formDto.getContent()).setRecord(record).build();
            model.addAttribute("xform", xform);
        } else {
            Xform xform = new XformBuilder().setStoreConnector(storeConnector)
                    .setUserConnector(userConnector)
                    .setContent(formDto.getContent()).build();
            model.addAttribute("xform", xform);
        }

        return "operation/process-operation-viewStartForm";
    }

    /**
     * 实际展示配置任务的配置.
     */
    public String doTaskConf(FormParameter formParameter, Model model) {
        model.addAttribute("bpmProcessId", formParameter.getBpmProcessId());

        model.addAttribute("businessKey", formParameter.getBusinessKey());
        model.addAttribute("nextStep", formParameter.getNextStep());

        List<HumanTaskDefinition> humanTaskDefinitions = humanTaskConnector
                .findHumanTaskDefinitions(formParameter
                        .getProcessDefinitionId());
        model.addAttribute("humanTaskDefinitions", humanTaskDefinitions);

        return "operation/process-operation-taskConf";
    }

    /**
     * 读取草稿箱中的表单数据，转换成json.
     */
    public String findStartFormData(String businessKey) throws Exception {
        Record record = keyValueConnector.findByCode(businessKey);

        if (record == null) {
            return null;
        }

        Map map = new HashMap();

        for (Prop prop : record.getProps().values()) {
            map.put(prop.getCode(), prop.getValue());
        }

        String json = jsonMapper.toJson(map);

        return json;
    }

    
    // ~ ======================================================================
    
    @Resource
    public void setKeyValueConnector(KeyValueConnector keyValueConnector) {
        this.keyValueConnector = keyValueConnector;
    }

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
    public void setProcessConnector(ProcessConnector processConnector) {
        this.processConnector = processConnector;
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
    public void setStoreConnector(StoreConnector storeConnector) {
        this.storeConnector = storeConnector;
    }

    @Resource
    public void setFormConnector(FormConnector formConnector) {
        this.formConnector = formConnector;
    }

    @Resource
    public void setTenantHolder(TenantHolder tenantHolder) {
        this.tenantHolder = tenantHolder;
    }

    @Resource
    public void setUserConnector(UserConnector userConnector) {
        this.userConnector = userConnector;
    }
    
 // ~ ======================================================================
    @Resource
    public void setLllegalFreezeManager(LllegalFreezeManager lllegalFreezeManager) {
        this.lllegalFreezeManager = lllegalFreezeManager;
    }
    @Resource
	public void setLllegalFreezeService(LllegalFreezeService lllegalFreezeService) {
		this.lllegalFreezeService = lllegalFreezeService;
	}
    @Resource
   	public void setProcessOperationController(
   			ProcessOperationController processOperationController) {
   		this.processOperationController = processOperationController;
   	}
    @Resource
    public void setFileUploadAPI(FileUploadAPI fileUploadAPI) {
    	this.fileUploadAPI = fileUploadAPI;
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
    public void setWebAPI(WebAPI webAPI) {
        this.webAPI = webAPI;
    }
    @Resource
   	public void setBusinessDetailManager(BusinessDetailManager businessDetailManager) {
   		this.businessDetailManager = businessDetailManager;
   	}
}
