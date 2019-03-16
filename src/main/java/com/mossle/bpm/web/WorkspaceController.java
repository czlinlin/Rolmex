package com.mossle.bpm.web;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.mossle.api.dict.DictConnector;
import com.mossle.api.form.FormDTO;
import com.mossle.api.humantask.HumanTaskConnector;
import com.mossle.api.humantask.HumanTaskConstants;
import com.mossle.api.humantask.HumanTaskDTO;
import com.mossle.api.keyvalue.KeyValueConnector;
import com.mossle.api.keyvalue.Record;
import com.mossle.api.notification.NotificationConnector;
import com.mossle.api.notification.NotificationDTO;
import com.mossle.api.operation.OperationConnector;
import com.mossle.api.org.OrgConnector;
import com.mossle.api.party.PartyConnector;
import com.mossle.api.party.PartyDTO;
import com.mossle.api.process.ProcessConnector;
import com.mossle.api.tenant.TenantHolder;
import com.mossle.api.user.UserConnector;
import com.mossle.base.persistence.domain.BusinessDetailEntity;
import com.mossle.base.persistence.domain.BusinessTypeEntity;
import com.mossle.base.persistence.manager.BusinessDetailManager;
import com.mossle.base.persistence.manager.BusinessTypeManager;
import com.mossle.bpm.cmd.CounterSignCmd;
import com.mossle.bpm.cmd.FindHistoryGraphCmd;
import com.mossle.bpm.cmd.FindNextActivitiesCmd;
import com.mossle.bpm.cmd.HistoryProcessInstanceDiagramCmd;
import com.mossle.bpm.cmd.ProcessDefinitionDiagramCmd;
import com.mossle.bpm.cmd.RollbackTaskCmd;
import com.mossle.bpm.graph.Graph;
import com.mossle.bpm.persistence.domain.BpmCategory;
import com.mossle.bpm.persistence.domain.BpmProcess;
import com.mossle.bpm.persistence.manager.BpmCategoryManager;
import com.mossle.bpm.persistence.manager.BpmProcessManager;
import com.mossle.bpm.service.TraceService;
import com.mossle.bpm.service.WorkSpaceService;
import com.mossle.common.utils.StringUtils;
import com.mossle.core.annotation.Log;
import com.mossle.core.auth.CurrentUserHolder;
import com.mossle.core.export.ExcelExport;
import com.mossle.core.mapper.JsonMapper;
import com.mossle.core.page.Page;
import com.mossle.core.query.PropertyFilter;
import com.mossle.humantask.persistence.domain.TaskInfo;
import com.mossle.humantask.persistence.domain.UnfinishProcessInstance;
import com.mossle.humantask.persistence.manager.TaskInfoManager;
import com.mossle.humantask.support.HumanTaskConnectorImpl;
import com.mossle.humantask.web.TaskWorkspaceController;
import com.mossle.msg.MsgConstants;
import com.mossle.operation.persistence.domain.CustomEntity;
import com.mossle.operation.persistence.manager.CustomManager;
import com.mossle.operation.service.OperationService;
import com.mossle.operation.web.TaskOperationController;
import com.mossle.party.persistence.domain.PartyEntity;
import com.mossle.spi.process.InternalProcessConnector;
import com.mossle.util.ExportUtil;

import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.TaskService;
import org.activiti.engine.form.StartFormData;
import org.activiti.engine.form.TaskFormData;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.pvm.PvmActivity;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mossle.core.spring.MessageHelper;

/**
 * 我的流程 待办流程 已办未结
 */
@Controller
@RequestMapping("bpm")
public class WorkspaceController {
    private static Logger logger = LoggerFactory
            .getLogger(WorkspaceController.class);
    private BpmCategoryManager bpmCategoryManager;
    private BpmProcessManager bpmProcessManager;
    private ProcessEngine processEngine;
    private UserConnector userConnector;
    private ProcessConnector processConnector;
    private CurrentUserHolder currentUserHolder;
    private TraceService traceService;
    private TenantHolder tenantHolder;
    private KeyValueConnector keyValueConnector;
    private JsonMapper jsonMapper = new JsonMapper();
    private HumanTaskConnector humanTaskConnector;
    private NotificationConnector notificationConnector;
    private InternalProcessConnector internalProcessConnector;
    private String baseUrl;
    private BusinessTypeManager businessTypeManager;
    private OrgConnector orgConnector;
    private PartyConnector partyConnector;
    private JdbcTemplate jdbcTemplate;
    private DictConnector dictConnector;
    private MessageHelper messageHelper;
    SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
    private Map<String, String> aliasMap = new HashMap<String, String>();
    @Resource
    private TaskInfoManager taskInfoManager;
    @Resource
    private HumanTaskConnectorImpl humanTaskConnectorImpl;
    @Resource
    private CustomManager customManager;
    @Resource
    private BusinessDetailManager businessDetailManager;
    @Resource
    private WorkSpaceService workSpaceService;
    @Autowired
    private OperationConnector operationConnector;
    @Autowired
    private OperationService operationService;
    
