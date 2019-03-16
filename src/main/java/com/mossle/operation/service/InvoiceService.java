package com.mossle.operation.service;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.mossle.api.tenant.TenantHolder;
import com.mossle.base.persistence.domain.BusinessDetailEntity;
import com.mossle.base.persistence.manager.BusinessDetailManager;
import com.mossle.common.utils.FileUploadAPI;
import com.mossle.core.MultipartHandler;
import com.mossle.core.auth.CurrentUserHolder;
import com.mossle.core.util.StringUtils;
import com.mossle.dict.persistence.domain.DictInfo;
import com.mossle.dict.support.DictConnectorImpl;
import com.mossle.humantask.persistence.domain.TaskInfo;
import com.mossle.humantask.persistence.manager.TaskInfoManager;
import com.mossle.keyvalue.persistence.domain.RecordInfo;
import com.mossle.keyvalue.persistence.manager.RecordManager;
import com.mossle.msg.persistence.manager.MsgInfoManager;
import com.mossle.operation.persistence.domain.Invoice;
import com.mossle.operation.persistence.domain.InvoiceDTO;
import com.mossle.operation.persistence.manager.InvoiceManager;

@Service
@Transactional(readOnly = true)
public class InvoiceService {
    private static Logger logger = LoggerFactory.getLogger(InvoiceService.class);
    
    public static final String OPERATION_BUSINESS_KEY = "businessKey";
    public static final String OPERATION_TASK_ID = "taskId";
    public static final String OPERATION_BPM_PROCESS_ID = "bpmProcessId";
    public static final int STATUS_DRAFT_PROCESS = 0;
    public static final int STATUS_DRAFT_TASK = 1;
    public static final int STATUS_RUNNING = 2;
    
    private KeyValueConnector keyValueConnector;
    private ProcessConnector processConnector;

    private OperationService operationService;
    private InvoiceManager invoiceManager;
    private FileUploadAPI fileUploadAPI;
    private BusinessDetailManager businessDetailManager;
    private DictConnectorImpl dictConnectorImpl;
    @Resource
    private CurrentUserHolder currentUserHolder;
    @Resource
    private TenantHolder tenantHolder;
    @Resource
    private MultipartResolver multipartResolver;
    @Resource
    private MsgInfoManager msgInfoManager;
    @Resource
    private RecordManager recordManager;
    
