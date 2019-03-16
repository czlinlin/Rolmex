package com.mossle.bpm.listener;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.delegate.event.impl.ActivitiEntityEventImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.el.ExpressionManager;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.task.IdentityLink;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;

import com.mossle.api.humantask.HumanTaskConnector;
import com.mossle.api.humantask.HumanTaskConstants;
import com.mossle.api.humantask.HumanTaskDTO;
import com.mossle.api.humantask.ParticipantDTO;
import com.mossle.api.keyvalue.KeyValueConnector;
import com.mossle.api.keyvalue.Prop;
import com.mossle.api.keyvalue.Record;
import com.mossle.api.notification.NotificationConnector;
import com.mossle.api.user.UserConnector;
import com.mossle.bpm.persistence.domain.BpmConfUser;
import com.mossle.bpm.persistence.manager.BpmConfUserManager;
import com.mossle.bpm.support.DelegateTaskHolder;
import com.mossle.bpm.support.HumanTaskBuilder;
import com.mossle.core.mapper.BeanMapper;
import com.mossle.party.PartyConstants;
import com.mossle.party.persistence.domain.PartyEntity;
import com.mossle.party.persistence.manager.PartyEntityManager;
import com.mossle.party.support.PartyOrgConnector;
import com.mossle.user.PersonInfoConstants;

public class HumanTaskEventListener implements ActivitiEventListener {
	
    public static final int TYPE_COPY = 3;
    private static Logger logger = LoggerFactory.getLogger(HumanTaskEventListener.class);
    
    private HumanTaskConnector humanTaskConnector;
    private BpmConfUserManager bpmConfUserManager;
    private PartyEntityManager partyEntityManager;
    private KeyValueConnector keyValueConnector;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private UserConnector userConnector;
    @Autowired
    private NotificationConnector notificationConnector;
    @Autowired
    private PartyOrgConnector partyOrgConnector;

    private BeanMapper beanMapper = new BeanMapper();
    private String baseUrl;

