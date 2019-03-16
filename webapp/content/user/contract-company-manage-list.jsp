<%@page contentType="text/html;charset=UTF-8"%>
<%@include file="/taglibs.jsp"%>
<%
	pageContext.setAttribute("currentHeader", "person");
%>
<%
	pageContext.setAttribute("currentMenu", "person");
%>
<c:if test="${isAdminRole=='1'}">
	<%pageContext.setAttribute("currentMenuName", "人事管理");%>
</c:if>

<!doctype html>
<html lang="en">

<head>
<%@include file="/common/meta.jsp"%>
<title><spring:message code="dev.employee-info.list.title"
		text="麦联" /></title>
<%@include file="/common/s3.jsp"%>
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
                'filter_LIKES_p.FULL_NAME': '${param.filter_LIKES_p.FULL_NAME}',
                'partyStructTypeId': '${partyStructTypeId}',
                'partyEntityId': '${partyEntityId}'
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
</head>

<body>
	<%-- <%@include file="/header/org.jsp"%> --%>
	<%@include file="/header/navbar.jsp" %>

	<div class="row-fluid">
		<%@include file="/menu/sidebar.jsp" %>

		<section id="m-main" class="col-md-10" >
			<iframe id="mainframe" name="mainframe" src="${tenantPrefix}/user/contract-company-manage-list-i.do"
				width="100%" height="900px" frameborder="0"></iframe>
			<div class="m-spacer"></div>

		</section>
	</div>
	
</body>

</html>

