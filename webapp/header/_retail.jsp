<%@ page language="java" pageEncoding="UTF-8" %>

<%@include file="_header_first.jsp"%>

    <!-- start of header bar -->
    <div class="navbar navbar-default">
      <div class="navbar-inner">
        <div class="container">
          <a data-target=".navbar-responsive-collapse" data-toggle="collapse" class="btn btn-navbar">
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
          </a>
          <a href="${tenantPrefix}/portal/index.do" class="brand">
	        <img src="${cdnPrefix}/logo.png" title="首页"  class="img-responsive pull-left" style="margin-top:-25px;margin-right:5px;">
		  </a>
          <div class="nav-collapse collapse navbar-responsive-collapse">
            <ul class="nav">
              <li class="divider-vertical"></li>

<tags:menuNav2 systemCode="retail"/>

            </ul>

			<%@include file="_header_second.jsp"%>
          </div><!-- /.nav-collapse -->
        </div>
      </div><!-- /navbar-inner -->
    </div>
    <!-- end of header bar -->
