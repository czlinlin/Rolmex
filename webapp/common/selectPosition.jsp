<%@page contentType="text/html;charset=UTF-8" %>
<div id="div_process_dialog">
	<input type="hidden" id="iptStartPositionId" name="iptStartPositionId">
</div>
<script>
var loading;
var fnLoading=function(conf){
	if(loading==undefined||loading==null){
		loading = bootbox.dialog({
            message: '<p style="width:90%;margin:0 auto;text-align:center;">提交中...</p>',
            size: 'small',
            closeButton: false
     	});
	}
	else
		loading.modal('show');
	
	$("#"+conf.submitBtnId).attr({"disabled":"disabled"});
}

var fnHideLoading=function(conf){
	loading.modal('hide');
	$("#"+conf.submitBtnId).removeAttr("disabled");
}

var fnFormSubmit=function(conf){
	if(!conf.txtPositionId)
		conf.txtPositionId="iptStartPositionId";
	var positionId=$("#"+conf.txtPositionId).val();
	if(positionId==""){
		alert("没有获取到您发起的岗位信息，请刷新页面重试！");
		return;
	}
	applyCodeIfExist(conf);
}

//判断受理单编号是否已存在,若不存在，返回当前受理单号，若存在，生成一个新的受理单号
function applyCodeIfExist(conf) {
	fnLoading(conf);	
	var applyCode = $("#"+conf.applyCodeId).val();
	$.ajax({      
        url: conf.checkApplyCodeUrl,      
        datatype: "json",
        data:{"applyCode": applyCode},
        type: 'get',      
        success: function (e) {
        	//成功后回调   
        	if (e == "-1") {
        		fnHideLoading(conf);
        		alert("单据号重复，请刷新页重新获取！");  
        	} else {
        		$("#"+conf.applyCodeId).val(e);
        		$('#'+conf.formId).attr('action',conf.actionUrl);
    			$('#'+conf.formId).submit();
        	}  
        },      
        error: function(e){      
        	fnHideLoading(conf);
        	//失败后回调      
            alert("服务器请求失败,请重试");  
            $("#confirmStartProcess").removeAttr("disabled");//将按钮可用               
        }
   }); 
}

function checkPostion(conf){
	if(!conf.txtPositionId)
		conf.txtPositionId="iptStartPositionId";
	var businessDetailId=$("#"+conf.businessDetailId).val();
	if(businessDetailId==""||businessDetailId=="0") return;
	$.ajax({      
        url: conf.checkUrl,      
        datatype: "json",
        data:{"businessDetailId": businessDetailId},
        type: 'get',      
        success: function (strReturnJson) {
        	if(strReturnJson==undefined||strReturnJson==null||strReturnJson==""){
        		//fnHideLoading(conf);
        		alert("获取岗位信息异常，请联系管理员！");
        		return;
        	}
        	if(strReturnJson.code!=200){
        		//fnHideLoading(conf);
        		alert(strReturnJson.message);
        		return;
        	}
        	
        	var jsonDataList=strReturnJson.data;
        	if(strReturnJson.message=="one"){
        		$("#"+conf.txtPositionId).val(jsonDataList[0].positionId);
        		if(conf.isBranchCompany){
        			if(conf.isBranchCompany=="0"){
        				return;
        			}
        		}
        		if($("#"+conf.selectAreaId).size()>0){
        			var html="<option value='"+jsonDataList[0].areaId+"'>"+jsonDataList[0].areaName+"</option>";
        			$("#"+conf.selectAreaId).html(html);
        		}
        		if($("#"+conf.selectCompanyId).size()>0){
        			var html="<option value='"+jsonDataList[0].companyId+"'>"+jsonDataList[0].companyName+"</option>";
        			$("#"+conf.selectCompanyId).html(html);
        		}
        		if($("#"+conf.iptAreaId).size()>0){
        			$("#"+conf.iptAreaId).val(jsonDataList[0].areaId);
        		}
        		if($("#"+conf.iptCompanyId).size()>0){
        			$("#"+conf.iptCompanyId).val(jsonDataList[0].companyId);
        		}
        		if($("#"+conf.iptAreaName).size()>0){
        			$("#"+conf.iptAreaName).val(jsonDataList[0].areaName);
        		}
        		if($("#"+conf.iptCompanyName).size()>0){
        			$("#"+conf.iptCompanyName).val(jsonDataList[0].companyName);
        		}
        		
        		//ckx 
        		if("business" == conf.formType){
            		replace(conf,jsonDataList[0].areaId,jsonDataList[0].areaName,jsonDataList[0].companyId,jsonDataList[0].companyName);
        		}        		
        		
        		
        	}
        	else{
        		//fnHideLoading(conf);
        		//弹框选择
        		fnSetPosition(conf,jsonDataList);
        	}
    		
        },      
        error: function(e){
        	//fnHideLoading(conf);
        	//失败后回调      
            alert("服务器请求失败,请重试");  
            $("#confirmStartProcess").removeAttr("disabled");//将按钮可用               
        }
   });
}

