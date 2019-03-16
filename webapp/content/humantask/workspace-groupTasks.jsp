<%@page contentType="text/html;charset=UTF-8" %>
<%@include file="/taglibs.jsp" %>
<%
    pageContext.setAttribute("currentHeader", "pim");
%>
<%
    pageContext.setAttribute("currentMenu", "task");
%>
<%
    pageContext.setAttribute("currentChildMenu", "待领任务");
%>
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
                'filter_LIKES_full_name': '${param.filter_LIKES_full_name}',
                'filter_LIKES_ucode': '${param.filter_LIKES_ucode}',
                'filter_EQS_systemid': '${param.filter_EQS_systemid}',
                'filter_EQS_areaId': '${param.filter_EQS_areaId}',
                'filter_EQS_companyid': '${param.filter_EQS_companyid}',
                'filter_EQS_businessTypeId': '${param.filter_EQS_businessTypeId}',
                'filter_EQS_businessDetailId': '${param.filter_EQS_businessDetailId}',
                'filter_GED_create_time': '${param.filter_GED_create_time}',
                'filter_LED_create_time': '${param.filter_LED_create_time}'
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

                <form id="charege-infoForm" name="charege-infoForm" method="post" action="workspace-groupTasks.do"
                      class="form-inline">
                    <div class="row">
                        <div class="col-md-4">
                            <label>受理单号:</label>
                            <input type="text" style="width:200px;" maxlength="30" id="charege-info_applyCode"
                                   name="filter_LIKES_applyCode" value="${param.filter_LIKES_applyCode}"
                                   class="form-control" onchange="fntrim(this)">
                        </div>
                        <div class="col-md-4">
                            <label >主题:</label>
                            <input type="text" style="width:200px;" maxlength="30" id="charege-theme"
                                   name="filter_LIKES_theme" value="${param.filter_LIKES_theme}"
                                   class="form-control" onchange="fntrim(this)">
                        </div>
                    </div>
                    <br>
                    <div class="row">
                        <div class="col-md-4">
                            <label >申请人:</label>
                            <input type="text" style="width:200px;" maxlength="15" id="charege-info_full_name"
                                   name="filter_LIKES_full_name" value="${param.filter_LIKES_full_name}"
                                   class="form-control" onchange="fntrim(this)">
                        </div>
                        <div class="col-md-4">
                            <label >经销商编号:</label>
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
                            <button id="btn_Search" class="btn btn-default a-search" type="submit">
                                查询
                            </button>
                        </div>
                    </div>
                </form>
            </div>
        </div>

        <!-- <div style="margin-bottom: 20px;">

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
  </div> -->

        <form id="pimRemindGridForm" name="pimRemindGridForm" method='post'
              action="pim-note-remove.do" class="m-form-blank">
            <div class="panel panel-default">
                <div class="panel-heading">
                    <i class="glyphicon glyphicon-list"></i> 列表
                </div>
                <div style="overflow-x: scroll">
                    <table id="pimRemindGrid" class="table table-hover mytable" style="min-width:1400px;">
                        <thead>
                        <tr>
                            <th>操作</th>
                            <th>受理单号</th>
                            <th>主题</th>
                            <th>申请人</th>
                            <th>经销商编号</th>
                            <th>业务类型</th>
                            <th>业务细分</th>
                            <th>所属体系</th>
                            <th>所属大区</th>
                            <th>所属分公司</th>
                            <th>申请时间</th>
                            <th>最后审批时间</th>
                        </tr>
                        </thead>

                        <tbody>
                        <c:forEach items="${page.result}" var="item">
                            <tr>
                                <td><a href="workspace-claimTask.do?taskId=${item.id}">认领</a>
                                    <a
                                            href="${tenantPrefix}/bpm/workspace-viewHistory.do?processInstanceId=${item.processInstanceId}&url=${item.url}"
                                            target="_blank">历史</a></td>
                                <td>${item.applyCode}</td>

                                <td><a class="workTask_title rwop" title="${item.theme}"
                                       href="${tenantPrefix}${item.url}?processInstanceId=${item.processInstanceId}"
                                       target="_blank">${item.theme}</a></td>
                                <td>${item.applyUserName}</td>
                                <td>${item.ucode}</td>
                                <td>${item.businessTypeName }</td>
                                <td><span class="workTask_title rwop"
                                          title="${item.businessDetailName }">${item.businessDetailName }</span></td>
                                <td>${item.systemName }</td>
                                <td>${item.areaName == "null" ? "" : item.areaName}</td>
                                <td>${item.companyName }</td>
                                <td><fmt:formatDate value="${item.startTime}"
                                                    pattern="yyyy-MM-dd HH:mm:ss"/></td>
                                <td><fmt:formatDate value="${item.completeTime}"
                                                    pattern="yyyy-MM-dd HH:mm:ss" /></td>
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
