package com.mossle.auth.support;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import com.google.common.base.Joiner;
import com.mossle.api.humantask.HumanTaskConnector;
import com.mossle.api.menu.MenuConnector;
import com.mossle.api.menu.MenuDTO;
import com.mossle.api.tenant.TenantHolder;
import com.mossle.api.userauth.UserAuthConnector;
import com.mossle.auth.persistence.domain.Menu;
import com.mossle.auth.persistence.manager.MenuManager;
import com.mossle.core.auth.CurrentUserHolder;
import com.mossle.core.util.StringUtils;
import com.mossle.party.PartyConstants;
import com.mossle.party.service.PartyService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 用来通过用户权限来动态生成菜单.
 */
public class MenuConnectorImpl implements MenuConnector {
	private static Logger logger = LoggerFactory.getLogger(MenuConnectorImpl.class);
	private MenuManager menuManager;
	private UserAuthConnector userAuthConnector;
	private TenantHolder tenantHolder;
	private MenuCache menuCache;
	private CurrentUserHolder currentUserHolder;
	private JdbcTemplate jdbcTemplate;
	private PartyService partyService;
	@Autowired
	private HumanTaskConnector humanTaskConnector;
	
	/**
	 * systemCode算子系统的标识，比如个人事务子系统，账号子系统.
	 */
	public List<MenuDTO> findMenus(String systemCode, String userId) {
		String tenantId = tenantHolder.getTenantId();
		List<MenuDTO> menuDtos = this.menuCache.findByCode(systemCode);

		if (menuDtos == null) {
			String hql = "from Menu where menu.display='true' and menu.type='system' and menu.code=? order by priority";
			List<Menu> menus = menuManager.find(hql, systemCode);
			menuDtos = this.convertMenuDtos(menus, false);
			this.menuCache.updateByCode(systemCode, menuDtos);
		}
		
		List<String> permissions = userAuthConnector.findById(userId, tenantId).getPermissions();

		return this.filterMenuDtos(menuDtos, permissions, false, userId);
	}
	
	/**
	 * 根据 systemCode算子系统的标识，比如个人事务子系统，账号子系统.
	 */
	public List<MenuDTO> findMenusByCode(String systemCode, String userId) {
		String tenantId = tenantHolder.getTenantId();
		List<MenuDTO> menuDtos = this.menuCache.findByCode(systemCode);
		
		//add by lilei at 2018.05.22
		String sqlFindPermissions = "SELECT P.id AS PERMISSION"
	            + " FROM AUTH_USER_STATUS US,AUTH_USER_ROLE UR,AUTH_ROLE R,AUTH_PERM_ROLE_DEF PR,auth_menu P"
	            + " WHERE US.ID=UR.USER_STATUS_ID AND UR.ROLE_ID=R.ID AND R.ROLE_DEF_ID=PR.ROLE_DEF_ID AND PR.PERM_ID=P.ID"
	            + " AND US.DEL_FLAG = '0' AND US.REF=? AND US.TENANT_ID=?";
		
		List<String> permissions=jdbcTemplate.queryForList(sqlFindPermissions, String.class, userId,tenantId);
		
		String strPermission=Joiner.on(",").join(permissions);

		if (menuDtos == null) {
			String hql = "from Menu where menu.display='true' and menu.type='system' and menu.code=? and id in(?)  order by priority";
			List<Menu> menus = menuManager.find(hql, systemCode,strPermission);
			menuDtos = this.convertMenuDtos(menus, false);
			this.menuCache.updateByCode(systemCode, menuDtos);
		}
		
		

		/*List<String> permissions = userAuthConnector.findById(userId, tenantId).getPermissions();*/
		
		//return this.filterMenuDtos(menuDtos, permissions, false, userId);
		
		return menuDtos;
	}

	/**
	 * 获得所有子系统的入口，比如账号子系统.
	 */
	public List<MenuDTO> findSystemMenus(String userId) {
		String tenantId = tenantHolder.getTenantId();
		List<MenuDTO> menuDtos = this.menuCache.findEntries();

		if (menuDtos == null) {
			List<Menu> menus = menuManager.find("from Menu where type='system' and display='true'");
			menuDtos = this.convertMenuDtos(menus, true);
			this.menuCache.updateEntries(menuDtos);
		}

		List<String> permissions = userAuthConnector.findById(userId, tenantId).getPermissions();

		return this.filterMenuDtos(menuDtos, permissions, true, userId);
	}

	public Menu findMenuByUrl(String url) {
		return menuManager.findUniqueBy("url", url);
	}

