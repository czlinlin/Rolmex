
;(function($){
	var conf_defaults={
			formId: 'xform',
			actionUrl:"",
			eleClass:'check_process'
	};
	var operationSubmit=function(conf){
		if (!conf) {
			conf_defaults=conf;
		}
		
		var input_array=$('.check_process');
		
	}
	
})(jquery);

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
        success: function(data) {
            if(data!="1"){
                dialog.modal('hide');
                alert("操作密码错误，请重新输入！");
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
                bootbox.alert("提交超时");
            return false;
        }
    });
};