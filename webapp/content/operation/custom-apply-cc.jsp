<%@page contentType="text/html;charset=UTF-8"%>
<%@include file="/taglibs.jsp"%>
<%pageContext.setAttribute("currentHeader", "bpm-workspace");%>
<%pageContext.setAttribute("currentMenu", "bpm-process");%>
<!doctype html>
<html lang="en">
<head>
<%@include file="/common/meta.jsp"%>

    <title>麦联</title>
    <%@include file="/common/s3.jsp"%>
	 <%@include file="/common/s3.jsp"%>
</head>
<style type="text/css">
         #tb1 td{border:1px solid #BBB }
        .f_td{ width:120px; font-size:12px;white-space:nowrap }
        .f_r_td{ width:130px; text-align:left;}
        #tb1 tr td input{border: navajowhite;width: 100%;} 
        #tb1 tr td textarea{border: navajowhite;}     
        #tb1 tr td{text-align:center;line-height:28px;} 
        #tb1 tr td.f_td.f_right{text-align:right;}    
        #tb1 tr td input.input_width{width:auto;}
        #tb1 tr td input,#tb1 tr td textarea{padding:3px 3px 0 3px;} 
    </style>
    
   
    
<body>
 <%@include file="/header/bpm-workspace3.jsp"%>
    <form id="xform" method="post"   action="${tenantPrefix}/operationApply/process-operationApply-startProcessInstance.do" class="xf-form" enctype="multipart/form-data">
			申请单
    <br />

    <div class="container">
    		
     <section id="m-main" class="col-md-12" style="padding-top:65px;">

		<input id="processInstanceId"  name="processInstanceId" type="hidden" value="<%= request.getParameter("processInstanceId")%>">
		<input id="isConfirmed"  name="isConfirmed"  type="hidden">
		
    	<table id="tb1" style="width:100%;">
    		 <tr>
                <td colspan='4' align='center' class='f_td'>
                    <h2>自定义申请单</h2>
                </td>
            </tr>
          <tr>
                <td class='f_td' align='right' style='padding-right:20px;'>
                    	提交次数：
                    	
                </td>
                <td>
               		${customEntity.submitTimes}<input id="submitTimes" style="display:none; name="submitTimes" value="${customEntity.submitTimes}" readonly>
                 </td>
                 <td class='f_td' align='right' style='padding-right:20px;'>
                    	受理单编号：
                </td>
                <td>
               		${customEntity.applyCode}<input id="applyCode" style="display:none;" name="applyCode" value="${customEntity.applyCode}" readonly>
                 </td>
            </tr>
    	
    	<tr>
                <td>
                 <span id='tag_Theme'>&nbsp;<span style="color:Red">*</span>主题</span>：
                </td>
                <td colspan='3' style="text-align:left;">
                    ${customEntity.theme} <input name="theme" style="display:none" id="theme" value="${customEntity.theme}" readOnly>
                </td>
            </tr>
           <tr>
                <td >
                    <span id='tag_depart'>&nbsp;抄送</span>：
                </td>
                <td colspan='3' style="text-align:left;">
                    ${customEntity.ccName}<input name="ccName" style="display:none"  id="ccName" value="${customEntity.ccName}" readOnly>
                </td>
            </tr>
            <tr>
                <td>
                    <span id='tag_bustype'>&nbsp;<span style="color:Red">*</span>申请业务类型</span>：
                </td>
                <td style="text-align:left;">
	                    <select style="display:none" name="busType" id="busType">
							<option selected="selected" >自定义</option>
				 		</select>
				 		自定义
						<input name="businessType" type="hidden" value="自定义"  id="businessType"  />
                </td>
                <td>
                    <span id='tag_busDetails'>&nbsp;<span style="color:Red">*</span>业务细分</span>：
                </td>
                <td style="text-align:left;">
                    <select style="display:none" name="busDetails" id="busDetails">
						<option >自定义申请</option>
					</select>
					自定义申请
					<input name="businessDetail" type="hidden" value="自定义"  id="businessDetail"  />
                </td>
            </tr>
            
            <tr>
                <td>
                    <span id='tag_toStart'>&nbsp;发起人</span>：
                </td>
                <td  colspan='3'>
                     ${customEntity.name}<input id="name" style="display:none" name="name" value="${customEntity.name}" readonly>
                </td>
            </tr>
            
            <tr>
                <td colspan='4' align='center' class='f_td'>
                    <span style="color:Red">*</span>申请内容
                </td>
            </tr>
            <tr>
                <td colspan='4' style='height:100px;text-align:left;vertical-align:top;' >
                   ${customEntity.applyContent}<textarea style="display:none;" name="applyContent" maxlength="4000"  id="applyContent" rows="2" cols="20" id="appContext" class="text0" style="height:99px;width:900px;padding-left:10px;padding-top:10px" readonly>${customEntity.applyContent}</textarea>
                </td>
            </tr>
            <tr>
                <td colspan='4' style='height:100px;vertical-align:top;padding:3px 3px 3px 3px;'>
                	<span>审核人</span><br/>
		  			<ul id="ulapprover" style="width:96%;margin:0 auto;list-style:none;">
		  			${approver}
		 			</ul>
                </td>
            </tr>
            <tr>
            	<td>历史附件：</td>
            	<td colspan="3" style="text-align:left;padding:10px;"><%@include file="/common/show_file.jsp" %></td>
            </tr>
	 </table>  
	 
	 <table width="90%" cellspacing="0" cellpadding="0" border="0" align="center" class="table table-border">
		  <thead>
		    <tr>
			  <th>环节</th>
			  <th>操作人</th>
			  <th>时间</th>
			  <th>结果</th>
			  <th>意见</th>
			</tr>
		  </thead>
		  <tbody>
			  <c:forEach var="item" items="${logHumanTaskDtos}">
			  <c:if test="${not empty item.completeTime}">
		    <tr>
			  <td>${item.name}</td>
			  <td><tags:isDelUser userId="${item.assignee}"/></td>
			  <td><fmt:formatDate value="${item.completeTime}" type="both"/></td>
			  <td>${item.action}</td>
			   <td>${item.comment}</td>
			  <script>
			  
				</script>
				
			</tr>
			  </c:if>
			  </c:forEach>
		  </tbody>
	</table>
        </section>
	<!-- end of main -->
     <!-- <table>
       	<tr>
       	   <td>
	         <div class="form-group">
                 <label class="control-label col-md-2" name="fileName"></label>
                 <div class="col-md-8">
                     
                 </div>
             </div>
	       </td>
		</tr>
    </table> -->
  </div>
 
        <div style="width:500px;margin:20px auto;text-align:center">
            <button type="button" class="btn btn-default" onclick="javascript:history.back();">返回</button>
        </div>
</form>
</body>


</html>