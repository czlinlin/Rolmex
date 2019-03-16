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
	    $(function(){
			$(":radio").click(function(){
	    	   if ($(this).val() == "0") {
	    		   $("#inputSelect").show();
	    		   $("#divSelect").hide();
	    	   } else {
	    		   $("#inputSelect").hide();
	    		   $("#divSelect").show();
	    	   }
	    	});
		});
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
	    <li class="active">${bpmConfNode.name}-表单</li>
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
    添加
	<div class="pull-right ctrl">
	  <a class="btn btn-default btn-xs"><i id="bpm-conf-formSearchIcon" class="glyphicon glyphicon-chevron-up"></i></a>
    </div>
  </div>
  <div class="panel-body">

		  <form name="bpmCategoryForm" method="post" action="bpm-conf-form-save.do" class="form-inline">
			<input type="hidden" name="bpmConfNodeId" value="${param.bpmConfNodeId}">
			<label class="control-label col-md-1" for="value">表单:</label>
			<div class="col-sm-6">
				<span id ="inputSelect"><input type="text" id="value" name="value" value="" style="width:100%"></span>
				<span id ="divSelect" style="display:none">
					<select id="outValue" name="outValue" style="width:100%">
						<option value="">请选择</option>
						<option value="operation/common-operation">常规非常规-发起</option>
						<option value="operation/common-operation-confirm">常规非常规-审批</option>
						<option value="operation/common-operation-modify">常规非常规-重新调整</option>
						<option value="operation/cancel-order">撤单-发起</option>
						<option value="operation/cancel-order-confirm">撤单-审批</option>
						<option value="operation/cancel-order-modify">撤单-重新调整</option>
						<!-- <option value="operation/custom-apply-list">自定义-发起</option>
						<option value="operation/custom-apply-confirm">自定义-审批</option>
						<option value="operation/custom-apply-modify">自定义-重新调整</option> -->
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
<!-- 						<option value="operation/process/ExchangeApprovalForm">质量问题换货-审批</option> -->
						<option value="operation/quality-exchange-confirm">质量问题换货-审批</option>
<!-- 						<option value="operation/process/QualityProblemExchangeAdjustmentForm">质量问题换货-重新调整</option> -->
						<option value="operation/quality-exchange-modify">质量问题换货-重新调整</option>
						<option value="operation/car-apply-user-list">用车申请-发起</option>
						<option value="operation/car-apply-confirm">用车申请-普通审批</option>
						<option value="operation/car-apply-driver-step-start">用车申请-行政部司机step-start</option>
						<option value="operation/car-apply-driver-step-end">用车申请-行政部司机step-end</option>
						<option value="operation/car-user-list-modify">用车申请-重新调整</option>
					</select>
				</span>
			</div>
		    <div class="col-sm-5">
		     <label for="type0">类型:</label>
		    <input type="radio" id="type0" name="type" value="0" checked>
		    <label for="type0">内部</label>
		    <input type="radio" id="type1" name="type" value="1">
		    <label for="type1">外部</label>
			<button class="btn btn-small" onclick="document.bpmCategoryForm.submit()">提交</button>
			</div>
		  </form>

		</div>
	  </div>

<form id="bpm-conf-formGridForm" name="bpm-conf-formGridForm" method='post' action="bpm-conf-form-remove.do" class="m-form-blank">
      <div class="panel panel-default">
        <div class="panel-heading">
		  <i class="glyphicon glyphicon-list"></i>
		  <spring:message code="scope-info.scope-info.list.title" text="列表"/>
		</div>


    <input type="hidden" name="bpmTaskDefId" value="${bpmTaskDefId}">
    <table id="bpmCategoryGrid" class="table table-hover">
      <thead>
        <tr>
          <th width="10" style="text-indent:0px;text-align:center;"><input type="checkbox" name="checkAll" onchange="toggleSelectedItems(this.checked)"></th>
          <th class="sorting" name="id"><spring:message code="user.bpmCategory.list.id" text="编号"/></th>
          <th class="sorting" name="name"><spring:message code="user.bpmCategory.list.name" text="名称"/></th>
          <th class="sorting" name="priority">类型</th>
          <th class="sorting" name="status">状态</th>
          <th width="100">&nbsp;</th>
        </tr>
      </thead>
      <tbody>
        <c:forEach items="${bpmConfForms}" var="item">
        <tr>
          <td><input type="checkbox" class="selectedItem a-check" name="selectedItem" value="${item.id}"></td>
          <td>${item.id}</td>
          <td>${item.value}</td>
          <td>${item.type == 0 ? '电子表单' : '外部表单'}</td>
          <td>
		    <c:if test="${item.status == 0}">默认</c:if>
		    <c:if test="${item.status == 2}">删除</c:if>
		    <c:if test="${item.status == 1}">修改</c:if>
		  </td>
          <td>
		    <a href="bpm-conf-form-remove.do?id=${item.id}">删除</a>
          </td>
        </tr>
        </c:forEach>
      </tbody>
    </table>


      </div>
</form>

      <div class="m-spacer"></div>

      </section>
	  <!-- end of main -->
	</div>

  </body>

</html>

