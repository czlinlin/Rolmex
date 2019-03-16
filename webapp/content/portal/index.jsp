<%@page contentType="text/html;charset=UTF-8"%>
<%@include file="/taglibs.jsp"%>
<%pageContext.setAttribute("currentHeader", "dashboard");%>
<%pageContext.setAttribute("currentMenu", "dashboard");%>
<%pageContext.setAttribute("currentMenuName", "index");%>
<!doctype html>
<html lang="en">

  <head>
    <%@include file="/common/meta.jsp"%>
    <title>麦联</title>
	<%@include file="/common/s3.jsp"%>

    <script src='${cdnPrefix}/portal/dashboard.js' type='text/javascript'></script>
    <link rel='stylesheet' href='${cdnPrefix}/portal/dashboard.css' type='text/css' media='screen' />
    <script type="text/javascript" src="${cdnPrefix}/portal/portal.js"></script>
    
    <link type="text/css" rel="stylesheet" href="${cdnPrefix}/userpicker3/userpicker.css">
    <!-- bootbox -->
    <script type="text/javascript" src="${cdnPrefix}/bootbox/bootbox.min1.js"></script>
	<style>
		.safesure td
        {
            border: 1px solid #CCCCCC;
            border-collapse: collapse;
            width: 50px;
            height: 15px;
        }
	</style>
  </head>

  <body>
    <%@include file="/header/portal.jsp"%>
  
    <div data-height="300" class="container-fluid dashboard dashboard-draggable" id="dashboard" style="margin-top:70px;">
      <header></header>
      <section class="row">
<c:forEach items="${map}" var="entry">
        <div class="portal-col col-md-4 col-sm-6" data-id="${entry.key}" data-order="${entry.key}">
  <c:forEach items="${entry.value}" var="item">
		<div data-id="${item.id}" class="portlet" data-order="${item.rowIndex}">
          <div data-url="${tenantPrefix}${item.portalWidget.url}" class="panel panel-default" id="panel${item.id}" data-id="${item.id}">
            <div class="panel-heading">
              <%-- <div class="panel-actions">
                <button class="btn btn-sm refresh-panel">
                	
                	<i class="glyphicon glyphicon-refresh"></i>
                </button>
                
                <div class="dropdown">
                  <button data-toggle="dropdown" class="btn btn-sm" role="button"><span class="caret"></span></button>
                  <ul aria-labelledby="dropdownMenu1" role="menu" class="dropdown-menu">
                    <li><a href="javascript:void(0);updateWidget(${item.id}, ${item.portalWidget.id}, '${item.name}')"><i class="glyphicon glyphicon-pencil"></i> 编辑</a></li>
                    <li><a class="remove-panel" href="#"><i class="glyphicon glyphicon-remove"></i> 移除</a></li>
                  </ul>
                </div> 
              </div> --%>
		      <i class="glyphicon glyphicon-list"></i> ${item.name}
            </div>
            <div class="panel-body">
		      <table class="table table-hover">
			    <thead>
			      <tr>
				    <th>编号</th>
				    <th>名称</th>
				    <th>创建时间</th>
				    <th>&nbsp;</th>
			      </tr>
			    </thead>
			    <tbody>
			    <c:forEach items="${personalTasks.result}" var="item">
			      <tr>
				    <td>${item.id}</td>
				    <td>${item.name}</td>
				    <td><fmt:formatDate value="${item.createTime}" pattern="yyyy-MM-dd HH:mm:ss" /></td>
				    <td>
				      <a href="${tenantPrefix}/operation/task-operation-viewTaskForm.do?humanTaskId=${item.id}" class="btn btn-xs btn-primary">处理</a>
				    </td>
			      </tr>
			    </c:forEach>
			    </tbody>
		      </table>
            </div>
          </div>
        </div>
  </c:forEach>
		</div>
</c:forEach>

      </section>
    </div>

<div id="widgetModal" class="modal fade">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
        <h4 class="modal-title">编辑组件</h4>
      </div>
      <div class="modal-body">
	    <form id="widgetForm" action="save.do" method="post">
		  <input id="portalItemId" type="hidden" name="id" value="">
		  <div class="form-group">
			<label for="portalWidgetId">组件</label>
		    <select id="portalWidgetId" class="form-control" name="portalWidgetId">
		      <c:forEach items="${portalWidgets}" var="item">
			  <option value="${item.id}">${item.name}</option>
			  </c:forEach>
		    </select>
		  </div>
		  <div class="form-group">
			<label for="portalItemName">标题</label>
		    <input id="portalItemName" class="form-control" type="text" value="" name="portalItemName">
		  </div>
		</form>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
        <button type="button" class="btn btn-primary" onclick="$('#widgetForm').submit();">保存</button>
      </div>
    </div>
  </div>
  <div style="display:none" id="divChangePwd">
  	<table style="width:80%">
  		<tr>
  			<td style="width:20%"></td>
  			<td style="width:80%"><div id="divMsg" style="margin:0 5px;color:red;"></div></td>
  		</tr>
  		<tr>
  			<td style="width:20%">&emsp;原密码：</td>
  			<td style="width:80%"><input id="priPwd" type="password"  onpaste="return false" oncopy="return false" oncut="return false"  style="margin:0 5px;"  maxlength="20"/></td>
  		</tr>
  		<tr>
  			<td>&emsp;新密码：</td>
  			<td><input id="newPwd" onkeyup="checkPwd(this)"  onpaste="return false" oncopy="return false" oncut="return false" type="password" style="margin:0 5px;"  maxlength="20"/></td>
  		</tr>
  		<tr>
  			<td>确认密码：</td>
  			<td><input id="confirmPwd" type="password"  onpaste="return false" oncopy="return false" oncut="return false" style="margin:0 5px;"  maxlength="20"/></td>
  		</tr>
  		<tr>
	        <td class="tdl">安全性：</td>
	        <td class="tdr" style=" width:100px;height:40px;">
	            <table cellspacing="0" cellpadding="0" style="width:100%;" class="safetable">
	                <tr class="safesure">
	                    <td style="border:1px solid #DEDEDE; height:18px;" id="td1">
	                    </td>
	                    <td style="border:1px solid #DEDEDE; height:18px;" id="td2">
	                    </td>
	                    <td style="border:1px solid #DEDEDE; height:18px;" id="td3">
	                    </td>
	                    <td style="border:0px; height:18px; text-align:center;line-height:18px;" id="safelv"></td>
	                    
	                </tr>
	            </table>
	        </td>
	    </tr>
	</table>
  </div>
