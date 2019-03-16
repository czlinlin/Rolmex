<%@page contentType="text/html;charset=UTF-8" %>
<%@include file="/taglibs.jsp" %>
<%pageContext.setAttribute("currentHeader", "pim");%>
<%pageContext.setAttribute("currentMenu", "workReport");%>
<!doctype html>
<html lang="en">

<head>
    <%@include file="/common/meta.jsp" %>
    <title>麦联-提交任务</title>
    <%@include file="/common/s3.jsp" %>
    <script>
        function checkWord(obj) {
            var value = $(obj).val();
            var length = value.length;
            //长度限制为200
            if (length > 200) {
                value = value.substring(0, 200);
                $(obj).attr("value", value);
            }
        }
    </script>
</head>

<body>
<%@include file="/header/navbar.jsp"%>

<div class="row-fluid">
    <%@include file="/menu/sidebar.jsp"%>
    <!-- start of main -->
    <section id="m-main" class="col-md-10" style="margin-top:65px;">

        <div class="panel panel-default">
            <div class="panel-heading">
                任务提交
            </div>

            <div class="panel-body">

                <form id="workTaskInfoForm" method="post" action="work-task-info-submit-save.do" class="form-horizontal" enctype=multipart/form-data>
                    <input id="id"  name="id" type="hidden" value="${id}"></input>
                    <div class="form-group">
                        <label class="control-label col-md-2">任务标题</label>
                        <div class="col-md-8">
                            <div>${model.title}&nbsp;&nbsp;</div>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="control-label col-md-2" for="workReportInfo_remarks">备注</label>
                        <div class="col-md-8">
                            <textarea id="workReportInfo_remarks" name="remarks" class="form-control " maxlength="200" onkeyup="checkWord(this)"></textarea>
                            <span style="color:gray;"> 备注限输入200字以内 </span>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="control-label col-md-2">附件</label>
                        <div class="col-md-8">
                            <%@include file="/common/_uploadFile.jsp"%>
                            <span style="color:gray;"> 请添加共小于200M的附件 </span>
                        </div>
                    </div>

						<div class="form-group">
							<div class="col-md-offset-2 col-md-10">
								<button type="submit" class="btn btn-default a-submit">
									<spring:message code='core.input.save' text='提交' />
								</button>
								&nbsp;
                                <button type="button" class="btn btn-default"
                                        onclick="window.location.href='work-task-charge-list.do'">返回
                                </button>
							</div>
						</div>
					</form>
				</div>
			</div>

		</section>
		<!-- end of main -->
	</div>
</body>
</html>
