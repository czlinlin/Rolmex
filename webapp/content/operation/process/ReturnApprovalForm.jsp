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
		
		//接收申请单数据
		$(function() {
		 	var id=$("#processInstanceId").val();
				if (id !="") {
					$.ajaxSettings.async = false; 
			    	$.getJSON('${tenantPrefix}/rs/Return/getReturnInfo', {
			        	id: id
			        	}, function(data) {
			            	for (var i = 0; i < data.length; i++) {
			            	   //alert(JSON.stringify(data));
			            	   $("#returnId").val(data[i].id);
			                   $("#wareHouse").html(data[i].wareHouse);
			                   $("#empNo").val(data[i].empNo);
			                   $("#ucode").html(data[i].ucode);
			                   $("#shopName").html(data[i].shopName);  
			                   $("#returnDate").html(data[i].returnDate); 
			                   $("#processInstanceId").html(data[i].processInstanceId); 
			                   $("#orderNumber").val(data[i].orderNumber);  
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
				                        "<td><input type='text' value='"+data[i].proName+"' style='border:0px;text-align:center;width:100%' readonly></td>" +
				                        
				                        "<td><input type='text' value='"+data[i].shopPVNum+"' style='border:0px;text-align:center;width:100%' readonly></td>" + 
				                        "<td><input type='text' value='"+data[i].shopReNum+"' style='border:0px;text-align:center;width:100%' readonly></td>" +  
				                        "<td><input type='text' value='"+data[i].shopPV+"' style='border:0px;text-align:center;width:100%' readonly></td>" +  
				                        
				                        "<td><input type='text' value='"+data[i].shopRewardNum+"' style='border:0px;text-align:center;width:100%' readonly></td>" +  
				                        "<td><input type='text' value='"+data[i].shopRewNum+"' style='border:0px;text-align:center;width:100%' readonly></td>" + 
				                        "<td><input type='text' value='"+data[i].shopRewardPV+"' style='border:0px;text-align:center;width:100%' readonly></td>" +
				                        
				                        "<td><input type='text' value='"+data[i].shopWalletNum+"' style='border:0px;text-align:center;width:100%' readonly></td>" +  
				                        "<td><input type='text' value='"+data[i].shopwalNum+"' style='border:0px;text-align:center;width:100%' readonly></td>" +  
				                        "<td><input type='text' value='"+data[i].shopWalletPV+"' style='border:0px;text-align:center;width:100%' readonly></td>" +
				                        "</tr>";
				                    }
				                    var tbody=window.document.getElementById("tbody-result");
				                    tbody.innerHTML = str;
			                    }
			                )
		         };
		})
		//审批表单获取申请人及其他审批人的提交状态信息
		
		/* $(function() {
			
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
		}) */
		
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
                        "actionUrl": '${tenantPrefix}/Return/process-operationReturnApproval-completeTask.do?flag='+flag,
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
		
		function changeMallDiv(){
	   		 $("#form-tfoot").html('<tr id="xf-2-6">'
		 				 	+'<td id="xf-2-6-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="15%">'
						 	+'<div class="xf-handler">'
						 	+'<label style="display:block;text-align:center;margin-bottom:0px;"><font color="red">*</font>&nbsp;退货原因</label>'	
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
							+'	<label style="display:block;text-align:center;margin-bottom:0px;"><font color="red">*</font>&nbsp;退款账户信息</label>'
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
							+'<font color="red">*</font>&nbsp;	商城退货请确定是否已撤单<input type="checkbox" id="confirmId" checked>'
							+'</div>'
							+'</tr>');
	   		 
	   	 }
	   	 function changeOaDiv(){
	   		 $("#form-tfoot").html('<tr id="xf-2-6">'
	   				 	+'<td id="xf-2-6-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="15%">'
	   				 	+'<div class="xf-handler">'
	   				 	+'<label style="display:block;text-align:center;margin-bottom:0px;"><font color="red">*</font>&nbsp;退货原因</label>'	
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
							+'	<label style="display:block;text-align:center;margin-bottom:0px;"><font color="red">*</font>&nbsp;退款账户信息</label>'
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
    <%@include file="/header/bpm-workspace3.jsp"%>
<form id="xform" method="post" class="xf-form" enctype="multipart/form-data">
    <div class="container">
      <section id="m-main" class="col-md-12" style="padding-top:65px;">
		<input id="processInstanceId" type="hidden" name="processInstanceId" value="${processInstanceId}">
		<input id="humanTaskId" type="hidden"  name="humanTaskId" value="${humanTaskId}">
		<input id="activityId" type="hidden" name="activityId" value="">
		<input id="userId" type="hidden" name="userId" value="<%=userId %>">
		<%-- <input id="userId" type="hidden" name="userId" value="<%=request.getParameter("userId") %>>">  --%>
		<%-- <input id="bpmProcessId" type="hidden" name="bpmProcessId" value="<%= request.getParameter("bpmProcessId")%>">
		<input id="businessKey" type="hidden" name="businessKey" value="<%= request.getParameter("businessKey")%>"> --%>
		<!-- <table width="100%" cellspacing="0" cellpadding="0" border="0" align="center" class="xf-table">
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
		</table> -->


      
		<%-- <input id="processDefinitionId" type="hidden" name="processDefinitionId" value="<%= request.getParameter("processDefinitionId")%>">
		<input id="bpmProcessId" type="hidden" name="bpmProcessId" value="<%= request.getParameter("bpmProcessId")%>">
		<input id="autoCompleteFirstTask" type="hidden" name="autoCompleteFirstTask" value="false">
		<input id="businessKey" type="hidden" name="businessKey" value="<%= request.getParameter("businessKey")%>"> --%>
		<input id="returnId" type="hidden" name="returnId" value="">
		
		<div id="xf-form-table">
			<div id="xf-1" class="xf-section">
				<h1 style="text-align:center;">退货审批单</h1>
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
									<label style="display:block;text-align:center;margin-bottom:0px;">客服工号：<input type="text" id="empNo" name="empNo" style="border:0px" readonly></label>
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
						<tr id="xf-2-2" style="height:30px">
							<td id="xf-2-2-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left" width="15%">
								<div class="xf-handler" align="center" id="ucode">
									
								</div>
							</td>
							<td id="xf-2-2-1" class="xf-cell xf-cell-right xf-cell-bottom" width="35%" colspan="4">
								<div class="xf-handler" align="center" id="shopName">
								</div>
							</td>
							<td id="xf-2-2-2" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left" width="15%">
								<div class="xf-handler" align="center" id="shopTel">
								</div>
							</td>
							<td id="xf-2-2-3" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left" width="35%" colspan="4">
								<div class="xf-handler" align="center" id="returnDate">
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
									<textarea id="orderNumber" style="width:100%;border:0px;" readonly rows="2"></textarea>
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
						</tbody>
						<tbody id="tbody-result">
							<tr></tr>
						</tbody>
						<tbody id="form-tfoot">
						<tr id="xf-2-6">
							<td id="xf-2-6-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="15%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">退货原因</label>
								</div>
							</td>
							<td id="xf-2-6-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="75%" colspan="9">
								<div class="xf-handler">
									<textarea id="returnReaon" rows="5" style="width:100%;border:0px;" readonly></textarea>
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
								<div class="xf-handler" align="center" id="shopPayStock" style="height:12px">
									<!-- <label id="shop_pay_stock"></label> -->
									<!-- <input type="text" id="shop_pay_stock" name="shopPayStock"> -->
								</div>
							</td>
							<td id="xf-2-8-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="17%" colspan="2">
								<div class="xf-handler" align="center" id="rewardIntegralStock">
									<!-- <label id="reward_integral_stock"></label> -->
									<!-- <input name="rewardIntegralStock" type="text"> -->
								</div>
							</td>
							<td id="xf-2-8-2" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="18%" colspan="2">
								<div class="xf-handler" align="center" id="personPayStock">
									<!-- <label id="person_pay_stock"></label> -->
									<!-- <input type="text" name="personPayStock"> -->
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
						</tbody>
						<tr id="xf-2-9">
							<td id="xf-2-9-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="15%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">审批人意见</label>
								</div>
							</td>
							<td id="xf-2-9-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="15%" colspan="9">
								<div class="xf-handler">
									<textarea id="comment" name="comment" maxlength="300" style="width:100%" rows="5" cols="20" required onfocus="empty()" onblur="getAgree()">同意</textarea>
								</div>
							</td>
						</tr>
				</table>
				<table>
					<tr>
						<td>
							<div class="xf-handler">
								<label style="display:block;text-align:center;margin-bottom:0px;width:100%"><font style="color:red">*</font>操作密码</label>
							</div>
						</td>
						<td>
							<div class="xf-handler">
								 <input name="txtPrivateKey" type="password" maxlength="25" id="txtPrivateKey"  onblur='isPwd();'/>
                         		 <input id="isPwdRight" name="isPwdRight" type="hidden"/>
							</div>
						</td>
					</tr>
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
