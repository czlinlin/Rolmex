package com.mossle.humantask.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import com.mossle.api.dict.DictConnector;
import com.mossle.api.form.FormConnector;
import com.mossle.api.form.FormDTO;
import com.mossle.api.humantask.HumanTaskConnector;
import com.mossle.api.humantask.HumanTaskConstants;
import com.mossle.api.humantask.HumanTaskDTO;
import com.mossle.api.humantask.HumanTaskDefinition;
import com.mossle.api.humantask.ParticipantDTO;
import com.mossle.api.org.OrgConnector;
import com.mossle.api.party.PartyConnector;
import com.mossle.base.persistence.manager.BusinessDetailManager;
import com.mossle.base.service.DetailProcessService;
import com.mossle.base.service.DetailProcessService.GetWhole;
import com.mossle.core.auth.CurrentUserHolder;
import com.mossle.core.mapper.BeanMapper;
import com.mossle.core.page.Page;
import com.mossle.core.query.PropertyFilter;
import com.mossle.core.query.PropertyFilterUtils;
import com.mossle.dict.persistence.domain.DictInfo;
import com.mossle.humantask.listener.HumanTaskListener;
import com.mossle.humantask.persistence.domain.TaskConfUser;
import com.mossle.humantask.persistence.domain.TaskHistoryLog;
import com.mossle.humantask.persistence.domain.TaskInfo;
import com.mossle.humantask.persistence.domain.TaskInfoAndRecord;
import com.mossle.humantask.persistence.domain.TaskInfoApprovePosition;
import com.mossle.humantask.persistence.domain.TaskParticipant;
import com.mossle.humantask.persistence.domain.TaskToolLog;
import com.mossle.humantask.persistence.domain.UnfinishProcessInstance; 
import com.mossle.humantask.persistence.manager.TaskConfUserManager;
import com.mossle.humantask.persistence.manager.TaskDeadlineManager;
import com.mossle.humantask.persistence.manager.TaskHistoryLogManager;
import com.mossle.humantask.persistence.manager.TaskInfoApprovePositionManager;
import com.mossle.humantask.persistence.manager.TaskInfoManager;
import com.mossle.humantask.persistence.manager.TaskParticipantManager;
import com.mossle.keyvalue.persistence.manager.RecordManager;
import com.mossle.operation.persistence.domain.ApplyDTO;
import com.mossle.operation.persistence.domain.BusinessDTO;
import com.mossle.operation.persistence.domain.CancelOrderDTO;
import com.mossle.operation.persistence.domain.CustomEntityDTO;
import com.mossle.operation.persistence.domain.CustomPre;
import com.mossle.operation.persistence.domain.Exchange;
import com.mossle.operation.persistence.domain.FreezeDTO;
import com.mossle.operation.persistence.domain.InvoiceDTO;
import com.mossle.operation.persistence.domain.LllegalFreezeDTO;
import com.mossle.operation.persistence.domain.ReturnDTO;
import com.mossle.operation.persistence.manager.CustomApproverManager;
import com.mossle.operation.persistence.manager.CustomPreManager;
import com.mossle.party.persistence.domain.PartyEntity;
import com.mossle.party.persistence.manager.PartyEntityManager;
import com.mossle.party.support.PartyOrgConnector;
import com.mossle.spi.humantask.TaskDefinitionConnector;
import com.mossle.spi.process.InternalProcessConnector;
import com.mossle.spi.process.ProcessTaskDefinition;
import com.mossle.util.DateUtil;
import com.mossle.util.StringUtil;
import com.mossle.ws.persistence.domain.OnLineInfoDTO;
public class HumanTaskConnectorImpl implements HumanTaskConnector {
    private Logger logger = LoggerFactory.getLogger(HumanTaskConnectorImpl.class);
    private JdbcTemplate jdbcTemplate;
    private TaskInfoManager taskInfoManager;
    private TaskParticipantManager taskParticipantManager;
    private TaskConfUserManager taskConfUserManager;
    private TaskDeadlineManager taskDeadlineManager;
    private InternalProcessConnector internalProcessConnector;
    private TaskDefinitionConnector taskDefinitionConnector;
    private FormConnector formConnector;
    private BeanMapper beanMapper = new BeanMapper();
    private List<HumanTaskListener> humanTaskListeners;
    private DictConnector dictConnector;
    private BusinessDetailManager businessDetailManager;
    @Autowired
    private PartyEntityManager partyEntityManager;
    @Autowired
    private PartyConnector partyConnector;
    private OrgConnector orgConnector;
    private PartyOrgConnector partyOrgConnector;
    private TaskInfoApprovePositionManager taskInfoApprovePositionManager;
    @Autowired
    private DetailProcessService detailProcessService;
   @Autowired
    private CurrentUserHolder currentUserHolder;
    @Autowired
    private RecordManager recordManager;
    @Autowired
    private TaskHistoryLogManager taskHistoryLogManager;
    @Autowired
    private CustomApproverManager customApproverManager;
    @Autowired
    private CustomPreManager customPreManager;
    
    // ~

    /**
     * 创建一个任务.
     */
    public HumanTaskDTO createHumanTask() {
        return new HumanTaskBuilder().create();
    }

    // ~

    /**
     * 删除任务.
     */
    public void removeHumanTask(String humanTaskId) {
        TaskInfo taskInfo = taskInfoManager.get(Long.parseLong(humanTaskId));
        this.removeHumanTask(taskInfo);
    }

    public void removeHumanTaskByTaskId(String taskId) {
        TaskInfo taskInfo = taskInfoManager.findUniqueBy("taskId", taskId);
        this.removeHumanTask(taskInfo);
    }

    public void removeHumanTaskByProcessInstanceId(String processInstanceId) {
        String hql = "from TaskInfo where status='active' and processInstanceId=?";
        List<TaskInfo> taskInfos = taskInfoManager.find(hql, processInstanceId);

        for (TaskInfo taskInfo : taskInfos) {
            // TODO 2017-12-28 shijingxin 删除代领任务
            hql = "from TaskParticipant where taskInfo=?";
            List<TaskParticipant> taskParticipants = taskParticipantManager.find(hql, taskInfo);
            if (taskParticipants != null && taskParticipants.size() > 0) {
                for (TaskParticipant taskParticipant : taskParticipants) {
                    taskParticipantManager.removeById(taskParticipant.getId());
                }
            }
            this.removeHumanTask(taskInfo);
        }
    }

    public void removeHumanTask(TaskInfo taskInfo) {
        taskInfoManager.removeAll(taskInfo.getTaskDeadlines());
        taskInfoManager.removeAll(taskInfo.getTaskLogs());
        taskInfoManager.remove(taskInfo);
    }

    // ~

    /**
     * 保存任务.
     */
    public HumanTaskDTO saveHumanTask(HumanTaskDTO humanTaskDto) {
        return this.saveHumanTask(humanTaskDto, true);
    }

    @Transactional(readOnly = false)
    public HumanTaskDTO saveHumanTask(HumanTaskDTO humanTaskDto, boolean triggerListener) {
        // process first
        Long id = null;

        if (humanTaskDto.getId() != null) {
            try {
                id = Long.parseLong(humanTaskDto.getId());
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
            }
        }

        TaskInfo taskInfo = new TaskInfo();

        if (id != null) {
            taskInfo = taskInfoManager.get(id);
        }

        beanMapper.copy(humanTaskDto, taskInfo, HumanTaskDTO.class, TaskInfo.class);
        logger.debug("action : {}", humanTaskDto.getAction());
        logger.debug("comment : {}", humanTaskDto.getComment());
        logger.debug("action : {}", taskInfo.getAction());
        logger.debug("comment : {}", taskInfo.getComment());

        if (humanTaskDto.getParentId() != null) {
            taskInfo.setTaskInfo(taskInfoManager.get(Long.parseLong(humanTaskDto.getParentId())));
        }

        taskInfoManager.save(taskInfo);

        if (triggerListener) {
            // create
            if ((id == null) && (humanTaskListeners != null)) {
                for (HumanTaskListener humanTaskListener : humanTaskListeners) {
                    try {
                        humanTaskListener.onCreate(taskInfo);
                    } catch (Exception ex) {
                        logger.error(ex.getMessage(), ex);
                    }
                }
            }

            humanTaskDto.setAssignee(taskInfo.getAssignee());
            humanTaskDto.setOwner(taskInfo.getOwner());
        }

        humanTaskDto.setId(Long.toString(taskInfo.getId()));

        return humanTaskDto;
    }

    /**
     * 保存任务，并处理参与者.
     */
    public HumanTaskDTO saveHumanTaskAndProcess(HumanTaskDTO humanTaskDto) {
        return this.saveHumanTask(humanTaskDto, true);
    }

    public HumanTaskDTO findHumanTaskByTaskId(String taskId) {
        TaskInfo taskInfo = taskInfoManager.findUniqueBy("taskId", taskId);
        HumanTaskDTO humanTaskDto = new HumanTaskDTO();
        humanTaskDto = convertHumanTaskDto(taskInfo);

        return humanTaskDto;
    }

    @SuppressWarnings("unchecked")
    public List<HumanTaskDTO> findHumanTasksByProcessInstanceId(String processInstanceId) {
        List<TaskInfo> taskInfos = taskInfoManager.find(
                "from TaskInfo where processInstanceId=? and catalog !='copy' order by createTime asc,-complete_Time desc",
                processInstanceId);

        return this.convertHumanTaskDtos(taskInfos);
    }
    
    public List<HumanTaskDTO> convertHumanTaskDtosForPosition(Collection<TaskInfo> taskInfos) {
        List<HumanTaskDTO> humanTaskDtos = new ArrayList<HumanTaskDTO>();

        for (TaskInfo taskInfo : taskInfos) {
            if (taskInfo.getComment() == null)
                taskInfo.setComment("");
            
            TaskInfoApprovePosition taskInfoApprovePosition=taskInfoApprovePositionManager.findUniqueBy("taskId", taskInfo.getId());
            //String strSql="SELECT ifnull(position_id,0) as position_id,ifnull(approve_position_name,'') as approve_position_name FROM task_info_approve_position WHERE task_id=%s";
            /*List<Map<String,Object>> mapPositionList=jdbcTemplate.queryForList(String.format(strSql, taskInfo.getId()));
            if(mapPositionList!=null&&mapPositionList.size()>0){
            	Map<String,Object> mapPosition=mapPositionList.get(0);*/
            if(taskInfoApprovePosition!=null){
            	String strApprovePositionName=taskInfoApprovePosition.getApprovePositionName()==null?"":taskInfoApprovePosition.getApprovePositionName();
            	//如果有值，则直接取
            	if(!strApprovePositionName.equals(""))
            		taskInfo.setName(strApprovePositionName);
            	else {
            		//task_info_approve_position 没有值，有岗位值，则重新查询
            		Long postId=taskInfoApprovePosition.getPositionId()==null?0L:taskInfoApprovePosition.getPositionId();
            		if(postId!=0){
            			Map<String,String> mapPositionName=partyOrgConnector.getParentPartyEntityId(postId.toString());
            			taskInfoApprovePosition.setApprovePositionName(mapPositionName.get("position_name"));
            			taskInfoApprovePositionManager.save(taskInfoApprovePosition);
            			/*jdbcTemplate.update(String.format("update task_info_approve_position set approve_position_name='%s' where task_id=%s", 
			            					mapPositionName.get("position_name"),
			            					taskInfo.getId()
			            					 )
            							);*/
            			taskInfo.setName(mapPositionName.get("position_name"));
            		}
				}
            	/*PartyEntity partyEntity=partyEntityManager.findUniqueBy("id",Long.parseLong(mapPosition.get("position_id").toString()));
            	String strPositionNo="";
            	if(partyEntity.getPartyType().getId().equals(PartyConstants.PARTY_TYPE_POST)){
            		strSql="SELECT * FROM party_entity_attr WHERE ID=%s";
                	List<Map<String,Object>> mapPostAttrList=jdbcTemplate.queryForList(String.format(strSql, partyEntity.getId()));
                    if(mapPostAttrList!=null&&mapPostAttrList.size()>0){
                    	strPositionNo="("+mapPostAttrList.get(0).get("positionNo").toString()+")";
                    }
            	}
            	
            	String strDepartInfo=partyConnector.findDepartmentById(mapPosition.get("position_id").toString()).getName();
            	if(strDepartInfo.equals("罗麦科技")){
            		PartyDTO partyDTO=partyConnector.findAreaById(mapPosition.get("position_id").toString());
            		if(partyDTO!=null)
            			strDepartInfo=partyDTO.getName();
            		
            	}
            		
            	String strCompanyInfo=partyConnector.findCompanyInfoById(mapPosition.get("position_id").toString()).getName();
            	if(strCompanyInfo.equals(strDepartInfo))
            		taskInfo.setName(strCompanyInfo+"-"+partyEntity.getName()+strPositionNo);
            	else
            		taskInfo.setName(strCompanyInfo+"-"+strDepartInfo+"-"+partyEntity.getName()+strPositionNo);*/
            }
            HumanTaskDTO humanTaskDTO=convertHumanTaskDto(taskInfo);
            
            humanTaskDtos.add(humanTaskDTO);
        }

        return humanTaskDtos;
    }
    
    @SuppressWarnings("unchecked")
    public List<HumanTaskDTO> findHumanTasksForPositionByProcessInstanceId(String processInstanceId) {
        List<TaskInfo> taskInfos = taskInfoManager.find(
                "from TaskInfo where processInstanceId=? and catalog !='copy' order by createTime asc,-complete_Time desc",
                processInstanceId);

        return this.convertHumanTaskDtosForPosition(taskInfos);
    }

    public HumanTaskDTO findHumanTask(String humanTaskId) {
        Assert.hasText(humanTaskId, "humanTaskId不能为空");

        TaskInfo taskInfo = taskInfoManager.get(Long.parseLong(humanTaskId));
        return this.convertHumanTaskDto(taskInfo);
    }

    public List<HumanTaskDTO> findSubTasks(String parentTaskId) {
        List<TaskInfo> taskInfos = taskInfoManager.findBy("taskInfo.id", Long.parseLong(parentTaskId));

        return this.convertHumanTaskDtos(taskInfos);
    }

    /**
     * 获取任务表单.
     */
    public FormDTO findTaskForm(String humanTaskId) {

        com.mossle.spi.humantask.FormDTO taskFormDto = null;
        HumanTaskDTO humanTaskDto = this.findHumanTask(humanTaskId);

        FormDTO formDto = null;

        if (humanTaskDto.getTaskId() != null) {
            // formDto = internalProcessConnector.findTaskForm(humanTaskDto
            // .getTaskId());
            taskFormDto = taskDefinitionConnector.findForm(humanTaskDto.getCode(),
                    humanTaskDto.getProcessDefinitionId());

            if (taskFormDto == null) {
                logger.info("cannot find form by code : {}, processDefinition : {}", humanTaskDto.getCode(),
                        humanTaskDto.getProcessDefinitionId());
            } else {
                formDto = new FormDTO();
                formDto.setCode(taskFormDto.getKey());

                List<String> operations = taskDefinitionConnector.findOperations(humanTaskDto.getCode(),
                        humanTaskDto.getProcessDefinitionId());
                formDto.getButtons().addAll(operations);
                formDto.setActivityId(humanTaskDto.getCode());
                formDto.setProcessDefinitionId(humanTaskDto.getProcessDefinitionId());
            }
        } else {
            formDto = new FormDTO();
            formDto.setCode(humanTaskDto.getForm());
            formDto.setActivityId(humanTaskDto.getCode());
            formDto.setProcessDefinitionId(humanTaskDto.getProcessDefinitionId());
        }

        if (formDto == null) {
            logger.error("cannot find form : {}", humanTaskId);

            return new FormDTO();
        }

        formDto.setTaskId(humanTaskId);

        FormDTO contentFormDto = formConnector.findForm(formDto.getCode(), humanTaskDto.getTenantId());

        if (contentFormDto == null) {
            logger.error("cannot find form : {}", formDto.getCode());

            // zyl 2017-08-10
            if (taskFormDto != null && "external".equals(taskFormDto.getType())) {
                formDto.setRedirect(true);
                formDto.setUrl(taskFormDto.getKey());
                return formDto;

            }

            return formDto;
        }

        formDto.setRedirect(contentFormDto.isRedirect());
        formDto.setUrl(contentFormDto.getUrl());
        formDto.setContent(contentFormDto.getContent());

        return formDto;
    }

    /**
     * 根据流程定义获得所有任务定义.
     */
    public List<HumanTaskDefinition> findHumanTaskDefinitions(String processDefinitionId) {
        List<ProcessTaskDefinition> processTaskDefinitions = internalProcessConnector
                .findTaskDefinitions(processDefinitionId);

        List<HumanTaskDefinition> humanTaskDefinitions = new ArrayList<HumanTaskDefinition>();

        for (ProcessTaskDefinition processTaskDefinition : processTaskDefinitions) {
            HumanTaskDefinition humanTaskDefinition = new HumanTaskDefinition();
            beanMapper.copy(processTaskDefinition, humanTaskDefinition);
            humanTaskDefinitions.add(humanTaskDefinition);
        }

        return humanTaskDefinitions;
    }

    /**
     * 流程发起之前，配置每个任务的负责人.
     */
    public void configTaskDefinitions(String businessKey, List<String> taskDefinitionKeys, List<String> taskAssignees) {
        if (taskDefinitionKeys == null) {
            return;
        }

        int index = 0;

        for (String taskDefinitionKey : taskDefinitionKeys) {
            String taskAssignee = taskAssignees.get(index++);
            String hql = "from TaskConfUser where businessKey=? and code=?";
            TaskConfUser taskConfUser = taskConfUserManager.findUnique(hql, businessKey, taskDefinitionKey);

            if (taskConfUser == null) {
                taskConfUser = new TaskConfUser();
            }

            taskConfUser.setBusinessKey(businessKey);
            taskConfUser.setCode(taskDefinitionKey);
            taskConfUser.setValue(taskAssignee);
            taskConfUserManager.save(taskConfUser);
        }
    }

    /**
     * 完成任务.
     */
    @Transactional(readOnly = false)
    public void completeTask(String humanTaskId, String userId, String action, String comment,
                             Map<String, Object> taskParameters) {
        Assert.hasText(humanTaskId, "humanTaskId不能为空");
        logger.info("completeTask humanTaskId : {}, userId : {}, comment: {}", humanTaskId, userId, comment);

        HumanTaskDTO humanTaskDto = this.findHumanTask(humanTaskId);

        if (humanTaskDto == null) {
            throw new IllegalStateException("任务不存在");
        }

        humanTaskDto.setStatus("complete");
        humanTaskDto.setCompleteTime(new Date());
        humanTaskDto.setAction("完成");

        if (StringUtils.isNotBlank(action)) {
            humanTaskDto.setAction(action);
        }

        if (StringUtils.isNotBlank(comment)) {
            humanTaskDto.setComment(comment);
        }

        // TODO 017-12-29 没发现有啥用
        /*
         * Long longTaskId = Long.parseLong(humanTaskDto.getId());
		 * List<TaskDeadline> taskDeadlines = taskDeadlineManager.find(
		 * "from TaskDeadline where taskInfo.id=?", longTaskId); Long longTaskId
		 * = Long.parseLong(humanTaskDto.getId()); List<TaskDeadline>
		 * taskDeadlines =
		 * taskDeadlineManager.find("from TaskDeadline where taskInfo.id=?",
		 * longTaskId);
		 * 
		 * for (TaskDeadline taskDeadline : taskDeadlines) {
		 * taskDeadlineManager.remove(taskDeadline); }
		 */

        // 处理抄送任务
        if ("copy".equals(humanTaskDto.getCategory())) {
            humanTaskDto.setStatus("complete");
            humanTaskDto.setCompleteTime(new Date());
            humanTaskDto.setAction("完成");
            this.saveHumanTask(humanTaskDto, false);

            return;
        }

        // 处理startEvent任务
        if ("startEvent".equals(humanTaskDto.getCategory())) {
            humanTaskDto.setStatus("complete");
            humanTaskDto.setAction("提交");
            humanTaskDto.setCompleteTime(new Date());
            this.saveHumanTask(humanTaskDto, false);
            internalProcessConnector.signalExecution(humanTaskDto.getExecutionId());

            return;
        }

        logger.debug("{}", humanTaskDto.getDelegateStatus());

        // 处理协办任务
        if ("pending".equals(humanTaskDto.getDelegateStatus())) {
            humanTaskDto.setStatus("active");
            humanTaskDto.setDelegateStatus("resolved");
            humanTaskDto.setAssignee(humanTaskDto.getOwner());
            humanTaskDto.setAction("完成");
            this.saveHumanTask(humanTaskDto, false);
            internalProcessConnector.resolveTask(humanTaskDto.getTaskId());

            return;
        }

        // 处理协办链式任务
        if ("pendingCreate".equals(humanTaskDto.getDelegateStatus())) {
            humanTaskDto.setCompleteTime(new Date());
            humanTaskDto.setDelegateStatus("resolved");
            humanTaskDto.setStatus("complete");
            humanTaskDto.setAction("完成");
            this.saveHumanTask(humanTaskDto, false);

            if (humanTaskDto.getParentId() != null) {
                HumanTaskDTO targetHumanTaskDto = this.findHumanTask(humanTaskDto.getParentId());
                targetHumanTaskDto.setStatus("active");

                if (targetHumanTaskDto.getParentId() == null) {
                    targetHumanTaskDto.setDelegateStatus("resolved");
                }

                this.saveHumanTask(targetHumanTaskDto, false);
            }

            return;
        }

        this.saveHumanTask(humanTaskDto, false);

        // 判断加签
        if ("vote".equals(humanTaskDto.getCatalog()) && (humanTaskDto.getParentId() != null)) {
            HumanTaskDTO parentTask = this.findHumanTask(humanTaskDto.getParentId());
            boolean completed = true;

            for (HumanTaskDTO childTask : parentTask.getChildren()) {
                if (!"complete".equals(childTask.getStatus())) {
                    completed = false;

                    break;
                }
            }

            if (completed) {
                parentTask.setAssignee(parentTask.getOwner());
                parentTask.setOwner("");
                parentTask.setStatus("complete");
                parentTask.setCompleteTime(new Date());
                parentTask.setAction("完成");
                this.saveHumanTask(parentTask, false);
                internalProcessConnector.completeTask(humanTaskDto.getTaskId(), userId, taskParameters);
            }
        } else {
            internalProcessConnector.completeTask(humanTaskDto.getTaskId(), userId, taskParameters);
        }

        if (humanTaskListeners != null) {
            Long id = null;

            try {
                id = Long.parseLong(humanTaskDto.getId());
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
            }

            if (id == null) {
                return;
            }

            TaskInfo taskInfo = taskInfoManager.get(id);

            for (HumanTaskListener humanTaskListener : humanTaskListeners) {
                try {
                    humanTaskListener.onComplete(taskInfo);   // 发消息
                } catch (Exception ex) {
                    logger.error(ex.getMessage(), ex);
                }
            }
        }
    }

    /**
     * 待办任务.
     */
    public Page findPersonalTasks(String userId, String tenantId, int pageNo, int pageSize) {
        Page page = taskInfoManager.pagedQuery("from TaskInfo where assignee=? and tenantId=? and status='active'",
                pageNo, pageSize, userId, tenantId);
        List<TaskInfo> taskInfos = (List<TaskInfo>) page.getResult();
        List<HumanTaskDTO> humanTaskDtos = this.convertHumanTaskDtos(taskInfos);
        page.setResult(humanTaskDtos);

        return page;
    }

    /**
     * 待办任务.
     */
    public Page findPersonalTasks(String userId, String tenantId, List<PropertyFilter> propertyFilters, Page page) {

        String sqlPagedQuerySelect = "select *, ti.COMPLETE_TIME"
                + " from (select i.id,i.process_instance_id,i.action,r.applycode,i.presentation_subject,"
                + "r.pk_id,r.theme,r.user_id,p.full_name,r.ucode,r.businesstypeid,r.businesstypename,r.businessdetailid,"
                + "r.businessdetailname,r.systemid,r.systemname,r.areaid,r.areaname,r.companyid,r.companyname,"
                + "r.create_time,i.catalog,i.complete_time,i.assignee,r.url,i.suspend_Status as pro_falg,i.business_key"
                + " from task_info i" + " inner join kv_record r on i.business_key = r.id"
                + " inner join person_info p on r.user_id = p.id"
                + " where i.assignee  ='" + userId + "' and i.`status` = 'active' and i.CATALOG !='copy') t" 
                + " left join (SELECT MAX(ti.COMPLETE_TIME) as COMPLETE_TIME,ti.BUSINESS_KEY FROM task_info ti"
		        + " WHERE ti.`STATUS` = 'complete' and ti.CATALOG = 'normal' group by ti.BUSINESS_KEY ) ti on t.business_key = ti.BUSINESS_KEY"
                + " where assignee  ='" + userId + "'";

        String sqlPagedQueryCount = "select count(*) from (select i.id,i.process_instance_id,i.action,r.applycode,i.presentation_subject,"
                + "r.theme,r.user_id,p.full_name,r.ucode,r.businesstypeid,r.businesstypename,r.businessdetailid,"
                + "r.businessdetailname,r.systemid,r.systemname,r.areaid,r.areaname,r.companyid,r.companyname,"
                + "r.create_time,i.catalog,i.complete_time,i.assignee,r.url" + " from task_info i"
                + " inner join kv_record r on i.business_key = r.id"
                + " inner join person_info p on r.user_id = p.id"
                + " where i.assignee  ='" + userId + "' and i.`status` = 'active' and i.CATALOG !='copy') t where  assignee  ='" + userId + "'";

        StringBuilder buff = new StringBuilder();
        List<Object> paramList = new ArrayList<Object>();
        boolean checkWhere = sqlPagedQuerySelect.toLowerCase().indexOf("where") == -1;
        PropertyFilterUtils.buildConfigurations(propertyFilters, buff, paramList, checkWhere);
        /*
         * logger.debug("propertyFilters : {}", propertyFilters);
		 * logger.debug("buff : {}", buff); logger.debug("paramList : {}",
		 * paramList); logger.debug("checkWhere : {}", checkWhere);
		 */
        String sql = buff.toString();
        String countSql = sqlPagedQueryCount + " " + sql;
        String selectSql = sqlPagedQuerySelect + " " + sql + " order by create_time asc limit " + page.getStart() + ","
                + page.getPageSize();

        logger.debug("countSql : {}", countSql);
        logger.debug("selectSql : {}", selectSql);

        Object[] params = paramList.toArray();
        int totalCount = jdbcTemplate.queryForObject(countSql, Integer.class, params);
        List<Map<String, Object>> list = jdbcTemplate.queryForList(selectSql, params);
        List<UnfinishProcessInstance> unfinishPros = new ArrayList<UnfinishProcessInstance>();
        //从数据字典获取请假申请的超时标准
        List<DictInfo> dictInfos = dictConnector.findDictInfoListByType("leaveOvertime");
        String timeOut = "";
        //该字典仅一条数据，添加数据后无需改动代码
        for(DictInfo dictInfo : dictInfos){
        	if(dictInfo.getName().equals("12小时")){
        		timeOut = dictInfo.getValue();
        	}
        }
        long timeOutParseLong = Long.parseLong(timeOut);
        for (Map<String, Object> map : list) {
        	//是请假申请
        	if("8001".equals(map.get("businessdetailid"))){
        		Date createTime = (Date) map.get("create_time");
        		String pkId =(String) map.get("pk_id");
        		String findEntity = "select * from oa_bpm_customform where id="+pkId;
        		Map<String, Object> queryForMap = jdbcTemplate.queryForMap(findEntity);
        		String type = queryForMap.get("type").toString();
        		if("5".equals(type)){//表示补休假
        			map.put("compare", true);
        		}else{
        			String startTime = queryForMap.get("startTime").toString();
            		startTime = startTime += ":00";
    				long timeDifference = DateUtil.getTimeDifference(startTime,String.valueOf(createTime));
    				//12小时对应的秒数43200,(例：前一天21:00:59提交申请，请假时间始于今天09：00，不计超时)
    				if(timeDifference > timeOutParseLong-60){
    					map.put("compare", true);
    				}else{
    					map.put("compare", false);
    				}
        		}
        		
        	}
            unfinishPros.add(convertUnfinishProsDTO(map));
        }

        page.setTotalCount(totalCount);
        page.setResult(unfinishPros);

        return page;

    }

