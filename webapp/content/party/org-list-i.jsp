<%@page contentType="text/html;charset=UTF-8"%>
<%@include file="/taglibs.jsp"%>
<%pageContext.setAttribute("currentHeader", "org");%>
<%pageContext.setAttribute("currentMenuName", "人力资源");%>
<%pageContext.setAttribute("currentMenu", "org");%>
<!doctype html>
<html lang="en">

  <head>
    <%@include file="/common/meta.jsp"%>
    <title><spring:message code="dev.org.list.title" text="麦联"/></title>
    <%@include file="/common/s3.jsp"%>
    <script type="text/javascript" src="${cdnPrefix}/bootbox/bootbox.min1.js"></script>
    <script type="text/javascript" src="${cdnPrefix}/userpicker3-v2/postForPersonInFo.js"></script>
    <style>
    	.modal{top:50px;}
    </style>
    <script type="text/javascript">
		var config = {
		    id: 'orgGrid',
		    pageNo: ${page.pageNo},
		    pageSize: ${page.pageSize},
		    totalCount: ${page.totalCount},
		    resultSize: ${page.resultSize},
		    pageCount: ${page.pageCount},
		    orderBy: '${page.orderBy == null ? "" : page.orderBy}',
		    asc: ${page.asc},
		    params: {
		        'partyStructTypeId': '${param.partyStructTypeId}',
		        'partyEntityId': '${param.partyEntityId}',
		        'name': '${param.name}'
		    },
			selectedItemClass: 'selectedItem',
			gridFormId: 'orgGridForm',
			exportUrl: 'org-export.do',
            setPositionUrl:"${tenantPrefix}/rs/party/person-info-setpositionno",
            getPositionUrl:"${tenantPrefix}/rs/party/person-info-getpositionno"
		};

		var table;

		$(function() {
			window.parent.$.showMessage($('#m-success-tip-message').html(), {
                position: 'top',
                size: '50',
                fontSize: '20px'
            });
			
			table = new Table(config);
		    table.configPagination('.m-pagination');
		    table.configPageInfo('.m-page-info');
		    table.configPageSize('.m-page-size');
		    
		    //岗位
			createUserPickerForPersonInfo({
			    modalId: 'userPickerForPersonInfo',
			    targetId: 'PersonInfoDiv', //这个是点击哪个 会触发弹出窗口
			    showExpression: true,
			    multiple: true,
			    searchUrl: '${tenantPrefix}/rs/user/search',
			    treeNoPostUrl: '${tenantPrefix}/party/asyncTree.do?partyStructTypeId=1&notViewPost=true&notAuth=false',
			    childPostUrl: '${tenantPrefix}/rs/party/searchPost'
			});
		});

		function search() {
			$("#mainframe").attr('src','org-list-i.do');
		}
		
		function addAdmin(partyStructTypeId, partyEntityId, partyTypeId, level) {
			$("#mainframe").attr('src','org-admin-list.do?partyStructTypeId=' + partyStructTypeId + '&partyEntityId=' + 
					partyEntityId + '&partyTypeId=' + partyTypeId + '&level=' + level);
		}

		// 删除
        function removeOrg(selectedItem, partyStructTypeId, partyEntityId) {
            if (confirm('确定要删除此项吗？')) {
            	$.ajax({      
                    url: '${tenantPrefix}/rs/party/removeParty',      
                    datatype: "json",
                    data:{"selectedItem": selectedItem},
                    type: 'get',      
                    success: function (e) {
                    	//成功后回调   
                    	if (e.name == "删除成功") {
                    		alert(e.name);
                    		window.parent.removeNode(e.id);
                    		// var zTree = $.fn.zTree.getZTreeObj("treeMenu");
                    		
                    		$('#orgGridForm').attr('action', 'org-list-i.do?partyStructTypeId=1&partyEntityId=' + e.pid );
                            $('#orgGridForm').submit(); 
                    		
                    	} else {
                            alert(e.name); 
                    	}
                    	     
                    },      
                    error: function(e){      
                    	//失败后回调      
                        alert("服务器请求失败");      
                    }/* ,      
                    beforeSend: function(){      
                    //发送请求前调用，可以放一些"正在加载"之类额话      
                        alert("正在加载");           
            		} */
               }); 

                return true;
            } else {
                return false;
            }
        }
    </script>
  </head>

  <body>
    
    <div class="row-fluid">
	  
	  <c:if test="${not empty flashMessages}">
		<div id="m-success-tip-message" style="display:none;">
		  <ul>
		  <c:forEach items="${flashMessages}" var="item">
		    <c:if test="${item != ''}">
		    	<li>${item}</li>
		    </c:if>
		  </c:forEach>
		  </ul>
		</div>
	 </c:if>
	
	  <!-- start of main -->
      <section id="m-main" class="col-md-12" style="padding-top:65px;">
		
		<div class="panel panel-default">
		  <div class="panel-heading">
			<i class="glyphicon glyphicon-list"></i>
		    查询
			<div class="pull-right ctrl">
			  <a class="btn btn-default btn-xs"><i id="orgSearchIcon" class="glyphicon glyphicon-chevron-up"></i></a>
		    </div>
		  </div>
  		  <div class="panel-body">
			  <form name="orgForm" method="post" action="" class="form-inline" >
			    <input type="hidden" name="partyStructTypeId" value="${param.partyStructTypeId}">
			    <input type="hidden" name="partyEntityId" value="${param.partyEntityId}">
			    <label for="org_name"><spring:message code='org.org.list.search.name' text='名称'/>:</label>
			    <input type="text" id="org_name" name="name" value="${param.name}" class="form-control">
				<button class="btn btn-default a-search" onclick="search()">查询</button>&nbsp;
			  </form>
		  </div>
	   </div>
	   
       <div style="margin-bottom: 20px;">
	    <div class="pull-left">
	      <div class="btn-group" role="group">
	      <%-- <c:if test="${adminId == true && partyTypeId == 5}">
	      	<button class="btn btn-default a-insert" onclick="location.href='org-input.do?partyStructTypeId=${partyStructTypeId}&partyEntityId=${partyEntityId}&partyTypeId=1&level=${level}'">关联岗位</button>
	      </c:if> --%>
		    <c:forEach items="${childTypes}" var="item">	
		        <c:if test="${item.name == '人员' && partyType == '2'}">
		        	<c:if test="${partyTypeId == 5}">
		        		<tags:buttonOpteration 
                            	opterNames="关联${item.name}" 
                            	buttonTypes="button" 
                            	opterTypes="href" 
                            	opterParams="org-input.do?partyStructTypeId=${partyStructTypeId}&partyEntityId=${partyEntityId}&partyTypeId=${item.id}&level=${level}"/>
		    			<%-- <button class="btn btn-default a-insert" onclick="location.href='org-input.do?partyStructTypeId=${partyStructTypeId}&partyEntityId=${partyEntityId}&partyTypeId=${item.id}&level=${level}'">关联${item.name}</button> --%>
		    		</c:if>
		    		<c:if test="${partyTypeId != 5}">
		    			<tags:buttonOpteration 
                            	opterNames="新建${item.name}" 
                            	buttonTypes="button" 
                            	opterTypes="href" 
                            	opterParams="org-input.do?partyStructTypeId=${partyStructTypeId}&partyEntityId=${partyEntityId}&partyTypeId=${item.id}&level=${level}"/>
		    			<%-- <button class="btn btn-default a-insert" onclick="location.href='org-input.do?partyStructTypeId=${partyStructTypeId}&partyEntityId=${partyEntityId}&partyTypeId=${item.id}&level=${level}'">新建${item.name}</button> --%>
		    		</c:if>
		        </c:if>
		    	<c:if test="${item.name != '人员' }">
		    		<tags:buttonOpteration 
                            	opterNames="新建${item.name}" 
                            	buttonTypes="button" 
                            	opterTypes="href" 
                            	opterParams="org-input.do?partyStructTypeId=${partyStructTypeId}&partyEntityId=${partyEntityId}&partyTypeId=${item.id}&level=${level}"/>
		    		<%-- <button class="btn btn-default a-insert" onclick="location.href='org-input.do?partyStructTypeId=${partyStructTypeId}&partyEntityId=${partyEntityId}&partyTypeId=${item.id}&level=${level}'">新建${item.name}</button> --%>
		    	</c:if>
		    </c:forEach>
		  </div>
		  <c:if test="${viewManage}">
		  	<tags:buttonOpteration 
                       	opterNames="管理者" 
                       	buttonTypes="button" 
                       	opterTypes="href" 
                       	opterParams="org-admin-list.do?partyStructTypeId=${partyStructTypeId}&partyEntityId=${partyEntityId}&partyTypeId=${item.id}&level=${level}"/>
		  	<%-- <a href="org-admin-list.do?partyStructTypeId=${partyStructTypeId}&partyEntityId=${partyEntityId}&partyTypeId=${item.id}&level=${level}" class="btn btn-default">管理者</a> --%>
		  	<%-- <a onclick="addAdmin(${partyStructTypeId},${partyEntityId},'',${level})" class="btn btn-default">管理者</a>--%>
		  </c:if>
		  
		</div>

		<div class="pull-right">
		  每页显示
		  <select class="m-page-size form-control" style="display:inline;width:auto;">
		    <option value="10">10</option>
		    <option value="20">20</option>
		    <option value="50">50</option>
		  </select>
		  条
        </div>

	    <div class="clearfix"></div>
	  </div>

