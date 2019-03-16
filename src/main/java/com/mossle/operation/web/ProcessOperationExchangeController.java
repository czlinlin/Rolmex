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
import com.mossle.operation.persistence.domain.Exchange;
import com.mossle.operation.persistence.domain.ExchangeDTO;
import com.mossle.operation.persistence.domain.ExchangeProducts;
import com.mossle.operation.persistence.domain.ExchangeProductsDTO;
import com.mossle.operation.persistence.domain.Product;
import com.mossle.operation.persistence.domain.Return;
import com.mossle.operation.persistence.domain.ReturnDTO;
import com.mossle.operation.persistence.manager.ExchangeManager;
import com.mossle.operation.persistence.manager.ExchangeProductsManager;
import com.mossle.operation.persistence.manager.ProductManager;
import com.mossle.operation.persistence.manager.ReturnManager;
import com.mossle.operation.service.CustomWorkService;
import com.mossle.operation.service.ExchangeService;
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
@RequestMapping("Exchange")
@Path("Exchange")
public class ProcessOperationExchangeController {
	
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
    private FileUploadAPI fileUploadAPI;;
    
    private ExchangeManager exchangeManager;
    private ExchangeProductsManager exchangeProductsManager;
    private ExchangeService exchangeService;
    private AccountInfoManager accountInfoManager;
    private AccountCredentialManager accountCredentialManager;
    private CustomPasswordEncoder customPasswordEncoder;
    //private ReturnShopDetailManager returnShopDetailManager;
    @Autowired
    private CustomWorkService customWorkService;
    /**
     * 发起流程.
     */
    @RequestMapping("process-operationExchange-startProcessInstance")
    @Log(desc = "发起流程", action = "startProcess", operationDesc = "流程中心-我的流程-发起流程-换货")
    public String startProcessInstance(HttpServletRequest request, @ModelAttribute ExchangeDTO exchangeDTO,String areaId,String areaName,
    		String companyId,String companyName,
    		@RequestParam(value = "quality", required = false) String quality,
    		@RequestParam(value = "files", required = false) MultipartFile [] files,
    		@RequestParam(value = "backType", required = false) String backType,
    		@RequestParam(value = "backProName", required = false) String backProName,
    		@RequestParam(value = "maxNumber", required = false) String maxNumber,//最大产品数量
    		@RequestParam(value = "backNumber", required = false) String backNumber,
    		@RequestParam(value = "backTotalPrice", required = false) String backTotalPrice,
    		@RequestParam(value = "backTotalPV", required = false) String backTotalPV,
    		@RequestParam(value = "backUnitPrice", required = false) String backUnitPrice,//退货的单价
    		@RequestParam(value = "backUnitPV", required = false) String backUnitPV,
    		@RequestParam(value = "backProNo", required = false) String backProNo,//退货的编号
    		@RequestParam(value = "productionDate", required = false) String productionDate,//生产日期
    		@RequestParam(value = "qualityAssuranceDate", required = false) String qualityAssuranceDate,//质保期
    		@RequestParam(value = "exchangeType", required = false) String exchangeType,
    		@RequestParam(value = "exchangeProName", required = false) String exchangeProName,
    		@RequestParam(value = "exchangeNumber", required = false) String exchangeNumber,
    		@RequestParam(value = "exchangeTotalPrice", required = false) String exchangeTotalPrice,
    		@RequestParam(value = "exchangeTotalPV", required = false) String exchangeTotalPV,
    		@RequestParam(value = "exchangeUnitPrice", required = false) String exchangeUnitPrice,//换货的单价
    		@RequestParam(value = "exchangeUnitPV", required = false) String exchangeUnitPV,
    		@RequestParam(value = "exchangeProNo", required = false) String exchangeProNo,//换货的编号
            @RequestParam("bpmProcessId") String bpmProcessId, HumanTaskDTO humanTaskDTO,
            @RequestParam("businessKey") String businessKey, Model model) throws Exception {
    	
        String userId = currentUserHolder.getUserId();
        String tenantId = tenantHolder.getTenantId();
        String result = "1";
        List<ExchangeProductsDTO> proList = new ArrayList<ExchangeProductsDTO>();
        //退货
        String []backProNames = backProName.split(",");
        String []maxNumbers = maxNumber.split(",");
        String []backNumbers = backNumber.split(",");
        String []backTotalPrices = backTotalPrice.split(",");
        String []backTotalPVs = backTotalPV.split(",");
        String []backUnitPrices = backUnitPrice.split(",");
        String []backUnitPVs = backUnitPV.split(",");
        String []backProNos = backProNo.split(",");
        String []productionDates = null;
         String []qualityAssuranceDates = null;
        if(!StringUtils.isEmpty(productionDate))
        	productionDates = productionDate.split(",");
        if(!StringUtils.isEmpty(qualityAssuranceDate))
        	qualityAssuranceDates = qualityAssuranceDate.split(",");
       //换货
        String []exchangeProNames = exchangeProName.split(",");
        String []exchangeNumbers = exchangeNumber.split(",");
        String []exchangeTotalPrices = exchangeTotalPrice.split(",");
        String []exchangeTotalPVs = exchangeTotalPV.split(",");
        String []exchangeUnitPrices = exchangeUnitPrice.split(",");
        String []exchangeUnitPVs = exchangeUnitPV.split(",");
        String []exchangeProNos = exchangeProNo.split(",");
        //循环退货
        for(int i = 0;i<backProNames.length;i++ ){
        	ExchangeProductsDTO exchangeProduct = new ExchangeProductsDTO();
        	exchangeProduct.setProductName(backProNames[i]);
        	exchangeProduct.setMaxProductNum(maxNumbers[i]);
        	exchangeProduct.setProductNum(backNumbers[i]);
        	exchangeProduct.setTotalPrice(backTotalPrices[i]);
        	exchangeProduct.setTotalPv(backTotalPVs[i]);
        	exchangeProduct.setType("0");
        	exchangeProduct.setPrice(backUnitPrices[i]);//原货品的产品单价
        	exchangeProduct.setPv(backUnitPVs[i]);
        	if(productionDates!=null&&productionDates.length>0&&!com.mossle.core.util.StringUtils.isBlank(productionDates[i]))
        		exchangeProduct.setProductionDate(productionDates[i]);//生产日期
        	else {
        		exchangeProduct.setProductionDate("0000-00-00");//生产日期
			}
        	if(qualityAssuranceDates!=null&&qualityAssuranceDates.length>0&&!com.mossle.core.util.StringUtils.isBlank(qualityAssuranceDates[i]))
        		exchangeProduct.setQualityAssuranceDate(qualityAssuranceDates[i]);//质保期
        	else {
        		exchangeProduct.setQualityAssuranceDate("");//质保期
			}
        	exchangeProduct.setProductNo(backProNos[i]);
        	proList.add(exchangeProduct);
        }
        //循环换货
        for(int i=0;i<exchangeProNames.length;i++){
        	if(exchangeProNames[i].equals("")||exchangeProNames[i].equals("请选择")){
        		continue;
        	}
        	ExchangeProductsDTO exchangeProduct = new ExchangeProductsDTO();
        	exchangeProduct.setProductName(exchangeProNames[i]);
        	exchangeProduct.setProductNum(exchangeNumbers[i]);
        	exchangeProduct.setTotalPrice(exchangeTotalPrices[i]);
        	exchangeProduct.setTotalPv(exchangeTotalPVs[i]);
        	exchangeProduct.setType("1");
        	exchangeProduct.setPrice(exchangeUnitPrices[i]);//换货的产品单价
        	exchangeProduct.setPv(exchangeUnitPVs[i]);
        	exchangeProduct.setProductNo(exchangeProNos[i]);
        	proList.add(exchangeProduct);
        }
        try{
    		exchangeService.saveExchange(request, exchangeDTO, proList, userId, areaId, areaName, companyId, companyName, quality, businessKey, files, tenantId);
        }catch(Exception e){
        	e.printStackTrace();
        	result = e.getMessage();
        	
        }
        
        //return JSONObject.toJSONString(result);
        return "operation/process-operation-startProcessInstance";
    }
    
    
    /**
     * @param param
     * @return
     * 格式化金额，保留小数点后两位
     */
    /*public String formatPrice(String param){
    	return "";
    }*/
    
    
    /** 审批各节点获取外部申请单数据*/ 
    @GET
    @Path("getExchangeInfo")
   public List<ExchangeDTO> getExchangeById(@QueryParam("id")String id){

	   List<Exchange> returnInfo = exchangeManager.findBy("processInstanceId", id);
	   List <ExchangeDTO> returnDTOList = new ArrayList<ExchangeDTO>(); 
	   SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
	   for(Exchange getInfo : returnInfo){
		   ExchangeDTO exchangeDTO = new ExchangeDTO();
		   exchangeDTO.setWareHouse(getInfo.getWareHouse());
		   exchangeDTO.setEmpNo(getInfo.getEmpNo());
		   exchangeDTO.setUcode(getInfo.getUcode());
		   exchangeDTO.setName(getInfo.getName());
		   exchangeDTO.setTel(getInfo.getTel());
		   exchangeDTO.setExchangeDate(getInfo.getExchangeDate());
		   exchangeDTO.setOrderNumber(getInfo.getOrderNumber());
		   exchangeDTO.setExchangeReason(getInfo.getExchangeReason());
		   exchangeDTO.setPayType(getInfo.getPayType());
		   exchangeDTO.setProcessInstanceId(getInfo.getProcessInstanceId());
		   //exchangeDTO.setUserId(getInfo.getUserId());
		   exchangeDTO.setApplyCode(getInfo.getApplyCode());
		   exchangeDTO.setOrderTime(getInfo.getOrderTime());
		   exchangeDTO.setOldConsignee(getInfo.getOldConsignee());
		   exchangeDTO.setOldConsigneeTel(getInfo.getOldConsigneeTel());
		   exchangeDTO.setOldConsigneeAddress(getInfo.getOldConsigneeAddress());
		   exchangeDTO.setZipCode(getInfo.getZipCode());
		   exchangeDTO.setNewConsignee(getInfo.getNewConsignee());
		   exchangeDTO.setNewConsigneeTel(getInfo.getNewConsigneeTel());
		   exchangeDTO.setNewConsigneeAddress(getInfo.getNewConsigneeAddress());
		   exchangeDTO.setId(getInfo.getId());
		   exchangeDTO.setInputApplyCode(getInfo.getInputApplyCode());
		   returnDTOList.add(exchangeDTO);
		   
		   
	   }
		return returnDTOList;
	}
    /** 审批各节点获取外部产品申请单数据*/ 
    @GET
    @Path("getProductInfo")
    public List<ExchangeProducts> getProductById(@QueryParam("id")String id){
    	List<ExchangeProducts> productInfo = exchangeProductsManager.findBy("exchangeId", id);
    	List <ExchangeProducts> productDTOList = new ArrayList<ExchangeProducts>(); 
    	for(ExchangeProducts getInfo : productInfo){
    		ExchangeProducts product = new ExchangeProducts();
    		product.setId(getInfo.getId());
    		product.setExchangeId(id.toString());
    		product.setProductName(getInfo.getProductName());
    		product.setProductNum(getInfo.getProductNum());
    		product.setTotalPv(getInfo.getTotalPv());
    		product.setTotalPrice(getInfo.getTotalPrice());
    		product.setType(getInfo.getType());
    		
    		productDTOList.add(product);
    		
    		
    	}
    	return productDTOList;
    }
    /**
     * @param id
     * @return
     * 换货申请的子表数据
     */
    @GET
    @Path("getProductList")
    public Map<String,List<ExchangeProducts>> getProductListById(@QueryParam("id")String id){
    	Map<String,List<ExchangeProducts>> result = new HashMap<>();
    	List<ExchangeProducts> list = null;
    	List<ExchangeProducts> returnProductList = new ArrayList<>();;
    	List<ExchangeProducts> exchangeProductList = new ArrayList<>();
    	Exchange exchange = exchangeManager.findUniqueBy("processInstanceId", id);
    	if(exchange != null){
    		list = exchangeProductsManager.findBy("exchangeId",String.valueOf(exchange.getId()));
	    	for(ExchangeProducts pro : list){
	    		if("0".equals(pro.getType())){
	    			returnProductList.add(pro);
	    		}else{
	    			exchangeProductList.add(pro);
	    		}
	    	}
	    	result.put("0", returnProductList);
	    	result.put("1", exchangeProductList);
    	}
    	return result;
    }
    
