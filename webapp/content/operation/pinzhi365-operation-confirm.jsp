<%@page contentType="text/html;charset=UTF-8"%>
<%@include file="/taglibs.jsp"%>
<%pageContext.setAttribute("currentHeader", "bpm-workspace");%>
<%pageContext.setAttribute("currentMenu", "bpm-process");%>
<!doctype html>
<html lang="en">
<head>
<%@include file="/common/meta.jsp"%>

    <title><spring:message code="demo.demo.input.title" text="麦联"/></title>
    <%@include file="/common/s3.jsp"%>
	<!-- bootbox -->
    <script type="text/javascript" src="${cdnPrefix}/bootbox/bootbox.min.js"></script>
	<link href="${cdnPrefix}/xform3/styles/xform.css" rel="stylesheet">
    <script type="text/javascript" src="${cdnPrefix}/xform3/xform-packed.js"></script>

    <link type="text/css" rel="stylesheet" href="${cdnPrefix}/userpicker3-v2/userpicker.css">
    <script type="text/javascript" src="${cdnPrefix}/userpicker3-v2/userpicker.js"></script>
	<script type="text/javascript" src="${cdnPrefix}/operation/TaskOperation.js"></script>
	<script type="text/javascript" src="${cdnPrefix}/operation/operation.js?v=1.20"></script>
	
	<style type="text/css">
		.xf-handler {
			cursor: auto;
		}
	</style>
	
	<script type="text/javascript">
		document.onmousedown = function(e) {};
		document.onmousemove = function(e) {};
		document.onmouseup = function(e) {};
		document.ondblclick = function(e) {};

		var xform;
		
		 //调用接口，根据经销商编号，获取直销oa上存的对应信息：姓名 电话 等
		$(function() {
		 	
		      setTimeout(function() {
				$('.datepicker').datepicker({
					autoclose: true,
					language: 'zh_CN',
					format: 'yyyy-mm-dd'
				})
			}, 500);
		})
		
		ROOT_URL = '${tenantPrefix}';
		taskOperation = new TaskOperation();
		
		function applyCompleteTask(flag) {
			
			//若要驳回或不同意，必须填写意见才能提交
			if ((flag == 2 || flag == 0)&& document.getElementById('comment').value ==""){
				alert("请填写意见！");
				return false;
			}

            var conf={
                "formId":"xform",
                "checkUrl":"${tenantPrefix}/rs/customer/opteraion-verifyPassword",
                "actionUrl": '${tenantPrefix}/pinzhi365/pinzhi365-completeTask.do?flag=' + flag,
                "iptPwdId":"txtPrivateKey"}

            operationSubmit(conf);
		}

	</script> 
</head>
<style type="text/css">
         #tb1 td{border:1px solid #BBB }
        .f_td{ width:120px; font-size:12px;white-space:nowrap }
        .f_r_td{ width:130px; text-align:left;}
        #tb1 tr td input{border: navajowhite;text-align:center;width:100%;}  
        #tb1 tr td textarea{border: navajowhite;text-align:center;}    
        #tb1 tr td{text-align:center;}        
    </style>
<body>
 <%@include file="/header/bpm-workspace3.jsp"%>
 	<form id="xform" method="post"    class="xf-form" enctype="multipart/form-data">
			品质365流程审批	
    <br />
<div class="container">
    	<section id="m-main" class="col-md-12" style="padding-top:65px;">
		<input id ="filePath" name="filePath"  type="hidden" >
		<input id="processInstanceId" type="hidden" name="processInstanceId" value="<%= request.getParameter("processInstanceId")%>">
		<input id="humanTaskId" type="hidden"  name="humanTaskId" value="<%= request.getParameter("humanTaskId")%>">
     <table id="tb1" >
    	 <tr>
                <td colspan='8' align='center' class='f_td'>
                    <h2>品质365申请单审批</h2>
                </td>
        </tr>
        
            
            	<tr>
                <td colspan='8' align='center' class='f_td'>
                    <span style="color:Red">*</span>意见
                </td>
            </tr>
            <tr>
                <td colspan='8' style='height:80px' >
                    <textarea name="comment" id="comment" rows="2" cols="20" id="appContext" class="text0" style="height:79px;width:1100px;" ></textarea>
                </td>
            </tr>
            
        </table>
       
        
		   <table> 
		   <div class="form-group">
            	<tr>
            		<td colspan='8'><code>*</code>操作密码：</td>
            	</tr>
            </div>
            <div class="form-group">
            	<tr>
                   <td colspan='8'>
                         <input name="txtPrivateKey" type="password" maxlength="25" id="txtPrivateKey" />
                         <input  id="isPwdRight" name="isPwdRight" STYLE="display:none;"/>
                   </td>
         		</tr>
         	 </div>
       </table>      
        </div>
       </section>

  </div> 
	  <div class="navbar navbar-default navbar-fixed-bottom">
	    	<div class="text-center" style="padding-top:8px;">
			    <div class="text-center" style="padding-top:8px;">
					<button id="completeTask1" type="button" class="btn btn-default" onclick="applyCompleteTask(1)">同意</button>
					<button id="completeTask2" type="button" class="btn btn-default" onclick="applyCompleteTask(2)">驳回</button>
					<button id="completeTask3" type="button" class="btn btn-default" onclick="applyCompleteTask(0)">不同意</button>
				</div>
			</div>
	   </div>
	</div>
	

	 <table width="70%" cellspacing="0" cellpadding="0" border="0" align="center" class="table table-border">
		  <thead>
		    <tr>
			  <th>环节</th>
			  <th>审批人</th>
			  <th>时间</th>
			  <th>结果</th>
			  <th>意见</th>
			</tr>
		  </thead>
		  <tbody>
			  <c:forEach var="item" items="${logHumanTaskDtos}">
			  <c:if test="${not empty item.completeTime}">
		    <tr>
			  <td>${item.name}</td>
			  <td><tags:user userId="${item.assignee}"/></td>
			  <td><fmt:formatDate value="${item.completeTime}" type="both"/></td>
			  <td>${item.action}</td>
			   <td>${item.comment}</td>
			  <script>
			  
				</script>
				
			</tr>
			  </c:if>
			  </c:forEach>
		  </tbody>
	</table>
</form>

</body>
</html>