	public Menu findMenuByUrl(String url, String type) {
		String hql = "from Menu where type=? and url=? ";
		
		// String hql="from Menu where type=? and url like '%" + url + "%'";
		List<Menu> menus = menuManager.find(hql, type, url);
		if (menus == null)
			return null;
		return menus.get(0);
	}

	public MenuDTO findMenuDtoByUrl(String url) {
		Menu menu = findMenuByUrl(url);
		return this.convertMenuDto(menu, true);
	}

	public Menu findMenuById(Long id) {
		return menuManager.findUniqueBy("id", id);
	}

	public MenuDTO findMenuDtoById(Long id) {
		Menu menu = findMenuById(id);
		return this.convertMenuDto(menu, true);
	}

	public List<MenuDTO> findChildMenuDtos(Menu parentMenu) {
		List<Menu> menus = findChildMenus(parentMenu, false);
		return convertMenuDtos(menus, true);
	}

	public List<MenuDTO> findChildMenuDtos(Menu parentMenu, boolean excludeModule) {
		List<Menu> menus = findChildMenus(parentMenu, false);
		return convertMenuDtos(menus, excludeModule);
	}
	
	public List<Menu> findChildMenus(Menu parentMenu, boolean auth) {
		
		String tenantId = tenantHolder.getTenantId();
		String userId = currentUserHolder.getUserId();
		String hql = "from Menu where display='true' and menu=? order by priority";
		List<Menu> menus = menuManager.find(hql, parentMenu);
		
		if (auth) {
			List<String> permissions = userAuthConnector.findById(userId, tenantId).getPermissions();

			Iterator<Menu> it = menus.iterator();
			while(it.hasNext()){
				Menu menu = it.next();
				if (!permissions.contains(menu.getId().toString())) {
			        it.remove();
			    }
			}
		}
		
		//根据菜单查询该菜单的条数
		/*for (Menu menu : menus) {
			String title = menu.getTitle();
			if("待办审批".equals(title) || "待领审批".equals(title) || "抄送审批".equals(title) || "未结流程".equals(title)){
				long count = humanTaskConnector.queryTaskCount(title);
				menu.setTitle(title+"&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;"+count);
			}
		}*/
		return menus;
	}

	/**
	 * 将menu转换为menuDto.
	 */
	public List<MenuDTO> convertMenuDtos(List<Menu> menus, boolean excludeModule) {
		List<MenuDTO> menuDtos = new ArrayList<MenuDTO>();

		for (Menu menu : menus) {
			if (!("true".equals(menu.getDisplay()))) {
				continue;
			}

			if (excludeModule && "module".equals(menu.getType())) {
				continue;
			}

			MenuDTO menuDto = this.convertMenuDto(menu, excludeModule);
			menuDtos.add(menuDto);
		}

		return menuDtos;
	}

	/**
	 * 把menu数据复制给dto.
	 */
	public MenuDTO convertMenuDto(Menu menu, boolean excludeModule) {
		MenuDTO menuDto = new MenuDTO();
		
		menuDto.setId(menu.getId());
		menuDto.setCode(menu.getCode());
		menuDto.setTitle(menu.getTitle());
		// 为了jsp里使用方便，要去掉url前面的/
		menuDto.setUrl(this.processUrl(menu.getUrl()));

		if (menu.getPerm() != null) {
			menuDto.setPermission(menu.getPerm().getCode());
		}

		menuDto.setType(menu.getType());

		List<Menu> menus = menuManager.find("from Menu where display='true' and menu=? order by priority", menu);
		List<MenuDTO> menuDtos = this.convertMenuDtos(menus, excludeModule);
		menuDto.setChildren(menuDtos);

		return menuDto;
	}

	/**
	 * 按个人权限过滤菜单.
	 */
	public List<MenuDTO> filterMenuDtos(List<MenuDTO> menuDtos, List<String> permissions, boolean excludeModule, String userId) {
		List<MenuDTO> result = new ArrayList<MenuDTO>();

		for (MenuDTO menuDto : menuDtos) {
			if (excludeModule && "module".equals(menuDto.getType())) {
				continue;
			}
			
			if (!PartyConstants.ADMIN_USER_ID.toString().equals(userId)) {
				if ((!permissions.contains("*")) && (!permissions.contains(menuDto.getId().toString()))) {
					logger.debug("permissions : {}", permissions);
					logger.debug("skip : {}", menuDto.getPermission());
	
					continue;
				}
			}

			MenuDTO item = this.filterMenuDto(menuDto, permissions, excludeModule, userId);
			result.add(item);
		}

		return result;
	}

