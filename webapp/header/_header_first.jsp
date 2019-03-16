<%@ page language="java" pageEncoding="UTF-8" %>

    <c:if test="${not empty flashMessages}">
	<div id="m-success-message" style="display:none;">
	  <ul>
	  <c:forEach items="${flashMessages}" var="item">
	    <li>${item}</li>
	  </c:forEach>
	  </ul>
	</div>
	</c:if>
	<script type="text/javascript">
function unreadCount() {
	$.getJSON('${tenantPrefix}/rs/msg/unreadCount?_sed=' + new Date().getTime(), {}, function(data) {
		if (data.data == 0) {
			$('#unreadMsg').html('');
		} else {
			$('#unreadMsg').html(data.data);
		}
	});
}

// zyl 2017-08-08
unreadCount();
setInterval(unreadCount, 1000*60*2);
	</script>
