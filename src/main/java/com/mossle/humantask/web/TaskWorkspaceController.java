package com.mossle.humantask.web;

import com.alibaba.fastjson.JSONObject;
import com.hp.hpl.sparta.ParseLog;
import com.mossle.api.dict.DictConnector;
import com.mossle.api.humantask.HumanTaskConnector;
import com.mossle.api.humantask.HumanTaskConstants;
import com.mossle.api.humantask.HumanTaskDTO;
import com.mossle.api.org.OrgConnector;
import com.mossle.api.party.PartyConnector;
import com.mossle.api.party.PartyDTO;
import com.mossle.api.tenant.TenantHolder;
import com.mossle.base.persistence.domain.BusinessDetailEntity;
import com.mossle.base.persistence.domain.BusinessTypeEntity;
import com.mossle.base.persistence.manager.BusinessDetailManager;
import com.mossle.base.persistence.manager.BusinessTypeManager;
import com.mossle.bpm.web.WorkspaceController;
import com.mossle.common.utils.PasswordUtil;
import com.mossle.core.auth.CurrentUserHolder;
import com.mossle.core.export.ExcelExport;
import com.mossle.core.export.Exportor;
import com.mossle.core.mapper.BeanMapper;
import com.mossle.core.mapper.JsonMapper;
import com.mossle.core.page.Page;
import com.mossle.core.query.PropertyFilter;
import com.mossle.core.spring.MessageHelper;
import com.mossle.core.spring.SignInfo;
import com.mossle.core.util.StringUtils;
import com.mossle.dict.persistence.domain.DictInfo;
import com.mossle.dict.support.DictConnectorImpl;
import com.mossle.humantask.persistence.domain.TaskInfo;
import com.mossle.humantask.persistence.domain.UnfinishProcessInstance;
import com.mossle.humantask.persistence.manager.TaskInfoManager;
import com.mossle.humantask.support.HumanTaskConnectorImpl;
import com.mossle.operation.service.OperationService;
import com.mossle.party.persistence.domain.PartyEntity;
import com.mossle.party.persistence.domain.PartyStruct;
import com.mossle.party.persistence.domain.PartyStructType;
import com.mossle.party.persistence.manager.PartyEntityManager;
import com.mossle.party.persistence.manager.PartyStructManager;
import com.mossle.party.persistence.manager.PartyStructTypeManager;
import com.mossle.party.rs.PartyResource;
import com.mossle.user.persistence.domain.PersonInfo;
import com.mossle.user.persistence.manager.PersonInfoManager;
import com.mossle.util.ExportUtil;
import com.mossle.util.StringUtil;
import com.mossle.ws.online.Common;

import org.apache.poi.sl.draw.binding.STRectAlignment;
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

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
@RequestMapping("humantask")
public class TaskWorkspaceController {
    private static Logger logger = LoggerFactory
            .getLogger(TaskWorkspaceController.class);
    private TaskInfoManager taskInfoManager;
    private Exportor exportor;
    private BeanMapper beanMapper = new BeanMapper();
    private JsonMapper jsonMapper = new JsonMapper();
    private MessageHelper messageHelper;
    private CurrentUserHolder currentUserHolder;
    private JdbcTemplate jdbcTemplate;
    private HumanTaskConnector humanTaskConnector;
    private TenantHolder tenantHolder;
    private BusinessTypeManager businessTypeManager;
    private BusinessDetailManager businessDetailManager;
    private OrgConnector orgConnector;
    private PartyConnector partyConnector;
    private DictConnectorImpl dictConnectorImpl;
    private PartyEntityManager partyEntityManager;
    private PartyStructManager partyStructManager;
    private DictConnector dictConnector;
    private PersonInfoManager personInfoManager;
    private HumanTaskConnectorImpl humanTaskConnectorImpl;
	private Map<String, String> aliasMap = new HashMap<String, String>();
    SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
    private SignInfo signInfo;
    private PartyStructTypeManager partyStructTypeManager;
    private PartyResource partyResource;
    @Autowired
    private OperationService operationService;
    
    /**
     * 待办任务.
     */
    @RequestMapping("workspace-personalTasks")
    public String personalTasks(@ModelAttribute Page page,
                                @RequestParam Map<String, Object> parameterMap, Model model) {
        
    	String userId = currentUserHolder.getUserId();
        String tenantId = tenantHolder.getTenantId();

        // page = humanTaskConnector.findPersonalTasks(userId, tenantId, page.getPageNo(), page.getPageSize());

        model = GetModelCmd(userId, model);

        Map<String, Object> map = this.convertAlias(parameterMap);
        List<PropertyFilter> propertyFilters =PropertyFilter.buildFromMap(map);
        page = humanTaskConnector.findPersonalTasks(userId, tenantId, propertyFilters, page);
        model.addAttribute("page", page);

        return "humantask/workspace-personalTasks";
    }

