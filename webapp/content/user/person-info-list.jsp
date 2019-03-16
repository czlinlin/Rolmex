<%@page contentType="text/html;charset=UTF-8"%>
<%@include file="/taglibs.jsp"%>
<%
	pageContext.setAttribute("currentHeader", "person");
%>
<%
	pageContext.setAttribute("currentMenu", "person");
%>
<c:if test="${isAdminRole=='1'}">
	<%pageContext.setAttribute("currentMenuName", "人事管理");%>
</c:if>

<!doctype html>
<html lang="en">

<head>
<%@include file="/common/meta.jsp"%>
<title><spring:message code="dev.employee-info.list.title"
		text="麦联" /></title>
<%@include file="/common/s3.jsp"%>
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
                'filter_LIKES_p.FULL_NAME': '${param.filter_LIKES_p.FULL_NAME}',
                'partyStructTypeId': '${partyStructTypeId}',
                'partyEntityId': '${partyEntityId}'
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
        });

        // 职员离职
        function quit(id, partyEntityId) {
            if (confirm('确定要将此职员离职吗？')) {
                $('#person-infoGridForm').attr('action', '${tenantPrefix}/user/person-info-quit.do?id=' + id +
                    '&partyEntityId=' + partyEntityId);
                $('#person-infoGridForm').submit();
                return true;
            } else {
                return false;
            }
        }

        // 重置密码

        function reset(id) {
            var dialogHtml = '<div><span style="color: red">*</span>新密码<br/><div class="new">';
            dialogHtml += '<input id="newPassword" type="password" style="width:98%;border:1px solid #ccc;border-radius:4px;padding:6px 12px"></input>';
            dialogHtml += '</div><br/>';
            dialogHtml += '<div><span style="color: red">*</span>确认密码<br/><div class="new">';
            dialogHtml += '<input id="confirmPassword" type="password" style="width:98%;border:1px solid #ccc;border-radius:4px;padding:6px 12px"></input>';

            dialogHtml += "</div>";

            var dialog = bootbox.dialog({
                title: "重置密码",
                message: dialogHtml,
                buttons: {
                    noclose: {
                        label: '提交',
                        className: 'btn-primary',
                        callback: function () {
                            if ($("#newPassword").val()< 1) {
                                bootbox.alert("新密码为必填字段！");
                                return false;
                            }
                            if ($("#confirmPassword").val() != $("#newPassword").val()) {
                                bootbox.alert("两次密码输入不一致！");
                                return false;
                            };
                            var loading=bootbox.dialog({
                                message: '<p>提交中...</p>',
                                closeButton: false
                            });

                            var newPassword=($("#newPassword").val());
                            $.post(config.resetUrl, {id: id, newPassword: newPassword}, function (data) {
                                loading.modal('hide')
                                if (data == undefined || data == null || data == "") {
                                    bootbox.alert("重置失败");
                                    return false;
                                }
                                if(data.code==200){
                                    dialog.modal('hide')
                                    bootbox.alert({message:data.message,callback:function(){document.getElementById('btn_Search').click();}});
                                }
                                else
                                    bootbox.alert(data.message);

                                return data.code==200;
                            })
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


        // 重置密码

        function resetKey(id) {
            var dialogHtml = '<div><span style="color: red">*</span>新私钥<br/><div class="new">';
            dialogHtml += '<input id="newPassword" type="password" style="width:98%;border:1px solid #ccc;border-radius:4px;padding:6px 12px"></input>';
            dialogHtml += '</div><br/>';
            dialogHtml += '<div><span style="color: red">*</span>确认私钥<br/><div class="new">';
            dialogHtml += '<input id="confirmPassword" type="password" style="width:98%;border:1px solid #ccc;border-radius:4px;padding:6px 12px"></input>';

            dialogHtml += "</div>";

            var dialog = bootbox.dialog({
                title: "重置私钥",
                message: dialogHtml,
                buttons: {
                    noclose: {
                        label: '提交',
                        className: 'btn-primary',
                        callback: function () {
                            if ($("#newPassword").val()< 1) {
                                bootbox.alert("新私钥为必填字段！");
                                return false;
                            }
                            if ($("#confirmPassword").val() != $("#newPassword").val()) {
                                bootbox.alert("两次私钥输入不一致！");
                                return false;
                            };
                            var loading=bootbox.dialog({
                                message: '<p>提交中...</p>',
                                closeButton: false
                            });

                            var newPassword=($("#newPassword").val());
                            $.post(config.resetKeyUrl, {id: id, newPassword: newPassword}, function (data) {
                                loading.modal('hide')
                                if (data == undefined || data == null || data == "") {
                                    bootbox.alert("重置失败");
                                    return false;
                                }
                                if(data.code==200){
                                    dialog.modal('hide')
                                    bootbox.alert({message:data.message,callback:function(){document.getElementById('btn_Search').click();}});
                                }
                                else
                                    bootbox.alert(data.message);

                                return data.code==200;
                            })
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
    </script>
</head>

<body>
	<%-- <%@include file="/header/org.jsp"%> --%>
	<%@include file="/header/navbar.jsp" %>

	<div class="row-fluid">
		<%@include file="/menu/sidebar.jsp" %>

		<section id="m-main" class="col-md-10" >
			<iframe id="mainframe" name="mainframe" src=""
				width="100%" height="900px" frameborder="0"></iframe>
			<div class="m-spacer"></div>

		</section>
	</div>
	<script>
		var urlPrefix="${tenantPrefix}";
		var iframeUrl=urlPrefix+"/user/person-info-list-i.do";
		$(function(){
			if($("#accordion").size()>0){
				var sideMenus=$("#accordion .panel-default");
				var sideMenuCount=sideMenus.length;
				if(sideMenuCount<1) return;
				var sideCurrentMenu=$(sideMenus[0]);
				var panelCollapse=sideCurrentMenu.find(".panel-collapse");
				if(!panelCollapse.hasClass("in")){
					panelCollapse.addClass("in");
					panelCollapse.attr("aria-expanded","true");
				}
					
				var sideName=delHtmlTag(sideCurrentMenu.find(".panel-title").html());
				if(sideName=='花名册')
					iframeUrl="person-info-list-i.do";
				else if(sideName=='组织结构配置'){
					var url=sideCurrentMenu.find(".panel-body ul li:first a").attr("href");
					if(url!=undefined)
						location.href=url;
					return;
				}
				else if(sideName=='组织机构'){
					iframeUrl=urlPrefix+"/party/org-list-i.do";
				}
				else if(sideName=='考勤'){
					iframeUrl=urlPrefix+"/party/attendance-list-i.do";
				}
				else if(sideName=='考勤统计'){
					iframeUrl=urlPrefix+"/party/attendance-statistics-list-i.do";
				}
			}
			if(iframeUrl==undefined||iframeUrl==null)
				iframeUrl=urlPrefix+"/user/person-info-list-i.do";
				
			$("#mainframe").attr("src",iframeUrl);
		})
		
		function delHtmlTag(str){
			str=str.replace(/<[^>]+>/g,"");//去掉所有的html标记
			str=str.replace(/\s+/g,"");
			return str;
		}
	</script>
</body>

</html>

