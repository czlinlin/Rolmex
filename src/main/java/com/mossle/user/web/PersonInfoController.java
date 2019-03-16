package com.mossle.user.web;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.util.StringUtil;
import org.apache.xmlbeans.impl.util.Base64;
import org.apache.xmlbeans.impl.xb.xsdschema.impl.NumFacetImpl;
import org.codehaus.janino.Java;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Joiner;
import com.mossle.api.dict.DictConnector;
import com.mossle.api.humantask.HumanTaskConnector;
import com.mossle.api.keyvalue.FormParameter;
import com.mossle.api.keyvalue.KeyValueConnector;
import com.mossle.api.org.OrgConnector;
import com.mossle.api.party.PartyConnector;
import com.mossle.api.party.PartyDTO;
import com.mossle.api.party.PartyEntityOrgDTO;
import com.mossle.api.process.ProcessConnector;
import com.mossle.api.tenant.TenantHolder;
import com.mossle.api.user.PersonInfoDTO;
import com.mossle.api.user.RosterLogDTO;
import com.mossle.auth.RoleConstants;
import com.mossle.auth.persistence.domain.AttendanceRecords;
import com.mossle.auth.persistence.domain.PersonAttendanceRecords;
import com.mossle.auth.persistence.domain.SpecialDate;
import com.mossle.base.persistence.domain.BusinessTypeEntity;
import com.mossle.core.annotation.Log;
import com.mossle.core.auth.CurrentUserHolder;
import com.mossle.core.excel.ReadExcel;
import com.mossle.core.export.ExcelExport;
import com.mossle.core.export.Exportor;
import com.mossle.core.export.TableModel;
import com.mossle.core.id.IdGenerator;
import com.mossle.core.mapper.BeanMapper;
import com.mossle.core.mapper.JsonMapper;
import com.mossle.core.page.Page;
import com.mossle.core.query.PropertyFilter;
import com.mossle.core.query.PropertyFilterUtils;
import com.mossle.core.spring.MessageHelper;
import com.mossle.dict.persistence.domain.DictInfo;
import com.mossle.dict.persistence.manager.DictInfoManager;
import com.mossle.operation.persistence.domain.CustomApprover;
import com.mossle.operation.persistence.domain.CustomEntity;
import com.mossle.operation.persistence.domain.CustomEntityDTO;
import com.mossle.operation.persistence.manager.CustomApproverManager;
import com.mossle.operation.persistence.manager.CustomManager;
import com.mossle.operation.service.CustomService;
import com.mossle.operation.service.CustomWorkService;
import com.mossle.operation.service.OperationService;
import com.mossle.party.PartyConstants;
import com.mossle.party.persistence.domain.PartyEntity;
import com.mossle.party.persistence.domain.PartyStruct;
import com.mossle.party.persistence.domain.PartyStructType;
import com.mossle.party.persistence.manager.PartyEntityManager;
import com.mossle.party.persistence.manager.PartyStructManager;
import com.mossle.party.persistence.manager.PartyStructTypeManager;
import com.mossle.project.rs.ProjectResource;
import com.mossle.user.persistence.domain.AccountCredential;
import com.mossle.user.persistence.domain.PersonContractCompanyManage;
//import com.mossle.user.persistence.domain.PersonAuth;
import com.mossle.user.persistence.domain.PersonInfo;
import com.mossle.user.persistence.domain.PersonInfoDTOForExport;
import com.mossle.user.persistence.domain.PersonWorkNumber;
import com.mossle.user.persistence.domain.RosterLog;
import com.mossle.user.persistence.domain.UpdatePerson;
import com.mossle.user.persistence.manager.AccountCredentialManager;
import com.mossle.user.persistence.manager.AccountInfoManager;
import com.mossle.user.persistence.manager.ContractCompanyManager;
//import com.mossle.user.persistence.manager.PersonAuthManager;
import com.mossle.user.persistence.manager.PersonInfoManager;
import com.mossle.user.persistence.manager.PersonWorkNumberManager;
import com.mossle.user.persistence.manager.RosterLogManager;
import com.mossle.user.persistence.manager.UpdatePersonManager;
import com.mossle.user.service.PersonInfoService;
import com.mossle.user.service.UserService;
import com.mossle.worktask.persistence.domain.WorkTaskInfo;
import com.mossle.worktask.persistence.domain.WorkTaskInfoInstance;
import com.mossle.worktask.persistence.manager.WorkTaskInfoManager;
import com.mossle.worktask.rs.WorkTaskResource;
import com.mossle.core.export.ExcelExport;
import com.mossle.core.export.Exportor;
import com.mossle.core.export.TableModel;
import com.mossle.core.query.PropertyFilter;
import com.mossle.core.spring.MessageHelper;

@Controller
@RequestMapping("user")
public class PersonInfoController {

    private static Logger logger = LoggerFactory.getLogger(PersonInfoController.class);

    private AccountInfoManager accountInfoManager;
    private PersonInfoManager personInfoManager;
    private MessageHelper messageHelper;
    private Exportor exportor;
    private TenantHolder tenantHolder;
    private PartyStructTypeManager partyStructTypeManager;
    private JdbcTemplate jdbcTemplate;
    private CurrentUserHolder currentUserHolder;
    private PartyEntityManager partyEntityManager;
    private PartyStructManager partyStructManager;
    private PersonInfoService personInfoService;
    private ProcessConnector processConnector;
    private HumanTaskConnector humanTaskConnector;
    private AccountCredentialManager accountCredentialManager;
    private DictConnector dictConnector;
    private WorkTaskResource workTaskResource;
    private ProjectResource projectResource;
    private UserService userService;
    private OperationService operationService;
    
    private ContractCompanyManager contractCompanyManager;
    
    
   /* @Resource
    private PersonAuthManager personAuthManager;*/

    private String strIds = "";
    private Map<String, String> aliasMap = new HashMap<String, String>();
    
    private JsonMapper jsonMapper = new JsonMapper();
    
    private UpdatePersonManager updatePersonManager ;
    
    private PartyConnector partyConnector;
    private IdGenerator idGenerator;
    SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
    
    private BeanMapper beanMapper = new BeanMapper();
    @Autowired
    private CustomWorkService customWorkService;
    @Autowired
    private DictInfoManager dictInfoManager;
    
    /**
     * 在职人员列表展示
     *
     * @param page
     * @param parameterMap
     * @param model
     * @param partyStructTypeId
     * @param partyEntityId
     * @return
     */
    @RequestMapping("person-info-list")
    @Log(desc = "人力资源", action = "查看", operationDesc = "人力资源-职员管理-在职人员-查看")
    public String list(@ModelAttribute Page page,
                       @RequestParam Map<String, Object> parameterMap, Model model,
                       @RequestParam(value = "partyStructTypeId", required = false) Long partyStructTypeId,
                       @RequestParam(value = "partyEntityId", required = false) Long partyEntityId) {

        Long accountId = Long.parseLong(currentUserHolder.getUserId());

        // 通过Id判断角色
        String sqlFindRoles = "SELECT R.id AS ROLE"
                + " FROM AUTH_USER_STATUS US,AUTH_USER_ROLE UR,AUTH_ROLE R"
                + " WHERE US.ID=UR.USER_STATUS_ID AND UR.ROLE_ID=R.ID"
                + " AND US.DEL_FLAG = '0' AND US.REF=? AND US.TENANT_ID=?";
       /* List<Map<String, Object>> roles = jdbcTemplate.queryForList(sqlFindRoles, accountId, "1");
        boolean isModify = false;
        if (roles != null && roles.size() > 0) {

            for (Map<String, Object> map : roles) {
                Long value = (Long) map.get("ROLE");

                if (value.equals(RoleConstants.SUPER_ADMIN_ID)) {
                    isModify = true;
                    break;
                }

                if (value.equals(RoleConstants.SYSTEM_ADMIN_ID)) {
                    isModify = true;
                    break;
                }
            }

            if (!isModify) {
                return "redirect:/portal/index.do";
            }
        } else {
            return "redirect:/portal/index.do";
        }*/
       
        String strRootNode="";
    	String strSql="Select partyEntityID from auth_orgdata where type='2' and union_id="+accountId;
    	List<String> rootNodeIdList=jdbcTemplate.queryForList(strSql, String.class);
    	if(rootNodeIdList!=null&&rootNodeIdList.size()>0)
    		strRootNode=rootNodeIdList.get(0);
    	
    	model.addAttribute("searchRootNode", strRootNode);
    	
    	model.addAttribute("isAdminRole",userService.getIsAdminRole(currentUserHolder.getUserId()));
        return "user/person-info-list";
    }

    /**
     * 在职人员列表查询
     *
     * @param page
     * @param parameterMap
     * @param model
     * @param partyStructTypeId
     * @param partyEntityId
     * @param isSearch          是否点击查询按钮
     * @return
     * @throws ParseException 
     */
    @RequestMapping("person-info-list-i")
    @Log(desc = "人力资源", action = "查看", operationDesc = "人力资源-职员管理-在职人员-查看")
    public String listi(@ModelAttribute Page page,
                        @RequestParam Map<String, Object> parameterMap, Model model,HttpServletRequest request,
                        @RequestParam(value = "partyStructTypeId", required = false) Long partyStructTypeId,
                        @RequestParam(value = "partyEntityId", required = false) Long partyEntityId,
                        @RequestParam(value = "isSearch", required = false) String isSearch) throws ParseException {
    	/*HttpSession session = request.getSession();
    	if(parameterMap.containsKey("filter_LIKES_p.FULL_NAME")){
    		session.setAttribute("query", parameterMap.get("filter_LIKES_p.FULL_NAME").toString());
    	}else{
    		parameterMap.put("filter_LIKES_p.FULL_NAME", session.getAttribute("query"));
    	}*/
    	String isOpenOtherName=userService.getOpenOtherNameStatus();
        model.addAttribute("isOpenOtherName", isOpenOtherName);
    	
    	Map<String, Object> map = this.convertAlias(parameterMap);
    	List<PropertyFilter> propertyFilters = PropertyFilter.buildFromMap(map);
        if (StringUtils.isBlank(isSearch)) {
            isSearch = "0";
            
            //filter_EQS_QUIT_FLAG
        }
        if(isSearch.equals("0")){
        	propertyFilters.add(new PropertyFilter("EQS_QUIT_FLAG", "0"));
        }
        
        String tenantId = tenantHolder.getTenantId();
        Long accountId = Long.parseLong(currentUserHolder.getUserId());

        // 维度，比如行政组织
        String hqlPartyStructType = "from PartyStructType where tenantId=? and display='true' order by priority";
        List<PartyStructType> partyStructTypes = partyStructTypeManager.find(hqlPartyStructType, tenantId);
        PartyStructType partyStructType = null;

        propertyFilters.add(new PropertyFilter("EQS_p.tenant_Id", tenantId));
        
        if(partyEntityId==null){
        	//partyEntityId=1L;
        	return "user/person-info-list-i";
        }
        	
        
       /* if (partyStructTypeId != null) {
            if (partyEntityId != null) {
                propertyFilters.add(new PropertyFilter("EQL_s.PARENT_ENTITY_ID", Long.toString(partyEntityId)));
            }
        } else {
            propertyFilters.add(new PropertyFilter("EQL_s.PARENT_ENTITY_ID", "-1"));
        }*/

        if (partyStructTypeId != null) {
            partyStructType = partyStructTypeManager.get(partyStructTypeId);
        } else {
            if (!partyStructTypes.isEmpty()) {
                // 如果没有指定维度，就使用第一个维度当做默认维度
                partyStructType = partyStructTypes.get(0);
                partyStructTypeId = partyStructType.getId();
            }
        }

        //region 根据条件查询
        //PartyEntity partyEntity = partyEntityManager.get(accountId);

        //String hql = "from PartyStruct where childEntity = ? and partyStructType = ?";

        //PartyStructType vo = partyStructTypeManager.get(PartyConstants.PARTY_STRUCT_TYPE_ORG);
        //PartyStruct partyStruct = partyStructManager.findUnique(hql, partyEntity, vo);

        //List<PartyEntity> list = new ArrayList<PartyEntity>();
        //list.add(partyStruct.getParentEntity());

        strIds = "";
        //if (!accountId.equals(PartyConstants.ADMIN_USER_ID)) {
        if ("1".equals(isSearch)) {  // 点击查询按钮，过滤权限
            /*List<Map> maps = generatePartyEntities(list, partyStructTypeId, accountId, false);
            getPartEntityIds(maps);
            strIds = strIds.substring(0, strIds.length() - 1);*/
            //String sqlString="select ID from party_entity where `NAME` like %"++"%";
        	//List<String> partyIdList=jdbcTemplate.queryForList(String.format(sqlString, partyEntityId), String.class);
        }
        //}
        //endregion
        
        /*if (accountId.equals(PartyConstants.ADMIN_USER_ID)) {
            page = pagedQuery(page, propertyFilters, strIds);
        } else {
            if (!PartyConstants.ROOT_PARTY_TREE_ID.equals(partyEntityId)) {  // 不是超级管理员，且没有点击根节点
                page = pagedQuery(page, propertyFilters, strIds);
            }
        }*/
        
        //如果不是超级管理员
        String strRootChildPartyIds="";
        String strPartyIds="";
        if(!PartyConstants.ADMIN_USER_ID.equals(accountId))
        {
        	if(partyEntityId.equals(PartyConstants.ROOT_PARTY_TREE_ID)){//如果是跟节点
        		//根节点点击是否显示数据
            	String strSql="Select partyEntityID from auth_orgdata where type='2' and union_id="+accountId;
            	List<String> rootNodeIdList=jdbcTemplate.queryForList(strSql, String.class);
            	String strRootNode="";
            	if(rootNodeIdList!=null&&rootNodeIdList.size()>0)
            		strRootNode=rootNodeIdList.get(0);
            	
            	if(!strRootNode.equals(partyEntityId.toString())){
            		strSql="SELECT CHILD_ENTITY_ID FROM party_struct s"
							+" INNER JOIN party_entity child on child.ID=s.CHILD_ENTITY_ID"
							+" WHERE PARENT_ENTITY_ID="+partyEntityId
							+ " and child.TYPE_ID='1'";
            		
            		List<String> rootChildPartIdList=jdbcTemplate.queryForList(strSql, String.class);
            		if(rootChildPartIdList!=null)
            			strRootChildPartyIds=Joiner.on(",").join(rootChildPartIdList);
            	} 
        	}
        	
        	//数据权限查询控制
	        List<String> partyIdList=null;
			String strSql="Select partyEntityID from auth_orgdata where type='1' and union_id="+accountId;
			partyIdList=jdbcTemplate.queryForList(strSql, String.class);
			if(partyIdList!=null)
				strPartyIds=Joiner.on(",").join(partyIdList);
			
			
			/*************************************************
			 * 去除虚拟账号
			 * 1.超级管理员虚拟人员-2
			 * 2.系统管理员-对应角色区分
			 * 3.经销商虚拟人员-4
			 * 4.机器人虚拟人员-3
			 * 5.测试用户
			 * ***********************************************/
			String strSerchRemoveId=PartyConstants.ADMIN_USER_ID+","
									+PartyConstants.SYSTEM_ROBOT_ID+","
									+PartyConstants.JXS_ID;
			PersonInfo personInfoTest=personInfoManager.findUniqueBy("username", "testuser");
			if(personInfoTest!=null)
				strSerchRemoveId+=","+personInfoTest.getId().toString();
			
			PersonInfo personInfoTest2=personInfoManager.findUniqueBy("username", "releasetest");
			if(personInfoTest2!=null)
				strSerchRemoveId+=","+personInfoTest2.getId().toString();
			
			String strSystemAdminIds="";
			List<String> systemAdminIdList=null;
			//查询属于角色ID为2(系统管理员)的所有用户ID
			strSql="SELECT US.ref FROM AUTH_USER_ROLE UR "
					+" INNER JOIN AUTH_USER_STATUS US ON UR.USER_STATUS_ID=US.ID"
					+" WHERE ROLE_ID=2";
			systemAdminIdList=jdbcTemplate.queryForList(strSql, String.class);
			if(systemAdminIdList!=null&&systemAdminIdList.size()>0)
				strSystemAdminIds=Joiner.on(",").join(systemAdminIdList);
			
			if(!strSystemAdminIds.equals(""))
				strSerchRemoveId+=","+strSystemAdminIds;
						
			if(!strRootChildPartyIds.equals("")&&!strSerchRemoveId.equals(""))
				strRootChildPartyIds+=","+strSerchRemoveId;
			else if(strRootChildPartyIds.equals("")&&!strSerchRemoveId.equals(""))
				strRootChildPartyIds+=strSerchRemoveId;
        }
		
		String strChildPartyIds="";
		List<String> childAllList=getAllDeparentById(partyEntityId);
		if(childAllList!=null)
			strChildPartyIds=Joiner.on(",").join(childAllList);
            
        Map<String,Object> mapResult= pagedQuery(page, propertyFilters, strIds,strPartyIds,strChildPartyIds,strRootChildPartyIds);
        
        page =(Page)mapResult.get("page");
        model.addAttribute("lockPersons", mapResult.get("lockPersons").toString());
        // 判断按钮是否可用

        /* boolean viewBtn = true;
        if (partyEntityId == null) {
            viewBtn = false;
        } else {
            PartyEntity pVo = partyEntityManager.get(partyEntityId);


            if (!accountId.equals(PartyConstants.ADMIN_USER_ID) && partyEntityId.equals(PartyConstants.ROOT_PARTY_TREE_ID)) {
                viewBtn = false;
            }

            // 超级管理员不允许建立分公司下的人员
            if (accountId.equals(PartyConstants.ADMIN_USER_ID) && pVo.getLevel() > 2) {
                viewBtn = false;
            }
        }*/
        
        //控制是否开启别名 :1开启 0关闭
        //List<DictInfo> dictInfos = dictConnector.findDictInfoListByType("isOpenOtherName");
        
        
        //查询此用户是否“系统管理员角色”（roleid=2），0：否，1：是
        String isSystemAdminRole="0";
		String strRoleSql="SELECT US.ref FROM AUTH_USER_ROLE UR "
				+" INNER JOIN AUTH_USER_STATUS US ON UR.USER_STATUS_ID=US.ID"
				+" WHERE ROLE_ID=2 AND US.ref="+currentUserHolder.getUserId();
		List<String> roleAdminIdList=jdbcTemplate.queryForList(strRoleSql, String.class);
		if(roleAdminIdList!=null&&roleAdminIdList.size()>0)
			isSystemAdminRole="1";
		model.addAttribute("isSystemAdminRole",isSystemAdminRole);
        model.addAttribute("page", page);
        model.addAttribute("param", parameterMap);
        model.addAttribute("partyStructTypes", partyStructTypes);
        model.addAttribute("partyStructType", partyStructType);
        model.addAttribute("partyStructTypeId", partyStructTypeId);
        model.addAttribute("partyEntityId", partyEntityId);
        PartyEntity partyEntity=partyEntityManager.findUniqueBy("id", partyEntityId);
        model.addAttribute("partyTypeId", partyEntity.getPartyType().getId());
        model.addAttribute("viewBtn", true);
        model.addAttribute("accountId", accountId);
        model.addAttribute("isSearch", isSearch);        
        
        return "user/person-info-list-i";
    }
    
