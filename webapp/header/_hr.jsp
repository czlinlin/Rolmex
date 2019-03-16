<%@ page language="java" pageEncoding="UTF-8" %>

<%@include file="_header_first.jsp"%>

<div class="navbar navbar-default navbar-fixed-top">
  <div class="container-fluid">
    <div class="navbar-header">
      <a class="navbar-brand" href="${tenantPrefix}/portal/index.do">
	    <img src="${cdnPrefix}/logo.png"  title="首页"  class="img-responsive pull-left" style="margin-top:-25px;margin-right:5px;">
	    <!-- Rolmex <sub><small>1.0.0</small></sub> -->
      </a>
    </div>

    <div class="navbar-collapse collapse">
      <ul class="nav navbar-nav" id="navbar-menu">
		<tags:menuNav3 systemCode="hr"/>
      </ul>

	  <%@include file="_header_second.jsp"%>
    </div>

  </div>
</div>
