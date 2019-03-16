<%@ page language="java" pageEncoding="UTF-8" %>
<%@page import="javax.servlet.jsp.tagext.TryCatchFinally"%>
<%@ page import="org.springframework.context.ApplicationContext"%>
<%@ page
	import="org.springframework.web.context.support.WebApplicationContextUtils"%>
<%@ page import="com.mossle.api.menu.MenuConnector"%>
<%@ page import="com.mossle.api.menu.MenuDTO"%>
<%@ page import="com.mossle.auth.persistence.domain.Menu"%>
<%@ page import="java.util.List"%>
<style type="text/css">
#accordion .panel-heading {
	cursor: pointer;
}
#accordion .panel-body {
	padding:0px;
}
</style>

<%--菜单控制begin --%>
<div class="panel-group col-md-2" id="accordion" role="tablist"
	aria-multiselectable="true" style="padding-top: 65px;">

	<%
		try {
			//根据当前uri获取菜单数据
			String request_uri = request.getAttribute("javax.servlet.forward.request_uri").toString();
			String contextPath = request.getContextPath();
			request_uri = request_uri.replaceAll(contextPath, "");
			
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

			//获取上级菜单
			menu = menu.getMenu();
			//out.println(menu.getType());

			//侧栏父级
			Menu sideMenu = null;
			if (menu.getType().equals("side"))
				sideMenu = menu;

			//获取上级菜单
			menu = menu.getMenu();
			//out.println(menu.getType());

			//头部导航左侧
			Menu moduleMenu = null;
			if (menu.getType().equals("module"))
				moduleMenu = menu;
			
			//out.println(moduleMenu.getTitle()+" "+moduleMenu.getType());

			//获取所有侧栏父级菜单
			List<Menu> sideMenus = menuConnector.findChildMenus(moduleMenu, false);
			
			for (Menu sm : sideMenus) {
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
		    <i class="glyphicon glyphicon-list"></i>
	        	<%=sm.getTitle()%>
	      </h4>
	    </div>
    
		<div id="collapse-<%=sm.getId()%>"
			class="panel-collapse collapse <%if (sideMenu.getId() == sm.getId())
						out.println("in");%>"
			role="tabpanel" aria-labelledby="heading-<%=sm.getId()%>">
			<div class="panel-body">
				<!-- nav 子菜单 开始 -------------------------------------------------- -->
				<ul class="nav nav-pills nav-stacked">
					<%
						//获取所有兄弟菜单
						List<Menu> pageMenus = menuConnector.findChildMenus(sm, false);
						for (Menu m : pageMenus) {
					%>
						<%if(m.getTitle().equals("树形结构")) {%>
							<%@include file="/common/tree/org.jsp" %>
						<%} else{%>
						<li
						<%if (pageMenu.getId() == m.getId())
							out.println("class='active'");%>>
						<a href="${tenantPrefix}<%=m.getUrl() %>"> <i
							class="glyphicon glyphicon-list"></i> <%=m.getTitle()%>
					</a>
					</li>
					<%
						} 
								}//侧栏子级循环结束
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
<%-- --%>

      <!-- start of sidebar -->
<%-- <div class="panel-group col-md-2" id="accordion" role="tablist" aria-multiselectable="true" style="padding-top:65px;">

  <div class="panel panel-default">
    <div class="panel-heading" role="tab" id="collapse-header-org" data-toggle="collapse" data-parent="#accordion" href="#collapse-body-org" aria-expanded="true" aria-controls="collapse-body-org">
      <h4 class="panel-title">
	    <i class="glyphicon glyphicon-list"></i>
        组织机构
      </h4>
    </div>
    <div id="collapse-body-org" class="panel-collapse collapse ${currentMenu == 'org' ? 'in' : ''}" role="tabpanel" aria-labelledby="collapse-header-org">
      <div class="panel-body">

		    <select style="width:100%;display:none" onchange="location.href='org-list.do?partyStructTypeId=' + this.value">
			  <c:forEach items="${partyStructTypes}" var="item">
			  <option value="${item.id}" ${item.id == param.partyStructTypeId ? 'selected' : ''}>${item.name}</option>
			  </c:forEach>
			</select>
            <ul id="treeMenu" class="ztree"></ul>
      </div>
    </div>
  </div>

		<footer id="m-footer" class="text-center">
		  <hr>
		  &copy;Rolmex
		</footer>

</div>
      <!-- end of sidebar -->

<script type="text/javascript">
		var setting = {
			data: {    
                simpleData: {    
                    enable: true  
                },
                key: {  
                    title: "title"  
                }
            },
			async: {
				enable: true,
				url: "${tenantPrefix}/party/asyncTree.do?partyStructTypeId=1&notViewPost=false&notAuth=true",
				autoParam:["id","name"],  
				type:"post",//默认post 
				dataFilter: filter  //异步返回后经过Filter   
			},
			view: {
				expandSpeed: "",
				nameIsHTML: true
			},
			callback: {
				onClick: function(event, treeId, treeNode) {
					window.frames[0].location = '${tenantPrefix}/party/org-list-i.do?partyStructTypeId=1&partyEntityId=' + treeNode.id;
				},
				asyncSuccess: zTreeOnAsyncSuccess,//异步加载成功的fun    
                asyncError: zTreeOnAsyncError   //加载错误的fun    
                // beforeClick:beforeClick //捕获单击节点之前的事件回调函数  
			}
		};

		//treeId是treeDemo  
        function filter(treeId, parentNode, childNodes) {    
            if (!childNodes) return null;    
            for (var i=0, l=childNodes.length; i<l; i++) {    
                childNodes[i].name = childNodes[i].name.replace('','');    
            }    
            return childNodes;    
        }    
          
        function beforeClick(treeId,treeNode){  
            if(!treeNode.isParent){  
                // alert("请选择父节点");  
                return false;  
            }else{  
                return true;  
            }  
        }  
          
        function zTreeOnAsyncError(event, treeId, treeNode){    
            alert("异步加载失败!");    
        }    
          
        function zTreeOnAsyncSuccess(event, treeId, treeNode, msg){    
              
        } 
        
		var zNodes =[];

		$(function(){
			
			$.fn.zTree.init($("#treeMenu"), setting, zNodes);
		});
		
		function removeNode(id) {
			var zTree = $.fn.zTree.getZTreeObj("treeMenu");
			var node = zTree.getNodeByParam("id",id);  
			zTree.removeNode(node);

		}
		
</script> --%>
