package com.mossle.operation.web;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
import com.mossle.core.MultipartHandler;
import com.mossle.core.annotation.Log;
import com.mossle.core.auth.CurrentUserHolder;
import com.mossle.core.auth.CustomPasswordEncoder;
import com.mossle.core.mapper.JsonMapper;
import com.mossle.core.spring.MessageHelper;
import com.mossle.operation.persistence.domain.Product;
import com.mossle.operation.persistence.domain.Return;
import com.mossle.operation.persistence.domain.ReturnDTO;
import com.mossle.operation.persistence.manager.ProductManager;
import com.mossle.operation.persistence.manager.ReturnManager;
import com.mossle.operation.service.OperationService;
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
@RequestMapping("Return")
@Path("Return")
public class ProcessOperationReturnController {
	
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
    
    private ReturnManager returnManager;
    private ProductManager productManager;
    private ReturnService returnService;
    private AccountInfoManager accountInfoManager;
    private AccountCredentialManager accountCredentialManager;
    private CustomPasswordEncoder customPasswordEncoder;
    //private ReturnShopDetailManager returnShopDetailManager;
    /**
     * 发起流程.
     */
    @RequestMapping("process-operationReturn-startProcessInstance")
    @Log(desc = "发起流程", action = "startProcess", operationDesc = "流程中心-我的流程-发起流程-退货")
    public String startProcessInstance(HttpServletRequest request, @ModelAttribute ReturnDTO returnDTO,String areaId,String areaName,
    		String companyId,String companyName,
    		@RequestParam(value = "proNo") String proNo,//产品编号
    		@RequestParam(value = "proName") String proName,//产品名称
    		@RequestParam(value = "shopPVNum") String shopPVNum,//店支付产品数量
    		@RequestParam(value = "shopReturn") String shopReturn,//店支付退回的数量
    		@RequestParam(value = "shopProPV") String shopProPV,//店支付退回产品的总pv
    		@RequestParam(value = "shopRewardNum") String shopRewardNum,//奖励积分产品数量
    		@RequestParam(value = "rewardReturn") String rewardReturn,//奖励积分退回的数量
    		@RequestParam(value = "shopRewardPV") String shopRewardPV,//奖励积分退回产品总pv
    		@RequestParam(value = "shopWalletNum") String shopWalletNum,//个人钱包产品数量
    		@RequestParam(value = "walletReturn") String walletReturn,//个人钱包退回数量
    		@RequestParam(value = "shopWalletPV") String shopWalletPV,//个人钱包退回产品总pv
    		@RequestParam(value = "proPV") String proPV,//产品单价pv
            @RequestParam("bpmProcessId") String bpmProcessId, HumanTaskDTO humanTaskDTO,
            @RequestParam("businessKey") String businessKey, Model model) throws Exception {
    	
        String userId = currentUserHolder.getUserId();
        
        List<Product> proList = new ArrayList<Product>();
        String [] proNos = proNo.split(",");
        String [] proNames = proName.split(",");
        String [] shopPVNums = shopPVNum.split(",");
        String [] shopReturns = shopReturn.split(",");
        String [] shopProPVs = shopProPV.split(",");
        String [] shopRewardNums = shopRewardNum.split(",");
        String [] rewardReturns = rewardReturn.split(",");
        String [] shopRewardPVs = shopRewardPV.split(",");
        String [] shopWalletNums = shopWalletNum.split(",");
        String [] walletReturns = walletReturn.split(",");
        String [] shopWalletPVs = shopWalletPV.split(",");
        String [] proPVs = proPV.split(",");
        for(int i = 0;i<proNames.length;i++){
        	Product product = new Product();
        	product.setProNo(proNos[i]);
        	product.setProName(proNames[i]);
        	product.setShopPVNum(shopPVNums[i]);
        	product.setShopReNum(shopReturns[i]);
        	product.setShopPV(shopProPVs[i]);
        	product.setShopRewardNum(shopRewardNums[i]);
        	product.setShopRewNum(rewardReturns[i]);
        	product.setShopRewardPV(shopRewardPVs[i]);
        	product.setShopWalletNum(shopWalletNums[i]);
        	product.setShopwalNum(walletReturns[i]);
        	product.setShopWalletPV(shopWalletPVs[i]);
        	product.setProPV(proPVs[i]);
        	proList.add(product);
        }
        
        returnService.saveReturn(request,returnDTO, proList,userId,areaId,areaName,companyId,companyName, businessKey);
        
        return "operation/process-operation-startProcessInstance";
    }
    
    
    
    
    
