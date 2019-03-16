<%@page contentType="text/html;charset=UTF-8" %>
<%@include file="/taglibs.jsp" %>
<%
    pageContext.setAttribute("currentHeader", "person");
%>
<%
    pageContext.setAttribute("currentMenu", "person");
%>
<%
    pageContext.setAttribute("currentMenuName", "人事管理");
%>
<!doctype html>
<html lang="en">
<head>
    <%@include file="/common/meta.jsp" %>
    <title><spring:message code="dev.employee-info.list.title"
                           text="麦联"/></title>
    <%@include file="/common/s3.jsp" %>
    <script type="text/javascript"
            src="${cdnPrefix}/bootbox/bootbox.min1.js"></script>
    <script type="text/javascript">
    
    	/* $(function(){
			window.parent.closeLoading();
			
			window.parent.$.showMessage($('#m-success-tip-message').html(), {
                position: 'top',
                size: '50',
                fontSize: '20px'
            });
    	}); */
    	
		function checkAll(obj){
			if(obj == 0){
				$('input:checkbox').attr("checked", true);
			}else if(obj == 1){
				$('input:checkbox').attr("checked", false);
			}
			
		}
    </script>
<style>
    	#tb1 td {
        border: 1px solid #BBB
    }

    .f_td {
        width: 120px;
        font-size: 12px;
        white-space: nowrap
    }

    .f_r_td {
        width: 130px;
        text-align: left;
    }


    #tb1 tr td textarea {
        border: navajowhite;
    }

    #tb1 tr td {
        text-align: center;
        line-height: 28px;
        height:28px;
    }

    #tb1 tr td.f_td.f_right {
        text-align: right;
    }

    #tb1 tr td input.input_width {
        width: auto;
    }
    #tb1 td{text-align:left;}
</style>
</head>

<body>

<div class="row-fluid">

	 <c:if test="${not empty flashMessages}">
	<div id="m-success-tip-message" style="display: none;">
		<ul>
			<c:forEach items="${flashMessages}" var="item">
				<c:if test="${item != ''}">
					<li>${item}</li>
				</c:if>
			</c:forEach>
		</ul>
	</div>
   </c:if>
	<section id="m-main" class="col-md-12" style="padding-top: 66px;">
		<div class="panel panel-default">
			<div class="panel-heading">
				<i class="glyphicon glyphicon-list"></i> 工资基本数据导出
				<div class="pull-right ctrl">
					<a class="btn btn-default btn-xs"><i id="orgSearchIcon"
						class="glyphicon glyphicon-chevron-up"></i></a>
				</div>
			</div>
			<div class="panel-body">
				<form id="export" name="exportAttendanceExcel" method="post" class="form-inline" action="person_salary_base-export.do" enctype="multipart/form-data">
					<table id="tb1" style="width:100%;border:1;">
						<tr>
							<td style="line-height:35px;text-align:left;padding-left:10px;" colspan="2">
								<c:forEach items="${dictInfoList }" var="dictInfo">
									<label><input name="salaryColumn" type="checkbox" value="${dictInfo.value}-${dictInfo.name}" />${dictInfo.name}</label>
								</c:forEach>
								&nbsp;&nbsp;&nbsp;
								<label><a onclick="checkAll(0)">全选</a></label>
								&nbsp;&nbsp;&nbsp;
								<label><a onclick="checkAll(1)">取消全选</a></label>
							</td>
						</tr>
						<tr>
							<td style="line-height:50px;" colspan="2">
								<button type="submit" class="btn btn-primary" id="btn_exportExcel" onclick="exportPersonSalaryBaseForm();">确认导出</button>
								<button type="button" class="btn btn-primary" onclick="history.back(-1)">取消</button>
							</td>
						</tr>
					</table>
					<input type="hidden" name="postId" value="${param.postId}">
					<input type="hidden" name="contractCompanyId" value="${param.contractCompanyId}">
					<input type="hidden" name="startDate" value="${param.startDate}">
					<input type="hidden" name="endDate" value="${param.endDate}">
					<input type="hidden" name="personName" value="${param.personName}">
				</form>
			</div>
		</div>
	</section>
</div>
<!-- <script type="text/javascript">
function exportPersonSalaryBaseForm(){
	$("#export").attr("action","person_salary_base-export.do");
}
	
</script> -->
</body>
</html>

