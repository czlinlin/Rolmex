<%@page contentType="text/html;charset=UTF-8"%>
<%@include file="/taglibs.jsp"%>
<%
	pageContext.setAttribute("currentHeader", "person");
%>
<%
	pageContext.setAttribute("currentMenu", "person");
%>
<%
	pageContext.setAttribute("currentMenuName", "人力资源");
%>
<!doctype html>
<html>

<head>
<%@include file="/common/meta.jsp"%>
<title><spring:message code="user.user.input.title" text="麦联" /></title>
<%@include file="/common/s3.jsp"%>

<link type="text/css" rel="stylesheet"
	href="${cdnPrefix}/orgpicker/orgpicker.css">
<script type="text/javascript" src="${cdnPrefix}/orgpicker/orgpicker.js"></script>

<link type="text/css" rel="stylesheet" href="${cdnPrefix}/userpicker3-v2/userpicker.css">
<script type="text/javascript" src="${cdnPrefix}/userpicker3-v2/userpickercustomaudit.js"></script>
<script type="text/javascript" src="${cdnPrefix}/operation/TaskOperation.js"></script>
<script type="text/javascript" src="${cdnPrefix}/userpicker3-v2/postForPersonInFo.js"></script>
<script type="text/javascript" src="${cdnPrefix}/bootbox/bootbox.min1.js"></script>
<script type="text/javascript">
        $(function () {
        	var isvalidate="${isValidate}";
        	if(isvalidate=="0"){
        		$("label font").remove();
        		$(".required").removeClass("mobileNumber");
        		$(".required").removeClass("telormobile");
        		$(".required").removeClass("email");
        		$(".required").removeClass("fax");
        		$(".required").removeClass("idCardNo");
        		$(".required").removeClass("required");
        	}
        	
            //审批人
            createUserPicker({
                modalId: 'leaderPicker',//这个 其实是弹出的窗口的div id
                targetId: 'leaderDiv', //这个是点击哪个 会触发弹出窗口
                inputStoreIds: {iptid: "leaderId", iptname: "leaderName"},//存储已选择的ID和name的input的id
                auditId: 'ulapprover',//显示审批步骤
                showExpression: true,
                multiple: true,
                searchUrl: '${tenantPrefix}/rs/user/searchVNoMe',
                treeUrl: '${tenantPrefix}/party/asyncTree.do?partyStructTypeId=1&notViewPost=true&notAuth=false',
                childUrl: '${tenantPrefix}/rs/party/searchUserNoMe'
            }); 
        	//岗位
            createUserPickerForPersonInfo({
                modalId: 'userPickerForPersonInfo',
                targetId: 'PersonInfoDiv', //这个是点击哪个 会触发弹出窗口
                showExpression: true,
                multiple: false,
                searchUrl: '${tenantPrefix}/rs/user/search',
                // treeNoPostUrl: '${tenantPrefix}/rs/party/treeNoPost?partyStructTypeId=1',
                treeNoPostUrl: '${tenantPrefix}/party/asyncTree.do?partyStructTypeId=1&notViewPost=true&notAuth=false',
                childPostUrl: '${tenantPrefix}/rs/party/searchPost'
            });

            createOrgPicker({
                modalId: 'orgPicker',
                showExpression: true,
                chkStyle: 'radio',
                searchUrl: '${tenantPrefix}/rs/user/search',
                treeUrl: '${tenantPrefix}/rs/party/treeNoPostCompanyChecked?partyStructTypeId=1',
                childUrl: '${tenantPrefix}/rs/party/searchUser'
            });

            $("#userBaseForm").validate({
                submitHandler: function (form) {
                    bootbox.animate(false);
                     form.submit();
                },
                errorClass: 'validate-error',
                rules: {
                	
                    /* username: {
                        remote: {
                            url: 'account-info-checkUsername.do',
                            data: {
                                <c:if test="${model != null}">
                                id: function () {
                                    return $('#userBase_id').val();
                                }
                                </c:if>
                            }
                        }
                    }, */
               },
                messages: {
                    /* username: {
                        remote: "<spring:message code='user.user.input.duplicate' text='存在重复账号'/>"
                    },
                    employeeNo: {
                        remote: "<spring:message code='user.user.input.duplicate' text='存在重复员工编号'/>"
                    } */
               
                }
            });
            
            var sectionJson = [{"begin": "#pickerStartTime", "end": "#pickerEndTime"}];
            fnTaskSectionPickerTime(sectionJson)
        })
        
        
        
         $(function(){
			  $("#quitFlagId :radio").click(function(){
			   	var leaveDate = $(this).val();
			   	if(leaveDate == '1'){
			   		$("#leaveDateId").html('<label class="control-label col-md-1" for="orgInputUser_priority">'
							+'<span style="color: red">*</span>工资结算日期</label>'
							+'<div class="col-sm-5" style="padding-left: 30px;">'
							+'<input autocomplete="off" placeholder="请选择时间" type="text" id="leaveDate" name="leaveDate" onclick="WdatePicker({dateFmt:&quot;yyyy-MM-dd&quot;})" class="Wdate" style="width:200px;background-color:#eee;padding-left:10px;" value="<fmt:formatDate value="${model.leaveDate }" pattern="yyyy-MM-dd" /> "/>'	
							+'</div>');
			   		$("#quitTimeId").html('<label class="control-label col-md-1" for="orgInputUser_priority">'
							+'<span style="color: red">*</span>禁止登录系统日期</label>'
							+'<div class="col-sm-5" style="padding-left: 55px;">'
							+'<input autocomplete="off" placeholder="请选择时间" type="text" id="quitTime" name="quitTime" onclick="WdatePicker({dateFmt:&quot;yyyy-MM-dd&quot;})" class="Wdate" style="width:200px;background-color:#eee;padding-left:10px;" value="<fmt:formatDate value="${model.quitTime }" pattern="yyyy-MM-dd" /> "/>'	
							+'</div>');
			   	}else{
			   		$("#leaveDateId").html("");
			   		$("#quitTimeId").html("");
			   		
			   	}
			  });
		 });
        
        
        
    </script>
    
     <script>
        //入职时间  合同到期时间
        var fnTaskSectionPickerTime = function (eleTimes) {
            $(eleTimes).each(function (i, ele) {
                $(ele.begin + " span").remove();
                $(ele.end + " span").remove();

               /*  $(ele.begin + " input").css("width", "835px");
                $(ele.end + " input").css("width", "835px"); */

                $(ele.begin + " input").addClass("Wdate");
                $(ele.end + " input").addClass("Wdate");

                var begin = $(ele.begin + " input").attr("id");
                var end = $(ele.end + " input").attr("id");

                if (begin == undefined)
                    $(ele.begin + " input").attr("id", "ipt" + ele.begin.replace("#", ""));
                if (end == undefined)
                    $(ele.end + " input").attr("id", "ipt" + ele.end.replace("#", ""))

                begin = $(ele.begin + " input").attr("id");
                end = $(ele.end + " input").attr("id");


                $(ele.begin + " input").attr("onclick", "WdatePicker({maxDate:'#F{$dp.$D(\\'" + end + "\\',{H:-1})||\\'2020-10-01\\'}',dateFmt:'yyyy-MM-dd'})");
                $(ele.end + " input").attr("onclick", "WdatePicker({minDate:'#F{$dp.$D(\\'" + begin + "\\',{H:+1})}',maxDate:'2020-10-01',dateFmt:'yyyy-MM-dd'})");
                //开始时间
                $(ele.begin + " .glyphicon-calendar").click(function () {
                    $(ele.begin + " input").click();
                })
                $(ele.end + " .glyphicon-calendar").click(function () {
                    $(ele.end + " input").click();
                })

            })
        }
        
        var fnGetWorkNumber=function(){
        	var partyEntityId=$("#orgPartyEntityId").val();
            	$.ajax({
                    url: "${tenantPrefix}/rs/party/getWorkNumber",
                    type: "GET",
                    data: {partyEntityId:partyEntityId},
                    timeout: 10000,
                    success: function (data) {
                    	
                    	$("#employeeNo_depart").val(data.workNumberPrefix);
                    },
                    error: function (XMLHttpRequest, textStatus, errorThrown) {
                        //alert("[" + XMLHttpRequest.status + "]error，请求失败");
                        alert("获取工号前缀失败，请手动改变工号前缀")
                    },
                    complete: function (xh, status) {
                        if (status == "timeout"){
                        	alert("获取工号前缀请求超时，请手动改变工号前缀");
                        	return false;
                        }  
                    }
                });	
        }
    </script> 
