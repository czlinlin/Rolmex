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
            //注册接收人弹出
            createUserPicker({
                modalId: 'leaderPicker',//这个 其实是弹出的窗口的div id
                targetId: 'leaderDiv', //这个是点击哪个 会触发弹出窗口
                showExpression: true,
                searchUrl: '${tenantPrefix}/rs/user/searchVNoMe',
                treeUrl: '${tenantPrefix}/party/asyncTree.do?partyStructTypeId=1&notViewPost=true&notAuth=false',
                childUrl: '${tenantPrefix}/rs/party/searchUserNoMe'
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
        })
    </script>

    <script type="text/javascript">


        $(document).ready(function () {
            var editor1 = CKEDITOR.replace('workReportInfo_completed');
            var editor2 = CKEDITOR.replace('workReportInfo_dealing');
            var editor3 = CKEDITOR.replace('workReportInfo_coordinate');
            var editor4 = CKEDITOR.replace('workReportInfo_problems');


            var typeValue = $("input[name='type']:checked").val();
            if (typeValue == 4) {
                divb();
            } else {
                diva();
            }
        });

        function submitInfo(datastatus) {
            var text1 = CKEDITOR.instances.workReportInfo_problems.document.getBody().getText();
            var text2 = CKEDITOR.instances.workReportInfo_completed.document.getBody().getText();
            var text3 = CKEDITOR.instances.workReportInfo_dealing.document.getBody().getText();
            $("#workReportInfo_problems").val($.trim(text1));
            $("#workReportInfo_dealing").val($.trim(text2));
            if (!$("#workReportInfoForm").valid()) {
                return false;
            }
            if ($.trim(text2) == "" && $.trim(text3) == "") {
                bootbox.alert({
                    message: '<p class="text-center"><i class="fa fa-spin fa-spinner"></i>请输入进行中或已完成工作！</p>',
                    size: 'small'
                });
                return false;
            }
            if (filesizes > 209715200) {
                bootbox.alert({
                    message: '<p class="text-center"><i class="fa fa-spin fa-spinner"></i>附件大小已经超过200M！</p>',
                    size: 'small'
                });

                return false;
            }
            var loading = bootbox.dialog({
                message: '<p style="width:90%;margin:0 auto;text-align:center;">提交中...</p>',
                size: 'small',
                closeButton: false
            });
            $("#datastatus").val(datastatus);
            $("#workReportInfoForm").submit();
            return true;
        }

        function diva() {
            document.getElementById("div1").style.display = "none";
            document.getElementById("div2").style.display = "block";
            document.getElementById("div3").style.display = "block";
            document.getElementById("div4").style.display = "block";
            document.getElementById("div5").style.display = "block";
        }
        
        //专项
        function divb() {
        	$('#workReportInfoForm').validate({
                rules: {
                    problems: {
                        required: true
                    }
                },
                ignore: "",
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
            document.getElementById("div1").style.display = "block";
            document.getElementById("div2").style.display = "none";
            document.getElementById("div3").style.display = "none";
            document.getElementById("div4").style.display = "none";
            document.getElementById("div5").style.display = "none";
           
           $("#workReportInfo_problems").addClass("required");
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
                修改
            </div>

            <div class="panel-body">

                <form id="workReportInfoForm" method="post" class="form-horizontal" action="work-report-info-save.do"
                      enctype=multipart/form-data>
                    <c:if test="${not empty model}">
                        <input id="workReportInfo_id" type="hidden" name="id" value="${model.id}">
                    </c:if>
                    <input id="datastatus" type="hidden" name="datastatus">

                    <div class="form-group">
                        <label class="control-label col-md-2" for="WorkReportInfo_title"><span
                                style="color:red;"> * </span>标题
                        </label>
                        <div class="col-md-8">
                            <input id="WorkReportInfo_title" type="text" name="title"
                                   value="${model.title}" size="40" class="form-control required" maxlength="50">
                        </div>
                    </div>

                    <div class="button-group">
                        <label class="control-label col-md-2"><span
                                style="color:red;"> * </span>汇报类型</label>
                        <div class="col-md-8">
                            <div style="display: none;">
                            <input type="radio" name="type" value="1" checked="checked"readonly>周报
                            <input type="radio" name="type" value="2"
                                   <c:if test="${model.type==2}">checked</c:if> readonly>月报
                            <input type="radio" name="type" value="3"
                                   <c:if test="${model.type==3}">checked</c:if>  readonly>年报
                            <input type="radio" name="type" value="4"
                                   <c:if test="${model.type==4}">checked</c:if>>专项</div>
                            <c:if test='${model.type=="1"}'>周报</c:if>
                            <c:if test='${model.type=="2"}'>月报</c:if>
                            <c:if test='${model.type=="3"}'>年报</c:if>
                            <c:if test='${model.type=="4"}'>专项</c:if>
                        </div>
                    </div>
                    <br><br>

                    <div class="form-group" id="div1" style="display: none">
                        <label class="control-label col-md-2" for="workReportInfo_problems"><span
                                style="color:red;"> * </span> 内容</label>
                        <div class="col-md-8">
                            <textarea id="workReportInfo_problems" name="problems"
                                      class="ckeditor">${model.problems}</textarea>
                        </div>
                    </div>
                    <br><br>
                    <div class="form-group" id="div3">
                        <label class="control-label col-md-2" for="workReportInfo_dealing"><%--<span
                                style="color:red;"> * </span>--%>进行中工作</label>
                        <div class="col-md-8">
                            <textarea id="workReportInfo_dealing" name="dealing"
                                      class="ckeditor">${model.dealing}</textarea>
                        </div>
                    </div>

                    <div class="form-group" id="div2">
                        <label class="control-label col-md-2" for="workReportInfo_completed">
                            已完成工作</label>
                        <div class="col-md-8">
                            <textarea id="workReportInfo_completed" name="completed"
                                      class="form-control">${model.completed}</textarea>
                        </div>
                    </div>


                    <div class="form-group" id="div4">
                        <label class="control-label col-md-2" for="workReportInfo_coordinate">需协调工作</label>
                        <div class="col-md-8">
                            <textarea id="workReportInfo_coordinate" name="coordinate"
                                      class="form-control ">${model.coordinate}</textarea>
                        </div>
                    </div>

                    <div class="form-group" id="div5">
                        <label class="control-label col-md-2" for="workReportInfo_remarks">备注</label>
                        <div class="col-md-8">
                            <textarea id="workReportInfo_remarks" name="remarks"
                                      class="form-control " maxlength="2000">${model.remarks}</textarea>
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
                        <label class="control-label col-md-2"><span
                                style="color:red;"> * </span> 接收人</label>
                        <div class="col-md-8">
                            <div class="input-group userPicker">
                                <input id="leaderId" type="hidden" name="sendee"
                                       value="${model.sendee}">
                                <input type="text" id="leaderName" name="sendeeName" class="form-control required"
                                       minlength="2" maxlength="50" class="form-control"
                                       value="<tags:user userId="${model.sendee}"></tags:user>"
                                       readOnly placeholder="点击后方图标即可选人">
                                <div id='leaderDiv' class="input-group-addon "><i class="glyphicon glyphicon-user"></i>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="control-label col-md-2">抄送</label>
                        <div class="col-md-8">
                            <div class="input-group userPicker">
                                <input id="btnPickerMany" type="hidden" name="ccnos" class="input-medium"
                                       value="${ccnos}">
                                <input type="text" id="userName" name="ccName"
                                       value="${ccnames}" class="form-control" readOnly placeholder="点击后方图标即可选人">
                                <div id="ccDiv" class="input-group-addon"><i class="glyphicon glyphicon-user"></i></div>
                            </div>
                        </div>
                    </div>
                    <c:if test="${reportAttr.ccPreSettingId!=0}">
                    	<div class="form-group">
                        	<label class="control-label col-md-2">抄送条线</label>
                        <div class="col-md-8">
                        	<select id="select_cc_presetting" class="form-control" name="preSettingId" onchange="showCCPath('opsition')">
	                            <c:forEach items="${ccPresetting}" var="item">
	                				<option value="${item.id}" ${reportAttr.ccPreSettingId==item.id?'selected':''}>${item.title}</option>
	                			</c:forEach>
                			</select>
                        </div>
                    </div>
                    </c:if>
                    <input type="hidden" name="iptStartPosition" value="${reportAttr.positionId}"/>

                    <div class="form-group">
                        <div class="col-md-offset-2 col-md-10">

                            <button type="button" class="btn btn-default a-submit" onclick="submitInfo(1)">提交
                            </button>
                            &nbsp;
                            <c:if test="${model.datastatus ==0 or model.datastatus ==null}">
                                <button type="button" class="btn btn-default a-submit" onclick="submitInfo(0)">保存草稿
                                </button>
                            </c:if>
                            &nbsp;<c:if test="${model.id!=null}">
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
