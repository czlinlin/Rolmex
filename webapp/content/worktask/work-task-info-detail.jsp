<%@page contentType="text/html;charset=UTF-8" %>
<%@include file="/taglibs.jsp" %>
<%pageContext.setAttribute("currentHeader", "pim");%>
<%pageContext.setAttribute("currentMenu", "worktask");%>
<!doctype html>
<html lang="en">

<head>
    <%@include file="/common/meta.jsp" %>
    <title>麦联</title>
    <%@include file="/common/s3.jsp" %>
    <style type="text/css">
        .alignRight {
            text-align: right;
            padding-right: 5px;
        }

        .col-md-8, .col-sm-8 {
            padding-left: 5px;
        }
    </style>
    <script type="text/javascript">
        $(function () {
            $("#ataskchild").click(function () {
                if ($("#divChild").is(":hidden")) {
                    $("#divChild").show();
                    $(this).html("折叠");
                }
                else {
                    $("#divChild").hide();
                    $(this).html("查看");
                }
            })
        })
    </script>
    <style>
        pre {
            white-space: pre-wrap;
            word-wrap: break-word;
        }
    </style>
    <script>
        function a() {
            var urlP = document.referrer;
            //alert(urlP);
            //alert(urlP.indexOf("temp") >= 0 || urlP.indexOf("sent")>=0);
            if (urlP.indexOf("temp") >= 0 || urlP.indexOf("sent") >= 0) {
                // alert("------返回并且刷新-----------" + urlP);
                window.location.assign(urlP);
            } else {
                // alert("---返回历史中的上一个网页---");
                window.history.back(-1);
            }
        }
    </script>
</head>

<body>
<%@include file="/header/navbar.jsp" %>

