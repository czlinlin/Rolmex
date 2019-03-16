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
        个人信息
      </h4>
    </div>
    <div id="collapse-body-bpm-process" class="panel-collapse collapse ${currentMenu == 'my' ? 'in' : ''}" role="tabpanel" aria-labelledby="collapse-header-bpm-process">
      <div class="panel-body">
        <ul class="nav nav-pills nav-stacked">
		  <li class = "${currentChildMenu == '个人信息' ? 'active' : ''}"><a href="${tenantPrefix}/user/my-info-input.do"><i class="glyphicon glyphicon-list ${currentChildMenu == '个人信息' ? 'active' : ''}"></i> 个人信息</a></li>
		  <li class = "${currentChildMenu == '个人流程设置' ? 'active' : ''}"><a href="${tenantPrefix}/user/my-custom-info-setting-list.do"><i class="glyphicon glyphicon-list ${currentChildMenu == '个人流程设置' ? 'active' : ''}"></i> 个人流程设置</a></li>
		  <!-- <li class = "${currentChildMenu == '修改头像' ? 'active' : ''}"><a href="${tenantPrefix}/user/my-avatar-input.do"><i class="glyphicon glyphicon-list ${currentChildMenu == '修改头像' ? 'active' : ''}"></i> 修改头像</a></li> -->
		  <li class = "${currentChildMenu == '修改密码' ? 'active' : ''}"><a href="${tenantPrefix}/user/my-change-password-input.do"><i class="glyphicon glyphicon-list ${currentChildMenu == '修改密码' ? 'active' : ''}"></i> 修改密码</a></li>
		  <li class = "${currentChildMenu == '修改私钥' ? 'active' : ''}"><a href="${tenantPrefix}/user/my-change-operationpassword-input.do"><i class="glyphicon glyphicon-list ${currentChildMenu == '修改私钥' ? 'active' : ''}"></i> 修改私钥</a></li>
		  <li class = "${currentChildMenu == '设备管理' ? 'active' : ''}"><a href="${tenantPrefix}/user/my-device-list.do"><i class="glyphicon glyphicon-list ${currentChildMenu == '设备管理' ? 'active' : ''}"></i> 设备管理</a></li>
          <%--  <li class = "${currentChildMenu == '重置密码' ? 'active' : ''}"><a href="${tenantPrefix}/user/person-info-reset.do"><i class="glyphicon glyphicon-list ${currentChildMenu == '重置密码' ? 'active' : ''}"></i> 重置密码</a></li>--%>
        </ul>
      </div>
    </div>
  </div>
		<footer id="m-footer" class="text-center">
		  <hr>
		  &copy;Rolmex
		</footer>
</div>

      <!-- end of sidebar -->