	/**
	 * 把menu数据复制给dto.
	 */
	public MenuDTO filterMenuDto(MenuDTO menuDto, List<String> permissions, boolean excludeModule, String userId) {
		MenuDTO item = new MenuDTO();
		item.setCode(menuDto.getCode());
		item.setTitle(menuDto.getTitle());
		// 为了jsp里使用方便，要去掉url前面的/
		item.setUrl(this.processUrl(menuDto.getUrl()));

		List<MenuDTO> children = this.filterMenuDtos(menuDto.getChildren(), permissions, excludeModule, userId);
		item.setChildren(children);

		return item;
	}

	/**
	 * 如果url以/开头，要去掉/，这样前端jsp渲染的时候就方便多了.
	 */
	public String processUrl(String url) {
		if (url == null) {
			return "";
		}

		if (url.length() == 0) {
			return "";
		}

		if (url.charAt(0) != '/') {
			return url;
		}

		for (int i = 0; i < url.length(); i++) {
			if (url.charAt(i) != '/') {
				return url.substring(i);
			}
		}

		return "";
	}
	
	/**
	 * 获取操作级菜单 add by lilei at 2018-05-17
	 * **/
	public List<Menu> findOpterationMenus(String url){
		
		String tenantId = tenantHolder.getTenantId();
		String userId = currentUserHolder.getUserId();
		String hql = "from Menu where display='true' and type='opteration' and url=? order by priority";
		List<Menu> menus = menuManager.find(hql, url);
		
		//不是超级管理员，则验证
		boolean auth=!userId.equals("2");
		if (auth) {
			List<String> permissions = userAuthConnector.findById(userId, tenantId).getPermissions();

			Iterator<Menu> it = menus.iterator();
			while(it.hasNext()){
				Menu menu = it.next();
				if (!permissions.contains(menu.getId().toString())) {
			        it.remove();
			    }
			}
		}
		return menus;
	}
	
	public MenuDTO findMenuDtoByTypeAndCode(String type,String code,Long accountId){
		if(partyService.findRoleByRef(accountId)){
			return null;
		}
		
		String hql = "from Menu where display='true' and type=? and code=?";
		List<Menu> menus = menuManager.find(hql, type, code);
		Menu menuDto=null;
		if (menus == null)
			return null;
		menuDto=menus.get(0);
		
		//如果不是超级管理员
		if(!accountId.equals(PartyConstants.ADMIN_USER_ID)){
			/*List<String> partyIdList=null;
			String strSql="Select partyEntityID from auth_orgdata where type='1' and union_id="+accountId;
			partyIdList=jdbcTemplate.queryForList(strSql, String.class);*/
			/*String strPartyIds="";
			if(partyIdList!=null)
			{
				strPartyIds=Joiner.on(",").join(partyIdList);
			}*/
			String tenantId = tenantHolder.getTenantId();
			String sqlFindPermissions = "SELECT P.id AS PERMISSION"
		            + " FROM AUTH_USER_STATUS US,AUTH_USER_ROLE UR,AUTH_ROLE R,AUTH_PERM_ROLE_DEF PR,auth_menu P"
		            + " WHERE US.ID=UR.USER_STATUS_ID AND UR.ROLE_ID=R.ID AND R.ROLE_DEF_ID=PR.ROLE_DEF_ID AND PR.PERM_ID=P.ID"
		            + " AND US.DEL_FLAG = '0' AND US.REF=? AND US.TENANT_ID=?";
			
			List<String> permissions=jdbcTemplate.queryForList(sqlFindPermissions, String.class, accountId,tenantId);
			
			if(!permissions.contains(menuDto.getId().toString()))
				return null;
		}
		
		MenuDTO item = new MenuDTO();
		item.setCode(menuDto.getCode());
		item.setTitle(menuDto.getTitle());
		// 为了jsp里使用方便，要去掉url前面的/
		item.setUrl(this.processUrl(menuDto.getUrl()));
		
		hql = "from Menu where display='true' and menu=? ";
		List<Menu> childMenus=menuManager.find(hql, menuDto);
		
		//List<MenuDTO> children = this.filterMenuDtos(menuDto.getChildren(), permissions, excludeModule, userId);
		item.setChildren(convertMenuDtos(childMenus,false));

		return item;
	}
	
