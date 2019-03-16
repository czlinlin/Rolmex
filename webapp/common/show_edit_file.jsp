<%@page contentType="text/html;charset=UTF-8" %>
<input type="hidden" id="iptdels" name="iptdels" value="">
<c:if test="${StoreInfos.size()==0}">
    无
</c:if>
<c:if test="${StoreInfos.size()!=0}">
    <div id="divShowImg">
        <c:forEach items="${StoreInfos}" var="storeInfo">
            <c:if test='${fn:contains(storeInfo.path,".jpg")
                            	|| fn:contains(storeInfo.path,".gif") 
                            	|| fn:contains(storeInfo.path,".png")
                            	|| fn:contains(storeInfo.path,".bmp")}'>

                <div style="float:left;margin:5px;"><img style="width:100px;height:100px;" src="${picUrl}/${storeInfo.path}"/>
                <br/>
				<a herf="javascript:" onclick="delDoc('${storeInfo.path}',this)">[删除]</a>
				</div>
            </c:if>
        </c:forEach>
        <div style="clear:both;"></div>
    </div>
    <br/>
    <c:forEach items="${StoreInfos}" var="storeInfo">
        <c:if test='${!fn:contains(storeInfo.path,".jpg")
                            	&& !fn:contains(storeInfo.path,".gif") 
                            	&& !fn:contains(storeInfo.path,".png")
                           	&& !fn:contains(storeInfo.path,".bmp")}'>
            <div>
            <a target="_blank"
               href="${tenantPrefix}/downloadAmachment/download.do?id=${storeInfo.id}">${storeInfo.name}
            </a>
            <a herf="javascript:" onclick="delDoc('${storeInfo.path}',this)">[删除]</a></div>
            <br/>
        </c:if>
        
    </c:forEach>
</c:if>
<script>
     $(function() {
   		$('#divShowImg').viewer({
   			url: 'src',
   		});
	});
    
    var delarr=[];
    //del
    var delDoc=function(path,thidId){
    	
    	var iptdels=$("#iptdels").val();
    	if(iptdels!=""){
    		for(var i=0;i<iptdels.split(',');i++){
    			if(iptdels[i]!=null&&iptdels[i]!="")
    				delarr.push(iptdels[i])
   			}
   		}
    	delarr.push(path);
    	
    	$("#iptdels").val(delarr.join(','));
    	//alert($(thidId).parent().html())
    	$(thidId).parent().remove();
    }
</script>