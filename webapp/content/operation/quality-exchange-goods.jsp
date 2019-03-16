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
			 
			 
			//下拉框联动：先到数据库中取业务类型
	    	/* var url = document.getElementById('r').value;
	    	var userId =document.getElementById('userId').value;
	    	
	    	$.getJSON('${tenantPrefix}/rs/business/post_types', {userId:userId,url:url}, 
	    			function(data) {
	    		var option = "<option value=''>请选择</option>" ;  
	        	for (var i = 0; i < data.length; i++) {
	        		//alert(JSON.stringify(data[i])); 
	                option += "<option value='"+ data[i].id+"'>"+ data[i].name+"</option>"  
	 			  }
	  			 $("#busType").html(option);//将循环拼接的字符串插入第二个下拉列表  
	  			}); */
	    	getSystemName();
		})
		
		ROOT_URL = '${tenantPrefix}';
		taskOperation = new TaskOperation();
		var conf={
				applyCodeId:"applyCode",				//受理单号input的ID
			    submitBtnId:"confirmStartProcess",		//提交按钮ID	
				checkApplyCodeUrl:"${tenantPrefix}/rs/business/applyCodeIfExist",	//验证受理单号url
       			checkUrl:"${tenantPrefix}/rs/customer/opteraion-getposition",		//获取岗位url
       			actionUrl:"${tenantPrefix}/QualityExchange/QualityExchange-startProcessInstance.do",//提交URL
       			businessDetailId:"businessDetailId",			//存储业务明细input的ID
       			formId:"xform",									//form的ID
       			selectAreaId:"area",
   	   			selectCompanyId:"branchOffice",
   	 			iptAreaId:"areaId",
   	 			iptAreaName:"areaName",
   	   	   		iptCompanyId:"companyId",
   	   	   		iptCompanyName:"companyName"
       		}
