<%@page contentType="text/html;charset=UTF-8"%>
<%@include file="/taglibs.jsp"%>
<%pageContext.setAttribute("currentHeader", "org");%>

<%pageContext.setAttribute("currentMenu", "org");%>
<!doctype html>
<html lang="en">

  <head>
    <%@include file="/common/meta.jsp"%>
    <title><spring:message code="dev.org.list.title" text="麦联"/></title>
    <%@include file="/common/s3.jsp"%>
    <script type="text/javascript">

		var table;

		$(function() {
		    if(window.parent.dialog!=undefined&&window.parent.dialog!=null)
	    	{
	    		window.parent.closeLoading();
	    	}
			table = new Table(config);
		    table.configPagination('.m-pagination');
		    table.configPageInfo('.m-page-info');
		    table.configPageSize('.m-page-size');
		});

		function search() {
			$("#authSearchForm").attr("action","user-orgdata-list-i.do");
			$("#authSearchForm").submit();
		}
		
		function addAdmin(partyStructTypeId, partyEntityId, partyTypeId, level) {
			$("#mainframe").attr('src','org-admin-list.do?partyStructTypeId=' + partyStructTypeId + '&partyEntityId=' + 
					partyEntityId + '&partyTypeId=' + partyTypeId + '&level=' + level);
		}

		// 删除
        function removeOrg(partyId) {
            if (confirm('确定要删除此项吗？')) {
            	$.ajax({      
                    url: '${tenantPrefix}/rs/auth/removePartyData',      
                    datatype: "json",
                    data:{"partyId": partyId},
                    type: 'post',      
                    success: function (e) {
                    	//成功后回调   
                    	if (e.result == "ok") {
                    		alert(e.msg);
                    		$("#btnSearch").click();
                    	} else {
                            alert(e.msg); 
                    	}
                    	     
                    },      
                    error: function(e){      
                    	//失败后回调      
                        alert("服务器请求失败");      
                    }/* ,      
                    beforeSend: function(){      
                    //发送请求前调用，可以放一些"正在加载"之类额话      
                        alert("正在加载");           
            		} */
               }); 

                return true;
            } else {
                return false;
            }
        }
    </script>
  </head>

  <body>
    
    <div class="row-fluid">
	  
	  <%-- <c:if test="${not empty flashMessages}">
		<div id="m-success-message" style="display:none;">
		  <ul>
		  <c:forEach items="${flashMessages}" var="item">
		    <c:if test="${item != ''}">
		    	<li>${item}</li>
		    </c:if>
		  </c:forEach>
		  </ul>
		</div>
	 </c:if> --%>
	
	  <!-- start of main -->
      <section id="m-main" class="col-md-12" style="padding-top:65px;">
		
		<div class="panel panel-default">
		  <div class="panel-heading">
			<i class="glyphicon glyphicon-list"></i>
		    查询
			<div class="pull-right ctrl">
			  <a class="btn btn-default btn-xs"><i id="orgSearchIcon" class="glyphicon glyphicon-chevron-up"></i></a>
		    </div>
		  </div>
  		  <div class="panel-body">
			  <form name="authSearchForm" method="post" action="user-orgdata-list-i.do" class="form-inline" >
			    <%--<input type="hidden" name="partyStructTypeId" value="${param.partyStructTypeId}">
			    <input type="hidden" name="partyEntityId" value="${param.partyEntityId}"> --%>
			    <label for="org_name"><spring:message code='org.org.list.search.name' text='姓名'/>:</label>
			    <input type="text" id="iptName" name="iptName" value="${paramname}" class="form-control">
				<button id="btnSearch" class="btn btn-default a-search" type="submit">查询</button>&nbsp;
			  </form>
		  </div>
	   </div>
	   
       <div style="margin-bottom: 20px;">
	    <div class="pull-left">
	      <div class="btn-group" role="group">
	      	<button class="btn btn-default a-insert" onclick="location.href='user-orgdata-input-i.do?id=0'">新建</button>
		  </div>
		  <%-- <c:if test="${viewManage}">
		  	<a href="org-admin-list.do?partyStructTypeId=${partyStructTypeId}&partyEntityId=${partyEntityId}&partyTypeId=${item.id}&level=${level}" class="btn btn-default">管理者</a>
		  	<a onclick="addAdmin(${partyStructTypeId},${partyEntityId},'',${level})" class="btn btn-default">管理者</a>
		  </c:if> --%>
		  
		</div>

		<!-- <div class="pull-right">
		  每页显示
		  <select class="m-page-size form-control" style="display:inline;width:auto;">
		    <option value="10">10</option>
		    <option value="20">20</option>
		    <option value="50">50</option>
		  </select>
		  条
        </div> -->

	    <div class="clearfix"></div>
	  </div>

<form id="orgGridForm" name="orgGridForm" method='post' action="org-remove.do" class="m-form-blank">
      <div class="panel panel-default">
        <div class="panel-heading">
		  <i class="glyphicon glyphicon-list"></i>
		  <spring:message code="scope-info.scope-info.list.title" text="列表"/>
		</div>


  <table id="orgGrid" class="table table-hover">
    <thead>
      <tr>
	    <!--
        <th width="10" class="table-check"><input type="checkbox" name="checkAll" onchange="toggleSelectedItems(this.checked)"></th>
		-->
        <th class="sorting" name="id">类型</th>
        <th class="sorting" name="name">姓名</th>
        <th width="120">操作</th>
      </tr>
    </thead>

    <tbody>
      <c:forEach items="${userList}" var="item">
      	
	      <tr>
		    <!--
	        <td><input type="checkbox" class="selectedItem" name="selectedItem" value="${item.childEntity.id}"></td>
			-->
	        <td>
	        	<c:if test="${item.type == '1'}">
	        		人员
	        	</c:if>
	        	<c:if test="${item.type == '2'}">
	        		角色
	        	</c:if>
	        </td>
	        <td>
	        	${item.name}
	        </td>
	        <td>
			    <a href="user-orgdata-input-i.do?id=${item.union_id}" class="a-remove">编辑</a>
			  	<a href="javascript:removeOrg(${item.union_id})" class="a-remove">删除</a>
			 </td>
	      </tr>
	     
      </c:forEach>
    </tbody>
  </table>


      </div>
</form>

	  <!-- <div>
	    <div class="m-page-info pull-left">
		  共100条记录 显示1到10条记录
		</div>

		<div class="btn-group m-pagination pull-right">
		  <button class="btn btn-default">&lt;</button>
		  <button class="btn btn-default">1</button>
		  <button class="btn btn-default">&gt;</button>
		</div>

	    <div class="clearfix"></div>
      </div> -->

      <div class="m-spacer"></div>

      </section>
	  <!-- end of main -->
	</div>

  </body>

</html>

