<%--

  User: wanghan
  Date: 2017\10\12 0012
  Time: 17:59

--%>
<%@page contentType="text/html;charset=UTF-8"%>
<%@include file="/taglibs.jsp"%>
<%pageContext.setAttribute("currentHeader", "person");%>
<%pageContext.setAttribute("currentMenu", "person");%>
<%pageContext.setAttribute("currentMenuName", "人力资源");%>

<!doctype html>
<html>

<head>
    <%@include file="/common/meta.jsp"%>
    <title><spring:message code="user.user.changepassword.title" text="麦联"/></title>
    <%@include file="/common/s3.jsp"%>
    <script type="text/javascript">
        $(function() {
            $('#userForm').validate({
                submitHandler: function(form) {
                    bootbox.animate(false);
                    var box = bootbox.dialog('<div class="progress progress-striped active" style="margin:0px;"><div class="bar" style="width: 100%;"></div></div>');
                    form.submit();
                },
                errorClass: 'validate-error'
            });
        });
    </script>
</head>

<body>
<%@include file="/header/org.jsp" %>
<div class="row-fluid">
    <%@include file="/menu/person.jsp" %>
    <!-- start of main -->
    <section id="m-main" class="col-md-10" style="margin-top:65px;">

        <article class="panel panel-default">
            <header class="panel-heading">
                <spring:message code="user.user.input.title" text="重置密码"/>
            </header>
            <div class="panel-body">

                <form id="userForm" method="post" action="person-info-resetsave.do" class="form-horizontal">

                    <div class="form-group">
                        <label class="control-label col-md-1" for="org_orgname"><spring:message code="org.org.input.orgname" text="用户姓名"/></label>
                        <div class="col-sm-5">
                            <input type="text" class="form-control " readonly id="userBase_fullName" name="fullName"
                                   placeholder="" value="${model.fullName}">
                        </div>
                        <%-- <label class="control-label col-md-1" ><spring:message code="user.user.changepassword.new" text="新密码"/></label>
                         <div class="col-sm-5">
                             <input id="newPassword" name="newPassword" type="password" value="" class="form-control required" maxlength="20">
                         </div>--%>
                    </div>
                    <div class="form-group">
                        <label class="control-label col-md-1" ><spring:message code="user.user.changepassword.new" text="新密码"/></label>
                        <div class="col-sm-5">
                            <input id="newPassword" name="newPassword" type="password" value="" class="form-control required" maxlength="20">
                        </div>
                    </div>

                    <div class="form-group">
                        <label class="control-label col-md-1" for="confirmPassword"><spring:message code="user.user.changepassword.confirm" text="确认密码"/></label>
                        <div class="col-sm-5">
                            <input id="confirmPassword" name="confirmPassword" type="password" value="" equalTo="#newPassword" class="form-control">
                        </div>
                    </div>
                    <div class="form-group">
                        <div class="col-md-offset-1 col-md-11">
                            <button id="submitButton" class="btn btn-default a-submit"><spring:message code='core.input.save' text='保存'/></button>
                            &nbsp;
                            <button type="button" class="btn btn-default"
                                    onclick="self.location=document.referrer;">返回
                            </button>
                        </div>
                    </div>
                </form>
            </div>
        </article>

        <div class="m-spacer"></div>

    </section>
    <!-- end of main -->
</div>

</body>

</html>
