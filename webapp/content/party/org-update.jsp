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
	<script type="text/javascript">
		$(function() {
			createOrgPicker2({
                modalId: 'orgPicker2',
                showExpression: true,
                chkStyle: 'radio',
                searchUrl: '${tenantPrefix}/rs/user/search',
                treeUrl: '${tenantPrefix}/rs/party/treeNoPostCompanyChecked?partyStructTypeId=1&partyTypeId=${partyType.id}',
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
		  	//岗位
            createUserPickerForPersonInfo({
                modalId: 'userPickerForPersonInfo',
                targetId: 'PersonInfoDiv', //这个是点击哪个 会触发弹出窗口
                showExpression: true,
                multiple: true,
                searchUrl: '${tenantPrefix}/rs/user/search',
                treeNoPostUrl: '${tenantPrefix}/party/asyncTree.do?partyStructTypeId=1&notViewPost=true&notAuth=false',
                childPostUrl: '${tenantPrefix}/rs/party/searchPost'
            });
			$("#shortName").keyup(function(){
		        var regExp = /[A-Z]$/;
		        if(!regExp.test($(this).val())){
		               $(this).val("");
		        }
			})
			//是否显示，取用户之前选择的值显示出来
			$("#isDisplay").val('${partyEntity.isDisplay}');
		});
		/* $(function(){
			$("#isRealPosition").change(function(){
				var position=$("#isRealPosition").val();
				if(position=="1"){
					$("#div_realPosition").show();
				}
				else{
					$("#div_realPosition").hide();
				}
			});
		}) */
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
      <section id="m-main" class="col-md-12" style="padding-top:65px;">

      <div class="panel panel-default">
        <div class="panel-heading">
		  <i class="glyphicon glyphicon-list"></i>
		  添加下级
		</div>

		<div class="panel-body">


<form id="orgForm" method="post" action="org-update.do" class="form-horizontal">
  <input id="org_partyStructTypeId" type="hidden" name="partyStructTypeId" value="${partyStructTypeId}">
  <input id="org_structId" type="hidden" name="structId" value="${partyStruct.id}">
  <input id="org_partyTypeId" type="hidden" name="partyTypeId" value="${partyType.id}">
  <input id="org_level" type="hidden" name="partyLevel" value="${level}">
    <!--  岗位  -->
	<c:if test="${partyType.type == 2}">
		<div class="form-group">
			<label class="control-label col-md-2">上级机构</label>
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
			<input id="org_level" type="hidden" name="partyLevel" value="${partyEntity.level}">
	  </div>							
	  <div class="form-group">
	    <label class="control-label col-md-2" for="orgInputUser_status">组织类型</label>
		<div class="col-sm-5" style="margin-top:7px;">
		  ${partyType.name}
	    </div>
	  </div>
	  <div class="form-group">
	    <label class="control-label col-md-2" for="org_name"><spring:message code="org.org.input.orgname" text="名称"/></label>
		<div class="col-sm-5">
		  <input id="org_id" type="hidden" name="childEntityId" value="${partyEntity.id}">
	      <input id="org_name" type="text" name="childEntityName" value="${partyEntity.name}" size="40" class="form-control required" minlength="1" maxlength="50" autocomplete="off">
	    </div>
	  </div>
	  <div class="form-group">
	    <label class="control-label col-md-2" for="orgInputUser_priority">排序</label>
		<div class="col-sm-5">
		  <input id="orgInputUser_priority" type="text" name="priority" value="${partyStruct.priority}" size="40" class="form-control required number" minlength="1" maxlength="50" autocomplete="off">
	    </div>
	  </div>
	  <div class="form-group"> 
	     <label class="control-label col-md-2" for="orginputuser_priority">是否显示</label> 
		<div class="col-sm-5">
		  <select id="isDisplay"  name="isDisplay"    autocomplete="off">
		    	 <option value="1">是</option>
		    	 <option value="0">否</option>
		   	</select>
		</div>
 	 </div>
 	 <%-- <div class="form-group">
	   <label class="control-label col-md-2" for="orgInputUser_priority">岗位编号</label>
		<div class="col-sm-5">
	  	<input id="orgPositionNo" type="text" name="positionNo" value="${positionNo}" size="40" class="form-control required number" maxlength="8" autocomplete="off">
	   </div>
     </div>
 	 <div class="form-group"> 
	     <label class="control-label col-md-2" for="orginputuser_priority">是否属于虚拟岗位</label> 
		<div class="col-sm-5">
		  <select id="isRealPosition"  name="isRealPosition" autocomplete="off">
		    	 <option value="0" ${isRealPosition=="0"?"selected":""}>否</option>
		    	 <option value="1" ${isRealPosition=="1"?"selected":""}>是</option>
		   	</select>
		</div>
 	 </div>
 	 <div id="div_realPosition" class="form-group" style='${isRealPosition=="1"?"":"display:none;"}'>
			<label class="control-label col-md-2" for="userPickerForPersonInfo">虚拟岗位对应真实岗位</label>
			<div class="col-sm-5 userPickerForPersonInfo">
				<div class="input-group">
					<input id="_task_name_key" type="hidden" name="postId"
						value="${positionRealIds}"> <input type="text"
						class="form-control required" id="postName"
						name="postName" 
						value="${positionRealNames}"
						readonly="readonly">
					<div id="PersonInfoDiv" class="input-group-addon">
						<i class="glyphicon glyphicon-user"></i>
					</div>
				</div>
			</div>
	</div> --%>
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
		<div class="col-sm-5" style="margin-top:7px;">
		  ${partyType.name}
	    </div>
	  </div>
	  <div class="form-group">
	    <label class="control-label col-md-1" for="org_name"><spring:message code="org.org.input.orgname" text="名称"/></label>
		<div class="col-sm-5">
		  <input id="org_id" type="hidden" name="childEntityId" value="${partyEntity.id}">
	      <input id="org_name" type="text" name="childEntityName" value="${partyEntity.name}" size="40" class="form-control required" minlength="1" maxlength="50" autocomplete="off">
		  <button id="btnClean" type="button" class="btn" style="display:none;">清空</button>
		  <!-- <button id="btnOpen" type="button" class="btn">选择已有组织</button> -->
	    </div>
	  </div>
	  <div class="form-group">
	    <label class="control-label col-md-1" for="org_name">缩写</label>
		<div class="col-sm-5">
	      <input id="shortName" type="text" name="shortName" value="${partyEntity.shortName}" placeholder="请输入四位大写英文字母" size="40" class="form-control required" minlength="4" maxlength="4" autocomplete="off">
	    </div>
	  </div>
	  <div class="form-group">
	    <label class="control-label col-md-1" for="orgInputUser_priority">排序</label>
		<div class="col-sm-5">
		  <input id="orgInputUser_priority" type="text" name="priority" value="${partyStruct.priority}" size="40" class="form-control required number" minlength="1" maxlength="50" autocomplete="off">
	    </div>
	  </div>
	   	<div class="form-group"> 
		     <label class="control-label col-md-1" for="orginputuser_priority">是否显示</label> 
				<div class="col-sm-5">
				  <select id="isDisplay"  name="isDisplay"    autocomplete="off">
				    	 <option value="1">是</option>
				    	 <option value="0">否</option>
			    	</select> 
			    </div>
   		</div>
	</c:if>
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
    <div class="col-md-offset-1 col-sm-10" style="margin:0 auto;text-align:center;">
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

  </body>

</html>

