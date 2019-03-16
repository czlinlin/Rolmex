var fnShow = function (id) {
    var dialog = bootbox.dialog({
        message: '<p class="text-center"><i class="fa fa-spin fa-spinner"></i>正在加载...</p>',
        size: 'small',
        closeButton: false
    });
    var html = '<div class="panel panel-default" style="max-height:500px;overflow-y:scroll;"><table class="table table-hover" style="width:100%;">';
    $.ajax({
            url: config.fnShowUrl,
            type: "POST",
            data: {id: id},
            timeout: 10000,
            success: function (data) {
                dialog.modal('hide');
                if (data.code == 200) {
                    if (data == undefined || data == null || data == "" || data.data.length < 1)
                        html += '<tr><td colspan="2">当前业务类型没有部门</td></tr>'
                    else {
                        if (data.data.length > 0) {
                            $(data.data).each(function (i, item) {
                                html += '<tr><td>' + item.up + '</td><td>' + '</td> <td>' + item.down + '</td></tr>'

                            })
                        }
                    }
                }
                html += "</table></div>";
                showDialog(html);
            }
            ,
            error: function (XMLHttpRequest, textStatus, errorThrown) {
                alert("请求超时")
            }
            ,
            complete: function (xh, status) {
                dialog.modal('hide');
                if (status == "timeout")
                    bootbox.alert("请求超时");
            }
        }
    );
}

var showDialog = function (show) {
    bootbox.dialog({
        title: '当前业务类型部门',
        message: show,
        buttons: {
            ok: {
                label: "确定"
            }
        }
    });
}

var positionShow = function (id) {

    var dialog = bootbox.dialog({
        message: '<p class="text-center"><i class="fa fa-spin fa-spinner"></i>正在加载...</p>',
        size: 'small',
        closeButton: false
    });
    var html = '<div class="panel panel-default" style="max-height:500px;overflow-y:scroll;"><table class="table table-hover" style="width:100%;">';
    $.ajax({
        url: config.positionShowUrl,
        type: "POST",
        data: {id: id},
        timeout: 10000,
        success: function (data) {
            dialog.modal('hide');

            if (data.code == 200) {
                if (data == undefined || data == null || data == "" || data.data.length < 1)
                    html += '<tr><td colspan="2">当前业务类型明细没有岗位</td></tr>'
                else {
                    if (data.data.length > 0) {
                        $(data.data).each(function (i, item) {
                            html += '<tr><td>' + item.parent + '</td><td>' + item.child + '</td> <td>' + item.position + '</td></tr>'

                        })
                    }
                }
            }
            html += "</table></div>";
            positionDialog(html);
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
var positionDialog = function (show) {
    bootbox.dialog({
        title: '当前业务类型明细的岗位',
        message: show,
        buttons: {
            ok: {
                label: "确定"
            }
        }
    });
}