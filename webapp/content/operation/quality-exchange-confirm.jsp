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
                       "actionUrl": '${tenantPrefix}/QualityExchange/ExchangeApproval-completeTask.do?flag=' + flag,
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
         #tb1 td{border:1px solid #BBB ;white-space:nowrap ;font-size:10pt;}
        .f_td{ width:120px; font-size:12px;white-space:nowrap;font-size:10pt; }
        .f_r_td{ width:130px; text-align:left;font-size:10pt;}  
        #tb1 tr td input{border: navajowhite; padding-left:5px ;font-size:10pt;}   
        #tb1 tr td{text-align:center; font-size:10pt;}  
         #trAddAfter tr td{text-align:center;font-size:10pt;}
    </style>
    
<body>
 <%@include file="/header/bpm-workspace3.jsp"%>
    <form id="xform" method="post"    class="xf-form" enctype="multipart/form-data">
			质量问题换货审批单
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
    	<input id="exchangeId" name="exchangeId" type="hidden">
		
    	<table id="tb1" >
    		
			<tr>
                <td colspan='8' align='center' class='f_td'><h2>质量问题换货审批单</h2></td>
            </tr>
            <tr>
<!--                 <td colspan='1'  class='f_td' align='right' style='padding-right:20px;'> -->
<!--                     	提交次数： -->
                    	
<!--                 </td> -->
<!--                 <td colspan='3' > -->
<!--                		<input id="submitTimes" name="submitTimes" readonly> -->
<!--                  </td> -->
                 <td colspan='3'  class='f_td' align='right' style='padding-right:20px;'>
                    	受理单编号：
                 </td>
                <td colspan='5' >
               		<input id="applyCode" name="applyCode" value="${code}" readonly>
                 </td>
            </tr>
 			
			
 			<tr>
         	  	<td >
                	<span id='tag_userID'>&nbsp;<span style="color:Red">*</span>经销商编号/手机号</span>：
                </td>
                <td >
                    <input id ="ucode" name="ucode" type="text" maxlength="8" readonly  />
                </td>
                <td >
                    <span id='tag_realName'>&nbsp;经销商姓名</span>：
                </td>
                <td >
                	<input name="name" id="name" readonly> 
                </td>
               <td class='f_td'>
                    <span id='tag_welfare'>&nbsp;福利级别</span>：
                </td>
                <td >
                    <input name="welfare" id="welfare" readonly> 
                </td>
                <td class='f_td'>
                    <span id='tag_level'>&nbsp;级别</span>：
                </td>
                <td >
                    <input name="level" id="level" readonly>
                </td>
            </tr>
           
           
            <tr>
                <td >
                    <span id='tag_system'>&nbsp;<span style="color:Red">*</span>所属体系</span>：
                </td>
                <td style="text-align:left;">

					<div id="system" name="system"  readonly></div>

                </td>
                <td >
                    <span id='tag_seller'>&nbsp;销售人</span>：
                </td>
                <td >
                    <input name="varFather" id="varFather"  readonly>
                </td>
                <td >
                    <span id='tag_service'>&nbsp;服务人</span>：  
                </td>
                <td >
                     <input name="varRe" id="varRe"    readonly> 
                </td>
                <td >
                    <span id='tag_addTime'>&nbsp;注册时间</span>：
                </td>
                <td >
                     <input name="addTime" id="addTime"   readonly> 
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
                    	<input id="busType" name="busType"  readonly>
				</td>
                <td class='f_td'>
                    <span id='tag_busDetails'>&nbsp;<span style="color:Red">*</span>业务细分</span>：
                </td>
                <td colspan='8' style="text-align:left;">
                    	<input id="busDetails" name="busDetails" readonly>
                </td>
            </tr>
           <input type = "hidden"  id="businessde" name="businessde" >
           
           
           
           <tr>
                <td class='f_td'>
                    <span id='Span1'>&nbsp;联系电话</span>：
                </td>
                <td >
                     <input name="tel" id="tel" readonly> 
                </td>   
                <td width='100px'  class='f_td'>
                    <span id='Span2'>&nbsp;联系地址</span>：
                </td>
                <td colspan='8' width='100px'>
                    <input style="width: 100%; height: 100%"  name="address" id="address" readonly> 
                </td>
            </tr>
            
            
             <tr>
                <td >
                    <span id='tag_sLevel'>&nbsp;<span style="color:Red">*</span>业务级别</span>：
                </td>
                <td style="text-align:left;">
                    <input   name="busLevel" id="busLevel" readonly/>
                </td>   
                <td width='100px'  class='f_td'>
                    <span id='tag_belongs'>&nbsp;<span style="color:Red">*</span>所属大区：</span>
                </td>
                <td colspan='8' class='f_r_td' style="text-align:left;">
                     <%-- <tags:party partyId='${areaId}'/> --%>
                     <input id="areaName" type="text" name="areaName" value="<tags:party partyId='${areaId}'/>"  readonly> 
                 </td>
             </tr>


            <tr id="trAddAfter">
	           
				<td >产品问题:</td>
				<td colspan='8' style='height:100px; text-align:left;' >
                    <textarea  maxlength="5000"  name="exchangeReason" id="exchangeReason" rows="3" cols="28" class="text0" style="height:99px;width:99%;padding-left:10px;padding-top:10px" readonly></textarea>
                    <label id="recordNum" style="display: none;text-align:right;" class="control-label col-md-12"></label>
                </td>
        	
        	</tr>
        	
        	<tr>
                   <td colspan='8' align='center' class='f_td'>
                        <span style="color:Red">*</span>批示内容
                   </td>
            </tr>
            <tr>
                <td colspan="8" style="height:80px; text-align:left;">
                    <textarea maxlength="300"  name="comment" id="comment" rows="2" cols="20" class="text0" style="height:79px;width:1100px" 
                    onfocus="if(value=='同意'){value=''}"  onblur="if (value ==''){value='同意'}">同意</textarea>
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
            <table class="col-md-10" style="margin: 0 15px;">
                <tr>
                    <td style="width:100px"><code>*</code>操作密码：</td>
                    <td>
                        <input name="txtPrivateKey" type="password" maxlength="25" id="txtPrivateKey"
                               style="float: left;" onblur='isPwd();'/>
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
	
