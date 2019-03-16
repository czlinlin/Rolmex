<%@page language="java" pageEncoding="UTF-8" %>
<!--[if lt IE 9]>
<script type="text/javascript">
//alert('您使用的浏览器版本太低，请使用IE9+，或者FireFox，Chrome浏览。');
</script>
<![endif]-->

<link rel="shortcut icon" type="image/x-icon" href="${cdnPrefix}/favicon.ico"/>
<!-- bootstrap -->
<link rel='stylesheet' href='${cdnPrefix}/bootstrap/3.3.7/css/bootstrap.min.css' type='text/css' media='screen'/>
<style type="text/css">
    .navbar-search .search-query {
        -webkit-border-radius: 15px;
        -moz-border-radius: 15px;
        border-radius: 15px;
    }

    .navbar .navbar-search .search-query {
        border-radius: 15px;
        box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1) inset, 0 1px 0 rgba(255, 255, 255, 0.15);
        color: gray;
        transition: width 0.3s ease 0s;
        width: 100px;
    }

    .navbar .navbar-search .search-query:focus {
        transition: width 0.3s ease 0s;
        width: 200px;
    }
</style>

<!-- html5 -->
<!--[if lt IE 9]>
<script src="${cdnPrefix}/respond/1.4.2/respond.min.js"></script>
<script type="text/javascript" src="${cdnPrefix}/html5/html5shiv.js"></script>
<![endif]-->

<!-- jquery -->
<script type='text/javascript' src='${cdnPrefix}/jquery/1.11.3/jquery.min.js'></script>
<script type="text/javascript" src="${cdnPrefix}/jquery/jquery-migrate-1.2.1.min.js"></script>
<!-- bootstrap -->
<script type='text/javascript' src='${cdnPrefix}/bootstrap/3.3.6/js/bootstrap.min.js'></script>
<!-- bootbox -->
<script type="text/javascript" src="${cdnPrefix}/bootbox/bootbox.min.js"></script>

<!-- message -->
<script type="text/javascript" src="${cdnPrefix}/jquery-sliding-message/jquery.slidingmessage.min.js"></script>
<script type="text/javascript" src="${cdnPrefix}/mossle/js/table.js"></script>

<!-- table and pager -->
<script type="text/javascript" src="${cdnPrefix}/pagination/pagination.js"></script>
<script type="text/javascript" src="${cdnPrefix}/table/table.js"></script>
<script type="text/javascript" src="${cdnPrefix}/table/messages_${locale}.js"></script>

<!-- validater -->
<script type="text/javascript" src="${cdnPrefix}/jquery-validation/jquery.validate.min.js"></script>
<script type="text/javascript" src="${cdnPrefix}/jquery-validation/additional-methods.js"></script>
<script type="text/javascript" src="${cdnPrefix}/jquery-validation/localization/messages_${locale}.js"></script>
<link type="text/css" rel="stylesheet" href="${cdnPrefix}/jquery-validation/jquery.validate.css"/>

<!-- datepicker -->
<link type="text/css" rel="stylesheet" href="${cdnPrefix}/bootstrap-datepicker/datepicker.css">
<script type="text/javascript" src="${cdnPrefix}/bootstrap-datepicker/bootstrap-datepicker.js"></script>
<script type="text/javascript"
        src="${cdnPrefix}/bootstrap-datepicker/locales/bootstrap-datepicker.${locale}.js"></script>
<link href="${cdnPrefix}/bootstrap-datetimepicker/css/bootstrap-datetimepicker.css" rel="stylesheet">
<script type="text/javascript" src="${cdnPrefix}/bootstrap-datetimepicker/js/bootstrap-datetimepicker.js"></script>
<script type="text/javascript"
        src="${cdnPrefix}/bootstrap-datetimepicker/js/locales/bootstrap-datetimepicker.${locale}.js"></script>

<!-- My97DatePicker -->
<script type="text/javascript" src="${cdnPrefix}/My97DatePicker/WdatePicker.js"></script>
<%-- <script type="text/javascript" src="${cdnPrefix}/My97DatePicker/lang/${locale}.js"></script> --%>

<!-- tree -->
<link rel="stylesheet" href="${cdnPrefix}/ztree/zTreeStyle/zTreeStyle.css" type="text/css"/>
<script type="text/javascript" src="${cdnPrefix}/ztree/jquery.ztree.all-3.5.min.js"></script>

<!-- ckeditor -->
<!--[if lt IE 9]>
<script type="text/javascript" src="${cdnPrefix}/ckeditor/ckeditor.js"></script>
<![endif]-->

<script type="text/javascript" src="${cdnPrefix}/ckeditor1/ckeditor.js"></script>
<script type="text/javascript" src="${cdnPrefix}/ckfinder/ckfinder.js"></script>

