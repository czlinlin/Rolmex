<%@page contentType="text/html;charset=UTF-8"%>
<%@include file="/taglibs.jsp"%>
<%pageContext.setAttribute("currentHeader", "quit");%>
<%pageContext.setAttribute("currentMenu", "quit");%>
<%pageContext.setAttribute("currentChildMenu", "员工列表");%>
<%pageContext.setAttribute("currentMenuName", "人力资源");%>
<!doctype html>
<html lang="en">

  <head>
    <%@include file="/common/meta.jsp"%>
    <title><spring:message code="dev.employee-info.list.title" text="麦联"/></title>
    <%@include file="/common/s3.jsp"%>
    <script type="text/javascript">
		var config = {
		    id: 'person-infoGrid',
		    pageNo: ${page.pageNo},
		    pageSize: ${page.pageSize},
		    totalCount: ${page.totalCount},
		    resultSize: ${page.resultSize},
		    pageCount: ${page.pageCount},
		    orderBy: '${page.orderBy == null ? "" : page.orderBy}',
		    asc: ${page.asc},
		    params: {
		        'filter_LIKES_p.FULL_NAME': '${param.filter_LIKES_p.FULL_NAME}',
                'isSearch': true
		    },
			selectedItemClass: 'selectedItem',
			gridFormId: 'person-infoGridForm',
			exportUrl: 'person-info-export.do'
		};

		var table;
		
		$(function() {
			table = new Table(config);
		    table.configPagination('.m-pagination');
		    table.configPageInfo('.m-page-info');
		    table.configPageSize('.m-page-size');
		});
		
		// 职员离职
		function resume(id) {
			if (confirm('确定要将此职员复职吗？')) {
				$('#person-infoGridForm').attr('action', '${tenantPrefix}/user/person-info-input-resume.do?id=' + id);
				$('#person-infoGridForm').submit();
				return true;
			} else {
				return false;
			}
		}
    </script>
  </head>

  <body>


    <div class="row-fluid">
	<c:if test="${not empty flashMessages}">
		<div id="m-success-message" style="display:none;">
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
      <section id="m-main" class="col-md-12" style="padding-top:3px;">
		
		<ul class="breadcrumb">
		    <li><a href="person-info-quit-list.do?isSearch=true">离职员工管理</a></li>
		    <li class="active"></li>
		</ul>
		
		<div class="panel panel-default">
		  <div class="panel-heading">
			<i class="glyphicon glyphicon-list"></i>
		    查询
			<div class="pull-right ctrl">
			  <a class="btn btn-default btn-xs"><i id="employee-infoSearchIcon" class="glyphicon glyphicon-chevron-up"></i></a>
		    </div>
		  </div>
  		  <div class="panel-body">
		    <form name="person-infoForm" method="post" action="person-info-quit-list.do?isSearch=true" class="form-inline">
		      <label for="person-info_name"><spring:message code='employee-info.employee-info.list.search.name' text='名称'/>:</label>
		      <input type="text" id="person-info_name" name="filter_LIKES_p.FULL_NAME" value="${param['filter_LIKES_p.FULL_NAME']}" class="form-control">
		      <input type="hidden" id="partyStructTypeId" name="partyStructTypeId" value="${partyStructTypeId}" class="form-control">
			  <button class="btn btn-default a-search" onclick="document.person-infoForm.submit()">查询</button>&nbsp;
		    </form>
		  </div>
	   </div>

      <div style="margin-bottom: 20px;">
	    <div class="pull-left btn-group" role="group">
		    <!-- <button class="btn btn-default a-insert" onclick="location.href='person-info-input.do?partyEntityId=${partyEntityId}'">新建</button>
		    <button class="btn btn-default a-remove" onclick="table.removeAll()">删除</button> 
		    <button class="btn btn-default a-export" onclick="table.exportExcel()">导出</button> -->
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

<form id="person-infoGridForm" name="person-infoGridForm" method='post' action="person-info-remove.do?partyEntityId=${partyEntityId}" class="m-form-blank">
      <div class="panel panel-default">
        <div class="panel-heading">
		  <i class="glyphicon glyphicon-list"></i>
		  <spring:message code="scope-info.scope-info.list.title" text="列表"/>
		</div>

  <table id="person-infoGrid" class="table table-hover">
    <thead>
      <tr>
        <th class="sorting" name="id"><spring:message code="employee-info.employee-info.list.id" text="工号"/></th>
        <th class="sorting" name="name"><spring:message code="employee-info.employee-info.list.name" text="姓名"/></th>
        <th class="sorting" name="name">性别</th>
        <th class="sorting" name="name">电话</th>
        <th class="sorting" name="name">公司</th>
        <th class="sorting" name="name">上级部门</th>
        <th class="sorting" name="name">状态</th>
        <th class="sorting" name="name">离职日期</th>
        <th class="sorting" name="name">操作</th>

        <th width="80">&nbsp;</th>
      </tr>
    </thead>

    <tbody> 
      <c:forEach items="${page.result}" var="item">
      <tr>
        
        <td>${item.employeeNo}</td>
        <td>${item.fullName}</td>
        <td>
        	<c:if test="${item.gender == 1}">男</c:if>
        	<c:if test="${item.gender == 2}">女</c:if>
        </td>
        <td>${item.cellphone}</td>
        <td>${item.companyName}</td>
        <td>${item.departmentName}</td>
        <td>
			  已离职
		</td>
        <td>${item.quitTime}</td>
        <td>
          	<a href="javascript:void(0)" onclick="resume('${item.id}')" class="a-update">复职</a>
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

      <div class="m-spacer"></div>

      </section>
	  <!-- end of main -->
	</div>

  </body>

</html>

