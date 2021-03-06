<%@page contentType="text/html;charset=UTF-8" %>
<%@include file="/taglibs.jsp" %>
<%pageContext.setAttribute("currentHeader", "cms");%>
<%pageContext.setAttribute("currentMenuName", "内容管理");%>
<%pageContext.setAttribute("currentMenu", "cms");%>
<!doctype html>
<html>

<head>
    <%@include file="/common/meta.jsp" %>
    <title>麦联</title>
    <%@include file="/common/s3.jsp" %>

    <link href="${tenantPrefix}/common/ueditor/themes/default/css/ueditor.css" type="text/css" rel="stylesheet">
    <script type="text/javascript" src="${tenantPrefix}/common/ueditor/ueditor.config.js"></script>
    <script type="text/javascript" src="${tenantPrefix}/common/ueditor/ueditor.all.js"></script>
    <script type="text/javascript" src="${tenantPrefix}/common/ueditor/lang/zh-cn/zh-cn.js"></script>
    <link type="text/css" rel="stylesheet" href="${cdnPrefix}/orgpicker/orgpicker.css">
    <script type="text/javascript" src="${cdnPrefix}/orgpicker/orgpicker.js"></script>
    <script type="text/javascript" src="${cdnPrefix}/bootbox/bootbox.min1.js"></script>

    <script type="text/javascript">

        var ue = UE.getEditor('cmsArticle_content', {
            "imageActionUrl": "${tenantPrefix}/cms/imageUpload.do",
            'toolbars': [['fullscreen', 'source', '|', 'undo', 'redo', '|',
                'bold', 'italic', '|',
                'customstyle', 'paragraph', 'fontfamily', 'fontsize', '|',
                'justifyleft', 'justifycenter', 'justifyright', 'justifyjustify', '|',
                'horizontal', 'date', 'time', '|', 'inserttable', 'simpleupload'
                /* 'imagenone', 'imageleft', 'imageright', 'imagecenter', '|', 'scrawl', 'pagebreak', 'template', 'background', '|',*/
                /*'deletetable', 'insertparagraphbeforetable', 'insertrow', 'deleterow', 'splittocells', 'splittorows', 'splittocols', '|',*/
                /* 'directionalityltr', 'directionalityrtl', 'indent', '|','preview', */]]

        });

            $(function () {
                createOrgPicker({
                    modalId: 'orgPicker',
                    showExpression: true,
                    chkStyle: 'checkbox',
                    searchUrl: '${tenantPrefix}/rs/user/search',
                    treeUrl: '${tenantPrefix}/rs/party/treeAuth?partyStructTypeId=1',
                    childUrl: '${tenantPrefix}/rs/party/searchUser'
                });
                var sectionJson = [{"begin": "#pickerStartTime", "end": "#pickerEndTime"}];

                fnCmsSectionPickerTime(sectionJson);

            })


    </script>
    <script>
        var fnCmsSectionPickerTime = function (eleTimes) {
            $(eleTimes).each(function (i, ele) {
                $(ele.begin + " span").remove();
                $(ele.end + " span").remove();

                $(ele.begin + " input").css("width", "835px");
                $(ele.end + " input").css("width", "835px");

                $(ele.begin + " input").addClass("Wdate");
                $(ele.end + " input").addClass("Wdate");

                var begin = $(ele.begin + " input").attr("id");

                var end = $(ele.end + " input").attr("id");


                if (begin == undefined)
                    $(ele.begin + " input").attr("id", "ipt" + ele.begin.replace("#", ""));
                if (end == undefined)
                    $(ele.end + " input").attr("id", "ipt" + ele.end.replace("#", ""))

                begin = $(ele.begin + " input").attr("id");
                end = $(ele.end + " input").attr("id");


                $(ele.begin + " input").attr("onclick", "WdatePicker({maxDate:'#F{$dp.$D(\\'" + end + "\\',{d:-1})||\\'2020-10-01\\'}',dateFmt:'yyyy-MM-dd 00:00:00'})");
                $(ele.end + " input").attr("onclick", "WdatePicker({minDate:'#F{$dp.$D(\\'" + begin + "\\',{d:+1})}',minDate:'%y-%M-{%d+1}',maxDate:'2020-10-01',dateFmt:'yyyy-MM-dd 23:59:59'})");
                //开始时间
                $(ele.begin + " .glyphicon-calendar").click(function () {
                    $(ele.begin + " input").click();
                })
                $(ele.end + " .glyphicon-calendar").click(function () {
                    $(ele.end + " input").click();
                })

            })
        }
    </script>
    <script type="text/javascript">
        $(document).ready(function () {
            $('#cmsArticleForm').validate({
                rules: {
                    cmsArticle_content: {
                        required: true
                    }
                },
                ignore: '',
                errorPlacement: function (error, element) {//error为错误提示对象，element为出错的组件对象
                    if (element.parent().parent().hasClass("form-group"))
                        error.appendTo(element.parent().parent());
                    else
                        error.appendTo(element.parent().parent().parent());
                },
                errorClass: 'validate-error'

            });

            $(".selector").validate({
                showErrors: function (errorMap, errorList) {
                    this.defaultShowErrors();
                }
            });
        });

        function submitInfo(status) {
            var text = ue.getContent();
            $("#cmsArticle_content").val($.trim(text));
            if (!$("#cmsArticleForm").valid()) {
                return false;
            }
            var loading = bootbox.dialog({
                message: '<p style="width:90%;margin:0 auto;text-align:center;">提交中...</p>',
                size: 'small',
                closeButton: false
            });
            $("#status").val(status);
            $("#content").val(text);
            $("#cmsArticleForm").submit();
            return true;
        }
    </script>

    <style>
        .cke_contents {
            height: 450px;
        }
    </style>
