<%@page contentType="text/html;charset=UTF-8"%>
<%@include file="/taglibs.jsp"%>
<!doctype html>
<html>

  <head>
    <%@include file="/common/meta.jsp"%>
    <title>麦联</title>
    <%@include file="/common/s3.jsp"%>
  </head>
  <body>
  	<%-- <%@include file="/header/bpm-workspace3.jsp" %> --%>
  	<%@include file="/header/portal.jsp"%>
  	<div data-height="300" class="container-fluid dashboard dashboard-draggable" id="dashboard" style="margin-top:70px;">
      <header></header>
      <section class="row">
        <div class="portal-col col-md-12">
			<div class="portlet" >
	          <div class="panel panel-default" id="panel">
	            <div class="panel-heading">
			      <i class="glyphicon glyphicon-list"></i>测试信息
	            </div>
	            <div class="panel-body">
			      	${test}
	            </div>
	          </div>
	        </div>
		</div>
      </section>
    </div>
  </body>
</html>