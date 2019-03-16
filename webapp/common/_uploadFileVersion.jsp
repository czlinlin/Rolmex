<%--
  
  User: wanghan
  Date: 2017\11\1 0001
  Time: 16:23
 
--%>
<%@page contentType="text/html;charset=UTF-8" %>

<div id="yesIe">
    <table id="fileTable" style="border:1;width:90%">
        <tbody>
        <tr id="file1">
            <td style="width:50%">
                <input type="hidden" id="hid1" value="0">
                <input id="files1" type="file" name="files" class="fileupload" data-no-uniform="true"
                       onchange="oncIe(1)" style="width:550px">
            </td>
            <td id="fileTd1" style="width:25%">

            </td>
            <td id="del1" style="width:25%">
                <span onclick="deleteAll();" style="cursor:pointer">全部删除</span>
            </td>
        </tr>
        </tbody>
    </table>
</div>

<p>
    <link rel="stylesheet" href="${cdnPrefix}/jquery-file-upload/css/jquery.fileupload.css">
    <link rel="stylesheet" href="${cdnPrefix}/jquery-file-upload/blueimp-gallery.min.css">
    <script type="text/javascript" src="${cdnPrefix}/bootbox/bootbox.min1.js"></script>

<div id="divFiles" class="files"></div>
</p>

<script type="text/javascript">

    var nodelIndexs = [];
    function onc() {
        nodelIndexs = [];
        var rows = $("#filetablenN").find("tr").length + 1;
        $('#divFiles').html("");
        var files = document.getElementById("files").files;
        for (var i = 0; i < files.length; i++) {
            nodelIndexs.push(i);
            $('#divFiles').append("<div>" + "文件名: " + files[i].name + " &nbsp; &nbsp;" + "文件大小: "
                + getSize(files[i].size) + " <span style='color: #1D82D0' onclick='clearTr(this," + i + ");'> 删除</span></div>");
        }
    }

    function oncIe(row) {
        var rows = row + 1;
        var fileupload = document.getElementById("files" + row);
        var file = $("#files" + row).val();   
        // alert(file);
        var ext=file.split('.')[file.split('.').length-1];
        if (ext == 'apk' || ext == 'wgt') {
        	
        }else{
        	alert("请选择 apk , wgt 格式的文件上传！");
            
            if ($.browser.msie) {
            	fileupload.outerHTML+=''; //IE
            } else {
            	fileupload.value="";
            }
            return;

        }
        /* var files = fileupload.files;
        for (var i = 0; i < files.length; i++) {
            var filenames = files[i].name;
            var ext = filenames.split('.')[filenames.split('.').length - 1];
            if (ext == 'apk' || ext == 'wgt') {
            } else {
                alert("请选择 apk , wgt 格式的文件上传！");
                fileupload.value = "";
            }
        } */
        if ($.browser.msie) {
            $("#del" + row).html('<span onclick="deleteTr(this);" style="cursor:pointer">&nbsp;&nbsp;删除</span>');
        } else {
            $("#fileTd" + row).html(getSize(fileupload.files[0].size));
            $("#del" + row).html('<span onclick="deleteTr(this);" style="cursor:pointer">&nbsp;&nbsp;删除</span>');
        }


        var addflag = $("#hid" + row).val();
        if (addflag == 0) {
            if ($.browser.msie) {
                $("#fileTable tbody").prepend('<tr><td style = "width:50%"><input type="hidden" id = "hid' + rows + '" value="0"><input id = "files' + rows + '" type="file" name="files" class="fileupload" data-no-uniform="true" onchange="oncIe(' + rows + ')" style="width:550px"></td>'
                    + '<td id = "fileTd' + rows + '" style = "width:25%"></td><td id = "del' + rows + '" style = "width:25%"><span onclick="deleteAll();" style="cursor:pointer">全部删除</span></td></tr>');
            } else {
                $("#fileTable tbody").prepend('<tr><td style = "width:50%"><input type="hidden" id = "hid' + rows + '" value="0"><input id = "files' + rows + '" type="file" name="files" class="fileupload" data-no-uniform="true" onchange="oncIe(' + rows + ')" style="width:550px"></td>'
                    + '<td id = "fileTd' + rows + '" style = "width:25%"></td><td id = "del' + rows + '" style = "width:25%"><span onclick="deleteAll();" style="cursor:pointer">全部删除</span></td></tr>');
            }

            $("#hid" + row).val("1");
        }
    }

    function deleteTr(nowTr) {
        $(nowTr).parent().parent().remove();
    }

    function deleteAll() {
        $('#fileTable tbody').empty();
        if ($.browser.msie) {
            $("#fileTable tbody").append('<tr><td style = "width:50%"><input type="hidden" id = "hid1" value="0"><input id = "files1" type="file" name="files" class="fileupload" data-no-uniform="true" onchange="oncIe(1)" style="width:550px"></td>'
                + '<td id = "fileTd1" style = "width:25%"></td><td id="del1" style = "width:25%"><span onclick="deleteAll();" style="cursor:pointer">全部删除</span></td></tr>');
        } else {
            $("#fileTable tbody").append('<tr><td style = "width:50%"><input type="hidden" id = "hid1" value="0"><input id = "files1" type="file" name="files" class="fileupload" data-no-uniform="true" onchange="oncIe(1)" style="width:550px"></td>'
                + '<td id = "fileTd1" style = "width:25%"></td><td id="del1" style = "width:25%"><span onclick="deleteAll();" style="cursor:pointer">全部删除</span></td></tr>');
        }
    }

    function clearTr(cTr, index) {
        //alert(nodelIndexs.join(','))
        var Indexs = [];
        for (var i = 0; i < nodelIndexs.length; i++) {
            alert(nodelIndexs[i]);
            if (nodelIndexs[i] != index)
                Indexs.push(nodelIndexs[i])
        }
        nodelIndexs = [];
        nodelIndexs = Indexs;
        $(cTr).parent("div").remove();
        $("#delfiles").val(nodelIndexs.join(','));

        //alert(nodelIndexs.join(','))
    }

    function clearFiles() {
        var obj = document.getElementById("files");
        obj.outerHTML = obj.outerHTML;

        $("#divFiles").empty();
    }

    function getSize(bytes) {
        if (typeof bytes !== 'number') {
            return '';
        }
        if (bytes >= 1000000000) {
            return (bytes / 1000000000).toFixed(2) + ' GB';
        }
        if (bytes >= 1000000) {
            return (bytes / 1000000).toFixed(2) + ' MB';
        }
        return (bytes / 1000).toFixed(2) + ' KB';
    }

</script>



