<%--
  User: ckx
  Date: 2018\12\4
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
    <title><spring:message code="dev.employee-info.list.title" text="麦联"/></title>
    <%@include file="/common/s3.jsp" %>
    <script type="text/javascript" src="${cdnPrefix}/bootbox/bootbox.min1.js"></script>
    <link type="text/css" rel="stylesheet" href="${cdnPrefix}/orgpicker/orgpicker.css">
	<script type="text/javascript" src="${cdnPrefix}/orgpicker/orgpickerSalary.js?v=1.0"></script>
    <!-- jqgrid表格js -->
    <!-- The jQuery library is a prerequisite for all jqSuite products -->
    <%-- <script type="text/ecmascript" src="${cdnPrefix}/jqgrid/js/jquery.min.js"></script>  --%>
    <!-- This is the Javascript file of jqGrid -->   
    <script type="text/ecmascript" src="${cdnPrefix}/jqgrid/js/trirand/jquery.jqGrid.min.js"></script>
    <!-- This is the localization file of the grid controlling messages, labels, etc.
    <!-- We support more than 40 localizations -->
    <script type="text/ecmascript" src="${cdnPrefix}/jqgrid/js/trirand/i18n/grid.locale-cn.js"></script>
    <!-- A link to a jQuery UI ThemeRoller theme, more than 22 built-in and many more custom -->
    <link rel="stylesheet" type="text/css" media="screen" href="${cdnPrefix}/jqgrid/css/jquery-ui.css" />
    <!-- The link to the CSS that the grid needs -->
    <link rel="stylesheet" type="text/css" media="screen" href="${cdnPrefix}/jqgrid/css/trirand/ui.jqgrid.css" />
    <script type="text/javascript" src="${cdnPrefix}/salary/salarySectionMonth.js"></script>
    <style type="text/css">
	   	.ui-jqgrid .inline-edit-cell{padding:0 4px;}
	   	.ui-jqgrid-htable{font-size:12px;}
	   	.ui-jqgrid-htable{font-size:12px;}
	   	.jqgrow{font-size:12px;text-align:center;}
	   	.jqgrow td{text-align:center;}
	   	.ui-jqgrid tr.ui-row-ltr td{text-align:center;}
	   	.ui-widget-content a{color:#337ab7;cursor:pointer;}
    </style>   
        
        
    <script type="text/javascript">
       $(function(){
    	   createOrgPicker2({
               modalId: 'orgPicker2',
               showExpression: true,
               chkStyle: 'checkbox',
               searchUrl: '${tenantPrefix}/rs/user/search',
               treeUrl: '${tenantPrefix}/rs/party/treeNoPostCompanyChecked?partyStructTypeId=1',
               childUrl: '${tenantPrefix}/rs/party/searchUser'
           });
        });
    </script>
	<script type="text/javascript">
		//查询参数
		var postId = "";
		var contractCompanyId = "";
		var startDate = "";
		var endDate = "";
		var personName = "";
		var title = "社保扣款明细";
		var contractCompanyName = "";
	
		//初始化合同单位
		$(function(){
			$.ajax({
				url: '${tenantPrefix}/user/getAllContractCompanyName.do',
				type : 'post',
				dataType : 'json',
				success :function(data){
					var strHtml ="";
					$("#contractCompanyId").val(data[0].contract_company_id);
					$("#companyName").val(data[0].contract_company_name);
		            for (var i = 0; i < data.length; i++) {
		                strHtml += "<option value='" + data[i].contract_company_id + "'>" + data[i].contract_company_name + "</option>";
		            }
		            $("#contractCompanyName").html(strHtml);
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
		});
	
		 // 查询
        function searchInfo() {
        	postId = $("#orgPartyEntityId").val();
        	contractCompanyId = $("#contractCompanyName").val();
        	startDate = $("#startDate").val();
        	endDate = $("#endDate").val();
        	personName = $("#personName").val();
        	contractCompanyName = $("#companyName").val();
        	if(startDate != "" && endDate != ""){
        		if(startDate == endDate){
            		title = contractCompanyName+"公司"+startDate+"社保扣款明细";
            	}else{
            		title = contractCompanyName+"公司"+startDate+"~"+endDate+"社保扣款明细";
            	}
        	}else if(startDate == ""){
        		alert("请选择开始时间");
        		return false;
        	}else if(endDate == ""){
        		alert("请选择结束时间");
        		return false;
        	}
        	getSalaryTable();
			/* setHeader();
        	$("#jqGrid-salary-table").jqGrid('setGridParam',{ 
                url:"${tenantPrefix}/user/salary-socialSecurity-list-data.do", 
                mtype:"post",
                datatype : "json", 
                postData:{
                	'postId':postId,
                	'contractCompanyId':contractCompanyId,
                	'startDate':startDate,
                	'endDate':endDate,
                	'personNamePar':personName
                }, //发送数据 
                page:"all" 
            }).trigger("reloadGrid"); */
        }
		//导入
		function importSalary(){
			$('#salaryBaseForm').attr('action', '${tenantPrefix}/user/salary-socialSecurity-import-excel.do');
		}
		//生成当月数据
		function createMonthData(){
			contractCompanyId = $("#contractCompanyName").val();
			if(contractCompanyId == ''){
				alert("请选择薪资单位");
				return false;
			}
			//查询当前薪资单位下的最大月份
			$.ajax({  
                url : "${tenantPrefix}/user/querySocialSecurityMonthByContractCompanyId.do",  
                data : {  
                    "contractCompanyId" : contractCompanyId  
                },  
                type : 'post', 
                dataType:'json',
                success : function(data) { 
                	if(data.boo){
                		var currentDate = data.currentDate;
                		var nextDate = data.nextDate;
                    	var year = data.year;
                    	var month = data.month;
                    	if (confirm("是否生成"+year+"年"+month+"月的数据？")){
                    		//生成数据
                			$.ajax({  
                                url : "${tenantPrefix}/user/salarySocialSecurityCreateMonthData.do",  
                                data : {  
                                    "contractCompanyId" : contractCompanyId,
                                    "currentDate":currentDate,
                                    "nextDate":nextDate,
                                    "year":year,
                                    "month":month
                                },  
                                type : 'post', 
                                dataType:'json',
                                success : function(data) {  
                                    if (data) {  
                                    	alert("生成成功")
                                    } else {  
                                        alert("error");  
                                    }  
                                },  
                            });
                    		
                    	}
                	}else{
                		alert("当前薪资单位无上月数据");
                	}
                },  
            });
			
			
		}
		
		//展示列表数据
		$(document).ready(function () {
			getSalaryTable();
        }); 
		
		function getSalaryTable(){
			$.ajax({
				url: '${tenantPrefix}/user/salaryOpteration.do',
				type : 'post',
				dataType : 'json',
				data:{
					url:"/user/salary-socialSecurity-list-i.do"
				},
				success :function(data){
					if(data.indexOf(",查询,")>-1){
						$("#btn_Search").attr("style","");
            		}else{
            			$("#btn_Search").remove();
            		}
					if(data.indexOf(",导入,")>-1){
						$("#btn_import").attr("style","");
            		}else{
            			$("#btn_import").remove();
            		}
					if(data.indexOf(",导出,")>-1){
						$("#btn_export").attr("style","");
            		}else{
            			$("#btn_export").remove();
            		}
					if(data.indexOf(",生成当月数据,")>-1){
						$("#btn_createMonthData").attr("style","");
            		}else{
            			$("#btn_createMonthData").remove();
            		}
					createJgrid(data);
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
		};
		
		//加载列表
		function createJgrid(data){
			 $.jgrid.gridUnload("jqGrid-salary-table");
	         $("#jqGrid-salary-table").jqGrid({
	                url: '${tenantPrefix}/user/salary-socialSecurity-list-data.do',
	                mtype:"post",
	                datatype: "json",
	                postData:{
	                	'postId':postId,
	                	'contractCompanyId':contractCompanyId,
	                	'startDate':startDate,
	                	'endDate':endDate,
	                	'personNamePar':personName
	                },
	                colNames: ['操作','员工状态','月份','姓名', '身份证号','户口性质', '养老基数', '比例', '金额', '比例','金额',
	                           '比例','金额','比例','金额','医疗保险基数','比例', '金额', '比例','金额',
	                           '比例','金额','比例','金额'],
	                colModel: [
						{ name : 'act',width : 100,frozen: true,sortable: false }, 
						{ label: '员工状态', name: 'quitFlag',align: 'center', width: 70, frozen: true,sortable: false },
						{ label: '月份', name: 'socialSecurityDate',align: 'center', width: 70, frozen: true,sortable: false },
						{ label: '姓名', name: 'personName',align: 'center', width: 70, frozen: true,sortable: false },
						{ label: '身份证号', name: 'idcardNum',align: 'center', width: 170, frozen: true,editable: true,cellattr: addCellAttr,sortable: false},
						{ label: '户口性质', name: 'accountCharacte',align: 'center', width: 70,editable: true,cellattr: addCellAttr,sortable: false},
						{ label: '养老基数', name: 'pensionBaseMoney',align: 'center', width: 70,editable: true,cellattr: addCellAttr,sortable: false},
						{ label: '比例', name: 'pensionCompanyProportion',align: 'center', width: 70,editable: true,cellattr: addCellAttr,sortable: false},
						{ label: '金额', name: 'pensionCompanyMoney',align: 'center', width: 70,editable: true ,cellattr: addCellAttr,sortable: false},
						{ label: '比例', name: 'pensionPersonalProportion',align: 'center', width: 70,editable: true,cellattr: addCellAttr,sortable: false},
						{ label: '金额', name: 'pensionPersonalMoney',align: 'center', width: 70,editable: true,cellattr: addCellAttr,sortable: false},
						{ label: '比例', name: 'unemploymentCompanyProportion',align: 'center', width: 70,editable: true,cellattr: addCellAttr,sortable: false},
						{ label: '金额', name: 'unemploymentCompanyMoney',align: 'center', width: 70,editable: true,cellattr: addCellAttr,sortable: false},
						{ label: '比例', name: 'unemploymentPersonalProportion',align: 'center', width: 70,editable: true,cellattr: addCellAttr,sortable: false},
						{ label: '金额', name: 'unemploymentPersonalMoney',align: 'center', width: 70,editable: true,cellattr: addCellAttr,sortable: false},
						{ label: '医疗保险基数', name: 'medicalBaseMoney',align: 'center', width: 70,editable: true,cellattr: addCellAttr,sortable: false},
						{ label: '比例', name: 'medicalCompanyProportion', align: 'center',width: 70,editable: true,cellattr: addCellAttr,sortable: false},
						{ label: '金额', name: 'medicalCompanyMoney',align: 'center', width: 70,editable: true,cellattr: addCellAttr,sortable: false},
						{ label: '比例', name: 'medicalPersonalProportion', align: 'center',width: 70,editable: true,cellattr: addCellAttr,sortable: false},
						{ label: '金额', name: 'medicalPersonalMoney',align: 'center', width: 70,editable: true,cellattr: addCellAttr,sortable: false},
						{ label: '比例', name: 'injuryCompanyProportion',align: 'center', width: 70,editable: true,cellattr: addCellAttr,sortable: false},
						{ label: '金额', name: 'injuryCompanyMoney',align: 'center', width: 70,editable: true,cellattr: addCellAttr,sortable: false},
						{ label: '比例', name: 'birthCompanyProportion',align: 'center', width: 70,editable: true,cellattr: addCellAttr,sortable: false},
						{ label: '金额', name: 'birthCompanyMoney',align: 'center', width: 70,editable: true,cellattr: addCellAttr,sortable: false},
	                ],
					loadonce: true,
	                shrinkToFit: false, // must be set with frozen columns, otherwise columns will be shrank to fit the grid width
	                width: "100%",
	                autowidth:true,
	                height: 350,
	                rowNum: 'all',
	                //pager: "#jqGridPager"
	                viewrecords : true,
	                gridComplete : function() { 
	                	var ids = jQuery("#jqGrid-salary-table").jqGrid('getDataIDs'); 
	                	for ( var i = 0; i < ids.length; i++) {//$('#jqGrid-salary-table').
	                		var cl = ids[i];
	                		var be = "";
	                	    var de = "";
	                		if(data.indexOf(",编辑,")>-1){
		                		be = "&nbsp&nbsp[<a id='edit' title='编辑' onclick=\"editParam('" + cl + "');\">编辑</a>]&nbsp&nbsp";
	                		}	
	                		if(data.indexOf(",删除,")>-1){
		                		de = "[<a id='deleteStr' title='删除' onclick=\"deleteRow('" + cl + "');\">删除</a>]&nbsp&nbsp";
	                		}
	                		jQuery("#jqGrid-salary-table").jqGrid('setRowData', ids[i], { 
	                			act : be + de
	                			}); 
	                		}
	                	if(!isInitPage){
	                		$("#jqGrid-salary-table").jqGrid("setFrozenColumns");
	                	}
	                	
	                	if(isInitPage) 
	                		isInitPage=false;
	                },
	                loadComplete: function() { //加载完数据后执行
	                	autoGridHeight();
	                }
	                //editurl : "${tenantPrefix}/user/editSalatyBase.do"
	            });
	         	setHeader();
		}
		
		var isInitPage=true;
		
		//设置表头
		function setHeader(){
        	jQuery("#jqGrid-salary-table").jqGrid('destroyGroupHeader');//最关键的一步、销毁合并表头分组、防止出现表头重叠
        	//单独调用该方法，设置四级表头
        	jQuery("#jqGrid-salary-table").jqGrid('setGroupHeaders', { 
            	useColSpanStyle: true, 
            	groupHeaders:[ 
            	               {startColumnName: 'act', numberOfColumns: 60, titleText: title},
            	             ]
            });
        	//单独调用该方法，设置三级表头
        	jQuery("#jqGrid-salary-table").jqGrid('setGroupHeaders', { 
            	useColSpanStyle: true, 
            	groupHeaders:[ 
								{startColumnName: 'pensionCompanyProportion', numberOfColumns: 4, titleText: '养老'},
								{startColumnName: 'unemploymentCompanyProportion', numberOfColumns: 4, titleText: '失业'},
								{startColumnName: 'medicalCompanyProportion', numberOfColumns: 4, titleText: '医疗'},
								{startColumnName: 'injuryCompanyProportion', numberOfColumns: 2, titleText: '工伤'},
								{startColumnName: 'birthCompanyProportion', numberOfColumns: 2, titleText: '生育'},
            	             ]
            });
          	//单独调用该方法，设置二级表头
            jQuery("#jqGrid-salary-table").jqGrid('setGroupHeaders', { 
            	useColSpanStyle: true, 
            	groupHeaders:[ 
            	               {startColumnName: 'pensionCompanyProportion', numberOfColumns: 2, titleText: '公司'},
            	               {startColumnName: 'pensionPersonalProportion', numberOfColumns: 2, titleText: '个人'},
            	               {startColumnName: 'unemploymentCompanyProportion', numberOfColumns: 2, titleText: '公司'},
            	               {startColumnName: 'unemploymentPersonalProportion', numberOfColumns: 2, titleText: '个人'},
            	               {startColumnName: 'medicalCompanyProportion', numberOfColumns: 2, titleText: '公司'},
            	               {startColumnName: 'medicalPersonalProportion', numberOfColumns: 2, titleText: '个人'},
            	               {startColumnName: 'injuryCompanyProportion', numberOfColumns: 2, titleText: '公司'},
            	               {startColumnName: 'birthCompanyProportion', numberOfColumns: 2, titleText: '公司'}
            	             ] 
            });
            //$("#jqGrid-salary-table").jqGrid("setFrozenColumns");
		}
		//调整样式
		function autoGridHeight(){
			$("#jqGrid-salary-table_frozen tr:gt(0)").attr("style","height: 23px;");
			/* var grid_frozenTr = $("#jqGrid-salary-table_frozen").find("tr");
			alert(grid_frozenTr.size()); */
			
		} 
		
 		//不同数据变颜色
		function addCellAttr(rowId, val, rawObject, cm, rdata) {
			 
			var ss=cm.name+'Color';
			eval('var strReturn=rawObject.'+ss);
        	if(strReturn == '1'){
        		return "style='color:red'";
        	}
        	
		}
		
		 function editParam(rowId) { //第三步：定义编辑操作
			 var parameter = { oneditfunc : function(rowid) { //在行成功转为编辑模式下触发的事件，参数为此行数据id 
				 //alert("edited" + rowid); 
				 }
			 } 
			 jQuery("#jqGrid-salary-table").editRow(rowId);//开启可编辑模式
			 //jQuery("#list").editRow(rowId,parameter); //如果需要参数 
			 //jQuery('#jqGrid-salary-table' ).jqGrid('editRow', rowId, true,pickdates); //开启可编辑模式
       		var cl = rowId; 
       		se = "[<a id='save' title='保存' onclick=\"saveParam('" + cl + "');\">保存</a>]&nbsp&nbsp";
       		ce = "[<a id='restor' title='取消编辑' onclick=\"cancelParam('" + cl + "');\">取消编辑</a>]&nbsp&nbsp";
       		jQuery("#jqGrid-salary-table").jqGrid('setRowData', rowId, { 
       			act : se + ce
       		});
       		
       		var this_scrollTop=$("#jqGrid-salary-table").parent().parent().scrollTop()+1;
       		$("#jqGrid-salary-table").parent().parent().scrollTop(this_scrollTop);
       		this_scrollTop+=-2;
       		$("#jqGrid-salary-table").parent().parent().scrollTop(this_scrollTop);
       		
       		//重置样式
       		scrollTop();
       		autoGridHeight();
		  } 
		 //第四步：定义保存操作，通过键值对把编辑的数据传到后台,如下 
		 function saveParam(rowId) {
			 var idcardNum = "";
			 $.each($("input[name='idcardNum']"),function(i,item){
				 var thisRowId=$(this).attr('rowid');
				 if(thisRowId==rowId)
					 idcardNum = $(this).val();
			 });
			 var parameter = { 
					url : "${tenantPrefix}/user/editSalarySocialSecurity.do", //代替jqgrid中的editurl 
					mtype : "POST", 
					extraparam : { // 额外 提交到后台的数据
						'idcardNum':idcardNum,
						'postId':postId,
	                	'contractCompanyId':contractCompanyId,
	                	'startDate':startDate,
	                	'endDate':endDate,
	                	'personNamePar':personName
					}, 
					successfunc : function(XHR) { 
						//在成功请求后触发;事件参数为XHR对象，需要返回true/false; 
						//alert(XHR.responseText);//接收后台返回的数据 
						if (XHR.responseText == "false") { 
							alert("上限值不能小于下限值"); 
							return false; //返回false会使用修改前的数据填充,同时关闭编辑模式。 
						} else { 
							alert("编辑成功"); 
							return true; //返回true会使用修改后的数据填充当前行,同时关闭编辑模式。
							} 
						}//end successfunc 
						}//end paramenter 
						jQuery('#jqGrid-salary-table').saveRow(rowId, parameter); 
			 
			 
				//var ids = jQuery("#jqGrid-salary-table").jqGrid('getDataIDs'); 
           		var cl = rowId; 
           		var edit = "edit"+cl;
           		var save = "save"+cl;
           		var restor = "restor"+cl;
           		var deleteStr = "delete"+cl;
           		be = "&nbsp&nbsp[<a id='edit' title='编辑' onclick=\"editParam('" + cl + "');\">编辑</a>]&nbsp&nbsp";
           		de = "[<a id='deleteStr' title='删除' onclick=\"deleteRow('" + cl + "');\">删除</a>]&nbsp&nbsp";
           		jQuery("#jqGrid-salary-table").jqGrid('setRowData', rowId, { 
           			act : be + de
           		}); 
           		//重置样式
           		scrollTop();
           		autoGridHeight();
			 } 
		 //第五步：定义取消操作 
		 function cancelParam(rowId) { 
			jQuery('#jqGrid-salary-table').restoreRow(rowId); //用修改前的数据填充当前行 
       		var cl = rowId; 
       		var edit = "edit"+cl;
       		var save = "save"+cl;
       		var restor = "restor"+cl;
       		var deleteStr = "delete"+cl;
       		be = "&nbsp&nbsp[<a id='edit' title='编辑' onclick=\"editParam('" + cl + "');\">编辑</a>]&nbsp&nbsp";
       		de = "[<a id='deleteStr' title='删除' onclick=\"deleteRow('" + cl + "');\">删除</a>]&nbsp&nbsp";
       		jQuery("#jqGrid-salary-table").jqGrid('setRowData', rowId, { 
       			act : be + de
       		}); 
       		//重置样式
       		scrollTop();
       		autoGridHeight();
		 }
 		//删除
		function deleteRow(id){
			if (confirm("您确定删除此记录？")){ 
				$.ajax({  
	                url : "${tenantPrefix}/user/deleteSalarySocialSecurityById.do",  
	                data : {  
	                    "id" : id  
	                },  
	                type : 'post', 
	                dataType:'json',
	                success : function(data) {  
	                    if (data) {  
	                    	
	                        alert("删除成功");  
	                        $("#jqGrid-salary-table tr[id='"+id+"']").remove();
	            			$("#jqGrid-salary-table_frozen tr[id='"+id+"']").remove();
	                       /*  var url = '${tenantPrefix}/user/salary-socialSecurity-list-data.do';  
	                        $("#jqGrid-salary-table").jqGrid('setGridParam', {  
	                            datatype : "json",  
	                            url : url  
	                        }).trigger("reloadGrid");   */
	                    } else {  
	                        alert('删除失败');  
	                    }  
	                },  
	            });
			}
       		//重置样式
       		autoGridHeight();
		}
 		
		//滚动条滑动，调整样式
 		function scrollTop(){
 			var this_scrollTop=$("#jqGrid-salary-table").parent().parent().scrollTop()+1;
       		$("#jqGrid-salary-table").parent().parent().scrollTop(this_scrollTop);
       		this_scrollTop+=-2;
       		$("#jqGrid-salary-table").parent().parent().scrollTop(this_scrollTop);
 		}
 		
    	function getCompany(){
			var  myselect=document.getElementById("contractCompanyName");
    		var index=myselect.selectedIndex ;  
    		var bt=myselect.options[index].value;
    		var t=myselect.options[index].text;
    		$("#contractCompanyId").val(bt);
    		$("#companyName").val(t);
		}
	</script>

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
                <form id="salaryBaseForm" class="form-inline">
                   	<label for="orgPartyEntityId">组织机构:</label>
                   	<div class="input-group orgPicker2">
                         <input id="orgPartyEntityId" type="hidden" name="postId"
                                value="">
                         <input type="text" class="form-control required" id="postName"
                                name="postName" placeholder="" value=""
                                readonly="readonly">
                         <div class="input-group-addon" style="cursor:pointer;text-decoration:none;"><i class="glyphicon glyphicon-user"></i></div>
                    </div>
					<label for="contractCompanyName">薪资单位:</label>
                    <select  id="contractCompanyName"  onchange="getCompany()" class="form-control">
						<option value="">请选择</option>
					</select>
					<input type="hidden" id="contractCompanyId" name="contractCompanyId" value="">
					<input type="hidden" id="companyName" name="companyName" value="">
                    <label >日期范围:</label>
                    <input class="form-control Wdate" autocomplete="off" placeholder="请选择开始时间" type="text" id="startDate" name="startDate" value="${startDate}" onclick="WdatePicker({maxDate:'#F{$dp.$D(\'endDate\')}',dateFmt:'yyyy-MM'})" class="Wdate" style="width:130px;background-color:#eee;padding-left:10px;"/>
                    &emsp;<span>至</span>&emsp; 
                    <input class="form-control Wdate" autocomplete="off" placeholder="请选择结束时间" type="text" id="endDate" name="endDate" value="${endDate}" onclick="WdatePicker({minDate:'#F{$dp.$D(\'startDate\')}',dateFmt:'yyyy-MM'})" class="Wdate" style="width:130px;background-color:#eee;padding-left:10px;"/>
                    <span role="presentation" class="dropdown">
				        <a id="drop4" href="#" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-haspopup="true" aria-expanded="false">
				          	快捷选择
				          <span class="caret"></span>
				        </a>
				        <ul id="menu1" class="dropdown-menu" aria-labelledby="drop4">
				          <li><a href="javascript:getSectionMonth(-1)">上月</a></li>
				          <li><a href="javascript:getSectionMonth(0)">本月</a></li>
				          <li role="separator" class="divider"></li>
				          <li><a href="javascript:getSectionMonth(-2)">上上月</a></li>
				          <li><a href="javascript:getSectionMonth(-3)">上三月</a></li>
				          <li><a href="javascript:getSectionMonth(-4)">上四月</a></li>
				          <li><a href="javascript:getSectionMonth(-5)">上五月</a></li>
				          <li><a href="javascript:getSectionMonth(-6)">上六月</a></li>
				        </ul>
				    </span>
                    <label for="person_name">姓名:</label>
                    <input type="text" id="personName" name="personName" value="${personName}" class="form-control">
                    <button id="btn_Search" style="display:none;" class="btn btn-default a-search" onclick="searchInfo();return false;">查询</button>
                   	
                    <button id="btn_import" style="display:none;" class="btn btn-default a-search" onclick="importSalary();">导入</button>
                    
                    <button id="btn_export" style="display:none;" class="btn btn-default a-search" onclick="exportPersonSalarySocialSecurity()">导出</button>
                    
                    <button id="btn_createMonthData" style="display:none;" class="btn btn-default a-search" onclick="createMonthData();return false;">生成当月数据</button>
                </form>
            </div>
        </div>
        <div id="tebleDiv" class="panel panel-default">
            <div class="panel-heading">
                <i class="glyphicon glyphicon-list"></i>
                <spring:message code="scope-info.scope-info.list.title" text="工资基本表"/>
                
            </div>
            <table id="jqGrid-salary-table" ></table>
			 <!-- style="border-collapse: collapse"  border="1" -->
			<!-- <div id="jqGridPager"></div> -->
        </div>
    </section>
</div>
<script type="text/javascript">
	/* 导出社保明细 */
	function exportPersonSalarySocialSecurity(){
		if($("#startDate").val()=="" || $("#endDate").val()==""){
			alert("请选择日期范围");
			return false;
		}
		$("#salaryBaseForm").attr("action","person-salary-social-security-export.do");
	}
</script>
</body>
</html>

