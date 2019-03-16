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

import org.codehaus.janino.Java.NewAnonymousClassInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.google.common.base.Joiner;
import com.graphbuilder.math.func.AtanFunction;
import com.mossle.api.dict.DictConnector;
import com.mossle.api.humantask.HumanTaskConnector;
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
import com.mossle.keyvalue.persistence.domain.RecordInfo;
import com.mossle.keyvalue.persistence.manager.RecordManager;
import com.mossle.msg.persistence.manager.MsgInfoManager;
import com.mossle.operation.persistence.domain.Apply;
import com.mossle.operation.persistence.domain.ApplyDTO;
import com.mossle.operation.persistence.manager.ApplyManager;
import com.mysql.fabric.xmlrpc.base.Array;

@Service
@Transactional(readOnly = true)
public class ApplyService {
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
    private FileUploadAPI fileUploadAPI;
    private TenantHolder tenantHolder;
    private CurrentUserHolder currentUserHolder;
    private DictConnector dictConnector;
    private ApplyManager applyManager;
    private MultipartResolver multipartResolver;
    private BusinessDetailManager businessDetailManager;
    private MsgInfoManager msgInfoManager;
    @Resource
    private RecordManager recordManager;
    @Resource
    private HumanTaskConnector humanTaskConnector;
    
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
    public void saveApplty(HttpServletRequest request, ApplyDTO applyDTO,
    		String areaId,String areaName,String companyId,String companyName,
			String bpmProcessId, MultipartFile[] files) throws Exception,
			IOException {
		
		String businessKey;
		Map<String, Object> processParameters = new HashMap<String, Object>();
        
        String userId = currentUserHolder.getUserId();
        
        FormParameter formParameter = this.doSaveRecord(request);
        
        if (StringUtils.isBlank(bpmProcessId)) {
        	bpmProcessId = formParameter.getBpmProcessId();
        }
        
        ProcessDTO processDto = processConnector.findProcess(bpmProcessId);
        String processDefinitionId = processDto.getProcessDefinitionId();
       
        Record record = keyValueConnector.findByCode(formParameter.getBusinessKey());
        
        businessKey = formParameter.getBusinessKey();
        
        processParameters.put("positionId", request.getParameter("iptStartPositionId"));
        this.operationService.startProcessInstance(userId, businessKey,
                processDefinitionId, processParameters, record);
        
        Apply apply = new Apply();
        apply.setApplyCode(applyDTO.getApplyCode());
        apply.setUcode(applyDTO.getUcode());
        apply.setContent(StringUtils.replaceSingleQuote(applyDTO.getApplyContent()));
        apply.setUserId(Long.parseLong(userId));
        apply.setProcessInstanceId(record.getRef());
        String tenantId = tenantHolder.getTenantId();
        
        apply.setUserName(applyDTO.getUserName());
        apply.setWelfare(applyDTO.getWelfare());
        apply.setLevel(applyDTO.getLevel());
        //将所属体系翻译成汉字跟表单内容一起存起来
        List<DictInfo> dictList=dictConnector.findDictInfoListByType("OwnedSystem", "1");
        
        for (DictInfo dictInfo : dictList) {
        	if (dictInfo.getValue().equals(applyDTO.getSystem()) ){
        		apply.setSystem(dictInfo.getName());
        		break;
        	}
        }
        
        apply.setVarFather(applyDTO.getVarFather());
        apply.setVarRe(applyDTO.getVarRe());
        apply.setAddTime(applyDTO.getAddTime());
        apply.setBusinessType(applyDTO.getBusinessType());
        apply.setBusinessDetail(applyDTO.getBusinessDetail());
        apply.setMobile(applyDTO.getMobile());
        apply.setAddress(applyDTO.getAddress());
        apply.setBusinessLevel(applyDTO.getBusinessLevel());
        apply.setArea(areaName);
        apply.setBusinessStand1(applyDTO.getBusinessStand1());
        apply.setBusinessStand2(applyDTO.getBusinessStand2());
        apply.setTreeInfo(applyDTO.getTreeInfo());
        apply.setCreateTime(new Date());
        apply.setSubmitTimes(1);
        //修改手机号类别系统，多个用逗号隔开 add by lilei at 2019.01.30
        apply.setFileName(applyDTO.getFileName());
        
        applyManager.save(apply);
        
        // 处理kv_record 表中的业务主键和html字段
        SaveFormHtml(apply,record.getBusinessDetailId(),businessKey);

        //保存附件
        fileUploadAPI.uploadFile(files, tenantId, Long.toString(apply.getId()), "operation/commApply");
       
    	//原jdbc更新record操作更改如下，更新的属性保持一致  TODO sjx 18.11.23
        RecordInfo recordInfo = recordManager.findUniqueBy("businessKey", formParameter.getBusinessKey());
        recordInfo.setAreaId(areaId);
        recordInfo.setAreaName(areaName);
        recordInfo.setCompanyId(companyId);
        recordInfo.setCompanyName(companyName);
    	recordManager.save(recordInfo);
        //处理受理单编号
        operationService.deleteApplyCode(applyDTO.getApplyCode());
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
    public void saveEndApply(HttpServletRequest request,
			RedirectAttributes redirectAttributes, String processInstanceId,
			String humanTaskId, MultipartFile[] files, String iptdels)
			throws Exception, IOException {
    	
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
        	processParameters.put("money",50000);
        	formParameter.setAction("同意");
        }
        if (f.equals("2")) {
	        processParameters.put("leaderComment", "驳回");
	        formParameter.setAction("驳回");
	        processParameters.put("money",50000);
        }    
        
	     if (f.equals("3")) {
		    processParameters.put("leaderComment", "调整申请");
		    formParameter.setAction("重新调整申请");
		    
		    //用户可能会调整内容和附件，要重新存进数据库中
		    Apply apply = new Apply();
  			apply.setApplyCode(m.getFirst("applyCode"));
    	 	apply.setUcode(m.getFirst("ucode"));
        	apply.setContent(m.getFirst("applyContent"));
        	apply.setUserName(m.getFirst("userName"));
        	apply.setWelfare(m.getFirst("welfare"));
        	apply.setLevel(m.getFirst("level"));
        	apply.setSystem(m.getFirst("systemName"));
        	apply.setVarFather(m.getFirst("varFather"));
        	apply.setVarRe(m.getFirst("varRe"));
        	apply.setAddTime(m.getFirst("addTime"));
        	apply.setBusinessType(m.getFirst("busType"));
        	apply.setBusinessDetail(m.getFirst("busDetailName"));
        	apply.setMobile(m.getFirst("mobile"));
        	apply.setAddress(m.getFirst("address"));
        	apply.setBusinessLevel(m.getFirst("busLevel"));
        	apply.setArea(m.getFirst("area"));
        	apply.setBusinessStand1(m.getFirst("businessStand1"));
        	apply.setBusinessStand2(m.getFirst("businessStand2"));
        	apply.setTreeInfo(m.getFirst("treeInfos"));
	        apply.setModifyTime(new Date());
	        apply.setUserId(Long.parseLong(currentUserHolder.getUserId()));
	        apply.setProcessInstanceId(processInstanceId);
	        apply.setId(Long.parseLong(m.getFirst("applyID")));
	        List<String> strList=m.get("fileName");
	        if(strList!=null){
	        	apply.setFileName(Joiner.on(",").join(strList));
	        }
	        else{
	        	apply.setFileName("");
	        }
	        //SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
	        String tenantId = tenantHolder.getTenantId();
	        apply.setSubmitTimes( Integer.parseInt(m.getFirst("submitTimes"))+1);
		        
		    //保存附件
	        if (iptdels != null && !iptdels.equals("")) {
	            fileUploadAPI.uploadFileDel(iptdels,m.getFirst("applyID"));
	        }
        	fileUploadAPI.uploadFile(files, tenantId, m.getFirst("applyID"), "operation/commApply");
		        
        	// zyl 处理 record 表中的提交次数 备注：由jdbc方式更改如下，更新属性保持不变 TODO sjx 18.11.27
			RecordInfo recordInfo = recordManager.findUniqueBy("businessKey", formParameter.getBusinessKey());
			recordInfo.setSubmitTimes(apply.getSubmitTimes());
			recordInfo.setSystemId(request.getParameter("systemID"));
			recordInfo.setSystemName(m.getFirst("systemName"));
			recordManager.save(recordInfo);
			
			applyManager.save(apply);
			
			//lilei 重新调整detailhtml
			SaveFormHtml(apply,recordInfo.getBusinessDetailId(),formParameter.getBusinessKey());
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
    
    public void SaveFormHtml(Apply apply,String detailId,String bussinessKey){
    	//SimpleDateFormat format =   new SimpleDateFormat("yyyy-MM-dd");
    	String detailHtml="<table  style=\"line-height:34px;\"  class=\"centerdiv\" cellpadding=\"0\" cellspacing=\"0\" >"
    			 +"<tr>"
    			 +"<td colspan=\"8\" align=\"center\"><h2>业务受理申请单</h2></td>"
    			 +"</td>"
    	        +"</tr>"
    	        +"<tr>"
    	            +"<td class=\"f_td\" colspan=\"2\">提交次数：</td>"
    	            +"<td class=\"f_r_td\" colspan=\"2\">"
    	               	 +apply.getSubmitTimes()
	               	+"</td>"
	               	+"<td class=\"f_td\" colspan=\"2\">受理单编号：</td>"
    	            +"<td class=\"f_r_td\" colspan=\"2\">"
    	               	 +apply.getApplyCode()
	               	+"</td>"
           	   +"</tr>"
           	   +"<tr>"
		            +"<td class=\"f_td\">经销商编号：</td>"
		            +"<td class=\"f_r_td\">"
		               	 +apply.getUcode()
		           	+"</td>"
		           	+"<td class=\"f_td\">经销商姓名：</td>"
		            +"<td class=\"f_r_td\">"
		               	 +apply.getUserName()
		           	+"</td>"
		           	+"<td class=\"f_td\">福利级别：</td>"
		            +"<td class=\"f_r_td\">"
		               	 +apply.getWelfare()
		           	+"</td>"
		           	+"<td class=\"f_td\">级别：</td>"
		            +"<td class=\"f_r_td\">"
		               	 +apply.getLevel()
		           	+"</td>"
		   	  +"</tr>"
		   	  +"<tr>"
		            +"<td class=\"f_td\">所属体系：</td>"
		            +"<td class=\"f_r_td\">"
		               	 +apply.getSystem()
		           	+"</td>"
		           	+"<td class=\"f_td\">销售人：</td>"
		            +"<td class=\"f_r_td\">"
		               	 +apply.getVarFather()
		           	+"</td>"
		           	+"<td class=\"f_td\">服务人：</td>"
		            +"<td class=\"f_r_td\">"
		               	 +apply.getVarRe()
		           	+"</td>"
		           	+"<td class=\"f_td\">注册时间：</td>"
		            +"<td class=\"f_r_td\">"
		               	 +StrToDateFormat(apply.getAddTime())
		           	+"</td>"
		   	  +"</tr>"
		   	  +"<tr>"
	            +"<td class=\"f_td\">联系电话：</td>"
	            +"<td class=\"f_r_td\">"+apply.getMobile()+"</td>"
	            +"<td class=\"f_td\">联系地址：</td>"
	            +"<td class=\"f_r_td\"  colspan=\"5\">"+apply.getAddress()+"</td>"
              +"</tr>"
			  +"<tr>"
    	    	    +"<td class=\"f_td\">申请业务类型：</td>"
    	            +"<td class=\"f_r_td\">"+apply.getBusinessType()+"</td>"
    	            +"<td class=\"f_td\">业务细分：</td>"
    	            +"<td class=\"f_r_td\" colspan=\"5\">"+apply.getBusinessDetail()+"</td>"
    	      +"</tr>"
    	      
             +"<tr>"
	             	+"<td class=\"f_td\">业务级别：</td>"
		            +"<td class=\"f_r_td\">"+apply.getBusinessLevel()+"</td>"
		            +"<td class=\"f_td\">所属大区：</td>"
		            +"<td class=\"f_r_td\"  colspan=\"5\">"+apply.getArea()+"</td>"
    	     +"</tr>"
    		 +"<tr>"
	          		+"<td class=\"f_td\">抄送：</td>"
		            +"<td class=\"f_r_td\" colspan=\"7\">{copyNames}</td>"
	         +"</tr>";
    		 Map<String,Object> mapDetail=getEditPhoneDetailId();
		     if(mapDetail.containsKey("businessDetailId")){
		    	 if(mapDetail.get("businessDetailId")!=null){
		    		 String businessDetailId=mapDetail.get("businessDetailId").toString();
		    		 if(!StringUtils.isBlank(businessDetailId)){
		    			 if(businessDetailId.equals(detailId)){
		    				 List<Map<String,Object>> mapSystemList=(List<Map<String,Object>>)mapDetail.get("systemList");
		    				 detailHtml+="<tr>"
			    			          		+"<td class=\"f_td\">需修改系统：</td>";
				             detailHtml+="<td style=\"text-align:left;\" class=\"f_r_td\" colspan=\"7\">";
				             if(mapSystemList.size()>0){
				            	 for (Map<String, Object> mapSystem: mapSystemList) {
				            		 detailHtml+="&emsp;<input type=\"checkbox\" name=\"fileName\" disabled";
				            		 if(apply.getFileName()!=null&&apply.getFileName().contains(mapSystem.get("value").toString())){
				            			 detailHtml+=" checked ";
				            			 detailHtml+=" value=\""+mapSystem.get("value")+"\"/>&nbsp;<span style=\"color:red\">"+mapSystem.get("title")+"</span>";
				            		 }
				            		 else
				            			 detailHtml+=" value=\""+mapSystem.get("value")+"\"/>&nbsp;<span>"+mapSystem.get("title")+"</span>";
								 }
				             }
				             detailHtml+="</td>"
		    				            +"</tr>";
		    			 }
		    		 }
		    	 }
		     }
		     detailHtml+="<tr>"
			        +"<td class=\"f_td\"  colspan=\"8\">申请内容</td>"
		     +"</tr>"
    	     +"<tr>"
    	            +"<td class=\"f_td\"  colspan=\"8\" >"
    	            + "<pre style=\"white-space: pre-wrap;word-wrap: break-word;\">"+apply.getContent().toString().replace("'", "")+"</pre></td>"
    	    +"</tr>"
    	    +"<tr>"
    	           +"<td class=\"f_td\" colspan=\"3\">业务标准(现场办理) </td>"
    	           +"<td class=\"f_td\" colspan=\"3\">业务标准(非现场办理)</td>"
    	           +"<td class=\"f_td\" colspan=\"2\">点位信息</td>"
            +"</tr>"
            +"<tr>"
		           +"<td class=\"f_td\" colspan=\"3\">"
		           + "<pre style=\"white-space: pre-wrap;word-wrap: break-word;\">"+apply.getBusinessStand1()+"</pre></td>"
		           +"<td class=\"f_td\" colspan=\"3\"><pre style=\"white-space: pre-wrap;word-wrap: break-word;\">"+apply.getBusinessStand2()+"</pre></td>"
		           +"<td class=\"f_td\" colspan=\"2\"><pre style=\"white-space: pre-wrap;word-wrap: break-word;\">"+apply.getTreeInfo()+"</pre></td>"
            +"</tr>"
	        +"</table>";
    	
    	RecordInfo recordInfo=recordManager.findUniqueBy("businessKey", bussinessKey);
    	if(recordInfo != null){
    		recordInfo.setPkId(apply.getId().toString());
    		recordInfo.setDetailHtml(detailHtml);
    	}
       	recordManager.save(recordInfo);
    }
    
    /**
    * 字符串转换成日期
    * @param str
    * @return date
    */
    public String StrToDateFormat(String str) {
      
       SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
       Date date = null;
       try {
        date = format.parse(str);
       } catch (Exception e) {
    	   
       }
       return format.format(date);
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
            /*logger.debug("multiValueMap : {}",
                    multipartHandler.getMultiValueMap());
            logger.debug("multiFileMap : {}",
                    multipartHandler.getMultiFileMap());*/

            formParameter = this.buildFormParameter(multipartHandler);
            
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
    
    /**
     * 通过multipart请求构建formParameter.
     */
    public FormParameter buildFormParameter(MultipartHandler multipartHandler) {
    	
        FormParameter formParameter = new FormParameter();
        formParameter.setMultiValueMap(multipartHandler.getMultiValueMap());
        formParameter.setMultiFileMap(multipartHandler.getMultiFileMap());
        
        formParameter.setBusinessKey(multipartHandler.getMultiValueMap().getFirst("businessKey"));
        
        //if (StringUtils.isBlank(multipartHandler.getMultiValueMap().getFirst("bpmProcessId"))) {
        	
        	String busDetailId = multipartHandler.getMultiValueMap().getFirst("busDetails");   // 业务细分ID
        	if (StringUtils.isNotBlank(busDetailId)) {
	        	BusinessDetailEntity businessDetailEntity = businessDetailManager.findUniqueBy("id", Long.parseLong(busDetailId));
	        	if (businessDetailEntity != null) {
	        		String bpmProcessId = businessDetailEntity.getBpmProcessId();
	        		formParameter.setBpmProcessId(bpmProcessId);
	        	}
        	} 
        //}else {
        	//formParameter.setBpmProcessId(multipartHandler.getMultiValueMap().getFirst("bpmProcessId"));
        //}

        formParameter.setHumanTaskId(multipartHandler.getMultiValueMap().getFirst("humanTaskId"));
        formParameter.setComment(multipartHandler.getMultiValueMap().getFirst("comment"));
        
        System.out.println("====== businessKey:" + formParameter.getBusinessKey() + ";busDetails:" + busDetailId + ";bpmProcessId:" + formParameter.getBpmProcessId());
        
        return formParameter;
    }
    
    /**
     * 获取修改手机号的细分ID
     * 判断条件为2个：1.是否开启（字典isOpen=1开启，细分的值，businessDetailId!=''）
     * add by lilei at 2019.01.30
     * **/
    public Map<String,Object> getEditPhoneDetailId(){
    	Map<String,Object> mapReturn=new HashMap<String, Object>();
    	List<DictInfo> dictInfoList=dictConnector.findDictInfoListByType("editPhoneBusinessDetaiId");
    	String isOpen="0";
    	String businessDetailId="";
    	List<Map<String,Object>> mapList=new ArrayList<Map<String,Object>>();
    	if(dictInfoList.size()>0){
    		for (DictInfo dictInfo : dictInfoList) {
    			if(dictInfo.getName().toLowerCase().equals("isopen")){
    				isOpen=dictInfo.getValue();
    			}
    			else if(dictInfo.getName().toLowerCase().equals("businessdetailid")){
    				businessDetailId=dictInfo.getValue();
    			}
    			else if(dictInfo.getName().toLowerCase().equals("editphonesystem")){
    				Map<String,Object> map=new HashMap<String, Object>();
    				String[] strNameValues=dictInfo.getValue().split("-");
    				map.put("title", strNameValues[0]);
    				map.put("value", strNameValues[1]);
    				mapList.add(map);
    			}
    		}
    		if(isOpen.equals("0")){
    			if(!StringUtils.isBlank(businessDetailId)){
    				businessDetailId="";
    			}
    		}
    	}
    	mapReturn.put("businessDetailId", businessDetailId);
    	mapReturn.put("systemList", mapList);
    	return mapReturn;
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
    public void setFileUploadAPI(FileUploadAPI fileUploadAPI) {
    	this.fileUploadAPI = fileUploadAPI;
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
    public void setDictConnector(DictConnector dictConnector){
    	this.dictConnector=dictConnector;
    }
    
    @Resource
    public void setApplyManager(ApplyManager applyManager) {
        this.applyManager = applyManager;
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
