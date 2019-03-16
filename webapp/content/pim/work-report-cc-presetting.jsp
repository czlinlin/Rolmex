<%@page contentType="text/html;charset=UTF-8" %>
<%@include file="/taglibs.jsp" %>
<%pageContext.setAttribute("currentHeader", "pim");%>
<%pageContext.setAttribute("currentMenu", "pim");%>
<%pageContext.setAttribute("currentChildMenu", "汇报条线管理");%>
<!doctype html>
<html lang="en">
<head>
    <%@include file="/common/meta.jsp" %>
    <title>麦联</title>
    <%@include file="/common/s3.jsp" %>
</head>
<body>

<%@include file="/header/version.jsp" %>

<div class="row-fluid">
    <%@include file="/menu/version.jsp" %>
    <section id="m-main" class="col-md-10" style="padding-top:65px;">
        <%-- <div class="panel panel-default">
            <div class="panel-heading">
                <i class="glyphicon glyphicon-list"></i>
               	 查询
            </div>
            <div class="panel-body">
                <form name="versionForm" method="post" action="version-info-list.do" class="form-inline">
                    <label>版本号:</label>
                    <input type="text" id="version_versioncode" name="filter_LIKES_versioncode"
                           value="${param.filter_LIKES_versioncode}" class="form-control">
                    <button id="btn_Search" class="btn btn-default a-search" onclick="document.versionForm.submit()">查询</button>
                    &nbsp;
                </form>
            </div>
        </div> --%>

        <div style="margin-bottom: 20px;">
            <div class="pull-left btn-group" role="group">
                <button class="btn btn-default a-insert" onclick="location.href='work-report-cc-presetting-node-input.do'">新建</button>
            </div>
            <div class="clearfix"></div>
        </div>

        <form id="version-infoGridForm" name="version-infoGridForm" method='post'
              <%--action="version-info-remove.do?id=${page.id}" --%>class="m-form-blank">
            <div class="panel panel-default">
                <div class="panel-heading">
                    <i class="glyphicon glyphicon-list"></i>列表
                </div>
                <table id="version-infoGrid" class="table table-hover">
                    <thead>
                    <tr>
                        <th>序号</th>
                        <th>条线名称</th>
                        <th>条线路径</th>
                        <th>状态</th>
                        <th>操作</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach items="${ccPresettingList}" var="item" varStatus="status">
                        <tr>
                        	<td>${status.index+1}</td>
                            <td>${item.title}</td>
                            <td>${item.node_title}</td>
                            <td>${item.status=="2"?"<font color='red'>禁用</font>":"正常"}</td>
                            <td>
                                <a href="work-report-cc-presetting-node-input.do?id=${item.id}">修改</a>
                                &emsp;
                                <a href="javascript:" onclick="fnDel(${item.id})">删除</a>
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
<script type="text/javascript" src="${cdnPrefix}/bootbox/bootbox.min1.js"></script>
<script>
	function fnDel(id){
		var confirmDialog=bootbox.confirm({
		    message: "确认删除此条线吗，删除后条线下节点也同时被删除？",
		    buttons: {
		        confirm: {
		            label: '确认',
		            className: 'btn-success'
		        },
		        cancel: {
		            label: '取消',
		            className: 'btn-danger'
		        }
		    },
		    callback: function (result) {
		    	if (!result) return;
		    	
		        confirmDialog.modal('hide');
                var loading = bootbox.dialog({
                    message: '<p class="text-center"><i class="fa fa-spin fa-spinner"></i>正在执行...</p>',
                    size: 'small',
                    closeButton: false
                });
                $.ajax({
                    url: "${tenantPrefix}/rs/pim/work-report-cc-presetting-remove",
                    type: "POST",
                    data: {id:id},
                    timeout: 10000,
                    success: function (data) {
                        loading.modal('hide');
                        if (data == undefined || data == null || data == "") {
                            bootbox.alert("操作失败");
                            return;
                        }

                        if (data.code == "200") {
                            var tip = bootbox.alert(
                                {
                                    message: "操作成功！",
                                    callback: function () {
                                    	location.href=location.href;
                                        tip.modal('hide');
                                    }
                                });
                        }
                        else
                            bootbox.alert(data.message);
                        return;
                    },
                    error: function (XMLHttpRequest, textStatus, errorThrown) {
                    	loading.modal('hide');
                    	bootbox.alert("[" + XMLHttpRequest.status + "]error，请求失败");
                    },
                    complete: function (xh, status) {
                    	loading.modal('hide');
                        if (status == "timeout")
                            bootbox.alert("请求超时");
                    }
                });
		    }
		});
	}
</script>
</body>
</html>
