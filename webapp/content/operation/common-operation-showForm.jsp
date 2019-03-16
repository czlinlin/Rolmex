<%@page contentType="text/html;charset=UTF-8"%>
<%@include file="/taglibs.jsp"%>
<%pageContext.setAttribute("currentHeader", "bpm-workspace");%>
<%pageContext.setAttribute("currentMenu", "bpm-process");%>
<!doctype html>
<html lang="en">
<head>
<%@include file="/common/meta.jsp"%>

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
	
	<script type="text/javascript">
		document.onmousedown = function(e) {};
		document.onmousemove = function(e) {};
		document.onmouseup = function(e) {};
		document.ondblclick = function(e) {};

		var xform;
		
		 //调用接口，根据经销商编号，获取直销oa上存的对应信息：姓名 电话 等
		$(function() {
		 	var id=(<%= request.getParameter("processInstanceId")%>); 
				if (id !="") {
			                    $.getJSON('${tenantPrefix}/rs/operationApply/getApplyInfo', {
			                    	id: id
			                    }, function(data) {
			                    	for (var i = 0; i < data.length; i++) {
			                    		//alert(data[i].fileName); 
			                    		$("#userName").html(data[i].userName);  
			                    		$("#welfare").html(data[i].welfare);
			                    		$("#level").html(data[i].level);  
			                    		$("#varFather").html(data[i].varFather);  
			                    		$("#varRe").html(data[i].varRe);  
			                    		$("#addTime").html(data[i].addTime);  
			                    		$("#mobile").html(data[i].mobile);  
			                    		$("#address").html(data[i].address);  
			                    		$("#ucode").html(data[i].ucode);  
			                    		$("#busType").html(data[i].businessType);
			                    		$("#busDetails").html(data[i].businessDetail);  
			                    		$("#busLevel").html(data[i].businessLevel); 
			                    		$("#span_content").html(data[i].applyContent); 
			                    		$("#area").html(data[i].area); 
			                    		$("#system").html(data[i].system); 
			                    		$("#standard").html(data[i].businessStand1);
			                    		$("#standard2").html(data[i].businessStand2);
			                    		$("#treeInfo").html(data[i].treeInfo);
			                    		$("#submitTimes").html(data[i].submitTimes);
			                    		$("#applyCode").html(data[i].applyCode);
			                    		$("#apply_edit_before").after(data[i].fileName);
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
        function MaxWords(){

            if($("#applyContent").val().length == "4000"){
                alert("申请内容字数已达上限4000字");
            }
        }

    </script>
</head>
<style type="text/css">
		.tableform{text-align:center; margin:10px auto;width:100%;}
         #tb1 td{border:1px solid #BBB;height:33px;line-height:33px;font-size:14px;}
        .f_td{ width:120px; font-size:12px;white-space:nowrap }
        .f_r_td{ width:130px; text-align:left;}
        #tb1 tr td input{border: navajowhite;text-align:center;}  
        #tb1 tr td textarea{border: navajowhite;}    
        #tb1 tr td{text-align:center;} 
        .tdl{white-space:nowrap}       
    </style>
<body>

 	<form id="xform" method="post"    class="xf-form" enctype="multipart/form-data">
	 <div id="divPrint">			
<div class="container">
    <section id="m-main" class="col-md-12" style="padding-top:15px;">
		<input id ="filePath" name="filePath"  type="hidden" >
		<input id="processInstanceId" type="hidden" name="processInstanceId" value="<%= request.getParameter("processInstanceId")%>">
		<input id="humanTaskId" type="hidden"  name="humanTaskId" value="<%= request.getParameter("humanTaskId")%>">
    <table id="tb1" class="tableform">
    	 <tr>
                <td colspan='8' align='center' class='f_td'>
                    <h2>业务受理详情单</h2>
                </td>
        </tr>
        <tr>
                <td colspan='2'  class='f_td  tdl' align='right' style='padding-right:20px;'>
                    	提交次数：
                </td>
                <td colspan='2' >
               		 <div class="xf-handler" id="submitTimes" name="submitTimes" style="text-align:center">
	                 <!-- <input id="submitTimes" name="submitTimes" readonly> -->
					</div>
               	</td>
                <td colspan='2'  class='f_td' align='right' style='padding-right:20px;'>
                    	受理单编号：
                </td>
	            <td colspan='2' >
		           <div class="xf-handler" id="applyCode" name="applyCode" style="text-align:center">
		                 <!-- <input id="applyCode" name="applyCode" readonly> -->
					</div>
	           </td>
            </tr>
            
    	<tr>
                <td class="f_td">
                    <span id='userID'>&nbsp;经销商编号</span>：
                </td>
                <td >
                	<div class="xf-handler" id="ucode" name="ucode" style="text-align:center">
		                 <!--  <input id ="ucode" name="ucode" type="text" maxlength="8"  readonly /> -->
					</div>
                </td>
                <td >
                    <span id='realName'>&nbsp;经销商姓名</span>：
                </td>
                <td >
                	<div class="xf-handler" id="userName" name="userName" style="text-align:center">
		                 <!--  <input name="userName" id="userName"   readonly>  -->
					</div>
                 </td>
               <td >
                    <span id='wf'>&nbsp;福利级别</span>：
                </td>
                <td >
                	<div class="xf-handler" id="welfare" name="welfare" style="text-align:center">
		                 <!--  <input name="welfare" id="welfare"   readonly>   -->
					</div>
                </td>
                <td >
                    <span id='tag_level'>&nbsp;级别</span>：
                </td>
                <td >
                	<div class="xf-handler" id="level" name="level" style="text-align:center">
		                 <!--  <input name="level" id="level"   readonly>    -->
					</div>
                </td>
           </tr>
           <tr>
                <td >
                    <span id='tag_system'>&nbsp;所属体系</span>：
                </td>
                <td >
                	<div class="xf-handler" id="system" name="system" style="text-align:center">
		                 <!--  <input name="system" id="system"   readonly>   -->
					</div>
                </td>
                <td >
                    <span id='seller'>&nbsp;销售人</span>：
                </td>
                <td >
                	<div class="xf-handler" id="varFather" name="varFather" style="text-align:center">
		                 <!--   <input name="varFather" id="varFather"   readonly>   -->
					</div>
                </td>
                <td >
                    <span id='tag_service'>&nbsp;服务人</span>：  
                </td>
                <td >
                	<div class="xf-handler" id="varRe" name="varRe" style="text-align:center">
		                 <!--   <input name="varRe" id="varRe"   readonly>   -->
					</div>
                </td>
                <td >
                    <span id='tag_addTime'>&nbsp;注册时间</span>：
                </td>
                <td >
                	<div class="xf-handler" id="addTime" name="addTime" style="text-align:center">
		                 <!--   <input name="addTime" id="addTime"   readonly>    -->
					</div>
               </td>
            </tr>
            <tr>
                <td  style="white-space:nowrap">
                    <span id='tag_bustype'>&nbsp;申请业务类型</span>：
                </td>
                <td  colspan='3'>
                	<div class="xf-handler" id="busType" name="busType" style="text-align:center">
		                 <!--   <input name="busType" id="busType"  readonly>   -->
					</div>
                </td>
                <td class='f_td'>
                    <span id='tag_busDetails'>&nbsp;业务细分</span>：
                </td>
                <td colspan='3'>
                	<div class="xf-handler" id="busDetails" name="busDetails" style="text-align:center">
		                 <!--   <input id="busDetails" name="busDetails" readonly>   -->
					</div>
                </td>
            </tr>
           <tr>
                <td>
                    <span id='Span1'>&nbsp;联系电话</span>：
                </td>
                <td colspan='3'>
                	<div class="xf-handler" id="mobile" name="mobile" style="text-align:center">
		                 <!--   <input name="mobile" id="mobile"  readonly>   -->
					</div>
                 </td>   
                <td>
                    <span id='Span2'  >&nbsp;联系地址</span>：
                </td>
                <td colspan='3'>
                	<div class="xf-handler" id="address" name="address" style="text-align:center">
		                 <!--   <input name="address" id="address"    readonly>   -->
					</div>
               </td>
            </tr>
            <tr>
                <td>
                    <span id='tag_sLevel'>&nbsp;业务级别</span>：
                </td>
                <td colspan='3'>
                	<div class="xf-handler" id="busLevel" name="busLevel" style="text-align:center">
		                 <!--   <input name="busLevel" id="busLevel"  readonly>   -->
					</div>
                </td>   
                <td width='100px'  >
                    <span id='tag_belongs'>&nbsp;所属大区：</span>
                </td>
                <td colspan='3'>
                	<div class="xf-handler" id="area" name="area" style="text-align:center">
		                 <!--   <input name="area" id="area"    readonly>   -->
					</div>
                </td>
            </tr>
            <tr id="apply_edit_before">
            	<td  class='f_td'>抄送：</td>
                <td id="copyNames" colspan='7' style="text-align:left;" class='f_td'>
                    	<%-- ${taskCopyNames} --%>
                </td>
            </tr>
            <tr>
                <td colspan='8' align='center' class='f_td'>
                   	 申请内容
                </td>
            </tr>
            <tr>
                <td colspan='8' >
                	<p style="white-space:pre-wrap;text-align:left; padding:5px 30px;"><span id="span_content"></span></p>
                </td>
            </tr>
            
            <tr>
                <td colspan='3' align='center' class='f_td'>
                   		 业务标准(现场办理)
                </td>
                <td colspan='3' align='center' class='f_td'>
                   		 业务标准(非现场办理)
                </td>
                <td colspan='2' align='center' class='f_td'>
                   		 点位信息
                </td>
            </tr>
            <tr>
                <td colspan='3' style='height:auto; width:300px; white-space:normal; ' class='leftAlign'>
                   <p style="white-space:pre-wrap;text-align:left; padding:5px 30px;"><span id="standard" name="standard"></span></p>
                </td>
                <td colspan='3' style='height:auto; width:300px; white-space:normal; ' class='leftAlign'>
                   <p style="white-space:pre-wrap;text-align:left; padding:5px 30px;"><span id="standard2" ></span></p>
                </td>
                <td colspan='2'  style='height:auto; width:200px;white-space:normal;' class='leftAlign'>
                   <p style="white-space:pre-wrap;text-align:left; padding:5px 30px;"><span id="treeInfo" ></span></p>
                </td>
            </tr>
             <tr>
                <td colspan="2"><label style="text-align:center;">历史附件:</label></td>
                <td colspan="6"><%@include file="/common/show_file.jsp" %></td>
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
	<div class="container" style="padding:20px 0px;">
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
</div>
 </div>

 </section>




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
</html>

