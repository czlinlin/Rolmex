package com.mossle.party.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.google.common.base.Joiner;
import com.mossle.party.persistence.domain.PartyEntity;
import com.mossle.party.persistence.domain.PartyStructType;
import com.mossle.party.persistence.domain.PartyType;
import com.mossle.party.persistence.manager.PartyEntityManager;
import com.mossle.party.persistence.manager.PartyStructManager;
import com.mossle.party.persistence.manager.PartyStructTypeManager;
import com.mossle.party.persistence.manager.PartyTypeManager;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class PartyService {
    private PartyEntityManager partyEntityManager;
    private PartyTypeManager partyTypeManager;
    private PartyStructManager partyStructManager;
    private PartyStructTypeManager partyStructTypeManager;
    private JdbcTemplate jdbcTemplate;

    // ~ ======================================================================
    public PartyEntity getEntity(Long id) {
        return partyEntityManager.get(id);
    }

    public PartyEntity getEntity(String name, String partyType) {
        String hql = "from PartyEntity where delFlag = '0' and name=? and partyType.name=?";

        return partyEntityManager.findUnique(hql, name, partyType);
    }

    public List<PartyEntity> findParentEntities(Long id,
                                                String partyStructType, String partyType) {
        String hql = "select p from PartyEntity o join o.parentStructs s join s.parentEntity p"
                + " where o.delFlag = '0' and o.id=? and s.partyStructType.name=? and p.partyType.name=?";

        return partyEntityManager.find(hql, id, partyStructType, partyType);
    }

    public List<PartyEntity> findChildEntities(Long id, String partyStructType,
                                               String partyType) {
        String hql = "select c from PartyEntity o join o.childStructs s join s.childEntity c"
                + " where o.delFlag = '0' and o.id=? and s.partyStructType.name=? and c.partyType.name=?";

        return partyEntityManager.find(hql, id, partyStructType, partyType);
    }

    public List<PartyEntity> findEntities(String partyType) {
        String hql = "from PartyEntity where delFlag = '0' and partyType.name=?";

        return partyEntityManager.find(hql, partyType);
    }

    public void removeEntity(long id) {
        partyEntityManager.removeById(id);
    }

    // ~ ======================================================================
    public PartyType getType(Long id) {
        return partyEntityManager.get(PartyType.class, id);
    }

    // ~ ======================================================================
    public void save(Object o) {
        partyEntityManager.save(o);
    }

    public void remove(Object o) {
        partyEntityManager.remove(o);
    }

    public boolean findRoleByRef(Long userId) {
        String sqlFindRoleId = "SELECT ROLE_ID AS ROLEID"
                + " FROM AUTH_USER_STATUS US,AUTH_USER_ROLE UR,AUTH_ROLE R"
                + " WHERE US.ID=UR.USER_STATUS_ID AND UR.ROLE_ID=R.ID"
                + " AND US.DEL_FLAG = '0' AND US.REF=? AND US.TENANT_ID=?";
        List<String> list = jdbcTemplate.queryForList(sqlFindRoleId, String.class, userId, 1);
        for (String temp : list) {
            if (temp.equals("2") || temp.equals("1")) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * 排除超级管理员角色
     * **/
    public boolean findRoleByRefNoSuperAdmin(Long userId) {
        String sqlFindRoleId = "SELECT ROLE_ID AS ROLEID"
                + " FROM AUTH_USER_STATUS US,AUTH_USER_ROLE UR,AUTH_ROLE R"
                + " WHERE US.ID=UR.USER_STATUS_ID AND UR.ROLE_ID=R.ID"
                + " AND US.DEL_FLAG = '0' AND US.REF=? AND US.TENANT_ID=?";
        List<String> list = jdbcTemplate.queryForList(sqlFindRoleId, String.class, userId, 1);
        for (String temp : list) {
            if (temp.equals("1")) {
                return false;
            }
        }
        return true;
    }
    // ~ ======================================================================

    /**
     * 同步更新PartyEntity，比如company,department,group,position,user里改了什么信息，就同步修改PartyEntity里的信息.
     */
    public void insertPartyEntity(String partyEntityRef, String partyTypeRef,
                                  String name) {
        PartyEntity partyEntity = new PartyEntity();
        partyEntity.setRef(partyEntityRef);
        partyEntity.setName(name);

        PartyType partyType = partyTypeManager
                .findUniqueBy("ref", partyTypeRef);
        partyEntity.setPartyType(partyType);
        partyEntityManager.save(partyEntity);
    }

    public void updatePartyEntity(String partyEntityRef, String partyTypeRef,
                                  String name) {
        PartyType partyType = partyTypeManager
                .findUniqueBy("ref", partyTypeRef);
        PartyEntity partyEntity = partyEntityManager.findUnique(
                "from PartyEntity where ref=? and partyType.id=?",
                partyEntityRef, partyType.getId());
        partyEntity.setName(name);
        partyEntityManager.save(partyEntity);
    }

    public void removePartyEntity(String partyEntityRef, String partyTypeRef) {
        PartyType partyType = partyTypeManager
                .findUniqueBy("ref", partyTypeRef);
        PartyEntity partyEntity = partyEntityManager.findUnique(
                "from PartyEntity where ref=? and partyType.id=?",
                partyEntityRef, partyType.getId());
        partyEntityManager.remove(partyEntity);
    }

    // ~ ======================================================================
    public Long getDefaultPartyStructTypeId() {
        PartyStructType partyStructType = partyStructTypeManager
                .findUnique("from PartyStructType");

        return partyStructType.getId();
    }

    public String getDefaultRootPartyEntityRef() {
        Long defaultPartyStructTypeId = getDefaultPartyStructTypeId();
        String hql = "select distinct o from PartyEntity o left join o.parentStructs p with p.partyStructType.id=? "
                + "join o.childStructs c where p is null and o.delFlag ='0' and c.partyStructType.id=?";
        PartyEntity partyEntity = partyEntityManager.findUnique(hql,
                defaultPartyStructTypeId, defaultPartyStructTypeId);

        return partyEntity.getRef();
    }

    public List<PartyEntity> getTopPartyEntities(Long partyStructTypeId) {
        String hql = "select ps.childEntity from PartyStruct ps where ps.parentEntity is null and ps.partyStructType.id=?";

        return partyEntityManager.find(hql, partyStructTypeId);
    }

    /**
     * 根据Id查询下级节点
     * @param partyEntityId
     * @return
     */
    public List<Map<String, Object>> getChildDeparentById(Long partyEntityId) {
		
    	String sqlString = "select e.TYPE_ID, e.id as id,e.name"+ 
    	" from party_struct s"+ 
    	" inner join party_entity e on s.CHILD_ENTITY_ID = e.ID"+ 
    	" where s.STRUCT_TYPE_ID = 1 and e.type_id in(2,3,4,6) and e.DEL_FLAG =0"+ 
    	" and s.PARENT_ENTITY_ID ="+partyEntityId;
    	
    	List<Map<String, Object>> partyIdList = jdbcTemplate.queryForList(sqlString);
    	
    	return partyIdList;
	}
    
    /**
     * 得到组织关系下所有子节点ID
     * @param partyEntityId
     * @return
     */
    public List<String> getAllDeparentById(Long partyEntityId) {
		
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
     * 得到公司或则部门下面所有人员
     * **/
    public String getChildList(){
    	return "";
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

}
