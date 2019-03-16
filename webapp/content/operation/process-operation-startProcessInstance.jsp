<%@page contentType="text/html;charset=UTF-8"%>
<%@include file="/taglibs.jsp"%>
<%pageContext.setAttribute("currentHeader", "bpm-workspace");%>
<%pageContext.setAttribute("currentMenu", "bpm-process");%>
<!doctype html>
<html lang="en">
  <script type="text/javascript">
   
     function  aaa()
   	{
    	 setTimeout(redirect(),5000);
   	}    
     
     function  redirect()
     {   
     	$('#xform').attr('action', '${tenantPrefix}/bpm/workspace-listRunningProcessInstances.do');
		$('#xform').submit();
     
     }   
     
     
    </script>
	


  <head>
    <%@include file="/common/meta.jsp"%>
    <title>麦联</title>
    <%@include file="/common/s3.jsp"%>
  </head>

  <body onload="aaa()">
    <%@include file="/header/bpm-workspace3.jsp"%>
 	<form id="xform" method="post"   action="${tenantPrefix}/bpm/workspace-listRunningProcessInstances.do" class="xf-form" enctype="multipart/form-data">
	
    <div class="container">

	<section id="m-main" class="col-md-12" style="padding-top:65px;">
	 
	  <div class="alert alert-info" role="alert">
		<button type="button" class="close" data-dismiss="alert" style="margin-right:30px;">×</button>
		<strong>流程已发起</strong>
	  </div>

    </section>
	<!-- end of main -->
	</div>
	</form>
  </body>

</html>