    @RequestMapping("workspace-home")
    public String home(Model model) {

        String userId = currentUserHolder.getUserId();
        String ids = "";
        List<BpmCategory> bpmCategories = new ArrayList<BpmCategory>();

        // 查询用户所属岗位
        List<PartyEntity> postList = orgConnector.getPostByUserId(userId);

        if (postList == null || postList.size() == 0) {
            /*String hql = "from BpmCategory where id=? order by priority";
            bpmCategories = bpmCategoryManager.find(hql, Long.parseLong("3"));
            if (bpmCategories != null && bpmCategories.size() >0) {
            	ids = "3";
            }*/
            BpmCategory bpm=getCommonBpmCategory();
            bpmCategories.add(bpm);
            if (bpmCategories != null && bpmCategories.size() >0) {
            	ids = "3";
            }
        } else {
            String postIds = "";
            String categoryId = "";
            for (PartyEntity vo : postList) {
                postIds += vo.getId() + ",";
            }

            String selectSql = "SELECT DISTINCT b.id as category_id,b.`NAME` as category_name,d.bpmProcessId,"
                    + "bpm.show_flag,bpm.DESCN,bpm.`NAME` as bpm_name,bpm.BYNAME FROM oa_ba_business_post p"
                    + " inner join oa_ba_business_detail d on p.detail_id = d.id"
                    + " inner join oa_ba_businesstype t on d.type_id = t.typeid"
                    + " inner join bpm_process bpm on d.bpmProcessId =bpm.ID"
                    //+ " inner join (select a.* from bpm_process a where a.PRIORITY = (select min(PRIORITY) from bpm_process where BYNAME = a.BYNAME)) bpm on d.bpmProcessId =bpm.ID"
                    + " inner join BPM_CATEGORY b on bpm.CATEGORY_ID = b.ID"
                    + " where bpm.show_flag = 1 and p.post_id in (" + postIds.substring(0, postIds.length() - 1) + ")"
            		+ " and b.id<>3"
            		+ " group by bpm.BYNAME"
                    + " order by b.PRIORITY,bpm.PRIORITY,d.bpmProcessId";
            List<Map<String, Object>> list = jdbcTemplate.queryForList(selectSql);

            BpmCategory bpmCategory = null;
            Set<BpmProcess> bpmProcesses =  new LinkedHashSet<>();

            for (Map<String, Object> map : list) {
                if ("".equals(categoryId)) {
                    bpmCategory = new BpmCategory();
                    BpmProcess bpmProcess = new BpmProcess();
                    categoryId = StringUtils.convertString(map.get("category_id"));
                    bpmCategory.setId(Long.parseLong(categoryId));
                    bpmCategory.setName(StringUtils.convertString(map.get("category_name")));

                    bpmProcess.setId(Long.parseLong(StringUtils.convertString(map.get("bpmProcessId"))));
                    bpmProcess.setName(StringUtils.convertString(map.get("BYNAME")));
                    bpmProcess.setDescn(StringUtils.convertString(map.get("DESCN")));
                    bpmProcess.setShowFlag(Integer.parseInt(StringUtils.convertString(map.get("show_flag"))));
                    bpmProcesses.add(bpmProcess);

                    ids += categoryId + ",";
                } else {
                    if (categoryId.equals(StringUtils.convertString(map.get("category_id")))) {
                        BpmProcess bpmProcess = new BpmProcess();
                        bpmProcess.setId(Long.parseLong(StringUtils.convertString(map.get("bpmProcessId"))));
                        bpmProcess.setName(StringUtils.convertString(map.get("BYNAME")));
                        bpmProcess.setDescn(StringUtils.convertString(map.get("DESCN")));
                        bpmProcess.setShowFlag(Integer.parseInt(StringUtils.convertString(map.get("show_flag"))));
                        bpmProcesses.add(bpmProcess);
                    } else {
                        bpmCategory.setBpmProcesses(bpmProcesses);
                        bpmCategories.add(bpmCategory);   // 将上一分类装入list

                        // 准备下一分类
                        bpmCategory = new BpmCategory();
                        BpmProcess bpmProcess = new BpmProcess();
                        bpmProcesses = new LinkedHashSet<BpmProcess>();
                        categoryId = StringUtils.convertString(map.get("category_id"));
                        bpmCategory.setId(Long.parseLong(categoryId));
                        bpmCategory.setName(StringUtils.convertString(map.get("category_name")));

                        bpmProcess.setId(Long.parseLong(StringUtils.convertString(map.get("bpmProcessId"))));
                        bpmProcess.setName(StringUtils.convertString(map.get("BYNAME")));
                        bpmProcess.setDescn(StringUtils.convertString(map.get("DESCN")));
                        bpmProcess.setShowFlag(Integer.parseInt(StringUtils.convertString(map.get("show_flag"))));
                        bpmProcesses.add(bpmProcess);

                        ids += categoryId + ",";
                    }
                }
            }

            if (list != null && list.size() > 0) {
                bpmCategory.setBpmProcesses(bpmProcesses);
                bpmCategories.add(bpmCategory);   // 将最后一个分类装入list
            }

            if ("".equals(ids)) {
            	BpmCategory bpm=getCommonBpmCategory();
                bpmCategories.add(bpm);
                if (bpmCategories != null && bpmCategories.size() >0) {
                	ids = "3";
                }
            } else {
                if (!ids.contains("3")) {
                	BpmCategory bpm=getCommonBpmCategory();
                	bpmCategories.add(bpm);
                    ids += "3,";
                }
            }
        }
        model.addAttribute("bpmCategories", bpmCategories);
        model.addAttribute("userName", userId);
        model.addAttribute("ids", ids);
        return "bpm/workspace-home";
    }
    
