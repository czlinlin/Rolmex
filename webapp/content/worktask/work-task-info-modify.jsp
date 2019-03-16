<%--
  
  User: wanghan
  Date: 2017\8\30 0030
  Time: 14:04
 
--%>
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
            fnTaskSectionPickerTime(sectionJson)
        })
    </script>
    <script>
        //任务
        var fnTaskSectionPickerTime = function (eleTimes) {
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


                $(ele.begin + " input").attr("onclick", "WdatePicker({maxDate:'#F{$dp.$D(\\'" + end + "\\',{H:-1})||\\'2020-10-01\\'}',dateFmt:'yyyy-MM-dd HH:00:00'})");
                $(ele.end + " input").attr("onclick", "WdatePicker({minDate:'#F{$dp.$D(\\'" + begin + "\\',{H:+1})}',maxDate:'2020-10-01',dateFmt:'yyyy-MM-dd HH:00:00'})");
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
        function fnPoint() {
            bootbox.alert({
                message: '<p class="text-center"><i class="fa fa-spin fa-spinner"></i>进行中的任务不可以修改负责人</p>',
                size: 'small'
            });
            return false;
        }
    </script>
    <script type="text/javascript">
        $(document).ready(function () {
            // var editor = CKEDITOR.replace('workTaskInfo_content');

            //validate
            $('#workTaskInfoForm').validate({
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
            //获取当前时间(到小时)
            var d = new Date();
            var current_time = d.getFullYear() + "-" + (d.getMonth() + 1) + "-" + d.getDate() + " " + d.getHours() + ":" + "00";
            var plantime = $("#workTaskInfo_plantime").val();

            function CompareDate(d1, d2) {
                return ((new Date(d1.replace(/-/g, "\/"))) > (new Date(d2.replace(/-/g, "\/"))));
            }

            if (CompareDate(current_time, plantime) == true) {

                bootbox.alert({
                    message: '<p class="text-center"><i class="fa fa-spin fa-spinner"></i>该任务的计划完成时间已过期，请重新编辑后发布！</p>',
                    size: 'small'
                });
                return false;
            }
            //var text = CKEDITOR.instances.workTaskInfo_content.document.getBody().getText();
            //$("#workTaskInfo_content").val($.trim(text));
            if (!$("#workTaskInfoForm").valid()) {
                return false;
            }
            var loading = bootbox.dialog({
                message: '<p style="width:90%;margin:0 auto;text-align:center;">提交中...</p>',
                size: 'small',
                closeButton: false
            });

            $("#datastatus").val(datastatus);
            $("#workTaskInfoForm").submit();
            return true;
        }

    </script>
    <script>
        function keypress1() {
            document.getElementById("name").style.display = "block";
            var text1 = document.getElementById("WorkTaskInfo_title").value;
            var len = 0 + text1.length;
            if (text1.length >= 50) {
                document.getElementById("WorkTaskInfo_title").value = text1.substr(0, 50);
                len = 50;
            } else {
                len = text1.length;
            }
            var show = len + " / 50";
            document.getElementById("name").innerText = show;
        }
        function keypress2() {
            document.getElementById("pinglun").style.display = "block";
            var text1 = document.getElementById("workTaskInfo_content").value;
            var len;//记录已输入字数
            if (text1.length >= 5000) {
                document.getElementById("workTaskInfo_content").value = text1.substr(0, 5000);
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
                修改任务
            </div>

            <div class="panel-body">

                <form id="workTaskInfoForm" method="post" class="form-horizontal" action="work-task-info-save.do"
                      enctype=multipart/form-data>
                    <c:if test="${not empty model}">
                        <input id="workTaskInfo_id" type="hidden" name="id" value="${model.id}">
                    </c:if>
                    <input id="datastatus" type="hidden" name="datastatus">

                    <c:if test="${projectcode!= null}">
                        <div class="form-group">
                            <label class="control-label col-md-2" for="WorkTaskInfo_uppercode">项目名称</label>
                            <div class="col-md-8">
                                <input id="WorkProjectInfo_projectcode" type="hidden" name="projectcode"
                                       value="${projectcode}" size="40" class="form-control">
                                <a href="${ctx}/project/work-project-info-detail.do?id=${projectcode}"
                                >${projectcode_show}</a>
                            </div>
                        </div>
                    </c:if>

                    <c:if test="${uppercode_show!= null}">
                        <div class="form-group">
                            <label class="control-label col-md-2" for="WorkTaskInfo_uppercode">上级任务</label>
                            <div class="col-md-8">
                                <input id="WorkTaskInfo_uppercode" type="hidden" name="uppercode"
                                       value="${uppercode}" size="40" class="form-control">
                                <input id="uppercode_show" type="text" name="uppercode_show"
                                       value="${uppercode_show}" size="40" class="form-control" readonly>
                            </div>
                        </div>
                    </c:if>

                    <div class="form-group">
                        <label class="control-label col-md-2" for="WorkTaskInfo_title"><span
                                style="color:red;"> * </span>标题</label>
                        <div class="col-md-8">
                            <c:if test="${model.datastatus=='1'}">
                                <input id="WorkTaskInfo_title" type="text" name="title"
                                       value="${model.title}" size="40" class="form-control" readonly>
                                <span style="color: grey">已发布的任务不可修改标题</span>
                            </c:if>
                            <c:if test="${model.datastatus=='0'}">
                                <input id="WorkTaskInfo_title" type="text" name="title"
                                       value="${model.title}" size="40" maxlength="50" onkeyup="keypress1()" onblur="keypress1()"
                                       class="form-control required">
                                <label id="name" style="display: none" class="control-label col-md-12"></label>
                            </c:if>
                        </div>
                    </div>

                    <div class="form-group">
                        <label class="control-label col-md-2" for="workTaskInfo_content"><span
                                style="color:red;"> * </span>内容描述</label>
                        <div class="col-md-8">
                            <textarea id="workTaskInfo_content" name="content"
                                      class="form-control required" onkeyup="keypress2()" onblur="keypress2()"
                                      style="width:100%"
                                      rows="5">${model.content}</textarea>
                            <label id="pinglun" style="display: none" class="control-label col-md-12"></label>
                        </div>
                    </div>

                    <%-- <div class="button-group">
                         <label class="control-label col-md-2">* 任务类型</label>
                         <div class="col-md-8">
                             <input type="radio" name="tasktype" value="1" checked="checked">个人任务
                             <input type="radio" name="tasktype" value="2"
                                    <c:if test="${model.tasktype==2}">checked</c:if>>部门任务
                         </div>
                     </div>
                     <br><br>--%>
                    <div class="form-group">
                        <label class="control-label col-md-2"><span style="color:red;"> * </span>负责人</label>
                        <div class="col-sm-8">
                            <div class="input-group userPicker">
                                <input id="leaderId" type="hidden" name="leader"
                                       value="${model.leader}">
                                <input type="text" id="leaderName" name="leaderName" class="form-control required"
                                       value="<tags:user userId="${model.leader}"></tags:user>" minlength="2"
                                       maxlength="50" readOnly placeholder="点击后方图标即可选人">
                                <div
                                        <c:if test='${model.status!=1}'>id='leaderDiv' </c:if>
                                        class="input-group-addon"><i
                                        class="glyphicon glyphicon-user"  <c:if
                                        test="${model.status==1}"> onclick="fnPoint()"</c:if> ></i>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="form-group">
                        <label class="control-label col-md-2">抄送</label>
                        <div class="col-md-8">
                            <div class="input-group ">
                                <input id="btnPickerMany" type="hidden" name="ccnos" class="input-medium"
                                       value="${ccnos}">
                                <input type="text" id="userName" name="ccName"
                                       value="${ccnames}" class="form-control" readOnly placeholder="点击后方图标即可选人">
                                <div id="ccDiv" class="input-group-addon"><i class="glyphicon glyphicon-user"></i>
                                </div>
                            </div>
                            <span style="color:gray;"> 抄送人与负责人重复，将自动剔除 </span>
                        </div>
                    </div>

                    <div class="form-group">
                        <label class="control-label col-md-2" for="workTaskInfo_starttime"><span
                                style="color:red;"> * </span> 计划开始时间</label>
                        <div class="col-sm-8">
                            <div id="pickerStartTime" class="input-group date">
                                <input id="workTaskInfo_starttime" type="text" name="starttime"
                                       value="<fmt:formatDate value='${model.starttime}' type="both" pattern='yyyy-MM-dd HH:mm'/>"
                                       readonly style="background-color:white;cursor:default;"
                                       class="form-control required">
                                <span class="input-group-addon"><i class="glyphicon glyphicon-calendar"></i></span>
                            </div>
                        </div>
                    </div>

                    <div class="form-group">
                        <label class="control-label col-md-2" for="workTaskInfo_plantime"><span
                                style="color:red;"> * </span> 计划完成时间</label>
                        <div class="col-sm-8">
                            <div id="pickerEndTime" class="input-group date">
                                <input id="workTaskInfo_plantime" type="text" name="plantime"
                                       value="<fmt:formatDate value='${model.plantime}' type="both" pattern='yyyy-MM-dd HH:mm'/>"
                                       readonly
                                       style="background-color:white;cursor:default;" class="form-control required">
                                <span class="input-group-addon"><i class="glyphicon glyphicon-calendar"></i></span>
                            </div>
                        </div>
                    </div>


                    <div class="form-group">
                        <label class="control-label col-md-2" for="workTaskInfo_workload"><span
                                style="color:red;"> * </span> 工作量（人/时）</label>
                        <div class="col-md-8">
                            <input id="workTaskInfo_workload" type="text" name="workload"
                                   value="${model.workload}" size="40" class="form-control required number" min="1"
                                   max="999">
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
                            <c:if test="${model.datastatus==0}">
                                &nbsp;
                                <button type="button" class="btn btn-default a-submit" onclick="submitInfo(0)">保存草稿
                                </button>
                            </c:if>
                            &nbsp;
                            <c:if test="${model.id!=null}">
                            <button type="button" class="btn btn-default"
                                    onclick="self.location=document.referrer;">返回</c:if>
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
