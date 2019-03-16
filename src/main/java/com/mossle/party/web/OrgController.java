package com.mossle.party.web;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import ch.qos.logback.core.joran.conditional.IfAction;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Joiner;
import com.mossle.api.party.PartyConnector;
import com.mossle.api.tenant.TenantHolder;
import com.mossle.api.user.RosterLogDTO;
import com.mossle.api.user.UserConnector;
import com.mossle.bpm.persistence.domain.BpmProcess;
import com.mossle.bpm.service.WorkSpaceService;
import com.mossle.core.annotation.Log;
import com.mossle.core.auth.CurrentUserHolder;
import com.mossle.core.mapper.BeanMapper;
import com.mossle.core.mapper.JsonMapper;
import com.mossle.core.page.Page;
import com.mossle.core.query.PropertyFilter;
import com.mossle.core.query.PropertyFilterUtils;
import com.mossle.core.spring.MessageHelper;
import com.mossle.dict.persistence.domain.DictInfo;
import com.mossle.dict.persistence.manager.DictInfoManager;
import com.mossle.operation.persistence.domain.CustomPre;
import com.mossle.operation.service.CustomService;
import com.mossle.operation.service.OperationService;
import com.mossle.party.PartyConstants;
import com.mossle.party.persistence.domain.AttendanceEntity;
import com.mossle.party.persistence.domain.OrgLogDTO;
import com.mossle.party.persistence.domain.OrgLogEntity;
import com.mossle.party.persistence.domain.PartyEntity;
import com.mossle.party.persistence.domain.PartyStruct;
import com.mossle.party.persistence.domain.PartyStructType;
import com.mossle.party.persistence.domain.PartyType;
import com.mossle.party.persistence.manager.AttendanceEntityManager;
import com.mossle.party.persistence.manager.OrgLogManager;
import com.mossle.party.persistence.manager.PartyEntityManager;
import com.mossle.party.persistence.manager.PartyStructManager;
import com.mossle.party.persistence.manager.PartyStructTypeManager;
import com.mossle.party.persistence.manager.PartyTypeManager;
import com.mossle.party.service.OrgService;
import com.mossle.party.service.PartyService;
import com.mossle.user.PersonInfoConstants;
import com.mossle.user.persistence.domain.AccountInfo;
import com.mossle.user.persistence.domain.RosterLog;
import com.mossle.user.persistence.domain.UpdatePerson;
import com.mossle.user.persistence.manager.AccountInfoManager;
import com.mossle.user.persistence.manager.RosterLogManager;
import com.mossle.user.persistence.manager.UpdatePersonManager;
import com.mossle.user.persistence.domain.PersonInfo;
import com.mossle.user.persistence.manager.PersonInfoManager;
import com.mossle.user.service.PersonInfoService;
import com.mossle.user.service.UserService;
import com.mossle.user.web.PersonInfoController;
import com.mossle.util.DateUtil;
import com.mossle.util.ExportUtil;
import com.mossle.util.StringUtil;

import org.activiti.engine.impl.cmd.AddCommentCmd;
import org.apache.commons.collections.comparators.ComparatorChain;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.wsdl.http.UrlEncoded;
import org.apache.xmlbeans.impl.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * party.
 */
@Controller
@RequestMapping("party")
public class OrgController {
	
    private static Logger logger = LoggerFactory.getLogger(OrgController.class);
    // public static final int TYPE_ORG = 0;
    // public static final int TYPE_USER = 1;
    // public static final int TYPE_POSITION = 2;
    private PartyEntityManager partyEntityManager;
    private PartyTypeManager partyTypeManager;
    private PartyStructManager partyStructManager;
    private PartyStructTypeManager partyStructTypeManager;
    private UserConnector userConnector;
    private CurrentUserHolder currentUserHolder;
	private PartyService partyService;
	private OrgService orgService;
    private BeanMapper beanMapper = new BeanMapper();
    private TenantHolder tenantHolder;
    private MessageHelper messageHelper;
    private AccountInfoManager accountInfoManager;
    private JsonMapper jsonMapper = new JsonMapper();
    private PersonInfoManager personInfoManager;
    private JdbcTemplate jdbcTemplate;
    private CustomService customService;
    private OperationService operationService;
    private DictInfoManager dictInfoManager;
    private UserService userService;
    private UpdatePersonManager updatePersonManager;
    private RosterLogManager rosterLogManager;
    private PersonInfoService personInfoService;
    private AttendanceEntityManager attendanceEntityManager;
    private ExportUtil exportUtil;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    private Long globalPartyEntityId;
    private Map<String,Object> userMap;
    private List<Map<String,Object>> userList;
    @Resource
    private PersonInfoController personInfoController;
    @Resource
    private WorkSpaceService workSpaceService;
    private OrgLogManager orgLogManager;
    @Autowired
    private PartyConnector partyConnector;
    
    /**
     * 初始化组织机构的维度，包括对应维度下的根节点.
     */
    public PartyEntity init(Model model, Long partyStructTypeId,
            Long partyEntityId) {
        String tenantId = tenantHolder.getTenantId();

        // 维度，比如行政组织
        String hqlPartyStructType = "from PartyStructType where tenantId=? and display='true' order by priority";
        List<PartyStructType> partyStructTypes = partyStructTypeManager.find(
                hqlPartyStructType, tenantId);
        PartyStructType partyStructType = null;

        if (partyStructTypeId != null) {
            partyStructType = partyStructTypeManager.get(partyStructTypeId);
        } else {
            if (!partyStructTypes.isEmpty()) {
                // 如果没有指定维度，就使用第一个维度当做默认维度
                partyStructType = partyStructTypes.get(0);
                partyStructTypeId = partyStructType.getId();
            }
        }

        if (partyEntityId == null) {
            // 如果没有指定组织，就返回顶级组织
            List<PartyEntity> partyEntities = partyService
                    .getTopPartyEntities(partyStructTypeId);

            if (!partyEntities.isEmpty()) {
                partyEntityId = partyEntities.get(0).getId();
            }
        }

        model.addAttribute("partyStructTypes", partyStructTypes);
        model.addAttribute("partyStructType", partyStructType);
        model.addAttribute("partyStructTypeId", partyStructTypeId);
        model.addAttribute("partyEntityId", partyEntityId);

        if (partyEntityId == null) {
            return null;
        }

        return partyEntityManager.get(partyEntityId);
    }

    /**
     * 显示下级列表.
     */
    @RequestMapping("org-list")
    @Log(desc = "人力资源", action = "查看", operationDesc = "人力资源-组织机构-查看")
    public String list(
            Model model,
            @RequestParam(value = "partyStructTypeId", required = false) Long partyStructTypeId,
            @RequestParam(value = "partyEntityId", required = false) Long partyEntityId,
            @RequestParam(value = "name", required = false) String name,
            @ModelAttribute Page page) {
    	model.addAttribute("isAdminRole",userService.getIsAdminRole(currentUserHolder.getUserId()));
    	return "party/org-list";
    }
    /**
     * 显示下级列表.
     */
    @RequestMapping("org-list-i")
    @Log(desc = "人力资源", action = "查看", operationDesc = "人力资源-组织机构-查看")
    public String orgList(
            Model model,
            @RequestParam(value = "partyStructTypeId", required = false) Long partyStructTypeId,
            @RequestParam(value = "partyEntityId", required = false) Long partyEntityId,
            @RequestParam(value = "name", required = false) String name,
            @ModelAttribute Page page) {
        PartyEntity partyEntity = this.init(model, partyStructTypeId,
                partyEntityId);
        
        Long accountId = Long.parseLong(currentUserHolder.getUserId());
        
        // 只有超级管理员才可以点击树的根节点   zyl 2017-07-12  
        if (partyEntityId != null) {
        	/*if (!accountId.equals(PartyConstants.ADMIN_USER_ID) && partyEntityId.equals(PartyConstants.ROOT_PARTY_TREE_ID)) {
        		model.addAttribute("viewManage", false);  // 是否显示管理者按钮
        		return "party/org-list-i";
        	}*/
        	if (accountId.equals(PartyConstants.ADMIN_USER_ID)) {
        		model.addAttribute("adminId", true);
        	}
        }
        
        if (partyEntity != null) {
            // 返回所有下级，包含组织，岗位，人员
            String hql = "from PartyStruct where childEntity.delFlag = '0' and parentEntity=? and partyStructType.id=?";

            if (name != null) {
                hql += (" and childEntity.name like '%" + name + "%'");
            }
            
            if (partyEntity.getPartyType().getType() == PartyConstants.TYPE_POSITION) {
            	partyStructTypeId = PartyConstants.PARTY_STRUCT_TYPE_POSITION_USER;
            }
            
            page.setOrderBy("childEntity.partyType.id");
            // 如果没有选中partyEntityId，就啥也不显示
            page = partyStructTypeManager.pagedQuery(hql, page.getPageNo(),
                    page.getPageSize(), partyEntity, partyStructTypeId);
            model.addAttribute("page", page);
            
            /*
             * 现行政组织规则:
             *公司：允许新建公司、新建部门、新建小组、新建岗位，新建大区
             *大区：允许新建公司、新建部门、新建小组、新建岗位
             *部门：允许新建小组、新建岗位
             *小组：允许新建小组、新建岗位
             *岗位：关联人员
             *管理者：允许在公司、部门、小组，大区下新建
             ***/
            if(partyEntity!=null){
            	//是否显示管理者
                boolean isViewManage=false;// 是否显示管理者按钮
                List<Long> longPartyTypes=new ArrayList<Long>();
                longPartyTypes.add(PartyConstants.PARTY_TYPE_COMPANY);
                longPartyTypes.add(PartyConstants.PARTY_TYPE_DEPARTMENT);
                longPartyTypes.add(PartyConstants.PARTY_TYPE_GROUP);
                longPartyTypes.add(PartyConstants.PARTY_TYPE_AREA);
                if((longPartyTypes).contains(partyEntity.getPartyType().getId()))
                	isViewManage=true;
                model.addAttribute("viewManage", isViewManage);
                
                //判断这个组织下可以创建哪些下级
                //List<PartyType> childUser = new ArrayList<PartyType>();
                List<PartyType> childTypes = partyTypeManager
	                    .find("select childType from PartyType childType join childType.parentStructRules parentStructRule where parentStructRule.parentType=? order by parentStructRule.childType.id asc",
	                            partyEntity.getPartyType());
                /*Collections.sort(childTypes,new Comparator<PartType>(){
                	
                });*/
                model.addAttribute("childTypes", childTypes);
            }

            // 判断这个组织下可以创建哪些下级
            // TODO: 应该判断维度
            /*if (partyEntityId != null) {
	            List<PartyType> childTypes = partyTypeManager
	                    .find("select childType from PartyType childType join childType.parentStructRules parentStructRule where parentStructRule.parentType=?",
	                            partyEntity.getPartyType());
	            
	            if (accountId.equals(PartyConstants.ADMIN_USER_ID)) {  // 超级管理员
	            	if (partyEntity.getLevel() == 2) { // 二级机构，只允许添加人员
		            	List<PartyType> childUser = new ArrayList<PartyType>();
		            	for(PartyType type : childTypes) {
		            		if (type.getId() == PartyConstants.TYPE_USER) {
		            			childUser.add(type);
		            			break;
		            		}
		            	}
		            	model.addAttribute("childTypes", childUser);
		            	model.addAttribute("viewManage", false);   // 是否显示管理者按钮
	            	} else if (partyEntity.getLevel() == 1) { // 一级机构，可添加任何类型
		            	model.addAttribute("childTypes", childTypes);
		            	if (partyEntity.getPartyType().getType() == PartyConstants.TYPE_POSITION) {
			            	model.addAttribute("viewManage", false);   // 是否显示管理者按钮
			            } else {
			            	model.addAttribute("viewManage", true);   // 是否显示管理者按钮
			            }
	            	} else {// 其他层级   无任何按钮
	            		model.addAttribute("viewManage", false);   // 是否显示管理者按钮
	            	}
	            } else {
	            	model.addAttribute("childTypes", childTypes);
	            	if (partyEntity.getPartyType().getType() == PartyConstants.TYPE_POSITION) {
		            	model.addAttribute("viewManage", false);   // 是否显示管理者按钮
		            } else {
		            	model.addAttribute("viewManage", true);   // 是否显示管理者按钮
		            }
	            }
	            
            	
            } else {
            	model.addAttribute("viewManage", false);   // 是否显示管理者按钮
            }*/
            
            model.addAttribute("level", partyEntity.getLevel());   // 层级
            model.addAttribute("partyId", partyEntity.getId());   // ID
            model.addAttribute("partyType", partyEntity.getPartyType().getType());   // 机构类型
            model.addAttribute("partyTypeId", partyEntity.getPartyType().getId());   // 机构类型
        }
        
        return "party/org-list-i";
    }
    /**
     * 考勤列表
     * @return
     * 
     */
    @RequestMapping("attendance-list-i")
    public String attendanceList( Model model,String year,String month,String name,
            @RequestParam(value = "partyStructTypeId", required = false) Long partyStructTypeId,
            @RequestParam(value = "partyEntityId", required = false) Long partyEntityId,
            @ModelAttribute Page page){
    	
    	Map<String,Object> result = new HashMap<String,Object>();
    	Date date = new Date();
    	Calendar a = Calendar.getInstance();
    	int s = a.MONTH;
	    a.set(Calendar.DATE, 1);//把日期设置为当月第一天  

	    //a.roll(Calendar.DATE, -1);//日期回滚一天，也就是最后一天  
	    //int maxDate = a.get(Calendar.DATE);
    	if(year == null || year == ""){
    		year = String.valueOf(a.get(Calendar.YEAR));
    	}
    	if(month == null || month == ""){
    		int current  = date.getMonth()+1;
    		month = String.valueOf(current);//默认取当月
    		if(Integer.parseInt(month)<10){
    			month = String.valueOf("0"+month);
    		}
    	}else{
    		if(Integer.parseInt(month)<10){
    			month = String.valueOf("0"+month);
    		}
    	}
    	
    	Calendar nextCalendar = Calendar.getInstance();
    	
    	int intYear=Integer.parseInt(year);
 	    int intMonth=Integer.parseInt(month);
 	    nextCalendar.set(intYear, intMonth, 1);
 	    nextCalendar.add(Calendar.DATE, -1);
 	    // 获取日
 	    int maxDate = nextCalendar.get(Calendar.DAY_OF_MONTH);
 	    
 	    model.addAttribute("year", year);
 	    model.addAttribute("month", month);
	    model.addAttribute("maxDate", maxDate);
	    //ckx
	    ArrayList<Map<String, Object>> dateList = new ArrayList<Map<String,Object>>();
	    for (int i = 1; i <= maxDate; i++) {
	    	Map<String, Object> map = new HashMap<String, Object>();
	    	map.put("day", i);
	    	String dateStr = DateUtil.formatDateStrToStr(year+"-"+month+"-"+i, "yyyy-MM-dd");
        	//查询当前人员班次时间
        	String weekCSStart = DateUtil.getWeekCS(dateStr);
        	//String attendanceStart = DateUtil.getAttendance(weekCSStart);
        	if("星期六".equals(weekCSStart) || "星期日".equals(weekCSStart)){
        		map.put("isWeekend", "1");
        	}else{
        		map.put("isWeekend", "0");
        	}
        	map.put("week", weekCSStart);
        	dateList.add(map);
	    }
	    model.addAttribute("dateList", dateList);
	    
 	    if(partyEntityId==null){
 	    	model.addAttribute("partyEntityId", 0);
 	    	return "party/org-attendance-list-i";
 	    }
 	    else {
 	    	model.addAttribute("partyEntityId", partyEntityId);
		}
 	    	
    	
    	//先去获取别名功能是否开启
    	String openOtherNameStatus = userService.getOpenOtherNameStatus();
    	model.addAttribute("openOtherNameStatus",openOtherNameStatus);
    	result = orgService.queryAttendance(partyEntityId,year,month,name,openOtherNameStatus);
    	List<Map<String,Object>> userList = (List<Map<String, Object>>) result.get("userList");
    	List<Map<String,Object>> attList = (List<Map<String, Object>>) result.get("attList");
    	List<Map<String,Object>> leaveList = (List<Map<String, Object>>) result.get("leaveList");
    	JSONArray array= JSONArray.parseArray(JSON.toJSONString(attList));
    	JSONArray leaveArray= JSONArray.parseArray(JSON.toJSONString(leaveList));
	    model.addAttribute("attListStr",array);
	    model.addAttribute("leaveListStr",leaveArray);
    	model.addAttribute("userList", userList);
    	
    	return "party/org-attendance-list-i";
    }

