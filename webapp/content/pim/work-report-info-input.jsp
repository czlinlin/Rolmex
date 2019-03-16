<%@page contentType="text/html;charset=UTF-8" %>
<%@include file="/taglibs.jsp" %>
<%pageContext.setAttribute("currentHeader", "pim");%>
<%pageContext.setAttribute("currentMenu", "workReport");%>
<!doctype html>
<html lang="en">

<head>
    <%@include file="/common/meta.jsp" %>
    <title>麦联</title>
    <%@include file="/common/s3.jsp" %>
    <link type="text/css" rel="stylesheet" href="${cdnPrefix}/userpicker3-v2/userpicker.css">
</head>
<body>
<%@include file="/header/navbar.jsp" %>
<div class="row-fluid">
    <%@include file="/menu/sidebar.jsp" %>
    <!-- start of main -->
    <section id="m-main" class="col-md-10" style="margin-top:65px;">

        <div class="panel panel-default">
            <div class="panel-heading">
             	新建汇报
            </div>
            <div class="panel-body">
                <form id="workReportInfoForm" method="post" class="form-horizontal" action="work-report-info-save.do"
                      enctype=multipart/form-data>
                    <c:if test="${not empty model}">
                        <input id="workReportInfo_id" type="hidden" name="id" value="${model.id}">
                    </c:if>
                    <input id="datastatus" type="hidden" name="datastatus">
                    <div class="form-group">
                        <label class="control-label col-md-2" for="WorkReportInfo_title"><span
                                style="color:red;"> * </span>标题
                        </label>
                        <div class="col-md-8">
                            <input id="WorkReportInfo_title" type="text" name="title"
                                   value="${model.title}" size="40" class="form-control required " maxlength="50" onkeyup="checkWord(this)" >
                        </div>
                    </div>
                    <div id="divShowSelectPre" style="display:none;">
	                    <div class="form-group">
	                    	<label class="control-label col-md-2" for="WorkReportInfo_title">汇报岗位</label>
	                    	<div class="col-md-3" style="padding-top:7px;">
	                    		<span id="spanReportPosition"></span>
	                    	</div>
	                    	<label class="control-label col-md-2" for="WorkReportInfo_title">抄送条线</label>
	                    	<div class="col-md-3" style="padding-top:7px;">
	                    		<span id="spanPreSetting"></span>
	                    	</div>
	                     </div>
	                     <div class="form-group">
	                    	<label class="control-label col-md-2" for="WorkReportInfo_title">抄送条线路径</label>
	                    	<div class="col-md-8" style="padding-top:7px;">
	                    		<span id="spanPreSettingPath"></span>
	                    	</div>
	                    </div>
	                      <div class="form-group">
	                    	<label class="control-label col-md-2" for="WorkReportInfo_title">选择操作</label>
	                    	<div class="col-md-8" style="padding-top:7px;">
	                    		[<a href="javascript:" onclick="$('#myModal').modal('show');">重新选择]</a>
	                    	</div>
	                    </div>
                    </div>
                    <div class="button-group">
                        <label class="control-label col-md-2"><span style="color:red;"> * </span>汇报类型</label>
                        <div class="col-md-8">
                            <input type="radio" name="type" value="1" checked="checked" onclick="diva()">周报
                            <input type="radio" name="type" value="2"
                                   <c:if test="${model.type==2}">checked</c:if> onclick="diva()">月报
                            <input type="radio" name="type" value="3"
                                   <c:if test="${model.type==3}">checked</c:if> onclick="diva()">年报
                            <input type="radio" name="type" value="4"
                                   <c:if test="${model.type==4}">checked</c:if> onclick="divb()">专项
                        </div>
                    </div>
                    <br><br>
                    <div class="form-group" id="div1" style="display: none;">
                        <label class="control-label col-md-2" for="workReportInfo_problems">
                        	<span style="color:red;"> * </span>
                        	内容
                        </label>
                        <div class="col-md-8">
                            <textarea id="workReportInfo_problems" name="problems" class="ckeditor" maxlength="2000">
                            	${model.problems}
                           	</textarea>
                        </div>
                    </div>
                    <br><br>
                    <div class="form-group" id="div3">
                        <label class="control-label col-md-2" for="workReportInfo_dealing">
                        	进行中工作
                       	</label>
                        <div class="col-md-8">
                            <textarea id="workReportInfo_dealing" name="dealing"
                                      class="ckeditor">${model.dealing}</textarea>
                        </div>
                    </div>
                    <div class="form-group" id="div2">
                        <label class="control-label col-md-2" for="workReportInfo_completed">
                           	已完成工作
                        </label>
                        <div class="col-md-8">
                            <textarea id="workReportInfo_completed" name="completed"
                                      class="">${model.completed}</textarea>
                        </div>
                    </div>
                    <div class="form-group" id="div4">
                        <label class="control-label col-md-2" for="workReportInfo_coordinate">需协调工作</label>
                        <div class="col-md-8">
                                  <textarea id="workReportInfo_coordinate" name="coordinate"
                                            class="form-control ">${model.coordinate}</textarea>
                        </div>
                    </div>
                    <div class="form-group" id="div5">
                        <label class="control-label col-md-2" for="workReportInfo_remarks">备注</label>
                        <div class="col-md-8">
                                  <textarea id="workReportInfo_remarks" name="remarks"
                                            class="form-control " maxlength="2000">${model.remarks}</textarea>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="control-label col-md-2" name="fileName">添加附件：</label>
                        <div class="col-md-8">
                            <%@include file="/common/_uploadFile.jsp" %>
                            <span style="color:gray;"> 请添加共小于200M的附件 </span>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="control-label col-md-2"><span style="color:red;"> * </span> 接收人</label>
                        <div class="col-md-8">
                            <div class="input-group userPicker">
                                <input id="leaderId" type="hidden" name="sendee"
                                       value="${model.sendee}">

                                <input type="text" id="leaderName" name="sendeeName" class="form-control required"
                                       minlength="2" maxlength="50" class="form-control"
                                       value="<tags:user userId="${model.sendee}"></tags:user>"
                                       readOnly placeholder="点击后方图标即可选人">
                                <div id='leaderDiv' class="input-group-addon "><i class="glyphicon glyphicon-user"></i>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="control-label col-md-2">抄送</label>
                        <div class="col-md-8">
                            <div class="input-group userPicker">
                                <input id="btnPickerMany" type="hidden" name="ccnos" class="input-medium"
                                       value="${ccnos}">
                                <input type="text" id="userName" name="ccName"
                                       value="${ccnames}" class="form-control" readOnly placeholder="点击后方图标即可选人">
                                <div id="ccDiv" class="input-group-addon"><i class="glyphicon glyphicon-user"></i></div>
                            </div>
                            <font color='gray'>如果接收人与抄送人重复，将自动剔除</font>
                        </div>
                    </div>
                    <div class="form-group">
                        <div class="col-md-offset-2 col-md-10">
                            <button type="button" class="btn btn-default a-submit" onclick="submitInfo(1)">提交
                            </button>
                            &nbsp;
                            <c:if test="${model.datastatus ==0 or model.datastatus ==null}">
                                <button type="button" class="btn btn-default a-submit" onclick="submitInfo(0)">保存草稿
                                </button>
                            </c:if>
                            &nbsp;<c:if test="${model.id!=null}">
                            <button type="button" class="btn btn-default"
                                    onclick="self.location=document.referrer;">返回</c:if>
                            </button>
                        </div>
                   </div>
                   <c:if test="${positionInfo.position_type=='person' 
 or (positionInfo.position_type=='positionOne' and positionInfo.isArea==false)
 or (positionInfo.position_type=='person' and positionInfo.isArea==false)}">
 	<input id="iptStartPosition" name="iptStartPosition" value="${positionInfo.position_value}" type="hidden" />
 </c:if>
 
