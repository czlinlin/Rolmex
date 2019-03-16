<%@page contentType="text/html;charset=UTF-8" %>
<%@include file="/taglibs.jsp" %>
<%pageContext.setAttribute("currentHeader", "bpm-workspace");%>
<%pageContext.setAttribute("currentMenu", "bpm-process");%>
<!doctype html>
<html lang="en">
<head>
    <%@include file="/common/meta.jsp" %>

    <title>麦联</title>
    <%@include file="/common/s3.jsp" %>
    <link type="text/css" rel="stylesheet" href="${cdnPrefix}/userpicker3-v2/userpicker.css">
    <script type="text/javascript" src="${cdnPrefix}/userpicker3-v2/userpickercustomapprove.js"></script>
	<script type="text/javascript" src="${cdnPrefix}/bootbox/bootbox.min1.js"></script>
    <script type="text/javascript" src="${cdnPrefix}/operation/operation.js?v=1.20"></script>
    <script type="text/javascript" src="${cdnPrefix}/popwindialog/popwin.js"></script>
    <script type="text/javascript">
    var leaderId = "";
    var leaderName = "";
    var selectIds = "";
    $(function(){
    	leaderId = $("#leaderId").val();
        leaderName = $("#leaderName").val();
        if(leaderId == ""){
        	$("#deleDiv").attr("style","display:none");
        	$("#deleDiv").next("i").attr("style","display:none");
        }
        selectIds = $("#iptOldSelectIds").val();
    });
         /* $(function () {
        	alert("ssssss"); */
            //注册负责人弹出
            /* createUserPicker({
                modalId: 'leaderPicker',//这个 其实是弹出的窗口的div id
                targetId: 'leaderDiv', //这个是点击哪个 会触发弹出窗口
                showExpression: true,
                searchUrl: '${tenantPrefix}/rs/user/search',
                treeUrl: '${tenantPrefix}/rs/party/treeNoAuth?partyStructTypeId=1',
                childUrl: '${tenantPrefix}/rs/party/searchUser'
            }); */
            
            //审批人
          /*   createUserPicker({
                modalId: 'leaderPicker',//这个 其实是弹出的窗口的div id
                targetId: 'leaderDiv', //这个是点击哪个 会触发弹出窗口
                inputStoreIds:{iptid:"leaderId",iptname:"leaderName"},//存储已选择的ID和name的input的id
                //auditId:'ulapprover',//显示审批步骤
                showExpression: true,
                multiple: false,
                searchUrl: '${tenantPrefix}/rs/user/searchVNoMe',
                treeUrl: '${tenantPrefix}/party/asyncTree.do?partyStructTypeId=1&notViewPost=true&notAuth=false',
                childUrl: '${tenantPrefix}/rs/party/searchUserNoMe'
            });
            
            leaderId = $("#leader").val();
            leaderName = $("#leaderName").val();
            
            alert(leaderId);
            alert(leaderName);
    		 
        })  */
//         //若该条流程 是审批   修改花名册  那么显示花名册的详情页
//         function openPersonInfo(){
//         window.open('${tenantPrefix}/user/person-info-input-forConfirm.do?applyCode=${customEntity.applyCode}&id=${personInfoId}&partyEntityId=${partyEntityId}','','width=1400,height=850,top=80,left=220,toolbar=no, menubar=no, scrollbars=yes, resizable=no');

