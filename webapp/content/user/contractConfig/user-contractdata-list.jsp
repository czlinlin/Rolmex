<%@page contentType="text/html;charset=UTF-8"%>
<%@include file="/taglibs.jsp"%>
<%pageContext.setAttribute("currentHeader", "contract");%>
<%pageContext.setAttribute("currentMenu", "contract");%>
<!doctype html>
<html lang="en">
  <head>
    <%@include file="/common/meta.jsp"%>
    <title><spring:message code="dev.org.list.title" text="麦联"/></title>
    <%@include file="/common/s3.jsp"%>
    <script type="text/javascript" src="${cdnPrefix}/bootbox/bootbox.min1.js"></script>
    <script type="text/javascript">
		
    </script>
  </head>
  <body>
  	<%@include file="/header/navbar.jsp" %>
    <div class="row-fluid">
	  <%@include file="/menu/sidebar.jsp"%>
	  <section id="m-main" class="col-md-10">
			<iframe id="mainframe" name="mainframe" src="auth-contractdata-i.do"
				width="100%" height="900px" frameborder="0"></iframe>
      </section> 
    </div>

  </body>
</html>

