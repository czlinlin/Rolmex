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
		.xf-table td{border:1px solid gray}
        .tableprint{width:100%;margin:10px 0 0 0;border-collapse:collapse;}
        .tableprint td{padding-left:20px;padding-right:5px;border:#CCCCCC 1px solid;line-height:35px;font-size:14px;}
        .divPrint table{font-size:14px;font-weight:mormal;border-collapse:collapse;}
        .divPrint table td{border:#CCCCCC 1px solid;}
        .tdl{white-space:nowrap}
        
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

    <style type="text/css">
         #tb1 td{border:1px solid #ccc }
        .f_td{font-size:12px;white-space:nowrap }
        .f_r_td{text-align:left;}  
        #tb1 tr td input{border: navajowhite;text-align:center;}   
        #tb1 tr td{text-align:center;}  
    </style>
    
<body>
    <form id="xform" method="post"    class="xf-form" enctype="multipart/form-data">
    <div id="divPrint">
    <div class="container">
    		
     <section id="m-main" class="col-md-12" style="padding-top:65px;">

		<input id="processInstanceId" type="hidden" name="processInstanceId" value="<%= request.getParameter("processInstanceId")%>">
		<input id="humanTaskId" type="hidden"  name="humanTaskId" value="<%= request.getParameter("humanTaskId")%>">
		<div>
    	<table id="tb1" class="xf-table" width="100%" cellspacing="0" cellpadding="0" border="0" align="center">
    		 <tr>
                <td colspan='8' align='center' class='f_td'><h2>撤单登记表</h2></td>
            </tr>
            <tr>
                <td colspan='1'  class='f_td tdl'  align='right' style='padding-right:20px;'>
                    	提交次数：
                </td>
                <td colspan='1' >
                	<div class="xf-handler" id="submitTimes" name="submitTimes" style="text-align:center">
	                 <!-- <input id="submitTimes" name="submitTimes" readonly> -->
					</div>
               	</td>
                 <td colspan='1'  class='f_td' align='right' style='padding-right:20px;'>
                    	受理单编号：
                  </td>
	              <td colspan='3' >
	              	<div class="xf-handler" id="applyCode" name="applyCode" style="text-align:center">
		                 <!-- <input id="applyCode" name="applyCode" readonly> -->
					</div>
	              </td>
            </tr>
 			 <tr>
         	    <td ><span id='tag_shopCode'>&nbsp;店编号</span>：</td>
                <td >
	                <div class="xf-handler" id="shopCode" name="ucode" style="text-align:center">
	                 <!-- <input id ="shopCode" name="ucode" type="text" maxlength="8" readonly/>-->
					</div>
               </td>
                <td ><span id='tag_shopName'>&nbsp;店姓名</span>：</td>
                <td >
                	<div class="xf-handler" id="shopName" name="shopName" style="text-align:center">
	                 <!-- <span name="shopName" id="shopName" readonly> -->
					</div>
               </td>
                <td ><span id='tag_shopMobile'>&nbsp;店电话</span>：</td>
                <td >
                	<div class="xf-handler" id="shopMobile" name="shopMobile" style="text-align:center">
	                 <!-- <input name="shopMobile" id="shopMobile" readonly> -->
					</div>
                </td>
            </tr>
            <tr>
         	    <td ><span id='tag_mobile'>&nbsp;来电电话</span>：</td>
                <td >
                	<div class="xf-handler" id="mobile" name="mobile" style="text-align:center">
	                 <!-- <input id ="mobile" name="mobile" type="text" readonly/> -->
					</div>
                </td>
                
                 <jsp:useBean id="time" class="java.util.Date"/>  
       			<td ><span id='tag_registerTime'>&nbsp;撤单登记时间</span>：</td>
					
                <td >
                	<div class="xf-handler" id="registerTime" name="registerTime" style="text-align:center">
	                 <!-- <input name="registerTime" id="registerTime" readonly> -->
					</div>
                
                 </td>
                <td ><span id='tag_registerName'>&nbsp;登记人</span>：</td>
                <td >
                	<div class="xf-handler" id="registerName" name="registerName" style="text-align:center">
	                 <!-- <input name="registerName" id="registerName" readonly> -->
					</div>
                </td>
             </tr>
             <tr>
         	    <td ><span id='copyId'>&nbsp;抄送</span>：</td>
                <td colspan='5' id="copyNames" style="text-align:left"></td>
             </tr>
            <tr id="trAddAfter">
	            <td >是否核实</td>
	            <td >
	            	<div class="xf-handler" id="isChecked" name="isChecked" style="text-align:center">
	                 <!-- <input name="isChecked" id="isChecked" readonly> -->
					</div>
				</td>
	            <td >撤单备注</td>
	            <td  colspan="3">
	            	<div class="xf-handler" id="cancelRemark" name="cancelRemark" style="text-align:center">
	                 <!-- <input type="text" id="cancelRemark" name="cancelRemark"  size="60" readonly /> -->
					</div>
				</td>
        	</tr>
        
            </table>
       	</div>
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
					<c:if test="${item.action != '提交' && item.action != '重新调整申请'}">
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
       </section>
	<!-- end of main -->
	</div>
	
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
                		$("#shopCode").html(data[i].ucode);  
                		$("#shopName").html(data[i].shopName);
                		$("#shopMobile").html(data[i].shopMobile);  
                		$("#mobile").html(data[i].mobile);
                		$("#registerTime").html(data[i].registerTime);
                		$("#registerName").html(data[i].registerName);
                		$("#isChecked").html(data[i].isChecked);
                		$("#cancelRemark").html(data[i].cancelRemark);
                		$("#submitTimes").html(data[i].submitTimes);
                		$("#applyCode").html(data[i].applyCode);
                		
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

