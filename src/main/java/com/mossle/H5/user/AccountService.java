/**
 *
 */
package com.mossle.H5.user;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.mossle.api.audit.AuditConnector;
import com.mossle.api.audit.AuditDTO;
import com.mossle.api.user.AccountStatus;
import com.mossle.api.user.AuthenticationHandler;
import com.mossle.common.utils.NetworkUtil;
import com.mossle.common.utils.WebAPI;
import com.mossle.spi.device.DeviceConnector;
import com.mossle.user.persistence.domain.AccountCredential;
import com.mossle.user.persistence.domain.AccountInfo;
import com.mossle.user.persistence.manager.AccountCredentialManager;
import com.mossle.user.persistence.manager.AccountInfoManager;
import com.mossle.user.rs.UserResource;

/**
 * @author Bing
 */
@Service
public class AccountService {
    private static Logger logger = LoggerFactory.getLogger(UserResource.class);
    private JdbcTemplate jdbcTemplate;
    private AuthenticationHandler authenticationHandler;
    private WebAPI webAPI;
    private AccountInfoManager accountInfoManager;
    private AuditConnector auditConnector;
    private DeviceConnector deviceConnector;

    public Map<String, Object> H5Logon(Map<String, Object> decryptedMap, HttpServletRequest httpRequest)
            throws UnknownHostException {
        // {percode=, method=H5Logon, strPwdType=0, strUserName=admin,
        // sign=f12acec3042394a245ae429cd49b55ae, strPwd=111111, timestamp=}

        // 返回值
        Map<String, Object> returnMap = new HashMap<String, Object>();

        String strUserName = decryptedMap.get("strUserName").toString();
        String strPwd = decryptedMap.get("strPwd").toString();
        String strPwdType = decryptedMap.get("strPwdType").toString();

        // 验证
        String username = strUserName.toLowerCase();
        StringBuilder outAccountStatus = new StringBuilder();// 通过参数返回Bing2018.2.11
        String response = authenticationHandler.doAuthenticate(username, strPwd, strPwdType, outAccountStatus);
        // System.out.println(outAccountStatus);

        // 验证不成功的返回===============================================
        if (AccountStatus.ACCOUNT_NOT_EXISTS.equals(response)) {
            returnMap.put("bSuccess", "false");
            returnMap.put("strMsg", "用户名不存在");
            saveLog(username, httpRequest, "用户名不存在");
            return returnMap;
        }

        if (AccountStatus.BAD_CREDENTIALS_Attempt2.equals(response)) {
            if (strUserName.equals("admin")) {
                returnMap.put("bSuccess", "false");
                returnMap.put("strMsg", "用户名或密码错误");
                saveLog(username, httpRequest, "用户名或密码错误");
            } else {
                returnMap.put("bSuccess", "false");
                returnMap.put("strMsg", "用户名或密码错误，您还可以尝试2次。");
                saveLog(username, httpRequest, "用户名或密码错误，您还可以尝试2次。");
            }
            return returnMap;
        }

        if (AccountStatus.BAD_CREDENTIALS_Attempt1.equals(response)) {
            if (strUserName.equals("admin")) {
                returnMap.put("bSuccess", "false");
                returnMap.put("strMsg", "用户名或密码错误");
                saveLog(username, httpRequest, "用户名或密码错误");
            } else {
                returnMap.put("bSuccess", "false");
                returnMap.put("strMsg", "用户名或密码错误，您还可以尝试1次。");
                saveLog(username, httpRequest, "用户名或密码错误，您还可以尝试1次。");
            }
            return returnMap;
        }

        if (AccountStatus.BAD_CREDENTIALS_LOCKED.equals(response)) {
            if (strUserName.equals("admin")) {
                returnMap.put("bSuccess", "false");
                returnMap.put("strMsg", "用户名或密码错误");
                saveLog(username, httpRequest, "用户名或密码错误");
            } else {
                returnMap.put("bSuccess", "false");
                returnMap.put("strMsg", "用户名或密码错误，已锁定。");
                saveLog(username, httpRequest, "用户名或密码错误，已锁定。");
            }
            return returnMap;
        }

        if (AccountStatus.PASSWORD_NOT_EXISTS.equals(response)) {
            returnMap.put("bSuccess", "false");
            returnMap.put("strMsg", "密码未设置");
            saveLog(username, httpRequest, "密码未设置");
            return returnMap;
        }

        if (AccountStatus.LOCKED.equals(response)) {
            returnMap.put("bSuccess", "false");
            returnMap.put("strMsg", "已锁定");
            saveLog(username, httpRequest, "已锁定");
            return returnMap;
        }

        if (AccountStatus.Gesture_NOT_EXISTS.equals(response)) {
            returnMap.put("bSuccess", "false");
            returnMap.put("strMsg", "手势未设置");
            saveLog(username, httpRequest, "手势未设置");
            return returnMap;
        }

        if (AccountStatus.Gesture_NOT_Open.equals(response)) {
            returnMap.put("bSuccess", "false");
            returnMap.put("strMsg", "手势未设置或已关闭");
            saveLog(username, httpRequest, "手势未设置或已关闭");
            return returnMap;
        }

        if (!AccountStatus.SUCCESS.equals(response)) {
            returnMap.put("bSuccess", "false");
            returnMap.put("strMsg", response);// 其他不成功的返回
            saveLog(username, httpRequest, response);
            return returnMap;
        }

        // 验证成功的返回=============================================
        String sql = "SELECT * FROM v_h5_logon WHERE varUserName = ?";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, new Object[]{strUserName});

