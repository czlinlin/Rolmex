package com.mossle.operation.service;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mossle.api.keyvalue.FormParameter;
import com.mossle.api.keyvalue.KeyValueConnector;
import com.mossle.api.keyvalue.Record;
import com.mossle.api.process.ProcessConnector;
import com.mossle.api.process.ProcessDTO;
import com.mossle.api.tenant.TenantHolder;
import com.mossle.base.persistence.domain.BusinessDetailEntity;
import com.mossle.base.persistence.manager.BusinessDetailManager;
import com.mossle.core.MultipartHandler;
import com.mossle.core.auth.CurrentUserHolder;
import com.mossle.keyvalue.persistence.domain.RecordInfo;
import com.mossle.keyvalue.persistence.manager.RecordManager;
import com.mossle.msg.persistence.manager.MsgInfoManager;
import com.mossle.operation.persistence.domain.Product;
import com.mossle.operation.persistence.domain.Return;
import com.mossle.operation.persistence.domain.ReturnDTO;
import com.mossle.operation.persistence.manager.ProductManager;
import com.mossle.operation.persistence.manager.ReturnManager;
import com.mossle.operation.web.ProcessOperationReturnController;

@Service
@Transactional(readOnly = true)
public class ReturnService {
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
    private ReturnManager returnManager;
    private ProductManager productManager;
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
    @Resource
    private MsgInfoManager msgInfoManager;
    @Resource
    private RecordManager recordManager;

    /**
     * 发起申请退货流程.
     */
    @Transactional(readOnly = false)
    public String saveReturn(HttpServletRequest request,ReturnDTO returnDTO,List<Product> productList, String userId,String areaId,String areaName,
    		String companyId,String companyName,
            String businessKey) throws Exception {
        
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
        
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        
        Return testReturn = new Return();
        testReturn.setId(returnDTO.getId());
        testReturn.setWareHouse(returnDTO.getWareHouse());
        testReturn.setEmpNo(returnDTO.getEmpNo());
        testReturn.setUcode(returnDTO.getUcode());
        testReturn.setShopName(returnDTO.getShopName());
        testReturn.setShopTel(returnDTO.getShopTel());
        testReturn.setReturnDate(formatter.parse(returnDTO.getReturnDate()));
        testReturn.setOrderNumber(returnDTO.getOrderNumber().toString().replace("'", ""));
        testReturn.setReturnReaon(returnDTO.getReturnReaon());
        testReturn.setShopPayStock(returnDTO.getShopPayStock());
        testReturn.setRewardIntegralStock(returnDTO.getRewardIntegralStock());
        testReturn.setPersonPayStock(returnDTO.getPersonPayStock());
        testReturn.setPayType(returnDTO.getPayType());
        testReturn.setUserId(Long.parseLong(userId));
        testReturn.setProcessInstanceId(record.getRef());
        testReturn.setSubmitTimes(String.valueOf(Integer.parseInt(returnDTO.getSubmitTimes())+1));
        //ckx
        testReturn.setInputApplyCode(returnDTO.getInputApplyCode());
        testReturn.setBankDeposit(returnDTO.getBankDeposit());
        testReturn.setAccountName(returnDTO.getAccountName());
        testReturn.setAccountNumber(returnDTO.getAccountNumber());
        
        returnManager.save(testReturn);
       
        saveProduct(productList, testReturn.getId());
        
        SaveFormHtml(testReturn,productList,businessKey);
        
        //根据 bpmProcessId  到 oa_ba_business_detail 表 找对应的明细ID
        String sql = "from BusinessDetailEntity where  bpmProcessId=? ";
        BusinessDetailEntity businessDetailEntity = businessDetailManager.findUnique(sql, bpmProcessId);
        processParameters.put("businessDetailId",businessDetailEntity.getId());
        //原jdbc更新record操作更改如下，更新的属性保持一致  TODO sjx 18.11.23
        RecordInfo recordInfo = recordManager.findUniqueBy("businessKey", formParameter.getBusinessKey());
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
    	operationService.deleteApplyCode(returnDTO.getApplyCode());
    	return "";
    }
    
    // 审批
    @Transactional(readOnly = false)
    public void saveReReturn(HttpServletRequest request,RedirectAttributes redirectAttributes,
            String processInstanceId, String humanTaskId) throws Exception, IOException {
    	
    	String userId = currentUserHolder.getUserId();
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
        	Return reReturn = new Return();
        	reReturn.setId(Long.parseLong(m.getFirst("returnId")));
        	reReturn.setWareHouse(m.getFirst("wareHouse"));
        	reReturn.setEmpNo(m.getFirst("empNo"));
        	reReturn.setUcode(m.getFirst("ucode"));
        	reReturn.setShopName(m.getFirst("shopName"));
        	reReturn.setShopTel(m.getFirst("shopTel"));
        	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); 
        	