</div>

    <div class="text-center">
	  &copy;Rolmex
    </div>
    <script>
    	var changpwd="${isChangePwd}";
    	$(function(){
    		if(changpwd=="1"){
   				fnTaskComment();
   			}
    	})
    	var isSubmit = false;
    	
    	var checkPwd=function(newid){
    		var reg1=/\W+\D+/;
    		var reg2 = /[0-9]/;
            var reg3 = /[a-zA-Z]/;
            var text = trim($(newid).val());
            $(newid).val(trim($(newid).val()));
            if (text.length >= 6) {
                if (reg1.test(text)&&reg2.test(text)&&reg3.test(text)) {
                	 $(".safesure td").css("background-color", "white");
                     $("#td1").css("background-color", "#61D01C");
                     $("#td2").css("background-color", "#61D01C");
                     $("#td3").css("background-color", "#61D01C");
                     $("#safelv").html("强").css("color", "#61D01C");
                     isSubmit = true;
                }
                else if (reg1.test(text)||reg2.test(text)||reg3.test(text)) {
                	if(reg1.test(text)&&reg2.test(text)||
              		   reg3.test(text)&&reg2.test(text)||
              		   reg3.test(text)&&reg1.test(text))
                		{
	                		$(".safesure td").css("background-color", "white");
	                        $("#td1").css("background-color", "#F9C14C");
	                        $("#td2").css("background-color", "#F9C14C");
	                        $("#safelv").html("中").css("color", "#F9C14C");
	                        isSubmit = true;
                		}
                	else{
                		$(".safesure td").css("background-color", "white");
                        $("#td1").css("background-color", "#FD2F2F");
                        $("#safelv").html("弱").css("color", "#FD2F2F");
                	}
                    
                }
                else if (text.length >= 16 && text.length <= 20) {
                	$(".safesure td").css("background-color", "white");
                    $("#td1").css("background-color", "#FD2F2F");
                    $("#safelv").html("弱").css("color", "#FD2F2F");
                }
            }
            else {
                $(".safesure td").css("background-color", "white");
                $("#safelv").html("");
            }
    	}
    	
    function trim(text) {
                return text.replace(/(^\s*)|(\s*$)/g, "");
            }
    	
   	var fnTaskComment = function () {
    var html=$("#divChangePwd").html();
    $("#divChangePwd").remove();
    var dialog = bootbox.dialog({
    	closeButton: false,
        title: "<font color='red'>您的密码过于简单，必须修改密码后才可以使用！</font>",
        message:html,
        buttons: {
            noclose: {
                label: '提交',
                className: 'btn-primary',
                callback: function () {
                	$("#divMsg").html("");
                    var priPwd = $("#priPwd").val();
                    if (priPwd == "") {
                    	$("#divMsg").html("*请输入原密码");
                        return false;
                    }
                    var newPwd = $("#newPwd").val();
                    if (newPwd == "") {
                    	$("#divMsg").html("*请输入新密码");
                        return false;
                    }
                    var confirmPwd = $("#confirmPwd").val();
                    if (confirmPwd == "") {
                        $("#divMsg").html("*请输入确认密码");
                        return false;
                    }
                    if (!isSubmit) {
                    	$("#divMsg").html("*密码强度太弱，请重新设置");
                        return false;
                    }
                    var loading = bootbox.dialog({
                        message: '<p>提交中...</p>',
                        closeButton: false
                    });
                    
                    $.post("${tenantPrefix}/rs/user/person-edit-defaultkey",
                    		{oldPassword: priPwd, 
                    		 newPassword: newPwd,
                    		 confirmPassword:confirmPwd
                    		 }, function (data) {
                        loading.modal('hide')
                        if (data == undefined || data == null || data == "") {
                            bootbox.alert("提交错误，请联系管理员！！！");
                            return false;
                        }
                        
                        if (data.code == 200) {
                            dialog.modal('hide')
                            bootbox.alert(data.message);
                            return;
                        }
                        else{
                        	bootbox.alert(data.message);
                        	return false;
                        }
                    })
                    return false;
                }
            }
        },
        callback: function (result) {
            alert(result);
            return;
        },
        show: true
    });
}
    </script>
  </body>

</html>
