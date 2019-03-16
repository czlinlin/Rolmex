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
	<%-- <script type="text/javascript" src="${cdnPrefix}/userpicker3-v2/userpickercustom.js"></script> --%>
	
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
        function MaxWords() {
            if ($("#theme").val().length == "101") {
                alert("主题字数已达上限100字");
            }
            if ($("#applyContent").val().length == "4001") {
                alert("申请内容字数已达上限4000字");
            }
        }
    </script>
	<script type="text/javascript">
		
			//接收请求数据
			$(function() {
			 	var id=(<%= request.getParameter("processInstanceId")%>); 
					if (id !="") {
				    	$.getJSON('${tenantPrefix}/rs/processLllegalFreeze/getLllegalFreezeInfo', {
				        	id: id
				        	}, function(data) {
				            	for (var i = 0; i < data.length; i++) {
				            	   //alert(JSON.stringify(data));
				            	   $("#submitTimes").html(data[i].submitTimes);
				            	   $("#applyCode").html(data[i].applyCode);
				                   $("#theme").html(data[i].theme);
				                   $("#cc").html(data[i].cc);  
				                   $("#businessType").html(data[i].businessType);  
				                   $("#businessDetail").html(data[i].businessDetail);  
				                   $("#businessLevel").html(data[i].businessLevel);  
				                   $("#initiator").html(data[i].initiator);
				                   $("#ucode").html(data[i].ucode);
				                   $("#name").html(data[i].name);
				                   $("#welfareLevel").html(data[i].welfareLevel);  
				                   $("#qualificationsStatus").html(data[i].qualificationsStatus);  
				                   $("#system").html(data[i].system);  
				                   $("#contact").html(data[i].contact);  
				                   $("#area").html(data[i].area);  
				                   $("#branchOffice").html(data[i].company);  
				                   $("#idNumber").html(data[i].idNumber);
				                   $("#aboveBoard").html(data[i].aboveBoard);  
				                   $("#directorContact").html(data[i].directorContact);  
				                   $("#applyMatter").html(data[i].applyMatter);  
				                   //$("#applyContent").html(data[i].applyContent);
				                   
				                }
				           });
			         };
			})	
    </script>

  </head>

  <body>
