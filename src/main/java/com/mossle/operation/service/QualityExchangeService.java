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
import com.mossle.operation.persistence.domain.Exchange;
import com.mossle.operation.persistence.domain.ExchangeDTO;
import com.mossle.operation.persistence.domain.ExchangeProducts;
import com.mossle.operation.persistence.domain.ExchangeProductsDTO;
import com.mossle.operation.persistence.domain.Freeze;
import com.mossle.operation.persistence.domain.Product;
import com.mossle.operation.persistence.domain.Return;
import com.mossle.operation.persistence.domain.ReturnDTO;
import com.mossle.operation.persistence.manager.ExchangeManager;
import com.mossle.operation.persistence.manager.ExchangeProductsManager;
import com.mossle.operation.persistence.manager.ProductManager;
import com.mossle.operation.persistence.manager.ReturnManager;
import com.mossle.operation.web.ProcessOperationReturnController;

@Service
@Transactional(readOnly = true)
public class QualityExchangeService {
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

    /**
     * 发起申请换货流程.
     */
    @Transactional(readOnly = false)
    public void saveExchange(HttpServletRequest request,ExchangeDTO exchangeDTO,List<ExchangeProductsDTO> productList, String userId,String areaId,String areaName, 
    		String companyId,String companyName,
            String businessKey,MultipartFile [] files,String tenantId) throws Exception{
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
        
        Exchange exchange = new Exchange();
        exchange.setProcessInstanceId(record.getRef());
        exchange.setApplyCode(exchangeDTO.getApplyCode());
        exchange.setName(exchangeDTO.getName());
        exchange.setTel(exchangeDTO.getTel());
        exchange.setUcode(exchangeDTO.getUcode());
        exchange.setAddress(exchangeDTO.getAddress());
        exchange.setAddTime(exchangeDTO.getAddTime());;
        exchange.setArea(areaName);
        exchange.setBusinessType(exchangeDTO.getBusinessType());
        exchange.setBusinessDetail(exchangeDTO.getBusinessDetail());
        exchange.setBusinessLevel(exchangeDTO.getBusinessLevel());
        exchange.setLevel(exchangeDTO.getLevel());
        
        //将所属体系翻译成汉字跟表单内容一起存起来
        List<DictInfo> dictList=dictConnectorImpl.findDictInfoListByType("OwnedSystem", "1");
        for (DictInfo dictInfo : dictList) {
        	if (dictInfo.getValue().equals(exchangeDTO.getSystem()) ){
        		exchange.setSystem(dictInfo.getName());
        		break;
        	}
        }
        exchange.setWelfare(exchangeDTO.getWelfare());
        exchange.setVarFather(exchangeDTO.getVarFather());
        exchange.setVarRe(exchangeDTO.getVarRe());
        exchange.setExchangeReason(exchangeDTO.getExchangeReason());
 
        exchangeManager.save(exchange);
        
        saveProduct(productList, exchange.getId().toString());
        
        fileUploadAPI.uploadFile(files, tenantId, Long.toString(exchange.getId()), "OA/process");
        
        this.saveFormHtml(exchange, productList, businessKey);
        
        String sql = "from BusinessDetailEntity where  bpmProcessId=? ";
        BusinessDetailEntity businessDetailEntity = businessDetailManager.findUnique(sql, bpmProcessId);
        //原jdbc更新record操作更改如下，更新的属性保持一致  TODO sjx 18.11.23
        RecordInfo recordInfo = recordManager.findUniqueBy("businessKey", businessKey);
        recordInfo.setBusinessTypeId(businessDetailEntity.getTypeId().toString());
        recordInfo.setBusinessTypeName(businessDetailEntity.getBusinessType());
        recordInfo.setBusinessDetailId(businessDetailEntity.getId().toString());
        recordInfo.setBusinessDetailName(businessDetailEntity.getBusiDetail());
        recordInfo.setAreaId(areaId);
        recordInfo.setAreaName(areaName);
        recordInfo.setCompanyId(companyId);
        recordInfo.setCompanyName(companyName);
    	recordManager.save(recordInfo);
    	//处理受理单编号
    	operationService.deleteApplyCode(exchangeDTO.getApplyCode());
    }
  