    /**
     * 考勤统计
     * 
     **/
    @RequestMapping("attendance-statistics-list-i")
    public String attendanceStatisticsList( Model model,
    		String beginDate,
    		String endDate,
            @RequestParam(value = "partyStructTypeId", required = false) Long partyStructTypeId,
            @RequestParam(value = "partyEntityId", required = false) Long partyEntityId,
            @RequestParam(value = "name", required = false) String name,
            RedirectAttributes redirectAttributes){
    	if(com.mossle.common.utils.StringUtils.isBlank(beginDate)){
    		beginDate = DateUtil.getMonthFrist();
    	}
    	if(com.mossle.common.utils.StringUtils.isBlank(endDate)){
    		endDate = DateUtil.getcurrenttimeDate();
    	}
    	model.addAttribute("beginDate",beginDate);
    	model.addAttribute("endDate",endDate);
    	if(partyEntityId==null){
    		model.addAttribute("partyEntityId", 0);
    		return "party/attendance-statistics-list-i";
    	}
    	else {
    		model.addAttribute("partyEntityId", partyEntityId);
        	model.addAttribute("partyStructTypeId", partyStructTypeId);
		}
    	
    	List<Map<String,Object>> attrndanceStatisticeList = orgService.getAttrndanceStatisticeList(name, partyEntityId,beginDate,endDate);
    	model.addAttribute("resultData", attrndanceStatisticeList);
    	model.addAttribute("name",name);
    	
    	return "party/attendance-statistics-list-i"; //
    }
    
    
    /**
     * 
     * @return
     */
    @RequestMapping("attendance-export-i")
    public void attendanceExcel(HttpServletResponse response, HttpServletRequest request,String arrStr,String year,String month){
    	JSONArray json = JSONObject.parseArray(arrStr);
    	//Map<String,Object> allList = queryAttendance(partyEntityId, year, month, name);
    	//Map<String,Object> allList = new HashMap<String,Object>();
    	exportUtil.export(json, response, request,year,month);
    }
    
    /**
     * 考勤列表查看请假或外出详情
     * @return
     */
    @RequestMapping("attendance-detail")
    @ResponseBody
    public String leaveDetail(String customFormId){
    	/*String detail = "select * from oa_bpm_customform o where o.applyCode='"+customFormId+"'";
    	Map<String,Object> detailMap = jdbcTemplate.queryForMap(detail);
		return JSONObject.toJSONString(detailMap);*/
    	String detail ="select k.detailHtml,k.url,k.REF from kv_record k where k.applyCode='"+customFormId+"'";
    	Map<String,Object> detailMap = jdbcTemplate.queryForMap(detail);
    	return JSONObject.toJSONString(detailMap);
    }
    /**
     * 新增.
     */
    @RequestMapping("org-input")
    public String input(
            Model model,
            @RequestParam(value = "partyStructTypeId", required = false) Long partyStructTypeId,
            @RequestParam(value = "partyTypeId", required = false) Long partyTypeId,
            @RequestParam(value = "partyEntityId", required = false) Long partyEntityId,
            @RequestParam(value = "level", required = false) int level)
            throws Exception {
        PartyEntity partyEntity = init(model, partyStructTypeId, partyEntityId);
        PartyType partyType = partyTypeManager.get(partyTypeId);

        //生成受理单编号
    	String userId = currentUserHolder.getUserId();
    	String code =  operationService.CreateApplyCode(userId);
    	
    	/*String isAudit="1";
        String hql="from DictInfo where dictType.name=?";
    	DictInfo dictInfo=dictInfoManager.findUnique(hql, "personMasterAudit");
        if(dictInfo==null){
        	isAudit=dictInfo.getValue();
        }*/
        
        String isAudit=userService.getAuditOpenStatus();
    	
        model.addAttribute("isAudit",isAudit);
    	model.addAttribute("code",code);
        model.addAttribute("partyEntity", partyEntity);
        model.addAttribute("partyType", partyType);
        model.addAttribute("level", level);
        if (partyEntity.getPartyType().getType() == PartyConstants.TYPE_POSITION) {
        	return "party/position-user-input";
        } else {
        	return "party/org-input";
        }
    }
    
    /**
     * 流程的关联人员
     * add by lilei at 2018-06-05
     * **/
    @RequestMapping("position-user-input-for-audit")
    public String inputRelationForAudit(
            Model model,
            @RequestParam(value = "applyCode", required = false) String applyCode,
            @RequestParam(value="isdetail",required=false) String isdetail
            /*@RequestParam(value = "partyStructTypeId", required = false) Long partyStructTypeId,
            @RequestParam(value = "partyTypeId", required = false) Long partyTypeId,
            @RequestParam(value = "partyEntityId", required = false) Long partyEntityId,
            @RequestParam(value = "level", required = false) int level*/
            )
            throws Exception {
    	
    	//岗位
		String positionName="";
		String structId="";
		String childEntityRef="";
		String userName="";
		String childEntityId="";
		String childEntityName="";
		String partyEntityId="";
		String partyTypeId="";
		String partyStructTypeId="";
		String priority="";
		if(com.mossle.core.util.StringUtils.isBlank(isdetail))
    		isdetail="1";
		
    	if(!com.mossle.core.util.StringUtils.isBlank(applyCode)){
    		UpdatePerson updatePerson=updatePersonManager.findUniqueBy("applyCode",applyCode);
    		if(updatePerson!=null){
    			if(!updatePerson.getIsApproval().equals("2"))
        			isdetail="1";
    			
    			Map<String,Object> map=jsonMapper.fromJson(updatePerson.getJsonContent(), Map.class);
    			structId=map.get("structId")==null?"":map.get("structId").toString();
    			childEntityRef=map.get("childEntityRef")==null?"":map.get("childEntityRef").toString();
    			childEntityId=map.get("childEntityId")==null?"":map.get("childEntityId").toString();
    			childEntityName=map.get("childEntityName")==null?"":map.get("childEntityName").toString();
    			partyEntityId=map.get("partyEntityId")==null?"":map.get("partyEntityId").toString();
    			partyTypeId=map.get("partyTypeId")==null?"":map.get("partyTypeId").toString();
    			partyStructTypeId=map.get("partyStructTypeId")==null?"":map.get("partyStructTypeId").toString();
    			priority=map.get("priority")==null?"":map.get("priority").toString();
    			
				if(!partyEntityId.equals("")){
					//岗位
					PartyEntity positionPartyEntity=partyEntityManager.findUniqueBy("id",Long.valueOf(partyEntityId));
					
					//部门
	    			String hql="from PartyStruct where partyStructType.id=1 and childEntity=?";
	    			PartyStruct deparmentPartyStruct=partyStructManager.findUnique(hql, positionPartyEntity);
	    			String deparmentName=deparmentPartyStruct.getParentEntity().getName();
	    			
	    			//公司
	    			hql="from PartyStruct where partyStructType.id=1 and childEntity=?";
	    			PartyStruct companyPartyStruct=partyStructManager.findUnique(hql, deparmentPartyStruct.getParentEntity());
	    			String companyName=companyPartyStruct.getParentEntity().getName();
	    			
	    			positionName=companyName+"-"+deparmentName+"-"+positionPartyEntity.getName();
				}
				
				if(!childEntityRef.equals("")){
					PartyEntity partyEntity=partyEntityManager.findUniqueBy("id",Long.valueOf(childEntityRef));
					if(partyEntity!=null)
						userName=partyEntity.getName();
				}
    			
    			//Map<String,Object> map=new HashMap<String, Object>();
    			//map.put("position", companyName+"-"+deparmentName+"-"+positionName);
    		}
    		
    		
    	}
    	
    	/*jsonMap.put("structId", partyStruct.getId());
    	jsonMap.put("childEntityRef", childEntityRef);
    	jsonMap.put("childEntityId", childEntityId);
    	jsonMap.put("childEntityName",childEntityName);
    	jsonMap.put("partyEntityId", partyEntityId);
    	jsonMap.put("partyTypeId", partyTypeId);
    	jsonMap.put("partyStructTypeId", partyStructTypeId);*/
		
    	model.addAttribute("structId",structId);
    	model.addAttribute("childEntityRef",childEntityRef);
    	model.addAttribute("userName", userName);
    	model.addAttribute("childEntityId", childEntityId);
    	model.addAttribute("childEntityName", childEntityName);
    	model.addAttribute("partyEntityId", partyEntityId);
    	model.addAttribute("partyTypeId", partyTypeId);
    	model.addAttribute("partyStructTypeId", partyStructTypeId);
    	model.addAttribute("priority", priority);
    	model.addAttribute("position", positionName);
    	model.addAttribute("applyCode",applyCode);
    	model.addAttribute("isdetail", isdetail);
    	
    	return "party/position-user-input-for-audit";
    	
        /*PartyEntity partyEntity = init(model, partyStructTypeId, partyEntityId);
        PartyType partyType = partyTypeManager.get(partyTypeId);

        //生成受理单编号
    	String userId = currentUserHolder.getUserId();
    	String code =  operationService.CreateApplyCode(userId);
        String isAudit=userService.getAuditOpenStatus();
        model.addAttribute("isAudit",isAudit);
    	model.addAttribute("code",code);
        model.addAttribute("partyEntity", partyEntity);
        model.addAttribute("partyType", partyType);
        model.addAttribute("level", level);
        if (partyEntity.getPartyType().getType() == PartyConstants.TYPE_POSITION) {
        	return "party/position-user-input";
        } else {
        	return "party/org-input";
        }*/
    }
    
