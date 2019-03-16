<%@page contentType="text/html;charset=UTF-8"%>
<%@include file="/taglibs.jsp"%>
<%pageContext.setAttribute("currentHeader", "auth");%>
<%pageContext.setAttribute("currentMenu", "auth");%>
<%pageContext.setAttribute("currentChildMenu", "角色管理");%>
<!doctype html>
<html>

  <head>
    <%@include file="/common/meta.jsp"%>
    <title>麦联</title>
    <%@include file="/common/s3.jsp"%>
    <script type="text/javascript" src="${cdnPrefix}/bootbox/bootbox.min1.js"></script>
    <script type="text/javascript">
    	var dialog=null;
    	var dialogLoading=function(){
	    	dialog = bootbox.dialog({
	                message: '<p class="text-center"><img alt="提交中..." src="${cdnPrefix}/mossle/img/loading.gif" style="width:24px;height:24px;"/><i class="fa fa-spin fa-spinner"></i>提交中...</p>',
	                size: 'small',
	                closeButton: false
	            });
    	};
    	
    	var closeLoading=function(){
    		dialog.modal('hide');
    	}
        //$(".modal-content").css("margin-top","65px")
        //dialog.modal('hide');
    </script>	
  </head>

  <body>
    <%@include file="/header/navbar.jsp" %>

    <div class="row-fluid">
	  <%@include file="/menu/sidebar.jsp"%>

	<!-- start of main -->
      

      <%-- <div class="panel panel-default">
        <div class="panel-heading">
		  <i class="glyphicon glyphicon-list"></i>
		  编辑数据权限
		</div>

<form id="roleForm" method="post" class="form-horizontal">
  
  <div class="panel-body">
	<%@include file="/common/tree/orgdata.jsp" %>
  </div>
      
  <div class="form-group">
    <div class="col-md-5 col-md-offset-2">
      <button type = "button" id="button" class="btn btn-default" onclick ="save();"><spring:message code='core.input.save' text='保存'/></button>
	  &nbsp;
      <button type="button" onclick="history.back();" class="btn btn-link"><spring:message code='core.input.back' text='返回'/></button>
    </div>
  </div>
</form>--%>
	<section id="m-main" class="col-md-10">
			<iframe id="mainframe" name="mainframe" src="user-orgdata-list-i.do"
				width="100%" height="900px" frameborder="0"></iframe>
    </section> 
	<!-- end of main -->
</div>
  </body>

</html>

