<%@page contentType="text/html;charset=UTF-8"%>
<%@include file="/taglibs.jsp"%>
<%pageContext.setAttribute("currentHeader", "bpm-workspace");%>
<%pageContext.setAttribute("currentMenu", "bpm-process");%>

<!doctype html>
<html lang="en">
<head>
<%@include file="/common/meta.jsp"%>
 <script type="text/javascript" src="jquery-1.7.2.min.js"></script>
    <title><spring:message code="demo.demo.input.title" text="麦联"/></title>
    <%@include file="/common/s3.jsp"%>
	<!-- bootbox -->
    <script type="text/javascript" src="${cdnPrefix}/bootbox/bootbox.min1.js"></script>
	<link href="${cdnPrefix}/xform3/styles/xform.css" rel="stylesheet">
    <script type="text/javascript" src="${cdnPrefix}/xform3/xform-packed.js"></script>

    <link type="text/css" rel="stylesheet" href="${cdnPrefix}/userpicker3-v2/userpicker.css">
    <script type="text/javascript" src="${cdnPrefix}/userpicker3-v2/userpicker.js"></script>
	<script type="text/javascript" src="${cdnPrefix}/operation/TaskOperation.js"></script>
	<script type="text/javascript" src="${cdnPrefix}/operation/operation.js?v=1.20"></script>
	
	<style type="text/css">
		.xf-handler {
			cursor: auto;
		}
	</style>
	
	<script type="text/javascript">
		document.onmousedown = function(e) {};
		document.onmousemove = function(e) {};
		document.onmouseup = function(e) {};
		document.ondblclick = function(e) {};

		var xform;
		
		
		 $(function() {
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
		
		function CompleteTask(flag) {
			
			//若要驳回或不同意，必须填写意见才能提交
			if ((flag == 2 || flag == 0)&&(( document.getElementById('comment').value == "") || ( document.getElementById('comment').value == "同意")) ){
				alert("请填写批示内容！");
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
                       "actionUrl": '${tenantPrefix}/operationCancelOrder/cancelOrder-completeTask.do?flag=' + flag,
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

    </script>
    
</head>

    <style type="text/css">
         #tb1 td{border:1px solid #BBB }
        .f_td{ width:120px; font-size:12px;white-space:nowrap }
        .f_r_td{ width:130px; text-align:left;}  
        #tb1 tr td input{border: navajowhite;}   
        #tb1 tr td{text-align:center;}  
    </style>
    
<body>
 <%@include file="/header/bpm-workspace3.jsp"%>
    <form id="xform" method="post"    class="xf-form" enctype="multipart/form-data">
			撤单登记表	
    <br />

    <div class="container" style="width:980px;">
    		
     <section id="m-main" class="col-md-12" style="padding-top:65px;">
		<input id="processInstanceId" type="hidden" name="processInstanceId" value="${processInstanceId}">
		<input id="humanTaskId" type="hidden"  name="humanTaskId" value="${humanTaskId}">
		<input id="userId" type="hidden"  name="userId" value="<%=userId %>">
		
    	<table id="tb1" >
    		 <tr>
                <td colspan='8' align='center' class='f_td'><h2>撤单登记表</h2></td>
            </tr>
            <tr>
                <td colspan='1'  class='f_td' align='right' style='padding-right:20px;'>
                    	提交次数：
                    	
                </td>
                <td colspan='1' >
               		<input id="submitTimes" name="submitTimes" readonly>
                 </td>
                 <td colspan='1'  class='f_td' align='right' style='padding-right:20px;'>
                    	受理单编号：
                  </td>
	              <td colspan='3' >
	               			<input id="applyCode" name="applyCode"  style = "width:200px;" readonly>
	              </td>
            </tr>
 			 <tr>
         	    <td ><span id='tag_shopCode'>&nbsp;店编号</span>：</td>
                <td ><input id ="shopCode" name="ucode" type="text" maxlength="8" readonly/></td>
                <td ><span id='tag_shopName'>&nbsp;店姓名</span>：</td>
                <td ><input name="shopName" id="shopName" readonly></td>
                <td ><span id='tag_shopMobile'>&nbsp;店电话</span>：</td>
                <td ><input name="shopMobile" id="shopMobile" readonly></td>
            </tr>
            <tr>
         	    <td ><span id='tag_mobile'>&nbsp;来电电话</span>：</td>
                <td ><input id ="mobile" name="mobile" type="text" readonly/></td>
                
                 <jsp:useBean id="time" class="java.util.Date"/>  
       			<td ><span id='tag_registerTime'>&nbsp;撤单登记时间</span>：</td>
					
                <td ><input name="registerTime" id="registerTime" readonly> </td>
                <td ><span id='tag_registerName'>&nbsp;登记人</span>：</td>
                <td ><input name="registerName" id="registerName" readonly></td>
             </tr>
             <tr>
         	    <td ><span id='copyId'>&nbsp;抄送</span>：</td>
                <td colspan='5' id="copyNames"></td>
             </tr>
            <tr id="trAddAfter">
	            <td >是否核实</td>
	            <td ><input name="isChecked" id="isChecked" readonly>
	            </td>
	            <td >撤单备注</td>
	            <td  colspan="3"><input type="text" id="cancelRemark" name="cancelRemark" size="60" readonly /></td>
        	</tr>
        	<tr>
                <td colspan='6' align='center' class='f_td'>
                    <span style="color:Red">*</span>批示内容
                </td>
            </tr>
            <tr>
                <td colspan='6' style='height:80px' >
                    <textarea  maxlength="300"  name="comment" id="comment" rows="2" cols="20" id="appContext" class="text0" style="height:79px;width:850px;" 
                    			onfocus="if(value=='同意'){value=''}"  onblur="if (value ==''){value='同意'}">同意</textarea>
                </td>
            </tr>
            <tr>
                                    <td ><code>*</code>操作密码：</td>
                                    <td >
                                        <input name="txtPrivateKey" type="password" maxlength="25" id="txtPrivateKey" style="float: left;" />
                                    	<input  id="isPwdRight" name="isPwdRight"  type="hidden" />
                                    </td>
         </tr>
            
            </table>
       </section>
	<!-- end of main -->
</div>
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
					<c:if test="${item.action != '提交' && item.action != '重新调整申请'}">
						<tr style="border-top:0px hidden;">
							<td>批示内容</td>
							<td colspan="4">${item.comment}</td>
						</tr>
					</c:if>
					 </c:if>
					  </c:forEach>
				  </tbody>
				</table>
	<br/>
	<br/>
	<div class="navbar navbar-default navbar-fixed-bottom">
	    	<div class="text-center" style="padding-top:8px;">
			    <div class="text-center" style="padding-top:8px;">
					<button id="completeTask1" type="button" class="btn btn-default" onclick="CompleteTask(1)">同意</button>
					<button id="completeTask2" type="button" class="btn btn-default" onclick="CompleteTask(2)">驳回</button>
					<button id="completeTask3" type="button" class="btn btn-default" onclick="CompleteTask(0)">不同意</button>
				</div>
			</div>
	</div>
	
<input type="hidden" id="hidTotal" name="hidTotal"  />
</form>
</body>

 

<script>

$(function () {

	var hidTotal;

	var id=${processInstanceId}; 

	if (id !="") {
		$.getJSON('${tenantPrefix}/rs/operationCancelOrder/getCancelOrderInfo', {
                	id: id
			}, function(data) {
               	for (var i = 0; i < data.length; i++) {
               		//alert(data[i].hidTotal); 
               		hidTotal = data[i].hidTotal;  
               		$("#shopCode").val(data[i].ucode);  
               		$("#shopName").val(data[i].shopName);
               		$("#shopMobile").val(data[i].shopMobile);  
               		$("#mobile").val(data[i].mobile);
               		$("#registerTime").val(data[i].registerTime);
               		$("#registerName").val(data[i].registerName);
               		$("#isChecked").val(data[i].isChecked);
               		$("#cancelRemark").val(data[i].cancelRemark);
               		$("#submitTimes").val(data[i].submitTimes);
               		$("#applyCode").val(data[i].applyCode);
               		
               		//画表单
					for (var num=1;num<data[i].ordersub.length+1;num++) {
						var html = "<tr id='tr" + num + "_1'><td class='f_td'>撤单编号" + num + "</td><td class='f_r_td'><input type='text' id='ucode" + num + "' name='ucode" + num + "' maxlength='8' class='text0'  readonly /></td>";
						html = html + "<td class='f_td'>撤单姓名</td><td class='f_r_td'><input type='text' id='userName" + num + "' name='userName" + num + "' class='text0' readonly /></td>";
                           html = html + "<td class='f_td'>编号加入日期</td><td class='f_r_td'><input type='text' id='addTime" + num + "' name='addTime" + num + "' class='text0' readonly /></td>";
                           html = html + "<td rowspan='2'><a href='javascript:;' ></a></td>";
                           html = html + "</tr><tr id='tr" + num + "_2'><td class='f_td'>撤单类型</td><td class='f_r_td'><input type='text'  id='cancelType" + num + "' name='cancelType" + num + "' readonly></td>";
                           html = html + "<td class='f_td'>撤单金额</td><td class='f_r_td'><input type='text' id='cancelMoney" + num + "' name='cancelMoney" + num + "' maxlength='8' class='text0' readonly /></td>";
                           html = html + "<td class='f_td'>业绩单号</td><td class='f_r_td'><input type='text' id='saleId" + num + "' name='saleId" + num + "' maxlength='20' class='text0' readonly /></td></tr>";

                           $(html).insertBefore("#trAddAfter");
          		 
						$("#ucode"+num+"").val(data[i].ordersub[num-1].ucode);
						$("#userName"+num+"").val(data[i].ordersub[num-1].userName); 
						$("#addTime"+num+"").val(data[i].ordersub[num-1].addTime); 
						$("#cancelType"+num+"").val(data[i].ordersub[num-1].cancelType); 
						$("#cancelMoney"+num+"").val(data[i].ordersub[num-1].cancelMoney); 
						$("#saleId"+num+"").val(data[i].ordersub[num-1].saleID); 
  
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



</script>


</html>

