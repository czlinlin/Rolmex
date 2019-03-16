<%@page contentType="text/html;charset=UTF-8" %>
<%@include file="/taglibs.jsp" %>
<%pageContext.setAttribute("currentHeader", "pim");%>
<%pageContext.setAttribute("currentMenu", "msg");%>
<!doctype html>
<html lang="en">

<head>
    <%@include file="/common/meta.jsp" %>
    <title>麦联</title>
    <%@include file="/common/s3.jsp" %>
    <script type="text/javascript">
        var config = {
            id: 'pimRemindGrid',
            pageNo: ${page.pageNo},
            pageSize: ${page.pageSize},
            totalCount: ${page.totalCount},
            resultSize: ${page.resultSize},
            pageCount: ${page.pageCount},
            orderBy: '${page.orderBy == null ? "" : page.orderBy}',
            asc: ${page.asc},
            params: {
                'filter_LIKES_content': '${param.filter_LIKES_content}'
            },
            selectedItemClass: 'selectedItem',
            gridFormId: 'pimRemindGridForm',
            exportUrl: 'pim-info-export.do'
        };

        var table;

        $(function () {
            table = new Table(config);
            table.configPagination('.m-pagination');
            table.configPageInfo('.m-page-info');
            table.configPageSize('.m-page-size');
        });
    </script>
    <style type="text/css">
        body {
            padding-right: 0px !important;
        }

        .mytable {
            /*table-layout: fixed;*/
            border: 0px;
            margin: 0px;
            border-collapse: collapse;
            width: 100%;
        }

        .mytable tr td .workTask_title {
            width: 400px;
            display: block;
            overflow: hidden;
        }

        .table {
            width: 100%;
        }

        .mytable tr td, .mytable tr td .rwop {
            text-overflow: ellipsis; /* for IE */
            -moz-text-overflow: ellipsis; /* for Firefox,mozilla */
            overflow: hidden;
            white-space: nowrap;
            border: 0px solid;
            text-align: left
        }
    </style>
</head>

<body>
<%@include file="/header/pim3.jsp" %>

<div class="row-fluid">
    <%@include file="/menu/pim3.jsp" %>

    <!-- start of main -->
    <section id="m-main" class="col-md-10" style="padding-top:65px;">

        <div class="panel panel-default">
            <div class="panel-heading">
                <i class="glyphicon glyphicon-list"></i>
                查询
                <div class="pull-right ctrl">
                    <a class="btn btn-default btn-xs"><i id="orgSearchIcon" class="glyphicon glyphicon-chevron-up"></i></a>
                </div>
            </div>
            <div class="panel-body">
                <form name="msgForm" method="post" action="msg-info-listSent.do" class="form-inline">
                    <label for="subject">标题:</label>
                    <input type="text" id="msgInfo_name" name="filter_LIKES_name" value="${param.filter_LIKES_name}"
                           class="form-control">
                    <label for="org_name">状态:</label>
                    <select id="msgInfo_Status" class="form-control" name="filter_EQI_status" title="">
                        <option value="" ${param.filter_EQI_status==""?"selected='selected'":""}>全部</option>
                        <option value="0"  ${param.filter_EQI_status=="0"?"selected='selected'":""}>未读</option>
                        <option value="1" ${param.filter_EQI_status=="1"?"selected='selected'":""}>已读</option>
                    </select>

                    <button class="btn btn-default a-search" onclick="document.msgForm.submit()">查询</button>
                    &nbsp;
                </form>
            </div>

        </div>

        <form id="pimRemindGridForm" name="pimRemindGridForm" method='post' action="pim-info-remove.do"
              class="m-form-blank mytable">
            <div class="panel panel-default">
                <div class="panel-heading">
                    发件箱
                </div>
                <table id="pimRemindGrid" class="table table-hover">
                    <thead>
                    <tr>
                        <th width="10" class="m-table-check"><input type="checkbox" name="checkAll"
                                                                    onchange="toggleSelectedItems(this.checked)"></th>
                        <th class="sorting" name="name">标题</th>
                        <th class="sorting" name="name">发件人</th>
                        <th class="sorting" name="name">发送时间</th>
                        <th class="sorting" name="name">状态</th>
                    </tr>
                    </thead>

                    <tbody>
                    <c:forEach items="${page.result}" var="item">
                        <tr>
                            <td><input type="checkbox" class="selectedItem a-check" name="selectedItem"
                                       value="${item.id}"></td>
                            <td><a class="workTask_title rwop" title="${item.name}" href="msg-info-view.do?id=${item.id}">${item.name}</a></td>
                            <td><tags:user userId="${item.senderId}"/></td>
                            <td><fmt:formatDate value="${item.createTime}" type="both"/></td>
                            <td>${item.status == 0 ? '未读' : '已读'}</td>
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
