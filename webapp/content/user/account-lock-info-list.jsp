<%@page contentType="text/html;charset=UTF-8" %>
<%@include file="/taglibs.jsp" %>
<%pageContext.setAttribute("currentHeader", "lock");%>
<%pageContext.setAttribute("currentMenu", "lock");%>
<!doctype html>
<html lang="en">

<head>
    <%@include file="/common/meta.jsp" %>
    <title><spring:message code="dev.account-lock-info.list.title" text="麦联"/></title>
    <%@include file="/common/s3.jsp" %>
    <script type="text/javascript">
        var config = {
            id: 'account-lock-infoGrid',
            pageNo: ${page.pageNo},
            pageSize: ${page.pageSize},
            totalCount: ${page.totalCount},
            resultSize: ${page.resultSize},
            pageCount: ${page.pageCount},
            orderBy: '${page.orderBy == null ? "" : page.orderBy}',
            asc: ${page.asc},
            params: {
                'filter_LIKES_name': '${param.filter_LIKES_name}'
            },
            selectedItemClass: 'selectedItem',
            gridFormId: 'account-lock-infoGridForm',
            exportUrl: 'account-lock-info-export.do'
        };

        var table;

        $(function () {
            table = new Table(config);
            table.configPagination('.m-pagination');
            table.configPageInfo('.m-page-info');
            table.configPageSize('.m-page-size');
        });
    </script>
    <script type="text/javascript" src="${cdnPrefix}/bootbox/bootbox.min1.js"></script>
    <script type="text/javascript">
        var resetAll = function () {
            var dialog = bootbox.dialog({
                message: '<p class="text-center"><i class="fa fa-spin fa-spinner"></i>正在操作...</p>',
                size: 'small',
                closeButton: false
            });
            $.ajax({
                url: "${tenantPrefix}/rs/user/person-reset-alldefaultkey",
                type: "POST",
                data: [],
                timeout: 10000,
                success: function (data) {
                    dialog.modal('hide');
                    if (data != undefined && data != null && data != "") {
                        if (data.code == 200)
                            alert(data.message)
                        else
                            alert(data.message)
                    }
                },
                error: function (XMLHttpRequest, textStatus, errorThrown) {
                    dialog.modal('hide');
                    alert("[" + XMLHttpRequest.status + "]error，请求失败")
                },
                complete: function (xh, status) {
                    dialog.modal('hide');
                    if (status == "timeout")
                        bootbox.alert("请求超时");
                }
            });
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

        <br>

        <div class="panel panel-default">
            <div class="panel-heading">
                <i class="glyphicon glyphicon-list"></i>
                查询
                <div class="pull-right ctrl">
                    <a class="btn btn-default btn-xs"><i id="account-lock-infoSearchIcon"
                                                         class="glyphicon glyphicon-chevron-up"></i></a>
                </div>
            </div>
            <div class="panel-body">

                <form name="account-lock-infoForm" method="post" action="account-lock-info-list.do"
                      class="form-inline">
                    <label for="account-lock-info_displayName">名称：</label>
                    <input type="text" id="account-lock-info_name" name="filter_LIKES_displayName"
                           value="${param.filter_LIKES_displayName}" class="form-control">
                    <button class="btn btn-default a-search" onclick="document.account-lock-infoForm.submit()">查询
                    </button>
                    &nbsp;
                </form>

            </div>
        </div>
        <div style="margin-bottom: 20px;">
            <div class="pull-left btn-group" role="group">
                <!-- <button class="btn btn-default a-insert" onclick='resetAll()'>重置密码</button> -->
                <!-- <label style="color:gray;">(重置全部密码)</label> -->
                <%--
                  <button class="btn btn-default a-insert" onclick="location.href='account-lock-info-input.do'">新建</button>
                  <button class="btn btn-default a-remove" onclick="table.removeAll()">删除</button>
                  <button class="btn btn-default a-export" onclick="table.exportExcel()">导出</button>
                  --%>
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

        <form id="account-lock-infoGridForm" name="account-lock-infoGridForm" method='post'
              action="account-lock-info-remove.do" class="m-form-blank">
            <div class="panel panel-default">
                <div class="panel-heading">
                    <i class="glyphicon glyphicon-list"></i>
                    <spring:message code="scope-info.scope-info.list.title" text="列表"/>
                </div>

                <table id="cal-infoGrid" class="table table-hover">
                    <thead>
                    <tr>
                        <%--   <th class="sorting" name="id"><spring:message code="cal-info.cal-info.list.id" text="编号"/></th>--%>
                        <th class="sorting" name="name">用户</th>

                        <th class="sorting" name="name">锁定时间</th>
                        <th class="sorting" name="name">操作</th>
                    </tr>
                    </thead>

                    <tbody>
                    <c:forEach items="${page.result}" var="item">
                        <tr>
                                <%--   <td>${item.id}</td>--%>
                            <td>${item.displayName}</td>
                            <td><fmt:formatDate value="${item.lockTime}" type="both" pattern='yyyy-MM-dd HH:mm'/></td>
                            <td><a href="account-lock-info-unlock.do?id=${item.id}">解锁</a></td>
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

