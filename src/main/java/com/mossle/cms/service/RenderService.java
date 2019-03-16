package com.mossle.cms.service;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.mossle.api.keyvalue.KeyValueConnector;
import com.mossle.api.notification.NotificationConnector;
import com.mossle.api.process.ProcessConnector;
import com.mossle.api.store.StoreConnector;
import com.mossle.api.tenant.TenantHolder;
import com.mossle.api.user.UserConnector;
import com.mossle.base.persistence.manager.BusinessDetailManager;
import com.mossle.cms.persistence.domain.CmsArticle;
import com.mossle.cms.persistence.domain.CmsCatalog;
import com.mossle.cms.persistence.manager.CmsArticleManager;
import com.mossle.common.utils.WebAPI;
import com.mossle.core.auth.CurrentUserHolder;
import com.mossle.core.page.Page;
import com.mossle.core.spring.SignInfo;
import com.mossle.core.template.TemplateService;

import com.mossle.humantask.persistence.manager.TaskInfoManager;
import com.mossle.keyvalue.persistence.manager.RecordManager;
import com.mossle.keyvalue.persistence.manager.TimeTaskManager;
import com.mossle.msg.MsgConstants;
import com.mossle.operation.persistence.manager.CodeManager;
import com.mossle.operation.service.OperationService;
import com.mossle.user.persistence.domain.PersonInfo;
import com.mossle.user.persistence.manager.PersonInfoManager;
import com.mossle.ws.persistence.manager.OnLineInfoManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

@Service
public class RenderService {
    private static Logger logger = LoggerFactory.getLogger(RenderService.class);
    private TemplateService templateService;
    private UserConnector userConnector;
    private String baseDir;
    private JdbcTemplate jdbcTemplate;
    private NotificationConnector notificationConnector;//发送消息
    private PersonInfoManager personInfoManager;
    private CmsArticleManager cmsArticleManager;

    public void render(CmsArticle cmsArticle) {
        this.renderDetail(cmsArticle);
        this.renderIndex(cmsArticle.getCmsCatalog());
    }

