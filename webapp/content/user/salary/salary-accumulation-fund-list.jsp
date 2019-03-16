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
    	var dialogLoading=function(msg){
	    	dialog = bootbox.dialog({
	                message: '<p class="text-center"><img alt="'+msg+'" src="${cdnPrefix}/mossle/img/loading.gif" style="width:24px;height:24px;"/><i class="fa fa-spin fa-spinner"></i>'+msg+'</p>',
	                size: 'large',
	                closeButton: false
	            });
    	};
    	
    	var closeLoading=function(){
    		if(dialog!=null)
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

	<section id="m-main" class="col-md-10">
			<iframe id="mainframe" name="mainframe" src="salary-accumulation-fund-list-i.do"
				width="100%" height="1000px" frameborder="0"></iframe>
    </section> 
	<!-- end of main -->
</div>
  </body>

</html>

