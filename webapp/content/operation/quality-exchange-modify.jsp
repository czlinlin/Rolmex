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
		
		function CompleteTask(flag) {



			
			
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
			
			
			
			
			var num = parseInt($("#hidTotal").val());
		
			for(var i=1;i<=num;i++) {
				
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
			
			//产品问题不能是空
			if ( document.getElementById('exchangeReason').value ==""){
				alert("请填写产品问题！");
				return false;
			}
			
			
			
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
	    			
	    			$('#xform').attr('action', '${tenantPrefix}/QualityExchange/ExchangeApproval-completeTask.do?flag=' + flag);
	    			$('#xform').submit();
	            },      
	            error: function(e){      
	            	loading.modal('hide');
	                alert("服务器请求失败,请重试"); 
	            }
	       });
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
         #tb1 td{border:1px solid #BBB ;white-space:nowrap;font-size:10pt; }
        .f_td{ width:120px; font-size:12px;white-space:nowrap ; font-size:10pt;}
        .f_r_td{ width:130px; text-align:left;font-size:10pt;}  
        #tb1 tr td input{border: navajowhite; padding-left:5px;   font-size:10pt;}   
        #tb1 tr td{text-align:center; font-size:10pt;}  
         #trAddAfter tr td{text-align:center;font-size:10pt;}
    </style>
<body>
 <%@include file="/header/bpm-workspace3.jsp"%>
    <form id="xform" method="post"   action="${tenantPrefix}/QualityExchange/ExchangeApproval-completeTask.do" class="xf-form" enctype="multipart/form-data">
			质量问题换货调整单
    <br />

    <div class="container" >
    		
     <section id="m-main" class="col-md-12" style="padding-top:65px;padding-bottom:20px">
		
		  <table width="100%" cellspacing="0" cellpadding="0" border="0" align="center" class="xf-table">
			  <tbody>
			    <tr>
				  <td width="25%" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top xf-cell-left">
				    <label style="display:block;text-align:center;margin-bottom:0px;padding-top:10px;padding-bottom:10px;">审核环节&nbsp;</label>
				  </td>
				  <td width="75%" class="xf-cell xf-cell-right xf-cell-bottom xf-cell-top" colspan="8" rowspan="2">
				    <div id="nextStep"></div>
				  </td>
				</tr>
			  </tbody>
		</table>
		
		
		
		<input id="processInstanceId" type="hidden" name="processInstanceId" value="${processInstanceId}">
		<input id="humanTaskId" type="hidden"  name="humanTaskId" value="${humanTaskId}">
   		<input id="createTime"  type="hidden"  name="createTime" >
   		<input id="cancelOrderTotalID"  type="hidden"   name="cancelOrderTotalID" >
    	<input id="processDefinitionId" type="hidden" name="processDefinitionId" value="${processDefinitionId}">
		<input id="bpmProcessId" type="hidden"  name="bpmProcessId" value="${bpmProcessId}">
		<input id="autoCompleteFirstTask" type="hidden" name="autoCompleteFirstTask" value="false">
		<input id="businessKey" type="hidden" name="businessKey" value="${businessKey}">
    	<input id="userId" type="hidden" name="userId" value="<%=userId %>">
    	<input id="exchangeId" name="exchangeId" type="hidden">
    	<table id="tb1" >
    		<tr>
                <td colspan='8' align='center' class='f_td'><h2>质量问题换货调整单</h2></td>
            </tr>
            <tr>
<!--                 <td colspan='1'  class='f_td' align='right' style='padding-right:20px;'> -->
<!--                     	提交次数： -->
                    	
<!--                 </td> -->
<!--                 <td colspan='3' > -->
<!--                		<input id="submitTimes" name="submitTimes" readonly> -->
<!--                  </td> -->
                 <td colspan='3'  class='f_td' align='right' style='padding-right:20px;'>
                    	受理单编号：
                 </td>
                <td colspan='5' >
               		<input id="applyCode" name="applyCode" value="${code}" readonly>
                 </td>
            </tr>
 			
			
 			<tr>
         	  	<td >
                	<span id='tag_userID'>&nbsp;<span style="color:Red">*</span>经销商编号/手机号</span>：
                </td>
                <td >
                    <input id ="ucode" name="ucode" type="text"  maxlength="15"  onblur=fnUserData();  />
                </td>
                <td class='f_td'>
                    <span id='tag_realName'>&nbsp;经销商姓名</span>：
                </td>
                <td >
                	<input name="name" id="name"  maxlength="10"> 
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
                    <input name="level" id="level"  maxlength="10">
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
                     <input name="varRe" id="varRe"    maxlength="30"> 
                </td>
                <td >
                    <span id='tag_addTime' >&nbsp;注册时间</span>：
                </td>
