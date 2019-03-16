<%@page contentType="text/html;charset=UTF-8" %>
<%@include file="/taglibs.jsp" %>
<%pageContext.setAttribute("currentHeader", "pim");%>
<%pageContext.setAttribute("currentMenu", "msg");%>
<!doctype html>
<html lang="en">

<head>
    <%@include file="/common/meta.jsp" %>
    <title>麦联</title>
    <%@include file="/common/s3.jsp" %>

    <link href="${tenantPrefix}/cdn/jquery-ui/jquery-ui.min.css" rel="stylesheet">
    <script src="${tenantPrefix}/cdn/jquery-ui/jquery-ui.min.js"></script>
    <link rel='stylesheet' href='${tenantPrefix}/cdn/inputosaurus/inputosaurus.css' type='text/css' media='screen'/>
    <script src='${tenantPrefix}/cdn/inputosaurus/inputosaurus.js' type='text/javascript'></script>
    <script type="text/javascript">
        $(function () {
            $("#msg-infoForm").validate({
                submitHandler: function (form) {
                    bootbox.animate(false);
                    var box = bootbox.dialog('<div class="progress progress-striped active" style="margin:0px;"><div class="bar" style="width: 100%;"></div></div>');
                    form.submit();
                },
                errorClass: 'validate-error',
                'rules': {
                    'username': {
                        'required': true,
                        'remote': '${tenantPrefix}/rs/user/exists'
                    }
                }
            });


            $('#msgInfo_username').inputosaurus({
                width: '350px',
                autoCompleteSource: function (request, response) {
                    var term = request.term;
                    if (term.length > 2) {
                        $.get('${tenantPrefix}/rs/user/search', {
                            username: term
                        }, function (result) {
                            var data = [];
                            for (var i = 0; i < result.length; i++) {
                                data.push(result[i].username);
                            }
                            response(data);
                        });
                    }
                },
                activateFinalResult: true,
                change: function (ev) {
                    // $('#widget2_reflect').val(ev.target.value);
                }
            });
        })

        function submitInfo() {

            $("#msgInfoForm").submit();
            return true;
        }
    </script>
    <script>
        function a() {
            var urlP = document.referrer;
            //alert(urlP.indexOf("temp") >= 0 || urlP.indexOf("sent")>=0);
            if (urlP.indexOf("listRe") >= 0) {
                // alert("------返回并且刷新-----------" + urlP);
                window.location.assign(urlP);
            } else {
                // alert("---返回历史中的上一个网页---");
                window.history.back(-1);
            }
        }
    </script>
</head>

<body>
<%@include file="/header/pim3.jsp" %>

<div class="row-fluid">
    <%@include file="/menu/sidebar.jsp" %>

    <!-- start of main -->

    <section id="m-main" class="col-md-10" style="margin-top:65px;">

        <div class="panel panel-default">
            <div class="panel-heading">
                查看详情
            </div>

            <div class="panel-body">
                <form id="msgInfoForm" method="post" class="form-horizontal" action="msg-info-toview.do"
                      enctype=multipart/form-data>
                    <hr>

                    <p>
                    	<c:if test="${model.type == 0}">
                    		该申请已被撤回，无法审批和查看详情，请通过新消息跳转。
                    	</c:if>
                    	<c:if test="${model.type != 0}">
                    		${model.content}
                    	</c:if>
                        <c:if test="${viewStatus!='2' && changeStatus=='0'}">&nbsp; &nbsp; &nbsp; &nbsp;
                            <button type="button" class="btn btn-default a-submit" onclick="submitInfo()">详情</button>
                        </c:if> &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;
                        <button type="button" class="btn btn-default a-submit" onclick="a();">返回</button>

                        <c:if test="${changeStatus=='1'}"> <br><br><span style="color: red">已无权查看详情</span> </c:if>
                        <c:if test="${viewStatus=='2'}"> <br><br><span style="color: red">已被删除或不在有效期内，无法查看详情</span> </c:if>

                    </p>
                    <p> <span
                            class="label label-default"><fmt:formatDate value="${model.createTime}" type="both"/></span>
                    </p>
                    <br>
                    <input id="data" type="hidden" name="data" value="${model.data}">
                    <input id="msgInfoId" type="hidden" name="id" value="${model.id}">
                    <input id="msgInfoType" type="hidden" name="msgType" value="${model.msgType}">


                </form>
            </div>
        </div>

    </section>


    <!-- end of main -->
</div>

</body>

</html>
