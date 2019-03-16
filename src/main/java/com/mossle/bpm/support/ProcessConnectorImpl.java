package com.mossle.bpm.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.DelegationState;
import org.activiti.engine.task.Task;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import com.mossle.api.dict.DictConnector;
import com.mossle.api.form.FormConnector;
import com.mossle.api.form.FormDTO;
import com.mossle.api.org.OrgConnector;
import com.mossle.api.process.ProcessConnector;
import com.mossle.api.process.ProcessDTO;
import com.mossle.api.user.UserConnector;
import com.mossle.bpm.cmd.FindFirstTaskFormCmd;
import com.mossle.bpm.persistence.domain.BpmConfForm;
import com.mossle.bpm.persistence.domain.BpmProcess;
import com.mossle.bpm.persistence.manager.BpmConfFormManager;
import com.mossle.bpm.persistence.manager.BpmProcessManager;
import com.mossle.core.page.Page;
import com.mossle.core.query.PropertyFilter;
import com.mossle.core.query.PropertyFilterUtils;
import com.mossle.humantask.persistence.domain.TaskInfo;
import com.mossle.humantask.persistence.domain.UnfinishProcessInstance;
import com.mossle.humantask.persistence.manager.TaskInfoManager;
import com.mossle.humantask.support.HumanTaskConnectorImpl;
import com.mossle.party.persistence.domain.PartyEntity;
import com.mossle.spi.humantask.TaskDefinitionConnector;
import com.mossle.spi.humantask.TaskUserDTO;
import com.mossle.spi.process.FirstTaskForm;

public class ProcessConnectorImpl implements ProcessConnector {
	
    private Logger logger = LoggerFactory.getLogger(ProcessConnectorImpl.class);
    private ProcessEngine processEngine;
    private BpmConfFormManager bpmConfFormManager;
    private BpmProcessManager bpmProcessManager;
    private UserConnector userConnector;
    private FormConnector formConnector;
    private TaskDefinitionConnector taskDefinitionConnector;
    private TaskInfoManager taskInfoManager;
    private JdbcTemplate jdbcTemplate;
    private DictConnector dictConnector;
    private OrgConnector orgConnector;
    
    /**
     * 发起流程.
     */
    public String startProcess(String userId, String businessKey,
            String processDefinitionId, Map<String, Object> processParameters) {
        // 先设置登录用户
        IdentityService identityService = processEngine.getIdentityService();
        identityService.setAuthenticatedUserId(userId);

        ProcessInstance processInstance = processEngine.getRuntimeService()
                .startProcessInstanceById(processDefinitionId, businessKey,
                        processParameters);

        /*
         * // {流程标题:title}-{发起人:startUser}-{发起时间:startTime} String processDefinitionName =
         * processEngine.getRepositoryService() .createProcessDefinitionQuery()
         * .processDefinitionId(processDefinitionId).singleResult() .getName(); String processInstanceName =
         * processDefinitionName + "-" + userConnector.findById(userId).getDisplayName() + "-" + new
         * SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());
         * processEngine.getRuntimeService().setProcessInstanceName( processInstance.getId(), processInstanceName);
         */
        return processInstance.getId();
    }

    /**
     * 获得流程配置.
     */
    public ProcessDTO findProcess(String processId) {
        if (processId == null) {
            logger.info("processId is null");

            return null;
        }

        ProcessDTO processDto = new ProcessDTO();
        BpmProcess bpmProcess = bpmProcessManager
                .get(Long.parseLong(processId));
        String processDefinitionId = bpmProcess.getBpmConfBase()
                .getProcessDefinitionId();
        String processDefinitionName = bpmProcess.getName();
        processDto.setProcessDefinitionId(processDefinitionId);
        processDto.setProcessDefinitionName(processDefinitionName);
        processDto.setConfigTask(Integer.valueOf(1).equals(
                bpmProcess.getUseTaskConf()));

        return processDto;
    }

    /**
     * 获得启动表单.
     * @param processDefinitionId  流程定义ID
     */
    public FormDTO findStartForm(String processDefinitionId) {
        ProcessDefinition processDefinition = processEngine
                .getRepositoryService().createProcessDefinitionQuery()
                .processDefinitionId(processDefinitionId).singleResult();
        FirstTaskForm firstTaskForm = processEngine.getManagementService()
                .executeCommand(new FindFirstTaskFormCmd(processDefinitionId));

        if ((!firstTaskForm.isExists())
                && (firstTaskForm.getActivityId() != null)) {
            // 再从数据库里找一遍配置
            com.mossle.spi.humantask.FormDTO humantaskFormDto = taskDefinitionConnector
                    .findForm(firstTaskForm.getActivityId(),
                            processDefinitionId);

            if (humantaskFormDto != null) {
                firstTaskForm.setFormKey(humantaskFormDto.getKey());
            }
        }

        if (!firstTaskForm.isExists()) {
            logger.info("cannot find startForm : {}", processDefinitionId);

            return new FormDTO();
        }

        if (!firstTaskForm.isTaskForm()) {
            logger.info("find startEventForm : {}", processDefinitionId);

            return this.findStartEventForm(firstTaskForm);
        }

        List<TaskUserDTO> taskUserDtos = taskDefinitionConnector.findTaskUsers(
                firstTaskForm.getActivityId(),
                firstTaskForm.getProcessDefinitionId());
        String assignee = firstTaskForm.getAssignee();
        logger.debug("assignee : {}", assignee);

        for (TaskUserDTO taskUserDto : taskUserDtos) {
            logger.debug("catalog : {}, user : {}", taskUserDto.getCatalog(),
                    taskUserDto.getValue());

            if ("assignee".equals(taskUserDto.getCatalog())) {
                assignee = taskUserDto.getValue();

                break;
            }
        }

        logger.debug("assignee : {}", assignee);

        boolean exists = assignee != null;

        if ((("${" + firstTaskForm.getInitiatorName() + "}").equals(assignee))
                || "常用语:流程发起人".equals(assignee)
                || ((assignee != null) && assignee.equals(Authentication
                        .getAuthenticatedUserId()))) {
            exists = true;
        } else {
            exists = false;
        }

        if (!exists) {
            logger.info("cannot find taskForm : {}, {}", processDefinitionId,
                    firstTaskForm.getActivityId());

            return new FormDTO();
        }

        com.mossle.spi.humantask.FormDTO taskFormDto = taskDefinitionConnector
                .findForm(firstTaskForm.getActivityId(),
                        firstTaskForm.getProcessDefinitionId());

        @SuppressWarnings("unchecked")
		List<BpmConfForm> bpmConfForms = bpmConfFormManager
                .find("from BpmConfForm where bpmConfNode.bpmConfBase.processDefinitionId=? and bpmConfNode.code=?",
                        firstTaskForm.getProcessDefinitionId(),
                        firstTaskForm.getActivityId());

        if (taskFormDto == null) {
            logger.info("cannot find bpmConfForm : {}, {}",
                    processDefinitionId, firstTaskForm.getActivityId());

            return new FormDTO();
        }

        FormDTO formDto = new FormDTO();
        formDto.setProcessDefinitionId(firstTaskForm.getProcessDefinitionId());
        formDto.setActivityId(firstTaskForm.getActivityId());
        formDto.setCode(taskFormDto.getKey());
        if(taskFormDto.getType().equals("external")){
        	//formDto.setCode(taskFormDto.getKey());
        	formDto.setRedirect(true);
        	formDto.setUrl(taskFormDto.getKey());
        	
        	if (StringUtils.isBlank(taskFormDto.getKey())) {
	        	if (bpmConfForms != null && bpmConfForms.size() >0) {
	        		BpmConfForm bpmConfForm = bpmConfForms.get(0);
	        		if (Integer.valueOf(1).equals(bpmConfForm.getType())) {
	                    formDto.setUrl(bpmConfForm.getValue());
	                }
	        	}
        	}
        	return formDto;
        }

        FormDTO contentFormDto = formConnector.findForm(taskFormDto.getKey(),
                processDefinition.getTenantId());

        if (contentFormDto == null) {
            logger.error("cannot find form : {}", formDto.getCode());

            return formDto;
        }

        
        formDto.setRedirect(contentFormDto.isRedirect());
        formDto.setUrl(contentFormDto.getUrl());
        formDto.setContent(contentFormDto.getContent());

        return formDto;
    }

    public FormDTO findStartEventForm(FirstTaskForm firstTaskForm) {
        ProcessDefinition processDefinition = processEngine
                .getRepositoryService().createProcessDefinitionQuery()
                .processDefinitionId(firstTaskForm.getProcessDefinitionId())
                .singleResult();
        List<BpmConfForm> bpmConfForms = bpmConfFormManager
                .find("from BpmConfForm where bpmConfNode.bpmConfBase.processDefinitionId=? and bpmConfNode.code=?",
                        firstTaskForm.getProcessDefinitionId(),
                        firstTaskForm.getActivityId());
        FormDTO formDto = new FormDTO();
        formDto.setProcessDefinitionId(firstTaskForm.getProcessDefinitionId());
        formDto.setActivityId(firstTaskForm.getActivityId());
        formDto.setCode(firstTaskForm.getFormKey());

        if (!bpmConfForms.isEmpty()) {
            BpmConfForm bpmConfForm = bpmConfForms.get(0);

            if (!Integer.valueOf(2).equals(bpmConfForm.getStatus())) {
                if (Integer.valueOf(1).equals(bpmConfForm.getType())) {
                    formDto.setRedirect(true);
                    formDto.setUrl(bpmConfForm.getValue());
                } else {
                    formDto.setCode(bpmConfForm.getValue());
                }
            }
        } else {
            logger.info("cannot find bpmConfForm : {}, {}",
                    firstTaskForm.getProcessDefinitionId(),
                    formDto.getActivityId());
        }

        FormDTO contentFormDto = formConnector.findForm(formDto.getCode(),
                processDefinition.getTenantId());

        if (contentFormDto == null) {
            logger.error("cannot find form : {}", formDto.getCode());

            return formDto;
        }

        formDto.setRedirect(contentFormDto.isRedirect());
        formDto.setUrl(contentFormDto.getUrl());
        formDto.setContent(contentFormDto.getContent());

        return formDto;
    }

