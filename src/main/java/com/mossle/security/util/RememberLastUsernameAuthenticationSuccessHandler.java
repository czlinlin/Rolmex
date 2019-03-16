package com.mossle.security.util;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.UUID;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.mossle.api.tenant.TenantHolder;
import com.mossle.common.utils.MacUtils;
import com.mossle.common.utils.NetworkUtil;
import com.mossle.security.SecurityConstants;
import com.mossle.security.impl.SpringSecurityUserAuth;
import com.mossle.spi.device.DeviceConnector;
import com.mossle.spi.device.DeviceDTO;

import eu.bitwalker.useragentutils.UserAgent;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

public class RememberLastUsernameAuthenticationSuccessHandler extends
        SavedRequestAwareAuthenticationSuccessHandler {
    private TenantHolder tenantHolder;
    private DeviceConnector deviceConnector;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
            HttpServletResponse response, Authentication authentication)
            throws ServletException, IOException {
        this.handleTenant(response);
        this.handleUsername(response, authentication);
        this.handleDevice(request, response);
        super.onAuthenticationSuccess(request, response, authentication);
    }

    public void handleTenant(HttpServletResponse response) {
        String tenantCode = tenantHolder.getTenantCode();
        this.addCookie(response, SecurityConstants.SECURITY_LAST_TENANT,
                tenantCode);
    }

    public void handleUsername(HttpServletResponse response,
            Authentication authentication) {
        String username = this.getUsername(authentication);
        this.addCookie(response, SecurityConstants.SECURITY_LAST_USERNAME,
                username);
    }

    public void handleDevice(HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        String deviceId = getCookie(request, "SECURITY_DEVICE_ID");

        if (deviceId == null) {
            deviceId = UUID.randomUUID().toString();
            this.addCookie(response, "SECURITY_DEVICE_ID", deviceId,
                    3600 * 24 * 365 * 100);
        }

        DeviceDTO deviceDto = deviceConnector.findDevice(deviceId);

        if (deviceDto == null) {
            deviceDto = new DeviceDTO();
            deviceDto.setCode(deviceId);
            
            //String ip = NetworkUtil.getIpAddress(request);
            //String ip1 = NetworkUtil.getIp(request);
            // String mac = MacUtils.getMac();
            
    		// InetAddress ia = InetAddress.getLocalHost();

    		// System.out.println(ia);

    		// getLocalMac(ia);

    		
            UserAgent userAgent = UserAgent.parseUserAgentString(request
                    .getHeader("User-Agent"));
            deviceDto.setType(userAgent.getOperatingSystem().getDeviceType()
                    .toString());
            deviceDto.setOs(userAgent.getOperatingSystem().toString());
            deviceDto.setClient(userAgent.getBrowser().toString());
            
        }

        deviceConnector.saveDevice(deviceDto);
    }

	private static void getLocalMac(InetAddress ia) throws SocketException {

		// TODO Auto-generated method stub

		//获取网卡，获取地址

		byte[] mac = NetworkInterface.getByInetAddress(ia).getHardwareAddress();

		System.out.println("mac数组长度："+mac.length);

		StringBuffer sb = new StringBuffer("");

		for(int i=0; i<mac.length; i++) {

			if(i!=0) {

				sb.append("-");

			}

			//字节转换为整数

			int temp = mac[i]&0xff;

			String str = Integer.toHexString(temp);

			System.out.println("每8位:"+str);

			if(str.length()==1) {

				sb.append("0"+str);

			}else {

				sb.append(str);

			}

		}

		System.out.println("本机MAC地址:"+sb.toString().toUpperCase());

	}

	
    public void addCookie(HttpServletResponse response, String key, String value) {
        this.addCookie(response, key, value, 3600 * 24 * 30);
    }

    public void addCookie(HttpServletResponse response, String key,
            String value, int maxAge) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }

    public String getCookie(HttpServletRequest request, String key) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if ((cookie == null) || (cookie.getName() == null)) {
                continue;
            }

            if (cookie.getName().equals(key)) {
                return cookie.getValue();
            }
        }

        return null;
    }

    public String getUsername(Authentication authentication) {
        if (authentication == null) {
            return "";
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof SpringSecurityUserAuth) {
            return ((SpringSecurityUserAuth) principal).getUsername();
        } else {
            return authentication.getName();
        }
    }

 
    @Resource
    public void setTenantHolder(TenantHolder tenantHolder) {
        this.tenantHolder = tenantHolder;
    }

    @Resource
    public void setDeviceConnector(DeviceConnector deviceConnector) {
        this.deviceConnector = deviceConnector;
    }
}
