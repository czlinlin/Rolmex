package com.mossle.party.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Null;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.ctc.wstx.util.StringUtil;
import com.google.common.base.Joiner;
import com.graphbuilder.math.func.AtanFunction;
import com.hp.hpl.sparta.xpath.AttrLessExpr;
import com.mossle.api.party.PartyConnector;
import com.mossle.api.party.PartyDTO;
import com.mossle.api.tenant.TenantHolder;
import com.mossle.core.auth.CurrentUserHolder;
import com.mossle.core.mapper.BeanMapper;
import com.mossle.core.mapper.JsonMapper;
import com.mossle.core.util.StringUtils;
import com.mossle.operation.persistence.domain.CustomEntity;
import com.mossle.operation.persistence.manager.CustomManager;
import com.mossle.operation.service.CustomService;
import com.mossle.party.PartyConstants;
import com.mossle.party.persistence.domain.OrgLogEntity;
import com.mossle.party.persistence.domain.PartyEntity;
import com.mossle.party.persistence.domain.PartyStruct;
import com.mossle.party.persistence.domain.PartyStructType;
import com.mossle.party.persistence.domain.PartyType;
import com.mossle.party.persistence.manager.OrgLogManager;
import com.mossle.party.persistence.manager.PartyEntityManager;
import com.mossle.party.persistence.manager.PartyStructManager;
import com.mossle.party.persistence.manager.PartyStructTypeManager;
import com.mossle.party.persistence.manager.PartyTypeManager;
import com.mossle.party.web.OrgController;
import com.mossle.user.PersonInfoConstants;
import com.mossle.user.persistence.domain.PersonInfo;
import com.mossle.user.persistence.domain.RosterLog;
import com.mossle.user.persistence.domain.UpdatePerson;
import com.mossle.user.persistence.manager.PersonInfoManager;
import com.mossle.user.persistence.manager.RosterLogManager;
import com.mossle.user.persistence.manager.UpdatePersonManager;
import com.mossle.user.service.PersonInfoService;
import com.mossle.user.service.UserService;
import com.mossle.util.DateUtil;

import org.activiti.engine.impl.cmd.CreateAttachmentCmd;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional(readOnly=true)
public class OrgService {
	private static Logger logger = LoggerFactory.getLogger(OrgController.class);
    private PartyEntityManager partyEntityManager;
    private PartyTypeManager partyTypeManager;
    private PartyStructManager partyStructManager;
    private PartyStructTypeManager partyStructTypeManager;
    private JdbcTemplate jdbcTemplate;
    private BeanMapper beanMapper = new BeanMapper();
    private TenantHolder tenantHolder;
    private CurrentUserHolder currentUserHolder;
    private PersonInfoManager personInfoManager;
    private JsonMapper jsonMapper=new JsonMapper();
    private UpdatePersonManager updatePersonManager;
    private CustomService customService;
    private RosterLogManager rosterLogManager; 
    private PersonInfoService personInfoService;
    private CustomManager customManager;
    private PartyConnector partyConnector;
    private PartyService partyService;
    @Autowired
    private UserService userService;
    private OrgLogManager orgLogManager;
    /**
     * 直接添加组织机构-走流程的保存方法
     * **/
    @Transactional(readOnly=false)
    public void partyInfoAddForAudit(
    		HttpServletRequest request,
            @ModelAttribute PartyStruct partyStruct,
            String childEntityRef,
            Long childEntityId,
            String childEntityName,
            String shortName,
            String isDisplay,
            String username,
            Long partyEntityId,
            Long partyTypeId,
            Long partyStructTypeId,
            int partyLevel,
            int priority
    		) throws Exception{
    	Map<String,Object> jsonMap=new HashMap<String, Object>();
    	jsonMap.put("partyStructId", partyStruct.getId());
    	jsonMap.put("childEntityRef", childEntityRef);
    	jsonMap.put("childEntityId", childEntityId);
    	jsonMap.put("childEntityName",childEntityName);
    	jsonMap.put("shortName", shortName);
    	jsonMap.put("isDisplay", isDisplay);
    	jsonMap.put("username", username);
    	jsonMap.put("partyEntityId",partyEntityId);
    	jsonMap.put("partyTypeId", partyTypeId);
    	jsonMap.put("partyStructTypeId",partyStructTypeId);
    	jsonMap.put("partyLevel",partyLevel);
    	jsonMap.put("priority",priority);
    	jsonMap.put("accountId",Long.valueOf(currentUserHolder.getUserId()));
    	
        UpdatePerson updatePerson = new UpdatePerson();
		updatePerson.setJsonContent(jsonMapper.toJson(jsonMap));
		updatePerson.setApplyCode(request.getParameter("applyCode"));
		updatePerson.setIsApproval("2");
		updatePerson.setUpdateParameters("");
		updatePerson.setEmployeeNo(partyEntityId.toString());
		updatePerson.setTypeID("orgadd");
		updatePersonManager.save(updatePerson);
    	    	
        //接下来发起流程（走的是自定义申请的流程）--------------------------------------------------
		StringBuffer contentBuffer=new StringBuffer();
		PartyType partyType=new PartyType();
        if(partyTypeId!=null)
        	partyType=partyTypeManager.findUniqueBy("id", partyTypeId);
        PartyEntity partyEntity=partyEntityManager.findUniqueBy("id", partyEntityId);
     	contentBuffer.append(String.format("上级机构：%s\n",partyEntity.getName()));
     	contentBuffer.append(String.format("新建%s：%s",partyType.getName(),childEntityName));
        
        MultipartFile[] files = null;
        String curentUserName = currentUserHolder.getName();
        customService.StartProcessCustomForPerson( request,curentUserName,contentBuffer.toString(),"",files,"4");
    }
    
    /**
     * 直接添加组织机构的保存方法
     * 或则流程审批结束后执行
     * **/
    @Transactional(readOnly=false)
    public void partyInfoAdd(
    		PartyStruct partyStruct,
    		String childEntityRef,
            Long childEntityId,
            String childEntityName,
            String shortName,
            String isDisplay,
            String username,
            Long partyEntityId,
            Long partyTypeId,
            Long partyStructTypeId,
            int partyLevel,
            int priority,
            Long accountId){
    	
    	PartyType partyType = partyTypeManager.get(partyTypeId);
        String tenantId = tenantHolder.getTenantId();
        //Long accountId = Long.parseLong(currentUserHolder.getUserId());
        
        if (partyType.getType() == PartyConstants.TYPE_USER) {
            /* zyl 2017-07-04
             * 原逻辑:在建人员的时候，是从账户选用户，不符合逻辑
             * 现逻辑:直接建立人员，在建账户时，选择人员
        	// 人员
            PartyEntity child = partyEntityManager.findUnique(
                    "from PartyEntity where partyType=? and ref=?", partyType,
                    childEntityRef);
            logger.debug("child : {}", child);
            */
            
            PartyEntity child = new PartyEntity();
            
            child.setName(username);
            child.setTenantId(tenantId);
            child.setLevel(partyLevel+1);
            child.setDelFlag(PersonInfoConstants.DELETE_FLAG_NO);
            child.setPartyType(partyTypeManager.get(partyTypeId));
            partyEntityManager.save(child);
            
            PartyEntity parent = partyEntityManager.get(partyEntityId);

            PartyStruct dest = new PartyStruct();
            beanMapper.copy(partyStruct, dest);
            dest.setPartyStructType(partyStructTypeManager
                    .get(partyStructTypeId));
            dest.setParentEntity(parent);
            dest.setChildEntity(child);
            dest.setTenantId(tenantId);
            dest.setAddUserId(accountId);
            dest.setPriority(priority);
            partyStructManager.save(dest);
        } else if (partyType.getType() == PartyConstants.TYPE_POSITION) {
            // 岗位
            PartyEntity child = null;
            if (childEntityId == null) {
                child = new PartyEntity();
                child.setName(childEntityName);
                child.setPartyType(partyType);
                child.setTenantId(tenantId);
                child.setLevel(partyLevel+1);
                child.setDelFlag(PersonInfoConstants.DELETE_FLAG_NO);
                child.setIsDisplay(isDisplay);
                partyEntityManager.save(child);
                //新建岗位编号
                setOrgNumber(child.getId());
                
            } else {
                child = new PartyEntity();
                child.setName(childEntityName);
                child.setPartyType(partyType);
                child.setTenantId(tenantId);
                child.setLevel(partyLevel+1);
                child.setDelFlag(PersonInfoConstants.DELETE_FLAG_NO);
                child.setIsDisplay(isDisplay);
                partyEntityManager.save(child);
            }

            //logger.debug("child : {}", child);

            PartyEntity parent = partyEntityManager.get(partyEntityId);

            PartyStruct dest = new PartyStruct();
            beanMapper.copy(partyStruct, dest);
            dest.setPartyStructType(partyStructTypeManager
                    .get(partyStructTypeId));
            dest.setParentEntity(parent);
            dest.setChildEntity(child);
            dest.setTenantId(tenantId);
            dest.setAddUserId(accountId);
            dest.setPriority(priority);
            partyStructManager.save(dest);
            
            //chengze 20181016
            OrgLogEntity orgLogEntity  = new OrgLogEntity();
            orgLogEntity.setModifyContent("新创建： "+child.getName());
            orgLogEntity.setOrgID(child.getId());
            orgLogEntity.setOperationID(accountId);
            orgLogEntity.setOperationTime(new Date());
            orgLogManager.save(orgLogEntity);
            
            
            
        } else {
            // 组织
            PartyEntity child = null;
            
            if (childEntityId == null) {
                child = new PartyEntity();
                child.setName(childEntityName);
                child.setShortName(shortName);
                child.setIsDisplay(isDisplay);
                child.setPartyType(partyType);
                child.setTenantId(tenantId);
                child.setLevel(partyLevel+1);
                child.setDelFlag(PersonInfoConstants.DELETE_FLAG_NO);
                child.setIsDisplay(isDisplay);
                partyEntityManager.save(child);
            } else {
                child = partyEntityManager.get(childEntityId);
            }

            //logger.debug("child : {}", child);
            PartyEntity parent = partyEntityManager.get(partyEntityId);
            PartyStruct dest = new PartyStruct();
            beanMapper.copy(partyStruct, dest);
            dest.setPartyStructType(partyStructTypeManager
                    .get(partyStructTypeId));
            dest.setParentEntity(parent);
            dest.setChildEntity(child);
            dest.setTenantId(tenantId);
            dest.setAddUserId(accountId);
            dest.setPriority(priority);
            partyStructManager.save(dest);
            
            OrgLogEntity orgLogEntity  = new OrgLogEntity();
            orgLogEntity.setModifyContent("新创建： "+child.getName());
            orgLogEntity.setOrgID(child.getId());
            orgLogEntity.setOperationID(accountId);
            orgLogEntity.setOperationTime(new Date());
            orgLogManager.save(orgLogEntity);
            
        }
    }
    
