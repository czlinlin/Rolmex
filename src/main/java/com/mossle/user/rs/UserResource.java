package com.mossle.user.rs;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.rmi.RemoteException;
import java.util.*;

import javax.annotation.Resource;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.xml.rpc.Call;
import javax.xml.rpc.ServiceException;
import javax.xml.rpc.holders.StringHolder;

import com.google.common.util.concurrent.Service;
import com.mossle.api.user.PersonInfoDTO;
import com.mossle.api.userauth.UserAuthConnector;
import com.mossle.auth.RoleConstants;
import com.mossle.auth.persistence.domain.UserStatus;
import com.mossle.auth.persistence.manager.UserStatusManager;
import com.mossle.common.utils.PasswordUtil;
import com.mossle.core.annotation.Log;
import com.mossle.core.auth.CurrentUserHolder;
import com.mossle.core.auth.CustomPasswordEncoder;
import com.mossle.core.id.IdGenerator;
import com.mossle.core.mapper.JsonMapper;
import com.mossle.core.page.Page;
import com.mossle.core.query.PropertyFilter;
import com.mossle.core.query.PropertyFilterUtils;
import com.mossle.core.spring.SignInfo;
import com.mossle.core.util.BaseDTO;
import com.mossle.core.util.StringUtils;
import com.mossle.operation.persistence.domain.CustomPresetApprover;
import com.mossle.operation.persistence.manager.CustomPresetApproverManager;
import com.mossle.party.PartyConstants;
import com.mossle.party.persistence.domain.PartyEntity;
import com.mossle.party.persistence.domain.PartyStruct;
import com.mossle.party.persistence.manager.PartyEntityManager;
import com.mossle.party.persistence.manager.PartyStructManager;
import com.mossle.party.rs.PartyResource;
import com.mossle.party.service.PartyService;
import com.mossle.user.PersonInfoConstants;
import com.mossle.user.persistence.domain.AccountCredential;
import com.mossle.user.persistence.domain.AccountInfo;
import com.mossle.user.persistence.domain.PersonAttendanceMachine;
import com.mossle.user.persistence.domain.PersonInfo;
import com.mossle.user.persistence.manager.AccountCredentialManager;
import com.mossle.user.persistence.manager.AccountInfoManager;
import com.mossle.user.persistence.manager.PersonAttendanceMachineManager;
import com.mossle.user.persistence.manager.PersonInfoManager;
import com.mossle.user.service.ChangePasswordService;
import com.mossle.user.service.UserService;
import com.mossle.user.support.ChangePasswordResult;
import com.mossle.worktask.persistence.domain.WorkTaskInfo;
import com.mossle.ws.oaclient.holders.GetUserInfoResponseGetUserInfoResultHolder;
import com.sun.mail.imap.protocol.ID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Component
@Path("user")
public class UserResource {
    private static Logger logger = LoggerFactory.getLogger(UserResource.class);
    private AccountInfoManager accountInfoManager;
    private Long defaultUserRepoId = 1L;
    private JsonMapper jsonMapper = new JsonMapper();
    private CurrentUserHolder currentUserHolder;
    private PersonInfoManager personInfoManager;
    private PartyEntityManager partyEntityManager;
    private AccountCredentialManager accountCredentialManager;
    private CustomPasswordEncoder customPasswordEncoder;
    private PartyStructManager partyStructManager;
    private JdbcTemplate jdbcTemplate;
    private PartyResource partyResource;
    private PartyService partyService;
    private SignInfo signInfo;
    private ChangePasswordService changePasswordService;
    private UserStatusManager userStatusManager;
    private UserAuthConnector userAuthConnector;
    private UserService userService;
    private CustomPresetApproverManager customPresetApproverManager;
    private IdGenerator idGenerator;
    private PersonAttendanceMachineManager personAttendanceMachineManager;
    
    @GET
    @Path("exists")
    @Produces(MediaType.APPLICATION_JSON)
    public boolean exists(@QueryParam("username") String username) {
        AccountInfo accountInfo = accountInfoManager.findUniqueBy("username",
                username);

        if (accountInfo.getDelFlag().equals(PersonInfoConstants.DELETE_FLAG_YES)) {
            return false;
        }
        return accountInfo != null;
    }

