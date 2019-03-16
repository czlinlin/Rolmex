<%@page contentType="text/html;charset=UTF-8"%>
<%@include file="/taglibs.jsp"%>
<%pageContext.setAttribute("currentHeader", "org");%>
<%pageContext.setAttribute("currentMenu", "org");%>
<!doctype html>
<html lang="en">

  <head>
    <%@include file="/common/meta.jsp"%>
    <title>麦联</title>
    <%@include file="/common/s3.jsp"%>
    <script type="text/javascript">
		$(function() {
		    $("#orgForm").validate({
		        submitHandler: function(form) {
					bootbox.animate(false);
					var box = bootbox.dialog('<div class="progress progress-striped active" style="margin:0px;"><div class="bar" style="width: 100%;"></div></div>');
		            form.submit();
		        },
		        errorClass: 'validate-error'
		    });
		})
    </script>

    <link type="text/css" rel="stylesheet" href="${cdnPrefix}/userpicker3/userpicker.css">
    <link type="text/css" rel="stylesheet" href="${cdnPrefix}/userpicker3-v2/userpicker.css">
	<script type="text/javascript" src="${cdnPrefix}/userpicker3-v2/userpickercustomauditForOrg.js"></script>
	<script type="text/javascript" src="${cdnPrefix}/userpicker3-v2/userpickerbypart.js?randomId=<%=Math.random()%>"></script>
	<script type="text/javascript">
		$(function() {
			//审批人
		    createUserSelectPicker({
		        modalId: 'leaderPicker',//这个 其实是弹出的窗口的div id
		        targetId: 'leaderDiv', //这个是点击哪个 会触发弹出窗口
		        inputStoreIds: {iptid: "leaderId", iptname: "leaderName"},//存储已选择的ID和name的input的id
		        auditId: 'ulapprover',//显示审批步骤
		        showExpression: true,
		        multiple: true,
		        searchUrl: '${tenantPrefix}/rs/user/searchVNoMe',
		        treeUrl: '${tenantPrefix}/party/asyncTree.do?partyStructTypeId=1&notViewPost=true&notAuth=false',
		        childUrl: '${tenantPrefix}/rs/party/searchUserNoMe'
		    });
			
			createUserPicker({
				modalId: 'userPicker',
				showExpression: true,
				searchUrl: '${tenantPrefix}/rs/user/search',
				treeUrl: '${tenantPrefix}/party/asyncTree.do?partyStructTypeId=1&notViewPost=true&notAuth=true',
				childUrl: '${tenantPrefix}/rs/party/searchUser'
			}); 
		})
    </script>
  </head>

  <body>

    <div class="row-fluid">
	<c:if test="${not empty flashMessages}">
		<div id="m-success-message" style="display:none;">
		  <ul>
		  <c:forEach items="${flashMessages}" var="item">
		    <c:if test="${item != ''}">
		    	<li>${item}</li>
		    </c:if>
		  </c:forEach>
		  </ul>
		</div>
	 </c:if>
	<!-- start of main -->
      <section id="m-main" class="col-md-12" style="padding-top:3px;">

      <div class="panel panel-default">
       <!--  <div class="panel-heading">
		  <i class="glyphicon glyphicon-list"></i>
		  关联人员
		</div> -->

		<div class="panel-body">


<form id="orgForm" method="post" action="position-user-input-for-audit-save.do" class="form-horizontal">
	<input id="applyCode" type="hidden" name="applyCode" value="${applyCode}">
  <input id="org_structId" type="hidden" name="structId" value="${structId}">
  <input id="org_childEntityId" type="hidden" name="childEntityId" value="${childEntityId}">
  <input id="org_childEntityName" type="hidden" name="childEntityName" value="${childEntityName}">
  <input id="org_partyEntityId" type="hidden" name="partyEntityId" value="${partyEntityId}">
  <input id="org_partyTypeId" type="hidden" name="partyTypeId" value="${partyTypeId}">
  <input id="org_partyStructTypeId" type="hidden" name="partyStructTypeId" value="${partyStructTypeId}">
  <div class="form-group">
    <label class="control-label col-md-1" for="org_orgname"><spring:message code="org.org.input.orgname" text="岗位名称"/></label>
	<div class="col-sm-5">
	  <div class="input-group">
	  	${position}
      </div>
	</div>
  </div>
  <div class="form-group">
    <label class="control-label col-md-1" for="org_orgname"><spring:message code="org.org.input.orgname" text="人员名称"/></label>
	<div class="col-sm-5">
	  <div class="input-group userPicker">
        <input id="_task_name_key" type="hidden" name="childEntityRef" value="${childEntityRef}" >
        <input type="text" class="form-control required"  ${isdetail=="1"?'readonly=readonly':''} name="username" placeholder="" value="${userName}" readonly="readonly">
        <c:if test="${isdetail=='0'}">
        	<div class="input-group-addon"><i class="glyphicon glyphicon-user"></i></div>
        </c:if>
      </div>
	</div>
  </div>
  <div class="form-group">
    <label class="control-label col-md-1" for="orgInputUser_priority">排序</label>
	<div class="col-sm-5">
	  <input id="orgInputUser_priority" type="text" name="priority"  ${isdetail=="1"?'readonly=readonly':''} value="${priority}" size="40" class="form-control required number" minlength="1" maxlength="50" autocomplete="off">
    </div>
  </div>
  <div class="form-group">
    <div class="col-md-offset-1 col-sm-5" style="text-align:center;margin:0 auto;">
      <c:if test="${isdetail=='0'}">
      	<button id="submitButton" class="btn btn-default a-submit" type="submit"><spring:message code='core.input.save' text='保存'/></button>
      </c:if>
      <button type="button" onclick="window.parent.$('#popWinClose').click();" class="btn btn-default"><spring:message code='core.input.back' text='关闭'/></button>
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

