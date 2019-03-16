<%--
  
  User: wanghan
  Date: 2017\10\19 0019
  Time: 11:07
 
--%>
<%@page contentType="text/html;charset=UTF-8" %>
<%@include file="/taglibs.jsp" %>
<%
    pageContext.setAttribute("currentHeader", "cms");
%>
<%
    pageContext.setAttribute("currentMenu", "cms");
%>
<!doctype html>
<html lang="en">

<head>
    <%@include file="/common/meta.jsp" %>
    <title>麦联</title>
    <%@include file="/common/s3.jsp" %>
    <script type="text/javascript">
        $(window).load(function () {
            //缩放图片到合适大小
            function ResizeImages() {
                var myimg, oldwidth, oldheight;
                var maxwidth = 700;
                var maxheight = 1000;
                var imgs = document.getElementById("picAndCon").getElementsByTagName("img");
                for (i = 0; i < imgs.length; i++) {
                    myimg = imgs[i];
                    if (myimg.width > myimg.height) {
                        if (myimg.width > maxwidth) {
                            oldwidth = myimg.width;
                            myimg.height = myimg.height * (maxwidth / oldwidth);
                            myimg.width = maxwidth;
                        }
                    } else {
                        if (myimg.height > maxheight) {
                            oldheight = myimg.height;
                            myimg.width = myimg.width * (maxheight / oldheight);
                            myimg.height = maxheight;
                        }
                    }
                }
            }

            //缩放图片到合适大小
            ResizeImages();
        });

    </script>
    <style type="text/css">
        .alignRight {
            text-align: right;
            padding-right: 5px;
        }

        .col-md-8, .col-sm-8 {
            padding-left: 5px;
        }
    </style>
</head>

<body>
<%@include file="/header/pim3.jsp" %>

<div class="row-fluid">
    <%@include file="/menu/sidebar.jsp" %>
    <!-- start of main -->
    <section id="m-main" class="col-md-10" style="margin-top:65px;">

        <div class="panel panel-default">
            <div class="panel-heading">
                公告详情
            </div>

            <div class="panel-body">
                <input id="datastatus" type="hidden" name="datastatus">
                <h4>详情</h4>
                <hr>
                <div class="row">
                    <label class="col-md-2 alignRight">标题：</label>
                    <div class="col-md-8">
                        ${model.title}
                    </div>
                </div>
                <%--
                                <div class="row">
                                    <label class="col-md-2 alignRight">公告范围：</label>
                                    <div class="col-md-8">
                                        <div class="input-group orgPicker">
                                            <input id="_task_name_key" type="hidden" name="partyEntityId"
                                                   value="${model.partyEntityId}">
                                            ${partyEntityNames}
                                        </div>
                                    </div>

                                </div>--%>
                <%--
                                <div class="row">
                                    <label class="col-md-2 alignRight">摘要：</label>
                                    <div class="col-md-8 ">
                                        ${model.summary}
                                    </div>
                                </div>--%>

                <div class="row">
                    <label class="control-label col-md-2 alignRight" for="workTaskInfo_content">内容：</label>
                    <div class="col-md-8" id="picAndCon">
                        ${model.content}
                    </div>
                </div>
                <div class="row">
                    <label class="col-md-2 alignRight">发布人：</label>
                    <div class="col-md-8 userPicker">
                        <tags:user userId="${model.userId}"/>
                    </div>
                </div>

                <div class="row">
                    <label class="col-md-2 alignRight">有效开始日期：</label>
                    <div class="col-md-8 ">
                        <fmt:formatDate value='${model.startTime}' pattern='yyyy-MM-dd HH:mm:ss '/>
                    </div>
                </div>
                <div class="row">
                    <label class="col-md-2 alignRight">有效结束日期：</label>
                    <div class="col-md-8 ">
                        <fmt:formatDate value='${model.endTime}' pattern='yyyy-MM-dd HH:mm:ss '/>
                    </div>
                </div>

                <div class="row">
                    <label class="control-label col-md-2 alignRight" name="fileName">附件：</label>
                    <div class="col-md-8">
                        <%@include file="/common/show_file.jsp" %>
                    </div>
                </div>
                <br>
                <br>
                <div class="row">
                    <div class="col-md-offset-2 col-md-10">
                        <button type="button" class="btn btn-default"
                                onclick="history.back();">返回
                        </button>
                    </div>
                </div>
                <br>
                <br>
    </section>
    <!-- end of main -->
</div>

</body>

</html>