    /*@RequestMapping("person-info-list-quit")
    @Log(desc = "人力资源", action = "查看", operationDesc = "人力资源-职员管理-在职人员-查看")
    public String listQuit(@ModelAttribute Page page,
                        @RequestParam Map<String, Object> parameterMap, Model model,HttpServletRequest request,
                        @RequestParam(value = "partyStructTypeId", required = false) Long partyStructTypeId,
                        @RequestParam(value = "partyEntityId", required = false) Long partyEntityId,
                        @RequestParam(value = "isSearch", required = false) String isSearch) throws ParseException {
    	HttpSession session = request.getSession();
    	if(parameterMap.containsKey("filter_LIKES_p.FULL_NAME")){
    		session.setAttribute("query", parameterMap.get("filter_LIKES_p.FULL_NAME").toString());
    	}else{
    		parameterMap.put("filter_LIKES_p.FULL_NAME", session.getAttribute("query"));
    	}
        if (StringUtils.isBlank(isSearch)) {
            isSearch = "0";
        }
        
        String tenantId = tenantHolder.getTenantId();
        Long accountId = Long.parseLong(currentUserHolder.getUserId());

        // 维度，比如行政组织
        String hqlPartyStructType = "from PartyStructType where tenantId=? and display='true' order by priority";
        List<PartyStructType> partyStructTypes = partyStructTypeManager.find(hqlPartyStructType, tenantId);
        PartyStructType partyStructType = null;

        Map<String, Object> map = this.convertAlias(parameterMap);

        List<PropertyFilter> propertyFilters = PropertyFilter.buildFromMap(map);

        propertyFilters.add(new PropertyFilter("EQS_p.tenant_Id", tenantId));
        if(partyEntityId==null){
        	//partyEntityId=1L;
        	return "user/person-info-list-i";
        }
        
        if (partyStructTypeId != null) {
            if (partyEntityId != null) {
                propertyFilters.add(new PropertyFilter("EQL_s.PARENT_ENTITY_ID", Long.toString(partyEntityId)));
            }
        } else {
            propertyFilters.add(new PropertyFilter("EQL_s.PARENT_ENTITY_ID", "-1"));
        }

        if (partyStructTypeId != null) {
            partyStructType = partyStructTypeManager.get(partyStructTypeId);
        } else {
            if (!partyStructTypes.isEmpty()) {
                // 如果没有指定维度，就使用第一个维度当做默认维度
                partyStructType = partyStructTypes.get(0);
                partyStructTypeId = partyStructType.getId();
            }
        }

        //region 根据条件查询
        //PartyEntity partyEntity = partyEntityManager.get(accountId);

        //String hql = "from PartyStruct where childEntity = ? and partyStructType = ?";

        //PartyStructType vo = partyStructTypeManager.get(PartyConstants.PARTY_STRUCT_TYPE_ORG);
        //PartyStruct partyStruct = partyStructManager.findUnique(hql, partyEntity, vo);

        //List<PartyEntity> list = new ArrayList<PartyEntity>();
        //list.add(partyStruct.getParentEntity());

        strIds = "";
        //if (!accountId.equals(PartyConstants.ADMIN_USER_ID)) {
        if ("1".equals(isSearch)) {  // 点击查询按钮，过滤权限
            List<Map> maps = generatePartyEntities(list, partyStructTypeId, accountId, false);
            getPartEntityIds(maps);
            strIds = strIds.substring(0, strIds.length() - 1);
            //String sqlString="select ID from party_entity where `NAME` like %"++"%";
        	//List<String> partyIdList=jdbcTemplate.queryForList(String.format(sqlString, partyEntityId), String.class);
        }
        //}
        //endregion
        
        if (accountId.equals(PartyConstants.ADMIN_USER_ID)) {
            page = pagedQuery(page, propertyFilters, strIds);
        } else {
            if (!PartyConstants.ROOT_PARTY_TREE_ID.equals(partyEntityId)) {  // 不是超级管理员，且没有点击根节点
                page = pagedQuery(page, propertyFilters, strIds);
            }
        }
        
        //如果不是超级管理员
        String strPartyIds="";
        if(!PartyConstants.ADMIN_USER_ID.equals(accountId))
        {
	        List<String> partyIdList=null;
			String strSql="Select partyEntityID from auth_orgdata where type='1' and union_id="+accountId;
			partyIdList=jdbcTemplate.queryForList(strSql, String.class);
			
			if(partyIdList!=null)
				strPartyIds=Joiner.on(",").join(partyIdList);
        }
		
		String strChildPartyIds="";
		List<String> childAllList=getAllDeparentById(partyEntityId);
		if(childAllList!=null)
			strChildPartyIds=Joiner.on(",").join(childAllList);
        
        page = pagedQuery(page, propertyFilters, strIds,strPartyIds,strChildPartyIds,is);


        // 判断按钮是否可用

        boolean viewBtn = true;
        if (partyEntityId == null) {
            viewBtn = false;
        } else {
            PartyEntity pVo = partyEntityManager.get(partyEntityId);


            if (!accountId.equals(PartyConstants.ADMIN_USER_ID) && partyEntityId.equals(PartyConstants.ROOT_PARTY_TREE_ID)) {
                viewBtn = false;
            }

            // 超级管理员不允许建立分公司下的人员
            if (accountId.equals(PartyConstants.ADMIN_USER_ID) && pVo.getLevel() > 2) {
                viewBtn = false;
            }
        }
        model.addAttribute("page", page);
        model.addAttribute("param", parameterMap);
        model.addAttribute("partyStructTypes", partyStructTypes);
        model.addAttribute("partyStructType", partyStructType);
        model.addAttribute("partyStructTypeId", partyStructTypeId);
        model.addAttribute("partyEntityId", partyEntityId);
        model.addAttribute("viewBtn", true);
        model.addAttribute("accountId", accountId);
        model.addAttribute("isSearch", isSearch);
        return "user/person-info-list-i";
    }*/
        
//    @RequestMapping("person-info-list-quit")
//    @Log(desc = "人力资源", action = "离职人员查看", operationDesc = "人力资源-职员管理-离职人员-查看")
//    public String listQuit(@ModelAttribute Page page,
//                        @RequestParam Map<String, Object> parameterMap, Model model,HttpServletRequest request,
//                        @RequestParam(value = "partyStructTypeId", required = false) Long partyStructTypeId,
//                        @RequestParam(value = "partyEntityId", required = false) Long partyEntityId,
//                        @RequestParam(value = "isSearch", required = false) String isSearch) throws ParseException {
//    	
//        if (StringUtils.isBlank(isSearch)) {
//            isSearch = "0";
//        }
//        
//        String tenantId = tenantHolder.getTenantId();
//        Long accountId = Long.parseLong(currentUserHolder.getUserId());
//
//        // 维度，比如行政组织
//        String hqlPartyStructType = "from PartyStructType where tenantId=? and display='true' order by priority";
//        List<PartyStructType> partyStructTypes = partyStructTypeManager.find(hqlPartyStructType, tenantId);
//        PartyStructType partyStructType = null;
//
//        Map<String, Object> map = this.convertAlias(parameterMap);
//
//        List<PropertyFilter> propertyFilters = PropertyFilter.buildFromMap(map);
//
//        propertyFilters.add(new PropertyFilter("EQS_p.tenant_Id", tenantId));
//        if(partyEntityId==null){
//        	//partyEntityId=1L;
//        	return "user/person-info-list-i";
//        }
//        
//        if (partyStructTypeId != null) {
//            partyStructType = partyStructTypeManager.get(partyStructTypeId);
//        } else {
//            if (!partyStructTypes.isEmpty()) {
//                // 如果没有指定维度，就使用第一个维度当做默认维度
//                partyStructType = partyStructTypes.get(0);
//                partyStructTypeId = partyStructType.getId();
//            }
//        }
//
//    
//        //如果不是超级管理员
//        String strPartyIds="";
//        if(!PartyConstants.ADMIN_USER_ID.equals(accountId))
//        {
//	        List<String> partyIdList=null;
//			String strSql="Select partyEntityID from auth_orgdata where type='1' and union_id="+accountId;
//			partyIdList=jdbcTemplate.queryForList(strSql, String.class);
//			
//			if(partyIdList!=null)
//				strPartyIds=Joiner.on(",").join(partyIdList);
//        }
//		
//		String strChildPartyIds="";
//		List<String> childAllList=getAllDeparentById(partyEntityId);
//		if(childAllList!=null)
//			strChildPartyIds=Joiner.on(",").join(childAllList);
//        
//        page = pagedQueryForQuit(page, propertyFilters, strIds,strPartyIds,strChildPartyIds);
//
//        model.addAttribute("page", page);
//        model.addAttribute("param", parameterMap);
//        model.addAttribute("partyStructTypes", partyStructTypes);
//        model.addAttribute("partyStructType", partyStructType);
//        model.addAttribute("partyStructTypeId", partyStructTypeId);
//        model.addAttribute("partyEntityId", partyEntityId);
//        model.addAttribute("viewBtn", true);
//        model.addAttribute("accountId", accountId);
//        model.addAttribute("isSearch", isSearch);
//        return "user/person-info-list-i";
//    }
    
    
    /**
     * 得到组织关系下所有子节点ID
     * @param partyEntityId
     * @return
     */
    private List<String> getAllDeparentById(Long partyEntityId) {
		
    	List<String> childAllList=new ArrayList<String>();
    	childAllList.add(partyEntityId.toString());    	
    	/*List<String> partyIdList=null;
		String strSql="Select partyEntityID from auth_orgdata where type='1' and union_id="+id;
		partyIdList=jdbcTemplate.queryForList(strSql, String.class);*/
    	
    	String sqlString="select CHILD_ENTITY_ID from party_struct where STRUCT_TYPE_ID=1 and PARENT_ENTITY_ID in(%s)";
    	List<String> partyIdList=jdbcTemplate.queryForList(String.format(sqlString, partyEntityId), String.class);
    	if(partyIdList!=null&&partyIdList.size()>0){
    		childAllList.addAll(partyIdList);
    		String strPartyIds=Joiner.on(",").join(partyIdList);
    		while(true){
    			List<String> childPartyIdList=jdbcTemplate.queryForList(String.format(sqlString,strPartyIds), String.class);
    			if(childPartyIdList!=null&&childPartyIdList.size()>0)
    			{
    				childAllList.addAll(childPartyIdList);
    				strPartyIds=Joiner.on(",").join(childPartyIdList);
    			}
    			else {
					break;
				}
        	}
    	}
    	return childAllList;
	}


    /**
     * 离职人员列表
     *
     * @param page
     * @param parameterMap
     * @param model
     * @param partyStructTypeId
     * @param partyEntityId
     * @param isSearch
     * @return
     */

    @RequestMapping("person-info-quit-list")
    @Log(desc = "人力资源", action = "查看", operationDesc = "人力资源-职员管理-离职人员-查看")
    public String quitListi(@ModelAttribute Page page,
                            @RequestParam Map<String, Object> parameterMap, Model model,
                            @RequestParam(value = "partyStructTypeId", required = false) Long partyStructTypeId,
                            @RequestParam(value = "partyEntityId", required = false) Long partyEntityId,
                            @RequestParam(value = "isSearch", required = false) boolean isSearch) {

        String tenantId = tenantHolder.getTenantId();
        Long accountId = Long.parseLong(currentUserHolder.getUserId());

        // 维度，比如行政组织
        String hqlPartyStructType = "from PartyStructType where tenantId=? and display='true' order by priority";
        List<PartyStructType> partyStructTypes = partyStructTypeManager.find(hqlPartyStructType, tenantId);
        PartyStructType partyStructType = null;

        Map<String, Object> map = this.convertAlias(parameterMap);

        List<PropertyFilter> propertyFilters = PropertyFilter.buildFromMap(map);

        propertyFilters.add(new PropertyFilter("EQS_p.tenant_Id", tenantId));

        if (partyStructTypeId != null) {
            partyStructType = partyStructTypeManager.get(partyStructTypeId);
        } else {
            if (!partyStructTypes.isEmpty()) {
                // 如果没有指定维度，就使用第一个维度当做默认维度
                partyStructType = partyStructTypes.get(0);
                partyStructTypeId = partyStructType.getId();
            }
        }

        PartyEntity partyEntity = partyEntityManager.get(accountId);

        String hql = "from PartyStruct where childEntity = ? and partyStructType = ?";

        PartyStructType vo = partyStructTypeManager.get(PartyConstants.PARTY_STRUCT_TYPE_ORG);
        PartyStruct partyStruct = partyStructManager.findUnique(hql, partyEntity, vo);

        List<PartyEntity> list = new ArrayList<PartyEntity>();
        list.add(partyStruct.getParentEntity());

        strIds = "";
        if (!accountId.equals(PartyConstants.ADMIN_USER_ID)) {
            List<Map> maps = generatePartyEntities(list, partyStructTypeId, accountId, false);
            getPartEntityIds(maps);
            strIds = strIds.substring(0, strIds.length() - 1);
        }

        // propertyFilters.add(new PropertyFilter("INS_s.PARENT_ENTITY_ID", strIds.substring(0,strIds.length()-1)));

        // logger.debug("{}", maps);
        if (isSearch) {
            page = pagedQueryQuit(page, propertyFilters, strIds);
        }

        model.addAttribute("page", page);
        model.addAttribute("param", parameterMap);
        model.addAttribute("partyStructTypes", partyStructTypes);
        model.addAttribute("partyStructType", partyStructType);
        model.addAttribute("partyStructTypeId", partyStructTypeId);
        model.addAttribute("partyEntityId", partyEntityId);
        model.addAttribute("accountId", accountId);

        return "user/person-info-quit-list";
    }


    @RequestMapping("person-info-account-input")
    @Log(desc = "人力资源", action = "查看", operationDesc = "人力资源-职员管理-离职人员-查看")
    public String accountInput(@RequestParam("code") String code) {
        PersonInfo personInfo = personInfoManager.findUniqueBy("code", code);

        if (personInfo == null) {
            personInfo = new PersonInfo();
            personInfo.setCode(code);
            personInfoManager.save(personInfo);
        }

        return "redirect:/user/person-info-input.do?id=" + personInfo.getId();
    }

    /**
     * 新增或编辑在职人员
     *
     * @param id
     * @param model
     * @param partyStructTypeId
     * @param partyEntityId
     * @return
     * @throws Exception 
     */
    @SuppressWarnings("deprecation")
	@RequestMapping("person-info-input")
    @Log(desc = "人力资源", action = "input", operationDesc = "人力资源-职员管理-新增编辑职员")
    public String input(@RequestParam(value = "id", required = false) Long id,
                        Model model, @RequestParam(value = "partyStructTypeId", required = false) Long partyStructTypeId,
                        @RequestParam(value = "partyEntityId", required = false) Long partyEntityId,
                        RedirectAttributes redirectAttributes) throws Exception {

        String tenantId = tenantHolder.getTenantId();

        PersonInfo personInfo = null;

        String hqlPartyStructType = "from PartyStructType where tenantId=? and display='true' order by priority";
        List<PartyStructType> partyStructTypes = partyStructTypeManager.find(hqlPartyStructType, tenantId);
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
            partyEntityId = PartyConstants.ROOT_PARTY_TREE_ID;
        }
        PartyEntity partyEntity = partyEntityManager.get(partyEntityId);

        // 手势开关
        String hql = "from AccountCredential where accountInfo.id = ?";

        if (id != null) {
            personInfo = personInfoManager.get(id);
            AccountCredential accountCredential = accountCredentialManager.findUnique(hql, id);
            if (accountCredential != null) {
                model.addAttribute("gestureSwitch", accountCredential.getGestureSwitch());
            } else {
                model.addAttribute("gestureSwitch", null);
            }
        } else {
            personInfo = new PersonInfo();
            model.addAttribute("gestureSwitch", null);
        }

        List<DictInfo> dictInfos = dictConnector.findDictInfoListByType("StaffPosition");
        //取户籍类型
        List<DictInfo> dictInfo_RegisterType = dictConnector.findDictInfoListByType("householdRegisterType");
        //取政治面貌
        List<DictInfo> dictInfo_politicalOutlook = dictConnector.findDictInfoListByType("politicalOutlook");
        //取民族
        List<DictInfo> dictInfo_nation = dictConnector.findDictInfoListByType("nation");
        //取学历
        List<DictInfo> dictInfo_education = dictConnector.findDictInfoListByType("education");
        //取学位
        List<DictInfo> dictInfo_academicDegree = dictConnector.findDictInfoListByType("academicDegree");
        //取用工类型
        List<DictInfo> dictInfo_laborType = dictConnector.findDictInfoListByType("laborType");
        //取进入方式
        List<DictInfo> dictInfo_entryMode = dictConnector.findDictInfoListByType("entryMode");
        //控制是否开启别名 :1开启 0关闭
        //List<DictInfo> dictInfo_otherName = dictConnector.findDictInfoListByType("isOpenOtherName");
     
        model.addAttribute("dictInfo_politicalOutlook", dictInfo_politicalOutlook); 
        model.addAttribute("dictInfo_RegisterType", dictInfo_RegisterType); 
        model.addAttribute("dictInfo_nation", dictInfo_nation); 
        model.addAttribute("dictInfo_education", dictInfo_education); 
        model.addAttribute("dictInfo_academicDegree", dictInfo_academicDegree); 
        model.addAttribute("dictInfo_laborType", dictInfo_laborType); 
        model.addAttribute("dictInfo_entryMode", dictInfo_entryMode); 
        
        
        if (id != null) {
            PartyEntity personEntity = partyEntityManager.get(id);
            List<PartyStruct> partyStructs = partyStructManager.find(
                    "from PartyStruct where childEntity = ? and partyStructType = ?",
                    personEntity, partyStructType);
            for (PartyStruct vo : partyStructs) {
                personInfo.setDepartmentCode(Long.toString(vo.getParentEntity().getId()));
                personInfo.setDepartmentName(vo.getParentEntity().getName());
            }
        }
        
        String employeeNo =userService.getWorkNumber();
		model.addAttribute("employeeNoNum", employeeNo);
		     
		 //处理级别
		if (personInfo.getLevel() != null && personInfo.getLevel().length()>0){
	        String level = personInfo.getLevel();
	        String[] s =  level.split("-");
	        String levelOne = s[0];
	        String levelTwo = s[1];
	        model.addAttribute("levelOne", levelOne);
	        model.addAttribute("levelTwo", levelTwo);
		}
		
		//取合同单位
		List<PersonContractCompanyManage> personContractCompanyManage =	contractCompanyManager.getAll();
		model.addAttribute("personContractCompanyManage",personContractCompanyManage);	
			
		//生成受理单编号
		String userId = currentUserHolder.getUserId();
		
    	String code =  operationService.CreateApplyCode(userId);
    	model.addAttribute("code",code);
        
    	String isAudit=userService.getAuditOpenStatus();
        model.addAttribute("isAudit", isAudit);
        
        String isValidate=userService.getValidateStatus();
        model.addAttribute("isValidate", isValidate);
        
        String isOpenOtherName=userService.getOpenOtherNameStatus();
        model.addAttribute("isOpenOtherName", isOpenOtherName);
        
        model.addAttribute("model", personInfo);
        model.addAttribute("dictInfos", dictInfos);
        model.addAttribute("partyStructTypes", partyStructTypes);
        model.addAttribute("partyStructType", partyStructType);
        model.addAttribute("partyStructTypeId", partyStructTypeId);
        model.addAttribute("partyEntityId", partyEntityId);
        model.addAttribute("partyEntity", partyEntity);
     
