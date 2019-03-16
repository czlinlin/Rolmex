<%@page contentType="text/html;charset=UTF-8"%>
<%@include file="/taglibs.jsp"%>
<%pageContext.setAttribute("currentHeader", "bpm-workspace");%>
<%pageContext.setAttribute("currentMenu", "bpm-process");%>

<!doctype html>
<html lang="en">
<head>
<%-- <%@include file="/common/meta.jsp"%> --%>
 <script type="text/javascript" src="jquery-1.7.2.min.js"></script>
    <title><spring:message code="demo.demo.input.title" text="麦联"/></title>
    <script type='text/javascript' src='${cdnPrefix}/jquery/1.11.3/jquery.min.js'></script>
    <link type="text/css" rel="stylesheet" href="${cdnPrefix}/viewer/viewer.min.css">
	<script type="text/javascript" src="${cdnPrefix}/viewer/viewer-jquery.min.js"></script>
    <%-- <%@include file="/common/s3.jsp"%> --%>
	<!-- bootbox -->
    <%-- <script type="text/javascript" src="${cdnPrefix}/bootbox/bootbox.min.js"></script>
	<link href="${cdnPrefix}/xform3/styles/xform.css" rel="stylesheet">
    <script type="text/javascript" src="${cdnPrefix}/xform3/xform-packed.js"></script>

    <link type="text/css" rel="stylesheet" href="${cdnPrefix}/userpicker3-v2/userpicker.css">
    <script type="text/javascript" src="${cdnPrefix}/userpicker3-v2/userpicker.js"></script>
	<script type="text/javascript" src="${cdnPrefix}/operation/TaskOperation.js"></script>
	<script type="text/javascript" src="${cdnPrefix}/operation/operation.js"></script> --%>
	<!-- <style type="text/css">
		.xf-handler {cursor: auto;}
		.centerdiv{margin:0px auto; text-align:center;width:100%;}
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
		
		.noTopBorder td{ border-top:none;}
		.trFirst td{border-top:none;}
		.trLast td{border-bottom:none;}
		
		#tb1 td{border:1px solid #BBB }
        .f_td{ width:120px; font-size:12px;white-space:nowrap }
        .f_r_td{ width:130px; text-align:left;}  
        #tb1 tr td input{border: navajowhite;text-align:center;}   
        #tb1 tr td{text-align:center;} 
        
        .tableprint,.table-border{margin:10px 0 0 0;border-collapse:collapse;width:100%;}
        .tableprint td{padding-left:20px;padding-right:5px;border:#CCC 1px solid;line-height:35px;font-size:14px;word-break:break-all;word-wrap:break-word}
    	.table-border td{padding-right:5px;border-top:#CCC 1px solid;line-height:35px;font-size:14px;word-break:break-all;word-wrap:break-word}
	</style> -->
	
	<style>
		body{font-size:14px;}
    	div{margin:0;border:none;}
    	#divPrint{width:900px;margin:0 auto;}
    	#divOrderForm table{margin:10px 0 0 0;border-collapse:collapse;width:100%;}
    	#divOrderForm table td{padding-left:5px;padding-right:5px;border:#CCC 1px solid;line-height:35px;font-size:14px;word-break:break-all;word-wrap:break-word}
    	#divOrderForm table td.f_td{width:140px;text-align:center;}
    	#divPrint{width:900px;margin:0 auto;}
        .tableprint,.table-border{margin:10px 0 0 0;border-collapse:collapse;width:100%;}
        .tableprint td{padding-left:20px;padding-right:5px;border:#CCC 1px solid;line-height:35px;font-size:14px;word-break:break-all;word-wrap:break-word}
    	.table-border td{padding-right:5px;border-top:#CCC 1px solid;line-height:35px;font-size:14px;word-break:break-all;word-wrap:break-word}
	</style>
	<script type="text/javascript">
		 var HKEY_Root, HKEY_Path, HKEY_Key;
	     HKEY_Root = "HKEY_CURRENT_USER";
	     HKEY_Path = "\\Software\\Microsoft\\Internet Explorer\\PageSetup\\";
	     function pagesetup_null() {
	         try {
	             var RegWsh = new ActiveXObject("WScript.Shell")
	             hkey_key = "header"
	             RegWsh.RegWrite(hkey_root + hkey_path + hkey_key, "")
	             hkey_key = "footer"
	             RegWsh.RegWrite(hkey_root + hkey_path + hkey_key, "")
	         } catch (e) {}
	     }
	     function printme() {
	     	var bdhtml=window.document.body.innerHTML;//获取当前页的html代码
	         document.body.innerHTML = document.getElementById('divPrint').innerHTML;
	         pagesetup_null();
	         window.print();
	         document.body.innerHTML=bdhtml;
	         window.close();
	     }
    </script>
</head>
<body>
 <div id="divPrint">
    <div class="container">
     <section id="m-main" class="col-md-12" style="padding-top:45px;">
		<input id="processInstanceId" type="hidden" name="processInstanceId" value="<%= request.getParameter("processInstanceId")%>">
		<input id="humanTaskId" type="hidden"  name="humanTaskId" value="<%= request.getParameter("humanTaskId")%>">
    		<div id="divOrderForm">${detailHtml}</div>
            <c:if test="${isPrint == false}">
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
							<tr>
								<td>${item.name}</td>
								<td><tags:isDelUser userId="${item.assignee}"/></td>
								<td><fmt:formatDate value="${item.completeTime}" type="both"/></td>
								<td>${item.action}</td>
								<td>${item.auditDuration}</td>
							</tr>
							<c:if test="${item.action != '发起自定义申请' && item.action != '重新发起申请'}">
									<tr style="border-top:0px hidden;">
										<td>批示内容</td>
										<td colspan="4">${item.comment}</td>
									</tr>
							</c:if>
						</c:forEach>
					</tbody>
				</table>
			</div>
		</c:if>
 		<c:if test="${isPrint == true}">
			<div>
			    <table style="width:100%;" cellspacing="0" cellpadding="0" border="0" align="center"
			           class="tableprint"> 
			        <tbody>
				        <c:forEach var="item" items="${logHumanTaskDtos}"  varStatus="status">
				        		<c:if test="${status.index==0}">
		                  			<tr>
										<td>
												<table width="100%" cellspacing="0" cellpadding="0" border="0">
													<tr>
														<td style="border-width:0;">提交</td>
										</tr>
										<tr>
											<td style="text-align:right;border-width:0;">
										<tags:isDelUser userId="${item.assignee}"/>&emsp;<fmt:formatDate value="${item.completeTime}" type="both" pattern='yyyy年MM月dd日 HH时mm分ss秒'/>
												</td>
											</tr>
										</table>
										</td>
			                       </tr>
		                		</c:if>
				        		<c:if test="${status.index>0}">
				        			<tr>
				                  		<td>${item.name} &nbsp;审批详情</td>
			                        </tr>
			                        <tr>
										<td>
											审核结果：${item.action}<br/>
											审核意见：${item.comment}
									  	</td>
									</tr>
									<tr>
										  <td style="text-align:right;">
											<tags:isDelUser userId="${item.assignee}"/>&emsp;<fmt:formatDate value="${item.completeTime}" type="both" pattern='yyyy年MM月dd日 HH时mm分ss秒'/>
											&nbsp;审核时长&nbsp;${item.auditDuration}
										  </td>
			                        </tr>
	                		  </c:if>
				        </c:forEach>
			        </tbody>
			    </table>
			</div>
         </c:if>
       </section>
	<!-- end of main -->
	</div>
	<input type="hidden" id="hidTotal" name="hidTotal"  />
</div>
<c:if test="${isPrint == true}">
	<div style="width:500px;margin:20px auto;text-align:center">
		<input value="打印" class="button" onclick="printme();" type="button">
	</div>
</c:if>
<c:if test="${viewBack == true}">
	<div style="width:500px;margin:20px auto;text-align:center">
		<button type="button" class="btn btn-default" onclick="javascript:history.back();">返回</button>
	</div>
</c:if>
</body>
<%-- <script>
$(function () {
 	var hidTotal;
	var id=(<%= request.getParameter("processInstanceId")%>); 
	if (id !="") {
	    $.getJSON('${tenantPrefix}/rs/operationOnlineOrder/getOnLineForm', {
	    	id: id
	    }, function(data) {
	    	if(data!=undefined&&data!=null){
	    		$("#divOrderForm").html(data.detailHtml);
	    	}
	  });
	};             
});
</script> --%>
<c:if test="${isPrint == true}">
	<script>
		$(function(){
			$('#divOrderForm img').css({"width":"99%","height":"auto","margin":"0 2px"});
		})
	</script>
</c:if>
<script>
     $(function() {
    	$('#divOrderForm img').parent().removeAttr("target");
    	$('#divOrderForm img').parent().attr("href","javascript:");
   		$('#divOrderForm').viewer({
   			url: 'src',
   		});
	});
</script>
</html>

