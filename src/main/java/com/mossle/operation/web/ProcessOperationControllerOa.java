package com.mossle.operation.web;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mossle.api.form.FormConnector;
import com.mossle.api.humantask.HumanTaskConnector;
import com.mossle.api.keyvalue.FormParameter;
import com.mossle.api.keyvalue.KeyValueConnector;
import com.mossle.api.keyvalue.Record;
import com.mossle.api.org.OrgConnector;
import com.mossle.api.process.ProcessConnector;
import com.mossle.api.store.StoreConnector;
import com.mossle.api.tenant.TenantHolder;
import com.mossle.api.user.UserConnector;
import com.mossle.button.ButtonHelper;
import com.mossle.common.utils.FileUploadAPI;
import com.mossle.common.utils.WebAPI;
import com.mossle.core.MultipartHandler;
import com.mossle.core.auth.CurrentUserHolder;
import com.mossle.core.auth.CustomPasswordEncoder;
import com.mossle.core.mapper.JsonMapper;
import com.mossle.core.spring.MessageHelper;
import com.mossle.dict.persistence.domain.DictInfo;
import com.mossle.dict.support.DictConnectorImpl;
import com.mossle.humantask.persistence.manager.TaskInfoManager;
import com.mossle.keyvalue.persistence.domain.TimeTaskInfo;
import com.mossle.keyvalue.persistence.manager.RecordManager;
import com.mossle.keyvalue.persistence.manager.TimeTaskManager;
import com.mossle.operation.persistence.manager.ApplyManager;
import com.mossle.operation.service.OperationService;
import com.mossle.operation.service.WSApplyService;
import com.mossle.party.persistence.domain.PartyEntity;
import com.mossle.user.persistence.domain.AccountCredential;
import com.mossle.user.persistence.domain.AccountInfo;
import com.mossle.user.persistence.manager.AccountCredentialManager;
import com.mossle.user.persistence.manager.AccountInfoManager;
import com.mossle.user.support.ChangePasswordResult;
import com.mossle.ws.persistence.domain.OnLineInfo;
import com.mossle.ws.persistence.manager.OnLineInfoManager;

/** 
 * @author  cz 
 * @version 2017年7月26日
 * 直销oa推送过来的流程，在这里完成审批操作
 */

@Component
@Controller
@RequestMapping("operationOA")
@Path("operationOA")
public class ProcessOperationControllerOa {

    private static Logger logger = LoggerFactory.getLogger(ProcessOperationController.class);
    
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
    private ApplyManager applyManager;
    private AccountInfoManager accountInfoManager;
    private AccountCredentialManager accountCredentialManager;
    private CustomPasswordEncoder customPasswordEncoder;
    private FileUploadAPI fileUploadAPI;
    private WebAPI webAPI;
    private DictConnectorImpl dictConnectorImpl;
    private ProcessOperationController processOperationController;
    
