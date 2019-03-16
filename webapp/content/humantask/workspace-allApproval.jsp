<%@page contentType="text/html;charset=UTF-8" %>
<%@include file="/taglibs.jsp" %>
<%pageContext.setAttribute("currentHeader", "pim");%>
<%pageContext.setAttribute("currentMenu", "task");%>
<%pageContext.setAttribute("currentChildMenu", "待办任务");%>
<!doctype html>
<html lang="en">

<head>
    <%@include file="/common/meta.jsp" %>
    <title>麦联</title>
    <%@include file="/common/s3.jsp" %>
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
                'filter_LIKES_theme': '${param.filter_LIKES_theme}',
                'filter_LIKES_ucode': '${param.filter_LIKES_ucode}',
                'filter_EQS_businessTypeId': '${param.filter_EQS_businessTypeId}',
                'filter_EQS_businessDetailId': '${param.filter_EQS_businessDetailId}',
                'filter_GED_create_time': '${param.filter_GED_create_time}',
                'filter_LED_create_time': '${param.filter_LED_create_time}',
                'filter_EQS_systemid': '${param.filter_EQS_systemid}',
                'filter_EQS_areaId': '${param.filter_EQS_areaId}',
                'filter_EQS_companyid': '${param.filter_EQS_companyid}',
                'filter_LIKES_full_name': '${param.filter_LIKES_full_name}',
                'filter_EQS_pro_status': '${param.filter_EQS_pro_status}'
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

            //设置段时间
            var sectionJson = [{"begin": "#pickerStartTime", "end": "#pickerEndTime"}];
            fnSectionPickerTime(sectionJson)
            fnGetTypeDetail();
            fnGetCompany();
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
            document.getElementById("charege-infoForm").action = "workspace-allApproval.do";
        }
        function fnGoAction() {
            if (${page.resultSize==0}) {
                alert("没有数据需要导出！")
                return false;
            }
            if ($("#charege-info_applyCode").val() == "" &&
                $("#charege-theme").val() == "" &&
                $("#charege-info_full_name").val() == "" &&
                $("#charege-info_ucode").val() == "" &&
                $("#selectSystem").val() == "" &&
                $("#selectArea").val() == "" &&
                $("#selectCompany").val() == "" &&
                $("#bussinessType").val() == "" &&
                $("#bussinessDetailType").val() == "" &&
                $("#workTask_Status").val() == "" &&
                $("input[name='filter_GED_create_time']").val() == "" &&
                $("input[name='filter_LED_create_time']").val() == ""
            ) {
                alert("请选择导出条件！");
                return false;
            }
            document.getElementById("charege-infoForm").action = "allApproval-export.do";
        }
        //companylist  selectArea  selectCompany
        var fnGetCompany = function () {
            var typeId = $("#selectArea").val();
            var html = "<option value=''>全部</option>";
            if (typeId == "") {
                $("#selectCompany").html(html);
                return;
            }

            html = "<option>加载中</option>";
            $("#selectCompany").html(html);
            if (typeId != "") {
                $.ajax({
                    url: "${tenantPrefix}/rs/bpm/company-info",
                    type: "POST",
                    data: {strAreaID: typeId},
                    timeout: 10000,
                    success: function (data) {
                        if (data != undefined && data != null && data != "") {
                            if (data.bSuccess == "true") {
                                html = "<option value=''>全部</option>";
                                $(data.companylist).each(function (i, item) {
                                    if ("${param.filter_EQS_companyid}" == item.ID)
                                        html += "<option value='" + item.ID + "' selected='selected'>" + item.NAME + "</option>";
                                    else
                                        html += "<option value='" + item.ID + "'>" + item.NAME + "</option>";
                                })
                            }
                            $("#selectCompany").html(html);
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
        function fntrim(inputObj) {
            var oldVal = inputObj.value;
            var newstr = $.trim(oldVal);
            inputObj.value = newstr;
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
<%@include file="/header/bpm-workspace3.jsp" %>

<div class="row-fluid">
    <%-- <%@include file="/menu/bpm-workspace3.jsp"%> --%>
    <%@include file="/menu/sidebar.jsp" %>

    <!-- start of main -->
    <section id="m-main" class="col-md-10" style="padding-top:65px;">

        <div class="panel panel-default">
            <div class="panel-heading">
                <i class="glyphicon glyphicon-list"></i>
                查询
                <div class="pull-right ctrl">
                    <a class="btn btn-default btn-xs"><i id="charege-infoSearchIcon"
                                                         class="glyphicon glyphicon-chevron-up"></i></a>
                </div>
            </div>
            <div class="panel-body">

                <form id="charege-infoForm" name="charege-infoForm" method="post" action="workspace-allApproval.do"
                      class="form-inline">
                    <div class="row">
                        <div class="col-md-4">
                            <label for="charege-info_name">受理单号:</label>
                            <input type="text" style="width:200px;" maxlength="30" id="charege-info_applyCode"
                                   name="filter_LIKES_applyCode" value="${param.filter_LIKES_applyCode}"
                                   class="form-control" onchange="fntrim(this)">
                        </div>
                        <div class="col-md-4">
                            <label>主题:</label>
                            <input type="text" style="width:200px;" maxlength="30" id="charege-theme"
                                   name="filter_LIKES_theme" value="${param.filter_LIKES_theme}"
                                   class="form-control" onchange="fntrim(this)">
                        </div>
                    </div>
                    <br>
                    <div class="row">
                        <div class="col-md-4">
                            <label for="charege-info_name">申请人:</label>
                            <input type="text" style="width:200px;" maxlength="15" id="charege-info_full_name"
                                   name="filter_LIKES_full_name" value="${param.filter_LIKES_full_name}"
                                   class="form-control" onchange="fntrim(this)">
                        </div>
                        <div class="col-md-4">
                            <label for="charege-info_name">经销商编号:</label>
                            <input type="text" style="width:150px;" maxlength="10" id="charege-info_ucode"
                                   name="filter_LIKES_ucode" value="${param.filter_LIKES_ucode}" class="form-control" onchange="fntrim(this)" onchange="fntrim(this)" onkeyup="value=value.replace(/[^\d]/g,'')">
                        </div>
                    </div>
                    <br>
                    <div class="row">
                        <div class="col-md-4">
                            <label for="selectSystem">所属体系:</label>
                            <select id="selectSystem" class="form-control" name="filter_EQS_systemid" title="">
                                <option value="" ${param.filter_EQS_systemid==""?"selected='selected'":""}>全部</option>
                                <c:forEach items="${systemlist}" var="item">
                                    <option value="${item.value}" ${param.filter_EQS_systemid==item.value?"selected='selected'":""}>${item.name}</option>
                                </c:forEach>
                            </select></div>
                        <div class="col-md-4">
                            <label for="selectArea">所属大区:</label>
                            <select id="selectArea" onchange="fnGetCompany()" class="form-control"
                                    name="filter_EQS_areaId"
                                    title="">
                                <option value="" ${param.filter_EQS_areaId==""?"selected='selected'":""}>全部</option>
                                <c:forEach items="${arealist}" var="area">
                                    <option value="${area.id}" ${param.filter_EQS_areaId==area.id?"selected='selected'":""}>${area.name}</option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="col-md-4">
                            <label for="selectCompany">所属分公司:</label>
                            <select id="selectCompany" class="form-control" name="filter_EQS_companyid" title="">
                                <option value="" ${param.filter_EQS_companyid==""?"selected='selected'":""}>全部</option>
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
                            <label for="workTask_Status">状态:</label>
                            <select id="workTask_Status" class="form-control" name="filter_EQS_pro_status" title="">
                                <option value="" ${param.filter_EQS_pro_status==""?"selected='selected'":""}>全部</option>
                                <c:forEach items="${statuslist}" var="item">
                                    <option value="${item.value}"  ${param.filter_EQS_pro_status==item.value?"selected='selected'":""}>${item.name}</option>
                                </c:forEach>
                            </select></div>
                        <div class="col-md-6">
                            <label for="charege-info_name">申请时间:</label>
                            <div id="pickerStartTime" class="input-group  date">
                                <input style="width:160px;" type="text" name="filter_GED_create_time"
                                       value="${param.filter_GED_create_time}" class="form-control required valid">
                                <span class="input-group-addon">
					<i class="glyphicon glyphicon-calendar"></i>
				</span>
                            </div>
                            至
                            <div id="pickerEndTime" class="input-group  date">
                                <input style="width:160px;" type="text" name="filter_LED_create_time"
                                       value="${param.filter_LED_create_time}" class="form-control required valid">
                                <span class="input-group-addon">
					<i class="glyphicon glyphicon-calendar"></i>
				</span>
                            </div>

                        </div>
                        <div class="col-md-2">
                            <button id="btn_Search" class="btn btn-default a-search" onclick="fnSearch()" type="submit">
                                查询
                            </button>
                            &nbsp;
                            <button class="btn btn-default a-export" onclick="fnGoAction()" type="submit">导出</button>
                        </div>
                    </div>
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

        <form id="pimRemindGridForm" name="pimRemindGridForm" method='post' action="pim-note-remove.do"
              class="m-form-blank mytable">
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
                            <th width="5%">操作</th>
                            <th>受理单号</th>
                            <th>状态</th>
                            <th>主题</th>
                            <th width="6%">申请人</th>
                            <th width="10%">经销商编号</th>
                            <th width="8%">业务类型</th>
                            <th width="8%">业务细分</th>
                            <th width="8%">所属体系</th>
                            <th width="8%">所属大区</th>
                            <th width="10%">所属分公司</th>
                            <th width="8%">申请时间</th>
                            <th width="8%">最后审批时间</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach items="${page.result}" var="item">

                            <tr  <c:if test="${item.status == '未审核'|| item.status == '驳回发起人'}"> style="color:blue"  </c:if>  <c:if test="${item.status == '审核中'||item.status == '审核中（驳回）'}"> style="color:green" </c:if>
                                    <c:if test="${item.status == '审核未通过' || item.status == '已取消'}">style="color:red" </c:if>
                                    <c:if test="${item.status == '已撤回'}">style="color:#ff6e0c" </c:if>>
                                <td>
                                        <%-- <a href="${tenantPrefix}/operation/task-operation-withdraw.do?humanTaskId=${item.id}&comment=">撤销</a>
                                    <a href="javascript:void(0);doTransfer(${item.id})">转发</a> --%>
                                    <!-- ckx add 2018/8/29  新增抄送功能 -->
	          						<a href="${tenantPrefix}/workOperationCustom/custom-copy-edit.do?processInstanceId=${item.processInstanceId}">抄送</a>
                                    <!-- 无论是自定义还是普通流程，详情直接跳表单 原地址 /bpm/workspace-viewHistory.do-->
                                    <a href="${tenantPrefix}/bpm/workspace-viewHistoryFrom.do?processInstanceId=${item.processInstanceId}&url=${item.url}"
                                       target="_blank">详情</a>
                                    <c:if test="${fn:contains(item.url,'?')}">
	                                    <a href="${tenantPrefix}${item.url}&processInstanceId=${item.processInstanceId}&isPrint=true"
	                                       target="_blank">打印</a>
                                    </c:if>
                                    <c:if test="${!fn:contains(item.url,'?')}">
	                                    <a href="${tenantPrefix}${item.url}?processInstanceId=${item.processInstanceId}&isPrint=true"
	                                       target="_blank">打印</a>
                                    </c:if>
                                </td>
                                <td>${item.applyCode}</td>
                                <td>${item.status}</td>
                                <td><c:if test="${!fn:contains(item.url,'?')}">
                                    <a  class="workTask_title rwop" title="${item.theme}"
                                        href="${tenantPrefix}${item.url}?processInstanceId=${item.processInstanceId}&isPrint=false"
                                        target="_blank">${item.theme}</a>
                                </c:if> <c:if test="${fn:contains(item.url,'?')}">
                                    <a  class="workTask_title rwop" title="${item.theme}"
                                        href="${tenantPrefix}${item.url}&processInstanceId=${item.processInstanceId}&isPrint=false"
                                        target="_blank">${item.theme}</a>
                                </c:if></td>
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
                                                    pattern="yyyy-MM-dd HH:mm:ss"/></td>
                            </tr>


                        </c:forEach>
                        </tbody>
                    </table>
                </div>
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

    </section>
    <!-- end of main -->
</div>

</body>

</html>
