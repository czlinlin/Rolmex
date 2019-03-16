<%@page contentType="text/html;charset=UTF-8" %>

<div id="yesIe">
    <table id="fileTable" style="border: 1; width: 95%">
        <tbody>
        <tr id="file1">
            <td style="width: 40%"><input type="hidden" id="hid1" value="0">
                <input id="files1" type="file" name="files" class="fileupload"
                       data-no-uniform="true" onchange="oncIe(1)" style="width: 450px">
            </td>
            <td id="fileTd1" style="width: 25%"></td>
            <td id="del1" style="width: 30%"><span onclick="deleteAll();"
                                                   style="cursor: pointer; white-space: nowrap">全部删除</span></td>
        </tr>
        </tbody>
    </table>
</div>

<p>
    <link rel="stylesheet"
          href="${cdnPrefix}/jquery-file-upload/css/jquery.fileupload.css">
    <link rel="stylesheet"
          href="${cdnPrefix}/jquery-file-upload/blueimp-gallery.min.css">
    <script type="text/javascript"
            src="${cdnPrefix}/bootbox/bootbox.min1.js"></script>
<div id="divFiles" class="files"></div>
</p>

<script type="text/javascript">
    /*  if ($.browser.msie) {

     if ($.browser.version == "9.0") {
     $("#noIe").css('display', 'none');
     $("#yesIe").css('display', 'block');
     } else if ($.browser.version == "8.0") {
     $("#noIe").css('display', 'none');
     $("#yesIe").css('display', 'block');
     }
     } else {
     $("#noIe").css('display', 'block');
     $("#yesIe").css('display', 'none');
     }*/
    var filesizes = "";
    var showsizes;
    var nodelIndexs = [];
    function check() {
        if(filesizes>209715200){
            alert("附件大小已经超过200M！")
            return false;
        }
    }
    function onc() {
        nodelIndexs = [];
        var rows = $("#filetablenN").find("tr").length + 1;
        $('#divFiles').html("");
        var files = document.getElementById("files").files;
        for (var i = 0; i < files.length; i++) {
            nodelIndexs.push(i);
            $('#divFiles')
                .append(
                    "<div>"
                    + "文件名: "
                    + files[i].name
                    + " &nbsp; &nbsp;"
                    + "文件大小: "
                    + getSize(files[i].size)
                    + " <span style='color: #1D82D0' onclick='clearTr(this,"
                    + i + ");'> 删除</span></div>");
        }
    }

    function oncIe(row) {
        var rows = row + 1;
        var fileupload = document.getElementById("files" + row);
        var file = $("#files" + row).val();
        var ext = file.split('.')[file.split('.').length - 1];
        if (ext == 'jpg' || ext == 'gif' || ext == 'png' || ext == 'bmp'
            || ext == 'doc' || ext == 'docx' || ext == 'xls'
            || ext == 'xlsx' || ext == 'ppt' || ext == 'pptx'
            || ext == 'pdf' || ext == 'txt' || ext == 'rar' || ext == 'zip'
            || ext == 'JPG' || ext == 'GIF' || ext == 'PNG' || ext == 'BMP'
            || ext == 'DOC' || ext == 'DOCX' || ext == 'XLS' || ext == 'XLSX'
            || ext == 'PPT' || ext == 'PPTX' || ext == 'PDF' || ext == 'TXT'
            || ext == 'RAR' || ext == 'ZIP') {
            /* var files = document.getElementById("files").files;
             filesizes=filesizes+ files[i].size();
             showsizes=getSize(filesizes);*/
        } else {
            alert("请选择 jpg , gif , png , bmp , doc , docx , xls , xlsx , ppt , pptx , pdf , txt , rar , zip 格式的文件上传！");

            if ($.browser.msie) {
                fileupload.outerHTML += ''; //IE
            } else {
                fileupload.value = "";
            }
            return;
        }
        if ($.browser.msie) {
            $("#del" + row)
                .html(
                    '<span onclick="deleteTrE(this);" style="cursor:pointer">&nbsp;&nbsp;删除</span>');
        } else {

            filesizes = Number(filesizes) + Number(fileupload.files[0].size);
            showsizes = getSize(filesizes);
            $("#fileTd" + row).html(getSize(fileupload.files[0].size));

            $("#del" + row)
                .html(
                    '<span onclick="deleteTr(this);" style="cursor:pointer">&nbsp;&nbsp;删除</span>');
        }

        var addflag = $("#hid" + row).val();
        if (addflag == 0) {
            if ($.browser.msie) {
                $("#fileTable tbody")
                    .prepend(
                        '<tr><td style = "width:40%"><input type="hidden" id = "hid' + rows + '" value="0"><input id = "files'
                        + rows
                        + '" type="file" name="files" class="fileupload" data-no-uniform="true" onchange="oncIe('
                        + rows
                        + ')" style="width:450px"></td>'
                        + '<td id = "fileTd'
                        + rows
                        + '" style = "width:25%"></td> <td id = "del'
                        + rows
                        + '" style = "width:30%"><span onclick="deleteAll();" style="cursor:pointer  white-space: nowrap">全部删除</span></td></tr>');
            } else {
                $("#fileTable tbody")
                    .prepend(
                        '<tr><td style = "width:40%"><input type="hidden" id = "hid' + rows + '" value="0"><input id = "files'
                        + rows
                        + '" type="file" name="files" class="fileupload" data-no-uniform="true" onchange="oncIe('
                        + rows
                        + ')" style="width:450px"></td>'
                        + '<td id = "fileTd'
                        + rows
                        + '" style = "width:25%">' + showsizes + '</td><td id = "del'
                        + rows
                        + '" style = "width:30%"><span onclick="deleteAll();" style="cursor:pointer  white-space: nowrap">全部删除</span></td></tr>');
            }

            $("#hid" + row).val("1");
        }
    }

    function deleteTr(nowTr) {
        var a = $(nowTr).parent().parent().find("input[type='file']");
        var tempsize = a[0].files[0].size;
        filesizes = Number(filesizes) - Number(tempsize);
        showsizes = getSize(filesizes);
        $('#fileTable').find('tr').first().find("td")[1].innerHTML = showsizes;
        $(nowTr).parent().parent().remove();

    }
    function deleteTrE(nowTr) {
        $(nowTr).parent().parent().remove();

    }

    function deleteAll() {
        $('#fileTable tbody').empty();
        filesizes = 0;
        showsizes = getSize(filesizes);
        if ($.browser.msie) {
            $("#fileTable tbody")
                .append(
                    '<tr><td style = "width:40%"><input type="hidden" id = "hid1" value="0"><input id = "files1" type="file" name="files" class="fileupload" data-no-uniform="true" onchange="oncIe(1)" style="width:450px"></td>'
                    + '<td id = "fileTd1" style = "width:25%"></td><td id="del1" style = "width:30%"><span onclick="deleteAll();" style="cursor:pointer  white-space: nowrap">全部删除</span></td></tr>');
        } else {
            $("#fileTable tbody")
                .append(
                    '<tr><td style = "width:40%"><input type="hidden" id = "hid1" value="0"><input id = "files1" type="file" name="files" class="fileupload" data-no-uniform="true" onchange="oncIe(1)" style="width:450px"></td>'
                    + '<td id = "fileTd1" style = "width:25%"></td><td id="del1" style = "width:30%"><span onclick="deleteAll();" style="cursor:pointer  white-space: nowrap">全部删除</span></td></tr>');
        }
    }

    function clearTr(cTr, index) {
        var Indexs = [];
        for (var i = 0; i < nodelIndexs.length; i++) {
            if (nodelIndexs[i] != index)
                Indexs.push(nodelIndexs[i])
        }
        nodelIndexs = [];
        nodelIndexs = Indexs;
        $(cTr).parent("div").remove();
        $("#delfiles").val(nodelIndexs.join(','));

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
        if (bytes >= 1024000000) {
            return (bytes / 1024000000).toFixed(2) + ' GB';
        }
        if (bytes >= 1024000) {
            return (bytes / 1024000).toFixed(2) + ' MB';
        }
        return (bytes / 1024).toFixed(2) + ' KB';
    }
</script>