    private BpmCategory getCommonBpmCategory() {
    	Set<BpmProcess> bpmProcess_list=new LinkedHashSet<BpmProcess>();
    	BpmCategory bpm = bpmCategoryManager.get(3L);
        String process_hql = "from BpmProcess where showFlag = 1 and bpmCategory.id = 3 and tenantId=? order by priority";
        List<BpmProcess> bpms = bpmProcessManager.find(process_hql,tenantHolder.getTenantId());
        List<String> commonProcessByNameList=new ArrayList<String>();
        for (BpmProcess bpmProcess : bpms) {
        	if(operationConnector.IsShowCommonProcess(bpmProcess.getId(), currentUserHolder.getUserId())){
        		if(commonProcessByNameList.size()>0&&commonProcessByNameList.contains(bpmProcess.getByName())){
        			continue;
        		}
    			BpmProcess bpmProcess_common = new BpmProcess();
    			bpmProcess_common.setId(bpmProcess.getId());
    			bpmProcess_common.setName(bpmProcess.getByName());
    			bpmProcess_common.setDescn(bpmProcess.getDescn());
    			bpmProcess_common.setShowFlag(bpmProcess.getShowFlag());
    			bpmProcess_list.add(bpmProcess_common);
    			
    			commonProcessByNameList.add(bpmProcess.getByName());
        	}
		}
    	bpm.setBpmProcesses(bpmProcess_list);
        return bpm;
	}

    /**
     * 总部流程
     *
     * @param model
     * @return
     */
    @RequestMapping("workspace-homeByCategoryHQ")
    public String homeByCategoryHQ(Model model) {

        String tenantId = tenantHolder.getTenantId();
        String userName = currentUserHolder.getUsername();

        String hql = "from BpmCategory where tenantId=? order by priority";

        List<BpmCategory> bpmCategories = bpmCategoryManager.find(hql, tenantId);
        model.addAttribute("bpmCategories", bpmCategories);
        model.addAttribute("userName", userName);
        model.addAttribute("ids", "2,3");
        return "bpm/workspace-home";
    }

    /**
     * 分公司流程
     *
     * @param model
     * @return
     */
    @RequestMapping("workspace-homeByCategoryCommpany")
    public String homeByCategoryCommpany(Model model) {

        String tenantId = tenantHolder.getTenantId();
        String userName = currentUserHolder.getUsername();

        String hql = "from BpmCategory where tenantId=? order by priority";

        List<BpmCategory> bpmCategories = bpmCategoryManager.find(hql, tenantId);
        model.addAttribute("bpmCategories", bpmCategories);
        model.addAttribute("userName", userName);
        model.addAttribute("ids", "1,3");
        return "bpm/workspace-home";
    }

    @RequestMapping("workspace-graphProcessDefinition")
    public void graphProcessDefinition(
            @RequestParam("bpmProcessId") Long bpmProcessId,
            HttpServletResponse response) throws Exception {
        BpmProcess bpmProcess = bpmProcessManager.get(bpmProcessId);
        String processDefinitionId = bpmProcess.getBpmConfBase()
                .getProcessDefinitionId();

        Command<InputStream> cmd = null;
        cmd = new ProcessDefinitionDiagramCmd(processDefinitionId);

        InputStream is = processEngine.getManagementService().executeCommand(
                cmd);
        response.setContentType("image/png");

        IOUtils.copy(is, response.getOutputStream());
    }

    // ~ ======================================================================
    @RequestMapping("workspace-endProcessInstance")
    public String endProcessInstance(
            @RequestParam("processInstanceId") String processInstanceId,String humanTaskId) {//新增参数humanTaskId shijingxin
        Authentication.setAuthenticatedUserId(currentUserHolder.getUserId());
        //humanTaskConnectorImpl.removeHumanTask(humanTaskId);//删除任务 shijingxin
        workSpaceService.cancel(processInstanceId,Long.parseLong(humanTaskId));
        return "redirect:/bpm/workspace-listRunningProcessInstances.do";
    }

    @RequestMapping("workspace-copyProcessInstance")
    public String copyProcessInstance(
            @RequestParam("processInstanceId") String processInstanceId)
            throws Exception {
        // 复制流程
        // 1. 从历史获取businessKey
        HistoricProcessInstance historicProcessInstance = processEngine
                .getHistoryService().createHistoricProcessInstanceQuery()
                .processInstanceId(processInstanceId).singleResult();
        String businessKey = historicProcessInstance.getBusinessKey();
        String processDefinitionId = historicProcessInstance
                .getProcessDefinitionId();

        // 2. 从businessKey获取keyvalue
        Record original = keyValueConnector.findByCode(businessKey);

        // 3. 找到流程的第一个form
        FormDTO formDto = this.processConnector
                .findStartForm(processDefinitionId);

        List<String> fieldNames = new ArrayList<String>();

        if (formDto.isExists()) {
            String content = formDto.getContent();
            logger.debug("content : {}", content);

            Map<String, Object> formJson = jsonMapper.fromJson(
                    formDto.getContent(), Map.class);
            List<Map<String, Object>> sections = (List<Map<String, Object>>) formJson
                    .get("sections");

            for (Map<String, Object> section : sections) {
                if (!"grid".equals(section.get("type"))) {
                    continue;
                }

                List<Map<String, Object>> fields = (List<Map<String, Object>>) section
                        .get("fields");

                for (Map<String, Object> field : fields) {
                    logger.debug("field : {}", field);

                    String type = (String) field.get("type");
                    String name = (String) field.get("name");
                    String label = name;

                    if ("label".equals(type)) {
                        continue;
                    }

                    // if (formField != null) {
                    // continue;
                    // }
                    fieldNames.add(name);
                }
            }
        }

        logger.debug("fieldNames : {}", fieldNames);

        // 4. 使用第一个form复制数据，后续的审批意见数据之类的不要复制
        Record record = keyValueConnector.copyRecord(original, fieldNames);

        // 5. 跳转到草稿箱
        return "redirect:/operation/process-operation-listDrafts.do";
    }