    /**
     * 未结流程.
     */
	public Page findRunningProcessInstances(Page page, List<PropertyFilter> propertyFilters, String userId) {

        String sqlPagedQuerySelect = "select *,ti.COMPLETE_TIME"
                + " from (select hi.id_ as id,hi.proc_inst_id_ as proc_inst_id,"
                + "hi.business_key_ as business_key,hi.proc_def_id_ as proc_def_id,r.CREATE_TIME as start_time,hi.end_time_ as end_time,"
    	 		+ "hi.name_ as pro_name,hi.start_user_id_ as start_user_id,r.id as record_id,r.category,"
    	 		+ "r.`status`,r.`name` as record_name,r.theme,r.applyCode,r.ucode,r.audit_status pro_status,"
    	 		+ "r.businessTypeName,r.businessDetailName,r.submitTimes,r.businessTypeId,r.businessDetailId,r.url,'普通流程' as pro_flag"
    	 		+ " from act_hi_procinst hi"
    	 		+ " inner join kv_record r on hi.business_key_ = r.id"
    	 		+ " where hi.start_user_id_ = '" + userId + "' and hi.end_time_ is null"
    	 		+ " union all"
    	 		+ " select k.id,k.process_instance_id,k.business_key,'自定义申请',r.create_time,r.end_time,k.presentation_subject,r.user_id,r.id,"
    	 		+ "r.category,r.`status`,r.`name`,r.theme,r.applyCode,r.ucode,r.audit_status pro_status,"
    	 		+ "r.businessTypeName,r.businessDetailName,r.submitTimes,r.businessTypeId,r.businessDetailId,r.url,k.suspend_Status"
    	 		+ " from task_info k"
    	 		+ " inner join kv_record r on k.business_key = r.id and k.ASSIGNEE = r.user_id"
    	 		+ " where r.user_id ='" + userId + "' and (r.audit_status != '2' and r.audit_status != '3' and r.audit_status !='6') and k.suspend_Status = '自定义申请'  and k.catalog='start') nobpm"
    	 		+ " left join (SELECT MAX(ti.COMPLETE_TIME) as COMPLETE_TIME,ti.BUSINESS_KEY FROM task_info ti"
		        + " WHERE ti.`STATUS` = 'complete' and ti.CATALOG = 'normal' group by ti.BUSINESS_KEY ) ti on nobpm.business_key = ti.BUSINESS_KEY"
    	 		+ " where nobpm.start_user_id ='" + userId + "'";

         String sqlPagedQueryCount = "select count(*) from (select hi.id_ as id,hi.proc_inst_id_ as proc_inst_id,"
    	 		+ "hi.business_key_ as business_key,hi.proc_def_id_ as proc_def_id,r.CREATE_TIME as start_time,"
    	 		+ "hi.name_ as pro_name,hi.start_user_id_ as start_user_id,r.id as record_id,r.category,"
    	 		+ "r.`status`,r.`name` as record_name,r.theme,r.applyCode,r.ucode,r.audit_status pro_status,"
    	 		+ "r.businessTypeName,r.businessDetailName,r.submitTimes,r.businessTypeId,r.businessDetailId,r.url"
    	 		+ " from act_hi_procinst hi"
    	 		+ " inner join kv_record r on hi.business_key_ = r.id"
    	 		+ " where hi.start_user_id_ = '" + userId + "' and hi.end_time_ is null"
    	 		+ " union all"
    	 		+ " select k.id,k.process_instance_id,k.business_key,'自定义申请',r.create_time,k.presentation_subject,r.user_id,r.id,"
    	 		+ "r.category,r.`status`,r.`name`,r.theme,r.applyCode,r.ucode,r.audit_status pro_status,"
    	 		+ "r.businessTypeName,r.businessDetailName,r.submitTimes,r.businessTypeId,r.businessDetailId,r.url"
    	 		+ " from task_info k"
    	 		+ " inner join kv_record r on k.business_key = r.id and k.ASSIGNEE = r.user_id"
    	 		+ " where r.user_id ='" + userId + "' and (r.audit_status != '2' and r.audit_status != '3' and r.audit_status !='6') and r.businessTypeId = '9999'  and k.catalog='start') nobpm"
     	 		+ " where nobpm.start_user_id ='" + userId + "'";

         StringBuilder buff = new StringBuilder();
         List<Object> paramList = new ArrayList<Object>();
         boolean checkWhere = sqlPagedQuerySelect.toLowerCase().indexOf("where") == -1;
         PropertyFilterUtils.buildConfigurations(propertyFilters, buff,
                 paramList, checkWhere);
         /*
         logger.debug("propertyFilters : {}", propertyFilters);
         logger.debug("buff : {}", buff);
         logger.debug("paramList : {}", paramList);
         logger.debug("checkWhere : {}", checkWhere);
 		*/
         String sql = buff.toString();
         String countSql = sqlPagedQueryCount + " " + sql;
         String selectSql = sqlPagedQuerySelect + " " + sql + " order by nobpm.start_time Desc limit " 
                            + page.getStart() + "," + page.getPageSize();

         logger.debug("countSql : {}", countSql);
         logger.debug("selectSql : {}", selectSql);

         Object[] params = paramList.toArray();
         int totalCount = jdbcTemplate.queryForObject(countSql, Integer.class, params);
         List<Map<String, Object>> list = jdbcTemplate.queryForList(selectSql,  params);
         // List<PersonInfoDTO> personDtos = new ArrayList<PersonInfoDTO>();
         List<UnfinishProcessInstance> unfinishPros = new ArrayList<UnfinishProcessInstance>();
         
         for (Map<String, Object> map : list) {
        	 UnfinishProcessInstance uModelInstance=convertUnfinishProsDTO(map);
        	 
        	 if("".equals(convertString(map.get("theme")))) {
        		 sql = "select DISTINCT i.PRESENTATION_SUBJECT from task_info i where i.BUSINESS_KEY=? and i.CATALOG='start'";
        		 
        		 List<Map<String, Object>> taskList = jdbcTemplate.queryForList(sql, convertString(map.get("business_key")));
        		 if (taskList != null && !taskList.isEmpty()) {
        			 Map<String, Object> taskMap = taskList.get(0);
        			 uModelInstance.setTheme(convertString(taskMap.get("PRESENTATION_SUBJECT")));
        		 }
        	 }
        	 uModelInstance.setStatus(dictConnector.findDictNameByValue("RecordStatus", map.get("pro_status").toString()));
        	 
        	 
        	 unfinishPros.add(uModelInstance);
         }

         page.setTotalCount(totalCount);
         page.setResult(unfinishPros);

         return page;
    }
	
	/**
     * 未结流程（首页专用）.
     */
    public Page findRunningProcessInstancesToPortal(Page page, List<PropertyFilter> propertyFilters, String userId) {

        String sqlPagedQuerySelect = "select * from (select hi.id_ as id,hi.proc_inst_id_ as proc_inst_id,"
                + "hi.business_key_ as business_key,hi.proc_def_id_ as proc_def_id,r.CREATE_TIME as start_time,hi.end_time_ as end_time,"
    	 		+ "hi.name_ as pro_name,hi.start_user_id_ as start_user_id,r.id as record_id,r.category,"
    	 		+ "r.`status`,r.`name` as record_name,r.theme,r.applyCode,r.ucode,r.audit_status pro_status,"
    	 		+ "r.businessTypeName,r.businessDetailName,r.submitTimes,r.businessTypeId,r.businessDetailId,r.url,'普通流程' as pro_flag"
    	 		+ " from act_hi_procinst hi"
    	 		+ " inner join kv_record r on hi.business_key_ = r.id"
    	 		+ " where hi.start_user_id_ = '" + userId + "' and hi.end_time_ is null"
    	 		+ " union all"
    	 		+ " select k.id,k.process_instance_id,k.business_key,'自定义申请',r.create_time,r.end_time,k.presentation_subject,r.user_id,r.id,"
    	 		+ "r.category,r.`status`,r.`name`,r.theme,r.applyCode,r.ucode,r.audit_status pro_status,"
    	 		+ "r.businessTypeName,r.businessDetailName,r.submitTimes,r.businessTypeId,r.businessDetailId,r.url,k.suspend_Status"
    	 		+ " from task_info k"
    	 		+ " inner join kv_record r on k.business_key = r.id and k.ASSIGNEE = r.user_id"
    	 		+ " where r.user_id ='" + userId + "' and r.audit_status not in(2,3,6) and k.suspend_Status = '自定义申请'  and k.catalog='start') nobpm"
    	 		+ " where nobpm.start_user_id ='" + userId + "'";

         StringBuilder buff = new StringBuilder();
         List<Object> paramList = new ArrayList<Object>();
         boolean checkWhere = sqlPagedQuerySelect.toLowerCase().indexOf("where") == -1;
         PropertyFilterUtils.buildConfigurations(propertyFilters, buff,
                 paramList, checkWhere);

         String sql = buff.toString();
         String selectSql = sqlPagedQuerySelect + " " + sql + " order by nobpm.start_time Desc limit " 
                            + page.getStart() + "," + page.getPageSize();

         logger.debug("selectSql : {}", selectSql);

         Object[] params = paramList.toArray();
         int totalCount = 10;
         List<Map<String, Object>> list = jdbcTemplate.queryForList(selectSql,  params);
         // List<PersonInfoDTO> personDtos = new ArrayList<PersonInfoDTO>();
         List<UnfinishProcessInstance> unfinishPros = new ArrayList<UnfinishProcessInstance>();
         
         for (Map<String, Object> map : list) {
        	 UnfinishProcessInstance uModelInstance=convertUnfinishProsDTO(map);
        	 unfinishPros.add(uModelInstance);
         }

         page.setTotalCount(totalCount);
         page.setResult(unfinishPros);

         return page;
    }
	
