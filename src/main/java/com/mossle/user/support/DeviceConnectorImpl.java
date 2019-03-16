package com.mossle.user.support;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Resource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import ch.qos.logback.core.net.LoginAuthenticator;

import com.mossle.api.tenant.TenantHolder;
import com.mossle.common.utils.AuthUtils;
import com.mossle.common.utils.DateUtils;
import com.mossle.core.auth.CurrentUserHolder;
import com.mossle.spi.device.DeviceConnector;
import com.mossle.spi.device.DeviceDTO;
import com.mossle.user.persistence.domain.AccountDevice;
import com.mossle.user.persistence.domain.AccountInfo;
import com.mossle.user.persistence.domain.AccountToken;
import com.mossle.user.persistence.manager.AccountDeviceManager;
import com.mossle.user.persistence.manager.AccountInfoManager;
import com.mossle.user.persistence.manager.AccountTokenManager;
import com.sun.istack.logging.Logger;

public class DeviceConnectorImpl implements DeviceConnector {
	//private logg
	private AccountDeviceManager accountDeviceManager;
	private AccountInfoManager accountInfoManager;
	private CurrentUserHolder currentUserHolder;
	private TenantHolder tenantHolder;
	private JdbcTemplate jdbcTemplate;
	private AccountTokenManager accountTokenManager;
	
	public DeviceDTO findDevice(String code) {
		if (code == null) {
			return null;
		}

		AccountDevice accountDevice = accountDeviceManager.findUniqueBy("code", code);

		if (accountDevice == null) {
			return null;
		}

		DeviceDTO deviceDto = new DeviceDTO();
		deviceDto.setCode(accountDevice.getCode());
		deviceDto.setType(accountDevice.getType());
		deviceDto.setOs(accountDevice.getOs());
		deviceDto.setClient(accountDevice.getClient());
		deviceDto.setStatus(accountDevice.getStatus());

		return deviceDto;
	}

	public void saveDevice(DeviceDTO deviceDto) {
		if (deviceDto == null) {
			return;
		}

		if (deviceDto.getCode() == null) {
			return;
		}

		AccountDevice accountDevice = accountDeviceManager.findUniqueBy("code", deviceDto.getCode());
		Date now = new Date();

		if (accountDevice == null) {
			accountDevice = new AccountDevice();
			accountDevice.setCode(deviceDto.getCode());
			accountDevice.setType(deviceDto.getType());
			accountDevice.setOs(deviceDto.getOs());
			accountDevice.setClient(deviceDto.getClient());
			accountDevice.setCreateTime(now);
			accountDevice.setLastLoginTime(now);
			accountDevice.setStatus("new");
			accountDevice.setTenantId(tenantHolder.getTenantId());

			AccountInfo accountInfo = accountInfoManager.get(Long.parseLong(currentUserHolder.getUserId()));
			accountDevice.setAccountInfo(accountInfo);
		} else {
			accountDevice.setLastLoginTime(now);
		}

		accountDeviceManager.save(accountDevice);
	}
	
	
	@Transactional
	public String saveDevice(DeviceDTO deviceDto, String userId) {
		if (deviceDto == null) {
			return "";
		}

		if (deviceDto.getCode() == null) {
			return "";
		}

		AccountDevice accountDevice = accountDeviceManager.findUniqueBy("code", deviceDto.getCode());
		Date now = new Date();
		if (accountDevice == null) {
			accountDevice = new AccountDevice();
			accountDevice.setCode(deviceDto.getCode());
			accountDevice.setStatus("new");
			accountDevice.setTenantId(tenantHolder.getTenantId());
		}

		AccountInfo accountInfo = accountInfoManager.get(Long.parseLong(userId));
		accountDevice.setAccountInfo(accountInfo);
		accountDevice.setType(deviceDto.getType());
		accountDevice.setOs(deviceDto.getOs());
		accountDevice.setClient(deviceDto.getClient());
		accountDevice.setVendor(deviceDto.getVendor());
		accountDevice.setModel(deviceDto.getModel());
		accountDevice.setLastLoginTime(now);
		accountDevice.setCreateTime(now);
		accountDevice.setAttribute1(deviceDto.getPlat());// 平台：1=ios，2=其他（如android）。Bing2018.2.8
		accountDevice.setPushType(deviceDto.getPushType());
		
		/*// 生成产生Token的随机值
		String randomCode ="";
		String sql = "SELECT id,UUID,generate_date FROM Token_randomCode WHERE id =1 AND generate_date = ?";
		List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, new Object[] { DateUtils.getDate() });
		if (list == null || list.size() == 0) {
			randomCode = UUID.randomUUID().toString();
			String updateSql = "update Token_randomCode set UUID=?,generate_date=? where id=?";
			jdbcTemplate.update(updateSql, randomCode , DateUtils.getDate(), 1);
		} else {
			Map<String, Object> map = list.get(0);
			randomCode = getStringValue(map, "UUID");
		}
					
		param.put("login_time", DateUtils.formatDateTime(now));
		param.put("randomCode", randomCode);
		param.put("clientID", deviceDto.getCode());
		param.put("user_id", Long.toString(accountInfo.getId()));
		

		accountDevice.setToken(AuthUtils.generateSecondToken(param));*/
		
		accountDeviceManager.save(accountDevice);
		
		return accountDevice.getToken();
	}
	
