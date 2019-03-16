<%@page contentType="text/html;charset=UTF-8"%>
<%@include file="/taglibs.jsp"%>
<%pageContext.setAttribute("currentHeader", "bpm-workspace");%>
<%pageContext.setAttribute("currentMenu", "bpm-process");%>

<!doctype html>
<html lang="en">
<head>
<%@include file="/common/meta.jsp"%>
 <script type="text/javascript" src="jquery-1.7.2.min.js"></script>
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
		input{height:25px;}
	</style>
	
	<script type="text/javascript">
		document.onmousedown = function(e) {};
		document.onmousemove = function(e) {};
		document.onmouseup = function(e) {};
		document.ondblclick = function(e) {};

		var xform;
		
		 $(function() {
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
       			actionUrl:"${tenantPrefix}/CarApply/CarApply-startProcessInstance.do",//提交URL
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
			if ( document.getElementById('carUser').value ==""){
				alert("请输入用车人！");
				return false;
			}
			
			if ( document.getElementById('departmentName').value ==""){
				alert("请输入部门！");
				return false;
			}
			
			if ( document.getElementById('destination').value ==""){
				alert("请输入目的地！");
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
			
			if ( document.getElementById('content').value ==""){
				alert("请输入用车事由！");
				return false;
			}
			
			if(document.getElementById('startDate').value == ""){
        		alert("请选择开始时间！");
                return false;
        	}
        	if(document.getElementById('endDate').value == ""){
        		alert("请选择结束时间！");
                return false;
        	}
        	if(document.getElementById('totalTime').value == ""){
        		alert("请填写总计时间！");
                return false;
        	}
			
			fnFormSubmit(conf);
		}
		
		//共计时长的校验
    	function checkedTime(obj){
    		var a = obj;
    		a.value=a.value.toString().match(/^\d+(?:\.\d{0,1})?/)
    	}
		
		//计算用车时长（小时，保留一位小数）
    	  function checkField( ){
    		  if((!document.getElementById('startDate').value == "")&&
    				  (!document.getElementById('endDate').value == "")){
          		var t1 = document.getElementById('startDate').value;
          		var t2 = document.getElementById('endDate').value;
          		var date1 = new Date(t1);
          		var date2 = new Date(t2);
          		var s1 = date1.getTime(),s2 = date2.getTime();
          		var total = (s2 - s1)/1000;
          		var hour = (total/(60*60)).toFixed(1);
          		$("#totalTime").val(hour);
          	}
    	 }
    </script>
    
</head>
<style type="text/css">
         #tb1 td{border:1px solid #BBB;white-space: nowrap; font-size:14px; height:41px;line-height:41px;}
        .f_td{ width:150px; font-size:12px;white-space:nowrap ;font-size:10pt;}
        .f_r_td{ width:130px; text-align:left;font-size:14px;}  
        #tb1  input{padding-left:5px;   font-size:14px; border: none; overflow: hidden;height: 100%;width: 100%;background-color:#eee;}   
        #tb1 tr td{text-align:center;font-size:14px;}
        select.form-control{border:0;width:100%;}
        textarea{background-color:#eee;border:0;padding:6px 0 0 6px;height:120px;width:100%;}  
    </style>
<body>
 <%@include file="/header/bpm-workspace3.jsp"%>
    <form id="xform" method="post"   action="${tenantPrefix}/CarApply/CarApply-startProcessInstance.do" class="xf-form" enctype="multipart/form-data">
			用车申请单
    <br />

    <div class="container" >
     <section id="m-main" class="col-md-12" style="padding-top:65px;padding-bottom:20px">
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
		<input id="bpmProcessId" type="hidden"  name="bpmProcessId" value="${bpmProcessId}">
		<input id="categoryId" type="hidden" name="categoryId" value="${categoryId}"/>
		<input id="businessDetailId" type="hidden" name="businessDetailId" value="">
		<input id="autoCompleteFirstTask" type="hidden" name="autoCompleteFirstTask" value="false">
		<input id="businessKey" type="hidden" name="businessKey" value="${businessKey}">
    	<input id="url" type="hidden" name="url" value="/CarApply/car-apply-detail.do">
    	<input id="areaId" type="hidden" name="areaId" value="${areaId}">
<%--     	<input id="areaName" type="hidden" name="areaName" value="<tags:party partyId='${areaId}'/>"> --%>
    	<input id="companyId" type="hidden" name="companyId">
		<input id="companyName" type="hidden" name="companyName">
		<input id="r" type="hidden"  name="r" value="${url}">
		<input id="userId" type="hidden" name="userId" value="${userId}">
		
    	<table id="tb1" width="100%" style="margin-top:25px;">
    		 <tr>
                <td colspan='4' align='center' class='f_td'><h2>用车申请单</h2></td>
            </tr>
             <tr>
                <td colspan='2'  class='f_td' align='right' style='padding-right:20px;'>
                    	受理单编号：
                </td>
                <td colspan='2'  class='f_td' align='right' style='padding-right:20px;'>
                    	${code} <input id="applyCode" name="applyCode" value="${code}" type="hidden">
                </td>
            </tr>
 			<tr>
                <td width="120px">
                    <span style="color:Red">*</span><span id='tag_realName'>&nbsp;用车人姓名</span>：
                </td>
                <td>
                	<input name="carUser" id="carUser" maxlength="10"> 
                </td>
               <td width="120px" >
                    <span style="color:Red">*</span><span id='tag_welfare'>&nbsp;部门</span>：
                </td>
                <td>
                    <input name="departmentName" id="departmentName" maxlength="10"> 
                </td>
          </tr>
          <tr>
               <td class='f_td'>
                   <span style="color:Red">*</span><span id='tag_level'>&nbsp;目的地</span>：
               </td>
               <td colspan="3">
                   <input name="destination" id="destination" maxlength="10" >
               </td>
            </tr>
            <tr>
                <td  style="white-space:nowrap">
                    <span id='tag_bustype'>&nbsp;<span style="color:Red">*</span>申请业务类型</span>：
                </td>
                <td style="text-align:left;width:420px;">
                    <select id="busType" name="busType"  onchange="getBusinessDetail()" class="form-control">
						<option value="">请选择</option>
						<c:forEach items="${businessTypeList}" var="item">
							<option value="${item.id}" >${item.name}</option>
						</c:forEach>
						 <input id="businessType" name="businessType"  type="hidden">
					</select>
                </td>
                <td class='f_td'>
                    <span id='tag_busDetails'>&nbsp;<span style="color:Red">*</span>业务细分</span>：
                </td>
                <td  style="text-align:left;">
                     <select id="busDetails" name="busDetails"  onchange="setLevel()" class="form-control">
                        <option value="">请选择</option>
                     </select>
                     <input id="businessDetail" name="businessDetail" type="hidden">
                </td>
            </tr>
           <input type = "hidden"  id="businessde" name="businessde" >
             <tr>
	            <td><span style="color:Red">*</span>用车事由：</td>
        		<td colspan='3' style='height:100px; text-align:left;' >
                    <textarea  maxlength="5000"  name="content" id="content" rows="3" cols="28" class="text0"></textarea>
                    <label id="recordNum" style="display: none;text-align:right;" class="control-label col-md-12"></label>
                </td>
        	</tr>
            <tr>
               	<td>
                     <span style="color:Red">*</span><span>时间</span>：
                 </td>
                 <td colspan="2">
                 	<input autocomplete="off" placeholder="请选择开始时间" type="text" id="startDate" name="startDate" onclick="WdatePicker({maxDate:'#F{$dp.$D(\'endDate\')}',dateFmt:'yyyy-MM-dd HH:mm'})" class="Wdate"  onchange="checkField()" style="width:160px;background-color:#eee;padding-left:10px;"/>
                        &emsp;<span>至</span>&emsp; 
                    <input autocomplete="off" placeholder="请选择结束时间" type="text" id="endDate" name="endDate" onclick="WdatePicker({minDate:'#F{$dp.$D(\'startDate\')}',dateFmt:'yyyy-MM-dd HH:mm'})" class="Wdate"  onchange="checkField()"  style="width:160px;background-color:#eee;padding-left:10px;"/>
                 </td>
                 <td id="totalTdId">
                 	<span >共</span>
                 	<input autocomplete="off" style="padding-left:10px;width:120px;background:#eee;" type="text" id="totalTime" name="totalTime" onkeyup="checkedTime(this)" onafterpaste="checkedTime(this)" readonly>
                 	<span >时</span>
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
	        <button id="confirmStartProcess" class="btn btn-default" type="button" onclick="startProcessInstance()">提交</button>
	    	<button type="button" class="btn btn-default" onclick="javascript:history.back();">返回<tton>
    	</div>
    	</div>
	   </div>

	<input type='text' style='display:none'/>
	<%@include file="/common/selectPosition.jsp" %>
	
	
</form>
</body>

 

<script>


function getBusinessDetail() {
   		//根据用户选择的业务类型，到数据库中取业务细分
   		var  myselect=document.getElementById("busType");
   		var index=myselect.selectedIndex ;  
   		var bt=myselect.options[index].value;
   		var t=myselect.options[index].text;
   		$("#businessType").val(t);
   		// alert(JSON.stringify(myselect.options[index].value));
   		
   		var url = document.getElementById('r').value;
   		var userId =document.getElementById('userId').value;
   		
   		var categoryId=$("#categoryId").val();
   		$.getJSON('${tenantPrefix}/rs/business/post_details', 
 	    			{ bt:bt,userId:userId,url:url,categoryId:categoryId},    
 	    			function(data) {
 	    		var option = "<option value=''>请选择</option>" ;  
 	        	for (var prop in data[0]) {
 	        		//alert(JSON.stringify(data[i])); 
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
    		//alert(t);
    		$("#businessDetail").val(t);
    		$("#businessDetailId").val(bd);

    		checkPostion(conf);
    		
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
    			})
    		}
    	}

 </script>
</html>

