<%@page contentType="text/html;charset=UTF-8"%>
<%@include file="/taglibs.jsp"%>
<%pageContext.setAttribute("currentHeader", "auth");%>
<%pageContext.setAttribute("currentMenu", "auth");%>

<%pageContext.setAttribute("currentChildMenu", "角色管理");%>
<!doctype html>
<html>

  <head>
    <%@include file="/common/meta.jsp"%>
    <title>麦联</title>
    <%@include file="/common/s3.jsp"%>
	<script type="text/javascript" src="${cdnPrefix}/bootbox/bootbox.min1.js"></script>
    <link href="${cdnPrefix}/xform3/styles/xform.css" rel="stylesheet">
    <script type="text/javascript" src="${cdnPrefix}/xform3/xform-packed.js"></script>
    <link type="text/css" rel="stylesheet" href="${cdnPrefix}/userpicker3-v2/userpicker.css">
    <script type="text/javascript" src="${cdnPrefix}/userpicker3-v2/userpickercustomaudit.js"></script>
    <!--  -->
    <script type="text/javascript">
		$(function() {
			window.parent.closeLoading();
			
			window.parent.$.showMessage($('#m-success-tip-message').html(), {
                position: 'top',
                size: '50',
                fontSize: '20px'
            });
			
		    $("input[name='iptAuditOpen']").click(function(){
		    	var status=$("input[name='iptAuditOpen']:checked").val();
		    	$.ajax({
                    url: "${tenantPrefix}/rs/auth/setPersonConfig",
                    type: "POST",
                    data: {status:status},
                    timeout: 10000,
                    success: function (data) {
                    	if(data!=undefined&&data!=null){
            				if(data.result=="ok"){
            					alert("设置成功");
            					return;
            				}
            				else{
            					alert(data.msg);
            					return
            				}
            			}
                    },
                    error: function (XMLHttpRequest, textStatus, errorThrown) {
                        alert("[" + XMLHttpRequest.status + "]error，请求失败")
                    },
                    complete: function (xh, status) {
                        if (status == "timeout"){
                        	alert("请求超时");
                        	return false;
                        }  
                    }
                });
		    })
		    
		    $("input[name='iptValidateOpen']").click(function(){
		    	var status=$("input[name='iptValidateOpen']:checked").val();
		    	$.ajax({
                    url: "${tenantPrefix}/rs/auth/setPersonValidateConfig",
                    type: "POST",
                    data: {status:status},
                    timeout: 10000,
                    success: function (data) {
                    	if(data!=undefined&&data!=null){
            				if(data.result=="ok"){
            					alert("设置成功");
            					return;
            				}
            				else{
            					alert(data.msg);
            					return
            				}
            			}
                    },
                    error: function (XMLHttpRequest, textStatus, errorThrown) {
                        alert("[" + XMLHttpRequest.status + "]error，请求失败")
                    },
                    complete: function (xh, status) {
                        if (status == "timeout"){
                        	alert("请求超时");
                        	return false;
                        }  
                    }
                });
		    });
		});
		
		var fnUploadExcel=function(){
	    	if($("#excelFile").val()==""){
	    		alert("请选择导入的EXCEL文件");
	    		return false;
	    	}
	    	else if($("#excelFile").val().lastIndexOf(".xls") < 0){
	    		alert("只能上传EXCEL文件");
	    		return false;
	    	}
	    	
	    	$("#dataWorkNumberForm").attr("action","${tenantPrefix}/user/person-info-worknumber-import.do");
	    	window.parent.dialogLoading("正在进行导入，请勿刷新页面");
	    	return true;
		}
		
		/*sjx  */
		$(function () {
            //审批人
            createUserPicker({
                modalId: 'leaderPicker',//这个 其实是弹出的窗口的div id
                targetId: 'leaderDiv', //这个是点击哪个 会触发弹出窗口
                inputStoreIds: {iptid: "leaderId", iptname: "leaderName"},//存储已选择的ID和name的input的id
                auditId: 'ulapprover',//显示审批步骤
                showExpression: true,
                multiple: true,
                searchUrl: '${tenantPrefix}/rs/user/searchVNoMe',
                treeUrl: '${tenantPrefix}/party/asyncTree.do?partyStructTypeId=1&notViewPost=true&notAuth=false',
                childUrl: '${tenantPrefix}/rs/party/searchUserNoMe'
            });

            createUserPicker({
                modalId: 'ccUserPicker',
                targetId: 'ccDiv',
                inputStoreIds: {iptid: "btnPickerMany", iptname: "userName"},//存储已选择的ID和name的input的id
                multiple: true,
                showExpression: true,
                searchUrl: '${tenantPrefix}/rs/user/searchVNoMe',
                treeUrl: '${tenantPrefix}/party/asyncTree.do?partyStructTypeId=1&notViewPost=true&notAuth=false',
                childUrl: '${tenantPrefix}/rs/party/searchUserNoMe'
            })
        })		
		function authorization(){
			var userIds = $("#leaderId").val();
			if(userIds == ""){
				alert("请选择人员");
				return false;
			}
			$("#personAlias").submit();
		}
    </script>
    <c:if test="${isOpenOtherNameOpter=='1'}">
	    <script type="text/javascript">
	    	$(function(){
	    		$("input[name='iptOtherNameOpen']").click(function(){
			    	var status=$("input[name='iptOtherNameOpen']:checked").val();
			    	$.ajax({
	                    url: "${tenantPrefix}/rs/auth/setOtherNameOpenConfig",
	                    type: "POST",
	                    data: {status:status},
	                    timeout: 10000,
	                    success: function (data) {
	                    	if(data!=undefined&&data!=null){
	            				if(data.result=="ok"){
	            					alert(data.msg);
	            					window.location.reload();//刷新当前页面（此开关控制其他功能）
	            					return;
	            				}
	            				else{
	            					alert(data.msg);
	            					return
	            				}
	            			}
	                    },
	                    error: function (XMLHttpRequest, textStatus, errorThrown) {
	                        alert("[" + XMLHttpRequest.status + "]error，请求失败")
	                    },
	                    complete: function (xh, status) {
	                        if (status == "timeout"){
	                        	alert("请求超时");
	                        	return false;
	                        }  
	                    }
	                });
			    });
	    	});
	    </script>
    </c:if>
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
	
  <section id="m-main" class="col-md-12" style="padding-top:65px;">
      <div class="panel panel-default">
        <div class="panel-heading">
		  <i class="glyphicon glyphicon-list"></i>
		  人事管理参数配置
		</div>

	<form id="authDataForm" method="post" class="form-horizontal">
		<table id="tb1" style="width:100%;">
			<%-- <tr>
	  			<td colspan="2">
					<div class="form-group" style="margin-top:15px;">
					    <div class="col-md-5 col-md-offset-2">
					      <button type = "button" id="button" class="btn btn-default" onclick ="save();"><spring:message code='core.input.save' text='保存'/></button>
						  &nbsp;
					      <button type="button" onclick="history.back();" class="btn btn-default"><spring:message code='core.input.back' text='返回'/></button>
					      <input type="hidden" id="iptDataIds" name="iptDataIds" value="${dataIds}"/>
					    </div>
					  </div>
	  			</td>
	  		</tr> --%>
			<tr>
	           <td>
	            	是否开启审批：
	           </td>
	           <td>
	           		<label class="checkbox-inline">
	           			<input type="radio" name="iptAuditOpen" ${status=="1"?"checked='checked'":""} value="1" /> 开启
	           		</label>
	           		<label class="checkbox-inline">
	           			<input type="radio" name="iptAuditOpen" ${status=="0"?"checked='checked'":""} value="0" /> 关闭
	           		</label>
	           </td>
	  		</tr>
	  		<tr>
	           <td>
	            	是否开启花名册验证：
	           </td>
	           <td>
	           		<label class="checkbox-inline">
	           			<input type="radio" name="iptValidateOpen" ${isValidate=="1"?"checked='checked'":""} value="1" /> 开启
	           		</label>
	           		<label class="checkbox-inline">
	           			<input type="radio" name="iptValidateOpen" ${isValidate=="0"?"checked='checked'":""} value="0" /> 关闭
	           		</label>
	           </td>
	  		</tr>
	  		<c:if test="${isOpenOtherNameOpter=='1'}">
		  		<tr>
		           <td>
		            	是否开启“别名”功能：
		           </td>
		           <td>
		           		<div style="color:blue">此功能只有“超级管理员”可见</div>
		           		<label class="checkbox-inline">
		           			<input type="radio" name="iptOtherNameOpen" ${isOtherNameOpen=="1"?"checked='checked'":""} value="1" /> 开启
		           		</label>
		           		<label class="checkbox-inline">
		           			<input type="radio" name="iptOtherNameOpen" ${isOtherNameOpen=="0"?"checked='checked'":""} value="0" /> 关闭
		           		</label>
		           </td>
		  		</tr>
		  	</c:if>
	  	</table>
	</form>