        	Date date = sdf.parse(m.getFirst("returnDate"));
        	reReturn.setReturnDate(date);
        	reReturn.setOrderNumber(m.getFirst("orderNumber"));
        	reReturn.setReturnReaon(m.getFirst("returnReaon"));
        	reReturn.setShopPayStock(m.getFirst("shopPayStock"));
        	reReturn.setRewardIntegralStock(m.getFirst("rewardIntegralStock"));
        	reReturn.setPersonPayStock(m.getFirst("personPayStock"));
        	reReturn.setProcessInstanceId(processInstanceId);
        	reReturn.setPayType(m.getFirst("payType"));
        	reReturn.setUserId(Long.parseLong(userId));
        	reReturn.setId(Long.parseLong(m.getFirst("returnId")));
        	reReturn.setSubmitTimes(m.getFirst("submitTimes"));
        	reReturn.setInputApplyCode(m.getFirst("inputApplyCode"));
        	reReturn.setBankDeposit(m.getFirst("bankDeposit"));
        	reReturn.setAccountName(m.getFirst("accountName"));
        	reReturn.setAccountNumber(m.getFirst("accountNumber"));
        	returnManager.save(reReturn);
        	// zyl 处理 record 表中的提交次数 备注：由jdbc方式更改如下，更新属性保持不变 TODO sjx 18.11.27
	        RecordInfo recordInfo = recordManager.findUniqueBy("businessKey", formParameter.getBusinessKey());
	        recordInfo.setSubmitTimes(Integer.parseInt(reReturn.getSubmitTimes()));
	        recordManager.save(recordInfo);
        	// 删除子表
        	jdbcTemplate.update("DELETE FROM ENTITY_PRODUCT WHERE return_id=?", m.getFirst("returnId"));
        	
        	
        	//调整产品表数据重新写入表
        	List<String> proNoList = m.get("proNo");//产品编号
        	List<String> proNameList = m.get("proName");//产品名称
        	List<String> proPVList = m.get("proPV");//产品单价PV值
        	
        	List<String> shopPVNumList = m.get("shopPVNum");//店支付（订货数量，退货数量，PV值）
        	List<String> shopReNumList = m.get("shopReturn");
        	List<String> shopPVList = m.get("shopProPV");
        	
        	List<String> shopRewardNumList = m.get("shopRewardNum");//奖励积分（订货数量，退货数量，PV值）
        	List<String> shopRewNumList = m.get("rewardReturn");
        	List<String> shopRewardPVList = m.get("shopRewardPV");
        	
        	List<String> shopWalletNumList = m.get("shopWalletNum");//个人钱包（订货数量，退货数量，PV值）
        	List<String> shopwalNumList = m.get("walletReturn");
        	List<String> shopWalletPVList = m.get("shopWalletPV");
        	
        	List<Product> productListNew=new ArrayList<Product>();
        	for(int i=0;i<shopPVNumList.size();i++){
        		if(!"0".equals(shopReNumList.get(i)) || !"0".equals(shopRewNumList.get(i)) || !"0".equals(shopwalNumList.get(i))){
        			Product product = new Product();
            		product.setProNo(proNoList.get(i));
            		product.setProName(proNameList.get(i));
            		product.setShopPVNum(shopPVNumList.get(i));
            		product.setShopReNum(shopReNumList.get(i));
            		product.setShopPV(shopPVList.get(i));
            		product.setShopRewardNum(shopRewardNumList.get(i));
            		product.setShopRewNum(shopRewNumList.get(i));
            		product.setShopRewardPV(shopRewardPVList.get(i));
            		product.setShopWalletNum(shopWalletNumList.get(i));
            		product.setShopwalNum(shopwalNumList.get(i));
            		product.setShopWalletPV(shopWalletPVList.get(i));
            		product.setProPV(proPVList.get(i));
            		product.setReturnId(Long.parseLong(m.getFirst("returnId")));
            		productListNew.add(product);
            		productManager.save(product);
        		}
        	}
        	
