package com.mossle.version.rs;

import com.mossle.core.annotation.Log;
import com.mossle.core.util.BaseDTO;
import com.mossle.version.persistence.domain.VersionInfo;
import com.mossle.version.persistence.manager.VersionInfoManager;
import com.mossle.worktask.rs.WorkTaskResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;


/**
 * Created by wanghan on 2017\10\10 0010.
 */
@Component
@Path("version")
public class VersionResource {
    private static Logger logger = LoggerFactory
            .getLogger(WorkTaskResource.class);
    private VersionInfoManager versionInfoManager;

    @POST
    @Path("version-info-del")
    @Produces(MediaType.APPLICATION_JSON)
    @Log(desc = "删除", action = "del", operationDesc = "系统配置-版本管理-删除操作")
    public BaseDTO fnVersionDel(@FormParam("id") Long id) {
        BaseDTO result = new BaseDTO();
        try {
            if (id == null || id < 1) {
                result.setCode(500);
                logger.debug("删除操作-获取参数id错误");
                result.setMessage("获取参数错误");
                return result;
            }
            VersionInfo versionInfo = versionInfoManager.findUniqueBy("id", id);

            if (versionInfo == null) {
                result.setCode(500);
                logger.debug("删除操作-没有查询到版本信息");
                result.setMessage("没有查询到版本信息");
                return result;
            }
            versionInfoManager.remove(versionInfo);
            result.setCode(200);
        } catch (ArithmeticException e) {
            result.setCode(500);
            result.setMessage("删除出错");
            logger.error("删除操作-：" + e.getMessage() + "\r\n" + e.fillInStackTrace());
        }
        return result;
    }

    @Resource
    public void setVersionInfoManager(VersionInfoManager versionInfoManager) {
        this.versionInfoManager = versionInfoManager;
    }
}
