package com.mossle.api.notification;

import java.util.Collection;

public interface NotificationConnector {
	
	/**
	 * 流程发送消息
	 * @param notificationDto
	 * @param tenantId
	 */
    void send(NotificationDTO notificationDto, String tenantId);

    /**
     * 其他功能发送消息
     * @param notificationDto
     * @param  subject    标题
     * @param  content    内容
     */
    void send(NotificationDTO notificationDto, String tenantId, String subject, String content);
    
    /**
     * 功能行发送消息
     * @param bussinessId 业务ID
     * @param tenantId 租用者ID
     * @param sender 发送者
     * @param receiver 接收者
     * @param title 标题
     * @param  msgType 消息类型  0:流程 1:项目  2:任务  3:议题   4:汇报   5:公告
     * @param content 内容
     */
    public void send(String bussinessId,String tenantId,String sender,String receiver,String title,String content, int msgType);
    
    Collection<String> getTypes(String tenantId);
}
