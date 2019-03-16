package com.mossle.core.spring;

import java.util.Properties;

public class SignInfo{
	//region 留给直销OA流程的key
	private String signKey;
	private String paramKey;
	//endregion
	
	//region 调用发送短信webservice的key
	private String messageRequestUrl;
	private String messageCooperKey;
	private String messageCooperName;
	//endregion
	
	//region 申请旗舰店调结束后，调用直销OA的key
	public String oaApplySignMsg;
	public String oaApplyUrl;
	//endregion
	
	//region 推送APP消息的配置文件
	public String appPushUrl;
	public String appPushKey;
	public String appPushUserType;
	//endregion
	
	//region 调用股票权益平台系统配置文件
	public String stockPlatUrl;
	//endregion
	
	public String getSignKey() {
		return signKey;
	}

	public void setSignKey(String signKey) {
		this.signKey = signKey;
	}
	
	public String getParamKey() {
		return paramKey;
	}

	public void setParamKey(String paramKey) {
		this.paramKey = paramKey;
	}

	public String getMessageCooperKey() {
		return messageCooperKey;
	}

	public String getOaApplySignMsg() {
		return oaApplySignMsg;
	}

	public void setOaApplySignMsg(String oaApplySignMsg) {
		this.oaApplySignMsg = oaApplySignMsg;
	}

	public String getOaApplyUrl() {
		return oaApplyUrl;
	}

	public void setOaApplyUrl(String oaApplyUrl) {
		this.oaApplyUrl = oaApplyUrl;
	}

	//region 发送短信的url和key
	public String getMessageRequestUrl() {
		return messageRequestUrl;
	}

	public void setMessageRequestUrl(String messageRequestUrl) {
		this.messageRequestUrl = messageRequestUrl;
	}
	
	public void setMessageCooperKey(String messageCooperKey) {
		this.messageCooperKey = messageCooperKey;
	}

	public String getMessageCooperName() {
		return messageCooperName;
	}

	public void setMessageCooperName(String messageCooperName) {
		this.messageCooperName = messageCooperName;
	}
	//endregion

	//region 推送APP消息
	public String getAppPushUrl() {
		return appPushUrl;
	}

	public void setAppPushUrl(String appPushUrl) {
		this.appPushUrl = appPushUrl;
	}

	public String getAppPushKey() {
		return appPushKey;
	}

	public void setAppPushKey(String appPushKey) {
		this.appPushKey = appPushKey;
	}

	public String getAppPushUserType() {
		return appPushUserType;
	}

	public void setAppPushUserType(String appPushUserType) {
		this.appPushUserType = appPushUserType;
	}
	//endregion

	//region 调用股票权益系统的
	public String getStockPlatUrl() {
		return stockPlatUrl;
	}

	public void setStockPlatUrl(String stockPlatUrl) {
		this.stockPlatUrl = stockPlatUrl;
	}
	//endregion
}