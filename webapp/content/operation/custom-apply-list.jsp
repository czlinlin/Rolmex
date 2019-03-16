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

            /* createUserPicker({
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

        })

        function startProcessInstance() {

            //没填写主题不能发起流程
            if ($("#theme").val().length == 0) {
                alert("请填写主题");
                return false;
            }

            //没填写内容不能发起流程
            if ($("#applyContent").val().length == 0) {
                alert("请填写内容");
                return false;
            }

            //必须选择下一步审批人 才能发起申请
            if (document.getElementById('leaderId').value == "") {
                alert("请选择审批人！");
                return false;
            }
            
            if ($("#theme").val().length>100) {
                alert("主题字数最多为100字");
                return false;
            }
            
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
            
            //判断受理单编号是否存在
			applyCodeIfExist();
            
           
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
	        			$('#xform').attr('action', '${tenantPrefix}/operationCustom/custom-startProcessInstance.do');
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
        width: 100%;
        height: 28px;
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
            <input id="url" type="hidden" name="url" value="/operationCustom/custom-detail.do?suspendStatus=custom">
            <input id="area" type="hidden" name="area" value="<tags:areaName userId="${userName}"/> ">
            <input id="areaId" type="hidden" name="areaId" value="<%= request.getParameter("areaId")==null?"":request.getParameter("areaId")%>">
            <input id="uid" type="hidden" name="uid" value="<%= request.getParameter("userName")%>">
            <table id="tb1" style="width:100%;">
                <tr>
                    <td colspan='4' align='center' class='f_td'>
                        <h2>自定义申请单</h2>
                    </td>
                </tr>
                <tr>
                    <td colspan='4' class='f_td f_right' align='right' style='padding-right:20px;'>
                        提交次数：0&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; 受理单编号：<span id="spanApplyCode">${code}</span><input
                            id="applyCode" class="input_width" style="display:none;" name="applyCode" value="${code}"
                            readonly>
                    </td>
                </tr>
                <tr>
                    <td>
                        <span id='tag_Theme'>&nbsp;<span style="color:Red">*</span>主题</span>：
                    </td>
                    <td colspan='4'>
                        <input maxlength="100" style="background:#eee;" name="theme" style='height:30px' type="text" maxlength="50" id="theme"
                               />
                    </td>
                </tr>
                <tr>
                    <td>
                        抄送
                    </td>
                    <td colspan='4'>
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
                    <td>
                        <span id='tag_bustype'>&nbsp;<span style="color:Red">*</span>申请业务类型</span>：
                    </td>
                    <td>
                        <select name="busType" id="busType" class="form-control" style="border:none;">
                            <option value="9999">自定义</option>
                        </select>
                        <input name="businessType" type="hidden" value="自定义" id="businessType"/>
                    </td>
                    <td>
                        <span id='tag_busDetails'>&nbsp;<span style="color:Red">*</span>业务细分</span>：
                    </td>
                    <td>
                        <select name="busDetails" id="busDetails" class="form-control" style="border:none;">
                            <option value="8888">自定义申请</option>
                        </select>
                        <input name="businessDetail" type="hidden" value="自定义申请" id="businessDetail"/>
                    </td>
                </tr>

                <tr>
                    <!-- <td>
                        <span>&nbsp;业务级别</span>：
                    </td>
                    <td>
                        <input id="businessLevel" name="businessLevel" value="CG" readOnly>
                    </td> -->
                    <td>
                        <span id='tag_toStart'>&nbsp;发起人</span>：
                    </td>
                    <td colspan='4' >
                        <input id="name" style='height:30px' name="name" type="text" value="<tags:user userId="${userName}"/>" readOnly>
                    </td>
                </tr>

                <tr>
                    <td colspan='4' align='center' class='f_td'>
                        <span style="color:Red">*</span>申请内容
                    </td>
                </tr>
                <tr>
                    <td colspan='4' style='height:100px'>
                        <textarea maxlength="4000" name="applyContent" id="applyContent" rows="2" cols="20"
                                  class="text0"
                                  style="height:99px;width:100%;padding-left:10px;padding-top:10px;background:#eee;"
                                  ></textarea>
                    </td>
                </tr>
                <tr>
                    <td colspan='4'><span style="color:red;">请按顺序选择审核人</span><br/>
                        <ul id="ulapprover" style="width:96%;margin:0 auto;list-style:none;">
                        </ul>
                    </td>
                </tr>
                <tr>
                    <td><font color="red " size="4"> * </font>审批人</td>
                    <td colspan='4'>
                        <div class="input-group leaderUserPicker" style="width:100%;">
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
                <tr>
                	<td>添加附件：</td>
                	<td colspan="3" class="tdfile">
                		 <%@include file="/common/_uploadFile.jsp" %>
                    	<span style="color:gray;"> 请添加共小于200M的附件 </span>
                	</td>
                </tr>
            </table>
            <%-- <div>
                <label class="control-label col-md-2" name="fileName">添加附件：</label>
                <div>
                    <%@include file="/common/_uploadFile.jsp" %>
                    <span style="color:gray;"> 请添加共小于200M的附件 </span>
                </div>
            </div> --%>

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