<style type="text/css">
label {
	white-space: nowrap
}
</style>
</head>

<body>

<div class="row-fluid">

		<%-- <c:if test="${not empty flashMessages}">
			<div id="m-success-message" style="display: none;">
				<ul>
					<c:forEach items="${flashMessages}" var="item">
						<c:if test="${item != ''}">
							<li>${item}</li>
						</c:if>
					</c:forEach>
				</ul>
			</div>
		</c:if> --%>
		<!-- start of main -->
		<section id="m-main" class="col-md-12" style="padding-top: 3px;">

			<!-- <ul class="breadcrumb">
				<li><a href="person-list-info.do">花名册 / 职员录入</a></li>
				<li class="active"></li>
			</ul> -->

	<div class="panel panel-default">
		<%-- <div class="panel-heading">
			<i class="glyphicon glyphicon-list"></i>
				<c:if test="${model.id == null}">
                   		 录入职员
                </c:if>
				
				<c:if test="${model.id != null}">
                    	编辑职员
                </c:if>

		</div> --%>

			<div class="panel-body">

					<form id="userBaseForm" method="post" enctype="multipart/form-data"
						action="person-info-update-ForModifySave.do?typeID=${typeID}&applyCode=${applyCode} "
						class="form-horizontal">

						<input id="userBase_id" type="hidden" name="id"
							value="${model.id}">
					
						<div class="form-group">
								<label class="control-label col-md-1">上级机构</label>
								<div class="col-sm-5">
									<div class="input-group orgPicker">
									
									<c:if test="${typeID == 'personUpdate'}">
										<input id="orgPartyEntityId" type="hidden" name="partyEntityId"
											value="${model.departmentCode}"> <input type="text"
											class="form-control required" id="departmentName"
											name="departmentName" placeholder=""
											value="${model.departmentName}" minlength="2" maxlength="50"
											>
									</c:if>
									
									
									<c:if test="${typeID == 'personadd'}">
										<input id="orgPartyEntityId" type="hidden" name="partyEntityId"
											value="${model.departmentCode}"> <input type="text"
											class="form-control required" id="departmentName"
											name="departmentName" placeholder=""
											value="${model.departmentName}" minlength="2" maxlength="50"
											readonly="readonly">
									</c:if>
									<div class="input-group-addon">
											<i class="glyphicon glyphicon-user"></i>
										</div>
									</div>
								</div>
								<input id="org_level" type="hidden" name="partyLevel"
									value="${partyEntity.level}">
							</div>

						
						<div class="form-group">
							<label class="control-label col-md-1" for="userBase_username"><font
								color="red">*</font>
							<spring:message code="user.user.input.username" text="用户名" /></label>
							<div class="col-sm-5">
								<input id="userBase_username" type="text" name="username"
									value="${model.username}" size="40"  placeholder="请输入用户名(必填)"
									class="form-control required" minlength="2" maxlength="50">
							</div>

							<label class="control-label col-md-1" for="org_orgname"><font
								color="red">*</font>工号</label>
							<div class="col-sm-5">
							
