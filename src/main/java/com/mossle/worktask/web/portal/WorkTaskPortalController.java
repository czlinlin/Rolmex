package com.mossle.worktask.web.portal;

import com.mossle.api.tenant.TenantHolder;
import com.mossle.core.auth.CurrentUserHolder;
import com.mossle.core.page.Page;
import com.mossle.core.query.PropertyFilter;
import com.mossle.pim.persistence.domain.PimTask;
import com.mossle.pim.web.portal.TaskPortalController;
import com.mossle.worktask.persistence.domain.WorkTaskInfo;
import com.mossle.worktask.persistence.manager.WorkTaskInfoManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

/**
 * Created by wanghan on 2017\11\6 0006.
 */
@RestController
@RequestMapping("worktask/portal")
public class WorkTaskPortalController {
    private static Logger logger = LoggerFactory
            .getLogger(TaskPortalController.class);
    private CurrentUserHolder currentUserHolder;
    private TenantHolder tenantHolder;
    private WorkTaskInfoManager workTaskInfoManager;

    @RequestMapping("tasks")
    public String personalTasks(@ModelAttribute Page page,
                                @RequestParam Map<String, Object> parameterMap) {

        List<PropertyFilter> propertyFilters = PropertyFilter.buildFromMap(parameterMap);
        String userId = currentUserHolder.getUserId();
        propertyFilters.add(new PropertyFilter("EQL_leader", userId));
        propertyFilters.add(new PropertyFilter("EQS_datastatus", "1"));
        propertyFilters.add(new PropertyFilter("INS_status", "0,1"));
        page.setDefaultOrder("plantime", page.ASC);
        page = workTaskInfoManager.pagedQuery(page, propertyFilters);


        List<WorkTaskInfo> workTaskInfos = (List<WorkTaskInfo>) page.getResult();

        StringBuilder buff = new StringBuilder();
        buff.append("<table class='table table-hover'>");
        buff.append("  <thead>");
        buff.append("    <tr>");
        buff.append("      <th>任务标题</th>");
        buff.append("    </tr>");
        buff.append("  </thead>");
        buff.append("  <tbody>");

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        for (WorkTaskInfo workTaskInfo : workTaskInfos) {
            String strTitle=workTaskInfo.getTitle();
            if(strTitle==null) strTitle="";
            else if (strTitle.length()>30){
                strTitle=strTitle.substring(0,30)+"...";
            }
            buff.append("    <tr>");
            buff.append("  <td><a title='"+workTaskInfo.getTitle()+"' href='../worktask/work-task-info-detail.do?id="
                    + workTaskInfo.getId() + "'>" + strTitle
                    + "</a></td>");
            buff.append("    </tr>");
        }
        buff.append("  </tbody>");
        buff.append("</table>");

        return buff.toString();
    }

    @Resource
    public void setWorkTaskInfoManager(WorkTaskInfoManager workTaskInfoManager) {
        this.workTaskInfoManager = workTaskInfoManager;
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
