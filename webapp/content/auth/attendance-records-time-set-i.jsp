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
        var config = {
            id: 'person-infoGrid',
            pageNo: ${page.pageNo},
            pageSize: ${page.pageSize},
            totalCount: ${page.totalCount},
            resultSize: ${page.resultSize},
            pageCount: ${page.pageCount},
            orderBy: '${page.orderBy == null ? "" : page.orderBy}',
            asc: ${page.asc},
            params: {
                'filter_LIKES_RECORDNAME': '${param.filter_LIKES_RECORDNAME}',
              
                'isSearch': '${isSearch}'
            },
            selectedItemClass: 'selectedItem',
            gridFormId: 'person-infoGridForm',
            exportUrl: 'person-info-export.do',
            resetUrl: "${tenantPrefix}/rs/user/person-info-reset",
            resetKeyUrl: "${tenantPrefix}/rs/user/person-info-resetkey"
        };

        var table;

        $(function () {
            table = new Table(config);
            table.configPagination('.m-pagination');
            table.configPageInfo('.m-page-info');
            table.configPageSize('.m-page-size');
            $("#attendanceManage").css({'background-color':'#EBEBEB'});
        });

        var isSubmit = false;
        // 查询
        function searchInfo() {
        	
        	$('#isSearch').val("1");
            $('#person-infoGridForm').attr('action', '${tenantPrefix}/auth/attendance-records-time-set-i.do?');
            $('#person-infoGridForm').submit();
        }

        
        
        /* function newAttendance() {
        	
			$('#attendanceRecordsTimeSetForm1').attr('action', '${tenantPrefix}/auth/new-attendance-records-list.do');
            $('#attendanceRecordsTimeSetForm1').submit();
        } */
        
        //删除考勤组
       		var deleteAttendanceDialog = function (id) {
				bootbox.dialog({
			        title: '删除考勤组',
			        message: '确定删除考勤组吗',
			        buttons: {
			            ok: {
			                label: "确定",
				            callback: function(){
									
				            	$('#attendanceRecordsTimeSetForm1').attr('action', '${tenantPrefix}/auth/delete-attendance-record.do?id='+id);
				                $('#attendanceRecordsTimeSetForm1').submit();

				                }
			    			},
			    		  cancel: {
							label: '取消',
						}
			        }
			    });
			} 
        
        
       	 //查看人员
       		var personList = function (id) {
       		
				var dialog = bootbox.dialog({
			        message: '<p class="text-center"><i class="fa fa-spin fa-spinner"></i>正在加载...</p>',
			        size: 'small',
			        closeButton: false
			    });
			    var html = '<div class="panel panel-default" style="max-height:500px;overflow-y:scroll;"> <table class="table table-hover" style="width:100%;">';
			    $.ajax({
			        url: "${tenantPrefix}/rs/auth/attend-person-list",
			        data:{id:id},
			        type: "POST",
			        timeout: 10000,
			        success: function (data) {
			            dialog.modal('hide');
			                if (data == undefined || data == null || data == "" || data.length < 1)
			                    html += '<tr><td colspan="2">无</td></tr>'
			                else {
			                    if (data.length > 0) {
			                    	for (var i = 0; i < data.length; i++) {
			                    		if(data[i].id!=null && data[i].id!="" && data[i].personName!=null ){
			                    		
			                    		html += '<tr><td >  '+data[i].personName+' </td></tr>'
			                    		}
			                    	}
			                    }
			                }
			          
			            html += "</table></div>";
			            personListDialog(html);
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
        
			var personListDialog = function (show) {
				bootbox.dialog({
			        title: '查看考勤组人员',
			        message: show,
			        buttons: {
			            ok: {
			                label: "确定",
				           }
			    	}
			    });
			}  
       	 
    </script>

    <style type="text/css">
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
    <!-- start of main -->
    <section id="m-main" class="col-md-12" style="padding-top: 3px;">

        <ul class="breadcrumb">
            <li><a href="person-info-list-i.do">考勤 / 时间设置</a></li>
            <li class="active"></li>
        </ul>


		<div class="pull-left">
			<div class="btn-group" role="group">
				<button id="attendanceManage" class="btn btn-default a-search" onclick="location.href='attendance-records-time-set-i.do'">考勤组管理</button>
				<button id="shiftManage" class="btn btn-default a-search" onclick="location.href='timeSet-shiftManage-i.do'">班次管理</button>
			</div>
		</div>
		<br/><br/><br/>

        <div class="panel panel-default">
            <div class="panel-heading">
                <i class="glyphicon glyphicon-list"></i> 查询
                <div class="pull-right ctrl">
                    <a class="btn btn-default btn-xs"><i
                            id="employee-infoSearchIcon"
                            class="glyphicon glyphicon-chevron-up"></i></a>
                </div>
            </div>
            <div class="panel-body">
                <form name="person-infoForm" method="post" id="person-infoForm"
                      action="attendance-records-time-set-i.do" class="form-inline">
                    <label for="person-info_name"><spring:message
                            code='employee-info.employee-info.list.search.name' text='考勤组名称'/>:</label>
                      
                    <input type="text" id="person-info_name"
                           name="filter_LIKES_RECORDNAME"
                           value="${param.filter_LIKES_RECORDNAME}" class="form-control">
                        
             		<input type="hidden" id="isSearch" name="isSearch" value="${isSearch}" class="form-control">
                    
                    <button id="btn_Search" class="btn btn-default a-search"
                            onclick="searchInfo()">查询
                    </button>
                   	&nbsp;
                </form>
            </div>
        </div>

        <!-- <div style="margin-bottom: 20px;">
            <div class="pull-right">
				  每页显示
				  <select class="m-page-size form-control" style="display:inline;width:auto;">
				    <option value="10">10</option>
				    <option value="20">20</option>
				    <option value="50">50</option>
				  </select>
				  条
	        </div>
            <div class="clearfix"></div>
        </div> -->
        
        <div style="margin-bottom:10px;">
              <button type="button" class="btn btn-default" onclick="location.href='${tenantPrefix}/auth/new-attendance-records-list.do'">新增考勤组</button>
        </div>
       <form id="person-infoGridForm" name="person-infoGridForm"
              method='post'
              action="person-info-remove.do?partyEntityId=${partyEntityId}"
              class="m-form-blank">   
              
            <div class="panel panel-default">
                <div class="panel-heading">
                    <i class="glyphicon glyphicon-list"></i>
                    <spring:message code="scope-info.scope-info.list.title" text="考勤组"/>
                </div>
             
				<div  style="overflow-x:scroll;">
                <table id="person-infoGrid" class="table table-hover">
                    <thead>
                    <tr>
                    	<th name="name">操作</th>
                    	<th name="filter_LIKES_RECORDNAME">名称</th>
                        <th >人数</th>     
                        <th name="company">人员</th>
                        <th name="departmentName">类型</th>
                        <th class="sorting" name="name">考勤时间</th>
                  
                        
                    </tr>
                    </thead>

                    <tbody>
                     <c:forEach items="${page.result}" var="item">
                        <tr>
                        
                        	<td><a href="new-attendance-records-list.do?id=${item.id}">[修改规则]</a>
                        		  <a href="javascript:" onclick='deleteAttendanceDialog(${item.id})'>[删除]</a>
                        	</td>
                        	
                            <td>${item.recordName}		</td><!--考勤组的 名称-->
                          	<td>${item.personNum}		</td><!-- 人数-->
                            <td><a href="javascript:" onclick='personList(${item.id})'>查看</a></td><!-- 人员 -->
                            <td>固定班制</td><!-- 类型 -->
                            <td>
                            <c:if test="${item.mondayShiftStartTime!=null}">
                            		[周一：${item.mondayShiftStartTime}-${item.mondayShiftEndTime}]   
                            </c:if>
							<c:if test="${item.tuesdayStartTime!=null}">
                            		[周二：${item.tuesdayStartTime}-${item.tuesdayEndTime}]   
                            </c:if>
                            <c:if test="${item.wednesdayStartTime!=null}">
                            		[周三：${item.wednesdayStartTime}-${item.wednesdayEndTime}]   
                            </c:if>
                            <c:if test="${item.thursdayStartTime!=null}">
                            		[周四：${item.thursdayStartTime}-${item.thursdayEndTime}]   
                            </c:if>
                            <c:if test="${item.fridayStartTime!=null}">
                            		[周五：${item.fridayStartTime}-${item.fridayEndTime}]   
                            </c:if>
                            <c:if test="${item.SaturdayStartTime!=null}">
                            		[周六：${item.SaturdayStartTime}-${item.SundayEndTime}]  
                            </c:if>
                            <c:if test="${item.SundayStartTime!=null}">
                            		[周日：${item.SundayStartTime}-${item.SundayEndTime}]  
                            </c:if>
                   		</td><!-- 考勤时间 -->
                         
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
				</div>
                
              
	            </div>
                <%--   重置密码弹窗--%>
               

            </div>
        </form>

        <div>
            <div class="m-page-info pull-left">共100条记录 显示1到10条记录</div>

            <div class="btn-group m-pagination pull-right">
                <button class="btn btn-default">&lt;</button>
                <button class="btn btn-default">1</button>
                <button class="btn btn-default">&gt;</button>
            </div>

            <div class="clearfix"></div>
        </div>

        <div class="m-spacer"></div>

    </section>
    <!-- end of main -->
</div>

</body>

</html>

