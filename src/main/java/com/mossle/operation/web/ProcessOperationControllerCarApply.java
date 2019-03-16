package com.mossle.operation.web;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import org.apache.axis.utils.StringUtils;
import org.apache.poi.ss.formula.functions.Now;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartResolver;
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
import com.mossle.button.ButtonDTO;
import com.mossle.button.ButtonHelper;
import com.mossle.common.utils.FileUploadAPI;
import com.mossle.common.utils.WebAPI;
import com.mossle.core.MultipartHandler;
import com.mossle.core.annotation.Log;
import com.mossle.core.auth.CurrentUserHolder;
import com.mossle.core.auth.CustomPasswordEncoder;
import com.mossle.core.mapper.JsonMapper;
import com.mossle.core.spring.MessageHelper;
import com.mossle.internal.store.persistence.domain.StoreInfo;
import com.mossle.operation.persistence.domain.CancelOrderSub;
import com.mossle.operation.persistence.domain.CarApply;
import com.mossle.operation.persistence.domain.CarApplyDTO;
import com.mossle.operation.persistence.domain.Exchange;
import com.mossle.operation.persistence.domain.ExchangeDTO;
import com.mossle.operation.persistence.domain.ExchangeProducts;
import com.mossle.operation.persistence.domain.ExchangeProductsDTO;
import com.mossle.operation.persistence.domain.Product;
import com.mossle.operation.persistence.domain.Return;
import com.mossle.operation.persistence.domain.ReturnDTO;
import com.mossle.operation.persistence.manager.CarApplyManager;
import com.mossle.operation.persistence.manager.ExchangeManager;
import com.mossle.operation.persistence.manager.ExchangeProductsManager;
import com.mossle.operation.persistence.manager.ProductManager;
import com.mossle.operation.persistence.manager.ReturnManager;
import com.mossle.operation.service.CarApplyService;
import com.mossle.operation.service.CustomWorkService;
import com.mossle.operation.service.ExchangeService;
import com.mossle.operation.service.OperationService;
import com.mossle.operation.service.QualityExchangeService;
import com.mossle.operation.service.ReturnService;
import com.mossle.user.persistence.domain.AccountCredential;
import com.mossle.user.persistence.domain.AccountInfo;
import com.mossle.user.persistence.manager.AccountCredentialManager;
import com.mossle.user.persistence.manager.AccountInfoManager;
import com.mossle.user.support.ChangePasswordResult;
import com.mossle.xform.Xform;
import com.mossle.xform.XformBuilder;


/**
 * 流程操作.
 * 
 * 
 */
@Component
@Controller
@RequestMapping("CarApply")
@Path("CarApply")
public class ProcessOperationControllerCarApply {
	
    private static Logger logger = LoggerFactory.getLogger(ProcessOperationController.class);
    
    public static final int STATUS_DRAFT_PROCESS = 0;
    public static final int STATUS_DRAFT_TASK = 1;
    public static final int STATUS_RUNNING = 2;
    
    
    private OperationService operationService;
    
    private KeyValueConnector keyValueConnector;
    private MessageHelper messageHelper;
    private CurrentUserHolder currentUserHolder;
    private HumanTaskConnector humanTaskConnector;
    private MultipartResolver multipartResolver;
    private StoreConnector storeConnector;
    private ButtonHelper buttonHelper = new ButtonHelper();
    private FormConnector formConnector;
    private JsonMapper jsonMapper = new JsonMapper();
    private TenantHolder tenantHolder;
    private UserConnector userConnector;
    private WebAPI webAPI;;
    private FileUploadAPI fileUploadAPI;
    private ExchangeManager exchangeManager;
    private ExchangeProductsManager exchangeProductsManager;
    private AccountInfoManager accountInfoManager;
    private AccountCredentialManager accountCredentialManager;
    private CustomPasswordEncoder customPasswordEncoder;
    //private ReturnShopDetailManager returnShopDetailManager;
    @Autowired
    private CustomWorkService customWorkService;
    private CarApplyService carApplyService;
    private CarApplyManager carApplyManager;
    
    /**
     * 发起流程.
     */
    @RequestMapping("CarApply-startProcessInstance")
    @Log(desc = "发起流程", action = "startProcess", operationDesc = "流程中心-我的流程-发起流程-用车申请")
    public String startProcessInstance(HttpServletRequest request, @ModelAttribute CarApplyDTO carApplyDTO,
    		String companyId,String companyName,
    		//@RequestParam(value = "quality", required = false) String quality,
    		@RequestParam(value = "files", required = false) MultipartFile [] files,
    
            @RequestParam("bpmProcessId") String bpmProcessId, HumanTaskDTO humanTaskDTO,
            @RequestParam("businessKey") String businessKey, Model model) throws Exception {
    	
        String userId = currentUserHolder.getUserId();
        String tenantId = tenantHolder.getTenantId();
        String result = "1";
      
        
        try{
        	carApplyService.saveCarApply(request, carApplyDTO, userId, companyId, companyName, businessKey, tenantId);
        }catch(Exception e){
        	e.printStackTrace();
        	result = e.getMessage();
        	
        }
        
        return "operation/process-operation-startProcessInstance";
    }
    
