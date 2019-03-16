//页面加载完毕后
$(function () {
    /*if($("input[name='iptpercent']").size()<1)
     return;

     var iptIds=$("input[name='iptpercent']");
     var iptIdArray=[]
     $(iptIds).each(function(i,item){
     iptIdArray.push($(item).val())
     })
     var ids=iptIdArray.join(',')
     $.ajax({
     url: config.projectProgressUrl,
     type:"POST",
     data:{ids:ids},
     timeout:10000,
     success: function(data) {
     if(data==undefined||data==null||data==""){
     bootbox.alert("获取项目错误");
     return;
     }
     if(data.code=="200"){

     $(data.data).each(function(i,item){

     var html='<div class="progress div_progress">';
     html+='目标进度:'+item.targetpercent+',当前进度:'+item.actualpercent;
     html+='</div>';
     html+='<div class="progress target_progress target_'+item.bg+'" style="width:'+item.targetpercent+'"></div>';
     html+='<div class="progress actual_progress actual_'+item.bg+'" style="width:'+item.actualpercent+'"></div>';

     var percent=$("#percent"+item.id);

     var targetdiv=$("#targetPercent"+item.id);
     targetdiv.addClass("blankrangle");
     targetdiv.html('<div class="target_'+item.bg+'" style="width:'+item.targetpercent+'">'+item.targetpercent+'</div>');

     var actualdiv=$("#actualPercent"+item.id);
     actualdiv.addClass("blankrangle");
     actualdiv.html('<div class="actual_'+item.bg+'" style="width:'+item.actualpercent+'">'+item.actualpercent+'</div>');

     percent.addClass("blankrangle");
     percent.html(html);
     })
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
     });*/
})

