/*查询任务抄送人*/
var showCCMan = function (id) {
    var showMsg = '当前任务没有抄送人！';
    var dialog = bootbox.dialog({
        message: '<p class="text-center"><i class="fa fa-spin fa-spinner"></i>正在加载...</p>',
        size: 'small',
        closeButton: false
    });
    $.ajax({
        url: config.taskCCUrl,
        type: "POST",
        data: {id: id},
        timeout: 10000,
        success: function (data) {
            dialog.modal('hide');
            if (data != undefined && data != null && data != "") {
                if (data.code == 200 && data.data.length > 0) {
                    var manList = [];
                    $(data.data).each(function (i, item) {
                        manList.push(item.name);
                    })
                    showMsg = manList.join(',');
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
        title: '当前任务抄送人',
        message: ccMan,
        buttons: {
            ok: {
                label: "确定"
            }
        }
    });
}

//发布
var fnTaskPublish = function (id) {
    var confirmDialog = bootbox.confirm({
        message: "确定要发布此任务吗？",
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
                url: config.taskPublishUrl,
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


//task exec
var fnTaskExec = function (id) {
    var confirmDialog = bootbox.confirm({
        message: "确定要执行此任务吗？",
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
                url: config.taskExecUrl,
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

//task submit
var fnTaskSubmit = function (id) {
    var loading = bootbox.dialog({
        message: '<p class="text-center"><i class="fa fa-spin fa-spinner"></i>正在执行...</p>',
        size: 'small',
        closeButton: false
    });
    $.post(config.taskSubmitUrl, {id: id}, function (data) {
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

//task comment
var fnTaskComment = function (id) {
    var dialog = bootbox.dialog({
        title: "报告备注",
        message: '<textarea id="txtAreaContent" style="width:98%;border:1px solid #ccc;border-radius:4px;padding:6px 12px"></textarea>',
        buttons: {
            cancel: {
                label: '取消',
                className: 'btn-danger'
            },
            noclose: {
                label: '提交',
                className: 'btn-primary',
                callback: function () {
                    var feeBackContent = $("#txtAreaContent").val();
                    if (feeBackContent == "") {
                        bootbox.alert("请输入备注内容");
                        return false;
                    }
                    if (feeBackContent.length > 200) {
                        bootbox.alert("最多输入200个字");
                        return false;
                    }
                    var loading = bootbox.dialog({
                        message: '<p>提交中...</p>',
                        closeButton: false
                    });
                    $.post(config.taskCommentUrl, {id: id, content: feeBackContent}, function (data) {
                        loading.modal('hide')
                        if (data == undefined || data == null || data == "") {
                            bootbox.alert("提交备注结果失败");
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

//显示send-list 操作
//close task
var fnTaskColosed = function (id) {
    var confirmDialog = bootbox.confirm({
        message: "确定要关闭此任务吗，关闭后其子任务也随其关闭？",
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
                url: config.taskColseUrl,
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

//task del
var fnTaskDel = function (id) {
    var confirmDialog = bootbox.confirm({
        message: "确定要删除此任务吗，删除后其子任务也随其删除？",
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
                url: config.taskDelUrl,
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
//task del
var fnTaskRealDel = function (id) {
    var confirmDialog = bootbox.confirm({
        message: "确定要删除此草稿任务吗？",
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
                url: config.taskRealDelUrl,
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

//task evaluate
var fnTaskEvaluate = function (id) {
    var dialogHtml = '<div>任务评星<br/><div class="divstar">';
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
        title: "任务评价",
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
                    /*	if($(".divstar .select").size()<1){
                     bootbox.alert("请先评星");
                     return false;
                     }*/
                    var num = $(".divstar .select").attr("starnum");
                    var feeBackContent = $("#txtAreaContent").val();
                    if (feeBackContent.length > 200) {
                        bootbox.alert("最多输入200个字");
                        return false;
                    }
                    var loading = bootbox.dialog({
                        message: '<p>提交中...</p>',
                        closeButton: false
                    });
                    $.post(config.taskEvalUrl, {id: id, score: num, content: feeBackContent}, function (data) {
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
//sent-list end