</head>

<body>
<%@include file="/header/cms.jsp" %>

<div class="row-fluid">
    <%@include file="/menu/sidebar.jsp" %>

    <!-- start of main -->
    <section id="m-main" class="col-md-10" style="padding-top:65px;">

        <div class="panel panel-default">
            <div class="panel-heading">
                <i class="glyphicon glyphicon-list"></i>
                编辑
            </div>

            <div class="panel-body">
                <form id="cmsArticleForm" method="post" action="cms-article-save.do" class="form-horizontal"
                      enctype="multipart/form-data">
                    <div class="form-group">
                        <label class="control-label col-md-1" for="cms-article_cmsArticlename"><span style="color:red;"> * </span>标题</label>
                        <div class="col-md-9">
                            <input id="cms-article_cmsArticlename" type="text" name="title" value="${model.title}"
                                   size="40" class="form-control required" minlength="2" maxlength="50">
                        </div>
                    </div>
                    <input id="cmsCatalogId" type="hidden" name="cmsCatalogId" value="1">
                    <input id="status" type="hidden" name="status">
                    <input id="content" type="hidden" name="content">
                    <div class="form-group">
                        <label class="control-label col-md-1"><span style="color:red;"> * </span>公告范围</label>
                        <div class="col-md-9">
                            <div class="input-group orgPicker">
                                <input id="_task_name_key" type="hidden" name="partyEntityId"
                                       value="${model.partyEntityId}">
                                <input type="text" class="form-control required" id="departmentName"
                                       name="partyEntityName" placeholder="" value="${model.partyEntityId}"
                                       readonly="readonly">
                                <div class="input-group-addon"><i class="glyphicon glyphicon-user"></i></div>
                            </div>
                        </div>
                    </div>
                    <%--
                                        <div class="form-group">
                                            <label class="control-label col-md-1" for="cmsArticle_summary"><span
                                                    style="color:red;"> * </span>摘要</label>
                                            <div class="col-md-7">
                                                <textarea id="cmsArticle_summary" name="summary" maxlength="200"
                                                          class="form-control required">${model.summary}</textarea>
                                            </div>
                                        </div>--%>
                    <div class="form-group">
                        <label class="control-label col-md-1" for="cms-article_cmsArticlename"><span style="color:red;"> * </span>内容</label>
                        <div class="col-md-9">
                            <textarea id="cmsArticle_content" name="cmsArticle_content"
                                      style="height:500px;">${model.content}</textarea>
                        </div>
                    </div>
                    <!--
  <div class="form-group">
    <label class="control-label col-md-1" for="cms-article_cmsArticlename">图标</label>
	<div class="col-sm-5">
	  <input id="cms-article_cmsArticlename" type="text" name="logo" value="${model.logo}" size="40" class="text" minlength="2" maxlength="50">
    </div>
  </div>
  <div class="form-group">
    <label class="control-label col-md-1" for="cms-article_cmsArticlename">关键字</label>
	<div class="col-sm-5">
	  <input id="cms-article_cmsArticlename" type="text" name="keyword" value="${model.keyword}" size="40" class="text" minlength="2" maxlength="50">
    </div>
  </div>
  <div class="form-group">
    <label class="control-label col-md-1" for="cms-article_cmsArticlename">标签</label>
	<div class="col-sm-5">
	  <input id="cms-article_cmsArticlename" type="text" name="tags" value="${model.tags}" size="40" class="text" minlength="2" maxlength="50">
    </div>
  </div>
  <div class="form-group">
    <label class="control-label col-md-1" for="cms-article_cmsArticlename">来源</label>
	<div class="col-sm-5">
	  <input id="cms-article_cmsArticlename" type="text" name="source" value="${model.source}" size="40" class="text" minlength="2" maxlength="50">
    </div>
  </div>
  <div class="form-group">
    <label class="control-label col-md-1" for="cms-article_cmsArticlename">允许评论</label>
	<div class="col-sm-5">
	  <input id="cms-article_cmsArticlename" type="checkbox" name="allowComment" value="1" class="text" ${model.allowComment == 1 ? 'checked' : ''}>
    </div>
  </div>
  <div class="form-group">
    <label class="control-label col-md-1" for="cms-article_cmsArticlename">状态</label>
	<div class="col-sm-5">
	  <input id="cms-article_cmsArticlename" type="checkbox" name="status" value="1" class="text" ${model.status == 1 ? 'checked' : ''}>
    </div>
  </div>
