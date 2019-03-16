<%@page contentType="text/html;charset=UTF-8"%>
<%@include file="/taglibs.jsp"%>
<%-- <%pageContext.setAttribute("currentHeader", "bpm-workspace");%>
<%pageContext.setAttribute("currentMenu", "bpm-process");%> --%>
<!doctype html>
<html lang="en">
<head>
    <title>麦联</title>
    <script type='text/javascript' src='${cdnPrefix}/jquery/1.11.3/jquery.min.js'></script>
    <link type="text/css" rel="stylesheet" href="${cdnPrefix}/viewer/viewer.min.css">
	<script type="text/javascript" src="${cdnPrefix}/viewer/viewer-jquery.min.js"></script>
	<script type="text/javascript" src="${cdnPrefix}/popwindialog/popwin.js"></script>
    <style type="text/css">
    	body{font-size:14px;}
    	div{margin:0;border:none;}
    	#divPrint{width:900px;margin:0 auto;}
        /* #tb1{border-collapse:collapse;}
        #tb1 td{border:1px solid #CCC;font-size:14px;text-align:center;line-height:28px;padding-left:3px;} */
        .tableprint,.table-border{margin:10px 0 0 0;border-collapse:collapse;width:100%;}
        .tableprint .tdl{white-space:nowrap}
        
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
	     
	      //若该条流程 是   修改花名册  那么显示花名册的详情页
	        function openPersonInfo(){
				popWin.scrolling="auto";
				popWin.showWin("1080"
		    			,"600"
		    			,"花名册"
		    			,"${tenantPrefix}/user/person-info-input-forConfirm.do?applyCode=${customEntity.applyCode}&id=${personInfoId}&partyEntityId=${partyEntityId}");
	        }
	     
	      //若该条流程 是调岗    那么显示调岗的详情页
		    function changePost(){
		    	popWin.showWin("768"
		    			,"400"
		    			,"岗位调整信息"
		    			,"${tenantPrefix}/user/person-info-position-change-forModify.do?applyCode=${customEntity.applyCode}&id=${personInfoId}&isdetail=1");
			}
			
		  //若该条流程 是新建组织机构， 那么跳转到新建组织机构页面
		    function orgCreate(){
		    	popWin.showWin("768"
		    			,"550"
		    			,"组织结构新建信息"
		    			,"${tenantPrefix}/party/org-update-for-audit.do?applyCode=${customEntity.applyCode}&isdetail=1");
			}
		    
		    function orgUpdate(){
		    	popWin.showWin("768"
		    			,"550"
		    			,"组织结构的修改信息"
		    			,"${tenantPrefix}/party/org-update-for-audit.do?applyCode=${customEntity.applyCode}&isdetail=1");
			}
		    
		    function orgRelation(){
		    	popWin.showWin("768"
		    			,"400"
		    			,"岗位关联人员信息"
		    			,"${tenantPrefix}/party/position-user-input-for-audit.do?applyCode=${customEntity.applyCode}&isdetail=1");
		    }
    </script>
