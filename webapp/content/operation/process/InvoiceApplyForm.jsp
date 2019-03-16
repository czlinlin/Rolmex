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
	
	<style type="text/css">
		.xf-handler {
			cursor: auto;
		}
		input{border : 1px solid #F2F2F2;height:25px}
		textarea{border : 1px solid #F2F2F2;}
	</style>

	<script type="text/javascript">
		document.onmousedown = function(e) {};
		document.onmousemove = function(e) {};
		document.onmouseup = function(e) {};
		document.ondblclick = function(e) {};

		var xform;
		function maxWords(){
			var textPublic = $("#invoiceDetailPublic").val();
			var textPerson = $("#invoiceDetailPerson").val();
			if(textPublic.length > 5000){
				document.getElementById("invoiceDetailPublic").value = textPublic.substr(0,5000);
			}
			if(textPerson.length > 5000){
				document.getElementById("invoiceDetailPerson").value = textPerson.substr(0,5000);
			}
		}
		$(function() {
			//document.getElementById("commonId").checked=true;
			document.getElementById("categoryPerson").checked=true;
			personCheck();
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
		
		function time(){
			var now = new Date();
			var year = now.getFullYear();
			var month = now.getMonth();
			var day = now.getDate();
			if(month<9&&day<10){
				document.getElementById("applyDate").value = year+"-"+"0"+(month+1)+"-"+"0"+day;
			}else if(month<9){
				document.getElementById("applyDate").value = year+"-"+"0"+(month+1)+"-"+day;
			}else if(day<10){
				document.getElementById("applyDate").value = year+"-"+(month+1)+"-"+"0"+day;
			} else{
				document.getElementById("applyDate").value = year+"-"+(month+1)+"-"+day;
			}
			
			
		}
		//选择对公执行该方法
		function disableElement(){
			document.getElementById("appreciationid1").checked=true;
			document.getElementById("appreciationid1").disabled=false;
			document.getElementById("appreciationid2").disabled=false;
			document.getElementById("commonId").disabled=true;
			document.getElementById("invoiceTitlePerson").value="";//清空个人的输入框数据
			document.getElementById("invoiceTitlePerson").disabled=true;
			document.getElementById("invoiceDetailPerson").value="";
			document.getElementById("invoiceDetailPerson").disabled=true;
			document.getElementById("invoiceMoneyPerson").value="";
			document.getElementById("invoiceMoneyPerson").disabled=true;
			document.getElementById("idNumber").value="";
			document.getElementById("idNumber").disabled=true;
			
			document.getElementById("invoiceTitlePublic").disabled=false;
			document.getElementById("invoiceDetailPublic").disabled=false;
			document.getElementById("invoiceMoneyPublic").disabled=false;
			document.getElementById("enterpriseName").disabled=false;
			document.getElementById("taxNumber").disabled=false;
			document.getElementById("openingBank").disabled=false;
			document.getElementById("accountNumber").disabled=false;
			document.getElementById("enterpriseAddress").disabled=false;
		}
		//选择个人执行该方法
		function personCheck(){
			document.getElementById("commonId").checked=true;
			
			document.getElementById("commonId").disabled=false;
			document.getElementById("appreciationid1").disabled=true;
			document.getElementById("appreciationid2").disabled=true;
			
			document.getElementById("invoiceTitlePerson").disabled=false;
			document.getElementById("invoiceDetailPerson").disabled=false;
			document.getElementById("invoiceMoneyPerson").disabled=false;
			document.getElementById("idNumber").disabled=false;
			
			document.getElementById("invoiceTitlePublic").value="";//清空对公输入框的数据
			document.getElementById("invoiceTitlePublic").disabled=true;
			document.getElementById("invoiceDetailPublic").value="";
			document.getElementById("invoiceDetailPublic").disabled=true;
			document.getElementById("invoiceMoneyPublic").value="";
			document.getElementById("invoiceMoneyPublic").disabled=true;
			document.getElementById("enterpriseName").value="";
			document.getElementById("enterpriseName").disabled=true;
			document.getElementById("taxNumber").value="";
			document.getElementById("taxNumber").disabled=true;
			document.getElementById("openingBank").value="";
			document.getElementById("openingBank").disabled=true;
			document.getElementById("accountNumber").value="";
			document.getElementById("accountNumber").disabled=true;
			document.getElementById("enterpriseAddress").value="";
			document.getElementById("enterpriseAddress").disabled=true;
			
		}
		var conf={
				applyCodeId:"applyCode",				//受理单号input的ID
			    submitBtnId:"confirmStartProcess",		//提交按钮ID	
				checkApplyCodeUrl:"${tenantPrefix}/rs/business/applyCodeIfExist",	//验证受理单号url
       			checkUrl:"${tenantPrefix}/rs/customer/opteraion-getposition",		//获取岗位url
       			actionUrl:"${tenantPrefix}/Invoice/process-operationInvoice-startProcessInstance.do",//提交URL
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
			if($("#ucode").val().replace(/(^\s*)|(\s*$)/g, "") == ''){
				alert("专卖店编号/手机号不能为空");
				 return;
			}
			if($("#orderNumber").val().replace(/(^\s*)|(\s*$)/g, "") == ''){
				alert("订单单据号不能为空");
				 return;
			}
			if($("#system").val() == '请选择'){
				alert("请选择体系");
				 return;
			}
			var category = $('input:radio[name="category"]:checked').val();
			if(category == "个人"){
				if($("#invoiceTitlePerson").val().replace(/(^\s*)|(\s*$)/g, "") == ''){
					alert("请输入个人的发票抬头");
					return;
				}
				if($("#invoiceDetailPerson").val().replace(/(^\s*)|(\s*$)/g, "") == ''){
					alert("请输入个人的发票明细");
					return;
				}
				if($("#invoiceMoneyPerson").val().replace(/(^\s*)|(\s*$)/g, "") == ''){
					alert("请输入个人的发票开具总金额");
					return;
				}
				if($("#idNumber").val().replace(/(^\s*)|(\s*$)/g, "") == ''){
					alert("请输入身份证号");
					return;
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
			}else if(category == "对公"){
				if($("#invoiceTitlePublic").val().replace(/(^\s*)|(\s*$)/g, "") == ''){
					alert("请输入对公的发票抬头");return
				}
				if($("#invoiceDetailPublic").val().replace(/(^\s*)|(\s*$)/g, "") == ''){
					alert("请输入对公的发票明细");return
				}
				if($("#invoiceMoneyPublic").val().replace(/(^\s*)|(\s*$)/g, "") == ''){
					alert("请输入对公的发票开具总金额");return
				}
				if($("#enterpriseName").val().replace(/(^\s*)|(\s*$)/g, "") == ''){
					alert("请输入企业名称");return
				}
				if($("#taxNumber").val().replace(/(^\s*)|(\s*$)/g, "") == ''){
					alert("请输入税务登记号");return
				}
				if($("#openingBank").val().replace(/(^\s*)|(\s*$)/g, "") == ''){
					alert("请输入开户行");return
				}
				if($("#accountNumber").val().replace(/(^\s*)|(\s*$)/g, "") == ''){
					alert("请输入开户行电话");return
				}
				if($("#enterpriseAddress").val().replace(/(^\s*)|(\s*$)/g, "") == ''){
					alert("请输入企业地址及电话");
					return;
				}
				
			}
			if($("#invoiceMailAddress").val().replace(/(^\s*)|(\s*$)/g, "") == ''){
				alert("请输入发票邮寄地址");
				return;
			} 
			if($("#adreesseeName").val().replace(/(^\s*)|(\s*$)/g, "") == ''){
				alert("请输入收件人姓名");
				return;
			} 
			if($("#addresseeTel").val().replace(/(^\s*)|(\s*$)/g, "") == ''){
				alert("请输入收件人电话");
				return
			}else{
				var tel = $("#addresseeTel").val().replace(/(^\s*)|(\s*$)/g, "");
               	var reg = /^(\d{3,4})-(\d{7,8})/;//验证电话号码
                var regph=/[1][3-9][0-9]{9,9}/;//验证手机号码
                var re=tel.match(reg);
              	var reph=tel.match(regph);
                if(re==null&&reph==null){ //只能输入正确格式的手机或电话号码
                    alert('收件人电话不符，请重新输入！\n注：手机（以数字1开头的11位数字） 固话（区号 7位或8位的数字号码）！');
                    tel.focus();
                    return false;
                }
			}
			if($("#addresseeSpareTel").val().replace(/(^\s*)|(\s*$)/g, "") == ''){
				alert("请输入收件人备用电话");
				return
			}else{
				var tel = $("#addresseeSpareTel").val().replace(/(^\s*)|(\s*$)/g, "");
               	var reg = /^(\d{3,4})-(\d{7,8})/;//验证电话号码
                var regph=/[1][3-9][0-9]{9,9}/;//验证手机号码
                var re=tel.match(reg);
              	var reph=tel.match(regph);
                if(re==null&&reph==null){ //只能输入正确格式的手机或电话号码
                    alert('收件人备用电话不符，请重新输入！\n注：手机（以数字1开头的11位数字） 固话（区号 7位或8位的数字号码）！');
                    tel.focus();
                    return false;
                }
			}
			
			if($("#ucode").val().length == 11){
				if($("#shopName").val() == ''){
					alert("专卖店姓名不能为空");
					return
				}
			}else{
				if($("#shopName").val() == ''||$("#shopTel").val() == ''){
					alert("专卖店编号不存在，请检查后输入");
					return
				}
				
			}
			
			fnFormSubmit(conf);
		}
		
		 //调用接口，根据经销商编号，获取直销oa上存的对应信息：姓名 电话 等
		 $(function() {
			     //分公司名
				$.getJSON('${tenantPrefix}/rs/party/branchOfficeName',{userId :  $("#userId").val()},
						function(data){
							//alert(data[0].name);
							//alert(JSON.stringify(data));
							$("#branchOffice").val(data[0].name);
						}
				);
		    	//区域名
				$.getJSON('${tenantPrefix}/rs/party/AreaName',{userId : $("#userId").val()},
						function(data){
							//alert("区域："+JSON.stringify(data));
							if (data[0] != null) {
								$("#area").val(data[0].name);
								$("#areaId").val(data[0].id);
							}
						}
				);
					$("#ucode").keydown(function(event) {    
						if (event.keyCode == 13) {  
							var id = $("#ucode").val();
							//alert(id);
		                	if (id !="") {
			                    $.getJSON('${tenantPrefix}/rs/varUser/userInfo', {
			                    	customerInfoId: id
			                    }, function(data) {
			                    	//alert(JSON.stringify(data)); 
			          				//$("#realName").html(''+''+data.name);
				          			$("#shopName").val(data.name);
				          			//$("#welfare").html(data.rank);
				          			$("#shopTel").val(data.mobile);
				          			
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
		 //鼠标点击其他地方触发获取信息
		function fnshopData() {
				var id = $("#ucode").val();
				//alert(id);
				if (id !="") {
			        //ckx  判断商城还是oa
			        if(id.length == 11){
			        	changeMallDiv();
			        	$("#shopName").val('');
			  			$("#shopTel").val('');
			  			$("#shopTel").attr("placeholder","不可输入");
						$("#shopName").removeAttr("readonly");
						$("#h1Id").html("罗麦随行发票申请单");
			        }else{
			        	changeOaDiv();
			        	$("#h1Id").html("发票申请单");
			        	$("#shopName").attr("readonly","readonly");
			        	$.getJSON('${tenantPrefix}/rs/varUser/userInfo', {
				        	customerInfoId: id
				        }, function(data) {
				        	console.log(JSON.stringify(data));
							$("#shopName").val(''+''+data.name);
				  			$("#shopTel").val(data.mobile);
				  		});
			        }
			    } 
		}

			function getSystemName() {
				var  myselect=document.getElementById("system");
				var index=myselect.selectedIndex ;  
				var text=myselect.options[index].text;
				$("#systemName").val(text);
			 }
			function changeMallDiv(){
				var companyName = $("#companyName").val();
				var areaName = $("#areaName").val();
				$("#form-tfoot").html('<tr id="xf-2-3">'
						+'<td id="xf-2-3-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left" width="25%">'
						+'<div class="xf-handler">'
						+'	<label style="display:block;text-align:center;margin-bottom:0px;">所属区域</label>'
						+'</div>'
						+'</td>'
						+'<td id="xf-2-3-1" class="xf-cell xf-cell-right xf-cell-bottom" width="25%">'
						+'<div class="xf-handler">'
						+'	<input value="'+areaName+'" type="text" id="areaName" name="areaName" style="border:0px;text-align:center" readonly>'
						+'</div>'
						+'</td>'
						+'<td id="xf-2-3-2" class="xf-cell xf-cell-right xf-cell-bottom"  width="25%">'
						+'<div class="xf-handler">'
						+'	<label style="display:block;text-align:center;margin-bottom:0px;">所属分公司</label>'
						+'</div>'
						+'</td>'
						+'<td id="xf-2-3-3" class="xf-cell xf-cell-right xf-cell-bottom" width="25%">'
						+'<div class="input-group userPicker" style="width: 175px;">'
						+'	<input value="'+companyName+'" type="text" id="companyName" name="companyName" style="border:0px;text-align:center" readonly>'
						+'</div>'
						+'</td>'
						/* +'	<input type="hidden" id="system" name="system" value="">'
						+'	<input type="hidden" id="systemName" name="systemName" value="">' */
						+'</tr>');
			}
			
			function changeOaDiv(){
				var companyName = $("#companyName").val();
				var areaName = $("#areaName").val();
				$("#form-tfoot").html('<tr id="xf-2-3">'
						+'<td id="xf-2-3-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left" width="25%">'
						+'<div class="xf-handler">'
						+'	<label style="display:block;text-align:center;margin-bottom:0px;">所属区域</label>'
						+'</div>'
						+'</td>'
						+'<td id="xf-2-3-1" class="xf-cell xf-cell-right xf-cell-bottom" width="25%">'
						+'<div class="xf-handler">'
						+'	<input value="'+areaName+'" type="text" id="areaName" name="areaName" style="border:0px;text-align:center" readonly>'
						+'</div>'
						+'</td>'
						+'<td id="xf-2-3-2" class="xf-cell xf-cell-right xf-cell-bottom"  width="25%">'
						+'<div class="xf-handler">'
						+'	<label style="display:block;text-align:center;margin-bottom:0px;">所属体系</label>'
						+'</div>'
						+'</td>'
						+'<td id="xf-2-3-3" class="xf-cell xf-cell-right xf-cell-bottom" width="25%">'
						+'<div class="xf-handler">'
						+'	<select class="form-control required" id="system" name="system" onchange="getSystemName()">'
						+'		<option value="请选择">请选择</option>'
						+'		<c:forEach items="${systemlist}" var="item">'
						+'			<option value="${item.value}" >${item.name}</option>'
						+'		</c:forEach>'
						+'	</select>'
						+'	<input type="hidden" id="systemName" name="systemName">'
						+'</div>'
						+'</td>'
						+'</tr>'
						+'<tr id="xf-2-4">'
						+'<td id="xf-2-4-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="25%">'
						+'<div class="xf-handler">'
						+'	<label style="display:block;text-align:center;margin-bottom:0px;">所属分公司</label>'
						+'</div>'
						+'</td>'
						+'<td id="xf-2-4-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top" colspan="3"  width="25%">'
						+'<div class="input-group userPicker" style="width: 175px;">'
						+'	<input value="'+companyName+'" type="text" id="companyName" name="companyName" style="border:0px;text-align:center" readonly>'
						+'</div>'
						+'</td>'
						+'</tr>');
			}
    </script>
  </head>

  <body onload="time()">
    <%@include file="/header/bpm-workspace3.jsp"%>
<form id="xform" name="xform" method="post" class="xf-form" enctype="multipart/form-data">
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
				  // $('#activityId').val(data[i].id);
			  }
		  });
		  </script> --%>
      
		<input id="processDefinitionId" type="hidden" name="processDefinitionId" value="${processDefinitionId}">
		<%-- <input id="processInstanceId" type="hidden" name="processInstanceId" value="<%= request.getParameter("processInstanceId")%>"> --%>
		<%-- <input id="humanTaskId" type="hidden" name="humanTaskId" value="<%= request.getParameter("humanTaskId")%>"> --%>
		<input id="bpmProcessId" type="hidden" name="bpmProcessId" value="${bpmProcessId}">
		<input id="businessDetailId" type="hidden" name="businessDetailId" value="${businessDetailId}">
		<input id="autoCompleteFirstTask" type="hidden" name="autoCompleteFirstTask" value="false">
		<input id="businessKey" type="hidden" name="businessKey" value="${businessKey}">
		<%-- <input id="assignee" type="hidden" name="assignee" value="<%= request.getParameter("assignee")%>"> --%>
		<input id="userId" type="hidden" name="userId" value="${userId}">
		<input id="activityId" type="hidden" name="activityId" value="">
		<input id="url" type="hidden" name="url" value="/Invoice/form-detail.do">
		<input type="hidden" id="applyCode" name="applyCode" value="${code}">
		<input type="hidden" id="areaId" name="areaId">
		<input type="hidden" id="companyId" name="companyId">
		
		<div id="xf-form-table">
			<div id="xf-1" class="xf-section">
				<h1 id="h1Id" style="text-align:center;">发票申请单</h1>
			</div>
			
			<div id="xf-2" class="xf-section">
				<table class="xf-table" width="100%" cellspacing="0" cellpadding="0" border="0" align="center">
					<tbody>
						<tr id="xf-2-0">
							<td id="xf-2-0-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="25%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">专卖店编号/手机号</label>
								</div>
							</td>
							<td id="xf-2-0-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left"  width="25%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">专卖店姓名</label>
								</div>
							</td>
							<td id="xf-2-0-2" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top"  width="25%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">专卖店电话</label>
								</div>
							</td>
							<td id="xf-2-0-3" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top"  width="25%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">申请发票日期</label>
								</div>
							</td>
						</tr>
						<tr id="xf-2-1">
							<td id="xf-2-1-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left" width="25%">
								<div class="xf-handler">
									<input type="text" name="ucode" id="ucode" style="width:100%" onblur='fnshopData()' maxlength="11" onkeyup="var reg = /^[0-9]\d*$/; if(!reg.test(this.value)) this.value = ''; ">
								</div>
							</td>
							<td id="xf-2-1-1" class="xf-cell xf-cell-right xf-cell-bottom"  width="25%">
								<div class="xf-handler">
									<input type="text" name="shopName" id="shopName" style="width:100%" readonly>
								</div>
							</td>
							<td id="xf-2-1-2" class="xf-cell xf-cell-right xf-cell-bottom"  width="25%">
								<div class="xf-handler">
									<input type="text" name="shopTel" id="shopTel" style="width:100%" readonly>
								</div>
							</td>
							<td id="xf-2-1-3" class="xf-cell xf-cell-right xf-cell-bottom"  width="25%">
								<div class="xf-handler" >
									<input type="text" id="applyDate" name="invoiceDate" readonly="readonly" style="width:100%;text-align:center" >
								</div>
							</td>
						</tr>
						<tr id="xf-2-2">
							<td id="xf-2-2-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left" width="25%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">订单单据号</label>
								</div>
							</td>
							<td id="xf-2-2-1" class="xf-cell xf-cell-right xf-cell-bottom" width="25%" colspan="3">
								<div class="xf-handler">
									<textarea rows="3" id="orderNumber" name="orderNumber" style="width:100%"></textarea>
								</div>
							</td>
							
						</tr>
						<tbody id="form-tfoot">
						<tr id="xf-2-3">
							<td id="xf-2-3-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left" width="25%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">所属区域</label>
								</div>
							</td>
							<td id="xf-2-3-1" class="xf-cell xf-cell-right xf-cell-bottom" width="25%">
								<div class="xf-handler">
									<input type="text" id="areaName" name="areaName" style="border:0px;text-align:center" readonly>
								</div>
							</td>
							<td id="xf-2-3-2" class="xf-cell xf-cell-right xf-cell-bottom"  width="25%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">所属体系</label>
								</div>
							</td>
							<td id="xf-2-3-3" class="xf-cell xf-cell-right xf-cell-bottom" width="25%">
								<div class="xf-handler">
									<select class="form-control required" id="system" name="system" onchange="getSystemName()">
										<option value="请选择">请选择</option>
										<c:forEach items="${systemlist}" var="item">
		  									<option value="${item.value}" >${item.name}</option>
		  								</c:forEach>
									</select>
									<input type="hidden" id="systemName" name="systemName">
								</div>
							</td>
						</tr>
						<tr id="xf-2-4">
							<td id="xf-2-4-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="25%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">所属分公司</label>
								</div>
							</td>
							<td id="xf-2-4-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top" colspan="3"  width="25%">
								<div class="input-group userPicker" style="width: 175px;">
									<input type="text" id="companyName" name="companyName" style="border:0px;text-align:center" readonly>
							    </div>
							</td>
						</tr>
						</tbody>
						<tr id="xf-2-5">
							<td id="xf-2-5-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="25%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">发票类型</label>
								</div>
							</td>
							<td id="xf-2-5-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="25%" colspan="3">
								<div class="xf-handler">
									<input type="radio" id="commonId" name="invoiceType" value="普通发票">普通发票&nbsp;&nbsp;<input type="radio" id="appreciationid1" name="invoiceType" value="增值税普通发票">增值税普通发票
									&nbsp;&nbsp;<input type="radio" id="appreciationid2" name="invoiceType" value="增值税专用发票">增值税专用发票
								</div>
							</td>
						</tr>
						<tr id="xf-2-6">
							<td id="xf-2-6-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="25%" rowspan="4" align="center">
								<div class="xf-handler">
									<input id="categoryPerson" type="radio" name="category"  value="个人" onclick="personCheck()"><label>个人</label>
								</div>
							</td>
							<td id="xf-2-6-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="25%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">发票抬头</label>
								</div>
							</td>
							<td id="xf-2-6-2" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="50%" colspan="2">
								<div class="xf-handler">
									<input type="text" id="invoiceTitlePerson" name="invoiceTitle" style="width:100%" value="" maxlength="100">
								</div>
							</td>
						</tr>
						<tr id="xf-2-7" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
							<td id="xf-2-7-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="25%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">发票明细(产品名称、价格、数量)</label>
								</div>
							</td>
							<td id="xf-2-7-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="50%" colspan="2">
								<div class="xf-handler">
									<textarea rows="5" id="invoiceDetailPerson" name="invoiceDetail" style="width:100%" onkeyup="maxWords()"  onblur="maxWords()"></textarea>
								</div>
							</td>
						</tr>
						<tr id="xf-2-8" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
							<td id="xf-2-8-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="25%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;"> 发票开具总金额</label>
								</div>
							</td>
							<td id="xf-2-8-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="50%" colspan="2">
								<div class="xf-handler">
									<input type="text" id="invoiceMoneyPerson" name="invoiceMoney" style="width:100%"  onblur="var reg = /^[0-9]+([.]{1}[0-9]{1,2})?$/; if(!reg.test(this.value)) this.value = ''; ">
								</div>
							</td>
						</tr>
						<tr id="xf-2-9" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
							<td id="xf-2-9-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="25%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;"> 身份证号码</label>
								</div>
							</td>
							<td id="xf-2-9-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="75%" colspan="3">
								<div class="xf-handler">
									<input id="idNumber" name="idNumber" type="text" style="width:100%" maxlength="18">
								</div>
							</td>
						</tr>
						<tr id="xf-2-10" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
							<td id="xf-2-10-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" align="center" rowspan="8" width="25%">
								<div class="xf-handler">
									<input type="radio" name="category"  value="对公" onclick="disableElement()"><label>对公</label>
								</div>
							</td>
							<td id="xf-2-10-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="25%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">发票抬头</label>
								</div>
							</td>
							<td id="xf-2-10-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="50%" colspan="2">
								<div class="xf-handler">
									<input type="text" id="invoiceTitlePublic" name="invoiceTitle" style="width:100%" maxlength="100">
								</div>
							</td>
						</tr>
						<tr id="xf-2-11" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
							<td id="xf-2-11-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="25%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">发票明细(产品名称、价格、数量)</label>
								</div>
							</td>
							<td id="xf-2-11-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="50%" colspan="2">
								<div class="xf-handler">
									<textarea rows="5" id="invoiceDetailPublic" name="invoiceDetail" style="width:100%" onkeyup="maxWords()"  onblur="maxWords()"></textarea>
								</div>
							</td>
						</tr>
						<tr id="xf-2-12" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
							<td id="xf-2-12-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="25%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;"> 发票开具总金额</label>
								</div>
							</td>
							<td id="xf-2-12-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="50%" colspan="2">
								<div class="xf-handler">
									<input type="text" id="invoiceMoneyPublic" name="invoiceMoney" style="width:100%"  onblur="var reg = /^[0-9]+([.]{1}[0-9]{1,2})?$/; if(!reg.test(this.value)) this.value = ''; ">
								</div>
							</td>
						</tr>
						<tr id="xf-2-13" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
							<td id="xf-2-13-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="25%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;"> 企业名称</label>
								</div>
							</td>
							<td id="xf-2-13-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="50%" colspan="2">
								<div class="xf-handler">
									<input type="text" id="enterpriseName" name="enterpriseName" style="width:100%" maxlength="100">
								</div>
							</td>
						</tr>
						<tr id="xf-2-14" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
							<td id="xf-2-14-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="25%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;"> 税务登记号</label>
								</div>
							</td>
							<td id="xf-2-14-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="50%" colspan="2">
								<div class="xf-handler">
									<input type="text" id="taxNumber" name="taxNumber" style="width:100%" maxlength="25">
								</div>
							</td>
						</tr>
						<tr id="xf-2-15" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
							<td id="xf-2-15-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="25%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;"> 开户行</label>
								</div>
							</td>
							<td id="xf-2-15-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="50%" colspan="2">
								<div class="xf-handler">
									<input type="text" id="openingBank" name="openingBank" style="width:100%" maxlength="50">
								</div>
							</td>
						</tr>
						<tr id="xf-2-16" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
							<td id="xf-2-16-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="25%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;"> 开户行账号</label>
								</div>
							</td>
							<td id="xf-2-16-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="50%" colspan="2">
								<div class="xf-handler">
									<input type="text" id="accountNumber" name="accountNumber" style="width:100%" maxlength="30" onkeyup="var reg = /^[0-9]\d*$/; if(!reg.test(this.value)) this.value = ''; ">
								</div>
							</td>
						</tr>
						<tr id="xf-2-17" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
							<td id="xf-2-17-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="25%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;"> 企业地址及电话</label>
								</div>
							</td>
							<td id="xf-2-17-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="50%" colspan="2">
								<div class="xf-handler">
									<input type="text" id="enterpriseAddress" name="enterpriseAddress" style="width:100%" maxlength="100">
								</div>
							</td>
						</tr>
							
						<tr id="xf-18">
							<td id="xf-18-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="25%">
								<label style="display:block;text-align:center;margin-bottom:0px;">发票邮寄地址</label>
							</td>
							<td id="xf-18-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" colspan="3">
								<input id="invoiceMailAddress" name="invoiceMailAddress" type="text" style="width:100%">
							</td>
						</tr>
						<tr id="xf-19">
							<td id="xf-19-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" colspan="2" width="50%">
								<label style="display:block;text-align:center;margin-bottom:0px;">收件人姓名</label>
							</td>
							<td id="xf-19-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="25%"><label style="display:block;text-align:center;margin-bottom:0px;">收件人电话</label></td>
							<td id="xf-19-2" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="25%"><label style="display:block;text-align:center;margin-bottom:0px;">收件人备用电话</label></td>
						</tr>
					
						<tr id="xf-20">
							<td id="xf-20-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" colspan="2"><input id="adreesseeName" name="addressee" type="text" style="width:100%" maxlength="10"></td>
							<td id="xf-20-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left"><input id="addresseeTel" name="addresseeTel" type="text" style="width:100%" maxlength="11"></td>
							<td id="xf-20-2" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left"><input id="addresseeSpareTel" name="addresseeSpareTel" type="text" style="width:100%" maxlength="11"></td>
						</tr>
						<tr id="xf-21">
							<td id="xf-21-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" colspan="4">
								<label style="display:block;text-align:left;margin-bottom:0px;">注：产品明细可选日用品、化妆品、保健品统称，增值税专用发票除外</label>
							</td>
						</tr>
						<tr id="xf-22">
							<td id="xf-22-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" colspan="4">
								<label style="display:block;margin-bottom:0px;">注：1.普通发票、增值税普通发票可针对个人或对公开具，增值税专用发票仅针对对公开具；2.增值税专用发票必须开具产品明细，其他票据的产品明细可选填日用品、化妆品、保健品统称；3.增值税专用发票开具时请上传对方公司营业执照、税务登记证及开户行许可证电子版；4.不可直接体现在发票中的产品为：罗麦π化负离子健康机、罗麦健康活氧解毒机、罗麦π石链(长)、罗麦π石链、罗麦π水宝、杯芯（8个/盒）、罗麦居家套装锅具（汤锅、炒锅）、罗麦熣燦套锅（砂光）、罗麦熣燦套锅（镜光），如会员订购此类产品，可将其开具为其他产品。</label>
							</td>
						</tr>
						<tr id="xf-23">
							<td id="xf-23-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" colspan="4">
								<label style="display:block;text-align:center;margin-bottom:0px;">上传附件</label>
							</td>
						</tr>
						<!-- <tr id="xf-24">
							<td id="xf-24-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" colspan="4" height="140px">
								<div class="xf-handler" style="margin-top:0px">
									<input id="fileup" type="file" name="file" value="$('#fileup').val()"><br>
									<input type="button" value="取消上传" onclick="deselect()">
									<label style="display:block;margin-bottom:0px;">注：文件大小（10M）限制, 文件后缀类型（*.jpg;*.gif;*.bmp;*.png;*.doc;*.docx;*.xls;*.xlsx;*.pdf）</label>
								</div>
							</td>
						</tr> -->
						<tr id="xf-24">
							<td id="xf-24-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" colspan="4">
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
		<br><br><br>
    </section>
	<!-- end of main -->

  </div>
  <div class="navbar navbar-default navbar-fixed-bottom">
    <div class="container-fluid">
      <div class="text-center" style="padding-top:8px;">
	    <div class="text-center" style="padding-top:8px;">
			<!-- <button id="saveDraft" class="btn btn-default" type="button" onclick="taskOperation.saveDraft()">保存草稿</button> -->
			<!-- <button id="confirmStartProcess" class="btn btn-default" type="button" onclick="taskOperation.taskConf()">配置任务</button> -->
			<button id="confirmStartProcess" class="btn btn-default" type="button" onclick="startProcessInstance()">提交数据</button>
			<button type="button" class="btn btn-default" onclick="javascript:history.back();">返回</button>
		</div>
	
	  </div>
    </div>
  </div>
  <%@include file="/common/selectPosition.jsp" %>
</form>
</body>

</html>
