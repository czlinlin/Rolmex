package com.mossle.api.user;

import java.net.UnknownHostException;

public interface AuthenticationHandler {
	boolean support(String type);

	boolean isPasswordTooSimple();

	String doAuthenticate(String username, String password, String application) throws UnknownHostException;

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
	String doAuthenticate(String username, String password, String application, StringBuilder outAccountStatus)
			throws UnknownHostException;
}