    /**
     * 抄送任务.
     */
    @RequestMapping("workspace-personalCopyTasks")
    public String personalCopyTasks(@ModelAttribute Page page,
                                    @RequestParam Map<String, Object> parameterMap, Model model) {
        String userId = currentUserHolder.getUserId();
        String tenantId = tenantHolder.getTenantId();

        // page = humanTaskConnector.findPersonalTasks(userId, tenantId, page.getPageNo(), page.getPageSize());
        String status = StringUtil.toString(parameterMap.get("status"));
        model = GetModelCmd(userId, model);

        Map<String, Object> map = this.convertAlias(parameterMap);

        List<PropertyFilter> propertyFilters = PropertyFilter.buildFromMap(map);
        page = humanTaskConnector.findPersonalCopyTasks(userId, tenantId, propertyFilters, page,status);
        model.addAttribute("page", page);
        model.addAttribute("statuslist", dictConnector.findDictInfoListByType("RecordStatus"));
        model.addAttribute("status", status);
        return "humantask/workspace-personalCopyTasks";
    }

    
    /**
     * 流程中心 -查看协同老数据 .
     * @throws Exception 
     */
    @RequestMapping("workspace-viewOldData")
    public String personalviewOldData(@ModelAttribute Page page,
                                    @RequestParam Map<String, Object> parameterMap, Model model) throws Exception {
    	
    
       Long accountId = Long.parseLong(currentUserHolder.getUserId());  

       String sqlString = "SELECT oldSysUserName FROM account_credential WHERE ACCOUNT_ID = "+accountId;

       String oldSysUserName = jdbcTemplate.queryForObject(sqlString, String.class);
       if(oldSysUserName==null)
    	   oldSysUserName="";
       //String oldSysUserName =  list.get(0).get("oldSysUserName").toString();
       
       String paramKey=signInfo.getParamKey();	
       String strEnUserName=Common.Encrypt3DES(oldSysUserName,paramKey);
       
       String strSignKey=signInfo.getSignKey();
       String signStr = oldSysUserName + strSignKey;
       signStr = Md5(signStr);
       
       
       List<DictInfo> dictInfos = dictConnector.findDictInfoListByType("personOldDataLink");
       
       model.addAttribute("oldSysUserName",oldSysUserName);
       model.addAttribute("oldDataUserName", strEnUserName);
       model.addAttribute("signStr", signStr);
       model.addAttribute("dictInfos", dictInfos.get(0).getValue());
       
        return "humantask/workspace-viewOldData";
    }
    
    public static String Md5(String value)
	{
		value=PasswordUtil.getMD5(value);
		return value;
	}
	
    
    
    /**
     * 全部审批.
     */
    @RequestMapping("workspace-allApproval")
    public String allApproval(@ModelAttribute Page page,
                              @RequestParam Map<String, Object> parameterMap, Model model) {
        String userId = currentUserHolder.getUserId();
        String tenantId = tenantHolder.getTenantId();

        // page = humanTaskConnector.findPersonalTasks(userId, tenantId, page.getPageNo(), page.getPageSize());

        model = GetModelCmd(userId, model);

        Map<String, Object> map = this.convertAlias(parameterMap);

        List<PropertyFilter> propertyFilters = PropertyFilter.buildFromMap(map);
        page = humanTaskConnector.findAllApproval(userId, tenantId, propertyFilters, page);
        model.addAttribute("page", page);
        model.addAttribute("statuslist", dictConnector.findDictInfoListByType("RecordStatus"));
        return "humantask/workspace-allApproval";
    }

    /**
     * 定制审批  20171106 chengze.
     */
    @RequestMapping("workspace-speicalPeopleApproval")
    public String speicalPeopleApproval(@ModelAttribute Page page,
                                        @RequestParam Map<String, Object> parameterMap, Model model) {
        String userId = currentUserHolder.getUserId();
        String tenantId = tenantHolder.getTenantId();

        model = GetModelCmd(userId, model);

        Map<String, Object> map = this.convertAlias(parameterMap);

        List<PropertyFilter> propertyFilters = PropertyFilter.buildFromMap(map);
        page = humanTaskConnector.findSpeicalPeopleApproval(userId, tenantId, propertyFilters, page);
        model.addAttribute("page", page);
        model.addAttribute("statuslist", dictConnector.findDictInfoListByType("RecordStatus"));
        return "humantask/workspace-speicalPeople";
    }

