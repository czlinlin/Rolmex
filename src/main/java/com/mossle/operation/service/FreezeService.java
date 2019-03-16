package com.mossle.operation.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mossle.api.form.FormDTO;
import com.mossle.api.humantask.HumanTaskDTO;
import com.mossle.api.keyvalue.FormParameter;
import com.mossle.api.keyvalue.KeyValueConnector;
import com.mossle.api.keyvalue.Record;
import com.mossle.api.process.ProcessConnector;
import com.mossle.api.process.ProcessDTO;
import com.mossle.api.tenant.TenantHolder;
import com.mossle.base.persistence.domain.BusinessDetailEntity;
import com.mossle.base.persistence.manager.BusinessDetailManager;
import com.mossle.bpm.rs.BpmResource;
import com.mossle.common.utils.FileUploadAPI;
import com.mossle.core.MultipartHandler;
import com.mossle.core.auth.CurrentUserHolder;
import com.mossle.dict.persistence.domain.DictInfo;
import com.mossle.dict.support.DictConnectorImpl;
import com.mossle.humantask.persistence.manager.TaskInfoManager;
import com.mossle.keyvalue.persistence.domain.RecordInfo;
import com.mossle.keyvalue.persistence.manager.RecordManager;
import com.mossle.msg.persistence.manager.MsgInfoManager;
import com.mossle.operation.persistence.domain.Freeze;
import com.mossle.operation.persistence.domain.FreezeDTO;
import com.mossle.operation.persistence.manager.FreezeManager;

@Service
@Transactional(readOnly = true)
public class FreezeService {
    private static Logger logger = LoggerFactory.getLogger(FreezeService.class);
    
    public static final String OPERATION_BUSINESS_KEY = "businessKey";
    public static final String OPERATION_TASK_ID = "taskId";
    public static final String OPERATION_BPM_PROCESS_ID = "bpmProcessId";
    public static final int STATUS_DRAFT_PROCESS = 0;
    public static final int STATUS_DRAFT_TASK = 1;
    public static final int STATUS_RUNNING = 2;
    private KeyValueConnector keyValueConnector;
    private ProcessConnector processConnector;

    private OperationService operationService;
    private FreezeManager freezeManager;
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
    private TaskInfoManager taskInfoManager;
    @Resource
    private BpmResource bpmResource;
    @Resource
    private MsgInfoManager msgInfoManager;
    @Resource
    private RecordManager recordManager;
    /**
     * 发起申请冻结/解冻流程.
     */
    @Transactional(readOnly = false)
    public String saveFreeze(HttpServletRequest request,FreezeDTO freezeDTO,String areaId,String areaName,String companyId,String companyName, String userId, String tenantId,String systemName,
           String bpmProcessId, String businessKey,
            MultipartFile [] files) throws Exception {
        
    	Map<String, Object> processParameters = new HashMap<String, Object>();
    	FormParameter formParameter = this.doSaveRecord(request);
		if (StringUtils.isBlank(bpmProcessId))
        	bpmProcessId = formParameter.getBpmProcessId();
		
        ProcessDTO processDto = processConnector.findProcess(bpmProcessId);
        String processDefinitionId = processDto.getProcessDefinitionId();
        Record record = keyValueConnector.findByCode(formParameter.getBusinessKey());
        businessKey = formParameter.getBusinessKey();
        
        //得到岗位，存储add by lilei at 2018-09-12
        processParameters.put("positionId", request.getParameter("iptStartPositionId"));
        operationService.startProcessInstance(userId, businessKey,
                processDefinitionId, processParameters, record);
        
        Freeze freeze = new Freeze();
        freeze.setId(freezeDTO.getId());
        freeze.setUcode(freezeDTO.getUcode());
        freeze.setName(freezeDTO.getName());
        freeze.setContact(freezeDTO.getContact());
        freeze.setSalesLevel(freezeDTO.getSalesLevel());
        freeze.setWelfareLevel(freezeDTO.getWelfareLevel());
        freeze.setActivationState(freezeDTO.getActivationState());
        List<DictInfo> dictList=dictConnectorImpl.findDictInfoListByType("OwnedSystem", "1");
        for (DictInfo dictInfo : dictList) {
        	if (dictInfo.getValue().equals(freezeDTO.getSystem()) ){
        		freeze.setSystem(dictInfo.getName());
        	}
        }
        freeze.setAboveBoard(freezeDTO.getAboveBoard().toString().replace("'", ""));
        freeze.setFrozenState(freezeDTO.getFrozenState());
        freeze.setArea(areaName);
        freeze.setDirector(freezeDTO.getDirector().toString().replace("'", ""));
        freeze.setDirectorContact(freezeDTO.getDirectorContact().toString().replace("'", ""));
        freeze.setBranchOffice(companyName);
        freeze.setIdNumber(freezeDTO.getIdNumber());
        freeze.setApplyMatter(freezeDTO.getApplyMatter());
        if(freezeDTO.getApplyContent().length() > 5000){
        	 freeze.setApplyContent(freezeDTO.getApplyContent().substring(0, 5000).replace("'", ""));
        }else{
        	 freeze.setApplyContent(freezeDTO.getApplyContent().toString().replace("'", ""));
        }
        
        freeze.setUserId(Long.parseLong(userId));
        freeze.setProcessInstanceId(record.getRef());
      
        freezeManager.save(freeze);
        fileUploadAPI.uploadFile(files, tenantId, Long.toString(freeze.getId()), "OA/process");
        
        String sql = "from BusinessDetailEntity where  bpmProcessId=? ";
        BusinessDetailEntity businessDetailEntity = businessDetailManager.findUnique(sql, bpmProcessId);
        processParameters.put("businessDetailId", businessDetailEntity.getId().toString());
        SaveFormHtml(freeze,businessKey,freezeDTO.getApplyCode());
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
        recordInfo.setSystemName(systemName);
        recordInfo.setApplyContent(freeze.getApplyContent().toString().replace("'", ""));
    	recordManager.save(recordInfo);
    	// 处理受理单编号
    	operationService.deleteApplyCode(freezeDTO.getApplyCode());
    	return "";
    }
    
