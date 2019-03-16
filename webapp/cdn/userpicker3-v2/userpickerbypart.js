var createUserPicker = function(conf) {
	conf = conf ? conf : {};
	var defaults = {
		modalId: 'userPicker',
		multiple: false,
		searchUrl: '/mossle-web-user/default/rs/party/searchUser',
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
+'  <div class="modal-dialog">'
+'    <div class="modal-content">'
+'      <div class="modal-header">'
+'        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>'
+'        <h3>选择人员</h3>'
+'      </div>'
+'      <div class="modal-body">'
/*
+'        <ul id="myTabs" class="nav nav-tabs">'
+'          <li role="presentation" class="active"><a href="#org">组织机构</a></li>'
+'        </ul>'
*/
+'        <div class="tab-content">'
+'          <div role="tabpanel" class="tab-pane active" id="org">'
+'        <div class="row" id="org">'
+'          <div class="col-md-4">'
+'            <ul id="' + conf.modalId + 'treeMenu" class="ztree"></ul>'
+'          </div>'
+'          <div class="col-md-8">'
/*
+'            <div>'
+'              <label for="' + conf.modalId + '_username" style="display:inline" class="">账号:</label>'
+'              <input type="text" id="' + conf.modalId + '_username" value="" style="margin-bottom:0px; width:auto; display:inline;" class="form-control">'
+'              <button id="' + conf.modalId + '_search" class="btn btn-default">查询</button>'
+'            </div>'
*/
+'            <div class="panel panel-default" style="max-height:300px;overflow:auto;">'
/*
+'              <div class="panel-heading">'
+'                <h3 class="panel-title">人员</h3>'
+'              </div>'*/
+'              <table id="' + conf.modalId + '_grid" class="table table-hover">'
+'                <thead>'
+'                  <tr style="background-color: #1d82d0;color:#fff;">'
+'                    <th width="10" class="m-table-check">&nbsp;</th>'
+'                    <th>姓名</th>'
+'                    <th>岗位</th>'
+'                  </tr>'
+'                </thead>'
+'                <tbody id="' + conf.modalId + '_body">'
/*    
+'                  <tr>'
+'                    <td><input id="' + conf.modalId + '_item_1" type="' + (conf.multiple ? 'checkbox' : 'radio') + '" name="selectedItem" class="selectedItem" value="1" title="admin" style="margin-top:0px;"></td>'
+'                    <td>admin</td>'
+'                  </tr>'
+'                  <tr>'
+'                    <td><input id="' + conf.modalId + '_item_2" type="' + (conf.multiple ? 'checkbox' : 'radio') + '" name="selectedItem" class="selectedItem" value="2" title="user" style="margin-top:0px;"></td>'
+'                    <td>user</td>'
+'                  </tr>'
*/    
+'                </tbody>'
+'              </table>'
+'            </div>'
+'          </div>'
+'        </div>'
+'          </div>'
+'        </div>'
+'      </div>'
+'      <div class="modal-footer">'
+'        <span id="' + conf.modalId + '_result" style="float:left;"></span>'
+'        <a id="' + conf.modalId + '_close" href="#" class="btn" data-dismiss="modal">关闭</a>'
+'        <a id="' + conf.modalId + '_select" href="#" class="btn btn-primary">选择</a>'
+'      </div>'
+'    </div>'
+'  </div>'
+'</div>');

		$('#myTabs a').click(function (e) {
		  e.preventDefault()
		  $(this).tab('show')
		});

    }

	var doSearch = function(username) {
        $.ajax({
            url: conf.searchUrl,
            data: {
                username: username
            },
            success: function(data) {
                var html = '';
                for (var i = 0; i < data.length; i++) {
                    var item = data[i];
                    html +=
                      '<tr>'
                        +'<td><input id="' + conf.modalId + '_item_' + i + '" type="' + (conf.multiple ? 'checkbox' : 'radio')
						+ '" class="selectedItem" name="name" value="'
                        + item.id + '" title="' + item.displayName + '"></td>'
                        +'<td><label for="' + conf.modalId + '_item_' + i + '">' + item.displayName + '</label></td>'
                      +'</tr>'
                }
                $('#' + conf.modalId + '_body').html(html);
            }
        });
	}

	var doSearchChild = function(parentId) {
		// alert(conf.childUrl);
        $.ajax({
            url: conf.childUrl,
            data: {
                parentId: parentId
            },
            success: function(data) {
                var html = '';
                for (var i = 0; i < data.length; i++) {
                    var item = data[i];
                    html +=
                      '<tr>'
                        +'<td><input id="' + conf.modalId + '_item_' + item.id + '" type="' + (conf.multiple ? 'checkbox' : 'radio')
						+ '" class="selectedItem" name="name" value="'
                        + item.id + '" title="' + item.userName + '"></td>'
                        +'<td><label for="' + conf.modalId + '_item_' + item.id + '">' + item.userName + '</label></td>'
                        +'<td><label for="' + conf.modalId + '_item_post' + item.id + '">' + item.postName + '</label></td>'
                      +'</tr>'
                }
                $('#' + conf.modalId + '_body').html(html);
            }
        });
	}

    $(document).delegate('.userPicker .input-group-addon', 'click', function(e) {

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
				autoParam:["id","name"],  
				type:"post",//默认post 
				dataFilter: filter  //异步返回后经过Filter   
			},
			view: {
				expandSpeed: "",
				nameIsHTML: true
			},
			callback: {
				onClick: function(event, treeId, treeNode) {
					// console.info(treeNode.id);
					doSearchChild(treeNode.id);
					
					//doSearch('',treeNode.id);
				},
				asyncSuccess: zTreeOnAsyncSuccess,//异步加载成功的fun    
                asyncError: zTreeOnAsyncError   //加载错误的fun 
                // beforeClick:beforeClick //捕获单击节点之前的事件回调函数 
			}
		};

		//treeId是treeDemo  
        function filter(treeId, parentNode, childNodes) {    
            if (!childNodes) return null;    
            for (var i=0, l=childNodes.length; i<l; i++) {    
            	if (childNodes[i] != null) {
            		childNodes[i].name = childNodes[i].name.replace('', '');
            	}
            }    
            return childNodes;    
        }    
          
        function beforeClick(treeId,treeNode){  
            if(!treeNode.isParent){   
                return false;  
            }else{  
                return true;  
            }  
        }  
          
        function zTreeOnAsyncError(event, treeId, treeNode){    
            alert("异步加载失败!");    
        }    
          
        function zTreeOnAsyncSuccess(event, treeId, treeNode, msg){    
              
        } 
        
		var zNodes = [];

		try {
			$.fn.zTree.init($("#" + conf.modalId + "treeMenu"), setting, zNodes);
		} catch(e) {
			console.error(e);
		}

        $('#' + conf.modalId).data('userPicker', $(this).parent());
        $('#' + conf.modalId).modal();

		//doSearch('');
    });

    // $(document).delegate('#' + conf.modalId + '_body tr', 'click', function(e) {
	//	$('input[type=radio].selectedItem').prop('checked', false);
	//	$(this).find('.selectedItem').prop('checked', true);
    // });

    $(document).delegate('#' + conf.modalId + '_body .selectedItem', 'click', function(e) {
		if (conf.multiple) {
			var el = $(this);
			if (el.prop('checked')) {
				var html = '&nbsp;<span class="label label-default" id="' + $(this).val() + '" title="' + $(this).attr('title') + '">' + $(this).attr('title') + '<i class="glyphicon glyphicon-remove" style="cursor:pointer;"></i></span>';
				$('#' + conf.modalId + '_result').append(html);
			} else {
				$('#' + conf.modalId + '_result #' + el.val()).remove();
			}
		} else {
			var html = '<span class="label label-default" id="' + $(this).val() + '" title="' + $(this).attr('title') + '">' + $(this).attr('title') + '<i class="glyphicon glyphicon-remove" style="cursor:pointer;"></i></span>';
			$('#' + conf.modalId + '_result').html(html);
		}
	});

    $(document).delegate('#' + conf.modalId + '_aliasBody .selectedItem', 'click', function(e) {
		var html = '<span class="label" id="' + $(this).val() + '" title="' + $(this).attr('title') + '">' + $(this).attr('title') + '<i class="icon-minus-sign" style="cursor:pointer;"></i></span>';
		$('#' + conf.modalId + '_result').html(html);
	});

    $(document).delegate('#' + conf.modalId + '_exprBody .selectedItem', 'blur', function(e) {
		var html = '<span class="label" id="' + $(this).val() + '" title="' + $(this).val() + '">' + $(this).val() + '<i class="icon-minus-sign" style="cursor:pointer;"></i></span>';
		$('#' + conf.modalId + '_result').html(html);
	});

	$(document).delegate('.glyphicon-remove', 'click', function(e) {
		var id = $(this).parent().attr('id');
		$('#' + conf.modalId + '_item_' + id).prop('checked', false);
		$(this).parent().remove();
	});

	$(document).delegate('#' + conf.modalId + '_search', 'click', function(e) {
		doSearch($('#' + conf.modalId + '_username').val());
	});

	$(document).delegate('#' + conf.modalId + '_username', 'keypress', function(e) {
		if (e.which == 13) {
			doSearch($('#' + conf.modalId + '_username').val());
		}
	});

    $(document).delegate('#' + conf.modalId + '_select', 'click', function(e) {
        $('#' + conf.modalId).modal('hide');
        var userPickerElement = $('#' + conf.modalId).data('userPicker');
		if (conf.multiple) {
			var el = $('#' + conf.modalId + '_result .label');
			var ids = [];
			var names = [];
			el.each(function(index, item) {
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
