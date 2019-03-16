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
	<script type="text/javascript" src="${cdnPrefix}/float/FloatOperate.js"></script><!-- 浮点数计算 -->
	<!-- bootstrap -->
<%-- 	<script type="text/javascript" src="${cdnPrefix}/bootstrap/js/bootstrap.min.js"></script>
	<link href="${cdnPrefix}/bootstrap/css/bootstrap.min.css" rel="stylesheet">
	<!-- bootstrap-select -->
    <!-- <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap-select/1.12.4/css/bootstrap-select.min.css"> -->
    <script src="https://cdnjs.cloudflare.com/ajax/libs/bootstrap-select/1.12.4/js/bootstrap-select.min.js"></script>
	 --%>
	<style type="text/css">
		.xf-handler {
			cursor: auto;
			margin:0;
		}
		/* table{ border-collapse:collapse } */ 
		.xf-form td{border:1px solid gray}
		#form-thead td{border:1px solid gray}
		#form-tfoot td{border:1px solid gray}
		input{border : 1px solid #F2F2F2;}
		select{border : 1px solid #F2F2F2}
		textarea{border : 1px solid #F2F2F2}
		#xtable td{border: 1px solid #B5B5B5;line-height:35px;height:35px;}
		#xtable{border: 1px solid #B5B5B5;}
		#xtable td .xf-handler {
			cursor: auto;
			margin:0;
		}
		#xtable td input[type='text']{height:35px;width:100%;background:#eee;}
	</style>

	<script type="text/javascript">
		var proNoStr = "";
		var serialStr = "";
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
			var now = new Date();
			var year = now.getFullYear();
			var month = now.getMonth();
			month = parseInt(month)+parseInt(1)
			if(month < 10){
				month = "0"+month;
			}
			var day = now.getDate();
			if(day < 10){
				day = "0"+day;
			}
			$("#exchangeDate").val(year+"-"+month+"-"+day);
			$("#lbl_exchangeDate").html(year+"-"+month+"-"+day);
			
		})
		
		
		function getOrderInfo (){
			var loading = bootbox.dialog({
                message: '<p style="width:90%;margin:0 auto;text-align:center;">获取中...</p>',
                size: 'small',
                closeButton: false
         	});
			var ucode = $("#ucode").val().replace(/(^\s*)|(\s*$)/g, "");
			var orderNumber = $("#orderNumber").val().replace(/(^\s*)|(\s*$)/g, "");
			if(ucode==""){
				alert("请输入专卖店编号/手机号");
				loading.modal('hide');
				return;
			}
			
			if (orderNumber=="") {
                alert("请输入订单单据号！以逗号来分隔，例如：OC15020500001,OC15020500002");
                loading.modal('hide');
                return;
            }
			
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
				
				if(ucode.length == 11){
					//商城
					changeMallDiv();
					$("#name").removeAttr("readonly");
					$("#name").attr("value","");
					$("#tel").attr("placeholder","不可输入");
					$("#tel").attr("value","");
					
					$.getJSON('${tenantPrefix}/workOperationCustom/getDict.do', {
						dictName:"fixedCode"
					},function(data){
						if(data.length == 0){
	               		 alert("数据字典未配置固定经销商编号");
						}
						var userCode = data;
						//根据固定专卖店编号获取仓库信息
						//根据专卖店编号获取仓库信息
						 $.getJSON('${tenantPrefix}/rs/varUser/storeInfo-forexchange', {
			                  customerInfoId: userCode
			                  }, function(strReturnJson) {
			                	  if(strReturnJson==undefined||strReturnJson==null||strReturnJson==""){
			                   		alert("获取仓库信息错误，请检查专卖店编号/手机号后，重新获取");
			                   		loading.modal('hide');
			                   		return;
			                   	 }
			               	  	if(strReturnJson.code!=200){
			                   		 alert(strReturnJson.message);
			                   		 loading.modal('hide');
			                   		 $("#wareHouse").html("<option value=''>请选择</option>");
			                   		 return;
			                   	 }
			               	  	
			               	  	 var data=strReturnJson.data;
			                   	 if(data.length ==0){
			                   		 alert("获取数据成功，但没有仓库数据，请检查专卖店编号/手机号后，重新获取");
			                   		 loading.modal('hide');
			                   		 return;
			                   	 }
			                   	 var strHtml ="<option value=''>请选择</option>";
			                     for (var i = 0; i < data.length; i++) {
			                         strHtml += "<option value='" + data[i].varStoCode + "'>" + data[i].nvrStoName + "</option>";
			                     }
			                     $("#wareHouse").html(strHtml);
			                     getwareHouse();
			                     
			                 	 $("#name").val("");
			                 	 $("#tel").val("");
			                     //根据专卖店编号和单据号获取退货信息
				       			 $.getJSON('${tenantPrefix}/rs/varUser/mallOrderInfo-forexchange', {
				       		                  customerInfoId: ucode,
				       		                  orderInfoId : orderNumber
				       		                  }, function(strReturnJson) {
				       	                    	 if(strReturnJson==undefined||strReturnJson==null||strReturnJson==""){
				       	                    		alert("获取订单信息错误，请检查专卖店编号/手机号和订单号后，重新获取");
				       	                    		return;
				       	                    	 }
				       	                    	 
				       	                    	 if(strReturnJson.code!=200){
				       	                    		 alert(strReturnJson.message);
				       	                    		 loading.modal('hide');
				       	                    		 $("#name").val("");
				       	                    		 $("#tel").val("");
				       	                    		 return;
				       	                    	 }
				       	                    	 var data=strReturnJson.data;
				       	                    	 if(data.length ==0){
				       	                    		 alert("订单没有数据信息，请检查专卖店编号/手机号和订单数据后，重新获取");
				       	                    		 loading.modal('hide');
				       	                    		 $("#name").val("");
				       	                    		 $("#tel").val("");
				       	                    		 return;
				       	                    	 }
				       	                    	 
				       		          			 var total = data.length;
				       		          			 /* var str = "<tr>"
				       		          			 		  +"<td rowspan='2' style='text-align:center;'>序号</td><td colspan='4' style='text-align:center;'>退回产品清单</td><td colspan='4' style='text-align:center;'>所换产品清单<a href='javascript:' onclick='fnAddColumn()'>[添加一行]</a>&emsp;<a href='javascript:' onclick='fnDelColumn()'>[删除一行]&emsp;</a></td>"
				       		          			 		  +"</tr>"
				       		          			 		  +"<tr>"
				       		          			 		  +"<td style='text-align:center;'>产品名称</td><td style='text-align:center;width:80px;'>数量</td><td style='text-align:center;width:80px;'>总金额</td><td style='text-align:center;width:80px;'>总PV</td><td style='text-align:center;'>产品名称</td><td style='text-align:center;width:80px;'>数量</td><td style='text-align:center;width:80px;'>总金额</td><td style='text-align:center;width:80px;'>总PV</td>"
				       		          			 		  +"</tr>"; */
				       		          			 var str = "";
				       		          			 var allTotalPrice=0;
				       		          			 var allTotalPV=0;
				       		          			 var allNumber=0;
				       		          			 proNoStr="";//
				       		          		     serialStr="";
				       		                    //遍历产品数据
				       		                    for (var i = 0; i < data.length; i++) {
				       		                    	//获取商品的产品编号，用于计算使用
				       		                    	if(i < data.length-1){
				       		                    		proNoStr += data[i].proNo+",";
				       		                    	}else{
				       		                    		proNoStr += data[i].proNo;
				       		                    	}
				       		                    	//获取商品的产品编号，用于计算使用
				       		                    	if(i < data.length-1){
				       		                    		serialStr += parseInt(i+1)+",";
				       		                    	}else{
				       		                    		serialStr += parseInt(i+1);
				       		                    	}
				       		                    	
				       		                    	var number = parseInt(data[i].shopPVNum)+parseInt(data[i].shopRewardNum)+parseInt(data[i].shopWalletNum);
				       		                    	var totalPrice = parseFloat(number*data[i].proPrice);//单个商品总金额
				       		                    	var totalPV = parseFloat(number*data[i].proPV);//单个商品总PV
				       		                    	totalPrice = returnFloat(totalPrice);
				       		                    	totalPV = returnFloat(totalPV);
				       		                    	allTotalPrice = parseFloat(allTotalPrice)+parseFloat(totalPrice);
				       		                    	allTotalPrice= returnFloat(allTotalPrice);
				       		                    	allTotalPV = parseFloat(allTotalPV)+parseFloat(totalPV);
				       		                    	allTotalPV = returnFloat(allTotalPV);
				       		                    	allNumber = parseFloat(allNumber)+parseFloat(number);
				       		                    	//商城获取到的商品单价金额和单价pv需要特殊格式化
				       		                    	var proPrice = returnFloat(data[i].proPrice);
				       		                    	var proPV = returnFloat(data[i].proPV);
				       		                    	str += "<tr>"+
				       		                    		   "<td style='text-align:center;'>"+parseInt(i+1)+"</td>"+
				       		                    		   "<td>"+data[i].proName+"</td>"+
				       		                    		   "<td><input type='text' id='old"+data[i].proNo+"number' onkeyup='exchangeResult(this.id,"+number+")' value='"+number+"'</td>"+
				       		                    		   "<td id='old"+data[i].proNo+"totalPrice'>"+totalPrice+"</td>"+
				       		                    		   "<td id='old"+data[i].proNo+"totalPV'>"+totalPV+"</td>"+
				       		                    		   "<input type='hidden' name='backType' value='0'>"+//以下至空格行为要传的参数
				       		                    		   "<input type='hidden' name='backProName' value='"+data[i].proName+"'>"+
				       		                    		   "<input id='back"+data[i].proNo+"number' type='hidden' name='backNumber' value='"+number+"'>"+
				       		                    		   "<input id='back"+data[i].proNo+"totalPrice' type='hidden' name='backTotalPrice' value='"+totalPrice+"'>"+
				       		                    		   "<input id='back"+data[i].proNo+"totalPV' type='hidden' name='backTotalPV' value='"+totalPV+"'>"+
				       		                    		   "<input id='back"+data[i].proNo+"' type='hidden' name='backProNo' value='"+data[i].proNo+"'>"+
				       		                    		   "<input type='hidden' id='old"+data[i].proNo+"unitPrice' name='backUnitPrice' value='"+proPrice+"'>"+
				       		                    		   "<input type='hidden' id='old"+data[i].proNo+"unitPV' name='backUnitPV' value='"+proPV+"'>"+
				       		                    		   "<input type='hidden' id='max"+data[i].proNo+"number' name='maxNumber' value='"+number+"'>"+//记录每个产品的最大数量，方便调整时做数量的校验 18.11.2
				       		                    		   
				       		                    		   "<td class='.td_selectpicker'><select data-live-search='true' class='selectpicker' id='"+parseInt(i+1)+"' onchange='exchangeProduct(this.id)'></select></td>"+
				       		                    		   "<td><input id='"+parseInt(i+1)+"number' type='text' style='width:100%' onkeyup='exchangeResultDetail(this.id)' value='0'></td>"+
				       		                    		   "<td id='"+parseInt(i+1)+"price'>0</td>"+
				       		                    		   "<td id='"+parseInt(i+1)+"PV'>0</td>"+
				       		                    		   "<input type='hidden' name='exchangeType' value='1'>"+//以下至<tr>为要传的参数
				       		                    		   "<input type='hidden' id='exchange"+parseInt(i+1)+"proName' name='exchangeProName' value='请选择'>"+
				       		                    		   "<input type='hidden' id='exchange"+parseInt(i+1)+"number' name='exchangeNumber' value='0'>"+
				       		                    		   "<input type='hidden' id='exchange"+parseInt(i+1)+"totalPrice' name='exchangeTotalPrice' value='0'>"+
				       		                    		   "<input type='hidden' id='exchange"+parseInt(i+1)+"totalPV' name='exchangeTotalPV' value='0'>"+
				       		                    		   "<input type='hidden' id='"+parseInt(i+1)+"proNo' name='exchangeProNo' value='请选择'>"+
				       		                    		   "<input type='hidden' id='"+parseInt(i+1)+"unitPrice' name='exchangeUnitPrice' value='0'>"+
				       		                    		   "<input type='hidden' id='"+parseInt(i+1)+"unitPV' name='exchangeUnitPV' value='0'>"+
				       		                    		   "</tr>";
				       		                    	getExchange(parseInt(i+1));
				       		                    }
				       		                    str += "<tr>"+
				       		                    	   "<td style='text-align:center;'>合计</td>"+
				       		                    	   "<td></td>"+
				       		                    	   "<td id='oldAllTotalNumber' style='text-align:center;'>"+allNumber+"</td>"+
				       		                    	   "<td id='oldAllTotalPrice' style='text-align:center;'>"+allTotalPrice+"</td>"+
				       		                    	   "<td id='oldAllTotalPV' style='text-align:center;'>"+allTotalPV+"</td>"+
				       		                    	   "<td></td>"+
				       		                    	   "<td id='AllTotalNumber' style='text-align:center;'></td>"+
				       		                    	   "<td id='AllTotalPrice' style='text-align:center;'></td>"+
				       		                    	   "<td id='AllTotalPV' style='text-align:center;'></td>"+
				       		                    	   "</tr>";
				       		                    $("#product-tbody").html(str);
				       		                    }
				       		          		);
				       			 loading.modal('hide');
			       		});
					});
				}else{
					$("#name").attr("readonly","readonly");
					changeOaDiv();
					//根据专卖店编号获取仓库信息
					 $.getJSON('${tenantPrefix}/rs/varUser/storeInfo-forexchange', {
		                  customerInfoId: ucode
		                  }, function(strReturnJson) {
		                	  if(strReturnJson==undefined||strReturnJson==null||strReturnJson==""){
		                   		alert("获取仓库信息错误，请检查专卖店编号/手机号后，重新获取");
		                   		loading.modal('hide');
		                   		return;
		                   	 }
		               	  	if(strReturnJson.code!=200){
		                   		 alert(strReturnJson.message);
		                   		 loading.modal('hide');
		                   		 $("#wareHouse").html("<option value=''>请选择</option>");
		                   		 return;
		                   	 }
		               	  	
		               	  	 var data=strReturnJson.data;
		                   	 if(data.length ==0){
		                   		 alert("获取数据成功，但没有仓库数据，请检查专卖店编号/手机号后，重新获取");
		                   		 loading.modal('hide');
		                   		 return;
		                   	 }
		                   	 var strHtml ="<option value=''>请选择</option>";
		                     for (var i = 0; i < data.length; i++) {
		                         strHtml += "<option value='" + data[i].varStoCode + "'>" + data[i].nvrStoName + "</option>";
		                     }
		                     $("#wareHouse").html(strHtml);
		                     getwareHouse();
		                     
		                 	 $("#name").val("");
		                 	 $("#tel").val("");
		                     //根据专卖店编号和单据号获取退货信息
			       			 $.getJSON('${tenantPrefix}/rs/varUser/orderInfo-forexchange', {
			       		                  customerInfoId: ucode,
			       		                  orderInfoId : orderNumber
			       		                  }, function(strReturnJson) {
			       	                    	 if(strReturnJson==undefined||strReturnJson==null||strReturnJson==""){
			       	                    		alert("获取订单信息错误，请检查专卖店编号/手机号和订单号后，重新获取");
			       	                    		return;
			       	                    	 }
			       	                    	 
			       	                    	 if(strReturnJson.code!=200){
			       	                    		 alert(strReturnJson.message);
			       	                    		 loading.modal('hide');
			       	                    		 $("#name").val("");
			       	                    		 $("#tel").val("");
			       	                    		 return;
			       	                    	 }
			       	                    	 var data=strReturnJson.data;
			       	                    	 if(data.length ==0){
			       	                    		 alert("订单没有数据信息，请检查专卖店编号/手机号和订单数据后，重新获取");
			       	                    		 loading.modal('hide');
			       	                    		 $("#name").val("");
			       	                    		 $("#tel").val("");
			       	                    		 return;
			       	                    	 }
			       	                    	 
			       		          			 $("#name").val(data[0].shopName);
			       		          			 $("#tel").val(data[0].shopTel);
			       		          			 var total = data.length;
			       		          			 /* var str = "<tr>"
			       		          			 		  +"<td rowspan='2' style='text-align:center;'>序号</td><td colspan='4' style='text-align:center;'>退回产品清单</td><td colspan='4' style='text-align:center;'>所换产品清单<a href='javascript:' onclick='fnAddColumn()'>[添加一行]</a>&emsp;<a href='javascript:' onclick='fnDelColumn()'>[删除一行]&emsp;</a></td>"
			       		          			 		  +"</tr>"
			       		          			 		  +"<tr>"
			       		          			 		  +"<td style='text-align:center;'>产品名称</td><td style='text-align:center;width:80px;'>数量</td><td style='text-align:center;width:80px;'>总金额</td><td style='text-align:center;width:80px;'>总PV</td><td style='text-align:center;'>产品名称</td><td style='text-align:center;width:80px;'>数量</td><td style='text-align:center;width:80px;'>总金额</td><td style='text-align:center;width:80px;'>总PV</td>"
			       		          			 		  +"</tr>"; */
			       		          			 var str = "";
			       		          			 var allTotalPrice=0;
			       		          			 var allTotalPV=0;
			       		          			 var allNumber=0;
			       		          			 proNoStr="";//
			       		          		     serialStr="";
			       		                    //遍历产品数据
			       		                    for (var i = 0; i < data.length; i++) {
			       		                    	//获取商品的产品编号，用于计算使用
			       		                    	if(i < data.length-1){
			       		                    		proNoStr += data[i].proNo+",";
			       		                    	}else{
			       		                    		proNoStr += data[i].proNo;
			       		                    	}
			       		                    	//获取商品的产品编号，用于计算使用
			       		                    	if(i < data.length-1){
			       		                    		serialStr += parseInt(i+1)+",";
			       		                    	}else{
			       		                    		serialStr += parseInt(i+1);
			       		                    	}
			       		                    	
			       		                    	var number = parseInt(data[i].shopPVNum)+parseInt(data[i].shopRewardNum)+parseInt(data[i].shopWalletNum);
			       		                    	var totalPrice = parseFloat(number*data[i].proPrice);//单个商品总金额
			       		                    	var totalPV = parseFloat(number*data[i].proPV);//单个商品总PV
			       		                    	totalPrice = returnFloat(totalPrice);
			       		                    	totalPV = returnFloat(totalPV);
			       		                    	allTotalPrice = parseFloat(allTotalPrice)+parseFloat(totalPrice);
			       		                    	allTotalPrice= returnFloat(allTotalPrice);
			       		                    	allTotalPV = parseFloat(allTotalPV)+parseFloat(totalPV);
			       		                    	allTotalPV = returnFloat(allTotalPV);
			       		                    	allNumber = parseFloat(allNumber)+parseFloat(number);
			       		                    	str += "<tr>"+
			       		                    		   "<td style='text-align:center;'>"+parseInt(i+1)+"</td>"+
			       		                    		   "<td>"+data[i].proName+"</td>"+
			       		                    		   "<td><input type='text' id='old"+data[i].proNo+"number' onkeyup='exchangeResult(this.id,"+number+")' value='"+number+"'</td>"+
			       		                    		   "<td id='old"+data[i].proNo+"totalPrice'>"+totalPrice+"</td>"+
			       		                    		   "<td id='old"+data[i].proNo+"totalPV'>"+totalPV+"</td>"+
			       		                    		   "<input type='hidden' name='backType' value='0'>"+//以下至空格行为要传的参数
			       		                    		   "<input type='hidden' name='backProName' value='"+data[i].proName+"'>"+
			       		                    		   "<input id='back"+data[i].proNo+"number' type='hidden' name='backNumber' value='"+number+"'>"+
			       		                    		   "<input id='back"+data[i].proNo+"totalPrice' type='hidden' name='backTotalPrice' value='"+totalPrice+"'>"+
			       		                    		   "<input id='back"+data[i].proNo+"totalPV' type='hidden' name='backTotalPV' value='"+totalPV+"'>"+
			       		                    		   "<input type='hidden' id='old"+data[i].proNo+"unitPrice' name='backUnitPrice' value='"+data[i].proPrice+"'>"+
			       		                    		   "<input type='hidden' id='old"+data[i].proNo+"unitPV' name='backUnitPV' value='"+data[i].proPV+"'>"+
			       		                    		   "<input id='back"+data[i].proNo+"' type='hidden' name='backProNo' value='"+data[i].proNo+"'>"+
			       		                    		   "<input id='max"+data[i].proNo+"number' type='hidden' name='maxNumber' value='"+number+"'>"+//记录产品最大数量，用于调整页检验数量18.11.2
			       		                    		   
			       		                    		   "<td class='.td_selectpicker'><select data-live-search='true' class='selectpicker' id='"+parseInt(i+1)+"' onchange='exchangeProduct(this.id)'></select></td>"+
			       		                    		   "<td><input id='"+parseInt(i+1)+"number' type='text' style='width:100%' onkeyup='exchangeResultDetail(this.id)' value='0'></td>"+
			       		                    		   "<td id='"+parseInt(i+1)+"price'>0</td>"+
			       		                    		   "<td id='"+parseInt(i+1)+"PV'>0</td>"+
			       		                    		   "<input type='hidden' name='exchangeType' value='1'>"+//以下至<tr>为要传的参数
			       		                    		   "<input type='hidden' id='exchange"+parseInt(i+1)+"proName' name='exchangeProName' value='请选择'>"+
			       		                    		   "<input type='hidden' id='exchange"+parseInt(i+1)+"number' name='exchangeNumber' value='0'>"+
			       		                    		   "<input type='hidden' id='exchange"+parseInt(i+1)+"totalPrice' name='exchangeTotalPrice' value='0'>"+
			       		                    		   "<input type='hidden' id='exchange"+parseInt(i+1)+"totalPV' name='exchangeTotalPV' value='0'>"+
			       		                    		   "<input type='hidden' id='"+parseInt(i+1)+"unitPrice' name='exchangeUnitPrice' value='0'>"+
			       		                    		   "<input type='hidden' id='"+parseInt(i+1)+"unitPV' name='exchangeUnitPV' value='0'>"+
			       		                    		   "<input type='hidden' id='"+parseInt(i+1)+"proNo' name='exchangeProNo' value='请选择'>"+
			       		                    		   "</tr>";
			       		                    	getExchange(parseInt(i+1));
			       		                    }
			       		                    str += "<tr>"+
			       		                    	   "<td style='text-align:center;'>合计</td>"+
			       		                    	   "<td></td>"+
			       		                    	   "<td id='oldAllTotalNumber' style='text-align:center;'>"+allNumber+"</td>"+
			       		                    	   "<td id='oldAllTotalPrice' style='text-align:center;'>"+allTotalPrice+"</td>"+
			       		                    	   "<td id='oldAllTotalPV' style='text-align:center;'>"+allTotalPV+"</td>"+
			       		                    	   "<td></td>"+
			       		                    	   "<td id='AllTotalNumber' style='text-align:center;'></td>"+
			       		                    	   "<td id='AllTotalPrice' style='text-align:center;'></td>"+
			       		                    	   "<td id='AllTotalPV' style='text-align:center;'></td>"+
			       		                    	   "</tr>";
			       		                    $("#product-tbody").html(str);
			       		                    }
			       		          		);
			       			 loading.modal('hide');
		       		});
				
				}
				
			});
			
			
			
            
		}
		
		var fnAddColumn=function(){
			
			var num=$("#product-tbody tr:last").prev().find("td:first").html();
			var num_no=(parseInt(num)+1);
			serialStr += ","+num_no;
			var select_html= "<tr class='tr_addcolumn' value='"+num_no+"number'>"+
		 		   "<td style='text-align:center'>"+num_no+"</td><td></td><td></td><td></td><td></td>"+
		 		   "<td class='.td_selectpicker'><select data-live-search='true' class='selectpicker' id='"+num_no+"' onchange='exchangeProduct(this.id)'></select></td>"+
		 		   "<td><input id='"+num_no+"number' type='text' style='width:100%' onkeyup='exchangeResultDetail(this.id)' value='0'></td>"+
		 		   "<td id='"+num_no+"price'>0</td>"+
		 		   "<td id='"+num_no+"PV'>0</td>"+
		 		   "<input type='hidden' name='exchangeType' value='1'>"+//以下至<tr>为要传的参数
		 		   "<input type='hidden' id='exchange"+num_no+"proName' name='exchangeProName' value='请选择'>"+
		 		   "<input type='hidden' id='exchange"+num_no+"number' name='exchangeNumber' value='0'>"+
		 		   "<input type='hidden' id='exchange"+num_no+"totalPrice' name='exchangeTotalPrice' value='0'>"+
		 		   "<input type='hidden' id='exchange"+num_no+"totalPV' name='exchangeTotalPV' value='0'>"+
		 		   "<input type='hidden' id='"+num_no+"unitPrice' name='exchangeUnitPrice' value='0'>"+
		 		   "<input type='hidden' id='"+num_no+"unitPV' name='exchangeUnitPV' value='0'>"+
		 		   "<input type='hidden' id='"+num_no+"proNo' name='exchangeProNo' value='请选择'>"+
		 		   "</tr>";
 		    $("#product-tbody tr:last").before(select_html);	 
				getExchange(num_no);
 			
		}
		
		var fnDelColumn=function(){
			if($(".tr_addcolumn").size()>0){
				var lastIndex = serialStr.lastIndexOf(",");
				serialStr = serialStr.substring(0,lastIndex);
				var delId = $(".tr_addcolumn:last").attr("value");
				exchangeResultDetail(delId);
				$(".tr_addcolumn:last").remove();
				
			}
		}
		
		//保留小数点后两位
		function returnFloat(value){
			 var value=Math.round(parseFloat(value)*100)/100;
			 var xsd=value.toString().split(".");
			 /* alert(value+"-----"+xsd.length); */
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
		function exchangeResult(id,maxNum){
			var value = $("#"+id).val();
			if(value == ""){
				$("#"+id).val(0);
				value=0;
			}
			if(value > maxNum){
				$("#"+id).val(maxNum);
			}
			value = $("#"+id).val();
			var reg = /^([1-9][0-9]*){1,3}$/;
			var endIndex = id.indexOf("number");
			var proNo = id.substring(3,endIndex);
			if(value.match(reg)==null){
				$("#"+id).val(0);
				number=0;
			}
			//获取单价和pv值
			var unitPrice = $("#old"+proNo+"unitPrice").val();
			var unitPV = $("#old"+proNo+"unitPV").val();
			//计算某个商品的总Pv和总金额
			var totalPrice = parseFloat(value)*parseFloat(unitPrice);
			var totalPV = parseFloat(value)*parseFloat(unitPV);
			totalPrice = returnFloat(totalPrice);
			totalPV = returnFloat(totalPV);
			//获取某个商品的总金额和总pv的td 显示用
			$("#old"+proNo+"totalPrice").html(totalPrice);
			$("#old"+proNo+"totalPV").html(totalPV);
			//传值用
			$("#back"+proNo+"totalPrice").val(totalPrice);
			$("#back"+proNo+"totalPV").val(totalPV);
			$("#back"+proNo+"number").val(value);
			//定义三个变量，分别是合计总金额和总pv
			var allTotalPrice = 0;
			var allTotalPV = 0;
			var allTotalNumber = 0;
			var array = proNoStr.split(",");
			//循环获取每个产品的总金额和总pv,计算合计
			for(var i=0;i<array.length;i++){
				allTotalPrice=FloatAdd(allTotalPrice, parseFloat($("#old"+array[i]+"totalPrice").html()));
				allTotalPV=FloatAdd(allTotalPV, parseFloat($("#old"+array[i]+"totalPV").html()));
				allTotalNumber=FloatAdd(allTotalNumber, parseFloat($("#old"+array[i]+"number").val()));
				
				/* allTotalPrice += parseFloat($("#old"+array[i]+"totalPrice").html());
				allTotalPV += parseFloat($("#old"+array[i]+"totalPV").html());
				allTotalNumber += parseInt($("#old"+array[i]+"number").val()); */
			}
			
			//合计总金额转小数
			allTotalPrice = returnFloat(allTotalPrice);
			//alert(allTotalPrice);
			allTotalPV = returnFloat(allTotalPV);
			$("#oldAllTotalPrice").html(allTotalPrice);
			$("#oldAllTotalPV").html(allTotalPV);
			$("#oldAllTotalNumber").html(allTotalNumber);
			//合计综合金额 数量赋值隐藏input
			$("#backAllNumber").val(allTotalNumber);
			$("#backAllTotalPrice").val(allTotalPrice);
			$("#backAllTotalPV").val(allTotalPV);
		}
		/* 获取用户选择的要换的产品 */
		function exchangeProduct(id){
			var myselect=document.getElementById(id);
    		var index=myselect.selectedIndex;
    		var value=myselect.options[index].value;
    		var text=myselect.options[index].text;
    		//vaule拆分
    		var array = value.split(",");
    		if(text == "请选择"){
    			//商品编号也需要有值
    			$("#"+id+"proNo").val(text);
    			$("#"+id+"unitPrice").val(0);//赋值给隐藏的input
    			$("#"+id+"unitPV").val(0);
    			$("#"+id+"price").html(0);//所换产品清单的总金额和总PV默认先赋值单价的
    			$("#"+id+"PV").html(0);
    			$("#"+id+"number").val(0);
    			//赋值给传值的input
    			$("#exchange"+id+"proName").val(text);
    			$("#exchange"+id+"number").val(0);
    			$("#exchange"+id+"totalPV").val(0);
    			$("#exchange"+id+"totalPrice").val(0);
    			
    		}else{
    			$("#"+id+"proNo").val(array[0]);//产品编号
    			$("#"+id+"unitPrice").val(array[2]);//赋值给隐藏的input
    			$("#"+id+"unitPV").val(array[1]);
    			$("#"+id+"price").html(array[2]);//所换产品清单的总金额和总PV默认先赋值单价的
    			$("#"+id+"PV").html(array[1]);
    			$("#"+id+"number").val(1);//换货数量默认为1
    			//赋值给传值的input
    			$("#exchange"+id+"proName").val(text);
    			$("#exchange"+id+"number").val(1);
    			$("#exchange"+id+"totalPV").val(array[1]);
    			$("#exchange"+id+"totalPrice").val(array[2]);
    		}
    		
    		$("#"+id).val(value);
			
    		//合计
    		var selectArray=$(".selectpicker");
    		var totalExchangePrice=0;
    		var totalExchangePV=0;
    		var totalExchangeNum=0;
    		if(selectArray.length>0){
    			for(var i=0;i<selectArray.length;i++){
    				var selectValue=$(selectArray[i]).val();
    				if(selectValue!="0"){
    					var valueArray=selectValue.split(',');
    					var iptNum=$(selectArray[i]).parent().next().find("input").val();
    					totalExchangeNum=FloatAdd(totalExchangeNum, iptNum);
    					totalExchangePrice=FloatAdd(totalExchangePrice,FloatMul(parseFloat(iptNum),parseFloat(valueArray[1])))
    					totalExchangePV=FloatAdd(totalExchangePV,FloatMul(parseFloat(iptNum),parseFloat(valueArray[2])))
    				}
   				}
    		}
    		
    		var totalExchangePrice_str=totalExchangePrice+"";
    		var totalExchangePV_str=totalExchangePV+"";
    		if(totalExchangePrice_str.indexOf(".")<0){
    			totalExchangePrice_str=totalExchangePrice_str+".00";
    		}else{
    			var totalExchangePrice_str_array =  totalExchangePrice_str.split(".");
    			if(totalExchangePrice_str_array[1].length == 1){
    				totalExchangePrice_str=totalExchangePrice_str+"0";
    			}
    		}
    			
    		if(totalExchangePV_str.indexOf(".")<0){
    			totalExchangePV_str=totalExchangePV_str+".00";
    		}else{
    			var totalExchangePV_str_array = totalExchangePV_str.split(".");
    			if(totalExchangePV_str_array[1].length == 1){
    				totalExchangePV_str = totalExchangePV_str+"0";
    			}
    		}
    			
    		
			$("#AllTotalPrice").html(totalExchangePrice_str);
			$("#AllTotalPV").html(totalExchangePV_str);
			$("#AllTotalNumber").html(totalExchangeNum);
			
			$("#exchangeAllTotalNumber").val(totalExchangePrice_str);
			$("#exchangeAllTotalPrice").val(totalExchangePV_str);
			$("#exchangeAllTotalPV").val(totalExchangeNum);
		}
		
		function exchangeResultDetail(id){
			var number = $("#"+id).val();
			if(number == ""){
				$("#"+id).val(0);
			}
			number = $("#"+id).val();
			//正则校验输入的数量
			var reg = /^([1-9][0-9]*){1,3}$/;
			if(number.match(reg)==null){
				$("#"+id).val(0);
				number=0;
			}
			var array = id.split("n");
			var sub = array[0];
			var unitPrice = $("#"+sub+"unitPrice").val();
			if(unitPrice == 0 || unitPrice == ""){//换货清单中不选择产品，直接选择换货数量
				$("#"+id).val(0);
				return;
			}
			var unitPV = $("#"+sub+"unitPV").val();
			var totalPrice = parseFloat(number)*parseFloat(unitPrice);
			var totalPV = parseFloat(number)*parseFloat(unitPV);
			totalPrice = returnFloat(totalPrice);
			totalPV = returnFloat(totalPV);
			//给单个商品的总金额和总PV赋值
			$("#"+sub+"price").html(totalPrice);
			$("#"+sub+"PV").html(totalPV);
			var serialArray = serialStr.split(",");
			//给传值的input赋值
			$("#exchange"+sub+"number").val(number);
			$("#exchange"+sub+"totalPrice").val(totalPrice);
			$("#exchange"+sub+"totalPV").val(totalPV);
			//定义三个变量，所有产品的总金额 总PV 总数量
			var allTotalPrice = 0;
			var allTotalPV = 0;
			var allTotalNumber = 0;
			for(var i = 0;i<serialArray.length;i++){
				allTotalPrice=FloatAdd(allTotalPrice, parseFloat($("#"+serialArray[i]+"price").html()));
				allTotalPV=FloatAdd(allTotalPV, parseFloat($("#"+serialArray[i]+"PV").html()));
				allTotalNumber=FloatAdd(allTotalNumber, parseFloat($("#"+serialArray[i]+"number").val()));
			}
			allTotalPrice = returnFloat(allTotalPrice);
			allTotalPV = returnFloat(allTotalPV);
			//给合计的商品总金额和总PV赋值
			$("#AllTotalPrice").html(allTotalPrice);
			$("#AllTotalPV").html(allTotalPV);
			$("#AllTotalNumber").html(allTotalNumber);
			//合计input赋值
			$("#exchangeAllTotalNumber").val(allTotalNumber);
			$("#exchangeAllTotalPrice").val(allTotalPrice);
			$("#exchangeAllTotalPV").val(allTotalPV);
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
				$("#lbl_empNo").html(data.employeeNo);
			});
		})
		
		ROOT_URL = '${tenantPrefix}';
		taskOperation = new TaskOperation();
		function getExchange(product){
			var ucode = $("#ucode").val();
			if(ucode.length == 11){
				var timestamp=new Date().getTime();
				$.getJSON("${tenantPrefix}/rs/varUser/mallProductInfo",
						{timestamp:timestamp},function (data){
							console.log(data);
							var option = "<option value='0'>请选择</option>";
							for(var i=0;i<data.length;i++){
								var mnyPV =returnFloat(data[i].mnyPV);
								var mnyPrice=returnFloat(data[i].mnyPrice);
								option += "<option value='"+data[i].varProductNO+","+mnyPV+","+mnyPrice+"'>"+data[i].varProductName+"</option>";
							}
							$("#"+product).html(option);
				});
			}else{
				$.getJSON("${tenantPrefix}/rs/varUser/productInfo",
						{customerInfoId:ucode},function (data){
							console.log(data);
							var option = "<option value='0'>请选择</option>";
							for(var i=0;i<data.length;i++){
								option += "<option value='"+data[i].varProductNO+","+data[i].mnyPV+","+data[i].mnyPrice+"'>"+data[i].varProductName+"</option>";
							}
							$("#"+product).html(option);
				});	
			}
			
		}
		var conf={
				applyCodeId:"applyCode",				//受理单号input的ID
			    submitBtnId:"confirmStartProcess",		//提交按钮ID	
				checkApplyCodeUrl:"${tenantPrefix}/rs/business/applyCodeIfExist",	//验证受理单号url
    			checkUrl:"${tenantPrefix}/rs/customer/opteraion-getposition",		//获取岗位url
    			actionUrl:"${tenantPrefix}/Exchange/process-operationExchange-startProcessInstance.do",//提交URL
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
			
			var val=$('input:radio[name="payType"]:checked').val();
            if(val==null){
                alert("手续费必选一项");
                return;
            }
            if(val == "减免"){
            	if($("#inputApplyCode").val() == ""){
            		alert("请输入受理单号");
            	}
            }else{
            	if($("#inputApplyCode").val() != ""){
            		$("#inputApplyCode").val("");
            	}
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
			if($("#orderTime").val() == ''){
				alert("订货时间不能为空");
				 return;
			}
			if($("#oldConsignee").val() == ''){
				alert("原收货人不能为空");
				 return;
			}
			if($("#oldConsigneeTel").val() == ''){
				alert("原收货电话不能为空");
				 return;
			}
			if($("#oldConsigneeAddress").val() == ''){
				alert("原收货地址不能为空");
				 return;
			}
			if($("#exchangeReason").val().replace(/(^\s*)|(\s*$)/g, "") == ''){
				alert("换货原因不能为空");
				 return;
			}
			if($("#newConsigneeAddress").val() == ''){
				alert("更改后收货地址不能为空");
				 return;
			}
			if($("#newConsignee").val() == ''){
				alert("更改后收货人不能为空");
				 return;
			}
			if($("#newConsigneeTel").val() == ''){
				alert("更改后收货电话不能为空");
				 return;
			}
			if(document.getElementById("outOrder") == 'out'){
				alert("存在过期单据号！");
				return
			}
			if($("#shopName").val() == ''||$("#shopTel").val() == ''){
				alert("请核对专卖店编号/手机号和订单号");
				return
			}
			if($("#wareHouse").val() == ''){
				alert("请选择仓库");
				return
			}
			if($("#name").val() == ''){
				alert("请填写姓名");
				return;
			}
			if($("#zipCode").val() == ''){
				alert("请填写邮编");
				return;
			}
			
			var oldAllTotalPrice = $("#oldAllTotalPrice").html();
			var AllTotalPrice = $("#AllTotalPrice").html();
			
			var oldAllTotalPV = $("#oldAllTotalPV").html();
			var AllTotalPV = $("#AllTotalPV").html();
			if(oldAllTotalPrice != AllTotalPrice){
				alert("换回和换出产品需要等值等PV");
				return;
			}
			if(oldAllTotalPV != AllTotalPV){
				alert("换回和换出产品需要等值等PV");
				return;
			}
			if($("#AllTotalNumber").html()==0 || $("#AllTotalNumber").html()== ""){
				alert("所换产品清单未选择产品");
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
    	 $(function(){
			  $("#xtable :radio").click(function(){
			   	var type = $(this).val();
			   	if(type == '减免'){
			   		$("#inputApplyCode").show();
			   	}else{
			   		$("#inputApplyCode").hide();	
			   	}
			  });
    	 })
    	 
    	 function changeMallDiv(){
    		 $("#form-tfoots").html('<tr id="xf-2-3">'
								+'<td id="xf-2-3-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left" rowspan="3">'
								+'	<div class="xf-handler">'
								+'		<label style="display:block;text-align:center;margin-bottom:0px;"><font color="red">*</font>&nbsp;订货时间</label>'
								+'	</div>'
								+'</td>'
								+'<td id="xf-2-3-1" class="xf-cell xf-cell-right xf-cell-bottom" colspan="5" rowspan="3">'
								+'	<div class="xf-handler">'
								+'		<div id="pickerTime" class="input-group  datepickerend date">'
								+'		    <input type="text" name="orderTime" id="orderTime" required style="" autocomplete="off">'
								+'		    <span id="calendar" class="input-group-addon">'
								+'				<i class="glyphicon glyphicon-calendar"></i>'
								+'			</span>'
								+'    	</div>'
								+'    		</div>'
								+'</td>'
								+'<td id="xf-2-3-2" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left" rowspan="3">'
								+'	<div class="xf-handler">'
								+'		<label style="display:block;text-align:center;margin-bottom:0px;"><font color="red">*</font>&nbsp;手续费</label>'
								+'	</div>'
								+'</td>'
								+'	<td id="xf-2-4-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" colspan="3">'
								+'		<div class="xf-handler">'
								+'			<input type="radio" name="payType" value="已转账" id="mallPay">已转账'
								+'		</div>'
								+'	</td>'
								+'</tr>'
								
								+'<tr>'
								+'	<td class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" colspan="3">'
								+'   	<input type="radio" name="payType" value="店支付">店支付'
								+'	</td>'
								+'</tr>'
								
								+'<tr id="xf-2-5">'
								+'	<td id="xf-2-5-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" colspan="3">'
								+'		<!-- <div class="xf-handler"> -->'
								+'			<input type="radio" name="payType" value="减免" id="derate" >减免'
								+'			<input type="text" id="inputApplyCode" name="inputApplyCode" style="display:none;" placeholder="请输入受理单号" maxlength="20">'
								+'		<!-- </div> -->'
								+'	</td>'
								+'</tr>');
    		 
    		 $("#xtable :radio").click(function(){
 			   	var type = $(this).val();
 			   	if(type == '减免'){
 			   		$("#inputApplyCode").show();
 			   	}else{
 			   		$("#inputApplyCode").hide();	
 			   	}
 			  });
    		 
    		 picker();
    	 }
    	 function changeOaDiv(){
    		 $("#form-tfoots").html('<tr id="xf-2-3">'
								+'<td id="xf-2-3-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left" rowspan="3">'
								+'	<div class="xf-handler">'
								+'		<label style="display:block;text-align:center;margin-bottom:0px;"><font color="red">*</font>&nbsp;订货时间</label>'
								+'	</div>'
								+'</td>'
								+'<td id="xf-2-3-1" class="xf-cell xf-cell-right xf-cell-bottom" colspan="5" rowspan="3">'
								+'	<div class="xf-handler">'
								+'		<div id="pickerTime" class="input-group  datepickerend date">'
								+'		    <input type="text" name="orderTime" id="orderTime" required style="" autocomplete="off">'
								+'		    <span id="calendar" class="input-group-addon">'
								+'				<i class="glyphicon glyphicon-calendar"></i>'
								+'			</span>'
								+'    	</div>'
								+'    		</div>'
								+'</td>'
								+'<td id="xf-2-3-2" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left" rowspan="3">'
								+'	<div class="xf-handler">'
								+'		<label style="display:block;text-align:center;margin-bottom:0px;"><font color="red">*</font>&nbsp;手续费</label>'
								+'	</div>'
								+'</td>'
								+'<td id="xf-2-3-3" class="xf-cell xf-cell-right xf-cell-bottom" colspan="3">'
								+'	<div class="xf-handler">'
								+'		<input type="radio" name="payType" value="店支付" id="shopPay">店支付'
								+'	</div>'
								+'</td>'
								+'</tr>'
								+'<tr id="xf-2-4">'
								+'	<td id="xf-2-4-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" colspan="3">'
								+'		<div class="xf-handler">'
								+'			<input type="radio" name="payType" value="个人钱包" id="personPay">个人钱包'
								+'		</div>'
								+'	</td>'
								+'</tr>'
								+'<tr id="xf-2-5">'
								+'	<td id="xf-2-5-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" colspan="3">'
								+'		<!-- <div class="xf-handler"> -->'
								+'			<input type="radio" name="payType" value="减免" id="derate" >减免'
								+'			<input type="text" id="inputApplyCode" name="inputApplyCode" style="display:none;" placeholder="请输入受理单号" maxlength="20">'
								+'		<!-- </div> -->'
								+'	</td>'
								+'</tr>');
    		 $("#xtable :radio").click(function(){
 			   	var type = $(this).val();
 			   	if(type == '减免'){
 			   		$("#inputApplyCode").show();
 			   	}else{
 			   		$("#inputApplyCode").hide();	
 			   	}
 			  });
    		 
    		 picker();
    	 }
    </script>
  </head>

  <body>
    <%@include file="/header/bpm-workspace3.jsp"%>
<form id="xform" method="post" action="${tenantPrefix}/Exchange/process-operationExchange-startProcessInstance.do" class="xf-form" enctype="multipart/form-data" style="AutoScrool:true">
    <div class="container">
      <section id="m-main" class="col-md-12" style="padding-top:65px;">
		<input id="processDefinitionId" type="hidden" name="processDefinitionId" value="${processDefinitionId}">
		<input id="bpmProcessId" type="hidden" name="bpmProcessId" value="${bpmProcessId}">
		<input id="businessDetailId" type="hidden" name="businessDetailId" value="${businessDetailId}">
		<input id="autoCompleteFirstTask" type="hidden" name="autoCompleteFirstTask" value="false">
		<input id="businessKey" type="hidden" name="businessKey" value="${businessKey}">
		<input id="userId" type="hidden" name="userId" value="${userId}">
		<input id="activityId" type="hidden" name="activityId" value="">
		<input id="url" type="hidden" name="url" value="/Exchange/form-detail.do">
		<input type="hidden" id="applyCode" name="applyCode" value="${code}">
		<input id="outOrder" type="hidden">
		<input id="areaId" type="hidden" name="areaId">
		<input id="areaName" type="hidden" name="areaName">
		<input id="companyId" type="hidden" name="companyId">
		<input id="companyName" type="hidden" name="companyName">
		<input style="border:0px;width:10px" readonly type="hidden" id="submitTimes" name="submitTimes" value="0">
		<div id="xf-form-table">
			<div id="xf-1" class="xf-section">
				<h1 style="text-align:center;">换货申请单</h1>
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
							<td id="xf-2-0-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" style="width:150px;">
								<label style="display:block;text-align:center;margin-bottom:0px;">所属仓库</label>
							</td>
							<td style="text-align:center;" colspan="2">
								<div class="xf-handler">
									<select id="wareHouse" onchange="getwareHouse()" class="width:98%;line-height:30px;">
										<option value="">请选择</option>
									</select>
									<input type="hidden" id="wareHouseVal" name="wareHouse" value="">
								</div>
							</td>
							<td id="xf-2-0-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" colspan="2">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;"><font color="red">*</font>申请换货日期</label>
								</div>
							</td>
							<td id="xf-2-0-2" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" style="width:260px;">
								<div class="xf-handler">
									<input type="hidden" id="exchangeDate" name="exchangeDate" style="width:100%;" >
								</div>
								<label id="lbl_exchangeDate" style="font-weight:normal"></label>
							</td>
							<td id="xf-2-0-3" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" style="">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">客服工号</label>
								</div>
							</td>
							<td style="text-align:center;" colspan="2">
								<div class="xf-handler">
									<input type="hidden" id="empNo" name="empNo" style="border:0px" readonly>
								</div>
								<label id="lbl_empNo" style="font-weight:normal"></label>
							</td>
						</tr>
						<tr id="xf-2-1">
							<td id="xf-2-1-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;"><font color='red'>*</font>&nbsp;专卖店编号/手机号</label>
								</div>
							</td>
							<td id="xf-2-1-1" class="xf-cell xf-cell-right xf-cell-bottom" colspan="2">
								<div class="xf-handler">
									<input type="text" id=ucode name="ucode" style="width:100%;" maxlength="11" onkeyup="var reg = /^[0-9]\d*$/; if(!reg.test(this.value)) this.value = '';">
								</div>
							</td>
							<td id="xf-2-1-2" class="xf-cell xf-cell-right xf-cell-bottom" colspan="2">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;"><font color="red">*</font>姓名</label>
								</div>
							</td>
							<td id="xf-2-1-3" class="xf-cell xf-cell-right xf-cell-bottom">
								<div class="xf-handler" >
									<input type="text" id="name" name="name" style="width:100%;" readonly>
								</div>
							</td>
							<td id="xf-2-1-4" class="xf-cell xf-cell-right xf-cell-bottom">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">电话</label>
								</div>
							</td>
							<td id="xf-2-1-5" class="xf-cell xf-cell-right xf-cell-bottom" colspan="2">
								<div class="xf-handler" >
									<input type="text" id="tel" name="tel" style="width:100%;" readonly>
								</div>
							</td>
						</tr>
						<tr id="xf-2-2">
							<td id="xf-2-2-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;"><font color="red">*</font>订单编号</label>
								</div>
							</td>
							<td id="xf-2-2-1" class="xf-cell xf-cell-right xf-cell-bottom" colspan="8">
								<div class="xf-handler">
									<input required id="orderNumber" name="orderNumber" style="width:100%;cursor:default;" type="text" placeholder="多个订单号请用英文逗号分开">
								</div>
							</td>
						</tr>
						<tbody id="form-tfoots">
							<tr id="xf-2-3">
							<td id="xf-2-3-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left" rowspan="3">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;"><font color='red'>*</font>&nbsp;订货时间</label>
								</div>
							</td>
							<td id="xf-2-3-1" class="xf-cell xf-cell-right xf-cell-bottom" colspan="5" rowspan="3">
								<div class="xf-handler">
									<div id="pickerTime" class="input-group  datepickerend date">
									    <input type="text" name="orderTime" id="orderTime" required style="" autocomplete="off">
									    <span id="calendar" class="input-group-addon">
											<i class="glyphicon glyphicon-calendar"></i>
										</span>
							    	</div>
								</div>
							</td>
							<td id="xf-2-3-2" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left" rowspan="3">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;"><font color='red'>*</font>&nbsp;手续费</label>
								</div>
							</td>
							<td id="xf-2-3-3" class="xf-cell xf-cell-right xf-cell-bottom" colspan="3">
								<div class="xf-handler">
									<input type="radio" name="payType" value="店支付" id="shopPay">店支付
								</div>
							</td>
							</tr>
							<tr id="xf-2-4">
								<td id="xf-2-4-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" colspan="3">
									<div class="xf-handler">
										<input type="radio" name="payType" value="个人钱包" id="personPay">个人钱包
									</div>
								</td>
							</tr>
							<tr id="xf-2-5">
								<td id="xf-2-5-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" colspan="3">
									<!-- <div class="xf-handler"> -->
										<input type="radio" name="payType" value="减免" id="derate" >减免
										<input type="text" id="inputApplyCode" name="inputApplyCode" style="display:none;" placeholder="请输入受理单号" maxlength="20">
									<!-- </div> -->
								</td>
							</tr>
						</tbody>
						
						<tr id="xf-2-6">
							<td id="xf-2-6-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;"><font color="red">*</font>原收货人姓名</label>
								</div>
							</td>
							<td id="xf-2-6-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" colspan="2">
								<div class="xf-handler">
									<input type="text" id="oldConsignee" name="oldConsignee" style="width:100%;" maxlength="10">
								</div>
							</td>
							<td id="xf-2-6-2" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" colspan="2">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;"><font color="red">*</font>原收货电话</label>
								</div>
							</td>
							<td id="xf-2-6-3" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
								<div class="xf-handler">
									<input type="text" id="oldConsigneeTel" name="oldConsigneeTel" style="width:100%;" maxlength="11" onkeyup="var reg = /^[0-9]\d*$/; if(!reg.test(this.value)) this.value = '';">
								</div>
							</td>
							<td id="xf-2-6-4" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;"><font color="red">*</font>邮编</label>
								</div>
							</td>
							<td id="xf-2-6-5" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" colspan="2">
								<div class="xf-handler">
									<input type="text" id="zipCode" name="zipCode" style="width:100%;" maxlength="6" onkeyup="var reg = /^[0-9]\d*$/; if(!reg.test(this.value)) this.value = ''; ">
								</div>
							</td>
						</tr>
						<tr id="xf-2-7">
							<td id="xf-2-7-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;"><font color="red">*</font>原收货地址</label>
								</div>
							</td>
							<td id="xf-2-7-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" colspan="8">
								<div class="xf-handler">
									<input type="text" id="oldConsigneeAddress" name="oldConsigneeAddress" style="width:100%;" maxlength="250">
								</div>
							</td>
						</tr>
						</thead>
						<tbody id="result-tbody">
							<tr>
								<td rowspan='2' style='text-align:center;'>序号</td>
								<td colspan='4' style='text-align:center;'>退回产品清单</td>
								<td colspan='4' style='text-align:center;'>所换产品清单<a href='javascript:' onclick='fnAddColumn()'>[添加一行]</a>&emsp;<a href='javascript:' onclick='fnDelColumn()'>[删除一行]&emsp;</a></td>
							</tr>
							<tr>
								<td style='text-align:center;'>产品名称</td>
								<td style='text-align:center;width:80px;'>数量</td>
								<td style='text-align:center;width:80px;'>总金额</td>
								<td style='text-align:center;width:80px;'>总PV</td>
								<td style='text-align:center;'>产品名称</td>
								<td style='text-align:center;width:80px;'>数量</td>
								<td style='text-align:center;width:80px;'>总金额</td>
								<td style='text-align:center;width:80px;'>总PV</td>
							</tr>
							<tbody id="product-tbody"><tr></tr></tbody>
						</tbody>
						<tfoot id="form-tfoot">
							<tr id="xf-2-8">
								<td id="xf-2-8-0">
									<div class="xf-handler">
										<label style="display:block;text-align:center;margin-bottom:0px;"><font color="red">*</font>换货原因</label>
									</div>
								</td>
								<td id="xf-2-8-1" colspan="8">
									<div class="xf-handler">
										<input type="text" id="exchangeReason" name="exchangeReason" style="width:100%;" maxlength="70">
									</div>
								</td>
								
							</tr>
							<tr id="xf-2-9">
								<td id="xf-2-9-0">
									<div class="xf-handler">
										<label style="display:block;text-align:center;margin-bottom:0px;"><font color="red">*</font>更改后收货地址</label>
									</div>
								</td>
								<td id="xf-2-9-1" colspan="8">
									<div class="xf-handler">
										<input type="text" id="newConsigneeAddress" name="newConsigneeAddress" style="width:100%;" maxlength="250">
									</div>
								</td>
							</tr>
							<tr id="xf-2-10">
								<td id="xf-2-10-0">
									<div class="xf-handler">
										<label style="display:block;text-align:center;margin-bottom:0px;"><font color="red">*</font>更改后收货人</label>
									</div>
								</td>
								<td id="xf-2-10-1" colspan="8">
									<div class="xf-handler">
										<input type="text" id="newConsignee" name="newConsignee" maxlength="10">
									</div>
								</td>
							</tr>
							<tr id="xf-2-11">
								<td id="xf-2-11-0">
									<div class="xf-handler">
										<label style="display:block;text-align:center;margin-bottom:0px;"><font color="red">*</font>更改后收货电话</label>
									</div>
								</td>
								<td id="xf-2-11-1" colspan="8">
									<div class="xf-handler">
										<input type="text" id="newConsigneeTel" name="newConsigneeTel" maxlength="11" onkeyup="var reg = /^[0-9]\d*$/; if(!reg.test(this.value)) this.value = '';">
									</div>
								</td>
							</tr>
							<tr id="xf-2-12">
								<td colspan="9" align="center">上传附件（<span style="color:red;"> 请上传一张换货申请人身份证复印件(图片格式) </span>）</td>
							</tr>
							<tr id="xf-2-13">
								<td id="xf-2-13-0" colspan="9">
									<div class="xf-handler">
										<%@include file="/common/_uploadFile.jsp"%>
									</div>
								</td>
							</tr>
							<tr id="xf-2-12">
								<td id="xf-2-12-0" colspan="9">
									<p>换货准则：</p>
									<font color="red">1）订单之日起90天内的订单可以办理换货；</font>
									<font color="red">2）退回产品不拆封、不污染、不影响二次销售；</font>
									<font color="red">3）换回和换出产品需要等值等PV；</font>
									<font color="red">4）办理换货的专卖店店支付需要有50元；</font>
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
		picker();
	});
	function picker(){
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
        calendar.style.position = "relative";
	}
</script>
</html>
