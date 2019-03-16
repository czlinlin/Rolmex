<%@page contentType="text/html;charset=UTF-8"%>

<!-- The fileinput-button span is used to style the file input field as button -->
<span class="btn btn-primary fileinput-button"> <i
	class="glyphicon glyphicon-plus"></i> <span>选择文件</span> <input
	type="file" name="file" class="fileupload" data-no-uniform="true"
	data-url="${tenantPrefix}/disk/disk-info-uploadFile.do" data-form-data='{"path":"${path}"}'>
</span>

<p>
<div id="progress" class="progress">
	<div class="progress-bar progress-bar-success"></div>
</div>
<!-- The container for the uploaded files -->
<div id="files" class="files"></div>
<input type="hidden" id='fileIds' value='0' />
</p>

<link rel="stylesheet"
	href="${cdnPrefix}/jquery-file-upload/css/jquery.fileupload.css">
<script
	src="${cdnPrefix}/jquery-file-upload/js/vendor/jquery.ui.widget.js"></script>
<script
	src="${cdnPrefix}/jquery-file-upload/js/jquery.iframe-transport.js"></script>
<script src="${cdnPrefix}/jquery-file-upload/js/jquery.fileupload.js"></script>

<script type="text/javascript">
	function generateFileupload(maxLimitedSize) {
		$('#progress').hide();
		$('.fileupload')
				.fileupload(
						{
							dataType : 'json',
							add : function(e, data) {
								var file = data.files[0];
								if (file.size > maxLimitedSize) {
									alert("文件过大");
								} else {
									data.submit();
								}
							},
							submit : function(e, data) {
								$('#progress').show();
								var $this = $(this);
								data.jqXHR = $this.fileupload('send', data);
								$('.progress-bar').css('width', '0%')
										.html('0%');
								return false;
							},
							done : function(e, data) {
								console.log(data);

								var content = ' <i class="icon-16 icon-16-'+data.result.type+'"></i>';
								content += '<a target="_blank" href="${tenantPrefix}/disk/disk-info-view.do?id='
										+ data.result.id + '">'
										+ data.result.name + '</a> ';
								$('#files').append(content);

								var ids = $('#fileIds').val();
								ids += ',' + data.result.id;
								$('#fileIds').val(ids);

								$('#progress').hide();
							},
							fail : function(e, data) {
								alert("上传失败");
							},
							progressall : function(e, data) {
								var progress = parseInt(data.loaded
										/ data.total * 100, 10);
								$('.progress-bar').css('width', progress + '%')
										.html(progress + '%');
							}
						});
	}

	$(function() {
		generateFileupload(1024 * 1024 * 1024);
	});
</script>