    public void saveFormHtml(Exchange exchange,List<ExchangeProductsDTO> dataList,String bussinessKey){
    	List<ExchangeProductsDTO> productList = new ArrayList<ExchangeProductsDTO>();
    	for (ExchangeProductsDTO exchangeProductsDTO : dataList) {
    		if(!"0".equals(exchangeProductsDTO.getProductNum())){
    			productList.add(exchangeProductsDTO);
    		}
		}
    	
    
    	String detailHtml="<table  style=\"line-height:34px;\"  class=\"centerdiv\" cellpadding=\"0\" cellspacing=\"0\" >"
   			 +"<tr>"
   			 +"<td colspan=\"8\" align=\"center\"><h2>{qualityExchangeTable}</h2></td>"
   	        +"</tr>"
   	        +"<tr>"
   	            +"<td class=\"f_td\" colspan=\"4\" style=\"padding-right:20px;text-align:right;\"><b>"
   	               	 +"&nbsp;&nbsp;受理单编号："
   	              +"</b></td>"
	               	
	               	 +"<td class=\"f_r_td\" colspan=\"4\">"
    	               	 +exchange.getApplyCode()
	               	+"</td>"
	        +"</tr>"

		       +"<tr>"
		            +"<td class=\"f_td\" style=\"text-align:center\" colspan=\"1\"> 专卖店编号/手机号</td>"
		            +"<td class=\"f_td\" style=\"text-align:center\" colspan=\"1\">"+ exchange.getUcode()+"</td>"
		            
		           	+"<td class=\"f_td\" style=\"text-align:center\" colspan=\"1\">姓名</td>"
		           	+"<td class=\"f_td\" style=\"text-align:center\" colspan=\"1\">"+exchange.getName()+"</td>"
		           	
		           	+"<td class=\"f_td\" style=\"text-align:center\" colspan=\"1\">福利级别</td>"
		           	+"<td class=\"f_td\" style=\"text-align:center\" colspan=\"1\">"+exchange.getWelfare()+"</td>"
		           	
		           	+"<td class=\"f_td\" style=\"text-align:center\" colspan=\"1\">级别</td>"
		           	+"<td class=\"f_td\" style=\"text-align:center\" colspan=\"1\">"+exchange.getLevel()+"</td>"
		          +"</tr>"
		           	
		  		  +"<tr>"
		           	+"<td class=\"f_td\" style=\"text-align:center\" colspan=\"1\">所属体系</td>"
		           	+"<td class=\"f_td\" style=\"text-align:center\" colspan=\"1\">"+exchange.getSystem()+"</td>"
		           	
		           	+"<td class=\"f_td\" style=\"text-align:center\" colspan=\"1\">销售人</td>"
		           	+"<td class=\"f_td\" style=\"text-align:center\" colspan=\"1\">"+exchange.getVarFather()+"</td>"
		           	
		           	+"<td class=\"f_td\" style=\"text-align:center\" colspan=\"1\">服务人</td>"
		           	+"<td class=\"f_td\" style=\"text-align:center\" colspan=\"1\">"+exchange.getVarRe()+"</td>"
		           	
		           	+"<td class=\"f_td\" style=\"text-align:center\" colspan=\"1\">注册时间</td>"
		           	+"<td class=\"f_td\" style=\"text-align:center\" colspan=\"1\">"+exchange.getAddTime()+"</td>"
		         +"</tr>"
		           	
 				 +"<tr>"	
 				   +"<td class=\"f_td\" style=\"text-align:center\">抄送</td>"
 				   +"<td class=\"f_r_td\" style=\"text-align:left\" colspan=\"7\">"+"{copyNames}"+
 				   "</td>"
		        +"</tr>" 
 				   
 				+"<tr>"	
		           	+"<td class=\"f_td\" style=\"text-align:center\" colspan=\"2\">申请业务类型</td>"
		           	+"<td class=\"f_td\" style=\"text-align:center\" colspan=\"2\">"+exchange.getBusinessType()+"</td>"
		           	
		           	+"<td class=\"f_td\" style=\"text-align:center\" colspan=\"2\">业务细分</td>"
		           	+"<td class=\"f_td\" style=\"text-align:center\" colspan=\"2\">"+exchange.getBusinessDetail()+"</td>"
		         +"</tr>" 
		           	
				+"<tr>"
		           	+"<td class=\"f_td\" style=\"text-align:center\" colspan=\"2\">联系电话</td>"
		           	+"<td class=\"f_td\" style=\"text-align:center\" colspan=\"2\">"+exchange.getTel()+"</td>"
		           	
		           	+"<td class=\"f_td\" style=\"text-align:center\" colspan=\"2\">联系地址</td>"
		           	+"<td class=\"f_td\" style=\"text-align:center\" colspan=\"2\">"+exchange.getAddress()+"</td>"
		        +"</tr>"    	
		        
				+"<tr>"
					+"<td class=\"f_td\" style=\"text-align:center\" colspan=\"2\">业务级别</td>"
					+"<td class=\"f_td\" style=\"text-align:center\" colspan=\"2\">"+exchange.getBusinessLevel()+"</td>"
					
					+"<td class=\"f_td\" style=\"text-align:center\" colspan=\"2\">所属大区</td>"
					+"<td class=\"f_td\" style=\"text-align:center\" colspan=\"2\">"+exchange.getArea()+"</td>"
				+"</tr>" 
		   
       
	    	  +"<tr>"
//	    	  	+"<td class=\"f_td\" style=\"text-align:center;width:20px;\">序号</td>"
	    	  	+"<td class=\"f_td\" style=\"text-align:center\" colspan=\"2\">质量产品名称</td>"
		        +"<td class=\"f_r_td\" style=\"text-align:center\" colspan=\"2\">数量</td>"
		        +"<td class=\"f_r_td\" style=\"text-align:center\" colspan=\"4\">生产日期</td>"
		     +"</tr>";
   		
		  
    			if(productList!=null&&productList.size()>0){

    				for(int i=0;i<productList.size();i++){
    					detailHtml+="<tr>"
//    							   +"<td class=\"f_td\" style=\"text-align:center;\" colspan=\"6\">"+(i+1)+"</td>"
    							   +"<td class=\"f_td\" style=\"text-align:center;\" colspan=\"2\">"+productList.get(i).getProductName()+"</td>"
    							   +"<td class=\"f_td\" style=\"text-align:center;\"  colspan=\"2\">"+productList.get(i).getProductNum()+"</td>"

    							   +"<td class=\"f_td\" style=\"text-align:center;\"  colspan=\"4\">"+productList.get(i).getProductionDate()+"</td>"
    							   +"</tr>";

    				}

    			}
   	detailHtml+=""
   			       	  
				+"<tr>"
					+"<td class=\"f_td\" style=\"text-align:center\" colspan=\"8\">产品问题</td>"
				+"</tr>" 
					
				+"<tr>"
					+"<td class=\"f_td\" style=\"text-align:center\" colspan=\"8\">"+exchange.getExchangeReason()+"</td>"
				+"</tr>" 
					
           +"</table>";
   	
   	
   	RecordInfo recordInfo=recordManager.findUniqueBy("businessKey", bussinessKey);
	if(recordInfo != null){
		recordInfo.setPkId(exchange.getId().toString());
		recordInfo.setDetailHtml(detailHtml);
	}
   	recordManager.save(recordInfo);
    }
 
