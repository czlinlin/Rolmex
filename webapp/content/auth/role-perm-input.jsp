<%@page contentType="text/html;charset=UTF-8"%>
<%@include file="/taglibs.jsp"%>
<%pageContext.setAttribute("currentHeader", "auth");%>
<%pageContext.setAttribute("currentMenu", "auth");%>
<%pageContext.setAttribute("currentMenuName", "系统配置");%>
<%pageContext.setAttribute("currentChildMenu", "角色管理");%>
<!doctype html>
<html>

  <head>
    <%@include file="/common/meta.jsp"%>
    <title>麦联</title>
    <%@include file="/common/s3.jsp"%>
    <script type="text/javascript">
		$(function() {
		    $("#role-permForm").validate({
		        submitHandler: function(form) {
					bootbox.animate(false);
					var box = bootbox.dialog('<div class="progress progress-striped active" style="margin:0px;"><div class="bar" style="width: 100%;"></div></div>');
		            form.submit();
		        },
		        errorClass: 'validate-error'
		    });
		})
		
		function save () {
			var treeObj = $.fn.zTree.getZTreeObj("treeMenu");
			var nodes = treeObj.getCheckedNodes(true);
			
			var result='';  
            /* if(nodes.length==0){  
                alert("必须选择菜单！");  
                return false;  
            }   */
            for (var i = 0; i < nodes.length; i++) {  
                //var halfCheck = nodes[i].getCheckStatus();  
                 //if (!halfCheck.half){  
                    result += nodes[i].id +',';  
                 //}  
              
            }  
            result=result.substring(0,result.lastIndexOf(","));  
            $("#selectedItem").val(result);
            //alert(result);
			$("#roleForm").attr('action',"role-perm-save.do");    //通过jquery为action属性赋值
	        $("#roleForm").submit();    //提交ID为myform的表单
           
		}
		
    </script>
  </head>

  <body>
    <%@include file="/header/sendmail.jsp"%>

    <div class="row-fluid">
	  <%@include file="/menu/auth.jsp"%>

	<!-- start of main -->
      <section id="m-main" class="col-md-10" style="padding-top:65px;">

      <div class="panel panel-default">
        <div class="panel-heading">
		  <i class="glyphicon glyphicon-list"></i>
		  编辑
		</div>

		<div class="panel-body">


<form id="roleForm" method="post" class="form-horizontal">
  <input type="hidden" name="id" value="${id}">
  <input type="hidden" id = "selectedItem" name="selectedItem" value="">
  <%-- <c:forEach items="${permTypes}" var="permType">
  <div class="form-group">
	<label class="control-label col-md-2"><strong>${permType.name}:</strong></label>
    <div class="col-md-10">
      <c:forEach items="${permType.perms}" var="item" varStatus="status">
        <input id="selectedItem-${item.id}" type="checkbox" name="selectedItem" value="${item.id}" <tags:contains items="${selectedItem}" item="${item.id}">checked</tags:contains>>
        <label for="selectedItem-${item.id}" style="display:inline;font-weight:normal;">${item.name}</label>
		&nbsp;
		<c:if test="${status.count % 5 == 0}">
		<br>
		</c:if>
      </c:forEach>
    </div>
  </div>
  </c:forEach> --%>
  
  <div class="panel-body">
	<ul id="treeMenu" class="ztree"></ul>
  </div>
      
  <div class="form-group">
    <div class="col-md-5 col-md-offset-2">
      <button type = "button" id="button" class="btn btn-default" onclick ="save();"><spring:message code='core.input.save' text='保存'/></button>
	  &nbsp;
      <button type="button" onclick="history.back();" class="btn btn-link"><spring:message code='core.input.back' text='返回'/></button>
    </div>
  </div>
</form>

		</div>
      </article>

    </section>
	<!-- end of main -->
	</div>
	
	<script type="text/javascript">
		var setting = {
			async: {
				enable: true,
				url: "${tenantPrefix}/rs/auth/getMenus?roleId=${id}"
			},
			check: {
				chkboxType: {"Y": "ps", "N": "ps"},
				chkStyle: "checkbox",
				enable: true,
                nocheckInherit: false
			},
			callback: {
				onClick: function(event, treeId, treeNode) {
					// location.href = '${tenantPrefix}/party/org-list.do?partyStructTypeId=${partyStructTypeId}&partyEntityId=' + treeNode.id;
				}
			}
		};
		
		var zNodes =[];

		$(function(){
			$.fn.zTree.init($("#treeMenu"), setting, zNodes);
			
		});
		
	</script>
  </body>

</html>