        	this.SaveFormHtml(reReturn,productListNew,formParameter.getBusinessKey());
            
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
     * 生成表单的html
     * **/
    public void SaveFormHtml(Return testReturn,List<Product> productList,String bussinessKey){
    	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    	String detailHtml="<table  style=\"line-height:34px;\"  class=\"centerdiv\" cellpadding=\"0\" cellspacing=\"0\" >"
    			 +"<tr>"
    			 +"<td colspan=\"10\" align=\"center\"><h2>退货申请单</h2></td>"
    			 +"</td>"
    	        +"</tr>"
    	       /* +"<tr>"
    	            +"<td class=\"f_td\" colspan=\"10\" style=\"padding-right:20px;text-align:right;\">"
    	            + "<b>&nbsp;&nbsp;受理单编号："
    	               	 +testReturn()
	               	+"</b></td>"
           	   +"</tr>"*/
           	   +"<tr>"
		            +"<td class=\"f_td\" colspan=\"5\" style=\"text-align:center\">"
		            + "所属仓库："+testReturn.getWareHouse()
		            + "</td>"
		            +"<td class=\"f_r_td\" colspan=\"5\">"
		               	 +"客服工号："+testReturn.getEmpNo()
		           	+"</td>"
	           +"</tr>"    	
           	   +"<tr>"
		            +"<td class=\"f_td\">专卖店编号/手机号</td>"
		            +"<td class=\"f_td\" colspan=\"4\">专卖店姓名</td>"
		            +"<td class=\"f_td\">专卖店电话</td>"
		            +"<td class=\"f_td\" colspan=\"4\">申请退货日期</td>"
		       +"</tr>"
		       +"<tr>"
		            +"<td class=\"f_td\">"+testReturn.getUcode()+"</td>"
		            +"<td class=\"f_r_td\" colspan=\"4\">"+testReturn.getShopName()+"</td>"
		            +"<td class=\"f_td\">"+testReturn.getShopTel()+"</td>"
		            +"<td class=\"f_r_td\" colspan=\"4\">"+format.format(testReturn.getReturnDate())+"</td>"
		       +"</tr>"
           	   +"<tr>"
		            +"<td class=\"f_td\">单据号：</td>"
		            +"<td class=\"f_r_td\" colspan=\"9\">"
		               	 +testReturn.getOrderNumber().toString().replace("'", "")
		           	+"</td>"
		       +"</tr>"
		       +"<tr>"
		            +"<td class=\"f_td\">抄送：</td>"
		            +"<td class=\"f_r_td\" colspan=\"9\">"
		            +"{copyNames}"
		           	+"</td>"
		       +"</tr>"
		       +"<tr>"
		           	+"<td class=\"f_td\" rowspan=\"2\">产品名称</td>"
		            +"<td class=\"f_td\" colspan=\"3\">店支付账户退货</td>"
		            +"<td class=\"f_td\" colspan=\"3\">奖励积分账户退货</td>"
		            +"<td class=\"f_td\" colspan=\"3\">个人钱包账户退货</td>"
		      +"</tr>"
		      +"<tr>"
		           	+"<td class=\"f_td\">订货数量</td>"
		           	+"<td class=\"f_td\">退货数量</td>"
		           	+"<td class=\"f_td\">总PV</td>"
		           	+"<td class=\"f_td\">订货数量</td>"
		           	+"<td class=\"f_td\">退货数量</td>"
		           	+"<td class=\"f_td\">总PV</td>"
		           	+"<td class=\"f_td\">订货数量</td>"
		           	+"<td class=\"f_td\">退货数量</td>"
		           	+"<td class=\"f_td\">总PV</td>"
		      +"</tr>";
    		  if(productList!=null&&productList.size()>0){
    			  for(Product pro:productList){
    				  detailHtml+="<tr>"
    				  +"<td class=\"f_td\">"+pro.getProName()+"</td>"
    				  +"<td class=\"f_td\">"+pro.getShopPVNum()+"</td>"
    				  +"<td class=\"f_td\">"+pro.getShopReNum()+"</td>"
    				  +"<td class=\"f_td\">"+pro.getShopPV()+"</td>"
    				  
    				  +"<td class=\"f_td\">"+pro.getShopRewardNum()+"</td>"
    				  +"<td class=\"f_td\">"+pro.getShopRewNum()+"</td>"
    				  +"<td class=\"f_td\">"+pro.getShopRewardPV()+"</td>"
    				  
    				  +"<td class=\"f_td\">"+pro.getShopWalletNum()+"</td>"
    				  +"<td class=\"f_td\">"+pro.getShopwalNum()+"</td>"
    				  +"<td class=\"f_td\">"+pro.getShopWalletPV()+"</td>"
    				  +"</tr>";
    			  }
    		  }
    	
	          detailHtml+="<tr>"     
		           	+"<td class=\"f_td\">退货原因</td>"
		            +"<td class=\"f_r_td\" colspan=\"9\">"
		               	 +"<pre style=\"white-space: pre-wrap;word-wrap: break-word;\">"+testReturn.getReturnReaon().toString().replace("'", "")+"</pre>"
		           	+"</td>"
		   	  +"</tr>"
		   	  +"<tr>"     
		           	+"<td class=\"f_td\" rowspan=\"2\">专卖店库存</td>"
		           	+"<td class=\"f_td\" colspan=\"2\">店支付库存</td>"
		           	+"<td class=\"f_td\" colspan=\"2\">奖励积分库存</td>"
		           	+"<td class=\"f_td\" colspan=\"2\">个人钱包库存</td>"
		           	+"<td class=\"f_td\" rowspan=\"2\">手续费</td>"
		           	+"<td class=\"f_td\" rowspan=\"2\" colspan=\"2\">"
		           	+testReturn.getPayType()
		           	+ "</td>"
		   	  +"</tr>"
		   	  +"<tr>"
		           	+"<td class=\"f_td\" colspan=\"2\">"+testReturn.getShopPayStock()+"</td>"
		           	+"<td class=\"f_td\" colspan=\"2\">"+testReturn.getRewardIntegralStock()+"</td>"
		           	+"<td class=\"f_td\" colspan=\"2\">"+testReturn.getPersonPayStock()+"</td>"
		           	+ "</td>"
		   	  +"</tr>"
			  +"<tr>"     
		           	+"<td class=\"f_td\" rowspan=\"2\">退款账户信息</td>"
		           	+"<td class=\"f_td\" colspan=\"3\">开户行(含支行)</td>"
		           	+"<td class=\"f_td\" colspan=\"3\">开户名</td>"
		           	+"<td class=\"f_td\" colspan=\"3\">账号</td>"
	          +"</tr>"
	          +"<tr>"
		           	+"<td class=\"f_td\" colspan=\"3\">"+testReturn.getBankDeposit()+"</td>"
		           	+"<td class=\"f_td\" colspan=\"3\">"+testReturn.getAccountName()+"</td>"
		           	+"<td class=\"f_td\" colspan=\"3\">"+testReturn.getAccountNumber()+"</td>"
		           	+ "</td>"
	          +"</tr>";
		           	
	          if(testReturn.getUcode().length() == 11){
	        	  detailHtml+="<tr>"
	        			  +"<td class=\"f_td\" colspan=\"10\"><font color=\"red\">*</font>&nbsp;商城退货请确定是否已撤单<input disabled=\"true\" type=\"checkbox\" id=\"confirmId\" checked></td>"
	        			  +"</tr>";
	        	  
	          }
	         
	         
	         
	          detailHtml+="</table>";
    	
	        RecordInfo recordInfo=recordManager.findUniqueBy("businessKey", bussinessKey);
	      	if(recordInfo != null){
	      		recordInfo.setPkId(testReturn.getId().toString());
	      		recordInfo.setDetailHtml(detailHtml);
	      	}
	        recordManager.save(recordInfo);
    }
    
