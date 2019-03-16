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
	
	<style type="text/css">
		.xf-handler {
			cursor: auto;
		}
		input{height:25px;}
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
						  $("#completeTask2").css('display',"none");
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
		
		function CompleteTask(flag) {
			
			var num = parseInt($("#hidTotal").val());
			for(var i=1;i<=num;i++) {
				if(document.getElementById('ucode'+i)!=undefined ){
					
				//撤单编号必填
				if ( document.getElementById('ucode'+i).value ==""){
					alert("请填写撤单编号"+i);
					return false;
				}
			
				//撤单类型必须选择
				if ( document.getElementById('cancelType'+i).value ==""||document.getElementById('cancelType'+i).value =="请选择"){
					alert("请选择撤单类型！");
					return false;
				}
				
					//若“撤单类型”为“零售业绩单”，则必须输入“撤单金额”
					if ( document.getElementById('cancelType'+i).value =="零售业绩单"&&document.getElementById('cancelMoney'+i).value == ""){
						alert("请填写撤单编号"+i+"的撤单金额！");
						return false;
					}
					//若“撤单类型”为“零售业绩单(补)”，则必须输入“撤单金额”和“业绩单号”
					if ( document.getElementById('cancelType'+i).value =="零售业绩单(补)"&&(document.getElementById('cancelMoney'+i).value == "" || document.getElementById('saleId'+i).value == "")){
						alert("请填写撤单编号"+i+"的撤单金额和业绩单号！");
						return false;
					}
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
	    			
	    			$('#xform').attr('action', '${tenantPrefix}/operationCancelOrder/cancelOrder-completeTask.do?flag=' + flag);
	    			$('#xform').submit();
	            },      
	            error: function(e){      
	            	loading.modal('hide');
	                alert("服务器请求失败,请重试"); 
	            }
	       });
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
    <form id="xform" method="post"   action="${tenantPrefix}/operationApply/process-operationApply-startProcessInstance.do" class="xf-form" enctype="multipart/form-data">
			常规/非常规业务	
    <br />

    <div class="container" style="width:980px;">
    		
     <section id="m-main" class="col-md-12" style="padding-top:65px;padding-bottom:20px">
		
		<input id="processInstanceId" type="hidden" name="processInstanceId" value="${processInstanceId}">
		<input id="humanTaskId" type="hidden"  name="humanTaskId" value="${humanTaskId}">
   		<input id="createTime"  type="hidden"  name="createTime" >
   		<input id="cancelOrderTotalID"  type="hidden"   name="cancelOrderTotalID" >
    	<input id="processDefinitionId" type="hidden" name="processDefinitionId" value="${processDefinitionId}">
		<input id="bpmProcessId" type="hidden"  name="bpmProcessId" value="${bpmProcessId}">
		<input id="autoCompleteFirstTask" type="hidden" name="autoCompleteFirstTask" value="false">
		<input id="businessKey" type="hidden" name="businessKey" value="${businessKey}">
    	<input id="userId" type="hidden" name="userId" value="<%=userId %>">
    	<table id="tb1" >
    		<tr>
                <td colspan='8' align='center' class='f_td'><h2>调整撤单登记表</h2></td>
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
               		<input id="applyCode" name="applyCode" readonly>
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
            </table>
       </section>
	<!-- end of main -->
	<div class="" style="margin-top:10px;text-align:center;">
        <p style="color:Red; font-weight:bold;margin-bottom:30px">
        </br></br></br>
            注：若“撤单类型”为“零售业绩单”，则必须输入“撤单金额”！<br />若“撤单类型”为“零售业绩单(补)”，则必须输入“撤单金额”和“业绩单号”！
        </p>
        <button id="confirmStartProcess" class="btn btn-default" type="button" onclick="addTr();">添加</button>
    </div>

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
							<td colspan="4">${item.comment}</td>
						</tr>
					</c:if>
					 </c:if>
					  </c:forEach>
				  </tbody>
				</table>
	<br/>
	<br/>
  </div> 
	  <div class="navbar navbar-default navbar-fixed-bottom">
	    	<div class="text-center" style="padding-top:8px;">
			    <div class="text-center" style="padding-top:8px;">
					<button id="completeTask1" type="button" class="btn btn-default" onclick="CompleteTask(3)">重新申请</button>
					<button id="completeTask2" type="button" class="btn btn-default" onclick="CompleteTask(4)">撤销申请</button>
					<button id="endProcess" type="button" class="btn btn-default" onclick="confirmOperation()">撤销申请</button>
					<button type="button" class="btn btn-default" onclick="javascript:history.back();">返回</button>
				</div>
			</div>
	   </div>

<input type="hidden" id="hidTotal" name="hidTotal"  />
<input  id="hidNum" name="hidNum" type="hidden" />
</form>
</body>

 

