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
	 <script type="text/javascript" src="${cdnPrefix}/bootbox/bootbox.min1.js"></script>
   	<link type="text/css" rel="stylesheet" href="${cdnPrefix}/userpicker3-v2/userpicker.css">
    <script type="text/javascript" src="${cdnPrefix}/userpicker3-v2/userpickercustomwithdraw.js"></script>
	<script type="text/javascript" src="${cdnPrefix}/popwindialog/popwin.js"></script>
	<script type="text/javascript" src="${cdnPrefix}/userpicker3-v2/userpickerbycopy.js?v=1.0"></script>
    <script type="text/javascript">

	$(function() {
		
		
		 //下一步审批人弹出
        createUserPicker({
            modalId: 'leaderPicker',//这个 其实是弹出的窗口的div id
            targetId: 'leaderDiv', //这个是点击哪个 会触发弹出窗口
            inputStoreIds: {iptid: "allLeaderId", iptname: "allLeaderName"},//存储已选择的ID和name的input的id
            auditId: 'ulapprover',//显示审批步骤
            showExpression: true,
            multiple: true,
            searchUrl: '${tenantPrefix}/rs/user/searchVNoMe',
            treeUrl: '${tenantPrefix}/party/asyncTree.do?partyStructTypeId=1&notViewPost=true&notAuth=false',
            childUrl: '${tenantPrefix}/rs/party/searchUserNoMe'
        });
		 
		 //抄送
        /* createUserPicker({
            modalId: 'ccUserPicker',
            targetId: 'ccDiv',
            multiple: true,
            showExpression: true,
            searchUrl: '${tenantPrefix}/rs/user/searchVNoMe',
            treeUrl: '${tenantPrefix}/rs/party/treeNoAuth?partyStructTypeId=1',
            childUrl: '${tenantPrefix}/rs/party/searchUserNoMe'
        }); */
        createUserPickerCopy({
    		modalId: 'userPicker',
    		showExpression: true,
    		multiple: true,
    		//inputStoreIds: {iptid: "btnPickerMany", iptname: "userName"},//存储已选择的ID和name的input的id
    		searchUrl: '${tenantPrefix}/rs/user/search',
    		treeNoPostUrl: '${tenantPrefix}/party/asyncTree.do?partyStructTypeId=1&notViewPost=true',
    		treeUrl: '${tenantPrefix}/party/asyncTree.do?partyStructTypeId=1&notViewPost=true',
    		childUrl: '${tenantPrefix}/rs/party/searchUser',
    		childPostUrl: '${tenantPrefix}/rs/party/searchPost'
    	});
      //同行人员
		createUserPicker({
            modalId: 'txUserPicker',
            targetId: 'txDiv',
            inputStoreIds: {iptid: "txId", iptname: "txName"},//存储已选择的ID和name的input的id
            multiple: true,
            showExpression: true,
            searchUrl: '${tenantPrefix}/rs/user/searchVNoMe',
            treeUrl: '${tenantPrefix}/party/asyncTree.do?partyStructTypeId=1&notViewPost=true&notAuth=false',
            childUrl: '${tenantPrefix}/rs/party/searchUserNoMe'
        })
		 
	})
	
	    $(function(){
			  $("#formTypeForId :radio").click(function(){
			   	var type = $(this).val();
			   	if(type == '4'){
			   		$("#totalTdId").html("");
			   	}else if($("#totalTdId").text() == ''){
			   		$("#totalTdId").html('<span >共</span>'
	                    	+'<input style="padding-left:10px;width: 30%;background:#eee;" type="text" id="totalTime" name="totalTime" >'
	                    	+'<span >时</span>'
			   				);
			   	}
			  });
		 });
	//value="${customEntity.totalTime }"
	function confirmOperation(flag) {
		
		//没填写主题不能发起流程
		/* if($("#theme").val().length ==0){
   		 	alert("请填写主题");
   		 	return false;
	   	 } */
		
		/* if ($("#theme").val().length>100) {
            alert("主题字数最多为100字");
            return false;
        } */
		
		//没填写内容不能发起流程
		
		if("${personTypeID}" == ''){
			if($("#applyContent").val().length ==0){
	   		 	alert("请填写内容");
	   		 	return false;
		   	 }
		}
        var checkedType = $('input:radio:checked').val();
        var hiddenFormType = $("#formType").val();
        
        if(hiddenFormType == '4' && checkedType == '4'){
        	
        	if(document.getElementById('startDate').value == ""){
        		alert("请选择开始时间！");
                return false;
        	}
        	if(document.getElementById('endDate').value == ""){
        		alert("请选择结束时间！");
                return false;
        	}
        	
        	
        }else{
        	if(document.getElementById('startDate').value == ""){
        		alert("请选择开始时间！");
                return false;
        	}
        	if(document.getElementById('endDate').value == ""){
        		alert("请选择结束时间！");
                return false;
        	}
        	if(document.getElementById('totalTime').value == ""){
        		alert("请填写总计时间！");
                return false;
        	}
        }
		
		if(flag == 4){
			var msg = "确定要撤销申请吗,请确认？";  
            if (!confirm(msg)){ 
              return false;  
            }  
		}
		
		if(flag == 3){
			if($("#allLeaderId").val() == ''){
	   		 	alert("请选择审核人");
	   		 	return false;
		   	 }
		}
		
		
		if("${personTypeID}" == ''){
	        if ($("#applyContent").val().length >"4000") {
	            alert("申请内容字数最多为4000字");
	            return false;
	        }
		}
		
		//调整人重新申请和撤销申请先检验该流程的状态是否是已撤回或驳回发起人
        $.ajax({      
            url: '${tenantPrefix}/rs/bpm/getStatus',      
            datatype: "json",
            data:{"processInstanceId": $("#processInstanceId").val(),"humanTaskId":$("#humanTaskId").val(),"userId":$("#userId").val(),"resource":'adjustment'},
            type: 'get',      
            success: function (e) {
            	if(e == 'error'){
            		alert("该申请状态已变更，您已无权操作。");
            		return false;
            	}
            	if(e == 'noAuth'){
            		alert("您无权操作。");
            		return false;
            	}
            	var loading = bootbox.dialog({
                    message: '<p style="width:90%;margin:0 auto;text-align:center;">提交中...</p>',
                    size: 'small',
                    closeButton: false
             	});
        		
        		$('#xform').attr('action', '${tenantPrefix}/workOperationCustom/custom-work-completeTask.do?flag=' + flag);
        		$('#xform').submit();
            },      
            error: function(e){      
            	loading.modal('hide');
                alert("服务器请求失败,请重试"); 
            }
       });
	}
	
	function MaxWordsTheme(){
		
		 if($("#theme").val().length =="100"){
   		 alert("主题字数已达上限100字");
   	 }
	}
	
	function MaxWords(){

		 if($("#applyContent").val().length == "4000"){
	   		 alert("申请内容字数已达上限4000字");
 	 }
	}
	//共计时长的校验
	function checkedTime(obj){
		var a = obj;
		a.value=a.value.toString().match(/^\d+(?:\.\d{0,1})?/)
	}
	</script>  
