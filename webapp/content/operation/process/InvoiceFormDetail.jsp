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
            font-size:14px;
        }
        pre { 
			white-space: pre-wrap;
		    word-wrap: break-word;
		    background-color:white;
		    border:0px
		}
        .tableprint{margin:10px 0 0 0;border-collapse:collapse;}
        .tableprint td{padding-left:20px;padding-right:5px;border:#CCCCCC 1px solid;line-height:35px;font-size:14px;}
        .tdl{white-space:nowrap}
    </style>
	<script type="text/javascript">
        var HKEY_Root, HKEY_Path, HKEY_Key;
        HKEY_Root = "HKEY_CURRENT_USER";
        HKEY_Path = "\\Software\\Microsoft\\Internet Explorer\\PageSetup\\";
        function pagesetup_null() {
            try {
                var RegWsh = new ActiveXObject("WScript.Shell")
                hkey_key = "header"
                RegWsh.RegWrite(hkey_root + hkey_path + hkey_key, "")
                hkey_key = "footer"
                RegWsh.RegWrite(hkey_root + hkey_path + hkey_key, "")
            } catch (e) {}
        }
        function printme() {
        	var bdhtml=window.document.body.innerHTML;//获取当前页的html代码
            document.body.innerHTML = document.getElementById('divPrint').innerHTML;
            pagesetup_null();
            window.print();
            document.body.innerHTML=bdhtml;
            window.close();
        }
    </script>
	<script type="text/javascript">
	//接收申请单的数据
	$(function() {
	 	var id=(<%= request.getParameter("processInstanceId")%>);
			if (id !="") {
		                    $.getJSON('${tenantPrefix}/rs/Invoice/getInvoiceInfo', {
		                    	id: id
		                    }, function(data) {
		                    	for (var i = 0; i < data.length; i++) {
		                    		//alert(JSON.stringify(data)); 
		                    		$("#ucode").html(data[i].ucode);  
		                    		$("#shopName").html(data[i].shopName);
		                    		$("#shopTel").html(data[i].shopTel);  
		                    		$("#applyDate").html(data[i].invoiceDate);  
		                    		$("#orderNumber").html(data[i].orderNumber);  
		                    		
		                    		$("#idNumber").html(data[i].idNumber);
		                    		$("#invoiceType").html(data[i].invoiceType);
		                    		$("#invoiceMailAddress").html(data[i].invoiceMailAddress);
		                    		
		                    		 
		                    		$("#addressee").html(data[i].addressee); 
		                    		$("#addresseeTel").html(data[i].addresseeTel); 
		                    		$("#addresseeSpareTel").html(data[i].addresseeSpareTel); 
		                    		//$("#enclosure").val(data[i].path);
		                    		$("#person").html("个人");
		                    		$("#public").html("对公");
		                    		
		                    		
		                    		
	                    			if(data[i].category == "个人"){
	                    				//$("#person").html(data[i].category);
	                    				//$("#person").attr("checked",true);
	                    				$("#invoiceTitlePerson").html(data[i].invoiceTitle);
		                    			$("#invoiceDetailPerson").html(data[i].invoiceDetail);
		                    			$("#invoiceMoneyPerson").html(data[i].invoiceMoney);
		                    			//发票类别是个人时，给对公的输入框输入空数值
		                    			//$("#public").attr("checked",false);
	                    			}else if(data[i].category == "对公"){
	                    				//$("#public").attr("checked",true);
	                    				$("#invoiceTitlePublic").html(data[i].invoiceTitle);
		                    			$("#invoiceDetailPublic").html(data[i].invoiceDetail);
		                    			$("#invoiceMoneyPublic").html(data[i].invoiceMoney);
		                    			$("#enterpriseName").html(data[i].enterpriseName);
		                    			$("#taxNumber").html(data[i].taxNumber);
		                    			$("#openingBank").html(data[i].openingBank);
		                    			$("#accountNumber").html(data[i].accountNumber);
		                    			$("#enterpriseAddress").html(data[i].enterpriseAddress);
		                    			//发票类别是对公时，给个人的输入框输入空数值
		                    			//$("#person").attr("checked",false);
	                    			}
	                    			
	                    			
	                    			if(data[i].ucode.length == 11){
	                    				changeMallDiv();
	                    				$("#area").html(data[i].area);  
			                    		$("#branchOffice").html(data[i].branchOffice); 
			                    		$("#h1Id").html("罗麦随行发票申请单");
			                    		$("#shopTel").html("无");  
	                    			}else{
	                    				changeOaDiv();
	                    				$("#area").html(data[i].area);  
			                    		$("#system").html(data[i].system);  
			                    		$("#branchOffice").html(data[i].branchOffice); 
			                    		$("#h1Id").html("发票申请单");
	                    			}
	                    			
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
	});
	
	
	function changeMallDiv(){
		$("#form-tfoot").html('<tr id="xf-2-3">'
				+'<td id="xf-2-3-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left" width="25%">'
				+'<div class="xf-handler">'
				+'	<label style="display:block;text-align:center;margin-bottom:0px;">所属区域</label>'
				+'</div>'
				+'</td>'
				+'<td id="xf-2-3-1" class="xf-cell xf-cell-right xf-cell-bottom" width="25%">'
				+'<div class="xf-handler" id="area">'
				//+'	<input type="text" id="areaName" name="areaName" style="border:0px;text-align:center" readonly>'
				+'</div>'
				+'</td>'
				+'<td id="xf-2-3-2" class="xf-cell xf-cell-right xf-cell-bottom"  width="25%">'
				+'<div class="xf-handler">'
				+'	<label style="display:block;text-align:center;margin-bottom:0px;">所属分公司</label>'
				+'</div>'
				+'</td>'
				+'<td id="xf-2-3-3" class="xf-cell xf-cell-right xf-cell-bottom" width="25%">'
				+'<div class="input-group userPicker" style="width: 175px;" id="branchOffice">'
				//+'	<input value="'+companyName+'" type="text" id="companyName" name="companyName" style="border:0px;text-align:center" readonly>'
				+'</div>'
				+'</td>'
				+'</tr>');
	}
	
	function changeOaDiv(){
		$("#form-tfoot").html('<tr id="xf-2-3">'
				+'<td id="xf-2-3-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left" width="25%">'
				+'<div class="xf-handler">'
				+'	<label style="display:block;text-align:center;margin-bottom:0px;">所属区域</label>'
				+'</div>'
				+'</td>'
				+'<td id="xf-2-3-1" class="xf-cell xf-cell-right xf-cell-bottom" width="25%">'
				+'<div class="xf-handler" id="area">'
				//+'	<input type="text" id="areaName" name="areaName" style="border:0px;text-align:center" readonly>'
				+'</div>'
				+'</td>'
				+'<td id="xf-2-3-2" class="xf-cell xf-cell-right xf-cell-bottom"  width="25%">'
				+'<div class="xf-handler">'
				+'	<label style="display:block;text-align:center;margin-bottom:0px;">所属体系</label>'
				+'</div>'
				+'</td>'
				+'<td id="xf-2-3-3" class="xf-cell xf-cell-right xf-cell-bottom" width="25%">'
				+'<div class="xf-handler">'
				+'	<select class="form-control required" >'
				+'		<option id="system" value="">请选择</option>'
				/* +'		<c:forEach items="${systemlist}" var="item">'
				+'			<option value="${item.value}" >${item.name}</option>'
				+'		</c:forEach>' */
				+'	</select>'
				//+'	<input type="hidden" id="systemName" name="systemName">'
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
				+'<div class="input-group userPicker" style="width: 175px;" id="branchOffice">'
				//+'	<input value="'+companyName+'" type="text" id="companyName" name="companyName" style="border:0px;text-align:center" readonly>'
				+'</div>'
				+'</td>'
				+'</tr>');
	}
	
	
    </script>
  </head>

  <body>
<form id="xform" method="post" class="xf-form" enctype="multipart/form-data">
<div id="divPrint">
    <div class="container">

	  <!-- start of main -->
      <section id="m-main" class="col-md-12" style="padding-top:65px;">
		
		<div id="xf-form-table">
			<div id="xf-1" class="xf-section">
				<h1 id="h1Id" style="text-align:center;">发票详情单</h1>
			</div>
			
			<div id="xf-2" class="xf-section">
				<table class="xf-table" width="100%" cellspacing="0" cellpadding="0" border="0" align="center">
					<tbody>
						<tr id="xf-2-0">
							<td id="xf-2-0-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="25%">
								<div class="xf-handler">
									<label class="tdl" style="display:block;text-align:center;margin-bottom:0px;">专卖店编号/手机号</label>
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
								<div class="xf-handler" id="ucode" style="text-align:left">
									<!-- <input type="text" name="ucode"  style="width:100%" onblur='fnshopData()'> -->
								</div>
							</td>
							<td id="xf-2-1-1" class="xf-cell xf-cell-right xf-cell-bottom"  width="25%">
								<div class="xf-handler" id="shopName" style="text-align:left">
									<!-- <input type="text" name="shopName"  style="width:100%" readonly> -->
								</div>
							</td>
							<td id="xf-2-1-2" class="xf-cell xf-cell-right xf-cell-bottom"  width="25%">
								<div class="xf-handler" id="shopTel" style="text-align:left">
									<!-- <input type="text" name="shopTel"  style="width:100%" readonly> -->
								</div>
							</td>
							<td id="xf-2-1-3" class="xf-cell xf-cell-right xf-cell-bottom"  width="25%">
								<div class="xf-handler" id="applyDate" style="text-align:left">
									<!-- <input type="text"  name="invoiceDate" readonly="readonly" style="width:100%;text-align:left" > -->
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
								<span class="xf-handler" id="orderNumber" style='width: 100px;word-wrap:break-word;word-break:break-all'>
									<!-- <input class="form-control required"  name="orderNumber" value="" required="true" style="background-color:white;cursor:default;" type="text"> -->
								</span>
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
								<div class="xf-handler" id="area" style="text-align:left">
									<!-- <input type="text"  name="area" style="border:0px;text-align:left" readonly> -->
								</div>
							</td>
							<td id="xf-2-3-2" class="xf-cell xf-cell-right xf-cell-bottom"  width="25%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">所属体系</label>
								</div>
							</td>
							<td id="xf-2-3-3" class="xf-cell xf-cell-right xf-cell-bottom" width="25%">
								<div class="xf-handler">
									<select class="form-control required">
										<option id="system" value=""></option>
									</select>
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
								<div class="input-group userPicker" style="width: 175px;" id="branchOffice">
									<!-- <input type="text"  name="branchOffice" style="border:0px;text-align:left" readonly> -->
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
								<div class="xf-handler" id="invoiceType">
								</div>
							</td>
						</tr>
						<tr id="xf-9-9">
							<td id="xf-2-9-9" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="25%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">抄送</label>
								</div>
							</td>
							<td id="xf-3-9-9" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="25%" colspan="3">
								<div class="xf-handler" id="copyNames">
								</div>
							</td>
						</tr>
						<tr id="xf-2-6">
							<td id="xf-2-6-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="25%" rowspan="4" align="left">
								<div class="xf-handler" id="person">
									<!-- <input type="radio" name="category"  value="个人" onclick="check()"><label>个人</label> -->
								</div>
							</td>
							<td id="xf-2-6-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="25%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">发票抬头</label>
								</div>
							</td>
							<td id="xf-2-6-2" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="50%" colspan="2">
								<div class="xf-handler" id="invoiceTitlePerson">
									<!-- <input type="text"  name="invoiceTitle" style="width:100%"> -->
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
								<div class="xf-handler" id="invoiceDetailPerson" style='width: 100px;word-wrap:break-word;word-break:break-all'> 
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
								<div class="xf-handler" id="invoiceMoneyPerson">
									<!-- <input type="text"  name="invoiceMoney" style="width:100%"> -->
								</div>
							</td>
						</tr>
						<tr id="xf-2-9" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
							<td id="xf-2-9-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="25%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;"> 身份证号码</label>
								</div>
							</td>
							<td id="xf-2-9-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="75%" colspan="2">
								<div class="xf-handler" id="idNumber">
									<!-- <input  name="idNumber" type="text" style="width:100%"> -->
								</div>
							</td>
						</tr>
						<tr id="xf-2-10" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
							<td id="xf-2-10-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" align="left" rowspan="8" width="25%">
								<div class="xf-handler" id="public">
									<!-- <input type="radio" name="category"  value="对公" onclick="disableElement()"><label>对公</label> -->
								</div>
							</td>
							<td id="xf-2-10-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="25%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">发票抬头</label>
								</div>
							</td>
							<td id="xf-2-10-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="50%" colspan="2">
								<div class="xf-handler" id="invoiceTitlePublic">
									<!-- <input type="text"  name="invoiceTitle" style="width:100%"> -->
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
								<span class="xf-handler" id="invoiceDetailPublic" style='width: 100px;word-wrap:break-word;word-break:break-all'>
								</span>
							</td>
						</tr>
						<tr id="xf-2-12" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
							<td id="xf-2-12-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="25%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;"> 发票开具总金额</label>
								</div>
							</td>
							<td id="xf-2-12-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="50%" colspan="2">
								<div class="xf-handler" id="invoiceMoneyPublic">
									<!-- <input type="text" id="invoiceMoneyPublic" name="invoiceMoney" style="width:100%"> -->
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
								<div class="xf-handler" id="enterpriseName">
									<!-- <input type="text" id="enterpriseName" name="enterpriseName" style="width:100%"> -->
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
								<div class="xf-handler" id="taxNumber">
									<!-- <input type="text"  name="taxNumber" style="width:100%"> -->
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
								<div class="xf-handler" id="openingBank">
									<!-- <input type="text" id="openingBank" name="openingBank" style="width:100%"> -->
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
								<div class="xf-handler" id="accountNumber">
									<!-- <input type="text" id="accountNumber" name="accountNumber" style="width:100%"> -->
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
								<div class="xf-handler" id="enterpriseAddress">
									<!-- <input type="text" id="enterpriseAddress" name="enterpriseAddress" style="width:100%"> -->
								</div>
							</td>
						</tr>
							
						<tr id="xf-18">
							<td id="xf-18-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="25%">
								<label style="display:block;text-align:center;margin-bottom:0px;">发票邮寄地址</label>
							</td>
							<td id="xf-18-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" colspan="3">
								<div class="xf-handler" id="invoiceMailAddress">
									<!-- <input id="invoiceMailAddress" name="invoiceMailAddress" type="text" style="width:100%"> -->
								</div>
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
							<td id="xf-20-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" colspan="2"><div id="addressee" style="text-align:left"></div></td>
							<td id="xf-20-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left"><div id="addresseeTel" style="text-align:left"></div></td>
							<td id="xf-20-2" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left"><div id="addresseeSpareTel" style="text-align:left"></div></td>
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
							<td id="xf-23-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
								<label style="display:block;text-align:center;margin-bottom:0px;">附件内容</label>
							</td>
							<td class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" colspan="3">
                   			 	<%@include file="/common/show_file.jsp" %>
							</td>
						</tr>
						
					</tbody>
				</table>
			</div>
			<c:if test="${isPrint == true}">
                        <div>
                            <table width="100%" cellspacing="0" cellpadding="0" border="0" align="center"
                                   class="tableprint"> 
                                <tbody>
                                <c:forEach var="item" items="${logHumanTaskDtos}"  varStatus="status">
                                		<c:if test="${status.index==0}">
                                			<tr>
												  <td>
												  		<table width="100%" cellspacing="0" cellpadding="0" border="0">
												  			<tr>
												  				<td style="border-width:0;">提交</td>
												  			</tr>
												  			<tr>
												  				<td style="text-align:right;border-width:0;">
												  					<tags:isDelUser userId="${item.assignee}"/>&emsp;<fmt:formatDate value="${item.completeTime}" type="both" pattern='yyyy年MM月dd日 HH时mm分ss秒'/>
												  				</td>
												  			</tr>
												  		</table>
										  		  </td>
		                                    </tr>
                                		</c:if>
                                		<c:if test="${status.index>0}">
                                			<tr>
		                                        <td>${item.name} &nbsp;审批详情</td>
		                                    </tr>
		                                    <tr>
										  		  <td>
										  		  		审核结果：${item.action}<br/>
										  		  		审核意见：${item.comment}
												  </td>
											</tr>
											<tr>
												  <td style="text-align:right;">
												  		<tags:isDelUser userId="${item.assignee}"/>&emsp;<fmt:formatDate value="${item.completeTime}" type="both" pattern='yyyy年MM月dd日 HH时mm分ss秒'/>
												  		&nbsp;审核时长&nbsp;${item.auditDuration}
										  		  </td>
		                                    </tr>
                                		</c:if>
                                </c:forEach>
                                </tbody>
                            </table>
                        </div>
                        </c:if>
                        <c:if test="${isPrint == false}">
			<div class="container">
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
				    <tr>
					  <td>${item.name}</td>
					  <td><tags:isDelUser userId="${item.assignee}"/></td>
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
					  </c:forEach>
				  </tbody>
		    </table>
	 </div>
	 </c:if>
		</div>
    </section>
  </div>
  </div>
  <c:if test="${isPrint == true}">
 	<div style="width:500px;margin:20px auto;text-align:center">
		<input value="打印" class="button" onclick="printme();" type="button">
	</div>
  </c:if>
   <c:if test="${viewBack == true}">
 	<div style="width:500px;margin:20px auto;text-align:center">
		<button type="button" class="btn btn-default" onclick="javascript:history.back();">返回</button>
	</div>
  </c:if>
</form>
</body>
</html>
