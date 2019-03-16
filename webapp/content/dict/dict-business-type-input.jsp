<%@page contentType="text/html;charset=UTF-8" %>
<%@include file="/taglibs.jsp" %>
<%pageContext.setAttribute("currentHeader", "dict");%>
<%pageContext.setAttribute("currentMenu", "dict");%>
<%pageContext.setAttribute("currentMenuName", "系统配置");%>
<%pageContext.setAttribute("currentChildMenu", "业务类型");%>
<!doctype html>
<html>

<head>
    <%@include file="/common/meta.jsp" %>
    <title>麦联</title>
    <%@include file="/common/s3.jsp" %>
    
     <link type="text/css" rel="stylesheet" href="${cdnPrefix}/orgpicker/orgpicker.css">
    <script type="text/javascript" src="${cdnPrefix}/orgpicker/orgpicker.js"></script>
    
    <script type="text/javascript">
        $(function () {
               	
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
                submitHandler: function (form) {
                    bootbox.animate(false);
                    var box = bootbox.dialog('<div class="progress progress-striped active" style="margin:0px;"><div class="bar" style="width: 100%;"></div></div>');
                    form.submit();
                },
                errorClass: 'validate-error'
            });
            
            var selectIds="${model.formid}";
            //获取表单名称
        	$.getJSON('${tenantPrefix}/rs/detailPostService/formName', {}, function(data) {
        		var htm = '';
        		var option = "<option value=''>请选择</option>" ;  
        		for (var i = 0; i < data.length; i++) {
        			htm += '<input  name="formNames"  type="checkbox" value="'+data[i].formid+'"';
        			if((","+selectIds+",").indexOf(data[i].formid)>-1)
        				 htm+=' checked="checked" '
        	        htm+='/> '+data[i].formName + "</br>";
       	     	}
       			$(htm).appendTo("#trAddAfter");
        		}); 
            
        	
            
       })
    </script>
</head>

<body>
<%@include file="/header/dict.jsp" %>

<div class="row-fluid">
    <%@include file="/menu/dict.jsp" %>

    <!-- start of main -->
    <section id="m-main" class="col-md-10" style="padding-top:65px;">

        <div class="panel panel-default">
            <div class="panel-heading">
                <i class="glyphicon glyphicon-list"></i>
                编辑
            </div>

            <div class="panel-body">


                <form id="dictTypeForm" method="post" action="dict-business-type-save.do" class="form-horizontal">
                    <c:if test="${model != null}">
                        <input id="dictType_id" type="hidden" name="id" value="${model.id}">
                    </c:if>
                    <div class="form-group">
                        <label class="control-label col-md-1" for="dictType_name">名称</label>
                        <div class="col-sm-5">
                            <input id="dictType_name" type="text" name="businesstype" value="${model.businesstype}"
                                   size="40" class="text">
                        </div>
                    </div>

<!--                     <div class="form-group"> -->
<!--                         <label class="control-label col-md-1" for="dictType_name">表单</label> -->
<!--                         <div class="col-sm-5"> -->
<%--                             <input id="formName" type="text" name="formName" value="${model.formName}" size="40" --%>
<!--                                    class="text" readonly> -->
<!--                         </div> -->
<!--                     </div> -->
                    
                    
                   <div  class="form-group hide">
							<label class="control-label col-md-1" for="dictType_name">表单</label>
							<div id="trAddAfter" class="col-sm-5">
									<input id="formid" name="formid" type="hidden" value=''>
									<input id="formName" name="formName" type="hidden" value=''>
							
							</div>
				</div>  
                    




  <div class="form-group hide">
      <label class="control-label col-md-1">部门</label>
      <div class="col-sm-5">
          <div class="input-group orgPicker">
              <input id="_task_name_key" type="hidden" name="departmentCode"
                     value="${model.departmentCode}">
              <input type="text" class="form-control required" id="departmentName"
                     name="department" placeholder="" value="${model.department}"
                     minlength="2" maxlength="500" readonly="readonly">
              <div class="input-group-addon"><i class="glyphicon glyphicon-user"></i></div>
          </div>
      </div>
      <input id="org_level" type="hidden" name="partyLevel" value="${partyEntity.level}">
  </div>




<!--                     <div class="form-group"> -->
<!--                         <label class="control-label col-md-1" for="dictType_type">部门</label> -->
<!--                         <div class="col-sm-5"> -->
<%--                             <input id="dictType_type" type="text" name="department" value="${model.department}" --%>
<!--                                    size="40" class="text" readonly> -->
<!--                         </div> -->
<!--                     </div> -->
                    <div class="form-group">
                        <label class="control-label col-md-1" for="docInfo_descn">是否启用</label>
                        <div class="col-sm-5">

                            <c:if test="${model.enable=='是'}">
                                <select name="enable" id="enable">
                                    <option value="是">是</option>
                                    <option value="否">否</option>
                                </select>

                            </c:if>
                            <c:if test="${model.enable=='否'}">
                                <select name="enable" id="enable">
                                    <option value="否">否</option>
                                    <option value="是">是</option>
                                </select>

                            </c:if>
                        </div>
                    </div>
                    <div class="form-group">
                        <div class="col-md-offset-2 col-md-10">
                            <button type="submit" class="btn btn-default a-submit">保存</button>
                            &nbsp;
                            <button type="button" class="btn btn-default a-submit" onclick="history.back();">返回</button>
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

