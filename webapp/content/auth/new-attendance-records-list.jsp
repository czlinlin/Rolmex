<%@page contentType="text/html;charset=UTF-8"%>
<%@include file="/taglibs.jsp"%>
<!doctype html>
<html lang="en">
<head>
    <link type="text/css" rel="stylesheet" href="${cdnPrefix}/userpicker3-v2/userpicker.css">
    <script type="text/javascript" src="${cdnPrefix}/userpicker3-v2/orgpicker2.js"></script> 
<%@include file="/common/meta.jsp"%>
<title><spring:message code="dev.dict-type.list.title" text="麦联" /></title>
<%@include file="/common/s3.jsp"%>
<script type="text/javascript">
$(function() {
	createOrgPicker2({
        modalId: 'orgPicker2',
        showExpression: true,
        chkStyle: 'checkbox',
        chkboxType: { "Y" : "s", "N" : "s" },
        searchUrl: '${tenantPrefix}/rs/user/search',
        treeUrl: '${tenantPrefix}/rs/party/treeForAttendanceRecordSet?partyStructTypeId=1&partyTypeId=${partyType.id}',
        childUrl: '${tenantPrefix}/rs/party/searchUser',
        loadingImg:'${cdnPrefix}/mossle/img/loading.gif'
    });	
})  
</script>