        returnMap.put("bSuccess", "true");
        returnMap.put("strMsg", "登录成功");
        returnMap.put("OAUploadUrl", webAPI.getUploadUrl());
        returnMap.put("OAViewUrl", webAPI.getViewUrl());
        returnMap.put("OADownloadUrl", webAPI.getDownloadUrl());
        returnMap.put("LogOnData", list);
        
        //登录记录token
        Map<String, Object> userMap=list.get(0);
        String token = deviceConnector.getToeknValue(userMap.get("varPerCode").toString());
		returnMap.put("token", token);

        // 简单密码需要修改，返回是否修改密码。---------------------------------------------------------------------
        String isChangePwd = "0";// 是否修改密码；1：是，0：否
        if (outAccountStatus.indexOf(AccountStatus.OTP_PASSWORD_TOO_SIMPLE) > -1)
            isChangePwd = "1";

        returnMap.put("isChangePwd", isChangePwd);

        saveLog(username, httpRequest, "成功");

		/*
         * {"bSuccess":"true","strMsg":"登录成功","OAUploadUrl":
		 * "http://192.168.226.123:9200","LogOnData":[
		 * {"varPerCode":"PE0000000001","DepartID":100400,"varUserName":"admin",
		 * "varRealName":"管理员","varPhoto":"","varPrivateKey":"","intCompanyID":
		 * 100000,"chrIsPermiss":"1","varParamKey":"2017090713540364","varPerNo"
		 * :null,"PositionIDS":"100402","JobLevel":0,"ExistsAllAudit":"0",
		 * "ExistsMyAudit":"0","PN24C":"0","PN25":"0","ExistsCustom":"0"}]}
		 */

        return returnMap;
    }

    @SuppressWarnings("unused")
    private void saveLog(String username, HttpServletRequest httpRequest, String result) throws UnknownHostException {

        username = username.toLowerCase();

        String hqlai = "from AccountInfo where username=? and delFlag = '0'";
        AccountInfo accountInfo = accountInfoManager.findUnique(hqlai, username);// .findUniqueBy("username",username);

        String userId = "";
        if (accountInfo != null) {
            userId = Long.toString(accountInfo.getId());
        } else {
            userId = username;
        }

        AuditDTO auditDto = new AuditDTO();
        auditDto.setUserId(userId);
        auditDto.setAuditTime(new Date());
        auditDto.setAction("登录");
        auditDto.setResult(result);
        auditDto.setApplication("Rolmex-App");
        auditDto.setClient(NetworkUtil.getVisitIp(httpRequest));
        auditDto.setServer(InetAddress.getLocalHost().getHostAddress());
        auditDto.setTenantId("1");

		/*
		 * UserAgent userAgent =
		 * UserAgent.parseUserAgentString(httpRequest.getHeader("User-Agent"));
		 * deviceDto.setType(userAgent.getOperatingSystem().getDeviceType().
		 * toString());
		 * deviceDto.setOs(userAgent.getOperatingSystem().toString());
		 * deviceDto.setClient(userAgent.getBrowser().toString());
		 * 
		 * deviceDto.setVendor(vendor); deviceDto.setModel(model);
		 */

        auditConnector.log(auditDto);
    }

    @Resource
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Resource
    public void setAuthenticationHandler(AuthenticationHandler authenticationHandler) {
        this.authenticationHandler = authenticationHandler;
    }

    @Resource
    public void setWebAPI(WebAPI webAPI) {
        this.webAPI = webAPI;
    }

    @Resource
    public void setAccountCredentialManager(AccountCredentialManager accountCredentialManager) {
    }

    @Resource
    public void setAccountInfoManager(AccountInfoManager accountInfoManager) {
        this.accountInfoManager = accountInfoManager;
    }

    @Resource
    public void setAuditConnector(AuditConnector auditConnector) {
        this.auditConnector = auditConnector;
    }
    
    @Resource
	public void setDeviceConnector(DeviceConnector deviceConnector) {
		this.deviceConnector = deviceConnector;
	}
}
