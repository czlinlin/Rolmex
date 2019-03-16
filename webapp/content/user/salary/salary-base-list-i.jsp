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
    <%-- <script type="text/ecmascript" src="${cdnPrefix}/jqgrid/js/jquery.min.js"></script> --%> 
    <!-- This is the Javascript file of jqGrid -->   
    <script type="text/ecmascript" src="${cdnPrefix}/jqgrid/js/trirand/jquery.jqGrid.min.js"></script>
    <!-- This is the localization file of the grid controlling messages, labels, etc.
    <!-- We support more than 40 localizations -->
    <script type="text/ecmascript" src="${cdnPrefix}/jqgrid/js/trirand/i18n/grid.locale-cn.js"></script>
    <!-- A link to a jQuery UI ThemeRoller theme, more than 22 built-in and many more custom -->
    <link rel="stylesheet" type="text/css" media="screen" href="${cdnPrefix}/jqgrid/css/jquery-ui.css" />
    <!-- The link to the CSS that the grid needs -->
     <link rel="stylesheet" type="text/css" media="screen" href="${cdnPrefix}/jqgrid/css/trirand/ui.jqgrid.css" />
    <!-- <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css">  -->
    <script type="text/javascript" src="${cdnPrefix}/salary/salarySectionMonth.js"></script>
    <!-- 双日历控件 -->
	<%-- <link href="${cdnPrefix}/datepicker/bootstrap/css/bootstrap.min.css" rel="stylesheet">
	<link rel="stylesheet" type="text/css" media="all" href="${cdnPrefix}/datepicker/daterangepicker/daterangepicker.css" />
	<script type="text/javascript" src="${cdnPrefix}/datepicker/jquery/1.12.4/jquery.min.js" ></script>
	<script type="text/javascript" src="${cdnPrefix}/datepicker/bootstrap/js/bootstrap.min.js" ></script>
	<script type="text/javascript" src="${cdnPrefix}/datepicker/daterangepicker/moment.min.js" ></script>
	<script type="text/javascript" src="${cdnPrefix}/datepicker/daterangepicker/daterangepicker.js" ></script> --%>
    
    
    
    
    <style type="text/css">
	   	.ui-jqgrid .inline-edit-cell{padding:0 4px;}
	   	.ui-jqgrid-htable{font-size:12px;}
	   	.ui-jqgrid-htable{font-size:12px;}
	   	.jqgrow{font-size:12px;text-align:center;}
	   	.jqgrow td{text-align:center;}
	   	.ui-jqgrid tr.ui-row-ltr td{text-align:center;}
	   	.ui-widget-content a{color:#337ab7;cursor:pointer;}
    </style>
 <!--    <style type="text/css">
      	.form-control-my {
		    height: 34px;
		    padding: 6px 12px;
		    font-size: 14px;
		    line-height: 1.42857143;
		    color: #555;
		    border: 1px solid #ccc;
		    border-radius: 4px;
		    box-shadow: inset 0 1px 1px rgba(0,0,0,.075);
		    -o-transition: border-color ease-in-out .15s,box-shadow ease-in-out .15s;
		    transition: border-color ease-in-out .15s,box-shadow ease-in-out .15s;
		}
    
    </style> -->
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
		var contractCompanyName = "";
		var startDate = "";
		var endDate = "";
		var personName = "";
		var title = "工资基本表";
	
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
        	$("#jqGrid-salary-table").jqGrid('clearGridData',false);//clearGridData();
        	$("#gview_jqGrid-salary-table").find(".frozen-div:last").remove();
        	$("#gview_jqGrid-salary-table").find(".frozen-bdiv:last").remove();
			postId = $("#orgPartyEntityId").val();
        	contractCompanyId = $("#contractCompanyName").val();
        	startDate = $("#startDate").val();
        	endDate = $("#endDate").val();
        	personName = $("#personName").val();
        	contractCompanyName = $("#companyName").val();
        	if(startDate != "" && endDate != ""){
        		if(startDate == endDate){
            		title = contractCompanyName+"公司"+startDate+"工资基本表";
            	}else{
            		title = contractCompanyName+"公司"+startDate+"~"+endDate+"工资基本表";
            	}
        	}else if(startDate == ""){
        		alert("请选择开始时间");
        		return false;
        	}else if(endDate == ""){
        		alert("请选择结束时间");
        		return false;
        	}
        	//$("#jqGrid-salary-table").html("");
        	 
        	getSalaryTable();
			/* setHeader();
        	$("#jqGrid-salary-table").jqGrid('setGridParam',{ 
                url:"${tenantPrefix}/user/salary-base-list-data.do", 
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
			$('#salaryBaseForm').attr('action', '${tenantPrefix}/user/salary-base-import-excel.do');
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
					url:"/user/salary-base-list-i.do"
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
		}
		
		//加载列表
		function createJgrid(data){
			 $.jgrid.gridUnload("jqGrid-salary-table");
	         $("#jqGrid-salary-table").jqGrid({
	                url: '${tenantPrefix}/user/salary-base-list-data.do',
	                mtype:"post",
	                datatype: "json",
	                postData:{
	                	'postId':postId,
	                	'contractCompanyId':contractCompanyId,
	                	'startDate':startDate,
	                	'endDate':endDate,
	                	'personNamePar':personName
	                },
	                colNames: ['操作', '姓名', '工号', '身份证号','月份', '薪资单位','级别', '入职时间', '应出勤', '出差', '调休','年假',
	                           '产假','1.5倍','2倍','3倍','缺勤天数','病假','事假','0.5天','1天以上','迟到早退/次','实际出勤','工资标准',
	                           '基本工资','职务工资','技术工资','保密津贴','技术津贴','绩效奖金','转正扣款','月工资','入职年限','交通','住宿','司龄',
	                           '餐费','通讯','其他','加班费','缺勤','病假','事假','旷工','迟到早退','补杂项','应发工资','养老','失业','医疗','公积金',
	                           '其他项','合计','税前工资','子女教育','继续教育','住房贷款利息','住房租金','赡养老人','商业健康险','纳税工资',
	                           '个人所得税','实发工资','备注'],
	                colModel: [
						{ name : 'act',align: 'center',width : 120,sortable: false,frozen: true }, 
						{ label: '姓名', name: 'personName',align: 'center', width: 70, frozen: true ,sortable: false},
						{ label: '工号', name: 'employeeNo',align: 'center', width: 100, frozen: true,sortable: false},
						{ label: '身份证号', name: 'idcardNum',align: 'center', width: 170, frozen: true,editable: true,cellattr: addCellAttr,sortable: false},
						{ label: '月份', name: 'salaryDate',align: 'center', width: 70,sortable: false },
						{ label: '薪资单位', name: 'contractCompanyName',align: 'center', width: 100,sortable: false},
						{ label: '级别', name: 'personLevel',align: 'center', width: 70,sortable: false},
						{ label: '入职时间', name: 'attr1',align: 'center', width: 100,sortable: false},
						{ label: '应出勤', name: 'allAttendanceDays',align: 'center', width: 70,editable: true,sortable: false},
						{ label: '出差', name: 'paidTripDays',align: 'center', width: 70,editable: true,sortable: false},
						{ label: '调休', name: 'paidLieuLeaveDays',align: 'center', width: 70,editable: true,sortable: false},
						{ label: '年假', name: 'paidAnnualLeaveDays',align: 'center', width: 70,editable: true,sortable: false},
						{ label: '产假', name: 'paidMaternityLeaveDays',align: 'center', width: 70,editable: true,sortable: false},
						{ label: '1.5倍', name: 'onePointFiveOvertimeDays',align: 'center', width: 70,editable: true,sortable: false},
						{ label: '2倍', name: 'twoOvertimeDays',align: 'center', width: 70,editable: true,sortable: false},
						{ label: '3倍', name: 'threeOvertimeDays',align: 'center', width: 70,editable: true,sortable: false},
						{ label: '缺勤天数', name: 'missingAttendanceDays',align: 'center', width: 70,editable: true,sortable: false},
						{ label: '病假', name: 'sickLeaveDays', align: 'center',width: 70,editable: true,sortable: false},
						{ label: '事假', name: 'casualLeaveDays',align: 'center', width: 70,editable: true,sortable: false},
						{ label: '0.5天', name: 'halfAbsentDays',align: 'center', width: 70,editable: true,sortable: false},
						{ label: '1天以上', name: 'oneAbsentDays',align: 'center', width: 70,editable: true,sortable: false},
						{ label: '迟到早退/次', name: 'leaveEarlyLateCount',align: 'center', width: 70,editable: true,sortable: false},
						{ label: '实际出勤', name: 'actualAttendanceDays',align: 'center', width: 70,editable: true,sortable: false},
						{ label: '工资标准', name: 'wagesLevelMoney',align: 'center', width: 70,editable: true,cellattr: addCellAttr,sortable: false},
						{ label: '基本工资', name: 'baseWagesMoney',align: 'center', width: 70,editable: true,cellattr: addCellAttr,sortable: false},
						{ label: '职务工资', name: 'postWagesMoney',align: 'center', width: 70,editable: true,cellattr: addCellAttr,sortable: false},
						{ label: '技术工资', name: 'technicalWagesMoney',align: 'center', width: 70,editable: true,cellattr: addCellAttr,sortable: false},
						{ label: '保密津贴', name: 'confidentialityAllowanceMoney',align: 'center', width: 70,editable: true,cellattr: addCellAttr,sortable: false},
						{ label: '技术津贴', name: 'technicalAllowanceMoney',align: 'center', width: 70,editable: true,cellattr: addCellAttr,sortable: false},
						{ label: '绩效奖金', name: 'achievementBonusMoney',align: 'center', width: 70,editable: true,cellattr: addCellAttr,sortable: false},
						{ label: '转正扣款', name: 'correctionDeductionsMoney',align: 'center', width: 70,editable: true,sortable: false},
						{ label: '月工资', name: 'monthWagesMoney',align: 'center', width: 70,editable: true,sortable: false},
						{ label: '入职年限', name: 'entryAgeExpense',align: 'center', width: 70,editable: true,cellattr: addCellAttr,sortable: false},
						{ label: '交通', name: 'carExpenseMoney',align: 'center', width: 70,editable: true,cellattr: addCellAttr,sortable: false},
						{ label: '住宿', name: 'hotelExpenseMoney',align: 'center', width: 70,editable: true,cellattr: addCellAttr,sortable: false},
						{ label: '司龄', name: 'jobAgeExpenseMoney',align: 'center', width: 70,editable: true,cellattr: addCellAttr,sortable: false},
						{ label: '餐费', name: 'mealsExpenseMoney',align: 'center', width: 70,editable: true,cellattr: addCellAttr,sortable: false},
						{ label: '通讯', name: 'communicationExpenseMoney',align: 'center', width: 70,editable: true,cellattr: addCellAttr,sortable: false},
						{ label: '其他', name: 'otherExpenseMoney',align: 'center', width: 70,editable: true,cellattr: addCellAttr,sortable: false},
						{ label: '加班费', name: 'overtimePayMoney',align: 'center', width: 70,editable: true,sortable: false},
						{ label: '缺勤', name: 'missingDeductionMoney',align: 'center', width: 70,editable: true,sortable: false},
						{ label: '病假', name: 'sickDeductionMoney',align: 'center', width: 70,editable: true,sortable: false},
						{ label: '事假', name: 'casualDeductionMoney',align: 'center', width: 70,editable: true,sortable: false},
						{ label: '旷工', name: 'absentDeductionMoney',align: 'center', width: 70,editable: true,sortable: false},
						{ label: '迟到早退', name: 'earlyLateDeductionMoney',align: 'center', width: 70,editable: true,sortable: false},
						{ label: '补杂项', name: 'supplementItemsMoney',align: 'center', width: 70,editable: true,sortable: false},
						{ label: '应发工资', name: 'allWagesMoney',align: 'center', width: 70,editable: true,sortable: false},
						{ label: '养老', name: 'socialPensionDeductionMoney',align: 'center', width: 70,editable: true,cellattr: addCellAttr,sortable: false},
						{ label: '失业', name: 'socialUnemploymentDeductionMoney',align: 'center', width: 70,editable: true,cellattr: addCellAttr,sortable: false},
						{ label: '医疗', name: 'socialMedicalDeductionMoney',align: 'center', width: 70,editable: true,cellattr: addCellAttr,sortable: false},
						{ label: '公积金', name: 'socialProvidentFundDeductionMoney',align: 'center', width: 70,editable: true,cellattr: addCellAttr,sortable: false},
						{ label: '其他项', name: 'socialOtherDeductionMoney',align: 'center', width: 70,editable: true,cellattr: addCellAttr,sortable: false},
						{ label: '合计', name: 'socialTotalDeductionMoney',align: 'center', width: 70,editable: true,cellattr: addCellAttr,sortable: false},
						{ label: '税前工资', name: 'grossWagesMoney',align: 'center', width: 70,editable: true,sortable: false},
						{ label: '子女教育', name: 'specialChildrenEducationMoney',align: 'center', width: 70,editable: true,cellattr: addCellAttr,sortable: false},
						{ label: '继续教育', name: 'specialContinuEducationMoney',align: 'center', width: 70,editable: true,cellattr: addCellAttr,sortable: false},
						{ label: '住房贷款利息', name: 'specialHotelInterestMoney',align: 'center', width: 100,editable: true,cellattr: addCellAttr,sortable: false},
						{ label: '住房租金', name: 'specialHotelRentMoney',align: 'center', width: 70,editable: true,cellattr: addCellAttr,sortable: false},
						{ label: '赡养老人', name: 'specialSupportElderlyMoney',align: 'center', width: 70,editable: true,cellattr: addCellAttr,sortable: false},
						{ label: '商业健康险', name: 'specialCommercialHealthInsuranceMoney',align: 'center', width: 70,editable: true,cellattr: addCellAttr,sortable: false},
						{ label: '纳税工资', name: 'taxableWages',align: 'center', width: 70,editable: true,sortable: false},
						{ label: '个人所得税', name: 'personalIncomeMoney',align: 'center', width: 70,editable: true,sortable: false},
						{ label: '实发工资', name: 'realWagesMoney',align: 'center', width: 70,editable: true,sortable: false},
						{ label: '备注', name: 'remark', width: 200,editable: true,sortable: false},
						
	                ],
					loadonce: false,
	                shrinkToFit: false, // must be set with frozen columns, otherwise columns will be shrank to fit the grid width
	                //scroll:true,
	                width: "100%",
	                autowidth:true,
	                height: 350,
	                rowNum: 'all',
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
	                loadComplete: function() {
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
        	//单独调用该方法，设置三级表头
        	jQuery("#jqGrid-salary-table").jqGrid('setGroupHeaders', { 
            	useColSpanStyle: true, 
            	groupHeaders:[ 
            	               {startColumnName: 'act', numberOfColumns: 60, titleText: title},
            	             ]
            });
          	//单独调用该方法，设置二级表头
            jQuery("#jqGrid-salary-table").jqGrid('setGroupHeaders', { 
            	useColSpanStyle: true, 
            	groupHeaders:[ 
            	               {startColumnName: 'paidTripDays', numberOfColumns: 4, titleText: '带薪假期/天'},
            	               {startColumnName: 'onePointFiveOvertimeDays', numberOfColumns: 3, titleText: '加班/天'},
            	               {startColumnName: 'sickLeaveDays', numberOfColumns: 2, titleText: '加班/天'},
            	               {startColumnName: 'onePointFiveOvertimeDays', numberOfColumns: 2, titleText: '扣款假期/天'},
            	               {startColumnName: 'halfAbsentDays', numberOfColumns: 2, titleText: '旷工/天'},
            	               {startColumnName: 'entryAgeExpense', numberOfColumns: 7, titleText: '福利补贴'},
            	               {startColumnName: 'missingDeductionMoney', numberOfColumns: 5, titleText: '扣款/元'},
            	               {startColumnName: 'socialPensionDeductionMoney', numberOfColumns: 6, titleText: '保险公积金代扣款'},
            	               {startColumnName: 'specialChildrenEducationMoney', numberOfColumns: 6, titleText: '专项附加扣除项目'}
            	             ] 
            });
            
		}
		//调整样式
		function autoGridHeight(){
			$("#jqGrid-salary-table_frozen tr:gt(0)").attr("style","height: 23px;");
			var timestamp1 =Date.parse(new Date());
            console.log(timestamp1);
		} 
		
 		//不同数据变颜色
		function addCellAttr(rowId, val, rawObject, cm, rdata) {
			 
			var ss=cm.name+'Color';
			eval('var strReturn=rawObject.personSalarySupport.'+ss);
        	if(strReturn == '1'){
        		return "style='color:red;'";
        	}
        	
		}
 		
 		//, editrules: { required: true, custom: true, custom_func: ValidateTvalue }
 		//身份证号的校验   value=输入控件的值，name=列名称（来自colModel）
 		function ValidateTvalue(value,name){
 			// 身份证号码为15位或者18位，15位时全为数字，18位前17位为数字，最后一位是校验位，可能为数字或字符X 
 			var reg = /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/; 
 	        var re = new RegExp(reg);
 	        if (re.test(value)) {
 	            return [true, ""];
 	        }else {
 	            return [false, "身份证号格式错误！"];
 	        }
 		}
 		
		//定义编辑操作
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
       		
       		//滑动滚动条
       		scrollTop();
       		
       		//重置样式
       		autoGridHeight();
		  } 
		 //第四步：定义保存操作，通过键值对把编辑的数据传到后台,如下 
		 //{upperLimit: value1,lowerLimit:value2} 
		 function saveParam(rowId) { 
			 
			 var idcardNum = "";
			 $.each($("input[name='idcardNum']"),function(i,item){
				 var thisRowId=$(this).attr('rowid');
				 if(thisRowId==rowId)
					 idcardNum = $(this).val();
			 });
			 
			 var parameter = { 
					url : "${tenantPrefix}/user/editSalatyBase.do", //代替jqgrid中的editurl 
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
			//var ids = jQuery("#jqGrid-salary-table").jqGrid('getDataIDs'); 
           	//for ( var i = 0; i < ids.length; i++) {//$('#jqGrid-salary-table').
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
           		//}
           	
           		
           	 //调整样式
           	 scrollTop();
           	 autoGridHeight();
		 }
 		//删除
		function deleteRow(id){
 			
			if (confirm("您确定删除此记录？")){ 
				$.ajax({  
	                url : "${tenantPrefix}/user/deleteSalaryBaseById.do",  
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
	                        
	                        
	                        //$("#"+id).remove();
	                        /* var url = '${tenantPrefix}/user/salary-base-list-data.do';  
	                        $("#jqGrid-salary-table").jqGrid('setGridParam', {  
	                            datatype : "json",  
	                            url : url  
	                        }).trigger("reloadGrid");  */ 
	                    } else {  
	                        alert('删除失败');  
	                    }  
	                },  
	            });
			}
			//调整样式
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
                         <input id="orgPartyEntityId" type="hidden" name="postId" class="form-control"
                                value="">
                         <input type="text" class="form-control required" id="postName" class="form-control"
                                name="postName" placeholder="" value=""
                                readonly="readonly">
                         <div class="input-group-addon" style="cursor:pointer;text-decoration:none;" ><i class="glyphicon glyphicon-user"></i></div>
                    </div>
					<label for="contractCompanyName">薪资单位:</label>
                    <select id="contractCompanyName"  onchange="getCompany()"  class="form-control">
						<option value="">请选择</option>
					</select>
					<input type="hidden" id="contractCompanyId" name="contractCompanyId" value="">
					<input type="hidden" id="companyName" name="companyName" value="">
                    <label >日期范围:</label>
                   <input class="form-control Wdate" autocomplete="off" placeholder="请选择开始时间" type="text" id="startDate" name="startDate" class="Wdate"  value="${startDate}" onclick="WdatePicker({maxDate:'#F{$dp.$D(\'endDate\')}',dateFmt:'yyyy-MM'})" style="width:130px;background-color:#eee;padding-left:10px;"/>
                    &emsp;<span>至</span>&emsp; 
                    <input class="form-control Wdate" autocomplete="off" placeholder="请选择结束时间" type="text" id="endDate" name="endDate" class="Wdate"  value="${endDate}" onclick="WdatePicker({minDate:'#F{$dp.$D(\'startDate\')}',dateFmt:'yyyy-MM'})"style="width:130px;background-color:#eee;padding-left:10px;"/>
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
                    <%-- <span id="divDateId" class="">
						<input class="form-control-my Wdate" autocomplete="off" placeholder="请选择时间" type="text" id="searchDate" name="searchDate" value=""  style="width:200px;background-color:#eee;padding-left:10px;"/>
						<input type="hidden" id="startDate" name="startDate" value="${startDate}" />
	                    <input type="hidden" id="endDate" name="endDate" value="${endDate}" />
                    </span> --%>
                      
                    <label for="person_name">姓名:</label>
                    <input type="text" id="personName" name="personName" value="${personName}" class="form-control">
                    <button id="btn_Search" style="display:none;" class="btn btn-default a-search" onclick="searchInfo();return false;">查询</button>
                    <button id="btn_import" style="display:none;" class="btn btn-default a-search" onclick="importSalary();">导入</button>
                    <button id="btn_export" style="display:none;" class="btn btn-default a-search" onclick="chooseExportTerm()">导出
                    </button>
                    
                   
                    
                </form>
                <form action="">
                	
                </form>
            </div>
        </div>
           
        <div id="tebleDiv" class="panel panel-default">
            <div class="panel-heading">
                <i class="glyphicon glyphicon-list"></i>
                <spring:message code="scope-info.scope-info.list.title" text="工资基本表"/>
            </div>
			<table id="jqGrid-salary-table" ></table> <!-- style="border-collapse: collapse"  border="1" -->
			<!-- <div id="jqGridPager"></div> -->
        </div>
     
    </section>
</div>
<script type="text/javascript">
	/* 跳转页面，选择导出项 */
	function chooseExportTerm(){
		if($("#startDate").val()=="" || $("#endDate").val()==""){
			alert("请选择日期范围");
			return false;
		}
		$("#salaryBaseForm").attr("action","person-salary-base-choose-trim.do");
	}
</script>
</body>
</html>

