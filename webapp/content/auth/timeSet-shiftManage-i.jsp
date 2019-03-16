<%@page contentType="text/html;charset=UTF-8"%>
<%@include file="/taglibs.jsp"%>
<%pageContext.setAttribute("currentHeader", "org");%>

<%pageContext.setAttribute("currentMenu", "org");%>
<%pageContext.setAttribute("currentMenuName", "index");%>
<!doctype html>
<html lang="en">

  <head>
    <%@include file="/common/meta.jsp"%>
    <title><spring:message code="dev.org.list.title" text="麦联"/></title>
    <%@include file="/common/s3.jsp"%>
    <script type="text/javascript" src="${cdnPrefix}/bootbox/bootbox.js"></script>
    <script type="text/javascript">
    var config = {
            id: 'processGrid',
            pageNo: ${page.pageNo},
            pageSize: ${page.pageSize},
            totalCount: ${page.totalCount},
            resultSize: ${page.resultSize},
            pageCount: ${page.pageCount},
            orderBy: '${page.orderBy == null ? "" : page.orderBy}',
            params: {
                'shiftName': '${param.shiftName}'
            },
            asc: ${page.asc},
            selectedItemClass: 'selectedItem',
            gridFormId: 'processGridForm',
        };
	    $(function(){
			window.parent.$.showMessage($('#m-success-tip-message').html(), {
	            position: 'top',
	            size: '50',
	            fontSize: '20px'
	        });
		});
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
		    $("#shiftManage").css({'background-color':'#EBEBEB'});
		});
    </script>
  </head>

  <body>
    <div class="row-fluid">
	    <c:if test="${not empty flashMessages}">
			<div id="m-success-tip-message" style="display: none;">
				<ul>
					<c:forEach items="${flashMessages}" var="item">
						<c:if test="${item != ''}">
							<li>${item}</li>
						</c:if>
					</c:forEach>
				</ul>
			</div>
	   </c:if>
    
	  <!-- start of main -->
      <section id="m-main" class="col-md-12" style="padding-top:65px;">
      	<div class="pull-left">
			<div class="btn-group" role="group">
				<button id="attendanceManage" class="btn btn-default a-search" onclick="location.href='attendance-records-time-set-i.do'">考勤组管理</button>
				<button id="shiftManage" class="btn btn-default a-search" onclick="location.href='timeSet-shiftManage-i.do'">班次管理</button>
			</div>
		</div>
		<br/><br/><br/>
		<div class="panel panel-default">
		  <div class="panel-heading">
			<i class="glyphicon glyphicon-list"></i>
		    查询
			<div class="pull-right ctrl">
			  <a class="btn btn-default btn-xs"><i id="orgSearchIcon" class="glyphicon glyphicon-chevron-up"></i></a>
		    </div>
		  </div>
  		  <div class="panel-body">
			  <form name="authSearchForm" method="post" action="timeSet-shiftManage-i.do" class="form-inline" >
			    <%--<input type="hidden" name="partyStructTypeId" value="${param.partyStructTypeId}">
			    <input type="hidden" name="partyEntityId" value="${param.partyEntityId}"> --%>
			    <label for="org_name"><spring:message code='org.org.list.search.name' text='班次名称'/>:</label>
			    <input type="text" id="shiftName" name="shiftName" value="${param.shiftName}" class="form-control">
				<button id="btnSearch" class="btn btn-default a-search" type="submit">查询</button>&nbsp;
			  </form>
		  </div>
	   </div>
	   
       <div style="margin-bottom: 20px;">
	    <div class="pull-left">
	      <div class="btn-group" role="group">
	      	<button class="btn btn-default a-insert" onclick="location.href='new-shift-i.do?'">新增班次</button>
	      	<!-- <button class="btn btn-default a-insert" onclick="location.href='user-orgdata-input-i.do?id=0'">编辑</button>
	      	<button class="btn btn-default a-remove" onclick="table.removeAll()">删除</button> -->
		  </div>
		</div>

		<div class="pull-right">
		  每页显示
		  <select class="m-page-size form-control" style="display:inline;width:auto;">
		    <option value="10">10</option>
		    <option value="20">20</option>
		    <option value="50">50</option>
		  </select>
		  条
        </div>

	    <div class="clearfix"></div>
	  </div>

<form id="orgGridForm" name="orgGridForm" method='post'  class="m-form-blank">
      <div class="panel panel-default">
        <div class="panel-heading">
		  <i class="glyphicon glyphicon-list"></i>
		  <spring:message code="scope-info.scope-info.list.title" text="列表"/>
		</div>
  <table id="orgGrid" class="table table-hover">
    <thead>
      <tr>
        <!-- <th width="10" class="table-check"><input type="checkbox" name="checkAll" onchange="toggleSelectedItems(this.checked)"></th> -->
        <th>班次名称</th>
        <th>考勤</th>
        <th>操作</th>
      </tr>
    </thead>

    <tbody>
      <c:forEach items="${page.result}" var="item">
	      <tr>
	        <%-- <td><input id="shiftId" type="checkbox" class="selectedItem" name="selectedItem" value="${item.id}"></td> --%>
	        <td>
	        	${item.shiftName}
	        </td>
	        <td>
			    ${item.startTime}--${item.endTime}
			 </td>
	        <td>
	        	<!-- 校验一下该班次是否是休息班次 -->
	        	<c:if test="${item.shiftType == 0}">
	        		<a href="update-shift-i.do?id=${item.id}" class="a-update"><spring:message code="core.list.edit" text="编辑"/></a>
			    	<a href="remove-shift-i.do?id=${item.id}" onclick="if(confirm('确认删除吗？')==false)return false;" class="a-update"><spring:message code="core.list.edit" text="删除"/></a>
	        	</c:if>
			 </td>
	      </tr>
      </c:forEach>
    </tbody>
  </table>
      </div>
</form>
	  <div>
	    <div class="m-page-info pull-left">
		  共100条记录 显示1到10条记录
		</div>

		<div class="btn-group m-pagination pull-right">
		  <button class="btn btn-default">&lt;</button>
		  <button class="btn btn-default">1</button>
		  <button class="btn btn-default">&gt;</button>
		</div>

	    <div class="clearfix"></div>
      </div>
      </section>
	  <!-- end of main -->
	</div>
  </body>
</html>