<c:if test="${positionInfo.position_type=='positionList'
 or (positionInfo.position_type=='positionOne' and positionInfo.isArea)
 or (positionInfo.position_type=='person' and positionInfo.isArea)}">
<!-- <input id="iptStartPosition_more" name="iptStartPosition" value="" type="hidden" /> -->
<div class="modal fade" id="myModal" tabindex="-1" role="dialog" data-keyboard="false" data-backdrop="static" aria-labelledby="myModalLabel" aria-hidden="true">
	<div class="modal-dialog">
		<div class="modal-content">
			<div class="modal-header">
				<h4 class="modal-title" id="myModalLabel">
					选择汇报岗位
				</h4>
			</div>
			<div class="modal-body">
				<table>
					<c:if test="${positionInfo.position_type=='positionList'}">
						<tr>
							<td style="width:110px;text-align:right;">选择岗位：</td>
							<td>
								<c:forEach items="${positionInfo.position_list}" var="item">
			                 		<input type="radio" name="iptStartPosition" value="${item.id}" is-area="${item.isArea}" is-managerAbove="${positionInfo.isManagerAbove}"/>
			                 		&nbsp;<label id="lblOption${item.id}">${item.position}</label><br/>
			                 	</c:forEach>
							</td>
						</tr>
	                </c:if>
	                
	                <%--这里是大区的人【有岗通过岗位判断，否则通过人员信息判断】--%>
	                <c:if test="${positionInfo.position_type=='positionOne' or positionInfo.position_type=='person'}">
	                	<input type="hidden" id="iptPositionOne" name="iptPositionOne"/>
	               		<input type="radio" style="display:none;" checked name="iptStartPosition" value="${positionInfo.position_value}"  is-area="${positionInfo.isArea}"/>
	                </c:if>
	                
	                <%-- 是否经理级以上 --%>
	                <c:if test="${positionInfo.isManagerAbove}">
		                <c:if test="${positionInfo.position_type=='person'}">
		                	<font color='red'>注意：大区/分公司人员，如果没有岗位信息，则不走汇报条线抄送</font>
		                </c:if>
		                <c:if test="${positionInfo.position_type=='positionOne' or positionInfo.position_type=='positionList'}">
			                <c:if test="${not empty ccPresetting}">
			                	<tr id="tr-cc-presetting" style="${(positionInfo.position_type=='positionOne' 
					                						or positionInfo.position_type=='person')?'':'display:none'}">
			                		<td style="width:110px;text-align:right;line-height:30px;">抄送条线选择：</td>
			                		<td>
					                	<div>
					                		<select id="select_cc_presetting" name="preSettingId" onchange="showCCPath('opsition')">
					                			<c:forEach items="${ccPresetting}" var="item">
					                				<option value="${item.id}">${item.title}</option>
					                			</c:forEach>
					                		</select>
					               		</div>
				               		</td>
			               		</tr>
			               		<c:forEach items="${ccPresetting}" var="item">
				               		<tr class="tr-cc-path" id="tr-cc-path-${item.id}" style="display:none">
				               			<%--${(positionInfo.position_type=='positionOne' 
					                						or positionInfo.position_type=='person')?'display:none':''}--%>
				               			<td  style="text-align:right;line-height:30px;">抄送路径：</td>
				               			<td>
			                				<div id="divNodeTitle${item.id}">${item.node_title}</div>
				               			</td>
				               		</tr>
			               		</c:forEach>
			                </c:if>
		                </c:if>
		        	 </c:if>
                </table>
			</div>
			<div class="modal-footer">
				<!-- <div id="div_msg" style="display:none;">加载数据中</div> -->
				<button id="btn_ok" type="button" class="btn btn-primary">
					确定
				</button>
			</div>
		</div>
	</div>
