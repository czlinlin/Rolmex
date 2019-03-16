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
		#xtable td{border: 1px solid #B5B5B5}
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
		.xf-table td{border:1px solid gray}
        .tableprint{margin:10px 0 0 0;border-collapse:collapse;}
        .tableprint td{padding-left:20px;padding-right:5px;border:#CCCCCC 1px solid;line-height:35px;font-size:14px;}
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
		document.onmousedown = function(e) {};
		document.onmousemove = function(e) {};
		document.onmouseup = function(e) {};
		document.ondblclick = function(e) {};

		//接收申请单数据
		$(function() {
		 	var id=(<%= request.getParameter("processInstanceId")%>); 
				if (id !="") {
					$.ajaxSettings.async=false;
			    	$.getJSON('${tenantPrefix}/rs/Return/getReturnInfo', {
			        	id: id
			        	}, function(data) {
			            	for (var i = 0; i < data.length; i++) {
			            	   //alert(JSON.stringify(data));
			            	   $("#returnId").val(data[i].id);
			                   $("#wareHouse").html(data[i].wareHouse);
			                   $("#empNo").html(data[i].empNo);
			                   $("#ucode").html(data[i].ucode);
			                   $("#shopName").html(data[i].shopName);  
			                   $("#returnDate").html(data[i].returnDate);  
			                   $("#orderNumber").html(data[i].orderNumber);  
			                   
			                   
			                   if(data[i].ucode.length == 11){
			                	   changeMallDiv();
			                	   $("#shopTel").html("无");  
			                	   $("#returnReaon").html(data[i].returnReaon);  
				                   $("#shopPayStock").html(data[i].shopPayStock);  
				                   $("#rewardIntegralStock").html(data[i].rewardIntegralStock);  
				                   $("#personPayStock").html(data[i].personPayStock); 
				                   $("#bankDeposit").html(data[i].bankDeposit); 
				                   $("#accountName").html(data[i].accountName); 
				                   $("#accountNumber").html(data[i].accountNumber); 
				                   if(data[i].payType == "店支付"){
				                	   $("#shopPay").attr("checked",true);
				                   }
				                   if(data[i].payType == "个人支付"){
				                	   $("#personPay").attr("checked",true);
				                   }
				                   if(data[i].payType == "手续费减免"){
				                	   $("#derate").attr("checked",true);
				                	   $("#inputApplyCode").html(data[i].inputApplyCode); 
				                	   $("#inputApplyCode").removeAttr("style");
				                   }
				                   if(data[i].payType == "贷款中扣除" || data[i].payType == "货款中扣除"){
				                	   $("#mallPay").attr("checked",true);
				                   }
			                   }else{
			                	   changeOaDiv();
			                	   $("#shopTel").html(data[i].shopTel);  
			                	   $("#returnReaon").html(data[i].returnReaon);  
				                   $("#shopPayStock").html(data[i].shopPayStock);  
				                   $("#rewardIntegralStock").html(data[i].rewardIntegralStock);  
				                   $("#personPayStock").html(data[i].personPayStock); 
				                   $("#bankDeposit").html("无"); 
				                   $("#accountName").html("无"); 
				                   $("#accountNumber").html("无");
				                   if(data[i].payType == "店支付"){
				                	   $("#shopPay").attr("checked",true);
				                   }
				                   if(data[i].payType == "个人支付"){
				                	   $("#personPay").attr("checked",true);
				                   }
				                   if(data[i].payType == "手续费减免"){
				                	   $("#derate").attr("checked",true);
				                	   $("#inputApplyCode").html(data[i].inputApplyCode); 
				                	   $("#inputApplyCode").removeAttr("style");
				                	   //removeAttr("readonly");
				                   }
				                   if(data[i].payType == "贷款中扣除" || data[i].payType == "货款中扣除"){
				                	   $("#mallPay").attr("checked",true);
				                   }
				                   
			                   }
			                   $("input[name='payType']").attr("disabled",true);	
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
		})

		//产品表数据
		$(function() {
		 	var id=$("#returnId").val();
		 	//alert("产品="+id);
				if (id !="") {
			    	$.getJSON('${tenantPrefix}/rs/Return/getProductInfo', {
			        	id: id
			        	}, function(data) {
			            		//alert(JSON.stringify(data));
			            		 var str = "";
				                    //遍历产品数据
				                    for (var i = 0; i < data.length; i++) {
				                    	str += "<tr style='width:200px'>" +  
				                        "<td><div align='left'>"+data[i].proName+"</div></td>" +
				                        
				                        "<td><div align='left'>"+data[i].shopPVNum+"</div></td>" +
				                        "<td><div align='left'>"+data[i].shopReNum+"</div></td>" +
				                        "<td><div align='left'>"+data[i].shopPV+"</div></td>" +
				                        
				                        "<td><div align='left'>"+data[i].shopRewardNum+"</div></td>" +
				                        "<td><div align='left'>"+data[i].shopRewNum+"</div></td>" +
				                        "<td><div align='left'>"+data[i].shopRewardPV+"</div></td>" +
				                        
				                        "<td><div align='left'>"+data[i].shopWalletNum+"</div></td>" +
				                        "<td><div align='left'>"+data[i].shopwalNum+"</div></td>" +
				                        "<td><div align='left'>"+data[i].shopWalletPV+"</div></td>" +
				                        "</tr>";
				                    }
				                    var tbody=window.document.getElementById("tbody-result");
				                    tbody.innerHTML = str;
			                    }
			                )
		         };
		})
   	 function changeMallDiv(){
   		 $("#form-tfoot").html('<tr id="xf-2-6">'
	 				 	+'<td id="xf-2-6-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="15%">'
					 	+'<div class="xf-handler">'
					 	+'<label style="display:block;text-align:center;margin-bottom:0px;">退货原因</label>'	
					 	+'</div>'
					 	+'</td>'
					 	+'<td id="xf-2-6-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="75%" colspan="9">'
					 	+'<div class="xf-handler">'
					 	+'<textarea readonly id="returnReaon" name="returnReaon" rows="5" style="width:100%" maxlength="500" onkeyup="MaxWords(this)" onblur="MaxWords(this)"></textarea>'	
					 	+'<label id="recordNum" style="display: none;text-align:right;" class="control-label col-md-12"></label>'	
					 	+'</div>'
					 	+'</td>'
					 	+'</tr>'
					 	+'<tr id="xf-2-7">'
						+'<td id="xf-2-7-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="15%" rowspan="2">'
						+'<div class="xf-handler">'
						+'<label style="display:block;text-align:center;margin-bottom:0px;">专卖店库存</label>'	
						+'</div>'
						+'</td>'
						+'<td id="xf-2-7-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="16%" colspan="2" >'
						+'<div class="xf-handler">'
						+'<label style="display:block;text-align:center;margin-bottom:0px;">店支付库存</label>'	
						+'</div>'
						+'</td>'
						+'<td id="xf-2-7-2" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="17%" colspan="2" >'
						+'<div class="xf-handler">'
						+'<label style="display:block;text-align:center;margin-bottom:0px;">奖励积分库存</label>'	
						+'</div>'
						+'</td>'
						+'<td id="xf-2-7-3" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="18%" colspan="2">'
						+'<div class="xf-handler">'
						+'<label style="display:block;text-align:center;margin-bottom:0px;">个人钱包库存</label>'	
						+'</div>'
						+'</td>'
						+'<td id="xf-2-7-4" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="8%" rowspan="2">'
						+'<div class="xf-handler">'
						+'<label style="display:block;text-align:center;margin-bottom:0px;">手续费</label>'	
						+'</div>'
						+'</td>'
						+'<td id="xf-2-7-6" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="16%" colspan="2">'
						+'<div class="xf-handler">'
						+'<input type="radio" name="payType" value="货款中扣除" id="mallPay">货款中扣除'	
						+'</div>'
						+'</td>'
						+'<tr id="xf-2-9">'
						+'<td id="xf-2-9-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="16%" colspan="2">'
						+'<div class="xf-handler">'
						+'	<!-- <label id="shop_pay_stock"></label> -->'
						+'<span id="shopPayStock"></span>'	
						+'</div>'
						+'</td>'
						+'<td id="xf-2-9-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="17%" colspan="2">'
						+'<div class="xf-handler">'
						+'	<!-- <label id="reward_integral_stock"></label> -->'
						+'<span id="rewardIntegralStock"></span>'	
						+'</div>'
						+'</td>'
						+'<td id="xf-2-9-2" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="18%" colspan="2">'
						+'<div class="xf-handler">'
						+'	<!-- <label id="person_pay_stock"></label> -->'
						+'<span id="personPayStock"></span>'	
						+'</div>'
						+'</td>'
						+'<td id="xf-2-9-3" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="16%" colspan="2">'
						+'<div class="xf-handler">'
						+'<input type="radio" name="payType" value="手续费减免" id="derate">手续费减免'	
						+'<br/><span id="inputApplyCode" style="display:none;"></span>'	
						+'</div>'
						+'</td>'
						+'</tr>'
						+'<tr id="xf-3-1">'
						+'<td id="xf-3-1-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left" width="15%" rowspan="2">'
						+'<div class="xf-handler">'
						+'	<label style="display:block;text-align:center;margin-bottom:0px;">退款账户信息</label>'
						+'</div>'
						+'</td>'
						+'<td id="xf-3-1-1" class="xf-cell xf-cell-right xf-cell-bottom"  width="20%" colspan="3">'
						+'<div class="xf-handler">'
						+'	<label style="display:block;text-align:center;margin-bottom:0px;">开户行(含支行)</label>'
						+'</div>'
						+'</td>'
						+'<td id="xf-3-1-2" class="xf-cell xf-cell-right xf-cell-bottom"  width="20%" colspan="3">'
						+'<div class="xf-handler">'
						+'	<label style="display:block;text-align:center;margin-bottom:0px;">开户名</label>'
						+'</div>'
						+'</td>'
						+'<td id="xf-3-1-3" class="xf-cell xf-cell-right xf-cell-bottom"  width="20%" colspan="3">'
						+'<div class="xf-handler" >'
						+'	<label style="display:block;text-align:center;margin-bottom:0px;">账号</label>'
						+'</div>'
						+'</td>'
						+'</tr>'
						+'<tr id="xf-3-2">'
						+'<td id="xf-3-2-1" class="xf-cell xf-cell-right xf-cell-bottom" width="20%" colspan="3">'
						+'<div class="xf-handler">'
						+'	<span id="bankDeposit"></span>'
						+'</div>'
						+'</td>'
						+'<td id="xf-3-2-2" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left" width="20%" colspan="3">'
						+'<div class="xf-handler">'
						+'	<span id="accountName"></span>'
						+'</div>'
						+'</td>'
						+'<td id="xf-3-2-3" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left" width="20%" colspan="3">'
						+'<div class="xf-handler">'
						+'	<span id="accountNumber"></span>'
						+'</div>'
						+'</td>'
						+'</tr>'
						+'<tr id="xf-3-3">'
						+'<td id="xf-3-3-1" class="xf-cell xf-cell-right xf-cell-bottom" width="100%" colspan="10">'
						+'<div class="xf-handler" style="display:block;text-align:center;margin-bottom:0px;">'
						+'商城退货请确定是否已撤单<input disabled="true" type="checkbox" id="confirmId" checked>'
						+'</div>'
						+'</tr>');
   		 
   	 }
   	 function changeOaDiv(){
   		 $("#form-tfoot").html('<tr id="xf-2-6">'
   				 	+'<td id="xf-2-6-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="15%">'
   				 	+'<div class="xf-handler">'
   				 	+'<label style="display:block;text-align:center;margin-bottom:0px;">退货原因</label>'	
   				 	+'</div>'
   				 	+'</td>'
   				 	+'<td id="xf-2-6-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="75%" colspan="9">'
   				 	+'<div class="xf-handler">'
   				 	+'<textarea readonly id="returnReaon" name="returnReaon" rows="5" style="width:100%" maxlength="500" onkeyup="MaxWords(this)" onblur="MaxWords(this)"></textarea>'	
   				 	+'<label id="recordNum" style="display: none;text-align:right;" class="control-label col-md-12"></label>'	
   				 	+'</div>'
   				 	+'</td>'
   				 	+'</tr>'
   				 	+'<tr id="xf-2-7">'
						+'<td id="xf-2-7-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="15%" rowspan="3">'
						+'<div class="xf-handler">'
						+'<label style="display:block;text-align:center;margin-bottom:0px;">专卖店库存</label>'	
						+'</div>'
						+'</td>'
						+'<td id="xf-2-7-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="16%" colspan="2" rowspan="2">'
						+'<div class="xf-handler">'
						+'<label style="display:block;text-align:center;margin-bottom:0px;">店支付库存</label>'	
						+'</div>'
						+'</td>'
						+'<td id="xf-2-7-2" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="17%" colspan="2" rowspan="2">'
						+'<div class="xf-handler">'
						+'<label style="display:block;text-align:center;margin-bottom:0px;">奖励积分库存</label>'	
						+'</div>'
						+'</td>'
						+'<td id="xf-2-7-3" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="18%" colspan="2" rowspan="2">'
						+'<div class="xf-handler">'
						+'<label style="display:block;text-align:center;margin-bottom:0px;">个人钱包库存</label>'	
						+'</div>'
						+'</td>'
						+'<td id="xf-2-7-4" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="8%" rowspan="3">'
						+'<div class="xf-handler">'
						+'<label style="display:block;text-align:center;margin-bottom:0px;">手续费</label>'	
						+'</div>'
						+'</td>'
						+'<td id="xf-2-7-5" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="8%">'
						+'<div class="xf-handler">'
						+'<label style="display:block;text-align:center;margin-bottom:0px;">店支付</label>'	
						+'</div>'
						+'</td>'
						+'<td id="xf-2-7-6" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="8%">'
						+'<div class="xf-handler">'
						+'<input type="radio" name="payType" value="店支付" id="shopPay">50元'	
						+'</div>'
						+'</td>'
						+'</tr>'
						+'<tr id="xf-2-8">'
						+'<td id="xf-2-8-3" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="8%">'
						+'<div class="xf-handler">'
						+'<label style="display:block;text-align:center;margin-bottom:0px;">个人支付</label>'	
						+'</div>'
						+'</td>'
						+'<td id="xf-2-8-4" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="8%">'
						+'<div class="xf-handler">'
						+'<input type="radio" name="payType" value="个人支付" id="personPay">50元'	
						+'</div>'
						+'</td>'
						+'</tr>'
						+'<tr id="xf-2-9">'
						+'<td id="xf-2-9-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="16%" colspan="2">'
						+'<div class="xf-handler">'
						+'	<!-- <label id="shop_pay_stock"></label> -->'
						+'<span id="shopPayStock"></span> '	
						+'</div>'
						+'</td>'
						+'<td id="xf-2-9-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="17%" colspan="2">'
						+'<div class="xf-handler">'
						+'	<!-- <label id="reward_integral_stock"></label> -->'
						+'<span id="rewardIntegralStock"></span> '	
						+'</div>'
						+'</td>'
						+'<td id="xf-2-9-2" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="18%" colspan="2">'
						+'<div class="xf-handler">'
						+'	<!-- <label id="person_pay_stock"></label> -->'
						+'<span id="personPayStock"></span> '	
						+'</div>'
						+'</td>'
						+'<td id="xf-2-9-3" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="16%" colspan="2">'
						+'<div class="xf-handler">'
						+'<input type="radio" name="payType" value="手续费减免" id="derate">手续费减免'	
						+'<br/><span id="inputApplyCode" style="display:none;"></span> '	
						+'</div>'
						+'</td>'
						+'</tr>'
						+'<tr id="xf-3-1">'
						+'<td id="xf-3-1-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left" width="15%" rowspan="2">'
						+'<div class="xf-handler">'
						+'	<label style="display:block;text-align:center;margin-bottom:0px;">退款账户信息</label>'
						+'</div>'
						+'</td>'
						+'<td id="xf-3-1-1" class="xf-cell xf-cell-right xf-cell-bottom"  width="20%" colspan="3">'
						+'<div class="xf-handler">'
						+'	<label style="display:block;text-align:center;margin-bottom:0px;">开户行(含支行)</label>'
						+'</div>'
						+'</td>'
						+'<td id="xf-3-1-2" class="xf-cell xf-cell-right xf-cell-bottom"  width="20%" colspan="3">'
						+'<div class="xf-handler">'
						+'	<label style="display:block;text-align:center;margin-bottom:0px;">开户名</label>'
						+'</div>'
						+'</td>'
						+'<td id="xf-3-1-3" class="xf-cell xf-cell-right xf-cell-bottom"  width="20%" colspan="3">'
						+'<div class="xf-handler" >'
						+'	<label style="display:block;text-align:center;margin-bottom:0px;">账号</label>'
						+'</div>'
						+'</td>'
						+'</tr>'
						+'<tr id="xf-3-2">'
						+'<td id="xf-3-2-1" class="xf-cell xf-cell-right xf-cell-bottom" width="20%" colspan="3">'
						+'<div class="xf-handler">'
						+'<span id="bankDeposit">无</span>'
						+'</div>'
						+'</td>'
						+'<td id="xf-3-2-2" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left" width="20%" colspan="3">'
						+'<div class="xf-handler">'
						+'<span id="accountName">无</span>'
						+'</div>'
						+'</td>'
						+'<td id="xf-3-2-3" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left" width="20%" colspan="3">'
						+'<div class="xf-handler">'
						+'<span id="accountNumber">无</span>'
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
		<input id="returnId" type="hidden" name="returnId" value="">
		<div id="xf-form-table">
			<div id="xf-1" class="xf-section">
				<h1 style="text-align:center;">退货详情单</h1>
			</div>
			
			<div id="xf-2" class="xf-section">
				<table id="xtable" class="xf-table" width="100%" cellspacing="0" cellpadding="0" border="0" align="center">
					<tbody>
						<tr id="xf-2-0">
							<td id="xf-2-0-0" style="text-align:center;" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="50%" colspan="5">
								<div class="xf-handler">
									<label >所属仓库 &nbsp
										<select name="wareHouse">
											<option id="wareHouse" value=""></option>
										</select>
									</label>
								</div>
							</td>
							<td id="xf-2-0-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left"  width="50%" colspan="5">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">客服工号：<span id="empNo"></span></label>
								</div>
							</td>
						</tr>
						<tr id="xf-2-1">
							<td id="xf-2-1-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left" width="15%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">专卖店编号/手机号</label>
								</div>
							</td>
							<td id="xf-2-1-1" class="xf-cell xf-cell-right xf-cell-bottom"  width="35%" colspan="4">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">专卖店姓名</label>
								</div>
							</td>
							<td id="xf-2-1-2" class="xf-cell xf-cell-right xf-cell-bottom"  width="15%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">专卖店电话</label>
								</div>
							</td>
							<td id="xf-2-1-3" class="xf-cell xf-cell-right xf-cell-bottom"  width="35%" colspan="4">
								<div class="xf-handler" >
									<label style="display:block;text-align:center;margin-bottom:0px;">申请退货日期</label>
								</div>
							</td>
						</tr>
						<tr id="xf-2-2">
							<td id="xf-2-2-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left" width="15%">
								<div class="xf-handler" style="text-align:left;">
									<span id="ucode"></span>
								</div>
							</td>
							<td id="xf-2-2-1" class="xf-cell xf-cell-right xf-cell-bottom" width="35%" colspan="4">
								<div class="xf-handler" style="text-align:left;">
									<span id="shopName"></span>
								</div>
							</td>
							<td id="xf-2-2-2" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left" width="15%">
								<div style="text-align:left;">
									<span id="shopTel"></span>
								</div>
							</td>
							<td id="xf-2-2-3" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left" width="35%" colspan="4">
								<div class="xf-handler" style="text-align:left;">
									<span id="returnDate"></span>
								</div>
							</td>
						</tr>
						<tr id="xf-2-3">
							<td id="xf-2-3-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left" width="15%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">订单单据号</label>
								</div>
							</td>
							<td id="xf-2-3-1" class="xf-cell xf-cell-right xf-cell-bottom" width="75%" colspan="9">
								<div class="xf-handler">
									<span id="orderNumber" style="white-space:pre-wrap;"></span>
								</div>
							</td>
						</tr>
						<tr id="xf-9-9">
							<td id="xf-9-9-9" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left" width="15%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">&nbsp;抄送</label>
								</div>
							</td>
							<td id="xf-2-3-1" class="xf-cell xf-cell-right xf-cell-bottom" width="75%" colspan="9">
								<div class="xf-handler">
									<span id="copyNames" style="white-space:pre-wrap;"></span>
								</div>
							</td>
						</tr>
						<tr id="xf-2-4">
							<td id="xf-2-4-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="15%" rowspan="2">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">产品名称</label>
								</div>
							</td>
							<td id="xf-2-4-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" colspan="3" width="24%">
								<label style="display:block;text-align:center;margin-bottom:0px;">店支付账户退货</label>
							</td>
							<td id="xf-2-4-2" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" colspan="3" width="27%">
								<label style="display:block;text-align:center;margin-bottom:0px;">奖励积分账户退货</label>
							</td>
							<td id="xf-2-4-3" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" colspan="3" width="24%">
								<label style="display:block;text-align:center;margin-bottom:0px;">个人钱包账户退货</label>
							</td>
						</tr>
						<tr id="xf-2-5">
							<td id="xf-2-5-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="8%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">订货数量</label>
								</div>
							</td>
							<td id="xf-2-5-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="8%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">退货数量</label>
								</div>
							</td>
							<td id="xf-2-5-2" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="8%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">总PV</label>
								</div>
							</td>
							<td id="xf-2-5-3" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="9%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">订货数量</label>
								</div>
							</td>
							<td id="xf-2-5-4" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="9%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">退货数量</label>
								</div>
							</td>
							<td id="xf-2-5-5" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="9%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">总PV</label>
								</div>
							</td>
							<td id="xf-2-5-6" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="8%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">订货数量</label>
								</div>
							</td>
							<td id="xf-2-5-7" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="8%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">退货数量</label>
								</div>
							</td>
							<td id="xf-2-5-8" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="8%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">总PV</label>
								</div>
							</td>
						</tr>
						<tbody id="tbody-result"></tbody>
						<tfoot id="form-tfoot">
						<tr id="xf-2-6">
							<td id="xf-2-6-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="15%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">退货原因</label>
								</div>
							</td>
							<td id="xf-2-6-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="75%" colspan="9">
								<div class="xf-handler">
									<span id="returnReaon" style="white-space:pre-wrap;"></span>
								</div>
							</td>
						</tr>
						<tr id="xf-2-7">
							<td id="xf-2-7-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="15%" rowspan="2">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">专卖店库存</label>
								</div>
							</td>
							<td id="xf-2-7-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="16%" colspan="2">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">店支付库存</label>
								</div>
							</td>
							<td id="xf-2-7-2" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="17%" colspan="2">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">奖励积分库存</label>
								</div>
							</td>
							<td id="xf-2-7-3" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="18%" colspan="2">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">个人钱包库存</label>
								</div>
							</td>
							<td id="xf-2-7-4" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="8%" rowspan="2">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">手续费</label>
								</div>
							</td>
							<td id="xf-2-7-5" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="8%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">店支付</label>
								</div>
							</td>
							<td id="xf-2-7-6" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="8%">
								<div class="xf-handler">
									<input type="radio" name="payType" value="店支付" id="shopPay">50元
								</div>
							</td>
						</tr>
						<tr id="xf-2-8">
							<td id="xf-2-8-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="16%" colspan="2">
								<div class="xf-handler" style="text-align:left;">
									<span id="shopPayStock"></span>
								</div>
							</td>
							<td id="xf-2-8-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="17%" colspan="2">
								<div class="xf-handler" style="text-align:left;">
									<span id="rewardIntegralStock"></span>
								</div>
							</td>
							<td id="xf-2-8-2" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="18%" colspan="2">
								<div class="xf-handler" style="text-align:left;">
									<span id="personPayStock"></span>
								</div>
							</td>
							<td id="xf-2-8-3" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="8%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">个人支付</label>
								</div>
							</td>
							<td id="xf-2-8-4" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="8%">
								<div class="xf-handler">
									<input type="radio" name="payType" value="个人支付" id="personPay">50元
								</div>
							</td>
						</tr>
						</tfoot>
					</tbody>
				</table>
			</div>
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