<!-- 								老用户修改  取原有的工号 -->
								
									<input type="text" id="employeeNo_depart"  
										name="employeeNoDepart" value="${model.employeeNoDepart}"  class="form-control required" style="width:200px;height: 35px;display:inline;">
									<input type="text"  id="employeeNo_num"
										name="employeeNoNum"  value="${model.employeeNoNum}" style="width:100px;height: 35px;display:inline;">
									<input type="hidden"  id="employeeNo"
										name=employeeNo  value="${model.employeeNo}">	
					
							</div>
						</div>

						<div class="form-group">
							<label class="control-label col-md-1" for="org_orgname"><font
								color="red">*</font>
							<spring:message code="org.org.input.orgname" text="姓名" /></label>
							<div class="col-sm-5">
								<input type="text" class="form-control required" minlength="2"   placeholder="请输入姓名(必填)" 
									maxlength="50" id="userBase_fullName" name="fullName"
									placeholder="" value="${model.fullName}">
							</div>

							<label class="control-label col-md-1" for="orgInputUser_status">性别</label>
							<div class="col-sm-5">
								<label for="gender1" class="radio inline col-md-3"> <input
									id="gender1" type="radio" name="gender" value="1"
									class="required"
									${(model.gender == '1' || model.id == null) ? 'checked' : ''}>
									男
								</label> <label for="gender2" class="radio inline col-md-3"> <input
									id="gender2" type="radio" name="gender" value="2"
									class="required" ${model.gender == '2' ? 'checked' : ''}>
									女
								</label> <label for="gender2" class="validate-error" generated="true"
									style="display: none;"></label>
							</div>
						</div>

						
						<div class="form-group">
						
						<c:if test="${typeID == 'personadd'}">
						
							 <label class="control-label col-md-1" for="userPickerForPersonInfo">
								<spring:message code="org.org.input.orgname" text="岗位" /> </label>
							
		                        <div class="col-sm-5 userPickerForPersonInfo">
		                            <div class="input-group ">
		                                <input id="_task_name_key" type="hidden" name="postId"
		                                       value="${model.postId}">
		                                <input type="text" name="postName" id="postName"
		                                       value="${model.postName}" class="form-control" readonly placeholder="点击右侧图标选择岗位" >
		                                <div id='PersonInfoDiv' class="input-group-addon"><i class="glyphicon glyphicon-user"></i></div>
		                            </div>
		                        </div>
	                     </c:if>
	                        
	                        <label class="control-label col-md-1" for="org_orgname"><font
								color="red">*</font>
							<spring:message code="org.org.input.orgname" text="级别" /> </label>
							<div class="col-sm-5" class="form-control required">
								<select id="level_1" name="level_1" class="form-control required"  onchange="getLevelSub()" style="width:200px;display:inline;">
									<option value="">请选择</option>
								
								</select>
								
								<select id="level_2" name="level_2" class="form-control required" onchange="saveLevel()" style="width:200px;display:inline;">
									<option value="">请选择</option>
								</select>
									 <input id="level" name="level" type="hidden"  value="${model.level}">
							</div>
	                        
	                        
	                    </div>
							
						<div class="form-group">
							<label class="control-label col-md-1" for="org_orgname"><font
								color="red">*</font>
							<spring:message code="org.org.input.orgname" text="职位" /> </label>
							<div class="col-sm-5">
								<select id="positionCode"  class="form-control required" 
									name="positionCode">
									<option value="">请选择</option>
									<c:forEach items='${dictInfos}' var="dictInfo">
										<option value="${dictInfo.value}"
											<c:if test='${model.positionCode == dictInfo.value}'>selected</c:if>>${dictInfo.name}</option>
									</c:forEach>
								</select>
							</div>
							
							<label class="control-label col-md-1" for="userBase_title">职称</label>
							<div class="col-sm-5">
								<input id="userBase_title" type="text" name="title" placeholder="请输入职称"
									value="${model.title}" size="40" class="form-control" 
									 minlength="2"
									maxlength="50">
							</div>
								
							<div style="display: none">
								<label class="control-label col-md-1" for="orgInputUser_status">是否兼职</label>
								<div class="col-sm-5">
									<label for="orgInputUser_status1" class="radio inline col-md-4">
										<input id="orgInputUser_status1" type="radio" name="jobStatus"
										value="1" class="required"
										${(model.jobStatus == '1' || model.id == null) ? 'checked' : ''}>
										主职
									</label> <label for="orgInputUser_status2"
										class="radio inline col-md-4"> <input
										id="orgInputUser_status2" type="radio" name="jobStatus"
										value="2" class="required"
										${model.jobStatus == '2' ? 'checked' : ''}> 兼职
									</label> <label for="orgInputUser_status2" class="validate-error"
										generated="true" style="display: none;"></label>
								</div>
							</div>
						</div>

					<div class="form-group">
							<label class="control-label col-md-1" for="userBase_telephone"><font color='red'>*</font>联系电话</label>
							<div class="col-sm-5">
								<input id="userBase_telephone" type="text" name="telephone"  placeholder="请输入联系电话(必填)"
									value="${model.telephone}" size="11" class="form-control required mobileNumber"
									maxlength="11">
							</div>

							<label class="control-label col-md-1" for="userBase_cellphone">备用联系电话</label>
							<div class="col-sm-5">
								<input id="userBase_cellphone" type="text" name="cellphone"  placeholder="请输入备用电话""
									value="${model.cellphone}" size="40" class="form-control telormobile"
									minlength="2" maxlength="20">
							</div>
					</div>
				
					<div class="form-group">
							<label class="control-label col-md-1" for="userBase_email"><font
								color="red">*</font>邮箱</label>
							<div class="col-sm-5">
								<input id="userBase_email" type="text" name="email"
									value="${model.email}" size="40"  placeholder="请输入邮箱(必填)"
									class="form-control required email" minlength="2"
									maxlength="50">
							</div>
						
							<label class="control-label col-md-2" for="userBase_fax">紧急联系人及电话</label>
							<div class="col-sm-4">
								<input id="userBase_fax" type="text" name="fax"  placeholder="请输入紧急联系人及电话"
									value="${model.fax}" size="40" class="form-control"
									 maxlength="32">
							</div>
						
					</div>

					<div class="form-group">
							<label class="control-label col-md-1" for="userBase_wxNo">微信</label>
							<div class="col-sm-5">
								<input id="userBase_wxNo" type="text" name="wxNo"
									value="${model.wxNo}" size="40" class="form-control"  placeholder="请输入微信"
									minlength="2" maxlength="50">
							</div>
						
							<label class="control-label col-md-1" for="userBase_qq">QQ</label>
							<div class="col-sm-5">
								<input id="userBase_qq" type="text" name="qq"  placeholder="请输入QQ"
									value="${model.qq}" size="40" class="form-control"
									minlength="2" maxlength="16">
							</div>
					</div>

					<div class="form-group">
					
						<label class="control-label col-md-1" for="userBase_nameBefore">曾用名</label>
							<div class="col-sm-5">
								<input id="name_used_before" type="text" name="nameBefore"  placeholder="请输入曾用名"
									value="${model.nameBefore}" size="40" class="form-control"
									 minlength="2" maxlength="50">
							</div>
					