    /**
     * 流程的关联人员-保存
     * add by lilei at 2018-06-05
     * **/
    @RequestMapping("position-user-input-for-audit-save")
    public String saveRelationForAudit(
            Model model,
            @RequestParam(value = "applyCode", required = false) String applyCode,
            @RequestParam(value = "structId", required = false) Long structId,
            @RequestParam(value = "childEntityRef", required = false) Long childEntityRef,
            @RequestParam(value = "childEntityId", required = false) Long childEntityId,
            @RequestParam(value = "childEntityName", required = false) Long childEntityName,
            @RequestParam(value = "partyEntityId", required = false) Long partyEntityId,
            @RequestParam(value = "partyTypeId", required = false) Long partyTypeId,
            @RequestParam(value = "partyStructTypeId", required = false) Long partyStructTypeId,
            @RequestParam(value = "priority", required = false) int priority,
            RedirectAttributes redirectAttributes
            )
            throws Exception {
    	
    	if(!com.mossle.core.util.StringUtils.isBlank(applyCode)){
    		orgService.relationPositionPersonEditForAudit(
    				applyCode, 
    				structId, 
    				childEntityRef,
    				childEntityId, 
    				childEntityName,
    				partyEntityId,
    				partyTypeId,
    				partyStructTypeId, 
    				priority);
    	}
    	
    	return "redirect:/user/close-popwin-dialog.do?msgTip="+java.net.URLEncoder.encode(java.net.URLEncoder.encode("修改成功","utf-8"),"utf-8");
    }

    /**
     * 编辑.
     */
    @RequestMapping("org-input-update")
    public String orgInputUpdate(Model model,
            @RequestParam(value = "id", required = false) Long id)
            throws Exception {
    	PartyStruct partyStruct = partyStructManager.get(id);
        PartyEntity partyEntity = partyEntityManager.get(partyStruct.getChildEntity().getId());
        PartyType partyType = partyEntity.getPartyType();
        
        String isAudit=userService.getAuditOpenStatus();
        String code =operationService.CreateApplyCode(currentUserHolder.getUserId());
        model.addAttribute("code",code);
        model.addAttribute("isAudit", isAudit);
        model.addAttribute("partyEntity", partyEntity);
        model.addAttribute("partyType", partyType);
        model.addAttribute("partyStructTypeId", 1);
        model.addAttribute("partyStruct", partyStruct);
        
        model.addAttribute("level", partyEntity.getLevel());
        
        String tenantId = tenantHolder.getTenantId();
        String hqlPartyStructType = "from PartyStructType where tenantId=? and display='true' order by priority";
        List<PartyStructType> partyStructTypes = partyStructTypeManager.find(hqlPartyStructType, tenantId);
        
        PartyStructType partyStructType = null;
        partyStructType = partyStructTypes.get(0);
        List<PartyStruct> partyStructs = partyStructManager.find(
                "from PartyStruct where childEntity = ? and partyStructType = ?",
                partyEntity, partyStructType);
        
        for (PartyStruct vo : partyStructs) {
        	 model.addAttribute("departmentCode", Long.toString(vo.getParentEntity().getId()));
             model.addAttribute("departmentName", vo.getParentEntity().getName());
        }
        
        /*String strIsRealPost="0";	//是否虚拟岗位，1：是；0：否
        String strPostNo="";		//岗位编号
        String strPositionRealIds="";	//真实岗位ID
        Map<String,Object> mapAttr=jdbcTemplate.queryForMap("SELECT * FROM party_entity_attr WHERE ID=?",id);
        if(mapAttr!=null){
        	strIsRealPost=mapAttr.get("isRealPosition").toString();
        	strPostNo=mapAttr.get("positionNo")==null?"":mapAttr.get("positionNo").toString();
        	strPositionRealIds=mapAttr.get("positionRealIds")==null?"":mapAttr.get("positionRealIds").toString();
        	
        	if(com.mossle.core.util.StringUtils.isBlank(strPositionRealIds)){
        		String[] strIds=strPositionRealIds.split
        	}
        }	
        model.addAttribute("isRealPosition",strIsRealPost);
        model.addAttribute("positionNo",strPostNo);
        model.addAttribute("positionRealIds",strPositionRealIds);*/
      
        return "party/org-update";
        
    }
    
  
    
    /**
     * 查看组织机构日志.
     */
    
   	@RequestMapping("org-log")
    @Log(desc = "人力资源", action = "input", operationDesc = "人力资源-组织机构-查看日志")
    public String rosterLogList(@ModelAttribute Page page,
       					@RequestParam Map<String, Object> parameterMap,
       					@RequestParam(value = "id", required = false) Long id,
                        Model model) throws Exception {

       	List<PropertyFilter> propertyFilters = PropertyFilter
                   .buildFromMap(parameterMap);
       	page = pagedQueryForOrgLog(page, propertyFilters,id.toString());
          
          
           model.addAttribute("model", page);
           model.addAttribute("id", id);
      
           return "party/org-log-list";
       }
    
    
    //查询组织机构日志
    private Page pagedQueryForOrgLog(Page page, List<PropertyFilter> propertyFilters, String strIds) throws ParseException {

        String sqlPagedQuerySelect = "SELECT b.`NAME`,a.* from oa_ba_orglog a JOIN party_entity b on a.operationID = b.ID  WHERE orgID=   " + strIds;

        String sqlPagedQueryCount = "SELECT count(1) from oa_ba_orglog a JOIN party_entity b on a.operationID = b.ID  WHERE orgID= " + strIds ;

         StringBuilder buff = new StringBuilder();
        List<Object> paramList = new ArrayList<Object>(); 
        boolean checkWhere = sqlPagedQuerySelect.toLowerCase().indexOf("where") == -1;
        PropertyFilterUtils.buildConfigurations(propertyFilters, buff,
                paramList, checkWhere);
        
        String sql = buff.toString();
        String countSql = "";
        String selectSql = "";
        if (StringUtils.isEmpty(strIds)) {
            countSql = sqlPagedQueryCount + " " + sql;
            selectSql = sqlPagedQuerySelect + " " + sql + " ORDER BY operationTime limit " + page.getStart() + "," + page.getPageSize();
        } else {
            countSql = sqlPagedQueryCount + " " + sql ;
            selectSql = sqlPagedQuerySelect + " " + sql +  " ORDER BY operationTime limit "
                    + page.getStart() + "," + page.getPageSize();
        }

        logger.debug("countSql : {}", countSql);
        logger.debug("selectSql : {}", selectSql);

        Object[] params = paramList.toArray();
        int totalCount = jdbcTemplate.queryForObject(countSql, Integer.class,
                params);
        List<Map<String, Object>> list = jdbcTemplate.queryForList(selectSql,
                params);
        List<OrgLogDTO> rosterLog = new ArrayList<OrgLogDTO>();

        for (Map<String, Object> map : list) {
        	 //chengze 20181016
        	OrgLogDTO orgLogDTO  = new OrgLogDTO();
        	orgLogDTO.setModifyContent(map.get("modifyContent").toString());
        	orgLogDTO.setOperationName( map.get("NAME").toString() );
        	orgLogDTO.setOperationTime(convertString(map.get("operationTime")).substring(0, 10));
            
        	rosterLog.add(orgLogDTO);
        }

        page.setTotalCount(totalCount);
        page.setResult(rosterLog);

        return page;
    }
    
    
    
    
    
    /**
     * 含有流程的编辑
     * add by lilei at 2018-06-04
     * **/
    @RequestMapping("org-update-for-audit")
    public String orgInputUpdateForAudit(Model model,
            @RequestParam(value = "applyCode",required=false) String applyCode,
            @RequestParam(value="isdetail",required=false) String isdetail)
            throws Exception {
    	if(com.mossle.core.util.StringUtils.isBlank(isdetail))
    		isdetail="1";
    	if(!com.mossle.core.util.StringUtils.isBlank(applyCode)){
    		UpdatePerson updatePerson=updatePersonManager.findUniqueBy("applyCode",applyCode);
    		if(updatePerson!=null)
    		{
    			if(!updatePerson.getIsApproval().equals("2"))
        			isdetail="1";
    			
    			Map<String,Object> map=jsonMapper.fromJson(updatePerson.getJsonContent(),Map.class);
    			if(updatePerson.getTypeID().equals("orgadd")){
    				//add
        			/*jsonMap.put("partyStructId", partyStruct.getId());
                	jsonMap.put("childEntityRef", childEntityRef);
                	jsonMap.put("childEntityId", childEntityId);
                	jsonMap.put("childEntityName",childEntityName);
                	jsonMap.put("shortName", shortName);
                	jsonMap.put("isDisplay", isDisplay);
                	jsonMap.put("username", username);
                	jsonMap.put("partyEntityId",partyEntityId);
                	jsonMap.put("partyTypeId", partyTypeId);
                	jsonMap.put("partyStructTypeId",partyStructTypeId);
                	jsonMap.put("partyLevel",partyLevel);*/
    				
    				/*Long partyStructId=Long.valueOf(map.get("partyStructId").toString());
    				PartyStruct partyStruct = partyStructManager.get(partyStructId);
    		        PartyEntity partyEntity = partyEntityManager.get(partyStruct.getChildEntity().getId());
    		        PartyType partyType = partyEntity.getPartyType();*/
    		        
    		        //String isAudit=userService.getAuditOpenStatus();
    		        //String code =operationService.CreateApplyCode(currentUserHolder.getUserId());
    		        //model.addAttribute("code",code);
    		        //model.addAttribute("isAudit", isAudit);
    		        
    		        /*model.addAttribute("partyEntity", partyEntity);
    		        model.addAttribute("partyType", partyType);
    		        model.addAttribute("partyStructTypeId", 1);
    		        model.addAttribute("partyStruct", partyStruct);
    		        model.addAttribute("level", partyEntity.getLevel());*/
    		        
    		        model.addAttribute("partyStructId", map.get("partyStructId")==null?"":map.get("partyStructId").toString());
    		        model.addAttribute("childEntityRef", map.get("childEntityRef")==null?"":map.get("childEntityRef").toString());
    		        model.addAttribute("childEntityId", map.get("childEntityId")==null?"":map.get("childEntityId").toString());
    		        model.addAttribute("childEntityName", map.get("childEntityName")==null?"":map.get("childEntityName").toString());
    		        model.addAttribute("shortName", map.get("shortName")==null?"":map.get("shortName").toString());
    		        model.addAttribute("isDisplay", map.get("isDisplay")==null?"":map.get("isDisplay").toString());
    		        model.addAttribute("username", map.get("username")==null?"":map.get("username").toString());
    		        model.addAttribute("partyEntityId", map.get("partyEntityId")==null?"":map.get("partyEntityId").toString());
    		        model.addAttribute("partyTypeId", map.get("partyTypeId")==null?"":map.get("partyTypeId").toString());
    		        //model.addAttribute("partyStructTypeId", map.get("partyStructTypeId")==null?"":map.get("partyStructTypeId").toString());
    		        model.addAttribute("level", map.get("partyLevel")==null?"":map.get("partyLevel").toString());
    		        model.addAttribute("priority", map.get("priority")==null?"":map.get("priority").toString());
    		        model.addAttribute("accountId", map.get("accountId")==null?"":map.get("accountId").toString());
    		        
    		        PartyType partyType=new PartyType();
    		        if(map.get("partyTypeId")!=null)
    		        	partyType=partyTypeManager.findUniqueBy("id", Long.valueOf(map.get("partyTypeId").toString()));
    		        model.addAttribute("partyType", partyType);
    		        model.addAttribute("partyStructTypeId", 1);
    		        
    			}
    			else if(updatePerson.getTypeID().equals("orgupdate")){
    				//update
        			/*jsonMap.put("structId", structId);
                	jsonMap.put("childEntityId", childEntityId);
                	jsonMap.put("childEntityName", childEntityName);
                	jsonMap.put("shortName",shortName);
                	jsonMap.put("isDisplay", isDisplay);
                	jsonMap.put("priority", priority);*/
					
    				Long partyStructId=Long.valueOf(map.get("structId").toString());
    				PartyStruct partyStruct = partyStructManager.get(partyStructId);
    		        PartyEntity partyEntity = partyEntityManager.get(partyStruct.getChildEntity().getId());
    		        PartyType partyType = partyEntity.getPartyType();
    				/*model.addAttribute("partyEntity", partyEntity);
    		        model.addAttribute("partyType", partyType);
    		        model.addAttribute("partyStructTypeId", 1);
    		        model.addAttribute("partyStruct", partyStruct);
    		        model.addAttribute("level", partyEntity.getLevel());*/
    				
    				model.addAttribute("partyStructId", map.get("structId")==null?"":map.get("structId").toString());
    		        model.addAttribute("childEntityRef", "");
    		        model.addAttribute("childEntityId", map.get("childEntityId")==null?"":map.get("childEntityId").toString());
    		        model.addAttribute("childEntityName", map.get("childEntityName")==null?"":map.get("childEntityName").toString());
    		        model.addAttribute("shortName", map.get("shortName")==null?"":map.get("shortName").toString());
    		        model.addAttribute("isDisplay", map.get("isDisplay")==null?"":map.get("isDisplay").toString());
    		        model.addAttribute("username", "");
    		        model.addAttribute("partyEntityId",partyEntity.getId());
    		        model.addAttribute("partyTypeId","");
    		        //model.addAttribute("partyStructTypeId", map.get("partyStructTypeId")==null?"":map.get("partyStructTypeId").toString());
    		        model.addAttribute("level",partyEntity.getLevel());
    		        model.addAttribute("priority", map.get("priority")==null?"":map.get("priority").toString());
    		        
    		        model.addAttribute("partyType", partyType);
    		        model.addAttribute("partyStructTypeId", 1);
    		        model.addAttribute("departmentCode", map.get("departmentCode")==null?"":map.get("departmentCode").toString());
    		        model.addAttribute("departmentName", map.get("departmentName")==null?"":map.get("departmentName").toString());
    		     
    			}
    		}
    	}
    	model.addAttribute("isdetail", isdetail);
    	model.addAttribute("applyCode", applyCode);
    	
    	/*PartyStruct partyStruct = partyStructManager.get(id);
        PartyEntity partyEntity = partyEntityManager.get(partyStruct.getChildEntity().getId());
        PartyType partyType = partyEntity.getPartyType();
        
        String isAudit=userService.getAuditOpenStatus();
        String code =operationService.CreateApplyCode(currentUserHolder.getUserId());
        model.addAttribute("code",code);
        model.addAttribute("isAudit", isAudit);
        model.addAttribute("partyEntity", partyEntity);
        model.addAttribute("partyType", partyType);
        model.addAttribute("partyStructTypeId", 1);
        model.addAttribute("partyStruct", partyStruct);
        
        model.addAttribute("level", partyEntity.getLevel());*/

        return "party/org-update-for-audit";
        
    }
    
