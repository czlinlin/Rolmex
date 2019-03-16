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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.mossle.core.util.StringUtils;
import com.mossle.keyvalue.persistence.domain.RecordInfo;
import com.mossle.keyvalue.persistence.manager.RecordManager;
import com.mossle.msg.persistence.manager.MsgInfoManager;
import com.mossle.operation.persistence.domain.CancelOrder;
import com.mossle.operation.persistence.domain.CancelOrderDTO;
import com.mossle.operation.persistence.domain.CancelOrderSub;
import com.mossle.operation.persistence.manager.CancelOrderManager;
import com.mossle.operation.persistence.manager.CancelOrderSubManager;

@Service
@Transactional(readOnly = true)
public class CancelOrderService {
    private static Logger logger = LoggerFactory.getLogger(BusinessService.class);
    
    public static final String OPERATION_BUSINESS_KEY = "businessKey";
    public static final String OPERATION_TASK_ID = "taskId";
    public static final String OPERATION_BPM_PROCESS_ID = "bpmProcessId";
    public static final int STATUS_DRAFT_PROCESS = 0;
    public static final int STATUS_DRAFT_TASK = 1;
    public static final int STATUS_RUNNING = 2;
    
    private KeyValueConnector keyValueConnector;
    private ProcessConnector processConnector;
    private OperationService operationService;
    private TenantHolder tenantHolder;
    private CurrentUserHolder currentUserHolder;
    private CancelOrderManager cancelOrderManager;
    private CancelOrderSubManager cancelOrderSubManager;
    private MultipartResolver multipartResolver;
    private BusinessDetailManager businessDetailManager;
    private MsgInfoManager msgInfoManager;
    @Resource
    private RecordManager recordManager;

    
    /**
     * 发起申请
     * @param request
     * @param applyDTO
     * @param bpmProcessId
     * @param files
     * @throws Exception
     * @throws IOException
     */
    @Transactional(readOnly = false)
    public  void StartProcessCancelOrder(HttpServletRequest request,
			CancelOrderDTO cancelOrderDTO,String areaId,String areaName,String companyId,String companyName, String bpmProcessId)
			throws Exception {
	
		String businessKey;
		Map<String, Object> processParameters = new HashMap<String, Object>();
    	String userId = currentUserHolder.getUserId();
        FormParameter formParameter = this.doSaveRecord(request);
        ProcessDTO processDto = processConnector.findProcess(bpmProcessId);
        String processDefinitionId = processDto.getProcessDefinitionId();
        Record record = keyValueConnector.findByCode(formParameter.getBusinessKey());
        businessKey = formParameter.getBusinessKey();
        
        //得到岗位，存储add by lilei at 2018-09-12
        processParameters.put("positionId", request.getParameter("iptStartPositionId"));
        this.operationService.startProcessInstance(userId, businessKey,processDefinitionId, processParameters, record);
        
        //将店编号、姓名、电话、来电电话、撤单登记时间、登记人、是否核实、撤单备注 存入主表
        CancelOrder cancelOrder = new CancelOrder();
        cancelOrder.setApplyCode(cancelOrderDTO.getApplyCode());
        cancelOrder.setUserId(Long.parseLong(userId));
        cancelOrder.setProcessInstanceId(record.getRef());
        cancelOrder.setUcode(cancelOrderDTO.getUcode());
        cancelOrder.setShopName(cancelOrderDTO.getShopName());
        cancelOrder.setShopMobile(cancelOrderDTO.getShopMobile());
        cancelOrder.setMobile(cancelOrderDTO.getMobile().toString().replace("'", ""));
        cancelOrder.setRegisterName(cancelOrderDTO.getRegisterName());
        cancelOrder.setRegisterTime(cancelOrderDTO.getRegisterTime());
        cancelOrder.setIsChecked(cancelOrderDTO.getIsChecked());
        cancelOrder.setCancelRemark(StringUtils.replaceSingleQuote(cancelOrderDTO.getCancelRemark().toString()));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String str = sdf.format(new Date());
        cancelOrder.setCreateTime(str);
        cancelOrder.setHidTotal(cancelOrderDTO.getHidTotal());
        cancelOrder.setSubmitTimes(1);
        cancelOrderManager.save(cancelOrder);
        
        //处理受理单编号
        operationService.deleteApplyCode(cancelOrderDTO.getApplyCode());
  
        List<CancelOrderSub> cancelOrderList=new ArrayList<CancelOrderSub>();
        //页面有多少行撤单编号，取数字标识
        Long num = Long.parseLong( request.getParameter("hidNum"));
        //将撤单编号、姓名、加入日期、撤单类型、金额、业绩单号存入子表
        for(int i=1;i<num;i++){
        	
        	String cm = request.getParameter("ucode"+i);
        	
        	if(cm!=null){
        	CancelOrderSub cancelOrderSub = new CancelOrderSub();
        	cancelOrderSub.setCancelOrderID(cancelOrder.getId());
        	cancelOrderSub.setCancelMoney(StringUtils.replaceSingleQuote(request.getParameter("cancelMoney"+i).toString()));
        	cancelOrderSub.setCancelType(request.getParameter("cancelType"+i));
        	cancelOrderSub.setSaleID(StringUtils.replaceSingleQuote(request.getParameter("saleId"+i).toString()));
        	cancelOrderSub.setUcode(request.getParameter("ucode"+i));
        	cancelOrderSub.setUserName(request.getParameter("userName"+i));
        	cancelOrderSub.setAddTime(request.getParameter("addTime"+i));
        	cancelOrderList.add(cancelOrderSub);
        	cancelOrderSubManager.save(cancelOrderSub);
        	}
        }
        
        // 处理kv_record 表中的业务主键和html字段
        SaveFormHtml(cancelOrder,cancelOrderList,formParameter.getBusinessKey());
        
        String sql = "from BusinessDetailEntity where  bpmProcessId=? ";
        BusinessDetailEntity businessDetailEntity = businessDetailManager.findUnique(sql, bpmProcessId);
        processParameters.put("businessDetailId",businessDetailEntity.getId());
        //根据 bpmProcessId  到 oa_ba_business_detail 表 找对应的明细ID
        //String areaId,String areaName,String companyId,String companyName,
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
        recordInfo.setApplyContent(cancelOrder.getCancelRemark().toString().replace("'", ""));
    	recordManager.save(recordInfo);
      }
    
