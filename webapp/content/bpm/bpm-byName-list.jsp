<%@page contentType="text/html;charset=UTF-8"%>
<%@include file="/taglibs.jsp"%>
<%pageContext.setAttribute("currentHeader", "bpm-console");%>
<%pageContext.setAttribute("currentMenu", "bpm-category");%>
<%pageContext.setAttribute("currentMenuName", "流程管理");%>
<%pageContext.setAttribute("currentChildMenu", "流程配置");%>
<!doctype html>
<html lang="en">

  <head>
    <%@include file="/common/meta.jsp"%>
    <title><spring:message code="dev.bpm-process.list.title" text="麦联"/></title>
    <%@include file="/common/s3.jsp"%>
    <script type="text/javascript" src="${cdnPrefix}/userpicker3-v2/postForPersonInFo.js"></script>
    <link type="text/css" rel="stylesheet" href="${cdnPrefix}/userpicker3-v2/userpicker.css">
    <script type="text/javascript">
var config = {
    id: 'bpm-processGrid',
    pageNo: ${page.pageNo},
    pageSize: ${page.pageSize},
    totalCount: ${page.totalCount},
    resultSize: ${page.resultSize},
    pageCount: ${page.pageCount},
    orderBy: '${page.orderBy == null ? "" : page.orderBy}',
    asc: ${page.asc},
    params: {
        'filter_LIKES_name': '${param.filter_LIKES_name}',
        'filter_LIKES_byName': '${param.filter_LIKES_byName}',
        'filter_LIKES_postId': '${param.filter_LIKES_postId}',
        'filter_LIKES_postName': '${param.filter_LIKES_postName}'
    },
	selectedItemClass: 'selectedItem',
	gridFormId: 'bpm-processGridForm',
	exportUrl: 'bpm-process-export.do'
};

var table;

$(function() {
	table = new Table(config);
    table.configPagination('.m-pagination');
    table.configPageInfo('.m-page-info');
    table.configPageSize('.m-page-size');
    
    
    
  	//岗位
    createUserPickerForPersonInfo({
        modalId: 'userPickerForPersonInfo',
        targetId: 'postDiv', //这个是点击哪个 会触发弹出窗口
        showExpression: true,
        multiple: false,
        searchUrl: '${tenantPrefix}/rs/user/search',
        // treeNoPostUrl: '${tenantPrefix}/rs/party/treeNoPost?partyStructTypeId=1',
        treeNoPostUrl: '${tenantPrefix}/party/asyncTree.do?partyStructTypeId=1&notViewPost=true&notAuth=false',
        childPostUrl: '${tenantPrefix}/rs/party/searchPost'
    });
});



    </script>
  </head>

  <body>
    <%@include file="/header/bpm-console.jsp"%>

    <div class="row-fluid">
	  <%@include file="/menu/bpm-console.jsp"%>

	  <!-- start of main -->
      <section id="m-main" class="col-md-10" style="padding-top:65px;">
		<ul class="breadcrumb">
	    <li><a href="bpm-process-list.do">流程配置</a></li>
	    <!-- <li class="active"></li>  -->
	    </ul>
<div class="panel panel-default">
  <div class="panel-heading">
	<i class="glyphicon glyphicon-list"></i>
    查询
	<div class="pull-right ctrl">
	  <a class="btn btn-default btn-xs"><i id="bpm-processSearchIcon" class="glyphicon glyphicon-chevron-up"></i></a>
    </div>
  </div>
  <div class="panel-body">

		  <form name="bpm-processForm" method="post" action="bpm-byName-list.do" class="form-inline">
		    <%-- <label for="bpm-process_name"><spring:message code='bpm-process.bpm-process.list.search.name' text='流程'/>:</label>
		    <input type="text" id="bpm-process_name" name="filter_LIKES_name" value="${param.filter_LIKES_name}" class="form-control">
		    &nbsp&nbsp&nbsp&nbsp --%>
		    <label for="bpm-process_byName"><spring:message code='bpm-process.bpm-process.list.search.name' text='别名'/>:</label>
		    <input type="text" id="bpm-process_byName" name="filter_LIKES_byName" value="${param.filter_LIKES_byName}" class="form-control">
		    &nbsp&nbsp&nbsp&nbsp
			<%-- <label for="userPickerForPersonInfo"><spring:message code='bpm-process.bpm-process.list.search.name' text='岗位'/>:</label>
		    <!-- <div class="col-sm-5 userPickerForPersonInfo"> -->
                 <div class="input-group userPickerForPersonInfo">
                    <input id="_task_name_key" type="hidden" name="filter_LIKES_postId"
                           value="${param.filter_LIKES_postId}">
                    <input type="text" name="filter_LIKES_postName" id="postName"
                           value="${param.filter_LIKES_postName}" class="form-control" readonly placeholder="点击右侧图标选择岗位" >
                    <div id='PersonInfoDiv' class="input-group-addon"><i class="glyphicon glyphicon-user"></i></div>
                </div> 
            <!-- </div> -->
			&nbsp&nbsp&nbsp&nbsp --%>
			<button class="btn btn-default a-search" onclick="document.bpm-processForm.submit()">查询</button>&nbsp;
		  </form>

		</div>
	  </div>

      <div style="margin-bottom: 20px;">
	    <%-- <div class="pull-left btn-group" role="group">
		  <button class="btn btn-default a-insert" onclick="location.href='bpm-process-input.do'">新建</button>
		  <button class="btn btn-default a-remove" onclick="table.removeAll()">删除</button>
		  <c:if test="${!empty isShow }">
		  		<button class="btn btn-default a-export" onclick="location.href='${tenantPrefix}/bpm/bpm-byName-list.do'">别名统计</button>
		  </c:if>
		  <button class="btn btn-default a-export" onclick="table.exportExcel()">导出</button>
		</div> --%>

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

<form id="bpm-processGridForm" name="bpm-processGridForm" method='post' action="bpm-process-remove.do" class="m-form-blank">
      <div class="panel panel-default">
        <div class="panel-heading">
		  <i class="glyphicon glyphicon-list"></i>
		  <spring:message code="scope-info.scope-info.list.title" text="列表"/>
		</div>


    <table id="bpmProcessGrid" class="table table-hover">
      <thead>
        <tr>
          <th width="10" style="text-indent:0px;text-align:center;"><input type="checkbox" name="checkAll" onchange="toggleSelectedItems(this.checked)"></th>
          <th class="sorting" name="byName">别名</th>
          <th class="sorting" name="count">条数</th>
        </tr>
      </thead>
      <tbody>
        <c:forEach items="${page.result}" var="item">
        <tr>
          <td><input type="checkbox" class="selectedItem a-check" name="selectedItem" value="${item.id}"></td>
          <td>${item.byName}</td>
          <td>${item.count}</td>
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