    public void onEvent(ActivitiEvent event) {
        if (!(event instanceof ActivitiEntityEventImpl)) {
            return;
        }

        ActivitiEntityEventImpl activitiEntityEventImpl = (ActivitiEntityEventImpl) event;
        Object entity = activitiEntityEventImpl.getEntity();

        if (!(entity instanceof TaskEntity)) {
            return;
        }

        TaskEntity taskEntity = (TaskEntity) entity;

        try {
            switch (event.getType()) {
            case TASK_CREATED:   // 创建任务
                logger.info("create : {}", taskEntity.getId());
                this.onCreate(taskEntity);

                break;

            case TASK_ASSIGNED:   // 任务分配
                logger.debug("assign : {}", taskEntity.getId());
                this.onAssign(taskEntity);

                break;

            case TASK_COMPLETED:   // 任务完成
                logger.info("complete : {}", taskEntity.getId());
                this.onComplete(taskEntity);

                break;

            case ENTITY_DELETED:    // 删除
                logger.debug("delete : {}", taskEntity.getId());
                this.onDelete(taskEntity);

                break;

            default:
                logger.info("{} : {}", event.getType(), taskEntity.getId());
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    public void onCreate(DelegateTask delegateTask) throws Exception {
        HumanTaskDTO humanTaskDto = null;

        // 根据delegateTask创建HumanTaskDTO
        try {
            DelegateTaskHolder.setDelegateTask(delegateTask);

            humanTaskDto = this.createHumanTask(delegateTask);
            
            
            // 任务抄送
            this.checkCopyHumanTask(delegateTask, humanTaskDto);
        } catch (Exception ex) {
        	logger.info("=============== Create Task ===============");
        	logger.error(ex.getMessage(), ex);
        } finally {
            DelegateTaskHolder.clear();
        }

        if (humanTaskDto != null) {
            delegateTask.setAssignee(humanTaskDto.getAssignee());
            delegateTask.setOwner(humanTaskDto.getOwner());
        }
    }

    public void onAssign(DelegateTask delegateTask) throws Exception {
    }

    /**
     * 如果直接完成了activiti的task，要同步完成HumanTask.
     */
    public void onComplete(DelegateTask delegateTask) throws Exception {
        HumanTaskDTO humanTaskDto = humanTaskConnector
                .findHumanTaskByTaskId(delegateTask.getId());

        if ("complete".equals(humanTaskDto.getStatus())) {
            return;
        }

        humanTaskDto.setStatus("complete");
        humanTaskDto.setCompleteTime(new Date());

        if ("start".equals(humanTaskDto.getCatalog())) {
            humanTaskDto.setAction("提交");
        } else {
            humanTaskDto.setAction("完成");
        }

        humanTaskConnector.saveHumanTask(humanTaskDto, false);
    }

    public void onDelete(DelegateTask delegateTask) throws Exception {
        HumanTaskDTO humanTaskDto = humanTaskConnector
                .findHumanTaskByTaskId(delegateTask.getId());

        if (humanTaskDto == null) {
            return;
        }

        if ("complete".equals(humanTaskDto.getStatus())) {
            return;
        }
       
        humanTaskDto.setStatus("delete");
        humanTaskDto.setCompleteTime(new Date());
        humanTaskDto.setAction("驳回");
        humanTaskDto.setOwner(humanTaskDto.getAssignee());
        humanTaskDto.setAssignee(Authentication.getAuthenticatedUserId());
        humanTaskConnector.saveHumanTask(humanTaskDto, false);
    }

    /**
     * 是否会签任务.
     */
    public boolean isVote(DelegateTask delegateTask) {
        ExecutionEntity executionEntity = (ExecutionEntity) delegateTask
                .getExecution();
        ActivityImpl activityImpl = executionEntity.getActivity();

        return activityImpl.getProperty("multiInstance") != null;
    }

    public HumanTaskDTO createHumanTask(DelegateTask delegateTask)
            throws Exception {
    	
    	HumanTaskDTO humanTaskDto = null;
    	// 通过流程定义ID 查询流程配置的名称   zyl 20181015  主题显示
        /*String sql ="select p.name from bpm_process p"
        		+ " inner join bpm_conf_base b on p.CONF_BASE_ID=b.ID"
        		+ " where b.PROCESS_DEFINITION_ID=?";
        Map<String, Object> map = jdbcTemplate.queryForMap(sql, delegateTask.getProcessDefinitionId());

        if (map != null) {
        	humanTaskDto = new HumanTaskBuilder()
            	.setDelegateTask(delegateTask)
            	.setVote(this.isVote(delegateTask)).setPresentationSubject(getStringValue(map, "name")).build();
        	
        } else {*/
        	humanTaskDto = new HumanTaskBuilder()
            	.setDelegateTask(delegateTask)
            	.setVote(this.isVote(delegateTask)).build();
        //}

        humanTaskDto = humanTaskConnector.saveHumanTask(humanTaskDto);
        logger.debug("candidates : {}", delegateTask.getCandidates());

        for (IdentityLink identityLink : delegateTask.getCandidates()) {
            String type = identityLink.getType();
            ParticipantDTO participantDto = new ParticipantDTO();
            participantDto.setType(type);
            participantDto.setHumanTaskId(humanTaskDto.getId());

            if ("user".equals(type)) {
                participantDto.setCode(identityLink.getUserId());
            } else {
                participantDto.setCode(identityLink.getGroupId());
            }

            humanTaskConnector.saveParticipant(participantDto);
        }

        return humanTaskDto;
    }

    public void checkCopyHumanTask(DelegateTask delegateTask,
            HumanTaskDTO humanTaskDto) throws Exception {
        List<BpmConfUser> bpmConfUsers = bpmConfUserManager
                .find("from BpmConfUser where bpmConfNode.bpmConfBase.processDefinitionId=? and bpmConfNode.code=?",
                        delegateTask.getProcessDefinitionId(), delegateTask
                                .getExecution().getCurrentActivityId());
        logger.debug("{}", bpmConfUsers);

        ExpressionManager expressionManager = Context
                .getProcessEngineConfiguration().getExpressionManager();

        try {
            for (BpmConfUser bpmConfUser : bpmConfUsers) {
                logger.debug("status : {}, type: {}", bpmConfUser.getStatus(),
                        bpmConfUser.getType());
                logger.debug("value : {}", bpmConfUser.getValue());

                String value = expressionManager
                        .createExpression(bpmConfUser.getValue())
                        .getValue(delegateTask).toString();
                
                if (bpmConfUser.getStatus() == 1) {
                    if (bpmConfUser.getType() == TYPE_COPY) {
                        logger.info("copy humantask : {}, {}",
                                humanTaskDto.getId(), value);
                        
                        // zyl 2017-07-26
                        PartyEntity partyEntity = partyEntityManager.get(Long.parseLong(value));
                        
                        if (partyEntity != null && partyEntity.getDelFlag().equals(PersonInfoConstants.DELETE_FLAG_NO)) {
	                        if (partyEntity.getPartyType().getType().equals(PartyConstants.TYPE_USER)) { // 人员
	                        	this.copyHumanTask(humanTaskDto, partyEntity.getRef());
	                        } else {
	                        	// TODO 如果抄送岗位或小组 之类的 此处需要修改
	                        	this.copyHumanTask(humanTaskDto, value);
	                        }
                        }
                    }
                }
            }
            
            // zyl 2017-07-26  此节点是否存在自定义的抄送人
            copyCustomUser(delegateTask, humanTaskDto);
            
        } catch (Exception ex) {
            logger.debug(ex.getMessage(), ex);
        }
    }
    
    /**
     * 处理自定义抄送人
     * @author zyl
     * @param delegateTask
     * @param humanTaskDto
     */
	private void copyCustomUser(DelegateTask delegateTask, HumanTaskDTO humanTaskDto) {
		
		Record record = keyValueConnector.findByCode(humanTaskDto.getBusinessKey());
		
		String activityId = "";
		String copyUser = "";
		
		Map<String, Prop> props = record.getProps();
		for (Prop prop : props.values()) {  
			
			if (prop.getCode().equals("activityId")) {
				activityId = prop.getValue();
			}
		    if (prop.getCode().equals("copyUserValue")) {  // 存在抄送人
		    	copyUser = prop.getValue();
		    }
		  
		} 
		
		// 节点相同，且存在抄送人 
		if (delegateTask.getTaskDefinitionKey().equals(activityId) && StringUtils.isNotBlank(copyUser)) {
			//考虑抄送给多人的情况   ckx add 2018/9/7
			//if(copyUser.contains("岗位:")){
				String [] array = copyUser.split(",");
				int length = array.length;
				for(int k = 0;k<length;k++){
					String positionType = "";
					String strId = array[k];
					if(strId.contains("岗位:")){
						positionType = "1";
						strId = strId.replace("岗位:", "");
						SaveCopyHumanTaskPost(humanTaskDto, strId, positionType);
					}else{
						positionType = "2";
						SaveCopyHumanTaskPost(humanTaskDto, strId, positionType);
					}
					
				}
			//}
				/*else{
				String [] array = copyUser.split(",");
				int length = array.length;
				for(int k = 0;k<length;k++){
					
					PartyEntity partyEntity = partyEntityManager.get(Long.parseLong(array[k]));
					if (partyEntity!= null && partyEntity.getDelFlag().equals(PersonInfoConstants.DELETE_FLAG_NO)) {
					    if (partyEntity.getPartyType().getType().equals(PartyConstants.TYPE_USER)) { // 人员
					    	this.copyHumanTask(humanTaskDto, partyEntity.getRef());
					    } else {
					    	// TODO 如果抄送岗位或小组 之类的 此处需要修改
					    	this.copyHumanTask(humanTaskDto, partyEntity.getId().toString());
					    }
					}
				}
			}*/
			
		}
	}

    public void copyHumanTask(HumanTaskDTO humanTaskDto, String userId) {
        // 创建新任务
        HumanTaskDTO target = new HumanTaskDTO();
        beanMapper.copy(humanTaskDto, target);
        target.setId(null);
        target.setCategory("copy");
        target.setAssignee(userId);
        target.setCatalog(HumanTaskConstants.CATALOG_COPY);

        humanTaskConnector.saveHumanTask(target);
    }
    //保存task_info 和岗位表  ckx add 2018/9/7
    public void SaveCopyHumanTaskPost(HumanTaskDTO humanTaskDto, String userId,String positionType) {
        // 创建新任务
        HumanTaskDTO target = new HumanTaskDTO();
        beanMapper.copy(humanTaskDto, target);
        target.setId(null);
        target.setCategory("copy");
        target.setAssignee(userId);
        target.setCatalog(HumanTaskConstants.CATALOG_COPY);

        humanTaskConnector.saveHumanTask(target);
        
        Map<String,String> mapPosition=partyOrgConnector.getParentPartyEntityId(userId);
        //保存抄送表
        if("copy".equals(target.getCatalog())){
        	jdbcTemplate.execute("insert into task_info_approve_position (task_id,position_id,position_type,business_key,position_parentId,approve_position_name)"
        			+ " values ('"+target.getId()+"','"+userId+"','"+positionType+"','"+target.getBusinessKey()+"',"+mapPosition.get("parent_id")+",'"+mapPosition.get("position_name")+"')");
        }
    }


    public boolean isFailOnException() {
        return false;
    }

    /**
   	 * 获得string值.
   	 */
   	public String getStringValue(Map<String, Object> map, String name) {
   		Object value = map.get(name);

   		if (value == null) {
   			return null;
   		}

   		if (value instanceof String) {
   			return (String) value;
   		}

   		return value.toString();
   	}
   	
    @Resource
    public void setHumanTaskConnector(HumanTaskConnector humanTaskConnector) {
        this.humanTaskConnector = humanTaskConnector;
    }

    @Resource
    public void setBpmConfUserManager(BpmConfUserManager bpmConfUserManager) {
        this.bpmConfUserManager = bpmConfUserManager;
    }
    
    @Resource
    public void setPartyEntityManager(PartyEntityManager partyEntityManager) {
        this.partyEntityManager = partyEntityManager;
    }
    
    @Resource
    public void setKeyValueConnector(KeyValueConnector keyValueConnector) {
        this.keyValueConnector = keyValueConnector;
    }
    @Value("${application.baseUrl}")
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
}
