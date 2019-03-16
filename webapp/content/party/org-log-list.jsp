<%--
  
  User: wanghan
  Date: 2017\11\3 0003
  Time: 16:33
 
--%>
<%@page contentType="text/html;charset=UTF-8" %>
<%@include file="/taglibs.jsp" %>
<%
    pageContext.setAttribute("currentHeader", "person");
%>
<%
    pageContext.setAttribute("currentMenu", "person");
%>
<%
    pageContext.setAttribute("currentMenuName", "人力资源");
%>
<!doctype html>
<html lang="en">

<head>
    <%@include file="/common/meta.jsp" %>
    <title><spring:message code="dev.employee-info.list.title"
                           text="麦联"/></title>
    <%@include file="/common/s3.jsp" %>
    <script type="text/javascript"
            src="${cdnPrefix}/bootbox/bootbox.min1.js"></script>
    <script type="text/javascript">
        var config = {
            id: 'person-infoGrid',
            pageNo: ${page.pageNo},
            pageSize: ${page.pageSize},
            totalCount: ${page.totalCount},
            resultSize: ${page.resultSize},
            pageCount: ${page.pageCount},
            orderBy: '${page.orderBy == null ? "" : page.orderBy}',
            asc: ${page.asc},
            params: {
                'filter_LIKES_FULL_NAME': '${param.filter_LIKES_FULL_NAME}',
                'partyStructTypeId': '${partyStructTypeId}',
                'partyEntityId': '${partyEntityId}',
                'isSearch': '${isSearch}',
                'id': '${id}',
            },
            selectedItemClass: 'selectedItem',
            gridFormId: 'person-infoGridForm',
            exportUrl: 'person-info-export.do',
            resetUrl: "${tenantPrefix}/rs/user/person-info-reset",
            resetKeyUrl: "${tenantPrefix}/rs/user/person-info-resetkey"
        };

        var table;

        $(function () {
            table = new Table(config);
            table.configPagination('.m-pagination');
            table.configPageInfo('.m-page-info');
            table.configPageSize('.m-page-size');
        });

    
     
   
    </script>

    <style type="text/css">
        th {
            white-space: nowrap
        }
        td{
        	white-space: nowrap
        }
    </style>
</head>

<body>

<div class="row-fluid">
    <c:if test="${not empty flashMessages}">
        <div id="m-success-message" style="display: none;">
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

      
        <div style="margin-bottom: 20px;">
           
			<div class="pull-left btn-group" role="group">
				<button class="btn btn-default a-insert" onclick="history.go(-1)">返回</button>
			</div>
			
            <div class="pull-right" style="display: none">
                每页显示 <select class="m-page-size form-control"
                             style="display: inline; width: auto;">
                <option value="10">10</option>
            </select> 条
            </div>

            <div class="clearfix"></div>
        </div>

        <form id="person-infoGridForm" name="person-infoGridForm"
              method='post'
              action="person-info-rosterLogList.do?id=${id}"
              class="m-form-blank">
            <div class="panel panel-default">
                <div class="panel-heading">
                    <i class="glyphicon glyphicon-list"></i>
                    <spring:message code="scope-info.scope-info.list.title" text="列表"/>
                </div>
				<div  style="overflow-x:scroll;">
                <table id="person-infoGrid" class="table table-hover">
                    <thead>
                    <tr>
                               
                        <th name="operationID">操作人员</th>
                        <th name="updateContent">修改内容</th>
                        <th name="updateTime">修改时间</th>
                        

                    </tr>
                    </thead>

                    <tbody>
                    <c:forEach items="${page.result}" var="item">
                        <tr>
                        
                            <td>${item.operationName}</td>
                            
                            <td>${item.modifyContent}</td>
                            
                            <td>${item.operationTime}</td> 
                            
         
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
				</div>
               

            </div>
        </form>

        <div>
            <div class="m-page-info pull-left">共100条记录 显示1到10条记录</div>

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

