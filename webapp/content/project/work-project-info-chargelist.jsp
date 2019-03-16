<%--
  
  User: wanghan
  Date: 2017\9\9 0009
  Time: 10:20
 
--%>
<%@page contentType="text/html;charset=UTF-8" %>
<%@page import="java.util.Date" %>
<%@include file="/taglibs.jsp" %>
<!doctype html>
<html lang="en">

<head>
    <%@include file="/common/meta.jsp" %>
    <title>麦联</title>
    <%@include file="/common/s3.jsp" %>
    <link type="text/css" rel="stylesheet" href="${cdnPrefix}/userpicker3/userpicker.css">
    <link type="text/css" rel="stylesheet" href="${cdnPrefix}/project/project.css">
    <!-- bootbox -->
    <script type="text/javascript" src="${cdnPrefix}/bootbox/bootbox.min1.js"></script>
    <script type="text/javascript">
        var config = {
            id: 'charege-infoGrid',
            pageNo: ${page.pageNo},
            pageSize: ${page.pageSize},
            totalCount: ${page.totalCount},
            resultSize: ${page.resultSize},
            pageCount: ${page.pageCount},
            orderBy: '${page.orderBy == null ? "" : page.orderBy}',
            asc: ${page.asc},
            params: {
            	'filter_LIKES_title':'${param.filter_LIKES_title}',
            	'filter_EQS_status':'${param.filter_EQS_status}',
            	'filter_GED_startdate':'${param.filter_GED_startdate}',
            	'filter_LED_startdate':'${param.filter_LED_startdate}',
            	'filter_GED_publishtime':'${param.filter_GED_publishtime}',
            	'filter_LED_publishtime':'${param.filter_LED_publishtime}',
            	'filter_GED_plandate':'${param.filter_GED_plandate}',
            	'filter_LED_plandate':'${param.filter_LED_plandate}'
            },
            selectedItemClass: 'selectedItem',
            gridFormId: 'charege-infoGridForm',
            exportUrl: 'work-task-charge-list.do',
            //自定义JS
            projectSubmitUrl: "${tenantPrefix}/rs/project/work-project-info-submit",//提交
            projectNotifyUrl: "${tenantPrefix}/rs/project/work-project-info-notifyshow",
            projectExecUrl: "${tenantPrefix}/rs/project/work-project-info-exec"
        };

        var table;

        $(function () {
            table = new Table(config);
            table.configPagination('.m-pagination');
            table.configPageInfo('.m-page-info');
            table.configPageSize('.m-page-size');

            //设置段时间
            var sectionJson = [{"begin": "#pickerStartTime", "end": "#pickerEndTime"},
                {"begin": "#pickerStartTimeS", "end": "#pickerEndTimeS"},
                {"begin": "#pickerStartTimeP", "end": "#pickerEndTimeP"}];
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

        .mytable tr td .blankrangle{width:175px;}
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
    <script>
        function fnSearch() {
            document.getElementById("chargeInfoForm").action = "work-project-info-chargelist.do";
        }
        function fnGoAction() {
            if (${page.resultSize==0}) {
                alert("没有数据需要导出！")
                return false;
            }
            if ($("#projectInfo_title").val() == "" &&
                $("#project_Status").val() == "" &&
                $("input[name='filter_GED_startdate']").val() == "" &&
                $("input[name='filter_LED_startdate']").val() == "" &&
                $("input[name='filter_GED_plandate']").val() == "" &&
                $("input[name='filter_LED_plandate']").val() == "" &&
                $("input[name='filter_GED_publishtime']").val() == "" &&
                $("input[name='filter_LED_publishtime']").val() == ""
            ) {
                alert("请选择导出条件！");
                return false;
            }
            document.getElementById("chargeInfoForm").action = "procharge-export.do";
        }
    </script>
</head>
<body>
<%@include file="/header/navbar.jsp" %>

<div class="row-fluid">
    <%@include file="/menu/sidebar.jsp" %>

    <!-- start of main -->
    <section id="m-main" class="col-md-10" style="padding-top:65px;">

        <div class="panel panel-default">
            <div class="panel-heading">
                <i class="glyphicon glyphicon-list"></i>
                查询
                <div class="pull-right ctrl">
                    <a class="btn btn-default btn-xs"><i id="charege-infoSearchIcon"
                                                         class="glyphicon glyphicon-chevron-up"></i></a>
                </div>
            </div>
            <div class="panel-body">
                <form id="chargeInfoForm" name="chargeInfoForm" method="post"
                      action="work-project-info-chargelist.do" class="form-inline">
                    <label for="projectInfo_title">项目名称:</label> <input
                        type="text" id="projectInfo_title"
                        name="filter_LIKES_title"
                        value="${param.filter_LIKES_title}" class="form-control">
                    &nbsp;&nbsp;
                    <label for="project_Status">状态:</label>
                    <select id="project_Status" class="form-control" name="filter_EQS_status" title="">
                        <option value="" ${param.filter_EQS_status==""?"selected='selected'":""}>全部</option>
                        <option value="0"  ${param.filter_EQS_status=="0"?"selected='selected'":""}>已发布</option>
                        <option value="1" ${param.filter_EQS_status=="1"?"selected='selected'":""}>进行中</option>
                        <option value="2" ${param.filter_EQS_status=="2"?"selected='selected'":""}>已完成</option>
                        <option value="3" ${param.filter_EQS_status=="3"?"selected='selected'":""}>已关闭</option>
                        <option value="4" ${param.filter_EQS_status=="4"?"selected='selected'":""}>已评价</option>
                    </select>
                    &nbsp;&nbsp;
                    <label> 计划开始日期:</label>
                    <div id="pickerStartTimeS" class="input-group date">
                        <input style="width:160px;" type="text" name="filter_GED_startdate"
                               value="${param.filter_GED_startdate}" class="form-control required valid" readonly>
                        <span class="input-group-addon">
				<i class="glyphicon glyphicon-calendar"></i>
			</span>
                    </div>
                    至
                    <div id="pickerEndTimeS" class="input-group date">
                        <input style="width:160px;" type="text" name="filter_LED_startdate"
                               value="${param.filter_LED_startdate}" class="form-control required valid" readonly>
                        <span class="input-group-addon">
				<i class="glyphicon glyphicon-calendar"></i>
			</span>
                    </div>
                    &nbsp;&nbsp;<br/><br/>
                    <label> 计划完成时间:</label>
                    <div id="pickerStartTimeP" class="input-group date">
                        <input style="width:160px;" type="text" name="filter_GED_plandate"
                               value="${param.filter_GED_plandate}" class="form-control required valid" readonly>
                        <span class="input-group-addon">
				<i class="glyphicon glyphicon-calendar"></i>
			</span>
                    </div>
                    至
                    <div id="pickerEndTimeP" class="input-group date">
                        <input style="width:160px;" type="text" name="filter_LED_plandate"
                               value="${param.filter_LED_plandate}" class="form-control required valid" readonly>
                        <span class="input-group-addon">
				<i class="glyphicon glyphicon-calendar"></i>
			</span>
                    </div>
                    &nbsp;
                    &nbsp;
                    <label> 发布时间:</label>
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
                    <button id="btn_Search" class="btn btn-default a-search" onclick="fnSearch()" type="submit">查询
                    </button>
                    &nbsp;
                    <button id="btn_Export" class="btn btn-default a-search" onclick="fnGoAction()" type="submit">导出
                    </button>
                </form>
            </div>
        </div>

        <div style="margin-bottom: 20px;">
            <div class="pull-left btn-group" role="group">
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

        <form id="workcenter-waitinfoGridForm" name="workcenter-waitinfoGridForm" method='post'
              action="charege-info-remove.do" class="m-form-blank">
            <div class="panel panel-default">
                <div class="panel-heading">
                    <i class="glyphicon glyphicon-list"></i>
                    <spring:message code="scope-info.scope-info.list.title" text="列表"/>
                </div>


                <table id="dynamicModelGrid" class="table table-hover mytable">
                    <thead>
                    <tr>
                        <th>操作</th>
                        <th>项目名称</th>
                        <th>状态</th>
                        <th>知会人</th>
                        <th>计划开始日期</th>
                        <th>计划完成日期</th>
                        <th>提交/关闭日期</th>
                        <th>进度</th>
                        <th>发布人</th>
                        <th>发布时间</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach items="${page.result}" var="item">
                        <tr>
                            <td>
                                <c:if test="${item.status=='0' or item.status=='1'}"><a
                                        href="work-project-info-notify-edit.do?id=${item.id}">知会</a></c:if>
                                <c:if test="${item.status=='1'}">
                                    <a href="work-project-info-submit.do?id=${item.id}">提交</a>&nbsp;
                                </c:if>
                                <c:if test='${item.status=="0"}'>
                                    <a href="javascript:" onclick="fnProjectExec(${item.id})">执行</a>&nbsp;
                                </c:if>
                                <c:if test="${item.status=='0' or item.status=='1'}"><a
                                        href="${ctx}/worktask/work-task-info-protaskinput.do?projectcode=${item.id}">添加任务</a></c:if>
                            </td>
                            <td>

                                <a class="project_title rwop" title="${item.title}"href='work-project-info-detail.do?id=${item.id}'>
                                        ${item.title}
                                </a>
                            </td>
                            <td>
                                <c:if test='${item.status=="0"}'>已发布</c:if>
                                <c:if test='${item.status=="1"}'>进行中</c:if>
                                <c:if test='${item.status=="2"}'>已完成</c:if>
                                <c:if test='${item.status=="3"}'>已关闭</c:if>
                                <c:if test='${item.status=="4"}'>已评价</c:if>
                            </td>
                            <td>
                                [<a href="javascript:" onclick='showNotify(${item.id})'>查看</a>]
                            </td>
                            <td><fmt:formatDate value="${item.startdate}" type="both" pattern='yyyy-MM-dd'/></td>
                            <td><fmt:formatDate value="${item.plandate}" type="both" pattern='yyyy-MM-dd'/></td>
                            <td><fmt:formatDate value="${item.committime}" type="both" pattern='yyyy-MM-dd'/></td>
                            <td>
                                <input name='iptpercent' type="hidden" value="${item.id}">
                                <div id="percent${item.id}" class="blankrangle percent">
                                    <div class="task_progress div_progress">
                                    当前进度:
                                        <c:if test='${item.currentpercent==null}'>0%</c:if>
                                        <c:if test='${item.currentpercent!=null}'>${item.currentpercent}%</c:if>
                                        目标进度:
                                        <c:if test='${item.targetpercent==null}'>0%</c:if>
                                        <c:if test='${item.targetpercent!=null}'>${item.targetpercent}%</c:if>
                                        
                                    </div>
                                    <div class="task_progress target_progress target_${item.bg}"
                                         style="width:${item.targetpercent}%"></div>
                                    <div class="task_progress actual_progress actual_${item.bg}"
                                         style="width:${item.currentpercent}%"></div>
                                </div>
                            </td>
                            <td><tags:isDelUser userId="${item.publisher}"/></td>
                            <td><fmt:formatDate value="${item.publishtime}" type="both"
                                                pattern='yyyy-MM-dd HH:mm:ss'/></td>
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


