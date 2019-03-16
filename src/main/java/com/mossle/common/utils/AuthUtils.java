package com.mossle.common.utils;

import java.util.Map;


public class AuthUtils {

	private static final String PRIVATE_KEY = "com.rolmex.dw.ml";

	public static String generateSecondToken(Map<String,String> param) {
		if(param == null) return null;
		
		String login_time = String.valueOf(param.get("login_time"));
		String user_id = String.valueOf(param.get("user_id"));
		String randomCode = String.valueOf(param.get("randomCode"));
		
		return MD5Util.getMD5String(login_time+user_id+randomCode+PRIVATE_KEY);
	}
	
	@SuppressWarnings("unused")
	private static boolean validSecondToken(Map<String,String> param, String token) {
		if(token == null) return false;
		return token.equals(generateSecondToken(param));
	}
	
}
