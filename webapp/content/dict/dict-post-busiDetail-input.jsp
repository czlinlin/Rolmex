<%@page contentType="text/html;charset=UTF-8" %>
<%@include file="/taglibs.jsp" %>
<%pageContext.setAttribute("currentHeader", "dict");%>
<%pageContext.setAttribute("currentMenu", "dict");%>
<%pageContext.setAttribute("currentMenuName", "系统配置");%>
<%pageContext.setAttribute("currentChildMenu", "业务类型明细");%>
<!doctype html>
<html>

<head>
    <%@include file="/common/meta.jsp" %>
    <title>麦联</title>
    <%@include file="/common/s3.jsp" %>
    <link type="text/css" rel="stylesheet"
          href="${cdnPrefix}/userpicker3-v2/userpicker.css">
    <script type="text/javascript" src="${cdnPrefix}/userpicker3-v2/post.js"></script>
    <script type="text/javascript">
        $(function () {
        	
        	
            $("#dict-typeForm").validate({
                submitHandler: function (form) {
                    bootbox.animate(false);
                    var box = bootbox.dialog('<div class="progress progress-striped active" style="margin:0px;"><div class="bar" style="width: 100%;"></div></div>');
                    form.submit();
                },
                errorClass: 'validate-error'
            });
            createUserPicker({
                modalId: 'userPicker',
                showExpression: true,
                multiple: false,
                searchUrl: '${tenantPrefix}/rs/user/search',
                // treeNoPostUrl: '${tenantPrefix}/rs/party/treeNoPost?partyStructTypeId=1',
                treeNoPostUrl: '${tenantPrefix}/party/asyncTree.do?partyStructTypeId=1&notViewPost=true&notAuth=false',
                childPostUrl: '${tenantPrefix}/rs/party/searchPost'
            });

        });

		
        
		var setting = {
			async: {
				enable: true,
				url: "${tenantPrefix}/rs/dict/getChenked?id=${id}"
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
		

		

		function save () {
			var treeObj = $.fn.zTree.getZTreeObj("treeMenu");
			var nodes = treeObj.getCheckedNodes(true);
			
			var result='';  
          
            for (var i = 0; i < nodes.length; i++) {  
            	
                    result += nodes[i].id +',';  
               
            }  
            result=result.substring(0,result.lastIndexOf(","));  
            $("#selectedItem").val(result);
            //alert(result);
			$("#dictTypeForm").attr('action',"post-detail-input-save.do?id=${id}");    //通过jquery为action属性赋值
	        $("#dictTypeForm").submit();    //提交ID为myform的表单
           
		}
		
		
    </script>
</head>

<body>
<%@include file="/header/dict.jsp" %>

<div class="row-fluid">
    <%@include file="/menu/dict.jsp" %>

    <!-- start of main -->
    <section id="m-main" class="col-md-10" style="padding-top: 65px;">

        <div class="panel panel-default">
            <div class="panel-heading">
                <i class="glyphicon glyphicon-list"></i> 编辑
            </div>

            <div class="panel-body">

                <form id="dictTypeForm" name="dictTypeForm" method="post"
                      action="dict-business-detail-save.do" class="form-horizontal">
                      <input type="hidden" id = "selectedItem" name="selectedItem" value="">
        
                    <div class="form-group">
                        <label class="control-label col-md-2" for="_task_name_key">选择岗位:</label>
                        <div class="col-sm-8 userPicker">
                            <div class="input-group ">
                                <input id="_task_name_key" type="hidden" name="postId"
                                       value="${postId}">
                                <input type="text" name="postName" id="postName"
                                       value="${postName}" class="form-control" readonly>
                                <div class="input-group-addon"><i class="glyphicon glyphicon-user"></i></div>
                            </div>
                        </div>
                    </div>
                    
			 
			 <div class="panel-body">
               <label class="control-label col-md-2" for="_task_name_key">选择业务明细:</label>
  			</div>
                
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
        </div>
    </section>
    <!-- end of main -->
</div>

</body>
<script>



   
</script>
</html>



