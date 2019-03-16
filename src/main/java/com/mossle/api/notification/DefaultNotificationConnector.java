package com.mossle.api.notification;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.mossle.api.template.TemplateConnector;
import com.mossle.api.template.TemplateDTO;
import com.mossle.core.template.TemplateService;

public class DefaultNotificationConnector implements NotificationConnector,
        NotificationRegistry {
    private Map<String, NotificationHandler> map = new HashMap<String, NotificationHandler>();
    private TemplateConnector templateConnector;
    private TemplateService templateService;

    public void send(NotificationDTO notificationDto, String tenantId) {
        if (notificationDto.getTemplate() != null) {
            TemplateDTO templateDto = templateConnector.findByCode(
                    notificationDto.getTemplate(), tenantId);
            String subject = this.processTemplate(
                    templateDto.getField("subject"), notificationDto.getData());
            String content = this.processTemplate(
                    templateDto.getField("content"), notificationDto.getData());

            if (subject != null) {
                notificationDto.setSubject(subject);
            }

            if (content != null) {
                notificationDto.setContent(content);
            }
        }

        List<String> types = notificationDto.getTypes();

        for (String type : types) {
            sendByType(type, notificationDto, tenantId);
        }
    }
    
    /**
     * 其他功能发送消息
     * @param notificationDto
     * @param  subject    标题
     * @param  content    内容
     */
    public void send(NotificationDTO notificationDto, String tenantId, String subject, String content) {
    	
           
        if (subject != null) {
            notificationDto.setSubject(subject);
        }

        if (content != null) {
            notificationDto.setContent(content);
        }
        
        sendByType("msg", notificationDto, tenantId);
        
    }
    
    /**
     * 功能行发送消息
     * @param bussinessId 业务ID
     * @param tenantId 租用者ID
     * @param sender 发送者
     * @param receiver 接收者
     * @param title 标题
     * @param content 内容
     * @param msgType 消息类型
     */
    public void send(String bussinessId, String tenantId, String sender, String receiver,
    		String title, String content, int msgType)
    {
    	try{
        	NotificationDTO notificationDto = new NotificationDTO();
            notificationDto.setReceiver(receiver);   // 接收者
            notificationDto.setReceiverType("userid");        // 默认值，不用修改
            notificationDto.setSender(sender); // 发送者
            
            Map<String, Object> data = new HashMap<String, Object>();
            data.put("humanTaskId", bussinessId);   // 业务主键
            notificationDto.setData(data);
            //ckx 抄送单独处理
            if(msgType == 99){
            	//审核未通过
            	notificationDto.setMsgType(0);
            	notificationDto.setIsSendMsg("2");
            }else{
            	notificationDto.setMsgType(msgType);// 消息类型   0:流程 1:项目  2:任务  3:议题   4:汇报   5:公告
            }
            
            
            send(notificationDto, tenantId, title,content);

        }
        catch(Exception e)
        {
        	//logger.debug(strFn+"的消息提醒出现异常，"+e.getMessage()+"\r\n"+e.getStackTrace());
        	try {
				throw e;
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
        }
    }
    
    
    public void sendByType(String type, NotificationDTO notificationDto,
            String tenantId) {
        NotificationHandler notificationHandler = map.get(type);

        if (notificationHandler == null) {
            return;
        }

        notificationHandler.handle(notificationDto, tenantId);
    }

    public String processTemplate(String template, Map<String, Object> data) {
        return templateService.renderText(template, data);
    }

    public void register(NotificationHandler notificationHandler) {
        map.put(notificationHandler.getType(), notificationHandler);
    }

    public void unregister(NotificationHandler notificationHandler) {
        map.remove(notificationHandler.getType());
    }

    public Collection<String> getTypes(String tenantId) {
        return map.keySet();
    }

    public void setMap(Map<String, NotificationHandler> map) {
        this.map = map;
    }

    public void setTemplateConnector(TemplateConnector templateConnector) {
        this.templateConnector = templateConnector;
    }

    public void setTemplateService(TemplateService templateService) {
        this.templateService = templateService;
    }
}
