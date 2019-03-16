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
	</style>

	<script type="text/javascript">
		document.onmousedown = function(e) {};
		document.onmousemove = function(e) {};
		document.onmouseup = function(e) {};
		document.ondblclick = function(e) {};
		
		var xform;

		$(function() {
			
			$("#customerId").keydown(function(event) {    
                if (event.keyCode == 13) {    
                    var id = $("#customerId").val();
                    if (id !="") {
	                    $.getJSON('${tenantPrefix}/rs/customer/customerInfo', {
	                    	customerInfoId: id
	                    }, function(data) {
	          				$('#customerName').append('&nbsp;');
		          			
		          			$('#customerName').append(data.name);
		          			
		          		});
                    } else {
                    	alert("必须输入客户编号!");
                    }
                }    
            });
            
			createUserPicker({
				modalId: 'userPicker',
				showExpression: true,
				multiple: true,
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
		
		function startProcessInstance() {
			$('#xform').attr('action', '${tenantPrefix}/operationTest/process-operationTest-startProcessInstance.do');
			$('#xform').submit();
		}
    </script>
  </head>

  <body>
    <%@include file="/header/bpm-workspace3.jsp"%>
<form id="xform" method="post" action="${tenantPrefix}/operationTest/process-operationTest-startProcessInstance.do" class="xf-form" enctype="multipart/form-data">
    <div class="container">

	  <!-- start of main -->
      <section id="m-main" class="col-md-12" style="padding-top:65px;">

		<table width="100%" cellspacing="0" cellpadding="0" border="0" align="center" class="xf-table">
		  <tbody>
		    <tr>
			  <td width="25%" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
			    <label style="display:block;text-align:right;margin-bottom:0px;padding-top:10px;padding-bottom:10px;">下个环节&nbsp;</label>
			  </td>
			  <td width="75%" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top" colspan="3" rowspan="1">
			    <div id="nextStep"></div>
			  </td>
			</tr>
		  </tbody>
		</table>
		<script>
		  $.getJSON('${tenantPrefix}/rs/bpm/next', {
			  processDefinitionId: '<%= request.getParameter("processDefinitionId")%>',
			  activityId: '<%= request.getParameter("activityId")%>'
		  }, function(data) {
			  $('#nextStep').append('&nbsp;');
			  for (var i = 0; i < data.length; i++) {
				  $('#nextStep').append(data[i].name);
				  $('#activityId').val(data[i].id);
			  }
		  });
		</script>

      
		<input id="processDefinitionId" type="hidden" name="processDefinitionId" value="<%= request.getParameter("processDefinitionId")%>">
		<input id="bpmProcessId" type="hidden" name="bpmProcessId" value="<%= request.getParameter("bpmProcessId")%>">
		<input id="autoCompleteFirstTask" type="hidden" name="autoCompleteFirstTask" value="false">
		<input id="businessKey" type="hidden" name="businessKey" value="<%= request.getParameter("businessKey")%>">
		<input id="activityId" type="hidden" name="activityId" value="">
		
		<div id="xf-form-table">
			<div id="xf-1" class="xf-section">
				<h1 style="text-align:center;">申请单</h1>
			</div>
			
			<div id="xf-2" class="xf-section">
				<table class="xf-table" width="100%" cellspacing="0" cellpadding="0" border="0" align="center">
					<tbody>
						<tr id="xf-2-0">
							<td id="xf-2-0-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="25%">
								<div class="xf-handler">
									<label style="display:block;text-align:right;margin-bottom:0px;">请假原因</label>
								</div>
							</td>
							<td id="xf-2-0-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top" colspan="3" rowspan="1" width="75%">
								<div class="xf-handler">
									<input class="form-control required" name="reason" value="" required="true" style="margin-bottom:0px;" maxlength="200" type="text">
								</div>
							</td>
						</tr>
						<tr id="xf-2-1">
							<td id="xf-2-1-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left" width="25%">
								<div class="xf-handler">
									<label style="display:block;text-align:right;margin-bottom:0px;">请假类型</label>
								</div>
							</td>
							<td id="xf-2-1-1" class="xf-cell xf-cell-right xf-cell-bottom" colspan="3" rowspan="1" width="75%">
								<div class="xf-handler">
								<label class="radio-inline" style="padding-top:5px;padding-bottom:5px;">
									<input class="required" name="type" value="倒休" required="true" type="radio">
									倒休
								</label>
								<label class="validate-error" for="type" generated="true" style="display:none;"></label>
								<label class="radio-inline" style="padding-top:5px;padding-bottom:5px;">
									<input class="required" name="type" value="事假" required="true" type="radio">
									事假
								</label>
								<label class="validate-error" for="type" generated="true" style="display:none;"></label>
								<label class="radio-inline" style="padding-top:5px;padding-bottom:5px;">
									<input class="required" name="type" value="病假" required="true" type="radio">
									病假
								</label>
								<label class="validate-error" for="type" generated="true" style="display:none;"></label>
								<label class="radio-inline" style="padding-top:5px;padding-bottom:5px;">
									<input class="required" name="type" value="婚假" required="true" type="radio">
									婚假
								</label>
								<label class="validate-error" for="type" generated="true" style="display:none;"></label>
								</div>
							</td>
						</tr>
						<tr id="xf-2-2">
							<td id="xf-2-2-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left" width="25%">
								<div class="xf-handler">
									<label style="display:block;text-align:right;margin-bottom:0px;">开始时间</label>
								</div>
							</td>
							<td id="xf-2-2-1" class="xf-cell xf-cell-right xf-cell-bottom" width="25%">
								<div class="input-group datepicker date">
									<input class="form-control required" name="startDate" value="" required="true" style="background-color:white;cursor:default;" type="text">
									<span class="input-group-addon">
										<i class="glyphicon glyphicon-calendar"></i>
									</span>
								</div>
							</td>
							<td id="xf-2-2-2" class="xf-cell xf-cell-right xf-cell-bottom" colspan="2" rowspan="1" width="50%">
								<div class="xf-handler">
									<select class="form-control required" name="startDateTime" required="true" style="margin-bottom:0px;width:auto;">
										<option value="上午">上午</option>
										<option value="下午">下午</option>
									</select>
								</div>
							</td>
						</tr>
						<tr id="xf-2-3">
							<td id="xf-2-3-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left" width="25%">
								<div class="xf-handler">
									<label style="display:block;text-align:right;margin-bottom:0px;">结束时间</label>
								</div>
							</td>
							<td id="xf-2-3-1" class="xf-cell xf-cell-right xf-cell-bottom" width="25%">
								<div class="input-group datepicker date">
									<input class="form-control required" name="endDate" value="" required="true" style="background-color:white;cursor:default;" type="text">
									<span class="input-group-addon">
										<i class="glyphicon glyphicon-calendar"></i>
									</span>
								</div>
							</td>
							<td id="xf-2-3-2" class="xf-cell xf-cell-right xf-cell-bottom" colspan="2" rowspan="1" width="50%">
								<div class="xf-handler">
									<select class="form-control required" name="endDateTime" required="true" style="margin-bottom:0px;width:auto;">
										<option value="下午">下午</option>
										<option value="上午">上午</option>
									</select>
								</div>
							</td>
						</tr>
						<tr id="xf-2-4">
							<td id="xf-2-4-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="25%">
								<div class="xf-handler">
									<label style="display:block;text-align:right;margin-bottom:0px;">抄送人</label>
								</div>
							</td>
							<td id="xf-2-4-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top"  width="25%">
								
								<div class="input-group userPicker" style="width: 175px;">
								  <input id="_task_name_key" type="hidden" name="copyUserValue" class="input-medium" value="">
								  <input type="text" name="copyUserName" style="width: 175px;background-color:white;" value="" class="form-control" readonly>
								  <div class="input-group-addon"><i class="glyphicon glyphicon-user"></i></div>
							    </div>
							</td>
							<td id="xf-2-4-2" class="xf-cell xf-cell-right xf-cell-bottom" colspan="2" rowspan="1" width="50%">
								<div class="xf-handler">
									如有抄送功能，可仿照此例。
								</div>
							</td>
						</tr>
						<tr id="xf-2-5">
							<td id="xf-2-5-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left" width="25%">
								<div class="xf-handler">
									<label style="display:block;text-align:right;margin-bottom:0px;">客户编号</label>
								</div>
							</td>
							<td id="xf-2-5-1" class="xf-cell xf-cell-right xf-cell-bottom" width="25%">
								<div class="xf-handler">
									<input class="form-control required" id="customerId" name="customerId" value="" required="true" style="margin-bottom:0px;" maxlength="200" type="text">
								</div>
							</td>
							<td id="xf-2-5-2" class="xf-cell xf-cell-right xf-cell-bottom"  width="25%">
								<div class="xf-handler">
									<label style="display:block;text-align:right;margin-bottom:0px;">客户姓名</label>
								</div>
							</td>
							<td id="xf-2-5-3" class="xf-cell xf-cell-right xf-cell-bottom"  width="25%">
								<div id="customerName" class="xf-handler">
								</div>
							</td>
						</tr>
						<tr id="xf-2-6">
							<td id="xf-2-6-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="25%">
								<div class="xf-handler">
									<label style="display:block;text-align:right;margin-bottom:0px;">拟稿审批</label>
								</div>
							</td>
							<td id="xf-2-6-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top"  width="25%">
								
								<div class="input-group userPicker" style="width: 175px;">
								  <input id="_task_name_key" type="hidden" name="countersignUsers" class="input-medium" value="">
								  <input type="text" name="countersignUsersName" style="width: 175px;background-color:white;" value="" class="form-control" readonly>
								  <div class="input-group-addon"><i class="glyphicon glyphicon-user"></i></div>
							    </div>
							</td>
							<td id="xf-2-6-2" class="xf-cell xf-cell-right xf-cell-bottom" colspan="2" rowspan="1" width="50%">
								<div class="xf-handler">
									如有会签功能，可仿照此例。
								</div>
							</td>
						</tr>
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
	    <div class="text-center" style="padding-top:8px;">
			<!-- <button id="saveDraft" class="btn btn-default" type="button" onclick="taskOperation.saveDraft()">保存草稿</button> -->
			<button id="confirmStartProcess" class="btn btn-default" type="button" onclick="taskOperation.taskConf()">配置任务</button>
			<button id="confirmStartProcess" class="btn btn-default" type="button" onclick="startProcessInstance()">提交数据</button>
		</div>
	
	  </div>
    </div>
  </div>
</form>
</body>

</html>
