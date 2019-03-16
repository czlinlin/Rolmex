  <%@page contentType="text/html;charset=UTF-8"%>
  <ul id="treePersonMenu" class="ztree"></ul>
<!-- end of sidebar -->

<script type="text/javascript">
    var rootNode="${searchRootNode==null?'':searchRootNode}";
    
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
            url: "${tenantPrefix}/party/asyncReportTreeForAuth.do?partyStructTypeId=1&notViewPost=true&notAuth=true",
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
                $("#reportFrame").contents().find("#parentEntityId").val(treeNode.id);
                $("#reportFrame").contents().find("#btn_Search").click();
            },
            asyncSuccess: zTreeOnAsyncSuccess,//异步加载成功的fun
            asyncError: zTreeOnAsyncError   //加载错误的fun
            //beforeClick:beforeClick //捕获单击节点之前的事件回调函数
        }
    };

    //treeId是treeDemo
    function filter(treeId, parentNode, childNodes) {
        /* if (!childNodes) return null;
        for (var i = 0, l = childNodes.length; i < l; i++) {
            childNodes[i].name = childNodes[i].name.replace('', '');
        } */
        return childNodes;
    }

    function beforeClick(treeId, treeNode) {
        if (!treeNode.isParent) {
            alert("请选择父节点");
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

    $(function () {
        $.fn.zTree.init($("#treePersonMenu"), setting, zNodes);
        //alert("${partyId}");
        // setTimeout("selectNode()", 1000);
    });

    /* function selectNode() {
     //do something
     zTree_Menu = $.fn.zTree.getZTreeObj("treeMenu");
     var node = zTree_Menu.getNodeByParam("id","${partyId}");
     zTree_Menu.selectNode(node,true);//指定选中ID的节点
     } */
</script>