    private TaskInfoManager taskInfoManager;
    /**
     * 发起申请发票流程.
     */
    @Transactional(readOnly = false)
    public String saveInvoice(HttpServletRequest request,InvoiceDTO invoiceDTO,String areaId,String areaName,String companyId,String companyName, String userId, String tenantId,
           String bpmProcessId, String businessKey,MultipartFile[] files) throws Exception {
             
    	Map<String, Object> processParameters = new HashMap<String, Object>();
    	FormParameter formParameter = this.doSaveRecord(request);
		if (StringUtils.isBlank(bpmProcessId)) {
        	bpmProcessId = formParameter.getBpmProcessId();
        }
        ProcessDTO processDto = processConnector.findProcess(bpmProcessId);
        String processDefinitionId = processDto.getProcessDefinitionId();
       
        Record record = keyValueConnector.findByCode(formParameter.getBusinessKey());
        
        businessKey = formParameter.getBusinessKey();
        
        //得到岗位，存储add by lilei at 2018-09-12
        processParameters.put("positionId", request.getParameter("iptStartPositionId"));
        this.operationService.startProcessInstance(userId, businessKey,
                processDefinitionId, processParameters, record);
        
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        
        Invoice invoice = new Invoice();
        //invoice.setId(Long.parseLong(Id));
        invoice.setIdNumber(invoiceDTO.getIdNumber());
        invoice.setUcode(invoiceDTO.getUcode());
        invoice.setShopName(invoiceDTO.getShopName());
        invoice.setShopTel(invoiceDTO.getShopTel());
        invoice.setInvoiceDate(formatter.parse(invoiceDTO.getInvoiceDate()));
        invoice.setOrderNumber(StringUtils.replaceSingleQuote(invoiceDTO.getOrderNumber().toString()));
        invoice.setTaxNumber(invoiceDTO.getTaxNumber());
        invoice.setArea(areaName);
        List<DictInfo> dictList=dictConnectorImpl.findDictInfoListByType("OwnedSystem", "1");
        for (DictInfo dictInfo : dictList) {
        	if (dictInfo.getValue().equals(invoiceDTO.getSystem()) ){
        		invoice.setSystem(dictInfo.getName());
        	}
        }
        invoice.setBranchOffice(companyName);
        invoice.setInvoiceType(invoiceDTO.getInvoiceType());
        invoice.setCategory(invoiceDTO.getCategory());
        invoice.setInvoiceTitle(StringUtils.replaceSingleQuote(invoiceDTO.getInvoiceTitle()));
        invoice.setInvoiceDetail(StringUtils.replaceSingleQuote(invoiceDTO.getInvoiceDetail()));
        invoice.setInvoiceMoney(invoiceDTO.getInvoiceMoney());
        invoice.setEnterpriseName(StringUtils.replaceSingleQuote(invoiceDTO.getEnterpriseName()));
        invoice.setTaxNumber(StringUtils.replaceSingleQuote(invoiceDTO.getTaxNumber()));
        invoice.setOpeningBank(StringUtils.replaceSingleQuote(invoiceDTO.getOpeningBank()));
        invoice.setAccountNumber(invoiceDTO.getAccountNumber());
        invoice.setEnterpriseAddress(StringUtils.replaceSingleQuote(invoiceDTO.getEnterpriseAddress()));
        invoice.setInvoiceMailAddress(StringUtils.replaceSingleQuote(invoiceDTO.getInvoiceMailAddress()));
        invoice.setAddressee(StringUtils.replaceSingleQuote(invoiceDTO.getAddressee()));
        invoice.setAddresseeTel(StringUtils.replaceSingleQuote(invoiceDTO.getAddresseeTel()));
        invoice.setAddresseeSpareTel(StringUtils.replaceSingleQuote(invoiceDTO.getAddresseeSpareTel()));
        
        /*if(!files.isEmpty()){
        	StoreDTO storeDto = storeConnector.saveStore(
                    "cms/jsp/invoice",
                    new MultipartFileDataSource(file), 
                    tenantId);
        	invoice.setEnclosure(file.getOriginalFilename());
        	invoice.setPath(storeDto.getKey());

        }*/
        //testInvoice.setEnclosure(testInvoiceDTO.getEnclosure());
        invoice.setUserId(Long.parseLong(userId));
        invoice.setProcessInstanceId(record.getRef());
        invoiceManager.save(invoice);
        fileUploadAPI.uploadFile(files, tenantId, Long.toString(invoice.getId()), "OA/process");
        
        SaveFormHtml(invoice,businessKey,invoiceDTO.getApplyCode());
        
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
    	
    	// 处理受理单编号
    	operationService.deleteApplyCode(invoiceDTO.getApplyCode());
    	return "";
    }
    
