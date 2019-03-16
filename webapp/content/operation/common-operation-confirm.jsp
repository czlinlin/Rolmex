<%@page contentType="text/html;charset=UTF-8" %>
<%@include file="/taglibs.jsp" %>
<%pageContext.setAttribute("currentHeader", "bpm-workspace");%>
<%pageContext.setAttribute("currentMenu", "bpm-process");%>
<!doctype html>
<html lang="en">
<head>
    <%@include file="/common/meta.jsp" %>

    <title><spring:message code="demo.demo.input.title" text="麦联"/></title>
    <%@include file="/common/s3.jsp" %>
    <!-- bootbox -->
    <script type="text/javascript" src="${cdnPrefix}/bootbox/bootbox.min1.js"></script>
    <link href="${cdnPrefix}/xform3/styles/xform.css" rel="stylesheet">
    <script type="text/javascript" src="${cdnPrefix}/xform3/xform-packed.js"></script>

    <link type="text/css" rel="stylesheet" href="${cdnPrefix}/userpicker3-v2/userpicker.css">
    <script type="text/javascript" src="${cdnPrefix}/userpicker3-v2/userpicker.js"></script>
    <script type="text/javascript" src="${cdnPrefix}/operation/TaskOperation.js"></script>
    <script type="text/javascript" src="${cdnPrefix}/operation/operation.js?v=1.20"></script>

    <style type="text/css">
        .xf-handler {
            cursor: auto;
        }
    </style>

    <script type="text/javascript">
        document.onmousedown = function (e) {
        };
        document.onmousemove = function (e) {
        };
        document.onmouseup = function (e) {
        };
        document.ondblclick = function (e) {
        };

        var xform;

        //调用接口，根据经销商编号，获取直销oa上存的对应信息：姓名 电话 等
        $(function () {
            var id =${processInstanceId};
            if (id != "") {
                $.getJSON('${tenantPrefix}/rs/operationApply/getApplyInfo', {
                    id: id
                }, function (data) {
                    for (var i = 0; i < data.length; i++) {
                        //alert(data[i].fileName);
                        $("#userName").html(data[i].userName);
                        $("#welfare").html(data[i].welfare);
                        $("#level").html(data[i].level);
                        $("#varFather").html(data[i].varFather);
                        $("#varRe").html(data[i].varRe);
                        $("#addTime").html(data[i].addTime);
                        $("#mobile").html(data[i].mobile);
                        $("#address").val(data[i].address);
                        $("#ucode").html(data[i].ucode);
                        $("#busType").val(data[i].businessType);
                        $("#busDetails").val(data[i].businessDetail);
                        $("#busLevel").val(data[i].businessLevel);
                        $("#span_content").html(data[i].applyContent);
                        $("#area").val(data[i].area);
                        $("#system").html(data[i].system);
                        $("#standard").html(data[i].businessStand1);
                        $("#standard2").html(data[i].businessStand2);
                        $("#treeInfo").html(data[i].treeInfo);
                        $("#submitTimes").val(data[i].submitTimes);
                        $("#applyCode").val(data[i].applyCode);

                    }
                });
                
              //获取抄送人
                $.getJSON('${tenantPrefix}/workOperationCustom/getFormCopyName.do', {
                	id: id
                }, function(data) {
                	if(data != ''){
                		$("#copyNames").html(data);
                	}
                });
            };
            setTimeout(function () {
                $('.datepicker').datepicker({
                    autoclose: true,
                    language: 'zh_CN',
                    format: 'yyyy-mm-dd'
                })
            }, 500);
             //审核环节
            $.ajax({
				url:"${tenantPrefix}/dict/getProcessPostInfoByProcessInstanceId.do",
				data:{processInstanceId:id},
				dataType:"json",
				type:"post",
				success:function(data){
					//console.log(data);
					$('#nextStep').append(data.whole);
				},
				error:function(){
					alert("获取流程审核人岗位信息出错！");
				}
			});
        })

        ROOT_URL = '${tenantPrefix}';
        taskOperation = new TaskOperation();

        function applyCompleteTask(flag) {

            //若要驳回或不同意，必须填写意见才能提交
            if ((flag == 2 || flag == 0) &&(( document.getElementById('comment').value == "") || ( document.getElementById('comment').value == "同意")) ){
                alert("请填写批示内容！");
                return false;
            }
           //每次审核人审核时都先检验该流程的状态是否是已撤回
            $.ajax({      
	            url: '${tenantPrefix}/rs/bpm/getStatus',      
	            datatype: "json",
	            data:{"processInstanceId": $("#processInstanceId").val(),"humanTaskId":$("#humanTaskId").val(),"userId":$("#userId").val()},
	            type: 'get',      
	            success: function (e) {
	            	if(e == 'error'){
	            		alert("该申请已撤回，暂无法审核。");
	            		return false;
	            	}
	            	if(e == 'noAuth'){
	            		alert("您无权审核。");
	            		return false;
	            	}
	            	var conf={
                       "formId":"xform",
                       "checkUrl":"${tenantPrefix}/rs/customer/opteraion-verifyPassword",
                       "actionUrl": '${tenantPrefix}/operationApply/apply-completeTask.do?flag=' + flag,
                       "iptPwdId":"txtPrivateKey"
                    }

                    operationSubmit(conf);
	            },      
	            error: function(e){      
	            	loading.modal('hide');
	                alert("服务器请求失败,请重试");  
	            }
	       });
            
        }

        function MaxWords() {

            if ($("#content").val().length == "4000") {
                alert("申请内容字数已达上限4000字");
            }
        }
    </script>
