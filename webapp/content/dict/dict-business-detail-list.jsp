<%@page contentType="text/html;charset=UTF-8"%>
<%@include file="/taglibs.jsp"%>
<%pageContext.setAttribute("currentHeader", "dict");%>
<%pageContext.setAttribute("currentMenu", "dict");%>
<%pageContext.setAttribute("currentMenuName", "系统配置");%>
<%pageContext.setAttribute("currentChildMenu", "业务类型明细");%>
<!doctype html>
<html lang="en">
<head>
<%@include file="/common/meta.jsp"%>
<title><spring:message code="dev.dict-type.list.title" text="麦联" /></title>
<%@include file="/common/s3.jsp"%>
<script type="text/javascript" src="${cdnPrefix}/popwindialog/popwin.js"></script>
<script type="text/javascript">
        var config = {
            id: 'dict-typeGrid',
            pageNo: ${page.pageNo},
            pageSize: ${page.pageSize},
            totalCount: ${page.totalCount},
            resultSize: ${page.resultSize},
            pageCount: ${page.pageCount},
            orderBy: '${page.orderBy == null ? "" : page.orderBy}',
            asc: ${page.asc},
            params: {
                'filter_LIKES_busiDetail': '${param.filter_LIKES_busiDetail}',
                'filter_INS_bpmProcessId':'${param.filter_INS_bpmProcessId}',
                'filter_LIKES_businessType':'${param.filter_LIKES_businessType}'
            },
            selectedItemClass: 'selectedItem',
            gridFormId: 'dict-typeGridForm',
            exportUrl: 'dict-type-export.do',
            positionShowUrl: "${tenantPrefix}/rs/business/bussiness-detail-positionshow",//业务类型明细岗位显示
        };

        var table;
        $(function () {
            table = new Table(config);
            table.configPagination('.m-pagination');
            table.configPageInfo('.m-page-info');
            table.configPageSize('.m-page-size');
        });
        
        function openDialog(busDetailId){
			popWin.scrolling="auto";
			popWin.showWin("800"
    			,"480"
    			,"设置多分支流程条件"
    			,"${tenantPrefix}/dict/dict-business-detail-set-contidion.do?id="+busDetailId);
	    }
        //设置细分是否属于大区
        function setArea(busDetailId){
			popWin.scrolling="auto";
			popWin.showWin("800"
    			,"480"
    			,"设置大区"
    			,"${tenantPrefix}/dict/dict-business-detail-set-contidion-area.do?id="+busDetailId);
	    }
        
    </script>
<script type="text/javascript"
	src="${cdnPrefix}/bootbox/bootbox.min1.js"></script>
<script type="text/javascript" src="${cdnPrefix}/business/business.js"></script>
<style>
body {
	padding-right: 0px !important;
}

th {
	white-space: nowrap
}
</style>
</head>

<body>
	<%@include file="/header/dict.jsp"%>

	<div class="row-fluid">
		<%@include file="/menu/dict.jsp"%>
		<!-- start of main -->
		<section id="m-main" class="col-md-10" style="padding-top: 65px;">


			<div class="panel panel-default">
				<div class="panel-heading">
					查询
					<div class="pull-right ctrl">
						<a class="btn btn-default btn-xs"><i
							id="workReportInfoSearchIcon"
							class="glyphicon glyphicon-chevron-up"></i></a>
					</div>
				</div>
				<div class="panel-body">
					<form name="dict-typeGridForm" method="post"
						action="dict-business-detail-list.do" class="form-inline">
						<label for="B_businessType">业务类型：</label> <input type="text"
							id="B_businessType" name="filter_LIKES_businessType"
							value="${param.filter_LIKES_businessType}" class="form-control">
						<label for="B_busiDetail">业务类型细分：</label> <input type="text"
							id="B_busiDetail" name="filter_LIKES_busiDetail"
							value="${param.filter_LIKES_busiDetail}" class="form-control">
						<label for="bpmProcessId">流程名称：</label> <input type="text"
						id="bpmProcessId" name="filter_INS_bpmProcessId"
						value="${param.filter_INS_bpmProcessId}" class="form-control">
						<button id="btn_Search" class="btn btn-default a-search"
							onclick="document.dict-typeGridForm.submit()">查询</button>
						&nbsp;
					</form>
				</div>
			</div>
			<div style="margin-bottom: 20px;">
				<div class="pull-left btn-group" role="group">
					<button class="btn btn-default a-insert"
						onclick="location.href='dict-business-detail-new.do'">新建
					</button>
					<!-- <button class="btn btn-default a-remove" onclick="table.removeAll()">删除</button> -->
				</div>
				<div style="margin-bottom: 20px;">
					<div class="pull-right">
						每页显示 <select class="m-page-size form-control"
							style="display: inline; width: auto;">
							<option value="10">10</option>
							<option value="20">20</option>
							<option value="50">50</option>
						</select> 条
					</div>
					<div class="clearfix"></div>
				</div>
				<div class="clearfix"></div>
			</div>

			<form id="dict-typeGridForm" name="dict-typeGridForm" method='post'
				action="dict-business-type-remove.do" class="m-form-blank">
				<div class="panel panel-default">
					<div class="panel-heading">
						<i class="glyphicon glyphicon-list"></i> 列表
					</div>

					<table id="dict-typeGrid" class="table table-hover ">
						<thead>
							<tr>
								<th name="id">编号</th>
								<th name="busiType">业务类型</th>
								<th name="busiDetail">业务类型细分</th>
								<th name="title">标题</th>
								<th name=bpmProcessId>流程定义</th>
								<th name="postID">岗位</th>
								<th name="formID">表单</th>
								<th name="createTime">创建时间</th>
								<th>操作</th>
							</tr>
						</thead>

						<tbody>
							<c:forEach items="${page.result}" var="item">
								<tr>
									<td>${item.id}</td>
									<td>${item.businessType}</td>
									<td>${item.busiDetail}</td>
									<td>${item.title}</td>
									<td><tags:bpmName2 bpmProcessId="${item.bpmProcessId}" /></td>
									<td><a href="javascript:"
										onclick='positionShow("${item.id}")'>[查看]</a></td>
									<%-- <td><tags:post detailID="${item.id}"/></td>--%>
									<td>${item.formName}</td>
									<td>${item.createTime}</td>
									<td>
										<a href="dict-business-detail-input.do?id=${item.id}" class="a-update">编辑</a>
										<a href="javascript:" onclick='openDialog(${item.id})'>流程设置</a>
										<a href="dict-business-detail-linkSetting.do?id=${item.id}">审批环节设置</a>
										<a href="javascript:" onclick='setArea(${item.id})'>大区设置</a>
									</td>
								</tr>
							</c:forEach>
						</tbody>
					</table>
				</div>
			</form>

			<div>
				<div class="m-page-info pull-left">共100条记录 显示1到10条记录</div>

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

