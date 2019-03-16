<%--
  
  User: ckx
  Date: 2018\08\13 
  
 
--%>
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
    
    	$(function(){
			window.parent.closeLoading();
			
			window.parent.$.showMessage($('#m-success-tip-message').html(), {
                position: 'top',
                size: '50',
                fontSize: '20px'
            });
    	});
    
		//上传图片
		var fnUploadExcel = function(){
			
			if($("#excelFile").val()==""){
	    		alert('请选择导入的EXCEL文件');
	    		return false;
	    	}
	    	if($("#excelFile").val().lastIndexOf(".xls") < 0 && $("#excelFile").val().lastIndexOf(".xlsx") < 0){
	    		alert('只能上传EXCEL文件');
	    		return false;
	    	}
	    	
	    	
	    	window.parent.dialogLoading("正在进行导入，请勿刷新页面");
	    	$("#import").attr("action","${tenantPrefix}/PartyExcelController/attendance-excel-import.do");
	    	$("#import").submit();
	    	return true;
			/* $.ajax({
				url: '${tenantPrefix}/PartyExcelController/check-attendance-excel-import.do',
				type : 'post',
				dataType : 'json',
				success :function(data){
					
					if(data == "false"){
						alert('请设置导入日期');
					}else{
						window.dialogLoading("正在进行导入，请勿刷新页面");
						//$("excelFileHidden").val($("#excelFile").val());
						$("#import").attr("action","${tenantPrefix}/PartyExcelController/attendance-excel-import.do");
						//$("#attendanceForm").submit();
						//$("#imBtnId").click();
					}
				},
	            error: function (XMLHttpRequest, textStatus, errorThrown) {
	                alert("[" + XMLHttpRequest.status + "]error，请求失败")
	            },
	            complete: function (xh, status) {
	                if (status == "timeout"){
	                	alert('请求超时');
	                	return false;
	                }  
	            }
	        }); */
		}
    </script>
    <!-- <style type="text/css">

    #tb1{text-align:center; margin:10px auto;width:80%;line-height: 2.5;}
        #tb1 td{border:1px solid #BBB; }
    
        th {
            white-space: nowrap
        }
        td{
        	white-space: nowrap
        }
    </style> -->
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
				<i class="glyphicon glyphicon-list"></i> 导入
				<div class="pull-right ctrl">
					<a class="btn btn-default btn-xs"><i id="orgSearchIcon"
						class="glyphicon glyphicon-chevron-up"></i></a>
				</div>
			</div>
			<div class="panel-body">
				<form id="import" name="importAttendanceExcel" method="post" class="form-inline" enctype="multipart/form-data">
					<table id="tb1" style="width:100%;border:1;">
						<tr>
							<td style="line-height:35px;text-align:left;padding-left:10px;" colspan="2">
								<span style='color:red;'>提示：</span>
								<br/><font>1.导入EXCEL包括列有：<font color="blue">工号，姓名，部门，机器号，编号，打卡时间</font></font>
								<br/><font>2.请确认设置导入日期；若不设置，则默认沿用之前的导入截至日期</font>
								
							</td>
						</tr>
						<tr>
							<td style="line-height:35px;">选择导入的EXCEL文件：</td>
							<td style="padding-left:5px;"><input type="file" name="excelFile" id="excelFile" accept=".xls,.xlsx" />
								<div id="warnId" style="color:gray;text-align:left;" >(请选择要上传的EXCEL文件)</div>
							</td>
						
						</tr>
						<tr>
							<td style="line-height:50px;" colspan="2">
								<button type="button" class="btn btn-primary" id="btn_importExcel" onclick="fnUploadExcel();">导入文件</button>
							</td>
						</tr>
					</table>
								
					
					<!-- <button type="button" class="btn btn-default a-search" onclick="toUpload()">选择并导入</button>
					<input onchange="uploadExcel()" type="file" id="excelFile" name="excelFile" accept=".xls,.xlsx" style="filter:alpha(opacity=0);opacity:0;width: 0;height: 0;">
					<input onchange="uploadExcel()" type="file" id="excelFileHidden" name="excelFile" accept=".xls,.xlsx" style="filter:alpha(opacity=0);opacity:0;width: 0;height: 0;">
					&nbsp; -->
				</form>
				<!-- <font>导入EXCEL包括列有：<font color="red">工号，姓名，部门，机器号，编号，打卡时间</font></font> -->
			</div>
		</div>
	
	
	</section>

    <!-- 失败 start -->
    <c:if test="${failList != null}">
		<section id="m-main" class="col-md-12" style="padding-top:65px;">
	      <div class="panel panel-default" >
	        <div class="panel-heading">
			  <i class="glyphicon glyphicon-list"></i>
			  导入失败，共${allSize}人，失败${failSize}人，具体信息如下<span style='color:red;'>(请及时复制保存，若页面刷新将消失)</span>
			</div>
	
		<form id="authDataForm" method="post" class="form-horizontal" style=" overflow-y:auto; overflow-x:auto;height:400px;">
			<table id="tb1" style="width:100%;" >
			<c:forEach items="${failList}" var="item">	
				<tr>
		           <td>
		            	<span style='color:red;'> 工号：${item.userCode}</span>
		           </td>
		          
		           <td>
		            	<span style='color:red;'> 姓名： ${item.userName}</span>
		           </td>
		          
		           <td>
		            	<span style='color:red;'> 部门：${item.departmentName}</span>
		           </td>
		           <td>
		            	<span style='color:red;'> 机器号：${item.machNo}</span>
		           </td>
		           <td>
		            	<span style='color:red;'> 编号：${item.userNo}</span>
		           </td>
		           <td>
		            	<span style='color:red;'> 打卡时间：${item.dateTime}</span>
		           </td>
		           <td>
		            	 <span style='color:red;'> ${item.failReason}</span>
		           </td>
		           
			  		</tr>
		  	</c:forEach>  	
		  	</table>
		</form>
		
		</div>
		</section>
	</c:if>
    
    
    <!-- 失败  end -->
</div>

</body>
<script>
/* 
	function setDeadLine(){
	
		var  myselect=document.getElementById("importDeadLine");
		var index=myselect.selectedIndex ;  
		var bt=myselect.options[index].value;
		document.getElementById("attendanceImportDeadLine").value=bt;
	}
	
	
	 function attendanceImportSetSave(){
			
			$('#attendanceImportSetForm').attr('action', '${tenantPrefix}/auth/attendanceImportSetSave.do');
			$('#attendanceImportSetForm').submit();
		} */

</script>


</html>

