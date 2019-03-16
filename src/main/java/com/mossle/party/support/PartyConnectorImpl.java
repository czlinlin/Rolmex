package com.mossle.party.support;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.ws.rs.GET;

import org.activiti.engine.impl.cmd.AddCommentCmd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import com.google.common.base.Joiner;
import com.graphbuilder.math.func.AtanFunction;
import com.mossle.api.party.PartyConnector;
import com.mossle.api.party.PartyDTO;
import com.mossle.api.party.PartyEntityOrgDTO;
import com.mossle.core.util.StringUtils;
import com.mossle.party.PartyConstants;
import com.mossle.party.persistence.domain.PartyEntity;
import com.mossle.party.persistence.domain.PartyStruct;
import com.mossle.party.persistence.domain.PartyStructType;
import com.mossle.party.persistence.manager.PartyEntityManager;
import com.mossle.party.persistence.manager.PartyStructManager;
import com.mossle.party.persistence.manager.PartyStructTypeManager;
import com.mossle.user.PersonInfoConstants;
import com.mossle.user.persistence.domain.AccountInfo;
import com.mossle.user.persistence.domain.PersonInfo;
import com.mossle.user.persistence.manager.PersonInfoManager;
import com.mossle.user.web.PersonInfoController;

public class PartyConnectorImpl implements PartyConnector {	
	
	private static Logger logger = LoggerFactory.getLogger(PartyConnectorImpl.class);
	
    private PartyEntityManager partyEntityManager;
    private PartyStructManager partyStructManager;
    private PartyStructTypeManager partyStructTypeManager;
    PartyEntity partyEntity = null;
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private PersonInfoManager personInfoManager;
    @Autowired
    private PartyOrgConnector partyOrgConnector;
    
    public PartyDTO findById(String partyId) {
        Long id = Long.parseLong(partyId);
        PartyEntity partyEntity = partyEntityManager.get(id);
        PartyDTO partyDto = new PartyDTO();
        
        // zyl 2017-08-15
        if (partyEntity.getDelFlag().equals(PersonInfoConstants.DELETE_FLAG_NO)) {
	        partyDto.setId(partyId);
	        partyDto.setName(partyEntity.getName());
        }
        return partyDto;
    }
    
    /**
     * 通过PartyId 查找所属的大区
     * @author zyl
     * @param partyId
     * @return
     */
    public PartyDTO findAreaById(String partyId) {
    	
    	Long id = Long.parseLong(partyId);
    	PartyEntity vo = partyEntityManager.get(id);

		partyEntity = null;
    	getPartyArea(vo, PartyConstants.PARTY_STRUCT_TYPE_ORG, PartyConstants.PARTY_TYPE_AREA);

		PartyDTO partyDto = null;
    	if (partyEntity != null) {
			partyDto = new PartyDTO();
    		partyDto.setId(Long.toString(partyEntity.getId()));
    		partyDto.setName(partyEntity.getName());
    	}
    	
    	return partyDto;
    }
    /**
     * 通过PartyId 查找所属的公司
     * @author zyl
     * @param partyId
     * @return
     */
    public PartyDTO findCompanyById(String partyId) {
    	
    	Long id = Long.parseLong(partyId);
        PartyEntity vo = partyEntityManager.get(id);
        
        partyEntity = null;
    	getPartyCompany(vo, PartyConstants.PARTY_STRUCT_TYPE_ORG, PartyConstants.PARTY_TYPE_COMPANY);

		PartyDTO partyDto = null;
    	if (partyEntity != null) {
    		partyDto = new PartyDTO();
            partyDto.setId(Long.toString(partyEntity.getId()));
            partyDto.setName(partyEntity.getName());
    	}
    	
    	return partyDto;
    }
    
    public PartyEntityOrgDTO findCompanyInfoById(String partyId) {
    	
    	Long id = Long.parseLong(partyId);
        PartyEntity vo = partyEntityManager.get(id);
        
        partyEntity = null;
    	getPartyCompany(vo, PartyConstants.PARTY_STRUCT_TYPE_ORG, PartyConstants.PARTY_TYPE_COMPANY);

    	PartyEntityOrgDTO partyDto = null;
    	if (partyEntity != null) {
    		partyDto = new PartyEntityOrgDTO();
            partyDto.setId(Long.toString(partyEntity.getId()));
            partyDto.setName(partyEntity.getName());
            partyDto.setShortName(partyEntity.getShortName());
    	}
    	
    	return partyDto;
    }
    