    /**
     * 导出未结流程.
     */
    public Page exportRunningProcessInstances(Page page, List<PropertyFilter> propertyFilters, String userId) {
        String sqlPagedQuerySelect = "select * ,ti.COMPLETE_TIME "
                + " from (select hi.id_ as id,hi.proc_inst_id_ as proc_inst_id,"
                + "hi.business_key_ as business_key,hi.proc_def_id_ as proc_def_id,r.CREATE_TIME as start_time,hi.end_time_ as end_time,"
                + "hi.name_ as pro_name,hi.start_user_id_ as start_user_id,r.id as record_id,r.category,"
                + "r.`status`,r.`name` as record_name,r.theme,r.applyCode,r.ucode,r.audit_status pro_status,"
                + "r.businessTypeName,r.businessDetailName,r.submitTimes,r.businessTypeId,r.businessDetailId,r.url,'普通流程' as pro_flag,r.apply_content"
                + " from act_hi_procinst hi"
                + " inner join kv_record r on hi.business_key_ = r.id"
                + " where hi.start_user_id_ = '" + userId + "' and hi.end_time_ is null"
                + " union all"
                + " select k.id,k.process_instance_id,k.business_key,'自定义申请',r.create_time,r.end_time,k.presentation_subject,r.user_id,r.id,"
                + "r.category,r.`status`,r.`name`,r.theme,r.applyCode,r.ucode,r.audit_status pro_status,"
                + "r.businessTypeName,r.businessDetailName,r.submitTimes,r.businessTypeId,r.businessDetailId,r.url,k.suspend_Status,r.apply_content"
                + " from task_info k"
                + " inner join kv_record r on k.business_key = r.id and k.ASSIGNEE = r.user_id"
                + " where r.user_id ='" + userId + "' AND (r.audit_status != '2' AND r.audit_status != '3' AND r.audit_status != '6' ) and k.suspend_Status = '自定义申请'  and k.catalog='start') nobpm"
                + " left join (SELECT MAX(ti.COMPLETE_TIME) as COMPLETE_TIME,ti.BUSINESS_KEY FROM task_info ti"
		        + " WHERE ti.`STATUS` = 'complete' and ti.CATALOG = 'normal' group by ti.BUSINESS_KEY ) ti on nobpm.business_key = ti.BUSINESS_KEY"
                + " where nobpm.start_user_id ='" + userId + "'";

        StringBuilder buff = new StringBuilder();
        List<Object> paramList = new ArrayList<Object>();
        boolean checkWhere = sqlPagedQuerySelect.toLowerCase().indexOf("where") == -1;
        PropertyFilterUtils.buildConfigurations(propertyFilters, buff,
                paramList, checkWhere);
        String sql = buff.toString();
        String selectSql = sqlPagedQuerySelect + " " + sql + " order by nobpm.start_time Desc  ";
        Object[] params = paramList.toArray();
        List<Map<String, Object>> list = jdbcTemplate.queryForList(selectSql,  params);

        List<UnfinishProcessInstance> unfinishPros = new ArrayList<UnfinishProcessInstance>();

        for (Map<String, Object> map : list) {
            UnfinishProcessInstance uModelInstance=convertUnfinishProsDTO(map);
            uModelInstance.setStatus(dictConnector.findDictNameByValue("RecordStatus", map.get("pro_status").toString()));
            unfinishPros.add(uModelInstance);
        }
        page.setResult(unfinishPros);

        return page;
    }
    
