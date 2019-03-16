package com.mossle.user.authenticate;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

import javax.annotation.Resource;

import com.mossle.api.audit.AuditConnector;
import com.mossle.api.audit.AuditDTO;
import com.mossle.api.user.AccountStatus;
import com.mossle.api.user.AuthenticationHandler;
import com.mossle.api.user.AuthenticationType;
import com.mossle.core.auth.CustomPasswordEncoder;
import com.mossle.user.PersonInfoConstants;
import com.mossle.user.persistence.domain.AccountCredential;
import com.mossle.user.persistence.domain.AccountInfo;
import com.mossle.user.persistence.manager.AccountCredentialManager;
import com.mossle.user.persistence.manager.AccountInfoManager;
import com.mossle.user.service.AccountLockService;

import eu.bitwalker.useragentutils.UserAgent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Bing
 *
 */
/**
 * @author Bing
 *
 */
/**
 * @author Bing
 *
 */
public class NormalAuthenticationHandler implements AuthenticationHandler {
	private static Logger logger = LoggerFactory.getLogger(NormalAuthenticationHandler.class);
	private AccountInfoManager accountInfoManager;
	private AccountCredentialManager accountCredentialManager;
	private CustomPasswordEncoder customPasswordEncoder;
	private AccountLockService accountLockService;
	private boolean isPasswordTooSimple = false;
	private AuditConnector auditConnector;

	public boolean support(String type) {
		return AuthenticationType.NORMAL.equals(type);
	}

	public String doAuthenticate(String username, String password, String application) throws UnknownHostException {
		if (username == null) {
			logger.info("username cannot be null");

			return AccountStatus.ACCOUNT_NOT_EXISTS;
		}

		username = username.toLowerCase();

		String hqlai = "from AccountInfo where username=? and delFlag = '0'";
		AccountInfo accountInfo = accountInfoManager.findUnique(hqlai, username);// .findUniqueBy("username",username);

		if (accountInfo == null) {
			return AccountStatus.ACCOUNT_NOT_EXISTS;
		}

		// zyl 2017-08-14 用户被删除
		// if
		// (accountInfo.getDelFlag().equals(PersonInfoConstants.DELETE_FLAG_YES))
		// {
		// return AccountStatus.ACCOUNT_NOT_EXISTS;
		// }

		// 用户被锁定 Bing 2017.10.26
		if (accountInfo.getLocked().equals(AccountStatus.LOCKED)) {
			return AccountStatus.LOCKED;
		}

		String hql = "from AccountCredential where accountInfo=? and catalog='default'";
		AccountCredential accountCredential = accountCredentialManager.findUnique(hql, accountInfo);

		if (accountCredential == null) {
			return AccountStatus.PASSWORD_NOT_EXISTS;
		}

		setPasswordTooSimple(accountCredential);// 设置，密码是否太简单。

		// 密码类型 0密码
		if (application.equals("0")) {
			if (customPasswordEncoder.matches(password, accountCredential.getPassword())) {
				accountCredential.setFailedPasswordCount(0);
				accountCredentialManager.save(accountCredential);
				return AccountStatus.SUCCESS;
			} else {
				return failedPassword(accountCredential, username, application);
				// return AccountStatus.BAD_CREDENTIALS;
			}
		} else {// 密码类型 1图案
			if (accountCredential.getGestureSwitch() == null) {
				return AccountStatus.Gesture_NOT_EXISTS;
			}
			if (!accountCredential.getGestureSwitch().equals("open")) {
				return AccountStatus.Gesture_NOT_Open;
			}
			if (accountCredential.getGesturePassword() == null) {
				return AccountStatus.PASSWORD_NOT_EXISTS;
			}
			if (accountCredential.getGesturePassword().equals(password)) {
				accountCredential.setFailedPasswordCount(0);
				accountCredentialManager.save(accountCredential);
				return AccountStatus.SUCCESS;
			} else {
				return failedPassword(accountCredential, username, application);
				// return AccountStatus.BAD_CREDENTIALS;
			}
		}
	}

