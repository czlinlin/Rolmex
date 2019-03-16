package com.mossle.operation.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;














//import org.activiti.web.simple.webapp.model.Leave;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.alibaba.fastjson.JSONObject;
import com.mossle.api.form.FormConnector;
import com.mossle.api.form.FormDTO;
import com.mossle.api.humantask.HumanTaskConnector;
import com.mossle.api.humantask.HumanTaskDTO;
import com.mossle.api.humantask.HumanTaskDefinition;
import com.mossle.api.keyvalue.FormParameter;
import com.mossle.api.keyvalue.KeyValueConnector;
import com.mossle.api.keyvalue.Prop;
import com.mossle.api.keyvalue.Record;
import com.mossle.api.store.StoreConnector;
import com.mossle.api.tenant.TenantHolder;
import com.mossle.api.user.UserConnector;
import com.mossle.base.persistence.domain.BusinessDetailEntity;
import com.mossle.base.persistence.manager.BusinessDetailManager;
//import com.mossle.base.persistence.domain.ReturnShopDetailEntity;
//import com.mossle.base.persistence.manager.ReturnShopDetailManager;
import com.mossle.button.ButtonDTO;
import com.mossle.button.ButtonHelper;
import com.mossle.common.utils.Exceptions;
import com.mossle.common.utils.FileUploadAPI;
import com.mossle.common.utils.StringUtils;
import com.mossle.common.utils.WebAPI;
import com.mossle.core.annotation.Log;
import com.mossle.core.auth.CurrentUserHolder;
import com.mossle.core.auth.CustomPasswordEncoder;
import com.mossle.core.mapper.JsonMapper;
import com.mossle.core.spring.MessageHelper;
import com.mossle.humantask.persistence.manager.TaskInfoManager;
import com.mossle.internal.store.persistence.domain.StoreInfo;
import com.mossle.operation.persistence.domain.Business;
import com.mossle.operation.persistence.domain.BusinessDTO;
import com.mossle.operation.persistence.manager.BusinessManager;
import com.mossle.operation.service.BusinessService;
import com.mossle.operation.service.CustomWorkService;
import com.mossle.operation.service.OperationService;
import com.mossle.user.persistence.domain.AccountCredential;
import com.mossle.user.persistence.domain.AccountInfo;
import com.mossle.user.persistence.manager.AccountCredentialManager;
import com.mossle.user.persistence.manager.AccountInfoManager;
import com.mossle.user.support.ChangePasswordResult;
import com.mossle.util.StringUtil;
import com.mossle.xform.Xform;
import com.mossle.xform.XformBuilder;


/**
 * 流程操作.
 * 业务申请（分公司）流程
 * 
 */
@Component
@Controller
@RequestMapping("processBusiness")
@Path("processBusiness")
public class ProcessOperationBusinessController {
	
    private static Logger logger = LoggerFactory.getLogger(ProcessOperationBusinessController.class);
    
    public static final int STATUS_DRAFT_PROCESS = 0;
    public static final int STATUS_DRAFT_TASK = 1;
    public static final int STATUS_RUNNING = 2;
    
    private KeyValueConnector keyValueConnector;
    private MessageHelper messageHelper;
    private CurrentUserHolder currentUserHolder;
    private HumanTaskConnector humanTaskConnector;
    private StoreConnector storeConnector;
    private ButtonHelper buttonHelper = new ButtonHelper();
    private FormConnector formConnector;
    private JsonMapper jsonMapper = new JsonMapper();
    private TenantHolder tenantHolder;
    private UserConnector userConnector;
    private FileUploadAPI fileUploadAPI;
    private WebAPI webAPI;
    //private HumanTaskDTO humanTaskDTO;
    
    private BusinessManager businessManager;
    private BusinessService businessService;
    private AccountInfoManager accountInfoManager;
    private AccountCredentialManager accountCredentialManager;
    private CustomPasswordEncoder customPasswordEncoder;
    
    @Autowired
    private CustomWorkService customWorkService;
    @Autowired
    private OperationService operationService;
    
    @Resource
    private TaskInfoManager taskInfoManager;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private BusinessDetailManager businessDetailManager;
    /**
     * 发起流程.
     */
    @RequestMapping("process-operationBusiness-startProcessInstance")
    @Log(desc = "发起流程", action = "startProcess", operationDesc = "流程中心-我的流程-发起流程-分公司/大区业务")
    public String startProcessInstance(HttpServletRequest request, @ModelAttribute Business businessDTO,String businessDetailId,String areaId,String branchOfficeId,
            @RequestParam("bpmProcessId") String bpmProcessId, HumanTaskDTO humanTaskDTO, @RequestParam(value = "files", required = false) MultipartFile [] files,
            @RequestParam("businessKey") String businessKey,Model model) throws Exception {
        
    	// String aa = getUserInfo();
        
        String userId = currentUserHolder.getUserId();
        String tenantId = tenantHolder.getTenantId();
        
        copyUser(businessDTO);
    	
        businessService.saveBusiness(request,businessDTO,businessDetailId, areaId, businessKey, userId, tenantId, branchOfficeId,bpmProcessId, files);
       
        return "operation/process-operation-startProcessInstance";
    }
    