    /**
     * 未结流程.
     */
    public Page findRunningProcessInstances(String userId, String tenantId,
            Page page) {
        HistoryService historyService = processEngine.getHistoryService();

        // TODO: 改成通过runtime表搜索，提高效率
        long count = historyService.createHistoricProcessInstanceQuery()
                .processInstanceTenantId(tenantId).startedBy(userId)
                .unfinished().count();
        HistoricProcessInstanceQuery query = historyService
                .createHistoricProcessInstanceQuery()
                .processInstanceTenantId(tenantId).startedBy(userId)
                .unfinished();
      

        if (page.getOrderBy() != null) {
            String orderBy = page.getOrderBy();

            if ("processInstanceStartTime".equals(orderBy)) {
                query.orderByProcessInstanceStartTime();
            }

            if (page.isAsc()) {
            	query.desc();
            } else {
            	query.asc();
            }
        } 

        List<HistoricProcessInstance> historicProcessInstances = query
                .listPage((int) 0, 100000);//page.getPageSize() 这个size不要了，先全部取出来，再跟自定义申请柔和到一起按时间排序，再分页显示出来，不让用户选择每页显示条数
        
        // 将取出的未结流程放入list，之后把自定义的未结流程也一起放入list，再传回页面，这样将这两部分就柔和到一起了
        List<UnfinishProcessInstance> list = new ArrayList<UnfinishProcessInstance>();
        for (HistoricProcessInstance vo : historicProcessInstances) {
        	UnfinishProcessInstance unfinishProcessInstance = new UnfinishProcessInstance();
        	unfinishProcessInstance.setId(vo.getId());
        	unfinishProcessInstance.setBusinessKey(vo.getBusinessKey());
        	unfinishProcessInstance.setStartTime(vo.getStartTime());
        	unfinishProcessInstance.setProcessDefinitionId(vo.getProcessDefinitionId());
        	unfinishProcessInstance.setName(vo.getName());
        	
        	list.add(unfinishProcessInstance);
        }
        
        //计算自定义的未结流程总数是多少
        String hql = "from TaskInfo where assignee=? and status ='active' and suspendStatus = '自定义申请' order by createTime desc";
        List<TaskInfo> t = taskInfoManager.find(hql, userId);;
        int customCount = t.size();
        //计算总行数：未结流程的加上自定义的行数
        count=count+customCount;
        
        
        	 hql = "from TaskInfo where assignee=? and status ='active' and suspendStatus = '自定义申请' ";
	        
	        List<TaskInfo> taskInfos = taskInfoManager.find(hql, userId);
	     
	        for (TaskInfo unProInstance : taskInfos) {
	        	 UnfinishProcessInstance unfinishProcessInstance = new UnfinishProcessInstance();
	         	 unfinishProcessInstance.setBusinessKey(Long.toString(unProInstance.getId()));
	         	 unfinishProcessInstance.setStartTime(unProInstance.getCreateTime());
	         	 unfinishProcessInstance.setProcessDefinitionId("自定义申请");
	         	 unfinishProcessInstance.setName(unProInstance.getPresentationSubject());
	         	 list.add(unfinishProcessInstance);
	         }
	     
	    SortClass sort = new SortClass();  
	    Collections.sort(list,sort);  
	    
	   //当前要取自定义申请，从第几条开始取
    	int currentStart= (page.getPageNo()-1)*10+1;
    	
    	//判断当前页是否是最后一页
    	int lastPageNo=0;//末页的页数
    	int lastCountNo=0;//末页的行数 
    	if(count%10>0){
    		lastPageNo =(int)count/10+1;
    		lastCountNo=(int)count%10;
    	}
    	
    	//取到第几条结束  若不是末页，一共取十条，若当前页就是末页，还剩几条取几条
    	int currentEnd = 0;
    	if(page.getPageNo()==lastPageNo){
    		 currentEnd =  currentStart+lastCountNo-1;
    	}else{
    		currentEnd = currentStart+9;
    	}
    	
    	
    	List<UnfinishProcessInstance> unfinishResult = new ArrayList<UnfinishProcessInstance>();
	    
    	for(int i= currentStart-1;i<currentEnd;i++){
    		unfinishResult.add(list.get(i));
    	}
       
	    
        page.setResult(unfinishResult);
        page.setTotalCount(count);

        return page;
    }

   
    /**
     * 未结流程.
     */
    public Long findRunningProcessInstances(String userId, String tenantId) {
    	
    	HistoryService historyService = processEngine.getHistoryService();
    	
    	long count = historyService.createHistoricProcessInstanceQuery()
                .processInstanceTenantId(tenantId).startedBy(userId)
                .unfinished().count();
    	return count;
    }
    
    
    /**
     * 已结流程.
     */
    public Page findCompletedProcessInstances(Page page, List<PropertyFilter> propertyFilters, String userId) {
    	

        
        //获取当前登录人的岗位id，若有多个岗位?
        StringBuffer postIDBuffer = new StringBuffer("");
        List<PartyEntity> postlist = orgConnector.getPostByUserId(userId);
		if (postlist != null && postlist.size() > 0) {
			for (PartyEntity party : postlist) {
				postIDBuffer.append(party.getId() + ",");
			}
			postIDBuffer.delete(postIDBuffer.length() - 1, postIDBuffer.length());
		}
        String 	postID = postIDBuffer.toString();
        if (StringUtils.isBlank(postID)) {
        	postID =userId;
        } else {
        	postID = postID + "," + userId;
        }
//    	String sqlPagedQuerySelect = "select *,(SELECT MAX( COMPLETE_TIME ) FROM `task_info` ti WHERE nobpm.BUSINESS_KEY = ti.BUSINESS_KEY AND `STATUS` = 'complete' AND CATALOG NOT IN ( 'start', 'copy' )) AS COMPLETE_TIME "
//    			+ "from (select hi.id_ as id,hi.proc_inst_id_ as proc_inst_id,"
//    	 		+ "hi.business_key_ as business_key,hi.proc_def_id_ as proc_def_id,r.CREATE_TIME as start_time,hi.end_time_ as end_time,"
//    	 		+ "hi.name_ as pro_name,hi.start_user_id_ as start_user_id,r.id as record_id,r.category,"
//    	 		+ "r.`status`,r.`name` as record_name,r.theme,r.applyCode,r.ucode,"
//    	 		+ "r.businessTypeName,r.businessDetailName,r.submitTimes,r.businessTypeId,r.businessDetailId,r.url,r.apply_content,'普通流程' as pro_flag"
//    	 		+ ",r.audit_status pro_status"
//    	 		+ " from act_hi_procinst hi"
//    	 		+ " inner join kv_record r on hi.business_key_ = r.id"
//    	 		+ " where hi.start_user_id_ ='" + userId + "' and hi.end_time_ is not null"
//    	 		+ " union all"
//    	 		+ " select k.id,k.process_instance_id,k.business_key,'自定义申请',r.create_time,r.end_time,k.presentation_subject,r.user_id,r.id,"
//    	 		+ "r.category,r.`status`,r.`name`,r.theme,r.applyCode,r.ucode,"
//    	 		+ "r.businessTypeName,r.businessDetailName,r.submitTimes,r.businessTypeId,r.businessDetailId,r.url,r.apply_content,k.suspend_Status"
//    	 		+ ",r.audit_status pro_status"
//    	 		+ " from task_info k"
//    	 		+ " inner join kv_record r on k.business_key = r.id and k.assignee = r.user_id"
//    	 		+ " where r.user_id = '" + userId + "' and r.end_time is not null and k.suspend_Status = '自定义申请'  and k.catalog='start'"
//    	 		+ ") nobpm"
//    	 		+ " where nobpm.start_user_id ='" + userId + "'";

    	
    	// zyl 屏蔽
    	/*String sqlPagedQuerySelect ="select nobpm.id,nobpm.BUSINESS_KEY,nobpm.proc_inst_id,nobpm.start_time,nobpm.end_time,nobpm.proc_def_id,nobpm.pro_name,nobpm.theme,nobpm.applyCode,nobpm.ucode,nobpm.businessTypeName,nobpm.businessDetailName,nobpm.businessTypeId,nobpm.businessDetailId,nobpm.url,nobpm.pro_flag,nobpm.apply_content,nobpm.submitTimes,nobpm.pro_status"
    	+ " ,(SELECT MAX( COMPLETE_TIME )FROM `task_info` ti JOIN task_info_approve_position tap ON ti.id = tap.task_id WHERE " 
    	+ " tap.position_id  in (" + postID + ") AND `STATUS` = 'complete' AND CATALOG NOT IN ( 'start', 'copy' )) AS COMPLETE_TIME" 
    	+ " from (select DISTINCT hi.id_ as id,tap.position_id,hi.proc_inst_id_ as proc_inst_id,hi.business_key_ as business_key,hi.proc_def_id_ as proc_def_id,r.CREATE_TIME as start_time,hi.end_time_ as end_time,hi.name_ as pro_name,hi.start_user_id_ as start_user_id,r.id as record_id,r.category,r.`status`,r.`name` as record_name,r.theme"
    	+ " ,r.applyCode,r.ucode,r.businessTypeName,r.businessDetailName,r.submitTimes,r.businessTypeId,r.businessDetailId,r.url,r.apply_content,'普通流程' as pro_flag,r.audit_status pro_status" 
    	+ " from act_hi_procinst hi inner join kv_record r on hi.business_key_ = r.id"
    	+ " join task_info_approve_position tap ON k.BUSINESS_KEY = tap.BUSINESS_KEY and k.ID = tap.task_id" 
    	+ " where tap.position_id in (" + postID + ") and hi.end_time_ is not null" 
    	+ " union all" 
    	+ " select DISTINCT k.id,tap.position_id,k.process_instance_id,k.business_key,'自定义申请',r.create_time,r.end_time,k.presentation_subject,r.user_id,r.id,r.category,r.`status`,r.`name`,r.theme,r.applyCode,r.ucode,r.businessTypeName,r.businessDetailName,r.submitTimes,r.businessTypeId,r.businessDetailId,r.url,r.apply_content"
    	+ " ,k.suspend_Status,r.audit_status pro_status from task_info k inner join kv_record r on k.business_key = r.id and k.assignee = r.user_id"
    	+ " join task_info_approve_position tap ON k.BUSINESS_KEY = tap.BUSINESS_KEY and k.ID = tap.task_id" 
    	+ " where tap.position_id in (" + postID + "," + userId + ") and (r.audit_status = '2' or r.audit_status = '3' or r.audit_status ='6') and k.suspend_Status = '自定义申请'  and k.catalog='start'"
    	+ " ) nobpm" 
    	+ " where 1=1 ";*/
    	
        StringBuffer sqlPagedQuerySelect = new StringBuffer();
        
        // 返回字段
        StringBuffer sqlSelect = new StringBuffer();
        sqlSelect.append("nobpm.BUSINESS_KEY,nobpm.proc_inst_id,nobpm.start_time,nobpm.end_time,");
        sqlSelect.append("nobpm.proc_def_id,nobpm.pro_name,nobpm.theme,nobpm.applyCode,nobpm.ucode,nobpm.businessTypeName,");
        sqlSelect.append("nobpm.businessDetailName,nobpm.businessTypeId,nobpm.businessDetailId,nobpm.url,nobpm.pro_flag,");
        sqlSelect.append("nobpm.apply_content,nobpm.submitTimes,nobpm.pro_status,nobpm.full_name,");
        sqlSelect.append("ti.COMPLETE_TIME");
        
        // 查询涉及的表
        StringBuffer sqlFrom = new StringBuffer();
        sqlFrom.append(" FROM(SELECT DISTINCT tap.position_id, k.process_instance_id AS proc_inst_id,");
        sqlFrom.append("k.business_key AS business_key, '' AS proc_def_id, r.create_time AS start_time, r.end_time AS end_time,");
        sqlFrom.append("k.presentation_subject AS pro_name,r.user_id AS start_user_id,r.id AS record_id,r.category AS category,");
        sqlFrom.append("r.status AS status,r.name AS record_name,r.theme AS theme,r.applyCode AS applyCode,r.ucode AS ucode,");
        sqlFrom.append("r.businessTypeName,r.businessDetailName,r.submitTimes,r.businessTypeId,r.businessDetailId,");
        sqlFrom.append("r.url,r.apply_content,'普通流程' AS pro_flag,r.audit_status pro_status,p.full_name FROM task_info k");
        sqlFrom.append(" INNER JOIN kv_record r ON k.business_key = r.id AND k.assignee = r.user_id");
        sqlFrom.append(" JOIN task_info_approve_position tap ON k.BUSINESS_KEY = tap.BUSINESS_KEY AND k.ID = tap.task_id");
        sqlFrom.append(" JOIN person_info p ON r.user_id=p.id");
        sqlFrom.append(" WHERE tap.position_id IN (").append(postID).append(")");
        sqlFrom.append(" AND ( r.audit_status = '2' OR r.audit_status = '3' OR r.audit_status = '6')");
        sqlFrom.append(" AND r.businessTypeId <> '9999' AND k.catalog = 'start'");
        sqlFrom.append(" UNION ALL");
        sqlFrom.append(" SELECT DISTINCT tap.position_id,k.process_instance_id,k.business_key,'自定义申请',");
        sqlFrom.append("r.create_time,r.end_time,k.presentation_subject,r.user_id,r.id,r.category,r.status,");
        sqlFrom.append("r.name,r.theme,r.applyCode,r.ucode,r.businessTypeName,r.businessDetailName,r.submitTimes,");
        sqlFrom.append("r.businessTypeId,r.businessDetailId,r.url,r.apply_content,k.suspend_Status,r.audit_status pro_status,p.full_name");
        sqlFrom.append(" FROM task_info k");
        sqlFrom.append(" INNER JOIN kv_record r ON k.business_key = r.id AND k.assignee = r.user_id");
        sqlFrom.append(" JOIN task_info_approve_position tap ON k.BUSINESS_KEY = tap.BUSINESS_KEY AND k.ID = tap.task_id");
        sqlFrom.append(" JOIN person_info p ON p.id=r.user_id");
        sqlFrom.append(" WHERE tap.position_id IN (").append(postID).append(")");
        sqlFrom.append(" AND ( r.audit_status = '2' OR r.audit_status = '3' OR r.audit_status = '6')");
        sqlFrom.append(" AND r.businessTypeId= '9999' AND k.catalog = 'start') nobpm");

        StringBuffer sqlLeftFrom = new StringBuffer();
        sqlLeftFrom.append(" left join (SELECT MAX(ti.COMPLETE_TIME) as COMPLETE_TIME,ti.BUSINESS_KEY FROM task_info ti");
        sqlLeftFrom.append(" WHERE ti.`STATUS` = 'complete' and ti.CATALOG = 'normal' group by ti.BUSINESS_KEY ) ti on nobpm.business_key = ti.BUSINESS_KEY");
        
        sqlPagedQuerySelect.append("SELECT ").append(sqlSelect.toString());
        sqlPagedQuerySelect.append(sqlFrom.toString()).append(sqlLeftFrom.toString()).append(" WHERE 1 = 1");
    	
    	StringBuffer sqlPagedQueryCount = new StringBuffer();
    	sqlPagedQueryCount.append("select count(1)");
    	sqlPagedQueryCount.append(sqlFrom.toString()).append(" WHERE 1 = 1");
    	/*String sqlPagedQueryCount ="select  count(1)"
    	    	+ " from (select DISTINCT hi.id_ as id,tap.position_id,hi.proc_inst_id_ as proc_inst_id,hi.business_key_ as business_key,hi.proc_def_id_ as proc_def_id,r.CREATE_TIME as start_time,hi.end_time_ as end_time,hi.name_ as pro_name,hi.start_user_id_ as start_user_id,r.id as record_id,r.category,r.`status`,r.`name` as record_name,r.theme"
    	    	+ " ,r.applyCode,r.ucode,r.businessTypeName,r.businessDetailName,r.submitTimes,r.businessTypeId,r.businessDetailId,r.url,r.apply_content,'普通流程' as pro_flag,r.audit_status pro_status" 
    	    	+ " from act_hi_procinst hi inner join kv_record r on hi.business_key_ = r.id"
    	    	+ " join task_info_approve_position tap ON hi.BUSINESS_KEY_ = tap.BUSINESS_KEY" 
    	    	+ " where tap.position_id in (" + postID + ") and hi.end_time_ is not null" 
    	    	+ " union all" 
    	    	+ " select DISTINCT k.id,tap.position_id,k.process_instance_id,k.business_key,'自定义申请',r.create_time,r.end_time,k.presentation_subject,r.user_id,r.id,r.category,r.`status`,r.`name`,r.theme,r.applyCode,r.ucode,r.businessTypeName,r.businessDetailName,r.submitTimes,r.businessTypeId,r.businessDetailId,r.url,r.apply_content"
    	    	+ " ,k.suspend_Status,r.audit_status pro_status from task_info k inner join kv_record r on k.business_key = r.id and k.assignee = r.user_id join task_info_approve_position tap ON k.BUSINESS_KEY = tap.BUSINESS_KEY" 
    	    	+ " where tap.position_id in (" + postID + "," + userId + ") and (r.audit_status = '2' or r.audit_status = '3' or r.audit_status ='6') is not null and k.suspend_Status = '自定义申请'  and k.catalog='start'"
    	    	+ " ) nobpm" 
    	    	+ " where 1=1 ";*/
    	
    	
    	
    	
    	
    	
    	
//    	String sqlPagedQueryCount = "select count(1) from (select hi.id_ as id,hi.proc_inst_id_ as proc_inst_id,"
//    	 		+ " hi.business_key_ as business_key,hi.proc_def_id_ as proc_def_id,r.CREATE_TIME as start_time,hi.end_time_ as end_time,"
//    	 		+ " hi.name_ as pro_name,hi.start_user_id_ as start_user_id,r.id as record_id,r.category,"
//    	 		+ " r.`status`,r.`name` as record_name,r.theme,r.applyCode,r.ucode,"
//    	 		+ " r.businessTypeName,r.businessDetailName,r.submitTimes,r.businessTypeId,r.businessDetailId"
//    	 		+ " ,r.audit_status pro_status"
//    	 		+ " from act_hi_procinst hi"
//    	 		+ " inner join kv_record r on hi.business_key_ = r.id"
//    	 		+ " where hi.start_user_id_ ='" + userId + "' and hi.end_time_ is not null "
//    	 		+ " union all"
//    	 		+ " select k.id,k.process_instance_id,k.business_key,'自定义申请',r.create_time,r.end_time,k.presentation_subject,r.user_id,r.id,"
//    	 		+ " r.category,r.`status`,r.`name`,r.theme,r.applyCode,r.ucode,"
//    	 		+ " r.businessTypeName,r.businessDetailName,r.submitTimes,r.businessTypeId,r.businessDetailId"
//    	 		+ " ,r.audit_status pro_status"
//    	 		+ " from task_info k"
//    	 		+ " inner join kv_record r on k.business_key = r.id and k.assignee = r.user_id"
//    	 		+ " where  r.user_id = '" + userId + "' and r.end_time is not null and k.suspend_Status = '自定义申请' and k.catalog='start'"
//    	 		+ " )  nobpm"
//    	 		+ " where nobpm.start_user_id ='" + userId + "'";

         StringBuilder buff = new StringBuilder();
         List<Object> paramList = new ArrayList<Object>();
         boolean checkWhere = sqlPagedQuerySelect.toString().toLowerCase().indexOf("where") == -1;
         PropertyFilterUtils.buildConfigurations(propertyFilters, buff,
                 paramList, checkWhere);
         /*
         logger.debug("propertyFilters : {}", propertyFilters);
         logger.debug("buff : {}", buff);
         logger.debug("paramList : {}", paramList);
         logger.debug("checkWhere : {}", checkWhere);
 		*/
         String sql = buff.toString();
         String countSql = sqlPagedQueryCount.toString() + " " + sql;
         String selectSql = sqlPagedQuerySelect.toString() + " " + sql + " order by nobpm.start_time Desc limit " 
                            + page.getStart() + "," + page.getPageSize();

         logger.debug("countSql : {}", countSql);
         logger.debug("selectSql : {}", selectSql);

         Object[] params = paramList.toArray();
         int totalCount = jdbcTemplate.queryForObject(countSql, Integer.class, params);
         List<Map<String, Object>> list = jdbcTemplate.queryForList(selectSql,  params);
         // List<PersonInfoDTO> personDtos = new ArrayList<PersonInfoDTO>();
         List<UnfinishProcessInstance> unfinishPros = new ArrayList<UnfinishProcessInstance>();
         
         for (Map<String, Object> map : list) {
        	 UnfinishProcessInstance uModelInstance=convertUnfinishProsDTO(map);
        	 uModelInstance.setStatus(dictConnector.findDictNameByValue("RecordStatus", map.get("pro_status").toString()));
        	 unfinishPros.add(uModelInstance);
         }

         page.setTotalCount(totalCount);
         page.setResult(unfinishPros);

         return page;
    }

