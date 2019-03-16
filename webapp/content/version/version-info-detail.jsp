<%--
  
  User: wanghan
  Date: 2017\10\10 0010
  Time: 9:27
 
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
    <script type="text/javascript">

    </script>
</head>

<body>
<%@include file="/header/version.jsp" %>
<div class="row-fluid">
    <%@include file="/menu/version.jsp" %>
    <!-- start of main -->
    <section id="m-main" class="col-md-10" style="margin-top:65px;">

        <div class="panel panel-default">
            <div class="panel-heading">
                版本详情
            </div>

            <div class="panel-body">

                <form id="versionInfoForm" method="post" class="form-horizontal" action="version-info-save.do"
                      enctype=multipart/form-data>
                    <input id="workTaskInfo_id" type="hidden" name="id" value="${versionInfo.id}">

                    <div class="form-group">
                        <label class="control-label col-md-2">版本号：</label>
                        <div class="col-md-4">
                           ${versionInfo.versioncode}
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="control-label col-md-2">备注：</label>
                        <div class="col-md-4">
                                  ${versionInfo.remarks}
                        </div>
                    </div>

                    <div class="row">
                        <div class="col-md-8">
                            <label class="control-label col-md-3 alignRight" name="fileName">历史附件：</label>
                            <div class="col-md-8 alignLeft">
                                <%@include file="/common/show_file.jsp" %>
                            </div>
                        </div>
                    </div>


                    <div class="form-group">
                        <div class="col-md-offset-2 col-md-10">
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