//         }
        
       
      //若该条流程 是   花名册  那么显示花名册的详情页
        function openPersonInfo(){
			popWin.scrolling="auto";
			popWin.showWin("1080"
	    			,"600"
	    			,"花名册"
	    			,"${tenantPrefix}/user/person-info-input-forConfirm.do?applyCode=${customEntity.applyCode}&id=${personInfoId}&partyEntityId=${partyEntityId}");
        	
        }
      //若该条流程 是调岗    那么显示调岗的详情页
	    function changePost(){
	    	popWin.showWin("768"
	    			,"400"
	    			,"岗位调整信息"
	    			,"${tenantPrefix}/user/person-info-position-change-forModify.do?applyCode=${customEntity.applyCode}&id=${personInfoId}&isdetail=1");
		}
		
	  //若该条流程 是新建组织机构， 那么跳转到新建组织机构页面
	    function orgCreate(){
	    	popWin.showWin("768"
	    			,"550"
	    			,"组织结构新建信息"
	    			,"${tenantPrefix}/party/org-update-for-audit.do?applyCode=${customEntity.applyCode}&isdetail=1");
		}
	    
	    function orgUpdate(){
	    	popWin.showWin("768"
	    			,"550"
	    			,"组织结构的修改信息"
	    			,"${tenantPrefix}/party/org-update-for-audit.do?applyCode=${customEntity.applyCode}&isdetail=1");
		}
	    
	    function orgRelation(){
	    	popWin.showWin("768"
	    			,"400"
	    			,"岗位关联人员信息"
	    			,"${tenantPrefix}/party/position-user-input-for-audit.do?applyCode=${customEntity.applyCode}&isdetail=1");
	    }

	    var dialog=null;
	    var fnDialog=function(){
	    	$("#divButton button").attr("disabled","disabled");
	    	dialog = bootbox.dialog({
                message:'<p class="text-center"><i class="fa fa-spin fa-spinner"></i>提交中</p>',
                size:'small',
                closeButton: false
            });
	    }
	    
	    var fnDialogClose=function(){
	    	if(dialog!=null)
	    		dialog.modal("hide");
	    	$("#divButton button").removeAttr("disabled");
	    }
	    
        function fnCustomSubmit(flag) {
			var modifyInput = $("#modifyInput").val();
			if(flag=="1"){
				if(modifyInput == '1' || modifyInput == '3'){
					var leaId = $("#leaderId").val();
					if(leaId == '' && leaderId != ''){
						//var title="添加";
						//$("#modifyInput").attr("value","0");
						alert("下一步审核人不能为空");
						return false;
					}else if(leaId == '' && leaderId == ''){
						$("#modifyInput").attr("value","0");
					}
				}
			}
            //根据表单中选择的下一步审批人，查下该人是否已经审批过该条申请，避免重复审批
            /* if ((flag == 1 ) && document.getElementById('leaderId').value != "") {

                isConfirm(document.getElementById('leaderId').value, document.getElementById('processInstanceId').value);
            } else { */
            	fnDialog();
	             //若要驳回或不同意，必须填写意见才能提交
	            if ((flag == 2 || flag == 0) &&(( document.getElementById('comment').value == "") || ( document.getElementById('comment').value == "同意")) ){
	            	fnDialogClose();
	            	alert("请填写批示内容！");
	                return false;
	            }

	         if(document.getElementById('comment').value!=""&&document.getElementById('comment').value.length>300){
	        	 fnDialogClose();
	        	 alert("意见字数最多为300字！");
                 return false;
	        }
	         if(flag==1){
	        	 var currSelectId=$("#leaderId").val();
	        	 if(currSelectId==""){
        		 	if(!confirm("温馨提示：若无下一步审核人，点击【确定】后，流程将结束，请确认？")){
        		 		fnDialogClose();
        		 		return false;
        		 	}
	        	 }
	        	 
		         if(currSelectId!=""){
		        	 var initUserId=$("#iptInitUsreId").val();
		        	 if(initUserId==currSelectId){
		        		 fnDialogClose();
		        		 alert("下一步审核人不能是发起人！");
		        		 return false;
		        	 }
		         
		        	 var oldSelectIds=$("#iptOldSelectIds").val();
		        	 if(oldSelectIds!=""&&(","+oldSelectIds).indexOf(","+currSelectId+",")>-1){
		        		 fnDialogClose();
		        		 alert("下一步审核人不能是流程中已有的审核人！");
		        		 return false;
		        	 }
		         }
	         }
	       //每次审核人审核时都先检验该流程的状态是否是已撤回
            $.ajax({      
	            url: '${tenantPrefix}/rs/bpm/getStatus',      
	            datatype: "json",
	            async:false, 
	            data:{"processInstanceId": $("#processInstanceId").val(),"humanTaskId":$("#humanTaskId").val(),"userId":$("#userId").val()},
	            type: 'get',      
	            success: function (e) {
	            	if(e == 'error'){
	            		fnDialogClose();
	            		alert("该申请已撤回，暂无法审核。");
	            		return false;
	            	}
	            	if(e == 'noAuth'){
	            		fnDialogClose();
	            		alert("您无权审核。");
	            		return false;
	            	}
	            	var conf={
                       "formId":"xform",
                       "checkUrl":"${tenantPrefix}/rs/customer/opteraion-verifyPassword",
                       "actionUrl": '${tenantPrefix}/operationCustom/custom-completeTask.do?flag=' + flag,
       	               "iptPwdId":"txtPrivateKey"
       	            }
	            	
                    operationProcessSubmit(conf);
	            },      
	            error: function(e){      
	            	fnDialogClose();
	                alert("服务器请求失败,请重试");  
	            }
	       });
            
        }
            
        var operationProcessSubmit = function(conf) {
            //验证密码
            var pwd = $("#"+conf.iptPwdId).val();
            if(pwd==""){
            	fnDialogClose();
                alert("请输入操作密码！");
                return false;
            }

            $.ajax({
                url:conf.checkUrl,
                type:"GET",
                data:{pwd: pwd},
                timeout:10000,
                success: function(data) {
                	if(data.code!=200){
                    	fnDialogClose();
                        alert(data.message);
                        return false;
                    }
                    
                    fnDialog();
                    $("#"+conf.formId).attr('action', conf.actionUrl);
                    $("#"+conf.formId).submit();
                },
                error:function(XMLHttpRequest, textStatus, errorThrown){
                	fnDialogClose();
                	alert("验证密码错误，提交失败");
                    return false;
                },
                complete:function(xh,status){
                	fnDialogClose();
                    if(status=="timeout")
                        bootbox.alert("验证密码超时");
                    return false;
                }
            });
        };

        function isConfirm(leaderId, formID) {

        }
        function MaxWords() {

            if ($("#applyContent").val().length == "4000") {
                alert("申请内容字数已达上限4000字");
            }
        }
        
        function modifyLeader(type){
        	$("#iptOldSelectIds").attr("value",selectIds);
        
        	//1 添加   2删除   3  替换
        	if(type != 2){
        		//审批人
            	createUserPicker({
            		type:type,
            		leaderName:leaderName,
                    modalId: 'leaderPicker',//这个 其实是弹出的窗口的div id
                    targetId: 'leaderDiv', //这个是点击哪个 会触发弹出窗口
                    inputStoreIds:{iptid:"leaderId",iptname:"leaderName"},//存储已选择的ID和name的input的id
                    //auditId:'ulapprover',//显示审批步骤
                    showExpression: true,
                    multiple: false,
                    searchUrl: '${tenantPrefix}/rs/user/searchVNoMe',
                    treeUrl: '${tenantPrefix}/party/asyncTree.do?partyStructTypeId=1&notViewPost=true&notAuth=false',
                    childUrl: '${tenantPrefix}/rs/party/searchUserNoMe'
                });
        	}else{
        		var processInstanceId = $("#processInstanceId").val();
        		//下下步审核人  
                $.getJSON('${tenantPrefix}/workOperationCustom/getNextApprover.do', {
                	userId:leaderId,processInstanceId:processInstanceId
                }, function(data) {
                	if(data != ''){
                		$("#leaderId").attr("value",data.id);
                		$("#leaderName").attr("value",data.userName)
                		$("#modifyInput").val("2");
                		
                		var newSelectIds = selectIds.replace(data.id+",", '');
                		$("#iptOldSelectIds").attr("value",newSelectIds);
                		
                	}
                });
        		
        	}
        }
        
    </script>