    /**
     * 部门申请.cz
     */
    @RequestMapping("workspace-departmentApplication")
    public String departmentApplication(@ModelAttribute Page page,
                                        @RequestParam Map<String, Object> parameterMap, Model model) {
        String userId = currentUserHolder.getUserId();
        String tenantId = tenantHolder.getTenantId();
        String checkArea = "0";//0为非分公司职员
        model = GetModelCmd(userId, model);
         Map<String, Object> map = this.convertAlias(parameterMap);
        List<PropertyFilter> propertyFilters = PropertyFilter.buildFromMap(map);
        
        //判断当前登录人是否该[部门/公司]的管理者 ，若是管理者 返回数据，若不是管理者 就没有权限查看数据，返回空
        
        //获取部门id
        String departmentId = "";
        String orgID = "";
      
        /**
         * 获得管理者对应的组织机构.
         */
        
        List<Long> isManager =  orgConnector.getPartyByManageId(userId);
        if(isManager!=null&&isManager.size()>0){
        	for(int i =0;i<isManager.size();i++){
        		orgID = orgID + isManager.get(i).toString() + ",";
        	}
        	orgID = orgID.substring(0, orgID.length()-1);	
        
	        //寻找该 组织机构 下面所有的组织机构
	    	List<String> list = partyResource.getPartyEntityNoPerson(orgID);
	    	List<String> newList = removeDuplicateWithOrder(list);
	    	//遍历结果，将这些组织机构的id放入一个字符串
	    	for(int i =0;i<newList.size();i++){
	    		departmentId= departmentId + newList.get(i)+",";
	    	}
	    	if (StringUtils.isNotBlank(departmentId)) {
	    		departmentId = departmentId.substring(0, departmentId.length()-1);	
	    		departmentId = departmentId + "," + orgID;
	    	} else {
	    		departmentId = orgID;
	    	}
	    	
	    	if (StringUtils.isBlank(departmentId)) {
	    		departmentId = "9999";
	    	}
	        //查询这些组织机构中发起的审批流程
	        page = humanTaskConnector.findDepartmentApplication(userId, tenantId, departmentId, propertyFilters, page,checkArea);
	
	        model.addAttribute("page", page);
	        model.addAttribute("statuslist", dictConnector.findDictInfoListByType("RecordStatus"));
        }
        return "humantask/workspace-departmentApplication";
    }

    /**
     * 部门审批.wh
     */
    
    @RequestMapping("workspace-departmentApproval")
    public String departmentApproval(@ModelAttribute Page page,
                                     @RequestParam Map<String, Object> parameterMap, Model model) {
        String userId = currentUserHolder.getUserId();
        String tenantId = tenantHolder.getTenantId();
        String checkArea = "0";//0为非分公司职员
        model = GetModelCmd(userId, model);
        Map<String, Object> map = this.convertAlias(parameterMap);
        List<PropertyFilter> propertyFilters = PropertyFilter.buildFromMap(map);
        
        //判断当前登录人是否该[部门/公司]的管理者 ，若是管理者 返回数据，若不是管理者 就没有权限查看数据，返回空
        
        //获取部门id
        String departmentId = "";
        String orgID = "";
      
        /**
         * 获得管理者对应的组织机构.
         */
        
        List<Long> isManager =  orgConnector.getPartyByManageId(userId);
        if(isManager!=null&&isManager.size()>0){
        	for(int i =0;i<isManager.size();i++){
        		orgID = orgID + isManager.get(i).toString() + ",";
        	}
        	orgID = orgID.substring(0, orgID.length()-1);	
        
	        //寻找该 组织机构 下面所有的组织机构
	    	List<String> list = partyResource.getPartyEntityNoPerson(orgID);
	    	List<String> newList = removeDuplicateWithOrder(list);
	    	//遍历结果，将这些组织机构的id放入一个字符串
	    	for(int i =0;i<newList.size();i++){
	    		departmentId= departmentId + newList.get(i)+",";
	    	}
	    	if (StringUtils.isNotBlank(departmentId)) {
	    		departmentId = departmentId.substring(0, departmentId.length()-1);	
	    		departmentId = departmentId + "," + orgID;
	    	} else {
	    		departmentId = orgID;
	    	}
	    	
	    	if (StringUtils.isBlank(departmentId)) {
	    		departmentId = "9999";
	    	}
	    	
	        //查询这些组织机构中发起的审批流程
	        page = humanTaskConnector.findDepartmentApproval(userId, tenantId, departmentId, propertyFilters, page,checkArea);
	        List<DictInfo> statuslist = dictConnector.findDictInfoListByType("RecordStatus");
	        List<DictInfo> temp = new ArrayList<>();
	        for (DictInfo dictInfo : statuslist) {
	            if (dictInfo.getValue().equals("0")) {
	                temp.add(dictInfo);
	            }
	        }
	        statuslist.removeAll(temp);
	        model.addAttribute("page", page);
	        model.addAttribute("statuslist", statuslist);
       }
       return "humantask/workspace-departmentApproval";
    }
    
    // 去重
    private List<String> removeDuplicateWithOrder(List<String> list) {    
        Set set = new HashSet();    
        List newList = new ArrayList();    
       for (Iterator iter = list.iterator(); iter.hasNext();) {    
             Object element = iter.next();    
             if (set.add(element))    
                newList.add(element);    
          }     
         list.clear();    
         list.addAll(newList);    
         return list;
     }   
    
