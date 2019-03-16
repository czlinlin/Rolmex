<%@page contentType="text/html;charset=UTF-8"%>
<%@include file="/taglibs.jsp"%>
<%
	pageContext.setAttribute("currentHeader", "dict");
%>
<%
	pageContext.setAttribute("currentMenu", "dict");
%>
<%
	pageContext.setAttribute("currentMenuName", "系统配置");
%>
<%
	pageContext.setAttribute("currentChildMenu", "业务类型明细");
%>
<!doctype html>
<html lang="en">

<head>
<%@include file="/common/meta.jsp"%>
<title><spring:message code="dev.dict-type.list.title" text="麦联" /></title>
<%@include file="/common/s3.jsp"%>
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
                'filter_LIKES_busiDetail': '${param.filter_LIKES_busiDetail}'
            },
            selectedItemClass: 'selectedItem',
            gridFormId: 'dict-typeGridForm',
            exportUrl: 'dict-type-export.do',
            busiDetailShowUrl: "${tenantPrefix}/rs/dict/bussiness-detail-busiDetailShow",//业务类型明细岗位显示
        };

        var table;

        $(function () {
            table = new Table(config);
            table.configPagination('.m-pagination');
            table.configPageInfo('.m-page-info');
            table.configPageSize('.m-page-size');
        });
        
        
        var busiDetailShow = function (id) {

            var dialog = bootbox.dialog({
                message: '<p class="text-center"><i class="fa fa-spin fa-spinner"></i>正在加载...</p>',
                size: 'small',
                closeButton: false
            });
            var html = '<div class="panel panel-default" style="max-height:500px;overflow-y:scroll;"><table class="table table-hover" style="width:100%;">';
            $.ajax({
                url: config.busiDetailShowUrl,
                type: "POST",
                data: {id: id},
                timeout: 10000,
                success: function (data) {
                    dialog.modal('hide');

                    if (data.code == 200) {
                        if (data == undefined || data == null || data == "" || data.data.length < 1)
                            html += '<tr><td colspan="2">没有业务类型</td></tr>'
                        else {
                            if (data.data.length > 0) {
                                $(data.data).each(function (i, item) {
                                    html += '<tr><td>' + item.typeName + '</td><td>' + item.busiDetailName + '</td></tr>'

                                })
                            }
                        }
                    }
                    html += "</table></div>";
                    busiDetailShowDialog(html);
                },
                error: function (XMLHttpRequest, textStatus, errorThrown) {
                    alert("请求超时")
                },
                complete: function (xh, status) {
                    dialog.modal('hide');
                    if (status == "timeout")
                        bootbox.alert("请求超时");
                }
            });
        }
        var busiDetailShowDialog = function (show) {
            bootbox.dialog({
                title: '当前岗位的业务类型明细',
                message: show,
                buttons: {
                    ok: {
                        label: "确定"
                    }
                }
            });
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
						<label for="B_busiDetail">业务类型细分：</label> <input type="text"
							id="B_busiDetail" name="filter_LIKES_busiDetail"
							value="${param.filter_LIKES_busiDetail}" class="form-control">
						<button id="btn_Search" class="btn btn-default a-search"
							onclick="document.dict-typeGridForm.submit()">查询</button>
						&nbsp;
					</form>
				</div>
			</div>
			<div style="margin-bottom: 20px;">
				<div class="pull-left btn-group" role="group">
					<button class="btn btn-default a-insert"
						onclick="location.href='dict-post-busidetail-new.do'">新建
					</button>
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
								<th name="postID">岗位</th>
								<th name="busiType">业务类型</th>
<!-- 								<th name="busiDetail">业务类型细分</th> -->
								<th name="createTime">创建时间</th>
							</tr>
						</thead>

						<tbody>
							<c:forEach items="${page.result}" var="item">
								<tr>
									<td>${item.post_id}</td>
									<td>${item.post_name}</td>
									
									<td><a href="javascript:"
										onclick='busiDetailShow("${item.post_id}")'>[查看]</a></td>
									
<%-- 									<td>${item.typeName}</td> --%>
<%-- 									<td>${item.busiDetailName}</td> --%>
 									<td>${item.createTime}</td> 
									<td><a href="dict-post-busidetail-input.do?id=${item.id}"
										class="a-update"><spring:message code="core.list.edit"
												text="编辑" /></a></td>
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

