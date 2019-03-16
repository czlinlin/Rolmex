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
		
			 //用户输入店编号 回车 触发，取店姓名和电话
			 $("#shopCode").keydown(function(event) {    
					if (event.keyCode == 13) {  
						var id = $("#shopCode").val();
						//alert(id);
	                	if (id !="") {
	                		$.getJSON('${tenantPrefix}/rs/varUser/userInfo', {
	                        	customerInfoId: id
	                        }, function(data) {
	                        	//alert(JSON.stringify(data)); 
	                			$("#shopName").val(''+''+data.name);
	                  			$("#shopMobile").val(data.mobile);
	                  		});
	                    } else {
	                    	alert("必须输入客户编号!");
	                    }
	                }    
	            });	 
		 
		 })
		
		ROOT_URL = '${tenantPrefix}';
		taskOperation = new TaskOperation();
		var conf={
				applyCodeId:"applyCode",				//受理单号input的ID
			    submitBtnId:"confirmStartProcess",		//提交按钮ID	
				checkApplyCodeUrl:"${tenantPrefix}/rs/business/applyCodeIfExist",	//验证受理单号url
       			checkUrl:"${tenantPrefix}/rs/customer/opteraion-getposition",		//获取岗位url
       			actionUrl:"${tenantPrefix}/operationCancelOrder/process-operationCancelOrder-startProcessInstance.do",//提交URL
       			businessDetailId:"businessDetailId",			//存储业务明细input的ID
       			formId:"xform",									//form的ID
       			selectAreaId:"area",
   	   			selectCompanyId:"branchOffice",
   	 			iptAreaId:"areaId",
   	 			iptAreaName:"areaName",
   	   	   		iptCompanyId:"companyId",
   	   	   		iptCompanyName:"companyName"
       		}
		$(function(){
			checkPostion(conf);
		})
		function startProcessInstance() {

			//店编号 为空  不允许提交
			if ( document.getElementById('shopCode').value ==""){
				alert("请输入店编号！");
				return false;
			}
			   
			//店编号 不正确提交
			if ( document.getElementById('shopMobile').value ==""){
				alert("店编号不正确！");
				return false;
			}
			
			//来电电话 为空  不允许提交
			if ( document.getElementById('mobile').value ==""){
				alert("请输入来电电话！");
				return false;
			}
			
			//没有输入撤单编号  不允许提交
			if ( document.getElementById('ucode1').value ==""){
				alert("请至少输入一条撤单编号！");
				return false;
			}
	
		//判断核实选择的"是" 才能提交
			var flag = 0;
			var radionum = document.getElementById("xform").isChecked;
			 for(var i=0;i<radionum.length;i++){
				if(radionum[i].checked){
					flag = flag+1;
				if(radionum[i].value=="否" ){
					alert("是核实才能提交！");
					return false;
					}
			  	}
			  }
		 	if(flag==0 ){
					alert("是核实才能提交！");
					return false;
					} 
			 
			var num = parseInt($("#hidNum").val());
			for(var i=1;i<num;i++)
				{
				if(document.getElementById('ucode'+i)!=undefined ){
				
					//撤单编号必填
					if ( document.getElementById('ucode'+i).value ==""){
						alert("请填写撤单编号"+i);
						return false;
					}
					
					//撤单编号 不正确 不允许提交
					if (document.getElementById('userName'+i).value ==""){
						alert("撤单编号"+i+"不正确！");
						return false;
					}
				
					//撤单类型必须选择
					if ( document.getElementById('cancelType'+i).value ==""||document.getElementById('cancelType'+i).value =="请选择"){
						alert("请选择撤单类型！");
						return false;
					}
				
					//若“撤单类型”为“零售业绩单”，则必须输入“撤单金额”
					if ( document.getElementById('cancelType'+i).value =="零售业绩单"&&document.getElementById('cancelMoney'+i).value == ""){
						alert("请填写撤单编号"+i+"的撤单金额！");
						return false;
					}
					//若“撤单类型”为“零售业绩单(补)”，则必须输入“撤单金额”和“业绩单号”
					if ( document.getElementById('cancelType'+i).value =="零售业绩单(补)"&&(document.getElementById('cancelMoney'+i).value == "" || document.getElementById('saleId'+i).value == "")){
						alert("请填写撤单编号"+i+"的撤单金额和业绩单号！");
						return false;
					}
				}
				}
						
			
			fnFormSubmit(conf);
		}
    </script>
    
