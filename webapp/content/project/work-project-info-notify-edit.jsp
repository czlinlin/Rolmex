<%@page contentType="text/html;charset=UTF-8" %>
<%@include file="/taglibs.jsp" %>
<%pageContext.setAttribute("currentHeader", "pim");%>
<%pageContext.setAttribute("currentMenu", "workReport");%>
<!doctype html>
<html lang="en">

<head>
    <%@include file="/common/meta.jsp" %>
    <title>麦联</title>
    <%@include file="/common/s3.jsp" %>

    <link type="text/css" rel="stylesheet" href="${cdnPrefix}/userpicker3-v2/userpicker.css">
    <script type="text/javascript" src="${cdnPrefix}/userpicker3-v2/userpickercustom.js"></script>
    <script type="text/javascript" src="${cdnPrefix}/bootbox/bootbox.min1.js"></script>

    <script type="text/javascript">
        $(function () {
            //注册抄送人弹出
            createUserPicker({
                modalId: 'ccUserPicker',
                targetId: 'ccDiv',
                multiple: true,
                showExpression: true,
                searchUrl: '${tenantPrefix}/rs/user/searchVNoMe',
                treeUrl: '${tenantPrefix}/party/asyncTree.do?partyStructTypeId=1&notViewPost=true&notAuth=false',
                childUrl: '${tenantPrefix}/rs/party/searchUserNoMe'
            })
        })
        function fnCheck() {
            var btnPickerMany = $("#btnPickerMany").val();
            if ($.trim(btnPickerMany) == "") {
                bootbox.alert({
                    message: '<p class="text-center"><i class="fa fa-spin fa-spinner"></i>未添加知会人！</p>',
                    size: 'small'
                });
                return false;
            }
            $("#workReportInfoForm").submit();
            return true;
        }
    </script>
</head>

<body>
<%@include file="/header/navbar.jsp" %>

<div class="row-fluid">
    <%@include file="/menu/sidebar.jsp" %>
    <!-- start of main -->
    <section id="m-main" class="col-md-10" style="margin-top:65px;">

        <div class="panel panel-default">
            <div class="panel-heading">
                添加知会人
            </div>

            <div class="panel-body">

                <form id="workReportInfoForm" method="post" action="work-project-info-notify-edit-save.do"
                      class="form-horizontal" enctype=multipart/form-data>
                    <div class="form-group">
                        <label class="control-label col-md-2"></label>
                        <div class="col-md-8">
                            <font color='red'>提示：<font color='gray'>已经知会的人不在重复知会</font></font>
                            <input id="id" name="id" type="hidden" value="${id}"/>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="control-label col-md-2" style="padding:0">项目标题：</label>
                        <div class="col-md-8">
                            <div>${model.title}&nbsp;&nbsp;<%-- [<a target="blank" href="work-project-info-detail.do?id=${id}">查看详情</a>] --%></div>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="control-label col-md-2" style="padding:0">已发知会人：</label>
                        <div class="col-md-8">
                            <div>${notifynames}</div>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="control-label col-md-2" style="padding:0">选择知会人：</label>
                        <div class="col-md-8">
                            <div class="input-group userPicker">
                                <input id="btnPickerMany" type="hidden" name="selectIds" class="input-medium"
                                       value="">
                                <input type="text" id="userName" name="selectNames"
                                       value="" class="form-control " readOnly placeholder="点击后方图标即可选人">
                                <div id="ccDiv" class="input-group-addon"><i class="glyphicon glyphicon-user"></i></div>
                            </div>
                        </div>
                    </div>


                    <div class="form-group">
                        <div class="col-md-offset-2 col-md-10">
                            <button type="button" class="btn btn-default a-submit" onclick="fnCheck()">提交
                            </button>
                            &nbsp;
                            <button type="button" class="btn btn-link a-cancel"
                                    onclick="self.location=document.referrer;">
                                <spring:message code='core.input.back' text='返回'/>
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
