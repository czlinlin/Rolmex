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

    <!-- bootbox -->
    <script type="text/javascript" src="${cdnPrefix}/bootbox/bootbox.min1.js"></script>
    <link href="${cdnPrefix}/xform3/styles/xform.css" rel="stylesheet">
    <script type="text/javascript" src="${cdnPrefix}/xform3/xform-packed.js"></script>

    <link type="text/css" rel="stylesheet" href="${cdnPrefix}/userpicker3-v2/userpicker.css">
    <script type="text/javascript" src="${cdnPrefix}/userpicker3-v2/userpickerbybpm.js"></script>
    <script type="text/javascript" src="${cdnPrefix}/operation/TaskOperation.js"></script>
    <script type="text/javascript" src="${cdnPrefix}/operation/operation.js?v=1.20"></script>
    <%-- <script type="text/javascript" src="${cdnPrefix}/userpicker3-v2/userpickercustom.js"></script> --%>

    <style type="text/css">
        .xf-handler {
            cursor: auto;
        }

        pre {
            white-space: pre-wrap;
            word-wrap: break-word;
            background-color: white;
            border: 0px
        }
    </style>

    <script type="text/javascript">
        document.onmousedown = function (e) {
        };
        document.onmousemove = function (e) {
        };
        document.onmouseup = function (e) {
        };
        document.ondblclick = function (e) {
        };

        var xform;

        $(function () {

            createUserPicker({
                modalId: 'userPicker',
                multiple: true,
                showExpression: true,
                searchUrl: '${tenantPrefix}/rs/user/search',
                treeUrl: '${tenantPrefix}/rs/party/treeNoAuth?partyStructTypeId=1',
                childUrl: '${tenantPrefix}/rs/party/searchUser'
            });

            setTimeout(function () {
                $('.datepicker').datepicker({
                    autoclose: true,
                    language: 'zh_CN',
                    format: 'yyyy-mm-dd'
                })
            }, 500);
        })

        ROOT_URL = '${tenantPrefix}';
        taskOperation = new TaskOperation();

        //完成任务（同意，不同意，驳回）
        function completeTask(flag) {
            if ($("#comment").val().replace(/(^\s*)|(\s*$)/g, "") == "" && flag == 0 || $("#comment").val().replace(/(^\s*)|(\s*$)/g, "") == "" && flag == 2
                || $("#comment").val() == "同意" && flag == 0 || $("#comment").val() == "同意" && flag == 2) {
                alert("不同意和驳回请填写意见");
                return false;
            }
            //每次审核人审核时都先检验该流程的状态是否是已撤回
            $.ajax({      
	            url: '${tenantPrefix}/rs/bpm/getStatus',      
	            datatype: "json",
	            data:{"processInstanceId": $("#processInstanceId").val(),"humanTaskId":$("#humanTaskId").val(),"userId":$("#userId").val()},
	            type: 'get',      
	            success: function (e) {
	            	if(e == 'error'){
	            		alert("该申请已撤回，暂无法审核。");
	            		return false;
	            	}
	            	if(e == 'noAuth'){
	            		alert("您无权审核。");
	            		return false;
	            	}
	            	var conf = {
                        "formId": "xform",
                        "checkUrl": "${tenantPrefix}/rs/customer/opteraion-verifyPassword",
                        "actionUrl": '${tenantPrefix}/processLllegalFreeze/process-operationLllegalFreezeApproval-completeTask.do?flag=' + flag,
                        "iptPwdId": "txtPrivateKey"
                    }
                    operationSubmit(conf);
	            },      
	            error: function(e){      
	            	loading.modal('hide');
	                alert("服务器请求失败,请重试");  
	            }
	       });
        }

        //接收请求数据
        $(function () {
            var id = $("#processInstanceId").val();
            if (id != "") {
                $.getJSON('${tenantPrefix}/rs/processLllegalFreeze/getLllegalFreezeInfo', {
                    id: id
                }, function (data) {
                    for (var i = 0; i < data.length; i++) {
                        //alert(JSON.stringify(data));
                        $("#submitTimes").val(data[i].submitTimes);
                        $("#applyCode").val(data[i].applyCode);
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
                        $("#applyContent").html(data[i].applyContent);
                    }
                });
            };
             //审核环节
            $.ajax({
				url:"${tenantPrefix}/dict/getProcessPostInfoByProcessInstanceId.do",
				data:{processInstanceId:id},
				dataType:"json",
				type:"post",
				success:function(data){
					//console.log(data);
					$('#nextStep').append(data.whole);
				},
				error:function(){
					alert("获取流程审核人岗位信息出错！");
				}
			});
        })

        function empty() {
            if ($("#comment").val() == "同意") {
                $("#comment").val('');
            }
        }
        function getAgree() {
            if ($("#comment").val() == "") {
                $("#comment").val('同意');
            }
        }
    </script>
