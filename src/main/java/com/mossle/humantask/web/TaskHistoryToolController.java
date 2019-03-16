package com.mossle.humantask.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.alibaba.fastjson.JSONObject;
import com.mossle.api.dict.DictConnector;
import com.mossle.api.humantask.HumanTaskConnector;
import com.mossle.api.org.OrgConnector;
import com.mossle.api.tenant.TenantHolder;
import com.mossle.base.persistence.domain.BusinessTypeEntity;
import com.mossle.base.persistence.manager.BusinessTypeManager;
import com.mossle.bpm.web.WorkspaceController;
import com.mossle.core.auth.CurrentUserHolder;
import com.mossle.core.page.Page;
import com.mossle.core.query.PropertyFilter;
import com.mossle.core.spring.MessageHelper;
import com.mossle.dict.persistence.domain.DictInfo;
import com.mossle.dict.support.DictConnectorImpl;
import com.mossle.humantask.persistence.domain.TaskInfo;
import com.mossle.humantask.persistence.manager.TaskInfoManager;
import com.mossle.keyvalue.persistence.domain.RecordInfo;
import com.mossle.keyvalue.persistence.manager.RecordManager;
import com.mossle.party.persistence.domain.PartyEntity;
import com.mossle.party.persistence.manager.PartyEntityManager;
import com.mossle.util.StringUtil;

/*
 * 历史小工具
 * ckx
 */

@Controller
@RequestMapping("humantask")
public class TaskHistoryToolController {
	private static Logger logger = LoggerFactory.getLogger(WorkspaceController.class);
	
	@Autowired
	private CurrentUserHolder currentUserHolder;
	@Autowired
	private TenantHolder tenantHolder;
	@Autowired
	private BusinessTypeManager businessTypeManager;
	@Autowired
	private HumanTaskConnector humanTaskConnector;
	@Autowired
	private DictConnectorImpl dictConnectorImpl;
	@Autowired
	private OrgConnector orgConnector;
	@Autowired
	private PartyEntityManager partyEntityManager;
	@Autowired
	private DictConnector dictConnector;
	@Autowired
	private TaskInfoManager taskInfoManager;
	@Autowired
	private MessageHelper messageHelper;
	@Autowired
	private RecordManager recordManager;
	@Autowired
	private JdbcTemplate jdbcTemplate;
	private Map<String, String> aliasMap = new HashMap<String, String>();
	
	
	@RequestMapping("checkTaskInfo-start")
	@ResponseBody
	public String checkTaskInfoStart(@RequestParam(value = "taskIds", required = true) String taskIds){
		boolean isUnfinishedStart = true;
		String[] split = taskIds.split(",");
		for (String taskId : split) {
			TaskInfo taskInfo = taskInfoManager.findUniqueBy("id", Long.parseLong(taskId));
			RecordInfo recordInfo = recordManager.findUniqueBy("businessKey", taskInfo.getBusinessKey());
			String auditStatus = recordInfo.getAuditStatus();
			String catalog = taskInfo.getCatalog();
			if(!"2".equals(auditStatus) && !"3".equals(auditStatus) && !"6".equals(auditStatus) && "start".equals(catalog)){
				isUnfinishedStart = false;
				break;
			}
		}
		return JSONObject.toJSONString(isUnfinishedStart);
	}
	
	
	/**
	 * 查询显示环节日志列表
	 * @param page
	 * @param parameterMap
	 * @param model
	 * @return
	 * ckx
	 */
	@RequestMapping("humantask-history-tool-log-list-i")
	public String humantaskHistoryToolLogSub(@ModelAttribute Page page,
            @RequestParam Map<String, Object> parameterMap, Model model){
			String searchUserId = StringUtil.toString(parameterMap.get("user_id"));
		 	String userId = currentUserHolder.getUserId();
	        String tenantId = tenantHolder.getTenantId();
	        // page = humanTaskConnector.findPersonalTasks(userId, tenantId, page.getPageNo(), page.getPageSize());
	        model = GetModelCmd(userId, model);
	        Map<String, Object> map = this.convertAlias(parameterMap);
	        List<PropertyFilter> propertyFilters = PropertyFilter.buildFromMap(map);
	        String isSearch = StringUtil.toString(map.get("isSearch"));
	        if("1".equals(isSearch)){
	        	page = humanTaskConnector.findHistoryToolLog(searchUserId,userId, tenantId, propertyFilters, page);
	        }
	        model.addAttribute("page", page);
	        model.addAttribute("statuslist", dictConnector.findDictInfoListByType("RecordStatus"));
	        model.addAttribute("searchUserId", searchUserId);
	        model.addAttribute("isSearch", isSearch);
        return "humantask/workspace-historyToolTasks-log-list-i";
	}
	
	
	@RequestMapping("humantask-history-tool-log-list")
	public String humantaskHistoryToolLog(){
		return "humantask/workspace-historyToolTasks-log-list";
	}
	
	
	/**
	 * 保存流程环节修改
	 * @param taskIds
	 * @param postId
	 * @param setType
	 * @param assignee
	 * @param model
	 * @param redirectAttributes
	 * @return
	 */
	@RequestMapping("history-task-info-save")
	public String historyTaskInfoSave(@RequestParam(value = "taskIds", required = true) String taskIds,
			@RequestParam(value = "postId", required = true) Long postId,
			@RequestParam(value = "setType", required = true) String setType,
			@RequestParam(value = "assignee", required = true) String assignee,
            Model model,RedirectAttributes redirectAttributes){
		
		String[] split = taskIds.split(",");
		for (String taskId : split) {
			humanTaskConnector.saveTask(Long.parseLong(taskId), assignee, setType, postId);
		}
		messageHelper.addFlashMessage(redirectAttributes, "core.success.save","保存成功");
		return "redirect:/humantask/humantask-history-tool-list-i.do";
	}
	