</div>
</section>
	<c:if test="${isImport=='1'}"> 
		<section id="m-main" class="col-md-12">
		      <div class="panel panel-default">
		        <div class="panel-heading">
				  <i class="glyphicon glyphicon-list"></i>
				  初始化导入花名册工号
				</div>
		<form id="dataWorkNumberForm" method="post" class="form-horizontal"  enctype="multipart/form-data">
			<table id="tb1" style="width:100%;">
				<tr>
					<td style="line-height:35px;text-align:left;padding-left:10px;" colspan="2">
						<span style='color:red;'>提示：</span>
						<br/>1.数据导入成功后，功能将隐藏
						<br/>2.导入EXCEL包括列有：<span style='color:blue'>工号，姓名，用户名，公司，部门</span>
					</td>
				</tr>
				<tr>
					<td style="line-height:35px;">选择导入的EXCEL文件：</td>
					<td style="padding-left:5px;"><input type="file" name="excelFile" id="excelFile"/><div style="color:gray;text-align:left;">(请选择要上传的EXCEL文件)</div></td>
				</tr>
				<tr>
					<td style="line-height:50px;" colspan="2">
						<button type="submit" class="btn btn-primary" id="btn_importExcel" onclick="fnUploadExcel();">上传并导入数据</button>
					</td>
				</tr>
			</table>
		</form>
		</div>
		</section>
	</c:if>
	
