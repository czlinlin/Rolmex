<%@page contentType="text/html;charset=UTF-8"%>
<%@include file="/taglibs.jsp"%>
<%
	pageContext.setAttribute("currentHeader", "bpm-workspace");
%>
<%
	pageContext.setAttribute("currentMenu", "bpm-process");
%>
<!doctype html>
<html lang="en">

<head>
<%@include file="/common/meta.jsp"%>
<title><spring:message code="demo.demo.input.title" text="麦联" /></title>
<%@include file="/common/s3.jsp"%>

<!-- bootbox -->
<script type="text/javascript" src="${cdnPrefix}/bootbox/bootbox.min1.js"></script>
<link href="${cdnPrefix}/xform3/styles/xform.css" rel="stylesheet">
<script type="text/javascript" src="${cdnPrefix}/xform3/xform-packed.js"></script>

<link type="text/css" rel="stylesheet"
	href="${cdnPrefix}/userpicker3-v2/userpicker.css">
<script type="text/javascript"
	src="${cdnPrefix}/userpicker3-v2/userpickerbybpm.js"></script>
<script type="text/javascript"
	src="${cdnPrefix}/operation/TaskOperation.js"></script>
    <script type="text/javascript" src="${cdnPrefix}/operation/operation.js?v=1.20"></script>

