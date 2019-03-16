package com.mossle.operation.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.CopyUtils;
import org.apache.commons.lang3.StringUtils;
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
import com.mossle.api.tenant.TenantHolder;
import com.mossle.base.persistence.domain.BusinessDetailEntity;
import com.mossle.base.persistence.manager.BusinessDetailManager;
import com.mossle.bpm.rs.BpmResource;
import com.mossle.common.utils.FileUploadAPI;
import com.mossle.core.MultipartHandler;
import com.mossle.core.auth.CurrentUserHolder;
import com.mossle.core.id.IdGenerator;
import com.mossle.humantask.persistence.domain.TaskInfo;
import com.mossle.humantask.persistence.manager.TaskInfoManager;
import com.mossle.keyvalue.persistence.domain.RecordInfo;
import com.mossle.keyvalue.persistence.manager.RecordManager;
import com.mossle.msg.persistence.domain.MsgInfo;
import com.mossle.msg.persistence.manager.MsgInfoManager;
import com.mossle.operation.persistence.domain.Business;
import com.mossle.operation.persistence.manager.BusinessManager;
import com.mossle.util.StringUtil;

@Service
@Transactional(readOnly = true)
public class BusinessService {
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
    private BusinessManager businessManager;
    private FileUploadAPI fileUploadAPI;
    private BusinessDetailManager businessDetailManager;
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
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private CustomWorkService customWorkService;
    @Autowired
    private RecordManager recordManager;
    @Autowired
	private IdGenerator idGenerator;
    
    /**
     * 发起业务申请（分公司）流程.
     */
    @Transactional(readOnly = false)
    public String saveBusiness(HttpServletRequest request,Business businessDTO,String businessDetailId,String areaId,String businessKey, String userId, String tenantId,String branchOfficeId,
    		String bpmProcessId,  MultipartFile [] files) throws Exception {
    	
    	
    	Map<String, Object> processParameters = new HashMap<String, Object>();
    	FormParameter formParameter = this.doSaveRecord(request);
    	//取下一节点
    	if (StringUtils.isBlank(bpmProcessId) || !bpmProcessId.equals(formParameter.getBpmProcessId())) {
        	bpmProcessId = formParameter.getBpmProcessId();
        }
    	
    	ProcessDTO processDto = processConnector.findProcess(bpmProcessId);
    	String processDefinitionId = processDto.getProcessDefinitionId();
    	
    	Record record = keyValueConnector.findByCode(formParameter.getBusinessKey());
    	operationService.updateNextActivityId(bpmProcessId, formParameter, record, processDefinitionId);
       
        businessKey = formParameter.getBusinessKey();
        //通过页面直接得到岗位ID
        processParameters.put("positionId", request.getParameter("iptStartPositionId"));
        operationService.startProcessInstance(userId, businessKey, processDefinitionId, processParameters, record);
        
        Business business = new Business();
        business.setId(businessDTO.getId());
        business.setApplyCode(businessDTO.getApplyCode());
        if(businessDTO.getTheme().length() > 100){
        	 business.setTheme(businessDTO.getTheme().substring(0,100).replace("'", ""));
        }else{
        	business.setTheme(businessDTO.getTheme().toString().replace("'", ""));
        }
        business.setCc(businessDTO.getCc());
        business.setCopyUserValue(businessDTO.getCopyUserValue());
        business.setBusinessType(businessDTO.getBusinessType());
        business.setBusinessDetail(businessDTO.getBusinessDetail());
        business.setBusinessLevel(businessDTO.getBusinessLevel());
        business.setInitiator(businessDTO.getInitiator());
        business.setArea(businessDTO.getArea());
        String branchOffice = "";
        if(businessDTO.getBranchOffice() == null){
        	branchOfficeId = "";
        	business.setBranchOffice("");
        }else{
            branchOffice = businessDTO.getBranchOffice();
        	business.setBranchOffice(businessDTO.getBranchOffice());
        }
        if(businessDTO.getApplyContent().length() > 5000){
        	business.setApplyContent(businessDTO.getApplyContent().substring(0, 5000).replace("'", ""));
        }else{
        	business.setApplyContent(businessDTO.getApplyContent().toString().replace("'", ""));
        }
        business.setSubmitTimes(String.valueOf(Integer.parseInt(businessDTO.getSubmitTimes())+1));
        
        business.setUserId(Long.parseLong(userId));
        business.setProcessInstanceId(record.getRef());
        businessManager.save(business);
        fileUploadAPI.uploadFile(files, tenantId, Long.toString(business.getId()), "OA/process");
        //获取是否大区表单  ckx
        String isArea = StringUtil.toString(request.getParameter("isArea"));
        // 处理kv_record 表中的业务主键和html字段
        SaveFormHtml(business,businessKey,isArea);
        BusinessDetailEntity businessDetailEntity = businessDetailManager.get(Long.parseLong(businessDetailId));
        
        //原jdbc更新record操作更改如下，更新的属性保持一致  TODO sjx 18.11.23
        RecordInfo recordInfo = recordManager.findUniqueBy("businessKey", formParameter.getBusinessKey());
        recordInfo.setBusinessTypeId(businessDetailEntity.getTypeId().toString());
        recordInfo.setBusinessTypeName(businessDetailEntity.getBusinessType());
        recordInfo.setBusinessDetailId(businessDetailEntity.getId().toString());
        recordInfo.setBusinessDetailName(businessDetailEntity.getBusiDetail());
        recordInfo.setAreaId(areaId);
        recordInfo.setCompanyId(branchOfficeId);
        recordInfo.setApplyContent(business.getApplyContent().toString().replace("'", ""));
        recordInfo.setTheme(business.getTheme().toString().replace("'", ""));
    	recordManager.save(recordInfo);
    	
    	//保存是否发起大区表单  ckx
    	if("1".equals(isArea)){
        	keyValueConnector.updateBySql(String.format("delete from kv_record_condition where conditionName = 'region' and conditionValue = 'region' and businessKey=%s", businessKey));
    		String strInsertSql="insert into kv_record_condition(id,businessKey,conditionName,conditionValue)"
    				+" values(%s,%s,'%s','%s')";
    		keyValueConnector.updateBySql(String.format(strInsertSql, 
					idGenerator.generateId(),
					businessKey,
					"region",
					"region"));
    	}else{
        	keyValueConnector.updateBySql(String.format("delete from kv_record_condition where conditionName = 'region' and conditionValue = 'region' and businessKey=%s", businessKey));
    	}
    	
    	// 清空抄送人
    	String sqlRecordUpdate = "update KV_PROP set value= '' where code = 'activityId' and record_id= '" + record.getCode() + "'";
    	keyValueConnector.updateBySql(sqlRecordUpdate);
    	//处理受理单编号
    	operationService.deleteApplyCode(businessDTO.getApplyCode());
    	return "";
    }
    