    private void SaveFormHtml(CancelOrder cancelOrder,List<CancelOrderSub> cancelOrderList,String bussinessKey){
    	String detailHtml="<div><table  style=\"line-height:34px;\"  class=\"centerdiv\" cellpadding=\"0\" cellspacing=\"0\" >"
    			 +"<tr>"
    			 +"<td colspan=\"6\" align=\"center\"><h2>撤单登记表</h2></td>"
    			 +"</td>"
    	        +"</tr>"
    	        +"<tr>"
    	            +"<td class=\"f_td\" colspan=\"2\">"
    	               	 +"提交次数："+cancelOrder.getSubmitTimes()
    	             +"</td>"
    	             +"<td class=\"f_td\">"
    	               	 +"受理单编号"
    	            +"</td>"
    	            +"<td class=\"f_r_td\" colspan=\"3\">"
    	               	 +cancelOrder.getApplyCode()
    	    	 +"</td>"
    	        +"</tr>"
    			  +"<tr>"
    	    	    +"<td class=\"f_td\"><span id=\"tag_shopCode\">&nbsp;店编号</span>：</td>"
    	            +"<td class=\"f_r_td\">"+cancelOrder.getUcode()+"</td>"
    	            +"<td class=\"f_td\">店姓名：</td>"
    	            +"<td class=\"f_r_td\">"+cancelOrder.getShopName()+"</td>"
    	            +"<td class=\"f_td\">店电话：</td>"
    	            +"<td class=\"f_r_td\">"+cancelOrder.getShopMobile()+"</td>"
    	            +"</tr>"
    	            +"<tr>"
	    	        +"<td class=\"f_td\"><span id=\"tag_mobile\">&nbsp;来电电话</span>：</td>"
    	            +"<td class=\"f_r_td\">"+cancelOrder.getMobile().toString().replace("'", "")+"</td>"
    	            +"<td class=\"f_td\">撤单登记时间：</td>"
    	            +"<td class=\"f_r_td\">"+cancelOrder.getRegisterTime()+"</td>"
    	    	    +"<td class=\"f_td\">登记人</td>"
    	            +"<td class=\"f_r_td\">"+cancelOrder.getRegisterName()+"</td>"
    	    	 +"</tr>"
			    	+"<tr>"
				        +"<td class=\"f_td\">抄送：</td>"
				        +"<td class=\"f_r_td\" colspan=\"5\">{copyNames}</td>"
			        +"</tr>";
    	
    	         if(cancelOrderList!=null&&cancelOrderList.size()>0){
    	        	 int i=1;
    	        	 for(CancelOrderSub sub:cancelOrderList){
    	        		 detailHtml+="<tr>"
	    	        				 +"<td class=\"f_td\">撤单编号"+i+"</td>"
	    	        				 +"<td class=\"f_r_td\">"+sub.getUcode().toString().replace("'", "")+"</td>"
	    	        				 +"<td class=\"f_td\">撤单姓名</td>"
	    	        				 +"<td class=\"f_r_td\">"+sub.getUserName()+"</td>"
	    	        				 +"<td class=\"f_td\">编号加入日期</td>"
	    	        				 +"<td class=\"f_r_td\">"+sub.getAddTime()+"</td>"
    	        				 +"</tr>"
    	        				 +"<tr>"
	    	        				 +"<td class=\"f_td\">撤单类型</td>"
	    	        				 +"<td class=\"f_r_td\">"+sub.getCancelType()+"</td>"
	    	        				 +"<td class=\"f_td\">撤单金额</td>"
	    	        				 +"<td class=\"f_r_td\">"+sub.getCancelMoney().toString().replace("'", "")+"</td>"
	    	        				 +"<td class=\"f_td\">业绩单号</td>"
	    	        				 +"<td class=\"f_r_td\">"+sub.getSaleID().toString().replace("'", "")+"</td>"
    	        				 +"</tr>";
    	        		 i++;
    	        	 }
    	         }   
    	    	 
	    	 detailHtml+="<tr>"
    	        +"<td class=\"f_td\">是否核实</td>"
    	            +"<td class=\"f_r_td\">"+(cancelOrder.getIsChecked())+""
    	            +"</td>"
    	            +"<td class=\"f_td\">撤单备注</td>"
    	            +"<td class=\"f_r_td\" colspan=\"3\"><pre style=\"white-space: pre-wrap;word-wrap: break-word;\">"+cancelOrder.getCancelRemark().toString().replace("'", "")+"</pre></td>"
    	            +"</tr>"
    	        +"</table></div>";
    	
	    	RecordInfo recordInfo=recordManager.findUniqueBy("businessKey", bussinessKey);
        	if(recordInfo != null){
        		recordInfo.setPkId(cancelOrder.getId().toString());
        		recordInfo.setDetailHtml(detailHtml);
        	}
           	recordManager.save(recordInfo);
    }
    
