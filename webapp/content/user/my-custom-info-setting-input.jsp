<%@page contentType="text/html;charset=UTF-8"%>
<%@include file="/taglibs.jsp"%>
<%pageContext.setAttribute("currentHeader", "my");%>
<%pageContext.setAttribute("currentMenu", "my");%>
<%pageContext.setAttribute("currentMenuName", "index");%>
<%pageContext.setAttribute("currentChildMenu", "个人流程设置");%>
<!doctype html>
<html lang="en">

  <head>
    <%@include file="/common/meta.jsp"%>
    <title>麦联</title>
    <%@include file="/common/s3.jsp"%>
    <link type="text/css" rel="stylesheet" href="${cdnPrefix}/userpicker3-v2/userpicker.css">
    <script type="text/javascript" src="${cdnPrefix}/userpicker3-v2/userpickercustomaudit.js"></script>
    <script type="text/javascript">
$(function() {
	
	$(function () {
        //审批人
        createUserPicker({
            modalId: 'leaderPicker',//这个 其实是弹出的窗口的div id
            targetId: 'leaderDiv', //这个是点击哪个 会触发弹出窗口
            inputStoreIds: {iptid: "approverIds", iptname: "leaderName"},//存储已选择的ID和name的input的id
            auditId: 'ulapprover',//显示审批步骤
            showExpression: true,
            multiple: true,
            searchUrl: '${tenantPrefix}/rs/user/searchVNoMe',
            treeUrl: '${tenantPrefix}/party/asyncTree.do?partyStructTypeId=1&notViewPost=true&notAuth=false',
            childUrl: '${tenantPrefix}/rs/party/searchUserNoMe'
        });
        
        var strNames=$("#leaderName").val();
        if(strNames!=""){
        	var auditHtml="";
        	var nameArray=strNames.split(',');
        	for(var i=0;i<nameArray.length;i++){
        		auditHtml+="<li style=\"width:140px;float:left;\">";
                auditHtml+=(i+1)+"."+nameArray[i];
                auditHtml+="</li>";
        	}
        	$("#ulapprover").html(auditHtml);
        }
	});
	
    $("#custom-setting-Form").validate({
        submitHandler: function(form) {
			bootbox.animate(false);
			var box = bootbox.dialog('<div class="progress progress-striped active" style="margin:0px;"><div class="bar" style="width: 100%;"></div></div>');
            form.submit();
        },
        errorClass: 'validate-error'
    });
})
    </script>
  </head>

  <body>
    <%@include file="/header/my.jsp"%>
    <div class="row-fluid">
	  <%@include file="/menu/my.jsp"%>
	  <!-- start of main -->
      <section id="m-main" class="col-md-10" style="margin-top:65px;">

      <div class="panel panel-default">
        <div class="panel-heading">
		  个人流程设置
		</div>
		<div class="panel-body">
		<form id="custom-setting-Form" method="post" action="my-custom-info-setting-save.do" class="form-horizontal">
		  <div class="form-group">
		    <label class="control-label col-md-1" for="preSet_name">预设名称</label>
			<div class="col-sm-10">
			  <input type="hidden" name="userId" value="${customPresetApprover.userId}" />
			  <input type="hidden" name="id" value="${customPresetApprover.id}" />
			  <input type="hidden" name="createDate" value="${customPresetApprover.createDate}" />
			  <input  style="width:98%;" id="preSet_name" type="text" maxlength="20" name="name" class="form-control required" value="${customPresetApprover.name}">
		    </div>
		  </div>
	     	<div class="form-group">
			    <label class="control-label col-md-1" for="pimRemind_repeatType">预设审批人</label>
					<div class="col-sm-10">
					  <table style="width:98%;">
					  	<tr>
		                    <td><span style="color:red;">请按顺序选择审核人</span><br/>
		                        <ul id="ulapprover" style="width:96%;margin:0 auto;list-style:none;">
		                        </ul>
		                    </td>
		                </tr>
		                <tr>
		                    <td>
		                        <div class="input-group userPicker" style="width:100%;">
		                            <input id="approverIds" name="approverIds" type="hidden"
		                                   value="${customPresetApprover.approverIds}">
		                            <input type="text" id="leaderName" name="nextUser" class="form-control required"
		                                   value="<tags:user userId="${approverNames}"></tags:user>" minlength="2"
		                                   maxlength="50" class="form-control" readOnly placeholder="点击后方图标即可选人">
		                            <div id='leaderDiv' class="input-group-addon"><i class="glyphicon glyphicon-user"></i>
		                            </div>
		                        </div>
		                    </td>
		                </tr>
					  </table>
				    </div>
		  	</div>
		  <div class="form-group">
		    <label class="control-label col-md-1" for="pimRemind_description">排序号</label>
			<div class="col-sm-10">
			  <input id="pimInfo_name"  style="width:100px;" type="text" maxlength="4" name="orderNum" class="form-control required" value="${customPresetApprover.orderNum==null?'0':customPresetApprover.orderNum}">
			  <label style="color:gray;">(排序号1>0)</label>
		    </div>
		  </div>
		  <div class="form-group">
		    <label class="control-label col-md-1" for="pimRemind_infoTime">备注</label>
			<div class="col-sm-10">
			  <textarea id="remark" class="form-control" name="remark" style="width:98%;">${customPresetApprover.remark}</textarea>
		    </div>
		  </div>
		  <div class="form-group">
		    <label class="control-label col-md-1" for="pimRemind_infoTime">使用状态</label>
			<div class="col-sm-10">
			  <input id='rdUse' type="radio" name="delStatus"  ${(customPresetApprover.delStatus==null||customPresetApprover.delStatus=="0")?"checked":""} value="0"/><label for="rdUse">正常</label>
			  <input id='rdNoUse' type="radio" name="delStatus" ${customPresetApprover.delStatus=="1"?"checked":""}  value="1"/><label for="rdNoUse">禁用</label>
		    </div>
		  </div>
		  <div class="form-group">
		    <div class="col-md-offset-1 col-md-11">
		      <button type="submit" class="btn btn-default a-submit"><spring:message code='core.input.save' text='保存'/></button>
			  &nbsp;
		      <button type="button" class="btn btn-default" onclick="history.back();"><spring:message code='core.input.back' text='返回'/></button>
		    </div>
		  </div>
		</form>
        </div>
      </div>
      </section>
	  <!-- end of main -->
	</div>
  </body>
</html>
