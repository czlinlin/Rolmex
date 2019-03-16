  <%@page contentType="text/html;charset=UTF-8"%>
  <select style="width:100%;display:none"
          onchange="location.href='org-list.do?partyStructTypeId=' + this.value">
      <c:forEach items="${partyStructTypes}" var="item">
          <option value="${item.id}" ${item.id == param.partyStructTypeId ? 'selected' : ''}>${item.name}</option>
      </c:forEach>
  </select>
  <ul id="treePersonMenuAttendanceStatistics" class="ztree"></ul>
<!-- end of sidebar -->

<script type="text/javascript">
    var rootNode="${searchRootNode==null?'':searchRootNode}";
    
    var settingAttendanceStatistics = {
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
            url: "${tenantPrefix}/party/asyncTreeForAuth.do?partyStructTypeId=1&notViewPost=true&notAuth=true",
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
            	if(treeNode.id == 1){
            		return false;
            	}
                window.frames[0].location = '${tenantPrefix}/party/attendance-statistics-list-i.do?partyStructTypeId=1&partyEntityId=' + treeNode.id;
            },
            asyncSuccess: zTreeOnAsyncSuccess,//异步加载成功的fun
            asyncError: zTreeOnAsyncError   //加载错误的fun
            //beforeClick:beforeClick //捕获单击节点之前的事件回调函数
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

    function zTreeOnAsyncSuccess(event, treeId, treeNode, msg) {

    }

    var zNodes = [];

    $(function () {
        $.fn.zTree.init($("#treePersonMenuAttendanceStatistics"), settingAttendanceStatistics, zNodes);
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
