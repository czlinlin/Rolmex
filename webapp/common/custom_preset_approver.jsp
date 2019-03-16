<%@page contentType="text/html;charset=UTF-8" %>
<script type="text/javascript" src="${cdnPrefix}/jquery-tooltipster/jquery.tooltipster.js"></script>
<link type="text/css" rel="stylesheet" href="${cdnPrefix}/jquery-tooltipster/tooltipster.css">
	<ul id="ulPreSetApprover">
		<c:forEach items="${presetApproverList}" var="item">
			<li style="list-style:none;padding:10px;float:left;">
				[<a href="javascript:" style="color:#1D82D0;text-decoration:none;" onclick="fnAuditorSetting(${item.id})">
					<span class="preSetAuditor" title="<tags:displayName userIds="${item.approverIds}" showType="2"/>">${item.name}</span>
				</a>]
				<input type="hidden" id="iptIds${item.id}" value="${item.approverIds}"/>
				<input type="hidden" id="iptNames${item.id}" value="<tags:displayName userIds="${item.approverIds}" showType="1"/>"/>
			</li>
		</c:forEach>		
	</ul>
	<script>
		var fnAuditorSetting=function(id){
			var selectIds=$("#leadId").val();
						
			var strNames=$("#iptNames"+id).val();
			//var auditTip="";
	        if(strNames!=""){
	        	var auditHtml="";
	        	var nameArray=strNames.split(',');
	        	for(var i=0;i<nameArray.length;i++){
	        		auditHtml+="<li style=\"width:140px;float:left;\">";
	                auditHtml+=(i+1)+"."+nameArray[i];
	                auditHtml+="</li>";
	                
	                //auditTip+=(i+1)+"."+nameArray[i];
	        	}
	        	$("#ulapprover").html(auditHtml);
	        }
			$("#leaderId").val($("#iptIds"+id).val());
			$("#leaderName").val(strNames);
			//$(thisId).attr("title",auditTip);
		}
		
		$(function(){
			$(".preSetAuditor").tooltipster();
		})
	</script>

<c:if test="${empty presetApproverList}">
	无（可到“个人信息”->“个人流程设置”中添加）
</c:if>