    // 审批
    @Transactional(readOnly = false)
    public void saveReExchange(HttpServletRequest request,RedirectAttributes redirectAttributes,
            String processInstanceId, String humanTaskId,MultipartFile[] files, String iptdels,String quality) throws Exception, IOException {
    	
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
        if (f.equals("2")) {
        	processParameters.put("leaderComment", "驳回");
        	formParameter.setAction("驳回");
        }
        if (f.equals("3")) {
        	processParameters.put("leaderComment", "调整申请");
        	formParameter.setAction("重新申请");
        	//调整申请内容，重写入表
        	  
            Exchange exchange = new Exchange();

            exchange.setAddress(m.getFirst("address"));
            exchange.setAddTime(m.getFirst("addTime"));;
            exchange.setArea(m.getFirst("areaName"));
            exchange.setBusinessType(m.getFirst("busType"));
            exchange.setBusinessDetail(m.getFirst("busDetails"));
            exchange.setBusinessLevel(m.getFirst("busLevel"));
            exchange.setLevel(m.getFirst("level"));
            
            //将所属体系翻译成汉字跟表单内容一起存起来
            List<DictInfo> dictList=dictConnectorImpl.findDictInfoListByType("OwnedSystem", "1");
            for (DictInfo dictInfo : dictList) {
            	if (dictInfo.getValue().equals(m.getFirst("system")) ){
            		exchange.setSystem(dictInfo.getName());
            		break;
            	}
            }
      
            exchange.setWelfare(m.getFirst("welfare"));
            
            exchange.setVarFather(m.getFirst("varFather"));
            exchange.setVarRe(m.getFirst("varRe"));
     
        	exchange.setUcode(m.getFirst("ucode"));
        	exchange.setName(m.getFirst("name"));
        	exchange.setTel(m.getFirst("tel"));
        
        	exchange.setApplyCode(m.getFirst("applyCode"));
        	exchange.setExchangeReason(m.getFirst("exchangeReason"));
        	exchange.setProcessInstanceId(processInstanceId);
        

        	exchange.setId(Long.parseLong(m.getFirst("exchangeId")));
        	
        	if (iptdels != null && !iptdels.equals("")) {
        		fileUploadAPI.uploadFileDel(iptdels,m.getFirst("exchangeId"));
	        }

	        fileUploadAPI.uploadFile(files, tenantId,m.getFirst("exchangeId"), "OA/process");
        	exchangeManager.save(exchange);
        	//处理 record 表中的提交次数
        	int submitTimes = record.getSubmitTimes();
			submitTimes++;
			RecordInfo recordInfo = recordManager.findUniqueBy("businessKey", formParameter.getBusinessKey());
			recordInfo.setSubmitTimes(submitTimes);
			recordManager.save(recordInfo);
	        
			int total =Integer.parseInt(m.getFirst("hidTotal")) ;
			String hidnum = m.getFirst("hidNum");
        	// 删除子表
        	jdbcTemplate.update("DELETE FROM ENTITY_EXCHANGE_PRODUCT WHERE exchange_id=?", m.getFirst("exchangeId"));
        	
        	
        	//产品数据存表
        	
        	 List<ExchangeProductsDTO> productList = new ArrayList<ExchangeProductsDTO>();
        	
        	 int hidNum =Integer.parseInt(m.getFirst("hidNum")) ;
        	 
            for(int i = 1;i<=hidNum;i++){
            	
            	if(m.getFirst("qualityName"+i)!=null && !m.getFirst("qualityName"+i).equals("")){
	            	ExchangeProducts pro = new ExchangeProducts();
	            	ExchangeProductsDTO epdtoDto = new ExchangeProductsDTO();
	            	//pro.setId(Long.parseLong(m.getFirst("exchangeId")));
	            	pro.setExchangeId(m.getFirst("exchangeId"));
	            	pro.setProductName(m.getFirst("qualityName"+i));//质量产品名称
	            	pro.setProductNum(m.getFirst("qualityNum"+i));//产品数量
	            	pro.setProductionDate(m.getFirst("manuTime"+i));//生产日期
	            	
	            	epdtoDto.setExchangeId(m.getFirst("exchangeId"));
	            	epdtoDto.setProductName(m.getFirst("qualityName"+i));//质量产品名称
	            	epdtoDto.setProductNum(m.getFirst("qualityNum"+i));//产品数量
	            	epdtoDto.setProductionDate(m.getFirst("manuTime"+i));//生产日期
	            	
	            	exchangeProductsManager.save(pro);
	            	
	            	productList.add(epdtoDto);
            	}
             }
            
            this.saveFormHtml(exchange, productList, formParameter.getBusinessKey());
        	
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
}