    /**
     * 流程列表（所有的流程定义即流程模型）
     *
     * @return
     */
    @RequestMapping("workspace-listProcessDefinitions")
    public String listProcessDefinitions(Model model) {
        String tenantId = tenantHolder.getTenantId();
        RepositoryService repositoryService = processEngine
                .getRepositoryService();
        List<ProcessDefinition> processDefinitions = repositoryService
                .createProcessDefinitionQuery()
                .processDefinitionTenantId(tenantId).active().list();
        model.addAttribute("processDefinitions", processDefinitions);

        return "bpm/workspace-listProcessDefinitions";
    }

    @Log(desc = "流程中心", action = "查看", operationDesc = "流程中心-我的流程-未结流程-查看")
    @RequestMapping("workspace-listRunningProcessInstances")
    public String listRunningProcessInstances(@ModelAttribute Page page,
                                              @RequestParam Map<String, Object> parameterMap, Model model) {
        String userId = currentUserHolder.getUserId();
        String tenantId = tenantHolder.getTenantId();

        model = GetModelCmd(userId, model);

        Map<String, Object> map = this.convertAlias(parameterMap);

        List<PropertyFilter> propertyFilters = PropertyFilter.buildFromMap(map);

        page = processConnector.findRunningProcessInstances(page, propertyFilters, userId);


        model.addAttribute("page", page);

        model.addAttribute("statuslist", dictConnector.findDictInfoListByType("RecordStatus"));

        return "bpm/workspace-listRunningProcessInstances";
    }

    private Model GetModelCmd(String userId, Model model) {
        // PartyDTO partyDTO = partyConnector.findDepartmentById(userId);
        String tenantid = tenantHolder.getTenantId();

        //String hlSql = "from BusinessTypeEntity where departmentCode like '%" + partyDTO.getId() + "%' and tenantId=" + tenantid + " and enable='是'";
        String hlSql = "from BusinessTypeEntity where tenantId=" + tenantid + " and enable='是'";
        List<BusinessTypeEntity> entityList = businessTypeManager.find(hlSql);

        model.addAttribute("typelist", entityList);
        return model;
    }

    //导出未结流程
    @RequestMapping("listRunningProcessInstances-export")
    public void listRunningProcessInstancesExport(@ModelAttribute Page page,
                                  @RequestParam Map<String, Object> parameterMap, HttpServletResponse response,HttpServletRequest request)
            throws Exception {
    	String userId = currentUserHolder.getUserId();

        Map<String, Object> map = this.convertAlias(parameterMap);
        List<PropertyFilter> propertyFilters = PropertyFilter.buildFromMap(map);
        
        String detaliId = "";
        String formName = "";
        for (int i = 0; i < propertyFilters.size(); i++) {
        	PropertyFilter p = propertyFilters.get(i);
        	if(p.getPropertyName().equals("businessDetailId")){
        		detaliId = p.getMatchValue().toString();
        		boolean boo = ExportUtil.isNumeric(detaliId);
                if(!boo){
            	  	ExportUtil.errHtml(response);
            	  	return;
          		}else{
          			formName = humanTaskConnectorImpl.confirmBydetailId(detaliId);
              		break;
          		}
        	}
		}
        
        if(detaliId.length() != 0){
        	//含有细分，导出表单数据
        	page = processConnector.exportRunningProcessInstancesDetail(userId, propertyFilters, page,formName);
        }else{
        	//不含细分，导出列表数据
        	page = processConnector.exportRunningProcessInstances(page, propertyFilters, userId);
        }
        
        ExportUtil.export(page, response, request, detaliId, formName,"未结流程_");
    }
    
    /**
     * 已结流程.
     *
     * @return
     */
    @Log(desc = "流程中心", action = "查看", operationDesc = "流程中心-我的流程-已结流程-查看")
    @RequestMapping("workspace-listCompletedProcessInstances")
    public String listCompletedProcessInstances(@ModelAttribute Page page,
                                                @RequestParam Map<String, Object> parameterMap, Model model) {
        String userId = currentUserHolder.getUserId();
        String tenantId = tenantHolder.getTenantId();

        // page = processConnector.findCompletedProcessInstances(userId, tenantId, page);

        model = GetModelCmd(userId, model);

        Map<String, Object> map = this.convertAlias(parameterMap);

        List<PropertyFilter> propertyFilters = PropertyFilter.buildFromMap(map);

        page = processConnector.findCompletedProcessInstances(page, propertyFilters, userId);
        model.addAttribute("page", page);

        model.addAttribute("statuslist", dictConnector.findDictInfoListByType("RecordStatus"));

        return "bpm/workspace-listCompletedProcessInstances";
    }

