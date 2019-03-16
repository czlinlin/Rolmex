
operationSubmit = function(conf) {
	/*if (!conf) {
		conf = {
			formId: 'xform',
			checkUrl:"",
			actionUrl:"",
            iptPwdId:""
		};
	}

	this.formId = conf.formId;
	this.checkUrl = conf.checkUrl
    this.actionUrl = conf.actionUrl;
    this.iptPwdId = conf.iptPwdId;*/

    //验证密码
    var pwd = $("#"+conf.iptPwdId).val();
    if(pwd==""){
        alert("请输入操作密码！");
        return false;
    }

    var dialog = bootbox.dialog({
        message:'<p class="text-center"><i class="fa fa-spin fa-spinner"></i>提交中...</p>',
        size:'small',
        closeButton: false
    });

    $.ajax({
        url:conf.checkUrl,
        type:"GET",
        data:{pwd: pwd},
        timeout:10000,
        dataType:"json",
        success: function(data) {
        	if(data.code!=200){
                alert(data.message);
                return false;
            }

            $("#"+conf.formId).attr('action', conf.actionUrl);
            $("#"+conf.formId).submit();
        },
        error:function(XMLHttpRequest, textStatus, errorThrown){
            alert("验证密码错误，提交失败");
            return false;
        },
        complete:function(xh,status){
            dialog.modal('hide');
            if(status=="timeout")
                bootbox.alert("验证密码超时");
            return false;
        }
    });
};