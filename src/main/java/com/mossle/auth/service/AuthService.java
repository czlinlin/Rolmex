package com.mossle.auth.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import com.mossle.auth.persistence.domain.Access;
import com.mossle.auth.persistence.domain.AuthOrgData;
import com.mossle.auth.persistence.domain.Perm;
import com.mossle.auth.persistence.domain.Role;
import com.mossle.auth.persistence.domain.UserStatus;
import com.mossle.auth.persistence.manager.AccessManager;
import com.mossle.auth.persistence.manager.AuthOrgDataManager;
import com.mossle.auth.persistence.manager.PermManager;
import com.mossle.auth.persistence.manager.RoleManager;
import com.mossle.auth.persistence.manager.UserStatusManager;
import com.mossle.auth.support.Exporter;
import com.mossle.auth.support.Importer;
import com.mossle.core.id.IdGenerator;
import com.mossle.core.spring.MessageHelper;
import com.mossle.core.util.StringUtils;
import com.mossle.dict.persistence.domain.DictInfo;
import com.mossle.dict.persistence.manager.DictInfoManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

@Service
@Transactional
public class AuthService {
    private static final int PRIORITY_STEP = 10;
    private static Logger logger = LoggerFactory.getLogger(AuthService.class);
    private UserStatusManager userStatusManager;
    private RoleManager roleManager;
    private JdbcTemplate jdbcTemplate;
    private AccessManager accessManager;
    private PermManager permManager;
    private AuthOrgDataManager authOrgDataManager;
    private IdGenerator idGenerator;
    private DictInfoManager dictInfoManager;

    public UserStatus createOrGetUserStatus(String username, String ref,
            String userRepoRef, String tenantId) {
        UserStatus userStatus = userStatusManager.findUnique(
                "from UserStatus where username=? and tenantId=?", username,
                tenantId);

        if (userStatus == null) {
            userStatus = new UserStatus();
            userStatus.setUsername(username);
            userStatus.setRef(ref);
            userStatus.setUserRepoRef(userRepoRef);
            userStatus.setTenantId(tenantId);
            // TODO: 考虑status同步的策略，目前是默认都设置成了有效
            userStatus.setStatus(1);
            userStatusManager.save(userStatus);
        }

        return userStatus;
    }

    public void configUserRole(Long userId, List<Long> roleIds,
            String userRepoRef, String tenantId, boolean clearRoles) {
        logger.debug("userId: {}, roleIds: {}", userId, roleIds);

        UserStatus userStatus = userStatusManager.get(userId);

        if (userStatus == null) {
            logger.warn("cannot find UserStatus : {}", userId);

            return;
        }

        if (clearRoles) {
            List<Role> roles = new ArrayList<Role>();

            roles.addAll(userStatus.getRoles());

            for (Role role : roles) {
                userStatus.getRoles().remove(role);
            }
        }

        if (roleIds == null) {
            roleIds = Collections.emptyList();
        }

        for (Long roleId : roleIds) {
            Role role = roleManager.get(roleId);
            boolean skip = false;

            if (role == null) {
                logger.warn("role is null, roleId : {}", roleId);

                continue;
            }

            for (Role r : userStatus.getRoles()) {
                logger.debug("r.getId() : {}, role.getId() : {}", r.getId(),
                        role.getId());

                if (r.getId().equals(role.getId())) {
                    skip = true;

                    break;
                }
            }

            if (skip) {
                continue;
            }

            userStatus.getRoles().add(role);
        }

        userStatusManager.save(userStatus);
    }

    /**
     * 修改“别名”功能开启状态
     * **/
    public boolean setOtherNameOpenConfig(String status){
    	try {
    		String hql="from DictInfo where dictType.name=?";
        	DictInfo dictInfo=dictInfoManager.findUnique(hql, "isOpenOtherName");
    		
			if(dictInfo!=null){
				if(dictInfo.getValue().equals("0")&&status.equals("1")){
					//开启
					String strPartyEntitySql_one="UPDATE party_entity SET REAL_NAME=`NAME`";
					String strPartyEntitySql_two="UPDATE party_entity SET `NAME`=f_getAnotherName(`NAME`) where type_id=1";
					String strPerSonInfoSql_one="UPDATE person_info SET REAL_NAME=FULL_NAME";
					String strPerSonInfoSql_two="UPDATE person_info SET FULL_NAME=f_getAnotherName(FULL_NAME)";
					String strAccountInfosql_one="UPDATE account_info SET REAL_NAME=DISPLAY_NAME";
					String strAccountInfoSql_two="UPDATE account_info SET DISPLAY_NAME=f_getAnotherName(DISPLAY_NAME)";
					
					jdbcTemplate.update(strPartyEntitySql_one);
					jdbcTemplate.update(strPartyEntitySql_two);
					jdbcTemplate.update(strPerSonInfoSql_one);
					jdbcTemplate.update(strPerSonInfoSql_two);
					jdbcTemplate.update(strAccountInfosql_one);
					jdbcTemplate.update(strAccountInfoSql_two);
				}
				else if(dictInfo.getValue().equals("1")&&status.equals("0")){
					//关闭
					String strPartyEntitySql_one="UPDATE party_entity SET `NAME`=REAL_NAME where type_id=1";
					//String strPartyEntitySql_two="UPDATE party_entity SET `NAME`=f_getAnotherName(`NAME`)";
					String strPerSonInfoSql_one="UPDATE person_info SET FULL_NAME=REAL_NAME";
					//String strPerSonInfoSql_two="UPDATE person_info SET FULL_NAME=f_getAnotherName(FULL_NAME)";
					String strAccountInfosql_one="UPDATE account_info SET DISPLAY_NAME=REAL_NAME" ;
					//String strAccountInfoSql_two="UPDATE account_info SET DISPLAY_NAME=f_getAnotherName(DISPLAY_NAME)";
					
					jdbcTemplate.update(strPartyEntitySql_one);
					//jdbcTemplate.update(strPartyEntitySql_two);
					jdbcTemplate.update(strPerSonInfoSql_one);
					//jdbcTemplate.update(strPerSonInfoSql_two);
					jdbcTemplate.update(strAccountInfosql_one);
					//jdbcTemplate.update(strAccountInfoSql_two);
					//当全局别名关闭时将person_machine中允许修改别名的置为可修改
					String modify = "update person_machine set is_modify=0";
					jdbcTemplate.update(modify);
				}
					
    			dictInfo.setValue(status);
    			dictInfoManager.save(dictInfo);
    		}
    		
    		return true;
		} catch (Exception e) {
			return false;
		}
    }
    