</head>
<style type="text/css">
          #tb1 td{border:1px solid #BBB }
        .f_td{ width:120px; font-size:12px;white-space:nowrap }
        .f_r_td{ width:130px; text-align:left;}
        #tb1 tr td input{border: navajowhite;/* width: 100%; */}
        #tb1 tr td textarea{border: navajowhite;}     
        #tb1 tr td{text-align:center;} 
        #tb1 tr td.f_td.f_right{text-align:right;}    
        #tb1 tr td input.input_width{width:auto;}   
        #tb1 tr td input{border: navajowhite;/* width: 100%; */}
        #tb1 tr td textarea{border: navajowhite;}     
        #tb1 tr td{text-align:center;line-height:28px;} 
        #tb1 tr td.f_td.f_right{text-align:right;}    
        #tb1 tr td input.input_width{width:auto;}
        
        /* input{height:28px;} */
        #fileTable td{border:0px solid #BBB;}      
    </style>

<body style="margin-bottom:50px;">
 <%@include file="/header/bpm-workspace3.jsp"%>
    <form id="xform" method="post"    action="${tenantPrefix}/operationCustom/custom-completeTask.do"  class="xf-form" enctype="multipart/form-data">
			调整申请单
    <br />

    <div class="container">
    		
     <section id="m-main" class="col-md-12" style="padding-top:65px;">

		<input id="processInstanceId"  name="processInstanceId" type="hidden" value="${processInstanceId}">
		<input id="isConfirmed"  name="isConfirmed"  type="hidden">
		<input id="humanTaskId" type="hidden" name="humanTaskId" value="${humanTaskId}">
		<input id="userId" type="hidden" name="userId" value="<%=userId %>">
		
    	<table id="tb1" style="width:100%;">
    		 <tr>
                <td colspan='6' align='center' class='f_td'>
                    <h2>调整${customEntity.theme }单</h2>
                    <input type="hidden" name="formType" id="formType" value="${customEntity.formType }">
                    <c:if test="${customEntity.formType == 1 }">
                    		<input name="theme" type="hidden"  id="theme" value="请假申请"/>
                    		<input name="businessDetail" type="hidden" value="请假申请" id="businessDetail"/>
                    		<input name="busDetails" type="hidden" value="8001" id="busDetails"/>
                    	</c:if>
                    	<c:if test="${customEntity.formType == 2 }">
                    		<input name="theme" type="hidden"  id="theme" value="出差外出申请"/>
                    		<input name="businessDetail" type="hidden" value="出差外出申请" id="businessDetail"/>
                    		<input name="busDetails" type="hidden" value="8002" id="busDetails"/>
                    	</c:if>
                    	<c:if test="${customEntity.formType == 3 }">
                    		<input name="type" type="hidden" id="type" value="1">
                    		<input name="theme" type="hidden"  id="theme" value="加班申请"/>
                    		<input name="businessDetail" type="hidden" value="加班申请" id="businessDetail"/>
                    		<input name="busDetails" type="hidden" value="8003" id="busDetails"/>
                    	</c:if>
                    	<c:if test="${customEntity.formType == 4 }">
                    		<input name="theme" type="hidden"  id="theme" value="特殊考勤说明申请"/>
                    		<input name="businessDetail" type="hidden" value="特殊考勤说明申请" id="businessDetail"/>
                    		<input name="busDetails" type="hidden" value="8004" id="busDetails"/>
                    	</c:if>
                </td>
            </tr>
	         <tr>
	          		<td colspan='6'  class='f_td f_right' align='right' style='padding-right:20px;font-size:14px;'> 
	                    	提交次数：${customEntity.submitTimes}&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<input id="submitTimes" style="display:none;" class="input_width" name="submitTimes" value="${customEntity.submitTimes}" readonly> &nbsp; 受理单编号：${customEntity.applyCode}<input id="applyCode" class="input_width" style="display:none" name="applyCode" value="${customEntity.applyCode}" readonly>
	                </td>
	         </tr>
    	
    		<tr>
                <td>
                    <span id='tag_toStart'>&nbsp;姓名</span>：
                </td>
                <td>
                      <input id="name" name="name" value="${customEntity.name}" readonly>
                      
                </td>
                <td style="width: 145px;">
                    <span id='tag_toStart'>&nbsp;部门</span>：
                </td>
                <td>
                      <input id="departmentName" name="departmentName" value="${customEntity.departmentName}" readonly>
                </td>
                <td colspan="2" style="width: 30%;align:center">
                	${customEntity.date }
                	<input type="hidden" name="date" value="${customEntity.date }">
                </td>
            </tr>
            
            <c:if test="${customEntity.formType == 1 }">
                	 <tr>
	                	<td><span>类别</span>：</td>
	                	<td colspan='5'>
	                		<div>
	                		<input <c:if test="${customEntity.type == '1' }">checked="checked"</c:if>  type="radio" name="type" value="1"><label>&nbsp;病假</label>
	                		<input <c:if test="${customEntity.type == '2' }">checked="checked"</c:if> type="radio" name="type" value="2"><label>&nbsp;事假</label>
	                		<input <c:if test="${customEntity.type == '3' }">checked="checked"</c:if> type="radio" name="type" value="3"><label>&nbsp;倒休假</label>
	                		<input <c:if test="${customEntity.type == '4' }">checked="checked"</c:if> type="radio" name="type" value="4"><label>&nbsp;年假</label>
	                		<input <c:if test="${customEntity.type == '5' }">checked="checked"</c:if> type="radio" name="type" value="5"><label>&nbsp;补休假</label>
	                		<input <c:if test="${customEntity.type == '6' }">checked="checked"</c:if> type="radio" name="type" value="6"><label>&nbsp;婚假</label>
	                		<input <c:if test="${customEntity.type == '7' }">checked="checked"</c:if> type="radio" name="type" value="7"><label>&nbsp;产假</label>
	                		<input <c:if test="${customEntity.type == '8' }">checked="checked"</c:if> type="radio" name="type" value="8"><label>&nbsp;丧假</label>
	                		<input <c:if test="${customEntity.type == '9' }">checked="checked"</c:if> type="radio" name="type" value="9"><label>&nbsp;其他</label>
	                		</div>
	                	</td>
	                </tr>
              </c:if>
              <c:if test="${customEntity.formType == 2 }">
                	 <tr>
	                	<td><span>类别</span>：</td>
	                	<td colspan='3'>
	                		<div>
	                		<input <c:if test="${customEntity.type == '1' }">checked="checked"</c:if>  type="radio" name="type" value="1"><label>&nbsp;出差</label>
	                		<input <c:if test="${customEntity.type == '2' }">checked="checked"</c:if> type="radio" name="type" value="2"><label>&nbsp;因公外出</label>
	                		<input <c:if test="${customEntity.type == '3' }">checked="checked"</c:if> type="radio" name="type" value="3"><label>&nbsp;其他</label>
	                		</div>
	                	</td>
	                	<td><span>目的地</span></td>
	                	<td>
	                		<input style="background-color:#eee;padding-left:10px;" type="text" name="destination" id="destination" value="${customEntity.destination }"> 
	                	</td>
	                </tr>
              </c:if>
               <c:if test="${customEntity.formType == 4 }">
                	 <tr>
	                	<td><span>类别</span>：</td>
	                	<td colspan='5' id="formTypeForId">
	                		<div>
	                		<input <c:if test="${customEntity.type == '1' }">checked="checked"</c:if> type="radio" name="type" value="1"><label>&nbsp;销假</label>
	                		<input <c:if test="${customEntity.type == '2' }">checked="checked"</c:if> type="radio" name="type" value="2"><label>&nbsp;迟到</label>
	                		<input <c:if test="${customEntity.type == '3' }">checked="checked"</c:if> type="radio" name="type" value="3"><label>&nbsp;临时外出</label>
        			        <input <c:if test="${customEntity.type == '4' }">checked="checked"</c:if> type="radio" name="type" value="4"><label>&nbsp;漏打卡</label>
        			        <input <c:if test="${customEntity.type == '5' }">checked="checked"</c:if> type="radio" name="type" value="5"><label>&nbsp;其他</label>
	                		</div>
	                	</td>
	                </tr>
              </c:if>
            
            
	      	<tr>
	       		<td>
	       		<span>抄送</span>：
	       		</td>
	       		<td  colspan='5'>
                    <%-- <div class="input-group "  style="width:100%;">
                        <input id="btnPickerMany" type="hidden" name="ccnos" class="input-medium"
                               value="${customEntity.ccnos}">
                        <input type="text" id="userName" name="ccName"
                               value="${customEntity.ccName}" class="form-control" readOnly placeholder="点击后方图标即可选人">
                        <div id="ccDiv" class="input-group-addon"><i class="glyphicon glyphicon-user"></i></div>
                    </div> --%>
                    <div class="input-group userPicker" style="width:100%;">
					  <input id="btnPickerMany" type="hidden" name="ccnos" class="input-medium" value="${customEntity.ccnos}">
					  <input id="userName" type="text" name="ccName" placeholder="点击后方图标即可选人" class="form-control" value="${customEntity.ccName}" readonly>
					  <div id="ccDiv" class="input-group-addon"><i class="glyphicon glyphicon-user"></i></div>
				    </div>
	            </td>
			</tr> 
           
            <tr>
            	<td>
                    <span id='tag_toStart'>&nbsp;业务级别</span>：
                </td>
                <td colspan="2">
                      <input id="businessLevel" name="businessLevel" value="${customEntity.businessLevel}" readonly>
                      
                </td>
                <td>
                    <span id='tag_toStart'>&nbsp;发起人</span>：
                </td>
                <td colspan="2">
                      <input id="name" name="name" value="${customEntity.name}" readonly>
                </td>
            </tr>
            
            <c:if test="${personTypeID != 'personadd' 
            			&& personTypeID != 'personUpdate'
            			&& personTypeID!='changePost'
            			&& personTypeID!='orgadd'
            			&& personTypeID!='orgupdate'
            			&& personTypeID!='postwithperson'}">
             
            	<tr>
	                <td colspan='6' align='center' class='f_td'>
	                    <span style="color:Red">*</span>事由
	                </td>
	            </tr>
	            <tr>
	                <td colspan='6' style='height:100px' >
	                    <textarea  maxlength="4000"  name="applyContent" id="applyContent" rows="2" cols="20" class="text0" style="height:99px;width:100%;padding-left:10px;padding-top:10px;background:#eee;">${customEntity.applyContent}</textarea>
	                </td>
	            </tr>
	            <!-- <tr>
	            	<td>
	            		<span>时间</span>：
	            	</td>
	            	<td></td>
	            </tr> -->
            <%-- <c:if test="${personTypeID== 'personadd' 
            			|| personTypeID == 'personUpdate'
            			|| personTypeID=='changePost'
            			|| personTypeID=='orgadd'
            			|| personTypeID=='orgupdate'
            			|| personTypeID=='postwithperson'}">
            			<textarea  maxlength="4000" name="applyContent" id="applyContent" rows="2" cols="20" class="text0" style="height:99px;width:100%;padding-left:10px;padding-top:10px;background:#eee;display:none;">${customEntity.applyContent}</textarea>
            </c:if> --%>
            
            </c:if>

            <tr>
	           	<td>
	                  <span style="color:Red">*</span><span>时间</span>：
	              </td>
	              <td colspan="3">
	              	<!-- <span style="float: left;">自</span> -->
	              	<input autocomplete="off" type="text" id="startDate" name="startDate" value="${customEntity.startTime }" onclick="WdatePicker({maxDate:'#F{$dp.$D(\'endDate\')}',dateFmt:'yyyy-MM-dd HH:mm'})" class="Wdate" style="width:200px;background-color:#eee;"/>
	              	
	                     &emsp;<span>至</span>&emsp; 
	                     <input autocomplete="off" type="text" id="endDate" name="endDate" value="${customEntity.endTime }" onclick="WdatePicker({minDate:'#F{$dp.$D(\'startDate\')}',dateFmt:'yyyy-MM-dd HH:mm'})" class="Wdate" style="width:200px;background-color:#eee;"/>
	                    
	              </td>
	              <td colspan="2" id="totalTdId">
	              	<c:if test="${customEntity.formType == '4' }">
					<c:if test="${customEntity.type != '4' }">
						<span >共</span>
	              		<input autocomplete="off" style="width: 30%;;background:#eee;" type="text" id="totalTime" name="totalTime" value="${customEntity.totalTime }" onkeyup="checkedTime(this)" onafterpaste="checkedTime(this)">
	              	<span >时</span>
					</c:if>
					</c:if>
            		<c:if test="${customEntity.formType != '4' }">
						<span >共</span>
	              		<input autocomplete="off" style="width: 30%;;background:#eee;" type="text" id="totalTime" name="totalTime" value="${customEntity.totalTime }" onkeyup="checkedTime(this)" onafterpaste="checkedTime(this)">
	              	<span >时</span>
					</c:if>
	              
	              
	              	
	             </td>
            </tr>
          	<c:if test="${customEntity.formType == '2' }">
          		<tr>
		       		<td>
		       		<span>同行人</span>：
		       		</td>
		       		<td  colspan='5'>
	                    <div class="input-group "  style="width:100%;">
	                        <input id="txId" type="hidden" name="txnos" class="input-medium"
	                               value="${customEntity.peerId}">
	                        <input type="text" id="txName" name="txName"
	                               value="${customEntity.peerName}" class="form-control" readOnly placeholder="点击后方图标即可选人">
	                        <div id="txDiv" class="input-group-addon"><i class="glyphicon glyphicon-user"></i></div>
	                    </div>
		            </td>
				</tr> 
          	</c:if>
          	<tr>
               <td colspan='6' style='height:100px;vertical-align:top;padding:3px 3px 3px 3px;'>
               	<span>审核人</span><br/>
		  		<ul id="ulapprover" style="width:96%;margin:0 auto;list-style:none;">
		  			${approver}
	  			</ul>
	  			<button id="leaderDiv" type="button" class="btn btn-default" style="background-color:#1D82D0;color: #FFFFFF;float:right" >重新选择审核人</button>
               </td>
           </tr>
		   <tr>
		   	   <td><font color="red ">*</font>下一步审批人</td>
		       <td  colspan='5'>
			       	<!-- <div class="form-group">
		                        <div class="col-sm-8"> -->
		                            <div class="input-group leaderUserPicker" style="width:100%;">
		                                <input id="nextLeaderId" type="hidden" name="leader"
		                                       value="${auditorId}">
		                                <input type="text" id="nextLeaderName" name="leaderName" class="form-control required"
		                                       value="<tags:user userId="${auditorName}"></tags:user>" minlength="2"
		                                       maxlength="50" class="form-control" readOnly placeholder="点击后方图标即可选人">
		                                <%-- <c:if test="${isCanSelect == true}">
	                                    	<div id='leaderDiv' class="input-group-addon">
	                                    		<i class="glyphicon glyphicon-user"></i>
	                                    	</div>
	                                    </c:if> --%>
	                                    <input id="allLeaderId" type="hidden" name="allLeaderId"
                                       		   value="${allAuditorId}">
		                                <input id="allLeaderName" type="hidden" name="allLeaderName" class="form-control required"
		                                       value="${allAuditorName}">
		                                <input id="oldLeaderId" type="hidden" name="oldLeaderId"
		                                       value="${allAuditorId}">
		                            </div>
		                      <!--   </div>
		                    </div> -->
			    </td>
			</tr>
			<%-- <tr>
				<td>原有附件</td>
				<td colspan="3"><%@include file="/common/show_edit_file.jsp" %></td>
			</tr>
			<tr>
				<td>添加附件：</td>
				<td colspan="3">
					<%@include file="/common/_uploadFile.jsp" %>
                    <span style="color:gray;"> 请添加共小于200M的附件 </span>
                </td>
			</tr> --%>
		 </table>
		 
		 <table width="70%" cellspacing="0" cellpadding="0" border="0" align="center" class="table table-border">
		  <thead>
		    <tr>
			  <th>环节</th>
			  <th>操作人</th>
			  <th>时间</th>
			  <th>结果</th>
			  <th>审核时长</th>
			  <th>意见</th>
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
			  <td>${item.comment}</td>
			</tr>
			  </c:if>
			  </c:forEach>
		  </tbody>
	</table>
        </section>
	<!-- end of main -->
	
			<%-- <table style ="width:80%">
	  	 			<div class="form-group">
                        <label class="control-label col-md-2" name="fileName">原有附件</label>
                        <div class="col-md-8">
                            <%@include file="/common/show_edit_file.jsp" %>
                        </div>
                    </div>
			</table>
			<table style ="width:85%">
				<tr>
					<td>
                        <label name="fileName">添加附件：</label>
	                        <div >
	                            <%@include file="/common/_uploadFile.jsp" %>
	                            <span style="color:gray;"> 请添加共小于200M的附件 </span>
	                        </div>
                    </td>
			   </tr>
			</table> --%>
	</div> 
</div> 
	 
	 
	 <br/>  
	<br/>  
	  
		<div class="navbar navbar-default navbar-fixed-bottom">
	    	<div class="text-center" style="padding-top:8px;">
			    <div class="text-center" style="padding-top:8px;">
					<button id="completeTask1" type="button" class="btn btn-default" onclick="confirmOperation(3)">重新申请</button>
					<button id="completeTask2" type="button" class="btn btn-default" onclick="confirmOperation(4)">撤销申请</button>
					<button type="button" class="btn btn-default" onclick="javascript:history.back();">返回</button>
				</div>
			</div>
	   </div>

	</div>
	
</form>
</body>


</html>