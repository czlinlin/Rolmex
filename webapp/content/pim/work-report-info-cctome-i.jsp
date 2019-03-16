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
    <!-- bootbox -->
    <script type="text/javascript" src="${cdnPrefix}/bootbox/bootbox.min1.js"></script>
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
                'filter_EQS_workReportInfo.type': '${param.filter_EQS_workReportInfo.type}',
                'filter_EQS_status': '${param.filter_EQS_status}',
                'filter_GED_forwardtime': '${param.filter_GED_forwardtime}',
                'filter_LED_forwardtime': '${param.filter_LED_forwardtime}',
                'filter_EQS_ccPreSettingId':'${param.filter_EQS_ccPreSettingId}',
                'filter_LIKES_user_id':'${param.filter_LIKES_user_id}'
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

          //设置段时间
            var sectionJson=[{"begin":"#pickerStartTime","end":"#pickerEndTime"}];
            fnSectionPickerTime(sectionJson)
            
        });
        
        function reportExport(){
        	if($("#workReportInfo_title").val().replace(/(^\s*)|(\s*$)/g, "")==''&& $("#workReportInfo_Type").val()==''
        			&&$("#workReportInfoCc_status").val()==''&&$("#iptpickerStartTime").val()==''&&$("#iptpickerEndTime").val()==''
        			&&$("#ccPreSettingId").val()==''&&$('#filter_LIKES_user_id').val()==''){
        		alert("请选择导出条件！");
        		return false;
        	}
        	$("#workReportToMeForm").attr("action","work-report-info-cctome-export.do").submit();
        }
        function reportSearch(){
        	$("#workReportToMeForm").attr("action","work-report-info-cctome-i.do").submit();
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

        .mytable tr td .report_title{width:150px;display:block;overflow: hidden;}
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

<div class="row-fluid">
	
    <!-- start of main -->
    <section id="m-main" class="col-md-12">

        <div class="panel panel-default">
            <div class="panel-heading">
                查询
                <div class="pull-right ctrl">
                    <a class="btn btn-default btn-xs"><i id="workReportInfoSearchIcon" class="glyphicon glyphicon-chevron-up"></i></a>
                </div>
            </div>
            <div class="panel-body">
                <form id="workReportToMeForm" name="workReportToMeForm" method="post" action="work-report-info-cctome-i.do" class="form-inline">
                    <label for="workReportInfo_title">&emsp;&emsp;标题：</label>
                    <input type="text" id="workReportInfo_title" name="filter_LIKES_title" value="${param.filter_LIKES_title}" class="form-control" maxlength="20">
                    <label for="workReportInfo_Type">类型：</label>
                    <select id="workReportInfo_Type"  class="form-control" name="filter_EQS_type">
                        <option value="" ${param.filter_EQS_type == '' ? 'selected' : ''}>全部</option>
                        <option value="1" ${param.filter_EQS_type == '1' ? 'selected' : ''}>周报</option>
                        <option value="2" ${param.filter_EQS_type == '2' ? 'selected' : ''}>月报</option>
                        <option value="3" ${param.filter_EQS_type == '3' ? 'selected' : ''}>年报</option>
                        <option value="4" ${param.filter_EQS_type == '4' ? 'selected' : ''}>专项</option>
                    </select>
                    <label for="workReportInfoCc_status">状态：</label>
                    <select id="workReportInfoCc_status"  class="form-control" name="filter_EQS_workReportCc.status">
                        <option value="" ${param["filter_EQS_workReportCc.status"] == '' ? 'selected' : ''}>全部</option>
                        <option value="0" ${param["filter_EQS_workReportCc.status"] == '0' ? 'selected' : ''}>未读</option>
                        <option value="1" ${param["filter_EQS_workReportCc.status"] == '1' ? 'selected' : ''}>已读</option>
                    </select>
                    <label for="filter_EQS_ccPreSettingId">汇报条线：</label>
                    <select id="ccPreSettingId"  class="form-control" name="filter_EQS_ccPreSettingId">
                    	<option value="">全部</option>
                    	<c:forEach  items="${ccPresetting}" var="item">
                        	<option value="${item.id}" ${item.id==param["filter_EQS_ccPreSettingId"] ? 'selected' : ''}>${item.title}</option>
                    	</c:forEach>
                        
                    </select>
                    <br/>
                    <br/>
                    <label for="pickerStartTime"> 汇报时间：</label>
                    <div id="pickerStartTime" class="input-group date">
                        <input style="width:160px;" type="text" name="filter_GED_report_Date" value="${param.filter_GED_report_Date}" class="form-control required valid" readonly>
                        <span class="input-group-addon">
				<i class="glyphicon glyphicon-calendar"></i>
			</span>
                    </div>
                    至
                    <div id="pickerEndTime" class="input-group date">
                        <input style="width:160px;" type="text" name="filter_LED_report_Date" value="${param.filter_LED_report_Date}" class="form-control required valid" readonly>
                        <span class="input-group-addon">
				<i class="glyphicon glyphicon-calendar"></i>
			</span>
                    </div>
                    
                    <label for="workReportInfo_title">汇报人:</label>
                    <input type="text" id="filter_LIKES_user_id" name="filter_LIKES_user_id" value="${param.filter_LIKES_user_id}" class="form-control" maxlength="20">
                    <input type="hidden" name="reportCatalog" value="抄送我的">
                    <input type="hidden" id="parentEntityId" name="filter_INL_user_id" value="${param.filter_INL_user_id}">
                    <button class="btn btn-default a-search"  id="btn_Search" onclick="reportSearch()">查询</button>&nbsp;
                    <button class="btn btn-default a-search"  id="btn_Export" onclick="return reportExport()">导出</button>
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

        <form id="workReportCcToMeForm" name="workReportCcToMeForm" method='post' action="work-report-info-cctome.do" class="m-form-blank">
            <div class="panel panel-default">
                <div class="panel-heading">
                    列表
                </div>
                <table id="workReportCcToMe" class="table table-hover mytable">
                    <thead>
                    <tr>
                        <th>操作</th>
                        <th>标题</th>
                        <th>类型</th>
                        <th>状态</th>
                        <th>汇报时间</th>
                        <th>汇报人</th>

                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach items="${page.result}" var="info">
                        <tr style="${info.showCcType=='2'?'color:#B45B3E':''}">
                            <td>
                                <a href="javascript:window.parent.location.href='work-report-info-turn.do?id=${info.id}&type=2'" class="a-update">
                                		<spring:message code="core.list.edit" text="转发"/></a>
                                <a href="javascript:" onclick="window.parent.window.open('work-report-info-print.do?id=${info.id}')" class="a-update"><spring:message code="core.list.print" text="打印"/></a>
                            </td>
                            <td>
                            	<a  class="report_title rwop" title="${item.title}" href="javascript:window.parent.location.href='work-report-info-look.do?id=${info.id}'"> ${info.title}</a>
                            </td>
                            <td>
                                <c:if test='${info.type=="1"}'>周报</c:if>
                                <c:if test='${info.type=="2"}'>月报</c:if>
                                <c:if test='${info.type=="3"}'>年报</c:if>
                                <c:if test='${info.type=="4"}'>专项</c:if>
                            </td>
                            <td>
                                <c:if test='${info.showCcStatus=="0"}'>未读</c:if>
                                <c:if test='${info.showCcStatus=="1"}'>已读</c:if>
                            </td>
                            <td><fmt:formatDate value="${info.reportDate}" type="both" /></td>
                            <td><tags:isDelUser userId="${info.userId}"/></td>

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

    </section>
    <!-- end of main -->
</div>
</body>

</html>