</head>
<body>
	<div id="divPrint">
    	<table class="tableprint" cellspacing="0" cellpadding="0" border="0">
    		 <tr>
                <td colspan='4' align='center'>
                    <h2>${customEntity.theme}单</h2>
                </td>
            </tr>
          	<tr>
                <td align='right' class="tdl">
                    	提交次数：
                </td>
                <td style="text-align:left;">
               		${customEntity.submitTimes}
                 </td>
                 <td align='right'>
                   	受理单编号：
                </td>
                <td style="text-align:left;">
               		${customEntity.applyCode}
                 </td>
            </tr>
    		<tr>
                <td>
                 	&emsp;&emsp;姓名：
                </td>
                <td style="text-align:left;">
                   ${customEntity.name}
                </td>
                <td>
                 	&emsp;&emsp;&emsp;部门：
                </td>
                <td style="text-align:left;">
                   ${customEntity.departmentName}
                </td>
            </tr>
            <tr>
                <td>
                 	&emsp;&emsp;类型：
                </td>
                <td style="text-align:left;">
					<c:if test="${customEntity.formType == '1' }">
						<c:if test="${customEntity.type == '1' }">
							病假
						</c:if>
						<c:if test="${customEntity.type == '2' }">
							事假
						</c:if>
						<c:if test="${customEntity.type == '3' }">
							倒休假
						</c:if>
						<c:if test="${customEntity.type == '4' }">
							年假
						</c:if>
						<c:if test="${customEntity.type == '5' }">
							补休假
						</c:if>
						<c:if test="${customEntity.type == '6' }">
							婚假
						</c:if>
						<c:if test="${customEntity.type == '7' }">
							产假
						</c:if>
						<c:if test="${customEntity.type == '8' }">
							丧假
						</c:if>
						<c:if test="${customEntity.type == '9' }">
							其他
						</c:if>
					</c:if> 
					<c:if test="${customEntity.formType == '2' }">
						<c:if test="${customEntity.type == '1' }">
							出差
						</c:if>
						<c:if test="${customEntity.type == '2' }">
							因公外出
						</c:if>
						<c:if test="${customEntity.type == '3' }">
							其他
						</c:if>
					</c:if>  
					<c:if test="${customEntity.formType == '3' }">
						加班
					</c:if> 
					<c:if test="${customEntity.formType == '4' }">
						<c:if test="${customEntity.type == '1' }">
							销假
						</c:if>
						<c:if test="${customEntity.type == '2' }">
							迟到
						</c:if>
						<c:if test="${customEntity.type == '3' }">
							临时外出
						</c:if>
						<c:if test="${customEntity.type == '4' }">
							漏打卡
						</c:if>
						<c:if test="${customEntity.type == '5' }">
							其他
						</c:if>
					</c:if>             
                </td>
                <td>
                 	&emsp;&emsp;&emsp;时间：
                </td>
                <td style="text-align:left;">
                   ${customEntity.date}
                </td>
            </tr>
            <tr>
                <td>
                    &emsp;&emsp;抄送：
                </td>
                <td colspan='3' style="text-align:left;">
                   ${customEntity.ccName}
                </td>
            </tr>
            <tr>
                <td>
                   	业务类型：
                </td>
                <td style="text-align:left;">
                 	自定义
				</td>
                <td>
                   	&emsp;业务细分：
                </td>
                <td style="text-align:left;">
                    ${customEntity.theme}
                </td>
            </tr>
            <tr>
            	<td>
                   	 业务级别：
                </td>
                <td style="text-align:left;">
                     ${customEntity.businessLevel}
                </td>
                <td>
                   	&emsp;&emsp;发起人：
                </td>
                <td style="text-align:left;">
                     ${customEntity.name}
                </td>
            </tr>
            <tr>
                <td colspan='4' align='center'>
                  		事由
                </td>
            </tr>
            <tr>
                <td colspan='4' style='height:80px;text-align:left;text-align:left;vertical-align:top;'>
                	<pre style="white-space: pre-wrap;word-wrap: break-word;background:none;border:none;">${customEntity.applyContent}</pre>
                </td>
            </tr>
            <c:if test="${customEntity.formType == '2'}">
            	<tr>
            		<td>
                    &emsp;同行人：
	                </td>
	                <td colspan='3' style="text-align:left;">
	                   ${customEntity.peerName}
	                </td>
            	</tr>
               <tr>
            		<td>
                    &emsp;目的地：
	                </td>
	                <td colspan='3' style="text-align:left;">
	                   ${customEntity.destination}
	                </td>
            	</tr>
            </c:if>
            <tr>
            	<td>
            		&emsp;&emsp;时间：
            	</td>
            	<td colspan='2' align='center'>
            		自&emsp;${customEntity.startTime}&emsp;至&emsp;${customEntity.endTime}
            	</td>
            	<td id="totalTdId">
            		<c:if test="${customEntity.formType == '4' }">
					<c:if test="${customEntity.type != '4' }">
						<span>共 &emsp;${customEntity.totalTime}&emsp;时</span>
					</c:if>
					</c:if>
            		<c:if test="${customEntity.formType != '4' }">
						<span>共 &emsp;${customEntity.totalTime}&emsp;时</span>
					</c:if>
            	</td>
            </tr>
            <tr>
                 <td colspan='4' style='height:100px;vertical-align:top;padding:3px 3px 3px 3px;text-align:center;'>
                 	<span>审核人</span><br/>
			  		<ul id="ulapprover" style="width:96%;margin:0 auto;list-style:none;">
			  			${approver}
		  			</ul>
                 </td>
            </tr>
           <%--  <tr>
            	<td>历史附件：</td>
            	<td colspan="3" style="text-align:left;padding:10px;"><%@include file="/common/show_file.jsp" %></td>
            </tr> --%>
         </table>
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
							<%-- <c:if test="${item.action != '发起自定义申请' && item.action != '重新发起申请'}"> --%>
								<tr style="border-top:0px hidden;">
									<td>批示内容</td>
									<td colspan="4">${item.comment}</td>
								</tr>
							<%-- </c:if> --%>
						</c:forEach>
					</tbody>
				</table>
			</div>
		</c:if>
 		<c:if test="${isPrint == true}">
			<div>
			    <table style="width:100%;" cellspacing="0" cellpadding="0" border="0" align="center"
			           class="container tableprint"> 
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
</html>