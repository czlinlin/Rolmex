package com.mossle.bpm.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.mossle.api.keyvalue.KeyValueConnector;
import com.mossle.api.tenant.TenantHolder;
import com.mossle.bpm.persistence.domain.BpmCategory;
import com.mossle.bpm.persistence.domain.BpmConfBase;
import com.mossle.bpm.persistence.domain.BpmConfNode;
import com.mossle.bpm.persistence.domain.BpmConfUser;
import com.mossle.bpm.persistence.domain.BpmProcess;
import com.mossle.bpm.persistence.manager.BpmCategoryManager;
import com.mossle.bpm.persistence.manager.BpmConfBaseManager;
import com.mossle.bpm.persistence.manager.BpmConfUserManager;
import com.mossle.bpm.persistence.manager.BpmProcessManager;
import com.mossle.bpm.persistence.manager.BpmTaskDefManager;
import com.mossle.core.auth.CurrentUserHolder;
import com.mossle.core.export.Exportor;
import com.mossle.core.export.TableModel;
import com.mossle.core.mapper.BeanMapper;
import com.mossle.core.page.Page;
import com.mossle.core.query.PropertyFilter;
import com.mossle.core.spring.MessageHelper;
import com.mossle.common.utils.StringUtils;
import com.mossle.util.StringUtil;

import org.activiti.engine.ProcessEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("bpm")
public class BpmProcessController {
    private BpmProcessManager bpmProcessManager;
    private BpmCategoryManager bpmCategoryManager;
    private BpmTaskDefManager bpmTaskDefManager;
    private BpmConfBaseManager bpmConfBaseManager;
    private Exportor exportor;
    private BeanMapper beanMapper = new BeanMapper();
    private ProcessEngine processEngine;
    private MessageHelper messageHelper;
    private TenantHolder tenantHolder;
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private BpmConfUserManager bpmConfUserManager;
    @Autowired
    private CurrentUserHolder currentUserHolder;
    @Autowired
    private KeyValueConnector keyValueConnector;
    
