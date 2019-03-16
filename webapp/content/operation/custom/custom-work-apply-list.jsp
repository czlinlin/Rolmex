<%@page contentType="text/html;charset=UTF-8" %>
<%@include file="/taglibs.jsp" %>
<%pageContext.setAttribute("currentHeader", "bpm-workspace");%>
<%pageContext.setAttribute("currentMenu", "bpm-process");%>
<!doctype html>
<html lang="en">
<head>
    <%@include file="/common/meta.jsp" %>

    <title><spring:message code="demo.demo.input.title" text="麦联"/></title>
    <%@include file="/common/s3.jsp" %>
    <script type="text/javascript" src="${cdnPrefix}/bootbox/bootbox.min1.js"></script>
    <link href="${cdnPrefix}/xform3/styles/xform.css" rel="stylesheet">
    <script type="text/javascript" src="${cdnPrefix}/xform3/xform-packed.js"></script>
    <link type="text/css" rel="stylesheet" href="${cdnPrefix}/userpicker3-v2/userpicker.css">
    <script type="text/javascript" src="${cdnPrefix}/userpicker3-v2/userpickercustomaudit.js"></script>
    <script type="text/javascript" src="${cdnPrefix}/operation/TaskOperation.js"></script>
   	<script type="text/javascript" src="${cdnPrefix}/userpicker3-v2/userpickerbycopy.js?v=1.0"></script>

    <script type="text/javascript">

        $(function () {
            //审批人
          createUserPicker({
                modalId: 'leaderPicker',//这个 其实是弹出的窗口的div id
                targetId: 'leaderDiv', //这个是点击哪个 会触发弹出窗口
                inputStoreIds: {iptid: "leaderId", iptname: "leaderName"},//存储已选择的ID和name的input的id
                auditId: 'ulapprover',//显示审批步骤
                showExpression: true,
                multiple: true,
                searchUrl: '${tenantPrefix}/rs/user/searchVNoMe',
                treeUrl: '${tenantPrefix}/party/asyncTree.do?partyStructTypeId=1&notViewPost=true&notAuth=false',
                childUrl: '${tenantPrefix}/rs/party/searchUserNoMe'
            });
			//抄送
           /*  createUserPicker({
                modalId: 'ccUserPicker',
                targetId: 'ccDiv',
                inputStoreIds: {iptid: "btnPickerMany", iptname: "userName"},//存储已选择的ID和name的input的id
                multiple: true,
                showExpression: true,
                searchUrl: '${tenantPrefix}/rs/user/searchVNoMe',
                treeUrl: '${tenantPrefix}/party/asyncTree.do?partyStructTypeId=1&notViewPost=true&notAuth=false',
                childUrl: '${tenantPrefix}/rs/party/searchUserNoMe'
            }) */
            createUserPickerCopy({
        		modalId: 'userPicker',
        		showExpression: true,
        		multiple: true,
        		searchUrl: '${tenantPrefix}/rs/user/searchVNoMe',
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
			
            var sectionJson = [{"begin": "#pickerStartTime", "end": "#pickerEndTime"}];
            fnSectionPickerTime(sectionJson)

          
        })
		 $(function(){
			  $("#formTypeForId :radio").click(function(){
			   	var type = $(this).val();
			   	if(type == '4'){
			   		$("#totalTdId").html("");
			   	}else{
			   		$("#totalTdId").html('<span >共</span>'
	                    	+'<input style="padding-left:10px;width: 30%;background:#eee;" type="text" id="totalTime" name="totalTime">'
	                    	+'<span >时</span>'
			   				);
			   		
			   	}
			  });
		 });
       
        
        function startProcessInstance() {

            //没填写主题不能发起流程
            /* if ($("#theme").val().length == 0) {
                alert("请填写主题");
                return false;
            } */

            //没填写内容不能发起流程
            if ($("#applyContent").val().length == 0) {
                alert("请填写内容");
                return false;
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

            //必须选择下一步审批人 才能发起申请
            if (document.getElementById('leaderId').value == "") {
                alert("请选择审批人！");
                return false;
            }
            
            /* if ($("#theme").val().length>100) {
                alert("主题字数最多为100字");
                return false;
            } */
            
            if ($("#applyContent").val().length >"4000") {
                alert("申请内容字数最多为4000字");
                return false;
            }

            //下一步审批人不能选择发起人自己
            //if (document.getElementById('leaderId').value ==document.getElementById('uid').value){
            //alert("下一步审批人不能选择自己！");
            //return false;
            //}

            //抄送人不能是下一步审批人
            //var leaderName=document.getElementById('leaderName').value;
            //var ccName = document.getElementById('userName').value;
            //if ( (ccName.indexOf(leaderName) >= 0)){
            //alert("抄送人与审批人重复！");
            //return false;
            //}
            
            
            
            var formType = $("#formType").val();
          	//请假
            if(formType == '1'){
            	//判断当前人员是否绑定考勤组
                $.ajax({      
    	            url: '${tenantPrefix}/workOperationCustom/checkedAttendanceRecords.do',      
    	            datatype: "json",
    	            data:{},
    	            type: 'get',      
    	            success: function (data) {
    	            	//成功后回调   
    	            	if(data == 'false'){
    	            		bootbox.alert("请先设置考勤日期！");
    	            		return false;
    	            	}else{
    	            		//判断受理单编号是否存在
    	        			applyCodeIfExist();
    	            	}
    	            	
    	            },      
    	            error: function(e){      
    	            	//loading.modal('hide');
    	            	//失败后回调      
    	                alert("服务器请求失败,请重试");  
    	                $("#confirmStartProcess").removeAttr("disabled");//将按钮可用 
    	            }
    	       });
            }else{
            	//判断受理单编号是否存在
    			applyCodeIfExist();
            }
            
            
            
            
           
        }
        
      //判断受理单编号是否已存在,若不存在，返回当前受理单号，若存在，生成一个新的受理单号
		function applyCodeIfExist() {
	
			var loading = bootbox.dialog({
                message: '<p style="width:90%;margin:0 auto;text-align:center;">提交中...</p>',
                size: 'small',
                closeButton: false
         	});
    	  
			var applyCode = document.getElementById('applyCode').value;
			
			$.ajax({      
	            url: '${tenantPrefix}/rs/business/applyCodeIfExist',      
	            datatype: "json",
	            data:{"applyCode": applyCode},
	            type: 'get',      
	            success: function (e) {
	            	//成功后回调   
	            	if (e == "-1") {
	            		alert("系统正在处理，请耐心等待！");  
	            	} else {
	            		$("#applyCode").val(e);
	        			$('#xform').attr('action', '${tenantPrefix}/workOperationCustom/custom-work-startProcessInstance.do');
	                    $('#xform').submit();
	            	}
	            },      
	            error: function(e){      
	            	loading.modal('hide');
	            	//失败后回调      
	                alert("服务器请求失败,请重试");  
	                $("#confirmStartProcess").removeAttr("disabled");//将按钮可用 
	            }
	       }); 
        }
        

        function MaxWordsTheme() {

            if ($("#theme").val().length == "100") {
                alert("主题字数已达上限100字");
            }
        }

        function MaxWords() {

            if ($("#applyContent").val().length == "4000") {
                alert("申请内容字数已达上限4000字");
            }
        }
		//根据用户id获取其大区id
		$(function (){
			$.getJSON('${tenantPrefix}/rs/party/AreaName',{userId : <%=request.getParameter("userName")%>},function(data){
				if(data[0] != null && data[0].name.substring(2) == "大区"){
					$("#areaId").val(data[0].id);
				}
			})
		})
    </script>
    <script  type="text/javascript">
    function year_change(){
               var years=document.getElementById("year").value;
               var mouths=document.getElementById("mouth").value;
               var days=document.getElementById("day").value;
               if((years % 4 ==0 && years % 100!=0) || years % 400 ==0){
                  if(mouths=="2"){
                	  if(days > 29){
                		  alert("这个月29天");
                		  $("#day").val("");
                	  }
                  }else if(mouths=="4" || mouths=="6"|| mouths=="9" || mouths=="11"){
                	  if(days > 30){
                		  alert("这个月30天");
                		  $("#day").val("");
                	  }
                  }else{
                	  if(days > 31){
                		  alert("这个月31天"); 
                		  $("#day").val("");
                	  }
                  }
               }else{
                   if(mouths=="2"){
                	   if(days > 28){
                		  alert("这个月28天");  
                		  $("#day").val("");
                	   }
                  }else if(mouths=="4" || mouths=="6"|| mouths=="9" || mouths=="11"){
                	  if(days > 30){
                		  alert("这个月30天");
                		  $("#day").val("");
                	  }
                      
                  }else{
                	  if(days > 31){
                		  alert("这个月31天");
                		  $("#day").val("");
                	  }
                  }
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
    #tb1 td {
        border: 1px solid #BBB
    }

    .f_td {
        width: 120px;
        font-size: 12px;
        white-space: nowrap
    }

    .f_r_td {
        width: 130px;
        text-align: left;
    }

    #tb1 tr td input {
        border: navajowhite;
        /*  width: 100%;
        height: 28px;  */
    }

    #tb1 tr td textarea {
        border: navajowhite;
    }

    #tb1 tr td {
        text-align: center;
        line-height: 28px;
        height:28px;
    }

    #tb1 tr td.f_td.f_right {
        text-align: right;
    }

    #tb1 tr td input.input_width {
        width: auto;
    }

    
    #fileTable td{border:0px solid #BBB;}
