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
<script type="text/javascript" src="${cdnPrefix}/float/FloatOperate.js"></script><!-- 浮点数计算 -->
    <link type="text/css" rel="stylesheet" href="${cdnPrefix}/userpicker3-v2/userpicker.css">
    <script type="text/javascript" src="${cdnPrefix}/userpicker3-v2/userpickerbybpm.js"></script>
	<script type="text/javascript" src="${cdnPrefix}/operation/TaskOperation.js"></script>
	
	<style type="text/css">
		.xf-handler {
			cursor: auto;
			margin:0;
		}
		#xf-section td{border:1px solid gray}
		#form-thead td{border:1px solid gray}
		#form-tfoot td{border:1px solid gray}
		input{border : 1px solid #F2F2F2;}
		select{border : 1px solid #F2F2F2}
		textarea{border : 1px solid #F2F2F2}
		#xtable td{border: 1px solid #B5B5B5;line-height:35px;height:35px;}
		#xtable td .xf-handler {
			cursor: auto;
			margin:0;
		}
		#xtable td input[type='text']{height:35px;width:100%;background:#eee;}
		
		pre {
            white-space: pre-wrap;
            word-wrap: break-word;
            background-color: white;
            border: 0px
        }
	</style>

	<script type="text/javascript">
		var proNoStr = "";
		var serialStr = "";
		document.onmousedown = function(e) {};
		document.onmousemove = function(e) {};
		document.onmouseup = function(e) {};
		document.ondblclick = function(e) {};

		var xform;
		
		//点击获取订单信息
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
		    	                    		 $("#pro").val("");
		     	                    		 return;
		     	                    	 }
		     	                    	 var data=strReturnJson.data;
		     	                    	 if(data.length ==0){
		     	                    		alert("订单没有数据信息，请检查专卖店编号/手机号和订单数据后，重新获取");
		     	                    		 $("#pro").val("");
		     	                    		 return;
		     	                    	 }
		     	                     
				          			 $("#pro").val("已获取产品");
				          			 var total = data.length;
				          			 /* var str = "<tr>"
				          			 		  +"<td rowspan='2' style='text-align:center;'>序号</td><td colspan='4' style='text-align:center;'>退回产品清单</td><td colspan='4' style='text-align:center;'>所换产品清单<a href='javascript:' onclick='fnAddColumn()'>[添加一行]</a>&emsp;<a href='javascript:' onclick='fnDelColumn()'>[删除一行]&emsp;</a></td>"
				          			 		  +"</tr>"
				          			 		  +"<tr>"
				          			 		  +"<td style='text-align:center;'>产品名称</td><td style='text-align:center;'>数量</td><td style='text-align:center;width:80px;'>总金额</td><td style='text-align:center;'>总PV</td><td style='text-align:center;'>产品名称</td><td style='text-align:center;'>数量</td><td style='text-align:center;width:80px;'>总金额</td><td style='text-align:center;'>总PV</td>"
				          			 		  +"</tr>"; */
				          			 var str = "";
				          			 var allTotalPrice=0;
				          			 var allTotalPV=0;
				          			 var allNumber=0;
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
				                    		   "<input type='hidden' id='old"+data[i].proNo+"unitPrice' name='backUnitPrice' value='"+proPrice+"'>"+
				                    		   "<input type='hidden' id='old"+data[i].proNo+"unitPV' name='backUnitPV' value='"+proPV+"'>"+
				                    		   "<input id='back"+data[i].proNo+"' type='hidden' name='backProNo' value='"+data[i].proNo+"'>"+
				                    		   "<input id='max"+data[i].proNo+"number' type='hidden' name='maxNumber' value='"+number+"'>"+//重新发起时存入产品最大数量
				                    		   
				                    		   "<td  class='.td_selectpicker'><select data-live-search='true' class='selectpicker' id='"+parseInt(i+1)+"' onchange='exchangeProduct(this.id)'></select></td>"+
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
				                    	   "<td id='oldAllTotalNumber'>"+allNumber+"</td>"+
				                    	   "<td id='oldAllTotalPrice'>"+allTotalPrice+"</td>"+
				                    	   "<td id='oldAllTotalPV'>"+allTotalPV+"</td>"+
				                    	   "<input type='hidden' id='backAllTotalNumberVal' name='backAllTotalNumberVal'>"+//退货产品总合计value
				                    	   "<input type='hidden' id='backAllTotalPriceVal' name='backAllTotalPriceVal'>"+
				                    	   "<input type='hidden' id='backAllTotalPVVal' name='backAllTotalPVVal'>"+
				                    	   "<td></td>"+
				                    	   "<td id='AllTotalNumber'></td>"+
				                    	   "<td id='AllTotalPrice'></td>"+
				                    	   "<td id='AllTotalPV'></td>"+
				                    	   "<input type='hidden' id='exchangeAllTotalNumberVal' name='exchangeAllTotalNumberVal'>"+//换货产品总合计value
				                    	   "<input type='hidden' id='exchangeAllTotalPriceVal' name='exchangeAllTotalPriceVal'>"+
				                    	   "<input type='hidden' id='exchangeAllTotalPVVal' name='exchangeAllTotalPVVal'>"+
				                    	   "</tr>";
		                    	   	$("#product-tbody").html(str);
				                    }
				          		);
				}else{
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
		    	                    		 $("#name").val("");
		    	                    		 $("#tel").val("");
		    	                    		 $("#pro").val("");
		     	                    		 return;
		     	                    	 }
		     	                    	 var data=strReturnJson.data;
		     	                    	 if(data.length ==0){
		     	                    		alert("订单没有数据信息，请检查专卖店编号/手机号和订单数据后，重新获取");
		     	                    		 $("#name").val("");
		     	                    		 $("#tel").val("");
		     	                    		 $("#pro").val("");
		     	                    		 return;
		     	                    	 }
		     	                     
				          			 $("#name").val(data[0].shopName);
				          			 $("#tel").val(data[0].shopTel);
				          			 $("#pro").val("已获取产品");
				          			 var total = data.length;
				          			 /* var str = "<tr>"
				          			 		  +"<td rowspan='2' style='text-align:center;'>序号</td><td colspan='4' style='text-align:center;'>退回产品清单</td><td colspan='4' style='text-align:center;'>所换产品清单<a href='javascript:' onclick='fnAddColumn()'>[添加一行]</a>&emsp;<a href='javascript:' onclick='fnDelColumn()'>[删除一行]&emsp;</a></td>"
				          			 		  +"</tr>"
				          			 		  +"<tr>"
				          			 		  +"<td style='text-align:center;'>产品名称</td><td style='text-align:center;'>数量</td><td style='text-align:center;width:80px;'>总金额</td><td style='text-align:center;'>总PV</td><td style='text-align:center;'>产品名称</td><td style='text-align:center;'>数量</td><td style='text-align:center;width:80px;'>总金额</td><td style='text-align:center;'>总PV</td>"
				          			 		  +"</tr>"; */
				          			 var str = "";
				          			 var allTotalPrice=0;
				          			 var allTotalPV=0;
				          			 var allNumber=0;
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
				                    		   "<td width='60px'><input type='text' id='old"+data[i].proNo+"number' onkeyup='exchangeResult(this.id,"+number+")' value='"+number+"'</td>"+
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
				                    		   "<input id='max"+data[i].proNo+"number' type='hidden' name='maxNumber' value='"+number+"'>"+//产品最大数量
				                    		   
				                    		   "<td  class='.td_selectpicker'><select data-live-search='true' class='selectpicker' id='"+parseInt(i+1)+"' onchange='exchangeProduct(this.id)'></select></td>"+
				                    		   "<td width='60px'><input id='"+parseInt(i+1)+"number' type='text' style='width:100%' onkeyup='exchangeResultDetail(this.id)' value='0'></td>"+
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
				                    	   "<td id='oldAllTotalNumber'>"+allNumber+"</td>"+
				                    	   "<td id='oldAllTotalPrice'>"+allTotalPrice+"</td>"+
				                    	   "<td id='oldAllTotalPV'>"+allTotalPV+"</td>"+
				                    	   "<input type='hidden' id='backAllTotalNumberVal' name='backAllTotalNumberVal'>"+//退货产品总合计value
				                    	   "<input type='hidden' id='backAllTotalPriceVal' name='backAllTotalPriceVal'>"+
				                    	   "<input type='hidden' id='backAllTotalPVVal' name='backAllTotalPVVal'>"+
				                    	   "<td></td>"+
				                    	   "<td id='AllTotalNumber'></td>"+
				                    	   "<td id='AllTotalPrice'></td>"+
				                    	   "<td id='AllTotalPV'></td>"+
				                    	   "<input type='hidden' id='exchangeAllTotalNumberVal' name='exchangeAllTotalNumberVal'>"+//换货产品总合计value
				                    	   "<input type='hidden' id='exchangeAllTotalPriceVal' name='exchangeAllTotalPriceVal'>"+
				                    	   "<input type='hidden' id='exchangeAllTotalPVVal' name='exchangeAllTotalPVVal'>"+
				                    	   "</tr>";
		                    	   	$("#product-tbody").html(str);
			                    }
			          		);
				}
				
				
			});
			
			//$.weeboxs.open('正在获取数据，请稍等...', { title: '提示', type: 'alert', showButton: false });
			
			
			
			 
			 loading.modal('hide');
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
		function getExchangeSelected(product,proNo,price,pv){
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
							$("#"+product+" option[value='"+proNo+","+pv+","+price+"']").attr("selected","selected");
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
							$("#"+product+" option[value='"+proNo+","+pv+","+price+"']").attr("selected","selected");
				});	
			}
		}
		function exchangeResult(id,maxNum){
			var value = $("#"+id).val();
			if(value == ""){
				$("#"+id).val(0);
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
				value = 0;
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
			}
			//合计总金额转小数
			allTotalPrice = returnFloat(allTotalPrice);
			allTotalPV = returnFloat(allTotalPV);
			$("#oldAllTotalPrice").html(allTotalPrice);
			$("#oldAllTotalPV").html(allTotalPV);
			$("#oldAllTotalNumber").html(allTotalNumber);
			//s退货合计总
			$("#backAllTotalNumberVal").val(allTotalNumber);
			$("#backAllTotalPriceVal").val(allTotalPrice);
			$("#backAllTotalPVVal").val(allTotalPV);
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
				$("#"+id+"proNo").val(text);
				$("#"+id+"unitPrice").val(0);//赋值给隐藏的input
				$("#"+id+"unitPV").val(0);
				$("#"+id+"price").html(0);//所换产品清单的总金额和总PV默认先赋值单价的
				$("#"+id+"PV").html(0);
				$("#"+id+"number").val(0);
				
				//赋值给传值的input
				$("#exchange"+id+"proName").val(text);
				$("#exchange"+id+"number").val(1);
				$("#exchange"+id+"totalPV").val(array[1]);
				$("#exchange"+id+"totalPrice").val(array[2]);
    		}else{
    			$("#"+id+"proNo").val(array[0]);
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
				number = 0;
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
			
			/* $("#oldAllTotalPrice").html(allTotalPrice);
			$("#oldAllTotalPV").html(allTotalPV);
			$("#oldAllTotalNumber").html(allTotalNumber); */
			
			//换货合计（总）
			$("#exchangeAllTotalNumberVal").val(allTotalNumber);
			$("#exchangeAllTotalPriceVal").val(allTotalPrice);
			$("#exchangeAllTotalPVVal").val(allTotalPV);
		}

		//驳回后接收自己当时提交的主表数据
		$(function() {
		 	var id= $("#processInstanceId").val();
				if (id !="") {
			    	$.getJSON('${tenantPrefix}/rs/Exchange/getExchangeInfo', {
			        	id: id
			        	}, function(data) {
			            	for (var i = 0; i < data.length; i++) {
			            		$("#applyCode").val(data[i].applyCode);
			            		$("#wareHouse").html(data[i].wareHouse);
			                    $("#wareHouseVal").val(data[i].wareHouse);//用于调整申请传值
			            		$("#exchangeDate").val(data[i].exchangeDate);
			            		$("#lbl_exchangeDate").html(data[i].exchangeDate);
			                   $("#empNo").val(data[i].empNo);
			                   $("#lbl_empNo").html(data[i].empNo);
			                   $("#ucode").val(data[i].ucode);
			                   $("#name").val(data[i].name);
			                   $("#tel").val(data[i].tel);
			                   $("#ucodes").html(data[i].ucode);
			                   $("#names").html(data[i].name);
			                   
			                   
			                   $("#orderNumber").val(data[i].orderNumber);
			                   
			                   $("#exchangeReason").val(data[i].exchangeReason);
			                   $("#oldConsignee").val(data[i].oldConsignee);
			                   $("#oldConsigneeTel").val(data[i].oldConsigneeTel);
			                   $("#oldConsigneeAddress").val(data[i].oldConsigneeAddress);
			                   $("#zipCode").val(data[i].zipCode);
			                   $("#newConsignee").val(data[i].newConsignee);
			                   $("#newConsigneeTel").val(data[i].newConsigneeTel);
			                   $("#newConsigneeAddress").val(data[i].newConsigneeAddress);
			                   
			                   if(data[i].ucode.length == 11){
			                	   changeMallDiv();
			                	   $("#tels").html("无");
			                	   $("#orderTime").val(data[i].orderTime);
				                   if(data[i].payType == "减免"){
				                	   $("#derate").attr("checked",true);
				                	   $("#inputApplyCode").attr("type","text");
				                	   $("#inputApplyCode").val(data[i].inputApplyCode);
				                	   $("#inputApplyCode").removeAttr("style");
				                   }
				                   if(data[i].payType == "贷款中扣除" || data[i].payType == "已支付" || data[i].payType == "已转账"){
				                	   $("#mallPay").attr("checked",true);
				                   }
				                   if(data[i].payType == "店支付"){
				                	   $("#mallShopPay").attr("checked",true);
				                   }
			                   }else{
			                	   changeOaDiv();
			                	   $("#tels").html(data[i].tel);
			                	   $("#orderTime").val(data[i].orderTime);
				                   if(data[i].payType == "店支付"){
				                	   $("#shopPay").attr("checked",true);
				                   }
				                   if(data[i].payType == "个人钱包"){
				                	   $("#personPay").attr("checked",true);
				                   }
				                   if(data[i].payType == "减免"){
				                	   $("#derate").attr("checked",true);
				                	   $("#inputApplyCode").attr("type","text");
				                	   $("#inputApplyCode").val(data[i].inputApplyCode);
				                	   $("#inputApplyCode").removeAttr("style");
				                   }
				                  
			                   }
			                   
			                   $("#processInstanceId").val(data[i].processInstanceId);
			                   $("#exchangeId").val(data[i].id);
			                   
	                    }
			            	
			            
			            var str = "";
			            //获取换货产品数据
			            $.getJSON('${tenantPrefix}/rs/Exchange/getProductList',
			            		{id:id},function(data){
			            			//console.log(data);
			            			var typeTemp = 0;
			            			if(data[0].length > data[1].length)
			            				typeTemp = data[0].length;
			            			else
			            				typeTemp = data[1].length;
			            			//退货产品清单合计
			            			var returnNumber = 0;
			            			var returnAllTotalPrice = 0;
			            			var returnAllTotalPv = 0;
			            			//换货产品清单合计
			            			var exchangeNumber = 0;
			            			var exchangeAllTotalPrice = 0;
			            			var exchangeAllTotalPv = 0;
			            			for(var i=0;i<typeTemp;i++){
			            				//获取商品的产品编号，用于计算使用
				                    	if(i < typeTemp-1){
				                    		proNoStr += data[0][i].productNo+",";
				                    	}else{
				                    		proNoStr += data[0][i].productNo;
				                    	}
				                    	//获取商品的产品编号，用于计算使用
				                    	if(i < typeTemp-1){
				                    		serialStr += parseInt(i+1)+",";
				                    	}else{
				                    		serialStr += parseInt(i+1);
				                    	}
			            				str += "<tr>"+
			                    		   "<td style='text-align:center;'>"+parseInt(i+1)+"</td>";
			            				if(i < data[0].length){
			            					var number = data[0][i].maxProductNum;
			            					returnNumber += parseInt(data[0][i].productNum);
 			            					returnAllTotalPrice += parseFloat(data[0][i].totalPrice);
 			            					returnAllTotalPv += parseFloat(data[0][i].totalPv);
					            			str += 
				                    		   "<td>"+data[0][i].productName+"</td>"+
				                    		   "<td width='60px'><input type='text' id='old"+data[0][i].productNo+"number' onkeyup='exchangeResult(this.id,"+number+")' value='"+data[0][i].productNum+"'</td>"+
				                    		   "<td id='old"+data[0][i].productNo+"totalPrice'>"+data[0][i].totalPrice+"</td>"+
				                    		   "<td id='old"+data[0][i].productNo+"totalPV'>"+data[0][i].totalPv+"</td>"+
				                    		   "<input type='hidden' name='backType' value='0'>"+//以下至空格行为要传的参数
				                    		   "<input type='hidden' name='backProName' value='"+data[0][i].productName+"'>"+
				                    		   "<input id='back"+data[0][i].productNo+"number' type='hidden' name='backNumber' value='"+data[0][i].productNum+"'>"+
				                    		   "<input id='back"+data[0][i].productNo+"totalPrice' type='hidden' name='backTotalPrice' value='"+data[0][i].totalPrice+"'>"+
				                    		   "<input id='back"+data[0][i].productNo+"totalPV' type='hidden' name='backTotalPV' value='"+data[0][i].totalPv+"'>"+
				                    		   "<input type='hidden' id='old"+data[0][i].productNo+"unitPrice' name='backUnitPrice' value='"+data[0][i].price+"'>"+
				                    		   "<input type='hidden' id='old"+data[0][i].productNo+"unitPV' name='backUnitPV' value='"+data[0][i].pv+"'>"+
				                    		   "<input id='back"+data[0][i].productNo+"' type='hidden' name='backProNo' value='"+data[0][i].productNo+"'>"+
				                    		   "<input id='max"+data[0][i].productNo+"number' type='hidden' name='maxNumber' value='"+number+"'>";//产品最大数量
			            				}else{
			            					str += 
			            						"<td></td>"+
			            						"<td></td>"+
			            						"<td></td>"+
			            						"<td></td>";
			            				}
			            				if(i <data[1].length){
			            					exchangeNumber += parseInt(data[1][i].productNum);
			            					exchangeAllTotalPrice += parseFloat(data[1][i].totalPrice);
			            					exchangeAllTotalPv += parseFloat(data[1][i].totalPv);
			            					str += 
			            						"<td  class='.td_selectpicker'><select data-live-search='true' class='selectpicker' id='"+parseInt(i+1)+"' onchange='exchangeProduct(this.id)'></select></td>"+
				                    		   "<td width='60px'><input id='"+parseInt(i+1)+"number' type='text' style='width:100%' onkeyup='exchangeResultDetail(this.id)' value='"+data[1][i].productNum+"'></td>"+
				                    		   "<td id='"+parseInt(i+1)+"price'>"+data[1][i].totalPrice+"</td>"+
				                    		   "<td id='"+parseInt(i+1)+"PV'>"+data[1][i].totalPv+"</td>"+
				                    		   "<input type='hidden' name='exchangeType' value='1'>"+//以下至<tr>为要传的参数
				                    		   "<input type='hidden' id='exchange"+parseInt(i+1)+"proName' name='exchangeProName' value='"+data[1][i].productName+"'>"+
				                    		   "<input type='hidden' id='exchange"+parseInt(i+1)+"number' name='exchangeNumber' value='"+data[1][i].productNum+"'>"+
				                    		   "<input type='hidden' id='exchange"+parseInt(i+1)+"totalPrice' name='exchangeTotalPrice' value='"+data[1][i].totalPrice+"'>"+
				                    		   "<input type='hidden' id='exchange"+parseInt(i+1)+"totalPV' name='exchangeTotalPV' value='"+data[1][i].totalPv+"'>"+
				                    		   "<input type='hidden' id='"+parseInt(i+1)+"unitPrice' name='exchangeUnitPrice' value='"+data[1][i].price+"'>"+
				                    		   "<input type='hidden' id='"+parseInt(i+1)+"unitPV' name='exchangeUnitPV' value='"+data[1][i].pv+"'>"+
				                    		   "<input type='hidden' id='"+parseInt(i+1)+"proNo' name='exchangeProNo' value='"+data[1][i].productNo+"'>";
			            						
			            				}else{
			            					   str +=
			            						   "<td  class='.td_selectpicker'><select data-live-search='true' class='selectpicker' id='"+parseInt(i+1)+"' onchange='exchangeProduct(this.id)'></select></td>"+
					                    		   "<td width='60px'><input id='"+parseInt(i+1)+"number' type='text' style='width:100%' onkeyup='exchangeResultDetail(this.id)' value='0'></td>"+
					                    		   "<td id='"+parseInt(i+1)+"price'>0</td>"+
					                    		   "<td id='"+parseInt(i+1)+"PV'>0</td>"+
					                    		   "<input type='hidden' name='exchangeType' value='1'>"+//以下至<tr>为要传的参数
					                    		   "<input type='hidden' id='exchange"+parseInt(i+1)+"proName' name='exchangeProName' value='请选择'>"+
					                    		   "<input type='hidden' id='exchange"+parseInt(i+1)+"number' name='exchangeNumber' value='0'>"+
					                    		   "<input type='hidden' id='exchange"+parseInt(i+1)+"totalPrice' name='exchangeTotalPrice' value='0'>"+
					                    		   "<input type='hidden' id='exchange"+parseInt(i+1)+"totalPV' name='exchangeTotalPV' value='0'>"+
					                    		   "<input type='hidden' id='"+parseInt(i+1)+"unitPrice' name='exchangeUnitPrice' value='0'>"+
					                    		   "<input type='hidden' id='"+parseInt(i+1)+"unitPV' name='exchangeUnitPV' value='0'>"+
					                    		   "<input type='hidden' id='"+parseInt(i+1)+"proNo' name='exchangeProNo' value='请选择'>";
			            				}
			            				str += "</tr>"; 
			            				if(i < data[1].length){
			            					getExchangeSelected(parseInt(i+1),data[1][i].productNo,data[1][i].price,data[1][i].pv);
			            				}else{
			            					getExchange(parseInt(i+1));
			            				}
			            				
			            			}
			            			returnAllTotalPrice = returnFloat(returnAllTotalPrice);
		            				returnAllTotalPv = returnFloat(returnAllTotalPv);
		            				
		            				exchangeAllTotalPrice = returnFloat(exchangeAllTotalPrice);
		            				exchangeAllTotalPv = returnFloat(exchangeAllTotalPv);
		            				
			            			str += "<tr>"+
			                    	   "<td style='text-align:center;'>合计</td>"+
			                    	   "<td></td>"+
			                    	   "<td id='oldAllTotalNumber'>"+returnNumber+"</td>"+
			                    	   "<td id='oldAllTotalPrice'>"+returnAllTotalPrice+"</td>"+
			                    	   "<td id='oldAllTotalPV'>"+returnAllTotalPv+"</td>"+
			                    	   "<td></td>"+
			                    	   "<td id='AllTotalNumber'>"+exchangeNumber+"</td>"+
			                    	   "<td id='AllTotalPrice'>"+exchangeAllTotalPrice+"</td>"+
			                    	   "<td id='AllTotalPV'>"+exchangeAllTotalPv+"</td>"+
			                    	   "</tr>";
	                    	   	$("#product-tbody").html(str);
			           });
			            
	                });
			    	
					  $("#xtable :radio").click(function(){
					   	var type = $(this).val();
					   	if(type == '减免'){
					   		$("#inputApplyCode").attr("type","text");
					   	}else{
					   		$("#inputApplyCode").attr("type","hidden");
					   	}
					  });
	         };//if结尾

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
			if(flag == 3){
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
				if(flag == 3 && $("#orderNumber").val().length > "200"){
					alert("订单单据号过多");
					return
				}
				
				if($("#zipCode").val() == ''){
					alert("请填写邮编");
					return;
				}
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
	            
	            if($("#AllTotalNumber").html()==0 || $("#AllTotalNumber").html()== ""){
					alert("所换产品清单未选择产品");
					return;
				}
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
	    			
	    			$('#xform').attr('action', '${tenantPrefix}/Exchange/process-operationExchangeApproval-completeTask.do?flag='+flag);
	    			$('#xform').submit();
	            },      
	            error: function(e){      
	            	loading.modal('hide');
	                alert("服务器请求失败,请重试"); 
	            }
	       });
		}

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
								+'   	<input type="radio" name="payType" value="店支付" id="mallShopPay">店支付'
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
<form id="xform" method="post" action="${tenantPrefix}/Exchange/process-operationExchangeApproval-completeTask.do" class="xf-form" enctype="multipart/form-data" style="AutoScrool:true">
    <div class="container">

	  <!-- start of main -->
      <section id="m-main" class="col-md-12" style="padding-top:65px;">
      	<%-- <input id="processDefinitionId" type="text" name="processDefinitionId" value="<%= request.getParameter("processDefinitionId")%>">
      	<input id="bpmProcessId" type="text" name="bpmProcessId" value="<%= request.getParameter("bpmProcessId")%>">
      	<input id="businessKey" type="text" name="businessKey" value="<%= request.getParameter("businessKey")%>"> --%>
		<input id="processInstanceId" type="hidden" name="processInstanceId" value="${processInstanceId}">
		<input id="humanTaskId" type="hidden"  name="humanTaskId" value="${humanTaskId}">
		<input id="activityId" type="hidden" name="activityId" value="">
		<input id="exchangeId" name="exchangeId" type="hidden">
		<input id="pro"  type="hidden">
		<input id="userId" type="hidden" name="userId" value="<%=userId %>">
		<input id="applyCode" type="hidden" name="applyCode">
		<!-- <input style="border:0px;width:10px" readonly type="hidden" id="submitTimes" name="submitTimes"> -->
		
		<div id="xf-form-table">
			<div id="xf-1" class="xf-section">
				<h1 style="text-align:center;">换货调整单</h1>
			</div>

			<div id="xf-2" class="xf-section">
				<table id="xtable" class="xf-table" style="width:100%" cellspacing="0" cellpadding="0" border="1" align="center">
					<thead id="form-thead">
						<tr id="xf-2-0">
							<td id="xf-2-0-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" style="width:150px;">
								<label style="display:block;text-align:center;margin-bottom:0px;">所属仓库</label>
							</td>
							<td style="text-align:center;" colspan="2">
								<div class="xf-handler">
									<select class="width:98%;line-height:30px;">
										<option id="wareHouse" value=""></option>
									</select>
									<input type="hidden" id="wareHouseVal" name="wareHouse">
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
							<td id="xf-2-0-3" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">客服工号</label>
								</div>
							</td>
							<td style="text-align:center;" colspan="2" >
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
								<div class="xf-handler" id=ucodes>
									
								</div>
								<input type="hidden" id=ucode name="ucode" style="width:100%;" readonly>
							</td>
							<td id="xf-2-1-2" class="xf-cell xf-cell-right xf-cell-bottom" colspan="2">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;"><font color="red">*</font>姓名</label>
								</div>
							</td>
							<td id="xf-2-1-3" class="xf-cell xf-cell-right xf-cell-bottom">
								<div class="xf-handler" id="names">
									
								</div>
								<input type="hidden" id="name" name="name" style="width:100%;" readonly>
							</td>
							<td id="xf-2-1-4" class="xf-cell xf-cell-right xf-cell-bottom">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">电话</label>
								</div>
							</td>
							<td id="xf-2-1-5" class="xf-cell xf-cell-right xf-cell-bottom" colspan="2">
								<div class="xf-handler" id="tels">
									
								</div>
								<input type="hidden" id="tel" name="tel" style="width:100%;" readonly>
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
									<input  required id="orderNumber" name="orderNumber" style="width:100%;cursor:default;" type="text" placeholder="多个订单号请用英文逗号分开">
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
								<div class="xf-handler">
									<input type="radio" name="payType" value="减免" id="derate" >减免
									<input type="hidden" id="inputApplyCode" name="inputApplyCode"  placeholder="请输入受理单号" maxlength="20">
								</div>
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
									<input type="text" id="zipCode" name="zipCode" style="width:100%;" maxlength="6" onkeyup="var reg = /^[0-9]\d*$/; if(!reg.test(this.value)) this.value = '';">
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
								<td id="xf-2-12-0" align="center" colspan="9">上传附件（<span style="color:red;"> 请上传一张换货申请人身份证复印件 </span>）</td>
							</tr>
							<tr id="xf-2-13">
								<td id="xf-2-13-0" colspan="9">
									<div class="xf-handler">
										<%@include file="/common/_uploadFile.jsp"%>
									</div>
								</td>
							</tr>
							<tr id="xf-2-14">
								<td id="xf-2-14-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
									<div class="xf-handler">
										<label style="display:block;text-align:center;margin-bottom:0px;">历史附件</label>
									</div>
								</td>
								<td id="xf-2-14-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" colspan="8">
									<div class="col-md-8">
	                            		<%@include file="/common/show_edit_file.jsp" %>
	                        		</div>
								</td>
							</tr>
							<tr id="xf-2-15">
								<td id="xf-2-15-0" colspan="9">
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
			<div>
			</div>
		</div>
			 
		<br/><br/><br/><br/>
    </section>
	<!-- end of main -->
	<table width="100%" cellspacing="0" cellpadding="0" border="0" align="center" class="table table-border">
				    <tr>
					  <th>环节</th>
					  <th>操作人</th>
					  <th>时间</th>
					  <th>结果</th>
					  <th>审核时长</th>
					</tr>
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
						<tr style="border-top:0px text;">
							<td>批示内容</td>
							<td colspan="4"><pre>${item.comment}</pre></td>
						</tr>
					</c:if>
					  </c:if>
					  </c:forEach>
				</table>
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