    @RequestMapping("bpm-process-list")
    public String list(@ModelAttribute Page page,
            @RequestParam Map<String, Object> parameterMap, Model model) {
        String tenantId = tenantHolder.getTenantId();
        page.setDefaultOrder("priority", page.ASC);
        //获取当前登陆人 , 判断是否显示别名统计  add by ckx 2018/10/15
        String username = currentUserHolder.getUsername();
        if("dwadmin".equals(username)){
        	model.addAttribute("isShow", "1");//显示
        }
        
        
        String resultSql = "SELECT DISTINCT bp.* FROM bpm_process bp LEFT JOIN bpm_conf_node bcn on bp.CONF_BASE_ID = bcn.conf_base_id "
        		+ "LEFT JOIN bpm_conf_user bcu ON bcn.id = bcu.NODE_ID where bp.tenant_id = '"+tenantId+"' ";
        String countSql = "SELECT count(DISTINCT bp.id) as count FROM bpm_process bp LEFT JOIN bpm_conf_node bcn on bp.CONF_BASE_ID = bcn.conf_base_id "
        		+ "LEFT JOIN bpm_conf_user bcu ON bcn.id = bcu.NODE_ID where bp.tenant_id = '"+tenantId+"' ";
       // page = bpmProcessManager.pagedQuery(page, propertyFilters);
        String branchSql = "";
        Object name = parameterMap.get("filter_LIKES_name");
        Object postId = parameterMap.get("filter_LIKES_postId");
        Object bName = parameterMap.get("filter_LIKES_byName");
        if(null != name && !"".equals(name) && !"null".equals(name)){
        	branchSql += " and bp.name like '%"+name+"%' ";
        }
        if(null != postId && !"".equals(postId) && !"null".equals(postId)){
        	branchSql += " and bcu.value = '岗位:"+postId+"' ";
        }
        if(null != bName && !"".equals(bName) && !"null".equals(bName)){
        	branchSql += " and bp.byname like '%"+bName+"%' ";
        }
        String orderSql = " order by bp.priority asc limit "+page.getStart() +" , " + page.getPageSize();
        List<Map<String,Object>> resultList = null;
        try {
			resultList = jdbcTemplate.queryForList(resultSql+branchSql+orderSql);
		} catch (DataAccessException e) {
		}
        long totalCount = 0 ;
        ArrayList<BpmProcess> bpmProcessList = new ArrayList<BpmProcess>();
        if(null != resultList && resultList.size() > 0){
        	for (Map<String, Object> map : resultList) {
        		String id = StringUtil.toString(map.get("id"));
        		String nameS = StringUtil.toString(map.get("name"));
        		String categoryId = StringUtil.toString(map.get("category_id"));
        		String priority = StringUtil.toString(map.get("priority"));
        		String descn = StringUtil.toString(map.get("descn"));
        		String useTaskConf = StringUtil.toString(map.get("use_task_conf"));
        		String code = StringUtil.toString(map.get("code"));
        		String confBaseId = StringUtil.toString(map.get("conf_base_id"));
        		String tenantIdS = StringUtil.toString(map.get("tenant_id"));
        		String showFlag = StringUtil.toString(map.get("show_flag"));
        		String byName = StringUtil.toString(map.get("byname"));
        		
				BpmConfBase bpmConfBase = new BpmConfBase(Long.parseLong(confBaseId));
				BpmCategory bpmCategory = new BpmCategory();
				/*String baseConfSql = "select * from bpm_conf_base where id";
				jdbcTemplate.queryForObject(sql, requiredType);*/
				Map<String, Object> categoryMap = null;
				if(null != categoryId && !"".equals(categoryId) && !"null".equals(categoryId)){
					try {
						categoryMap = jdbcTemplate.queryForMap("select * from bpm_category where id = '"+categoryId+"'");
					} catch (DataAccessException e) {
					}
				}
				if(null != categoryMap){
					
					bpmCategory.setName(StringUtil.toString(categoryMap.get("name")));
					bpmCategory.setId(Long.parseLong(confBaseId));
					bpmCategory.setTenantId(StringUtil.toString(categoryMap.get("tenant_id")));
					String str = StringUtil.toString(categoryMap.get("priority"));
					if(!"".equals(str)){
						bpmCategory.setPriority(Integer.parseInt(str));
					}
				}
				BpmProcess bpmProcess = new BpmProcess();
				bpmProcess.setId(Long.parseLong(id));
				bpmProcess.setName(nameS);
				if (StringUtils.isNotBlank(priority)) {
					bpmProcess.setPriority(Integer.parseInt(priority));
				}
				bpmProcess.setDescn(descn);
				bpmProcess.setUseTaskConf(Integer.parseInt(useTaskConf));
				bpmProcess.setCode(code);
				bpmProcess.setBpmConfBase(bpmConfBase);
				bpmProcess.setTenantId(tenantIdS);
				bpmProcess.setShowFlag(Integer.parseInt(showFlag));
				bpmProcess.setByName(byName);
				bpmProcess.setBpmCategory(bpmCategory);
				bpmProcessList.add(bpmProcess);
				
			}
        	
        }
        //查询次数
        Map<String, Object> countMap = jdbcTemplate.queryForMap(countSql+branchSql);
        
        if(null != countMap){
        	totalCount = Long.parseLong(StringUtil.toString(countMap.get("count")));
        }
        
        page.setTotalCount(totalCount);
        page.setResult(bpmProcessList);
        model.addAttribute("page", page);

        return "bpm/bpm-process-list";
    }
    /**
     * 别名列表的展示 ckx
     * @param page
     * @param parameterMap
     * @param model
     * @return
     */
    @RequestMapping("bpm-byName-list")
    public String byNameList(@ModelAttribute Page page,
            @RequestParam Map<String, Object> parameterMap,
            Model model){
    	String tenantId = tenantHolder.getTenantId();
    	//结果集sql
    	String resultSql = "select bp.byname as byName,count(*) as count from bpm_process bp where bp.tenant_id = '"+tenantId+"' ";
    	//总条数sql
    	String countSql = "select DISTINCT bp.BYNAME from bpm_process bp where bp.tenant_id = '"+tenantId+"' ";
    	String branchSql = "";
    	Object byName = parameterMap.get("filter_LIKES_byName");
    	if(null != byName && !"".equals(byName) && !"null".equals(byName)){
        	branchSql += " and bp.byname like '%"+byName+"%' ";
        }
    	String orderSql = " order by bp.priority asc limit "+page.getStart() +" , " + page.getPageSize();
    	String groupSql = " GROUP BY bp.BYNAME ";
    	//结果集
    	List<Map<String,Object>> byNameList = jdbcTemplate.queryForList(resultSql+branchSql+groupSql+orderSql);
    	List<Map<String,Object>> countList = jdbcTemplate.queryForList(countSql+branchSql);
    	int totalCount = countList.size();
    	page.setTotalCount(totalCount);
    	page.setResult(byNameList);
    	model.addAttribute("page", page);
    	return "bpm/bpm-byName-list";
    }
    
    
    @RequestMapping("bpm-process-input")
    public String input(@RequestParam(value = "id", required = false) Long id,
            Model model) {
    	
    	BpmProcess bpmProcess=null;
        if (id != null) {
            bpmProcess = bpmProcessManager.get(id);
            model.addAttribute("model", bpmProcess);
        }

        List<BpmCategory> bpmCategories = bpmCategoryManager.getAll();
        List<BpmConfBase> bpmConfBases = new ArrayList<BpmConfBase>();
        
        String selectSql = "SELECT b.ID,b.PROCESS_DEFINITION_ID,b.PROCESS_DEFINITION_KEY,b.PROCESS_DEFINITION_VERSION,"
        		+ "if(p.NAME_ is null,b.PROCESS_DEFINITION_ID,p.NAME_) as BPM_NAME"
        		+ " FROM bpm_conf_base b"
        		+ " inner join ACT_RE_PROCDEF p on b.process_definition_id = p.ID_"
        		+ " where p.SUSPENSION_STATE_ = 1"
        		+ " order by b.PROCESS_DEFINITION_ID";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(selectSql);
        for (Map<String, Object> map : list) {
        	BpmConfBase bpmConfBase = new BpmConfBase();
        	
        	bpmConfBase.setId(Long.parseLong(StringUtils.convertString(map.get("ID"))));
        	bpmConfBase.setProcessDefinitionId(StringUtils.convertString(map.get("BPM_NAME")));
        	bpmConfBase.setProcessDefinitionKey(StringUtils.convertString(map.get("PROCESS_DEFINITION_KEY")));
        	bpmConfBase.setProcessDefinitionVersion(Integer.parseInt(StringUtils.convertString(map.get("PROCESS_DEFINITION_VERSION"))));
        	
        	bpmConfBases.add(bpmConfBase);
        }
        //ckx 2019/2/18  不再获取流程标题    流程标题设置在细分编辑
        /*String bpmProcessTitle="";
        if(bpmProcess!=null){
        	List<Map<String,Object>> mapBpmProcessAttrList=jdbcTemplate.queryForList(String.format("SELECT * FROM bpm_process_attr WHERE bpmProcessId=%s", bpmProcess.getId()));
        	if(mapBpmProcessAttrList!=null&&mapBpmProcessAttrList.size()>0){
        		Map<String,Object> mapBpmAttr=mapBpmProcessAttrList.get(0);
        		bpmProcessTitle=mapBpmAttr.get("bpmProcessTitle")==null?"":mapBpmAttr.get("bpmProcessTitle").toString();
        	}
        }*/
        
        //model.addAttribute("bpmProcessTitle", bpmProcessTitle==null?"":bpmProcessTitle);
        model.addAttribute("bpmCategories", bpmCategories);
        model.addAttribute("bpmConfBases", bpmConfBases);

        return "bpm/bpm-process-input";
    }

