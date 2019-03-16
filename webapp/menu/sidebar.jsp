<%@page import="com.fasterxml.jackson.annotation.JsonInclude.Include"%>
<%@page import="javax.servlet.jsp.tagext.TryCatchFinally"%>
<%@ page language="java" pageEncoding="UTF-8"%>
<%@ page import="org.springframework.context.ApplicationContext"%>
<%@ page
	import="org.springframework.web.context.support.WebApplicationContextUtils"%>
<%@ page import="com.mossle.api.menu.MenuConnector"%>
<%@ page import="com.mossle.api.menu.MenuDTO"%>
<%@ page import="com.mossle.auth.persistence.domain.Menu"%>
<%@ page import="com.mossle.core.auth.CurrentUserHolder" %>
<%@ page import="java.util.List"%>

<!-- start of sidebar -->
<style type="text/css">
#accordion .panel-heading {
	cursor: pointer;
}

#accordion .panel-body {
	padding: 0px;
}
.badge1 {
	display: inline-block;
	min-width: 10px;
	padding: 3px 7px;
	font-size: 12px;
	font-weight: 700;
	line-height: 1;
	color: #fff;
	text-align: center;
	white-space: nowrap;
	vertical-align: middle;
	background-color: #e03737;
	border-radius: 10px
}

</style>
<div class="panel-group col-md-2" id="accordion" role="tablist"
	aria-multiselectable="true" style="padding-top: 65px;">

	<%
		try {
			//根据当前uri获取菜单数据
			String request_uri = request.getAttribute("javax.servlet.forward.request_uri").toString();
			String contextPath = request.getContextPath();
			request_uri = request_uri.replaceAll(contextPath, "");
			//out.println(request_uri);
			
			// 请求参数
			String queryurl=request.getQueryString();  
		    /* if(null!=queryurl){  
		    	request_uri+="?"+queryurl;  
		    }  */
		    // out.println("===" + request_uri);
			ApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(application);
			MenuConnector menuConnector = ctx.getBean(MenuConnector.class);
			Menu menu = menuConnector.findMenuByUrl(request_uri, "page");
			//out.println("========================" + request_uri);
			//out.println("========================" + menu.getType());
	
			//根据当前菜单数据获取父级菜单数据
			Menu pageMenu = null;
			if (menu.getType().equals("page"))
				pageMenu = menu;
			//out.println("page:"+menu.getTitle());
			//获取上级菜单
			menu = menu.getMenu();
			//out.println(menu.getType());

			//侧栏父级
			Menu sideMenu = null;
			if (menu.getType().equals("side"))
				sideMenu = menu;
			//out.println("side:"+menu.getTitle());

			//获取上级菜单
			menu = menu.getMenu();
			//out.println(menu.getType());

			//头部导航左侧
			Menu moduleMenu = null;
			if (menu.getType().equals("module"))
				moduleMenu = menu;

			//out.println("module:"+menu.getTitle());
			
			CurrentUserHolder currentUserHolder1 = ctx.getBean(CurrentUserHolder.class);
			//out.println(currentUserHolder1.getUserId());
			
			//是否需要验证
			boolean isAuth=!currentUserHolder1.getUserId().equals("2");
			//获取所有侧栏父级菜单
			List<Menu> sideMenus = menuConnector.findChildMenus(moduleMenu, isAuth);
			int sideMenusCount=sideMenus.size();
			for (Menu sm : sideMenus) {
				//out.println(sm.getTitle()+"<br/>");
	%>

	<!-- Collapse 父菜单  开始 ===================================================== -->
	<div class="panel panel-default">
		<%--
		<div class="panel-heading" role="tab" id="heading-<%=sm.getId()%>">
			<h4 class="panel-title">
				<a role="button" data-toggle="collapse" data-parent="#accordion"
					href="#collapse-<%=sm.getId()%>" aria-expanded="true"
					aria-controls="collapse<%=sm.getId()%>"
					style="text-decoration: none"><a href="${tenantPrefix}<%=sm.getUrl() %>"> <i
					class="glyphicon glyphicon-list"></i> <%=sm.getTitle()%>
				</a>
			</h4>
		</div> --%>
		
		<div class="panel-heading" role="tab" id="collapse-header-bpm-process" data-toggle="collapse" data-parent="#accordion" href="#collapse-<%=sm.getId()%>" aria-expanded="true" aria-controls="collapse-body-bpm-process">
	      <h4 class="panel-title">
		    <i class="glyphicon glyphicon-list" ></i>
	        	<%=sm.getTitle()%>
	      </h4>
	    </div>
    
		<div id="collapse-<%=sm.getId()%>"
			class="panel-collapse collapse <%if ((sideMenu.getId() == sm.getId())||sideMenusCount==1)
						out.println("in");%>"
			role="tabpanel" aria-labelledby="heading-<%=sm.getId()%>">
			<div class="panel-body">
				<!-- nav 子菜单 开始 -------------------------------------------------- -->
				<ul class="nav nav-pills nav-stacked">
					<%
						//获取所有兄弟菜单
								List<Menu> pageMenus = menuConnector.findChildMenus(sm, isAuth);
								//out.println(sideMenus.size());
								for (Menu m : pageMenus) {
									if(m.getTitle().equals("树形机构")){
					%>
						<%@include file="/common/tree/org.jsp" %>
					<%} else if(m.getTitle().equals("树形职员")){%>
						<%@include file="/common/tree/person.jsp" %>
					<%} else if(m.getTitle().equals("树形职员考勤")){%>
						<%@include file="/common/tree/person-attendance.jsp" %>
					<%} else if(m.getTitle().equals("树形考勤统计")){%>
						<%@include file="/common/tree/person-attendance-statistics.jsp" %>
					<%}else{ %>
					<li
						<%if (pageMenu.getId() == m.getId())
							out.println("class='active'");%>>
						<a href="${tenantPrefix}<%=m.getUrl() %>" > <i
							class="glyphicon glyphicon-list"></i> <%=m.getTitle()%>
							<!-- &emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp; -->
							<i class="badge" id="<%=m.getTitle()%>"></i>
						</a>
					</li>	
					<%
						} //侧栏子级循环结束
								}
					%>
				</ul>
				<!-- nav 子菜单 结束 -------------------------------------------------- -->
			</div>
		</div>

	</div>
	<!-- Collapse 父菜单  结束 ================================================================= -->

	<%
		} //侧栏父级循环结束
		} catch (Exception e) {
			out.println(e.getMessage());
		}
	%>