    // 审批
    @Transactional(readOnly = false)
    public void saveReFreeze(HttpServletRequest request,RedirectAttributes redirectAttributes,
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
			// 调整申请内容，重写入表
			Freeze freeze = new Freeze();
			freeze.setUcode(m.getFirst("ucode"));
			freeze.setName(m.getFirst("name"));
			freeze.setContact(m.getFirst("contact"));
			freeze.setSalesLevel(m.getFirst("salesLevel"));
			freeze.setWelfareLevel(m.getFirst("welfareLevel"));
			freeze.setActivationState(m.getFirst("activationState"));
			freeze.setSystem(m.getFirst("systemName"));
			freeze.setAboveBoard(m.getFirst("aboveBoard"));
			freeze.setFrozenState(m.getFirst("frozenState"));
			freeze.setArea(m.getFirst("area"));
			freeze.setDirector(m.getFirst("director"));
			freeze.setDirectorContact(m.getFirst("directorContact"));
			freeze.setBranchOffice(m.getFirst("branchOffice"));
			freeze.setIdNumber(m.getFirst("idNumber"));
			freeze.setApplyMatter(m.getFirst("applyMatter"));
			if(m.getFirst("applyContent").length() > 5000){
				freeze.setApplyContent(m.getFirst("applyContent").substring(0, 5000));
			}else{
				freeze.setApplyContent(m.getFirst("applyContent"));
			}
			
			freeze.setUserId(Long.parseLong(userId));
			freeze.setProcessInstanceId(processInstanceId);
			freeze.setId(Long.parseLong(m.getFirst("freezeId")));
			String tenantId = tenantHolder.getTenantId();
			if (iptdels != null && !iptdels.equals("")) {
				fileUploadAPI.uploadFileDel(iptdels, m.getFirst("freezeId"));
			}

			fileUploadAPI.uploadFile(files, tenantId, m.getFirst("freezeId"), "OA/process");
			freezeManager.save(freeze);

			// 调整申请，提交次数需要+1。Bing 2017.11.18
			int submitTimes = record.getSubmitTimes();
			submitTimes++;
	    	//jdbc更新体系ID和体系名称操作更改如下，并修复次数未正常+1的问题 TODO sjx
	    	RecordInfo recordInfo = recordManager.findUniqueBy("businessKey", formParameter.getBusinessKey());
	        recordInfo.setSubmitTimes(submitTimes);
	        recordInfo.setSystemId(m.getFirst("system"));
	        recordInfo.setSystemName(freeze.getSystem());
	        recordManager.save(recordInfo);
			// lilei 处理存储detailHtml
			this.SaveFormHtml(freeze, formParameter.getBusinessKey(), record.getApplyCode());
		}
		if (f.equals("4")) {
			processParameters.put("leaderComment", "撤销申请");
			formParameter.setAction("撤销申请");
		}