</head>
<style type="text/css">
	#tb1{text-align:center; margin:10px auto;width:100%;border-collapse:collapse;}
    #tb1 td {
        border: 1px solid #BBB; text-align:center; margin:10px auto;width:100%;
        line-height:33px;height:33px;font-size:14px;
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
        text-align: center;
    }

    #tb1 tr td textarea {
        border: navajowhite;
    }

    #tb1 tr td {
        text-align: center;
    }
</style>
<body>
<%@include file="/header/bpm-workspace3.jsp" %>
<form id="xform" method="post" class="xf-form" enctype="multipart/form-data">

    <div class="container">
        <section id="m-main" class="col-md-12" style="padding-top:65px;">
            <input id="filePath" name="filePath" type="hidden">
            <input id="processInstanceId" type="hidden" name="processInstanceId"
                   value="${processInstanceId}">
            <input id="humanTaskId" type="hidden" name="humanTaskId" value="${humanTaskId}">
            <input id="userId" type="hidden" name="userId" value="<%=userId %>">

                <table width="100%" cellspacing="0" cellpadding="0" border="0" align="center" class="xf-table">
                  <tbody>
                    <tr>
                      <td width="25%" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
                        <label style="display:block;text-align:center;margin-bottom:0px;padding-top:10px;padding-bottom:10px;">审核环节</label>
                      </td>
                      <td width="75%" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top" colspan="3" rowspan="1">
                        <div id="nextStep"></div>
                      </td>
                    </tr>
                  </tbody>
                </table>
                
            <table id="tb1"  cellspacing="0" cellpadding="0" border="0">
                <tr>
                    <td colspan='8' align='center' class='f_td'>
                        <h2>业务受理申请单审批</h2>
                    </td>
                </tr>
                <tr>
                    <td colspan='2' class='f_td' align='right' style='padding-right:20px;'>
                        提交次数：
                    </td>
                    <td colspan='2'>
                        <input id="submitTimes" name="submitTimes" readonly>
                    </td>
                    <td colspan='2' class='f_td' align='right' style='padding-right:20px;'>
                        受理单编号：
                    </td>
                    <td colspan='2'>
                        <input id="applyCode" name="applyCode" readonly>
                    </td>
                </tr>

                <tr>
                    <td>
                        <span id='userID'>&nbsp;经销商编号</span>：
                    </td>
                    <td>
                    	<div id="ucode" name="ucode" ></div>
					<!-- 	<input id="ucode" name="ucode" type="text" maxlength="8" readonly/> -->
                    </td>
                    <td>
                        <span id='realName'>&nbsp;经销商姓名</span>：
                    </td>
                    <td>
                    	<div id="userName" name="userName" ></div>
<!--                         <input name="userName" id="userName" readonly> -->
                    </td>
                    <td>
                        <span id='wf'>&nbsp;福利级别</span>：
                    </td>
                    <td>
                    	<div id="welfare" name="welfare" ></div>
<!--                         <input name="welfare" id="welfare" readonly> -->
                    </td>
                    <td>
                        <span id='tag_level'>&nbsp;级别</span>：
                    </td>
                    <td>
                    	<div id="level" name="level" ></div>
<!--                         <input name="level" id="level" readonly> -->
                    </td>
                </tr>
                <tr>
                    <td>
                        <span id='tag_system'>&nbsp;所属体系</span>：
                    </td>
                    <td>
                    	<div id="system" name="system" ></div>
<!--                         <input name="system" id="system" readonly> -->
                    </td>
                    <td>
                        <span id='seller'>&nbsp;销售人</span>：
                    </td>
                    <td>
                    	<div id="varFather"></div>
                        <!-- <input name="varFather" id="varFather" readonly> -->
                    </td>
                    <td>
                        <span id='tag_service'>&nbsp;服务人</span>：
                    </td>
                    <td>
                    	<div id="varRe"></div>
						<!--  <input name="varRe" id="varRe" readonly> -->
                    </td>
                    <td>
                        <span id='tag_addTime'>&nbsp;注册时间</span>：
                    </td>
                    <td>
                    	<div id="addTime"></div>
					<!--	<input name="addTime" id="addTime" readonly> -->
                    </td>
                </tr>

                <tr>
                    <td style="white-space:nowrap">
                        <span id='tag_bustype'>&nbsp;申请业务类型</span>：
                    </td>
                    <td  colspan='3'>
                        <input name="busType" id="busType" readonly>
                    </td>
                    <td class='f_td'>
                        <span id='tag_busDetails'>&nbsp;业务细分</span>：
                    </td>
                    <td colspan='3'>
                        <input id="busDetails" readonly>
                    </td>
                </tr>
                <tr>
                    <td>
                        <span id='Span1'>&nbsp;联系电话</span>：
                    </td>
                    <td  colspan='3'>
                    	<div name="mobile" id="mobile" ></div>