    /**
     * 自动设置岗位编号(岗位时执行)
     * add {@link Byte} lilei {@link AtanFunction} 2018-09-12
     * */
    private void setOrgNumber(Long orgId) {
		String strNum="";
		String strSql="select MAX(positionNo) as maxPositionNo from party_entity_attr";
		String num=jdbcTemplate.queryForObject(strSql, String.class);
		if(StringUtils.isBlank(num))
			strNum="0001";
		else{
			strNum=String.valueOf((Integer.parseInt(num)+1));
			if(strNum.length()==1)
				strNum="000"+strNum;
			else if(strNum.length()==2) 
				strNum="00"+strNum;
			else if(strNum.length()==3)
				strNum="0"+strNum;
		}
		strSql=String.format("insert into party_entity_attr(id,positionNo,isRealPosition,positionRealIds) "
				+ "values(%s,'%s','%s','%s')", orgId,strNum,"0","");
		jdbcTemplate.update(strSql);
	}
   
    /**
     * 直接修改组织机构走流程的保存方法
     * @throws Exception 
     * 
     * **/
    @Transactional(readOnly=false)
    public void partyInfoUpdateForAudit(
    		HttpServletRequest request,
    		Long structId,
            Long childEntityId,
            String childEntityName,
            String shortName,
            String isDisplay,
            int priority,
            String departmentCode,
            String departmentName
            ) throws Exception{
    	Map<String,Object> jsonMap=new HashMap<String, Object>();
    	jsonMap.put("structId", structId);
    	jsonMap.put("childEntityId", childEntityId);
    	jsonMap.put("childEntityName", childEntityName);
    	jsonMap.put("shortName",shortName);
    	jsonMap.put("isDisplay", isDisplay);
    	jsonMap.put("priority", priority);
    	jsonMap.put("departmentCode", departmentCode);
    	jsonMap.put("departmentName", departmentName);
    	
        UpdatePerson updatePerson = new UpdatePerson();
		updatePerson.setJsonContent(jsonMapper.toJson(jsonMap));
		updatePerson.setApplyCode(request.getParameter("applyCode"));
		updatePerson.setIsApproval("2");
		updatePerson.setUpdateParameters("");
		updatePerson.setEmployeeNo(childEntityId.toString());
		updatePerson.setTypeID("orgupdate");
		updatePersonManager.save(updatePerson);
    	
		PartyStruct partyStruct=partyStructManager.findUniqueBy("id", structId);
		StringBuffer contentBuffer=new StringBuffer();
		
		String parentName = partyStruct.getParentEntity().getName();
		if(parentName!=null&& !parentName.equals(departmentName)){
			contentBuffer.append(String.format("调整机构：%s\n",partyStruct.getChildEntity().getName()));
			contentBuffer.append(String.format("[上级机构] 由“%s”修改为“%s”\n",parentName,departmentName));
		}else {
			contentBuffer.append(String.format("上级机构：%s\n",partyStruct.getParentEntity().getName()));
	     	contentBuffer.append(String.format("调整机构：%s\n",partyStruct.getChildEntity().getName()));
		}
     	if(!childEntityName.equals(partyStruct.getChildEntity().getName()))
     		contentBuffer.append(String.format("[名称] 由“%s”修改为“%s”\n",partyStruct.getChildEntity().getName(),childEntityName));
     	if(partyStruct.getChildEntity().getShortName()!=null&&
     			!shortName.equals(partyStruct.getChildEntity().getShortName()))
     		contentBuffer.append(String.format("[缩写] 由“%s”修改为“%s”\n",partyStruct.getChildEntity().getShortName(),shortName));
     	if(priority!=partyStruct.getPriority())
     		contentBuffer.append(String.format("[排序] 由“%s”修改为“%s”\n",partyStruct.getPriority().toString(),String.valueOf(priority)));
     	if(!isDisplay.equals(partyStruct.getChildEntity().getIsDisplay()))
     		contentBuffer.append(String.format("[是否显示] 由“%s”修改为“%s”",
     				partyStruct.getChildEntity().getIsDisplay()=="1"?"是":"否",
     				isDisplay=="1"?"是":"否"));
		
        //接下来发起流程（走的是自定义申请的流程）--------------------------------------------------
        MultipartFile[] files = null;
        String curentUserName = currentUserHolder.getName();
        customService.StartProcessCustomForPerson( request,curentUserName,contentBuffer.toString(),"",files,"5");
    }
    
    /**
     * 新建或者修改组织机构，
     * 驳回发起人后，修改”组织机构“内容保存的方法
     * **/
    @Transactional(readOnly=false)
    public void partyInfoAddAndEditForAudit(
		String applyCode,
		Long partyStructId,
        String childEntityRef,
        Long childEntityId,
        String childEntityName,
        String shortName,
        String isDisplay,
        String username,
        Long partyEntityId,
        Long partyTypeId,
        Long partyStructTypeId,
        int partyLevel,
        int priority,
        Long accountId,
        String departmentCode,
        String departmentName
		){
    	try
    	{
    	UpdatePerson updatePerson=updatePersonManager.findUniqueBy("applyCode", applyCode);
    	StringBuffer contentBuffer=new StringBuffer();
    	PartyType partyType=new PartyType();
    	if(updatePerson!=null){
    		if(updatePerson.getTypeID().equals("orgadd")){
    			Map<String,Object> jsonMap=new HashMap<String, Object>();
            	jsonMap.put("partyStructId", partyStructId);
            	jsonMap.put("childEntityRef", childEntityRef);
            	jsonMap.put("childEntityId", childEntityId);
            	jsonMap.put("childEntityName",childEntityName);
            	jsonMap.put("shortName", shortName);
            	jsonMap.put("isDisplay", isDisplay);
            	jsonMap.put("username", username);
            	jsonMap.put("partyEntityId",partyEntityId);
            	jsonMap.put("partyTypeId", partyTypeId);
            	jsonMap.put("partyStructTypeId",partyStructTypeId);
            	jsonMap.put("partyLevel",partyLevel);
            	jsonMap.put("priority",priority);
            	jsonMap.put("accountId",accountId);
            	
            	updatePerson.setJsonContent(jsonMapper.toJson(jsonMap));
            	updatePersonManager.save(updatePerson);
            	
            	if(partyTypeId!=null)
                	partyType=partyTypeManager.findUniqueBy("id", partyTypeId);
            	
            	PartyEntity partyEntity=partyEntityManager.findUniqueBy("id", partyEntityId);
             	contentBuffer.append(String.format("上级机构：%s\n",partyEntity.getName()));
             	contentBuffer.append(String.format("新建%s：%s",partyType.getName(),childEntityName));
    		}
    		else if(updatePerson.getTypeID().equals("orgupdate")){
    			Map<String,Object> jsonMap=new HashMap<String, Object>();
            	jsonMap.put("structId", partyStructId);
            	jsonMap.put("childEntityId", childEntityId);
            	jsonMap.put("childEntityName", childEntityName);
            	jsonMap.put("shortName",shortName);
            	jsonMap.put("isDisplay", isDisplay);
            	jsonMap.put("priority", priority);
            	jsonMap.put("departmentCode",departmentCode);
            	jsonMap.put("departmentName",departmentName);
            	updatePerson.setJsonContent(jsonMapper.toJson(jsonMap));
            	updatePersonManager.save(updatePerson);
            	
            	
            	PartyStruct partyStruct=partyStructManager.findUniqueBy("id", partyStructId);
            	
            	String parentName = partyStruct.getParentEntity().getName();
            	if(parentName!=null&& !parentName.equals(departmentName)){
        			contentBuffer.append(String.format("调整机构：%s\n",partyStruct.getChildEntity().getName()));
        			contentBuffer.append(String.format("[上级机构] 由“%s”修改为“%s”\n",parentName,departmentName));
        		}else {
        			contentBuffer.append(String.format("上级机构：%s\n",partyStruct.getParentEntity().getName()));
        	     	contentBuffer.append(String.format("调整机构：%s\n",partyStruct.getChildEntity().getName()));
        		}
            	
            	if(!childEntityName.equals(partyStruct.getChildEntity().getName()))
             		contentBuffer.append(String.format("[名称] 由“%s”修改为“%s”\n",partyStruct.getChildEntity().getName(),childEntityName));
             	if(partyStruct.getChildEntity().getShortName()!=null&&
             			!shortName.equals(partyStruct.getChildEntity().getShortName()))
             		contentBuffer.append(String.format("[缩写] 由“%s”修改为“%s”\n",partyStruct.getChildEntity().getShortName(),shortName));
             	if(priority!=partyStruct.getPriority())
             		contentBuffer.append(String.format("[排序] 由“%s”修改为“%s”\n",partyStruct.getPriority().toString(),String.valueOf(priority)));
             	if(!isDisplay.equals(partyStruct.getChildEntity().getIsDisplay()))
             		contentBuffer.append(String.format("[是否显示] 由“%s”修改为“%s”",
             				partyStruct.getChildEntity().getIsDisplay().equals("1")?"是":"否",
             				isDisplay.equals("1")?"是":"否"));
    		}
    		
    		CustomEntity customEntity=customManager.findUniqueBy("applyCode",applyCode);
    		if(customEntity!=null){
    			String applyContent=contentBuffer.toString();
    			customEntity.setApplyContent(applyContent);
    			customManager.save(customEntity);
    		}
    	}
    	}
    	catch(Exception ex){
    		
    	}
    }
    