    /**
     * 公司申请.wh
     */
    @RequestMapping("workspace-companyApplication")
    public String companyApplication(@ModelAttribute Page page,
                                     @RequestParam Map<String, Object> parameterMap, Model model) {
        String userId = currentUserHolder.getUserId();
        String tenantId = tenantHolder.getTenantId();
        String checkArea = "0";//0为非分公司职员
        model = GetModelCmd(userId, model);

        Map<String, Object> map = this.convertAlias(parameterMap);

        List<PropertyFilter> propertyFilters = PropertyFilter.buildFromMap(map);
        //获取公司id
        PersonInfo personInfo = personInfoManager.findUniqueBy("id", Long.parseLong(userId));
        String companyId = personInfo.getCompanyCode();

        //分公司人员获取公司id
        String departmentId = personInfo.getDepartmentCode();
        PartyDTO partyDTOArea = partyConnector.findAreaById(departmentId);
        if (partyDTOArea != null) {
            companyId = personInfo.getDepartmentCode();
            checkArea = "1";
        }
        page = humanTaskConnector.findCompanyApplication(userId, tenantId, companyId, propertyFilters, page,checkArea);
        model.addAttribute("page", page);
        model.addAttribute("statuslist", dictConnector.findDictInfoListByType("RecordStatus"));
        return "humantask/workspace-companyApplication";
    }


    /**
     * 公司审批.wh
     */
    @RequestMapping("workspace-companyApproval")
    public String companyApproval(@ModelAttribute Page page,
                                  @RequestParam Map<String, Object> parameterMap, Model model) {
        String userId = currentUserHolder.getUserId();
        String tenantId = tenantHolder.getTenantId();
        String checkArea = "0";//0为非分公司职员
        model = GetModelCmd(userId, model);

        Map<String, Object> map = this.convertAlias(parameterMap);

        List<PropertyFilter> propertyFilters = PropertyFilter.buildFromMap(map);
        //获取公司id
        PersonInfo personInfo = personInfoManager.findUniqueBy("id", Long.parseLong(userId));
        String companyId = personInfo.getCompanyCode();

        //分公司人员获取公司id
        String departmentId = personInfo.getDepartmentCode();
        PartyDTO partyDTOArea = partyConnector.findAreaById(departmentId);
        if (partyDTOArea != null) {
            companyId = personInfo.getDepartmentCode();
            checkArea = "1";
        }
        page = humanTaskConnector.findCompanyApproval(userId, tenantId, companyId, propertyFilters, page, checkArea);

        List<DictInfo> statuslist = dictConnector.findDictInfoListByType("RecordStatus");
        List<DictInfo> temp = new ArrayList<>();
        for (DictInfo dictInfo : statuslist) {
            if (dictInfo.getValue().equals("0")) {
                temp.add(dictInfo);
            }
        }
        statuslist.removeAll(temp);
        model.addAttribute("page", page);
        model.addAttribute("statuslist", statuslist);
        return "humantask/workspace-companyApproval";
    }

