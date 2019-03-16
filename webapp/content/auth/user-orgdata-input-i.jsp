<%@page contentType="text/html;charset=UTF-8"%>
<%@include file="/taglibs.jsp"%>
<%pageContext.setAttribute("currentHeader", "auth");%>
<%pageContext.setAttribute("currentMenu", "auth");%>
<%pageContext.setAttribute("currentChildMenu", "角色管理");%>
<!doctype html>
<html>

  <head>
    <%@include file="/common/meta.jsp"%>
    <title>麦联</title>
    <%@include file="/common/s3.jsp"%>
	<script type="text/javascript" src="${cdnPrefix}/bootbox/bootbox.min1.js"></script>
    <link href="${cdnPrefix}/xform3/styles/xform.css" rel="stylesheet">
    <script type="text/javascript" src="${cdnPrefix}/xform3/xform-packed.js"></script>
    <link type="text/css" rel="stylesheet" href="${cdnPrefix}/userpicker3-v2/userpicker.css">
    <script type="text/javascript" src="${cdnPrefix}/userpicker3-v2/userpickercustomaudit.js"></script>
    <script type="text/javascript">
		$(function() {
		    /* $("#authDataForm").validate({
		        submitHandler: function(form) {
					bootbox.animate(false);
					var box = bootbox.dialog('<div class="progress progress-striped active" style="margin:0px;"><div class="bar" style="width: 100%;"></div></div>');
		            form.submit();
		        },
		        errorClass: 'validate-error'
		    }); */
		    
		    createUserPicker({
                modalId: 'ccUserPicker',
                targetId: 'ccDiv',
                inputStoreIds: {iptid: "ids", iptname: "userName"},//存储已选择的ID和name的input的id
                multiple: true,
                showExpression: true,
                searchUrl: '${tenantPrefix}/rs/user/searchVWithAdmin',
                treeUrl: '${tenantPrefix}/party/asyncTree.do?partyStructTypeId=1&notViewPost=true&notAuth=false',
                childUrl: '${tenantPrefix}/rs/party/searchUserWithAdmin'
            })
		})
		
		function save () {
			var treeObj = $.fn.zTree.getZTreeObj("treeMenuOrgData");
			var nodes = treeObj.getCheckedNodes(true);
			
			var result='';  
            for (var i = 0; i < nodes.length; i++) { 
                result += nodes[i].id +',';
            }  
            result=result.substring(0,result.lastIndexOf(","));
            
            if($("input[type='checkbox']:checkbox").size()>0)
            	$("#iptRootNode").val("1");
           	else
           		$("#iptRootNode").val("");	
            window.parent.dialogLoading();
            $("#iptDataIds").val(result);
			$("#authDataForm").attr('action',"user-orgdata-input-save.do");    //通过jquery为action属性赋值
	        $("#authDataForm").submit();    //提交ID为myform的表单
		}
    </script>
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
    </style>
  </head>

  <body>
  <div class="row-fluid">
  <section id="m-main" class="col-md-12" style="padding-top:65px;">
      <div class="panel panel-default">
        <div class="panel-heading">
		  <i class="glyphicon glyphicon-list"></i>
		  编辑数据权限
		</div>

<form id="authDataForm" method="post" class="form-horizontal">
	<table id="tb1" style="width:100%;">
		<tr>
  			<td colspan="2">
				<div class="form-group" style="margin-top:15px;">
				    <div class="col-md-5 col-md-offset-2">
				      <button type = "button" id="button" class="btn btn-default" onclick ="save();"><spring:message code='core.input.save' text='保存'/></button>
					  &nbsp;
				      <button type="button" onclick="history.back();" class="btn btn-default"><spring:message code='core.input.back' text='返回'/></button>
				      <input type="hidden" id="iptDataIds" name="iptDataIds" value="${dataIds}"/>
				    </div>
				  </div>
  			</td>
  		</tr>
		<tr>
           <td>
            	数据配置人
           </td>
           <td>
           		<div class="input-group " style="width:100%;">
			        <input id="ids" type="hidden" name="ids" class="input-medium"
			               value="${id}"><%-- ${ccnos} --%>
			        <input type="text" id="userName" name="ccName"
			               value="${name}" class="form-control" readOnly placeholder="点击后方图标即可选人"><%-- ${ccnames} --%>
			        <div id="ccDiv" class="input-group-addon"><i class="glyphicon glyphicon-user"></i></div>
			    </div>
           </td>
  		</tr>
  		<tr>
  			<td>是否可查询节点下人员</td>
  			<td>
	  			<div class="input-group" style="text-align:left;">
					<input id="iptRootNode" name="iptRootNode" style="margin-left:10px;" type="checkbox" ${iptRootNode=='1'?'checked=checked':''} value="${iptRootNode}"/>&nbsp;<label for="iptRootNode">罗麦集团</label>
			    	<font color="gray">(选中并保存后，可查询属于此节点下的直属人员)</font>
			    </div>
		    </td>
  		</tr>
  		<tr>
  			<td colspan="1" valign="top">组织机构</td>
  			<td colspan="1">
				<div class="panel-body">
					<%@include file="/common/tree/orgdata.jsp" %>
			  	</div>
  			</td>
  		</tr>
  	</table>
</form>
</div>
</section>
</div>
  </body>

</html>

