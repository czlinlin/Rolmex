<%@page contentType="text/html;charset=UTF-8"%>
<%@include file="/taglibs.jsp"%>
<%pageContext.setAttribute("currentHeader", "bpm-workspace");%>
<%pageContext.setAttribute("currentMenu", "bpm-process");%>
<!doctype html>
<html lang="en">
<head>
<%@include file="/common/meta.jsp"%>

    <title><spring:message code="demo.demo.input.title" text="麦联"/></title>
    <%@include file="/common/s3.jsp"%>
	<!-- bootbox -->
    <script type="text/javascript" src="${cdnPrefix}/bootbox/bootbox.min1.js"></script>
	<link href="${cdnPrefix}/xform3/styles/xform.css" rel="stylesheet">
    <script type="text/javascript" src="${cdnPrefix}/xform3/xform-packed.js"></script>

    <link type="text/css" rel="stylesheet" href="${cdnPrefix}/userpicker3-v2/userpicker.css">
    <script type="text/javascript" src="${cdnPrefix}/userpicker3-v2/userpicker.js"></script>
	<script type="text/javascript" src="${cdnPrefix}/operation/TaskOperation.js"></script>
	
	<style type="text/css">
		.xf-handler {
			cursor: auto;
		}
	</style>
	
	<script type="text/javascript">
		document.onmousedown = function(e) {};
		document.onmousemove = function(e) {};
		document.onmouseup = function(e) {};
		document.ondblclick = function(e) {};

		var xform;
		
		 //调用接口，根据经销商编号，获取直销oa上存的对应信息：姓名 电话 等
		$(function() {
		 	var id=${processInstanceId}; 
				if (id !="") {
                    $.getJSON('${tenantPrefix}/rs/operationApply/getApplyInfo', {
                    	id: id
                    }, function(data) {
                    	for (var i = 0; i < data.length; i++) {
                    		//alert(data[i].createTime); 
                    		$("#userName").val(data[i].userName);  
                    		$("#welfare").val(data[i].welfare);
                    		$("#level").val(data[i].level);  
                    		$("#varFather").val(data[i].varFather);  
                    		$("#varRe").val(data[i].varRe);  
                    		$("#addTime").val(data[i].addTime);  
                    		$("#mobile").val(data[i].mobile);  
                    		$("#address").val(data[i].address);  
                    		$("#ucode").val(data[i].ucode);  
                    		$("#busType").val(data[i].businessType);
                    		$("#busDetailName").val(data[i].businessDetail);  
                    		$("#busLevel").val(data[i].businessLevel);
                    		
                    		$("#div_busType").html(data[i].businessType);
                    		$("#div_busDetailName").html(data[i].businessDetail);  
                    		$("#div_busLevel").html(data[i].businessLevel); 
                    		
                    		$("#applyContent").val(data[i].applyContent); 
                    		$("#area").val(data[i].area);
                    		
                    		$("#div_area").html(data[i].area);
                    		
                    		var selectValue="";
                    		var optionArray=$("#system option");
                    		for(var m=0;m<optionArray.length;m++){
	                   			if(data[i].system==$(optionArray[m]).html()){
	                   				selectValue=$(optionArray[m]).attr("value");
	                   			}
                    		}
                    		$("#system").val(selectValue);
                    		$("#systemID").val(selectValue);
                    		
                    		$("#systemName").val(data[i].system); 
                    		$("#standard").html(data[i].businessStand1);
                    		$("#standard2").html(data[i].businessStand2);
                    		$("#treeInfo").html(data[i].treeInfo);
                    		$("#businessStand1").val(data[i].businessStand1);
                    		$("#businessStand2").val(data[i].businessStand2);
                    		$("#treeInfos").val(data[i].treeInfo);
                    		$("#createTime").val(data[i].createTime);
                    		$("#applyID").val(data[i].id);
                    		$("#submitTimes").val(data[i].submitTimes);
                    		$("#applyCode").val(data[i].applyCode);
                    		
                    		$("#div_submitTimes").html(data[i].submitTimes);
                    		$("#div_applyCode").html(data[i].applyCode);
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
		
			setTimeout(function() {
				$('.datepicker').datepicker({
					autoclose: true,
					language: 'zh_CN',
					format: 'yyyy-mm-dd'
				})
			}, 500);
			//根据条件注掉其中一个撤销申请按钮
	         $.ajax({      
		            url: '${tenantPrefix}/rs/bpm/removeButton',      
		            datatype: "json",
		            data:{"processInstanceId": $("#processInstanceId").val()},
		            type: 'get',      
		            success: function (e) {
		            	//alert(JSON.stringify(e));
		            	if(e == "normalReject"){
							  $("#endProcess").css('display',"none");
						}else{
							  $("#completeTask2").css('display',"none");
						}
		            },      
		            error: function(e){      
		            	loading.modal('hide');
		                alert("服务器请求失败,请重试");  
		            }
		       });
		})
		
		ROOT_URL = '${tenantPrefix}';
		taskOperation = new TaskOperation();
		
		function applyCompleteTask(flag) {
			//调整人重新申请和撤销申请先检验该流程的状态是否是已撤回或驳回发起人
            $.ajax({      
	            url: '${tenantPrefix}/rs/bpm/getStatus',      
	            datatype: "json",
	            data:{"processInstanceId": $("#processInstanceId").val(),"humanTaskId":$("#humanTaskId").val(),"userId":$("#userId").val(),"resource":'adjustment'},
	            type: 'get',      
	            success: function (e) {
	            	if(e == 'error'){
	            		alert("该申请状态已变更，您已无权操作。");
	            		return false;
	            	}
	            	if(e == 'noAuth'){
	            		alert("您无权操作。");
	            		return false;
	            	}
	            	var loading = bootbox.dialog({
	                    message: '<p style="width:90%;margin:0 auto;text-align:center;">提交中...</p>',
	                    size: 'small',
	                    closeButton: false
	             	});
	    			
	    			$('#xform').attr('action', '${tenantPrefix}/operationApply/apply-completeTask.do?flag=' + flag);
	    			$('#xform').submit();
	            },      
	            error: function(e){      
	            	loading.modal('hide');
	                alert("服务器请求失败,请重试"); 
	            }
	       });
		}
		
		function MaxWords(obj){
	    	 document.getElementById("recordNum").style.display = "block";
           var text1 = document.getElementById("applyContent").value;
           var len;//记录已输入字数
           if (text1.length >= 5000) {
               document.getElementById("applyContent").value = text1.substr(0, 5000);
               len = 5000;
           }
           else {
               len = text1.length;
           }
           var show = len + " / 5000";
           document.getElementById("recordNum").innerText = show;
	     }

		function getSystemName() {
			var  myselect=document.getElementById("system");
			var index=myselect.selectedIndex ;  
			var bt=myselect.options[index].value;
			var text=myselect.options[index].text;
			$("#systemName").val(text);
			$("#systemID").val(bt);
		 }
		
	    //流程撤回后点击撤销申请，确认框提示
		function confirmOperation(){
			var msg = "确定要撤销申请吗,请确认？";  
            if (!confirm(msg)){  
                return false;  
            }
          //调整人撤销申请先检验该流程的状态是否是已撤回或驳回发起人
            $.ajax({      
	            url: '${tenantPrefix}/rs/bpm/getStatus',      
	            datatype: "json",
	            data:{"processInstanceId": $("#processInstanceId").val(),"humanTaskId":$("#humanTaskId").val(),"userId":$("#userId").val(),"resource":'adjustment'},
	            type: 'get',      
	            success: function (e) {
	            	if(e == 'error'){
	            		alert("该申请状态已变更，您已无权操作。");
	            		return false;
	            	}
	            	if(e == 'noAuth'){
	            		alert("您无权操作。");
	            		return false;
	            	}
	            	var loading = bootbox.dialog({
	                    message: '<p style="width:90%;margin:0 auto;text-align:center;">提交中...</p>',
	                    size: 'small',
	                    closeButton: false
	             	});
	            	$('#xform').attr('action', '${tenantPrefix}/bpm/workspace-endProcessInstance.do?processInstanceId=${processInstanceId}&humanTaskId=${humanTaskId}');
	    			$('#xform').submit();
	            },      
	            error: function(e){      
	            	loading.modal('hide');
	                alert("服务器请求失败,请重试"); 
	            }
	       });
		}
		
	</script>
    
</head>
    <style type="text/css">
    	#tb1{text-align:center; margin:10px auto;width:100%;table-collapse:collapse;}
        #tb1 td{border:1px solid #BBB; height:36px;line-height:36px;}
        #tb1 td,.f_td{font-size:14px;white-space:nowrap;}
        .f_r_td{text-align:left;}
        #tb1 tr td input[type='text']{background:#eee;width:100%;height:38px;padding-left:15px;border:none;}
        #tb1 tr td div{padding-left:15px;} 
        #tb1 tr td textarea{border: navajowhite;}     
        #tb1 tr td{text-align:center;}
        #tb1 .btitle{font-size:24px;padding:20px 0;}
        
        select.form-control{border:0;width:100%;}
        textarea{background:#eee;width:100%;}
        .xf-table td{padding:5px 0;}
        
        #fileTable{width:100%;}
        #fileTable td{border:none;}
        #tb1 tr td.td_uploadFile div{padding:0;}        
    </style>
<body>
 <%@include file="/header/bpm-workspace3.jsp"%>
    <form id="xform" method="post"    class="xf-form" enctype="multipart/form-data">
			常规/非常规业务	
    <br />
<div class="container">
    <section id="m-main" class="col-md-12" style="padding-top:65px;">
    <table width="100%" cellspacing="0" cellpadding="0" border="0" align="center" class="xf-table">
			  <tbody>
			    <tr>
				  <td width="25%" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
				    <label style="display:block;text-align:center;margin-bottom:0px;padding-top:10px;padding-bottom:10px;">审核环节&nbsp;</label>
				  </td>
				  <td width="75%" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top" colspan="3" rowspan="1">
				    <div id="nextStep"></div>
				  </td>
				</tr>
			  </tbody>
	</table>
	<script>
				  var processInstanceId =  "${processInstanceId}";
				  $.ajax({
	    				url:"${tenantPrefix}/dict/getProcessPostInfoByProcessInstanceId.do",
	    				data:{processInstanceId:processInstanceId},
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

	</script>
		<input id="processInstanceId" type="hidden" name="processInstanceId" value="${processInstanceId}">
		<input id="humanTaskId" type="hidden"  name="humanTaskId" value="${humanTaskId}">
   		<input id="businessStand1"  type="hidden"  name="businessStand1" >
    	<input id="businessStand2"  type="hidden"  name="businessStand2" >
    	<input id="treeInfos" type="hidden"   name="treeInfos" >
    	<input id="createTime"  type="hidden"  name="createTime" >
    	<input id="applyID"  type="hidden"  name="applyID" >
    	<input id="userId" type="hidden" name="userId" value="<%=userId %>">
    	<table id="tb1" >
    		<tr>
                <td colspan='8' align='center' class='f_td'>
                    <h2>调整申请(常规/非常规业务)</h2>
     			</td>
            </tr>
             <tr>
                <td colspan='2'  class='f_td' align='right' style='padding-right:20px;'>
                    	提交次数：
                </td>
                <td colspan='2' >
                	<div id="div_submitTimes"></div>
               		<input id="submitTimes" name="submitTimes" readonly style="display:none;">
                 </td>
                 <td colspan='2'  class='f_td' align='right' style='padding-right:20px;'>
                    	受理单编号：
                 </td>
                <td colspan='2' >
                	<div id="div_applyCode"></div>
               		<input id="applyCode" name="applyCode" readonly style="display:none;">
                 </td>
            </tr>
            
            <tr>
                <td>
                    <span id='userID'>&nbsp;<span style="color:Red">*</span>经销商编号</span>：
                </td>
                <td>
                    <input id ="ucode" name="ucode" type="text" maxlength="8"  readonly />
                </td>
                <td>
                    <span id='realName'>&nbsp;经销商姓名</span>：
                </td>
                <td>
                	<input name="userName" id="userName" type="text" readonly> 
                </td>
               <td >
                    <span id='wf'>&nbsp;福利级别</span>：
                </td>
                <td>
                    <input name="welfare" id="welfare" type="text" readonly> 
                </td>
                <td >
                    <span id='tag_level'>&nbsp;级别</span>：
                </td>
                <td >
                    <input name="level" id="level" type="text" readonly> 
                </td>
             </tr>
              <tr>
                <td >
                    <span id='tag_system'>&nbsp;<span style="color:Red">*</span>所属体系</span>：
                </td>
                 <td style="text-align:left;">
                    <select name="system" id="system" onchange="getSystemName()" class="form-control">
						<c:forEach items="${systemlist}" var="item">
		  					<option value="${item.value}" >${item.name}</option>
		  				</c:forEach>			
					</select>
					<input id="systemName" name="systemName" type="hidden"  >
					<input id="systemID" name="systemID" type="hidden">
                </td>
                <td>
                    <span id='seller'>&nbsp;销售人</span>：
                </td>
                <td>
                    <input name="varFather" id="varFather" type="text" readonly>
                </td>
                <td>
                    <span id='tag_service'>&nbsp;服务人</span>：  
                </td>
                <td>
                   <input name="varRe" id="varRe" type="text" readonly>
                </td>
                <td>
                    <span id='tag_addTime'>&nbsp;注册时间</span>：
                </td>
                <td>
                    <input name="addTime" id="addTime" type="text" readonly> 
                </td>
            </tr>
            <tr>
                <td>
                    <span id='Span1'>&nbsp;联系电话</span>：
                </td>
                <td colspan='3'>
                     <input name="mobile" id="mobile" type="text" readonly> 
                </td>   
                <td>
                    <span id='Span2'>&nbsp;联系地址</span>：
                </td>
                <td colspan='3'>
                    <input name="address" id="address" type="text" readonly> 
                 </td>
            </tr>
            <tr>
                <td style="white-space:nowrap">
                    <span id='tag_bustype'>&nbsp;申请业务类型</span>：
                </td>
                <td colspan='3' style="text-align:left;">
                	<div id="div_busType"></div>
                    <input name="busType" id="busType" type="text" readonly style="display:none;">
				 </td>
                <td class='f_td'>
                    <span id='tag_busDetails'>&nbsp;业务细分</span>：
                </td>
                <td colspan='3' style="text-align:left;">
                    <div id="div_busDetailName"></div>
                    <input id="busDetailName" name="busDetailName" readonly style="display:none;">    
                </td>
            </tr>
            <tr>
                <td>
                    <span id='tag_sLevel'>&nbsp;业务级别</span>：
                </td>
                <td colspan='3' style="text-align:left;">
                	<div id="div_busLevel"></div>
                    <input name="busLevel" id="busLevel" type="text" readonly style="display:none;">
				 </td>   
                <td>
                    <span id='tag_belongs'>&nbsp;所属大区：</span>
                </td>
                <td colspan='3' style="text-align:left;">
                	<div id="div_area"></div>
                    <input name="area" id="area" readonly style="display:none;">
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
                    <span style="color:Red">*</span>申请内容
                </td>
            </tr>
            <tr>
                <td colspan='8' style='height:100px'  style="text-align:left;">
                    <textarea maxlength="5000"   name="applyContent" id="applyContent" rows="2" cols="20" class="text0" style="height:99px;padding-left:10px;padding-top:10px"  onkeyup="MaxWords(this)" onblur="MaxWords(this)"></textarea>
                    <label id="recordNum" style="display: none;text-align:right;" class="control-label col-md-12"></label>
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
                	<p style="white-space:pre-wrap;text-align:left; padding:5px 30px;"><span id="standard2" ></span></p>
                </td>
                <td colspan='2'  style='height:auto; width:200px;white-space:normal;' class='leftAlign'>
                   	<p style="white-space:pre-wrap;text-align:left; padding:5px 30px;"><span id="treeInfo" ></span></p>
                </td>
            </tr>
            <tr>
            	<td>原有附件：</td>
            	<td colspan="7" style="text-align:left;padding-left:15px;">
            		<%@include file="/common/show_edit_file.jsp" %>
            	</td>
            </tr>
            <tr>
            	<td>添加附件：</td>
            	<td class="td_uploadFile" colspan="7" style="text-align:left;padding-left:15px;">
            		<%@include file="/common/_uploadFile.jsp" %>
                    <span style="color:gray;"> 请添加共小于200M的附件 </span>
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
					<c:if test="${item.action != '提交' && item.action != '重新申请'}">
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
	<!-- end of main -->
</div> 
	 
	  <br/>  
	<br/>  
	 <div class="navbar navbar-default navbar-fixed-bottom">
	    	<div class="text-center" style="padding-top:8px;">
			    <div class="text-center" style="padding-top:8px;">
					<button id="completeTask1" type="button" class="btn btn-default" onclick="applyCompleteTask(3)">重新申请</button>
					<button id="completeTask2" type="button" class="btn btn-default" onclick="applyCompleteTask(4)">撤销申请</button>
					<button id="endProcess" type="button" class="btn btn-default" onclick="confirmOperation()">撤销申请</button>
					<button type="button" class="btn btn-default" onclick="javascript:history.back();">返回</button>
				</div>
			</div>
	   </div>	       
</form>
</body>
</html>