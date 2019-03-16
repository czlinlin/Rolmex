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
        /* createUserPicker({
            modalId: 'leaderPicker',//这个 其实是弹出的窗口的div id
            targetId: 'leaderDiv', //这个是点击哪个 会触发弹出窗口
            showExpression: true,
            searchUrl: '${tenantPrefix}/rs/user/searchV',
            treeUrl: '${tenantPrefix}/rs/party/treeNoAuth?partyStructTypeId=1',
            childUrl: '${tenantPrefix}/rs/party/searchUser'
        }); */
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
        }) */
        createUserPickerCopy({
    		modalId: 'userPicker',
    		showExpression: true,
    		multiple: true,
    		inputStoreIds: {iptid: "btnPickerMany", iptname: "userName"},//存储已选择的ID和name的input的id
    		searchUrl: '${tenantPrefix}/rs/user/search',
    		treeNoPostUrl: '${tenantPrefix}/party/asyncTree.do?partyStructTypeId=1&notViewPost=true',
    		treeUrl: '${tenantPrefix}/party/asyncTree.do?partyStructTypeId=1&notViewPost=true',
    		childUrl: '${tenantPrefix}/rs/party/searchUser',
    		childPostUrl: '${tenantPrefix}/rs/party/searchPost'
    	});
		 
	})
	
		//若该条流程 是   修改花名册  那么显示花名册的详情页
        function openPersonInfo(){
			popWin.scrolling="auto";
			popWin.showWin("1080"
	    			,"600"
	    			,"花名册"
	    			,"${tenantPrefix}/user/person-info-input-forModify.do?applyCode=${customEntity.applyCode}&id=${personInfoId}&partyEntityId=${partyEntityId}");
        }
		//若该条流程 是调岗    那么显示调岗的详情页
	    function changePost(){
	    	popWin.showWin("768"
	    			,"600"
	    			,"编辑岗位调整"
	    			,"${tenantPrefix}/user/person-info-position-change-forModify.do?applyCode=${customEntity.applyCode}&id=${personInfoId}&isdetail=0");
		}
		
	  //若该条流程 是新建组织机构， 那么跳转到新建组织机构页面
	    function orgCreate(){
	    	popWin.showWin("768"
	    			,"550"
	    			,"新建组织结构的修改"
	    			,"${tenantPrefix}/party/org-update-for-audit.do?applyCode=${customEntity.applyCode}&isdetail=0");
		}
	    
	    function orgUpdate(){
	    	popWin.showWin("768"
	    			,"550"
	    			,"编辑组织结构的修改"
	    			,"${tenantPrefix}/party/org-update-for-audit.do?applyCode=${customEntity.applyCode}&isdetail=0");
		}
	    
	    function orgRelation(){
	    	popWin.showWin("768"
	    			,"400"
	    			,"岗位关联人员"
	    			,"${tenantPrefix}/party/position-user-input-for-audit.do?applyCode=${customEntity.applyCode}&isdetail=0");
	    }
		
	
	function confirmOperation(flag) {
		
		//没填写主题不能发起流程
		if($("#theme").val().length ==0){
   		 	alert("请填写主题");
   		 	return false;
	   	 }
		
		if ($("#theme").val().length>100) {
            alert("主题字数最多为100字");
            return false;
        }
		
		//没填写内容不能发起流程
		
		if("${personTypeID}" == ''){
			if($("#applyContent").val().length ==0){
	   		 	alert("请填写内容");
	   		 	return false;
		   	 }
		}
		if(flag == 3){
			if($("#allLeaderId").val() == ''){
	   		 	alert("请选择审核人");
	   		 	return false;
		   	 }
		}
		
		if(flag == 4){
			var msg = "确定要撤销申请吗,请确认？";  
            if (!confirm(msg)){ 
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
        		
        		$('#xform').attr('action', '${tenantPrefix}/operationCustom/custom-completeTask.do?flag=' + flag);
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
	
	</script>  
</head>
<style type="text/css">
          #tb1 td{border:1px solid #BBB }
        .f_td{ width:120px; font-size:12px;white-space:nowrap }
        .f_r_td{ width:130px; text-align:left;}
        #tb1 tr td input{border: navajowhite;width: 100%;} 
        #tb1 tr td textarea{border: navajowhite;}     
        #tb1 tr td{text-align:center;} 
        #tb1 tr td.f_td.f_right{text-align:right;}    
        #tb1 tr td input.input_width{width:auto;}   
        #tb1 tr td input{border: navajowhite;width: 100%;} 
        #tb1 tr td textarea{border: navajowhite;}     
        #tb1 tr td{text-align:center;line-height:28px;} 
        #tb1 tr td.f_td.f_right{text-align:right;}    
        #tb1 tr td input.input_width{width:auto;}
        
        input{height:28px;}
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
                <td colspan='4' align='center' class='f_td'>
                    <h2>调整自定义申请单</h2>
                </td>
            </tr>
	         <tr>
	          		<td colspan='4'  class='f_td f_right' align='right' style='padding-right:20px;'> 
	                    	提交次数：${customEntity.submitTimes}&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<input id="submitTimes" style="display:none;" class="input_width" name="submitTimes" value="${customEntity.submitTimes}" readonly> &nbsp; 受理单编号：${customEntity.applyCode}<input id="applyCode" class="input_width" style="display:none" name="applyCode" value="${customEntity.applyCode}" readonly>
	                </td>
	         </tr>
    	
    		<tr>
                <td >
                 <span id='tag_Theme'>&nbsp;<span style="color:Red">*</span>主题</span>：
                </td>
                <td colspan='4'   >
                    <input  maxlength="100"  style="background:#eee;"  name="theme"  id="theme" value="${customEntity.theme}" readonly>
                </td>
            </tr>
	      	<tr>
	       		<td>
	       		抄送
	       		</td>
	       		<td  colspan='3'>
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
                    <span id='tag_bustype'>&nbsp;<span style="color:Red">*</span>申请业务类型</span>：
                </td>
                <td>
	                    <select name="busType" id="busType" class="form-control"  style="border:none;">
							<option value="9999">自定义</option>
				 		</select>
						<input name="businessType" type="hidden" value="自定义"  id="businessType"  />
                </td>
                <td>
                    <span id='tag_busDetails'>&nbsp;<span style="color:Red">*</span>业务细分</span>：
                </td>
                <td>
                    <select name="busDetails" id="busDetails" class="form-control"  style="border:none;">
						<option value="8888">自定义申请</option>
					</select>
					<input name="businessDetail" type="hidden" value="自定义申请"  id="businessDetail"  />
                </td>
            </tr>
            <tr>
            	<td>
                    <span id='tag_toStart'>&nbsp;业务级别</span>：
                </td>
                <td>
                      <input id="businessLevel" name="businessLevel" value="${customEntity.businessLevel}" readonly>
                      
                </td>
                <td>
                    <span id='tag_toStart'>&nbsp;发起人</span>：
                </td>
                <td>
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
	                <td colspan='4' align='center' class='f_td'>
	                    <span style="color:Red">*</span>申请内容
	                </td>
	            </tr>
	            <tr>
	                <td colspan='4' style='height:100px' >
	                    <textarea  maxlength="4000"  name="applyContent" id="applyContent" rows="2" cols="20" class="text0" style="height:99px;width:100%;padding-left:10px;padding-top:10px;background:#eee;">${customEntity.applyContent}</textarea>
	                </td>
	            </tr>
            <%-- <c:if test="${personTypeID== 'personadd' 
            			|| personTypeID == 'personUpdate'
            			|| personTypeID=='changePost'
            			|| personTypeID=='orgadd'
            			|| personTypeID=='orgupdate'
            			|| personTypeID=='postwithperson'}">
            			<textarea  maxlength="4000" name="applyContent" id="applyContent" rows="2" cols="20" class="text0" style="height:99px;width:100%;padding-left:10px;padding-top:10px;background:#eee;display:none;">${customEntity.applyContent}</textarea>
            </c:if> --%>
            
            </c:if>
            <c:if test="${personTypeID == 'personadd'||personTypeID == 'personUpdate'}">
                
                 <tr>
                    <td colspan='4' style='height:80px'>
                    
                    	<a href="#" onclick="openPersonInfo()"> 点击这里修改花名册</a>
                    </td>
                </tr>
          	</c:if>
          	
          	<c:if test="${personTypeID == 'changePost'}">
                
                 <tr>
                    <td colspan='4' style='height:80px'>
                    
                    	<a href="#" onclick="changePost()"> 点击这里修改调岗信息</a>
                    </td>
                </tr>
          	</c:if>
          	
          	<c:if test="${personTypeID == 'orgadd'}">
                
                 <tr>
                    <td colspan='4' style='height:80px'>
                    
                    	<a href="#" onclick="orgCreate()"> 点击这里新建组织机构</a>
                    </td>
                </tr>
          	</c:if>
          	
          	<c:if test="${personTypeID == 'orgupdate'}">
                
                 <tr>
                    <td colspan='4' style='height:80px'>
                    
                    	<a href="#" onclick="orgUpdate()"> 点击这里修改组织机构</a>
                    </td>
                </tr>
          	</c:if>
          	
          	<c:if test="${personTypeID == 'postwithperson'}">
                
                 <tr>
                    <td colspan='4' style='height:80px'>
                    
                    	<a href="#" onclick="orgRelation()"> 点击这里进行关联人员的修改</a>
                    </td>
                </tr>
          	</c:if>
          	<tr>
               <td colspan='4' style='height:100px;vertical-align:top;padding:3px 3px 3px 3px;'>
               	<span>审核人</span><br/>
		  		<ul id="ulapprover" style="width:96%;margin:0 auto;list-style:none;">
		  			${approver}
	  			</ul>
	  			<!-- <div id='leaderDiv' class="input-group-addon"><i class="glyphicon glyphicon-user"></i>
	  			
                </div> -->
                <button id="leaderDiv" type="button" class="btn btn-default" style="background-color:#1D82D0;color: #FFFFFF;float:right" >重新选择审核人</button>
               </td>
           </tr>
		   <tr>
		   	   <td><font color="red " size="4"> * </font>下一步审批人</td>
		       <td  colspan='3'>
			       	<!--<div class="form-group">
                         <div class="col-sm-8"> -->
                            <div class="input-group leaderUserPicker" style="width:100%;">
                                <input id="nextLeaderId" type="hidden" name="leader"
                                       value="${auditorId}">
                                <input type="text" id="nextLeaderName" name="leaderName" class="form-control required"
                                       value="<tags:user userId="${auditorName}"></tags:user>" minlength="2"
                                       maxlength="50" class="form-control" readOnly placeholder="点击后方图标即可选人">
                                <%-- <c:if test="${isCanSelect == true}"> --%>
                                   	<!-- <div id='leaderDiv' class="input-group-addon"><i class="glyphicon glyphicon-user"></i>
                                   	</div> -->
                                <%-- </c:if> --%>
                                
                                <input id="allLeaderId" type="hidden" name="allLeaderId"
                                       value="${allAuditorId}">
                                <input id="allLeaderName" type="hidden" name="allLeaderName" class="form-control required"
                                       value="${allAuditorName}">
                                <input id="oldLeaderId" type="hidden" name="oldLeaderId"
                                       value="${allAuditorId}">
                            </div>
                        <!-- </div> 
                    </div>-->
			    </td>
			</tr>
			<tr>
				<td>原有附件</td>
				<td colspan="3"><%@include file="/common/show_edit_file.jsp" %></td>
			</tr>
			<tr>
				<td>添加附件：</td>
				<td colspan="3">
					<%@include file="/common/_uploadFile.jsp" %>
                    <span style="color:gray;"> 请添加共小于200M的附件 </span>
                </td>
			</tr>
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