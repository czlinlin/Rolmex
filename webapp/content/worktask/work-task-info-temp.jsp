<%--

  User: wanghan
  Date: 2017\8\30 0030
  Time: 15:13

--%>
<%@page contentType="text/html;charset=UTF-8" %>
<%@include file="/taglibs.jsp" %>
<%
    pageContext.setAttribute("currentHeader", "worktask");
%>
<%
    pageContext.setAttribute("currentMenu", "worktask");
%>
<!doctype html>
<html lang="en">

<head>
    <%@include file="/common/meta.jsp" %>
    <title>麦联</title>
    <%@include file="/common/s3.jsp" %>

    <!-- bootbox -->
    <script type="text/javascript" src="${cdnPrefix}/bootbox/bootbox.min1.js"></script>

    <script type="text/javascript">
        var config = {
            id: 'workTaskInfoGrid',
            pageNo: ${page.pageNo},
            pageSize: ${page.pageSize},
            totalCount: ${page.totalCount},
            resultSize: ${page.resultSize},
            pageCount: ${page.pageCount},
            orderBy: '${page.orderBy == null ? "" : page.orderBy}',
            asc: ${page.asc},
            params: {
                'filter_LIKES_title': '${param.filter_LIKES_title}',
                'filter_GED_starttime': '${param.filter_GED_starttime}',
                'filter_LED_starttime': '${param.filter_LED_starttime}',
                'filter_GED_plantime': '${param.filter_GED_plantime}',
                'filter_LED_plantime': '${param.filter_LED_plantime}',
                'filter_GED_publishtime': '${param.filter_GED_publishtime}',
                'filter_lED_publishtime': '${param.filter_lED_publishtime}'
            },
            selectedItemClass: 'selectedItem',
            gridFormId: 'workTaskInfoGridForm',
            exportUrl: 'work-task-info-export.do',
            //自定义JS
            taskPublishUrl: "${tenantPrefix}/rs/worktask/work-task-info-publish",
            taskCCUrl: "${tenantPrefix}/rs/worktask/work-task-info-cc",//抄送人
            taskRealDelUrl: "${tenantPrefix}/rs/worktask/work-task-info-realdel"
        };

        var table;

        $(function () {
            table = new Table(config);
            table.configPagination('.m-pagination');
            table.configPageInfo('.m-page-info');
            table.configPageSize('.m-page-size');

            //设置段时间
            var sectionJson = [{"begin": "#pickerStartTime", "end": "#pickerEndTime"},
                {"begin": "#pickerStartTimeP", "end": "#pickerEndTimeP"},
                {"begin": "#pickerStartTimeA", "end": "#pickerEndTimeA"}];
            fnSectionPickerTime(sectionJson)
        });
    </script>
    <script type="text/javascript" src="${cdnPrefix}/worktask/worktask.js"></script>
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
            width: 150px;
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
<%@include file="/header/navbar.jsp" %>