	/**
	 * 验证操作级菜单 add by lilei at 2019-01-02
	 * @throws UnsupportedEncodingException 
	 * **/
	public Map<String,Object> checkOperationMenuByName(HttpServletRequest request,String type,String action) throws UnsupportedEncodingException{
		Map<String,Object> returnMap=new HashMap<String, Object>();
		try{
			if(StringUtils.isBlank(type))
				type="1";
			
			String operation_url="";
			if(type.equals("1")){
				/*String request_uri = request.getRequestURI();
				String contextPath = request.getContextPath();
				operation_url = request_uri.replaceAll(contextPath, "");*/
				operation_url=request.getPathInfo();
			}
			else{
				String request_referer =request.getHeader("referer");//来源页 //request.getRequestURI();
				if(com.mossle.core.util.StringUtils.isBlank(request_referer)){
					returnMap.put("checkResult", false);
					returnMap.put("url", "redirect:/error/error-info.do?error="
											+java.net.URLEncoder.encode(java.net.URLEncoder.encode("非法操作","utf-8"),"utf-8"));
					return returnMap;
				}
				/*StringBuffer url = request.getRequestURL(); 
				String request_uri=request.getRequestURI();
				String tempContextUrl = url
								.delete(url.length() - request_uri.length(), url.length())
								.append(request.getContextPath())
								.toString();*/
				
				StringBuffer url = request.getRequestURL(); 
				String request_path=request.getPathInfo();
				String tempContextUrl1 = url
								.delete(url.length() - request_path.length(), url.length())
								.toString();
				operation_url = request_referer.replaceAll(tempContextUrl1, "");
			}
			if(StringUtils.isBlank(operation_url)){
				returnMap.put("checkResult", false);
				returnMap.put("url", "redirect:/error/error-info.do?error="
										+java.net.URLEncoder.encode(java.net.URLEncoder.encode("访问的URL有误","utf-8"),"utf-8"));
				return returnMap;
			}
			List<Menu> menuAuthList=findOpterationMenus(operation_url);
			boolean isHaveAuth=false;
			if(menuAuthList.size()>0){
				for (Menu menu : menuAuthList) {
					if(menu.getTitle().equals(action)){
						isHaveAuth=true;
						break;
					}
				}
			}
			if(!isHaveAuth){
				returnMap.put("checkResult", false);
				returnMap.put("url", "redirect:/error/error-info.do?error="
										+java.net.URLEncoder.encode(java.net.URLEncoder.encode("访问的页面没有权限","utf-8"),"utf-8"));
			}
			else{
				returnMap.put("checkResult", true);
				returnMap.put("url", "");
			}
		}
		catch(Exception ex){
			returnMap.put("checkResult", false);
			returnMap.put("url", "redirect:/error/error-info.do?error="
									+java.net.URLEncoder.encode(java.net.URLEncoder.encode("验证URL有误，请联系管理员","utf-8"),"utf-8"));
			return returnMap;
		}
		return returnMap;
	}

	/**
	 * 验证工资操作级菜单 
	 * add by ckx 
	 * at 2019-01-03
	 * 
	 * **/
	public Map<String, Object> checkMenuByName(String url, String action ) throws Exception {
		Map<String,Object> returnMap=new HashMap<String, Object>();
		List<Menu> menuAuthList=findOpterationMenus(url);
		boolean isHaveAuth=false;
		if(menuAuthList.size()>0){
			for (Menu menu : menuAuthList) {
				if("查询".equals(action)){
					isHaveAuth=true;
					break;
				}else{
					if(menu.getTitle().equals(action)){
						isHaveAuth=true;
						break;
					}
				}
			}
		}else{
			returnMap.put("checkResult", false);
			returnMap.put("url", "redirect:/error/error-info.do?error="
									+java.net.URLEncoder.encode(java.net.URLEncoder.encode("非法操作","utf-8"),"utf-8"));
			return returnMap;
			
		}
		
		if(isHaveAuth){
			returnMap.put("checkResult", true);
			returnMap.put("url", "");
		}else{
			returnMap.put("checkResult", false);
			returnMap.put("url", "redirect:/error/error-info.do?error="
									+java.net.URLEncoder.encode(java.net.URLEncoder.encode("访问的页面没有权限","utf-8"),"utf-8"));
		}
		
		return returnMap;
	}
	
	
	@Resource
	public void setMenuManager(MenuManager menuManager) {
		this.menuManager = menuManager;
	}

	@Resource
	public void setUserAuthConnector(UserAuthConnector userAuthConnector) {
		this.userAuthConnector = userAuthConnector;
	}

	@Resource
	public void setTenantHolder(TenantHolder tenantHolder) {
		this.tenantHolder = tenantHolder;
	}

	@Resource
	public void setMenuCache(MenuCache menuCache) {
		this.menuCache = menuCache;
	}

	@Resource
	public void setCurrentUserHolder(CurrentUserHolder currentUserHolder) {
		this.currentUserHolder = currentUserHolder;
	}
	
	//private JdbcTemplate jdbcTemplate;
	@Resource
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate){
		this.jdbcTemplate = jdbcTemplate;
	}
	
	@Resource
	public void setPartyService(PartyService partyService){
		this.partyService = partyService;
	}

}