<!--                 <td > -->
<!--                      <input name="addTime" id="addTime"   readonly>  -->
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
                    	<input id="busType" name="busType"  readonly>
				</td>
                <td class='f_td'>
                    <span id='tag_busDetails'>&nbsp;<span style="color:Red">*</span>业务细分</span>：
                </td>
                <td colspan='8' style="text-align:left;">
                    	<input id="busDetails" name="busDetails"  readonly>
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
                    <input   name="busLevel" id="busLevel" readonly/>
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
				<td colspan='8' style='height:100px; text-align:left;' >
                    <textarea  maxlength="500"  name="exchangeReason" id="exchangeReason" rows="3" cols="28" class="text0" style="height:99px;width:99%;padding-left:10px;padding-top:10px" ></textarea>
                    <label id="recordNum" style="display: none;text-align:right;" class="control-label col-md-12"></label>
                </td>
        	
        	</tr>
        	
            </table>
            
            
	  	 	<table style ="width:80%">
	  	 			<div class="form-group">
                        <label class="control-label col-md-2" name="fileName">原有附件</label>
                        <div class="col-md-8">
                            <%@include file="/common/show_edit_file.jsp" %>
                        </div>
                    </div>
			</table>
			<table style ="width:80%">
				<div class="form-group">
					<tr>
						<td>
	                        <label name="fileName">添加附件：</label>
		                        <div >
		                            <%@include file="/common/_uploadFile.jsp" %>
		                            <span style="color:gray;"> 请添加共小于200M的附件 </span>
		                        </div>
	                    </td>
				   </tr>
				 </div>
			</table>
            
       </section>
	<!-- end of main -->
	<div class="" style="margin-top:10px;text-align:center;">
        <p style="color:Red; font-weight:bold;margin-bottom:30px">
        </br></br></br>
               </p>
        <button id="confirmStartProcess" class="btn btn-default" type="button" onclick="addTr();">添加</button>
    </div>

<table id="myTable" width="100%" cellspacing="0" cellpadding="0" border="0" align="center" class="table table-border">
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
	<br/>
	<br/>
  </div> 
	  <div class="navbar navbar-default navbar-fixed-bottom">
	    	<div class="text-center" style="padding-top:8px;">
			    <div class="text-center" style="padding-top:8px;">
					<button id="completeTask1" type="button" class="btn btn-default" onclick="CompleteTask(3)">重新申请</button>
					<button id="completeTask2" type="button" class="btn btn-default" onclick="CompleteTask(4)">撤销申请</button>
					<button id="endProcess" type="button" class="btn btn-default" onclick="confirmOperation()">撤销申请</button>
					<button type="button" class="btn btn-default" onclick="javascript:history.back();">返回</button>
				</div>
			</div>
	   </div>

<input  id="hidTotal" name="hidTotal"   type="hidden"/>
<input  id="hidNum" name="hidNum"  type="hidden" />
</form>
</body>

 

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




//取原表单的数据显示出来
$(function () {

	 var hidTotal;
	 var hidNum;
	var id=${processInstanceId}; 

				if (id !="") {
	                $.getJSON('${tenantPrefix}/rs/QualityExchange/getQualityExchangeInfo', {
	                	id: id
	                }, function(data) {
	                	for (var i = 0; i < data.length; i++) {
	                		//alert(data[i].exchangeProductSub[0].productName); 
	                		$("#applyCode").val(data[i].applyCode);  
	                		
	                		$("#ucode").val(data[i].ucode);  
	                		$("#name").val(data[i].name);
	                		$("#welfare").val(data[i].welfare);  
	                		$("#level").val(data[i].level);
	                		
	                		var selectValue="";
                    		var optionArray=$("#system option");
                    		for(var m=0;m<optionArray.length;m++){
                    			
	                			if(data[i].system==$(optionArray[m]).html()){
	                				selectValue=$(optionArray[m]).attr("value");
	                			}
                			}
                			$("#system").val(selectValue);
                			$("#systemName").val(selectValue);
                			
	                		$("#varFather").val(data[i].varFather);
	                		$("#varRe").val(data[i].varRe);
	                		$("#addTime").val(data[i].addTime);
	                		$("#busType").val(data[i].businessType);
	                		$("#busDetails").val(data[i].businessDetail);
	                		$("#tel").val(data[i].tel);
	                		$("#address").val(data[i].address);
	                		$("#busLevel").val(data[i].businessLevel);
	                		$("#areaName").val(data[i].area);
	                		$("#exchangeReason").val(data[i].exchangeReason);
	                		$("#exchangeId").val(data[i].id);
	                	}
	  				});
	             getProductList();  
             
	         };
	});

