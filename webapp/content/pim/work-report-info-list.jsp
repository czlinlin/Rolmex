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

    <!-- bootbox -->
    <script type="text/javascript" src="${cdnPrefix}/bootbox/bootbox.min1.js"></script>
    <!-- select -->
    <link type="text/css" rel="stylesheet" href="${cdnPrefix}/userpicker3-v2/userpicker.css">
    <script type="text/javascript" src="${cdnPrefix}/userpicker3-v2/userpickercustom.js"></script>

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
                'filter_LIKES_title': '${param.filter_LIKES_title}',
                'filter_EQS_type': '${param.filter_EQS_type}',
                'filter_EQS_status': '${param.filter_EQS_status}',
                'filter_GED_reportDate': '${param.filter_GED_reportDate}',
                'filter_LED_reportDate': '${param.filter_LED_reportDate}'
            },
            selectedItemClass: 'selectedItem',
            gridFormId: 'workReportInfoGridForm',
            exportUrl: 'pim--export.do',
            //自定义JS
            reportCCInfoUrl: "${tenantPrefix}/rs/pim/work-report-info-cc",
            reportRealDelUrl: "${tenantPrefix}/rs/pim/work-report-info-drawback",
        };

        var table;

        $(function () {
            table = new Table(config);
            table.configPagination('.m-pagination');
            table.configPageInfo('.m-page-info');
            table.configPageSize('.m-page-size');

            //dialog of person select
            createUserPicker({
                modalId: 'ccUserPicker',
                targetId: 'ccDiv',
                multiple: true,
                showExpression: true,
                searchUrl: '${tenantPrefix}/rs/user/search',
                treeUrl: '${tenantPrefix}/rs/party/treeNoAuth?partyStructTypeId=1',
                childUrl: '${tenantPrefix}/rs/party/searchUser'

            })


            //设置段时间
            var sectionJson = [{"begin": "#pickerStartTime", "end": "#pickerEndTime"}];
            fnSectionPickerTime(sectionJson)

        });

        //show feeback
        var fnFeeBackInfo = function (id) {
            var msg = $("#spanFeeBack" + id).html();
            var dialog = bootbox.dialog({
                title: "反馈信息",
                message: msg,
                show: true,
                buttons: {
                    ok: {label: "确定"}
                }
            });
        }
        
        function reportExport(){
        	if($("#workReportInfo_title").val().replace(/(^\s*)|(\s*$)/g, "")==''&& $("#workReportInfo_Type").val()==''
        			&&$("#workReportInfo_Status").val()==''&&$("#pickerStartTime").val()==''&&$("#iptpickerEndTime").val()==''){
        		alert("请选择导出条件！");
        		return;
        	}
        	$("#workReportInfoForm").attr("action","work-report-info-list-export.do").submit();
        }
        function reportSearch(){
        	$("#workReportInfoForm").attr("action","work-report-info-list.do").submit();
        }
    </script>
    <script type="text/javascript" src="${cdnPrefix}/report/report.js"></script>
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

        .mytable tr td .report_title {
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
    <section id="m-main" class="col-md-10" style="padding-top:65px;">

        <div class="panel panel-default">
            <div class="panel-heading">
                查询
                <div class="pull-right ctrl">
                    <a class="btn btn-default btn-xs"><i id="workReportInfoSearchIcon"
                                                         class="glyphicon glyphicon-chevron-up"></i></a>
                </div>
            </div>
            <div class="panel-body">
                <form id="workReportInfoForm" name="workReportInfoForm" method="post" action="work-report-info-list.do" class="form-inline">
                    <label for="workReportInfo_title">标题:</label>
                    <input type="text" id="workReportInfo_title" name="filter_LIKES_title"
                           value="${param.filter_LIKES_title}" class="form-control" maxlength="20">
                    &nbsp;&nbsp;
                    <label for="workReportInfo_Type">类型:</label>
                    <select id="workReportInfo_Type" class="form-control" name="filter_EQS_type">
                        <option value=""  ${param.filter_EQS_type==""?"selected='selected'":""}>全部</option>
                        <option value="1" ${param.filter_EQS_type=="1"?"selected='selected'":""}>周报</option>
                        <option value="2" ${param.filter_EQS_type=="2"?"selected='selected'":""}>月报</option>
                        <option value="3" ${param.filter_EQS_type=="3"?"selected='selected'":""}>年报</option>
                        <option value="4" ${param.filter_EQS_type=="4"?"selected='selected'":""}>专项</option>
                    </select>
                    &nbsp;&nbsp;
                    <label for="workReportInfo_Status">状态:</label>
                    <select id="workReportInfo_Status" class="form-control" name="filter_EQS_status" title="">
                        <option value="" ${param.filter_EQS_status==""?"selected='selected'":""}>全部</option>
                        <option value="0"  ${param.filter_EQS_status=="0"?"selected='selected'":""}>未读</option>
                        <option value="1" ${param.filter_EQS_status=="1"?"selected='selected'":""}>已读</option>
                        <option value="2" ${param.filter_EQS_status=="2"?"selected='selected'":""}>已反馈</option>
                    </select>
                    &nbsp;&nbsp;
                    <label for="pickerStartTime"> 发布时间:</label>
                    <div id="pickerStartTime" class="input-group date">
                        <input id="filter_GED_reportDate" style="width:160px;" type="text" name="filter_GED_reportDate"
                               value="${param.filter_GED_reportDate}" class="form-control required valid" readonly>
                        <span class="input-group-addon">
							<i class="glyphicon glyphicon-calendar"></i>
						</span>
                    </div>
                    至
                    <div id="pickerEndTime" class="input-group date">
                        <input id="filter_LED_reportDate" style="width:160px;" type="text" name="filter_LED_reportDate"
                               value="${param.filter_LED_reportDate}" class="form-control required valid" readonly>
                        <span class="input-group-addon">
							<i class="glyphicon glyphicon-calendar"></i>
						</span>
                    </div>
					<input type="hidden" name="reportCatalog" value="已发汇报">
                    <button id="btn_Search"  class="btn btn-default a-search" onclick="reportSearch()">查询</button>
                    <button id="btn_Export"  class="btn btn-default a-search" onclick="reportExport()">导出</button>

                    &nbsp;
                </form>
            </div>
        </div>

        <div style="margin-bottom: 20px;">
            <%--    <div class="pull-left btn-group" role="group">
                    <button class="btn btn-small a-search" onclick="document.location.href='work-report-info-export.do'">导出</button>
            </div>--%>
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

        <form id="workReportInfoGridForm" name="workReportInfoGridForm" method='post' action="work-report-info-list.do"
              class="m-form-blank">
            <div class="panel panel-default">
                <div class="panel-heading">
                    列表
                </div>
                <table id="workReportInfoGrid" class="table table-hover mytable">
                    <thead>
                    <tr>
                        <th>操作</th>
                        <th>汇报标题</th>
                        <th>汇报类型</th>
                        <th>状态</th>
                        <th>接收人</th>
                        <th>抄送人</th>
                        <th>汇报时间</th>
                        <th>反馈信息</th>
                    </tr>
                    </thead>

                    <tbody>
                    <c:forEach items="${page.result}" var="item">
                        <tr>
                            <td>
                                <a href="work-report-info-turn.do?id=${item.id}&type=4" class="a-update"><spring:message
                                        code="core.list.edit" text="转发"/></a>
                                <c:if test='${item.status=="0"}'>
                                    <a href="work-report-info-modify.do?id=${item.id}" class="a-update"><spring:message
                                            code="core.list.edit" text="修改"/></a>
                                </c:if>
                                <c:if test='${item.status=="0"}'>
                                    <a href="javascript:" onclick='fnReportDrawback(${item.id})' class="a-update">删除</a>
                                </c:if>
                                <a target="blank" href="work-report-info-print.do?id=${item.id}"
                                   class="a-update"><spring:message code="core.list.edit" text="打印"/></a>
                            </td>
                            <td><a class="report_title rwop" title="${item.title}"
                                   href="work-report-info-look.do?id=${item.id}"> ${item.title}</a></td>
                            <td>
                                <c:if test='${item.type=="1"}'>周报</c:if>
                                <c:if test='${item.type=="2"}'>月报</c:if>
                                <c:if test='${item.type=="3"}'>年报</c:if>
                                <c:if test='${item.type=="4"}'>专项</c:if>
                            </td>
                            <td>
                                <c:if test='${item.status=="0"}'>未读</c:if>
                                <c:if test='${item.status=="1"}'>已读</c:if>
                                <c:if test='${item.status=="2"}'>已反馈</c:if>
                            </td>
                            <td><tags:isDelUser userId="${item.sendee}"/></td>
                            <td>[<a href="javascript:" onclick="fnShowCCInfo(${item.id})">查看</a>]</td>
                            <td><fmt:formatDate value="${item.reportDate}" type="both"/></td>
                            <td>
                                <c:if test='${item.status=="2"}'>
                                    [<a href="javascript:" onclick="fnFeeBackInfo(${item.id})">查看</a>]
                                    <span id="spanFeeBack${item.id}" style="display:none">
	        		${item.feedback}<br/><br/>
	        		时间：<fmt:formatDate value="${item.feedbacktime}" type="both"/>
	        	</span>
                                </c:if>
                                <c:if test='${item.status!="2"}'>
                                    无
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

    </section>
    <!-- end of main -->
</div>

</body>

</html>