		operationService.completeTask(humanTaskId, userId, formParameter, processParameters, record,
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

    public void SaveFormHtml(Freeze freeze,String bussinessKey,String applyCode){
    	String detailHtml="<table  style=\"line-height:34px;\"  class=\"centerdiv\" cellpadding=\"0\" cellspacing=\"0\" >"
    			 +"<tr>"
    			 +"<td colspan=\"6\" align=\"center\"><h2>冻结/解冻申请单(外事)</h2></td>"
    			 +"</td>"
    	        +"</tr>"
    	        +"<tr>"
    	            +"<td class=\"f_td\" colspan=\"6\" style=\"padding-right:20px;text-align:right;\"><b>"
    	               	 +"&nbsp;&nbsp;受理单编号："
    	               	 +applyCode
	               	+"</b></td>"
           	   +"</tr>"
           	   +"<tr>"
		            +"<td class=\"f_td\">编号：</td>"
		            +"<td class=\"f_r_td\">"
		               	 +freeze.getUcode()
		           	+"</td>"
		           	+"<td class=\"f_td\">姓名：</td>"
		            +"<td class=\"f_r_td\">"
		               	 +freeze.getName()
		           	+"</td>"
		           	+"<td class=\"f_td\">联系方式：</td>"
		            +"<td class=\"f_r_td\">"
		               	 +freeze.getContact()
		           	+"</td>"
		       +"</tr>"
		       +"<tr>"
		            +"<td class=\"f_td\">抄送：</td>"
		            +"<td class=\"f_r_td\" style=\"text-align:left;\" colspan=\"5\" >"
		            +"{copyNames}"
		           	+"</td>"
	           	+"</tr>"
		       +"<tr>"
		            +"<td class=\"f_td\">销售级别：</td>"
		            +"<td class=\"f_r_td\">"
		               	 +freeze.getSalesLevel()
		           	+"</td>"
		           	+"<td class=\"f_td\">福利级别：</td>"
		            +"<td class=\"f_r_td\">"
		               	 +freeze.getWelfareLevel()
		           	+"</td>"
		           	+"<td class=\"f_td\">激活状态：</td>"
		            +"<td class=\"f_r_td\">"
		               	 +freeze.getActivationState()
		           	+"</td>"
	          +"</tr>"
	          +"<tr>"
		            +"<td class=\"f_td\">所属体系：</td>"
		            +"<td class=\"f_r_td\">"
		               	 +freeze.getSystem()
		           	+"</td>"
		           	+"<td class=\"f_td\">上属董事：</td>"
		            +"<td class=\"f_r_td\">"
		               	 +freeze.getAboveBoard().toString().replace("'", "")
		           	+"</td>"
		           	+"<td class=\"f_td\">冻结状态：</td>"
		            +"<td class=\"f_r_td\">"
		               	 +freeze.getFrozenState()
		           	+"</td>"
	          +"</tr>"
	          +"<tr>"
		            +"<td class=\"f_td\">所属区域：</td>"
		            +"<td class=\"f_r_td\">"
		               	 +freeze.getArea()
		           	+"</td>"
		           	+"<td class=\"f_td\">董事姓名：</td>"
		            +"<td class=\"f_r_td\">"
		               	 +freeze.getDirector().toString().replace("'", "")
		           	+"</td>"
		           	+"<td class=\"f_td\">联系方式：</td>"
		            +"<td class=\"f_r_td\">"
		               	 +freeze.getDirectorContact().toString().replace("'", "")
		           	+"</td>"
	          +"</tr>"
	          +"<tr>"
		            +"<td class=\"f_td\">所属分公司：</td>"
		            +"<td class=\"f_r_td\">"
		               	 +freeze.getBranchOffice()
		           	+"</td>"
		           	+"<td class=\"f_td\">身份证号：</td>"
		            +"<td class=\"f_r_td\" colspan=\"3\">"
		               	 +freeze.getIdNumber()
		           	+"</td>"
	          +"</tr>"
	          +"<tr>"
			        +"<td class=\"f_td\">申请受理事项：</td>"
		            +"<td class=\"f_td\" style=\"text-align:left;\" colspan=\"5\" >"
			        +freeze.getApplyMatter()
			        +"</td>"
		      +"</tr>"
    	      +"<tr>"
			        +"<td class=\"f_td\">申请内容：</td>"
    	            +"<td class=\"f_td\" style=\"text-align:left;\" colspan=\"5\" >"
			        +"<pre style=\"white-space: pre-wrap;word-wrap: break-word;\">"+freeze.getApplyContent().toString().replace("'", "")+"</pre>"
			        +"</td>"
    	     +"</tr>"
	         +"</table>";
    	
    	RecordInfo recordInfo=recordManager.findUniqueBy("businessKey", bussinessKey);
    	if(recordInfo != null){
    		recordInfo.setPkId(freeze.getId().toString());
    		recordInfo.setDetailHtml(detailHtml);
    	}
       	recordManager.save(recordInfo);
    }

    /**
     * 发起流程.
     */
    /*public void startProcessInstance(String userId, String businessKey,
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
    public void setFreezeManager(FreezeManager freezeManager) {
        this.freezeManager = freezeManager;
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
}
