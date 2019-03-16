<%--
  
  User: wanghan
  Date: 2017\10\24 0024
  Time: 15:06
 
--%>
<%@page contentType="text/html;charset=UTF-8" %>
<%@include file="/taglibs.jsp" %>
<%pageContext.setAttribute("currentHeader", "pim");%>
<%pageContext.setAttribute("currentMenu", "msg");%>
<!doctype html>
<html lang="en">

<head>
    <%@include file="/common/meta.jsp" %>
    <title>麦联</title>
    <%@include file="/common/s3.jsp" %>
    <script type="text/javascript" src="${cdnPrefix}/bootbox/bootbox.min1.js"></script>
    <script type="text/javascript">
        var config = {
            id: 'pimRemindGrid',
            pageNo: ${page.pageNo},
            pageSize: ${page.pageSize},
            totalCount: ${page.totalCount},
            resultSize: ${page.resultSize},
            pageCount: ${page.pageCount},
            orderBy: '${page.orderBy == null ? "" : page.orderBy}',
            asc: ${page.asc},
            params: {
                'filter_LIKES_name': '${param.filter_LIKES_name}',
                'filter_EQI_msgType': '${param.filter_EQI_msgType}',
                'filter_GED_createTime': '${param.filter_GED_createTime}'
            },
            selectedItemClass: 'selectedItem',
            gridFormId: 'pimRemindGridForm',
            exportUrl: 'pim-info-export.do',
            msgDelUrl: "${tenantPrefix}/rs/msg/msg-info-del",
        };

        var table;

        $(function () {
            table = new Table(config);
            table.configPagination('.m-pagination');
            table.configPageInfo('.m-page-info');
            table.configPageSize('.m-page-size');
            //设置段时间
            var sectionJson = [{"begin": "#pickerStartTime", "end": "#pickerEndTime"},
                {"begin": "#pickerPublicStartTime", "end": "#pickerPublicEndTime"}];
            fnSectionPickerTime(sectionJson)

        });
        //task del
        var fnMsgDel=function(id){
            var confirmDialog=bootbox.confirm({
                message: "确定要删除此消息吗？",
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
                    if(!result) return;

                    confirmDialog.modal('hide');
                    var loading = bootbox.dialog({
                        message:'<p class="text-center"><i class="fa fa-spin fa-spinner"></i>正在执行...</p>',
                        size:'small',
                        closeButton: false
                    });
                    $.ajax({
                        url: config.msgDelUrl,
                        type:"POST",
                        data:{id:id},
                        timeout:10000,
                        success: function(data) {
                            loading.modal('hide');
                            if(data==undefined||data==null||data==""){
                                bootbox.alert("删除操作失败");
                                return;
                            }

                            if(data.code=="200"){
                                //dialog.modal('hide')
                                var tip=bootbox.alert(
                                    {
                                        message:"删除操作成功！",
                                        callback:function(){
                                            //$("#btn_Search").click();
                                            document.getElementById('btn_Search').click();
                                            tip.modal('hide');

                                        }
                                    });
                            }
                            else
                                bootbox.alert(data.message);
                            return;
                        },
                        error:function(XMLHttpRequest, textStatus, errorThrown){
                            alert("["+XMLHttpRequest.status+"]error，请求失败")
                        },
                        complete:function(xh,status){
                            if(status=="timeout")
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
            width: 400px;
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
<%@include file="/header/pim3.jsp" %>

<div class="row-fluid">
    <%@include file="/menu/sidebar.jsp" %>

    <!-- start of main -->
    <section id="m-main" class="col-md-10" style="padding-top:65px;">

        <div class="panel panel-default">
            <div class="panel-heading">
                <i class="glyphicon glyphicon-list"></i>
                查询
                <div class="pull-right ctrl">
                    <a class="btn btn-default btn-xs"><i id="orgSearchIcon" class="glyphicon glyphicon-chevron-up"></i></a>
                </div>
            </div>

            <div class="panel-body">
                <form name="msgForm" method="post" action="msg-info-listRead.do" class="form-inline ">
                    <label for="subject">标题:</label>
                    <input type="text" id="msgInfo_name" name="filter_LIKES_name"  value="${param.filter_LIKES_name}"
                           class="form-control">
                    <%--    &nbsp;
                        <label for="org_name">状态:</label>
                        <select id="msgInfo_Status" class="form-control" name="filter_EQI_status" title="">
                            <option value="" ${param.filter_EQI_status==""?"selected='selected'":""}>全部</option>
                            <option value="0"  ${param.filter_EQI_status=="0"?"selected='selected'":""}>未读</option>
                            <option value="1" ${param.filter_EQI_status=="1"?"selected='selected'":""}>已读</option>
                        </select>--%>
                    &nbsp;
                    <label for="msg_type">类型:</label>
                    <select id="msg_type" class="form-control" name="filter_EQI_msgType" title="">
                        <option value="" ${param.filter_EQI_msgType==""?"selected='selected'":""}>全部</option>
                        <option value="0"  ${param.filter_EQI_msgType=="0"?"selected='selected'":""}>流程</option>
                        <option value="1" ${param.filter_EQI_msgType=="1"?"selected='selected'":""}>项目</option>
                        <option value="2" ${param.filter_EQI_msgType=="2"?"selected='selected'":""}>任务</option>
                        <option value="3" ${param.filter_EQI_msgType=="3"?"selected='selected'":""}>议题</option>
                        <option value="4" ${param.filter_EQI_msgType=="4"?"selected='selected'":""}>汇报</option>
                        <option value="5" ${param.filter_EQI_msgType=="5"?"selected='selected'":""}>公告</option>
                    </select>

                    &nbsp;
                    <label>发布时间：</label>
                    <div id="pickerPublicStartTime" class="input-group  datepickerbegin date">
                        <input style="width:160px;" type="text" name="filter_GED_createTime"
                               value="${param.filter_GED_createTime}" class="form-control required valid" readonly>
                        <span class="input-group-addon">
					<i class="glyphicon glyphicon-calendar"></i>
				</span>
                    </div>
                    至
                    <div id="pickerPublicEndTime" class="input-group  datepickerend date">
                        <input style="width:160px;" type="text" name="filter_LED_createTime"
                               value="${param.filter_LED_createTime}" class="form-control required valid" readonly>
                        <span class="input-group-addon">
					<i class="glyphicon glyphicon-calendar"></i>
				</span>
                    </div>
                    <button  id="btn_Search" class="btn btn-default a-search" onclick="document.msgForm.submit()">查询</button>
                    &nbsp;
                </form>
            </div>

        </div>
        <div style="margin-bottom: 20px;">
            <div class="pull-right">
                每页显示 <select class="m-page-size form-control"
                             style="display: inline; width: auto;">
                <option value="10">10</option>
                <option value="20">20</option>
                <option value="50">50</option>
            </select> 条
            </div>

            <div class="clearfix"></div>
        </div>

        <form id="pimRemindGridForm" name="pimRemindGridForm" method='post' action="pim-info-remove.do"
              class="m-form-blank mytable">
            <div class="panel panel-default">
                <div class="panel-heading">
                    已读消息
                </div>
                <table id="pimRemindGrid" class="table table-hover">
                    <thead>
                    <tr>
                        <th>操作</th>
                        <th class="sorting" name="title">标题</th>
                        <th class="sorting" name="type">类型</th>
                        <%--  <th class="sorting" name="name">发送人</th>--%>
                        <th class="sorting" name="createTime">发送时间</th>
                        <%--<th class="sorting" name="name">状态</th>--%>
                    </tr>
                    </thead>

                    <tbody>
                    <c:forEach items="${page.result}" var="item">
                        <tr>
                            <td>  &nbsp;&nbsp;<a href="javascript:" onclick="fnMsgDel(${item.id})">删除</a></td>
                            <td><a class="workTask_title rwop" title="${item.name}" href="msg-info-readView.do?id=${item.id}">${item.name}</a></td>
                            <td>
                                <c:if test='${item.msgType=="0"}'>流程</c:if>
                                <c:if test='${item.msgType=="1"}'>项目</c:if>
                                <c:if test='${item.msgType=="2"}'>任务</c:if>
                                <c:if test='${item.msgType=="3"}'>议题</c:if>
                                <c:if test='${item.msgType=="4"}'>汇报</c:if>
                                <c:if test='${item.msgType=="5"}'>公告</c:if>
                            </td>
                                <%--  <td><tags:user userId="${item.senderId}"/></td>--%>
                            <td><fmt:formatDate value="${item.createTime}" type="both"/></td>
                                <%--    <td>${item.status == 0 ? '未读' : '已读'}</td>--%>
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

    </section>
    <!-- end of main -->
</div>

</body>

</html>
