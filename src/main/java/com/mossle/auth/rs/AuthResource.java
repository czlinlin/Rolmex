package com.mossle.auth.rs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.mossle.api.menu.MenuConnector;
import com.mossle.api.menu.MenuDTO;
import com.mossle.api.tenant.TenantHolder;
import com.mossle.api.user.UserConnector;
import com.mossle.api.userauth.UserAuthConnector;
import com.mossle.auth.persistence.domain.Access;
import com.mossle.auth.persistence.domain.AuthOrgData;
import com.mossle.auth.persistence.domain.Menu;
import com.mossle.auth.persistence.domain.Role;
import com.mossle.auth.persistence.domain.RoleDef;
import com.mossle.auth.persistence.domain.UserStatus;
import com.mossle.auth.persistence.manager.AccessManager;
import com.mossle.auth.persistence.manager.AuthOrgDataManager;
import com.mossle.auth.persistence.manager.RoleManager;
import com.mossle.auth.persistence.manager.UserStatusManager;
import com.mossle.auth.service.AuthService;
import com.mossle.core.auth.CurrentUserHolder;
import com.mossle.dict.persistence.domain.DictInfo;
import com.mossle.dict.persistence.manager.DictInfoManager;
import com.mossle.dict.persistence.manager.DictTypeManager;
import com.mossle.party.PartyConstants;
import com.mossle.party.persistence.domain.PartyEntity;
import com.mossle.party.persistence.domain.PartyStruct;
import com.mossle.user.PersonInfoConstants;
import com.mossle.user.persistence.domain.AccountInfo;
import com.mossle.user.persistence.domain.PersonInfo;
import com.mossle.user.persistence.manager.PersonInfoManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Path("auth")
public class AuthResource {
    private static Logger logger = LoggerFactory.getLogger(AuthResource.class);
    public static final String HQL_AUTHORITY = "select p.code from Perm p join p.roleDefs rd join rd.roles r join r.userStatuses u"
            + " where u.id=? and r.localId=?";
    public static final String HQL_ATTRIBUTE = "select r.name from Role r join r.userStatuses u"
            + " where u.id=? and r.localId=?";
    public static final String HQL_ACCESS = "from Access where tenantId=? order by priority";
    private UserStatusManager userStatusManager;
    private AccessManager accessManager;
    private UserConnector userConnector;
    private JdbcTemplate jdbcTemplate;
    private RoleManager roleManager;
    private AuthService authService;
    private TenantHolder tenantHolder;
    private MenuConnector menuConnector;
    private AuthOrgDataManager authOrgDataManager;
    private DictTypeManager dictTypeManager;
    private DictInfoManager dictInfoManager;
    private PersonInfoManager personInfoManager;
    private CurrentUserHolder currentUserHolder;
    
    @GET
    @Path("userid")
    @Produces(MediaType.APPLICATION_JSON)
    public UserDTO getUserById(@QueryParam("userId") String userId) {
        if (userId == null) {
            logger.error("userId cannot be null");

            return null;
        }

        try {
            com.mossle.api.user.UserDTO apiUserDto = userConnector
                    .findById(userId);

            UserDTO userDto = new UserDTO();

            if (apiUserDto == null) {
                logger.error("user is not exists : [{}]", userId);

                userDto.setUsername(userId);
                userDto.setPassword("NO_PASSWORD");
                userDto.setAuthorities(Collections.EMPTY_LIST);
                userDto.setAttributes(Collections.EMPTY_LIST);

                return userDto;
            }

            String hql = "from UserStatus where username=? and userRepoRef=?";
            UserStatus userStatus = userStatusManager.findUnique(hql,
                    apiUserDto.getUsername(), tenantHolder.getUserRepoRef());

            if (userStatus == null) {
                logger.debug("user has no authorities : [{}]", userId);

                logger.debug("find user : [{}]", apiUserDto.getUsername());
                userDto.setUsername(apiUserDto.getUsername());

                if ((userDto.getUsername() == null)
                        || "".equals(userDto.getUsername())) {
                    userDto.setUsername(apiUserDto.getId());
                }

                userDto.setPassword("NO_PASSWORD");
                userDto.setAuthorities(Collections.EMPTY_LIST);
                userDto.setAttributes(Collections.EMPTY_LIST);
                logger.debug("username : [{}]", userDto.getUsername());
                logger.debug("password : [{}]", userDto.getPassword());
            } else {
                userDto.setUsername(userStatus.getUsername());
                userDto.setPassword(userStatus.getPassword());

                List<String> authorties = userStatusManager.find(HQL_AUTHORITY,
                        userStatus.getId(), tenantHolder.getTenantId());
                userDto.setAuthorities(authorties);

                List<String> roles = userStatusManager.find(HQL_ATTRIBUTE,
                        userStatus.getId(), tenantHolder.getTenantId());
                List<String> attributes = new ArrayList<String>();

                for (String role : roles) {
                    attributes.add("ROLE_" + role);
                }

                userDto.setAttributes(attributes);
            }

            return userDto;
        } catch (Exception ex) {
            logger.error("", ex);

            UserDTO userDto = new UserDTO();
            userDto.setUsername(userId);

            return userDto;
        }
    }