<form id="orgGridForm" name="orgGridForm" method='post' action="org-remove.do" class="m-form-blank">
      <div class="panel panel-default">
        <div class="panel-heading">
		  <i class="glyphicon glyphicon-list"></i>
		  <spring:message code="scope-info.scope-info.list.title" text="列表"/>
		</div>


  <table id="orgGrid" class="table table-hover">
    <thead>
      <tr>
	    <!--
        <th width="10" class="table-check"><input type="checkbox" name="checkAll" onchange="toggleSelectedItems(this.checked)"></th>
		-->
        <th class="sorting" name="id">编号</th>
        <th class="sorting" name="id">用户名/缩写</th>
        <th class="sorting" name="name">岗位编号</th>
        <th class="sorting" name="name">名称</th>
        <th class="sorting" name="partyType">类型</th>
		<!--
        <th class="sorting" name="admin">管理</th>-->
        <th class="sorting" name="admin">岗位</th>
        <th width="120">是否显示</th>
        <th width="150">操作</th>
      </tr>
    </thead>

    <tbody>
      <c:forEach items="${page.result}" var="item">
      	
	      <tr>
		    <!--
	        <td><input type="checkbox" class="selectedItem" name="selectedItem" value="${item.childEntity.id}"></td>
			-->
	        <td>
	        	<c:if test="${item.childEntity.partyType.name == '人员'}">
	        		${item.childEntity.shortName}
	        	</c:if>
	        </td>
	        <td>
	        	<c:if test="${item.childEntity.partyType.name != '人员'}">
	        		${item.childEntity.shortName}
	        	</c:if>
	        	<c:if test="${item.childEntity.partyType.name == '人员'}">
	        		<tags:userName userId="${item.childEntity.id}"/>
	        	</c:if>
	        </td>
	        <td>
	        	<c:if test="${item.childEntity.partyType.name == '岗位'}">
	        		<tags:partyAttr partyId="${item.childEntity.id}"/></td>
	        	</c:if>
	        <td>${item.childEntity.name}</td>
	        <td>${item.childEntity.partyType.name}</td>
			<!--
	        <td>${item.admin == 1}</td>
			-->
	        <td>
			  <c:forEach items="${item.childEntity.parentStructs}" var="childStruct">
			
			    <c:if test="${childStruct.parentEntity.partyType.id==5 && childStruct.partyStructType.id == 4}">
				    <c:if test="${childStruct.parentEntity.isDisplay == 1}">
				    ${childStruct.parentEntity.name}&nbsp;&nbsp;
				    </c:if>
				</c:if>
			  </c:forEach>
			</td>
			  <td>
				   <c:if test="${item.childEntity.partyType.type != 1}">
								 <c:if test="${item.childEntity.isDisplay == 1}">
					         	 	是	
					        	</c:if>
					        	
					        	 <c:if test="${item.childEntity.isDisplay == 0}">
					         	 	否	
					        	</c:if>
					</c:if>
			 </td>
	        <td>
	          <c:if test="${item.childEntity.partyType.type==1}">
			    <!-- <a href="org-position-input.do?partyStructTypeId=${partyStructTypeId}&partyEntityId=${item.childEntity.id}&partyTypeId=5" class="a-remove">配置职位</a> -->
			  </c:if>
			  <c:if test="${item.childEntity.partyType.id == 5}">
		  	  	<tags:buttonOpteration 
                       	opterNames="岗位属性" 
                       	buttonTypes="a" 
                       	opterTypes="click" 
                       	opterParams="fnSetPosition(${item.childEntity.id})"/>
			  </c:if>
			  <c:if test="${item.childEntity.partyType.type != 1}">
			  	<tags:buttonOpteration 
                       	opterNames="编辑|删除|查看日志" 
                       	buttonTypes="a|a|a" 
                       	opterTypes="href|click|href" 
                       	opterParams="org-input-update.do?id=${item.id}|removeOrg('${item.id}','${partyStructTypeId}','${partyEntityId}')|org-log.do?id=${item.childEntity.id}"/>
                
