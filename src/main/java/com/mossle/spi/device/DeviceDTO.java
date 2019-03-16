package com.mossle.spi.device;

public class DeviceDTO {
	private String code;// device token 用于标识设备的id
	private String type;
	private String os;
	private String client;
	private String status;
	private String vendor;// 设备的生产厂商
	private String model;// 设备的型号
	private String userId;
	private String token;
	private String UUID;
	private String pushType;
	
	private String plat;// 1=ios，2=其他（如android）

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getOs() {
		return os;
	}

	public void setOs(String os) {
		this.os = os;
	}

	public String getClient() {
		return client;
	}

	public void setClient(String client) {
		this.client = client;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * @return vendor
	 */
	public String getVendor() {
		return vendor;
	}

	/**
	 * @param vendor
	 *            要设置的 vendor
	 */
	public void setVendor(String vendor) {
		this.vendor = vendor;
	}

	/**
	 * @return model
	 */
	public String getModel() {
		return model;
	}

	/**
	 * @param model
	 *            要设置的 model
	 */
	public void setModel(String model) {
		this.model = model;
	}

	/**
	 * @return userId
	 */
	public String getUserId() {
		return userId;
	}

	/**
	 * @param userId
	 *            要设置的 userId
	 */
	public void setUserId(String userId) {
		this.userId = userId;
	}


	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getUUID() {
		return UUID;
	}

	public void setUUID(String uUID) {
		UUID = uUID;
	}

	
	/**
	 * @return plat
	 */
	public String getPlat() {
		return plat;
	}

	/**
	 * @param plat
	 *            要设置的 plat
	 */
	public void setPlat(String plat) {
		this.plat = plat;
	}

	/**
	 * 推送，0：华为；1：小米
	 * **/
	public String getPushType() {
		return pushType;
	}

	public void setPushType(String pushType) {
		this.pushType = pushType;
	}
	
}
