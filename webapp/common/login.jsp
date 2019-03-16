<%@page contentType="text/html;charset=UTF-8"%>
<%@include file="/common/taglibs.jsp"%>
<!doctype html>
<html lang="en">

  <head>
    <%@include file="/common/meta.jsp"%>
    <title><spring:message code="core.login.title" text="麦联"/></title>
	<%@include file="/common/s3.jsp"%>
	<script type="text/javascript">
	$(function() {
		focusTenant();
	});
	
	function focusTenant() {
		if (document.f.tenant.value == '') {
			document.f.tenant.focus();
		} else {
			focusUsername();
		}
	}
	
	function focusUsername() {
		if (document.f.j_username.value == '') {
			document.f.j_username.focus();
		} else {
			document.f.j_password.focus();
		}
	}
	
	function login() {
		
	    $.ajax({  
	        type : "POST", 
	        async: false,
	        url : "http://localhost:8080/lemon/rs/android/login",  
	        data : {  
	        	username: "admin",
		    	password: "1",
		    	code: "122393421342412234",
		        type: "android",
		        name :"adf"
	        },  
	        success : function(data) {            
	            //TODO  
	        	// alert("second success"); 
	            alert(JSON.stringify(data));
	        },        
	        error: function(XMLHttpRequest, textStatus, errorThrown) {            
	        	 alert(XMLHttpRequest.status);
	             alert(XMLHttpRequest.readyState);
	             alert(textStatus);
	        },
	        complete: function() {            
	        	alert("complete");
	        }
		});  
    
	/*
    var jqxhr = $.post('http://localhost:8080/lemon/rs/android/login',{
    	username: "admin",
    	password: "1",
    	code: "122393421342412234",
        type: "android",
        name :""
    }, function() {
        alert("success");
      },"text")
      .success(function() { alert("second success"); })
      .error(function() { alert("error"); })
      .complete(function() { alert("complete"); });
    */
	}
	
	
	if(window!=top){
	 	top.location.href=location.href;
	}
	
	</script>
  </head>

  <body>

    <!-- start of header bar -->
<div class="navbar navbar-default navbar-fixed-top">
  <div class="container-fluid">
    <div class="navbar-header">
      <a class="navbar-brand" href="${tenantPrefix}">
	    <img src="${cdnPrefix}/logo.png" title="首页"  class="img-responsive pull-left" style="margin-top:-25px;margin-right:5px;">
	    <!-- Rolmex <sub><small>1.0.0</small></sub> -->
      </a>
    </div>

    <div class="navbar-collapse collapse">

      <ul class="nav navbar-nav navbar-right">
	    <li>
          <a href="?locale=zh_CN"><img src="${cdnPrefix}/flags/china.gif" height="20"></a>
		</li>
		<!-- 
	    <li>
          <a href="?locale=en_US"><img src="${cdnPrefix}/flags/us.gif" height="20"></a>
		</li>
		 -->
	  </ul>
	</div>
  </div>
</div>
    <!-- end of header bar -->

	<div class="row login" style="margin-top:200px;">
	  <div class="container-fluid">

	  <div class="col-md-4"></div>

	<!-- start of main -->
    <section class="col-md-4">
	  <div class="alert alert-danger" role="alert" ${param.error==true ? '' : 'style="display:none"'}>
        <span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true"></span>
        <strong><spring:message code="core.login.failure" text="登录失败"/></strong>
		&nbsp;
        ${sessionScope['SPRING_SECURITY_LAST_EXCEPTION'].message}
      </div>

      <article class="panel panel-default">
        <header class="panel-heading">
		  <spring:message code="core.login.title" text="登录"/>
		</header>

		<div class="panel-body">

<form id="userForm" name="f" method="post" action="${tenantPrefix}/j_spring_security_check" class="form-horizontal">
  <div class="form-group" style="display:none">
    <label class="col-md-2 control-label" for="tenant">租户</label>
	<div class="col-md-10">
      <input type='text' id="tenant" name='tenant' class="form-control" value="${empty sessionScope['SECURITY_LAST_TENANT'] ? cookie['SECURITY_LAST_TENANT'].value : sessionScope['SECURITY_LAST_TENANT']}">
      <span id="tenantText" class="glyphicon glyphicon-remove form-control-feedback" aria-hidden="true" style="right:15px;cursor:pointer;pointer-events:auto;display:none;"></span>
    </div>
  </div>
  <div class="form-group">
    <label class="col-md-2 control-label" for="username"><spring:message code="core.login.username" text="账号"/></label>
	<div class="col-md-10">
      <input type='text' id="username" name='j_username' class="form-control" value="${empty sessionScope['SECURITY_LAST_USERNAME'] ? cookie['SECURITY_LAST_USERNAME'].value : sessionScope['SECURITY_LAST_USERNAME']}" aria-describedby="inputSuccess3Status">
      <span id="usernameText" class="glyphicon glyphicon-remove form-control-feedback" aria-hidden="true" style="right:15px;cursor:pointer;pointer-events:auto;display:none;"></span>
    </div>
  </div>
  <div class="form-group">
    <label class="col-md-2 control-label" for="password"><spring:message code="core.login.password" text="密码"/></label>
	<div class="col-md-10">
      <input type='password' id="password" name='j_password' class="form-control" value=''>
    </div>
  </div>
  <c:if test="${sessionScope['captchaSessionToken']}">
  <div class="form-group" id="captchaArea">
    <label class="col-md-2 control-label" for="password" style="padding-left:0px;">验证码</label>
	<div class="col-md-2">
	  <img id="captchaPicture" src="captcha.jsp?_=<%=System.currentTimeMillis()%>" onclick="this.src='captcha.jsp?_=' + new Date().getTime()">
	</div>
	<div class="col-md-8">
      <input type='text' id="captcha" name='captcha' class="form-control" value=''>
    </div>
  </div>
  </c:if>
  <div class="form-group" style="display:none">
    <label class="col-md-2 control-label" for="username">&nbsp;</label>
	<div class="col-md-10">
      <input type='checkbox' name='_spring_security_remember_me' id="_spring_security_remember_me" />
	  <label for="_spring_security_remember_me">两周内自动登陆</label>
    </div>
  </div>
  <div class="form-group">
    <div class="col-md-5"></div>
    <div class="col-md-2">
      <input class="btn btn-default" name="submit" type="submit" value="<spring:message code='core.login.submit' text='登录'/>"/>
    </div>
    <div class="col-md-5"></div>
  </div>
</form>
        </div>
      </article>

      <div class="m-spacer"></div>
	</section>
	<!-- end of main -->

	  <div class="col-md-4"></div>
	  </div>
    </div>

  </body>
</html>
