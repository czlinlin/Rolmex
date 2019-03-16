<%--
  
  User: wanghan
  Date: 2017\8\21 0021
  Time: 14:55
 
--%>
<%@page contentType="text/html;charset=UTF-8" %>
<%@include file="/taglibs.jsp" %>
<%pageContext.setAttribute("currentHeader", "pim");%>
<%pageContext.setAttribute("currentMenu", "workReport");%>
<!doctype html>
<html lang="en">

<head>
    <%@include file="/common/meta.jsp" %>
    <title>麦联</title>
    <%@include file="/common/s3.jsp" %>
    <script type="text/javascript">
        var config = {
            id: 'workReportInfoGrid',
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
            gridFormId: 'workReportInfoGridForm',
            exportUrl: 'pim--export.do'
        };

        var table;

        $(function () {
            table = new Table(config);
            table.configPagination('.m-pagination');
            table.configPageInfo('.m-page-info');
            table.configPageSize('.m-page-size');
        });
    </script>

    <script type="text/javascript">
        function CcToMe() {

            alert("抄送人："+"${ccnames}");
            
        }
    </script>
</head>

<body>
<%@include file="/header/navbar.jsp"%>

<div class="row-fluid">
    <%@include file="/menu/sidebar.jsp"%>

    <!-- start of main -->
    <section id="m-main" class="col-md-10" style="padding-top:65px;">

        <div class="panel panel-default">
            <div class="panel-heading">
                查询
                <div class="pull-right ctrl">
                    <a class="btn btn-default btn-xs"><i id="workReportInfoSearchIcon" class="glyphicon glyphicon-chevron-up"></i></a>
                </div>
            </div>
            <div class="panel-body">
                <form name="workReportToMeForm" method="post" action="work-report-tome-list.do" class="form-inline">
                    <label for="workReportInfo_title">标题:</label>
                    <input type="text" id="workReportInfo_title" name="filter_LIKES_title" value="${param.workReportInfo_title}" class="form-control" maxlength="20">
                    <label for="workReportInfo_Type">类型:</label>
                    <select id="workReportInfo_Type"  class="form-control" name="workReportInfo_Type">
                        <option value="">全部</option>
                        <option value="1">周报</option>
                        <option value="2">月报</option>
                        <option value="3">年报</option>
                        <option value="4">其他</option>
                    </select>
                    <label for="workReportInfo_Status">状态:</label>
                    <select id="workReportInfo_Status"  class="form-control" name="workReportInfo_Status">
                        <option value="">全部</option>
                        <option value="0">未读</option>
                        <option value="1">已读</option>
                    </select>

                    <button class="btn btn-default a-search" id="btn_Search">查询</button>&nbsp;
                </form>
            </div>
        </div>

        <div style="margin-bottom: 20px;">
            <div class="pull-right">
                每页显示
                <select class="m-page-size form-control" style="display:inline;width:auto;">
                    <option value="10">10</option>
                    <option value="20">20</option>
                    <option value="50">50</option>
                </select>
                条
            </div>
        </div>

        <form id="workReportCcToMeForm" name="workReportCcToMeForm" method='post' action="work-report-info-cctome.do" class="m-form-blank">
            <div class="panel panel-default">
                <div class="panel-heading">
                    列表
                </div>
                <table id="workReportCcToMe" class="table table-hover">
                    <thead>
                    <tr>
                        <th>标题</th>
                        <th>类型</th>
                        <th>状态</th>
                        <th>抄送人</th>
                        <th>汇报时间</th>
                        <th>汇报人</th>
                        <th>操作</th>
                        <th width="80">&nbsp;</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach items="${page.result}" var="infos">
                        <tr>
                            <td><a href="#"> ${infos.title}</a></td>
                            <td>
                                <c:if test='${infos.type=="1"}'>周报</c:if>
                                <c:if test='${infos.type=="2"}'>月报</c:if>
                                <c:if test='${infos.type=="3"}'>年报</c:if>
                                <c:if test='${infos.type=="4"}'>其他</c:if>
                            </td>
                            <td>
                                <c:if test='${infos.infoStatus=="0"}'>未读</c:if>
                                <c:if test='${infos.infoStatus=="1"}'>已读</c:if>
                            </td>
                            <td><a href="javascript:;" onclick="CcToMe()">[查看]</a></td>
                            <td>${infos.reportDate}</td>
                            <td><tags:user userId="${infos.userId}"/></td>
                            <td>打印</td>
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