	/**
	 * 成功登录APP后，返回一个token验证值 add by lieli at 2018-02-28
	 * **/
	@Transactional(readOnly=false)
	public String getToeknValue(String userId)
	{
		try {
			Date now = new Date();
			Map<String,String> param = new HashMap<String, String>();
			// 生成产生Token的随机值
			String randomCode ="";
			String sql = "SELECT id,UUID,generate_date FROM Token_randomCode WHERE id =1 AND generate_date = ?";
			List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, new Object[] { DateUtils.getDate() });
			if (list == null || list.size() == 0) {
				randomCode = UUID.randomUUID().toString();
				String updateSql = "update Token_randomCode set UUID=?,generate_date=? where id=?";
				jdbcTemplate.update(updateSql, randomCode , DateUtils.getDate(), 1);
			} else {
				Map<String, Object> map = list.get(0);
				randomCode = getStringValue(map, "UUID");
			}
						
			param.put("login_time", DateUtils.formatDateTime(now));
			param.put("randomCode", randomCode);
			param.put("user_id", userId);
			
			
			Long userIdLong=Long.parseLong(userId);
			String strTokenSql = "select * from account_token where userid="+userIdLong;
			List<Map<String, Object>> mapTokenList=jdbcTemplate.queryForList(strTokenSql);
			String token=AuthUtils.generateSecondToken(param);
			if(mapTokenList==null||mapTokenList.size()<1){
				strTokenSql="insert into account_token(userid,token,updateDate) values(?,?,?)";
				jdbcTemplate.update(strTokenSql, userId ,token , DateUtils.formatDateTime(now));
			}
			else {
				strTokenSql="update account_token set token=?,updateDate=? where userid=?";
				jdbcTemplate.update(strTokenSql ,token , DateUtils.formatDateTime(now),userId);
			}
			/*AccountToken accountToken=accountTokenManager.findUniqueBy("userId", userIdLong);
			if(accountToken==null){
				accountToken=new AccountToken();
				accountToken.setUserId(userIdLong);
				accountToken.setUpdateDate(now);				
			}
			accountToken.setToken(AuthUtils.generateSecondToken(param));
			accountTokenManager.save(accountToken);*/
			
			return token;
		} catch (Exception e) {
			System.out.print("生成token异常，userid:"+userId+"\r\n"+e.getMessage()+"\r\n"+e.getStackTrace());;
			return "";
		}
		
	}

	/**
	 * 获得string值.
	 */
	private String getStringValue(Map<String, Object> map, String name) {
		Object value = map.get(name);

		if (value == null) {
			return null;
		}

		if (value instanceof String) {
			return (String) value;
		}

		return value.toString();
	}
	
	/**
	 * 删除该用户的所有移动设备信息 Bing 2018.2.7
	 * 
	 * @param account_id
	 */
	public void removeAll(String account_id) {
		String hql = "from AccountDevice where type='MOBILE' and accountInfo.id=?";
		List<AccountDevice> listAccountDevice = accountDeviceManager.find(hql, Long.parseLong(account_id));
		accountDeviceManager.removeAll(listAccountDevice);
	}

	@Resource
	public void setAccountDeviceManager(AccountDeviceManager accountDeviceManager) {
		this.accountDeviceManager = accountDeviceManager;
	}

	@Resource
	public void setAccountInfoManager(AccountInfoManager accountInfoManager) {
		this.accountInfoManager = accountInfoManager;
	}

	@Resource
	public void setCurrentUserHolder(CurrentUserHolder currentUserHolder) {
		this.currentUserHolder = currentUserHolder;
	}

	@Resource
	public void setTenantHolder(TenantHolder tenantHolder) {
		this.tenantHolder = tenantHolder;
	}
	
	@Resource
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
	
	@Resource
	public void setAccountTokenManager(AccountTokenManager accountTokenManager) {
		this.accountTokenManager = accountTokenManager;
	}
}
