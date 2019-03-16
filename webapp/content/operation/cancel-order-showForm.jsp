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
    <script type="text/javascript" src="${cdnPrefix}/bootbox/bootbox.min.js"></script>
	<link href="${cdnPrefix}/xform3/styles/xform.css" rel="stylesheet">
    <script type="text/javascript" src="${cdnPrefix}/xform3/xform-packed.js"></script>

    <link type="text/css" rel="stylesheet" href="${cdnPrefix}/userpicker3-v2/userpicker.css">
    <script type="text/javascript" src="${cdnPrefix}/userpicker3-v2/userpicker.js"></script>
	<script type="text/javascript" src="${cdnPrefix}/operation/TaskOperation.js"></script>
	
	<style type="text/css">
        .xf-handler {
            cursor: auto;
            font-size:14px;
        }
        .tableprint{margin:10px 0 0 0;border-collapse:collapse;}
        .tableprint td{padding-left:20px;padding-right:5px;border:#CCCCCC 1px solid;line-height:35px;font-size:14px;}
    </style>
</head>

    <style type="text/css">
         #tb1 td{border:1px solid #BBB }
        .f_td{ width:120px; font-size:12px;white-space:nowrap }
        .f_r_td{ width:130px; text-align:left;}  
        #tb1 tr td input{border: navajowhite;text-align:center;}   
        #tb1 tr td{text-align:center;}  
    </style>
    
<body>
    <form id="xform" method="post"    class="xf-form" enctype="multipart/form-data">
    <div class="container" style="width:980px;">
    		
     <section id="m-main" class="col-md-12" style="padding-top:65px;">

		<input id="processInstanceId" type="hidden" name="processInstanceId" value="<%= request.getParameter("processInstanceId")%>">
		<input id="humanTaskId" type="hidden"  name="humanTaskId" value="<%= request.getParameter("humanTaskId")%>">
		
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
	            <td  colspan="3"><input type="text" id="cancelRemark" name="cancelRemark"  size="60" readonly /></td>
        	</tr>
        
            </table>
            <c:if test="${isPrint == true}">
                        <div>
                            <table width="100%" cellspacing="0" cellpadding="0" border="0" align="center"
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
												  					<tags:user userId="${item.assignee}"/>&emsp;<fmt:formatDate value="${item.completeTime}" type="both" pattern='yyyy年MM月dd日 HH时mm分ss秒'/>
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
												  		<tags:user userId="${item.assignee}"/>&emsp;<fmt:formatDate value="${item.completeTime}" type="both" pattern='yyyy年MM月dd日 HH时mm分ss秒'/>
												  		&nbsp;审核时长&nbsp;${item.auditDuration}
										  		  </td>
		                                    </tr>
                                		</c:if>
                                </c:forEach>
                                </tbody>
                            </table>
                        </div>
                        </c:if>
                        <c:if test="${isPrint == false}">
                        	<div class="container">
								 <table width="100%" cellspacing="0" cellpadding="0" border="0" align="center" class="table table-border">
									  <thead>
									    <tr>
										  <th>环节</th>
										  <th>操作人</th>
										  <th>时间</th>
										  <th>结果</th>
										  <th>审核时长</th>
										  <th>意见</th>
										</tr>
									  </thead>
									  <tbody>
										  <c:forEach var="item" items="${logHumanTaskDtos}">
									    <tr>
										  <td>${item.name}</td>
										  <td><tags:user userId="${item.assignee}"/></td>
										  <td><fmt:formatDate value="${item.completeTime}" type="both"/></td>
										  <td>${item.action}</td>
										  <td>${item.auditDuration}</td>
										   <td>${item.comment}</td>
										</tr>
										  </c:forEach>
									  </tbody>
								</table>
						 </div>
                        </c:if>
       </section>
	<!-- end of main -->
	
		

	</div>
	
	 <c:if test="${isPrint == true}">
                    <div style="width:500px;margin:20px auto;text-align:center">
                        <input value="打印" class="button" onclick="printme();" type="button">
                    </div>
                </c:if>
	 

<input type="hidden" id="hidTotal" name="hidTotal"  />
</form>
</body>

 

<script>

$(function () {

 var hidTotal;

var id=(<%= request.getParameter("processInstanceId")%>); 

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
                	}
                	
                	//画表单
                	for (var num=1;i<hidTotal+1;i++) {
                   	 var html = "<tr id='tr" + num + "_1'><td class='f_td'>撤单编号" + num + "</td><td class='f_r_td'><input type='text' id='ucode" + num + "' name='ucode" + num + "' maxlength='8' class='text0'  readonly /></td>";
                        html = html + "<td class='f_td'>撤单姓名</td><td class='f_r_td'><input type='text' id='userName" + num + "' name='userName" + num + "' class='text0' readonly /></td>";
                        html = html + "<td class='f_td'>编号加入日期</td><td class='f_r_td'><input type='text' id='addTime" + num + "' name='addTime" + num + "' class='text0' readonly /></td>";
                        html = html + "<td rowspan='2'><a href='javascript:;' ></a></td>";
                        html = html + "</tr><tr id='tr" + num + "_2'><td class='f_td'>撤单类型</td><td class='f_r_td'><input type='text'  id='cancelType" + num + "' name='cancelType" + num + "' readonly></td>";
                        html = html + "<td class='f_td'>撤单金额</td><td class='f_r_td'><input type='text' id='cancelMoney" + num + "' name='cancelMoney" + num + "' maxlength='8' class='text0' readonly /></td>";
                        html = html + "<td class='f_td'>业绩单号</td><td class='f_r_td'><input type='text' id='saleId" + num + "' name='saleId" + num + "' maxlength='20' class='text0' readonly /></td></tr>";

                        $(html).insertBefore("#trAddAfter");
                        num = num + 1;
                        
                      //取子表的数据显示出来
                        $.getJSON('${tenantPrefix}/rs/operationCancelOrder/getCancelOrderSubInfo'
                 		       , function(data) {
                 		    	   //alert(JSON.stringify(data));
                 		         	for (var i = 0 ,num = 1; i < data.length; i++,num++) {
                 		                    		 
                 		                    		$("#ucode"+num+"").val(data[i].ucode);
                 		                    		$("#userName"+num+"").val(data[i].userName); 
                 		                    		$("#addTime"+num+"").val(data[i].addTime); 
                 		                    		$("#cancelType"+num+"").val(data[i].cancelType); 
                 		                    		$("#cancelMoney"+num+"").val(data[i].cancelMoney); 
                 		                    		$("#saleId"+num+"").val(data[i].saleID); 
                 		                    	 }
                 		                    });
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