    /**
    * 通过PartyId 查找所属的部门
    * @author zyl
    * @param partyId
    * @return
    */
    public PartyDTO findDepartmentById(String partyId) {
    	
    	Long id = Long.parseLong(partyId);
        PartyEntity vo = partyEntityManager.get(id);
        
        partyEntity = null;
        
    	getPartyCompany(vo, PartyConstants.PARTY_STRUCT_TYPE_ORG, PartyConstants.PARTY_TYPE_DEPARTMENT);

		
    	PartyDTO partyDto = null;
    	if (partyEntity != null) {
    		partyDto = new PartyDTO();
            partyDto.setId(Long.toString(partyEntity.getId()));
            partyDto.setName(partyEntity.getName());
    	}
    	
    	return partyDto;
    }
    
    /**
     * 根据PartyEntity得到工号前缀
     * **/
    public String getWorkNumerPrefix(String partyEntityId) {
		PartyEntity partyEntity=partyEntityManager.findUniqueBy("id",Long.valueOf(partyEntityId));
		/****************************
		 * 获取前缀规则
		 * 1.部门下小组：公司+部门
		 * 2.公司下小组：公司+小组
		 * 3.大区：公司+大区
		 * 4.大区下的部门：大区+部门
		 * 5.公司下的公司：公司+公司
		 * **************************/
		
		//System.out.print("公司/部门ID："+partyEntityId);
		try
		{
			String hqlString="from PartyStruct where childEntity = ? and partyStructType.id = ?";
			PartyStruct partyStruct=partyStructManager.findUnique(hqlString,partyEntity,1L);
			//部门
			if(partyEntity.getPartyType().getId().equals(PartyConstants.PARTY_TYPE_DEPARTMENT)){
				//公司、大区(4)
				if(partyStruct.getParentEntity().getPartyType().getId().equals(PartyConstants.PARTY_TYPE_COMPANY)
						||partyStruct.getParentEntity().getPartyType().getId().equals(PartyConstants.PARTY_TYPE_AREA))
					return partyStruct.getParentEntity().getShortName()+"-"+partyEntity.getShortName();
			}//小组（1,2）
			else if(partyEntity.getPartyType().getId().equals(PartyConstants.PARTY_TYPE_GROUP)){
				//2
				if(partyStruct.getParentEntity().getPartyType().getId().equals(PartyConstants.PARTY_TYPE_COMPANY))
						return partyStruct.getParentEntity().getShortName()+"-"+partyEntity.getShortName();
				
				//1
				if(partyStruct.getParentEntity().getPartyType().getId().equals(PartyConstants.PARTY_TYPE_DEPARTMENT))
				{
					PartyEntityOrgDTO partyEntityOrgDTO=findCompanyInfoById(partyStruct.getParentEntity().getId().toString());
					return partyEntityOrgDTO.getShortName()+"-"+partyStruct.getParentEntity().getShortName();
				}
			}//3
			else if(partyEntity.getPartyType().getId().equals(PartyConstants.PARTY_TYPE_AREA)){
				PartyEntityOrgDTO partyEntityOrgDTO=findCompanyInfoById(partyStruct.getParentEntity().getId().toString());
				return partyEntityOrgDTO.getShortName()+"-"+partyStruct.getParentEntity().getShortName();
			}
			else if(partyEntity.getPartyType().getId().equals(PartyConstants.PARTY_TYPE_COMPANY)){
				if(!partyStruct.getChildEntity().getId().equals(PartyConstants.ROOT_PARTY_TREE_ID)
					&&!partyStruct.getParentEntity().getPartyType().getId().equals(PartyConstants.PARTY_TYPE_AREA)){
					return partyStruct.getParentEntity().getShortName()+"-"+partyEntity.getShortName();
				}
			}
			
			if(partyStruct.getChildEntity().getId().equals(PartyConstants.ROOT_PARTY_TREE_ID))
				return partyStruct.getChildEntity().getShortName();
			PartyEntityOrgDTO partyEntityOrgDTO=findCompanyInfoById(partyStruct.getParentEntity().getId().toString());
			return partyEntityOrgDTO.getShortName()+"-"+partyStruct.getParentEntity().getShortName();
		}
		catch(Exception ex){
			return "";
		}
	}
    