<%--                 <a href="org-log.do?id=${item.childEntity.id}">查看日志</a>     --%>
                   	
			    <%-- <a href="org-input-update.do?id=${item.id}" class="a-remove">编辑</a>			  	
			  	<a href="javascript:void(0)" onclick ="removeOrg('${item.id}','${partyStructTypeId}','${partyEntityId}')" class="a-remove">删除</a> --%>
			  </c:if>
			  <c:if test="${partyTypeId == 5}">
			  	<tags:buttonOpteration 
                       	opterNames="删除关联人员" 
                       	buttonTypes="a" 
                       	opterTypes="href" 
                       	opterParams="org-remove.do?selectedItem=${item.id}&partyStructTypeId=${partyStructTypeId}&partyEntityId=${partyEntityId}"/>
			  	<%-- <a href="org-remove.do?selectedItem=${item.id}&partyStructTypeId=${partyStructTypeId}&partyEntityId=${partyEntityId}" class="a-remove">删除人员</a> --%>
			  </c:if>
			  
			 </td>
	      </tr>
	     
      </c:forEach>
    </tbody>
  </table>
      </div>
</form>
	  <div>
	    <div class="m-page-info pull-left">
		  共100条记录 显示1到10条记录
		</div>
		<div class="btn-group m-pagination pull-right">
		  <button class="btn btn-default">&lt;</button>
		  <button class="btn btn-default">1</button>
		  <button class="btn btn-default">&gt;</button>
		</div>

	    <div class="clearfix"></div>
      </div>

      <div class="m-spacer"></div>

      </section>
	  <!-- end of main -->
	</div>