    public void SaveFormHtml(Invoice invoice,String bussinessKey,String applyCode){
    	 SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    	String detailHtml="<table  style=\"line-height:34px;\"  class=\"centerdiv\" cellpadding=\"0\" cellspacing=\"0\" >"
    			 +"<tr>"
	    			 +"<td colspan=\"4\" align=\"center\"><h2>发票申请单</h2></td>"
	    			 +"</td>"
    	        +"</tr>"
    			+"<tr>"
	    			+"<td class=\"f_td\" colspan=\"4\" align=\"center\">受理单号："+applyCode+"</td>"
	    			+"</td>"
    			+"</tr>"
    	        +"<tr>"
    	            +"<td class=\"f_td\">专卖店编号/手机号</td>"
    	            +"<td class=\"f_td\">专卖店姓名</td>"
    	            +"<td class=\"f_td\">专卖店电话</td>"
    	            +"<td class=\"f_td\">申请发票日期</td>"
           	   +"</tr>"
           	   +"<tr>"
		            +"<td class=\"f_td\">"+invoice.getUcode()+"</td>"
		            +"<td class=\"f_td\">"+invoice.getShopName()+"</td>"
		            +"<td class=\"f_td\">"+invoice.getShopTel()+"</td>"
		            +"<td class=\"f_td\">"+format.format(invoice.getInvoiceDate())+"</td>"
               +"</tr>"
           	   +"<tr>"
		            +"<td class=\"f_td\">订单单据号</td>"
		            +"<td class=\"f_r_td\" colspan=\"3\">"
		               	 +invoice.getOrderNumber().toString().replace("'", "")
		           	+"</td>"
		       +"</tr>"
		           	
				+"<tr>"
					+"<td class=\"f_td\">抄送：</td>"
					+"<td class=\"f_r_td\" colspan=\"3\">"
					+"{copyNames}"
					+"</td>"
				+"</tr>";
	    	if(invoice.getUcode().length() == 11){
	    		detailHtml +="<tr>"
			           	+"<td class=\"f_td\">所属区域</td>"
			            +"<td class=\"f_r_td\">"
			               	 +invoice.getArea()
			           	+"</td>"
			           	+"<td class=\"f_td\">所属体系：</td>"
			            +"<td class=\"f_r_td\">"
			            	+invoice.getBranchOffice()
			           	+"</td>"
			      +"</tr>";
	    	}else{
	    		detailHtml +="<tr>"
			           	+"<td class=\"f_td\">所属区域</td>"
			            +"<td class=\"f_r_td\">"
			               	 +invoice.getArea()
			           	+"</td>"
			           	+"<td class=\"f_td\">所属体系：</td>"
			            +"<td class=\"f_r_td\">"
			               	 +invoice.getSystem()
			           	+"</td>"
			      +"</tr>"
		          +"<tr>"     
			           	+"<td class=\"f_td\">所属分公司：</td>"
			            +"<td class=\"f_r_td\" colspan=\"3\">"
			               	 +invoice.getBranchOffice()
			           	+"</td>"
			   	  +"</tr>";
	    	}
    	
		   	detailHtml +="<tr>"     
		           	+"<td class=\"f_td\">发票类型：</td>"
		            +"<td class=\"f_r_td\" colspan=\"3\">"
		               	 +invoice.getInvoiceType()
		           	+"</td>"
		   	  +"</tr>";
		      if(invoice.getCategory().equals("个人"))
		      {
		    	  detailHtml+="<tr>"     
			           	+"<td class=\"f_td\" rowspan=\"4\">个人</td>";
		    	  detailHtml+="<td class=\"f_td\">发票抬头</td>"
				            +"<td class=\"f_r_td\" colspan=\"2\">"
				               	 +invoice.getInvoiceTitle().toString().replace("'", "")
				           	+"</td>"
				   	  +"</tr>"
				   	  +"<tr>"
				           	+"<td class=\"f_td\">发票明细(产品名称、价格、数量)</td>"
				            +"<td class=\"f_r_td\" colspan=\"2\">"
				               	 +invoice.getInvoiceDetail().toString().replace("'", "")
				           	+"</td>"
				   	  +"</tr>"
				   	  +"<tr>"
				           	+"<td class=\"f_td\">发票开具总金额</td>"
				            +"<td class=\"f_r_td\" colspan=\"2\">"
				               	 +invoice.getInvoiceMoney()
				           	+"</td>"
				   	  +"</tr>"
		    	  	  +"<tr>"     
		    	  	  		+"<td class=\"f_td\">身份证号</td>"
		    	  	  		+"<td class=\"f_r_td\" colspan=\"2\">"
		    	  	  			+invoice.getIdNumber()
		    	  	  		+"</td>"
		    	  	  +"</tr>";
		      }
		      
           	  if(invoice.getCategory().equals("对公"))
		      {     	
           		detailHtml+="<tr>"     
			           	+"<td class=\"f_td\" rowspan=\"8\">对公</td>";
           		detailHtml+="<td class=\"f_td\">发票抬头</td>"
			            +"<td class=\"f_r_td\" colspan=\"2\">"
			               	 +invoice.getInvoiceTitle().toString().replace("'", "")
			           	+"</td>"
			   	  +"</tr>"
			   	  +"<tr>"
			           	+"<td class=\"f_td\">发票明细(产品名称、价格、数量)</td>"
			            +"<td class=\"f_r_td\" colspan=\"2\">"
			               	 +invoice.getInvoiceDetail().toString().replace("'", "")
			           	+"</td>"
			   	  +"</tr>"
			   	  +"<tr>"
			           	+"<td class=\"f_td\">发票开具总金额</td>"
			            +"<td class=\"f_r_td\" colspan=\"2\">"
			               	 +invoice.getInvoiceMoney()
			           	+"</td>"
			   	  +"</tr>"
           		  +"<tr>"
			           	+"<td class=\"f_td\">企业名称</td>"
			            +"<td class=\"f_r_td\" colspan=\"2\">"
			               	 +invoice.getEnterpriseName().toString().replace("'", "")
			           	+"</td>"
			   	  +"</tr>"
			   	  +"<tr>"
			           	+"<td class=\"f_td\">税务登记证</td>"
			            +"<td class=\"f_r_td\" colspan=\"2\">"
			               	 +invoice.getTaxNumber().toString().replace("'", "")
			           	+"</td>"
			   	  +"</tr>"
			   	  +"<tr>"
			           	+"<td class=\"f_td\">开户行</td>"
			            +"<td class=\"f_r_td\" colspan=\"2\">"
			               	 +invoice.getOpeningBank().toString().replace("'", "")
			           	+"</td>"
			   	  +"</tr>"
			   	  +"<tr>"
			           	+"<td class=\"f_td\">开户行账号</td>"
			            +"<td class=\"f_r_td\" colspan=\"2\">"
			               	 +invoice.getAccountNumber()
			           	+"</td>"
			   	  +"</tr>"
			   	  +"<tr>"
			           	+"<td class=\"f_td\">企业地址及电话</td>"
			            +"<td class=\"f_r_td\" colspan=\"2\">"
			               	 +invoice.getEnterpriseAddress().toString().replace("'", "")
			           	+"</td>"
			   	  +"</tr>";
		      }
           	  
           	detailHtml+="<tr>"     
				           	+"<td class=\"f_td\">发票邮寄地址</td>"
				            +"<td class=\"f_r_td\" colspan=\"3\">"
				               	 +invoice.getInvoiceMailAddress().toString().replace("'", "")
				           	+"</td>"
				   	  +"</tr>"
		           	  +"<tr>"
				            +"<td class=\"f_td\" colspan=\"2\">收件人姓名</td>"
				            +"<td class=\"f_td\">收件人电话</td>"
				            +"<td class=\"f_td\">收件人备用电话</td>"
				   	   +"</tr>"
				   	   +"<tr>"
				            +"<td class=\"f_td\" colspan=\"2\">"+invoice.getAddressee().toString().replace("'", "")+"</td>"
				            +"<td class=\"f_td\">"+invoice.getAddresseeTel().toString().replace("'", "")+"</td>"
				            +"<td class=\"f_td\">"+invoice.getAddresseeSpareTel().toString().replace("'", "")+"</td>"
				       +"</tr>"
				       +"</table>";
    	
           	RecordInfo recordInfo=recordManager.findUniqueBy("businessKey", bussinessKey);
        	if(recordInfo != null){
        		recordInfo.setPkId(invoice.getId().toString());
        		recordInfo.setDetailHtml(detailHtml);
        	}
           	recordManager.save(recordInfo);
    }
    
