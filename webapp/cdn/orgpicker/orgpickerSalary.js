var createOrgPicker2 = function(conf) {
	var ids = "";
	var count = 0;
	conf = conf ? conf : {};
	var defaults = {
		modalId: 'orgPicker2',
		chkStyle: 'radio',
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
		+'        <h3>选择机构</h3>'
		+'      </div>'
		+'      <div class="modal-body">'
		+'        <div class="tab-content">'
		+'          <div role="tabpanel" class="tab-pane active" id="org">'
		+'            <div class="row" id="org">'
		+'              <div class="col-md-12">'
		+'                <ul id="' + conf.modalId + 'treeMenu" class="ztree"></ul>'
		+'              </div>'
		+'            </div>'
		+'          </div>'
		+'        </div>'
		+'      </div>'
		+'      <div class="modal-footer">'
		+'        <span id="' + conf.modalId + '_result" style="float:left;display:block,"></span>'
		+'        <a id="' + conf.modalId + '_close" href="#" class="btn" data-dismiss="modal">关闭</a>'
		+'        <a id="' + conf.modalId + '_select" href="#" class="btn btn-primary">选择</a>'
		+'      </div>'
		+'    </div>'
		+'  </div>'
		+'</div>'
		);
    }

    $(document).delegate('.orgPicker2 .input-group-addon', 'click', function(e) {

		var setting = {
			async: {
				enable: true,
				url: conf.treeUrl,
				autoParam: ["id"],
			},
			check: {chkboxType: {"Y": "s", "N": "s"},
				enable: true,
				chkStyle: conf.chkStyle,
				nocheckInherit: false},
			callback: {
				/*onClick: function(event, treeId, treeNode) {
				}*/
				onCheck:zTreeOnCheck,
				onAsyncSuccess: show
			}
		};

		var zNodes = [];

		try {
			$.fn.zTree.init($("#" + conf.modalId + "treeMenu"), setting, zNodes);
		} catch(e) {
			console.error(e);
		}
        setTimeout(function checkNode() {
            /*var treeObj = $.fn.zTree.getZTreeObj(conf.modalId + "treeMenu");
            var node = treeObj.getNodeByParam("id", $("#orgPartyEntityId").val());
            treeObj.checkNode(node, true, true);*/
        	//show();
        }, 1000);
        
		
		
		
		
		
        $('#' + conf.modalId).data('orgPicker2', $(this).parent());
        $('#' + conf.modalId).modal();

    });

    
    $(document).delegate('#' + conf.modalId + '_select', 'click', function(e) {
    	
    	var treeObj = $.fn.zTree.getZTreeObj(conf.modalId + "treeMenu");
    	var nodes = treeObj.getCheckedNodes(true);

        var names="";
        var ids=""
        for(var i=0;i<nodes.length;i++){
	        names+=nodes[i].name + ",";
	        ids += nodes[i].id + ","; //获取选中节点的值
        }

        if (ids != "") {
        	ids = ids.substring(0,ids.length-1);
        }
        if (names != "") {
        	names = names.substring(0,names.length-1);
        }

        $('#' + conf.modalId).modal('hide');
        var orgPickerElement = $('#' + conf.modalId).data('orgPicker2');
        
        if (ids != "") {
			orgPickerElement.children('input[type=hidden]').val(ids);
			orgPickerElement.children('input[type=text]').val(names);
        }else{
        	orgPickerElement.children('input[type=hidden]').val("");
			orgPickerElement.children('input[type=text]').val("");
        }
        
        /*if(fnGetWorkNumber!=undefined&&fnGetWorkNumber!=null){
        	fnGetWorkNumber();
        }*/
    });
    
    
    
    
		function show(){
			var ztreeIds = $("#orgPartyEntityId").val();  
			if(ztreeIds.trim()!=""){  
				var ztree = $.fn.zTree.getZTreeObj(conf.modalId + "treeMenu");  
		        var ztreeId = ztreeIds.split(",");  
		        for(var j=0;j<ztreeId.length;j++){
		        	var treeId = ztreeId[j];
		            var node = ztree.getNodeByParam("id",treeId,null); 
		            node.checked = true;
					ztree.updateNode(node);
					if(node.checked){
				    	if (ids.indexOf(node.id) < 0) {
		                    //count++;
		                    var idcount = node.id;
							var html = '<span class="label label-default" style="float:left;margin:2px 1px 0 1px;" id="' + node.id + '" title="' + node.name + '">' + node.name + '<i class="glyphicon glyphicon-remove" style="cursor:pointer;"></i></span>';
							$('#' + conf.modalId + '_result').append(html);
							ids = ids + "," + node.id;
						}
					}/* else {
						$('#' + conf.modalId + '_result #' + node.id).remove();
						ids = ids.replace(node.id, "");
					}*/
		            
		        }  
			 } 
			
		}
		function zTreeOnCheck(event, treeId, treeNode) {
			console.log(treeNode.id);
		    if(treeNode.checked){
		    	if (ids.indexOf(treeNode.id) < 0) {
                    count++;
                    //var idcount = treeNode.id;
					var html = '<span class="label label-default" style="float:left;margin:2px 1px 0 1px;" id="' + treeNode.id + '" title="' + treeNode.name + '">' + treeNode.name + '<i class="glyphicon glyphicon-remove" style="cursor:pointer;"></i></span>';
					$('#' + conf.modalId + '_result').append(html);
					ids = ids + "," + treeNode.id;
				}
			} else {
				$('#' + conf.modalId + '_result #' + treeNode.id).remove();
				ids = ids.replace(treeNode.id, "");
			}
		    //获取所有子节点 
		    getAllChildrenNodes(treeNode);
		}
		    
		// 递归，获取所有子节点 
		function getAllChildrenNodes(treeNode){ 
			if (treeNode.isParent) { 
				var childrenNodes = treeNode.children; 
				if (childrenNodes) { 
					for (var i = 0; i < childrenNodes.length; i++) { 
						var childrenTreeNode = childrenNodes[i];
						if(childrenTreeNode.checked){
					    	if (ids.indexOf(childrenTreeNode.id) < 0) {
			                    //count++;
			                    var idcount = childrenTreeNode.id;
								var html = '<span class="label label-default" style="float:left;margin:2px 1px 0 1px;" id="' + childrenTreeNode.id + '" title="' + childrenTreeNode.name + '">' + childrenTreeNode.name + '<i class="glyphicon glyphicon-remove" style="cursor:pointer;"></i></span>';
								$('#' + conf.modalId + '_result').append(html);
								ids = ids + "," + childrenTreeNode.id;
							}
						} else {
							$('#' + conf.modalId + '_result #' + childrenTreeNode.id).remove();
							ids = ids.replace(childrenTreeNode.id, "");
						}
						//result += ',' + childrenNodes[i].id; 
						getAllChildrenNodes(childrenNodes[i]); 
					} 
				} 
			} 
		}
	 
		//点击删除当前选中项
		$(document).delegate('.glyphicon-remove', 'click', function(e) {
			var id = $(this).parent().attr('id');
			//$('#' + conf.modalId + '_item_' + id).prop('checked', false);
			 var ztree = $.fn.zTree.getZTreeObj(conf.modalId + "treeMenu");  
			 var node = ztree.getNodeByParam("id",id,null);
			 node.checked = false;
			 ztree.updateNode(node);
			 $(this).parent().remove();
			 ids = ids.replace(id, "");
		});	
    
    
}
