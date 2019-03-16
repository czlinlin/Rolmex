<%@page contentType="text/html;charset=UTF-8" %>
<%@include file="/taglibs.jsp" %>
<%pageContext.setAttribute("currentHeader", "quit");%>
<%pageContext.setAttribute("currentMenu", "quit");%>
<!doctype html>
<html>

<head>
    <%@include file="/common/meta.jsp" %>
    <title><spring:message code="user.user.input.title" text="麦联"/></title>
    <%@include file="/common/s3.jsp" %>

    <link type="text/css" rel="stylesheet" href="${cdnPrefix}/orgpicker/orgpicker.css">
    <script type="text/javascript" src="${cdnPrefix}/orgpicker/orgpicker.js"></script>

    <script type="text/javascript">
        $(function () {

            createOrgPicker({
                modalId: 'orgPicker',
                showExpression: true,
                chkStyle: 'radio',
                searchUrl: '${tenantPrefix}/rs/user/search',
                treeUrl: '${tenantPrefix}/rs/party/treeNoPost?partyStructTypeId=1',
                childUrl: '${tenantPrefix}/rs/party/searchUser'
            });

            $("#userBaseForm").validate({
                submitHandler: function (form) {
                    bootbox.animate(false);
                    var box = bootbox.dialog('<div class="progress progress-striped active" style="margin:0px;text-align:center;"><div class="bar" style="width: 100%;text-align:center;">正在提交数据...</div></div>');
                    form.submit();
                },
                errorClass: 'validate-error',
                rules: {
                    username: {
                        remote: {
                            url: 'account-info-checkUsername.do',
                            data: {
                                <c:if test="${model != null}">
                                id: function () {
                                    return $('#userBase_id').val();
                                }
                                </c:if>
                            }
                        }
                    }
                },
                messages: {
                    username: {
                        remote: "<spring:message code='user.user.input.duplicate' text='存在重复账号'/>"
                    }
                }
            });
        })
    </script>
</head>

<body>