    @RequestMapping("bpm-process-save")
    public String save(@ModelAttribute BpmProcess bpmProcess,
            @RequestParam("bpmCategoryId") Long bpmCategoryId,
            @RequestParam("bpmConfBaseId") Long bpmConfBaseId,
            /*@RequestParam("bpmProcessTitle") String bpmProcessTitle,*/
            RedirectAttributes redirectAttributes) {
        BpmProcess dest = null;
        Long id = bpmProcess.getId();

        if (id != null) {
            dest = bpmProcessManager.get(id);
            beanMapper.copy(bpmProcess, dest);
        } else {
            dest = bpmProcess;

            String tenantId = tenantHolder.getTenantId();
            dest.setTenantId(tenantId);
        }

        dest.setBpmCategory(bpmCategoryManager.get(bpmCategoryId));
        dest.setBpmConfBase(bpmConfBaseManager.get(bpmConfBaseId));
        if (dest.getPriority() == null) {
        	dest.setPriority(0);
        }
        bpmProcessManager.save(dest);
        
        //ckx 2019/2/18  不再获取流程标题    流程标题设置在细分编辑
        //keyValueConnector.updateBySql(String.format("delete from bpm_process_attr where bpmProcessId=%s", dest.getId()));
        //keyValueConnector.updateBySql(String.format("insert into bpm_process_attr(bpmProcessId,bpmProcessTitle) values(%s,'%s')", dest.getId(),bpmProcessTitle));
        
        messageHelper.addFlashMessage(redirectAttributes, "core.success.save",
                "保存成功");

        return "redirect:/bpm/bpm-process-list.do";
    }