    // 审批
    @Transactional(readOnly = false)
    public void saveReBusiness(HttpServletRequest request,RedirectAttributes redirectAttributes,
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
        	// 驳回操作，清空抄送人
        	String sqlRecordUpdate = "update KV_PROP set value= '' where code = 'activityId' and record_id= '" + record.getCode() + "'";
        	keyValueConnector.updateBySql(sqlRecordUpdate);
		}
        if (f.equals("1")) {
        	processParameters.put("leaderComment", "同意");
        	formParameter.setAction("同意");
        	// 驳回操作，清空抄送人
        	String sqlRecordUpdate = "update KV_PROP set value= '' where code = 'activityId' and record_id= '" + record.getCode() + "'";
        	keyValueConnector.updateBySql(sqlRecordUpdate);
		}
        if (f.equals("2")) {
        	processParameters.put("leaderComment", "驳回");
        	formParameter.setAction("驳回");
        	// 驳回操作，清空抄送人
        	String sqlRecordUpdate = "update KV_PROP set value= '' where code = 'activityId' and record_id= '" + record.getCode() + "'";
        	keyValueConnector.updateBySql(sqlRecordUpdate);
        }
        if (f.equals("3")) {
        	operationService.updateNextActivityId("", formParameter, record, "");
        	//删除之前抄送人
        	String hql = "from TaskInfo where processInstanceId = ? and catalog = ?";
        	List<TaskInfo> taskList = taskInfoManager.find(hql, processInstanceId,"copy");
        	for(TaskInfo task : taskList){
        		MsgInfo msg = msgInfoManager.findUniqueBy("data", task.getId().toString());
        		if(msg != null){
        			
        			msgInfoManager.removeById(msg.getId());//重新申请操作时，将抄送人对应的消息删除 2018-01-19
        			//jdbcTemplate.update("delete from msg_info where id = '"+msg.getId()+"'");
        		}
        		
        		taskInfoManager.removeById(task.getId());
        		//jdbcTemplate.update("delete from task_info where id = '"+task.getId()+"'");
        		//ckx 删除岗位关联表
        		jdbcTemplate.update("delete from task_info_approve_position where task_id = '"+task.getId()+"'");
        		
        	}
        	//删除实体表的抄送人
        	//jdbcTemplate.update("update entity_business set cc = '' ,copy_user_value = ''  where process_instance_id = '"+processInstanceId+"'");
        	//CKX add 2018/9/10
        	/*if(!m.getFirst("cc").isEmpty()){
   		        customWorkService.copySave(processInstanceId,m.getFirst("copyUserValue"), m.getFirst("cc"),  m.getFirst("theme"), userId);
   		     }*/
        	
        	processParameters.put("leaderComment", "调整申请");
        	formParameter.setAction("重新申请");
        	//调整申请内容，重写入表
        	Business business = new Business();
        	business.setId(Long.parseLong(m.getFirst("businessId")));
        	if(m.getFirst("theme").length() > 100){
        		business.setTheme(m.getFirst("theme").substring(0, 100));
        	}else{
        		business.setTheme(m.getFirst("theme"));
        	}
        	
        	//ckx 处理抄送人
        	String cc = m.getFirst("cc");
        	String copyUserValue = m.getFirst("copyUserValue");
        	String copyIds = "";
        	String copyNames = "";
        	if(StringUtils.isNotBlank(copyUserValue)){
            		if(copyUserValue.contains("岗位:")){
                		String[] splitCopyId = copyUserValue.split(",");
                		String[] splitCopyName = cc.split(",");
                		for (int i = 0; i < splitCopyId.length; i++) {
                			String strCopyId = splitCopyId[i];
                        	String strCopyName = splitCopyName[i];
                        	if(strCopyId.contains("岗位:")){
                				//岗位查询人员
                        		strCopyId = strCopyId.replaceAll("岗位:", "");
                	    		
                        		//查询岗位上一级
                        		/*Map<String, Object> queryCopyMap = jdbcTemplate.queryForMap("select * from party_entity e LEFT JOIN party_struct s ON e.ID = s.PARENT_ENTITY_ID where s.CHILD_ENTITY_ID = '"+strCopyId+"';");
                	    		String strCopyNameS = StringUtil.toString(queryCopyMap.get("NAME"));*/
                        		String lastPost = customWorkService.getLastPost(strCopyId);
                        		Map<String, Object> queryCopyPostMap = null;
                				try {
                    	    		queryCopyPostMap = jdbcTemplate.queryForMap("select e.NAME from party_entity e where e.ID = '"+strCopyId+"'");
                				} catch (Exception e) {
                				}
                	    		String strCopyPostName = StringUtil.toString(queryCopyPostMap.get("NAME") == null ? "" : queryCopyPostMap.get("NAME"));
                	    		copyIds += strCopyId+",";
                				copyNames += lastPost+strCopyPostName+",";
                        		
                			}else{
                				copyIds += strCopyId+",";
                				copyNames += strCopyName+",";
                			}
            			}
                		
                	}else{
                		copyIds += copyUserValue+",";
        				copyNames += cc+",";
                	}
            	
            	if(StringUtils.isNotBlank(copyIds)){
            		String str = copyIds.substring(copyIds.length()-1,copyIds.length() );
            		if(",".equals(str)){
            			copyIds = copyIds.substring(0, copyIds.length()-1);
            		}
            		String strName = copyNames.substring(copyNames.length()-1,copyNames.length() );
            		if(",".equals(strName)){
            			copyNames = copyNames.substring(0, copyNames.length()-1);
            		}
            		//businessDTO.setCopyUserValue(copyIds);
            		business.setCc(copyNames);
            	}
        	}else{
        		business.setCc("");//m.getFirst("cc")
        	}
        	
        	
        	business.setCopyUserValue(m.getFirst("copyUserValue"));//m.getFirst("copyUserValue")
        	business.setBusinessType(m.getFirst("businessType"));
        	business.setBusinessDetail(m.getFirst("businessDetail"));
        	business.setBusinessLevel(m.getFirst("businessLevel"));
        	business.setInitiator(m.getFirst("initiator"));
        	business.setArea(m.getFirst("area"));
        	business.setBranchOffice(m.getFirst("branchOffice"));
        	if(m.getFirst("applyContent").length() > 5000){
        		business.setApplyContent(m.getFirst("applyContent").substring(0, 5000));
        	}else{
        		business.setApplyContent(m.getFirst("applyContent"));
        	}
        	business.setSubmitTimes(m.getFirst("submitTimes"));
        	business.setApplyCode(m.getFirst("applyCode"));
        	business.setUserId(Long.parseLong(userId));
        	business.setProcessInstanceId(m.getFirst("processInstanceId"));
        	String tenantId = tenantHolder.getTenantId();
        	
        	// zyl 处理 record 表中的提交次数 备注：由jdbc方式更改如下，更新属性保持不变 TODO sjx 18.11.27
	        RecordInfo recordInfo = recordManager.findUniqueBy("businessKey", formParameter.getBusinessKey());
	        recordInfo.setSubmitTimes(Integer.parseInt(business.getSubmitTimes()));
	        recordInfo.setTheme(business.getTheme());
	        recordManager.save(recordInfo);

			
			if (iptdels != null && !iptdels.equals("")) {
        		fileUploadAPI.uploadFileDel(iptdels,m.getFirst("businessId"));
	        }

	       fileUploadAPI.uploadFile(files, tenantId,m.getFirst("businessId"), "OA/process");
	       businessManager.save(business);
	       
	       //获取是否大区表单  ckx
	       String isArea = StringUtil.toString(request.getParameter("isArea"));
	       //lilei 处理存储detailHtml
	       this.SaveFormHtml(business, formParameter.getBusinessKey(),isArea);
            
        }
        if (f.equals("4")) {
        	processParameters.put("leaderComment", "撤销申请");
        	formParameter.setAction("撤销申请");
        }
	      