    @GET
    @Path("get")
    @Produces(MediaType.APPLICATION_JSON)
    public BaseDTO getUserByUsername(@QueryParam("username") String username) {
        if (StringUtils.isBlank(username)) {
            logger.error("username cannot be blank");

            return null;
        }

        try {
            AccountInfo accountInfo = accountInfoManager.findUniqueBy(
                    "username", username);

            BaseDTO result = new BaseDTO();

            if (accountInfo == null) {
                logger.error("user is not exists : [{}]", username);
                result.setCode(404);
                result.setMessage("user is not exists : [" + username + "]");

                return result;
            }

            // zyl 2017-07-14
            if (accountInfo.getDelFlag().equals(PersonInfoConstants.DELETE_FLAG_YES)) {
                logger.error("user is not exists : [{}]", username);
                result.setCode(404);
                result.setMessage("user is not exists : [" + username + "]");

                return result;
            }

            Map<String, Object> map = new HashMap<String, Object>();
            map.put("id", accountInfo.getId());
            map.put("username", accountInfo.getUsername());
            map.put("nickName", accountInfo.getNickName());
            map.put("displayName", accountInfo.getDisplayName());
            result.setCode(200);
            result.setData(map);

            return result;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            BaseDTO result = new BaseDTO();
            result.setCode(500);
            result.setMessage(ex.getMessage());

            return result;
        }
    }

    /*在一定范围内搜索用户*/
    @GET
    @Path("searchVNoMe")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Map<String, Object>> searchVNoMe(
            @QueryParam("username") String username, @QueryParam("partyStructId") Long partyStructId) {
        Long personId = Long.parseLong(currentUserHolder.getUserId());
        List<PersonInfo> personInfos = new ArrayList<PersonInfo>();
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        if (partyStructId == 1) {
            Page page = personInfoManager.pagedQuery(
                    "from PersonInfo where delFlag = '0' and fullName like ? order by positionCode desc", 1, 100, "%" + username
                            + "%");
            personInfos = (List<PersonInfo>) page.getResult();
        } else {
            PartyEntity vo1 = partyEntityManager.get(partyStructId);
            List<PartyEntity> list1 = new ArrayList<PartyEntity>();
            list1.add(vo1);
            //获取到所有子节点map
            List<Map> partyEntities = partyResource.generatePartyEntities(list1, PartyConstants.PARTY_STRUCT_TYPE_ORG, false, false, true);
            //遍历map，拼成逗号隔开的字符串
            String ids = generatePartyIds(partyEntities);
            ids = ids.substring(0, ids.length() - 1);
            personInfos = QueryPersonInfos(ids, username);
        }

        for (PersonInfo personInfo : personInfos) {
            if (!personInfo.getId().equals(personId)) {
                if (partyService.findRoleByRef(personInfo.getId())) {
                    Map<String, Object> map = new HashMap<String, Object>();
                    PartyEntity partyEntity = partyEntityManager.get(personInfo.getId());
                    String postName = "";
                    if (partyEntity != null) {
	                    Set<PartyStruct> partyStructs = partyEntity.getParentStructs();
	                    for (PartyStruct vo : partyStructs) {
	                        if (vo.getParentEntity().getPartyType().getId().equals(PartyConstants.PARTY_TYPE_POST)) {
	                            postName += vo.getParentEntity().getName() + ",";
	                        }
	                    }
                    }
                    if (com.mossle.common.utils.StringUtils.isNotBlank(postName)) {
                        postName = postName.substring(0, postName.length() - 1);
                    }
                    map.put("id", personInfo.getId());
                    map.put("username", personInfo.getUsername());
                    map.put("displayName", personInfo.getFullName());
                    map.put("postName", postName);

                    list.add(map);
                }
            }
        }

        return list;
    }

    /*在一定范围内搜索用户*/
    @GET
    @Path("searchV")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Map<String, Object>> searchV(
            @QueryParam("username") String username, @QueryParam("partyStructId") Long partyStructId) {
        List<PersonInfo> personInfos = new ArrayList<PersonInfo>();
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        if (partyStructId == 1) {
            Page page = personInfoManager.pagedQuery(
                    "from PersonInfo where delFlag = '0' and fullName like ? order by positionCode desc", 1, 100, "%" + username
                            + "%");
            personInfos = (List<PersonInfo>) page.getResult();
        } else {
            PartyEntity vo1 = partyEntityManager.get(partyStructId);
            List<PartyEntity> list1 = new ArrayList<PartyEntity>();
            list1.add(vo1);
            //获取到所有子节点map
            List<Map> partyEntities = partyResource.generatePartyEntities(list1, PartyConstants.PARTY_STRUCT_TYPE_ORG, false, false, true);
            //遍历map，拼成逗号隔开的字符串
            String ids = generatePartyIds(partyEntities);
            ids = ids.substring(0, ids.length() - 1);
            personInfos = QueryPersonInfos(ids, username);
        }

        for (PersonInfo personInfo : personInfos) {
            if (partyService.findRoleByRef(personInfo.getId())) {
                Map<String, Object> map = new HashMap<String, Object>();
                PartyEntity partyEntity = partyEntityManager.get(personInfo.getId());
                String postName = "";
                Set<PartyStruct> partyStructs = partyEntity.getParentStructs();
                for (PartyStruct vo : partyStructs) {
                    if (vo.getParentEntity().getPartyType().getId().equals(PartyConstants.PARTY_TYPE_POST)) {
                        postName += vo.getParentEntity().getName() + ",";
                    }
                }
                if (com.mossle.common.utils.StringUtils.isNotBlank(postName)) {
                    postName = postName.substring(0, postName.length() - 1);
                }
                map.put("id", personInfo.getId());
                map.put("username", personInfo.getUsername());
                map.put("displayName", personInfo.getFullName());
                map.put("postName", postName);

                list.add(map);
            }
        }

        return list;
    }
    