</head>
<style type="text/css">
         #tb1 td{border:1px solid #BBB }
        .f_td{ width:120px; font-size:12px;white-space:nowrap }
        .f_r_td{ width:130px; text-align:left;}  
        #tb1 tr td input{border: navajowhite;}   
        #tb1 tr td{text-align:center;}  
    </style>
<body>
 <%@include file="/header/bpm-workspace3.jsp"%>
    <form id="xform" method="post"   action="${tenantPrefix}/operationApply/process-operationApply-startProcessInstance.do" class="xf-form" enctype="multipart/form-data">
			常规/非常规业务	
    <br />

    <div class="container" style="width:980px;">
    		
     <section id="m-main" class="col-md-12" style="padding-top:65px;padding-bottom:20px">

		<input id="processDefinitionId" type="hidden" name="processDefinitionId" value="${processDefinitionId}">
		<input id="bpmProcessId" type="hidden"  name="bpmProcessId" value="${bpmProcessId}">
		<input id="businessDetailId" type="hidden" name="businessDetailId" value="${businessDetailId}">
		<input id="autoCompleteFirstTask" type="hidden" name="autoCompleteFirstTask" value="false">
		<input id="businessKey" type="hidden" name="businessKey" value="${businessKey}">
    	<input id="url" type="hidden" name="url" value="/operationCancelOrder/cancelOrder-detail.do">
    	<input id="areaId" type="hidden" name="areaId" value="${areaId}">
    	<input id="areaName" type="hidden" name="areaName" value="<tags:party partyId='${areaId}'/>">
    	<input id="companyId" type="hidden" name="companyId">
		<input id="companyName" type="hidden" name="companyName">
    	
    	<table id="tb1" >
    		 <tr>
                <td colspan='6' align='center' class='f_td'><h2>撤单登记表</h2></td>
                </td>
            </tr>
            <tr>
                <td colspan='2'  class='f_td' align='right' style='padding-right:20px;'>
                    	提交次数：0
                 </td>
                 <td colspan='1'  class='f_td' align='right' style='padding-right:20px;'>
                    	受理单编号：
                </td>
                <td colspan='3'  class='f_td' align='right' style='padding-right:20px;'>
                    	<input id="applyCode" name="applyCode" value="${code}"  readonly>
                </td>
            </tr>
         
             
 			 <tr>
         	    <td ><span id='tag_shopCode'>&nbsp;店编号</span>：</td>
                <td ><input id ="shopCode" name="ucode" type="text" maxlength="8"  onblur=fnshopData();  /></td>
                <td ><span id='tag_shopName'>&nbsp;店姓名</span>：</td>
                <td ><input name="shopName" id="shopName" readonly></td>
                <td ><span id='tag_shopMobile'>&nbsp;店电话</span>：</td>
                <td ><input name="shopMobile" id="shopMobile" readonly></td>
            </tr>
            <tr>
         	    <td ><span id='tag_mobile'>&nbsp;来电电话</span>：</td>
                <td ><input id ="mobile" name="mobile" type="text" maxlength="11"/></td>
                
                 <jsp:useBean id="time" class="java.util.Date"/>  
       			<td ><span id='tag_registerTime'>&nbsp;撤单登记时间</span>：</td>
					<%   
					     java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("yyyy-MM-dd"); 
					    java.util.Date currentTime = new java.util.Date();//得到当前系统时间 
					    String str_date1 = formatter.format(currentTime); //将日期时间格式化 
					%>
                <td ><input name="registerTime" id="registerTime" value="<%=str_date1%>" readonly > </td>
                <td ><span id='tag_registerName'>&nbsp;登记人</span>：</td>
                <td ><input name="registerName" id="registerName" value="<tags:user userId="<%=userId%>"/>" readonly ></td>
             </tr>
            <tr id="trAddAfter">
            <td >是否核实</td>
	            <td ><label><input type="radio" id='onChecked' name='isChecked' value='是'  />是</label>
	            	 <label><input type="radio" id='noChecked' name='isChecked' value='否'  />否</label>
	            </td>
	            <td >撤单备注</td>
	            <td  colspan="3"><input type="text" id="cancelRemark" name="cancelRemark" maxlength="500"  size="60" /></td>
        	</tr>
            </table>
       </section>
	<!-- end of main -->

	<div class="" style="margin-top:10px;text-align:center;">
        <p style="color:Red; font-weight:bold;margin-bottom:30px">
        </br></br></br>
            注：若“撤单类型”为“零售业绩单”，则必须输入“撤单金额”！<br />若“撤单类型”为“零售业绩单(补)”，则必须输入“撤单金额”和“业绩单号”！
        </p>
        <button id="addStartProcess" class="btn btn-default" type="button" onclick="addTr();">添加</button>
        <button id="confirmStartProcess" class="btn btn-default" type="button" onclick="startProcessInstance()">提交</button>
    	<button type="button" class="btn btn-default" onclick="javascript:history.back();">返回<tton>
    </div>

	<input type='text' style='display:none'/>
	<%@include file="/common/selectPosition.jsp" %>
