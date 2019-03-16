<%@page contentType="text/html;charset=UTF-8"%>
<%@include file="/taglibs.jsp"%>
<%pageContext.setAttribute("currentHeader", "tenant");%>
<%pageContext.setAttribute("currentMenu", "tenant");%>
<!doctype html>
<html lang="en">

  <head>
    <%@include file="/common/meta.jsp"%>
    <title><spring:message code="tenant-info.tenant-info.input.title" text="麦联"/></title>
    <%@include file="/common/s.jsp"%>
    <script type="text/javascript">
$(function() {
    $("#tenant-infoForm").validate({
        submitHandler: function(form) {
			bootbox.animate(false);
			var box = bootbox.dialog('<div class="progress progress-striped active" style="margin:0px;"><div class="bar" style="width: 100%;"></div></div>');
            form.submit();
        },
        errorClass: 'validate-error',
        rules: {
            name: {
                remote: {
                    url: 'tenant-info-checkName.do',
                    data: {
                        <c:if test="${model != null}">
                        id: ${model.id},
                        </c:if>
						tenantId: function() {
							return $('#tenant-info_global').val();
						}
                    }
                }
            }
        },
        messages: {
            name: {
                remote: "名称重复"
            }
        }
    });

	$("#tenant_local_global").change(function() {
		$("#tenant-infoForm").validate().resetForm();
	});
})
    </script>
  </head>

  <body>
    <%@include file="/header/tenant.jsp"%>

    <div class="row-fluid">
	  <%@include file="/menu/tenant.jsp"%>

	  <!-- start of main -->
      <section id="m-main" class="span10">

      <article class="m-widget">
        <header class="header">
		  <h4 class="title"><spring:message code="tenant-info.tenant-info.input.title" text="编辑"/></h4>
		</header>

		<div class="content content-inner">

<form id="tenant-infoForm" method="post" action="tenant-info-save.do?operationMode=STORE" class="form-horizontal">
  <s:if test="model != null">
  <input id="tenant-info_id" type="hidden" name="id" value="${model.id}">
  </s:if>
  <div class="control-group">
    <label class="control-label" for="tenant-info_code">代码</label>
	<div class="controls">
	  <input id="tenant-info_code" type="text" name="code" value="${model.code}" size="40" class="text required" minlength="1" maxlength="10">
    </div>
  </div>
  <div class="control-group">
    <label class="control-label" for="tenant-info_name"><spring:message code="tenant-info.tenant-info.input.name" text="名称"/></label>
	<div class="controls">
	  <input id="tenant-info_name" type="text" name="name" value="${model.name}" size="40" class="text required" minlength="2" maxlength="10">
    </div>
  </div>
  <div class="control-group">
    <label class="control-label" for="tenant-info_reference">引用</label>
	<div class="controls">
	  <input id="tenant-info_reference" name="ref" type="text" value="${model.ref}">
    </div>
  </div>
  <div class="control-group">
    <label class="control-label" for="tenant-info_global">共享</label>
	<div class="controls">
	  <input id="tenant-info_shared" name="shared" type="checkbox" value="1" ${model.shared == 1 ? 'checked' : ''}>
    </div>
  </div>
  <div class="control-group">
    <label class="control-label" for="tenant-info_global">登录方式</label>
	<div class="controls">
	  <select id="tenant-info_global" name="userRepoRef">
      <c:forEach items="${userRepoDtos}" var="item">
	    <option value="${item.id}" ${item.id==model.userRepoRef ? 'selected' : ''}>${item.code}</option>
	  </c:forEach>
	  </select>
    </div>
  </div>
  <div class="control-group">
    <div class="controls">
      <button id="submitButton" type="submit" class="btn a-submit"><spring:message code='core.input.save' text='保存'/></button>
	  &nbsp;
      <button type="button" onclick="history.back();" class="btn a-cancel"><spring:message code='core.input.back' text='返回'/></button>
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