        //PartyEntityOrgDTO partyCompanyDTO=partyConnector.findCompanyInfoById(partyEntity.getId().toString());
        String strWorkNumberPrefix="";
        if(partyEntity!=null&&partyEntity.getId()!=null)
        	strWorkNumberPrefix=partyConnector.getWorkNumerPrefix(partyEntity.getId().toString());
        model.addAttribute("workNumberPrefix", strWorkNumberPrefix);
        return "user/person-info-input";
    }
    
    private String getWorkNumber() {
		String strNum="";
		String strSql="select number from person_WorkNumber where isUse='0' order by numberNo limit 0,1";
		String num=jdbcTemplate.queryForObject(strSql, String.class);
		if(num==null)
		{
			String strInsert="insert into person_WorkNumber(id,numberNo,isUse) values(%s,%s,'%s')";
			strSql="select numberNo from person_WorkNumber order by numberNo desc limit 0,1";
			num=jdbcTemplate.queryForObject(strSql, String.class);
			if(num==null) num="0";
			for(int i=1;i<100;i++)
			{
				jdbcTemplate.update(String.format(strInsert,idGenerator.generateId(),Integer.valueOf(num)+i),"0");
			}
			strNum=getWorkNumber();
		}
		else{
			strNum=num;
			if(strNum.length()==1)
				strNum="000"+strNum;
			else if(strNum.length()==2) 
				strNum="00"+strNum;
			else if(strNum.length()==3)
				strNum="0"+strNum;
		}
		return strNum;
	}
    
    
    /**
     * 展示花名册的日志
     *
     * @param id
     * @param model
     * @param partyStructTypeId
     * @param partyEntityId
     * @return
     * @throws Exception 
     */
    @SuppressWarnings("deprecation")
	@RequestMapping("person-info-rosterLogList")
    @Log(desc = "人力资源", action = "input", operationDesc = "人力资源-职员管理-展示花名册的日志")
    public String rosterLogList(@ModelAttribute Page page,
    					@RequestParam Map<String, Object> parameterMap,
    					@RequestParam(value = "id", required = false) Long id,
                        Model model, @RequestParam(value = "partyStructTypeId", required = false) Long partyStructTypeId,
                        @RequestParam(value = "partyEntityId", required = false) Long partyEntityId) throws Exception {

    	List<PropertyFilter> propertyFilters = PropertyFilter
                .buildFromMap(parameterMap);
    	page = pagedQueryForRosterLog(page, propertyFilters,id.toString());
       
       
        model.addAttribute("model", page);
        model.addAttribute("id", id);
   
        return "user/person-roster-log-list";
    }
    
      
    
    
    /**
     * 审核人查看花名册，跳转至花名册的详情页
     *
     * @param id
     * @param model
     * @param partyStructTypeId
     * @param partyEntityId
     * @return
     * @throws Exception 
     */
    @SuppressWarnings("deprecation")
	@RequestMapping("person-info-input-forConfirm")
    @Log(desc = "人力资源", action = "input", operationDesc = "人力资源-职员管理-审核人查看花名册的详情页")
    public String inputForConfirm(
    					@RequestParam(value = "applyCode", required = false) String applyCode
    					,@RequestParam(value = "id", required = false) Long id,
                        Model model, @RequestParam(value = "partyStructTypeId", required = false) Long partyStructTypeId,
                        @RequestParam(value = "partyEntityId", required = false) Long partyEntityId
                        ) throws Exception {

        String tenantId = tenantHolder.getTenantId();

        PersonInfo personInfo = null;
        
      //此处，取json串的内容展示给审核人
		List<UpdatePerson> updatePerson = updatePersonManager.findBy("applyCode", applyCode);
		
		 Object succesResponse = JSON.parse(updatePerson.get(0).getJsonContent());    //先转换成Object

	     Map map = (Map)succesResponse;
		
		//String map = updatePerson.get(0).getJsonContent();
		
		personInfo=jsonMapper.fromJson(map.get("personInfo").toString(),PersonInfo.class);
	
		String PersonTypeID = updatePerson.get(0).getTypeID();
		
		if(partyEntityId==null){
			partyEntityId = 0l;
		}
		
		PartyEntity partyEntity = partyEntityManager.get(partyEntityId);
		

        String hqlPartyStructType = "from PartyStructType where tenantId=? and display='true' order by priority";
        List<PartyStructType> partyStructTypes = partyStructTypeManager.find(hqlPartyStructType, tenantId);
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
//        if (partyEntityId == null) {
//            partyEntityId = PartyConstants.ROOT_PARTY_TREE_ID;
//        }
//        PartyEntity partyEntity = partyEntityManager.get(partyEntityId);

        // 手势开关
        String hql = "from AccountCredential where accountInfo.id = ?";

        if (id != null) {
            AccountCredential accountCredential = accountCredentialManager.findUnique(hql, id);
            if (accountCredential != null) {
                model.addAttribute("gestureSwitch", accountCredential.getGestureSwitch());
            } else {
                model.addAttribute("gestureSwitch", null);
            }
        } 

//        if (id != null) {
//            PartyEntity personEntity = partyEntityManager.get(id);
//            List<PartyStruct> partyStructs = partyStructManager.find(
//                    "from PartyStruct where childEntity = ? and partyStructType = ?",
//                    personEntity, partyStructType);
//            for (PartyStruct vo : partyStructs) {
//                personInfo.setDepartmentCode(Long.toString(vo.getParentEntity().getId()));
//                personInfo.setDepartmentName(vo.getParentEntity().getName());
//            }
//        }
        
        model.addAttribute("PersonTypeID", PersonTypeID);
        model.addAttribute("model", personInfo);

        model.addAttribute("partyStructTypes", partyStructTypes);
        model.addAttribute("partyStructType", partyStructType);
        model.addAttribute("partyStructTypeId", partyStructTypeId);
        model.addAttribute("partyEntityId", partyEntityId);
        model.addAttribute("partyEntity", partyEntity);

        return "user/person-info-input-forconfirm";
    }
    
    
    /**
     * 驳回流程发起人，重新调整申请，跳转至花名册的修改调整详情页
     *
     * @param id
     * @param model
     * @param partyStructTypeId
     * @param partyEntityId
     * @return
     * @throws Exception 
     */
    @SuppressWarnings("deprecation")
	@RequestMapping("person-info-input-forModify")
    @Log(desc = "人力资源", action = "input", operationDesc = "人力资源-职员管理-驳回流程发起人 跳转至花名册的修改调整详情页")
    public String inputForModify(
    					@RequestParam(value = "applyCode", required = false) String applyCode
    					,@RequestParam(value = "id", required = false) Long id,
                        Model model, @RequestParam(value = "partyStructTypeId", required = false) Long partyStructTypeId,
                        @RequestParam(value = "partyEntityId", required = false) Long partyEntityId) throws Exception {

        String tenantId = tenantHolder.getTenantId();
        
        PersonInfo personInfo = new PersonInfo();
        String typeID = "";
        
      //此处，取json串的内容展示给审核人
  		List<UpdatePerson> updatePerson = updatePersonManager.findBy("applyCode", applyCode);
  		
  		 Object succesResponse = JSON.parse(updatePerson.get(0).getJsonContent());    //先转换成Object

  	     Map map = (Map)succesResponse;
  		
  		 personInfo=jsonMapper.fromJson(map.get("personInfo").toString(),PersonInfo.class);
  	
  		 typeID = updatePerson.get(0).getTypeID();
    	
        String hqlPartyStructType = "from PartyStructType where tenantId=? and display='true' order by priority";
        List<PartyStructType> partyStructTypes = partyStructTypeManager.find(hqlPartyStructType, tenantId);
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
            partyEntityId = PartyConstants.ROOT_PARTY_TREE_ID;
        }
        PartyEntity partyEntity = partyEntityManager.get(partyEntityId);

        // 手势开关
        String hql = "from AccountCredential where accountInfo.id = ?";

        if (id != null) {
            AccountCredential accountCredential = accountCredentialManager.findUnique(hql, id);
            if (accountCredential != null) {
                model.addAttribute("gestureSwitch", accountCredential.getGestureSwitch());
            } else {
                model.addAttribute("gestureSwitch", null);
            }
        } 

        if (id != null) {
            PartyEntity personEntity = partyEntityManager.get(id);
            List<PartyStruct> partyStructs = partyStructManager.find(
                    "from PartyStruct where childEntity = ? and partyStructType = ?",
                    personEntity, partyStructType);
            for (PartyStruct vo : partyStructs) {
                personInfo.setDepartmentCode(Long.toString(vo.getParentEntity().getId()));
                personInfo.setDepartmentName(vo.getParentEntity().getName());
            }
        }
        
        List<DictInfo> dictInfos = dictConnector.findDictInfoListByType("StaffPosition");
        //取户籍类型
        List<DictInfo> dictInfo_RegisterType = dictConnector.findDictInfoListByType("householdRegisterType");
        //取政治面貌
        List<DictInfo> dictInfo_politicalOutlook = dictConnector.findDictInfoListByType("politicalOutlook");
        //取民族
        List<DictInfo> dictInfo_nation = dictConnector.findDictInfoListByType("nation");
        //取学历
        List<DictInfo> dictInfo_education = dictConnector.findDictInfoListByType("education");
        //取学位
        List<DictInfo> dictInfo_academicDegree = dictConnector.findDictInfoListByType("academicDegree");
        //取用工类型
        List<DictInfo> dictInfo_laborType = dictConnector.findDictInfoListByType("laborType");
        //取进入方式
        List<DictInfo> dictInfo_entryMode = dictConnector.findDictInfoListByType("entryMode");
        
        //取合同单位
      		List<PersonContractCompanyManage> personContractCompanyManage =	contractCompanyManager.getAll();
      		model.addAttribute("personContractCompanyManage",personContractCompanyManage);	
      		
        String isValidate=userService.getValidateStatus();
        model.addAttribute("isValidate", isValidate);
    
        model.addAttribute("dictInfo_politicalOutlook", dictInfo_politicalOutlook); 
        model.addAttribute("dictInfo_RegisterType", dictInfo_RegisterType); 
        model.addAttribute("dictInfo_nation", dictInfo_nation); 
        model.addAttribute("dictInfo_education", dictInfo_education); 
        model.addAttribute("dictInfo_academicDegree", dictInfo_academicDegree); 
        model.addAttribute("dictInfo_laborType", dictInfo_laborType); 
        model.addAttribute("dictInfo_entryMode", dictInfo_entryMode);
        
        
        model.addAttribute("dictInfos", dictInfos);
        model.addAttribute("applyCode", applyCode);
        model.addAttribute("typeID", typeID);
        model.addAttribute("model", personInfo);

        model.addAttribute("partyStructTypes", partyStructTypes);
        model.addAttribute("partyStructType", partyStructType);
        model.addAttribute("partyStructTypeId", partyStructTypeId);
        model.addAttribute("partyEntityId", partyEntityId);
        model.addAttribute("partyEntity", partyEntity);
        
        return "user/person-info-input-formodify";
    }
    
    
    
    /**
     * 进入权限编辑（暂时不用，先注掉）
     * @param model
     * @param userId
     * @return
     */
   /* @RequestMapping("person-auth-input")
    public String authPerson(Model model,@RequestParam(value = "userId", required = true) Long userId){
    	String strId = "";
    	String strName = "";
    	List<PersonAuth> persons = personAuthManager.findBy("userId", String.valueOf(userId));
    	for(PersonAuth p : persons){
    		strId += p.getDepartmentId()+",";
    		strName += p.getDepartmentName()+",";
    	}
    	if(strId.length()>0){
    		strId = strId.substring(0, strId.length()-1);
        	strName = strName.substring(0, strName.length()-1);
    	}
    	PartyEntity party = partyEntityManager.findUniqueBy("id", userId);
    	String name = party.getName();
    	model.addAttribute("id", userId);
    	model.addAttribute("departmentId", strId);
    	model.addAttribute("departmentName", strName);
    	model.addAttribute("name", name);
    	PersonAuth person = personAuthManager.findUniqueBy("userId", String.valueOf(userId));
    	PartyEntity party = partyEntityManager.findUniqueBy("id", userId);
    	String name = party.getName();
    	model.addAttribute("id", userId);
    	model.addAttribute("name", name);
    	if(person == null){
    		return "user/person-auth-input";
    	}
    	model.addAttribute("personAuth", person);
    	return "user/person-auth-input";
    }*/
    /**
     * 保存权限编辑（未启用）
     * @param userId
     * @param departmentId
     * @param departmentName
     * @return
     */
    /*@RequestMapping("person-auth-save")
    public String authPersonSave(@RequestParam(value = "userId", required = true) Long userId,
    		@RequestParam(value = "departmentId", required = false) String departmentId,
    		@RequestParam(value = "departmentName", required = false) String departmentName){
    	//PersonAuth person = personAuthManager.findUniqueBy("userId", String.valueOf(userId));
    	if(person == null){//表中未给其人配置权限
    		PersonAuth personNew = new PersonAuth();
    		personNew.setDepartmentId(departmentId);
    		personNew.setUserId(String.valueOf(userId));
    		
    		personNew.setDepartmentName(departmentName);
    		personNew.setTenantId("1");
    		personAuthManager.save(personNew);
    		return "user/person-info-list-i";
    	}else{
    		Long id = person.getId();
    		if(departmentId.equals("")){
    			personAuthManager.removeById(id);
    		}else{
    			person.setDepartmentId(departmentId);
        		person.setDepartmentName(departmentName);
        		personAuthManager.save(person);
    		}
    		
    		return "user/person-info-list-i";
    	}
    	//表中数据不以逗号分隔
    	List<PersonAuth> persons = personAuthManager.findBy("userId", String.valueOf(userId));
    	String []arrId = departmentId.split(",");
    	String []arrName = departmentName.split(",");
    	if(persons.size() == 0){
	    	for(int i = 0;i < arrId.length; i++){
	    		PartyEntity party = partyEntityManager.findUniqueBy("id", Long.parseLong(arrId[i]));
	    		if((party.getLevel() != 3 && party.getLevel() != 4) || party.getName().substring(2).equals("大区")){
	    			continue;
	    		}
	    		PersonAuth personNew = new PersonAuth();
	    		personNew.setDepartmentId(arrId[i]);
	    		personNew.setDepartmentName(arrName[i]);
	    		personNew.setUserId(String.valueOf(userId));
	    		personNew.setTenantId("1");
	    		personAuthManager.save(personNew);
	    	}
    	}else{
			for(PersonAuth p : persons){//删除表中已存数据
				personAuthManager.removeById(p.getId());
			}
			if(!departmentId.equals("")){
				for(int i = 0;i < arrId.length; i++){
		    		PersonAuth personNew = new PersonAuth();
		    		personNew.setDepartmentId(arrId[i]);
		    		personNew.setDepartmentName(arrName[i]);
		    		personNew.setUserId(String.valueOf(userId));
		    		personNew.setTenantId("1");
		    		personAuthManager.save(personNew);
		    	}
			}
    	}
    	return "user/person-info-list-i";
    }*/
    
    /**
     * 离职人员复职编辑页面
     *
     * @param id
     * @param model
     * @param partyStructTypeId
     * @param partyEntityId
     * @return
     */
    @RequestMapping("person-info-input-resume")
    @Log(desc = "人力资源", action = "resume", operationDesc = "人力资源-职员管理-离职人员复职编辑")
    public String inputResume(@RequestParam(value = "id", required = false) Long id,
                              Model model, @RequestParam(value = "partyStructTypeId", required = false) Long partyStructTypeId,
                              @RequestParam(value = "partyEntityId", required = false) Long partyEntityId) {

        String tenantId = tenantHolder.getTenantId();

        PersonInfo personInfo = null;

        String hqlPartyStructType = "from PartyStructType where tenantId=? and display='true' order by priority";
        List<PartyStructType> partyStructTypes = partyStructTypeManager.find(hqlPartyStructType, tenantId);
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
            partyEntityId = PartyConstants.ROOT_PARTY_TREE_ID;
        }
        PartyEntity partyEntity = partyEntityManager.get(partyEntityId);

        // 手势开关
        String hql = "from AccountCredential where accountInfo.id = ?";

        if (id != null) {
            personInfo = personInfoManager.get(id);
            PartyStruct partyStruct=partyStructManager.findUniqueBy("childEntity.id",id);
            personInfo.setDepartmentName(partyStruct.getParentEntity().getName());
            personInfo.setDepartmentCode(Long.toString(partyStruct.getParentEntity().getId()));
            AccountCredential accountCredential = accountCredentialManager.findUnique(hql, id);
            if (accountCredential != null) {
                model.addAttribute("gestureSwitch", accountCredential.getGestureSwitch());
            } else {
                model.addAttribute("gestureSwitch", null);
            }

        } else {
            personInfo = new PersonInfo();

            model.addAttribute("gestureSwitch", null);
        }
        List<DictInfo> dictInfos = dictConnector.findDictInfoListByType("StaffPosition");

        model.addAttribute("dictInfos", dictInfos);
        model.addAttribute("model", personInfo);

        model.addAttribute("partyStructTypes", partyStructTypes);
        model.addAttribute("partyStructType", partyStructType);
        model.addAttribute("partyStructTypeId", partyStructTypeId);
        model.addAttribute("partyEntityId", partyEntityId);
        // model.addAttribute("partyEntity", partyEntity);

        return "user/person-info-resume";
    }
    
    /**
     * 保存
     *
     * @param personInfo
     * @param password
     * @param confirmPassword
     * @param partyLevel
     * @param partyEntityId
     * @param redirectAttributes
     * @return
     * @throws Exception
     */
    @RequestMapping("person-info-save")
    @Log(desc = "人力资源", action = "保存", operationDesc = "人力资源-职员管理-在职人员-保存")
    public String save(HttpServletRequest request,
    				Model model, @ModelAttribute PersonInfo personInfo,
                       @RequestParam(value = "partyLevel", required = false) int partyLevel,
                       @RequestParam(value = "partyEntityId", required = false) Long partyEntityId,
                       @RequestParam(value="postId",required=false) Long postId,
                       RedirectAttributes redirectAttributes) throws Exception {

         String tenantId = tenantHolder.getTenantId();
        // Long accountId = Long.parseLong(currentUserHolder.getUserId());

    	//接下来为发起流程做准备（新职员录入走的是自定义申请的流程）--------------------------------------------------
         String userId = currentUserHolder.getUserId();

      //region 走流程
        
   
       //修改后的person实体转为json串 存起来，等审核通过后，用这个json串去更新personInfo对应的那条数据      
        Long accountId = Long.parseLong(currentUserHolder.getUserId());
        //当前id的一个实体
        PartyEntity parent = partyEntityManager.get(partyEntityId);
        //当前id的父点
        PartyStruct partyStructParent = partyStructManager.findUniqueBy("childEntity.id", partyEntityId);

        //通过职位的id翻译职位名称
        if(personInfo.getPositionCode()!=null &&personInfo.getPositionCode().length()>0){
        String sql = "select  f_GetPositionByDict("+personInfo.getPositionCode()+") as positionName ";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);
        personInfo.setPositionName(list.get(0).get("positionName").toString());
        }
        
      //通过合同单位的id翻译单位名称
        if(personInfo.getContractCompany()!=null &&personInfo.getContractCompany().length()>0){
        	PersonContractCompanyManage pccmCompanyManage =contractCompanyManager.get(Long.parseLong(personInfo.getContractCompany()));
        	personInfo.setContractCompany(pccmCompanyManage.getContractCompanyName());
        	personInfo.setContractCompanyID(pccmCompanyManage.getId().toString());
        }

        personInfo.setQuitFlag("0");
        personInfo.setDelFlag("0");
        personInfo.setTenantId(tenantId);
        personInfo.setAddTime(new Date());
        //将工号的部门和数字部分拼接起来 存入数据库
        String employeeNo = personInfo.getEmployeeNoDepart()+personInfo.getEmployeeNoNum();
        personInfo.setEmployeeNo(employeeNo);
        
        if (personInfo.getStopFlag() == null) {
            personInfo.setStopFlag("disabled");
        }

        if (personInfo.getSecret() == null) {
            personInfo.setSecret("0");
        }

        //查找当前部门id大区
        PartyDTO partyDTOArea = partyConnector.findAreaById(Long.toString(partyEntityId));
        //除分公司外添加职员
        if (partyDTOArea == null) {
            personInfo.setDepartmentCode(Long.toString(parent.getId()));
            personInfo.setDepartmentName(parent.getName());

            PartyDTO partyDTO = partyConnector.findCompanyById(Long.toString(parent.getId()));
            personInfo.setCompanyCode(partyDTO.getId());
            personInfo.setCompanyName(partyDTO.getName());
        } else {
            //分公司添加职员
            PartyEntity parentArea = partyEntityManager.findUniqueBy("id", Long.parseLong(partyDTOArea.getId()));
            PartyDTO partyDTO = partyConnector.findCompanyById(Long.toString(parentArea.getId()));
            personInfo.setCompanyCode(partyDTO.getId());
            personInfo.setCompanyName(partyDTO.getName());
            String parentId = Long.toString(partyStructParent.getParentEntity().getId());
            if (parentId.equals(partyDTOArea.getId())) {
                //上个点就是大区
                personInfo.setDepartmentCode(Long.toString(parent.getId()));
                personInfo.setDepartmentName(parent.getName());
            } else {
                //当前id的一个实体
                PartyEntity parentEntity = partyEntityManager.get(Long.parseLong(parentId));
                //上个点不是大区是分公司
                personInfo.setDepartmentCode(Long.toString(parentEntity.getId()));
                personInfo.setDepartmentName(parentEntity.getName());
            }
        }
        
        
 
    	
        // 先进行校验
       /* if (password != null) {
            if (!password.equals(confirmPassword)) {

                messageHelper.addMessage(model, "两次输入密码不符！");
                returnModel(model, personInfo, partyEntityId);

                return "user/person-info-input";
            }
        }*/

        // 验证员工编号
