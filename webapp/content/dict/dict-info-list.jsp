<%@page contentType="text/html;charset=UTF-8"%>
<%@include file="/taglibs.jsp"%>
<%pageContext.setAttribute("currentHeader", "dict");%>
<%pageContext.setAttribute("currentMenu", "dict");%>
<%pageContext.setAttribute("currentMenuName", "系统配置");%>
<%pageContext.setAttribute("currentChildMenu", "数据字典");%>
<!doctype html>
<html lang="en">

  <head>
    <%@include file="/common/meta.jsp"%>
    <title><spring:message code="dev.dict-type.list.title" text="麦联"/></title>
    <%@include file="/common/s3.jsp"%>

  </head>

  <body>
    <%@include file="/header/dict.jsp"%>

    <div class="row-fluid">
	  <%@include file="/menu/dict.jsp"%>

	  <!-- start of main -->
      <section id="m-main" class="col-md-10" style="padding-top:65px;">

<div class="panel panel-default">
  <div class="panel-heading">
	<i class="glyphicon glyphicon-list"></i>
    查询
	<div class="pull-right ctrl">
	  <a class="btn btn-default btn-xs"><i id="dict-typeSearchIcon" class="glyphicon glyphicon-chevron-up"></i></a>
    </div>
  </div>
  <div class="panel-body">

		  <form name="dict-typeForm" method="post" action="dict-type-list.do" class="form-inline">
		    <label for="dict-type_name"><spring:message code='dict-type.dict-type.list.search.name' text='名称'/>:</label>
		    <input type="text" id="dict-type_name" name="filter_LIKES_name" value="${param.filter_LIKES_name}" class="form-control">
			<button class="btn btn-default a-search" onclick="document.dict-typeForm.submit()">查询</button>&nbsp;
		  </form>

		</div>
	  </div>

      <div style="margin-bottom: 20px;">
	    <div class="pull-left btn-group" role="group">
		  <button class="btn btn-default a-insert" onclick="location.href='dict-info-input.do?typeId=${typeId}'">新建</button>
		<%--  <button class="btn btn-default a-remove" onclick="table.removeAll()">删除</button>--%>
	<%--	  <button class="btn btn-default a-export" onclick="table.exportExcel()">导出</button>--%>
		</div>

		 <div class="clearfix"></div>
	  </div>

<form id="dict-infoGridForm" name="dict-infoGridForm" method='post' a class="m-form-blank">
      <div class="panel panel-default">
        <div class="panel-heading">
		  <i class="glyphicon glyphicon-list"></i>
		  <spring:message code="scope-info.scope-info.list.title" text="列表"/>
		</div>

  <table id="dict-infoGrid" class="table table-hover">
    <thead>
      <tr>
        <th class="sorting" name="id"><spring:message code="dict-info.dict-info.list.id" text="编号"/></th>
        <th class="sorting" name="name"><spring:message code="dict-info.dict-info.list.name" text="名称"/></th>
        <th class="sorting" name="value">数据</th>
        <th width="80">&nbsp;</th>
      </tr>
    </thead>

    <tbody>
      <c:forEach items="${dictInfos}" var="item">
      <tr>
        <td>${item.id}</td>
        <td>${item.name}</td>
        <td>${item.value}</td>
        <td>
          <a href="dict-info-input.do?id=${item.id}&typeId=${param.typeId}" class="a-update">编辑</a>
          <a href="dict-info-remove.do?id=${item.id}&typeId=${param.typeId}" class="a-update">删除</a>
        </td>
      </tr>
      </c:forEach>
    </tbody>
  </table>


      </div>
</form>



      <div class="m-spacer"></div>

      </section>
	  <!-- end of main -->
	</div>

  </body>

</html>

