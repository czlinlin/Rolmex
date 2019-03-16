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
    <!-- bootbox -->
    <script type="text/javascript" src="${cdnPrefix}/bootbox/bootbox.min1.js"></script>
    <script type="text/javascript">
    	var fnDelOpter=function(id){
    		var confirmDialog = bootbox.confirm({
    	        message: "确定要删除此条流程预设置吗？",
    	        buttons: {
    	            confirm: {
    	                label: '确定',
    	                className: 'btn-success'
    	            },
    	            cancel: {
    	                label: '取消',
    	                className: 'btn-danger'
    	            }
    	        },
    	        callback: function (result) {
    	            if (!result) return;
    	            confirmDialog.modal('hide');
    	            var loading = bootbox.dialog({
    	                message: '<p class="text-center"><i class="fa fa-spin fa-spinner"></i>正在执行...</p>',
    	                size: 'small',
    	                closeButton: false
    	            });
    	            $.ajax({
    	                url: "${tenantPrefix}/rs/user/custom-setting-del",
    	                type: "POST",
    	                data: {id: id},
    	                timeout: 10000,
    	                success: function (data) {
    	                    loading.modal('hide');
    	                    if (data == undefined || data == null || data == "") {
    	                        bootbox.alert("删除操作失败");
    	                        return;
    	                    }

    	                    if (data.code == "200") {
    	                        //dialog.modal('hide')
    	                        var tip = bootbox.alert(
    	                            {
    	                                message: "删除成功！",
    	                                callback: function () {
    	                                    //$("#btn_Search").click();
    	                                    location.href=location.href;
    	                                    tip.modal('hide');

    	                                }
    	                            });
    	                    }
    	                    else
    	                        bootbox.alert(data.message);
    	                    return;
    	                },
    	                error: function (XMLHttpRequest, textStatus, errorThrown) {
    	                    loading.modal('hide');
    	                    bootbox.alert("[" + XMLHttpRequest.status + "]error，请求失败");
    	                    //alert("[" + XMLHttpRequest.status + "]error，请求失败")
    	                },
    	                complete: function (xh, status) {
    	                    loading.modal('hide');
    	                    if (status == "timeout")
    	                        bootbox.alert("请求超时");
    	                }
    	            });
    	        }
    	    });
    	};
    </script>
  </head>
  <body>
    <%@include file="/header/my.jsp"%>
    <div class="row-fluid">
    	<c:if test="${not empty flashMessages}">
	        <div id="m-success-tip-message" style="display: none;">
	            <ul>
	                <c:forEach items="${flashMessages}" var="item">
	                    <c:if test="${item != ''}">
	                        <li  style="list-style:none; word-wrap:break-word;">${item}</li>
	                    </c:if>
	                </c:forEach>
	            </ul>
	        </div>
	    </c:if>
	  	<%@include file="/menu/my.jsp"%>
	  	<!-- start of main -->
		<section id="m-main" class="col-md-10" style="padding-top:65px;">
			<div style="margin-bottom: 40px;">
		 		<div class="pull-left btn-group" role="group">
					<button class="btn btn-default a-insert" onclick="location.href='my-custom-info-setting-input.do'">新建</button>
				</div>
			</div>
			<div class="panel panel-default">
			    <div class="panel-heading">
					<i class="glyphicon glyphicon-list"></i>
					<spring:message code="user.user.list.title" text="自定义流程预设信息列表"/>
				</div>
				<div>
					<table id="auditBaseGrid" class="table table-hover">
					   <thead>
					     <tr>
					       <th class="sorting" name="name">预设审批名称</th>
					       <th style="width:760px;">预设审批人</th>
					       <th class="sorting" name="name">使用状态</th>
					       <th class="sorting" name="name">排序号</th>
					       <th class="sorting" name="name">添加时间</th>
					       <th>备注</th>
					       <th>操作</th>
					     </tr>
					   </thead>
					
					   <tbody>
					     <c:forEach items="${presetApproverList}" var="item">
							<tr>
							  	<td>${item.name}</td>
							  	<td><tags:displayName userIds="${item.approverIds}" showType="2"/></td>
								<td>${item.delStatus=="0"?"正常":"禁用"}</td>
								<td>${item.orderNum}</td>
								<td><fmt:formatDate value="${item.createDate}" type="both" pattern='yyyy-MM-dd'/></td>
								<td>${item.remark}</td>
								<td>
									<a href="my-custom-info-setting-input.do?id=${item.id}">编辑</a>
									<a href="javascript:fnDelOpter(${item.id})">删除</a>	
								</td>
							</tr>
						</c:forEach>
					  </tbody>
					</table>
				</div>
			</div>
	    </section>
	  	<!-- end of main -->
	</div>
  </body>
</html>