//        if (!this.checkEmployeeNo(personInfo.getEmployeeNo(), null)) {
//
//            messageHelper.addMessage(model, "员工编号已存在，请重新输入！");
//            returnModel(model, personInfo, partyEntityId);
//
//            return "user/person-info-input";
//        }

        // 验证登录名
        if (!this.checkUsername(personInfo.getUsername(), null)) {

            messageHelper.addMessage(model, "员工用户名已存在，请重新输入！");
            returnModel(model, personInfo, partyEntityId);
            return "user/person-info-input";
        }
        
        
		//String hsqlString="from PersonWorkNumber where numberNo=?";
		
		
		String isAudit=userService.getAuditOpenStatus();
		
        //如不调用流程，则直接进行保存操作
        boolean isProcess=isAudit.equals("1");
        if(isProcess){
        	
        	//接下来发起流程（修改职员信息走的是自定义申请的流程）--------------------------------------------------
            /*String content = " 花名册 录入新员工:"+personInfo.getUsername();

            String content = " 花名册 录入新员工:"+personInfo.getFullName();
            MultipartFile[] files = null;
            customService.StartProcessCustomForPerson(request,personInfo.getFullName(),content,"",files,"1");
        	//JSON记录添加职员需要的信息
            Map<String,Object> jsonMap=new HashMap<String, Object>();
            jsonMap.put("personInfo", personInfo);
            jsonMap.put("partyLevel",partyLevel);
            jsonMap.put("parentPartyEntityId", partyEntityId);
            jsonMap.put("postPartyEntityId", postId);
            jsonMap.put("accountId",Long.parseLong(currentUserHolder.getUserId()));
            String jsonString=jsonMapper.toJson(jsonMap);
            
            UpdatePerson updatePerson = new UpdatePerson();
      		updatePerson.setJsonContent(jsonString);
      		updatePerson.setApplyCode(request.getParameter("applyCode"));
      		updatePerson.setIsApproval("2");//1：流程审核同意   0：流程审核不同意    2:审核中 20180515 cz 新录入数据 初始化为不同意，待审核通过再更新为1
      		updatePerson.setUpdateParameters("");
      		updatePerson.setEmployeeNo("");
      		updatePerson.setTypeID("personadd");
    	  	updatePersonManager.save(updatePerson);*/
    	  	
    	  	personInfoService.insertPersonInfoForAudit(request, personInfo, partyLevel, partyEntityId, postId);
            messageHelper.addFlashMessage(redirectAttributes, "core.success.save", "录入员工流程审批申请成功，流程审核通过后生效");
            
        }
        else {
        	// 调业务层方法保存数据
            personInfoService.insertPersonInfo("0",personInfo, partyLevel, partyEntityId,postId,Long.parseLong(currentUserHolder.getUserId()));
            messageHelper.addFlashMessage(redirectAttributes, "core.success.save", "保存成功");
		}
    
        return "redirect:/user/person-info-list-i.do?partyEntityId=" + partyEntityId +
                "&partyStructTypeId=" + PartyConstants.PARTY_STRUCT_TYPE_ORG;
    }
 
    /**
     * 将对象注入Model
     *
     * @param model
     * @param personInfo
     * @param partyEntityId
     */
    private void returnModel(Model model, PersonInfo personInfo,
                             Long partyEntityId) {

        // PartyEntity partyEntity = partyEntityManager.get(partyEntityId);
        List<DictInfo> dictInfos = dictConnector.findDictInfoListByType("StaffPosition");

        model.addAttribute("model", personInfo);
        model.addAttribute("dictInfos", dictInfos);
        model.addAttribute("partyEntityId", partyEntityId);
        // model.addAttribute("partyEntity", partyEntity);
    }
  
    /**
     * 修改
     *
     * @param personInfo
     * @param password
     * @param confirmPassword
     * @param partyLevel
     * @param partyEntityId
     * @param redirectAttributes
     * @return
     * @throws Exception
     */
    @RequestMapping("person-info-update")
    @Log(desc = "人力资源", action = "修改", operationDesc = "人力资源-职员管理-在职人员-修改")
    public String update(HttpServletRequest request,
    					Model model, @ModelAttribute PersonInfo personInfo,
                         //@RequestParam(value = "password", required = false) String password,
                         //@RequestParam(value = "confirmPassword", required = false) String confirmPassword,
                         @RequestParam(value = "partyLevel", required = false) int partyLevel,
                         @RequestParam(value = "partyEntityId", required = false) Long partyEntityId,
                         @RequestParam(value = "gestureSwitch", required = false) String gestureSwitch,
                         RedirectAttributes redirectAttributes) throws Exception {

    	
        String tenantId = tenantHolder.getTenantId();
        Long accountId = Long.parseLong(currentUserHolder.getUserId());
        // 先进行校验
        /*if (password != null) {
            if (!password.equals(confirmPassword)) {

                messageHelper.addMessage(model, "两次输入密码不符！");
                returnModel(model, personInfo, partyEntityId);

                return "user/person-info-input";
            }
        }
*/

//        // 验证员工编号
//        if (!this.checkEmployeeNo(personInfo.getEmployeeNo(), personInfo.getId())) {
//
//            messageHelper.addMessage(model, "员工编号已存在，请重新输入！");
//            returnModel(model, personInfo, partyEntityId);
//
//            return "user/person-info-input";
//        }
//
//        // 验证登录名
//        if (!this.checkUsername(personInfo.getUsername(), personInfo.getId())) {
//
//            messageHelper.addMessage(model, "员工用户名已存在，请重新输入！");
//            returnModel(model, personInfo, partyEntityId);
//            return "user/person-info-input";
//        }

		
        String strhql="from UpdatePerson where typeID='personUpdate' and isApproval='2' and employeeNo=?";
    	List<UpdatePerson> updatePersonExistList=updatePersonManager.find(strhql,personInfo.getId().toString());
    	if(updatePersonExistList!=null&&updatePersonExistList.size()>0){
    		UpdatePerson updatePersonExist=updatePersonExistList.get(0);
    		messageHelper.addFlashMessage(redirectAttributes, "core.success.save", "此账号有修改流程存在，流程结束方能修改，具体单号："+updatePersonExist.getApplyCode());
    		return "redirect:/user/person-info-list-i.do?partyEntityId=" + partyEntityId +
                    "&partyStructTypeId=" + PartyConstants.PARTY_STRUCT_TYPE_ORG;
    	}
    	
    	//由在职转换为离职
    	String oldQuitFlag=request.getParameter("oldQuitFlag");
    	if(oldQuitFlag.equals("0")&&personInfo.getQuitFlag().equals("1")){
    		String strSqlString=String.format("SELECT DISTINCT r.applyCode FROM kv_record r"
									+" INNER JOIN task_info i on r.BUSINESS_KEY=i.BUSINESS_KEY"
									+" where i.ASSIGNEE='%s' and r.audit_status<>'2' and r.audit_status<>'3' and r.audit_status<>'6' and r.audit_status<>'8'",
									personInfo.getId().toString());// and i.CATALOG='start'
    		
    		//该职员发起的流程，未审核完成的，不允许离职
    		String strSearchSql=strSqlString+" and i.CATALOG='start'";
    		List<String> startProcessList=jdbcTemplate.queryForList(strSearchSql,String.class);
    		String applyNocompleteCodes="";
    		if(startProcessList!=null&&startProcessList.size()>0){
    			String applyCodes=Joiner.on(",").join(startProcessList);
    			applyNocompleteCodes+=applyCodes;
    			/*messageHelper.addFlashMessage(redirectAttributes, "core.success.save", "该人员有发起且未审核完成的流程，不允许离职，具体单号："+applyCodes);
	  	        return "redirect:/user/person-info-list-i.do?partyEntityId=" + partyEntityId +
	  	                "&partyStructTypeId=" + PartyConstants.PARTY_STRUCT_TYPE_ORG;*/
    		}
    		
    		//经该职员审批，但是流程未结束的，不允许离职
    		strSearchSql=strSqlString+" and i.CATALOG='normal' and i.status='complete'";
    		startProcessList=jdbcTemplate.queryForList(strSearchSql,String.class);
    		if(startProcessList!=null&&startProcessList.size()>0){
    			String applyCodes=Joiner.on(",").join(startProcessList);
    			if(!applyNocompleteCodes.equals("")){
    				applyNocompleteCodes+=",";
    			}
    			applyNocompleteCodes+=applyCodes;
    			/*messageHelper.addFlashMessage(redirectAttributes, "core.success.save", "该人员有审批过且未审核完成的流程，不允许离职，具体单号："+applyCodes);
	  	        return "redirect:/user/person-info-list-i.do?partyEntityId=" + partyEntityId +
	  	                "&partyStructTypeId=" + PartyConstants.PARTY_STRUCT_TYPE_ORG;*/
    		}
    		
    		//待办审批里面，有需要该职员进行审核的，不允许离职
    		strSearchSql=strSqlString+" and i.CATALOG='normal' and i.status='active'";
    		startProcessList=jdbcTemplate.queryForList(strSearchSql,String.class);
    		if(startProcessList!=null&&startProcessList.size()>0){
    			String applyCodes=Joiner.on(",").join(startProcessList);
    			if(!applyNocompleteCodes.equals("")){
    				applyNocompleteCodes+=",";
    			}
    			applyNocompleteCodes+=applyCodes;
    			/*messageHelper.addFlashMessage(redirectAttributes, "core.success.save", "该人员有待办审批的流程，不允许离职，具体单号："+applyCodes);
	  	        return "redirect:/user/person-info-list-i.do?partyEntityId=" + partyEntityId +
	  	                "&partyStructTypeId=" + PartyConstants.PARTY_STRUCT_TYPE_ORG;*/
    		}
    		
    		if(!applyNocompleteCodes.equals("")){
    			messageHelper.addFlashMessage(redirectAttributes, "core.success.save", "该人员有发起且未审核完成，审批过且未审核完成，待办审批的流程，不允许离职，具体单号："+applyNocompleteCodes);
	  	        return "redirect:/user/person-info-list-i.do?partyEntityId=" + partyEntityId +
	  	                "&partyStructTypeId=" + PartyConstants.PARTY_STRUCT_TYPE_ORG;
    		}
    		/*if(startProcessList!=null&&startProcessList.size()>0){
    			String applyCodes=Joiner.on(",").join(startProcessList);
    			messageHelper.addFlashMessage(redirectAttributes, "core.success.save", "该人员有发起且未审核完成的流程，不允许离职，具体单号："+applyCodes);
	  	        return "redirect:/user/person-info-list-i.do?partyEntityId=" + partyEntityId +
	  	                "&partyStructTypeId=" + PartyConstants.PARTY_STRUCT_TYPE_ORG;
    		}
    		
    		//经该职员审批，但是流程未结束的，不允许离职
    		strSearchSql=strSqlString+" and i.CATALOG='normal' and i.status='complete'";
    		startProcessList=jdbcTemplate.queryForList(strSearchSql,String.class);
    		if(startProcessList!=null&&startProcessList.size()>0){
    			String applyCodes=Joiner.on(",").join(startProcessList);
    			messageHelper.addFlashMessage(redirectAttributes, "core.success.save", "该人员有审批过且未审核完成的流程，不允许离职，具体单号："+applyCodes);
	  	        return "redirect:/user/person-info-list-i.do?partyEntityId=" + partyEntityId +
	  	                "&partyStructTypeId=" + PartyConstants.PARTY_STRUCT_TYPE_ORG;
    		}
    		
    		//待办审批里面，有需要该职员进行审核的，不允许离职
    		strSearchSql=strSqlString+" and i.CATALOG='normal' and i.status='active'";
    		startProcessList=jdbcTemplate.queryForList(strSearchSql,String.class);
    		if(startProcessList!=null&&startProcessList.size()>0){
    			String applyCodes=Joiner.on(",").join(startProcessList);
    			messageHelper.addFlashMessage(redirectAttributes, "core.success.save", "该人员有待办审批的流程，不允许离职，具体单号："+applyCodes);
	  	        return "redirect:/user/person-info-list-i.do?partyEntityId=" + partyEntityId +
	  	                "&partyStructTypeId=" + PartyConstants.PARTY_STRUCT_TYPE_ORG;
    		}*/

    	}
        
        //通过职位的id翻译职位名称
        if(personInfo.getPositionCode()!=null &&personInfo.getPositionCode().length()>0){
        String sql = "select  f_GetPositionByDict("+personInfo.getPositionCode()+") as positionName ";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);
        personInfo.setPositionName(list.get(0).get("positionName").toString());
        }
        
        //通过合同单位的id翻译单位名称
        if(personInfo.getContractCompany()!=null &&personInfo.getContractCompany().length()>0){
        	PersonContractCompanyManage pccmCompanyManage =contractCompanyManager.get(Long.parseLong(personInfo.getContractCompany()));
        	personInfo.setContractCompany(pccmCompanyManage.getContractCompanyName());
        	personInfo.setContractCompanyID(pccmCompanyManage.getId().toString());
        }
        
        personInfo.setDepartmentCode(Long.toString(partyEntityId));
        PartyDTO partyDTO = partyConnector.findCompanyById(Long.toString(partyEntityId));
        String companyId = "";
        String companyName = "";
        if (partyDTO != null) {
        	companyId = partyDTO.getId();
            companyName = partyDTO.getName();
        }
         
        personInfo.setCompanyCode(companyId);
        personInfo.setCompanyName(companyName);
        personInfo.setDelFlag("0");
        //personInfo.setQuitFlag("0");
        personInfo.setTenantId(tenantId);
        //更新员工编号
    	//将工号的部门和数字部分拼接起来 存入数据库
        String employeeNo = personInfo.getEmployeeNoDepart()+personInfo.getEmployeeNoNum();
         personInfo.setEmployeeNo(employeeNo);
         
         
//       //控制是否开启别名 :1开启 0关闭
// 		List<DictInfo> dictInfo_otherName = dictConnector.findDictInfoListByType("isOpenOtherName");
// 		dictInfo_otherName.get(0).getValue().equals("1"){
// 			personInfo.setRealName(realName);
// 			personInfo.set
// 		}
         
         
	      String isAudit=userService.getAuditOpenStatus();
	      if(isAudit.equals("1")){
	  			personInfoService.insertPersonInfoForUpdate(request, gestureSwitch, personInfo);
	  			messageHelper.addFlashMessage(redirectAttributes, "core.success.save", "修改信息流程发起成功，流程审核通过后生效");
	      }
	      else{
	    	  personInfoService.updatePersonInfo("0",personInfo, "", gestureSwitch,Long.valueOf(currentUserHolder.getUserId()));
	    	  messageHelper.addFlashMessage(redirectAttributes, "core.success.save", "保存成功");
	      }
        
        return "redirect:/user/person-info-list-i.do?partyEntityId=" + partyEntityId +
                "&partyStructTypeId=" + PartyConstants.PARTY_STRUCT_TYPE_ORG;
    }
    
 

    
//    public void updatePersonInfo(@ModelAttribute PersonInfo personInfo,
//             @RequestParam(value = "confirmPassword", required = false) String confirmPassword,
//             @RequestParam(value = "gestureSwitch", required = false) String gestureSwitch
//             ) throws Exception {
//  
//    		// 调业务层方法修改数据
//	    	personInfoService.updatePersonInfo(personInfo, confirmPassword, gestureSwitch,Long.valueOf(currentUserHolder.getUserId()));
//
//    	}
    

    /**
     * 流程被驳回到发起人，发起人重新调整申请，这里只保存personinfo的数据，不在这里发起流程， cz 20180524
     *
     * @param personInfo
     * @param password
     * @param confirmPassword
     * @param partyLevel
     * @param partyEntityId
     * @param redirectAttributes
     * @return
     * @throws Exception
     */
    @RequestMapping("person-info-update-ForModifySave")
    @Log(desc = "人力资源", action = "修改", operationDesc = "人力资源-职员管理-在职人员-修改")
    public String updateForPersonModifySave(HttpServletRequest request,
    					Model model, @ModelAttribute PersonInfo personInfo,
    					 @RequestParam(value = "typeID", required = false) String typeID,
                         @RequestParam(value = "applyCode", required = false) String applyCode,
                         @RequestParam(value = "partyLevel", required = false) int partyLevel,
                         @RequestParam(value = "partyEntityId", required = false) Long partyEntityId,
                         @RequestParam(value = "gestureSwitch", required = false) String gestureSwitch,
                         @RequestParam(value="postId",required=false) Long postId,
                         RedirectAttributes redirectAttributes) throws Exception {

        String tenantId = tenantHolder.getTenantId();
        Long accountId = Long.parseLong(currentUserHolder.getUserId());


        //通过职位的id翻译职位名称
        if(personInfo.getPositionCode()!=null&&personInfo.getPositionCode().length()>0){
	        String sql = "select  f_GetPositionByDict("+personInfo.getPositionCode()+") as positionName ";
	        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);
	        personInfo.setPositionName(list.get(0).get("positionName").toString());
        }
        
        //通过合同单位的id翻译单位名称
        if(personInfo.getContractCompany()!=null &&personInfo.getContractCompany().length()>0){
        	PersonContractCompanyManage pccmCompanyManage =contractCompanyManager.get(Long.parseLong(personInfo.getContractCompany()));
        	personInfo.setContractCompany(pccmCompanyManage.getContractCompanyName());
        	personInfo.setContractCompanyID(pccmCompanyManage.getId().toString());
        }
        
        personInfo.setDepartmentCode(Long.toString(partyEntityId));
        PartyDTO partyDTO = partyConnector.findCompanyById(Long.toString(partyEntityId));
        String companyId = "";
        String companyName = "";
        if (partyDTO != null) {
        	companyId = partyDTO.getId();
            companyName = partyDTO.getName();
        }
         if(personInfo.getDelFlag()==null){personInfo.setDelFlag("0");}
        if (personInfo.getStopFlag() == null) {personInfo.setStopFlag("disabled");}
        personInfo.setCompanyCode(companyId);
        personInfo.setCompanyName(companyName);
        personInfo.setTenantId(tenantId);
        //更新员工编号
    	//将工号的部门和数字部分拼接起来 存入数据库
        String employeeNo = personInfo.getEmployeeNoDepart()+personInfo.getEmployeeNoNum();
         personInfo.setEmployeeNo(employeeNo);
        
