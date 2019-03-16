package com.mossle.cms.web.portal;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import com.mossle.api.party.PartyConnector;
import com.mossle.api.party.PartyDTO;
import com.mossle.api.tenant.TenantHolder;

import com.mossle.cms.persistence.domain.CmsArticle;
import com.mossle.cms.persistence.domain.CmsRange;
import com.mossle.cms.persistence.manager.CmsArticleManager;

import com.mossle.core.auth.CurrentUserHolder;
import com.mossle.core.page.Page;

import com.mossle.core.query.PropertyFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("cms/portal")
public class CmsPortalController {
    private static Logger logger = LoggerFactory
            .getLogger(CmsPortalController.class);
    private CmsArticleManager cmsArticleManager;
    private TenantHolder tenantHolder;
    private CurrentUserHolder currentUserHolder;
    private PartyConnector partyConnector;

    @RequestMapping("articles")
    public String articles(@ModelAttribute Page page,
                           @RequestParam Map<String, Object> parameterMap) throws Exception {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String now = formatter.format(new Date());
        String userId = currentUserHolder.getUserId();
        PartyDTO partyDTO = partyConnector.findDepartmentById(userId);
        StringBuilder buff = new StringBuilder();
        buff.append("<table class='table'>");
        buff.append("<tbody>");
        //当前登录人直接组织id
        String partyEntityId = partyDTO == null ? "" : partyDTO.getId();
        List<PropertyFilter> propertyFilters = PropertyFilter.buildFromMap(parameterMap);
        propertyFilters.add(new PropertyFilter("EQI_status", "1"));//1：发布状态的公告
        propertyFilters.add(new PropertyFilter("LED_startTime", now));//有效开始日期
        propertyFilters.add(new PropertyFilter("GED_endTime", now));//有效结束日期
        page = cmsArticleManager.pagedQueryByCms(page, partyEntityId, propertyFilters);
        List<CmsArticle> cmsArticles = (List<CmsArticle>) page.getResult();
        for (CmsArticle cmsArticle : cmsArticles) {
            if (cmsArticle.getPublishTime() != null) {
                buff.append("<tr>");
                /*buff.append("  <td>" + cmsArticle.getCmsCatalog().getName()
                        + "</td>");*/

                String strTitle = cmsArticle.getTitle();
                if (strTitle == null) strTitle = "";
                else if (strTitle.length() > 15) {
                    strTitle = strTitle.substring(0, 15) + "...";
                }

                buff.append("  <td><a title='" + cmsArticle.getTitle() + "' href='../cms/cms-article-meview.do?id="
                        + cmsArticle.getId() + "'>" + strTitle
                        + "</a></td>");
                buff.append("  <td>"
                        + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cmsArticle
                        .getPublishTime()) + "</td>");
                buff.append("</tr>");
            }
        }

        buff.append("</tbody>");
        buff.append("</table>");

        return buff.toString();
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
    public void setPartyConnector(PartyConnector partyConnector) {
        this.partyConnector = partyConnector;
    }

    @Resource
    public void setTenantHolder(TenantHolder tenantHolder) {
        this.tenantHolder = tenantHolder;
    }
}
