<%@page contentType="text/html;charset=UTF-8" %>
<%@include file="/taglibs.jsp" %>
<%pageContext.setAttribute("currentHeader", "dict");%>
<%pageContext.setAttribute("currentMenu", "dict");%>
<%pageContext.setAttribute("currentMenuName", "系统配置");%>
<%pageContext.setAttribute("currentChildMenu", "业务类型明细");%>
<!doctype html>
<html>
<head>
    <%@include file="/common/meta.jsp" %>
    <title>麦联</title>
    <%@include file="/common/s3.jsp" %>
    <script type="text/javascript" src="${cdnPrefix}/popwindialog/popwin.js"></script>
    <script type="text/javascript" src="${cdnPrefix}/bootbox/bootbox.min1.js"></script>
    <script type="text/javascript" src="${cdnPrefix}/business/stepDetail.js"></script><!-- 多分支的也封装到这个js文件 -->
    <style>
    	#linkTable td{
    		border:1px solid #0D0D0D;
    	}
    	.head{
    		text-align:center;
    	}
    </style>
</head>
<body>
<%@include file="/header/dict.jsp" %>
 <%@include file="/menu/dict.jsp" %>
  <section id="m-main" class="col-md-10" style="padding-top:65px;">
        <div class="panel panel-default">
            <div class="panel-heading">
                <i class="glyphicon glyphicon-list"></i> 编辑
            </div>
            <div class="panel-body">
                <form id="linkForm" name="dictTypeForm" method="post"
                      action="dict-business-detail-link-save.do" class="form-horizontal">
                     <div class="form-group">
                   		<label class="col-md-2" style="float:left;">细分名称：</label>
	                     <div class="col-sm-2">
                        	<input type="hidden" name="businessDetailId" value="${business.id}"/>
                            ${business.busiDetail}
                        </div>
                   		<label class="col-md-2" style="float:left;">查询条件请配置:</label>
	                     <div class="col-sm-2">
	                     	<input id="condition" type="hidden" value="${condition}"/>
                            <font color="red">${condition}</font>
                        </div>
                    </div>
                     <font color="red" style="margin-left:30px;">提示：</font><br/>
                     <font color="red" style="margin-left:50px;">1.审批节点的选择请按着流程审批顺序进行选择</font><br/>
                     <font color="red" style="margin-left:50px;">2.查询条件中不需要输入引号</font><br/>
                     <font color="red" style="margin-left:50px;">3.area(==1 隶属大区 == 0 非大区)</font><br/>
                     <font color="red" style="margin-left:50px;">4.position(>=3 经理及以上 <3 经理以下)</font><br/>
                     <font color="red" style="margin-left:50px;">5.money(>=3000 金额3千及以上  <3000 金额3千以下)</font>
               		<table id="linkTable" width="100%">
               			<thead>
	               			<tr><td colspan="6">审批环节设置</td></tr>
	               			<tr>
	               				<td class="head" style="width:5%;">序号</td>
	               				<td class="head" style="width:15%;">名称</td>
	               				<td class="head" style="width:30%;">查询条件</td>
	               				<td class="head" style="width:35%;">审批节点</td>
	               				<td class="head" style="width:5%;">操作</td>
	               				<td class="head" style="width:10%;">备注</td>
	               			</tr>
               			</thead>
               			<tbody id="resultBody">
               				<c:forEach items="${branchApprovalLinkEntitys}" var="branchApprovalLinkEntity" varStatus="resultIndex">
               					<input type="hidden" name="id" value="${branchApprovalLinkEntity.id}"/>
               					<tr id="<c:out value='${resultIndex.index+1}'/>">
               						<td class='head'><c:out value='${resultIndex.index+1}'/></td>
               						<td><input type='text' name='conditionName' style='width:100%;border:0px solid gray;' value='${branchApprovalLinkEntity.conditionName}'/></td>
               						<td><input type='text' name='conditionType' style='width:100%;border:0px solid gray;' value='${branchApprovalLinkEntity.conditionType}'></td>
               						<td><input id='link<c:out value='${resultIndex.index+1}'/>' readOnly type='text' name='conditionNode' style='width:90%;border:0px solid gray;' value='${branchApprovalLinkEntity.conditionNode}'><a href='javascript:' onclick='chooseNode("${business.id}","<c:out value='${resultIndex.index+1}'/>")'>选择</a></td>
               						<td class='head'><button id='button<c:out value='${resultIndex.index+1}'/>' type='button' onclick='delRows(this.id)' class='btn btn-default'>删除</button></td>
               						<td><input type='text' name='note' style='width:100%;border:0px solid gray;' value='${branchApprovalLinkEntity.note}'/></td>
               					</tr>
               				</c:forEach>
               			</tbody>
               			<tfoot>
               			</tfoot>
               		</table><br/>
                   <button type="button" onclick="addRows()" class="btn btn-default">新增行</button>
                    <div class="form-group">
                        <div class="col-md-5" style="margin:0 auto;width:100%;text-align:center;">
                            <button type="button" onclick="linkSave()"  class="btn btn-default">提交</button>&emsp;
                            <button type="button" class="btn a-cancel" onclick="history.back();">返回</button>
                        </div>
                    </div>
                    <input id="linkResult" type="hidden" name="linkResult">
                </form>
             </div>
        </div>
    </section>
