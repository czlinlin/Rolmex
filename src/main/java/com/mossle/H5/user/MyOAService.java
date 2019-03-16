package com.mossle.H5.user;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.mossle.api.avatar.AvatarConnector;
import com.mossle.api.avatar.AvatarDTO;
import com.mossle.api.org.OrgConnector;
import com.mossle.api.store.StoreConnector;
import com.mossle.api.store.StoreDTO;
import com.mossle.api.tenant.TenantHolder;
import com.mossle.common.utils.WebAPI;
import com.mossle.core.annotation.Log;
import com.mossle.core.id.IdGenerator;
import com.mossle.core.store.InputStreamDataSource;
import com.mossle.core.util.StringUtils;
import com.mossle.internal.store.persistence.domain.StoreInfo;
import com.mossle.party.PartyConstants;
import com.mossle.party.persistence.domain.PartyEntity;
import com.mossle.party.persistence.domain.PartyStruct;
import com.mossle.party.persistence.manager.PartyEntityManager;
import com.mossle.pim.web.WorkReportInfoController;
import com.mossle.user.ImageUtils;
import com.mossle.user.persistence.domain.AccountCredential;
import com.mossle.user.persistence.domain.AccountInfo;
import com.mossle.user.persistence.domain.PersonAttendanceMachine;
import com.mossle.user.persistence.domain.PersonInfo;
import com.mossle.user.persistence.manager.AccountInfoManager;
import com.mossle.user.persistence.manager.PersonAttendanceMachineManager;
import com.mossle.user.persistence.manager.PersonInfoManager;
import com.mossle.user.persistence.manager.AccountCredentialManager;
import com.mossle.user.service.ChangePasswordService;
import com.mossle.user.service.UserService;
import com.mossle.user.support.ChangePasswordResult;
import com.mossle.party.support.PartyOrgConnector;

/**
 * @author lilei
 * @date 2017-09-18
 */
@Service
public class MyOAService {
	private static Logger logger = LoggerFactory.getLogger(WorkReportInfoController.class);
	private PersonInfoManager personInfoManager;
	private ChangePasswordService changePasswordService;
	private AccountInfoManager accountInfoManager;
	private AccountCredentialManager accountCredentialManager;
	private AvatarConnector avatarConnector;
	private StoreConnector storeConnector;
	private TenantHolder tenantHolder;
	private WebAPI webAPI;
	private OrgConnector orgConnector;
	private PartyEntityManager partyEntityManager;
	private UserService userService;
	private JdbcTemplate jdbcTemplate;
	private IdGenerator idGenerator;
	private PersonAttendanceMachineManager personAttendanceMachineManager;

	/*
	 * 查询我的信息 lilei
	 */
	@Log(desc = "查询”我的“信息", action = "Search", operationDesc = "手机APP-我的-我的信息")
	public Map<String, Object> GetMyInfo(Map<String, Object> decryptedMap) {
		// {strPageSize=5, strPerCode=2, percode=2, method=WorkReportToMe,
		// sign=e458da11bb70689df863747cb2020546, strPageIndex=1, timestamp=}

		Map<String, Object> returnMap = new HashMap<String, Object>();

		try {
			// 验证参数================================================================================
			if (!decryptedMap.containsKey("strPerCode")) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "参数错误");
				logger.debug("手机APP-我的-我的信息-手机没有传入strPerCode参数");
				return returnMap;
			}

			// 获取数据================================================
			long percode = Long.valueOf(decryptedMap.get("strPerCode").toString());

			logger.info("手机APP-我的传入参数：strPerCode=" + percode);

			List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

