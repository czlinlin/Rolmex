<%@page contentType="text/html;charset=UTF-8" %>
<div class="form-group">	
				<tr>
                    <td colspan='4' class='f_td f_right' align='right' style='padding-right:20px;'>
                        	提交次数：0&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; 受理单编号：<span id="spanApplyCode">${code}</span>
                        <input id="applyCode" class="input_width" style="display:none;" 
                        		name="applyCode" value="${code}"  readonly>
                    </td>
                </tr>	
							
			</div>		
				
		<div  class="container">		
			<tr>
                    <td colspan='4'><span style="color:red;">请按顺序选择审核人</span><br/>
                        <ul id="ulapprover" style="width:96%;margin:0 auto;list-style:none;">
                        </ul>
                    </td>
              	</tr>
              		
              	<tr>
                    <td><font color="red " size="4"> * </font>审批人</td>
                    <td colspan='4'>
                        <div class="input-group userPicker" style="width:100%;">
                            <input id="leaderId" name="nextID" type="hidden" name="leader"
 		                                   value="${model.leader}" --%> 
                                   	>
                            <input type="text" id="leaderName" name="nextUser"  
 		                                   value="<tags:user userId="${model.leader}"></tags:user>"  --%>
                                   minlength="2" 
                                   maxlength="50" class="form-control" readOnly placeholder="点击后方图标即可选人">
                            <div id='leaderDiv'  class="input-group-addon"><i class="glyphicon glyphicon-user"></i>
                            </div>
                        </div>
                    </td>
              	</tr>
	</div>			