</head>
<style type="text/css">
    #tb1 td {
        border: 1px solid #BBB
    }

    .f_td {
        width: 120px;
        font-size: 12px;
        white-space: nowrap
    }

    .f_r_td {
        width: 130px;
        text-align: left;
    }

    #tb1 tr td input {
        border: navajowhite;
        text-align: left;
        width: 100%;
    }

    #tb1 tr td textarea {
        border: navajowhite;
    }

    #tb1 tr td {
        text-align: center;
    }
    #tb1 tr td input{border: navajowhite;width: 100%;} 
        #tb1 tr td textarea{border: navajowhite;}     
        #tb1 tr td{text-align:center;line-height:28px;} 
        #tb1 tr td.f_td.f_right{text-align:right;}    
        #tb1 tr td input.input_width{width:auto;}
        #tb1 tr td textarea{padding:3px 3px 0 3px;}
        input{height:27px;}
</style>


<body>
<%@include file="/header/bpm-workspace3.jsp" %>
<form id="xform" method="post" action="${tenantPrefix}/operationApply/process-operationApply-startProcessInstance.do"
      class="xf-form" enctype="multipart/form-data">
    申请单
    <br/>

    <div class="container">

        <section id="m-main" class="col-md-12" style="padding-top:65px;">
        	<input id="theme" name="theme" type="hidden" value="${customEntity.theme }">
			<input id="formType" name="formType" type="hidden" value="${customEntity.formType }">
            <input id="processInstanceId" name="processInstanceId" type="hidden"
                   value="${customId}">
            <input id="isConfirmed" name="isConfirmed" type="hidden">
            <input id="humanTaskId" name="humanTaskId" type="hidden" value="${humanTaskId}">
            <input id="userId" name="userId" type="hidden" value="<%=userId %>">

            <table id="tb1" style="width:100%;">
                <tr>
                    <td colspan='4' align='center' class='f_td'>
                        <h2>${customEntity.theme }单</h2>
                    </td>
                </tr>
                <tr>
                    <td align='right'>
                        提交次数：
                    </td>
                    <td>
                        ${customEntity.submitTimes}<input id="submitTimes" style="display:none" name="submitTimes" value="${customEntity.submitTimes}" readonly>
                    </td>
                    <td align='right'>
                        受理单编号：
                    </td>
                    <td>
                        ${customEntity.applyCode}<input id="applyCode" style="display:none" name="applyCode" value="${customEntity.applyCode}" readonly>
                    </td>
                </tr>

                <tr>
                    <td>
                    <span id='tag_toStart'>&nbsp;姓名</span>：
                </td>
                <td>
                      <input id="name" name="name" value="${customEntity.name}" readonly>
                      
                </td>
                <td>
                    <span id='tag_toStart'>&nbsp;部门</span>：
                </td>
                <td>
                      <input id="departmentName" name="departmentName" value="${customEntity.departmentName}" readonly>
                </td>
                </tr>
                <tr>
	                <td>
	                 	类型：
	                </td>
	                <td style="text-align:left;">
					<c:if test="${customEntity.formType == '1' }">
						<c:if test="${customEntity.type == '1' }">
							病假
						</c:if>
						<c:if test="${customEntity.type == '2' }">
							事假
						</c:if>
						<c:if test="${customEntity.type == '3' }">
							倒休假
						</c:if>
						<c:if test="${customEntity.type == '4' }">
							年假
						</c:if>
						<c:if test="${customEntity.type == '5' }">
							补休假
						</c:if>
						<c:if test="${customEntity.type == '6' }">
							婚假
						</c:if>
						<c:if test="${customEntity.type == '7' }">
							产假
						</c:if>
						<c:if test="${customEntity.type == '8' }">
							丧假
						</c:if>
						<c:if test="${customEntity.type == '9' }">
							其他
						</c:if>
					</c:if> 
					<c:if test="${customEntity.formType == '2' }">
						<c:if test="${customEntity.type == '1' }">
							出差
						</c:if>
						<c:if test="${customEntity.type == '2' }">
							因公外出
						</c:if>
						<c:if test="${customEntity.type == '3' }">
							其他
						</c:if>
					</c:if>   
					<c:if test="${customEntity.formType == '3' }">
						加班
					</c:if>
					<c:if test="${customEntity.formType == '4' }">
						<c:if test="${customEntity.type == '1' }">
							销假
						</c:if>
						<c:if test="${customEntity.type == '2' }">
							迟到
						</c:if>
						<c:if test="${customEntity.type == '3' }">
							临时外出
						</c:if>
						<c:if test="${customEntity.type == '4' }">
							漏打卡
						</c:if>
						<c:if test="${customEntity.type == '5' }">
							其他
						</c:if>
					</c:if>              
	                </td>
	                <td>
	                 	时间：
	                </td>
	                <td style="text-align:left;">
	                   ${customEntity.date}
	                </td>
	            </tr>
                <tr>
                    <td>
                        <span id='tag_depart'>&nbsp;抄送</span>：
                    </td>
                    <td colspan='3' style="text-align:left;">
                    	${customEntity.ccName}
                        <input name="ccName" id="ccName" style="display:none;" value="${customEntity.ccName}" readOnly>
                    </td>
                </tr>

                <tr>
                	<td>
                        <span id='tag_toStart'>&nbsp;业务级别</span>：
                    </td>
                    <td>
                        <input id="name" name="name" value="${customEntity.businessLevel}" readonly>
                    </td>
                    <td>
                        <span id='tag_toStart'>&nbsp;发起人</span>：
                    </td>
                    <td>
                        <input id="name" name="name" value="${customEntity.name}" readonly>
                        <input id="iptInitUsreId" name="" type="hidden" value="${initUserId}"/>
                    </td>
                </tr>

                <tr>
                    <td colspan='4' align='center' class='f_td'>
                        	事由
                    </td>
                </tr>
                <tr>
                    <td colspan='4' style='height:100px;text-align:left;vertical-align:top;padding:3px 3px 3px 3px;'>
                        <pre style="white-space: pre-wrap;word-wrap: break-word;background:none;border:none;">${customEntity.applyContent}</pre>
                    </td>
                </tr>
                 <c:if test="${customEntity.formType == '2'}">
	            	<tr>
	            		<td>
	                    &emsp;同行人：
		                </td>
		                <td colspan='3' style="text-align:left;">
		                   ${customEntity.peerName}
		                </td>
	            	</tr>
	               <tr>
	            		<td>
	                    &emsp;目的地：
		                </td>
		                <td colspan='3' style="text-align:left;">
		                   ${customEntity.destination}
		                </td>
	            	</tr>
	            </c:if>
 	            <tr>
	            	<td>
	            		&emsp;&emsp;时间：
	            	</td>
	            	<td colspan='2' align='center'>
	            		自&emsp;${customEntity.startTime}&emsp;至&emsp;${customEntity.endTime}
	            	</td>
	            	<td>
	            		<c:if test="${customEntity.formType == '4' }">
					<c:if test="${customEntity.type != '4' }">
						<span>共 &emsp;${customEntity.totalTime}&emsp;时</span>
					</c:if>
					</c:if>
            		<c:if test="${customEntity.formType != '4' }">
						<span>共 &emsp;${customEntity.totalTime}&emsp;时</span>
					</c:if>
	            	
	            	</td>
	            </tr>
                <tr>
                    <td colspan='4' style='height:100px;vertical-align:top;padding:3px 3px 3px 3px;'>
                    	<span>审核人</span><br/>
                    	<input id="iptOldSelectIds" type="hidden" value="${auditorSelectIds}"/>
				  		<ul id="ulapprover" style="width:96%;margin:0 auto;list-style:none;">
				  			${approver}
			  			</ul>
                        
                    </td>
                </tr>
                <tr>
                    <td colspan='4' align='center' class='f_td'>
                        <span style="color:Red">*</span>批示内容
                    </td>
                </tr>
                <tr>
                    <td colspan='4' style='height:80px'>
                        <textarea maxlength="300" name="comment" id="comment" rows="2" cols="20"
                                  class="text0" style="height:79px;width:100%;background:#eee;" onfocus="if(value=='同意'){value=''}"  onblur="if (value ==''){value='同意'}">同意</textarea>
                    </td>
                </tr>
                
                
                
               
                
                
                <!-- 添加，删除，替换标识 -->
                <input id="modifyInput" type="hidden" name="type" value="0">
                <tr>
                	<td>下一步审批人</td>
                    <td colspan='3'>
                           <div style="width:100%;">
                               <div class="input-group leaderPicker"  style="width:100%;">
                                   <input id="leaderId" name="leader" type="hidden"
                                          value="${auditorId}">
                                   <input type="text" id="leaderName" name="leaderName" class="form-control required"
                                          value="${auditorName}" minlength="2"
                                          maxlength="50" class="form-control" readOnly placeholder="点击后方图标即可选人,以最后一次操作为准">
                                   <c:if test="${isCanSelect == true}">
                                   	<div class="input-group-addon">
                                   		<!-- <i class="glyphicon glyphicon-user"></i> -->
                                   		
                                   		<i id='leaderDiv' onclick="modifyLeader(1)" value="1"><a href="#" style="cursor:pointer">添加</a></i>
                                    	<i id="deleDiv" onclick="modifyLeader(2)" value="2"><a href="#" style="cursor:pointer">删除</a></i>
                                    	<i id='leaderDiv' onclick="modifyLeader(3)" value="3"><a href="#" style="cursor:pointer">替换</a></i>
                                   	</div>
                                   </c:if>
                               </div>
                        </div>
                    </td>
                </tr>
               <%--  <tr>
	            	<td style="">历史附件：</td>
	            	<td colspan="3" style="text-align:left;padding:10px;"><%@include file="/common/show_file.jsp" %></td>
	            </tr> --%>
	            <tr>
	            	<td><code>*</code>操作密码：</td>
	            	<td  colspan="3">
	            		<input name="txtPrivateKey" type="password" style="background:#eee;" maxlength="25" id="txtPrivateKey" style="float: left;"
                           />
                    	<input id="isPwdRight" name="isPwdRight" type="hidden"/>
	            	</td>
	            </tr>
            </table>
            
            <table width="90%" cellspacing="0" cellpadding="0" border="0" align="center" class="table table-border">
        <thead>
        <tr>
            <th>环节</th>
            <th>操作人</th>
            <th>时间</th>
            <th>结果</th>
            <th>审核时长</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach var="item" items="${logHumanTaskDtos}">
            <c:if test="${not empty item.completeTime}">
               <tr>
					  <td>${item.name}</td>
					  <td><tags:user userId="${item.assignee}"/></td>
					  <td><fmt:formatDate value="${item.completeTime}" type="both"/></td>
					  <td>${item.action}</td>
					  <td>${item.auditDuration}</td>
					</tr>
					<%-- <c:if test="${item.action != '发起自定义申请' && item.action != '重新发起申请'}"> --%>
						<tr style="border-top:0px hidden;">
							<td>批示内容</td>
							<td colspan="4">${item.comment}</td>
						</tr>
					<%-- </c:if> --%>
					 </c:if>
					  </c:forEach>
    </table>
        </section>
        <!-- end of main -->
        <%-- <div style="clear:both;"></div>
        <table>
            <tr>
                <div class="form-group">
                    <label class="control-label col-md-2" name="fileName">历史附件：</label>
                    <div class="col-md-8">
                        <%@include file="/common/show_file.jsp" %>
                    </div>
                </div>
            </tr>
        </table> --%>
       <!--  <table class="col-md-10" style="margin: 0 15px;">
            <tr>
                <td style="width:100px"><code>*</code>操作密码：</td>
                <td>
                    
                </td>
            </tr>
        </table> -->

    </div>
    
    </div>
    
    
    <br/>
    <br/>


    <div class="navbar navbar-default navbar-fixed-bottom">
        <div class="text-center" style="padding-top:8px;">
            <div id="divButton" class="text-center" style="padding-top:8px;">
                <button id="completeTask1" type="button" class="btn btn-default" onclick="fnCustomSubmit(1)">同意
                </button>
                <button id="completeTask2" type="button" class="btn btn-default" onclick="fnCustomSubmit(2)">驳回</button>
                <button id="completeTask3" type="button" class="btn btn-default" onclick="fnCustomSubmit(0)">不同意</button>
                <button type="button" class="btn btn-default" onclick="javascript:history.back();">返回</button>
                <!-- <button id="completeTask1" type="button" class="btn btn-default" onclick="confirm(5)">同意并结束流程</button> -->
            </div>
        </div>
    </div>


    </div>


</form>
</body>


</html>