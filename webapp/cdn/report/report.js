//report feeback
var fnFeeBack = function (id) {
    var dialog = bootbox.dialog({
        title: "汇报反馈",
        message: '<textarea id="txtAreaContent" style="width:98%;border:1px solid #ccc;border-radius:4px;padding:6px 12px"></textarea>',
        buttons: {
            noclose: {
                label: '提交',
                className: 'btn-primary',
                callback: function () {
                    var feeBackContent = $("#txtAreaContent").val();
                    
                    if($.trim(feeBackContent)==""){
                    	bootbox.alert("反馈内容不能为空");
                        return false;
                    }
                    var loading = bootbox.dialog({
                        message: '<p>提交中...</p>',
                        closeButton: false
                    });
                    $.post(config.reportFeeBackUrl, {
                        id: id,
                        content: feeBackContent
                    }, function (data) {
                        loading.modal('hide')
                        if (data == undefined || data == null || data == "") {
                            bootbox.alert("提交执行结果失败");
                            return false;
                        }

                        if (data.result == "ok") {
                            dialog.modal('hide')
                            bootbox.alert({
                                message: data.content, callback: function () {
                                    $("#btn_Search").click();
                                }
                            });
                        }
                        else
                            bootbox.alert(data.content);

                        return data.result == "ok";
                    })
                    //dialog.modal('hide');
                    return false;
                }
            },
            cancel: {
                label: '取消',
                className: 'btn-danger'
            }
        },
        callback: function (result) {
            alert(result);
            return;
        },
        show: true
    });
}

        //show cc
        var fnShowCCInfo = function (id) {
            var loading = bootbox.dialog({
                message: '<p>查询中...</p>',
                closeButton: false
            });
            var html = '<div class="panel panel-default" style="max-height:300px;overflow-y:scroll;"><table class="table table-hover" style="width:100%;"><tr><th>姓名</th><th>阅读状态</th></tr>';
            $.post(config.reportCCInfoUrl, {id: id}, function (data) {
                loading.modal('hide');
                if (data.code == 200) {
                    if (data == undefined || data == null || data == "" || data.data.length < 1)
                        html += '<tr><td colspan="2">无抄送信息</td></tr>'
                    else {
                        if (data.data.length > 0) {
                            $(data.data).each(function (i, item) {
                                html += '<tr><td>' + item.name + '</td><td>' + item.status + '</td></tr>'

                            })
                        }
                    }
                    html += "</table></div>";

                    var ccDialog = bootbox.dialog({
                        title: "抄送信息",
                        message: html,
                        show: true,
                        buttons: {
                            ok: {label: "确定"}
                        }
                    });
                }
                else {
                    bootbox.alert(data.message);
                }
            })
        }

//show turn to info
var fnShowTurnToInfo = function (id) {
    var loading = bootbox.dialog({
        message: '<p>查询中...</p>',
        closeButton: false
    });
    var html = '<div class="panel panel-default" style="max-height:300px;overflow-y:scroll;"><table class="table table-hover" style="width:100%;"><tr><th>姓名</th><th>阅读状态</th><th>转发备注</th></tr>';
    $.post(config.reportShowTurnInfoUrl, {id: id}, function (data) {
        loading.modal('hide');

        if (data.code == 200) {
            if (data == undefined || data == null || data == "" || data.data.length < 1)
                html += '<tr><td colspan="3">无转发信息</td></tr>'
            else {
                if (data.data.length > 0) {
                    $(data.data).each(function (i, item) {
                        html += '<tr>'
                        html += '<td>' + item.name + '</td>'
                        html += '<td>' + item.status + '</td>'
                        html += '<td>' + item.remarks + '</td>'
                        html += '</tr>'
                    })
                }
            }
            html += "</table></div>";

            var ccDialog = bootbox.dialog({
                title: "我转发信息",
                message: html,
                show: true,
                buttons: {
                    ok: {label: "确定"}
                }
            });
        }
        else {
            bootbox.alert(data.message);
        }
    })
}
        