    /**
     * 直接修改组织机构的保存方法
     * 或则流程审批结束后执行
     * **/
    @Transactional(readOnly=false)
    public void partyInfoUpdate(
    		Long structId,
            Long childEntityId,
            String childEntityName,
            String shortName,
            String isDisplay,
            int priority,
            String partyEntityId,
            String partyEntityName){
    	Long accountId = Long.parseLong(currentUserHolder.getUserId());
    	
    	String  companyName = ""; 
    	String  companyid = "";

    	PartyEntity child  = partyEntityManager.get(childEntityId);
    	
    	
    	//先对比修改了哪些内容，记录下来,存入日志
    	String changeContent = "";
    	if (!child.getName().equals(childEntityName)) {
    		changeContent = changeContent + " 名称由"+child.getName()+"修改为"+childEntityName;
		}
    	if (child.getShortName()!=null && !child.getShortName().equals(shortName)) {
    		changeContent = changeContent + " 缩写由"+child.getShortName()+"修改为"+shortName;
		}
    	//取原上级机构，与新的对比，若有修改，记录到changeContent字符串中
    	 String sqlForFullName = "SELECT a.ID,a.`NAME` FROM party_entity a join party_struct b on a.ID = b.PARENT_ENTITY_ID WHERE b.CHILD_ENTITY_ID = '"+child.getId()+"'";
	     List<Map<String, Object>> listForFullName = jdbcTemplate.queryForList(sqlForFullName);
	     String oldParentName = listForFullName.get(0).get("NAME").toString();
	     String oldParentID = listForFullName.get(0).get("ID").toString();
		
    	if (!partyEntityId.equals(oldParentID)) {
    		changeContent = changeContent + " 上级机构由"+oldParentName+"修改为"+partyEntityName;
		}
    	//对比是否显示有没有修改
    	String oldDisplay = "";
    	String newDisplay = "";
    	if(isDisplay.equals("1")) newDisplay="显示"; 	else newDisplay="不显示";
    	if(child.getIsDisplay().equals("1")) oldDisplay="显示"; 	else oldDisplay="不显示";
    	if (!isDisplay.equals(child.getIsDisplay())) {
    		changeContent = changeContent + " "+oldDisplay+"修改为"+newDisplay;
		}
    	
    	
    	//更新PersonInfo
    	long partyType=child.getPartyType().getId();
	
    	child.setName(childEntityName);
    	child.setShortName(shortName);
    	child.setIsDisplay(isDisplay);
    	partyEntityManager.save(child);
    	
    	
    	 //chengze 20181016 修改内容,存入日志
    	if(!changeContent.equals("")){
	        OrgLogEntity orgLogEntity  = new OrgLogEntity();
	        orgLogEntity.setModifyContent(changeContent);
	        orgLogEntity.setOrgID(child.getId());
	        orgLogEntity.setOperationID(accountId);
	        orgLogEntity.setOperationTime(new Date());
	        orgLogManager.save(orgLogEntity);
    	}

        
    	PartyEntity parent = null;
    	parent = partyEntityManager.get(Long.parseLong(partyEntityId));
    	
    	PartyStruct partyStruct = partyStructManager.get(structId);
    	partyStruct.setPriority(priority);
    	partyStruct.setParentEntity(parent);
    	partyStructManager.save(partyStruct);
    	

    	if(partyType==PartyConstants.PARTY_TYPE_COMPANY){
    		//判断是否属于分公司（如果是，则看成部门）
    		String hqlString="from PartyStruct where childEntity=? and partyStructType.id=1";
    		List<PartyStruct> childPartyStructList=partyStructManager.find(hqlString,child);
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
    	else if((partyType==PartyConstants.PARTY_TYPE_DEPARTMENT)||(partyType==PartyConstants.PARTY_TYPE_GROUP)){
    		String hql="from PersonInfo where departmentCode=?";
	    	List<PersonInfo> personInfoList= personInfoManager.find(hql,child.getId().toString());
	    	if(personInfoList!=null&&personInfoList.size()>0){
	    		for (PersonInfo personInfo : personInfoList) {
					personInfo.setDepartmentName(childEntityName);
					personInfo.setCompanyName(partyEntityName);
					personInfo.setCompanyCode(partyEntityId);
					personInfoManager.save(personInfo);
				}
	    	}
    	}
    	//岗位
//    	else if(partyType==PartyConstants.PARTY_TYPE_POST){
//
////    		PartyDTO partyDto2 = 	partyConnector.findDepartmentById(child.getId().toString());
////    		 departName = partyDto2.getName();
////    		 departid = partyDto2.getId();
//    		PartyDTO partyDto1 = 	partyConnector.findCompanyById(partyEntityId);
//    		companyName = partyDto1.getName();
//  		  	companyid = partyDto1.getId();
//    		
//  		
//  		  	
//    		String hql="select p from PartyStruct as s ,PersonInfo as p where p.id =s.childEntity.id and s.partyStructType.id=4  and s.parentEntity.id = ?";
//	    	List<PersonInfo> personInfoList= personInfoManager.find(hql,child.getId());
//	    	if(personInfoList!=null&&personInfoList.size()>0){
//	    		for (PersonInfo personInfo : personInfoList) {
//	    			personInfo.setDepartmentCode(partyEntityId);
//					personInfo.setDepartmentName(partyEntityName);
//					personInfo.setCompanyName(companyName);
//					personInfo.setCompanyCode(companyid);
//					personInfoManager.save(personInfo);
//				}
//	    	}
//    	}
    
    }
    
    /**
     * 关联人员 add by lilei at 2018.05.31
     * **/
    @Transactional(readOnly=false)
    public void relationPostionPerson(
    		PartyStruct partyStruct,
            String childEntityRef,
            Long childEntityId,
            String childEntityName,
            Long partyEntityId,
            Long partyTypeId,
            Long partyStructTypeId,
            int priority,
    		Long accountId,
    		String applyCode){
    	
	    	//Long accountId = Long.parseLong(currentUserHolder.getUserId());
	        String tenantId = tenantHolder.getTenantId();
	        PartyType partyType = partyTypeManager.get(partyTypeId);
	        
	        if(applyCode.equals("0")){
	        	//记录日志
		        String positionName=personInfoService.getPositionCompany(partyEntityId);
		    	PartyEntity partyEntity=partyEntityManager.findUniqueBy("id", Long.valueOf(childEntityRef));
		    	RosterLog rosterLog = new RosterLog ();
			    rosterLog.setCode(applyCode);     									//受理单编号.
			    rosterLog.setOperationID(currentUserHolder.getName()); 				//操作人员id.
			    rosterLog.setContentBefore(partyEntity.getName());  				//修改之前的内容.
			    rosterLog.setContentNew("关联了岗位“"+positionName+"”");				//修改后的新内容.
			    rosterLog.setUpdateColumn(""); 										//被修改的字段名.
			    rosterLog.setUpdateColumnName("人员");
			    rosterLog.setIsapproval("0");										//1：流程审核还未通过   0：流程审核通过
			    rosterLog.setUpdateTime(new Date());  	  							//修改时间.
			    rosterLog.setEmployeeNo(childEntityRef.toString());					//被修改的员工编号.
			    rosterLogManager.save(rosterLog);
	        }
	        
	
	        // 岗位人员
	        PartyStructType partyStructType = partyStructTypeManager.get(PartyConstants.PARTY_STRUCT_TYPE_POSITION_USER);
	        if (partyType.getType() == PartyConstants.TYPE_USER) {
	            PartyEntity child = partyEntityManager.findUnique(
	                    "from PartyEntity where partyType=? and id=?", partyType, Long.parseLong(childEntityRef));
	            //logger.debug("child : {}", child);
	
	            PartyEntity parent = partyEntityManager.get(partyEntityId);
	            PartyStruct dest = new PartyStruct();
	            beanMapper.copy(partyStruct, dest);
	            dest.setPartyStructType(partyStructType);
	            dest.setParentEntity(parent);
	            dest.setChildEntity(child);
	            dest.setAddUserId(accountId);
	            dest.setTenantId(tenantId);
	            dest.setPriority(priority);
	            partyStructManager.save(dest);
	            
	            
	            //chengze 20181016 记录组织机构日志
	            OrgLogEntity orgLogEntity  = new OrgLogEntity();
	            orgLogEntity.setModifyContent(/*parent.getName()+*/"关联人员： "+child.getName());
	            orgLogEntity.setOrgID(parent.getId());
	            orgLogEntity.setOperationID(accountId);
	            orgLogEntity.setOperationTime(new Date());
	            orgLogManager.save(orgLogEntity);
	        } else {
	        	logger.info("关联人员能："+partyType.getType());
	            //logger.info("unsupport : {}", partyType.getType());
	        }
    }
    
    /**
     * 关联人员的走流程
     * add by lilei at 2018-06-06
     * **/
    @Transactional(readOnly=false)
    public  void relationPositionPersonForAudit(
		HttpServletRequest request,
		String applyCode,
		Long structId,
		Long childEntityRef,
		Long childEntityId,
		String childEntityName,
		Long partyEntityId,
		Long partyTypeId,
		Long partyStructTypeId,
		int priority) throws Exception {
    	
    	String positionName=personInfoService.getPositionCompany(partyEntityId);
    	PartyEntity partyEntity=partyEntityManager.findUniqueBy("id", childEntityRef);
    	RosterLog rosterLog = new RosterLog ();
	    rosterLog.setCode(applyCode);     									//受理单编号.
	    rosterLog.setOperationID(currentUserHolder.getName()); 				//操作人员id.
	    rosterLog.setContentBefore(partyEntity.getName());  				//修改之前的内容.
	    rosterLog.setContentNew("关联了岗位"+positionName);					//修改后的新内容.
	    rosterLog.setUpdateColumn(""); 										//被修改的字段名.
	    rosterLog.setUpdateColumnName("人员");
	    rosterLog.setIsapproval("1");										//1：流程审核还未通过   0：流程审核通过
	    rosterLog.setUpdateTime(new Date());  	  							//修改时间.
	    rosterLog.setEmployeeNo(childEntityRef.toString());					//被修改的员工编号.
	    rosterLogManager.save(rosterLog);
    	
    	Map<String,Object> jsonMap=new HashMap<String, Object>();
    	jsonMap.put("structId", structId);
    	jsonMap.put("childEntityRef", childEntityRef);
    	jsonMap.put("childEntityId", childEntityId);
    	jsonMap.put("childEntityName",childEntityName);
    	jsonMap.put("partyEntityId", partyEntityId);
    	jsonMap.put("partyTypeId", partyTypeId);
    	jsonMap.put("partyStructTypeId", partyStructTypeId);
    	jsonMap.put("priority", priority);
    	jsonMap.put("accountId", Long.valueOf(currentUserHolder.getUserId()));
    	
        UpdatePerson updatePerson = new UpdatePerson();
		updatePerson.setJsonContent(jsonMapper.toJson(jsonMap));
		updatePerson.setApplyCode(request.getParameter("applyCode"));
		updatePerson.setIsApproval("2");
		updatePerson.setUpdateParameters("");
		updatePerson.setEmployeeNo(partyEntityId.toString());
		updatePerson.setTypeID("postwithperson");
		updatePersonManager.save(updatePerson);
    	
        //接下来发起流程（走的是自定义申请的流程）--------------------------------------------------
		StringBuffer contentBuffer=new StringBuffer();
		contentBuffer.append(String.format("岗位名称：%s\n",positionName));
     	contentBuffer.append(String.format("关联人员：%s",personInfoService.getPositionCompany(childEntityRef)));
        MultipartFile[] files = null;
        String curentUserName = currentUserHolder.getName();
        customService.StartProcessCustomForPerson( request,curentUserName,contentBuffer.toString(),"",files,"6");
	}
    
    /**
     * 关联人员-走流程-驳回发起人的操作
     * add by lilei at 2018-06-06
     * @throws IOException 
     * **/
    @Transactional(readOnly=false)
    public  void relationPositionPersonEditForAudit(
    		String applyCode,
            Long structId,
            Long childEntityRef,
            Long childEntityId,
            Long childEntityName,
            Long partyEntityId,
            Long partyTypeId,
            Long partyStructTypeId,
            int priority) throws Exception{
    	
    	String hqlString="from RosterLog where isapproval='1' and code=?";
    	RosterLog rosterLog =rosterLogManager.findUnique(hqlString,applyCode);
    	if(rosterLog!=null){
    		//String positionName=personInfoService.getPositionCompany(partyEntityId);
        	PartyEntity partyEntity=partyEntityManager.findUniqueBy("id", childEntityRef);
        	
    		rosterLog.setContentBefore(partyEntity.getName());  	//修改之前的内容.
  	      	//rosterLog.setContentNew(newPostion);					//修改后的新内容.
  	      	rosterLogManager.save(rosterLog);
    	}
    	
    	UpdatePerson updatePerson=updatePersonManager.findUniqueBy("applyCode",applyCode);
		if(updatePerson!=null){
			if(updatePerson.getIsApproval().equals("2")){
				Map<String,Object> jsonMap=new HashMap<String, Object>();
				jsonMap.put("structId", structId);
		    	jsonMap.put("childEntityRef", childEntityRef);
		    	jsonMap.put("childEntityId", childEntityId);
		    	jsonMap.put("childEntityName",childEntityName);
		    	jsonMap.put("partyEntityId", partyEntityId);
		    	jsonMap.put("partyTypeId", partyTypeId);
		    	jsonMap.put("partyStructTypeId", partyStructTypeId);
		    	jsonMap.put("priority", priority);
		    	jsonMap.put("accountId", Long.valueOf(currentUserHolder.getUserId()));
		    	
		    	updatePerson.setJsonContent(jsonMapper.toJson(jsonMap));
		    	updatePersonManager.save(updatePerson);
			}
			
			StringBuffer contentBuffer=new StringBuffer();
			String positionName=personInfoService.getPositionCompany(partyEntityId);
			contentBuffer.append(String.format("岗位名称：%s\n",positionName));
			PartyEntity partyEntity=partyEntityManager.findUniqueBy("id",childEntityRef); 
	     	contentBuffer.append(String.format("关联人员：%s",personInfoService.getPositionCompany(childEntityRef)));
	     	CustomEntity customEntity=customManager.findUniqueBy("applyCode",applyCode);
    		if(customEntity!=null){
    			customEntity.setApplyContent(contentBuffer.toString());
    			customManager.save(customEntity);
    		}
		}
    }
    /**
     * 查询考勤列表
     * 超级管理员 机器人 经销商隶属罗麦集团，罗麦集团根节点点击无响应
     * 故可不做筛选
  * @param partyEntityId
  * @param year
  * @param month
  * @param name
  * @return
  */
 public Map<String,Object> queryAttendance(Long partyEntityId,String year,String month,String name,String openOtherNameStatus){
 	   List<Map<String,Object>> userList = new ArrayList<>();
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
		String strSql="SELECT US.ref FROM AUTH_USER_ROLE UR "
				+" INNER JOIN AUTH_USER_STATUS US ON UR.USER_STATUS_ID=US.ID"
				+" WHERE ROLE_ID=2";
		systemAdminIdList=jdbcTemplate.queryForList(strSql, String.class);
		if(systemAdminIdList!=null&&systemAdminIdList.size()>0)
			strSystemAdminIds=Joiner.on(",").join(systemAdminIdList);
		
		if(!strSystemAdminIds.equals(""))
			strSerchRemoveId+=","+strSystemAdminIds;
		
		systemAdminIdList=jdbcTemplate.queryForList(strSql, String.class);
		
		//拼接年月日为离职人员做
    	int intYear=Integer.parseInt(year);
 	    int intMonth=Integer.parseInt(month);
 	    Calendar calSearch=Calendar.getInstance();
 	    //下个月的第一天
 	    calSearch.set(intYear, intMonth, 1);
	   if(name == null || name.equals("")){
		   // 点击 node 的直属人员
			String hql = "select p from PersonInfo as p,PartyEntity as e, PartyStruct as s "
					+ "where p.partyId = e.id and  e.id =s.childEntity  and s.partyStructType = "
					+ PartyConstants.PARTY_STRUCT_TYPE_ORG ;
			//and e.delFlag='0' and p.quitFlag='0'
			hql += " and s.parentEntity =" + partyEntityId + " order by p.employeeNo ";
			List<PersonInfo> personList = personInfoManager.find(hql);
			PartyEntity vo = partyEntityManager.get(partyEntityId);
			
	 	    String strQuitAbnormalId="";//记录没有离职时间的离职人员
			for (PersonInfo personInfo : personList) {
				//虚拟账号不加载
				if((","+strSerchRemoveId+",").contains(","+personInfo.getId()+","))
					continue;
				Map<String, Object> map = new HashMap<String, Object>();
				//离职人员的处理
				String quitDays = "";
				String quitMonth = "";
				if(personInfo.getQuitFlag().equals("1")){
					if(personInfo.getQuitTime()==null){
						strQuitAbnormalId+=personInfo.getId()+",";
						continue;
					}
					Date quitDate = personInfo.getLeaveDate();
					String formatDate = DateUtil.formatDate(quitDate, "");
					formatDate = formatDate.substring(0, 10);
 					quitDate = DateUtil.formatDateStr(formatDate+" 18:00:00", "");
 					//Date quitDate = DateUtil.formatDateStr(leaveDate+" 18:00:00", "");
					//Date quitDate=personInfo.getQuitTime();
 					//获取离职日期
 					quitDays = formatDate.substring(8, 10);
 					//获取离职月份
 					quitMonth = formatDate.substring(5, 7);
					Calendar calQuit=Calendar.getInstance();
 					calQuit.setTime(quitDate);
 					int intQuitYear=calQuit.get(Calendar.YEAR);
 					int intQuitMonth=calQuit.get(Calendar.MONTH)+1;
 					if(intQuitMonth>11){
 						intQuitYear+=1;
 						intQuitMonth=0;
 					}
 					calQuit.set(intQuitYear,intQuitMonth,1);
					
					//如果查询时间大于离职时间
					if(calSearch.after(calQuit)){
						continue;
					}
					map.put("isQuitFlag", "1");
					map.put("quitMonth", quitMonth);
   				}else{
   					map.put("isQuitFlag", "0");
   					map.put("quitMonth", quitMonth);
   				}
				
 				map.put("quitDays", quitDays);
 				map.put("id", personInfo.getId().toString());
 				map.put("department_code", vo.getId().toString());
 				map.put("department_name", vo.getName());
 				if(openOtherNameStatus.equals("1")){//表示别名是开启状态
 					map.put("real_name", personInfo.getRealName());
 				}else{
 					map.put("full_name", personInfo.getFullName());
 				}
 				map.put("employee_no", personInfo.getEmployeeNo());
 				userList.add(map);
 			}
 	       
 	    	List<Map<String, Object>> list =partyService.getChildDeparentById(partyEntityId);//一级子节点集合
 	    	for (Map oneMap : list) {
 	    		//通过id查询部门名称
 	    		PartyEntity partyEntiy = partyEntityManager.get(Long.parseLong(oneMap.get("id").toString()));
 	    		
 	    		String strChildPartyIds="";
 	    		List<String> childAllList=partyService.getAllDeparentById(Long.parseLong(oneMap.get("id").toString()));
 	    		if(childAllList!=null) {
 	    			strChildPartyIds=Joiner.on(",").join(childAllList);
 	    			
 	    			strChildPartyIds = strChildPartyIds + "," + oneMap.get("id"); //and e.delFlag='0' and p.quitFlag='0'
 	    	        hql = "select p from PersonInfo as p,PartyEntity as e, PartyStruct as s "
 	    	        		+ "where p.partyId = e.id and  e.id =s.childEntity  and s.partyStructType = " + PartyConstants.PARTY_STRUCT_TYPE_ORG;
 	    	        				
 	    	        
 	    			if(!com.mossle.core.util.StringUtils.isBlank(strChildPartyIds))
 	    				hql+="and s.parentEntity in ("+strChildPartyIds+") order by p.employeeNo ";
 	    	       
 	    	        List<PersonInfo> personInfoList =personInfoManager.find(hql) ;  
 	    	        for (PersonInfo personInfo : personInfoList) {
 	    	        	if(strSystemAdminIds.indexOf(personInfo.getId().toString())>=0||personInfo.getUsername().equals("releasetest")||personInfo.getUsername().toLowerCase().equals("testuser")){
 	    					continue;
 	    				}
 	    	        	Map<String,Object> map = new HashMap<String,Object>();
 	    	        	//ckx
 	    	        	//离职人员的处理
 	    	        	String quitDays = "";
 	    	        	String quitMonth = "";
	 	   				if(personInfo.getQuitFlag().equals("1")){
	 	   					if(personInfo.getQuitTime()==null){
	 	   						strQuitAbnormalId+=personInfo.getId()+",";
	 	   						continue;
	 	   					}
	 	   					Date quitDate = personInfo.getLeaveDate();
	 	   					String formatDate = DateUtil.formatDate(quitDate, "");
	 	   					formatDate = formatDate.substring(0, 10);
 	    					quitDate = DateUtil.formatDateStr(formatDate+" 18:00:00", "");
 	    					//Date quitDate = DateUtil.formatDateStr(leaveDate+" 18:00:00", "");
	 	   					//Date quitDate=personInfo.getQuitTime();
 	    					//获取离职日期
 	    					quitDays = formatDate.substring(8, 10);
 	    					//获取离职月份
 	    					quitMonth = formatDate.substring(5, 7);
	 	   					Calendar calQuit=Calendar.getInstance();
	 	    					calQuit.setTime(quitDate);
	 	    					int intQuitYear=calQuit.get(Calendar.YEAR);
	 	    					int intQuitMonth=calQuit.get(Calendar.MONTH)+1;
	 	    					if(intQuitMonth>11){
	 	    						intQuitYear+=1;
	 	    						intQuitMonth=0;
	 	    					}
	 	    					calQuit.set(intQuitYear,intQuitMonth,1);
	 	   					
	 	   					//如果查询时间大于离职时间
	 	   					if(calSearch.after(calQuit)){
	 	   						continue;
	 	   					}
	 	   					map.put("isQuitFlag", "1");
	 	   					map.put("quitMonth", quitMonth);
	 	   				}else{
	 	   					map.put("isQuitFlag", "0");
	 	   					map.put("quitMonth", quitMonth);
	 	   				}
 	    	        	
	 	   				map.put("quitDays", quitDays);
 	    	        	map.put("id", personInfo.getId().toString());
 	    	        	map.put("department_code", partyEntiy.getId().toString());
 	    	        	map.put("department_name", partyEntiy.getName());
 	    	        	if(openOtherNameStatus.equals("1")){//表示别名是开启状态
 	    					map.put("real_name", personInfo.getRealName());
 	    				}else{
 	    					map.put("full_name", personInfo.getFullName());
 	    				}
 	    	        	map.put("employee_no", personInfo.getEmployeeNo());
 	    	        	userList.add(map);
 	    	        }
 	    	       // p.id,p.DEPARTMENT_CODE,p.DEPARTMENT_NAME,p.FULL_NAME
 	    	        
 	    		}
 	    	}
 	   }else{//姓名条件不为空时
 			String strChildPartyIds = "";
 			List<String> childAllList = partyService.getAllDeparentById(partyEntityId);
 			if (childAllList != null) {
 				strChildPartyIds = Joiner.on(",").join(childAllList);

 				strChildPartyIds = strChildPartyIds + "," + partyEntityId;
 				//根据别名状态为查询名字的key赋值 hibernate对象查询 实体属性为key
 				String nameKey = "";
 				if(openOtherNameStatus.equals("1")){
 					nameKey = "p.realName";
 				}else{
 					nameKey = "p.fullName";
 				}
 				String hql = "select p from PersonInfo as p,PartyEntity as e, PartyStruct as s "  //and e.delFlag='0' and p.quitFlag='0'
 						+ "where p.partyId = e.id and  e.id =s.childEntity  and "+ nameKey+" like '%"+name+"%' and s.partyStructType = "
 						+ PartyConstants.PARTY_STRUCT_TYPE_ORG;

 				if (!com.mossle.core.util.StringUtils.isBlank(strChildPartyIds))
 					hql += "and s.parentEntity in (" + strChildPartyIds + ") order by p.employeeNo ";

 				List<PersonInfo> personInfoList = personInfoManager.find(hql);
 				for (PersonInfo personInfo : personInfoList) {
 					if(strSystemAdminIds.indexOf(personInfo.getId().toString())>=0||personInfo.getUsername().equals("releasetest")||personInfo.getUsername().toLowerCase().equals("testuser")){
 	 					continue;
 	 				}
 					Map<String, Object> map = new HashMap<String, Object>();
 					//离职人员的处理
 					String quitDays = "";
 					String quitMonth = "";
 					if(personInfo.getQuitFlag().equals("1")){
 						if(personInfo.getQuitTime()==null){
 							continue;
 						}
 						Date quitDate = personInfo.getLeaveDate();
 						String formatDate = DateUtil.formatDate(quitDate, "");
 						formatDate = formatDate.substring(0, 10);
 	 					quitDate = DateUtil.formatDateStr(formatDate+" 18:00:00", "");
 	 					//Date quitDate = DateUtil.formatDateStr(leaveDate+" 18:00:00", "");
 						//Date quitDate=personInfo.getQuitTime();
 	 					//获取离职日期
    					quitDays = formatDate.substring(8, 10);
    					//获取离职月份
     					quitMonth = formatDate.substring(5, 7);
 						Calendar calQuit=Calendar.getInstance();
 	 					calQuit.setTime(quitDate);
 	 					int intQuitYear=calQuit.get(Calendar.YEAR);
 	 					int intQuitMonth=calQuit.get(Calendar.MONTH)+1;
 	 					if(intQuitMonth>11){
 	 						intQuitYear+=1;
 	 						intQuitMonth=0;
 	 					}
 	 					calQuit.set(intQuitYear,intQuitMonth,1);
 						
 						//如果查询时间大于离职时间
 						if(calSearch.after(calQuit)){
 							continue;
 						}
 						map.put("isQuitFlag", "1");
 						map.put("quitMonth", quitMonth);
 	   				}else{
 	   					map.put("isQuitFlag", "0");
 	   					map.put("quitMonth", quitMonth);
 	   				}
 					
 					map.put("quitDays", quitDays);
 					map.put("id", personInfo.getId().toString());
 					map.put("department_code",personInfo.getDepartmentCode());
 					map.put("department_name", personInfo.getDepartmentName());
 					if(openOtherNameStatus.equals("1")){//表示别名是开启状态
 	 					map.put("real_name", personInfo.getRealName());
 	 				}else{
 	 					map.put("full_name", personInfo.getFullName());
 	 				}
 					map.put("employee_no", personInfo.getEmployeeNo());

 					userList.add(map);
 				}

 			}
 	   }
 		
     	
     	// userList.addAll(jdbcTemplate.queryForList(sql));
     	//~-------------------------考勤集合-----------------------------------
     	String attSql = "";
     	if(name == null  || name.equals("")){
     		attSql = "select * from attendance where year="+year+" and month="+month+" ORDER BY work_date";
 	 	}else{
 	 		attSql = "select * from attendance where year="+year+" and month="+month+" and worker like'%"+name+"%' ORDER BY work_date";
 	 	}
     	List<Map<String,Object>> attList = jdbcTemplate.queryForList(attSql);
     	
     	//ckx  考勤修改，默认显示系统设置的考勤时间
     	queryData(year, month, openOtherNameStatus, userList, attList);
     	
     	JSONArray array= JSONArray.parseArray(JSON.toJSONString(attList));
     	String name_key = "";
     	if(openOtherNameStatus.equals("1")){//开启别名
     		name_key = "p.real_name";
     	}else{
     		name_key = "p.full_name";
     	}
			
 
     	//请假详情
     	String leave = "";
     	if(name == null  || name.equals("")){
     		leave = "select customFormId,formType,type, userId, DATE_FORMAT(startTime,'%Y') YEAR,DATE_FORMAT(startTime,'%m') month,DATE_FORMAT(startTime,'%d') day"
 	 			       + " from leave_details l join kv_record k on l.customFormId=k.applyCode"
 	 			       +" where DATE_FORMAT(startTime,'%Y-%m')='"+year+"-"+month+"'"
 	 			       +" and k.audit_status=2";
     	}else{
     		leave = "select customFormId,formType,type, userId, DATE_FORMAT(startTime,'%Y') YEAR,DATE_FORMAT(startTime,'%m') month,DATE_FORMAT(startTime,'%d') day"
 	 			       + " from leave_details l join person_info p on p.id=l.userId join kv_record k on l.customFormId=k.applyCode"
 	 			       +" where DATE_FORMAT(startTime,'%Y-%m')='"+year+"-"+month+"'"
 	 			       +" and "+ name_key +" like '%"+name+"%'and k.audit_status=2";
     	}
 	 	
 	 	List<Map<String,Object>> leaveList = jdbcTemplate.queryForList(leave);
 	 	for(Map lea : leaveList){
 	 		Object objFormType = lea.get("formType");
 	 		Object objType = lea.get("type");
 		 	if(objFormType.equals("1")){//请假
 		 		if(objType.equals("1")){//病假
 		 			objType = "病假";
 		 		}else if(objType.equals("2")){//事假
 		 			objType = "事假";
 		 		}else if(objType.equals("3")){//倒休假
 		 			objType = "倒休假";
 		 		}else if(objType.equals("4")){//年假
 		 			objType = "年假";
 		 		}else if(objType.equals("5")){//补休假
 		 			objType = "补休假";
 		 		}else if(objType.equals("6")){//婚假
 		 			objType = "婚假";
 		 		}else if(objType.equals("7")){//产假
 		 			objType = "产假";
 		 		}else if(objType.equals("8")){//丧假
 		 			objType = "丧假";
 		 		}else if(objType.equals("9")){//其他
 		 			objType = "其他";
 		 		}
 		 		lea.put("type", objType);
 		 	}else if(objFormType.equals("2")){//出差
 		 		if(objType.equals("1")){//出差
 		 			objType = "出差";
 		 		}else if(objType.equals("2")){//因公外出
 		 			objType = "因公外出";
 		 		}else if(objType.equals("3")){//其他
 		 			objType = "其他";
 		 		}
 		 		lea.put("type", objType);
 		 	}else if(objFormType.equals("3")){//加班
 		 		objType = "加班";
 		 		lea.put("type", objType);
 		 	}else if(objFormType.equals("4")){//特殊考勤
 		 		if(objType.equals("1")){//销假
 		 			objType = "销假";
 		 		}else if(objType.equals("2")){//迟到
 		 			objType = "迟到";
 		 		}else if(objType.equals("3")){//临时外出
 		 			objType = "临时外出";
 		 		}else if(objType.equals("4")){//漏打卡
 		 			objType = "漏打卡";
 		 		}else if(objType.equals("5")){//其他
 		 			objType = "其他";
 		 		}
 		 		lea.put("type", objType);
 		 	}
 	 	}
 	 	
 	 	//离职人员
 	 	for (Map<String, Object> map : userList) {
 	 		String isQuitFlag = com.mossle.util.StringUtil.toString(map.get("isQuitFlag"));
 	 		if("1".equals(isQuitFlag)){
 	 			//获取当前月份有多少天
 	 			int monthDays = DateUtil.getMonthDays(year, month);
 	 			int quitDays = Integer.parseInt(map.get("quitDays").toString());
 	 			for (int i = quitDays; i <= monthDays; i++) {
					Map<String, Object> quitMap = new HashMap<String, Object>();
					quitMap.put("customFormId", "");
					quitMap.put("formType", "");
					quitMap.put("type", "");
					quitMap.put("userId", map.get("id"));
					quitMap.put("year", year);
					quitMap.put("month", month);
					quitMap.put("day", String.format("%02d", i));
					quitMap.put("quitMonth", com.mossle.util.StringUtil.toString(map.get("quitMonth")));
					leaveList.add(quitMap);
				}
 	 		}
		}
 	 	Map<String,Object> result = new HashMap<String,Object>();
 	 	result.put("userList", userList);
     	result.put("attList", attList);
     	result.put("leaveList", leaveList);
     	return result;
     }

	private void queryData(String year, String month,
			String openOtherNameStatus, List<Map<String, Object>> userList,
			List<Map<String, Object>> attList) {
		int monthDays = DateUtil.getMonthDays(year, month);
     	for (Map<String, Object> map : userList) {
			String userId = com.mossle.util.StringUtil.toString(map.get("id"));
			String employeeNo = com.mossle.util.StringUtil.toString(map.get("employee_no"));
			String departmentName = com.mossle.util.StringUtil.toString(map.get("department_name"));
			String departmentCode = com.mossle.util.StringUtil.toString(map.get("department_code"));
			String userName = "";
			if("1".equals(openOtherNameStatus)){//表示别名是开启状态
				userName = com.mossle.util.StringUtil.toString(map.get("real_name"));
				}else{
				userName = com.mossle.util.StringUtil.toString(map.get("full_name"));
				}
			//特殊考勤日期
			List<Map<String, Object>> queryForMapSpecialList = null;
			try {
				queryForMapSpecialList = jdbcTemplate.queryForList("select a.shiftID,a.specialDate from special_date a LEFT JOIN person_attendance_records p ON a.attendanceRecordsID = p.attendanceRecordsID where p.personID = '"+userId+"'");
			} catch (Exception e) {
			}
			for (int i = 1; i <= monthDays; i++) {
				boolean boo = false;
				for (Map<String, Object> map2 : attList) {
					String day = com.mossle.util.StringUtil.toString(map2.get("day"));
					String userListId = com.mossle.util.StringUtil.toString(map2.get("user_id"));
					
					if(userId.equals(userListId) && day.equals(String.format("%02d", i))){
						boo = true;
						break;
					}
				}
				if(!boo){
					Map<String, Object> attMap = new HashMap<String, Object>();
					//根据年月日获取考勤的上下班时间
					String dateStr = DateUtil.formatDateStrToStr(year+"-"+month+"-"+i, "yyyy-MM-dd");
		        	//查询当前人员班次时间
		        	String weekCSStart = DateUtil.getWeekCS(dateStr);
		        	String attendanceStart = DateUtil.getAttendance(weekCSStart);
		        	Map<String, Object> timeMap = null;
		    		try {
		    			timeMap = jdbcTemplate.queryForMap("select s.start_time , s.end_time from shift s where s.id in (select a."+attendanceStart+" from attendance_records a LEFT JOIN person_attendance_records p ON a.id = p.attendanceRecordsID where p.personID = '"+userId+"')");
		    		} catch (Exception e) {
		    		}
		    		String goTime = "";
		    		String offTime = "";
		    		if(null != timeMap){
		    			goTime = com.mossle.util.StringUtil.toString(timeMap.get("start_time"));
		    			offTime = com.mossle.util.StringUtil.toString(timeMap.get("end_time"));
		    		}
		    		//特殊考勤
					if(null != queryForMapSpecialList && queryForMapSpecialList.size() > 0){
						for (Map<String, Object> map2 : queryForMapSpecialList) {
							Object shiftID = map2.get("shiftID");
							String specialDate = com.mossle.util.StringUtil.toString(map2.get("specialDate"));
							
							Map<String, Object> queryForMap = null;
							try {
								queryForMap = jdbcTemplate.queryForMap("select s.start_time , s.end_time from shift s where s.id = '"+shiftID+"'");
							} catch (Exception e) {
							}
							if(null != queryForMap){
								if(com.mossle.common.utils.StringUtils.isNotBlank(com.mossle.util.StringUtil.toString(queryForMap.get("start_time")))){
									if(dateStr.equals(specialDate)){
										goTime = com.mossle.util.StringUtil.toString(queryForMap.get("start_time"));
									}
								}
								if(com.mossle.common.utils.StringUtils.isNotBlank(com.mossle.util.StringUtil.toString(queryForMap.get("end_time")))){
									if(dateStr.equals(specialDate)){
										offTime = com.mossle.util.StringUtil.toString(queryForMap.get("end_time"));
									}
								}
							}
						}
					}
		    		attMap.put("department_name", departmentName);
		    		attMap.put("department_id", departmentCode);
		    		attMap.put("user_id", userId);
		    		attMap.put("worker", userName);
		    		attMap.put("work_date", year+"-"+month+"-"+i);
		    		attMap.put("go_to_work", "");
		    		attMap.put("go_off_work", "");
		    		attMap.put("year", year);
		    		attMap.put("month", String.format("%02d", Integer.parseInt(month)));
		    		attMap.put("day", String.format("%02d", i));
		    		attMap.put("constraint_to_work", goTime);
		    		attMap.put("constraint_off_work", offTime);
		    		attMap.put("user_code", employeeNo);
		    		attList.add(attMap);
				}
			}
		}
	}
 
 
    // ~ ======================================================================
    @Resource
    public void setPartyEntityManager(PartyEntityManager partyEntityManager) {
        this.partyEntityManager = partyEntityManager;
    }

    @Resource
    public void setPartyTypeManager(PartyTypeManager partyTypeManager) {
        this.partyTypeManager = partyTypeManager;
    }

    @Resource
    public void setPartyStructManager(PartyStructManager partyStructManager) {
        this.partyStructManager = partyStructManager;
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
    public void setPersonInfoManager(PersonInfoManager personInfoManager) {
        this.personInfoManager = personInfoManager;
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
   	public void setRosterLogManager(RosterLogManager rosterLogManager) {
   		this.rosterLogManager = rosterLogManager;
   	}
    
    @Resource
   	public void setUpdatePersonManager(UpdatePersonManager updatePersonManager) {
   		this.updatePersonManager = updatePersonManager;
   	}
    
    @Resource
   	public void setCustomService(CustomService customService) {
   		this.customService = customService;
   	}
    
    @Resource
   	public void setPersonInfoService(PersonInfoService personInfoService) {
   		this.personInfoService = personInfoService;
   	}
    
    @Resource
   	public void setCustomManager(CustomManager customManager) {
   		this.customManager = customManager;
   	}

    @Resource
	public void setPartyConnector(PartyConnector partyConnector) {
		this.partyConnector = partyConnector;
	}
    
    @Resource
    public void setPartyService(PartyService partyService) {
        this.partyService = partyService;
    }
    
    @Resource
    public void setOrgLogManager(OrgLogManager orgLogManager) {
        this.orgLogManager = orgLogManager;
    }

	public List<Map<String,Object>> getAttrndanceStatisticeList(String name, Long partyEntityId, String startTime,String endTime) {
		//partyEntityId = (long) 853712512417792;
	   List<Map<String,Object>> resultData = new ArrayList<Map<String,Object>>();
		//先去获取别名功能是否开启
	   String openOtherNameStatus = userService.getOpenOtherNameStatus();
  	   List<Map<String,Object>> userList = new ArrayList<>();
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
 		String strSql="SELECT US.ref FROM AUTH_USER_ROLE UR "
 				+" INNER JOIN AUTH_USER_STATUS US ON UR.USER_STATUS_ID=US.ID"
 				+" WHERE ROLE_ID=2";
 		systemAdminIdList=jdbcTemplate.queryForList(strSql, String.class);
 		if(systemAdminIdList!=null&&systemAdminIdList.size()>0)
 			strSystemAdminIds=Joiner.on(",").join(systemAdminIdList);
 		
 		if(!strSystemAdminIds.equals(""))
 			strSerchRemoveId+=","+strSystemAdminIds;
 		
 		systemAdminIdList=jdbcTemplate.queryForList(strSql, String.class);
 		
 		Date startDate = DateUtil.formatDateStr(startTime, "yyyy-MM-dd HH:mm:ss");
		Calendar calSearch=Calendar.getInstance();
		calSearch.setTime(startDate);
 	   if(name == null || name.equals("")){
 		   // 点击 node 的直属人员
 			String hql = "select p from PersonInfo as p,PartyEntity as e, PartyStruct as s "
 					+ "where p.partyId = e.id and  e.id =s.childEntity  and s.partyStructType = "
 					+ PartyConstants.PARTY_STRUCT_TYPE_ORG ;
 			//and e.delFlag='0' and p.quitFlag='0'
 			hql += " and s.parentEntity =" + partyEntityId + " order by p.employeeNo ";
 			List<PersonInfo> personList = personInfoManager.find(hql);
 			PartyEntity vo = partyEntityManager.get(partyEntityId);
 			
 			//拼接年月日为离职人员做
 	    	/*int intYear=Integer.parseInt();
 	 	    int intMonth=Integer.parseInt();
 	 	    
 	 	    //下个月的第一天
 	 	    calSearch.set(intYear, intMonth, 1);*/
 			//查询的开始时间
 			
 	 	    String strQuitAbnormalId="";//记录没有离职时间的离职人员
 			for (PersonInfo personInfo : personList) {
 				//虚拟账号不加载
 				if((","+strSerchRemoveId+",").contains(","+personInfo.getId()+","))
 					continue;
 				
 				//离职人员的处理
 				if(personInfo.getQuitFlag().equals("1")){
 					if(personInfo.getQuitTime()==null){
 						strQuitAbnormalId+=personInfo.getId()+",";
 						continue;
 					}
 					
 					//Date quitDate=personInfo.getQuitTime();
 					Date quitDate = personInfo.getLeaveDate();
 					String formatDate = DateUtil.formatDate(quitDate, "");
					formatDate = formatDate.substring(0, 10);
 					quitDate = DateUtil.formatDateStr(formatDate+" 18:00:00", "");
 					//Date quitDate = DateUtil.formatDateStr(leaveDate+" 18:00:00", "");
 					
 					Calendar calQuit=Calendar.getInstance();
  					calQuit.setTime(quitDate);
  					/*int intQuitYear=calQuit.get(Calendar.YEAR);
  					int intQuitMonth=calQuit.get(Calendar.MONTH)+1;
  					if(intQuitMonth>11){
  						intQuitYear+=1;
  						intQuitMonth=0;
  					}
  					calQuit.set(intQuitYear,intQuitMonth,1);*/
 					
 					//如果查询时间大于离职时间
 					if(calSearch.after(calQuit)){
 						continue;
 					}
 				}
 				
  				Map<String, Object> map = new HashMap<String, Object>();
  				map.put("id", personInfo.getId().toString());
  				map.put("department_code", vo.getId().toString());
  				map.put("department_name", vo.getName());
  				if(openOtherNameStatus.equals("1")){//表示别名是开启状态
  					map.put("real_name", personInfo.getRealName());
  				}else{
  					map.put("full_name", personInfo.getFullName());
  				}
  				map.put("employee_no", personInfo.getEmployeeNo());
  				userList.add(map);
  			}
  	       
  	    	List<Map<String, Object>> list =partyService.getChildDeparentById(partyEntityId);//一级子节点集合
  	    	for (Map oneMap : list) {
  	    		// 通过id 查询部门名称
  	    		PartyEntity partyEntiy = partyEntityManager.get(Long.parseLong(oneMap.get("id").toString()));
  	    		
  	    		String strChildPartyIds="";
  	    		List<String> childAllList=partyService.getAllDeparentById(Long.parseLong(oneMap.get("id").toString()));
  	    		if(childAllList!=null) {
  	    			strChildPartyIds=Joiner.on(",").join(childAllList);
  	    			
  	    			strChildPartyIds = strChildPartyIds + "," + oneMap.get("id"); //and e.delFlag='0' and p.quitFlag='0' 
  	    	        hql = "select p from PersonInfo as p,PartyEntity as e, PartyStruct as s "
  	    	        		+ "where p.partyId = e.id and  e.id =s.childEntity and s.partyStructType = " + PartyConstants.PARTY_STRUCT_TYPE_ORG;
  	    	        				
  	    	        
  	    			if(!com.mossle.core.util.StringUtils.isBlank(strChildPartyIds))
  	    				hql+="and s.parentEntity in ("+strChildPartyIds+") order by p.employeeNo ";
  	    	       
  	    	        List<PersonInfo> personInfoList =personInfoManager.find(hql) ;  
  	    	        for (PersonInfo personInfo : personInfoList) {
  	    	        	if(strSystemAdminIds.indexOf(personInfo.getId().toString())>=0||personInfo.getUsername().equals("releasetest")||personInfo.getUsername().toLowerCase().equals("testuser")){
  	    					continue;
  	    				}
  	    	        	
  	    	        	//ckx
 	    	        	//离职人员的处理
	 	   				if(personInfo.getQuitFlag().equals("1")){
	 	   					if(personInfo.getQuitTime()==null){
	 	   						strQuitAbnormalId+=personInfo.getId()+",";
	 	   						continue;
	 	   					}
	 	   					Date quitDate = personInfo.getLeaveDate();
	 	   					String formatDate = DateUtil.formatDate(quitDate, "");
	 	   					formatDate = formatDate.substring(0, 10);
	 	    					quitDate = DateUtil.formatDateStr(formatDate+" 18:00:00", "");
	 	    				//Date quitDate = DateUtil.formatDateStr(leaveDate+" 18:00:00", "");
	 	   					//Date quitDate=personInfo.getQuitTime();
	 	   					Calendar calQuit=Calendar.getInstance();
	 	    					calQuit.setTime(quitDate);
	 	    					/*int intQuitYear=calQuit.get(Calendar.YEAR);
	 	    					int intQuitMonth=calQuit.get(Calendar.MONTH)+1;
	 	    					int intQuitDay = calQuit.get(Calendar.DATE);
	 	    					if(intQuitMonth>11){
	 	    						intQuitYear+=1;
	 	    						intQuitMonth=0;
	 	    					}
	 	    					calQuit.set(intQuitYear,intQuitMonth,intQuitDay);*/
	 	   					
	 	   					//如果查询时间大于离职时间
	 	   					if(calSearch.after(calQuit)){
	 	   						continue;
	 	   					}
	 	   				}
  	    	        	
  	    	        	Map<String,Object> map = new HashMap<String,Object>();
  	    	        	map.put("id", personInfo.getId().toString());
  	    	        	map.put("department_code", partyEntiy.getId().toString());
  	    	        	map.put("department_name", partyEntiy.getName());
  	    	        	if(openOtherNameStatus.equals("1")){//表示别名是开启状态
  	    					map.put("real_name", personInfo.getRealName());
  	    				}else{
  	    					map.put("full_name", personInfo.getFullName());
  	    				}
  	    	        	map.put("employee_no", personInfo.getEmployeeNo());
  	    	        	userList.add(map);
  	    	        }
  	    	       // p.id,p.DEPARTMENT_CODE,p.DEPARTMENT_NAME,p.FULL_NAME
  	    	        
  	    		}
  	    	}
  	   }else{//姓名条件不为空时
  			String strChildPartyIds = "";
  			List<String> childAllList = partyService.getAllDeparentById(partyEntityId);
  			if (childAllList != null) {
  				strChildPartyIds = Joiner.on(",").join(childAllList);

  				strChildPartyIds = strChildPartyIds + "," + partyEntityId;
  				//根据别名状态为查询名字的key赋值 hibernate对象查询 实体属性为key
  				String nameKey = "";
  				if(openOtherNameStatus.equals("1")){
  					nameKey = "p.realName";
  				}else{
  					nameKey = "p.fullName";
  				}
  				String hql = "select p from PersonInfo as p,PartyEntity as e, PartyStruct as s "  //and e.delFlag='0' and p.quitFlag='0'
  						+ "where p.partyId = e.id and  e.id =s.childEntity  and "+ nameKey+" like '%"+name+"%' and s.partyStructType = "
  						+ PartyConstants.PARTY_STRUCT_TYPE_ORG;

  				if (!com.mossle.core.util.StringUtils.isBlank(strChildPartyIds))
  					hql += "and s.parentEntity in (" + strChildPartyIds + ") order by p.employeeNo ";

  				List<PersonInfo> personInfoList = personInfoManager.find(hql);
  				for (PersonInfo personInfo : personInfoList) {
  					if(strSystemAdminIds.indexOf(personInfo.getId().toString())>=0||personInfo.getUsername().equals("releasetest")||personInfo.getUsername().toLowerCase().equals("testuser")){
  	 					continue;
  	 				}
  					
  					//离职人员的处理
 					if(personInfo.getQuitFlag().equals("1")){
 						if(personInfo.getQuitTime()==null){
 							continue;
 						}
 						Date quitDate = personInfo.getLeaveDate();
 						String formatDate = DateUtil.formatDate(quitDate, "");
 						formatDate = formatDate.substring(0, 10);
 	 					quitDate = DateUtil.formatDateStr(formatDate+" 18:00:00", "");
 						//Date quitDate=personInfo.getQuitTime();
 						Calendar calQuit=Calendar.getInstance();
 	 					calQuit.setTime(quitDate);
 	 					/*int intQuitYear=calQuit.get(Calendar.YEAR);
 	 					int intQuitMonth=calQuit.get(Calendar.MONTH)+1;
 	 					if(intQuitMonth>11){
 	 						intQuitYear+=1;
 	 						intQuitMonth=0;
 	 					}
 	 					calQuit.set(intQuitYear,intQuitMonth,1);*/
 						
 						//如果查询时间大于离职时间
 						if(calSearch.after(calQuit)){
 							continue;
 						}
 					}
  					
  					
  					Map<String, Object> map = new HashMap<String, Object>();
  					map.put("id", personInfo.getId().toString());
  					map.put("department_code",personInfo.getDepartmentCode());
  					map.put("department_name", personInfo.getDepartmentName());
  					if(openOtherNameStatus.equals("1")){//表示别名是开启状态
  	 					map.put("real_name", personInfo.getRealName());
  	 				}else{
  	 					map.put("full_name", personInfo.getFullName());
  	 				}
  					map.put("employee_no", personInfo.getEmployeeNo());

  					userList.add(map);
  				}

  			}
  	   }
		
		StringBuffer sb = new StringBuffer();
		if(null != userList && userList.size() > 0){
			for (Map<String, Object> map : userList) {
				sb.append(map.get("id")+",");
			}
		}
		String str = sb.toString();
		if(null != str && !"".equals(str) && !"null".equals(str)){
			str = str.substring(0, str.length()-1);
		}else{
			return resultData;
		}
		
		String sql = "select l.id,l.customFormId,l.userId,l.formType,l.type,l.startTime from leave_details l join kv_record k on l.customFormId = k.applyCode where k.audit_status=2  and l.formType is NOT NULL and l.userid IN ("+str+")";
		if(StringUtils.isNotBlank(startTime) && StringUtils.isNotBlank(endTime) ){
			//String s = startTime.substring(0, startTime.length()-3);
			//String e = endTime.substring(0, endTime.length()-3);
			//sql += " and startTime >= '"+s+"' and startTime <= '"+e+"' ";
			sql += " and DATE_FORMAT(startTime,'%Y-%m-%d %H:%i:%s') >= '"+startTime+"' and DATE_FORMAT(startTime,'%Y-%m-%d %H:%i:%s') <= '"+endTime+"' ;";
		}else{
			return resultData;
		}
		//获取数据
		List<Map<String,Object>> queryForList = jdbcTemplate.queryForList(sql);
		
		for (Map<String, Object> userMap : userList) {
			int overTimeInt = 0; //加班
			int sickLeaveInt = 0; //病假
			int absenceLeaveInt = 0; //事假
			int annualLeaveInt = 0;  //年假
			int maritalLeaveInt = 0;//婚假
			int maternityLeaveInt = 0;//产假
			int funeralLeaveInt = 0;//丧假
			int breakOffLeaveInt = 0;//补休
			int vacationsLeaveInt = 0;//倒休假
			int backLeaveInt = 0;//销假
			int otherInt = 0;//销假
			
			HashMap<String, Object> resultDataMap = new HashMap<String, Object>();
			String userId = userMap.get("id").toString();
			
			for (Map<String, Object> map : queryForList) {
				String objFormType = map.get("formType").toString();
				String objType = map.get("type").toString();
				String userIdStr = map.get("userId").toString();
				if(userId.equals(userIdStr)){
					if(objFormType.equals("1")){//请假
		 		 		if(objType.equals("1")){//病假
		 		 			objType = "病假";
		 		 			sickLeaveInt++;
		 		 		}else if(objType.equals("2")){//事假
		 		 			objType = "事假";
		 		 			absenceLeaveInt++;
		 		 		}else if(objType.equals("3")){//倒休假
		 		 			objType = "倒休假";
		 		 			vacationsLeaveInt++;
		 		 		}else if(objType.equals("4")){//年假
		 		 			objType = "年假";
		 		 			annualLeaveInt++;
		 		 		}else if(objType.equals("5")){//补休假
		 		 			objType = "补休假";
		 		 			breakOffLeaveInt++;
		 		 		}else if(objType.equals("6")){//婚假
		 		 			objType = "婚假";
		 		 			maritalLeaveInt++;
		 		 		}else if(objType.equals("7")){//产假
		 		 			objType = "产假";
		 		 			maternityLeaveInt++;
		 		 		}else if(objType.equals("8")){//丧假
		 		 			objType = "丧假";
		 		 			funeralLeaveInt++;
		 		 		}else if(objType.equals("9")){//其他
		 		 			objType = "其他";
		 		 			otherInt++;
		 		 		}
		 		 	}else if(objFormType.equals("2")){//出差
		 		 		if(objType.equals("1")){//出差
		 		 			objType = "出差";
		 		 		}else if(objType.equals("2")){//因公外出
		 		 			objType = "因公外出";
		 		 		}else if(objType.equals("3")){//其他
		 		 			objType = "其他";
		 		 			otherInt++;
		 		 		}
		 		 	}else if(objFormType.equals("3")){//加班
		 		 		objType = "加班";
		 		 		overTimeInt++;
		 		 	}else if(objFormType.equals("4")){//特殊考勤
		 		 		if(objType.equals("1")){//销假
		 		 			objType = "销假";
		 		 			backLeaveInt++;
		 		 		}else if(objType.equals("2")){//迟到
		 		 			objType = "迟到";
		 		 		}else if(objType.equals("3")){//临时外出
		 		 			objType = "临时外出";
		 		 		}else if(objType.equals("4")){//漏打卡
		 		 			objType = "漏打卡";
		 		 		}else if(objType.equals("5")){//其他
		 		 			objType = "其他";
		 		 			otherInt++;
		 		 		}
		 		 	}
					
				}
			}
			//拼接数据
			resultDataMap.put("orgName", userMap.get("department_name"));
			resultDataMap.put("userCode", userMap.get("employee_no"));
			if(openOtherNameStatus.equals("1")){//表示别名是开启状态
				resultDataMap.put("userName", userMap.get("real_name"));
			}else{
				resultDataMap.put("userName", userMap.get("full_name"));
			}
			resultDataMap.put("overTime", overTimeInt);
			resultDataMap.put("sickLeave", sickLeaveInt);
			resultDataMap.put("absenceLeave", absenceLeaveInt);
			resultDataMap.put("annualLeave", annualLeaveInt);
			resultDataMap.put("maritalLeave", maritalLeaveInt);
			resultDataMap.put("maternityLeave", maternityLeaveInt);
			resultDataMap.put("funeralLeave", funeralLeaveInt);
			resultDataMap.put("breakOffLeave", breakOffLeaveInt);
			resultDataMap.put("vacationsLeave", vacationsLeaveInt);
			resultDataMap.put("backLeave", backLeaveInt);
			resultDataMap.put("other", otherInt);
			
			//添加到list
			resultData.add(resultDataMap);
		}
		
		return resultData;
	}
}
