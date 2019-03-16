<%--
  
  User: wanghan
  Date: 2017\9\29 0029
  Time: 16:23
 
--%>
<%@page contentType="text/html;charset=UTF-8" %>
<%@include file="/taglibs.jsp" %>
<%pageContext.setAttribute("currentHeader", "version");%>
<%pageContext.setAttribute("currentMenu", "version");%>
<!doctype html>
<html lang="en">
<head>
    <%@include file="/common/meta.jsp" %>
    <title>麦联</title>
    <%@include file="/common/s3.jsp" %>
    <script type="text/javascript" src="${cdnPrefix}/bootbox/bootbox.min1.js"></script>
    <script type="text/javascript">
        var config = {
            id: 'version-typeGrid',
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
            gridFormId: 'version-typeGridForm',
            versionDelUrl: "${tenantPrefix}/rs/version/version-info-del",
        };

        var table;

        $(function () {
            table = new Table(config);
            table.configPagination('.m-pagination');
            table.configPageInfo('.m-page-info');
            table.configPageSize('.m-page-size');
        });
    </script>

    <script type="text/javascript" src="${cdnPrefix}/version/version.js"></script>
</head>
<body>

<%@include file="/header/version.jsp" %>

<div class="row-fluid">
    <%@include file="/menu/version.jsp" %>


    <!-- start of main -->
    <section id="m-main" class="col-md-10" style="padding-top:65px;">

        <div class="panel panel-default">
            <div class="panel-heading">
                <i class="glyphicon glyphicon-list"></i>
                查询
                <div class="pull-right ctrl">
                    <%--  <a class="btn btn-default btn-xs"><i id="version-typeSearchIcon" class="glyphicon glyphicon-chevron-up"></i></a>--%>
                </div>
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
        </div>

        <div style="margin-bottom: 20px;">
            <div class="pull-left btn-group" role="group">
                <button class="btn btn-default a-insert" onclick="location.href='version-info-input.do'">新建</button>
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

        <form id="version-infoGridForm" name="version-infoGridForm" method='post'
              <%--action="version-info-remove.do?id=${page.id}" --%>class="m-form-blank">
            <div class="panel panel-default">
                <div class="panel-heading">
                    <i class="glyphicon glyphicon-list"></i>
                    列表
                </div>

                <table id="version-infoGrid" class="table table-hover">
                    <thead>
                    <tr>
                        <%-- <th width="10" class="m-table-check"><input type="checkbox" name="checkAll"
                                                                     onchange="toggleSelectedItems(this.checked)"></th>--%>
                        <th name="name">版本号</th>
                        <th name="value">备注</th>
                        <th name="value">保存时间</th>
                        <th name="value">操作</th>
                    </tr>
                    </thead>

                    <tbody>
                    <c:forEach items="${page.result}" var="page" varStatus="status">
                        <tr>
                                <%-- <td><input type="checkbox" class="selectedItem a-check" name="selectedItem"
                                            value="${page.id}"></td>--%>
                            <td><a href="version-info-detail.do?id=${page.id}">${page.versioncode}</a></td>
                            <td>${page.remarks}</td>
                            <td><fmt:formatDate value="${page.savetime}" type="both" pattern='yyyy-MM-dd HH:mm'/></td>
                            <td>
                                <a href="javascript:" onclick="fnVersionDel(${page.id})">删除</a>
                                &nbsp;&nbsp;
                                <c:if test="${status.index == 0}">
                                    <a href="version-info-modify.do?id=${page.id}" class="a-update">编辑</a>
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

        <div class="m-spacer"></div>

    </section>
    <!-- end of main -->
</div>
</body>
</html>
