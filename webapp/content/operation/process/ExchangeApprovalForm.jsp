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
    <script type="text/javascript" src="${cdnPrefix}/bootbox/bootbox.min1.js"></script>
	<link href="${cdnPrefix}/xform3/styles/xform.css" rel="stylesheet">
    <script type="text/javascript" src="${cdnPrefix}/xform3/xform-packed.js"></script>

    <link type="text/css" rel="stylesheet" href="${cdnPrefix}/userpicker3-v2/userpicker.css">
    <script type="text/javascript" src="${cdnPrefix}/userpicker3-v2/userpickerbybpm.js"></script>
	<script type="text/javascript" src="${cdnPrefix}/operation/TaskOperation.js"></script>
	  <script type="text/javascript" src="${cdnPrefix}/operation/operation.js?v=1.20"></script>
	
	<style type="text/css">
		.xf-handler {
			cursor: auto;
		}
		.xf-table td{border:1px solid gray}
		pre { 
			white-space: pre-wrap;
		    word-wrap: break-word;
		    background-color:white;
		    border:0px
		}
		#xtable td{border: 1px solid #B5B5B5}
	</style>

	<script type="text/javascript">
		document.onmousedown = function(e) {};
		document.onmousemove = function(e) {};
		document.onmouseup = function(e) {};
		document.ondblclick = function(e) {};

		var xform;
		
		
		ROOT_URL = '${tenantPrefix}';
		taskOperation = new TaskOperation();
		
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
                        "actionUrl": '${tenantPrefix}/Exchange/process-operationExchangeApproval-completeTask.do?flag='+flag,
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
    <style>
    	.centerdiv{width:100%;border-spacing:0;text-border:border-collapse;}
    	.centerdiv td{border:1px solid #ccc;}
    	.centerdiv td.f_td{text-align:right;}
    	.centerdiv td.f_r_td{padding-left:6px;}
    </style>
  </head>

  <body>
    <%@include file="/header/bpm-workspace3.jsp"%>
<form id="xform" method="post" class="xf-form" enctype="multipart/form-data">
    <div class="container">
      <section id="m-main" class="col-md-12" style="padding-top:65px;">
		<input id="processInstanceId" type="hidden" name="processInstanceId" value="${processInstanceId}">
		<input id="humanTaskId" type="hidden"  name="humanTaskId" value="${humanTaskId}">
		<input id="activityId" type="hidden" name="activityId" value="">
		<input id="userId" type="hidden" name="userId" value="<%=userId %>">
		<input id="exchangeId" type=hidden name="exchangeId" value="">
		
		<div id="xf-form-table">
			<div id="xf-2" class="xf-section">
				${detailHtml}				
				<table class="centerdiv showpic">
					<tbody>
						<tr style="text-align:center">
							<c:if test="${resource == 0}">
								<td colspan="2" style="">附件（换货申请人身份证复印件）</td>
							</c:if>
							<c:if test="${resource == 1}">
								<td colspan="2" style="">附件</td>
							</c:if>
			            </tr>
		            	<tr>
		            		<td colspan="2" style="text-align:left;padding:10px;"><%@include file="/common/show_file.jsp" %></td>
			            </tr>
						<tr style="text-align:center"><td style="border-top:none;" colspan="2">审批意见</td></tr>
						<tr id="xf-2-12">
							<td id="xf-2-12-1" colspan="2"><textarea id="comment" name="comment" onfocus="empty()" onblur="getAgree()" required style="width:100%;background:#eee;" rows="5" cols="20">同意</textarea></td>
						</tr>
					<tr>
						<td>
							<div class="xf-handler">
								<label style="display:block;text-align:center;margin-bottom:0px;width:100%"><font style="color:red">*</font>操作密码</label>
							</div>
						</td>
						<td>
							<div class="xf-handler">
								 <input name="txtPrivateKey" type="password" maxlength="25" id="txtPrivateKey"  style="background:#eee;width:100%" onblur='isPwd();'/>
                         		 <input id="isPwdRight" name="isPwdRight" type="hidden"/>
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
    </section>
  </div>
  <br/><br/><br/>
  <div class="navbar navbar-default navbar-fixed-bottom">
    <div class="container-fluid">
      <div class="text-center" style="padding-top:8px;">
	    <div class="text-center" style="padding-top:8px;">
			<button id="confirmStartProcess" class="btn btn-default" type="button" onclick="completeTask(1)">同意</button>
			<button id="backProcess" class="btn btn-default" type="button" onclick="completeTask(2)">驳回</button>
			<button id="disagree" name="approval" class="btn btn-default" type="button" onclick="completeTask(0)" value="不同意">不同意</button>
			<button type="button" class="btn btn-default" onclick="javascript:history.back();">返回</button>
		</div>
	
	  </div>
    </div>
  </div>
</form>
</body>
</html>
