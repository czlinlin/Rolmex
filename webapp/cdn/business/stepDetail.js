/**
 * 未结流程统计列表的常规流程获取流程步骤环节
 */
var detailShow = function (businessDetailID,businessKey) {
    var dialog = bootbox.dialog({
        message: '<p class="text-center"><i class="fa fa-spin fa-spinner"></i>正在加载...</p>',
        size: 'small',
        closeButton: false
    });
    var html = '<div class="panel panel-default" style="max-height:500px;overflow-y:scroll;"><table class="table table-hover" style="width:100%;">';
    $.ajax({
    	url:config.isBranches,
    	type:"post",
    	data:{businessDetailID: businessDetailID,businessKey: businessKey},
    	dataType:"json",
    	timeout:10000,
    	success:function(data){
    		if(data[0] == "0"){
    			$.ajax({
    		        url: config.detailShowUrl,
    		        type: "POST",
    		        data: {businessDetailID: businessDetailID},
    		        dataType:"JSON",
    		        timeout: 10000,
    		        success: function (data) {
    		            dialog.modal('hide');
    		           if (data.code == 200) {
    		                if (data.whole == undefined || data.whole == null || data == "" || data.whole.length < 1)
    		                    html += '<tr><td colspan="2">未获取到相关流程审核步骤详情</td></tr>'
    		                else {
    		                	html += '<tr><td>' + data.whole + '</td></tr>'
    		                }
    		            }
    		            html += "</table></div>";
    		            stepDialog(html);
    		        },
    		        error: function (XMLHttpRequest, textStatus, errorThrown) {
    		        	dialog.modal('hide');
    		            alert("请求超时")
    		        },
    		        complete: function (xh, status) {
    		            dialog.modal('hide');
    		            if (status == "timeout")
    		                bootbox.alert("请求超时");
    		        }
    		    });
    		}else{
    			var isMoney = "0";
    			var money = "";
    			var userId = data[1];
    			if(data.length > 2){
    				isMoney = "1";
    				money = data[2];
    			}
    			$.ajax({
        			url:config.branchesUrl,
        			data:{businessDetailID:businessDetailID,userId:userId,isMoney:isMoney,money:money},
        			dataType:"json",
        			type: "POST",
        			timeout: 10000,
        			success:function(data){
        				dialog.modal('hide');
        				if (data[0] == undefined || data[0] == null)
		                    html += '<tr><td colspan="2">未获取到相关流程审核步骤详情</td></tr>'
		                else {
		                	html += '<tr><td>' + data[0].conditionNode + '</td></tr>'
		                }
			            html += "</table></div>";
			            stepDialog(html);
        			},
        			error:function(){
        				dialog.modal('hide');
        				alert("获取多分支流程环节错误！");
        			}
        		});
    		}
    	},
    	error:function(){
    		dialog.modal('hide');
    		alert("验证流程是否为多分支失败！");
    	}
    });
    
}
/**
 * 未结流程统计列表的自定义常规流程获取流程步骤环节
 */
var detailShowCustom = function (processInstanceId) {
	
	var dialog = bootbox.dialog({
		message: '<p class="text-center"><i class="fa fa-spin fa-spinner"></i>正在加载...</p>',
		size: 'small',
		closeButton: false
	});
	var html = '<div class="panel panel-default" style="max-height:500px;overflow-y:scroll;"><table class="table table-hover" style="width:100%;">';
	$.ajax({
		url: config.detailShowCustomUrl,
		type: "POST",
		data: {processInstanceId: processInstanceId},
		dataType:"JSON",
		timeout: 10000,
		success: function (data) {
			dialog.modal('hide');
			if (data.code == 200) {
				if (data.whole == undefined || data.whole == null || data == "" || data.whole.length < 1)
					html += '<tr><td colspan="2">未获取到相关流程审核步骤详情</td></tr>'
				else {
					html += '<tr><td>' + data.whole + '</td></tr>'
				}
			}else if(data.code == 500){
				html += '<tr><td>' + data.whole + '</td></tr>'
			}
			html += "</table></div>";
			stepDialog(html);
		},
		error: function (XMLHttpRequest, textStatus, errorThrown) {
			alert("请求超时")
		},
		complete: function (xh, status) {
			dialog.modal('hide');
			if (status == "timeout")
				bootbox.alert("请求超时");
		}
	});
}
/**
 *多分支流程获取流程步骤环节
 */
var chooseNode = function(busDetailId,index){
	var dialog = bootbox.dialog({
		message: '<p class="text-center"><i class="fa fa-spin fa-spinner"></i>正在加载...</p>',
		size: 'small',
		closeButton: false
	});
	var html = '<table style="width:100%"><tr><td style="border-bottom:1px solid #ccc;"><div style="width:130px;">'+
				'<input type="hidden" id="iptSelectIds" name="iptSelectIds"/>'+
				'流程的审批环节：</div></td>';
	$.ajax({
		url: config.chooseNodeUrl,
		type: "POST",
		data: {businessDetailId: busDetailId},
		dataType:"json",
		timeout: 10000,
		success: function (data) {
			dialog.modal('hide');
			html += "<td id='text'></td>" +
					"<tr><td colspan='2'>";
			for(var i=0;i<data.length;i++){
				html += "<input type=\"checkbox\" onclick='fnSelectNode(this)' name=\"node\" value="+data[i].name+">"+data[i].name+"<br/>";
			}
			html += "</td></tr></table>";
			branchStepDialog(html,index);
		},
		error: function (XMLHttpRequest, textStatus, errorThrown) {
			alert("请求超时");
		},
		complete: function (xh, status) {
			dialog.modal('hide');
			if (status == "timeout")
				bootbox.alert("请求超时");
		}
	});
}

var fnSelectNode=function(obj){
	var bool_checked=$(obj).prop("checked");
	var thisValue=$(obj).val();
	var iptSelectIds=$("#iptSelectIds").val();
	if(iptSelectIds!=""){
		if(bool_checked){
			iptSelectIds+="->"+thisValue;
		}else{
			var iptSelectArray=iptSelectIds.split('->');
			var newArray=[];
			for(var i=0;i<iptSelectArray.length;i++){
				if(thisValue!=iptSelectArray[i]){
					newArray.push(iptSelectArray[i]);
					iptSelectIds=newArray.join("->");
				}
			}
		}
		
	}else{
		if(bool_checked){
			iptSelectIds=thisValue;
		}
	}
	if(!bool_checked && iptSelectIds.indexOf("->") < 0){
		$("#iptSelectIds").val("");
		$("#text").html("");
	}else{
		$("#iptSelectIds").val(iptSelectIds);
		$("#text").html(iptSelectIds);
	}
}

var branchStepDialog = function (show,index) {
	bootbox.dialog({
		title: '请选择流程审核节点',
		message: show,
		buttons: {
			ok: {
				label: "确定",
				callback:function(result){
					result = "";
					/*$.each($('input[type=checkbox]:checked'),function(){
						var value = $(this).val();
						result += value.substring(0,value.length)+"->";
					});*/
					result=$("#iptSelectIds").val();
					$("#link"+index).val(result);
					//$("#link"+index).val(result.substring(0,result.length-2));
				}
			},
			cancel:{
				label:"取消"
			}
		}
		
	});
}
var stepDialog = function (show) {
    bootbox.dialog({
        title: '当前流程审核环节',
        message: show,
        buttons: {
            ok: {
                label: "确定"
            }
        }
    });
}