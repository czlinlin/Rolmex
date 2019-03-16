<%@page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
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
	
	<style type="text/css">
		.xf-handler {
			cursor: auto;
		}
		input[type="text"]{border : 1px solid #F2F2F2;height:25px}
		textarea{border : 1px solid #F2F2F2}
		select{border : 1px solid #F2F2F2}
		pre{
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
		
		ROOT_URL = '${tenantPrefix}';
		taskOperation = new TaskOperation();
		
		
		function getSystemName() {
			var  myselect=document.getElementById("system");
			var index=myselect.selectedIndex ;  
			var text=myselect.options[index].text;
			$("#systemName").val(text);
	   }
		
		 //调用接口，根据经销商编号，获取直销oa上存的对应信息：姓名 电话 等
		 $(function() {
					$("#number").keydown(function(event) {    
						if (event.keyCode == 13) {  
							var id = $("#number").val();
							//alert(id);
		                	if (id !="") {
			                    $.getJSON('${tenantPrefix}/rs/varUser/userInfo', {
			                    	customerInfoId: id
			                    }, function(data) {
			          				//$("#realName").html(''+''+data.name);
				          			$("#name").val(data.name);
				          			//$("#welfare").html(data.rank);
				          			$("#contact").val(data.mobile);
			          				
				          		});
		                    } else {
		                    	alert("必须输入客户编号!");
		                    }
		                }    
		            });
		
			setTimeout(function() {
				$('.datepicker').datepicker({
					autoclose: true,
					language: 'zh_CN',
					format: 'yyyy-mm-dd'
				})
			}, 500);
		}) 
		
		//申请被驳回并带回请求数据
		$(function() {
		 	var id=$("#processInstanceId").val(); 
				if (id !="") {
			    	$.getJSON('${tenantPrefix}/rs/processFreeze/getFreezeInfo', {
			        	id: id
			        	}, function(data) {
			            	for (var i = 0; i < data.length; i++) {
			            	   //alert(JSON.stringify(data)); 
			                   $("#ucode").val(data[i].ucode);
			                   $("#name").val(data[i].name);  
			                   $("#contact").val(data[i].contact);  
			                   $("#salesLevel").val(data[i].salesLevel);  
			                   $("#welfareLevel").val(data[i].welfareLevel);  
			                   $("#activationState").val(data[i].activationState);
			                   var systemId = "";
	                    		if(data[i].system == "恒旭"){
	                    			systemId = 1;
	                    			$("#systemName").val(data[i].system);
	                    		}else if(data[i].system == "大家和"){
	                    			systemId = 2;
	                    			$("#systemName").val(data[i].system);
	                    		}else if(data[i].system == "易成"){
	                    			systemId = 3;
	                    			$("#systemName").val(data[i].system);
	                    		}else if(data[i].system == "正泰"){
	                    			systemId = 4;
	                    			$("#systemName").val(data[i].system);
	                    		}else if(data[i].system == "二部"){
	                    			systemId = 5;
	                    			$("#systemName").val(data[i].system);
	                    		}
	                    		$("#system").val(systemId);
			                   $("#aboveBoard").val(data[i].aboveBoard);  
			                   $("#frozenState").val(data[i].frozenState);  
			                   $("#area").val(data[i].area);
			                   $("#director").val(data[i].director);  
			                   $("#directorContact").val(data[i].directorContact);  
			                   $("#branchOffice").val(data[i].branchOffice);  
			                   $("#idNumber").val(data[i].idNumber);
			                   if(data[i].applyMatter == "冻结"){
			                	   $("#frozen").attr("checked",true);
			                   }else if(data[i].applyMatter == "解冻"){
			                	   $("#thaw").attr("checked",true);
			                   }else if(data[i].applyMatter == "开除"){
			                	   $("#expel").attr("checked",true);
			                   }else if(data[i].applyMatter == "限制"){
			                	   $("#limit").attr("checked",true);
			                   }else if(data[i].applyMatter == "取消限制"){
			                	   $("#cancelLimit").attr("checked",true);
			                   }else if(data[i].applyMatter == "其他"){
			                	   $("#other").attr("checked",true);
			                   }
			                   $("#applyContent").val(data[i].applyContent);
			                   $("#freezeId").val(data[i].id);
		        			}
				        });
			    	
				    	 //获取抄送人  
	                    $.getJSON('${tenantPrefix}/workOperationCustom/getFormCopyName.do', {
	                    	id: id
	                    }, function(data) {
	                    	if(data != ''){
	                    		$("#copyNames").html(data);
	                    	}
	                    });
				    	
			         };
			         //根据条件注掉其中一个撤销申请按钮
			         $.ajax({      
				            url: '${tenantPrefix}/rs/bpm/removeButton',      
				            datatype: "json",
				            data:{"processInstanceId": $("#processInstanceId").val()},
				            type: 'get',      
				            success: function (e) {
				            	//alert(JSON.stringify(e));
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
			})	
		//相关button操作
		function completeTask(flag) {
			 if(flag == 3){
    			var i = 1;
    			var num = $("#submitTimes").attr('value');
    			var number =parseInt(num)+parseInt(i);
    			$("#submitTimes").val(number);
	    	}
			if($("#applyContent").val().replace(/(^\s*)|(\s*$)/g, "") == ""){
				alert("请输入内容");
				return
			}
			if(flag == 4){
				var msg = "确定要撤销申请吗,请确认？";  
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
	    			$('#xform').attr('action', '${tenantPrefix}/processFreeze/process-operationFreezeApproval-completeTask.do?flag='+flag);
	    			$('#xform').submit();
	            },      
	            error: function(e){      
	            	loading.modal('hide');
	                alert("服务器请求失败,请重试"); 
	            }
	       });
    		
		}
		 
		
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
    </script>
  </head>

  <body>
    <%@include file="/header/bpm-workspace3.jsp"%>
<form id="xform" method="post" action="${tenantPrefix}/testReturn/process-operationTestReturn-startProcessInstance.do" class="xf-form" enctype="multipart/form-data">
    <div class="container">

	  <!-- start of main -->
      <section id="m-main" class="col-md-12" style="padding-top:65px;">
     <!--  <table width="100%" cellspacing="0" cellpadding="0" border="0" align="center" class="xf-table">
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
		</table> -->
		<input id="processInstanceId" type="hidden" name="processInstanceId" value="${processInstanceId}">
		<input id="humanTaskId" type="hidden" name="humanTaskId" value="${humanTaskId}">
		<input id="freezeId" type="hidden" name="freezeId">
		<input id="userId" type="hidden" name="userId" value="<%=userId %>">
		<script>
		  <%-- $.getJSON('${tenantPrefix}/rs/bpm/whole', {
			  processDefinitionId: '<%= request.getParameter("processDefinitionId")%>',
			  activityId: '<%= request.getParameter("activityId")%>',
			  isWhole:true
		  }, function(data) {
			  $('#nextStep').append('&nbsp;');
			  for (var i = 0; i < data.length; i++) {
				  $('#nextStep').append(data[i].name);
				  
			  }
		  }); --%>
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
		</script>
		
		
		<div id="xf-form-table">
			<div id="xf-1" class="xf-section">
				<h1 style="text-align:center;">冻结/解冻调整单</h1>
			</div>
			
			<div id="xf-2" class="xf-section">
				<table class="xf-table" width="100%" cellspacing="0" cellpadding="0" border="0" align="center">
					<tbody>
						<tr id="xf-2-0">
							<td id="xf-2-0-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
								<div class="xf-handler" align="center">
									<label>编号：</label>
								</div>
							</td>
							<td id="xf-2-0-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
								<div class="xf-handler">
									<input id="ucode" type="text" name="ucode" style="width:100%" readonly>
								</div>
							</td>
							<td id="xf-2-0-2" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
								<div class="xf-handler" align="center">
									<label>姓名：</label>
								</div>
							</td>
							<td id="xf-2-0-3" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
								<div class="xf-handler">
									<input id="name" type="text" name="name" style="width:100%" readonly>
								</div>
							</td>
							<td id="xf-2-0-4" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
								<div class="xf-handler" align="center">
									<label>联系方式：</label>
								</div>
							</td>
							<td id="xf-2-0-5" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
								<div class="xf-handler">
									<input id="contact" type="text" name="contact" style="width:100%" readonly>
								</div>
							</td>
						</tr>
						<tr id="xf-2-1">
							<td id="xf-2-1-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">销售级别：</label>
								</div>
							</td>
							<td id="xf-2-1-1" class="xf-cell xf-cell-right xf-cell-bottom">
								<div class="xf-handler">
									<input type="text" id="salesLevel" name="salesLevel" style="width:100%" readonly>
								</div>
							</td>
							<td id="xf-2-1-2" class="xf-cell xf-cell-right xf-cell-bottom">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">福利级别：</label>
								</div>
							</td>
							<td id="xf-2-1-3" class="xf-cell xf-cell-right xf-cell-bottom">
								<div class="xf-handler" >
									<input type="text" id="welfareLevel" name="welfareLevel" style="width:100%" readonly>
								</div>
							</td>
							<td id="xf-2-1-4" class="xf-cell xf-cell-right xf-cell-bottom">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">激活状态：</label>
								</div>
							</td>
							<td id="xf-2-1-5" class="xf-cell xf-cell-right xf-cell-bottom">
								<div class="xf-handler" >
									<input type="text" id="activationState" name="activationState" style="width:100%" readonly>
								</div>
							</td>
						</tr>
						<tr id="xf-2-2">
							<td id="xf-2-2-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left">
								<div class="xf-handler" align="center">
									<label>所属体系：</label>
								</div>
							</td>
							<td id="xf-2-2-1" class="xf-cell xf-cell-right xf-cell-bottom">
								<div class="xf-handler">
									<!-- <input type="text" id="system" name="system" style="width:100%" readonly> -->
									<select class="form-control required" id="system" name="system" onchange="getSystemName()">
										<c:forEach items="${systemlist}" var="item">
		  									<option value="${item.value}" >${item.name}</option>
		  								</c:forEach>
									</select>
									<input type="hidden" id="systemName" name="systemName">
								</div>
							</td>
							<td id="xf-2-2-2" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left" align="center">
								<label>上属董事：</label>
							</td>
							<td id="xf-2-2-3" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left">
								<div class="xf-handler">
									<input type="text" name="aboveBoard" style="width:100%" id="aboveBoard">
								</div>
							</td>
							<td id="xf-2-2-4" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">冻结状态：</label>
								</div>
							</td>
							<td id="xf-2-2-5" class="xf-cell xf-cell-right xf-cell-bottom">
								<div class="xf-handler">
									<input type="text" name="frozenState" style="width:100%" id="frozenState" readonly>
								</div>
							</td>
						</tr>
						<tr id="xf-2-3">
							<td id="xf-2-3-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">所属区域：</label>
								</div>
							</td>
							<td id="xf-2-3-1" class="xf-cell xf-cell-right xf-cell-bottom">
								<div class="xf-handler">
									<input type="text" id="area" name="area" style="width:100%" readonly>
								</div>
							</td>
							<td id="xf-2-3-2" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">董事姓名：</label>
								</div>
							</td>
							<td id="xf-2-3-3" class="xf-cell xf-cell-right xf-cell-bottom">
								<div class="xf-handler">
									<input type="text" id="director" name="director" style="width:100%">
								</div>
							</td>
							<td id="xf-2-3-4" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">联系方式：</label>
								</div>
							</td>
							<td id="xf-2-3-5" class="xf-cell xf-cell-right xf-cell-bottom">
								<div class="xf-handler">
									<input type="text" id="directorContact" name="directorContact" style="width:100%">
								</div>
							</td>
						</tr>
						
							
						
						<tr id="xf-2-4">
							<td id="xf-2-4-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">所属分公司：</label>
								</div>
							</td>
							<td id="xf-2-4-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
								<div class="xf-handler">
									<input type="text" id="branchOffice" name="branchOffice" style="width:100%" readonly>
								</div>
							</td>
							<td id="xf-2-4-2" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
								<label style="display:block;text-align:center;margin-bottom:0px;">身份证号：</label>
							</td>
							<td id="xf-2-4-3" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" colspan="3">
								<input type="text" id="idNumber" name="idNumber" style="width:100%">
							</td>
						</tr>
						<tr id="xf-9-9">
							<td id="xf-9-9-9" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="8%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">抄送：</label>
								</div>
							</td>
							<td id="xf-8-8-8" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" colspan="5">
								<span id="copyNames"></span>
							</td>
						</tr>
						<tr id="xf-2-5">
							<td id="xf-2-5-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="8%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">申请受理事项：</label>
								</div>
							</td>
							<td id="xf-2-5-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" colspan="5">
								<div class="xf-handler">
									<input type="radio" id="frozen" name="applyMatter" value="冻结">冻结&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
									<input type="radio" id="expel" name="applyMatter" value="开除">开除&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
									<input type="radio" id="thaw" name="applyMatter" value="解冻">解冻&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
									<input type="radio" id="limit" name="applyMatter" value="限制">限制&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
									<input type="radio" id="cancelLimit" name="applyMatter" value="取消限制">取消限制&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
									<input type="radio" id="other" name="applyMatter" value="其他">其他
								</div>
							</td>
						</tr>
						<tr id="xf-2-6">
							<td id="xf-2-6-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">申请内容：</label>
								</div>
							</td>
							<td id="xf-2-6-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" colspan="5">
								<div class="xf-handler">
									<textarea id="applyContent" name="applyContent" rows="10" maxlength="5000" onkeyup="MaxWords(this)" onblur="MaxWords(this)" style="width:100%"></textarea>
									<label id="recordNum" style="display: none;text-align:right;" class="control-label col-md-12"></label>
								</div>
							</td>
						</tr>
						<tr id="xf-2-7">
							<td id="xf-2-7-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" colspan="6">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">上传附件</label>
								</div>
							</td>
							
						</tr>
						<tr id="xf-2-8">
							<td id="xf-2-8-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" colspan="6">
		                        <div class="col-md-8">
		                            <%@include file="/common/_uploadFile.jsp"%>
		                            <span style="color:gray;"> 请添加小于200M的附件 </span>
		                        </div>
	                    	</td>
						</tr>
						<tr id="xf-2-9">
							<td id="xf-2-9-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="8%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">历史附件</label>
								</div>
							</td>
							<td id="xf-2-9-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" colspan="5">
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
			<button id="adjustment" class="btn btn-default" type="button" onclick="completeTask(3)">调整申请</button>
			<button id="revoke" class="btn btn-default" type="button" onclick="completeTask(4)">撤销申请</button>
			<button id="endProcess" type="button" class="btn btn-default" onclick="confirmOperation()">撤销申请</button>
			<button type="button" class="btn btn-default" onclick="javascript:history.back();">返回</button>
			
		</div>
	
	  </div>
    </div>
  </div>
</form>


</body>

</html>
