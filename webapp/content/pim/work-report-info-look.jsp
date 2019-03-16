<%--
  
  User: wanghan
  Date: 2017\8\18 0018
  Time: 9:20
 
--%>
<%@page import="com.fasterxml.jackson.annotation.JsonInclude.Include" %>
<%@page contentType="text/html;charset=UTF-8" %>
<%@include file="/taglibs.jsp" %>
<%pageContext.setAttribute("currentHeader", "pim");%>
<%pageContext.setAttribute("currentMenu", "workReport");%>
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

        pre {
            white-space: pre-wrap;
            word-wrap: break-word;
        }
    </style>
    <script type="text/javascript">
        $(function () {
            if (${model.type=="4"}) {
                document.getElementById("div1").style.display = "block";
                document.getElementById("div2").style.display = "none";
                document.getElementById("div3").style.display = "none";
                document.getElementById("div4").style.display = "none";
                document.getElementById("div5").style.display = "none";
            } else {
                document.getElementById("div1").style.display = "none";
                document.getElementById("div2").style.display = "block";
                document.getElementById("div3").style.display = "block";
                document.getElementById("div4").style.display = "block";
                document.getElementById("div5").style.display = "block";
            }
        });
    </script>
    <script>
        function a() {
            var urlP = document.referrer;
            //alert(urlP);
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
                查看汇报
            </div>

            <div class="panel-body">
                <hr>
                <div class="row">
                    <div class="col-md-8">
                        <label class="col-md-3 alignRight">标题：</label>
                        <div class="col-md-9">
                        	${model.title}
                        </div>
                    </div>
                </div>

                <div class="row">
                    <div class="col-md-8">
                        <label class="control-label col-md-3 alignRight">汇报类型：</label>
                        <div class="col-md-9">
	                        <c:if test='${model.type=="1"}'>周报</c:if>
	                        <c:if test='${model.type=="2"}'>月报</c:if>
	                        <c:if test='${model.type=="3"}'>年报</c:if>
	                        <c:if test='${model.type=="4"}'>专项</c:if>
                        </div>
                    </div>
                </div>
                <div class="row" id="div1">
                    <div class="col-md-8">
                        <label class="control-label col-md-3 alignRight">内容：</label>
                        <div class="col-md-9" style="width: 600px;overflow: hidden ;word-wrap:break-word">
                            ${model.problems}
                        </div>
                    </div>
                </div>
                <div class="row" id="div3">
                    <div class="col-md-8">
                        <label class="control-label col-md-3 alignRight">进行中工作：</label>
                        <div class="col-md-9" style="overflow: hidden ;word-wrap:break-word">
                            ${model.dealing}</div>
                    </div>
                </div>
                <div class="row" id="div2">
                    <div class="col-md-8">
                        <label class="control-label col-md-3 alignRight"> 已完成工作：</label>
                        <div class="col-md-9" style="overflow: hidden ;word-wrap:break-word">
                            ${model.completed}
                        </div>
                    </div>
                </div>
                <div class="row" id="div4">
                    <div class="col-md-8">
                        <label class="control-label col-md-3 alignRight">需协调工作：</label>
                        <div class="col-md-9" style="overflow: hidden ;word-wrap:break-word">
                            ${model.coordinate}</div>

                    </div>
                </div>
                <c:if test="${model.datastatus=='1'}">
                    <div class="row" id="div4">
                        <div class="col-md-8">
                            <label class="control-label col-md-3 alignRight">状态：</label>
                            <div class="col-md-9">
	                            <c:if test='${model.status=="0"}'>未读</c:if>
	                            <c:if test='${model.status=="1"}'>已读</c:if>
	                            <c:if test='${model.status=="2"}'>已反馈</c:if>
                            </div>
                        </div>
                    </div>
                </c:if>
                <c:if test='${model.status=="2" }'>
                
                <c:if test="${workReportForwards.size()!=0}">
                     <c:forEach items="${workReportForwards}" var="report">
                 		<c:if test='${report.isfeedbackforward=="1"}'>
                 		
	                    <div class="row" id="div4">
	                        <div class="col-md-8">
	                            <label class="control-label col-md-3 alignRight">反馈内容：</label>
	                            <div class="col-md-9">
	                                  <pre> ${model.feedback}</pre>
	                            </div>
	                        </div>
	                    </div>
	                    <div class="row" id="div4">
	                        <div class="col-md-8">
	                            <label class="control-label col-md-3 alignRight">反馈时间：</label>
	                            <fmt:formatDate value='${model.feedbacktime}' pattern='yyyy-MM-dd HH:mm:ss'/>
	                        </div>
	                    </div>
	                    
                  </c:if>
                 </c:forEach>
                </c:if>
                
                
                
                <c:if test="${workReportForwards.size()==0}">
                     <div class="row" id="div4">
	                        <div class="col-md-8">
	                            <label class="control-label col-md-3 alignRight">反馈内容：</label>
	                            <div class="col-md-9">
	                                  <pre> ${model.feedback}</pre>
	                            </div>
	                        </div>
	                    </div>
	                    <div class="row" id="div4">
	                        <div class="col-md-8">
	                            <label class="control-label col-md-3 alignRight">反馈时间：</label>
	                            <fmt:formatDate value='${model.feedbacktime}' pattern='yyyy-MM-dd HH:mm:ss'/>
	                        </div>
	                    </div>
	            </c:if>
                
             </c:if>


                <div class="row" id="div5">
                    <div class="col-md-8">
                        <label class="control-label col-md-3 alignRight">备注：</label>
                        <div class="col-md-9">
                            <c:if test="${model.remarks!=''}">
                                <pre>${model.remarks}</pre>
                            </c:if>
                            <c:if test="${model.remarks==''}">
                                无
                            </c:if>
                        </div>
                    </div>
                </div>

                <div class="row">
                    <div class="col-md-8">
                        <label class="control-label col-md-3 alignRight" name="fileName">附件：</label>
                        <div class="col-md-9 alignLeft">
                            <%@include file="/common/show_file.jsp" %>
                        </div>
                    </div>
                </div>

                <div class="row">
                    <div class="col-md-8">
                        <label class="control-label col-md-3 alignRight"> 接收人：</label>
                        <div class="col-md-9">
                            <tags:isDelUser userId="${model.sendee}"></tags:isDelUser>
                        </div>
                    </div>
                </div>

                <div class="row">
                    <div class="col-md-8">
                        <label class="control-label col-md-3 alignRight"> 抄送：</label>
                        <div class="col-md-9">
                            ${ccnames}
                        </div>
                    </div>
                </div>
                <c:if test="${model.datastatus=='1'}">
                    <div class="row">
                        <div class="col-md-8">
                            <label class="control-label col-md-3 alignRight"> 转发给我：</label>
                            <div class="col-md-9">
                                <table class="col-md-12" border="1px">
                                    <thead>
                                    <th class="control-label col-md-2 " style="text-align: center">转发人</th>
                                    <th class="control-label col-md-2 " style="text-align: center">转发时间</th>
                                    <th class="control-label col-md-3 " style="text-align: center">转发备注</th>
                                    </thead>
                                    <tbody>
                                    <c:if test="${workReportForwards.size()!=0}">
                                        <c:forEach items="${workReportForwards}" var="report">
                                            <tr>
                                                <td style="text-align: center">
                                                	<tags:isDelUser userId="${report.forwarder}"></tags:isDelUser>
                                               	</td>
                                                <td>
                                                	<fmt:formatDate value="${report.forwardtime}" type="both"
                                                                    pattern='yyyy-MM-dd HH:mm:ss'/>
                                                </td>
                                                <td>${report.remarks}</td>
                                            </tr>
                                        </c:forEach>
                                    </c:if>
                                    <c:if test="${workReportForwards.size()==0}">
                                        <tr>
                                            <td style="text-align: center">无</td>
                                            <td style="text-align: center">无</td>
                                            <td style="text-align: center">无</td>
                                        </tr>
                                    </c:if>

                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>

                    <br>
                    <br>
                    <div class="row">
                        <div class="col-md-8">
                            <label class="control-label col-md-3 alignRight"> 转发至：</label>
                            <div class="col-md-9">
                                <table class="col-md-12" border="1px">
                                    <thead>
                                    <th class="control-label col-md-2 " style="text-align: center">接收人</th>
                                    <th class="control-label col-md-2 " style="text-align: center">转发时间</th>
                                    <th class="control-label col-md-3 " style="text-align: center">转发备注</th>
                                    </thead>
                                    <tbody>
                                    <c:if test="${workReportSendees.size()!=0}">
                                        <c:forEach items="${workReportSendees}" var="workReportSendees">
                                            <tr>
                                                <td style="text-align: center">
                                                	<tags:isDelUser userId="${workReportSendees.sendee}"></tags:isDelUser>
                                               	</td>
                                                <td>
                                                	<fmt:formatDate value="${workReportSendees.forwardtime}" type="both"
                                                                pattern='yyyy-MM-dd HH:mm:ss'/>
                                                </td>
                                                <td>${workReportSendees.remarks}</td>
                                            </tr>
                                        </c:forEach>
                                    </c:if>
                                    <c:if test="${workReportSendees.size()==0}">
                                        <tr>
                                            <td style="text-align: center">无</td>
                                            <td style="text-align: center">无</td>
                                            <td style="text-align: center">无</td>
                                        </tr>
                                    </c:if>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>

                </c:if>
                <br>
                <br>
                <div class="row">
                    <div class="col-md-offset-2 col-md-10">
                        <button type="button" class="btn btn-default"
                                onclick="a();">返回
                        </button>
                    </div>
                </div>
            </div>
        </div>

    </section>
    <!-- end of main -->
</div>


</body>
</html>
