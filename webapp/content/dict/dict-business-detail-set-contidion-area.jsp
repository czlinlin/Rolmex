<%@page contentType="text/html;charset=UTF-8" %>
<%@include file="/taglibs.jsp" %>
<%pageContext.setAttribute("currentHeader", "dict");%>
<%pageContext.setAttribute("currentMenu", "dict");%>
<%pageContext.setAttribute("currentMenuName", "系统配置");%>
<%pageContext.setAttribute("currentChildMenu", "业务类型明细");%>
<!doctype html>
<html>
<head>
    <%@include file="/common/meta.jsp" %>
    <title>麦联</title>
    <%@include file="/common/s3.jsp" %>
    <style>
    	.chk_common,.chk_range,.chk_person,.chk_branch{display:none;}
    </style>
</head>
<body>
<div class="row-fluid">
    <section id="m-main" class="col-md-10">
        <div class="panel">
            <div class="panel-body">
                <form id="dictTypeForm" name="dictTypeForm" method="post"
                      action="dict-business-detail-contidion-area-save.do" class="form-horizontal">
                     <div class="form-group">
                   		<label class="col-md-5" style="float:left;">细分名称：</label>
	                     <div class="col-sm-5">
                        	<input type="hidden" name="detailId" value="${business.id}"/>
                            ${business.busiDetail}
                        </div>
                        
                    </div>
                    <div class="form-group">
                   		<label class="col-md-5" style="float:left;">是否设置大区：</label>
	                    <div class="form-group">
	                        <div class="col-md-5">
	                            <input type="radio" value="1" name="isOpen" <c:if test="${commonChecked == '1' }">checked="checked"</c:if> > 启用
	                            <input type="radio" value="0" name="isOpen" <c:if test="${commonChecked == '0' }">checked="checked"</c:if>> 关闭
	                        </div>
	                    </div>
                    </div>
                    <div class="form-group">
                        <div class="col-md-5" style="margin:0 auto;width:100%;text-align:center;">
                            <button type="submit"  class="btn btn-default">提交</button>&emsp;
                            <button type="button" onclick="window.parent.$('#popWinClose').click();" class="btn btn-default">关闭</button>
                        </div>
                    </div>
                    
                </form>
            </div>
        </div>
    </section>
</div>
<script  type="text/javascript">
	var fnChecked=function(id,className){
		if($(id).attr("checked"))
			$("."+className).attr("checked",true);
		else
			$("."+className).attr("checked",false);
	}
	$(function(){
		$("input:checkbox:checked").each(function(i,item){
			var className=$(item).attr("data-class");
			if($(item).attr("checked"))
				$("."+className).attr("checked",true);
			else
				$("."+className).attr("checked",false);
		});
	})
</script>
</body>
</html>