    /**
     * 待办任务(首页专用).
     */
    public Page findPersonalTasksToPortal(String userId, String tenantId, List<PropertyFilter> propertyFilters, Page page) {

        String sqlPagedQuerySelect = "select i.id,i.process_instance_id,i.action,r.applycode,i.presentation_subject,"
                + "r.pk_id,r.theme,r.user_id,p.full_name,r.ucode,r.businesstypeid,r.businesstypename,r.businessdetailid,"
                + "r.businessdetailname,r.systemid,r.systemname,r.areaid,r.areaname,r.companyid,r.companyname,"
                + "r.create_time,i.catalog,i.complete_time,i.assignee,r.url,i.suspend_Status as pro_falg,i.business_key"
                + " from task_info i" + " inner join kv_record r on i.business_key = r.id"
                + " inner join person_info p on r.user_id = p.id"
                + " where i.assignee  ='" + userId + "' and i.`status` = 'active' and i.CATALOG !='copy'";


        StringBuilder buff = new StringBuilder();
        List<Object> paramList = new ArrayList<Object>();
        boolean checkWhere = sqlPagedQuerySelect.toLowerCase().indexOf("where") == -1;
        PropertyFilterUtils.buildConfigurations(propertyFilters, buff, paramList, checkWhere);

        String sql = buff.toString();

        String selectSql = sqlPagedQuerySelect + " " + sql + " order by create_time limit " + page.getStart() + ","
                + page.getPageSize();

        logger.debug("selectSql : {}", selectSql);

        Object[] params = paramList.toArray();
        int totalCount = 10;
        List<Map<String, Object>> list = jdbcTemplate.queryForList(selectSql, params);
        List<UnfinishProcessInstance> unfinishPros = new ArrayList<UnfinishProcessInstance>();
        //从数据字典获取请假申请的超时标准
        List<DictInfo> dictInfos = dictConnector.findDictInfoListByType("leaveOvertime");
        String timeOut = "";
        //该字典仅一条数据，添加数据后无需改动代码
        for(DictInfo dictInfo : dictInfos){
        	if(dictInfo.getName().equals("12小时")){
        		timeOut = dictInfo.getValue();
        	}
        }
        long timeOutParseLong = Long.parseLong(timeOut);
        for (Map<String, Object> map : list) {
        	//是请假申请
        	if("8001".equals(map.get("businessdetailid"))){
        		Date createTime = (Date) map.get("create_time");
        		String pkId =(String) map.get("pk_id");
        		String findEntity = "select * from oa_bpm_customform where id="+pkId;
        		Map<String, Object> queryForMap = jdbcTemplate.queryForMap(findEntity);
        		String type = queryForMap.get("type").toString();
        		if("5".equals(type)){//表示补休假
        			map.put("compare", true);
        		}else{
        			String startTime = queryForMap.get("startTime").toString();
            		startTime = startTime += ":00";
    				long timeDifference = DateUtil.getTimeDifference(startTime,String.valueOf(createTime));
    				//12小时对应的秒数43200,(例：前一天21:00:59提交申请，请假时间始于今天09：00，不计超时)
    				if(timeDifference > timeOutParseLong-60){
    					map.put("compare", true);
    				}else{
    					map.put("compare", false);
    				}
        		}
        		
        	}
            unfinishPros.add(convertUnfinishProsDTO(map));
        }

        page.setTotalCount(totalCount);
        page.setResult(unfinishPros);

        return page;

    }

    /**
     * 抄送任务.
     */
    public Page findPersonalCopyTasks(String userId, String tenantId, List<PropertyFilter> propertyFilters, Page page,String status) {

    	
    	 //获取当前登录人的岗位id，若有多个岗位，放入一个字符串中
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
    		postID = "9999";
    	}

    	postID = postID +","+ userId;
    			
        String sqlPagedQuerySelect = "select  * ,ti.COMPLETE_TIME "
                + " from (select DISTINCT i.process_instance_id,r.applycode,"
                + "r.pk_id,r.theme,r.user_id,p.full_name,r.ucode,r.businesstypeid,r.businesstypename,r.businessdetailid,"
                + "r.businessdetailname,r.systemid,r.systemname,r.areaid,r.areaname,r.companyid,r.companyname,"
                + "r.create_time,i.catalog,r.url,i.suspend_Status as pro_falg,r.audit_status as pro_status,i.BUSINESS_KEY,i.presentation_subject "
                + " from task_info i" + " inner join kv_record r on i.business_key = r.id JOIN task_info_approve_position tap ON i.ID = tap.task_id "
                + " inner join person_info p on r.user_id = p.id"
                + " where i.CATALOG ='copy' and tap.position_id in (" + postID + ")) t"
                + " left join (SELECT MAX(ti.COMPLETE_TIME) as COMPLETE_TIME,ti.BUSINESS_KEY FROM task_info ti"
		        + " WHERE ti.`STATUS` = 'complete' and ti.CATALOG = 'normal' group by ti.BUSINESS_KEY ) ti on t.business_key = ti.BUSINESS_KEY"
                + " LEFT JOIN (SELECT MIN(tt.CREATE_TIME) AS cc_time,tt.BUSINESS_KEY FROM task_info tt GROUP BY tt.BUSINESS_KEY) tt ON t.business_key = tt.BUSINESS_KEY"
                + " LEFT JOIN ( SELECT tip.id as tip_id,tip.BUSINESS_KEY,tip.user_id as cc_user_id FROM task_info_copy tip where tip.user_id = '" + userId + "') tip ON t.business_key = tip.BUSINESS_KEY "
                + " where 1=1  and pro_status  ='" + 2 + "'  ";

        String sqlPagedQueryCount = "select count(1) from(select DISTINCT i.process_instance_id,r.applycode,"
                + "r.theme,r.user_id,p.full_name,r.ucode,r.businesstypeid,r.businesstypename,r.businessdetailid,"
                + "r.businessdetailname,r.systemid,r.systemname,r.areaid,r.areaname,r.companyid,r.companyname,"
                + "r.create_time,i.catalog,r.url,i.suspend_Status as pro_falg,r.audit_status as pro_status,i.BUSINESS_KEY,i.presentation_subject "
                + " from task_info i" + " inner join kv_record r on i.business_key = r.id  JOIN task_info_approve_position tap ON i.ID = tap.task_id "
                + " inner join person_info p on r.user_id = p.id"
                + " where i.CATALOG ='copy' and tap.position_id in (" + postID + ") ) t"
                + " left join (SELECT MAX(ti.COMPLETE_TIME) as COMPLETE_TIME,ti.BUSINESS_KEY FROM task_info ti"
		        + " WHERE ti.`STATUS` = 'complete' and ti.CATALOG = 'normal' group by ti.BUSINESS_KEY ) ti on t.business_key = ti.BUSINESS_KEY"
                + " LEFT JOIN (SELECT MIN(tt.CREATE_TIME) AS cc_time,tt.BUSINESS_KEY FROM task_info tt GROUP BY tt.BUSINESS_KEY) tt ON t.business_key = tt.BUSINESS_KEY"
                + " LEFT JOIN ( SELECT tip.id as tip_id,tip.BUSINESS_KEY,tip.user_id as cc_user_id FROM task_info_copy tip where tip.user_id = '" + userId + "' ) tip ON t.business_key = tip.BUSINESS_KEY "
                + " where 1=1  and pro_status  ='" + 2 + "'  ";

        StringBuilder buff = new StringBuilder();
        List<Object> paramList = new ArrayList<Object>();
        boolean checkWhere = sqlPagedQuerySelect.toLowerCase().indexOf("where") == -1;
        PropertyFilterUtils.buildConfigurations(propertyFilters, buff, paramList, checkWhere);
        /*
         * logger.debug("propertyFilters : {}", propertyFilters);
		 * logger.debug("buff : {}", buff); logger.debug("paramList : {}",
		 * paramList); logger.debug("checkWhere : {}", checkWhere);
		 */
        String sql = buff.toString();
        String statusSql = "";
        //ckx
        if("1".equals(status)){
        	statusSql = " and tip_id <> '' ";
        }else if("0".equals(status)){
        	statusSql = " and tip_id is NULL ";
        }
        
        String countSql = sqlPagedQueryCount + " "+statusSql +" "+ sql;
        String selectSql = sqlPagedQuerySelect + " "+statusSql +" "+ sql + " order by cc_time Desc limit " + page.getStart() + ","
                + page.getPageSize();//TODO sjx 18.10.15 抄送审批列表按抄送时间倒序（新增查询字段抄送时间）

        logger.debug("countSql : {}", countSql);
        logger.debug("selectSql : {}", selectSql);

        Object[] params = paramList.toArray();
        int totalCount = jdbcTemplate.queryForObject(countSql, Integer.class, params);
        List<Map<String, Object>> list = jdbcTemplate.queryForList(selectSql, params);
        List<UnfinishProcessInstance> unfinishPros = new ArrayList<UnfinishProcessInstance>();
        //从数据字典获取请假申请的超时标准
        List<DictInfo> dictInfos = dictConnector.findDictInfoListByType("leaveOvertime");
        String timeOut = "";
        //该字典仅一条数据，添加数据后无需改动代码
        for(DictInfo dictInfo : dictInfos){
        	if(dictInfo.getName().equals("12小时")){
        		timeOut = dictInfo.getValue();
        	}
        }
        long timeOutParseLong = Long.parseLong(timeOut);
        for (Map<String, Object> map : list) {
        	//是请假申请
        	if("8001".equals(map.get("businessdetailid"))){
        		Date createTime = (Date) map.get("create_time");
        		String pkId =(String) map.get("pk_id");
        		String findEntity = "select * from oa_bpm_customform where id="+pkId;
        		Map<String, Object> queryForMap = jdbcTemplate.queryForMap(findEntity);
        		String type = queryForMap.get("type").toString();
        		if("5".equals(type)){//表示补休假
        			map.put("compare", true);
        		}else{
        			String startTime = queryForMap.get("startTime").toString();
            		startTime = startTime += ":00";
    				long timeDifference = DateUtil.getTimeDifference(startTime,String.valueOf(createTime));
    				//12小时对应的秒数43200,(例：前一天21:00:59提交申请，请假时间始于今天09：00，不计超时)
    				if(timeDifference > timeOutParseLong-60){
    					map.put("compare", true);
    				}else{
    					map.put("compare", false);
    				}
        		}
        		
        	}
            UnfinishProcessInstance uModelInstance = convertUnfinishProsDTO(map);
            uModelInstance.setStatus(dictConnector.findDictNameByValue("RecordStatus", map.get("pro_status").toString()));
            unfinishPros.add(uModelInstance);
        }

        page.setTotalCount(totalCount);
        page.setResult(unfinishPros);

        return page;

    }

    /**
     * 导出抄送任务. wh
     */
    public Page exportPersonalCopyTasks(String userId, String tenantId, List<PropertyFilter> propertyFilters,
                                        Page page) {
    	
    	//获取当前登录人的岗位id，若有多个岗位，放入一个字符串中
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
    		postID = "9999";
    	}
    	
    	postID = postID +","+ userId;
    	
    	

        String sqlPagedQuerySelect ="select * , ti.COMPLETE_TIME "
                + " from (select DISTINCT i.CREATE_TIME as cc_time, i.process_instance_id,r.applycode,r.apply_content,"
                + "r.theme,r.user_id,p.full_name,r.ucode,r.businesstypeid,r.businesstypename,r.businessdetailid,"
                + "r.businessdetailname,r.systemid,r.systemname,r.areaid,r.areaname,r.companyid,r.companyname,"
                + "r.create_time,i.catalog,r.url,i.suspend_Status as pro_falg,r.audit_status as pro_status,i.BUSINESS_KEY"
                + " from task_info i" + " inner join kv_record r on i.business_key = r.id JOIN task_info_approve_position tap ON i.ID = tap.task_id "
                + " inner join person_info p on r.user_id = p.id"
                + " where i.CATALOG ='copy' and tap.position_id in (" + postID + ")) t"
                + " left join (SELECT MAX(ti.COMPLETE_TIME) as COMPLETE_TIME,ti.BUSINESS_KEY FROM task_info ti"
		        + " WHERE ti.`STATUS` = 'complete' and ti.CATALOG = 'normal' group by ti.BUSINESS_KEY ) ti on t.business_key = ti.BUSINESS_KEY"
                + " where 1=1  and pro_status  ='" + 2 + "'  ";
        		
        		
        		