	/**
	 * 跳转到编辑页面
	 * @param taskIds
	 * @param model
	 * @param redirectAttributes
	 * @return
	 */
	@RequestMapping("history-task-info-input")
	public String historyTaskInfoInput(@RequestParam(value = "taskIds", required = true) String taskIds,
			@RequestParam(value="isCustom",required=true) String isCustom,
            Model model,RedirectAttributes redirectAttributes){
		if("1".equals(isCustom)){
			long taskId = Long.parseLong(taskIds);
			TaskInfo findUniqueBy = taskInfoManager.findUniqueBy("id", taskId);
			String businessKey = findUniqueBy.getBusinessKey();
			model.addAttribute("businessKey", businessKey);
		}
		model.addAttribute("isCustom", isCustom);
		model.addAttribute("taskIds", taskIds);
		StringBuffer applyCodes = new StringBuffer();
		String codes = "";
		String sql = "select DISTINCT k.applyCode from task_info t LEFT JOIN kv_record k on t.BUSINESS_KEY = k.BUSINESS_KEY where t.ID in ("+taskIds+");";
		List<Map<String, Object>> applyCodeList = jdbcTemplate.queryForList(sql);
		for (Map<String, Object> map : applyCodeList) {
			applyCodes.append(map.get("applyCode")+",");
		}
		if(StringUtils.isNotBlank(applyCodes)){
			codes =  applyCodes.substring(0, applyCodes.length()-1);
		}
		
		model.addAttribute("applyCodes", codes);
		return "humantask/history-task-info-input";
	}
	
	
	/**
	 * 查询显示环节列表
	 * @param page
	 * @param parameterMap
	 * @param model
	 * @return
	 * ckx
	 */
	@RequestMapping("humantask-history-tool-list-i")
	public String humantaskHistoryToolSub(@ModelAttribute Page page,
            @RequestParam Map<String, Object> parameterMap, Model model){
			String searchUserId = StringUtil.toString(parameterMap.get("user_id"));
		 	String userId = currentUserHolder.getUserId();
	        String tenantId = tenantHolder.getTenantId();
	        // page = humanTaskConnector.findPersonalTasks(userId, tenantId, page.getPageNo(), page.getPageSize());
	        model = GetModelCmd(userId, model);
	        Map<String, Object> map = this.convertAlias(parameterMap);
	        List<PropertyFilter> propertyFilters = PropertyFilter.buildFromMap(map);
	        String isSearch = StringUtil.toString(map.get("isSearch"));
	        if("1".equals(isSearch)){
	        	page = humanTaskConnector.findHistoryTool(searchUserId,userId, tenantId, propertyFilters, page);
	        }
	        model.addAttribute("page", page);
	        model.addAttribute("statuslist", dictConnector.findDictInfoListByType("RecordStatus"));
	        model.addAttribute("searchUserId", searchUserId);
	        model.addAttribute("isSearch", isSearch);
        return "humantask/workspace-historyToolTasks-list-i";
	}
	
	@RequestMapping("humantask-history-tool-list")
	public String humantaskHistoryTool(){
		return "humantask/workspace-historyToolTasks-list";
	}
	
	
    /**
     * 得到一级业务类型
     **/
    private Model GetModelCmd(String userId, Model model) {
        //一级业务类型
        //  PartyDTO partyDTO = partyConnector.findDepartmentById(userId);
        String tenantid = tenantHolder.getTenantId();
        //String hlSql="from BusinessTypeEntity where departmentCode like '%"+partyDTO.getId()+"%' and tenantId="+tenantid+" and enable='是'";
        String hlSql = "from BusinessTypeEntity where tenantId=" + tenantid + " and enable='是'";
        List<BusinessTypeEntity> entityList = businessTypeManager.find(hlSql);
        model.addAttribute("typelist", entityList);
        //得到体系
        List<DictInfo> dictList = dictConnectorImpl.findDictInfoListByType("OwnedSystem", tenantid);
        model.addAttribute("systemlist", dictList);
        //根据人大区（无绑定则，返回所有大区）
        List<PartyEntity> arealist = new ArrayList<PartyEntity>();
        PartyEntity party = orgConnector.findPartyAreaByUserId(userId);
        if (party != null)
            arealist.add(party);
        else {
            String hsql = " from PartyEntity where partyType.id=?";
            arealist = partyEntityManager.findBy("partyType.id", 6L);
        }
        model.addAttribute("arealist", arealist);
        return model;
    }
    protected Map<String, Object> convertAlias(Map<String, Object> parameters) {
        logger.debug("parameters : {}", parameters);
        Map<String, Object> parameterMap = new HashMap<String, Object>();
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            String key = entry.getKey();
            for (Map.Entry<String, String> aliasEntry : aliasMap.entrySet()) {
                String aliasKey = "_" + aliasEntry.getKey();
                String aliasValue = "_" + aliasEntry.getValue();
                if (key.indexOf(aliasKey) != -1) {
                    key = key.replace(aliasKey, aliasValue);
                    break;
                }
            }
            parameterMap.put(key, entry.getValue());
        }
        logger.debug("parameterMap : {}", parameterMap);
        return parameterMap;
    }
}