			PersonInfo personInfo = personInfoManager.findUniqueBy("id", percode);
			if (personInfo == null) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "未查询到您的信息");
				logger.debug("手机APP--我的信息我的-未查询到信息（PersonInfo）");
				return returnMap;
			}

			/*
			 * { "bSuccess":"true", "strMsg":"加载成功", " PersonnelInfo ":
			 * [{"PN01":"职员编号", "PN03":"用户名", "PN06":"姓名", "PN07C":"性别-中文",
			 * "PN09":"头像地址", "PN20C":"部门名称-中文", "PN21":"职位名称", "PL02":"手机号码",
			 * "PL03":"联系电话", "PL04":"传真", "PL05":"邮箱", "PL06":"微信",
			 * "PL07":"QQ号码", "PL08":"地址", }] }
			 */
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("PN01", TrimNull(personInfo.getEmployeeNo()));
			map.put("PN03", TrimNull(personInfo.getUsername()));
			map.put("PN06", TrimNull(personInfo.getFullName()));// 姓名
			map.put("PN07C", (personInfo.getGender() == null) ? "" : (personInfo.getGender().equals("1") ? "男" : "女"));// 性别-中文

			// String tenanId=tenantHolder.getTenantId();
			List<StoreInfo> infoList = storeConnector.getStore("avatar", decryptedMap.get("strPerCode").toString());
			if (infoList != null && infoList.size() > 0)
				map.put("PN09", infoList.get(0).getPath());// 头像地址
			else
				map.put("PN09", "");// 头像地址

			map.put("PN20C", TrimNull(personInfo.getDepartmentName()));// 部门名称-中文

			map.put("PN21", GetPositionsName(String.valueOf(percode)));// "职位名称"
			map.put("PL02", TrimNull(personInfo.getCellphone()));// "手机号码"
			map.put("PL03", TrimNull(personInfo.getTelephone()));// "联系电话"
			map.put("PL04", TrimNull(personInfo.getFax()));// "传真"
			map.put("PL05", TrimNull(personInfo.getEmail()));// "邮箱"
			map.put("PL06", TrimNull(personInfo.getWxNo()));// 微信
			map.put("PL07", TrimNull(personInfo.getQq()));// QQ号码
			map.put("PL08", TrimNull(personInfo.getAddress()));// 地址
			map.put("isOpenOtherName", userService.getOpenOtherNameStatus());//开启“别名”功能状态1是0否
			map.put("realName", TrimNull(personInfo.getRealName()));
			
			map.put("isEditOtherName",GetIsEditOtherName(percode));

			list.add(map);

			returnMap.put("bSuccess", "true");
			returnMap.put("strMsg", "加载成功");
			returnMap.put("PersonnelInfo", list);
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("bSuccess", "false");
			returnMap.put("strMsg", "加载错误，请联系管理员");
			logger.error("手机APP--我的信息我的-查询异常，" + e.getMessage() + "\r\n" + e.getStackTrace());
		}

		return returnMap;
	}
	
	/**
	 * 获取是否允许修改别名
	 * @return 0：是，1：否
	 * */
	private String GetIsEditOtherName(Object percode){
		String isEditOtherName="1";
		try{
			String strSql=String.format("select is_modify from person_machine where person_id=%s", percode);
			List<Map<String,Object>> mapIsEditList=jdbcTemplate.queryForList(strSql);
			//isEditOtherName=jdbcTemplate.queryForMap(strSql);
			if(mapIsEditList==null||mapIsEditList.size()<1)
				isEditOtherName="0";
			else 
				isEditOtherName=mapIsEditList.get(0).get("is_modify").toString();
		}
		catch(Exception ex){
			isEditOtherName="1";
		}
		return isEditOtherName;
	}

	private String GetPositionsName(String userId) {
		StringBuffer strReurn = new StringBuffer("");
		List<PartyEntity> list = orgConnector.getPostByUserId(userId);
		if (list != null && list.size() > 0) {
			for (PartyEntity party : list) {
				strReurn.append(party.getName() + ",");
			}
			strReurn.delete(strReurn.length() - 1, strReurn.length());
		}
		return strReurn.toString();
	}

	private String TrimNull(String value) {
		if (StringUtils.isBlank(value))
			return "";
		else {
			return value;
		}
	}

	/*
	 * 修改头像
	 */
	@Log(desc = "修改头像信息", action = "update", operationDesc = "手机APP-我的-修改头像")
	public Map<String, Object> ChangeMyHeadImg(Map<String, Object> decryptedMap) {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			// 验证参数================================================================================
			if (!decryptedMap.containsKey("strPerCode") || StringUtils.isBlank(decryptedMap.get("strPerCode").toString())) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "参数错误");
				logger.debug("手机APP-我的-我的信息-手机没有传入strPerCode参数");
				return returnMap;
			} else if (!decryptedMap.containsKey("strPhoto") || StringUtils.isBlank(decryptedMap.get("strPhoto").toString())) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "请选择头像地址");
				logger.debug("手机APP-我的-我的信息-手机没有传入strPhoto(头像地址)参数");
				return returnMap;
			}

			// 获取数据================================================
			long percode = Long.valueOf(decryptedMap.get("strPerCode").toString());
			String userId = decryptedMap.get("strPerCode").toString();
			String strPhoto = decryptedMap.get("strPhoto").toString();

			logger.info("手机APP-我的-修改头像传入参数：strPerCode:" + percode + "；strPhoto=" + strPhoto);

			PersonInfo personInfo = personInfoManager.findUniqueBy("id", percode);
			if (personInfo == null) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "未查询到您的信息");
				logger.debug("手机APP-我的-修改头像-未查询到信息（PersonInfo）");
				return returnMap;
			}

			String tenanId = tenantHolder.getTenantId();

			String filename = strPhoto.substring(strPhoto.lastIndexOf("/") + 1, strPhoto.length());

			storeConnector.saveUniqueStore("avatar", filename, tenanId, userId, strPhoto);
			returnMap.put("bSuccess", "true");
			returnMap.put("strMsg", "修改成功");

		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("bSuccess", "false");
			returnMap.put("strMsg", "保存错误，请联系管理员");
			logger.error("手机APP--我的-修改头像-修改异常，" + e.getMessage() + "\r\n" + e.getStackTrace());
		}

		return returnMap;
	}

	/*
	 * 修改我的信息 lilei
	 */
	@Log(desc = "修改联系信息", action = "update", operationDesc = "手机APP-我的-修改联系信息")
	public Map<String, Object> EditMyInfo(Map<String, Object> decryptedMap) {
		Map<String, Object> returnMap = new HashMap<String, Object>();

		try {
			// 验证参数================================================================================
			if (!decryptedMap.containsKey("strPerCode") || StringUtils.isBlank(decryptedMap.get("strPerCode").toString())) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "参数错误");
				logger.debug("手机APP-我的-修改联系信息-手机没有传入strPerCode参数");
				return returnMap;
			} else if (!decryptedMap.containsKey("strMobile") || StringUtils.isBlank(decryptedMap.get("strMobile").toString())) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "请填写手机号");
				return returnMap;
			}

			// 获取数据================================================
			long percode = Long.valueOf(decryptedMap.get("strPerCode").toString());

			PersonInfo personInfo = personInfoManager.findUniqueBy("id", percode);
			if (personInfo == null) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "未查询到您的信息");
				logger.debug("手机APP-我的-修改联系信息-未查询到信息（PersonInfo）");
				return returnMap;
			}

			// a) strPerCode：登录用户编号（必填）；
			// b) strMobile：手机号（必填）；
			// c) strLink：固话；
			// d) strFax：传真；
			// e) strEmail：邮箱；
			// f) strWei：微信；
			// g) strQQ：QQ号码；
			// h) strAddress：地址；
			
			boolean isOpenOtherName=userService.getOpenOtherNameStatus().equals("1");//“别名”功能开启状态，1是0否

			StringBuffer sbLog = new StringBuffer("手机APP-我的-修改联系信息-传入参数：");
			sbLog.append("strPerCode=" + percode);
			sbLog.append(";strMobile=" + decryptedMap.get("strMobile").toString());
			sbLog.append(";strLink=" + decryptedMap.get("strLink").toString());
			sbLog.append(";strFax=" + decryptedMap.get("strFax").toString());
			sbLog.append(";strEmail=" + decryptedMap.get("strEmail").toString());
			sbLog.append(";strWei=" + decryptedMap.get("strWei").toString());
			sbLog.append(";strQQ=" + decryptedMap.get("strQQ").toString());
			sbLog.append(";strAddress=" + decryptedMap.get("strAddress").toString());
			
			if(isOpenOtherName)
				sbLog.append(";otherName=" + decryptedMap.get("otherName").toString());
			logger.info(sbLog.toString());

			if (decryptedMap.get("strEmail") == null || decryptedMap.get("strEmail").toString().equals("")) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "邮箱必填");
				return returnMap;
			}
			if (decryptedMap.get("strEmail") != null && !decryptedMap.get("strEmail").toString().equals("")) {
				String pattern = "^[A-Za-z0-9\\u4e00-\\u9fa5]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$";
				Pattern r = Pattern.compile(pattern);
				Matcher matcher = r.matcher(decryptedMap.get("strEmail").toString());
				if (!matcher.matches()) {
					returnMap.put("bSuccess", "false");
					returnMap.put("strMsg", "邮箱格式不正确");
					return returnMap;
				}
				// Matcher matcher = pattern.ma(decryptedMap.get("strEmail"));
			}
			
			personInfo.setCellphone(decryptedMap.get("strMobile").toString());
			personInfo.setTelephone(decryptedMap.get("strLink").toString());
			personInfo.setFax(decryptedMap.get("strFax").toString());
			personInfo.setEmail(decryptedMap.get("strEmail").toString());
			personInfo.setWxNo(decryptedMap.get("strWei").toString());
			personInfo.setQq(decryptedMap.get("strQQ").toString());
			personInfo.setAddress(decryptedMap.get("strAddress").toString());
			
			String isEditOtherName=GetIsEditOtherName(percode);
			/*if(isOpenOtherName){
				if(isEditOtherName.equals("1"))
					
			}*/
			if(isOpenOtherName){
				//0:表示允许修改
				if(isEditOtherName.equals("0")){
					//别名
					String strOtherName=decryptedMap.get("otherName").toString();
					String sqlString=String.format("select * from person_info WHERE ID<>%s and FULL_NAME = '%s'", percode,strOtherName);
					List<Map<String, Object>> rootNodeIdList=jdbcTemplate.queryForList(sqlString);
		        	if(rootNodeIdList!=null&&rootNodeIdList.size()>0){
						returnMap.put("bSuccess", "false");
						returnMap.put("strMsg", "别名重复，请重新输入");
						return returnMap;
					}
		        	
		        	sqlString="from PersonAttendanceMachine where personId=?";
		        	List<PersonAttendanceMachine> personMachineList=personAttendanceMachineManager.find(sqlString, percode);
		        	
		        	Date currentTime = new Date();
	            	SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	            	String dateString = formatter.format(currentTime);
		        	if(personMachineList==null||personMachineList.size()<1){
		        		PersonAttendanceMachine personAttendanceMachine = new PersonAttendanceMachine();
		            	personAttendanceMachine.setPersonId(percode);
		            	personAttendanceMachine.setMach_no("");
		            	personAttendanceMachine.setUser_no("");
		            	personAttendanceMachine.setRemark("");
		            	personAttendanceMachine.setCreate_date(new Date());
		            	personAttendanceMachine.setIs_modify("1");
		                personAttendanceMachine.setModify_date(new Date());
		                personAttendanceMachine.setModify_num("1");
		                personAttendanceMachineManager.save(personAttendanceMachine);
		        	}
		        	else {
		        		PersonAttendanceMachine personAttendanceMachine=personMachineList.get(0);
		        		personAttendanceMachine.setIs_modify("1");
		        		personAttendanceMachine.setModify_date(new Date());
		        		String modifyNum="0";
		        		if(!StringUtils.isBlank(personAttendanceMachine.getModify_num())){
		        			modifyNum=(Integer.parseInt(personAttendanceMachine.getModify_num())+1)+"";
		        		}
		        		personAttendanceMachine.setModify_num(modifyNum);
		        		personAttendanceMachineManager.save(personAttendanceMachine);
					}
		        	
					PartyEntity partyEntity=partyEntityManager.findUniqueBy("id", personInfo.getId());
					partyEntity.setName(strOtherName);
					partyEntityManager.save(partyEntity);
					
					AccountInfo accountInfo=accountInfoManager.findUniqueBy("id", personInfo.getId());
					accountInfo.setDisplayName(strOtherName);
					accountInfoManager.save(accountInfo);
					
					personInfo.setFullName(strOtherName);
				}
			}
				
			personInfoManager.save(personInfo);
					
			returnMap.put("bSuccess", "true");
			returnMap.put("strMsg", "修改成功");
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("bSuccess", "false");
			returnMap.put("strMsg", "保存错误，请联系管理员");
			logger.error("手机APP--我的-修改联系信息-修改异常，" + e.getMessage() + "\r\n" + e.getStackTrace());
		}

		return returnMap;
	}

	/*
	 * 修改密码
	 */
	@Log(desc = "修改密码", action = "update", operationDesc = "手机APP-我的-修改密码")
	public Map<String, Object> ChangeMyPwd(Map<String, Object> decryptedMap) {
		Map<String, Object> returnMap = new HashMap<String, Object>();

		try {
			// 验证参数================================================================================
			if (!decryptedMap.containsKey("strPerCode") || StringUtils.isBlank(decryptedMap.get("strPerCode").toString())) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "参数错误");
				logger.debug("手机APP-我的-修改密码-手机没有传入strPerCode参数");
				return returnMap;
			} else if (!decryptedMap.containsKey("strPwdOld") || StringUtils.isBlank(decryptedMap.get("strPwdOld").toString())) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "请填写原密码");
				return returnMap;
			} else if (!decryptedMap.containsKey("strPwdNew") || StringUtils.isBlank(decryptedMap.get("strPwdNew").toString())) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "请填写新密码");
				return returnMap;
			}

			// 获取数据================================================
			long percode = Long.valueOf(decryptedMap.get("strPerCode").toString());

			String oldPassword = String.valueOf(decryptedMap.get("strPwdOld").toString());
			String newPassword = String.valueOf(decryptedMap.get("strPwdNew").toString());
			String confirmPassword = newPassword;

			StringBuffer sbLog = new StringBuffer("手机APP-我的-修改密码-传入参数：");
			sbLog.append("strPerCode=" + percode);
			sbLog.append(";strPwdOld=" + decryptedMap.get("strPwdOld").toString());
			sbLog.append(";strPwdNew=" + decryptedMap.get("strPwdNew").toString());
			logger.info(sbLog.toString());

			PersonInfo personInfo = personInfoManager.findUniqueBy("id", percode);
			if (personInfo == null) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "未查询到您的信息");
				logger.debug("手机APP-我的-修改密码-未查询到信息（PersonInfo）");
				return returnMap;
			}

			ChangePasswordResult changePasswordResult = changePasswordService.changePassword(percode, oldPassword,
					newPassword, confirmPassword);

			if (changePasswordResult.isSuccess()) {
				returnMap.put("bSuccess", "true");
				returnMap.put("strMsg", "修改成功");
			} else {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", changePasswordResult.getMessage());
				logger.debug("手机APP-我的-修改密码-保存失败（PersonInfo）");
			}
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("bSuccess", "false");
			returnMap.put("strMsg", "保存错误，请联系管理员");
			logger.error("手机APP-我的-修改密码-保存异常，" + e.getMessage() + "\r\n" + e.getStackTrace());
		}
		return returnMap;
	}

	/*
	 * 修改私钥 lilei
	 */
	@Log(desc = "修改私钥", action = "update", operationDesc = "手机APP-我的-修改私钥")
	public Map<String, Object> ChangeMyOpterationPwd(Map<String, Object> decryptedMap) {
		Map<String, Object> returnMap = new HashMap<String, Object>();

		try {
			// 验证参数================================================================================
			if (!decryptedMap.containsKey("strPerCode") || StringUtils.isBlank(decryptedMap.get("strPerCode").toString())) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "参数错误");
				logger.debug("手机APP-我的-修改私钥-手机没有传入strPerCode参数");
				return returnMap;
			} else if (!decryptedMap.containsKey("strPwdOld") || StringUtils.isBlank(decryptedMap.get("strPwdOld").toString())) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "请填写原密码");
				return returnMap;
			} else if (!decryptedMap.containsKey("strPwdNew") || StringUtils.isBlank(decryptedMap.get("strPwdNew").toString())) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "请填写新密码");
				return returnMap;
			}

			// 获取数据================================================
			long percode = Long.valueOf(decryptedMap.get("strPerCode").toString());

			String oldPassword = String.valueOf(decryptedMap.get("strPwdOld").toString());
			String newPassword = String.valueOf(decryptedMap.get("strPwdNew").toString());
			String confirmPassword = newPassword;

			StringBuffer sbLog = new StringBuffer("手机APP-我的-修改私钥-传入参数：");
			sbLog.append("strPerCode=" + percode);
			sbLog.append(";strPwdOld=" + decryptedMap.get("strPwdOld").toString());
			sbLog.append(";strPwdNew=" + decryptedMap.get("strPwdNew").toString());
			logger.info(sbLog.toString());

			PersonInfo personInfo = personInfoManager.findUniqueBy("id", percode);
			if (personInfo == null) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "未查询到您的信息");
				logger.debug("手机APP-我的-修改私钥-未查询到信息（PersonInfo）");
				return returnMap;
			}

			ChangePasswordResult changePasswordResult = changePasswordService.changeOperationPassword(percode,
					oldPassword, newPassword, confirmPassword);

			if (changePasswordResult.isSuccess()) {
				returnMap.put("bSuccess", "true");
				returnMap.put("strMsg", "修改成功");
			} else {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", changePasswordResult.getMessage());
				logger.debug("手机APP-我的-修改私钥-保存失败");
			}
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("bSuccess", "false");
			returnMap.put("strMsg", "保存错误，请联系管理员");
			logger.error("手机APP-我的-修改私钥-保存异常，" + e.getMessage() + "\r\n" + e.getStackTrace());
		}
		return returnMap;
	}

	/*
	 * 验证私钥 lilei
	 */
	@Log(desc = "验证私钥", action = "search", operationDesc = "手机APP-我的-验证私钥")
	public Map<String, Object> CheckMyOpterationPwd(Map<String, Object> decryptedMap) {
		Map<String, Object> returnMap = new HashMap<String, Object>();

		try {
			// 验证参数================================================================================
			if (!decryptedMap.containsKey("strPerCode") || StringUtils.isBlank(decryptedMap.get("strPerCode").toString())) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "参数错误");
				logger.debug("手机APP-我的-验证私钥-手机没有传入strPerCode参数");
				return returnMap;
			} else if (!decryptedMap.containsKey("strPrivatePwd") || StringUtils.isBlank(decryptedMap.get("strPrivatePwd").toString())) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "请输入密匙");
				return returnMap;
			}

			// 获取数 据================================================
			long percode = Long.valueOf(decryptedMap.get("strPerCode").toString());

			String strPrivatePwd = String.valueOf(decryptedMap.get("strPrivatePwd").toString());

			StringBuffer sbLog = new StringBuffer("手机APP-我的-验证私钥-传入参数：");
			sbLog.append("strPerCode=" + percode);
			sbLog.append(";strPrivatePwd=" + decryptedMap.get("strPrivatePwd").toString());
			logger.info(sbLog.toString());

			AccountInfo accountInfo = accountInfoManager.get(percode);

			String hql = "from AccountCredential where accountInfo=? and catalog='default'";
			AccountCredential accountCredential = accountCredentialManager.findUnique(hql, accountInfo);

			if (changePasswordService.isPasswordValid(strPrivatePwd, accountCredential.getOperationPassword())) {
				returnMap.put("bSuccess", "true");
				returnMap.put("strMsg", "验证通过");
			} else {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "私钥不正确");
			}
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("bSuccess", "false");
			returnMap.put("strMsg", "验证错误，请联系管理员");
			logger.error("手机APP-我的-修改私钥-保存异常，" + e.getMessage() + "\r\n" + e.getStackTrace());
		}
		return returnMap;
	}
	// gestureSwitch

	/*
	 * 更新手势密码开启状态 lilei
	 */
	@Log(desc = "更新手势密码开启状态", action = "update", operationDesc = "手机APP-我的-更新手势密码开启状态")
	public Map<String, Object> ChangePicPwdStatus(Map<String, Object> decryptedMap) {
		Map<String, Object> returnMap = new HashMap<String, Object>();

		try {
			// 验证参数================================================================================
			if (!decryptedMap.containsKey("strPerCode") || StringUtils.isBlank(decryptedMap.get("strPerCode").toString())) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "参数错误");
				logger.debug("手机APP-我的-更新手势密码开启状态-手机没有传入strPerCode参数");
				return returnMap;
			}

			// 获取数据================================================
			long percode = Long.valueOf(decryptedMap.get("strPerCode").toString());

			StringBuffer sbLog = new StringBuffer("手机APP-我的-更新手势密码开启状态-传入参数：");
			sbLog.append("strPerCode=" + percode);
			logger.info(sbLog.toString());

			PersonInfo personInfo = personInfoManager.findUniqueBy("id", percode);
			if (personInfo == null) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "未查询到您的信息");
				return returnMap;
			}

			// 手势开关
			String hql = "from AccountCredential where accountInfo.id = ?";

			AccountCredential accountCredential = accountCredentialManager.findUnique(hql, percode);
			// if (accountCredential != null) {
			// model.addAttribute("gestureSwitch",
			// accountCredential.getGestureSwitch());
			// } else {
			// model.addAttribute("gestureSwitch", null);
			// }
			if (accountCredential.getGestureSwitch().equals("open"))
				accountCredential.setGestureSwitch("close");
			else
				accountCredential.setGestureSwitch("open");

			accountCredentialManager.save(accountCredential);

			returnMap.put("bSuccess", "true");
			returnMap.put("strMsg", "更新成功");
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("bSuccess", "false");
			returnMap.put("strMsg", "更新手势状态错误，请联系管理员");
			logger.error("手机APP-我的-更新手势密码开启状态-更新异常，" + e.getMessage() + "\r\n" + e.getStackTrace());
		}
		return returnMap;
	}

	/*
	 * 验证手势密码 lilei
	 */
	@Log(desc = "验证手势密码", action = "search", operationDesc = "手机APP-我的-验证手势密码")
	public Map<String, Object> CheckPicPwd(Map<String, Object> decryptedMap) {
		Map<String, Object> returnMap = new HashMap<String, Object>();

		try {
			// 验证参数================================================================================
			if (!decryptedMap.containsKey("strPerCode") || StringUtils.isBlank(decryptedMap.get("strPerCode").toString())) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "参数错误");
				logger.debug("手机APP-我的-更新手势密码开启状态-手机没有传入strPerCode参数");
				return returnMap;
			} else if (!decryptedMap.containsKey("strPicPwd") || StringUtils.isBlank(decryptedMap.get("strPicPwd").toString())) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "请连接手势");
				return returnMap;
			}

			// 获取数据================================================
			long percode = Long.valueOf(decryptedMap.get("strPerCode").toString());
			String strPicPwd = String.valueOf(decryptedMap.get("strPicPwd").toString());

			StringBuffer sbLog = new StringBuffer("手机APP-我的-更新手势密码开启状态-传入参数：");
			sbLog.append("strPerCode=" + percode);
			sbLog.append("strPicPwd=" + strPicPwd);
			logger.info(sbLog.toString());

			PersonInfo personInfo = personInfoManager.findUniqueBy("id", percode);
			if (personInfo == null) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "未查询到您的信息");
				return returnMap;
			}

			// 手势开关
			String hql = "from AccountCredential where accountInfo.id = ?";

			AccountCredential accountCredential = accountCredentialManager.findUnique(hql, percode);
			if (accountCredential.getGesturePassword().equals(strPicPwd)) {
				returnMap.put("bSuccess", "true");
				returnMap.put("strMsg", "通过");
				return returnMap;
			} else {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "验证没通过");
				logger.info("手机APP-我的-更新手势密码开启状态-手势验证没有通过");
				return returnMap;
			}
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("bSuccess", "false");
			returnMap.put("strMsg", "验证手势密码错误，请联系管理员");
			logger.error("手机APP-我的-验证手势密码-异常，" + e.getMessage() + "\r\n" + e.getStackTrace());
		}
		return returnMap;
	}

	@Log(desc = "查询手势状态", action = "search", operationDesc = "手机APP-我的-查询手势状态")
	public Map<String, Object> SearchPicPwdStatus(Map<String, Object> decryptedMap) {
		Map<String, Object> returnMap = new HashMap<String, Object>();

		try {
			// 验证参数================================================================================
			if (!decryptedMap.containsKey("strPerCode") || StringUtils.isBlank(decryptedMap.get("strPerCode").toString())) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "参数错误");
				logger.debug("手机APP-我的-查询手势状态-手机没有传入strPerCode参数");
				return returnMap;
			}

			// 获取数据================================================
			long percode = Long.valueOf(decryptedMap.get("strPerCode").toString());
			// String strPicPwd =
			// String.valueOf(decryptedMap.get("strPicPwd").toString());

			StringBuffer sbLog = new StringBuffer("手机APP-我的-更新手势密码开启状态-传入参数：");
			sbLog.append("strPerCode=" + percode);
			// sbLog.append("strPicPwd=" + strPicPwd);
			logger.info(sbLog.toString());

			PersonInfo personInfo = personInfoManager.findUniqueBy("id", percode);
			if (personInfo == null) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "未查询到您的信息");
				return returnMap;
			}

			// 手势开关
			String hql = "from AccountCredential where accountInfo.id = ?";

			AccountCredential accountCredential = accountCredentialManager.findUnique(hql, percode);
			/*
			 * if (accountCredential.getGestureSwitch().equals("open"))
			 * accountCredential.setGestureSwitch("close"); else
			 * accountCredential.setGestureSwitch("open");
			 */

			returnMap.put("bSuccess", "true");
			returnMap.put("strMsg", "加载成功");
			returnMap.put("switchStatus", accountCredential.getGestureSwitch());

		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("bSuccess", "false");
			returnMap.put("strMsg", "验证手势密码错误，请联系管理员");
			logger.error("手机APP-我的-验证手势密码-异常，" + e.getMessage() + "\r\n" + e.getStackTrace());
		}
		return returnMap;
	}

	/*
	 * 添加/修改手势密码 lilei
	 */
	@Log(desc = "添加/修改手势密码", action = "update", operationDesc = "手机APP-我的-添加/修改手势密码")
	public Map<String, Object> AddAndEditPicPwd(Map<String, Object> decryptedMap) {
		Map<String, Object> returnMap = new HashMap<String, Object>();

		try {
			// 验证参数================================================================================
			if (!decryptedMap.containsKey("strPerCode") || StringUtils.isBlank(decryptedMap.get("strPerCode").toString()) 
				|| !decryptedMap.containsKey("strActionType") || StringUtils.isBlank(decryptedMap.get("strActionType").toString())
				|| !decryptedMap.containsKey("strPicPwd") || StringUtils.isBlank(decryptedMap.get("strPicPwd").toString()) 
				|| !decryptedMap.containsKey("strPicPwdStatus") || StringUtils.isBlank(decryptedMap.get("strPicPwdStatus").toString())) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "参数错误");
				logger.debug("手机APP-我的-更新手势密码开启状态-参数错误");
				return returnMap;
			}

			// 获取数据================================================
			long percode = Long.valueOf(decryptedMap.get("strPerCode").toString());
			String strActionType = String.valueOf(decryptedMap.get("strActionType").toString());
			String strPicPwd = String.valueOf(decryptedMap.get("strPicPwd").toString());
			String strPicPwdStatus = String.valueOf(decryptedMap.get("strPicPwdStatus").toString());

			StringBuffer sbLog = new StringBuffer("手机APP-我的-更新手势密码开启状态-传入参数：");
			sbLog.append("strPerCode=" + percode);
			sbLog.append(";strActionType=" + strActionType);
			sbLog.append(";strPicPwd=" + strPicPwd);
			sbLog.append(";strPicPwdStatus=" + strPicPwdStatus);
			sbLog.append(";strPicPwdOld=" + decryptedMap.get("strPicPwdOld"));
			logger.info(sbLog.toString());

			PersonInfo personInfo = personInfoManager.findUniqueBy("id", percode);
			if (personInfo == null) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "未查询到您的信息");
				return returnMap;
			}

			// 手势开关
			String hql = "from AccountCredential where accountInfo.id = ?";

			AccountCredential accountCredential = accountCredentialManager.findUnique(hql, percode);

			if (strActionType.equals("1")) {
				if (!decryptedMap.containsKey("strPicPwdOld") || StringUtils.isBlank(decryptedMap.get("strPicPwdOld").toString())) {
					returnMap.put("bSuccess", "false");
					returnMap.put("strMsg", "参数错误");
					return returnMap;
				}
				String strPicPwdOld = String.valueOf(decryptedMap.get("strPicPwdOld").toString());
				if (!accountCredential.getGesturePassword().equals(strPicPwdOld)) {
					returnMap.put("bSuccess", "false");
					returnMap.put("strMsg", "原支付密码错误！");
					return returnMap;
				}
			}
			accountCredential.setGesturePassword(strPicPwd);
			accountCredential.setGestureSwitch(strPicPwdStatus.equals("1") ? "open" : "close");

			accountCredentialManager.save(accountCredential);

			returnMap.put("bSuccess", "true");
			returnMap.put("strMsg", "更新成功");
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("bSuccess", "false");
			returnMap.put("strMsg", "设置或更新手势密码错误，请联系管理员");
			logger.error("手机APP-我的-设置或更新手势密码-异常，" + e.getMessage() + "\r\n" + e.getStackTrace());
		}
		return returnMap;
	}

	/** 设置推送消息开关(初始化) lilei **/
	@Log(desc = "设置推送消息开关(初始化)", action = "search", operationDesc = "手机APP-我的-设置推送消息开关(初始化)")
	public Map<String, Object> SearchPushSwitchStatus(Map<String, Object> decryptedMap) {
		// {strPerCode=2, percode=2,
		// method=getpushonoff,sign=f953b4c3460eb4e790b99538244b5e3b,
		// timestamp=}

		Map<String, Object> returnMap = new HashMap<String, Object>();

		try {
			// 验证参数================================================================================
			if (!decryptedMap.containsKey("strPerCode") || StringUtils.isBlank(decryptedMap.get("strPerCode").toString()) ) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "参数错误");
				logger.debug("手机APP-我的-设置推送消息开关(初始化)-手机没有传入strPerCode参数");
				return returnMap;
			}

			// 获取数 据================================================
			long percode = Long.valueOf(decryptedMap.get("strPerCode").toString());

			StringBuffer sbLog = new StringBuffer("手机APP-我的-设置推送消息开关(初始化)-传入参数：");
			sbLog.append("strPerCode=" + percode);
			logger.info(sbLog.toString());

			AccountInfo accountInfo = accountInfoManager.get(percode);

			String hql = "from AccountCredential where accountInfo=? and catalog='default'";
			AccountCredential accountCredential = accountCredentialManager.findUnique(hql, accountInfo);

			List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
			// type:1消息，2公告，3审批

			if (accountCredential.getMessagePushStatus() != null) {
				if (accountCredential.getMessagePushStatus().equals("0")) {
					Map<String, Object> mapMsg = new HashMap<String, Object>();
					mapMsg.put("varPushType", "1");// 1消息
					list.add(mapMsg);
				}
			}

			if (accountCredential.getNoticePushStatus() != null) {
				if (accountCredential.getNoticePushStatus().equals("0")) {
					Map<String, Object> mapNotice = new HashMap<String, Object>();
					mapNotice.put("varPushType", "2");// 2公告
					list.add(mapNotice);
				}
			}

			if (accountCredential.getAuditPushStatus() != null) {
				if (accountCredential.getAuditPushStatus().equals("0")) {
					Map<String, Object> mapAudit = new HashMap<String, Object>();
					mapAudit.put("varPushType", "3");// 3审批
					list.add(mapAudit);
				}
			}

			returnMap.put("bSuccess", "true");
			if (list.size() > 0) {
				returnMap.put("strMsg", "加载成功");
				returnMap.put("ClosePushMsgType", list);
			} else
				returnMap.put("strMsg", "暂无数据");

		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("bSuccess", "false");
			returnMap.put("strMsg", "加载错误，请联系管理员");
			logger.error("手机APP-我的-设置推送消息开关(初始化)-加载异常，" + e.getMessage() + "\r\n" + e.getStackTrace());
		}
		return returnMap;
	}

	/**
	 * 设置推送开/关，1消息，2公告，3审批 lilei
	 */
	@Log(desc = "设置推送开/关", action = "search", operationDesc = "手机APP-我的-设置推送开/关")
	public Map<String, Object> SetPushSwitchStatus(Map<String, Object> decryptedMap) {
		Map<String, Object> returnMap = new HashMap<String, Object>();

		try {
			// 验证参数================================================================================
			if (!decryptedMap.containsKey("strPerCode") || StringUtils.isBlank(decryptedMap.get("strPerCode").toString()) ) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "参数错误");
				logger.debug("手机APP-我的-设置推送开/关-手机没有传入strPerCode或则pushType参数");
				return returnMap;
			}

			// 获取数据================================================
			long percode = Long.valueOf(decryptedMap.get("strPerCode").toString());
			String pushType = String.valueOf(decryptedMap.get("pushType").toString());

			StringBuffer sbLog = new StringBuffer("手机APP-我的-设置推送开/关-传入参数：");
			sbLog.append("strPerCode=" + percode);
			sbLog.append(";pushType=" + pushType);
			logger.info(sbLog.toString());

			PersonInfo personInfo = personInfoManager.findUniqueBy("id", percode);
			if (personInfo == null) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "未查询到您的信息");
				return returnMap;
			}

			// 手势开关
			String hql = "from AccountCredential where accountInfo.id = ?";

			AccountCredential accountCredential = accountCredentialManager.findUnique(hql, percode);
			if (pushType.equals("")) {
				accountCredential.setMessagePushStatus("1");
				accountCredential.setNoticePushStatus("1");
				accountCredential.setAuditPushStatus("1");
			}
			pushType = "," + pushType + ",";
			if (pushType.contains(",1,"))
				accountCredential.setMessagePushStatus("0");
			else
				accountCredential.setMessagePushStatus("1");

			if (pushType.contains(",2,"))
				accountCredential.setNoticePushStatus("0");
			else
				accountCredential.setNoticePushStatus("1");

			if (pushType.contains(",3,"))
				accountCredential.setAuditPushStatus("0");
			else
				accountCredential.setAuditPushStatus("1");

			accountCredentialManager.save(accountCredential);
			returnMap.put("bSuccess", "true");
			returnMap.put("strMsg", "设置成功");

		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("bSuccess", "false");
			returnMap.put("strMsg", "设置错误，请联系管理员");
			logger.error("手机APP-我的-设置推送开/关-保存异常，" + e.getMessage() + "\r\n" + e.getStackTrace());
		}
		return returnMap;
	}

	@Resource
	public void setPersonInfoManager(PersonInfoManager personInfoManager) {
		this.personInfoManager = personInfoManager;
	}

	@Resource
	public void setChangePasswordService(ChangePasswordService changePasswordService) {
		this.changePasswordService = changePasswordService;
	}

	@Resource
	public void setAccountInfoManager(AccountInfoManager accountInfoManager) {
		this.accountInfoManager = accountInfoManager;
	}

	@Resource
	public void setAccountCredentialManager(AccountCredentialManager accountCredentialManager) {
		this.accountCredentialManager = accountCredentialManager;
	}

	@Resource
	public void setAvatarConnector(AvatarConnector avatarConnector) {
		this.avatarConnector = avatarConnector;
	}

	@Resource
	public void setStoreConnector(StoreConnector storeConnector) {
		this.storeConnector = storeConnector;
	}

	@Resource
	public void setTenantHolder(TenantHolder tenantHolder) {
		this.tenantHolder = tenantHolder;
	}

	@Resource
	public void setWebAPI(WebAPI webAPI) {
		this.webAPI = webAPI;
	}

	@Resource
	public void setOrgConnector(OrgConnector orgConnector) {
		this.orgConnector = orgConnector;
	}
	
	@Resource
	public void setPartyEntityManager(PartyEntityManager partyEntityManager) {
		this.partyEntityManager = partyEntityManager;
	}
	
	@Resource
	public void setUserService(UserService userService) {
		this.userService = userService;
	}
	
	@Resource
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
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