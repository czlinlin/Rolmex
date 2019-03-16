package com.mossle.humantask.listener;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.mossle.api.keyvalue.KeyValueConnector;
import com.mossle.api.keyvalue.Record;
import com.mossle.api.notification.NotificationConnector;
import com.mossle.api.notification.NotificationDTO;
import com.mossle.api.user.UserConnector;
import com.mossle.api.user.UserDTO;
import com.mossle.humantask.persistence.domain.TaskInfo;
import com.mossle.keyvalue.persistence.domain.RecordInfo;
import com.mossle.keyvalue.persistence.manager.RecordManager;
import com.mossle.msg.MsgConstants;
import com.mossle.spi.humantask.TaskDefinitionConnector;
import com.mossle.spi.humantask.TaskNotificationDTO;
import com.mossle.spi.process.InternalProcessConnector;
import com.mossle.util.StringUtil;

import org.apache.commons.lang3.StringUtils;
import org.omg.CORBA.PRIVATE_MEMBER;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

public class TaskNotificationHumanTaskListener implements HumanTaskListener {
    private static Logger logger = LoggerFactory
            .getLogger(TaskNotificationHumanTaskListener.class);
    private TaskDefinitionConnector taskDefinitionConnector;
    private NotificationConnector notificationConnector;
    private UserConnector userConnector;
    private InternalProcessConnector internalProcessConnector;
    private String baseUrl;
    private KeyValueConnector keyValueConnector;
    private JdbcTemplate jdbcTemplate;
    //private RecordManager recordManager;
    
    @Override
    public void onCreate(TaskInfo taskInfo) throws Exception {
        // TODO: ignore notification when skip this task
        this.doNotice(taskInfo, "create");
    }

    @Override
    public void onComplete(TaskInfo taskInfo) throws Exception {
        this.doNotice(taskInfo, "complete");
    }

    public void doNotice(TaskInfo taskInfo, String eventName) {
    	
        String taskDefinitionKey = taskInfo.getCode();
        String processDefinitionId = taskInfo.getProcessDefinitionId();
        
        List<TaskNotificationDTO> taskNotifications = taskDefinitionConnector
                .findTaskNotifications(taskDefinitionKey, processDefinitionId, eventName);

        Map<String, Object> data = this.prepareData(taskInfo);

        if (!"不同意".equals(taskInfo.getAction())) {
	        for (TaskNotificationDTO taskNotification : taskNotifications) {
	        	String templateCode = "";
	        	if ("copy".equals(taskInfo.getCatalog())) {
	        		templateCode = "arrival-copy";
	        	} else {
	        		templateCode = taskNotification.getTemplateCode();
	        	}
	            
	            String type = taskNotification.getType();
	            String receiver = taskNotification.getReceiver();
	            UserDTO userDto = null;
	            List<Map<String, Object>> queryForUserList = null;
	            String oldAss = taskInfo.getAssignee();
	            if ("任务接收人".equals(receiver)) {
	                userDto = userConnector.findById(taskInfo.getAssignee());
	                //ckx   add 2018/9/7
	                if(null == userDto){
	                	
						try {
							//通过岗位查询人员
							queryForUserList = jdbcTemplate.queryForList("select e.id,name from party_entity e join party_struct s on e.id=s.CHILD_ENTITY_ID where e.DEL_FLAG = '0' and  s.PARENT_ENTITY_ID='"+taskInfo.getAssignee()+"';");
						} catch (DataAccessException e) {
						}
						//发送消息
						if(null != queryForUserList){
							for (Map<String, Object> map : queryForUserList) {
								String id = StringUtil.toString(map.get("id"));
								UserDTO userDTO2 = userConnector.findById(id);
								if(null != userDTO2){
									taskInfo.setAssignee(id);
									data = this.prepareData(taskInfo);
									NotificationDTO notificationDto = new NotificationDTO();
									notificationDto.setReceiver(userDTO2.getId());
									notificationDto.setReceiverType("userid");
									notificationDto.setTypes(Arrays.asList(type.split(",")));
									notificationDto.setData(data);
									notificationDto.setTemplate(templateCode);
									notificationDto.setMsgType(MsgConstants.MSG_TYPE_BPM);
									notificationConnector.send(notificationDto, taskInfo.getTenantId());
									taskInfo.setAssignee(oldAss);
								}
							}
						}
	                }
	            } else if ("流程发起人".equals(receiver)) {
	                String initiator = internalProcessConnector
	                        .findInitiator(taskInfo.getProcessInstanceId());
	                userDto = userConnector.findById(initiator);
	            }
	            /*else {     // zyl 2017-07-27  暂时屏蔽 如有特殊的需求在改造
	                userDto = userConnector.findById(receiver);
	            }*/
	
	            if (userDto == null) {
	                logger.debug("userDto is null : {}", receiver);
	
	                continue;
	            }
	
	            NotificationDTO notificationDto = new NotificationDTO();
	            notificationDto.setReceiver(userDto.getId());
	            notificationDto.setReceiverType("userid");
	            notificationDto.setTypes(Arrays.asList(type.split(",")));
	            notificationDto.setData(data);
	            notificationDto.setTemplate(templateCode);
	            notificationDto.setMsgType(MsgConstants.MSG_TYPE_BPM);
	            notificationConnector.send(notificationDto, taskInfo.getTenantId());
	        }
        }
    }