<script>
//取原表单的数据显示出来
$(function () {

	 var hidTotal;
	 var hidNum;
	var id=${processInstanceId}; 

				if (id !="") {
	                $.getJSON('${tenantPrefix}/rs/operationCancelOrder/getCancelOrderInfo', {
	                	id: id
	                }, function(data) {
	                	for (var i = 0; i < data.length; i++) {
	                		//alert(data[i].hidTotal); 
	                		hidTotal = data[i].hidTotal; 
	                		hidNum   = data[i].hidTotal; 
	                		$("#hidTotal").val(data[i].hidTotal); 
	                		$("#hidNum").val(data[i].hidTotal); 
	                		$("#shopCode").val(data[i].ucode);  
	                		$("#shopName").val(data[i].shopName);
	                		$("#shopMobile").val(data[i].shopMobile);  
	                		$("#mobile").val(data[i].mobile);
	                		$("#registerTime").val(data[i].registerTime);
	                		$("#registerName").val(data[i].registerName);
	                		$("#isChecked").val(data[i].isChecked);
	                		$("#cancelRemark").val(data[i].cancelRemark);
	                		$("#cancelOrderTotalID").val(data[i].id);
	                		$("#submitTimes").val(data[i].submitTimes);
	                		$("#applyCode").val(data[i].applyCode);
	                		var remove = "";
	                		//画表单
	    					for (var num=1;num<data[i].ordersub.length+1;num++) {
	    						if(num != 1){
	    							remove = "[删除]";
	    						}
	    						var html = "<tr id='tr" + num + "_1'><td class='f_td'>撤单编号" + num + "</td><td class='f_r_td'><input type='text' id='ucode" + num + "' name='ucode" + num + "' maxlength='8' class='text0'  readonly /></td>";
	 	                        html = html + "<td class='f_td'>撤单姓名</td><td class='f_r_td'><input type='text' id='userName" + num + "' name='userName" + num + "' class='text0' readonly /></td>";
	 	                        html = html + "<td class='f_td'>编号加入日期</td><td class='f_r_td'><input type='text' id='addTime" + num + "' name='addTime" + num + "' class='text0' readonly /></td>";
	 	                        html = html + "<td rowspan='2'  style='position:absolute;margin-top: 15px;'><a href='javascript:;' onclick='removeTr(" + num + ");'>"+remove+"</a></td>";
	 	                        html = html + "</tr><tr id='tr" + num + "_2'><td class='f_td'>撤单类型</td><td class='f_r_td'><select id='cancelType" + num + "' name='cancelType" + num + "' ><option value='' selected>请选择</option><option value='销售业绩单'>销售业绩单</option><option value='零售业绩单'>零售业绩单</option><option value='零售业绩单(补)'>零售业绩单(补)</option><option value='升级单'>升级单</option></select></td>";
	 	                        html = html + "<td class='f_td'>撤单金额</td><td class='f_r_td'><input type='text' id='cancelMoney" + num + "' name='cancelMoney" + num + "' maxlength='8' class='text0'  /></td>";
	 	                        html = html + "<td class='f_td'>业绩单号</td><td class='f_r_td'><input type='text' id='saleId" + num + "' name='saleId" + num + "' maxlength='20' class='text0'  /></td></tr>";

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


//添加列
function addTr() {
    	var num = parseInt($("#hidNum").val());
    	num = num+1;
        var total = parseInt($("#hidNum").val());
        if (total < 20) {
        	 var html = "<tr id='tr" + num + "_1'><td class='f_td'>撤单编号" + num + "</td><td class='f_r_td'><input type='text' id='ucode" + num + "' name='ucode" + num + "' maxlength='8' class='text0' value='' onblur='fnUserData(" + num + ");' /></td>";
             html = html + "<td class='f_td'>撤单姓名</td><td class='f_r_td'><input type='text' id='userName" + num + "' name='userName" + num + "' class='text0' value='' readonly/></td>";
             html = html + "<td class='f_td'>编号加入日期</td><td class='f_r_td'><input type='text' id='addTime" + num + "' name='addTime" + num + "' class='text0' value='' readonly/></td>";
             html = html + "<td rowspan='2'  style='position:absolute;margin-top: 15px;'><a href='javascript:;' onclick='removeTr(" + num + ");'>[删除]</a></td>";
             html = html + "</tr><tr id='tr" + num + "_2'><td class='f_td'>撤单类型</td><td class='f_r_td'><select id='cancelType" + num + "' name='cancelType" + num + "' ><option value='' selected>请选择</option><option value='销售业绩单'>销售业绩单</option><option value='零售业绩单'>零售业绩单</option><option value='零售业绩单(补)'>零售业绩单(补)</option><option value='升级单'>升级单</option></select></td>";
             html = html + "<td class='f_td'>撤单金额</td><td class='f_r_td'><input type='text' id='cancelMoney" + num + "' name='cancelMoney" + num + "' maxlength='8' class='text0' value=''  /></td>";
             html = html + "<td class='f_td'>业绩单号</td><td class='f_r_td'><input type='text' id='saleId" + num + "' name='saleId" + num + "' maxlength='20' class='text0' value='' /></td></tr>";

             $(html).insertBefore("#trAddAfter");
             
		$("#hidTotal").val(total + 1);
		$("#hidNum").val(total + 1);

        }else {
            alert("最多添加20条撤单编号！");
            return;
        }

    }
//移除添加列
function removeTr(num) {
    var total = parseInt($("#hidTotal").val());            
    var trid = "#tr" + num + "_1,#tr" + num+ "_2";
    $(trid).remove();
    $("#hidTotal").val(total - 1);
}

//加载撤单编号的姓名和加入日期
function fnUserData(num) {
	var code = "#ucode" + num;
    var id = $(code).val();
 
    if (id !="") {
	    	$.getJSON('${tenantPrefix}/rs/varUser/userInfo', {
	        	customerInfoId: id
	        }, function(data) {
	        	//alert(JSON.stringify(data)); 
				$("#userName"+ num ).val(data.name);
	  			$("#addTime"+ num ).val(data.addTime);
	  		});
       }
}

 


</script>


</html>

