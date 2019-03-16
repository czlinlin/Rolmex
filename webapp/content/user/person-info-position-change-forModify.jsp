<%@page contentType="text/html;charset=UTF-8"%>
<%@include file="/taglibs.jsp"%>
<%pageContext.setAttribute("currentHeader", "person");%>
<%pageContext.setAttribute("currentMenu", "person");%>
<!doctype html>
<html lang="en">

  <head>
    <%@include file="/common/meta.jsp"%>
    <title>麦联</title>
    <%@include file="/common/s3.jsp"%>
    <script type="text/javascript">
		/* $(function() {
		    $("#orgForm").validate({
		        submitHandler: function(form) {
					bootbox.animate(false);
					var box = bootbox.dialog('<div class="progress progress-striped active" style="margin:0px;"><div class="bar" style="width: 100%;"></div></div>');
		            form.submit();
		        },
		        errorClass: 'validate-error'
		    });
		}) */
    </script>

    <link type="text/css" rel="stylesheet"
	href="${cdnPrefix}/orgpicker/orgpicker.css">
<script type="text/javascript" src="${cdnPrefix}/orgpicker/orgpicker.js"></script>

<link type="text/css" rel="stylesheet" href="${cdnPrefix}/userpicker3-v2/userpicker.css">
<script type="text/javascript" src="${cdnPrefix}/userpicker3-v2/userpickercustomaudit.js"></script>
<script type="text/javascript" src="${cdnPrefix}/operation/TaskOperation.js"></script>
<script type="text/javascript" src="${cdnPrefix}/userpicker3-v2/postForPersonInFo.js"></script>
	<script type="text/javascript">
		$(function() {
			
			
			  //审批人
            createUserPicker({
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
                multiple: false,
                searchUrl: '${tenantPrefix}/rs/user/search',
                // treeNoPostUrl: '${tenantPrefix}/rs/party/treeNoPost?partyStructTypeId=1',
                treeNoPostUrl: '${tenantPrefix}/party/asyncTree.do?partyStructTypeId=1&notViewPost=true&notAuth=false',
                childPostUrl: '${tenantPrefix}/rs/party/searchPost'
            });
			
			//提交
			$("#submitButton").click(function(){
				if($("input[name='iptCurrentPost']:checked").size()<1){
					alert("没有选择现有的岗位");
					return false;
				}
				if($("#_task_name_key").val()==""){
					alert("没有选择调整的岗位");
					return false;
				}
				$("form").submit();
				return true;
			})
		})
    </script>
  </head>
  <body>
	 <div class="row-fluid">
	<!-- start of main -->
	   <section id="m-main" class="col-md-12" style="padding-top:3px;">
			<div class="panel panel-default">
			      <!-- <div class="panel-heading">
					  <i class="glyphicon glyphicon-list"></i>职员岗位调整
				  </div> -->
	  		  
			<div class="panel-body">
				<form id="orgForm" method="post" action="person-info-position-change-forModify-save.do" class="form-horizontal">
				 <input id="applyCode" type = "hidden" name="applyCode" value="${applyCode}"  >
 
				  <div class="form-group">
				    <label class="control-label col-md-1" for="orgInputUser_priority">职员姓名：</label>
					<div class="col-sm-5">
						<input type="hidden" id="id" name="id" value="${accountId}" />
					  ${name}
				    </div>
				  </div>
				  <div class="form-group">
				    <label class="control-label col-md-1" for="orgInputUser_priority">现有岗位：</label>
					<div class="col-sm-5">
						<c:forEach items="${positions}" var="item">
							<input type="radio" name="iptCurrentPost" ${isdetail=="1"?'disabled=disabled':''} value="${item.id}" ${oldPostId==item.id?'checked=checked':""}/>${item.position}<br/>
						</c:forEach>
				    </div>
				  </div>
				  <div class="form-group">
				    <label class="control-label col-md-1" for="orgInputUser_priority">调整岗位：</label>
					  <div class="col-sm-5 userPickerForPersonInfo">
                           <div class="input-group ">
                               <input id=_task_name_key type="hidden" name="postId"
                                      value="${newPostId}">
                               <input type="text" name="postName" id="postName"
                                      value="${newPostName}" class="form-control" readonly>
                                      <c:if test="${isdetail=='0'}">
		                               	<div id='PersonInfoDiv' class="input-group-addon"><i class="glyphicon glyphicon-user"></i></div>
	                                  </c:if>
                           </div>
                       </div>
				  </div>
				  <div class="form-group">
				    <div class="col-md-offset-1 col-sm-5" style="text-align:center;margin:0 auto;">
				    	<c:if test="${isdetail=='0'}">
				      		<button id="submitButton" type="button" class="btn btn-default a-submit"><spring:message code='core.input.save' text='保存'/></button>
				      	</c:if>
						<button type="button" onclick="window.parent.$('#popWinClose').click();" class="btn btn-default"><spring:message code='core.input.back' text='关闭'/></button>
				    </div>
				  </div>
				</form>
			</div>
			</div>
	 </section>
		</div>
  </body>

</html>

