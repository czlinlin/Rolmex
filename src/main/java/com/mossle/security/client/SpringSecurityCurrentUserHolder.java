package com.mossle.security.client;

import javax.annotation.Resource;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.mossle.api.user.UserConnector;
import com.mossle.api.user.UserDTO;
import com.mossle.core.auth.CurrentUserHolder;
import com.mossle.security.util.SpringSecurityUtils;

public class SpringSecurityCurrentUserHolder implements CurrentUserHolder {
	
	private UserConnector userConnector;
	
    public String getUserId() {
        return SpringSecurityUtils.getCurrentUserId();
    }

    public String getUsername() {
        return SpringSecurityUtils.getCurrentUsername();
    }
    
    public String getName() {
    	
    	UserDTO userDto = userConnector.findById(SpringSecurityUtils.getCurrentUserId());
    	
        return userDto.getDisplayName();
    }
    /**
     * 判断用户是否拥有角色, 如果用户拥有参数中的任意一个角色则返回true.
     */
    public boolean hasRole(String... roles) {
    	return SpringSecurityUtils.hasRole(roles);
    }
    
    public Authentication getAuthentication() {
    	return SpringSecurityUtils.getAuthentication();
    }


	@Resource
	public void setUserConnector(UserConnector userConnector) {
		this.userConnector = userConnector;
	}
    
    
}
