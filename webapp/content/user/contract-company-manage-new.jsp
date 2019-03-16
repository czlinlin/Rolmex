<%@page contentType="text/html;charset=UTF-8"%>
<%@include file="/taglibs.jsp"%>
<%pageContext.setAttribute("currentHeader", "dict");%>
<%pageContext.setAttribute("currentMenu", "dict");%>
<%pageContext.setAttribute("currentMenuName", "人事管理");%>
<%pageContext.setAttribute("currentChildMenu", "工资");%>
<!doctype html>
<html>

  <head>
    <%@include file="/common/meta.jsp"%>
    <title>麦联</title>
    <%@include file="/common/s3.jsp"%>
    
    <link type="text/css" rel="stylesheet" href="${cdnPrefix}/orgpicker/orgpicker.css">
    <script type="text/javascript" src="${cdnPrefix}/orgpicker/orgpicker.js"></script>
    
    
    <script type="text/javascript">
$(function() {
	
	 createOrgPicker({
         modalId: 'orgPicker',
         showExpression: true,
         multiple: false,
         chkStyle: 'checkbox',
         searchUrl: '${tenantPrefix}/rs/user/search',
         treeUrl: '${tenantPrefix}/rs/party/treeNoAuth?partyStructTypeId=1',
         childUrl: '${tenantPrefix}/rs/party/searchUser'
     });
	
	
    $("#dict-typeForm").validate({
        submitHandler: function(form) {
			bootbox.animate(false);
			var box = bootbox.dialog('<div class="progress progress-striped active" style="margin:0px;"><div class="bar" style="width: 100%;"></div></div>');
            form.submit();
        },
        errorClass: 'validate-error'
    });

})


// function saveContractCompany() {
// 	//名称 为空  不允许提交
// 	if ( document.getElementById('contractCompanyName').value ==""){
// 		alert("请输入名称！");
// 		return false;
// 	}
// 	alert("666");
// 	//部门 为空  不允许提交
// 	/* if ( document.getElementById('departmentName').value ==""){
// 		alert("请选择部门！");
// 		return false;
// 	} */
	
// 	//表单 为空  不允许提交
// 	//if ( document.getElementById('formNames').value ==""){
// 	//	alert("请选择表单！");
// 	//	return false;
// 	//}

	
// 	$('#dict-typeForm').attr('action', '${tenantPrefix}/user/contract-company-manage-save.do');
// 	$('#dict-typeForm').submit();
// }


		 


    </script>
  </head>

  <body>
<%@include file="/header/navbar.jsp" %>

    <div class="row-fluid">
	 

	<!-- start of main -->
      <section id="m-main" class="col-md-12" style="padding-top:65px;">

      <div class="panel panel-default">
        <div class="panel-heading">
		  <i class="glyphicon glyphicon-list"></i>
		 新建
		</div>

		<div class="panel-body">


<form id="dict-typeForm" method="post" action="contract-company-manage-save.do" class="form-horizontal">

  <div class="form-group">
    <label class="control-label col-md-1" ><font color="red">*</font>单位名称</label>
	<div class="col-sm-5">
	  <input id="contractCompanyName" type="text"  name="contractCompanyName"  size="40"  class="form-control required">
    </div>
  </div>
  
  <div class="form-group">
    <label class="control-label col-md-1" >单位邮箱</label>
	<div class="col-sm-5">
	  <input id="companyEmail" type="text" name="companyEmail"  size="40"  class="form-control  email" >
    </div>
  </div>
  <div class="form-group">
    <label class="control-label col-md-1" >smtp服务器</label>
	<div class="col-sm-5">
	  <select name="smtpServerId" id="smtpServerId" class="form-control">
	  	<c:forEach items="${smtpServerList }" var="smtpServer">
	  		<option value="${smtpServer.id}">${smtpServer.name }</option>
	  	</c:forEach>
	  </select>
    </div>
  </div>
  <div class="form-group">
    <label class="control-label col-md-1" >pop服务器</label>
	<div class="col-sm-5">
	  <select name="popServerId" id="popServerId" class="form-control">
	  	<c:forEach items="${popServerList }" var="popServer">
	  		<option value="${popServer.id }">${popServer.name }</option>
	  	</c:forEach>
	  </select>
    </div>
  </div>
  <div class="form-group">
    <label class="control-label col-md-1" for="dictType_name">备注</label>
	<div class="col-sm-5">
	  <input id="remark" type="text" name="remark"  size="40"   class="form-control" >
    </div>
  </div>


  
  <div class="form-group">
    <label class="control-label col-md-1">是否启用</label>
	<div class="col-sm-5">
	 
                    <select name="isenable" id="isenable" class="form-control">
						<option value="是">是</option>
						<option value="否">否</option>
					</select>       
    </div>
  </div>
  <div class="form-group">
    <div class="col-sm-6">
      <!-- <button type="button" class="btn a-submit"><spring:message code='core.input.save' text='保存'/></button> -->
      <button id="confirm" class="btn a-submit" type="submit" onclick="saveContractCompany()">保存</button>
	  &nbsp;
      <button type="button" class="btn a-cancel" onclick="location.href='contract-company-manage-list-i.do'"><spring:message code='core.input.back' text='返回'/></button>
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

