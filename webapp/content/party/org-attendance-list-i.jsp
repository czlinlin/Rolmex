<%@page contentType="text/html;charset=UTF-8"%>
<%@include file="/taglibs.jsp"%>
<%
	pageContext.setAttribute("currentHeader", "person");
%>
<%
	pageContext.setAttribute("currentMenuName", "人事管理");
%>
<%
	pageContext.setAttribute("currentMenu", "person");
%>
<!doctype html>
<html lang="en">
<head>
<%@include file="/common/meta.jsp"%>
<title><spring:message code="dev.org.list.title" text="麦联" /></title>
<%@include file="/common/s3.jsp"%>
<link type="text/css" rel="stylesheet" href="${cdnPrefix}/userpicker3/userpicker.css">
<script type="text/javascript" src="${cdnPrefix}/bootbox/bootbox.min1.js"></script>
<script type="text/javascript" src="${cdnPrefix}/worktask/worktask.js"></script>
<script type="text/javascript" src="${cdnPrefix}/table/jquery.sdrowspan.js"></script>
<script type="text/javascript" src="${cdnPrefix}/popwindialog/popwin.js"></script>
<script type="text/javascript">
		var dialogAsyncLoading=null;
		var dialog=null;
		var dialogLoading=function(msg){
			dialog = bootbox.dialog({
		            message: '<p class="text-center"><img alt="'+msg+'" src="${cdnPrefix}/mossle/img/loading.gif" style="width:24px;height:24px;"/><i class="fa fa-spin fa-spinner"></i>'+msg+'</p>',
		            size: 'large',
		            closeButton: false
		        });
		};
		
		var closeLoading=function(){
			if(dialog!=null)
				dialog.modal('hide');
		}
		var config = {
		    id: 'orgGrid',
		    pageNo: ${page.pageNo},
		    pageSize: ${page.pageSize},
		    totalCount: ${page.totalCount},
		    resultSize: ${page.resultSize},
		    pageCount: ${page.pageCount},
		    orderBy: '${page.orderBy == null ? "" : page.orderBy}',
		    asc: ${page.asc},
		    params: {
		        'partyStructTypeId': '${param.partyStructTypeId}',
		        'partyEntityId': '${param.partyEntityId}',
		        'name': '${param.name}'
		    },
			selectedItemClass: 'selectedItem',
			gridFormId: 'orgGridForm',
			exportUrl: 'org-export.do'
		};

		var table;

		$(function() {
			window.parent.dialogAsyncLoading.modal('hide');
			//closeLoading();
			merge('left_table2','0');
			merge('left_table2','1');
			merge('left_table2','2');
			//merge('left_table2','3');
			/* for (var i = 6; i<100; i=i+3) {
				merge('table',i);
			} */
			
			/* var step = 3;
			for(i=6;i<=1000;i+=step){
				alert(i);
				merge('table',i);
				
				
			}  */
			
			fillAtt();
			
			
			$("#table > tbody tr").each(function(){
				var td=$(this).find("td:eq(3)");  //2:是tdIdx,从0 开始 
				td.html("${month}");
			}); 
			
			fillLeave();
			
			window.parent.$.showMessage($('#m-success-tip-message').html(), {
                position: 'top',
                size: '50',
                fontSize: '20px'
            });
			
			table = new Table(config);
		    table.configPagination('.m-pagination');
		    table.configPageInfo('.m-page-info');
		    table.configPageSize('.m-page-size');
		});
		
		function merge(tableId,col){
        	var tr = document.getElementById(tableId);
        	for(var i=1; i<tr.rows.length; i++){                //表示数据内容的第二行
            	if(tr.rows[i].cells[col].innerHTML == tr.rows[i - 1].cells[col].innerHTML){//col代表列
                	t = i-1;
                	
	                while(tr.rows[i].cells[col].innerHTML == tr.rows[t].cells[col].innerHTML){
	                    tr.rows[i].cells[col].style.display="none";
	                    if(tr.rows[t].cells[col].rowSpan <= (i-t)){  
	                        tr.rows[t].cells[col].rowSpan +=1;      //设置前一行的rowspan+1
	                    }
	                    i++;
	                    if (i >= tr.rows.length) {
	                    	break;
	                    }
	                }
            	}               
        	} 	
		}
		
		function fillAtt() {
			var str = $("#attListStr").html();
			var jsonarray= $.parseJSON(str);
			$.each(jsonarray, function (i, n){
				
				$("#class_" + n.user_id + "_" + n.month + "_" + n.day + "_01").html(n.constraint_to_work);
			    $("#class_" + n.user_id + "_" + n.month + "_" + n.day + "_11").html(n.constraint_off_work);
			    
				$("#att_" + n.user_id + "_" + n.month + "_" + n.day + "_01").html(n.go_to_work);
			    $("#att_" + n.user_id + "_" + n.month + "_" + n.day + "_11").html(n.go_off_work);
			    //比较时间，并标识迟到早退
				if(n.go_to_work != "" && n.go_to_work != undefined && n.constraint_to_work != undefined  && n.constraint_to_work != ""){
					var go_to_work = "2018-01-01 "+n.go_to_work;
					var constraint_to_work = "2018-01-01 "+n.constraint_to_work;
				    if(CompareDate(go_to_work,constraint_to_work)){
				    	$("#att_" + n.user_id + "_" + n.month + "_" + n.day + "_01").attr("style","color:red");
				    	$("#class_" + n.user_id + "_" + n.month + "_" + n.day + "_01").attr("style","color:red");
				    }
				}else{
					$("#att_" + n.user_id + "_" + n.month + "_" + n.day + "_01").attr("style","min-width: 58px; min-height: 20px;color:red");
			    	$("#class_" + n.user_id + "_" + n.month + "_" + n.day + "_01").attr("style","min-width: 58px; min-height: 20px;color:red");
				} 
				if(n.go_off_work != "" && n.go_off_work != undefined && n.constraint_off_work != undefined && n.constraint_off_work != ""){
					var go_off_work = "2018-01-01 "+n.go_off_work;
					var constraint_off_work = "2018-01-01 "+n.constraint_off_work;
				    if(CompareDate(constraint_off_work,go_off_work)){
				    	$("#att_" + n.user_id + "_" + n.month + "_" + n.day + "_11").attr("style","color:red");
				    	$("#class_" + n.user_id + "_" + n.month + "_" + n.day + "_11").attr("style","color:red");
				    }
				}else{
					$("#att_" + n.user_id + "_" + n.month + "_" + n.day + "_11").attr("style","min-width: 58px; min-height: 20px;color:red");
			    	$("#class_" + n.user_id + "_" + n.month + "_" + n.day + "_11").attr("style","min-width: 58px; min-height: 20px;color:red");
				}
			});
			
			$(".1").attr("style","text-align: center;min-width: 55px;background-color:#eee;");
		}
		//ckx   时间的比较
		function CompareDate(d1,d2){
		  	return ((new Date(d1.replace(/-/g,"\/"))) > (new Date(d2.replace(/-/g,"\/"))));
		}
		function detail(customFormId){
			$.ajax({
				url:'${tenantPrefix}/party/attendance-detail.do',
				data:{customFormId:customFormId},
				dataType:'json',
				type:'post',
				success:function (detail){
					/* bootbox.alert("<div>申请时间："+detail.createTime+
										"<br/>申请人:"+detail.name+
										"<br/>请假时间:"+detail.startTime+"~"+detail.endTime+"  共"+detail.totalTime+"天"+
										"<br/>请假事由:"+detail.content+
									"</div>"); */
									//bootbox.alert(detail.detailHtml);
					openLeaveInfo(detail.url,detail.REF)
				}
			})
		}
		function openLeaveInfo(url,ref){//弹出考勤详情页
			popWin.scrolling="auto";
			popWin.showWin("950"
	    			,"600"
	    			,""
	    			,"${tenantPrefix}/bpm/workspace-viewHistoryFrom.do?processInstanceId="+ref+"&url="+url);
        	
        }
		function fillLeave() {
			var currentMonth = $("#currentMonthId").val();
			var leaveStr = $("#leaveList").html();
			var jsonarrayleave= $.parseJSON(leaveStr);
			console.log(jsonarrayleave);
			$.each(jsonarrayleave, function (i, m){
				var customFormId = m.customFormId;
				if(customFormId != ""){
					$("#xiu_" + m.userId + "_" + m.month + "_" + m.day + "_01").append("<a href='javascript:' onclick='detail(\""+m.customFormId+"\")'>"+m.type+"</a><br/>");
				}else{
					if(m.quitMonth == m.month){
						$("#xiu_" + m.userId + "_" + m.month + "_" + m.day + "_01").html("<font color='red'>离职</font>");
					}
				}
			})
		}
		
		var partyId="${partyEntityId}";
		function search(){
			if(partyId=="0"){
				alert("请先选择左侧树形节点！");
				return;
			}
			document.getElementById("attendanceForm").action = "attendance-list-i.do";
		}
    </script>
    
      <!-- 异步上传 -->
	<script type="text/javascript" src="${cdnPrefix}/jquery-file-upload/jquery.ajaxfileupload.js"></script>
    <script type="text/javascript">
		//上传图片
		var fnUploadExcel = function(){
			$("#partyStructTypeIdStr").val($("#partyStructTypeId").val());
	    	$("#partyEntityIdStr").val($("#partyEntityId").val());
	    	$("#yearStr").val($('#year option:selected').val());
	    	$("#monthStr").val($('#month option:selected').val());
	    	$("#nameStr").val($('#name').val());
			if($("#excelFile").val()==""){
	    		alert('请选择导入的EXCEL文件');
	    		return false;
	    	}
	    	if($("#excelFile").val().lastIndexOf(".xls") < 0 || $("#excelFile").val().lastIndexOf(".xlsx") < 0){
	    		alert('只能上传EXCEL文件');
	    		return false;
	    	}
	    	
	    	
	    	window.dialogLoading("正在进行导入，请勿刷新页面");
	    	$("#import").attr("action","${tenantPrefix}/PartyExcelController/attendance-excel-import.do");
	    	return true;
		}
		//上传方法
		function upload(){
			
			$.ajaxFileUpload({
				url: '${tenantPrefix}/PartyExcelController/attendance-excel-import.do',
				type : 'post',
				dataType : 'json',
				// 与input的file类型的域id绑定
				fileElementId : 'excelFile',
				// 是否使用安全的方式
				secureuri : false,
				
				success :function(data){
					alert(data);
					if(data == "true"){
						window.closeLoading();
						alert('导入成功');
						$("excelFile").val("");
						document.getElementById("attendanceForm").action = "attendance-list-i.do";
						document.getElementById("attendanceForm").submit();
					}
				},
				error: function (XMLHttpRequest, textStatus, errorThrown) {
	                    alert("[" + XMLHttpRequest.status + "]error，请求失败")
	            },
                complete: function (xh, status) {
                    if (status == "timeout"){
                    	alert('请求超时');
                    	return false;
                	}  
                }
				});
		}
		
		//导出时数据转json字符串
		function tableToJSON() {
			if(partyId=="0"){
				alert("请先选择左侧树形节点！");
				return;
			}
			var arrL2 = [];
			var il2 = 0;
			var rl2 = 1;
			$("#left_table2 tr").each(function(){
				  //遍历td
				var childArrl2 = {};
				il2 =0;
				$(this).find("td").each(function(){
					childArrl2[rl2 + "_" + il2] = $(this).text();
					
					il2++;
					//alert(" text="+$(this).text());
				});
				rl2++;
				arrL2.push(childArrl2);
			});
			
			
			var arrR2 = [];
			var ir2 = 4;
			var rr2 = 1;
			$("#right_table2 tr").each(function(){
				  //遍历td
				var childArrR2 = {};
				ir2 =4;
				$(this).find("td").each(function(){
					childArrR2[rr2 + "_" + ir2] = $(this).text();
					
					ir2++;
					//alert(" text="+$(this).text());
				});
				rr2++;
				arrR2.push(childArrR2);
			});
			//alert(JSON.stringify(arrL2));
			//alert(JSON.stringify(arrR2))
			var all = [{}];
			
		   for (var i = 0; i < arrL2.length; i++) {
			  
			  var object = $.extend({}, arrL2[i], arrR2[i]);
			  all.push(object);
			}  
				
			var arrStr = JSON.stringify(all);
			$("#arrStr").val(arrStr);
			var year =  $("#year option:selected").text();
			var month =  $("#month option:selected").text();
			document.getElementById("attendanceForm").action = "attendance-export-i.do?year="+year+"&month="+month;
    	}
    </script>
    <style>
    	.table{border-collapse: collapse;}
    	.table td{border:1px solid #ddd;}
    	.table th{border:1px solid #ddd;}
    	.table > tbody > tr > td{vertical-align: middle;}
    	.table > thead > tr > th{border-bottom:none;}
    	
    	th {
            white-space: nowrap
        }
        td{
        	white-space: nowrap
        }
        .table-list td{min-width:55px;}
    </style>
        <style>
    	#tb1 td {
        border: 1px solid #BBB
    }

    .f_td {
        width: 120px;
        font-size: 12px;
        white-space: nowrap
    }

    .f_r_td {
        width: 130px;
        text-align: left;
    }


    #tb1 tr td textarea {
        border: navajowhite;
    }

    #tb1 tr td {
        text-align: center;
        line-height: 28px;
        height:28px;
    }

    #tb1 tr td.f_td.f_right {
        text-align: right;
    }

    #tb1 tr td input.input_width {
        width: auto;
    }
    #tb1 td{text-align:left;}
    </style>
    
    <style type="text/css">
		#left_div{
		   /* width:500px;
		     float: left; */
		    padding:0;
		}
		#left_div1{
		    width: 100%;
		}
		#left_div2{
		    margin-top:-20px;
		    width: 100%;
		    height: 442px;
		    overflow: hidden;
		}
		#left_table1 th{
		   /*  background: #E9F8FF; */
		    text-align:center;
		}
		#left_table2 th{
		    text-align:center;
		}
		
		#right_div{
		    /* float: left; */
		    padding:0;
		}
		#right_div1{
		    width: 100%;
		    overflow: hidden;
		}
		#right_divx{
		    /* width: 900px; */
		}
		#right_div2{
		    margin-top:-20px;
		    width:100%;
		    height:460px;
		    overflow: auto;
		}
		#right_table1{
		    /* width: 880px; */
		}
		#right_table2{
		    /**width和max-width一起写，手机浏览器打开也能固定长度**/
		    width: 880px;
		    max-width: 880px;
		    white-space:nowrap;
		}
		#right_table1 th{
		    /* background: #E9F8FF; */
		    text-align:center;
		    /* width:10%; */
		}
		#right_table2 td{
		    /* width:10%; */
		    text-align:center;
		}
	</style>