var fnSetPosition=function(conf,datalist){
		var html ="";
		html+='<table style="width:98%">';
		html+='<tr>'
		html+='<td>'
		html+='<div id="divMsg" style="margin:0 5px;color:red;"></div>'
		html+='</td>'
		html+='</tr>'
		html+='<tr>'
		html+='<td><div style="color:red;font-size:16px;">为保证流程的正确流转,请选择对应的流程发起岗位：</div></td>'
		html+='</tr>'
		html+='<tr>'
		html+='<td>';
		for(var n in datalist){
			html+='<div sytle="line-height:30px;">';
			html+=datalist[n].num+'.';
			html+='<input name="iptRadio" type="radio" value="'+datalist[n].positionId+'" ';
			html+='areaid="'+datalist[n].areaId+'" areaname="'+datalist[n].areaName+'" companyid="'+datalist[n].companyId+'" companyname="'+datalist[n].companyName+'"/>'
			html+=datalist[n].positionName;
			html+='</div>';
		}
		html+='</td>';
		html+='</tr>'
		html+='</table>'
		var dialogInput =bootbox.dialog({
           title: "发起流程选择岗位",
           message: html,
           closeButton: false,
           buttons: {
               noclose: {
                   label: '确定',
                   className: 'btn-primary',
                   callback: function () {
                   	   var rdCheck=$("input[name='iptRadio']:checked");
                   	   if(rdCheck.size()<1){
                   		   alert("请选择发起流程的岗位");
                		   return false;
                 		}
                   	   var selectPositionId=rdCheck.val();
                   	   $("#"+conf.txtPositionId).val(selectPositionId);
	                   	if(conf.isBranchCompany){
	            			if(conf.isBranchCompany=="0"){
	            				return;
	            			}
	            		}
	            		if($("#"+conf.selectAreaId).size()>0){
	            			var html="<option value='"+rdCheck.attr("areaid")+"'>"+rdCheck.attr("areaname")+"</option>";
	            			$("#"+conf.selectAreaId).html(html);
	            		}
	            		if($("#"+conf.selectCompanyId).size()>0){
	            			var html="<option value='"+rdCheck.attr("companyid")+"'>"+rdCheck.attr("companyname")+"</option>";
	            			$("#"+conf.selectCompanyId).html(html);
	            		}
	            		if($("#"+conf.iptAreaId).size()>0){
	            			$("#"+conf.iptAreaId).val(rdCheck.attr("areaid"));
	            		}
	            		if($("#"+conf.iptCompanyId).size()>0){
	            			$("#"+conf.iptCompanyId).val(rdCheck.attr("companyid"));
	            		}
	            		if($("#"+conf.iptAreaName).size()>0){
	            			$("#"+conf.iptAreaName).val(rdCheck.attr("areaname"));
	            		}
	            		if($("#"+conf.iptCompanyName).size()>0){
	            			$("#"+conf.iptCompanyName).val(rdCheck.attr("companyname"));
	            		}
	            		//ckx 
	            		if("business" == conf.formType){
		            		replace(conf,rdCheck.attr("areaid"),rdCheck.attr("areaname"),rdCheck.attr("companyid"),rdCheck.attr("companyname"));
	            		}
                       return true;
                   }
               }
           },
           callback: function (result) {
               alert(result);
               return;
           },
           show: true
       });
}

//根据业务细分来显示对应文字
function replace(conf,areaId,areaName,companyId,companyName){
	 var businessDetailId=$("#businessDetailId").val();
	 $.ajax({      
	        url: conf.checkBusinessDetailUrl,      
	        datatype: "json",
	        data:{businessDetailId:businessDetailId},
	        type: 'get',      
	        success: function (data) {
	        	if("true"==data){
	        		$("#hId").html("启明项目申请单");
	        		$("#areaLabelId").html('<font color="red">*</font>&nbsp;属地区域：');
	      		  	$("#branchOfficeLabelId").html('<font color="red">*</font>&nbsp;属地项目部：');
	      		  
	      		  	if($("#"+conf.selectAreaId).size()>0){
	        			var html="<option value='"+areaId+"'>"+areaName+"</option>";
	        			$("#"+conf.selectAreaId).html(html);
	        		}
	        		if($("#"+conf.selectCompanyId).size()>0){
	        			var html="<option value='"+companyId+"'>"+companyName.substring(0,companyName.length-3)+"项目部"+"</option>";
	        			$("#"+conf.selectCompanyId).html(html);
	        		}
	        		if($("#"+conf.iptAreaId).size()>0){
	        			$("#"+conf.iptAreaId).val(areaId);
	        		}
	        		if($("#"+conf.iptCompanyId).size()>0){
	        			$("#"+conf.iptCompanyId).val(companyId);
	        		}
	        		if($("#"+conf.iptAreaName).size()>0){
	        			$("#"+conf.iptAreaName).val(areaName);
	        		}
	        		if($("#"+conf.iptCompanyName).size()>0){
	        			$("#"+conf.iptCompanyName).val(companyName.substring(0,companyName.length-3)+"项目部");
	        		}
	        	}else{
	        		//$("#hId").html("启明项目申请单");
	        		$("#areaLabelId").html('<font color="red">*</font>&nbsp;大区：');
	      		  	$("#branchOfficeLabelId").html('<font color="red">*</font>&nbsp;分公司：');
	        	}
	    		
	        },      
	        error: function(e){
	        	//失败后回调      
	            alert("服务器请求失败,请重试");  
	        }
	   });
	 
}


</script>