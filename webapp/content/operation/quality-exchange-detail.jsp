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
		     .tableprint{margin:10px 0 0 0;border-collapse:collapse;}
        .tableprint td{padding-left:20px;padding-right:5px;border:#CCCCCC 1px solid;line-height:35px;font-size:14px;}
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
/*          #tb1 td{border:1px solid #BBB ;white-space:nowrap;font-size:10pt;} */
/*         .f_td{ width:120px; font-size:12px;white-space:nowrap ;font-size:10pt;} */
/*         .f_r_td{ width:130px; text-align:left ;font-size:10pt;}   */
/*         #tb1 tr td input{border: navajowhite; padding-left:5px ;font-size:10pt;}    */
/*         #tb1 tr td{text-align:center; font-size:10pt;}   */
        
        #trAddAfter tr td{text-align:center;font-size:10pt;}
        
        .tableform{text-align:center; margin:10px auto;width:100%;}
         #tb1 td{border:1px solid #BBB;}
        .f_td{ width:120px; font-size:12px;white-space:nowrap }
        .f_r_td{ width:130px; text-align:left;}
        #tb1 tr td input{border: navajowhite;text-align:center;width:100%;}  
        #tb1 tr td textarea{border: navajowhite;}    
        #tb1 tr td{text-align:center;} 
        .tdl{white-space:nowrap}       
        
    </style>
    
<body>
 <%@include file="/header/bpm-workspace3.jsp"%>
    <form id="xform" method="post"    class="xf-form" enctype="multipart/form-data">
			质量问题换货-审批	
    <br />
 <div id="divPrint">	
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
    	<input id="exchangeId" name="exchangeId" type="hidden">
		
    	<table id="tb1" class="tableform" width="100%" cellspacing="0" cellpadding="0" border="0" align="center">
			<tr>
                <td colspan='8' align='center' class='f_td'><h2>质量问题换货详情单</h2></td>
            </tr>
            <tr>
                 <td colspan='3'  class='f_td' align='right' style='padding-right:20px;'>
                    	受理单编号：
                 </td>
                <td colspan='5' >
               		<span id="applyCode"></span>
                 </td>
            </tr>
 			<tr>
         	  	<td >
                	<span id='tag_userID'>&nbsp;<span style="color:Red">*</span>经销商编号/手机号</span>：
                </td>
                <td >
                    <span id="ucode"></span>
                </td>
                <td >
                    <span id='tag_realName'>&nbsp;经销商姓名</span>：
                </td>
                <td >
                	<span id="name"></span>
                </td>
               <td class='f_td'>
                    <span id='tag_welfare'>&nbsp;福利级别</span>：
                </td>
                <td >
                    <span id="welfare"></span>
                </td>
                <td class='f_td'>
                    <span id='tag_level'>&nbsp;级别</span>：
                </td>
                <td >
                    <span id="level"></span>
                </td>
            </tr>
            <tr>
                <td >
                    <span id='tag_system'>&nbsp;<span style="color:Red">*</span>所属体系</span>：
                </td>
                <td style="text-align:center;">
					<span id="system"></span>
				</td>
                <td >
                    <span id='tag_seller'>&nbsp;销售人</span>：
                </td>
                <td >
                   <span id="varFather"></span>
                </td>
                <td >
                    <span id='tag_service'>&nbsp;服务人</span>：  
                </td>
                <td >
                     <span id="varRe"></span>
                </td>
                <td >
                    <span id='tag_addTime'>&nbsp;注册时间</span>：
                </td>
                <td >
                     <span id="addTime"></span>
                </td>
            </tr>
<!--             <tr> -->
<!--            	   <td>抄送</td> -->
<%--            	   <td colspan="7" style="text-align:left;">${copyNames}</td> --%>
<!--             </tr> -->
            <tr>
                <td  style="white-space:nowrap">
                    <span id='tag_bustype'>&nbsp;<span style="color:Red">*</span>申请业务类型</span>：
                </td>
                <td style="text-align:left;">
                    	<span id="busType"></span>
				</td>
                <td class='f_td'>
                    <span id='tag_busDetails'>&nbsp;<span style="color:Red">*</span>业务细分</span>：
                </td>
                <td colspan='8' style="text-align:left;">
                    	<span id="busDetails"></span>
                </td>
            </tr>
           <input type = "hidden"  id="businessde" name="businessde" >
           <tr>
                <td class='f_td'>
                    <span id='Span1'>&nbsp;联系电话</span>：
                </td>
                <td >
                     <span id="tel"></span>
                </td>   
                <td width='100px'  class='f_td'>
                    <span id='Span2'>&nbsp;联系地址</span>：
                </td>
                <td colspan='8' >
                    <span id="address"></span>
                </td>
            </tr>
             <tr>
                <td >
                    <span id='tag_sLevel'>&nbsp;<span style="color:Red">*</span>业务级别</span>：
                </td>
                <td style="text-align:left;">
                    <span id="busLevel"></span>
                </td>   
                <td width='100px'  class='f_td'>
                    <span id='tag_belongs'>&nbsp;<span style="color:Red">*</span>所属大区：</span>
                </td>
                <td colspan='8' class='f_r_td' style="text-align:left;">
                     <span id="areaName"></span>
                 </td>
             </tr>
			<tr id="trAddAfter">
	           <td >产品问题:</td>
				<td colspan='8' style='height:100px; text-align:left;' >
				<span id="exchangeReason"></span>
                    <label id="recordNum" style="display: none;text-align:right;" class="control-label col-md-12"></label>
                </td>
        	</tr>
		 </table>
 		 <table>
            <tr>
                <div class="form-group">
                    <label class="control-label col-md-2" name="fileName">历史附件：</label>
                    <div class="col-md-8">
                        <%@include file="/common/show_file.jsp" %>
                    </div>
                </div>
            </tr>
         </table>
		 <c:if test="${isPrint == true}">
		    <div>
		      <table width="100%" cellspacing="0" cellpadding="0" border="0" align="center" class="tableprint"> 
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
	<br/>
	<br/>
</section>
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

	
<input  id="hidTotal" name="hidTotal"  type="hidden" />
<input  id="hidNum" name="hidNum"   type="hidden"/>
</form>
</body>

 

<script>

$(function () {

	
	var id=(<%= request.getParameter("processInstanceId")%>);
	
				if (id !="") {
	                $.getJSON('${tenantPrefix}/rs/QualityExchange/getQualityExchangeInfo', {
	                	id: id
	                }, function(data) {
	                	for (var i = 0; i < data.length; i++) {
	                		//alert(data[i].exchangeProductSub[0].productName); 
	                		 
	                		$("#applyCode").html(data[i].applyCode);
	                		
	                		$("#ucode").html(data[i].ucode);  
	                		$("#name").html(data[i].name);
	                		$("#welfare").html(data[i].welfare);  
	                		$("#level").html(data[i].level);
	                		
	                		$("#system").html(data[i].system);
               			
	                		$("#varFather").html(data[i].varFather);
	                		$("#varRe").html(data[i].varRe);
	                		$("#addTime").html(data[i].addTime);
	                		$("#busType").html(data[i].businessType);
	                		$("#busDetails").html(data[i].businessDetail);
	                		$("#tel").html(data[i].tel);
	                		$("#address").html(data[i].address);
	                		$("#busLevel").html(data[i].businessLevel);
	                		$("#areaName").html(data[i].area);
	                		$("#exchangeReason").html(data[i].exchangeReason);
	                		$("#exchangeId").val(data[i].id);
	                	}
	  				});
	             getProductList();  
            
	         };
            
});


function getProductList() {
	
	var id=(<%= request.getParameter("processInstanceId")%>); 
	
	  $.getJSON('${tenantPrefix}/rs/QualityExchange/getProductList', {
      	id: id
      }, function(data) {
      	for (var num=1  ;num<data.length+1 ;num++) {

      		$("#hidTotal").val(data.length); 
      		$("#hidNum").val(data.length); 
      	
      		var remove = "";
      		//画表单<span id="areaName"></span>
				
					/* var html = "<tr id='tr" + num + "_1'><td class='f_td'>质量产品名称" + num + "</td><td class='f_r_td'><input type='text' id='qualityName" + num + "' name='qualityName" + num + "'  class='text0'  readonly  /></td>";
                      html = html + "<td class='f_td'>产品数量</td><td class='f_r_td'><input type='text' id='qualityNum" + num + "' name='qualityNum" + num + "' class='text0' readonly  /></td>";
                      html = html + "<td style='padding-left:5px;' colspan='2'>产品生产日期</td><td colspan='2'><input type='text' id='manuTime" + num + "' name='manuTime" + num + "' class='text0' readonly /></td></tr>";
                  */
                  var html = "<tr id='tr" + num + "_1'><td class='f_td'>质量产品名称" + num + "</td><td class='f_r_td'><span id='qualityName" + num + "'></span></td>";
                  html = html + "<td class='f_td'>产品数量</td><td class='f_r_td'><span id='qualityNum" + num + "'></span></td>";
                  html = html + "<td style='padding-left:5px;' colspan='2'>产品生产日期</td><td colspan='2'><span id='manuTime" + num + "'></span></td></tr>";
             
                     $(html).insertBefore("#trAddAfter");
    		 
					$("#qualityName"+num+"").html(data[num-1].productName);
					$("#qualityNum"+num+"").html(data[num-1].productNum); 
					$("#manuTime"+num+"").html(data[num-1].productionDate); 
      		}

       });
}


</script>



</html>
