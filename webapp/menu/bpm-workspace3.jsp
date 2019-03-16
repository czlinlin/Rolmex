<%@ page language="java" pageEncoding="UTF-8" %>
      <!-- start of sidebar -->
<style type="text/css">
#accordion .panel-heading {
	cursor: pointer;
}
#accordion .panel-body {
	padding:0px;
}
</style>

<div class="panel-group col-md-2" id="accordion" role="tablist" aria-multiselectable="true" style="padding-top:65px;">

  <div class="panel panel-default">
    <div class="panel-heading" role="tab" id="collapse-header-bpm-process" data-toggle="collapse" data-parent="#accordion" href="#collapse-body-bpm-process" aria-expanded="true" aria-controls="collapse-body-bpm-process">
      <h4 class="panel-title">
	    <i class="glyphicon glyphicon-list"></i>
        我的流程
      </h4>
    </div>
    <div id="collapse-body-bpm-process" class="panel-collapse collapse ${currentMenu == 'bpm-process' ? 'in' : ''}" role="tabpanel" aria-labelledby="collapse-header-bpm-process">
      <div class="panel-body">
        <ul class="nav nav-pills nav-stacked">
		  <li class = "${currentChildMenu == '发起流程' ? 'active' : ''}"><a href="${tenantPrefix}/bpm/workspace-home.do"><i class="glyphicon glyphicon-list ${currentChildMenu == '发起流程' ? 'active' : ''}"></i> 发起流程</a></li>
		  <li class = "${currentChildMenu == '未结流程' ? 'active' : ''}"><a href="${tenantPrefix}/bpm/workspace-listRunningProcessInstances.do"><i class="glyphicon glyphicon-list ${currentChildMenu == '未结流程' ? 'active' : ''}"></i> 未结流程</a></li>
		  <li class = "${currentChildMenu == '办结流程' ? 'active' : ''}"><a href="${tenantPrefix}/bpm/workspace-listCompletedProcessInstances.do"><i class="glyphicon glyphicon-list ${currentChildMenu == '办结流程' ? 'active' : ''}"></i> 办结流程</a></li>
		  <%--
		  <li><a href="${tenantPrefix}/bpm/workspace-listInvolvedProcessInstances.do"><i class="glyphicon glyphicon-list"></i> 参与的流程</a></li>
		  --%>
		  <li class = "${currentChildMenu == '草稿箱' ? 'active' : ''}"><a href="${tenantPrefix}/operation/process-operation-listDrafts.do"><i class="glyphicon glyphicon-list ${currentChildMenu == '草稿箱' ? 'active' : ''}"></i> 草稿箱</a></li>
        </ul>
      </div>
    </div>
  </div>

  <div class="panel panel-default">
    <div class="panel-heading" role="tab" id="collapse-header-task" data-toggle="collapse" data-parent="#accordion" href="#collapse-body-task" aria-expanded="true" aria-controls="collapse-body-task">
      <h4 class="panel-title">
	    <i class="glyphicon glyphicon-list"></i>
        我的任务
      </h4>
    </div>
    <div id="collapse-body-task" class="panel-collapse collapse ${currentMenu == 'task' ? 'in' : ''}" role="tabpanel" aria-labelledby="collapse-header-task">
      <div class="panel-body">
        <ul class="nav nav-pills nav-stacked">
		  <li class = "${currentChildMenu == '待办任务' ? 'active' : ''}"><a href="${tenantPrefix}/humantask/workspace-personalTasks.do"><i class="glyphicon glyphicon-list ${currentChildMenu == '草稿箱' ? 'active' : ''}"></i> 待办任务</a></li>
		  <li class = "${currentChildMenu == '待领任务' ? 'active' : ''}"><a href="${tenantPrefix}/humantask/workspace-groupTasks.do"><i class="glyphicon glyphicon-list ${currentChildMenu == '待领任务' ? 'active' : ''}"></i> 待领任务</a></li>
		  <li class = "${currentChildMenu == '已办任务' ? 'active' : ''}"><a href="${tenantPrefix}/humantask/workspace-historyTasks.do"><i class="glyphicon glyphicon-list ${currentChildMenu == '已办任务' ? 'active' : ''}"></i> 已办任务</a></li>
		  <li class = "${currentChildMenu == '经手任务' ? 'active' : ''}"><a href="${tenantPrefix}/humantask/workspace-delegatedTasks.do"><i class="glyphicon glyphicon-list ${currentChildMenu == '经手任务' ? 'active' : ''}"></i> 经手任务</a></li>
        </ul>
      </div>
    </div>
  </div>

  <div class="panel panel-default">
    <div class="panel-heading" role="tab" id="collapse-header-delegate" data-toggle="collapse" data-parent="#accordion" href="#collapse-body-delegate" aria-expanded="true" aria-controls="collapse-body-delegate">
      <h4 class="panel-title">
	    <i class="glyphicon glyphicon-list"></i>
        规则设置
      </h4>
    </div>
    <div id="collapse-body-delegate" class="panel-collapse collapse ${currentMenu == 'bpm-delegate' ? 'in' : ''}" role="tabpanel" aria-labelledby="collapse-header-delegate">
      <div class="panel-body">
        <ul class="nav nav-pills nav-stacked">
		  <li class = "${currentChildMenu == '代理规则' ? 'active' : ''}"><a href="${tenantPrefix}/delegate/delegate-listMyDelegateInfos.do"><i class="glyphicon glyphicon-list ${currentChildMenu == '代理规则' ? 'active' : ''}"></i> 代理规则</a></li>
		  <!-- <li class = "${currentChildMenu == '常用语' ? 'active' : ''}"><a href="${tenantPrefix}/pim/pim-phrase-my-list.do"><i class="glyphicon glyphicon-list ${currentChildMenu == '常用语' ? 'active' : ''}"></i> 常用语</a></li> -->
        </ul>
      </div>
    </div>
  </div>

</div>

      <!-- end of sidebar -->