    /** 审批各节点获取外部申请单数据*/ 
    @GET
    @Path("getCarApplyInfo")
   public List<CarApplyDTO> getCarApplyById(@QueryParam("id")String id ){

	   List<CarApply> returnInfo = carApplyManager.findBy("processInstanceID", id);
	   List <CarApplyDTO> returnDTOList = new ArrayList<CarApplyDTO>(); 
	   SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
	   for(CarApply getInfo : returnInfo){
		   
		   //取主表数据
		    CarApplyDTO carApplyDTO = new CarApplyDTO();
		    carApplyDTO.setUcode(getInfo.getUcode());
		    carApplyDTO.setApplyCode(getInfo.getApplyCode());//受理单编号
	        carApplyDTO.setCarUser(getInfo.getCarUser());//用车人姓名
	        carApplyDTO.setUcode(getInfo.getUcode());//发起人id
	        carApplyDTO.setContent(getInfo.getContent());//用车事由
	        carApplyDTO.setDepartmentCode(getInfo.getDepartmentCode());//部门编码
	        carApplyDTO.setDepartmentName(getInfo.getDepartmentName());//部门名称
	        carApplyDTO.setBusinessType(getInfo.getBusinessType());
	        carApplyDTO.setBusinessDetail(getInfo.getBusinessDetail());
	        carApplyDTO.setDestination(getInfo.getDestination());//目的地
	        carApplyDTO.setBorrowCarTime(getInfo.getBorrowCarTime());//借车时间
	        carApplyDTO.setReturnCarTime(getInfo.getReturnCarTime());//还车时间
	        carApplyDTO.setTotalTime(getInfo.getTotalTime());//共计时长
	        carApplyDTO.setPlateNumber(getInfo.getPlateNumber());
	        carApplyDTO.setDriver(getInfo.getDriver());
	        carApplyDTO.setBorrowCarMileage(getInfo.getBorrowCarMileage());
	        carApplyDTO.setReturnCarMileage(getInfo.getReturnCarMileage());
	        carApplyDTO.setMileage(getInfo.getMileage());
	        carApplyDTO.setOilMoney(getInfo.getOilMoney());
	        carApplyDTO.setRemainOil(getInfo.getRemainOil());
	        
	        
	       returnDTOList.add(carApplyDTO);
	    
	   }
	   
		return returnDTOList;
	}
    
 
    
    /**
     * 完成任务.
     */
    @RequestMapping("CarApply-completeTask")
    @Log(desc = "审批流程", action = "confirmProcess", operationDesc = "流程中心-我的审批-待办审批-质量换货")
    public String completeTask(HttpServletRequest request,
            RedirectAttributes redirectAttributes,
            @RequestParam("processInstanceId") String processInstanceId, 
            @RequestParam("humanTaskId") String humanTaskId
           ) throws Exception {
    	
    	try {
    		carApplyService.saveCarApply(request, redirectAttributes, processInstanceId, humanTaskId);
        } catch (IllegalStateException ex) {
            logger.error(ex.getMessage(), ex);
            messageHelper.addFlashMessage(redirectAttributes, "任务不存在");

            return "redirect:/humantask/workspace-personalTasks.do";
        }
    	
       return "operation/task-operation-completeTask";
    	
    }
    //==================================================================================
    //验证密码是否正确
    @GET
    @Path("exchange-verifyPassword")
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
     * @throws Exception 
     * */

    /**
     * 申请单详情或打印页
     * */
    @RequestMapping("car-apply-detail")
    @Log(desc = "查看详情页", action = "processDetail", operationDesc = "流程中心-质量问题换货-详情")
    public String formDetail( @RequestParam("processInstanceId") String processInstanceId,
    		@RequestParam(value = "isPrint", required = false) boolean isPrint,
    		@RequestParam(value = "viewBack", required = false) boolean viewBack,
    		Model model){
    	
    	 //审批记录
        List<HumanTaskDTO> logHumanTaskDtos = humanTaskConnector
                .findHumanTasksForPositionByProcessInstanceId(processInstanceId);
        //获得审核时长
        logHumanTaskDtos = operationService.settingAuditDuration(logHumanTaskDtos);
        model.addAttribute("logHumanTaskDtos", logHumanTaskDtos);
        model.addAttribute("isPrint", isPrint);
        model.addAttribute("viewBack", viewBack);
        model.addAttribute("processInstanceId", processInstanceId);
        operationService.copyMsgUpdate(processInstanceId);
        
        return "operation/car-apply-detail";
      
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
    
    
   
    
   
  /*  @RequestMapping("task-operation-ReturnDisagree")
    public String disagree(HttpServletRequest request, @RequestParam("humanTaskId") String humanTaskId,String activityId,String comment){
    	humanTaskConnector.skip("end", activityId, "");
    	return "redirect:/humantask/workspace-personalTasks.do";
    }*/
    

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
    public void setExchangeManager(ExchangeManager exchangeManager) {
        this.exchangeManager = exchangeManager;
    }
    @Resource
    public void setExchangeProductsManager(ExchangeProductsManager exchangeProductsManager) {
    	this.exchangeProductsManager = exchangeProductsManager;
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
    public void setFileUploadAPI(FileUploadAPI fileUploadAPI) {
    	this.fileUploadAPI = fileUploadAPI;
    }


	@Resource
	public void setCarApplyService(CarApplyService carApplyService) {
		this.carApplyService = carApplyService;
	}
    
	@Resource
	public void setCarApplyManager(CarApplyManager carApplyManager) {
		this.carApplyManager = carApplyManager;
	}
	
}