    /**
     * 导出已结流程.
     */
    public Page exportCompletedProcessInstances(Page page, List<PropertyFilter> propertyFilters, String userId) {
        
    	//获取当前登录人的岗位id，若有多个岗位?
        StringBuffer postIDBuffer = new StringBuffer("");
        List<PartyEntity> postlist = orgConnector.getPostByUserId(userId);
		if (postlist != null && postlist.size() > 0) {
			for (PartyEntity party : postlist) {
				postIDBuffer.append(party.getId() + ",");
			}
			postIDBuffer.delete(postIDBuffer.length() - 1, postIDBuffer.length());
		}
        String 	postID = postIDBuffer.toString();
        if (StringUtils.isBlank(postID)) {
        	postID =userId;
        } else {
        	postID = postID + "," + userId;
        }
    	
        StringBuffer sqlPagedQuerySelect = new StringBuffer();
        
        // 返回字段
        StringBuffer sqlSelect = new StringBuffer();
        sqlSelect.append("nobpm.BUSINESS_KEY,nobpm.proc_inst_id,nobpm.start_time,nobpm.end_time,");
        sqlSelect.append("nobpm.proc_def_id,nobpm.pro_name,nobpm.theme,nobpm.applyCode,nobpm.ucode,nobpm.businessTypeName,");
        sqlSelect.append("nobpm.businessDetailName,nobpm.businessTypeId,nobpm.businessDetailId,nobpm.url,nobpm.pro_flag,");
        sqlSelect.append("nobpm.apply_content,nobpm.submitTimes,nobpm.pro_status,nobpm.full_name,");
        sqlSelect.append("ti.COMPLETE_TIME");
        
        // 查询涉及的表
        StringBuffer sqlFrom = new StringBuffer();
        sqlFrom.append(" FROM(SELECT DISTINCT tap.position_id, k.process_instance_id AS proc_inst_id,");
        sqlFrom.append("k.business_key AS business_key, '' AS proc_def_id, r.create_time AS start_time, r.end_time AS end_time,");
        sqlFrom.append("k.presentation_subject AS pro_name,r.user_id AS start_user_id,r.id AS record_id,r.category AS category,");
        sqlFrom.append("r.status AS status,r.name AS record_name,r.theme AS theme,r.applyCode AS applyCode,r.ucode AS ucode,");
        sqlFrom.append("r.businessTypeName,r.businessDetailName,r.submitTimes,r.businessTypeId,r.businessDetailId,");
        sqlFrom.append("r.url,r.apply_content,'普通流程' AS pro_flag,r.audit_status pro_status,p.full_name FROM task_info k");
        sqlFrom.append(" INNER JOIN kv_record r ON k.business_key = r.id AND k.assignee = r.user_id");
        sqlFrom.append(" JOIN task_info_approve_position tap ON k.BUSINESS_KEY = tap.BUSINESS_KEY AND k.ID = tap.task_id");
        sqlFrom.append(" JOIN person_info p ON p.id=r.user_id");
        sqlFrom.append(" WHERE tap.position_id IN (").append(postID).append(")");
        sqlFrom.append(" AND ( r.audit_status = '2' OR r.audit_status = '3' OR r.audit_status = '6')");
        sqlFrom.append(" AND r.businessTypeId <> '9999' AND k.catalog = 'start'");
        sqlFrom.append(" UNION ALL");
        sqlFrom.append(" SELECT DISTINCT tap.position_id,k.process_instance_id,k.business_key,'自定义申请',");
        sqlFrom.append("r.create_time,r.end_time,k.presentation_subject,r.user_id,r.id,r.category,r.status,");
        sqlFrom.append("r.name,r.theme,r.applyCode,r.ucode,r.businessTypeName,r.businessDetailName,r.submitTimes,");
        sqlFrom.append("r.businessTypeId,r.businessDetailId,r.url,r.apply_content,k.suspend_Status,r.audit_status pro_status,p.full_name");
        sqlFrom.append(" FROM task_info k");
        sqlFrom.append(" INNER JOIN kv_record r ON k.business_key = r.id AND k.assignee = r.user_id");
        sqlFrom.append(" JOIN task_info_approve_position tap ON k.BUSINESS_KEY = tap.BUSINESS_KEY AND k.ID = tap.task_id");
        sqlFrom.append(" JOIN person_info p ON p.id=r.user_id");
        sqlFrom.append(" WHERE tap.position_id IN (").append(postID).append(")");
        sqlFrom.append(" AND ( r.audit_status = '2' OR r.audit_status = '3' OR r.audit_status = '6')");
        sqlFrom.append(" AND r.businessTypeId= '9999' AND k.catalog = 'start') nobpm");
       
        StringBuffer sqlLeftFrom = new StringBuffer();
        sqlLeftFrom.append(" left join (SELECT MAX(ti.COMPLETE_TIME) as COMPLETE_TIME,ti.BUSINESS_KEY FROM task_info ti");
        sqlLeftFrom.append(" WHERE ti.`STATUS` = 'complete' and ti.CATALOG = 'normal' group by ti.BUSINESS_KEY ) ti on nobpm.business_key = ti.BUSINESS_KEY");
        
        sqlPagedQuerySelect.append("SELECT ").append(sqlSelect.toString());
        sqlPagedQuerySelect.append(sqlFrom.toString()).append(sqlLeftFrom.toString()).append(" WHERE 1 = 1");
    	
    	
//    	String sqlPagedQuerySelect = "select *,(SELECT MAX( COMPLETE_TIME ) FROM `task_info` ti WHERE nobpm.BUSINESS_KEY = ti.BUSINESS_KEY AND `STATUS` = 'complete' AND CATALOG NOT IN ( 'start', 'copy' )) AS COMPLETE_TIME "
//                + "from (select hi.id_ as id,hi.proc_inst_id_ as proc_inst_id,"
//                + "hi.business_key_ as business_key,hi.proc_def_id_ as proc_def_id,r.CREATE_TIME as start_time,hi.end_time_ as end_time,"
//                + "hi.name_ as pro_name,hi.start_user_id_ as start_user_id,r.id as record_id,r.category,"
//                + "r.`status`,r.`name` as record_name,r.theme,r.applyCode,r.ucode,"
//                + "r.businessTypeName,r.businessDetailName,r.submitTimes,r.businessTypeId,r.businessDetailId,r.url,r.apply_content,'普通流程' as pro_flag"
//                + ",r.audit_status pro_status"
//                + " from act_hi_procinst hi"
//                + " inner join kv_record r on hi.business_key_ = r.id"
//                + " where hi.start_user_id_ ='" + userId + "' and hi.end_time_ is not null"
//                + " union all"
//                + " select k.id,k.process_instance_id,k.business_key,'自定义申请',r.create_time,r.end_time,k.presentation_subject,r.user_id,r.id,"
//                + "r.category,r.`status`,r.`name`,r.theme,r.applyCode,r.ucode,"
//                + "r.businessTypeName,r.businessDetailName,r.submitTimes,r.businessTypeId,r.businessDetailId,r.url,r.apply_content,k.suspend_Status"
//                + ",r.audit_status pro_status"
//                + " from task_info k"
//                + " inner join kv_record r on k.business_key = r.id and k.assignee = r.user_id"
//                + " where r.user_id = '" + userId + "' and r.end_time is not null and k.suspend_Status = '自定义申请'  and k.catalog='start'"
//                + ") nobpm"
//                + " where nobpm.start_user_id ='" + userId + "'";

        StringBuilder buff = new StringBuilder();
        List<Object> paramList = new ArrayList<Object>();
        boolean checkWhere = sqlPagedQuerySelect.toString().toLowerCase().indexOf("where") == -1;
        PropertyFilterUtils.buildConfigurations(propertyFilters, buff,
                paramList, checkWhere);

        String sql = buff.toString();
        String selectSql = sqlPagedQuerySelect + " " + sql + " order by nobpm.start_time Desc  ";

        Object[] params = paramList.toArray();

        List<Map<String, Object>> list = jdbcTemplate.queryForList(selectSql,  params);
        List<UnfinishProcessInstance> unfinishPros = new ArrayList<UnfinishProcessInstance>();

        for (Map<String, Object> map : list) {
            UnfinishProcessInstance uModelInstance=convertUnfinishProsDTO(map);
            uModelInstance.setStatus(dictConnector.findDictNameByValue("RecordStatus", map.get("pro_status").toString()));
            unfinishPros.add(uModelInstance);
        }

        page.setResult(unfinishPros);

        return page;
    }
    /**
     * 已结流程.
     */
    public Page findCompletedProcessInstances(String userId, String tenantId,
            Page page) {
        HistoryService historyService = processEngine.getHistoryService();

        long count = historyService.createHistoricProcessInstanceQuery()
                .processInstanceTenantId(tenantId).startedBy(userId).finished()
                .count();
        List<HistoricProcessInstance> historicProcessInstances = historyService
                .createHistoricProcessInstanceQuery().startedBy(userId)
                .processInstanceTenantId(tenantId).finished()
                .listPage((int) 0, 100000);

        // 将取出的未结流程放入list，之后把自定义的未结流程也一起放入list，再传回页面，这样将这两部分就柔和到一起了
        List<UnfinishProcessInstance> list = new ArrayList<UnfinishProcessInstance>();
        for (HistoricProcessInstance vo : historicProcessInstances) {
        	UnfinishProcessInstance unfinishProcessInstance = new UnfinishProcessInstance();
        	unfinishProcessInstance.setId(vo.getId());
        	unfinishProcessInstance.setBusinessKey(vo.getBusinessKey());
        	unfinishProcessInstance.setStartTime(vo.getStartTime());
        	unfinishProcessInstance.setEndTime(vo.getEndTime());
        	unfinishProcessInstance.setProcessDefinitionId(vo.getProcessDefinitionId());
        	unfinishProcessInstance.setName(vo.getName());
        	
        	list.add(unfinishProcessInstance);
        }
        
        //计算自定义的未结流程总数是多少
        String hql = "from TaskInfo where assignee=? and status ='complete' and suspendStatus = '自定义申请' order by createTime desc";
        List<TaskInfo> t = taskInfoManager.find(hql, userId);
        int customCount = t.size();
        //计算总行数：未结流程的加上自定义的行数
        count=count+customCount;
        
        
        	 hql = "from TaskInfo where assignee=? and status ='complete' and suspendStatus = '自定义申请' ";
	        
	        List<TaskInfo> taskInfos = taskInfoManager.find(hql, userId);
	     
	        for (TaskInfo unProInstance : taskInfos) {
	        	 UnfinishProcessInstance unfinishProcessInstance = new UnfinishProcessInstance();
	         	 unfinishProcessInstance.setBusinessKey(Long.toString(unProInstance.getId()));
	         	 unfinishProcessInstance.setStartTime(unProInstance.getCreateTime());
	         	 unfinishProcessInstance.setEndTime(unProInstance.getCompleteTime());
	         	 unfinishProcessInstance.setProcessDefinitionId("自定义申请");
	         	 unfinishProcessInstance.setName(unProInstance.getPresentationSubject());
	         	 list.add(unfinishProcessInstance);
	         }
	     
	    SortClass sort = new SortClass();  
	    Collections.sort(list,sort);
	    
	   //当前要取自定义申请，从第几条开始取
    	int currentStart= (page.getPageNo()-1)*10+1;
    	
    	//判断当前页是否是最后一页
    	int lastPageNo=0;//末页的页数
    	int lastCountNo=0;//末页的行数 
    	if(count%10>0){
    		lastPageNo =(int)count/10+1;
    		lastCountNo=(int)count%10;
    	}
    	
    	//取到第几条结束  若不是末页，一共取十条，若当前页就是末页，还剩几条取几条
    	int currentEnd = 0;
    	if(page.getPageNo()==lastPageNo){
    		 currentEnd =  currentStart+lastCountNo-1;
    	}else{
    		currentEnd = currentStart+9;
    	}
    	
    	
    	List<UnfinishProcessInstance> unfinishResult = new ArrayList<UnfinishProcessInstance>();
    	
    	for(int i= currentStart-1;i<currentEnd;i++){
    		unfinishResult.add(list.get(i));
    	}
       
        page.setResult(unfinishResult);
        page.setTotalCount(count);

        return page;
    }