<!-- viewjs -->
<link type="text/css" rel="stylesheet" href="${cdnPrefix}/viewer/viewer.min.css">
<script type="text/javascript" src="${cdnPrefix}/viewer/viewer-jquery.min.js"></script>

<!-- tablednd -->
<script type="text/javascript" src="${cdnPrefix}/jquery-tablednd/jquery.tablednd.min.js"></script>
<style type="text/css">
    #accordion .panel-heading {
        cursor: pointer;
    }

    #accordion .panel-body {
        padding: 0px;
    }
</style>
<script type="text/javascript">
    $(function () {
        $.showMessage($('#m-success-message').html(), {
            position: 'top',
            size: '55',
            fontSize: '20px'
        });

        var newDate = new Date();
        var t = newDate.toJSON();

        $('.datepicker').datepicker({
            language: '${locale}',
            format: 'yyyy-mm-dd',
            todayBtn: true,
            autoclose: true
        });

        $('.datetimepicker').datetimepicker({
            language: '${locale}',
            format: "yyyy-mm-dd hh:ii",

            autoclose: true,
            todayBtn: true,
            pickerPosition: "bottom-left"
        });

        $('.datetimepickersecond').datetimepicker({
            language: '${locale}',
            format: "yyyy-mm-dd hh:ii:ss",

            autoclose: true,
            todayBtn: true,
            pickerPosition: "bottom-left"
        });

        $('.datetimepickerhour').datetimepicker({
            language: '${locale}',
            format: "yyyy-mm-dd hh:00",

            autoclose: true,
            todayBtn: true,
            minView: 1,
            pickerPosition: "bottom-left"
        });

        function widgetToggleContent() {
            var self = $(this);
            self.toggleClass('glyphicon-chevron-up');
            self.toggleClass('glyphicon-chevron-down');
            var widget = self.parents('.panel');
            var content = widget.find('.panel-body');
            content.toggle(200);
        }

        $(document).delegate('.panel .panel-heading .ctrl .glyphicon-chevron-up', 'click', widgetToggleContent);
        $(document).delegate('.panel .panel-heading .ctrl .glyphicon-chevron-down', 'click', widgetToggleContent);
    });

    /*$.validator.setDefaults({
     errorPlacement : function(error, element) {//error为错误提示对象，element为出错的组件对象
     if(element.parent().parent().hasClass("form-group"))
     error.appendTo(element.parent().parent());
     else
     error.appendTo(element.parent().parent().parent());
     },
     errorClass: 'validate-error'
     });*/

    var fnSectionPickerTime = function (eleTimes) {
        $(eleTimes).each(function (i, ele) {
            $(ele.begin + " span").remove();
            $(ele.end + " span").remove();

            $(ele.begin + " input").css("width", "170px");
            $(ele.end + " input").css("width", "170px");

            $(ele.begin + " input").addClass("Wdate");
            $(ele.end + " input").addClass("Wdate");

            $(ele.begin + " input").attr("id", "ipt" + ele.begin.replace("#", ""));
            $(ele.end + " input").attr("id", "ipt" + ele.end.replace("#", ""))
            var begin = $(ele.begin + " input").attr("id");
            var end = $(ele.end + " input").attr("id");
            $(ele.begin + " input").attr("onclick", "WdatePicker({maxDate:'#F{$dp.$D(\\'" + end + "\\')||\\'2020-10-01\\'}',dateFmt:'yyyy-MM-dd 00:00:00'})");
            $(ele.end + " input").attr("onclick", "WdatePicker({minDate:'#F{$dp.$D(\\'" + begin + "\\')}',maxDate:'2020-10-01',dateFmt:'yyyy-MM-dd 23:59:59'})");
            //开始时间
            $(ele.begin + " .glyphicon-calendar").click(function () {
                $(ele.begin + " input").click();
            })
            $(ele.end + " .glyphicon-calendar").click(function () {
                $(ele.end + " input").click();
            })
        })

        /* $(eleTimes).each(function(i,ele){
         //开始时间
         $(ele.begin).datepicker({
         //todayBtn : "linked",
         format:"yyyy-mm-dd 00:00:00",
         autoclose : true,
         todayHighlight : false,
         language:"
        ${locale}",
         endDate : new Date()
         }).on('changeDate',function(e){
         var startTime = e.date;
         //alert(startTime);
         if(startTime!="")
         $(ele.end).datepicker('setStartDate',startTime);
         });

         //结束时间：
         $(ele.end).datepicker({
         //todayBtn : "linked",
         format:"yyyy-mm-dd 23:59:59",
         autoclose : true,
         todayHighlight : false,
         language:"
        ${locale}",
         startDate: $(ele.begin).val(),
         endDate:new Date()
         }).on('changeDate',function(e){
         var endTime = e.date;
         //alert(endTime);
         if(endTime!="")
         $(ele.begin).datepicker('setEndDate',endTime);
         })
         }) */

    }

</script>
