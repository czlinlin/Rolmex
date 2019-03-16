<%@page contentType="text/html;charset=UTF-8"%>
<%@include file="/taglibs.jsp"%>
<%pageContext.setAttribute("currentHeader", "bpm-workspace");%>
<%pageContext.setAttribute("currentMenu", "bpm-process");%>
<%pageContext.setAttribute("currentChildMenu", "发起流程");%>
<!doctype html>
<html lang="en">

  <head>
    <%@include file="/common/meta.jsp"%>
    <title>麦联</title>
    <%@include file="/common/s3.jsp"%>
  </head>

  <body>
    <%@include file="/header/bpm-workspace3.jsp"%>

    <div class="row-fluid">
    <%-- <%@include file="/menu/bpm-workspace3.jsp"%> --%>
	<%@include file="/menu/sidebar.jsp" %>
    <!-- start of main -->
      <section id="m-main" class="col-md-10" style="padding-top:65px;">
		<ul class="breadcrumb">
	    <li><a href="workspace-home.do">发起流程</a></li>
	    <!-- <li class="active"></li>  -->
	    </ul>
		<c:forEach items="${bpmCategories}" var="bpmCategory">
        	<div>
        		<c:if test="${fn:contains(ids,bpmCategory.id)}">
		    	<div class="panel panel-default">
			      	<div class="panel-heading">
				    	<h3 class="panel-title">
					  		<i class="glyphicon glyphicon-list"></i>
					  		${bpmCategory.name}
						</h3>
			      	</div>
			  		<div class="panel-body">
						<c:forEach items="${bpmCategory.bpmProcesses}" var="bpmProcess">
							<c:if test="${bpmProcess.showFlag == 1 }">
						    <div class="col-md-6">
								<div class="caption">
								  <h4 title ="${bpmProcess.name}">
								  	<a href="${tenantPrefix}/operation/process-operation-viewStartForm.do?categoryId=<tags:encrypParameter parameters='${bpmCategory.id}'/>&bpmProcessId=<tags:encrypParameter parameters='${bpmProcess.id}'/>">${bpmProcess.name}</a>
								  </h4>
								    <%-- <p>${bpmProcess.descn}&nbsp;</p> 
								 
								    <div class="btn-group" style="margin-bottom:10px;">
								      <a class="btn btn-default btn-sm" href="${tenantPrefix}/operation/process-operation-viewStartForm.do?bpmProcessId=${bpmProcess.id}"><i class="glyphicon glyphicon-play"></i> 发起</a>
								      <a class="btn btn-default btn-sm" href="workspace-graphProcessDefinition.do?bpmProcessId=${bpmProcess.id}" target="_blank"><i class="glyphicon glyphicon-picture"></i> 图形</a>
								    </div> --%>
								</div>
						    </div>
							</c:if>
						</c:forEach>
      					<c:if test="${bpmCategory.id == 3}">
					      	<div class="col-md-6">
							  <div class="caption">
							    <h4>
							    	<a href="${tenantPrefix}/operationCustom/custom-apply-list.do?userName=${userName}">自定义申请</a>
							    </h4>
					            <%-- <p>自定义流程&nbsp;</p>
					         
					            <div class="btn-group" style="margin-bottom:10px;">
					             <a class="btn btn-default btn-sm" href="${tenantPrefix}/operationCustom/custom-apply-list.do?userName=${userName}"><i class="glyphicon glyphicon-play"></i> 发起</a>
					             
					            </div> --%>
							  </div>
					        </div>
					        <div class="col-md-6">
							  <div class="caption">
							    <h4>
							    	<a href="${tenantPrefix}/workOperationCustom/custom-work-apply-list.do?userName=${userName}&formType=1">请假申请</a>
							    </h4>
					            </div>
							  </div>
							  <div class="col-md-6">
							  <div class="caption">
							    <h4>
							    	<a href="${tenantPrefix}/workOperationCustom/custom-work-apply-list.do?userName=${userName}&formType=2">出差外出申请</a>
							    </h4>
					            </div>
							  </div> 
							  <div class="col-md-6">
							  <div class="caption">
							    <h4>
							    	<a href="${tenantPrefix}/workOperationCustom/custom-work-apply-list.do?userName=${userName}&formType=3">加班申请</a>
							    </h4>
					            </div>
							  </div>
							  <div class="col-md-6">
							  <div class="caption">
							    <h4>
							    	<a href="${tenantPrefix}/workOperationCustom/custom-work-apply-list.do?userName=${userName}&formType=4">特殊考勤说明申请</a>
							    </h4>
					            </div>
							  </div> 
							 
					        </div>
      					</c:if>
			  		</div>
		    	</div>
				</c:if>
        	</div>
		</c:forEach>

    </section>
    <!-- end of main -->
    </div>

  </body>

</html>
