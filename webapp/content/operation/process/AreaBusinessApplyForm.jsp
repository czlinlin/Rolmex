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
    <script type="text/javascript" src="${cdnPrefix}/bootbox/bootbox.min.js"></script>
	<link href="${cdnPrefix}/xform3/styles/xform.css" rel="stylesheet">
    <script type="text/javascript" src="${cdnPrefix}/xform3/xform-packed.js"></script>

    <link type="text/css" rel="stylesheet" href="${cdnPrefix}/userpicker3-v2/userpicker.css">
    <%-- <script type="text/javascript" src="${cdnPrefix}/userpicker3-v2/userpickerbybpm.js"></script> --%>
    <script type="text/javascript" src="${cdnPrefix}/userpicker3-v2/userpickercustom.js"></script>
	<script type="text/javascript" src="${cdnPrefix}/operation/TaskOperation.js"></script>
	
	<style type="text/css">
		.xf-handler {
			cursor: auto;
		}
		input[type="text"]{border : 1px solid #F2F2F2;height:25px}
		textarea{border : 1px solid #F2F2F2}
		select{border : 1px solid #F2F2F2}
	</style>

	<script type="text/javascript">
		document.onmousedown = function(e) {};
		document.onmousemove = function(e) {};
		document.onmouseup = function(e) {};
		document.ondblclick = function(e) {};

		
		var xform;

		$(function() {
			
			createUserPicker({
                modalId: 'ccUserPicker',
                targetId: 'ccDiv',
                multiple: true,
                showExpression: true,
                searchUrl: '${tenantPrefix}/rs/user/searchVNoMe',
                treeUrl: '${tenantPrefix}/party/asyncTree.do?partyStructTypeId=1&notViewPost=true&notAuth=false',
                childUrl: '${tenantPrefix}/rs/party/searchUserNoMe'
            })
		})
		
		ROOT_URL = '${tenantPrefix}';
		taskOperation = new TaskOperation();
		
		function startProcessInstance() {
			if($("#theme").val().replace(/(^\s*)|(\s*$)/g, "") == ""){
				alert("主题不能为空");
				return
			}
			if($("#busType").val() == "请选择" || $("#busType").val() == ""){
				alert("业务类型不能为空");
				return
			}
			if($("#busDetail").val() == "请选择" || $("#busDetail").val() == ""){
				alert("业务细分不能为空");
				return
			}
			
			if($("#applyContent").val().replace(/(^\s*)|(\s*$)/g, "") == ""){
				alert("请输入内容");
				return
			}
			if($("#theme").val().length > "100"){
				var msg = "主题已超出100字，继续提交系统会自动截取100字以内内容，返回修改请取消";  
	            if (!confirm(msg)){  
	                return false;  
	            }  
			}
			if($("#applyContent").val().length > '5000'){
				var msg = "申请内容已超出5000字，继续提交系统会自动截取4000字以内内容，返回修改请取消";  
	            if (!confirm(msg)){  
	                return false;  
	            }  
			}
			
			/* var text = CKEDITOR.instances.applyContent.document.getBody().getText();
			if($.trim(text) == ""){
				alert("请输入内容");
				return
			} */
			//判断受理单编号是否存在
			applyCodeIfExist();
			
		}
		//判断受理单编号是否已存在,若不存在，返回当前受理单号，若存在，生成一个新的受理单号
		function applyCodeIfExist() {

			var applyCode = document.getElementById('applyCode').value;
			
			$.ajax({      
	            url: '${tenantPrefix}/rs/business/applyCodeIfExist',      
	            datatype: "json",
	            data:{"applyCode": applyCode},
	            type: 'get',      
	            success: function (e) {
	            	//成功后回调   
	            		$("#applyCode").val(e);
	        			$('#xform').submit();
	            	     
	            },      
	            error: function(e){      
	            	//失败后回调      
	                alert("服务器请求失败,请重试");      
	            }/* ,      
	            beforeSend: function(){      
	            //发送请求前调用，可以放一些"正在加载"之类额话      
	                alert("正在加载");           
	    		} */
	       }); 
        }
	$(function(){
		//业务类型
		<%-- var userId =<%=request.getParameter("userId")%>;
		var url = document.getElementById('r').value;
    	$.getJSON('${tenantPrefix}/rs/business/post_types', {userId : userId,url:url}, function(data) {
    		var option = "<option value=''>请选择</option>" ;  
        	 //alert(JSON.stringify(data[i])); 
        	 for(var i = 0;i<data.length;i++){
        		 option += "<option value='"+ data[i].id+"'>"+ data[i].name+"</option>" 
	  			 $("#busType").html(option);//将循环拼接的字符串插入第二个下拉列表  
        	 }
            
  		}); --%>
  		getBusinessDetail();
	})
    	function getBusinessDetail() {
    		//根据用户选择的业务类型，到数据库中取业务细分
    		var  myselect=document.getElementById("busType");
    		var index=myselect.selectedIndex ;  
    		var bt=myselect.options[index].value;
    		var t=myselect.options[index].text;
    		document.getElementById("businessTypeId").value = bt;
    		var userId = $("#userId").val();
    		var url = document.getElementById('r').value;
    		$("#businessType").val(t);
    		// alert(JSON.stringify(myselect.options[index].value));
    		$.getJSON('${tenantPrefix}/rs/business/post_details', 
  	    			{ bt:bt,userId:userId,url:url},    
  	    			function(data) {
  	    		var option = "<option value=''>请选择</option>" ;  
  	        	for (var prop in data[0]) {
  	        		//alert(JSON.stringify(data[i])); 
  	                option += "<option value='"+ prop+"'>"+ data[0][prop]+"</option>"  
  	 			}
  	  			$("#busDetail").html(option);//将循环拼接的字符串插入第二个下拉列表  
  	    	});
    	}
    	
    	//根据用户选择的业务类型明细，到数据库中取业务级别
    	function setLevel() {
    		var  myselect=document.getElementById("busDetail");
    		var index=myselect.selectedIndex ;  
    		var bd=myselect.options[index].value;
    		var t=myselect.options[index].text;
    		$("#businessDetailId").val(bd);
    		$("#businessDetail").val(t);
    		$.getJSON('${tenantPrefix}/rs/business/level', { bd:bd}, 
    				function(data) {
	        		var option = "" ;  
	            	for (var i = 0; i < data.length; i++) {
                    option += "<option>"+ data[i].level+"</option>"  
                  }
      			 $("#busLevel").html(option);//将循环拼接的字符串插入第二个下拉列表  
      			var  myselect=document.getElementById("busLevel");
        		var index=myselect.selectedIndex ;  
        		var bd=myselect.options[index].value;
        		var t=myselect.options[index].text;
      			$("#businessLevel").val(t);
      		 });
    		
    		//实现不同业务细分挂不同流程：根据用户选择的业务类型和业务细分去oa_ba_business_detail取流程的ID
        	
    		var businessDetailId= $("#busDetail").val();
    		if(businessDetailId == ""){
    			$('#nextStep').html('');
    		}else{
    			$.getJSON('${tenantPrefix}/rs/detailPostService/BpmProcessIDByBusinessDetail', 
      	    			{ businessDetailID:businessDetailId},    
      	    			function(data) {
      	    				$("#bpmProcessId").val(data[0].bpmProcessId);
      	    				if($("#nextStep").html() != ''){
      		  					$('#nextStep').html('');
      		  				}
    		  	    		$('#nextStep').append(data[0].whole);
      	    	});
    		}
    }
	    	 
	    	/*==============================================================================================  */
	    	
		  	 //获取大区
	    	$.getJSON('${tenantPrefix}/rs/party/AreaName',{userId : <%=request.getParameter("userId")%>},function(data){
	    		var str = "";
	    		if(data[0] != null && data[0].name.substring(2) == "大区"){
	    			$("#areaId").val(data[0].id);
	    			$("#area").val(data[0].name);
	    		}
	    	})   
		     
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
    </script>
  </head>

  <body>
   <%@include file="/header/bpm-workspace3.jsp"%>
<form id="xform" method="post" action="${tenantPrefix}/processBusiness/process-operationBusiness-startProcessInstance.do" class="xf-form" enctype="multipart/form-data">
    <div class="container">

	  <!-- start of main -->
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
		
		<input id="processDefinitionId" type="hidden" name="processDefinitionId" value="${processDefinitionId}">
		<input id="userId" type="hidden" name="userId" value="${userId}">
		<input id="bpmProcessId" type="hidden" name="bpmProcessId" value="">
		<input id="autoCompleteFirstTask" type="hidden" name="autoCompleteFirstTask" value="false">
		<input id="businessKey" type="hidden" name="businessKey" value="${businessKey}">
		<input id="activityId" type="hidden" name="activityId" value="">
		<input id="areaId" type="hidden" name="areaId" value="">
		<input id="url" type="hidden" name="url" value="/processBusiness/areaForm-detail.do">
		<input id="r" type="hidden"  name="r" value="${url}">
		<input id="businessTypeId" type="hidden"  name="businessTypeId" value="">
		<input id="businessDetailId" type="hidden"  name="businessDetailId" value="">
		<div id="xf-form-table">
			<div id="xf-1" class="xf-section">
				<h1 style="text-align:center;">业务申请单（大区）</h1>
			</div>
			
			<div id="xf-2" class="xf-section">
				<table class="xf-table" width="100%" cellspacing="0" cellpadding="0" border="0" align="center">
					<tbody>
						<tr id="xf-2-0">
							<td id="xf-2-0-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" colspan="4" width="100%">
								<div class="xf-handler">
									<label style="display:block;text-align:right;margin-bottom:0px;">提交次数： <input style="border:0px;width:10px" readonly type="text" id="submitTimes" name="submitTimes" value="0">      &nbsp;&nbsp;申请单号:<input readonly type="text" id="applyCode" name="applyCode" style="border:0px" value="${code}"></label>
								</div>
							</td>
						</tr>
						<tr id="xf-2-1">
							<td id="xf-2-1-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left" width="50%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;"><font color='red'>*</font>&nbsp;主题：</label>
								</div>
							</td>
							<td id="xf-2-1-1" class="xf-cell xf-cell-right xf-cell-bottom" colspan="3" width="50%">
								<div class="xf-handler">
									<input id="theme" name="theme" type="text" style="width:100%;" maxlength="100">
								</div>
							</td>
						</tr>
						<tr id="xf-2-2">
							<td id="xf-2-2-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left" width="25%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">抄送：</label>
								</div>
							</td>
							<td id="xf-2-2-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top" colspan="3" width="75%">
								<div class="input-group userPicker">
	                                <input id="btnPickerMany" type="hidden" name="copyUserValue" class="input-medium"  value="">
	                                <input type="text" id="userName" name="cc" style="width: 800px;background-color:white"
                                     value="" class="form-control" readOnly placeholder="点击后方图标即可选人">
	                                <div id="ccDiv" class="input-group-addon"><i class="glyphicon glyphicon-user"></i></div>
                            	</div>
							</td>
							
						</tr>
						<tr id="xf-2-3">
							<td id="xf-2-3-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left" width="25%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;"><font color='red'>*</font>&nbsp;申请业务类型：</label>
								</div>
							</td>
							<td id="xf-2-3-1" class="xf-cell xf-cell-right xf-cell-bottom" width="25%">
								<div class="xf-handler">
									<select id="busType" onchange="getBusinessDetail()">
										<option value="-1">请选择</option>
										<c:forEach items="${businessTypeList}" var="item">
  											<option value="${item.id}" >${item.name}</option>
  										</c:forEach>
										<input type="hidden" id="businessType" name="businessType" >
									</select>
								</div>
							</td>
							<td id="xf-2-3-2" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-left" width="25%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;"><font color='red'>*</font>&nbsp;业务细分：</label>
								</div>
							</td>
							<td id="xf-2-3-3" class="xf-cell xf-cell-right xf-cell-bottom" width="25%">
								<div class="xf-handler">
									<select id="busDetail" name="busDetail" onchange="setLevel()">
										<option value="请选择">请选择</option>
										<input type="hidden" id="businessDetail" name="businessDetail" >
									</select>
								</div>
							</td>
						</tr>
						
						<tr id="xf-2-4">
							<td id="xf-2-4-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="25%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;"><font color='red'>*</font>&nbsp;业务级别：</label>
								</div>
							</td>
							<td id="xf-2-4-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="25%">
								<select id="busLevel">
									<option value="请选择">请选择</option>
									<input type="hidden" id="businessLevel" name="businessLevel" >
								</select>
							</td>
							<td id="xf-2-4-2" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="25%">
								<label style="display:block;text-align:center;margin-bottom:0px;">发起人：</label>
							</td>
							<td id="xf-2-4-3" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="25%">
								<div class="xf-handler">
									<input type="text" id="initiator" name="initiator"  style="width:100%" readonly value="<tags:user userId='<%=userId%>'/>"/>
								<%-- <tags:user userId='<%=userId%>'/>  --%>
								</div>
							</td>
						</tr>
						
						<tr id="xf-2-5">
							<td id="xf-2-5-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="25%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;"><font color='red'>*</font>&nbsp;大区：</label>
								</div>
							</td>
							<td id="xf-2-5-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="75%" colspan="3">
								<div class="xf-handler">
									<input type="text" id="area" name="area" style="width:100%" readonly>
								</div>
							</td>
						</tr>
						<tr id="xf-2-6">
							<td id="xf-2-6-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="25%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;"><font color='red'>*</font>&nbsp;申请内容</label>
								</div>
							</td>
							<td id="xf-2-6-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" colspan="3" width="75%">
								<div class="xf-handler">
									<textarea id="applyContent" name="applyContent" rows="10" style="width:100%" maxlength="5000" onkeyup="MaxWords(this)" onblur="MaxWords(this)"></textarea>
									<label id="recordNum" style="display: none;text-align:right;" class="control-label col-md-12"></label>
								</div>
							</td>
						</tr>
						<tr id="xf-2-7">
							<td id="xf-2-7-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" colspan="4" width="100%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">上传附件</label>
								</div>
							</td>
						</tr>
						<tr id="xf-8">
							<td id="xf-8-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" colspan="4">
		                        <div class="col-md-8">
		                            <%@include file="/common/_uploadFile.jsp"%>
		                            <span style="color:gray;"> 请添加小于200M的附件 </span>
		                        </div>
	                    	</td>
                    	</tr>
						
					</tbody>
				</table>
			</div>
		</div>
		<br>
    </section>
	<!-- end of main -->
  </div>
  <div class="navbar navbar-default navbar-fixed-bottom">
    <div class="container-fluid">
      <div class="text-center" style="padding-top:8px;">
	    <div class="text-center" style="padding-top:8px;">
			<button id="confirmStartProcess" class="btn btn-default" type="button" onclick="startProcessInstance()" value="提交">提交数据</button>
			<button type="button" class="btn btn-default" onclick="javascript:history.back();">返回</button>
		</div>
	  </div>
    </div>
  </div>
</form>
</body>
</html>
