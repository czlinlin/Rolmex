<%@page contentType="text/html;charset=UTF-8" %>
<%@include file="/taglibs.jsp" %>
<%pageContext.setAttribute("currentHeader", "dict");%>
<%pageContext.setAttribute("currentMenu", "dict");%>
<%pageContext.setAttribute("currentMenuName", "系统配置");%>
<%pageContext.setAttribute("currentChildMenu", "业务类型明细");%>
<!doctype html>
<html>

<head>
    <%@include file="/common/meta.jsp" %>
    <title>麦联</title>
    <%@include file="/common/s3.jsp" %>
    <link type="text/css" rel="stylesheet" href="${cdnPrefix}/userpicker3-v2/userpicker.css">
    <script type="text/javascript" src="${cdnPrefix}/userpicker3-v2/post.js"></script>
    <link type="text/css" rel="stylesheet" href="${cdnPrefix}/jquery-searchableSelect/jquery-searchableSelect.css?v=1.22">
    <script type="text/javascript" src="${cdnPrefix}/jquery-searchableSelect/jquery-searchableSelect.js?v=1.56"></script>
    <script type="text/javascript">
        $(function () {
            $("#dict-typeForm").validate({
                submitHandler: function (form) {
                    bootbox.animate(false);
                    var box = bootbox.dialog('<div class="progress progress-striped active" style="margin:0px;"><div class="bar" style="width: 100%;"></div></div>');
                    form.submit();
                },
                errorClass: 'validate-error'
            });

            createUserPicker({
                modalId: 'userPicker',
                showExpression: true,
                multiple: true,
                searchUrl: '${tenantPrefix}/rs/user/search',
                treeNoPostUrl: '${tenantPrefix}/party/asyncTree.do?partyStructTypeId=1&notViewPost=true&notAuth=false',
                childPostUrl: '${tenantPrefix}/rs/party/searchPost'
            });
        })

        function startProcessInstance() {
            //岗位 为空  不允许提交
            /* if (document.getElementById('postName').value == "") {
                alert("请选择岗位！");
                return false;
            } */

            //将用户选择的业务级别隐藏到input 里面
            var myselect = document.getElementById("level1");
            var index = myselect.selectedIndex;
            var bt = myselect.options[index].value;
            var t = myselect.options[index].text;
            $("#level").val(t);


            $('#dictTypeForm').attr('action', '${tenantPrefix}/dict/dict-business-detail-save.do');
            $('#dictTypeForm').submit();
        }
    </script>
</head>

<body>
<%@include file="/header/dict.jsp" %>

