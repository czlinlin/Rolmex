<%@page contentType="text/html;charset=UTF-8" %>
<%@include file="/taglibs.jsp" %>
<%pageContext.setAttribute("currentHeader", "pim");%>
<%pageContext.setAttribute("currentMenu", "task");%>
<%pageContext.setAttribute("currentChildMenu", "数据管理");%>
<!doctype html>
<html lang="en">

<head>
    <%@include file="/common/meta.jsp" %>
    <title>麦联</title>
    <%@include file="/common/s3.jsp" %>
    <script type="text/javascript" src="${cdnPrefix}/bootbox/bootbox.min1.js"></script>
    <link type="text/css" rel="stylesheet" href="${cdnPrefix}/userpicker3-v2/userpicker.css">
    <script type="text/javascript" src="${cdnPrefix}/userpicker3-v2/userpickercustomaudit.js"></script>
    <script type="text/javascript">
	    $(function(){
			window.parent.closeLoading();
			
			window.parent.$.showMessage($('#m-success-tip-message').html(), {
	            position: 'top',
	            size: '50',
	            fontSize: '20px'
	        });
		});
    </script>
    <script type="text/javascript">
        var config = {
            id: 'processGrid',
            pageNo: ${page.pageNo},
            pageSize: ${page.pageSize},
            totalCount: ${page.totalCount},
            resultSize: ${page.resultSize},
            pageCount: ${page.pageCount},
            orderBy: '${page.orderBy == null ? "" : page.orderBy}',
            asc: ${page.asc},
            params: {
                'filter_LIKES_applyCode': '${param.filter_LIKES_applyCode}',
                'filter_EQS_businessTypeId': '${param.filter_EQS_businessTypeId}',
                'filter_EQS_businessDetailId': '${param.filter_EQS_businessDetailId}',
                'filter_EQS_pro_status': '${param.filter_EQS_pro_status}',
                'filter_EQS_data_type': '${param.filter_EQS_data_type}',
                'user_id': '${searchUserId}',
                'isSearch': '${isSearch}'
                
            },
            selectedItemClass: 'selectedItem',
            gridFormId: 'processGridForm',
            exportUrl: 'process-export.do'
        };

        var table;

        $(function () {
        	
           //审批人
           createUserPicker({
                 modalId: 'leaderPicker',//这个 其实是弹出的窗口的div id
                 targetId: 'leaderDiv', //这个是点击哪个 会触发弹出窗口
                 inputStoreIds: {iptid: "leaderId", iptname: "leaderName"},//存储已选择的ID和name的input的id
                 auditId: 'ulapprover',//显示审批步骤
                 showExpression: true,
                 multiple: false,
                 searchUrl: '${tenantPrefix}/rs/user/searchV',
                 treeUrl: '${tenantPrefix}/party/asyncTree.do?partyStructTypeId=1&notViewPost=true&notAuth=false',
                 childUrl: '${tenantPrefix}/rs/party/searchUserNoMe'
             });
        	
        	
            table = new Table(config);
            table.configPagination('.m-pagination');
            table.configPageInfo('.m-page-info');
            table.configPageSize('.m-page-size');
            var isSearch = "${isSearch}";
            if(isSearch == ""){
            	$("#pageMsgId").html("请点击查询按钮，进行查询");
            }
            fnGetTypeDetail();
        })

        var fnGetTypeDetail = function () {
            var typeId = $("#bussinessType").val();
            var html = "<option value=''>全部</option>";
            if (typeId == "") {
                $("#bussinessDetailType").html(html);
                return;
            }

            html = "<option>加载中</option>";
            $("#bussinessDetailType").html(html);
            if (typeId != "") {
            	
            	if (typeId=='9999'){
            		
            		//ckx   2018/07/25  增加自定义细分查询   ckx 2018/07/31  增加出差，特殊考勤查询
                	if("${param.filter_EQS_businessDetailId}" == 8001){
                    	html = "<option value=''>全部</option><option value='8888'>自定义申请</option><option selected='selected' value='8001'>请假申请</option><option value='8003'>加班申请</option><option value='8002'>出差外出申请</option><option value='8004'>特殊考勤说明申请</option>";
                    }else if("${param.filter_EQS_businessDetailId}" == 8002){
                        html = "<option value=''>全部</option><option value='8888'>自定义申请</option><option value='8001'>请假申请</option><option value='8003'>加班申请</option><option value='8002' selected='selected' >出差外出申请</option><option value='8004'>特殊考勤说明申请</option>";
                    }else if("${param.filter_EQS_businessDetailId}" == 8003){
                        html = "<option value=''>全部</option><option value='8888'>自定义申请</option><option value='8001'>请假申请</option><option selected='selected' value='8003'>加班申请</option><option value='8002'>出差外出申请</option><option value='8004'>特殊考勤说明申请</option>";
                    }else if("${param.filter_EQS_businessDetailId}" == 8004){
                        html = "<option value=''>全部</option><option value='8888'>自定义申请</option><option value='8001'>请假申请</option><option value='8003'>加班申请</option><option value='8002'>出差外出申请</option><option value='8004' selected='selected' >特殊考勤说明申请</option>";
                    }else if("${param.filter_EQS_businessDetailId}" == 8888){
                        html = "<option value=''>全部</option><option selected='selected' value='8888'>自定义申请</option><option value='8001'>请假申请</option><option value='8003'>加班申请</option><option value='8002'>出差外出申请</option><option value='8004'>特殊考勤说明申请</option>";
                    }else{
                    	html = "<option selected='selected' value=''>全部</option><option value='8888'>自定义申请</option><option value='8001'>请假申请</option><option value='8003'>加班申请</option><option value='8002'>出差外出申请</option><option value='8004'>特殊考勤说明申请</option>";
                    }
           		
           		 $("#bussinessDetailType").html(html);
           		return ;
           		
           	}
                $.ajax({
                    url: "${tenantPrefix}/rs/bpm/bussiness-detail",
                    type: "POST",
                    data: {strBusType: typeId},
                    timeout: 10000,
                    success: function (data) {
                        if (data != undefined && data != null && data != "") {
                            if (data.bSuccess == "true") {
                                html = "<option value=''>全部</option>";
                                $(data.BussinessDetails).each(function (i, item) {
                                    if ("${param.filter_EQS_businessDetailId}" == item.intBDID)
                                        html += "<option value='" + item.intBDID + "' selected='selected'>" + item.varDetails + "</option>";
                                    else
                                        html += "<option value='" + item.intBDID + "'>" + item.varDetails + "</option>";
                                })
                            }
                            $("#bussinessDetailType").html(html);
                        }
                    },
                    error: function (XMLHttpRequest, textStatus, errorThrown) {
                        alert("[" + XMLHttpRequest.status + "]error，请求失败")
                    },
                    complete: function (xh, status) {
                        dialog.modal('hide');
                        if (status == "timeout")
                            bootbox.alert("请求超时");
                    }
                });
            }
        }
        function fnSearch() {
            document.getElementById("charege-infoForm").action = "humantask-history-tool-list-i.do";
        }
 
        function fntrim(inputObj) {
            var oldVal = inputObj.value;
            var newstr = $.trim(oldVal);
            inputObj.value = newstr;
        }
        
        function fnEditPosition(){
        	var boo = $("table input[type='checkbox']").is(':checked');
			if(!boo){
				alert("请选择数据");
				return false;
			}
			var nineNum = 0;
			var otherNum = 0;
			//获取所有选中的复选框
			 $.each($('input:checkbox:checked'),function(){
                //alert("你选了："+$('input[type=checkbox]:checked').length+"个，其中有："+$(this).attr("src"));
                var src = $(this).attr("src");
                if("9999" == src){
                	nineNum++;
                }else{
                	otherNum++;
                }
                
            });
			
			 if(nineNum > 1 || (otherNum > 0 && nineNum > 0)){
				alert("自定义环节只能单条指定");
             	return false;
             }else{
            	 //判断是否是未结流程并且是发起人
         	        text = $("input:checkbox[name='taskIds']:checked").map(function(index,elem) {
         	            return $(elem).val();
         	        }).get().join(',');
         	       // alert("选中的checkbox的值为："+text);
            	 
         	       $.ajax({
               		type : "POST", 
               		dataType:"json",
                       url : "${tenantPrefix}/humantask/checkTaskInfo-start.do",  
                       data : {  
                       		taskIds: text
                       },  
                       success : function(data) {
                    	   if(data == true){
	                    	   if(nineNum == 1 && otherNum == 0){
	                				$("#isCustom").val("1");
	                			}
	                      		$("#historyTasksForm").attr("action","${tenantPrefix}/humantask/history-task-info-input.do");
	                  	    	$("#historyTasksForm").submit();
                    	   }else{
                    		   alert("选中环节包含未结流程且为发起人");
                    		   return false;
                    	   }
                       },        
                       error: function(XMLHttpRequest, textStatus, errorThrown) {            
                       	 alert("验证指定人是否为流程发起人和是否为流程已有审核人失败！");
                       }
               	});
            	 
            	 
            	 
            	 
            	 
            	 
     			/* if(nineNum == 1 && otherNum == 0){
     				$("#isCustom").val("1");
     			}
           		$("#historyTasksForm").attr("action","${tenantPrefix}/humantask/history-task-info-input.do");
       	    	$("#historyTasksForm").submit(); */
             }
        	
        }
        function checkAll(obj){
        	var boo = $(obj).is(':checked');
			if(boo){
				$('table input:checkbox').attr("checked", true);
			}else {
				$('table input:checkbox').attr("checked", false);
			}
			
		}
        
    </script>
    <style type="text/css">
        body {
            padding-right: 0px !important;
        }

        .mytable {
            /*table-layout: fixed;*/
            border: 0px;
            margin: 0px;
            border-collapse: collapse;
            width: 100%;
        }

        .mytable tr td .workTask_title {
            width: 150px;
            display: block;
            overflow: hidden;
        }

        .table {
            width: 100%;
        }

        .mytable tr td, .mytable tr td .rwop {
            text-overflow: ellipsis; /* for IE */
            -moz-text-overflow: ellipsis; /* for Firefox,mozilla */
            overflow: hidden;
            white-space: nowrap;
            border: 0px solid;
            text-align: left
        }
    </style>