    /**
     * @return
     * 流程中心--我的待办--管理者
     */
    @RequestMapping("workspace-manageQuery")
    public String manageQuery(@ModelAttribute Page page,@RequestParam Map<String, Object> parameterMap, Model model){
    	String userId = currentUserHolder.getUserId();
        String tenantId = tenantHolder.getTenantId();
        model = GetModelCmd(userId, model);
        Map<String, Object> map = this.convertAlias(parameterMap);
        List<PropertyFilter> propertyFilters =PropertyFilter.buildFromMap(map);
        
        try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        page = humanTaskConnector.findManageTasks(userId, tenantId, propertyFilters, page);
        model.addAttribute("page", page);
        model.addAttribute("statuslist", dictConnector.findDictInfoListByType("RecordStatus"));
        model.addAttribute("systemlist", dictConnector.findDictInfoListByType("OwnedSystem"));

        return "humantask/workspace-manageQuery";
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

    /**
     * 待领任务.
     */
    @RequestMapping("workspace-groupTasks")
    public String groupTasks(@ModelAttribute Page page,
                             @RequestParam Map<String, Object> parameterMap, Model model) {

        String userId = currentUserHolder.getUserId();
        String tenantId = tenantHolder.getTenantId();

        // page = humanTaskConnector.findGroupTasks(userId, tenantId,page.getPageNo(), page.getPageSize());

        model = GetModelCmd(userId, model);

        Map<String, Object> map = this.convertAlias(parameterMap);

        List<PropertyFilter> propertyFilters = PropertyFilter.buildFromMap(map);
        page = humanTaskConnector.findGroupTasks(userId, tenantId, propertyFilters, page);

        model.addAttribute("page", page);

        return "humantask/workspace-groupTasks";
    }

    /**
     * 已办任务.
     */
    @RequestMapping("workspace-historyTasks")
    public String historyTasks(@ModelAttribute Page page,
                               @RequestParam Map<String, Object> parameterMap, Model model) {

        String userId = currentUserHolder.getUserId();
        String tenantId = tenantHolder.getTenantId();

        // page = humanTaskConnector.findFinishedTasks(userId, tenantId, page.getPageNo(), page.getPageSize());

        model = GetModelCmd(userId, model);

        Map<String, Object> map = this.convertAlias(parameterMap);
        List<PropertyFilter> propertyFilters = PropertyFilter.buildFromMap(map);

        page = humanTaskConnector.findFinishedTasks(userId, tenantId, propertyFilters, page);

        model.addAttribute("page", page);
        model.addAttribute("statuslist", dictConnector.findDictInfoListByType("RecordStatus"));

        return "humantask/workspace-historyTasks";
    }

    /**
     * 代理中的任务.
     */
    @RequestMapping("workspace-delegatedTasks")
    public String delegatedTasks(@ModelAttribute Page page,
                                 @RequestParam Map<String, Object> parameterMap, Model model) {
        String userId = currentUserHolder.getUserId();
        String tenantId = tenantHolder.getTenantId();
        page = humanTaskConnector.findDelegateTasks(userId, tenantId,
                page.getPageNo(), page.getPageSize());
        model.addAttribute("page", page);

        return "humantask/workspace-delegatedTasks";
    }

    /**
     * 领取.
     */
    @RequestMapping("workspace-claimTask")
    public String claimTask(@RequestParam("taskId") Long taskId, RedirectAttributes redirectAttributes) {
        String userId = currentUserHolder.getUserId();
        
        /*List<TaskInfo> taskInfoList=taskInfoManager.findBy("id", taskId);
        if(taskInfoList!=null&&taskInfoList.size()>0){
        	TaskInfo taskInfo=taskInfoList.get(0);
        	if(!StringUtils.isBlank(taskInfo.getAssignee())){
        		messageHelper.addFlashMessage(redirectAttributes, "core.success.publish", "该流程已被认领");
                return "redirect:/humantask/workspace-groupTasks.do";
        	}
        }
        else {
        	messageHelper.addFlashMessage(redirectAttributes, "core.success.publish", "流程不存在");
            return "redirect:/humantask/workspace-groupTasks.do";
		}*/
        
        TaskInfo taskInfo = taskInfoManager.get(taskId);
        if(taskInfo != null){
        	if(!StringUtils.isBlank(taskInfo.getAssignee())){
        		messageHelper.addFlashMessage(redirectAttributes, "core.success.publish", "该流程已被认领");
                return "redirect:/humantask/workspace-groupTasks.do";
        	}
        } else {
        	messageHelper.addFlashMessage(redirectAttributes, "core.success.publish", "流程不存在");
            return "redirect:/humantask/workspace-groupTasks.do";
		}
        
        taskInfo.setAssignee(userId);
        taskInfoManager.save(taskInfo);
        
        try {
			operationService.setClaimPosition(taskId==null?"":taskId.toString(),userId);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        return "redirect:/humantask/workspace-personalTasks.do";
    }

    /**
     * 转发已结流程.
     */
    @RequestMapping("workspace-transferTask")
    public String transferTask(@RequestParam("humanTaskId") String humanTaskId,
                               @RequestParam("assignee") String assignee) {
        String tenantId = tenantHolder.getTenantId();

        // 1. 找到任务
        HumanTaskDTO historyHumanTask = humanTaskConnector
                .findHumanTask(humanTaskId);

        // 2. 创建一个任务，设置为未读，转发状态
        HumanTaskDTO humanTaskDto = humanTaskConnector.createHumanTask();
        humanTaskDto.setProcessInstanceId(historyHumanTask
                .getProcessInstanceId());
        humanTaskDto.setPresentationSubject(historyHumanTask
                .getPresentationSubject());
        humanTaskDto.setAssignee(assignee);
        humanTaskDto.setTenantId(tenantId);
        humanTaskDto.setParentId(historyHumanTask.getId());
        // TODO: 还没有字段
        // humanTaskDto.setCopyStatus("unread");
        humanTaskDto.setCatalog(HumanTaskConstants.CATALOG_COPY);
        humanTaskDto.setAction("unread");
        humanTaskDto.setBusinessKey(historyHumanTask.getBusinessKey());
        humanTaskDto.setProcessDefinitionId(historyHumanTask
                .getProcessDefinitionId());

        try {
            // TODO: 等到流程支持viewFormKey，才能设置。目前做不到
            humanTaskDto.setForm(historyHumanTask.getForm());
            humanTaskDto.setName(historyHumanTask.getName());
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }

        humanTaskConnector.saveHumanTask(humanTaskDto);

        // 3. 把任务分配给对应的人员
        return "redirect:/humantask/workspace-historyTasks.do#";
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

    //导出全部审批
    @RequestMapping("allApproval-export")
    public void allApprovalExport(@ModelAttribute Page page,
                                  @RequestParam Map<String, Object> parameterMap, HttpServletResponse response, HttpServletRequest request)
            throws Exception {
        String userId = currentUserHolder.getUserId();
        String tenantId = tenantHolder.getTenantId();

        Map<String, Object> map = this.convertAlias(parameterMap);
        List<PropertyFilter> propertyFilters = PropertyFilter.buildFromMap(map);
        //TODO sjx 2018-04-19 导出功能扩展
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
        	page = humanTaskConnector.exportAllApprovalBydetail(userId, tenantId, propertyFilters, page,formName);
        }else{
        	page = humanTaskConnector.exportAllApproval(userId, tenantId, propertyFilters, page);
        }
        
        ExportUtil.export(page, response, request, detaliId, formName, "全部审批_");
    }
    //导出管理者查询的列表
    @RequestMapping("manageQuery-export")
    public void manageQueryExport(@ModelAttribute Page page,
    		@RequestParam Map<String, Object> parameterMap, HttpServletResponse response, HttpServletRequest request)
    				throws Exception {
    	String userId = currentUserHolder.getUserId();
    	String tenantId = tenantHolder.getTenantId();
    	
    	Map<String, Object> map = this.convertAlias(parameterMap);
    	List<PropertyFilter> propertyFilters = PropertyFilter.buildFromMap(map);
    	//TODO sjx 2018-04-19 导出功能扩展
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
    		page = humanTaskConnector.exportManageQueryBydetail(userId, tenantId, propertyFilters, page, formName);
    		ExportUtil.export(page, response, request, detaliId, formName, "管理审批_");
    	}else{
    		page = humanTaskConnector.exportManageQuery(userId, tenantId, propertyFilters, page);
    		ExportUtil.exportManage(page, response, request);
    	}
    }

    //导出抄送审批
    @RequestMapping("personalCopyTasks-export")
    public void personalCopyTasksExport(@ModelAttribute Page page,
                                        @RequestParam Map<String, Object> parameterMap, HttpServletResponse response, HttpServletRequest request)
            throws Exception {
        String userId = currentUserHolder.getUserId();
        String tenantId = tenantHolder.getTenantId();

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
        	page = humanTaskConnector.exportPersonalCopyTasksBydetail(userId, tenantId, propertyFilters, page,formName);
        }else{
        	page = humanTaskConnector.exportPersonalCopyTasks(userId, tenantId, propertyFilters, page);
        }
        
        ExportUtil.export(page, response, request, detaliId, formName, "抄送审批_");
    }
    