    /**
     * 公告在有效期内的发送消息
     **/
    @Transactional(readOnly = false)
    public void PushCmsMsg() {
        try {
            //获取所有未发送消息，并且在已经在有效期内的公告
            String strCmsSql = "SELECT id,title,CONTENT,`STATUS`,PUBLISH_TIME,WEIGHT,USER_ID,CATALOG_ID,TENANT_ID,party_entity_id,\n" +
                    "start_time,end_time FROM cms_article where WEIGHT=1";
            List<Map<String, Object>> mapList = jdbcTemplate.queryForList(strCmsSql);
            System.out.println("需发布的消息的公告个数：" + mapList.size());
            if (mapList != null && mapList.size() > 0) {
                for (Map<String, Object> map : mapList) {
                    Long id = Long.parseLong(map.get("id").toString());
                    CmsArticle cmsArticle = cmsArticleManager.get(id);
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
                        String partyRange = cmsArticle.getPartyEntityId();
                        String[] split_data0 = partyRange.split(",");
                        for (int j = 0; j < split_data0.length; j++) {
                            List<PersonInfo> personInfos = personInfoManager.findBy("departmentCode", split_data0[j]);
                            for (PersonInfo personInfo : personInfos) {
                                String title = "[" + cmsArticle.getTitle() + "]公告提醒";
                                String content = "您有公告[" + cmsArticle.getTitle() + "]，请查看。";
                                String receiver = personInfo.getId().toString();
                                notificationConnector.send(
                                        cmsArticle.getId().toString(),
                                        "1",
                                        cmsArticle.getUserId(),
                                        receiver,
                                        title,
                                        content, MsgConstants.MSG_TYPE_NOTICE);
                            }
                        }
                        cmsArticle.setPublishTime(new Date());
                        cmsArticle.setWeight(0);
                        cmsArticleManager.save(cmsArticle);
                    }



                }

            }
            String strCmsSql2 = "SELECT id,title,CONTENT,`STATUS`,PUBLISH_TIME,WEIGHT,USER_ID,CATALOG_ID,TENANT_ID,party_entity_id,\n" +
                    "start_time,end_time FROM cms_article where WEIGHT=1";
            List<Map<String, Object>> mapList2 = jdbcTemplate.queryForList(strCmsSql);
            System.out.println("剩余不在有效期需发布的消息的公告个数：" + mapList2.size());
        } catch (Exception e) {
            logger.info("公告定时任务异常：" + e.getMessage() + "\r\n" + e.getStackTrace());
        }
    }

    public void renderDetail(CmsArticle cmsArticle) {
        try {
            Map<String, Object> data = new HashMap<String, Object>();
            CmsCatalog cmsCatalog = cmsArticle.getCmsCatalog();
            data.put("article", cmsArticle);
            data.put("catalog", cmsCatalog);

            String html = templateService.render(
                    cmsCatalog.getTemplateDetail(), data);
            String path = baseDir + "/cms/html/" + cmsArticle.getId() + ".html";
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(
                    new FileOutputStream(path), "UTF-8"));
            writer.print(html);
            writer.flush();
            writer.close();
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    public void renderIndex(CmsCatalog cmsCatalog) {
        try {
            Map<String, Object> data = new HashMap<String, Object>();
            data.put("catalog", cmsCatalog);
            data.put("articles", cmsCatalog.getCmsArticles());

            String html = templateService.render(cmsCatalog.getTemplateIndex(),
                    data);
            String path = baseDir + "/cms/html/index.html";
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(
                    new FileOutputStream(path), "UTF-8"));
            writer.print(html);
            writer.flush();
            writer.close();
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    public String view(CmsArticle cmsArticle, List<CmsCatalog> cmsCatalogs,
                       Page page) {
        Assert.notNull(cmsArticle, "cmsArticle must not null");

        Map<String, Object> data = new HashMap<String, Object>();
        CmsCatalog cmsCatalog = cmsArticle.getCmsCatalog();
        data.put("article", cmsArticle);
        data.put("catalog", cmsCatalog);
        data.put("userConnector", userConnector);
        data.put("catalogs", cmsCatalogs);
        data.put("page", page);

        return templateService.render(cmsCatalog.getTemplateDetail(), data);
    }

    // ~ ==================================================
    public String viewIndex(List<CmsCatalog> cmsCatalogs) {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("catalogs", cmsCatalogs);

        String html = templateService.render("/default/index.html", data);

        return html;
    }

    public String viewCatalog(CmsCatalog cmsCatalog, Page page,
                              List<CmsCatalog> cmsCatalogs) {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("catalog", cmsCatalog);
        data.put("userConnector", userConnector);
        data.put("page", page);
        data.put("catalogs", cmsCatalogs);

        String html = templateService
                .render(cmsCatalog.getTemplateList(), data);

        return html;
    }

    public String viewArticle(CmsArticle cmsArticle) {
        Map<String, Object> data = new HashMap<String, Object>();
        CmsCatalog cmsCatalog = cmsArticle.getCmsCatalog();
        data.put("article", cmsArticle);
        data.put("catalog", cmsCatalog);
        data.put("userConnector", userConnector);

        return templateService.render(cmsCatalog.getTemplateDetail(), data);
    }

    public String viewSite(List<CmsCatalog> cmsCatalogs) {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("catalogs", cmsCatalogs);
        data.put("userConnector", userConnector);

        String html = templateService.render("/default/index.html", data);

        return html;
    }

    // ~ ==================================================
    @Resource
    public void setTemplateService(TemplateService templateService) {
        this.templateService = templateService;
    }

    @Resource
    public void setUserConnector(UserConnector userConnector) {
        this.userConnector = userConnector;
    }

    @Value("${store.baseDir}")
    public void setBaseDir(String baseDir) {
    	
    	/*
    	//获取Tomcat服务器所在的路径
        String tomcat_path = System.getProperty( "user.dir" );  
        //获取Tomcat服务器所在路径的最后一个文件目录  
        String bin_path = tomcat_path.substring(tomcat_path.lastIndexOf("/")+1,tomcat_path.length()); 
        
        String all_path = "";
        if(("bin").equals(bin_path)){//手动启动Tomcat时获取路径为：D:\Software\Tomcat-8.5\bin  
            //获取保存上传图片的文件路径  
            all_path=tomcat_path.substring(0,System.getProperty("user.dir").lastIndexOf("/")) + "/webapps" ;       
        }else{//服务中自启动Tomcat时获取路径为：D:\Software\Tomcat-8.5  
            all_path = tomcat_path + "/webapps";   
        }  
        
        this.baseDir = all_path + "/" + baseDir;
        */
        this.baseDir = baseDir;

    }

    @Resource
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Resource
    public void setNotificationConnector(NotificationConnector notificationConnector) {
        this.notificationConnector = notificationConnector;
    }

    @Resource
    public void setPersonInfoManager(PersonInfoManager personInfoManager) {
        this.personInfoManager = personInfoManager;
    }

    @Resource
    public void setCmsArticleManager(CmsArticleManager cmsArticleManager) {
        this.cmsArticleManager = cmsArticleManager;
    }

}
