<%--
  
  User: wanghan
  Date: 2017\9\9 0009
  Time: 9:30
 
--%>
<%@page contentType="text/html;charset=UTF-8" %>
<%@include file="/taglibs.jsp" %>
<%
    pageContext.setAttribute("currentHeader", "project");
%>
<%
    pageContext.setAttribute("currentMenu", "project");
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
            id: 'projectInfoGrid',
            pageNo: ${page.pageNo},
            pageSize: ${page.pageSize},
            totalCount: ${page.totalCount},
            resultSize: ${page.resultSize},
            pageCount: ${page.pageCount},
            orderBy: '${page.orderBy == null ? "" : page.orderBy}',
            asc: ${page.asc},
            params: {
            	'filter_LIKES_title':'${param.filter_LIKES_title}',
            	'filter_GED_startdate':'${param.filter_GED_startdate}',
            	'filter_LED_startdate':'${param.filter_LED_startdate}',
            	'filter_GED_publishtime':'${param.filter_GED_publishtime}',
            	'filter_LED_publishtime':'${param.filter_LED_publishtime}',
            	'filter_GED_plandate':'${param.filter_GED_plandate}',
            	'filter_LED_plandate':'${param.filter_LED_plandate}'
            },
            selectedItemClass: 'selectedItem',
            gridFormId: 'projectInfoGridForm',
            //自定义JS
            projectRealDelUrl: "${tenantPrefix}/rs/project/work-project-info-realdel",
            projectNotifyUrl:"${tenantPrefix}/rs/project/work-project-info-notifyshow",
            projectPublishUrl:"${tenantPrefix}/rs/project/work-project-info-publish"
        };

        var table;

        $(function () {
            table = new Table(config);
            table.configPagination('.m-pagination');
            table.configPageInfo('.m-page-info');
            table.configPageSize('.m-page-size');
			
          	//设置段时间
            var sectionJson=[{"begin":"#pickerStartTime","end":"#pickerEndTime"},
                             {"begin":"#pickerStartTimeS","end":"#pickerEndTimeS"},
                             {"begin":"#pickerStartTimeP","end":"#pickerEndTimeP"}];
            fnSectionPickerTime(sectionJson)
        });
    </script>
    <script type="text/javascript" src="${cdnPrefix}/project/project.js"></script>
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

        .mytable tr td .project_title{width:150px;display:block;overflow: hidden;}
        .table{width:100%;}
        .mytable tr td, .mytable tr td .rwop{
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
                <form name="projectInfoForm" method="post"
                      action="work-project-info-temp.do" class="form-inline">
                    <label for="projectInfo_title">项目名称:</label> <input
                        type="text" id="projectInfo_title"
                        name="filter_LIKES_title"
                        value="${param.filter_LIKES_title}" class="form-control">

                    &nbsp;&nbsp;
                    <label> 计划开始时间:</label>
                    <div  id="pickerStartTimeS" class="input-group date">
                        <input style="width:160px;" type="text" name="filter_GED_startdate"
                               value="${param.filter_GED_startdate}" class="form-control required valid" readonly>
                        <span class="input-group-addon">
				<i class="glyphicon glyphicon-calendar"></i>
			</span>
                    </div>
                    至
                    <div  id="pickerEndTimeS" class="input-group date">
                        <input style="width:160px;" type="text" name="filter_LED_startdate"
                               value="${param.filter_LED_startdate}" class="form-control required valid" readonly>
                        <span class="input-group-addon">
				<i class="glyphicon glyphicon-calendar"></i>
			</span>
                    </div>
                    <br><br>
                    <label> 计划完成时间:</label>
                    <div id="pickerStartTimeP" class="input-group date">
                        <input style="width:160px;" type="text"  name="filter_GED_plandate"
                               value="${param.filter_GED_plandate}" class="form-control required valid" readonly>
                        <span class="input-group-addon">
				<i class="glyphicon glyphicon-calendar"></i>
			</span>
                    </div>
                    至
                    <div  id="pickerEndTimeP"  class="input-group date">
                        <input style="width:160px;" type="text" name="filter_LED_plandate"
                               value="${param.filter_LED_plandate}" class="form-control required valid" readonly>
                        <span class="input-group-addon">
				<i class="glyphicon glyphicon-calendar"></i>
			</span>
                    </div>
                    &nbsp;
                    &nbsp;
                    <label> 添加时间:</label>
                    <div id="pickerStartTime" class="input-group date">
                        <input style="width:160px;" type="text" name="filter_GED_publishtime"
                               value="${param.filter_GED_publishtime}" class="form-control required valid" readonly>
                        <span class="input-group-addon">
				<i class="glyphicon glyphicon-calendar"></i>
			</span>
                    </div>
                    至
                    <div id="pickerEndTime" class="input-group date">
                        <input style="width:160px;" type="text" name="filter_LED_publishtime"
                               value="${param.filter_LED_publishtime}" class="form-control required valid" readonly>
                        <span class="input-group-addon">
				<i class="glyphicon glyphicon-calendar"></i>
			</span>
                    </div>
                    <button id="btn_Search" class="btn btn-default a-search" type="submit">查询</button>
                </form>
            </div>
        </div>

        <div style="margin-bottom: 20px;">
        <%--    <div class="pull-left btn-group" role="group">
                <button class="btn btn-default a-insert"
                        onclick="location.href='work-project-info-input.do'">新建
                </button>
            </div>--%>

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

        <form id="projectInfoGridForm" name="projectInfoGridForm "
              method='post' action="work-project-info-temp.do" class="m-form-blank" enctype="multipart/form-data">
            <div class="panel panel-default">
                <div class="panel-heading">列表</div>
                <table id="projectInfoGrid" class="table table-hover mytable">
                    <thead>
                    <tr>
                        <th style="text-indent:0px;text-align:center;">操作</th>
                        <th>项目名称</th>
                        <th>负责人</th>
                        <th>知会人</th>
                        <th>开始时间</th>
                        <th>计划完成时间</th>
                        <th>添加时间</th>
                    </tr>
                    </thead>

                    <tbody>
                    <c:forEach items="${page.result}" var="item">
                        <tr>
                            <td><a href="work-project-info-modify.do?id=${item.id}">修改</a>
                                &nbsp;&nbsp;<a href="javascript:" onclick="fnProjectRealDel(${item.id})">删除</a>
                                &nbsp;&nbsp;<a href="javascript:" onclick="fnProjectPublish(${item.id})">发布 </a>
                            <td>
                                <a class="project_title rwop" title="${item.title}" href='work-project-info-detail.do?id=${item.id}'>
                                        ${item.title}
                                </a>
                            </td>
                            <td><tags:isDelUser userId="${item.leader}"></tags:isDelUser></td>
                            <td>
                                [<a href="javascript:" onclick='showNotify(${item.id})'>查看</a>]
                            </td>
                            <td><fmt:formatDate value="${item.startdate}" type="both" pattern='yyyy-MM-dd '/></td>
                            <td><fmt:formatDate value="${item.plandate}" type="both" pattern='yyyy-MM-dd'/></td>
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