</style>


<body style="margin-bottom:60px;">
<%@include file="/header/bpm-workspace3.jsp" %>
<form id="xform" method="post" action="${tenantPrefix}/operationApply/process-operationApply-startProcessInstance.do"
      class="xf-form" enctype="multipart/form-data">
    申请单
    <br/>
    <div class="container">
        <section id="m-main" class="col-md-12" style="padding-top:65px;">
            <input id="processDefinitionId" type="hidden" name="processDefinitionId"
                   value="<%= request.getParameter("processDefinitionId")%>">
            <input id="bpmProcessId" name="bpmProcessId" type="hidden"
                   value="<%= request.getParameter("bpmProcessId")%>">
            <input id="autoCompleteFirstTask" type="hidden" name="autoCompleteFirstTask" value="false">
            <input id="businessKey" type="hidden" name="businessKey" value="<%= request.getParameter("businessKey")%>">
            <input id="url" type="hidden" name="url" value="/workOperationCustom/custom-work-detail.do?suspendStatus=custom">
            <input id="area" type="hidden" name="area" value="<tags:areaName userId="${userName}"/> ">
            <input id="areaId" type="hidden" name="areaId" value="<%= request.getParameter("areaId")==null?"":request.getParameter("areaId")%>">
            <input id="uid" type="hidden" name="uid" value="<%= request.getParameter("userName")%>">
            <input name="businessType" type="hidden" value="自定义" id="businessType"/>
            <input name="busType" type="hidden" value="9999" id="busType"/>
            <table id="tb1" style="width:100%;">
                <tr>
                    <td colspan='6' align='center' class='f_td'>
                    	<c:if test="${formType == 1 }">
                    		<h2>请假申请单</h2>
                    		<input name="theme" type="hidden"  id="theme" value="请假申请"/>
                    		<input name="businessDetail" type="hidden" value="请假申请" id="businessDetail"/>
                    		<input name="busDetails" type="hidden" value="8001" id="busDetails"/>
                    	</c:if>
                    	<c:if test="${formType == 2 }">
                    		<h2>出差外出申请单</h2>
                    		<input name="theme" type="hidden"  id="theme" value="出差外出申请"/>
                    		<input name="businessDetail" type="hidden" value="出差外出申请" id="businessDetail"/>
                    		<input name="busDetails" type="hidden" value="8002" id="busDetails"/>
                    	</c:if>
                    	<c:if test="${formType == 3 }">
                    		<h2>加班申请单</h2>
                    		<input name="type" type="hidden" id="type" value="1">
                    		<input name="theme" type="hidden"  id="theme" value="加班申请"/>
                    		<input name="businessDetail" type="hidden" value="加班申请" id="businessDetail"/>
                    		<input name="busDetails" type="hidden" value="8003" id="busDetails"/>
                    	</c:if>
                    	<c:if test="${formType == 4 }">
                    		<h2>特殊考勤说明申请单</h2>
                    		<input name="theme" type="hidden"  id="theme" value="特殊考勤说明申请"/>
                    		<input name="businessDetail" type="hidden" value="特殊考勤说明申请" id="businessDetail"/>
                    		<input name="busDetails" type="hidden" value="8004" id="busDetails"/>
                    		
                    	</c:if>
                        
                    </td>
                </tr>
                <tr>
                    <td colspan='6' class='f_td f_right' align='right' style='padding-right:20px;font-size: 14px;'>
                        提交次数：0&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; 受理单编号：<span id="spanApplyCode">${code}</span><input
                            id="applyCode" class="input_width" style="display:none;" name="applyCode" value="${code}"
                            readonly>
                      <input id="formType" type="hidden" name="formType" value="<%= request.getParameter("formType")%>">
                    </td>
                </tr>
                <tr>
                	<td><span>部门</span>：</td>
                	<td style="w">
                		<input type="text" value="<tags:user userId="${departmentName}"/>" name="departmentName" readOnly/>
                	</td>
                	<td style="width: 110px;"><span>姓名</span>：</td>
                	<td>
                		<input style="" type="text" value="<tags:user userId="${userName}"/>" name="name" readOnly/>
                	</td>
                	<td style="width: 30%;align:center" colspan="2">
                		${nowDate }
                		<input type="hidden" name="date" value="${nowDate }">
                	</td>
                </tr>
                <c:if test="${formType == 1 }">
                	 <tr>
	                	<td><span>类别</span>：</td>
	                	<td colspan='5'>
	                		<div>
	                		<input checked="checked"  type="radio" name="type" value="1"><label>&nbsp;病假</label>
	                		<input type="radio" name="type" value="2"><label>&nbsp;事假</label>
	                		<input type="radio" name="type" value="3"><label>&nbsp;倒休假</label>
	                		<input type="radio" name="type" value="4"><label>&nbsp;年假</label>
	                		<input type="radio" name="type" value="5"><label>&nbsp;补休假</label>
	                		<input type="radio" name="type" value="6"><label>&nbsp;婚假</label>
	                		<input type="radio" name="type" value="7"><label>&nbsp;产假</label>
	                		<input type="radio" name="type" value="8"><label>&nbsp;丧假</label>
	                		<input type="radio" name="type" value="9"><label>&nbsp;其他</label>
	                		</div>
	                	</td>
	                </tr>
                </c:if>
                <c:if test="${formType == 2 }">
                	 <tr>
	                	<td><span>类别</span>：</td>
	                	<td colspan='3'>
	                		<div>
	                		<input checked="checked" type="radio" name="type" value="1"><label>&nbsp;出差</label>
	                		<input type="radio" name="type" value="2"><label>&nbsp;因公外出</label>
	                		<input type="radio" name="type" value="3"><label>&nbsp;其他</label>
	                		</div>
	                	</td>
	                	<td><span>目的地</span>：</td>
	                	<td>
	                		<input style="background-color:#eee;padding-left:10px;" type="text" name="destination" id="destination" placeholder="请输入目的地"> 
	                	</td>
	                </tr>
                </c:if>
                <c:if test="${formType == 4 }">
                	 <tr>
	                	<td><span>特殊考勤类别</span>：</td>
	                	<td colspan='5'>
	                		<div id="formTypeForId">
	                		<input checked="checked" type="radio" name="type" value="1"><label>&nbsp;销假</label>
	                		<input type="radio" name="type" value="2"><label>&nbsp;迟到</label>
	                		<input type="radio" name="type" value="3"><label>&nbsp;临时外出</label>
	                		<input type="radio" name="type" value="4"><label>&nbsp;漏打卡</label>
	                		<input type="radio" name="type" value="5"><label>&nbsp;其他</label>
	                		</div>
	                	</td>
	                </tr>
                </c:if>
                <tr>
                    <td align='center' class='f_td'>
                        <span style="color:Red">*</span><span style="font-size: 14px;">事由：</span>
                    </td>
               
                    <td colspan='5' style='height:100px'>
                        <textarea maxlength="4000" name="applyContent" id="applyContent" rows="2" cols="20"
                                  class="text0"
                                  style="height:99px;width:100%;padding-left:10px;padding-top:10px;background:#eee;"
                                  ></textarea>
                    </td>
                </tr>
                <tr>
                	<td>
                        <span style="color:Red">*</span><span>时间</span>：
                    </td>
                    <td colspan="3">
                    	<input autocomplete="off" placeholder="请选择开始时间" type="text" id="startDate" name="startDate" onclick="WdatePicker({maxDate:'#F{$dp.$D(\'endDate\')}',dateFmt:'yyyy-MM-dd HH:mm'})" class="Wdate" style="width:200px;background-color:#eee;padding-left:10px;"/>
                    	
                           &emsp;<span>至</span>&emsp; 
                           <input autocomplete="off" placeholder="请选择结束时间" type="text" id="endDate" name="endDate" onclick="WdatePicker({minDate:'#F{$dp.$D(\'startDate\')}',dateFmt:'yyyy-MM-dd HH:mm'})" class="Wdate" style="width:200px;background-color:#eee;padding-left:10px;"/>
                          
                    </td>
                    <td colspan="2" id="totalTdId">
                    	<span >共</span>
                    	<input autocomplete="off" style="padding-left:10px;width: 30%;background:#eee;" type="text" id="totalTime" name="totalTime" onkeyup="checkedTime(this)" onafterpaste="checkedTime(this)">
                    	<span >时</span>
                    </td>
                </tr>
                <c:if test="${formType == 2 }">
                	<tr>
                		<td><span>同行人员</span>：</td>
                		<td colspan='5'>
	                        <div class="input-group txUserPicker" style="width:100%;">
	                            <input id="txId" name="txnos" type="hidden"
	                                   value="${model.leader}">
	                            <input type="text" id="txName" name="txName" class="form-control required"
	                                   value="<tags:user userId="${model.leader}"></tags:user>" minlength="2"
	                                   maxlength="50" class="form-control" readOnly placeholder="点击后方图标即可选人">
	                            <div id='txDiv' class="input-group-addon"><i class="glyphicon glyphicon-user"></i>
	                            </div>
	                        </div>
	                    </td>
                	</tr>
                </c:if>
             
              
                <tr>
                    <td>
                       <span>抄送</span>： 
                    </td>
                    <td colspan='5'>
                        <%-- <div class="input-group " style="width:100%;">
                            <input id="btnPickerMany" type="hidden" name="ccnos" class="input-medium"
                                   value="${ccnos}">
                            <input type="text" id="userName" name="ccName"
                                   value="${ccnames}" class="form-control" readOnly placeholder="点击后方图标即可选人">
                            <div id="ccDiv" class="input-group-addon"><i class="glyphicon glyphicon-user"></i></div>
                        </div> --%>
                        <div class="input-group userPicker" style="width:100%;">
						  <input id="btnPickerMany" type="hidden" name="ccnos" class="input-medium" value="">
						  <input id="userName" type="text" name="ccName" placeholder="点击后方图标即可选人" class="form-control" readonly>
						  <div id="ccDiv" class="input-group-addon"><i class="glyphicon glyphicon-user"></i></div>
					    </div>
                    </td>
                </tr>
                <tr>
                	<td colspan='6'>
                		<span style="color:red;">点击下面可快捷选择审核人</span><br/>
                		<div>
                			<%@include file="/common/custom_preset_approver.jsp" %>
                		</div>
                	</td>
                </tr>
                <tr>
                    <td colspan='6'><span style="color:red;">请按顺序选择审核人</span><br/>
                        <ul id="ulapprover" style="width:96%;margin:0 auto;list-style:none;">
                        </ul>
                    </td>
                </tr>
                <tr>
                    <td><font color="red ">*</font><span>审批人</span>：</td>
                    <td colspan='5'>
                        <div class="input-group leaderPicker" style="width:100%;">
                            <input id="leaderId" name="nextID" type="hidden" name="leader"
                                   value="${model.leader}">
                            <input type="text" id="leaderName" name="nextUser" class="form-control required"
                                   value="<tags:user userId="${model.leader}"></tags:user>" minlength="2"
                                   maxlength="50" class="form-control" readOnly placeholder="点击后方图标即可选人">
                            <div id='leaderDiv' class="input-group-addon" style="cursor:pointer;"><i class="glyphicon glyphicon-user"></i>
                            </div>
                        </div>
                    </td>
                </tr>
                <%-- <tr>
                	<td><span>添加附件</span>：</td>
                	<td colspan="6" class="tdfile">
                		 <%@include file="/common/_uploadFile.jsp" %>
                    	<span style="color:gray;"> 请添加共小于200M的附件 </span>
                	</td>
                </tr> --%>
            </table>
            <br>
            <%-- <c:if test="${formType == 1 }"> --%>
<!--                 <span style="color:red;">注：此申请单须由申请人亲自填写，经部门经理、主管领导、集团人事部综合主管王利娟、集团总裁办副总裁臧总，审批后报人事部审核合格后方可生效</span>
 -->           <%--  </c:if> --%>
            <%-- <c:if test="${formType == 3 }">
                <span style="color:red;">注：此加班单须由加班人亲自填写，经部门经理、主管领导、集团人事部综合主管王丽娟、集团总裁办副总裁臧总审批后报人事部审核合格后方可生效</span>
            </c:if> --%>
        </section>
        <!-- end of main -->
    </div>
    <div class="navbar navbar-default navbar-fixed-bottom">
        <div class="text-center" style="padding-top:8px;">
            <div class="text-center" style="padding-top:8px;">
                <button id="confirmStartProcess" class="btn btn-default" type="button" onclick="startProcessInstance()">
                    提交数据
                </button>
                <button type="button" class="btn btn-default" onclick="javascript:history.back();">返回</button>
            </div>
        </div>
    </div>
</form>
</body>
</html>