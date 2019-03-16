var createOrgPicker2 = function(conf) {
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
		+'        <span id="' + conf.modalId + '_result" style="float:left;"></span>'
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
				onClick: function(event, treeId, treeNode) {
				}
			}
		};

		var zNodes = [];

		try {
			$.fn.zTree.init($("#" + conf.modalId + "treeMenu"), setting, zNodes);
		} catch(e) {
			console.error(e);
		}
        setTimeout(function checkNode() {
            var treeObj = $.fn.zTree.getZTreeObj(conf.modalId + "treeMenu");
            var node = treeObj.getNodeByParam("id", $("#_task_name_key").val());
            treeObj.checkNode(node, true, true);
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
        }
        
        if(fnGetWorkNumber!=undefined&&fnGetWorkNumber!=null){
        	fnGetWorkNumber();
        }
    });
}
