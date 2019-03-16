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
	<style type="text/css">
		.centerdiv{width:950px; margin:0px auto; text-align:center;}
		.flownoborder{border:0px; text-align:center}
		.flowtable{width:950px; line-height:34px; text-align:left;border-collapse:collapse;}
		.flowtable tr td{border:1px solid #bbb;}
		.tdlable { margin:0px 5px; height:30px;}
		.tdl{text-align:center; width:120px;}
		.f_tb{border:1px solid #bbb; width:950px; line-height:34px;}
		.f_td{ border:1px solid #bbb;  width:150px; text-align:center;}
		.f_r_td{border:1px solid #bbb; padding-left:2px;  width:130px; text-align:left; word-break: break-all; word-wrap:break-word;}
		.f_r_td ul{ list-style:none; padding-left:40px}
		.f_r_td ul li{width:150px; float:left; line-height:25px; padding-top:9px; background-image:none;}
		.red{ color:Red}
		.f_td1{ border:1px solid #bbb;  width:120px; text-align:center;}
		.f_r_td1{border:1px solid #bbb; padding-left:2px;  width:160px; text-align:left; word-break: break-all; word-wrap:break-word;}
		.f_r_td1 ul{ list-style:none; padding-left:40px}
		.f_r_td1 ul li{width:150px; float:left; line-height:25px; padding-top:9px; background-image:none;}
		
		
		.tableReturn{ border-collapse:collapse;width:949px;margin:0 auto; padding:0px;}
		.tableReturn td{border:1px solid gray;line-height:25px;}
		.tableReturn td input[type='text'],.tableReturn td textarea,.tableReturn td input[type='submit']{width:100%;border:none;text-align:center;background:#eee;}
		.tableReturn td input.date{width:48%;}
		.noTopBorder td{ border-top:none;}
		.tableProductList{width:947px; overflow:hidden;margin-top:0;  padding:0px;}
		.tableProductList{ border-collapse:inherit;}
		.trFirst td{border-top:none;}
		.trLast td{border-bottom:none;}
		
		#tb1 td{border:1px solid #BBB }
        .f_td{ width:120px; font-size:12px;white-space:nowrap }
        .f_r_td{ width:130px; text-align:left;}  
        #tb1 tr td input{border: navajowhite;text-align:center;}   
        #tb1 tr td{text-align:center;} 
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
		
		var conf={
                "formId":"xform",
                "checkUrl":'${tenantPrefix}/rs/customer/opteraion-verifyPassword',
                "actionUrl": '${tenantPrefix}/operationOA/OA-completeTask.do',
                "iptPwdId":"txtPrivateKey"}
		function CompleteTask(flag) {
			//若要驳回或不同意，必须填写意见才能提交			
			if ((flag == 2 || flag == 0) &&(( document.getElementById('comment').value == "") || ( document.getElementById('comment').value == "同意")) ){
                alert("请填写批示内容！");
                return false;
            }
			
			//验证密码
            var pwd = $("#"+conf.iptPwdId).val();
            if(pwd==""){
                alert("请输入操作密码！");
                return false;
            }
            
            var dialog = bootbox.dialog({
                message:'<p class="text-center"><i class="fa fa-spin fa-spinner"></i>提交中...</p>',
                size:'small',
                closeButton: false
            });
            
            $.ajax({
                url:conf.checkUrl,
                type:"GET",
                data:{pwd: pwd},
                timeout:10000,
                success: function(data) {
                	if(data.code!=200){
                        alert(data.message);
                        return false;
                    }

                    requestProcess(flag);
                },
                error:function(XMLHttpRequest, textStatus, errorThrown){
                    alert("验证密码错误，提交失败");
                    return false;
                },
                complete:function(xh,status){
                    dialog.modal('hide');
                    if(status=="timeout")
                        bootbox.alert("提交超时");
                    return false;
                }
            });
		}
		
		var requestProcess=function(flag){
			<%--  $("#"+conf.formId).attr('action', conf.actionUrl+'?flag=' + flag);
            $("#"+conf.formId).submit(); --%>
            
			var applyCode=$("#applyCode").val();
			var id="<%= request.getParameter("processInstanceId")%>";
			if (id !="") {
			    $.getJSON('${tenantPrefix}/rs/operationOnlineOrder/isAuth', {
			    	id: id
			    }, function(dataReturn) {
			    	if(flag==1){
			    		if(dataReturn.isauth=="1"){
			    			$.getJSON('${tenantPrefix}/rs/wsapi/checksab', {
				    			applycode: applyCode
						    }, function(data) {
						    	if(data==undefined||data==null){
						    		alert("异步请求异常！");
						    		return;
						    	}
						    	if(data.status=="ok"){
						    		$("#"+conf.formId).attr('action', conf.actionUrl+'?flag=' + flag);
				                    $("#"+conf.formId).submit();
						    	}
						    	else if(data.status=="error"){
						    		alert(data.msg)
						    		return;
						    	}
						    	else if(data.status=="exist"){
						    		alert(data.msg+"\r\n"+"请点击'不同意'结束您的流程！")
						    		return;
						    	}
						    	else{
						    		alert("未验证授权书信息！");
						    		return;
						    	}
						    });
			    		}
			    		else{
			    			$("#"+conf.formId).attr('action', conf.actionUrl+'?flag=' + flag);
		                    $("#"+conf.formId).submit();
		    			}
			    	}
			    	else{
			    		$("#"+conf.formId).attr('action', conf.actionUrl+'?flag=' + flag);
	                    $("#"+conf.formId).submit();
			    	}
			  });
			};
		}
    </script>
</head>
<body>
 <%@include file="/header/bpm-workspace3.jsp"%>
    <form id="xform" method="post"    class="xf-form" enctype="multipart/form-data">
    <br />
    <div class="container" style="width:980px;">
     <section id="m-main" class="col-md-12" style="padding-top:65px;">
		<input id="processInstanceId" type="hidden" name="processInstanceId" value="${processInstanceId}">
		<input id="humanTaskId" type="hidden"  name="humanTaskId" value="${humanTaskId}">
		<input id="applyCode"  type="hidden"  name="applyCode" value=""/>
    		<div id="divOrderForm"></div>
        	<table id="tb1" class="centerdiv" style="border-top:none;">
	        	<tr>
	                <td colspan='6' align='center' class='f_td' style="border-top-width:0;">
	                    <span style="color:Red">*</span>批示内容
	                </td>
	            </tr>
	            <tr>
	                <td colspan='6' style='height:80px'>
	                    <textarea name="comment" id="comment" rows="2" cols="20" style="width:100%;border:none;height:79px;padding:0 0 0 5px;background:#eee;" class="text0" onfocus="if(value=='同意'){value=''}"  onblur="if (value ==''){value='同意'}">同意</textarea>
	                </td>
	            </tr>
	            <tr>
	                <td ><code>*</code>操作密码：</td>
	                <td >
	                    <input name="txtPrivateKey" type="password" maxlength="25" id="txtPrivateKey" style="width:100%;text-align:left;padding-left:5px;background:#eee;" />
	                	<input  id="isPwdRight" name="isPwdRight"  type="hidden" />
	                </td>
	         	</tr>
            </table>
            <div>
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
										<td colspan="4" style="vertical-align:top">${item.comment}</td>
									</tr>
								</c:if>
							</c:if>
						</c:forEach>
					</tbody>
				</table>
			</div>
       </section>
	<!-- end of main -->
	</div>
	 
	<br/>
    <br/>
	<div class="navbar navbar-default navbar-fixed-bottom">
	   	<div class="text-center" style="padding-top:8px;">
		    <div class="text-center" style="padding-top:8px;">
				<button id="completeTask1" type="button" class="btn btn-default" onclick="CompleteTask(1)">同意</button>
				<button id="completeTask4" type="button" class="btn btn-default" onclick="CompleteTask(4)">同意并授权</button>
				<button id="completeTask2" type="button" class="btn btn-default" onclick="CompleteTask(2)">驳回</button>
				<button id="completeTask3" type="button" class="btn btn-default" onclick="CompleteTask(0)">不同意</button>
				<button type="button" class="btn btn-default" onclick="javascript:history.back();">返回</button>
			</div>
		</div>
	</div>
<input type="hidden" id="hidTotal" name="hidTotal"  />
</form>
</body>
<script>
	$(function () {
	 	var hidTotal;
		var id="<%= request.getParameter("processInstanceId")%>";
		if (id !="") {
		    $.getJSON('${tenantPrefix}/rs/operationOnlineOrder/getOnLineForm', {
		    	id: id
		    }, function(data) {
		    	if(data!=undefined&&data!=null){
		    		$("#divOrderForm").html(data.detailHtml);
		    		$('#divOrderForm img').parent().removeAttr("target");
		        	$('#divOrderForm img').parent().attr("href","javascript:");
		        	$('#divOrderForm').viewer({
		       			url: 'src',
		       		});
		    		if(data.auditStatus=="0"||data.isshow=="0"){
		    			$("#completeTask2").remove();
		    		}
		    		if(data.isAuthorization=="0"){
		    			$("#completeTask4").remove();
		    		}
		    		
		    		$("#applyCode").val(data.applyCode);
		    		
		    	}
		  });
		};             
	});
</script>
</html>