</head>
<body>
	<div class="row-fluid" >
		<c:if test="${not empty flashMessages}">
			<div id="m-success-tip-message" style="display: none;">
				<ul>
					<c:forEach items="${flashMessages}" var="item">
						<c:if test="${item != ''}">
							<li>${item}</li>
						</c:if>
					</c:forEach>
				</ul>
			</div>
		</c:if>
		
		<!-- <div class="container-fluid">
		  <div id="left_div">
		    <div id="left_div1">
		      <table id="left_table1" class="table table-bordered">
		        <tr>
		          <th>我不会动</th>
		        </tr>
		      </table>
		    </div>
		    <div id="left_div2">
		      <table id="left_table2" class="table table-bordered">
		      </table>
		    </div>
		  </div>
		  <div id="right_div">
		    <div id="right_div1">
		      <div id="right_divx">
		        <table id="right_table1" class="table table-bordered">
		          <tr>
		            <th>我是表头</th>
		            <th>我是表头</th>
		            <th>我是表头</th>
		            <th>我是表头</th>
		            <th>我是表头</th>
		            <th>我是表头</th>
		            <th>我是表头</th>
		            <th>我是表头</th>
		            <th>我是表头</th>
		            <th>我是表头</th>
		          </tr>
		        </table>
		      </div>
		    </div>
		    <div id="right_div2">
		      <table id="right_table2" class="table table-bordered">
		      </table>
		    </div>
		  </div>
		</div> -->

		<!-- start of main -->
		<section id="m-main" class="col-md-12" style="padding-top:66px;">
			
			<div class="panel panel-default">
				<div class="panel-heading">
					<i class="glyphicon glyphicon-list"></i> 查询
					<div class="pull-right ctrl">
						<a class="btn btn-default btn-xs"><i id="orgSearchIcon"
							class="glyphicon glyphicon-chevron-up"></i></a>
					</div>
				</div>
				<div class="panel-body">
					<form id="attendanceForm" name="attendanceForm" method="post" action="" class="form-inline" enctype="multipart/form-data">
						<textarea id="arrStr" name="arrStr" style="display:none" rows="" cols=""></textarea>
						<input id="partyStructTypeId" type="hidden" name="partyStructTypeId"
							value="${param.partyStructTypeId}"> <input id="partyEntityId" type="hidden"
							name="partyEntityId" value="${param.partyEntityId}"> <label
							for="org_name"><spring:message
								code='org.org.list.search.name' text='年份' />:</label> <select id="year" class="form-control" name="year">
							<c:forEach var="allYear" begin="${year-5}" end="${year+5}">
								<c:if test="${allYear == year}">
									<option value="${allYear}" selected>${allYear}</option>
								</c:if>
								<c:if test="${allYear != year}">
									<option value="${allYear}">${allYear}</option>
								</c:if>
							</c:forEach>
							
						</select> <label for="org_name"><spring:message
								code='org.org.list.search.name' text='月份' />:</label> <select id="month" class="form-control" name="month">
							<c:forEach var="allMonth" begin="1" end="12">
								<c:if test="${allMonth == month}">
									<option value="${allMonth}" selected>${allMonth}</option>
								</c:if>
								<c:if test="${allMonth != month}">
									<option value="${allMonth}">${allMonth}</option>
								</c:if>
							</c:forEach>
						</select> 
						<label for="org_name"><spring:message
						code='org.org.list.search.name' text='姓名' />:</label> 
						<input type="text" id="name" name="name" value="${param.name}" class="form-control">
						<button class="btn btn-default a-search" onclick="search()">查询</button>
						&nbsp;
						<button class="btn btn-default a-search" type="submit" onclick="tableToJSON()">导出</button>
					</form>
				</div>
			</div>

			<form id="orgGridForm" name="orgGridForm" method='post'
				action="org-remove.do" class="m-form-blank">
				
				<p id="attListStr" style="display:none">${attListStr}</p>
				<p id="leaveList" style="display:none">${leaveListStr}</p>
				
				<div id="div-list-panel" class="panel panel-default">
					<div class="panel-heading">
						<i class="glyphicon glyphicon-list"></i>
						<spring:message code="scope-info.scope-info.list.title" text="列表" />
					</div>
					
					<div id="container-fluid" class="container-fluid" style="padding:0;height:500px;">
						<c:if test="${partyEntityId!='0'}">
							<div id="left_div" class="col-md-3" style="width:500px;">
					    <div id="left_div1">
					      <table id="left_table1" class="table table-bordered">
				        	<tr>
					          <th style="text-align:center;" class="sorting" style="width:24.9%;">机构</th>
							  <th style="text-align:center;" class="sorting" style="width:24.9%;">工号</th>
							  <th style="text-align:center;" class="sorting" style="width:24.9%;">姓名</th>
							  <th style="text-align:center;" class="sorting" style="width:24.9%;">月份</th>
				          	</tr>
					      </table>
					    </div>
					    <div id="left_div2">
					      <table id="left_table2" class="table table-bordered">
					      	<c:forEach items="${userList}" var="user">
								<tr style="height:30px">
						      		<td style="text-align:center;width:24.9%;">${user.department_name}</td>
									<td style="text-align:center;width:24.9%;">${user.employee_no}</td>
									<c:if test="${openOtherNameStatus == 1}"><!-- 别名开启  -->
										<td style="text-align:center;width:24.9%;" >${user.real_name}</td>
									</c:if>
									<c:if test="${openOtherNameStatus == 0}">
										<td style="text-align:center;width:24.9%;" >${user.full_name}</td>
									</c:if> 
									<%-- <c:if test="${openOtherNameStatus == 1 }">
										<c:if test="${user.isQuitFlag == 1}">
											<td style="text-align:center;width:24.9%;color:red;" >${user.real_name}</td>
										</c:if>
										<c:if test="${user.isQuitFlag == 0}">
											<td style="text-align:center;width:24.9%;" >${user.real_name}</td>
										</c:if>
									</c:if>
									<c:if test="${openOtherNameStatus == 0}">
										<c:if test="${user.isQuitFlag == 1}">
											<td style="text-align:center;width:24.9%;color:red;" >${user.full_name}</td>
										</c:if>
										<c:if test="${user.isQuitFlag == 0}">
											<td style="text-align:center;width:24.9%;" >${user.full_name}</td>
										</c:if>
									</c:if> --%>
									<td style="text-align:center;width:24.9%;" >${month}</td>
								</tr> 
								<tr style="height:30px">
						      		<td style="text-align:center;width:24.9%;">${user.department_name}</td>
									<td style="text-align:center;width:24.9%;">${user.employee_no}</td>
									<c:if test="${openOtherNameStatus == 1}"><!-- 别名开启  -->
										<td style="text-align:center;width:24.9%;" >${user.real_name}</td>
									</c:if>
									<c:if test="${openOtherNameStatus == 0}">
										<td style="text-align:center;width:24.9%;" >${user.full_name}</td>
									</c:if>
									
									<%-- <c:if test="${openOtherNameStatus == 1 }"><!-- 别名开启  -->
										<c:if test="${user.isQuitFlag == 1}">
											<td style="text-align:center;width:24.9%;color:red;" >${user.real_name}</td>
										</c:if>
										<c:if test="${user.isQuitFlag == 0}">
											<td style="text-align:center;width:24.9%;" >${user.real_name}</td>
										</c:if>
									</c:if>
									<c:if test="${openOtherNameStatus == 0}">
										<c:if test="${user.isQuitFlag == 1}">
											<td style="text-align:center;width:24.9%;color:red;" >${user.full_name}</td>
										</c:if>
										<c:if test="${user.isQuitFlag == 0}">
											<td style="text-align:center;width:24.9%;" >${user.full_name}</td>
										</c:if>
									</c:if> --%>
									
									 
									<td style="text-align:center;width:24.9%;" >${month}</td>
								</tr>
							</c:forEach>
					      </table>
					    </div>
					  </div>
					  <div id="right_div"  class="col-md-9">
					    <div id="right_div1">
					      <div id="right_divx">
					        <table id="right_table1" class="table table-bordered">
					          <tr>
					            <%-- <c:forEach var="v" begin="1" end="${maxDate}">
									<th style="text-align:center;" class="sorting"><div>${v}</div></th>
								</c:forEach> --%>
								<c:forEach items="${dateList }" var="days">
									<%-- <c:if test="${days.isWeekend == 1}"> color="red"--%>
										<th style="text-align:center;" class="sorting"><div ><font >${days.day}&nbsp(${days.week})</font></div></th>
									<%-- </c:if> --%>
									<%-- <c:if test="${days.isWeekend == 0}">
										<th style="text-align:center;" class="sorting"><div >${days.day}</div></th>
									</c:if> --%>
								</c:forEach>
					          </tr>
					        </table>
					      </div>
					    </div>
					    <div id="right_div2">
					      <table id="right_table2" class="table table-bordered">
						      <c:forEach items="${userList}" var="user">
						      	<tr  style="height:30px">
						      		<%-- <c:forEach var="v" begin="1" end="${maxDate}">
										<c:if test="${v<10}">
											<td id="class_${user.id}_${month}_0${v}_0" style="text-align:center;min-width:55px;"><div id="class_${user.id}_${month}_0${v}_01"></div></td>
											<td id="att_${user.id}_${month}_0${v}_0" style="text-align:center;min-width:55px;"><div id="att_${user.id}_${month}_0${v}_01"></div></td>
											<td id="xiu_${user.id}_${month}_0${v}_0" style="text-align:center;min-width:55px;" rowspan="2"><div id="xiu_${user.id}_${month}_0${v}_01"></div></td>
										</c:if>
										<c:if test="${v>=10}">
											<td id="class_${user.id}_${month}_${v}_0" style="text-align:center;min-width:55px;"><div id="class_${user.id}_${month}_${v}_01"></div></td>
											<td id="att_${user.id}_${month}_${v}_0" style="text-align:center;min-width:55px;"><div id="att_${user.id}_${month}_${v}_01"></div></td>
											<td id="xiu_${user.id}_${month}_${v}_0" style="text-align:center;min-width:55px;"rowspan="2"><div id="xiu_${user.id}_${month}_${v}_01"></div></td>
										</c:if>
									</c:forEach> --%>
									<c:forEach items="${dateList }" var="days">
										<c:if test="${days.day<10}">
											<td id="class_${user.id}_${month}_0${days.day}_0" class="${days.isWeekend }" style="text-align:center;min-width:55px;"><div id="class_${user.id}_${month}_0${days.day}_01"></div></td>
											<td id="att_${user.id}_${month}_0${days.day}_0" class="${days.isWeekend }" style="text-align:center;min-width:55px;"><div id="att_${user.id}_${month}_0${days.day}_01"></div></td>
											<td id="xiu_${user.id}_${month}_0${days.day}_0" class="${days.isWeekend }" style="text-align:center;min-width:55px;" rowspan="2"><div id="xiu_${user.id}_${month}_0${days.day}_01"></div></td>
										</c:if>
										<c:if test="${days.day>=10}">
											<td id="class_${user.id}_${month}_${days.day}_0" class="${days.isWeekend }" style="text-align:center;min-width:55px;"><div id="class_${user.id}_${month}_${days.day}_01"></div></td>
											<td id="att_${user.id}_${month}_${days.day}_0" class="${days.isWeekend }" style="text-align:center;min-width:55px;"><div id="att_${user.id}_${month}_${days.day}_01"></div></td>
											<td id="xiu_${user.id}_${month}_${days.day}_0" class="${days.isWeekend }" style="text-align:center;min-width:55px;"rowspan="2"><div id="xiu_${user.id}_${month}_${days.day}_01"></div></td>
										</c:if>
									</c:forEach>
						      	</tr>
						      	<tr  style="height:30px">
							      	<%-- <c:forEach var="v" begin="1" end="${maxDate}">
										<c:if test="${v<10}">
											<td id="class_${user.id}_${month}_0${v}_1" style="text-align:center;min-width:55px;"><div id="class_${user.id}_${month}_0${v}_11"></div></td>
											<td id="att_${user.id}_${month}_0${v}_1" style="text-align:center;min-width:55px;"><div id="att_${user.id}_${month}_0${v}_11"></div></td>
										</c:if>
										<c:if test="${v>=10}">
											<td id="class_${user.id}_${month}_${v}_1" style="text-align:center;min-width:55px;"><div id="class_${user.id}_${month}_${v}_11"></div></td>
											<td id="att_${user.id}_${month}_${v}_1" style="text-align:center;min-width:55px;"><div id="att_${user.id}_${month}_${v}_11"></div></td>
										</c:if>
									</c:forEach> --%>
									<c:forEach items="${dateList }" var="days">
										<c:if test="${days.day<10}">
												<td id="class_${user.id}_${month}_0${days.day}_1" class="${days.isWeekend }" style="text-align:center;min-width:55px;"><div id="class_${user.id}_${month}_0${days.day}_11"></div></td>
												<td id="att_${user.id}_${month}_0${days.day}_1" class="${days.isWeekend }" style="text-align:center;min-width:55px;"><div id="att_${user.id}_${month}_0${days.day}_11"></div></td>
											</c:if>
											<c:if test="${days.day>=10}">
												<td id="class_${user.id}_${month}_${days.day}_1" class="${days.isWeekend }" style="text-align:center;min-width:55px;"><div id="class_${user.id}_${month}_${days.day}_11"></div></td>
												<td id="att_${user.id}_${month}_${days.day}_1" class="${days.isWeekend }" style="text-align:center;min-width:55px;"><div id="att_${user.id}_${month}_${days.day}_11"></div></td>
										</c:if>
									</c:forEach>
								</tr>
							</c:forEach>
					      </table>
					    </div>
					  </div>
					</c:if>
					</div>	
					
					<%-- <div style="overflow:scroll;height:500px;">
						<table id="table" border="0" style="border-color:#ddd;" cellspacing="0" cellpadding="0" class="table table-list table-hover">
							<thead>
								<tr>
									<th style="text-align:center;" class="sorting" width="60px;">机构</th>
									<th style="text-align:center;" class="sorting" width="60px;">工号</th>
									<th style="text-align:center;" class="sorting" width="60px;">姓名</th>
									<th style="text-align:center;" class="sorting" width="60px;">月份</th>
									<c:forEach var="v" begin="1" end="${maxDate}">
										<th style="text-align:center;" colspan="3" class="sorting">${v}</th>
									</c:forEach>
								</tr>
							</thead>
							<tbody id="tbody">
							<c:forEach items="${userList}" var="user">
								<tr style="height:30px">
									<td style="text-align:center;width:90px;">${user.department_name}</td>
									<td style="text-align:center;width:90px;">${user.employee_no}</td>
									<td style="text-align:center;width:60px;" >${user.full_name}</td>
									<td style="text-align:center;width:60px;" >${user.id}_${month}</td>
									<c:forEach var="v" begin="1" end="${maxDate}">
										<c:if test="${v<10}">
											<td id="class_${user.id}_${month}_0${v}_0" style="text-align:center;width:55px;"></td>
											<td id="att_${user.id}_${month}_0${v}_0" style="text-align:center;width:55px;"></td>
											<td id="xiu_${user.id}_${month}_0${v}_0" style="text-align:center;width:55px;" rowspan="2"></td>
										</c:if>
										<c:if test="${v>=10}">
											<td id="class_${user.id}_${month}_${v}_0" style="text-align:center;width:55px;"></td>
											<td id="att_${user.id}_${month}_${v}_0" style="text-align:center;width:55px;"></td>
											<td id="xiu_${user.id}_${month}_${v}_0" style="text-align:center;width:55px;"rowspan="2"></td>
										</c:if>
									</c:forEach>
								</tr>
								<tr style="height:30px">
									<td style="text-align:center;width:90px;">${user.department_name}</td>
									<td style="text-align:center;width:90px;">${user.employee_no}</td>
									<td style="text-align:center;width:60px;" >${user.full_name}</td>
									<td style="text-align:center;width:60px;">${user.id}_${month}</td>
									<c:forEach var="v" begin="1" end="${maxDate}">
										<c:if test="${v<10}">
											<td id="class_${user.id}_${month}_0${v}_1" style="text-align:center;width:55px;"></td>
											<td id="att_${user.id}_${month}_0${v}_1" style="text-align:center;width:55px;"></td>
											
										</c:if>
										<c:if test="${v>=10}">
											<td id="class_${user.id}_${month}_${v}_1" style="text-align:center;width:55px;"></td>
											<td id="att_${user.id}_${month}_${v}_1" style="text-align:center;width:55px;"></td>
											
										</c:if>
									</c:forEach>
								</tr>
								</c:forEach>
							</tbody>
						</table>
					</div> --%>
				</div>
			</form>
			<!-- <div>
				<div class="m-page-info pull-left">共100条记录 显示1到10条记录</div>
				<div class="btn-group m-pagination pull-right">
					<button class="btn btn-default">&lt;</button>
					<button class="btn btn-default">1</button>
					<button class="btn btn-default">&gt;</button>
				</div>
				<div class="clearfix"></div>
			</div> -->
			<div class="m-spacer"></div>
		</section>
		<!-- end of main -->
	</div>
	<script type="text/javascript">
		//固定和滚动
		var right_div2 = document.getElementById("right_div2");
		right_div2.onscroll = function(){
		    var right_div2_top = this.scrollTop;
		    var right_div2_left = this.scrollLeft;
		    document.getElementById("left_div2").scrollTop = right_div2_top;
		    document.getElementById("right_div1").scrollLeft = right_div2_left;
		}
		var initWidth=$("#div-list-panel").width();
		var left_width=$("#left_div").width();
		
		var right_width=""+(initWidth)-left_width+"px";
		document.getElementById("right_div1").style.width=right_width;
		document.getElementById("right_div2").style.width=right_width;
		//设置右边div宽度
		document.getElementById("right_div").style.width=right_width;
		setInterval(function() {
		    document.getElementById("right_div").style.width=right_width;  
		}, 0);
		
		var right_table2_width=$("#right_table2").width();
		var right_table2_height=$("#right_table2").width();
		document.getElementById("right_table1").style.width=right_table2_width;
		
		//设置左侧表头宽度
		var tr_first=$("#left_table2 tr")[0];
		var tr_first_tds=$(tr_first).find("td");
		//table宽度设置
		$.each($("#left_table1 tr th"),function(i,item){
			$(item).css("width",$(tr_first_tds[i]).width());
		});
		
		//设置右侧表头宽度
		$("#right_table2 tr td div").css({"min-width":58,"min-height":20});
		
		$("#right_divx").width($("#right_divx").width()+20)
		
		$("#right_divx table").width($("#right_divx").width());
		
		var tr_right_first=$("#right_table2 tr")[0];
		var tr_right_first_tds=$(tr_right_first).find("td");
		
        var userAgent = navigator.userAgent; //取得浏览器的userAgent字符串
        var isEdge=userAgent.indexOf("Edge")>-1;
        var isOpera = userAgent.indexOf("Opera") > -1; //判断是否Opera浏览器
        var isIE =userAgent.indexOf("MSIE") > -1; //判断是否IE浏览器
        //var isEdge = userAgent.indexOf("Windows NT 6.1; Trident/7.0;") > -1 && !isIE; //判断是否IE的Edge浏览器
        var isFF = userAgent.indexOf("Firefox") > -1; //判断是否Firefox浏览器
        var isSafari = userAgent.indexOf("Safari") > -1 && userAgent.indexOf("Chrome") == -1; //判断是否Safari浏览器
        var isChrome = userAgent.indexOf("Chrome") >  -1; //判断Chrome浏览器
        
        var reIE = new RegExp("MSIE (\\d+\\.\\d+);");
        reIE.test(userAgent);
        var ie_version= parseFloat(RegExp["$1"]);
 
		//table宽度设置
		var n=0;
		$.each($("#right_table1 tr th"),function(i,item){
			//合并三列，所以需要三列的宽度
			var td_ele_1=$(tr_right_first_tds[n]);
			var width_1=td_ele_1.width()+11;
			n++;
			var td_ele_2=$(tr_right_first_tds[n]);
			var width_2=td_ele_2.width()+11;
			n++;
			var td_ele_3=$(tr_right_first_tds[n]);
			var width_3=td_ele_3.width()+12;
			n++;
			var width_total=width_1+width_2+width_3;
			if(isChrome||ie_version==8){
				if(!isEdge)
					width_total+=3;
			}
				
			if(screen.width<1600)
				width_total-=1.6;
			
			//alert(width_total);
			
			$(item).find("div").css("width",width_total);
		});
		
		
		
		$(function(){
			$("#right_div2").width($("#right_div1").width()+20);
			$("#m-main").css({"padding-left":"0","padding-right":"0"});
			
			//table高度设置
			var tr_right_first_trs=$("#right_table2 tr");
			var m=0;
			$.each($("#left_table2 tr"),function(i,item){
				var td_height=$(tr_right_first_trs[m]).find("td:first").height();
				if(td_height>20)
					td_height+=16.5;
				$(item).find("td:last").css("height",td_height);
				m++;
			});
		})
		
	</script>
</body>
</html>

