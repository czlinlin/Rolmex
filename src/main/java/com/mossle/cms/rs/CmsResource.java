package com.mossle.cms.rs;

import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import com.mossle.api.notification.NotificationConnector;
import com.mossle.api.store.StoreConnector;
import com.mossle.api.store.StoreDTO;
import com.mossle.api.tenant.TenantHolder;

import com.mossle.cms.persistence.domain.CmsArticle;
import com.mossle.cms.persistence.domain.CmsRange;
import com.mossle.cms.persistence.manager.CmsArticleManager;
import com.mossle.cms.persistence.manager.CmsRangeManager;
import com.mossle.core.annotation.Log;
import com.mossle.core.auth.CurrentUserHolder;
import com.mossle.core.util.BaseDTO;
import com.mossle.core.util.ServletUtils;

import com.mossle.msg.MsgConstants;
import com.mossle.user.persistence.domain.PersonInfo;
import com.mossle.user.persistence.manager.PersonInfoManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;


@Component
@Path("cms")
public class CmsResource {
    private static Logger logger = LoggerFactory.getLogger(CmsResource.class);
    private StoreConnector storeConnector;
    private TenantHolder tenantHolder;
    private CmsArticleManager cmsArticleManager;
    private CmsRangeManager cmsRangeManager;
    private PersonInfoManager personInfoManager;
    private NotificationConnector notificationConnector;//发送消息
    private CurrentUserHolder currentUserHolder;


    @POST
    @Path("cms-article-del")
    @Produces(MediaType.APPLICATION_JSON)
    @Log(desc = "删除", action = "del", operationDesc = "内容管理-公告管理-草稿公告-删除")
    public BaseDTO cmsDel(@FormParam("id") Long id) {
        BaseDTO result = new BaseDTO();
        try {
            if (id == null || id < 1) {
                result.setCode(500);
                logger.debug("公告删除操作-获取参数id错误");
                result.setMessage("获取参数错误");
                return result;
            }
            CmsArticle cmsArticle = cmsArticleManager.get(id);
            if (cmsArticle == null) {
                result.setCode(500);
                logger.debug("公告删除操作-没有查询到公告信息");
                result.setMessage("没有查询到公告信息");
                return result;
            }
            cmsArticleManager.remove(cmsArticle);
            result.setCode(200);
        } catch (ArithmeticException e) {
            result.setCode(500);
            result.setMessage("删除出错");
            logger.error("公告删除操作-：" + e.getMessage() + "\r\n" + e.fillInStackTrace());
        }
        return result;
    }


    @POST
    @Path("cms-article-deltele")
    @Produces(MediaType.APPLICATION_JSON)
    @Log(desc = "删除", action = "del", operationDesc = "内容管理-公告管理-已发公告-删除")
    public BaseDTO cmsDeltele(@FormParam("id") Long id) {
        BaseDTO result = new BaseDTO();
        try {
            if (id == null || id < 1) {
                result.setCode(500);
                logger.debug("公告删除操作-获取参数id错误");
                result.setMessage("获取参数错误");
                return result;
            }
            CmsArticle cmsArticle = cmsArticleManager.get(id);
            if (cmsArticle == null) {
                result.setCode(500);
                logger.debug("公告删除操作-没有查询到公告信息");
                result.setMessage("没有查询到公告信息");
                return result;
            }
            //status=2删除状态
            cmsArticle.setStatus(2);
            cmsArticleManager.save(cmsArticle);
            result.setCode(200);
        } catch (ArithmeticException e) {
            result.setCode(500);
            result.setMessage("删除出错");
            logger.error("公告删除操作-：" + e.getMessage() + "\r\n" + e.fillInStackTrace());
        }
        return result;
    }

