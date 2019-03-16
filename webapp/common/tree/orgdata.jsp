  <%@page contentType="text/html;charset=UTF-8"%>
  	
  	<div id="divLoading" style="display:none;">
  		<img alt="加载中..." src="${cdnPrefix}/mossle/img/loading.gif" style="width:24px;height:24px;"/>
  	</div>
    <ul id="treeMenuOrgData" class="ztree"></ul>
<!-- end of sidebar -->

<script type="text/javascript">
	var loading=$("#divLoading");
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
            url: "${tenantPrefix}/party/asyncTreeCycle.do?partyStructTypeId=1&notViewPost=true&notAuth=true&partyid=<%=request.getParameter("id")%>",
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
                //window.frames[0].location = '${tenantPrefix}/user/person-info-list-i.do?partyStructTypeId=1&partyEntityId=' + treeNode.id;
            },
            //beforeAsync : ztreeBeforeAsync,
            onAsyncSuccess: zTreeOnAsyncSuccess,//异步加载成功的fun
            onAsyncError: zTreeOnAsyncError   //加载错误的fun
            //beforeClick:beforeClick //捕获单击节点之前的事件回调函数
        },
        check: {
            enable: true
        }
    };

    //treeId是treeDemo
    function filter(treeId, parentNode, childNodes) {
        if (!childNodes) return null;
        for (var i = 0, l = childNodes.length; i < l; i++) {
            childNodes[i].name = childNodes[i].name.replace('', '');
        }
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

    function zTreeOnAsyncSuccess(event, treeId, treeNode,msg) {
    	loading.hide();
    }

    var zNodes = [];

    $(function () {
    	loading.show();
        $.fn.zTree.init($("#treeMenuOrgData"), setting, zNodes).expandAll(true);
    });
</script>
