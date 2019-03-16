<%@page contentType="text/html;charset=UTF-8"%>
<%@include file="/taglibs.jsp"%>
<%pageContext.setAttribute("currentHeader", "org");%>
<%pageContext.setAttribute("currentMenu", "org");%>
<%pageContext.setAttribute("currentMenuName", "人力资源");%>
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
			var box = bootbox.dialog('<div class="progress progress-striped active" style="margin:0px;text-align:center;"><div class="bar" style="width: 100%;text-align:center;">正在提交数据...</div></div>');
            form.submit();
        },
        errorClass: 'validate-error'
    });
})
    </script>
    
    
	<link type="text/css" rel="stylesheet" href="${cdnPrefix}/orgpicker/orgpicker.css">
	<script type="text/javascript" src="${cdnPrefix}/orgpicker/orgpicker2.js"></script>
    
    <link type="text/css" rel="stylesheet" href="${cdnPrefix}/userpicker3-v2/userpicker.css">
	<script type="text/javascript" src="${cdnPrefix}/userpicker3-v2/userpickercustomauditForOrg.js"></script>
	<script type="text/javascript" src="${cdnPrefix}/userpicker3-v2/postForPersonInFo.js"></script>
	<script>
		$(function() {
			
			createOrgPicker2({
                modalId: 'orgPicker2',
                showExpression: true,
                chkStyle: 'radio',
                searchUrl: '${tenantPrefix}/rs/user/search',
                treeUrl: '${tenantPrefix}/rs/party/treeNoPostCompanyChecked?partyStructTypeId=1',
                childUrl: '${tenantPrefix}/rs/party/searchUser'
            });
			
			
			
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
			
			$("#shortName").keyup(function(){
		        var regExp = /[A-Z]$/;
		        if(!regExp.test($(this).val())){
		               $(this).val("");
		        }
			})
			
			//是否显示，取用户之前选择的值显示出来
			$("#isDisplay").val(${partyEntity.isDisplay});
		});
			
	</script>
  </head>

  <body>
    <div class="row-fluid">
	 <%-- <c:if test="${not empty flashMessages}">
		<div id="m-success-message" style="display:none;">
		  <ul>
		  <c:forEach items="${flashMessages}" var="item">
		    <c:if test="${item != ''}">
		    	<li>${item}</li>
		    </c:if>
		  </c:forEach>
		  </ul>
		</div>
	 </c:if> --%>
	<!-- start of main -->
      <section id="m-main" class="col-md-12" style="padding-top:3px;">

      <div class="panel panel-default">
        <!-- <div class="panel-heading">
		  <i class="glyphicon glyphicon-list"></i>
		  添加下级
		</div> -->
		<div class="panel-body">
