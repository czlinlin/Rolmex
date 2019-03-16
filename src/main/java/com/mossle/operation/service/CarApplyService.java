package com.mossle.operation.service;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Null;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.stax2.ri.typed.ValueDecoderFactory.DecimalDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import ch.qos.logback.core.joran.conditional.IfAction;

import com.mossle.api.keyvalue.FormParameter;
import com.mossle.api.keyvalue.KeyValueConnector;
import com.mossle.api.keyvalue.Record;
import com.mossle.api.process.ProcessConnector;
import com.mossle.api.process.ProcessDTO;
import com.mossle.api.store.StoreConnector;
import com.mossle.api.tenant.TenantHolder;
import com.mossle.base.persistence.domain.BusinessDetailEntity;
import com.mossle.base.persistence.manager.BusinessDetailManager;
import com.mossle.common.utils.FileUploadAPI;
import com.mossle.common.utils.WebAPI;
import com.mossle.core.MultipartHandler;
import com.mossle.core.auth.CurrentUserHolder;
import com.mossle.dict.persistence.domain.DictInfo;
import com.mossle.dict.support.DictConnectorImpl;
import com.mossle.internal.store.persistence.domain.StoreInfo;
import com.mossle.keyvalue.persistence.domain.RecordInfo;
import com.mossle.keyvalue.persistence.manager.RecordManager;
import com.mossle.msg.persistence.manager.MsgInfoManager;
import com.mossle.operation.persistence.domain.CarApply;
import com.mossle.operation.persistence.domain.CarApplyDTO;
import com.mossle.operation.persistence.domain.Exchange;
import com.mossle.operation.persistence.domain.ExchangeDTO;
import com.mossle.operation.persistence.domain.ExchangeProducts;
import com.mossle.operation.persistence.domain.ExchangeProductsDTO;
import com.mossle.operation.persistence.domain.Freeze;
import com.mossle.operation.persistence.domain.Product;
import com.mossle.operation.persistence.domain.Return;
import com.mossle.operation.persistence.domain.ReturnDTO;
import com.mossle.operation.persistence.manager.CarApplyManager;
import com.mossle.operation.persistence.manager.ExchangeManager;
import com.mossle.operation.persistence.manager.ExchangeProductsManager;
import com.mossle.operation.persistence.manager.ProductManager;
import com.mossle.operation.persistence.manager.ReturnManager;
import com.mossle.operation.web.ProcessOperationReturnController;

@Service
@Transactional(readOnly = true)
public class CarApplyService {
    private static Logger logger = LoggerFactory.getLogger(ReturnService.class);
    
    public static final String OPERATION_BUSINESS_KEY = "businessKey";
    public static final String OPERATION_TASK_ID = "taskId";
    public static final String OPERATION_BPM_PROCESS_ID = "bpmProcessId";
    public static final int STATUS_DRAFT_PROCESS = 0;
    public static final int STATUS_DRAFT_TASK = 1;
    public static final int STATUS_RUNNING = 2;
    private KeyValueConnector keyValueConnector;
    private ProcessConnector processConnector;

    private OperationService operationService;
    private ExchangeManager exchangeManager;
    private ExchangeProductsManager exchangeProductsManager;
    private BusinessDetailManager businessDetailManager;
    @Resource
    private CurrentUserHolder currentUserHolder;
    @Resource
    private ProcessOperationReturnController processOperationReturnController;
    @Resource
    private MultipartResolver multipartResolver;
    @Resource
    private TenantHolder tenantHolder;
    @Resource
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private WebAPI webAPI;
    private FileUploadAPI fileUploadAPI;
    @Autowired
    private StoreConnector storeConnector;
    @Resource
    private ProductManager productManager;
    @Resource
    private MsgInfoManager msgInfoManager;
    @Resource
    private RecordManager recordManager;
    
    private DictConnectorImpl dictConnectorImpl;
    private CarApplyManager carApplyManager;

