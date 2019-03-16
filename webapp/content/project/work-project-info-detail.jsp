<%--
  
  User: wanghan
  Date: 2017\9\9 0009
  Time: 10:21
 
--%>
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
    <link type="text/css" rel="stylesheet" href="${cdnPrefix}/project/project.css">
    <style type="text/css">
        .alignRight {
            text-align: right;
            padding-right: 5px;
        }

        .col-md-8, .col-sm-8 {
            padding-left: 5px;
        }

        pre {
            white-space: pre-wrap;
            word-wrap: break-word;
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
            $(".btaskchildClass").click(function () {
                var divId = this.id;
                var taskId = divId.split("_")[1];
                if ($("#ChildTaskDiv_" + taskId).is(":hidden")) {
                    $("#ChildTaskDiv_" + taskId).show();
                    $(this).html("折叠");
                }
                else {
                    $("#ChildTaskDiv_" + taskId).hide();
                    $(this).html("查看");
                }
            })
        })

    </script>
    <script>
        function a() {
            var urlP = document.referrer;
            //alert(urlP);
            //alert(urlP.indexOf("temp") >= 0 || urlP.indexOf("sent")>=0);
            if (urlP.indexOf("temp") >= 0 || urlP.indexOf("list") >= 0) {
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
                项目详情
            </div>

            <div class="panel-body">
                <input id="datastatus" type="hidden" name="datastatus">
                <h4>项目详情</h4>
                <hr>
                <div class="row">
                    <div class="col-md-6">
                        <label class="control-label col-md-4 alignRight">项目名称：</label>
                        <div class="col-sm-8">
                            ${model.title}
                        </div>
                    </div>
                    <div class="col-md-6">
                        <label class="control-label col-md-4 alignRight">负责人：</label>
                        <div class="col-sm-8">
                            <div class="input-group userPicker">
                                <tags:isDelUser userId="${model.leader}"/>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="row">
                    <div class="col-md-12">
                        <label class="control-label col-md-2 alignRight">项目描述：&nbsp;</label>
                        <div class="col-md-8">
                            <pre>${model.content}</pre>

                        </div>
                    </div>
                </div>


                <div class="row">
                    <div class="col-md-6">
                        <label class="control-label col-md-4 alignRight" for="projectTaskInfo_startdate">计划开始日期：</label>
                        <div class="col-sm-8">
                            <fmt:formatDate value='${model.startdate}' pattern='yyyy-MM-dd'/>
                        </div>
                    </div>
                    <div class="col-md-6">
                        <label class="control-label col-md-4 alignRight" for="projectTaskInfo_plandate">计划完成日期：</label>
                        <div class="col-sm-8">
                            <fmt:formatDate value='${model.plandate}' pattern='yyyy-MM-dd'/>
                        </div>
                    </div>
                </div>

                <div class="row">
                    <c:if test="${model.datastatus!='0'}">
                        <c:if test="${model.status=='1' || model.status=='2' || model.status=='4'}">
                            <div class="col-md-6">
                                <label class="control-label col-md-4 alignRight"
                                       for="projectTaskInfo_startdate">实际开始时间：</label>
                                <div class="col-sm-8">
                                    <fmt:formatDate value='${model.exectime}' pattern='yyyy-MM-dd HH:mm:ss'/>
                                </div>
                            </div>
                            <div class="col-md-6">
                            </div>
                        </c:if>
                    </c:if>
                </div>
                <div class="row">
                    <c:if test="${model.datastatus!='0'}">
                        <div class="col-md-6">
                            <label class="control-label col-md-4 alignRight">状态：</label>
                            <div class="col-sm-8">
                                <div class="input-group userPicker">
                                    <c:if test='${model.status=="0"}'>已发布</c:if>
                                    <c:if test='${model.status=="1"}'>进行中</c:if>
                                    <c:if test='${model.status=="2"}'>已完成</c:if>
                                    <c:if test='${model.status=="3"}'>已关闭</c:if>
                                    <c:if test='${model.status=="4"}'>已评价</c:if>
                                </div>
                            </div>
                        </div>

                        <div class="col-md-6">
                        </div>
                    </c:if>
                </div>


                <div class="row">
                    <c:if test="${model.datastatus!='0'}">
                        <div class="col-md-6">
                            <label class="control-label col-md-4 alignRight">进度：</label>
                            <div class="col-sm-8">
                                <div class="input-group">
                                    <div class="blankrangle percent">
                                        <div class="task_progress div_progress">
                                            当前进度:${current}%
                                            目标进度:${target}%

                                        </div>
                                        <div class="task_progress target_progress target_${bg}"
                                             style="width:${target}%"></div>
                                        <div class="task_progress actual_progress actual_${bg}"
                                             style="width:${current}%"></div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </c:if>
                </div>

                <div class="row">
                    <div class="col-md-6">
                        <label class="control-label col-md-4 alignRight">发布人：</label>
                        <div class="col-md-8">
                            <tags:isDelUser userId="${model.publisher}"/>
                        </div>
                    </div>
                    <div class="col-md-6">
                        <c:if test="${model.datastatus=='0'}">
                            <label class="control-label col-md-4 alignRight">添加时间：</label>
                        </c:if>
                        <c:if test="${model.datastatus=='1'}">
                            <label class="control-label col-md-4 alignRight">发布时间：</label>
                        </c:if>
                        <div class="col-sm-8">
                            <fmt:formatDate value='${model.publishtime}' pattern='yyyy-MM-dd HH:mm:ss'/>
                        </div>
                    </div>
                </div>
                <div class="row">
                    <div class="col-md-6">
                        <label class="control-label col-md-4 alignRight">知会人：</label>
                        <div class="col-md-8">
                            ${notifynames}
                        </div>
                    </div>
                    <div class="col-md-6">
                    </div>
                </div>

                <%--  <c:if test="${model.datastatus!='0'}">
                      <div class="row">
                          <div class="col-md-6">
                              <label class="control-label col-md-4 alignRight">备注：</label>
                              <div class="col-sm-8">
                                  <pre>${model.remarks}</pre>
                              </div>
                          </div>
                      </div>
                  </c:if>--%>
                <c:if test="${model.committime!=null}">
                    <div class="row">
                        <div class="col-md-6">
                            <label class="control-label col-md-4 alignRight">提交/关闭时间：</label>
                            <div class="col-sm-8">
                                <fmt:formatDate value='${model.committime}' pattern='yyyy-MM-dd HH:mm'/>
                            </div>
                        </div>
                    </div>

                    <div class="row">
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
                    </div>
                </c:if>
                <div class="row">
                    <c:if test="${model.datastatus!='0'}">
                    <c:if test='${model.status=="2" or model.status=="4"}'>
                    <div class="col-md-6">
                        <label class="control-label col-md-4 alignRight">提交备注：</label>
                        <div class="col-sm-8">
                            <pre>${model.remarks}</pre>
                        </div>
                    </div>
                </div>
                <div class="row">
                    <div class="col-md-6">
                        <label class="control-label col-md-4 alignRight"
                               name="fileName">提交附件：</label>
                        <div class="col-md-8 alignLeft">
                            <c:if test="${StoreSubmitInfos==null or StoreSubmitInfos.size()==0}">
                                无
                            </c:if>
                            <c:if test="${StoreSubmitInfos!=null and StoreSubmitInfos.size()>0}">
                                <div id="divShowSubmitImg${model.id}">
                                    <c:forEach items="${StoreSubmitInfos}"
                                               var="storesubmitInfo">
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
                                            $('#divShowSubmitImg' + '${model.id}').viewer({
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
                    </c:if>
                    </c:if>
                </div>


                <div class="row">
                    <c:if test='${model.status=="4"}'>
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
                    <div class="col-md-12">
                        <label class="control-label col-md-2
                         alignRight">评分内容：</label>
                        <div class="col-md-8">
                            <pre>${model.evaluate}</pre>
                        </div>
                    </div>
                    </c:if>
                </div>


                <div class="row">
                    <div class="col-md-6">
                        <label class="control-label col-md-4 alignRight" name="fileName">附件：</label>
                        <div class="col-md-8 alignLeft">
                            <%@include file="/common/show_file.jsp" %>
                        </div>
                    </div>
                </div>


                <c:if test="${model.datastatus!='0'}">
                    <h4> &nbsp;&nbsp;任务信息（
                        <c:if test='${fChildNum>0}'>
                            [<a id="ataskchild" href="javascript:" onclick="">查看</a>]
                        </c:if>
                        <c:if test='${fChildNum<1}'>
                            无
                        </c:if>
                        &nbsp;任务数：${fChildNum}）
                    </h4>
                    <hr>
                </c:if>


                <div id="divChild" style="display:none;">
                    <c:forEach items="${workTaskInfos}" var="workTaskInfo">
                        <div class="row">
                            <div class="col-md-6">
                                <label class="control-label col-md-4 alignRight"
                                       for="workTaskInfo_starttime">标题：</label>
                                <div class="col-sm-8">
                                        ${workTaskInfo.title}
                                </div>
                            </div>
                            <div class="col-md-6">
                            </div>
                        </div>
                        <div class="row">
                            <div class="col-md-12">
                                <label class="control-label col-md-2 alignRight">内容描述：&nbsp;</label>
                                <div class="col-md-8">
                                    <pre>${workTaskInfo.content}</pre>
                                </div>
                            </div>
                        </div>
                        <div class="row">
                            <div class="col-md-6">
                                <label class="control-label col-md-4 alignRight">负责人：</label>
                                <div class="col-sm-8">
                                    <div class="input-group userPicker">
                                        <tags:isDelUser userId="${workTaskInfo.leader}"/>
                                    </div>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <label class="control-label col-md-4 alignRight"
                                       for="workTaskInfo_workload">工作量(人/时)：</label>
                                <div id="workTaskInfo_workload" class="col-md-8">
                                        ${workTaskInfo.workload}
                                </div>
                            </div>
                        </div>
                        <div class="row">
                            <div class="col-md-6">
                                <label class="control-label col-md-4 alignRight">抄送人：</label>
                                <div class="col-sm-8">
                                        ${workTaskInfo.ccshow}
                                </div>
                            </div>
                        </div>
                        <div class="row">
                            <div class="col-md-6">
                                <label class="control-label col-md-4 alignRight">发布人：</label>
                                <div class="col-sm-8">
                                    <tags:isDelUser userId="${workTaskInfo.publisher}"/>
                                </div>
                            </div>

                            <div class="col-md-6">
                                <label class="control-label col-md-4 alignRight"
                                       for="workTaskInfo_workload">发布时间：</label>
                                <div class="col-md-8">
                                    <fmt:formatDate value='${workTaskInfo.publishtime}' pattern='yyyy-MM-dd HH:mm:ss'/>
                                </div>
                            </div>
                        </div>
                        <div class="row">
                            <div class="col-md-6">
                                <label class="control-label col-md-4 alignRight">任务状态：</label>
                                <div class="col-md-8">
                                    <c:if test='${workTaskInfo.status=="0"}'>已发布</c:if>
                                    <c:if test='${workTaskInfo.status=="1"}'>进行中</c:if>
                                    <c:if test='${workTaskInfo.status=="2"}'>已完成</c:if>
                                    <c:if test='${workTaskInfo.status=="3"}'>已关闭</c:if>
                                    <c:if test='${workTaskInfo.status=="4"}'>已评价</c:if>
                                </div>
                            </div>
                            <c:if test="${workTaskInfo.datastatus!='0'}">
                                <c:if test="${workTaskInfo.status=='1' || workTaskInfo.status=='2' || workTaskInfo.status=='4'}">
                                    <div class="col-md-6">
                                        <label class="control-label col-md-4 alignRight">实际开始时间：</label>
                                        <div class="col-md-8">
                                            <fmt:formatDate value='${workTaskInfo.exectime}'
                                                            pattern='yyyy-MM-dd HH:mm:ss'/>
                                        </div>
                                    </div>
                                </c:if>
                            </c:if>
                        </div>
                        <div class="row">
                            <div class="col-md-12">
                                <label class="control-label col-md-2 alignRight">备注：</label>
                                <div class="col-md-8">
                                    <pre>${workTaskInfo.remarks}</pre>
                                </div>
                            </div>
                        </div>
                        <div class="row">
                            <div class="col-md-6">
                                <label class="control-label col-md-4 alignRight" name="fileName">附件：</label>
                                <div class="col-sm-8 ">
                                    <c:if test="${workTaskInfo.storeInfos.size()==0}">
                                        无
                                    </c:if>
                                    <c:if test="${workTaskInfo.storeInfos.size()!=0}">
                                        <div id="divShowImg${workTaskInfo.id}">
                                            <c:forEach items="${workTaskInfo.storeInfos}" var="storeCInfo">
                                                <c:if test='${fn:contains(storeCInfo.path,".jpg")
                                 or fn:contains(storeCInfo.path,".gif")
                                 or fn:contains(storeCInfo.path,".png")
                                 or fn:contains(storeCInfo.path,".bmp")}'>

                                                    <img style="width:100px;height:100px;"
                                                         src="${picUrl}/${storeCInfo.path}"/>

                                                </c:if>
                                            </c:forEach>
                                        </div>
                                        <c:forEach items="${workTaskInfo.storeInfos}" var="storeCInfo">
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

                        <c:if test="${workTaskInfo.committime!=null}">
                            <div class="row">
                                <div class="col-md-6">
                                    <label class="control-label col-md-4 alignRight">提交/关闭时间：</label>
                                    <div class="col-md-8">
                                        <fmt:formatDate value='${workTaskInfo.committime}' pattern='yyyy-MM-dd HH:mm'/>
                                    </div>
                                </div>
                            </div>
                            <c:if test='${workTaskInfo.status!="3"}'>
                                <div class="row">
                                    <div class="col-md-6">
                                        <label class="control-label col-md-4 alignRight">
                                            完成效率：
                                        </label>
                                        <div class="col-sm-8">
                                            <c:if test='${workTaskInfo.efficiency=="0"}'>准时</c:if>
                                            <c:if test='${workTaskInfo.efficiency=="1"}'>提前</c:if>
                                            <c:if test='${workTaskInfo.efficiency=="2"}'>延期</c:if>
                                        </div>
                                    </div>
                                </div>
                            </c:if>
                        </c:if>
                        <c:if test='${workTaskInfo.status=="2" or workTaskInfo.status=="4"}'>
                            <div class="row">
                                <div class="col-md-12">
                                    <label class="control-label col-md-2 alignRight">提交备注：</label>
                                    <div class="col-md-8">
                                        <pre>${workTaskInfo.annex}</pre>
                                    </div>
                                </div>
                            </div>
                            <div class="row">
                                <div class="col-md-6">
                                    <label class="control-label col-md-4 alignRight"
                                           name="fileName">提交附件：</label>
                                    <div class="col-md-8 alignLeft">
                                        <c:if test="${workTaskInfo.storeSubmitInfos==null or workTaskInfo.storeSubmitInfos.size()==0}">
                                            无
                                        </c:if>
                                        <c:if test="${workTaskInfo.storeSubmitInfos!=null and workTaskInfo.storeSubmitInfos.size()>0}">
                                            <div id="divShowSubmitImg${workTaskInfo.id}">
                                                <c:forEach items="${workTaskInfo.storeSubmitInfos}"
                                                           var="storesubmitInfo">
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
                                                        $('#divShowSubmitImg' + '${workTaskInfo.id}').viewer({
                                                            url: 'src',
                                                        });
                                                    });
                                                </script>
                                            </div>
                                            <br/>
                                            <c:forEach items="${workTaskInfo.storeSubmitInfos}" var="storesubmitInfo">
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
                        <c:if test='${workTaskInfo.status=="4"}'>
                            <div class="row">
                                <label class="control-label col-md-2 alignRight">评分星级：</label>
                                <div class="col-md-8">
                                    <c:forEach var="i" begin="1" end="5">
                                        <c:if test='${i<=workTaskInfo.evalscore}'>
                                            <img src="${cdnPrefix}/worktask/star_yellow.png">
                                        </c:if>
                                        <c:if test='${i>workTaskInfo.evalscore}'>
                                            <img src="${cdnPrefix}/worktask/star_gray.png">
                                        </c:if>
                                    </c:forEach>
                                </div>
                            </div>
                            <div class="row">
                                <label class="control-label col-md-2 alignRight">评分时间：</label>
                                <div class="col-md-8">
                                    <fmt:formatDate value='${workTaskInfo.evaltime}' pattern='yyyy-MM-dd HH:mm:ss'/>
                                </div>
                            </div>
                            <div class="row">
                                <label class="control-label col-md-2 alignRight">评分内容：</label>
                                <div class="col-md-8">
                                    <pre>${workTaskInfo.evaluate}</pre>
                                </div>
                            </div>
                        </c:if>

                        <div class="row">
                            <label class="control-label col-md-2 alignRight"></label>
                            <div class="col-md-8">
                                <h5>
                                    &nbsp; &nbsp; 子任务信息（
                                    <c:if test='${workTaskInfo.tasknum>0}'>
                                        <a id="btaskchild_${workTaskInfo.id}"
                                           class="btaskchildClass" href="javascript:"
                                           onclick="">[查看]</a>
                                    </c:if>
                                    <c:if test='${workTaskInfo.tasknum<1}'>
                                        无
                                    </c:if>
                                    &nbsp;子任务数：${workTaskInfo.tasknum}）
                                </h5>
                            </div>
                        </div>
                        <hr>
                        <div id="ChildTaskDiv_${workTaskInfo.id}" style="display:none; ">
                            <c:forEach items="${workTaskInfo.workChildTaskInfoList}" var="childInfo">
                                <div class="row">
                                    <div class="col-md-6">
                                        <label class="control-label col-md-4 alignRight"
                                        >标题：</label>
                                        <div class="col-sm-8">
                                                ${childInfo.title}
                                        </div>
                                    </div>
                                    <div class="col-md-6">
                                    </div>
                                </div>

                                <div class="row">
                                    <div class="col-md-12">
                                        <label class="control-label col-md-2 alignRight">内容描述：&nbsp;</label>
                                        <div class="col-md-8">
                                            <pre>${childInfo.content}</pre>

                                        </div>
                                    </div>
                                </div>

                                <div class="row">
                                    <div class="col-md-6">
                                        <label class="control-label col-md-4 alignRight"
                                        >计划开始时间：</label>
                                        <div class="col-sm-8">
                                            <fmt:formatDate value='${childInfo.starttime}' pattern='yyyy-MM-dd HH:mm'/>
                                        </div>
                                    </div>
                                    <div class="col-md-6">
                                        <label class="control-label col-md-4 alignRight"
                                        >计划完成时间：</label>
                                        <div class="col-sm-8">
                                            <fmt:formatDate value='${childInfo.plantime}' pattern='yyyy-MM-dd HH:mm'/>
                                        </div>
                                    </div>
                                </div>

                                <div class="row">
                                    <div class="col-md-6">
                                        <label class="control-label col-md-4 alignRight">负责人：</label>
                                        <div class="col-sm-8">
                                            <div class="input-group userPicker">
                                                <tags:isDelUser userId="${childInfo.leader}"/>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="col-md-6">
                                        <label class="control-label col-md-4 alignRight"
                                               for="workTaskInfo_workload">工作量(人/时)：</label>
                                        <div class="col-md-8">
                                                ${childInfo.workload}
                                        </div>
                                    </div>
                                </div>

                                <div class="row">
                                    <div class="col-md-6">
                                        <label class="control-label col-md-4 alignRight">抄送人：</label>
                                        <div class="col-sm-8">
                                                ${childInfo.ccshow}
                                        </div>
                                    </div>
                                </div>

                                <div class="row">
                                    <div class="col-md-6">
                                        <label class="control-label col-md-4 alignRight">任务状态：</label>
                                        <div class="col-md-8">
                                            <c:if test='${childInfo.status=="0"}'>已发布</c:if>
                                            <c:if test='${childInfo.status=="1"}'>进行中</c:if>
                                            <c:if test='${childInfo.status=="2"}'>已完成</c:if>
                                            <c:if test='${childInfo.status=="3"}'>已关闭</c:if>
                                            <c:if test='${childInfo.status=="4"}'>已评价</c:if>
                                        </div>
                                    </div>
                                    <div class="col-md-6">
                                        <div class="row">
                                            <label class="control-label col-md-4 alignRight" name="fileName">附件：</label>
                                            <div class="col-md-8 alignLeft">
                                                <c:if test="${fn:length(childInfo[storeInfos.size()])==0}">
                                                    无
                                                </c:if>
                                                <c:if test="${fn:length(childInfo[storeInfos.size()])!=0}">
                                                    <div id="divShowImg${childInfo.id}">
                                                        <c:forEach items="${childInfo.storeInfos}" var="storeInfo">
                                                            <c:if test='${fn:contains(storeInfo.path,".jpg")
                                              or fn:contains(storeInfo.path,".gif")
                                              or fn:contains(storeInfo.path,".png")
                                              or fn:contains(storeInfo.path,".bmp")}'>

                                                                <img style="width:100px;height:100px;"
                                                                     src="${picUrl}/${storeInfo.path}"/>

                                                            </c:if>
                                                        </c:forEach>
                                                    </div>
                                                    <c:forEach items="${workTaskInfo.storeInfos}" var="storeInfo">
                                                        <c:if test='${!fn:contains(storeInfo.path,".jpg")
                                              and !fn:contains(storeInfo.path,".gif")
                                              and !fn:contains(storeInfo.path,".png")
                                              and !fn:contains(storeInfo.path,".bmp")}'>
                                                            <a target="_blank"
                                                               href="${tenantPrefix}/downloadAmachment/download.do?id=${childInfo.storeInfos.id}">${childInfo.storeInfos.name}
                                                            </a>
                                                        </c:if>
                                                        <br/>
                                                    </c:forEach>
                                                </c:if>
                                                <script>
                                                    $(function () {
                                                        $('#divShowImg' + '${childInfo.id}').viewer({
                                                            url: 'src',
                                                        });
                                                    });
                                                </script>
                                            </div>
                                        </div>
                                    </div>
                                </div>

                                <div class="row">
                                    <div class="col-md-6">
                                        <label class="control-label col-md-4 alignRight">发布人：</label>
                                        <div class="col-md-8">
                                            <tags:isDelUser userId="${childInfo.publisher}"/>
                                        </div>
                                    </div>
                                    <div class="col-md-6">
                                        <label class="control-label col-md-4 alignRight">发布时间：</label>
                                        <div class="col-md-8">
                                            <fmt:formatDate value='${childInfo.publishtime}'
                                                            pattern='yyyy-MM-dd HH:mm:ss'/>
                                        </div>
                                    </div>
                                </div>
                                <div class="row">
                                    <div class="col-md-12">
                                        <label class="control-label col-md-2 alignRight">备注：</label>
                                        <div class="col-sm-8">
                                            <pre>${childInfo.remarks}</pre>
                                        </div>
                                    </div>
                                </div>
                                <c:if test="${childInfo.committime!=null}">
                                    <div class="row">
                                        <div class="col-md-6">
                                            <label class="control-label col-md-4 alignRight">提交/关闭时间：</label>
                                            <div class="col-sm-8">
                                                <fmt:formatDate value='${childInfo.committime}'
                                                                pattern='yyyy-MM-dd HH:mm'/>
                                            </div>
                                        </div>
                                    </div>
                                    <c:if test='${childInfo.status!="3"}'>
                                        <div class="row">
                                            <div class="col-md-6">
                                                <label class="control-label col-md-4 alignRight">
                                                    完成效率：
                                                </label>
                                                <div class="col-sm-8">
                                                    <c:if test='${workTaskInfo.efficiency=="0"}'>准时</c:if>
                                                    <c:if test='${workTaskInfo.efficiency=="1"}'>提前</c:if>
                                                    <c:if test='${workTaskInfo.efficiency=="2"}'>延期</c:if>
                                                </div>
                                            </div>
                                        </div>
                                    </c:if>
                                </c:if>


                                <c:if test='${childInfo.status=="2" or childInfo.status=="4"}'>
                                    <div class="row">
                                        <div class="col-md-12">
                                            <label class="control-label col-md-2 alignRight">提交备注：</label>
                                            <div class="col-sm-8">
                                                <pre>${childInfo.annex}</pre>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="row">
                                        <div class="col-md-6">
                                            <label class="control-label col-md-4 alignRight"
                                                   name="fileName">提交附件：</label>
                                            <div class="col-sm-8 alignLeft">
                                                <c:if test="${childInfo.storeSubmitInfos==null or fn:length(childInfo[storeSubmitInfos.size()])==0}">
                                                    无
                                                </c:if>
                                                <c:if test="${childInfo.storeSubmitInfos!=null and fn:length(childInfo[storeSubmitInfos.size()])==0}">
                                                    <div id="divShowSubmitImg${childInfo.id}">
                                                        <c:forEach items="${childInfo.storeSubmitInfos}"
                                                                   var="storesubmitInfo">
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
                                                                $('#divShowSubmitImg' + '${childInfo.id}').viewer({
                                                                    url: 'src',
                                                                });
                                                            });
                                                        </script>
                                                    </div>
                                                    <br/>
                                                    <c:forEach items="${childInfo.storeSubmitInfos}"
                                                               var="storesubmitInfo">
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

                                <c:if test='${childInfo.status=="4"}'>
                                    <div class="row">
                                        <label class="control-label col-md-2 alignRight">评分星级：</label>
                                        <div class="col-md-8">
                                            <c:forEach var="i" begin="1" end="5">
                                                <c:if test='${i<=childInfo.evalscore}'>
                                                    <img src="${cdnPrefix}/worktask/star_yellow.png">
                                                </c:if>
                                                <c:if test='${i>childInfo.evalscore}'>
                                                    <img src="${cdnPrefix}/worktask/star_gray.png">
                                                </c:if>
                                            </c:forEach>
                                        </div>
                                    </div>
                                    <div class="row">
                                        <label class="control-label col-md-2 alignRight">评分时间：</label>
                                        <div class="col-md-8">
                                            <fmt:formatDate value='${childInfo.evaltime}'
                                                            pattern='yyyy-MM-dd HH:mm:ss'/>
                                        </div>
                                    </div>
                                    <div class="row">
                                        <label class="control-label col-md-2 alignRight">评分内容：</label>
                                        <div class="col-md-8">
                                            <pre>${childInfo.evaluate}</pre>
                                        </div>
                                    </div>
                                </c:if>
                                <hr>
                            </c:forEach>
                        </div>

                    </c:forEach>
                </div>
                <hr>

            </div>
            <br><br>
            <div class="row">
                <div class="col-md-offset-5 col-md-5">
                    <button type="button" class="btn btn-default"
                            onclick="a();">返回
                    </button>
                </div>
            </div>
            <br><br>
        </div>

    </section>


    <!-- end of main -->
</div>

</body>

</html>