	/**
	 * 登录验证 Bing 2018.2.11
	 * 
	 * @param username
	 * @param password
	 * @param application
	 *            0密码1图案
	 * @param outAccountStatus
	 *            参数返回值
	 * @return
	 * @throws UnknownHostException
	 */
	public String doAuthenticate(String username, String password, String application, StringBuilder outAccountStatus)
			throws UnknownHostException {
		if (username == null) {
			logger.info("username cannot be null");

			return AccountStatus.ACCOUNT_NOT_EXISTS;
		}

		username = username.toLowerCase();

		String hqlai = "from AccountInfo where username=? and delFlag = '0'";
		AccountInfo accountInfo = accountInfoManager.findUnique(hqlai, username);// .findUniqueBy("username",username);

		if (accountInfo == null) {
			return AccountStatus.ACCOUNT_NOT_EXISTS;
		}

		// 用户被锁定 Bing 2017.10.26
		if (accountInfo.getLocked().equals(AccountStatus.LOCKED)) {
			return AccountStatus.LOCKED;
		}

		String hql = "from AccountCredential where accountInfo=? and catalog='default'";
		AccountCredential accountCredential = accountCredentialManager.findUnique(hql, accountInfo);

		if (accountCredential == null) {
			return AccountStatus.PASSWORD_NOT_EXISTS;
		}

		// 设置参数返回，密码太简单。Bing 2018.2.11
		if (accountCredential.getPassword().equals(customPasswordEncoder.encode("111111"))
				|| accountCredential.getPassword().equals(customPasswordEncoder.encode("123456"))) {
//			outAccountStatus = new StringBuilder();
			outAccountStatus.append(AccountStatus.OTP_PASSWORD_TOO_SIMPLE);
		}
		System.out.println(outAccountStatus);

		// 密码类型 0密码
		if (application.equals("0")) {
			if (customPasswordEncoder.matches(password, accountCredential.getPassword())) {
				accountCredential.setFailedPasswordCount(0);
				accountCredentialManager.save(accountCredential);
				return AccountStatus.SUCCESS;
			} else {
				return failedPassword(accountCredential, username, application);
				// return AccountStatus.BAD_CREDENTIALS;
			}
		} else {// 密码类型 1图案
			if (accountCredential.getGestureSwitch() == null) {
				return AccountStatus.Gesture_NOT_EXISTS;
			}
			if (!accountCredential.getGestureSwitch().equals("open")) {
				return AccountStatus.Gesture_NOT_Open;
			}
			if (accountCredential.getGesturePassword() == null) {
				return AccountStatus.PASSWORD_NOT_EXISTS;
			}
			if (accountCredential.getGesturePassword().equals(password)) {
				accountCredential.setFailedPasswordCount(0);
				accountCredentialManager.save(accountCredential);
				return AccountStatus.SUCCESS;
			} else {
				return failedPassword(accountCredential, username, application);
				// return AccountStatus.BAD_CREDENTIALS;
			}
		}
	}

	/**
	 * @return isPasswordTooSimple
	 */
	public boolean isPasswordTooSimple() {
		return isPasswordTooSimple;
	}

	/**
	 * 密码失败次数增加，达到次数锁定。Bing 2017.10.26
	 * 
	 * @param accountCredential
	 */
	String failedPassword(AccountCredential accountCredential, String username, String application) {
		int failedPasswordAttemptCount = 3;// 可以错几次

		int c = accountCredential.getFailedPasswordCount();// 已经错了几次

		// 累加保存
		c++;
		accountCredential.setFailedPasswordCount(c);
		accountCredentialManager.save(accountCredential);

		// 锁定
		if (c >= failedPasswordAttemptCount) {
			if(!username.equals("admin")) {
				AccountInfo accountInfo = accountCredential.getAccountInfo();
				accountInfo.setLocked("locked");
				accountInfo.setLockTime(new Date());
				accountInfoManager.save(accountInfo);
			}
			accountLockService.addLockLog(username, application, new Date());
		}

		// 返回
		switch (failedPasswordAttemptCount - c) {
		case 2:
			return AccountStatus.BAD_CREDENTIALS_Attempt2;
		case 1:
			return AccountStatus.BAD_CREDENTIALS_Attempt1;
		default:
			return AccountStatus.BAD_CREDENTIALS_LOCKED;
		}
	}

	/**
	 * 设置，密码是否太简单。
	 * 
	 * @param accountCredential
	 */
	void setPasswordTooSimple(AccountCredential accountCredential) {
		if (accountCredential.getPassword().equals(customPasswordEncoder.encode("111111")))
			isPasswordTooSimple = true;

		if (accountCredential.getPassword().equals(customPasswordEncoder.encode("123456")))
			isPasswordTooSimple = true;
	}

	// @Resource =======================================================
	@Resource
	public void setAccountInfoManager(AccountInfoManager accountInfoManager) {
		this.accountInfoManager = accountInfoManager;
	}

	@Resource
	public void setAuditConnector(AuditConnector auditConnector) {
		this.auditConnector = auditConnector;
	}

	@Resource
	public void setAccountCredentialManager(AccountCredentialManager accountCredentialManager) {
		this.accountCredentialManager = accountCredentialManager;
	}

	@Resource
	public void setCustomPasswordEncoder(CustomPasswordEncoder customPasswordEncoder) {
		this.customPasswordEncoder = customPasswordEncoder;
	}

	@Resource
	public void setAccountLockService(AccountLockService accountLockService) {
		this.accountLockService = accountLockService;
	}

}