<!-- 					class="form-control required email" -->
						<label class="control-label col-md-1" for="userBase_identityID"><font
								color="red">*</font>身份证号</label>
							<div class="col-sm-5">
								<input id="identity_Card" type="text" name="identityID" placeholder="请输入身份证号"
									value="${model.identityID}" size="40" class="form-control required idCardNo"
									maxlength="18">
							</div>
					</div>
					
					<div class="form-group">
							<label class="control-label col-md-1" for="native_place"><font
								color="red">*</font>籍贯</label>
							<div class="col-sm-5">
								<input id="native_place" type="text" name="nativePlace" placeholder="请输入籍贯(必填)"
									value="${model.nativePlace}" size="40" class="form-control required"
									 minlength="2"
									maxlength="200">
							</div>
						
							<label class="control-label col-md-1" for="registered_residence"><font
								color="red">*</font>户口所在地</label>
							<div class="col-sm-5">
								<input id="registered_residence" type="text" name="registeredResidence" placeholder="请输入户口所在地(必填)"
									value="${model.registeredResidence}" size="40" class="form-control required"
									 minlength="2"
									maxlength="200">
							</div>
					</div>
						
					<div class="form-group">
							<label class="control-label col-md-1" for="householdRegisterType"><font
								color="red">*</font>
							<spring:message code="org.org.input.orgname" text="户籍类型" /> </label>
							<div class="col-sm-5">
								<select id="householdRegisterType" class="form-control required"
									name="householdRegisterType">
									<option value="">请选择</option>
									<c:forEach items='${dictInfo_RegisterType}' var="dictInfo_RegisterType">
										<option value="${dictInfo_RegisterType.value}"
											<c:if test='${model.householdRegisterType == dictInfo_RegisterType.value}'>selected</c:if>>${dictInfo_RegisterType.name}</option>
									</c:forEach>
								</select>
							</div>
						
	 						<label class="control-label col-md-1" for="userBase_politicalOutlook"><font
								color="red">*</font>政治面貌</label>
							<div class="col-sm-5">
								<select id="political_outlook" class="form-control required"
									name="politicalOutlook">
									<option value="">请选择</option>
									<c:forEach items='${dictInfo_politicalOutlook}' var="dictInfo_politicalOutlook">
										<option value="${dictInfo_politicalOutlook.value}"
											<c:if test='${model.politicalOutlook == dictInfo_politicalOutlook.value}'>selected</c:if>>${dictInfo_politicalOutlook.name}</option>
									</c:forEach>
								</select>
							</div>
					</div>
						
					<div class="form-group">
							<label class="control-label col-md-1" for="nation"><font
								color="red">*</font>民族</label>
							<div class="col-sm-5">
								<select id="nation" name="nation" class="form-control required">
								<option value="">请选择</option>
									<c:forEach items='${dictInfo_nation}' var="dictInfo_nation">
										<option value="${dictInfo_nation.value}"
											<c:if test='${model.nation == dictInfo_nation.value}'>selected</c:if>>${dictInfo_nation.name}</option>
									</c:forEach>
								</select>
							</div>
						
							<label class="control-label col-md-1" for="education"><font
								color="red">*</font>学历</label>
							<div class="col-sm-5">
								<select id="education" name="education" class="form-control required">	
									<option value="">请选择</option>
									<c:forEach items='${dictInfo_education}' var="dictInfo_education">
										<option value="${dictInfo_education.value}"
											<c:if test='${model.education == dictInfo_education.value}'>selected</c:if>>${dictInfo_education.name}</option>
									</c:forEach>
								</select>
							</div>
					</div>
						
				<div class="form-group">
							<label class="control-label col-md-1" for="major"><font
								color="red">*</font>专业</label>
							<div class="col-sm-5">
								<input id="major" type="text" name="major" placeholder="请输入专业"
									value="${model.major}" size="40" class="form-control required"
									 minlength="2"
									maxlength="100">
							</div>
						
							<label class="control-label col-md-1" for="academic_degree"><font
									color="red">*</font>学位</label>
							<div class="col-sm-5">
								<select id="academicDegree" name="academicDegree" class="form-control required">	
									<option value="">请选择</option>
									<c:forEach items='${dictInfo_academicDegree}' var="dictInfo_academicDegree">
										<option value="${dictInfo_academicDegree.value}"
											<c:if test='${model.academicDegree == dictInfo_academicDegree.value}'>selected</c:if>>${dictInfo_academicDegree.name}</option>
									</c:forEach>
								</select>
							</div>
					</div>
						
					<div class="form-group">
							<label class="control-label col-md-1" for="skill_specialty">技能特长</label>
							<div class="col-sm-5">
								<input id="skill_specialty" type="text" name="skillSpecialty" placeholder="请输入技能特长"
									value="${model.skillSpecialty}" size="40" class="form-control"
									 minlength="2"
									maxlength="50">
							</div>
							
							<label class="control-label col-md-1" for="labor_type"><font
								color="red">*</font>用工类型</label>
							<div class="col-sm-5">
								<select id="laborType" name="laborType" class="form-control required">	
									<option value="">请选择</option>
									<c:forEach items='${dictInfo_laborType}' var="dictInfo_laborType">
										<option value="${dictInfo_laborType.value}"
											<c:if test='${model.laborType == dictInfo_laborType.value}'>selected</c:if>>${dictInfo_laborType.name}</option>
									</c:forEach>
								</select>
							</div>
							
					</div>
						
					<div class="form-group">
							<label class="control-label col-md-1" for="entry_mode"><font
								color="red">*</font>进入方式</label>
							<div class="col-sm-5">
								<select id="entryMode" name="entryMode" class="form-control required">
									<option value="">请选择</option>
									<c:forEach items='${dictInfo_entryMode}' var="dictInfo_entryMode">
										<option value="${dictInfo_entryMode.value}"
											<c:if test='${model.entryMode == dictInfo_entryMode.value}'>selected</c:if>>${dictInfo_entryMode.name}</option>
									</c:forEach>
								</select>
							</div>
							
							<label class="control-label col-md-1" for="contract_company"><font
								color="red">*</font>合同单位</label>
