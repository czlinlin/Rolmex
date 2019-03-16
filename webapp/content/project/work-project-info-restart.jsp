<%@page contentType="text/html;charset=UTF-8" %>
<%@include file="/taglibs.jsp" %>
<%pageContext.setAttribute("currentHeader", "worktask");%>
<%pageContext.setAttribute("currentMenu", "worktask");%>
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
            //注册负责人弹出
            createUserPicker({
                modalId: 'leaderPicker',//这个 其实是弹出的窗口的div id
                targetId: 'leaderDiv', //这个是点击哪个 会触发弹出窗口
                showExpression: true,
                searchUrl: '${tenantPrefix}/rs/user/searchV',
                treeUrl: '${tenantPrefix}/party/asyncTree.do?partyStructTypeId=1&notViewPost=true&notAuth=false',
                childUrl: '${tenantPrefix}/rs/party/searchUser'
            });
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

            //设置段时间
            var sectionJson = [{"begin": "#pickerStartTime", "end": "#pickerEndTime"}];
            fnProjectSectionPickerTime(sectionJson)
        })
    </script>
    <script>
        //项目
        var fnProjectSectionPickerTime = function (eleTimes) {
            $(eleTimes).each(function (i, ele) {
                $(ele.begin + " span").remove();
                $(ele.end + " span").remove();

                $(ele.begin + " input").css("width", "835px");
                $(ele.end + " input").css("width", "835px");

                $(ele.begin + " input").addClass("Wdate");
                $(ele.end + " input").addClass("Wdate");

                var begin = $(ele.begin + " input").attr("id");

                var end = $(ele.end + " input").attr("id");


                if (begin == undefined)
                    $(ele.begin + " input").attr("id", "ipt" + ele.begin.replace("#", ""));
                if (end == undefined)
                    $(ele.end + " input").attr("id", "ipt" + ele.end.replace("#", ""))

                begin = $(ele.begin + " input").attr("id");
                end = $(ele.end + " input").attr("id");


                $(ele.begin + " input").attr("onclick", "WdatePicker({maxDate:'#F{$dp.$D(\\'" + end + "\\',{d:-1})||\\'2020-10-01\\'}',dateFmt:'yyyy-MM-dd '})");
                $(ele.end + " input").attr("onclick", "WdatePicker({minDate:'#F{$dp.$D(\\'" + begin + "\\',{d:+1})}',minDate:'%y-%M-{%d+1}',maxDate:'2020-10-01',dateFmt:'yyyy-MM-dd '})");
                //开始时间
                $(ele.begin + " .glyphicon-calendar").click(function () {
                    $(ele.begin + " input").click();
                })
                $(ele.end + " .glyphicon-calendar").click(function () {
                    $(ele.end + " input").click();
                })

            })
        }
    </script>
    <script type="text/javascript">
        $(document).ready(function () {
            /*       var editor = CKEDITOR.replace('workProjectInfo_content');*/

            //validate
            $('#workProjectInfoForm').validate({
                rules: {
                    content: {
                        required: true
                    }
                },
                ignore: '',
                errorPlacement: function (error, element) {//error为错误提示对象，element为出错的组件对象
                    if (element.parent().parent().hasClass("form-group"))
                        error.appendTo(element.parent().parent());
                    else
                        error.appendTo(element.parent().parent().parent());
                },
                errorClass: 'validate-error'

            });

            $(".selector").validate({
                showErrors: function (errorMap, errorList) {
                    this.defaultShowErrors();
                }
            });
        });
        function submitInfo(datastatus) {
            var d = new Date();
            var current_time = d.getFullYear() + "-" + (d.getMonth() + 1) + "-" + d.getDate();
            var plandate = $("#plandate").val();

            function CompareDate(d1, d2) {
                return ((new Date(d1.replace(/-/g, "\/"))) > (new Date(d2.replace(/-/g, "\/"))));
            }

            if (CompareDate(current_time, plandate) == true) {
                bootbox.alert({
                    message: '<p class="text-center"><i class="fa fa-spin fa-spinner"></i>该项目的计划完成时间已过期，请重新编辑后发布！</p>',
                    size: 'small'
                });
                return false;
            }
            /*    var text = CKEDITOR.instances.workProjectInfo_content.document.getBody().getText();

             $("#workProjectInfo_content").val($.trim(text));*/

            if (!$("#workProjectInfoForm").valid()) {
                return false;
            }
            var loading = bootbox.dialog({
                message: '<p style="width:90%;margin:0 auto;text-align:center;">提交中...</p>',
                size: 'small',
                closeButton: false
            });
            $("#datastatus").val(datastatus);
            $("#workProjectInfoForm").submit();
            return true;
        }
    </script>
    <script>
        function keypress1() {
            document.getElementById("name").style.display = "block";
            var text1 = document.getElementById("workProjectInfo_title").value;
            var len = 0 + text1.length;
            if (text1.length >= 50) {
                document.getElementById("workProjectInfo_title").value = text1.substr(0, 50);
                len = 50;
            } else {
                len = text1.length;
            }
            var show = len + " / 50";
            document.getElementById("name").innerText = show;
        }
        function keypress2() {
            document.getElementById("pinglun").style.display = "block";
            var text1 = document.getElementById("workProjectInfo_content").value;
            var len;//记录已输入字数
            if (text1.length >= 5000) {
                document.getElementById("workProjectInfo_content").value = text1.substr(0, 5000);
                len = 5000;
            }
            else {
                len = text1.length;
            }
            var show = len + " / 5000";
            document.getElementById("pinglun").innerText = show;
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
                重启项目
            </div>

            <div class="panel-body">

                <form id="workProjectInfoForm" method="post" class="form-horizontal" action="work-project-info-save.do"
                      enctype=multipart/form-data>

                    <input id="datastatus" type="hidden" name="datastatus">
                    <div class="form-group">
                        <label class="control-label col-md-2" for="workProjectInfo_title"><span
                                style="color:red;"> * </span> 项目名称</label>
                        <div class="col-md-8">
                            <input type="hidden" name="iptresart" value="restart">
                            <input type="hidden" name="iptoldid" value="${model.id}">
                            <input id="workProjectInfo_title" type="text" name="title"
                                   value="${model.title}" size="40" maxlength="50" onkeyup="keypress1()" onblur="keypress1()"
                                   class="form-control required">
                            <label id="name" style="display: none;" class="control-label col-md-12"></label>
                        </div>
                    </div>


                    <div class="form-group">
                        <label class="control-label col-md-2" for="workProjectInfo_content"><span
                                style="color:red;"> * </span>项目描述</label>
                        <div class="col-md-8">
                            <textarea id="workProjectInfo_content" name="content"
                                      class="form-control required" onkeyup="keypress2()" onblur="keypress2()"
                                      style="width:100%"
                                      rows="5">${model.content}</textarea>
                            <label id="pinglun" style="display: none;" class="control-label col-md-12"></label>
                        </div>
                    </div>

                    <div class="form-group">
                        <label class="control-label col-md-2"><span style="color:red;"> * </span> 负责人</label>
                        <div class="col-sm-8">
                            <div class="input-group userPicker">
                                <input id="leaderId" type="hidden" name="leader"
                                <%--value="${model.leader}"--%>>
                                <input type="text" id="leaderName" name="leaderName" class="form-control required"
                                       <%-- value="<tags:user userId="${model.leader}"></tags:user>" --%>minlength="2"
                                       maxlength="50" class="form-control" readOnly>
                                <div id='leaderDiv' class="input-group-addon"><i class="glyphicon glyphicon-user"></i>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="form-group">
                        <label class="control-label col-md-2">知会人</label>
                        <div class="col-md-8">
                            <div class="input-group userPicker">
                                <input id="btnPickerMany" type="hidden" name="notifynos" class="input-medium"
                                <%-- value="${notifynos}"--%>>
                                <input type="text" id="userName" name="notifynames"
                                <%-- value="${notifynames}"--%> class="form-control" readOnly>
                                <div id="ccDiv" class="input-group-addon"><i class="glyphicon glyphicon-user"></i></div>
                            </div>
                            <span style="color:gray;"> 如果知会人与负责人重复，将自动剔除 </span>
                        </div>
                    </div>

                    <div class="form-group">
                        <label class="control-label col-md-2" for="pickerStartTime"><span style="color:red;"> * </span>
                            计划开始日期</label>
                        <div class="col-sm-8">
                            <div id="pickerStartTime" class="input-group date">
                                <input type="text" name="startdate"
                                <%-- value="<fmt:formatDate value='${model.startdate}' type="both" pattern='yyyy-MM-dd '/>"--%>
                                       readonly style="background-color:white;cursor:default;"
                                       class="form-control required">
                                <span class="input-group-addon"><i class="glyphicon glyphicon-calendar"></i></span>
                            </div>
                        </div>
                    </div>

                    <div class="form-group">
                        <label class="control-label col-md-2" for="pickerEndTime"><span style="color:red;"> * </span>
                            计划完成日期</label>
                        <div class="col-sm-8">
                            <div id="pickerEndTime" class="input-group date">
                                <input id="plandate" type="text" name="plandate"
                                <%--  value="<fmt:formatDate value='${model.plandate}' type="both" pattern='yyyy-MM-dd '/>"--%>
                                       readonly
                                       style="background-color:white;cursor:default;" class="form-control required">
                                <span class="input-group-addon"><i class="glyphicon glyphicon-calendar"></i></span>
                            </div>
                        </div>
                    </div>

                    <div class="form-group">
                        <label class="control-label col-md-2" name="fileName">历史附件：</label>
                        <div class="col-md-8">
                            <%@include file="/common/show_edit_file.jsp" %>
                        </div>
                    </div>

                    <div class="form-group">
                        <label class="control-label col-md-2" name="fileName">添加附件：</label>
                        <div class="col-md-8">
                            <%@include file="/common/_uploadFile.jsp" %>
                            <span style="color:gray;"> 请添加共小于200M的附件 </span>
                        </div>
                    </div>
                    <div class="form-group">
                        <div class="col-md-offset-2 col-md-10">
                            <button type="button" class="btn btn-default a-submit" onclick="submitInfo(1)">发布
                            </button>
                            <c:if test="${model.status!=1 and model.status!=0 or model.datastatus==0}">
                                &nbsp;
                                <button type="button" class="btn btn-default a-submit" onclick="submitInfo(0)">保存草稿
                                </button>
                            </c:if>
                            &nbsp;
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