<style type="text/css">
.xf-handler {
	cursor: auto;
}
pre { 
	white-space: pre-wrap;
    word-wrap: break-word;
    background-color:white;
    border:0px;
    padding:0;
    margin:0;
}
input[type="password"]{border : 1px solid #F2F2F2;background:#eee;width:100%;padding:3px 12px;width: 100%}
textarea{border : 1px solid #F2F2F2;background:#eee;padding:6px 12px;width: 100%;margin:0;}
.padding-6-12{padding:6px 12px;}
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
		
		ROOT_URL = '${tenantPrefix}';
		taskOperation = new TaskOperation();
		
		//完成任务（同意，不同意，驳回）
		function completeTask(flag) {
			if($("#comment").val().replace(/(^\s*)|(\s*$)/g, "") == ""&&flag==0 || $("#comment").val().replace(/(^\s*)|(\s*$)/g, "") == ""&&flag==2
					||$("#comment").val() == "同意"&&flag==0 || $("#comment").val() == "同意"&&flag==2){
				alert("不同意和驳回请填写意见");
				return false;
			}
			//每次审核人审核时都先检验该流程的状态是否是已撤回
            $.ajax({      
	            url: '${tenantPrefix}/rs/bpm/getStatus',      
	            datatype: "json",
	            data:{"processInstanceId": $("#processInstanceId").val(),"humanTaskId":$("#humanTaskId").val(),"userId":$("#userId").val()},
	            type: 'get',      
	            success: function (e) {
	            	if(e == 'error'){
	            		alert("该申请已撤回，暂无法审核。");
	            		return false;
	            	}
	            	if(e == 'noAuth'){
	            		alert("您无权审核。");
	            		return false;
	            	}
	            	var conf={
                        "formId":"xform",
                        "checkUrl":"${tenantPrefix}/rs/customer/opteraion-verifyPassword",
                        "actionUrl": '${tenantPrefix}/processGroupBusiness/process-operationGroupBusinessApproval-completeTask.do?flag='+flag,
                        "iptPwdId":"txtPrivateKey"
	                }

                    operationSubmit(conf);
	            },      
	            error: function(e){      
	            	loading.modal('hide');
	                alert("服务器请求失败,请重试");  
	            }
	       });
            
		}
		
		//接收请求数据
		$(function() {
		 	var id=$("#processInstanceId").val(); 
				if (id !="") {
			    	$.getJSON('${tenantPrefix}/rs/processGroupBusiness/getGroupBusinessInfo', {
			        	id: id
			        	}, function(data) {
			            	for (var i = 0; i < data.length; i++) {
			            	   //alert(JSON.stringify(data));
			            	   $("#submitTimes").val(data[i].submitTimes);
			            	   $("#span_submitTimes").html(data[i].submitTimes);
			            	   $("#applyCode").val(data[i].applyCode);
			            	   $("#span_applyCode").html(data[i].applyCode);
			            	   $("#theme").html(data[i].theme);
			                   $("#cc").html(data[i].cc);  
			                   $("#businessType").html(data[i].businessType);  
			                   $("#businessDetail").html(data[i].businessDetail);  
			                   $("#businessLevel").html(data[i].businessLevel);  
			                   $("#initiator").html(data[i].initiator);  
			                   $("#applyContent").html(data[i].applyContent); 
	                    }
	                });
		         };
		         /* 这是先前判断是否显示审批步骤的参数，现利用它判断是否是多分支 （1.常规 0.多分支） */
		         <c:if test="${isShowAuditStep=='1'}">
			         //审核环节
			         $.ajax({
		    				url:"${tenantPrefix}/dict/getProcessPostInfoByProcessInstanceId.do",
		    				data:{processInstanceId:id},
		    				dataType:"json",
		    				type:"post",
		    				success:function(data){
		    					//console.log(data);
		    					$('#nextStep').append(data.whole);
		    				},
		    				error:function(){
		    					alert("获取流程审核人岗位信息出错！");
		    				}
		    			});
		         </c:if>
		         <c:if test="${isShowAuditStep=='0'}">
		         	var businessDetailId = $("#businessDetailId").val();
		         	var userId = $("#applyUserId").val();
		         	var isMoney = $("#isMoney").val();
		         	var money = $("#money").val();
		            $.ajax({
	    				url:"${tenantPrefix}/dict/getBranchProcessStep.do",
	    				data:{businessDetailID:businessDetailId,userId:userId,isMoney:isMoney,money:money},
	    				dataType:"json",
	    				type:"post",
	    				success:function(data){
	    					//alert(JSON.stringify(data));
	    					if($("#nextStep").html() != ''){
	  		  					$('#nextStep').html('');
	  		  				}
	  	    				$('#nextStep').append(data[0].conditionNode);
	    				},
	    				error:function(){
	    					alert("获取流程审核人岗位信息出错！");
	    				}
	    			});
		         </c:if>
		})	
		
		function empty(){
			if($("#comment").val() == "同意"){
				$("#comment").val('');
			}
		}
		function getAgree(){
			if($("#comment").val() == ""){
				$("#comment").val('同意');
			}
		}
    </script>
</head>

<body>
	<%@include file="/header/bpm-workspace3.jsp"%>
	<form id="xform" method="post" class="xf-form"
		enctype="multipart/form-data">
		<div class="container">

			<!-- start of main -->
			<section id="m-main" class="col-md-12" style="padding-top: 65px;">
				<%-- <c:if test="${isShowAuditStep=='1'}"> --%>
				<table width="100%" cellspacing="0" cellpadding="0" border="0" align="center" class="xf-table">
				  <tbody>
				    <tr>
					  <td width="25%" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
					    <label style="display:block;text-align:center;margin-bottom:0px;padding-top:10px;padding-bottom:10px;">审核环节&nbsp;</label>
					  </td>
					  <td width="75%" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top" colspan="3" rowspan="1">
					    <div id="nextStep"></div>
					  </td>
					</tr>
				  </tbody>
			   </table>
			   <%-- </c:if> --%>
				<input id="processInstanceId" type="hidden" name="processInstanceId" value="${processInstanceId}"> 
				<input id="humanTaskId" type="hidden" name="humanTaskId" value="${humanTaskId}"> 
				<input id="activityId" type="hidden" name="activityId" value=""> 
				<input id="userId" type="hidden" name="userId" value='<%=userId%>'> 
				<!--businessDetailId isMoney money为实现多分支审批步骤-->
				<input id="businessDetailId" type="hidden" name="businessDetailId" value='${businessDetailId}'> 
				<input id="isMoney" type="hidden" name="isMoney" value='${ismoney}'> 
				<input id="money" type="hidden" name="money" value='${money}'> 
				<input id="applyUserId" type="hidden" name="applyUserId" value="${applyUserId}"/> 
	
				<div id="xf-form-table">
					<div id="xf-1" class="xf-section">
						<c:if test="${!empty bpmProcessTitle }">
							<h1 style="text-align: center;">${bpmProcessTitle}<!-- 业务审批单 --></h1>
						</c:if>
						<c:if test="${empty bpmProcessTitle }">
							<h1 style="text-align: center;">业务审批单</h1>
						</c:if>
					</div>

					<div id="xf-2" class="xf-section">
						<table class="xf-table" width="100%" cellspacing="0"
							cellpadding="0" border="0" align="center">
							<tbody>
								<tr id="xf-2-0">
									<td id="xf-2-0-0"
										class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left padding-6-12"
										colspan="4">
										<div class="xf-handler">
											<label
												style="display: block; text-align: right; margin-bottom: 0px;">提交次数：
												<span id="span_submitTimes"></span>
												<input style="border: 0px; width: 10px;display:none;" readonly type="text"
												id="submitTimes" name="submitTimes">
												&nbsp;&nbsp;申请单号:<span id="span_applyCode"></span><input type="text" id="applyCode"
												name="applyCode" value="${code}"
												style="border:0px;display:none;" readonly>
											</label>
										</div>
									</td>
								</tr>
								<tr id="xf-2-1">
									<td id="xf-2-1-0"
										class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left"
										width="15%">
										<div class="xf-handler">
											<label
												style="display: block; text-align: center; margin-bottom: 0px;">&nbsp;主题：</label>
										</div>
									</td>
									<td id="xf-2-1-1" class="xf-cell xf-cell-right xf-cell-bottom padding-6-12" colspan="3">
										<div class="xf-handler" id="theme"></div>
									</td>
								</tr>
								<tr id="xf-2-2">
									<td id="xf-2-2-0"
										class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left">
										<div class="xf-handler">
											<label style="display: block; text-align: center; margin-bottom: 0px;">抄送：</label>
										</div>
									</td>
									<td id="xf-2-2-1" class="xf-cell xf-cell-right xf-cell-bottom padding-6-12" colspan="3">
										<div class="xf-handler" id="cc"></div>
									</td>
								</tr>
								<tr id="xf-2-3">
									<td id="xf-2-3-0"
										class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left">
										<div class="xf-handler">
											<label style="display: block; text-align: center; margin-bottom: 0px;">申请业务类型：</label>
										</div>
									</td>
									<td id="xf-2-3-1" class="xf-cell xf-cell-right xf-cell-bottom padding-6-12">
										<div class="xf-handler" id="businessType"></div>
									</td>
									<td id="xf-2-3-2"
										class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left">
										<div class="xf-handler">
											<label style="display: block; text-align: center; margin-bottom: 0px;">业务细分：</label>
										</div>
									</td>
									<td id="xf-2-3-3" class="xf-cell xf-cell-right xf-cell-bottom padding-6-12">
										<div class="xf-handler" id="businessDetail"></div>
									</td>
								</tr>

								<tr id="xf-2-4">
									<td id="xf-2-4-0"
										class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
										<div class="xf-handler">
											<label style="display: block; text-align: center; margin-bottom: 0px;">业务级别：</label>
										</div>
									</td>
									<td id="xf-2-4-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left padding-6-12">
										<div class="xf-handler" id="businessLevel"></div>
									</td>
									<td id="xf-2-4-2" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
										<label style="display: block; text-align: center; margin-bottom: 0px;">发起人：</label>
									</td>
									<td id="xf-2-4-3" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left padding-6-12">
										<div class="xf-handler" id="initiator"></div>
									</td>
								</tr>
								
								<c:if test="${ismoney=='1'}">
									<tr id="trmoney">
										<td class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" style="text-align:center;"><label>金额:</label></td>
										<td id="moneyHtml" colspan="3" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left padding-6-12">
											${money}
										</td>
									</tr>
								</c:if>
								
								<tr id="xf-2-6">
									<td id="xf-2-6-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
										<div class="xf-handler">
											<label style="display: block; text-align: center; margin-bottom: 0px;">申请内容</label>
										</div>
									</td>
									<td id="xf-2-6-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left padding-6-12" colspan="3">
										<div class="xf-handler" style="padding:0;margin:0;">
											<pre>${groupBusiness.applyContent}</pre>
										</div>
									</td>
								</tr>
								<tr id="xf-2-7">
									<td id="xf-2-7-0"
										class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
										<div class="xf-handler">
											<label style="display: block; text-align: center; margin-bottom: 0px;">附件：</label>
										</div>
									</td>
									<td id="xf-2-7-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left padding-6-12" colspan="3">
										<div class="col-md-8" style="padding:0;margin:0;">
											<%@include file="/common/show_file.jsp"%>
										</div>
									</td>
								</tr>
								<tr id="xf-2-8">
									<td id="xf-2-8-0"
										class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
										<div class="xf-handler">
											<label id="commentText"
												style="display: block; text-align: center; margin-bottom: 0px;"><font style="color: red;width:100%">*</font>审批人意见：</label>
										</div>
									</td>
									<td id="xf-2-8-1"
										class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left"
										colspan="3">
										<div class="xf-handler"  style="padding:0;margin:0;">
											<textarea id="comment" placeholder="请输入审批意见" name="comment" maxlength="300" style="width:100%" rows="5" cols="20" onfocus="empty()" onblur="getAgree()" required>同意</textarea>
										</div>
									</td>
								</tr>
								<tr>
								<td class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
									<div class="xf-handler">
										<label style="display: block; text-align: center; margin-bottom: 0px;width:100%">
											<font style="color: red;width:100%">*</font>操作密码
										</label>
									</div>
								</td>
								<td class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left"
										colspan="3">
									<div class="xf-handler" style="padding:0;margin:0;">
										<input name="txtPrivateKey" type="password" maxlength="25" placeholder="请输入操作密码"
											id="txtPrivateKey" onblur='isPwd();' /> <input
											id="isPwdRight" name="isPwdRight" type="hidden" />
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
					  <th>审核时长</th>
					</tr>
				  </thead>
				  <tbody>
					  <c:forEach var="item" items="${logHumanTaskDtos}">
					  <c:if test="${not empty item.completeTime}">
				    <tr>
					  <td>${item.name}</td>
					  <td><tags:user userId="${item.assignee}"/></td>
					  <td><fmt:formatDate value="${item.completeTime}" type="both"/></td>
					  <td>${item.action}</td>
					  <td>${item.auditDuration}</td>
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
		<br />
		<br />
		<br />
		<br />
		<div class="navbar navbar-default navbar-fixed-bottom">
			<div class="container-fluid">
				<div class="text-center" style="padding-top: 8px;">
					<div class="text-center" style="padding-top: 8px;">
						<!-- <button id="confirmStartProcess" class="btn btn-default" type="button" onclick="taskOperation.taskConf()">配置任务</button> -->
						<button id="complete" onclick="completeTask(1)"
							class="btn btn-default" type="button">同意</button>
						<button id="backProcess" class="btn btn-default"
							onclick="completeTask(2)" type="button">驳回</button>
						<button id="disagree" name="approval" class="btn btn-default"
							onclick="completeTask(0)" type="button" value="不同意">不同意</button>
						<button type="button" class="btn btn-default"
							onclick="javascript:history.back();">返回</button>
						<%-- <a href="${tenantPrefix}/rs/Business/enclosures?key=value">下载</a> --%>
					</div>

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
