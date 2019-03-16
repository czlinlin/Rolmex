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
    	//初始化合同单位
    	$(function(){
    		$.ajax({
				url: '${tenantPrefix}/user/getAllContractCompanyName.do',
				type : 'post',
				dataType : 'json',
				success :function(data){
					var strHtml ="<option value=''>请选择</option>";
		            for (var i = 0; i < data.length; i++) {
		                strHtml += "<option value='" + data[i].contract_company_id + "'>" + data[i].contract_company_name + "</option>";
		            }
		            $("#contractCompanyName").html(strHtml);
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
	        });
    	});
    	
    
		//上传图片
		var fnUploadExcel = function(){
			
			
			var contractCompanyId = $("#contractCompanyName").val();
			if(contractCompanyId == ''){
				alert("请选择导入单位");
				return false;
			}
			$("#contractCompanyId").attr("value",contractCompanyId);
			
			var boo = $("input[type='checkbox']").is(':checked');
			/* if(!boo){
				alert("请选择导入项");
				return false;
			} */
			if($("#excelFile").val()==""){
	    		alert('请选择导入的EXCEL文件');
	    		return false;
	    	}
	    	if($("#excelFile").val().lastIndexOf(".xls") < 0 && $("#excelFile").val().lastIndexOf(".xlsx") < 0){
	    		alert('只能上传EXCEL文件');
	    		return false;
	    	}
	    	
	    	
	    	window.parent.dialogLoading("正在进行导入，请勿刷新页面");
	    	$("#import").attr("action","${tenantPrefix}/user/salary-base-import-excel-do.do");
	    	$("#import").submit();
	    	return true;
		}
		
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
				<i class="glyphicon glyphicon-list"></i> 工资基本数据导入
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
								<!-- <br/><font>1.导入EXCEL包括列有：<font color="blue">工号，姓名，薪资单位，身份证号</font></font> -->
								<br/><a href="${tenantPrefix}/user/salaryDownload.do?type=1" >工资基本表下载</a>
								
							</td>
						</tr>
						<tr>
							<td style="line-height:35px;text-align:left;padding-left:10px;" colspan="2">
								<label for="contractCompanyName">薪资单位:</label>
			                    <select id="contractCompanyName" class="width:98%;line-height:30px;">
									<option value="">请选择</option>
								</select>
								<input type="hidden" id="contractCompanyId" name="contractCompanyId" value="">
							</td>
						</tr>
						<tr>
							<td style="line-height:35px;text-align:left;padding-left:10px;" colspan="2">
								<c:forEach items="${dictInfoList }" var="dictInfo">
									<label><input name="salaryColume" type="checkbox" value="${dictInfo.value }-${dictInfo.priority}" />${dictInfo.name }</label>
								</c:forEach>
								&nbsp&nbsp&nbsp
								<label><a onclick="checkAll(0)">全选</a></label>
								&nbsp&nbsp&nbsp
								<label><a onclick="checkAll(1)">取消全选</a></label>
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
				</form>
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
		            	<span style='color:red;'> 工资月份：${item.salaryDate}</span>
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

