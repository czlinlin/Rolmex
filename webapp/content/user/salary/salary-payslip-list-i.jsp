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
 	<script type="text/javascript" src="${cdnPrefix}/salary/salarySectionMonth.js"></script>

    <style type="text/css">
    	.my-showPwd {
    	    margin-top: 200px;
    	}
    	.modal-content {
    		margin-top: 65px;
    	}
    	
    </style>
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
		var title = "工资表";
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
			if(contractCompanyId == ''){
				alert("请选择薪资单位");
				return false;
			}
        	
        	if(startDate != "" && endDate != ""){
        		if(startDate == endDate){
            		title = contractCompanyName+"公司"+startDate+"工资表";
            	}else{
            		title = contractCompanyName+"公司"+startDate+"~"+endDate+"工资表";
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
					url:"/user/salary-payslip-list-i.do"
				},
				success :function(data){
					if(data.indexOf(",查询,")>-1){
						$("#btn_Search").attr("style","");
            		}else{
            			$("#btn_Search").remove();
            		}
					if(data.indexOf(",导出,")>-1){
						$("#btn_export").attr("style","");
            		}else{
            			$("#btn_export").remove();
            		}
					if(data.indexOf(",发送工资条,")>-1){
						$("#btn_sendSalaryPayslip").attr("style","");
            		}else{
            			$("#btn_sendSalaryPayslip").remove();
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
	                colNames: ['月份','工号','姓名','应出勤','实际出勤','月工资','加班费','缺勤','病假', '事假','旷工','迟到早退',
	                           '补杂项','应发工资','养老','失业','医疗','公积金','其他项','合计','税前工资','子女教育','继续教育','住房贷款利息','住房租金','赡养老人','商业健康险','个人所得税','实发工资','备注','发送次数'],
	                colModel: [
						{ label: '月份', name: 'salaryDate',align: 'center', width: 60 ,sortable: false,frozen: true},
						{ label: '工号', name: 'employeeNo',align: 'center', width: 110,sortable: false,frozen: true},
						{ label: '姓名', name: 'personName',align: 'center', width: 60,sortable: false,frozen: true},
						{ label: '应出勤', name: 'allAttendanceDays',align: 'center', width: 55 ,sortable: false},
						{ label: '实际出勤', name: 'actualAttendanceDays',align: 'center', width: 70,sortable: false},
						{ label: '月工资', name: 'monthWagesMoney',align: 'center', width: 60,sortable: false},
						{ label: '加班费', name: 'overtimePayMoney',align: 'center', width: 60,sortable: false},
						{ label: '缺勤', name: 'missingDeductionMoney',align: 'center', width: 60,sortable: false},
						{ label: '病假', name: 'sickDeductionMoney',align: 'center', width: 60,sortable: false},
						{ label: '事假', name: 'casualDeductionMoney',align: 'center', width: 60,sortable: false},
						{ label: '旷工', name: 'absentDeductionMoney',align: 'center', width: 60,sortable: false},
						{ label: '迟到早退', name: 'earlyLateDeductionMoney',align: 'center', width: 70,sortable: false},
						{ label: '补杂项', name: 'supplementItemsMoney',align: 'center', width: 60,sortable: false},
						{ label: '应发工资', name: 'allWagesMoney',align: 'center', width: 70,sortable: false},
						{ label: '养老', name: 'socialPensionDeductionMoney',align: 'center', width: 60,sortable: false},
						{ label: '失业', name: 'socialUnemploymentDeductionMoney',align: 'center', width: 60,sortable: false},
						{ label: '医疗', name: 'socialMedicalDeductionMoney',align: 'center', width: 60,sortable: false},
						{ label: '公积金', name: 'socialProvidentFundDeductionMoney',align: 'center', width: 60,sortable: false},
						{ label: '其他项', name: 'socialOtherDeductionMoney',align: 'center', width: 60,sortable: false},
						{ label: '合计', name: 'socialTotalDeductionMoney',align: 'center', width: 60,sortable: false},
						{ label: '税前工资', name: 'grossWagesMoney',align: 'center', width: 70,sortable: false},
						{ label: '子女教育', name: 'specialChildrenEducationMoney',align: 'center', width: 70,sortable: false},
						{ label: '继续教育', name: 'specialContinuEducationMoney',align: 'center', width: 70,sortable: false},
						{ label: '住房贷款利息', name: 'specialHotelInterestMoney',align: 'center', width: 100,sortable: false},
						{ label: '住房租金', name: 'specialHotelRentMoney',align: 'center', width: 70,sortable: false},
						{ label: '赡养老人', name: 'specialSupportElderlyMoney',align: 'center', width: 70,sortable: false},
						{ label: '商业健康险', name: 'specialCommercialHealthInsuranceMoney',align: 'center', width: 70,sortable: false},
						{ label: '个人所得税', name: 'personalIncomeMoney',align: 'center', width: 90,sortable: false},
						{ label: '实发工资', name: 'realWagesMoney',align: 'center', width: 70,sortable: false},
						{ label: '备注', name: 'remark', width: 60,sortable: false},
						{ label: '发送次数', name: 'sendCount',align: 'center', width: 60,sortable: false},
	                ],
					loadonce: true,
	                shrinkToFit: false, // must be set with frozen columns, otherwise columns will be shrank to fit the grid width
	                sortable: false,
	                width: "100%",
	                autowidth:true,
	                height: 350,
	                rowNum: 'all',
	                //pager: "#jqGridPager"
	                viewrecords : true,
	                multiselect: true,//复选框  
	                gridComplete : function() { 
	                	if(!isInitPage){
	                		$("#jqGrid-salary-table").jqGrid("setFrozenColumns");
	                	}
	                	
	                	if(isInitPage) 
	                		isInitPage=false;
	                },
	                loadComplete: function() {
	                	autoGridHeight();
	                }
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
            	               {startColumnName: 'salaryDate', numberOfColumns: 31, titleText: title},
            	             ]
            });
          	//单独调用该方法，设置二级表头
            jQuery("#jqGrid-salary-table").jqGrid('setGroupHeaders', { 
            	useColSpanStyle: true, 
            	groupHeaders:[ 
            	               {startColumnName: 'sickDeductionMoney', numberOfColumns: 5, titleText: '请假扣款'},
            	               {startColumnName: 'socialPensionDeductionMoney', numberOfColumns: 6, titleText: '保险公积金扣款'},
            	               {startColumnName: 'specialChildrenEducationMoney', numberOfColumns: 6, titleText: '专项附加扣除项目'}
            	             ] 
            });
            //$("#jqGrid-salary-table").jqGrid("setFrozenColumns");
		}
		
		function autoGridHeight(){
			$("#jqGrid-salary-table_frozen tr:gt(0)").attr("style","height: 23px;");
			/* var grid_frozenTr = $("#jqGrid-salary-table_frozen").find("tr");
			alert(grid_frozenTr.size()); */
			var timestamp1 =Date.parse(new Date());
            console.log(timestamp1);
		}
		
		//发送工资条
		function sendSalaryPayslip(){
			
			/* contractCompanyId = $("#contractCompanyName").val();
			if(contractCompanyId == ''){
				alert("请选择薪资单位");
				return false;
			} */
			//获取多选到的id集合
			var ids = $("#jqGrid-salary-table").jqGrid("getGridParam", "selarrrow");
			if(ids == ''){
				alert("请选择发送人员");
				return false;
			}
            var html = $("#divSendPayslip").html();
            ids = JSON.stringify(ids);
            
            //判断当前薪资单位是否配置邮箱
            $.ajax({  
                  url : "${tenantPrefix}/user/checkedCompanyEmail.do",  
                  data : {  
                      "contractCompanyId" : contractCompanyId
                  },  
                  type : 'post', 
                  dataType:'json',
                  success : function(data) {
                  	if(!data){
                  		bootbox.alert("请配置合同单位邮箱地址！");
                  	}else{
                  		showPwd(ids);
                  	}
                  }, 
                  error:function(XMLHttpRequest, textStatus, errorThrown){
		            	bootbox.alert("获取失败");
		                return false;
            	  },
	              complete:function(xh,status){
	                if(status=="timeout"){
	                	 bootbox.alert("获取超时");
	                }
	                return false;
	              }
            });
 
            
            
			
			//遍历访问这个集合
			/* $(ids).each(function (index, id){
			     //由id获得对应数据行
			var row = $("#jqGrid-salary-table").jqGrid('getRowData', id);
			    alert("row.ID:"+row.employeeNo+"  "+"row.fieldName:"+row.personName);
			}); */
		}
		
    	function showPwd(ids){
	    		var html ="";
	    		
	    		html+='<table style="width:80%">';
	    		html+='<tr>'
	    		html+='<td style="width:20%"></td>'
	    		html+='<td style="width:80%">'
	    		html+='<div id="divMsg" style="margin:0 5px;color:red;"></div>'
	    		html+='</td>'
	    		html+='</tr>'
	    		html+='<tr>'
	    		html+='<td style="text-align:right;">邮箱密码：</td>'
	    		html+='<td style="height:40px;"><input id="emailPassword" value="" type="password" style="padding:0 5px;height:30px;line-height:25px;" placeholder="请输入邮箱密码"/></td>'
	    		html+='</tr>'
    			html+='<tr>'
   	    		html+='<td style="text-align:right;">操作密码：</td>'
   	    		html+='<td style="height:40px;"><input id="userPassword" value="" type="password" style="padding:0 5px;height:30px;line-height:25px;" placeholder="请输入操作密码"/></td>'
   	    		html+='</tr>'
	    		html+='</table>'
	    		var dialogInput = bootbox.dialog({
	                title: "密码",
	                message: html,
	                className: "my-showPwd",
	                buttons: {
	                    noclose: {
	                        label: '提交',
	                        className: 'btn-primary',
	                        callback: function () {
	                        	var userPassword=$("#userPassword").val();
	                            if (userPassword.length==0) {
	                                bootbox.alert("请输入操作密码！");
	                                return false;
	                            }
	                            
	                            var emailPassword=$("#emailPassword").val();
	                            if (emailPassword.length==0) {
	                                bootbox.alert("请输入邮箱密码！");
	                                return false;
	                            };
	                            
	                            //验证操作密码
	                            $.ajax({  
					                url : "${tenantPrefix}/rs/customer/opteraion-verifyPassword",  
					                data:{pwd: userPassword},  
					                type : 'get', 
					                dataType:'json',
					                success : function(data) { 
					                	if(data.code!=200){
					                		bootbox.alert("操作密码错误！");
					                        //alert(data.message);
					                        return false;
					                    }
					                	var loading = bootbox.dialog({
			                                message: '<p>发送中...</p>',
			                                closeButton: false
			                        	});
					                	//发送邮件
					                	$.ajax({  
					                        url : "${tenantPrefix}/user/sendPayslip.do",  
					                        data : {  
					                            "ids" : ids,
					                            "emailPassword":emailPassword
					                        },  
					                        type : 'post', 
					                        dataType:'json',
					                        success : function(data) {
					                        	if(data){
					                        		loading.modal('hide');
					                        		dialogInput.modal('hide');
					                        		bootbox.alert("发送成功！");
					                        	}else{
					                        		loading.modal('hide');
					                        		bootbox.alert("邮箱密码错误！");
							                        return false;
					                        	}
					                        }, 
					                        error:function(XMLHttpRequest, textStatus, errorThrown){
							                	bootbox.alert("发送失败");
							                    return false;
							                },
							                complete:function(xh,status){
							                    if(status=="timeout"){
							                    	 bootbox.alert("发送超时");
							                    }
							                    return false;
							                }
					                    });
					                },
					                error:function(XMLHttpRequest, textStatus, errorThrown){
					                	bootbox.alert("验证密码错误，提交失败");
					                    return false;
					                },
					                complete:function(xh,status){
					                    if(status=="timeout"){
					                    	 bootbox.alert("验证密码超时");
					                    }
					                    return false;
					                }
					            });
	                            return false;
	                        }
	                    },
	                    cancel: {
	                        label: '取消',
	                        className: 'btn-danger'
	                    }
	                },
	                callback: function (result) {
	                    alert(result);
	                    return;
	                },
	                show: true
	            });
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
                    <select class="form-control" id="contractCompanyName" onchange="getCompany()" class="width:98%;line-height:30px;">
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
                    <button id="btn_export" style="display:none;" class="btn btn-default a-search" onclick="exportPersonSalarySlipForm()">导出</button>
                    <button id="btn_sendSalaryPayslip" style="display:none;" class="btn btn-default a-search" onclick="sendSalaryPayslip();return false;">发送工资条</button>
               
               		
               
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
         <%--   输入密码弹框--%>
     <div style="display:none" id="divSendPayslip">
         <table style="width:80%">
             <tr>
                 <td style="width:20%"></td>
                 <td style="width:80%">
                     <div id="divMsg" style="margin:0 5px;color:red;"></div>
                 </td>
             </tr>

             <tr>
                 <td>&emsp;新密码：</td>
                 <td><input id="newPwd" onkeyup="checkPwd(this)" onpaste="return false" oncopy="return false"
                            oncut="return false" type="password" style="margin:0 5px;" maxlength="20"/></td>
             </tr>
             <tr></tr>
             <tr>
                 <td style="width: 50px ; height: 20px">确认密码：</td>
                 <td><input id="confirmPwd" type="password" onpaste="return false" oncopy="return false"
                            oncut="return false" style="margin:0 5px;" maxlength="20"/></td>
             </tr>
             <tr>
                 <td class="tdl">安全性：</td>
                 <td class="tdr" style=" width:100px;height:40px;">
                     <table cellspacing="0" cellpadding="0" style="width:100%;" class="safetable">
                         <tr class="safesure">
                             <td style="border:1px solid #DEDEDE; height:18px; width: 50px" id="td1">
                             </td>
                             <td style="border:1px solid #DEDEDE; height:18px;  width: 50px" id="td2">
                             </td>
                             <td style="border:1px solid #DEDEDE; height:18px;  width: 50px" id="td3">
                             </td>
                             <td style="border:0px; height:18px; text-align:center;line-height:18px;"
                                 id="safelv"></td>

                         </tr>
                     </table>
                 </td>
             </tr>
         </table>
     </div>
</div>
<script type="text/javascript">
	/* 工资条  */
	function exportPersonSalarySlipForm(){
		if($("#startDate").val()=="" || $("#endDate").val()==""){
			alert("请选择日期范围");
			return false;
		}
		$("#salaryBaseForm").attr("action","person_salary_slip-export.do");
	}
</script>
</body>
</html>