        operationService.completeTask(humanTaskId, userId,
                formParameter, processParameters, record,
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
                
        //if (StringUtils.isBlank(multipartHandler.getMultiValueMap().getFirst("bpmProcessId"))) {
        	String busDetailId = multipartHandler.getMultiValueMap().getFirst("busDetail");   // 业务细分ID
        	if(!StringUtils.isBlank(busDetailId)){
	        	BusinessDetailEntity businessDetailEntity = businessDetailManager.findUniqueBy("id", Long.parseLong(busDetailId));
	        	if (businessDetailEntity != null) {
	        		String bpmProcessId = businessDetailEntity.getBpmProcessId();
	        		formParameter.setBpmProcessId(bpmProcessId);
	        	}
        	}
       /* }else{
    		formParameter.setBpmProcessId(multipartHandler.getMultiValueMap().getFirst("bpmProcessId"));
    	}*/

        formParameter.setHumanTaskId(multipartHandler.getMultiValueMap().getFirst("humanTaskId"));
        formParameter.setComment(multipartHandler.getMultiValueMap().getFirst("comment"));

        System.out.println("====== businessKey:" + formParameter.getBusinessKey() + ";busDetails:" + busDetailId + ";bpmProcessId:" + formParameter.getBpmProcessId());
        
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
    public void SaveFormHtml(Business business,String bussinessKey,String isArea){
    	String branchOffice = business.getBranchOffice();
    	
    	String detailHtml="<table  style=\"line-height:34px;\"  class=\"centerdiv\" cellpadding=\"0\" cellspacing=\"0\" >"
    			 +"<tr>";
    			if("1".equals(isArea)){
    				//根据细分获取标题
    				String title = "业务申请单";
    				Record findByRef = keyValueConnector.findByRef(business.getProcessInstanceId());
    	        	BusinessDetailEntity businessDetailEntity=businessDetailManager.get(Long.parseLong(findByRef.getBusinessDetailId()));
    	        	if(null != businessDetailEntity){
    	        		if(StringUtils.isNotBlank(businessDetailEntity.getTitle())){
    	        			title = businessDetailEntity.getTitle();
        	        	}
    	        	}
    				detailHtml +="<td colspan=\"4\" align=\"center\"><h2>"+title+"</h2></td>";
    			}else{
    				if(branchOffice.contains("项目部")){
    					detailHtml +="<td colspan=\"4\" align=\"center\"><h2>启明项目申请单</h2></td>";
    				}else{
    					detailHtml +="<td colspan=\"4\" align=\"center\"><h2>业务申请单</h2></td>";
    				}
    			}
				
				detailHtml +="</td>"
    	        +"</tr>"
    	        +"<tr>"
    	            +"<td class=\"f_td\" colspan=\"4\" style=\"padding-right:20px;text-align:right;\"><b>提交次数："
    	               	 +business.getSubmitTimes()+"&nbsp;&nbsp;受理单编号："
    	               	 +business.getApplyCode()
	               	+"</b></td>"
           	   +"</tr>"
           	   +"<tr>"
		            +"<td class=\"f_td\">主题：</td>"
		            +"<td class=\"f_r_td\" colspan=\"3\">"
		               	 +business.getTheme().toString().replace("'", "")
		           	+"</td>"
		       +"</tr>"
           	   +"<tr>"
		            +"<td class=\"f_td\">抄送：</td>"
		            +"<td class=\"f_r_td\" colspan=\"3\">"
		               	 /*+business.getCc()*/
		            +"{copyNames}"
		           	+"</td>"
		       +"</tr>"
		       +"<tr>"
		           	+"<td class=\"f_td\">申请业务类型：</td>"
		            +"<td class=\"f_r_td\">"
		               	 +business.getBusinessType()
		           	+"</td>"
		           	+"<td class=\"f_td\">业务细分：</td>"
		            +"<td class=\"f_r_td\">"
		               	 +business.getBusinessDetail()
		           	+"</td>"
		      +"</tr>"
	          +"<tr>"     
		           	+"<td class=\"f_td\">业务级别：</td>"
		            +"<td class=\"f_r_td\">"
		               	 +business.getBusinessLevel()
		           	+"</td>"
		           	+"<td class=\"f_td\">发起人：</td>"
		            +"<td class=\"f_r_td\">"
		               	 +business.getInitiator()
		           	+"</td>"
		   	  +"</tr>";
			if(StringUtils.isNotBlank(branchOffice) && !"全部".equals(branchOffice)){
				if(branchOffice.contains("项目部")){
					detailHtml +="<tr>"     
							+"<td class=\"f_td\">属地区域：</td>"
							+"<td class=\"f_r_td\">"
							+business.getArea()
							+"</td>"
							+"<td class=\"f_td\">属地项目部：</td>"
							+"<td class=\"f_r_td\">"
							+business.getBranchOffice()
							+"</td>"
							+"</tr>";
				}
			}else{
				if("1".equals(isArea)){
					detailHtml +="<tr>"     
				           	+"<td class=\"f_td\">大区：</td>"
				            +"<td class=\"f_r_td\" colspan=\"3\">"
				               	 +business.getArea()
				           	+"</td>"
				   	  +"</tr>";
				}else{
					detailHtml +="<tr>"     
				           	+"<td class=\"f_td\">大区：</td>"
				            +"<td class=\"f_r_td\">"
				               	 +business.getArea()
				           	+"</td>"
				           	+"<td class=\"f_td\">分公司：</td>"
				            +"<td class=\"f_r_td\">"
				               	 +business.getBranchOffice()
				           	+"</td>"
				   	  +"</tr>";
				}
			}
		   	  
		   	detailHtml +="<tr>"
			        +"<td class=\"f_td\">申请内容</td>"
    	            +"<td class=\"f_td\" style=\"text-align:left;\" colspan=\"3\" >"
    	            + "<pre style=\"white-space: pre-wrap;word-wrap: break-word;\">"+business.getApplyContent().toString().replace("'", "")+"</pre></td>"
    	     +"</tr>"
	         +"</table>";
    	
		   	RecordInfo recordInfo=recordManager.findUniqueBy("businessKey", bussinessKey);
	    	if(recordInfo != null){
	    		recordInfo.setPkId(business.getId().toString());
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
    public void setBusinessManager(BusinessManager businessManager) {
        this.businessManager = businessManager;
    }
    @Resource
    public void setFileUploadAPI(FileUploadAPI fileUploadAPI) {
    	this.fileUploadAPI = fileUploadAPI;
    }
    @Resource
    public void setBusinessDetailManager(BusinessDetailManager businessDetailManager) {
        this.businessDetailManager = businessDetailManager;
    }
   
   
}