    /**
     * 参与流程.
     */
    public Page findInvolvedProcessInstances(String userId, String tenantId,
            Page page) {
        HistoryService historyService = processEngine.getHistoryService();

        // TODO: finished(), unfinished()
        long count = historyService.createHistoricProcessInstanceQuery()
                .processInstanceTenantId(tenantId).involvedUser(userId).count();
        List<HistoricProcessInstance> historicProcessInstances = historyService
                .createHistoricProcessInstanceQuery()
                .processInstanceTenantId(tenantId).involvedUser(userId)
                .listPage((int) page.getStart(), page.getPageSize());

        page.setResult(historicProcessInstances);
        page.setTotalCount(count);

        return page;
    }

    /**
     * 待办任务（个人任务）.
     */
    public Page findPersonalTasks(String userId, String tenantId, Page page) {
        TaskService taskService = processEngine.getTaskService();

        long count = taskService.createTaskQuery().taskTenantId(tenantId)
                .taskAssignee(userId).active().count();
        List<Task> tasks = taskService.createTaskQuery().taskTenantId(tenantId)
                .taskAssignee(userId).active()
                .listPage((int) page.getStart(), page.getPageSize());
        page.setResult(tasks);
        page.setTotalCount(count);

        return page;
    }

    /**
     * 代领任务（组任务）.
     */
    public Page findGroupTasks(String userId, String tenantId, Page page) {
        TaskService taskService = processEngine.getTaskService();

        long count = taskService.createTaskQuery().taskTenantId(tenantId)
                .taskCandidateUser(userId).active().count();
        List<Task> tasks = taskService.createTaskQuery().taskTenantId(tenantId)
                .taskCandidateUser(userId).active()
                .listPage((int) page.getStart(), page.getPageSize());
        page.setResult(tasks);
        page.setTotalCount(count);

        return page;
    }

    /**
     * 已办任务（历史任务）.
     */
    public Page findHistoryTasks(String userId, String tenantId, Page page) {
        HistoryService historyService = processEngine.getHistoryService();

        long count = historyService.createHistoricTaskInstanceQuery()
                .taskTenantId(tenantId).taskAssignee(userId).finished().count();
        List<HistoricTaskInstance> historicTaskInstances = historyService
                .createHistoricTaskInstanceQuery().taskTenantId(tenantId)
                .taskAssignee(userId).finished()
                .listPage((int) page.getStart(), page.getPageSize());
        page.setResult(historicTaskInstances);
        page.setTotalCount(count);

        return page;
    }

