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
<style>
/* #left_table1 tr th{width:100px}
#left_table1 tr td{width:100px} */
#left_table1 tr{width:100%;}
#left_table2 tr{width:100%;}
</style>
<script type="text/javascript" src="${cdnPrefix}/bootbox/bootbox.min1.js"></script>
<script type="text/javascript" src="${cdnPrefix}/worktask/worktask.js"></script>
<%-- <script type="text/javascript" src="${cdnPrefix}/table/jquery.sdrowspan.js"></script> --%>
<script type="text/javascript" src="${cdnPrefix}/popwindialog/popwin.js"></script>
<script type="text/javascript" src="${cdnPrefix}/bootbox/bootbox.min1.js"></script>
<script type="text/javascript">
		var config = {
		    params: {
		        'partyStructTypeId': '${partyStructTypeId}',
		        'partyEntityId': '${partyEntityId}',
		        'name': '${name}',
		        'beginDate': '${beginDate}',
                'endDate': '${endDate}'
		    },
			selectedItemClass: 'selectedItem',
			gridFormId: 'orgGridForm',
			exportUrl: 'org-export.do'
		};

		var table;

		$(function() {
			//设置段时间
            var sectionJson = [{"begin": "#pickerStartTime", "end": "#pickerEndTime"},
                {"begin": "#pickerPublicStartTime", "end": "#pickerPublicEndTime"}];
            fnSectionPickerTime(sectionJson);
		});
		
		function exportExcel(){
			if(partyId=="0"){
				alert("请先选择左侧树形节点！");
				return;
			}
			document.getElementById("attendanceForm").action = "${tenantPrefix}/PartyExcelController/exportAttendanceStatistics.do";
		}
		
		var partyId="${partyEntityId}";
		function search(){
			if(partyId=="0"){
				alert("请先选择左侧树形节点！");
				return;
			}
			document.getElementById("attendanceForm").action = "attendance-statistics-list-i.do";
		}
    </script>
    
      <!-- 异步上传 -->
	<script type="text/javascript" src="${cdnPrefix}/jquery-file-upload/jquery.ajaxfileupload.js"></script>
    <script type="text/javascript">
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
							value="${partyStructTypeId}"> <input id="partyEntityId" type="hidden"
							name="partyEntityId" value="${partyEntityId}"> 
                          
                          <label for="charege-info_name"><spring:message
                            code='charege-info.charege-info.list.search.publishtime' text='时间'/>:</label>
                    <div id="pickerPublicStartTime" class="input-group  datepickerbegin date">
                        <input style="width:160px;" type="text" name="beginDate"
                               value="${beginDate}" class="form-control required valid" readonly>
                        <span class="input-group-addon">
					<i class="glyphicon glyphicon-calendar"></i>
				</span>
                    </div>
                    至
                    <div id="pickerPublicEndTime" class="input-group  datepickerend date">
                        <input style="width:160px;" type="text" name="endDate"
                               value="${endDate}" class="form-control required valid" readonly>
                        <span class="input-group-addon">
					<i class="glyphicon glyphicon-calendar"></i>
				</span>
                    </div>
                         
						<label for="org_name"><spring:message
						code='org.org.list.search.name' text='姓名' />:</label> 
						<input type="text" id="name" name="name" value="${name}" class="form-control">
						<button class="btn btn-default a-search" onclick="search()">查询</button>
						&nbsp;
						<button class="btn btn-default a-search" type="submit" onclick="exportExcel()">导出</button>
					</form>
				</div>
			</div>

			<form id="orgGridForm" name="orgGridForm" method='post'
				action="org-remove.do" class="m-form-blank">
				

				<div id="div-list-panel" class="panel panel-default">
					<div class="panel-heading">
						<i class="glyphicon glyphicon-list"></i>
						<spring:message code="scope-info.scope-info.list.title" text="列表" />
					</div>
					
					<div id="container-fluid" class="container-fluid" style="padding:0;height:auto;">
					  <div id="left_div" class="col-md-3" style="width:100%;">
					    <div id="left_div1" style="height:500px;overflow:auto;padding-top:35px;">
					     <table id="left_table1" class="table table-bordered" style="border-collapse: collapse;border-spacing: 0;table-layout: fixed;position:absolute;z-index:999;top:0;left:0;width:99%;background-color:#ffffff;">
				        	<tr>
					          <th style="text-align:center;width:150px;" class="sorting" >机构</th>
							  <th style="text-align:center;width:200px;" class="sorting">工号</th>
							  <th style="text-align:center;" class="sorting" >姓名</th>
							  <th style="text-align:center;" class="sorting" >加班</th>
							  <th style="text-align:center;" class="sorting" >病假</th>
							  <th style="text-align:center;" class="sorting" >事假</th>
							  <th style="text-align:center;" class="sorting" >年假</th>
							  <th style="text-align:center;" class="sorting" >婚假</th>
							  <th style="text-align:center;" class="sorting" >产假</th>
							  <th style="text-align:center;" class="sorting" >丧假</th>
							  <th style="text-align:center;" class="sorting" >补休假</th>
							  <th style="text-align:center;" class="sorting" >倒休假</th>
							  <th style="text-align:center;" class="sorting" >销假</th>
							  <th style="text-align:center;" class="sorting" >其他</th>
				          	</tr>
				          </table>
					      <table id="left_table2" class="table table-bordered" style="border-collapse: collapse;border-spacing: 0;table-layout: fixed;">
				        	<!-- <tr>
					          <th style="text-align:center;" class="sorting" >机构</th>
							  <th style="text-align:center;width:200px" class="sorting">工号</th>
							  <th style="text-align:center;" class="sorting" >姓名</th>
							  <th style="text-align:center;" class="sorting" >加班</th>
							  <th style="text-align:center;" class="sorting" >病假</th>
							  <th style="text-align:center;" class="sorting" >事假</th>
							  <th style="text-align:center;" class="sorting" >年假</th>
							  <th style="text-align:center;" class="sorting" >婚假</th>
							  <th style="text-align:center;" class="sorting" >产假</th>
							  <th style="text-align:center;" class="sorting" >丧假</th>
							  <th style="text-align:center;" class="sorting" >补休假</th>
							  <th style="text-align:center;" class="sorting" >倒休假</th>
							  <th style="text-align:center;" class="sorting" >销假</th>
							  <th style="text-align:center;" class="sorting" >其他</th>
				          	</tr> -->
				          	<tbody id="dataBody">
					          	<c:forEach items="${resultData}" var="resultData">
									<tr style="height:30px">
							      		<td style="text-align:center;width:150px;">${resultData.orgName}</td>
										<td style="text-align:center;width:200px;">${resultData.userCode}</td>
										<td style="text-align:center;">${resultData.userName}</td>
										<td style="text-align:center;">${resultData.overTime}</td>
										<td style="text-align:center;">${resultData.sickLeave}</td>
										<td style="text-align:center;">${resultData.absenceLeave}</td>
										<td style="text-align:center;">${resultData.annualLeave}</td>
										<td style="text-align:center;">${resultData.maritalLeave}</td>
										<td style="text-align:center;">${resultData.maternityLeave}</td>
										<td style="text-align:center;">${resultData.funeralLeave}</td>
										<td style="text-align:center;">${resultData.breakOffLeave}</td>
										<td style="text-align:center;">${resultData.vacationsLeave}</td>
										<td style="text-align:center;">${resultData.backLeave}</td>
										<td style="text-align:center;">${resultData.other}</td>
									</tr>
								</c:forEach>
							</tbody>
					      </table>
					    </div>
					  </div>
					</div>
				</div>
			</form>
			<div class="m-spacer"></div>
		</section>
		<!-- end of main -->
	</div>
</body>
<script type="text/javascript">
	$(function(){  
		//alert(document.documentElement.scrollHeight);//900
		//alert(document.documentElement.offsetHeight);//756
		//alert(document.getElementById("dataBody").clientHeight);
		if(document.getElementById("dataBody").clientHeight<500){
			document.getElementById("left_table1").style.width="100%";
		}
	});  
</script>

</html>