    @GET
    @Path("user")
    @Produces(MediaType.APPLICATION_JSON)
    public UserDTO getUser(@QueryParam("username") String username) {
        if (username == null) {
            logger.error("username cannot be null");

            return null;
        }

        logger.debug("username : {}", username);

        try {
            com.mossle.api.user.UserDTO apiUserDto = userConnector
                    .findByUsername(username, tenantHolder.getUserRepoRef());

            UserDTO userDto = new UserDTO();

            if (apiUserDto == null) {
                logger.error("user is not exists : [{}]", username);

                userDto.setUsername(username);
                userDto.setPassword("NO_PASSWORD");
                userDto.setAuthorities(Collections.EMPTY_LIST);
                userDto.setAttributes(Collections.EMPTY_LIST);

                return userDto;
            }

            String hql = "from UserStatus where username=? and userRepoRef=?";
            UserStatus userStatus = userStatusManager.findUnique(hql,
                    apiUserDto.getUsername(), tenantHolder.getUserRepoRef());

            if (userStatus == null) {
                logger.debug("user has no authorities : [{}]", username);
                userDto.setUsername(username);
                userDto.setAuthorities(Collections.EMPTY_LIST);
                userDto.setAttributes(Collections.EMPTY_LIST);
            } else {
                userDto.setUsername(userStatus.getUsername());
                userDto.setPassword(userStatus.getPassword());
                userDto.setAppId("0");

                List<String> authorties = userStatusManager.find(HQL_AUTHORITY,
                        userStatus.getId(), tenantHolder.getTenantId());
                logger.debug("authorties : {}", authorties);
                userDto.setAuthorities(authorties);

                List<String> roles = userStatusManager.find(HQL_ATTRIBUTE,
                        userStatus.getId(), tenantHolder.getTenantId());
                logger.debug("roles : {}", roles);

                List<String> attributes = new ArrayList<String>();

                for (String role : roles) {
                    attributes.add("ROLE_" + role);
                }

                userDto.setAttributes(attributes);
            }

            return userDto;
        } catch (Exception ex) {
            logger.error("", ex);

            UserDTO userDto = new UserDTO();
            userDto.setUsername(username);

            return userDto;
        }
    }

    @GET
    @Path("resource")
    @Produces(MediaType.APPLICATION_JSON)
    public List<AccessDTO> getResource() {
        List<Access> accesses = accessManager.find(HQL_ACCESS,
                tenantHolder.getTenantId());
        List<AccessDTO> accessDtos = new ArrayList<AccessDTO>();

        for (Access access : accesses) {
            AccessDTO dto = new AccessDTO();
            dto.setAccess(access.getValue());
            dto.setPermission(access.getPerm().getCode());
            accessDtos.add(dto);
        }

        return accessDtos;
    }

    // ~ ======================================================================
    @GET
    @Path("findUsers")
    @Produces(MediaType.APPLICATION_JSON)
    public List<UserDTO> findUsers(@QueryParam("appId") Long appId) {
        Long localId = appId;
        Long globalId = jdbcTemplate.queryForObject(
                "select global_id from tenant_local where id=?", Long.class,
                localId);
        logger.debug("globalId : {}", globalId);
        logger.debug("localId : {}", localId);

        List<UserStatus> userStatuses = userStatusManager.find(
                "from UserStatus where localId=?", localId);
        List<UserDTO> userDtos = new ArrayList<UserDTO>();

        for (UserStatus userStatus : userStatuses) {
            UserDTO userDto = new UserDTO();
            userDto.setUserId(userStatus.getId());
            userDto.setUsername(userStatus.getUsername());

            List<String> roles = userStatusManager.find(HQL_ATTRIBUTE,
                    userStatus.getId());
            logger.debug("roles : {}", roles);

            userDto.setAuthorities(roles);
            userDtos.add(userDto);
        }

        return userDtos;
    }