<div class="row-fluid">
    <%@include file="/menu/sidebar.jsp" %>
    <!-- start of main -->
    <section id="m-main" class="col-md-10" style="margin-top:65px;">

        <div class="panel panel-default">
            <div class="panel-heading">
                任务详情
            </div>

            <div class="panel-body">
                <input id="datastatus" type="hidden" name="datastatus">
                <h4>任务</h4>
                <hr>
                <%--     <c:if test="${proInfoId!= null}">
                     <div class="row">
                         <label class="col-md-2 alignRight">上级项目：</label>
                         <div class="col-md-8">
                             <input id="proInfoTitle" type="hidden" name="proInfoTitle"
                                    value="${proInfoId}" size="40" class="form-control">
                             <a href="${ctx}/project/work-project-info-detail.do?id=${proInfoId}"> ${proInfoTitle}</a>
                         </div>
                     </div>
                     </c:if>--%>

                <c:if test="${proInfoId!= null}">
                    <div class="row">
                        <div class="col-md-6">
                            <label class="control-label col-md-4 alignRight" for="workTaskInfo_starttime">上级项目：</label>
                            <div class="col-sm-8">
                                <input id="proInfoTitle" type="hidden" name="proInfoTitle"
                                       value="${proInfoId}" size="40" class="form-control">
                                <a href="${ctx}/project/work-project-info-detail.do?id=${proInfoId}"> ${proInfoTitle}</a>
                            </div>
                        </div>
                        <div class="col-md-6">

                        </div>
                    </div>
                </c:if>
                <c:if test="${model.uppercode!= 0}">
                    <div class="row">
                        <label class="col-md-2 alignRight">上级任务:</label>
                        <div class="col-md-8">
                            <input id="WorkTaskInfo_uppercode" type="hidden" name="uppercode"
                                   value="${uppercode}" size="40" class="form-control">
                            <a href="work-task-info-detail.do?id=${model.uppercode}"> ${uppercode_show}</a>
                        </div>
                    </div>
                </c:if>

                <div class="row">
                    <div class="col-md-6">
                        <label class="control-label col-md-4 alignRight" for="workTaskInfo_starttime">标题：</label>
                        <div class="col-sm-8">
                            ${model.title}
                        </div>
                    </div>
                    <div class="col-md-6">

                    </div>
                </div>

                <div class="row">
                    <div class="col-md-12">
                        <label class="control-label col-md-2 alignRight" for="workTaskInfo_content">内容描述：&nbsp;</label>
                        <div class="col-md-8">
                            <pre>${model.content}</pre>
                        </div>
                    </div>
                </div>

                <div class="row">
                    <div class="col-md-6">
                        <label class="control-label col-md-4 alignRight" for="workTaskInfo_starttime">计划开始时间：</label>
                        <div class="col-sm-8">
                            <fmt:formatDate value='${model.starttime}' pattern='yyyy-MM-dd HH:mm'/>
                        </div>
                    </div>
                    <div class="col-md-6">
                        <label class="control-label col-md-4 alignRight" for="workTaskInfo_plantime">计划完成时间：</label>
                        <div class="col-sm-8">
                            <fmt:formatDate value='${model.plantime}' pattern='yyyy-MM-dd HH:mm'/>
                        </div>
                    </div>
                </div>

                <div class="row">
                    <div class="col-md-6">
                        <label class="control-label col-md-4 alignRight">负责人：</label>
                        <div class="col-sm-8">
                            <div class="input-group userPicker">
                                <tags:isDelUser userId="${model.leader}"/>
                            </div>
                        </div>
                    </div>
                    <div class="col-md-6">
                        <label class="control-label col-md-4 alignRight" for="workTaskInfo_workload">工作量(人/时)：</label>
                        <div class="col-md-8">
                            ${model.workload}
                        </div>
                    </div>
                </div>

                <div class="row">
                    <c:if test="${model.datastatus=='1'}">
                        <div class="col-md-6">
                            <label class="control-label col-md-4 alignRight">任务状态：</label>
                            <div class="col-md-8">
                                <c:if test='${model.status=="0"}'>已发布</c:if>
                                <c:if test='${model.status=="1"}'>进行中</c:if>
                                <c:if test='${model.status=="2"}'>已完成</c:if>
                                <c:if test='${model.status=="3"}'>已关闭</c:if>
                                <c:if test='${model.status=="4"}'>已评价</c:if>
                            </div>
                        </div>
                        <c:if test="${model.status=='1' || model.status=='2' ||model.status=='4'}">
                            <div class="col-md-6">
                                <label class="control-label col-md-4 alignRight">实际开始时间：</label>
                                <div class="col-sm-8">
                                    <div class="input-group userPicker">
                                        <fmt:formatDate value='${model.exectime}' pattern='yyyy-MM-dd HH:mm:ss'/>
                                    </div>
                                </div>
                            </div>
                        </c:if></c:if>
                </div>
                <div class="row">
                    <div class="col-md-6">
                        <label class="control-label col-md-4 alignRight">抄送人：</label>
                        <div class="col-sm-8">
                            ${ccnames}
                        </div>
                    </div>
                    <div class="col-md-6">
                    </div>
                </div>
                <div class="row">
                    <div class="col-md-6">
                        <label class="control-label col-md-4 alignRight">发布人：</label>
                        <div class="col-md-8">
                            <tags:isDelUser userId="${model.publisher}"/>
                        </div>
                    </div>
                    <div class="col-md-6">
                        <c:if test="${model.datastatus=='1'}">
                            <label class="control-label col-md-4 alignRight">发布时间：</label></c:if>
                        <c:if test="${model.datastatus=='0'}">
                            <label class="control-label col-md-4 alignRight">添加时间：</label></c:if>
                        <div class="col-md-8">
                            <fmt:formatDate value='${model.publishtime}' pattern='yyyy-MM-dd HH:mm:ss'/>
                        </div>
                    </div>
                </div>

                <div class="row">
                    <div class="col-md-6">
                        <label class="control-label col-md-4 alignRight" for="fileName">附件：</label>
                        <div class="col-sm-8">
                            <%@include file="/common/show_file.jsp" %>
                        </div>
                    </div>
                    <div class="col-md-6">
                    </div>
                </div>

                <div class="row">
                    <div class="col-md-12">
                        <label class="control-label col-md-2 alignRight">备注：</label>
                        <div class="col-sm-8">
                           <pre>${model.remarks}</pre>
                        </div>
                    </div>
                </div>


                <c:if test="${model.committime!=null}">
                    <div class="row">
                        <div class="col-md-6">
                            <label class="control-label col-md-4 alignRight">
                                <c:if test='${model.status=="2" or model.status=="4"}'>
                                    提交时间：
                                </c:if>
                                <c:if test='${model.status=="3"}'>
                                    关闭时间：
                                </c:if>
                            </label>
                            <div class="col-sm-8">
                                <fmt:formatDate value='${model.committime}' pattern='yyyy-MM-dd HH:mm'/>
                            </div>
                        </div>
                    </div>
                    <div class="row">
                        <c:if test='${model.status=="2" or model.status=="4"}'>
                            <div class="col-md-6">
                                <label class="control-label col-md-4 alignRight">
                                    完成效率：
                                </label>
                                <div class="col-sm-8">
                                    <c:if test='${model.efficiency=="0"}'>准时</c:if>
                                    <c:if test='${model.efficiency=="1"}'>提前</c:if>
                                    <c:if test='${model.efficiency=="2"}'>延期</c:if>
                                </div>
                            </div>
                        </c:if>
                    </div>
                </c:if>

                <c:if test='${model.status=="2" or model.status=="4"}'>
                    <div class="row">
                        <div class="col-md-12">
                            <label class="control-label col-md-2 alignRight">提交备注：</label>
                            <div class="col-sm-8">
                                   <pre> ${model.annex}</pre>
                            </div>
                        </div>
                    </div>
                    <div class="row">
                        <div class="col-md-6">
                            <label class="control-label col-md-4 alignRight" name="fileName">提交附件：</label>
                            <div class="col-md-8 ">
                                <c:if test="${StoreSubmitInfos==null or StoreSubmitInfos.size()==0}">
                                    无
                                </c:if>
                                <c:if test="${StoreSubmitInfos!=null and StoreSubmitInfos.size()>0}">
                                    <div id="divShowSubmitImg">
                                        <c:forEach items="${StoreSubmitInfos}" var="storesubmitInfo">
                                            <c:if test='${fn:contains(storesubmitInfo.path,".jpg")
                            	or fn:contains(storesubmitInfo.path,".gif")
                            	or fn:contains(storesubmitInfo.path,".png")
                            	or fn:contains(storesubmitInfo.path,".bmp")}'>

                                                <img style="width:100px;height:100px;"
                                                     src="${picUrl}/${storesubmitInfo.path}"/>

                                            </c:if>
                                        </c:forEach>
                                        <script>
                                            $(function () {
                                                $('#divShowSubmitImg').viewer({
                                                    url: 'src',
                                                });
                                            });
                                        </script>
                                    </div>
                                    <br/>
                                    <c:forEach items="${StoreSubmitInfos}" var="storesubmitInfo">
                                        <c:if test='${!fn:contains(storesubmitInfo.path,".jpg")
                            	and !fn:contains(storesubmitInfo.path,".gif")
                            	and !fn:contains(storesubmitInfo.path,".png")
                            	and !fn:contains(storesubmitInfo.path,".bmp")}'>
                                            <a target="_blank"
                                               href="${tenantPrefix}/downloadAmachment/download.do?id=${storesubmitInfo.id}">${storesubmitInfo.name}
                                            </a>
                                            <br/>
                                        </c:if>

                                    </c:forEach>
                                </c:if>

                            </div>
                        </div>
                    </div>
                </c:if>

                <c:if test='${model.status=="4"}'>
                    <div class="row">
                        <label class="control-label col-md-2 alignRight">评分星级：</label>
                        <div class="col-md-8">
                            <c:forEach var="i" begin="1" end="5">
                                <c:if test='${i<=model.evalscore}'>
                                    <img src="${cdnPrefix}/worktask/star_yellow.png">
                                </c:if>
                                <c:if test='${i>model.evalscore}'>
                                    <img src="${cdnPrefix}/worktask/star_gray.png">
                                </c:if>
                            </c:forEach>
                        </div>
                    </div>
                    <div class="row">
                        <label class="control-label col-md-2 alignRight">评分时间：</label>
                        <div class="col-md-8">
                            <fmt:formatDate value='${model.evaltime}' pattern='yyyy-MM-dd HH:mm:ss'/>
                        </div>
                    </div>
                    <div class="row">
                        <label class="control-label col-md-2 alignRight">评分内容：</label>
                        <div class="col-md-8">
                            <pre>${model.evaluate}</pre>
                        </div>
                    </div>
                </c:if>
                <hr>
                <c:if test="${model.uppercode==0}">
                    <c:if test="${model.status!='3' }">
                        <h4>
                            子任务（
                            <c:if test='${childnum>0}'>
                                [<a id="ataskchild" href="javascript:" onclick="">查看</a>]
                            </c:if>
                            <c:if test='${childnum<1}'>
                                无
                            </c:if>
                            &nbsp;子任务数：${childnum}）
                        </h4>
                    </c:if>
                </c:if>
                <hr>
                <div id="divChild" style="display:none;">
                    <c:forEach items="${childlist}" var="child">
                        <div class="row">
                            <div class="col-md-6">
                                <label class="control-label col-md-4 alignRight"
                                       for="workTaskInfo_starttime">标题：</label>
                                <div class="col-sm-8">
                                        ${child.title}
                                </div>
                            </div>
                            <div class="col-md-6">

                            </div>
                        </div>

                        <div class="row">
                            <div class="col-md-12">
                                <label class="control-label col-md-2 alignRight">内容描述：&nbsp;</label>
                                <div class="col-md-8">
                                    <pre>${child.content}</pre>
                                </div>
                            </div>
                        </div>

                        <div class="row">
                            <div class="col-md-6">
                                <label class="control-label col-md-4 alignRight"
                                       for="workTaskInfo_starttime">计划开始时间：</label>
                                <div class="col-sm-8">
                                    <fmt:formatDate value='${child.starttime}' pattern='yyyy-MM-dd HH:mm'/>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <label class="control-label col-md-4 alignRight"
                                       for="workTaskInfo_plantime">计划完成时间：</label>
                                <div class="col-sm-8">
                                    <fmt:formatDate value='${child.plantime}' pattern='yyyy-MM-dd HH:mm'/>
                                </div>
                            </div>
                        </div>
                        <div class="row">
                            <div class="col-md-6">
                                <label class="control-label col-md-4 alignRight">负责人：</label>
                                <div class="col-sm-8">
                                    <div class="input-group userPicker">
                                        <tags:isDelUser userId="${child.leader}"/>
                                    </div>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <label class="control-label col-md-4 alignRight"
                                       for="workTaskInfo_workload">工作量(人/时)：</label>
                                <div class="col-md-8">
                                        ${child.workload}
                                </div>
                            </div>
                        </div>
                        <div class="row">
                            <div class="col-md-6">
                                <label class="control-label col-md-4 alignRight">抄送人：</label>
                                <div class="col-sm-8">
                                        ${child.ccshow}
                                </div>
                            </div>
                            <div class="col-md-6">
                            </div>
                        </div>

                        <div class="row">
                            <div class="col-md-6">
                                <label class="control-label col-md-4 alignRight">任务状态：</label>
                                <div class="col-md-8">
                                    <c:if test='${child.status=="0"}'>已发布</c:if>
                                    <c:if test='${child.status=="1"}'>进行中</c:if>
                                    <c:if test='${child.status=="2"}'>已完成</c:if>
                                    <c:if test='${child.status=="3"}'>已关闭</c:if>
                                    <c:if test='${child.status=="4"}'>已评价</c:if>
                                </div>
                            </div>
                            <c:if test="${child.status=='1' || child.status=='2' ||child.status=='4'}">
                                <div class="col-md-6">
                                    <label class="control-label col-md-4 alignRight">实际开始时间：</label>
                                    <div class="col-sm-8">
                                        <div class="input-group userPicker">
                                            <fmt:formatDate value='${child.exectime}' pattern='yyyy-MM-dd HH:mm:ss'/>
                                        </div>
                                    </div>
                                </div>
                            </c:if>
                        </div>


                        <div class="row">
                            <div class="col-md-6">
                                <label class="control-label col-md-4 alignRight" name="fileName">附件：</label>
                                <div class="col-sm-8 ">
                                    <c:if test="${child.storeInfos.size()==0}">
                                        无
                                    </c:if>
                                    <c:if test="${child.storeInfos.size()!=0}">
                                        <div id="divShowImg${child.id}">
                                            <c:forEach items="${child.storeInfos}" var="storeCInfo">
                                                <c:if test='${fn:contains(storeCInfo.path,".jpg")
                            	or fn:contains(storeCInfo.path,".gif")
                            	or fn:contains(storeCInfo.path,".png")
                            	or fn:contains(storeCInfo.path,".bmp")}'>

                                                    <img style="width:100px;height:100px;"
                                                         src="${picUrl}/${storeCInfo.path}"/>

                                                </c:if>
                                            </c:forEach>
                                        </div>
                                        <c:forEach items="${child.storeInfos}" var="storeCInfo">
                                            <c:if test='${!fn:contains(storeCInfo.path,".jpg")
                            	and !fn:contains(storeCInfo.path,".gif")
                            	and !fn:contains(storeCInfo.path,".png")
                            	and !fn:contains(storeCInfo.path,".bmp")}'>
                                                <a target="_blank"
                                                   href="${tenantPrefix}/downloadAmachment/download.do?id=${storeCInfo.id}">${storeCInfo.name}
                                                </a>
                                            </c:if>
                                            <br/>
                                        </c:forEach>
                                    </c:if>
                                    <script>
                                        $(function () {
                                            $('#divShowImg' + '${child.id}').viewer({
                                                url: 'src',
                                            });
                                        });
                                    </script>
                                </div>
                            </div>
                            <div class="col-md-6">
                            </div>
                        </div>

                        <div class="row">
                            <div class="col-md-6">
                                <label class="control-label col-md-4 alignRight">发布人姓名：</label>
                                <div class="col-md-8">
                                    <tags:isDelUser userId="${child.publisher}"/>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <label class="control-label col-md-4 alignRight">发布人时间：</label>
                                <div class="col-md-8">
                                    <fmt:formatDate value='${child.publishtime}' pattern='yyyy-MM-dd HH:mm:ss'/>
                                </div>
                            </div>
                        </div>

                        <div class="row">
                            <div class="col-md-12">
                                <label class="control-label col-md-2 alignRight">备注：</label>
                                <div class="col-sm-8">
                                    <pre>${child.remarks}</pre>
                                </div>
                            </div>
                        </div>
                        <c:if test="${child.committime!=null}">
                            <div class="row">
                                <div class="col-md-6">
                                    <label class="control-label col-md-4 alignRight">
                                        <c:if test='${child.status=="2" or child.status=="4" }'>
                                            提交时间：
                                        </c:if>
                                        <c:if test='${child.status=="3"}'>
                                            关闭时间：
                                        </c:if>
                                    </label>
                                    <div class="col-sm-8">
                                        <fmt:formatDate value='${child.committime}' pattern='yyyy-MM-dd HH:mm'/>
                                    </div>
                                </div>
                            </div>

                            <div class="row">
                            <c:if test='${child.status=="2" or child.status=="4" }'>
                                <div class="col-md-6">
                                    <label class="control-label col-md-4 alignRight">
                                        完成效率：
                                    </label>
                                    <div class="col-sm-8">
                                        <c:if test='${child.efficiency=="0"}'>准时</c:if>
                                        <c:if test='${child.efficiency=="1"}'>提前</c:if>
                                        <c:if test='${child.efficiency=="2"}'>延期</c:if>
                                    </div>
                                </div>
                                </div>
                            </c:if>
                        </c:if>

                        <c:if test='${child.status=="2" or child.status=="4"}'>
                            <div class="row">
                                <div class="col-md-12">
                                    <label class="control-label col-md-2 alignRight">提交备注：</label>
                                    <div class="col-sm-8">
                                        <pre>${child.annex}</pre>
                                    </div>
                                </div>
                            </div>
                            <div class="row">
                                <div class="col-md-6">
                                    <label class="control-label col-md-4 alignRight" name="fileName">提交附件：</label>
                                    <div class="col-sm-8 ">
                                        <c:if test="${child.storeSubmitInfos==null or child.storeSubmitInfos.size()==0}">
                                            无
                                        </c:if>
                                        <c:if test="${child.storeSubmitInfos!=null and child.storeSubmitInfos.size()>0}">
                                            <div id="divShowSubmitImg${child.id}">
                                                <c:forEach items="${child.storeSubmitInfos}" var="storesubmitInfo">
                                                    <c:if test='${fn:contains(storesubmitInfo.path,".jpg")
                            	or fn:contains(storesubmitInfo.path,".gif")
                            	or fn:contains(storesubmitInfo.path,".png")
                            	or fn:contains(storesubmitInfo.path,".bmp")}'>

                                                        <img style="width:100px;height:100px;"
                                                             src="${picUrl}/${storesubmitInfo.path}"/>

                                                    </c:if>
                                                </c:forEach>
                                                <script>
                                                    $(function () {
                                                        $('#divShowSubmitImg' + '${child.id}').viewer({
                                                            url: 'src',
                                                        });
                                                    });
                                                </script>
                                            </div>
                                            <br/>
                                            <c:forEach items="${child.storeSubmitInfos}" var="storesubmitInfo">
                                                <c:if test='${!fn:contains(storesubmitInfo.path,".jpg")
                            	and !fn:contains(storesubmitInfo.path,".gif")
                            	and !fn:contains(storesubmitInfo.path,".png")
                            	and !fn:contains(storesubmitInfo.path,".bmp")}'>
                                                    <a target="_blank"
                                                       href="${tenantPrefix}/downloadAmachment/download.do?id=${storesubmitInfo.id}">${storesubmitInfo.name}
                                                    </a>
                                                    <br/>
                                                </c:if>

                                            </c:forEach>
                                        </c:if>

                                    </div>
                                </div>
                            </div>
                        </c:if>

                        <c:if test='${child.status=="4"}'>
                            <div class="row">
                                <label class="control-label col-md-2 alignRight">评分星级：</label>
                                <div class="col-md-8">
                                    <c:forEach var="i" begin="1" end="5">
                                        <c:if test='${i<=child.evalscore}'>
                                            <img src="${cdnPrefix}/worktask/star_yellow.png">
                                        </c:if>
                                        <c:if test='${i>child.evalscore}'>
                                            <img src="${cdnPrefix}/worktask/star_gray.png">
                                        </c:if>
                                    </c:forEach>
                                </div>
                            </div>
                            <div class="row">
                                <label class="control-label col-md-2 alignRight">评分时间：</label>
                                <div class="col-md-8">
                                    <fmt:formatDate value='${child.evaltime}' pattern='yyyy-MM-dd HH:mm:ss'/>
                                </div>
                            </div>
                            <div class="row">
                                <label class="control-label col-md-2 alignRight">评分内容：</label>
                                <div class="col-md-8">
                                        ${child.evaluate}
                                </div>
                            </div>
                        </c:if>
                        <hr>
                    </c:forEach>
                </div>
            </div>
            <br> <br>
            <div class="row">
                <div class="col-md-offset-5 col-md-5">
                    <button type="button" class="btn btn-default"
                            onclick="a();">返回
                    </button>
                </div>
            </div>
            <br> <br>
        </div>


    </section>


    <!-- end of main -->
</div>

</body>

</html>