<div class="row-fluid">
    <%@include file="/menu/sidebar.jsp" %>

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
                <form name="workTaskInfoForm" method="post"
                      action="work-task-info-temp.do" class="form-inline">
                    <label for="workTaskInfo_title">标题:</label> <input
                        type="text" id="workTaskInfo_title"
                        name="filter_LIKES_title"
                        value="${param.filter_LIKES_title}" class="form-control">

                    &nbsp;&nbsp;
                    <label> 计划开始时间:</label>
                    <div id="pickerStartTime" class="input-group date">
                        <input style="width:160px;" type="text" name="filter_GED_starttime"
                               value="${param.filter_GED_starttime}" class="form-control required valid" readonly>
                        <span class="input-group-addon">
				<i class="glyphicon glyphicon-calendar"></i>
			</span>
                    </div>
                    至
                    <div id="pickerEndTime" class="input-group date">
                        <input style="width:160px;" type="text" name="filter_LED_starttime"
                               value="${param.filter_LED_starttime}" class="form-control required valid" readonly>
                        <span class="input-group-addon">
				<i class="glyphicon glyphicon-calendar"></i>
			</span>
                    </div>
                    <br> <br>
                    <label> 计划完成时间:</label>
                    <div id="pickerStartTimeP" class="input-group  date">
                        <input style="width:160px;" type="text" name="filter_GED_plantime"
                               value="${param.filter_GED_plantime}" class="form-control required valid" readonly>
                        <span class="input-group-addon">
				<i class="glyphicon glyphicon-calendar"></i>
			</span>
                    </div>
                    至
                    <div id="pickerEndTimeP" class="input-group  date">
                        <input style="width:160px;" type="text" name="filter_LED_plantime"
                               value="${param.filter_LED_plantime}" class="form-control required valid" readonly>
                        <span class="input-group-addon">
				<i class="glyphicon glyphicon-calendar"></i>
			</span>
                    </div>            &nbsp;&nbsp;

                    <label> 添加时间:</label>
                    <div id="pickerStartTimeA" class="input-group datetimepickerhour date">
                        <input style="width:140px;" type="text"  name="filter_GED_publishtime"
                               value="${param.filter_GED_publishtime}" class="form-control required valid" readonly>
                        <span class="input-group-addon">
				<i class="glyphicon glyphicon-calendar"></i>
			</span>
                    </div>
                    至
                    <div  id="pickerEndTimeA" class="input-group datetimepickerhour date">
                        <input style="width:140px;" type="text" name="filter_LED_publishtime"
                               value="${param.filter_LED_publishtime}" class="form-control required valid" readonly>
                        <span class="input-group-addon">
				<i class="glyphicon glyphicon-calendar"></i>
			</span>
                    </div>
                    <button id="btn_Search" class="btn btn-default a-search" type="submit">查询</button>
                    &nbsp;
                    &nbsp;
                </form>
            </div>
        </div>

        <div style="margin-bottom: 20px;">
            <div class="pull-left btn-group" role="group">
            </div>

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

        <form id="workTaskInfoGridForm" name="workTaskInfoGridForm "
              method='post' action="work-task-info-temp.do" class="m-form-blank" enctype="multipart/form-data">
            <div class="panel panel-default">
                <div class="panel-heading">列表</div>
                <table id="workTaskInfoGrid" class="table table-hover mytable">
                    <thead>
                    <tr>
                        <th>操作</th>
                        <th>标题</th>
                        <th>父任务/子任务</th>
                        <th>负责人</th>
                        <th>抄送</th>
                        <th>计划开始时间</th>
                        <th>计划完成时间</th>
                        <th>添加时间</th>
                    </tr>
                    </thead>

                    <tbody>
                    <c:forEach items="${page.result}" var="item">
                        <tr>

                            <td><a href="work-task-info-modify.do?id=${item.id}">修改</a>
                                &nbsp;&nbsp;<a href="javascript:" onclick="fnTaskRealDel(${item.id})"> 删除</a>
                                &nbsp;&nbsp;<a href="javascript:" onclick="fnTaskPublish(${item.id})">发布 </a>

                            <td>
                                <a class="workTask_title rwop" title="${item.title}"
                                   href="work-task-info-detail.do?id=${item.id}"> ${item.title}</a></td>
                                <%-- <td><a href="#">${}</a> &nbsp;/ &nbsp;<a href="#">${item.uppercodeshow}</a></td>--%>
                            <td>${item.parentshow} &nbsp;/ &nbsp;${item.childshow}</td>
                            <td><tags:isDelUser userId="${item.leader}"></tags:isDelUser></td>
                            <td>
                                [<a href="javascript:" onclick='showCCMan("${item.id}")'>查看</a>]
                            </td>
                            <td><fmt:formatDate value="${item.starttime}" type="both" pattern='yyyy-MM-dd HH:mm'/></td>
                            <td><fmt:formatDate value="${item.plantime}" type="both" pattern='yyyy-MM-dd HH:mm'/></td>
                            <td><fmt:formatDate value="${item.publishtime}" type="both"
                                                pattern='yyyy-MM-dd HH:mm'/></td>

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

    </section>
    <!-- end of main -->
</div>

</body>

</html>

