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
    
     <style type="text/css">
        .alignRight {
            text-align: right;
            padding-right: 5px;
        }

        .col-md-8, .col-sm-8 {
            padding-left: 5px;
        }
    </style>

    <link type="text/css" rel="stylesheet" href="${cdnPrefix}/userpicker3-v2/userpicker.css">
<%--     <script type="text/javascript" src="${cdnPrefix}/bootbox/bootbox.min1.js"></script> --%>
<%--     <script type="text/javascript" src="${cdnPrefix}/userpicker3-v2/userpickerbycopy.js?v=1.0"></script> --%>
    <script type="text/javascript" src="${cdnPrefix}/worktask/worktask.js"></script>
<%-- 	<script type="text/javascript" src="${cdnPrefix}/popwindialog/popwin.js"></script> --%>

    <script type="text/javascript" src="${cdnPrefix}/userpicker3-v2/userpickercustom.js"></script>
    <script type="text/javascript" src="${cdnPrefix}/bootbox/bootbox.min1.js"></script>

    <script type="text/javascript">
        $(function () {
        	
        	 //注册接收人弹出
            createUserPicker({
                modalId: 'leaderPicker',//这个 其实是弹出的窗口的div id
                targetId: 'leaderDiv', //这个是点击哪个 会触发弹出窗口
                showExpression: true,
                multiple: true,
                searchUrl: '${tenantPrefix}/rs/user/searchVNoMe',
                treeUrl: '${tenantPrefix}/party/asyncTree.do?partyStructTypeId=1&notViewPost=true&notAuth=false',
                childUrl: '${tenantPrefix}/rs/party/searchUserNoMe'
            });
        	
        	
        	
        	
        	
//             //注册抄送人弹出
        
//             createUserPickerCopy({
//         		modalId: 'userPicker',
//         		showExpression: true,
//         		multiple: true,
//         		searchUrl: '${tenantPrefix}/rs/user/searchVNoMe',
//         		treeNoPostUrl: '${tenantPrefix}/party/asyncTree.do?partyStructTypeId=1&notViewPost=true',
//         		treeUrl: '${tenantPrefix}/party/asyncTree.do?partyStructTypeId=1&notViewPost=true',
//         		childUrl: '${tenantPrefix}/rs/party/searchUser',
//         		childPostUrl: '${tenantPrefix}/rs/party/searchPost'
//         	});
        })
       
        function fnCheck() {
            var btnPickerMany = $("#leaderId").val();
            if ($.trim(btnPickerMany) == "") {
                bootbox.alert({
                    message: '<p class="text-center"><i class="fa fa-spin fa-spinner"></i>未添加抄送人！</p>',
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
    <section id="m-main" class="col-md-10" style="margin-top:65px;"><!-- width: 100%; -->

        <div class="panel panel-default">
            <div class="panel-heading">
                添加抄送人
            </div>

            <div class="panel-body">

                <form id="workReportInfoForm" method="post" action="worktask-CC-input-save.do"
                      class="form-horizontal" enctype=multipart/form-data>
                    <div class="form-group">
                        <label class="control-label col-md-2"></label>
                        <div class="col-md-8">
                            <font color='red'>提示：<font color='gray'>已经抄送的人不再重复抄送</font></font>
                            <input id="id" name="id" type="hidden" value="${id}"/>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="control-label col-md-2" style="padding:0">主题：</label>
                        <div class="col-md-8">
                            <div>${workTaskInfo.title}&nbsp;&nbsp;</div>
                       		<input id="title" name="title" type="hidden" value="${workTaskInfo.title}"/>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="control-label col-md-2" style="padding:0">已发抄送人：</label>
                        <div class="col-md-8">
                            <div>${notifynames}</div>
                        </div>
                    </div>
<!--                     <div class="form-group"> -->
<!--                         <label class="control-label col-md-2" style="padding:0">选择抄送人：</label> -->
<!--                         <div class="col-md-8"> -->
<!--                          	<div class="input-group userPicker" style="display:block-inline;"> -->
<!-- 							  <input id="btnPickerMany" type="hidden" name="copyIds" class="input-medium" value=""> -->
<!-- 							  <input id="userName" type="text" name="copyNames" placeholder="点击后方图标即可选人" class="form-control" readonly> -->
<!-- 							  <div class="input-group-addon"><i class="glyphicon glyphicon-user"></i></div> -->
<!-- 						    </div> -->
<!--                         </div> -->
<!--                     </div> -->
                    
                    
                        <div class="form-group">
                        <label class="control-label col-md-2"><span style="color:red;"> * </span> 选择抄送人</label>
                        <div class="col-md-8">
                            <div class="input-group userPicker">
                                <input id="leaderId" type="hidden" name="sendee"
                                       value="">

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
                        <div class="col-md-offset-2 col-md-10">
                            <button type="button" class="btn btn-default a-submit" onclick="fnCheck()">提交
                            </button>
                            &nbsp;
							<button type="button" class="btn btn-link a-cancel"
                                    onclick="self.location=document.referrer;">
                                <spring:message code='core.input.back' text='返回'/>
                            </button>                        </div>
                    </div>
                </form>
            </div>
        </div>

    </section>
    <!-- end of main -->
</div>

</body>

</html>