</div>
                </form>
            </div>
        </div>

    </section>
    <!-- end of main -->
</div>

<script>
   $(function(){
	   $('#myModal').modal('show');
	   $("#btn_ok").click(function(){
		  if($("input[name='iptStartPosition']:checked").length<1){
			  bootbox.alert({
                  message: '<p class="text-center"><i class="fa fa-spin fa-spinner"></i>请选择一个汇报岗位！</p>',
                  size: 'small'
              });
			  return;
		  };
		  
		  var positionValue=$("input[name='iptStartPosition']:checked").val();
		  if($("#lblOption"+positionValue).size()>0){
			  $("#spanReportPosition").html($("#lblOption"+positionValue).html().substr(3));
			  $("#spanReportPosition").parent().parent().show();
		  }
		  else{
			  $("#spanReportPosition").parent().parent().hide();
		  }
		  
		  var isArea=$("input[name='iptStartPosition']:checked").attr("is-area");
		  if(isArea=="true"){
			  var selectValue=$("#select_cc_presetting").val();
			  var selectText=$("#select_cc_presetting option:selected").text();//获取当前选择项的值.
			  $("#spanPreSetting").html(selectText);
			  $("#spanPreSettingPath").html($("#divNodeTitle"+selectValue).html());
			  
			  $("#spanPreSetting").parent().show();
			  $("#spanPreSetting").parent().prev().show();
			  $("#spanPreSettingPath").parent().parent().show();
		  }
		  else{
			  $("#spanPreSetting").parent().hide();
			  $("#spanPreSetting").parent().prev().hide();
			  $("#spanPreSettingPath").parent().parent().hide();
		  }
		  $("#divShowSelectPre").show();
		  
		  $('#myModal').modal("hide");
	   });
	   
	   function hideTip(){
		   $("#btn_ok").html("确定");
           $("#btn_ok").removeAttr("disabled");
	   }
	   
	   $("input[name='iptStartPosition']").click(function(){
		   if($(this).attr("is-area")=="true"&&$(this).attr("is-managerAbove")=="true"){
			   $("#tr-cc-presetting").show();
			   showCCPath('opsition')
		   }
		   else{
			   $("#tr-cc-presetting").hide();
			   $(".tr-cc-path").hide();
		   }
	   });
	   var type="init";
	   if($("#iptPositionOne").size()>0)
		   type="position";
	   showCCPath(type);
   });
   
   function showCCPath(oper_type){
	   if(oper_type=="init") return;
	   var ele_select=$("#select_cc_presetting");
	   var select_id=ele_select.val();
	   $(".tr-cc-path").hide();
	   $("#tr-cc-path-"+select_id).show();
   }
