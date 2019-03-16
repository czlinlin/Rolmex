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
                       "actionUrl": '${tenantPrefix}/CarApply/CarApply-completeTask.do?flag=' + flag,
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
    	 #tb1{width:100%;}
         #tb1 td{border:1px solid #BBB ;white-space:nowrap ;font-size:14px;height:41px;line-height:41px;}
        .f_td{ width:150px; font-size:12px;white-space:nowrap;font-size:14px; text-align:center; }
        .f_r_td{text-align:left;font-size:14px;}  
        #tb1 input{padding-left:5px;font-size:14px;border:none;width: 100%;background:#eee;}    
      	#tb1 tr td{font-size:14px;padding-left:6px;}
      	pre{border:none;margin:0;background:#fff;padding-left:6px;}
      	textarea{background-color:#eee;border:0;padding:6px 0 0 6px;height:120px;width:100%;}  
    </style>
<body>
 <%@include file="/header/bpm-workspace3.jsp"%>
    <form id="xform" method="post"    class="xf-form" enctype="multipart/form-data">
			用车审批单
    <br />
    <div class="container" >
     <section id="m-main" class="col-md-12" style="padding-top:65px;">
		<input id="processInstanceId" type="hidden" name="processInstanceId" value="${processInstanceId}">
		<input id="humanTaskId" type="hidden"  name="humanTaskId" value="${humanTaskId}">
		<input id="userId" type="hidden"  name="userId" value="<%=userId %>">
		<input id="createTime"  type="hidden"  name="createTime" >
   		<input id="cancelOrderTotalID"  type="hidden"   name="cancelOrderTotalID" >
    	<input id="processDefinitionId" type="hidden" name="processDefinitionId" value="${processDefinitionId}">
		<input id="bpmProcessId" type="hidden"  name="bpmProcessId" value="${bpmProcessId}">
		<input id="autoCompleteFirstTask" type="hidden" name="autoCompleteFirstTask" value="false">
		<input id="businessKey" type="hidden" name="businessKey" value="${businessKey}">
		<table width="100%" cellspacing="0" cellpadding="0" border="0" align="center" class="xf-table">
           <tbody>
             <tr>
               <td width="25%" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
                 <label style="display:block;text-align:center;margin-bottom:0px;padding-top:10px;padding-bottom:10px;">审核环节</label>
               </td>
               <td width="75%" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top" colspan="3" rowspan="1">
                 <div id="nextStep"></div>
               </td>
             </tr>
           </tbody>
         </table>
    	<table id="tb1" style="margin-top:25px;">
			<tr>
                <td colspan='4' align='center' class='f_td'><h2>用车审批单</h2></td>
            </tr>
            <tr>
                 <td colspan='2'  class='f_td' align='right' style='padding-right:20px;width:50%;'>
                    	受理单编号：
                 </td>
                <td colspan='2'  >
                		<div id="applyCode" name="applyCode"></div>
                 </td>
            </tr>
 			<tr>
				<td width="130px;" class="f_td">车牌号： </td>
                 <td width="250px;" class="f_r_td">
                	<div id="divplateNumber" name="divplateNumber"></div>
                	<input id="plateNumber" name="plateNumber" type="hidden"></input>
                </td>
                
                <td class="f_td">
                    	驾驶人：
                </td>
                 <td class="f_r_td">
                	<div id="divdriver" name="divdriver"></div>
                	<input id="driver" name="driver" type="hidden"></input>
                </td>
            </tr>
 			
			<tr>
				<td class="f_td">
                    <span id='tag_realName'>&nbsp;用车人姓名</span>：
                </td>
                <td class="f_r_td">
                	<div id="carUser" name="carUser"></div>
                </td>
               <td class='f_td'>
                    <span id='tag_welfare'>&nbsp;部门</span>：
                </td>
                <td >
                    <div id="departmentName" name="departmentName"></div>
                </td>
          </tr>
          <tr>
                <td class='f_td'>
                    <span id='tag_level'>&nbsp;目的地</span>：
                </td>
                <td colspan="3">
                    <div id="destination" name="destination"></div>
                </td>
            </tr>
            <tr>
                <td class="f_td">申请业务类型： </td>
                <td>
                   	<div id="busType" name="busType"></div>
				</td>
                <td class="f_td">业务细分： </td>
                <td>
                	<div id="busDetails" name="busDetails"></div>
                </td>
            </tr>
           <tr>
	            <td class="f_td">用车事由：</td>
        		<td colspan='3' style='text-align:left;padding-left:0;' >
        			<pre><div id="div_content"></div></pre>
                    <textarea style="display:none;"  maxlength="5000"  name="content" id="content" class="text0" readonly></textarea>
                    <label id="recordNum" style="display: none;text-align:right;" class="control-label col-md-12"></label>
                </td>
        	</tr>
        	<tr>
       			<td class="f_td">&emsp;&emsp;时间：</td>
				<td colspan="2"  align='left' >
					<span id="borrowCarTime" name="borrowCarTime"></span>
					&emsp;至&emsp;
	            	<span id="returnCarTime" name="returnCarTime"></span>
	            </td>
				<td colspan="1" align='left' >
					共<span id="totalTime" name="totalTime" style="padding:0 6px;"></span>时
				</td>
	        </tr>
        	
        	<tr>
                  <td colspan='4'  class='f_td'>
                        <span style="color:Red">*</span>批示内容
                   </td>
            </tr>
            <tr>
                <td colspan="4" style="text-align:left;padding-left:0px;">
                    <textarea maxlength="300"  name="comment" id="comment" rows="2" cols="20" class="text0"
                    onfocus="if(value=='同意'){value=''}"  onblur="if (value ==''){value='同意'}">同意</textarea>
                </td>
            </tr>
        	<tr>
	        	<td class="f_td"><span style="color:Red">*</span>操作密码：</td>
	        	<td colspan="3" style="padding-left:0px;">
	        		<input name="txtPrivateKey" type="password" maxlength="25" id="txtPrivateKey" style="background:#eee;" onblur='isPwd();'/>
                    <input id="isPwdRight" name="isPwdRight" type="hidden"/>
	        	</td>
        	</tr>
    </table>
       </section>
	<!-- end of main -->
</div>
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
	</div>
	<br/>
	<br/>
	<div class="navbar navbar-default navbar-fixed-bottom">
    	<div class="text-center" style="padding-top:8px;">
		    <div class="text-center" style="padding-top:8px;">
				<button id="completeTask1" type="button" class="btn btn-default" onclick="CompleteTask(1)">同意</button>
				<button id="completeTask2" type="button" class="btn btn-default" onclick="CompleteTask(2)">驳回</button>
				<button id="completeTask3" type="button" class="btn btn-default" onclick="CompleteTask(0)">不同意</button>
				<button type="button" class="btn btn-default" onclick="javascript:history.back();">返回</button>
			</div>
		</div>
	</div>
</form>
</body>
<script>
	$(function () {
		var id=${processInstanceId}; 
			if (id !="") {
                $.getJSON('${tenantPrefix}/rs/CarApply/getCarApplyInfo', {
                	id: id
                }, function(data) {
	               	for (var i = 0; i < data.length; i++) {
	               		//alert(data[i].exchangeProductSub[0].productName); 
	               		$("#applyCode").html(data[i].applyCode);  
	               		$("#ucode").html(data[i].ucode);  
	               		$("#name").html(data[i].name);
	               		$("#departmentCode").html(data[i].departmentCode);  
	               		$("#departmentName").html(data[i].departmentName);
	               		$("#destination").html(data[i].destination);
	             				$("#content").html(data[i].content);
	             				$("#div_content").html(data[i].content);
	               		$("#carUser").html(data[i].carUser);
	               		$("#borrowCarTime").html(data[i].borrowCarTime);
	               		$("#returnCarTime").html(data[i].returnCarTime);
	               		$("#totalTime").html(data[i].totalTime);
	               		$("#busType").html(data[i].businessType);
	               		$("#busDetails").html(data[i].businessDetail);
	               		$("#divplateNumber").html(data[i].plateNumber);
	               		$("#plateNumber").val(data[i].plateNumber);
	               		
	               		$("#divdriver").html(data[i].driver);
	               		$("#driver").val(data[i].driver);
	               	}
				});
              	//审核环节
                $.ajax({
       			url:"${tenantPrefix}/dict/getProcessPostInfoByProcessInstanceId.do",
       			data:{processInstanceId:id},
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
         };
	});
</script>
</html>