    @RequestMapping("org-update-for-audit-save")
    @Log(desc = "人力资源", action = "update", operationDesc = "人力资源-组织机构-流程保存")
    public String saveForAudit(
    		HttpServletRequest request,
    		@RequestParam(value = "applyCode", required = false) String applyCode,
    		@RequestParam(value = "partyStructId", required = false) Long partyStructId,
            @RequestParam(value = "childEntityRef", required = false) String childEntityRef,
            @RequestParam(value = "childEntityId", required = false) Long childEntityId,
            @RequestParam(value = "childEntityName", required = false) String childEntityName,
            @RequestParam(value = "shortName", required = false) String shortName,
            @RequestParam(value = "isDisplay", required = false) String isDisplay,
            @RequestParam(value = "username", required = false) String username,
            @RequestParam("partyEntityId") Long partyEntityId,
            @RequestParam("departmentCode") String departmentCode,
            @RequestParam("departmentName") String departmentName,
            @RequestParam("partyTypeId") Long partyTypeId,
            @RequestParam("partyStructTypeId") Long partyStructTypeId,
            @RequestParam("partyLevel") int partyLevel,
            @RequestParam("priority") int priority,
            @RequestParam("accountId") Long accountId)
            throws Exception {
		    	orgService.partyInfoAddAndEditForAudit(applyCode, 
		    			partyStructId,
		    			childEntityRef, 
		    			childEntityId, 
		    			childEntityName, 
		    			shortName, 
		    			isDisplay, 
		    			username, 
		    			partyEntityId, 
		    			partyTypeId, 
		    			partyStructTypeId, 
		    			partyLevel,
						priority, 
						accountId,
						departmentCode,
						departmentName);
        return "redirect:/user/close-popwin-dialog.do?msgTip="+java.net.URLEncoder.encode(java.net.URLEncoder.encode("修改成功","utf-8"),"utf-8");
    }
    
    /**
     * 添加下级.
     */
    @RequestMapping("org-save")
    @Log(desc = "人力资源", action = "保存", operationDesc = "人力资源-组织机构-保存")
    public String save(
    		HttpServletRequest request,
            @ModelAttribute PartyStruct partyStruct,
            @RequestParam(value = "childEntityRef", required = false) String childEntityRef,
            @RequestParam(value = "childEntityId", required = false) Long childEntityId,
            @RequestParam(value = "childEntityName", required = false) String childEntityName,
            @RequestParam(value = "shortName", required = false) String shortName,
            @RequestParam(value = "isDisplay", required = false) String isDisplay,
            @RequestParam(value = "username", required = false) String username,
            @RequestParam("partyEntityId") Long partyEntityId,
            @RequestParam("partyTypeId") Long partyTypeId,
            @RequestParam("partyStructTypeId") Long partyStructTypeId,
            @RequestParam("partyLevel") int partyLevel,
            @RequestParam("priority") int priority,
            RedirectAttributes redirectAttributes)
            throws Exception {
	        String isAudit=userService.getAuditOpenStatus();
	        if(isAudit.equals("1")){
	        	orgService.partyInfoAddForAudit(
	        			request,
	        			partyStruct,
	        			childEntityRef,
	        			childEntityId,
	        			childEntityName,
	        			shortName,
	        			isDisplay, 
	        			username, 
	        			partyEntityId, 
	        			partyTypeId, 
	        			partyStructTypeId, 
	        			partyLevel, 
	        			priority);
	            messageHelper.addFlashMessage(redirectAttributes, "core.success.save", "新建申请成功，流程审核通过后生效");
	        }
	        else {
				orgService.partyInfoAdd(partyStruct,
										childEntityRef, 
										childEntityId,
										childEntityName,
										shortName,
										isDisplay,
										username,
										partyEntityId,
										partyTypeId,
										partyStructTypeId,
										partyLevel,
										priority,
										Long.valueOf(currentUserHolder.getUserId()));
				messageHelper.addFlashMessage(redirectAttributes, "core.success.save", "保存成功");
				
				//将改动内容存入日志表
				
				
			}
	        return "redirect:/party/org-list-i.do?partyStructTypeId="
	                + partyStructTypeId + "&partyEntityId=" + partyEntityId;
    }
    
    /**
     * 添加下级.
     */
    @RequestMapping("org-update")
    @Log(desc = "人力资源", action = "修改", operationDesc = "人力资源-组织机构-修改")
    public String upate(
    		HttpServletRequest request,
    		@RequestParam(value = "structId", required = false) Long structId,
            @RequestParam(value = "childEntityId", required = false) Long childEntityId,
            @RequestParam(value = "childEntityName", required = false) String childEntityName,
            @RequestParam(value = "shortName", required = false) String shortName,
            @RequestParam(value = "isDisplay", required = false) String isDisplay,
            @RequestParam(value = "priority", required = false) int priority,
            @RequestParam(value = "departmentCode", required = false) String departmentCode,
            @RequestParam(value = "departmentName", required = false) String departmentName,
            @RequestParam(value = "partyTypeId", required = false) String partyTypeId,
            RedirectAttributes redirectAttributes) throws Exception {
    	
    	/*PartyEntity child  = partyEntityManager.get(childEntityId);
    	child.setName(childEntityName);
    	child.setShortName(shortName);
    	child.setIsDisplay(isDisplay);
    	partyEntityManager.save(child);
    	
    	PartyStruct partyStruct = partyStructManager.get(structId);
    	partyStruct.setPriority(priority);
    	partyStructManager.save(partyStruct);
    	
    	//更新PersonInfo
    	long partyType=child.getPartyType().getId();
    	if(partyType==PartyConstants.PARTY_TYPE_COMPANY){
    		//判断是否属于分公司（如果是，则看成部门）
    		String hqlString="from PartyStruct where childEntity.id=? and partyStructType.id=1";
    		List<PartyStruct> childPartyStructList=partyStructManager.find(hqlString,"childEntity.id", child.getId());
    		if(childPartyStructList!=null&&childPartyStructList.size()>0)
    		{
    			PartyStruct childPartyStruct=childPartyStructList.get(0);
    			if(childPartyStruct.getParentEntity().getPartyType().getId()==PartyConstants.PARTY_TYPE_AREA){
        			//属于部门
        			String hql="from PersonInfo where departmentCode=?";
        	    	List<PersonInfo> personInfoList= personInfoManager.find(hql,child.getId().toString());
        	    	if(personInfoList!=null&&personInfoList.size()>0){
        	    		for (PersonInfo personInfo : personInfoList) {
    						personInfo.setDepartmentName(childEntityName);
    						personInfoManager.save(personInfo);
    					}
        	    	}
        		}
        		else {
        			//属于公司
        			String hql="from PersonInfo where companyCode=?";
        	    	List<PersonInfo> personInfoList= personInfoManager.find(hql,child.getId().toString());
        	    	if(personInfoList!=null&&personInfoList.size()>0){
        	    		for (PersonInfo personInfo : personInfoList) {
    						personInfo.setCompanyName(childEntityName);
    						personInfoManager.save(personInfo);
    					}
        	    	}
    			}
    		}
    	}
    	else if(partyType==PartyConstants.PARTY_TYPE_DEPARTMENT){
    		String hql="from PersonInfo where departmentCode=?";
	    	List<PersonInfo> personInfoList= personInfoManager.find(hql,child.getId().toString());
	    	if(personInfoList!=null&&personInfoList.size()>0){
	    		for (PersonInfo personInfo : personInfoList) {
					personInfo.setDepartmentName(childEntityName);
					personInfoManager.save(personInfo);
				}
	    	}
    	}*/
    	
    	//走流程
    	String isAudit=userService.getAuditOpenStatus();
    	if(isAudit.equals("1")){
    		/*Long structId,
            Long childEntityId,
            String childEntityName,
            String shortName,
            String isDisplay,
            int priority*/
    		
    		/*Map<String,Object> jsonMap=new HashMap<String, Object>();
        	jsonMap.put("structId", structId);
        	jsonMap.put("childEntityId", childEntityId);
        	jsonMap.put("childEntityName", childEntityName);
        	jsonMap.put("shortName",shortName);
        	jsonMap.put("isDisplay", isDisplay);
        	jsonMap.put("priority", priority);
        	
            UpdatePerson updatePerson = new UpdatePerson();
    		updatePerson.setJsonContent(jsonMapper.toJson(jsonMap));
    		updatePerson.setApplyCode(request.getParameter("applyCode"));
    		updatePerson.setIsApproval("2");
    		updatePerson.setUpdateParameters("");
    		updatePerson.setEmployeeNo(childEntityId.toString());
    		updatePerson.setTypeID("orgupdate");
    		updatePersonManager.save(updatePerson);
        	
    		PartyEntity partyEntity=partyEntityManager.findUniqueBy("id", childEntityId);
    		
        	//region 走流程
            //接下来发起流程（走的是自定义申请的流程）--------------------------------------------------
    		String titleString="";
            String content = "组织结构 "+ partyEntity.getName() +" 改为 "+childEntityName;
            MultipartFile[] files = null;
            String curentUserName = currentUserHolder.getName();
            customService.StartProcessCustomForPerson( request,curentUserName,content,"",files,"5");*/
            
            orgService.partyInfoUpdateForAudit(
							            		request, 
							            		structId, 
							            		childEntityId, 
							            		childEntityName, 
							            		shortName, 
							            		isDisplay, 
							            		priority,
							            		departmentCode,
							            		departmentName);
            messageHelper.addFlashMessage(redirectAttributes, "core.success.save", "新建申请成功，流程审核通过后生效");
    	}
    	else {
    		orgService.partyInfoUpdate(
					structId, 
					childEntityId, 
					childEntityName, 
					shortName, 
					isDisplay, 
					priority,
					departmentCode,
					departmentName);
    		//同步修改岗位配置流程的节点名称 sjx 18.09.15
    		if(partyTypeId.equals("5")){//判断是否编辑修改的是岗位
    			String postId = String.valueOf(childEntityId);
        		workSpaceService.modifyPostUpdateProcessConfig(postId,childEntityName);
    		}
    		messageHelper.addFlashMessage(redirectAttributes, "core.success.save", "保存成功");
		}
    	PartyStruct partyStruct = partyStructManager.get(structId);
        return "redirect:/party/org-list-i.do?partyStructTypeId=1&partyEntityId=" + partyStruct.getParentEntity().getId();
    

        
    
    
    }
    
