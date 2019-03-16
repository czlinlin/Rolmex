<%@page contentType="text/html;charset=UTF-8" %>
<%@include file="/taglibs.jsp" %>
<%pageContext.setAttribute("currentHeader", "bpm-workspace");%>
<%pageContext.setAttribute("currentMenu", "bpm-process");%>

<!doctype html>
<html lang="en">

<head>
    <%@include file="/common/meta.jsp" %>
    <title><spring:message code="demo.demo.input.title" text="麦联"/></title>
    <%@include file="/common/s3.jsp" %>

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

        pre {
            white-space: pre-wrap;
            word-wrap: break-word;
            background-color: white;
            border: 0px
        }
    </style>

    <script type="text/javascript">
        document.onmousedown = function (e) {
        };
        document.onmousemove = function (e) {
        };
        document.onmouseup = function (e) {
        };
        document.ondblclick = function (e) {
        };

        var xform;

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
         })

         ROOT_URL = '${tenantPrefix}';
         taskOperation = new TaskOperation(); */


        //相关button操作
        function completeTask(flag) {
            if ($("#comment").val().replace(/(^\s*)|(\s*$)/g, "") == "" && flag == 0 || $("#comment").val().replace(/(^\s*)|(\s*$)/g, "") == "" && flag == 2
                || $("#comment").val() == "同意" && flag == 0 || $("#comment").val() == "同意" && flag == 2) {
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
	            	 var conf = {
                         "formId": "xform",
                         "checkUrl": "${tenantPrefix}/rs/customer/opteraion-verifyPassword",
                         "actionUrl": '${tenantPrefix}/Invoice/process-operationInvoiceApproval-completeTask.do?flag=' + flag,
                         "iptPwdId": "txtPrivateKey"
                     }
                     operationSubmit(conf);
	            },      
	            error: function(e){      
	            	loading.modal('hide');
	                alert("服务器请求失败,请重试");  
	            }
	       });
        }
        //接收申请单的数据
        $(function () {
            var id = $("#processInstanceId").val();
            if (id != "") {

                $.getJSON('${tenantPrefix}/rs/Invoice/getInvoiceInfo', {
                    id: id
                }, function (data) {
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
                        $("#person").html("个人");
                        $("#public").html("对公");


                        if (data[i].category == "个人") {
                            //$("#person").html(data[i].category);
                            //$("#person").attr("checked",true);
                            $("#invoiceTitlePerson").html(data[i].invoiceTitle);
                            $("#invoiceDetailPerson").html(data[i].invoiceDetail);
                            $("#invoiceMoneyPerson").html(data[i].invoiceMoney);
                            //发票类别是个人时，给对公的输入框输入空数值
                            //$("#public").attr("checked",false);

                        } else if (data[i].category == "对公") {
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
            }
            ;
        });

        function empty() {
            if ($("#comment").val() == "同意") {
                $("#comment").val('');
            }
        }
        function getAgree() {
            if ($("#comment").val() == "") {
                $("#comment").val('同意');
            }
        }
        
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
<%@include file="/header/bpm-workspace3.jsp" %>
<form id="xform" method="post" class="xf-form" enctype="multipart/form-data">
    <div class="container">

        <!-- start of main -->
        <section id="m-main" class="col-md-12" style="padding-top:65px;">
            <input id="processInstanceId" type="hidden" name="processInstanceId" value="${processInstanceId}">
            <input id="humanTaskId" type="hidden" name="humanTaskId" value="${humanTaskId}">
            <input id="autoCompleteFirstTask" type="hidden" name="autoCompleteFirstTask" value="false">
            <input id="activityId" type="hidden" name="activityId" value="">
            <input id="userId" type="hidden" name="userId" value="<%=userId %>">

            <div id="xf-form-table">
                <div id="xf-1" class="xf-section">
                    <h1 id="h1Id" style="text-align:center;">发票审批单</h1>
                </div>

                <div id="xf-2" class="xf-section">
                    <table class="xf-table" width="100%" cellspacing="0" cellpadding="0" border="0" align="center">
                        <tbody>
                        <tr id="xf-2-0">
                            <td id="xf-2-0-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left"
                                width="25%">
                                <div class="xf-handler">
                                    <label style="display:block;text-align:center;margin-bottom:0px;">专卖店编号/手机号</label>
                                </div>
                            </td>
                            <td id="xf-2-0-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left"
                                width="25%">
                                <div class="xf-handler">
                                    <label style="display:block;text-align:center;margin-bottom:0px;">专卖店姓名</label>
                                </div>
                            </td>
                            <td id="xf-2-0-2" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top" width="25%">
                                <div class="xf-handler">
                                    <label style="display:block;text-align:center;margin-bottom:0px;">专卖店电话</label>
                                </div>
                            </td>
                            <td id="xf-2-0-3" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top" width="25%">
                                <div class="xf-handler">
                                    <label style="display:block;text-align:center;margin-bottom:0px;">申请发票日期</label>
                                </div>
                            </td>
                        </tr>
                        <tr id="xf-2-1">
                            <td id="xf-2-1-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left" width="25%">
                                <div class="xf-handler" align="center" id="ucode">
                                </div>
                            </td>
                            <td id="xf-2-1-1" class="xf-cell xf-cell-right xf-cell-bottom" width="25%">
                                <div class="xf-handler" align="center" id="shopName">
                                </div>
                            </td>
                            <td id="xf-2-1-2" class="xf-cell xf-cell-right xf-cell-bottom" width="25%">
                                <div class="xf-handler" align="center" id="shopTel">
                                </div>
                            </td>
                            <td id="xf-2-1-3" class="xf-cell xf-cell-right xf-cell-bottom" width="25%">
                                <div class="xf-handler" align="center" id="applyDate">
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
                                <div class="xf-handler" id="orderNumber">
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
                                <div class="xf-handler" align="center" id="area">
                                </div>
                            </td>
                            <td id="xf-2-3-2" class="xf-cell xf-cell-right xf-cell-bottom" width="25%">
                                <div class="xf-handler">
                                    <label style="display:block;text-align:center;margin-bottom:0px;">所属体系</label>
                                </div>
                            </td>
                            <td id="xf-2-3-3" class="xf-cell xf-cell-right xf-cell-bottom" width="25%">
                                <div class="xf-handler" align="center" id="system">
                                </div>
                            </td>
                        </tr>
                        <tr id="xf-2-4">
                            <td id="xf-2-4-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left"
                                width="25%">
                                <div class="xf-handler">
                                    <label style="display:block;text-align:center;margin-bottom:0px;">所属分公司</label>
                                </div>
                            </td>
                            <td id="xf-2-4-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top" colspan="3"
                                width="25%">
                                <div class="input-group userPicker" style="width: 175px;" id="branchOffice">
                                </div>
                            </td>
                        </tr>
                        </tbody>
                        <tr id="xf-2-5">
                            <td id="xf-2-5-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left"
                                width="25%">
                                <div class="xf-handler">
                                    <label style="display:block;text-align:center;margin-bottom:0px;">发票类型</label>
                                </div>
                            </td>
                            <td id="xf-2-5-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left"
                                width="25%" colspan="3">
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
                            <td id="xf-2-6-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left"
                                width="25%" rowspan="4" align="center">
                                <div class="xf-handler" id="person">
                                    <!-- <input type="radio" id="person" name="category"  value="个人"><label>个人</label> -->
                                </div>
                            </td>
                            <td id="xf-2-6-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left"
                                width="25%">
                                <div class="xf-handler">
                                    <label style="display:block;text-align:center;margin-bottom:0px;">发票抬头</label>
                                </div>
                            </td>
                            <td id="xf-2-6-2" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left"
                                width="50%" colspan="2">
                                <div class="xf-handler" id="invoiceTitlePerson">
                                </div>
                            </td>
                        </tr>
                        <tr id="xf-2-7" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
                            <td id="xf-2-7-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left"
                                width="25%">
                                <div class="xf-handler">
                                    <label style="display:block;text-align:center;margin-bottom:0px;">发票明细(产品名称、价格、数量)</label>
                                </div>
                            </td>
                            <td id="xf-2-7-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left"
                                width="50%" colspan="2">
                                <div class="xf-handler" id="invoiceDetailPerson">
                                </div>
                            </td>
                        </tr>
                        <tr id="xf-2-8" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
                            <td id="xf-2-8-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left"
                                width="25%">
                                <div class="xf-handler">
                                    <label style="display:block;text-align:center;margin-bottom:0px;"> 发票开具总金额</label>
                                </div>
                            </td>
                            <td id="xf-2-8-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left"
                                width="50%" colspan="2">
                                <div class="xf-handler" id="invoiceMoneyPerson">
                                </div>
                            </td>
                        </tr>
                        <tr id="xf-2-9" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
                            <td id="xf-2-9-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left"
                                width="25%">
                                <div class="xf-handler">
                                    <label style="display:block;text-align:center;margin-bottom:0px;"> 身份证号码</label>
                                </div>
                            </td>
                            <td id="xf-2-9-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left"
                                width="75%" colspan="3">
                                <div class="xf-handler" id="idNumber">
                                </div>
                            </td>
                        </tr>
                        <tr id="xf-2-10" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
                            <td id="xf-2-10-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left"
                                align="center" rowspan="8" width="25%">
                                <div class="xf-handler" id="public">
                                </div>
                            </td>
                            <td id="xf-2-10-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left"
                                width="25%">
                                <div class="xf-handler">
                                    <label style="display:block;text-align:center;margin-bottom:0px;">发票抬头</label>
                                </div>
                            </td>
                            <td id="xf-2-10-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left"
                                width="50%" colspan="2">
                                <div class="xf-handler" id="invoiceTitlePublic">
                                </div>
                            </td>
                        </tr>
                        <tr id="xf-2-11" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
                            <td id="xf-2-11-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left"
                                width="25%">
                                <div class="xf-handler">
                                    <label style="display:block;text-align:center;margin-bottom:0px;">发票明细(产品名称、价格、数量)</label>
                                </div>
                            </td>
                            <td id="xf-2-11-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left"
                                width="50%" colspan="2">
                                <div class="xf-handler" id="invoiceDetailPublic">
                                </div>
                            </td>
                        </tr>
                        <tr id="xf-2-12" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
                            <td id="xf-2-12-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left"
                                width="25%">
                                <div class="xf-handler">
                                    <label style="display:block;text-align:center;margin-bottom:0px;"> 发票开具总金额</label>
                                </div>
                            </td>
                            <td id="xf-2-12-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left"
                                width="50%" colspan="2">
                                <div class="xf-handler" id="invoiceMoneyPublic">
                                </div>
                            </td>
                        </tr>
                        <tr id="xf-2-13" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
                            <td id="xf-2-13-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left"
                                width="25%">
                                <div class="xf-handler">
                                    <label style="display:block;text-align:center;margin-bottom:0px;"> 企业名称</label>
                                </div>
                            </td>
                            <td id="xf-2-13-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left"
                                width="50%" colspan="2">
                                <div class="xf-handler" id="enterpriseName">
                                </div>
                            </td>
                        </tr>
                        <tr id="xf-2-14" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
                            <td id="xf-2-14-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left"
                                width="25%">
                                <div class="xf-handler">
                                    <label style="display:block;text-align:center;margin-bottom:0px;"> 税务登记号</label>
                                </div>
                            </td>
                            <td id="xf-2-14-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left"
                                width="50%" colspan="2">
                                <div class="xf-handler" id="taxNumber">
                                </div>
                            </td>
                        </tr>
                        <tr id="xf-2-15" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
                            <td id="xf-2-15-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left"
                                width="25%">
                                <div class="xf-handler">
                                    <label style="display:block;text-align:center;margin-bottom:0px;"> 开户行</label>
                                </div>
                            </td>
                            <td id="xf-2-15-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left"
                                width="50%" colspan="2">
                                <div class="xf-handler" id="openingBank">
                                </div>
                            </td>
                        </tr>
                        <tr id="xf-2-16" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
                            <td id="xf-2-16-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left"
                                width="25%">
                                <div class="xf-handler">
                                    <label style="display:block;text-align:center;margin-bottom:0px;"> 开户行账号</label>
                                </div>
                            </td>
                            <td id="xf-2-16-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left"
                                width="50%" colspan="2">
                                <div class="xf-handler" id="accountNumber">
                                </div>
                            </td>
                        </tr>
                        <tr id="xf-2-17" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
                            <td id="xf-2-17-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left"
                                width="25%">
                                <div class="xf-handler">
                                    <label style="display:block;text-align:center;margin-bottom:0px;"> 企业地址及电话</label>
                                </div>
                            </td>
                            <td id="xf-2-17-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left"
                                width="50%" colspan="2">
                                <div class="xf-handler" id="enterpriseAddress">
                                </div>
                            </td>
                        </tr>

                        <tr id="xf-18">
                            <td id="xf-18-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left"
                                width="25%">
                                <label style="display:block;text-align:center;margin-bottom:0px;">发票邮寄地址</label>
                            </td>
                            <td id="xf-18-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left"
                                colspan="3">
                                <div class="xf-handler" id="invoiceMailAddress">
                                </div>
                            </td>
                        </tr>
                        <tr id="xf-19">
                            <td id="xf-19-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left"
                                colspan="2" width="50%">
                                <label style="display:block;text-align:center;margin-bottom:0px;">收件人姓名</label>
                            </td>
                            <td id="xf-19-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left"
                                width="25%"><label
                                    style="display:block;text-align:center;margin-bottom:0px;">收件人电话</label></td>
                            <td id="xf-19-2" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left"
                                width="25%"><label
                                    style="display:block;text-align:center;margin-bottom:0px;">收件人备用电话</label></td>
                        </tr>

                        <tr id="xf-20">
                            <td id="xf-20-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left"
                                colspan="2">
                                <div class="xf-handler" id="addressee">
                                </div>
                            </td>
                            <td id="xf-20-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
                                <div class="xf-handler" id="addresseeTel">
                                </div>
                            </td>
                            <td id="xf-20-2" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
                                <div class="xf-handler" style="height:20px" id="addresseeSpareTel">
                                </div>
                            </td>
                        </tr>
                        <tr id="xf-21">
                            <td id="xf-21-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left"
                                colspan="4">
                                <label style="display:block;text-align:left;margin-bottom:0px;">注：产品明细可选日用品、化妆品、保健品统称，增值税专用发票除外</label>
                            </td>
                        </tr>
                        <tr id="xf-22">
                            <td id="xf-22-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left"
                                colspan="4">
                                <label style="display:block;margin-bottom:0px;">注：1.普通发票、增值税普通发票可针对个人或对公开具，增值税专用发票仅针对对公开具；2.增值税专用发票必须开具产品明细，其他票据的产品明细可选填日用品、化妆品、保健品统称；3.增值税专用发票开具时请上传对方公司营业执照、税务登记证及开户行许可证电子版；4.不可直接体现在发票中的产品为：罗麦π化负离子健康机、罗麦健康活氧解毒机、罗麦π石链(长)、罗麦π石链、罗麦π水宝、杯芯（8个/盒）、罗麦居家套装锅具（汤锅、炒锅）、罗麦熣燦套锅（砂光）、罗麦熣燦套锅（镜光），如会员订购此类产品，可将其开具为其他产品。</label>
                            </td>
                        </tr>
                        <tr id="xf-24">
                            <td id="xf-24-0" align=center
                                class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
                                <label>附件：</label>
                            </td>
                            <td id="xf-24-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left"
                                colspan="3">
                                <div class="col-md-8">
                                    <%@include file="/common/show_file.jsp" %>
                                </div>
                            </td>
                        </tr>
                        <tr id="xf-23">
                            <td id="xf-23-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
                                <div align=center class="xf-handler" style="margin-top:0px">
                                    <label>审批人意见</label>
                                </div>
                            </td>
                            <td id="xf-23-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left"
                                colspan="3">
                                <div id="opinion">
                                    <textarea id="comment" name="comment" maxlength="300" rows="5" cols="20"
                                              style="width:100%" onfocus="empty()" onblur="getAgree()"
                                              required>同意</textarea>
                                </div>
                            </td>
                        </tr>
                        </tbody>
                    </table>
                    <table>
                        <tr>
                            <td>
                                <div class="xf-handler">
                                    <label style="display:block;text-align:center;margin-bottom:0px;width:100%"><font
                                            style="color:red">*</font>操作密码</label>
                                </div>
                            </td>
                            <td>
                                <div class="xf-handler">
                                    <input name="txtPrivateKey" type="password" maxlength="25" id="txtPrivateKey"
                                           onblur='isPwd();'/>
                                    <input id="isPwdRight" name="isPwdRight" type="hidden"/>
                                </div>
                            </td>
                        </tr>
                    </table>
                    <table width="100%" cellspacing="0" cellpadding="0" border="0" align="center"
                           class="table table-border">
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
                                        <td colspan="4">
                                            <pre>${item.comment}</pre>
                                        </td>
                                    </tr>
                                </c:if>
                            </c:if>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
            </div>
            <br>
        </section>
        <!-- end of main -->
    </div>
    <br/><br/><br/><br/>
    <div class="navbar navbar-default navbar-fixed-bottom">
        <div class="container-fluid">
            <div class="text-center" style="padding-top:8px;">
                <div class="text-center" style="padding-top:8px;">
                    <!-- <button id="saveDraft" class="btn btn-default" type="button" onclick="taskOperation.saveDraft()">保存草稿</button> -->
                    <button onclick="completeTask(1)" class="btn btn-default" type="button">同意</button>
                    <button id="backProcess" class="btn btn-default" onclick="completeTask(2)" type="button">驳回</button>
                    <button id="disagree" name="approval" class="btn btn-default" onclick="completeTask(0)"
                            type="button" value="不同意">不同意
                    </button>
                    <button type="button" class="btn btn-default" onclick="javascript:history.back();">返回</button>
                    <%-- <a href="${tenantPrefix}/rs/Invoice/enclosures?key=value">下载</a> --%>
                </div>
            </div>
        </div>
    </div>
</form>
</body>

</html>
