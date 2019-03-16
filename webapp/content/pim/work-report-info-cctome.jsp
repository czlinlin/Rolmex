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
        		return;
        	}
        	$("#workReportToMeForm").attr("action","work-report-info-cctome-export.do").submit();
        }
        function reportSearch(){
        	$("#workReportToMeForm").attr("action","work-report-info-cctome.do").submit();
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
<%@include file="/header/navbar.jsp"%>

<div class="row-fluid">
    <%@include file="/menu/sidebar.jsp"%>
    <c:if test="${not empty partyIdList}">
	    <div class="col-md-2" style="padding-top:65px;">
			<div class="panel panel-default">
	            <div class="panel-heading">
	                	汇报树形
	            </div>
	            <div class="panel-body">
					<%@include file="/common/tree/person-for-report.jsp" %>
				</div>
			</div>
		</div>
	    <section id="m-main" class="col-md-8" style="padding-top:65px;">
    </c:if>
    <c:if test="${empty partyIdList}">
    	<section id="m-main" class="col-md-10" style="padding-top:65px;">
    </c:if>
		<iframe src="work-report-info-cctome-i.do" id="reportFrame" width="100%" height="900px" frameborder="0"></iframe>
	</section>
    <!-- end of main -->
</div>
</body>

</html>
