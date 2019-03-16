<%@page contentType="text/html;charset=UTF-8" %>
<%@include file="/taglibs.jsp" %>
<%pageContext.setAttribute("currentHeader", "dict");%>
<%pageContext.setAttribute("currentMenu", "dict");%>
<%pageContext.setAttribute("currentMenuName", "系统配置");%>
<%pageContext.setAttribute("currentChildMenu", "业务类型");%>
<!doctype html>
<html lang="en">

<head>
    <%@include file="/common/meta.jsp" %>
    <title><spring:message code="dev.dict-type.list.title" text="麦联"/></title>
    <%@include file="/common/s3.jsp" %>
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
                'filter_LIKES_businesstype': '${param.filter_LIKES_businesstype}'
            },
            selectedItemClass: 'selectedItem',
            gridFormId: 'dict-typeGridForm',
            exportUrl: 'dict-type-export.do',
            fnShowUrl: "${tenantPrefix}/rs/business/bussiness-type-fnshow",//业务类型部门显示
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
    <script type="text/javascript" src="${cdnPrefix}/business/business.js"></script>
    <style>
        body {
            padding-right: 0px !important;
        }
        .hide{display:none;}
    </style>
</head>

<body>
<%@include file="/header/dict.jsp" %>

<div class="row-fluid">
    <%@include file="/menu/dict.jsp" %>

    <!-- start of main -->
    <section id="m-main" class="col-md-10" style="padding-top:65px;">
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
                      action="dict-business-type-list.do" class="form-inline">
                    <label for="B_businesstype">业务类型名称：</label> <input
                        type="text" id="B_businesstype"
                        name="filter_LIKES_businesstype"
                        value="${param.filter_LIKES_businesstype}" class="form-control">
                    <button id="btn_Search" class="btn btn-default a-search"
                            onclick="document.dict-typeGridForm.submit()">查询
                    </button>
                    &nbsp;
                </form>
            </div>
        </div>
        <div style="margin-bottom: 20px;">
            <div class="pull-left btn-group" role="group">
                <button class="btn btn-default a-insert" onclick="location.href='dict-business-type-new.do'">新建</button>
                <!-- <button class="btn btn-default a-remove" onclick="table.removeAll()">删除</button> -->
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

        <form id="dict-typeGridForm" name="dict-typeGridForm" method='post' action="dict-business-type-remove.do"
              class="m-form-blank">
            <div class="panel panel-default">
                <div class="panel-heading">
                    <i class="glyphicon glyphicon-list"></i>
                    <spring:message code="scope-info.scope-info.list.title" text="列表"/>
                </div>

                <table id="dict-typeGrid" class="table table-hover">
                    <thead>
                    <tr>
                        <th name="id">编号</th>
                        <th name="name">业务类型名称</th>
                        <th class="hide" name="formname">表单</th>
                        <th class="hide" name="department">部门</th>
                        <th name="enable">是否启用</th>
                        <th>操作</th>
                    </tr>
                    </thead>

                    <tbody>
                    <c:forEach items="${page.result}" var="item">
                        <tr>
                            <td>${item.id}</td>
                            <td>${item.businesstype}</td>
                            <td  class="hide">${item.formName}</td>
                            <td  class="hide"><a href="javascript:" onclick='fnShow("${item.id}")'>[查看]</a></td>
                            <td>${item.enable}</td>
                            <td>
                                <a href="dict-business-type-input.do?id=${item.id}" class="a-update"><spring:message
                                        code="core.list.edit" text="编辑"/></a>
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

