<%@page contentType="text/html;charset=UTF-8"%>
<c:if test="${isAudit=='1'}">
	<hr>
	<input id="url" type="hidden" name="url" value="/operationCustom/custom-detail.do?suspendStatus=custom">
	<div class="form-group">
		<label class="control-label col-md-1" for="orgInputUser_priority">受理单编号</label>
		<div class="col-sm-5" style="padding-top:8px;">
			<span id="spanApplyCode" style="">${code}</span>
                    <input id="applyCode" class="input_width" style="display:none;" 
                    		name="applyCode" value="${code}"  readonly>
		</div>
	</div>
	<div style="display:none;">
		<table>
			<tr>
		           <td>
		               <span id='tag_bustype'>&nbsp;<span style="color:Red">*</span>申请业务类型</span>：
		           </td>
		           <td>
		               <select name="busType" id="busType" class="form-control" style="border:none;">
		                   <option value="9999">自定义</option>
		               </select>
		               <input name="businessType" type="hidden" value="自定义" id="businessType"/>
		           </td>
		           <td>
		               <span id='tag_busDetails'>&nbsp;<span style="color:Red">*</span>业务细分</span>：
		           </td>
		           <td>
		               <select name="busDetails" id="busDetails" class="form-control" style="border:none;">
		                   <option value="8888">自定义申请</option>
		               </select>
		               <input name="businessDetail" type="hidden" value="自定义申请" id="businessDetail"/>
		           </td>
		       </tr>
	       </table>	
      </div>		
		<div class="form-group">
			<label class="control-label col-md-1" for="orgInputUser_priority"></label>
			<div class="col-sm-10" style="padding-top:8px;">
				<div style="color:red;">请按顺序选择审核人</div>
				<ul id="ulapprover" style="width:96%;margin:0 auto;list-style:none;">
                      </ul>
			</div>
		</div>	
		<div  class="form-group">	
			<label class="control-label col-md-1"><span style="color:red;">*</span>审批人</label>
			<div class="col-sm-10" style="padding-top:8px;">
				<div class="input-group userPickerTwo" style="width:100%;">
                          <input id="leaderId" name="nextID" type="hidden" name="leader">
                          <input type="text" id="leaderName" name="nextUser" 
                                 minlength="2"  maxlength="50" class="form-control required" readOnly placeholder="点击后方图标即可选人">
                          <div id='leaderDiv'  class="input-group-addon"><i class="glyphicon glyphicon-user"></i>
                          </div>
                      </div>
			</div>
		</div>	
</c:if>