<%@page contentType="text/html;charset=UTF-8" %>
<%@include file="/taglibs.jsp" %>
<%pageContext.setAttribute("currentHeader", "humantask");%>
<%pageContext.setAttribute("currentMenu", "humantask");%>
<!doctype html>
<html lang="en">

<head>
    <%@include file="/common/meta.jsp" %>
    <title>麦联</title>
    <%@include file="/common/s3.jsp" %>
    <link type="text/css" rel="stylesheet" href="${cdnPrefix}/userpicker3/userpicker.css">
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
                'filter_LIKES_title': '${param.filter_LIKES_title}',
                'filter_EQS_status': '${param.filter_EQS_status}',
                'filter_GED_plantime': '${param.filter_GED_plantime}',
                'filter_LED_plantime': '${param.filter_LED_plantime}',
                'filter_GED_publishtime': '${param.filter_GED_publishtime}',
                'filter_LED_publishtime': '${param.filter_LED_publishtime}'
            },
            selectedItemClass: 'selectedItem',
            gridFormId: 'charege-infoGridForm',
            exportUrl: 'work-task-charge-list.do',
            //自定义JS
            taskCCUrl: "${tenantPrefix}/rs/worktask/work-task-info-cc",//抄送人
            taskExecUrl: "${tenantPrefix}/rs/worktask/work-task-info-exec",//执行
            taskSubmitUrl: "${tenantPrefix}/rs/worktask/work-task-info-submit",//提交
            taskCommentUrl: "${tenantPrefix}/rs/worktask/work-task-comment-save"//备注
        };

        var table;

        $(function () {
            table = new Table(config);
            table.configPagination('.m-pagination');
            table.configPageInfo('.m-page-info');
            table.configPageSize('.m-page-size');

            //设置段时间
            var sectionJson = [{"begin": "#pickerStartTime", "end": "#pickerEndTime"},
                {"begin": "#pickerPublicStartTime", "end": "#pickerPublicEndTime"}];
            fnSectionPickerTime(sectionJson);


        })
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
    <script>
        function fnSearch() {
            document.getElementById("charege-infoForm").action = "work-task-charge-list.do";
        }
        function fnGoAction() {

            if (${page.resultSize==0}) {
                alert("没有数据需要导出！")
                return false;
            }
            if ($("#charege-info_title").val() == "" &&
                $("#workTask_Status").val() == "" &&
                $("input[name='filter_GED_publishtime']").val() == "" &&
                $("input[name='filter_LED_publishtime']").val() == "" &&
                $("input[name='filter_GED_plantime']").val() == "" &&
                $("input[name='filter_LED_plantime']").val() == ""
            ) {
                alert("请选择导出条件！");
                return false;
            }
            document.getElementById("charege-infoForm").action = "charge-export.do";
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
                <form id="charege-infoForm" name="charege-infoForm" method="post" action="work-task-charge-list.do" class="form-inline">
                    <label for="charege-info_name">标题:</label>
                    <input type="text" style="width:200px;" id="charege-info_title" name="filter_LIKES_title"
                           value="${param.filter_LIKES_title}" class="form-control">
                    &nbsp;&nbsp;
                    <label for="workTask_Status">状态:</label>
                    <select id="workTask_Status" class="form-control" name="filter_EQS_status" title="">
                        <option value="" ${param.filter_EQS_status==""?"selected='selected'":""}>全部</option>
                        <option value="0"  ${param.filter_EQS_status=="0"?"selected='selected'":""}>已发布</option>
                        <option value="1" ${param.filter_EQS_status=="1"?"selected='selected'":""}>进行中</option>
                    </select>
                    &nbsp;&nbsp;
                    <label for="charege-info_name"><spring:message code='charege-info.charege-info.list.search.plantime'
                                                                   text='计划完成时间'/>:</label>
                    <div id="pickerStartTime" class="input-group  datepickerbegin date">
                        <input style="width:160px;" type="text" name="filter_GED_plantime"
                               value="${param.filter_GED_plantime}" class="form-control required valid" readonly>
                        <span class="input-group-addon">
					<i class="glyphicon glyphicon-calendar"></i>
				</span>
                    </div>

                    至
                    <div id="pickerEndTime" class="input-group  datepickerend date">
                        <input style="width:160px;" type="text" name="filter_LED_plantime"
                               value="${param.filter_LED_plantime}" class="form-control required valid" readonly>
                        <span class="input-group-addon">
					<i class="glyphicon glyphicon-calendar"></i>
				</span>
                    </div>
                    <br/><br/>
                    <label for="charege-info_name"><spring:message
                            code='charege-info.charege-info.list.search.publishtime' text='发布时间'/>:</label>
                    <div id="pickerPublicStartTime" class="input-group  datepickerbegin date">
                        <input style="width:160px;" type="text" name="filter_GED_publishtime"
                               value="${param.filter_GED_publishtime}" class="form-control required valid" readonly>
                        <span class="input-group-addon">
					<i class="glyphicon glyphicon-calendar"></i>
				</span>
                    </div>
                    至
                    <div id="pickerPublicEndTime" class="input-group  datepickerend date">
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
                        <th style="text-indent:0px;text-align:center;">操作</th>
                        <th class="sorting" name="name">标题</th>
                        <th class="sorting">状态</th>
                        <th class="sorting" name="name">抄送</th>
                        <th class="sorting" name="name">计划开始时间</th>
                        <th class="sorting" name="name">计划完成时间</th>
                        <th class="sorting" name="name">发布人</th>
                        <th class="sorting" name="name">发布时间</th>
                    </tr>
                    </thead>

                    <tbody>
                    <c:forEach items="${page.result}" var="item">
                        <tr>
                            <td>
                                <c:if test="${item.uppercode==0}">
                                    <a href="work-task-info-appendtask.do?uppercode=${item.id}"> [添加子任务]</a>&nbsp;
                                </c:if>
                                <c:if test='${item.status=="0"}'>
                                    <a href="javascript:" onclick="fnTaskExec(${item.id})">[执行]</a>&nbsp;
                                </c:if>
                                <c:if test='${item.status=="1"}'>
                                    <a href="work-task-info-submit.do?id=${item.id}">[提交]</a>&nbsp;
                                </c:if>
                                
                                <a href="javascript:" onclick="fnTaskComment(${item.id})">[备注]</a>
					<!-- cz add 2018/12/12  新增抄送功能 -->
<a href="${tenantPrefix}/worktask/worktask-CC-input-new.do?id=${item.id}">[抄送]</a>
                           
                           </td>
                            <td>
                                <a class="workTask_title rwop" title="${item.title}"
                                   href='work-task-info-detail.do?id=${item.id}'>
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
                                [<a href="javascript:" onclick='showCCMan("${item.id}")'><spring:message
                                    code="core.list.edit" text="查看"/></a>]
                            </td>
                            <td><fmt:formatDate value="${item.starttime}" type="both" pattern='yyyy-MM-dd HH:mm'/></td>
                            <td><fmt:formatDate value="${item.plantime}" type="both" pattern='yyyy-MM-dd HH:mm'/></td>
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