</script>
</c:if>
<script type="text/javascript" src="${cdnPrefix}/userpicker3-v2/userpickercustom.js"></script>
    <script type="text/javascript" src="${cdnPrefix}/bootbox/bootbox.min1.js"></script>

    <script type="text/javascript">
        $(function () {
            //注册接收人弹出
            createUserPicker({
                modalId: 'leaderPicker',//这个 其实是弹出的窗口的div id
                targetId: 'leaderDiv', //这个是点击哪个 会触发弹出窗口
                showExpression: true,
                searchUrl: '${tenantPrefix}/rs/user/searchVNoMe',
                treeUrl: '${tenantPrefix}/party/asyncTree.do?partyStructTypeId=1&notViewPost=true&notAuth=false',
                childUrl: '${tenantPrefix}/rs/party/searchUserNoMe'
            });
            //注册抄送人弹出
            createUserPicker({
                modalId: 'ccUserPicker',
                targetId: 'ccDiv',
                multiple: true,
                showExpression: true,
                searchUrl: '${tenantPrefix}/rs/user/searchVNoMe',
                treeUrl: '${tenantPrefix}/party/asyncTree.do?partyStructTypeId=1&notViewPost=true&notAuth=false',
                childUrl: '${tenantPrefix}/rs/party/searchUserNoMe'
            })
        })
    </script>
    <script type="text/javascript">
        $(document).ready(function () {
            var editor1 = CKEDITOR.replace('workReportInfo_completed');
            var editor2 = CKEDITOR.replace('workReportInfo_dealing');
            var editor3 = CKEDITOR.replace('workReportInfo_coordinate');
            var editor4 = CKEDITOR.replace('workReportInfo_problems');

            //validate
            $('#workReportInfoForm').validate({
                rules: {
                    problems: {
                        required: true
                    }
                },
                ignore: "",
                errorPlacement: function (error, element) {//error为错误提示对象，element为出错的组件对象
                    if (element.parent().parent().hasClass("form-group"))
                        error.appendTo(element.parent().parent());
                    else
                        error.appendTo(element.parent().parent().parent());
                },
                errorClass: 'validate-error'
            });
            $(".selector").validate({
                showErrors: function (errorMap, errorList) {
                    this.defaultShowErrors();
                }
            });
            var typeValue = $("input[name='type']:checked").val();
            if (typeValue == 4) {
                divb();
            } else {
                diva();
            }
        });

        function submitInfo(datastatus) {
            var text1 = CKEDITOR.instances.workReportInfo_problems.document.getBody().getText();
            var text2 = CKEDITOR.instances.workReportInfo_completed.document.getBody().getText();
            var text3 = CKEDITOR.instances.workReportInfo_dealing.document.getBody().getText();
            $("#workReportInfo_problems").val($.trim(text1));
            $("#workReportInfo_completed").val($.trim(text2));
            $("#workReportInfo_dealing").val($.trim(text3));
            if (!$("#workReportInfoForm").valid()) {
                return false;
            }
            if ($.trim(text2) == "" && $.trim(text3) == "") {
                bootbox.alert({
                    message: '<p class="text-center"><i class="fa fa-spin fa-spinner"></i>请输入进行中或已完成工作！</p>',
                    size: 'small'
                });
                return false;
            }
            if (filesizes > 209715200) {
                bootbox.alert({
                    message: '<p class="text-center"><i class="fa fa-spin fa-spinner"></i>附件大小已经超过200M！</p>',
                    size: 'small'
                });

                return false;
            }
            var loading = bootbox.dialog({
                message: '<p style="width:90%;margin:0 auto;text-align:center;">提交中...</p>',
                size: 'small',
                closeButton: false
            });
            $("#datastatus").val(datastatus);
            $("#workReportInfoForm").submit();
            return true;
        }

        function diva() {
            CKEDITOR.instances.workReportInfo_problems.setData("content");
            document.getElementById("div1").style.display = "none";
            document.getElementById("div2").style.display = "block";
            document.getElementById("div3").style.display = "block";
            document.getElementById("div4").style.display = "block";
            document.getElementById("div5").style.display = "block";
            CKEDITOR.instances.workReportInfo_dealing.setData("");
        }
        function divb() {
            CKEDITOR.instances.workReportInfo_dealing.setData("content");
            document.getElementById("div1").style.display = "block";
            document.getElementById("div2").style.display = "none";
            document.getElementById("div3").style.display = "none";
            document.getElementById("div4").style.display = "none";
            document.getElementById("div5").style.display = "none";
            CKEDITOR.instances.workReportInfo_problems.setData("");
        }
    </script>
    <script>
        function checkWord(obj) {
            var value = $(obj).val();
            var length = value.length;
            if (length > 50) {
                value = value.substring(0, 50);
                $(obj).attr("value", value);
            }
        }
        function checkWordContent(obj) {
            var value = $(obj).val();
            var length = value.length;
            if (length > 2000) {
                value = value.substring(0, 2000);
                $(obj).attr("value", value);
            }
        }
    </script>
</body>
</html>