    @GET
    @Path("findRoles")
    @Produces(MediaType.APPLICATION_JSON)
    public List<RoleDTO> findRoles(@QueryParam("appId") Long appId) {
        Long localId = appId;
        Long globalId = jdbcTemplate.queryForObject(
                "select global_id from tenant_local where id=?", Long.class,
                localId);
        logger.debug("globalId : {}", globalId);
        logger.debug("localId : {}", localId);

        List<Role> roles = roleManager.find("from Role where localId=?",
                localId);
        List<RoleDTO> roleDtos = new ArrayList<RoleDTO>();

        for (Role role : roles) {
            RoleDTO roleDto = new RoleDTO();
            roleDto.setId(role.getId());
            roleDto.setName(role.getName());

            roleDtos.add(roleDto);
        }

        return roleDtos;
    }

    @GET
    @Path("getUserByUsername")
    @Produces(MediaType.APPLICATION_JSON)
    public UserDTO getUserByUsername(@QueryParam("username") String username,
            @QueryParam("appId") Long appId) {
        logger.debug("username : {}", username);

        Long localId = appId;
        Long globalId = jdbcTemplate.queryForObject(
                "select global_id from tenant_local where id=?", Long.class,
                localId);
        logger.debug("globalId : {}", globalId);
        logger.debug("localId : {}", localId);

        com.mossle.api.user.UserDTO apiUserDto = userConnector.findByUsername(
                username, Long.toString(globalId));

        if (apiUserDto == null) {
            return null;
        }

        String userId = apiUserDto.getId();

        UserStatus userStatus = userStatusManager.findUnique(
                "from UserStatus where ref=? and localId=?", userId, localId);

        if (userStatus == null) {
            userStatus = new UserStatus();
            userStatus.setRef(userId);
            userStatus.setUsername(username);
            userStatus.setStatus(1);
            userStatus.setUserRepoRef(tenantHolder.getUserRepoRef());
            userStatus.setTenantId(tenantHolder.getTenantId());
            userStatusManager.save(userStatus);
        }

        UserDTO userDto = new UserDTO();

        userDto.setUserId(userStatus.getId());

        userDto.setUsername(apiUserDto.getUsername());

        List<String> roles = userStatusManager.find(HQL_ATTRIBUTE,
                userStatus.getId());
        logger.debug("roles : {}", roles);

        userDto.setAuthorities(roles);

        return userDto;
    }

    @GET
    @Path("configUserRole")
    @Produces(MediaType.APPLICATION_JSON)
    public boolean configUserRole(@QueryParam("userId") Long userId,
            @QueryParam("roleIds") List<Long> roleIds) {
        logger.info("userId : {}", userId);
        logger.info("roleIds : {}", roleIds);

        authService
                .configUserRole(userId, roleIds, tenantHolder.getUserRepoRef(),
                        tenantHolder.getTenantId(), true);

        return true;
    }

    private List<String> convertMapListToStringList(Long roleId) {
    	
        List<String> stringList = new ArrayList<String>();
        
        Role role = roleManager.get(roleId);
        RoleDef roleDef = role.getRoleDef();
        Set<Menu> menus = roleDef.getMenus();
        
        for (Menu menu: menus) {
            stringList.add(menu.getId().toString()); 
        }

        return stringList;
    }
    
    @POST
    @Path("getMenus")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Map> getMenus(@QueryParam("roleId") Long roleId) {
    	
    	List<String> permissions = convertMapListToStringList(roleId);
    	
    	Menu parentMenu = menuConnector.findMenuById(new Long("21"));
    	
    	List<MenuDTO> list = menuConnector.findChildMenuDtos(parentMenu, false);

        return generateMenus(list, permissions);
    }
    
