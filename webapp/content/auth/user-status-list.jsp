<%@page contentType="text/html;charset=UTF-8" %>
<%@include file="/taglibs.jsp" %>
<%pageContext.setAttribute("currentHeader", "user");%>
<%pageContext.setAttribute("currentMenu", "auth");%>
<%pageContext.setAttribute("currentMenuName", "系统配置");%>
<%pageContext.setAttribute("currentChildMenu", "用户管理");%>
<!doctype html>
<html lang="en">

<head>
    <%@include file="/common/meta.jsp" %>
    <title><spring:message code="dev.user-status.list.title" text="麦联"/></title>
    <%@include file="/common/s3.jsp" %>
    <script type="text/javascript">
        var config = {
            id: 'user-statusGrid',
            pageNo: ${page.pageNo},
            pageSize: ${page.pageSize},
            totalCount: ${page.totalCount},
            resultSize: ${page.resultSize},
            pageCount: ${page.pageCount},
            orderBy: '${page.orderBy == null ? "" : page.orderBy}',
            asc: ${page.asc},
            params: {
                'filter_LIKES_DISPLAY_NAME': '${param.filter_LIKES_DISPLAY_NAME}',
                'filter_LIKES_REAL_NAME': '${param.filter_LIKES_REAL_NAME}',
                'filter_EQL_ROLE_ID':'${param.filter_EQL_ROLE_ID}'
            },
            selectedItemClass: 'selectedItem',
            gridFormId: 'user-statusGridForm',
            exportUrl: 'user-status-export.do'
        };

        var table;

        $(function () {
            table = new Table(config);
            table.configPagination('.m-pagination');
            table.configPageInfo('.m-page-info');
            table.configPageSize('.m-page-size');
        });
    </script>
</head>

<body>
<%-- <%@include file="/header/auth.jsp"%> --%>
<%@include file="/header/sendmail.jsp" %>
<div class="row-fluid">
    <%@include file="/menu/auth.jsp" %>

    <!-- start of main -->
    <section id="m-main" class="col-md-10" style="padding-top:65px;">

        <div class="panel panel-default">
            <div class="panel-heading">
                <i class="glyphicon glyphicon-list"></i>
                查询
                <div class="pull-right ctrl">
                    <a class="btn btn-default btn-xs"><i id="user-statusSearchIcon"
                                                         class="glyphicon glyphicon-chevron-up"></i></a>
                </div>
            </div>
            <div class="panel-body">

                <form name="user-statusForm" method="post" action="user-status-list.do" class="form-inline">
                    <label for="user-status_name"><spring:message code='user-status.user-status.list.search.name'
                                                                  text='姓名'/>：</label>
                     <c:if test="${isOpenOtherName=='0'}">
		                    <input type="text" id="user-status_name" name="filter_LIKES_DISPLAY_NAME"
		                           value="${param.filter_LIKES_DISPLAY_NAME}" class="form-control">
                      </c:if>
                      
                     <c:if test="${isOpenOtherName=='1'}">
		                    <input type="text" id="user-status_realname" name="filter_LIKES_REAL_NAME"
		                           value="${param.filter_LIKES_REAL_NAME}" class="form-control">
		                          
		                     <label for="user-status_name"><spring:message code='user-status.user-status.list.search.name'
                                                                  text='别名'/>:</label>     
		                          
		                    <input type="text" id="user-status_name" name="filter_LIKES_DISPLAY_NAME"
		                           value="${param.filter_LIKES_DISPLAY_NAME}" class="form-control">
                      </c:if>
                      <label for="user-status_name">角色：</label>       
                     <select name="filter_EQL_ROLE_ID" class="form-control">
                     	<option value="">全部</option>
                     	<c:forEach items="${roleList}" var="item">
                     		<option value="${item.id}" ${item.id==param.filter_EQL_ROLE_ID?'selected':''}>${item.name}</option>
                     	</c:forEach>
                     </select>
                    <button class="btn btn-default a-search" onclick="document.user-statusForm.submit()">查询</button>
                    &nbsp;
                </form>

            </div>
        </div>

        <div style="margin-bottom: 20px;">
            <div class="pull-left btn-group" role="group">
                <!-- <button class="btn btn-default a-insert" onclick="location.href='user-status-input.do'">新建</button> -->
            <%--    <button class="btn btn-default a-remove" onclick="table.removeAll()">删除</button>--%>
                <%--	  <button class="btn btn-default a-export" onclick="table.exportExcel()">导出</button>--%>
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

        <form id="user-statusGridForm" name="user-statusGridForm" method='post' action="user-status-remove.do"
              class="m-form-blank">
            <div class="panel panel-default">
                <div class="panel-heading">
                    <i class="glyphicon glyphicon-list"></i>
                    <spring:message code="scope-info.scope-info.list.title" text="列表"/>
                </div>


                <table id="userGrid" class="table table-hover">
                    <thead>
                    <tr>
                      <%--  <th width="10" class="table-check"><input type="checkbox" name="checkAll"
                                                                  onchange="toggleSelectedItems(this.checked)"></th>--%>
                        <th class="sorting" name="id"><spring:message code="user.user.list.id" text="编号"/></th>
                        <th class="sorting" name="username"><spring:message code="user.user.list.username"
                                                                            text="账号"/></th>
                        
                        <th class="sorting" name="username"><spring:message code="user.user.list.username"
	                                                                            text="姓名"/></th>
                        
                        
                        
                          <c:if test="${isOpenOtherName =='1'}">
	                      	 <th class="sorting" name="realname"><spring:message code="user.user.list.realname"
	                                                                            text="别名"/></th>
                        </c:if>
                        
                        <th class="sorting" name="status"><spring:message code="user.user.list.status" text="状态"/></th>
                        <th class="sorting" name="ref"><spring:message code="user.user.list.ref" text="引用"/></th>
                        <th name="description"><spring:message code="user.user.list.authorities" text="权限"/></th>
                        <th width="150">&nbsp;</th>
                    </tr>
                    </thead>

                    <tbody>
                    <c:forEach items="${page.result}" var="item">
                        <tr>
                          <%--  <td><input type="checkbox" class="selectedItem" name="selectedItem" value="${item.id}"></td>--%>
                            <td>${item.id}</td>

                            <td>${item.username}</td>
                            
                            <c:if test="${isOpenOtherName =='0'}">
			                         <td>${item.diaplayName}</td>
	                        </c:if>
                            
                             <c:if test="${isOpenOtherName =='1'}">
			                        <td>${item.realName}</td>
  									<td>${item.diaplayName}</td>
	                         </c:if>
                          
                            <td>${item.enabled ? '启用' : '禁用'}</td>
                            <td>${item.ref}</td>
                            <td>${item.authorities}</td>

                            <td>
                                <!-- <tags:hasPerm value="user:write">
            <a href="user-status-input.do?id=${item.id}"><spring:message code="core.list.edit" text="编辑"/></a>&nbsp;
			</tags:hasPerm> -->
                                <!--<tags:hasPerm value="user:auth">
			
            <a href="user-status-password.do?id=${item.id}"><spring:message code="user.user.list.password" text="设置密码"/></a>
			
            <a href="javascript:void(0);location.href='user-role-input.do?id=${item.id}'"><spring:message code="user.user.list.role" text="设置权限"/></a>
			</tags:hasPerm>-->

                                <a href="javascript:void(0);location.href='user-role-input.do?id=${item.id}'"><spring:message
                                        code="user.user.list.role" text="设置权限"/></a>
                            </td>
                        </tr>
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