    //导出办结流程
    @RequestMapping("listCompletedProcessInstances-export")
    public void listCompletedProcessInstancesExport(@ModelAttribute Page page,
                                                  @RequestParam Map<String, Object> parameterMap, HttpServletResponse response,HttpServletRequest request)
            throws Exception {
    	String userId = currentUserHolder.getUserId();

        Map<String, Object> map = this.convertAlias(parameterMap);
        List<PropertyFilter> propertyFilters = PropertyFilter.buildFromMap(map);
        String detaliId = "";
        String formName = "";
        for (int i = 0; i < propertyFilters.size(); i++) {
        	PropertyFilter p = propertyFilters.get(i);
        	if(p.getPropertyName().equals("businessDetailId")){
        		detaliId = p.getMatchValue().toString();
        		boolean boo = ExportUtil.isNumeric(detaliId);
                if(!boo){
            	  	ExportUtil.errHtml(response);
            	  	return;
          		}else{
          			formName = humanTaskConnectorImpl.confirmBydetailId(detaliId);
              		break;
          		}
        	}
		}
        
        if(detaliId.length() != 0){
        	//含有细分，导出表单数据
             page = processConnector.exportCompletedProcessInstancesDetail(page, propertyFilters, userId,formName);
        }else{
        	//不含细分，导出列表数据
             page = processConnector.exportCompletedProcessInstances(page, propertyFilters, userId);
        }
        
        ExportUtil.export(page, response, request, detaliId, formName,"办结流程_");
    }
    
    /**
     * 用户参与的流程（包含已经完成和未完成）
     *
     * @return
     */
    @RequestMapping("workspace-listInvolvedProcessInstances")
    public String listInvolvedProcessInstances(@ModelAttribute Page page,
                                               Model model) {
        // TODO: finished(), unfinished()
        String userId = currentUserHolder.getUserId();
        String tenantId = tenantHolder.getTenantId();
        page = processConnector.findInvolvedProcessInstances(userId, tenantId,
                page);
        model.addAttribute("page", page);

        return "bpm/workspace-listInvolvedProcessInstances";
    }

    /**
     * 流程跟踪
     *
     * @throws Exception
     */
    @RequestMapping("workspace-graphHistoryProcessInstance")
    public void graphHistoryProcessInstance(
            @RequestParam("processInstanceId") String processInstanceId,
            HttpServletResponse response) throws Exception {
        Command<InputStream> cmd = new HistoryProcessInstanceDiagramCmd(
                processInstanceId);

        InputStream is = processEngine.getManagementService().executeCommand(
                cmd);
        response.setContentType("image/png");

        int len = 0;
        byte[] b = new byte[1024];

        while ((len = is.read(b, 0, 1024)) != -1) {
            response.getOutputStream().write(b, 0, len);
        }
    }

    /**
     * 待办任务（个人任务）
     *
     * @return
     */
    @RequestMapping("workspace-listPersonalTasks")
    public String listPersonalTasks(@ModelAttribute Page page, Model model) {
        String userId = currentUserHolder.getUserId();
        String tenantId = tenantHolder.getTenantId();
        page = processConnector.findPersonalTasks(userId, tenantId, page);
        model.addAttribute("page", page);

        return "bpm/workspace-listPersonalTasks";
    }

    /**
     * 代领任务（组任务）
     *
     * @return
     */
    @RequestMapping("workspace-listGroupTasks")
    public String listGroupTasks(@ModelAttribute Page page, Model model) {
        String userId = currentUserHolder.getUserId();
        String tenantId = tenantHolder.getTenantId();

        page = processConnector.findGroupTasks(userId, tenantId, page);
        model.addAttribute("page", page);

        return "bpm/workspace-listGroupTasks";
    }

    /**
     * 已办任务（历史任务）
     *
     * @return
     */
    @RequestMapping("workspace-listHistoryTasks")
    public String listHistoryTasks(@ModelAttribute Page page, Model model) {
        String userId = currentUserHolder.getUserId();
        String tenantId = tenantHolder.getTenantId();

        page = processConnector.findHistoryTasks(userId, tenantId, page);
        model.addAttribute("page", page);

        return "bpm/workspace-listHistoryTasks";
    }

    /**
     * 代理中的任务（代理人还未完成该任务）
     *
     * @return
     */
    @RequestMapping("workspace-listDelegatedTasks")
    public String listDelegatedTasks(@ModelAttribute Page page, Model model) {
        String userId = currentUserHolder.getUserId();
        String tenantId = tenantHolder.getTenantId();
        page = processConnector.findGroupTasks(userId, tenantId, page);
        model.addAttribute("page", page);

        return "bpm/workspace-listDelegatedTasks";
    }

    /**
     * 同时返回已领取和未领取的任务.
     */
    @RequestMapping("workspace-listCandidateOrAssignedTasks")
    public String listCandidateOrAssignedTasks(@ModelAttribute Page page,
                                               Model model) {
        String userId = currentUserHolder.getUserId();
        String tenantId = tenantHolder.getTenantId();
        page = processConnector.findCandidateOrAssignedTasks(userId, tenantId,
                page);
        model.addAttribute("page", page);

        return "bpm/workspace-listCandidateOrAssignedTasks";
    }

    // ~ ======================================================================