    /**
     * 发起流程.
     */
    @Transactional(readOnly = false)
    public void saveCarApply(HttpServletRequest request,CarApplyDTO carApplyDTO, String userId, 
    		String companyId,String companyName,
            String businessKey,String tenantId) throws Exception{
    	Map<String, Object> processParameters = new HashMap<String, Object>();
    	FormParameter formParameter = this.doSaveRecord(request);
		String bpmProcessId = formParameter.getBpmProcessId();
        ProcessDTO processDto = processConnector.findProcess(bpmProcessId);
        String processDefinitionId = processDto.getProcessDefinitionId();
        Record record = keyValueConnector.findByCode(formParameter.getBusinessKey());
        businessKey = formParameter.getBusinessKey();
        
        //得到岗位，存储add by lilei at 2018-09-12
        processParameters.put("positionId", request.getParameter("iptStartPositionId"));
        this.operationService.startProcessInstance(userId, businessKey,processDefinitionId, processParameters, record);
        
        CarApply carApply = new CarApply();
        carApply.setApplyCode(carApplyDTO.getApplyCode());//受理单编号
        carApply.setCarUser(carApplyDTO.getCarUser());//用车人姓名
        carApply.setUcode(currentUserHolder.getUserId());//发起人id
        carApply.setContent(carApplyDTO.getContent());//用车事由
        carApply.setDepartmentCode(0L);//部门编码
        carApply.setDepartmentName(carApplyDTO.getDepartmentName());//部门名称
        carApply.setBusinessType(carApplyDTO.getBusinessType());
        carApply.setBusinessDetail(carApplyDTO.getBusinessDetail());
        carApply.setDestination(carApplyDTO.getDestination());//目的地
        carApply.setBorrowCarTime(request.getParameter("startDate"));//借车时间
        carApply.setReturnCarTime(request.getParameter("endDate"));//还车时间
        carApply.setTotalTime(carApplyDTO.getTotalTime());//共计时长
        carApply.setProcessInstanceID(record.getRef());
        
        carApplyManager.save(carApply);
        
     // 处理kv_record 表中的业务主键和html字段
        this.saveFormHtml(carApply,businessKey);
     
        String sql = "from BusinessDetailEntity where  bpmProcessId=? ";
        BusinessDetailEntity businessDetailEntity = businessDetailManager.findUnique(sql, bpmProcessId);
        //原jdbc更新record操作更改如下，更新的属性保持一致  TODO sjx 18.11.23
        RecordInfo recordInfo = recordManager.findUniqueBy("businessKey", businessKey);
        recordInfo.setBusinessTypeId(businessDetailEntity.getTypeId().toString());
        recordInfo.setBusinessTypeName(businessDetailEntity.getBusinessType());
        recordInfo.setBusinessDetailId(businessDetailEntity.getId().toString());
        recordInfo.setBusinessDetailName(businessDetailEntity.getBusiDetail());
        
        recordInfo.setCompanyId(companyId);
        recordInfo.setCompanyName(companyName);
    	recordManager.save(recordInfo);
    	//处理受理单编号
    	operationService.deleteApplyCode(carApplyDTO.getApplyCode());
    }
  

