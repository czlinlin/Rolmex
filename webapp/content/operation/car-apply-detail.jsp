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
		
    </script>
    
</head>

    <style type="text/css">
         #tb1 td{border:1px solid #BBB ;white-space:nowrap ;font-size:14px;height:41px;line-height:41px;}
        .f_td{font-size:12px;white-space:nowrap;font-size:14px;text-align:left;padding-left:6px;}
        .f_r_td{ width:150px; text-align:center;font-size:14px;padding-left:6px;}  
        #tb1  input{border: navajowhite; padding-left:5px ;font-size:14px; height: 100%;width: 100%;}   
        #tb1 tr td{font-size:10pt;font-size:14px;}  
        
        #divPrint{margin:0 auto;}
        .tableform{margin:10px 0;}
        #tb1 td{border:1px solid #CCC;font-size:14px;} */
        .tableprint,.table-border{margin:10px 0 0 0;border-collapse:collapse;width:100%;}
        .tableprint .tdl{white-space:nowrap}
        
        .tableprint,.table-border{margin:10px 0 0 0;border-collapse:collapse;width:100%;}
        .tableprint td{padding-left:20px;padding-right:5px;border:#CCC 1px solid;line-height:35px;font-size:14px;word-break:break-all;word-wrap:break-word}
    	.table-border td{padding-right:5px;border-top:#CCC 1px solid;line-height:35px;font-size:14px;word-break:break-all;word-wrap:break-word}
    </style>
    
<body>
<div id="divPrint">
	<div class="container">
     <section id="m-main" style="padding-top:15px;">
    	<table id="tb1" class="tableform" style="width:100%;">
			<tr>
                <td colspan='4' align='center'><h2>用车申请详情单</h2></td>
            </tr>
            <tr>
                <td colspan='2'  class='f_r_td' align='right' style='padding-right:20px;'>
                    	受理单编号：
                </td>
                <td colspan='2' class="f_td">
               		<div id="applyCode" name="applyCode"></div>
                 </td>
            </tr>
			<tr>
				<td width="130px;" class='f_r_td'> 车牌号： </td>
                <td class="f_td">
                 	<div id="plateNumber" name="plateNumber"></div>
                </td>
                <td width="130px;" class='f_r_td'>驾驶人：</td>
                <td class="f_td">
                	<div id="driver" name="driver"></div>
                </td>
            </tr>
            <tr>    
                <td class='f_r_td'>用车人姓名： </td>
                <td class="f_td">
                	<div id="carUser" name="carUser"></div>
                </td>
               <td class='f_r_td'>部门：</td>
               <td class="f_td">
                   <div id="departmentName" name="departmentName"></div>
               </td>
          	</tr>
          	<tr>
                <td class='f_r_td'>目的地 ：</td>
                <td class="f_td" colspan="3">
                    <div id="destination" name="destination"></div>
                </td>
            </tr>
            <tr>
                <td class='f_r_td'>申请业务类型：  </td>
                <td class="f_td">
                	<div id="busType" name="busType"></div>
				</td>
                <td class='f_r_td'>业务细分：   </td>
                <td class="f_td">
                 	<div id="busDetails" name="busDetails"></div>
                </td>
            </tr>
          	<tr>
            	<td class='f_r_td'>用车事由：</td>
        		<td class="f_td" colspan='3' style="text-align:left;padding-left:6px;">
        			<pre id="content" style="border:none;margin:0;background:#fff;padding-left:0px;">
	        			
        			</pre>
                </td>
        	</tr>
        	<tr>
	           	<td class='f_r_td'>&emsp;&emsp;时间：</td>
				<td  class="f_td" colspan="2"  align='center' >
					<span id="borrowCarTime" name="borrowCarTime"></span>
					&emsp;&emsp;至&emsp;&emsp;
	            	<span id="returnCarTime" name="returnCarTime"></span>
	            </td>
				<td align='center' >
					&emsp;&emsp;共&emsp;&emsp;
					<span id="totalTime" name="totalTime"></span>
					&emsp;&emsp;时
				</td>
	       </tr>
	       <tr>
		       	<td class='f_r_td'>借车里程：  </td>
         		<td class="f_td">
	         		<div id="borrowCarMileage" name="borrowCarMileage"></div> 
	           </td>
	           <td class='f_r_td'>还车里程： </td>
	           <td class="f_td">
	           		<div id="returnCarMileage" name="returnCarMileage"></div> 
	           </td>
          </tr>
          <tr>
             <td class='f_r_td'>行驶里程： </td>
             <td class="f_td" colspan="3">
             	<div id="mileage" name="mileage"></div>
             </td>
          </tr>
          <tr>    
             <td class='f_r_td'>加油金额： </td>
             <td class="f_td">
             	<div id="oilMoney" name="oilMoney"></div> 
             </td>
             <td class='f_r_td'>剩余油量： </td>
             <td class="f_td">
             	<div id="remainOil" name="remainOil"></div>
             </td>
          </tr>
    </table>
 </section>
	<!-- end of main -->
	</div>
	<c:if test="${isPrint == true}">
		    <div class="container">
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
	                		$("#carUser").html(data[i].carUser);
	                		$("#borrowCarTime").html(data[i].borrowCarTime);
	                		$("#returnCarTime").html(data[i].returnCarTime);
	                		$("#totalTime").html(data[i].totalTime);
	                		$("#busType").html(data[i].businessType);
	                		$("#busDetails").html(data[i].businessDetail);
	                		$("#plateNumber").html(data[i].plateNumber);
	                		$("#driver").html(data[i].driver);
	                		
	                		$("#borrowCarMileage").html(data[i].borrowCarMileage);
	                		$("#returnCarMileage").html(data[i].returnCarMileage);
	                		$("#mileage").html(data[i].mileage);
	                		$("#oilMoney").html(data[i].oilMoney);
	                		$("#remainOil").html(data[i].remainOil);
	                	}
	  				});        
	         };          
});




</script>


</html>

