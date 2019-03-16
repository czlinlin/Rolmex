<%@page contentType="text/html;charset=UTF-8" %>
<%@include file="/taglibs.jsp" %>
<%pageContext.setAttribute("currentHeader", "pim");%>
<%pageContext.setAttribute("currentMenu", "pim");%>
<%pageContext.setAttribute("currentChildMenu", "汇报条线管理");%>
<!doctype html>
<html>

<head>
    <%@include file="/common/meta.jsp" %>
    <title>麦联</title>
    <%@include file="/common/s3.jsp" %>
    <link type="text/css" rel="stylesheet" href="${cdnPrefix}/userpicker3-v2/userpicker.css">
</head>

<body>
<%@include file="/header/version.jsp" %>
<div class="row-fluid">
    <%@include file="/menu/dict.jsp" %>
    <!-- start of main -->
    <section id="m-main" class="col-md-10" style="margin-top:65px;">
        <div class="panel panel-default">
            <div class="panel-heading">
               	 编辑
            </div>
            <div class="panel-body">
                <form id="versionInfoForm" method="post" class="form-horizontal" action="work-report-cc-presetting-node-save.do"
                      enctype=multipart/form-data>
                    <fieldset>
                    	<legend>基础信息</legend>
                    	<div>
	                      	<input id="workTaskInfo_id" type="hidden" name="id" value="${model.id}">
		                    <div class="form-group">
		                        <label class="control-label col-md-2" for="version_code">条线名称</label>
		                        <div class="col-md-4">
		                        	<input id="id" type="hidden" name="id" value="${model.id}" />
		                            <input id="title" type="text" name="title"
		                                   value="${model.title}" size="40" class="form-control required" maxlength="50">
		                        </div>
		                    </div>
		                    <div class="form-group">
		                        <label class="control-label col-md-2" for="version_code">状态</label>
		                        <div class="col-md-4">
		                            <input id="rad_status_normal" name="status" type="radio" value="1" <c:if test='${model.status==null}'>checked</c:if> ${model.status=="1"?"checked":""}/>
		                            <label for="rad_status_normal">正常</label>
		                            <input id="rad_status_abnormal" name="status" type="radio" value="2"  ${model.status=="2"?"checked":""}/>
		                            <label for="rad_status_normal">禁用</label>
		                        </div>
		                    </div>
		                    <div class="form-group">
		                        <label class="control-label col-md-2" for="version_remarks">备注</label>
		                        <div class="col-md-4">
		                            <textarea id="note" type="text" name="note" size="40" class="form-control " maxlength="50"
		                                      style="height: 140px">${model.note}</textarea>
		                            <span style="color:gray;"> 请将字数限制在50字以内 </span>
		                        </div>
		                    </div>
		                    
	                    </div>
	                    <legend>条形节点</legend>
	                    
	                    <div class="panel panel-default">
	                    	<div class="col-sm-5 userPickerForPersonInfo" style="display:none;">
	                            <div class="input-group">
	                                <input id="_task_name_key" type="hidden" name="postId"
	                                       value="${postId}">
	                                <input type="text" name="postName" id="postName"
	                                       value="${postName}" class="form-control" readonly placeholder="点击右侧图标选择岗位" >
	                                
	                                <div id='PersonInfoDiv' class="input-group-addon"><i class="glyphicon glyphicon-user"></i></div>
	                            </div>
	                        </div>
	                        <input type="hidden" name="ipt_index" id="ipt_index" value="" />
			                <table id="tab-report-presetting" class="table table-hover">
			                    <thead>
			                    <tr>
			                        <th>序号</th>
			                        <th>节点名称</th>
			                        <th>分配策略</th>
			                        <th>岗位</th>
			                        <th>操作</th>
			                    </tr>
			                    </thead>
			                    <tbody>
			                    <c:if test="${empty nodelist}">
			                    	<tr class="tr-report-presetting">
			                        	<td>${status.index+1}</td>
			                            <td><input name="presetting_title" type="text" value="${item.title}" /></td>
			                            <td>
		                            		<select name="presetting_type">
		                            			<option value="0">候选组</option>
									            <option value="1">大区对接人</option>
										        <option value="2">同一大区</option>
										        <option value="3">同一分公司</option>
		                            		</select>
		                            	</td>
			                            <td class="td_select_position">
											<div class="col-sm-5">
					                            <div class="input-group">
					                                <input id="postId_1" class="postId" data-index="1" type="hidden" name="ipt_postId" value="${postId}">
					                                <input id="postName_1" class="postName" data-index="1" type="text" name="ipt_postName" value="${postName}" class="form-control" readonly placeholder="点击右侧图标选择岗位" >
					                                <div id="div_select_position_1" class="input-group-addon div_select_position"><i class="glyphicon glyphicon-user"></i></div>
					                            </div>
					                        </div>
										</td>
			                            <td>
			                                <a href="javascript:" onclick="removeRow(this)">删除</a>
			                            </td>
			                        </tr>
			                    </c:if>
			                    <c:forEach items="${nodelist}" var="item" varStatus="status">
			                        <tr class="tr-report-presetting">
			                        	<td>${status.index+1}</td>
			                            <td><input class="presetting_title" name="presetting_title" type="text" value="${item.title}" /></td>
			                            <td>
		                            		<select class="presetting_type" name="presetting_type">
		                            			<option value="0" ${item.presetting_type=="0"?"selected":""}>候选组</option>
									            <option value="1"  ${item.presetting_type=="1"?"selected":""}>大区对接人</option>
										        <option value="2"  ${item.presetting_type=="2"?"selected":""}>同一大区</option>
										        <option value="3"  ${item.presetting_type=="3"?"selected":""}>同一分公司</option>
		                            		</select>
		                            	</td>
			                            <td class="td_select_position">
											<div class="col-sm-5">
					                            <div class="input-group">
					                                <input id="postId_${status.index+1}" class="postId" data-index="${status.index+1}" type="hidden" name="ipt_postId" value="${item.positionId}">
					                                <input id="postName_${status.index+1}" class="postName" data-index="${status.index+1}" type="text" name="ipt_postName" value="${item.positionName}" class="form-control" readonly placeholder="点击右侧图标选择岗位" >
					                                <div id="div_select_position_${status.index+1}" class="input-group-addon div_select_position"><i class="glyphicon glyphicon-user"></i></div>
					                            </div>
					                        </div>
										</td>
			                            <td>
			                                <a href="javascript:" onclick="removeRow(this)">删除</a>
			                            </td>
			                        </tr>
			                    </c:forEach>
			                    </tbody>
			                </table>
			            </div>
			            <div style="margin-bottom: 20px;">
				            <div class="pull-left btn-group" role="group">
				                <button class="btn btn-default a-insert" type="button" onclick="addRow()">新增行</button>
				            </div>
				            <div class="clearfix"></div>
				        </div>
				        <div>添加节点提示：【<span style="color:blue;" id="span_explain" class="span_close">展开</span>】</div>
	                    <div id="div_explain" style="display:none;">
						<font color="red">说明：每个节点的岗位可以多选，可以是实岗也可以是虚岗。</font><br/>
						 <b>分配策略:</b>
						     <p>1.候选组：如果是虚岗，则其虚岗对应的全部实岗，如果是实岗，直接取实岗即可。</p>
						     <p>2.大区对接人：根据数据字典配置项选取对应的实岗。</p>
						     <p>3.同一大区：根据汇报人查找同一大区对应的实岗。</p>
						     <p>4.同一分公司：根据汇报人查找同一分公司对应的实岗。</p>
						</div>
	                    <div class="form-group">
	                        <div class="col-md-offset-2 col-md-10">
	                            <button id="submitButton" type="submit" class="btn btn-default a-submit">
	                          		保存
	                            </button>
	                            <button type="button" class="btn btn-default"
	                                    onclick="self.location=document.referrer;">返回
	                            </button>
	                        </div>
	                    </div>
                    </fieldset>
                </form>
            </div>
        </div>
    </section>
