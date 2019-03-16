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

            var applyCode = document.getElementById('applyCode').value;
            
            $("#userBaseForm").validate({
                submitHandler: function (form) {
                    bootbox.animate(false);
                    var box = bootbox.dialog('<div class="progress progress-striped active" style="margin:0px;text-align:center;"><div class="bar" style="width: 100%;text-align:center;">正在提交数据...</div></div>');
                    form.submit();
                },
                errorClass: 'validate-error',
                rules: {
                	
//                     username: {
//                         remote: {
//                             url: 'account-info-checkUsername.do',
//                             data: {
//                                 <c:if test="${model != null}">
//                                 id: function () {
//                                     return $('#userBase_id').val();
//                                 }
//                                 </c:if>
//                             }
//                         }
//                     },
//                     employeeNo: {
//                         remote: {
//                             url: 'account-info-checkEmployeeNo.do',
//                             data: {
//                                 <c:if test="${model != null}">
//                                 id: function () {
//                                     return $('#userBase_id').val();
//                                 }
//                                 </c:if>
//                             }
//                         }
//                     },
                    
// 					//	验证受理单编号 是否 唯一
//                     applyCode: {
                    	
//                         remote: {
                        	
                        	
//                         	url: '${tenantPrefix}/rs/business/applyCodeIfExist',      
//             	            datatype: "json",
//             	            data:{"applyCode": applyCode},
//             	            type: 'get',      
//             	            success: function (e) {
//             	            	//成功后回调   
//             	            	if (e == "-1") {
//             	            		alert("系统正在处理，请耐心等待！");  
//             	            	} else {
//             	            		$("#applyCode").val(e);		
//             	            	}
//             	            },      
//             	            error: function(e){      
//             	            	loading.modal('hide');
//             	            	//失败后回调      
//             	                alert("服务器请求失败,请重试");  
//             	                $("#submitButton").removeAttr("disabled");//将按钮可用 
//             	            }
//                         }
//                     }
                    
                    
                },
                messages: {
//                     username: {
//                         remote: "<spring:message code='user.user.input.duplicate' text='存在重复账号'/>"
//                     },
//                     employeeNo: {
//                         remote: "<spring:message code='user.user.input.duplicate' text='存在重复员工编号'/>"
//                     }
               
                }
            });
            
            var sectionJson = [{"begin": "#pickerStartTime", "end": "#pickerEndTime"}];
            fnTaskSectionPickerTime(sectionJson)
        })
    </script>
    
     <script>
        //入职时间  合同到期时间
        var fnTaskSectionPickerTime = function (eleTimes) {
            $(eleTimes).each(function (i, ele) {
                $(ele.begin + " span").remove();
                $(ele.end + " span").remove();

                $(ele.begin + " input").css("width", "835px");
                $(ele.end + " input").css("width", "835px");

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


                $(ele.begin + " input").attr("onclick", "WdatePicker({maxDate:'#F{$dp.$D(\\'" + end + "\\',{H:-1})||\\'2020-10-01\\'}',dateFmt:'yyyy-MM-dd HH:00:00'})");
                $(ele.end + " input").attr("onclick", "WdatePicker({minDate:'#F{$dp.$D(\\'" + begin + "\\',{H:+1})}',maxDate:'2020-10-01',dateFmt:'yyyy-MM-dd HH:00:00'})");
                //开始时间
                $(ele.begin + " .glyphicon-calendar").click(function () {
                    $(ele.begin + " input").click();
                })
                $(ele.end + " .glyphicon-calendar").click(function () {
                    $(ele.end + " input").click();
                })

            })
        }
    </script>
<style type="text/css">
label {
	white-space: nowrap
}
</style>
<meta http-equiv="Content-Type" content="multipart/form-data; charset=utf-8" />
</head>

<body>

<div class="row-fluid">

		<c:if test="${not empty flashMessages}">
			<div id="m-success-message" style="display: none;">
				<ul>
					<c:forEach items="${flashMessages}" var="item">
						<c:if test="${item != ''}">
							<li>${item}</li>
						</c:if>
					</c:forEach>
				</ul>
			</div>
		</c:if>
		<!-- start of main -->
		<section id="m-main" class="col-md-12" style="padding-top: 3px;">



	<div class="panel panel-default">
		<div class="panel-body">

					<form id="userBaseForm" method="post" enctype="multipart/form-data"
						class="form-horizontal">

						<input id="userBase_id" type="hidden" name="id"
							value="${model.id}">
							
					
						