<!--                         <input name="mobile" id="mobile" readonly> -->
                    </td>
                    <td>
                        <span id='Span2'>&nbsp;联系地址</span>：
                    </td>
                    <td colspan='3'>
                        <input name="address" id="address" readonly>
                    </td>
                </tr>
                <tr>
                    <td>
                        <span id='tag_sLevel'>&nbsp;业务级别</span>：
                    </td>
                    <td  colspan='3'>
                        <input name="busLevel" id="busLevel" readonly>
                    </td>
                    <td width='100px'>
                        <span id='tag_belongs'>&nbsp;所属大区：</span>
                    </td>
                    <td colspan='3'>
                        <input name="area" id="area" readonly>
                    </td>
                </tr>
                <tr>
	            	<td  class='f_td'>抄送：</td>
	                <td id="copyNames" colspan='7' style="text-align:left;" class='f_td'>
	                    	<%-- ${taskCopyNames} --%>
	                </td>
	            </tr>
	            ${detailHtml}
                <tr>
                    <td colspan='8' align='center' class='f_td'>
                       	申请内容
                    </td>
                </tr>
                <tr>
                <td colspan='8' >
                	<p style="white-space:pre-wrap;text-align:left; padding:5px 30px;"><span id="span_content"></span></p>
                </td>
            	</tr>

                <tr>
                    <td colspan='3' align='center' class='f_td'>
                        业务标准(现场办理)
                    </td>
                    <td colspan='3' align='center' class='f_td'>
                        业务标准(非现场办理)
                    </td>
                    <td colspan='2' align='center' class='f_td'>
                        点位信息
                    </td>
                </tr>
                <tr>
                    <td colspan='3' style='height:auto; width:300px; white-space:normal; ' class='leftAlign'>
                        <p style="white-space:pre-wrap;text-align:left; padding:5px 30px;"><span id="standard" name="standard"></span></p>
                    </td>
                    <td colspan='3' style='height:auto; width:300px; white-space:normal; ' class='leftAlign'>
                        <p style="white-space:pre-wrap;text-align:left; padding:5px 30px;"><span id="standard2"></span></p>
                    </td>
                    <td colspan='2' style='height:auto; width:200px;white-space:normal;' class='leftAlign'>
                        <p style="white-space:pre-wrap;text-align:left; padding:5px 30px;"><span id="treeInfo"></span></p>
                    </td>
                </tr>

                <tr>
                    <td colspan='8' align='center' class='f_td'>
                        批示内容
                    </td>
                </tr>
                <tr>
                <td colspan="8" style="height:80px; text-align:left;">
                    <textarea maxlength="300"  name="comment" id="comment" rows="2" cols="20" class="text0" style="height:79px;width:1100px" 
                    onfocus="if(value=='同意'){value=''}"  onblur="if (value ==''){value='同意'}">同意</textarea>
                </td>
            </tr>
            </table>

            <table>
                <tr>
                    <div class="form-group">
                        <label class="control-label col-md-2" name="fileName">历史附件：</label>
                        <div class="col-md-8">
                            <%@include file="/common/show_file.jsp" %>
                        </div>
                    </div>
                </tr>
            </table>
            <table class="col-md-10" style="margin: 0 15px;">
                <tr>
                    <td style="width:100px"><code>*</code>操作密码：</td>
                    <td>
                        <input name="txtPrivateKey" type="password" maxlength="25" id="txtPrivateKey"
                               style="float: left;" onblur='isPwd();'/>
                        <input id="isPwdRight" name="isPwdRight" type="hidden"/>
                    </td>
                </tr>
            </table>
            
            <table width="100%" cellspacing="0" cellpadding="0" border="0" align="center" class="table table-border">
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
					<c:if test="${item.action != '提交' && item.action != '重新调整申请'}">
						<tr style="border-top:0px hidden;">
							<td>批示内容</td>
							<td colspan="4">${item.comment}</td>
						</tr>
					</c:if>
					 </c:if>
					  </c:forEach>
				  </tbody>
				</table>
			</section>
    </div>

    
    

    <br/>
    <br/>


    <div class="navbar navbar-default navbar-fixed-bottom">
        <div class="text-center" style="padding-top:8px;">
            <div class="text-center" style="padding-top:8px;">
                <button id="completeTask1" type="button" class="btn btn-default" onclick="applyCompleteTask(1)">同意
                </button>
                <button id="completeTask2" type="button" class="btn btn-default" onclick="applyCompleteTask(2)">驳回
                </button>
                <button id="completeTask3" type="button" class="btn btn-default" onclick="applyCompleteTask(0)">不同意
                </button>
                <button type="button" class="btn btn-default" onclick="javascript:history.back();">返回</button>
            </div>
        </div>
    </div>

</form>

</body>
</html>