    /**
     * 发起流程页面（启动一个流程实例）内置流程表单方式
     *
     * @return
     */
    @RequestMapping("workspace-prepareStartProcessInstance")
    public String prepareStartProcessInstance(
            @RequestParam("processDefinitionId") String processDefinitionId,
            Model model) {
        FormService formService = processEngine.getFormService();
        StartFormData startFormData = formService
                .getStartFormData(processDefinitionId);
        model.addAttribute("startFormData", startFormData);

        return "bpm/workspace prepareStartProcessInstance";
    }

    // ~ ======================================================================

    /**
     * 完成任务页面
     *
     * @return
     */
    @RequestMapping("workspace-prepareCompleteTask")
    public String prepareCompleteTask(@RequestParam("taskId") String taskId,
                                      Model model) {
        FormService formService = processEngine.getFormService();

        TaskFormData taskFormData = formService.getTaskFormData(taskId);

        model.addAttribute("taskFormData", taskFormData);

        return "bpm/workspace-prepareCompleteTask";
    }

    /**
     * 认领任务（对应的是在组任务，即从组任务中领取任务）
     *
     * @return
     */
    @RequestMapping("workspace-claimTask")
    public String claimTask(@RequestParam("taskId") String taskId) {
        String userId = currentUserHolder.getUserId();

        TaskService taskService = processEngine.getTaskService();
        taskService.claim(taskId, userId);
        
        return "redirect:/bpm/workspace-listPersonalTasks.do";
    }

    /**
     * 任务代理页面
     *
     * @return
     */
    @RequestMapping("workspace-prepareDelegateTask")
    public String prepareDelegateTask() {
        return "bpm/workspace-prepareDelegateTask";
    }

    /**
     * 任务代理
     *
     * @return
     */
    @RequestMapping("workspace-delegateTask")
    public String delegateTask(@RequestParam("taskId") String taskId,
                               @RequestParam("userId") String userId) {
        TaskService taskService = processEngine.getTaskService();
        taskService.delegateTask(taskId, userId);

        return "redirect:/bpm/workspace-listPersonalTasks.do";
    }

    /**
     * TODO 该方法有用到？
     *
     * @return
     */
    @RequestMapping("workspace-resolveTask")
    public String resolveTask(@RequestParam("taskId") String taskId) {
        TaskService taskService = processEngine.getTaskService();
        taskService.resolveTask(taskId);

        return "redirect:/bpm/workspace-listPersonalTasks.do";
    }

    /**
     * 查看历史【包含流程跟踪、任务列表（完成和未完成）、流程变量】.
     */
    @RequestMapping("workspace-viewHistory")
    public String viewHistory(
            @RequestParam("processInstanceId") String processInstanceId,
            @RequestParam(value = "url", required = false) String url,
            @RequestParam(value = "viewBack", required = false) boolean viewBack,
            Model model) {

        if (url.indexOf("custom") >= 0) {
            return "redirect:" + url + "&processInstanceId=" + processInstanceId;
        }

        String userId = currentUserHolder.getUserId();
        HistoryService historyService = processEngine.getHistoryService();
        HistoricProcessInstance historicProcessInstance = historyService
                .createHistoricProcessInstanceQuery()
                .processInstanceId(processInstanceId).singleResult();

        if (userId.equals(historicProcessInstance.getStartUserId())) {
            // startForm
        }

        List<HistoricTaskInstance> historicTasks = historyService
                .createHistoricTaskInstanceQuery()
                .processInstanceId(processInstanceId).list();
        // List<HistoricVariableInstance> historicVariableInstances = historyService
        // .createHistoricVariableInstanceQuery()
        // .processInstanceId(processInstanceId).list();
        model.addAttribute("historicTasks", historicTasks);

        // 获取流程对应的所有人工任务（目前还没有区分历史）
        List<HumanTaskDTO> humanTasks = humanTaskConnector
                .findHumanTasksForPositionByProcessInstanceId(processInstanceId);
        List<HumanTaskDTO> humanTaskDtos = new ArrayList<HumanTaskDTO>();

        for (HumanTaskDTO humanTaskDto : humanTasks) {
            if (humanTaskDto.getParentId() != null) {
                continue;
            }

            humanTaskDtos.add(humanTaskDto);
        }

        model.addAttribute("humanTasks", humanTaskDtos);
        // model.addAttribute("historicVariableInstances",
        // historicVariableInstances);
        model.addAttribute("nodeDtos",
                traceService.traceProcessInstance(processInstanceId));
        model.addAttribute("historyActivities", processEngine
                .getHistoryService().createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceId).list());

        if (historicProcessInstance.getEndTime() == null) {
            model.addAttribute("currentActivities", processEngine
                    .getRuntimeService()
                    .getActiveActivityIds(processInstanceId));
        } else {
            model.addAttribute("currentActivities", Collections
                    .singletonList(historicProcessInstance.getEndActivityId()));
        }

