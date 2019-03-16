
var createUserPicker = function (conf) {
    var ids = "";
    var count = 0;
    var partyStructId = 1;
    conf = conf ? conf : {};
    var defaults = {
        modalId: 'userPicker',
        multiple: false,
        searchUrl: '/mossle-web-user/default/rs/user/search',
        treeUrl: '/mossle-app-lemon/rs/party/tree?partyStructTypeId=1'
    };
    for (var key in defaults) {
        if (!conf[key]) {
            conf[key] = defaults[key];
        }
    }

    if ($('#' + conf.modalId).length == 0) {
        $(document.body).append(
            '<div id="' + conf.modalId + '" class="modal fade">'
            + '  <div class="modal-dialog">'
            + '    <div class="modal-content">'
            + '      <div class="modal-header">'
            + '        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>'
            + '        <h3>选择用户</h3>'
            + '      </div>'
            + '      <div class="modal-body">'
            + '        <ul id="myTabs" class="nav nav-tabs">'
            + '          <li role="presentation" class="active"><a href="#org">组织机构</a></li>'
            + '        </ul>'
            + '        <div class="tab-content">'
            + '          <div role="tabpanel" class="tab-pane active" id="org">'
            + '        <div class="row" id="org">'
            + '          <div class="col-md-4">'
            + '            <ul id="' + conf.modalId + 'treeMenu" class="ztree"></ul>'
            + '          </div>'
            + '          <div class="col-md-8">'
            + '            <div>'
            + '              <label for="' + conf.modalId + '_username"  class="">姓名:</label>'
            + '              <input type="text" id="' + conf.modalId + '_username" style="margin-bottom:0px; width:auto; display:inline;" class="form-control">'
            + '              <button id="' + conf.modalId + '_search" class="btn btn-default">查询</button>'
            + '            </div>'
            + '            <div class="panel panel-default" style="max-height:330px;min-height: 50px;overflow:auto;">'
            /* +'              <div class="panel-heading">'
             +'                <h3 class="panel-title">用户</h3>'
             +'              </div>'*/
            + '              <table id="' + conf.modalId + '_grid" class="table table-hover">'
            + '                <thead>'
            + '                  <tr style="background-color: #1d82d0;color:#fff;">'
            + '                    <th width="20" class="m-table-check">&nbsp;</th>'
            + '                    <th width="100">姓名</th>'
            + '                    <th>岗位</th>'
            + '                  </tr>'
            + '                </thead>'
            + '                <tbody  id="' + conf.modalId + '_body">'
            + '                    <th width="180"></th>'
            + '                </tbody>'
            + '              </table>'
            + '            </div>'
            + '          </div>'
            + '        </div>'
            + '          </div>'
            + '        </div>'
            + '      </div>'
            + '      <div class="modal-footer">'
            + '        <span id="' + conf.modalId + '_result" style="float:left;display:block"></span>'
            + '        <a id="' + conf.modalId + '_close" href="javascript:" class="btn" data-dismiss="modal">关闭</a>'
            + '        <a id="' + conf.modalId + '_select" href="javascript:" class="btn btn-primary">选择</a>'
            + '      </div>'
            + '    </div>'
            + '  </div>'
            + '</div>');

        $('#myTabs a').click(function (e) {
            e.preventDefault()
            $(this).tab('show')
        });

    }

    var doSearch = function (username, partyStructId) {
        $.ajax({
            url: conf.searchUrl,
            data: {
                username: username,
                partyStructId: partyStructId
            },
            success: function (data) {
                var html = '';
                for (var i = 0; i < data.length; i++) {
                    var item = data[i];
                    var check = "";
                    if (ids.indexOf(item.id) >= 0) {
                        check = "checked";
                    }
                    html +=
                        '<tr>'
                        + '<td><input id="' + conf.modalId + '_item_' + i + '" type="' + (conf.multiple ? 'checkbox' : 'radio')
                        + '" class="selectedItem" name="name" value="'
                        + item.id + '" title="' + item.displayName + '"' + check + '></td>'
                        + '<td><label for="' + conf.modalId + '_item_' + item.id + '">' + item.displayName + '</label></td>'
                        + '<td><label for="' + conf.modalId + '_item_post' + item.id + '">' + item.postName + '</label></td>'
                        + '</tr>'
                }
                $('#' + conf.modalId + '_body').html(html);
            }
        });
    }

    var doSearchChild = function (parentId) {
        $.ajax({
            url: conf.childUrl,
            data: {
                parentId: parentId
            },
            success: function (data) {
                var html = '';
                for (var i = 0; i < data.length; i++) {
                    var item = data[i];
                    var check = "";
                    if (ids.indexOf(item.id) >= 0) {
                        check = "checked";
                    }

                    html +=
                        '<tr>'
                        + '<td><input id="' + conf.modalId + '_item_' + item.id + '" type="' + (conf.multiple ? 'checkbox' : 'radio')
                        + '" class="selectedItem" name="name" value="'
                        + item.id + '" title="' + item.userName + '"' + check + '></td>'
                        + '<td><label for="' + conf.modalId + '_item_' + item.id + '">' + item.userName + '</label></td>'
                        + '<td><label for="' + conf.modalId + '_item_post' + item.id + '">' + item.postName + '</label></td>'
                        + '</tr>'
                }

                var obj = $("#" + conf.modalId + "_item_")

                partyStructId = parentId;
                $('#' + conf.modalId + '_body').html(html);
            }
        });
    }


//这里是通过样式 绑定click事件的  把这里改成通过id绑定就ok了  conf里面加个targetId 表示要在哪个上面绑定
//     $(document).delegate('.userPicker .input-group-addon', 'click', function(e) {
    $(document).delegate('#' + conf.targetId, 'click', function (e) {
            var multiple = $(this).parent().data('multiple');
            if (multiple) {
                conf.multiple = true;
            }
            var setting = {
                data: {
                    simpleData: {
                        enable: true
                    },
                    key: {  
                        title: "title"  
                    }
                },
                async: {
                    enable: true,
                    url: conf.treeUrl,
                    autoParam: ["id", "name"],
                    type: "post",//默认post
                    dataFilter: filter  //异步返回后经过Filter
                },
                view: {
                    expandSpeed: "",
                    nameIsHTML: true
                },
                callback: {
                    onClick: function (event, treeId, treeNode) {
                        doSearchChild(treeNode.id);
                    },
                    asyncSuccess: zTreeOnAsyncSuccess,//异步加载成功的fun
                    asyncError: zTreeOnAsyncError   //加载错误的fun 
                    // beforeClick:beforeClick //捕获单击节点之前的事件回调函数 
                }
            };

            //treeId是treeDemo
            function filter(treeId, parentNode, childNodes) {
                if (!childNodes) return null;
                for (var i = 0, l = childNodes.length; i < l; i++) {
                	if (childNodes[i] != null) {
                		childNodes[i].name = childNodes[i].name.replace('', '');
                	}
                }
                return childNodes;
            }

            function beforeClick(treeId, treeNode) {
                if (!treeNode.isParent) {
                    return false;
                } else {
                    return true;
                }
            }

            function zTreeOnAsyncError(event, treeId, treeNode) {
                alert("异步加载失败!");
            }

            function zTreeOnAsyncSuccess(event, treeId, treeNode, msg) {

            }

            var zNodes = [];

            try {
                $.fn.zTree.init($("#" + conf.modalId + "treeMenu"), setting, zNodes);
            } catch (e) {
                console.error(e);
            }

            $('#' + conf.modalId + '_body').empty();
            //如果为多选则显示，每个人；否则，只显示一个人
            $('#' + conf.modalId + '_result').empty();//清空
            if(conf.multiple){
            	$('#' + conf.modalId + '_username').val("");
                var strIds = "";
                ids = $("#"+conf.inputStoreIds.iptid).val();
                strIds = ids;
                var names = $("#"+conf.inputStoreIds.iptname).val();
                if (strIds != "") {
                    arrNames = names.split(",");
                    arrIds = strIds.split(",");
                    for (var i = 0; i < arrIds.length; i++) {
                        var html = '&nbsp;<span class="label label-default" style="float:left;margin:2px 1px 0 1px;" id="' + arrIds[i] + '" title="' + arrNames[i] + '">' + arrNames[i] + '<i class="glyphicon glyphicon-remove" style="cursor:pointer;"></i></span>';
                        /*if (i % 5 == 0) {
                            $('#' + conf.modalId + '_result').append("<br/>");
                        }*/
                        $('#' + conf.modalId + '_result').append(html);
                    }
                }
            }
            else{
            	$('#' + conf.modalId + '_username').val("");
                var id = $("#"+conf.inputStoreIds.iptid).val();
                ids = id;
                var name = $("#"+conf.inputStoreIds.iptname).val();
                if (id != "") {
                    $('#' + conf.modalId + '_result').empty();
                    var html = '&nbsp;<span class="label label-default" style="float:left;margin:2px 1px 0 1px;" id="' + id + '" title="' + name + '">' + name + '<i class="glyphicon glyphicon-remove" style="cursor:pointer;"></i></span>';
                    $('#' + conf.modalId + '_result').append(html);
                }
            }
            
            /*if (conf.modalId == 'leaderPicker') {
                $('#' + conf.modalId + '_username').val("");
                var id = $("#leaderId").val();
                ids = id;
                var name = $("#leaderName").val();
                if (id != "") {
                    // 清空
                    $('#' + conf.modalId + '_result').empty();
                    //var html = '&nbsp;<span class="label label-default" id="' + id + '" title="' + name + '">' + name + '<i class="glyphicon glyphicon-remove" style="cursor:pointer;"></i></span>';
                    $('#' + conf.modalId + '_result').append(html);
                } else {
                    $('#' + conf.modalId + '_result').empty();
                }
            }

            if (conf.modalId == 'ccUserPicker') {
                $('#' + conf.modalId + '_username').val("");
                var strIds = "";
                ids = $("#btnPickerMany").val();
                strIds = ids;
                var names = $("#userName").val();
                if (strIds != "") {
                    // 清空
                    $('#' + conf.modalId + '_result').empty();
                    arrNames = names.split(",");
                    arrIds = strIds.split(",");
                    for (var i = 0; i < arrIds.length; i++) {
                        var html = '&nbsp;<span class="label label-default" id="' + arrIds[i] + '" title="' + arrNames[i] + '">' + arrNames[i] + '<i class="glyphicon glyphicon-remove" style="cursor:pointer;"></i></span>';
                        if (i % 5 == 0) {
                            $('#' + conf.modalId + '_result').append("<br>");
                        }
                        $('#' + conf.modalId + '_result').append(html);
                    }
                } else {
                    $('#' + conf.modalId + '_result').empty();
                }
            }*/

            $('#' + conf.modalId).data('userPicker', $(this).parent());
            $('#' + conf.modalId).modal();
            // doSearch('');
        }
    );

// $(document).delegate('#' + conf.modalId + '_body tr', 'click', function(e) {
//	$('input[type=radio].selectedItem').prop('checked', false);
//	$(this).find('.selectedItem').prop('checked', true);
// });
    if (conf.modalId == 'ccUserPicker') {
        ids = $("#btnPickerMany").val();
    }

    if (conf.modalId == 'leaderPicker') {
        ids = $("#leaderId").val();
    }
    
    //选择人
    $(document).delegate('#' + conf.modalId + '_body .selectedItem', 'click', function (e) {
        if (conf.multiple) {
            var el = $(this);
            if (el.prop('checked')) {
                if (ids.indexOf($(this).val()) < 0) {
                    count++;
                    var idcount = ids.split(",");
                    /*if ((idcount.length) % 5 == 0) {
                        $('#' + conf.modalId + '_result').append("<br>");
                    }*/

                    var html = '&nbsp;<span class="label label-default" style="float:left;margin:2px 1px 0 1px;" id="' + $(this).val() + '" title="' + $(this).attr('title') + '">' + $(this).attr('title') + '<i class="glyphicon glyphicon-remove" style="cursor:pointer;"></i></span>';
                    $('#' + conf.modalId + '_result').append(html);
                    ids = ids + "," + $(this).val();
                }
            } else {
                $('#' + conf.modalId + '_result #' + el.val()).remove();
                ids = ids.replace(el.val(), "");
            }
        } else {
            var html = '<span class="label label-default" id="' + $(this).val() + '" title="' + $(this).attr('title') + '">' + $(this).attr('title') + '<i class="glyphicon glyphicon-remove" style="cursor:pointer;"></i></span>';
            $('#' + conf.modalId + '_result').html(html);
            ids = $(this).val();
        }
    });

    $(document).delegate('#' + conf.modalId + '_aliasBody .selectedItem', 'click', function (e) {
        var html = '<span class="label" id="' + $(this).val() + '" title="' + $(this).attr('title') + '">' + $(this).attr('title') + '<i class="icon-minus-sign" style="cursor:pointer;"></i></span>';
        $('#' + conf.modalId + '_result').html(html);
    });

    $(document).delegate('#' + conf.modalId + '_exprBody .selectedItem', 'blur', function (e) {
        var html = '<span class="label" id="' + $(this).val() + '" title="' + $(this).val() + '">' + $(this).val() + '<i class="icon-minus-sign" style="cursor:pointer;"></i></span>';
        $('#' + conf.modalId + '_result').html(html);
    });

    $(document).delegate('.glyphicon-remove', 'click', function (e) {
        var id = $(this).parent().attr('id');

        $('#' + conf.modalId + '_item_' + id).prop('checked', false);
        $(this).parent().remove();

        ids = ids.replace(id, "");
    });

    $(document).delegate('#' + conf.modalId + '_search', 'click', function (e) {
        doSearch($('#' + conf.modalId + '_username').val(), partyStructId);

    });

    $(document).delegate('#' + conf.modalId + '_username', 'keypress', function (e) {
        if (e.which == 13) {
            doSearch($('#' + conf.modalId + '_username').val(), partyStructId);
        }
    });

    $(document).delegate('#' + conf.modalId + '_select', 'click', function (e) {
        $('#' + conf.modalId).modal('hide');
        var userPickerElement = $('#' + conf.modalId).data('userPicker');
        if (conf.multiple) {
            var el = $('#' + conf.modalId + '_result .label');
            var ids = [];
            var names = [];
            var auditHtml="";
            el.each(function (index, item) {
                ids.push($(item).attr('id'));
                names.push($(item).attr('title'));
                //auditNames.push();
                auditHtml+="<li style=\"width:140px;float:left;\">";
                auditHtml+=(index+1)+"."+$(item).attr('title');
                auditHtml+="</li>";
            });
            userPickerElement.children('input[type=hidden]').val(ids.join(','));
            userPickerElement.children('input[type=text]').val(names.join(','));
            if($("#"+conf.auditId).size()>0){
            	$("#"+conf.auditId).html(auditHtml);
            }
            
        } else {
            var el = $('#' + conf.modalId + '_result .label');
            userPickerElement.children('input[type=hidden]').val(el.attr('id'));
            userPickerElement.children('input[type=text]').val(el.attr('title'));
        }
    });
}
