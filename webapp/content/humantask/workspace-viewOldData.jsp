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
        

        var table;

 
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
                
                <div class="pull-right ctrl">
                    <a class="btn btn-default btn-xs"><i id="charege-infoSearchIcon"
                                                         class="glyphicon glyphicon-chevron-up"></i></a>
                </div>
            </div>
            <div class="panel-body">

                <form id="charege-infoForm" name="charege-infoForm" method="post"
                     
                      class="form-inline">
                   <span style='color:red;'>提示：</span>
                   <br/>
                   <br/>
                   	1.点击下面的链接，会打开新页面进入协同系统<br/>
                   	<br/>
                   	2.使用前请先设置正确的协同用户名：点击右上角的名字-个人信息-协同用户名（将协同系统的登录名输入进去提交即可）<br/>
	              <br/>
					<c:if test="${oldSysUserName==''}">
						【<font color='red'>请先去“个人信息”设置协同用户名</font>】
					</c:if>
					<c:if test="${oldSysUserName!=''}">
						<div style="font-size:16px;font-weight:bold;"><a target="_blank" href="${dictInfos}?uname=${oldDataUserName}&sign=${signStr}">【点击打开协同系统】</a></div>
					</c:if>
                </form>
            </div>
        </div>

    </section>
    <!-- end of main -->
</div>

</body>

</html>
