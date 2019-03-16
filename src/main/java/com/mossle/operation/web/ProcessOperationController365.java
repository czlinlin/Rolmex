package com.mossle.operation.web;

import java.util.Date;
import java.util.Calendar;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat; 
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

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
import com.mossle.base.persistence.domain.DetailPostEntity;
import com.mossle.base.rs.BusinessResource.BusinessDetailDTO;
import com.mossle.button.ButtonDTO;
import com.mossle.button.ButtonHelper;
import com.mossle.cms.persistence.domain.CmsAttachment;
import com.mossle.common.utils.Exceptions;
import com.mossle.common.utils.FileUploadAPI;
import com.mossle.common.utils.StringUtils;
import com.mossle.common.utils.WebAPI;
import com.mossle.core.MultipartHandler;
import com.mossle.core.annotation.Log;
import com.mossle.core.auth.CurrentUserHolder;
import com.mossle.core.auth.CustomPasswordEncoder;
import com.mossle.core.mapper.JsonMapper;
import com.mossle.core.page.Page;
import com.mossle.core.query.PropertyFilter;
import com.mossle.core.spring.MessageHelper;
import com.mossle.core.store.MultipartFileDataSource;
import com.mossle.core.util.ServletUtils;
import com.mossle.dict.persistence.domain.DictInfo;
import com.mossle.dict.support.DictConnectorImpl;
import com.mossle.internal.store.persistence.domain.StoreInfo;
import com.mossle.operation.persistence.domain.Apply;
import com.mossle.operation.persistence.domain.ApplyDTO;
import com.mossle.operation.persistence.domain.CustomEntity;
import com.mossle.operation.persistence.domain.TestEntity;
import com.mossle.operation.persistence.domain.TestEntityDTO;
import com.mossle.operation.persistence.manager.ApplyManager;
import com.mossle.operation.persistence.manager.TestEntityManager;
import com.mossle.operation.service.OperationService;
import com.mossle.party.persistence.domain.PartyEntity;
import com.mossle.party.persistence.manager.PartyEntityManager;
import com.mossle.pim.persistence.domain.WorkReportAttachment;
import com.mossle.project.persistence.domain.WorkProjectInfo;
import com.mossle.project.persistence.domain.WorkProjectNotify;
import com.mossle.user.persistence.domain.AccountCredential;
import com.mossle.user.persistence.domain.AccountInfo;
import com.mossle.user.persistence.manager.AccountCredentialManager;
import com.mossle.user.persistence.manager.AccountInfoManager;
import com.mossle.user.support.ChangePasswordResult;
import com.mossle.xform.Xform;
import com.mossle.xform.XformBuilder;

import org.activiti.engine.ProcessEngines;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** 
 * @author  cz 
 * @version 2017年7月26日
 * 直销oa推送过来的流程，在这里完成审批操作
 */

@Component
@Controller
@RequestMapping("pinzhi365")
@Path("pinzhi365")
public class ProcessOperationController365 {

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
    
    
    
	 // ~ ======================================================================
    /**
     * 完成任务.
     */
    @GET
    @RequestMapping("pinzhi365-completeTask")
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

    	Map<String, Object> processParameters = new HashMap<String, Object>();
    	
    	/*FormParameter formParameter = this.doSaveRecord(request);
        Record record = keyValueConnector.findByCode(formParameter.getBusinessKey());
        */
    	
        MultipartHandler multipartHandler = new MultipartHandler(multipartResolver);
        FormParameter formParameter = null;
        multipartHandler.handle(request);
        formParameter = this.buildFormParameter(multipartHandler);
            
		Record record = keyValueConnector.findByRef(processInstanceId);
		formParameter.setBusinessKey(record.getBusinessKey());
		
        MultiValueMap<String,String> m = formParameter.getMultiValueMap();
    
        String f = m.getFirst("flag");
        if (f.equals("0")) {
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
        
	   try {
            this.operationService.completeTask(humanTaskId, userId,
                    formParameter, processParameters, record,
                    processInstanceId);
        } catch (IllegalStateException ex) {
        	logger.error(ex.getMessage(), ex);
            String errStr = Exceptions.getExceptionAllinformation(ex);
            
    		if (StringUtils.inString(errStr, "org.hibernate.exception.LockAcquisitionException") || 
    				StringUtils.inString(errStr, "com.mysql.jdbc.exceptions.jdbc4.MySQLTransactionRollbackException")) {
    			messageHelper.addFlashMessage(redirectAttributes, "系统繁忙或网络异常，请稍后重试！");
    		} else if (StringUtils.inString(errStr, "任务不存在")){
    			messageHelper.addFlashMessage(redirectAttributes, "任务不存在");
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
	    @Path("pinzhi365-verifyPassword")
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

            if(StringUtils.isBlank(formParameter.getBusinessKey())) {
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
    
}