    /**
     * 删除下级.
     */
    @RequestMapping("org-remove")
    @Log(desc = "人力资源", action = "删除", operationDesc = "人力资源-组织机构-删除")
    public String removeUser(
            @RequestParam("selectedItem") List<Long> selectedItem,
            @RequestParam("partyEntityId") Long partyEntityId,
            @RequestParam("partyStructTypeId") Long partyStructTypeId,
            RedirectAttributes redirectAttributes) {
    	
    	Long accountId = Long.parseLong(currentUserHolder.getUserId());
    	String delPersonNameString= "";
    	
    	String name ="";
    	boolean blnFlag = false;
    	boolean postChangeing = false;
        for (Long childId : selectedItem) {
            PartyStruct partyStruct = partyStructManager.get(childId);
            Long partyStructType = partyStruct.getPartyStructType().getId();
            Long child = partyStruct.getChildEntity().getId();
            if(PartyConstants.PARTY_STRUCT_TYPE_POSITION_USER == partyStructType){//岗位关系
            	String hql = "from UpdatePerson where typeID='changePost' and isApproval=2 and employeeNo=?";
            	List find = updatePersonManager.find(hql, String.valueOf(child));
            	if(find.size() > 0){
            		postChangeing = true;
            		break;
            	}
            }
            String hql = "from PartyStruct where childEntity.delFlag = '0' and parentEntity=? and partyStructType.id=?";
            PartyEntity vo = partyStruct.getChildEntity();
            delPersonNameString = vo.getName();
            // 如果没有选中partyEntityId，就啥也不显示
            Page page = partyStructManager.pagedQuery(hql, 1,
                    100, vo, partyStructTypeId);
            
            if (page.getResultSize() >0) {
            	name += partyStruct.getChildEntity().getName() + ",";
            	continue;
            }
                       
            // zyl 2017-07-12
            if (StringUtils.isBlank(name)) {
	            if (PartyConstants.PARTY_STRUCT_TYPE_ORG.equals(partyStruct.getPartyStructType().getId())) {
		            PartyEntity partyEntity = partyEntityManager.get(partyStruct.getChildEntity().getId());
		            partyEntity.setDelFlag(PersonInfoConstants.DELETE_FLAG_YES);
		            partyStructManager.remove(partyStruct); 
		            partyEntityManager.update(partyEntity);
	            }
	            
	            if (PartyConstants.PARTY_STRUCT_TYPE_MANAGE.equals(partyStruct.getPartyStructType().getId())) {
	            	partyStructManager.remove(partyStruct);
	            	blnFlag = true;
	            }
	            
	            if (PartyConstants.PARTY_STRUCT_TYPE_POSITION_USER.equals(partyStruct.getPartyStructType().getId())) {
	            	partyStructManager.remove(partyStruct);
	            }
	            
	            String positionName=personInfoService.getPositionCompany(vo.getId());
	            RosterLog rosterLog = new RosterLog ();
	    	    rosterLog.setCode("0");     									//受理单编号.
	    	    rosterLog.setOperationID(currentUserHolder.getName()); 			//操作人员id.
	    	    rosterLog.setContentBefore("”"+positionName+"“");  				//修改之前的内容.
	    	    rosterLog.setContentNew("岗位被删除了");							//修改后的新内容.
	    	    rosterLog.setUpdateColumn(""); 									//被修改的字段名.
	    	    rosterLog.setUpdateColumnName("删除岗位");
	    	    rosterLog.setIsapproval("0");									//1：流程审核还未通过   0：流程审核通过
	    	    rosterLog.setUpdateTime(new Date());  	  						//修改时间.
	    	    rosterLog.setEmployeeNo(vo.getId().toString());					//被修改的员工编号.
	    	    rosterLogManager.save(rosterLog);
            }
        }
        //该岗位正在调岗
        if(postChangeing){
        	messageHelper.addFlashMessage(redirectAttributes, "该员工有正在调岗的流程进行中，不能进行该操作！");
        	return "redirect:/party/org-list-i.do?partyStructTypeId="
                    + partyStructTypeId + "&partyEntityId=" + partyEntityId;
        }
        if (StringUtils.isBlank(name)) {
        	
        	//chengze 20181016 记录组织机构日志
            OrgLogEntity orgLogEntity  = new OrgLogEntity();
            orgLogEntity.setModifyContent("删除人员："+delPersonNameString);
            orgLogEntity.setOrgID(partyEntityId);
            orgLogEntity.setOperationID(accountId);
            orgLogEntity.setOperationTime(new Date());
            orgLogManager.save(orgLogEntity);
        	
        	messageHelper.addFlashMessage(redirectAttributes, "core.success.delete", "删除成功");
        	
        } else {
        	messageHelper.addFlashMessage(redirectAttributes, "core.success.delete", "由于 " + name.substring(0,name.length()-1) + " 存在下级节点，未成功删除！");
        }
        
        if (blnFlag) {
        	return "redirect:/party/org-admin-list.do?partyStructTypeId="
                    + partyStructTypeId + "&partyEntityId=" + partyEntityId;
        } else {
        	return "redirect:/party/org-list-i.do?partyStructTypeId="
                    + partyStructTypeId + "&partyEntityId=" + partyEntityId;
        	
        }
        
    }
    
    /**
     * 维护负责人.
     */
    @RequestMapping("org-admin-list")
    @Log(desc = "人力资源", action = "查看", operationDesc = "人力资源-组织机构-管理者-查看")
    public String orgAdminList(
            Model model,
            @RequestParam(value = "partyStructTypeId", required = false) Long partyStructTypeId,
            @RequestParam(value = "partyEntityId", required = false) Long partyEntityId)
            throws Exception {
        PartyEntity partyEntity = init(model, partyStructTypeId, partyEntityId);

        model.addAttribute("partyEntity", partyEntity);

        // TODO: 先写死id=2是负责关系
        // PartyStructType partyStructType = partyStructTypeManager.get(2L);
        if (partyEntity != null) {
            // 组织的负责人可能是岗位，可能是人
            String hql = "from PartyStruct where parentEntity=? and partyStructType=2";

            // 如果没有选中partyEntityId，就啥也不显示
            Page page = partyStructTypeManager.pagedQuery(hql, 1, 10,
                    partyEntity);
            model.addAttribute("page", page);
        }

        return "party/org-admin-list";
    }

    /**
     * 添加管理人或管理岗位.
     */
    @RequestMapping("org-admin-input")
    public String orgAdminInput(Model model,
            @RequestParam(value = "partyStructTypeId", required = false) Long partyStructTypeId,
            @RequestParam(value = "partyTypeId", required = false) Long partyTypeId,
            @RequestParam(value = "partyEntityId", required = false) Long partyEntityId)
            throws Exception {
    	
        partyStructTypeId = PartyConstants.PARTY_STRUCT_TYPE_ORG;

        PartyEntity partyEntity = init(model, partyStructTypeId, partyEntityId);
        PartyType partyType = partyTypeManager.get(partyTypeId);

        model.addAttribute("partyEntity", partyEntity);
        model.addAttribute("partyType", partyType);

        return "party/org-admin-input";
    }

    /**
     * 保存管理.
     */
    @RequestMapping("org-admin-save")
    @Log(desc = "人力资源", action = "保存", operationDesc = "人力资源-组织机构-管理者-保存")
    public String orgAdminSave(
            @ModelAttribute PartyStruct partyStruct,
            @RequestParam(value = "childEntityRef", required = false) String childEntityRef,
            @RequestParam(value = "childEntityId", required = false) Long childEntityId,
            @RequestParam(value = "childEntityName", required = false) String childEntityName,
            @RequestParam("partyEntityId") Long partyEntityId,
            @RequestParam("partyTypeId") Long partyTypeId,
            @RequestParam("partyStructTypeId") Long partyStructTypeId)
            throws Exception {
    	
    	Long accountId = Long.parseLong(currentUserHolder.getUserId());
        String tenantId = tenantHolder.getTenantId();
        
        PartyType partyType = partyTypeManager.get(partyTypeId);

        // 管理关系，暂定2，以后看来还是改成code较好
        PartyStructType partyStructType = partyStructTypeManager.get(PartyConstants.PARTY_STRUCT_TYPE_MANAGE);

        if (partyType.getType() == PartyConstants.TYPE_USER) {
            // 人员   zyl 2017-07-13
            //PartyEntity child = partyEntityManager.findUnique(
            //        "from PartyEntity where partyType=? and ref=?", partyType, childEntityRef);
            
            PartyEntity child = partyEntityManager.findUnique(
                    "from PartyEntity where partyType=? and id=?", partyType, Long.parseLong(childEntityRef));
            logger.debug("child : {}", child);

            PartyEntity parent = partyEntityManager.get(partyEntityId);

            PartyStruct dest = new PartyStruct();
            beanMapper.copy(partyStruct, dest);
            dest.setPartyStructType(partyStructType);
            dest.setParentEntity(parent);
            dest.setChildEntity(child);
            dest.setAddUserId(accountId);
            dest.setTenantId(tenantId);
            partyStructManager.save(dest);
            
            //chengze 20181016
            OrgLogEntity orgLogEntity  = new OrgLogEntity();
            orgLogEntity.setModifyContent(parent.getName()+"关联管理者： "+child.getName());
            orgLogEntity.setOrgID(parent.getId());
            orgLogEntity.setOperationID(accountId);
            orgLogEntity.setOperationTime(new Date());
            orgLogManager.save(orgLogEntity);
            
            
            
            
        } else if (partyType.getType() == PartyConstants.TYPE_POSITION) {
            // 岗位
            PartyEntity child = null;

            /*if (childEntityId == null) {
                child = new PartyEntity();
                child.setName(childEntityName);
                child.setPartyType(partyType);
                partyEntityManager.save(child);
            } else {*/
                child = new PartyEntity();
                child.setName(childEntityName);
                child.setPartyType(partyType);
                partyEntityManager.save(child);
            //}

            logger.debug("child : {}", child);

            PartyEntity parent = partyEntityManager.get(partyEntityId);

            PartyStruct dest = new PartyStruct();
            beanMapper.copy(partyStruct, dest);
            dest.setPartyStructType(partyStructType);
            dest.setParentEntity(parent);
            dest.setChildEntity(child);
            dest.setAddUserId(accountId);
            dest.setTenantId(tenantId);
            partyStructManager.save(dest);
        } else {
            logger.info("unsupport : {}", partyType.getType());
        }

        partyStructTypeId = PartyConstants.PARTY_STRUCT_TYPE_ORG;

        return "redirect:/party/org-admin-list.do?partyStructTypeId="
                + partyStructTypeId + "&partyEntityId=" + partyEntityId;
    }
    