</div>

<!-- end of sidebar -->
<script type="text/javascript">
	// ckx 2019/2/13
	 $(document).ready(function(){
		refreshCount();
		var t1=window.setInterval(refreshCount, 1000*60*2);
	 });
	 
	function refreshCount() {
		$.ajax({
            url: "${tenantPrefix}/humantask/queryTaskCount.do?_sed=" + new Date().getTime()+"",
            type: "POST",
            data: {},
            dataType : "json",
            success: function (data) {
                if (data != undefined && data != null && data != "") {
                	if(data.personalTasks != 0){
                		$("#待办审批").text(data.personalTasks);
                		$("#待办审批").attr("style","background-color: #e03737;color: #fff;float:right ");
                	}
                	if(data.groupTasks != 0){
                		$("#待领审批").text(data.groupTasks);
                		$("#待领审批").attr("style","background-color: #e03737;color: #fff;float:right ");
                	}
                	if(data.personalCopyTasks != 0){
                		$("#抄送审批").text(data.personalCopyTasks);
                		$("#抄送审批").attr("style","background-color: #e03737;color: #fff;float:right ");
                	}
                	if(data.listRunningProcessInstances != 0){
                		$("#未结流程").text(data.listRunningProcessInstances);
                		$("#未结流程").attr("style","background-color: #e03737;color: #fff;float:right ");
                	}
                	/* var c1 = $("#待办审批").parent().parent().attr("class");
                	var c2 = $("#待领审批").parent().parent().attr("class");
                	var c3 = $("#抄送审批").parent().parent().attr("class");
                	var c4 = $("#未结流程").parent().parent().attr("class");
                	if(c1 == 'active'){
                		$("#待办审批").attr("style","background-color: #fff;");
                	}
                	if(c2 == 'active'){
                		$("#待领审批").attr("style","background-color: #fff;");
                	}
                	if(c3 == 'active'){
                		$("#抄送审批").attr("style","background-color: #fff;");
                	}
                	if(c4 == 'active'){
                		$("#未结流程").attr("style","background-color: #fff;");
                	} */
                }
            },
            error: function (XMLHttpRequest, textStatus, errorThrown) {
                console.log("[" + XMLHttpRequest.status + "]error，请求失败");
            },
            complete: function (xh, status) {
                if (status == "timeout")
                	console.log("请求超时");
            }
        });
	}

  //去掉定时器的方法  
  //window.clearInterval(t1);   
	
</script>