</div>
<script type="text/javascript" src="${cdnPrefix}/bootbox/bootbox.min1.js"></script>
<script type="text/javascript" src="${cdnPrefix}/userpicker3-v2/postForPersonInFo.js"></script>
<script>
	$(function(){
		
		$("#span_explain").click(function(){
			if($(this).hasClass('span_close')){
				$(this).removeClass('span_close');
				$("#div_explain").show();
				$(this).html("折叠");
			}
			else{
				$(this).addClass('span_close');
				$("#div_explain").hide();
				$(this).html("展开");
			}
		})
		
		fnListenerClick(1);
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
		
		$(document).delegate('#userPickerForPersonInfo_select', 'click', function (e) {
			var data_index=$("#ipt_index").val();
			var postIds=$("#_task_name_key").val();//.replace(",","|");
			$("#postId_"+data_index).val(postIds);
			$("#postName_"+data_index).val($("#postName").val());
			//alert($("#postName_"+data_index).val());
		});
	})

	function addRow(){
		$("#tab-report-presetting").append($(".tr-report-presetting:last").prop("outerHTML"));
		var length=$(".tr-report-presetting:last td:first").html();
		var data_index=parseInt(length)+1;
		$(".tr-report-presetting:last td:first").html(data_index);
		
		var ele_title=$(".tr-report-presetting:last .presetting_title");
		ele_title.val("");
		
		var ele_type=$(".tr-report-presetting:last .presetting_type");
		ele_type.val("0");
		
		var ele_postid=$(".tr-report-presetting:last .td_select_position .postId");
		ele_postid.attr("id","postId_"+data_index);
		ele_postid.attr("data-index",data_index);
		ele_postid.val("");
		
		var ele_postname=$(".tr-report-presetting:last .td_select_position .postName");
		ele_postname.attr("id","postName_"+data_index);
		ele_postname.attr("data-index",data_index);
		ele_postname.val("");
		
		var ele_div=$(".tr-report-presetting:last .td_select_position .div_select_position");
		ele_div.attr("id","div_select_position_"+data_index);
		
		fnListenerClick(data_index);
	}
	
	function fnListenerClick(index){
		$("#div_select_position_"+index).click(function(){
			var postIds=$("#postId_"+index).val();//.replace("|",",");
			var postNames=$("#postName_"+index).val();
			var data_index=$("#postId_"+index).attr("data-index");
			
			//alert(postIds);
			
			$("#_task_name_key").val(postIds);
			$("#postName").val(postNames);
			$("#ipt_index").val(data_index);
			
			$("#PersonInfoDiv").click();
		})
	}
	
	function removeRow(id){
		var length=$(".tr-report-presetting").size();
		if(parseInt(length)>1)
			$(id).parent().parent().remove();
		else
			alert("至少保留一行");
	}
</script>
</body>
</html>