// 		$(function(){
// 			checkPostion(conf);
// 		})
		function startProcessInstance() {

			
			
			//经销商编号 为空  不允许提交
			if ( document.getElementById('ucode').value ==""){
				alert("请输入经销商编号！");
				return false;
			}
			   
			var id = $("#ucode").val();
			//若输入的不是11位手机号,各个项都是系统自动返回，验证经销商编号填的是否正确
	    	if (id !=""&& id.length!=11) {
				//经销商编号不正确  不允许提交
				if ( document.getElementById('name').value =="" || document.getElementById('name').value =="null"){
					alert("经销商编号不正确！");
					return false;
				}
	    	}
	    	
	    	//若输入的是11位手机号，各个项用户都必须填
	    	if (id !=""&& id.length==11) {
				//经销商姓名 必填
				if ( document.getElementById('name').value =="" || document.getElementById('name').value =="null"){
					alert("请输入经销商姓名！");
					return false;
				}
				if ( document.getElementById('welfare').value =="" ){
					alert("请输入福利级别！");
					return false;
				}
				if ( document.getElementById('level').value =="" ){
					alert("请输入级别！");
					return false;
				}
				if ( document.getElementById('varFather').value =="" ){
					alert("请输入销售人！");
					return false;
				}
				if ( document.getElementById('varRe').value =="" ){
					alert("请输入服务人！");
					return false;
				}
				if ( document.getElementById('addTime').value =="" ){
					alert("请选择注册时间！");
					return false;
				}
				if ( document.getElementById('tel').value =="" ){
					alert("请输入电话！");
					return false;
				}
				if ( document.getElementById('address').value =="" ){
					alert("请输入地址！");
					return false;
				}
	    	}

			var num = parseInt($("#hidNum").val());
			for(var i=1;i<num;i++)
				{
				if(document.getElementById('qualityName'+i)!=undefined ){
				
					//产品名称必填
					if ( document.getElementById('qualityName'+i).value ==""){
						alert("请填写产品名称"+i);
						return false;
					}
					
					//数量必填
					if (document.getElementById('qualityNum'+i).value ==""){
						alert("请填写数量"+i);
						return false;
					}
				
					if ( document.getElementById('manuTime'+i).value ==""){
						alert("请填写日期");
						return false;
					}
				
				}
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
			
			//产品问题不能是空
			if ( document.getElementById('exchangeReason').value ==""){
				alert("请填写产品问题！");
				return false;
			}
			
			fnFormSubmit(conf);
		}
		
		
// 		function MaxWords(obj){
//     		document.getElementById("recordNum").style.display = "block";
//            var text1 = document.getElementById("applyContent").value;
//            var len;//记录已输入字数
//            if (text1.length >= 5000) {
//                document.getElementById("applyContent").value = text1.substr(0, 5000);
//                len = 5000;
//             }
//            else {
//                len = text1.length;
//            }
//            var show = len + " / 5000";
//            document.getElementById("recordNum").innerText = show;
// 	     }
		
		
    </script>
    
</head>
<style type="text/css">
         #tb1 td{border:1px solid #BBB;white-space: nowrap; font-size:10pt; }
        .f_td{ width:120px; font-size:12px;white-space:nowrap ;font-size:10pt;}
        .f_r_td{ width:130px; text-align:left;font-size:10pt;}  
        #tb1 tr td input{border: navajowhite;padding-left:5px;   font-size:10pt;}   
        #tb1 tr td{text-align:center;font-size:10pt;}  
          #trAddAfter tr td{text-align:center;font-size:10pt;}
    </style>
<body>
 <%@include file="/header/bpm-workspace3.jsp"%>
    <form id="xform" method="post"   action="${tenantPrefix}/QualityExchange/QualityExchange-startProcessInstance.do" class="xf-form" enctype="multipart/form-data">
			质量问题换货申请单
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
		<input id="businessDetailId" type="hidden" name="businessDetailId" value="${businessDetailId}">
		<input id="autoCompleteFirstTask" type="hidden" name="autoCompleteFirstTask" value="false">
		<input id="businessKey" type="hidden" name="businessKey" value="${businessKey}">
    	<input id="url" type="hidden" name="url" value="/QualityExchange/quality-exchange-detail.do">
    	<input id="areaId" type="hidden" name="areaId" value="${areaId}">
<%--     	<input id="areaName" type="hidden" name="areaName" value="<tags:party partyId='${areaId}'/>"> --%>
    	<input id="companyId" type="hidden" name="companyId">
		<input id="companyName" type="hidden" name="companyName">
		<input id="r" type="hidden"  name="r" value="${url}">
		<input id="userId" type="hidden" name="userId" value="${userId}">
		
    	
    	<table id="tb1" >
    		 <tr>
                <td colspan='8' align='center' class='f_td'><h2>质量问题换货申请单</h2></td>
                </td>
            </tr>
             <tr>
<!--                 <td colspan='2'  class='f_td' align='right' style='padding-right:20px;'> -->
<!--                     	提交次数 -->
<!--                  </td> -->
<!--                  <td colspan='2'  class='f_td' align='right' style='padding-right:20px;'> -->
<!--                     	0 -->
<!--                  </td> -->
                <td colspan='4'  class='f_td' align='right' style='padding-right:20px;'>
                    	受理单编号：
                </td>
                <td colspan='4'  class='f_td' align='right' style='padding-right:20px;'>
                    	<input id="applyCode" name="applyCode" value="${code}" readonly>
                </td>
            </tr>
         
             
 			<tr>
         	  	<td >
                	<span id='tag_userID'>&nbsp;<span style="color:Red">*</span>经销商编号/手机号</span>：
                </td>
                <td >
                    <input id ="ucode" name="ucode" type="text" maxlength="15"  onblur=fnUserData();  />
                </td>
                <td>
                    <span id='tag_realName'>&nbsp;经销商姓名</span>：
                </td>
                <td >
                	<input name="name" id="name" maxlength="10"> 
                </td>
               <td class='f_td'>
                    <span id='tag_welfare'>&nbsp;福利级别</span>：
                </td>
                <td >
                    <input name="welfare" id="welfare" maxlength="10"> 
                </td>
                <td class='f_td'>
                    <span id='tag_level'>&nbsp;级别</span>：
                </td>
                <td >
                    <input name="level" id="level" maxlength="10" >
                </td>
            </tr>
           
           
            <tr>
                <td >
                    <span id='tag_system'>&nbsp;<span style="color:Red">*</span>所属体系</span>：
                </td>
                <td style="text-align:left;">
                    <select name="system" id="system" onchange="getSystemName()" style="width:136px;">
						<c:forEach items="${systemlist}" var="item">
		  					<option value="${item.value}" >${item.name}</option>
		  				</c:forEach>			
					</select>
					<input id="systemName" name="systemName" type="hidden"  >
                </td>
                <td >
                    <span id='tag_seller'>&nbsp;销售人</span>：
                </td>
                <td >
                    <input name="varFather" id="varFather"  maxlength="30">
                </td>
                <td >
                    <span id='tag_service'>&nbsp;服务人</span>：  
                </td>
                <td >
                     <input name="varRe" id="varRe"   maxlength="30" > 
                </td>
                <td >
                    <span id='tag_addTime'>&nbsp;注册时间</span>：
                </td>
<!--                 <td > -->
<!--                      <input name="addTime" id="addTime"   >  -->
<!--                 </td> -->
                
                	<td >
             
                	 <div class="col-sm-8">
                	 <div id="pickerStartTime" class="input-group date"> 
                		<input type='text' id='addTime' name='addTime' onClick='WdatePicker()'    
                			readonly style="background-color:white;cursor:default;" class="Wdate">
             		 </div>  
             		 </td> 
            </tr>
           
           
            <tr>
                <td  style="white-space:nowrap">
                    <span id='tag_bustype'>&nbsp;<span style="color:Red">*</span>申请业务类型</span>：
                </td>
                <td style="text-align:left;">
                    <select id="busType" name="busType"  onchange="getBusinessDetail()">
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
                <td colspan='8' style="text-align:left;">
                     <select id="busDetails" name="busDetails"  onchange="setLevel()">
                        <option value="">请选择</option>
                     </select>
                     <input id="businessDetail" name="businessDetail" type="hidden">
                </td>
            </tr>
           <input type = "hidden"  id="businessde" name="businessde" >
           
           
           
           <tr>
                <td class='f_td'>
                    <span id='Span1'>&nbsp;联系电话</span>：
                </td>
                <td >
                     <input name="tel" id="tel" maxlength="11"> 
                </td>   
                <td width='100px'  class='f_td'>
                    <span id='Span2'>&nbsp;联系地址</span>：
                </td>
                <td colspan='8' width='100px'>
                    <input name="address" id="address" style="width: 100%; height: 100%"  maxlength="1000"> 
                </td>
            </tr>
            
            
            
            
             <tr>
                <td >
                    <span id='tag_sLevel'>&nbsp;<span style="color:Red">*</span>业务级别</span>：
                </td>
                <td style="text-align:left;">
                    <select name="busLevel" id="busLevel" >
					</select>
                    <input type="hidden"  name="businessLevel" id="businessLevel" />
                </td>   
                <td width='100px'  class='f_td'>
                    <span id='tag_belongs'>&nbsp;<span style="color:Red">*</span>所属大区：</span>
                </td>
                <td colspan='8' class='f_r_td' style="text-align:left;">
                     <%-- <tags:party partyId='${areaId}'/> --%>
                     <input id="areaName" type="text" name="areaName" value="<tags:party partyId='${areaId}'/>"  readonly> 
                 </td>
             </tr>
            
           

            <tr id="trAddAfter">
            
	            <td >产品问题:</td>
<!-- 	            <td  colspan="3"><input type="text" id="cancelRemark" name="cancelRemark" maxlength="500"  size="60" /></td> -->
        		
        		<td colspan='8' style='height:100px; text-align:left;' >
                    <textarea  maxlength="500"  name="exchangeReason" id="exchangeReason" rows="3" cols="28" class="text0" style="height:99px;width:99%;padding-left:10px;padding-top:10px" ></textarea>
                    <label id="recordNum" style="display: none;text-align:right;" class="control-label col-md-12"></label>
                </td>
        	
        	</tr>
            </table>
      
             <div >
                <label class="control-label col-md-2" name="fileName">添加附件：</label>
                <div >
                      <%@include file="/common/_uploadFile.jsp" %>
                      <span style="color:gray;"> 请添加共小于200M的附件 </span>
                </div>
        	 </div>
            
       </section>
	<!-- end of main -->
  </div> 
	 	<br/>
    	<br/>
     	<br/>
     	
		<div class="navbar navbar-default navbar-fixed-bottom">
	    <div class="text-center" style="padding-top:8px;">
		<div class="text-center" style="padding-top:8px;">
			    
	        <button id="addStartProcess" class="btn btn-default" type="button" onclick="addTr();">添加</button>
	        <button id="confirmStartProcess" class="btn btn-default" type="button" onclick="startProcessInstance()">提交</button>
	    	<button type="button" class="btn btn-default" onclick="javascript:history.back();">返回<tton>
    	</div>
    	</div>
	   </div>

	<input type='text' style='display:none'/>
	<%@include file="/common/selectPosition.jsp" %>
	
	<input  id="hidNum" name="hidNum"  value="1"  type="hidden"/>
 	<input   id="hidTotal" name="hidTotal"   value="0"  type="hidden"/>
	
</form>
</body>

 

<script>

$(function () {
	
	if (parseInt($("#hidTotal").val()) <= 0) {
         addTr();
    };
    
});
//添加列
function addTr() {
    	var num = parseInt($("#hidNum").val());
    	var remove = "";
     	if(num != 1){
    		remove = "[删除]";
     	}
        var total = parseInt($("#hidTotal").val());
        if (total < 20) {
        	 var html = "<tr id='tr" + num + "_1'><td >质量产品名称" + num + "</td><td ><input type='text' id='qualityName" + num + "' name='qualityName" + num + "'  maxlength='50'  /></td>";
             html = html + "<td >产品数量</td><td ><input type='text' id='qualityNum" + num + "' name='qualityNum" + num + "'  maxlength='10' onkeyup='qualityNumVerify(this.id)' /></td>";
             html = html + "<td colspan='1' style='padding-left:5px;'>产品生产日期</td><td  colspan='2' >";
             
                	html += ' <div class="col-sm-8">'  
                	html +=  '<div id="pickerStartTime" class="input-group date">'  
                	html +=  "<input type='text' id='manuTime" + num + "' name='manuTime" + num + "' onClick='WdatePicker()' " ;  
                	
                	html +=  ' readonly style="background-color:white;cursor:default;"'  
                	html +=  ' class="Wdate">'
             
              	html +=  " </div>  </td> ";
             
             
             
             html = html + "<td ><a href='javascript:;' onclick='removeTr(" + num + ");'>"+remove+"</a></td></tr>";
          
             $(html).insertBefore("#trAddAfter");
             
		$("#hidNum").val(num + 1);
        $("#hidTotal").val(total + 1);

        }
    }
//移除添加列
function removeTr(num) {
    var total = parseInt($("#hidTotal").val());      
    var hidNum   = parseInt($("#hidNum").val());
    var trid = "#tr" + num + "_1,#tr" + num+ "_2";
    $(trid).remove();
    $("#hidTotal").val(total - 1);
   
}
	 
 function getSystemName() {
	var  myselect=document.getElementById("system");
	var index=myselect.selectedIndex ;  
	// var bt=myselect.options[index].value;
	var text=myselect.options[index].text;
	$("#systemName").val(text);
 }
 
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
    		
    		$.getJSON('${tenantPrefix}/rs/business/post_details', 
  	    			{ bt:bt,userId:userId,url:url},    
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
    		$.getJSON('${tenantPrefix}/rs/business/level', { bd:bd}, 
    				function(data) {
	        		var option = "" ;  
	            	for (var i = 0; i < data.length; i++) {

                    option += "<option value='"+ data[i].id+"'>"+ data[i].level+"</option>"  
                    
                    $("#standard").text(data[i].standFirst);
                    $("#standard2").text(data[i].standSecond);
                    $("#businessStand1").val(data[i].standFirst);
                    $("#businessStand2").val(data[i].standSecond);
                    //alert(JSON.stringify(data));
                  }
      			 $("#busLevel").html(option);//将循环拼接的字符串插入第二个下拉列表  
      			var  myselect=document.getElementById("busLevel");
        		var index=myselect.selectedIndex ;  
        		// var bd=myselect.options[index].value;
        		var t=myselect.options[index].text;
      			$("#businessLevel").val(t);
      		 });
    		
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
    //调用接口，根据经销商编号，获取直销oa上存的对应信息：姓名 电话 等
    // 移开鼠标 触发
    function fnUserData() {
    	var id = $("#ucode").val();
		//alert(id.length);
    	if (id !=""&& id.length!=11) {
            $.getJSON('${tenantPrefix}/rs/varUser/userInfo', {
            	customerInfoId: id
            }, function(data) {
            	//alert(JSON.stringify(data)); 
            	
            	if(data.name=="null"||data.name==""||data.name==null){
            		$("#name").val('');
            	}else{
      			$("#name").val(''+''+data.name);}
            	
      			$("#welfare").val(data.rank);
  				$("#level").val(data.level);
				
				$("#varFather").val(data.father);
      			
      			$("#varRe").val(data.re);
      			
      			$("#addTime").val(data.addTime);
      			
      			$("#tel").val(data.mobile);
      			
      			$("#address").val(data.address);     			
      		});
        } 
    	
    	if (id !=""&& id.length==11) {
            	$("#name").val('');
            	
      			$("#welfare").val('');
  				$("#level").val('');
				
				$("#varFather").val('');
      			
      			$("#varRe").val('');
      			
      			$("#addTime").val('');
      			
      			$("#tel").val('');
      			
      			$("#address").val('');     			
      		
        }
    }
	
    
    
    function qualityNumVerify(id){
		var value = $("#"+id).val();
		if(value == ""){
			$("#"+id).val(0);
			value=0;
		}
		if(value > 50000){
			$("#"+id).val(50000);
		}
		value = $("#"+id).val();
		var reg = /^([1-9][0-9]*){1,3}$/;
	
		if(value.match(reg)==null){
			$("#"+id).val("");
		
		}
	
		
		
	}
    
    
 </script>
</html>