<script type="text/javascript">
      //更改班次
       		var changeShift = function (flag) {
				var dialog = bootbox.dialog({
			        message: '<p class="text-center"><i class="fa fa-spin fa-spinner"></i>正在加载...</p>',
			        size: 'small',
			        closeButton: false
			    });
			    var html = '<div class="panel panel-default" style="max-height:500px;overflow-y:scroll;"> <table class="table table-hover" style="width:100%;"><tr><td >班次名称</td><td>考勤时间</td></tr>';
			    
			    $.ajax({
			        url: "${tenantPrefix}/rs/auth/shift-list",
			        type: "POST",
			        timeout: 10000,
			        success: function (data) {
			            dialog.modal('hide');
			                if (data == undefined || data == null || data == "" || data.length < 1)
			                    html += '<tr><td colspan="2">噢没有可选班次呢</td></tr>'
			                else {
			                    if (data.length > 0) {
			                    	for (var i = 0; i < data.length; i++) {
			                    		
			                    		if(data[i].id!=null && data[i].id!="" && data[i].shiftName!=null&&data[i].delStatus==0 ){
			                    			
			                    			var selected="";
			                    			//若非快捷设置，复选框自动选中之前的选项
			                    			if(flag!=99){
			                       				if(document.getElementById("shiftid"+flag).value==data[i].id)selected="checked";
			                    			}
			                    			
			                       			var startEndTime
			                       			if(data[i].startTime==null&&data[i].endTime==null){
			                       				startEndTime="";
			                       			}else{
			                       				startEndTime = data[i].startTime + ' - ' +  data[i].endTime
			                       			}
			                    			
			                    		html += '<tr><td > <input id="shiftid'+i+'" '+selected+'  ShiftTime="'+startEndTime+'" type="radio" value=' + data[i].id + ' name="changeShiftId" >' + data[i].shiftName + '</td><td>' +  startEndTime + '</td></tr>'
			                    		}
			                    	}
			                    }
			                }
			          html += "</table></div>";
			          changeShiftDialog(html,flag);
			        },
			        error: function (XMLHttpRequest, textStatus, errorThrown) {
			            alert("请求超时")
			        },
			        complete: function (xh, status) {
			            dialog.modal('hide');
			            if (status == "timeout")
			                bootbox.alert("请求超时");
			        }
			    });
			}
        
			var changeShiftDialog = function (show,flag) {
				var inputName = "shiftid"+flag;
				var shiftTimeFlag = "shiftTime"+flag;
				bootbox.dialog({
			        title: '选择班次',
			        message: show,
			        buttons: {
			            ok: {
			                label: "确定",
				            callback: function(){
				            		var iptSelect=$("input[name='changeShiftId']:checked");
				            		var iptSelectValue=iptSelect.val();
				            		var tdContent =iptSelect.attr("ShiftTime");
				            		
				            		if(iptSelectValue==undefined){
	    		            			alert('请选择班次');
	    		            			return false;
	    		            		}
				            		
				            		if(flag!=99){	
					            		document.getElementById(inputName).value = iptSelectValue;
					                	document.getElementById(shiftTimeFlag).innerText = tdContent;
				            		}
				                	 if(flag==99){		//快捷设置
				                		 
				                		 if(document.getElementsByName("selOne")[0].checked==true) {
				                			 document.getElementById("shiftid1").value = iptSelectValue;
							                document.getElementById("shiftTime1").innerText = tdContent;
				                	     } 
				                		 if(document.getElementsByName("selOne")[1].checked==true) {
				                			 document.getElementById("shiftid2").value = iptSelectValue;
							                document.getElementById("shiftTime2").innerText = tdContent;
				                	     }
				                		 if(document.getElementsByName("selOne")[2].checked==true) {
				                			 document.getElementById("shiftid3").value = iptSelectValue;
							                document.getElementById("shiftTime3").innerText = tdContent;
				                	     }
				                		 if(document.getElementsByName("selOne")[3].checked==true) {
				                			 document.getElementById("shiftid4").value = iptSelectValue;
							                document.getElementById("shiftTime4").innerText = tdContent;
				                	     }
				                		 if(document.getElementsByName("selOne")[4].checked==true) {
				                			 document.getElementById("shiftid5").value = iptSelectValue;
							                document.getElementById("shiftTime5").innerText = tdContent;
				                	     }
				                		 if(document.getElementsByName("selOne")[5].checked==true) {
				                			 document.getElementById("shiftid6").value = iptSelectValue;
							                document.getElementById("shiftTime6").innerText = tdContent;
				                	     }
				                		 if(document.getElementsByName("selOne")[6].checked==true) {
				                			 document.getElementById("shiftid7").value = iptSelectValue;
							                document.getElementById("shiftTime7").innerText = tdContent;
				                	     }
				                		 $('#selOne1,#selOne2,#selOne3,#selOne4,#selOne5,#selOne6,#selOne7,#selAll').prop('checked',false);
				                	 }
							     
				                }
			    			},
			    		  cancel: {
							label: '取消',
						}
			        }
			    });
			}  
   
			function attendanceRecordSave(){
				$('#attendanceRecordForm').validate({
		                rules: {
		                    content: {
		                        required: true
		                    }
		                }
		            });
				if(document.getElementById("id").value== ""){
					$('#attendanceRecordForm').attr('action', '${tenantPrefix}/auth/attendanceRecordSave.do');
				}
				if(document.getElementById("id").value!= ""){
					$('#attendanceRecordForm').attr('action', '${tenantPrefix}/auth/attendanceRecordUpdate.do');
				}
			    $('#attendanceRecordForm').submit();
			} 
		
    </script>
    
<script type="text/javascript"
	src="${cdnPrefix}/bootbox/bootbox.min1.js"></script>
<script type="text/javascript" src="${cdnPrefix}/business/business.js"></script>
<style>
body {
	padding-right: 0px !important;
}
th {
	white-space: nowrap
}
#tb1{text-align:center; margin:10px auto;width:80%;}
#tb1 td{border:1px solid #BBB;height: 30px; }
#tb2  {border-collapse:separate;border-spacing:15px;} 
#tb3  {   border-collapse:   separate;   border-spacing:   30px;   } 
#tb5{text-align:center; margin:10px auto;width:80%;}
#tb5 td{border:1px solid #BBB; height: 30px;}
</style>
</head>