    /**
     * 代理中的任务（代理人还未完成该任务）.
     */
    public Page findDelegatedTasks(String userId, String tenantId, Page page) {
        TaskService taskService = processEngine.getTaskService();

        long count = taskService.createTaskQuery().taskTenantId(tenantId)
                .taskOwner(userId).taskDelegationState(DelegationState.PENDING)
                .count();
        List<Task> tasks = taskService.createTaskQuery().taskTenantId(tenantId)
                .taskOwner(userId).taskDelegationState(DelegationState.PENDING)
                .listPage((int) page.getStart(), page.getPageSize());
        page.setResult(tasks);
        page.setTotalCount(count);

        return page;
    }

    /**
     * 同时返回已领取和未领取的任务.
     */
    public Page findCandidateOrAssignedTasks(String userId, String tenantId,
            Page page) {
        TaskService taskService = processEngine.getTaskService();

        long count = taskService.createTaskQuery().taskTenantId(tenantId)
                .taskCandidateOrAssigned(userId).count();
        List<Task> tasks = taskService.createTaskQuery().taskTenantId(tenantId)
                .taskCandidateOrAssigned(userId)
                .listPage((int) page.getStart(), page.getPageSize());
        page.setResult(tasks);
        page.setTotalCount(count);

        return page;
    }

    /**
     * 流程定义.
     */
    public Page findProcessDefinitions(String tenantId, Page page) {
        RepositoryService repositoryService = processEngine
                .getRepositoryService();
        long count = repositoryService.createProcessDefinitionQuery()
                .processDefinitionTenantId(tenantId).count();
        List<ProcessDefinition> processDefinitions = repositoryService
                .createProcessDefinitionQuery()
                .processDefinitionTenantId(tenantId)
                .listPage((int) page.getStart(), page.getPageSize());
        page.setResult(processDefinitions);
        page.setTotalCount(count);

        return page;
    }

    /**
     * 流程定义.
     */
    public Page findProcessDefinitions(String name, String tenantId, Page page) {
    	
        RepositoryService repositoryService = processEngine.getRepositoryService();
        long count = repositoryService.createProcessDefinitionQuery()
                .processDefinitionTenantId(tenantId).processDefinitionName(name).count();
        List<ProcessDefinition> processDefinitions = repositoryService
                .createProcessDefinitionQuery()
                .processDefinitionTenantId(tenantId).processDefinitionName(name)
                .listPage((int) page.getStart(), page.getPageSize());
        page.setResult(processDefinitions);
        page.setTotalCount(count);

        return page;
    }
    
    /**
     * 流程实例.
     */
    public Page findProcessInstances(String tenantId, Page page) {
        RuntimeService runtimeService = processEngine.getRuntimeService();
        long count = runtimeService.createProcessInstanceQuery()
                .processInstanceTenantId(tenantId).count();
        List<ProcessInstance> processInstances = runtimeService
                .createProcessInstanceQuery().processInstanceTenantId(tenantId)
                .listPage((int) page.getStart(), page.getPageSize());
        page.setResult(processInstances);
        page.setTotalCount(count);

        return page;
    }

    /**
     * 任务.
     */
    public Page findTasks(String tenantId, Page page) {
        TaskService taskService = processEngine.getTaskService();
        long count = taskService.createTaskQuery().taskTenantId(tenantId)
                .count();
        List<Task> tasks = taskService.createTaskQuery().taskTenantId(tenantId)
                .listPage((int) page.getStart(), page.getPageSize());
        page.setResult(tasks);
        page.setTotalCount(count);

        return page;
    }

    /**
     * 部署.
     */
    public Page findDeployments(String tenantId, Page page) {
        RepositoryService repositoryService = processEngine
                .getRepositoryService();
        long count = repositoryService.createDeploymentQuery()
                .deploymentTenantId(tenantId).count();
        List<Deployment> deployments = repositoryService
                .createDeploymentQuery().deploymentTenantId(tenantId)
                .listPage((int) page.getStart(), page.getPageSize());
        page.setResult(deployments);
        page.setTotalCount(count);

        return page;
    }

    /**
     * 历史流程实例.
     */
    public Page findHistoricProcessInstances(String tenantId, Page page) {
        HistoryService historyService = processEngine.getHistoryService();

        long count = historyService.createHistoricProcessInstanceQuery()
                .processInstanceTenantId(tenantId).count();
        List<HistoricProcessInstance> historicProcessInstances = historyService
                .createHistoricProcessInstanceQuery()
                .processInstanceTenantId(tenantId)
                .listPage((int) page.getStart(), page.getPageSize());
        page.setResult(historicProcessInstances);
        page.setTotalCount(count);

        return page;
    }

    /**
     * 历史节点.
     */
    public Page findHistoricActivityInstances(String tenantId, Page page) {
        HistoryService historyService = processEngine.getHistoryService();

        long count = historyService.createHistoricActivityInstanceQuery()
                .activityTenantId(tenantId).count();
        List<HistoricActivityInstance> historicActivityInstances = historyService
                .createHistoricActivityInstanceQuery()
                .activityTenantId(tenantId)
                .listPage((int) page.getStart(), page.getPageSize());
        page.setResult(historicActivityInstances);
        page.setTotalCount(count);

        return page;
    }

    /**
     * 历史任务.
     */
    public Page findHistoricTaskInstances(String tenantId, Page page) {
        HistoryService historyService = processEngine.getHistoryService();

        long count = historyService.createHistoricTaskInstanceQuery()
                .taskTenantId(tenantId).count();
        List<HistoricTaskInstance> historicTaskInstances = historyService
                .createHistoricTaskInstanceQuery().taskTenantId(tenantId)
                .listPage((int) page.getStart(), page.getPageSize());
        page.setResult(historicTaskInstances);
        page.setTotalCount(count);

        return page;
    }

    /**
     * 作业.
     */
    public Page findJobs(String tenantId, Page page) {
        ManagementService managementService = processEngine
                .getManagementService();

        long count = managementService.createJobQuery().jobTenantId(tenantId)
                .count();
        List<Job> jobs = managementService.createJobQuery()
                .jobTenantId(tenantId)
                .listPage((int) page.getStart(), page.getPageSize());
        page.setResult(jobs);
        page.setTotalCount(count);

        return page;
    }

    protected UnfinishProcessInstance convertUnfinishProsDTO(Map<String, Object> map) {
        if ((map == null) || map.isEmpty()) {
            logger.info("UnfinishPros[{}] is null.", map);

            return null;
        }
        UnfinishProcessInstance unfinishProcessInstance = new UnfinishProcessInstance();
    	unfinishProcessInstance.setId(convertString(map.get("id")));
    	unfinishProcessInstance.setBusinessKey(convertString(map.get("business_key")));
    	unfinishProcessInstance.setProcessInstanceId(convertString(map.get("proc_inst_id")));
    	unfinishProcessInstance.setStartTime((Date)map.get("start_time"));
    	unfinishProcessInstance.setEndTime((Date)map.get("end_time"));
    	unfinishProcessInstance.setProcessDefinitionId(convertString(map.get("proc_def_id")));
    	unfinishProcessInstance.setName(convertString(map.get("pro_name")));
    	unfinishProcessInstance.setTheme(convertString(map.get("theme")).equals("") ? convertString(map.get("pro_name")) : convertString(map.get("theme")));
    	unfinishProcessInstance.setApplyCode(convertString(map.get("applyCode")));
    	unfinishProcessInstance.setUcode(convertString(map.get("ucode")));
    	unfinishProcessInstance.setBusinessTypeName(convertString(map.get("businessTypeName")));
    	unfinishProcessInstance.setBusinessDetailName(convertString(map.get("businessDetailName")));
    	unfinishProcessInstance.setBusinessTypeId(convertString(map.get("businessTypeId")));
    	unfinishProcessInstance.setBusinessDetailId(convertString(map.get("businessDetailId")));
    	unfinishProcessInstance.setUrl(convertString(map.get("url")));
    	unfinishProcessInstance.setProFlag(convertString(map.get("pro_flag")));
    	unfinishProcessInstance.setApplyContent(convertString(map.get("apply_content")));
    	unfinishProcessInstance.setSubmitTimes(convertString(map.get("submitTimes")) == null ? 1 : Integer.parseInt(convertString(map.get("submitTimes"))));
    	unfinishProcessInstance.setCompleteTime((Date)map.get("COMPLETE_TIME"));
    	unfinishProcessInstance.setApplyUserName(convertString(map.get("full_name")));

        return unfinishProcessInstance;
    }
    
    private String convertString(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof String) {
            return (String) value;
        }

