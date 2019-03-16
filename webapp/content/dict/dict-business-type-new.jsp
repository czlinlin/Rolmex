<%@page contentType="text/html;charset=UTF-8"%>
<%@include file="/taglibs.jsp"%>
<%pageContext.setAttribute("currentHeader", "dict");%>
<%pageContext.setAttribute("currentMenu", "dict");%>
<%pageContext.setAttribute("currentMenuName", "系统配置");%>
<%pageContext.setAttribute("currentChildMenu", "业务类型");%>
<!doctype html>
<html>

  <head>
    <%@include file="/common/meta.jsp"%>
    <title>麦联</title>
    <%@include file="/common/s3.jsp"%>
    
    <link type="text/css" rel="stylesheet" href="${cdnPrefix}/orgpicker/orgpicker.css">
    <script type="text/javascript" src="${cdnPrefix}/orgpicker/orgpicker.js"></script>
    
    
    <script type="text/javascript">
$(function() {
	
	 createOrgPicker({
         modalId: 'orgPicker',
         showExpression: true,
         multiple: false,
         chkStyle: 'checkbox',
         searchUrl: '${tenantPrefix}/rs/user/search',
         treeUrl: '${tenantPrefix}/rs/party/treeNoAuth?partyStructTypeId=1',
         childUrl: '${tenantPrefix}/rs/party/searchUser'
     });
	
	
    $("#dict-typeForm").validate({
        submitHandler: function(form) {
			bootbox.animate(false);
			var box = bootbox.dialog('<div class="progress progress-striped active" style="margin:0px;"><div class="bar" style="width: 100%;"></div></div>');
            form.submit();
        },
        errorClass: 'validate-error'
    });
    
  //获取表单名称
	$.getJSON('${tenantPrefix}/rs/detailPostService/formName', {}, function(data) {
		var htm = '';
		var option = "<option value=''>请选择</option>" ;  
		for (var i = 0; i < data.length; i++) {
			//alert(JSON.stringify(data[i])); 
	       // option += "<option value='"+ data[i].formid+"'>"+ data[i].formName+"</option>"  
	        htm += '<input  name="formNames"  type="checkbox" value="'+data[i].formid+'"> '+data[i].formName + "</br>";
	     }
			//alert(htm);
			$(htm).appendTo("#trAddAfter");
		});
})


function saveBusinessType() {
	//名称 为空  不允许提交
	if ( document.getElementById('dictType_name').value ==""){
		alert("请输入名称！");
		return false;
	}
	
	//部门 为空  不允许提交
	/* if ( document.getElementById('departmentName').value ==""){
		alert("请选择部门！");
		return false;
	} */
	
	//表单 为空  不允许提交
	//if ( document.getElementById('formNames').value ==""){
	//	alert("请选择表单！");
	//	return false;
	//}

	
	$('#dictTypeForm').attr('action', '${tenantPrefix}/dict/dict-business-type-save.do');
	$('#dictTypeForm').submit();
}

function getFormName() {
	var  myselect=document.getElementById("formNames");
	var index=myselect.selectedIndex ;  
	var bt=myselect.options[index].value;
	var t=myselect.options[index].text;
	
	$("#formid").val(bt);
	$("#formName").val(t);
}



    </script>
  </head>

  <body>
    <%@include file="/header/dict.jsp"%>

    <div class="row-fluid">
	  <%@include file="/menu/dict.jsp"%>

	<!-- start of main -->
      <section id="m-main" class="col-md-10" style="padding-top:65px;">

      <div class="panel panel-default">
        <div class="panel-heading">
		  <i class="glyphicon glyphicon-list"></i>
		 新建
		</div>

		<div class="panel-body">


<form id="dictTypeForm" method="post" action="dict-business-type-save.do" class="form-horizontal">

  <div class="form-group">
    <label class="control-label col-md-1" for="dictType_name">名称</label>
	<div class="col-sm-5">
	  <input id="dictType_name" type="text" name="businesstype"  size="40" class="text" >
    </div>
  </div>

 <div  class="form-group hide">
							<label class="control-label col-md-1" for="dictType_name">表单</label>
							<div id="trAddAfter" class="col-sm-5">
									<input id="formid" name="formid" type="hidden" value="">
									<input id="formName" name="formName" type="hidden" value="">
						<!-- 	<input type="checkbox" value="" onchange="getFormName()"> -->
							<input  name="formNames" value=""/>
							</div>
						</div> 
  
  <div class="form-group hide">
      <label class="control-label col-md-1">部门</label>
      <div class="col-sm-5">
          <div class="input-group orgPicker">
              <input id="_task_name_key" type="hidden" name="departmentCode"
                     value="">
              <input type="text" class="form-control required" id="departmentName"
                     name="department" placeholder="" value=""
                     minlength="2" maxlength="500" readonly="readonly">
              <div class="input-group-addon"><i class="glyphicon glyphicon-user"></i></div>
          </div>
      </div>
      <input id="org_level" type="hidden" name="partyLevel" value="${partyEntity.level}">
  </div>
  
  <div class="form-group">
    <label class="control-label col-md-1" for="docInfo_descn">是否启用</label>
	<div class="col-sm-5">
	 
                    <select name="enable" id="enable">
						<option value="是">是</option>
						<option value="否">否</option>
					</select>       
    </div>
  </div>
  <div class="form-group">
    <div class="col-sm-6">
      <!-- <button type="button" class="btn a-submit"><spring:message code='core.input.save' text='保存'/></button> -->
      <button id="confirm" class="btn a-submit" type="button" onclick="saveBusinessType()">保存</button>
	  &nbsp;
      <button type="button" class="btn a-cancel" onclick="history.back();"><spring:message code='core.input.back' text='返回'/></button>
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

