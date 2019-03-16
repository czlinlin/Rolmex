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
public class ExchangeService {
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

    /**
     * 发起申请换货流程.
     */
    @Transactional(readOnly = false)
    public void saveExchange(HttpServletRequest request,ExchangeDTO exchangeDTO,List<ExchangeProductsDTO> productList, String userId,String areaId,String areaName, 
    		String companyId,String companyName,String quality,
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
        exchange.setWareHouse(exchangeDTO.getWareHouse());
        exchange.setExchangeDate(exchangeDTO.getExchangeDate());
        exchange.setEmpNo(exchangeDTO.getEmpNo());
        exchange.setApplyCode(exchangeDTO.getApplyCode());
        exchange.setName(exchangeDTO.getName());
        exchange.setTel(exchangeDTO.getTel());
        exchange.setUcode(exchangeDTO.getUcode());
        exchange.setOrderNumber(exchangeDTO.getOrderNumber());
        exchange.setOrderTime(exchangeDTO.getOrderTime());
        exchange.setOldConsignee(exchangeDTO.getOldConsignee());
        exchange.setOldConsigneeTel(exchangeDTO.getOldConsigneeTel());
        exchange.setZipCode(exchangeDTO.getZipCode());
        exchange.setOldConsigneeAddress(exchangeDTO.getOldConsigneeAddress());
        exchange.setExchangeReason(exchangeDTO.getExchangeReason());
        exchange.setNewConsigneeAddress(exchangeDTO.getNewConsigneeAddress());
        exchange.setNewConsignee(exchangeDTO.getNewConsignee());
        exchange.setNewConsigneeTel(exchangeDTO.getNewConsigneeTel());
        exchange.setPayType(exchangeDTO.getPayType());
        if(exchangeDTO.getInputApplyCode() != null && !exchangeDTO.getInputApplyCode().equals("")){
        	exchange.setInputApplyCode(exchangeDTO.getInputApplyCode());
        }
        //testReturn.setSubmitTimes(String.valueOf(Integer.parseInt(returnDTO.getSubmitTimes())+1));
        
        exchangeManager.save(exchange);
        
        saveProduct(productList, exchange.getId().toString());
        
        fileUploadAPI.uploadFile(files, tenantId, Long.toString(exchange.getId()), "OA/process");
        String strFilePath="";
        if(!"isQuality".equals(quality))
        	SaveFormHtml(exchange,productList,businessKey,strFilePath);
        else
        	this.saveFormHtml(exchange, productList, businessKey, strFilePath);
        
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
    //productList
    public void SaveFormHtml(Exchange exchange,List<ExchangeProductsDTO> dataList,String bussinessKey,String strFilePath){
    	List<ExchangeProductsDTO> productList = new ArrayList<ExchangeProductsDTO>();
    	for (ExchangeProductsDTO exchangeProductsDTO : dataList) {
    		if(!"0".equals(exchangeProductsDTO.getProductNum())){
    			productList.add(exchangeProductsDTO);
    		}
		}
    	
    	
    	String detailHtml="<table  style=\"line-height:34px;\"  class=\"centerdiv\" cellpadding=\"0\" cellspacing=\"0\" >"
    			 +"<tr>"
    			 +"<td colspan=\"9\" align=\"center\"><h2>{exchangeTable}</h2></td>"
    			 +"</td>"
    	        +"</tr>"
    	        +"<tr>"
    	            +"<td class=\"f_td\" colspan=\"9\" style=\"padding-right:20px;text-align:right;\"><b>"
    	               	 +"&nbsp;&nbsp;受理单编号："
    	               	 +exchange.getApplyCode()
	               	+"</b></td>"
           	   +"</tr>"
           	   +"<tr>"
		            +"<td class=\"f_td\">所属仓库：</td>"
		            +"<td class=\"f_r_td\" colspan=\"2\">"
		               	 +exchange.getWareHouse()
		           	+"</td>"
		           	+"<td class=\"f_td\">审批换货时间：</td>"
		            +"<td class=\"f_r_td\" colspan=\"2\">"
		               	 +exchange.getExchangeDate()
		           	+"</td>"
		           	+"<td class=\"f_td\">客服工号：</td>"
		            +"<td class=\"f_r_td\" colspan=\"2\">"
		               	 +exchange.getEmpNo()
		           	+"</td>"
		       +"</tr>"
		       +"<tr>"
		            +"<td class=\"f_td\">专卖店编号/手机号：</td>"
		            +"<td class=\"f_r_td\" colspan=\"2\">"
		               	 +exchange.getUcode()
		           	+"</td>"
		           	+"<td class=\"f_td\">姓名：</td>"
		            +"<td class=\"f_r_td\" colspan=\"2\">"
		               	 +exchange.getName()
		           	+"</td>";
    	 if(exchange.getUcode().length() == 11){
    		 detailHtml +="<td class=\"f_td\">电话：</td>"
  		            +"<td class=\"f_r_td\" colspan=\"2\">无"
  		           	+"</td>"; 
    	 }else{
    		 detailHtml +="<td class=\"f_td\">电话：</td>"
  		            +"<td class=\"f_r_td\" colspan=\"2\">"
  		               	 +exchange.getTel()
  		           	+"</td>"; 
    	 }
    	 
		      detailHtml +="</tr>"
	          +"<tr>"
	            +"<td class=\"f_td\">订单编号：</td>"
	            +"<td class=\"f_r_td\" colspan=\"8\">"
	               	 +exchange.getOrderNumber()
	           	+"</td>"
           	  +"</tr>"
           	 +"<tr>"
	            +"<td class=\"f_td\">抄送：</td>"
	            +"<td class=\"f_r_td\" colspan=\"8\">"
	               	 +"{copyNames}"
	           	+"</td>"
        	  +"</tr>"
           	  +"<tr>"
	            +"<td class=\"f_td\">订货时间：</td>"
	            +"<td class=\"f_r_td\" colspan=\"3\">"
	               	 +exchange.getOrderTime()
	           	+"</td>"
	           	+"<td class=\"f_td\">手续费：</td>"
	            +"<td class=\"f_r_td\" colspan=\"4\">"
	               	 +(exchange.getPayType().equals("减免")?(exchange.getPayType()+"："+exchange.getInputApplyCode()):exchange.getPayType())
	           	+"</td>"
           	  +"</tr>"
           	  +"<tr>"
	            +"<td class=\"f_td\">原收货人姓名：</td>"
	            +"<td class=\"f_r_td\" colspan=\"2\">"
	               	 +exchange.getOldConsignee()
	           	+"</td>"
	           	+"<td class=\"f_td\">原收货电话：</td>"
	            +"<td class=\"f_r_td\" colspan=\"2\">"
	               	 +exchange.getOldConsigneeTel()
	           	+"</td>"
	           	+"<td class=\"f_td\">邮编：</td>"
	            +"<td class=\"f_r_td\" colspan=\"2\">"
	               	 +exchange.getZipCode()
	           	+"</td>"
		      +"</tr>"
		      +"<tr>"
	            +"<td class=\"f_td\">原收货地址：</td>"
	            +"<td class=\"f_r_td\" colspan=\"8\" style=\"word-break:break-all;word-wrap:break-word\">"
	               	 +exchange.getOldConsigneeAddress()
	           	+"</td>"
         	  +"</tr>"
	    	  +"<tr>"
	    	  	+"<td class=\"f_td\" rowspan=\"2\" style=\"text-align:center\">序号</td>"
		        +"<td class=\"f_td\" colspan=\"4\" style=\"text-align:center\">退回产品清单</td>"
		       	+"<td class=\"f_td\" colspan=\"4\" style=\"text-align:center\">所换产品清单</td>"
		      +"</tr>"
	          +"<tr>"
		        +"<td class=\"f_td\" style=\"text-align:center\">产品名称</td>"
		        +"<td class=\"f_r_td\" style=\"text-align:center\">数量</td>"
		       	+"<td class=\"f_td\" style=\"text-align:center\">总金额</td>"
		        +"<td class=\"f_r_td\" style=\"text-align:center\">总PV</td>"
		        +"<td class=\"f_td\" style=\"text-align:center\">产品名称</td>"
		        +"<td class=\"f_r_td\" style=\"text-align:center\">数量</td>"
		       	+"<td class=\"f_td\" style=\"text-align:center\">总金额</td>"
		        +"<td class=\"f_r_td\" style=\"text-align:center\">总PV</td>"
		      +"</tr>";
    		
		    	if(productList!=null&&productList.size()>0){
		    		  List<ExchangeProductsDTO> proReutrnList=new ArrayList<ExchangeProductsDTO>();
		    		  List<ExchangeProductsDTO> proExchangeList=new ArrayList<ExchangeProductsDTO>();
					  for(ExchangeProductsDTO pro:productList){
						  if(pro.getType().equals("0"))
							  proReutrnList.add(pro);
						  else if(pro.getType().equals("1"))
							  proExchangeList.add(pro);
					  }
					  
					  double returnTotalPrice=0.00;
					  double returnTottalPV=0.00;
					  int returnTotalNum=0;
					  double exchangeTotalPrice=0.00;
					  double exchangeTotalPV=0.00;
					  int exchangeTotalNum=0;
					  int returnLength=proReutrnList.size();
					  int exchangeLength=proExchangeList.size();
					  int maxSize=returnLength>exchangeLength?returnLength:exchangeLength;
					  for(int i=0;i<maxSize;i++){
						  detailHtml+="<tr>";
						  detailHtml+="<td class=\"f_td\" style=\"text-align:center;\">"+(i+1)+"</td>";
						  if(i<returnLength){
							  ExchangeProductsDTO proDto=proReutrnList.get(i);
							  detailHtml+=String.format("<td class=\"f_td\" style=\"text-align:center\">%s</td><td class=\"f_td\" style=\"text-align:center\">%s</td><td class=\"f_td\" style=\"text-align:center\">%s</td><td class=\"f_td\" style=\"text-align:center\">%s</td>",
									  				proDto.getProductName(),
								  					proDto.getProductNum(),
									  				proDto.getTotalPrice(),
									  				proDto.getTotalPv());
							  returnTotalNum+=Integer.parseInt(proDto.getProductNum());
							  returnTotalPrice+=Double.parseDouble(proDto.getTotalPrice());
							  returnTottalPV+=Double.parseDouble(proDto.getTotalPv());
						  }
						  else 
							  detailHtml+="<td class=\"f_td\" style=\"text-align:center\"></td><td class=\"f_td\" style=\"text-align:center\"></td><td class=\"f_td\" style=\"text-align:center\"></td><td class=\"f_td\" style=\"text-align:center\"></td>";
						  
						  if(i<exchangeLength){
							  ExchangeProductsDTO proDto=proExchangeList.get(i);
							  detailHtml+=String.format("<td class=\"f_td\" style=\"text-align:center\">%s</td><td class=\"f_td\" style=\"text-align:center\">%s</td><td class=\"f_td\" style=\"text-align:center\">%s</td><td class=\"f_td\" style=\"text-align:center\">%s</td>",
												  proDto.getProductName(),
												  proDto.getProductNum(),
												  proDto.getTotalPrice(),
												  proDto.getTotalPv());
							  exchangeTotalNum+=Integer.parseInt(proDto.getProductNum());
							  exchangeTotalPrice+=Double.parseDouble(proDto.getTotalPrice());
							  exchangeTotalPV+=Double.parseDouble(proDto.getTotalPv());
						  }
						  else
							  detailHtml+="<td class=\"f_td\" style=\"text-align:center\"></td><td class=\"f_td\" style=\"text-align:center\"></td><td class=\"f_td\" style=\"text-align:center\"></td><td class=\"f_td\" style=\"text-align:center\"></td>";
						  
						  detailHtml+="</tr>";
					  }
					  
					  detailHtml+=String.format("<tr><td class=\"f_td\" style=\"text-align:center\">合计</td><td class=\"f_td\" style=\"text-align:center\"></td><td class=\"f_td\" style=\"text-align:center\">%s</td><td class=\"f_td\" style=\"text-align:center\">%s</td><td class=\"f_td\" style=\"text-align:center\">%s</td><td class=\"f_td\" style=\"text-align:center\"></td><td class=\"f_td\" style=\"text-align:center\">%s</td><td class=\"f_td\" style=\"text-align:center\">%s</td><td class=\"f_td\" style=\"text-align:center\">%s</td></tr>",
							  					returnTotalNum,
							  					m2(returnTotalPrice),
							  					m2(returnTottalPV),
							  					exchangeTotalNum,
							  					m2(exchangeTotalPrice),
					  							m2(exchangeTotalPV)
							  					);
				  }
    	
    	detailHtml+="<tr>"
	            +"<td class=\"f_td\">换货原因：</td>"
	            +"<td class=\"f_r_td\" colspan=\"8\">"
	               	 +exchange.getExchangeReason()
	           	+"</td>"
	           	/*+"<td class=\"f_td\" rowspan=\"4\">身份证复印件：</td>"
				  +"<td class=\"f_r_td\" colspan=\"4\" rowspan=\"4\">"
				     	 +String.format("<img src=\"%s\" style=\"width:100px;height:100px;\" alt=\"身份证复印件\"/>",webAPI.getViewUrl()+"/"+strFilePath)
			 	  +"</td>"*/
           	  +"</tr>"
			  +"<tr>"
				+"<td class=\"f_td\"><div style=\"width:150px;\">更改后收货地址：</div></td>"
				+"<td class=\"f_r_td\" colspan=\"8\" style=\"word-break:break-all;word-wrap:break-word\">"
				   	 +exchange.getNewConsigneeAddress()
					+"</td>"
			  +"</tr>"
				 +"<tr>"
				 +"<td class=\"f_td\">更改后收货人：</td>"
				 +"<td class=\"f_r_td\" colspan=\"8\">"
				    	 +exchange.getNewConsignee()
				 +"</td>"
			  +"</tr>"
			  +"<tr>"
				  +"<td class=\"f_td\">更改后收货电话：</td>"
				  +"<td class=\"f_r_td\" colspan=\"8\">"
				     	 +exchange.getNewConsigneeTel()
			 	  +"</td>"
		 	  +"</tr>"
            +"</table>";
    	
    	
    	RecordInfo recordInfo=recordManager.findUniqueBy("businessKey", bussinessKey);
    	if(recordInfo != null){
    		recordInfo.setPkId(exchange.getId().toString());
    		recordInfo.setDetailHtml(detailHtml);
    	}
       	recordManager.save(recordInfo);
    }
    public void saveFormHtml(Exchange exchange,List<ExchangeProductsDTO> dataList,String bussinessKey,String strFilePath){
    	List<ExchangeProductsDTO> productList = new ArrayList<ExchangeProductsDTO>();
    	for (ExchangeProductsDTO exchangeProductsDTO : dataList) {
    		if(!"0".equals(exchangeProductsDTO.getProductNum())){
    			productList.add(exchangeProductsDTO);
    		}
		}
    	
    	String tel = "无";
    	if(exchange.getUcode().length()!=11)
    		tel = exchange.getTel();
    	String detailHtml="<table  style=\"line-height:34px;\"  class=\"centerdiv\" cellpadding=\"0\" cellspacing=\"0\" >"
   			 +"<tr>"
   			 +"<td colspan=\"7\" align=\"center\"><h2>{qualityExchangeTable}</h2></td>"
   	        +"</tr>"
   	        +"<tr>"
   	            +"<td class=\"f_td\" colspan=\"7\" style=\"padding-right:20px;text-align:right;\"><b>"
   	               	 +"&nbsp;&nbsp;受理单编号："
   	               	 +exchange.getApplyCode()
	               	+"</b></td>"
          	   +"</tr>"
          	   +"<tr>"
		            +"<td class=\"f_td\" colspan=\"2\">所属仓库：</td>"
		            +"<td class=\"f_r_td\" colspan=\"1\">"
		               	 +exchange.getWareHouse()
		           	+"</td>"
		           	+"<td class=\"f_td\" colspan=\"2\">客服工号：</td>"
		            +"<td class=\"f_r_td\" colspan=\"2\">"
		               	 +exchange.getEmpNo()
		           	+"</td>"
		       +"</tr>"
		       +"<tr>"
		            +"<td class=\"f_td\" style=\"text-align:center\" colspan=\"2\"> 专卖店编号/手机号</td>"
		           	+"<td class=\"f_td\" style=\"text-align:center\" colspan=\"1\">姓名</td>"
		           	+"<td class=\"f_td\" style=\"text-align:center\" colspan=\"1\">电话</td>"
		           	+"<td class=\"f_td\" style=\"text-align:center\" colspan=\"2\">订货时间</td>"
		           	+"<td class=\"f_td\" style=\"text-align:center\" colspan=\"1\">申请换货时间</td>"
	          +"</tr>"
	          +"<tr>"
	          	+"<td class=\"f_td\" style=\"text-align:center\" colspan=\"2\">"+ exchange.getUcode()+"</td>"
	           	+"<td class=\"f_td\" style=\"text-align:center\" colspan=\"1\">"+exchange.getName()+"</td>"
	           	+"<td class=\"f_td\" style=\"text-align:center\" colspan=\"1\">"+tel+"</td>"
	           	+"<td class=\"f_td\" style=\"text-align:center\" colspan=\"2\">"+exchange.getOrderTime()+"</td>"
	           	+"<td class=\"f_td\" style=\"text-align:center\" colspan=\"1\">"+exchange.getExchangeDate()+"</td>"
          	  +"</tr>"
          	 +"<tr>"
	            +"<td class=\"f_td\" colspan=\"2\">订单单据号：</td>"
	            +"<td class=\"f_r_td\" colspan=\"5\">"
	               	 +exchange.getOrderNumber()
	           	+"</td>"
	         +"</tr>"
	         +"<tr>"
	            +"<td class=\"f_td\" colspan=\"2\">抄送：</td>"
	            +"<td class=\"f_r_td\" colspan=\"5\">"
	               	 +"{copyNames}"
	           	+"</td>"
     	     +"</tr>"
	    	  +"<tr>"
	    	  	+"<td class=\"f_td\" style=\"text-align:center;width:20px;\">序号</td>"
	    	  	+"<td class=\"f_td\" style=\"text-align:center\">产品名称</td>"
		        +"<td class=\"f_r_td\" style=\"text-align:center\">数量</td>"
		       	+"<td class=\"f_td\" style=\"text-align:center\">总金额</td>"
		        +"<td class=\"f_r_td\" style=\"text-align:center\">总PV</td>"
		        +"<td class=\"f_r_td\" style=\"text-align:center\">生产日期</td>"
		        +"<td class=\"f_r_td\" style=\"text-align:center\">质保期</td>"
		      +"</tr>";
   		
		  /*  	if(productList!=null&&productList.size()>0){
		    		  List<ExchangeProductsDTO> proReutrnList=new ArrayList<ExchangeProductsDTO>();
		    		  List<ExchangeProductsDTO> proExchangeList=new ArrayList<ExchangeProductsDTO>();
					  for(ExchangeProductsDTO pro:productList){
						  if(pro.getType().equals("0"))
							  proReutrnList.add(pro);
						  else if(pro.getType().equals("1"))
							  proExchangeList.add(pro);
					  }
					  
					  double returnTotalPrice=0.00;
					  double returnTottalPV=0.00;
					  int returnTotalNum=0;
					  double exchangeTotalPrice=0.00;
					  double exchangeTotalPV=0.00;
					  int exchangeTotalNum=0;
					  int returnLength=proReutrnList.size();
					  int exchangeLength=proExchangeList.size();
					  int maxSize=returnLength>exchangeLength?returnLength:exchangeLength;
					  for(int i=0;i<productList.size();i++){
						  detailHtml+="<tr>";
						  detailHtml+="<td class=\"f_td\" style=\"text-align:center;\">"+(i+1)+"</td>";
						  if(i<returnLength){
							  ExchangeProductsDTO proDto=proReutrnList.get(i);
							  detailHtml+=String.format("<td class=\"f_td\" style=\"text-align:center\">%s</td><td class=\"f_td\" style=\"text-align:center\">%s</td><td class=\"f_td\" style=\"text-align:center\">%s</td><td class=\"f_td\" style=\"text-align:center\">%s</td><td class=\"f_td\" style=\"text-align:center\">%s</td><td class=\"f_td\" style=\"text-align:center\">%s</td>",
									  				proDto.getProductName(),
								  					proDto.getProductNum(),
									  				proDto.getTotalPrice(),
									  				proDto.getTotalPv(),
							  						proDto.getProductionDate(),
							  						proDto.getQualityAssuranceDate());
							  returnTotalNum+=Integer.parseInt(proDto.getProductNum());
							  returnTotalPrice+=Double.parseDouble(proDto.getTotalPrice());
							  returnTottalPV+=Double.parseDouble(proDto.getTotalPv());
						  }
						  else 
							  detailHtml+="<td class=\"f_td\" style=\"text-align:center\"></td><td class=\"f_td\" style=\"text-align:center\"></td><td class=\"f_td\" style=\"text-align:center\"></td><td class=\"f_td\" style=\"text-align:center\"></td><td class=\"f_td\" style=\"text-align:center\"></td><td class=\"f_td\" style=\"text-align:center\"></td>";
						  
						  if(i<exchangeLength){
							  ExchangeProductsDTO proDto=proExchangeList.get(i);
							  detailHtml+=String.format("<td class=\"f_td\" style=\"text-align:center\">%s</td><td class=\"f_td\" style=\"text-align:center\">%s</td><td class=\"f_td\" style=\"text-align:center\">%s</td><td class=\"f_td\" style=\"text-align:center\">%s</td>",
												  proDto.getProductName(),
												  proDto.getProductNum(),
												  proDto.getTotalPrice(),
												  proDto.getTotalPv());
							  exchangeTotalNum+=Integer.parseInt(proDto.getProductNum());
							  exchangeTotalPrice+=Double.parseDouble(proDto.getTotalPrice());
							  exchangeTotalPV+=Double.parseDouble(proDto.getTotalPv());
						  }
						  else
							  detailHtml+="<td class=\"f_td\" style=\"text-align:center\"></td><td class=\"f_td\" style=\"text-align:center\"></td><td class=\"f_td\" style=\"text-align:center\"></td><td class=\"f_td\" style=\"text-align:center\"></td>";
						  
						  detailHtml+="</tr>";
					  }
					  
					  detailHtml+=String.format("<tr><td class=\"f_td\" style=\"text-align:center\">合计</td><td class=\"f_td\" style=\"text-align:center\"></td><td class=\"f_td\" style=\"text-align:center\">%s</td><td class=\"f_td\" style=\"text-align:center\">%s</td><td class=\"f_td\" style=\"text-align:center\">%s</td><td class=\"f_td\" style=\"text-align:center\"></td><td class=\"f_td\" style=\"text-align:center\"></td><td class=\"f_td\" style=\"text-align:center\"></td><td class=\"f_td\" style=\"text-align:center\">%s</td><td class=\"f_td\" style=\"text-align:center\">%s</td><td class=\"f_td\" style=\"text-align:center\">%s</td></tr>",
							  					returnTotalNum,
							  					m2(returnTotalPrice),
							  					m2(returnTottalPV),
							  					exchangeTotalNum,
							  					m2(exchangeTotalPrice),
					  							m2(exchangeTotalPV)
							  					);
				  }*/
    			if(productList!=null&&productList.size()>0){
    				double returnTotalPrice=0.00;
					double returnTottalPV=0.00;
					int returnTotalNum=0;
    				for(int i=0;i<productList.size();i++){
    					detailHtml+="<tr>"
    							   +"<td class=\"f_td\" style=\"text-align:center;width:50px;\">"+(i+1)+"</td>"
    							   +"<td class=\"f_td\" style=\"text-align:center;\">"+productList.get(i).getProductName()+"</td>"
    							   +"<td class=\"f_td\" style=\"text-align:center;\">"+productList.get(i).getProductNum()+"</td>"
    							   +"<td class=\"f_td\" style=\"text-align:center;\">"+productList.get(i).getTotalPrice()+"</td>"
    							   +"<td class=\"f_td\" style=\"text-align:center;\">"+productList.get(i).getTotalPv()+"</td>"
    							   +"<td class=\"f_td\" style=\"text-align:center;\">"+productList.get(i).getProductionDate()+"</td>"
    							   +"<td class=\"f_td\" style=\"text-align:center;\">"+productList.get(i).getQualityAssuranceDate()+"</td>"
    							   +"</tr>";
    					returnTotalNum+=Integer.parseInt(productList.get(i).getProductNum());
						returnTotalPrice+=Double.parseDouble(productList.get(i).getTotalPrice());
						returnTottalPV+=Double.parseDouble(productList.get(i).getTotalPv());
    				}
    				detailHtml+=String.format("<tr><td class=\"f_td\" style=\"text-align:center\">合计</td><td class=\"f_td\" style=\"text-align:center\"></td><td class=\"f_td\" style=\"text-align:center\">%s</td><td class=\"f_td\" style=\"text-align:center\">%s</td><td class=\"f_td\" style=\"text-align:center\">%s</td><td class=\"f_td\" style=\"text-align:center\"></td><td class=\"f_td\" style=\"text-align:center\"></td></tr>",
		  					returnTotalNum,
		  					m2(returnTotalPrice),
		  					m2(returnTottalPV)
		  					);
    			}
   	detailHtml+="<tr>"
	            +"<td class=\"f_td\" colspan=\"2\">换货原因：</td>"
	            +"<td class=\"f_r_td\" colspan=\"5\">"
	               	 +exchange.getExchangeReason()
	           	+"</td>"
          	  +"</tr>"
			  +"<tr>"
				+"<td class=\"f_td\" colspan=\"2\">收货地址：</td>"
				+"<td class=\"f_r_td\" colspan=\"5\">"
				   	 +exchange.getNewConsigneeAddress()
					+"</td>"
           +"</table>";
   	
   	
   	/*String sqlRecordUpdate = "update KV_RECORD set detailHtml= '" + detailHtml + "',pk_Id='"+exchange.getId()+"' where id= " + bussinessKey ;
   	keyValueConnector.updateBySql(sqlRecordUpdate);*/
   	RecordInfo recordInfo=recordManager.findUniqueBy("businessKey", bussinessKey);
   	if(recordInfo != null){
   		recordInfo.setPkId(exchange.getId().toString());
   	   	recordInfo.setDetailHtml(detailHtml);
   	}
   	recordManager.save(recordInfo);
    }
    public String m2(double d_num) {
        DecimalFormat df = new DecimalFormat("#.00");
        return df.format(d_num);
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
        	
        	exchange.setWareHouse(m.getFirst("wareHouse"));
        	exchange.setEmpNo(m.getFirst("empNo"));
        	exchange.setUcode(m.getFirst("ucode"));
        	exchange.setName(m.getFirst("name"));
        	exchange.setTel(m.getFirst("tel"));
        	/*SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); 
        	
        	Date date = sdf.parse(m.getFirst("returnDate"));*/
        	exchange.setApplyCode(m.getFirst("applyCode"));
        	exchange.setInputApplyCode(m.getFirst("inputApplyCode"));
        	exchange.setExchangeDate(m.getFirst("exchangeDate"));
        	exchange.setOrderNumber(m.getFirst("orderNumber"));
        	exchange.setExchangeReason(m.getFirst("exchangeReason"));
        	exchange.setProcessInstanceId(processInstanceId);
        	exchange.setPayType(m.getFirst("payType"));
        	exchange.setOrderTime(m.getFirst("orderTime"));
        	exchange.setZipCode(m.getFirst("zipCode"));//oldConsignee
        	exchange.setOldConsignee(m.getFirst("oldConsignee"));
        	exchange.setOldConsigneeTel(m.getFirst("oldConsigneeTel"));
        	exchange.setOldConsigneeAddress(m.getFirst("oldConsigneeAddress"));
        	exchange.setNewConsignee(m.getFirst("newConsignee"));
        	exchange.setNewConsigneeTel(m.getFirst("newConsigneeTel"));
        	exchange.setNewConsigneeAddress(m.getFirst("newConsigneeAddress"));
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
        	// 删除子表
        	jdbcTemplate.update("DELETE FROM ENTITY_EXCHANGE_PRODUCT WHERE exchange_id=?", m.getFirst("exchangeId"));
        	
        	
        	//调整产品表数据重新写入表 (退货)
        	List<String> backProNoList = m.get("backProNo");
        	List<String> backUnitPriceList = m.get("backUnitPrice");
        	List<String> backUnitPVList = m.get("backUnitPV");
        	
        	List<String> backProNameList = m.get("backProName");
        	List<String> backNumberList = m.get("backNumber");
        	List<String> maxNumberList = m.get("maxNumber");
        	
        	List<String> backTotalPriceList = m.get("backTotalPrice");
        	List<String> backTotalPVList = m.get("backTotalPV");
        	//生产日期和质保期
        	List<String> productionDateList = m.get("productionDate");
        	List<String> qualityAssuranceDateList = m.get("qualityAssuranceDate");
        	
        	List<ExchangeProducts> productListNew=new ArrayList<ExchangeProducts>();
        	List<ExchangeProductsDTO> productListDTO=new ArrayList<ExchangeProductsDTO>();
        	for(int i=0;i<backProNameList.size();i++){
        		if("0".equals(backNumberList.get(i)))
        			continue;
        		
        		ExchangeProducts product = new ExchangeProducts();
        		product.setProductNo(backProNoList.get(i));
        		product.setPrice(backUnitPriceList.get(i));
        		product.setPv(backUnitPVList.get(i));
        		product.setProductName(backProNameList.get(i));
        		product.setProductNum(backNumberList.get(i));
        		product.setMaxProductNum(maxNumberList.get(i));
        		product.setTotalPrice(backTotalPriceList.get(i));
        		product.setTotalPv(backTotalPVList.get(i));
        		product.setExchangeId(m.getFirst("exchangeId"));
        		product.setType("0");
        		if(productionDateList != null)
        			product.setProductionDate(productionDateList.get(i));
        		if(qualityAssuranceDateList != null)
        			product.setQualityAssuranceDate(qualityAssuranceDateList.get(i));
        		productManager.save(product);
        		
        		ExchangeProductsDTO DTO = new ExchangeProductsDTO();
        		DTO.setProductNo(backProNoList.get(i));
        		DTO.setPrice(backUnitPriceList.get(i));
        		DTO.setPv(backUnitPVList.get(i));
        		DTO.setProductName(backProNameList.get(i));
        		DTO.setProductNum(backNumberList.get(i));
        		DTO.setMaxProductNum(maxNumberList.get(i));
        		DTO.setTotalPrice(backTotalPriceList.get(i));
        		DTO.setTotalPv(backTotalPVList.get(i));
        		DTO.setExchangeId(m.getFirst("exchangeId"));
        		if(productionDateList != null)
        			DTO.setProductionDate(productionDateList.get(i));
        		if(qualityAssuranceDateList != null)
        			DTO.setQualityAssuranceDate(qualityAssuranceDateList.get(i));
        		DTO.setType("0");
        		productListDTO.add(DTO);
        		
        	}
        	
        	//调整产品表数据重新写入表 (换货)
        	List<String> exchangeProNoList = m.get("exchangeProNo");
        	List<String> exchangeUnitPriceList = m.get("exchangeUnitPrice");
        	List<String> exchangeUnitPVList = m.get("exchangeUnitPV");
        	List<String> exchangeProNameList = m.get("exchangeProName");
        	List<String> exchangeNumberList = m.get("exchangeNumber");
        	
        	List<String> exchangeTotalPriceList = m.get("exchangeTotalPrice");
        	List<String> exchangeTotalPVList = m.get("exchangeTotalPV");
        	if(exchangeProNameList != null){
	        	for(int i=0;i<exchangeProNameList.size();i++){
	        		ExchangeProducts product = new ExchangeProducts();
	        		if(exchangeProNameList.get(i).equals("")||exchangeProNameList.get(i).equals("请选择")){
	        			continue;
	        		}
	        		if("0".equals(exchangeNumberList.get(i)))
	        			continue;
	        		product.setProductNo(exchangeProNoList.get(i));
	        		product.setPrice(exchangeUnitPriceList.get(i));
	        		product.setPv(exchangeUnitPVList.get(i));
	        		product.setProductName(exchangeProNameList.get(i));
	        		product.setProductNum(exchangeNumberList.get(i));
	        		product.setTotalPrice(exchangeTotalPriceList.get(i));
	        		product.setTotalPv(exchangeTotalPVList.get(i));
	        		product.setExchangeId(m.getFirst("exchangeId"));
	        		product.setType("1");
	        		productListNew.add(product);
	        		productManager.save(product);
	        		
	        		ExchangeProductsDTO DTO = new ExchangeProductsDTO();
	        		DTO.setProductNo(exchangeProNoList.get(i));
	        		DTO.setPrice(exchangeUnitPriceList.get(i));
	        		DTO.setPv(exchangeUnitPVList.get(i));
	        		DTO.setProductName(exchangeProNameList.get(i));
	        		DTO.setProductNum(exchangeNumberList.get(i));
	        		DTO.setTotalPrice(exchangeTotalPriceList.get(i));
	        		DTO.setTotalPv(exchangeTotalPVList.get(i));
	        		DTO.setExchangeId(m.getFirst("exchangeId"));
	        		DTO.setType("1");
	        		productListDTO.add(DTO);
	        	}
        	}
        	String strFilePath="";
            /*List<StoreInfo> storeList=storeConnector.getStore(exchange.getId().toString());
            if(storeList!=null&&storeList.size()>0)
            	strFilePath=storeList.get(0).getPath();*/
        	if("isQuality".equals(quality))
        		this.saveFormHtml(exchange, productListDTO, formParameter.getBusinessKey(), strFilePath);
        	else
        		this.SaveFormHtml(exchange, productListDTO, formParameter.getBusinessKey(), strFilePath);
            
        }
        if (f.equals("4")) {
        	processParameters.put("leaderComment", "撤销申请");
        	formParameter.setAction("撤销申请");
        }
      
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
        
        //if (StringUtils.isBlank(multipartHandler.getMultiValueMap().getFirst("bpmProcessId"))) {
        	
        	String busDetailId = multipartHandler.getMultiValueMap().getFirst("businessDetailId");   // 业务细分ID
        	if (busDetailId!=null) {
	        	BusinessDetailEntity businessDetailEntity = businessDetailManager.findUniqueBy("id", Long.parseLong(busDetailId));
	        	if (businessDetailEntity != null) {
	        		String bpmProcessId = businessDetailEntity.getBpmProcessId();
	        		formParameter.setBpmProcessId(bpmProcessId);
	        	}
        	} 
        /*}else {
        	formParameter.setBpmProcessId(multipartHandler.getMultiValueMap().getFirst("bpmProcessId"));
        }*/
        
        
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
            	pro.setProductNo(product.get(i).getProductNo());//商品编号
            	pro.setProductName(product.get(i).getProductName());
            	pro.setMaxProductNum(product.get(i).getMaxProductNum());
            	pro.setProductNum(product.get(i).getProductNum());
            	pro.setPv(product.get(i).getPv());//商品单价pv
            	pro.setTotalPv(product.get(i).getTotalPv());
            	pro.setPrice(product.get(i).getPrice());//商品单价金额
            	pro.setTotalPrice(product.get(i).getTotalPrice());
            	pro.setType(product.get(i).getType());
            	pro.setProductionDate(product.get(i).getProductionDate());//生产日期
            	pro.setQualityAssuranceDate(product.get(i).getQualityAssuranceDate());//质保期

                exchangeProductsManager.save(pro);
        	}
        	
        }
    }

    /**
     * 发起流程.
     */
