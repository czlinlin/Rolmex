<%--
  
  User: cz
  Date: 2018\07\23 
  
 
--%>
<%@page contentType="text/html;charset=UTF-8" %>
<%@include file="/taglibs.jsp" %>
<%
    pageContext.setAttribute("currentHeader", "person");
%>
<%
    pageContext.setAttribute("currentMenu", "person");
%>
<%
    pageContext.setAttribute("currentMenuName", "人事管理");
%>
<!doctype html>
<html lang="en">

<head>
    <%@include file="/common/meta.jsp" %>
    <title><spring:message code="dev.employee-info.list.title"
                           text="麦联"/></title>
    <%@include file="/common/s3.jsp" %>
    <script type="text/javascript"
            src="${cdnPrefix}/bootbox/bootbox.min1.js"></script>
    <script type="text/javascript">
     
        $(function () {
        	
        	document.getElementById("importDeadLine").value = ${model.cutOffData};
        	var tipMsg=$('#m-success-tip-message').html();
        	window.parent.bootbox.alert({
                message:tipMsg,
                size: 'small',
                buttons: {
                    ok: {
                        label: "确定"
                    }
                }
            });
            
            
            
        });

    </script>
    <style type="text/css">

    #tb1{text-align:center; margin:10px auto;width:80%;line-height: 2.5;}
        #tb1 td{border:1px solid #BBB; }
    
        th {
            white-space: nowrap
        }
        td{
        	white-space: nowrap
        }
    </style>
</head>

<body>

<div class="row-fluid">
	<c:if test="${not empty flashMessages}">
		<div id="m-success-tip-message"  style="background:black;top:155px;">
			<ul>
				<c:forEach items="${flashMessages}" var="item">
					<c:if test="${item != ''}">
						<li style="list-style:none;">${item}</li>
					</c:if>
				</c:forEach>
			</ul>
		</div>
	   </c:if>

    <!-- start of main -->
    <section id="m-main" class="col-md-12">
       <form id="attendanceImportSetForm" name="attendanceImportSetForm"
       action="${tenantPrefix}/auth/attendanceImportSetSave.do"
              method='post' class="form-inline">   
            <section id="m-main" class="col-md-12" style="padding-top:65px;">   
            <div class="panel panel-default">
                <div class="panel-heading">
                    <i class="glyphicon glyphicon-list"></i>
                    <spring:message code="scope-info.scope-info.list.title" text="导入设置"/>
                </div>
             
			<div>
                <table id="tb1" >
                   <tr>
                    <td style="text-align:right;padding-right:5px;">
	                	<span>导入截止日期：</span>
	                </td>
                    <td style="text-align:left;">
                    	<select id="importDeadLine" name="busDetails" class="form-control"  onchange="setDeadLine()" style="width:200px;margin:5px 0 5px 15px;">
                    		<option value="99">不设置</option>
                        	<option value="1">1</option>
							<option value="2">2</option>
							<option value="3">3</option>
							<option value="4">4</option>
							<option value="5">5</option>
							<option value="6">6</option>
							<option value="7">7</option>
							<option value="8">8</option>
							<option value="9">9</option>
							<option value="10">10</option>
							<option value="11">11</option>
							<option value="12">12</option>
							<option value="13">13</option>
							<option value="14">14</option>
							<option value="15">15</option>
							<option value="16">16</option>
							<option value="17">17</option>
							<option value="18">18</option>
							<option value="19">19</option>
							<option value="20">20</option>
							<option value="21">21</option>
							<option value="22">22</option>
							<option value="23">23</option>
							<option value="24">24</option>
							<option value="25">25</option>
							<option value="26">26</option>
							<option value="27">27</option>
							<option value="28">28</option>
							<option value="29">29</option>
							<option value="30">30</option>
							<option value="31">31</option>
                         	<input id="attendanceImportDeadLine" name="cutoffdata" type="hidden" value="${model.cutOffData}">
                     	</select>
                     </td>
                   </tr> 
                   <tr>
                    <td style="text-align:right;padding-right:5px;">
	                	<span >期限范围：</span>
	                </td>
                    <td style="text-align:left;padding-left:15px;">
                           <div id="pickerStartTime" class="input-group date" style="padding:5px 0;">
                               <input type="text" name="startdata" onClick="WdatePicker({dateFmt:'yyyy-MM-dd',maxDate:enddata.value})"
                                      value="<fmt:formatDate value='${model.startData}' type="both" pattern='yyyy-MM-dd '/>"
                                      readonly style="background-color:white;cursor:default; line-height: 1.5;"
                                      class="Wdate form-control">
                           </div>
                        	至
                           <div id="pickerEndTime" class="input-group date" >
                               <input id="enddata" type="text" name="enddata" onClick="WdatePicker({dateFmt:'yyyy-MM-dd',minDate:startdata.value})" 
                                      value="<fmt:formatDate value='${model.endData}' type="both" pattern='yyyy-MM-dd '/>"
                                      readonly
                                      style="background-color:white;cursor:default;line-height: 1.5;" 
                                      class="Wdate form-control">
                              
                           </div>
                    </td>
                   </tr>
          </table>
		
			
                  <div style="margin:10px auto;width:98%;text-align:center;">
                      <button type="button" class="btn btn-default a-submit" onclick="attendanceImportSetSave()">保存
                      </button>
                  </div>
         	</div>    
		
     </div>
   </section>    
   </div>
  </form>

    </section>
    <!-- end of main -->
</div>

</body>
<script>

	function setDeadLine(){
	
		var  myselect=document.getElementById("importDeadLine");
		var index=myselect.selectedIndex ;  
		var bt=myselect.options[index].value;
		document.getElementById("attendanceImportDeadLine").value=bt;
	}
	
	
	 function attendanceImportSetSave(){
			
			$('#attendanceImportSetForm').attr('action', '${tenantPrefix}/auth/attendanceImportSetSave.do');
			$('#attendanceImportSetForm').submit();
		
		}

</script>


</html>