//        		"select * , (SELECT MAX( COMPLETE_TIME ) FROM `task_info` ti WHERE t.BUSINESS_KEY = ti.BUSINESS_KEY AND `STATUS` = 'complete' AND CATALOG NOT IN ( 'start', 'copy' )) AS COMPLETE_TIME "
//                + " from (select DISTINCT i.id,i.process_instance_id,r.applycode,i.presentation_subject,"
//                + "r.theme,r.user_id,p.full_name,r.ucode,r.businesstypeid,r.businesstypename,r.businessdetailid,"
//                + "r.businessdetailname,r.systemid,r.systemname,r.areaid,r.areaname,r.companyid,r.companyname,"
//                + "r.create_time,i.catalog,r.url,i.suspend_Status as pro_falg,r.audit_status as pro_status,i.BUSINESS_KEY"
//                + " from task_info i" + " inner join kv_record r on i.business_key = r.id"
//                + " inner join person_info p on r.user_id = p.id"
//                + " where i.CATALOG ='copy' and i.assignee  ='" + userId + "'  ) t where 1=1  and pro_status  ='" + 2 + "'  ";

        StringBuilder buff = new StringBuilder();
        List<Object> paramList = new ArrayList<Object>();
        boolean checkWhere = sqlPagedQuerySelect.toLowerCase().indexOf("where") == -1;
        PropertyFilterUtils.buildConfigurations(propertyFilters, buff, paramList, checkWhere);
        String sql = buff.toString();

        String selectSql = sqlPagedQuerySelect + " " + sql + " order by COMPLETE_TIME Desc ";

        Object[] params = paramList.toArray();
        List<Map<String, Object>> list = jdbcTemplate.queryForList(selectSql, params);
        List<UnfinishProcessInstance> unfinishPros = new ArrayList<UnfinishProcessInstance>();

        for (Map<String, Object> map : list) {
            UnfinishProcessInstance uModelInstance = convertUnfinishProsDTO(map);
            uModelInstance
                    .setStatus(dictConnector.findDictNameByValue("RecordStatus", map.get("pro_status").toString()));
            unfinishPros.add(uModelInstance);
        }

        page.setResult(unfinishPros);

        return page;

    }
    public String [] tableAndEntity(String formName,String table,String entity){
    	String [] arr = new String[2];
    	if(formName.equals("operation/custom-apply-list")){
    		table = "left join oa_bpm_customform e on k.pk_id=e.id";
    		entity = "CustomEntityDTO";
    		arr[0] = table;
    		arr[1] = entity;
    	}else if(formName.equals("operation/process/GroupBusinessApplyForm")){
    		table = "left join entity_group_business e on k.pk_id=e.id";
    		entity = "GroupBusinessDTO";
    		arr[0] = table;
    		arr[1] = entity;
    	}else if(formName.equals("operation/process/BusinessApplyForm")){
    		table = "left join entity_business e on k.pk_id=e.id";
    		entity = "BusinessDTO";
    		arr[0] = table;
    		arr[1] = entity;
    	}else if(formName.equals("operation/process/FreezeApplyForm")){
    		table = "left join entity_freeze e on k.pk_id=e.id";
    		entity = "FreezeDTO";
    		arr[0] = table;
    		arr[1] = entity;
    	}else if(formName.equals("operation/process/LllegalFreezeApplyForm")){
    		table = "left join entity_lllegal_freeze e on k.pk_id=e.id";
    		entity = "LllegalFreezeDTO";
    		arr[0] = table;
    		arr[1] = entity;
    	}else if(formName.equals("operation/process/ReturnApplyForm")){
    		table = "left join entity_return e on k.pk_id=e.id";
        	entity = "ReturnDTO";
        	arr[0] = table;
    		arr[1] = entity;
        }else if(formName.equals("operation/process/InvoiceApplyForm")){
        	table = "left join entity_invoice e on k.pk_id=e.id";
        	entity = "InvoiceDTO";
        	arr[0] = table;
    		arr[1] = entity;
        }else if(formName.equals("operation/cancel-order")){
        	table = " left join `oa_bpm_cancel_order_total` e on k.pk_id=e.id";
	    	entity = "CancelOrderDTO";
	    	arr[0] = table;
    		arr[1] = entity;
        }else if(formName.equals("operation/common-operation")){
        	table = " left join `oa_bpm_commapply` e on k.pk_id=e.id";
	    	entity = "ApplyDTO";
	    	arr[0] = table;
    		arr[1] = entity;
        }else if(formName.equals("oaServicePushProcess")){
        	table = " left join `ro_pf_oaonline` e on k.pk_id=e.id";
	    	entity = "OnLineInfo";
	    	arr[0] = table;
    		arr[1] = entity;
        }else if(formName.equals("operation/process/ExchangeApplyForm")){
        	table = " left join `entity_exchange` e on k.pk_id=e.id";
	    	entity = "Exchange";
	    	arr[0] = table;
    		arr[1] = entity;
        }else if(formName.equals("operation/quality-exchange-goods")||"operation/process/QualityProblemExchangeApplyForm".equals(formName)){
        	table = " left join `entity_exchange` e on k.pk_id=e.id";
	    	entity = "Exchange";
	    	arr[0] = table;
    		arr[1] = entity;
        }
    	return arr;
    }
    /**
     * 存储数据的实体确定
     * @param entity
     * @param page
     * @param list
     */
    public void confirmEntity(String entity,Page page,List<Map<String, Object>> list){
    	if(entity.equals("BusinessDTO") || entity.equals("GroupBusinessDTO")){//业务申请和分公司业务申请共用
        	List<BusinessDTO> businesss = new ArrayList<BusinessDTO>();
        	for (Map<String, Object> map : list) {
        		BusinessDTO business = BusinessProcsee(map);
        		//uModelInstance.setStatus(dictConnector.findDictNameByValue("RecordStatus", map.get("pro_status").toString()));
        		businesss.add(business);
        	}
        	page.setResult(businesss);
        }
        if(entity.equals("FreezeDTO")){
        	List<FreezeDTO> freezes = new ArrayList<FreezeDTO>();
        	for (Map<String, Object> map : list) {
        		FreezeDTO freeze = FreezeProsee(map);
        		//uModelInstance.setStatus(dictConnector.findDictNameByValue("RecordStatus", map.get("pro_status").toString()));
        		freezes.add(freeze);
        	}
        	page.setResult(freezes);
        }
        if(entity.equals("LllegalFreezeDTO")){
        	List<LllegalFreezeDTO> LllegalFreeze = new ArrayList<LllegalFreezeDTO>();
        	for (Map<String, Object> map : list) {
                LllegalFreezeDTO uModelInstance = lllegalFreezeProsee(map);
                //uModelInstance.setStatus(dictConnector.findDictNameByValue("RecordStatus", map.get("pro_status").toString()));
                LllegalFreeze.add(uModelInstance);
            }
            page.setResult(LllegalFreeze);
        }
        if(entity.equals("ReturnDTO")){
        	List<ReturnDTO> returnDTO = new ArrayList<ReturnDTO>();
        	for (Map<String, Object> map : list) {
        		ReturnDTO returnData = returnProcess(map);
        		//uModelInstance.setStatus(dictConnector.findDictNameByValue("RecordStatus", map.get("pro_status").toString()));
        		returnDTO.add(returnData);
        	}
        	page.setResult(returnDTO);
        }
        if(entity.equals("InvoiceDTO")){
        	List<InvoiceDTO> invoiceDTO = new ArrayList<InvoiceDTO>();
        	for (Map<String, Object> map : list) {
        		InvoiceDTO invoiceData = invoiceProcess(map);
        		//uModelInstance.setStatus(dictConnector.findDictNameByValue("RecordStatus", map.get("pro_status").toString()));
        		invoiceDTO.add(invoiceData);
        	}
        	page.setResult(invoiceDTO);
        }
       
        if(entity.equals("CustomEntityDTO")){
        	List<CustomEntityDTO> customDTO = new ArrayList<CustomEntityDTO>();
        	for (Map<String, Object> map : list) {
        		CustomEntityDTO customData = customProcess(map);
        		//uModelInstance.setStatus(dictConnector.findDictNameByValue("RecordStatus", map.get("pro_status").toString()));
        		customDTO.add(customData);
        	}
        	page.setResult(customDTO);
        }
        if(entity.equals("CancelOrderDTO")){
        	List<CancelOrderDTO> cancelDTO = new ArrayList<CancelOrderDTO>();
        	for (Map<String, Object> map : list) {
        		CancelOrderDTO cancelData = cancelOrderProcess(map);
        		//uModelInstance.setStatus(dictConnector.findDictNameByValue("RecordStatus", map.get("pro_status").toString()));
        		cancelDTO.add(cancelData);
        	}
        	page.setResult(cancelDTO);
        }
        if(entity.equals("ApplyDTO")){
        	List<ApplyDTO> applyDTO = new ArrayList<ApplyDTO>();
        	for (Map<String, Object> map : list) {
        		ApplyDTO applyData = commonProcess(map);
        		//uModelInstance.setStatus(dictConnector.findDictNameByValue("RecordStatus", map.get("pro_status").toString()));
        		applyDTO.add(applyData);
        	}
        	page.setResult(applyDTO);
        }
        if(entity.equals("OnLineInfo")){
        	List<OnLineInfoDTO> OnLineInfos = new ArrayList<OnLineInfoDTO>();
        	for (Map<String, Object> map : list) {
        		OnLineInfoDTO onLineInfo = this.onlineProcess(map);
        		OnLineInfos.add(onLineInfo);
        	}
        	page.setResult(OnLineInfos);
        }
        if(entity.equals("Exchange")){
        	List<Exchange> xchangeeList = new ArrayList<Exchange>();
        	for (Map<String, Object> map : list) {
        		Exchange exchange = this.exchangeProcess(map);
        		xchangeeList.add(exchange);
        	}
        	page.setResult(xchangeeList);
        }
    }
    private Exchange exchangeProcess(Map<String, Object> map) {
    	if ((map == null) || map.isEmpty()) {
    		logger.info("finishProcessInstance[{}] is null.", map);
    		return null;
    	}
    	Exchange exchange = new Exchange();
    	exchange.setApplyCode(convertString(map.get("apply_code")));
    	exchange.setWareHouse(convertString(map.get("ware_house")));
    	exchange.setExchangeDate(convertString(map.get("apply_exchange_date")));
    	exchange.setEmpNo(convertString(map.get("emp_no")));
    	exchange.setUcode(convertString(map.get("ucode")));
    	exchange.setName(convertString(map.get("name")));
    	exchange.setTel(convertString(map.get("tel")));
    	exchange.setOrderNumber(convertString(map.get("order_number")));
    	exchange.setOrderTime(convertString(map.get("order_time")));
    	exchange.setPayType(convertString(map.get("pay_type")));
    	exchange.setOldConsignee(convertString(map.get("old_consignee")));
    	exchange.setOldConsigneeTel(convertString(map.get("old_consignee_tel")));
    	exchange.setZipCode(convertString(map.get("zip_code")));
    	exchange.setOldConsigneeAddress(convertString(map.get("old_consignee_address")));
    	exchange.setExchangeReason(convertString(map.get("exchange_reason")));
    	exchange.setNewConsigneeAddress(convertString(map.get("new_consignee_address")));
    	exchange.setNewConsignee(convertString(map.get("new_consignee")));
    	exchange.setNewConsigneeTel(convertString(map.get("new_consignee_tel")));
    	exchange.setAssignee(null != map.get("assignee")
        		? convertString(map.get("assignee")) : "");
    	//质量换货字段赋值 TODO sjx 2018.11.22
    	exchange.setWelfare(convertString(map.get("welfare")));
    	exchange.setLevel(convertString(map.get("level")));
    	exchange.setSystem(convertString(map.get("system")));
    	exchange.setVarFather(convertString(map.get("varFather")));
    	exchange.setVarRe(convertString(map.get("varRe")));
    	exchange.setAddTime(convertString(map.get("addTime")));
    	exchange.setBusinessType(convertString(map.get("businessType")));
    	exchange.setBusinessDetail(convertString(map.get("businessDetail")));
    	exchange.setBusinessLevel(convertString(map.get("businessLevel")));
    	exchange.setAddress(convertString(map.get("address")));
    	exchange.setArea(convertString(map.get("area")));
    	return exchange;
	}

	/**
     * 导出抄送菜单的实体表数据.
     */
    public Page exportPersonalCopyTasksBydetail(String userId, String tenantId, List<PropertyFilter> propertyFilters,
    		Page page,String formName) {
    	String table = "";
    	String entity ="";
    	String []arr = this.tableAndEntity(formName,table,entity);
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
        	postID = "9999";
        }

        postID = postID +","+ userId;
    	
    	
    	
    	String sqlPagedQuerySelect = "select f_GetFirstAuditComment(t.BUSINESS_KEY,t.businesstypeid,t.businessdetailid) as comment, t.* , ti.COMPLETE_TIME "
                + " from (select DISTINCT e.*,k.`businessDetailId`,k.`businessTypeId`,k.`applyCode` kapplyCode,k.`theme` ktheme,k.`ucode` kucode,i.presentation_subject,p.full_name,k.systemid,k.areaid,k.companyid,k.create_time,i.suspend_Status AS pro_falg,k.audit_status AS pro_status,i.BUSINESS_KEY"
                + " from task_info i" + " inner join kv_record k on i.business_key = k.id"
                + " inner join person_info p on k.user_id = p.id   JOIN task_info_approve_position tap ON i.ID = tap.task_id  "+table
                + " where i.CATALOG ='copy' and tap.position_id in (" + postID + ")  ) t" 
                + " left join (SELECT MAX(ti.COMPLETE_TIME) as COMPLETE_TIME,ti.BUSINESS_KEY FROM task_info ti"
		        + " WHERE ti.`STATUS` = 'complete' and ti.CATALOG = 'normal' group by ti.BUSINESS_KEY ) ti on t.business_key = ti.BUSINESS_KEY"
                + " where 1=1  and pro_status  ='" + 2 + "'  ";
    	
    	StringBuilder buff = new StringBuilder();
    	List<Object> paramList = new ArrayList<Object>();
    	boolean checkWhere = sqlPagedQuerySelect.toLowerCase().indexOf("where") == -1;
    	PropertyFilterUtils.buildConfigurations(propertyFilters, buff, paramList, checkWhere);
    	String sql = buff.toString();
    	String [] param = sql.split("and");
        StringBuffer sb = new StringBuffer();
        for(int i = 0;i < param.length;i++){
        	if(param[i].indexOf("theme like ?")>=0){
        		param[i] = "ktheme like ?";
        	}
        	if(param[i].indexOf("ucode like ?")>=0){
        		param[i] = "kucode like ?";
        	}
        	if(param[i].indexOf("applyCode like ?")>=0){
        		param[i] = "kapplyCode like ?";
        	}
        	sb.append(" "+param[i]);
        	if(i != param.length-1){
        		sb.append(" and");
        	}
        }
    	String selectSql = sqlPagedQuerySelect + " " + sb + " order by COMPLETE_TIME Desc ";
    	
    	Object[] params = paramList.toArray();
    	List<Map<String, Object>> list = jdbcTemplate.queryForList(selectSql, params);
    	this.confirmEntity(entity, page, list);
    	return page;
    	
    }

    /**
     * 全部审批 20171106 chengze.
     */
    public Page findAllApproval(String userId, String tenantId, List<PropertyFilter> propertyFilters, Page page) {

        String sqlPagedQuerySelect = "select * ,(SELECT MAX( COMPLETE_TIME ) FROM `task_info` ti WHERE t.BUSINESS_KEY = ti.BUSINESS_KEY AND `STATUS` = 'complete' AND CATALOG NOT IN ( 'start', 'copy' )) AS COMPLETE_TIME "
                + " from (select DISTINCT i.process_instance_id,r.applycode,i.presentation_subject,"
                + "r.theme,r.user_id,p.full_name,r.ucode,r.businesstypeid,r.businesstypename,r.businessdetailid,"
                + "r.businessdetailname,r.systemid,r.systemname,r.areaid,r.areaname,r.companyid,r.companyname,"
                + "r.create_time,i.catalog,i.assignee,r.url,i.suspend_Status as pro_falg,r.audit_status as pro_status,i.BUSINESS_KEY"
                + " from task_info i" + " inner join kv_record r on i.business_key = r.id"
                + " inner join person_info p on r.user_id = p.id" + " where i.`catalog` = 'start') t where 1=1 ";

        String sqlPagedQueryCount = "select count(*) from (select DISTINCT i.process_instance_id,r.applycode,i.presentation_subject,"
                + "r.theme,r.user_id,p.full_name,r.ucode,r.businesstypeid,r.businesstypename,r.businessdetailid,"
                + "r.businessdetailname,r.systemid,r.systemname,r.areaid,r.areaname,r.companyid,r.companyname,"
                + "r.create_time,i.catalog,i.assignee,r.url,r.audit_status as pro_status" + " from task_info i"
                + " inner join kv_record r on i.business_key = r.id"
                + " inner join person_info p on r.user_id = p.id" + " where i.`catalog` = 'start') t  where 1=1";

        StringBuilder buff = new StringBuilder();
        List<Object> paramList = new ArrayList<Object>();
        boolean checkWhere = sqlPagedQuerySelect.toLowerCase().indexOf("where") == -1;
        PropertyFilterUtils.buildConfigurations(propertyFilters, buff, paramList, checkWhere);
        /*
         * logger.debug("propertyFilters : {}", propertyFilters);
		 * logger.debug("buff : {}", buff); logger.debug("paramList : {}",
		 * paramList); logger.debug("checkWhere : {}", checkWhere);
		 */
        String sql = buff.toString();
        String countSql = sqlPagedQueryCount + " " + sql;
        String selectSql = sqlPagedQuerySelect + " " + sql + " order by create_time DESC limit " + page.getStart() + ","
                + page.getPageSize();

        logger.debug("countSql : {}", countSql);
        logger.debug("selectSql : {}", selectSql);

        Object[] params = paramList.toArray();
        int totalCount = jdbcTemplate.queryForObject(countSql, Integer.class, params);
        List<Map<String, Object>> list = jdbcTemplate.queryForList(selectSql, params);
        List<UnfinishProcessInstance> unfinishPros = new ArrayList<UnfinishProcessInstance>();

        for (Map<String, Object> map : list) {
            UnfinishProcessInstance uModelInstance = convertUnfinishProsDTO(map);
            uModelInstance
                    .setStatus(dictConnector.findDictNameByValue("RecordStatus", map.get("pro_status").toString()));
            unfinishPros.add(uModelInstance);
        }

        page.setTotalCount(totalCount);
        page.setResult(unfinishPros);

        return page;

    }

    /**
     * 导出全部审批 wh
     */
    public Page exportAllApproval(String userId, String tenantId, List<PropertyFilter> propertyFilters, Page page) {

        String sqlPagedQuerySelect = "select * ,(SELECT MAX( COMPLETE_TIME ) FROM `task_info` ti WHERE t.BUSINESS_KEY = ti.BUSINESS_KEY AND `STATUS` = 'complete' AND CATALOG NOT IN ( 'start', 'copy' )) AS COMPLETE_TIME "
                + " from (select DISTINCT i.process_instance_id,r.applycode,i.presentation_subject,"
                + "r.theme,r.user_id,p.full_name,r.ucode,r.businesstypeid,r.businesstypename,r.businessdetailid,"
                + "r.businessdetailname,r.systemid,r.systemname,r.areaid,r.areaname,r.companyid,r.companyname,"
                + "r.create_time,i.catalog,i.assignee,r.url,i.suspend_Status as pro_falg,r.audit_status as pro_status,i.BUSINESS_KEY,r.apply_content"
                + " from task_info i" + " inner join kv_record r on i.business_key = r.id"
                + " inner join person_info p on r.user_id = p.id" + " where i.`catalog` = 'start') t where 1=1 ";

        StringBuilder buff = new StringBuilder();
        List<Object> paramList = new ArrayList<Object>();
        boolean checkWhere = sqlPagedQuerySelect.toLowerCase().indexOf("where") == -1;
        PropertyFilterUtils.buildConfigurations(propertyFilters, buff, paramList, checkWhere);
        String sql = buff.toString();

        String selectSql = sqlPagedQuerySelect + " " + sql + " order by create_time DESC ";

        Object[] params = paramList.toArray();

        List<Map<String, Object>> list = jdbcTemplate.queryForList(selectSql, params);
        List<UnfinishProcessInstance> unfinishPros = new ArrayList<UnfinishProcessInstance>();

        for (Map<String, Object> map : list) {
            UnfinishProcessInstance uModelInstance = convertUnfinishProsDTO(map);
            uModelInstance
                    .setStatus(dictConnector.findDictNameByValue("RecordStatus", map.get("pro_status").toString()));
            unfinishPros.add(uModelInstance);
        }

        page.setResult(unfinishPros);

        return page;

    }
    /**
     * 通过细分导出流程对应的表单实体数据--全部审批
     * @author sjx
     * @param userId
     * @param tenantId
     * @param propertyFilters
     * @param page
     * @return
     */
    public Page exportAllApprovalBydetail(String userId, String tenantId, List<PropertyFilter> propertyFilters, Page page,String formName) {
    	String table = "";
    	String entity ="";
    	String []arr = this.tableAndEntity(formName,table,entity);
		table = arr[0];
    	entity = arr[1];
    	String sqlPagedQuerySelect = "select f_GetFirstAuditComment (t.BUSINESS_KEY,t.businesstypeid,t.businessdetailid) AS COMMENT, t.* ,(SELECT MAX( COMPLETE_TIME ) FROM `task_info` ti WHERE t.BUSINESS_KEY = ti.BUSINESS_KEY AND `STATUS` = 'complete' AND CATALOG NOT IN ( 'start', 'copy' )) AS COMPLETE_TIME "
                + " from (select DISTINCT e.*, k.applycode kapplyCode,i.presentation_subject,"
                + "p.full_name,k.ucode kucode,k.theme ktheme,k.businesstypeid,k.businesstypename,k.businessdetailid,"
                + "k.businessdetailname,k.systemid,k.systemname,k.areaid,k.areaname,k.companyid,k.companyname,"
                + "k.create_time,i.catalog,i.assignee,k.url,i.suspend_Status as pro_falg,k.audit_status as pro_status,i.BUSINESS_KEY"
                + " from task_info i" + " inner join kv_record k on i.business_key = k.id"
                + " inner join person_info p on k.user_id = p.id " + table +
                " where i.`catalog` = 'start') t where 1=1 ";
    	
    	StringBuilder buff = new StringBuilder();
    	List<Object> paramList = new ArrayList<Object>();
    	boolean checkWhere = sqlPagedQuerySelect.toLowerCase().indexOf("where") == -1;
    	PropertyFilterUtils.buildConfigurations(propertyFilters, buff, paramList, checkWhere);
    	String sql = buff.toString();
    	String [] param = sql.split("and");
        StringBuffer sb = new StringBuffer();
        for(int i = 0;i < param.length;i++){
        	if(param[i].indexOf("theme like ?")>=0){
        		param[i] = "ktheme like ?";
        	}
        	if(param[i].indexOf("ucode like ?")>=0){
        		param[i] = "kucode like ?";
        	}
        	if(param[i].indexOf("applyCode like ?")>=0){
        		param[i] = "kapplyCode like ?";
        	}
        	sb.append(" "+param[i]);
        	if(i != param.length-1){
        		sb.append(" and");
        	}
        }
    	String selectSql = sqlPagedQuerySelect + " " + sb + " order by COMPLETE_TIME Desc ";
    	
    	Object[] params = paramList.toArray();
    	List<Map<String, Object>> list = jdbcTemplate.queryForList(selectSql, params);
    	this.confirmEntity(entity, page, list);
    	return page;
    }
    /* (non-Javadoc)
     * @see com.mossle.api.humantask.HumanTaskConnector#exportManageQuery(java.lang.String, java.lang.String, java.util.List, com.mossle.core.page.Page)
     * 未结流程统计  列表数据导出
     */
    public Page exportManageQuery(String userId, String tenantId, List<PropertyFilter> propertyFilters, Page page) {
    	List<String> userAndPost = isManage(userId);
    	String positionStr = userAndPost.get(0);
    	String userIds = userAndPost.get(1);
    	if(positionStr.lastIndexOf(",") == positionStr.length()-1)
    		positionStr = positionStr.substring(0, positionStr.length()-1);
    	if(userIds.lastIndexOf(",") == userIds.length()-1)
    		userIds = userIds.substring(0, userIds.length()-1);
    	String sqlPagedQuerySelect = "SELECT *,(SELECT MAX(COMPLETE_TIME) FROM `task_info` ti WHERE t.BUSINESS_KEY = ti.BUSINESS_KEY AND `STATUS` = 'complete' AND CATALOG = 'normal') AS COMPLETE_TIME"
				+" FROM"
				+" (SELECT DISTINCT r.ref AS process_instance_id,r.applycode,r.theme,r.user_id,p.full_name,r.ucode,r.businessTypeId,r.businesstypename,r.businessdetailid,r.businessdetailname,r.systemid,r.systemname,"
				+" r.areaid,r.areaname,r.companyid,r.companyname,r.create_time,r.url,r.audit_status AS pro_status,i.BUSINESS_KEY,i.PROCESS_DEFINITION_ID"
				+" FROM task_info i INNER JOIN kv_record r ON i.business_key = r.id INNER JOIN person_info p ON r.user_id = p.id WHERE i.CATALOG = 'normal' AND r.audit_status IN ('0', '1', '4', '7', '8')) t"
				+" WHERE"
				+" ((t.businessTypeId <> '9999' AND t.PROCESS_DEFINITION_ID IN (SELECT DISTINCT b.PROCESS_DEFINITION_ID"
				+" FROM bpm_conf_base b INNER JOIN bpm_conf_node n ON n.CONF_BASE_ID = b.ID INNER JOIN bpm_conf_user u ON u.NODE_ID = n.ID WHERE REPLACE (u.`VALUE`, '岗位:', '') IN ("+positionStr+")"
				+" ))OR (t.businessTypeId = '9999' AND t.business_Key IN (SELECT ca.business_key FROM custom_approver ca WHERE ca.opterType NOT IN ('2', '3') AND approverId IN ("+userIds+"))))";
    	StringBuilder buff = new StringBuilder();
        List<Object> paramList = new ArrayList<Object>();
        boolean checkWhere = sqlPagedQuerySelect.toLowerCase().indexOf("where") == -1;
        PropertyFilterUtils.buildConfigurations(propertyFilters, buff, paramList, checkWhere);
        String sql = buff.toString();

        String selectSql = sqlPagedQuerySelect + " " + sql + " order by create_time DESC ";

        Object[] params = paramList.toArray();

        List<Map<String, Object>> list = jdbcTemplate.queryForList(selectSql, params);
        List<UnfinishProcessInstance> unfinishPros = new ArrayList<UnfinishProcessInstance>();
        
        for (Map<String, Object> map : list) {
        	GetWhole getWhole = new GetWhole();
        	if(null!=map.get("businessTypeId")){
        		if(!map.get("businessTypeId").toString().equals("")){
        			if("9999".equals(map.get("businessTypeId").toString())){
                		if(null!=map.get("process_instance_id")){
                			if(!map.get("process_instance_id").toString().equals(""))
                				getWhole = detailProcessService.bgProcessPersonInfoByProcessInstanceId(map.get("process_instance_id").toString());
                		}
        	    	}else{
        				getWhole = detailProcessService.bgGetProcessPostInfoByBusinessDetailId(map.get("businessdetailid").toString());
                	}
        		}
        	}
        	if(getWhole != null){
        		map.put("whole", getWhole.getWhole());
        	}
            UnfinishProcessInstance uModelInstance = convertUnfinishProsDTO(map);
            uModelInstance.setStatus(dictConnector.findDictNameByValue("RecordStatus", map.get("pro_status").toString()));
            unfinishPros.add(uModelInstance);
        }

        page.setResult(unfinishPros);

        return page;

    }
    /* (non-Javadoc)
     * @see com.mossle.api.humantask.HumanTaskConnector#exportManageQueryBydetail(java.lang.String, java.lang.String, java.util.List, com.mossle.core.page.Page, java.lang.String)
     * 未结流程统计  根据细分导出流程的表单数据
     */
    public Page exportManageQueryBydetail(String userId, String tenantId, List<PropertyFilter> propertyFilters, Page page,String formName) {
    	String table = "";
    	String entity ="";
    	String []arr = this.tableAndEntity(formName,table,entity);
		table = arr[0];
    	entity = arr[1];
    	//获取岗位和人员的字符串
    	List<String> userAndPost = isManage(userId);
    	String positionStr = userAndPost.get(0);
    	String userIds = userAndPost.get(1);
    	if(positionStr.lastIndexOf(",") == positionStr.length()-1)
    		positionStr = positionStr.substring(0, positionStr.length()-1);
    	if(userIds.lastIndexOf(",") == userIds.length()-1)
    		userIds = userIds.substring(0, userIds.length()-1);
    	/*String sqlPagedQuerySelect1 = "SELECT *,(SELECT MAX(COMPLETE_TIME) FROM `task_info` ti WHERE t.BUSINESS_KEY = ti.BUSINESS_KEY AND `STATUS` = 'complete' AND CATALOG NOT IN ('start', 'copy')) AS COMPLETE_TIME"
    			+" FROM"
    			+" (SELECT DISTINCT e.*,i.BUSINESS_KEY,k.businessTypeId,k.businessDetailId,"
    			+" k.companyid,k.create_time,k.url,k.audit_status AS pro_status FROM task_info i"
    			+" INNER JOIN kv_record k ON i.business_key = k.id"
    			+" INNER JOIN person_info p ON k.user_id = p.id "+table
    			+" WHERE i.CATALOG<>'start' AND k.audit_status in('0','1','4','7','8')"
    			+" AND (k.businessTypeId<>'9999' AND i.PROCESS_DEFINITION_ID in(SELECT DISTINCT b.PROCESS_DEFINITION_ID  FROM  bpm_conf_base b"
    			+" INNER JOIN bpm_conf_node n ON n.CONF_BASE_ID=b.ID"
    			+" INNER JOIN bpm_conf_user u ON u.NODE_ID=n.ID"
    			+" WHERE  u.`VALUE`<>'常用语:流程发起人' AND REPLACE(u.`VALUE`,'岗位:','') in("+positionStr+"))"
    			+" OR (k.businessTypeId='9999' AND i.business_Key in(SELECT ca.business_key FROM custom_approver ca where ca.opterType not in('2','3') AND approverId in("+userIds+")  GROUP BY ca.business_key)))"
    			+") t"
    			+" WHERE 1 = 1";*/
    	//因为kv_record取的别名不一致
    	table = table.replace("k.", "r.");
    	String sqlPagedQuerySelect = "SELECT *,(SELECT MAX(COMPLETE_TIME) FROM `task_info` ti WHERE t.BUSINESS_KEY = ti.BUSINESS_KEY AND `STATUS` = 'complete' AND CATALOG = 'normal') AS COMPLETE_TIME"
				+" FROM"
				+" (SELECT DISTINCT e.*, i.BUSINESS_KEY,r.businessTypeId,r.businessDetailId,r.companyid,r.create_time,r.url,r.audit_status AS pro_status,"
				+" i.PROCESS_DEFINITION_ID,i.PRESENTATION_SUBJECT"
				+" FROM task_info i INNER JOIN kv_record r ON i.business_key = r.id INNER JOIN person_info p ON r.user_id = p.id "+table+" WHERE i.CATALOG = 'normal' AND r.audit_status IN ('0', '1', '4', '7', '8')) t"
				+" WHERE"
				+" ((t.businessTypeId <> '9999' AND t.PROCESS_DEFINITION_ID IN (SELECT DISTINCT b.PROCESS_DEFINITION_ID"
				+" FROM bpm_conf_base b INNER JOIN bpm_conf_node n ON n.CONF_BASE_ID = b.ID INNER JOIN bpm_conf_user u ON u.NODE_ID = n.ID WHERE REPLACE (u.`VALUE`, '岗位:', '') IN ("+positionStr+")"
				+" ))OR (t.businessTypeId = '9999' AND t.business_Key IN (SELECT ca.business_key FROM custom_approver ca WHERE ca.opterType NOT IN ('2', '3') AND approverId IN ("+userIds+"))))";
    	StringBuilder buff = new StringBuilder();
    	List<Object> paramList = new ArrayList<Object>();
    	boolean checkWhere = sqlPagedQuerySelect.toLowerCase().indexOf("where") == -1;
    	PropertyFilterUtils.buildConfigurations(propertyFilters, buff, paramList, checkWhere);
    	String sql = buff.toString();
    	String [] param = sql.split("and");
        StringBuffer sb = new StringBuffer();
        for(int i = 0;i < param.length;i++){
        	if(param[i].indexOf("theme like ?")>=0){
        		param[i] = "ktheme like ?";
        	}
        	if(param[i].indexOf("ucode like ?")>=0){
        		param[i] = "kucode like ?";
        	}
        	if(param[i].indexOf("applyCode like ?")>=0){
        		param[i] = "kapplyCode like ?";
        	}
        	sb.append(" "+param[i]);
        	if(i != param.length-1){
        		sb.append(" and");
        	}
        }
    	String selectSql = sqlPagedQuerySelect + " " + sb + " order by COMPLETE_TIME Desc ";
    	
    	Object[] params = paramList.toArray();
    	List<Map<String, Object>> list = jdbcTemplate.queryForList(selectSql, params);
    	this.confirmEntity(entity, page, list);
    	return page;
    	
    }
    
    /**
     * 定制审批 20171106 chengze.
     */
    public Page findSpeicalPeopleApproval(String userId, String tenantId, List<PropertyFilter> propertyFilters,
                                          Page page) {
        // IN ('895001','435001')
        String speicalPeople = "";
        // 先获取数字字典中定制人的id
        List<DictInfo> dictInfo = this.dictConnector.findDictInfoListByType("speicalPeopleCode");
        for (DictInfo p : dictInfo) {
            speicalPeople = p.getValue();
        }
        String speicalPeopleResult = "";
        String[] spId = null;
        spId = speicalPeople.split(",");
        for (int i = 0; i < spId.length; i++) {

            speicalPeopleResult = speicalPeopleResult + spId[i] + ",";
        }

        speicalPeopleResult = speicalPeopleResult.substring(0, speicalPeopleResult.length() - 1);

        String sqlPagedQuerySelect = "select * ,ti.COMPLETE_TIME "
                + " from (select DISTINCT i.process_instance_id,r.applycode,i.presentation_subject,"
                + "r.theme,r.user_id,p.full_name,r.ucode,r.businesstypeid,r.businesstypename,r.businessdetailid,"
                + "r.businessdetailname,r.systemid,r.systemname,r.areaid,r.areaname,r.companyid,r.companyname,"
                + "r.create_time,i.catalog,i.assignee,r.url,i.suspend_Status as pro_falg,r.audit_status as pro_status,i.BUSINESS_KEY"
                + " from (select  b.*  from  (select DISTINCT PROCESS_INSTANCE_ID from task_info i "
                + " where i.assignee  in (" + speicalPeopleResult + ") ) a "
                + " join task_info b on a.PROCESS_INSTANCE_ID = b.PROCESS_INSTANCE_ID and b.`catalog` = 'start') i"
                + " inner join kv_record r on i.business_key = r.id"
                + " inner join person_info p on r.user_id = p.id) t" 
                + " left join (SELECT MAX(ti.COMPLETE_TIME) as COMPLETE_TIME,ti.BUSINESS_KEY FROM task_info ti"
		        + " WHERE ti.`STATUS` = 'complete' and ti.CATALOG = 'normal' group by ti.BUSINESS_KEY ) ti on t.business_key = ti.BUSINESS_KEY"
                + " where 1=1 ";

        String sqlPagedQueryCount = "select count(*) from (select DISTINCT i.process_instance_id,r.applycode,i.presentation_subject,"
                + "r.theme,r.user_id,p.full_name,r.ucode,r.businesstypeid,r.businesstypename,r.businessdetailid,"
                + "r.businessdetailname,r.systemid,r.systemname,r.areaid,r.areaname,r.companyid,r.companyname,"
                + "r.create_time,i.catalog,i.assignee,r.url,i.suspend_Status as pro_falg,r.audit_status as pro_status"
                + " from (select  b.*  from  (select DISTINCT PROCESS_INSTANCE_ID from task_info i "
                + " where i.assignee  in (" + speicalPeopleResult + ") ) a "
                + " join task_info b on a.PROCESS_INSTANCE_ID = b.PROCESS_INSTANCE_ID and b.`catalog` = 'start') i"
                + " inner join kv_record r on i.business_key = r.id"
                + " inner join person_info p on r.user_id = p.id) t where 1=1 ";

        StringBuilder buff = new StringBuilder();
        List<Object> paramList = new ArrayList<Object>();
        boolean checkWhere = sqlPagedQuerySelect.toLowerCase().indexOf("where") == -1;
        PropertyFilterUtils.buildConfigurations(propertyFilters, buff, paramList, checkWhere);
        /*
         * logger.debug("propertyFilters : {}", propertyFilters);
		 * logger.debug("buff : {}", buff); logger.debug("paramList : {}",
		 * paramList); logger.debug("checkWhere : {}", checkWhere);
		 */
        String sql = buff.toString();
        String countSql = sqlPagedQueryCount + " " + sql;
        String selectSql = sqlPagedQuerySelect + " " + sql + " order by create_time DESC limit " + page.getStart() + ","
                + page.getPageSize();

        logger.debug("countSql : {}", countSql);
        logger.debug("selectSql : {}", selectSql);

        Object[] params = paramList.toArray();
        int totalCount = jdbcTemplate.queryForObject(countSql, Integer.class, params);
        List<Map<String, Object>> list = jdbcTemplate.queryForList(selectSql, params);
        List<UnfinishProcessInstance> unfinishPros = new ArrayList<UnfinishProcessInstance>();

        for (Map<String, Object> map : list) {
            UnfinishProcessInstance uModelInstance = convertUnfinishProsDTO(map);
            uModelInstance
                    .setStatus(dictConnector.findDictNameByValue("RecordStatus", map.get("pro_status").toString()));
            unfinishPros.add(uModelInstance);
        }

        page.setTotalCount(totalCount);
        page.setResult(unfinishPros);

        return page;

    }

    /**
     * 导出定制审批 wh
     */
    public Page exportSpeicalPeopleApproval(String userId, String tenantId, List<PropertyFilter> propertyFilters,
                                            Page page) {
        // IN ('895001','435001')
        String speicalPeople = "";
        // 先获取数字字典中定制人的id
        List<DictInfo> dictInfo = this.dictConnector.findDictInfoListByType("speicalPeopleCode");
        for (DictInfo p : dictInfo) {
            speicalPeople = p.getValue();
        }
        String speicalPeopleResult = "";
        String[] spId = null;
        spId = speicalPeople.split(",");
        for (int i = 0; i < spId.length; i++) {

            speicalPeopleResult = speicalPeopleResult + spId[i] + ",";
        }

        speicalPeopleResult = speicalPeopleResult.substring(0, speicalPeopleResult.length() - 1);
        String sqlPagedQuerySelect = "select * ,ti.COMPLETE_TIME "
                + " from (select DISTINCT i.process_instance_id,r.applycode,i.presentation_subject,"
                + "r.theme,r.user_id,p.full_name,r.ucode,r.businesstypeid,r.businesstypename,r.businessdetailid,"
                + "r.businessdetailname,r.systemid,r.systemname,r.areaid,r.areaname,r.companyid,r.companyname,"
                + "r.create_time,i.catalog,i.assignee,r.url,i.suspend_Status as pro_falg,r.audit_status as pro_status,i.BUSINESS_KEY,r.apply_content"
                + " from (select  b.*  from  (select DISTINCT PROCESS_INSTANCE_ID from task_info i "
                + " where i.assignee  in (" + speicalPeopleResult + ") ) a "
                + " join task_info b on a.PROCESS_INSTANCE_ID = b.PROCESS_INSTANCE_ID and b.`catalog` = 'start') i"
                + " inner join kv_record r on i.business_key = r.id"
                + " inner join person_info p on r.user_id = p.id) t" 
                + " left join (SELECT MAX(ti.COMPLETE_TIME) as COMPLETE_TIME,ti.BUSINESS_KEY FROM task_info ti"
		        + " WHERE ti.`STATUS` = 'complete' and ti.CATALOG = 'normal' group by ti.BUSINESS_KEY ) ti on t.business_key = ti.BUSINESS_KEY"
                + " where 1=1 ";

        StringBuilder buff = new StringBuilder();
        List<Object> paramList = new ArrayList<Object>();
        boolean checkWhere = sqlPagedQuerySelect.toLowerCase().indexOf("where") == -1;
        PropertyFilterUtils.buildConfigurations(propertyFilters, buff, paramList, checkWhere);

        String sql = buff.toString();

        String selectSql = sqlPagedQuerySelect + " " + sql + " order by create_time DESC ";

        Object[] params = paramList.toArray();
        List<Map<String, Object>> list = jdbcTemplate.queryForList(selectSql, params);
        List<UnfinishProcessInstance> unfinishPros = new ArrayList<UnfinishProcessInstance>();

        for (Map<String, Object> map : list) {
            UnfinishProcessInstance uModelInstance = convertUnfinishProsDTO(map);
            uModelInstance
                    .setStatus(dictConnector.findDictNameByValue("RecordStatus", map.get("pro_status").toString()));
            unfinishPros.add(uModelInstance);
        }

        page.setResult(unfinishPros);

        return page;

    }
    /**
     * 导出定制审批流程主表数据
     * @author sjx
     */
    public Page exportSpeicalPeopleApprovalBydetail(String userId, String tenantId, List<PropertyFilter> propertyFilters,
            Page page,String formName) {
    	 String speicalPeople = "";
         // 先获取数字字典中定制人的id
         List<DictInfo> dictInfo = this.dictConnector.findDictInfoListByType("speicalPeopleCode");
         for (DictInfo p : dictInfo) {
             speicalPeople = p.getValue();
         }
         String speicalPeopleResult = "";
         String[] spId = null;
         spId = speicalPeople.split(",");
         for (int i = 0; i < spId.length; i++) {

             speicalPeopleResult = speicalPeopleResult + spId[i] + ",";
         }

         speicalPeopleResult = speicalPeopleResult.substring(0, speicalPeopleResult.length() - 1);
        String table = "";
     	String entity ="";
     	String []arr = this.tableAndEntity(formName,table,entity);
 		table = arr[0];
     	entity = arr[1];
         String sqlPagedQuerySelect = "select f_GetFirstAuditComment(t.BUSINESS_KEY,t.businesstypeid,t.businessdetailid) as comment, t.* ,ti.COMPLETE_TIME "
                 + " from (select DISTINCT e.*,k.applycode kapplyCode,i.presentation_subject,"
                 + "k.theme ktheme,p.full_name,k.ucode kucode,k.businessDetailId,k.businessTypeId,"
                 + "k.systemid,k.areaid,k.companyid,"
                 + "k.create_time,i.catalog,i.assignee,k.url,i.suspend_Status as pro_falg,k.audit_status as pro_status,i.BUSINESS_KEY"
                 + " from (select  b.*  from  (select DISTINCT PROCESS_INSTANCE_ID from task_info i "
                 + " where i.assignee  in (" + speicalPeopleResult + ") ) a "
                 + " join task_info b on a.PROCESS_INSTANCE_ID = b.PROCESS_INSTANCE_ID and b.`catalog` = 'start') i"
                 + " inner join kv_record k on i.business_key = k.id "+table
                 + " inner join person_info p on k.user_id = p.id) t"
                 + " left join (SELECT MAX(ti.COMPLETE_TIME) as COMPLETE_TIME,ti.BUSINESS_KEY FROM task_info ti"
                 + " WHERE ti.`STATUS` = 'complete' and ti.CATALOG = 'normal' group by ti.BUSINESS_KEY ) ti on t.business_key = ti.BUSINESS_KEY"
                 + " where 1=1 ";
         StringBuilder buff = new StringBuilder();
         List<Object> paramList = new ArrayList<Object>();
         boolean checkWhere = sqlPagedQuerySelect.toLowerCase().indexOf("where") == -1;
         PropertyFilterUtils.buildConfigurations(propertyFilters, buff, paramList, checkWhere);

         String sql = buff.toString();
     	 String [] param = sql.split("and");
         StringBuffer sb = new StringBuffer();
         for(int i = 0;i < param.length;i++){
         	if(param[i].indexOf("theme like ?")>=0){
         		param[i] = "ktheme like ?";
         	}
         	if(param[i].indexOf("ucode like ?")>=0){
         		param[i] = "kucode like ?";
         	}
         	if(param[i].indexOf("applyCode like ?")>=0){
         		param[i] = "kapplyCode like ?";
         	}
         	sb.append(" "+param[i]);
         	if(i != param.length-1){
         		sb.append(" and");
         	}
         }
     	String selectSql = sqlPagedQuerySelect + " " + sb + " order by COMPLETE_TIME Desc ";
     	
     	Object[] params = paramList.toArray();
     	List<Map<String, Object>> list = jdbcTemplate.queryForList(selectSql, params);
     	this.confirmEntity(entity, page, list);
     	return page;
    }
    /**
     * 部门申请.
     */
    public Page findDepartmentApplication(String userId, String tenantId, String departmentId,
                                          List<PropertyFilter> propertyFilters, Page page, String checkArea) {
        String sqlPagedQuerySelect = "";
        String sqlPagedQueryCount = "";
        if (checkArea.equals("0")) {
        	
        	StringBuffer sqlSelect = new StringBuffer();
        	sqlSelect.append("*,ti.COMPLETE_TIME");
        	
        	StringBuffer sqlFrom = new StringBuffer();
        	sqlFrom.append("(SELECT DISTINCT p.FULL_NAME,p.DEPARTMENT_CODE,k.*, t.CATALOG,t.process_instance_id,");
        	sqlFrom.append("t.presentation_subject,t.suspend_Status,k.audit_status AS pro_status");
        	sqlFrom.append(" FROM kv_record k");
        	sqlFrom.append(" JOIN person_info p ON p.ID = k.USER_ID");
        	sqlFrom.append(" JOIN task_info t ON k.id = t.BUSINESS_KEY");
        	sqlFrom.append(" JOIN task_info_approve_position tap ON t.ID = tap.task_id");
        	sqlFrom.append(" JOIN party_entity pe ON pe.id = tap.position_parentId");
        	sqlFrom.append(" WHERE t.CATALOG = 'start'");
        	sqlFrom.append(" AND pe.id IN (").append(departmentId).append(")) t");
        	
        	StringBuffer sqlLeftFrom = new StringBuffer();
        	sqlLeftFrom.append(" left join (SELECT MAX(ti.COMPLETE_TIME) as COMPLETE_TIME,ti.BUSINESS_KEY FROM task_info ti");
        	sqlLeftFrom.append(" WHERE ti.`STATUS` = 'complete' and ti.CATALOG = 'normal' group by ti.BUSINESS_KEY ) ti on t.business_key = ti.BUSINESS_KEY");
        	
            /*sqlPagedQuerySelect = "SELECT * ,(SELECT MAX( COMPLETE_TIME ) FROM `task_info` ti WHERE t.BUSINESS_KEY = ti.BUSINESS_KEY AND `STATUS` = 'complete' AND CATALOG NOT IN ( 'start', 'copy' )) AS COMPLETE_TIME "
                    + " FROM (SELECT p.FULL_NAME,p.DEPARTMENT_CODE,k.*,t.CATALOG,t.process_instance_id,t.presentation_subject,t.suspend_Status ,k.audit_status as pro_status"
                    + " from kv_record k  JOIN person_info p ON p.ID = k.USER_ID" + " join task_info t on k.id=t.BUSINESS_KEY"
                    + " JOIN party_entity pe ON pe.id = k.USER_ID "
                    + " JOIN party_struct ps ON pe.ID = ps.CHILD_ENTITY_ID" 
                    + " where t.CATALOG='start' and ps.PARENT_ENTITY_ID IN("
                    + departmentId + ")  ) t where 1=1";

            sqlPagedQueryCount = "SELECT count(1) from ( SELECT"
                    + " p.FULL_NAME,p.DEPARTMENT_CODE,k.*,t.CATALOG,t.process_instance_id,t.presentation_subject,t.suspend_Status ,k.audit_status as pro_status"
                    + " from kv_record k  JOIN person_info p ON p.ID = k.USER_ID" + " join task_info t on k.id=t.BUSINESS_KEY"
                    + " JOIN party_entity pe ON pe.id = k.USER_ID "
                    + " JOIN party_struct ps ON pe.ID = ps.CHILD_ENTITY_ID" 
                    + " where t.CATALOG='start' and ps.PARENT_ENTITY_ID IN("
                    + departmentId + ")  ) t where 1=1";*/
        	sqlPagedQuerySelect = "SELECT " + sqlSelect.toString() + " from " + sqlFrom.toString() + sqlLeftFrom.toString()  + " where 1=1";
        	sqlPagedQueryCount = "SELECT count(1) from " + sqlFrom + " where 1=1"; 
        } 
        StringBuilder buff = new StringBuilder();
        List<Object> paramList = new ArrayList<Object>();
        boolean checkWhere = sqlPagedQuerySelect.toLowerCase().indexOf("where") == -1;
        PropertyFilterUtils.buildConfigurations(propertyFilters, buff, paramList, checkWhere);

        String sql = buff.toString();
        String countSql = sqlPagedQueryCount + " " + sql;
        String selectSql = sqlPagedQuerySelect + " " + sql + " order by create_time DESC limit " + page.getStart() + ","
                + page.getPageSize();

        logger.debug("countSql : {}", countSql);
        logger.debug("selectSql : {}", selectSql);

        Object[] params = paramList.toArray();
        int totalCount = jdbcTemplate.queryForObject(countSql, Integer.class, params);
        List<Map<String, Object>> list = jdbcTemplate.queryForList(selectSql, params);
        List<UnfinishProcessInstance> unfinishPros = new ArrayList<UnfinishProcessInstance>();

        for (Map<String, Object> map : list) {
            UnfinishProcessInstance uModelInstance = convertUnfinishProsDTO(map);
            uModelInstance
                    .setStatus(dictConnector.findDictNameByValue("RecordStatus", map.get("pro_status").toString()));
            unfinishPros.add(uModelInstance);
        }

        page.setTotalCount(totalCount);
        page.setResult(unfinishPros);

        return page;
    }

    /**
     * 导出部门申请.
     */
    public Page exportDepartmentApplication(String userId, String tenantId, String departmentId,
                                            List<PropertyFilter> propertyFilters, Page page, String checkArea) {
//        String sqlPagedQuerySelect = "";
//        if (checkArea.equals("0")) {
//            sqlPagedQuerySelect = "SELECT * ,(SELECT MAX( COMPLETE_TIME ) FROM `task_info` ti WHERE t.BUSINESS_KEY = ti.BUSINESS_KEY AND `STATUS` = 'complete' AND CATALOG NOT IN ( 'start', 'copy' )) AS COMPLETE_TIME "
//                    + " FROM (SELECT p.FULL_NAME,p.DEPARTMENT_CODE,k.*,t.CATALOG,t.process_instance_id,t.presentation_subject,t.suspend_Status ,k.audit_status as pro_status"
//                    + " from kv_record k" + " join task_info t on k.id=t.BUSINESS_KEY"
//                    + " join person_info p on p.id=k.USER_ID" + " where t.CATALOG='start' and p.DEPARTMENT_CODE='"
//                    + departmentId + "') t where 1=1";
//        } else {
//            sqlPagedQuerySelect = "SELECT * ,(SELECT MAX( COMPLETE_TIME ) FROM `task_info` ti WHERE t.BUSINESS_KEY = ti.BUSINESS_KEY AND `STATUS` = 'complete' AND CATALOG NOT IN ( 'start', 'copy' )) AS COMPLETE_TIME "
//                    + " FROM (SELECT p.FULL_NAME,p.DEPARTMENT_CODE,k.*,t.CATALOG,t.process_instance_id,t.presentation_subject,t.suspend_Status ,k.audit_status as pro_status"
//                    + " from kv_record k" + " join task_info t on k.id=t.BUSINESS_KEY"
//                    + " join person_info p on p.id=k.USER_ID"
//                    + " join party_struct s on s.CHILD_ENTITY_ID=p.id"
//                    + " where t.CATALOG='start' and s.PARENT_ENTITY_ID='"
//                    + departmentId + "') t where 1=1";
//        }
    	
    	
    	 String sqlPagedQuerySelect = "";
         if (checkArea.equals("0")) {
         	
         	StringBuffer sqlSelect = new StringBuffer();
         	sqlSelect.append("*,ti.COMPLETE_TIME");
         	
         	StringBuffer sqlFrom = new StringBuffer();
         	sqlFrom.append("(SELECT DISTINCT p.FULL_NAME,p.DEPARTMENT_CODE,k.*, t.CATALOG,t.process_instance_id,");
         	sqlFrom.append("t.presentation_subject,t.suspend_Status,k.audit_status AS pro_status");
         	sqlFrom.append(" FROM kv_record k");
         	sqlFrom.append(" JOIN person_info p ON p.ID = k.USER_ID");
         	sqlFrom.append(" JOIN task_info t ON k.id = t.BUSINESS_KEY");
         	sqlFrom.append(" JOIN task_info_approve_position tap ON t.ID = tap.task_id");
         	sqlFrom.append(" JOIN party_entity pe ON pe.id = tap.position_parentId");
         	sqlFrom.append(" WHERE t.CATALOG = 'start'");
         	sqlFrom.append(" AND pe.id IN (").append(departmentId).append(")) t");
    	
         	StringBuffer sqlLeftFrom = new StringBuffer();
        	sqlLeftFrom.append(" left join (SELECT MAX(ti.COMPLETE_TIME) as COMPLETE_TIME,ti.BUSINESS_KEY FROM task_info ti");
        	sqlLeftFrom.append(" WHERE ti.`STATUS` = 'complete' and ti.CATALOG = 'normal' group by ti.BUSINESS_KEY ) ti on t.business_key = ti.BUSINESS_KEY");
        	
        	sqlPagedQuerySelect = "SELECT " + sqlSelect.toString() + " from " + sqlFrom.toString() + sqlLeftFrom.toString()  + " where 1=1";
        }
    	
    	
    	
    	
        StringBuilder buff = new StringBuilder();
        List<Object> paramList = new ArrayList<Object>();
        boolean checkWhere = sqlPagedQuerySelect.toLowerCase().indexOf("where") == -1;
        PropertyFilterUtils.buildConfigurations(propertyFilters, buff, paramList, checkWhere);

        String sql = buff.toString();
        String selectSql = sqlPagedQuerySelect + " " + sql + " order by create_time DESC  ";

        Object[] params = paramList.toArray();
        List<Map<String, Object>> list = jdbcTemplate.queryForList(selectSql, params);
        List<UnfinishProcessInstance> unfinishPros = new ArrayList<UnfinishProcessInstance>();

        for (Map<String, Object> map : list) {
            UnfinishProcessInstance uModelInstance = convertUnfinishProsDTO(map);
            uModelInstance
                    .setStatus(dictConnector.findDictNameByValue("RecordStatus", map.get("pro_status").toString()));
            unfinishPros.add(uModelInstance);
        }

        page.setResult(unfinishPros);

        return page;
    }
    /**
     * 导出部门申请主表数据
     * @author ckx
     */
    @Override
	public Page exportDepartmentApplicationDetail(String userId,
			String tenantId, String departmentId,
			List<PropertyFilter> propertyFilters, Page page, String checkArea,
			String formName) {
		String sqlPagedQuerySelect = "";
		
		String table = "";
    	String entity ="";
    	String []arr = this.tableAndEntity(formName,table,entity);
		table = arr[0];
    	entity = arr[1];
		
		
    	if (checkArea.equals("0")) {
    		
            sqlPagedQuerySelect = "SELECT f_GetFirstAuditComment (t.BUSINESS_KEY,t.businesstypeid,t.businessdetailid) AS COMMENT, t.* ,ti.COMPLETE_TIME "
                    + " FROM (SELECT distinct e.*,k.applyCode as k_applyCode,k.systemId,k.apply_content as k_apply_content,k.areaId,k.areaName,k.audit_status,k.businessDetailId,k.businessTypeId,k.BUSINESS_KEY,k.CREATE_TIME,k.end_time,"
                    +" k.theme as k_theme,k.ucode as k_ucode,k.url, p.FULL_NAME,p.DEPARTMENT_CODE,t.CATALOG,t.process_instance_id t_process,t.presentation_subject,t.suspend_Status ,k.audit_status as pro_status"+ " from kv_record k" + " join task_info t on k.id=t.BUSINESS_KEY"
                    + " join person_info p on p.id=k.USER_ID JOIN task_info_approve_position tap ON t.ID = tap.task_id	JOIN party_entity pe ON pe.id = tap.position_parentId " +table
                    + " where t.CATALOG='start' AND pe.id IN ("
                    + departmentId + ")) t" 
                    + " left join (SELECT MAX(ti.COMPLETE_TIME) as COMPLETE_TIME,ti.BUSINESS_KEY FROM task_info ti"
                    + " WHERE ti.`STATUS` = 'complete' and ti.CATALOG = 'normal' group by ti.BUSINESS_KEY ) ti on t.business_key = ti.BUSINESS_KEY"
                    + " where 1=1";
        } else {
            sqlPagedQuerySelect = "SELECT f_GetFirstAuditComment (t.BUSINESS_KEY,t.businesstypeid,t.businessdetailid) AS COMMENT, t.* ,ti.COMPLETE_TIME "
                    + " FROM (SELECT e.*,k.applyCode as k_applyCode,k.systemId,k.apply_content as k_apply_content,k.areaId,k.areaName,k.audit_status,k.businessDetailId,k.businessTypeId,k.BUSINESS_KEY,k.CREATE_TIME,k.end_time,"
                    +" k.theme as k_theme,k.ucode as k_ucode,k.url, p.FULL_NAME,p.DEPARTMENT_CODE,t.CATALOG,t.process_instance_id t_process,t.presentation_subject,t.suspend_Status ,k.audit_status as pro_status"
                    + " from kv_record k" + " join task_info t on k.id=t.BUSINESS_KEY"
                    + " join person_info p on p.id=k.USER_ID"
                    + " join party_struct s on s.CHILD_ENTITY_ID=p.id  JOIN task_info_approve_position tap ON t.ID = tap.task_id JOIN party_entity pe ON pe.id = tap.position_parentId " +table
                    + " where t.CATALOG='start' AND pe.id IN ("
                    + departmentId + ")) t" 
                    + " left join (SELECT MAX(ti.COMPLETE_TIME) as COMPLETE_TIME,ti.BUSINESS_KEY FROM task_info ti"
                    + " WHERE ti.`STATUS` = 'complete' and ti.CATALOG = 'normal' group by ti.BUSINESS_KEY ) ti on t.business_key = ti.BUSINESS_KEY"
                    + " where 1=1";
        }
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
        String selectSql = sqlPagedQuerySelect + " " + sql + " order by create_time DESC  ";

        Object[] params = paramList.toArray();
        List<Map<String, Object>> list = jdbcTemplate.queryForList(selectSql, params);
    	this.confirmEntity(entity, page, list);

        return page;
	}
    /**
     * 部门审批.
     */
    public Page findDepartmentApproval(String userId, String tenantId, String departmentId,
                                       List<PropertyFilter> propertyFilters, Page page, String checkArea) {

        String sqlPagedQuerySelect = "";
        String sqlPagedQueryCount = "";
        if (checkArea.equals("0")) {
            /*sqlPagedQuerySelect = "SELECT * ,(SELECT MAX( COMPLETE_TIME ) FROM `task_info` ti WHERE t.BUSINESS_KEY = ti.BUSINESS_KEY AND `STATUS` = 'complete' AND CATALOG NOT IN ( 'start', 'copy' )) AS COMPLETE_TIME "
                    + " FROM (SELECT k.*,a.DISPLAY_NAME,s.PROCESS_INSTANCE_ID,s.presentation_subject ,s.suspend_Status,k.audit_status as pro_status"
                    + " from (select b.ASSIGNEE,BUSINESS_KEY,b.PROCESS_INSTANCE_ID,b.presentation_subject,b.suspend_Status , min(b.TASK_ID) "
                    + " from  party_struct ps JOIN task_info b ON  ps.CHILD_ENTITY_ID = b.ASSIGNEE WHERE ps.PARENT_ENTITY_ID IN (" + departmentId
                    + " ) and b.CATALOG <>'start' and b.CATALOG <>'copy' and b.`STATUS`='complete'  GROUP BY b.BUSINESS_KEY) s"
                    + " join kv_record k on s.BUSINESS_KEY = k.id" + " join account_info  a on k.USER_ID = a.ID"
                    + " where k.USER_ID<>s.ASSIGNEE ) t where 1=1 ";
            sqlPagedQueryCount = "SELECT COUNT(1) from (SELECT "
                    + " k.*,a.DISPLAY_NAME,s.PROCESS_INSTANCE_ID,s.presentation_subject ,s.suspend_Status,k.audit_status as pro_status"
                    + " from (select b.ASSIGNEE,BUSINESS_KEY,b.PROCESS_INSTANCE_ID,b.presentation_subject,b.suspend_Status , min(b.TASK_ID) "
                    + " from  party_struct ps JOIN task_info b ON  ps.CHILD_ENTITY_ID = b.ASSIGNEE WHERE ps.PARENT_ENTITY_ID IN (" + departmentId
                    + " ) and b.CATALOG <>'start' and b.CATALOG <>'copy' and b.`STATUS`='complete'  GROUP BY b.BUSINESS_KEY) s"
                    + " join kv_record k on s.BUSINESS_KEY = k.id" + " join account_info  a on k.USER_ID = a.ID"
                    + " where k.USER_ID<>s.ASSIGNEE ) t where 1=1 ";*/
            
            StringBuffer sqlSelect = new StringBuffer();
            sqlSelect.append("*,ti.COMPLETE_TIME");
            
            StringBuffer sqlFrom = new StringBuffer();
            sqlFrom.append("(SELECT k.*, a.DISPLAY_NAME,s.PROCESS_INSTANCE_ID,s.presentation_subject,s.suspend_Status,k.audit_status AS pro_status");
            sqlFrom.append(" FROM( SELECT b.ASSIGNEE,b.BUSINESS_KEY,b.PROCESS_INSTANCE_ID,b.presentation_subject,b.suspend_Status,min(b.TASK_ID)");
            sqlFrom.append(" FROM party_struct ps");
            sqlFrom.append(" JOIN task_info b ON ps.CHILD_ENTITY_ID = b.ASSIGNEE");
            sqlFrom.append(" JOIN kv_record r ON b.BUSINESS_KEY = r.id");
            sqlFrom.append(" JOIN task_info_approve_position tap ON b.ID = tap.task_id");
            sqlFrom.append(" JOIN party_entity pe ON pe.id = tap.position_parentId");
            sqlFrom.append(" WHERE pe.id IN (").append(departmentId).append(")");
            sqlFrom.append(" AND b.CATALOG <> 'start' AND b.CATALOG <> 'copy' AND b.`STATUS` = 'complete'");
            sqlFrom.append(" GROUP BY b.BUSINESS_KEY) s");
            sqlFrom.append(" JOIN kv_record k ON s.BUSINESS_KEY = k.id");
            sqlFrom.append(" JOIN account_info a ON k.USER_ID = a.ID");
            sqlFrom.append(" WHERE 1=1) t");
            
            StringBuffer sqlLeftFrom = new StringBuffer();
            sqlLeftFrom.append(" left join (SELECT MAX(ti.COMPLETE_TIME) as COMPLETE_TIME,ti.BUSINESS_KEY FROM task_info ti");
            sqlLeftFrom.append(" WHERE ti.`STATUS` = 'complete' and ti.CATALOG = 'normal' group by ti.BUSINESS_KEY ) ti on t.business_key = ti.BUSINESS_KEY");
            
            sqlPagedQuerySelect = "select " + sqlSelect.toString() + " from" + sqlFrom.toString() + sqlLeftFrom.toString() + " where 1=1 ";
            sqlPagedQueryCount = "select count(1) from " + sqlFrom.toString() + " where 1=1 ";
        } 
        StringBuilder buff = new StringBuilder();
        List<Object> paramList = new ArrayList<Object>();
        boolean checkWhere = sqlPagedQuerySelect.toLowerCase().indexOf("where") == -1;
        PropertyFilterUtils.buildConfigurations(propertyFilters, buff, paramList, checkWhere);

        String sql = buff.toString();
        String countSql = sqlPagedQueryCount + " " + sql;
        String selectSql = sqlPagedQuerySelect + " " + sql + " order by create_time DESC limit " + page.getStart() + ","
                + page.getPageSize();

        logger.debug("countSql : {}", countSql);
        logger.debug("selectSql : {}", selectSql);

        Object[] params = paramList.toArray();
        int totalCount = jdbcTemplate.queryForObject(countSql, Integer.class, params);
        List<Map<String, Object>> list = jdbcTemplate.queryForList(selectSql, params);
        List<UnfinishProcessInstance> unfinishPros = new ArrayList<UnfinishProcessInstance>();

        for (Map<String, Object> map : list) {
            UnfinishProcessInstance uModelInstance = convertUnfinishProsDTOTemp(map);
            uModelInstance
                    .setStatus(dictConnector.findDictNameByValue("RecordStatus", map.get("audit_status").toString()));
            unfinishPros.add(uModelInstance);
        }

        page.setTotalCount(totalCount);
        page.setResult(unfinishPros);

        return page;
    }

    /**
     * 导出部门审批.
     */
    public Page exportDepartmentApproval(String userId, String tenantId, String departmentId,
                                         List<PropertyFilter> propertyFilters, Page page, String checkArea) {
        String sqlPagedQuerySelect = "";
        if (checkArea.equals("0")) {
//            sqlPagedQuerySelect = "SELECT * ,(SELECT MAX( COMPLETE_TIME ) FROM `task_info` ti WHERE t.BUSINESS_KEY = ti.BUSINESS_KEY AND `STATUS` = 'complete' AND CATALOG NOT IN ( 'start', 'copy' )) AS COMPLETE_TIME "
//                    + " FROM (SELECT k.*,a.DISPLAY_NAME,s.PROCESS_INSTANCE_ID,s.presentation_subject ,s.suspend_Status,k.audit_status as pro_status"
//                    + " from (select a.DEPARTMENT_CODE,b.ASSIGNEE,BUSINESS_KEY,b.PROCESS_INSTANCE_ID,b.presentation_subject,b.suspend_Status , min(b.TASK_ID) "
//                    + " from person_info a join task_info b on a.id = b.ASSIGNEE WHERE DEPARTMENT_CODE  ='" + departmentId
//                    + "'"
//                    + " and b.CATALOG <>'start' and b.CATALOG <>'copy' and b.`STATUS`='complete'  GROUP BY b.BUSINESS_KEY) s"
//                    + " join kv_record k on s.BUSINESS_KEY = k.id" + " join account_info  a on k.USER_ID = a.ID"
//                    + " where k.USER_ID<>s.ASSIGNEE ) t where 1=1 ";
            
        	StringBuffer sqlSelect = new StringBuffer();
            sqlSelect.append("*,ti.COMPLETE_TIME");
            
            StringBuffer sqlFrom = new StringBuffer();
            sqlFrom.append("(SELECT k.*, a.DISPLAY_NAME,s.PROCESS_INSTANCE_ID,s.presentation_subject,s.suspend_Status,k.audit_status AS pro_status");
            sqlFrom.append(" FROM( SELECT b.ASSIGNEE,b.BUSINESS_KEY,b.PROCESS_INSTANCE_ID,b.presentation_subject,b.suspend_Status,min(b.TASK_ID)");
            sqlFrom.append(" FROM party_struct ps");
            sqlFrom.append(" JOIN task_info b ON ps.CHILD_ENTITY_ID = b.ASSIGNEE");
            sqlFrom.append(" JOIN kv_record r ON b.BUSINESS_KEY = r.id");
            sqlFrom.append(" JOIN task_info_approve_position tap ON b.ID = tap.task_id");
            sqlFrom.append(" JOIN party_entity pe ON pe.id = tap.position_parentId");
            sqlFrom.append(" WHERE pe.id IN (").append(departmentId).append(")");
            sqlFrom.append(" AND b.CATALOG <> 'start' AND b.CATALOG <> 'copy' AND b.`STATUS` = 'complete'");
            sqlFrom.append(" GROUP BY b.BUSINESS_KEY) s");
            sqlFrom.append(" JOIN kv_record k ON s.BUSINESS_KEY = k.id");
            sqlFrom.append(" JOIN account_info a ON k.USER_ID = a.ID");
            sqlFrom.append(" WHERE 1=1) t");
            
            StringBuffer sqlLeftFrom = new StringBuffer();
            sqlLeftFrom.append(" left join (SELECT MAX(ti.COMPLETE_TIME) as COMPLETE_TIME,ti.BUSINESS_KEY FROM task_info ti");
            sqlLeftFrom.append(" WHERE ti.`STATUS` = 'complete' and ti.CATALOG = 'normal' group by ti.BUSINESS_KEY ) ti on t.business_key = ti.BUSINESS_KEY");
            
            sqlPagedQuerySelect = "select " + sqlSelect.toString() + " from" + sqlFrom.toString() + sqlLeftFrom.toString() + " where 1=1 ";
            
            
        } 
        StringBuilder buff = new StringBuilder();
        List<Object> paramList = new ArrayList<Object>();
        boolean checkWhere = sqlPagedQuerySelect.toLowerCase().indexOf("where") == -1;
        PropertyFilterUtils.buildConfigurations(propertyFilters, buff, paramList, checkWhere);

        String sql = buff.toString();
        String selectSql = sqlPagedQuerySelect + " " + sql + " order by create_time DESC ";

        Object[] params = paramList.toArray();
        List<Map<String, Object>> list = jdbcTemplate.queryForList(selectSql, params);
        List<UnfinishProcessInstance> unfinishPros = new ArrayList<UnfinishProcessInstance>();

        for (Map<String, Object> map : list) {
            UnfinishProcessInstance uModelInstance = convertUnfinishProsDTOTemp(map);
            uModelInstance
                    .setStatus(dictConnector.findDictNameByValue("RecordStatus", map.get("audit_status").toString()));
            unfinishPros.add(uModelInstance);
        }

        page.setResult(unfinishPros);

        return page;
    }
    /**
     * 导出部门审批主表数据
     * @author ckx
     */
    @Override
	public Page exportDepartmentApprovalDetail(String userId, String tenantId,
			String departmentId, List<PropertyFilter> propertyFilters,
			Page page, String checkArea, String formName) {
	String sqlPagedQuerySelect = "";
		
		String table = "";
    	String entity ="";
    	String []arr = this.tableAndEntity(formName,table,entity);
		table = arr[0];
    	entity = arr[1];
		
		
    	if (checkArea.equals("0")) {
            sqlPagedQuerySelect = "SELECT f_GetFirstAuditComment (t.BUSINESS_KEY,t.businesstypeid,t.businessdetailid) AS COMMENT, t.* ,ti.COMPLETE_TIME "
                    + " FROM (SELECT e.*,k.applyCode as k_applyCode,k.systemId,k.apply_content k_apply_content,k.areaId,k.areaName,k.audit_status,k.businessDetailId,k.businessTypeId,k.BUSINESS_KEY,k.CREATE_TIME,k.end_time, k.theme as k_theme,k.ucode as k_ucode,k.url,k.companyId,a.DISPLAY_NAME,s.b_PROCESS_INSTANCE_ID as s_process_instance_id,s.presentation_subject ,s.suspend_Status,k.audit_status as pro_status"
                    + " from (select b.ASSIGNEE,b.BUSINESS_KEY,b.PROCESS_INSTANCE_ID as b_process_instance_id,b.presentation_subject,b.suspend_Status , min(b.TASK_ID) "
                    + " from person_info a join task_info b on a.id = b.ASSIGNEE JOIN party_struct ps ON ps.CHILD_ENTITY_ID = b.ASSIGNEE JOIN kv_record r ON b.BUSINESS_KEY = r.id 	"
                    + "JOIN task_info_approve_position tap ON b.ID = tap.task_id "
                    + "JOIN party_entity pe ON pe.id = tap.position_parentId "
                    + "WHERE pe.id IN (" + departmentId
                    + ")"
                    + " and b.CATALOG <>'start' and b.CATALOG <>'copy' and b.`STATUS`='complete'  GROUP BY b.BUSINESS_KEY) s"
                    + " join kv_record k on s.BUSINESS_KEY = k.id" + " join account_info  a on k.USER_ID = a.ID "+table
                    + "  ) t" 
                    + " left join (SELECT MAX(ti.COMPLETE_TIME) as COMPLETE_TIME,ti.BUSINESS_KEY FROM task_info ti"
                    + " WHERE ti.`STATUS` = 'complete' and ti.CATALOG = 'normal' group by ti.BUSINESS_KEY ) ti on t.business_key = ti.BUSINESS_KEY"
                    + " where 1=1 ";
        } 
//    	else {
//            sqlPagedQuerySelect = "SELECT f_GetFirstAuditComment (t.BUSINESS_KEY,t.businesstypeid,t.businessdetailid) AS COMMENT, t.* ,(SELECT MAX( COMPLETE_TIME ) FROM `task_info` ti WHERE t.BUSINESS_KEY = ti.BUSINESS_KEY AND `STATUS` = 'complete' AND CATALOG NOT IN ( 'start', 'copy' )) AS COMPLETE_TIME "
//                    + " FROM (SELECT e.*,k.applyCode as k_applyCode,k.systemId,k.apply_content,k.areaId,k.areaName,k.audit_status,k.businessDetailId,k.businessTypeId,k.BUSINESS_KEY,k.CREATE_TIME,k.end_time, k.theme as k_theme,k.ucode as k_ucode,k.url,k.companyId,a.DISPLAY_NAME,s.b_PROCESS_INSTANCE_ID as s_process_instance_id,s.presentation_subject ,s.suspend_Status,k.audit_status as pro_status"
//                    + " from (select b.ASSIGNEE,BUSINESS_KEY,b.PROCESS_INSTANCE_ID as b_process_instance_id,b.presentation_subject,b.suspend_Status , min(b.TASK_ID) "
//                    + " from person_info a join task_info b on a.id = b.ASSIGNEE"
//                    + " inner JOIN party_struct s on s.CHILD_ENTITY_ID=a.id"
//                    + " WHERE s.PARENT_ENTITY_ID  ='" + departmentId
//                    + "'"
//                    + " and b.CATALOG <>'start' and b.CATALOG <>'copy' and b.`STATUS`='complete'  GROUP BY b.BUSINESS_KEY) s"
//                    + " join kv_record k on s.BUSINESS_KEY = k.id" + " join account_info  a on k.USER_ID = a.ID "+table
//                    + " where k.USER_ID<>s.ASSIGNEE ) t where 1=1 ";
//        }
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
        String selectSql = sqlPagedQuerySelect + " " + sql + " order by create_time DESC  ";

        Object[] params = paramList.toArray();
        List<Map<String, Object>> list = jdbcTemplate.queryForList(selectSql, params);
    	this.confirmEntity(entity, page, list);

        return page;
	}
    /**
     * 公司申请.
     */
    public Page findCompanyApplication(String userId, String tenantId, String companyId,
                                       List<PropertyFilter> propertyFilters, Page page, String checkArea) {
        String sqlPagedQuerySelect = "";
        String sqlPagedQueryCount = "";
        if (checkArea.equals("0")) {
            sqlPagedQuerySelect = "SELECT * ,(SELECT MAX( COMPLETE_TIME ) FROM `task_info` ti WHERE t.BUSINESS_KEY = ti.BUSINESS_KEY AND `STATUS` = 'complete' AND CATALOG NOT IN ( 'start', 'copy' )) AS COMPLETE_TIME "
                    + " FROM ( SELECT p.FULL_NAME,k.*,t.CATALOG,t.process_instance_id ,t.presentation_subject ,"
                    + " t.suspend_Status ,k.audit_status as pro_status from kv_record k"
                    + " join task_info t on k.id=t.BUSINESS_KEY" + " join person_info p on p.id=k.USER_ID"
                    + " where t.CATALOG='start' and p.company_code='" + companyId + "') t where 1=1 ";

            sqlPagedQueryCount = "SELECT COUNT(1) from (SELECT "
                    + " p.FULL_NAME,k.*,t.CATALOG,t.process_instance_id ,t.presentation_subject,  "
                    + " t.suspend_Status ,k.audit_status as pro_status FROM kv_record k "
                    + " join task_info t on k.id=t.BUSINESS_KEY " + " join person_info p on p.id=k.USER_ID"
                    + " where t.CATALOG='start' and p.company_code='" + companyId + "') t where 1=1 ";
        } else {
            sqlPagedQuerySelect = "SELECT * ,(SELECT MAX( COMPLETE_TIME ) FROM `task_info` ti WHERE t.BUSINESS_KEY = ti.BUSINESS_KEY AND `STATUS` = 'complete' AND CATALOG NOT IN ( 'start', 'copy' )) AS COMPLETE_TIME "
                    + " FROM ( SELECT p.FULL_NAME,k.*,t.CATALOG,t.process_instance_id ,t.presentation_subject ,"
                    + " t.suspend_Status ,k.audit_status as pro_status from kv_record k"
                    + " join task_info t on k.id=t.BUSINESS_KEY" + " join person_info p on p.id=k.USER_ID"
                    + " where t.CATALOG='start' and p.DEPARTMENT_CODE='" + companyId + "') t where 1=1 ";

            sqlPagedQueryCount = "SELECT COUNT(1) from (SELECT "
                    + " p.FULL_NAME,k.*,t.CATALOG,t.process_instance_id ,t.presentation_subject,  "
                    + " t.suspend_Status ,k.audit_status as pro_status FROM kv_record k "
                    + " join task_info t on k.id=t.BUSINESS_KEY " + " join person_info p on p.id=k.USER_ID"
                    + " where t.CATALOG='start' and p.DEPARTMENT_CODE='" + companyId + "') t where 1=1 ";
        }
        StringBuilder buff = new StringBuilder();
        List<Object> paramList = new ArrayList<Object>();
        boolean checkWhere = sqlPagedQuerySelect.toLowerCase().indexOf("where") == -1;
        PropertyFilterUtils.buildConfigurations(propertyFilters, buff, paramList, checkWhere);

        String sql = buff.toString();
        String countSql = sqlPagedQueryCount + " " + sql;
        String selectSql = sqlPagedQuerySelect + " " + sql + " order by create_time DESC limit " + page.getStart() + ","
                + page.getPageSize();

        logger.debug("countSql : {}", countSql);
        logger.debug("selectSql : {}", selectSql);

        Object[] params = paramList.toArray();
        int totalCount = jdbcTemplate.queryForObject(countSql, Integer.class, params);
        List<Map<String, Object>> list = jdbcTemplate.queryForList(selectSql, params);
        List<UnfinishProcessInstance> unfinishPros = new ArrayList<UnfinishProcessInstance>();

        for (Map<String, Object> map : list) {
            UnfinishProcessInstance uModelInstance = convertUnfinishProsDTO(map);
            uModelInstance
                    .setStatus(dictConnector.findDictNameByValue("RecordStatus", map.get("audit_status").toString()));
            unfinishPros.add(uModelInstance);
        }

        page.setTotalCount(totalCount);
        page.setResult(unfinishPros);

        return page;
    }

    /**
     * 导出公司申请.
     */
    public Page exportCompanyApplication(String userId, String tenantId, String companyId,
                                         List<PropertyFilter> propertyFilters, Page page, String checkArea) {
        String sqlPagedQuerySelect = "";
        if (checkArea.equals("0")) {
            sqlPagedQuerySelect = "SELECT * ,(SELECT MAX( COMPLETE_TIME ) FROM `task_info` ti WHERE t.BUSINESS_KEY = ti.BUSINESS_KEY AND `STATUS` = 'complete' AND CATALOG NOT IN ( 'start', 'copy' )) AS COMPLETE_TIME "
                    + " FROM ( SELECT p.FULL_NAME,k.*,t.CATALOG,t.process_instance_id ,t.presentation_subject ,"
                    + " t.suspend_Status ,k.audit_status as pro_status from kv_record k"
                    + " join task_info t on k.id=t.BUSINESS_KEY" + " join person_info p on p.id=k.USER_ID"
                    + " where t.CATALOG='start' and p.company_code='" + companyId + "') t where 1=1 ";
        } else {
            sqlPagedQuerySelect = "SELECT * ,(SELECT MAX( COMPLETE_TIME ) FROM `task_info` ti WHERE t.BUSINESS_KEY = ti.BUSINESS_KEY AND `STATUS` = 'complete' AND CATALOG NOT IN ( 'start', 'copy' )) AS COMPLETE_TIME "
                    + " FROM ( SELECT p.FULL_NAME,k.*,t.CATALOG,t.process_instance_id ,t.presentation_subject ,"
                    + " t.suspend_Status ,k.audit_status as pro_status from kv_record k"
                    + " join task_info t on k.id=t.BUSINESS_KEY" + " join person_info p on p.id=k.USER_ID"
                    + " where t.CATALOG='start' and p.=DEPARTMENT_CODE'" + companyId + "') t where 1=1 ";
        }
        StringBuilder buff = new StringBuilder();
        List<Object> paramList = new ArrayList<Object>();
        boolean checkWhere = sqlPagedQuerySelect.toLowerCase().indexOf("where") == -1;
        PropertyFilterUtils.buildConfigurations(propertyFilters, buff, paramList, checkWhere);

        String sql = buff.toString();
        String selectSql = sqlPagedQuerySelect + " " + sql + " order by create_time DESC ";
        Object[] params = paramList.toArray();
        List<Map<String, Object>> list = jdbcTemplate.queryForList(selectSql, params);
        List<UnfinishProcessInstance> unfinishPros = new ArrayList<UnfinishProcessInstance>();

        for (Map<String, Object> map : list) {
            UnfinishProcessInstance uModelInstance = convertUnfinishProsDTO(map);
            uModelInstance
                    .setStatus(dictConnector.findDictNameByValue("RecordStatus", map.get("audit_status").toString()));
            unfinishPros.add(uModelInstance);
        }
        page.setResult(unfinishPros);

        return page;
    }
    /**
     * 导出公司申请主表数据
     * @author ckx
     */
    @Override
	public Page exportCompanyApplicationDetail(String userId, String tenantId,
			String companyId, List<PropertyFilter> propertyFilters, Page page,
			String checkArea, String formName) {
		String sqlPagedQuerySelect = "";
		String table = "";
    	String entity ="";
    	String []arr = this.tableAndEntity(formName,table,entity);
		table = arr[0];
    	entity = arr[1];
	 
    	if (checkArea.equals("0")) {
            sqlPagedQuerySelect = "SELECT f_GetFirstAuditComment (t.BUSINESS_KEY,t.businesstypeid,t.businessdetailid) AS COMMENT, t.* ,(SELECT MAX( COMPLETE_TIME ) FROM `task_info` ti WHERE t.BUSINESS_KEY = ti.BUSINESS_KEY AND `STATUS` = 'complete' AND CATALOG NOT IN ( 'start', 'copy' )) AS COMPLETE_TIME "
                    + " FROM ( SELECT e.*,k.applyCode as k_applyCode,k.systemId,k.apply_content as k_apply_content,k.areaId,k.areaName,k.audit_status,k.businessDetailId,k.businessTypeId,k.BUSINESS_KEY,k.CREATE_TIME,k.end_time, k.theme as k_theme,k.ucode as k_ucode,k.url,k.companyId, p.FULL_NAME,t.CATALOG,t.process_instance_id as t_process_instance_id ,t.presentation_subject ,"
                    + " t.suspend_Status ,k.audit_status as pro_status from kv_record k"
                    + " join task_info t on k.id=t.BUSINESS_KEY" + " join person_info p on p.id=k.USER_ID "+table
                    + " where t.CATALOG='start' and p.company_code='" + companyId + "') t where 1=1 ";
        } else {
            sqlPagedQuerySelect = "SELECT f_GetFirstAuditComment (t.BUSINESS_KEY,t.businesstypeid,t.businessdetailid) AS COMMENT, t.* ,(SELECT MAX( COMPLETE_TIME ) FROM `task_info` ti WHERE t.BUSINESS_KEY = ti.BUSINESS_KEY AND `STATUS` = 'complete' AND CATALOG NOT IN ( 'start', 'copy' )) AS COMPLETE_TIME "
                    + " FROM ( SELECT e.*,k.applyCode as k_applyCode,k.systemId,k.apply_content,k.areaId,k.areaName,k.audit_status,k.businessDetailId,k.businessTypeId,k.BUSINESS_KEY,k.CREATE_TIME,k.end_time, k.theme as k_theme,k.ucode as k_ucode,k.url,k.companyId, p.FULL_NAME,t.CATALOG,t.process_instance_id as t_process_instance_id,t.presentation_subject ,"
                    + " t.suspend_Status ,k.audit_status as pro_status from kv_record k"
                    + " join task_info t on k.id=t.BUSINESS_KEY" + " join person_info p on p.id=k.USER_ID "+table
                    + " where t.CATALOG='start' and p.DEPARTMENT_CODE='" + companyId + "') t where 1=1 ";
        }

        
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
        String selectSql = sqlPagedQuerySelect + " " + sql + " order by create_time DESC  ";

        Object[] params = paramList.toArray();
        List<Map<String, Object>> list = jdbcTemplate.queryForList(selectSql, params);
    	this.confirmEntity(entity, page, list);

        return page;
	}
    /**
     * 公司审批.
     */
    public Page findCompanyApproval(String userId, String tenantId, String companyId,
                                    List<PropertyFilter> propertyFilters, Page page, String checkArea) {
        String sqlPagedQuerySelect = "";
        String sqlPagedQueryCount = "";
        if (checkArea.equals("0")) {
            sqlPagedQuerySelect = "SELECT * ,(SELECT MAX( COMPLETE_TIME ) FROM `task_info` ti WHERE t.BUSINESS_KEY = ti.BUSINESS_KEY AND `STATUS` = 'complete' AND CATALOG NOT IN ( 'start', 'copy' )) AS COMPLETE_TIME "
                    + "FROM ( SELECT k.*,a.DISPLAY_NAME,s.process_instance_id ,s.presentation_subject,s.suspend_Status ,k.audit_status as pro_status "
                    + " from (select a.COMPANY_CODE,b.ASSIGNEE,BUSINESS_KEY,b.PROCESS_INSTANCE_ID,b.presentation_subject ,b.suspend_Status,min(b.TASK_ID) "
                    + " from person_info a join task_info b on a.id = b.ASSIGNEE WHERE COMPANY_CODE  ='" + companyId + "'"
                    + " and b.CATALOG <>'start' and b.CATALOG <>'copy' AND b.`STATUS`='complete' GROUP BY b.BUSINESS_KEY) s "
                    + " join kv_record k on s.BUSINESS_KEY = k.id " + " join account_info  a on k.USER_ID = a.ID "
                    + " where s.ASSIGNEE<>k.USER_ID ) t where 1=1 ";

            sqlPagedQueryCount = "SELECT COUNT(1) FROM ( SELECT "
                    + " k.*,a.DISPLAY_NAME,s.process_instance_id ,s.presentation_subject,s.suspend_Status,k.audit_status as pro_status "
                    + " from (select a.COMPANY_CODE,b.ASSIGNEE,BUSINESS_KEY,b.PROCESS_INSTANCE_ID,b.presentation_subject,b.suspend_Status,min(b.TASK_ID) "
                    + " from person_info a join task_info b on a.id = b.ASSIGNEE WHERE COMPANY_CODE  ='" + companyId + "'"
                    + " and b.CATALOG <>'start' and b.CATALOG <>'copy' AND b.`STATUS`='complete' GROUP BY b.BUSINESS_KEY) s "
                    + " join kv_record k on s.BUSINESS_KEY = k.id " + " join account_info  a on k.USER_ID = a.ID "
                    + " where s.ASSIGNEE<>k.USER_ID ) t where 1=1 ";
        } else {
            sqlPagedQuerySelect = "SELECT * ,(SELECT MAX( COMPLETE_TIME ) FROM `task_info` ti WHERE t.BUSINESS_KEY = ti.BUSINESS_KEY AND `STATUS` = 'complete' AND CATALOG NOT IN ( 'start', 'copy' )) AS COMPLETE_TIME "
                    + "FROM ( SELECT k.*,a.DISPLAY_NAME,s.process_instance_id ,s.presentation_subject,s.suspend_Status ,k.audit_status as pro_status "
                    + " from (select a.COMPANY_CODE,b.ASSIGNEE,BUSINESS_KEY,b.PROCESS_INSTANCE_ID,b.presentation_subject ,b.suspend_Status,min(b.TASK_ID) "
                    + " from person_info a join task_info b on a.id = b.ASSIGNEE WHERE a.DEPARTMENT_CODE  ='" + companyId + "'"
                    + " and b.CATALOG <>'start' and b.CATALOG <>'copy' AND b.`STATUS`='complete' GROUP BY b.BUSINESS_KEY) s "
                    + " join kv_record k on s.BUSINESS_KEY = k.id " + " join account_info  a on k.USER_ID = a.ID "
                    + " where s.ASSIGNEE<>k.USER_ID ) t where 1=1 ";

            sqlPagedQueryCount = "SELECT COUNT(1) FROM ( SELECT "
                    + " k.*,a.DISPLAY_NAME,s.process_instance_id ,s.presentation_subject,s.suspend_Status,k.audit_status as pro_status "
                    + " from (select a.COMPANY_CODE,b.ASSIGNEE,BUSINESS_KEY,b.PROCESS_INSTANCE_ID,b.presentation_subject,b.suspend_Status,min(b.TASK_ID) "
                    + " from person_info a join task_info b on a.id = b.ASSIGNEE WHERE a.DEPARTMENT_CODE  ='" + companyId + "'"
                    + " and b.CATALOG <>'start' and b.CATALOG <>'copy' AND b.`STATUS`='complete' GROUP BY b.BUSINESS_KEY) s "
                    + " join kv_record k on s.BUSINESS_KEY = k.id " + " join account_info  a on k.USER_ID = a.ID "
                    + " where s.ASSIGNEE<>k.USER_ID ) t where 1=1 ";
        }
        StringBuilder buff = new StringBuilder();
        List<Object> paramList = new ArrayList<Object>();
        boolean checkWhere = sqlPagedQuerySelect.toLowerCase().indexOf("where") == -1;
        PropertyFilterUtils.buildConfigurations(propertyFilters, buff, paramList, checkWhere);

        String sql = buff.toString();
        String countSql = sqlPagedQueryCount + " " + sql;
        String selectSql = sqlPagedQuerySelect + " " + sql + " order by create_time DESC limit " + page.getStart() + ","
                + page.getPageSize();

        logger.debug("countSql : {}", countSql);
        logger.debug("selectSql : {}", selectSql);

        Object[] params = paramList.toArray();
        int totalCount = jdbcTemplate.queryForObject(countSql, Integer.class, params);
        List<Map<String, Object>> list = jdbcTemplate.queryForList(selectSql, params);
        List<UnfinishProcessInstance> unfinishPros = new ArrayList<UnfinishProcessInstance>();

        for (Map<String, Object> map : list) {
            UnfinishProcessInstance uModelInstance = convertUnfinishProsDTOTemp(map);
            uModelInstance
                    .setStatus(dictConnector.findDictNameByValue("RecordStatus", map.get("audit_status").toString()));
            unfinishPros.add(uModelInstance);
        }

        page.setTotalCount(totalCount);
        page.setResult(unfinishPros);

        return page;
    }

    /**
     * 导出公司审批.
     */
    public Page exportCompanyApproval(String userId, String tenantId, String companyId,
                                      List<PropertyFilter> propertyFilters, Page page, String checkArea) {
        String sqlPagedQuerySelect = "";
        if (checkArea.equals("0")) {
            sqlPagedQuerySelect = "SELECT * ,(SELECT MAX( COMPLETE_TIME ) FROM `task_info` ti WHERE t.BUSINESS_KEY = ti.BUSINESS_KEY AND `STATUS` = 'complete' AND CATALOG NOT IN ( 'start', 'copy' )) AS COMPLETE_TIME "
                    + "FROM ( SELECT k.*,a.DISPLAY_NAME,s.process_instance_id ,s.presentation_subject,s.suspend_Status ,k.audit_status as pro_status "
                    + " from (select a.COMPANY_CODE,b.ASSIGNEE,BUSINESS_KEY,b.PROCESS_INSTANCE_ID,b.presentation_subject ,b.suspend_Status,min(b.TASK_ID) "
                    + " from person_info a join task_info b on a.id = b.ASSIGNEE WHERE COMPANY_CODE  ='" + companyId + "'"
                    + " and b.CATALOG <>'start' and b.CATALOG <>'copy' AND b.`STATUS`='complete' GROUP BY b.BUSINESS_KEY) s "
                    + " join kv_record k on s.BUSINESS_KEY = k.id " + " join account_info  a on k.USER_ID = a.ID "
                    + " where s.ASSIGNEE<>k.USER_ID ) t where 1=1 ";
        }else{
            sqlPagedQuerySelect = "SELECT * ,(SELECT MAX( COMPLETE_TIME ) FROM `task_info` ti WHERE t.BUSINESS_KEY = ti.BUSINESS_KEY AND `STATUS` = 'complete' AND CATALOG NOT IN ( 'start', 'copy' )) AS COMPLETE_TIME "
                    + "FROM ( SELECT k.*,a.DISPLAY_NAME,s.process_instance_id ,s.presentation_subject,s.suspend_Status ,k.audit_status as pro_status "
                    + " from (select a.COMPANY_CODE,b.ASSIGNEE,BUSINESS_KEY,b.PROCESS_INSTANCE_ID,b.presentation_subject ,b.suspend_Status,min(b.TASK_ID) "
                    + " from person_info a join task_info b on a.id = b.ASSIGNEE WHERE DEPARTMENT_CODE  ='" + companyId + "'"
                    + " and b.CATALOG <>'start' and b.CATALOG <>'copy' AND b.`STATUS`='complete' GROUP BY b.BUSINESS_KEY) s "
                    + " join kv_record k on s.BUSINESS_KEY = k.id " + " join account_info  a on k.USER_ID = a.ID "
                    + " where s.ASSIGNEE<>k.USER_ID ) t where 1=1 ";
        }
        StringBuilder buff = new StringBuilder();
        List<Object> paramList = new ArrayList<Object>();
        boolean checkWhere = sqlPagedQuerySelect.toLowerCase().indexOf("where") == -1;
        PropertyFilterUtils.buildConfigurations(propertyFilters, buff, paramList, checkWhere);

        String sql = buff.toString();
        String selectSql = sqlPagedQuerySelect + " " + sql + " order by create_time DESC ";

        Object[] params = paramList.toArray();
        List<Map<String, Object>> list = jdbcTemplate.queryForList(selectSql, params);
        List<UnfinishProcessInstance> unfinishPros = new ArrayList<UnfinishProcessInstance>();

        for (Map<String, Object> map : list) {
            UnfinishProcessInstance uModelInstance = convertUnfinishProsDTOTemp(map);
            uModelInstance
                    .setStatus(dictConnector.findDictNameByValue("RecordStatus", map.get("audit_status").toString()));
            unfinishPros.add(uModelInstance);
        }
        page.setResult(unfinishPros);

        return page;
    }
    /**
     * 导出公司审批主表数据
     * @author ckx
     */
    @Override
	public Page exportCompanyApprovalDetail(String userId, String tenantId,
			String companyId, List<PropertyFilter> propertyFilters, Page page,
			String checkArea, String formName) {
		String sqlPagedQuerySelect = "";
		String table = "";
    	String entity ="";
    	String []arr = this.tableAndEntity(formName,table,entity);
		table = arr[0];
    	entity = arr[1];
		
    	if (checkArea.equals("0")) {
            sqlPagedQuerySelect = "SELECT f_GetFirstAuditComment (t.BUSINESS_KEY,t.businesstypeid,t.businessdetailid) AS COMMENT, t.* ,(SELECT MAX( COMPLETE_TIME ) FROM `task_info` ti WHERE t.BUSINESS_KEY = ti.BUSINESS_KEY AND `STATUS` = 'complete' AND CATALOG NOT IN ( 'start', 'copy' )) AS COMPLETE_TIME "
                    + "FROM ( SELECT e.*,k.applyCode as k_applyCode,k.systemId,k.apply_content as k_apply_content,k.areaId,k.areaName,k.audit_status,k.businessDetailId,k.businessTypeId,k.BUSINESS_KEY,k.CREATE_TIME,k.end_time, k.theme as k_theme,k.ucode as k_ucode,k.url,k.companyId,a.DISPLAY_NAME,s.process_instance_id as s_process_instance_id,s.presentation_subject,s.suspend_Status ,k.audit_status as pro_status "
                    + " from (select a.COMPANY_CODE,b.ASSIGNEE,BUSINESS_KEY,b.PROCESS_INSTANCE_ID,b.presentation_subject ,b.suspend_Status,min(b.TASK_ID) "
                    + " from person_info a join task_info b on a.id = b.ASSIGNEE WHERE COMPANY_CODE  ='" + companyId + "'"
                    + " and b.CATALOG <>'start' and b.CATALOG <>'copy' AND b.`STATUS`='complete' GROUP BY b.BUSINESS_KEY) s "
                    + " join kv_record k on s.BUSINESS_KEY = k.id " + " join account_info  a on k.USER_ID = a.ID "+table
                    + " where s.ASSIGNEE<>k.USER_ID ) t where 1=1 ";
        }else{
            sqlPagedQuerySelect = "SELECT f_GetFirstAuditComment (t.BUSINESS_KEY,t.businesstypeid,t.businessdetailid) AS COMMENT, t.* ,(SELECT MAX( COMPLETE_TIME ) FROM `task_info` ti WHERE t.BUSINESS_KEY = ti.BUSINESS_KEY AND `STATUS` = 'complete' AND CATALOG NOT IN ( 'start', 'copy' )) AS COMPLETE_TIME "
                    + "FROM ( SELECT e.*,k.applyCode as k_applyCode,k.systemId,k.apply_content as k_apply_content,k.areaId,k.areaName,k.audit_status,k.businessDetailId,k.businessTypeId,k.BUSINESS_KEY,k.CREATE_TIME,k.end_time, k.theme as k_theme,k.ucode as k_ucode,k.url,k.companyId,a.DISPLAY_NAME,s.process_instance_id as s_process_instance_id,s.presentation_subject,s.suspend_Status ,k.audit_status as pro_status "
                    + " from (select a.COMPANY_CODE,b.ASSIGNEE,BUSINESS_KEY,b.PROCESS_INSTANCE_ID,b.presentation_subject ,b.suspend_Status,min(b.TASK_ID) "
                    + " from person_info a join task_info b on a.id = b.ASSIGNEE WHERE DEPARTMENT_CODE  ='" + companyId + "'"
                    + " and b.CATALOG <>'start' and b.CATALOG <>'copy' AND b.`STATUS`='complete' GROUP BY b.BUSINESS_KEY) s "
                    + " join kv_record k on s.BUSINESS_KEY = k.id " + " join account_info  a on k.USER_ID = a.ID "+table
                    + " where s.ASSIGNEE<>k.USER_ID ) t where 1=1 ";
        }
		
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
        String selectSql = sqlPagedQuerySelect + " " + sql + " order by create_time DESC  ";

        Object[] params = paramList.toArray();
        List<Map<String, Object>> list = jdbcTemplate.queryForList(selectSql, params);
    	this.confirmEntity(entity, page, list);

        return page;
	}

    /**
     * 已办任务.
     */
    public Page findFinishedTasks(String userId, String tenantId, int pageNo, int pageSize) {
        Page page = taskInfoManager.pagedQuery("from TaskInfo where assignee=? and tenantId=? and status='complete'",
                pageNo, pageSize, userId, tenantId);
        List<TaskInfo> taskInfos = (List<TaskInfo>) page.getResult();
        List<HumanTaskDTO> humanTaskDtos = this.convertHumanTaskDtos(taskInfos);

        page.setResult(humanTaskDtos);

        return page;
    }

    /**
     * 已办任务.
     */
    public Page findFinishedTasks(String userId, String tenantId, List<PropertyFilter> propertyFilters, Page page) {

//        String subsql = "SELECT * ,(SELECT MAX( COMPLETE_TIME ) FROM `task_info` ti WHERE t.BUSINESS_KEY = ti.BUSINESS_KEY AND `STATUS` = 'complete' AND CATALOG NOT IN ( 'start', 'copy' )) AS COMPLETE_TIME "
//                + " FROM ( SELECT k.*,a.DISPLAY_NAME as full_name,s.process_instance_id ,s.presentation_subject,s.suspend_Status ,k.audit_status as pro_status "
//                + " from (select b.ASSIGNEE,BUSINESS_KEY,b.PROCESS_INSTANCE_ID,b.presentation_subject ,b.suspend_Status,min(b.TASK_ID) "
//                + " from person_info a join task_info b on a.id = b.ASSIGNEE WHERE b.ASSIGNEE  ='" + userId + "'"
//                + " and b.CATALOG <>'start' and b.CATALOG <>'copy' AND b.`STATUS`='complete' GROUP BY b.BUSINESS_KEY) s "
//                + " join kv_record k on s.BUSINESS_KEY = k.id " + " join account_info  a on k.USER_ID = a.ID "
//                + " ) t where 1=1 ";
     
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
        	postID = userId;
        } else {
        	postID = postID + "," + userId;
        }
        /*String subsql = "SELECT * ,(SELECT MAX( COMPLETE_TIME ) FROM `task_info` ti WHERE t.BUSINESS_KEY = ti.BUSINESS_KEY AND `STATUS` = 'complete' AND CATALOG NOT IN ( 'start', 'copy' )) AS COMPLETE_TIME "
                + " FROM ( SELECT  DISTINCT k.*,a.DISPLAY_NAME as full_name,s.process_instance_id ,s.presentation_subject,s.suspend_Status ,k.audit_status as pro_status "
                + " from (select  DISTINCT b.BUSINESS_KEY,b.PROCESS_INSTANCE_ID,b.presentation_subject ,b.suspend_Status,min(b.TASK_ID) "
                + " FROM task_info b JOIN task_info_approve_position tap ON b.ID = tap.task_id WHERE tap.position_id in ('"+postID+"')"
                + " and b.CATALOG <>'start' and b.CATALOG <>'copy' AND b.`STATUS`='complete' GROUP BY b.BUSINESS_KEY) s "
                + " join kv_record k on s.BUSINESS_KEY = k.id " + " join account_info  a on k.USER_ID = a.ID "
                + " ) t where 1=1 ";*/
        
        StringBuffer subSql = new StringBuffer();
        subSql.append("SELECT * ");
        subSql.append(" FROM ( SELECT  DISTINCT k.*,a.DISPLAY_NAME as full_name,s.process_instance_id ,s.presentation_subject,s.suspend_Status ,k.audit_status as pro_status,s.assignee,s.person_create_time,s.person_complete_time ");
        subSql.append(" from (select  DISTINCT b.BUSINESS_KEY,b.PROCESS_INSTANCE_ID,b.presentation_subject ,b.suspend_Status,min(b.TASK_ID),(select FULL_NAME from person_info where id = b.assignee) as ASSIGNEE,");
        //经我审批列表添加审核时长 TODO sjx 18.01.14
        subSql.append("(SELECT DISTINCT max(info.CREATE_TIME) FROM task_info info where info.BUSINESS_KEY=r.BUSINESS_KEY and info.ASSIGNEE=b.ASSIGNEE) as person_create_time,(SELECT DISTINCT max(info.COMPLETE_TIME) FROM task_info info where info.BUSINESS_KEY=r.BUSINESS_KEY and info.ASSIGNEE=b.ASSIGNEE) as person_complete_time");
        subSql.append(" FROM task_info b JOIN task_info_approve_position tap ON b.ID = tap.task_id");
        subSql.append(" inner JOIN kv_record r ON b.BUSINESS_KEY = r.id");
        subSql.append(" WHERE tap.position_id in (").append(postID).append(")");
        subSql.append(" and b.CATALOG <>'start' and b.CATALOG <>'copy' AND b.`STATUS`='complete' GROUP BY b.BUSINESS_KEY) s");
        subSql.append(" join kv_record k on s.BUSINESS_KEY = k.id " + " join account_info  a on k.USER_ID = a.ID) t where 1=1 ");
        
        
        String sqlPagedQuerySelect = " SELECT *,ti.COMPLETE_TIME FROM (" + subSql.toString() + ") t" 
        		+ " left join (SELECT MAX(ti.COMPLETE_TIME) as COMPLETE_TIME,ti.BUSINESS_KEY FROM task_info ti"
		        + " WHERE ti.`STATUS` = 'complete' and ti.CATALOG = 'normal' group by ti.BUSINESS_KEY ) ti on t.business_key = ti.BUSINESS_KEY"
        		+ " where 1=1 ";

        String sqlPagedQueryCount = " SELECT count(1) FROM (" + subSql.toString() + ") t where 1=1 ";

        StringBuilder buff = new StringBuilder();
        List<Object> paramList = new ArrayList<Object>();
        boolean checkWhere = sqlPagedQuerySelect.toLowerCase().indexOf("where") == -1;
        PropertyFilterUtils.buildConfigurations(propertyFilters, buff, paramList, checkWhere);

        String sql = buff.toString();
        String countSql = sqlPagedQueryCount + " " + sql;
        String selectSql = sqlPagedQuerySelect + " " + sql + " order by COMPLETE_TIME DESC limit " + page.getStart() + ","
                + page.getPageSize();

        logger.debug("countSql : {}", countSql);
        logger.debug("selectSql : {}", selectSql);
        // System.out.println(selectSql);

        Object[] params = paramList.toArray();
        int totalCount = jdbcTemplate.queryForObject(countSql, Integer.class, params);
        List<Map<String, Object>> list = jdbcTemplate.queryForList(selectSql, params);
        List<UnfinishProcessInstance> unfinishPros = new ArrayList<UnfinishProcessInstance>();
        String personCreateTime = "";
        String personCompleteTime = "";
        String auditDuration = "";
        long timeDifference = 0;
        for (Map<String, Object> map : list) {
        	personCreateTime = map.get("person_create_time").toString();
        	personCompleteTime = map.get("person_complete_time").toString();
        	timeDifference = DateUtil.getTimeDifference(personCompleteTime, personCreateTime);
        	auditDuration = DateUtil.secondsToTime(timeDifference);
        	map.put("auditDuration", auditDuration);
            UnfinishProcessInstance uModelInstance = convertUnfinishProsDTO(map);
            uModelInstance
                    .setStatus(dictConnector.findDictNameByValue("RecordStatus", map.get("pro_status").toString()));
            unfinishPros.add(uModelInstance);
        }

        page.setTotalCount(totalCount);
        page.setResult(unfinishPros);
        
        return page;

    }

    /**
     * 导出已办任务（经我审批）.
     */
    public Page exportFinishedTasks(String userId, String tenantId, List<PropertyFilter> propertyFilters, Page page) {

    	
//        String sqlPagedQuerySelect = "SELECT * ,(SELECT MAX( COMPLETE_TIME ) FROM `task_info` ti WHERE t.BUSINESS_KEY = ti.BUSINESS_KEY AND `STATUS` = 'complete' AND CATALOG NOT IN ( 'start', 'copy' )) AS COMPLETE_TIME "
//                + " FROM ( SELECT k.*,a.DISPLAY_NAME as full_name,s.process_instance_id ,s.presentation_subject,s.suspend_Status ,k.audit_status as pro_status "
//                + " from (select b.ASSIGNEE,BUSINESS_KEY,b.PROCESS_INSTANCE_ID,b.presentation_subject ,b.suspend_Status,min(b.TASK_ID) "
//                + " from person_info a join task_info b on a.id = b.ASSIGNEE WHERE b.ASSIGNEE  ='" + userId + "'"
//                + " and b.CATALOG <>'start' and b.CATALOG <>'copy' AND b.`STATUS`='complete' GROUP BY b.BUSINESS_KEY) s "
//                + " join kv_record k on s.BUSINESS_KEY = k.id " + " join account_info  a on k.USER_ID = a.ID "
//                + " ) t where 1=1 ";
        
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
        	postID = userId;
        } else {
        	postID = postID + "," + userId;
        }
        
        
        StringBuffer subSql = new StringBuffer();
        subSql.append("SELECT *");
        subSql.append(" FROM ( SELECT  DISTINCT k.*,a.DISPLAY_NAME as full_name,s.process_instance_id ,s.presentation_subject,s.suspend_Status ,k.audit_status as pro_status,s.assignee,s.person_create_time,s.person_complete_time ");
        subSql.append(" from (select  DISTINCT b.BUSINESS_KEY,b.PROCESS_INSTANCE_ID,b.presentation_subject ,b.suspend_Status,min(b.TASK_ID),(select FULL_NAME from person_info where id = b.ASSIGNEE) as assignee,");
        subSql.append("(SELECT DISTINCT max(info.CREATE_TIME) FROM task_info info where info.BUSINESS_KEY=r.BUSINESS_KEY and info.ASSIGNEE=b.ASSIGNEE) as person_create_time,(SELECT DISTINCT max(info.COMPLETE_TIME) FROM task_info info where info.BUSINESS_KEY=r.BUSINESS_KEY and info.ASSIGNEE=b.ASSIGNEE) as person_complete_time");
        subSql.append(" FROM task_info b JOIN task_info_approve_position tap ON b.ID = tap.task_id");
        subSql.append(" inner JOIN kv_record r ON b.BUSINESS_KEY = r.id");
        subSql.append(" WHERE tap.position_id in (").append(postID).append(")");
        subSql.append(" and b.CATALOG <>'start' and b.CATALOG <>'copy' AND b.`STATUS`='complete' GROUP BY b.BUSINESS_KEY) s");
        subSql.append(" join kv_record k on s.BUSINESS_KEY = k.id " + " join account_info  a on k.USER_ID = a.ID) t where 1=1 ");
        

        String sqlPagedQuerySelect = " SELECT *,ti.COMPLETE_TIME FROM (" + subSql.toString() + ") t"
        		+ " left join (SELECT MAX(ti.COMPLETE_TIME) as COMPLETE_TIME,ti.BUSINESS_KEY FROM task_info ti"
		        + " WHERE ti.`STATUS` = 'complete' and ti.CATALOG = 'normal' group by ti.BUSINESS_KEY ) ti on t.business_key = ti.BUSINESS_KEY"
                + " where 1=1 ";
        
        StringBuilder buff = new StringBuilder();
        List<Object> paramList = new ArrayList<Object>();
        boolean checkWhere = sqlPagedQuerySelect.toLowerCase().indexOf("where") == -1;
        PropertyFilterUtils.buildConfigurations(propertyFilters, buff, paramList, checkWhere);

        String sql = buff.toString();
        String selectSql = sqlPagedQuerySelect + " " + sql + " order by COMPLETE_TIME DESC ";

        Object[] params = paramList.toArray();
        List<Map<String, Object>> list = jdbcTemplate.queryForList(selectSql, params);
        List<UnfinishProcessInstance> unfinishPros = new ArrayList<UnfinishProcessInstance>();
        String personCreateTime = "";
        String personCompleteTime = "";
        String auditDuration = "";
        long timeDifference = 0;
        for (Map<String, Object> map : list) {
        	personCreateTime = map.get("person_create_time").toString();
        	personCompleteTime = map.get("person_complete_time").toString();
        	timeDifference = DateUtil.getTimeDifference(personCompleteTime, personCreateTime);
        	auditDuration = DateUtil.secondsToTime(timeDifference);
        	map.put("auditDuration", auditDuration);
            UnfinishProcessInstance uModelInstance = convertUnfinishProsDTO(map);
            uModelInstance
                    .setStatus(dictConnector.findDictNameByValue("RecordStatus", map.get("pro_status").toString()));
            unfinishPros.add(uModelInstance);
        }
        page.setResult(unfinishPros);

        return page;

    }
    
    /**
     * 导出条件中带有业务细分，则导出该细分下对应的表单数据（导出已办菜单数据）
     * 
     */
    public Page exportFinishedTasksBydetail(String userId, String tenantId, List<PropertyFilter> propertyFilters, Page page,String formName){
    
    	String table = "";
    	String entity ="";
    	String []arr = this.tableAndEntity(formName,table,entity);
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
//        if (StringUtils.isBlank(postID)) {
//        	postID = "9999";
//        }

        if (StringUtils.isBlank(postID)) {
        	postID = userId;
        } else {
        	postID = postID + "," + userId;
        }
        
        
    	String sqlPagedQuerySelect = "SELECT f_GetFirstAuditComment(t.BUSINESS_KEY,t.businesstypeid,t.businessdetailid) as comment, t.* ,ti.COMPLETE_TIME "
                + " FROM ( SELECT k.`BUSINESS_KEY`,k.`businessDetailId`,k.`businessdetailname`,k.`businessTypeId`,k.`applyCode` kapplyCode,k.`areaId`,k.`companyId`,k.`theme` ktheme,k.`ucode` kucode,k.`systemId`,k.`CREATE_TIME`,e.*,a.DISPLAY_NAME as full_name,s.presentation_subject,s.suspend_Status ,k.audit_status as pro_status,s.assignee "
                + " from (select  DISTINCT b.BUSINESS_KEY,b.PROCESS_INSTANCE_ID,b.presentation_subject ,b.suspend_Status,min(b.TASK_ID),(select FULL_NAME from person_info where id = b.ASSIGNEE) as assignee "
                + " FROM task_info b JOIN task_info_approve_position tap ON b.ID = tap.task_id"
                + " inner JOIN kv_record r ON b.BUSINESS_KEY = r.id"
                + " WHERE tap.position_id in ("+postID+") "
                + " and b.CATALOG <>'start' and b.CATALOG <>'copy' AND b.`STATUS`='complete' and r.businessTypeId <> '9999' GROUP BY b.BUSINESS_KEY"
                + " union all"
                + " select  DISTINCT b.BUSINESS_KEY,b.PROCESS_INSTANCE_ID,b.presentation_subject ,b.suspend_Status,min(b.TASK_ID),(select FULL_NAME from person_info where id = b.ASSIGNEE) as assignee "
                + " FROM task_info b JOIN task_info_approve_position tap ON b.ID = tap.task_id"
                + " inner JOIN kv_record r ON b.BUSINESS_KEY = r.id"
                + " WHERE tap.position_id in ("+postID+","+userId+")"
                + " and b.CATALOG <>'start' and b.CATALOG <>'copy' AND b.`STATUS`='complete' and r.businessTypeId = '9999' GROUP BY b.BUSINESS_KEY) s "
                + " join kv_record k on s.BUSINESS_KEY = k.id " + " join account_info  a on k.USER_ID = a.ID "+table
                + " ) t"
                + " left join (SELECT MAX(ti.COMPLETE_TIME) as COMPLETE_TIME,ti.BUSINESS_KEY FROM task_info ti"
		        + " WHERE ti.`STATUS` = 'complete' and ti.CATALOG = 'normal' group by ti.BUSINESS_KEY ) ti on t.business_key = ti.BUSINESS_KEY"
                + " where 1=1 ";
    	StringBuilder buff = new StringBuilder();
        List<Object> paramList = new ArrayList<Object>();
        boolean checkWhere = sqlPagedQuerySelect.toLowerCase().indexOf("where") == -1;
        PropertyFilterUtils.buildConfigurations(propertyFilters, buff, paramList, checkWhere);

        String sql = buff.toString();
        String [] param = sql.split("and");
        StringBuffer sb = new StringBuffer();
        for(int i = 0;i < param.length;i++){
        	if(param[i].indexOf("theme like ?")>=0){
        		param[i] = "ktheme like ?";
        	}
        	if(param[i].indexOf("ucode like ?")>=0){
        		param[i] = "kucode like ?";
        	}
        	if(param[i].indexOf("applyCode like ?")>=0){
        		param[i] = "kapplyCode like ?";
        	}
        	sb.append(" "+param[i]);
        	if(i != param.length-1){
        		sb.append(" and");
        	}
        }
        String selectSql = sqlPagedQuerySelect + " " + sb + " order by COMPLETE_TIME DESC ";
        
        Object[] params = paramList.toArray();
        List<Map<String, Object>> list = jdbcTemplate.queryForList(selectSql,params);
        this.confirmEntity(entity, page, list);
    	return page;
    }
    /**
     * 待领任务.
     */
    public Page findGroupTasks(String userId, String tenantId, int pageNo, int pageSize) {
        List<String> partyIds = new ArrayList<String>();
        partyIds.addAll(this.findGroupIds(userId));
        partyIds.addAll(this.findUserIds(userId));

        logger.debug("party ids : {}", partyIds);

        if (partyIds.isEmpty()) {
            return new Page();
        }

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("partyIds", partyIds);
        map.put("tenantId", tenantId);

        String hql = "select distinct t from TaskInfo t join t.taskParticipants p with p.ref in (:partyIds) where t.tenantId=:tenantId and t.assignee=null and t.status='active'";
        Page page = taskInfoManager.pagedQuery(hql, pageNo, pageSize, map);

        // List<PropertyFilter> propertyFilters = PropertyFilter
        // .buildFromMap(parameterMap);
        // propertyFilters.add(new PropertyFilter("EQS_status", "active"));
        // propertyFilters.add(new PropertyFilter("INLS_assignee", null));
        List<TaskInfo> taskInfos = (List<TaskInfo>) page.getResult();
        List<HumanTaskDTO> humanTaskDtos = this.convertHumanTaskDtos(taskInfos);
        page.setResult(humanTaskDtos);

        return page;
    }

    /**
     * 待领任务.
     */
    public Page findGroupTasks(String userId, String tenantId, List<PropertyFilter> propertyFilters, Page page) {

        List<String> partyIds = new ArrayList<String>();
        String strIds = "";
        partyIds.addAll(this.findGroupIds(userId));
        partyIds.addAll(this.findUserIds(userId));

        logger.debug("party ids : {}", partyIds);

        if (partyIds.isEmpty()) {
            return new Page();
        }

        for (String str : partyIds) {
            strIds += "'" + str + "',";
        }

        strIds = strIds.substring(0, strIds.length() - 1);

        String sqlPagedQuerySelect = "select * , ti.COMPLETE_TIME "
                + " from (select DISTINCT i.id,i.process_instance_id,i.action,r.applycode,i.presentation_subject,"
                + "r.theme,r.user_id,p.full_name,r.ucode,r.businesstypeid,r.businesstypename,r.businessdetailid,"
                + "r.businessdetailname,r.systemid,r.systemname,r.areaid,r.areaname,r.companyid,r.companyname,"
                + "r.create_time,i.catalog,t_p.REF,r.url,i.business_key" + " from task_info i"
                + " inner join task_participant t_p on i.id = t_p.task_id"
                + " inner join kv_record r on i.business_key = r.id"
                + " inner join person_info p on r.user_id = p.id"
                + " where ifnull(i.ASSIGNEE,'')='' and i.`status`='active') t"
                + " left join (SELECT MAX(ti.COMPLETE_TIME) as COMPLETE_TIME,ti.BUSINESS_KEY FROM task_info ti"
		        + " WHERE ti.`STATUS` = 'complete' and ti.CATALOG = 'normal' group by ti.BUSINESS_KEY ) ti on t.business_key = ti.BUSINESS_KEY"
                + " where REF in (" + strIds + ")";

        String sqlPagedQueryCount = "select count(*) from (select DISTINCT i.id,i.process_instance_id,i.action,r.applycode,i.presentation_subject,"
                + "r.theme,r.user_id,p.full_name,r.ucode,r.businesstypeid,r.businesstypename,r.businessdetailid,"
                + "r.businessdetailname,r.systemid,r.systemname,r.areaid,r.areaname,r.companyid,r.companyname,"
                + "r.create_time,i.catalog,t_p.REF,r.url" + " from task_info i"
                + " inner join task_participant t_p on i.id = t_p.task_id"
                + " inner join kv_record r on i.business_key = r.id"
                + " inner join person_info p on r.user_id = p.id"
                + " where ifnull(i.ASSIGNEE,'')='' and i.`status`='active') t where REF in (" + strIds + ")";

        StringBuilder buff = new StringBuilder();
        List<Object> paramList = new ArrayList<Object>();
        boolean checkWhere = sqlPagedQuerySelect.toLowerCase().indexOf("where") == -1;
        PropertyFilterUtils.buildConfigurations(propertyFilters, buff, paramList, checkWhere);

        String sql = buff.toString();
        String countSql = sqlPagedQueryCount + " " + sql;
        String selectSql = sqlPagedQuerySelect + " " + sql + " order by create_time limit " + page.getStart() + ","
                + page.getPageSize();

        logger.debug("countSql : {}", countSql);
        logger.debug("selectSql : {}", selectSql);

        Object[] params = paramList.toArray();
        int totalCount = jdbcTemplate.queryForObject(countSql, Integer.class, params);
        List<Map<String, Object>> list = jdbcTemplate.queryForList(selectSql, params);
        List<UnfinishProcessInstance> unfinishPros = new ArrayList<UnfinishProcessInstance>();

        for (Map<String, Object> map : list) {
            unfinishPros.add(convertUnfinishProsDTO(map));
        }

        page.setTotalCount(totalCount);
        page.setResult(unfinishPros);

        return page;
    }

    /**
     * 经手任务.
     */
    public Page findDelegateTasks(String userId, String tenantId, int pageNo, int pageSize) {
        Page page = taskInfoManager.pagedQuery("from TaskInfo where owner=? and tenantId=? and status='active'", pageNo,
                pageSize, userId, tenantId);
        List<TaskInfo> taskInfos = (List<TaskInfo>) page.getResult();
        List<HumanTaskDTO> humanTaskDtos = this.convertHumanTaskDtos(taskInfos);
        page.setResult(humanTaskDtos);

        return page;
    }

    /**
     * 领取任务.
     */
    public void claimTask(String humanTaskId, String userId) {
        TaskInfo taskInfo = taskInfoManager.get(Integer.parseInt(humanTaskId));

        if (taskInfo.getAssignee() != null) {
            throw new IllegalStateException("task " + humanTaskId + " already be claimed by " + taskInfo.getAssignee());
        }

        taskInfo.setAssignee(userId);
        taskInfoManager.save(taskInfo);
    }

    /**
     * 释放任务.
     */
    public void releaseTask(String humanTaskId, String comment) {
        TaskInfo taskInfo = taskInfoManager.get(Integer.parseInt(humanTaskId));

        taskInfo.setAssignee(null);
        taskInfoManager.save(taskInfo);
    }

    /**
     * 转办.
     */
    public void transfer(String humanTaskId, String userId, String comment) {
        HumanTaskDTO humanTaskDto = this.findHumanTask(humanTaskId);
        humanTaskDto.setOwner(humanTaskDto.getAssignee());
        humanTaskDto.setAssignee(userId);
        this.saveHumanTask(humanTaskDto, false);

        internalProcessConnector.transfer(humanTaskDto.getTaskId(), humanTaskDto.getAssignee(),
                humanTaskDto.getOwner());
    }

    /**
     * 取消转办.
     */
    public void cancel(String humanTaskId, String userId, String comment) {
        HumanTaskDTO humanTaskDto = this.findHumanTask(humanTaskId);
        humanTaskDto.setAssignee(humanTaskDto.getOwner());
        humanTaskDto.setOwner("");
        this.saveHumanTask(humanTaskDto, false);
    }

    /**
     * 回退，指定节点，重新分配.
     */
    public void rollbackActivity(String humanTaskId, String activityId, String comment) {
        HumanTaskDTO humanTaskDto = findHumanTask(humanTaskId);

        if (humanTaskDto == null) {
            throw new IllegalStateException("任务不存在");
        }

        String taskId = humanTaskDto.getTaskId();
        internalProcessConnector.rollback(taskId, activityId, null);
    }

    /**
     * 回退，指定节点，上个执行人.
     */
    public void rollbackActivityLast(String humanTaskId, String activityId, String comment) {
        HumanTaskDTO humanTaskDto = findHumanTask(humanTaskId);

        if (humanTaskDto == null) {
            throw new IllegalStateException("任务不存在");
        }

        String taskId = humanTaskDto.getTaskId();
        internalProcessConnector.rollbackAuto(taskId, activityId);
    }

    /**
     * 回退，指定节点，指定执行人.
     */
    public void rollbackActivityAssignee(String humanTaskId, String activityId, String userId, String comment) {
        HumanTaskDTO humanTaskDto = findHumanTask(humanTaskId);

        if (humanTaskDto == null) {
            throw new IllegalStateException("任务不存在");
        }

        String taskId = humanTaskDto.getTaskId();
        internalProcessConnector.rollback(taskId, activityId, userId);
    }

    /**
     * 回退，上个节点，重新分配.
     */
    public void rollbackPrevious(String humanTaskId, String comment) {
        HumanTaskDTO humanTaskDto = findHumanTask(humanTaskId);
        if (humanTaskDto == null) {
            throw new IllegalStateException("任务不存在");
        }
        String taskId = humanTaskDto.getTaskId();
        internalProcessConnector.rollback(taskId, null, null);
    }

    /**
     * 回退，上个节点，上个执行人.
     */
    public void rollbackPreviousLast(String humanTaskId, String comment) {
        HumanTaskDTO humanTaskDto = findHumanTask(humanTaskId);

        if (humanTaskDto == null) {
            throw new IllegalStateException("任务不存在");
        }

        String taskId = humanTaskDto.getTaskId();
        internalProcessConnector.rollbackAuto(taskId, null);
    }

    /**
     * 回退，上个节点，指定执行人.
     */
    public void rollbackPreviousAssignee(String humanTaskId, String userId, String comment) {
        HumanTaskDTO humanTaskDto = findHumanTask(humanTaskId);

        if (humanTaskDto == null) {
            throw new IllegalStateException("任务不存在");
        }

        String taskId = humanTaskDto.getTaskId();
        internalProcessConnector.rollback(taskId, null, userId);
    }

    /**
     * 回退，开始事件，流程发起人.
     */
    public void rollbackStart(String humanTaskId, String comment) {
        HumanTaskDTO humanTaskDto = findHumanTask(humanTaskId);

        if (humanTaskDto == null) {
            throw new IllegalStateException("任务不存在");
        }

        String taskId = humanTaskDto.getTaskId();
        String processDefinitionId = humanTaskDto.getProcessDefinitionId();
        String processInstanceId = humanTaskDto.getProcessInstanceId();
        String activityId = this.internalProcessConnector.findInitialActivityId(processDefinitionId);
        String initiator = this.internalProcessConnector.findInitiator(processInstanceId);
        internalProcessConnector.rollback(taskId, activityId, initiator);
    }

    /**
     * 回退，流程发起人.
     */
    public void rollbackInitiator(String humanTaskId, String comment) {
        HumanTaskDTO humanTaskDto = findHumanTask(humanTaskId);
        
        if (humanTaskDto == null) {
            throw new IllegalStateException("任务不存在");
        }

        humanTaskDto.setAction("回退（发起人）");
        humanTaskDto.setComment(comment);
        this.saveHumanTask(humanTaskDto, false);
        
        String taskId = humanTaskDto.getTaskId();
        String processDefinitionId = humanTaskDto.getProcessDefinitionId();
        String processInstanceId = humanTaskDto.getProcessInstanceId();
        String initiator = this.internalProcessConnector.findInitiator(processInstanceId);
        String activityId = this.internalProcessConnector.findFirstUserTaskActivityId(processDefinitionId, initiator);
        internalProcessConnector.rollback(taskId, activityId, initiator);
    }

    /**
     * 撤销.
     */
    public void withdraw(String humanTaskId, String comment) {
        HumanTaskDTO humanTaskDto = findHumanTask(humanTaskId);

        if (humanTaskDto == null) {
            throw new IllegalStateException("任务不存在");
        }

        internalProcessConnector.withdrawTask(humanTaskDto.getTaskId());
    }

    /**
     * 协办.
     */
    public void delegateTask(String humanTaskId, String userId, String comment) {
        HumanTaskDTO humanTaskDto = this.findHumanTask(humanTaskId);
        humanTaskDto.setOwner(humanTaskDto.getAssignee());
        humanTaskDto.setAssignee(userId);
        humanTaskDto.setDelegateStatus("pending");
        this.saveHumanTask(humanTaskDto, false);
        internalProcessConnector.delegateTask(humanTaskDto.getTaskId(), userId);
    }

    /**
     * 协办，链状.
     */
    public void delegateTaskCreate(String humanTaskId, String userId, String comment) {
        HumanTaskDTO humanTaskDto = this.findHumanTask(humanTaskId);
        humanTaskDto.setDelegateStatus("pendingCreate");
        humanTaskDto.setStatus("pending");
        this.saveHumanTask(humanTaskDto, false);

        HumanTaskDTO targetHumanTaskDto = this.createHumanTask();
        beanMapper.copy(humanTaskDto, targetHumanTaskDto);
        targetHumanTaskDto.setStatus("active");
        targetHumanTaskDto.setParentId(humanTaskDto.getId());
        targetHumanTaskDto.setId(null);
        targetHumanTaskDto.setOwner(humanTaskDto.getAssignee());
        targetHumanTaskDto.setAssignee(userId);

        this.saveHumanTask(targetHumanTaskDto, false);

        if (humanTaskDto.getParentId() == null) {
            humanTaskDto.setOwner(humanTaskDto.getAssignee());
            humanTaskDto.setAssignee(userId);
            // 只有第一次协办才更新bpm的历史
            internalProcessConnector.delegateTask(humanTaskDto.getTaskId(), userId);
            humanTaskDto.setAssignee(humanTaskDto.getOwner());
            humanTaskDto.setOwner(null);
            this.saveHumanTask(humanTaskDto, false);
        }
    }

    /**
     * 沟通.
     */
    public void communicate(String humanTaskId, String userId, String comment) {
        HumanTaskDTO humanTaskDto = this.findHumanTask(humanTaskId);
        HumanTaskDTO target = new HumanTaskDTO();
        beanMapper.copy(humanTaskDto, target);
        target.setId(null);
        target.setCatalog(HumanTaskConstants.CATALOG_COMMUNICATE);
        target.setAssignee(userId);
        target.setParentId(humanTaskId);
        target.setMessage(comment);

        this.saveHumanTask(target, false);
    }

    /**
     * 反馈.
     */
    public void callback(String humanTaskId, String userId, String comment) {
        HumanTaskDTO humanTaskDto = this.findHumanTask(humanTaskId);
        humanTaskDto.setStatus("complete");
        humanTaskDto.setCompleteTime(new Date());
        humanTaskDto.setAction("反馈");
        humanTaskDto.setComment(comment);
        this.saveHumanTask(humanTaskDto, false);
    }

    /**
     * 跳过.
     */
    public void skip(String humanTaskId, String userId, String comment) {
        HumanTaskDTO humanTaskDto = this.findHumanTask(humanTaskId);
        humanTaskDto.setStatus("complete");
        humanTaskDto.setCompleteTime(new Date());
        humanTaskDto.setAction("跳过");
        humanTaskDto.setComment(comment);
        humanTaskDto.setOwner(humanTaskDto.getAssignee());
        humanTaskDto.setAssignee(userId);
        this.saveHumanTask(humanTaskDto, false);
        internalProcessConnector.completeTask(humanTaskDto.getTaskId(), userId, Collections.<String, Object>emptyMap());
    }

    public List<String> findGroupIds(String userId) {
        String groupSql = "select ps.PARENT_ENTITY_ID as ID from PARTY_STRUCT ps,PARTY_ENTITY child,PARTY_TYPE type"
                + " where ps.CHILD_ENTITY_ID=child.ID and child.TYPE_ID=type.ID and child.del_flag = '0' and type.TYPE='1' and child.REF=?";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(groupSql, userId);
        List<String> partyIds = new ArrayList<String>();

        for (Map<String, Object> map : list) {
            partyIds.add(map.get("ID").toString());
        }

        return partyIds;
    }

    public List<String> findUserIds(String userId) {
        String userSql = "select pe.ID as ID from PARTY_ENTITY pe,PARTY_TYPE type"
                + " where pe.TYPE_ID=type.ID and type.TYPE='1' and pe.del_flag = '0' and pe.REF=?";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(userSql, userId);
        List<String> partyIds = new ArrayList<String>();

        for (Map<String, Object> map : list) {
            partyIds.add(map.get("ID").toString());
        }

        return partyIds;
    }
    /**
     * 流程管理--任务管理
     */
    @Override
	public Page findUserTasks(List<PropertyFilter> propertyFilters, Page page) {
    	String sqlPagedQuerySelect = "select k.audit_status,k.areaId,k.areaName,k.companyId,k.companyName,k.audit_status,k.businessTypeId,k.businessTypeName,k.businessDetailId,k.businessDetailName,k.BUSINESS_KEY, k.applyCode,"
    			+ "t.ID,t.PRESENTATION_SUBJECT,t.`NAME`,t.`STATUS`,t.CREATE_TIME,t.COMPLETE_TIME,t.ASSIGNEE,t.PROCESS_STARTER,t.CATALOG,t.ACTION from kv_record k join task_info t on k.business_key=t.business_key where 1=1";
    	String sqlPagedQueryCount ="select count(*) from kv_record k join task_info t on k.business_key=t.business_key where 1=1";
	    StringBuilder buff = new StringBuilder();
	    List<Object> paramList = new ArrayList<Object>();
	    boolean checkWhere = sqlPagedQuerySelect.toLowerCase().indexOf("where") == -1;
	    PropertyFilterUtils.buildConfigurations(propertyFilters, buff, paramList, checkWhere);
	    String sql = buff.toString();
	    String countSql = sqlPagedQueryCount + " " + sql;
	    String selectSql = sqlPagedQuerySelect + " " + sql + " order by t.complete_time limit " + page.getStart() + ","
	            + page.getPageSize();
	
	    //logger.debug("countSql : {}", countSql);
	    logger.debug("selectSql : {}", selectSql);
	
	    Object[] params = paramList.toArray();
	    int totalCount = jdbcTemplate.queryForObject(countSql, Integer.class, params);
	    List<Map<String, Object>> list = jdbcTemplate.queryForList(selectSql, params);
	    List<TaskInfoAndRecord> taskList = new ArrayList<TaskInfoAndRecord>();
	
	    for (Map<String, Object> map : list) {
	        taskList.add(convertTaskInfoDTO(map));
	    }
	    page.setTotalCount(totalCount);
	    page.setResult(taskList);
	
	    return page;
	}
    public Page findManageTasks(String userId, String tenantId,List<PropertyFilter> propertyFilters, Page page){
    	List<String> userAndPost = isManage(userId);
    	String postionStr = userAndPost.get(0);
    	String userIds = userAndPost.get(1);
    	if(postionStr.lastIndexOf(",") == postionStr.length()-1){
			postionStr = postionStr.substring(0,postionStr.length()-1);
		}
		if(userIds.lastIndexOf(",")==userIds.length()-1)
			userIds = userIds.substring(0, userIds.length()-1);
	    String sqlPagedQuerySelect = "SELECT *,(SELECT MAX(COMPLETE_TIME) FROM `task_info` ti WHERE t.BUSINESS_KEY = ti.BUSINESS_KEY AND `STATUS` = 'complete' AND CATALOG='normal') AS COMPLETE_TIME"
	    							+" FROM"
	    							+" (SELECT DISTINCT r.ref AS process_instance_id,r.applycode,r.theme,r.user_id,p.full_name,r.ucode,r.businessTypeId,r.businesstypename,r.businessdetailid,r.businessdetailname,r.systemid,r.systemname,"
	    							+" r.areaid,r.areaname,r.companyid,r.companyname,r.create_time,r.url,r.audit_status AS pro_status,i.BUSINESS_KEY,i.PROCESS_DEFINITION_ID"
	    							+" FROM task_info i INNER JOIN kv_record r ON i.business_key = r.id INNER JOIN person_info p ON r.user_id = p.id WHERE i.CATALOG = 'normal' AND r.audit_status IN ('0', '1', '4', '7', '8')) t"
	    							+" WHERE"
	    							+" ((t.businessTypeId <> '9999' AND t.PROCESS_DEFINITION_ID IN (SELECT DISTINCT b.PROCESS_DEFINITION_ID"
	    							+" FROM bpm_conf_base b INNER JOIN bpm_conf_node n ON n.CONF_BASE_ID = b.ID INNER JOIN bpm_conf_user u ON u.NODE_ID = n.ID WHERE REPLACE (u.`VALUE`, '岗位:', '') IN ("+postionStr+")"
	    							+" ))OR (t.businessTypeId = '9999' AND t.business_Key IN (SELECT ca.business_key FROM custom_approver ca WHERE ca.opterType NOT IN ('2', '3') AND approverId IN ("+userIds+"))))";
	    							
	    
	    String sqlPagedQueryCount = "SELECT count(*)"
									+" FROM"
									+" (SELECT DISTINCT r.ref AS process_instance_id,r.applycode,r.theme,r.user_id,p.full_name,r.ucode,r.businessTypeId,r.businesstypename,r.businessdetailid,r.businessdetailname,r.systemid,r.systemname,"
									+" r.areaid,r.areaname,r.companyid,r.companyname,r.create_time,r.url,r.audit_status AS pro_status,i.BUSINESS_KEY,i.PROCESS_DEFINITION_ID"
									+" FROM task_info i INNER JOIN kv_record r ON i.business_key = r.id INNER JOIN person_info p ON r.user_id = p.id WHERE i.CATALOG = 'normal' AND r.audit_status IN ('0', '1', '4', '7', '8')) t"
									+" WHERE"
									+" ((t.businessTypeId <> '9999' AND t.PROCESS_DEFINITION_ID IN (SELECT DISTINCT b.PROCESS_DEFINITION_ID"
									+" FROM bpm_conf_base b INNER JOIN bpm_conf_node n ON n.CONF_BASE_ID = b.ID INNER JOIN bpm_conf_user u ON u.NODE_ID = n.ID WHERE u.`VALUE` <> '常用语:流程发起人' AND REPLACE (u.`VALUE`, '岗位:', '') IN ("+postionStr+")"
									+" ))OR (t.businessTypeId = '9999' AND t.business_Key IN (SELECT ca.business_key FROM custom_approver ca WHERE ca.opterType NOT IN ('2', '3') AND approverId IN ("+userIds+"))))";
	    
    	StringBuilder buff = new StringBuilder();
        List<Object> paramList = new ArrayList<Object>();
        boolean checkWhere = sqlPagedQuerySelect.toLowerCase().indexOf("where") == -1;
        PropertyFilterUtils.buildConfigurations(propertyFilters, buff, paramList, checkWhere);
        String sql = buff.toString();
        String countSql = sqlPagedQueryCount + " " + sql;
        String selectSql = sqlPagedQuerySelect + " " + sql + " order by create_time desc limit " + page.getStart() + ","
                + page.getPageSize();

        logger.debug("countSql : {}", countSql);
        logger.debug("selectSql : {}", selectSql);

        Object[] params = paramList.toArray();
        int totalCount = jdbcTemplate.queryForObject(countSql, Integer.class, params);
        List<Map<String, Object>> list = jdbcTemplate.queryForList(selectSql, params);
        List<UnfinishProcessInstance> unfinishPros = new ArrayList<UnfinishProcessInstance>();
        
        for (Map<String, Object> map : list) {
            UnfinishProcessInstance uModelInstance = convertUnfinishProsDTO(map);
            uModelInstance.setStatus(dictConnector.findDictNameByValue("RecordStatus", map.get("pro_status").toString()));
            unfinishPros.add(uModelInstance);
        }

        page.setTotalCount(totalCount);
        page.setResult(unfinishPros);
    	return page;
    }
    /**
     * @param userId
     * @return
     * 校验用户是否是管理者 
     */
    public List<String> isManage(String userId){
    	List<String> list = new ArrayList<>();
    	String isManage = "";
    	String postionStr = "";
    	String userIds = userId+",";
    	//判断当前登录人是否是管理者
    	String isManageSql = "select s.ID,s.STRUCT_TYPE_ID,s.PARENT_ENTITY_ID,s.CHILD_ENTITY_ID,e.TYPE_ID,e.`NAME` from party_struct s join party_entity e on s.PARENT_ENTITY_ID=e.ID where e.DEL_FLAG=0 and e.IS_DISPLAY=1 and s.STRUCT_TYPE_ID=2 and s.CHILD_ENTITY_ID="+userId;
    	List<Map<String,Object>> manageList = jdbcTemplate.queryForList(isManageSql);
    	for(int i=0;i<manageList.size();i++){
			isManage += manageList.get(i).get("PARENT_ENTITY_ID");
			if(manageList.size()-i != 1)
				isManage += ",";
    	}
    	String positionByUserId = "select s.ID,s.STRUCT_TYPE_ID,s.PARENT_ENTITY_ID,s.CHILD_ENTITY_ID,e.TYPE_ID,e.`NAME` from party_struct s join party_entity e on s.PARENT_ENTITY_ID=e.ID where e.DEL_FLAG=0 and e.IS_DISPLAY=1 and s.STRUCT_TYPE_ID=4 and s.CHILD_ENTITY_ID="+userId;
    	List<Map<String,Object>> positionList = jdbcTemplate.queryForList(positionByUserId);
    	for(int j=0;j<positionList.size();j++){
    		postionStr += positionList.get(j).get("PARENT_ENTITY_ID");
    		postionStr += ",";
    	}
    	if(!"".equals(isManage)){
    		boolean b = true;
    		while(b){
    			if(StringUtils.isEmpty(isManage)){
    				b = false;
    				continue;
    			}
    			if(isManage.lastIndexOf(",") == isManage.length()-1)
    				isManage = isManage.substring(0, isManage.length()-1);
				List<String> result = recursion(isManage);
				if(result.size() == 0)
					b = false;
				for(int i=0;i<result.size();i++){
					if(i == 0)
						postionStr += result.get(i);
					else if(i == 1)
						userIds += result.get(i);
					else
						isManage = result.get(i);
				}
    		}
    	}
    	list.add(postionStr);
		list.add(userIds);
    	return list;
    }
    /**
     * @param orgId
     * @return
     * 递归获取机构下的岗位和人员
     */
    public List<String> recursion(String orgId){
    	List<String> list = new ArrayList<>();
    	StringBuffer bufferPosition = new StringBuffer();
    	StringBuffer bufferPerson = new StringBuffer();
    	StringBuffer buffer = new StringBuffer();
    	String positionByOrg = "select DISTINCT e.* from party_entity e join party_struct s on e.ID=s.CHILD_ENTITY_ID where e.DEL_FLAG=0 and e.IS_DISPLAY=1 AND s.PARENT_ENTITY_ID in("+orgId+")";
    	List<Map<String,Object>> departmentAndPosition = jdbcTemplate.queryForList(positionByOrg);
    	for(Map<String,Object> map : departmentAndPosition){
    		if("5".equals(map.get("type_id").toString())){
    			bufferPosition.append(map.get("id"));
    			bufferPosition.append(",");
    		}else if("1".equals(map.get("type_id").toString())){
    			bufferPerson.append(map.get("id"));
    			bufferPerson.append(",");
    		}else{
    			buffer.append(map.get("id").toString());
    			buffer.append(",");
    		}
    	}
    	list.add(bufferPosition.toString());
    	list.add(bufferPerson.toString());
    	list.add(buffer.toString());
    	return list;
    }
    
    // ~ ==================================================
    protected TaskInfoAndRecord convertTaskInfoDTO(Map<String, Object> map) {
    	if ((map == null) || map.isEmpty()) {
            logger.info("convertTaskInfoDTO[{}] is null.", map);

            return null;
        }

    	TaskInfoAndRecord taskInfo = new TaskInfoAndRecord();
    	taskInfo.setId(Long.parseLong(convertString(map.get("id"))));
    	taskInfo.setPresentationSubject(convertString(map.get("PRESENTATION_SUBJECT")));
    	taskInfo.setName(convertString(map.get("NAME")));
    	taskInfo.setStatus(convertString(map.get("STATUS")));
    	taskInfo.setCreateTime((Date) map.get("CREATE_TIME"));
    	taskInfo.setCompleteTime((Date) map.get("COMPLETE_TIME"));
    	taskInfo.setAssignee(convertString(map.get("ASSIGNEE")));
    	taskInfo.setProcessStarter(convertString(map.get("PROCESS_STARTER")));
    	taskInfo.setCatalog(convertString(map.get("CATALOG")));
    	taskInfo.setAction(convertString(map.get("ACTION")));
    	taskInfo.setApplyCode(convertString(map.get("applyCode")));
    	//--------------------------------------------------------
    	taskInfo.setAreaId(convertString(map.get("areaId")));
    	taskInfo.setAreaName(convertString(map.get("areaName")));
    	taskInfo.setCompanyId(convertString(map.get("companyId")));
    	taskInfo.setCompanyName(convertString(map.get("companyName")));
    	taskInfo.setBusinessTypeId(convertString(map.get("businessTypeId")));
    	taskInfo.setBusinessTypeName(convertString(map.get("businessTypeName")));
    	taskInfo.setBusinessdDetailId(convertString(map.get("businessDetailId")));
    	taskInfo.setBusinessDetailName(convertString(map.get("businessDetailName")));
    	taskInfo.setBusinessKey(convertString(map.get("business_key")));
    	taskInfo.setAuditStatus(convertString(map.get("audit_status")));
        return taskInfo;
    }
    public List<HumanTaskDTO> convertHumanTaskDtos(Collection<TaskInfo> taskInfos) {
        List<HumanTaskDTO> humanTaskDtos = new ArrayList<HumanTaskDTO>();

        for (TaskInfo taskInfo : taskInfos) {
            if (taskInfo.getComment() == null)
                taskInfo.setComment("");
            humanTaskDtos.add(convertHumanTaskDto(taskInfo));
        }

        return humanTaskDtos;
    }

    public HumanTaskDTO convertHumanTaskDto(TaskInfo taskInfo) {
        if (taskInfo == null) {
            return null;
        }

        HumanTaskDTO humanTaskDto = new HumanTaskDTO();
        beanMapper.copy(taskInfo, humanTaskDto);
        if (taskInfo.getTaskInfo() != null) {
            humanTaskDto.setParentId(Long.toString(taskInfo.getTaskInfo().getId()));
        }
        if (!taskInfo.getTaskInfos().isEmpty()) {
            List<HumanTaskDTO> children = this.convertHumanTaskDtos(taskInfo.getTaskInfos());
            humanTaskDto.setChildren(children);
        }

        return humanTaskDto;
    }

    public void saveParticipant(ParticipantDTO participantDto) {
        TaskParticipant taskParticipant = new TaskParticipant();
        taskParticipant.setRef(participantDto.getCode());
        taskParticipant.setType(participantDto.getType());
        taskParticipant.setTaskInfo(taskInfoManager.get(Long.parseLong(participantDto.getHumanTaskId())));
        taskParticipantManager.save(taskParticipant);
    }

    protected UnfinishProcessInstance convertUnfinishProsDTO(Map<String, Object> map) {
        if ((map == null) || map.isEmpty()) {
            logger.info("finishProcessInstance[{}] is null.", map);

            return null;
        }
        UnfinishProcessInstance unfinishProcessInstance = new UnfinishProcessInstance();
        unfinishProcessInstance.setId(convertString(map.get("id")));
        unfinishProcessInstance.setProcessInstanceId(convertString(map.get("process_instance_id")));
        unfinishProcessInstance.setAction(convertString(map.get("action")));
        unfinishProcessInstance.setBusinessKey(convertString(map.get("business_key")));
        unfinishProcessInstance.setStartTime((Date) map.get("create_time"));
        unfinishProcessInstance.setCcTime((Date) map.get("cc_time"));
        unfinishProcessInstance.setEndTime((Date) map.get("complete_time"));
        unfinishProcessInstance.setName(convertString(map.get("presentation_subject")));
        /*unfinishProcessInstance.setTheme(StringUtils.isBlank(convertString(map.get("theme")))
                ? convertString(map.get("presentation_subject")) : convertString(map.get("theme")));*/
        unfinishProcessInstance.setTheme(convertString(map.get("theme")));
        unfinishProcessInstance.setApplyCode(convertString(map.get("applyCode")));
        unfinishProcessInstance.setUcode(convertString(map.get("ucode")));
        unfinishProcessInstance.setBusinessTypeName(convertString(map.get("businessTypeName")));
        unfinishProcessInstance.setBusinessDetailName(convertString(map.get("businessDetailName")));
        unfinishProcessInstance.setApplyUserName(convertString(map.get("full_name")));
        unfinishProcessInstance.setSystemName(null != map.get("systemname")
        		? convertString(map.get("systemname")) : "");
        unfinishProcessInstance.setAreaName(convertString(map.get("areaname")));
        unfinishProcessInstance.setCompanyName(convertString(map.get("companyname")));
        unfinishProcessInstance.setSystemId(convertString(map.get("systemid")));
        unfinishProcessInstance.setAreaId(convertString(map.get("areaid")));
        unfinishProcessInstance.setCompanyId(convertString(map.get("companyid")));
        unfinishProcessInstance.setUrl(convertString(map.get("url")));
        unfinishProcessInstance.setCatalog(convertString(map.get("catalog")));
        unfinishProcessInstance.setProFlag(convertString(map.get("suspend_Status")));
        unfinishProcessInstance.setApplyContent(convertString(map.get("apply_content")));
        unfinishProcessInstance.setCompleteTime((Date) map.get("complete_time"));
        // unfinishProcessInstance.setSubmitTimes(convertString(map.get("submitTimes"))
        // == null ? 1 :
        // Integer.parseInt(convertString(map.get("submitTimes"))));
        unfinishProcessInstance.setBusinessTypeId(convertString(map.get("businessTypeId")));
        unfinishProcessInstance.setBusinessDetailId(convertString(map.get("businessdetailid")));
        unfinishProcessInstance.setWhole(convertString(map.get("whole")));
        unfinishProcessInstance.setAssignee(null != map.get("assignee")
        		? convertString(map.get("assignee")) : "");
        if(map.get("compare") != null){
        	unfinishProcessInstance.setCompare((boolean)map.get("compare"));
        }
        if(map.get("auditDuration") != null){
        	unfinishProcessInstance.setAuditDuration(convertString(map.get("auditDuration")));
        }
        unfinishProcessInstance.setStatus(convertString(map.get("status")));
        unfinishProcessInstance.setIsRead(null != map.get("tip_id")
        		? "1" : "0");
        
        return unfinishProcessInstance;

    }
    /**
     * TODO shijingxin 2018.3.8
     * @param detailId
     * @return
     */
    public String confirmBydetailId(String detailId){
    	String formName = "";
    	//自定义细分需要特殊区分
    	if(detailId.equals("8888")||detailId.equals("8001")||detailId.equals("8002")||detailId.equals("8003")||detailId.equals("8004")){
    		formName = "operation/custom-apply-list";
    	}else{
    		formName = businessDetailManager.findUniqueBy("id", Long.parseLong(detailId)).getFormid();
    	}
    	return formName;
    }
    // 按部门和公司查询 数据存放
    protected UnfinishProcessInstance convertUnfinishProsDTOTemp(Map<String, Object> map) {
        if ((map == null) || map.isEmpty()) {
            logger.info("finishProcessInstance[{}] is null.", map);

            return null;
        }

        UnfinishProcessInstance unfinishProcessInstance = new UnfinishProcessInstance();
        unfinishProcessInstance.setId(convertString(map.get("id")));
        unfinishProcessInstance.setProcessInstanceId(convertString(map.get("process_instance_id")));
        unfinishProcessInstance.setAction(convertString(map.get("action")));
        unfinishProcessInstance.setBusinessKey(convertString(map.get("business_key")));
        unfinishProcessInstance.setStartTime((Date) map.get("create_time"));
        unfinishProcessInstance.setName(convertString(map.get("presentation_subject")));
        unfinishProcessInstance.setTheme(StringUtils.isBlank(convertString(map.get("theme")))
                ? convertString(map.get("presentation_subject")) : convertString(map.get("theme")));
        unfinishProcessInstance.setApplyCode(convertString(map.get("applyCode")));
        unfinishProcessInstance.setUcode(convertString(map.get("ucode")));
        unfinishProcessInstance.setBusinessTypeName(convertString(map.get("businessTypeName")));
        unfinishProcessInstance.setBusinessDetailName(convertString(map.get("businessDetailName")));
        unfinishProcessInstance.setApplyUserName(convertString(map.get("DISPLAY_NAME")));
        unfinishProcessInstance.setSystemName(convertString(map.get("systemname")));
        unfinishProcessInstance.setAreaName(convertString(map.get("areaname")));
        unfinishProcessInstance.setCompanyName(convertString(map.get("companyname")));
        unfinishProcessInstance.setSystemId(convertString(map.get("systemid")));
        unfinishProcessInstance.setAreaId(convertString(map.get("areaid")));
        unfinishProcessInstance.setCompanyId(convertString(map.get("companyid")));
        unfinishProcessInstance.setUrl(convertString(map.get("url")));
        unfinishProcessInstance.setCatalog(convertString(map.get("catalog")));
        unfinishProcessInstance.setProFlag(convertString(map.get("pro_flag")));
        unfinishProcessInstance.setApplyContent(convertString(map.get("apply_content")));
        unfinishProcessInstance.setCompleteTime((Date) map.get("complete_time"));
        // unfinishProcessInstance.setSubmitTimes(convertString(map.get("submitTimes"))
        // == null ? 1 :
        // Integer.parseInt(convertString(map.get("submitTimes"))));
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
     * 导出分公司业务申请实体表数据
     * @param map
     * @return
     */
    protected BusinessDTO BusinessProcsee(Map<String, Object> map) {
    	if ((map == null) || map.isEmpty()) {
    		logger.info("finishProcessInstance[{}] is null.", map);
    		
    		return null;
    	}
    	BusinessDTO businessDTO = new BusinessDTO();
    	businessDTO.setSubmitTimes(convertString(map.get("submit_times")));
    	businessDTO.setApplyCode(convertString(map.get("apply_code")));
    	businessDTO.setTheme(convertString(map.get("theme")));
    	businessDTO.setCc(convertString(map.get("cc")));
    	businessDTO.setBusinessType(convertString(map.get("business_type")));
    	businessDTO.setBusinessDetail(convertString(map.get("business_detail")));
    	businessDTO.setBusinessLevel(convertString(map.get("business_level")));
    	businessDTO.setInitiator(convertString(map.get("initiator")));
    	businessDTO.setArea(convertString(map.get("area")));
    	businessDTO.setBranchOffice(convertString(map.get("branch_office")));
    	businessDTO.setApplyContent(convertString(map.get("apply_content")));
    	businessDTO.setCreateTime((Date)map.get("create_time"));
    	businessDTO.setCompleteTime((Date)map.get("complete_time"));
    	businessDTO.setComment(convertString(map.get("comment")));
    	businessDTO.setAssignee(null != map.get("assignee")
        		? convertString(map.get("assignee")) : "");
    	return businessDTO;
    	
    }
    /**
     * 导出冻结/解冻实体表数据
     * @param map
     * @return
     */
    protected FreezeDTO FreezeProsee(Map<String, Object> map) {
    	if ((map == null) || map.isEmpty()) {
    		logger.info("finishProcessInstance[{}] is null.", map);
    		
    		return null;
    	}
    	FreezeDTO FreezeDTO = new FreezeDTO();
    	FreezeDTO.setApplyCode(convertString(map.get("applyCode")));
    	FreezeDTO.setUcode(convertString(map.get("ucode")));
    	FreezeDTO.setName(convertString(map.get("name")));
    	FreezeDTO.setContact(convertString(map.get("contact")));
    	FreezeDTO.setSalesLevel(convertString(map.get("sales_level")));
    	FreezeDTO.setWelfareLevel(convertString(map.get("welfare_level")));
    	FreezeDTO.setActivationState(convertString(map.get("activation_state")));
    	FreezeDTO.setSystem(convertString(map.get("system")));
    	FreezeDTO.setAboveBoard(convertString(map.get("above_board")));
    	FreezeDTO.setFrozenState(convertString(map.get("frozen_state")));
    	FreezeDTO.setArea(convertString(map.get("area")));
    	FreezeDTO.setDirector(convertString(map.get("director")));
    	FreezeDTO.setDirectorContact(convertString(map.get("director_contact")));
    	FreezeDTO.setBranchOffice(convertString(map.get("branch_office")));
    	FreezeDTO.setIdNumber(convertString(map.get("id_number")));
    	FreezeDTO.setApplyMatter(convertString(map.get("apply_matter")));
    	FreezeDTO.setApplyContent(convertString(map.get("apply_content")));
    	FreezeDTO.setCreateTime((Date)map.get("create_time"));
    	FreezeDTO.setCompleteTime((Date)map.get("complete_time"));
    	FreezeDTO.setComment(convertString(map.get("comment")));
    	FreezeDTO.setAssignee(null != map.get("assignee")
        		? convertString(map.get("assignee")) : "");
    	return FreezeDTO;
    	
    }
    /**
     * 导出违规冻结/解冻实体表数据
     * @param map
     * @return
     */
    protected LllegalFreezeDTO lllegalFreezeProsee(Map<String, Object> map) {
    	if ((map == null) || map.isEmpty()) {
    		logger.info("finishProcessInstance[{}] is null.", map);
    		
    		return null;
    	}
    	
    	LllegalFreezeDTO lllegalFreezeDTO = new LllegalFreezeDTO();
    	lllegalFreezeDTO.setSubmitTimes(convertString(map.get("submit_times")));
    	lllegalFreezeDTO.setTheme(convertString(map.get("theme")));
    	lllegalFreezeDTO.setCc(convertString(map.get("cc")));
    	lllegalFreezeDTO.setApplyCode(convertString(map.get("applyCode")));
    	lllegalFreezeDTO.setUcode(convertString(map.get("ucode")));
    	lllegalFreezeDTO.setBusinessType(convertString(map.get("business_type")));
    	lllegalFreezeDTO.setBusinessDetail(convertString(map.get("business_detail")));
    	lllegalFreezeDTO.setBusinessLevel(convertString(map.get("business_level")));
    	lllegalFreezeDTO.setInitiator(convertString(map.get("initiator")));
    	lllegalFreezeDTO.setName(convertString(map.get("name")));
    	lllegalFreezeDTO.setWelfareLevel(convertString(map.get("welfare_level")));
    	lllegalFreezeDTO.setQualificationsStatus(convertString(map.get("qualifications_status")));
    	lllegalFreezeDTO.setSystem(convertString(map.get("system")));
    	lllegalFreezeDTO.setContact(convertString(map.get("contact")));
    	lllegalFreezeDTO.setArea(convertString(map.get("area")));
    	lllegalFreezeDTO.setCompany(convertString(map.get("branch_office")));
    	lllegalFreezeDTO.setIdNumber(convertString(map.get("id_number")));
    	lllegalFreezeDTO.setAboveBoard(convertString(map.get("above_board")));
    	lllegalFreezeDTO.setDirectorContact(convertString(map.get("director_contact")));
    	lllegalFreezeDTO.setApplyMatter(convertString(map.get("apply_matter")));
    	lllegalFreezeDTO.setApplyContent(convertString(map.get("apply_content")));
    	lllegalFreezeDTO.setCreateTime((Date)map.get("create_time"));
    	lllegalFreezeDTO.setCompleteTime((Date)map.get("complete_time"));
    	lllegalFreezeDTO.setComment(convertString(map.get("comment")));
    	lllegalFreezeDTO.setAssignee(null != map.get("assignee")
        		? convertString(map.get("assignee")) : "");
    	return lllegalFreezeDTO;
    	
    }
    /**
     * 导出退货实体表数据
     * @param map
     * @return
     */
    protected ReturnDTO returnProcess(Map<String, Object> map) {
    	if ((map == null) || map.isEmpty()) {
    		logger.info("finishProcessInstance[{}] is null.", map);
    		
    		return null;
    	}
    	
    	ReturnDTO returnDTO = new ReturnDTO();
    	returnDTO.setWareHouse(convertString(map.get("ware_house")));
    	returnDTO.setEmpNo(convertString(map.get("emp_no")));
    	returnDTO.setUcode(convertString(map.get("ucode")));
    	returnDTO.setShopName(convertString(map.get("shop_name")));
    	returnDTO.setShopTel(convertString(map.get("shop_tel")));
    	returnDTO.setReturnDate(convertString(map.get("return_date")));
    	returnDTO.setOrderNumber(convertString(map.get("order_number")));
    	returnDTO.setReturnReaon(convertString(map.get("return_reaon")));
    	returnDTO.setShopPayStock(convertString(map.get("shop_pay_stock")));
    	returnDTO.setRewardIntegralStock(convertString(map.get("reward_integral_stock")));
    	returnDTO.setPersonPayStock(convertString(map.get("person_pay_stock")));
    	returnDTO.setPayType(convertString(map.get("pay_type")));
    	returnDTO.setCreateTime((Date)map.get("create_time"));
    	returnDTO.setCompleteTime((Date)map.get("complete_time"));
    	returnDTO.setComment(convertString(map.get("comment")));
    	returnDTO.setAssignee(null != map.get("assignee")
        		? convertString(map.get("assignee")) : "");
    	return returnDTO;
    	
    }
    /**
     * 导出发票实体表数据
     * @param map
     * @return
     */
    protected InvoiceDTO invoiceProcess(Map<String, Object> map) {
    	if ((map == null) || map.isEmpty()) {
    		logger.info("finishProcessInstance[{}] is null.", map);
    		
    		return null;
    	}
    	InvoiceDTO invoiceDTO = new InvoiceDTO();
    	invoiceDTO.setUcode(convertString(map.get("ucode")));
    	invoiceDTO.setShopName(convertString(map.get("shop_name")));
    	invoiceDTO.setShopTel(convertString(map.get("shop_tel")));
    	invoiceDTO.setInvoiceDate(convertString(map.get("invoice_date")));
    	invoiceDTO.setOrderNumber(convertString(map.get("order_number")));
    	invoiceDTO.setArea(convertString(map.get("area")));
    	invoiceDTO.setSystem(convertString(map.get("system")));
    	invoiceDTO.setBranchOffice(convertString(map.get("branch_office")));
    	invoiceDTO.setInvoiceType(convertString(map.get("invoice_type")));
    	invoiceDTO.setCategory(convertString(map.get("category")));
    	invoiceDTO.setInvoiceTitle(convertString(map.get("invoice_title")));
    	invoiceDTO.setInvoiceDetail(convertString(map.get("invoice_detail")));
    	invoiceDTO.setInvoiceMoney(convertString(map.get("invoice_money")));
    	invoiceDTO.setIdNumber(convertString(map.get("id_number")));
    	invoiceDTO.setEnterpriseName(convertString(map.get("enterprise_name")));
    	invoiceDTO.setTaxNumber(convertString(map.get("tax_number")));
    	invoiceDTO.setOpeningBank(convertString(map.get("opening_bank")));
    	invoiceDTO.setAccountNumber(convertString(map.get("account_number")));
    	invoiceDTO.setEnterpriseAddress(convertString(map.get("enterprise_address")));
    	invoiceDTO.setInvoiceMailAddress(convertString(map.get("invoice_mail_address")));
    	invoiceDTO.setAddressee(convertString(map.get("addressee")));
    	invoiceDTO.setAddresseeTel(convertString(map.get("addressee_tel")));
    	invoiceDTO.setAddresseeSpareTel(convertString(map.get("addressee_spare_tel")));
    	invoiceDTO.setCreateTime((Date)map.get("create_time"));
    	invoiceDTO.setCompleteTime((Date)map.get("complete_time"));
    	invoiceDTO.setComment(convertString(map.get("comment")));
    	invoiceDTO.setAssignee(null != map.get("assignee")
        		? convertString(map.get("assignee")) : "");
    	return invoiceDTO;
    	
    }
    /**
     * 自定义的实体数据
     * @param map
     * @return
     */
    protected CustomEntityDTO customProcess(Map<String, Object> map) {
    	if ((map == null) || map.isEmpty()) {
    		logger.info("finishProcessInstance[{}] is null.", map);
    		
    		return null;
    	}
    	CustomEntityDTO custom = new CustomEntityDTO();
    	Integer submitTimes=0;
    	String strSubmitTimes=convertString(map.get("submitTimes"));
    	if(!com.mossle.core.util.StringUtils.isBlank(strSubmitTimes))
    		submitTimes=Integer.parseInt(strSubmitTimes);
    	custom.setSubmitTimes(submitTimes);
    	custom.setApplyCode(convertString(map.get("applyCode")));
    	custom.setTheme(convertString(map.get("subject")));
    	custom.setCcName(convertString(map.get("ccName")));
    	custom.setBusinessType("自定义");
    	custom.setBusinessDetail("自定义申请");
    	custom.setName(convertString(map.get("name")));
    	custom.setApplyContent(convertString(map.get("content")));
    	if(map.get("create_time") != null && convertString(map.get("create_time")).lastIndexOf(".0")>0){
    		custom.setCreateTime(convertString(map.get("create_time")).substring(0, convertString(map.get("create_time")).lastIndexOf(".0")));
    	}
    	if(map.get("complete_time") != null && convertString(map.get("complete_time")).lastIndexOf(".0")>0){
    		custom.setCompleteTime(convertString(map.get("complete_time")).substring(0, convertString(map.get("complete_time")).lastIndexOf(".0")));
    	}
    	String comment="";
    	comment=convertString(map.get("comment"));
    	if(!com.mossle.core.util.StringUtils.isBlank(comment))
    		comment=comment.replaceAll("<br/>", "，");
    	custom.setComment(comment);
    	custom.setAssignee(null != map.get("assignee")
        		? convertString(map.get("assignee")) : "");
    	return custom;
    }
    /**
     * 撤单 实体数据
     * @param map
     * @return
     */
    protected CancelOrderDTO cancelOrderProcess(Map<String, Object> map){
    	if ((map == null) || map.isEmpty()) {
    		logger.info("finishProcessInstance[{}] is null.", map);
    		return null;
    	}
    	CancelOrderDTO cancel = new CancelOrderDTO();
    	cancel.setSubmitTimes(Integer.parseInt(convertString(map.get("submitTimes"))));
    	cancel.setApplyCode(convertString(map.get("applyCode")));
    	cancel.setApplyCode(convertString(map.get("applyCode")));
    	cancel.setUcode(convertString(map.get("shopCode")));
    	cancel.setShopName(convertString(map.get("shopName")));
    	cancel.setShopMobile(convertString(map.get("shopMobile")));
    	cancel.setMobile(convertString(map.get("mobile")));
    	cancel.setRegisterTime(convertString(map.get("registerTime")));
    	cancel.setRegisterName(convertString(map.get("registerName")));
    	cancel.setIsChecked(convertString(map.get("isChecked")));
    	cancel.setCancelRemark(convertString(map.get("cancelRemark")));
    	if(map.get("create_time") != null && convertString(map.get("create_time")).lastIndexOf(".0")>0){
    		cancel.setCreateTime(convertString(map.get("create_time")).substring(0, convertString(map.get("create_time")).lastIndexOf(".0")));
    	}
    	if(map.get("complete_time") != null && convertString(map.get("complete_time")).lastIndexOf(".0")>0){
    		cancel.setCompleteTime(convertString(map.get("complete_time")).substring(0, convertString(map.get("complete_time")).lastIndexOf(".0")));
    	}
    	cancel.setComment(convertString(map.get("comment")));
    	cancel.setAssignee(null != map.get("assignee")
        		? convertString(map.get("assignee")) : "");
    	return cancel;
    }
    /**
     * 常规/非常规业务 实体数据
     * @param map
     * @return
     */
    protected ApplyDTO commonProcess(Map<String, Object> map){
    	if ((map == null) || map.isEmpty()) {
    		logger.info("finishProcessInstance[{}] is null.", map);
    		return null;
    	}
    	ApplyDTO apply = new ApplyDTO();
    	apply.setSubmitTimes(Integer.parseInt(convertString(map.get("submitTimes"))));
    	apply.setApplyCode(convertString(map.get("applyCode")));
    	apply.setUcode(convertString(map.get("ucode")));
    	apply.setUserName(convertString(map.get("userName")));
    	apply.setWelfare(convertString(map.get("welfare")));
    	apply.setLevel(convertString(map.get("level")));
    	apply.setSystem(convertString(map.get("system")));
    	apply.setVarFather(convertString(map.get("varFather")));
    	apply.setVarRe(convertString(map.get("varRe")));
    	apply.setAddTime(convertString(map.get("addTime")));
    	apply.setBusinessType(convertString(map.get("businessType")));
    	apply.setBusinessDetail(convertString(map.get("businessDetail")));
    	apply.setMobile(convertString(map.get("mobile")));
    	apply.setAddress(convertString(map.get("address")));
    	apply.setBusinessLevel(convertString(map.get("businessLevel")));
    	apply.setArea(convertString(map.get("area")));
    	apply.setApplyContent(convertString(map.get("content")));
    	apply.setCreateTime((Date)map.get("create_time"));
    	apply.setCompleteTime((Date)map.get("complete_time"));
    	apply.setComment(convertString(map.get("comment")));
    	apply.setAssignee(null != map.get("assignee")
        		? convertString(map.get("assignee")) : "");
    	return apply;
    	}
    /**
     * 直销发起的流程
     * @author sjx
     * @param map
     * @return
     */
     protected OnLineInfoDTO onlineProcess(Map<String, Object> map){
    	if ((map == null) || map.isEmpty()) {
    		logger.info("finishProcessInstance[{}] is null.", map);
    		return null;
    	}
    	OnLineInfoDTO onLineInfo = new OnLineInfoDTO();
    	onLineInfo.setApplycode(convertString(map.get("varApplyCode")));//单据号
    	onLineInfo.setUcode(convertString(map.get("ucode")));//申请人编号
    	onLineInfo.setBranch(convertString(map.get("varBranch")));//部门
    	onLineInfo.setName(convertString(map.get("varApplyName")));//申请人姓名
    	onLineInfo.setNewname(convertString(map.get("varNewName")));//新姓名
    	onLineInfo.setIdentity(convertString(map.get("varApplyIdentity")));//申请人证件号
    	onLineInfo.setNewidentity(convertString(map.get("varNewidentity")));//申请人新证件号
    	onLineInfo.setWelfaregrade(convertString(map.get("varWelfareGrade")));//福利级别
    	String strBankAddress=convertString(map.get("varBankAddress"));
    	if(!com.mossle.core.util.StringUtils.isBlank(strBankAddress))
    		strBankAddress=strBankAddress.replaceAll(",","");
    	else
    		strBankAddress="";
    	onLineInfo.setBankaddress(strBankAddress);//新银行地址(包含省/市/区县)
    	onLineInfo.setBankname(convertString(map.get("varBankName")));//新银行
    	onLineInfo.setCompleteremark(convertString(map.get("varCompleteRemark")));//营业执照注册号
    	onLineInfo.setApplytype(convertString(map.get("businessdetailname")));
    	onLineInfo.setMobile(convertString(map.get("varMobile")));//短信接收号码
    	onLineInfo.setBankcode(convertString(map.get("varBankcode")));//代理人编号
    	onLineInfo.setReason(convertString(map.get("varReason")));//申请原因
    	onLineInfo.setApplytime((Date)map.get("create_time"));
    	onLineInfo.setAudittime((Date)map.get("complete_time"));
    	String strComment="";
    	strComment=convertString(map.get("comment"));
    	if(!com.mossle.core.util.StringUtils.isBlank(strComment)) {
    		strComment=LostHtml(strComment.replaceAll("<br/>", "，"));
    	}
    	onLineInfo.setComment(strComment);
    	onLineInfo.setAssignee(null != map.get("assignee")
        		? convertString(map.get("assignee")) : "");
    	//以下 TODO sjx 2018.11.15
    	onLineInfo.setShopLicense(convertString(map.get("varShopLicense")));//统一社会信用代码
    	onLineInfo.setEnterpriseName(convertString(map.get("varEnterpriseName")));//企业名称
    	onLineInfo.setLegaler(convertString(map.get("varLegaler")));//法定代表人
    	onLineInfo.setDistributorPhone(convertString(map.get("varDistributorPhone")));//联系电话
    	onLineInfo.setLegalerIdCard(convertString(map.get("varLegalerIdCard")));//法人身份证号码
    	onLineInfo.setScopeBusiness(convertString(map.get("varScopeBusiness")));//经营范围
    	onLineInfo.setNote(convertString(map.get("varNote")));//代理区域或备注
    	onLineInfo.setPublicAccount(convertString(map.get("varPublicAccount")));//对公账户行号
    	onLineInfo.setStoreArea(convertString(map.get("varStoreArea")));//实体店面积
    	onLineInfo.setAccountType(convertString(map.get("varAccountType")));//企业性质
    	onLineInfo.setAccountNumbr(convertString(map.get("varAccountNumber")));//银行账号
    	onLineInfo.setOpeningBank(convertString(map.get("varOpeningBank")));//开户行
    	return onLineInfo;
    }
     
     private String LostHtml(String strHtml){
 		String regxpForHtml = "<([^>]*)>"; // 过滤所有以<开头以>结尾的标签
 		Pattern p_html = Pattern.compile(regxpForHtml, Pattern.CASE_INSENSITIVE);
         Matcher m_html = p_html.matcher(strHtml);
         strHtml = m_html.replaceAll(""); // 过滤html标签
         return strHtml;
 	}

     @Transactional(readOnly=false)
     public int saveTask(Long taskId,String assignee,String strType,Long positionId){
    	 String userId = currentUserHolder.getUserId();
    	 TaskInfo taskInfo = taskInfoManager.findUniqueBy("id", taskId);
    	 String oldAssignee = "";
    	 if(taskInfo != null){
    		oldAssignee = taskInfo.getAssignee();
    		//更新该表用作自定义驳回，查找上一级审核人信息
    		String hql = "from CustomPre where (assignee='"+oldAssignee+"'or previous='"+oldAssignee+"') and formID =? ";
    		List<CustomPre> cPrevious = customPreManager.find(hql,taskInfo.getProcessInstanceId());
    		for(CustomPre customPre : cPrevious ){
    			if(customPre.getAssignee().equals(oldAssignee)){
    				customPre.setAssignee(assignee);
    			}
    			if(customPre.getPrevious().equals(oldAssignee)){
    				customPre.setPrevious(assignee);
    			}
    			customPreManager.save(customPre);
    		}
     	 	taskInfo.setAssignee(assignee);
    	 }else{
    		 return 0;
    	 }
    	 
    	 taskInfoManager.save(taskInfo);
    	 //如果是自定义的申请，同步更新自定义的审核步骤表
    	 String hql = "update CustomApprover set approverId=? where approverId="+oldAssignee+" and businessKey="+taskInfo.getBusinessKey();
    	 int batchUpdate = customApproverManager.batchUpdate(hql, Long.parseLong(assignee));
    	 logger.debug("人工指定流程任务，同步更新自定义审核步骤表（0非自定义  1自定义）"+batchUpdate);
    	 
    	 TaskInfoApprovePosition task = taskInfoApprovePositionManager.findUniqueBy("taskId", taskId);
    	 if(task == null){
    		 task = new TaskInfoApprovePosition();
    		 task.setTaskId(taskId);
    		 task.setDataType("1");
    	 }
    	 task.setBusinessKey(task.getBusinessKey());
    	 task.setPositionType(strType);
    	 Map<String,String> mapPosition = null;
    	 if(strType.equals("1")){
    		 task.setPositionId(positionId);
    		 mapPosition=partyOrgConnector.getParentPartyEntityId(positionId.toString());
    	 }else{
    		 task.setPositionId(Long.parseLong(assignee));
    		 mapPosition=partyOrgConnector.getParentPartyEntityId(assignee);
    	 }
    	 if(mapPosition == null){
    		 logger.error("获取父级组织失败");
    		 return 0;
    	 }
    	 task.setPositionParentId(Long.parseLong(mapPosition.get("parent_id")));
    	 task.setApprovePositionName(mapPosition.get("position_name"));
    	 taskInfoApprovePositionManager.save(task);
    	 //保存日志  ckx
    	 TaskHistoryLog taskHistoryLog = new TaskHistoryLog();
    	 taskHistoryLog.setTaskInfo(taskInfo);
    	 taskHistoryLog.setCreator(userId); //修改人
    	 taskHistoryLog.setStartOwner(oldAssignee); //之前负责人
    	 taskHistoryLog.setEndOwner(assignee); //修改之后的负责人
    	 taskHistoryLog.setEventTime(new Date()); //创建人
    	 taskHistoryLog.setTaskStatus(strType); //类型  1：岗位  2：人员 
    	 taskHistoryLog.setContent(String.valueOf(positionId)); //岗位及人员ID

    	 taskHistoryLogManager.save(taskHistoryLog);
 	   	 logger.info("调整流程环节{{{{{{======}}}}}}用户："+userId+" taskId:"+taskId+" assignee:"+assignee+" strType(1:岗位 2:人):"+strType+" positionId:"+positionId);
    	 
    	 /*if(String.valueOf(typeId).equals("5")){
    		 task.setPositionType("1");//1为岗位 2为人
    		 task.setApprovePositionName("");
    		 task.setPositionId(Long.parseLong(assignee));
    		 //查询其上级机构id
    		 String partyByChildId = "select e.id from party_entity e join party_struct s on e.ID=s.PARENT_ENTITY_ID where s.CHILD_ENTITY_ID=?";
    		 Map<String,Object> superParty = jdbcTemplate.queryForMap(partyByChildId);
    		 String superPartyId = superParty.get("id").toString();
    		 task.setPositionParentId(Long.parseLong(superPartyId));
    	 }else{
    		 task.setPositionType("2");
    	 }*/
    	 return 1;
     }
    // ~ ==================================================
    @Resource
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Resource
    public void setTaskInfoManager(TaskInfoManager taskInfoManager) {
        this.taskInfoManager = taskInfoManager;
    }

    @Resource
    public void setTaskParticipantManager(TaskParticipantManager taskParticipantManager) {
        this.taskParticipantManager = taskParticipantManager;
    }

    @Resource
    public void setTaskConfUserManager(TaskConfUserManager taskConfUserManager) {
        this.taskConfUserManager = taskConfUserManager;
    }

    @Resource
    public void setTaskDeadlineManager(TaskDeadlineManager taskDeadlineManager) {
        this.taskDeadlineManager = taskDeadlineManager;
    }

    @Resource
    public void setInternalProcessConnector(InternalProcessConnector internalProcessConnector) {
        this.internalProcessConnector = internalProcessConnector;
    }

    @Resource
    public void setTaskDefinitionConnector(TaskDefinitionConnector taskDefinitionConnector) {
        this.taskDefinitionConnector = taskDefinitionConnector;
    }

    @Resource
    public void setFormConnector(FormConnector formConnector) {
        this.formConnector = formConnector;
    }

    public void setHumanTaskListeners(List<HumanTaskListener> humanTaskListeners) {
        this.humanTaskListeners = humanTaskListeners;
    }

    @Resource
    public void setDictConnectorImpl(DictConnector dictConnector) {
        this.dictConnector = dictConnector;
    }
    @Resource
    public void setBusinessDetailManager(BusinessDetailManager businessDetailManager) {
    	this.businessDetailManager = businessDetailManager;
    }
    
    @Resource
	public void setOrgConnector(OrgConnector orgConnector) {
		this.orgConnector = orgConnector;
	}
    
    @Resource
	public void setPartyOrgConnector(PartyOrgConnector partyOrgConnector) {
		this.partyOrgConnector = partyOrgConnector;
	}
    
    @Resource
	public void setTaskInfoApprovePositionManager(TaskInfoApprovePositionManager taskInfoApprovePositionManager) {
		this.taskInfoApprovePositionManager = taskInfoApprovePositionManager;
	}
    /**
     * 查询历史小工具的数据
     * ckx
     */
	public Page findHistoryTool(String searchUserId ,String userId, String tenantId,
			List<PropertyFilter> propertyFilters, Page page) {

        String sqlPagedQuerySelect = "select * from (select t.id,t.BUSINESS_KEY,t.PROCESS_INSTANCE_ID,k.applyCode,ap.approve_position_name,t.name,t.PRESENTATION_SUBJECT,k.theme,k.USER_ID,p.FULL_NAME,"
        		+"k.ucode,k.businesstypeid,k.businesstypename,k.businessdetailid,k.businessdetailname,k.systemid,k.systemname,k.areaid,k.areaname,t.action,"
			    +"k.companyid,k.companyname,t.create_time,t.catalog,t.assignee,k.url,t.suspend_Status AS pro_falg,k.audit_status AS pro_status,ap.position_id,ap.data_type,t.COMPLETE_TIME from task_info_approve_position ap "
			    +" left join task_info t on ap.task_id = t.ID left join kv_record k on t.BUSINESS_KEY = k.BUSINESS_KEY LEFT JOIN person_info p ON k.USER_ID = p.ID where t.CATALOG != 'copy' ) s where assignee != '' and 1=1 ";

        String sqlPagedQueryCount = "select count(*) from (select t.id,t.BUSINESS_KEY,t.PROCESS_INSTANCE_ID,k.applyCode,ap.approve_position_name,t.name,t.PRESENTATION_SUBJECT,k.theme,k.USER_ID,p.FULL_NAME,"
        		+"k.ucode,k.businesstypeid,k.businesstypename,k.businessdetailid,k.businessdetailname,k.systemid,k.systemname,k.areaid,k.areaname,t.action,"
			    +"k.companyid,k.companyname,t.create_time,t.catalog,t.assignee,k.url,t.suspend_Status AS pro_falg,k.audit_status AS pro_status,ap.position_id,ap.data_type,t.COMPLETE_TIME from task_info_approve_position ap "
			    +" left join task_info t on ap.task_id = t.ID left join kv_record k on t.BUSINESS_KEY = k.BUSINESS_KEY LEFT JOIN person_info p ON k.USER_ID = p.ID where t.CATALOG != 'copy' ) s where assignee != '' and 1=1 ";

        StringBuilder buff = new StringBuilder();
        List<Object> paramList = new ArrayList<Object>();
        boolean checkWhere = sqlPagedQuerySelect.toLowerCase().indexOf("where") == -1;
        PropertyFilterUtils.buildConfigurations(propertyFilters, buff, paramList, checkWhere);
        String sql = buff.toString();
        String positionIdSql = "";
        if(StringUtils.isNotBlank(searchUserId)){
        	/*String positionId = "";
        	//根据人员id查询岗位
        	List<Map<String,Object>> queryForList = jdbcTemplate.queryForList("select e.* from party_entity e join party_struct s on e.id=s.PARENT_ENTITY_ID where  e.DEL_FLAG = '0' and TYPE_ID = '5' and s.CHILD_ENTITY_ID= '"+searchUserId+"';");
        	if(null != queryForList && queryForList.size() > 0){
        		for (Map<String, Object> map : queryForList) {
        			String id = StringUtil.toString(map.get("id"));
        			positionId += id+",";
				}
        	}
        	positionId += searchUserId;*/
        	positionIdSql = " and assignee = "+searchUserId+"";
        }
        String countSql = sqlPagedQueryCount + " " + sql + " " +positionIdSql;
        String selectSql = sqlPagedQuerySelect + " " + sql + " " +positionIdSql + " order by COMPLETE_TIME DESC limit " + page.getStart() + ","
                + page.getPageSize();

        logger.debug("countSql : {}", countSql);
        logger.debug("selectSql : {}", selectSql);

        Object[] params = paramList.toArray();
        int totalCount = jdbcTemplate.queryForObject(countSql, Integer.class, params);
        List<Map<String, Object>> list = jdbcTemplate.queryForList(selectSql, params);
        List<UnfinishProcessInstance> unfinishPros = new ArrayList<UnfinishProcessInstance>();
        
        for (Map<String, Object> map : list) {
            UnfinishProcessInstance uModelInstance = convertUnfinishProsDTO(map);
            uModelInstance.setStatus(dictConnector.findDictNameByValue("RecordStatus", map.get("pro_status").toString()));
            uModelInstance.setApprovePositionName(null != map.get("approve_position_name")
            		? StringUtil.toString(map.get("approve_position_name")) : StringUtil.toString(map.get("name")));
            uModelInstance.setPositionId(StringUtil.toString(map.get("position_id")));
            uModelInstance.setDataType(StringUtil.toString(map.get("data_type")));
            unfinishPros.add(uModelInstance);
            //System.out.println(uModelInstance.toString());
        }

        page.setTotalCount(totalCount);
        page.setResult(unfinishPros);

        return page;
	}
	
	
	/**
	 * 日志查询
	 * ckx
	 */
	@Override
	public Page findHistoryToolLog(String searchUserId, String userId,String tenantId, List<PropertyFilter> propertyFilters, Page page) {
		
		String sqlPagedQuerySelect = "select l.TASK_ID as id,l.event_time,l.creator,l.start_owner,l.end_owner,l.task_status,l.content as position_id,k.applyCode,k.theme,i.presentation_subject,p.approve_position_name,p.business_key,p.data_type "
				+ "from task_history_log l LEFT JOIN task_info i on l.TASK_ID = i.ID LEFT JOIN kv_record k on i.business_key = k.BUSINESS_KEY LEFT JOIN task_info_approve_position p on l.TASK_ID = p.task_id where 1=1 ";

        String sqlPagedQueryCount = "select count(*) "
				+ "from task_history_log l LEFT JOIN task_info i on l.TASK_ID = i.ID LEFT JOIN kv_record k on i.BUSINESS_KEY = k.BUSINESS_KEY LEFT JOIN task_info_approve_position p on l.TASK_ID = p.task_id where 1=1 ";

        StringBuilder buff = new StringBuilder();
        List<Object> paramList = new ArrayList<Object>();
        boolean checkWhere = sqlPagedQuerySelect.toLowerCase().indexOf("where") == -1;
        PropertyFilterUtils.buildConfigurations(propertyFilters, buff, paramList, checkWhere);
        String sql = buff.toString();
        String countSql = sqlPagedQueryCount + " " + sql ;
        String selectSql = sqlPagedQuerySelect + " " + sql + " order by l.event_time DESC limit " + page.getStart() + ","
                + page.getPageSize();

        logger.debug("countSql : {}", countSql);
        logger.debug("selectSql : {}", selectSql);

        Object[] params = paramList.toArray();
        int totalCount = jdbcTemplate.queryForObject(countSql, Integer.class, params);
        List<Map<String, Object>> list = jdbcTemplate.queryForList(selectSql, params);
        List<TaskToolLog> taskToolLogList = new ArrayList<TaskToolLog>();
        for (Map<String, Object> map : list) {
        	TaskToolLog taskToolLog = convertTaskToolLog(map);
        	taskToolLogList.add(taskToolLog);
        }
        page.setTotalCount(totalCount);
        page.setResult(taskToolLogList);
        return page;
	}
	
	
	private TaskToolLog convertTaskToolLog(Map<String, Object> map) {
		if ((map == null) || map.isEmpty()) {
            logger.info("finishProcessInstance[{}] is null.", map);
            return null;
        }
		TaskToolLog taskToolLog = new TaskToolLog();
		taskToolLog.setId(StringUtil.toString(map.get("id")));
		taskToolLog.setApplyCode(StringUtil.toString(map.get("applyCode")));
		taskToolLog.setTheme(null != map.get("theme") ? StringUtil.toString(map.get("presentation_subject")) : "");
		taskToolLog.setStartAssignee(StringUtil.toString(map.get("start_owner")));
		taskToolLog.setEndAssignee(StringUtil.toString(map.get("end_owner")));
		taskToolLog.setPositionId(StringUtil.toString(map.get("position_id")));
		String positionType = StringUtil.toString(map.get("task_status"));
		if("1".equals(positionType)){
			taskToolLog.setPositionType("岗位");
		}else if("2".equals(positionType)){
			taskToolLog.setPositionType("人员");
		}
		taskToolLog.setDataType(StringUtil.toString(map.get("data_type")));
		taskToolLog.setApprovePositionName(StringUtil.toString(map.get("approve_position_name")));
		taskToolLog.setBusinessKey(StringUtil.toString(map.get("business_key")));
		taskToolLog.setCreator(StringUtil.toString(map.get("creator")));
		taskToolLog.setCreateTime((Date)map.get("event_time"));
		
		return taskToolLog;
		
	}

	/**
	 * 根据菜单查询该菜单下的数据
	 * ckx
	 */
	@Override
	public long queryTaskCount(String title) {
		title = title.trim();
		String userId = currentUserHolder.getUserId();
		int totalCount = 0;
		String countSql = "";
		if("待办审批".equals(title)){
			countSql = "select count(*) from (select i.id,i.process_instance_id,i.action,r.applycode,i.presentation_subject,"
	                + "r.theme,r.user_id,p.full_name,r.ucode,r.businesstypeid,r.businesstypename,r.businessdetailid,"
	                + "r.businessdetailname,r.systemid,r.systemname,r.areaid,r.areaname,r.companyid,r.companyname,"
	                + "r.create_time,i.catalog,i.complete_time,i.assignee,r.url" + " from task_info i"
	                + " inner join kv_record r on i.business_key = r.id"
	                + " inner join person_info p on r.user_id = p.id"
	                + " where i.assignee  ='" + userId + "' and i.`status` = 'active' and i.CATALOG !='copy') t where  assignee  ='" + userId + "'";
		}else if("待领审批".equals(title)){
			List<String> partyIds = new ArrayList<String>();
	        String strIds = "";
	        partyIds.addAll(this.findGroupIds(userId));
	        partyIds.addAll(this.findUserIds(userId));
	        for (String str : partyIds) {
	            strIds += "'" + str + "',";
	        }
	        strIds = strIds.substring(0, strIds.length() - 1);
			countSql = "select count(*) from (select DISTINCT i.id,i.process_instance_id,i.action,r.applycode,i.presentation_subject,"
	                + "r.theme,r.user_id,p.full_name,r.ucode,r.businesstypeid,r.businesstypename,r.businessdetailid,"
	                + "r.businessdetailname,r.systemid,r.systemname,r.areaid,r.areaname,r.companyid,r.companyname,"
	                + "r.create_time,i.catalog,t_p.REF,r.url" + " from task_info i"
	                + " inner join task_participant t_p on i.id = t_p.task_id"
	                + " inner join kv_record r on i.business_key = r.id"
	                + " inner join person_info p on r.user_id = p.id"
	                + " where ifnull(i.ASSIGNEE,'')='' and i.`status`='active') t where REF in (" + strIds + ")";
		}else if("抄送审批".equals(title)){
			//获取当前登录人的岗位id，若有多个岗位，放入一个字符串中
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
	    		postID = "9999";
	    	}
	    	postID = postID +","+ userId;
	        countSql = "select count(1) from(select DISTINCT i.process_instance_id,r.applycode,"
	                + "r.theme,r.user_id,p.full_name,r.ucode,r.businesstypeid,r.businesstypename,r.businessdetailid,"
	                + "r.businessdetailname,r.systemid,r.systemname,r.areaid,r.areaname,r.companyid,r.companyname,"
	                + "r.create_time,i.catalog,r.url,i.suspend_Status as pro_falg,r.audit_status as pro_status,i.BUSINESS_KEY,i.presentation_subject "
	                + " from task_info i" + " inner join kv_record r on i.business_key = r.id  JOIN task_info_approve_position tap ON i.ID = tap.task_id "
	                + " inner join person_info p on r.user_id = p.id"
	                + " where i.CATALOG ='copy' and tap.position_id in (" + postID + ") ) t"
	                + " left join (SELECT MAX(ti.COMPLETE_TIME) as COMPLETE_TIME,ti.BUSINESS_KEY FROM task_info ti"
			        + " WHERE ti.`STATUS` = 'complete' and ti.CATALOG = 'normal' group by ti.BUSINESS_KEY ) ti on t.business_key = ti.BUSINESS_KEY"
	                + " LEFT JOIN (SELECT MIN(tt.CREATE_TIME) AS cc_time,tt.BUSINESS_KEY FROM task_info tt GROUP BY tt.BUSINESS_KEY) tt ON t.business_key = tt.BUSINESS_KEY"
	                + " LEFT JOIN ( SELECT tip.id as tip_id,tip.BUSINESS_KEY,tip.user_id as cc_user_id FROM task_info_copy tip where tip.user_id = '" + userId + "') tip ON t.business_key = tip.BUSINESS_KEY "
	                + " where 1=1  and pro_status  ='" + 2 + " ' and tip_id is null ";
		}else if("未结流程".equals(title)){
			countSql = "select count(*) from (select hi.id_ as id,hi.proc_inst_id_ as proc_inst_id,"
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
		}
		if(StringUtils.isNotBlank(countSql)){
			totalCount = jdbcTemplate.queryForObject(countSql, Integer.class);
		}
		return totalCount;
	}

}