    // 审批
    @Transactional(readOnly = false)
    public void saveReInvoice(HttpServletRequest request,RedirectAttributes redirectAttributes,
            String processInstanceId, 
            String humanTaskId, MultipartFile[] files, String iptdels) throws Exception, IOException {
    	
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

			// 重申请的数据写入表中
			Invoice invoice = new Invoice();
			invoice.setUcode(m.getFirst("ucode"));
			invoice.setShopName(m.getFirst("shopName"));
			invoice.setShopTel(m.getFirst("shopTel"));
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			invoice.setInvoiceDate(sdf.parse(m.getFirst("invoiceDate")));
			invoice.setOrderNumber(m.getFirst("orderNumber"));
			invoice.setArea(m.getFirst("area"));
			invoice.setSystem(m.getFirst("systemName"));
			invoice.setBranchOffice(m.getFirst("branchOffice"));
			invoice.setInvoiceType(m.getFirst("invoiceType"));
			invoice.setCategory(m.getFirst("category"));
			invoice.setInvoiceTitle(m.getFirst("invoiceTitle"));
			invoice.setInvoiceDetail(m.getFirst("invoiceDetail"));
			invoice.setInvoiceMoney(m.getFirst("invoiceMoney"));
			invoice.setIdNumber(m.getFirst("idNumber"));
			invoice.setEnterpriseName(m.getFirst("enterpriseName"));
			invoice.setTaxNumber(m.getFirst("taxNumber"));
			invoice.setOpeningBank(m.getFirst("openingBank"));
			invoice.setAccountNumber(m.getFirst("accountNumber"));
			invoice.setEnterpriseAddress(m.getFirst("enterpriseAddress"));
			invoice.setInvoiceMailAddress(m.getFirst("invoiceMailAddress"));
			invoice.setAddressee(m.getFirst("addressee"));
			invoice.setAddresseeTel(m.getFirst("addresseeTel"));
			invoice.setAddresseeSpareTel(m.getFirst("addresseeSpareTel"));

			String tenantId = tenantHolder.getTenantId();
			invoice.setUserId(Long.parseLong(userId));
			invoice.setProcessInstanceId(record.getRef());
			invoice.setId(Long.parseLong(m.getFirst("invoiceId")));
			// fileUploadAPI.uploadFile(files, tenantId,
			// Long.toString(invoice.getId()), "OA/process");
			// 保存附件
			if (iptdels != null && !iptdels.equals("")) {
				fileUploadAPI.uploadFileDel(iptdels, Long.toString(invoice.getId()));
			}
			fileUploadAPI.uploadFile(files, tenantId, Long.toString(invoice.getId()), "OA/process");
			invoiceManager.save(invoice);

			// 调整申请，提交次数需要+1。Bing 2017.11.18
			int submitTimes = record.getSubmitTimes();
			submitTimes++;
			//jdbc更新体系ID和体系名称操作更改如下，并修复次数未正常+1的问题 TODO sjx
	    	RecordInfo recordInfo = recordManager.findUniqueBy("businessKey", formParameter.getBusinessKey());
	        recordInfo.setSubmitTimes(submitTimes);
	        recordInfo.setSystemId(m.getFirst("system"));
	        recordInfo.setSystemName(invoice.getSystem());
	        recordManager.save(recordInfo);
			// 处理存储detailHtml
			this.SaveFormHtml(invoice, formParameter.getBusinessKey(), record.getApplyCode());
		}
		if (f.equals("4")) {
			processParameters.put("leaderComment", "撤销申请");
			formParameter.setAction("撤销申请");
		}

