<%@tag pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>
<%@tag import="java.util.ArrayList"%>
<%@tag import="java.util.List"%>
<%@tag import="org.springframework.context.ApplicationContext"%>
<%@tag
	import="org.springframework.web.context.support.WebApplicationContextUtils"%>
<%@tag import="com.mossle.api.menu.MenuConnector"%>
<%@tag import="com.mossle.api.menu.MenuDTO"%>
<%@tag import="com.mossle.auth.persistence.domain.Menu"%>
<%@tag import="com.mossle.core.auth.CurrentUserHolder"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<%
	try {
		//根据当前uri获取菜单数据
		String request_uri = request.getAttribute("javax.servlet.forward.request_uri").toString();
		String contextPath = request.getContextPath();
		request_uri = request_uri.replaceAll(contextPath, "");

		ApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(application);
		MenuConnector menuConnector = ctx.getBean(MenuConnector.class);
		Menu menu = menuConnector.findMenuByUrl(request_uri, "page");

		//根据当前菜单数据获取父级菜单数据
		Menu pageMenu = null;
		if (menu.getType().equals("page"))
			pageMenu = menu;
		//out.println(pageMenu.getType());

		//获取上级菜单
		menu = menu.getMenu();

		//侧栏父级
		Menu sideMenu = null;
		if (menu.getType().equals("side"))
			sideMenu = menu;

		//获取上级菜单
		menu = menu.getMenu();

		//头部导航左侧
		Menu moduleMenu = null;
		if (menu.getType().equals("module"))
			moduleMenu = menu;

		//获取上级菜单
		menu = menu.getMenu();

		//头部导航右侧
		Menu systemMenu = null;
		if (menu.getType().equals("system"))
			systemMenu = menu;
		
		//out.println(menu.getTitle());

		CurrentUserHolder currentUserHolder = ctx.getBean(CurrentUserHolder.class);

		String userId = currentUserHolder.getUserId();
		// System.out.println("userId : " + userId);
		
		List<Menu> menus = menuConnector.findChildMenus(systemMenu, true);
		int index = 0;
		int size = menus.size();
		//out.println(menus.size());
		
		List<Menu> overflows = new ArrayList<Menu>();
		
		for (Menu m : menus) {
			// System.out.println(m.getTitle());
			jspContext.setAttribute("menu", m);
			
			index++;
			if (index > 5) {
				overflows.add(m);
				continue;
			}
%>

<li class="${currentHeader == menu.code ? 'active' : ''}"><a
	href="${tenantPrefix}${menu.url}"> <i
		class="glyphicon glyphicon-list"></i> ${menu.title}
</a></li>


<%
	}//for end
		
		if (!overflows.isEmpty()) {
			jspContext.setAttribute("overflows", overflows);
%>
<li class="dropdown"><a data-toggle="dropdown"
	class="dropdown-toggle" href="#"> <i
		class="glyphicon glyphicon-list"></i> 更多 <b class="caret"></b>
</a>
	<ul class="dropdown-menu">
		<c:forEach items="${overflows}" var="child">
			<li><a href="${tenantPrefix}/${child.url}"><i
					class="glyphicon glyphicon-list"></i>&nbsp;${child.title}</a></li>
		</c:forEach>
	</ul></li>
<%
	}
	} catch (Exception ex) {
		System.out.println(ex);
	}
%>
