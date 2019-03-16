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
		/* table{ border-collapse:collapse } */ 
		.xf-form td{border:1px solid gray}
		#form-thead td{border:1px solid gray}
		#form-tfoot td{border:1px solid gray}
		input{border : 1px solid #F2F2F2;height:25px}
		select{border : 1px solid #F2F2F2}
		textarea{border : 1px solid #F2F2F2}
		#xtable td{border: 1px solid #B5B5B5}
	</style>

	<script type="text/javascript">
		document.onmousedown = function(e) {};
		document.onmousemove = function(e) {};
		document.onmouseup = function(e) {};
		document.ondblclick = function(e) {};

		var xform;
		function getwareHouse(){
			var  myselect=document.getElementById("wareHouse");
    		var index=myselect.selectedIndex ;  
    		var bt=myselect.options[index].value;
    		var t=myselect.options[index].text;
    		$("#wareHouseVal").val(t);
		}
		$(function(){
			  $("#xtable :radio").click(function(){
			   	var type = $(this).val();
			   	if(type == '手续费减免'){
			   		$("#inputApplyCode").show();
			   	}else{
			   		$("#inputApplyCode").hide();	
			   	}
			  });
  	 })
		function getOrderInfo (){
			var loading = bootbox.dialog({
                message: '<p style="width:90%;margin:0 auto;text-align:center;">获取中...</p>',
                size: 'small',
                closeButton: false
         	});
			
			var ucode = $("#ucode").val().replace(/(^\s*)|(\s*$)/g, "");
			var orderNumber = $("#orderNumber").val().replace(/(^\s*)|(\s*$)/g, "");
			
			if (ucode == '') {
                alert("请输入专卖店编号/手机号");
                loading.modal('hide');
                return;
            }
			if (orderNumber == '') {
                alert("请输入订单单据号！以逗号来分隔，例如：OC15020500001,OC15020500002");
                loading.modal('hide');
                return;
            }
			$("#result-tbody").find("tr").remove();
			$("#result-tbody").append("<tr></tr>");
			//$.weeboxs.open('正在获取数据，请稍等...', { title: '提示', type: 'alert', showButton: false });
			
			
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
					//手续费变化
					changeMallDiv();
					$("#shopTel").attr("placeholder","不可输入");
					$("#shopTel").attr("value","");
					$("#shopName").attr("value","");
					$("#shopName").removeAttr("readonly");
					$("#bankDeposit").removeAttr("readonly");
					$("#accountName").removeAttr("readonly");
					$("#accountNumber").removeAttr("readonly");
					$.getJSON('${tenantPrefix}/workOperationCustom/getDict.do', {
						dictName:"fixedCode"
					},function(data){
						if(data.length == 0){
	               		 alert("数据字典未配置固定经销商编号");
						}
						var userCode = data;
						//根据固定专卖店编号获取仓库信息
						 $.getJSON('${tenantPrefix}/rs/varUser/storeInfo', {
					                  customerInfoId: userCode
					                  }, function(data) {
				                    	 if(data.length == 0){
				                    		 alert("不存在此店信息");
				                    	 }
				                    	 var strHtml = "";
				                    	 var choose = "<option value='请选择'>请选择</option>";
				                         for (var i = 0; i < data.length; i++) {
				                        	 if(i == 0){
				                        		 strHtml += choose;
				                        	 }
				                             strHtml += "<option value='" + data[i].varStoCode + "'>" + data[i].nvrStoName + "</option>"
				                         }
				                         $("#wareHouse").html(strHtml);
				                         //getwareHouse();
				                         //根据专卖店编号和单据号获取退货信息
				                         $.getJSON('${tenantPrefix}/rs/varUser/mallOrderInfo', {
				                             customerInfoId: ucode,
				                             orderInfoId : orderNumber

				                             }, function(data) {
				             	              	 if(data.length == 0){
				             	              		 alert("请检查专卖店编号/手机号与订单号是否匹配");
				             	              	 }
				                     			 var total = data.length;
				                     			 var str = "";
				                               //遍历产品数据
				                               for (var i = 0; i < data.length; i++) {
				                            	   //格式化商城的产品单价pv
				                            	   var proPV = returnFloat(data[i].proPV);
				                               	str += "<tr id='bor' name='productArray'>" +  
				                                   "<td align='left' class='mytd'><input type='hidden' name='proName' value='" + data[i].proName + "'>" +
				                                   "<input id='proNo" + data[i].proNo + "' type='hidden' name='proNo' value='" + data[i].proNo + "'>" +data[i].proName+ "</td>" +
				                                   
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
				                               var tbody=window.document.getElementById("result-tbody");
				                               tbody.innerHTML = str;
			               			});
					      });
					});
					
					
					
				}else{
					//专卖店
					changeOaDiv();
					
					$("#shopName").attr("readonly","readonly");
					$("#bankDeposit").attr("readonly","readonly");
					$("#accountName").attr("readonly","readonly");
					$("#accountNumber").attr("readonly","readonly");
					//根据专卖店编号获取仓库信息
					 $.getJSON('${tenantPrefix}/rs/varUser/storeInfo', {
				                  customerInfoId: ucode
				                  }, function(data) {
			                    	 if(data.length == 0){
			                    		 alert("不存在此店信息");
			                    	 }
			                    	 var strHtml = "";
			                    	 var choose = "<option value='请选择'>请选择</option>";
			                         for (var i = 0; i < data.length; i++) {
			                        	 if(i == 0){
			                        		 strHtml += choose;
			                        	 }
			                             strHtml += "<option value='" + data[i].varStoCode + "'>" + data[i].nvrStoName + "</option>"
			                         }
			                         $("#wareHouse").html(strHtml);
			                         //getwareHouse();
			                         //根据专卖店编号和单据号获取退货信息
			                         $.getJSON('${tenantPrefix}/rs/varUser/orderInfo', {
			                             customerInfoId: ucode,
			                             orderInfoId : orderNumber

			                             }, function(data) {
			             	              	 //alert(JSON.stringify(data));
			             	              	 console.log(data);
			             	              	 if(data.length == 0){
			             	              		 alert("请检查专卖店编号/手机号与订单号是否匹配");
			             	              	 }
			                     			 $("#shopName").val(data[0].shopName);
			                     			 $("#shopTel").val(data[0].shopTel);
			                     			 var total = data.length;
			                     			 var str = "";
			                               //遍历产品数据
			                               for (var i = 0; i < data.length; i++) {
			                               	str += "<tr id='bor' name='productArray'>" +  
			                                   "<td align='left' class='mytd '>"+
			                               	   "<input type='hidden' name='proName' value='" + data[i].proName + "'>" +
			                                   "<input id='proNo" + data[i].proNo + "' type='hidden' name='proNo' value='"+ data[i].proNo +"'>" +data[i].proName+ "</td>" +
			                                   
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
			                               var tbody=window.document.getElementById("result-tbody");
			                               tbody.innerHTML = str;
		               			});
				          			
				      });
				}
				
				
				
				
			});
			 loading.modal('hide');
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
			//var money = $("#proPV" + proNo).val();
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
			//alert(proPv);
			var maxnum = $("#proRewardNum" + proNo).val();
			var num = $("#shopReward" + proNo).val();
			if(num == ""){
				$("#shopReward" + proNo).val(0);
				num = 0;
			}
			//var money = $("#proPV" + proNo).val();
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
			//var money = $("#proPV" + proNo).val();
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
		
		
		
		
		$(function() {
			//获取大区
			$.getJSON('${tenantPrefix}/rs/party/AreaName',{userId : $("#userId").val()},function(data){
				if (data[0] != null) {
		    		$("#areaId").val(data[0].id);
		    		$("#areaName").val(data[0].name);
				}
	    	})
	    	
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
			//根据userId获取用户工号
			$.getJSON('${tenantPrefix}/rs/user/getEmpNo',{userId : $("#userId").val()},function(data){
				$("#empNo").val(data.employeeNo);
			})
		})
		
		ROOT_URL = '${tenantPrefix}';
		taskOperation = new TaskOperation();
		var conf={
				applyCodeId:"applyCode",				//受理单号input的ID
			    submitBtnId:"confirmStartProcess",		//提交按钮ID	
				checkApplyCodeUrl:"${tenantPrefix}/rs/business/applyCodeIfExist",	//验证受理单号url
       			checkUrl:"${tenantPrefix}/rs/customer/opteraion-getposition",		//获取岗位url
       			actionUrl:"${tenantPrefix}/Return/process-operationReturn-startProcessInstance.do",//提交URL
       			businessDetailId:"businessDetailId",			//存储业务明细input的ID
       			formId:"xform",									//form的ID
       			selectAreaId:"area",
   	   			selectCompanyId:"branchOffice",
   	 			iptAreaId:"areaId",
   	 			iptAreaName:"areaName",
   	   	   		iptCompanyId:"companyId",
   	   	   		iptCompanyName:"companyName"
       		}
		$(function(){
			checkPostion(conf);
		})
		function startProcessInstance() {
			var ucode = $("#ucode").val().replace(/(^\s*)|(\s*$)/g, "");
			
			var val=$('input:radio[name="payType"]:checked').val();
			
			if(val != null){
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
			 
			if($("#ucode").val().replace(/(^\s*)|(\s*$)/g, "") == ''){
				alert("专卖店编号/手机号不能为空");
				 return;
			}
			if($("#orderNumber").val().replace(/(^\s*)|(\s*$)/g, "") == ''){
				alert("订单单据号不能为空");
				 return;
			}
			if($("#orderNumber").val().length > '200'){
				alert("订单单据号过多");
				 return;
			}
			if($("#returnDate").val() == ''){
				alert("退货日期不能为空");
				 return;
			}
			if($("#returnReaon").val().replace(/(^\s*)|(\s*$)/g, "") == ''){
				alert("退货原因不能为空");
				 return;
			}
			if(document.getElementById("outOrder") == 'out'){
				alert("存在过期单据号！");
				return
			}
			if(ucode.length == 11){
				if($("#shopName").val() == ''){
					alert("请输入专卖店姓名");
					return
				}
			}else{
				if($("#shopName").val() == ''||$("#shopTel").val() == ''){
					alert("请核对专卖店编号和订单号");
					return
				}
			}
			
			if($("#wareHouse").val() == '请选择'){
				alert("请选择仓库");
				return
			}
			
			
			if( ($("#shopPayStock").val() == '0'||$("#shopPayStock").val() =='0.00')&&($("#rewardIntegralStock").val() == '0' ||$("#rewardIntegralStock").val() == '0.00')&&($("#personPayStock").val() == '0' || $("#personPayStock").val() == '0.00') ){
				alert("请填写退货数量");
				return;
			}
			if(ucode.length == 11 && $("#bankDeposit").val() == ''){
				alert("请填写开户行");
				return;
			}
			if(ucode.length == 11 && $("#accountName").val() == ''){
				alert("请填写开户名");
				return;
			}
			if(ucode.length == 11 ){
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
			
			
			if(ucode.length == 11 && !$('#confirmId').prop('checked')){
			   alert("请确认商城是否已撤单");
			   return;
			}
			fnFormSubmit(conf);
		}
	
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
		
    	 function changeMallDiv(){
    		 $("#form-tfoot").html('<tr id="xf-2-7">'
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
						+'<td id="xf-2-9-3" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left " width="16%" colspan="2">'
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
						+'	<input class="colour-ee" type="text" id="accountNumber" name="accountNumber" style="width:100%;text-align:left"  maxlength="19" >'  //'/^([1-9]{1})(\d{15}|\d{18})$/'
						+'</div>'
						+'</td>'
						+'</tr>'
						+'<tr id="xf-3-3">'
						+'<td id="xf-3-3-1" class="xf-cell xf-cell-right xf-cell-bottom" width="100%" colspan="10">'
						+'<div class="xf-handler" style="display:block;text-align:center;margin-bottom:0px;">'
						+'<font color="red">*</font>&nbsp;	商城退货请确定是否已撤单<input type="checkbox" id="confirmId">'
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
    		 $("#form-tfoot").html('<tr id="xf-2-7">'
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
						+'<td id="xf-2-9-3" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left " width="16%" colspan="2">'
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

<form id="xform" method="post" action="${tenantPrefix}/Return/process-operationReturn-startProcessInstance.do" class="xf-form" enctype="multipart/form-data" style="AutoScrool:true">
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
		<input id="bpmProcessId" type="hidden" name="bpmProcessId" value="${bpmProcessId}">
		<input id="businessDetailId" type="hidden" name="businessDetailId" value="${businessDetailId}">
		<input id="autoCompleteFirstTask" type="hidden" name="autoCompleteFirstTask" value="false">
		<input id="businessKey" type="hidden" name="businessKey" value="${businessKey}">
		<input id="userId" type="hidden" name="userId" value="${userId}">
		<%-- <input id="assignee" type="hidden" name="assignee" value="<%= request.getParameter("assignee")%>">
		<input id="completeTime" type="hidden" name="completeTime" value="<%= request.getParameter("completeTime")%>"> --%>
		<input id="activityId" type="hidden" name="activityId" value="">
		<input id="url" type="hidden" name="url" value="/Return/from-detail.do">
		<input type="hidden" id="applyCode" name="applyCode" value="${code}">
		<input id="outOrder" type="hidden">
		<input id="areaId" type="hidden" name="areaId">
		<input id="areaName" type="hidden" name="areaName">
		<input id="companyId" type="hidden" name="companyId">
		<input id="companyName" type="hidden" name="companyName">
		<input style="border:0px;width:10px" readonly type="hidden" id="submitTimes" name="submitTimes" value="0">
		<div id="xf-form-table">

			<div id="xf-1" class="xf-section">
				<h1 style="text-align:center;">退货申请单</h1>
			</div>
			<font size="4" >注意：</font><br/>
			<font color="red">
			1.此流程表单默认为原始表单，如果为商城表单则，点击“获取订单信息”按钮后，表单内容则变为商城表单<br/>
			2.商城表单判断规则为：专卖店编号/手机号填写为11位数字的手机号
			</font>
			<div id="xf-2" class="xf-section">
				<table id="xtable" class="xf-table" style="width:100%" cellspacing="0" cellpadding="0" border="1" align="center">
					<thead id="form-thead">
						<tr id="xf-2-0">
							<td id="xf-2-0-0" style="text-align:center;" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="50%" colspan="5">
								<div class="xf-handler">
									<label>所属仓库 &nbsp 
										<select id="wareHouse" onchange="getwareHouse()">
											<option value="请选择">请选择</option>
											<input type="hidden" id="wareHouseVal" name="wareHouse">
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
									<label style="display:block;text-align:center;margin-bottom:0px;"><font color='red'>*</font>&nbsp;专卖店编号/手机号</label>
								</div>
							</td>
							<td id="xf-2-1-1" class="xf-cell xf-cell-right xf-cell-bottom"  width="35%" colspan="4">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;"><font color='red'>*</font>专卖店姓名</label>
								</div>
							</td>
							<td id="xf-2-1-2" class="xf-cell xf-cell-right xf-cell-bottom"  width="15%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">专卖店电话</label>
								</div>
							</td>
							<td id="xf-2-1-3" class="xf-cell xf-cell-right xf-cell-bottom"  width="35%" colspan="4">
								<div class="xf-handler" >
									<label style="display:block;text-align:center;margin-bottom:0px;"><font color='red'>*</font>&nbsp;申请退货日期</label>
								</div>
							</td>
						</tr>
						<tr id="xf-2-2">
							<td id="xf-2-2-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left colour-ee" width="15%">
								<div class="xf-handler">
									<input class="colour-ee" type="text" name="ucode" style="width:100%;text-align:left" id="ucode" maxlength="11" onkeyup="var reg = /^[0-9]\d*$/; if(!reg.test(this.value)) this.value = ''; " required>
								</div>
							</td>
							<td id="xf-2-2-1" class="xf-cell xf-cell-right xf-cell-bottom colour-ee" width="35%" colspan="4">
								<div class="xf-handler">
									<input readonly class="colour-ee" required id="shopName" name="shopName" style="width:100%;cursor:default;" type="text">
								</div>
							</td>
							<td id="xf-2-2-2" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left colour-ee" width="15%">
								<div class="xf-handler">
									<input class="colour-ee" readonly type="text" id="shopTel" name="shopTel" style="width:100%;text-align:left">
								</div>
							</td>
							<td id="xf-2-2-3" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left colour-ee" width="35%" colspan="4">
								<div id="pickerTime" class="input-group  datepickerend date">
								    <input class="colour-ee"  type="text" name="returnDate" style="width:350px;" id="returnDate" required>
								    <span id="calendar" class="input-group-addon">
										<i class="glyphicon glyphicon-calendar"></i>
									</span>
							    </div>
								
							</td>
						</tr>
						<tr id="xf-2-3">
							<td id="xf-2-3-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left" width="15%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;"><font color='red'>*</font>&nbsp;订单单据号</label>
								</div>
							</td>
							<td id="xf-2-3-1" class="xf-cell xf-cell-right xf-cell-bottom colour-ee" width="75%" colspan="9">
								<div class="xf-handler">
									<textarea class="colour-ee" rows="2" id="orderNumber" name="orderNumber" style="width:100%" maxlength="200"></textarea>
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
						</thead>
						<tbody id="result-tbody">
							<tr></tr>
						</tbody>
						
						<tr id="xf-2-6">
							<td id="xf-2-6-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="15%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;"><font color='red'>*</font>&nbsp;退货原因</label>
								</div>
							</td>
							<td id="xf-2-6-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left colour-ee" width="75%" colspan="9">
								<div class="xf-handler">
									<textarea class="colour-ee" id="returnReaon" name="returnReaon" rows="5" style="width:100%" maxlength="500" onkeyup="MaxWords(this)" onblur="MaxWords(this)"></textarea>
									<label id="recordNum" style="display: none;text-align:right;" class="control-label col-md-12"></label>
								</div>
							</td>
						</tr>
						<tfoot id="form-tfoot">
						<tr id="xf-2-7">
							<td id="xf-2-7-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="15%" rowspan="3">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">专卖店库存</label>
								</div>
							</td>
							<td id="xf-2-7-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="16%" colspan="2" rowspan="2">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">店支付库存</label>
								</div>
							</td>
							<td id="xf-2-7-2" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="17%" colspan="2" rowspan="2">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">奖励积分库存</label>
								</div>
							</td>
							<td id="xf-2-7-3" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="18%" colspan="2" rowspan="2">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">个人钱包库存</label>
								</div>
							</td>
							<td id="xf-2-7-4" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="8%" rowspan="3">
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
							<!-- <td id="xf-2-8-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="16%" colspan="2">
								<div class="xf-handler">
									<label id="shop_pay_stock"></label>
									<input type="text" id="shopPayStock" name="shopPayStock" style="width:100%;text-align:left" value="0" readonly>
								</div>
							</td>
							<td id="xf-2-8-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="17%" colspan="2">
								<div class="xf-handler">
									<label id="reward_integral_stock"></label>
									<input name="rewardIntegralStock" id="rewardIntegralStock" type="text" style="width:100%;text-align:left" value="0" readonly>
								</div>
							</td>
							<td id="xf-2-8-2" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="18%" colspan="2">
								<div class="xf-handler">
									<label id="person_pay_stock"></label>
									<input type="text" id="personPayStock" name="personPayStock" style="width:100%;text-align:left" value="0" readonly>
								</div>
							</td> -->
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
						<tr id="xf-2-9">
							<td id="xf-2-9-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="16%" colspan="2">
								<div class="xf-handler">
									<!-- <label id="shop_pay_stock"></label> -->
									<input type="text" id="shopPayStock" name="shopPayStock" style="width:100%;text-align:left" value="0" readonly>
								</div>
							</td>
							<td id="xf-2-9-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="17%" colspan="2">
								<div class="xf-handler">
									<!-- <label id="reward_integral_stock"></label> -->
									<input name="rewardIntegralStock" id="rewardIntegralStock" type="text" style="width:100%;text-align:left" value="0" readonly>
								</div>
							</td>
							<td id="xf-2-9-2" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="18%" colspan="2">
								<div class="xf-handler">
									<!-- <label id="person_pay_stock"></label> -->
									<input type="text" id="personPayStock" name="personPayStock" style="width:100%;text-align:left" value="0" readonly>
								</div>
							</td>
							<td id="xf-2-9-3" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="16%" colspan="2">
								<div class="xf-handler">
									<input type="radio" name="payType" value="手续费减免" id="derate">手续费减免
									<input type="text" id="inputApplyCode" name="inputApplyCode" style="display:none;" placeholder="请输入受理单号" maxlength="20">
								</div>
							</td>
						</tr>
					
						<tr id="xf-3-1">
							<td id="xf-3-1-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left" width="15%" rowspan="2">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;"><font color='red'>*</font>&nbsp;退款账户信息</label>
								</div>
							</td>
							<td id="xf-3-1-1" class="xf-cell xf-cell-right xf-cell-bottom"  width="35%" colspan="4">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">开户行(含支行)</label>
								</div>
							</td>
							<td id="xf-3-1-2" class="xf-cell xf-cell-right xf-cell-bottom"  width="20%" colspan="2">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">开户名</label>
								</div>
							</td>
							<td id="xf-3-1-3" class="xf-cell xf-cell-right xf-cell-bottom"  width="20%" colspan="3">
								<div class="xf-handler" >
									<label style="display:block;text-align:center;margin-bottom:0px;">账号</label>
								</div>
							</td>
						</tr>
						<tr id="xf-3-2">
							<td id="xf-3-2-1" class="xf-cell xf-cell-right xf-cell-bottom colour-ee" width="35%" colspan="4">
								<div class="xf-handler">
									<input class="colour-ee" readonly id="bankDeposit" name="bankDeposit" style="width:100%;cursor:default;" type="text">
								</div>
							</td>
							<td id="xf-3-2-2" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left colour-ee" width="20%" colspan="2">
								<div class="xf-handler">
									<input class="colour-ee" readonly type="text" id="accountName" name="accountName" style="width:100%;text-align:left">
								</div>
							</td>
							<td id="xf-3-2-3" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left colour-ee" width="20%" colspan="3">
								<div class="xf-handler">
									<input class="colour-ee" readonly type="text" id="accountNumber" name="accountNumber" style="width:100%;text-align:left">
								</div>
							</td>
						</tr>
					</tfoot>
				</table>
			</div>
		</div>
		<br/><br/><br/><br/>
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
			<button id="get" class="btn btn-default" type="button" onclick="getOrderInfo()">获取订单信息</button>
			<button type="button" class="btn btn-default" onclick="javascript:history.back();">返回</button>
		</div>
	
	  </div>
    </div>
  </div>
  <%@include file="/common/selectPosition.jsp" %>
</form>
</body>
<script>
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
        var calendar = document.getElementById("calendar");
        calendar.style.position = "absolute";
        calendar.style.left = "350px";
        calendar.style.top = "-2px";
	})
</script>
</html>
 <%-- /*$("#ucode").keydown(function(event) {    
						if (event.keyCode == 13) {  
							var id = $("#ucode").val();
							//alert(id);
		                	if (id !="") {
			                    $.getJSON('${tenantPrefix}/rs/varUser/userInfo', {
			                    	customerInfoId: id
			                    }, function(data) {
			                    	alert(JSON.stringify(data)); 
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
		            */ --%>