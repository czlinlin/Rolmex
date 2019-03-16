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
    <script type="text/javascript"
            src="${cdnPrefix}/bootbox/bootbox.min1.js"></script>
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
		    
		    var tipMsg=$('#m-success-tip-message').html();
		    if($('#m-success-tip-message').size()>0){
		    	if(tipMsg!=""){
		    		window.parent.bootbox.alert({
		                message:tipMsg,
		                size: 'large',
		                buttons: {
		                    ok: {
		                        label: "确定"
		                    }
		                }
		            });
		    	}
		    }
        	
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

    <div class="row-fluid" style="padding-top:70px;">
	<c:if test="${not empty flashMessages}">
		<div id="m-success-tip-message" style="display:none;" >
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
        <div class="panel-heading">
		  <i class="glyphicon glyphicon-list"></i>
		  关联人员
		</div>

		<div class="panel-body">


<form id="orgForm" method="post" action="position-user-save.do" class="form-horizontal">
  <input id="org_partyStructTypeId" type="hidden" name="partyStructTypeId" value="${partyStructTypeId}">
  <input id="org_partyEntityId" type="hidden" name="partyEntityId" value="${partyEntityId}">
  <input id="org_partyTypeId" type="hidden" name="partyTypeId" value="${partyType.id}">
  <input id="org_level" type="hidden" name="partyLevel" value="${level}">
  <div class="form-group">
    <label class="control-label col-md-1" for="org_orgname"><spring:message code="org.org.input.orgname" text="名称"/></label>
	<div class="col-sm-5">
	  <div class="input-group userPicker">
        <input id="_task_name_key" type="hidden" name="childEntityRef" value="" >
        <input type="text" class="form-control required" name="username" placeholder="" value="" readonly="readonly">
        <div class="input-group-addon"><i class="glyphicon glyphicon-user"></i></div>
      </div>
	</div>
  </div>
  <div class="form-group" style="display:none;">
    <label class="control-label col-md-1" for="orgInputUser_status">是否兼职</label>
	<div class="col-sm-5">
	  <label for="orgInputUser_status1" class="radio inline">
	    <input id="orgInputUser_status1" type="radio" name="status" value="1" class="required" checked>
		主职
	  </label>
	  <label for="orgInputUser_status2" class="radio inline">
	    <input id="orgInputUser_status2" type="radio" name="status" value="2" class="required">
		兼职
	  </label>
	  <label for="orgInputUser_status2" class="validate-error" generated="true" style="display:none;"></label>
    </div>
  </div>
  <div class="form-group">
    <label class="control-label col-md-1" for="orgInputUser_priority">排序</label>
	<div class="col-sm-5">
	  <input id="orgInputUser_priority" type="text" name="priority" value="${priority}" size="40" class="form-control required number" minlength="1" maxlength="50" autocomplete="off">
    </div>
  </div>
  
  <%@include file="/common/custom_audit_fororg.jsp" %>
  <%-- <c:if test="${isAudit=='1'}">
	<hr>
	<input id="url" type="hidden" name="url" value="/operationCustom/custom-detail.do?suspendStatus=custom">
	<div class="form-group">
			<label class="control-label col-md-1" for="orgInputUser_priority">受理单编号</label>
			<div class="col-sm-5" style="padding-top:8px;">
				<span id="spanApplyCode" style="">${code}</span>
                     <input id="applyCode" class="input_width" style="display:none;" 
                     		name="applyCode" value="${code}"  readonly>
			</div>
		</div>	
		<div class="form-group">
			<label class="control-label col-md-1" for="orgInputUser_priority"></label>
			<div class="col-sm-10" style="padding-top:8px;">
				<div style="color:red;">请按顺序选择审核人</div>
				<ul id="ulapprover" style="width:96%;margin:0 auto;list-style:none;">
                      </ul>
			</div>
		</div>	
		<div  class="form-group">	
			<label class="control-label col-md-1"><span style="color:red;">*</span>审批人</label>
			<div class="col-sm-10" style="padding-top:8px;">
				<div class="input-group userPicker" style="width:100%;">
                          <input id="leaderId" name="nextID" type="hidden" name="leader">
                          <input type="text" id="leaderName" name="nextUser" 
                                 minlength="2"  maxlength="50" class="form-control required" readOnly placeholder="点击后方图标即可选人">
                          <div id='leaderDiv'  class="input-group-addon"><i class="glyphicon glyphicon-user"></i>
                          </div>
                      </div>
			</div>
		</div>	
</c:if> --%>
  <div class="form-group">
    <div class="col-md-offset-1 col-sm-5">
      <button id="submitButton" class="btn btn-default a-submit"><spring:message code='core.input.save' text='保存'/></button>
      <button type="button" onclick="history.back();" class="btn btn-link"><spring:message code='core.input.back' text='返回'/></button>
    </div>
  </div>
</form>

		</div>
      </article>

    </section>
	<!-- end of main -->
	</div>
</div>
  </body>

</html>