    /**
     * 在一定范围内搜索用户
     * **/
    @GET
    @Path("searchVWithAdmin")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Map<String, Object>> searchVWithAdmin(
            @QueryParam("username") String username, @QueryParam("partyStructId") Long partyStructId) {
        List<PersonInfo> personInfos = new ArrayList<PersonInfo>();
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        if (partyStructId == 1) {
            Page page = personInfoManager.pagedQuery(
                    "from PersonInfo where delFlag = '0' and fullName like ? order by positionCode desc", 1, 100, "%" + username
                            + "%");
            personInfos = (List<PersonInfo>) page.getResult();
        } else {
            PartyEntity vo1 = partyEntityManager.get(partyStructId);
            List<PartyEntity> list1 = new ArrayList<PartyEntity>();
            list1.add(vo1);
            //获取到所有子节点map
            List<Map> partyEntities = partyResource.generatePartyEntities(list1, PartyConstants.PARTY_STRUCT_TYPE_ORG, false, false, true);
            //遍历map，拼成逗号隔开的字符串
            String ids = generatePartyIds(partyEntities);
            ids = ids.substring(0, ids.length() - 1);
            personInfos = QueryPersonInfos(ids, username);
        }

        for (PersonInfo personInfo : personInfos) {
            if (partyService.findRoleByRefNoSuperAdmin(personInfo.getId())) {
                Map<String, Object> map = new HashMap<String, Object>();
                PartyEntity partyEntity = partyEntityManager.get(personInfo.getId());
                String postName = "";
                Set<PartyStruct> partyStructs = partyEntity.getParentStructs();
                for (PartyStruct vo : partyStructs) {
                    if (vo.getParentEntity().getPartyType().getId().equals(PartyConstants.PARTY_TYPE_POST)) {
                        postName += vo.getParentEntity().getName() + ",";
                    }
                }
                if (com.mossle.common.utils.StringUtils.isNotBlank(postName)) {
                    postName = postName.substring(0, postName.length() - 1);
                }
                map.put("id", personInfo.getId());
                map.put("username", personInfo.getUsername());
                map.put("displayName", personInfo.getFullName());
                map.put("postName", postName);

                list.add(map);
            }
        }

        return list;
    }

