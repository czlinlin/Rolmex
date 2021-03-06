//发布
var fnCmsPublish = function (id) {
    var confirmDialog = bootbox.confirm({
        message: "确定要发布此公告吗？",
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
                url: config.cmsPublishUrl,
                type: "POST",
                data: {id: id},
                timeout: 10000,
                success: function (data) {
                    loading.modal('hide');
                    if (data == undefined || data == null || data == "") {
                        bootbox.alert("发布操作失败");
                        return;
                    }

                    if (data.code == "200") {
                        var tip = bootbox.alert(
                            {
                                message: "发布操作成功！",
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


//删除
var fnCmsDel=function(id){
    var confirmDialog=bootbox.confirm({
        message: "确定要删除此草稿公告吗？",
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
                url: config.cmsDelUrl,
                type:"POST",
                data:{id:id},
                timeout:10000,
                success: function(data) {
                    loading.modal('hide');
                    if(data==undefined||data==null||data==""){
                        bootbox.alert("删除草稿操作失败");
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
                    alert("["+XMLHttpRequest.status+"]error，请求失败")
                },
                complete:function(xh,status){
                    if(status=="timeout")
                        bootbox.alert("请求超时");
                }
            });
        }
    });
}


//已发删除
var fnDel=function(id){
    var confirmDialog=bootbox.confirm({
        message: "确定要删除此公告吗？",
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
                url: config.DelUrl,
                type:"POST",
                data:{id:id},
                timeout:10000,
                success: function(data) {
                    loading.modal('hide');
                    if(data==undefined||data==null||data==""){
                        bootbox.alert("删除操作失败");
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
                    alert("["+XMLHttpRequest.status+"]error，请求失败")
                },
                complete:function(xh,status){
                    if(status=="timeout")
                        bootbox.alert("请求超时");
                }
            });
        }
    });
}