<!-- 							<div class="form-group"> -->
<!-- 								<label class="control-label col-md-1">上级机构</label> -->
<!-- 								<div class="col-sm-5"> -->
								
<!-- 								<input id="departmentName" type="text" name="departmentName" -->
<%-- 									value="${model.departmentName}" size="40" --%>
<!-- 									class="form-control required" minlength="2" maxlength="50" readonly> -->
<!-- 								</div> -->
								
<!-- 							</div> -->
							
							
							<div class="form-group">
								<label class="control-label col-md-1">上级机构</label>
								<div class="col-sm-5">
									<div class="input-group orgPicker">
										<input id="_task_name_key" type="hidden" name="partyEntityId"
											value="${model.departmentCode}"> <input type="text"
											class="form-control required" id="departmentName"
											name="departmentName" placeholder=""
											value="${model.departmentName}" minlength="2" maxlength="50"
											readonly="readonly">
										
									</div>
								</div>
								<input id="org_level" type="hidden" name="partyLevel"
									value="${partyEntity.level}">
							</div>
							
							
							
						
						<div class="form-group">
							<label class="control-label col-md-1" for="userBase_username"><font
								color="red"></font>
							<spring:message code="user.user.input.username" text="用户名" /></label>
							<div class="col-sm-5">
								<input id="userBase_username" type="text" name="username"
									value="${model.username}" size="40"
									class="form-control required" minlength="2" maxlength="50" readonly>
							</div>

							<label class="control-label col-md-1" for="org_orgname"><font
								color="red"></font>工号</label>
							<div class="col-sm-5">
							
									<input id="employeeNo" type="text" name="employeeNo"
									value="${model.employeeNo}" size="40"
									 minlength="2" class="form-control"
									maxlength="50" readonly>
							</div>
						</div>

						<div class="form-group">
							<label class="control-label col-md-1" for="org_orgname"><font
								color="red"></font>
							<spring:message code="org.org.input.orgname" text="姓名" /></label>
							<div class="col-sm-5">
								<input type="text" class="form-control required" minlength="2"
									maxlength="50" id="userBase_fullName" name="fullName"
									placeholder="" value="${model.fullName}" readonly>
							</div>

							<label class="control-label col-md-1" for="orgInputUser_status">性别</label>
							<div class="col-sm-5">
								
									<c:if test="${model.gender == '1'}">
										<input id="gender" type="text" name="gender"
										value="男" size="40" class="form-control"
										minlength="2" maxlength="32"   readonly>
									</c:if>
									
									<c:if test="${model.gender == '2'}">
										<input id="gender" type="text" name="gender"
										value="女" size="40" class="form-control"
										minlength="2" maxlength="32"   readonly>
									</c:if>
							</div>
						</div>
	
						<div class="form-group">
						
					<c:if test="${PersonTypeID == 'personadd'}">
						
                        <label class="control-label col-md-1" for="org_orgname">
							<spring:message code="org.org.input.orgname" text="岗位" /> </label>
							<div class="col-sm-5" class="form-control required">	
								<input id="_task_name_key" type="hidden" name="postId"
	                                       value="${model.postId}">
								<input id="postName" type="text" name="postName"
									value="${model.postName}" size="40" class="form-control"
									minlength="2" maxlength="32"   readonly>	
							</div>
	                 </c:if>       
	                    
	                        <label class="control-label col-md-1" for="org_orgname"><font
								color="red"></font>
							<spring:message code="org.org.input.orgname" text="级别" /> </label>
							<div class="col-sm-5" class="form-control required">	
							
								<input id="level" type="text" name="level"
									value='${model.level!=null?model.level.replace("-","级-"):""}' size="40" class="form-control"
									 maxlength="32"   readonly>	
							</div>
	                    </div>
							
						<div class="form-group">
							<label class="control-label col-md-1" for="org_orgname"><font
								color="red"></font>
							<spring:message code="org.org.input.orgname" text="职位" /> </label>
							<div class="col-sm-5">
								<input id="positionCode" type="text" name="positionCode"
									value="${model.positionName}" size="40" class="form-control"
									minlength="2" maxlength="32"   readonly>
							
							</div>
							
							<label class="control-label col-md-1" for="userBase_title">职称</label>
							<div class="col-sm-5">
									
								<input id="userBase_title" type="text" name="title"
									value="${model.title}" size="40" class="form-control"
									minlength="2" maxlength="32"   readonly>
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
							<label class="control-label col-md-1" for="userBase_telephone"><font color='red'></font>联系电话</label>
							<div class="col-sm-5">
								<input id="userBase_telephone" type="text" name="telephone"
									value="${model.telephone}" size="40" class="form-control"
									minlength="2" maxlength="32"   readonly>
							</div>

							<label class="control-label col-md-1" for="userBase_cellphone">备用联系电话</label>
							<div class="col-sm-5">
								<input id="userBase_cellphone" type="text" name="cellphone"
									value="${model.cellphone}" size="40" class="form-control"
									minlength="2" maxlength="32" readonly>
							</div>
					</div>
				
					<div class="form-group">
							<label class="control-label col-md-1" for="userBase_email"><font
								color="red"></font>邮箱</label>
							<div class="col-sm-5">
								<input id="userBase_email" type="text" name="email"
									value="${model.email}" size="40"
									class="form-control required email" minlength="2"
									maxlength="50" readonly>
							</div>
						
							<label class="control-label col-md-2" for="userBase_fax">紧急联系人及电话</label>
							<div class="col-sm-4">
								<input id="userBase_fax" type="text" name="fax"
									value="${model.fax}" size="40" class="form-control"
									minlength="2" maxlength="32" readonly>
							</div>
						
					</div>

					<div class="form-group">
							<label class="control-label col-md-1" for="userBase_wxNo">微信</label>
							<div class="col-sm-5">
								<input id="userBase_wxNo" type="text" name="wxNo"
									value="${model.wxNo}" size="40" class="form-control"
									minlength="2" maxlength="32" readonly>
							</div>
						
							<label class="control-label col-md-1" for="userBase_qq">QQ</label>
							<div class="col-sm-5">
								<input id="userBase_qq" type="text" name="qq"
									value="${model.qq}" size="40" class="form-control"
									minlength="2" maxlength="16" readonly>
							</div>
					</div>

					<div class="form-group">
					
						<label class="control-label col-md-1" for="userBase_nameBefore">曾用名</label>
							<div class="col-sm-5">
								<input id="name_used_before" type="text" name="nameBefore"
									value="${model.nameBefore}" size="40" class="form-control"
									 minlength="2" maxlength="50" readonly>
							</div>
					
