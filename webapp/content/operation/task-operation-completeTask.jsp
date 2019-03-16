<%@page contentType="text/html;charset=UTF-8" %>
<%@include file="/taglibs.jsp" %>
<%pageContext.setAttribute("currentHeader", "bpm-workspace");%>
<%pageContext.setAttribute("currentMenu", "bpm-process");%>
<!doctype html>
<html lang="en">

<head>
    <%@include file="/common/meta.jsp" %>
    <title>麦联</title>
    <%@include file="/common/s3.jsp" %>

    <script type="text/javascript" src="${cdnPrefix}/bootbox/bootbox.min1.js"></script>
    <script type="text/javascript">
        function bbb() {
            var dialog = bootbox.dialog({
                message: '<p class="text-center"><i class="fa fa-spin fa-spinner"></i>正在加载...</p>',
                size: 'small',
                closeButton: false
            });
        }
        function aaa() {
            $('#xform').attr('action', '${tenantPrefix}/humantask/workspace-personalTasks.do');
            $('#xform').submit();
            setTimeout(5000);
        }
    </script>
</head>

<body onload="aaa()">
<%@include file="/header/bpm-workspace3.jsp" %>
<form id="xform" method="post" action="${tenantPrefix}/humantask/workspace-personalTasks.do" class="xf-form"
      enctype="multipart/form-data">

    <div class="container">

        <!-- start of main -->
        <section id="m-main" class="col-md-12" style="padding-top:65px;">

            <div class="alert alert-info" role="alert">
                <button type="button" class="close" data-dismiss="alert" style="margin-right:30px;" onclick="bbb()">×</button>
                <strong>操作成功</strong>
            </div>

        </section>
        <!-- end of main -->
    </div>
</form>
</body>

</html>
