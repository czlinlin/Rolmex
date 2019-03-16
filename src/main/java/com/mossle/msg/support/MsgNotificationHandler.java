package com.mossle.msg.support;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import com.mossle.api.notification.NotificationDTO;
import com.mossle.api.notification.NotificationHandler;
import com.mossle.core.util.StringUtils;
import com.mossle.msg.persistence.domain.MsgInfo;
import com.mossle.msg.persistence.manager.MsgInfoManager;
import com.mossle.util.StringUtil;


public class MsgNotificationHandler implements NotificationHandler {
	
    private MsgInfoManager msgInfoManager;
    private String defaultSender = "";

    public void handle(NotificationDTO notificationDto, String tenantId) {
    	
        if (!"userid".equals(notificationDto.getReceiverType())) {
            return;
        }

        MsgInfo msgInfo = new MsgInfo();
        msgInfo.setName(StringUtils.replaceSingleQuote(notificationDto.getSubject()));
        msgInfo.setContent(StringUtils.replaceSingleQuote(notificationDto.getContent()));
        msgInfo.setReceiverId(notificationDto.getReceiver());
        msgInfo.setCreateTime(new Date());
        msgInfo.setStatus(0);
        msgInfo.setTenantId(tenantId);
        msgInfo.setMsgType(notificationDto.getMsgType());
        String humanTaskId = (String) notificationDto.getData().get("humanTaskId");
        msgInfo.setData(humanTaskId);
        
        if (StringUtils.isNotBlank(notificationDto.getSender())) {
            msgInfo.setSenderId(notificationDto.getSender());
        } else {
            msgInfo.setSenderId(defaultSender);
        }

        //ckx 单独处理抄送
        if("2".equals(notificationDto.getIsSendMsg())){
        	msgInfo.setIsSendMsg("2");
        }

        if ("arrival-copy".equals(notificationDto.getTemplate())) {
        	msgInfo.setIsSendMsg("2");
        }

        msgInfoManager.save(msgInfo);
    }

    public String getType() {
        return "msg";
    }

    public void setDefaultSender(String defaultSender) {
        this.defaultSender = defaultSender;
    }

    @Resource
    public void setMsgInfoManager(MsgInfoManager msgInfoManager) {
        this.msgInfoManager = msgInfoManager;
    }
}
