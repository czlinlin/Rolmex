<%@page contentType="text/html;charset=UTF-8"%>
<%@include file="/taglibs.jsp"%>
<%pageContext.setAttribute("currentHeader", "my");%>
<%pageContext.setAttribute("currentMenu", "my");%>
<%pageContext.setAttribute("currentMenuName", "index");%>
<%pageContext.setAttribute("currentChildMenu", "个人信息");%>
<!doctype html>
<html lang="en">
  <head>
    <%@include file="/common/meta.jsp"%>
    <title>麦联</title>
    <%@include file="/common/s3.jsp"%>
    <script type="text/javascript">
	$(function() {
	    $("#userForm").validate({
	        submitHandler: function(form) {
				bootbox.animate(false);
				var box = bootbox.dialog('<div class="progress progress-striped active" style="margin:0px;"><div class="bar" style="width: 100%;"></div></div>');
	            form.submit();
	        },
	        errorClass: 'validate-error'
	    });
	
	    $('#myTab a').click(function (e) {
			e.preventDefault();
			$(this).tab('show');
		});
	})
    </script>
  </head>
  <body>
    <%@include file="/header/my.jsp"%>

    <div class="row-fluid">
	  <%@include file="/menu/my.jsp"%>

	  <!-- start of main -->
      <section id="m-main" class="col-md-10" style="margin-top:65px;">

      <div class="panel panel-default">
        <div class="panel-heading">
		  维护个人信息
		</div>

		<div class="panel-body">

<form id="pimRemindForm" method="post" action="my-info-save.do" class="form-horizontal">
  <div class="form-group">
    <label class="control-label col-md-1" for="pimRemind_repeatType">账号</label>
	<div class="col-sm-5">
	  <label class="control-label">${accountInfo.username}</label>
    </div>
  </div>
  
  
  
  <c:forEach items="${dictInfo_otherName}" var="otherName">
	    <c:if test="${otherName.value == 1}">
	    
		     <div class="form-group">
			    <label class="control-label col-md-1" for="pimRemind_repeatType">姓名</label>
					<div class="col-sm-5">
					  <label class="control-label">${accountInfo.realName}</label>
				    </div>
		  	</div>
		     
		    <div class="form-group">
			    <label class="control-label col-md-1" for="pimRemind_repeatType">别名</label>   
				<div class="col-sm-5">
				
				<c:if test="${isResetAnotherName == 0}">
				  <input id="pimInfo_name" type="text" name="fullName" value="${accountInfo.displayName}" class="form-control required" minlength="2" maxlength="50">
   				 </c:if>
   				 
   				<c:if test="${isResetAnotherName == 1}">
   					<label class="control-label">${accountInfo.displayName}</label>
				</c:if> 
   				 
   				</div>
	  		</div>
        </c:if>
   </c:forEach>	
   
   
   <c:forEach items="${dictInfo_otherName}" var="otherName">
	    <c:if test="${otherName.value == 0}"> 
	    
		     <div class="form-group">
			    <label class="control-label col-md-1" for="pimRemind_repeatType">姓名</label>
					<div class="col-sm-5">
					  <label class="control-label">${accountInfo.displayName}</label>
				    </div>
		  	</div>
		</c:if>
   </c:forEach>	
   
   <div class="form-group">
    <label class="control-label col-md-1" for="pimRemind_description">岗位</label>
	<div class="col-sm-5">
			<c:if test="${not empty positions}">
				<c:forEach items="${positions}" var="item">
					<p>${item.position}<p/>
			    </c:forEach>
			</c:if>
		  <c:if test="${empty positions}">
		  		<label class="control-label">无</label>
		  </c:if>
    </div>
  </div>
   
   
  
  <div class="form-group">
    <label class="control-label col-md-1" for="pimRemind_description">邮箱</label>
	<div class="col-sm-5">
	  <input id="pimInfo_name" type="text" name="email" value="${personInfo.email}" class="form-control required" minlength="2" maxlength="50">
    </div>
  </div>
  <div class="form-group">
    <label class="control-label col-md-1" for="pimRemind_infoTime">电话</label>
	<div class="col-sm-5">
	  <input id="pimInfo_name" type="text" name="cellphone" value="${personInfo.cellphone}" class="form-control required" minlength="2" maxlength="50">
    </div>
  </div>
  <div class="form-group">
    <label class="control-label col-md-1" for="pimRemind_infoTime">（协同）用户名</label>
	<div class="col-sm-5">
	  <input id="oldSysUserName" type="text" name="oldSysUserName" value="${accountCredential.oldSysUserName}" class="form-control" minlength="2" maxlength="50">
    </div>
    <font color='color:gray'>（协同办公软件系统用户名）</font>
  </div>
  <div class="form-group">
    <div class="col-md-offset-1 col-md-11">
      <button type="button" class="btn btn-default a-submit" onclick="myInfoInputSave()"><spring:message code='core.input.save' text='保存'/></button>
	  &nbsp;
      <button type="button" class="btn btn-default" onclick="history.back();"><spring:message code='core.input.back' text='返回'/></button>
    </div>
  </div>
</form>
        </div>
      </div>

      </section>
	  <!-- end of main -->
	</div>

  </body>
  <script>
  function myInfoInputSave(){
		var anotherName = document.getElementById('pimInfo_name').value;
		$.ajax({
	        url: '${tenantPrefix}/rs/user/anotherNameRepet',      
	        datatype: "json",
	        data:{anotherName:encodeURI(anotherName)},
	        type:'get',
	        contentType:'application/x-www-form-urlencoded;charset=UTF-8',
	        success: function (e) {
	        	if (e == "1") {
	        		alert("别名已存在，请重新修改！"); 
	        	} else {
	        		$("#applyCode").val(e);
	    			$('#pimRemindForm').attr('action', '${tenantPrefix}/user/my-info-save.do');
	     			$('#pimRemindForm').submit();
	        	}	
	        },      
	        error: function(e){
	            alert("服务器请求失败,请重试");  
	        }      
	    }); 
	}
  </script>
</html>
