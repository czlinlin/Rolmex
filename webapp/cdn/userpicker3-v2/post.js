var createUserPicker = function (conf) {
    var ids = "";
    var count = 0;
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
            + '        <h3>选择岗位</h3>'
            + '      </div>'
            + '      <div class="modal-body">'
            + '        <ul id="myTabs" class="nav nav-tabs">'
            + '          <li role="presentation" class="active"><a href="#post">岗位</a></li>'
            + '        </ul>'
            + '        <div class="tab-content">'

            + '          <div role="tabpanel" class="tab-pane  active" id="post">'
            + '        <div class="row" id="org">'
            + '          <div class="col-md-4">'
            + '            <ul id="' + conf.modalId + 'treeNoPostMenu" class="ztree"></ul>'
            + '          </div>'
            + '          <div class="col-md-8">'
            /*+'            <div>'
             +'              <label for="' + conf.modalId + '_username" style="display:inline" class="">账号:</label>'
             +'              <input type="text" id="' + conf.modalId + '_username" value="" style="margin-bottom:0px; width:auto; display:inline;" class="form-control">'
             +'              <button id="' + conf.modalId + '_search" class="btn btn-default">查询</button>'
             +'            </div>'*/
            + '            <div class="panel panel-default" style="max-height:300px;overflow:auto;">'
            + '              <div class="panel-heading">'
            + '                <h3 class="panel-title">岗位名称</h3>'
            + '              </div>'
            + '              <table id="' + conf.modalId + '_grid" class="table table-hover">'
            /*+'                <thead>'
             +'                  <tr>'
             +'                    <th width="10" class="m-table-check">&nbsp;</th>'
             +'                    <th>岗位名称</th>'
             +'                  </tr>'
             +'                </thead>'*/
            + '                <tbody id="' + conf.modalId + '_bodyPost">'

            + '                </tbody>'
            + '              </table>'
            + '            </div>'
            + '          </div>'
            + '        </div>'
            + '          </div>'

            + '          <div role="tabpanel" class="tab-pane" id="expr">'
            + '    <div class="panel panel-default">'

            + '      <div class="panel-content">'
            + '  <table id="' + conf.modalId + '_exprGrid" class="table table-hover">'

            + '    <tbody id="' + conf.modalId + '_exprBody">'
            + '      <tr>'
            + '        <td><input id="' + conf.modalId + '_item_expr_1" type="text" name="selectedItem" class="selectedItem form-control" value="${initiator}" title="${initator}" style="margin-top:0px;"></td>'
            + '      </tr>'
            + '    </tbody>'
            + '  </table>'
            + '      </div>'
            + '    </div>'
            + '          </div>'
            + '        </div>'
            + '      </div>'
            + '      <div class="modal-footer">'
            + '        <span id="' + conf.modalId + '_result" style="float:left;"></span>'
            + '        <a id="' + conf.modalId + '_close" href="#" class="btn" data-dismiss="modal">关闭</a>'
            + '        <a id="' + conf.modalId + '_select" href="#" class="btn btn-primary">选择</a>'
            + '      </div>'
            + '    </div>'
            + '  </div>'
            + '</div>');

        $('#myTabs a').click(function (e) {
            e.preventDefault()
            $(this).tab('show')
        });

    }
    var doSearchPost = function (parentId) {
        $.ajax({
            url: conf.childPostUrl,
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
                        + '<td><input id="' + conf.modalId + '_itemPost_' + i + '" type="' + (conf.multiple ? 'checkbox' : 'radio')
                        + '" class="selectedItem" name="name" value="'
                        + item.id + '" title="' + item.userName + '"' + check + '></td>'
                        + '<td><label for="' + conf.modalId + '_itemPost_' + i + '">' + item.userName + '</label></td>'
                        + '</tr>'
                }
                var obj = $("#" + conf.modalId + "_item_")

                $('#' + conf.modalId + '_bodyPost').html(html);
            }
        });
    }

    $(document).delegate('.userPicker .input-group-addon', 'click', function (e) {

        var multiple = $(this).parent().data('multiple');
        if (multiple) {
            conf.multiple = true;
        }

        var settingPost = {
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
                url: conf.treeNoPostUrl,
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
                    // console.info(treeNode.id);
                    doSearchPost(treeNode.id);
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

        var zNodesNoPost = [];

        try {
            $.fn.zTree.init($("#" + conf.modalId + "treeNoPostMenu"), settingPost, zNodesNoPost);
        } catch (e) {
            console.error(e);
        }
        $('#' + conf.modalId + '_bodyPost').empty();
        var strIds = "";
        ids = $("#_task_name_key").val();
        strIds = ids;
        var names = $("#postName").val();
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
        $('#' + conf.modalId).data('userPicker', $(this).parent());
        $('#' + conf.modalId).modal();

    });

    ids = $("#postId").val();
    $(document).delegate('#' + conf.modalId + '_body .selectedItem', 'click', function (e) {
        if (conf.multiple) {
            var el = $(this);
            if (el.prop('checked')) {
                if (ids.indexOf($(this).val()) < 0) {
                    count++;
                    var idcount = ids.split(",");
                    if ((idcount.length) % 5 == 0) {
                        $('#' + conf.modalId + '_result').append("<br>");
                    }

                    var html = '&nbsp;<span class="label label-default" id="' + $(this).val() + '" title="' + $(this).attr('title') + '">' + $(this).attr('title') + '<i class="glyphicon glyphicon-remove" style="cursor:pointer;"></i></span>';
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

    $(document).delegate('#' + conf.modalId + '_bodyPost .selectedItem', 'click', function (e) {
        if (conf.multiple) {
            var el = $(this);
            if (el.prop('checked')) {
                count++;
                var idcount = ids.split(",");
                if ((idcount.length) % 5 == 0) {
                    $('#' + conf.modalId + '_result').append("<br>");
                }

                var html = '&nbsp;<span class="label label-default" id="' + $(this).val() + '" title="' + $(this).attr('title') + '">' + $(this).attr('title') + '<i class="glyphicon glyphicon-remove" style="cursor:pointer;"></i></span>';
                $('#' + conf.modalId + '_result').append(html);
                ids = ids + "," + $(this).val();
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

    $(document).delegate('#' + conf.modalId + '_select', 'click', function (e) {
        $('#' + conf.modalId).modal('hide');
        var userPickerElement = $('#' + conf.modalId).data('userPicker');
        if (conf.multiple) {
            var el = $('#' + conf.modalId + '_result .label');
            var ids = [];
            var names = [];
            el.each(function (index, item) {
                ids.push($(item).attr('id'));
                names.push($(item).attr('title'));
            });
            userPickerElement.children('input[type=hidden]').val(ids.join(','));
            userPickerElement.children('input[type=text]').val(names.join(','));
        } else {
            var el = $('#' + conf.modalId + '_result .label');
            userPickerElement.children('input[type=hidden]').val(el.attr('id'));
            userPickerElement.children('input[type=text]').val(el.attr('title'));
        }
    });
}