<body>

	<!-- start of main -->
	<section id="m-main" class="col-md-12" style="padding-top: 65px;">
		考勤 / 时间设置 / 新增考勤组
	 <div class="panel-body">	
		<form id="attendanceRecordForm" name="attendanceRecordForm" method='post' 
		class="form-horizontal" enctype="multipart/form-data" action="${tenantPrefix}/auth/attendanceRecordSave.do" >
			<div class="panel panel-default">
			<div class="panel-heading">
                <i class="glyphicon glyphicon-list"></i>
                <spring:message code="scope-info.scope-info.list.title" text="新增考勤组"/>
             </div>	
		    <div id="pickerEndTime" class="input-group date">
                <input id="plandate" type="hidden" name="plandate"
                       value="<fmt:formatDate value='${model.plandate}' type="both" pattern='yyyy-MM-dd '/>"
                       readonly
                       style="background-color:white;cursor:default;" class="form-control required">
             </div>
                 
			<table id="tb2">
	    		<tr>
	         	  	<td style="text-align:right;">
	                    <span id='tag_realName'>&nbsp;考勤组名称</span>：
	                </td>
	                <td >
	                	<input name="recordName" id="attendance_records_name" value="${attendanceRecords.recordName}" class="form-control required"> 
	                </td>
	             </tr>   
	             
	             <tr>   
	             	<td style="text-align:right;">
	             		<span id='tag_realName'>&nbsp;参与考勤人员</span>：
					</td>	
					<td>	
						<!-- <div class="form-group">
							<div class="col-sm-12"> -->
								<div class="input-group orgPicker2" style="margin-left:-15px;">
									<input id="orgPartyEntityId" type="hidden" name="attendance_records_person_id"
										value="${personAttendanceIDString}"> <input type="text"
										class="form-control required" id="attendance_records_name"
										name="personAttendanceNameString" 
										value="${personAttendanceNameString}"
										readonly="readonly" />
										<div class="input-group-addon" style="left:-15px;position: relative;margin:0px;">
											<i class="glyphicon glyphicon-user"></i>
										</div>
								</div>
							<!-- </div>
						</div> -->
						<input id="attendance_records_hidden_person" type="hidden" name="attendance_records_hidden_person"
						value="${partyEntity.level}" />
					</td>	
				</tr>
	             <tr>   
	                <td style="text-align:right;">
	                	<span id='tag_realName'>&nbsp;参与类型</span>：
	                </td>
	                <td class='f_td'>
	                    <span id='tag_realName'>&nbsp;固定班制</span>
	                </td>
	             </tr>
	             
	             <tr>
	                <td style="text-align:right;">
	                    <span id='tag_realName'>&nbsp;工作日设置</span>：
	                </td>
	                <td class='f_td'>
	                	<a href="javascript:" onclick='changeShift(99)'>快捷设置班次</a>
	                </td>
	            </tr>
	            
         	</table>
			</br>	
			
					
			<table id="tb1">
			<input id="id" name="id" type="hidden"  value = "${attendanceRecords.id}"/>
    			<tr>
         	  		<td >
	                    <input type="checkbox"  name="selAll" id="selAll"  onClick="funSelAll(this)"> 
	                </td>
	                <td >
	                    <span id='tag_realName'>&nbsp;工作日</span>
	                </td>
	                <td >
	                	<span id='tag_realName'>&nbsp;班次时间段</span>
	                </td>
	               <td class='f_td'>
	                    <span id='tag_realName'>&nbsp;操作</span>
	                </td>
              	</tr>
         <c:if test="${attendShiftTImeList!=null}"> 
           <c:forEach items='${attendShiftTImeList}' var="item">
              <tr>
              	<td>
                    <input type="checkbox" id="selOne1" name="selOne"> 
                </td>
                <td >
                    <span id='tag_realName'>&nbsp;周一 </span>
                </td>
                <td class='f_td'>
                	<span id='shiftTime1' readonly>${item.mondayShiftStartTime}-${item.mondayShiftEndTime}</span>
                    
                </td>
                <td>
                	<a href="javascript:" onclick='changeShift(1,${item.mondayShiftID})'>更改班次</a>
                	<input type = "hidden" name = "mondayShiftID" id =  "shiftid1"    value="${item.mondayShiftID}"  />
                 </td>
           	  </tr>
          
             <tr>
              	<td>
                    <input type="checkbox" id="selOne2"  name="selOne"> 
                </td>
                <td >
                    <span id='tag_realName'>&nbsp;周二 </span>
                </td>
                <td class='f_td'>
                    <span id='shiftTime2' readonly>${item.tuesdayStartTime}-${item.tuesdayEndTime}  </span>
                </td>
                <td >
                	<a href="javascript:" onclick='changeShift(2,${item.tuesdayShiftID})'>更改班次</a>
                	<input type = "hidden" name = "tuesdayShiftID" id =  "shiftid2"    value="${item.tuesdayShiftID}"  />
                 </td>
            </tr>
            <tr>
              	<td>
                    <input type="checkbox" id="selOne3"  name="selOne"> 
                </td>
                <td >
                    <span id='tag_realName'>&nbsp;周三 </span>
                </td>
                <td class='f_td'>
                    <span id='shiftTime3' readonly> ${item.wednesdayStartTime}-${item.wednesdayEndTime}</span>
                </td>
                <td>
                	<a href="javascript:" onclick='changeShift(3,${item.wednesdayShiftID})'>更改班次</a>
                	<input type = "hidden" name = "wednesdayShiftID" id =  "shiftid3"    value="${item.wednesdayShiftID}"  />
                 </td>
            </tr>
            
            <tr>
              	<td>
                    <input type="checkbox" id="selOne4"  name="selOne"> 
                </td>
                <td>
                    <span id='tag_realName'>&nbsp;周四 </span>
                </td>
                <td class='f_td'>
                    <span id='shiftTime4' readonly>${item.thursdayStartTime}-${item.thursdayEndTime} </span>
                </td> 
                <td>
                	<a href="javascript:" onclick='changeShift(4,${item.thursdayShiftID})'>更改班次</a>
                	<input type = "hidden" name = "thursdayShiftID" id =  "shiftid4"    value="${item.thursdayShiftID}"  />
                 </td>
            </tr>
            
            <tr>
              	<td>
                    <input type="checkbox" id="selOne5"  name="selOne"> 
                </td>
                <td >
                    <span id='tag_realName'>&nbsp;周五</span>
                </td>
                <td class='f_td'>
                    <span id='shiftTime5' readonly>${item.fridayStartTime}-${item.fridayEndTime} </span>
                </td>
                <td >
                	<a href="javascript:" onclick='changeShift(5,${item.fridayShiftID})'>更改班次</a>
                	<input type = "hidden" name = "fridayShiftID" id =  "shiftid5"    value="${item.fridayShiftID}"  />
                 </td>
            </tr>
            
            <tr>
              	<td>
                    <input type="checkbox" id="selOne6"  name="selOne"> 
                </td>
                <td>
                    <span id='tag_realName'>&nbsp;周六 </span>
                </td>
                <td class='f_td'>
                    <span id='shiftTime6' readonly>${item.SaturdayStartTime}-${item.SaturdayEndTime}  </span>
                </td>
                <td >
                	<a href="javascript:" onclick='changeShift(6,${item.SaturdayShiftID})'>更改班次</a>
                	<input type = "hidden" name = "SaturdayShiftID" id =  "shiftid6"  value="${item.SaturdayShiftID}"  />
                 </td>
            </tr>
            
            <tr>
              	<td >
                    <input type="checkbox" id="selOne7"  name="selOne"> 
                </td>
                <td >
                    <span id='tag_realName'>&nbsp;周日 </span>
                </td>
                <td class='f_td'> 
                    <span id='shiftTime7' readonly>${item.SundayStartTime}-${item.SundayEndTime}  </span>
                </td>
                <td >
                	<a href="javascript:" onclick='changeShift(7,${item.SundayShiftID})'>更改班次</a>
                	<input type = "hidden" name = "SundayShiftID" id =  "shiftid7" value="${item.SundayShiftID}" />
                 </td>
            </tr>
        </c:forEach> 
      </c:if>
      
       <c:if test="${attendShiftTImeList==null}"> 
            
              <tr>
              	<td>
                    <input type="checkbox" id="selOne1" name="selOne"> 
                </td>
                <td>
                    <span id='tag_realName'>&nbsp;周一 </span>
                </td>
                <td class='f_td'>
                	<span id='shiftTime1' readonly></span>
                    
                </td>
                <td>
                	<a href="javascript:" onclick='changeShift(1,"")'>更改班次</a>
                	<input type = "hidden" name = "mondayShiftID" id =  "shiftid1"      />
                 </td>
           	  </tr>
          
             <tr>
              	<td>
                    <input type="checkbox" id="selOne2" name="selOne"> 
                </td>
                <td>
                    <span id='tag_realName'>&nbsp;周二 </span>
                </td>
                <td class='f_td'>
                    <span id='shiftTime2' readonly>  </span>
                </td>
                <td>
                	<a href="javascript:" onclick='changeShift(2)'>更改班次</a>
                	<input type = "hidden" name = "tuesdayShiftID" id =  "shiftid2"      />
                 </td>
            </tr>
            <tr>
              	<td>
                    <input type="checkbox" id="selOne3" name="selOne"> 
                </td>
                <td >
                    <span id='tag_realName'>&nbsp;周三 </span>
                </td>
                <td class='f_td'>
                    <span id='shiftTime3' readonly> </span>
                </td>
                <td>
                	<a href="javascript:" onclick='changeShift(3)'>更改班次</a>
                	<input type = "hidden" name = "wednesdayShiftID" id =  "shiftid3"     />
                 </td>
            </tr>
            
            <tr>
              	<td>
                    <input type="checkbox" id="selOne4" name="selOne"> 
                </td>
                <td >
                    <span id='tag_realName'>&nbsp;周四 </span>
                </td>
                <td class='f_td'>
                    <span id='shiftTime4' readonly> </span>
                </td> 
                <td >
                	<a href="javascript:" onclick='changeShift(4)'>更改班次</a>
                	<input type = "hidden" name = "thursdayShiftID" id =  "shiftid4"      />
                 </td>
            </tr>
            
            <tr>
              	<td>
                    <input type="checkbox" id="selOne5" name="selOne"> 
                </td>
                <td >
                    <span id='tag_realName'>&nbsp;周五</span>
                </td>
                <td class='f_td'>
                    <span id='shiftTime5' readonly> </span>
                </td>
                <td >
                	<a href="javascript:" onclick='changeShift(5)'>更改班次</a>
                	<input type = "hidden" name = "fridayShiftID" id =  "shiftid5"      />
                 </td>
            </tr>
            
            <tr>
              	<td>
                    <input type="checkbox" id="selOne6" name="selOne"> 
                </td>
                <td >
                    <span id='tag_realName'>&nbsp;周六 </span>
                </td>
                <td class='f_td'>
                    <span id='shiftTime6' readonly>  </span>
                </td>
                <td >
                	<a href="javascript:" onclick='changeShift(6)'>更改班次</a>
                	<input type = "hidden" name = "SaturdayShiftID" id =  "shiftid6"  "  />
                 </td>
            </tr>
            
            <tr>
              	<td >
                    <input type="checkbox" id="selOne7" name="selOne"> 
                </td>
                <td >
                    <span id='tag_realName'>&nbsp;周日 </span>
                </td>
                <td class='f_td'> 
                    <span id='shiftTime7' readonly>  </span>
                </td>
                <td >
                	<a href="javascript:" onclick='changeShift(7)'>更改班次</a>
                	<input type = "hidden" name = "SundayShiftID" id =  "shiftid7"  />
                 </td>
            </tr>
      </c:if>
   </table>			
	</br>			
		
		<table id="tb3">
			 <tr>
         	  	<td style="text-align:right;">
                    <span id='tag_realName'>&nbsp;特殊日期</span>：
                </td>
                <td >
                	<button id="addSpecialDateButton" class="btn btn-default" type="button" onclick="addSpecialButton(1,0,'')">添加</button>
				</td>
			</tr>   
        
            <c:if test="${specialList!=null}">
           		<input  id="hidNum" name="hidNum" type="hidden" value="${hidNum}" />
           		<input   id="hidTotal" name="hidTotal" type="hidden"  value="${totalNum}"  />
            </c:if>
            <c:if test="${specialList==null}">
           		<input  id="hidNum" name="hidNum" type="hidden" value="1" />
           		<input  id="hidTotal" name="hidTotal" type="hidden"  value="0" />
            </c:if>
	     </table>				
		
		<table id="tb5">
			<tr id="trAddAfter">
			             <td >日期</td>
				         <td >考勤时间</td>
				         <td>操作</td>
	        </tr>
        	<c:forEach items='${specialList}' var="item"  >		
        			<tr id="tr${item.i}_1"><td id="specialTableID${item.i}">${item.speicalDate}</td>
		        	<td id="specialTableContent${item.i}">${item.specialShiftTime}</td>
		        	<input type='hidden' name= "specialTablehidden${item.i}" id= "specialTablehidden${item.i}" value="${item.speicalDate},${item.shiftID}"/>
        			<td ><a href='javascript:;' onclick="addSpecialButton(2,${item.i},'specialTablehidden${item.i}');">[编辑]</a><a href='javascript:;' onclick="removeTr(${item.i});">[删除]</a></td>
        	</c:forEach>
        </table>
	   
	   <div >
                  <div class="col-md-offset-2 col-md-10">
                      <button type="button" class="btn btn-default a-submit" onclick="attendanceRecordSave()">保存
                      </button>
                      &nbsp;
                      <button type="button" class="btn btn-default a-submit" onclick="history.back();">返回</button>
                  </div>
         </div>
         </br>
         </br>
         </br>
	 </div>
	</form>
	</div>		

	
