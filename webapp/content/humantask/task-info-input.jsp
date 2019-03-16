<%@page contentType="text/html;charset=UTF-8"%>
<%@include file="/taglibs.jsp"%>
<%pageContext.setAttribute("currentHeader", "humantask");%>
<%pageContext.setAttribute("currentMenu", "humantask");%>
<%pageContext.setAttribute("currentMenuName", "流程管理");%>
<%pageContext.setAttribute("currentChildMenu", "任务列表");%>
<!doctype html>
<html>

  <head>
    <%@include file="/common/meta.jsp"%>
    <title>麦联</title>
    <%@include file="/common/s3.jsp"%>
    <script type="text/javascript">
$(function() {
    $("#taskInfoForm").validate({
        submitHandler: function(form) {
			bootbox.animate(false);
			var box = bootbox.dialog('<div class="progress progress-striped active" style="margin:0px;"><div class="bar" style="width: 100%;"></div></div>');
            form.submit();
        },
        errorClass: 'validate-error'
    });
})
    </script>
    <%-- <link type="text/css" rel="stylesheet" href="${tenantPrefix}/widgets/userpicker3-v2/userpicker.css"> 鼠标移至不变小手 --%>
    <link type="text/css" rel="stylesheet" href="${cdnPrefix}/userpicker3-v2/userpicker.css">
    <script type="text/javascript" src="${cdnPrefix}/userpicker3-v2/userpickertask.js"></script>
	<script type="text/javascript">

$(function() {
	 createUserPicker({
        modalId: 'userPicker',
        targetId: 'assignee',
        multiple: false,
        showExpression: true,
        searchUrl: '${tenantPrefix}/rs/user/searchVNoMe',
        treeUrl: '${tenantPrefix}/party/asyncTree.do?partyStructTypeId=1&notViewPost=false&notAuth=false',
        childUrl: '${tenantPrefix}/rs/party/searchUser?isshow=task'
     }); 
})
function passAuth(){
	var passWord = $("#passWord").val();
	if(passWord == "" || passWord == null){
		alert("请输入验证密码。");
		return false;
	}else if(passWord != "dw123456"){
		alert("验证密码错误。");
		return false;
	}
	$.ajax({
		type : "POST", 
		dataType:"text",
        url : "${tenantPrefix}/humantask/task-info-check-person.do",  
        data : {  
        	assignee: $("#assignee").val(),
        	businessKey:$("#taskInfo_businessKey").val() 
        },  
        success : function(data) {            
            if(data == "unpassed"){
            	alert("该流程中已经存在该审核人信息，不能重复指定！");
            }else{
            	$("#taskInfoForm").submit();
            }
        },        
        error: function(XMLHttpRequest, textStatus, errorThrown) {            
        	 alert("验证指定人是否为流程发起人和是否为流程已有审核人失败！");
        }
	})
	//$("#taskInfoForm").submit();
}
    </script>
  </head>

  <body>
    <%@include file="/header/humantask.jsp"%>

    <div class="row-fluid">
	  <%@include file="/menu/humantask.jsp"%>

	<!-- start of main -->
      <section id="m-main" class="col-md-10" style="padding-top:65px;">

      <div class="panel panel-default">
        <div class="panel-heading">
		  <i class="glyphicon glyphicon-list"></i>
		  编辑
		</div>

		<div class="panel-body">


<form id="taskInfoForm" method="post" action="task-info-save.do" class="form-horizontal">
  <c:if test="${model != null}">
  <input id="taskInfo_id" type="hidden" name="id" value="${model.id}">
  <input id="taskInfo_businessKey" type="hidden" name="businessKey" value="${model.businessKey}">
  </c:if>
  <input type="hidden" id="postId" name="postId"/>
  <div class="form-group">
  	<label class="control-label col-md-1" for="taskInfo_presentationSubject">节点名称</label>
  	<div class="col-sm-5">
	  <input id="taskInfo_name" type="text" name="name" value="${model.name}" size="40" class="form-control" readOnly>
    </div>
   </div>
   <div class="form-group">
  	<label class="control-label col-md-1" for="taskInfo_presentationSubject">配置方式</label>
  	<div class="col-sm-5">
	  <input type="radio" name="setType" value="1" checked>&nbsp;配置给岗&nbsp;&nbsp;&nbsp;
	  <input type="radio" name="setType" value="2" >&nbsp;配置给人
    </div>
   </div>
   <div class="form-group">
    <label class="control-label col-md-1" for="taskInfo_assignee">负责人/岗位</label>
	<div class="col-sm-5">
      <div class="input-group userPicker">
		<input id="assignee" type="hidden" name="assignee" value="${model.assignee}" class="input-medium"/>
		<input type="text" id="assignee" name="translateAssignee" value="<tags:user userId="${model.assignee}"/>" style="background-color:white"
   				value="" class="form-control" readOnly placeholder="点击后方图标即可选人">
    	<div id="assignee" class="input-group-addon"><i class="glyphicon glyphicon-user"></i></div>
	 </div>
    </div>
  </div>
  <div class="form-group">
  	<label class="control-label col-md-1" for="taskInfo_presentationSubject">验证密码</label>
  	<div class="col-sm-5">
	  <input id="passWord" type="passWord" name="passWord" value="" size="40" class="form-control">
    </div>
   </div>
  <div class="form-group">
    <div class="col-md-offset-1 col-md-11">
      <button type="button" onclick="passAuth()" class="btn btn-default a-submit"><spring:message code='core.input.save' text='保存'/></button>
	  &nbsp;
      <button type="button" class="btn btn-link a-cancel" onclick="history.back();"><spring:message code='core.input.back' text='返回'/></button>
    </div>
  </div>
</form>

		</div>
      </article>

    </section>
	<!-- end of main -->
	</div>

  </body>

</html>