    /**
     * 得到机构的所属公司
     * @param vo
     * @param partyStructTypeId   组织关系
     * @param partyEntityType   机构类型
     * @return
     */
    private void getPartyCompany(PartyEntity vo,long partyStructTypeId, long partyEntityType) {
       
        try {
        	if (vo != null) {
        		generatePartyEntity(vo, partyStructTypeId, partyEntityType);
        	}
            
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }

    }
    /**
     * 得到机构的所属大区
     * @param vo
     * @param partyStructTypeId   组织关系
     * @param partyEntityType   机构类型
     * @return
     */
    private void getPartyArea(PartyEntity vo,long partyStructTypeId, long partyEntityType) {
    	
    	try {
    		if (vo != null) {
    			generatePartyEntityArea(vo, partyStructTypeId, partyEntityType);
    		}
    		
    	} catch (Exception ex) {
    		logger.error(ex.getMessage(), ex);
    	}
    	
    }
	
    private void generatePartyEntity(PartyEntity childEntity, Long partyStructTypeId, long partyEntityType) {
        // Map<String, Object> map = new HashMap<String, Object>();

        try {
        	logger.debug("parentPartyEntityId : {}", childEntity.getPartyType().getId());
            if (childEntity.getPartyType().getId().equals(PartyConstants.PARTY_TYPE_COMPANY)) {
            	partyEntity = childEntity;
            }else {
            
	            PartyStructType partyStructType = partyStructTypeManager.get(PartyConstants.PARTY_STRUCT_TYPE_ORG);
	            
	            @SuppressWarnings("unchecked")
				List<PartyStruct> partyStructs = partyStructManager.find(
	                    "from PartyStruct where childEntity = ? and partyStructType = ?",
	                    childEntity, partyStructType);
	            // List<PartyEntity> partyEntities = new ArrayList<PartyEntity>();
	
	            for (PartyStruct partyStruct : partyStructs) {
	                
	                PartyEntity parentPartyEntity = partyStruct.getParentEntity();
	                
	                if (parentPartyEntity == null) {
	                    // logger.info("child party entity is null");
	                    continue;
	                }
	                
	                if (parentPartyEntity.getDelFlag().equals(PersonInfoConstants.DELETE_FLAG_YES)) {
	                    // logger.info("child party entity is delete");
	                    continue;
	                }
	                
	                // logger.debug("parentPartyEntity : {}", parentPartyEntity);
	                if (parentPartyEntity.getPartyType().getId().equals(partyEntityType)) {
	                	partyEntity = parentPartyEntity;
	                } else {
	                	getPartyCompany(parentPartyEntity, partyStructTypeId, partyEntityType);
	                }
	            }
            }
            
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
    }
    private void generatePartyEntityArea(PartyEntity childEntity, Long partyStructTypeId, long partyEntityType) {
    	// Map<String, Object> map = new HashMap<String, Object>();
    	
    	try {
    		logger.debug("parentPartyEntityId : {}", childEntity.getPartyType().getId());
    		if (childEntity.getPartyType().getId().equals(PartyConstants.PARTY_TYPE_AREA)) {
    			partyEntity = childEntity;
    		}else {
    			
    			PartyStructType partyStructType = partyStructTypeManager.get(PartyConstants.PARTY_STRUCT_TYPE_ORG);
    			
    			List<PartyStruct> partyStructs = partyStructManager.find(
    					"from PartyStruct where childEntity = ? and partyStructType = ?",
    					childEntity, partyStructType);
    			List<PartyEntity> partyEntities = new ArrayList<PartyEntity>();
    			
    			for (PartyStruct partyStruct : partyStructs) {
    				
    				PartyEntity parentPartyEntity = partyStruct.getParentEntity();
    				
    				if (parentPartyEntity == null) {
    					// logger.info("child party entity is null");
    					
    					continue;
    				}
    				
    				if (parentPartyEntity.getDelFlag().equals(PersonInfoConstants.DELETE_FLAG_YES)) {
    					// logger.info("child party entity is delete");
    					
    					continue;
    				}
    				
    				// logger.debug("parentPartyEntity : {}", parentPartyEntity);
    				if (parentPartyEntity.getPartyType().getId().equals(partyEntityType)) {
    					partyEntity = parentPartyEntity;
    				} else {
    					getPartyArea(parentPartyEntity, partyStructTypeId, partyEntityType);
    				}
    			}
    		}
    		
    	} catch (Exception ex) {
    		logger.error(ex.getMessage(), ex);
    	}
    }
    
    /**
     * 跟据用户id得到公司或者大区信息
     * 循环先找到大区或则公司停止
     * ·add by lilei at 2018-06-29
     * **/
    public PartyDTO getCompanyOrAreaByUserId(String partyEntityId){
    	PartyDTO partyDto=null;
    	
    	try{
    		PartyEntity partyEntity=partyEntityManager.findUniqueBy("id",Long.valueOf(partyEntityId));
    		if(partyEntity==null)
    			return null;
    		int i=0;
    		//如果20层你还没找到，那你是真的找不到了，放弃吧
    		while(i<20){
    			String hqlString="from PartyStruct where childEntity = ? and partyStructType.id = ?";
    			PartyStruct partyStruct=partyStructManager.findUnique(hqlString,partyEntity,1L);
    			partyEntity=partyStruct.getParentEntity();
    			if(partyEntity==null)
    				break;
    			if(partyEntity.getDelFlag().equals(PersonInfoConstants.DELETE_FLAG_YES))
    				break;
    			//找到第一个大区或则公司停下，此次循环结束
    			if(partyEntity.getPartyType().getId()==PartyConstants.PARTY_TYPE_COMPANY
    					||partyEntity.getPartyType().getId()==PartyConstants.PARTY_TYPE_AREA)
    			{
    				partyDto = new PartyDTO();
    	            partyDto.setId(Long.toString(partyEntity.getId()));
    	            partyDto.setName(partyEntity.getName());
    	            break;
    			}
    			i++;
    		}
    	}
    	catch(Exception ex){
    		logger.error(ex.getMessage(), ex);
    	}
    	return partyDto;
    }
    
    /**
     * 获取岗位编号
     * add by lilei at 2018-09-12
     * **/
    public String getPartyPositionNo(String partyId){
    	String strPositionNo="";
    	try {
    	String strSql=String.format("select positionNo from party_entity_attr where id=%s",partyId);
			List<Map<String,Object>> mapReturnList=jdbcTemplate.queryForList(strSql);
			if(mapReturnList!=null&&mapReturnList.size()>0)
				strPositionNo=mapReturnList.get(0).get("positionNo").toString();
		} catch (Exception e) {
			strPositionNo="";
			e.printStackTrace();
		}
    	if(StringUtils.isBlank(strPositionNo))
    		strPositionNo="";
    	return strPositionNo;
    }
    
    /**
     * 得到系统管理员/虚拟账户   ADD BY LILEI AT 2019.01.08
     * 1.超级管理员虚拟人员-2
	 * 2.系统管理员-对应角色区分
	 * 3.经销商虚拟人员-4
	 * 4.机器人虚拟人员-3
	 * 5.测试用户
     * **/
    public List<String> getSystemAcccountIdList(){
    	List<String> accountIdList=new ArrayList<String>();
    	
    	accountIdList.add(PartyConstants.ADMIN_USER_ID.toString());
    	accountIdList.add(PartyConstants.SYSTEM_ROBOT_ID);
    	accountIdList.add(PartyConstants.JXS_ID.toString());
    	/*String strSerchRemoveId=PartyConstants.ADMIN_USER_ID+","
				+PartyConstants.SYSTEM_ROBOT_ID+","
				+PartyConstants.JXS_ID;*/
		PersonInfo personInfoTest=personInfoManager.findUniqueBy("username", "testuser");
		if(personInfoTest!=null)
			accountIdList.add(personInfoTest.getId().toString());
		
		PersonInfo personInfoTest2=personInfoManager.findUniqueBy("username", "releasetest");
		if(personInfoTest2!=null)
			accountIdList.add(personInfoTest2.getId().toString());
		
		List<String> systemAdminIdList=null;
		//查询属于角色ID为2(系统管理员)的所有用户ID
		String strSql="SELECT US.ref FROM AUTH_USER_ROLE UR "
		+" INNER JOIN AUTH_USER_STATUS US ON UR.USER_STATUS_ID=US.ID"
		+" WHERE ROLE_ID=2";
		systemAdminIdList=jdbcTemplate.queryForList(strSql, String.class);
		if(systemAdminIdList!=null&&systemAdminIdList.size()>0)
			accountIdList.addAll(systemAdminIdList);
		
		return accountIdList;
    }
    
    /**
     * 根据当前userId
     * 查找当前部门（如果为公司则不查找）上级部门直到公司
     * 和获取下级所有部门
     * ADD BY LILEI AT 2019.01.21
     * **/
    public List<String> getUpperCompanyAndLowerDepartment(String userId){
    	List<String> partyIdList=new ArrayList<String>();
    	try{
    		//找出管理者所在部门
    		String strHql="from PartyStruct where childEntity.id=? and parentEntity.delFlag='" + PartyConstants.PARTY_NORMAL
            		+ "' and partyStructType.id=" + PartyConstants.PARTY_STRUCT_TYPE_MANAGE;
            List<PartyStruct> manangerPartyStructList=partyStructManager.find(strHql, Long.parseLong(userId));
            if(manangerPartyStructList.size()>0){
            	for (PartyStruct partyStruct : manangerPartyStructList) {
            		Long parentPartyId=partyStruct.getParentEntity().getId();
        			/*if(partyIdList.contains(parentPartyId.toString()))
        				continue;*/
        			
        			//1.找出部门/公司下的所有部门/小组
        			List<String> departLowerPartyIdList=getLowerAllDeparentById(parentPartyId);
					if(departLowerPartyIdList.size()>0){
						for (String partyId : departLowerPartyIdList) {
							if(partyIdList.contains(partyId))
	            				continue;
							partyIdList.add(partyId);
						}
					}
					
					boolean isArea=false;
			    	PartyEntity partyEntityArea=partyOrgConnector.findPartyAreaByUserId(parentPartyId.toString());
			    	if(partyEntityArea!=null)
			    		isArea=true;
					
					//2.找出非公司下部门的上级，直到公司
					if(!partyStruct.getParentEntity().getPartyType().getId().equals(PartyConstants.PARTY_TYPE_COMPANY)){
						List<String> departUpperPartyIdList=getUpperIdLineList(parentPartyId.toString(),isArea);
						for (String partyId : departUpperPartyIdList) {
							if(partyIdList.contains(partyId))
	            				continue;
							partyIdList.add(partyId);
						}
					}
					else{
						if(isArea){
							List<String> departUpperPartyIdList=getUpperIdLineList(parentPartyId.toString(),isArea);
							for (String partyId : departUpperPartyIdList) {
								if(partyIdList.contains(partyId))
		            				continue;
								partyIdList.add(partyId);
							}
						}
					}
				}
            }
    	}
    	catch(Exception ex){
    		logger.info("根据当前userId查找当前部门（如果为公司则不查找）"
    				+ "上级部门直到公司和获取下级所有部门异常："+ex.getMessage()+"\r\n"+ex.getStackTrace());
    		partyIdList.clear();
    	}
    	
    	return partyIdList;
    }
    
    /**
     * 得到部门下所有部门/小组信息
     * ADD BY LILEI AT 2019.01.21
     * **/
    public List<String> getLowerAllDeparentById(Long partyEntityId) {
    	List<String> childAllList=new ArrayList<String>();
    	childAllList.add(partyEntityId.toString());    	
    	String sqlString="select s.CHILD_ENTITY_ID from party_struct s"
    			+ " inner join party_entity c on c.id=s.CHILD_ENTITY_ID"
    			+ " where c.type_id not in(1,5) and s.STRUCT_TYPE_ID=1 and s.PARENT_ENTITY_ID in(%s)";
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
     * 得到上级部门的这条线的partyId，直到公司
     * ADD BY LILEI AT 2019.01.21
     * **/
    public List<String> getUpperIdLineList(String partyEntityId,boolean isArea) {
    	List<String> partyIdList=new ArrayList<String>();
    	
    	try{
    		PartyEntity partyEntity=partyEntityManager.findUniqueBy("id",Long.valueOf(partyEntityId));
    		if(partyEntity==null)
    			return null;
    		int i=0;
    		//如果20层你还没找到，那你是真的找不到了，放弃吧
    		while(i<20){
    			String hqlString="from PartyStruct where childEntity = ? and partyStructType.id = ?";
    			PartyStruct partyStruct=partyStructManager.findUnique(hqlString,partyEntity,1L);
    			partyEntity=partyStruct.getParentEntity();
    			if(partyEntity==null)
    				break;
    			if(partyEntity.getDelFlag().equals(PersonInfoConstants.DELETE_FLAG_YES))
    				break;
    			
    			partyIdList.add(partyEntity.getId().toString());
    			
    			if(isArea){
    				if(partyEntity.getPartyType().getId()==PartyConstants.PARTY_TYPE_COMPANY){
    					if(partyEntity.getName().contains("罗麦科技"))
    						break;
    				}
    			}
    			else {
    				//找到第一个公司停下，此次循环结束
        			if(partyEntity.getPartyType().getId()==PartyConstants.PARTY_TYPE_COMPANY)
        	            break;
				}
    			i++;
    		}
    	}
    	catch(Exception ex){
    		logger.error(ex.getMessage(), ex);
    		partyIdList.clear();
    	}
    	return partyIdList;
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
    public void setPartyStructTypeManager(PartyStructTypeManager partyStructTypeManager) {
        this.partyStructTypeManager = partyStructTypeManager;
    }
    
    @Resource
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
}