    /**
     * 保存岗位与人员
     */
    @RequestMapping("position-user-save")
    public String positionUserSave(
    		HttpServletRequest request,
            @ModelAttribute PartyStruct partyStruct,
            @RequestParam(value = "childEntityRef", required = false) String childEntityRef,
            @RequestParam(value = "childEntityId", required = false) Long childEntityId,
            @RequestParam(value = "childEntityName", required = false) String childEntityName,
            @RequestParam("partyEntityId") Long partyEntityId,
            @RequestParam("partyTypeId") Long partyTypeId,
            @RequestParam("partyStructTypeId") Long partyStructTypeId,
            @RequestParam("priority") int priority,
            @RequestParam(value = "partyLevel", required = false) String partyLevel,
            RedirectAttributes redirectAttributes)
            throws Exception {
    	
    	/*Long accountId = Long.parseLong(currentUserHolder.getUserId());
        String tenantId = tenantHolder.getTenantId();
        
        PartyType partyType = partyTypeManager.get(partyTypeId);

        // 岗位人员
        PartyStructType partyStructType = partyStructTypeManager.get(PartyConstants.PARTY_STRUCT_TYPE_POSITION_USER);

        if (partyType.getType() == PartyConstants.TYPE_USER) {

            PartyEntity child = partyEntityManager.findUnique(
                    "from PartyEntity where partyType=? and id=?", partyType, Long.parseLong(childEntityRef));
            logger.debug("child : {}", child);

            PartyEntity parent = partyEntityManager.get(partyEntityId);

            PartyStruct dest = new PartyStruct();
            beanMapper.copy(partyStruct, dest);
            dest.setPartyStructType(partyStructType);
            dest.setParentEntity(parent);
            dest.setChildEntity(child);
            dest.setAddUserId(accountId);
            dest.setTenantId(tenantId);
            partyStructManager.save(dest);
        } else {
            logger.info("unsupport : {}", partyType.getType());
        }*/
    	
    	Map<String, Object> countMap = jdbcTemplate.queryForMap("select count(*) as count from party_struct where CHILD_ENTITY_ID = '"+String.valueOf(childEntityRef)+"' and PARENT_ENTITY_ID = '"+String.valueOf(partyEntityId)+"' and STRUCT_TYPE_ID = '4';");
    	String count = StringUtil.toString(countMap.get("count"));
    	if(!"0".equals(count) && !"1".equals(count)){
    		messageHelper.addFlashMessage(redirectAttributes, "core.success.delete", "人员已关联，请勿重复关联");
    		return "redirect:/party/org-input.do?partyStructTypeId="+partyStructTypeId+"&partyEntityId="+partyEntityId+"&partyTypeId="+partyTypeId+"&level="+partyLevel;
    	}
    	//开启流程审批，走流程
    	String isAudit=userService.getAuditOpenStatus();
    	if(isAudit.equals("1")){
    		/*@ModelAttribute PartyStruct partyStruct,
            @RequestParam(value = "childEntityRef", required = false) String childEntityRef,
            @RequestParam(value = "childEntityId", required = false) Long childEntityId,
            @RequestParam(value = "childEntityName", required = false) String childEntityName,
            @RequestParam("partyEntityId") Long partyEntityId,
            @RequestParam("partyTypeId") Long partyTypeId,
            @RequestParam("partyStructTypeId") Long partyStructTypeId*/
            
    		/*Map<String,Object> jsonMap=new HashMap<String, Object>();
        	jsonMap.put("structId", partyStruct.getId());
        	jsonMap.put("childEntityRef", childEntityRef);
        	jsonMap.put("childEntityId", childEntityId);
        	jsonMap.put("childEntityName",childEntityName);
        	jsonMap.put("partyEntityId", partyEntityId);
        	jsonMap.put("partyTypeId", partyTypeId);
        	jsonMap.put("partyStructTypeId", partyStructTypeId);
        	jsonMap.put("priority", priority);
        	
            UpdatePerson updatePerson = new UpdatePerson();
    		updatePerson.setJsonContent(jsonMapper.toJson(jsonMap));
    		updatePerson.setApplyCode(request.getParameter("applyCode"));
    		updatePerson.setIsApproval("2");
    		updatePerson.setUpdateParameters("");
    		updatePerson.setEmployeeNo(partyEntityId.toString());
    		updatePerson.setTypeID("postwithperson");
    		updatePersonManager.save(updatePerson);
        	
    		PartyEntity postionPartyEntity=partyEntityManager.findUniqueBy("id", partyEntityId);
        	//region 走流程
            //接下来发起流程（走的是自定义申请的流程）--------------------------------------------------
    		String titleString="";
            String content = "关联人员   "+(postionPartyEntity==null?"":postionPartyEntity.getName())+"岗位关联人员"+request.getParameter("username");
            MultipartFile[] files = null;
            String curentUserName = currentUserHolder.getName();
            customService.StartProcessCustomForPerson( request,curentUserName,content,"",files,"6");*/
    		orgService.relationPositionPersonForAudit(
    				request,
    				request.getParameter("applyCode"), 
    				partyStruct.getId(), 
    				Long.valueOf(childEntityRef), 
    				childEntityId, 
    				childEntityName,
					partyEntityId, 
					partyTypeId, 
					partyStructTypeId, 
					priority);
            
            partyStructTypeId = PartyConstants.PARTY_STRUCT_TYPE_ORG;
	        messageHelper.addFlashMessage(redirectAttributes, "core.success.delete", "关联人员申请成功，流程审核通过后生效");
    	}
    	else {
			orgService.relationPostionPerson(
					partyStruct, 
					childEntityRef, 
					childEntityId, 
					childEntityName, 
					partyEntityId, 
					partyTypeId, 
					partyStructTypeId,
					priority,
					Long.valueOf(currentUserHolder.getUserId()),
					"0");
			 partyStructTypeId = PartyConstants.PARTY_STRUCT_TYPE_ORG;
	         messageHelper.addFlashMessage(redirectAttributes, "core.success.delete", "关联人员成功");
		}
        return "redirect:/party/org-list-i.do?partyStructTypeId="
                + partyStructTypeId + "&partyEntityId=" + partyEntityId;
    }

    /**
     * 添加职位.
     */
    @RequestMapping("org-position-input")
    public String orgPositionInput(
            Model model,
            @RequestParam(value = "partyStructTypeId", required = false) Long partyStructTypeId,
            @RequestParam(value = "partyTypeId", required = false) Long partyTypeId,
            @RequestParam(value = "partyEntityId", required = false) Long partyEntityId)
            throws Exception {
        partyStructTypeId = 1L;

        PartyEntity partyEntity = init(model, partyStructTypeId, partyEntityId);
        PartyType partyType = partyTypeManager.get(partyTypeId);

        model.addAttribute("partyEntity", partyEntity);
        model.addAttribute("partyType", partyType);

        return "party/org-position-input";
    }