-->
                    <!--
  <div class="form-group">
    <label class="control-label col-md-1" for="cms-article_cmsArticlename">创建时间</label>
	<div class="col-sm-5">
	  <input id="cms-article_cmsArticlename" type="text" name="createTime" value="${model.createTime}" class="text">
    </div>
  </div>
  <div class="form-group">
    <label class="control-label col-md-1" for="cms-article_cmsArticlename">发布时间</label>
	<div class="col-sm-5">
	  <input id="cms-article_cmsArticlename" type="text" name="publishTime" value="${model.publishTime}" class="text">
    </div>
  </div>
  <div class="form-group">
    <label class="control-label col-md-1" for="cms-article_cmsArticlename">关闭时间</label>
	<div class="col-sm-5">
	  <input id="cms-article_cmsArticlename" type="text" name="closeTime" value="${model.closeTime}" class="text">
    </div>
  </div>
-->
                    <!--
  <div class="form-group">
    <label class="control-label col-md-1" for="cms-article_cmsArticlename">类型</label>
	<div class="col-sm-5">
	  <input id="cms-article_cmsArticlename" type="text" name="type" value="${model.type}" class="text">
    </div>
  </div>
  <div class="form-group">
    <label class="control-label col-md-1" for="cms-article_cmsArticlename">置顶</label>
	<div class="col-sm-5">
	  <input id="cms-article_cmsArticlename" type="text" name="top" value="${model.top}" class="text">
    </div>
  </div>
  <div class="form-group">
    <label class="control-label col-md-1" for="cms-article_cmsArticlename">权重</label>
	<div class="col-sm-5">
	  <input id="cms-article_cmsArticlename" type="text" name="weight" value="${model.weight}" class="text">
    </div>
  </div>
-->
                    <div class="form-group">
                        <label class="control-label col-md-1" for="pickerStartTime"><span style="color:red;"> * </span>
                            生效时间</label>
                        <div class="col-sm-9">
                            <div id="pickerStartTime" class="input-group date">
                                <input type="text" name="startTime"
                                       value="<fmt:formatDate value='${model.startTime}' type="both" />"
                                       readonly style="background-color:white;cursor:default;"
                                       class="form-control required">
                                <span class="input-group-addon"><i class="glyphicon glyphicon-calendar"></i></span>
                            </div>
                        </div>
                    </div>

                    <div class="form-group">
                        <label class="control-label col-md-1" for="pickerEndTime"><span style="color:red;"> * </span>
                            失效时间</label>
                        <div class="col-sm-9">
                            <div id="pickerEndTime" class="input-group date">
                                <input id="endTime" type="text" name="endTime"
                                       value="<fmt:formatDate value='${model.endTime}' type="both" />"
                                       readonly
                                       style="background-color:white;cursor:default;" class="form-control required">
                                <span class="input-group-addon"><i class="glyphicon glyphicon-calendar"></i></span>
                            </div>
                        </div>
                    </div>

                    <div class="form-group">
                        <label class="control-label col-md-1" name="fileName">附件</label>
                        <div class="col-md-9">
                            <%@include file="/common/_uploadFile.jsp" %>
                            <span style="color:gray;"> 请添加共小于200M的附件 </span>
                        </div>
                    </div>

                    <div class="form-group">
                        <div class="col-md-offset-2 col-md-10">
                            <button type="button" class="btn btn-default a-submit" onclick="submitInfo(1)">发布
                            </button>
                            &nbsp;
                            <button type="button" class="btn btn-default a-submit" onclick="submitInfo(0)">保存草稿
                            </button>
                            &nbsp;

                        </div>
                    </div>
                </form>

            </div>
            </article>
        </div>

    </section>
    <!-- end of main -->
</div>

</body>

</html>