    @POST
    @Path("cms-article-publish")
    @Produces(MediaType.APPLICATION_JSON)
    @Log(desc = "发布", action = "发布", operationDesc = "内容管理-公告管理-草稿公告-发布")
    public BaseDTO cmsPublish(@FormParam("id") Long id) {
        BaseDTO result = new BaseDTO();
        String tenantId = tenantHolder.getTenantId();
        try {
            if (id == null || id < 1) {
                result.setCode(500);
                logger.debug("公告发布操作-获取参数id错误");
                result.setMessage("获取参数错误");
                return result;
            }
            CmsArticle cmsArticle = cmsArticleManager.get(id);
            cmsArticle.setPublishTime(new Date());
            cmsArticle.setStatus(1);
            cmsArticleManager.save(cmsArticle);
            result.setCode(200);
  /*        4)公告发布后，须向可查看公告的用户添加提醒消息，消息格式如下：
        a)标题：[公告标题]公告提醒；
        内容：您有新公告[公告标题]，请查看。；*/
            //发送消息
/*
            SimpleDateFormat formatterNew = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date start = cmsArticle.getStartTime();
            Date now = null;
            try {
                now = formatterNew.parse(formatterNew.format(new Date()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            boolean check = false;
            if (start.before(now)) {
                check = true;
            }
            if (cmsArticle.getStatus() == 1 && check == true) {
                List<CmsRange> cmsRanges = cmsRangeManager.findBy("cmsArticle.id", id);
                for (CmsRange cmsRange : cmsRanges) {
                    String partyId = cmsRange.getPartyId();
                    List<PersonInfo> personInfos = personInfoManager.findBy("departmentCode", partyId);
                    for (PersonInfo personInfo : personInfos) {
                        String title = "[" + cmsArticle.getTitle() + "]公告提醒";
                        String content = "您有公告[" + cmsArticle.getTitle() + "]，请查看。";
                        String receiver = personInfo.getId().toString();
                        notificationConnector.send(
                                cmsArticle.getId().toString(),
                                tenantId,
                                currentUserHolder.getUserId().toString(),
                                receiver,
                                title,
                                content, MsgConstants.MSG_TYPE_NOTICE);
                    }
                }
            }*/
        } catch (
                ArithmeticException e) {
            result.setCode(500);
            result.setMessage("发布出错");
            logger.error("公告发布操作-：" + e.getMessage() + "\r\n" + e.fillInStackTrace());
        }
        return result;
    }


    @GET
    @Path("image")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public InputStream image(@QueryParam("key") String key) throws Exception {
        String tenantId = tenantHolder.getTenantId();
        StoreDTO storeDto = storeConnector.getStore("cms/html/r/image", key,
                tenantId);

        return storeDto.getDataSource().getInputStream();
    }

    @GET
    @Path("video")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public InputStream video(@QueryParam("key") String key) throws Exception {
        String tenantId = tenantHolder.getTenantId();
        StoreDTO storeDto = storeConnector.getStore("cms/html/r/video", key,
                tenantId);

        return storeDto.getDataSource().getInputStream();
    }

    @GET
    @Path("audio")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public InputStream audio(@QueryParam("key") String key) throws Exception {
        String tenantId = tenantHolder.getTenantId();
        StoreDTO storeDto = storeConnector.getStore("cms/html/r/audio", key,
                tenantId);

        return storeDto.getDataSource().getInputStream();
    }

    @GET
    @Path("pdf")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public InputStream pdf(@QueryParam("key") String key) throws Exception {
        String tenantId = tenantHolder.getTenantId();
        StoreDTO storeDto = storeConnector.getStore("cms/html/r/pdf", key,
                tenantId);

        return storeDto.getDataSource().getInputStream();
    }

    @GET
    @Path("attachment")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public InputStream zip(@QueryParam("key") String key) throws Exception {
        String tenantId = tenantHolder.getTenantId();
        StoreDTO storeDto = storeConnector.getStore("cms/html/r/attachment",
                key, tenantId);

        return storeDto.getDataSource().getInputStream();
    }

    @GET
    @Path("attachments")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public InputStream attachments(@QueryParam("key") String key,
                                   HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        String tenantId = tenantHolder.getTenantId();
        StoreDTO storeDto = storeConnector.getStore("cms/html/r/attachments",
                key, tenantId);
        ServletUtils.setFileDownloadHeader(request, response,
                storeDto.getDisplayName());

        return storeDto.getDataSource().getInputStream();
    }

    @Resource
    public void setStoreConnector(StoreConnector storeConnector) {
        this.storeConnector = storeConnector;
    }

    @Resource
    public void setCmsRangeManager(CmsRangeManager cmsRangeManager) {
        this.cmsRangeManager = cmsRangeManager;
    }

    @Resource
    public void setPersonInfoManager(PersonInfoManager personInfoManager) {
        this.personInfoManager = personInfoManager;
    }

    @Resource
    public void setNotificationConnector(NotificationConnector notificationConnector) {
        this.notificationConnector = notificationConnector;
    }

    @Resource
    public void setCmsArticleManager(CmsArticleManager cmsArticleManager) {
        this.cmsArticleManager = cmsArticleManager;
    }

    @Resource
    public void setCurrentUserHolder(CurrentUserHolder currentUserHolder) {
        this.currentUserHolder = currentUserHolder;
    }

    @Resource
    public void setTenantHolder(TenantHolder tenantHolder) {
        this.tenantHolder = tenantHolder;
    }
}
