/**
 * 设备
 */
package com.mossle.H5.device;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import ch.qos.logback.classic.Logger;

import com.mossle.api.msg.MsgPushConnector;
import com.mossle.common.utils.WebAPI;
import com.mossle.core.util.StringUtils;
import com.mossle.spi.device.DeviceConnector;
import com.mossle.spi.device.DeviceDTO;
import com.mossle.user.persistence.domain.AccountToken;
import com.mossle.user.persistence.manager.AccountTokenManager;

import eu.bitwalker.useragentutils.UserAgent;

/**
 * @author Bing
 *
 */
@Service
public class DeviceService {
	private JdbcTemplate jdbcTemplate;
	private WebAPI webAPI;
	private DeviceConnector deviceConnector;
	private AccountTokenManager accountTokenManager;
	private MsgPushConnector msgPushConnector;

	public Map<String, Object> VersionUpdate(Map<String, Object> decryptedMap) {
		// {percode=, method=VersionUpdate,
		// sign=222c49da7a0341ab697b189d573c9800, CH02=1.16, timestamp=}

		Map<String, Object> returnMap = new HashMap<String, Object>();

		try {
			// 验证参数================================================================================
			if (!decryptedMap.containsKey("CH02")) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "参数错误");
				return returnMap;
			}

			// 获取参数================================================
			String version_code = decryptedMap.get("CH02").toString();

			// 查数据=========================================================
			// 1=apk------------------------------------------------------------------------------------------------------
			String sql = "SELECT * FROM `v_h5_version_info` WHERE CH07=1 AND version_code>? LIMIT 1";
			List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, new Object[] { version_code });
			if (list.size() > 0) {
				returnMap.put("bSuccess", "true");
				returnMap.put("strMsg", "加载成功");
				returnMap.put("OAUploadUrl", webAPI.getDownloadUrl());
				returnMap.put("Version", list);
				return returnMap;
			}

			// 0=wgt------------------------------------------------------------------------------------------------
			sql = "SELECT * FROM `v_h5_version_info` WHERE CH07=0 AND version_code>? LIMIT 1";
			list = jdbcTemplate.queryForList(sql, new Object[] { version_code });
			if (list.size() > 0) {
				returnMap.put("bSuccess", "true");
				returnMap.put("strMsg", "加载成功");
				returnMap.put("OAUploadUrl", webAPI.getDownloadUrl());
				returnMap.put("Version", list);
				return returnMap;
			}

		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("bSuccess", "false");
			returnMap.put("strMsg", "加载错误，请联系管理员");
		}

		returnMap.put("bSuccess", "true");
		returnMap.put("strMsg", "暂无数据");
		return returnMap;
	}

	public Map<String, Object> H5ClientID(Map<String, Object> decryptedMap, HttpServletRequest httpRequest) {
		// {strPerCode=, percode=, method=H5ClientID,
		// sign=da73d48811c9bf9c735938ddc1f17de3,
		// strClientID=6e49b1702fc2200e11e945efcfbd6e42, strPlat=2,
		// timestamp=}

		Map<String, Object> returnMap = new HashMap<String, Object>();

		try {
			// 验证参数================================================================================
			if (!decryptedMap.containsKey("strClientID")) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "参数错误");
				return returnMap;
			}

			// 获取参数================================================
			String strPerCode = decryptedMap.get("strPerCode").toString();
			String strClientID = decryptedMap.get("strClientID").toString();

			String vendor = "";
			if (decryptedMap.containsKey("vendor")) {
				vendor = decryptedMap.get("vendor").toString();
			}

			String model = "";
			if (decryptedMap.containsKey("model")) {
				model = decryptedMap.get("model").toString();
			}

			// 平台：1=ios，2=其他（如android）
			String strPlat = "";
			if (decryptedMap.containsKey("strPlat")) {
				strPlat = decryptedMap.get("strPlat").toString();
			}
			
			/**
			 * 推送渠道
			 * 0：华为；1：小米
			 * **/
			String strPushType="1";
			if (decryptedMap.containsKey("strPushType")) {
				if(!decryptedMap.get("strPushType").toString().equals(""))
					strPushType = decryptedMap.get("strPushType").toString();
			}

			deviceConnector.removeAll(strPerCode);// 先删除该用户所有的移动设备信息 Bing 2018.2.7

			DeviceDTO deviceDto = deviceConnector.findDevice(strClientID);
			if (deviceDto == null) {
				deviceDto = new DeviceDTO();
				deviceDto.setCode(strClientID);
			}

			UserAgent userAgent = UserAgent.parseUserAgentString(httpRequest.getHeader("User-Agent"));
			deviceDto.setType(userAgent.getOperatingSystem().getDeviceType().toString());
			deviceDto.setOs(userAgent.getOperatingSystem().toString());
			deviceDto.setClient(userAgent.getBrowser().toString());

			deviceDto.setVendor(vendor);
			deviceDto.setModel(model);
			deviceDto.setPlat(strPlat);
			deviceDto.setPushType(strPushType);
			
			deviceConnector.saveDevice(deviceDto, strPerCode);
			
			//如果设备唯一ID为空，则不调接口
			if(!StringUtils.isBlank(strClientID)){
				//推送关联设备，1：登录调用，2：退出调用
				Map<String,Object> map=new HashMap<String, Object>();
				map.put("user_id", strPerCode);
				map.put("identification", deviceDto.getCode());
				map.put("to_project", "0");//麦联项目
				map.put("system", (strPlat.equals("1")?"1":"0"));
				map.put("channel", deviceDto.getPushType());
				msgPushConnector.relationPhoneToekn(map);
			}
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("token", "");
			returnMap.put("bSuccess", "false");
			returnMap.put("strMsg", "异常");
			return returnMap;
		}

		returnMap.put("bSuccess", "true");
		returnMap.put("strMsg", "正常");
		return returnMap;
	}
	
	/*
	 * 每次请求验证token add by lilei at 2018-02-28
	 * */
	public boolean validAPPToken(String userId,String token){
		try {
			if(userId==null||userId.equals("")||token==null) 
				return false;
			String strTokenSql = "select * from account_token where userid="+userId;
			List<Map<String, Object>> mapTokenList=jdbcTemplate.queryForList(strTokenSql);
			
			//AccountToken accountToken=accountTokenManager.findUniqueBy("userId", Long.parseLong(userId));
			if(mapTokenList==null||mapTokenList.size()<1)
				return false;
			return token.equals(mapTokenList.get(0).get("token"));
		} catch (Exception ex) {
			System.out.print("验证token异常，userid:"+userId+ex.getMessage()+"\r\n"+ex.getStackTrace());
			return false;
		}
	}

	
	public Map<String, Object> queryForList(Map<String, Object> decryptedMap) {
		String sql = decryptedMap.get("sql").toString();
		List<Map<String, Object>> queryForList = jdbcTemplate.queryForList(sql);

		Map<String, Object> returnMap = new HashMap<String, Object>();
		returnMap.put("queryForList", queryForList);
		return returnMap;
	}

	@Resource
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Resource
	public void setWebAPI(WebAPI webAPI) {
		this.webAPI = webAPI;
	}

	@Resource
	public void setDeviceConnector(DeviceConnector deviceConnector) {
		this.deviceConnector = deviceConnector;
	}
	
	@Resource
	public void setAccountTokenManager(AccountTokenManager accountTokenManager) {
		this.accountTokenManager = accountTokenManager;
	}
	
	@Resource
	public void setMsgPushConnector(MsgPushConnector msgPushConnector) {
		this.msgPushConnector = msgPushConnector;
	}
}