//         //若重新调整的是更新花名册  记录到日志中
//         if(typeID.equals("personUpdate")){
//        	 
//        	 //先删除，再创建
//        	 List<RosterLog> rLog = rosterLogManager.findBy("code", applyCode);
//        	 
//        	 for(RosterLog r:rLog){
//        		 rosterLogManager.remove(r);
//        	 }
//        
//        //对比两个类的属性，找出 修改了哪些内容
//	        PersonInfo dest = null;
//	        dest = personInfoManager.get(personInfo.getId());  
//		      
//	        Map<String, String> result = new HashMap<String, String>();
//		
//		      Field[] fs = personInfo.getClass().getDeclaredFields();
//		      for (Field f : fs) {
//		          f.setAccessible(true);
//		          Object v1 = f.get(personInfo);
//		          Object v2 = f.get(dest);
////		          if( ! equals(v1, v2)&& !f.getName().equals("addTime")&& !f.getName().equals("partyId")&& !f.getName().equals("isApproval")&& !f.getName().equals("employeeNoDepart")&& !f.getName().equals("positionCode")){
////		              result.put(f.getName(), String.valueOf(equals(v1, v2)));
////		         
//		           if( ! equals(v1, v2)&& !f.getName().equals("serialVersionUID")&&!f.getName().equals("addTime")&& !f.getName().equals("postId")&& !f.getName().equals("postName")&& !f.getName().equals("departmentCode")&& !f.getName().equals("companyCode")&& !f.getName().equals("partyId")&& !f.getName().equals("isApproval")&& !f.getName().equals("employeeNoDepart")&& !f.getName().equals("positionCode")){
//			              result.put(f.getName(), String.valueOf(equals(v1, v2)));
//			             
//		              
//		              if(
//		            		  (v1==null||(v1!=null&&v1.equals(""))) 
//		            		  &&
//		            		  (v2==null||(v2!=null&&v2.equals("")))
//		            	)
//		            		  {
//		            	  			continue;
//		            		  }
//		              
//		              if(v1!=null){
//		            	  if(v1.equals("")){
//	 	            		  v1="无";
//		            	  }
//		              }else {
//		            	  v1 ="无";
//		              }
//		              
//		              if(v2!=null){
//		            	  if(v2.equals("")){
//		            		  v2="无";
//		            	  }
//		              }else {
//		            	  v2 ="无";
//		              }
//		             
//		             if(f.getName().equals("gender")||f.getName().equals("fertilityCondition")||f.getName().equals("marriage")||f.getName().equals("quitFlag")){
//		  	            		  
//		            	Map<String,String> map =  convertGender(f.getName(),v1.toString(),v2.toString());
//		            	
//		            	v1 = map.get("v1");
//		            	
//		            	v2 = map.get("v2");
//		              }
//		              
//		              RosterLog rosterLog = new RosterLog ();
//				      
//		              rosterLog.setCode(request.getParameter("applyCode"));     					/** 受理单编号. */
//				      rosterLog.setOperationID(fullName); 		/** 操作人员id. */
//				      rosterLog.setContentBefore(v2.toString());  	/** 修改之前的内容. */
//				      rosterLog.setContentNew(v1.toString());			/** 修改后的新内容. */
//				      rosterLog.setUpdateColumn(f.getName()); 		/**被修改的字段名. */
//				      rosterLog.setUpdateColumnName(convertRosterLog(rosterLog));
//				      rosterLog.setIsapproval("1");//1：流程审核还未通过   0：流程审核通过    20180515 cz 新录入数据 初始化为未通过，待审核通过再更新为0
//				      
//				      rosterLog.setUpdateTime(new Date());  	  	/** 修改时间. */
//				      rosterLog.setEmployeeNo(personInfo.getId().toString());			/**被修改的员工编号. */
//		          
//				      rosterLogManager.save(rosterLog);
//		          }
//		      }
//         }

         
//         //修改了哪些内容，记录到content里面
//         String content = "";
//   		List<RosterLog> rostlog = rosterLogManager.findBy("code",request.getParameter("applyCode"));
//   		
// 			CustomEntity customEntity =	customManager.findUniqueBy("applyCode", request.getParameter("applyCode"));
// 			
// 			if(typeID.equals("personUpdate")){
//	 			customEntity.setTheme("  修改花名册  "
//// 			+personInfo.getFullName()
//	 					);
//	 			for(RosterLog r :rostlog){
//	 				 content = "员工编号："+personInfo.getEmployeeNo()+" , 姓名："+personInfo.getFullName() 
//	 						+"\n";
//					 content = content + " ["+r.getUpdateColumnName() + "] 由  \" "+ (r.getContentBefore().equals("")?"无":r.getContentBefore())+"\"   修改：   \""+r.getContentNew()+"\" \n";	
//				}
// 			}else if(typeID.equals("personadd")){
// 	 			customEntity.setTheme("  [花名册]新员工录入 "
//// 			+personInfo.getFullName()
// 	 					);
// 	 			content = " [花名册]录入新员工: "+personInfo.getFullName();	
// 	 		}
// 			customEntity.setApplyContent(content);
// 			customManager.save(customEntity);
 			
       
//          //修改后的person实体转为json串 存起来，等审核通过后，用这个json串去更新personInfo对应的那条数据      
// 			Map<String,Object> jsonMap=new HashMap<String, Object>();
//	      UpdatePerson updatePerson=updatePersonManager.findUniqueBy("applyCode", applyCode);
//	      if(typeID.equals("personUpdate")){
//      		
//      		
//      		jsonMap.put("confirmPassword","");
//      		jsonMap.put("gestureSwitch", gestureSwitch);
//      		
//	      }else if(typeID.equals("personadd")){
//	    	  personInfo.setDelFlag("0");
//	    	  personInfo.setQuitFlag("0");
//	          jsonMap.put("partyLevel",partyLevel);
//	          jsonMap.put("parentPartyEntityId", partyEntityId);
//	          jsonMap.put("postPartyEntityId", postId);
//		    }
//	      jsonMap.put("personInfo", personInfo);
//	      jsonMap.put("accountId", Long.valueOf(currentUserHolder.getUserId()));
//	      
//	      	String jsonString=jsonMapper.toJson(jsonMap);
//            updatePerson.setJsonContent(jsonString);
//      		updatePerson.setIsApproval("2");//1：流程审核同意   0：流程审核不同意    2:审核中 20180515 cz 新录入数据 初始化为不同意，待审核通过再更新为1
//      		updatePerson.setEmployeeNo(personInfo.getId()==null?"":personInfo.getId().toString());
//      		
	 
	  	//updatePersonManager.update(updatePerson);
	
         personInfoService.insertPersonInfoForModifySave(request, typeID, gestureSwitch, personInfo, partyLevel, partyEntityId, applyCode, postId);
         
         
        messageHelper.addFlashMessage(redirectAttributes, "core.success.save", "保存成功");

