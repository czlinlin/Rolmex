<%@tag pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>
<%@tag import="java.util.List"%>
<%@tag import="org.springframework.context.ApplicationContext"%>
<%@tag import="org.springframework.web.context.support.WebApplicationContextUtils"%>
<%@tag import="com.mossle.api.menu.MenuConnector"%>
<%@tag import="com.mossle.api.menu.MenuDTO"%>
<%@tag import="com.mossle.core.auth.CurrentUserHolder"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@attribute name="currentMenuName" type="java.lang.Object" required="true"%>
<%
	String currentMenuName = (String) jspContext.getAttribute("currentMenuName");
	ApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(application);
	MenuConnector menuConnector = ctx.getBean(MenuConnector.class);
	CurrentUserHolder currentUserHolder = ctx.getBean(CurrentUserHolder.class);
	try {
		String userId = currentUserHolder.getUserId();
		// System.out.println("userId : " + userId);
		List<MenuDTO> menuDtos = menuConnector.findSystemMenus(userId);
		MenuDTO hrMenu = menuConnector.findMenuDtoByTypeAndCode("module", "hr",Long.valueOf(userId));
		if(hrMenu!=null)
			menuDtos.add(hrMenu);
		if (menuDtos.size()>0) {
			
%>
	<li class="dropdown">
		<%if (menuDtos.size() == 1 && menuDtos.get(0).getTitle().equals("个人事务"))  { %>
			
		<%} else { 
			if(currentMenuName.equals("index")){
				//currentMenuName=menuDtos.get(0).getTitle();
				jspContext.setAttribute("currentMenuName", menuDtos.get(0).getTitle());
			}
		%>
			<%if(currentMenuName.equals("index")){ 
				if(menuDtos.get(0).getTitle().equals("个人事务"))
					jspContext.setAttribute("currentMenuTitle", menuDtos.get(1).getTitle());
				else
					jspContext.setAttribute("currentMenuTitle", menuDtos.get(0).getTitle());
			%>
				<a data-toggle="dropdown" class="dropdown-toggle" href="#"> <i
					class="glyphicon glyphicon-list"></i> ${currentMenuTitle}<b class="caret"></b></a>
			<%} else{%>
			<a data-toggle="dropdown" class="dropdown-toggle" href="#"> <i
					class="glyphicon glyphicon-list"></i> ${currentMenuName}<b class="caret"></b></a>
			<%} %>
		<%} %>
		<ul class="dropdown-menu">
			<% 		
			for (MenuDTO menuDto : menuDtos) {
				// System.out.println("=========================" + menuDto.getTitle());
				jspContext.setAttribute("menu", menuDto);
			%>
				<c:if test="${menu.title != '个人事务'}">
					<li><a href="${tenantPrefix}/${menu.url}"><i
							class="glyphicon glyphicon-list"></i> ${menu.title}</a></li>
					<li class="divider"></li>
				</c:if>
			<%
			}
			
		}
	} catch (Exception ex) {
		System.out.println(ex);
	}
%>
		</ul>
	</li>