		this.operationService.completeTask(humanTaskId, userId, formParameter, processParameters, record,
					processInstanceId);
		//审批后将此任务对应的消息置为已读 2018.08.27 sjx
	    String updateMsg = "update MsgInfo set status=1 where data=?";
	    msgInfoManager.batchUpdate(updateMsg, humanTaskId);
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
    	if (StringUtils.isNotBlank(busDetailId)) {
        	BusinessDetailEntity businessDetailEntity = businessDetailManager.findUniqueBy("id", Long.parseLong(busDetailId));
        	if (businessDetailEntity != null) {
        		String bpmProcessId = businessDetailEntity.getBpmProcessId();
        		formParameter.setBpmProcessId(bpmProcessId);
        	}
    	} 
    	if (StringUtils.isBlank(formParameter.getBpmProcessId()) && StringUtils.isNotBlank(multipartHandler.getMultiValueMap().getFirst("bpmProcessId"))) {
    		formParameter.setBpmProcessId(multipartHandler.getMultiValueMap().getFirst("bpmProcessId"));
    	}
		formParameter.setHumanTaskId(multipartHandler.getMultiValueMap().getFirst("humanTaskId"));
		formParameter.setComment(multipartHandler.getMultiValueMap().getFirst("comment"));

		System.out.println("====== businessKey:" + formParameter.getBusinessKey() + ";busDetails:" + busDetailId + ";bpmProcessId:" + formParameter.getBpmProcessId());
		