    private TaskInfoManager taskInfoManager;
    private OnLineInfoManager onLineInfoManager;
	private JdbcTemplate jdbcTemplate;
	private RecordManager recordManager;
	private TimeTaskManager timeTaskManager;
	private WSApplyService wSApplyService;
	private OrgConnector orgConnector ;
	 // ~ ======================================================================
	    /**
	     * 完成任务.
	     */
	    @GET
	    @RequestMapping("OA-completeTask")
	    public String completeTask(HttpServletRequest request,
	            RedirectAttributes redirectAttributes,
	            @RequestParam("processInstanceId") String processInstanceId, 
	            @RequestParam("humanTaskId") String humanTaskId,
	            @RequestParam(value = "files", required = false) MultipartFile[] files,
	            @RequestParam(value = "iptdels", required = false) String iptdels,
	            @RequestParam(value = "iptresart", required = false) String iptresart,
	            @RequestParam(value = "iptoldid", required = false) String iptoldid,
	            String flag
	           ) throws Exception {

	    	//获得当前登录人的ID和姓名
	    	String userId = currentUserHolder.getUserId();
	    	
	    	// String userName = currentUserHolder.getUsername();
	    	
	    	Map<String, Object> processParameters = new HashMap<String, Object>();
	    	
	    	/*FormParameter formParameter = this.doSaveRecord(request);
	        Record record = keyValueConnector.findByCode(formParameter.getBusinessKey());*/
	        
	    	MultipartHandler multipartHandler = new MultipartHandler(multipartResolver);
	        FormParameter formParameter = null;
	        multipartHandler.handle(request);
	        formParameter = this.buildFormParameter(multipartHandler);
	            
			Record record = keyValueConnector.findByRef(processInstanceId);
			formParameter.setBusinessKey(record.getBusinessKey());
			
	        MultiValueMap<String,String> m = formParameter.getMultiValueMap();
	        
	        //验证前登录人岗位是否在线专员岗，是 返回1，否 返回0
	        String isAuthorization = isAuthorization(userId,record.getApplyCode(),formParameter.getBusinessKey());
		
	        if(isAuthorization.equals("1")){
	        	OnLineInfo onLineInfo=onLineInfoManager.findUnique("from OnLineInfo where applycode=?", record.getApplyCode());
	        	if(onLineInfo!=null)
	        	{
	        		onLineInfo.setIsAuthCertificate("0");
	        		onLineInfoManager.save(onLineInfo);
	        	}
	        	//若该人不是第一次审核，还是先将这个标识置回0
	        	/*String strSql="UPDATE ro_pf_oaonline SET chrIsAuthCertificate = '0' WHERE varApplyCode = '"+record.getApplyCode()+"'";
				jdbcTemplate.update(strSql);*/
	        } 

	        String f = m.getFirst("flag");
	        if (f.equals("0")) {
	        	String strApplyType=record.getBusinessDetailId();
	        	if(strApplyType.equals("8")||strApplyType.equals("9"))
	        	{
	        		OnLineInfo onLineInfo=onLineInfoManager.findUnique("from OnLineInfo where applycode=?", record.getApplyCode());
		        	if(onLineInfo!=null)
		        	{
		        		onLineInfo.setIsAuthCertificate("0");
		        		onLineInfoManager.save(onLineInfo);
		        	}
	        	}
	        	processParameters.put("leaderComment", "不同意");
	        	formParameter.setAction("不同意");
	        }
	        if (f.equals("1")) {
	        	processParameters.put("leaderComment", "同意");
	        	formParameter.setAction("同意");
	        }
	        if (f.equals("2")) {
		        processParameters.put("leaderComment", "驳回");
		        formParameter.setAction("驳回");
	        } 
	       
	        
	        //【在线专员岗】同意并授权
	        if (f.equals("4")) {
		        processParameters.put("leaderComment", "同意");
		        formParameter.setAction("同意");
		        
		         //再创建一条标识 这个流程时同意并授权的
		        /*TimeTaskInfo timeTaskEndService=new TimeTaskInfo();
				timeTaskEndService.setTaskType("createsab");
				timeTaskEndService.setTaskContent(record.getApplyCode());
				timeTaskEndService.setTaskAddDate(new Date());
				timeTaskEndService.setTaskNote("0");//0 表示该条流程在专员岗这里同意并授权了，但是该条流程还没有完成全部审核
				timeTaskManager.save(timeTaskEndService);*/
				
				formParameter.setComment((formParameter.getComment()==null?"":formParameter.getComment())+" <br/><font style='color:red'>同意旗舰店申请，并允许发放旗舰店授权书</font>");
				
				OnLineInfo onLineInfo=onLineInfoManager.findUnique("from OnLineInfo where applycode=?", record.getApplyCode());
	        	if(onLineInfo!=null)
	        	{
	        		onLineInfo.setIsAuthCertificate("1");
	        		onLineInfoManager.save(onLineInfo);
	        	}
				//同意并授权  在 ro_pf_oaonline 表中标识一下，最后一步审批的时候，有这个标识的就调用生成授权书
				/*String strSql="UPDATE ro_pf_oaonline SET chrIsAuthCertificate = '1' WHERE varApplyCode = '"+record.getApplyCode()+"'";
				jdbcTemplate.update(strSql);*/
	        } 
	        
		   try {
	            this.operationService.completeTask(humanTaskId, userId,
	                    formParameter, processParameters, record,
	                    processInstanceId);
	           
             wSApplyService.SetOATimeTask(formParameter.getBusinessKey(), humanTaskId);
		   } catch (IllegalStateException ex) {
	            logger.error(ex.getMessage(), ex);
	            messageHelper.addFlashMessage(redirectAttributes, "任务不存在");

	            return "redirect:/humantask/workspace-personalTasks.do";
	        }
	       return "operation/task-operation-completeTask";
	    }
		  
