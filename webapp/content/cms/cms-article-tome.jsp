<%--
  
  User: wanghan
  Date: 2017\10\11 0011
  Time: 10:43
 
--%>
<%@page contentType="text/html;charset=UTF-8" %>
<%@include file="/taglibs.jsp" %>
<%
    pageContext.setAttribute("currentHeader", "cms");
%>
<%
    pageContext.setAttribute("currentMenu", "cms");
%>
<!doctype html>
<html lang="en">

<head>
    <%@include file="/common/meta.jsp" %>
    <title><spring:message code="dev.cms-article.list.title"
                           text="麦联"/></title>
    <%@include file="/common/s3.jsp" %>
    <script type="text/javascript">
        var config = {
            id: 'cms-articleGrid',
            pageNo: ${page.pageNo},
            pageSize: ${page.pageSize},
            totalCount: ${page.totalCount},
            resultSize: ${page.resultSize},
            pageCount: ${page.pageCount},
            orderBy: '${page.orderBy == null ? "" : page.orderBy}',
            asc: ${page.asc},
            params: {
                'filter_LIKES_title': '${param.filter_LIKES_title}',
                'filter_LED_publishTime': '${param.filter_LED_publishTime}'

            },
            selectedItemClass: 'selectedItem',
            gridFormId: 'cms-articleGridForm',
        };

        var table;

        $(function () {
            table = new Table(config);
            table.configPagination('.m-pagination');
            table.configPageInfo('.m-page-info');
            table.configPageSize('.m-page-size');
            //设置段时间
            var sectionJson = [{"begin": "#pickerStartTime", "end": "#pickerEndTime"}
            ];
            fnSectionPickerTime(sectionJson)
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
<%@include file="/header/pim3.jsp"%>

<div class="row-fluid">
    <%@include file="/menu/sidebar.jsp" %>

    <!-- start of main -->
    <section id="m-main" class="col-md-10" style="padding-top: 65px;">

        <div class="panel panel-default">
            <div class="panel-heading">
                <i class="glyphicon glyphicon-list"></i> 查询
                <div class="pull-right ctrl">
                    <a class="btn btn-default btn-xs"><i
                            id="cms-articleSearchIcon" class="glyphicon glyphicon-chevron-up"></i></a>
                </div>
            </div>
            <div class="panel-body">

                <form name="cms-articleForm" method="post"
                      action="cms-article-tome.do" class="form-inline">
                    <label>标题:</label> <input
                        type="text" id="cms-article_name" name="filter_LIKES_title"
                        value="${param.filter_LIKES_title}" class="form-control">

                    &nbsp;
                    &nbsp;
                    <label> 发布时间:</label>
                    <div id="pickerStartTime" class="input-group date">
                        <input style="width:160px;" type="text" name="filter_GED_publishTime"
                               value="${param.filter_GED_publishTime}" class="form-control required valid" readonly>
                        <span class="input-group-addon">
				<i class="glyphicon glyphicon-calendar"></i>
			</span>
                    </div>
                    至
                    <div id="pickerEndTime" class="input-group date">
                        <input style="width:160px;" type="text" name="filter_LED_publishTime"
                               value="${param.filter_LED_publishTime}" class="form-control required valid" readonly>
                        <span class="input-group-addon">
				<i class="glyphicon glyphicon-calendar"></i>
			</span>
                    </div>
                    <button class="btn btn-default a-search"
                            onclick="document.cms-articleForm.submit()">查询
                    </button>
                    &nbsp;
                </form>

            </div>
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

        <form id="cms-articleGridForm" name="cms-articleGridForm"
              method='post' action="cms-article-remove.do" class="m-form-blank mytable">
            <div class="panel panel-default">
                <div class="panel-heading">
                    <i class="glyphicon glyphicon-list"></i>
                    <spring:message code="scope-info.scope-info.list.title" text="列表"/>
                </div>


                <table id="cmsArticleGrid" class="table table-hover">
                    <thead>
                    <tr>
                        <th name="title">标题</th>
                     <%--   <th name="summary">摘要</th>--%>
                        <th name="userId">发布人</th>
                        <th name="publishTime">发布时间</th>
                        <th width="110">&nbsp;</th>
                    </tr>
                    </thead>

                    <tbody>
                    <c:forEach items="${page.result}" var="item">
                        <tr>
                            <td><a  class="workTask_title rwop" title="${item.title}" href="cms-article-meview.do?id=${item.id}"> ${item.title}</a></td>
                      <%--      <td>${item.summary}</td>--%>
                            <td><tags:user userId="${item.userId}"></tags:user></td>
                            <td><fmt:formatDate value="${item.publishTime}"
                                                type="both" pattern='yyyy-MM-dd HH:mm'/></td>
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