<!-- 					class="form-control required email" -->
						<label class="control-label col-md-1" for="userBase_identityID"><font
								color="red"></font>身份证号</label>
							<div class="col-sm-5">
								<input id="identity_Card" type="text" name="identityID"
									value="${model.identityID}" size="40" class="form-control"
									 minlength="2"  maxlength="50" readonly>
							</div>
					</div>
					
					<div class="form-group">
							<label class="control-label col-md-1" for="native_place"><font
								color="red"></font>籍贯</label>
							<div class="col-sm-5">
								<input id="native_place" type="text" name="nativePlace"
									value="${model.nativePlace}" size="40" class="form-control"
									 minlength="2"
									maxlength="50" readonly>
							</div>
						
							<label class="control-label col-md-1" for="registered_residence"><font
								color="red"></font>户口所在地</label>
							<div class="col-sm-5">
								<input id="registered_residence" type="text" name="registeredResidence"
									value="${model.registeredResidence}" size="40" class="form-control"
									 minlength="2"
									maxlength="50" readonly>
							</div>
							
					</div>
						
					<div class="form-group">
							<label class="control-label col-md-1" for="householdRegisterType"><font
								color="red"></font>
							<spring:message code="org.org.input.orgname" text="户籍类型" /> </label>
							<div class="col-sm-5">
									<input id="householdRegisterType" type="text" name="householdRegisterType"
									value='<tags:dicTitle typeName="householdRegisterType" dicValue="${model.householdRegisterType}"/>' size="40" class="form-control"
									 maxlength="32"   readonly>
							</div>
						
	 						<label class="control-label col-md-1" for="userBase_politicalOutlook"><font
								color="red"></font>政治面貌</label>
							<div class="col-sm-5">
								<input id="political_outlook" type="text" name="politicalOutlook"
									value='<tags:dicTitle typeName="householdRegisterType" dicValue="${model.politicalOutlook}"/>' size="40" class="form-control"
									maxlength="50" readonly>
							</div>
					</div>
						
					<div class="form-group">
							<label class="control-label col-md-1" for="nation"><font
								color="red"></font>民族</label>
							<div class="col-sm-5">
								<input id="nation" type="text" name="nation"
									value='<tags:dicTitle typeName="nation" dicValue="${model.nation}"/>' size="40" class="form-control"
									maxlength="50" readonly>
							</div>
						
							<label class="control-label col-md-1" for="education"><font
								color="red"></font>学历</label>
							<div class="col-sm-5">
								<input id="education" type="text" name="education"
									value='<tags:dicTitle typeName="education" dicValue="${model.education}"/>' size="40" class="form-control"
									maxlength="50" readonly>
							</div>
					</div>
						
				<div class="form-group">
							<label class="control-label col-md-1" for="major"><font
								color="red"></font>专业</label>
							<div class="col-sm-5">
								<input id="major" type="text" name="major"
									value="${model.major}" size="40" class="form-control"
									 minlength="2"
									maxlength="50" readonly>
							</div>
						
							<label class="control-label col-md-1" for="academic_degree"><font
									color="red"></font>学位</label>
								<div class="col-sm-5">
									<input id="academic_degree" type="text" name="academicDegree"
										value='<tags:dicTitle typeName="academicDegree" dicValue="${model.academicDegree}"/>' size="40" class="form-control"
										 minlength="2"
										maxlength="50" readonly>
								</div>
							
					</div>
						
					<div class="form-group">
							<label class="control-label col-md-1" for="skill_specialty">技能特长</label>
							<div class="col-sm-5">
								<input id="skill_specialty" type="text" name="skillSpecialty"
									value="${model.skillSpecialty}" size="40" class="form-control"
									 minlength="2"
									maxlength="50" readonly>
							</div>
							
							<label class="control-label col-md-1" for="labor_type"><font
								color="red"></font>用工类型</label>
							<div class="col-sm-5">
								<input id="labor_type" type="text" name="laborType"
									value='<tags:dicTitle typeName="laborType" dicValue="${model.laborType}"/>' size="40" class="form-control"
									maxlength="50" readonly>
							</div>
							
					</div>
						
					<div class="form-group">
							<label class="control-label col-md-1" for="entry_mode"><font
								color="red"></font>进入方式</label>
							<div class="col-sm-5">
								<input id="entry_mode" type="text" name="entryMode"
									value='<tags:dicTitle typeName="entryMode" dicValue="${model.entryMode}"/>' size="40" class="form-control"
									 minlength="2"
									maxlength="50" readonly>
							</div>
							
							<label class="control-label col-md-1" for="contract_company"><font
								color="red"></font>合同单位</label>
							<div class="col-sm-5">
								<input id="contract_company" type="text" name="contractCompany"
									value="${model.contractCompany}" size="40" class="form-control"
									 minlength="2" 
									maxlength="50" readonly>
							</div>
							
					</div>
						
		
					<div class="form-group">
							<label class="control-label col-md-1" for="entry_time">
							   <span style="color:red;">  </span> 入职时间</label> 
							
							<div class="col-sm-5">
	                            <div id="pickerStartTime" class="input-group date">
	                                <input id="entry_time" type="text" name="entryTime"
	                                       value="<fmt:formatDate value='${model.entryTime}' type="both" pattern='yyyy-MM-dd HH:mm'/>"
	                                       readonly style="background-color:white;cursor:default;"
	                                       class="form-control required" readonly>
	                                
	                            </div>
                        	</div>
                        	
                        	<label class="control-label col-md-1" for="contract_expiration_time"><font
									color="red"></font>合同到期时间</label>
							<div class="col-sm-5">
								  <div id="pickerEndTime" class="input-group date">
		                                <input id="contract_expiration_time" type="text" name="contractExpirationTime"
		                                       value="<fmt:formatDate value='${model.contractExpirationTime}' type="both" pattern='yyyy-MM-dd HH:mm'/>"
		                                       readonly
		                                       style="background-color:white;cursor:default;" class="form-control required" readonly>
		                                
                            	</div>
							</div>
                   </div>
					<div class="form-group">
							<label class="control-label col-md-1" for="contract_deadline"><font
								color="red"></font>合同有效期</label>
							<div class="col-sm-5">
								<input id="contract_deadline" type="text" name="contractDeadline"
									value="${model.contractDeadline}" size="40" class="form-control"
									 minlength="2"
									maxlength="50" readonly>
							</div>
							
							<label class="control-label col-md-1" for="document">资料情况</label>
							<div class="col-sm-5">
								<input id="document" type="text" name="document"
									value="${model.document}" size="40" class="form-control"
									 minlength="2"
									maxlength="50" readonly>
							</div>
							
					</div>

					<div class="form-group">
							<label class="control-label col-md-1" for="insurance"><font
								color="red"></font>保险情况</label>
							<div class="col-sm-5">
								<input id="insurance" type="text" name="insurance"
									value="${model.insurance}" size="40" class="form-control"
									 minlength="2"
									maxlength="50" readonly>
							</div>
				
							<label class="control-label col-md-1" for="userBase_cellphone"><font
								color="red"></font>现住址</label>
								<div class="col-sm-5">
									<input id="userBase_address" type="text" name="address"
										value="${model.address}" size="40" class="form-control"
										minlength="2" maxlength="50" readonly> 
								</div>
				</div>
				
				
				<div class="form-group">
							<label class="control-label col-md-1" for="family_1">家庭成员1</label>
							<div class="col-sm-5">
								<input id="family_1" type="text" name="family_1"
									value="${model.family_1}" size="40"
									 minlength="2" class="form-control"
									maxlength="50" readonly>
							</div>
					
							<label class="control-label col-md-1" for="family_2">家庭成员2</label>
							<div class="col-sm-5">
								<input id="family_2" type="text" name="family_2"
									value="${model.family_2}" size="40"
									 minlength="2" class="form-control"
									maxlength="50" readonly>
							</div>
						
					</div>
					
					<div class="form-group">
							<label class="control-label col-md-1" for="orgInputUser_status">婚否</label>
							<div class="col-sm-5">
								
									<c:if test="${model.marriage == '1'}">
										<input id="marriage" type="text" name="marriage"
										value="已婚" size="40" class="form-control"
										minlength="2" maxlength="32"   readonly>
									</c:if>
									
									<c:if test="${model.marriage == '2'}">
										<input id="marriage" type="text" name="marriage"
										value="未婚" size="40" class="form-control"
										minlength="2" maxlength="32"   readonly>
									</c:if>
							</div>


							<label class="control-label col-md-1" for="fertilityCondition">生育情况</label>
							<div class="col-sm-5">
							
									<c:if test="${model.fertilityCondition == '1'}">
										<input id="fertilityCondition" type="text" name="fertilityCondition"
										value="已育 " size="40" class="form-control"
										minlength="2" maxlength="32"   readonly>
									</c:if>
									
									<c:if test="${model.fertilityCondition == '2'}">
										<input id="fertilityCondition" type="text" name="fertilityCondition"
										value="未育" size="40" class="form-control"
										minlength="2" maxlength="32"   readonly>
									</c:if>
							</div>
					
					</div>
					
					<c:if test="${PersonTypeID == 'personUpdate'}">
							<div class="form-group">
								<label class="control-label col-md-1" for="orgInputUser_priority">在职状态</label>
								<div class="col-sm-5">
									${(model.quitFlag == '0') ? '在职' : '离职'}
									
								</div>
							</div>
							
							<div class="form-group">
								<label class="control-label col-md-1" for="orgInputUser_priority">结算工资日期</label>
								<div class="col-sm-5" style="padding-left: 30px;">
									<span><fmt:formatDate value='${model.leaveDate}' pattern='yyyy-MM-dd'/></span>
								</div>
							</div>
							
							<div class="form-group">
								<label class="control-label col-md-1" for="orgInputUser_priority">禁止登录系统日期</label>
								<div class="col-sm-5" style="padding-left: 55px;">
									<span><fmt:formatDate value='${model.quitTime}' pattern='yyyy-MM-dd'/></span>
								</div>
							</div>
						</c:if>
					
					
					<div class="form-group">
							<label class="control-label col-md-1" for="educational_experience_1">教育经历1</label>
							<textarea name="educational_experience_1" rows="2" cols="20" id="educational_experience_1"
                                          class="text0" 
                                          style="height: 99px; width: 835px; padding-left: 10px; padding-top: 10px" readonly>${model.educational_experience_1}</textarea>
                        	
					</div>	
					
					<div class="form-group">
							<label class="control-label col-md-1" for="educational_experience_2">教育经历2</label>
							<textarea name="educational_experience_2" rows="2" cols="20" id="educational_experience_2"
                                          class="text0"
                                          style="height: 99px; width: 835px; padding-left: 10px; padding-top: 10px" readonly>${model.educational_experience_2}</textarea>
                        	
					</div>
		
					<div class="form-group">
							<label class="control-label col-md-1" for="educational_experience_3">教育经历3</label>
					
							<textarea name="educational_experience_3" rows="2" cols="20" id="educational_experience_3"
                                          class="text0"
                                          style="height: 99px; width: 835px; padding-left: 10px; padding-top: 10px" readonly>${model.educational_experience_3}</textarea>
                        	
					</div>

					<div class="form-group">
							<label class="control-label col-md-1" for="work_experience_1">工作经历1</label>
							
							<textarea name="work_experience_1" rows="2" cols="20" id="work_experience_1"
                                      class="text0"
                                      style="height: 99px; width: 835px; padding-left: 10px; padding-top: 10px" readonly>${model.work_experience_1}</textarea>
                        	
							
					</div>	
						
					<div class="form-group">	
							<label class="control-label col-md-1" for="work_experience_2">工作经历2</label>
							
								<textarea name="work_experience_2" rows="2" cols="20" id="work_experience_2"
                                          class="text0"
                                          style="height: 99px; width: 835px; padding-left: 10px; padding-top: 10px" readonly>${model.work_experience_2}</textarea>
                        
					</div>
			
					<div class="form-group">
							<label class="control-label col-md-1" for="userBase_remark">备注</label>
							
								<textarea name="remark" rows="2" cols="20" id="remark"
                                          class="text0"
                                          style="height: 99px; width: 835px; padding-left: 10px; padding-top: 10px" readonly>${model.remark}</textarea>
                
                	</div>
                	
                	<div class="form-group">
							<label class="control-label col-md-1" for="userBase_status">启用</label>
							<div class="col-sm-1">
								<input id="userBase_stopFlag" type="checkbox" name="stopFlag"
									value="active"
									${model.stopFlag == 'active' || model.stopFlag == null ? 'checked' : ''}>
							</div>

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
				    <div class="col-md-offset-1 col-sm-5" style="text-align:center;margin:0 auto;">
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
    
    $(function () {
    	

    	//到数据库中取 第一级 级别
        $.getJSON('${tenantPrefix}/rs/business/levels', {}, function (data) {
			var option = "<option value=''>请选择</option>";
            for (var i = 0; i < data.length; i++) {
                //alert(JSON.stringify(data[i]));
                option += "<option value='" + data[i].LEVEL + "' >" + data[i].LEVEL + "</option>"
            }
            $("#level_1").html(option);//将循环拼接的字符串插入第二个下拉列表
            
		});
    });
    
    //	取二级  级别
    function getLevelSub() {
        var myselect = document.getElementById("level_1");
        var index = myselect.selectedIndex;
        var t = myselect.options[index].text;

        $.getJSON('${tenantPrefix}/rs/business/levelSub', {t:t}, function (data) {
			var option = "<option value=''>请选择</option>";
            for (var i = 0; i < data.length; i++) {
                //alert(JSON.stringify(data[i]));
                option += "<option value='" + data[i].level_sub + "' >" + data[i].level_sub + "</option>"
            }
            $("#level_2").html(option);//将循环拼接的字符串插入第二个下拉列表
           
		});
    }
    
    //	保存级别
    function saveLevel() {
        var myselect = document.getElementById("level_1");
        var index = myselect.selectedIndex;
        var L1 = myselect.options[index].text;

        var myselect = document.getElementById("level_2");
        var index = myselect.selectedIndex;
        var L2 = myselect.options[index].text;

        var level_name = L1+'-'+L2;
        
        $("#level").val(level_name);

    }
</script>


</html>