    public void saveFormHtml(CarApply carApply,String bussinessKey){
    
    	String plateNumber ="";
    	String driver ="";
    	String borrowCarMileage ="";
    	String returnCarMileage ="";
    	String mileage ="";
    	String oilMoney ="";
    	String remainOil ="";
    	
    	if(carApply.getPlateNumber()!=null){
    		plateNumber = carApply.getPlateNumber();
    	}
    	if(carApply.getDriver()!=null){
    		driver = carApply.getDriver();
    	}
    	if(carApply.getBorrowCarMileage()!=null){
    		borrowCarMileage = carApply.getBorrowCarMileage();
    	}
    	if(carApply.getReturnCarMileage()!=null){
    		returnCarMileage = carApply.getReturnCarMileage();
    	}
    	if(carApply.getMileage()!=null){
    		mileage = carApply.getMileage();
    	}
    	if(carApply.getOilMoney()!=null){
    		oilMoney = carApply.getOilMoney();
    	}
    	if(carApply.getRemainOil()!=null){
    		remainOil = carApply.getRemainOil();
    	}
    	
    	
    	
    	String detailHtml="<table  style=\"line-height:34px;\"  class=\"centerdiv\" cellpadding=\"0\" cellspacing=\"0\" >"
   			 +"<tr>"
   			 +"<td colspan=\"4\"><h2>用车申请</h2></td>"
   	        +"</tr>"
   	        +"<tr>"
   	            +"<td class=\"f_td\" colspan=\"2\" style=\"padding-right:20px;text-align:right;\"><b>"
   	               	 +"&nbsp;&nbsp;受理单编号："
   	              +"</b></td>"
	               	
	               	 +"<td class=\"f_r_td\" colspan=\"2\">"
    	               	 +carApply.getApplyCode()
	               	+"</td>"
	        +"</tr>"
	       
				        
			+"<tr>"
				+"<td class=\"f_td\">车牌号：</td>"
	            +"<td class=\"f_r_td\">"+plateNumber+"</td>"
				
	            
	           	+"<td class=\"f_td\">驾驶人：</td>"
	           	+"<td class=\"f_r_td\">"+driver+"</td>"
	         +"</tr>"
			 
 
		       +"<tr>"
		            +"<td class=\"f_td\">用车人姓名：</td>"
		            +"<td class=\"f_r_td\">"+ carApply.getCarUser()+"</td>"
		            
		           	+"<td class=\"f_td\">部门：</td>"
		           	+"<td class=\"f_r_td\">"+carApply.getDepartmentName()+"</td>"
		         +"</tr>"  
		           	
		         +"<tr>"
		           	+"<td class=\"f_td\">目的地：</td>"
		           	+"<td class=\"f_r_td\" colspan=\"3\">"+carApply.getDestination()+"</td>"
		          +"</tr>"
		          
 				+"<tr>"	
		           	+"<td class=\"f_td\">申请业务类型：</td>"
		           	+"<td class=\"f_r_td\">"+carApply.getBusinessType()+"</td>"
		           	
		           	+"<td class=\"f_td\">业务细分：</td>"
		           	+"<td class=\"f_r_td\">"+carApply.getBusinessDetail()+"</td>"
		         +"</tr>" 
		           	
				+"<tr>"
		           	+"<td class=\"f_td\">用车事由：</td>"
		           	+"<td class=\"f_r_td\" colspan=\"3\">"
		           	+ "<pre style=\"white-space: pre-wrap;word-wrap: break-word;\">"+carApply.getContent()+"</pre></td>"
		       +"</tr>" 
		           	
		       +"<tr>"
		           	+"<td class=\"f_td\">时间：</td>"
		           	+"<td class=\"f_r_td\" colspan=\"2\">"
		           	+carApply.getBorrowCarTime()+"至"+carApply.getReturnCarTime()
		           	+"</td>"
		           	+"<td class=\"f_td\" colspan=\"2\">共"+carApply.getTotalTime()+"小时</td>"
		        +"</tr>"   
		           	
						        
				 +"<tr>"
					+"<td class=\"f_td\">借车里程：</td>"
					+"<td class=\"f_r_td\">"+borrowCarMileage+"</td>"
					+"<td class=\"f_td\">还车里程：</td>"
					+"<td class=\"f_r_td\">"+returnCarMileage+"</td>"
				+"</tr>"
				+"<tr>"
					+"<td class=\"f_td\">行驶里程：</td>"
					+"<td class=\"f_r_td\" colspan=\"3\">"+mileage+"</td>"
				+"</tr>"
				+"<tr>"
					+"<td class=\"f_td\">加油金额：</td>"
					+"<td class=\"f_r_td\">"+oilMoney+"</td>"
					+"<td class=\"f_td\">剩余油量：</td>"
					+"<td class=\"f_r_td\">"+remainOil+"</td>"
				+"</tr>"
           +"</table>";
   	
		   	RecordInfo recordInfo=recordManager.findUniqueBy("businessKey", bussinessKey);
			if(recordInfo != null){
				recordInfo.setPkId(carApply.getId().toString());
				recordInfo.setDetailHtml(detailHtml);
			}
		   	recordManager.save(recordInfo);
    }
 
