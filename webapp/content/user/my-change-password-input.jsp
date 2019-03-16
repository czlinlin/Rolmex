<%@page contentType="text/html;charset=UTF-8" %>
<%@include file="/taglibs.jsp" %>
<%pageContext.setAttribute("currentHeader", "my");%>
<%pageContext.setAttribute("currentMenu", "my");%>
<%pageContext.setAttribute("currentMenuName", "index");%>
<%pageContext.setAttribute("currentChildMenu", "修改密码");%>

<!doctype html>
<html>

<head>
    <%@include file="/common/meta.jsp" %>
    <title><spring:message code="user.user.changepassword.title" text="麦联"/></title>
    <%@include file="/common/s3.jsp" %>
    <script type="text/javascript">
        $(function () {
            $('#userForm').validate({
                submitHandler: function (form) {
                    bootbox.animate(false);
                    var box = bootbox.dialog('<div class="progress progress-striped active" style="margin:0px;"><div class="bar" style="width: 100%;"></div></div>');
                    form.submit();
                },
                errorClass: 'validate-error'
            });
        });
    </script>
    <script>
        var changpwd = "${isChangePwd}";
        $(function () {
            if (changpwd == "1") {
                fncheckSubmit();
            }
        })
        var isSubmit = false;

        var checkPwd = function (newid) {
            var reg1 = /\W+\D+/;
            var reg2 = /[0-9]/;
            var reg3 = /[a-zA-Z]/;
            var text = trim($(newid).val());
            $(newid).val(trim($(newid).val()));
            if (text.length >= 6) {
                if (reg1.test(text) && reg2.test(text) && reg3.test(text)) {
                    $(".safesure td").css("background-color", "white");
                    $("#td1").css("background-color", "#61D01C");
                    $("#td2").css("background-color", "#61D01C");
                    $("#td3").css("background-color", "#61D01C");
                    $("#safelv").html("强").css("color", "#61D01C");
                    isSubmit = true;
                }
                else if (reg1.test(text) || reg2.test(text) || reg3.test(text)) {
                    if (reg1.test(text) && reg2.test(text) ||
                        reg3.test(text) && reg2.test(text) ||
                        reg3.test(text) && reg1.test(text)) {
                        $(".safesure td").css("background-color", "white");
                        $("#td1").css("background-color", "#F9C14C");
                        $("#td2").css("background-color", "#F9C14C");
                        $("#safelv").html("中").css("color", "#F9C14C");
                        isSubmit = true;
                    }
                    else {
                        $(".safesure td").css("background-color", "white");
                        $("#td1").css("background-color", "#FD2F2F");
                        $("#safelv").html("弱").css("color", "#FD2F2F");
                    }

                }
                else if (text.length >= 16 && text.length <= 20) {
                    $(".safesure td").css("background-color", "white");
                    $("#td1").css("background-color", "#FD2F2F");
                    $("#safelv").html("弱").css("color", "#FD2F2F");
                }
            }
            else {
                $(".safesure td").css("background-color", "white");
                $("#safelv").html("");
            }
        }

        function trim(text) {
            return text.replace(/(^\s*)|(\s*$)/g, "");
        }

        function fncheckSubmit() {

            if (isSubmit == true) {
                $("#userForm").submit();
            } else {
                alert("密码强度太弱，请重新设置");
                return false;
            }
        }
    </script>
</head>

<body>
<%@include file="/header/my.jsp" %>

<div class="row-fluid">
    <%@include file="/menu/my.jsp" %>

    <!-- start of main -->
    <section id="m-main" class="col-md-10" style="margin-top:65px;">

        <article class="panel panel-default">
            <header class="panel-heading">
                <spring:message code="user.user.input.title" text="修改密码"/>
            </header>
            <div class="panel-body">

                <form id="userForm" method="post" action="my-change-password-save.do" class="form-horizontal">
                    <div id="divChangePwd">
                        <div class="form-group">
                            <label class="control-label col-md-1" for="oldPassword"><spring:message
                                    code="user.user.changepassword.old" text="原密码"/></label>
                            <div class="col-sm-5">
                                <input id="oldPassword" name="oldPassword" type="password" value=""
                                       class="form-control required" maxlength="20">
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="control-label col-md-1" for="oldPassword"><spring:message
                                    code="user.user.changepassword.new" text="新密码"/></label>
                            <div class="col-sm-5">
                                <input id="newPassword" name="newPassword" onkeyup="checkPwd(this)" type="password"
                                       value=""
                                       class="form-control required" maxlength="20">
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="control-label col-md-1" for="confirmPassword"><spring:message
                                    code="user.user.changepassword.confirm" text="确认密码"/></label>
                            <div class="col-sm-5">
                                <input id="confirmPassword" name="confirmPassword" type="password" value=""
                                       equalTo="#newPassword" class="form-control">
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="control-label col-md-1" for="oldPassword"><spring:message
                                    code="user.user.changepassword.new" text="安全性"/></label>
                            <div class="col-sm-5">
                                <table cellspacing="0" cellpadding="0" style="width:60%;" class="safetable">
                                    <tr class="safesure">
                                        <td style="border:1px solid #DEDEDE; height:18px; width: 80px" id="td1">
                                        </td>
                                        <td style="border:1px solid #DEDEDE; height:18px; width: 80px" id="td2">
                                        </td>
                                        <td style="border:1px solid #DEDEDE; height:18px; width: 80px" id="td3">
                                        </td>
                                        <td style="border:0px; height:18px; text-align:center;line-height:18px;"
                                            id="safelv"></td>
                                    </tr>
                                </table>
                            </div>
                        </div>
                    </div>
                    <div class="form-group">
                        <div class="col-md-offset-1 col-md-11">
                            <button type="button" onclick="fncheckSubmit()" class="btn btn-default">保存</button>
                            &nbsp;
                            <button type="button" onclick="history.back();" class="btn btn-default"><spring:message
                                    code='core.input.back' text='返回'/></button>
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
