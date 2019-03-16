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
		          			$("#userName").val(''+''+data.name);
		          			//$("#welfare").html(data.rank);
		          			$("#welfare").val(data.rank);
	          				$("#level").val(data.level);
							
							$("#varFather").val(data.father);
		          			
		          			$("#varRe").val(data.re);
		          			
		          			$("#addTime").val(data.addTime);
		          			
		          			$("#mobile").val(data.mobile);
		          			
		          			$("#address").val(data.address);
		          			
		          			$("#treeInfo").val('A区总业绩'+data.totalA+' '+'B区总业绩'+data.totalB+' '+'激活状态：'+data.pay);
		          			
		          		});
                    } else {
                    	alert("必须输入客户编号!");
                    }
                }    
            });
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
				          			$("#userName").val(''+''+data.name);
				          			//$("#welfare").html(data.rank);
				          			$("#welfare").val(data.rank);
			          				$("#level").val(data.level);
									
									$("#varFather").val(data.father);
				          			
				          			$("#varRe").val(data.re);
				          			
				          			$("#addTime").val(data.addTime);
				          			
				          			$("#mobile").val(data.mobile);
				          			
				          			$("#address").val(data.address);
				          			
				          			$("#treeInfo").val('A区总业绩'+data.totalA+' '+'B区总业绩'+data.totalB+' '+'激活状态：'+data.pay);
				          			
				          		});
		                    } else {
		                    	alert("必须输入客户编号!");
		                    }
		                }    
		            });
		
			setTimeout(function() {
				$('.datepicker').datepicker({
					autoclose: true,
					language: 'zh_CN',
					format: 'yyyy-mm-dd'
				})
			}, 500);
		})
		
		ROOT_URL = '${tenantPrefix}';
		taskOperation = new TaskOperation();
		var conf={
				applyCodeId:"applyCode",				//受理单号input的ID
			    submitBtnId:"confirmStartProcess",		//提交按钮ID	
				checkApplyCodeUrl:"${tenantPrefix}/rs/business/applyCodeIfExist",	//验证受理单号url
       			checkUrl:"${tenantPrefix}/rs/customer/opteraion-getposition",		//获取岗位url
       			actionUrl:"${tenantPrefix}/operationApply/process-operationApply-startProcessInstance.do",//提交URL
       			businessDetailId:"businessDetailId",			//存储业务明细input的ID
       			formId:"xform",									//form的ID
       			selectAreaId:"area",
   	   			selectCompanyId:"branchOffice",
   	 			iptAreaId:"areaId",
   	 			iptAreaName:"areaName",
   	   	   		iptCompanyId:"companyId",
   	   	   		iptCompanyName:"companyName"
       		}
		function startProcessInstance() {
			
			//经销商编号 为空  不允许提交
			if ( document.getElementById('ucode').value ==""){
				alert("请输入经销商编号！");
				return false;
			}
			
			//经销商姓名 为空  不允许提交
			if ( (document.getElementById('userName').value =="")|| (document.getElementById('userName').value =="null")){
				alert("经销商编号不正确！");
				return false;
			}
			
			//申请业务类型不能是空
			if ( document.getElementById('businessType').value ==""||document.getElementById('businessType').value =="请选择"){
				alert("请选择申请业务类型！");
				return false;
			}
			
			//申请业务明细不能是空
			if ( document.getElementById('businessDetail').value ==""||document.getElementById('businessDetail').value =="请选择"){
				alert("请选择业务细分！");
				return false;
			}
			
			//所属体系不能是空
			if ( document.getElementById('system').value ==""||document.getElementById('system').value =="请选择"){
				alert("请选择所属体系！");
				return false;
			}
			
			if($("input[name='fileName']").length>0){
				if($("input[name='fileName']:checked").length<1){
					alert("请选择需修改系统！");
					return false;
				}
			}
			
			//申请内容不能是空
			if ( document.getElementById('applyContent').value ==""){
				alert("请填写申请内容！");
				return false;
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
    </script>
    
</head>
<style type="text/css">
		#tb1{text-align:center; margin:10px auto;width:100%;}
        #tb1 td{border:1px solid #BBB; height:36px;line-height:36px;}
        #tb1 td,.f_td{font-size:14px;white-space:nowrap;}
        .f_r_td{text-align:left;}
        #tb1 tr td input[type='text']{background:#eee;;width:100%;height:38px;padding-left:15px;border:none;} 
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
    <form id="xform" method="post"   action="${tenantPrefix}/operationApply/process-operationApply-startProcessInstance.do" class="xf-form" enctype="multipart/form-data">
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
    	<input id="processDefinitionId" type="hidden" name="processDefinitionId" value="${processDefinitionId}">
		<input id="bpmProcessId" type="hidden"  name="bpmProcessId"  value="${bpmProcessId}">
		<input id="businessDetailId" type="hidden" name="businessDetailId" value="${businessDetailId}">
		<input id="autoCompleteFirstTask" type="hidden" name="autoCompleteFirstTask" value="false">
		<input id="businessKey" type="hidden" name="businessKey" value="${businessKey}">
    	<input id="url" type="hidden" name="url" value="/operationApply/commApplyFrom-detail.do">
    	<input id="areaId" type="hidden" name="areaId" value="${areaId}">
    	<input id="companyId" type="hidden" name="companyId">
		<input id="companyName" type="hidden" name="companyName">
    	<input id="userId" type="hidden" name="userId" value="${userId}">
    	<input id="r" type="hidden"  name="r" value="${url}">

	   	<table id="tb1">
   		   <tr>
               <td colspan='8' align='center' class='f_td btitle'>
                  	 业务受理申请单
               </td>
           </tr>
           <tr>
               <td colspan='2'  class='f_td' align='right' style='padding-right:20px;'>
                   	提交次数：
                </td>
                <td colspan='2'  class='f_td' align='right' style='padding-right:20px;'>
                   	0
                </td>
               <td colspan='2'  class='f_td' align='right' style='padding-right:20px;'>
                   	受理单编号：
               </td>
               <td colspan='2'  class='f_td' align='right' style='padding-right:20px;'>
               		${code}
                   	<input id="applyCode" name="applyCode" value="${code}" readonly  style="display:none;"/>
               </td>
           </tr>
   		<tr>
        	  	<td >
               	<span id='tag_userID'>&nbsp;<span style="color:Red">*</span>经销商编号</span>：
               </td>
               <td >
                   <input id ="ucode" name="ucode" type="text" maxlength="8" onblur=fnUserData();  />
               </td>
               <td >
                   <span id='tag_realName'>&nbsp;经销商姓名</span>：
               </td>
               <td >
               	<input name="userName" id="userName" type="text" readonly> 
               </td>
              <td class='f_td'>
                   <span id='tag_welfare'>&nbsp;福利级别</span>：
               </td>
               <td>
                   <input name="welfare" id="welfare" type="text" readonly> 
               </td>
               <td class='f_td'>
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
                   <select name="system" class="form-control" id="system" onchange="getSystemName()">
					<c:forEach items="${systemlist}" var="item">
	  					<option value="${item.value}" >${item.name}</option>
	  				</c:forEach>			
				</select>
				<input id="systemName" name="systemName" type="hidden"  >
               </td>
               <td >
                   <span id='tag_seller'>&nbsp;销售人</span>：
               </td>
               <td style="background:#eee;">
                   <input name="varFather" id="varFather" type="hidden" readonly>
               </td>
               <td >
                   <span id='tag_service'>&nbsp;服务人</span>：  
               </td>
               <td style="background:#eee;">
                    <input name="varRe" id="varRe"  type="hidden"  readonly> 
               </td>
               <td >
                   <span id='tag_addTime'>&nbsp;注册时间</span>：
               </td>
               <td style="background:#eee;">
                    <input name="addTime" id="addTime" type="hidden"  readonly> 
               </td>
           </tr>
           <tr>
               <td class='f_td'>
                   <span id='Span1'>&nbsp;联系电话</span>：
               </td>
               <td colspan='3'>
                    <input name="mobile" id="mobile" type="text" readonly> 
               </td>   
               <td width='100px'  class='f_td'>
                   <span id='Span2'>&nbsp;联系地址</span>：
               </td>
               <td colspan='3'>
                   <input name="address" id="address" type="text" readonly> 
               </td>
           </tr>
           <tr>
               <td  style="white-space:nowrap">
                   <span id='tag_bustype'>&nbsp;<span style="color:Red">*</span>申请业务类型</span>：
               </td>
               <td style="text-align:left;" colspan='3'>
                   <select id="busType" class="form-control" name="busType"  onchange="getBusinessDetail()">
					<option value="">请选择</option>
					<c:forEach items="${businessTypeList}" var="item">
						<option value="${item.id}" >${item.name}</option>
					</c:forEach>
				</select>
				<input id="businessType" name="businessType"  type="hidden">
               </td>
               <td class='f_td'>
                   <span id='tag_busDetails'>&nbsp;<span style="color:Red">*</span>业务细分</span>：
               </td>
               <td colspan='3' style="text-align:left;">
                    <select id="busDetails" class="form-control" name="busDetails"  onchange="setLevel()">
                       <option value="">请选择</option>
                    </select>
                    <input id="businessDetail" name="businessDetail" type="hidden">
                    <input type = "hidden"  id="businessde" name="businessde" >
               </td>
           </tr>
           <tr>
               <td >
                   <span id='tag_sLevel'>&nbsp;<span style="color:Red">*</span>业务级别</span>：
               </td>
               <td colspan='3' style="text-align:left;">
                   <select class="form-control" name="busLevel" id="busLevel" >
				</select>
                   <input type="hidden"  name="businessLevel" id="businessLevel" />
               </td>   
               <td width='100px'  class='f_td'>
                   <span id='tag_belongs'>&nbsp;<span style="color:Red">*</span>所属大区：</span>
               </td>
               <td  colspan='3' class='f_r_td' style="text-align:left;">
                    <%-- <tags:party partyId='${areaId}'/> --%>
                    <input class="con" id="areaName" type="text" name="areaName" value="<tags:party partyId='${areaId}'/>" style="background:#fff;" readonly> 
                </td>
            </tr>
            <tr id="tr_edit_phone_system" style="display:none;">
               <td>
                   <span id='tag_sLevel'>&nbsp;<span style="color:Red">*</span>需修改系统：</span>
               </td>
               <td colspan='7' style="text-align:left;" class="tdSystem">
	               	<%--&emsp;
	               	<input id="chkSell" type="checkbox" name="fileName" value="1"/>&nbsp;<span>办公系统</span>
                	&emsp;
            		<input id="chkMall" type="checkbox" name="fileName" value="2"/>&nbsp;<span>麦茂商城</span> --%>
               </td>
            </tr>
           
           <tr>
               <td colspan='8' align='center' class='f_td'>
                   <span style="color:Red">*</span>申请内容
               </td>
           </tr>
           <tr>
               <td colspan='8' style='height:100px; text-align:left;' >
                   <textarea  maxlength="5000"  name="applyContent" id="applyContent" rows="3" cols="28" class="text0" style="height:99px;padding-left:10px;padding-top:10px" onkeyup="MaxWords(this)" onblur="MaxWords(this)"></textarea>
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
                  <input id="businessStand1" name="businessStand1" type="hidden">
               </td>
               <td colspan='3' style='height:auto; width:300px; white-space:normal; ' class='leftAlign'>
                  <p style="white-space:pre-wrap;text-align:left; padding:5px 30px;"><span id="standard2" ></span></p>
                   <input id="businessStand2" name="businessStand2" type="hidden">
               </td>
               <td colspan='2'  style='height:auto; width:200px;white-space:normal;' class='leftAlign'>
                    <input type="hidden"  name="treeInfo" id="treeInfo" />
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
 	</section>
	<!-- end of main -->
  </div>
	<br/>
   	<br/>
    <br/>
	<div class="navbar navbar-default navbar-fixed-bottom">
		<div class="text-center" style="padding-top:8px;">
			<div class="text-center" style="padding-top:8px;">
				<button id="confirmStartProcess" class="btn btn-default" type="button" onclick="startProcessInstance()">提交数据</button>
				<button type="button" class="btn btn-default" onclick="javascript:history.back();">返回</button>
			</div>
		</div>
 	</div>
	<input type='text' style='display:none'/>
	<%@include file="/common/selectPosition.jsp" %>
</form>
</body>

<script>
	//下拉框联动：先到数据库中取业务类型
	 function getSystemName() {
		var  myselect=document.getElementById("system");
		var index=myselect.selectedIndex;
		var text=myselect.options[index].text;
		$("#systemName").val(text);
	 }
 
	//根据用户选择的业务类型，到数据库中取业务细分
 	function getBusinessDetail() {
   		var  myselect=document.getElementById("busType");
   		var index=myselect.selectedIndex ;  
   		var bt=myselect.options[index].value;
   		var t=myselect.options[index].text;
   		$("#businessType").val(t);
   		
   		var url = document.getElementById('r').value;
   		var userId =document.getElementById('userId').value;
   		$.getJSON('${tenantPrefix}/rs/business/post_details',{ bt:bt,userId:userId,url:url},function(data) {
    		var option = "<option value=''>请选择</option>" ;  
        	for (var prop in data[0]) {
                option += "<option value='"+ prop+"'>"+ data[0][prop]+"</option>"  
		  	}
 			$("#busDetails").html(option);//将循环拼接的字符串插入第二个下拉列表  
    	});
   	}
    	
   	//根据用户选择的业务类型明细，到数据库中取业务级别
   	function setLevel() {
   		var  myselect=document.getElementById("busDetails");
   		var index=myselect.selectedIndex ;  
   		var bd=myselect.options[index].value;
   		var t=myselect.options[index].text;
   		$("#businessDetail").val(t);
   		$.getJSON('${tenantPrefix}/rs/business/level',{ bd:bd},function(data) {
      		var option = "" ;  
          	for (var i = 0; i < data.length; i++) {
                 option += "<option value='"+ data[i].id+"'>"+ data[i].level+"</option>"
                 $("#standard").text(data[i].standFirst);
                 $("#standard2").text(data[i].standSecond);
                 $("#businessStand1").val(data[i].standFirst);
                 $("#businessStand2").val(data[i].standSecond);
            }
   			$("#busLevel").html(option);//将循环拼接的字符串插入第二个下拉列表  
   			var  myselect=document.getElementById("busLevel");
       		var index=myselect.selectedIndex;
       		var t=myselect.options[index].text;
     			$("#businessLevel").val(t);
   		 });
   		
   		checkPostion(conf);
   		
   		fnGetEditPhoneDetailId();
   		
   		//实现不同业务细分挂不同流程：根据用户选择的业务类型和业务细分去oa_ba_business_detail取流程的ID
   		if(bd == ""){
   			$('#nextStep').html('');
   		}else{
   			$.ajax({
   				url:"${tenantPrefix}/dict/getProcessPostInfoByBusinessDetailId.do",
   				data:{businessDetailID:bd},
   				dataType:"json",
   				type:"post",
   				success:function(data){
   					$("#bpmProcessId").val(data.bpmProcessId);
   					if($("#nextStep").html() != ''){
	  					$('#nextStep').html('');
	  				}
    				$('#nextStep').append(data.whole);
   				},
   				error:function(){
					alert("获取流程审核人岗位信息出错！");
   				}
   			})
   		}
   	}
    	
    //调用接口，根据经销商编号，获取直销oa上存的对应信息：姓名 电话 等
    function fnUserData() {
    	var id=$("#ucode").val();
    	if(id!="") {
            $.getJSON('${tenantPrefix}/rs/varUser/userInfo', {customerInfoId: id}, function(data) {
      			$("#userName").val(''+''+data.name);
      			$("#welfare").val(data.rank);
  				$("#level").val(data.level);
				$("#varFather").val(data.father);
      			$("#varRe").val(data.re);
      			$("#addTime").val(data.addTime);
      			$("#mobile").val(data.mobile);
      			$("#address").val(data.address);
      			$("#treeInfo").val('A区总业绩'+data.totalA+' '+'B区总业绩'+data.totalB+' '+'激活状态：'+data.pay);
     		});
        } 
    }
    
    function fnGetEditPhoneDetailId() {
    	var businessDetailId=$("#busDetails").val();
    	if(businessDetailId!=""){
            $.getJSON('${tenantPrefix}/rs/customer/getEditPhoneDetailId',{}, function(data) {
      			if(data==undefined||data==null||data=="")
      				return;
      			var detailId="";
      			if(data.code==200){
      				detailId=data.data.detailInfo.businessDetailId;
      			}
      			if(detailId!=""){
      				if(businessDetailId==detailId){
      					var html='';
      					for(var i=0;i<data.data.detailInfo.systemList.length;i++){
      						var systemInfo=data.data.detailInfo.systemList[i];
      						html+='&emsp;<input type="checkbox" name="fileName" value="'+systemInfo.value+'"/>&nbsp;<span>'+systemInfo.title+'</span>';
      					}
      					$("#tr_edit_phone_system .tdSystem").html(html);
      					$("#tr_edit_phone_system").show();
      				}
      				else{
      					$("#tr_edit_phone_system .tdSystem").html("");
      					$("#tr_edit_phone_system").hide();
      				}
      			}
      		});
        } 
    }
 </script>
</html>