    // 审批
    @Transactional(readOnly = false)
    public void saveCarApply(HttpServletRequest request,RedirectAttributes redirectAttributes,
            String processInstanceId, String humanTaskId) throws Exception, IOException {
    	
    	String userId = currentUserHolder.getUserId();
    	String tenantId = tenantHolder.getTenantId();
    	Map<String, Object> processParameters = new HashMap<String, Object>();
    	
    	MultipartHandler multipartHandler = new MultipartHandler(multipartResolver);
        FormParameter formParameter = null;
        multipartHandler.handle(request);
        formParameter = this.buildFormParameter(multipartHandler);
        
        MultiValueMap<String,String> m = formParameter.getMultiValueMap();
        String f = m.getFirst("flag");
        
        if (f.equals("3")) {   // 重新调整
        	formParameter = this.doSaveRecord(request);
        } 
		Record record = keyValueConnector.findByRef(processInstanceId);
		formParameter.setBusinessKey(record.getBusinessKey());
		
        if (f.equals("0")) {
        	processParameters.put("leaderComment", "不同意");
        	formParameter.setAction("不同意");
		}
        if (f.equals("1")) {
        	processParameters.put("leaderComment", "同意");
        	formParameter.setAction("同意");
        }
        
        //第三步骤  行政部司机填写车牌号
        if (f.equals("5")) {
        	processParameters.put("leaderComment", "同意");
        	formParameter.setAction("同意");
        	
        	if ( m.getFirst("plateNumber")!=null&&m.getFirst("plateNumber").length()>0){
        		CarApply carApply=carApplyManager.findUniqueBy("processInstanceID", m.getFirst("processInstanceId"));
        			carApply.setPlateNumber(m.getFirst("plateNumber"));
	        		carApply.setDriver(m.getFirst("driver"));
	        		carApplyManager.save(carApply);
	        		
	        		 // 处理kv_record 表中的业务主键和html字段
	                this.saveFormHtml(carApply,record.getBusinessKey());
	        	}
        	}	
        
        //第六步  行政部司机验收
        if (f.equals("6")) {
        	processParameters.put("leaderComment", "同意");
        	formParameter.setAction("同意");
        	
        	if ( m.getFirst("borrowCarMileage")!=null&&m.getFirst("borrowCarMileage").length()>0){
        		CarApply carApply=carApplyManager.findUniqueBy("processInstanceID", m.getFirst("processInstanceId"));
        			carApply.setBorrowCarMileage(m.getFirst("borrowCarMileage"));
	        		carApply.setReturnCarMileage(m.getFirst("returnCarMileage"));
	        		carApply.setMileage(m.getFirst("mileage"));
	        		carApply.setOilMoney(m.getFirst("oilMoney"));
	        		carApply.setRemainOil(m.getFirst("remainOil"));
	        		carApplyManager.save(carApply);
	        		
	        		 // 处理kv_record 表中的业务主键和html字段
	                this.saveFormHtml(carApply,record.getBusinessKey());
        		}
        }
        
        
        if (f.equals("2")) {
        	processParameters.put("leaderComment", "驳回");
        	formParameter.setAction("驳回");
        	
        	CarApply carApply=carApplyManager.findUniqueBy("processInstanceID", m.getFirst("processInstanceId"));
			if((m.getFirst("plateNumber").equals(""))&&(carApply.getPlateNumber()!=null&&carApply.getPlateNumber().length()>0)){
				carApply.setPlateNumber(m.getFirst("plateNumber"));
				
				 // 处理kv_record 表中的业务主键和html字段
                this.saveFormHtml(carApply,record.getBusinessKey());
				
			}
			if((m.getFirst("driver").equals(""))&&(carApply.getDriver()!=null&&carApply.getDriver().length()>0)){
				carApply.setDriver(m.getFirst("driver"));
				
				 // 处理kv_record 表中的业务主键和html字段
                this.saveFormHtml(carApply,record.getBusinessKey());
			}
    		
    		carApplyManager.save(carApply);
        }
        if (f.equals("3")) {
        	processParameters.put("leaderComment", "调整申请");
        	formParameter.setAction("重新申请");
        	//调整申请内容，重写入表
        	  
        	CarApply carApply =	carApplyManager.findUniqueBy("processInstanceID", m.getFirst("processInstanceId"));
        	
        	
        	//carApply.setLevel(m.getFirst("level"));
             carApply.setCarUser(m.getFirst("carUser"));//用车人姓名
             carApply.setUcode(userId);//发起人id
             carApply.setContent(m.getFirst("content"));//用车事由
             carApply.setDepartmentCode(0L);//部门编码
             carApply.setDepartmentName(m.getFirst("departmentName"));//部门名称
             carApply.setBusinessType(m.getFirst("businessType"));
             carApply.setBusinessDetail(m.getFirst("businessDetail"));
             carApply.setDestination(m.getFirst("destination"));//目的地
             carApply.setBorrowCarTime(m.getFirst("startDate"));//借车时间
             carApply.setReturnCarTime(m.getFirst("endDate"));//还车时间
             carApply.setTotalTime(m.getFirst("totalTime"));//共计时长
             
             carApply.setPlateNumber("");
     		 carApply.setDriver("");
     		 carApply.setBorrowCarMileage("");
    		 carApply.setReturnCarMileage("");
    		 carApply.setMileage("");
    		 carApply.setOilMoney("");
    		 carApply.setRemainOil("");
             
             carApplyManager.save(carApply);
          
      
        	//处理 record 表中的提交次数
        	int submitTimes = record.getSubmitTimes();
			submitTimes++;
			RecordInfo recordInfo = recordManager.findUniqueBy("businessKey", formParameter.getBusinessKey());
			recordInfo.setSubmitTimes(submitTimes);
			recordManager.save(recordInfo);
	   
            this.saveFormHtml(carApply, formParameter.getBusinessKey());
        	
        }
        if (f.equals("4")) {
        	processParameters.put("leaderComment", "撤销申请");
        	formParameter.setAction("撤销申请");
        }
        
        record = keyValueConnector.findByRef(processInstanceId);
        
        this.operationService.completeTask(humanTaskId, userId,
                formParameter, processParameters, record,
                    processInstanceId);
        //审批后将此任务对应的消息置为已读 2018.08.27 sjx
        String updateMsg = "update MsgInfo set status=1 where data=?";
        msgInfoManager.batchUpdate(updateMsg, humanTaskId);

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
    
    /**
     * 通过multipart请求构建formParameter.
     */
    public FormParameter buildFormParameter(MultipartHandler multipartHandler) {
        FormParameter formParameter = new FormParameter();
        formParameter.setMultiValueMap(multipartHandler.getMultiValueMap());
        formParameter.setMultiFileMap(multipartHandler.getMultiFileMap());
        
        formParameter.setBusinessKey(multipartHandler.getMultiValueMap().getFirst("businessKey"));
       	
        	String busDetailId = multipartHandler.getMultiValueMap().getFirst("businessDetailId");   // 业务细分ID
        	if (busDetailId!=null) {
	        	BusinessDetailEntity businessDetailEntity = businessDetailManager.findUniqueBy("id", Long.parseLong(busDetailId));
	        	if (businessDetailEntity != null) {
	        		String bpmProcessId = businessDetailEntity.getBpmProcessId();
	        		formParameter.setBpmProcessId(bpmProcessId);
	        	}
        	} 
  
        formParameter.setHumanTaskId(multipartHandler.getMultiValueMap().getFirst("humanTaskId"));
        formParameter.setComment(multipartHandler.getMultiValueMap().getFirst("comment"));
        
        System.out.println("====== businessKey:" + formParameter.getBusinessKey() + ";busDetails:" + busDetailId + ";bpmProcessId:" + formParameter.getBpmProcessId());
        
        return formParameter;
    }
    
    
    /**
     * 发起申请换货流程.保存产品数据
     */
    @Transactional(readOnly=false)
    public void saveProduct(List<ExchangeProductsDTO> product, String exchangeId) throws Exception {
    	
    	//产品数据存表
    	
        for(int i = 0;i<product.size();i++){
        	
        	if(StringUtils.isNotBlank(product.get(i).getProductNum()) && !"0".equals(product.get(i).getProductNum())){
        		ExchangeProducts pro = new ExchangeProducts();
            	pro.setId(product.get(i).getId());
            	pro.setExchangeId(exchangeId);
            	pro.setProductName(product.get(i).getProductName());//质量产品名称
            	pro.setProductNum(product.get(i).getProductNum());//产品数量
            	pro.setType(product.get(i).getType());
            	pro.setProductionDate(product.get(i).getProductionDate());//生产日期
            	
                exchangeProductsManager.save(pro);
        	}
        	
        }
    }

 
    
    @Resource
    public void setOperationService(OperationService operationService) {
        this.operationService = operationService;
    }
    
    @Resource
    public void setKeyValueConnector(KeyValueConnector keyValueConnector) {
        this.keyValueConnector = keyValueConnector;
    }
    
    @Resource
    public void setProcessConnector(ProcessConnector processConnector) {
        this.processConnector = processConnector;
    }
    
    @Resource
    public void setExchangeManager(ExchangeManager exchangeManager) {
        this.exchangeManager = exchangeManager;
    }
    @Resource
    public void setExchangeProductsManager(ExchangeProductsManager exchangeProductsManager) {
    	this.exchangeProductsManager = exchangeProductsManager;
    }
    @Resource
    public void setBusinessDetailManager(BusinessDetailManager businessDetailManager) {
        this.businessDetailManager = businessDetailManager;
    }
    @Resource
    public void setFileUploadAPI(FileUploadAPI fileUploadAPI) {
    	this.fileUploadAPI = fileUploadAPI;
    }
    
    @Resource
    public void setDictConnectorImpl(DictConnectorImpl dictConnectorImpl){
    	this.dictConnectorImpl=dictConnectorImpl;
    }

	@Resource
	public void setCarApplyManager(CarApplyManager carApplyManager) {
		this.carApplyManager = carApplyManager;
	}
    
    
}