</section>
<!-- end of main -->
</div>

</body>

<script>

    //特殊日期添加  
   function addSpecialButton(flag1,flag2,specialNum) { 
    	
    	if(specialNum!=""){
    		var arraySpecial = document.getElementById(specialNum).value.split(',');
    	}
    	var dialog = bootbox.dialog({  
           message: '<p class="text-center"><i class="fa fa-spin fa-spinner"></i>正在加载...</p>',  
           size: 'small',  
           closeButton: false  
       });  
       var html = '<div class="panel panel-default" style="max-height:500px;overflow-y:scroll;"> <div class="form-group"></br><label class="control-label col-md-2" for="pickerStartTime"><span style="color:red;"> * </span>'  
       	html += '日期</label>'  
       	html += ' <div class="col-sm-8">'  
       	html +=  '<div id="pickerStartTime" class="input-group date">'  
       	html +=  '<input type="text" id="specialDate" name="specialDate" onClick="WdatePicker()" '   
       	if(specialNum!=""){
       		html +=  ' value="'+arraySpecial[0]+'"'
       	}
       	html +=  ' readonly style="background-color:white;cursor:default;"'  
       	html +=  ' class="Wdate">'
     	html +=  ' </div>'
     	html +=  ' </div>'
     	html +=  ' </div></br><table class="table table-hover" style="width:100%;"><tr><td >班次名称</td><td>考勤时间</td></tr>';
       $.ajax({  
           url: "${tenantPrefix}/rs/auth/shift-list",  
           type: "POST",  
           timeout: 10000,  
           success: function (data) {  
               dialog.modal('hide');  
                   if (data == undefined || data == null || data == "" || data.length < 1)  
                       html += '<tr><td colspan="2">噢没有可选班次呢</td></tr>'  
                   else {  
                       if (data.length > 0) {  
                   			for (var i = 0; i < data.length; i++) {  
	                       		if(data[i].id!=null && data[i].id!="" && data[i].shiftName!=null&&data[i].delStatus==0 ){  
	                   			var selected="";
	                   			
	                   			if(specialNum!=""){
	                   				if(arraySpecial[1]==data[i].id)selected="checked";
	                   			}
	                   			
                       			var startEndTime
                       			if(data[i].startTime==null&&data[i].endTime==null){
                       				startEndTime="";
                       			}else{
                       				startEndTime = data[i].startTime + ' - ' +  data[i].endTime
                       			}
                       			
	                       		html += '<tr><td > <input id="shiftid'+i+'" '+selected+' ShiftTime="'+startEndTime+'" type="radio" value=' + data[i].id + ' name="changeShiftId" >' + data[i].shiftName + '</td><td>' +  startEndTime + '</td></tr>'  
	                       	}  
                       	} 
                   		}  
                   }  
         		html += "</table></div>";  
               addSpecialDialog(html,flag1,flag2);  
           },  
           error: function (XMLHttpRequest, textStatus, errorThrown) {  
               alert("请求超时")  
           },  
           complete: function (xh, status) {  
               dialog.modal('hide');  
               if (status == "timeout")  
                   bootbox.alert("请求超时");  
           }  
       });  
    }  

    	var addSpecialDialog = function (show,flag1,flag2) {  
			bootbox.dialog({  
    			title: '日期请选择工作日',  
    	        message: show,  
  				buttons: {  
    	            ok: {  
    	                label: "确定",  
    		            callback: function(){  
    		            	
    		            	
    		            		var num = parseInt($("#hidNum").val());
    		            		var total = parseInt($("#hidTotal").val());
    		            		var iptSelect=$("input[name='changeShiftId']:checked");  
    		            		var iptSelectValue=iptSelect.val(); //选中的班次id
    		            		var tdContent =iptSelect.attr("ShiftTime");//考勤时间
    		            		var specialTime =document.getElementById("specialDate").value ;  //特殊日期
    		            		
    		            		if(specialTime==''||iptSelect.val()==undefined){
    		            			alert('请选择时间和班次');
    		            			return false;
    		            		}
    		            		
    		            		if(flag1==1){
	    		            		//动态添加表格
	    		            		var html = "<tr id='tr" + num + "_1'><td id=specialTableID"+num+">"+specialTime+"</td>";
	        		                html = html + "<td id=specialTableContent"+num+">"+tdContent+"</td>";
	        		                html = html + "<input  type='hidden' name= 'specialTablehidden"+num+"' id= 'specialTablehidden"+num+"'/>";
	        		                html = html + "<td class='f_r_td'><a href='javascript:;' onclick='addSpecialButton(2," + num + ",&quot;specialTablehidden"+num+"&quot;);'>[编辑]</a><a href='javascript:;' onclick='removeTr(" + num + ");'>[删除]</a></td>";
	        		                $(html).insertAfter("#trAddAfter");
	        		               //隐藏域记录选中的特殊日期和班次id
	        		                document.getElementById("specialTablehidden"+num).value =specialTime+","+iptSelectValue;
	        		                $("#hidNum").val(num + 1);
	        		                $("#hidTotal").val(total + 1);
    		            		}
    		            		if(flag1==2){//编辑这条记录，不用新加表格
    		            			document.getElementById("specialTableID"+flag2).innerHTML =specialTime;
    		            			document.getElementById("specialTableContent"+flag2).innerHTML = tdContent;
    		            			document.getElementById("specialTablehidden"+flag2).value =specialTime+","+iptSelectValue;
    		            		}
    		            	}  
    	    			},  
    	    		  cancel: {  
    					label: '取消',  
    				}  
    	        }  
    	    });  
    	}   

    	//移除特殊日期的添加列
    	function removeTr(num) {
    	    var total = parseInt($("#hidTotal").val());      
    	    var trid = "#tr" + num + "_1";
    	    $(trid).remove();
    	    $("#hidTotal").val(total - 1);
    	   
    	}
	
    	//当全选按钮，选中时，所有复选框被选中，当全选按钮不被选中时，所有的也不被选中
    	function funSelAll(){
  			if(document.getElementsByName("selAll")[0].checked==true){
	    		$('#selOne1,#selOne2,#selOne3,#selOne4,#selOne5,#selOne6,#selOne7').prop('checked',true);
	    	}else{
	    		$('#selOne1,#selOne2,#selOne3,#selOne4,#selOne5,#selOne6,#selOne7').prop('checked',false);
			}
    	}
		
    	//var select_nodes=[];
  </script>  

</html>

 