    /**
     * 完成审批
     * @param request
     * @param redirectAttributes
     * @param processInstanceId
     * @param humanTaskId
     * @param files
     * @param iptdels
     * @throws Exception
     * @throws IOException
     */
    @Transactional(readOnly = false)
    public  void CompleteTaskCancelOrder(HttpServletRequest request,
			RedirectAttributes redirectAttributes, String processInstanceId,
			String humanTaskId) throws Exception {
		//获得当前登录人的ID和姓名
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
		    formParameter.setAction("重新调整申请");
		    
		    //用户可能会调整内容，要重新存进数据库中
		    CancelOrder cancelOrder = new CancelOrder();
		    cancelOrder.setApplyCode(m.getFirst("applyCode"));
		  	cancelOrder.setUserId(Long.parseLong(currentUserHolder.getUserId()));
		    cancelOrder.setProcessInstanceId(processInstanceId);
		    cancelOrder.setUcode(m.getFirst("ucode"));
		    cancelOrder.setShopName(m.getFirst("shopName"));
		    cancelOrder.setShopMobile(m.getFirst("shopMobile"));
		    cancelOrder.setMobile(m.getFirst("mobile"));
		    cancelOrder.setRegisterName(m.getFirst("registerName"));
		    cancelOrder.setRegisterTime(m.getFirst("registerTime"));
		    cancelOrder.setIsChecked(m.getFirst("isChecked"));
		    cancelOrder.setCancelRemark(m.getFirst("cancelRemark"));
		    cancelOrder.setProcessInstanceId(processInstanceId);
		    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		    String str = sdf.format(new Date());
		    cancelOrder.setModifyTime(str);
		    cancelOrder.setId(Long.parseLong(m.getFirst("cancelOrderTotalID")));
		    cancelOrder.setCreateTime(m.getFirst("createTime"));
		    cancelOrder.setHidTotal( Integer.parseInt(request.getParameter("hidTotal")));
		    cancelOrder.setSubmitTimes( Integer.parseInt(m.getFirst("submitTimes"))+1);
		    
		    // zyl 处理 record 表中的提交次数 备注：由jdbc方式更改如下，更新属性保持不变 TODO sjx 18.11.27
			RecordInfo recordInfo = recordManager.findUniqueBy("businessKey", formParameter.getBusinessKey());
			recordInfo.setSubmitTimes(cancelOrder.getSubmitTimes());
			recordManager.save(recordInfo);
		    
		    cancelOrderManager.save(cancelOrder);
		    
		    Long id = new Long (0);
		    List<CancelOrderSub> c = cancelOrderSubManager.findBy("cancelOrderID",Long.parseLong(m.getFirst("cancelOrderTotalID")));
		    for (CancelOrderSub getInfo : c) {
		    	id = getInfo.getId();
		    	cancelOrderSubManager.removeById(id);
		    }
		    
		    List<CancelOrderSub> cancelOrderList=new ArrayList<CancelOrderSub>();
		   //页面有多少行撤单编号，取数字标识
		    Long num = Long.parseLong( request.getParameter("hidTotal"));
		    //将撤单编号、姓名、加入日期、撤单类型、金额、业绩单号存入子表
		    for(int i=1;i<=num;i++){
		    	String cm = request.getParameter("ucode"+i);
		    	
		    	if(cm!=null){
		    	CancelOrderSub cancelOrderSub = new CancelOrderSub();
		    	cancelOrderSub.setCancelOrderID(Long.parseLong(m.getFirst("cancelOrderTotalID")));
		    	cancelOrderSub.setCancelMoney(m.getFirst("cancelMoney"+i));
		    	cancelOrderSub.setCancelType(request.getParameter("cancelType"+i));
		    	cancelOrderSub.setSaleID(request.getParameter("saleId"+i));
		    	cancelOrderSub.setUcode(request.getParameter("ucode"+i));
		    	cancelOrderSub.setUserName(request.getParameter("userName"+i));
		    	cancelOrderSub.setAddTime(request.getParameter("addTime"+i));
		    	cancelOrderList.add(cancelOrderSub);
		    	cancelOrderSubManager.save(cancelOrderSub);
		    }
		    	//lilei 处理存储detailHtml
		    	SaveFormHtml(cancelOrder,cancelOrderList,formParameter.getBusinessKey());
		    } 
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
        formParameter.setBusinessKey(multipartHandler.getMultiValueMap()
                .getFirst("businessKey"));
        
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
    public void setCurrentUserHolder(CurrentUserHolder currentUserHolder) {
        this.currentUserHolder = currentUserHolder;
    }
    
    @Resource
    public void setTenantHolder(TenantHolder tenantHolder) {
        this.tenantHolder = tenantHolder;
    }
    
    @Resource
    public void setCancelOrderManager(CancelOrderManager cancelOrderManager) {
        this.cancelOrderManager = cancelOrderManager;
    }
    @Resource
    public void setCancelOrderSubManager(CancelOrderSubManager cancelOrderSubManager) {
        this.cancelOrderSubManager = cancelOrderSubManager;
    }
   
    @Resource
    public void setBusinessDetailManager(BusinessDetailManager businessDetailManager) {
        this.businessDetailManager = businessDetailManager;
    }
    @Resource
    public void setMsgInfoManager(MsgInfoManager msgInfoManager) {
    	this.msgInfoManager = msgInfoManager;
    }
}
