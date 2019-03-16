package com.mossle.api.menu;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.mossle.auth.persistence.domain.Menu;

public interface MenuConnector {
    List<MenuDTO> findMenus(String systemCode, String userId);
    
    List<MenuDTO> findMenusByCode(String systemCode, String userId);

    List<MenuDTO> findSystemMenus(String userId);

    Menu findMenuByUrl(String url);

    Menu findMenuByUrl(String url, String type);
    
    MenuDTO findMenuDtoByUrl(String url);

    Menu findMenuById(Long id);
    
    MenuDTO findMenuDtoById(Long id);
    
    List<Menu> findChildMenus(Menu parentMenu, boolean auth);
    
    List<MenuDTO> findChildMenuDtos(Menu parentMenu);
    List<MenuDTO> findChildMenuDtos(Menu parentMenu, boolean excludeModule);
    
    /**
     * 获取操作级菜单 add by lilei at 2018-05-17
     * **/
    List<Menu> findOpterationMenus(String url);
    
    /**
     * add by lilei at 2018-05-29
     * **/
    MenuDTO findMenuDtoByTypeAndCode(String type,String code,Long accountId);
    
    /**
	 * 验证操作级菜单 add by lilei at 2019-01-02
	 * @throws UnsupportedEncodingException 
	 * **/
	Map<String,Object> checkOperationMenuByName(HttpServletRequest request,String type,String action) throws UnsupportedEncodingException;

	/**
	 * 验证工资操作级菜单 add by ckx at 2019-01-03
	 * 
	 * **/
	Map<String, Object> checkMenuByName(String string, String string2) throws Exception ;
}