	    /**
	     * 验证前登录人岗位是否在线专员岗，是 返回1，否 返回0
	     * @param userId
	     * @param applyCode
	     * @param businesskey
	     * @return
	     */
	    public String isAuthorization(String userId ,String applyCode,String businesskey)  {
	    	String isAuthorization = "0";//0 标识否，1 是
	    	String post_id = "";
	    	//到数据字典中取 在线专员的岗位ID
	    	List<DictInfo> dictInfo = this.dictConnectorImpl.findDictInfoListByType("onLinePostID");
	    	String dictValue = dictInfo.get(0).getValue();
	        //确定业务类型细分是【 “12万旗舰店申请”或“非12万旗舰店申请”】
	    	String sql = "select * from kv_record where business_key = "+businesskey+" and  businessDetailId IN ( '8','9')";
	    	List<Map<String, Object>>  s = jdbcTemplate.queryForList(sql);
	    	if(s!=null&&s.size()>0){
	    		//取当前登录人的所属岗位的ID
	    		List<PartyEntity> partyDTO = orgConnector.getPostByUserId(userId);
		        if (partyDTO.size() > 0) {
		           for (int i = 0; i < partyDTO.size(); i++) {
		          	  post_id =Long.toString( partyDTO.get(i).getId());
		          	  //若当前登录人岗位就是在线专员岗
		          	  if(post_id.equals(dictValue)){
		          		  isAuthorization = "1";
		          		  break;	 
		          	  }
		           }
		        }
	    	}
	       return isAuthorization;
       }
	    
	    // ~ ======================================================================
	    //验证密码是否正确
	    @GET
	    @Path("OA-verifyPassword")
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
	    // ~ ====================================================================== 
	    
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
            
            // formParameter.setBpmProcessId("797146747797504");
            
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
    public void setApplyManager(ApplyManager applyManager) {
        this.applyManager = applyManager;
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
	public void setProcessOperationController(
			ProcessOperationController processOperationController) {
		this.processOperationController = processOperationController;
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
    public void setDictConnectorImpl(DictConnectorImpl dictConnectorImpl){
    	this.dictConnectorImpl=dictConnectorImpl;
    }
    
    @Resource
	public void setTaskInfoManager(TaskInfoManager taskInfoManager) {
		this.taskInfoManager = taskInfoManager;
	}
    
    @Resource
	public void setOnLineInfoManager(OnLineInfoManager onLineInfoManager) {
		this.onLineInfoManager = onLineInfoManager;
	}
	
	@Resource
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
	
	@Resource
	public void setRecordManager(RecordManager recordManager) {
		this.recordManager = recordManager;
	}
	
	@Resource
	public void setTimeTaskManager(TimeTaskManager timeTaskManager){
		this.timeTaskManager = timeTaskManager;
	}
	
	@Resource
	public void setWSApplyService(WSApplyService wSApplyService) {
		this.wSApplyService = wSApplyService;
	}
	
	@Resource
	public void setOrgConnector(OrgConnector orgConnector) {
		this.orgConnector = orgConnector;
	}
}
