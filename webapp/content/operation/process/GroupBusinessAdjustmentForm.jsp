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
   <%-- <script type="text/javascript" src="${cdnPrefix}/userpicker3-v2/userpickerbybpm.js"></script> --%>
    <script type="text/javascript" src="${cdnPrefix}/userpicker3-v2/userpickercustom.js"></script>
	<script type="text/javascript" src="${cdnPrefix}/operation/TaskOperation.js"></script>
	<script type="text/javascript" src="${cdnPrefix}/userpicker3-v2/userpickerbycopy.js?v=1.0"></script>
	<style type="text/css">
		.xf-handler {
			cursor: auto;
			margin:0;
		}
		input[type="text"],input[type="number"]{border : 1px solid #F2F2F2;background:#eee;width:100%;padding:3px 12px;}
		textarea{border : 1px solid #F2F2F2;background:#eee;padding:6px 12px;}
		select{border : 1px solid #F2F2F2}
		pre{
			white-space: pre-wrap;
            word-wrap: break-word;
            background-color:white;
            border:0px
		}
		.padding-6-12{padding:6px 12px;}
	</style>
	<script type="text/javascript">
		document.onmousedown = function(e) {};
		document.onmousemove = function(e) {};
		document.onmouseup = function(e) {};
		document.ondblclick = function(e) {};

		$(function() {
			/* createUserPicker({
		        modalId: 'ccUserPicker',
		        targetId: 'ccDiv',
		        multiple: true,
		        showExpression: true,
		        searchUrl: '${tenantPrefix}/rs/user/searchVNoMe',
		        treeUrl: '${tenantPrefix}/rs/party/treeNoAuth?partyStructTypeId=1',
		        childUrl: '${tenantPrefix}/rs/party/searchUserNoMe'
		    }) */
			createUserPickerCopy({
        		modalId: 'userPicker',
        		showExpression: true,
        		multiple: true,
        		searchUrl: '${tenantPrefix}/rs/user/search',
        		treeNoPostUrl: '${tenantPrefix}/party/asyncTree.do?partyStructTypeId=1&notViewPost=true',
        		treeUrl: '${tenantPrefix}/party/asyncTree.do?partyStructTypeId=1&notViewPost=true',
        		childUrl: '${tenantPrefix}/rs/party/searchUser',
        		childPostUrl: '${tenantPrefix}/rs/party/searchPost'
        	});
		});
		

		//申请被驳回并带回请求数据
		$(function() {
		 	var id = $("#processInstanceId").val(); 
				if (id !="") {
			    	$.getJSON('${tenantPrefix}/rs/processGroupBusiness/getGroupBusinessInfo', {
			        	id: id
			        	}, function(data) {
			            	for (var i = 0; i < data.length; i++) {
			            		//alert(JSON.stringify(data)); 
			                   $("#theme").val(data[i].theme);
			                   $("#businessType").val(data[i].businessType);
			                   $("#span_businessType").html(data[i].businessType);
			                   $("#businessDetail").val(data[i].businessDetail);
			                   $("#span_businessDetail").html(data[i].businessDetail);
			                   $("#businessLevel").val(data[i].businessLevel);
			                   $("#span_businessLevel").html(data[i].businessLevel);
			                   $("#initiator").val(data[i].initiator);
			                   $("#span_initiator").html(data[i].initiator);
			                   $("#applyContent").val(data[i].applyContent);
			                   $("#submitTimes").val(data[i].submitTimes);
			                   $("#span_submitTimes").html(data[i].submitTimes);
			                   $("#applyCode").val(data[i].applyCode);
			                   $("#span_applyCode").html(data[i].applyCode);
			                   $("#businessId").val(data[i].id);
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
		         	var userId = $("#userId").val();
		         	var isMoney = $("#isMoney").val();
		         	var money = $("#ipt_money").val();
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
		       //根据条件注掉其中一个撤销申请按钮
		         $.ajax({      
		            url: '${tenantPrefix}/rs/bpm/removeButton',      
		            datatype: "json",
		            data:{"processInstanceId": $("#processInstanceId").val()},
		            type: 'get',      
		            success: function (e) {
		            	if(e == "normalReject"){
							  $("#endProcess").css('display',"none");
						}else{
							  $("#revoke").css('display',"none");
						}
		            },      
		            error: function(e){      
		            	loading.modal('hide');
		                alert("服务器请求失败,请重试");  
		            }
		       });
		});
		//金额修改，失去焦点重新加载环节
		function getBranchProcessStepMoney(money){
			$('#nextStep').html('');
   			var userId = $("#userId").val();
   			var isMoney = "1";
   			var businessDetailId = $("#businessDetailId").val();
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
		}
		//金额修改，失去焦点重新加载环节
		function getBranchProcessStepMoney(money){
			$('#nextStep').html('');
   			var userId = $("#userId").val();
   			var isMoney = "1";
   			var businessDetailId = $("#businessDetailId").val();
   			$.ajax({
   				url:"${tenantPrefix}/dict/getBranchProcessStep.do",
   				data:{businessDetailID:businessDetailId,userId:userId,isMoney:isMoney,money:money},
   				dataType:"json",
   				type:"post",
   				success:function(data){
   					//alert(JSON.stringify(data));
   					if("请检查金额的输入是否正确！" == data[0].note){
   						alert(data[0].note);
   						return false;
   					}
   					if($("#nextStep").html() != ''){
 		  				$('#nextStep').html('');
 		  			}
 	    			$('#nextStep').append(data[0].conditionNode);
   				},
   				error:function(){
   					alert("获取流程审核人岗位信息出错！");
   				}
   			});
		}
		function completeTask(flag) {
    		if(flag == 3){
    			var i = 1;
    			var num = $("#submitTimes").attr('value');
    			var number =parseInt(num)+parseInt(i);
    			$("#submitTimes").val(number);
    		}
    		if($("#theme").val().replace(/(^\s*)|(\s*$)/g, "") == ""){
				alert("主题不能为空");
				return false;
			}
    		if($("#applyContent").val().replace(/(^\s*)|(\s*$)/g, "") == ""){
				alert("请输入内容");
				return false;
			}
    		if(flag == 4){
				var msg = "确定要撤销申请吗,请确认？";  
	            if (!confirm(msg)){  
	                return false;  
	            }  
			}
    		if(flag == 3 && $("#theme").val().length > '100'){
    			var msg = "主题已超出100字，继续提交系统会自动截取100字以内内容，返回修改请取消";  
	            if (!confirm(msg)){  
	                return false;  
	            }  
    		}
    		if(flag == 3 && $("#applyContent").val().length > '5000'){
    			var msg = "申请内容已超出5000字，继续提交系统会自动截取5000字以内内容，返回修改请取消";  
	            if (!confirm(msg)){  
	                return false;  
	            }  
    		}
    		//调整人重新申请和撤销申请先检验该流程的状态是否是已撤回或驳回发起人
            $.ajax({      
	            url: '${tenantPrefix}/rs/bpm/getStatus',      
	            datatype: "json",
	            data:{"processInstanceId": $("#processInstanceId").val(),"humanTaskId":$("#humanTaskId").val(),"userId":$("#userId").val(),"resource":'adjustment'},
	            type: 'get',      
	            success: function (e) {
	            	if(e == 'error'){
	            		alert("该申请状态已变更，您已无权操作。");
	            		return false;
	            	}
	            	if(e == 'noAuth'){
	            		alert("您无权操作。");
	            		return false;
	            	}
	            	var loading = bootbox.dialog({
	                    message: '<p style="width:90%;margin:0 auto;text-align:center;">提交中...</p>',
	                    size: 'small',
	                    closeButton: false
	             	});
	    			$('#xform').attr('action', '${tenantPrefix}/processGroupBusiness/process-operationGroupBusinessApproval-completeTask.do?flag='+flag);
	    			$('#xform').submit();
	            },      
	            error: function(e){      
	            	loading.modal('hide');
	                alert("服务器请求失败,请重试"); 
	            }
	       });
		}
		function MaxWords(obj){
		   document.getElementById("recordNum").style.display = "block";
           var text1 = document.getElementById("applyContent").value;
           var len;//记录已输入字数
           if (text1.length >= 5000) {
               document.getElementById("applyContent").value = text1.substr(0, 5000);
               len = 5000;
           }
           else {
               len = text1.length;
           }
           var show = len + " / 5000";
           document.getElementById("recordNum").innerText = show;
		}
	
		
/*==============================================================================================  */
	  
		//流程撤回后点击撤销申请，确认框提示
		function confirmOperation(){
			var msg = "确定要撤销申请吗,请确认？";  
            if (!confirm(msg)){  
                return false;  
            }
          //调整人撤销申请先检验该流程的状态是否是已撤回或驳回发起人
            $.ajax({      
	            url: '${tenantPrefix}/rs/bpm/getStatus',      
	            datatype: "json",
	            data:{"processInstanceId": $("#processInstanceId").val(),"humanTaskId":$("#humanTaskId").val(),"userId":$("#userId").val(),"resource":'adjustment'},
	            type: 'get',      
	            success: function (e) {
	            	if(e == 'error'){
	            		alert("该申请状态已变更，您已无权操作。");
	            		return false;
	            	}
	            	if(e == 'noAuth'){
	            		alert("您无权操作。");
	            		return false;
	            	}
	            	var loading = bootbox.dialog({
	                    message: '<p style="width:90%;margin:0 auto;text-align:center;">提交中...</p>',
	                    size: 'small',
	                    closeButton: false
	             	});
	            	$('#xform').attr('action', '${tenantPrefix}/bpm/workspace-endProcessInstance.do?processInstanceId=${processInstanceId}&humanTaskId=${humanTaskId}');
	    			$('#xform').submit();
	            },      
	            error: function(e){      
	            	loading.modal('hide');
	                alert("服务器请求失败,请重试"); 
	            }
	       });
		}
		
    	function getInputLevel(){
    		if($("#ipt_ismoney").val()=="1"){
    			var money=$("#ipt_money").val();
    			if(money.length>12){
    				money=money.substr(0,12);
    				$("#ipt_money").val(money);
    			}
        		if(parseInt(money)>=3000){
        			//$("#busLevel").html("<option>S</option>");
        			$("#span_businessLevel").html("S");
        			$("#businessLevel").val("S");
        		}
        		else{
        			//$("#busLevel").html("<option>"+ old_Level+"</option>");
        			var pri_level=$("#ipt_pri_level").val();
        			$("#span_businessLevel").html(pri_level);
        			$("#businessLevel").val(pri_level);
        		}
    		}
    	}
    </script>
  </head>

  <body>
    <%@include file="/header/bpm-workspace3.jsp"%>
<form id="xform" method="post" action="${tenantPrefix}/Return/process-operationReturn-startProcessInstance.do" class="xf-form" enctype="multipart/form-data">
    <div class="container">

	  <!-- start of main -->
      <section id="m-main" class="col-md-12" style="padding-top:65px;">
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
		<input id="processDefinitionId" type="hidden" name="processDefinitionId" value="${processDefinitionId}">
		<input id="processInstanceId" type="hidden" name="processInstanceId" value="${processInstanceId}">
		<input id="humanTaskId" type="hidden" name="humanTaskId" value="${humanTaskId}">
		<input id="businessId" name="businessId" type="hidden">
		<input id="activityId" type="hidden" name="activityId" value="${activityId}">
		<input id="userId" type="hidden" name="userId" value="<%=userId %>">
		<!--businessDetailId isMoney money为实现多分支审批步骤-->
		<input id="businessDetailId" type="hidden" name="businessDetailId" value='${businessDetailId}'> 
		<input id="isMoney" type="hidden" name="isMoney" value='${ismoney}'> 
		<%-- <input id="money" type="hidden" name="money" value='${money}'> --%> 
		<div id="xf-form-table">
			<div id="xf-1" class="xf-section">
				<c:if test="${!empty bpmProcessTitle }">
					<h1 style="text-align: center;">${bpmProcessTitle}</h1>
				</c:if>
				<c:if test="${empty bpmProcessTitle }">
					<h1 style="text-align:center;">业务申请调整单</h1>
				</c:if>
			</div>
			
			<div id="xf-2" class="xf-section">
				<table class="xf-table" width="100%" cellspacing="0" cellpadding="0" border="0" align="center">
					<tbody>
						<tr id="xf-2-0">
							<td id="xf-2-0-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left padding-6-12" colspan="4" width="100%">
								<div class="xf-handler">
									<label style="display:block;text-align:right;margin-bottom:0px;">提交次数：<span id="span_submitTimes"></span>
									 <input style="border:0px;width:10px;display:none;" type="text" id="submitTimes" name="submitTimes" readonly>      
									 &nbsp;&nbsp;申请单号:<span id="span_applyCode"></span><input type="text" id="applyCode" name="applyCode" style="border:0px;display:none;" readonly></label>
								</div>
							</td>
						</tr>
						<tr id="xf-2-1">
							<td id="xf-2-1-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left" width="50%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;"><font color='red'>*</font>&nbsp;主题：</label>
								</div>
							</td>
							<td id="xf-2-1-1" class="xf-cell xf-cell-right xf-cell-bottom" colspan="3" width="50%">
								<div class="xf-handler">
									<input id="theme" name="theme" type="text" style="width:100%" maxlength="100" placeholder="请输入主题">
								</div>
							</td>
						</tr>
						<tr id="xf-2-2">
							<td id="xf-2-2-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left" width="50%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">抄送：</label>
								</div>
							</td>
							<td id="xf-2-2-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top" colspan="3" width="75%">
								<div class="input-group userPicker">
	                                <input id="btnPickerMany" type="hidden" name="copyUserValue" class="input-medium"  value="${groupBusiness.copyUserValue}">
	                                <input type="text" id="userName" name="cc" style="width: 800px;"
                                     value="${groupBusiness.cc}" class="form-control" readOnly placeholder="点击后方图标即可选人">
	                                <div id="ccDiv" class="input-group-addon"><i class="glyphicon glyphicon-user"></i></div>
                            	</div>
							</td>
						</tr>
						<tr id="xf-2-3">
							<td id="xf-2-3-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left" width="25%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">申请业务类型：</label>
								</div>
							</td>
							<td id="xf-2-3-1" class="xf-cell xf-cell-right xf-cell-bottom padding-6-12	" width="25%">
								<div class="xf-handler">
									<%-- <select id="busType" onchange="getBusinessDetail()">
										<option id="backType" value = "<%=request.getParameter("businessType") %>"><%=request.getParameter("businessType") %></option>
										<input type="hidden" id="businessType" name="businessType" >
									</select> --%>
									<span id="span_businessType"></span>
									<input type="text" id="businessType" name="businessType" style="width:100%;display:none;" readonly>
								</div>
							</td>
							<td id="xf-2-3-2" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left" width="25%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">业务细分：</label>
								</div>
							</td>
							<td id="xf-2-3-3" class="xf-cell xf-cell-right xf-cell-bottom padding-6-12" width="25%">
								<div class="xf-handler">
									<%-- <select id="busDetail" onchange="setLevel()">
										<option id="backDetail" value = "<%=request.getParameter("businessDetail") %>"></option>
										<input type="hidden" id="businessDetail" name="businessDetail" >
									</select> --%>
									<span id="span_businessDetail"></span>
									<input type="text" id="businessDetail" name="businessDetail" style="width:100%;display:none;" readonly>
								</div>
							</td>
						</tr>
						
						<tr id="xf-2-4">
							<td id="xf-2-4-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="25%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">&nbsp;业务级别：</label>
								</div>
							</td>
							<td id="xf-2-4-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left padding-6-12" width="25%">
								<%-- <select id="busLevel" name="businessLevel">
									<option id="backLevel" value = "<%=request.getParameter("businessLevel") %>"></option>
									<input type="hidden" id="businessLevel" name="businessLevel" >
								</select> --%>
								<span id="span_businessLevel"></span>
								<input type="text" id="businessLevel" name="businessLevel" style="width:100%;display:none;" readonly>
							</td>
							<td id="xf-2-4-2" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="25%">
								<label style="display:block;text-align:center;margin-bottom:0px;">发起人：</label>
							</td>
							<td id="xf-2-4-3" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left padding-6-12" width="25%">
								<div class="xf-handler">
									<span id="span_initiator"></span>
									<input type="text" id="initiator" name="initiator" style="width:100%;display:none;" readonly>
								</div>
							</td>
						</tr>
						
						<c:if test="${ismoney=='1'}">
							<tr id="trmoney">
								<td class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" style="text-align:center;"><label><font color='red'>*</font>&nbsp;金额:</label></td>
								<td colspan="3" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
									<%-- <input type="text" name="money" value="${money}" maxlength="8"  placeholder="请输入金额"/> --%>
									<input id="ipt_money" type="number" name="money" min="1" max="10" value="${money}" onkeyup="getInputLevel()" onblur="getBranchProcessStepMoney(this.value)" placeholder="请输入金额"/>
									<input type="hidden" id="ipt_ismoney" name="ipt_ismoney" value="${ismoney}"/>
									<input type="hidden" id="ipt_pri_level" value="${level}"/>
								</td>
							</tr>
						</c:if>
						
						<tr id="xf-2-6">
							<td id="xf-2-6-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="25%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;"><font color='red'>*</font>&nbsp;申请内容</label>
								</div>
							</td>
							<td id="xf-2-6-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" colspan="3" width="75%">
								<div class="xf-handler">
									<textarea id="applyContent"name="applyContent" rows="10" maxlength="5000"   placeholder="请输入申请内容" style="width:100%" onkeyup="MaxWords(this)" onblur="MaxWords(this)"></textarea>
									<label id="recordNum" style="display: none;text-align:right;" class="control-label col-md-12"></label>
								</div>
							</td>
						</tr>
						<tr id="xf-2-7">
							<td id="xf-2-7-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" colspan="4" width="100%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">上传附件</label>
								</div>
							</td>
						</tr>
						<tr id="xf-2-8">
							<td id="xf-2-8-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" colspan="4">
		                        <div class="col-md-8">
		                            <%@include file="/common/_uploadFile.jsp"%>
		                            <span style="color:gray;"> 请添加小于200M的附件 </span>
		                        </div>
	                    	</td>
						</tr>
						<tr id="xf-2-9">
							<td id="xf-2-9-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
		                        <div class="xf-handler">
		                            <label id="historyEnclosure" style="display:block;text-align:center;margin-bottom:0px;">历史附件：</label>
		                        </div>
	                    	</td>
							<td id="xf-2-9-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" colspan="3">
		                         <div class="col-md-8">
                            		<%@include file="/common/show_edit_file.jsp" %>
                        		</div>
	                    	</td>
						</tr>
						
					</tbody>
				</table>
				<table id="myTable" width="100%" cellspacing="0" cellpadding="0" border="0" align="center" class="table table-border">
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
  <br/><br/><br/><br/>
  <div class="navbar navbar-default navbar-fixed-bottom">
    <div class="container-fluid">
      <div class="text-center" style="padding-top:8px;">
	    <div class="text-center" style="padding-top:8px;">
			<!-- <button id="saveDraft" class="btn btn-default" type="button" onclick="taskOperation.saveDraft()">保存草稿</button> -->
			<!-- <button id="confirmStartProcess" class="btn btn-default" type="button" onclick="taskOperation.taskConf()">配置任务</button> -->
			<button id="adjustment" class="btn btn-default" type="button" onclick="completeTask(3)">调整申请</button>
			<button id="revoke" class="btn btn-default" type="button" onclick="completeTask(4)">撤销申请</button>
			<button id="endProcess" type="button" class="btn btn-default" onclick="confirmOperation()">撤销申请</button>
			<button type="button" class="btn btn-default" onclick="javascript:history.back();">返回</button>
			
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