<div class="row-fluid">
    <%@include file="/menu/dict.jsp" %>

    <!-- start of main -->
    <section id="m-main" class="col-md-10" style="padding-top:65px;">

        <div class="panel panel-default">
            <div class="panel-heading">
                <i class="glyphicon glyphicon-list"></i> 编辑
            </div>

            <div class="panel-body">
                <form id="dictTypeForm" name="dictTypeForm" method="post" action="dict-business-detail-save.do"
                      class="form-horizontal">
                    <input id="id" name="id" value="${model.id}" type="hidden">
                    <input id="createTime" name="createTime" value="${model.createTime}" type="hidden">
                    <div class="form-group">
                        <label class="control-label col-md-2" for="bpmProcess_name" >流程</label>
                        <div class="col-md-5">
                            <select name="bpmPName" id="bpmPName" onchange="getBpmName()">
                                <option value="">请选择</option>
                            </select>
                            <input id="bpmPName1" name="bpmPName1" type="hidden">
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="control-label col-md-2" for="bpmProcess_name" >流程标题</label>
                        <div class="col-md-5">
                            <input id="title" name="title" type="text" style="width:60%;" value="${model.title }">
                            <font color='gray'>(暂适用于通用表单)</font>
                        </div>
                    </div>

                    <div class="form-group">
                        <label class="control-label col-md-2" for="bpmProcess_name">表单</label>
                        <div class="col-md-5">
                            <select name="formName" id="formName" onchange="getFormName()">
                                <option value="">请选择</option>
                            </select>
                            <input id="formid" name="formid" type="hidden">
                            <input id="formNames" name="formNames" type="hidden">
                        </div>
                    </div>

                    <div class="form-group">
                        <label class="control-label col-md-2" for="dictType_name" >业务类型</label>
                        <div class="col-md-5">
                            <select name="busType" id="busType" onchange="getBusinessDetail()">
                                <option value="">请选择</option>
                            </select>
                            <input id="businessTypeName" name="businessTypeName" type="hidden">
                            <input id="businessType" name="businessType" type="hidden">
                        </div>
                    </div>

                    <div class="form-group">
                        <label class="control-label col-md-2" for="dictType_detail"
                              >类型细分</label>
                        <div class="col-md-5">
                            <input id="businessDetail" type="text" name="businessDetail" size="40" class="text"
                                   value="${model.busiDetail}">
                        </div>
                    </div>

                    <div class="form-group">
                        <label class="control-label col-md-2" for="dictType_name">业务级别</label>
                        <div class="col-md-5">
                            <c:choose>
                                <c:when test="${model.level=='A'}">
                                    <select name="level1" id="level1" onchange="getLevel()">
                                        <option value="A">A</option>
                                        <option value="B">B</option>
                                        <option value="CG">CG</option>
                                        <option value="S">S</option>
                                    </select>
                                </c:when>
                                <c:when test="${model.level=='B'}">
                                    <select name="level1" id="level1" onchange="getLevel()">
                                        <option value="B">B</option>
                                        <option value="A">A</option>
                                        <option value="CG">CG</option>
                                        <option value="S">S</option>
                                    </select>
                                </c:when>
                                <c:when test="${model.level=='CG'}">
                                    <select name="level1" id="level1" onchange="getLevel()">
                                        <option value="CG">CG</option>
                                        <option value="A">A</option>
                                        <option value="B">B</option>
                                        <option value="S">S</option>
                                    </select>
                                </c:when>
                                <c:when test="${model.level=='S'}">
                                    <select name="level1" id="level1" onchange="getLevel()">
                                        <option value="S">S</option>
                                        <option value="A">A</option>
                                        <option value="B">B</option>
                                        <option value="CG">CG</option>
                                    </select>
                                </c:when>
                                <c:otherwise>
                                    <select name="level1" id="level1" onchange="getLevel()">
                                        <option value="A">A</option>
                                        <option value="B">B</option>
                                        <option value="CG">CG</option>
                                        <option value="S">S</option>
                                    </select>
                                </c:otherwise>
                            </c:choose>
                            <input id="level" name="level" type="hidden">
                        </div>
                    </div>

                    <div class="form-group">
                        <label class="control-label col-md-2" for="_task_name_key">选择岗位:</label>
                        <div class="col-sm-8 userPicker">
                            <div class="input-group ">
                                <input id="_task_name_key" type="hidden" name="postId"
                                       value="${postId}">
                                <input type="text" name="postName" id="postName"
                                       value="${postName}"
                                       class="form-control" readonly>
                                <div class="input-group-addon"><i class="glyphicon glyphicon-user"></i></div>
                            </div>
                        </div>
                    </div>


                    <div class="form-group">
                        <label class="control-label col-md-2" for="dictType_detail"
                        >业务标准(现场办理)</label>
                        <div class="col-md-5">
                            <textarea name="standFirst" rows="4" cols="20" id="standFirst" value="${model.standFirst}"
                                      class="text0"
                                      style="height:99px;width:835px;padding-left:10px;padding-top:10px">${model.standFirst}</textarea>
                        </div>
                    </div>

                    <div class="form-group">
                        <label class="control-label col-md-2" for="dictType_detail"
                        >业务标准(非现场办理)</label>
                        <div class="col-md-5">
                            <textarea name="standSecond" rows="2" cols="20" id="standSecond" class="text0"
                                      style="height:99px;width:835px;padding-left:10px;padding-top:10px">${model.standSecond}</textarea>
                        </div>
                    </div>

                    <input type="hidden" name="bpmConfNodeId" value="${bpmConfNodeId}">

                    <div class="form-group">
                        <div class="col-md-offset-2 col-md-10">
                            <button id="confirmStartProcess" class="btn btn-default" type="button"
                                    onclick="startProcessInstance()">提交
                            </button>
                            <button type="button" class="btn a-cancel" onclick="history.back();">返回</button>
                        </div>
                    </div>

                </form>
            </div>
        </div>
    </section>
    <!-- end of main -->