<form id="orgForm" method="post" action="org-update-for-audit-save.do" class="form-horizontal">
  <input id="org_partyStructId" type="hidden" name="partyStructId" value="${partyStructId}">
  <input id="org_partyStructTypeId" type="hidden" name="partyStructTypeId" value="${partyStructTypeId}">
  <input id="org_childEntityRef" type="hidden" name="childEntityRef" value="${childEntityRef}">
  <input id="org_partyEntityId" type="hidden" name="partyEntityId" value="${partyEntityId}">
  <input id="org_structId" type="hidden" name="structId" value="${childEntityId}">
  <input id="org_username" type="hidden" name="username" value="${username}">
  <input id="org_partyTypeId" type="hidden" name="partyTypeId" value="${partyType.id}">
  <input id="org_partyStructTypeId" type="hidden" name="partyStructTypeId" value="${partyStructTypeId}">
  <input id="org_level" type="hidden" name="partyLevel" value="${level}">
  <input id="applyCode" type = "hidden" name="applyCode" value="${applyCode}">
  <input id="accountId" type = "hidden" name="accountId" value="${accountId}">
    <!--  岗位  -->
	<c:if test="${partyType.type == 2}">
	
	<div class="form-group">
			<label class="control-label col-md-1">上级机构</label>
			<div class="col-sm-5">
				<div class="input-group orgPicker2">
					<input id="orgPartyEntityId" type="hidden" name="departmentCode"
						value="${departmentCode}"> <input type="text"
						class="form-control required" id="departmentName"
						name="departmentName" 
						value="${departmentName}"
						readonly="readonly">
					<div class="input-group-addon">
						<i class="glyphicon glyphicon-user"></i>
					</div>
				</div>
			</div>
			<input id="org_level" type="hidden" name="partyLevel"
				value="${partyEntity.level}">
		</div>
	
	
	
	
	  <div class="form-group">
	    <label class="control-label col-md-1" for="orgInputUser_status">组织类型</label>
		<div class="col-sm-5">
		  ${partyType.name}
	    </div>
	  </div>
	  <div class="form-group">
	    <label class="control-label col-md-1" for="org_name"><spring:message code="org.org.input.orgname" text="名称"/></label>
		<div class="col-sm-5">
		  <input id="org_id" type="hidden" name="childEntityId" value="${childEntityId}">
	      <input id="org_name" type="text"  ${isdetail=="1"?'readonly=readonly':''} name="childEntityName" value="${childEntityName}" size="40" class="form-control required" minlength="1" maxlength="50" autocomplete="off">
	    </div>
	  </div>
	  <div class="form-group">
	    <label class="control-label col-md-1" for="orgInputUser_priority">排序</label>
		<div class="col-sm-5">
		  <input id="orgInputUser_priority" type="text"  ${isdetail=="1"?'readonly=readonly':''} name="priority" value="${priority}" size="40" class="form-control required number" minlength="1" maxlength="50" autocomplete="off">
	    </div>
	  </div>
	  
	  <div class="form-group"> 
		     <label class="control-label col-md-1" for="orginputuser_priority">是否显示</label> 
				<div class="col-sm-5">
				  <select id="isDisplay"  name="isDisplay"   ${isdetail=="1"?'disabled=disabled':''}  autocomplete="off">
				    	 <option value="1" ${isDisplay=="1"?"selected='selected'":""}>是</option>
				    	 <option value="0" ${isDisplay=="0"?"selected='selected'":""}>否</option>
			    	</select> 
			    </div>
   		</div> 
	  
	</c:if>
	
	<!--  公司、部门、小组、大区  -->
	<c:if test="${partyType.type == 0}">
	
	
		<div class="form-group">
			<label class="control-label col-md-1">上级机构</label>
			<div class="col-sm-5">
				<div class="input-group orgPicker2">
					<input id="orgPartyEntityId" type="hidden" name="departmentCode"
						value="${departmentCode}"> <input type="text"
						class="form-control required" id="departmentName"
						name="departmentName" 
						value="${departmentName}"
						readonly="readonly">
					<div class="input-group-addon">
						<i class="glyphicon glyphicon-user"></i>
					</div>
				</div>
			</div>
			<input id="org_level" type="hidden" name="partyLevel"
				value="${partyEntity.level}">
		</div>
	
	
	  <div class="form-group">
	    <label class="control-label col-md-1" for="orgInputUser_status">组织类型</label>
		<div class="col-sm-5">
		  ${partyType.name}
	    </div>
	  </div>
	  <div class="form-group">
	    <label class="control-label col-md-1" for="org_name"><spring:message code="org.org.input.orgname" text="名称"/></label>
		<div class="col-sm-5">
		  <input id="org_id" type="hidden" name="childEntityId" value="${childEntityId}">
	      <input id="org_name" type="text"  ${isdetail=="1"?'readonly=readonly':''} name="childEntityName" value="${childEntityName}" size="40" class="form-control required" minlength="1" maxlength="50" autocomplete="off">
		  <button id="btnClean" type="button" class="btn" style="display:none;">清空</button>
		  <!-- <button id="btnOpen" type="button" class="btn">选择已有组织</button> -->
	    </div>
	  </div>
	  <div class="form-group">
	    <label class="control-label col-md-1" for="org_name">缩写</label>
		<div class="col-sm-5">
	      <input id="shortName" type="text" name="shortName" ${isdetail=="1"?'readonly=readonly':''} value="${shortName}" placeholder="请输入四位大写英文字母" size="40" class="form-control required" minlength="4" maxlength="4" autocomplete="off">
	    </div>
	  </div>
	  <div class="form-group">
	    <label class="control-label col-md-1" for="orgInputUser_priority">排序</label>
		<div class="col-sm-5">
		  <input id="orgInputUser_priority" type="text"  ${isdetail=="1"?'readonly=readonly':''} name="priority" value="${priority}" size="40" class="form-control required number" minlength="1" maxlength="50" autocomplete="off">
	    </div>
	  </div>
	  
	   	<div class="form-group"> 
		     <label class="control-label col-md-1" for="orginputuser_priority">是否显示</label> 
				<div class="col-sm-5">
				  <select id="isDisplay"  name="isDisplay"  ${isdetail=="1"?'disabled=disabled':''}   autocomplete="off">
				    	 <option value="1" ${isDisplay=="1"?"selected='selected'":""}>是</option>
				    	 <option value="0" ${isDisplay=="0"?"selected='selected'":""}>否</option>
			    	</select> 
			    </div>
   		</div> 
	</c:if>
  <div class="form-group">
    <div class="col-md-offset-1 col-sm-5" style="text-align:center;margin:0 auto;">
    	<c:if test="${isdetail=='0'}">
      		<button id="submitButton" class="btn btn-default a-submit"><spring:message code='core.input.save' text='保存'/></button>
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

