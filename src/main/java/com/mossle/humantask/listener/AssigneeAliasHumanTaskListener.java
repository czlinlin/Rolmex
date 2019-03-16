package com.mossle.humantask.listener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.mossle.humantask.persistence.domain.TaskInfo;
import com.mossle.humantask.persistence.domain.TaskParticipant;
import com.mossle.humantask.persistence.manager.TaskParticipantManager;
import com.mossle.humantask.rule.ActivityAssigneeRule;
import com.mossle.humantask.rule.AssigneeRule;
import com.mossle.humantask.rule.CompanyManageAssigneeRule;
import com.mossle.humantask.rule.EqualsRuleMatcher;
import com.mossle.humantask.rule.InitiatorAssigneeRule;
import com.mossle.humantask.rule.PositionAssigneeRule;
import com.mossle.humantask.rule.PrefixRuleMatcher;
import com.mossle.humantask.rule.RuleMatcher;
import com.mossle.humantask.rule.SuperiorAssigneeRule;
import com.mossle.party.persistence.domain.PartyEntity;
import com.mossle.party.persistence.manager.PartyEntityManager;
import com.mossle.spi.process.InternalProcessConnector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 处理负责人配置的别名.
 */
public class AssigneeAliasHumanTaskListener implements HumanTaskListener {
    private static Logger logger = LoggerFactory
            .getLogger(AssigneeAliasHumanTaskListener.class);
    private InternalProcessConnector internalProcessConnector;
    private TaskParticipantManager taskParticipantManager;
    private PartyEntityManager partyEntityManager;
    private Map<RuleMatcher, AssigneeRule> assigneeRuleMap = new HashMap<RuleMatcher, AssigneeRule>();

    public AssigneeAliasHumanTaskListener() {
        SuperiorAssigneeRule superiorAssigneeRule = new SuperiorAssigneeRule();
        PositionAssigneeRule positionAssigneeRule = new PositionAssigneeRule();
        InitiatorAssigneeRule initiatorAssigneeRule = new InitiatorAssigneeRule();
        ActivityAssigneeRule activityAssigneeRule = new ActivityAssigneeRule();
        CompanyManageAssigneeRule manageAssigneeRule = new CompanyManageAssigneeRule();
        
        assigneeRuleMap.put(new EqualsRuleMatcher("常用语:直接上级"), superiorAssigneeRule);
        assigneeRuleMap.put(new EqualsRuleMatcher("常用语:流程发起人"), initiatorAssigneeRule);
        assigneeRuleMap.put(new PrefixRuleMatcher("岗位"), positionAssigneeRule);
        assigneeRuleMap.put(new PrefixRuleMatcher("环节处理人"), activityAssigneeRule);
        assigneeRuleMap.put(new EqualsRuleMatcher("常用语:公司管理者"), manageAssigneeRule);
    }

    @Override
    public void onCreate(TaskInfo taskInfo) throws Exception {
    	
    	boolean isAssigneeUser = true;   // 是否直接指定处理人
    	
        String assignee = taskInfo.getAssignee();
        logger.debug("assignee : {}", assignee);

        if (assignee == null) {
            return;
        }

        if (assignee.startsWith("${")) {
            assignee = (String) internalProcessConnector.executeExpression(
                    taskInfo.getTaskId(), assignee);
            
            PartyEntity vo = partyEntityManager.get(Long.parseLong(assignee));
            
            //taskInfo.setAssignee(assignee);
            taskInfo.setAssignee(vo.getRef());
            return;
        }

        for (Map.Entry<RuleMatcher, AssigneeRule> entry : assigneeRuleMap
                .entrySet()) {
            RuleMatcher ruleMatcher = entry.getKey();

            if (!ruleMatcher.matches(assignee)) {
                continue;
            }
            
            isAssigneeUser = false;
            String value = ruleMatcher.getValue(assignee);
            AssigneeRule assigneeRule = entry.getValue();
            logger.debug("value : {}", value);
            logger.debug("assigneeRule : {}", assigneeRule);

            if (assigneeRule instanceof SuperiorAssigneeRule) {
                this.processSuperior(taskInfo, assigneeRule, value);
            } else if (assigneeRule instanceof InitiatorAssigneeRule) {
                this.processInitiator(taskInfo, assigneeRule, value);
            } else if (assigneeRule instanceof ActivityAssigneeRule) {
                this.processActivityAssignee(taskInfo, assigneeRule, value);
            } else if (assigneeRule instanceof PositionAssigneeRule) {
                this.processPosition(taskInfo, assigneeRule, value);
            }  else if (assigneeRule instanceof CompanyManageAssigneeRule) {
                this.processCompanyManage(taskInfo, assigneeRule, value);
            }
        }
        
        // zyl 直接指定节点处理人
        if (isAssigneeUser) {
        	PartyEntity vo = partyEntityManager.get(Long.parseLong(assignee));
        	//ckx  update 2018/9/7
        	taskInfo.setAssignee(vo.getId().toString());
        }
    }

