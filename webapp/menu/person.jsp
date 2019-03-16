<%@ page language="java" pageEncoding="UTF-8" %>

<style type="text/css">
    #accordion .panel-heading {
        cursor: pointer;
    }

    #accordion .panel-body {
        padding: 0px;
    }
</style>

<!-- start of sidebar -->
<div class="panel-group col-md-2" id="accordion" role="tablist" aria-multiselectable="true" style="padding-top:65px;">

    <div class="panel panel-default">
        <div class="panel-heading" role="tab" id="collapse-header-org" data-toggle="collapse" data-parent="#accordion"
             href="#collapse-body-org" aria-expanded="true" aria-controls="collapse-body-org">
            <h4 class="panel-title">
                <i class="glyphicon glyphicon-list"></i>
                在职员工管理
            </h4>
        </div>
        <div id="collapse-body-org" class="panel-collapse collapse ${currentMenu == 'person' ? 'in' : ''}"
             role="tabpanel" aria-labelledby="collapse-header-org">
            <div class="panel-body">
                <select style="width:100%;display:none"
                        onchange="location.href='org-list.do?partyStructTypeId=' + this.value">
                    <c:forEach items="${partyStructTypes}" var="item">
                        <option value="${item.id}" ${item.id == param.partyStructTypeId ? 'selected' : ''}>${item.name}</option>
                    </c:forEach>
                </select>
                <ul id="treeMenu" class="ztree"></ul>
            </div>
        </div>
    </div>
    <%
        if ("2".equals(userId)) {
    %>
    <div class="panel panel-default">
        <div class="panel-heading" role="tab" id="collapse-header-template" data-toggle="collapse"
             data-parent="#accordion" href="#collapse-body-template" aria-expanded="true"
             aria-controls="collapse-body-template">
            <h4 class="panel-title">
                <i class="glyphicon glyphicon-list"></i>
                离职员工管理
            </h4>
        </div>
        <div id="collapse-body-template" class="panel-collapse collapse ${currentMenu == 'quit' ? 'in' : ''}"
             role="tabpanel" aria-labelledby="collapse-header-template">
            <div class="panel-body">
                <ul class="nav nav-pills nav-stacked">
                    <li class="${currentChildMenu == '员工列表' ? 'active' : ''}">
                        <a href="javascript:void(0)"
                           onclick="window.frames[0].location = '${tenantPrefix}/user/person-info-quit-list.do?isSearch=true'">
                            <i class="glyphicon glyphicon-list ${currentChildMenu == '员工列表' ? 'active' : ''}"></i> 员工列表
                        </a>
                    </li>

                </ul>
            </div>
        </div>
    </div>
    <%}%>
    <div class="panel panel-default">
        <div class="panel-heading" role="tab" id="collapse-header-lock" data-toggle="collapse" data-parent="#accordion"
             href="#collapse-body-lock" aria-expanded="true" aria-controls="collapse-body-lock">
            <h4 class="panel-title">
                <i class="glyphicon glyphicon-list"></i>
                解锁管理
            </h4>
        </div>
        <div id="collapse-body-lock" class="panel-collapse collapse ${currentMenu == 'lock' ? 'in' : ''}"
             role="tabpanel" aria-labelledby="collapse-header-lock">
            <div class="panel-body">
                <ul class="nav nav-pills nav-stacked">
                    <li class="${currentChildMenu == '解锁管理' ? 'active' : ''}">
                    <a href="javascript:void(0)"
                       onclick="window.frames[0].location = '${tenantPrefix}/user/account-lock-info-list.do'">
                        <i class="glyphicon glyphicon-list ${currentChildMenu == '解锁管理' ? 'active' : ''}"></i> 解锁管理
                    </a>
                    </li>
                </ul>
            </div>
        </div>
    </div>
    <footer id="m-footer" class="text-center">
        <hr>
        &copy;Rolmex
    </footer>

</div>
<!-- end of sidebar -->


<script type="text/javascript">
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
            url: "${tenantPrefix}/party/asyncTree.do?partyStructTypeId=1&notViewPost=true&notAuth=true",
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
                window.frames[0].location = '${tenantPrefix}/user/person-info-list-i.do?partyStructTypeId=1&partyEntityId=' + treeNode.id;
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

        $.fn.zTree.init($("#treeMenu"), setting, zNodes);
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