        // TODO zyl 屏蔽，再观察
        Graph graph = null;
        /*graph = processEngine.getManagementService().executeCommand(
                new FindHistoryGraphCmd(processInstanceId));*/
        model.addAttribute("graph", graph);
        model.addAttribute("historicProcessInstance", historicProcessInstance);
        model.addAttribute("url", url);
        model.addAttribute("viewBack", viewBack);
        return "bpm/workspace-viewHistory";
    }

    /**
     * 查看表单详情
     */
    @RequestMapping("workspace-viewHistoryFrom")
    public String viewHistoryFrom(@RequestParam("url") String url,
                                  @RequestParam("processInstanceId") String processInstanceId,
                                  @RequestParam(value = "viewBack", required = false) boolean viewBack) {
        if (url.indexOf("custom") >= 0) {
            return "redirect:" + url + "&processInstanceId=" + processInstanceId + "&viewBack=" + viewBack;
        } else {
            return "redirect:" + url + "?processInstanceId=" + processInstanceId + "&viewBack=" + viewBack;
        }
    }

    // ~ ==================================国内特色流程====================================

    /**
     * 回退任务
     *
     * @return
     */
    @RequestMapping("workspace-rollback")
    public String rollback(@RequestParam("taskId") String taskId) {
        Command<Object> cmd = new RollbackTaskCmd(taskId, null);

        processEngine.getManagementService().executeCommand(cmd);

        return "redirect:/bpm/workspace-listPersonalTasks.do";
    }

    /**
     * 撤销任务
     *
     * @return
     */

    /*
     * @RequestMapping("workspace-withdraw") public String withdraw(@RequestParam("taskId") String taskId) {
     * Command<Integer> cmd = new WithdrawTaskCmd(taskId);
     * 
     * processEngine.getManagementService().executeCommand(cmd);
     * 
     * return "redirect:/bpm/workspace-listPersonalTasks.do"; }
     */

    /**
     * 准备加减签.
     */
    @RequestMapping("workspace-changeCounterSign")
    public String changeCounterSign() {
        return "bpm/workspace-changeCounterSign";
    }

    /**
     * 进行加减签.
     */
    @RequestMapping("workspace-saveCounterSign")
    public String saveCounterSign(
            @RequestParam("operationType") String operationType,
            @RequestParam("userId") String userId,
            @RequestParam("taskId") String taskId) {
        CounterSignCmd cmd = new CounterSignCmd(operationType, userId, taskId);

        processEngine.getManagementService().executeCommand(cmd);

        return "redirect:/bpm/workspace-listPersonalTasks.do";
    }

    /**
     * 转发已结流程.
     */
    @RequestMapping("workspace-transferProcessInstance")
    public String transferProcessInstance(
            @RequestParam("processInstanceId") String processInstanceId,
            @RequestParam("assignee") String assignee) {
        String tenantId = tenantHolder.getTenantId();

        // 1. 找到历史
        HistoricProcessInstance historicProcessInstance = processEngine
                .getHistoryService().createHistoricProcessInstanceQuery()
                .processInstanceId(processInstanceId).singleResult();

        // 2. 创建一个任务，设置为未读，转发状态
        HumanTaskDTO humanTaskDto = humanTaskConnector.createHumanTask();
        humanTaskDto.setProcessInstanceId(processInstanceId);
        humanTaskDto.setPresentationSubject(historicProcessInstance.getName());
        humanTaskDto.setAssignee(assignee);
        humanTaskDto.setTenantId(tenantId);
        // TODO: 还没有字段
        // humanTaskDto.setCopyStatus("unread");
        humanTaskDto.setCatalog(HumanTaskConstants.CATALOG_COPY);
        humanTaskDto.setAction("unread");
        humanTaskDto.setBusinessKey(historicProcessInstance.getBusinessKey());
        humanTaskDto.setProcessDefinitionId(historicProcessInstance
                .getProcessDefinitionId());

        try {
            // TODO: 等到流程支持viewFormKey，才能设置。目前做不到
            List<HistoricTaskInstance> historicTaskInstances = processEngine
                    .getHistoryService().createHistoricTaskInstanceQuery()
                    .processInstanceId(processInstanceId).list();
            HistoricTaskInstance historicTaskInstance = historicTaskInstances
                    .get(0);
            humanTaskDto.setForm(historicTaskInstance.getFormKey());
            humanTaskDto.setName(historicTaskInstance.getName());
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }

        humanTaskConnector.saveHumanTask(humanTaskDto);

        // 3. 把任务分配给对应的人员
        return "redirect:/bpm/workspace-listCompletedProcessInstances.do";
    }

    /**
     * 催办.
     */
    @RequestMapping("workspace-remind")
    public String remind(
            @RequestParam("processInstanceId") String processInstanceId,
            @RequestParam("userId") String userId,
            @RequestParam("comment") String comment) {
        List<HumanTaskDTO> humanTaskDtos = humanTaskConnector
                .findHumanTasksForPositionByProcessInstanceId(processInstanceId);
        logger.debug("processInstanceId : {}", processInstanceId);

        logger.debug("humanTaskDtos : {}", humanTaskDtos);

        for (HumanTaskDTO humanTaskDto : humanTaskDtos) {
            if (humanTaskDto.getCompleteTime() != null) {
                continue;
            }

            String assignee = humanTaskDto.getAssignee();
            logger.debug("remind {}", assignee);

            NotificationDTO notificationDto = new NotificationDTO();
            notificationDto.setSender(currentUserHolder.getUserId());
            notificationDto.setReceiver(assignee);
            notificationDto.setReceiverType("userid");
            notificationDto.getTypes().add("msg");
            // notificationDto.getTypes().add("email");
            notificationDto.setSubject("请尽快办理 "
                    + humanTaskDto.getPresentationSubject());

            String url = baseUrl
                    + "/operation/task-operation-viewTaskForm.do?humanTaskId="
                    + humanTaskDto.getId();
            String content = "请尽快办理 " + humanTaskDto.getPresentationSubject()
                    + "<p><a href='" + url + "'>" + url + "</a></p>";
            notificationDto.setContent(content);
            notificationDto.setMsgType(MsgConstants.MSG_TYPE_BPM);
            notificationConnector.send(notificationDto, "1");
        }

        return "redirect:/bpm/workspace-listRunningProcessInstances.do";
    }

    /**
     * 跳过.
     */
    @RequestMapping("workspace-skip")
    public String skip(
            @RequestParam("processInstanceId") String processInstanceId,
            @RequestParam("userId") String userId,
            @RequestParam("comment") String comment) {
        List<HumanTaskDTO> humanTaskDtos = humanTaskConnector
                .findHumanTasksForPositionByProcessInstanceId(processInstanceId);
        logger.debug("processInstanceId : {}", processInstanceId);

        logger.debug("humanTaskDtos : {}", humanTaskDtos);

        for (HumanTaskDTO humanTaskDto : humanTaskDtos) {
            if (humanTaskDto.getCompleteTime() != null) {
                continue;
            }

            String humanTaskId = humanTaskDto.getId();
            humanTaskConnector.skip(humanTaskId, currentUserHolder.getUserId(),
                    comment);
        }

        return "redirect:/bpm/workspace-listRunningProcessInstances.do";
    }

    /**
     * 撤销.
     * 此方法为原系统自带
     * 麦联常规流程撤回在rs/BpmResource包下
     */
    @RequestMapping("workspace-withdraw")
    public String withdraw(
            @RequestParam("processInstanceId") String processInstanceId) {
        logger.debug("processInstanceId : {}", processInstanceId);

        ProcessInstance processInstance = processEngine.getRuntimeService()
                .createProcessInstanceQuery()
                .processInstanceId(processInstanceId).singleResult();
        String initiator = "";
        String firstUserTaskActivityId = internalProcessConnector
                .findFirstUserTaskActivityId(
                        processInstance.getProcessDefinitionId(), initiator);
        logger.debug("firstUserTaskActivityId : {}", firstUserTaskActivityId);

        List<HistoricTaskInstance> historicTaskInstances = processEngine
                .getHistoryService().createHistoricTaskInstanceQuery()
                .processInstanceId(processInstanceId)
                .taskDefinitionKey(firstUserTaskActivityId).list();
        HistoricTaskInstance historicTaskInstance = historicTaskInstances
                .get(0);
        String taskId = historicTaskInstance.getId();
        HumanTaskDTO humanTaskDto = humanTaskConnector
                .findHumanTaskByTaskId(taskId);
        String comment = "";
        humanTaskConnector.withdraw(humanTaskDto.getId(), comment);

        return "redirect:/bpm/workspace-listRunningProcessInstances.do";
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
    
    // ~ ======================================================================
    @Resource
    public void setBpmCategoryManager(BpmCategoryManager bpmCategoryManager) {
        this.bpmCategoryManager = bpmCategoryManager;
    }

    @Resource
    public void setBpmProcessManager(BpmProcessManager bpmProcessManager) {
        this.bpmProcessManager = bpmProcessManager;
    }

    @Resource
    public void setProcessEngine(ProcessEngine processEngine) {
        this.processEngine = processEngine;
    }

    @Resource
    public void setUserConnector(UserConnector userConnector) {
        this.userConnector = userConnector;
    }

    @Resource
    public void setProcessConnector(ProcessConnector processConnector) {
        this.processConnector = processConnector;
    }

    @Resource
    public void setCurrentUserHolder(CurrentUserHolder currentUserHolder) {
        this.currentUserHolder = currentUserHolder;
    }

    @Resource
    public void setTraceService(TraceService traceService) {
        this.traceService = traceService;
    }

    @Resource
    public void setTenantHolder(TenantHolder tenantHolder) {
        this.tenantHolder = tenantHolder;
    }

    @Resource
    public void setKeyValueConnector(KeyValueConnector keyValueConnector) {
        this.keyValueConnector = keyValueConnector;
    }

    @Resource
    public void setHumanTaskConnector(HumanTaskConnector humanTaskConnector) {
        this.humanTaskConnector = humanTaskConnector;
    }

    @Resource
    public void setNotificationConnector(
            NotificationConnector notificationConnector) {
        this.notificationConnector = notificationConnector;
    }

    @Resource
    public void setInternalProcessConnector(
            InternalProcessConnector internalProcessConnector) {
        this.internalProcessConnector = internalProcessConnector;
    }

    @Value("${application.baseUrl}")
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Resource
    public void setOrgConnector(OrgConnector orgConnector) {
        this.orgConnector = orgConnector;
    }

    @Resource
    public void setBusinessTypeManager(BusinessTypeManager businessTypeManager) {
        this.businessTypeManager = businessTypeManager;
    }

    @Resource
    public void setPartyConnector(PartyConnector partyConnector) {
        this.partyConnector = partyConnector;
    }

    @Resource
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Resource
    public void setDictConnectorImpl(DictConnector dictConnector) {
        this.dictConnector = dictConnector;
    }
    
    @Resource
    public void setMessageHelper(MessageHelper messageHelper) {
        this.messageHelper = messageHelper;
    }
}
