<%@page contentType="text/html;charset=UTF-8" %>

<c:if test="${StoreInfos.size()==0}">
    无
</c:if>
<c:if test="${StoreInfos.size()!=0}">
	<c:if test="${isPrint == true}">
    	<div id="divShowImg" style="text-align:center;">
    </c:if>
    <c:if test="${isPrint==null or isPrint == false}">
    	<div id="divShowImg">
    </c:if>
        <c:forEach items="${StoreInfos}" var="storeInfo">
            <c:if test='${fn:contains(storeInfo.path,".jpg")
                            	|| fn:contains(storeInfo.path,".gif") 
                            	|| fn:contains(storeInfo.path,".png")
                            	|| fn:contains(storeInfo.path,".bmp")}'>
				<c:if test="${isPrint == true}">
					<img style="width:98%;margin:5px auto;" src="${picUrl}/${storeInfo.path}"/>
				</c:if>
				<c:if test="${isPrint==null or isPrint == false}">
					<img style="width:100px;height:100px;" src="${picUrl}/${storeInfo.path}"/>
				</c:if>
		
            </c:if>
        </c:forEach>
    </div>
    <c:forEach items="${StoreInfos}" var="storeInfo">
        <c:if test='${!fn:contains(storeInfo.path,".jpg")
                            	&& !fn:contains(storeInfo.path,".gif") 
                            	&& !fn:contains(storeInfo.path,".png")
                            	&& !fn:contains(storeInfo.path,".bmp")}'>
            <a target="_blank"
               href="${tenantPrefix}/downloadAmachment/download.do?id=${storeInfo.id}">${storeInfo.name}
            </a>
        </c:if>
        <br/>
    </c:forEach>
</c:if>
<script>
     $(function() {
   		$('#divShowImg').viewer({
   			url: 'src',
   		});
	});
</script>