        return value.toString();
    }
    /**
     * 导出未结流程主表数据
     * @author ckx
     */
    @Override
	public Page exportRunningProcessInstancesDetail(String userId,
			List<PropertyFilter> propertyFilters, Page page, String formName) {
		String table = "";
    	String entity ="";
    	String []arr = new HumanTaskConnectorImpl().tableAndEntity(formName,table,entity);
		table = arr[0];
    	entity = arr[1];
    	String sqlPagedQuerySelect = "select f_GetFirstAuditComment (nobpm.BUSINESS_KEY,nobpm.businesstypeid,nobpm.businessdetailid) AS COMMENT, nobpm.* ,ti.COMPLETE_TIME "
                + " from (select e.*,  hi.id_ as h_id,hi.proc_inst_id_ as proc_inst_id,"
                + "hi.business_key_ as business_key,hi.proc_def_id_ as proc_def_id,k.CREATE_TIME as create_time,k.create_time start_time,hi.end_time_ as end_time,"
                + "hi.name_ as pro_name,hi.start_user_id_ as start_user_id,k.id as record_id,k.category as k_category,"
                + "k.`status`,k.`name` as record_name,k.theme as k_theme,k.applyCode as k_applyCode,k.ucode as k_ucode,k.audit_status pro_status,"
                + "k.businessTypeName,k.businessDetailName,k.submitTimes as k_submitTimes,k.businessTypeId,k.businessDetailId,k.url,'普通流程' as pro_flag,k.apply_content k_apply_content"
                + " from act_hi_procinst hi"
                + " inner join kv_record k on hi.business_key_ = k.id "+ table
                + " where hi.start_user_id_ = '" + userId + "' and hi.end_time_ is null"
                + " union all"
                + " select e.*, q.id q_id,q.process_instance_id,q.business_key,'自定义申请',k.create_time,k.create_time start_time,k.end_time,q.presentation_subject,k.user_id,k.id,"
                + "k.category as k_category,k.`status`,k.`name` as record_name,k.theme as k_theme,k.applyCode as k_applyCode,k.ucode as k_ucode,k.audit_status pro_status,"
                + "k.businessTypeName,k.businessDetailName,k.submitTimes as k_submitTimes,k.businessTypeId,k.businessDetailId,k.url,q.suspend_Status,k.apply_content k_apply_content"
                + " from task_info q"
                + " inner join kv_record k on q.business_key = k.id and q.ASSIGNEE = k.user_id " + table
                + " where k.user_id ='" + userId + "' and k.end_time is null and q.suspend_Status = '自定义申请'  and q.catalog='start') nobpm"
                + " left join (SELECT MAX(ti.COMPLETE_TIME) as COMPLETE_TIME,ti.BUSINESS_KEY FROM task_info ti"
		        + " WHERE ti.`STATUS` = 'complete' and ti.CATALOG = 'normal' group by ti.BUSINESS_KEY ) ti on nobpm.business_key = ti.BUSINESS_KEY"
                + " where nobpm.start_user_id ='" + userId + "'";

    	StringBuilder buff = new StringBuilder();
    	List<Object> paramList = new ArrayList<Object>();
    	boolean checkWhere = sqlPagedQuerySelect.toLowerCase().indexOf("where") == -1;
    	PropertyFilterUtils.buildConfigurations(propertyFilters, buff, paramList, checkWhere);
    	String sql = buff.toString();
    	 if(sql.contains("theme like ?")){
         	sql = sql.replaceAll("theme like ", "k_theme like ");
         };
         if(sql.contains("applyCode like ?")){
         	sql = sql.replaceAll("applyCode like ", "k_applyCode like ");
         };
         if(sql.contains("ucode like ?")){
         	sql = sql.replaceAll("ucode like ", "k_ucode like ");
         };
    	String selectSql = sqlPagedQuerySelect + " " + sql + " order by COMPLETE_TIME Desc ";
    	
    	Object[] params = paramList.toArray();
    	List<Map<String, Object>> list = jdbcTemplate.queryForList(selectSql, params);
    	new HumanTaskConnectorImpl().confirmEntity(entity, page, list);
    	return page;
	}
    /**
     * 导出已结流程主表数据
     * @author ckx
     */
	@Override
	public Page exportCompletedProcessInstancesDetail(Page page,
			List<PropertyFilter> propertyFilters, String userId, String formName) {
		String table = "";
    	String entity ="";
    	String []arr = new HumanTaskConnectorImpl().tableAndEntity(formName,table,entity);
		table = arr[0];
    	entity = arr[1];
    	//获取当前登录人的岗位id，若有多个岗位?
        StringBuffer postIDBuffer = new StringBuffer("");
        List<PartyEntity> postlist = orgConnector.getPostByUserId(userId);
		if (postlist != null && postlist.size() > 0) {
			for (PartyEntity party : postlist) {
				postIDBuffer.append(party.getId() + ",");
			}
			postIDBuffer.delete(postIDBuffer.length() - 1, postIDBuffer.length());
		}
        String 	postID = postIDBuffer.toString();
        if (StringUtils.isBlank(postID)) {
        	postID =userId;
        } else {
        	postID = postID + "," + userId;
        }
    	StringBuffer sqlPagedQuerySelect = new StringBuffer();
        
        // 返回字段
        StringBuffer sqlSelect = new StringBuffer();
        sqlSelect.append("*,ti.COMPLETE_TIME");
        
        // 查询涉及的表
        StringBuffer sqlFrom = new StringBuffer();
        sqlFrom.append(" FROM(SELECT DISTINCT ");
        sqlFrom.append("e.*,k.BUSINESS_KEY,k.businessDetailId as k_businessDetailId,k.businessTypeId as k_businessTypeId,k.audit_status as k_pro_status,k.create_time as k_start_time,k.applyCode as k_applyCode,k.ucode as k_ucode,k.theme as k_theme,k.create_time,p.full_name");
        sqlFrom.append(" FROM task_info t");
        sqlFrom.append(" INNER JOIN kv_record k ON t.business_key = k.id AND t.assignee = k.user_id");
        sqlFrom.append(" JOIN task_info_approve_position tap ON t.BUSINESS_KEY = tap.BUSINESS_KEY AND t.ID = tap.task_id");
        sqlFrom.append(" JOIN person_info p ON k.user_id=p.id ");
        sqlFrom.append(table);
        sqlFrom.append(" WHERE tap.position_id IN (").append(postID).append(")");
        sqlFrom.append(" AND ( k.audit_status = '2' OR k.audit_status = '3' OR k.audit_status = '6')");
        sqlFrom.append(" AND k.businessTypeId <> '9999' AND t.catalog = 'start'");
        sqlFrom.append(" UNION ALL");
        sqlFrom.append(" SELECT DISTINCT ");
        sqlFrom.append("e.*,k.BUSINESS_KEY,k.businessDetailId as k_businessDetailId,k.businessTypeId as k_businessTypeId,k.audit_status as k_pro_status,k.create_time as k_start_time,k.applyCode as k_applyCode,k.ucode as k_ucode,k.theme as k_theme,k.create_time,p.full_name");
        sqlFrom.append(" FROM task_info t");
        sqlFrom.append(" INNER JOIN kv_record k ON t.business_key = k.id AND t.assignee = k.user_id");
        sqlFrom.append(" JOIN task_info_approve_position tap ON t.BUSINESS_KEY = tap.BUSINESS_KEY AND t.ID = tap.task_id");
        sqlFrom.append(" JOIN person_info p ON p.id=k.user_id ");
        sqlFrom.append(table);
        sqlFrom.append(" WHERE tap.position_id IN (").append(postID).append(")");
        sqlFrom.append(" AND ( k.audit_status = '2' OR k.audit_status = '3' OR k.audit_status = '6')");
        sqlFrom.append(" AND k.businessTypeId= '9999' AND t.catalog = 'start') nobpm");

        StringBuffer sqlLeftFrom = new StringBuffer();
        sqlLeftFrom.append(" left join (SELECT MAX(ti.COMPLETE_TIME) as COMPLETE_TIME,ti.BUSINESS_KEY FROM task_info ti");
        sqlLeftFrom.append(" WHERE ti.`STATUS` = 'complete' and ti.CATALOG = 'normal' group by ti.BUSINESS_KEY ) ti on nobpm.business_key = ti.BUSINESS_KEY");
        
        sqlPagedQuerySelect.append("SELECT ").append(sqlSelect.toString());
        sqlPagedQuerySelect.append(sqlFrom.toString()).append(sqlLeftFrom.toString()).append(" WHERE 1 = 1");
    	
    	StringBuilder buff = new StringBuilder();
    	List<Object> paramList = new ArrayList<Object>();
    	boolean checkWhere = sqlPagedQuerySelect.toString().toLowerCase().indexOf("where") == -1;
    	
    	PropertyFilterUtils.buildConfigurations(propertyFilters, buff, paramList, checkWhere);
    	String sql = buff.toString();
    	if(sql.contains("businessDetailId")){
    		sql = sql.replaceAll("businessDetailId", "k_businessDetailId");
    	};
    	if(sql.contains("businessTypeId")){
    		sql = sql.replaceAll("businessTypeId", "k_businessTypeId");
    	};
    	if(sql.contains("theme")){
    		sql = sql.replaceAll("theme", "k_theme");
    	};
    	if(sql.contains("applyCode")){
    		sql = sql.replaceAll("applyCode", "k_applyCode");
    	};
    	if(sql.contains("ucode")){
    		sql = sql.replaceAll("ucode", "k_ucode");
    	};
    	if(sql.contains("pro_status")){
    		sql = sql.replaceAll("pro_status", "k_pro_status");
    	};
    	if(sql.contains("start_time")){
    		sql = sql.replaceAll("start_time", "k_start_time");
    	};
    	String selectSql = sqlPagedQuerySelect + " " + sql + " order by COMPLETE_TIME Desc ";
    	
    	Object[] params = paramList.toArray();
    	List<Map<String, Object>> list = jdbcTemplate.queryForList(selectSql, params);
    	new HumanTaskConnectorImpl().confirmEntity(entity, page, list);
    	return page;
	}
    @Resource
    public void setProcessEngine(ProcessEngine processEngine) {
        this.processEngine = processEngine;
    }

    @Resource
    public void setBpmConfFormManager(BpmConfFormManager bpmConfFormManager) {
        this.bpmConfFormManager = bpmConfFormManager;
    }

    @Resource
    public void setBpmProcessManager(BpmProcessManager bpmProcessManager) {
        this.bpmProcessManager = bpmProcessManager;
    }

    @Resource
    public void setUserConnector(UserConnector userConnector) {
        this.userConnector = userConnector;
    }

    @Resource
    public void setFormConnector(FormConnector formConnector) {
        this.formConnector = formConnector;
    }

    @Resource
    public void setTaskDefinitionConnector(
            TaskDefinitionConnector taskDefinitionConnector) {
        this.taskDefinitionConnector = taskDefinitionConnector;
    }
    
    @Resource
    public void setTaskInfoManager(
    		TaskInfoManager taskInfoManager) {
        this.taskInfoManager = taskInfoManager;
    }

	@Resource
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
	
	@Resource
	public void setDictConnectorImpl(DictConnector dictConnector) {
		this.dictConnector = dictConnector;
	}
	
	@Resource
	public void setOrgConnector(OrgConnector orgConnector) {
		this.orgConnector = orgConnector;
	}
   
}
