<%@page contentType="text/html;charset=UTF-8"%>
<%@include file="/taglibs.jsp"%>
<%pageContext.setAttribute("currentHeader", "contract");%>

<%pageContext.setAttribute("currentMenu", "contract");%>
<!doctype html>
<html lang="en">

  <head>
    <%@include file="/common/meta.jsp"%>
    <title><spring:message code="dev.org.list.title" text="麦联"/></title>
    <%@include file="/common/s3.jsp"%>
    
    <script type="text/javascript">
    $(function(){
    	var tipMsg=$('#m-success-tip-message').html();
    	window.parent.bootbox.alert({
            message:tipMsg,
            size: 'large',
            buttons: {
                ok: {
                    label: "确定"
                }
            }
        });
    })
    </script>
  </head>

  <body>
   <c:if test="${not empty flashMessages}">
        <div id="m-success-tip-message" style="display: none;">
            <ul>
                <c:forEach items="${flashMessages}" var="item">
                    <c:if test="${item != ''}">
                        <li  style="list-style:none; word-wrap:break-word;">${item}</li>
                    </c:if>
                </c:forEach>
            </ul>
        </div>
    </c:if>
    <div class="row-fluid">
      <section id="m-main" class="col-md-12" style="padding-top:65px;">
		<div class="panel panel-default">
		  <div class="panel-heading">
			<i class="glyphicon glyphicon-list"></i>
		    查询
			<div class="pull-right ctrl">
			  <a class="btn btn-default btn-xs"><i id="orgSearchIcon" class="glyphicon glyphicon-chevron-up"></i></a>
		    </div>
		  </div>
  		  <div class="panel-body">
			  <form name="authSearchForm" method="post" action="auth-contractdata-i.do" class="form-inline" >
			    <label for="org_name"><spring:message code='org.org.list.search.name' text='姓名'/>:</label>
			    <input type="text" id="name" name="name" value="${name}" class="form-control">
				<button id="btnSearch" class="btn btn-default a-search" type="submit">查询</button>&nbsp;
			  </form>
		  </div>
	   </div>
	   
       <div style="margin-bottom: 20px;">
	    <div class="pull-left">
	      <div class="btn-group" role="group">
	      	<button class="btn btn-default a-insert" onclick="location.href='newBuildOrEdit-contractdata.do?id=0'">新建</button>
		  </div>
		</div>
	    <div class="clearfix"></div>
	  </div>

<form id="orgGridForm" name="orgGridForm" method='post' action="org-remove.do" class="m-form-blank">
      <div class="panel panel-default">
        <div class="panel-heading">
		  <i class="glyphicon glyphicon-list"></i>
		  <spring:message code="scope-info.scope-info.list.title" text="列表"/>
		</div>
  <table id="orgGrid" class="table table-hover">
    <thead>
      <tr>
        <th class="sorting">类型</th>
        <th class="sorting">名称</th>
        <th class="sorting">负责合同单位</th>
        <th class="sorting">备注</th>
        <th width="120">操作</th>
      </tr>
    </thead>
    <tbody>
      <c:forEach items="${authContractdataList}" var="item">
	      <tr>
	        <td>
	        	<c:if test="${item.type == '1'}">
	        		人员
	        	</c:if>
	        	<c:if test="${item.type == '2'}">
	        		角色
	        	</c:if>
	        </td>
	        <td>
	        	${item.name}
	        </td>
	        <td>
	        	${item.company_ids}
	        </td>
	        <td>
	        	${item.note}
	        </td>
	        <td>
			    <a href="newBuildOrEdit-contractdata.do?id=${item.union_id}" class="a-remove">编辑</a>
			  	<a href="auth-contractdata-del.do?unionId=${item.union_id}" onclick="if(confirm('确认删除这条配置吗？')==false) return false;" class="a-remove">删除</a>
			 </td>
	      </tr>
	     
      </c:forEach>
    </tbody>
  </table>
      </div>
</form>
      <div class="m-spacer"></div>
      </section>
	</div>
  </body>
</html>