<input type="hidden" id="hidTotal" name="hidTotal"  />
</form>
</body>

 

<script>

$(function () {

	 var hidTotal;
	 var hidNum;
	var id=${processInstanceId}; 

				if (id !="") {
	                $.getJSON('${tenantPrefix}/rs/QualityExchange/getQualityExchangeInfo', {
	                	id: id
	                }, function(data) {
	                	for (var i = 0; i < data.length; i++) {
	                		//alert(data[i].exchangeProductSub[0].productName); 
	                		$("#applyCode").val(data[i].applyCode);  
	                		
	                		$("#ucode").val(data[i].ucode);  
	                		$("#name").val(data[i].name);
	                		$("#welfare").val(data[i].welfare);  
	                		$("#level").val(data[i].level);
	                		
	                		$("#system").html(data[i].system);
               			
	                		$("#varFather").val(data[i].varFather);
	                		$("#varRe").val(data[i].varRe);
	                		$("#addTime").val(data[i].addTime);
	                		$("#busType").val(data[i].businessType);
	                		$("#busDetails").val(data[i].businessDetail);
	                		$("#tel").val(data[i].tel);
	                		$("#address").val(data[i].address);
	                		$("#busLevel").val(data[i].businessLevel);
	                		$("#areaName").val(data[i].area);
	                		$("#exchangeReason").val(data[i].exchangeReason);
	                		$("#exchangeId").val(data[i].id);
	                	}
	  				});
	             getProductList();  
            
	         };
            
});


function getProductList() {
	
	var id=${processInstanceId}; 
	
	  $.getJSON('${tenantPrefix}/rs/QualityExchange/getProductList', {
      	id: id
      }, function(data) {
      	for (var num=1  ;num<data.length+1 ;num++) {

      		$("#hidTotal").val(data.length); 
      		$("#hidNum").val(data.length); 
      	
      		var remove = "";
      		//画表单
				
					
					var html = "<tr id='tr" + num + "_1'><td class='f_td'>质量产品名称" + num + "</td><td class='f_r_td'><input type='text' id='qualityName" + num + "' name='qualityName" + num + "'   class='text0'  readonly  /></td>";
                      html = html + "<td class='f_td'>产品数量</td><td class='f_r_td'><input type='text' id='qualityNum" + num + "' name='qualityNum" + num + "' class='text0' readonly  /></td>";
                      html = html + "<td style='padding-left:5px;' colspan='2'>产品生产日期</td><td colspan='2'><input type='text' id='manuTime" + num + "' name='manuTime" + num + "' class='text0' readonly /></td></tr>";
                 
                     $(html).insertBefore("#trAddAfter");
    		 
					$("#qualityName"+num+"").val(data[num-1].productName);
					$("#qualityNum"+num+"").val(data[num-1].productNum); 
					$("#manuTime"+num+"").val(data[num-1].productionDate); 
      		}

       });
}


</script>


</html>