<div class="row-fluid">

	<c:if test="${not empty flashMessages}">
		<div id="m-success-message" style="display:none;">
		  <ul>
		  <c:forEach items="${flashMessages}" var="item">
		    <c:if test="${item != ''}">
		    	<li>${item}</li>
		    </c:if>
		  </c:forEach>
		  </ul>
		</div>
	 </c:if>
    <!-- start of main -->
    <section id="m-main" class="col-md-12" style="padding-top:3px;">

        <ul class="breadcrumb">
            <li><a href="person-info-quit-list.do">离职员工管理</a></li>
            <li class="active"></li>
        </ul>

        <div class="panel panel-default">
            <div class="panel-heading">
                <i class="glyphicon glyphicon-list"></i>
                职员复职
            </div>

            <div class="panel-body">

                <form id="userBaseForm" method="post" action="person-info-resume.do" class="form-horizontal">

                    <input id="userBase_id" type="hidden" name="id" value="${model.id}">

                    <div class="form-group">
                        <label class="control-label col-md-1">上级机构</label>
                        <div class="col-sm-5">
                            <div class="input-group orgPicker">
                                <input id="_task_name_key" type="hidden" name="departmentCode"
                                       value="${model.departmentCode}">
                                <input type="text" class="form-control required" id="departmentName"
                                       name="departmentName" placeholder="" value="${model.departmentName}"
                                       minlength="2" maxlength="50" readonly="readonly">
                                <div class="input-group-addon"><i class="glyphicon glyphicon-user"></i></div>
                            </div>
                        </div>
                    </div>

                    <div class="form-group">
                        <label class="control-label col-md-1" for="org_orgname"><spring:message
                                code="org.org.input.orgname" text="姓名"/></label>
                        <div class="col-sm-5">
                            <input type="text" class="form-control required" id="userBase_fullName" name="fullName"
                                   placeholder="" value="${model.fullName}">
                        </div>

                        <label class="control-label col-md-1" for="userBase_username"><spring:message
                                code="user.user.input.username" text="登录账号"/></label>
                        <div class="col-sm-5">
                            <input id="userBase_username" type="text" name="username" value="" size="40"
                                   class="form-control required" minlength="2" maxlength="50">
                        </div>
                    </div>

                    <div class="form-group">
                        <label class="control-label col-md-1" for="userBase_password"><spring:message
                                code="user.user.input.password" text="密码"/></label>
                        <div class="col-sm-5">
                            <input id="userBase_password" type="password" name="password" size="40"
                                   class="form-control required" maxlength="10">
                        </div>

                        <label class="control-label col-md-1" for="userBase_confirmPassword"><spring:message
                                code="user.user.input.confirmpassword" text="验证密码"/></label>
                        <div class="col-sm-5">
                            <input id="userBase_confirmPassword" type="password" name="confirmPassword" size="40"
                                   class="form-control required" maxlength="10" equalTo="#userBase_password">
                        </div>
                    </div>

                    <div class="form-group">
                        <label class="control-label col-md-1" for="org_orgname"><spring:message
                                code="org.org.input.orgname" text="工号"/></label>
                        <div class="col-sm-5">
                            <input type="text" class="form-control" id="employeeNo" name="employeeNo" placeholder=""
                                   value="${model.employeeNo}">
                        </div>
                        <label class="control-label col-md-1" for="org_orgname"><font
                                color="red">*</font><spring:message
                                code="org.org.input.orgname" text="职位"/> </label>
                        <div class="col-sm-5">
                            <select id="positionCode" class="form-control required" name="positionCode">
                                <option value="">请选择</option>
                                <c:forEach items='${dictInfos}' var="dictInfo">
                                    <option value="${dictInfo.value}"
                                            <c:if test='${model.positionCode == dictInfo.value}'>selected</c:if>>${dictInfo.name}</option>
                                </c:forEach>
                            </select>
                            <%--  <label class="control-label col-md-1" for="org_orgname"><spring:message code="org.org.input.orgname" text="职位"/></label>
                              <div class="col-sm-5">
                                <input type="text" class="form-control" id="positionName" name="positionName" placeholder="" value="${model.positionName}">
                              </div>--%>
                        </div>

                        <div class="form-group">
                            <label class="control-label col-md-1" for="orgInputUser_status">性别</label>
                            <div class="col-sm-5">
                                <label for="gender1" class="radio inline col-md-3">
                                    <input id="gender1" type="radio" name="gender" value="1"
                                           class="required" ${(model.gender == '1' || model.id == null) ? 'checked' : ''}>
                                    男
                                </label>
                                <label for="gender2" class="radio inline col-md-3">
                                    <input id="gender2" type="radio" name="gender" value="2"
                                           class="required" ${model.gender == '2' ? 'checked' : ''}>
                                    女
                                </label>
                                <label for="gender2" class="validate-error" generated="true"
                                       style="display:none;"></label>
                            </div>

                            <label class="control-label col-md-1" for="orgInputUser_status">是否兼职</label>
                            <div class="col-sm-5">
                                <label for="orgInputUser_status1" class="radio inline col-md-4">
                                    <input id="orgInputUser_status1" type="radio" name="jobStatus" value="1"
                                           class="required" ${(model.jobStatus == '1' || model.id == null) ? 'checked' : ''}>
                                    主职
                                </label>
                                <label for="orgInputUser_status2" class="radio inline col-md-4">
                                    <input id="orgInputUser_status2" type="radio" name="jobStatus" value="2"
                                           class="required" ${model.jobStatus == '2' ? 'checked' : ''}> 兼职
                                </label>
                                <label for="orgInputUser_status2" class="validate-error" generated="true"
                                       style="display:none;"></label>
                            </div>
                        </div>

                        <div class="form-group">
                            <label class="control-label col-md-1" for="userBase_email">邮箱</label>
                            <div class="col-sm-5">
                                <input id="userBase_email" type="text" name="email" value="${model.email}" size="40"
                                       class="form-control required" minlength="2" maxlength="50">
                            </div>

                            <label class="control-label col-md-1" for="userBase_cellphone">手机</label>
                            <div class="col-sm-5">
                                <input id="userBase_cellphone" type="text" name="cellphone" value="${model.cellphone}"
                                       size="40" class="form-control required" minlength="2" maxlength="50">
                            </div>
                        </div>

                        <div class="form-group">
                            <label class="control-label col-md-1" for="userBase_cellphone">联系地址</label>
                            <div class="col-sm-11">
                                <input id="userBase_address" type="text" name="address" value="${model.address}"
                                       size="40" class="form-control" minlength="2" maxlength="50">
                            </div>
                        </div>

                        <div class="form-group">
                            <label class="control-label col-md-1" for="userBase_status">启用</label>
                            <div class="col-sm-2">
                                <input id="userBase_stopFlag" type="checkbox" name="stopFlag"
                                       value="active" ${model.stopFlag == 'active' || model.stopFlag == null ? 'checked' : ''}>
                            </div>
                            <c:if test="${gestureSwitch != null}">
                                <label class="control-label col-md-1" for="userBase_status" style="display: none">启用手势</label>
                                <div class="col-sm-2" style="display: none">
                                    <input id="userBase_gestureSwitch" type="checkbox" name="gestureSwitch"
                                           value="open" ${gestureSwitch == 'open' || gestureSwitch == null ? 'checked' : ''}>
                                </div>
                            </c:if>

                            <label class="control-label col-md-4" for="orgInputUser_priority">排序</label>
                            <div class="col-sm-5">
                                <input id="userBase_priority" type="text" name="priority" value="${model.priority}"
                                       size="40" class="form-control required number" minlength="1" maxlength="50"
                                       autocomplete="off">
                            </div>
                        </div>

                        <div class="form-group">
                            <div class="col-md-offset-1 col-md-5">
                                <button id="submitButton" type="submit" class="btn btn-default a-submit"><spring:message
                                        code='core.input.save' text='复职'/></button>
                                <button type="button" class="btn btn-default"
                                        onclick="self.location=document.referrer;">返回
                                </button>
                            </div>
                        </div>
                </form>
            </div>
        </div>

    </section>
    <!-- end of main -->
</div>

</body>

</html>
