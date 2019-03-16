<%@page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<%@include file="/taglibs.jsp"%>
<%pageContext.setAttribute("currentHeader", "bpm-workspace");%>
<%pageContext.setAttribute("currentMenu", "bpm-process");%>
<!doctype html>
<html lang="en">

  <head>
    <%@include file="/common/meta.jsp"%>
    <title><spring:message code="demo.demo.input.title" text="麦联
    "/></title>
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

        function MaxWords(){
            if($("#applyContent").val().length == "4001"){
                alert("申请内容字数已达上限4000字");
            }
        }

    </script>
	<script type="text/javascript">
		
		//接收申请单的请求数据
		$(function() {
		 	var id=(<%= request.getParameter("processInstanceId")%>); 
				if (id !="") {
			    	$.getJSON('${tenantPrefix}/rs/processFreeze/getFreezeInfo', {
			        	id: id
			        	}, function(data) {
			            	for (var i = 0; i < data.length; i++) {
			            		//alert(JSON.stringify(data)); 
			                   $("#ucode").html(data[i].ucode);
			                   $("#name").html(data[i].name);  
			                   $("#contact").html(data[i].contact);  
			                   $("#salesLevel").html(data[i].salesLevel);  
			                   $("#welfareLevel").html(data[i].welfareLevel);  
			                   $("#activationState").html(data[i].activationState);  
			                   $("#system").html(data[i].system);  
			                   $("#aboveBoard").html(data[i].aboveBoard);  
			                   $("#frozenState").html(data[i].frozenState);  
			                   $("#area").html(data[i].area);  
			                   $("#director").html(data[i].director);  
			                   $("#directorContact").html(data[i].directorContact);  
			                   $("#branchOffice").html(data[i].branchOffice);  
			                   $("#idNumber").html(data[i].idNumber);  
			                   //$("#applyContent").html(data[i].applyContent);  
			                   if(data[i].applyMatter == "冻结"){
			                	   $("#frozen").attr("checked",true);
			                   }else if(data[i].applyMatter ==  "解冻"){
			                	   $("#thaw").attr("checked",true);
			                   }else if(data[i].applyMatter == "开除"){
			                	   $("#expel").attr("checked",true);
			                   }else if(data[i].applyMatter == "限制"){
			                	   $("#limit").attr("checked",true);
			                   }else if(data[i].applyMatter == "取消限制"){
			                	   $("#cancelLimit").attr("checked",true);
			                   }else if(data[i].applyMatter == "其他"){
			                	   $("#other").attr("checked",true);
			                   }
			            	}
		            	})
		            	
		            	 //获取抄送人  
	                    $.getJSON('${tenantPrefix}/workOperationCustom/getFormCopyName.do', {
	                    	id: id
	                    }, function(data) {
	                    	if(data != ''){
	                    		$("#copyNames").html(data);
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
		
		<div id="xf-form-table">
			<div id="xf-1" class="xf-section">
				<h1 style="text-align:center;">冻结/解冻详情单</h1>
			</div>
			
			<div id="xf-2" class="xf-section">
				<table class="xf-table" width="100%" cellspacing="0" cellpadding="0" border="0" align="center">
					<tbody>
						<tr id="xf-2-0">
							<td id="xf-2-0-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="20%">
								<div class="xf-handler" align="center">
									<label class="tdl">编号：</label>
								</div>
							</td>
							<td id="xf-2-0-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
								<div class="xf-handler">
									<span id="ucode"></span>
								</div>
							</td>
							<td id="xf-2-0-2" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
								<div class="xf-handler" align="center">
									<label>姓名：</label>
								</div>
							</td>
							<td id="xf-2-0-3" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
								<div class="xf-handler">
									<span id="name"></span>
								</div>
							</td>
							<td id="xf-2-0-4" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
								<div class="xf-handler" align="center">
									<label>联系方式：</label>
								</div>
							</td>
							<td id="xf-2-0-5" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
								<div class="xf-handler">
									<span id="contact"></span>
								</div>
							</td>
						</tr>
						<tr id="xf-2-1">
							<td id="xf-2-1-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">销售级别：</label>
								</div>
							</td>
							<td id="xf-2-1-1" class="xf-cell xf-cell-right xf-cell-bottom">
								<div class="xf-handler">
									<span id="salesLevel"></span>
								</div>
							</td>
							<td id="xf-2-1-2" class="xf-cell xf-cell-right xf-cell-bottom">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">福利级别：</label>
								</div>
							</td>
							<td id="xf-2-1-3" class="xf-cell xf-cell-right xf-cell-bottom">
								<div class="xf-handler" >
									<span id="welfareLevel"></span>
								</div>
							</td>
							<td id="xf-2-1-4" class="xf-cell xf-cell-right xf-cell-bottom">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">激活状态：</label>
								</div>
							</td>
							<td id="xf-2-1-5" class="xf-cell xf-cell-right xf-cell-bottom">
								<div class="xf-handler" >
									<span id="activationState"></span>
								</div>
							</td>
						</tr>
						<tr id="xf-2-2">
							<td id="xf-2-2-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left">
								<div class="xf-handler" align="center">
									<label>所属体系：</label>
								</div>
							</td>
							<td id="xf-2-2-1" class="xf-cell xf-cell-right xf-cell-bottom">
								<div class="xf-handler">
									<span id="system"></span>
								</div>
							</td>
							<td id="xf-2-2-2" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left" align="center">
								<label>上属董事：</label>
							</td>
							<td id="xf-2-2-3" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left">
								<div class="xf-handler">
									<span id="aboveBoard"></span>
								</div>
							</td>
							<td id="xf-2-2-4" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">冻结状态：</label>
								</div>
							</td>
							<td id="xf-2-2-5" class="xf-cell xf-cell-right xf-cell-bottom">
								<div class="xf-handler">
									<span id="frozenState"></span>
								</div>
							</td>
						</tr>
						<tr id="xf-2-3">
							<td id="xf-2-3-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">所属区域：</label>
								</div>
							</td>
							<td id="xf-2-3-1" class="xf-cell xf-cell-right xf-cell-bottom">
								<div class="xf-handler">
									<span id="area"></span>
								</div>
							</td>
							<td id="xf-2-3-2" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">董事姓名：</label>
								</div>
							</td>
							<td id="xf-2-3-3" class="xf-cell xf-cell-right xf-cell-bottom">
								<div class="xf-handler">
									<span id="director"></span>
								</div>
							</td>
							<td id="xf-2-3-4" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">联系方式：</label>
								</div>
							</td>
							<td id="xf-2-3-5" class="xf-cell xf-cell-right xf-cell-bottom">
								<div class="xf-handler">
									<span id="directorContact"></span>
								</div>
							</td>
						</tr>
						
							
						
						<tr id="xf-2-4">
							<td id="xf-2-4-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">所属分公司：</label>
								</div>
							</td>
							<td id="xf-2-4-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
								<div class="xf-handler">
									<span id="branchOffice"></span>
								</div>
							</td>
							<td id="xf-2-4-2" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
								<label style="display:block;text-align:center;margin-bottom:0px;">身份证号：</label>
							</td>
							<td id="xf-2-4-3" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" colspan="3">
								<span id="idNumber"></span>
							</td>
						</tr>
						<tr id="xf-9-9">
							<td id="xf-9-9-9" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="8%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">抄送：</label>
								</div>
							</td>
							<td id="xf-8-8-8" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" colspan="5">
								<span id="copyNames"></span>
							</td>
						</tr>
						<tr id="xf-2-5">
							<td id="xf-2-5-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="8%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">申请受理事项：</label>
								</div>
							</td>
							<td id="xf-2-5-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" colspan="5">
								<div class="xf-handler" id="applyMatter">
									<input type="radio" id="frozen" name="applyMatter" value="冻结" disabled>冻结&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
									<input type="radio" id="expel" name="applyMatter" value="开除" disabled>开除&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
									<input type="radio" id="thaw" name="applyMatter" value="解冻" disabled>解冻&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
									<input type="radio" id="limit" name="applyMatter" value="限制" disabled>限制&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
									<input type="radio" id="cancelLimit" name="applyMatter" value="取消限制" disabled>取消限制&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
									<input type="radio" id="other" name="applyMatter" value="其他" disabled>其他
								</div>
							</td>
						</tr>
						<tr id="xf-2-6">
							<td id="xf-2-6-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">申请内容：</label>
								</div>
							</td>
							<td id="xf-2-6-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" colspan="5">
								<pre>${freeze.applyContent}</pre>
							</td>
						</tr>
						<tr id="xf-2-7">
							<td id="xf-2-7-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">附件内容</label>
								</div>
							</td>
							<td id="xf-2-7-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" colspan="5">
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
