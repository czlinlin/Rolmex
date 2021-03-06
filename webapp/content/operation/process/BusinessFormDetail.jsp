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
    <script type="text/javascript" src="${cdnPrefix}/userpicker3-v2/userpickerbybpm.js"></script>
	<script type="text/javascript" src="${cdnPrefix}/operation/TaskOperation.js"></script>
	
	<style type="text/css">
        .xf-handler {
            cursor: auto;
            font-size:14px;
        }
        .tableprint{margin:10px 0 0 0;border-collapse:collapse;}
        .tableprint td{padding-left:20px;padding-right:5px;border:#CCCCCC 1px solid;line-height:35px;font-size:14px;}
        .tdl{white-space:nowrap}
		pre {
			white-space: pre-wrap;
			word-wrap: break-word;
			background-color:white;
            border:0px
		}
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
	<script type="text/javascript">
		
		//接收请求数据
		$(function() {
		 	var id=(<%= request.getParameter("processInstanceId")%>); 
				if (id !="") {
			    	$.getJSON('${tenantPrefix}/rs/processBusiness/getBusinessInfo', {
			        	id: id
			        	}, function(data) {
			            	for (var i = 0; i < data.length; i++) {
			            	   //alert(JSON.stringify(data)); 
			                   /* $("#theme").html(data[i].theme);
			                   $("#cc").html(data[i].cc);  
			                   $("#businessType").html(data[i].businessType);  
			                   $("#businessDetail").html(data[i].businessDetail);  
			                   $("#businessLevel").html(data[i].businessLevel);  
			                   $("#initiator").html(data[i].initiator);  
			                   $("#area").html(data[i].area);  
			                   $("#branchOffice").html(data[i].branchOffice);  
			                   //$("#applyContent").html(data[i].applyContent); 
			                   $("#submitTimes").html(data[i].submitTimes);
			                   $("#applyCode").html(data[i].applyCode); */
			                   
			                 //20180328 cz 业务细分是'启明' 的申请，标题改成 "业务详情单（属地项目部）"
			                   if(data[i].businessDetail=="启明活动"){
		                        	var h1= document.getElementsByTagName("h1")[0];
		        			  	 	h1.innerHTML = "业务详情单（属地项目部）";
		                        }
			                   //ckx 2018/9/29
			                   replace(data[i].branchOffice,data[i].applyCode);
                   			}
		                });
		         };
		})
		
		function replace(branchOffice,applyCode){
			 $.ajax({      
			        url: "${tenantPrefix}/workOperationCustom/getBusinessDetail.do",      
			        datatype: "json",
			        data:{applyCode:applyCode},
			        type: 'get',      
			        success: function (data) {
			        	if("true" == data){
			        		$("#hId").html("启明项目详情单")
			        		$("#areaLabelId").html('<font color="red">*</font>&nbsp;属地区域：');
			      		  	$("#branchOfficeLabelId").html('<font color="red">*</font>&nbsp;属地项目部：');
			      		  	$("#branchOffice").html(branchOffice.substring(0,branchOffice.length-3)+"项目部");
			        	}
			    		
			        },      
			        error: function(e){
			        	//失败后回调      
			            alert("服务器请求失败,请重试");  
			        }
			   });
			 
		}
		
		
        function MaxWords(){
            if($("#applyContent").val().length == "4001"){
                alert("申请内容字数已达上限4000字");
            }
        }

    </script>
  </head>

  <body>
<form id="xform" method="post" class="xf-form" enctype="multipart/form-data">
<div id="divPrint">
    <div class="container">

	  <!-- start of main -->
      <section id="m-main" class="col-md-12" style="padding-top:65px;">
		
		<div id="xf-form-table">
			<div id="xf-1" class="xf-section">
				<c:if test="${isArea == true }">
					<c:if test="${!empty title }">
						<h1 id="hId" style="text-align:center;">${title }</h1>
					</c:if>
					<c:if test="${empty title }">
						<h1 id="hId" style="text-align:center;">业务详情单（分公司）</h1>
					</c:if>
				</c:if>
				<c:if test="${isArea != true }">
					<h1 id="hId" style="text-align:center;">业务详情单（分公司）</h1>
				</c:if>
			</div>
			
			<div id="xf-2" class="xf-section">
				<table class="xf-table" width="100%" cellspacing="0" cellpadding="0" border="0" align="center">
					<tbody>
						<tr id="xf-2-0">
							<td id="xf-2-0-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" colspan="4">
								<div class="xf-handler">
									<label style="display:block;text-align:right;margin-bottom:0px;">提交次数：<span id="submitTimes">${business.submitTimes}</span>      &nbsp;&nbsp;申请单号:<span id="applyCode">${business.applyCode}</span></label>
								</div>
							</td>
						</tr>
						<tr id="xf-2-1">
							<td id="xf-2-1-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left" width="25%">
								<div class="xf-handler">
									<label class="tdl" style="display:block;text-align:center;margin-bottom:0px;">主题：</label>
								</div>
							</td>
							<td id="xf-2-1-1" class="xf-cell xf-cell-right xf-cell-bottom" colspan="3">
								<div class="xf-handler">
									<span id="theme">${business.theme}</span>
								</div>
							</td>
						</tr>
						<tr id="xf-2-2">
							<td id="xf-2-2-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">抄送：</label>
								</div>
							</td>
							<td id="xf-2-2-1" class="xf-cell xf-cell-right xf-cell-bottom" colspan="3">
								<div class="xf-handler">
									<span id="cc">${business.cc}</span>
								</div>
							</td>
						</tr>
						<tr id="xf-2-3">
							<td id="xf-2-3-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;"><font color='red'>*</font>&nbsp;申请业务类型：</label>
								</div>
							</td>
							<td id="xf-2-3-1" class="xf-cell xf-cell-right xf-cell-bottom">
								<div class="xf-handler">
									<span id="businessType">${business.businessType}</span>
								</div>
							</td>
							<td id="xf-2-3-2" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;"><font color='red'>*</font>&nbsp;业务细分：</label>
								</div>
							</td>
							<td id="xf-2-3-3" class="xf-cell xf-cell-right xf-cell-bottom">
								<div class="xf-handler">
									<span id="businessDetail">${business.businessDetail}</span>
								</div>
							</td>
						</tr>
						
						<tr id="xf-2-4">
							<td id="xf-2-4-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;"><font color='red'>*</font>&nbsp;业务级别：</label>
								</div>
							</td>
							<td id="xf-2-4-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" >
								<span id="businessLevel">${business.businessLevel}</span>
							</td>
							<td id="xf-2-4-2" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
								<label style="display:block;text-align:center;margin-bottom:0px;">发起人：</label>
							</td>
							<td id="xf-2-4-3" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
								<div class="xf-handler" id="initiator">${business.initiator}</div>
							</td>
						</tr>
						<c:if test="${isArea == true }">
						<tr id="xf-2-5">
							<td id="xf-2-5-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
								<div class="xf-handler">
									<label id="areaLabelId" style="display:block;text-align:center;margin-bottom:0px;"><font color='red'>*</font>&nbsp;大区：</label>
								</div>
							</td>
							<td id="xf-2-5-1" colspan="3" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
								<div class="xf-handler">
									<span id="area">${business.area}</span>
								</div>
							</td>
							<%-- <td id="xf-2-5-2" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
								<div class="xf-handler">
									<label id="branchOfficeLabelId" style="display:block;text-align:center;margin-bottom:0px;"><font color='red'>*</font>&nbsp;分公司：</label>
								</div>
							</td>
							<td id="xf-2-5-3" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
								<div class="xf-handler">
									<span id="branchOffice">${business.branchOffice}</span>
								</div>
							</td> --%>
						</tr>
						</c:if>
						<c:if test="${isArea != true }">
							<tr id="xf-2-5">
								<td id="xf-2-5-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
									<div class="xf-handler">
										<label id="areaLabelId" style="display:block;text-align:center;margin-bottom:0px;"><font color='red'>*</font>&nbsp;大区：</label>
									</div>
								</td>
								<td id="xf-2-5-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
									<div class="xf-handler">
										<span id="area">${business.area}</span>
									</div>
								</td>
								<td id="xf-2-5-2" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
									<div class="xf-handler">
										<label id="branchOfficeLabelId" style="display:block;text-align:center;margin-bottom:0px;"><font color='red'>*</font>&nbsp;分公司：</label>
									</div>
								</td>
								<td id="xf-2-5-3" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
									<div class="xf-handler">
										<span id="branchOffice">${business.branchOffice}</span>
									</div>
								</td>
							</tr>
						</c:if>

						<tr id="xf-2-6">
							<td id="xf-2-6-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;"><font color='red'>*</font>&nbsp;申请内容</label>
								</div>
							</td>
							<td id="xf-2-6-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" colspan="3">
								<pre>${business.applyContent}</pre>
							</td>
						</tr>
						
						<tr id="xf-2-8">
							<td id="xg-2-8-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
								<label style="display:block;text-align:center;margin-bottom:0px;">附件:</label>
							</td>
							<td id="xf-2-8-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" colspan="3">
                   			 	<%@include file="/common/show_file.jsp" %>
							</td>
						</tr>
						
					</tbody>
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
					<c:if test="${item.action != '提交' && item.action != '重新申请'}">
						<tr style="border-top:0px hidden;">
							<td>批示内容</td>
							<td colspan="4"><pre>${item.comment}</pre></td>
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

</html>