/*    public void startProcessInstance(String userId, String businessKey,
            String processDefinitionId, Map<String, Object> processParameters,
            Record record) {
        String processInstanceId = processConnector.startProcess(userId,
                businessKey, processDefinitionId, processParameters);

        record = new RecordBuilder().build(record, STATUS_RUNNING,
                processInstanceId);
        keyValueConnector.save(record);
    }
    */
   
/*
    public String getParameter(Map<String, String[]> parameters, String name) {
        String[] value = parameters.get(name);

        if ((value == null) || (value.length == 0)) {
            return null;
        }

        return value[0];
    }

    public List<String> getParameterValues(Map<String, String[]> parameters,
            String name) {
        String[] value = parameters.get(name);

        if ((value == null) || (value.length == 0)) {
            return Collections.EMPTY_LIST;
        }

        return Arrays.asList(value);
    }

    public Map<String, Object> getVariables(Map<String, String[]> parameters) {
        Map<String, Object> variables = new HashMap<String, Object>();

        for (Map.Entry<String, String[]> entry : parameters.entrySet()) {
            String key = entry.getKey();
            String[] value = entry.getValue();

            if ((value == null) || (value.length == 0)) {
                variables.put(key, null);
            } else {
                variables.put(key, value[0]);
            }
        }

        return variables;
    }*/
    
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
}