    /**
     * 完成任务.
     */
    @RequestMapping("process-operationExchangeApproval-completeTask")
    @Log(desc = "审批流程", action = "confirmProcess", operationDesc = "流程中心-我的审批-待办审批-换货")
    public String completeTask(HttpServletRequest request,
            RedirectAttributes redirectAttributes,
            @RequestParam("processInstanceId") String processInstanceId, 
            @RequestParam(value = "quality", required = false) String quality, 
            @RequestParam("humanTaskId") String humanTaskId,
            String flag,@RequestParam(value = "files", required = false) MultipartFile[] files,@RequestParam(value = "iptdels", required = false) String iptdels
           ) throws Exception {
    	
    	try {
    		exchangeService.saveReExchange(request, redirectAttributes, processInstanceId, humanTaskId,files,iptdels,quality);
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
   @RequestMapping("form-detail")
    public String formDetail( @RequestParam("processInstanceId") String processInstanceId,
    		@RequestParam(value = "isPrint", required = false) boolean isPrint,Model model) throws Exception{
	    Record record = keyValueConnector.findByRef(processInstanceId);
	    String detailHtml = record.getDetailHtml();
	    //获取抄送人 ckx   add 2018/9/8
	    String taskCopyNames = "";
	    taskCopyNames = customWorkService.getTaskCopyNames(processInstanceId);
	    detailHtml = detailHtml.replace("{copyNames}", taskCopyNames);
	    detailHtml = detailHtml.replace("{exchangeTable}", "换货详情单");
	    detailHtml = detailHtml.replace("{qualityExchangeTable}", "质量换货详情单");
	    model.addAttribute("detailHtml", detailHtml);
	    if(detailHtml.contains("<h2>质量换货详情单</h2>")){
	    	model.addAttribute("resource",1);
	    }else{
	    	model.addAttribute("resource",0);
	    }
	    //审批记录
        List<HumanTaskDTO> logHumanTaskDtos = humanTaskConnector
                .findHumanTasksForPositionByProcessInstanceId(processInstanceId);
        //获得审核详情
        logHumanTaskDtos = operationService.settingAuditDuration(logHumanTaskDtos);
        model.addAttribute("logHumanTaskDtos", logHumanTaskDtos);
        model.addAttribute("isPrint", isPrint);
        //取附件
		model.addAttribute("picUrl", webAPI.getViewUrl());
		List<StoreInfo> exchangeStoreInfo = fileUploadAPI.getStore("OA/process", record.getPkId());
		model.addAttribute("StoreInfos", exchangeStoreInfo);
		operationService.copyMsgUpdate(processInstanceId);
    	return "operation/process/ExchangeFormDetail";
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
	public void setExchangeService(ExchangeService exchangeService) {
		this.exchangeService = exchangeService;
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
}
