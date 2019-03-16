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
                      action="dict-business-detail-contidion-save.do" class="form-horizontal">
                     <div class="form-group">
                   		<label class="col-md-5" style="float:left;">细分名称：</label>
	                     <div class="col-sm-5">
                        	<input type="hidden" name="detailId" value="${business.id}"/>
                            ${business.busiDetail}
                        </div>
                        
                    </div>
                    <c:if test="${isCommonProcess=='1'}">
                    <fieldset>
                    	<legend style="color:blue">用于设置公共流程</legend>
                    	<div class="form-group">
	                    	<label class="control-label col-md-1" for="dictType_name">公共流程设置（<font color='red'>公共流程有效</font>）：</label>
	                        <div class="col-md-5">
	                            <input type="checkbox" class="chk_common" name="conditionType" value="common-setting"/>
	                           	<input type="checkbox" class="chk_common" name="dataType" value="0"/>
	                           	1.<input onclick="fnChecked(this,'chk_common')" data-class="chk_common" name="chkContidion" type="checkbox" id="chk_common" value="is-show" ${commonChecked}/>
	                           	<input type="checkbox" class="chk_common" name="note" value="1"/>
	                           	<label for="chk_chk_common">公共流程[控制公共流程是否显示]</label>&emsp;&emsp;
	                        </div>
	                    </div>
	                    <div class="form-group">
	                    	<label class="control-label col-md-1" for="dictType_name">公共流程适用范围（<font color='red'>公共流程有效</font>）：</label>
	                        <div class="col-md-5">
	                            <c:forEach items="${companylist}" var="item" varStatus="status">
	                            	<input type="checkbox" class="chk_range chk_range_${item.value}" name="conditionType" value="common-setting"/>
	                            	<input type="checkbox" class="chk_range chk_range_${item.value}" name="dataType" value="1"/>
	                            	${status.index+1}.<input onclick="fnChecked(this,'chk_range_${item.value}')" data-class="chk_range_${item.value}" name="chkContidion" type="checkbox" id="chk_${item.value}" value="company-value" ${item.checked}/>
	                            	<label for="chk_${item.value}">${item.name}</label>&emsp;&emsp;
	                            	<input type="checkbox" class="chk_range chk_range_${item.value}" name="note" value="${item.value}"/>
	                            	<c:if test="${(status.index+1)%5==0}">
	                            		<br/>
	                            	</c:if>
	                            </c:forEach>
	                        </div>
	                    </div>
	                    <div class="form-group">
	                    	<label class="control-label col-md-1" for="dictType_name">公共流程使用人员（<font color='red'>公共流程有效</font>）：</label>
	                        <div class="col-md-5">
	                           	<input type="checkbox"  class="chk_person" name="conditionType" value="common-setting"/>
	                           	<input type="checkbox"  class="chk_person" name="dataType" value="0"/>
	                           	1.<input onclick="fnChecked(this,'chk_person')" data-class="chk_person" name="chkContidion" type="checkbox" id="chk_common" value="person-probation" ${commonStartChecked}/>
	                           	<input type="checkbox"  class="chk_person" name="note" value="2"/>
	                           	<label for="chk_chk_common">试用期人员</label>&emsp;&emsp;
	                        </div>
	                    </div>
                    </fieldset>
                   </c:if>
                    <fieldset>
                    	<legend style="color:blue">用于分支流程</legend>
	                    <div class="form-group">
	                    	<label class="control-label col-md-1" for="dictType_name">选择流条件：</label>
	                        <div class="col-md-5">
	                            <c:forEach items="${contidionlist}" var="item" varStatus="status">
	                            	<input type="checkbox" class="chk_branch chk_branch_${item.id}"  name="conditionType" value="audit-setting"/>
	                            	<input type="checkbox" class="chk_branch chk_branch_${item.id}" name="dataType" value="${item.type}"/>
	                            	${status.index+1}.<input onclick="fnChecked(this,'chk_branch_${item.id}')" data-class="chk_branch_${item.id}" name="chkContidion" type="checkbox" id="chk_${item.id}" value="${item.value}" ${item.checked}/>
	                            	<input type="checkbox" class="chk_branch chk_branch_${item.id}" name="note" value=""/>
	                            	<label for="chk_${item.id}">${item.name}</label>&emsp;&emsp;
	                            </c:forEach>
	                        </div>
	                    </div>
                    </fieldset>
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



