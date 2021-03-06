<%@page contentType="text/html;charset=UTF-8"%>
<%@include file="/taglibs.jsp"%>
<%pageContext.setAttribute("currentHeader", "bpm-workspace");%>
<%pageContext.setAttribute("currentMenu", "bpm-process");%>
<!doctype html>
<html lang="en">

  <head>
    <%@include file="/common/meta.jsp"%>
    <title><spring:message code="demo.demo.input.title" text="麦联"/></title>
    <%@include file="/common/s3.jsp"%>

	<!-- bootbox -->
    <script type="text/javascript" src="${cdnPrefix}/bootbox/bootbox.min.js"></script>
	<link href="${cdnPrefix}/xform3/styles/xform.css" rel="stylesheet">
    <script type="text/javascript" src="${cdnPrefix}/xform3/xform-packed.js"></script>

    <link type="text/css" rel="stylesheet" href="${cdnPrefix}/userpicker3-v2/userpicker.css">
    <script type="text/javascript" src="${cdnPrefix}/userpicker3-v2/userpickerbybpm.js"></script>
	<script type="text/javascript" src="${cdnPrefix}/operation/TaskOperation.js"></script>
	
	<style type="text/css">
		.xf-handler {
			cursor: auto;
		}
		pre { 
			white-space: pre-wrap;
		    word-wrap: break-word;
		    background-color:white;
		    border:0px
		}
	</style>

	<script type="text/javascript">
		document.onmousedown = function(e) {};
		document.onmousemove = function(e) {};
		document.onmouseup = function(e) {};
		document.ondblclick = function(e) {};

		var xform;

		$(function() {
			
			createUserPicker({
				modalId: 'userPicker',
				showExpression: true,
				searchUrl: '${tenantPrefix}/rs/user/search',
				treeUrl: '${tenantPrefix}/rs/party/treeNoAuth?partyStructTypeId=1',
				childUrl: '${tenantPrefix}/rs/party/searchUser'
			});
			
			setTimeout(function() {
				$('.datepicker').datepicker({
					autoclose: true,
					language: 'zh_CN',
					format: 'yyyy-mm-dd'
				})
			}, 500);
		})
		
    </script>
  </head>

  <body>
    <%@include file="/header/bpm-workspace3.jsp"%>
