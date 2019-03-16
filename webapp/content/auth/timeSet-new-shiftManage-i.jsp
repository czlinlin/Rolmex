<%@page contentType="text/html;charset=UTF-8"%>
<%@include file="/taglibs.jsp"%>
<%pageContext.setAttribute("currentHeader", "org");%>

<%pageContext.setAttribute("currentMenu", "org");%>
<!doctype html>
<html lang="en">

  <head>
    <%@include file="/common/meta.jsp"%>
    <title><spring:message code="dev.org.list.title" text="麦联"/></title>
    <%@include file="/common/s3.jsp"%>
    <script type="text/javascript">
    	$(function(){
    		getTime()
    	})
    	function getTime(){
    		//~--------------上班时间对---------------------
    		var startTimeH = $("#startTimeH").val();
    		var startTimeM = $("#startTimeM").val();
    		var endTimeH = $("#endTimeH").val();
    		var endTimeM = $("#endTimeM").val();
    		//~--------------休息时间对-------------------------------
    		var restStartTimeH = $("#restStartTimeH").val();
    		var restStartTimeM = $("#restStartTimeM").val();
    		var restEndTimeH = $("#restEndTimeH").val();
    		var restEndTimeM = $("#restEndTimeM").val();
    		//统一时间单位为分
    		//~---------------休息时间-----------------------------------
    		restStartTimeH = restStartTimeH*60;
    		var restStartTime = restStartTimeH + parseInt(restStartTimeM.replace("00",0));//休息开始时间，单位分钟
    		restEndTimeH = restEndTimeH*60;
    		var restEndTime = restEndTimeH + parseInt(restEndTimeM.replace("00",0));//休息结束时间，单位分钟
    		var restTime = restEndTime - restStartTime;
    		restTime = restTime/60;
    		$("#restTime").text(restTime);
    		//~--------------工作时间-----------------------------------------
    		startTimeH = startTimeH*60;
    		var startTime = startTimeH + parseInt(startTimeM.replace("00",0));//上班时间，单位分钟
    		endTimeH = endTimeH*60;
    		var endTime = endTimeH + parseInt(endTimeM.replace("00",0));//下班时间，单位分钟
    		var time = endTime - startTime;
    		time = time/60;
    		time = time - restTime;
    		$("#workTime").text(time);
    		//alert(time+"分钟");
    	} 
    	
    	function saveShift(){
    		var shiftName = $("#shiftName").val();
    		var workTime = $("#workTime").text();
    		var restTime = $("#restTime").text();
    		if(workTime <= 0 || restTime < 0){
    			alert("请检查工作时间设置或休息时间设置！");
    			return false;
    		}
    		if(restTime == 0){
    			var operation= confirm("提示：您设置的休息时长为0小时！");
    			if(operation == false){
    				return false;
    			}
    		}
    		if(shiftName == ""){
    			alert("班次名称为必填项");
    			return false;
    		}else{
    			$("#orgGridForm").submit();
    		}
    	}
    	function cancel(){
    		location.href = "timeSet-shiftManage-i.do";
    	}
    </script>
  </head>

  <body>
    
    <div class="row-fluid">
	  <!-- start of main -->