</body>
<script>
    //到数据库中取业务类型
    $(function () {
        $.getJSON('${tenantPrefix}/rs/business/types', {}, function (data) {

            var option = "<option value=''>请选择</option>";
            for (var i = 0; i < data.length; i++) {
                //alert(JSON.stringify(data[i]));
                option += "<option value='" + data[i].id + "' >" + data[i].name + "</option>"
            }
            $("#busType").html(option);//将循环拼接的字符串插入第二个下拉列表

            var bt = ${model.typeId};
            $("#busType").val(bt);

            var myselect = document.getElementById("busType");

            var index = myselect.selectedIndex;
            var bt = myselect.options[index].value;
            var t = myselect.options[index].text;
            $("#businessType").val(bt);
            $("#businessTypeName").val(t);
        });

        //取流程名称
        $.getJSON('${tenantPrefix}/rs/detailPostService/bpmAllName', {}, function (data) {
            var option = "<option value=''>请选择</option>";
            for (var i = 0; i < data.length; i++) {
                option += "<option value='" + data[i].id + "' >" + data[i].name + "</option>";
            }
            $("#bpmPName").html(option);//将循环拼接的字符串插入第二个下拉列表

            var p ="${model.bpmProcessId}";
            $("#bpmPName").val(p);

            /* var myselect = document.getElementById("bpmPName");
            var index = myselect.selectedIndex;
            var bt = myselect.options[index].value; */
            $("#bpmPName1").val(p);
            
            $("#bpmPName").searchableSelect();
        });

        //获取表单名称
        $.getJSON('${tenantPrefix}/rs/detailPostService/formName', {}, function (data) {
            var option = "<option value=''>请选择</option>";
            for (var i = 0; i < data.length; i++) {
                // alert(JSON.stringify(data[i]));
                option += "<option value='" + data[i].formid + "'>" + data[i].formName + "</option>";
            }
            $("#formName").html(option);//将循环拼接的字符串插入第二个下拉列表
			
            var myselect = document.getElementById("formName");
            myselect.value="${model.formid}";
        	$("#formid").val("${model.formid}");
            $("#formNames").val("${model.formName}");
        });
    });

    //将用户选择的业务类型的ID隐藏到input 里面
    function getBusinessDetail() {
        var myselect = document.getElementById("busType");

        var index = myselect.selectedIndex;
        var bt = myselect.options[index].value;
        var t = myselect.options[index].text;
        $("#businessType").val(bt);
        $("#businessTypeName").val(t);
    }

    function getBpmName() {
        var myselect = document.getElementById("bpmPName");

        var index = myselect.selectedIndex;
        var bt = myselect.options[index].value;

        $("#bpmPName1").val(bt);
        if(bt=="") return;
        
      	// 通过流程获取启动表单
        $.getJSON('${tenantPrefix}/rs/detailPostService/getStartFromByProcessId', {"bpmProcessId":bt}, function (data) {
            var formName = "";
            if(!data){
            	alert("流程设置错误，无法获得流程的启动页面，请核对流程后再试！");
            	return;
            }
        	if (data.length<1) {
            	alert("流程设置错误，无法获得流程的启动页面，请核对流程后再试！");
            	return;
            } else {
            	formName=data[0].formName;
            	
            	var myselect = document.getElementById("formName");
            	myselect.value=formName;
                var index = myselect.selectedIndex;
                var bt = myselect.options[index].value;
                var t = myselect.options[index].text;

                $("#formid").val(bt);
                $("#formNames").val(t);
            }
        });
    }

    function getFormName() {
        var myselect = document.getElementById("formName");
        var index = myselect.selectedIndex;
        var bt = myselect.options[index].value;
        var t = myselect.options[index].text;

        $("#formid").val(bt);
        $("#formNames").val(t);
    }

</script>
</html>



