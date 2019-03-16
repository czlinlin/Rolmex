<%@page contentType="text/html;charset=UTF-8" %>
<%@include file="/taglibs.jsp" %>
<%@page import="java.util.List" %>
<%
    pageContext.setAttribute("currentHeader", "bpm-workspace");
%>
<%
    pageContext.setAttribute("currentMenu", "bpm-process");
%>
<%
    pageContext.setAttribute("currentChildMenu", "未结流程");
%>
<!doctype html>
<html lang="en">

<head>
    <%@include file="/common/meta.jsp" %>
    <title>麦联</title>
    <%@include file="/common/s3.jsp" %>
    <!-- bootbox -->
    <script type="text/javascript" src="${cdnPrefix}/bootbox/bootbox.min1.js"></script>
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
                'filter_GED_start_time': '${param.filter_GED_start_time}',
                'filter_LED_start_time': '${param.filter_LED_start_time}',
                'filter_EQS_pro_status': '${param.filter_EQS_pro_status}'
            },
            selectedItemClass: 'selectedItem',
            gridFormId: 'processGridForm',
            exportUrl: 'process-export.do',
            drawwiteCusUrl: "${tenantPrefix}/rs/bpm/workspace-withdraw-custom",
            drawwiteUrl: "${tenantPrefix}/rs/bpm/workspace-withdraw",
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

        })
        function fnSearch() {
            document.getElementById("charege-infoForm").action = "workspace-listRunningProcessInstances.do";
        }
        function fnGoAction() {
            if (${page.resultSize==0}) {
                alert("没有数据需要导出！")
                return false;
            }
            if ($("#charege-info_applyCode").val() == "" &&
                $("#charege-theme").val() == "" &&
                $("#workTask_Status").val() == "" &&
                $("#charege-info_ucode").val() == "" &&
                $("#bussinessType").val() == "" &&
                $("#bussinessDetailType").val() == "" &&
                $("input[name='filter_GED_start_time']").val() == "" &&
                $("input[name='filter_LED_start_time']").val() == ""
            ) {
                alert("请选择导出条件！");
                return false;
            }
            document.getElementById("charege-infoForm").action = "listRunningProcessInstances-export.do";
        }
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

                if (typeId == '9999') {

                	//ckx   2018/07/31  增加自定义细分查询
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
                    return;

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
                    	dialog.modal('hide');
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
        function fndrawwith(id) {
            var confirmDialog = bootbox.confirm({
                message: "您确定撤回此申请单吗？撤回后请到【待办审批】进行处理。",
                buttons: {
                    confirm: {
                        label: '确定',
                        className: 'btn-success'
                    },
                    cancel: {
                        label: '取消',
                        className: 'btn-danger'
                    }
                },
                callback: function (result) {
                    if (!result) return;

                    confirmDialog.modal('hide');
                    var loading = bootbox.dialog({
                        message: '<p class="text-center"><i class="fa fa-spin fa-spinner"></i>正在执行...</p>',
                        size: 'small',
                        closeButton: false
                    });
                    $.ajax({
                        url: config.drawwiteUrl,
                        type: "POST",
                        data: {processInstanceId: id},
                        timeout: 10000,
                        success: function (data) {
                            loading.modal('hide');
                            if (data == undefined || data == null || data == "") {
                                bootbox.alert("操作失败");
                                return;
                            }

                            if (data.code == "200") {
                                //dialog.modal('hide')
                                var tip = bootbox.alert(
                                    {
                                        message: "操作成功！",
                                        callback: function () {
                                            document.getElementById("pimRemindGridForm").action = "${tenantPrefix}/humantask/workspace-personalTasks.do";
                                            document.getElementById("pimRemindGridForm").submit();
                                            tip.modal('hide');

                                        }
                                    });
                            }
                            else
                                bootbox.alert(data.message);
                            return;
                        },
                        error: function (XMLHttpRequest, textStatus, errorThrown) {
                        	dialog.modal('hide');
                            alert("[" + XMLHttpRequest.status + "]error，请求失败")
                        },
                        complete: function (xh, status) {
                        	dialog.modal('hide');
                            if (status == "timeout")
                                bootbox.alert("请求超时");
                        }
                    });
                }
            });

        }
        function fndrawwithcus(id) {
            var confirmDialog = bootbox.confirm({
                message: "您确定撤回此申请单吗？撤回后请到【待办审批】进行处理。",
                buttons: {
                    confirm: {
                        label: '确定',
                        className: 'btn-success'
                    },
                    cancel: {
                        label: '取消',
                        className: 'btn-danger'
                    }
                },
                callback: function (result) {
                    if (!result) return;

                    confirmDialog.modal('hide');
                    var loading = bootbox.dialog({
                        message: '<p class="text-center"><i class="fa fa-spin fa-spinner"></i>正在执行...</p>',
                        size: 'small',
                        closeButton: false
                    });
                    $.ajax({
                        url: config.drawwiteCusUrl,
                        type: "POST",
                        data: {processInstanceId: id},
                        timeout: 10000,
                        success: function (data) {
                            loading.modal('hide');
                            if (data == undefined || data == null || data == "") {
                                bootbox.alert("操作失败");
                                return;
                            }

                            if (data.code == "200") {
                                //dialog.modal('hide')
                                var tip = bootbox.alert(
                                    {
                                        message: "操作成功！",
                                        callback: function () {
                                            document.getElementById("pimRemindGridForm").action = "${tenantPrefix}/humantask/workspace-personalTasks.do";
                                            document.getElementById("pimRemindGridForm").submit();
                                            tip.modal('hide');

                                        }
                                    });
                            }
                            else
                                bootbox.alert(data.message);
                            return;
                        },
                        error: function (XMLHttpRequest, textStatus, errorThrown) {
                            alert("[" + XMLHttpRequest.status + "]error，请求失败")
                        },
                        complete: function (xh, status) {
                            if (status == "timeout")
                                bootbox.alert("请求超时");
                        }
                    });
                }
            });

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
    <section id="m-main" class="col-md-10" style="padding-top: 65px;">

        <div class="panel panel-default">
            <div class="panel-heading">
                <i class="glyphicon glyphicon-list"></i> 查询
                <div class="pull-right ctrl">
                    <a class="btn btn-default btn-xs"><i
                            id="charege-infoSearchIcon"
                            class="glyphicon glyphicon-chevron-up"></i></a>
                </div>
            </div>
            <div class="panel-body">
                <form id="charege-infoForm" name="charege-infoForm" method="post"
                      action="workspace-listRunningProcessInstances.do"
                      class="form-inline">
                    <div class="row">
                        <div class="col-md-4">
                            <label>受理单号:</label>
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
                            <label for="workTask_Status">状态:</label>
                            <select id="workTask_Status" class="form-control" name="filter_EQS_pro_status" title="">
                                <option value="" ${param.filter_EQS_pro_status==""?"selected='selected'":""}>全部</option>
                                <c:forEach items="${statuslist}" var="item">
                                    <c:if test="${item.value=='0' or item.value=='1' or item.value=='4' or item.value=='7' or item.value=='8'}">
                                        <option value="${item.value}"  ${param.filter_EQS_pro_status==item.value?"selected='selected'":""}>${item.name}</option>
                                    </c:if>
                                </c:forEach>
                            </select></div>
                        <div class="col-md-4">
                            <label>经销商编号:</label> <input
                                type="text" style="width: 150px;" maxlength="10"
                                id="charege-info_ucode" name="filter_LIKES_ucode"
                                value="${param.filter_LIKES_ucode}" class="form-control" onchange="fntrim(this)"
                                onchange="fntrim(this)" onkeyup="value=value.replace(/[^\d]/g,'')">
                        </div>
                    </div>
                    <br>
                    <div class="row">
                        <div class="col-md-4">
                            <label for="workTask_Status">业务类型:</label> <select
                                id="bussinessType" onchange="fnGetTypeDetail()"
                                class="form-control" name="filter_EQS_businessTypeId" title="">
                            <option value=""
                            ${param.filter_EQS_businessTypeId==""?"selected='selected'":""}>全部
                            </option>
                            <c:forEach items="${typelist}" var="itemType">
                                <option value="${itemType.id}"
                                    ${param.filter_EQS_businessTypeId==itemType.id?"selected='selected'":""}>${itemType.businesstype}</option>
                            </c:forEach>
                            <option value="9999"  ${param.filter_EQS_businessTypeId=="9999"?"selected='selected'":""}>
                                自定义
                            </option>
                        </select></div>
                        <div class="col-md-8">
                            <label for="workTask_Status">业务明细:</label> <select
                                id="bussinessDetailType" class="form-control"
                                name="filter_EQS_businessDetailId" title="">
                            <option value=""
                            ${param.filter_EQS_businessDetailId==""?"selected='selected'":""}>全部
                            </option>
                        </select></div>
                    </div>
                    <br>
                    <div class="row">
                        <div class="col-md-6">
                            <label>申请时间:</label>
                            <div id="pickerStartTime" class="input-group  date">
                                <input style="width: 160px;" type="text"
                                       name="filter_GED_start_time"
                                       value="${param.filter_GED_start_time}"
                                       class="form-control required valid"> <span
                                    class="input-group-addon"> <i
                                    class="glyphicon glyphicon-calendar"></i>
							</span>
                            </div>
                            至
                            <div id="pickerEndTime" class="input-group  date">
                                <input style="width: 160px;" type="text"
                                       name="filter_LED_start_time"
                                       value="${param.filter_LED_start_time}"
                                       class="form-control required valid"> <span
                                    class="input-group-addon"> <i
                                    class="glyphicon glyphicon-calendar"></i>
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

        <form id="pimRemindGridForm" name="pimRemindGridForm" method='post'
              action="pim-note-remove.do" class="m-form-blank">
            <div class="panel panel-default">
                <div class="panel-heading">
                    <i class="glyphicon glyphicon-list"></i> 列表
                </div>
                <div style="overflow-x:scroll">
                <table id="pimRemindGrid" class="table table-hover mytable" style="min-width:1400px;">
                    <thead>
                    <tr>
                        <td>操作</td>
                        <th>受理单号</th>
                        <th>主题</th>
                        <th>状态</th>
                        <th>经销商编号</th>
                        <th>业务类型</th>
                        <th>业务细分</th>
                        <th>提交次数</th>
                        <th>申请时间</th>
                        <th>最后审核时间</th>
                    </tr>
                    </thead>

                    <tbody>
                    <c:forEach items="${page.result}" var="item">
                        <tr  <c:if test="${item.status == '未审核'|| item.status == '驳回发起人'}"> style="color:blue" </c:if>
                                <c:if test="${item.status == '审核中'||item.status == '审核中（驳回）'}"> style="color:green" </c:if>
                                <c:if test="${item.status == '审核未通过' || item.status == '已取消'}">style="color:red" </c:if>
                                <c:if test="${item.status == '已撤回'}">style="color:#ff6e0c" </c:if>>

                            <td>
                                    <%-- <a href="workspace-endProcessInstance.do?processInstanceId=${item.id}">终止</a>
                                    <a href="workspace-remind.do?processInstanceId=${item.id}&userId=&comment=">催办</a>
                                    <a href="workspace-skip.do?processInstanceId=${item.id}&userId=&comment=">跳过</a>--%>
                                    <!-- ckx add 2018/8/29  新增抄送功能 -->
	          						<a href="${tenantPrefix}/workOperationCustom/custom-copy-edit.do?processInstanceId=${item.processInstanceId}" >抄送</a>
                                <c:if test="${item.proFlag != '自定义申请'}">
                                    <c:if test="${item.status=='未审核'||item.status=='审核中'||item.status=='审核中（驳回）'}">
                                        <a href="javascript:"
                                           onclick="fndrawwith( ${item.id} )">撤回</a>
                                    </c:if>
                                    
                                    <a href="workspace-viewHistoryFrom.do?processInstanceId=${item.id}&url=${item.url}"
                                       target="_blank">详情</a>
                                    <a href="${tenantPrefix}${item.url}?processInstanceId=${item.processInstanceId}&isPrint=true"
                                       target="_blank">打印</a>
                                </c:if>
                                <c:if test="${item.proFlag == '自定义申请'}">
                                    <c:if test="${item.status=='未审核'||item.status=='审核中'||item.status=='审核中（驳回）'}">
                                        <%--<a href="workspace-withdraw-custom.do?processInstanceId=${item.processInstanceId}" onclick="if(confirm('请确认是否要撤回此申请！')==false) return false;">撤回</a> --%>
                                        <a href="javascript:"
                                           onclick="fndrawwithcus( ${item.processInstanceId} )">撤回</a>
                                    </c:if>
                                    <a href="workspace-viewHistoryFrom.do?processInstanceId=${item.processInstanceId}&url=${item.url}"
                                       target="_blank">详情</a>
                                    <a href="${tenantPrefix}${item.url}&processInstanceId=${item.processInstanceId}&isPrint=true"
                                       target="_blank">打印</a>
                                </c:if>
                            </td>
                            <td>${item.applyCode}</td>
                            <td>
                                <c:if test="${item.proFlag != '自定义申请'}">
                                    <a class="workTask_title rwop" title="${item.theme}"
                                       href="${tenantPrefix}${item.url}?processInstanceId=${item.processInstanceId}&isPrint=false&number=<%=Math.random()%>"
                                       target="_blank">${item.theme}</a>
                                </c:if>
                                <c:if test="${item.proFlag == '自定义申请'}">
                                    <a class="workTask_title rwop" title="${item.theme}"
                                       href="${tenantPrefix}${item.url}&processInstanceId=${item.processInstanceId}&isPrint=false&number=<%=Math.random()%>"
                                    target="_blank">${item.theme}</a>
                                </c:if>
                            </td>
                            <td>${item.status} <%-- <tags:processName processDefinitionId="${item.processDefinitionId}"/> --%></td>
                            <td>${item.ucode}</td>
                            <td>${item.businessTypeName }</td>
                            <td><span class="workTask_title rwop"
                                      title="${item.businessDetailName }">${item.businessDetailName }</span></td>
                            <td>${item.submitTimes}</td>
                            <td><fmt:formatDate value="${item.startTime}"
                                                pattern="yyyy-MM-dd HH:mm:ss"/></td>
                            <td><fmt:formatDate value="${item.completeTime}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
                </div>
            </div>
        </form>

        <div>
            <div class="m-page-info pull-left">共100条记录 显示1到10条记录</div>

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