//report realdel
var fnReportRealDel=function(id){
	    var confirmDialog=bootbox.confirm({
	        message: "确定要删除此草稿吗？",
	        buttons: {
	            confirm: {
	                label: '确定',
	                className: 'btn-success'
	            },
	            cancel: {
	                label: '取消',
	                className: 'btn-danger'
	            }
	        },
	        callback: function (result) {
	        	if(!result) return;
	        	
	        	confirmDialog.modal('hide');
	        	var loading = bootbox.dialog({
  	     	    message:'<p class="text-center"><i class="fa fa-spin fa-spinner"></i>正在执行...</p>',
  	     	    size:'small',
  	     	    closeButton: false
  	     	});
	        	$.ajax({
	             url: config.reportRealDelUrl,
	             type:"POST",
	             data:{id:id},
	             timeout:10000,
	             success: function(data) {
	            	loading.modal('hide'); 
	            	if(data==undefined||data==null||data==""){
	     			bootbox.alert("提交删除草稿操作失败");
	     			return;
	     		}
	     		
	     		if(data.code=="200"){
	     			//dialog.modal('hide')
	     			var tip=bootbox.alert(
	     				{
	     					message:"删除操作成功！",
	     				 	callback:function(){
	     				 		//$("#btn_Search").click();
	     				 		document.getElementById('btn_Search').click();
	     				 		tip.modal('hide');
	     					 	
	     					 }
	     				});
	     		}
	     		else
	     			bootbox.alert(data.message);
	     		return;
	             },
	             error:function(XMLHttpRequest, textStatus, errorThrown){
	            	 loading.modal('hide');
	             	alert("["+XMLHttpRequest.status+"]error，请求失败")
	             },
	             complete:function(xh,status){
	            	 loading.modal('hide');
	             	if(status=="timeout")
	             		bootbox.alert("请求超时");
	             }
	         });
	        }
	    });
}

//已发汇报若接收人未读 可以删除
var fnReportDrawback=function(id){
    var confirmDialog=bootbox.confirm({
        message: "确定要删除此汇报吗？",
        buttons: {
            confirm: {
                label: '确定',
                className: 'btn-success'
            },
            cancel: {
                label: '取消',
                className: 'btn-danger'
            }
        },
        callback: function (result) {
            if(!result) return;

            confirmDialog.modal('hide');
            var loading = bootbox.dialog({
                message:'<p class="text-center"><i class="fa fa-spin fa-spinner"></i>正在执行...</p>',
                size:'small',
                closeButton: false
            });
            $.ajax({
                url: config.reportRealDelUrl,
                type:"POST",
                data:{id:id},
                timeout:10000,
                success: function(data) {
                    loading.modal('hide');
                    if(data==undefined||data==null||data==""){
                        bootbox.alert("提交删除操作失败");
                        return;
                    }

                    if(data.code=="200"){
                        //dialog.modal('hide')
                        var tip=bootbox.alert(
                            {
                                message:"删除操作成功！",
                                callback:function(){
                                    //$("#btn_Search").click();
                                    document.getElementById('btn_Search').click();
                                    tip.modal('hide');

                                }
                            });
                    }
                    else
                        bootbox.alert(data.message);
                    return;
                },
                error:function(XMLHttpRequest, textStatus, errorThrown){
                    loading.modal('hide');
                    alert("["+XMLHttpRequest.status+"]error，请求失败")
                },
                complete:function(xh,status){
                    loading.modal('hide');
                    if(status=="timeout")
                        bootbox.alert("请求超时");
                }
            });
        }
    });
}

//发布
var fnReportPublic = function (id) {
    var confirmDialog = bootbox.confirm({
        message: "确定要提交此汇报吗？",
        buttons: {
            confirm: {
                label: '确定',
                className: 'btn-success'
            },
            cancel: {
                label: '取消',
                className: 'btn-danger'
            }
        },
        callback: function (result) {
            if(!result) return;
            confirmDialog.modal('hide');
            var loading = bootbox.dialog({
                message: '<p class="text-center"><i class="fa fa-spin fa-spinner"></i>正在执行...</p>',
                size: 'small',
                closeButton: false
            });
            $.ajax({
                url: config.reportPublicUrl,
                type: "POST",
                data: {id: id},
                timeout: 10000,
                success: function (data) {
                    loading.modal('hide');
                    if (data == undefined || data == null || data == "") {
                        bootbox.alert("提交操作失败");
                        return;
                    }

                    if (data.code == "200") {
                        //dialog.modal('hide')
                        var tip = bootbox.alert(
                            {
                                message: "提交操作成功！",
                                callback: function () {
                                    //$("#btn_Search").click();
                                    document.getElementById('btn_Search').click();
                                    tip.modal('hide');
                                }
                            });
                    }
                    else
                        bootbox.alert(data.message);
                    return;
                },
                error: function (XMLHttpRequest, textStatus, errorThrown) {
                    alert("[" + XMLHttpRequest.status + "]error，请求失败")
                },
                complete: function (xh, status) {
                    if (status == "timeout")
                        bootbox.alert("请求超时");
                }
            });
        }
    });
}