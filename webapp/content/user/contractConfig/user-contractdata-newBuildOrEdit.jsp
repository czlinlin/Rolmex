<%@page contentType="text/html;charset=UTF-8"%>
<%@include file="/taglibs.jsp"%>
<%pageContext.setAttribute("currentHeader", "contract");%>

<%pageContext.setAttribute("currentMenu", "contract");%>
<!doctype html>
<html lang="en">

 <head>
   <%@include file="/common/meta.jsp"%>
   <title><spring:message code="dev.org.list.title" text="麦联"/></title>
   <%@include file="/common/s3.jsp"%>
   <link type="text/css" rel="stylesheet" href="${cdnPrefix}/userpicker3-v2/userpicker.css">
   <script type="text/javascript" src="${cdnPrefix}/userpicker3-v2/userpickercustom.js"></script>
   <script type="text/javascript">
	   $(function () {
	       //注册接收人弹出
	       createUserPicker({
	           modalId: 'leaderPicker',//这个 其实是弹出的窗口的div id
	           targetId: 'leaderDiv', //这个是点击哪个 会触发弹出窗口
	           showExpression: true,
	           searchUrl: '${tenantPrefix}/rs/user/searchVNoMe',
	           treeUrl: '${tenantPrefix}/party/asyncTree.do?partyStructTypeId=1&notViewPost=true&notAuth=false',
	           childUrl: '${tenantPrefix}/rs/party/searchUserNoMe'
	       });
	   });
	   
	   //保存配置信息
	   function saveSetting(){
		   var unionId = $("#leaderId").val();
		   if(unionId == ""){
			   alert("请选择数据配置人");
			   return false;
		   }
		   var companys = "";
		   $("input[type='checkbox']:checked").each(function(){
			   companys += $(this).val();
		   });
		   if(companys == ""){
			   alert("请选择合同单位");
			   return false;
		   }
		   $("#orgGridForm").submit();
	   }
   </script>
   <style type="text/css">
	   	#contractGrid td{
	   		border:1px solid gray;
	   		height:35px;
	   	}
	   	.oneTd{
	   		width:30%;
	   		text-align:center;
	   	}
	   	.buttonTd{
	   		text-align:center;
	   	}
	   	button{
	   		border-radius:8px;
	   	}
   </style>
 </head>

<body>
  <div class="row-fluid">
    <section id="m-main" class="col-md-12" style="padding-top:65px;">
	<form id="orgGridForm" name="orgGridForm" method='post' action="auth-contractdata-save.do" class="form-horizontal">
	    <div class="panel panel-default">
	      <div class="panel-heading">
	  		<i class="glyphicon glyphicon-list"></i>
	  		<spring:message code="scope-info.scope-info.list.title" text="编辑数据权限"/>
		  </div>
	
		<table id="contractGrid" style="width:100%;">
		  <!-- <thead>
		    <tr>
		      <th colspan="2">合同单位配置负责人</th>
		    </tr>
		  </thead> -->
		  <tbody>
	    	<tr>
	    		<td class="oneTd">数据配置人</td>
	    		<td>
	    			<div class="input-group userPicker">
                        <input id="leaderId" type="hidden" name="unionId" value="${contractByUnionId.union_id}">
                        <input type="text" id="leaderName" name="sendeeName" class="form-control required"
                               minlength="2" maxlength="50" class="form-control" value="<tags:user userId="${contractByUnionId.union_id}"></tags:user>" 
                               readOnly placeholder="点击后方图标即可选人">
                        <div id='leaderDiv' class="input-group-addon ">
                        	<i class="glyphicon glyphicon-user"></i>
                        </div>
                   </div>
	    		</td>
	    	</tr>
	    	<tr>
	    		<td class="oneTd">合同单位</td>
	    		<td>
	    			<c:forEach items="${contractCompanyList}" var="contractCompany">
		    			<c:choose>
		    				<c:when test="${fn:contains(contractByUnionId.companyIds,contractCompany.id)}">
		    					<input type="checkbox" checked name="contractCompany" value="${contractCompany.id}">${contractCompany.contract_company_name}
		    				</c:when>
		    				<c:otherwise>
		    					<input type="checkbox" name="contractCompany" value="${contractCompany.id}">${contractCompany.contract_company_name}
		    				</c:otherwise>
		    			</c:choose>
	    			</c:forEach>
	    		</td>
	    	</tr>
	    	<tr>
	    		<td class="oneTd">备注</td>
	    		<td><input name="note" type="text" style="width:100%;height:100%;" value="${contractByUnionId.note}"/></td>
	    	</tr>
	    	<tr>
	    		<td colspan="2" class="buttonTd"><button type="button" onclick="saveSetting()">保存</button>&nbsp;&nbsp;<button type="button" onclick="history.back()">返回</button></td>
	    	</tr>
	    </tbody>
	  </table>
	  </div>
	</form>
    </section>
  </div>
</body>
</html>

