<%@page contentType="text/html;charset=UTF-8" %>
<%@include file="/taglibs.jsp"%>
<%pageContext.setAttribute("currentHeader", "pim");%>
<%pageContext.setAttribute("currentMenu", "workReport");%>
<!doctype html>
<html lang="en">

<head>
	<title>麦联</title>
	<style type="text/css">
		body{font-size:14px;}
		.tablePrint{width:950px;margin:0 auto;border-collapse:collapse;}
		.tablePrint .printName{text-align:center;}
		.tablePrint td{padding:5px 10px;width:150px;line-height:28px;height:28px;border:1px solid #ccc;word-break:break-all;word-wrap:break-word}
		.tablePrint .tdl{text-align:right;padding-right:10px;}
		.tdBig{height:50px;}
	</style>
	<script type="text/javascript">
        var HKEY_Root, HKEY_Path, HKEY_Key;
        HKEY_Root = "HKEY_CURRENT_USER";
        HKEY_Path = "\\Software\\Microsoft\\Internet Explorer\\PageSetup\\";
        function pagesetup_null() {
            try {
                var RegWsh = new ActiveXObject("WScript.Shell")
                hkey_key = "header"
                RegWsh.RegWrite(hkey_root + hkey_path + hkey_key, "")
                hkey_key = "footer"
                RegWsh.RegWrite(hkey_root + hkey_path + hkey_key, "")
            } catch (e) {}
        }
        function printme() {
        	var bdhtml=window.document.body.innerHTML;//获取当前页的html代码
            document.body.innerHTML = document.getElementById('divPrint').innerHTML;
            pagesetup_null();
            window.print();
            document.body.innerHTML=bdhtml;
            window.close();
        }
    </script>
</head>

<body>
<div id="divPrint">
	<table class="tablePrint" border="0" cellspacing="0" cellpadding="0">
		<tr><td colspan="4" class="printName"><h3>工作汇报</h3></td></tr>
		<tr><td class="tdl">汇报标题</td><td  colspan="3">${model.title}</td></tr>
		<tr>
		<td class="tdl">汇报类型</td>
		<td>
				<c:if test='${model.type=="1"}'>周报</c:if>
	        	<c:if test='${model.type=="2"}'>月报</c:if>
	        	<c:if test='${model.type=="3"}'>年报</c:if>
	        	<c:if test='${model.type=="4"}'>专项</c:if>
		</td>
		<td class="tdl">汇报时间</td>
		<td><fmt:formatDate value="${model.reportDate}" type="both"/></td>
		</tr>
		<tr>
			<td class="tdl">汇报人</td><td>${publicMan}</td>
			<td class="tdl">接收人</td><td>${receiveMan}</td>
		</tr>
		<tr><td class="tdl">抄送给</td><td colspan="3" class="tdBig">${ccmans}</td></tr>
		<c:if test="${model.type!='4'}">
		<tr><td class="tdl">已完成工作</td><td colspan="3" class="tdBig">${model.completed}</td></tr>
		<tr><td class="tdl">进行中工作</td><td colspan="3" class="tdBig">${model.dealing}</td></tr>
		<tr><td class="tdl">需协调工作</td><td colspan="3" class="tdBig">${model.coordinate}</td></tr>
		</c:if>
		<c:if test="${model.type=='4'}">
		<tr><td class="tdl">专项</td><td colspan="3">${model.problems}</td></tr>
		</c:if>
		<tr>
		<td class="tdl">反馈时间</td>
		<td colspan="3">
			<fmt:formatDate value="${model.feedbacktime}" type="both"/></td>
		</tr>
		<tr><td class="tdl">反馈内容</td><td colspan="3">${model.feedback}</td></tr>
		<tr><td class="tdl">备注</td><td colspan="3">${model.remarks}</td></tr>

	</table>
</div>
<div style="width:500px;margin:20px auto;text-align:center">
	<input value="打印" class="button" onclick="printme();" type="button">
</div>
</body>
</html>
