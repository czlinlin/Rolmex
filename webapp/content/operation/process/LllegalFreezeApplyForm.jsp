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
	<script type="text/javascript" src="${cdnPrefix}/operation/TaskOperation.js"></script>
	<script type="text/javascript" src="${cdnPrefix}/userpicker3-v2/userpickercustom.js"></script>
	<script type="text/javascript" src="${cdnPrefix}/userpicker3-v2/userpickerbycopy.js?v=1.0"></script>
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
			/* createUserPicker({
                modalId: 'ccUserPicker',
                targetId: 'ccDiv',
                multiple: true,
                showExpression: true,
                searchUrl: '${tenantPrefix}/rs/user/searchVNoMe',
                treeUrl: '${tenantPrefix}/party/asyncTree.do?partyStructTypeId=1&notViewPost=true&notAuth=false',
                childUrl: '${tenantPrefix}/rs/party/searchUserNoMe'
            }) */
			createUserPickerCopy({
        		modalId: 'userPicker',
        		showExpression: true,
        		multiple: true,
        		searchUrl: '${tenantPrefix}/rs/user/searchVNoMe',
        		treeNoPostUrl: '${tenantPrefix}/party/asyncTree.do?partyStructTypeId=1&notViewPost=true',
        		treeUrl: '${tenantPrefix}/party/asyncTree.do?partyStructTypeId=1&notViewPost=true',
        		childUrl: '${tenantPrefix}/rs/party/searchUser',
        		childPostUrl: '${tenantPrefix}/rs/party/searchPost'
        	});
		})
		
		//调用接口，根据经销商编号，获取直销oa上存的对应信息：姓名 电话 等
		 $(function() {
					$("#ucode").keydown(function(event) {    
						if (event.keyCode == 13) {  
							var id = $("#ucode").val();
							//alert(id);
		                	if (id !="") {
			                    $.getJSON('${tenantPrefix}/rs/varUser/userInfo', {
			                    	customerInfoId: id
			                    }, function(data) {
			                    	//alert(JSON.stringify(data)); 
			          				//$("#realName").html(''+''+data.name);
				          			$("#name").val(data.name);
				          			//$("#welfare").html(data.rank);
				          			$("#contact").val(data.mobile);
				          			$("#qualificationsStatus").val(data.lock);
				          			$("#welfareLevel").val(data.rank);
				          			$("#inNumber").val(data.varCardNO);
				          			$("#aboveBoard").val(data.varDirectName);
				          			$("#directorContact").val(data.varDirectMobile);
				          		});
		                    } else {
		                    	alert("必须输入客户编号!");
		                    }
		                }    
		            });
		}) 
		 //鼠标点击其他地方触发获取信息
		function fnshopData() {
				var id = $("#ucode").val();
				//alert(id);
				if (id !="") {
			        $.getJSON('${tenantPrefix}/rs/varUser/userInfo', {
			        	customerInfoId: id
			        }, function(data) {
			        	//alert(JSON.stringify(data)); 
						$("#name").val(''+''+data.name);
						$("#contact").val(data.mobile);
	          			$("#qualificationsStatus").val(data.lock);
	          			$("#welfareLevel").val(data.rank);
	          			$("#inNumber").val(data.varCardNO);
	          			$("#aboveBoard").val(data.varDirectName);
	          			$("#directorContact").val(data.varDirectMobile);
			  		});
			    } 
		}
		ROOT_URL = '${tenantPrefix}';
		taskOperation = new TaskOperation();
		
		var conf={
				applyCodeId:"applyCode",				//受理单号input的ID
			    submitBtnId:"confirmStartProcess",		//提交按钮ID	
				checkApplyCodeUrl:"${tenantPrefix}/rs/business/applyCodeIfExist",	//验证受理单号url
       			checkUrl:"${tenantPrefix}/rs/customer/opteraion-getposition",		//获取岗位url
       			actionUrl:"${tenantPrefix}/processLllegalFreeze/process-operationLllegalFreeze-startProcessInstance.do",//提交URL
       			businessDetailId:"businessDetailId",			//存储业务明细input的ID
       			formId:"xform",									//form的ID
       			selectAreaId:"area",
   	   			selectCompanyId:"branchOfficeVal",
   	 			iptAreaId:"areaId",
   	 			iptAreaName:"areaName",
   	   	   		iptCompanyId:"companyId",
   	   	   		iptCompanyName:"companyName",
   	   	  		isBranchCompany:"0"
       		}
		function startProcessInstance() {
			$.extend($.validator.defaults,{ignore:""});
			if($("#theme").val().replace(/(^\s*)|(\s*$)/g, "") == ""){
				alert("主题不能为空");
				return
			}
			if($("#busType").val() == "" || $("#busType").val() == "请选择"){
				alert("业务类型不能为空");
				return
			}
			if($("#busDetail").val() == "" || $("#busDetail").val() == "请选择"){
				alert("业务明细不能为空");
				return
			}
			if($("#ucode").val().replace(/(^\s*)|(\s*$)/g, "") == ""){
				alert("编号不能为空");
				return
			}
			if($("#system").val() == "请选择" || $("#system").val() == "" ){
				alert("请选择体系");
				return
			}
			if($("#area").val() == "请选择" || $("#area").val() == ""){
				alert("请选择所属区域");
				return
			}
			if($("#branchOffice").val() == "" || $("#branchOffice").val() == "请选择" || $("#branchOffice").val() == null){
				alert("请选择所属分公司");
				return
			}
			if(!$(':radio[name=applyMatter]:checked').length) {
			    alert('请选择受理事项');
			    return
			}
			if($("#applyContent").val().replace(/(^\s*)|(\s*$)/g, "") == ""){
				alert("请输入内容");
				return
			}
			if($("#welfareLevel").val() == ""||$("#welfareLevel").val() == null){
				alert("编号不存在，请检查后输入");
				return
			}
			if($("#theme").val().length >'100'){
				var msg = "主题已超出100字，继续提交系统会自动截取100字以内内容，返回修改请取消";  
	            if (!confirm(msg)){  
	                return false;  
	            }  
			}
			if($("#applyContent").val().length > '5000'){
				var msg = "申请内容已超出5000字，继续提交系统会自动截取5000字以内内容，返回修改请取消";  
	            if (!confirm(msg)){  
	                return false;  
	            }  
			}
			
			fnFormSubmit(conf);
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
		//获得选择的体系
   	   function getSystemName() {
		 var  myselect=document.getElementById("system");
		 var index=myselect.selectedIndex ;  
		 var text=myselect.options[index].text;
		 $("#systemName").val(text);
	  }
		//下拉框联动：先到数据库中取业务类型
	   $(function(){
  			//业务类型
  			<%-- var userId =<%=request.getParameter("userId")%>;
  			var url = document.getElementById("r").value;
	    	$.getJSON('${tenantPrefix}/rs/business/post_types', {userId : userId, url : url}, function(data) {
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
	    		var userId = $("#userId").val();
	  			var url = document.getElementById("r").value;
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
	    		//alert(t);
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
	    		
	    		checkPostion(conf);
	    	
	    		//实现不同业务细分挂不同流程：根据用户选择的业务类型和业务细分去oa_ba_business_detail取流程的ID
	    		var businessDetailId= $("#busDetail").val();
	    		if(businessDetailId == ""){
	    			$('#nextStep').html('');
	    		}else{
	    			$.ajax({
	    				url:"${tenantPrefix}/dict/getProcessPostInfoByBusinessDetailId.do",
	    				data:{businessDetailID:businessDetailId},
	    				dataType:"json",
	    				type:"post",
	    				success:function(data){
	    					//console.log(data);
	    					$("#bpmProcessId").val(data.bpmProcessId);
	    					if($("#nextStep").html() != ''){
	  		  					$('#nextStep').html('');
	  		  				}
	  	    				$('#nextStep').append(data.whole);
	    				},
	    				error:function(){
	    					alert("获取流程审核人岗位信息出错！");
	    				}
	    			});
	    		}
	    }	 
	     
	    	
	    	
	    	
	    	
	    	/*==============================================================================================  */
	    	
		//下拉框联动：先到数据库中取区域id
	    $(function() {  
			
			//单据号用0自动补全6位
			function fill(numLen,n){
				var len = (numLen+"").length;
				//alert(len);
				while(len < n) {
					numLen = "0" + numLen;
					len++;
				}
				return numLen;

			}
	  	}); 
	     
/*=======================================================================================================================================  */	     
	    	 //获取区域和分公司
	    	$.getJSON('${tenantPrefix}/rs/party/AreaName',{userId : $("#userId").val()},function(data){
	    		var str = "";
	    		if(data[0] != null && data[0].name.substring(2) == "大区"){
	    			conf.isBranchCompany="1";
	    			$("#areaId").val(data[0].id);
	    			str += "<option value='" + data[0].id + "'>" + data[0].name + "</option>";
	    			$("#area").html(str);
	    			var  myselect=document.getElementById("area");
		    		var index=myselect.selectedIndex ;  
		    		var bt=myselect.options[index].value;
		    		var t=myselect.options[index].text;
		    		$("#areaId").val(bt);
		    		$("#areaVal").val(t);
	    			//获取分公司
	    	    	$.getJSON('${tenantPrefix}/rs/party/branchOfficeName',{userId : $("#userId").val()},
	    	    			function(data){
	    	    				var str = "";
	    	    				str += "<option value='" + data[0].id + "'>" + data[0].name + "</option>";
	    	    				$("#branchOffice").html(str);
	    	    				var  myselect=document.getElementById("branchOffice");
	    			    		var index=myselect.selectedIndex ;  
	    			    		var bt=myselect.options[index].value;
	    			    		var t=myselect.options[index].text;
	    			    		$("#branchOfficeVal").val(t);
	    			    		$("#companyId").val(bt);
	    	    			}
	    	    	)
	    		}else{
	    			$.getJSON('${tenantPrefix}/rs/party/regionId', {},
	    		    		function(data) {
	    		    		var str = "";
	    		    		var choose = "<option value='请选择'>请选择</option>";
	    	    			for(var i = 0;i<data.length;i++){
	    	    				if(i == 0){
	    	    					str += choose;
	    	    				}
	    	    				str += "<option value='" + data[i].id + "'>" + data[i].name + "</option>";
	    	    				$("#area").html(str);
	    	    			}
	    		    });
	    		}
	    		
	    	})
		    	function getBranchOffice(){
		    		//根据用户选择的大区，到数据库中取分公司
		    		var  myselect=document.getElementById("area");
		    		var index=myselect.selectedIndex ;  
		    		var bt=myselect.options[index].value;
		    		var t=myselect.options[index].text;
		    		$("#areaId").val(bt);
		    		$("#areaVal").val(t);
		    		// alert(JSON.stringify(myselect.options[index].value));
		    		$.getJSON('${tenantPrefix}/rs/party/childEntityId', 
		  	    			{ bt:bt},    
		  	    			function(data) {
		  	    		var option = "<option value=''>请选择</option>" ;  
		  	        	for (var i = 0; i < data.length; i++) {
		  	        		//alert(JSON.stringify(data[i])); 
		  	                option += "<option value='"+ data[i].id+"'>"+ data[i].name+"</option>"  
		  	 			  }
		  	  			 $("#branchOffice").html(option);//将循环拼接的字符串插入第二个下拉列表  
			    		
		  	    	});
        		}
        		//动态获取分公司的值
				function getCompany(){
					var myselect=document.getElementById("branchOffice");
					var index=myselect.selectedIndex ;  
		    		var bt=myselect.options[index].value;
		    		var t=myselect.options[index].text;
		    		$("#branchOfficeVal").val(t);
		    		$("#companyId").val(bt);
				}
	
    </script>
  </head>

  <body>
    <%@include file="/header/bpm-workspace3.jsp"%>
<form id="xform" name="form" method="post" action="${tenantPrefix}/processLllegalFreeze/process-operationLllegalFreeze-startProcessInstance.do" class="xf-form" enctype="multipart/form-data">
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
		<input id="date" type="hidden" name="date" value="">
		<input id="deptName" type="hidden" name="deptName" value="">
		<input id="url" type="hidden" name="url" value="/processLllegalFreeze/form-detail.do">
		<input id="r" type="hidden"  name="r" value="${url}">
		<input id="areaId" type="hidden"  name="areaId">
		<input id="companyId" type="hidden"  name="companyId">
		<input id="businessDetailId" type="hidden"  name="businessDetailId">
		<div id="xf-form-table">
			<div id="xf-1" class="xf-section">
				<h1 style="text-align:center;">违规冻结/解冻申请单</h1>
			</div>
			
			<div id="xf-2" class="xf-section">
				<table class="xf-table" width="100%" cellspacing="0" cellpadding="0" border="0" align="center">
					<tbody>
						<tr id="xf-2-0">
							<td id="xf-2-0-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" colspan="4" width="100%">
								<div class="xf-handler">
									<label style="display:block;text-align:right;margin-bottom:0px;">提交次数： <input style="border:0px;width:10px" readonly type="text" id="submitTimes" name="submitTimes" value="0">      &nbsp;&nbsp;申请单号: <input type="text" id="applyCode" name="applyCode" value="${code}" style="border:0px" readonly></label>
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
									<input id="theme" name="theme" type="text" style="width:100%" required maxlength="100">
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
								<!-- <div class="input-group userPicker">
	                                <input id="btnPickerMany" type="hidden" name="copyUserValue" class="input-medium"  value="">
	                                <input type="text" id="userName" name="cc" style="width: 800px;background-color:white"
                                     value="" class="form-control" readOnly placeholder="点击后方图标即可选人">
	                                <div id="ccDiv" class="input-group-addon"><i class="glyphicon glyphicon-user"></i></div>
                            	</div> -->
                            	
                            	<div class="input-group userPicker" style="width:100%;">
								  <input id="btnPickerMany" type="hidden" name="copyUserValue" class="input-medium" value="">
								  <input id="userName"  type="text" name="cc" placeholder="点击后方图标即可选人" style="width: 800px;background-color:white"
                                     value="" class="form-control" readonly>
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
										<option value="">请选择</option>
										<c:forEach items="${businessTypeList}" var="item">
  											<option value="${item.id}" >${item.name}</option>
  										</c:forEach>
										<input type="hidden" id="businessType" name="businessType" required>
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
										<input type="hidden" id="businessDetail" name="businessDetail" required>
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
									<input type="hidden" id="businessLevel" name="businessLevel" required>
								</select>
							</td>
							<td id="xf-2-4-2" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="25%">
								<label style="display:block;text-align:center;margin-bottom:0px;">发起人：</label>
							</td>
							<td id="xf-2-4-3" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="25%">
								<div class="xf-handler">
									<input type="text" id="initiator" name="initiator"  style="width:100%" readonly value="<tags:user userId='<%=userId%>'/>"/>
								</div>
							</td>
						</tr>
						
						<tr id="xf-2-5">
							<td id="xf-2-5-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="100%" colspan="4">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">申请内容</label>
								</div>
							</td>
						</tr>
						<tr id="xf-2-6">
							<td id="xf-2-6-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="25%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;"><font color='red'>*</font>&nbsp;编号：</label>
								</div>
							</td>
							<td id="xf-2-6-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="75%">
								<div class="xf-handler">
									<input type="text" id="ucode" name="ucode" style="width:100%" onblur='fnshopData()' maxlength="8" onkeyup="var reg = /^[0-9]\d*$/; if(!reg.test(this.value)) this.value = ''; " required>
								</div>
							</td>
							<td id="xf-2-6-2" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="25%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;"><font color='red'>*</font>&nbsp;姓名：</label>
								</div>
							</td>
							<td id="xf-2-6-3" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" colspan="3" width="75%">
								<div class="xf-handler">
									<input type="text" id="name" name="name" style="width:100%" readonly required>
								</div>
							</td>
						</tr>
						<tr id="xf-2-7">
							<td id="xf-2-7-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="100%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;"><font color='red'>*</font>&nbsp;福利级别：</label>
								</div>
							</td>
							<td id="xf-2-7-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="100%">
								<div class="xf-handler">
									<input type="text" id="welfareLevel" name="welfareLevel" style="width:100%" readonly required>
								</div>
							</td>
							<td id="xf-2-7-2" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="100%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;"><font color='red'>*</font>&nbsp;资格状态：</label>
								</div>
							</td>
							<td id="xf-2-7-3" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="100%">
								<div class="xf-handler">
									<input type="text" id="qualificationsStatus" name="qualificationsStatus" style="width:100%" readonly required>
								</div>
							</td>
						</tr>
						<tr id="xf-2-8">
							<td id="xf-2-8-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="100%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;"><font color='red'>*</font>&nbsp;所属体系：</label>
								</div>
							</td>
							<td id="xf-2-8-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="100%">
								<div class="xf-handler">
									<select id="system" name="system" required onchange="getSystemName()">
										<option value="请选择">请选择</option>
										<c:forEach items="${systemlist}" var="item">
		  									<option value="${item.value}" >${item.name}</option>
		  								</c:forEach>
									</select>
									<input type="hidden" id="systemName" name="systemName">
								</div>
							</td>
							<td id="xf-2-8-2" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="100%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;"><font color='red'>*</font>&nbsp;联系方式：</label>
								</div>
							</td>
							<td id="xf-2-8-3" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="100%">
								<div class="xf-handler">
									<input type="text" id="contact" name="contact" style="width:100%" readonly required>
								</div>
							</td>
						</tr>
						<tr id="xf-2-9">
							<td id="xf-2-9-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="100%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;"><font color='red'>*</font>&nbsp;所属区域：</label>
								</div>
							</td>
							<td id="xf-2-9-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="100%">
								<div class="xf-handler">
									<select id="area" onchange="getBranchOffice()">
										<input id="areaVal" name="area" type="hidden">
									</select>
								</div>
							</td>
							<td id="xf-2-9-2" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="100%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;"><font color='red'>*</font>&nbsp;所属分公司：</label>
								</div>
							</td>
							<td id="xf-2-9-3" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="100%">
								<div class="xf-handler">
									<select id="branchOffice" onchange="getCompany()">
										<option value="" selected = "selected">请选择</option>
										<input id="branchOfficeVal" name="company" type="hidden">
									</select>
								</div>
							</td>
						</tr>
						<tr id="xf-2-10">
							<td id="xf-2-10-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="100%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;"><font color='red'>*</font>&nbsp;身份证号：</label>
								</div>
							</td>
							<td id="xf-2-10-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="100%" colspan="3">
								<div class="xf-handler">
									<input type="text" id="inNumber" name="idNumber" style="width:100%" readonly required>
								</div>
							</td>
						</tr>
						<tr id="xf-2-11">
							<td id="xf-2-11-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="100%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;"><font color='red'>*</font>&nbsp;上属董事：</label>
								</div>
							</td>
							<td id="xf-2-11-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="100%">
								<div class="xf-handler">
									<input type="text" id="aboveBoard" name="aboveBoard" style="width:100%" readonly required>
								</div>
							</td>
							<td id="xf-2-11-2" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="100%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;"><font color='red'>*</font>&nbsp;联系方式：</label>
								</div>
							</td>
							<td id="xf-2-11-3" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="100%">
								<div class="xf-handler">
									<input type="text" id="directorContact" name="directorContact" style="width:100%" readonly required>
								</div>
							</td>
						</tr>
						<tr id="xf-2-12">
							<td id="xf-2-12-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="100%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;"><font color='red'>*</font>&nbsp;申请受理事项：</label>
								</div>
							</td>
							<td id="xf-2-12-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="100%" colspan="3">
								<div class="xf-handler">
									<input type="radio" name="applyMatter" value="冻结">冻结&nbsp;&nbsp;&nbsp;&nbsp;
									<input type="radio" name="applyMatter" value="开除">开除&nbsp;&nbsp;&nbsp;&nbsp;
									<input type="radio" name="applyMatter" value="解冻">解冻&nbsp;&nbsp;&nbsp;&nbsp;
									<input type="radio" name="applyMatter" value="其他">其他
								</div>
							</td>
						</tr>
						<tr id="xf-2-13">
							<td id="xf-2-13-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="100%">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;"><font color='red'>*</font>&nbsp;申请内容：</label>
								</div>
							</td>
							<td id="xf-2-13-1" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="100%" colspan="3">
								<div class="xf-handler">
									<textarea rows="10" maxlength="5000" id="applyContent" name="applyContent" style="width:100%" onkeyup="MaxWords(this)" onblur="MaxWords(this)" required></textarea>
									<label id="recordNum" style="display: none;text-align:right;" class="control-label col-md-12"></label>
								</div>
							</td>
						</tr>
						<tr id="xf-2-14">
							<td id="xf-2-14-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" width="100%" colspan="4">
								<div class="xf-handler">
									<label style="display:block;text-align:center;margin-bottom:0px;">证据：</label>
								</div>
							</td>
						</tr>
						<tr id="xf-2-15">
							<td id="xf-2-15-0" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left" colspan="4">
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
			<!-- <button id="saveDraft" class="btn btn-default" type="button" onclick="taskOperation.saveDraft()">保存草稿</button> -->
			<!-- <button id="confirmStartProcess" class="btn btn-default" type="button" onclick="taskOperation.taskConf()">配置任务</button> -->
			<button id="confirmStartProcess" class="btn btn-default" type="button" onclick="startProcessInstance()" value="提交">提交数据</button>
			<button type="button" class="btn btn-default" onclick="javascript:history.back();">返回</button>
		</div>
	
	  </div>
    </div>
  </div>
  <%@include file="/common/selectPosition.jsp" %>
</form>


</body>

</html>
