<%@ page language="java" pageEncoding="UTF-8" %>
<%@page import="org.apache.commons.lang3.StringUtils"%>
<%@page import="java.util.List"%>
<%@page import="org.springframework.context.ApplicationContext"%>
<%@page import="org.springframework.web.context.support.WebApplicationContextUtils"%>
<%@page import="com.mossle.api.userauth.UserAuthConnector"%>
<%@page import="com.mossle.core.auth.CurrentUserHolder"%>
<%@page import="com.mossle.auth.RoleConstants"%>
<%@page import="com.mossle.api.store.StoreConnector"%>
<%@page import="com.mossle.internal.store.persistence.domain.StoreInfo"%>

<%
	ApplicationContext headerCtx = WebApplicationContextUtils.getWebApplicationContext(application);;
	CurrentUserHolder currentUserHolder = headerCtx.getBean(CurrentUserHolder.class);
	UserAuthConnector userAuthConnector = headerCtx.getBean(UserAuthConnector.class);
	StoreConnector storeConnector = headerCtx.getBean(StoreConnector.class);
	
	String userId = currentUserHolder.getUserId();
	List<String> listRole = userAuthConnector.findById(userId, "1").getRoles();
	List<StoreInfo> listavatar = storeConnector.getStore(userId);
	String avatrPaht = "";
	String viewUrl = storeConnector.getViewUrl();
	if (listavatar != null & listavatar.size() > 0) {
		avatrPaht = ((StoreInfo)listavatar.get(0)).getPath();
		viewUrl = viewUrl + "/" + avatrPaht;
	}
%>
<div class="navbar navbar-default navbar-fixed-top">
  <div class="container-fluid">
    <div class="navbar-header">
      <a class="navbar-brand" href="${tenantPrefix}/portal/index.do">
	    <img src="${cdnPrefix}/logo.png" title="首页"  class="img-responsive pull-left" style="margin-top:-25px;margin-right:5px;">
      </a>
    </div>

    <div class="navbar-collapse collapse">
      <ul class="nav navbar-nav" id="navbar-menu">
		<tags:menuNav3 systemCode="pim"/>
      </ul>

      <ul class="nav navbar-nav navbar-right">
	    <li>
          <%-- <form class="navbar-form navbar-search" action="${tenantPrefix}/pim/address-list-list.do" role="search">
            <div class="form-group">
              <input type="text" class="form-control search-query" placeholder="搜索" name="username">
            </div>
          </form> --%>
	    </li>
	  
		<tags:menuSystem3/>

        <li class="dropdown">
          <a data-toggle="dropdown" class="dropdown-toggle" href="#">
		    <% if (StringUtils.isBlank(avatrPaht)) {%>
          	  <img src="${tenantPrefix}/rs/avatar?id=<tags:currentUserId/>&width=16" style="width:16px;height:16px;" class="img-circle">
            <% } else {%>
          	  <img src="<%=viewUrl%>" style="width:16px;height:16px;" class="img-circle">
            <%} %>
			<tags:currentUser/>
            <b class="caret"></b>
          </a>
          <ul class="dropdown-menu">
            <li><a href="${tenantPrefix}/user/my-info-input.do"><i class="glyphicon glyphicon-user"></i>个人信息</a></li>
            <li class="divider"></li>
			  <li><a href="${tenantPrefix}/j_spring_security_logout"><i class="glyphicon glyphicon-user"></i>退出</a></li>
          </ul>
        </li>
		<li>
          <a href="${tenantPrefix}/msg/msg-info-listReceived.do" title="消息">
            <i class="glyphicon glyphicon-bell"></i>
			<i id="unreadMsg" class="badge"></i>
	      </a>
		</li>

      </ul>
    </div>

  </div>
</div>