<!-- 							<div class="col-sm-5"> -->
<!-- 								<input id="contract_company" type="text" name="contractCompany"  placeholder="请输入合同单位" -->
<%-- 									value="${model.contractCompany}" size="40" class="form-control required" --%>
<!-- 									 minlength="2"  -->
<!-- 									maxlength="50"> -->
<!-- 							</div> -->
							
								<div class="col-sm-5">
									
									<select id="contractCompany" name="contractCompany" class="form-control required">
										<option value="">请选择</option>
										<c:forEach items='${personContractCompanyManage}' var="item">
											<option value="${item.id}"
												<c:if test='${model.contractCompanyID == item.id}'>selected</c:if>>${item.contractCompanyName}</option>
										</c:forEach>
									</select>	
										
								</div>
					</div>
						
		
					<div class="form-group">
							<label class="control-label col-md-1" for="entry_time">
							   <span style="color:red;"> * </span> 入职时间</label> 
							
							<div class="col-sm-5">
	                            <div id="pickerStartTime" class="input-group date">
	                                <input id="entry_time" type="text" name="entryTime"
	                                       value="<fmt:formatDate value='${model.entryTime}' type="both" pattern='yyyy-MM-dd'/>"
	                                       readonly style="background-color:white;cursor:default;" placeholder="请选择入职时间(必选)"
	                                       class="form-control required">
	                                <span class="input-group-addon"><i class="glyphicon glyphicon-calendar"></i></span>
	                            </div>
                        	</div>
                        	<label class="control-label col-md-1" for="contract_expiration_time" ><font
									color="red">*</font>合同到期时间</label>
							<div class="col-sm-5">
									  <div id="pickerEndTime" class="input-group date">
			                                <input id="contract_expiration_time" type="text" name="contractExpirationTime"
			                                       value="<fmt:formatDate value='${model.contractExpirationTime}' type="both" pattern='yyyy-MM-dd'/>"
			                                       readonly placeholder="请选择合同到期时间(必选)"
			                                       style="background-color:white;cursor:default;" class="form-control required">
			                                <span class="input-group-addon"><i class="glyphicon glyphicon-calendar"></i></span>
	                            	</div>
							</div>
                   </div>						
						
					<div class="form-group">
							
							<label class="control-label col-md-1" for="contract_deadline"><font
								color="red">*</font>合同有效期</label>
							<div class="col-sm-5">
								<input id="contract_deadline" type="text" name="contractDeadline"
									value="${model.contractDeadline}" size="40" class="form-control required"
									 minlength="2" placeholder="请输入合同有效期"
									maxlength="50">
							</div>
							
							<label class="control-label col-md-1" for="document">资料情况</label>
							<div class="col-sm-5">
								<input id="document" type="text" name="document" placeholder="请输入资料情况"
									value="${model.document}" size="40" class="form-control"
									 minlength="2"
									maxlength="200">
							</div>
							
					</div>

					<div class="form-group">
							<label class="control-label col-md-1" for="insurance"><font
								color="red">*</font>保险情况</label>
							<div class="col-sm-5">
								<input id="insurance" type="text" name="insurance" placeholder="请输入保险情况"
									value="${model.insurance}" size="40" class="form-control required" 
									 minlength="2"
									maxlength="50">
							</div>
				
							<label class="control-label col-md-1" for="userBase_cellphone"><font
								color="red">*</font>现住址</label>
								<div class="col-sm-5">
									<input id="userBase_address" type="text" name="address"
										value="${model.address}" size="40" class="form-control required" placeholder="请输入现住址"
										minlength="2" maxlength="50"> 
								</div>
				</div>
				
				
				<div class="form-group">
							<label class="control-label col-md-1" for="family_1">家庭成员1</label>
							<div class="col-sm-5">
								<input id="family_1" type="text" name="family_1"
									value="${model.family_1}" size="40" placeholder="请输入家庭成员"
									 minlength="2" class="form-control"
									maxlength="50">
							</div>
					
							<label class="control-label col-md-1" for="family_2">家庭成员2</label>
							<div class="col-sm-5">
								<input id="family_2" type="text" name="family_2"  placeholder="请输入家庭成员"
									value="${model.family_2}" size="40"
									 minlength="2" class="form-control"
									maxlength="50">
							</div>
						
					</div>
					
					<div class="form-group">
							<label class="control-label col-md-1" for="orgInputUser_status">婚否</label>
							<div class="col-sm-5">
								
								 <label for="marriage2" class="radio inline col-md-3"> <input
									id="marriage2" type="radio" name="marriage" value="2"
									class="required" ${(model.marriage == '2'|| model.id == null) ? 'checked' : ''}>
									未婚
								</label> 
								<label for="marriage1" class="radio inline col-md-3"> <input
									id="marriage1" type="radio" name="marriage" value="1"
									class="required"
									${model.marriage == '1'  ? 'checked' : ''}>
									已婚
								</label>
								 <label for="marriage3" class="radio inline col-md-3"> <input
									id="marriage3" type="radio" name="marriage" value="3"
									class="required" ${model.marriage == '3' ? 'checked' : ''}>
									离异
								</label> 
								<label for="marriage2" class="validate-error" generated="true"
									style="display: none;"></label>
							</div>


							<label class="control-label col-md-1" for="fertilityCondition">生育情况</label>
							<div class="col-sm-5">
								<label for="fertility_condition2" class="radio inline col-md-3"> <input
									id="fertility_condition2" type="radio" name="fertilityCondition" value="2"
									class="required" ${(model.fertilityCondition == '2'  || model.id == null)? 'checked' : ''}>
									未育
								</label>
								<label for="fertility_condition1" class="radio inline col-md-3"> <input 
									id="fertility_condition1" type="radio" name="fertilityCondition" value="1"
									class="required"
									${(model.fertilityCondition == '1') ? 'checked' : ''}>
									已育 
								</label>  <label for="fertility_condition2" class="validate-error" generated="true"
									style="display: none;"></label>
							</div>
					
					</div>
					
					
					
					<div class="form-group">
							<label class="control-label col-md-1" for="educational_experience_1">教育经历1</label>
							<textarea name="educational_experience_1" rows="2" cols="20" id="educational_experience_1"
                                          class="text0"   placeholder="请输入教育经历"
                                          style="height: 99px; width: 835px; padding-left: 10px; padding-top: 10px">${model.educational_experience_1}</textarea>
                        	
					</div>	
					
					<div class="form-group">
							<label class="control-label col-md-1" for="educational_experience_2">教育经历2</label>
							<textarea name="educational_experience_2" rows="2" cols="20" id="educational_experience_2"
                                          class="text0" placeholder="请输入教育经历"
                                          style="height: 99px; width: 835px; padding-left: 10px; padding-top: 10px">${model.educational_experience_2}</textarea>
                        	
					</div>
		
					<div class="form-group">
							<label class="control-label col-md-1" for="educational_experience_3">教育经历3</label>
					
							<textarea name="educational_experience_3" rows="2" cols="20" id="educational_experience_3"
                                          class="text0" placeholder="请输入教育经历"
                                          style="height: 99px; width: 835px; padding-left: 10px; padding-top: 10px">${model.educational_experience_3}</textarea>
                        	
					</div>

					<div class="form-group">
							<label class="control-label col-md-1" for="work_experience_1">工作经历1</label>
							
							<textarea name="work_experience_1" rows="2" cols="20" id="work_experience_1"
                                      class="text0" placeholder="请输入工作经历"
                                      style="height: 99px; width: 835px; padding-left: 10px; padding-top: 10px">${model.work_experience_1}</textarea>
                    </div>	
						
					<div class="form-group">	
							<label class="control-label col-md-1" for="work_experience_2">工作经历2</label>
							
								<textarea name="work_experience_2" rows="2" cols="20" id="work_experience_2"
                                          class="text0" placeholder="请输入工作经历"
                                          style="height: 99px; width: 835px; padding-left: 10px; padding-top: 10px">${model.work_experience_2}</textarea>
                        
					</div>
			
					<div class="form-group">
							<label class="control-label col-md-1" for="userBase_remark">备注</label>
							
								<textarea name="remark" rows="2" cols="20" id="remark"
                                          class="text0"  placeholder="请输入备注"
                                          style="height: 99px; width: 835px; padding-left: 10px; padding-top: 10px">${model.remark}</textarea>
                
                	</div>
		


						<div class="form-group">
							<label class="control-label col-md-1" for="userBase_status">启用</label>
							<div class="col-sm-1">
								<input id="userBase_stopFlag" type="checkbox" name="stopFlag"
									value="active"
									${model.stopFlag == 'active' || model.stopFlag == null ? 'checked' : ''}>
							</div>
						</div>
						<div class="form-group">
							<%--  <c:if test="${gestureSwitch != null}">
                             <label class="control-label col-md-1" for="userBase_gestureSwitch">启用手势</label>
                             <div class="col-sm-1">
                                 <input id="userBase_gestureSwitch" type="checkbox" name="gestureSwitch"
                                        value="open" ${gestureSwitch == 'open' || gestureSwitch == null ? 'checked' : ''}>
                             </div>
                         </c:if> --%>

							<label class="control-label col-md-1">通讯录权限</label>
							<div class="col-sm-3">
								<label class="radio inline col-md-4"> <input
									id="secret0" type="radio" name="secret" value="0" class=""
									${model.secret == '0' ? 'checked' : ''}> 公开
								</label> <label for="secret1" class="radio inline col-md-4"> <input
									id="secret1" type="radio" name="secret" value="1" class=""
									${(model.secret == '1' || model.id == null) ? 'checked' : ''}>
									内部
								</label> <label for="secret2" class="radio inline col-md-4"> <input
									id="secret2" type="radio" name="secret" value="2" class=""
									${model.secret == '2' ? 'checked' : ''}> 保密
								</label>
							</div>
					</div>
					<div class="form-group">
							<label class="control-label col-md-1" for="orgInputUser_priority"><font
								style="color: red">*</font>排序号</label>
							<div class="col-sm-5">
								<input id="userBase_priority" type="text" name="priority"
									value="${model.priority}" size="40"
									class="form-control required number"
									onkeyup="this.value=this.value.replace(/\D/g,'')"
									onafterpaste="this.value=this.value.replace(/\D/g,'')"
									<%-- onkeyup="if(this.value.length==1){this.value=this.value.replace(/[^1-9]/g,'')}else{this.value=this.value.replace(/\D/g,'')}"
				   onafterpaste="if(this.value.length==1){this.value=this.value.replace(/[^1-9]/g,'0')}else{this.value=this.value.replace(/\D/g,'')}"--%>
                                   data-rule-range="[0,10000]"
									autocomplete="off">
							</div>
						</div>
						
						<c:if test="${typeID == 'personUpdate'}">
							<div class="form-group">
								<label class="control-label col-md-1" for="orgInputUser_priority"><span
									style="color: red">*</span>在职状态</label>
								<div class="col-sm-5" id="quitFlagId">
									<label for="fertility_condition1" class="radio inline col-md-3"> <input 
										id="quit_flag1" type="radio" name="quitFlag" value="0"
										class="required"
										${(model.quitFlag == '0') ? 'checked' : ''}>
										在职
									</label> <label for="fertility_condition2" class="radio inline col-md-3"> <input
										id="quit_flag2" type="radio" name="quitFlag" value="1"
										class="required" ${model.quitFlag == '1' ? 'checked' : ''}>
										离职
									</label> <label for="fertility_condition2" class="validate-error" generated="true"
										style="display: none;"></label>
								</div>
							</div>
							
							<div class="form-group" id="leaveDateId" >
							<c:if test="${model.quitFlag == '1' }">
								<label class="control-label col-md-1" for="orgInputUser_priority">
								<span style="color: red">*</span>工资结算日期</label>
								<div class="col-sm-5" style="padding-left: 30px;">
									<input autocomplete="off" placeholder="请选择时间" type="text" id="leaveDate" name="leaveDate" onclick="WdatePicker({dateFmt:'yyyy-MM-dd'})" class="Wdate" style="width:200px;background-color:#eee;padding-left:10px;" value="<fmt:formatDate value="${model.leaveDate }" pattern="yyyy-MM-dd" /> "/>
								</div>
							</c:if>
							</div>
							<div class="form-group" id="quitTimeId" >
								<c:if test="${model.quitFlag == '1' }">
									<label class="control-label col-md-1" for="orgInputUser_priority">
									<span style="color: red">*</span>禁止登录系统日期</label>
									<div class="col-sm-5" style="padding-left: 55px;">
										<input autocomplete="off" placeholder="请选择时间" type="text" id="quitTime" name="quitTime" onclick="WdatePicker({dateFmt:'yyyy-MM-dd'})" class="Wdate" style="width:200px;background-color:#eee;padding-left:10px;" value="<fmt:formatDate value="${model.quitTime }" pattern="yyyy-MM-dd" /> "/>
									</div>
								</c:if>
							</div>
							
						</c:if>
					<div class="form-group">
							<div class="col-md-offset-1 col-md-5">

								<button id="submitButton" type="submit"
									class="btn btn-default a-submit"   >
									<spring:message code='core.input.save' text='保存' />
								</button>
								<button type="button" onclick="window.parent.$('#popWinClose').click();" class="btn btn-default"><spring:message code='core.input.back' text='关闭'/></button>
							</div>
					</div>
				
	</div>
