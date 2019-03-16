<%@page contentType="text/html;charset=UTF-8"%>
<%@include file="/taglibs.jsp"%>
<%pageContext.setAttribute("currentHeader", "person");%>
<%pageContext.setAttribute("currentMenu", "person");%>
<!doctype html>
<html lang="en">

  <head>
    <%@include file="/common/meta.jsp"%>
    <title>麦联</title>
    <%@include file="/common/s3.jsp"%>
    <script type="text/javascript">
		$(function(){
			setTimeout(function(){
					window.parent.$('#popWinClose').click();
				},1000);
		})
	</script>
  </head>
  <body>
 	<div style="margin-top:15px;width:90%;margin:0 auto;text-align:center;">
		<h2>${msg}</h2>
	</div>
  </body>

</html>

