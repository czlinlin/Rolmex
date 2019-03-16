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
<%-- <script type="text/javascript" src="${cdnPrefix}/userpicker3-v2/userpickerbybpm.js"></script> --%>
<script type="text/javascript"
	src="${cdnPrefix}/userpicker3-v2/userpickercustom.js"></script>
<script type="text/javascript"
	src="${cdnPrefix}/operation/TaskOperation.js"></script>
<%-- <script type="text/javascript" src="${cdnPrefix}/userpicker3-v2/userpickercustom.js"></script> --%>
<script type="text/javascript" src="${cdnPrefix}/userpicker3-v2/userpickerbycopy.js?v=1.0"></script>
<style type="text/css">
	.xf-handler {
		cursor: auto;
		margin:0;
	}
	input[type="text"],input[type="number"]{border : 1px solid #F2F2F2;background:#eee;width:100%;padding:3px 12px;}
	textarea{border : 1px solid #F2F2F2;background:#eee;padding:6px 12px;}
	select{border:0;}
	.xf-table td{height:28px;line-height:28px;}
</style>

<script type="text/javascript">
		document.onmousedown = function(e) {};
		document.onmousemove = function(e) {};
		document.onmouseup = function(e) {};
		document.ondblclick = function(e) {};
		var xform;
		$(function() {
            createUserPickerCopy({
        		modalId: 'userPicker',
        		showExpression: true,
        		multiple: true,
        		searchUrl: '${tenantPrefix}/rs/user/searchVNoMe',
        		treeNoPostUrl: '${tenantPrefix}/party/asyncTree.do?partyStructTypeId=1&notViewPost=true',
        		treeUrl: '${tenantPrefix}/party/asyncTree.do?partyStructTypeId=1&notViewPost=true',
        		childUrl: '${tenantPrefix}/rs/party/searchUser',
        		childPostUrl: '${tenantPrefix}/rs/party/searchPost'
        	});
		})
		
		ROOT_URL = '${tenantPrefix}';
		taskOperation = new TaskOperation();
		var conf={
				applyCodeId:"applyCode",				//受理单号input的ID
			    submitBtnId:"confirmStartProcess",		//提交按钮ID	
				checkApplyCodeUrl:"${tenantPrefix}/rs/business/applyCodeIfExist",	//验证受理单号url
    			checkUrl:"${tenantPrefix}/rs/customer/opteraion-getposition",		//获取岗位url
    			actionUrl:"${tenantPrefix}/processGroupBusiness/process-operationGroupBusiness-startProcessInstance.do",//提交URL
    			businessDetailId:"businessDetailId",			//存储业务明细input的ID
    			formId:"xform",									//form的ID
   				selectAreaId:"area",
   	   			selectCompanyId:"branchOffice",
   	 			iptAreaId:"areaId",
   	 			iptAreaName:"areaName",
   	   	   		iptCompanyId:"companyId",
   	   	   		iptCompanyName:"companyName",
   		}
		function startProcessInstance() {
			$.extend($.validator.defaults,{ignore:""});
			if($("#theme").val().replace(/(^\s*)|(\s*$)/g, "") == ""){
				alert("主题不能为空");
				return
			}
			if($("#busType").val() == "" || $("#busType").val() == "请选择"){
				alert("业务类型不能为空");
				return
			}
			if($("#busDetail").val() == "" || $("#busDetail").val() == "请选择"){
				alert("业务明细不能为空");
				return
			}
			if($("#ipt_ismoney").val()=="1"){
				if($("#ipt_money").val()==""){
					alert("请输入金额");
					return
				}
			}
			if($("#applyContent").val().replace(/(^\s*)|(\s*$)/g, "") == ""){
				alert("请输入内容");
				return
			}
			if($("#theme").val().length > "100"){
				var msg = "主题已超出100字，继续提交系统会自动截取100字以内内容，返回修改请取消";  
	            if (!confirm(msg)){  
	                return false;  
	            }  
			}
			if($("#applyContent").val().length > '5000'){
				var msg = "申请内容已超出5000字，继续提交系统会自动截取5000字以内内容，返回修改请取消";  
	            if (!confirm(msg)){  
	                return false;  
	            }  
			}
			fnFormSubmit(conf);
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
		$(function(){
			//业务类型
	   	  	getBusinessDetail();
		})
    	function getBusinessDetail(){
    		//根据用户选择的业务类型，到数据库中取业务细分
    		var  myselect=document.getElementById("busType");
    		var index=myselect.selectedIndex ;  
    		var bt=myselect.options[index].value;
    		var t=myselect.options[index].text;
    		var url = document.getElementById('r').value;
    		var userId = $("#userId").val();
    		$("#businessType").val(t);
    		var categoryId=$("#categoryId").val();
    		$.getJSON('${tenantPrefix}/rs/business/post_details',{bt:bt,userId:userId,url:url,categoryId:categoryId},function(data){
  	    		var option = "<option value=''>请选择</option>";
  	        	for(var prop in data[0]){
  	        		option += "<option value='"+prop+"'>"+ data[0][prop]+"</option>";
  	 			}
  	  			$("#busDetail").html(option);//将循环拼接的字符串插入第二个下拉列表
  	    	});
    	}
	    	
    	//根据用户选择的业务类型明细，到数据库中取业务级别
    	function setLevel() {
    		var  myselect=document.getElementById("busDetail");
    		var index=myselect.selectedIndex ;  
    		var bd=myselect.options[index].value;
    		var t=myselect.options[index].text;
    		//alert(t);
    		$("#businessDetailId").val(bd);
    		$("#businessDetail").val(t);
    		$.getJSON('${tenantPrefix}/rs/business/level', { bd:bd},function(data) {
        		var option = "" ;  
            	for (var i = 0; i < data.length; i++) {
                    option += "<option>"+ data[i].level+"</option>";
                }
    			$("#busLevel").html(option);//将循环拼接的字符串插入第二个下拉列表  
      			
      			
      			//流程级别获取后，得到分支流程条件
      			var detailId=$("#busDetail").val();
      			if(detailId!=""){
          			$.getJSON('${tenantPrefix}/rs/operation/getProcessContidionIsMoney', {detailId:detailId},function(data) {
	       				if(data!=undefined&&data!=null){
	       					if(data.level!=undefined&&data.level!=""){
	       						
	       						old_Level=data.level;
	       						$("#busLevel").html("<option>"+ data.level+"</option>");
	       						$("#businessLevel").val(data.level);
	       					}
	       					else{
	       						var myselect=document.getElementById("busLevel");
	       		        		var index=myselect.selectedIndex;  
	       		        		var bd=myselect.options[index].value;
	       		        		var t=myselect.options[index].text;
	       		      			$("#businessLevel").val(t);
	       		      			old_Level=t;
	       					}
	       					
	       					var cur_level=$("#businessLevel").val();
	       					if(data.ismoney=="1"){
	       						$("#ipt_ismoney").val("1");
	       						$("#trmoney").show();
	       						if(cur_level!="S"){
	       							getInputLevel();
	       						}
	       					}
	       					else{
	       						$("#ipt_ismoney").val("0");
	       						$("#trmoney").hide();
	       					}
	       					
	       					if(data.loadStep=="1"){
	       						$("#table_nextStep").show();
	       						getProcessStep(detailId);
	       					}
	       					else{
	       						//$('#nextStep').html('多分支流程不加载流程步骤');
	       						//$("#table_nextStep").hide();
	       						getBranchProcessStep(detailId);
	       					}
	       					if(data.bpmProcessTitle!=""){
	       						$("#processTitle").html(data.bpmProcessTitle);
	       					}
	       				}
	       				else
	       					$("#trmoney").hide();
	       			})
      			}
      		 });
    		checkPostion(conf);
    		//实现不同业务细分挂不同流程：根据用户选择的业务类型和业务细分去oa_ba_business_detail取流程的ID	        	
    		
	    }
    	
    	function getProcessStep(bd){
    		if(bd == ""){
    			$('#nextStep').html('');
    		}else{
    			$.ajax({
    				url:"${tenantPrefix}/dict/getProcessPostInfoByBusinessDetailId.do",
    				data:{businessDetailID:bd},
    				dataType:"json",
    				type:"post",
    				success:function(data){
    					$("#bpmProcessId").val(data.bpmProcessId);
    					if($("#nextStep").html() != ''){
  		  					$('#nextStep').html('');
  		  				}
  	    				$('#nextStep').append(data.whole);
    				},
    				error:function(){
    					alert("获取流程审核人岗位信息出错！");
    				}
    			});
    		}
    	}
    	/* 多分支流程步骤 */
    	function getBranchProcessStep(bd){
    		if(bd == ""){
    			$('#nextStep').html('');
    		}else{
    			var userId = $("#userId").val();
    			var isMoney = $("#ipt_ismoney").val();
    			var money;
    			if(isMoney == "1"){//有金额
    				money = $("#ipt_money").val();
    				if(money == ""){
    					return false;
    				}
    			}
    			$.ajax({
    				url:"${tenantPrefix}/dict/getBranchProcessStep.do",
    				data:{businessDetailID:bd,userId:userId,isMoney:isMoney,money:money},
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
    	}
    	
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
    	
    	var old_Level="";
    	function getInputLevel(){
    		if($("#ipt_ismoney").val()=="1"){
    			var money=$("#ipt_money").val();
    			if(money.length>12){
    				money=money.substr(0,12);
    				$("#ipt_money").val(money);
    			}
        		if(parseInt(money)>=3000){
        			$("#busLevel").html("<option>S</option>");
        			$("#businessLevel").val("S");
        		}
        		else{
        			$("#busLevel").html("<option>"+ old_Level+"</option>");
        			$("#businessLevel").val(old_Level);
        		}
    		}
    	}
    </script>
</head>

<body>
	<%@include file="/header/bpm-workspace3.jsp"%>
	<form id="xform" method="post" action='${tenantPrefix}/processGroupBusiness/process-operationGroupBusiness-startProcessInstance.do' class="xf-form" enctype="multipart/form-data">
		<div class="container">
			<!-- start of main -->
			<section id="m-main" class="col-md-12" style="padding-top: 65px;">

		<table id="table_nextStep" width="100%" cellspacing="0" cellpadding="0" border="0" align="center" class="xf-table">
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
				<input id="processDefinitionId" type="hidden" name="processDefinitionId" value="${processDefinitionId}">
				<input id="bpmProcessId" type="hidden" name="bpmProcessId" value="">
				<input id="categoryId" type="hidden" name="categoryId" value="${categoryId}"/>
				<input id="autoCompleteFirstTask" type="hidden" name="autoCompleteFirstTask" value="false"> 
				<input id="businessKey" type="hidden" name="businessKey" value="${businessKey}">
				<input id="userId" type="hidden" name="userId" value="${userId}">
				<input id="activityId" type="hidden" name="activityId" value="">
				<input id="url" type="hidden" name="url" value="/processGroupBusiness/form-detail.do">
				<input id="r" type="hidden"  name="r" value="${url}">
				<input id="businessDetailId" type=hidden name="businessDetailId">
				<div id="xf-form-table">
					<div id="xf-1" class="xf-section">
						<h1 style="text-align: center;" id="processTitle">业务申请单</h1>
					</div>

					<div id="xf-2" class="xf-section">
						<table class="xf-table" width="100%" cellspacing="0"
							cellpadding="0" border="0" align="center">
							<tbody>
								<tr id="xf-2-0">
									<td id="xf-2-0-0"
										class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left"
										colspan="4" width="100%">
										<div class="xf-handler">
											<label style="display: block; text-align: right; margin-bottom: 0px;">提交次数：0
											<input style="border: 0px; width: 10px" readonly type="hidden" id="submitTimes" name="submitTimes" value="0">
											&nbsp;&nbsp;申请单号:${code}<input type="hidden" id="applyCode" name="applyCode" value="${code}" style="border: 0px" readonly />
											</label>
										</div>
									</td>
								</tr>
								<tr id="xf-2-1">
									<td id="xf-2-1-0"
										class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left"
										width="50%">
										<div class="xf-handler">
											<label style="display: block; text-align: center; margin-bottom: 0px;"><font color='red'>*</font>&nbsp;主题：</label>
										</div>
									</td>
									<td id="xf-2-1-1" class="xf-cell xf-cell-right xf-cell-bottom"
										colspan="3" width="50%">
										<div class="xf-handler">
											<input id="theme" name="theme" type="text" style="width: 100%;height:35px;" maxlength="100" placeholder="请输入主题">
										</div>
									</td>
								</tr>
								<tr id="xf-2-2">
									<td id="xf-2-2-0"
										class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left"
										width="25%">
										<div class="xf-handler">
											<label style="display: block; text-align: center; margin-bottom: 0px;">抄送：</label>
										</div>
									</td>
									<td id="xf-2-2-1"
										class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top"
										colspan="3" width="75%">
										<!-- <div class="input-group userPicker">
											<input id="btnPickerMany" type="hidden" name="copyUserValue" class="input-medium" value="">
											<input type="text" id="userName" name="cc" style="width: 800px; background-color: white" value="" class="form-control" readOnly placeholder="点击后方图标即可选人">
											<div id="ccDiv" class="input-group-addon">
												<i class="glyphicon glyphicon-user"></i>
											</div>
										</div> -->
										<div class="input-group userPicker" style="width:100%;">
										  <input id="btnPickerMany" type="hidden" name="copyUserValue" class="input-medium" value="">
										  <input id="userName"  type="text" name="cc" placeholder="点击后方图标即可选人" style="width: 800px;"
		                                     value="" class="form-control" readonly>
										  <div id="ccDiv" class="input-group-addon"><i class="glyphicon glyphicon-user"></i></div>
									    </div>
									</td>
								</tr>
								<tr id="xf-2-3">
									<td id="xf-2-3-0"
										class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left"
										width="25%">
										<div class="xf-handler">
											<label style="display: block; text-align: center; margin-bottom: 0px;"><font color='red'>*</font>&nbsp;申请业务类型：</label>
										</div>
									</td>
									<td id="xf-2-3-1" class="xf-cell xf-cell-right xf-cell-bottom"
										width="25%">
										<div class="xf-handler">
											<select id="busType" class="form-control" style="border:none;" onchange="getBusinessDetail()">
												<option value="-1">请选择</option>
												<c:forEach items="${businessTypeList}" var="item">
		  											<option value="${item.id}" >${item.name}</option>
		  										</c:forEach>
												<input type="hidden" id="businessType" name="businessType">
											</select>
										</div>
									</td>
									<td id="xf-2-3-2"
										class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left"
										width="25%">
										<div class="xf-handler">
											<label style="display: block; text-align: center; margin-bottom: 0px;"><font color='red'>*</font>&nbsp;业务细分：</label>
										</div>
									</td>
									<td id="xf-2-3-3" class="xf-cell xf-cell-right xf-cell-bottom"
										width="25%">
										<div class="xf-handler">
											<select id="busDetail" class="form-control" style="border:none;" name ="busDetail" onchange="setLevel()">
												<option value="请选择">请选择</option>
												<input type="hidden" id="businessDetail" name="businessDetail">
											</select>
										</div>
									</td>
								</tr>
								<tr id="xf-2-4">
									<td id="xf-2-4-0"
										class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left"
										width="25%">
										<div class="xf-handler">
											<label style="display: block; text-align: center; margin-bottom: 0px;"><font color='red'>*</font>&nbsp;业务级别：</label>
										</div>
									</td>
									<td id="xf-2-4-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="25%">
										<select id="busLevel" class="form-control" style="border:none;">
											<option value="请选择">请选择</option>
											<input type="hidden" id="businessLevel" name="businessLevel">
										</select></td>
									<td id="xf-2-4-2" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="25%">
										<label style="display: block; text-align: center; margin-bottom: 0px;">发起人：</label>
									</td>
									<td id="xf-2-4-3" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="25%">
										<div class="xf-handler" style="padding:6px 12px;">
											<tags:user userId="<%=userId%>"/>
											<input type="hidden" id="initiator" name="initiator" style="width: 100%" readonly value='<tags:user userId="<%=userId%>"/>'/>
										</div>
									</td>
								</tr>
								<tr id="trmoney" style="display:none;">
									<td class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" style="text-align:center;"><font color='red'>*</font>&nbsp;<label>金额:</label></td>
									<td colspan="3" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
										<input id="ipt_money" type="number" name="money" value="" onkeyup="getInputLevel()" onblur="getBranchProcessStepMoney(this.value)" placeholder="请输入金额" style="width:100%;height:35px;"/>
										<input type="hidden" id="ipt_ismoney" name="ipt_ismoney" value="0"/>
									</td>
								</tr>
								<tr id="xf-2-6">
									<td id="xf-2-6-0"
										class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left"
										width="25%">
										<div class="xf-handler">
											<label style="display: block; text-align: center; margin-bottom: 0px;">
											<font color='red'>*</font>&nbsp;申请内容</label>
										</div>
									</td>
									<td id="xf-2-6-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" colspan="3" width="75%">
										<div class="xf-handler">
											<textarea id="applyContent" placeholder="请输入申请内容" rows="10" name="applyContent" style="width: 100%" maxlength="5000" onkeyup="MaxWords(this)" onblur="MaxWords(this)"></textarea>
											<label id="recordNum" style="display: none;text-align:right;" class="control-label col-md-12"></label>
										</div>
									</td>
								</tr>
								<tr id="xf-2-7">
									<td id="xf-2-7-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" colspan="4" width="100%">
										<div class="xf-handler">
											<label style="display: block; text-align: center; margin-bottom: 0px;">上传附件</label>
										</div>
									</td>
								</tr>
								<tr id="xf-2-8">
									<td id="xf-2-8-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" colspan="4">
										<div class="col-md-8">
											<%@include file="/common/_uploadFile.jsp"%>
											<span style="color: gray;"> 请添加小于200M的附件 </span>
										</div>
									</td>
								</tr>
							</tbody>
						</table>
					</div>
				</div>
				<br><br/><br/>
			</div>
		<div class="navbar navbar-default navbar-fixed-bottom">
			<div class="container-fluid">
				<div class="text-center" style="padding-top: 8px;">
					<div class="text-center" style="padding-top: 8px;">
						<!-- <button id="saveDraft" class="btn btn-default" type="button" onclick="taskOperation.saveDraft()">保存草稿</button> -->
						<!-- <button id="confirmStartProcess" class="btn btn-default" type="button" onclick="taskOperation.taskConf()">配置任务</button> -->
						<button id="confirmStartProcess" class="btn btn-default" type="button" onclick="startProcessInstance()" value="提交">提交数据</button>
						<button type="button" class="btn btn-default" onclick="javascript:history.back();">返回</button>
					</div>

				</div>
			</div>
		</div>
		<%@include file="/common/selectPosition.jsp" %>
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