</form>
</div>
</div>

	</section>
	<!-- end of main -->


</body>

<script>
var editLevel=$("#level").val();
var levelArray=editLevel.split('-');

$(function () {
	
	
	//到数据库中取 第一级 级别
    $.getJSON('${tenantPrefix}/rs/business/levels', {}, function (data) {
		var option = "<option value=''>请选择</option>";
        for (var i = 0; i < data.length; i++) {
            //alert(JSON.stringify(data[i]));
          
            if(levelArray.length==2&&levelArray[0]==data[i].level){
            	option += "<option value='" + data[i].level + "' selected='selected'>" + data[i].level+"级" + "</option>"
            	
            }
            else
            	option += "<option value='" + data[i].level + "' >" + data[i].level +"级"+"</option>"
        }
        $("#level_1").html(option);//将循环拼接的字符串插入第二个下拉列表
        getLevelSub();
        
	});
});

//	取二级  级别
function getLevelSub() {
    var myselect = document.getElementById("level_1");
    var index = myselect.selectedIndex;
    var t = myselect.options[index].value;
	
    $.getJSON('${tenantPrefix}/rs/business/levelSub', {t:t}, function (data) {
		var option = "<option value=''>请选择</option>";
        for (var i = 0; i < data.length; i++) {
            //alert(JSON.stringify(data[i]));
            if(levelArray.length==2&&levelArray[1]==data[i].level_sub){
            	option += "<option value='" + data[i].level_sub + "' selected='selected' >" + data[i].level_sub+ "</option>"
            	
            }
            else
            	option += "<option value='" + data[i].level_sub + "' >" + data[i].level_sub + "</option>"
        }
        $("#level_2").html(option);//将循环拼接的字符串插入第二个下拉列表
        
       
	});
}

//	保存级别
function saveLevel() {
    var myselect = document.getElementById("level_1");
    var index = myselect.selectedIndex;
    var L1 = myselect.options[index].value;

    var myselect = document.getElementById("level_2");
    var index = myselect.selectedIndex;
    var L2 = myselect.options[index].value;

    var level_name = L1+'-'+L2;
    
    $("#level").val(level_name);

}
    
</script>


</html>