    /** 审批各节点获取外部申请单数据*/ 
    @GET
    @Path("getReturnInfo")
   public List<ReturnDTO> getReturnById(@QueryParam("id")String id){

	   List<Return> returnInfo = returnManager.findBy("processInstanceId", id);
	   List <ReturnDTO> returnDTOList = new ArrayList<ReturnDTO>(); 
	   SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
	   for(Return getInfo : returnInfo){
		   ReturnDTO returnDTO = new ReturnDTO();
		   returnDTO.setWareHouse(getInfo.getWareHouse());
		   returnDTO.setEmpNo(getInfo.getEmpNo());
		   returnDTO.setUcode(getInfo.getUcode());
		   returnDTO.setShopName(getInfo.getShopName());
		   returnDTO.setShopTel(getInfo.getShopTel());
		   returnDTO.setReturnDate(formatter.format(getInfo.getReturnDate()));
		   returnDTO.setOrderNumber(getInfo.getOrderNumber());
		   returnDTO.setReturnReaon(getInfo.getReturnReaon());
		   returnDTO.setShopPayStock(getInfo.getShopPayStock());
		   returnDTO.setPersonPayStock(getInfo.getPersonPayStock());
		   returnDTO.setRewardIntegralStock(getInfo.getRewardIntegralStock());
		   returnDTO.setPayType(getInfo.getPayType());
		   returnDTO.setProcessInstanceId(getInfo.getProcessInstanceId());
		   returnDTO.setUserId(getInfo.getUserId());
		   returnDTO.setId(getInfo.getId());
		   returnDTO.setSubmitTimes(getInfo.getSubmitTimes());
		   //ckx
		   returnDTO.setInputApplyCode(getInfo.getInputApplyCode());
		   returnDTO.setBankDeposit(getInfo.getBankDeposit());
		   returnDTO.setAccountName(getInfo.getAccountName());
		   returnDTO.setAccountNumber(getInfo.getAccountNumber());
		   returnDTOList.add(returnDTO);
		   
		   
	   }
		return returnDTOList;
	}
    @GET
    @Path("getReturnProductInfo")
    public List<Product> getReturnProduct(@QueryParam("id")String id){
    	List<Product> list = null;
    	Return re = returnManager.findUniqueBy("processInstanceId", id);
    	if(re != null){
    		Long returnId = re.getId();
        	list = (List<Product>) productManager.findBy("returnId", returnId);
    	}else{
    		return list;
    	}
    	return list;
    }
    /** 审批各节点获取外部产品申请单数据*/ 
    @GET
    @Path("getProductInfo")
    public List<Product> getProductById(@QueryParam("id")Long id){
    	List<Product> productInfo = productManager.findBy("returnId", id);
    	List <Product> productDTOList = new ArrayList<Product>(); 
    	for(Product getInfo : productInfo){
    		Product product = new Product();
    		product.setId(getInfo.getId());
    		product.setProName(getInfo.getProName());
    		//店支付
    		product.setShopPVNum(getInfo.getShopPVNum());
    		product.setShopReNum(getInfo.getShopReNum());
    		product.setShopPV(getInfo.getShopPV());
    		//积分奖励
    		product.setShopRewardNum(getInfo.getShopRewardNum());
    		product.setShopRewNum(getInfo.getShopRewNum());
    		product.setShopRewardPV(getInfo.getShopRewardPV());
    		//个人钱包
    		product.setShopWalletNum(getInfo.getShopWalletNum());
    		product.setShopwalNum(getInfo.getShopwalNum());
    		product.setShopWalletPV(getInfo.getShopWalletPV());
    		productDTOList.add(product);
    		
    		
    	}
    	return productDTOList;
    }
    /**
     * 申请单详情或打印页
     * */
    @RequestMapping("from-detail")
    @Log(desc = "查看详情页", action = "processDetail", operationDesc = "流程中心-退货-详情")
    public String formDetail( @RequestParam("processInstanceId") String processInstanceId,Long returnId,
    		@RequestParam(value = "isPrint", required = false) boolean isPrint,
    		Model model){
    	this.getReturnById(processInstanceId);
    	this.getProductById(returnId);
    	 //审批记录
        List<HumanTaskDTO> logHumanTaskDtos = humanTaskConnector
                .findHumanTasksForPositionByProcessInstanceId(processInstanceId);
        //获得审核详情
        logHumanTaskDtos = operationService.settingAuditDuration(logHumanTaskDtos);
        model.addAttribute("logHumanTaskDtos", logHumanTaskDtos);
        model.addAttribute("isPrint", isPrint);
        operationService.copyMsgUpdate(processInstanceId);
        return "operation/process/ReturnFormDetail";
      
    }
    /**
     * 完成任务.
     */
    @RequestMapping("process-operationReturnApproval-completeTask")
    @Log(desc = "审批流程", action = "confirmProcess", operationDesc = "流程中心-我的审批-待办审批-退货")
    public String completeTask(HttpServletRequest request,
            RedirectAttributes redirectAttributes,
            @RequestParam("processInstanceId") String processInstanceId, 
            @RequestParam("humanTaskId") String humanTaskId,
            String flag
           ) throws Exception {
    	
    	try {
    		returnService.saveReReturn(request, redirectAttributes, processInstanceId, humanTaskId);
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
    @Path("return-verifyPassword")
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
   /* @RequestMapping("from-detail")
    public String formDetail( @RequestParam("processInstanceId") String processInstanceId){
    	this.getReturnById(processInstanceId);
    	return "operation/process/ReturnFormDetail";
    }*/
    
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
       formParameter.setComment(multipartHandler.getMultiValueMap()
    		   .getFirst("comment"));

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
    public void setReturnManager(ReturnManager returnManager) {
        this.returnManager = returnManager;
    }
    @Resource
    public void setProductManager(ProductManager productManager) {
    	this.productManager = productManager;
    }

    @Resource
	public void setReturnService(ReturnService returnService) {
		this.returnService = returnService;
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
}
