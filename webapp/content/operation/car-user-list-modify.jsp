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
		

		var conf={
				applyCodeId:"applyCode",				//受理单号input的ID
			    submitBtnId:"confirmStartProcess",		//提交按钮ID	
				checkApplyCodeUrl:"${tenantPrefix}/rs/business/applyCodeIfExist",	//验证受理单号url
       			checkUrl:"${tenantPrefix}/rs/customer/opteraion-getposition",		//获取岗位url
       			actionUrl:"${tenantPrefix}/CarApply/CarApply-startProcessInstance.do",//提交URL
       			businessDetailId:"businessDetailId",			//存储业务明细input的ID
       			formId:"xform",									//form的ID
       			selectAreaId:"area",
   	   			selectCompanyId:"branchOffice",
   	 			iptAreaId:"areaId",
   	 			iptAreaName:"areaName",
   	   	   		iptCompanyId:"companyId",
   	   	   		iptCompanyName:"companyName"
       		} 
		
		
		function CompleteTask(flag) {

			
			//经销商编号 为空  不允许提交
			if ( document.getElementById('carUser').value ==""){
				alert("请输入用车人！");
				return false;
			}
			   
				//经销商姓名 必填
				if ( document.getElementById('departmentName').value =="" || document.getElementById('departmentName').value =="null"){
					alert("请输入部门！");
					return false;
				}
				if ( document.getElementById('destination').value =="" ){
					alert("请输入目的地！");
					return false;
				}
				
				if ( document.getElementById('content').value =="" ){
					alert("用车事由！");
					return false;
				}
			
				if(document.getElementById('startDate').value == ""){
	        		alert("请选择开始时间！");
	                return false;
	        	}
	        	if(document.getElementById('endDate').value == ""){
	        		alert("请选择结束时间！");
	                return false;
	        	}
	        	if(document.getElementById('totalTime').value == ""){
	        		alert("请填写总计时间！");
	                return false;
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
	    			
	    			$('#xform').attr('action', '${tenantPrefix}/CarApply/CarApply-completeTask.do?flag=' + flag);
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
	    
	    

		//计算用车时长（小时，保留一位小数）
    	  function checkField( ){
    		  
    		  if((!document.getElementById('startDate').value == "")&&
    				  (!document.getElementById('endDate').value == "")){
          		
          		var t1 = document.getElementById('startDate').value;
          		var t2 = document.getElementById('endDate').value;

          		var date1 = new Date(t1);
          		var date2 = new Date(t2);
          		
          		var s1 = date1.getTime(),s2 = date2.getTime();
          		
          		var total = (s2 - s1)/1000;
          		 
          		//var hour = parseInt(total/(60*60));
          	
          		var hour = (total/(60*60)).toFixed(1);
          		
          		$("#totalTime").val(hour);
          	}
    	 }
	    
	    
    </script>
    
</head>
<style type="text/css">
         #tb1 td{border:1px solid #BBB ;white-space:nowrap;font-size:14px; height:41px;line-height:41px;}
        .f_td{ width:120px; font-size:12px;white-space:nowrap ; font-size:14px}
        .f_r_td{ width:130px; text-align:left;font-size:14px;}  
        #tb1  input{border: navajowhite; padding-left:5px;font-size:14px;hidden;height: 100%;width: 100%;background-color:#eee;}   
        #tb1 tr td{text-align:center; font-size:14px;}
        textarea{background-color:#eee;border:0;padding:6px 0 0 6px;height:120px;width:100%;}  
    </style>
<body>
 <%@include file="/header/bpm-workspace3.jsp"%>
    <form id="xform" method="post"   action="${tenantPrefix}/CarApply/CarApply-completeTask.do" class="xf-form" enctype="multipart/form-data">
			用车申请调整单
    <br/>
    <div class="container" >
     <section id="m-main" class="col-md-12" style="padding-top:65px;padding-bottom:20px">
		  <table width="100%" cellspacing="0" cellpadding="0" border="0" align="center" class="xf-table">
			  <tbody>
			    <tr>
				  <td width="25%" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
				    <label style="display:block;text-align:center;margin-bottom:0px;padding-top:10px;padding-bottom:10px;">审核环节&nbsp;</label>
				  </td>
				  <td width="75%" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-bottom" stype="padding-left:6px;">
				    <div id="nextStep"></div>
				  </td>
				</tr>
			  </tbody>
		</table>
		
		<input id="processInstanceId" type="hidden" name="processInstanceId" value="${processInstanceId}">
		<input id="humanTaskId" type="hidden"  name="humanTaskId" value="${humanTaskId}">
   		<input id="createTime"  type="hidden"  name="createTime" >
   		<input id="cancelOrderTotalID"  type="hidden"   name="cancelOrderTotalID" >
    	<input id="processDefinitionId" type="hidden" name="processDefinitionId" value="${processDefinitionId}">
		<input id="bpmProcessId" type="hidden"  name="bpmProcessId" value="${bpmProcessId}">
		<input id="autoCompleteFirstTask" type="hidden" name="autoCompleteFirstTask" value="false">
		<input id="businessKey" type="hidden" name="businessKey" value="${businessKey}">
    	<input id="userId"  name="userId" type="hidden" value="<%=userId %>">
    	<input id="url" type="hidden"  name="url" value="${url}">
    	
    	<table id="tb1"  width="100%" style="margin-top:25px;">
    		<tr>
                <td colspan='4' align='center' class='f_td'><h2>用车申请调整单</h2></td>
            </tr>
            <tr>
                 <td colspan='2'  class='f_td' align='right' style='padding-right:20px;'>
                    	受理单编号：
                 </td>
                <td colspan='2' >
                	<div id="div_applyCode" style="padding-left:6px;"></div>
               		<input id="applyCode" name="applyCode" value="${code}" readonly style="display:none;">
                 </td>
            </tr>
 			<tr>
				<td width="120px">
                    <span style="color:Red">*</span><span id='tag_realName'>&nbsp;用车人姓名</span>：
                </td>
                <td >
                	<input name="carUser" id="carUser" maxlength="10"> 
                </td>
               <td>
                    <span style="color:Red">*</span><span id='tag_welfare'>&nbsp;部门</span>：
                </td>
                <td >
                    <input name="departmentName" id="departmentName" maxlength="10"> 
                </td>
          </tr>
          <tr>
                <td class='f_td'>
                    <span style="color:Red">*</span><span id='tag_level'>&nbsp;目的地</span>：
                </td>
                <td colspan="3">
                    <input name="destination" id="destination" maxlength="10" >
                </td>
            </tr>
           <tr>
                <td  style="white-space:nowrap">
                    <span id='tag_bustype'>&nbsp;申请业务类型</span>：
                </td>
                <td style="text-align:left;width:420px;">
                  		<input id="busType" name="busType"  readonly style="display:none">
                  		<div id="div_busType" style="padding-left:6px;"></div>
                </td>
                <input id="businessType" name="businessType"  type="hidden">
                <td class='f_td'>
                    <span id='tag_busDetails'>&nbsp;业务细分</span>：
                </td>
                <td  style="text-align:left;">
                     <input id="busDetails" name="busDetails" readonly style="display:none">
                     <div id="div_busDetails" style="padding-left:6px;"></div>
                </td>
                 <input id="businessDetail" name="businessDetail" type="hidden">
            </tr>
           <input type = "hidden"  id="businessde" name="businessde" >
             <tr >
	            <td ><span style="color:Red">*</span>用车事由:</td>
        		<td colspan='3' style='height:100px; text-align:left;' >
                    <textarea  maxlength="5000"  name="content" id="content" rows="3" cols="28" class="text0" style="height:120px;width:100%;padding-left:10px;padding-top:10px" ></textarea>
                    <label id="recordNum" style="display: none;text-align:right;" class="control-label col-md-12"></label>
                </td>
        	</tr>
            <tr>
               	<td>
                    <span style="color:Red">*</span><span>时间</span>：
               	</td>
                <td colspan="2">
                   	<input autocomplete="off" placeholder="请选择开始时间" type="text" id="startDate" name="startDate" onclick="WdatePicker({maxDate:'#F{$dp.$D(\'endDate\')}',dateFmt:'yyyy-MM-dd HH:mm'})" class="Wdate" onchange="checkField()"  style="width:160px;background-color:#eee;padding-left:10px;"/>
                   	
                          &emsp;<span>至</span>&emsp; 
                          <input autocomplete="off" placeholder="请选择结束时间" type="text" id="endDate" name="endDate" onclick="WdatePicker({minDate:'#F{$dp.$D(\'startDate\')}',dateFmt:'yyyy-MM-dd HH:mm'})" class="Wdate" onchange="checkField()"  style="width:160px;background-color:#eee;padding-left:10px;"/>
                </td>
                <td colspan="1" id="totalTdId">
                   	<span >共</span>
                   	<input autocomplete="off" style="padding-left:10px;width: 30%;background:#eee;" type="text" id="totalTime" name="totalTime" onkeyup="checkedTime(this)" onafterpaste="checkedTime(this)">
                   	<span >时</span>
               </td>
          </tr>
  	</table>
       </section>
	<!-- end of main -->
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

	<input type='text' style='display:none'/>
	<%@include file="/common/selectPosition.jsp" %>
</form>
</body>
<script>
		
		var processInstanceId =  "${processInstanceId}";
		$.ajax({
				url:"${tenantPrefix}/dict/getProcessPostInfoByProcessInstanceId.do",
				data:{processInstanceId:processInstanceId},
				dataType:"json",
				type:"post",
				success:function(data){
					//console.log(data);
					$('#nextStep').append(data.whole);
				},
				error:function(){
					alert("获取流程审核人岗位信息出错！");
				}
		});

//取原表单的数据显示出来
$(function () {

	var id=${processInstanceId}; 

				if (id !="") {
	                $.getJSON('${tenantPrefix}/rs/CarApply/getCarApplyInfo', {
	                	id: id
	                }, function(data) {
	                	for (var i = 0; i < data.length; i++) {
	                		//alert(data[i].exchangeProductSub[0].productName); 
	                		$("#applyCode").val(data[i].applyCode);
	                		$("#div_applyCode").html(data[i].applyCode);
	                		$("#ucode").val(data[i].ucode); 
	                		$("#departmentCode").val(data[i].departmentCode);  
	                		$("#departmentName").val(data[i].departmentName);
	                		$("#destination").val(data[i].destination);
               				$("#content").val(data[i].content);
	                		$("#carUser").val(data[i].carUser);
	                		$("#startDate").val(data[i].borrowCarTime);
	                		$("#endDate").val(data[i].returnCarTime);
	                		$("#totalTime").val(data[i].totalTime);
	                		$("#busType").val(data[i].businessType);
	                		$("#div_busType").html(data[i].businessType);
	                		$("#busDetails").val(data[i].businessDetail);
	                		$("#div_busDetails").html(data[i].businessDetail);
	                		$("#businessType").val(data[i].businessType);
	                		$("#businessDetail").val(data[i].businessDetail);
	                	}
	  				});
	         };  
	});

</script>


</html>