<form id="orgGridForm" name="orgGridForm" action="save-shift-i.do" method='post' class="m-form-blank">
	 <section id="m-main" class="col-md-12" style="padding-top:65px;">
      <div class="panel panel-default">
        <div class="panel-heading">
		  <i class="glyphicon glyphicon-list"></i>
		  <spring:message code="scope-info.scope-info.list.title" text="新增班次"/>
		</div>
		  <table id="orgGrid" class="table table-hover">
		  	<input type="hidden" name="id" value="${shift.id}"/>
		    <div style="margin-left:50px;margin-top:30px;"><lable>班次名称：</lable><input id="shiftName" name="shiftName" type="text" value="${shift.shiftName}"/></div><br/>
		    <div style="margin-left:50px;margin-top:30px;">
			    <lable>上班考勤时间设置</lable>&nbsp;&nbsp;&nbsp;&nbsp;
			    <lable>上班：</lable>
			    <select id="startTimeH" name="startTimeH" onchange="getTime()">
			    	<c:forEach var="obj" items="${hour}" varStatus="i">
			    		<c:if test="${obj==shift.startTime.substring(0,2)}">
			    			<option value="${obj}" selected>${obj}</option>
			    		</c:if>
			    		<c:if test="${obj!=shift.startTime.substring(0,2)}">
			    			<option value="${obj}">${obj}</option>
			    		</c:if>
       				</c:forEach>
			    </select>
			    <select id="startTimeM" name="startTimeM" onchange="getTime()">
			    	<c:forEach var="obj" items="${minute}" varStatus="i">
			    		<c:if test="${obj==shift.startTime.substring(3,5)}">
			    			<option value="${obj}" selected>${obj}</option>
			    		</c:if>
			    		<c:if test="${obj!=shift.startTime.substring(3,5)}">
			    			<option value="${obj}">${obj}</option>
			    		</c:if>
       				</c:forEach>
			    </select>
			    <lable>下班：</lable>
			    <select id="endTimeH" name="endTimeH" onchange="getTime()">
			    	<c:forEach var="obj" items="${hour}" varStatus="i">
			    		<c:if test="${obj==shift.endTime.substring(0,2)}">
			    			<option value="${obj}" selected>${obj}</option>
			    		</c:if>
			    		<c:if test="${obj!=shift.endTime.substring(0,2)}">
			    			<option value="${obj}">${obj}</option>
			    		</c:if>
       				</c:forEach>
			    </select>
			    <select id="endTimeM" name="endTimeM" onchange="getTime()">
			    	<c:forEach var="obj" items="${minute}" varStatus="i">
			    		<c:if test="${obj==shift.endTime.substring(3,5)}">
			    			<option value="${obj}" selected>${obj}</option>
			    		</c:if>
			    		<c:if test="${obj!=shift.endTime.substring(3,5)}">
			    			<option value="${obj}">${obj}</option>
			    		</c:if>
       				</c:forEach>
			    </select>
		    </div>
		    <br/>
		    <div style="margin-left:50px;margin-top:30px;">
			    <lable>休息时间</lable>&nbsp;&nbsp;&nbsp;&nbsp;
			    <lable>休息开始：</lable>
			    <select id="restStartTimeH" name="restStartTimeH" onchange="getTime()">
			    	<c:forEach var="obj" items="${hour}" varStatus="i">
			    		<c:if test="${obj==shift.restStartTime.substring(0,2)}">
			    			<option value="${obj}" selected>${obj}</option>
			    		</c:if>
			    		<c:if test="${obj!=shift.restStartTime.substring(0,2)}">
			    			<option value="${obj}">${obj}</option>
			    		</c:if>
       				</c:forEach>
			    </select>
			    <select id="restStartTimeM" name="restStartTimeM" onchange="getTime()">
			    	<c:forEach var="obj" items="${minute}" varStatus="i">
			    		<c:if test="${obj==shift.restStartTime.substring(3,5)}">
			    			<option value="${obj}" selected>${obj}</option>
			    		</c:if>
			    		<c:if test="${obj!=shift.restStartTime.substring(3,5)}">
			    			<option value="${obj}">${obj}</option>
			    		</c:if>
       				</c:forEach>
			    </select>
			    <lable>休息结束：</lable>
			    <select id="restEndTimeH" name="restEndTimeH" onchange="getTime()">
			    	<c:forEach var="obj" items="${hour}" varStatus="i">
			    		<c:if test="${obj==shift.restEndTime.substring(0,2)}">
			    			<option value="${obj}" selected>${obj}</option>
			    		</c:if>
			    		<c:if test="${obj!=shift.restEndTime.substring(0,2)}">
			    			<option value="${obj}">${obj}</option>
			    		</c:if>
       				</c:forEach>
			    </select>
			    <select id="restEndTimeM" name="restEndTimeM" onchange="getTime()">
			    	<c:forEach var="obj" items="${minute}" varStatus="i">
			    		<c:if test="${obj==shift.restEndTime.substring(3,5)}">
			    			<option value="${obj}" selected>${obj}</option>
			    		</c:if>
			    		<c:if test="${obj!=shift.restEndTime.substring(3,5)}">
			    			<option value="${obj}">${obj}</option>
			    		</c:if>
       				</c:forEach>
			    </select>
				<br/>
			</div>
			<div style="margin-left:50px;margin-top:30px;">
				<lable>合计工作时长<span id="workTime"></span>小时，休息时间<span id="restTime"></span>小时。</lable>
			<div style="margin-left:50px;margin-top:30px;">
			<br/>
			<div style="margin-left:50px;">
				<button type="button" class="btn btn-default" onclick="saveShift()">保存</button>
				<button type="button" class="btn btn-default" onclick="cancel()">取消</button>
			</div>
		  </table>
      </div>
      </section>
</form>
	  <!-- end of main -->
	</div>
  </body>
</html>