		return formParameter;
	}

	/**
	 * 把数据先保存到keyvalue里.
	 */
	public FormParameter doSaveRecord(HttpServletRequest request) throws Exception {
		String userId = currentUserHolder.getUserId();
		String tenantId = tenantHolder.getTenantId();

		MultipartHandler multipartHandler = new MultipartHandler(multipartResolver);
		FormParameter formParameter = null;

		try {
			multipartHandler.handle(request);
			logger.debug("multiValueMap : {}", multipartHandler.getMultiValueMap());
			logger.debug("multiFileMap : {}", multipartHandler.getMultiFileMap());

			formParameter = this.buildFormParameter(multipartHandler);

			String businessKey = operationService.saveDraft(userId, tenantId, formParameter);

			if ((formParameter.getBusinessKey() == null) || "".equals(formParameter.getBusinessKey().trim())) {
				formParameter.setBusinessKey(businessKey);
			}
			// TODO zyl 2017-11-16 外部表单不需要保存prop
			/*
			 * Record record = keyValueConnector.findByCode(businessKey);
			 * 
			 * record = new RecordBuilder().build(record, multipartHandler,
			 * storeConnector, tenantId);
			 * 
			 * keyValueConnector.save(record);
			 */
		} finally {
			multipartHandler.clear();
		}

		return formParameter;
	}

    /**
     * 发起流程.
     */
  /*  public void startProcessInstance(String userId, String businessKey,
            String processDefinitionId, Map<String, Object> processParameters,
            Record record) {
        String processInstanceId = processConnector.startProcess(userId,
                businessKey, processDefinitionId, processParameters);

        record = new RecordBuilder().build(record, STATUS_RUNNING,
                processInstanceId);
        keyValueConnector.save(record);
    }*/
    
   
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
    public void setInvoiceManager(InvoiceManager invoiceManager) {
        this.invoiceManager = invoiceManager;
    }
    
    @Resource
    public void setFileUploadAPI(FileUploadAPI fileUploadAPI) {
        this.fileUploadAPI = fileUploadAPI;
    }
    @Resource
    public void setBusinessDetailManager(BusinessDetailManager businessDetailManager) {
        this.businessDetailManager = businessDetailManager;
    }
    @Resource
    public void setDictConnectorImpl(DictConnectorImpl dictConnectorImpl) {
    	this.dictConnectorImpl = dictConnectorImpl;
    }
    
    @Resource
    public void setTaskInfoManager(TaskInfoManager taskInfoManager) {
    	this.taskInfoManager = taskInfoManager;
    }
}