    /**
     * 处理抄送人
     * @param businessDTO
     */
	private void copyUser(Business businessDTO) {
		String copyUserValue = businessDTO.getCopyUserValue();
    	String cc = businessDTO.getCc();
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
                		Map<String, Object> queryCopyPostMap = null;
        				try {
            	    		queryCopyPostMap = jdbcTemplate.queryForMap("select e.NAME from party_entity e where e.ID = '"+strCopyId+"'");
        				} catch (Exception e) {
        				}
        	    		String strCopyPostName = StringUtil.toString(queryCopyPostMap.get("NAME") == null ? "" : queryCopyPostMap.get("NAME"));
        	    		copyIds += strCopyId+",";
        				copyNames += lastPost+strCopyPostName+",";
                		
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
    		//businessDTO.setCopyUserValue(copyIds);
    		businessDTO.setCc(copyNames);
    	}
	}
    
    /** 审批各节点获取外部申请单数据
     * @throws Exception 
     * */ 
    @GET
    @Path("getBusinessInfo")
   public List<BusinessDTO> getBusinessById(@QueryParam("id")String id) throws Exception{
	   List<Business> businessInfo = businessManager.findBy("processInstanceId", id);
	   List <BusinessDTO> businessDTOList = new ArrayList<BusinessDTO>(); 
	   for(Business getInfo : businessInfo){
		   BusinessDTO businessDTO = new BusinessDTO(); 
		   businessDTO.setId(getInfo.getId());
		   businessDTO.setTheme(getInfo.getTheme());
		   businessDTO.setCc(getInfo.getCc());
		   businessDTO.setBusinessType(getInfo.getBusinessType());
		   businessDTO.setBusinessDetail(getInfo.getBusinessDetail());
		   businessDTO.setBusinessLevel(getInfo.getBusinessLevel());
		   businessDTO.setInitiator(getInfo.getInitiator());
		   businessDTO.setArea(getInfo.getArea());
		   businessDTO.setBranchOffice(getInfo.getBranchOffice());
		   businessDTO.setApplyContent(getInfo.getApplyContent());
		   businessDTO.setSubmitTimes(getInfo.getSubmitTimes());
		   businessDTO.setApplyCode(getInfo.getApplyCode());
		   businessDTO.setEnclosure(getInfo.getEnclosure());
		   businessDTO.setPath(getInfo.getPath());
		   businessDTO.setUserId(getInfo.getUserId());
		   businessDTO.setProcessInstanceId(getInfo.getProcessInstanceId());
		   
		   // 查询附件
	       List<StoreInfo> list = fileUploadAPI.getStore("OA/process", getInfo.getId().toString());
	       businessDTO.setStoreInfos(list);
		   businessDTOList.add(businessDTO);
		   
		   
	   }
		return businessDTOList;
	}
    
   
    
    /**
     * 完成任务.
     */
    @RequestMapping("process-operationBusinessApproval-completeTask")
    @Log(desc = "审批流程", action = "confirmProcess", operationDesc = "流程中心-我的审批-待办审批-分公司/大区业务")
    public String completeTask(HttpServletRequest request,@RequestParam(value = "files", required = false) MultipartFile[] files,
    		 @RequestParam(value = "iptdels", required = false) String iptdels,
            RedirectAttributes redirectAttributes,
            @RequestParam("processInstanceId") String processInstanceId, 
            @RequestParam("humanTaskId") String humanTaskId,
            String flag
           ) {
    	try {
    		businessService.saveReBusiness(request, redirectAttributes, processInstanceId,humanTaskId, files, iptdels);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            String errStr = Exceptions.getExceptionAllinformation(ex);
            
    		if (StringUtils.inString(errStr, "org.hibernate.exception.LockAcquisitionException") || 
    				StringUtils.inString(errStr, "com.mysql.jdbc.exceptions.jdbc4.MySQLTransactionRollbackException")) {
    			messageHelper.addFlashMessage(redirectAttributes, "系统繁忙或网络异常，请稍后重试！");
    		} else {
    			messageHelper.addFlashMessage(redirectAttributes, "任务不存在");
    		}
    		
            return "redirect:/humantask/workspace-personalTasks.do";
        }
    	
       return "operation/task-operation-completeTask";
    }
    //==================================================================================
    //验证密码是否正确
    @GET
    @Path("business-verifyPassword")
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
    @Log(desc = "查看详情页", action = "processDetail", operationDesc = "流程中心-分公司/大区业务-详情")
    public String formDetail( @RequestParam("processInstanceId") String processInstanceId,
    		@RequestParam(value = "isPrint", required = false) boolean isPrint,
    		@RequestParam(value = "viewBack", required = false) boolean viewBack,
    		Model model){
    	try {
			this.getBusinessById(processInstanceId);
			Business business = businessManager.findUniqueBy("processInstanceId", processInstanceId);
    		model.addAttribute("business", business);
			Long id = business.getId();
    		//取附件
    		model.addAttribute("picUrl", webAPI.getViewUrl());
    		List<StoreInfo> businessList = fileUploadAPI.getStore("OA/process", Long.toString(id));
    		model.addAttribute("StoreInfos", businessList);
    		//审批记录
            List<HumanTaskDTO> logHumanTaskDtos = humanTaskConnector
                    .findHumanTasksForPositionByProcessInstanceId(processInstanceId);
            //获得审核时长
            logHumanTaskDtos = operationService.settingAuditDuration(logHumanTaskDtos);
            //判断是否大区表单   ckx 2018/1/28
        	String countSql = "select count(*) from kv_record_condition c left join kv_record k on k.BUSINESS_KEY = c.businessKey where k.ref = ? and conditionName = 'region' and conditionValue = 'region'";
        	int count = jdbcTemplate.queryForObject(countSql, Integer.class,processInstanceId );
        	boolean isArea = false;
        	if(count > 0){
        		isArea = true;
        	}
        	String title = "";
        	Record findByRef = keyValueConnector.findByRef(processInstanceId);
        	BusinessDetailEntity businessDetailEntity=businessDetailManager.get(Long.parseLong(findByRef.getBusinessDetailId()));
        	if(null != businessDetailEntity){
        		title = businessDetailEntity.getTitle();
        	}
        	model.addAttribute("isArea", isArea);
        	model.addAttribute("title", title);
            model.addAttribute("logHumanTaskDtos", logHumanTaskDtos);
            model.addAttribute("isPrint", isPrint);
            model.addAttribute("viewBack", viewBack);
            operationService.copyMsgUpdate(processInstanceId);
		} catch (Exception e) {
			e.printStackTrace();
		}
    	return "operation/process/BusinessFormDetail";
    }
    /**
     * 大区业务申请单详情页
     * */
    @RequestMapping("areaForm-detail")
    public String areaFormDetail( @RequestParam("processInstanceId") String processInstanceId,
    		@RequestParam(value = "isPrint", required = false) boolean isPrint,
    		@RequestParam(value = "viewBack", required = false) boolean viewBack,
    		Model model){
    	try {
    		this.getBusinessById(processInstanceId);
    		Business business = businessManager.findUniqueBy("processInstanceId", processInstanceId);
    		model.addAttribute("business", business);
    		Long id = business.getId();
    		//取附件
    		model.addAttribute("picUrl", webAPI.getViewUrl());
    		List<StoreInfo> businessList = fileUploadAPI.getStore("OA/process", Long.toString(id));
    		model.addAttribute("StoreInfos", businessList);
    		//审批记录
    		List<HumanTaskDTO> logHumanTaskDtos = humanTaskConnector
    				.findHumanTasksForPositionByProcessInstanceId(processInstanceId);
    		
    		model.addAttribute("logHumanTaskDtos", logHumanTaskDtos);
    		model.addAttribute("isPrint", isPrint);
    		model.addAttribute("viewBack", viewBack);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	return "operation/process/AreaBusinessFormDetail";
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
    
    // ~ ======================================================================
    

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

    /**
     * 判断当前细分是否为大区
     * @param businessDetailId
     * @return
     */
    @RequestMapping("checkedArea")
    @ResponseBody
    public String checkedArea(@RequestParam(value="businessDetailId") String businessDetailId){
    	Map<String, Object> map = new HashMap<String, Object>();
    	boolean isArea = false;
    	String title = "";
    	String countSql = "select count(*) from oa_ba_process_condition where busDetailId = ? and conditionType = 'common-setting-area' and conditionName = 'common-setting-area'";
    	int count = jdbcTemplate.queryForObject(countSql, Integer.class, businessDetailId);
    	if(count > 0){
    		isArea = true;
    	}
    	BusinessDetailEntity businessDetailEntity=businessDetailManager.get(Long.parseLong(businessDetailId));
    	if(null != businessDetailEntity){
    		if(StringUtils.isNotBlank(businessDetailEntity.getTitle())){
    			title = businessDetailEntity.getTitle();
    		}
    	}
    	map.put("isArea", isArea);
    	map.put("title", title);
    	return JSONObject.toJSONString(map);
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
    public void setHumanTaskConnector(HumanTaskConnector humanTaskConnector) {
        this.humanTaskConnector = humanTaskConnector;
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
    public void setBusinessManager(BusinessManager businessManager) {
        this.businessManager = businessManager;
    }
    @Resource
	public void setBusinessService(BusinessService businessService) {
		this.businessService = businessService;
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

}