<c:if test="${failMap != null}">
	<section id="m-main" class="col-md-12" style="padding-top:65px;">
      <div class="panel panel-default">
        <div class="panel-heading">
		  <i class="glyphicon glyphicon-list"></i>
		  导入失败，具体信息如下<span style='color:red;'>(请及时复制保存，若页面刷新将消失)</span>
		</div>

	<form id="authDataForm" method="post" class="form-horizontal">
		<table id="tb1" style="width:100%;" >
		<c:forEach items="${failMap}" var="item">	
			<tr>
	           <td>
	            	<span style='color:red;'> 工号：${item.workNumber}</span>
	           </td>
	          
	           <td>
	            	<span style='color:red;'> 姓名： ${item.fullName}</span>
	           </td>
	          
	           <td>
	            	<span style='color:red;'>  公司：${item.company}</span>
	           </td>
	          
	           <td>
	            	<span style='color:red;'> 部门：${item.departmentName}</span>
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
</div>
<c:if test="${isOtherNameOpen == 1}"><!-- 当别名功能是开启状态才显示 -->
	 <section id="m-main" class="col-md-12" style="padding-top:65px;">
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
      <div class="panel panel-default">
	        <div class="panel-heading">
			  <i class="glyphicon glyphicon-list"></i>
			  个人别名修改配置
			</div>
	
			<form id="personAlias" method="post" class="form-horizontal" action="personAlias.do">
				<table id="tb1" style="width:100%;">
					<tr>
						<td style="line-height:35px;text-align:left;padding-left:10px;" colspan="2">
							<span style='color:red;'>提示：</span>
							<br/><font>1.别名功能开启后，此功能可用。</font>
							<br/><font>2.一个用户只能修改一次别名；如需再次修改，可操作此功能。</font>
							<br/><font>3.选择一个或多个用户人员，保存成功后生效。</font>
						</td>
					</tr>
					<tr style="width:100%">
			           <td style="width:20%">
			            	人员选择：
			           </td>
			           <td style="width:80%">
			           		<div class="input-group userPicker" style="width:100%;">
		                         <input id="leaderId" type="hidden" name="userId" value="">
		                         <input type="text" id="userName" name="userName" class="form-control required"
		                                value="<tags:user userId="${model.leader}"></tags:user>" minlength="2"
		                                maxlength="50" class="form-control" readOnly placeholder="点击后方图标即可选人">
		                         <div id='leaderDiv' class="input-group-addon"><i class="glyphicon glyphicon-user"></i></div>
		                    </div>
			           </td>
			  		</tr>
			  		<tr>
			  			<td style="line-height:50px;" colspan="2">
			           		<button type="button" onclick="authorization()" class="btn btn-primary">保存</button>
			           	</td>
			  		</tr>
			  	</table>
			</form>
		</div>
	</section>
</c:if>
</body>
</html>

