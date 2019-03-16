package com.mossle.humantask.web;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.mossle.api.humantask.HumanTaskConnector;
import com.mossle.api.keyvalue.KeyValueConnector;
import com.mossle.api.keyvalue.Record;
import com.mossle.api.tenant.TenantHolder;
import com.mossle.core.auth.CurrentUserHolder;
import com.mossle.core.export.Exportor;
import com.mossle.core.export.TableModel;
import com.mossle.core.mapper.BeanMapper;
import com.mossle.core.mapper.JsonMapper;
import com.mossle.core.page.Page;
import com.mossle.core.query.PropertyFilter;
import com.mossle.core.spring.MessageHelper;
import com.mossle.humantask.persistence.domain.TaskInfo;
import com.mossle.humantask.persistence.manager.TaskInfoManager;
import com.mossle.operation.persistence.domain.CustomApprover;
import com.mossle.operation.persistence.manager.CustomApproverManager;import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;import org.springframework.web.servlet.mvc.support.RedirectAttributes;
@Controller
@RequestMapping("humantask")
public class TaskInfoController {
    private static Logger logger = LoggerFactory
            .getLogger(TaskInfoController.class);
    private TaskInfoManager taskInfoManager;
    private KeyValueConnector keyValueConnector;
    private Exportor exportor;
    private BeanMapper beanMapper = new BeanMapper();
    private JsonMapper jsonMapper = new JsonMapper();
    private MessageHelper messageHelper;
    private CurrentUserHolder currentUserHolder;
    private TenantHolder tenantHolder;
    private HumanTaskConnector humanTaskConnector;
	@Autowired
    private CustomApproverManager customApproverManager;   @RequestMapping("task-info-list")
    public String list(@ModelAttribute Page page,
            @RequestParam Map<String, Object> parameterMap, Model model) {
    	String userName = currentUserHolder.getName();
    	if(!userName.equals("东威管理员")){
    		return "redirect://portal/index.do";
    	}
    	List<PropertyFilter> propertyFilters = PropertyFilter
                .buildFromMap(parameterMap);
    	if(propertyFilters.size() == 0){
    		return "humantask/task-info-list";
    	}
        page = humanTaskConnector.findUserTasks( propertyFilters, page);
        model.addAttribute("page", page);

        return "humantask/task-info-list";
    }

    @RequestMapping("task-info-input")
    public String input(@RequestParam(value = "id", required = false) Long id,
            Model model,RedirectAttributes redirectAttributes) {
        if (id != null || String.valueOf(id).equals("")) {
            TaskInfo taskInfo = taskInfoManager.get(id);
            if(taskInfo == null){
            	messageHelper.addFlashMessage(redirectAttributes, "core.success.save",
                        "未查询到此流程任务");
            	return "redirect:/humantask/task-info-list.do";
            }
            model.addAttribute("model", taskInfo);
        }else{
        	messageHelper.addFlashMessage(redirectAttributes, "core.success.save",
                    "缺少流程任务id参数");
        	return "redirect:/humantask/task-info-list.do";
        }

        return "humantask/task-info-input";
    }

    @RequestMapping("task-info-save")
    public String save(@ModelAttribute TaskInfo taskInfo,String setType,Long postId,
            @RequestParam Map<String, Object> parameterMap,HttpServletResponse response,
            RedirectAttributes redirectAttributes) {
        String tenantId = tenantHolder.getTenantId();
        String userId = currentUserHolder.getUserId();
        Long taskId = taskInfo.getId();
        String assignee = taskInfo.getAssignee();
        int result = humanTaskConnector.saveTask(taskId,assignee,setType,postId);
        if(result == 1){
        	messageHelper.addFlashMessage(redirectAttributes, "core.success.save",
                    "保存成功");
        }
        
        return "redirect:/humantask/task-info-list.do";
    }

    @RequestMapping("task-info-remove")
    public String remove(@RequestParam("selectedItem") List<Long> selectedItem,
            RedirectAttributes redirectAttributes) {
        List<TaskInfo> taskInfos = taskInfoManager.findByIds(selectedItem);

        taskInfoManager.removeAll(taskInfos);
        messageHelper.addFlashMessage(redirectAttributes,
                "core.success.delete", "删除成功");

        return "redirect:/humantask/task-info-list.do";
    }
    @RequestMapping("task-info-check-person")
    @ResponseBody
    public String checkPerson(String assignee,String businessKey){
    	Record record = keyValueConnector.findByBusinessKey(businessKey);
    	//如果不是自定义，直接返回
    	if(!"9999".equals(record.getBusinessTypeId())){
    		return "pass";
    	}
    	String startUserId = record.getUserId();
    	String hql = "from CustomApprover where opterType not in('2','3') and businessKey=? and approverId="+assignee;
		List customApprovers = customApproverManager.find(hql, businessKey);
		if(startUserId.equals(assignee) || customApprovers.size() != 0){
			return "unpassed";
		}else{
			return "pass";
		}
    }
    @RequestMapping("task-info-export")
    public void export(@ModelAttribute Page page,
            @RequestParam Map<String, Object> parameterMap,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        List<PropertyFilter> propertyFilters = PropertyFilter
                .buildFromMap(parameterMap);
        page = taskInfoManager.pagedQuery(page, propertyFilters);

        List<TaskInfo> dynamicModels = (List<TaskInfo>) page.getResult();

        TableModel tableModel = new TableModel();
        tableModel.setName("dynamic model");
        tableModel.addHeaders("id", "name");
        tableModel.setData(dynamicModels);
        exportor.export(request, response, tableModel);
    }

    // ~ ======================================================================
    @Resource
    public void setTaskInfoManager(TaskInfoManager taskInfoManager) {
        this.taskInfoManager = taskInfoManager;
    }
    @Resource
    public void setKeyValueConnector(KeyValueConnector keyValueConnector) {
    	this.keyValueConnector = keyValueConnector;
    }

    @Resource
    public void setExportor(Exportor exportor) {
        this.exportor = exportor;
    }

    @Resource
    public void setMessageHelper(MessageHelper messageHelper) {
        this.messageHelper = messageHelper;
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
    public void setHumanTaskConnector(HumanTaskConnector humanTaskConnector) {
    	this.humanTaskConnector = humanTaskConnector;
    }
	@Resource
    public void setCustomApproverManager(CustomApproverManager customApproverManager) {
    	this.customApproverManager = customApproverManager;
    }
}
