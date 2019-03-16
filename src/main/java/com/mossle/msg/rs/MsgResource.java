package com.mossle.msg.rs;

import com.mossle.core.annotation.Log;
import com.mossle.core.auth.CurrentUserHolder;
import com.mossle.core.mapper.JsonMapper;
import com.mossle.core.util.BaseDTO;
import com.mossle.msg.persistence.domain.MsgInfo;
import com.mossle.msg.persistence.manager.MsgInfoManager;
import com.mossle.msg.persistence.manager.VMsgInfoManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Component
@Path("msg")
public class MsgResource {
    private static Logger logger = LoggerFactory.getLogger(MsgResource.class);
    private MsgInfoManager msgInfoManager;
    private JsonMapper jsonMapper = new JsonMapper();
    private CurrentUserHolder currentUserHolder;
    private VMsgInfoManager vMsgInfoManager;
    private boolean enable = true;

    @GET
    @Path("unreadCount")
    @Produces(MediaType.APPLICATION_JSON)
    public BaseDTO unreadCount() {
        if (!enable) {
            return new BaseDTO();
        }
        String userId = currentUserHolder.getUserId();
        Integer count = vMsgInfoManager.getCount(
                "select count(*) from VMsgInfo where receiverId=? and status=0",
                userId);
        BaseDTO result = new BaseDTO();
        result.setData(count);

        return result;
    }

    @POST
    @Path("msg-info-read")
    @Produces(MediaType.APPLICATION_JSON)
    @Log(desc = "已读", action = "submit", operationDesc = "消息中心-消息-未读消息-已读操作")
    public BaseDTO msgRead(@FormParam("userId") Long userId) {
        BaseDTO result = new BaseDTO();
        try {
            if (userId == null || userId < 1) {
                result.setCode(500);
                logger.debug("消息已读操作-获取参数id错误");
                result.setMessage("获取参数错误");
                return result;
            }
            int count = 0;
            List<MsgInfo> msgInfos = msgInfoManager.findBy("receiverId", userId.toString());
            for (MsgInfo msgInfo : msgInfos) {
                if (msgInfo.getStatus() == 0) {
                    msgInfo.setStatus(1);
                    msgInfoManager.save(msgInfo);
                    count = 1;//变更为已读的数据
                }
            }
            if (count == 1) {
                result.setCode(200);
            } else if (count == 0) {
                result.setCode(500);
                logger.debug("已读操作-没有查询到信息");
                result.setMessage("没有可变更为已读状态的消息");
                return result;
            }
        } catch (ArithmeticException e) {
            result.setCode(500);
            result.setMessage("已读出错");
            logger.error("已读操作-：" + e.getMessage() + "\r\n" + e.fillInStackTrace());
        }
        return result;
    }

    @POST
    @Path("msg-info-del")
    @Produces(MediaType.APPLICATION_JSON)
    @Log(desc = "删除", action = "del", operationDesc = "消息中心-消息-已读消息-删除操作")
    public BaseDTO msgDel(@FormParam("id") Long id) {
        BaseDTO result = new BaseDTO();
        try {
            if (id == null || id < 1) {
                result.setCode(500);
                logger.debug("消息删除操作-获取参数id错误");
                result.setMessage("获取参数错误");
                return result;
            }

            MsgInfo msgInfo = msgInfoManager.findUniqueBy("id", id);
            if (msgInfo == null) {
                result.setCode(500);
                logger.debug("删除操作-没有查询到信息");
                result.setMessage("没有查询到信息");
                return result;
            } else if (msgInfo.getStatus() == 2) {
                result.setCode(500);
                result.setMessage("该消息已经被删除，刷新查看");
                return result;
            }

            msgInfo.setStatus(2);
            msgInfoManager.save(msgInfo);
            result.setCode(200);
        } catch (ArithmeticException e) {
            result.setCode(500);
            result.setMessage("删除出错");
            logger.error("删除操作-：" + e.getMessage() + "\r\n" + e.fillInStackTrace());
        }
        return result;
    }

    // ~ ======================================================================
    @Resource
    public void setMsgInfoManager(MsgInfoManager msgInfoManager) {
        this.msgInfoManager = msgInfoManager;
    }

    @Resource
    public void setCurrentUserHolder(CurrentUserHolder currentUserHolder) {
        this.currentUserHolder = currentUserHolder;
    }
    @Resource
    public void setvMsgInfoManager(VMsgInfoManager vMsgInfoManager) {
        this.vMsgInfoManager = vMsgInfoManager;
    }

    @Value("${msg.enable}")
    public void setEnable(boolean enable) {
        this.enable = enable;
    }
}
