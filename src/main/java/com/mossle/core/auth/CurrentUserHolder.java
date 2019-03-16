package com.mossle.core.auth;

import org.springframework.security.core.Authentication;

public interface CurrentUserHolder {
    String getUserId();

    String getUsername();
    
    String getName();
    
    /**
     * 判断用户是否拥有角色, 如果用户拥有参数中的任意一个角色则返回true.
     */
    boolean hasRole(String... roles);
    
    Authentication getAuthentication();
}
