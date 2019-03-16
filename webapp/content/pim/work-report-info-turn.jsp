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
        $(function () {
            //注册转发人弹出
            createUserPicker({
                modalId: 'ccUserPicker',
                targetId: 'ccDiv',
                multiple: true,
                showExpression: true,
                searchUrl: '${tenantPrefix}/rs/user/searchVNoMe',
                treeUrl: '${tenantPrefix}/party/asyncTree.do?partyStructTypeId=1&notViewPost=true&notAuth=false',
                childUrl: '${tenantPrefix}/rs/party/searchUserNoMe'
            })
            $.post(childUrl, {id: "${id}"});
        })
        
        function aBack() {
            var urlP = document.referrer;
            if (urlP.indexOf("temp") >= 0 || urlP.indexOf("list") >= 0) {
                window.location.assign(urlP);
            } else {
                window.history.back(-1);
            }
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
                汇报转发
            </div>

            <div class="panel-body">

                <form id="workReportInfoForm" method="post" action="work-report-info-turnsave.do"
                      class="form-horizontal" enctype=multipart/form-data>
                    <input id="reportid" name="reportid" type="hidden" value="${id}"></input>
                    <input id="turntype" name="turntype" type="hidden" value="${turntype}"></input>
                    <div class="form-group">
                        <label class="control-label col-md-2">汇报标题(接收)</label>
                        <div class="col-md-8">
                            <div>${model.title}&nbsp;&nbsp;<%-- [<a target="blank" href="work-report-info-look.do?id=${id}">查看详情</a>] --%></div>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="control-label col-md-2" for="workReportInfo_remarks">备注</label>
                        <div class="col-md-8">
                            <textarea id="workReportInfo_remarks" name="remarks" class="form-control "></textarea>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="control-label col-md-2">选择转发人</label>
                        <div class="col-md-8">
                            <div class="input-group userPicker">
                                <input id="btnPickerMany" type="hidden" name="selectIds" class="input-medium  required"
                                       value="">
                                <input type="text" id="userName" name="selectNames"
                                       value="" class="form-control required" readOnly placeholder="点击后方图标即可选人">
                                <div id="ccDiv" class="input-group-addon"><i class="glyphicon glyphicon-user"></i></div>
                            </div>
                            &nbsp;<span style="color: red">提示：不可转发给汇报发送人</span>
                        </div>
                    </div>
                    
                    
                    <div class="form-group">
                        <label class="control-label col-md-2">转发反馈内容</label>
                        <div class="col-md-8">
                            <div class="input-group userPicker">
                                <input type="radio"  name="isFeedBackForward" value="1" checked="checked"><label>&nbsp;是</label>
	                			<input type="radio" name="isFeedBackForward" value="2"><label>&nbsp;否</label>
                            </div>
                        </div>
                    </div>
                    

                    <div class="form-group">
                        <div class="col-md-offset-2 col-md-10">
                            <button type="button" class="btn btn-default a-submit" onclick="fnSubmit()">
                                <spring:message code='core.input.save' text='提交'/>
                            </button>
                            &nbsp;
                            <button type="button" class="btn btn-link a-cancel"
                                    onclick="aBack();">
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
<script type="text/javascript">
    var fnSubmit = function () {
        if (!$("#workReportInfoForm").valid()) return false;
        $("#workReportInfoForm").submit();
        return true;
    }
</script>
</body>

</html>
