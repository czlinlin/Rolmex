<%@page contentType="text/html;charset=UTF-8"%>
<%@include file="/taglibs.jsp"%>
<%pageContext.setAttribute("currentHeader", "org");%>
<c:if test="${isAdminRole=='1'}">
	<%pageContext.setAttribute("currentMenuName", "人事管理");%>
</c:if>
<%pageContext.setAttribute("currentMenu", "org");%>
<!doctype html>
<html lang="en">

  <head>
    <%@include file="/common/meta.jsp"%>
    <title><spring:message code="dev.org.list.title" text="麦联"/></title>
    <%@include file="/common/s3.jsp"%>
    <script type="text/javascript">
		var config = {
		    id: 'orgGrid',
		    pageNo: ${page.pageNo},
		    pageSize: ${page.pageSize},
		    totalCount: ${page.totalCount},
		    resultSize: ${page.resultSize},
		    pageCount: ${page.pageCount},
		    orderBy: '${page.orderBy == null ? "" : page.orderBy}',
		    asc: ${page.asc},
		    params: {
		        'partyStructTypeId': '${param.partyStructTypeId}',
		        'partyEntityId': '${param.partyEntityId}',
		        'name': '${param.name}'
		    },
			selectedItemClass: 'selectedItem',
			gridFormId: 'orgGridForm',
			exportUrl: 'org-export.do'
		};

		var table;

		$(function() {
			table = new Table(config);
		    table.configPagination('.m-pagination');
		    table.configPageInfo('.m-page-info');
		    table.configPageSize('.m-page-size');
		});
	
    </script>
  </head>

  <body>
    <%@include file="/header/navbar.jsp" %>

    <div class="row-fluid">
	  <%-- <%@include file="/menu/org.jsp"%> --%>
	  <%@include file="/menu/sidebar.jsp" %>

	  <!-- start of main -->
      <section id="m-main" class="col-md-10" style="padding-top:65px;">
		<iframe id="mainframe" name="mainframe" src="org-list-i.do" width="100%" height="900px" frameborder="0"></iframe>
      	<div class="m-spacer"></div>

      </section>
	  <!-- end of main -->
	</div>

  </body>

</html>