</head>

<body>
<div class="row-fluid">
    <c:if test="${not empty flashMessages}">
	<div id="m-success-tip-message" style="display: none;">
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
    <section id="m-main" class="col-md-15" style="padding-top:65px;">

        <div class="panel panel-default">
            <div class="panel-heading">
                <i class="glyphicon glyphicon-list"></i>
               		 查询
                <div class="pull-right ctrl">
                    <a class="btn btn-default btn-xs"><i id="charege-infoSearchIcon"class="glyphicon glyphicon-chevron-up"></i></a>
                </div>
            </div>
            <div class="panel-body">

                <form id="charege-infoForm" name="charege-infoForm" method="post" action="humantask-history-tool-list-i.do"
                      class="form-inline">
                      		<input type="hidden" name="isSearch" value="1">
                    <div class="row">
                        <div class="col-md-4">
                            <label for="charege-info_name">受理单号:</label>
                            <input type="text" style="width:200px;" maxlength="30" id="charege-info_applyCode"
                                   name="filter_LIKES_applyCode" value="${param.filter_LIKES_applyCode}"
                                   class="form-control" onchange="fntrim(this)">
                        </div>
                       <div class="col-md-4">
                            <label for="workTask_Status">状态:</label>
                            <select id="workTask_Status" class="form-control" name="filter_EQS_pro_status" title="">
                                <option value="" ${param.filter_EQS_pro_status==""?"selected='selected'":""}>全部</option>
                                <c:forEach items="${statuslist}" var="item">
                                    <option value="${item.value}"  ${param.filter_EQS_pro_status==item.value?"selected='selected'":""}>${item.name}</option>
                                </c:forEach>
                            </select></div>
                    </div>
                    
                    <br>
                    <div class="row">
                        <div class="col-md-4">
                            <label for="bussinessType">业务类型:</label>
                            <select id="bussinessType" onchange="fnGetTypeDetail()" class="form-control"
                                    name="filter_EQS_businessTypeId" title="">
                                <option value="" ${param.filter_EQS_businessTypeId==""?"selected='selected'":""}>全部
                                </option>
                                <c:forEach items="${typelist}" var="itemType">
                                    <option value="${itemType.id}" ${param.filter_EQS_businessTypeId==itemType.id?"selected='selected'":""}>${itemType.businesstype}</option>
                                </c:forEach>
                                <option value="9999"  ${param.filter_EQS_businessTypeId=="9999"?"selected='selected'":""}>自定义</option>
                            </select>
                        </div>
                        <div class="col-md-8">
                            <label for="bussinessDetailType">业务明细:</label>
                            <select id="bussinessDetailType" class="form-control" name="filter_EQS_businessDetailId"
                                    title="">
                                <option value="" ${param.filter_EQS_businessDetailId==""?"selected='selected'":""}>全部
                                </option>
                            </select></div>
                    </div>
                    <br/>
                    <div class="row">
                        <div class="col-md-4">
                            <label for="charege-info_name">人员:</label>
                             <div class="input-group leaderPicker" style="width:50%;">
	                            <input id="leaderId" name="user_id" type="hidden" name="leader"
	                                   value="${searchUserId}">
	                            <input type="text" id="leaderName" name="user_name" class="form-control required"
	                                   value="<tags:user userId="${searchUserId}"></tags:user>" minlength="2"
	                                   maxlength="50" class="form-control" readOnly placeholder="点击后方图标即可选人">
	                            <div id='leaderDiv' class="input-group-addon" style="cursor:pointer;"><i class="glyphicon glyphicon-user"></i>
	                            </div>
                        	</div>
                   	
                        </div>
                       <div class="col-md-4">
                            <label for="workTask_Status">数据类型:</label>
                            <select id="workTask_Status" class="form-control" name="filter_EQS_data_type" title="">
                                <option value="" ${param.filter_EQS_data_type==""?"selected='selected'":""}>全部</option>
                                <option value="1" ${param.filter_EQS_data_type=="1"?"selected='selected'":""}>新数据</option>
                                <option value="2" ${param.filter_EQS_data_type=="2"?"selected='selected'":""}>旧数据</option>
                                <%-- <c:forEach items="${dataTypelist}" var="item">
                                    <option value="${item.value}"  ${param.filter_EQS_data_type==item.value?"selected='selected'":""}>${item.name}</option>
                                </c:forEach> --%>
                            </select></div>
                        <div class="col-md-2">
                            <button id="btn_Search" class="btn btn-default a-search" onclick="fnSearch()" type="submit">查询</button>
                        </div>
                    </div>
                    <br>
                    <div class="row">
                        <div class="col-md-4">
                        	<button class="btn btn-default " onclick="fnEditPosition();return false;">岗位指定</button>
                        </div>
                    </div>
                </form>
            </div>
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
          <br><br>
        <form id="historyTasksForm" name="historyTasksForm" method='post' action=""
              class="m-form-blank mytable">
            <input type="hidden" id="isCustom" name="isCustom" value="0">
            <div class="panel panel-default">
                <div class="panel-heading">
                    <i class="glyphicon glyphicon-list"></i>
                    列表
                </div>
                <div style="overflow-x:scroll">
                    <table id="pimRemindGrid" class="table table-hover mytable"
                           style="min-width: 2000px;">
                        <thead>
                        <tr>
                            <th><input onclick="checkAll(this);" type="checkbox"></th>
                            <th >受理单号</th>
                            <th >主题</th>
                            <th >状态</th>
                            <th width="20%">当前环节</th>
                            <th width="6%">操作</th>
                            <!-- <th width="6%">人员</th> -->
                            <th width="6%">申请人</th>
                            <th width="6%">负责人</th>
                            <th width="6%">经销商编号</th>
                            <th width="8%">业务类型</th>
                            <th width="8%">业务细分</th>
                            <th width="6%">所属体系</th>
                            <th width="6%">所属大区</th>
                            <th width="6%">所属分公司</th>
                            <th width="6%">申请时间</th>
                            <th width="6%">最后审批时间</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach items="${page.result}" var="item">

                            <tr  <%-- <c:if test="${item.status == '未审核'|| item.status == '驳回发起人'}"> style="color:blue"  </c:if>  <c:if test="${item.status == '审核中'||item.status == '审核中（驳回）'}"> style="color:green" </c:if>
                                    <c:if test="${item.status == '审核未通过' || item.status == '已取消'}">style="color:red" </c:if>
                                    <c:if test="${item.status == '已撤回'}">style="color:#ff6e0c" </c:if> --%>>
                                <td>
                                   <input name="taskIds" type="checkbox" value="${item.id}" src="${item.businessTypeId}">
                                </td>
                                <td>${item.applyCode}</td>
                                <td><c:if test="${!fn:contains(item.url,'?')}">
                                    <a  class="workTask_title rwop" title="${item.theme}"
                                        href="${tenantPrefix}${item.url}?processInstanceId=${item.processInstanceId}&isPrint=false"
                                        target="_blank">${item.theme}</a>
                                </c:if> <c:if test="${fn:contains(item.url,'?')}">
                                    <a  class="workTask_title rwop" title="${item.theme}"
                                        href="${tenantPrefix}${item.url}&processInstanceId=${item.processInstanceId}&isPrint=false"
                                        target="_blank">${item.theme}</a>
                                </c:if></td>
                                <td>${item.status}</td>
                                <th>${item.approvePositionName}</th>
                                <th>${item.action}</th>
                                <%--  <th><tags:user userId="${item.positionId}"></tags:user></th> --%>
                                <td>${item.applyUserName}</td>
                                <th><tags:user userId="${item.assignee}"></tags:user></th>
                                <td>${item.ucode}</td>
                                <td>${item.businessTypeName }</td>
                                <td><span class="workTask_title rwop"
                                          title="${item.businessDetailName }">${item.businessDetailName }</span>
                                </td>
                                <td>${item.systemName }</td>
                                <td>${item.areaName == "null" ? "" : item.areaName}</td>
                                <td>${item.companyName }</td>
                                <td><fmt:formatDate value="${item.startTime}"
                                                    pattern="yyyy-MM-dd HH:mm:ss"/></td>
                                <td><fmt:formatDate value="${item.completeTime}"
                                                    pattern="yyyy-MM-dd HH:mm:ss"/></td>
                            </tr>


                        </c:forEach>
                        </tbody>
                    </table>
                </div>
            </div>
        </form>

        <div>
            <div id="pageMsgId" class="m-page-info pull-left">
                共100条记录 显示1到10条记录
            </div>

            <div class="btn-group m-pagination pull-right">
                <button class="btn btn-default">&lt;</button>
                <button class="btn btn-default">1</button>
                <button class="btn btn-default">&gt;</button>
            </div>

            <div class="clearfix"></div>
        </div>

    </section>
    <!-- end of main -->
</div>

</body>

</html>