    /**
     * 保存职位.
     */
    @RequestMapping("org-position-save")
    public String orgPositionSave(@ModelAttribute PartyStruct partyStruct,
            @RequestParam(value = "childEntityRef", required = false) String childEntityRef,
            @RequestParam(value = "childEntityId", required = false) Long childEntityId,
            @RequestParam(value = "childEntityName", required = false) String childEntityName,
            @RequestParam("partyEntityId") Long partyEntityId,
            @RequestParam("partyTypeId") Long partyTypeId,
            @RequestParam("partyStructTypeId") Long partyStructTypeId)
            throws Exception {
        PartyType partyType = partyTypeManager.get(partyTypeId);

        // 岗位人员是5
        PartyStructType partyStructType = partyStructTypeManager.get(5L);

        if (partyType.getType() == PartyConstants.TYPE_POSITION) {
            // 岗位
            PartyEntity child = null;

            if (childEntityId == null) {
                child = new PartyEntity();
                child.setName(childEntityName);
                child.setPartyType(partyType);
                partyEntityManager.save(child);
            } else {
                child = new PartyEntity();
                child.setName(childEntityName);
                child.setPartyType(partyType);
                partyEntityManager.save(child);
            }

            logger.debug("child : {}", child);

            PartyEntity parent = partyEntityManager.get(partyEntityId);

            PartyStruct dest = new PartyStruct();
            beanMapper.copy(partyStruct, dest);
            dest.setPartyStructType(partyStructType);
            dest.setParentEntity(parent);
            dest.setChildEntity(child);
            dest.setTenantId("1");
            partyStructManager.save(dest);
        } else {
            logger.info("unsupport : {}", partyType.getType());
        }

        partyStructTypeId = 1L;

        return "redirect:/party/org-list.do?partyStructTypeId="
                + partyStructTypeId + "&partyEntityId=" + partyEntityId;
    }

    
    
    
    @SuppressWarnings("rawtypes")
    @RequestMapping("asyncTree")
    @Produces(MediaType.APPLICATION_JSON)
    public void asyncTree(@RequestParam(value = "id", required = false) String id,
            @QueryParam("partyStructTypeId") long partyStructTypeId,
            @RequestParam(value = "notViewPost", required = false) boolean notViewPost,
            @RequestParam(value = "notAuth", required = false) boolean notAuth,
            HttpServletResponse response) {
 
    	PartyEntity partyEntity = null;
    	if (StringUtils.isBlank(id)) {
    		List<PartyEntity> partyEntities = partyService.getTopPartyEntities(partyStructTypeId);
    		
    		partyEntity = partyEntities.get(0);
    	} else {
    		partyEntity = partyEntityManager.get(Long.parseLong(id));
    	}
        
    	
    	
        List<Map> result = asyncGeneratePartyEntities(partyEntity, partyStructTypeId, notAuth, notViewPost);
    	
        response.setContentType(MediaType.APPLICATION_JSON);
        try {
			response.getOutputStream().write(
			        jsonMapper.toJson(result).getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    }
    
    @SuppressWarnings({"rawtypes"})
	public List<Map> asyncGeneratePartyEntities(PartyEntity partyEntity, long partyStructTypeId,
			boolean auth, boolean notViewPost) {
    	
		if (partyEntity == null) {
			return null;
		}
		
		List<Map> list = new ArrayList<Map>();
		
		try {
			if (partyEntity.getId() == PartyConstants.ROOT_PARTY_TREE_ID) {
				
				
				Map<String, Object> map = new HashMap<String, Object>();
				
				map.put("id", partyEntity.getId());
				map.put("pId", 0);
					
				map.put("name", partyEntity.getName());
				map.put("ref", partyEntity.getRef());
				map.put("open", true);
				map.put("title", partyEntity.getName());
				list.add(map);
			
				
				// 查找下级节点
				asyncGeneratePartyEntity(partyEntity, partyStructTypeId, auth, list, notViewPost);
			} else {
				
				asyncGeneratePartyEntity(partyEntity, partyStructTypeId, auth, list, notViewPost);
			}
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
		
		return list;
	}
    
  //region 数据全控制的树形结构
    @SuppressWarnings("rawtypes")
    @RequestMapping("asyncTreeForAuth")
    @Produces(MediaType.APPLICATION_JSON)
    public void asyncTreeForAuth(@RequestParam(value = "id", required = false) String id,
            @QueryParam("partyStructTypeId") long partyStructTypeId,
            @RequestParam(value = "notViewPost", required = false) boolean notViewPost,
            @RequestParam(value = "notAuth", required = false) boolean notAuth,
            HttpServletResponse response) {
 
    	PartyEntity partyEntity = null;
    	if (StringUtils.isBlank(id)) {
    		List<PartyEntity> partyEntities = partyService.getTopPartyEntities(partyStructTypeId);
    		
    		partyEntity = partyEntities.get(0);
    	} else {
    		partyEntity = partyEntityManager.get(Long.parseLong(id));
    	}
        
    	List<Map> result=new ArrayList<Map>();
    	if(currentUserHolder.getUserId().equals(PartyConstants.ADMIN_USER_ID))
    		result = asyncGeneratePartyEntities(partyEntity, partyStructTypeId, notAuth, notViewPost);
    	else
    		result = asyncGeneratePartyEntitiesForAuth(partyEntity, partyStructTypeId, notAuth, notViewPost);
    	
        response.setContentType(MediaType.APPLICATION_JSON);
        try {
			response.getOutputStream().write(
			        jsonMapper.toJson(result).getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    }
    
    @SuppressWarnings({"rawtypes"})
	public List<Map> asyncGeneratePartyEntitiesForAuth(PartyEntity partyEntity, long partyStructTypeId,
			boolean auth, boolean notViewPost) {
    	
		if (partyEntity == null) {
			return null;
		}
		
		List<Map> list = new ArrayList<Map>();
		
		try {
			Long accountId = Long.parseLong(currentUserHolder.getUserId());
			List<String> partyIdList=null;
			String strSql="Select partyEntityID from auth_orgdata where type='1' and union_id="+accountId;
			partyIdList=jdbcTemplate.queryForList(strSql, String.class);
			
			if (partyEntity.getId() == PartyConstants.ROOT_PARTY_TREE_ID) {
				
				if((accountId.equals(PartyConstants.ADMIN_USER_ID))
						||(partyIdList!=null&&partyIdList.contains(partyEntity.getId().toString()))
					){
					Map<String, Object> map = new HashMap<String, Object>();
					
					map.put("id", partyEntity.getId());
					map.put("pId", 0);
						
					map.put("name", partyEntity.getName());
					map.put("ref", partyEntity.getRef());
					map.put("open", true);
					map.put("title", partyEntity.getName());
					list.add(map);
				}
				
				// 查找下级节点
				asyncGeneratePartyEntityForAuth(partyEntity, partyStructTypeId, auth, list, notViewPost,partyIdList);
			} else {
				
				asyncGeneratePartyEntityForAuth(partyEntity, partyStructTypeId, auth, list, notViewPost,partyIdList);
			}
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
		
		return list;
	}
    @SuppressWarnings({"unchecked", "rawtypes"})
	private void asyncGeneratePartyEntityForAuth(PartyEntity partyEntity, long partyStructTypeId,
			boolean auth, List<Map> list, boolean notViewPost,List<String> partyIdList) {
		
		Long accountId = Long.parseLong(currentUserHolder.getUserId());
		
		
		boolean isSupterAdmin=accountId.equals(PartyConstants.ADMIN_USER_ID);
		
		if(isSupterAdmin||(partyIdList!=null&&partyIdList.contains(partyEntity.getId().toString()))){
		List<PartyStruct> partyStructs = partyStructManager.find(
		        "from PartyStruct where parentEntity=? order by priority",
		        partyEntity);
		
		for (PartyStruct partyStruct : partyStructs) {
			if (partyStruct.getPartyStructType().getId() == partyStructTypeId) {
				PartyEntity childPartyEntity = partyStruct.getChildEntity();
				if(!childPartyEntity.getPartyType().getId().equals(PartyConstants.PARTY_TYPE_POST)){
					if(!isSupterAdmin&&(partyIdList!=null&&!partyIdList.contains(childPartyEntity.getId().toString()))){
						continue;
					}
				}
				
				// System.out.println(childPartyEntity.getId() + "-" + childPartyEntity.getName() + "-" + childPartyEntity.getPartyType().getType() );
				if (childPartyEntity == null) {
					logger.debug("child party entity is null");
					continue;
				}
				
				if (childPartyEntity.getDelFlag().equals(PersonInfoConstants.DELETE_FLAG_YES)) {
		            logger.debug("child party entity is delete");
		            continue;
		        }
				//判断是否显示
				String isDisplay = childPartyEntity.getIsDisplay();
				if (isDisplay == null ||isDisplay.equals("0"))
				{
		            logger.debug("child party entity is hidden");
		            continue;
		        }

		        if (childPartyEntity.getPartyType().getType() != PartyConstants.TYPE_USER) {
		            if (auth) {
		                //if (accountId.equals(PartyConstants.ADMIN_USER_ID)) {   // 超级管理员
		                	viewIsPostMap(partyEntity, list, notViewPost, childPartyEntity);
		                /*} else {
		                    if (!PartyConstants.ROOT_PARTY_TREE_ID.equals(partyStruct.getParentEntity().getId())) {
		                    	viewIsPostMap(partyEntity, list, notViewPost, childPartyEntity);
		                    } else {
		                        AccountInfo accountInfo = accountInfoManager.get(accountId);

		                        PartyEntity vo = partyEntityManager.get(Long.parseLong(accountInfo.getCode()));

		                        List<PartyStruct> structList = partyStructManager.find(
		                                "from PartyStruct where childEntity=?", vo);

		                        for (PartyStruct item : structList) {
		                            if (Long.toString(PartyConstants.ROOT_PARTY_TREE_ID).equals(item.getChildEntity().getId())) {
		                            	viewIsPostMap(partyEntity, list, notViewPost, childPartyEntity);
		                            }

		                            // 判断是否是分公司管理员的公司
		                            if (childPartyEntity.getId().equals(item.getParentEntity().getId())) {
		                            	viewIsPostMap(partyEntity, list, notViewPost, childPartyEntity);
		                            }
		                            break;
		                        }
		                    }
		                }*/
		            } else {
		            	viewIsPostMap(partyEntity, list, notViewPost, childPartyEntity);
		            }
		        }
			}
		}
		}
	}
    //endregion
    
    @SuppressWarnings("rawtypes")
    @RequestMapping("asyncTreeCycle")
    @Produces(MediaType.APPLICATION_JSON)
    public void asyncTreeCycle(@RequestParam(value = "id", required = false) String id,
            @QueryParam("partyStructTypeId") long partyStructTypeId,
            @RequestParam(value = "notViewPost", required = false) boolean notViewPost,
            @RequestParam(value = "notAuth", required = false) boolean notAuth,
            @RequestParam(value ="partyid",required=false) String partyid,
            HttpServletResponse response) {
 
    	PartyEntity partyEntity = null;
    	if (StringUtils.isBlank(id)) {
    		List<PartyEntity> partyEntities = partyService.getTopPartyEntities(partyStructTypeId);
    		
    		partyEntity = partyEntities.get(0);
    	} else {
    		partyEntity = partyEntityManager.get(Long.parseLong(id));
    	}
        
    	
    	
        //List<Map> result = asyncGeneratePartyEntitiesRecycle(partyEntity, partyStructTypeId, notAuth, notViewPost);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("id", partyEntity.getId());
		map.put("pId", 0);
			
		map.put("name", partyEntity.getName());
		map.put("ref", partyEntity.getRef());
		map.put("open", true);
		map.put("title", partyEntity.getName());
		map.put("isParent", true);
		
		List<String> partyIdList=null;
		if(!StringUtils.isBlank(partyid))
		{
			String strSql="Select partyEntityID from auth_orgdata where type='1' and union_id="+partyid;
			partyIdList=jdbcTemplate.queryForList(strSql, String.class);
		}
		
		
		map.put("checked", partyIdList!=null&&partyIdList.contains(partyEntity.getId().toString()));
		map.put("children", asyncGeneratePartyEntitiesRecycle(partyEntity, partyStructTypeId, notAuth, notViewPost,partyIdList));
        response.setContentType(MediaType.APPLICATION_JSON);
        try {
        	List<Map> mapList=new ArrayList<Map>();
        	mapList.add(map);
			response.getOutputStream().write(
			        jsonMapper.toJson(mapList).getBytes("UTF-8"));
			//mapList.contains(arg0)
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			try {
				throw e;
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
    	
    }
    
    /**
     * 循环读出组织结构信息
     * **/
    @SuppressWarnings({"rawtypes"})
	public List<Map> asyncGeneratePartyEntitiesRecycle(PartyEntity partyEntity, long partyStructTypeId,
			boolean auth, boolean notViewPost,List<String> partyIdList) {
    	
		if (partyEntity == null) {
			return null;
		}
		
		List<Map> list = new ArrayList<Map>();
		
		try {
			//List<Map> childMapList = new ArrayList<Map>();
			if(partyEntity.getChildStructs()!=null&&partyEntity.getChildStructs().size()>0){
				for(PartyStruct childStruct:partyEntity.getChildStructs()){
					PartyEntity childPartyEntity=childStruct.getChildEntity();
					
					//隐藏，则不读取
					if(childPartyEntity.getIsDisplay()!=null&&childPartyEntity.getIsDisplay().equals("0")) continue;
					
					if(childPartyEntity.getPartyType().getId().equals(PartyConstants.PARTY_TYPE_COMPANY)
					  ||childPartyEntity.getPartyType().getId().equals(PartyConstants.PARTY_TYPE_DEPARTMENT)
					  ||childPartyEntity.getPartyType().getId().equals(PartyConstants.PARTY_TYPE_GROUP)
					  ||childPartyEntity.getPartyType().getId().equals(PartyConstants.PARTY_TYPE_AREA)){
						Map<String, Object> childMap = new HashMap<String, Object>();
						
						childMap.put("id", childPartyEntity.getId());
						childMap.put("pId", 0);
							
						childMap.put("name", childPartyEntity.getName());
						childMap.put("ref", childPartyEntity.getRef());
						childMap.put("open", true);
						childMap.put("title", childPartyEntity.getName());
						childMap.put("checked", partyIdList!=null&&partyIdList.contains(childPartyEntity.getId().toString()));
						if(childPartyEntity.getChildStructs()!=null&&childPartyEntity.getChildStructs().size()>0
							&&!childPartyEntity.getPartyType().getId().equals(PartyConstants.PARTY_TYPE_GROUP))
						{
							
							List<Map> childMapList=asyncGeneratePartyEntitiesRecycle(childPartyEntity,partyStructTypeId,auth,notViewPost,partyIdList);
							childMap.put("children", childMapList);
							if(childMapList!=null&&childMapList.size()>0)
								childMap.put("isParent", true);
							else
								childMap.put("isParent", false);
							
							
						}
						else
							childMap.put("isParent", false);
						list.add(childMap);
					}
				}
				
			}
			
			
			/*if (partyEntity.getId() == PartyConstants.ROOT_PARTY_TREE_ID) {
				
				// 查找下级节点
				asyncGeneratePartyEntity(partyEntity, partyStructTypeId, auth, list, notViewPost);
			} else {
				
				asyncGeneratePartyEntity(partyEntity, partyStructTypeId, auth, list, notViewPost);
			}*/
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
		
		return list;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private void asyncGeneratePartyEntity(PartyEntity partyEntity, long partyStructTypeId,
			boolean auth, List<Map> list, boolean notViewPost) {
		
		Long accountId = Long.parseLong(currentUserHolder.getUserId());
		
		
		
		//List<String> partyIdList=null;
		//String strSql="Select partyEntityID from auth_orgdata where type='1' and union_id="+accountId;
		//partyIdList=jdbcTemplate.queryForList(strSql, String.class);
		
		//if(partyIdList!=null&&partyIdList.contains(partyEntity.getId())){
		List<PartyStruct> partyStructs = partyStructManager.find(
		        "from PartyStruct where parentEntity=? order by priority",
		        partyEntity);
		
		for (PartyStruct partyStruct : partyStructs) {
			if (partyStruct.getPartyStructType().getId() == partyStructTypeId) {
				PartyEntity childPartyEntity = partyStruct.getChildEntity();
				//if(partyIdList!=null&&!partyIdList.contains(childPartyEntity.getId())){
					//continue;
				//}
				
				// System.out.println(childPartyEntity.getId() + "-" + childPartyEntity.getName() + "-" + childPartyEntity.getPartyType().getType() );
				if (childPartyEntity == null) {
					logger.debug("child party entity is null");
					continue;
				}
				
				if (childPartyEntity.getDelFlag().equals(PersonInfoConstants.DELETE_FLAG_YES)) {
		            logger.debug("child party entity is delete");
		            continue;
		        }
				//判断是否显示
				String isDisplay = childPartyEntity.getIsDisplay();
				if (isDisplay == null ||isDisplay.equals("0"))
				{
		            logger.debug("child party entity is hidden");
		            continue;
		        }

		        if (childPartyEntity.getPartyType().getType() != PartyConstants.TYPE_USER) {
		            if (auth) {
		                //if (accountId.equals(PartyConstants.ADMIN_USER_ID)) {   // 超级管理员
		                	viewIsPostMap(partyEntity, list, notViewPost, childPartyEntity);
		                /*} else {
		                    if (!PartyConstants.ROOT_PARTY_TREE_ID.equals(partyStruct.getParentEntity().getId())) {
		                    	viewIsPostMap(partyEntity, list, notViewPost, childPartyEntity);
		                    } else {
		                        AccountInfo accountInfo = accountInfoManager.get(accountId);

		                        PartyEntity vo = partyEntityManager.get(Long.parseLong(accountInfo.getCode()));

		                        List<PartyStruct> structList = partyStructManager.find(
		                                "from PartyStruct where childEntity=?", vo);

		                        for (PartyStruct item : structList) {
		                            if (Long.toString(PartyConstants.ROOT_PARTY_TREE_ID).equals(item.getChildEntity().getId())) {
		                            	viewIsPostMap(partyEntity, list, notViewPost, childPartyEntity);
		                            }

		                            // 判断是否是分公司管理员的公司
		                            if (childPartyEntity.getId().equals(item.getParentEntity().getId())) {
		                            	viewIsPostMap(partyEntity, list, notViewPost, childPartyEntity);
		                            }
		                            break;
		                        }
		                    }
		                }*/
		            } else {
		            	viewIsPostMap(partyEntity, list, notViewPost, childPartyEntity);
		            }
		        }
			}
		}
		//}
	}

	// 机构树中是否显示岗位
	private void viewIsPostMap(PartyEntity partyEntity, List<Map> list,
			boolean notViewPost, PartyEntity childPartyEntity) {
		if (!notViewPost) {  // 显示全部
			generateMap(partyEntity, list, childPartyEntity, notViewPost);
		} else { // 不显示岗位
			if (childPartyEntity.getPartyType().getType() != PartyConstants.TYPE_POSITION) {
				generateMap(partyEntity, list, childPartyEntity, notViewPost);
			}
		}
	}

	private void generateMap(PartyEntity partyEntity, List<Map> list,
			PartyEntity childPartyEntity, boolean notViewPost) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("id", childPartyEntity.getId());
		map.put("pId", partyEntity.getId());
		if (childPartyEntity.getPartyType().getType() == PartyConstants.TYPE_POSITION) {
			map.put("name", "<span style='color:blue;margin-right:0px;'>" + childPartyEntity.getName() + "</span>");
		} else {
			map.put("name", childPartyEntity.getName());
		}
		map.put("title", childPartyEntity.getName());
		map.put("ref", childPartyEntity.getRef());
		map.put("open", true);
		Set<PartyStruct> child = childPartyEntity.getChildStructs();
		int i = 0;
		if (child != null && child.size() > 0) {
			
			for (PartyStruct struct : child) {
				PartyEntity vo = struct.getChildEntity();
				if (!notViewPost) {  // 显示全部
					if (vo.getPartyType().getType() != PartyConstants.TYPE_USER && vo.getDelFlag().equals(PersonInfoConstants.DELETE_FLAG_NO)) {
						i =1;
						break;
					}
				} else {
					if ((vo.getPartyType().getType() != PartyConstants.TYPE_USER && vo.getPartyType().getType() != PartyConstants.TYPE_POSITION)&& vo.getDelFlag().equals(PersonInfoConstants.DELETE_FLAG_NO)) {
						i =1;
						break;
					}
				}
			}
			if (i == 1) {
				map.put("isParent", true);
			} else {
				map.put("isParent", false);
			}
		} else {
			map.put("isParent", false);
		}
		list.add(map);
	}
	
	
	//region 汇报树形结构 add by lilei at 2019.01.08
    @SuppressWarnings("rawtypes")
    @RequestMapping("asyncReportTreeForAuth")
    @Produces(MediaType.APPLICATION_JSON)
    public void asyncReportTreeForAuth(@RequestParam(value = "id", required = false) String id,
            @QueryParam("partyStructTypeId") long partyStructTypeId,
            @RequestParam(value = "notViewPost", required = false) boolean notViewPost,
            @RequestParam(value = "notAuth", required = false) boolean notAuth,
            HttpServletResponse response) {
 
    	PartyEntity partyEntity = null;
    	if (StringUtils.isBlank(id)) {
    		List<PartyEntity> partyEntities = partyService.getTopPartyEntities(partyStructTypeId);
    		
    		partyEntity = partyEntities.get(0);
    	} else {
    		partyEntity = partyEntityManager.get(Long.parseLong(id));
    	}
        
    	List<Map> result=new ArrayList<Map>();
		result = asyncReportGeneratePartyEntitiesForAuth(partyEntity, partyStructTypeId, notAuth, notViewPost);
    	
        response.setContentType(MediaType.APPLICATION_JSON);
        try {
			response.getOutputStream().write(
			        jsonMapper.toJson(result).getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    }
    
    @SuppressWarnings({"rawtypes"})
	public List<Map> asyncReportGeneratePartyEntitiesForAuth(PartyEntity partyEntity, long partyStructTypeId,
			boolean auth, boolean notViewPost) {
		if (partyEntity == null) {
			return null;
		}
		List<Map> list = new ArrayList<Map>();
		try {
			Long accountId = Long.parseLong(currentUserHolder.getUserId());
			/*List<String> partyIdList=null;
			String strSql="Select partyEntityID from auth_orgdata where type='1' and union_id="+accountId;
			partyIdList=jdbcTemplate.queryForList(strSql, String.class);*/
			
			List<String> partyIdList=partyConnector.getSystemAcccountIdList();
			
			if (partyEntity.getId() == PartyConstants.ROOT_PARTY_TREE_ID) {
				/*if((accountId.equals(PartyConstants.ADMIN_USER_ID))
						||(partyIdList!=null&&partyIdList.contains(partyEntity.getId().toString()))
					){*/
					Map<String, Object> map = new HashMap<String, Object>();
					
					map.put("id", partyEntity.getId());
					map.put("pId", 0);
						
					map.put("name", partyEntity.getName());
					map.put("ref", partyEntity.getRef());
					map.put("open", true);
					map.put("title", partyEntity.getName());
					list.add(map);
				//}
				
				// 查找下级节点
				asyncReportGeneratePartyEntityForAuth(partyEntity, partyStructTypeId, auth, list, notViewPost,partyIdList);
			} else {
				
				asyncReportGeneratePartyEntityForAuth(partyEntity, partyStructTypeId, auth, list, notViewPost,partyIdList);
			}
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
		
		return list;
	}
    @SuppressWarnings({"unchecked", "rawtypes"})
	private void asyncReportGeneratePartyEntityForAuth(PartyEntity partyEntity, long partyStructTypeId,
			boolean auth, List<Map> list, boolean notViewPost,List<String> partyIdList) {
		
		Long accountId = Long.parseLong(currentUserHolder.getUserId());
		//boolean isSupterAdmin=accountId.equals(PartyConstants.ADMIN_USER_ID);
		
		//if(isSupterAdmin||(partyIdList!=null&&partyIdList.contains(partyEntity.getId().toString()))){
		
		String userId=currentUserHolder.getUserId();
    	List<String> partyUpperAndLowerIdList=partyConnector.getUpperCompanyAndLowerDepartment(accountId.toString());
		
		List<PartyStruct> partyStructs = partyStructManager.find(
		        "from PartyStruct where parentEntity=? order by priority",
		        partyEntity);
		
		for (PartyStruct partyStruct : partyStructs) {
			if (partyStruct.getPartyStructType().getId() == partyStructTypeId) {
				PartyEntity childPartyEntity = partyStruct.getChildEntity();
				if(childPartyEntity.getPartyType().getId().equals(PartyConstants.PARTY_TYPE_POST))
						continue;
				
				if(childPartyEntity.getPartyType().getId().equals(PartyConstants.PARTY_TYPE_USER)){
					if(partyIdList.contains(childPartyEntity.getId().toString())) 
						continue;
				}
				else{
					if(!partyUpperAndLowerIdList.contains(childPartyEntity.getId().toString()))
						continue;
				}				
				
				// System.out.println(childPartyEntity.getId() + "-" + childPartyEntity.getName() + "-" + childPartyEntity.getPartyType().getType() );
				if (childPartyEntity == null) {
					logger.debug("child party entity is null");
					continue;
				}
				
				if (childPartyEntity.getDelFlag().equals(PersonInfoConstants.DELETE_FLAG_YES)) {
		            logger.debug("child party entity is delete");
		            continue;
		        }
				
				if(!childPartyEntity.getPartyType().getId().equals(PartyConstants.PARTY_TYPE_USER)){
					//判断是否显示
					String isDisplay = childPartyEntity.getIsDisplay();
					if (isDisplay == null ||isDisplay.equals("0"))
					{
			            logger.debug("child party entity is hidden");
			            continue;
			        }
					
				}
	            /*if (auth) {
	                	viewIsPostMap(partyEntity, list, notViewPost, childPartyEntity);
	            } else {
	            	viewIsPostMap(partyEntity, list, notViewPost, childPartyEntity);
	            }*/
	            
	            generateReportMap(partyEntity,list,childPartyEntity,partyIdList);
			}
		}
		//}
	}
    
    private void generateReportMap(PartyEntity partyEntity, List<Map> list,
			PartyEntity childPartyEntity,List<String> partyIdList) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("id", childPartyEntity.getId());
		map.put("pId", partyEntity.getId());
		/*if (childPartyEntity.getPartyType().getType() == PartyConstants.TYPE_POSITION) {
			map.put("name", "<span style='color:blue;margin-right:0px;'>" + childPartyEntity.getName() + "</span>");
		} else {
			map.put("name", childPartyEntity.getName());
		}*/
		map.put("name", childPartyEntity.getName());
		map.put("title", childPartyEntity.getName());
		map.put("ref", childPartyEntity.getRef());
		map.put("open", true);
		Set<PartyStruct> child = childPartyEntity.getChildStructs();
		int i = 0;
		if (child != null && child.size() > 0) {
			
			for (PartyStruct struct : child) {
				PartyEntity vo = struct.getChildEntity();
				
				/*if(vo.getPartyType().getId().equals(PartyConstants.PARTY_TYPE_USER)){
					if(partyIdList.contains(vo.getId().toString())) 
						continue;
				}*/
				
				/*if (!notViewPost) {  // 显示全部
					if (vo.getPartyType().getType() != PartyConstants.TYPE_USER && vo.getDelFlag().equals(PersonInfoConstants.DELETE_FLAG_NO)) {
						i =1;
						break;
					}
				} else {
					if ((vo.getPartyType().getType() != PartyConstants.TYPE_USER && vo.getPartyType().getType() != PartyConstants.TYPE_POSITION)&& vo.getDelFlag().equals(PersonInfoConstants.DELETE_FLAG_NO)) {
						i =1;
						break;
					}
				}*/
				
				if (vo.getDelFlag().equals(PersonInfoConstants.DELETE_FLAG_NO)) {
					i =1;
					break;
				}
			}
			if (i == 1) {
				map.put("isParent", true);
			} else {
				map.put("isParent", false);
			}
		} else {
			map.put("isParent", false);
		}
		list.add(map);
	}
    //endregion
	
	 private String convertString(Object value) {
	        if (value == null) {
	            return "";
	        }

	        if (value instanceof String) {
	            return (String) value;
	        }

	        return value.toString();
	    }
	
    // ~ ==================================================
    @Resource
    public void setPartyEntityManager(PartyEntityManager partyEntityManager) {
        this.partyEntityManager = partyEntityManager;
    }

    @Resource
    public void setPartyStructManager(PartyStructManager partyStructManager) {
        this.partyStructManager = partyStructManager;
    }

    @Resource
    public void setPartyTypeManager(PartyTypeManager partyTypeManager) {
        this.partyTypeManager = partyTypeManager;
    }

    @Resource
    public void setPartyStructTypeManager(
            PartyStructTypeManager partyStructTypeManager) {
        this.partyStructTypeManager = partyStructTypeManager;
    }

    @Resource
    public void setUserConnector(UserConnector userConnector) {
        this.userConnector = userConnector;
    }

    @Resource
    public void setPartyService(PartyService partyService) {
        this.partyService = partyService;
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
    public void setMessageHelper(MessageHelper messageHelper) {
        this.messageHelper = messageHelper;
    }

	@Resource
	public void setAccountInfoManager(AccountInfoManager accountInfoManager) {
		this.accountInfoManager = accountInfoManager;
	}
    
	@Resource
	public void setPersonInfoManager(PersonInfoManager personInfoManager) {
		this.personInfoManager = personInfoManager;
	}
	
	@Resource
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
	
	@Resource
	public void setCustomService(CustomService customService) {
		this.customService = customService;
	}
	
    @Resource
    public void setOperationService(OperationService operationService) {
        this.operationService = operationService;
    }
    
    @Resource
    public void setDictInfoManager(DictInfoManager dictInfoManager) {
        this.dictInfoManager = dictInfoManager;
    }
    
    @Resource
    public void setUserService(UserService userService) {
        this.userService = userService;
    }
    
    @Resource
    public void setUpdatePersonManager(UpdatePersonManager updatePersonManager) {
        this.updatePersonManager = updatePersonManager;
    }
    
    @Resource
    public void setOrgService(OrgService orgService) {
        this.orgService = orgService;
    }
    
    @Resource
    public void setRosterLogManager(RosterLogManager rosterLogManager) {
        this.rosterLogManager = rosterLogManager;
    }
    
    @Resource
    public void setPersonInfoService(PersonInfoService personInfoService) {
        this.personInfoService = personInfoService;
    }
    @Resource
    public void setAttendanceEntityManager(AttendanceEntityManager attendanceEntityManager) {
    	this.attendanceEntityManager = attendanceEntityManager;
    }

	public SimpleDateFormat getSdf() {
		return sdf;
	}

	public void setSdf(SimpleDateFormat sdf) {
		this.sdf = sdf;
	}

	public ExportUtil getExportUtil() {
		return exportUtil;
	}

	public void setExportUtil(ExportUtil exportUtil) {
		this.exportUtil = exportUtil;
	}
	
	@Resource
	public void setOrgLogManager(OrgLogManager orgLogManager) {
	this.orgLogManager = orgLogManager;
	}
    
}
