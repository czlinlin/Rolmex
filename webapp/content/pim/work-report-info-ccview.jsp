<%--
  
  User: wanghan
  Date: 2017\8\22 0022
  Time: 14:41
 
--%>
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

</head>
<body>

<%@include file="/header/navbar.jsp"%>

<div class="row-fluid">
    <%@include file="/menu/sidebar.jsp"%>
    <!-- start of main -->
    <section id="m-main" class="col-md-10" style="margin-top:65px;">

        <div class="panel panel-default">
            <div class="panel-heading">
                查看汇报
            </div>

            <div class="panel-body">

                <form id="workReportInfoForm" method="post" class="form-horizontal" enctype=multipart/form-data>
                    <c:if test="${not empty model}">
                        <input id="workReportInfo_id" type="hidden" name="id" value="${model.id}">
                    </c:if>

                    <div class="form-group">
                        <label class="control-label col-md-2" for="WorkReportInfo_title">* 标题</label>
                        <div class="col-md-8">
                            <input id="WorkReportInfo_title" type="text" name="title"
                                   value="${model.title}" size="40" class="form-control" readonly>
                        </div>
                    </div>

                    <div class="button-group">
                        <label class="control-label col-md-2">汇报类型</label>
                        <div class="col-md-8">
                            <input type="radio" name="type" value="1" checked="checked" disabled>周报
                            <input type="radio" name="type" value="2"
                                   <c:if test="${model.type==2}">checked</c:if> disabled>月报
                            <input type="radio" name="type" value="3"
                                   <c:if test="${model.type==3}">checked</c:if> disabled>年报
                            <input type="radio" name="type" value="4"
                                   <c:if test="${model.type==4}">checked</c:if> disabled>其他
                        </div>
                    </div>
                    <br><br>

                    <div class="form-group">
                        <label class="control-label col-md-2">* 已完成工作</label>
                        <div class="col-md-8">
                            <div class="form-control">${model.completed}</div>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="control-label col-md-2">进行中工作</label>
                        <div class="col-md-8">
                            <div class="form-control"> ${model.dealing}</div>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="control-label col-md-2">需协调工作</label>
                        <div class="col-md-8">
                            <div class="form-control">${model.coordinate}</div>

                        </div>
                    </div>
                    <div class="form-group">
                        <label class="control-label col-md-2">问题</label>
                        <div class="col-md-8">
                            <div class="form-control"> ${model.coordinate}</div>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="control-label col-md-2">备注</label>
                        <div class="col-md-8">
                            <div class="form-control"> ${model.remarks}</div>

                        </div>
                    </div>

                    <div class="form-group">
                        <label class="control-label col-md-2" name="fileName">附件</label>

                        <div class="col-md-8">
                            <input class="form-control" value="${filename}" readonly>

                            <c:if test="${model.showAttachment==1}"> <br>
                                <a href="work-report-info-download.do?id=${model.id}">下载附件</a> <br></c:if>
                        </div>


                    </div>

                    <div class="form-group">
                        <label class="control-label col-md-2">* 接收人</label>
                        <div class="col-md-8">
                            <div class="input-group userPicker">
                                <input id="leaderId" type="hidden" name="sendee"
                                       value="${model.sendee}">
                                <input type="text" id="leaderName" name="sendeeName" class="form-control required"
                                       minlength="2" maxlength="50" class="form-control"
                                       value="<tags:user userId="${model.sendee}"></tags:user>"
                                       readOnly>
                                <div id='leaderDiv' class="input-group-addon"><i class="glyphicon glyphicon-user"></i>
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
                                       value="${ccnames}" class="form-control" readOnly>
                                <div id="ccDiv" class="input-group-addon"><i class="glyphicon glyphicon-user"></i></div>
                            </div>
                        </div>
                    </div>

                    <div class="form-group">
                        <label class="control-label col-md-2" for="WorkReportInfo_tome">转发给我</label>
                        <div class="col-md-8">
                            <input id="WorkReportInfo_tome" type="text" name="_forwarder"
                                   value="${forwarders}" size="40" class="form-control" readonly>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="control-label col-md-2" for="WorkReportInfo_foryou">转发至</label>
                        <div class="col-md-8">
                            <input id="WorkReportInfo_foryou" type="text" name="sendee"
                                   value="${sendees}" size="40" class="form-control" readonly>
                        </div>
                    </div>

                    <div class="form-group">
                        <div class="col-md-offset-2 col-md-10">
                            <button type="button" class="btn btn-link a-cancel"
                                    onclick="window.location.href='work-report-info-cctome.do'">返回
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