<script  type="text/javascript">
	var config={
			chooseNodeUrl:"${tenantPrefix}/dict/dict-business-detail-node-choose.do"
	}
	function addRows(){
		var condition = "";
		var conditionVal = $("#condition").val();
		if(conditionVal.indexOf("area") >= 0){
			condition += "area==1";
		}
		if(conditionVal.indexOf("position") >= 0){
			if(condition != ""){
				condition += "&&";
			}
			condition += "position>=3";
		}
		if(conditionVal.indexOf("money") >= 0){
			if(condition != ""){
				condition += "&&";
			}
			condition += "money>=3000";
		}
		var i = 1;
		$("#resultBody tr:last").each(function(){
			i = parseInt(this.id) + parseInt(1);
		});
		var html = "<tr id="+i+">"
				   +"<td class='head'>"+i+"</td>"
				   +"<td><input type='text' name='conditionName' style='width:100%;height:35px;border:0px solid gray;'/></td>"
				   +"<td><input type='text' name='conditionType' style='width:100%;height:35px;border:0px solid gray;' value='"+condition+"'></td>"
				   +"<td><input id=link"+i+" readOnly type='text' name='conditionNode' style='width:90%;height:35px;border:0px solid gray;'><a href='javascript:' onclick='chooseNode("+${business.id}+","+i+")'>选择</a></td>"
				   +"<td class='head'><button id=button"+i+" type='button' onclick='delRows(this.id)' class='btn btn-default'>删除</button></td>"
				   +"<td><input type='text' name='note' style='width:100%;height:35px;border:0px solid gray;'/></td>"
				   +"</tr>";
	   $("#resultBody").append(html);
	}
	function delRows(id){
		if(!confirm("确认删除该流程环节吗？")){
			return false;
		}
		id = id.substring(6);
		$("#"+id).remove();
	}
	function linkSave(){
		var i = 0;
		$("#resultBody tr").each(function(){
			i++;
		});
		$("#linkResult").val(i);
		var check = 0;
		var conditionNames = document.getElementsByName("conditionName");
		var conditionTypes = document.getElementsByName("conditionType");
		var conditionNodes = document.getElementsByName("conditionNode");
		for(var j=0;j<i;j++){
			if(conditionNames[j].value.replace(/(^\s*)|(\s*$)/g, "") == ""){
				alert("名称请填写完整！");
				check = 1;
				break;
			}
			if(conditionTypes[j].value.replace(/(^\s*)|(\s*$)/g, "") == ""){
				alert("查询条件请填写完整！");
				check = 1;
				break;
			}
			if(conditionNodes[j].value.replace(/(^\s*)|(\s*$)/g, "") == ""){
				alert("审批节点请填写完整！");
				check = 1;
				break;
			}
		}
		if(check == 1){
			return false;
		}
		$("#linkForm").submit();
	}
</script>
</body>
</html>