    @Override
    public void onComplete(TaskInfo taskInfo) throws Exception {
    }

    public void processSuperior(TaskInfo taskInfo, AssigneeRule assigneeRule,
            String value) {
        String processInstanceId = taskInfo.getProcessInstanceId();
        String startUserId = internalProcessConnector
                .findInitiator(processInstanceId);
        String userId = assigneeRule.process(startUserId);
        logger.debug("userId : {}", userId);
        taskInfo.setAssignee(userId);
    }

    // 获取公司管理者
    public void processCompanyManage(TaskInfo taskInfo, AssigneeRule assigneeRule,
            String value) {
    	
        String processInstanceId = taskInfo.getProcessInstanceId();
        String startUserId = internalProcessConnector
                .findInitiator(processInstanceId);
        List<String> userIds = assigneeRule.process(value,startUserId);
 
        if (userIds.isEmpty()) {
            logger.info("{} userIds is empty.公司管理者为空", taskInfo.getBusinessKey());
        } else if (userIds.size() == 1) {
            taskInfo.setAssignee(userIds.get(0));
        } else {
        	taskInfo.setAssignee("");
        	for (String userid : userIds) {
				TaskParticipant taskParticipant = new TaskParticipant();
                taskParticipant.setCategory("companymanage");

            	taskParticipant.setRef(userid);

                taskParticipant.setType("user");
                taskParticipant.setTaskInfo(taskInfo);
                taskParticipantManager.save(taskParticipant);
			}
        }
    }
    
    public void processInitiator(TaskInfo taskInfo, AssigneeRule assigneeRule,
            String value) {
        String processInstanceId = taskInfo.getProcessInstanceId();
        String startUserId = internalProcessConnector
                .findInitiator(processInstanceId);
        String userId = assigneeRule.process(startUserId);
        logger.debug("userId : {}", userId);
        taskInfo.setAssignee(userId);
    }

    public void processActivityAssignee(TaskInfo taskInfo,
            AssigneeRule assigneeRule, String value) {
        String processInstanceId = taskInfo.getProcessInstanceId();
        List<String> userIds = assigneeRule.process(value, processInstanceId);
        logger.debug("userIds : {}", userIds);

        if (!userIds.isEmpty()) {
            taskInfo.setAssignee(userIds.get(0));
        }
    }

    public void processPosition(TaskInfo taskInfo, AssigneeRule assigneeRule,
            String value) {
        String processInstanceId = taskInfo.getProcessInstanceId();
        String startUserId = internalProcessConnector
                .findInitiator(processInstanceId);
        List<String> userIds = assigneeRule.process(value, startUserId);
        logger.debug("userIds : {}", userIds);

        if (userIds.isEmpty()) {
            logger.info("{} userIds is empty", taskInfo.getCode());
            
            TaskParticipant taskParticipant = new TaskParticipant();
            taskParticipant.setTaskInfo(taskInfo);
            taskParticipant.setCategory("candidate");
            taskParticipant.setType("group");
            if (value.indexOf("岗位:")!=-1) {
                taskParticipant.setRef(value.split(":")[1]);
            } else {
                taskParticipant.setRef(value);
            }
            taskParticipantManager.save(taskParticipant);
            
        } else if (userIds.size() == 1) {
            taskInfo.setAssignee(userIds.get(0));
        } else {
        	TaskParticipant taskParticipant = new TaskParticipant();
            taskParticipant.setTaskInfo(taskInfo);
            taskParticipant.setCategory("candidate");
            taskParticipant.setType("group");
            if (value.indexOf("岗位:")!=-1) {
                taskParticipant.setRef(value.split(":")[1]);
            } else {
                taskParticipant.setRef(value);
            }
            taskParticipantManager.save(taskParticipant);
            
        }
    }

    @Resource
    public void setInternalProcessConnector(
            InternalProcessConnector internalProcessConnector) {
        this.internalProcessConnector = internalProcessConnector;
    }

    @Resource
	public void setTaskParticipantManager(
			TaskParticipantManager taskParticipantManager) {
		this.taskParticipantManager = taskParticipantManager;
	}

	@Resource
    public void setPartyEntityManager(
    		PartyEntityManager partyEntityManager) {
        this.partyEntityManager = partyEntityManager;
    }
    
}
