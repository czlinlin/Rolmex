<%@page contentType="text/html;charset=UTF-8"%>
<%@include file="/taglibs.jsp"%>
<%pageContext.setAttribute("currentHeader", "my");%>
<%pageContext.setAttribute("currentMenu", "my");%>
<%pageContext.setAttribute("currentMenuName", "系统配置");%>
<%pageContext.setAttribute("currentChildMenu", "修改头像");%>
<!doctype html>
<html>

  <head>
    <%@include file="/common/meta.jsp"%>
    <title><spring:message code="user.user.input.title" text="麦联"/></title>
    <%@include file="/common/s3.jsp"%>

    <link rel="stylesheet" href="${cdnPrefix}/jcrop/css/jquery.Jcrop.min.css" type="text/css" media="screen" />
    <script type="text/javascript" src="${cdnPrefix}/jcrop/js/jquery.Jcrop.min.js"></script>

    <script type="text/javascript">
$(window).load(function () {
	function cropOnSelect(c) {
		$('#x1').val(c.x);
		$('#y1').val(c.y);
		$('#x2').val(c.x2);
		$('#y2').val(c.y2);
		$('#w').val(c.w);
		$('#h').val(c.h);
	}

    $('#target').Jcrop({
		aspectRatio: 1,
		onSelect: cropOnSelect
	},function(){
      jcrop_api = this;
    });
    jcrop_api.setSelect([0, 0, ${min}, ${min}]);
});
    </script>

  </head>

  <body>
    <%@include file="/header/my.jsp"%>

    <div class="row-fluid">
	  <%@include file="/menu/my.jsp"%>

	<!-- start of main -->
      <section id="m-main" class="col-md-10" style="margin-top:65px;">

      <article class="panel panel-default">
        <header class="panel-heading">
		  <spring:message code="user.user.input.title" text="修改头像"/>
		</header>
		<div class="panel-body">

<form id="userBaseForm" method="post" action="my-avatar-save.do" class="form-horizontal">
  <input id="x1" type="hidden" name="x1" value="">
  <input id="y1" type="hidden" name="y1" value="">
  <input id="x2" type="hidden" name="x2" value="">
  <input id="y2" type="hidden" name="y2" value="">
  <input id="w" type="hidden" name="w" value="">
  <input id="h" type="hidden" name="h" value="">

  <div class="control-group">
    <label class="control-label" for="userBase_avatar">头像</label>
	<div class="controls">
	  <div id="avatarImage">
		<img id="target" src="my-avatar-view.do" style="width:${w}px;height:${h}px;">
	  </div>
    </div>
  </div>

  <div class="control-group">
    <div class="controls">
      <button id="submitButton" class="btn btn-default a-submit">确认</button>
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