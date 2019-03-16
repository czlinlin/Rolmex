var createUserPickerCopy = function(conf) {
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
+'  <div class="modal-dialog">'
+'    <div class="modal-content">'
+'      <div class="modal-header">'
+'        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>'
+'        <h3>选择用户</h3>'
+'      </div>'
+'      <div class="modal-body">'
+'        <ul id="myTabs" class="nav nav-tabs">'
+'          <li role="presentation" class="active"><a href="#orgCopy">人员</a></li>'
+'          <li role="presentation" ><a href="#postCopy">岗位</a></li>'
/*+'          <li role="presentation"><a href="#alias">常用语</a></li>'
+'          <li role="presentation"><a href="#expr">表达式</a></li>'*/
+'        </ul>'
+'        <div class="tab-content">'
+'          <div role="tabpanel" class="tab-pane active" id="orgCopy">'
+'        <div class="row" id="org">'
+'          <div class="col-md-4">'
+'            <ul id="' + conf.modalId + 'treeMenu" class="ztree"></ul>'
+'          </div>'
+'          <div class="col-md-8">'
+'            <div>'
+'              <label for="' + conf.modalId + '_username" style="display:inline" class="">姓名:</label>'
+'              <input type="text" id="' + conf.modalId + '_username" value="" style="margin-bottom:0px; width:auto; display:inline;" class="form-control">'
+'              <button id="' + conf.modalId + '_search" class="btn btn-default">查询</button>'
+'            </div>'
+'            <div class="panel panel-default " style="max-height:300px;overflow:auto;">'
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
+'          <div role="tabpanel" class="tab-pane " id="postCopy">'
+'        <div class="row" id="post">'
+'          <div class="col-md-4">'
+'            <ul id="' + conf.modalId + 'treeNoPostMenu" class="ztree"></ul>'
+'          </div>'
+'          <div class="col-md-8">'
/*+'            <div>'
+'              <label for="' + conf.modalId + '_usernamePost" style="display:inline" class="">岗位:</label>'
+'              <input type="text" id="' + conf.modalId + '_usernamePost" value="" style="margin-bottom:0px; width:auto; display:inline;" class="form-control">'
+'              <button id="' + conf.modalId + '_searchPost" class="btn btn-default">查询</button>'
+'            </div>'*/
+'            <div class="panel panel-default" style="max-height:300px;overflow:auto;">'
+'              <table id="' + conf.modalId + '_grid" class="table table-hover">'
+'                <thead>'
+'                  <tr style="background-color: #1d82d0;color:#fff;">'
+'                    <th width="10" class="m-table-check">&nbsp;</th>'
+'                    <th>岗位</th>'
+'					  <th>人员</th>'	
+'                  </tr>'
+'                </thead>'
+'                <tbody id="' + conf.modalId + '_bodyPost">'
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
+'          <div role="tabpanel" class="tab-pane" id="alias">'
+'    <div class="panel panel-default">'
+'      <div class="panel-content">'
/*+'  <table id="' + conf.modalId + '_aliasGrid" class="table table-hover">'
+'    <thead>'
+'      <tr style="background-color: #1d82d0;color:#fff;">'
+'        <th width="10" class="m-table-check">&nbsp;</th>'
+'        <th>常用语</th>'
+'      </tr>'
+'    </thead>'
+'    <tbody id="' + conf.modalId + '_aliasBody">'
+'      <tr>'
+'        <td><input id="' + conf.modalId + '_item_alias_1" type="radio" name="selectedItem" class="selectedItem" value="常用语:流程发起人" title="常用语:流程发起人" style="margin-top:0px;"></td>'
+'        <td>常用语:流程发起人</td>'
+'      </tr>'
+'      <tr>'
+'        <td><input id="' + conf.modalId + '_item_alias_1" type="radio" name="selectedItem" class="selectedItem" value="常用语:直接上级" title="常用语:直接上级" style="margin-top:0px;"></td>'
+'        <td>常用语:直接上级</td>'
+'      </tr>'
+'    </tbody>'
+'  </table>'*/
+'      </div>'
+'    </div>'
+'          </div>'
+'          <div role="tabpanel" class="tab-pane" id="expr">'
+'    <div class="panel panel-default">'
+'      <div class="panel-content">'
/*+'  <table id="' + conf.modalId + '_exprGrid" class="table table-hover">'
+'    <thead>'
+'      <tr style="background-color: #1d82d0;color:#fff;">'
+'        <th>表达式</th>'
+'      </tr>'
+'    </thead>'
+'    <tbody id="' + conf.modalId + '_exprBody">'
+'      <tr>'
+'        <td><input id="' + conf.modalId + '_item_expr_1" type="text" name="selectedItem" class="selectedItem form-control" value="${initiator}" title="${initator}" style="margin-top:0px;"></td>'
+'      </tr>'
+'    </tbody>'
+'  </table>'*/
+'      </div>'
+'    </div>'
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

	var doSearch = function(username,partyStructId) {
        $.ajax({
            url: conf.searchUrl,
            data: {
            	username: username,
                partyStructId: partyStructId
            },
            success: function(data) {
                var html = '';
                for (var i = 0; i < data.length; i++) {
                    var item = data[i];
                    var check = "";
                    if (ids.indexOf(item.id) >= 0) {
                        check = "checked";
                    }
                    /*html +=
                      '<tr>'
                        +'<td><input id="' + conf.modalId + '_item_' + i + '" type="' + (conf.multiple ? 'checkbox' : 'radio')
						+ '" class="selectedItem" name="name" value="'
                        + item.id + '" title="' + item.displayName + '"></td>'
                        +'<td><label for="' + conf.modalId + '_item_' + i + '">' + item.displayName + '</label></td>'
                      +'</tr>'*/
                    html +=
                        '<tr>'
                        + '<td><input id="' + conf.modalId + '_item_' + i + '" type="' + (conf.multiple ? 'checkbox' : 'radio')
                        + '" class="selectedItem" name="name" value="'
                        + item.id + '" title="' + item.displayName + '"'+check+'></td>'
                        + '<td><label for="' + conf.modalId + '_item_' + item.id + '">' + item.displayName + '</label></td>'
                        + '<td><label for="' + conf.modalId + '_item_post' + item.id + '">' + item.postName + '</label></td>'
                        + '</tr>'
                }
                $('#' + conf.modalId + '_body').html(html);
            }
        });
	}

	var doSearchChild = function(parentId) {
        $.ajax({
            url: conf.childUrl,
            data: {
                parentId: parentId
            },
            success: function(data) {
                var html = '';
                for (var i = 0; i < data.length; i++) {
                    var item = data[i];
                    var check = "";
                    if (ids.indexOf(item.id) >= 0) {
                        check = "checked";
                    }
                    html +=
                      '<tr>'
                        +'<td><input id="' + conf.modalId + '_item_' + item.id + '" type="' + (conf.multiple ? 'checkbox' : 'radio')
						+ '" class="selectedItem" name="name" value="'
                        + item.id + '" title="' + item.userName + '" '+check+'></td>'
                        +'<td><label for="' + conf.modalId + '_item_' + item.id + '">' + item.userName + '</label></td>'
                        +'<td><label for="' + conf.modalId + '_item_' + item.id + '">' + item.postName + '</label></td>'
                      +'</tr>'
                }
                partyStructId = parentId;
                $('#' + conf.modalId + '_body').html(html);
            }
        });
	}
	
	var doSearchPost = function(parentId) {
        $.ajax({
            url: conf.childPostUrl,
            data: {
                parentId: parentId
            },
            success: function(data) {
                var html = '';
                for (var i = 0; i < data.length; i++) {
                    var item = data[i];
                    var checkPost = "";
                    if (ids.indexOf(item.id) >= 0) {
                    	checkPost = "checked";
                    }
                    html +=
                      '<tr>'
                        +'<td><input id="' + conf.modalId + '_itemPost_' + i + '" type="' + (conf.multiple ? 'checkbox' : 'radio')
						+ '" class="selectedItem" name="name" value="岗位:'
                        + item.id + '" title="' + item.userName + '" '+checkPost+'></td>'
                        +'<td><label for="' + conf.modalId + '_itemPost_' + i + '">' + item.userName + '</label></td>'
                        +'<td><label for="' + conf.modalId + '_itemPost_' + i + '">' + item.userNames + '</label></td>'
                      +'</tr>'
                }
                $('#' + conf.modalId + '_bodyPost').html(html);
            }
        });
	}
	
	var doSearchPostName = function(postName) {
        $.ajax({
            url: conf.childPostUrl,
            data: {
            	postName: postName
            },
            success: function(data) {
                var html = '';
                for (var i = 0; i < data.length; i++) {
                    var item = data[i];
                    var checkPost = "";
                    if (ids.indexOf(item.id) >= 0) {
                    	checkPost = "checked";
                    }
                    html +=
                      '<tr>'
                        +'<td><input id="' + conf.modalId + '_itemPost_' + i + '" type="' + (conf.multiple ? 'checkbox' : 'radio')
						+ '" class="selectedItem" name="name" value="岗位:'
                        + item.id + '" title="' + item.userName + '" '+checkPost+'></td>'
                        +'<td><label for="' + conf.modalId + '_itemPost_' + i + '">' + item.userName + '</label></td>'
                        +'<td><label for="' + conf.modalId + '_itemPost_' + i + '">' + item.userNames + '</label></td>'
                      +'</tr>'
                }
                $('#' + conf.modalId + '_bodyPost').html(html);
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
				autoParam:["id","name"],  
				type:"post",//默认post 
				dataFilter: filterPost  //异步返回后经过Filter   
			},
			view: {
				expandSpeed: "",
				nameIsHTML: true
			},
			callback: {
				onClick: function(event, treeId, treeNode) {
					// console.info(treeNode.id);
					doSearchPost(treeNode.id);
				},
				asyncSuccess: zTreeOnAsyncSuccess,//异步加载成功的fun    
                asyncError: zTreeOnAsyncError   //加载错误的fun 
                // beforeClick:beforeClickPost //捕获单击节点之前的事件回调函数  
			}
		};
		
		//treeId是treeDemo  
        function filterPost(treeId, parentNode, childNodes) {    
            if (!childNodes) return null;    
            for (var i=0, l=childNodes.length; i<l; i++) {    
                childNodes[i].name = childNodes[i].name.replace('','');    
            }    
            return childNodes;    
        }  
        
        function beforeClickPost(treeId,treeNode){  
            if(!treeNode.isParent){   
                return false;  
            }else{  
                return true;  
            }  
        } 
		var zNodesNoPost = [];

		try {
			$.fn.zTree.init($("#" + conf.modalId + "treeNoPostMenu"), settingPost, zNodesNoPost);
		} catch(e) {
			console.error(e);
		}
		
		if (conf.modalId == 'userPicker') {
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
        }
		
		
    $('#' + conf.modalId).data('userPicker', $(this).parent());
    $('#' + conf.modalId).modal();

		// doSearch('');
    });

    // $(document).delegate('#' + conf.modalId + '_body tr', 'click', function(e) {
	//	$('input[type=radio].selectedItem').prop('checked', false);
	//	$(this).find('.selectedItem').prop('checked', true);
    // });

    $(document).delegate('#' + conf.modalId + '_body .selectedItem', 'click', function(e) {
		if (conf.multiple) {
			var el = $(this);
			if (el.prop('checked')) {
				if (ids.indexOf($(this).val()) < 0) {
                    count++;
                    var idcount = ids.split(",");
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
    
    $(document).delegate('#' + conf.modalId + '_bodyPost .selectedItem', 'click', function(e) {
		if (conf.multiple) {
			var el = $(this);
			if (el.prop('checked')) {
				if (ids.indexOf($(this).val()) < 0) {
                    count++;
                    var idcount = ids.split(",");
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
			ids = ids.replace(id, "");
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
		 ids = ids.replace(id, "");
		
	});

	$(document).delegate('#' + conf.modalId + '_search', 'click', function(e) {
		doSearch($('#' + conf.modalId + '_username').val(),partyStructId);
	});

	$(document).delegate('#' + conf.modalId + '_username', 'keypress', function(e) {
		if (e.which == 13) {
			doSearch($('#' + conf.modalId + '_username').val(),partyStructId);
		}
	});
	
	$(document).delegate('#' + conf.modalId + '_searchPost', 'click', function(e) {
		doSearchPostName($('#' + conf.modalId + '_usernamePost').val());
	});

	$(document).delegate('#' + conf.modalId + '_usernamePost', 'keypress', function(e) {
		if (e.which == 13) {
			doSearchPostName($('#' + conf.modalId + '_usernamePost').val());
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