</head>

<body>
<%@include file="/header/bpm-workspace3.jsp" %>
<form id="xform" method="post" class="xf-form" enctype="multipart/form-data">
    <div class="container">

        <!-- start of main -->
        <section id="m-main" class="col-md-12" style="padding-top:65px;">
			<table width="100%" cellspacing="0" cellpadding="0" border="0" align="center" class="xf-table">
			  <tbody>
			    <tr>
				  <td width="25%" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
				    <label style="display:block;text-align:center;margin-bottom:0px;padding-top:10px;padding-bottom:10px;">审核环节&nbsp;</label>
				  </td>
				  <td width="75%" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top" colspan="3" rowspan="1">
				    <div id="nextStep"></div>
				  </td>
				</tr>
			  </tbody>
		   </table>
            <input id="processInstanceId" type="hidden" name="processInstanceId" value="${processInstanceId}">
            <input id="humanTaskId" type="hidden" name="humanTaskId" value="${humanTaskId}">
            <input id="bpmProcessId" type="hidden" name="bpmProcessId" value="">
            <input id="autoCompleteFirstTask" type="hidden" name="autoCompleteFirstTask" value="false">
            <input id="activityId" type="hidden" name="activityId" value="">
            <input id="lllegalFreezeId" name="lllegalFreezeId" type="hidden">
            <input id="userId" name="userId" type="hidden" value="<%=userId %>">
            <div id="xf-form-table">
                <div id="xf-1" class="xf-section">
                    <h1 style="text-align:center;">违规冻结/解冻审批单</h1>
                </div>

                <div id="xf-2" class="xf-section">
                    <table class="xf-table" width="100%" cellspacing="0" cellpadding="0" border="0" align="center">
                        <tbody>
                        <tr id="xf-2-0">
                            <td id="xf-2-0-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left"
                                colspan="4" width="100%">
                                <div class="xf-handler">
                                    <label style="display:block;text-align:right;margin-bottom:0px;">提交次数： <input
                                            style="border:0px;width:10px" readonly type="text" id="submitTimes"
                                            name="submitTimes"> &nbsp;&nbsp;申请单号: <input type="text" id="applyCode"
                                                                                         name="applyCode"
                                                                                         style="border:0px"
                                                                                         readonly></label>
                                </div>
                            </td>
                        </tr>
                        <tr id="xf-2-1">
                            <td id="xf-2-1-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left" width="25%">
                                <div class="xf-handler">
                                    <label style="display:block;text-align:center;margin-bottom:0px;"><font color='red'>*</font>&nbsp;主题：</label>
                                </div>
                            </td>
                            <td id="xf-2-1-1" class="xf-cell xf-cell-right xf-cell-bottom" colspan="3" width="75%">
                                <div class="xf-handler" id="theme"></div>
                            </td>
                        </tr>
                        <tr id="xf-2-2">
                            <td id="xf-2-2-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left" width="25%">
                                <div class="xf-handler">
                                    <label style="display:block;text-align:center;margin-bottom:0px;">抄送：</label>
                                </div>
                            </td>
                            <td id="xf-2-2-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top" colspan="3"
                                width="75%">
                                <div class="input-group userPicker" id="cc"></div>
                            </td>

                        </tr>
                        <tr id="xf-2-3">
                            <td id="xf-2-3-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left" width="25%">
                                <div class="xf-handler">
                                    <label style="display:block;text-align:center;margin-bottom:0px;"><font color='red'>*</font>&nbsp;申请业务类型：</label>
                                </div>
                            </td>
                            <td id="xf-2-3-1" class="xf-cell xf-cell-right xf-cell-bottom" width="25%">
                                <div class="xf-handler" id="businessType"></div>
                            </td>
                            <td id="xf-2-3-2" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left" width="25%">
                                <div class="xf-handler">
                                    <label style="display:block;text-align:center;margin-bottom:0px;"><font color='red'>*</font>&nbsp;业务细分：</label>
                                </div>
                            </td>
                            <td id="xf-2-3-3" class="xf-cell xf-cell-right xf-cell-bottom" width="25%">
                                <div class="xf-handler" id="businessDetail"></div>
                            </td>
                        </tr>

                        <tr id="xf-2-4">
                            <td id="xf-2-4-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left"
                                width="25%">
                                <div class="xf-handler">
                                    <label style="display:block;text-align:center;margin-bottom:0px;"><font color='red'>*</font>&nbsp;业务级别：</label>
                                </div>
                            </td>
                            <td id="xf-2-4-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left"
                                width="25%">
                                <div class="xf-handler" id="businessLevel"></div>
                            </td>
                            <td id="xf-2-4-2" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left"
                                width="25%">
                                <label style="display:block;text-align:center;margin-bottom:0px;">发起人：</label>
                            </td>
                            <td id="xf-2-4-3" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left"
                                width="25%">
                                <div class="xf-handler" id="initiator"></div>
                            </td>
                        </tr>

                        <tr id="xf-2-5">
                            <td id="xf-2-5-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left"
                                width="100%" colspan="4">
                                <div class="xf-handler">
                                    <label style="display:block;text-align:center;margin-bottom:0px;">申请内容</label>
                                </div>
                            </td>
                        </tr>
                        <tr id="xf-2-6">
                            <td id="xf-2-6-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left"
                                width="25%">
                                <div class="xf-handler">
                                    <label style="display:block;text-align:center;margin-bottom:0px;"><font color='red'>*</font>&nbsp;编号：</label>
                                </div>
                            </td>
                            <td id="xf-2-6-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left"
                                width="25%">
                                <div class="xf-handler" id="ucode"></div>
                            </td>
                            <td id="xf-2-6-2" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left"
                                width="25%">
                                <div class="xf-handler">
                                    <label style="display:block;text-align:center;margin-bottom:0px;"><font color='red'>*</font>&nbsp;姓名：</label>
                                </div>
                            </td>
                            <td id="xf-2-6-3" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left"
                                width="25%">
                                <div class="xf-handler" id="name"></div>
                            </td>
                        </tr>
                        <tr id="xf-2-7">
                            <td id="xf-2-7-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left"
                                width="25%">
                                <div class="xf-handler">
                                    <label style="display:block;text-align:center;margin-bottom:0px;"><font color='red'>*</font>&nbsp;福利级别：</label>
                                </div>
                            </td>
                            <td id="xf-2-7-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left"
                                width="25%">
                                <div class="xf-handler" id="welfareLevel"></div>
                            </td>
                            <td id="xf-2-7-2" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left"
                                width="25%">
                                <div class="xf-handler">
                                    <label style="display:block;text-align:center;margin-bottom:0px;"><font color='red'>*</font>&nbsp;资格状态：</label>
                                </div>
                            </td>
                            <td id="xf-2-7-3" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left"
                                width="25%">
                                <div class="xf-handler" id="qualificationsStatus"></div>
                            </td>
                        </tr>
                        <tr id="xf-2-8">
                            <td id="xf-2-8-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left"
                                width="25%">
                                <div class="xf-handler">
                                    <label style="display:block;text-align:center;margin-bottom:0px;"><font color='red'>*</font>&nbsp;所属体系：</label>
                                </div>
                            </td>
                            <td id="xf-2-8-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left"
                                width="25%">
                                <div class="xf-handler" id="system"></div>
                            </td>
                            <td id="xf-2-8-2" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left"
                                width="25%">
                                <div class="xf-handler">
                                    <label style="display:block;text-align:center;margin-bottom:0px;"><font color='red'>*</font>&nbsp;联系方式：</label>
                                </div>
                            </td>
                            <td id="xf-2-8-3" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left"
                                width="25%">
                                <div class="xf-handler" id="contact"></div>
                            </td>
                        </tr>
                        <tr id="xf-2-9">
                            <td id="xf-2-9-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left"
                                width="25%">
                                <div class="xf-handler">
                                    <label style="display:block;text-align:center;margin-bottom:0px;"><font color='red'>*</font>&nbsp;所属区域：</label>
                                </div>
                            </td>
                            <td id="xf-2-9-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left"
                                width="25%">
                                <div class="xf-handler" id="area"></div>
                            </td>
                            <td id="xf-2-9-2" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left"
                                width="25%">
                                <div class="xf-handler">
                                    <label style="display:block;text-align:center;margin-bottom:0px;"><font color='red'>*</font>&nbsp;所属分公司：</label>
                                </div>
                            </td>
                            <td id="xf-2-9-3" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left"
                                width="25%">
                                <div class="xf-handler" id="branchOffice"></div>
                            </td>
                        </tr>
                        <tr id="xf-2-10">
                            <td id="xf-2-10-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left"
                                width="25%">
                                <div class="xf-handler">
                                    <label style="display:block;text-align:center;margin-bottom:0px;"><font color='red'>*</font>&nbsp;身份证号：</label>
                                </div>
                            </td>
                            <td id="xf-2-10-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left"
                                width="75%" colspan="3">
                                <div class="xf-handler" id="idNumber"></div>
                            </td>
                        </tr>
                        <tr id="xf-2-11">
                            <td id="xf-2-11-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left"
                                width="25%">
                                <div class="xf-handler">
                                    <label style="display:block;text-align:center;margin-bottom:0px;"><font color='red'>*</font>&nbsp;上属董事：</label>
                                </div>
                            </td>
                            <td id="xf-2-11-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left"
                                width="25%">
                                <div class="xf-handler" id="aboveBoard"></div>
                            </td>
                            <td id="xf-2-11-2" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left"
                                width="25%">
                                <div class="xf-handler">
                                    <label style="display:block;text-align:center;margin-bottom:0px;"><font color='red'>*</font>&nbsp;联系方式：</label>
                                </div>
                            </td>
                            <td id="xf-2-11-3" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left"
                                width="25%">
                                <div class="xf-handler" id="directorContact"></div>
                            </td>
                        </tr>
                        <tr id="xf-2-12">
                            <td id="xf-2-12-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left"
                                width="25%">
                                <div class="xf-handler">
                                    <label style="display:block;text-align:center;margin-bottom:0px;"><font color='red'>*</font>&nbsp;申请受理事项：</label>
                                </div>
                            </td>
                            <td id="xf-2-12-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left"
                                width="75%" colspan="3">
                                <div class="xf-handler" id="applyMatter"></div>
                            </td>
                        </tr>
                        <tr id="xf-2-13">
                            <td id="xf-2-13-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left"
                                width="25%">
                                <div class="xf-handler">
                                    <label style="display:block;text-align:center;margin-bottom:0px;"><font color='red'>*</font>&nbsp;申请内容：</label>
                                </div>
                            </td>
                            <td id="xf-2-13-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left"
                                width="75%" colspan="3">
                                <div class="xf-handler">
                                    <pre>${lllegalFreeze.applyContent}</pre>
                                </div>
                            </td>
                        </tr>

                        <tr id="xf-2-14">
                            <td id="xf-2-14-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left"
                                width="25%">
                                <div class="xf-handler">
                                    <label style="display:block;text-align:center;margin-bottom:0px;">附件：</label>
                                </div>
                            </td>
                            <td id="xf-2-14-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left"
                                width="75%" colspan="3">
                                <div class="col-md-8">
                                    <%@include file="/common/show_file.jsp" %>
                                </div>
                            </td>
                        </tr>
                        <tr id="xf-2-15">
                            <td id="xf-2-15-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
                                <div class="xf-handler">
                                    <label id="commentText" style="display:block;text-align:center;margin-bottom:0px;">审批人意见：</label>
                                </div>
                            </td>
                            <td id="xf-2-15-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left"
                                colspan="3">
                                <div class="xf-handler">
                                    <textarea id="comment" name="comment" maxlength="300" style="width:100%" rows="5"
                                              cols="20" onfocus="empty()" onblur="getAgree()" required>同意</textarea>
                                </div>
                            </td>
                        </tr>
                        </tbody>
                    </table>
                    <table>
                        <tr>
                            <td>
                                <div class="xf-handler">
                                    <label style="display:block;text-align:center;margin-bottom:0px;width:100%"><font
                                            style="color:red">*</font>操作密码</label>
                                </div>
                            </td>
                            <td>
                                <div class="xf-handler">
                                    <input name="txtPrivateKey" type="password" maxlength="25" id="txtPrivateKey"
                                           onblur='isPwd();'/>
                                    <input id="isPwdRight" name="isPwdRight" type="hidden"/>
                                </div>
                            </td>
                        </tr>
                    </table>
                    <table width="100%" cellspacing="0" cellpadding="0" border="0" align="center"
                           class="table table-border">
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
                            <c:if test="${not empty item.completeTime}">
                                <tr>
                                    <td>${item.name}</td>
                                    <td><tags:user userId="${item.assignee}"/></td>
                                    <td><fmt:formatDate value="${item.completeTime}" type="both"/></td>
                                    <td>${item.action}</td>
                                    <td>${item.auditDuration}</td>
                                </tr>
                                <c:if test="${item.action != '提交' && item.action != '重新申请'}">
                                    <tr style="border-top:0px hidden;">
                                        <td>批示内容</td>
                                        <td colspan="4">
                                            <pre>${item.comment}</pre>
                                        </td>
                                    </tr>
                                </c:if>
                            </c:if>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
            </div>
            <br>
        </section>
        <!-- end of main -->
    </div>
    <br/><br/><br/><br/>
    <div class="navbar navbar-default navbar-fixed-bottom">
        <div class="container-fluid">
            <div class="text-center" style="padding-top:8px;">
                <div class="text-center" style="padding-top:8px;">
                    <button id="complete" onclick="completeTask(1)" class="btn btn-default" type="button">同意</button>
                    <button id="backProcess" class="btn btn-default" onclick="completeTask(2)" type="button">驳回</button>
                    <button id="disagree" name="approval" class="btn btn-default" onclick="completeTask(0)"
                            type="button" value="不同意">不同意
                    </button>
                    <button type="button" class="btn btn-default" onclick="javascript:history.back();">返回</button>
                    <%-- <a href="${tenantPrefix}/rs/lllegalFreeze/enclosures?key=value">下载</a> --%>
                </div>

            </div>
        </div>
    </div>
</form>


</body>

</html>