<form id="xform" method="post" action="${tenantPrefix}/Return/process-operationReturn-startProcessInstance.do" class="xf-form" enctype="multipart/form-data">
    <div class="container">

	  <!-- start of main -->
      <section id="m-main" class="col-md-12" style="padding-top:65px;">
		<input id="processInstanceId" type="hidden" name="processInstanceId" value="${processInstanceId}">
		<input id="humanTaskId" type="hidden"  name="humanTaskId" value="${humanTaskId}">
		<input id="activityId" type="hidden" name="activityId" value="">
		<input id="userId" type="hidden" name="userId" value="<%=request.getParameter("userId") %>>"> 
		<input id="businessId" name="businessId" type="hidden">
		
		<script>
		 
		//接收请求数据
			$(function() {
			 	var id=$("#processInstanceId").val(); 
					if (id !="") {
				    	$.getJSON('${tenantPrefix}/rs/processBusiness/getBusinessInfo', {
				        	id: id
				        	}, function(data) {
				            	for (var i = 0; i < data.length; i++) {
				            	   //alert(JSON.stringify(data)); 
				                   $("#theme").val(data[i].theme);
				                   $("#cc").val(data[i].cc);  
				                   $("#businessType").val(data[i].businessType);  
				                   $("#businessDetail").val(data[i].businessDetail);  
				                   $("#businessLevel").val(data[i].businessLevel);  
				                   $("#initiator").val(data[i].initiator);  
				                   $("#area").val(data[i].area);  
				                   $("#branchOffice").val(data[i].branchOffice);  
				                   $("#applyContent").val(data[i].applyContent); 
				                   $("#submitTimes").val(data[i].submitTimes);
				                   $("#applyCode").val(data[i].applyCode);
				                   
				                 //20180328 cz 业务细分是'启明' 的申请，标题改成 "业务申请抄送单（属地项目部）"
			                        if(data[i].businessDetail=="启明活动"){
			                        	var h1= document.getElementsByTagName("h1")[0];
			        			  	 	h1.innerHTML = "业务申请抄送单（属地项目部）";
			                        }
			                        replace(data[i].branchOffice,data[i].applyCode); 
				            	}
				            });
			         };
			})	
		
			//ckx 2018/9/29 特殊细分进行替换
			function replace(branchOffice,applyCode){
				 $.ajax({      
				        url: "${tenantPrefix}/workOperationCustom/getBusinessDetail.do",      
				        datatype: "json",
				        data:{applyCode:applyCode},
				        type: 'get',      
				        success: function (data) {
				        	if("true"==data){
				        		$("#hId").html("启明项目调整单");
				        		$("#areaLabelId").html('<font color="red">*</font>&nbsp;属地区域：');
				      		    $("#branchOfficeLabelId").html('<font color="red">*</font>&nbsp;属地项目部：');
				      		    $("#branchOffice").html(branchOffice.substring(0,branchOffice.length-3)+"项目部");
				        	}
				    		
				        },      
				        error: function(e){
				        	//失败后回调      
				            alert("服务器请求失败,请重试");  
				        }
				   });
			}
		
		</script>

      
		
		<div id="xf-form-table">
			<div id="xf-1" class="xf-section">
				<h1 style="text-align:center;">业务申请抄送单（分公司）</h1>
			</div>
			
			<div id="xf-2" class="xf-section">
				<table class="xf-table" width="100%" cellspacing="0" cellpadding="0" border="0" align="center">
					<tbody>
						<tr id="xf-2-0">
							<td id="xf-2-0-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" colspan="4">
								<div class="xf-handler">
									<label style="display:block;text-align:right;margin-bottom:0px;">提交次数： <input style="border:0px;width:10px" readonly type="text" id="submitTimes" >      &nbsp;&nbsp;申请单号:<input type="text" id="applyCode" style="border:0px" readonly></label>
								</div>
							</td>
						</tr>
						<tr id="xf-2-1">
							<td id="xf-2-1-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left" width="15%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;"><font color='red'>*</font>&nbsp;主题：</label>
								</div>
							</td>
							<td id="xf-2-1-1" class="xf-cell xf-cell-right xf-cell-bottom" colspan="3">
								<div class="xf-handler">
									<input id="theme" type="text" style="width:100%;border:0px" readonly>
								</div>
							</td>
						</tr>
						<tr id="xf-2-2">
							<td id="xf-2-2-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">抄送：</label>
								</div>
							</td>
							<td id="xf-2-2-1" class="xf-cell xf-cell-right xf-cell-bottom" colspan="3">
								<div class="xf-handler">
									<input id="cc" type="text" style="width:100%;border:0px" readonly>
								</div>
							</td>
						</tr>
						<tr id="xf-2-3">
							<td id="xf-2-3-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;"><font color='red'>*</font>&nbsp;申请业务类型：</label>
								</div>
							</td>
							<td id="xf-2-3-1" class="xf-cell xf-cell-right xf-cell-bottom">
								<div class="xf-handler">
									<input type="text" id="businessType"  style="width:100%;border:0px" readonly>
								</div>
							</td>
							<td id="xf-2-3-2" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;"><font color='red'>*</font>&nbsp;业务细分：</label>
								</div>
							</td>
							<td id="xf-2-3-3" class="xf-cell xf-cell-right xf-cell-bottom">
								<div class="xf-handler">
									<input type="text" id="businessDetail"  style="width:100%;border:0px" readonly>
								</div>
							</td>
						</tr>
						
						<tr id="xf-2-4">
							<td id="xf-2-4-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;"><font color='red'>*</font>&nbsp;业务级别：</label>
								</div>
							</td>
							<td id="xf-2-4-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" >
								<input type="text" id="businessLevel"  style="width:100%;border:0px" readonly>
							</td>
							<td id="xf-2-4-2" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
								<label style="display:block;text-align:center;margin-bottom:0px;">发起人：</label>
							</td>
							<td id="xf-2-4-3" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
								<div class="xf-handler">
									<input  id="initiator" type="text" readonly style="width:100%;border:0px">
								</div>
							</td>
						</tr>
						<tr id="xf-2-5">
							<td id="xf-2-5-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;"><font color='red'>*</font>&nbsp;大区：</label>
								</div>
							</td>
							<td id="xf-2-5-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
								<div class="xf-handler">
									<input type="text" id="area" style="width:100%;border:0px" readonly>
								</div>
							</td>
							<td id="xf-2-5-2" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;"><font color='red'>*</font>&nbsp;分公司：</label>
								</div>
							</td>
							<td id="xf-2-5-3" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
								<div class="xf-handler">
									<input type="text" id="branchOffice" style="width:100%;border:0px" readonly>
								</div>
							</td>
						</tr>
						<tr id="xf-2-6">
							<td id="xf-2-6-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;"><font color='red'>*</font>&nbsp;申请内容</label>
								</div>
							</td>
							<td id="xf-2-6-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" colspan="3">
								<div class="xf-handler">
									<textarea id="applyContent" name="applyContent" rows="10" cols="1" style="width:100%;border:0px" readonly></textarea>
								</div>
							</td>
						</tr>
						<tr id="xf-2-7">
							<td id="xg-2-7-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
								<label style="display:block;text-align:center;margin-bottom:0px;">附件:</label>
							</td>
							<td id="xf-2-7-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" colspan="3">
								<div class="col-md-8">
                       			 	<%@include file="/common/show_file.jsp" %>
                    			</div>
							</td>
						</tr>
						
					</tbody>
				</table>
				<table width="100%" cellspacing="0" cellpadding="0" border="0" align="center" class="table table-border">
				  <thead>
				    <tr>
					  <th>环节</th>
					  <th>操作人</th>
					  <th>时间</th>
					  <th>结果</th>
					</tr>
				  </thead>
				  <tbody>
					  <c:forEach var="item" items="${logHumanTaskDtos}">
					  <c:if test="${not empty item.completeTime}">
				    <tr>
					  <td>${item.name}</td>
					  <td><tags:isDelUser userId="${item.assignee}"/></td>
					  <td><fmt:formatDate value="${item.completeTime}" type="both"/></td>
					  <td>${item.action}</td>
					</tr>
					<c:if test="${item.action != '提交' && item.action != '重新申请'}">
						<tr style="border-top:0px hidden;">
							<td>批示内容</td>
							<td colspan="4"><pre>${item.comment}</pre></td>
						</tr>
					</c:if>
					  </c:if>
					  </c:forEach>
				  </tbody>
				</table>
			</div>
		</div>
		<br>
	  

    </section>
	<!-- end of main -->

  </div>
  <div class="navbar navbar-default navbar-fixed-bottom">
    <div class="container-fluid">
      <div class="text-center" style="padding-top:8px;">
          <button type="button" class="btn btn-default" onclick="javascript:history.back();">返回</button>
	
	  </div>
    </div>
  </div>
</form>

<%-- <form>
   <input type="hidden" name="humanTaskId" value="${humanTaskId}"/>
   <input type="hidden" name="comment" value=""/>
  <div class="input-group userPicker" style="width:200px;">
	<input id="_task_name_key" type="hidden" name="userId" class="input-medium" value="">
	<input type="text" class="form-control" name="username" placeholder="" value="">
	<div class="input-group-addon"><i class="glyphicon glyphicon-user"></i></div>
  </div>
     <br>
     <button id="confirmStartProcess" class="btn btn-default" type="button" onclick="startProcessInstance()">提交数据</button>
</form> --%>
</body>

</html>