    //导出经我审批
    @RequestMapping("historyTasks-export")
    public void historyExport(@ModelAttribute Page page,
                              @RequestParam Map<String, Object> parameterMap, HttpServletResponse response, HttpServletRequest request)
            throws Exception {
        String userId = currentUserHolder.getUserId();
        String tenantId = tenantHolder.getTenantId();

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
        	page = humanTaskConnector.exportFinishedTasksBydetail(userId, tenantId, propertyFilters, page, formName);
        }else{
        	page = humanTaskConnector.exportFinishedTasks(userId, tenantId, propertyFilters, page);
        }
        ExportUtil.export(page, response, request, detaliId, formName, "经我审批_");
    }
    //导出定制审批
    @RequestMapping("speicalPeopleApproval-export")
    public void speicalPeopleApprovalExport(@ModelAttribute Page page,
                                            @RequestParam Map<String, Object> parameterMap, HttpServletResponse response, HttpServletRequest request)
            throws Exception {
        String userId = currentUserHolder.getUserId();
        String tenantId = tenantHolder.getTenantId();

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
        	page = humanTaskConnector.exportSpeicalPeopleApprovalBydetail(userId, tenantId, propertyFilters, page,formName);
        }else{
        	page = humanTaskConnector.exportSpeicalPeopleApproval(userId, tenantId, propertyFilters, page);
        }
        ExportUtil.export(page, response, request, detaliId, formName, "定制审批_");
    }


    //导出部门申请
    @RequestMapping("departmentApplication-export")
    public void departmentApplicationExport(@ModelAttribute Page page,
                                            @RequestParam Map<String, Object> parameterMap, HttpServletResponse response, HttpServletRequest request)
            throws Exception {
    	String userId = currentUserHolder.getUserId();
        String tenantId = tenantHolder.getTenantId();
        String checkArea = "0";//0为非分公司职员
        Map<String, Object> map = this.convertAlias(parameterMap);
        List<PropertyFilter> propertyFilters = PropertyFilter.buildFromMap(map);
//        //获取部门id
//        PersonInfo personInfo = personInfoManager.findUniqueBy("id", Long.parseLong(userId));
//        String departmentId = personInfo.getDepartmentCode();
//        //获取分公司人员部门id 
//        PartyDTO partyDTOArea = partyConnector.findAreaById(departmentId);
//        if (partyDTOArea != null) {
//            String hql = "from PartyStruct where partyStructType=1 and childEntity.id=? ";
//            List<PartyStruct> partyStructs = partyStructManager.find(hql, Long.parseLong(userId));
//            departmentId = Long.toString(partyStructs.get(0).getParentEntity().getId());
//            checkArea = "1";
//        }
//        
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
        
        //判断当前登录人是否该[部门/公司]的管理者 ，若是管理者 返回数据，若不是管理者 就没有权限查看数据，返回空
        
        //获取部门id
        String departmentId = "";
        String orgID = "";
      
        /**
         * 获得管理者对应的组织机构.
         */
        
        List<Long> isManager =  orgConnector.getPartyByManageId(userId);
        if(isManager!=null&&isManager.size()>0){
        	for(int i =0;i<isManager.size();i++){
        		orgID = orgID + isManager.get(i).toString() + ",";
        	}
        	orgID = orgID.substring(0, orgID.length()-1);	
        
	        //寻找该 组织机构 下面所有的组织机构
	    	List<String> list = partyResource.getPartyEntityNoPerson(orgID);
	    	List<String> newList = removeDuplicateWithOrder(list);
	    	//遍历结果，将这些组织机构的id放入一个字符串
	    	for(int i =0;i<newList.size();i++){
	    		departmentId= departmentId + newList.get(i)+",";
	    	}
	    	if (StringUtils.isNotBlank(departmentId)) {
	    		departmentId = departmentId.substring(0, departmentId.length()-1);	
	    		departmentId = departmentId + "," + orgID;
	    	} else {
	    		departmentId = orgID;
	    	}
	    	
	    	if (StringUtils.isBlank(departmentId)) {
	    		departmentId = "9999";
	    	}
        }
        
        
        if(detaliId.length() != 0){
        	//含有细分，导出表单数据
            page = humanTaskConnector.exportDepartmentApplicationDetail(userId, tenantId, departmentId, propertyFilters, page,checkArea,formName);
        }else{
        	//不含细分，导出列表数据
            page = humanTaskConnector.exportDepartmentApplication(userId, tenantId, departmentId, propertyFilters, page,checkArea);
        }
        
        ExportUtil.export(page, response, request, detaliId, formName,"申请查询_");
    }

    //导出部门审批
    @RequestMapping("departmentApproval-export")
    public void departmentApprovalExport(@ModelAttribute Page page,
                                         @RequestParam Map<String, Object> parameterMap, HttpServletResponse response, HttpServletRequest request)
            throws Exception {
    	String userId = currentUserHolder.getUserId();
        String tenantId = tenantHolder.getTenantId();
        String checkArea = "0";//0为非分公司职员
        Map<String, Object> map = this.convertAlias(parameterMap);
        List<PropertyFilter> propertyFilters = PropertyFilter.buildFromMap(map);
//        //获取部门id
//        PersonInfo personInfo = personInfoManager.findUniqueBy("id", Long.parseLong(userId));
//        String departmentId = personInfo.getDepartmentCode();
//        //获取分公司人员部门id
//        PartyDTO partyDTOArea = partyConnector.findAreaById(departmentId);
//        if (partyDTOArea != null) {
//            String hql = "from PartyStruct where partyStructType=1 and childEntity.id=? ";
//            List<PartyStruct> partyStructs = partyStructManager.find(hql, Long.parseLong(userId));
//            departmentId = Long.toString(partyStructs.get(0).getParentEntity().getId());
//            checkArea = "1";
//        }
        
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
        
        
        //判断当前登录人是否该[部门/公司]的管理者 ，若是管理者 返回数据，若不是管理者 就没有权限查看数据，返回空
        
        //获取部门id
        String departmentId = "";
        String orgID = "";
      
        /**
         * 获得管理者对应的组织机构.
         */
        
        List<Long> isManager =  orgConnector.getPartyByManageId(userId);
        if(isManager!=null&&isManager.size()>0){
        	for(int i =0;i<isManager.size();i++){
        		orgID = orgID + isManager.get(i).toString() + ",";
        	}
        	orgID = orgID.substring(0, orgID.length()-1);	
        
	        //寻找该 组织机构 下面所有的组织机构
	    	List<String> list = partyResource.getPartyEntityNoPerson(orgID);
	    	List<String> newList = removeDuplicateWithOrder(list);
	    	//遍历结果，将这些组织机构的id放入一个字符串
	    	for(int i =0;i<newList.size();i++){
	    		departmentId= departmentId + newList.get(i)+",";
	    	}
	    	if (StringUtils.isNotBlank(departmentId)) {
	    		departmentId = departmentId.substring(0, departmentId.length()-1);	
	    		departmentId = departmentId + "," + orgID;
	    	} else {
	    		departmentId = orgID;
	    	}
	    	
	    	if (StringUtils.isBlank(departmentId)) {
	    		departmentId = "9999";
	    	}
          }
               
        
        if(detaliId.length() != 0){
        	//含有细分，导出表单数据
            page = humanTaskConnector.exportDepartmentApprovalDetail(userId, tenantId, departmentId, propertyFilters, page,checkArea,formName);
        }else{
        	//不含细分，导出列表数据
            page = humanTaskConnector.exportDepartmentApproval(userId, tenantId, departmentId, propertyFilters, page,checkArea);
        }
        
        ExportUtil.export(page, response, request, detaliId, formName,"审批查询_");
    }

    //导出公司申请
    @RequestMapping("companyApplication-export")
    public void companyApplicationExport(@ModelAttribute Page page,
                                         @RequestParam Map<String, Object> parameterMap, HttpServletResponse response, HttpServletRequest request)
            throws Exception {
    	String userId = currentUserHolder.getUserId();
        String tenantId = tenantHolder.getTenantId();
        String checkArea = "0";//0为非分公司职员
        Map<String, Object> map = this.convertAlias(parameterMap);
        List<PropertyFilter> propertyFilters = PropertyFilter.buildFromMap(map);
        //获取公司id
        PersonInfo personInfo = personInfoManager.findUniqueBy("id", Long.parseLong(userId));
        String companyId = personInfo.getCompanyCode();

        //分公司人员获取公司id
        String departmentId = personInfo.getDepartmentCode();
        PartyDTO partyDTOArea = partyConnector.findAreaById(departmentId);
        if (partyDTOArea != null) {
            companyId = personInfo.getDepartmentCode();
            checkArea = "1";
        }
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
            page = humanTaskConnector.exportCompanyApplicationDetail(userId, tenantId, companyId, propertyFilters, page,checkArea,formName);
        }else{
        	//不含细分，导出列表数据
            page = humanTaskConnector.exportCompanyApplication(userId, tenantId, companyId, propertyFilters, page,checkArea);
        }
        
        ExportUtil.export(page, response, request, detaliId, formName,"公司申请_");
    }

    //导出公司审批
    @RequestMapping("companyApproval-export")
    public void companyApprovalExport(@ModelAttribute Page page,
                                      @RequestParam Map<String, Object> parameterMap, HttpServletResponse response, HttpServletRequest request)
            throws Exception {
    	String userId = currentUserHolder.getUserId();
        String tenantId = tenantHolder.getTenantId();
        String checkArea = "0";//0为非分公司职员
        Map<String, Object> map = this.convertAlias(parameterMap);
        List<PropertyFilter> propertyFilters = PropertyFilter.buildFromMap(map);
        //获取公司id
        PersonInfo personInfo = personInfoManager.findUniqueBy("id", Long.parseLong(userId));
        String companyId = personInfo.getCompanyCode();
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
            page = humanTaskConnector.exportCompanyApprovalDetail(userId, tenantId, companyId, propertyFilters, page,checkArea,formName);
        }else{
        	//不含细分，导出列表数据
            page = humanTaskConnector.exportCompanyApproval(userId, tenantId, companyId, propertyFilters, page,checkArea);
        }
        
        ExportUtil.export(page, response, request, detaliId, formName,"公司审批_");
        
    }


    // ~ ======================================================================
    @Resource
    public void setTaskInfoManager(TaskInfoManager taskInfoManager) {
        this.taskInfoManager = taskInfoManager;
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
    public void setCurrentUserHolder(CurrentUserHolder currentUserHolder) {
        this.currentUserHolder = currentUserHolder;
    }

    @Resource
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Resource
    public void setHumanTaskConnector(HumanTaskConnector humanTaskConnector) {
        this.humanTaskConnector = humanTaskConnector;
    }

    @Resource
    public void setTenantHolder(TenantHolder tenantHolder) {
        this.tenantHolder = tenantHolder;
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
    public void setBusinessDetailManager(BusinessDetailManager businessDetailManager) {
    	this.businessDetailManager = businessDetailManager;
    }

    @Resource
    public void setPartyConnector(PartyConnector partyConnector) {
        this.partyConnector = partyConnector;
    }

    @Resource
    public void setDictConnectorImpl(DictConnectorImpl dictConnectorImpl) {
        this.dictConnectorImpl = dictConnectorImpl;
    }

    @Resource
    public void setPartyEntityManager(PartyEntityManager partyEntityManager) {
        this.partyEntityManager = partyEntityManager;
    }

    @Resource
    public void setPartyStructManager(PartyStructManager partyStructManager) {
        this.partyStructManager = partyStructManager;
    }

    @Resource
    public void setPersonInfoManager(PersonInfoManager personInfoManager) {
        this.personInfoManager = personInfoManager;
    }

    /*@Resource
    public void setDictConnectorImpl(DictConnector dictConnector) {
        this.dictConnector = dictConnector;
    }*/
    
    @Resource
    public void setHumanTaskConnectorImpl(HumanTaskConnectorImpl humanTaskConnectorImpl) {
    	this.humanTaskConnectorImpl = humanTaskConnectorImpl;
    }

    @Resource
	public void setSignInfo(SignInfo signInfo) {
		this.signInfo = signInfo;
	}
    
    @Resource
    public void setDictConnector(DictConnector dictConnector) {
        this.dictConnector = dictConnector;
    }
    
    @Resource
    public void setPartyStructTypeManager(
            PartyStructTypeManager partyStructTypeManager) {
        this.partyStructTypeManager = partyStructTypeManager;
    }


	@Resource
	public void setPartyResource(PartyResource partyResource) {
		this.partyResource = partyResource;
	}
    
    @RequestMapping("queryTaskCount")
    @ResponseBody
    private String queryTaskCount(){
    	Map<String, String> map = new HashMap<String, String>();
    	List<String> list = new ArrayList<String>();
    	list.add("待办审批");
    	list.add("待领审批");
    	list.add("抄送审批");
    	list.add("未结流程");
    	for (String title : list) {
    		long queryTaskCount = humanTaskConnector.queryTaskCount(title);
    		if("待办审批".equals(title)){
    			map.put("personalTasks", String.valueOf(queryTaskCount));
    		}else if("待领审批".equals(title)){
    			map.put("groupTasks", String.valueOf(queryTaskCount));
    		}else if("抄送审批".equals(title)){
    			map.put("personalCopyTasks", String.valueOf(queryTaskCount));
    		}else if("未结流程".equals(title)){
    			map.put("listRunningProcessInstances", String.valueOf(queryTaskCount));
    		}
    		
    		
		}
    	
    	return JSONObject.toJSONString(map);
    }
    
}