</form>
</body>

 <input  id="hidNum" name="hidNum" type="hidden" value="1" />
 <input   id="hidTotal" name="hidTotal" type="hidden"  value="0" />

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
        	 var html = "<tr id='tr" + num + "_1'><td class='f_td'>撤单编号" + num + "</td><td class='f_r_td'><input type='text' id='ucode" + num + "' name='ucode" + num + "' maxlength='8' class='text0' value='' onblur='fnUserData(" + num + ");' /></td>";
             html = html + "<td class='f_td'>撤单姓名</td><td class='f_r_td'><input type='text' id='userName" + num + "' name='userName" + num + "' class='text0' value='' readonly/></td>";
             html = html + "<td class='f_td'>编号加入日期</td><td class='f_r_td'><input type='text' id='addTime" + num + "' name='addTime" + num + "' class='text0' value='' readonly/></td>";
             html = html + "<td rowspan='2' style='position:absolute;margin-top: 15px;'><a href='javascript:;' onclick='removeTr(" + num + ");'>"+remove+"</a></td>";
             html = html + "</tr><tr id='tr" + num + "_2'><td class='f_td'>撤单类型</td><td class='f_r_td'><select id='cancelType" + num + "' name='cancelType" + num + "' ><option value='' selected>请选择</option><option value='销售业绩单'>销售业绩单</option><option value='零售业绩单'>零售业绩单</option><option value='零售业绩单(补)'>零售业绩单(补)</option><option value='升级单'>升级单</option></select></td>";
             html = html + "<td class='f_td'>撤单金额</td><td class='f_r_td'><input type='text' id='cancelMoney" + num + "' name='cancelMoney" + num + "' maxlength='8' class='text0' value=''  /></td>";
             html = html + "<td class='f_td'>业绩单号</td><td class='f_r_td'><input type='text' id='saleId" + num + "' name='saleId" + num + "' maxlength='20' class='text0' value='' /></td></tr>";

             $(html).insertBefore("#trAddAfter");
             
		$("#hidNum").val(num + 1);
        $("#hidTotal").val(total + 1);

        }else {
            alert("最多添加20条撤单编号！");
            return;
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

//光标移开 后  加载撤单编号的姓名和加入日期
function fnUserData(num) {
	var code = "#ucode" + num;
    var id = $(code).val();
 
    if (id !="") {
	    	$.getJSON('${tenantPrefix}/rs/varUser/userInfo', {
	        	customerInfoId: id
	        }, function(data) {
	        	//alert(JSON.stringify(data)); 
				$("#userName"+ num ).val(data.name);
	  			$("#addTime"+ num ).val(data.addTime);
	  		});
       }
}
//调用接口，根据经销商编号，获取直销oa上存的对应信息：姓名 电话 等
////用户输入店编号 移开鼠标 触发，取店姓名和电话
function fnshopData() {
	
	var id = $("#shopCode").val();
	
	if (id !="") {
        $.getJSON('${tenantPrefix}/rs/varUser/userInfo', {
        	customerInfoId: id
        }, function(data) {
        	//alert(JSON.stringify(data)); 
			$("#shopName").val(''+''+data.name);
  			$("#shopMobile").val(data.mobile);
  		});
    } 
}


</script>


</html>

