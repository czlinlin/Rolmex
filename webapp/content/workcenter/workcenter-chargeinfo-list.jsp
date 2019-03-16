<%@page contentType="text/html;charset=UTF-8"%>
<%@include file="/taglibs.jsp"%>
<%pageContext.setAttribute("currentHeader", "humantask");%>
<%pageContext.setAttribute("currentMenu", "humantask");%>
<!doctype html>
<html lang="en">

  <head>
    <%@include file="/common/meta.jsp"%>
    <title>麦联</title>
    <%@include file="/common/s3.jsp"%>
    <link type="text/css" rel="stylesheet" href="${cdnPrefix}/userpicker3/userpicker.css">
    <!-- bootbox -->
    <script type="text/javascript" src="${cdnPrefix}/bootbox/bootbox.min1.js"></script>
  	<script type="text/javascript">
	var config = {
	    id: 'charege-infoGrid',
	    pageNo: ${page.pageNo},
	    pageSize: ${page.pageSize},
	    totalCount: ${page.totalCount},
	    resultSize: ${page.resultSize},
	    pageCount: ${page.pageCount},
	    orderBy: '${page.orderBy == null ? "" : page.orderBy}',
	    asc: ${page.asc},
	    params: {
	        'filter_LIKES_name': '${param.filter_LIKES_name}',
	        'filter_LIKES_status': '${param.filter_LIKES_status}'
	    },
		selectedItemClass: 'selectedItem',
		gridFormId: 'charege-infoGridForm',
		exportUrl: 'charege-info-export.do'
	};

var table;

$(function() {
	table = new Table(config);
    table.configPagination('.m-pagination');
    table.configPageInfo('.m-page-info');
    table.configPageSize('.m-page-size');
    
    //Search
    $("#btn_Search").click(function(){
    	var startTime=$("#pickerStartTime").val();
    	if(startTime!=""&&startTime.indexOf("00:00:00")<0)
    		startTime+=" 00:00:00";
    	$("#pickerStartTime").val(startTime);
    	var endTime=$("#pickerEndTime").val();
    	if(endTime!=""&&endTime.indexOf("23:59:59")<0)
    		endTime+=" 23:59:59";
    	$("#pickerEndTime").val(endTime);
    	document.charege-infoForm.submit();
    });   
})

var showCCMan=function(id){
    	var showMsg='当前任务没有抄送人！';
    	var dialog = bootbox.dialog({
    	    message:'<p class="text-center"><i class="fa fa-spin fa-spinner"></i>正在加载...</p>',
    	    size:'small',
    	    closeButton: false
    	});
    	$.ajax({
            url: '${tenantPrefix}/rs/workcenter/chargeCCInfo-list?id='+id,
            success: function(data) {
            	if(data!=undefined&&data!=null&&data!=""){
            		var manList=[];
            		$(data).each(function(i,item){
            			manList.push(item.userid);
            		})
            		showMsg=manList.join(',');
           		}
            	showCCDialog(showMsg);
            },
            error:function(XMLHttpRequest, textStatus, errorThrown){
            	alert("["+XMLHttpRequest.status+"]error，请求失败")
            },
            complete:function(){
            	dialog.modal('hide');
            }
        });
    }
    
var showCCDialog=function(ccMan){
	bootbox.dialog({
        title: '当前任务抄送人',
        message: ccMan,
        buttons: {
            ok: {
                label: "确定"
            }
        }
        });
}
    


    
</script>
  </head>

  <body>
    <%@include file="/header/workcenter.jsp"%>

    <div class="row-fluid">
	  <%@include file="/menu/workcenter.jsp"%>	

	  <!-- start of main -->
      <section id="m-main" class="col-md-10" style="padding-top:65px;">

<div class="panel panel-default">
  <div class="panel-heading">
	<i class="glyphicon glyphicon-list"></i>
    查询
	<div class="pull-right ctrl">
	  <a class="btn btn-default btn-xs"><i id="charege-infoSearchIcon" class="glyphicon glyphicon-chevron-up"></i></a>
    </div>
  </div>
  <div class="panel-body">

		  <form name="charege-infoForm" method="post" action="workcenter-chargeinfo-list.do" class="form-inline">
		    <label for="charege-info_name"><spring:message code='charege-info.charege-info.list.search.title' text='标题'/>:</label>
		    <input type="text" id="charege-info_title" name="filter_LIKES_title" value="${param.filter_LIKES_title}" class="form-control">
		    
		    &nbsp;&nbsp;
		    <label for="charege-info_name"><spring:message code='charege-info.charege-info.list.search.title' text='计划完成时间'/>:</label>
		    <div class="input-group date">
			    <input style="width:100px;" type="text" id="pickerStartTime" name="filter_GED_plantime" value="${param.filter_GED_plantime}" class="form-control required valid">
			    <span class="input-group-addon">
					<i class="glyphicon glyphicon-calendar"></i>
				</span>
		    </div>
		    至
		    <div class="input-group date">
			    <input style="width:100px;" type="text" id="pickerEndTime" name="filter_LED_plantime" value="${param.filter_LED_plantime}" class="form-control required valid">
			    <span class="input-group-addon">
					<i class="glyphicon glyphicon-calendar"></i>
				</span>
		    </div>
			<button id="btn_Search" class="btn btn-default a-search">查询</button>&nbsp;
		  </form>
		</div>
	  </div>

      <div style="margin-bottom: 20px;">
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
	  </div>

<form id="workcenter-waitinfoGridForm" name="workcenter-waitinfoGridForm" method='post' action="charege-info-remove.do" class="m-form-blank">
      <div class="panel panel-default">
        <div class="panel-heading">
		  <i class="glyphicon glyphicon-list"></i>
		  <spring:message code="scope-info.scope-info.list.title" text="列表"/>
		</div>


  <table id="dynamicModelGrid" class="table table-hover">
    <thead>
      <tr>
        <th class="sorting" name="name">标题</th>
        <th class="sorting" name="name">抄送</th>
        <th class="sorting" name="name">开始时间</th>
        <th class="sorting" name="name">计划完成时间</th>
        <th class="sorting" name="name">发布人</th>
        <th class="sorting" name="name">发布时间</th>
        <th width="80">操作</th>
      </tr>
    </thead>

    <tbody>
      <c:forEach items="${page.result}" var="item">
      <tr>
        <td>${item.title}</td>
        <td>[<a href="javascript:" onclick='showCCMan("${item.id}")'><spring:message code="core.list.edit" text="查看"/></a>]</td>
        <td><fmt:formatDate value="${item.starttime}" type="both"/></td>
        <td><fmt:formatDate value="${item.plantime}" type="both"/></td>
        <td><tags:user userId="${item.publisher}"/></td>
        <td><fmt:formatDate value="${item.publishtime}" type="both"/></td>
        <td>
          <a href="javascript:" onclick='alert("暂未开通");return;'><spring:message code="core.list.edit" text="提交"/></a>
        </td>
      </tr>
      </c:forEach>
    </tbody>
  </table>


      </div>
</form>

	  <div>
	    <div class="m-page-info pull-left">
		  共100条记录 显示1到10条记录
		</div>

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

