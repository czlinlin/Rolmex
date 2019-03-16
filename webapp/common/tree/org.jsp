<%@page contentType="text/html;charset=UTF-8"%>
<!-- start of sidebar -->
<%-- <div class="panel-group col-md-2" id="accordion" role="tablist" aria-multiselectable="true" style="padding-top:65px;">

  <div class="panel panel-default">
    <div class="panel-heading" role="tab" id="collapse-header-org" data-toggle="collapse" data-parent="#accordion" href="#collapse-body-org" aria-expanded="true" aria-controls="collapse-body-org">
      <h4 class="panel-title">
	    <i class="glyphicon glyphicon-list"></i>
        组织机构
      </h4>
    </div>
    <div id="collapse-body-org" class="panel-collapse collapse ${currentMenu == 'org' ? 'in' : ''}" role="tabpanel" aria-labelledby="collapse-header-org">
      <div class="panel-body"> --%>

		    <select style="width:100%;display:none" onchange="location.href='org-list.do?partyStructTypeId=' + this.value">
			  <c:forEach items="${partyStructTypes}" var="item">
			  <option value="${item.id}" ${item.id == param.partyStructTypeId ? 'selected' : ''}>${item.name}</option>
			  </c:forEach>
			</select>
            <ul id="treeOrgMenu" class="ztree"></ul>
      <!-- </div>
    </div>
  </div>

		<footer id="m-footer" class="text-center">
		  <hr>
		  &copy;Rolmex
		</footer>

</div> -->
      <!-- end of sidebar -->

<script type="text/javascript">
		var setting1 = {
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
				url: "${tenantPrefix}/party/asyncTreeForAuth.do?partyStructTypeId=1&notViewPost=false&notAuth=true",
				autoParam:["id","name"],  
				type:"post",//默认post 
				dataFilter: filter1  //异步返回后经过Filter   
			},
			view: {
				expandSpeed: "",
				nameIsHTML: true
			},
			callback: {
				onClick: function(event, treeId, treeNode) {
					window.frames[0].location = '${tenantPrefix}/party/org-list-i.do?partyStructTypeId=1&partyEntityId=' + treeNode.id;
				},
				asyncSuccess: zTreeOnAsyncSuccess1,//异步加载成功的fun    
                asyncError: zTreeOnAsyncError1   //加载错误的fun    
                // beforeClick:beforeClick //捕获单击节点之前的事件回调函数  
			}
		};

		//treeId是treeDemo  
        function filter1(treeId, parentNode, childNodes) {    
            if (!childNodes) return null;    
            for (var i=0, l=childNodes.length; i<l; i++) {    
                childNodes[i].name = childNodes[i].name.replace('','');    
            }    
            return childNodes;    
        }    
          
        function beforeClick(treeId,treeNode){  
            if(!treeNode.isParent){  
                // alert("请选择父节点");  
                return false;  
            }else{  
                return true;  
            }  
        }  
          
        function zTreeOnAsyncError1(event, treeId, treeNode){    
            alert("异步加载失败!");    
        }    
          
        function zTreeOnAsyncSuccess1(event, treeId, treeNode, msg){    
              
        } 
        
		var zNodes1 =[];

		$(function(){
			
			$.fn.zTree.init($("#treeOrgMenu"), setting1, zNodes1);
		});
		
		function removeNode(id) {
			var zTree = $.fn.zTree.getZTreeObj("treeOrgMenu");
			var node = zTree.getNodeByParam("id",id);  
			zTree.removeNode(node);

		}
		
</script>