<form id="xform" method="post" class="xf-form" enctype="multipart/form-data">
<div id="divPrint">
    <div class="container">

	  <!-- start of main -->
      <section id="m-main" class="col-md-12" style="padding-top:65px;">

		<script>
		</script>
		<div id="xf-form-table">
			<div id="xf-1" class="xf-section">
				<h1 style="text-align:center;">违规冻结/解冻详情单</h1>
			</div>
			
			<div id="xf-2" class="xf-section">
				<table class="xf-table" width="100%" cellspacing="0" cellpadding="0" border="0" align="center">
					<tbody>
						<tr id="xf-2-0">
							<td id="xf-2-0-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" colspan="4" width="100%">
								<div class="xf-handler">
									<label style="display:block;text-align:right;margin-bottom:0px;">提交次数： <span id="submitTimes"></span>      &nbsp;&nbsp;申请单号: <span id="applyCode"></span></label>
								</div>
							</td>
						</tr>
						<tr id="xf-2-1">
							<td id="xf-2-1-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left" width="25%">
								<div class="xf-handler">
									<label class="tdl" style="display:block;text-align:center;margin-bottom:0px;"><font color='red'>*</font>&nbsp;主题：</label>
								</div>
							</td>
							<td id="xf-2-1-1" class="xf-cell xf-cell-right xf-cell-bottom" colspan="3" width="75%">
								<div id="theme"></div>
							</td>
						</tr>
						<tr id="xf-2-2">
							<td id="xf-2-2-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left" width="25%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">抄送：</label>
								</div>
							</td>
							<td id="xf-2-2-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top" colspan="3" width="75%">
								<div class="input-group userPicker">
								 	<span id="cc"></span>
							    </div>
							</td>
							
						</tr>
						<tr id="xf-2-3">
							<td id="xf-2-3-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left" width="25%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;"><font color='red'>*</font>&nbsp;申请业务类型：</label>
								</div>
							</td>
							<td id="xf-2-3-1" class="xf-cell xf-cell-right xf-cell-bottom" width="25%">
								<div class="xf-handler">
									<span id="businessType"></span>
								</div>
							</td>
							<td id="xf-2-3-2" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left" width="25%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;"><font color='red'>*</font>&nbsp;业务细分：</label>
								</div>
							</td>
							<td id="xf-2-3-3" class="xf-cell xf-cell-right xf-cell-bottom" width="25%">
								<div class="xf-handler">
									<span id="businessDetail"></span>
								</div>
							</td>
						</tr>
						
						<tr id="xf-2-4">
							<td id="xf-2-4-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="25%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;"><font color='red'>*</font>&nbsp;业务级别：</label>
								</div>
							</td>
							<td id="xf-2-4-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="25%">
								<span id="businessLevel"></span>
							</td>
							<td id="xf-2-4-2" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="25%">
								<label style="display:block;text-align:center;margin-bottom:0px;">发起人：</label>
							</td>
							<td id="xf-2-4-3" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="25%">
								<div class="xf-handler">
									<span id="initiator"></span>
								</div>
							</td>
						</tr>
						
						<tr id="xf-2-5">
							<td id="xf-2-5-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="100%" colspan="4">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">申请内容</label>
								</div>
							</td>
						</tr>
						<tr id="xf-2-6">
							<td id="xf-2-6-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="25%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;"><font color='red'>*</font>&nbsp;编号：</label>
								</div>
							</td>
							<td id="xf-2-6-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="25%">
								<div class="xf-handler">
									<span id="ucode"></span>
								</div>
							</td>
							<td id="xf-2-6-2" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="25%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;"><font color='red'>*</font>&nbsp;姓名：</label>
								</div>
							</td>
							<td id="xf-2-6-3" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="25%">
								<div class="xf-handler">
									<span id="name"></span>
								</div>
							</td>
						</tr>
						<tr id="xf-2-7">
							<td id="xf-2-7-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="25%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;"><font color='red'>*</font>&nbsp;福利级别：</label>
								</div>
							</td>
							<td id="xf-2-7-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="25%">
								<div class="xf-handler">
									<span id="welfareLevel"></span>
								</div>
							</td>
							<td id="xf-2-7-2" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="25%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;"><font color='red'>*</font>&nbsp;资格状态：</label>
								</div>
							</td>
							<td id="xf-2-7-3" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="25%">
								<div class="xf-handler">
									<span id="qualificationsStatus"></span>
								</div>
							</td>
						</tr>
						<tr id="xf-2-8">
							<td id="xf-2-8-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="25%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;"><font color='red'>*</font>&nbsp;所属体系：</label>
								</div>
							</td>
							<td id="xf-2-8-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="25%">
								<div class="xf-handler">
									<span id="system"></span>
								</div>
							</td>
							<td id="xf-2-8-2" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="25%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;"><font color='red'>*</font>&nbsp;联系方式：</label>
								</div>
							</td>
							<td id="xf-2-8-3" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="25%">
								<div class="xf-handler">
									<span id="contact"></span>
								</div>
							</td>
						</tr>
						<tr id="xf-2-9">
							<td id="xf-2-9-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="25%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;"><font color='red'>*</font>&nbsp;所属区域：</label>
								</div>
							</td>
							<td id="xf-2-9-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="25%">
								<div class="xf-handler">
									<span id="area"></span>
								</div>
							</td>
							<td id="xf-2-9-2" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="25%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;"><font color='red'>*</font>&nbsp;所属分公司：</label>
								</div>
							</td>
							<td id="xf-2-9-3" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="25%">
								<div class="xf-handler">
									<span id="branchOffice"></span>
								</div>
							</td>
						</tr>
						<tr id="xf-2-10">
							<td id="xf-2-10-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="25%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;"><font color='red'>*</font>&nbsp;身份证号：</label>
								</div>
							</td>
							<td id="xf-2-10-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="75%" colspan="3">
								<div class="xf-handler">
									<span id="idNumber"></span>
								</div>
							</td>
						</tr>
						<tr id="xf-2-11">
							<td id="xf-2-11-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="25%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;"><font color='red'>*</font>&nbsp;上属董事：</label>
								</div>
							</td>
							<td id="xf-2-11-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="25%">
								<div class="xf-handler">
									<span id="aboveBoard"></span>
								</div>
							</td>
							<td id="xf-2-11-2" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="25%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;"><font color='red'>*</font>&nbsp;联系方式：</label>
								</div>
							</td>
							<td id="xf-2-11-3" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="25%">
								<div class="xf-handler">
									<span id="directorContact"></span>
								</div>
							</td>
						</tr>
						<tr id="xf-2-12">
							<td id="xf-2-12-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="25%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;"><font color='red'>*</font>&nbsp;申请受理事项：</label>
								</div>
							</td>
							<td id="xf-2-12-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="75%" colspan="3">
								<div class="xf-handler">
									<span id="applyMatter"></span>
								</div>
							</td>
						</tr>
						<tr id="xf-2-13">
							<td id="xf-2-13-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="25%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">申请内容：</label>
								</div>
							</td>
							<td id="xf-2-13-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="75%" colspan="3">
								 	<pre>${lllegalFreeze.applyContent}</pre>
							</td>
						</tr>
						
						<tr id="xf-2-14">
							<td id="xf-2-14-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="25%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">附件内容</label>
								</div>
							</td>
							<td id="xf-2-14-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="75%" colspan="3">
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