    public Map<String, Object> prepareData(TaskInfo taskInfo) {
        String assignee = taskInfo.getAssignee();
        String initiator = internalProcessConnector.findInitiator(taskInfo
                .getProcessInstanceId());
        UserDTO assigneeUser = null;

        if (StringUtils.isNotBlank(assignee)) {
            assigneeUser = userConnector.findById(assignee);
        }

        UserDTO initiatorUser = userConnector.findById(initiator);

        Map<String, Object> data = new HashMap<String, Object>();

        Map<String, Object> taskEntity = new HashMap<String, Object>();
        taskEntity.put("id", taskInfo.getId());
        taskEntity.put("name", taskInfo.getName());

        if (assigneeUser != null) {
            taskEntity.put("assignee", assigneeUser.getDisplayName());
        }
        Record record = keyValueConnector.findByCode(taskInfo.getBusinessKey());
        String theme = taskInfo.getPresentationSubject();
        if (record != null) {
        	data.put("url", record.getUrl() == null ? "" : record.getUrl());
        	
        	if (!StringUtils.isBlank(record.getTheme())) {
        		theme = record.getTheme();
        	}
        }
        data.put("theme", theme);
        data.put("task", taskEntity);
        data.put("initiator", initiatorUser.getDisplayName());
        data.put("humanTask", taskInfo);
        data.put("baseUrl", baseUrl);
        data.put("humanTaskId", Long.toString(taskInfo.getId()));
        data.put("processInstanceId", taskInfo.getProcessInstanceId() == null ? "" : taskInfo.getProcessInstanceId());
        data.put("action", taskInfo.getAction() == null ? "" : taskInfo.getAction());
        data.put("catalog", taskInfo.getCatalog() == null ? "" : taskInfo.getCatalog());
        return data;
    }

    @Resource
    public void setTaskDefinitionConnector(
            TaskDefinitionConnector taskDefinitionConnector) {
        this.taskDefinitionConnector = taskDefinitionConnector;
    }

    @Resource
    public void setNotificationConnector(
            NotificationConnector notificationConnector) {
        this.notificationConnector = notificationConnector;
    }

    @Resource
    public void setUserConnector(UserConnector userConnector) {
        this.userConnector = userConnector;
    }

    @Resource
    public void setInternalProcessConnector(
            InternalProcessConnector internalProcessConnector) {
        this.internalProcessConnector = internalProcessConnector;
    }
    
    @Resource
	public void setKeyValueConnector(KeyValueConnector keyValueConnector) {
		this.keyValueConnector = keyValueConnector;
	}
    
    @Resource
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
    /*@Resource
	public void setRecordManager(RecordManager recordManager) {
		this.recordManager = recordManager;
	}*/

	@Value("${application.baseUrl}")
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
}