    /**
     * 发起申请退货流程.
     */
    public String saveProduct(List<Product> product, Long returnId) throws Exception {
    	
    	//产品数据存表
    	
        for(int i = 0;i<product.size();i++){
        	if(!"0".equals(product.get(i).getShopReNum()) || !"0".equals(product.get(i).getShopRewNum()) || !"0".equals(product.get(i).getShopwalNum())){
        		Product pro = new Product();
            	pro.setId(product.get(i).getId());
            	pro.setReturnId(returnId);
            	pro.setProNo(product.get(i).getProNo());
            	pro.setProName(product.get(i).getProName());
            	pro.setProPV(product.get(i).getProPV());
            	//店支付
                pro.setShopPVNum(product.get(i).getShopPVNum());
                pro.setShopReNum(product.get(i).getShopReNum());
                pro.setShopPV(product.get(i).getShopPV());
                //奖励积分
                pro.setShopRewardNum(product.get(i).getShopRewardNum());
                pro.setShopRewNum(product.get(i).getShopRewNum());
                pro.setShopRewardPV(product.get(i).getShopRewardPV());
                
                //个人钱包
                pro.setShopWalletNum(product.get(i).getShopWalletNum());
                pro.setShopwalNum(product.get(i).getShopwalNum());
                pro.setShopWalletPV(product.get(i).getShopWalletPV());

                productManager.save(pro);
        	}
        }
    	return "";
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
    public void setReturnManager(ReturnManager returnManager) {
        this.returnManager = returnManager;
    }
    @Resource
    public void setProductManager(ProductManager productManager) {
    	this.productManager = productManager;
    }
    @Resource
    public void setBusinessDetailManager(BusinessDetailManager businessDetailManager) {
        this.businessDetailManager = businessDetailManager;
    }
}
