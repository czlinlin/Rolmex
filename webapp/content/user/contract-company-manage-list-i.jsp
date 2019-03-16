

<%@page contentType="text/html;charset=UTF-8" %>
<%@include file="/taglibs.jsp" %>
<%pageContext.setAttribute("currentHeader", "dict");%>
<%pageContext.setAttribute("currentMenu", "dict");%>
<%pageContext.setAttribute("currentMenuName", "人事管理");%>
<%pageContext.setAttribute("currentChildMenu", "合同单位管理");%>
<!doctype html>
<html lang="en">

<head>
    <%@include file="/common/meta.jsp" %>
    <title><spring:message code="dev.dict-type.list.title" text="麦联"/></title>
    <%@include file="/common/s3.jsp" %>
    <script type="text/javascript">
        var config = {
            id: 'dict-typeGrid',
            pageNo: ${page.pageNo},
            pageSize: ${page.pageSize},
            totalCount: ${page.totalCount},
            resultSize: ${page.resultSize},
            pageCount: ${page.pageCount},
            orderBy: '${page.orderBy == null ? "" : page.orderBy}',
            asc: ${page.asc},
            params: {
                'filter_LIKES_contractCompanyName': '${param.filter_LIKES_contractCompanyName}'
            },
            selectedItemClass: 'selectedItem',
            gridFormId: 'dict-typeGridForm',
            exportUrl: 'dict-type-export.do',
            fnShowUrl: "${tenantPrefix}/rs/business/bussiness-type-fnshow",//业务类型部门显示
        };

        var table;

        $(function () {
            table = new Table(config);
            table.configPagination('.m-pagination');
            table.configPageInfo('.m-page-info');
            table.configPageSize('.m-page-size');
        });
        
        

		//删除单位
			var deleteAttendanceDialog = function (id) {
			bootbox.dialog({
		        title: '删除合同单位',
		        message: '确定删除合同单位吗',
		        buttons: {
		            ok: {
		                label: "确定",
			            callback: function(){
								
			            	$('#dict-typeGridForm').attr('action', '${tenantPrefix}/user/delete-contract-company.do?id='+id);
			                $('#dict-typeGridForm').submit();
		
			                }
		    			},
		    		  cancel: {
						label: '取消',
					}
		        }
		    });
		} 
        
			$(function(){
		    	var tipMsg=$('#m-success-tip-message').html();
		    	window.parent.bootbox.alert({
		            message:tipMsg,
		            size: 'large',
		            buttons: {
		                ok: {
		                    label: "确定"
		                }
		            }
		        });
		    })
        
    </script>
    <script type="text/javascript" src="${cdnPrefix}/bootbox/bootbox.min1.js"></script>
    <script type="text/javascript" src="${cdnPrefix}/business/business.js"></script>
    <style>
        body {
            padding-right: 0px !important;
        }
        .hide{display:none;}
    </style>
</head>

<body>
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

    <div class="row-fluid">
	

    <!-- start of main -->
    <section id="m-main" class="col-md-12" style="padding-top:65px;">
        <div class="panel panel-default">
            <div class="panel-heading">
                查询
                <div class="pull-right ctrl">
                    <a class="btn btn-default btn-xs"><i
                            id="workReportInfoSearchIcon"
                            class="glyphicon glyphicon-chevron-up"></i></a>
                </div>
            </div>
            <div class="panel-body">
                <form name="dict-typeGridForm" method="post"
                      action="contract-company-manage-list-i.do" class="form-inline">
                    <label for="B_businesstype">合同单位名称：</label> <input
                        type="text" id="B_businesstype"
                        name="filter_LIKES_contractCompanyName"
                        value="${param.filter_LIKES_contractCompanyName}" class="form-control">
                    <button id="btn_Search" class="btn btn-default a-search"
                            onclick="document.dict-typeGridForm.submit()">查询
                    </button>
                    &nbsp;
                </form>
            </div>
        </div>
        <div style="margin-bottom: 20px;">
            <div class="pull-left btn-group" role="group">
<!--                 <button class="btn btn-default a-insert" onclick="location.href='contract-company-manage-new.do'">新建</button> -->
                <!-- <button class="btn btn-default a-remove" onclick="table.removeAll()">删除</button> -->
            
             <tags:buttonOpteration 
	                            	opterNames="新建" 
	                            	buttonTypes="button" 
	                            	opterTypes="href" 
	                            	opterParams="contract-company-manage-new.do"/>
            
            </div>

            <div class="pull-right">
                每页显示
                <select class="m-page-size form-control" style="display:inline;width:auto;">
                    <option value="10">10</option>
                    <option value="20">20</option>
                    <option value="50">50</option>
                </select>
                条
            </div>

            <div class="clearfix"></div>
        </div>

        <form id="dict-typeGridForm" name="dict-typeGridForm" method='post' action="dict-business-type-remove.do"
              class="m-form-blank">
            <div class="panel panel-default">
                <div class="panel-heading">
                    <i class="glyphicon glyphicon-list"></i>
                    <spring:message code="scope-info.scope-info.list.title" text="列表"/>
                </div>

                <table id="dict-typeGrid" class="table table-hover">
                    <thead>
                    <tr>
                        <th name="id">编号</th>
                        <th name="name">合同单位名称</th>
                        <th name="formname">单位邮箱</th>
                        <th name="smtp">smtp服务器</th>
                        <th name="pop">pop服务器</th>
                        <th name="department">备注</th>
                        <th name="enable">是否启用</th>
                        <th>操作</th>
                    </tr>
                    </thead>

                    <tbody>
                    <c:forEach items="${page.result}" var="item">
<%--                     	<c:if test="item.isenable=='是'"> --%>
                        <tr>
                            <td>${item.id}</td>
                            <td>${item.contractCompanyName}</td>
                            <td>${item.companyEmail}</td>
                            <td>${item.smtpServer.name}</td>
                            <td>${item.popServer.name}</td>
                            <td>${item.remark}</td>
                            <td>${item.isenable}</td>
                            <td>
                                <a href="contract-company-manage-update.do?id=${item.id}" ><spring:message
                                        code="core.list.edit" text="[编辑]"/></a>
                                <a href="javascript:" onclick='deleteAttendanceDialog(${item.id})'>[删除]</a>
                            </td>
                        </tr>
<%--                         </c:if> --%>
                    </c:forEach>
                    </tbody>
                </table>


            </div>
        </form>

        <div>
            <div class="m-page-info pull-left">
                共100条记录 显示1到10条记录
            </div>

            <div class="btn-group m-pagination pull-right">
                <button class="btn btn-default">&lt;</button>
                <button class="btn btn-default">1</button>
                <button class="btn btn-default">&gt;</button>
            </div>

            <div class="clearfix"></div>
        </div>

        <div class="m-spacer"></div>

    </section>
    <!-- end of main -->
</div>

</body>

</html>