    public String doExport() {
        Exporter exporter = new Exporter();
        exporter.setJdbcTemplate(jdbcTemplate);

        return exporter.execute();
    }

    public void doImport(String text) {
        Importer importer = new Importer();
        importer.setJdbcTemplate(jdbcTemplate);
        importer.execute(text);
    }

    public void batchSaveAccess(String text, String type, String tenantId) {
        List<Access> accesses = accessManager.find(
                "from Access where type=? and tenantId=?", type, tenantId);

        for (Access access : accesses) {
            accessManager.remove(access);
        }

        int priority = 0;

        for (String line : text.split("\n")) {
            String[] array = line.split(",");
            String value = array[0];
            String permStr = array[1];
            logger.debug("value : {}, perm : {}", value, permStr);

            value = value.trim();
            permStr = permStr.trim();

            if (value.length() == 0) {
                continue;
            }

            priority += PRIORITY_STEP;

            Access access = new Access();
            access.setValue(value);
            access.setTenantId(tenantId);
            access.setType(type);
            access.setPriority(priority);

            Perm perm = permManager.findUnique(
                    "from Perm where code=? and tenantId=?", permStr, tenantId);
            Assert.notNull(perm, "cannot find perm");
            access.setPerm(perm);
            accessManager.save(access);
        }
    }
    
    /**
     * 数据权限的保存-人力资源
     * **/
    @Transactional(readOnly = false)
    public void batchSaveAuthOrgData(String ids,String iptRootNode,String dataIds)
    {
    	try {
    		//删除
        	for(String id:ids.split(",")){
    			//List<AuthOrgData> orgDataList=authOrgDataManager.findBy("unionId", Long.valueOf(id));
    			//if(orgDataList!=null&&orgDataList.size()>0)
    				//authOrgDataManager.removeAll(orgDataList);
    			jdbcTemplate.update("delete from auth_orgdata where union_id=?",id);
    		}
        	
        	String strInsertSql="insert into auth_orgdata(id,type,union_id,partyEntityID) values(?,?,?,?)";
        	//添加
        	for(String dataId:dataIds.split(",")){
    			for(String id:ids.split(",")){
    				
    				jdbcTemplate.update(strInsertSql,
    						idGenerator.generateId(),
    						"1",
    						Long.valueOf(id),
    						Long.valueOf(dataId));
    				/*AuthOrgData orgData=new AuthOrgData();
					orgData.setId(idGenerator.generateId());
    				orgData.setType("1");//表示用户分配
    				orgData.setUnionId(Long.valueOf(id));
    				orgData.setPartyEntityId(Long.valueOf(dataId));
    				authOrgDataManager.save(orgData);*/
    			}
    		}
        	if(!StringUtils.isBlank(iptRootNode)){
        		for(String id:ids.split(",")){
    				
    				jdbcTemplate.update(strInsertSql,
    						idGenerator.generateId(),
    						"2",
    						Long.valueOf(id),
    						Long.valueOf(iptRootNode));
    			}
        	}
		} catch (Exception e) {
			// TODO: handle exception
		}
    }

    public List<Role> findRoles(String tenantId) {
        return roleManager.find("from Role where tenantId=?", tenantId);
    }

    @Resource
    public void setUserStatusManager(UserStatusManager userStatusManager) {
        this.userStatusManager = userStatusManager;
    }

    @Resource
    public void setRoleManager(RoleManager roleManager) {
        this.roleManager = roleManager;
    }

    @Resource
    public void setAccessManager(AccessManager accessManager) {
        this.accessManager = accessManager;
    }

    @Resource
    public void setPermManager(PermManager permManager) {
        this.permManager = permManager;
    }

    @Resource
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    @Resource
    public void setAuthOrgDataManager(AuthOrgDataManager authOrgDataManager) {
        this.authOrgDataManager = authOrgDataManager;
    }
    
    @Resource
    public void setIdGenerator(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }
    
    @Resource
    public void setDictInfoManager(DictInfoManager dictInfoManager) {
        this.dictInfoManager = dictInfoManager;
    }
}