//        return "redirect:/user/person-info-list-i.do?partyEntityId=" + partyEntityId +
//                "&partyStructTypeId=" + PartyConstants.PARTY_STRUCT_TYPE_ORG;
//        
        
        return "redirect:/user/close-popwin-dialog.do?msgTip="+java.net.URLEncoder.encode(java.net.URLEncoder.encode("修改成功","utf-8"),"utf-8");
       
        
    }
    

    
    /**
     * 导出
     *
     * @param page
     * @param parameterMap
     * @param request
     * @param response
     * @throws Exception
     */
    @RequestMapping("person-info-export")
    @Log(desc = "人力资源", action = "导出", operationDesc = "人力资源-职员管理-在职人员-导出")
    public void export(@ModelAttribute Page page,
                       @RequestParam Map<String, Object> parameterMap,
                       HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        String tenantId = tenantHolder.getTenantId();
        List<PropertyFilter> propertyFilters = PropertyFilter
                .buildFromMap(parameterMap);
        propertyFilters.add(new PropertyFilter("EQS_tenantId", tenantId));
        page = personInfoManager.pagedQuery(page, propertyFilters);

        List<PersonInfo> personInfos = (List<PersonInfo>) page.getResult();

        TableModel tableModel = new TableModel();
        tableModel.setName("employee info");
        tableModel.addHeaders("id", "name");
        tableModel.setData(personInfos);
        exportor.export(request, response, tableModel);
    }

    /**
     * 启用
     *
     * @param id
     * @param partyEntityId
     * @param redirectAttributes
     * @return
     */
    @RequestMapping("person-info-active")
    @Log(desc = "人力资源", action = "修改", operationDesc = "人力资源-职员管理-在职人员-启用")
    public String active(@RequestParam("id") Long id, @RequestParam(value = "partyEntityId", required = false) Long partyEntityId,
                         RedirectAttributes redirectAttributes) throws Exception {

        PersonInfo personInfo = personInfoManager.get(id);
        personInfo.setStopFlag("active");

        personInfoService.updatePersonInfoStopFlag(personInfo);

        messageHelper.addFlashMessage(redirectAttributes,
                "core.success.update", "操作成功");


        return "redirect:/user/person-info-list-i.do?partyEntityId=" + partyEntityId +
                "&partyStructTypeId=" + PartyConstants.PARTY_STRUCT_TYPE_ORG;
    }

    /**
     * 停用
     *
     * @param id
     * @param partyEntityId
     * @param redirectAttributes
     * @return
     */
    @RequestMapping("person-info-disable")
    @Log(desc = "人力资源", action = "修改", operationDesc = "人力资源-职员管理-在职人员-停用")
    public String disable(@RequestParam("id") Long id, @RequestParam(value = "partyEntityId", required = false) Long partyEntityId,
                          RedirectAttributes redirectAttributes) throws Exception {
        PersonInfo personInfo = personInfoManager.get(id);
        personInfo.setStopFlag("disabled");

        personInfoService.updatePersonInfoStopFlag(personInfo);

        messageHelper.addFlashMessage(redirectAttributes,
                "core.success.update", "操作成功");

        return "redirect:/user/person-info-list-i.do?partyEntityId=" + partyEntityId +
                "&partyStructTypeId=" + PartyConstants.PARTY_STRUCT_TYPE_ORG;
    }


    /**
     * 权限设置
     *
     * @param id
     * @param partyEntityId
     * @param redirectAttributes
     * @return
     */
    @RequestMapping("person-info-secretmake")
    @Log(desc = "人力资源", action = "修改", operationDesc = "人力资源-职员管理-在职人员-通讯录权限")
    public String secretMake(@RequestParam("id") Long id, @RequestParam("partyEntityId") Long partyEntityId, @RequestParam("secret") String secret,
                             RedirectAttributes redirectAttributes) throws Exception {

        PersonInfo personInfo = personInfoManager.get(id);
        if (secret.equals("0")) {
            personInfo.setSecret("0");
        }
        if (secret.equals("1")) {
            personInfo.setSecret("1");
        }
        if (secret.equals("2")) {
            personInfo.setSecret("2");
        }
        personInfoService.updatePersonInfoSecret(personInfo);

        messageHelper.addFlashMessage(redirectAttributes,
                "core.success.update", "操作成功");


        return "redirect:/user/person-info-list.do?partyEntityId=" + partyEntityId +
                "&partyStructTypeId=" + PartyConstants.PARTY_STRUCT_TYPE_ORG;
    }


    /**
     * 删除
     *
     * @param selectedItem
     * @param partyEntityId
     * @param redirectAttributes
     * @return
     */
    @RequestMapping("person-info-remove")
    @Log(desc = "人力资源", action = "删除", operationDesc = "人力资源-职员管理-在职人员-删除")
    public String remove(@RequestParam("selectedItem") List<Long> selectedItem,
                         @RequestParam("partyEntityId") Long partyEntityId, RedirectAttributes redirectAttributes) throws Exception {

        String tenantId = tenantHolder.getTenantId();
        String strPersonNames = "";
        // String strResult = "";

        List<PersonInfo> personInfos = personInfoManager.findByIds(selectedItem);

        for (PersonInfo personInfo : personInfos) {

            // 查询是否存在未结流程
            Long count = processConnector.findRunningProcessInstances(Long.toString(personInfo.getId()), tenantId);
            if (count.longValue() > new Long(0)) {
                strPersonNames += personInfo.getFullName() + ",";
                // strResult = "存在未结流程";
            }

            // 查询是否存在待办流程
            Page page = humanTaskConnector.findPersonalTasks(Long.toString(personInfo.getId()), tenantId, 1, 10);
            if (page.getResultSize() > 0) {
                if (strPersonNames.indexOf(personInfo.getFullName()) == -1) {
                    strPersonNames += personInfo.getFullName() + ",";
                }
                /*if (StringUtils.isBlank(strResult)) {
                    strResult = "存在待办流程";
                } else {
                	strResult += "、待办流程";
                }*/
            }

            // 查询是否存在已发布和进行中负责的任务或项目
            boolean isWork = workTaskResource.judgeMethod(personInfo.getId());
            if (!isWork) {
                if (strPersonNames.indexOf(personInfo.getFullName()) == -1) {
                    strPersonNames += personInfo.getFullName() + ",";
                }
            }

            boolean isProject = projectResource.judgeMethod(personInfo.getId());
            if (!isProject) {
                if (strPersonNames.indexOf(personInfo.getFullName()) == -1) {
                    strPersonNames += personInfo.getFullName() + ",";
                }
            }
        }

        if (StringUtils.isBlank(strPersonNames)) {
            personInfoService.deletePersonInfo(selectedItem);

            messageHelper.addFlashMessage(redirectAttributes,
                    "core.success.delete", "删除成功");
        } else {
            messageHelper.addFlashMessage(redirectAttributes,
                    "core.success.delete", strPersonNames.substring(0, strPersonNames.length() - 1) + " 存在未完成的工作(未结流程、待办流程、负责的项目、负责的任务)，不允许删除！");
        }

        return "redirect:/user/person-info-list-i.do?partyEntityId=" + partyEntityId +
                "&partyStructTypeId=" + PartyConstants.PARTY_STRUCT_TYPE_ORG;
    }

    /**
     * 职员离职
     *
     * @param id
     * @param partyEntityId
     * @param redirectAttributes
     * @return
     */
    @RequestMapping("person-info-quit")
    @Log(desc = "人力资源", action = "修改", operationDesc = "人力资源-职员管理-在职人员-离职")
    public String quit(@RequestParam("id") Long id, @RequestParam("partyEntityId") Long partyEntityId,
                       RedirectAttributes redirectAttributes) throws Exception {

        personInfoService.quitPersonInfo(id);

        messageHelper.addFlashMessage(redirectAttributes,
                "core.success.delete", "操作成功");

        return "redirect:/user/person-info-list-i.do?partyEntityId=" + partyEntityId +
                "&partyStructTypeId=" + PartyConstants.PARTY_STRUCT_TYPE_ORG;
    }

    /**
     * 职员复职
     *
     * @param personInfo
     * @param password
     * @param confirmPassword
     * @param redirectAttributes
     * @return
     */
    @RequestMapping("person-info-resume")
    @Log(desc = "人力资源", action = "修改", operationDesc = "人力资源-职员管理-离职人员-复职")
    public String resume(Model model, @ModelAttribute PersonInfo personInfo,
                         @RequestParam("id") Long id,
                         @RequestParam(value = "password", required = false) String password,
                         @RequestParam(value = "confirmPassword", required = false) String confirmPassword,
                         @RequestParam(value = "gestureSwitch", required = false) String gestureSwitch,
                         @RequestParam(value = "partyEntityId", required = false) Long partyEntityId,
                         RedirectAttributes redirectAttributes) throws Exception {

        // 先进行校验
        if (password != null) {
            if (!password.equals(confirmPassword)) {

                messageHelper.addMessage(model, "两次输入密码不符！");
                returnModel(model, personInfo, partyEntityId);

                return "user/person-info-resume";
            }
        }

        // 验证员工编号
        if (!this.checkEmployeeNo(personInfo.getEmployeeNo(), id)) {

            messageHelper.addMessage(model, "员工编号已存在，请重新输入！");
            returnModel(model, personInfo, partyEntityId);

            return "user/person-info-resume";
        }

        // 验证登录名
        if (!this.checkUsername(personInfo.getUsername(), id)) {

            messageHelper.addMessage(model, "员工用户名已存在，请重新输入！");
            returnModel(model, personInfo, partyEntityId);
            return "user/person-info-resume";
        }

        // 调业务层方法保存数据
        personInfoService.resumePersonInfo(id, personInfo, confirmPassword, gestureSwitch);

        messageHelper.addFlashMessage(redirectAttributes, "core.success.save", "复职成功");

        return "redirect:/user/person-info-quit-list.do?isSearch=true";
    }

    /**
     * 取得登录用户所在机构的所有下级机构的Id
     *
     * @param maps
     */
    public void getPartEntityIds(List<Map> maps) {
        for (Map party : maps) {
            strIds += party.get("id") + ",";
            if (party.containsKey("children")) {
                getPartEntityIds((List<Map>) party.get("children"));
            }
        }
    }

    private Map<String,Object> pagedQuery(Page page, List<PropertyFilter> propertyFilters, String strIds,String strPartyIds,String strChildPartyIds,String strRootChildPartyIds) throws ParseException {

    	Map<String,Object> mapResult=new HashMap<String,Object>();
        String sqlPagedQuerySelect = "SELECT p.*,s.PARENT_ENTITY_ID,"
                + "f_GetPositionName(p.ID) as positionName,f_GetPositionByDict(p.POSITION_CODE) AS POSTIONbYdICT"
                //+ ",i.LOCKED"
                + " FROM person_info p "
                + " inner join party_entity e on p.party_id = e.ID "
                + " inner join party_struct s on e.ID =s.CHILD_ENTITY_ID "
                //+ " inner join account_info i on i.ID=p.ID "
                + " where p.id!=2 "
                + " and s.STRUCT_TYPE_ID = " + PartyConstants.PARTY_STRUCT_TYPE_ORG;

        String sqlPagedQueryCount = "SELECT COUNT(*) FROM person_info p "
                + "inner join party_entity e on p.party_id = e.ID "
                + "inner join party_struct s on e.ID =s.CHILD_ENTITY_ID "
                //+ " inner join account_info i on i.ID=p.ID "
                + " where p.id!=2"
                + " and s.STRUCT_TYPE_ID = " + PartyConstants.PARTY_STRUCT_TYPE_ORG;

         StringBuilder buff = new StringBuilder();
        List<Object> paramList = new ArrayList<Object>(); 
        boolean checkWhere = sqlPagedQuerySelect.toLowerCase().indexOf("where") == -1;
        
        for(PropertyFilter lp:propertyFilters){
        	String filterName =lp.getPropertyName();
        	if(filterName.toLowerCase().equals("real_name")){
        		String strValue=lp.getMatchValue().toString();
    			sqlPagedQuerySelect +=" and p.Real_Name like '%"+strValue+"%' ";
    			sqlPagedQueryCount +=" and p.Real_Name  like '%"+strValue+"%'";
    			propertyFilters.remove(lp);
        	}
        }
        
        PropertyFilterUtils.buildConfigurations(propertyFilters, buff,
                paramList, checkWhere);
        /*
        logger.debug("propertyFilters : {}", propertyFilters);
        logger.debug("buff : {}", buff);
        logger.debug("paramList : {}", paramList);
        logger.debug("checkWhere : {}", checkWhere);
		*/
        String sql = buff.toString();
        String countSql = "";
        String selectSql = "";
        if (!StringUtils.isEmpty(strPartyIds)) {
        	sqlPagedQuerySelect+=" and s.PARENT_ENTITY_ID in (" + strPartyIds + ")";
        	sqlPagedQueryCount+=" and s.PARENT_ENTITY_ID in (" + strPartyIds + ")";
        }
        
        if (!StringUtils.isEmpty(strChildPartyIds)) {
        	sqlPagedQuerySelect+=" and s.PARENT_ENTITY_ID in (" + strChildPartyIds + ")";
        	sqlPagedQueryCount+=" and s.PARENT_ENTITY_ID in (" + strChildPartyIds + ")";
        }
        
        if(!StringUtils.isEmpty(strRootChildPartyIds)){
        	sqlPagedQuerySelect+=" and s.CHILD_ENTITY_ID not in (" + strRootChildPartyIds + ")";
        	sqlPagedQueryCount+=" and s.CHILD_ENTITY_ID not in (" + strRootChildPartyIds + ")";
        }
        
        for(PropertyFilter lp:propertyFilters){
        	String filterName =lp.getPropertyName();
        	if(filterName.equals("QUIT_FLAG")){
        		String quit_flag=lp.getMatchValue().toString();
        		if(quit_flag.equals("0")){
        			sqlPagedQuerySelect += " and p.del_Flag  ='0' ";//在职，删除标识等于0
        			sqlPagedQueryCount += " and p.del_Flag  ='0' ";
        		}
        			
        	}
        }
        
        if (StringUtils.isEmpty(strIds)) {
            countSql = sqlPagedQueryCount + " " + sql;   //排序  EMPLOYEE_NO  priority
            selectSql = sqlPagedQuerySelect + " " + sql + " ORDER BY EMPLOYEE_NO limit " + page.getStart() + "," + page.getPageSize();
        } else {
            countSql = sqlPagedQueryCount + " " + sql + " and s.PARENT_ENTITY_ID in (" + strIds + ")";
            selectSql = sqlPagedQuerySelect + " " + sql + " and s.PARENT_ENTITY_ID in (" + strIds + ") ORDER BY EMPLOYEE_NO limit "
                    + page.getStart() + "," + page.getPageSize();
        }

        logger.debug("countSql : {}", countSql);
        logger.debug("selectSql : {}", selectSql);

        Object[] params = paramList.toArray();
        int totalCount = jdbcTemplate.queryForObject(countSql, Integer.class,
                params);
        List<Map<String, Object>> list = jdbcTemplate.queryForList(selectSql,
                params);
        List<PersonInfoDTO> personDtos = new ArrayList<PersonInfoDTO>();

        String lockPersons="";
        StringBuffer sbSearchPersonIds=new StringBuffer();
        for (Map<String, Object> map : list) {
            PersonInfoDTO personDTO = convertPersonDTO(map);
            personDTO.setCompanyCode(convertString(map.get("COMPANY_CODE")));
            personDTO.setCompanyName(convertString(map.get("COMPANY_NAME")));
            personDTO.setPositionCode(convertString(map.get("positionName")));
            personDTO.setPositionName(convertString(map.get("POSTIONbYdICT")));
            personDtos.add(personDTO);
            sbSearchPersonIds.append(String.format("%s,",personDTO.getId().toString()));
            /*if(map.get("LOCKED")!=null&&map.get("LOCKED").toString().toLowerCase().equals("locked")){
            	lockPersons+=personDTO.getId()+",";
            }*/
        }
        page.setTotalCount(totalCount);
        page.setResult(personDtos);
        mapResult.put("page", page);
        
      //查询本页面已锁定的人员信息
        if(sbSearchPersonIds.length()>0)
        {
            String strSql=String.format("Select ID from account_info where LOWER(LOCKED)='locked' and ID in(%s)"
            		,sbSearchPersonIds.substring(0, sbSearchPersonIds.length()-1).toString());
            List<String> lockPersonList=jdbcTemplate.queryForList(strSql, String.class);
            if(lockPersonList!=null&&lockPersonList.size()>0)
            	lockPersons=Joiner.on(",").join(lockPersonList);
        }
        
        mapResult.put("lockPersons", lockPersons);
        
        return mapResult;
    }
    
    
    //查询离职人员
    private Page pagedQueryForQuit(Page page, List<PropertyFilter> propertyFilters, String strIds,String strPartyIds,String strChildPartyIds) throws ParseException {

        String sqlPagedQuerySelect = "SELECT p.*,s.PARENT_ENTITY_ID,"
                + "f_GetPositionName(p.ID) as positionName,f_GetPositionByDict(p.POSITION_CODE) AS POSTIONbYdICT"
                + " FROM person_info p "
                + "inner join party_entity e on p.party_id = e.ID "
                + "inner join party_struct s on e.ID =s.CHILD_ENTITY_ID "
                + " where p.QUIT_FLAG ='1' and p.DEL_FLAG ='1' and s.STRUCT_TYPE_ID = " + PartyConstants.PARTY_STRUCT_TYPE_ORG;

        String sqlPagedQueryCount = "SELECT COUNT(*) FROM person_info p "
                + "inner join party_entity e on p.party_id = e.ID "
                + "inner join party_struct s on e.ID =s.CHILD_ENTITY_ID "
                + " where p.QUIT_FLAG ='1' and p.DEL_FLAG ='1' and s.STRUCT_TYPE_ID = " + PartyConstants.PARTY_STRUCT_TYPE_ORG;

         StringBuilder buff = new StringBuilder();
        List<Object> paramList = new ArrayList<Object>(); 
        boolean checkWhere = sqlPagedQuerySelect.toLowerCase().indexOf("where") == -1;
        PropertyFilterUtils.buildConfigurations(propertyFilters, buff,
                paramList, checkWhere);
        /*
        logger.debug("propertyFilters : {}", propertyFilters);
        logger.debug("buff : {}", buff);
        logger.debug("paramList : {}", paramList);
        logger.debug("checkWhere : {}", checkWhere);
		*/
        String sql = buff.toString();
        String countSql = "";
        String selectSql = "";
        if (!StringUtils.isEmpty(strPartyIds)) {
        	sqlPagedQuerySelect+=" and s.PARENT_ENTITY_ID in (" + strPartyIds + ")";
        	sqlPagedQueryCount+=" and s.PARENT_ENTITY_ID in (" + strPartyIds + ")";
        }
        
        if (!StringUtils.isEmpty(strChildPartyIds)) {
        	sqlPagedQuerySelect+=" and s.PARENT_ENTITY_ID in (" + strChildPartyIds + ")";
        	sqlPagedQueryCount+=" and s.PARENT_ENTITY_ID in (" + strChildPartyIds + ")";
        }
        
        
        if (StringUtils.isEmpty(strIds)) {
            countSql = sqlPagedQueryCount + " " + sql;
            selectSql = sqlPagedQuerySelect + " " + sql + " ORDER BY priority limit " + page.getStart() + "," + page.getPageSize();
        } else {
            countSql = sqlPagedQueryCount + " " + sql + " and s.PARENT_ENTITY_ID in (" + strIds + ")";
            selectSql = sqlPagedQuerySelect + " " + sql + " and s.PARENT_ENTITY_ID in (" + strIds + ") ORDER BY priority limit "
                    + page.getStart() + "," + page.getPageSize();
        }

        logger.debug("countSql : {}", countSql);
        logger.debug("selectSql : {}", selectSql);

        Object[] params = paramList.toArray();
        int totalCount = jdbcTemplate.queryForObject(countSql, Integer.class,
                params);
        List<Map<String, Object>> list = jdbcTemplate.queryForList(selectSql,
                params);
        List<PersonInfoDTO> personDtos = new ArrayList<PersonInfoDTO>();

        for (Map<String, Object> map : list) {
            PersonInfoDTO personDTO = convertPersonDTO(map);
            personDTO.setCompanyCode(convertString(map.get("COMPANY_CODE")));
            personDTO.setCompanyName(convertString(map.get("COMPANY_NAME")));
            personDTO.setPositionCode(convertString(map.get("positionName")));
            personDTO.setPositionName(convertString(map.get("POSTIONbYdICT")));
            personDtos.add(personDTO);
        }

        page.setTotalCount(totalCount);
        page.setResult(personDtos);

        return page;
    } 
    
    //查询花名册日志
    private Page pagedQueryForRosterLog(Page page, List<PropertyFilter> propertyFilters, String strIds) throws ParseException {

        String sqlPagedQuerySelect = "SELECT * FROM oa_bpm_rosterlog WHERE EMPLOYEENO =  " + strIds + "  and  ISAPPROVAL = 0";

        String sqlPagedQueryCount = "SELECT COUNT(*) FROM oa_bpm_rosterlog WHERE EMPLOYEENO = " + strIds + "  and  ISAPPROVAL = 0";

         StringBuilder buff = new StringBuilder();
        List<Object> paramList = new ArrayList<Object>(); 
        boolean checkWhere = sqlPagedQuerySelect.toLowerCase().indexOf("where") == -1;
        PropertyFilterUtils.buildConfigurations(propertyFilters, buff,
                paramList, checkWhere);
        
        String sql = buff.toString();
        String countSql = "";
        String selectSql = "";
        
        //20181112  chengze 优化代码：去掉冗余代码
//        if (StringUtils.isEmpty(strIds)) {
//            countSql = sqlPagedQueryCount + " " + sql;
//            selectSql = sqlPagedQuerySelect + " " + sql + " ORDER BY UPDATE_TIME limit " + page.getStart() + "," + page.getPageSize();
//        } else {
//            countSql = sqlPagedQueryCount + " " + sql ;
//            selectSql = sqlPagedQuerySelect + " " + sql +  " ORDER BY UPDATE_TIME limit "
//                    + page.getStart() + "," + page.getPageSize();
//        }
        
        
        countSql = sqlPagedQueryCount + " " + sql ;
        selectSql = sqlPagedQuerySelect + " " + sql +  " ORDER BY UPDATE_TIME limit " 
                	+ page.getStart() + "," + page.getPageSize();
    
        

        logger.debug("countSql : {}", countSql);
        logger.debug("selectSql : {}", selectSql);

        Object[] params = paramList.toArray();
        int totalCount = jdbcTemplate.queryForObject(countSql, Integer.class,
                params);
        List<Map<String, Object>> list = jdbcTemplate.queryForList(selectSql,
                params);
        List<RosterLogDTO> rosterLog = new ArrayList<RosterLogDTO>();

        for (Map<String, Object> map : list) {
        	//OPERATION_ID  UPDATE_COLUMN_NAME CONTENT_BEFORE CONTENT_NEW UPDATE_TIME  ISAPPROVAL
        	
        
        	
        	
            RosterLogDTO rosterLogDTO = new RosterLogDTO();
            rosterLogDTO.setOperationID(map.get("OPERATION_ID").toString() );
            rosterLogDTO.setContentBefore(map.get("CONTENT_BEFORE").toString());
            rosterLogDTO.setContentNew(map.get("CONTENT_NEW").toString());
            rosterLogDTO.setUpdateColumnName(map.get("UPDATE_COLUMN_NAME")==null?"":map.get("UPDATE_COLUMN_NAME").toString() );
            rosterLogDTO.setUpdateTime(convertString(map.get("UPDATE_TIME")).substring(0, 10));
            rosterLog.add(rosterLogDTO);
        }

        page.setTotalCount(totalCount);
        page.setResult(rosterLog);

        return page;
    }

    /**
     * 查询离职的员工
     *
     * @param page
     * @param propertyFilters
     * @param strIds
     * @return
     */
    private Page pagedQueryQuit(Page page, List<PropertyFilter> propertyFilters, String strIds) {

        String sqlPagedQuerySelect = "SELECT p.*,s.PARENT_ENTITY_ID FROM person_info p "
                + "inner join party_entity e on p.party_id = e.ID "
                + "inner join party_struct s on e.ID =s.CHILD_ENTITY_ID where p.QUIT_FLAG ='1' and p.DEL_FLAG ='1'  and s.STRUCT_TYPE_ID =" + PartyConstants.PARTY_TYPE_USER;

        String sqlPagedQueryCount = "SELECT COUNT(*) FROM person_info p "
                + "inner join party_entity e on p.party_id = e.ID "
                + "inner join party_struct s on e.ID =s.CHILD_ENTITY_ID where p.QUIT_FLAG ='1' and p.DEL_FLAG ='1' and s.STRUCT_TYPE_ID =" + PartyConstants.PARTY_TYPE_USER;

        StringBuilder buff = new StringBuilder();
        List<Object> paramList = new ArrayList<Object>();
        boolean checkWhere = sqlPagedQuerySelect.toLowerCase().indexOf("where") == -1;
        PropertyFilterUtils.buildConfigurations(propertyFilters, buff,
                paramList, checkWhere);
        /*
        logger.debug("propertyFilters : {}", propertyFilters);
        logger.debug("buff : {}", buff);
        logger.debug("paramList : {}", paramList);
        logger.debug("checkWhere : {}", checkWhere);
		*/
        String sql = buff.toString();
        String countSql = "";
        String selectSql = "";
        if (StringUtils.isEmpty(strIds)) {
            countSql = sqlPagedQueryCount + " " + sql;
            selectSql = sqlPagedQuerySelect + " " + sql + " limit " + page.getStart() + "," + page.getPageSize();
        } else {
            countSql = sqlPagedQueryCount + " " + sql + " and s.PARENT_ENTITY_ID in (" + strIds + ")";
            selectSql = sqlPagedQuerySelect + " " + sql + " and s.PARENT_ENTITY_ID in (" + strIds + ") limit "
                    + page.getStart() + "," + page.getPageSize();
        }

        logger.debug("countSql : {}", countSql);
        logger.debug("selectSql : {}", selectSql);

        Object[] params = paramList.toArray();
        int totalCount = jdbcTemplate.queryForObject(countSql, Integer.class,
                params);
        List<Map<String, Object>> list = jdbcTemplate.queryForList(selectSql,
                params);
        List<PersonInfoDTO> personDtos = new ArrayList<PersonInfoDTO>();

        for (Map<String, Object> map : list) {
            personDtos.add(convertPersonDTOQuit(map));
        }

        page.setTotalCount(totalCount);
        page.setResult(personDtos);

        return page;
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
    
    
    
    

    protected PersonInfoDTO convertPersonDTO(Map<String, Object> map) throws ParseException {
        if ((map == null) || map.isEmpty()) {
            logger.info("person[{}] is null.", map);

            return null;
        }

        // logger.debug("{}", map);


        PersonInfoDTO personDTO = new PersonInfoDTO();
        personDTO.setId(Long.parseLong(convertString(map.get("ID"))));
        personDTO.setCode(convertString(map.get("CODE")));
        personDTO.setUserName(convertString(map.get("USERNAME")));
        personDTO.setNameBefore(convertString(map.get("NAME_BEFORE")));
        personDTO.setFullName(convertString(map.get("FULL_NAME")));
        personDTO.setRealName(convertString(map.get("REAL_NAME")));
        personDTO.setCellphone(convertString(map.get("CELLPHONE")));
        personDTO.setTelephone(convertString(map.get("TELEPHONE")));
        personDTO.setAddress(convertString(map.get("ADDRESS")));
        personDTO.setEmail(convertString(map.get("EMAIL")));
        personDTO.setEmployeeNo(convertString(map.get("EMPLOYEE_NO")));
        if (map.get("ADD_TIME") != null) {
        personDTO.setAddTime(convertString(map.get("ADD_TIME")).substring(0, 16));
        }
        personDTO.setGender(convertString(map.get("GENDER")));
        personDTO.setCompanyCode(convertString(map.get("COMPANY_CODE")));
        personDTO.setCompanyName(convertString(map.get("COMPANY_NAME")));
        personDTO.setDepartmentCode(convertString(map.get("DEPARTMENT_CODE")));
        personDTO.setDepartmentName(convertString(map.get("DEPARTMENT_NAME")));
        personDTO.setStopFlag(convertString(map.get("STOP_FLAG")));
        personDTO.setSecret(convertString(map.get("SECRET")));
        personDTO.setParentId(convertString(map.get("PARENT_ENTITY_ID")));
        
        personDTO.setFax(convertString(map.get("FAX")));
        personDTO.setWxNo(convertString(map.get("WXNO")));
        personDTO.setQq(convertString(map.get("QQ")));
        personDTO.setLevel(convertString(map.get("LEVEL")));
        personDTO.setNation(dictConnector.findDictNameByValue("nation",convertString(map.get("NATION"))));
        personDTO.setNativePlace(convertString(map.get("NATIVE_PLACE")));
        personDTO.setRegisteredResidence(convertString(map.get("REGISTERED_RESIDENCE")));
        personDTO.setHouseholdRegisterType(dictConnector.findDictNameByValue("householdRegisterType", convertString(map.get("HOUSEHOLD_REGISTER_TYPE"))));
        personDTO.setPoliticalOutlook(dictConnector.findDictNameByValue("politicalOutlook", convertString(map.get("POLITICAL_OUTLOOK"))));
        personDTO.setMajor(convertString(map.get("MAJOR")));
        personDTO.setTitle(convertString(map.get("TITLE")));
        personDTO.setAcademicDegree(dictConnector.findDictNameByValue("academicDegree", convertString(map.get("ACADEMIC_DEGREE"))));
        personDTO.setSkillSpecialty(convertString(map.get("SKILL_SPECIALTY")));
        personDTO.setLaborType(dictConnector.findDictNameByValue("laborType", convertString(map.get("LABOR_TYPE"))));
        personDTO.setEntryMode(dictConnector.findDictNameByValue("entryMode",convertString(map.get("ENTRY_MODE"))));
        personDTO.setEducation(dictConnector.findDictNameByValue("education", convertString(map.get("EDUCATION"))));
        if(convertString(map.get("MARRIAGE"))!=null){
        personDTO.setMarriage(convertString(map.get("MARRIAGE")).equals("1")?"已婚":"未婚");
        }else {
        	personDTO.setMarriage("");
		}
        
        if(convertString(map.get("FERTILITY_CONDITION"))!=null){
        personDTO.setFertilityCondition(convertString(map.get("FERTILITY_CONDITION")).equals("1")?"已育":"未育");
        }else {
        	personDTO.setFertilityCondition("");
		}
        
        
        if (map.get("ENTRY_TIME") != null) {
        personDTO.setEntryTime(convertString(map.get("ENTRY_TIME")).substring(0, 10));
        }
        
        if (map.get("CONTRACT_EXPIRATION_TIME") != null)
        personDTO.setContractExpirationTime(convertString(map.get("CONTRACT_EXPIRATION_TIME")).substring(0, 10));
        
        personDTO.setContractDeadline(convertString(map.get("CONTRACT_DEADLINE")));
        
        personDTO.setContractCompany(convertString(map.get("CONTRACT_COMPANY")));
        
        personDTO.setInsurance(convertString(map.get("INSURANCE")));
        
        personDTO.setDocument(convertString(map.get("DOCUMENT")));
        
        personDTO.setIdentityID(convertString(map.get("IDENTITY_ID")));
        
        personDTO.setFamily_1(convertString(map.get("FAMILY_1")));
        
        personDTO.setFamily_2(convertString(map.get("FAMILY_2")));
        
        personDTO.setEducational_experience_1(convertString(map.get("EDUCATIONAL_EXPERIENCE_1")));
        
        personDTO.setEducational_experience_2(convertString(map.get("EDUCATIONAL_EXPERIENCE_2")));
        
        personDTO.setEducational_experience_3(convertString(map.get("EDUCATIONAL_EXPERIENCE_3")));
        
        personDTO.setWork_experience_1(convertString(map.get("WORK_EXPERIENCE_1")));
        
        personDTO.setWork_experience_2(convertString(map.get("WORK_EXPERIENCE_2")));
        
        personDTO.setRemark(convertString(map.get("REMARK")));
        
        String quitFlag="0";
        quitFlag=convertString(map.get("QUIT_Flag"));
        if(com.mossle.core.util.StringUtils.isBlank(quitFlag))
        	quitFlag="0";
        personDTO.setQuitFlag(Integer.parseInt(quitFlag));
        
        if (map.get("QUIT_TIME") != null) {
            personDTO.setQuitTime(convertString(map.get("QUIT_TIME")).substring(0, 10));
        }
        if(null != map.get("LEAVE_DATE")){
        	personDTO.setLeaveDate(convertString(map.get("LEAVE_DATE")).substring(0, 10));
        }
        //{ID=1, PARTY_ID=2, CODE=2, USERNAME=admin, FAMILY_NAME=Rolmex, GIVEN_NAME=Admin, FULL_NAME=Admin Rolmex, CELLPHONE=18012345678, TELEPHONE=null, EMAIL=lingo@mossle.com, IM=null, COUNTRY=null, PROVINCE=null, CITY=null, BUILDING=null, FLOOR=null, SEAT=null, EMPLOYEE_NO=null, EMPLOYEE_TYPE=null, CARD=null, COMPANY_CODE=null, COMPANY_NAME=null, DEPARTMENT_CODE=null, DEPARTMENT_NAME=null, POSITION_CODE=null, POSITION_NAME=null, GENDER=null, BIRTHDAY=null, ID_CARD_TYPE=null, ID_CARD_VALUE=null, NATIONALITY=null, STAR=null, BLOOD=null, CLOTH_SIZE=null, STOP_FLAG=0, DEL_FLAG=0, QUIT_FLAG=0, ADD_TIME=2017-08-09 16:02:13.0, TENANT_ID=1}

        return personDTO;
    }
    protected PersonInfoDTO convertPersonDTOQuit(Map<String, Object> map) {
        if ((map == null) || map.isEmpty()) {
            logger.info("person[{}] is null.", map);

            return null;
        }

        // logger.debug("{}", map);


        PersonInfoDTO personDTO = new PersonInfoDTO();
        personDTO.setId(Long.parseLong(convertString(map.get("ID"))));
        personDTO.setCode(convertString(map.get("CODE")));
        personDTO.setUserName(convertString(map.get("USERNAME")));
        personDTO.setFullName(convertString(map.get("FULL_NAME")));
        personDTO.setCellphone(convertString(map.get("CELLPHONE")));
        personDTO.setEmail(convertString(map.get("EMAIL")));
        personDTO.setEmployeeNo(convertString(map.get("EMPLOYEE_NO")));
        personDTO.setAddTime(convertString(map.get("ADD_TIME")).substring(0, 16));
        personDTO.setGender(convertString(map.get("GENDER")));
        personDTO.setCompanyCode(convertString(map.get("COMPANY_CODE")));
        personDTO.setCompanyName(convertString(map.get("COMPANY_NAME")));
        personDTO.setDepartmentCode(convertString(map.get("DEPARTMENT_CODE")));
        personDTO.setDepartmentName(convertString(map.get("DEPARTMENT_NAME")));
        personDTO.setStopFlag(convertString(map.get("STOP_FLAG")));
        personDTO.setSecret(convertString(map.get("SECRET")));
        personDTO.setParentId(convertString(map.get("PARENT_ENTITY_ID")));
        PartyStruct partyStruct=partyStructManager.findUniqueBy("childEntity.id",personDTO.getId());
        String departmentN=partyStruct.getParentEntity().getName();
        personDTO.setDepartmentName(departmentN);
        if (map.get("QUIT_TIME") != null) {
            personDTO.setQuitTime(convertString(map.get("QUIT_TIME")).substring(0, 10));
        }
        if(null != map.get("LEAVE_DATE")){
        	personDTO.setLeaveDate(convertString(map.get("LEAVE_DATE")).substring(0, 10));
        }
        //{ID=1, PARTY_ID=2, CODE=2, USERNAME=admin, FAMILY_NAME=Rolmex, GIVEN_NAME=Admin, FULL_NAME=Admin Rolmex, CELLPHONE=18012345678, TELEPHONE=null, EMAIL=lingo@mossle.com, IM=null, COUNTRY=null, PROVINCE=null, CITY=null, BUILDING=null, FLOOR=null, SEAT=null, EMPLOYEE_NO=null, EMPLOYEE_TYPE=null, CARD=null, COMPANY_CODE=null, COMPANY_NAME=null, DEPARTMENT_CODE=null, DEPARTMENT_NAME=null, POSITION_CODE=null, POSITION_NAME=null, GENDER=null, BIRTHDAY=null, ID_CARD_TYPE=null, ID_CARD_VALUE=null, NATIONALITY=null, STAR=null, BLOOD=null, CLOTH_SIZE=null, STOP_FLAG=0, DEL_FLAG=0, QUIT_FLAG=0, ADD_TIME=2017-08-09 16:02:13.0, TENANT_ID=1}

        return personDTO;
    }
    private String convertString(Object value) {
        if (value == null) {
            return "";
        }

        if (value instanceof String) {
            return (String) value;
        }

        return value.toString();
    }

    public List<Map> generatePartyEntities(List<PartyEntity> partyEntities,
                                           long partyStructTypeId, Long accountId, boolean viewPost) {
        if (partyEntities == null) {
            return null;
        }

        List<Map> list = new ArrayList<Map>();

        try {
            for (PartyEntity partyEntity : partyEntities) {
                list.add(generatePartyEntity(partyEntity, partyStructTypeId, accountId, false, true));
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }

        return list;
    }

    private Map<String, Object> generatePartyEntity(PartyEntity partyEntity,
                                                    long partyStructTypeId, Long accountId, boolean viewPost, boolean auth) {
        Map<String, Object> map = new HashMap<String, Object>();

        try {
            map.put("id", partyEntity.getId());

            List<PartyStruct> partyStructs = partyStructManager.find("from PartyStruct where parentEntity=?", partyEntity);

            List<PartyEntity> partyEntities = new ArrayList<PartyEntity>();

            for (PartyStruct partyStruct : partyStructs) {
                if (partyStruct.getPartyStructType().getId() == partyStructTypeId) {
                    PartyEntity childPartyEntity = partyStruct.getChildEntity();

                    if (childPartyEntity == null) {
                        continue;
                    }

                    if (childPartyEntity.getPartyType().getType() != 1) {
                        if (auth) {
                            if (!PartyConstants.ROOT_PARTY_TREE_ID.equals(partyStruct.getParentEntity().getId())) {
                                partyEntities.add(childPartyEntity);
                            } else {
                                PartyEntity vo = partyEntityManager.get(accountId);

                                List<PartyStruct> list = partyStructManager.find("from PartyStruct where childEntity=?", vo);

                                for (PartyStruct item : list) {
                                    // 判断是否是分公司管理员的公司
                                    if (childPartyEntity.getId().equals(item.getParentEntity().getId())) {
                                        partyEntities.add(childPartyEntity);
                                    }
                                    // break;
                                }

                            }

                        } else {
                            partyEntities.add(childPartyEntity);
                        }
                    }

                }
            }

            if (!partyEntities.isEmpty()) {
                map.put("children", generatePartyEntities(partyEntities, partyStructTypeId, accountId, viewPost));
            }

            return map;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            return map;
        }
    }

    @RequestMapping("account-info-checkEmployeeNo")
    @ResponseBody
    @Log(desc = "人力资源", action = "checkEmployeeNo", operationDesc = "人力资源-职员管理-在职人员-检验工号")
    public boolean checkEmployeeNo(@RequestParam("employeeNo") String employeeNo,
                                   @RequestParam(value = "id", required = false) Long id)
            throws Exception {

        String hql = "from PersonInfo where delFlag = '0' and employeeNo=?";
        Object[] params = {employeeNo};

        if (id != null) {
            hql = "from PersonInfo where delFlag = '0' and employeeNo=? and id<>?";
            params = new Object[]{employeeNo, id};
        }

        boolean result = personInfoManager.findUnique(hql, params) == null;

        return result;
    }

    @Log(desc = "人力资源", action = "check", operationDesc = "人力资源-职员管理-检查用户名")
    private boolean checkUsername(String username, Long id) throws Exception {
        String tenantId = tenantHolder.getTenantId();
        String hql = "from AccountInfo where delFlag = '0' and username=? and tenantId=?";
        Object[] params = {username, tenantId};

        if (id != null) {
            hql = "from AccountInfo where delFlag = '0' and username=? and tenantId=? and id<>?";
            params = new Object[]{username, tenantId, id};
        }

        boolean result = accountInfoManager.findUnique(hql, params) == null;

        return result;
    }
    
    
//    String hql="from DictInfo where dictType.name =?";
//    		    	DictInfo dictInfo=dictInfoManager.findUnique(hql, "personMasterAudit");
//    		    	dictInfo.getvalue (0 关闭 ；1开启)
    
    @GET
	@RequestMapping("person-info-position-change-i")
	@Log(desc = "人事管理", action = "search", operationDesc = "人事管理-花名册-调岗")
	public String searchChangePostion(Model model,
            @RequestParam("id") Long id,
            RedirectAttributes redirectAttributes) throws Exception{
    	if(id==null){
    		messageHelper.addFlashMessage(redirectAttributes, "core.success.publish", "参数错误");
    		return "redirect:/user/person-info-list-i.do";
    	}
		
    	PartyEntity partyEntity=partyEntityManager.findUniqueBy("id", id);
    	if(partyEntity==null){
    		messageHelper.addFlashMessage(redirectAttributes, "core.success.publish", "系统查询不到调岗人员信息");
    		return "redirect:/user/person-info-list-i.do";
    	}
    	
    	PersonInfo personInfo=personInfoManager.findUniqueBy("id", partyEntity.getId());
    	if(personInfo==null){
    		messageHelper.addFlashMessage(redirectAttributes, "core.success.publish", "系统查询不到调岗人员相关信息");
    		return "redirect:/user/person-info-list-i.do";
    	}
    	
    	if("1".equals(personInfo.getQuitFlag()))
    	{
    		messageHelper.addFlashMessage(redirectAttributes, "core.success.publish", "已离职的人员不能调岗");
    		return "redirect:/user/person-info-list-i.do";
    	}
    	
    	model.addAttribute("name", partyEntity.getName());
    	model.addAttribute("accountId", id);
    	String hql="from PartyStruct where partyStructType.id=4 and childEntity=?";
    	List<PartyStruct> partyStructList=partyStructManager.find(hql,partyEntity);
    	List<Map<String,Object>> postionMapList=new ArrayList<Map<String,Object>>();
    	if(partyStructList!=null&&partyStructList.size()>0)
    	{
    		int i=0;
    		for(PartyStruct partyStruct:partyStructList){
    			//岗位
    			String positionName=partyStruct.getParentEntity().getName();
    			
    			//部门
    			hql="from PartyStruct where partyStructType.id=1 and childEntity=?";
    			PartyStruct deparmentPartyStruct=partyStructManager.findUnique(hql, partyStruct.getParentEntity());
    			
    			if (deparmentPartyStruct == null) {
    				continue;
    			}
    			String deparmentName=deparmentPartyStruct.getParentEntity().getName();
    			
    			//公司
    			hql="from PartyStruct where partyStructType.id=1 and childEntity=?";
    			PartyStruct companyPartyStruct=partyStructManager.findUnique(hql, deparmentPartyStruct.getParentEntity());
    			if (companyPartyStruct == null) {
    				continue;
    			}
    			String companyName=companyPartyStruct.getParentEntity().getName();
    			
    			Map<String,Object> map=new HashMap<String, Object>();
    			map.put("position", " "+(++i)+"."+companyName+"-"+deparmentName+"-"+positionName);
    			map.put("id", partyStruct.getParentEntity().getId());
    			postionMapList.add(map);
    		}
    	}
    	else {
    		messageHelper.addFlashMessage(redirectAttributes, "core.success.publish", "此人员没有岗位信息，请先关联岗位");
    		return "redirect:/user/person-info-list-i.do";
		}
    	
    	
    	//生成受理单编号
    	String userId = currentUserHolder.getUserId();
    	String code =  operationService.CreateApplyCode(userId);
    	
    	String isAudit=userService.getAuditOpenStatus();
        /*hql="from DictInfo where dictType.name=?";
    	DictInfo dictInfo=dictInfoManager.findUnique(hql, "personMasterAudit");
        if(dictInfo==null){
        	isAudit=dictInfo.getValue();
        }*/
    	
        model.addAttribute("isAudit",userService.getAuditOpenStatus());
    	model.addAttribute("code",code);
    	model.addAttribute("positions", postionMapList);
		
		//messageHelper.addFlashMessage(redirectAttributes, "core.success.publish", "保存成功");
		return "user/person-info-position-change-i";
	}
    
    @GET
   	@RequestMapping("person-info-position-change-i-save")
   	@Log(desc = "人事管理", action = "update", operationDesc = "人事管理-花名册-调岗")
   	public String changePostion(
   				HttpServletRequest request,
   			   @RequestParam(value="id",required=false) Long id,
               @RequestParam("iptCurrentPost") Long iptCurrentPost,
               @RequestParam("postId") Long postId,
               @RequestParam(value="isResetProcess",required=false) String isResetProcess,
               RedirectAttributes redirectAttributes){
    	try {
    		if(iptCurrentPost==null){
           		messageHelper.addFlashMessage(redirectAttributes, "core.success.publish", "没有选择要调的岗位");
           		//return "redirect:/user/person-info-position-change-i.do?id="+id;
           		return "redirect:/user/person-info-list-i.do";
           	}
        	
           	if(postId==null){
           		messageHelper.addFlashMessage(redirectAttributes, "core.success.publish", "没有选择调整的岗位");
           		//return "redirect:/user/person-info-position-change-i.do?id="+id;
           		return "redirect:/user/person-info-list-i.do";
           	}
       		
           	PartyEntity partyEntity=partyEntityManager.findUniqueBy("id", id);
           	if(partyEntity==null){
           		messageHelper.addFlashMessage(redirectAttributes, "core.success.publish", "系统查询不到调岗人员信息");
           		//return "redirect:/user/person-info-position-change-i.do?id="+id;
           		return "redirect:/user/person-info-list-i.do";
           	}
           	
           	PartyEntity changePartyEntity=partyEntityManager.findUniqueBy("id", postId);
           	if(changePartyEntity==null){
           		messageHelper.addFlashMessage(redirectAttributes, "core.success.publish", "系统查询不到调整的岗位");
           		//return "redirect:/user/person-info-position-change-i.do?id="+id;
           		return "redirect:/user/person-info-list-i.do";
           	}
           	
           	String hqlString="from UpdatePerson where isApproval='2' and typeID='changepost' and employeeNo=?";
           	UpdatePerson updatePersonExist=updatePersonManager.findUnique(hqlString, id.toString());
           	if(updatePersonExist!=null){
           		messageHelper.addFlashMessage(redirectAttributes, "core.success.publish", "人员有正在调整的岗位，正在审批，请审批结束后，再进行操作");
           		return "redirect:/user/person-info-list-i.do";
           	}
           	
           	//如果由审批状态，则走审批
           	String isAudit=userService.getAuditOpenStatus();
           	if(isAudit.equals("1")){
           		personInfoService.changePositionForAudit(
           					request,
           					request.getParameter("applyCode"),
           					partyEntity.getId(),
           					iptCurrentPost,
           					changePartyEntity.getId());
                
                messageHelper.addFlashMessage(redirectAttributes, "core.success.publish", "岗位调整申请成功，流程审核通过后生效");
           		return "redirect:/user/person-info-list-i.do";
           	}
           	else {
				userService.changePosition(partyEntity, changePartyEntity, iptCurrentPost,"0");
				messageHelper.addFlashMessage(redirectAttributes, "core.success.publish", "岗位调整成功");
	       		return "redirect:/user/person-info-list-i.do";
			}
		} catch (Exception e) {
			logger.info("人事管理-花名册-调岗：操作异常，"+e.getMessage()+"\r\n"+e.getStackTrace());
			messageHelper.addFlashMessage(redirectAttributes, "core.success.publish", "操作异常");
			//return "redirect:/user/person-info-position-change-i.do?id="+id;
       		return "redirect:/user/person-info-list-i.do";
		}
   	}

    
    //调岗  ：驳回发起人重新调整页面
    @GET
	@RequestMapping("person-info-position-change-forModify")
	@Log(desc = "人事管理", action = "search", operationDesc = "人事管理-花名册-调岗-驳回发起人")
	public String ChangePostionforModify(Model model,
			@RequestParam("applyCode") String applyCode, 
            @RequestParam("id") Long id,
            @RequestParam(value="isdetail",required=false) String isdetail,
            RedirectAttributes redirectAttributes) throws Exception{
    	if(id==null){
    		messageHelper.addFlashMessage(redirectAttributes, "core.success.publish", "参数错误");
    		return "redirect:/user/person-info-list-i.do";
    	}
		
    	PartyEntity partyEntity=partyEntityManager.findUniqueBy("id", id);
    	if(partyEntity==null){
    		messageHelper.addFlashMessage(redirectAttributes, "core.success.publish", "系统查询不到调岗人员信息");
    		return "redirect:/user/person-info-list-i.do";
    	}
    	
    	model.addAttribute("name", partyEntity.getName());
    	model.addAttribute("accountId", id);
    	String hql="from PartyStruct where partyStructType.id=4 and childEntity=?";
    	List<PartyStruct> partyStructList=partyStructManager.find(hql,partyEntity);
    	List<Map<String,Object>> postionMapList=new ArrayList<Map<String,Object>>();
    	if(partyStructList!=null&&partyStructList.size()>0)
    	{
    		int i=0;
    		for(PartyStruct partyStruct:partyStructList){
    			//岗位
    			String positionName=partyStruct.getParentEntity().getName();
    			
    			//部门
    			hql="from PartyStruct where partyStructType.id=1 and childEntity=?";
    			PartyStruct deparmentPartyStruct=partyStructManager.findUnique(hql, partyStruct.getParentEntity());
    			String deparmentName=deparmentPartyStruct.getParentEntity().getName();
    			
    			//公司
    			hql="from PartyStruct where partyStructType.id=1 and childEntity=?";
    			PartyStruct companyPartyStruct=partyStructManager.findUnique(hql, deparmentPartyStruct.getParentEntity());
    			String companyName=companyPartyStruct.getParentEntity().getName();
    			
    			Map<String,Object> map=new HashMap<String, Object>();
    			map.put("position", " "+(++i)+"."+companyName+"-"+deparmentName+"-"+positionName);
    			map.put("id", partyStruct.getParentEntity().getId());
    			postionMapList.add(map);
    		}
    	}
    	else {
    		messageHelper.addFlashMessage(redirectAttributes, "core.success.publish", "此人员没有岗位信息，请先关联岗位");
    		return "redirect:/user/person-info-list-i.do";
		}
    	
    	/*jsonMap.put("selfPartyEntityId", partyEntity.getId());
   		jsonMap.put("OldpartyEntityId", iptCurrentPost);
   		jsonMap.put("changePartyEntityId", changePartyEntity.getId());*/
    	String hqlString="from UpdatePerson where applyCode=?";
    	UpdatePerson updatePerson=updatePersonManager.findUnique(hqlString, applyCode);
    	
    	if(com.mossle.core.util.StringUtils.isBlank(isdetail))
    		isdetail="1";
    	
    	String strOldPostId="";
    	String strOldPostName="";
    	String strNewPostId="";
    	String strNewPostName="";
    	if(updatePerson!=null){
    		if(!updatePerson.getIsApproval().equals("2"))
    			isdetail="1";
    			
    		Map<String,Object> map=jsonMapper.fromJson(updatePerson.getJsonContent(), Map.class);
    		strOldPostId=map.get("OldpartyEntityId")==null?"":map.get("OldpartyEntityId").toString();
    		if(!strOldPostId.equals("")){
    			PartyEntity oldPartyEntity=partyEntityManager.findUniqueBy("id", Long.valueOf(strOldPostId));
    			if(oldPartyEntity!=null)
    				strOldPostName=oldPartyEntity.getName();
    		}
    		
    		strNewPostId=map.get("changePartyEntityId")==null?"":map.get("changePartyEntityId").toString();
    		if(!strNewPostId.equals("")){
    			PartyEntity changePartyEntity=partyEntityManager.findUniqueBy("id", Long.valueOf(strNewPostId));
    			if(changePartyEntity!=null)
    				strNewPostName=changePartyEntity.getName();
    		}
    	}
    	
    	model.addAttribute("oldPostId",strOldPostId);
    	model.addAttribute("oldPostName",strOldPostName);
    	model.addAttribute("newPostId",strNewPostId);
    	model.addAttribute("newPostName",strNewPostName);
    	
    	model.addAttribute("positions", postionMapList);
    	model.addAttribute("applyCode", applyCode);
    	model.addAttribute("isdetail", isdetail);
    	
		//messageHelper.addFlashMessage(redirectAttributes, "core.success.publish", "保存成功");
		return "user/person-info-position-change-forModify";
	}
    
    
    @GET
    @RequestMapping("person-info-position-change-forModify-save")
   	@Log(desc = "人事管理", action = "update", operationDesc = "人事管理-花名册-调岗-驳回发起人保存")
   	public String changePostion(
   			@RequestParam(value="id",required=false) Long id,
   			@RequestParam("applyCode") String applyCode,
            @RequestParam("iptCurrentPost") Long iptCurrentPost,
            @RequestParam("postId") Long postId,
            RedirectAttributes redirectAttributes) throws Exception
    {
    	personInfoService.changePositionEditForAudit(applyCode,id,iptCurrentPost,postId);
   		return "redirect:/user/close-popwin-dialog.do?msgTip="+java.net.URLEncoder.encode(java.net.URLEncoder.encode("修改成功","utf-8"),"utf-8");
    }
    
    @GET
    @RequestMapping("close-popwin-dialog")
    @Log(desc = "关闭弹出框", action = "search", operationDesc = "关闭弹出框")
    public String closeDialog(
    		Model model,
    		@RequestParam(value="msgTip",required=false) String msgTip
    		){
    	try {
			model.addAttribute("msg",java.net.URLDecoder.decode(msgTip,"utf-8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return "user/close-popwin-dialog";
    }
    
    //导出花名册
    @RequestMapping("personInfo-export")
    @Log(desc = "花名册导出", action = "export", operationDesc = "人力资源-花名册-导出")
    public void chargeExport(@ModelAttribute Page page,
                             @RequestParam Map<String, Object> parameterMap, HttpServletResponse response, HttpServletRequest request,
                             @RequestParam(value = "partyEntityId", required = false) Long partyEntityId)
                            throws Exception {
        String userId = currentUserHolder.getUserId();
        List<PropertyFilter> propertyFilters = PropertyFilter.buildFromMap(parameterMap);
        //propertyFilters.add(new PropertyFilter("EQL_leader", userId));
//        propertyFilters.add(new PropertyFilter("EQS_datastatus", "1"));
//        propertyFilters.add(new PropertyFilter("INS_status", "0,1"));
        //List<PersonInfo> workTaskInfoList = personInfoManager.find(propertyFilters);
        
        String sql = " ";
        
        boolean isExportQuit=false;//是否导出离职
        for(PropertyFilter lp:propertyFilters){
        	String filterName =lp.getPropertyName();
        	if(filterName.equals("FULL_NAME")){
        		sql +=" and p.fullName  like '%"+lp.getMatchValue().toString()+"%'";
        	}else if(filterName.equals("EMPLOYEE_NO")){
        		sql += " and p.employeeNo  ='"+lp.getMatchValue().toString()+"'";
        	}
        	else if(filterName.equals("QUIT_FLAG")){
        		String quit_flag=lp.getMatchValue().toString();
        		sql += " and p.quitFlag  ='"+quit_flag+"'";
        		
        		if(quit_flag.equals("1"))
        			isExportQuit=true;
        		else
        			sql += " and p.delFlag  ='0'";//在职，删除标识等于0
        	}
        	else if(filterName.toLowerCase().equals("real_name"))
        		sql += " and p.realName like '%"+lp.getMatchValue().toString()+"%'";
        }
        
        //先判断  要导出哪个部门的数据
        String strRootChildPartyIds="";
        String strPartyIds="";
        if(!PartyConstants.ADMIN_USER_ID.toString().equals(userId))
        {
        	if(partyEntityId.equals(PartyConstants.ROOT_PARTY_TREE_ID)){//如果是跟节点
        		//根节点点击是否显示数据
            	String strSql="Select partyEntityID from auth_orgdata where type='2' and union_id="+userId;
            	List<String> rootNodeIdList=jdbcTemplate.queryForList(strSql, String.class);
            	String strRootNode="";
            	if(rootNodeIdList!=null&&rootNodeIdList.size()>0)
            		strRootNode=rootNodeIdList.get(0);
            	
            	if(!strRootNode.equals(partyEntityId.toString())){
            		strSql="SELECT CHILD_ENTITY_ID FROM party_struct s"
							+" INNER JOIN party_entity child on child.ID=s.CHILD_ENTITY_ID"
							+" WHERE PARENT_ENTITY_ID="+partyEntityId
							+ " and child.TYPE_ID='1'";
            		
            		List<String> rootChildPartIdList=jdbcTemplate.queryForList(strSql, String.class);
            		if(rootChildPartIdList!=null)
            			strRootChildPartyIds=Joiner.on(",").join(rootChildPartIdList);
            	} 
        	}
        	
	        List<String> partyIdList=null;
			String strSql="Select partyEntityID from auth_orgdata where type='1' and union_id="+userId;
			partyIdList=jdbcTemplate.queryForList(strSql, String.class);
			
			if(partyIdList!=null)
				strPartyIds=Joiner.on(",").join(partyIdList);
			
			/*************************************************
			 * 去除虚拟账号
			 * 1.超级管理员虚拟人员-2
			 * 2.系统管理员-对应角色区分
			 * 3.经销商虚拟人员-4
			 * 4.机器人虚拟人员-3
			 * 5.测试用户
			 * ***********************************************/
			String strSerchRemoveId=PartyConstants.ADMIN_USER_ID+","
									+PartyConstants.SYSTEM_ROBOT_ID+","
									+PartyConstants.JXS_ID;
			PersonInfo personInfoTest=personInfoManager.findUniqueBy("username", "testuser");
			if(personInfoTest!=null)
				strSerchRemoveId+=","+personInfoTest.getId().toString();
			
			String strSystemAdminIds="";
			List<String> systemAdminIdList=null;
			//查询属于角色ID为2(系统管理员)的所有用户ID
			strSql="SELECT US.ref FROM AUTH_USER_ROLE UR "
					+" INNER JOIN AUTH_USER_STATUS US ON UR.USER_STATUS_ID=US.ID"
					+" WHERE ROLE_ID=2";
			systemAdminIdList=jdbcTemplate.queryForList(strSql, String.class);
			if(systemAdminIdList!=null&&systemAdminIdList.size()>0)
				strSystemAdminIds=Joiner.on(",").join(systemAdminIdList);
			
			if(!strSystemAdminIds.equals(""))
				strSerchRemoveId+=","+strSystemAdminIds;
			
			if(!strRootChildPartyIds.equals("")&&!strSerchRemoveId.equals(""))
				strRootChildPartyIds+=","+strSerchRemoveId;
			else if(strRootChildPartyIds.equals("")&&!strSerchRemoveId.equals(""))
				strRootChildPartyIds+=strSerchRemoveId;
        }
		
		String strChildPartyIds="";
		List<String> childAllList=getAllDeparentById(partyEntityId);
		if(childAllList!=null)
			strChildPartyIds=Joiner.on(",").join(childAllList);
        
        String hql = "select p from PersonInfo as p,PartyEntity as e, PartyStruct as s "
        		+ "where p.partyId = e.id and  e.id =s.childEntity  and s.partyStructType = " + PartyConstants.PARTY_STRUCT_TYPE_ORG;
        				
        if(!com.mossle.core.util.StringUtils.isBlank(strPartyIds))
        	hql+=" and s.parentEntity in ("+strPartyIds+") ";
        	
		if(!com.mossle.core.util.StringUtils.isBlank(strChildPartyIds))
			hql+="and s.parentEntity in ("+strChildPartyIds+")";
        if(!com.mossle.core.util.StringUtils.isBlank(strRootChildPartyIds))
        	hql+=" and s.childEntity not in("+strRootChildPartyIds+")";
        
        hql+=sql;
        List<PersonInfo> personInfoList =personInfoManager.find(hql) ;        
                
        List<PersonInfoDTOForExport> personInfoInstanceList = personInfoManager.exportInfo(personInfoList);
        if (personInfoInstanceList.size() == 0) {
            String title = "暂无数据需要导出！";
            StringBuffer sb = new StringBuffer();
            sb.append("<script language='javascript'>alert('");
            sb.append(title);
            sb.append("');history.go(-1);</script>");
            try {
                response.setContentType("text/html; charset=utf-8");
                String strHtml = "<body style='background-color: #D0D0D0'>";
                strHtml += "</body>";
                response.getWriter().println((strHtml));
                response.getWriter().println(sb.toString());
                response.getWriter().flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
        	String hearderString="";
        	String fieldNameString="";
            String fileName = "花名册_" + formatter.format(new Date()) + ".xls";
        
        	//控制是否开启别名 :1开启 0关闭
      		List<DictInfo> dictInfo_otherName = dictConnector.findDictInfoListByType("isOpenOtherName");
      		 if(dictInfo_otherName.get(0).getValue().equals("1")){
        
             hearderString="工号, 别名,姓名, 公司, 部门, 岗位, 职位, 用户名,状态,级别,性别,曾用名,籍贯,户口所在地,户籍类型,身份证号,民族,政治面貌,专业,学历,学位,职称,技能特长,联系电话,备用电话,邮箱,紧急联系人及电话,QQ,微信,用工类型,进入方式,入职时间,合同单位,合同到期时间,合同有效期,保险情况,资料情况,工作经历一,工作经历二,教育经历一,教育经历二,教育经历三,现住址,家庭成员一,家庭成员二,婚否,生育情况,备注,添加时间";
             fieldNameString="employeeNo,fullName,realName,companyName,departmentName,postName,positionName,username,stopFlag,level,gender,nameBefore,nativePlace,registeredResidence,householdRegisterType,identityID,nation,politicalOutlook,major,education,academicDegree,title,skillSpecialty,telephone,cellphone,email,fax,qq,wxNo,laborType,entryMode,entryTime,contractCompany,contractExpirationTime,contractDeadline,insurance,document,work_experience_1,work_experience_2,educational_experience_1,educational_experience_2,educational_experience_3,address,family_1,family_2,marriage,fertilityCondition,remark,addTime";
      		}else {
      			  hearderString="工号, 姓名, 公司, 部门, 岗位, 职位, 用户名,状态,级别,性别,曾用名,籍贯,户口所在地,户籍类型,身份证号,民族,政治面貌,专业,学历,学位,职称,技能特长,联系电话,备用电话,邮箱,紧急联系人及电话,QQ,微信,用工类型,进入方式,入职时间,合同单位,合同到期时间,合同有效期,保险情况,资料情况,工作经历一,工作经历二,教育经历一,教育经历二,教育经历三,现住址,家庭成员一,家庭成员二,婚否,生育情况,备注,添加时间";
                  fieldNameString="employeeNo,fullName,companyName,departmentName,postName,positionName,username,stopFlag,level,gender,nameBefore,nativePlace,registeredResidence,householdRegisterType,identityID,nation,politicalOutlook,major,education,academicDegree,title,skillSpecialty,telephone,cellphone,email,fax,qq,wxNo,laborType,entryMode,entryTime,contractCompany,contractExpirationTime,contractDeadline,insurance,document,work_experience_1,work_experience_2,educational_experience_1,educational_experience_2,educational_experience_3,address,family_1,family_2,marriage,fertilityCondition,remark,addTime";
           		
			}
            
            if(isExportQuit){
            	hearderString+=",离职时间";
            	fieldNameString+=",quitTime";
            }
            
            String[] headers =hearderString.split(",");
            //String[] headers={"工号", "姓名", "公司", "部门", "岗位", "职位", "用户名","级别","性别","曾用名","籍贯","户口所在地","户籍类型","身份证号","民族","政治面貌","专业","学历","学位","职称","技能特长","联系电话","备用电话","邮箱","紧急联系人及电话","QQ","微信","用工类型","进入方式","入职时间","合同单位","合同到期时间","合同有效期","保险情况","资料情况","工作经历一","工作经历二","教育经历一","教育经历二","教育经历三","现住址","家庭成员一","家庭成员二","婚否","生育情况","备注","添加时间"};          
            String[] fieldNames = fieldNameString.split(",");
            //String[] fieldNames={"employeeNo", "fullName", "companyName", "departmentName","star", "positionName", "username", "level", "gender", "nameBefore", "nativePlace", "registeredResidence", "householdRegisterType", "identityID", "nation", "politicalOutlook", "major", "education", "academicDegree", "title", "skillSpecialty", "telephone", "cellphone", "email", "fax", "qq", "wxNo", "laborType", "entryMode", "entryTime", "contractCompany", "contractExpirationTime", "contractDeadline", "insurance", "document", "work_experience_1", "work_experience_2", "educational_experience_1", "educational_experience_2", "educational_experience_3", "address", "family_1", "family_2", "marriage", "fertilityCondition", "remark", "addTime"};
            List<PersonInfoDTOForExport> dataset = personInfoInstanceList;
            if (request.getHeader("User-Agent").toUpperCase().indexOf("MSIE") > 0) {
                fileName = URLEncoder.encode(fileName, "UTF-8");
            } else {
                fileName = new String(fileName.getBytes(), "ISO8859-1");
            }
            // 设置response参数，可以打开下载页面
            response.setContentType("application/vnd.ms-excel;charset=utf-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
            OutputStream out = response.getOutputStream();
            ExcelExport.exportExcel(headers, fieldNames, dataset, out);
            out.flush();
        }
    }
    
    //@SuppressWarnings("deprecation")
	@RequestMapping("person-info-worknumber-import")
    @Log(desc = "人力资源", action = "update", operationDesc = "人力资源-花名册-工号信息导入")
    public String workNumberImport(Model model,
    		@RequestParam(value="excelFile",required = false) MultipartFile excelFile,
    		RedirectAttributes redirectAttributes) throws Exception {

		List<Map<String, Object>>  failMap = new ArrayList<Map<String,Object>>();
		
    	ReadExcel readExcel = new ReadExcel();
    	List<Map<String, Object>> userList = readExcel.getExcelInfo(excelFile);
    	if(userList!=null&&userList.size()>0){
    		//定制工号规则
        	String strDelete="TRUNCATE TABLE person_WorkNumber";
        	jdbcTemplate.update(strDelete);
        	String strInsert="insert into person_WorkNumber(id,numberNo,isUse) values(%s,%s,'%s')";
        	Integer intNum=0;
        	for(int i=1;i<2000;i++)
    		{
    			jdbcTemplate.update(String.format(strInsert,idGenerator.generateId(),intNum+i,"0"));
    		}
        	
          failMap =	personInfoService.importDataWrokNumber(userList);
        	
    	}
    		
    	else {
    		messageHelper.addFlashMessage(redirectAttributes, "core.success.publish", "导入EXCEL没有数据");
    		return "redirect:/auth/user-config-set-i.do";
		}
    	
    	if (failMap.size()>0){
	    	model.addAttribute("failMap",failMap);
	    	return "auth/user-config-set-i";
    	}else{
    		messageHelper.addFlashMessage(redirectAttributes, "core.success.publish", "导入成功");
    		return "redirect:/auth/user-config-set-i.do";
    	}
    	
    }
	
	//查看 合同单位管理菜单
	  @RequestMapping("contract-company-manage-list-i")
	    public String contractCompanyManageList(@ModelAttribute Page page,
	            @RequestParam Map<String, Object> parameterMap, Model model) {
	        String tenantId = tenantHolder.getTenantId();
	        List<PropertyFilter> propertyFilters = PropertyFilter
	                .buildFromMap(parameterMap);
	        propertyFilters.add(new PropertyFilter("EQS_tenantId", tenantId));
	        page = contractCompanyManager.pagedQuery(page, propertyFilters);

	        model.addAttribute("page", page);
	        return "user/contract-company-manage-list-i";
	    }
	  
	//查看 合同单位管理菜单
	  @RequestMapping("contract-company-manage-list")
	    public String contractCompanyManageListList(@ModelAttribute Page page,
	            @RequestParam Map<String, Object> parameterMap, Model model) {
	        
	        return "user/contract-company-manage-list";
	    }
	  
	  //新建合同单位
	    @RequestMapping("contract-company-manage-new")
	    public String contractCompanyManageNew(Model model) {
	    	//ckx 2019/2/1
	    	List<Map<String, Object>> smtpServerList = jdbcTemplate.queryForList("select i.id, t.descn,i.name,i.value from dict_type t LEFT JOIN dict_info i ON t.id = i.TYPE_ID where t.`NAME` = 'mailSmtpServer';");
	    	List<Map<String, Object>> popServerList = jdbcTemplate.queryForList("select i.id, t.descn,i.name,i.value from dict_type t LEFT JOIN dict_info i ON t.id = i.TYPE_ID where t.`NAME` = 'mailPop3Server';");
	    	model.addAttribute("smtpServerList", smtpServerList);
	    	model.addAttribute("popServerList", popServerList);
	       return "user/contract-company-manage-new";
	    }
	    
	  //编辑合同单位
	    @RequestMapping("contract-company-manage-update")
	    public String input(@RequestParam(value = "id", required = false) Long id,
	            Model model) {
	        if (id != null) {
	        	PersonContractCompanyManage pccm = contractCompanyManager.get(id);
	        	//ckx 2019/2/1
	            List<Map<String, Object>> smtpServerList = jdbcTemplate.queryForList("select i.id, t.descn,i.name,i.value from dict_type t LEFT JOIN dict_info i ON t.id = i.TYPE_ID where t.`NAME` = 'mailSmtpServer';");
		    	List<Map<String, Object>> popServerList = jdbcTemplate.queryForList("select i.id, t.descn,i.name,i.value from dict_type t LEFT JOIN dict_info i ON t.id = i.TYPE_ID where t.`NAME` = 'mailPop3Server';");
		    	model.addAttribute("model", pccm);
		    	model.addAttribute("smtpServerList", smtpServerList);
		    	model.addAttribute("popServerList", popServerList);
	        }
	        return "user/contract-company-manage-update";
	    }
	    
	  //保存 合同单位
	    @RequestMapping("contract-company-manage-save")
	    public String save(HttpServletRequest request,
	    		@ModelAttribute PersonContractCompanyManage dictType,
	            @RequestParam Map<String, Object> parameterMap,
	            //@RequestParam Long id,
	            RedirectAttributes redirectAttributes,
	            @RequestParam(value="smtpServerId" ,required=true) String smtpServerId,
	            @RequestParam(value="popServerId" ,required=true) String popServerId) {
	    	
	    	Long id = dictType.getId();
	        String tenantId = tenantHolder.getTenantId();
	        PersonContractCompanyManage dest = null;
	        //ckx 2019/2/1
	        DictInfo newDictInfoSmtp = dictInfoManager.findUniqueBy("id", Long.parseLong(smtpServerId));
            DictInfo newDictInfoPop = dictInfoManager.findUniqueBy("id", Long.parseLong(popServerId));
	        if (id != null) {
	            dest = contractCompanyManager.get(id);
	            String oldSmtpServerId = "";
	            String oldPopServerId = "";
	            DictInfo oldSmtpServer = dest.getSmtpServer();
	            DictInfo oldPopServer = dest.getPopServer();
	            if(null != oldSmtpServer){
	            	oldSmtpServerId = String.valueOf(oldSmtpServer.getId());
	            }
	            if(null != oldPopServer){
	            	oldPopServerId = String.valueOf(oldPopServer.getId());
	            }	
	            
	            //插入日志
	            String logContentString = "合同单位修改： ";
	           	if(!smtpServerId.equals(oldSmtpServerId) ){
	            	logContentString = logContentString+ oldSmtpServerId+" 修改为 "+newDictInfoSmtp.getValue()+" ";
	            }
	           
	           	if(!popServerId.equals(oldPopServerId)){
	            	logContentString = logContentString+ oldPopServerId+" 修改为 "+newDictInfoPop.getValue()+" ";
	            }
	           
	            if(!dest.getContractCompanyName().equals(dictType.getContractCompanyName())){
	            	logContentString = logContentString+ dest.getContractCompanyName()+" 修改为 "+dictType.getContractCompanyName()+" ";
	            } 
	            
	            if(!dest.getCompanyEmail().equals(dictType.getCompanyEmail())){
	            	logContentString = logContentString+ dest.getCompanyEmail()+" 修改为 "+dictType.getCompanyEmail()+" ";
	            }
	            
	            if(!dest.getIsenable().equals(dictType.getIsenable())){
	            	String oldEnableString = "";String newEnableString = "";
	            	if (dest.getIsenable().equals("是")) {	oldEnableString="启用";}else{	oldEnableString="不启用";}
	            	if (dictType.getIsenable().equals("是")) {	newEnableString="启用";}else{	newEnableString="不启用";}
	            	logContentString = logContentString+ oldEnableString+" 修改为 "+newEnableString;
	            }
	            if(logContentString!=null&&!logContentString.equals("合同单位修改： ")){
	            	logger.info( logContentString);
	            }
	            beanMapper.copy(dictType, dest);
	            //contractCompanyManager.remove(dictType);
	            
	        } else {
	            dest = dictType;
	            dest.setTenantId(tenantId);
	        }

	        dest.setSmtpServer(newDictInfoSmtp);
            dest.setPopServer(newDictInfoPop);
	        contractCompanyManager.save(dest);

	        messageHelper.addFlashMessage(redirectAttributes, "core.success.save",
	                "保存成功");

	        return "redirect:/user/contract-company-manage-list-i.do";
	        
	    }
	    
	    

	    //删除 合同单位
	    @RequestMapping("delete-contract-company")
	    public String deleteAttendanceRecord(@RequestParam(value = "id", required = false) Long id
	    		,RedirectAttributes redirectAttributes) throws Exception {	
	    	
	    	PersonContractCompanyManage personContractCompanyManage = contractCompanyManager.get(id);
	    	if(personContractCompanyManage!=null){
	    		contractCompanyManager.remove(personContractCompanyManage);
	    	}
	    	messageHelper.addFlashMessage(redirectAttributes, "core.success.save", "删除成功");
	    	return "redirect:/user/contract-company-manage-list-i.do";
	    }   
	    
    // ~ ======================================================================
    @Resource
    public void setAccountInfoManager(AccountInfoManager accountInfoManager) {
        this.accountInfoManager = accountInfoManager;
    }

    @Resource
    public void setPersonInfoManager(PersonInfoManager personInfoManager) {
        this.personInfoManager = personInfoManager;
    }

    @Resource
    public void setMessageHelper(MessageHelper messageHelper) {
        this.messageHelper = messageHelper;
    }

    @Resource
    public void setExportor(Exportor exportor) {
        this.exportor = exportor;
    }

    @Resource
    public void setTenantHolder(TenantHolder tenantHolder) {
        this.tenantHolder = tenantHolder;
    }

    @Resource
    public void setPartyStructTypeManager(
            PartyStructTypeManager partyStructTypeManager) {
        this.partyStructTypeManager = partyStructTypeManager;
    }

    @Resource
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Resource
    public void setCurrentUserHolder(CurrentUserHolder currentUserHolder) {
        this.currentUserHolder = currentUserHolder;
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
    public void setPersonInfoService(PersonInfoService personInfoService) {
        this.personInfoService = personInfoService;
    }

    @Resource
    public void setProcessConnector(ProcessConnector processConnector) {
        this.processConnector = processConnector;
    }

    @Resource
    public void setHumanTaskConnector(HumanTaskConnector humanTaskConnector) {
        this.humanTaskConnector = humanTaskConnector;
    }

    @Resource
    public void setDictConnector(DictConnector dictConnector) {
        this.dictConnector = dictConnector;
    }

    @Resource
    public void setAccountCredentialManager(AccountCredentialManager accountCredentialManager) {
        this.accountCredentialManager = accountCredentialManager;
    }

    @Resource
    public void setWorkTaskResource(WorkTaskResource workTaskResource) {
        this.workTaskResource = workTaskResource;
    }

    @Resource
    public void setProjectResource(ProjectResource projectResource) {
        this.projectResource = projectResource;
    }
    
    @Resource
	public void setCustomManager(CustomManager customManager) {
	}
    
    @Resource
    public void setOperationService(OperationService operationService) {
        this.operationService = operationService;
    }
    
    @Resource
	public void setCustomService(CustomService customService) {
	}
    
    @Resource
	public void setOrgConnector(OrgConnector orgConnector){
	}

    @Resource
	public void setRosterLogManager(RosterLogManager rosterLogManager) {
	}
    
    @Resource
    public void setKeyValueConnector(KeyValueConnector keyValueConnector) {
    }
    
    @Resource
    public void setPartyConnector(PartyConnector partyConnector) {
        this.partyConnector = partyConnector;
    }

    @Resource
	public void setUpdatePersonManager(UpdatePersonManager updatePersonManager) {
		this.updatePersonManager = updatePersonManager;
	}
    
    @Resource
	public void setIdGenerator(IdGenerator idGenerator) {
		this.idGenerator = idGenerator;
	}
    
    @Resource
    public void setUserService(UserService userService) {
		this.userService = userService;
	}
    
    @Resource
    public void setWorkTaskInfoManager(WorkTaskInfoManager workTaskInfoManager) {
    }

	@Resource
	public void setContractCompanyManager(
			ContractCompanyManager contractCompanyManager) {
		this.contractCompanyManager = contractCompanyManager;
	}
   
}
