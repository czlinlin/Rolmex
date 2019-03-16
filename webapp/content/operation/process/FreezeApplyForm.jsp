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
			
			//获取分公司
			$.getJSON('${tenantPrefix}/rs/party/branchOfficeName',{userId : $("#userId").val()},
					function(data){
						document.getElementById("branchOffice").value = data[0].name;
					}
			)
			//获取区域
			$.getJSON('${tenantPrefix}/rs/party/AreaName',{userId : $("#userId").val()},
					function(data){
						if (data[0] != null) {
							document.getElementById("area").value = data[0].name;
							document.getElementById("areaId").value = data[0].id;
						}
						
					}
			)
			
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
		
		var conf={
				applyCodeId:"applyCode",				//受理单号input的ID
			    submitBtnId:"confirmStartProcess",		//提交按钮ID	
				checkApplyCodeUrl:"${tenantPrefix}/rs/business/applyCodeIfExist",	//验证受理单号url
    			checkUrl:"${tenantPrefix}/rs/customer/opteraion-getposition",		//获取岗位url
    			actionUrl:"${tenantPrefix}/processFreeze/process-operationFreeze-startProcessInstance.do",//提交URL
    			businessDetailId:"businessDetailId",			//存储业务明细input的ID
    			formId:"xform",									//form的ID
    			selectAreaId:"area",
   	   			selectCompanyId:"branchOffice",
   	 			iptAreaId:"areaId",
   	 			iptAreaName:"areaName",
   	   	   		iptCompanyId:"companyId",
   	   	   		iptCompanyName:"companyName",
    		}
		$(function(){
			checkPostion(conf);
		})
		function startProcessInstance() {
			if($("#ucode").val().replace(/(^\s*)|(\s*$)/g, "") == ""){
				alert("编号不能为空");
				return
			}
			if($("#system").val() == "请选择"){
				alert("请选择体系");
				return
			}
			if($("#aboveBoard").val().replace(/(^\s*)|(\s*$)/g, "") == ""){
				alert("请输入上属董事");
				return
			}
			if($("#director").val().replace(/(^\s*)|(\s*$)/g, "") == ""){
				alert("请输入董事姓名");
				return
			}
			if($("#directorContact").val().replace(/(^\s*)|(\s*$)/g, "") == ""){
				alert("请输入联系方式");
				return
			}
			if($("#idNumber").val().replace(/(^\s*)|(\s*$)/g, "") == ""){
				alert("请输入身份证号");
				return
			}else{
				var idNumber = $("#idNumber").val().replace(/(^\s*)|(\s*$)/g, "");
               	var reg = /^\d{17}(\d|x)$/i;//验证身份证号
                var re=idNumber.match(reg);
                if(re==null){ //只能输入正确格式的身份证号
                    alert('身份证号码格式错误！');
                    idNumber.focus();
                    return false;
                }
			}
			var val=$('input:radio[name="applyMatter"]:checked').val();
            if(val==null){
                alert("申请受理事项必选一项");
                return;
            }
            var applyContent = $("#applyContent").val();
			var replaceapplyContent = applyContent.replace(/(^\s*)|(\s*$)/g, "");
            if(replaceapplyContent == ""){
				alert("请输入内容");
				return
			}
            if($("#name").val() == ""||$("#contact").val() == ""){
				alert("编号不存在，请检查后输入");
				return
			}
            if($("#applyContent").val().length > "5000"){
				var msg = "申请内容已超出5000字，继续提交系统会自动截取5000字以内内容，返回修改请取消";  
	            if (!confirm(msg)){  
	                return false;  
	            }  
			}
            
            
			fnFormSubmit(conf);
		}
		//获得选择的体系
	   	   function getSystemName() {
			 var  myselect=document.getElementById("system");
			 var index=myselect.selectedIndex ;  
			 var text=myselect.options[index].text;
			 $("#systemName").val(text);
		  }
		
		 //调用接口，根据经销商编号，获取直销oa上存的对应信息：姓名 电话 等
		 $(function() {
					$("#ucode").keydown(function(event) {    
						if (event.keyCode == 13) {  
							var id = $("#ucode").val();
		                	if (id !="") {
			                    $.getJSON('${tenantPrefix}/rs/varUser/userInfo', {
			                    	customerInfoId: id
			                    }, function(data) {
			          				//$("#realName").html(''+''+data.name);
				          			$("#name").val(data.name);
				          			//$("#welfare").html(data.rank);
				          			$("#contact").val(data.mobile);
				          			$("#salesLevel").val(data.level);
				          			$("#welfareLevel").val(data.rank);
				          			$("#activationState").val(data.pay);
				          			$("#frozenState").val(data.freeze);
				          			/* $("#aboveBoard").val(varDirectName);
				          			$("#director").val(varDirectName);
				          			$("#directorContact").val(varDirectMobile);
				          			$("#idNumber").val(varCardNO); */
			          				
				          		});
		                    } else {
		                    	alert("必须输入客户编号!");
		                    }
		                }    
		            });
		}) 
		 //鼠标点击其他地方触发获取信息
		function fnshopData() {
				var id = $("#ucode").val();
				if (id !="") {
			        $.getJSON('${tenantPrefix}/rs/varUser/userInfo', {
			        	customerInfoId: id
			        }, function(data) {
			        	//alert(JSON.stringify(data)); 
			        	$("#name").val(data.name);
	          			//$("#welfare").html(data.rank);
	          			$("#contact").val(data.mobile);
	          			$("#salesLevel").val(data.level);
	          			$("#welfareLevel").val(data.rank);
	          			$("#activationState").val(data.pay);
	          			$("#frozenState").val(data.freeze);
	          			
	          			/* $("#director").val(varDirectName);
	          			$("#directorContact").val(varDirectMobile);
	          			$("#idNumber").val(varCardNO); */
			  		});
			    } 
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
    </script>
  </head>

  <body>
    <%@include file="/header/bpm-workspace3.jsp"%>
<form id="xform" method="post" action="${tenantPrefix}/testReturn/process-operationTestReturn-startProcessInstance.do" class="xf-form" enctype="multipart/form-data">
    <div class="container">

	  <!-- start of main -->
      <section id="m-main" class="col-md-12" style="padding-top:65px;">

		<%-- <table width="100%" cellspacing="0" cellpadding="0" border="0" align="center" class="xf-table">
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
		<script>
		  $.getJSON('${tenantPrefix}/rs/bpm/whole', {
			  processDefinitionId: '<%= request.getParameter("processDefinitionId")%>',
			  activityId: '<%= request.getParameter("activityId")%>',
			  isWhole:false
		  }, function(data) {
			  $('#nextStep').append('&nbsp;');
			  for (var i = 0; i < data.length; i++) {
				  $('#nextStep').append(data[i].name);
				  //$('#activityId').val(data[i].id);
			  }
		  });
		</script> --%>

      
		<input id="processDefinitionId" type="hidden" name="processDefinitionId" value="${processDefinitionId}">
		<input id="userId" type="hidden" name="userId" value="${userId}">
		<input id="bpmProcessId" type="hidden" name="bpmProcessId" value="${bpmProcessId}">
		<input id="businessDetailId" type="hidden" name="businessDetailId" value="${businessDetailId}">
		<input id="autoCompleteFirstTask" type="hidden" name="autoCompleteFirstTask" value="false">
		<input id="businessKey" type="hidden" name="businessKey" value="${businessKey}">
		<%-- <input id="assignee" type="hidden" name="assignee" value="<%= request.getParameter("assignee")%>"> --%>
		<input id="activityId" type="hidden" name="activityId" value="">
		<input type="hidden" id="applyCode" name="applyCode" style="border:0px" value="${code}">
		<input id="url" type="hidden" name="url" value="/processFreeze/form-detail.do">
		<input id="areaId" type="hidden" name="areaId">
		<input id="companyId" type="hidden" name="companyId">
		<div id="xf-form-table">
			<div id="xf-1" class="xf-section">
				<h1 style="text-align:center;">冻结/解冻申请单</h1>
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
									<input id="ucode" type="text" name="ucode" style="width:100%" onblur='fnshopData()' maxlength="8" onkeyup="var reg = /^[0-9]\d*$/; if(!reg.test(this.value)) this.value = ''; ">
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
									<select id="system" name="system" onchange="getSystemName()">
										<option value="请选择">请选择</option>
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
									<!-- <select id="region" onchange="getChildId()">
										<option value="">请选择</option>
										<input type="hidden" id="regionValue" name="region">
									</select> -->
									<input type="text" id="areaName" name="areaName" style="width:100%" readonly>
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
									<!-- <select id="brachOffice">
										<option value="">请选择</option>
										<input type="hidden" id="brachOfficeValue" name="brachOffice">
									</select> -->
									<input type="text" id="companyName" name="companyName" style="width:100%" readonly>
								</div>
							</td>
							<td id="xf-2-4-2" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
								<label style="display:block;text-align:center;margin-bottom:0px;">身份证号：</label>
							</td>
							<td id="xf-2-4-3" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" colspan="3">
								<input type="text" id="idNumber" name="idNumber" style="width:100%">
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
									<input type="radio" name="applyMatter" value="冻结">冻结&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
									<input type="radio" name="applyMatter" value="开除">开除&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
									<input type="radio" name="applyMatter" value="解冻">解冻&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
									<input type="radio" name="applyMatter" value="限制">限制&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
									<input type="radio" name="applyMatter" value="取消限制">取消限制&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
									<input type="radio" name="applyMatter" value="其他">其他
								</div>
							</td>
						</tr>
						<tr id="xf-2-6">
							<td id="xf-2-6-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">申请内容：</label>
								</div>
							</td>
							<td id="xf-2-6-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" colspan="5" width="100%">
								<div class="xf-handler">
                            		<textarea id="applyContent" name="applyContent" rows="10" maxlength="5000" style="width:100%"  onkeyup="MaxWords(this)" onblur="MaxWords(this)"></textarea>
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
			<!-- <button id="confirmStartProcess" class="btn btn-default" type="button" onclick="taskOperation.taskConf()">配置任务</button> -->
			<button id="confirmStartProcess" class="btn btn-default" type="button" onclick="startProcessInstance()" value="提交">提交数据</button>
			<button type="button" class="btn btn-default" onclick="javascript:history.back();">返回</button>
			<!-- <button id="confirmStartProcess" class="btn btn-default" type="button" onclick="getShopInfo()">获取专卖店信息</button> -->
		</div>
	
	  </div>
    </div>
  </div>
  <%@include file="/common/selectPosition.jsp" %>
</form>


</body>

</html>