function getProductList() {
	
	var id=${processInstanceId}; 
	
	  $.getJSON('${tenantPrefix}/rs/QualityExchange/getProductList', {
      	id: id
      }, function(data) {
      	for (var num=1  ;num<data.length+1 ;num++) {

      		$("#hidTotal").val(data.length); 
      		$("#hidNum").val(data.length); 
      	
      		var remove = "";
      		//画表单
				
					if(num != 1){
						remove = "[删除]";
					}
					var html = "<tr id='tr" + num + "_1'><td class='f_td'>质量产品名称" + num + "</td><td class='f_r_td'><input type='text' id='qualityName" + num + "' name='qualityName" + num + "'   class='text0' maxlength='50'   /></td>";
                      html = html + "<td >产品数量</td><td ><input type='text' id='qualityNum" + num + "' name='qualityNum" + num + "' colspan='1' onkeyup='qualityNumVerify(this.id)'/></td>";
                      html = html + "<td  colspan='1' style='padding-left:5px;'>产品生产日期</td><td colspan='2' >";

                  	html += ' <div class="col-sm-8">';  
                  	html +=  '<div id="pickerStartTime" class="input-group date">';  
                  	html +=  "<input  type='text' id='manuTime" + num + "' name='manuTime" + num + "' onClick='WdatePicker()' " ;  
                  	
                  	html +=  ' readonly style="background-color:white;cursor:default;"'  
                  	html +=  ' class="Wdate">'
               
                	html +=  " </div>  </td> ";
                      html = html + "<td   ><a href='javascript:;' onclick='removeTr(" + num + ");'>"+remove+"</a></td></tr>";
                 
                     $(html).insertBefore("#trAddAfter");
    		 
					$("#qualityName"+num+"").val(data[num-1].productName);
					$("#qualityNum"+num+"").val(data[num-1].productNum); 
					$("#manuTime"+num+"").val(data[num-1].productionDate); 
      		}

       });
}

//添加列
function addTr() {
    
    	var num = parseInt($("#hidNum").val());
    	num = num+1;
        var total = parseInt($("#hidNum").val());
    	
        	 var html = "<tr id='tr" + num + "_1'><td >质量产品名称" + num + "</td><td ><input type='text' id='qualityName" + num + "' name='qualityName" + num + "'  maxlength='50' /></td>";
             html = html + "<td >产品数量</td><td ><input type='text' id='qualityNum" + num + "' name='qualityNum" + num + "'  onkeyup='qualityNumVerify(this.id)'  /></td>";
             html = html + "<td style='padding-left:5px;'>产品生产日期</td><td colspan='2'>";
             

         	html += ' <div class="col-sm-8">'  
         	html +=  '<div id="pickerStartTime" class="input-group date">'  
         	html +=  "<input type='text' id='manuTime" + num + "' name='manuTime" + num + "' onClick='WdatePicker()' " ;  
         	
         	html +=  ' readonly style="background-color:white;cursor:default;"'  
         	html +=  ' class="Wdate">'
	      	html +=  " </div>  </td> ";
             
             html = html + "<td ><a href='javascript:;' onclick='removeTr(" + num + ");'>[删除]</a></td></tr>";
          
             $(html).insertBefore("#trAddAfter");
             
             $("#hidTotal").val(total + 1);
     		$("#hidNum").val(total + 1);
     }
    
//移除添加列
function removeTr(num) {
    var total = parseInt($("#hidTotal").val());            
    var trid = "#tr" + num + "_1,#tr" + num+ "_2";
    $(trid).remove();
    $("#hidTotal").val(total - 1);
}

//调用接口，根据经销商编号，获取直销oa上存的对应信息：姓名 电话 等
// 移开鼠标 触发
function fnUserData() {
	var id = $("#ucode").val();
	//alert(id);
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
// 	if(value == ""){
// 		$("#"+id).val(0);
// 		value=0;
// 	}
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

