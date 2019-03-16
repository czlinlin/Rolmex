<%--
  
  User: wanghan
  Date: 2017\11\3 0003
  Time: 16:33
 
--%>
<%@page contentType="text/html;charset=UTF-8" %>
<%@include file="/taglibs.jsp" %>
<%
    pageContext.setAttribute("currentHeader", "person");
%>
<%
    pageContext.setAttribute("currentMenu", "person");
%>
<%
    pageContext.setAttribute("currentMenuName", "人事管理");
%>
<!doctype html>
<html lang="en">

<head>
    <%@include file="/common/meta.jsp" %>
    <title><spring:message code="dev.employee-info.list.title"
                           text="麦联"/></title>
    <%@include file="/common/s3.jsp" %>
    <script type="text/javascript"
            src="${cdnPrefix}/bootbox/bootbox.min1.js"></script>
    <script type="text/javascript">
        var config = {
            id: 'person-infoGrid',
            pageNo: ${page.pageNo},
            pageSize: ${page.pageSize},
            totalCount: ${page.totalCount},
            resultSize: ${page.resultSize},
            pageCount: ${page.pageCount},
            orderBy: '${page.orderBy == null ? "" : page.orderBy}',
            asc: ${page.asc},
            params: {
                'filter_LIKES_FULL_NAME': '${param.filter_LIKES_FULL_NAME}',
                'filter_LIKES_REAL_NAME': '${param.filter_LIKES_REAL_NAME}',
                'filter_LIKES_EMPLOYEE_NO': '${param.filter_LIKES_EMPLOYEE_NO}',
                'filter_EQS_QUIT_FLAG': '${param.filter_EQS_QUIT_FLAG}',
                 'partyStructTypeId': '${partyStructTypeId}',
                'partyEntityId': '${partyEntityId}',
                'isSearch': '${isSearch}',
                'filter_LIKES_USERNAME':'${param.filter_LIKES_USERNAME}'
            },
            selectedItemClass: 'selectedItem',
            gridFormId: 'person-infoGridForm',
            exportUrl: 'person-info-export.do',
            resetUrl: "${tenantPrefix}/rs/user/person-info-reset",
            resetKeyUrl: "${tenantPrefix}/rs/user/person-info-resetkey",
            setAttendanceUrl:"${tenantPrefix}/rs/user/person-info-setattendance",
            getAttendanceUrl:"${tenantPrefix}/rs/user/person-info-getattendance"
        };

        var table;

        $(function () {
            table = new Table(config);
            table.configPagination('.m-pagination');
            table.configPageInfo('.m-page-info');
            table.configPageSize('.m-page-size');
        });

        var isSubmit = false;
        // 查询
        function searchInfo() {
        	
        	$('#person-info_quit').val("0");
            $('#isSearch').val("1");
            $('#person-infoForm').attr('action', '${tenantPrefix}/user/person-info-list-i.do?');
            $('#person-infoForm').submit();
        }
        
        //查询离职人员
        function searchQuit() {
        	$(".person_quit").show();
			$('#person-info_quit').val("1");
		  	$('#isSearch').val("1");
            $('#person-infoForm').attr('action', '${tenantPrefix}/user/person-info-list-i.do?');
            $('#person-infoForm').submit();
			
		 }
        
        $(function(){
        	$("#trLoading").hide();
        	
        	if($('#person-info_quit').val()=="1")
        		$(".person_quit").show();
        	else
        		$(".person_quit").hide();
        	
        	/* window.parent.$.showMessage($('#m-success-tip-message').html(), {
                position: 'top',
                size: '50',
                fontSize: '20px'
            }); */
            
        	var tipMsg=$('#m-success-tip-message').html();
        	window.parent.bootbox.alert({
                message:tipMsg,
                size: 'large',
                buttons: {
                    ok: {
                        label: "确定"
                    }
                }
            });
        })
        
        // 安全性检验
        var checkPwd = function (newid) {
            var reg1 = /\W+\D+/;
            var reg2 = /[0-9]/;
            var reg3 = /[a-zA-Z]/;
            var text = trim($(newid).val());
            $(newid).val(trim($(newid).val()));
            if (text.length >= 6) {
                if (reg1.test(text) && reg2.test(text) && reg3.test(text)) {
                    $(".safesure td").css("background-color", "white");
                    $("#td1").css("background-color", "#61D01C");
                    $("#td2").css("background-color", "#61D01C");
                    $("#td3").css("background-color", "#61D01C");
                    $("#safelv").html("强").css("color", "#61D01C");
                    isSubmit = true;
                }
                else if (reg1.test(text) || reg2.test(text) || reg3.test(text)) {
                    if (reg1.test(text) && reg2.test(text) ||
                        reg3.test(text) && reg2.test(text) ||
                        reg3.test(text) && reg1.test(text)) {
                        $(".safesure td").css("background-color", "white");
                        $("#td1").css("background-color", "#F9C14C");
                        $("#td2").css("background-color", "#F9C14C");
                        $("#safelv").html("中").css("color", "#F9C14C");
                        isSubmit = true;
                    }
                    else {
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

        function fnReset(id) {
            var html = $("#divChangePwd").html();

            $("#divChangePwd").remove();
            var dialog = bootbox.dialog({
                closeButton: false,
                title: "重置密码",
                message: html,
                buttons: {
                    noclose: {
                        label: '提交',
                        className: 'btn-primary',
                        callback: function () {
                            $("#divMsg").html("");
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
                            if ($("#newPwd").val() != $("#confirmPwd").val()) {
                                $("#divMsg").html("*两次密码输入不一致");
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

                            $.post("${tenantPrefix}/rs/user/person-info-reset",
                                {
                                    id: id,
                                    newPassword: newPwd,
                                    confirmPassword: confirmPwd
                                }, function (data) {
                                    loading.modal('hide')
                                    if (data == undefined || data == null || data == "") {
                                        bootbox.alert("提交错误，请联系管理员！！！");
                                        return false;
                                    }

                                    if (data.code == 200) {
                                        var tip = bootbox.alert(
                                            {
                                                message: data.message,
                                                callback: function () {
                                                    document.getElementById('btn_Search').click();
                                                    tip.modal('hide');
                                                }
                                            });

                                     dialog.modal('hide')
                                   /*      bootbox.alert();*/

                                        return;
                                    }
                                    else {
                                        bootbox.alert(data.message);
                                        return false;
                                    }
                                })
                            return false;
                        }
                    },
                    cancel: {
                        label: '取消',
                        className: 'btn-danger',
                        callback: function () {
                            document.getElementById('btn_Search').click();
                            return;
                        },
                    }
                },
                show: true
            });
        }

        // 重置秘钥
        function resetKey(id) {
            var dialogHtml = '<div><span style="color: red">*</span>新私钥<br/><div class="new">';
            dialogHtml += '<input id="newPassword" type="password" style="width:98%;border:1px solid #ccc;border-radius:4px;padding:6px 12px"></input>';
            dialogHtml += '</div><br/>';
            dialogHtml += '<div><span style="color: red">*</span>确认私钥<br/><div class="new">';
            dialogHtml += '<input id="confirmPassword" type="password" style="width:98%;border:1px solid #ccc;border-radius:4px;padding:6px 12px"></input>';

            dialogHtml += "</div>";

            var dialog = bootbox.dialog({
                title: "重置私钥",
                message: dialogHtml,
                buttons: {
                    noclose: {
                        label: '提交',
                        className: 'btn-primary',
                        callback: function () {
                            if ($("#newPassword").val() < 1) {
                                bootbox.alert("新私钥为必填字段！");
                                return false;
                            }
                            if ($("#confirmPassword").val() != $("#newPassword").val()) {
                                bootbox.alert("两次私钥输入不一致！");
                                return false;
                            }
                            ;
                            var loading = bootbox.dialog({
                                message: '<p>提交中...</p>',
                                closeButton: false
                            });

                            var newPassword = ($("#newPassword").val());
                            $.post(config.resetKeyUrl, {id: id, newPassword: newPassword}, function (data) {
                                loading.modal('hide')
                                if (data == undefined || data == null || data == "") {
                                    bootbox.alert("重置失败");
                                    return false;
                                }
                                if (data.code == 200) {
                                    dialog.modal('hide')
                                    bootbox.alert({
                                        message: data.message, callback: function () {
                                            document.getElementById('btn_Search').click();
                                        }
                                    });
                                }
                                else
                                    bootbox.alert(data.message);

                                return data.code == 200;
                            })
                            return false;
                        }
                    },
                    cancel: {
                        label: '取消',
                        className: 'btn-danger'
                    }
                },
                callback: function (result) {
                    alert(result);
                    return;
                },
                show: true
            });
        }

        function trim(text) {
            return text.replace(/(^\s*)|(\s*$)/g, "");
        }
        
        var dialog=null;
        
        //导出
        function fnGoAction() {
        	if (${page.resultSize}==0) {
                alert("没有数据需要导出！")
                return false;
            }
        	/* dialog =window.parent.bootbox.dialog({
                message: '<p class="text-center"><img alt="正在导出，请稍等..." src="${cdnPrefix}/mossle/img/loading.gif" style="width:24px;height:24px;"/><i class="fa fa-spin fa-spinner"></i>正在导出，请稍等...</p>',
                size: 'small',
                closeButton: false
            }); */
        	
        	//dialog.modal('hide');
        	//return false;
        	
            document.getElementById("person-infoForm").action = "${tenantPrefix}/user/personInfo-export.do";
        }
        
    </script>

    <style type="text/css">
        th {
            white-space: nowrap
        }
        td{
        	white-space: nowrap
        }
    </style>
</head>

<body>

<div class="row-fluid">

    <c:if test="${not empty flashMessages}">
        <div id="m-success-tip-message" style="display: none;">
            <ul>
                <c:forEach items="${flashMessages}" var="item">
                    <c:if test="${item != ''}">
                        <li  style="list-style:none; word-wrap:break-word;">${item}</li>
                    </c:if>
                </c:forEach>
            </ul>
        </div>
    </c:if>
    <!-- start of main -->
    <section id="m-main" class="col-md-12" style="padding-top: 3px;">

        <ul class="breadcrumb">
            <li><a href="person-info-list-i.do">在职员工管理</a></li>
            <li class="active"></li>
        </ul>

        <div class="panel panel-default">
            <div class="panel-heading">
                <i class="glyphicon glyphicon-list"></i> 查询
                <div class="pull-right ctrl">
                    <a class="btn btn-default btn-xs"><i
                            id="employee-infoSearchIcon"
                            class="glyphicon glyphicon-chevron-up"></i></a>
                </div>
            </div>
            <div class="panel-body">
                <form name="person-infoForm" method="post" id="person-infoForm"
                      action="person-info-list-i.do" class="form-inline">
                    <label for="person-info_name"><spring:message
                            code='employee-info.employee-info.list.search.name' text='姓名'/>:</label>
                          <c:if test="${isOpenOtherName=='0'}">
                    <input type="text" id="person-info_name"
                           name="filter_LIKES_FULL_NAME"
                           value="${param.filter_LIKES_FULL_NAME}" class="form-control">
                          </c:if>
                          <c:if test="${isOpenOtherName=='1'}">
                          		<input type="text" id="person-info_name"
			                           name="filter_LIKES_REAL_NAME"
			                           value="${param.filter_LIKES_REAL_NAME}" class="form-control">
                          </c:if>
                     <label for="person-info_name"><spring:message
                            code='employee-info.employee-info.list.search.USERNAME' text='用户名'/>:</label>
                    <input type="text" id="person-info_p.USERNAME"
                           name="filter_LIKES_USERNAME"
                           value="${param.filter_LIKES_USERNAME}" class="form-control">
                           
                     <label for="person-info_employeeno"><spring:message
                            code='employee-info.employee-info.list.search.name' text='工号'/>:</label>
                    <input type="text" id="person-info_name"
                           name="filter_LIKES_EMPLOYEE_NO"
                           value="${param.filter_LIKES_EMPLOYEE_NO}" class="form-control">
                     
                    <input type="hidden" id="partyStructTypeId"
                           name="partyStructTypeId" value="${partyStructTypeId}"
                           class="form-control"> <input type="hidden" id="isSearch"
                                                        name="isSearch" value="${isSearch}" class="form-control">
                    <input id="partyEntityId" type="hidden" name="partyEntityId"
                                   value="${partyEntityId}">
                    <button id="btn_Search" class="btn btn-default a-search"
                            onclick="searchInfo()">查询
                    </button>
                   	&nbsp;
                     <button id="btn_Search_quit"  class="btn btn-default a-search"
                            onclick="searchQuit()">查询离职人员
                    </button>
                    
                   	
                     <input type="hidden" id="person-info_quit"
                           name="filter_EQS_QUIT_FLAG"
                           value="${param.filter_EQS_QUIT_FLAG==null?'0':param.filter_EQS_QUIT_FLAG}" class="form-control">
                    
                    <c:if test="${isSystemAdminRole=='0'}">
                   		&nbsp;
	                    <button id="btn_Export" class="btn btn-default a-search" onclick="fnGoAction()" type="submit">导出
	                    </button>
                   	</c:if>
                     
                </form>
            </div>
        </div>

        <div style="margin-bottom: 20px;">
            <div class="pull-left btn-group" role="group">
                <%-- <c:if test="${viewBtn == true }">
                    <button class="btn btn-default a-insert"
                            onclick="location.href='person-info-input.do?partyEntityId=${partyEntityId}'">新建
                    </button>
                    <button class="btn btn-default a-remove"
                            onclick="table.removeAll()">删除
                    </button>
                        <button class="btn btn-default a-export" onclick="table.exportExcel()">导出</button>
                </c:if> --%>
                
                <%-- <c:if test='${partyTypeId!=null&&partyTypeId!=2}'> --%>
	                <tags:buttonOpteration 
	                            	opterNames="新建" 
	                            	buttonTypes="button" 
	                            	opterTypes="href" 
	                            	opterParams="person-info-input.do?partyEntityId=${partyEntityId}"/>
	            <%-- </c:if> --%>
            </div>

            <div class="pull-right">
				  每页显示
				  <select class="m-page-size form-control" style="display:inline;width:auto;">
				    <option value="10">10</option>
				    <option value="20">20</option>
				    <option value="50">50</option>
				  </select>
				  条
	        </div>

            <div class="clearfix"></div>
        </div>

        <form id="person-infoGridForm" name="person-infoGridForm"
              method='post'
              action="person-info-remove.do?partyEntityId=${partyEntityId}"
              class="m-form-blank">
            <div class="panel panel-default">
                <div class="panel-heading">
                    <i class="glyphicon glyphicon-list"></i>
                    <spring:message code="scope-info.scope-info.list.title" text="列表"/>
                </div>
                <c:if test="${isSystemAdminRole=='0'}">
				<div  style="overflow-x:scroll;">
                <table id="person-infoGrid" class="table table-hover">
                    <thead>
                    <tr>
                    	<th name="name">操作</th>
                        <!-- <th width="30" class="table-check"><input type="checkbox"
                                                                  name="checkAll"
                                                                  onchange="toggleSelectedItems(this.checked)"></th> -->
                        <th name="filter_LIKES_EMPLOYEE_NO"><spring:message
                                code="employee-info.employee-info.list.id" text="工号"/></th>
                                
                         <c:if test="${isOpenOtherName=='1'}">
                         	<th name="name" >  <spring:message code="employee-info.employee-info.list.name" text="别名"/></th>
                         </c:if>
                         <th name="filter_LIKES_FULL_NAME"><spring:message
                                code="employee-info.employee-info.list.name" text="姓名"/></th>     
                           
                        <th name="company">公司</th>
                        <th name="departmentName">部门</th>
                        <th class="sorting" name="name">岗位</th>
                        
                        <th class="sorting" name="name">职位</th>
                                
                        <th name="name"><spring:message
                                code="employee-info.employee-info.list.name" text="用户名"/></th>
                                
                        <th name="level">级别</th>
                        
                                
                       
                        
                        <%--<th name="comment">上级部门</th>--%>
                        
                        
                        <th name="name">状态</th>
                        
                         <th name="sex">性别</th>
                         
                         <th name="nameBefore">曾用名</th>
                         
                         <th name="nativePlace">籍贯</th>
                         
                         <th name="registeredResidence">户口所在地</th>
                         
                         <th name="householdRegisterType">户籍类型</th>
                         
                         <th name="identityID">身份证号</th>
                         
                         <th name="nation">民族</th>
                         
                         <th name="politicalOutlook">政治面貌</th>
                         
                         <th name="major">专业</th>
                         
                         <th name="education">学历</th>
                         
                         <th name="academicDegree">学位</th>
                         
                         <th name="title">职称</th>
                         
                         <th name="skillSpecialty">技能特长</th>
                         
                         <th name="telephone">联系电话</th>
                         
                         <th name="cellphone">备用电话</th>
                         
                       	 <th name="email">邮箱</th>
                         
                         <th name="fax"> 紧急联系人及电话</th>
                          
                        <th name="qq">QQ</th>
                         
                        <th name="wxNo">微信</th>
                         
                        <th name="laborType">用工类型</th>
                         
                        <th name="entryMode">进入方式</th>
                         
                        <th name="entryTime">入职时间</th>
                         
                        <th name="contractCompany">合同单位</th>
                         
                        <th name="contractExpirationTime">合同到期时间</th>
                         
                        <th name="contractDeadline">合同有效期</th>
                         
                        <th name="insurance">保险情况</th>
                         
                        <th name="document">资料情况</th>
                         
                        <th name="work_experience_1">工作经历1</th>
                         
                        <th name="work_experience_2">工作经历2</th>
                         
                        <th name="educational_experience_1">教育经历1</th>
                         
                        <th name="educational_experience_2">教育经历2</th>
                         
                        <th name="educational_experience_3">教育经历3</th>
                         
                        <th name="address"> 现住址</th>
                         
                        <th name="family_1"> 家庭成员1</th>
                        
                        <th name="family_2"> 家庭成员2</th>
                        
                        <th name="marriage"> 婚否</th>
                        
                        <th name="fertilityCondition"> 生育情况</th>
                        
                        <th name="remark"> 备注</th>
                        
                        <th name="name">添加时间</th>
                        
                       	<th name="quitFlag"  style="display:none;" class="person_quit">工资结算日期</th>
                       	
                       	<th name="quitTime"  style="display:none;" class="person_quit">禁止登录系统时间</th>
                    </tr>
                    </thead>

                    <tbody>
                    <tr id="trLoading"><td colspan="11"><img alt="加载中..." src="${cdnPrefix}/mossle/img/loading.gif" style="width:24px;height:24px;"/></td>
                    <c:forEach items="${page.result}" var="item">
                        <tr>
                        	<td>
	                        	<c:if test='${item.quitFlag=="1"}'>
	                        		<tags:buttonOpteration 
	                            	opterNames="编辑" 
	                            	buttonTypes="a" 
	                            	opterTypes="href" 
	                            	opterParams="person-info-input.do?id=${item.id}&partyEntityId=${item.parentId}"/>
	                        	</c:if>
	                        	<c:if test='${item.quitFlag=="0"}'>
	                        		<c:set var="newLockPersons" value=",${lockPersons},"/>
	                        		<c:set var="itemId" value=",${item.id},"/>
	                        		<c:if test="${lockPersons!=''&&(fn:contains(newLockPersons,itemId))}">
	                        			<tags:buttonOpteration 
		                            	opterNames="解锁" 
		                            	buttonTypes="a" 
		                            	opterTypes="href" 
		                            	opterParams="account-lock-info-unlock.do?id=${item.id}&partyEntityId=${partyEntityId}"/>
	                        		</c:if>
	                            	<tags:buttonOpteration 
	                            	opterNames="编辑|考勤编号|调岗|离职|重置密码|重置私钥" 
	                            	buttonTypes="a|a|a||a|a" 
	                            	opterTypes="href|click|href|click|click|click" 
	                            	opterParams="person-info-input.do?id=${item.id}&partyEntityId=${item.parentId}|fnSetAttendance(${item.id},'${item.companyName}','${item.departmentName}','${item.fullName}')|person-info-position-change-i.do?id=${item.id}|quit('${item.id}','${item.parentId}','${item.departmentName}')|fnReset('${item.id}')|resetKey('${item.id}')"/>
                            	</c:if></td>
                            <%-- <td><c:if
                                    test="${item.id != '2' && item.id !='3' && item.id != accountId}">
                                <input type="checkbox" class="selectedItem a-check"
                                       name="selectedItem" value="${item.id}">
                            </c:if></td> --%>
                            <td>${item.employeeNo}</td>
                          		<c:if test="${isOpenOtherName=='1'}">
		                         	<td>${item.fullName}</td> <!-- 别名 -->
	                            	<td><a href="person-info-rosterLogList.do?id=${item.id}">${item.realName}</a></td> <!--真实 姓名 -->
		                         </c:if>
		                         <c:if test="${isOpenOtherName=='0'}">
		                         	<td><a href="person-info-rosterLogList.do?id=${item.id}">${item.fullName}</a></td> <!--真实 姓名 -->
		                         </c:if>
                            <td>${item.companyName}</td> <!-- 公司 -->
                            <td>${item.departmentName}</td> <!-- 部门 -->
                             <td>${item.positionCode}</td>
                           <td>${item.positionName}</td> <!-- 岗位 -->
                            <td>${item.userName}</td> <!-- 用户名 （系统的登录名）-->
                            <td>${item.level.toString().contains("级-")?item.level:(item.level.replace("-","级-"))}</td> <!-- 级别 -->
                                <%--	<td>${item.departmentName}</td>--%>
                            
<%--                             <td><tags:postNameIsDisplay positionName="${item.positionCode}" /></td> --%>
                            
                            <td>
	                            <%-- <c:if test="${item.id != accountId}">
	                                <c:if test="${item.stopFlag=='active'}">
	                                    <span style="color: green;">启用</span>
	                                    (<a href="person-info-disable.do?id=${item.id}&partyEntityId=${item.parentId}">禁用</a>)
	                                </c:if>
	                                <c:if test="${item.stopFlag=='disabled'}">
	                                    <span style="color: red;">禁用</span>
	                                    (<a href="person-info-active.do?id=${item.id}&partyEntityId=${item.parentId}">启用</a>)
	                                </c:if>
	                            </c:if> <c:if test="${item.id == accountId}">
	                                <c:if test="${item.stopFlag=='active'}">
	                                    <span style="color: green;">启用</span>
	                                </c:if>
	                                <c:if test="${item.stopFlag=='disabled'}">
	                                    <span style="color: red;">禁用</span>
	                                </c:if>
	                            </c:if> --%>
	                            <c:if test="${item.stopFlag=='active'}">
                                    <span style="color: green;">启用</span>
                                </c:if>
                                <c:if test="${item.stopFlag=='disabled'}">
                                    <span style="color: red;">禁用</span>
                                </c:if>
                            </td>
                            
                            
                            <td><c:if test="${item.gender == '1'}">男</c:if> <c:if
                                    test="${item.gender == '2'}">女</c:if></td>
                            
                            
                            <td>${item.nameBefore}</td>  <!-- 曾用名 -->
                            
                            <td>${item.nativePlace}</td>  <!-- 籍贯 -->
                            
                            <td>${item.registeredResidence}</td>  <!-- 户口所在地 -->
                            
                            <td>${item.householdRegisterType}</td>  <!-- 户籍类型 -->
                           
                            <td>${item.identityID}</td>  <!-- 身份证号 -->
                            
                            <td>${item.nation}</td>  <!-- 民族 -->
                            
                            <td>${item.politicalOutlook}</td>  <!-- 政治面貌-->
                            <td>${item.major}</td>  <!-- 专业 -->
                            
                            <td>${item.education}</td>  <!-- 学历 -->
                            
                            <td>${item.academicDegree}</td>  <!-- 学位 -->
                            
                            <td>${item.title}</td>  <!-- 职称 -->
                            
                            <td>${item.skillSpecialty}</td>  <!-- 技能特长 -->
                            
                            <td>${item.telephone}</td>  <!-- 联系电话1-->
                            
                            <td>${item.cellphone}</td>  <!-- 联系电话2 -->
                            
                            
                       		<td>${item.email}</td>  <!-- 邮箱 -->
                         	
                        	<td>${item.fax}</td>  <!--  紧急联系人及电话 -->
                         	
                        	<td>${item.qq}</td>  <!--QQ -->
                         	
                        	<td>${item.wxNo}</td>  <!-- 微信 -->
                         	
                        	<td>${item.laborType}</td>  <!-- 用工类型 -->
                         	
                        	<td>${item.entryMode}</td>  <!-- 进入方式 -->
                         	
                        	<td>${item.entryTime}</td>  <!-- 入职时间-->
                         	
                        	<td>${item.contractCompany}</td>  <!-- 合同单位-->
                         	
                        	<td>${item.contractExpirationTime}</td>  <!-- 合同到期时间 -->
                         	
                        	<td>${item.contractDeadline}</td>  <!-- 合同有效期  -->
                         	
                        	<td>${item.insurance}</td>  <!-- 保险情况 -->
                         	
                        	<td>${item.document}</td>  <!-- 资料情况 -->
                         	
                        	<td>${item.work_experience_1}</td>  <!-- 工作经历1-->
                         	
                        	<td>${item.work_experience_2}</td>  <!--工作经历2 -->
                         	
                        	<td>${item.educational_experience_1}</td>  <!-- 教育经历1 -->
                         	
                        	<td>${item.educational_experience_2}</td>  <!-- 教育经历2 -->
                         	
                        	<td>${item.educational_experience_3}</td>  <!-- 教育经历3 -->
                         	
                        	<td>${item.address}</td>  <!--  现住址-->
                         	
                        	<td>${item.family_1}</td>  <!-- 家庭成员1-->
                         	
                        	<td>${item.family_2}</td>  <!-- 家庭成员2 -->
                         	
                        	<td>${item.marriage}</td>  <!-- 婚否 -->
                         	
                        	<td>${item.fertilityCondition}</td>  <!--生育情况 -->
                         	
                        	<td>${item.remark}</td>  <!-- 备注 -->
                         	
                         	<td>${item.addTime}</td>      <!-- 添加日期 -->
                         	<%-- <c:if test='${item.quitFlag==1}'>  --%> 
                       		<td class="person_quit" style="display:none;">${item.leaveDate}</td>
                       		<td class="person_quit" style="display:none;">${item.quitTime}</td>
                         	<%-- </c:if> --%>
                         	            
<%--    通讯录权限                          <td><c:if test="${item.secret=='0'}"> --%> 
<!--                                 公开 -->
<%--                             </c:if> <c:if test="${item.secret=='1'}"> --%>
<!--                                 内部 -->
<%--                             </c:if> <c:if test="${item.secret=='2'}"> --%>
<!--                                 保密 -->
<%--                             </c:if></td> --%>
                            
                            
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
				</div>
                </c:if>
                
                <%--系统管理员，看这里哦，^_^--%>
                <c:if test="${isSystemAdminRole=='1'}">
                	<div  style="overflow-x:scroll;">
	                	<table id="person-infoGrid" class="table table-hover">
	                    <thead>
	                    <tr>
	                    	<th>操作</th>
                    	<c:if test="${isOpenOtherName=='1'}">
                    		<th>别名</th>
                    	</c:if>                    	
	                        <th>姓名</th>
	                        <th>用户名</th>        
	                        <th>公司</th>
	                        <th>部门</th>
	                        <th>岗位</th>
	                        <th>职位</th>
	                        <th>状态</th>
	                        <th>联系电话</th>
	                        <th>邮箱</th>
	                        <th>性别</th>
	                        <th>现住址</th>
	                        <th>备注</th>
	                        <th>添加时间</th>
	                       	<th name="quitFlag"  style="display:none;" class="person_quit">离职时间</th>
	                    </tr>
	                    </thead>
	                    <tbody>
	                    <tr id="trLoading"><td colspan="11"><img alt="加载中..." src="${cdnPrefix}/mossle/img/loading.gif" style="width:24px;height:24px;"/></td>
	                    <c:forEach items="${page.result}" var="item">
	                        <tr>
	                        	<td>
		                        	<c:if test='${item.quitFlag=="1"}'>
		                        		<tags:buttonOpteration 
		                            	opterNames="编辑" 
		                            	buttonTypes="a" 
		                            	opterTypes="href" 
		                            	opterParams="person-info-input.do?id=${item.id}&partyEntityId=${item.parentId}"/>
		                        	</c:if>
		                        	<c:if test='${item.quitFlag=="0"}'>
		                        		<c:set var="newLockPersons" value=",${lockPersons},"/>
		                        		<c:set var="itemId" value=",${item.id},"/>
		                        		<c:if test="${lockPersons!=''&&(fn:contains(newLockPersons,itemId))}">
		                        			<tags:buttonOpteration 
			                            	opterNames="解锁" 
			                            	buttonTypes="a" 
			                            	opterTypes="href" 
			                            	opterParams="account-lock-info-unlock.do?id=${item.id}&partyEntityId=${partyEntityId}"/>
		                        		</c:if>
		                            	<tags:buttonOpteration 
		                            	opterNames="编辑|调岗|离职|重置密码|重置私钥" 
		                            	buttonTypes="a|a||a|a" 
		                            	opterTypes="href|href|click|click|click" 
		                            	opterParams="person-info-input.do?id=${item.id}&partyEntityId=${item.parentId}|person-info-position-change-i.do?id=${item.id}|quit('${item.id}','${item.parentId}')|fnReset('${item.id}')|resetKey('${item.id}')"/>
	                            	</c:if>
	                            </td>
                            <c:if test="${isOpenOtherName=='1'}">
                            	<td>${item.fullName}</td> <!-- 别名 -->
                            	<td>${item.realName}</td> <!-- 姓名 -->
                            </c:if>
                            <c:if test="${isOpenOtherName=='0'}">
	                           <td>${item.fullName}</td> <!-- 姓名 -->
                            </c:if>                            
	                            <td>${item.userName}</td> <!-- 用户名 （系统的登录名）-->
	                            <td>${item.companyName}</td> <!-- 公司 -->
	                            <td>${item.departmentName}</td> <!-- 部门 -->
	                            <td>${item.positionCode}</td> <!-- 岗位 -->
	                            <td>${item.positionName}</td> <!-- 职位 -->
	                            <td>
		                            <c:if test="${item.stopFlag=='active'}">
		                                 <span style="color: green;">启用</span>
		                            </c:if>
		                            <c:if test="${item.stopFlag=='disabled'}">
		                                 <span style="color: red;">禁用</span>
		                            </c:if>
		                        </td>
	                           	<td>${item.telephone}</td>
	                            <td>${item.email}</td><!-- 邮箱 -->
	                            <td>
	                            	<c:if test="${item.gender == '1'}">男</c:if> <c:if
	                                    test="${item.gender == '2'}">女</c:if>
	                            </td>
	                            <td>${item.address}</td><!--现住址-->
	                            <td>${item.remark}</td><!--备注 -->
	                         	<td>${item.addTime}</td><!--添加日期 -->
	                        </tr>
	                    </c:forEach>
	                    </tbody>
	                </table>
	                </c:if>
	            </div>
                <%--   重置密码弹窗--%>
                <div style="display:none" id="divChangePwd">
                    <table style="width:80%">
                        <tr>
                            <td style="width:20%"></td>
                            <td style="width:80%">
                                <div id="divMsg" style="margin:0 5px;color:red;"></div>
                            </td>
                        </tr>

                        <tr>
                            <td>&emsp;新密码：</td>
                            <td><input id="newPwd" onkeyup="checkPwd(this)" onpaste="return false" oncopy="return false"
                                       oncut="return false" type="password" style="margin:0 5px;" maxlength="20"/></td>
                        </tr>
                        <tr></tr>
                        <tr>
                            <td style="width: 50px ; height: 20px">确认密码：</td>
                            <td><input id="confirmPwd" type="password" onpaste="return false" oncopy="return false"
                                       oncut="return false" style="margin:0 5px;" maxlength="20"/></td>
                        </tr>
                        <tr>
                            <td class="tdl">安全性：</td>
                            <td class="tdr" style=" width:100px;height:40px;">
                                <table cellspacing="0" cellpadding="0" style="width:100%;" class="safetable">
                                    <tr class="safesure">
                                        <td style="border:1px solid #DEDEDE; height:18px; width: 50px" id="td1">
                                        </td>
                                        <td style="border:1px solid #DEDEDE; height:18px;  width: 50px" id="td2">
                                        </td>
                                        <td style="border:1px solid #DEDEDE; height:18px;  width: 50px" id="td3">
                                        </td>
                                        <td style="border:0px; height:18px; text-align:center;line-height:18px;"
                                            id="safelv"></td>

                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </div>
				<%-- 考勤编号弹框 --%>
				<div style="display:none" id="divParentAttendanceInfo">
					<div style="display:none" id="divAttendanceInfo">
						<!-- <table style="width:80%">
	                        <tr>
	                            <td style="width:20%"></td>
	                            <td style="width:80%">
	                                <div id="divMsg" style="margin:0 5px;color:red;"></div>
	                            </td>
	                        </tr>
	                        <tr>
	                        	<td style="text-align:right;">人员信息：</td>
	                        	<td style="height:40px;"><div id="divAttendance_personInfo"></div></td>
	                        </tr>
	                        <tr>
	                        	<td style="text-align:right;">考勤机编号：</td>
	                        	<td style="height:40px;"><input id="iptAttendanceNo" value="{iptAttendanceNo}" type="text" style="padding:0 5px;height:30px;line-height:25px;" maxlength="10" placeholder="请输入考勤机编号"/></td>
	                        </tr>
	                        <tr>
	                        	<td style="text-align:right;">人员编号：</td>
	                        	<td style="height:40px;"><input id="iptPersonNo" value="{iptPersonNo}" type="text" style="padding:0 5px;height:30px;line-height:25px;" maxlength="10" placeholder="请输入人员编号"/></td>
	                        </tr>
	                        <tr>
	                        	<td style="text-align:right;">备注：</td>
	                        	<td style="height:40px;"><textarea id="iptPersonNote" value="{iptPersonNote}" style="width:90%;padding:0 5px;height:30px;line-height:25px;" maxlength="100"  placeholder="输入备注"></textarea></td>
	                        </tr>
	                    </table> -->
					</div>
				</div>
				
            </div>
        </form>

        <div>
            <div class="m-page-info pull-left">共100条记录 显示1到10条记录</div>

            <div class="btn-group m-pagination pull-right">
                <button class="btn btn-default">&lt;</button>
                <button class="btn btn-default">1</button>
                <button class="btn btn-default">&gt;</button>
            </div>

            <div class="clearfix"></div>
        </div>

        <div class="m-spacer"></div>

    </section>
    <!-- end of main -->
    <script type="text/javascript">
    	var fnSetAttendance=function(userid,company,depart,name){
    		$.get(config.getAttendanceUrl, {id: userid}, function (attendance) {
                 if (attendance == undefined || attendance == null || attendance == "") {
                     bootbox.alert("获取数据失败！");
                     return false;
                 }
                 if (attendance.code == 200) {
                	 
                	 var person=company+'-'+depart+'-'+name;
             		 $('#divAttendance_personInfo').html(person);
             		
             		 
             		var html ="";
             		
             		html+='<table style="width:80%">';
             		html+='<tr>'
             		html+='<td style="width:20%"></td>'
             		html+='<td style="width:80%">'
             		html+='<div id="divMsg" style="margin:0 5px;color:red;"></div>'
             		html+='</td>'
             		html+='</tr>'
             		html+='<tr>'
             		html+='<td style="text-align:right;">人员信息：</td>'
             		html+='<td style="height:40px;"><div id="divAttendance_personInfo">'+person+'</div></td>'
             		html+='</tr>'
             		html+='<tr>'
             		html+='<td style="text-align:right;">考勤机编号：</td>'
             		html+='<td style="height:40px;"><input id="iptAttendanceNo" value="'+(attendance.data.mach_no==null?"":attendance.data.mach_no)+'" type="text" style="padding:0 5px;height:30px;line-height:25px;" maxlength="10" placeholder="请输入考勤机编号"/></td>'
             		html+='</tr>'
             		html+='<tr>'
             		html+='<td style="text-align:right;">人员编号：</td>'
             		html+='<td style="height:40px;"><input id="iptPersonNo" value="'+(attendance.data.user_no==null?"":attendance.data.user_no)+'" type="text" style="padding:0 5px;height:30px;line-height:25px;" maxlength="10" placeholder="请输入人员编号"/></td>'
             		html+='</tr>'
             		html+='<tr>'
             		html+='<td style="text-align:right;">备注：</td>'
             		html+='<td style="height:40px;"><textarea id="iptPersonNote" value="'+(attendance.data.remark==null?"":attendance.data.remark)+'" style="width:90%;padding:0 5px;height:30px;line-height:25px;" maxlength="100"  placeholder="输入备注"></textarea></td>'
             		html+='</tr>'
             		html+='</table>'
             		var dialogInput = bootbox.dialog({
                         title: "设置人员考勤编号",
                         message: html,
                         buttons: {
                             noclose: {
                                 label: '提交',
                                 className: 'btn-primary',
                                 callback: function () {
                                 	var attendanceNo=$("#iptAttendanceNo").val();
                                     if (attendanceNo.length==0) {
                                         bootbox.alert("请输入考勤机编号！");
                                         return false;
                                     }
                                     
                                     var personNo=$("#iptPersonNo").val();
                                     if (personNo.length==0) {
                                         bootbox.alert("请输入人员编号！");
                                         return false;
                                     };
                                     var loading = bootbox.dialog({
                                         message: '<p>提交中...</p>',
                                         closeButton: false
                                     });

                                     var note =$("#iptPersonNote").val();
                                     $.post(config.setAttendanceUrl, {id: userid,attendanceNo:attendanceNo,personNo:personNo, note: note}, function (data) {
                                         loading.modal('hide')
                                         if (data == undefined || data == null || data == "") {
                                             bootbox.alert("操作失败！");
                                             return false;
                                         }
                                         if (data.code == 200) {
                                        	 dialogInput.modal('hide')
                                             bootbox.alert({
                                                 message: data.message, callback: function () {
                                                     document.getElementById('btn_Search').click();
                                                 }
                                             });
                                         }
                                         else
                                             bootbox.alert(data.message);

                                         return data.code == 200;
                                     })
                                     return false;
                                 }
                             },
                             cancel: {
                                 label: '取消',
                                 className: 'btn-danger'
                             }
                         },
                         callback: function (result) {
                             alert(result);
                             return;
                         },
                         show: true
                     });
                 }
                 else
                     bootbox.alert(data.message);
             });
    	}
    </script>
</div>

</body>

</html>