    private String generatePartyIds(List<Map> partyEntities) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Map party : partyEntities) {
            stringBuilder.append(party.get("id") + ",");
            List partys = (List) party.get("children");
            if (!CollectionUtils.isEmpty(partys)) {
                stringBuilder.append(generatePartyIds(partys));
            }
        }
        return stringBuilder.toString();
    }

    private List<PersonInfo> QueryPersonInfos(String strIds, String username) {

        String selectSql = "select p.ID,p.FULL_NAME,p.USERNAME FROM party_struct s " +
                "inner join account_info i on s.CHILD_ENTITY_ID = i.ID " +
                "inner join person_info p on i.ID=p.ID " +
                "where s.PARENT_ENTITY_ID in (" + strIds + ") and p.FULL_NAME LIKE '%" + username + "%' "
                + " AND p.QUIT_FLAG ='0' and p.DEL_FLAG ='0' and s.STRUCT_TYPE_ID = " + PartyConstants.PARTY_STRUCT_TYPE_ORG + "   ORDER BY p.POSITION_CODE DESC";

        List<Map<String, Object>> list = jdbcTemplate.queryForList(selectSql);
        List<PersonInfo> personInfos = new ArrayList<PersonInfo>();

        for (Map<String, Object> map : list) {
            personInfos.add(convertPPersonDTO(map));
        }

        return personInfos;
    }

    private List<AccountInfo> QueryAccountInfos(String strIds, String username) {

        String selectSql = "select i.ID,i.DISPLAY_NAME,i.USERNAME FROM party_struct s " +
                "inner join account_info i on s.CHILD_ENTITY_ID = i.ID " +
                "inner join person_info p on i.ID=p.ID " +
                "where s.PARENT_ENTITY_ID in (" + strIds + ") and i.DISPLAY_NAME LIKE '%" + username + "%' "
                + " AND p.QUIT_FLAG ='0' and p.DEL_FLAG ='0' and s.STRUCT_TYPE_ID = " + PartyConstants.PARTY_STRUCT_TYPE_ORG;

        List<Map<String, Object>> list = jdbcTemplate.queryForList(selectSql);
        List<AccountInfo> accountInfos = new ArrayList<AccountInfo>();

        for (Map<String, Object> map : list) {
            accountInfos.add(convertPersonDTO(map));
        }

        return accountInfos;
    }

    protected AccountInfo convertPersonDTO(Map<String, Object> map) {
        if ((map == null) || map.isEmpty()) {
            logger.info("AccountInfo[{}] is null.", map);

            return null;
        }
        AccountInfo accountInfo = new AccountInfo();
        accountInfo.setId(Long.parseLong(convertString(map.get("ID"))));
        accountInfo.setUsername(convertString(map.get("USERNAME")));
        accountInfo.setDisplayName(convertString(map.get("DISPLAY_NAME")));

        return accountInfo;
    }

    protected PersonInfo convertPPersonDTO(Map<String, Object> map) {
        if ((map == null) || map.isEmpty()) {
            logger.info("personInfo[{}] is null.", map);

            return null;
        }
        PersonInfo personInfo = new PersonInfo();
        personInfo.setId(Long.parseLong(convertString(map.get("ID"))));

        personInfo.setUsername(convertString(map.get("USERNAME")));
        personInfo.setFullName(convertString(map.get("FULL_NAME")));

        return personInfo;
    }

    private String convertString(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof String) {
            return (String) value;
        }

        return value.toString();
    }

    @GET
    @Path("search")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Map<String, Object>> search(
            @QueryParam("username") String username) {

        Page page = accountInfoManager.pagedQuery(
                "from AccountInfo where delFlag = '0' and displayName like ? ", 1, 100, "%" + username
                        + "%");
        List<AccountInfo> accountInfos = (List<AccountInfo>) page.getResult();
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

        for (AccountInfo accountInfo : accountInfos) {
            Map<String, Object> map = new HashMap<String, Object>();
            PartyEntity partyEntity = partyEntityManager.get(accountInfo.getId());


            String postName = "";
            Set<PartyStruct> partyStructs = partyEntity.getParentStructs();
            for (PartyStruct vo : partyStructs) {
                if (vo.getParentEntity().getPartyType().getId().equals(PartyConstants.PARTY_TYPE_POST)) {
                    postName += vo.getParentEntity().getName() + ",";
                }
            }
            if (com.mossle.common.utils.StringUtils.isNotBlank(postName)) {
                postName = postName.substring(0, postName.length() - 1);
            }
            map.put("id", accountInfo.getId());
            map.put("postName", postName);
            map.put("username", accountInfo.getUsername());
            map.put("displayName", accountInfo.getDisplayName());
            list.add(map);
        }

        return list;
    }

    @GET
    @Path("searchNoMe")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Map<String, Object>> searchNoMe(
            @QueryParam("username") String username) {
        Page page = accountInfoManager.pagedQuery(
                "from AccountInfo where delFlag = '0' and displayName like ? ", 1, 100, "%" + username
                        + "%");
        Long accountId = Long.parseLong(currentUserHolder.getUserId());
        List<AccountInfo> accountInfos = (List<AccountInfo>) page.getResult();
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

        for (AccountInfo accountInfo : accountInfos) {
            if (!accountInfo.getId().equals(accountId)) {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("id", accountInfo.getId());
                map.put("username", accountInfo.getUsername());
                PartyEntity partyEntity = partyEntityManager.get(accountInfo.getId());

                String postName = "";
                Set<PartyStruct> partyStructs = partyEntity.getParentStructs();
                for (PartyStruct vo : partyStructs) {
                    if (vo.getParentEntity().getPartyType().getId().equals(PartyConstants.PARTY_TYPE_POST)) {
                        postName += vo.getParentEntity().getName() + ",";
                    }
                }
                if (com.mossle.common.utils.StringUtils.isNotBlank(postName)) {
                    postName = postName.substring(0, postName.length() - 1);
                }

                map.put("postName", postName);
                map.put("displayName", accountInfo.getDisplayName());
                list.add(map);
            }
        }

        return list;
    }


    /**
     * 常规退货审批：申请单客服工号获取方法
     */
    @GET
    @Path(value = "getEmpNo")
    public PersonInfo findEmpNo(@QueryParam("userId") String userId) {
        PersonInfo personInfo = personInfoManager.get(Long.parseLong(userId));
        return personInfo;
    }

    /**
     * 重置密码.
     */
    @RequestMapping("person-info-resetsave")
    public String changePasswordSave(
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            RedirectAttributes redirectAttributes) {
        Long accountId = Long.parseLong(currentUserHolder.getUserId());

        return "redirect:/user/person-info-list.do";

    }

    @POST
    @Path("person-info-reset")
    @Produces(MediaType.APPLICATION_JSON)
    @Log(desc = "重置", action = "reset", operationDesc = "职员管理-重置密码")
    public BaseDTO reset(
            @FormParam("id") Long id,
            @FormParam("newPassword") String newPassword) {
    	
    	
        BaseDTO result = new BaseDTO();
        try {
        	Long accountId = Long.parseLong(currentUserHolder.getUserId());
        	
            if (id == null || id < 1) {
                result.setCode(500);
                logger.debug("重置操作-获取参数id错误");
                result.setMessage("获取参数错误");
                return result;
            }
            
            // 通过Id判断角色
            /*String sqlFindRoles = "SELECT R.id AS ROLE"
                    + " FROM AUTH_USER_STATUS US,AUTH_USER_ROLE UR,AUTH_ROLE R"
                    + " WHERE US.ID=UR.USER_STATUS_ID AND UR.ROLE_ID=R.ID"
                    + " AND US.DEL_FLAG = '0' AND US.REF=? AND US.TENANT_ID=?";
            List<Map<String, Object>> roles = jdbcTemplate.queryForList(sqlFindRoles, accountId, "1");
            boolean isModify = false;
            if (!accountId.equals(id) ) {
	            if (roles != null && roles.size() > 0) {
	            	
	            	for (Map<String, Object> map : roles) {
	                    Long value = (Long)map.get("ROLE");
	
	                    if (value.equals(RoleConstants.SUPER_ADMIN_ID)) {
	                    	isModify=true;
	                    	break;
	                    }
	                    
	                    if (value.equals(RoleConstants.SYSTEM_ADMIN_ID)) {
	                    	isModify=true;
	                    	break;
	                    }
	                }
	            	
	            	if (!isModify) {
	            		result.setCode(500);
		                logger.debug("重置操作-获取参数id错误");
		                result.setMessage("获取参数错误");
		                return result;
	            	}
	            } else {
	            	result.setCode(500);
	                logger.debug("重置操作-获取参数id错误");
	                result.setMessage("获取参数错误");
	                return result;
	            }
            }*/
           
            
            String hql = "from AccountCredential where ACCOUNT_ID=? and catalog='default'";
            AccountCredential accountCredential = accountCredentialManager.findUnique(hql, id);
            AccountInfo accountInfo = accountCredential.getAccountInfo();
            accountInfo.setLocked("unlocked");
            accountInfoManager.save(accountInfo);
            accountCredential.setPassword(encodePassword(newPassword));
            Calendar calendar = Calendar.getInstance();
            Date now = calendar.getTime();
            // calendar.add(Calendar.MONTH, 3);
            accountCredential.setModifyTime(now);
            accountCredential.setFailedPasswordCount(0);
            accountCredentialManager.save(accountCredential);
            result.setCode(200);
            result.setMessage("重置成功！");

        } catch (ArithmeticException e) {
            result.setCode(500);
            result.setMessage("重置出错");
            logger.error("重置操作-：" + e.getMessage() + "\r\n" + e.fillInStackTrace());
        }
        return result;
    }
    
    @POST
    @Path("person-info-checkEmployeeNo")
    @Produces(MediaType.APPLICATION_JSON)
    @Log(desc = "重置", action = "search", operationDesc = "职员管理-新建职员-查询工号是否被使用")
    public BaseDTO checkEmployeeNo(
            @FormParam("employeeNo") String employeeNo
    ) {
        String strSearchNo=userService.getWorkNumber();
        BaseDTO baseDTO=new BaseDTO();
        if(StringUtils.isBlank(employeeNo))
        {
        	baseDTO.setCode(404);
        	baseDTO.setMessage("工号不能为空");
        }
        else {
			if(strSearchNo.equals(employeeNo)){
	        	baseDTO.setCode(200);
	        	baseDTO.setMessage("验证通过");
			}
			else {
				baseDTO.setCode(205);
	        	baseDTO.setMessage("工号已经被使用，点击确定，系统将重新分配工号");
	        	baseDTO.setData(strSearchNo);
			}
		}
        return baseDTO;
    }


    @POST
    @Path("person-info-resetkey")
    @Produces(MediaType.APPLICATION_JSON)
    @Log(desc = "重置", action = "reset", operationDesc = "职员管理-重置私钥")
    public BaseDTO resetkey(
            @FormParam("id") Long id,
            @FormParam("newPassword") String newPassword
    ) {
        BaseDTO result = new BaseDTO();
        try {
            if (id == null || id < 1) {
                result.setCode(500);
                logger.debug("重置操作-获取参数id错误");
                result.setMessage("获取参数错误");
                return result;
            }
            String hql = "from AccountCredential where ACCOUNT_ID=? and catalog='default'";
            AccountCredential accountCredential = accountCredentialManager.findUnique(hql, id);
            accountCredential.setOperationPassword(encodePassword(newPassword));
            Calendar calendar = Calendar.getInstance();
            Date now = calendar.getTime();
            // calendar.add(Calendar.MONTH, 3);
            accountCredential.setModifyTime(now);
            accountCredentialManager.save(accountCredential);
            result.setCode(200);
            result.setMessage("重置成功！");

        } catch (ArithmeticException e) {
            result.setCode(500);
            result.setMessage("重置出错");
            logger.error("重置操作-：" + e.getMessage() + "\r\n" + e.fillInStackTrace());
        }
        return result;
    }
    
    @GET
    @Path("person-info-getattendance")
    @Produces(MediaType.APPLICATION_JSON)
    @Log(desc = "得到考勤机信息", action = "search", operationDesc = "职员管理-得到考勤机信息")
    @SuppressWarnings("unchecked")
    public BaseDTO getPersonAttendance(
            @QueryParam("id") Long id
    ) {
        BaseDTO result = new BaseDTO();
        try {
            if (id == null || id < 1) {
                result.setCode(500);
                logger.debug("操作失败-获取参数id错误");
                result.setMessage("获取参数错误");
                return result;
            }
            
            PersonAttendanceMachine personAttendanceMachine=new PersonAttendanceMachine();
			List<PersonAttendanceMachine> personAttendanceList=personAttendanceMachineManager.find("from PersonAttendanceMachine where personId=?", id);
            if(personAttendanceList!=null&&personAttendanceList.size()>0){
            	personAttendanceMachine=personAttendanceList.get(0);
            }
            result.setData(personAttendanceMachine);
            result.setCode(200);
            result.setMessage("操作成功！");

        } catch (Exception e) {
            result.setCode(500);
            result.setMessage("获取数据出错");
            logger.error("获取考勤机数据操作操作-：" + e.getMessage() + "\r\n" + e.fillInStackTrace());
        }
        return result;
    }
    
    @POST
    @Path("person-info-setattendance")
    @Produces(MediaType.APPLICATION_JSON)
    @Log(desc = "设置考勤机信息", action = "update", operationDesc = "职员管理-考勤编号")
    @SuppressWarnings("unchecked")
    public BaseDTO setAttendance(
            @FormParam("id") Long id,
            @FormParam("attendanceNo") String attendanceNo,
            @FormParam("personNo") String personNo,
            @FormParam("note") String note
    ) {
        BaseDTO result = new BaseDTO();
        try {
            if (id == null || id < 1) {
                result.setCode(500);
                logger.debug("操作失败-获取参数id错误");
                result.setMessage("获取参数错误");
                return result;
            }
            
            if (StringUtils.isBlank(attendanceNo)) {
                result.setCode(500);
                result.setMessage("请输入考勤机编号！");
                return result;
            }
            
            if (StringUtils.isBlank(personNo)) {
                result.setCode(500);
                result.setMessage("请输入人员编号！");
                return result;
            }
            
            List<Map<String,Object>> mapAttendanceListExists=jdbcTemplate.queryForList("select * from person_machine where person_id<>? and mach_no=? and user_no=?",id,attendanceNo,personNo);
            if(mapAttendanceListExists!=null&&mapAttendanceListExists.size()>0){
            	result.setCode(500);
                result.setMessage("考勤机编号和人员编号已存在，请重新输入！");
                return result;
            }
            
            //id: id,attendanceNo:attendanceNo,personNo:personNo, note: note
            
            //List<Map<String,Object>> mapAttendanceList=jdbcTemplate.queryForList("select * from person_machine where person_id=?",id);
            
            
			List<PersonAttendanceMachine> personAttendanceList=personAttendanceMachineManager.find("from PersonAttendanceMachine where personId=?", id);
            if(personAttendanceList!=null&&personAttendanceList.size()>0){
            	PersonAttendanceMachine personAttendanceMachine=personAttendanceList.get(0);
            	personAttendanceMachine.setMach_no(attendanceNo);
            	personAttendanceMachine.setUser_no(personNo);
            	personAttendanceMachine.setRemark(note);
            	personAttendanceMachine.setCreate_date(new Date());
            	personAttendanceMachineManager.save(personAttendanceMachine);
            	/*jdbcTemplate.update("update person_machine set mach_no=?,user_no=?,remark=?,create_date=? where person_id=?",
        							attendanceNo,
        							personNo,
        							note,
        							new Date(),
        							id);*/
            }
            else {
            	Long insertIdLong=idGenerator.generateId();
            	PersonAttendanceMachine personAttendanceMachineInsert=new PersonAttendanceMachine();
            	//personAttendanceMachineInsert.setId(insertIdLong);
            	personAttendanceMachineInsert.setPersonId(id);
            	personAttendanceMachineInsert.setMach_no(attendanceNo);
            	personAttendanceMachineInsert.setUser_no(personNo);
            	personAttendanceMachineInsert.setRemark(note);
            	personAttendanceMachineInsert.setCreate_date(new Date());
            	personAttendanceMachineManager.save(personAttendanceMachineInsert);
			}
            result.setCode(200);
            result.setMessage("操作成功！");

        } catch (ArithmeticException e) {
            result.setCode(500);
            result.setMessage("操作出错");
            logger.error("设置考勤机操作-：" + e.getMessage() + "\r\n" + e.fillInStackTrace());
        }
        return result;
    }

    /**
     * 重置全部密码
     *
     * @author lilei
     **/
    @POST
    @Path("person-reset-alldefaultkey")
    @Produces(MediaType.APPLICATION_JSON)
    @Log(desc = "重置全部默认密码", action = "reset", operationDesc = "解锁管理-重置全部密码")
    public BaseDTO resetAllDefaultkey() {
        BaseDTO result = new BaseDTO();
        try {
            String defaultPwd = encodePassword("111111");//默认密码
            //String defaultPwd1=encodePassword("123456");//默认密码
            //return result;
            String hql = "from AccountCredential where catalog='default' and password=?";
            List<AccountCredential> accountCredentialList
                    = accountCredentialManager.find(hql, defaultPwd);

            String cooperKey = signInfo.getMessageCooperKey().trim();//"ceshi1";
            String strCooperName = signInfo.getMessageCooperName().trim();//"ceshi";

            if (accountCredentialList != null && accountCredentialList.size() > 0) {
                for (AccountCredential accountCredential : accountCredentialList) {
                    //账号未被物理删除
                    if (accountCredential.getAccountInfo().getDelFlag().equals("0")) {
                        PersonInfo personInfo = personInfoManager
                                .findUniqueBy("id", accountCredential.getAccountInfo().getId());
                        if (personInfo != null) {
                            if (personInfo.getCellphone() != null && !personInfo.getCellphone().equals("")) {
                                //更改密码
                                String strNewPwdCode = includePwdCode();
                                String strNewPwd = encodePassword(strNewPwdCode);
                                accountCredential.setPassword(strNewPwd);
                                accountCredential.setOperationPassword(strNewPwd);
                                Calendar calendar = Calendar.getInstance();
                                Date now = calendar.getTime();
                                // calendar.add(Calendar.MONTH, 3);
                                accountCredential.setModifyTime(now);
                                accountCredentialManager.save(accountCredential);
                                //发送短信
                                String strPhoneNo = personInfo.getCellphone();
                                String strMsg = ("[{user}]您好，"
                                        + "为了安全考虑，现已将您的登录密码重置为[{pwd}]，"
                                        + "私钥重置为[{pwd}]，请您登录系统后立即修改！")
                                        .replace("{user}", personInfo.getFullName())
                                        .replace("{pwd}", strNewPwdCode);


                                String strEncryptMsg = strPhoneNo + "|" + cooperKey + "|" + strMsg;
                                strEncryptMsg = PasswordUtil.getMD5(strEncryptMsg);

                                try {
                                    String endPoint = signInfo.getMessageRequestUrl();//"http://202.70.12.18/SmsService.asmx?wsdl";
                                    com.mossle.ws.sms.WebServiceSoapProxy sProxy = new com.mossle.ws.sms.WebServiceSoapProxy(endPoint);

                                    StringHolder strReturnDesc = new StringHolder();

                                    javax.xml.rpc.holders.IntHolder sendMsgResult = new javax.xml.rpc.holders.IntHolder();
                                    sProxy.sendMsg(strPhoneNo, strMsg, strEncryptMsg, strCooperName, sendMsgResult, strReturnDesc);
                                    String xxString = "";
                                } catch (RemoteException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            }
            result.setCode(200);
            result.setMessage("重置成功，未设置手机号人员未处理！！！");

        } catch (ArithmeticException e) {
            result.setCode(500);
            result.setMessage("重置出错");
            logger.error("重置操作-：" + e.getMessage() + "\r\n" + e.fillInStackTrace());
        }
        return result;
    }

    @POST
    @Path("person-edit-defaultkey")
    @Produces(MediaType.APPLICATION_JSON)
    @Log(desc = "首页修改密码", action = "reset", operationDesc = "首页修改密码")
    public BaseDTO changePasswordSave(
            @FormParam("oldPassword") String oldPassword,
            @FormParam("newPassword") String newPassword,
            @FormParam("confirmPassword") String confirmPassword) {
        BaseDTO result = new BaseDTO();
        try {
            Long accountId = Long.parseLong(currentUserHolder.getUserId());

            ChangePasswordResult changePasswordResult = changePasswordService.changePassword(accountId, oldPassword, newPassword,
                    confirmPassword);

            if (changePasswordResult.isSuccess()) {
                result.setCode(200);
                result.setMessage("修改成功！！！");
            } else {
                result.setCode(500);
                result.setMessage(changePasswordResult.getMessage());
            }
        } catch (ArithmeticException e) {
            result.setCode(500);
            result.setMessage("修改失败，请联系管理员！！！");
        }
        return result;
    }

    /**
     * 生成6位随机重置密码
     *
     * @author lilei 2017.12.12
     */
    private String includePwdCode() {
        String strPwdCode = "";
        char[] strChars = {'1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};

        int intCycle = 6;
        Random rd = new Random(System.currentTimeMillis());
        for (int i = 0; i < intCycle; i++) {
            char strChar = strChars[rd.nextInt(strChars.length - 1)];
            strPwdCode += strChar;
            if (strPwdCode.length() >= 6)
                break;
        }
        return strPwdCode.toLowerCase();
    }
    
    /**
     * 个人预设流程-删除
     * **/
    @POST
    @Path("custom-setting-del")
    @Produces(MediaType.APPLICATION_JSON)
    @Log(desc = "个人流程预设信息删除", action = "delete", operationDesc = "个人信息-个人流程预设-删除")
    public BaseDTO customSettingDelete(@FormParam("id") Long id){
    	BaseDTO result = new BaseDTO();
    	try {
    		CustomPresetApprover customPresetApprover=customPresetApproverManager.findUniqueBy("id",id);
        	if(customPresetApprover!=null)
        		customPresetApproverManager.remove(customPresetApprover);
        	
        	result.setCode(200);
        	result.setMessage("删除成功！");
		} catch (Exception ex) {
			logger.info(ex.getMessage()+"\r\n"+ex.getStackTrace());
			result.setCode(500);
        	result.setMessage("删除失败，数据异常！");
		}
    	return result;
    }

    //判断别名是否重复
    @GET
    @Path("anotherNameRepet")
    @Produces(MediaType.TEXT_PLAIN)
    public String  anotherNameRepet(@QueryParam("anotherName") String anotherName) 
    		throws UnsupportedEncodingException {
    	String strSearch="0";
    	String isOpenOtherName=userService.getOpenOtherNameStatus();
    	if(isOpenOtherName.equals("1")){
    		String anotherNameString =	URLDecoder.decode(anotherName,"UTF-8");
        	String currentUserId=currentUserHolder.getUserId();
        	String sqlString=String.format("select * from person_info WHERE ID<>%s and FULL_NAME = '%s'", currentUserId,anotherNameString);
        	List<Map<String, Object>> rootNodeIdList=jdbcTemplate.queryForList(sqlString);
        	if(rootNodeIdList!=null&&rootNodeIdList.size()>0){
        		return "1"; //别名重复
        	}
    	}
		return strSearch;
    }
    
    // ~ ======================================================================
    @Resource
    public void setAccountInfoManager(AccountInfoManager accountInfoManager) {
        this.accountInfoManager = accountInfoManager;
    }

    @Resource
    public void setPartyEntityManager(PartyEntityManager partyEntityManager) {
        this.partyEntityManager = partyEntityManager;
    }

    @Resource
    public void setCurrentUserHolder(CurrentUserHolder currentUserHolder) {
        this.currentUserHolder = currentUserHolder;
    }

    public void setDefaultUserRepoId(Long defaultUserRepoId) {
        this.defaultUserRepoId = defaultUserRepoId;
    }

    @Resource
    public void setPersonInfoManager(PersonInfoManager personInfoManager) {
        this.personInfoManager = personInfoManager;
    }

    @Resource
    public void setAccountCredentialManager(
            AccountCredentialManager accountCredentialManager) {
        this.accountCredentialManager = accountCredentialManager;
    }


    public String encodePassword(String password) {
        if (customPasswordEncoder != null) {
            return customPasswordEncoder.encode(password);
        } else {
            return password;
        }
    }

    @Resource
    public void setCustomPasswordEncoder(
            CustomPasswordEncoder customPasswordEncoder) {
        this.customPasswordEncoder = customPasswordEncoder;
    }

    @Resource
    public void setPartyStructManager(PartyStructManager partyStructManager) {
        this.partyStructManager = partyStructManager;
    }

    @Resource
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Resource
    public void setPartyResource(PartyResource partyResource) {
        this.partyResource = partyResource;
    }

    @Resource
    public void setPartyService(PartyService partyService) {
        this.partyService = partyService;
    }

    @Resource
    public void setSignInfo(SignInfo signInfo) {
        this.signInfo = signInfo;
    }

    @Resource
    public void setChangePasswordService(ChangePasswordService changePasswordService) {
        this.changePasswordService = changePasswordService;
    }

    @Resource
	public void setUserStatusManager(UserStatusManager userStatusManager) {
		this.userStatusManager = userStatusManager;
	}
    
    @Resource
	public void setUserService(UserService userService) {
		this.userService = userService;
	}
    
    @Resource
    public void setCustomPresetApproverManager(CustomPresetApproverManager customPresetApproverManager) {
		this.customPresetApproverManager = customPresetApproverManager;
	}
    @Resource
    public void setIdGenerator(IdGenerator idGenerator) {
		this.idGenerator = idGenerator;
	}
    
    @Resource
    public void setPersonAttendanceMachineManager(PersonAttendanceMachineManager personAttendanceMachineManager) {
		this.personAttendanceMachineManager = personAttendanceMachineManager;
	}
}