    public List<Map> generateMenus(List<MenuDTO> parentList, List<String> permissions) {
        if (parentList == null) {
            return null;
        }

        List<Map> list = new ArrayList<Map>();

        try {
            for (MenuDTO menu : parentList) {
                list.add(generateMenu(menu, permissions));
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }

        return list;
    }
    
    public Map<String, Object> generateMenu(MenuDTO menu, List<String> permissions) {
    	
        Map<String, Object> map = new HashMap<String, Object>();

        try {
            map.put("id", menu.getId());
            map.put("name", menu.getTitle());
            map.put("code", menu.getCode());
            
            if (permissions.contains(menu.getId().toString())) {
            	map.put("checked", true);
		    }
            
            Menu parentMenu = menuConnector.findMenuById(menu.getId());
            List<MenuDTO> list = menuConnector.findChildMenuDtos(parentMenu, false);
            if (list == null || list.size() == 0) {
                map.put("open", false);
            } else {
                map.put("open", true);
                map.put("children", generateMenus(list, permissions));
            }
        	
            return map;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            return map;
        }
    }
    
    @POST
    @Path("removePartyData")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> removeAuthOrgData(@FormParam("partyId") Long partyId) {
    	Map<String, Object> map=new HashMap<String, Object>();
    	try {
    		List<AuthOrgData> orgDataList=authOrgDataManager.findBy("unionId", partyId);
    		authOrgDataManager.removeAll(orgDataList);
    		map.put("result","ok");
    		map.put("msg", "删除成功");
		} catch (Exception e) {
			map.put("result","error");
    		map.put("msg", "删除失败");
    		e.printStackTrace();
		}
    	return map;
    }
    
    @POST
    @Path("setPersonConfig")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> setPersonConfig(@FormParam("status") String status) {
    	Map<String, Object> map=new HashMap<String, Object>();
    	try {
    		String hql="from DictInfo where dictType.name=?";
        	DictInfo dictInfo=dictInfoManager.findUnique(hql, "personMasterAudit");
    		if(dictInfo!=null){
    			dictInfo.setValue(status);
    			dictInfoManager.save(dictInfo);
    			map.put("result","ok");
        		map.put("msg", "设置成功");
    		}
    		else {
    			map.put("result","error");
        		map.put("msg", "设置失败");
			}
		} catch (Exception e) {
			map.put("result","error");
    		map.put("msg", "设置失败了");
    		e.printStackTrace();
		}
    	return map;
    }
    
    @POST
    @Path("setPersonValidateConfig")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> setPersonValidateConfig(@FormParam("status") String status) {
    	Map<String, Object> map=new HashMap<String, Object>();
    	try {
    		String hql="from DictInfo where dictType.name=?";
        	DictInfo dictInfo=dictInfoManager.findUnique(hql, "personAddAndUPdateValidate");
    		if(dictInfo!=null){
    			dictInfo.setValue(status);
    			dictInfoManager.save(dictInfo);
    			map.put("result","ok");
        		map.put("msg", "设置成功");
    		}
    		else {
    			map.put("result","error");
        		map.put("msg", "设置失败");
			}
		} catch (Exception e) {
			map.put("result","error");
    		map.put("msg", "设置失败了");
    		e.printStackTrace();
		}
    	return map;
    }
    
    /**
     * 修改“别名”功能开启状态
     * **/
    @POST
    @Path("setOtherNameOpenConfig")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> setOtherNameOpenConfig(@FormParam("status") String status) {
    	Map<String, Object> map=new HashMap<String, Object>();
    	try {
    		if(!currentUserHolder.getUserId().equals("2")){
    			map.put("result","error");
        		map.put("msg", "你没有权限操作此功能");
        		return map;
    		}
    		if(authService.setOtherNameOpenConfig(status)){
    			map.put("result","ok");
        		map.put("msg", "设置成功");
    		}
    		else {
    			map.put("result","error");
        		map.put("msg", "设置失败");
			}
		} catch (Exception e) {
			map.put("result","error");
    		map.put("msg", "设置失败了");
    		e.printStackTrace();
		}
    	return map;
    }
    
    @POST
    @Path("refreshPersonWorkNumber")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> refreshPersonWorkNumber(@FormParam("status") String status) {
    	/*List<PersonInfo> personInfoAllList=personInfoManager.find("From PersonInfo");
    	for (PersonInfo personInfo : personInfoAllList) {
			
		}*/
    	return null;
    }
    
    @Resource
    public void setUserStatusManager(UserStatusManager userStatusManager) {
        this.userStatusManager = userStatusManager;
    }

    @Resource
    public void setAccessManager(AccessManager accessManager) {
        this.accessManager = accessManager;
    }

    @Resource
    public void setUserConnector(UserConnector userConnector) {
        this.userConnector = userConnector;
    }

    @Resource
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Resource
    public void setRoleManager(RoleManager roleManager) {
        this.roleManager = roleManager;
    }

    @Resource
    public void setAuthService(AuthService authService) {
        this.authService = authService;
    }

    @Resource
    public void setTenantHolder(TenantHolder tenantHolder) {
        this.tenantHolder = tenantHolder;
    }

    @Resource
	public void setMenuConnector(MenuConnector menuConnector) {
		this.menuConnector = menuConnector;
	}
    
    @Resource
	public void setAuthOrgDataManager(AuthOrgDataManager authOrgDataManager) {
		this.authOrgDataManager = authOrgDataManager;
	}
    
    @Resource
    public void setDictTypeManager(DictTypeManager dictTypeManager) {
        this.dictTypeManager = dictTypeManager;
    }
    
    @Resource
    public void setDictInfoManager(DictInfoManager dictInfoManager) {
        this.dictInfoManager = dictInfoManager;
    }
    
    @Resource
    public void setPersonInfoManager(PersonInfoManager personInfoManager) {
        this.personInfoManager = personInfoManager;
    }
    
    @Resource
    public void setCurrentUserHolder(CurrentUserHolder currentUserHolder) {
        this.currentUserHolder = currentUserHolder;
    }
}
