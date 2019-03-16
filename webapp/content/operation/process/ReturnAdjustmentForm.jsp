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
		.colour-ee{
			background: #eeee;
		}
		.xf-table td{border:1px solid gray}
		input{border : 1px solid #F2F2F2;height:25px}
		select{border : 1px solid #F2F2F2}
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
		
		//点击获取订单信息
		function getOrderInfo (){
			$("#shopPayStock").val("");
			$("#rewardIntegralStock").val("");
			$("#personPayStock").val("");
			var ucode = $("#ucode").val().replace(/(^\s*)|(\s*$)/g, "");
			var orderNumber = $("#orderNumber").val().replace(/(^\s*)|(\s*$)/g, "");
			if (orderNumber == '') {
                alert("请输入订单单据号！以逗号来分隔，例如：OC15020500001,OC15020500002");
                return;
            }
			$("#result-tbody").find("tr").remove();
			$("#result-tbody").append("<tr></tr>");
			
			var ucode = $("#ucode").val();
			
			//判断是否订单是否超过日期
			$.getJSON('${tenantPrefix}/rs/varUser/isMoreThanDate',{
				customerInfoId:ucode,orderNumber : orderNumber
			},function(checkResult){
				console.log(checkResult);
				
				if(checkResult.orderOut != ""&&checkResult.orderOut != null){
					if(checkResult.platform == "mall"){
						alert("订单号"+checkResult.orderOut+"已超过一个月");
						loading.modal('hide');
						return
					}else{
						alert("订单号"+checkResult.orderOut+"已超过三个月");
						loading.modal('hide');
						return
					}
				}
				if(checkResult.orderWrongful != ""&&checkResult.orderWrongful != null){
					
					alert("订单号格式不对或订单日期在当前时间之后");
					loading.modal('hide');
					return
				}
				
				//判断是否为商城订单 ckx
				if(ucode.length == 11){
					//商城
					//根据专卖店编号和单据号获取退货信息
	                $.getJSON('${tenantPrefix}/rs/varUser/mallOrderInfo', {
	                    customerInfoId: ucode,
	                    orderInfoId : orderNumber

	                    }, function(data) {
	    	              	 //alert(JSON.stringify(data));
	    	              	 if(data.length == 0){
	    	              		$("#pro").val("");
	    	              		 alert("请检查专卖店编号/手机号与订单号是否匹配");
	    	              		 
	    	              	 }
	            			 $("#shopName").val(data[0].shopName);
	            			// $("#shopTel").val(data[0].shopTel);
	            			 $("#pro").val(data[0].proName);
		          			 var total = data.length;
		          			 
	            			 var str = "";
	                      //遍历产品数据
	                      for (var i = 0; i < data.length; i++) {
	                    	  var proPV = returnFloat(data[i].proPV);
	                      	str += "<tr id='bor' name='productArray'>" +  
	                          "<td align='left' class='mytd'><input type='hidden' name='proName' value='" + data[i].proName + "'>" +
	                          "<input id='proNo" + data[i].proNo + "' type='hidden' name='proNo' value='" + data[i].proPV + "'>" +data[i].proName+ "</td>" +
	                          
	                          "<td align='left' class='mytd'><input id ='proNum" + data[i].proNo + "' type='hidden' name='shopPVNum' value='" + data[i].shopPVNum +"'>" +data[i].shopPVNum + "</td>" +
	                          "<td align='left' class='mytd colour-ee'><input class='colour-ee' id='shopReturn" + data[i].proNo + "' name='shopReturn' type='text' value='0' style='border:0px;text-align:left'" + ' onkeyup="getShopPV(' + "'" + data[i].proPV + "','"  + data[i].proNo + "','" + total +"')" + '"></td>' +
	                          "<td align='left' id='shopProPV" + data[i].proNo + "'>0.00</td>" +
	                          
	                          "<td align='left' class='mytd'><input id ='proRewardNum" + data[i].proNo + "' type='hidden' name='shopRewardNum' value='"+data[i].shopRewardNum+"'>"+data[i].shopRewardNum + "</td>" +
	                          "<td align='left' class='mytd'><input id='shopReward" + data[i].proNo + "' name='rewardReturn' value='0' type='text' style='border:0px;text-align:left'" + ' onkeyup="getRewardPV(' + "'" + data[i].proPV + "','"  + data[i].proNo + "','"+ total + "')" + '"></td>' +
	                          "<td align='left' id='shopRewardPV"+ data[i].proNo+"'>0.00</td>" +
	                          
	                          "<td align='left' class='mytd'><input id ='proWalletNum" + data[i].proNo + "' type='hidden' name='shopWalletNum' value='"+data[i].shopWalletNum+"'>"+data[i].shopWalletNum + "</td>" +
	                          "<td align='left' class='mytd'><input id='shopWallet" + data[i].proNo + "' name='walletReturn' value='0' type='text' style='border:0px;text-align:left'" + ' onkeyup="getWalletPV(' + "'" + data[i].proPV + "','"  + data[i].proNo + "','" + total + "')" + '"></td>' +
	                          "<td align='left' id='shopWalletPV"+data[i].proNo+"' class='mytd'>0.00</td>" +
	                          "<input id='shopInputProPV" + data[i].proNo + "' name='shopProPV' type='hidden' value='0'>" +
	                          "<input id='shopInputRewardPV"+ data[i].proNo+"' name='shopRewardPV' type='hidden' value='0' >" +
	                          "<input id='shopInputWalletPV"+data[i].proNo+"' name='shopWalletPV' type='hidden' value='0'>" +
	                          "<input id='proPV"+data[i].proNo+"' name='proPV' type='hidden' value='"+proPV+"'>" + 
	                          "</tr>"; 
	                          
	                      }
	                      var tbody=window.document.getElementById("tbody-result");
	                      tbody.innerHTML = str;
	  				});
				}else{
					 //根据专卖店编号和单据号获取退货信息
					 $.getJSON('${tenantPrefix}/rs/varUser/orderInfo', {
				                  customerInfoId: ucode,
				                  orderInfoId : orderNumber

				                  }, function(data) {
			                    	 //alert(JSON.stringify(data));
				          			 
				          			 var total = data.length;
				          			 if(total == 0){
				          				$("#pro").val("");
				          				alert("请检查专卖店编号/手机号与订单号是否匹配");
			                    	 }
				          			$("#shopName").val(data[0].shopName);
				          			 $("#shopTel").val(data[0].shopTel);
				          			 $("#pro").val(data[0].proName);
				          			 var str = "";
				                    //遍历产品数据
				                    for (var i = 0; i < data.length; i++) {
				                    	str += "<tr id='bor' name='productArray'>" +  
				                        "<td align='left' class='mytd'><input type='hidden' name='proName' value='" + data[i].proName + "'>" +
				                        "<input id='proNo" + data[i].proNo + "' type='hidden' name='proNo' value='" + data[i].proPV + "'>" +data[i].proName+ "</td>" +

				                        "<td align='left' class='mytd'><input id ='proNum" + data[i].proNo + "' type='hidden' name='shopPVNum' value='" + data[i].shopPVNum +"'>" +data[i].shopPVNum + "</td>" +
				                        "<td align='left' class='mytd colour-ee'><input class='colour-ee' id='shopReturn" + data[i].proNo + "' name='shopReturn' type='text' value='0' style='border:0px;text-align:left'" + ' onkeyup="getShopPV(' + "'" + data[i].proPV + "','"  + data[i].proNo + "','" + total +"')" + '"></td>' +
				                        "<td align='left' id='shopProPV" + data[i].proNo + "'>0.00</td>" +

				                        "<td align='left' class='mytd'><input id ='proRewardNum" + data[i].proNo + "' type='hidden' name='shopRewardNum' value='"+data[i].shopRewardNum+"'>"+data[i].shopRewardNum + "</td>" +
				                        "<td align='left' class='mytd colour-ee'><input class='colour-ee' id='shopReward" + data[i].proNo + "' name='rewardReturn' value='0' type='text' style='border:0px;text-align:left'" + ' onkeyup="getRewardPV(' + "'" + data[i].proPV + "','"  + data[i].proNo + "','"+ total + "')" + '"></td>' +
				                        "<td align='left' id='shopRewardPV"+ data[i].proNo+"'>0.00</td>" +

				                        "<td align='left' class='mytd'><input id ='proWalletNum" + data[i].proNo + "' type='hidden' name='shopWalletNum' value='"+data[i].shopWalletNum+"'>"+data[i].shopWalletNum + "</td>" +
				                        "<td align='left' class='mytd colour-ee'><input class='colour-ee' id='shopWallet" + data[i].proNo + "' name='walletReturn' value='0' type='text' style='border:0px;text-align:left'" + ' onkeyup="getWalletPV(' + "'" + data[i].proPV + "','"  + data[i].proNo + "','" + total + "')" + '"></td>' +
				                        "<td align='left' id='shopWalletPV"+data[i].proNo+"' class='mytd'>0.00</td>" +
				                        "<input id='shopInputProPV" + data[i].proNo + "' name='shopProPV' type='hidden' value='0'>" +
				                        "<input id='shopInputRewardPV"+ data[i].proNo+"' name='shopRewardPV' type='hidden' value='0' >" +
				                        "<input id='shopInputWalletPV"+data[i].proNo+"' name='shopWalletPV' type='hidden' value='0'>" +
				                        "<input id='proPV"+data[i].proNo+"' name='proPV' type='hidden' value='"+data[i].proPV+"'>" +
				                        "</tr>";

				                    }

				                    var tbody=window.document.getElementById("tbody-result");
				                    tbody.innerHTML = str;
				          });
				}
				
			});

		}

		//保留小数点后两位
		function returnFloat(value){
			 var value=Math.round(parseFloat(value)*100)/100;
			 var xsd=value.toString().split(".");
			 if(xsd.length==1){
				 val=value.toString() +".00";
				 return val;
			 }
			 if(xsd.length>1){
				 if(xsd[1].length<2){
				 	val=value.toString()+"0";
				 }
				 return val;
			 }
		}


		//店支付
		function getShopPV(proPv, proNo,total){
			var maxnum = $("#proNum" + proNo).val();
			var num = $("#shopReturn" + proNo).val();
			if(num == ""){
				$("#shopReturn" + proNo).val(0);
				num = 0;
			}
			//var money = $("#proNo" + proNo).val();
			var numlength = num.toString().length;
			var maxnumlength = maxnum.toString().length;
			if (num > maxnum || numlength > maxnumlength) {
				$("#shopReturn" + proNo).val(maxnum);
				var value = maxnum * proPv;
			}else{
				var value = num * proPv;
			}
			var a = returnFloat(value);


			var sumShopPv = $("#shopPayStock").val();
			if (sumShopPv == '') {
				$("#shopPayStock").val(a);
			} else {
				var oldPv =$("#shopInputProPV" + proNo).val();
				$("#shopPayStock").val(returnFloat(Number(sumShopPv) - Number(oldPv)  + Number(a)));
			}

			$("#shopProPV" + proNo).html(a);
			$("#shopInputProPV" + proNo).val(a);
		}
		//奖励积分
		function getRewardPV(proPv, proNo,total){
			var maxnum = $("#proRewardNum" + proNo).val();
			var num = $("#shopReward" + proNo).val();
			if(num == ""){
				$("#shopReward" + proNo).val(0);
				num = 0;
			}
				
			//var money = $("#proNo" + proNo).val();
			var numlength = num.toString().length;
			var maxnumlength = maxnum.toString().length;
			if (num > maxnum || numlength > maxnumlength) {
				$("#shopReward" + proNo).val(maxnum);
				var value = maxnum * proPv;
			}else{
				$("#shopReward" + proNo).val(num);
				var value = num * proPv;
			}
			var a = returnFloat(value);

			var sumRewardPv = $("#rewardIntegralStock").val();
			if (sumRewardPv == '') {
				$("#rewardIntegralStock").val(a);
			} else {
				var oldPv =$("#shopInputRewardPV" + proNo).val();
				$("#rewardIntegralStock").val(returnFloat(Number(sumRewardPv) - Number(oldPv)  + Number(a)));
			}

			$("#shopRewardPV" + proNo).html(a);
			$("#shopInputRewardPV" + proNo).val(a);

		}
		//个人钱包
		function getWalletPV(proPv, proNo,total){
			var maxnum = $("#proWalletNum" + proNo).val();
			var num = $("#shopWallet" + proNo).val();
			if(num == ""){
				$("#shopWallet" + proNo).val(0);
				num = 0;
			}
			//var money = $("#proNo" + proNo).val();
			var numlength = num.toString().length;
			var maxnumlength = maxnum.toString().length;
			if (num > maxnum || numlength > maxnumlength) {
				$("#shopWallet" + proNo).val(maxnum);
				var value = maxnum * proPv;
			}else{
				$("#shopWallet" + proNo).val(num);
				var value = num * proPv;
			}
			var a = returnFloat(value);
			var sumWalletPv = $("#personPayStock").val();
			if (sumWalletPv == '') {
				$("#personPayStock").val(a);
			} else {
				var oldPv =$("#shopInputWalletPV" + proNo).val();
				$("#personPayStock").val(returnFloat(Number(sumWalletPv) - Number(oldPv)  + Number(a)));
			}

			$("#shopWalletPV" + proNo).html(a);
			$("#shopInputWalletPV" + proNo).val(a);
		}


		//驳回后接收自己当时提交的主表数据
		$(function() {
		 	var id= $("#processInstanceId").val();
				if (id !="") {
			    	$.getJSON('${tenantPrefix}/rs/Return/getReturnInfo', {
			        	id: id
			        	}, function(data) {
			            	for (var i = 0; i < data.length; i++) {
			            		//alert(JSON.stringify(data));
			                   $("#wareHouse").html(data[i].wareHouse);
			                   $("#wareHouseVal").val(data[i].wareHouse);//用于调整申请传值
			                   $("#empNo").val(data[i].empNo);
			                   $("#ucode").val(data[i].ucode);
			                   $("#shopName").val(data[i].shopName);
			                   $("#shopTel").val(data[i].shopTel);
			                   $("#returnDate").val(data[i].returnDate);
			                   $("#orderNumber").val(data[i].orderNumber);
			                   
			                   $("#processInstanceId").val(data[i].processInstanceId);
			                   $("#returnId").val(data[i].id);
			                   $("#submitTimes").val(data[i].submitTimes);

			                   if(data[i].ucode.length == 11){
			                	   changeMallDiv();
			                	   $("#shopTels").html("无");
			                	   $("#returnReaon").val(data[i].returnReaon);  
				                   $("#shopPayStock").val(data[i].shopPayStock);  
				                   $("#rewardIntegralStock").val(data[i].rewardIntegralStock);  
				                   $("#personPayStock").val(data[i].personPayStock); 
				                  
				                   $("#bankDeposit").val(data[i].bankDeposit); 
				                   $("#accountName").val(data[i].accountName); 
				                   $("#accountNumber").val(data[i].accountNumber); 
				                   if(data[i].payType == "店支付"){
				                	   $("#shopPay").attr("checked",true);
				                   }
				                   if(data[i].payType == "个人支付"){
				                	   $("#personPay").attr("checked",true);
				                   }
				                   if(data[i].payType == "手续费减免"){
				                	   $("#derate").attr("checked",true);
				                	   $("#inputApplyCode").val(data[i].inputApplyCode); 
				                	   $("#inputApplyCode").removeAttr("style");
				                   }

				                   if(data[i].payType == "货款中扣除" || data[i].payType == "货款中扣除"){
				                	   $("#mallPay").attr("checked",true);
				                   }
			                   }else{
			                	   changeOaDiv();
			                	   $("#shopTels").html(data[i].shopTel);
			                	   $("#returnReaon").val(data[i].returnReaon);  
				                   $("#shopPayStock").val(data[i].shopPayStock);  
				                   $("#rewardIntegralStock").val(data[i].rewardIntegralStock);  
				                   $("#personPayStock").val(data[i].personPayStock); 
				                   $("#bankDeposit").val(data[i].bankDeposit); 
				                   $("#accountName").val(data[i].accountName); 
				                   $("#accountNumber").val(data[i].accountNumber); 
				                   if(data[i].payType == "店支付"){
				                	   $("#shopPay").attr("checked",true);
				                   }
				                   if(data[i].payType == "个人支付"){
				                	   $("#personPay").attr("checked",true);
				                   }
				                   if(data[i].payType == "手续费减免"){
				                	   $("#derate").attr("checked",true);
				                	   $("#inputApplyCode").val(data[i].inputApplyCode); 
				                	   $("#inputApplyCode").removeAttr("style");
				                	   //removeAttr("readonly");
				                   }
				                   if(data[i].payType == "货款中扣除" || data[i].payType == "货款中扣除"){
				                	   $("#mallPay").attr("checked",true);
				                   }
			                   }
			                   
			                    }
			            		$.getJSON("${tenantPrefix}/rs/Return/getReturnProductInfo",
			            				{id:id},function(data){
			            					//alert(JSON.stringify(data));
			            					console.log(data);
			            					var total = data.length;
						          			 if(total == 0){
						          				$("#pro").val("");
						          				alert("请检查专卖店编号/手机号与订单号是否匹配");
					                    	 }
			            					//遍历产品数据
			            					var str = "";
						                    for (var i = 0; i < data.length; i++) {
						                    	str += "<tr id='bor' name='productArray'>" + 
						                    	//产品名称
						                        "<td align='left' class='mytd'>"+
						                    	"<input type='hidden' name='proNo' value='" + data[i].proNo + "'>"+
						                    	"<input type='hidden' name='proName' value='" + data[i].proName + "'>"
						                        +data[i].proName+
						                        "</td>" +
												//店支付账户退货
						                        "<td align='left' class='mytd'><input id ='proNum" + data[i].proNo + "' type='hidden' name='shopPVNum' value='" + data[i].shopPVNum +"'>" +data[i].shopPVNum + "</td>" + //店支付产品数量
						                        "<td align='left' class='mytd colour-ee'><input class='colour-ee' id='shopReturn" + data[i].proNo + "' name='shopReturn' type='text' value='"+data[i].shopReNum+"' style='border:0px;text-align:left'" + ' onkeyup="getShopPV(' + "'" + data[i].proPV + "','"  + data[i].proNo + "','" + total +"')" + '"></td>' +
						                        "<td align='left' id='shopProPV" + data[i].proNo + "'>"+data[i].shopPV+"</td>" +
												//奖励积分账户退货
						                        "<td align='left' class='mytd'><input id ='proRewardNum" + data[i].proNo + "' type='hidden' name='shopRewardNum' value='"+data[i].shopRewardNum+"'>"+data[i].shopRewardNum + "</td>" +
						                        "<td align='left' class='mytd colour-ee'><input class='colour-ee' id='shopReward" + data[i].proNo + "' name='rewardReturn' value='"+data[i].shopRewNum+"' type='text' style='border:0px;text-align:left'" + ' onkeyup="getRewardPV(' + "'" + data[i].proPV + "','"  + data[i].proNo + "','"+ total + "')" + '"></td>' +
						                        "<td align='left' id='shopRewardPV"+ data[i].proNo+"'>"+data[i].shopRewardPV+"</td>" +
												//个人钱包账户退货
						                        "<td align='left' class='mytd'><input id ='proWalletNum" + data[i].proNo + "' type='hidden' name='shopWalletNum' value='"+data[i].shopWalletNum+"'>"+data[i].shopWalletNum + "</td>" +
						                        "<td align='left' class='mytd colour-ee'><input class='colour-ee' id='shopWallet" + data[i].proNo + "' name='walletReturn' value='"+data[i].shopwalNum+"' type='text' style='border:0px;text-align:left'" + ' onkeyup="getWalletPV(' + "'" + data[i].proPV + "','"  + data[i].proNo + "','" + total + "')" + '"></td>' +
						                        "<td align='left' id='shopWalletPV"+data[i].proNo+"' class='mytd'>"+data[i].shopWalletPV+"</td>" +
						                        
						                        "<input id='shopInputProPV" + data[i].proNo + "' name='shopProPV' type='hidden' value='"+data[i].shopPV+"'>" +
						                        "<input id='shopInputRewardPV"+ data[i].proNo+"' name='shopRewardPV' type='hidden' value='"+data[i].shopRewardPV+"' >" +
						                        "<input id='shopInputWalletPV"+data[i].proNo+"' name='shopWalletPV' type='hidden' value='"+data[i].shopWalletPV+"'>" +
						                        "<input id='proPV"+data[i].proNo+"' name='proPV' type='hidden' value='"+data[i].proPV+"'>" +
						                        "</tr>";

						                    }
						                    var tbody=window.document.getElementById("tbody-result");
						                    tbody.innerHTML = str;
			            				});
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

			setTimeout(function() {
				$('.datepicker').datepicker({
					autoclose: true,
					language: 'zh_CN',
					format: 'yyyy-mm-dd'
				})
			}, 500);
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



		ROOT_URL = '${tenantPrefix}';
		taskOperation = new TaskOperation();
		//完成任务
		function completeTask(flag) {
			var ucode = $("#ucode").val().replace(/(^\s*)|(\s*$)/g, "");
			if(flag == 3){
    			var i = 1;
    			var num = $("#submitTimes").attr('value');
    			var number =parseInt(num)+parseInt(i);
    			$("#submitTimes").val(number);
    		}
			if(flag == 3 && $("#orderNumber").val().length > "200"){
				alert("订单单据号过多");
				return
			}
			if(flag == 3 && $("#shopPayStock").val() == ""){
				$("#shopPayStock").val(0);
			}
			if(flag == 3 && $("#rewardIntegralStock").val() == ""){
				$("#rewardIntegralStock").val(0);
			}
			if(flag == 3 && $("#personPayStock").val() == ""){
				$("#personPayStock").val(0);
			}
			/* if(flag == 3&&$("#pro").val() == ""){
				alert("先获取订单信息");
				return
			} */
			var val=$('input:radio[name="payType"]:checked').val();
			if(flag == 3 && val != null){
				if(val != '手续费减免'){
	                $("#inputApplyCode").attr("value","");
	            }else{
	            	var applyCodeValue = $("#inputApplyCode").val();
	            	if(applyCodeValue == ''){
	            		alert("手续费减免，请输入受理单编号");
	    				return;
	            	}
	            }	
			}else{
				alert("手续费未选");
				return;
			}
			
			
			if(flag == 3&&ucode.length == 11 && $("#bankDeposit").val() == ''){
				alert("请填写开户行");
				return;
			}
			if(flag == 3&&ucode.length == 11 && $("#accountName").val() == ''){
				alert("请填写开户名");
				return;
			}
			if(flag == 3&&ucode.length == 11){
				if($("#accountNumber").val() == ''){
					alert("请填写账号");
					return;
				}else{
					var pattern = /^([1-9]{1})(\d{15}|\d{18})$/,
	                str = $("#accountNumber").val().replace(/\s+/g, "");
	                if (!pattern.test(str)) {
	                	alert("请填写正确的账号");
	                    return ;
	                }
				}
			}
			
			
			if(flag == 3&&ucode.length == 11 && !$('#confirmId').prop('checked')){
				alert("请确认商城是否已撤单");
				return;
			}
			
			if(flag == 4){
				var msg = "确定要撤销申请吗,请确认？";  
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
	    			
	    			$('#xform').attr('action', '${tenantPrefix}/Return/process-operationReturnApproval-completeTask.do?flag='+flag);
	    			$('#xform').submit();
	            },      
	            error: function(e){      
	            	loading.modal('hide');
	                alert("服务器请求失败,请重试"); 
	            }
	       });
		}


		$(function(){
			//开始时间
	        $("#pickerTime").datepicker({
	            //todayBtn : "linked",
	            format: "yyyy-mm-dd",
	            autoclose: true,
	            //todayHighlight : true,
	            language: "${locale}",
	            minView: 1,
	            pickerPosition: "bottom-left"
	        });
		})
		function MaxWords(obj){
		    	document.getElementById("recordNum").style.display = "block";
	            var text1 = document.getElementById("returnReaon").value;
	            var len;//记录已输入字数
	            if (text1.length >= 500) {
	                document.getElementById("returnReaon").value = text1.substr(0, 500);
	                len = 500;
	                $(obj).attr('value',len);
	            }
	            else {
	                len = text1.length;
	            }
	            var show = len + " / 500";
	            document.getElementById("recordNum").innerText = show;
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
		
		function changeMallDiv(){
   		 $("#form-tfoot").html('<tr id="xf-2-6">'
	 				 	+'<td id="xf-2-6-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="15%">'
					 	+'<div class="xf-handler">'
					 	+'<label style="display:block;text-align:center;margin-bottom:0px;"><font color="red">*</font>&nbsp;退货原因</label>'	
					 	+'</div>'
					 	+'</td>'
					 	+'<td id="xf-2-6-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left colour-ee" width="75%" colspan="9">'
					 	+'<div class="xf-handler">'
					 	+'<textarea class="colour-ee" id="returnReaon" name="returnReaon" rows="5" style="width:100%" maxlength="500" onkeyup="MaxWords(this)" onblur="MaxWords(this)"></textarea>'	
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
						+'<input type="text" id="shopPayStock" name="shopPayStock" style="width:100%;text-align:left" value="0" readonly>'	
						+'</div>'
						+'</td>'
						+'<td id="xf-2-9-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="17%" colspan="2">'
						+'<div class="xf-handler">'
						+'	<!-- <label id="reward_integral_stock"></label> -->'
						+'<input name="rewardIntegralStock" id="rewardIntegralStock" type="text" style="width:100%;text-align:left" value="0" readonly>'	
						+'</div>'
						+'</td>'
						+'<td id="xf-2-9-2" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="18%" colspan="2">'
						+'<div class="xf-handler">'
						+'	<!-- <label id="person_pay_stock"></label> -->'
						+'<input type="text" id="personPayStock" name="personPayStock" style="width:100%;text-align:left" value="0" readonly>'	
						+'</div>'
						+'</td>'
						+'<td id="xf-2-9-3" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="16%" colspan="2">'
						+'<div class="xf-handler">'
						+'<input type="radio" name="payType" value="手续费减免" id="derate">手续费减免'	
						+'<input class="colour-ee" type="text" id="inputApplyCode" name="inputApplyCode" style="display:none;" placeholder="请输入受理单号" maxlength="20">'	
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
						+'<td id="xf-3-2-1" class="xf-cell xf-cell-right xf-cell-bottom colour-ee" width="20%" colspan="3">'
						+'<div class="xf-handler">'
						+'	<input class="colour-ee" required id="bankDeposit" name="bankDeposit" style="width:100%;cursor:default;" type="text">'
						+'</div>'
						+'</td>'
						+'<td id="xf-3-2-2" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left colour-ee" width="20%" colspan="3">'
						+'<div class="xf-handler">'
						+'	<input class="colour-ee" type="text" id="accountName" name="accountName" style="width:100%;text-align:left">'
						+'</div>'
						+'</td>'
						+'<td id="xf-3-2-3" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left colour-ee" width="20%" colspan="3">'
						+'<div class="xf-handler">'
						+'	<input class="colour-ee" type="text" id="accountNumber" name="accountNumber" style="width:100%;text-align:left" maxlength="19" >'
						+'</div>'
						+'</td>'
						+'</tr>'
						+'<tr id="xf-3-3">'
						+'<td id="xf-3-3-1" class="xf-cell xf-cell-right xf-cell-bottom" width="100%" colspan="10">'
						+'<div class="xf-handler" style="display:block;text-align:center;margin-bottom:0px;">'
						+'<font color="red">*</font>&nbsp;	商城退货请确定是否已撤单<input type="checkbox" id="confirmId" checked>'
						+'</div>'
						+'</tr>');
   		 
   		 $("#xtable :radio").click(function(){
			   	var type = $(this).val();
			   	if(type == '手续费减免'){
			   		$("#inputApplyCode").show();
			   	}else{
			   		$("#inputApplyCode").hide();	
			   	}
			  });
   		 
   	 }
   	 function changeOaDiv(){
   		 $("#form-tfoot").html('<tr id="xf-2-6">'
   				 	+'<td id="xf-2-6-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="15%">'
   				 	+'<div class="xf-handler">'
   				 	+'<label style="display:block;text-align:center;margin-bottom:0px;"><font color="red">*</font>&nbsp;退货原因</label>'	
   				 	+'</div>'
   				 	+'</td>'
   				 	+'<td id="xf-2-6-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left colour-ee" width="75%" colspan="9">'
   				 	+'<div class="xf-handler">'
   				 	+'<textarea class="colour-ee" id="returnReaon" name="returnReaon" rows="5" style="width:100%" maxlength="500" onkeyup="MaxWords(this)" onblur="MaxWords(this)"></textarea>'	
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
						+'<input type="text" id="shopPayStock" name="shopPayStock" style="width:100%;text-align:left" value="0" readonly>'	
						+'</div>'
						+'</td>'
						+'<td id="xf-2-9-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="17%" colspan="2">'
						+'<div class="xf-handler">'
						+'	<!-- <label id="reward_integral_stock"></label> -->'
						+'<input name="rewardIntegralStock" id="rewardIntegralStock" type="text" style="width:100%;text-align:left" value="0" readonly>'	
						+'</div>'
						+'</td>'
						+'<td id="xf-2-9-2" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="18%" colspan="2">'
						+'<div class="xf-handler">'
						+'	<!-- <label id="person_pay_stock"></label> -->'
						+'<input type="text" id="personPayStock" name="personPayStock" style="width:100%;text-align:left" value="0" readonly>'	
						+'</div>'
						+'</td>'
						+'<td id="xf-2-9-3" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="16%" colspan="2">'
						+'<div class="xf-handler">'
						+'<input type="radio" name="payType" value="手续费减免" id="derate">手续费减免'	
						+'<input class="colour-ee"  type="text" id="inputApplyCode" name="inputApplyCode" style="display:none;" placeholder="请输入受理单号" maxlength="20">'	
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
						+'<td id="xf-3-2-1" class="xf-cell xf-cell-right xf-cell-bottom colour-ee" width="20%" colspan="3">'
						+'<div class="xf-handler">'
						+'	<input placeholder="不可输入" class="colour-ee" readonly id="bankDeposit" name="bankDeposit" style="width:100%;cursor:default;" type="text">'
						+'</div>'
						+'</td>'
						+'<td id="xf-3-2-2" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left colour-ee" width="20%" colspan="3">'
						+'<div class="xf-handler">'
						+'	<input placeholder="不可输入" class="colour-ee" readonly type="text" id="accountName" name="accountName" style="width:100%;text-align:left">'
						+'</div>'
						+'</td>'
						+'<td id="xf-3-2-3" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left colour-ee" width="20%" colspan="3">'
						+'<div class="xf-handler">'
						+'	<input placeholder="不可输入" class="colour-ee" readonly type="text" id="accountNumber" name="accountNumber" style="width:100%;text-align:left">'
						+'</div>'
						+'</td>'
						+'</tr>');
   		 
   		 $("#xtable :radio").click(function(){
			   	var type = $(this).val();
			   	if(type == '手续费减免'){
			   		$("#inputApplyCode").show();
			   	}else{
			   		$("#inputApplyCode").hide();	
			   	}
			  });
   	 }
		
    </script>
  </head>

  <body>
    <%@include file="/header/bpm-workspace3.jsp"%>
<form id="xform" method="post" action="${tenantPrefix}/Return/process-operationReturnApproval-completeTask.do" class="xf-form" enctype="multipart/form-data" style="AutoScrool:true">
    <div class="container">

	  <!-- start of main -->
      <section id="m-main" class="col-md-12" style="padding-top:65px;">
      	<%-- <input id="processDefinitionId" type="hidden" name="processDefinitionId" value="<%= request.getParameter("processDefinitionId")%>">
      	<input id="bpmProcessId" type="hidden" name="bpmProcessId" value="<%= request.getParameter("bpmProcessId")%>">
      	<input id="businessKey" type="hidden" name="businessKey" value="<%= request.getParameter("businessKey")%>"> --%>
		<input id="processInstanceId" type="hidden" name="processInstanceId" value="${processInstanceId}">
		<input id="humanTaskId" type="hidden"  name="humanTaskId" value="${humanTaskId}">
		<input id="activityId" type="hidden" name="activityId" value="">
		<input id="returnId" name="returnId" type="hidden">
		<input id="pro"  type="hidden">
		<input id="userId" type="hidden" name="userId" value="<%=userId %>">
		<input style="border:0px;width:10px" readonly type="hidden" id="submitTimes" name="submitTimes">
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
			  isWhole:true
		  }, function(data) {
			  $('#nextStep').append('&nbsp;');
			  for (var i = 0; i < data.length; i++) {
				  $('#nextStep').append(data[i].name);
				  // $('#activityId').val(data[i].id);
			  }
		  });
		</script> --%>


		<%-- <input id="processDefinitionId" type="hidden" name="processDefinitionId" value="<%= request.getParameter("processDefinitionId")%>">
		<input id="bpmProcessId" type="hidden" name="bpmProcessId" value="<%= request.getParameter("bpmProcessId")%>">
		<input id="autoCompleteFirstTask" type="hidden" name="autoCompleteFirstTask" value="false">
		<input id="businessKey" type="hidden" name="businessKey" value="<%= request.getParameter("businessKey")%>">
		<input id="activityId" type="hidden" name="activityId" value=""> --%>

		<div id="xf-form-table">
			<div id="xf-1" class="xf-section">
				<h1 style="text-align:center;">退货调整单</h1>
			</div>

			<div id="xf-2" class="xf-section">
				<table id="xtable" class="xf-table" width="100%" cellspacing="0" cellpadding="0" border="0" align="center">
					<tbody>
						<tr id="xf-2-0">
							<td id="xf-2-0-0" style="text-align:center;" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="50%" colspan="5">
								<div class="xf-handler">
									<label>所属仓库   &nbsp 
										<select>
										<option id="wareHouse" value=""></option>
										<input type="hidden" id="wareHouseVal" name="wareHouse">
										</select>
									</label>
									
								</div>
							</td>
							<td id="xf-2-0-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left"  width="50%" colspan="5">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">客服工号:<input type="text" id="empNo" name="empNo" style="border:0px" readonly></label>
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
								<div class="xf-handler" align="left">
									<input type="text" id="ucode" name="ucode" style="width:100%;text-align:left" readonly>
								</div>
							</td>
							<td id="xf-2-2-1" class="xf-cell xf-cell-right xf-cell-bottom" width="35%" colspan="4">
								<div class="xf-handler" align="left">
									<input type="text" id="shopName" name="shopName" style="width:100%" readonly>
								</div>
							</td>
							<td id="xf-2-2-2" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left" width="15%">
								<div class="xf-handler" align="left" id="shopTels">
									
								</div>
								<input type="hidden" id="shopTel" name="shopTel" style="width:100%;text-align:left" readonly>
							</td>
							<td id="xf-2-2-3" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left colour-ee" width="35%" colspan="4">
								<div id="pickerTime" class="input-group  datepickerend date">
								    <input class="colour-ee" type="text" name="returnDate" style="width:350px;" id="returnDate" required>
								    <span class="input-group-addon" style="padding-right:10px">
										<i class="glyphicon glyphicon-calendar"></i>
									</span>
							    </div>
							</td>
						</tr>
						<tr id="xf-2-3">
							<td id="xf-2-3-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left" width="15%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">订单单据号</label>
								</div>
							</td>
							<td id="xf-2-3-1" class="xf-cell xf-cell-right xf-cell-bottom colour-ee" width="75%" colspan="9">
								<div class="xf-handler">
									<textarea class="colour-ee" rows="2" id="orderNumber" name="orderNumber" style="width:100%" maxlength="200"></textarea>
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
						<tbody id="tbody-result">
							<tr></tr>
						</tbody>
						<tfoot id="form-tfoot">
						<tr id="xf-2-6">
							<td id="xf-2-6-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="15%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">退货原因</label>
								</div>
							</td>
							<td id="xf-2-6-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left colour-ee"  width="75%" colspan="9">
								<div class="xf-handler">
									<textarea class="colour-ee" id="returnReaon" name="returnReaon" rows="5" style="width:100%" maxlength="500" onkeyup="MaxWords(this)" onblur="MaxWords(this)"></textarea>
									<label id="recordNum" style="display: none;text-align:right;" class="control-label col-md-12"></label>
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
								<div class="xf-handler" align="center">
									<input type="radio" id="shopPay" name="payType" value="店支付">50元
								</div>
							</td>
						</tr>
						<tr id="xf-2-8">
							<td id="xf-2-8-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="16%" colspan="2">
								<div class="xf-handler" align="left">
									<input type="text" id="shopPayStock" name="shopPayStock" style="width:100%;text-align:left" readonly>
								</div>
							</td>
							<td id="xf-2-8-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="17%" colspan="2">
								<div class="xf-handler" align="left">
									<input type="text" id="rewardIntegralStock" name="rewardIntegralStock" style="width:100%;text-align:left" readonly>
								</div>
							</td>
							<td id="xf-2-8-2" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="18%" colspan="2">
								<div class="xf-handler" align="left">
									<input type="text" id="personPayStock" name="personPayStock" style="width:100%;text-align:left" readonly>
								</div>
							</td>
							<td id="xf-2-8-3" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="8%">
								<div class="xf-handler" align="center" id="personPayStock">
									<label>个人支付</label>
								</div>
							</td>
							<td id="xf-2-8-4" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="8%">
								<div class="xf-handler" align="center" id="personPayStock">
									<input type="radio" id="personPay" name="payType" value="个人支付">50元
								</div>
							</td>
						</tr>
						</tfoot>
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
		<br/><br/><br/><br/>
    </section>
	<!-- end of main -->
  </div>
  <br/><br/><br/>
  <div class="navbar navbar-default navbar-fixed-bottom">
    <div class="container-fluid">
      <div class="text-center" style="padding-top:8px;">
	    <div class="text-center" style="padding-top:8px;">
			<!-- <button id="saveDraft" class="btn btn-default" type="button" onclick="taskOperation.saveDraft()">保存草稿</button> -->
			<!-- <button id="confirmStartProcess" class="btn btn-default" type="button" onclick="taskOperation.taskConf()">配置任务</button> -->
			<button id="adjustment" class="btn btn-default" type="button" onclick="completeTask(3)">调整申请</button>
			<button id="revoke" class="btn btn-default" type="button" onclick="completeTask(4)">撤销申请</button>
			<button id="endProcess" type="button" class="btn btn-default" onclick="confirmOperation()">撤销申请</button>
			<button id="get" class="btn btn-default" type="button" onclick="getOrderInfo()">获取订单信息</button>
			<button type="button" class="btn btn-default" onclick="javascript:history.back();">返回</button>
		</div>
	
	  </div>
    </div>
  </div>
</form>
</body>

</html>