    @RequestMapping("bpm-process-remove")
    public String remove(@RequestParam("selectedItem") List<Long> selectedItem,
            RedirectAttributes redirectAttributes) {
        List<BpmProcess> bpmCategories = bpmProcessManager
                .findByIds(selectedItem);
        bpmProcessManager.removeAll(bpmCategories);
        messageHelper.addFlashMessage(redirectAttributes,
                "core.success.delete", "删除成功");

        return "redirect:/bpm/bpm-process-list.do";
    }

    @RequestMapping("bpm-process-export")
    public void export(@ModelAttribute Page page,
            @RequestParam Map<String, Object> parameterMap,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        List<PropertyFilter> propertyFilters = PropertyFilter
                .buildFromMap(parameterMap);
        page = bpmProcessManager.pagedQuery(page, propertyFilters);

        List<BpmProcess> bpmCategories = (List<BpmProcess>) page.getResult();
        TableModel tableModel = new TableModel();
        tableModel.setName("bpm-process");
        tableModel.addHeaders("id", "name");
        tableModel.setData(bpmCategories);
        exportor.export(request, response, tableModel);
    }

    // ~ ======================================================================
    @Resource
    public void setBpmProcessManager(BpmProcessManager bpmProcessManager) {
        this.bpmProcessManager = bpmProcessManager;
    }

    @Resource
    public void setBpmCategoryManager(BpmCategoryManager bpmCategoryManager) {
        this.bpmCategoryManager = bpmCategoryManager;
    }

    @Resource
    public void setBpmTaskDefManager(BpmTaskDefManager bpmTaskDefManager) {
        this.bpmTaskDefManager = bpmTaskDefManager;
    }

    @Resource
    public void setBpmConfBaseManager(BpmConfBaseManager bpmConfBaseManager) {
        this.bpmConfBaseManager = bpmConfBaseManager;
    }

    @Resource
    public void setProcessEngine(ProcessEngine processEngine) {
        this.processEngine = processEngine;
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
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
}
