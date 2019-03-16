package com.mossle.humantask.web.portal;

import com.mossle.api.humantask.HumanTaskConnector;
import com.mossle.api.tenant.TenantHolder;
import com.mossle.common.utils.DeEnCode;
import com.mossle.core.auth.CurrentUserHolder;
import com.mossle.core.page.Page;
import com.mossle.core.query.PropertyFilter;
import com.mossle.humantask.persistence.domain.UnfinishProcessInstance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("humantask/portal")
public class HumantaskPortalController {
    private static Logger logger = LoggerFactory
            .getLogger(HumantaskPortalController.class);
    private HumanTaskConnector humanTaskConnector;
    private CurrentUserHolder currentUserHolder;
    private TenantHolder tenantHolder;

    @RequestMapping("personalTasks")
    public String personalTasks() {
        String userId = currentUserHolder.getUserId();
        String tenantId = tenantHolder.getTenantId();
        
        
        
        Page potalPage = new Page(1, 10, "create_time", "ASC");
        List<PropertyFilter> propertyFilters = new ArrayList<PropertyFilter>();
        //Page page = findPersonalTasks = humanTaskConnector.findPersonalTasks(userId, tenantId, 1, 10);
        Page page = humanTaskConnector.findPersonalTasksToPortal(userId, tenantId, propertyFilters,
                potalPage);
        /*List<HumanTaskDTO> humanTaskDtos = (List<HumanTaskDTO>) page
                .getResult();*/

        StringBuilder buff = new StringBuilder();
        buff.append("<table class='table table-hover'>");
        buff.append("  <thead>");
        buff.append("    <tr>");
        buff.append("      <th>主题</th>");
        buff.append("      <th>申请人</th>");
        buff.append("      <th width='20%'>&nbsp;</th>");
        buff.append("    </tr>");
        buff.append("  </thead>");
        buff.append("  <tbody>");

        // DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        for (UnfinishProcessInstance vo : (List<UnfinishProcessInstance>) page.getResult()) {
        	
        	String processInstanceId = DeEnCode.encode(vo.getProcessInstanceId());
        	String id = DeEnCode.encode(vo.getId());
        	
            String strTitle = vo.getTheme();
            if (strTitle == null) strTitle = "";
            else if (strTitle.length() > 15) {
                strTitle = strTitle.substring(0, 15) + "...";
            }
            if("8001".equals(vo.getBusinessDetailId()) && vo.isCompare() == false){
            	buff.append("    <tr style='color:red;'>");
            }else{
            	 buff.append("    <tr>");
            }
            buff.append("      <td title='" + vo.getTheme() + "'>" + strTitle + "</td>");
            buff.append("      <td>" + vo.getApplyUserName() + "</td>");
            buff.append("      <td>");
            buff.append("        <a href='" + ".."
                    + "/operation/task-operation-viewTaskForm.do?humanTaskId="
                    + id + "&processInstanceId=" + processInstanceId + "&catalog=" + vo.getCatalog()
                    //+ "&action=" + vo.getAction()
                    + "' class='btn btn-xs btn-primary'>处理</a>");
            buff.append("      </td>");
            buff.append("    </tr>");
        }
        
        /*for (HumanTaskDTO humanTaskDto : humanTaskDtos) {
            buff.append("    <tr>");
            buff.append("      <td>" + humanTaskDto.getBusinessKey() + "</td>");
            buff.append("      <td>" + humanTaskDto.getPresentationSubject()
                    + "</td>");
            buff.append("      <td>");
            buff.append("        <a href='" + ".."
                    + "/operation/task-operation-viewTaskForm.do?humanTaskId="
                    + humanTaskDto.getId()
                    + "' class='btn btn-xs btn-primary'>处理</a>");
            buff.append("      </td>");
            buff.append("    </tr>");
        }*/

        buff.append("  </tbody>");
        buff.append("</table>");

        return buff.toString();
    }

    @Resource
    public void setHumanTaskConnector(HumanTaskConnector humanTaskConnector) {
        this.humanTaskConnector = humanTaskConnector;
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
