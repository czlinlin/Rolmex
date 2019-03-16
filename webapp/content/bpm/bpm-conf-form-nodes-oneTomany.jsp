<%@page contentType="text/html;charset=UTF-8"%>
<%@include file="/taglibs.jsp"%>
<%pageContext.setAttribute("currentHeader", "bpm-console");%>
<%pageContext.setAttribute("currentMenu", "bpm-category");%>
<%pageContext.setAttribute("currentMenuName", "流程管理");%>
<%pageContext.setAttribute("currentChildMenu", "流程配置");%>
<!doctype html>
<html lang="en">
  <head>
    <%@include file="/common/meta.jsp"%>
    <title><spring:message code="dev.bpm-conf-form.list.title" text="麦联"/></title>
    <%@include file="/common/s3.jsp"%>
    <script type="text/javascript">
	    function chooseForm(val){
	    	$("#formValue").val(val);
	    }
	    function submitOperation(){
	    	var formVal = $("#formValue").val();
	    	var nodes = $("input[type='checkbox']:checked").val();
	    	if(formVal == "" || nodes == undefined){
	    		alert("请将数据填写完整！");
	    		return false;
	    	}else{
	    		$("#bpmCategoryForm").submit();
	    	}
	    }
    </script>
  </head>
  <body>
    <%@include file="/header/bpm-console.jsp"%>
    <div class="row-fluid">
	  <%@include file="/menu/bpm-console.jsp"%>
	  <!-- start of main -->
      <section id="m-main" class="col-md-10" style="padding-top:65px;">
		<ul class="breadcrumb">
	    <li><a href="bpm-process-list.do">流程配置</a></li>
	    <li><a href="bpm-conf-node-list.do?bpmConfBaseId=${bpmConfBaseId}">${bpmConfBase.processDefinitionKey}</a></li>
	    <li class="active">${bpmConfNode.name}表单</li>
	  </ul>
<div class="panel panel-default">
  <div class="panel-heading">
	<i class="glyphicon glyphicon-list"></i>
    返回
	<div class="pull-right ctrl">
	  <a class="btn btn-default btn-xs"><i id="bpm-confenerSearchIcon" class="glyphicon glyphicon-chevron-up"></i></a>
    </div>
  </div>
  <div class="panel-body">
	<a class="btn btn-default" href="bpm-conf-node-list.do?bpmConfBaseId=${bpmConfBaseId}">返回</a>
  </div>
</div>

<div class="panel panel-default">
  <div class="panel-heading">
	<i class="glyphicon glyphicon-list"></i>
   批量挂表单
	<div class="pull-right ctrl">
	  <a class="btn btn-default btn-xs"><i id="bpm-conf-formSearchIcon" class="glyphicon glyphicon-chevron-up"></i></a>
    </div>
  </div>
  <div class="panel-body">
		  <form id="bpmCategoryForm" name="bpmCategoryForm" method="post" action="bpm-conf-form-nodes-save.do" class="form-inline">
			<label class="control-label col-md-1" for="value">表单:</label>
			<div class="col-sm-6">
				<span id ="divSelect" style="display:block">
					<input type="hidden" name="bpmConfBaseId" value="${bpmConfBaseId}">
					<input type="hidden" id="formValue" name="formValue">
					<select id="outValue" name="outValue" style="width:100%;height:30px;" onchange="chooseForm(this.value)">
						<option value="">请选择</option>
						<option value="operation/common-operation">常规非常规-发起</option>
						<option value="operation/common-operation-confirm">常规非常规-审批</option>
						<option value="operation/common-operation-modify">常规非常规-重新调整</option>
						<option value="operation/cancel-order">撤单-发起</option>
						<option value="operation/cancel-order-confirm">撤单-审批</option>
						<option value="operation/cancel-order-modify">撤单-重新调整</option>
						<option value="operation/process-operation-onlineAudit">直销OA审批</option>
						<option value="operation/pinzhi365-operation-confirm">品质365审批</option>
						<option value="operation/process/GroupBusinessApplyForm">业务申请-发起</option>
						<option value="operation/process/GroupBusinessApprovalForm">业务申请-审批</option>
						<option value="operation/process/GroupBusinessAdjustmentForm">业务申请-重新调整</option>
						<option value="operation/process/BusinessApplyForm">业务申请(分公司)-发起</option>
						<option value="operation/process/BusinessApprovalForm">业务申请(分公司)-审批</option>
						<option value="operation/process/BusinessAdjustmentForm">业务申请(分公司)-重新调整</option>
						<option value="operation/process/ReturnApplyForm">常规退货-发起</option>
						<option value="operation/process/ReturnApprovalForm">常规退货-审批</option>
						<option value="operation/process/ReturnAdjustmentForm">常规退货-重新调整</option>
						<option value="operation/process/InvoiceApplyForm">发票-发起</option>
						<option value="operation/process/InvoiceApprovalForm">发票-审批</option>
						<option value="operation/process/InvoiceAdjustmentForm">发票-重新调整</option>
						<option value="operation/process/FreezeApplyForm">外事冻结/解冻-发起</option>
						<option value="operation/process/FreezeApprovalForm">外事冻结/解冻-审批</option>
						<option value="operation/process/FreezeAdjustmentForm">外事冻结/解冻-重新调整</option>
						<option value="operation/process/LllegalFreezeApplyForm">违规冻结/解冻-发起</option>
						<option value="operation/process/LllegalFreezeApprovalForm">违规冻结/解冻-审批</option>
						<option value="operation/process/LllegalFreezeAdjustmentForm">违规冻结/解冻-重新调整</option>
						<option value="operation/process/AreaBusinessApplyForm">业务申请（大区）-发起</option>
						<option value="operation/process/AreaBusinessApprovalForm">业务申请（大区）-审批</option>
						<option value="operation/process/AreaBusinessAdjustmentForm">业务申请（大区）-重新调整</option>
						<option value="operation/process-operation-onlineAudit">oa推送流程-审批</option>
						<option value="operation/process/ExchangeApplyForm">常规换货-发起</option>
						<option value="operation/process/ExchangeApprovalForm">常规换货-审批</option>
						<option value="operation/process/ExchangeAdjustmentForm">常规换货-重新调整</option>
						<option value="operation/quality-exchange-goods">质量问题换货-发起</option>
						<option value="operation/quality-exchange-confirm">质量问题换货-审批</option>
						<option value="operation/quality-exchange-modify">质量问题换货-重新调整</option>
					</select>
				</span>
				<br/>
				<c:forEach items="${nodeResult}" var="item">
					<input type="checkbox" name="checkNodes" value="${item.id}">&nbsp;${item.name}&nbsp;&nbsp;
				</c:forEach>
			</div>
			<button type="button" class="btn btn-small" onclick="submitOperation()">提交</button>
		  </form>
		</div>
	  </div>
      <div class="m-spacer"></div>
      </section>
	  <!-- end of main -->
	</div>
  </body>
</html>