</body>
<script type="text/javascript">
		var fnIsRealchange=function(){
			var position=$("#isRealPosition").val();
			if(position=="1"){
				$("#tr_realPosition").show();
			}
			else{
				$("#tr_realPosition").hide();
			}
		}
    	var fnSetPosition=function(positionId){
    		$.get(config.getPositionUrl, {postId:positionId}, function (position) {
                 if (position == undefined || position == null || position == "") {
                     bootbox.alert("获取数据失败！");
                     return false;
                 }
                 if (position.code == 200) {
                	var positionNo="";
                	var isRealPosition="0";
                	var postId="";
                	var postName="";
                	if(position.data.positionNo) 
                		positionNo=position.data.positionNo;
                	if(position.data.isRealPosition) 
                		isRealPosition=position.data.isRealPosition;
                	if(position.data.positionRealIds) 
                		postId=position.data.positionRealIds;
                	if(position.data.postName) 
                		postName=position.data.postName;
                	
             		var html ="";
             		html+='<table style="width:98%">';
             		html+='<tr>'
             		html+='<td style="width:34%"></td>'
             		html+='<td style="width:65%">'
             		html+='<div id="divMsg" style="margin:0 5px;color:red;"></div>'
             		html+='</td>'
             		html+='</tr>'
             		/*html+='<tr>'
             		html+='<td style="text-align:right;">岗位编号：</td>';
             		
             		if(position.data.positionNo)
             			html+='<td style="height:40px;">'+positionNo+'<input id="orgPositionNo" type="hidden" name="positionNo" value="'+positionNo+'" size="40" class="form-control required number" maxlength="8" autocomplete="off"></td>';
           			else
           				html+='<td style="height:40px;"><input id="orgPositionNo" type="text" name="positionNo" value="'+positionNo+'" size="40" class="form-control required number" maxlength="8" autocomplete="off"></td>';
            		
         			html+='</tr>'*/
             		html+='<tr>'
             		html+='<td style="text-align:right;">是否属于虚拟岗位：</td>'
             		html+='<td style="height:40px;">'
             		html+='<select id="isRealPosition" class="form-control"  name="isRealPosition" onchange="fnIsRealchange()" autocomplete="off">'
            		html+='<option value="0" '+(isRealPosition=="0"?"selected":"")+'>否</option>'
           			html+='<option value="1" '+(isRealPosition=="1"?"selected":"")+'>是</option>'
         			html+='</select></td>'
             		html+='</tr>'
             		html+='<tr id="tr_realPosition" style="'+(isRealPosition=="0"?"display:none":"")+'">'
             		html+='<td style="text-align:right;">虚拟岗位对应真实岗位：</td>'
             		html+='<td style="height:40px;"><div class="userPickerForPersonInfo">'
           			html+='<div class="input-group">'
        			html+='<input id="_task_name_key" type="hidden" name="postId" value="'+postId+'">'
           			html+='<input type="text" class="form-control required" id="postName" name="postName" value="'+postName+'" readonly="readonly">'
          			html+='<div id="PersonInfoDiv" class="input-group-addon"><i class="glyphicon glyphicon-user"></i></div>'
       				html+='</div>'
      				html+='</div></td>'
             		html+='</tr>'
             		html+='</table>'
             		var dialogInput =bootbox.dialog({
                         title: "设置岗位属性",
                         message: html,
                         buttons: {
                             noclose: {
                                 label: '提交',
                                 className: 'btn-primary',
                                 callback: function () {
                                 	/* var positionNo=$("#orgPositionNo").val();
                                     if (positionNo.length==0) {
                                         bootbox.alert("请输入岗位编号！");
                                         return false;
                                     } */
                                     
                                     var isRealPosition=$("#isRealPosition").val();
                                     var positionRealIds=$("#_task_name_key").val();
                                     if (isRealPosition=="1") {
                                    	 if(positionRealIds.length==0){
                                    		 bootbox.alert("设置岗位为虚拟岗，请选择对应真实岗位！");
                                             return false;
                                    	 }
                                     };
                                     var loading = bootbox.dialog({
                                         message: '<p>提交中...</p>',
                                         closeButton: false
                                     });
                                     $.post(config.setPositionUrl, 
                                    		 {
                                    	 		postId:positionId,
                                    	 		isRealPosition:isRealPosition, 
                                    	 		positionRealIds: encodeURI(positionRealIds)
                                    	 		}, function (data) {
                                         loading.modal('hide')
                                         if (data == undefined || data == null || data == "") {
                                             bootbox.alert("操作失败！");
                                             return false;
                                         }
                                         if (data.code == 200) {
                                        	 dialogInput.modal('hide')
                                             bootbox.alert({
                                                 message: data.message
                                             });
                                         }
                                         else
                                             bootbox.alert(data.message);

                                         return data.code == 200;
                                     })
                                     return false;
                                 }
                             },
                             cancel: {
                                 label: '取消',
                                 className: 'btn-danger'
                             }
                         },
                         callback: function (result) {
                             alert(result);
                             return;
                         },
                         show: true
                     });
                 }
                 else{
                	bootbox.alert(data.message);
                 }
             });
    	}
    </script>
</html>

