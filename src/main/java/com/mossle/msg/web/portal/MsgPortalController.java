package com.mossle.msg.web.portal;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.annotation.Resource;

import com.mossle.api.humantask.HumanTaskConnector;
import com.mossle.api.humantask.HumanTaskDTO;
import com.mossle.api.tenant.TenantHolder;
import com.mossle.api.user.UserConnector;
import com.mossle.api.user.UserDTO;
import com.mossle.core.auth.CurrentUserHolder;
import com.mossle.core.auth.CurrentUserHolder;
import com.mossle.core.page.Page;
import com.mossle.msg.persistence.domain.MsgInfo;
import com.mossle.msg.persistence.domain.VMsgInfo;
import com.mossle.msg.persistence.manager.MsgInfoManager;

import com.mossle.msg.persistence.manager.VMsgInfoManager;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("msg/portal")
public class MsgPortalController {
    private static Logger logger = LoggerFactory
            .getLogger(MsgPortalController.class);
    private MsgInfoManager msgInfoManager;
    private TenantHolder tenantHolder;
    private CurrentUserHolder currentUserHolder;
    private UserConnector userConnector;
    private VMsgInfoManager vMsgInfoManager;

    @RequestMapping("msg")
    public String msg() throws Exception {
        String userId = currentUserHolder.getUserId();
        String hql = "from VMsgInfo where receiverId=? and status=0 order by createTime desc";
        List<VMsgInfo> msgInfos = (List<VMsgInfo>) vMsgInfoManager.pagedQuery(hql,
                1, 10, userId).getResult();

        StringBuilder buff = new StringBuilder();
        buff.append("<table class='table table-hover'>");
        buff.append("  <tbody>");

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        for (VMsgInfo msgInfo : msgInfos) {
            buff.append("    <tr>");
            buff.append("      <td title='" + msgInfo.getName() + "'>");
            buff.append("        <a href='" + ".."
                    + "/msg/msg-info-view.do?id=" + msgInfo.getId() + "'>"
                    + this.substr(msgInfo.getName()) + "</a>");
            buff.append("      </td>");
            buff.append("      <td>"
                    + dateFormat.format(msgInfo.getCreateTime()) + "</td>");
            buff.append("    </tr>");
        }

        buff.append("  </tbody>");
        buff.append("</table>");

        return buff.toString();
    }

    public String findDisplayName(String userId) {
        if (StringUtils.isBlank(userId)) {
            return "";
        }

        UserDTO userDTO = userConnector.findById(userId.trim());

        if (userDTO != null) {
            return userDTO.getDisplayName();
        }
        return "";
    }

    public String substr(String text) {
        if (StringUtils.isBlank(text)) {
            return "";
        }

        if (text.trim().length() < 15) {
            return text.trim();
        }

        return text.trim().substring(0, 15) + "...";
    }

    @Resource
    public void setMsgInfoManager(MsgInfoManager msgInfoManager) {
        this.msgInfoManager = msgInfoManager;
    }

    @Resource
    public void setTenantHolder(TenantHolder tenantHolder) {
        this.tenantHolder = tenantHolder;
    }

    @Resource
    public void setCurrentUserHolder(CurrentUserHolder currentUserHolder) {
        this.currentUserHolder = currentUserHolder;
    }
    @Resource
    public void setvMsgInfoManager(VMsgInfoManager vMsgInfoManager) {
        this.vMsgInfoManager = vMsgInfoManager;
    }
    @Resource
    public void setUserConnector(UserConnector userConnector) {
        this.userConnector = userConnector;
    }
}
