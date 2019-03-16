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
        	
            table = new Table(config);
            table.configPagination('.m-pagination');
            table.configPageInfo('.m-page-info');
            table.configPageSize('.m-page-size');
            var isSearch = "${isSearch}";
            if(isSearch == ""){
            	$("#pageMsgId").html("请点击查询按钮，进行查询");
            }
           // fnGetTypeDetail();
        })

        function fnSearch() {
            document.getElementById("charege-infoForm").action = "humantask-history-tool-log-list-i.do";
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
        	$("#historyTasksForm").attr("action","${tenantPrefix}/humantask/history-task-info-input.do");
	    	$("#historyTasksForm").submit();
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

                <form id="charege-infoForm" name="charege-infoForm" method="post" action="humantask-history-tool-log-list-i.do"
                      class="form-inline">
                      		<input type="hidden" name="isSearch" value="1">
                    <div class="row">
                        <div class="col-md-4">
                            <label for="charege-info_name">受理单号:</label>
                            <input type="text" style="width:200px;" maxlength="30" id="charege-info_applyCode"
                                   name="filter_LIKES_applyCode" value="${param.filter_LIKES_applyCode}"
                                   class="form-control" onchange="fntrim(this)">
                        </div>
                        <div class="col-md-2">
                            <button id="btn_Search" class="btn btn-default a-search" onclick="fnSearch()" type="submit">查询</button>
                        </div>
                    </div>
                    <br>
                </form>
            </div>
        </div>

        <!--
          <div class="pull-right">
            每页显示
            <select class="m-page-size form-control" style="display:inline;width:auto;">
              <option value="10">10</option>
              <option value="20">20</option>
              <option value="50">50</option>
            </select>
            条
          </div>

       -->

        <form id="historyTasksForm" name="historyTasksForm" method='post' action=""
              class="m-form-blank mytable">
            <div class="panel panel-default">
                <div class="panel-heading">
                    <i class="glyphicon glyphicon-list"></i>
                    列表
                </div>
                <div style="overflow-x:scroll">
                    <table id="pimRemindGrid" class="table table-hover mytable"
                           style="min-width: 100%">
                        <thead>
                        <tr>
                            <th >受理单号</th>
                            <th >环节编号</th>
                            <th >主题</th>
                            <th>之前负责人</th>
                            <th>之后负责人</th>
                            <th>岗/人</th>
                            <th>修改人</th>
                            <th>修改时间</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach items="${page.result}" var="item">

                            <tr  <%-- <c:if test="${item.status == '未审核'|| item.status == '驳回发起人'}"> style="color:blue"  </c:if>  <c:if test="${item.status == '审核中'||item.status == '审核中（驳回）'}"> style="color:green" </c:if>
                                    <c:if test="${item.status == '审核未通过' || item.status == '已取消'}">style="color:red" </c:if>
                                    <c:if test="${item.status == '已撤回'}">style="color:#ff6e0c" </c:if> --%>>
                                <td>${item.applyCode}</td>
                                <td>${item.id}</td>
                                <td>${item.theme}</td>
                                <td><tags:user userId="${item.startAssignee}"></tags:user></td>
                                <td><tags:user userId="${item.endAssignee}"></tags:user></td>
                                <td>${item.positionType}</td>
                                <td><tags:user userId="${item.creator}"></tags:user></td>
                                <td><fmt:formatDate value="${item.createTime}"
                                                    pattern="yyyy-MM-dd HH:mm:ss"/></td>
                               <%--  <td><c:if test="${!fn:contains(item.url,'?')}">
                                    <a  class="workTask_title rwop" title="${item.theme}"
                                        href="${tenantPrefix}${item.url}?processInstanceId=${item.processInstanceId}&isPrint=false"
                                        target="_blank">${item.theme}</a>
                                </c:if> <c:if test="${fn:contains(item.url,'?')}">
                                    <a  class="workTask_title rwop" title="${item.theme}"
                                        href="${tenantPrefix}${item.url}&processInstanceId=${item.processInstanceId}&isPrint=false"
                                        target="_blank">${item.theme}</a>
                                </c:if></td> --%>
                                <%-- <td>${item.status}</td>
                                <th>${item.approvePositionName}</th>
                                <th>${item.action}</th>
                                 <th><tags:user userId="${item.positionId}"></tags:user></th>
                                <td>${item.applyUserName}</td>
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
                                                    pattern="yyyy-MM-dd HH:mm:ss"/></td> --%>
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
