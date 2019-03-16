<%--

  User: wanghan
  Date: 2017\9\29 0029
  Time: 17:38

--%>
<%@page contentType="text/html;charset=UTF-8" %>
<%@include file="/taglibs.jsp" %>
<%pageContext.setAttribute("currentHeader", "version");%>
<%pageContext.setAttribute("currentMenu", "version");%>

<!doctype html>
<html>

<head>
    <%@include file="/common/meta.jsp" %>
    <title>麦联</title>
    <%@include file="/common/s3.jsp" %>
    <script type="text/javascript" src="${cdnPrefix}/bootbox/bootbox.min1.js"></script>
    <script type="text/javascript">
        $.validator.setDefaults({
            errorPlacement: function (error, element) {//error为错误提示对象，element为出错的组件对象
                if (element.parent().parent().hasClass("form-group"))
                    error.appendTo(element.parent().parent());
                else
                    error.appendTo(element.parent().parent().parent());
            },
            errorClass: 'validate-error'
        });
        function fnAttc() {
            var fileupload = document.getElementById("files1");
            //var historyFile = document.getElementById("historyFile");
            //alert($("#divShowImg").find("a").size());
            //return false;
            $("#versionInfoForm").submit();
            /* if (!$("#versionInfoForm").valid()) {
                return false;
            } else {
                if (fileupload.files && fileupload.files.length > 0) {
                    $("#versionInfoForm").submit();
                    return true;
                } else {
                    bootbox.alert({
                        message: '<p class="text-center"><i class="fa fa-spin fa-spinner"></i>请添加附件！</p>',
                        size: 'small'
                    });
                    return false;
                }
            } */
        }
    </script>
</head>


<body>
<%@include file="/header/version.jsp" %>
<div class="row-fluid">
    <%@include file="/menu/dict.jsp" %>
    <!-- start of main -->
    <section id="m-main" class="col-md-10" style="margin-top:65px;">

        <div class="panel panel-default">
            <div class="panel-heading">
                编辑
            </div>

            <div class="panel-body">

                <form id="versionInfoForm" method="post" class="form-horizontal" action="version-info-save.do"
                      enctype=multipart/form-data>
                    <input id="workTaskInfo_id" type="hidden" name="id" value="${versionInfo.id}">

                    <div class="form-group">
                        <label class="control-label col-md-2" for="version_code"><span style="color:red;"> * </span>版本号</label>
                        <div class="col-md-4">
                            <input id="version_code" type="text" name="versioncode"
                                   value="${versionInfo.versioncode}" size="40" class="form-control required">
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="control-label col-md-2" for="version_remarks">备注</label>
                        <div class="col-md-4">
                            <textarea id="version_remarks" type="text" name="remarks"
                                      value="${versionInfo.remarks}" class="form-control " maxlength="150"
                                      style="height: 140px">${versionInfo.remarks}</textarea>
                            <span style="color:gray;"> 请将字数限制在200字以内 </span>
                        </div>
                    </div>

                    <div class="row">
                        <div class="col-md-8">
                            <label class="control-label col-md-3 alignRight" name="fileName">历史附件:</label>
                            <div class="col-md-8 alignLeft">
                                <%@include file="/common/show_edit_file.jsp" %>
                            </div>
                        </div>
                    </div>

                    <div class="form-group">
                        <label class="control-label col-md-2" name="fileName">附件</label>
                        <div class="col-md-8">
                            <%@include file="/common/_uploadFileVersion.jsp" %>
                            <span style="color:gray;"> 请添加共小于200M的附件 </span>
                        </div>
                    </div>

                    <div class="form-group">
                        <div class="col-md-offset-2 col-md-10">
                            <button id="submitButton" type="button" class="btn btn-default a-submit" onclick="fnAttc()">
                                发布
                            </button>
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