//project submit
var fnProjectSubmit = function (id) {
    var loading = bootbox.dialog({
        message: '<p class="text-center"><i class="fa fa-spin fa-spinner"></i>正在执行...</p>',
        size: 'small',
        closeButton: false
    });
    $.post(config.projectSubmitUrl, {id: id}, function (data) {
        loading.modal('hide')
        if (data == undefined || data == null || data == "") {
            bootbox.alert("提交结果失败");
            return;
        }

        if (data.code == "200") {
            //dialog.modal('hide')
            var tip = bootbox.alert(
                {
                    message: "提交成功！",
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
    })
}

//project del
var fnProjectDel = function (id) {
    var confirmDialog = bootbox.confirm({
        message: "确定要删除此项目吗，删除后将删除其关联任务？",
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
            if (!result) return;

            confirmDialog.modal('hide');
            var loading = bootbox.dialog({
                message: '<p class="text-center"><i class="fa fa-spin fa-spinner"></i>正在执行...</p>',
                size: 'small',
                closeButton: false
            });
            $.ajax({
                url: config.projectDelUrl,
                type: "POST",
                data: {id: id},
                timeout: 10000,
                success: function (data) {
                    loading.modal('hide');
                    if (data == undefined || data == null || data == "") {
                        bootbox.alert("提交删除操作失败");
                        return;
                    }

                    if (data.code == "200") {
                        //dialog.modal('hide')
                        var tip = bootbox.alert(
                            {
                                message: "删除操作成功！",
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
                    loading.modal('hide');
                    alert("[" + XMLHttpRequest.status + "]error，请求失败")
                },
                complete: function (xh, status) {
                    loading.modal('hide');
                    if (status == "timeout")
                        bootbox.alert("请求超时");
                }
            });
        }
    });
}

//发布
var fnProjectPublish = function (id) {
    var confirmDialog = bootbox.confirm({
        message: "确定要发布此项目吗？",
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
            if (!result) return;
            confirmDialog.modal('hide');
            var loading = bootbox.dialog({
                message: '<p class="text-center"><i class="fa fa-spin fa-spinner"></i>正在执行...</p>',
                size: 'small',
                closeButton: false
            });
            $.ajax({
                url: config.projectPublishUrl,
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
                        //dialog.modal('hide')
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

//project realdel
var fnProjectRealDel = function (id) {
    var confirmDialog = bootbox.confirm({
        message: "确定要删除此项目吗？",
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
            if (!result) return;

            confirmDialog.modal('hide');
            var loading = bootbox.dialog({
                message: '<p class="text-center"><i class="fa fa-spin fa-spinner"></i>正在执行...</p>',
                size: 'small',
                closeButton: false
            });
            $.ajax({
                url: config.projectRealDelUrl,
                type: "POST",
                data: {id: id},
                timeout: 10000,
                success: function (data) {
                    loading.modal('hide');
                    if (data == undefined || data == null || data == "") {
                        bootbox.alert("提交删除草稿操作失败");
                        return;
                    }

                    if (data.code == "200") {
                        //dialog.modal('hide')
                        var tip = bootbox.alert(
                            {
                                message: "删除操作成功！",
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
                    loading.modal('hide');
                    alert("[" + XMLHttpRequest.status + "]error，请求失败")
                },
                complete: function (xh, status) {
                    loading.modal('hide');
                    if (status == "timeout")
                        bootbox.alert("请求超时");
                }
            });
        }
    });
}

//task close
var fnProjectColosed = function (id) {
    var confirmDialog = bootbox.confirm({
        message: "确定要关闭此项目吗，项目下的任务也将一起关闭？",
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
            if (!result) return;
            confirmDialog.modal('hide');
            var loading = bootbox.dialog({
                message: '<p class="text-center"><i class="fa fa-spin fa-spinner"></i>正在执行...</p>',
                size: 'small',
                closeButton: false
            });
            $.ajax({
                url: config.projectColseUrl,
                type: "POST",
                data: {id: id},
                timeout: 10000,
                success: function (data) {
                    loading.modal('hide');
                    if (data == undefined || data == null || data == "") {
                        bootbox.alert("提交关闭操作失败");
                        return;
                    }

                    if (data.code == "200") {
                        //dialog.modal('hide')
                        var tip = bootbox.alert(
                            {
                                message: "关闭操作成功！",
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
                    loading.modal('hide');
                    alert("[" + XMLHttpRequest.status + "]error，请求失败")
                },
                complete: function (xh, status) {
                    loading.modal('hide');
                    if (status == "timeout")
                        bootbox.alert("请求超时");
                }
            });
        }
    });
}

//project evaluate
var fnProjectEvaluate = function (id) {
    var dialogHtml = '<div>项目评星<br/><div class="divstar">';
    dialogHtml += '<img id="imgStar1" src="' + config.starImgUrl.yellow + '" onclick="fnEvalStar(1)"  starnum="1"/>';
    dialogHtml += '<img id="imgStar2" src="' + config.starImgUrl.yellow + '" onclick="fnEvalStar(2)" starnum="2"/>';
    dialogHtml += '<img id="imgStar3" src="' + config.starImgUrl.yellow + '" onclick="fnEvalStar(3)" starnum="3" class="select"/>';
    dialogHtml += '<img id="imgStar4" src="' + config.starImgUrl.gray + '" onclick="fnEvalStar(4)" starnum="4"/>';
    dialogHtml += '<img id="imgStar5" src="' + config.starImgUrl.gray + '" onclick="fnEvalStar(5)" starnum="5"/>';
    dialogHtml += '</div><br/>';
    dialogHtml += '评价内容'
    dialogHtml += '<textarea id="txtAreaContent" style="width:98%;border:1px solid #ccc;border-radius:4px;padding:6px 12px"></textarea>';
    dialogHtml += "</div>";

    var dialog = bootbox.dialog({
        title: "项目评价",
        message: dialogHtml,
        buttons: {
            cancel: {
                label: '取消',
                className: 'btn-danger'
            },
            noclose: {
                label: '提交',
                className: 'btn-primary',
                callback: function () {
                    /* if ($(".divstar .select").size() < 1) {
                     bootbox.alert("请先评星");
                     return false;
                     }*/
                    var star = $(".divstar .select").attr("starnum");

                    var feeBackContent = $("#txtAreaContent").val();
                    if (feeBackContent.length > 200) {
                        bootbox.alert("最多输入200个字");
                        return false;
                    }
                    var loading = bootbox.dialog({
                        message: '<p>提交中...</p>',
                        closeButton: false
                    });
                    $.post(config.projectEvalUrl, {id: id, score: star, content: feeBackContent}, function (data) {
                        loading.modal('hide')
                        if (data == undefined || data == null || data == "") {
                            bootbox.alert("提交评价结果失败");
                            return false;
                        }

                        if (data.code == 200) {
                            dialog.modal('hide')
                            bootbox.alert({
                                message: data.message, callback: function () {
                                    document.getElementById('btn_Search').click();
                                }
                            });
                        }
                        else
                            bootbox.alert(data.message);

                        return data.code == 200;
                    })
                    return false;
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

var fnEvalStar = function (num) {
    for (var i = 1; i <= 5; i++) {
        if (i != num) {
            $("#imgStar" + i).removeClass("select");
        }
    }

    $("#imgStar" + num).addClass("select");
    for (var i = 1; i <= 5; i++) {
        if (i > num)
            $("#imgStar" + i).attr("src", config.starImgUrl.gray)
        else
            $("#imgStar" + i).attr("src", config.starImgUrl.yellow)
    }
}

/*show notify*/
var showNotify = function (id) {
    var showMsg = '当前项目没有知会人！';
    var dialog = bootbox.dialog({
        message: '<p class="text-center"><i class="fa fa-spin fa-spinner"></i>正在加载...</p>',
        size: 'small',
        closeButton: false
    });
    $.ajax({
        url: config.projectNotifyUrl,
        type: "POST",
        data: {id: id},
        timeout: 10000,
        success: function (data) {
            if (data != undefined && data != null && data != "") {
                if (data.code == 200) {
                    var manList = [];
                    if (data.data != undefined && data.data != null && data.data != "" && data.data.length > 0) {
                        $(data.data).each(function (i, item) {
                            manList.push(item.name);
                        })
                        showMsg = manList.join(',');
                    }
                }
            }
            showCCDialog(showMsg);
        },
        error: function (XMLHttpRequest, textStatus, errorThrown) {
            alert("[" + XMLHttpRequest.status + "]error，请求失败")
        },
        complete: function (xh, status) {
            dialog.modal('hide');
            if (status == "timeout")
                bootbox.alert("请求超时");
        }
    });
}

var showCCDialog = function (ccMan) {
    bootbox.dialog({
        title: '当前项目知会人',
        message: ccMan,
        buttons: {
            ok: {
                label: "确定"
            }
        }
    });
}


// exec
var fnProjectExec = function (id) {
    var confirmDialog = bootbox.confirm({
        message: "确定要执行此项目吗？",
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
            if (!result) return;
            confirmDialog.modal('hide');
            var loading = bootbox.dialog({
                message: '<p class="text-center"><i class="fa fa-spin fa-spinner"></i>正在执行...</p>',
                size: 'small',
                closeButton: false
            });

            $.ajax({
                url: config.projectExecUrl,
                type: "POST",
                data: {id: id},
                timeout: 10000,
                success: function (data) {
                    loading.modal('hide');
                    if (data == undefined || data == null || data == "") {
                        bootbox.alert("执行操作失败");
                        return;
                    }

                    if (data.code == "200") {
                        //dialog.modal('hide')
                        var tip = bootbox.alert(
                            {
                                message: "执行成功！",
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