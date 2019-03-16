package com.mossle.operation.web;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.alibaba.fastjson.JSON;
import com.mossle.api.custom.CustomConnector;
import com.mossle.api.form.FormDTO;
import com.mossle.api.humantask.HumanTaskConnector;
import com.mossle.api.humantask.HumanTaskConstants;
import com.mossle.api.humantask.HumanTaskDTO;
import com.mossle.api.keyvalue.FormParameter;
import com.mossle.api.keyvalue.KeyValueConnector;
import com.mossle.api.keyvalue.Prop;
import com.mossle.api.keyvalue.Record;
import com.mossle.api.operation.OperationConnector;
import com.mossle.api.process.ProcessConnector;
import com.mossle.api.store.StoreConnector;
import com.mossle.api.tenant.TenantHolder;
import com.mossle.api.user.UserConnector;
import com.mossle.base.persistence.domain.BusinessDetailEntity;
import com.mossle.base.persistence.domain.DetailPostEntity;
import com.mossle.base.persistence.manager.BusinessDetailManager;
import com.mossle.base.rs.BusinessResource.BusinessTypeDTO;
import com.mossle.button.ButtonDTO;
import com.mossle.button.ButtonHelper;
import com.mossle.common.utils.DeEnCode;
import com.mossle.common.utils.FileUploadAPI;
import com.mossle.common.utils.StringUtils;
import com.mossle.common.utils.WebAPI;
import com.mossle.core.MultipartHandler;
import com.mossle.core.auth.CurrentUserHolder;
import com.mossle.core.mapper.BeanMapper;
import com.mossle.core.mapper.JsonMapper;
import com.mossle.core.spring.MessageHelper;
import com.mossle.dict.persistence.domain.DictInfo;
import com.mossle.dict.support.DictConnectorImpl;
import com.mossle.humantask.persistence.domain.TaskInfo;
import com.mossle.humantask.persistence.manager.TaskInfoManager;
import com.mossle.internal.store.persistence.domain.StoreInfo;
import com.mossle.operation.persistence.domain.Apply;
import com.mossle.operation.persistence.domain.ApplyDTO;
import com.mossle.operation.persistence.domain.Business;
import com.mossle.operation.persistence.domain.CustomEntity;
import com.mossle.operation.persistence.domain.Exchange;
import com.mossle.operation.persistence.domain.Freeze;
import com.mossle.operation.persistence.domain.GroupBusiness;
import com.mossle.operation.persistence.domain.Invoice;
import com.mossle.operation.persistence.domain.LllegalFreeze;
import com.mossle.operation.persistence.manager.ApplyManager;
import com.mossle.operation.persistence.manager.BusinessManager;
import com.mossle.operation.persistence.manager.CustomApproverManager;
import com.mossle.operation.persistence.manager.CustomManager;
import com.mossle.operation.persistence.manager.ExchangeManager;
import com.mossle.operation.persistence.manager.FreezeManager;
import com.mossle.operation.persistence.manager.GroupBusinessManager;
import com.mossle.operation.persistence.manager.InvoiceManager;
import com.mossle.operation.persistence.manager.LllegalFreezeManager;
import com.mossle.operation.service.ApplyService;
import com.mossle.operation.service.CustomWorkService;
import com.mossle.operation.service.OperationService;
import com.mossle.user.persistence.domain.PersonInfo;
import com.mossle.user.persistence.domain.UpdatePerson;
import com.mossle.user.persistence.manager.UpdatePersonManager;
import com.mossle.util.DateUtil;
import com.mossle.util.StringUtil;
import com.mossle.xform.Xform;
import com.mossle.xform.XformBuilder;

/**
 * 任务工具条.
 * 
 * @author Lingo
 */
@Component
@Controller
@RequestMapping("operation")
//@Path("operation")
public class TaskOperationController {
	
    private static Logger logger = LoggerFactory
            .getLogger(TaskOperationController.class);
    
    public static final int STATUS_DRAFT_PROCESS = 0;
    public static final int STATUS_DRAFT_TASK = 1;
    public static final int STATUS_RUNNING = 2;
    
    private OperationService operationService;
    private KeyValueConnector keyValueConnector;
    private MessageHelper messageHelper;
    private CurrentUserHolder currentUserHolder;
    private HumanTaskConnector humanTaskConnector;
    private MultipartResolver multipartResolver;
    private StoreConnector storeConnector;
    private ButtonHelper buttonHelper = new ButtonHelper();
    private JsonMapper jsonMapper = new JsonMapper();
    private TenantHolder tenantHolder;
    private BeanMapper beanMapper = new BeanMapper();
    private UserConnector userConnector;
    private CustomManager customManager;
    private FileUploadAPI fileUploadAPI;
    private WebAPI webAPI;
    private ApplyManager applyManager;
    private InvoiceManager invoiceManager;
    private BusinessManager businessManager;
    private BusinessDetailManager businessDetailManager;
    private GroupBusinessManager groupBusinessManager;
    private FreezeManager freezeManager;
    private LllegalFreezeManager lllegalFreezeManager;
    private TaskInfoManager taskInfoManager;
    private DictConnectorImpl dictConnectorImpl;
    private CustomConnector customConnector;
    @Resource
    private HttpServletRequest request;
    @Resource
    private ExchangeManager exchangeManager;
    
    private UpdatePersonManager updatePersonManager ;
    @Autowired
    private CustomWorkService customWorkService;
    @Autowired
    private OperationConnector operationConnector;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private ApplyService applyService;
    
    /**
     * 保存草稿.
     */
    @RequestMapping("task-operation-saveDraft")
    public String saveDraft(HttpServletRequest request) throws Exception {
        this.doSaveRecord(request);

        return "operation/task-operation-saveDraft";
    }
    
    /**
     * 显示任务表单.
     */
    
//    @POST
//    @Path("task-operation-viewTaskForm")
    @RequestMapping("task-operation-viewTaskForm")
    public String viewTaskForm(@RequestParam("humanTaskId") String humanTaskId,HttpServletResponse response,
    		@RequestParam(value = "action", required = false) String action,
    		@RequestParam(value = "catalog", required = false) String catalog,
    		@RequestParam(value = "processInstanceId", required = false) String processInstanceId,
            Model model, RedirectAttributes redirectAttributes)
            throws Exception {
    	SimpleDateFormat sdf  = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	if (!StringUtils.isNumeric(humanTaskId)){
    		humanTaskId = DeEnCode.decode(humanTaskId);
        	processInstanceId = DeEnCode.decode(processInstanceId);
    	}
    	HumanTaskDTO humanTaskDto = humanTaskConnector
                .findHumanTask(humanTaskId);
        //判断当前任务的状态是否已撤回  2018-01-13 shijingxin
        //Record recordStatus = keyValueConnector.findByRef(processInstanceId);
       TaskInfo task = taskInfoManager.findUniqueBy("id", Long.parseLong(humanTaskId));
    	if(task == null||(humanTaskDto.getCompleteTime() == null&&humanTaskDto.getStatus().equals("complete"))){
        	String message = "该申请已被撤回，无法审批和查看详情，请通过新消息跳转。";  
        	StringBuffer sb = new StringBuffer();
            sb.append("<script language='javascript'>alert('");
            sb.append(message);
            sb.append("');self.location=document.referrer;</script>");
            response.setContentType("text/html; charset=utf-8");  
            String strHtml = "<body style='background-color: #D0D0D0'>" ;
            strHtml += "</body>";
            response.getWriter().println((strHtml));
            response.getWriter().println(sb.toString());
            response.getWriter().flush();
            return "msg/msg-info-view";
        	//return "redirect:/msg/msg-info-readView.do?id=949780482719744";
        }
        
        if (humanTaskDto == null) {
            messageHelper.addFlashMessage(redirectAttributes, "任务不存在");
            return "redirect:/humantask/workspace-personalTasks.do";
        }
        // 处理转发抄送任务，设置为已读
        if (HumanTaskConstants.CATALOG_COPY.equals(humanTaskDto.getCatalog())) {
            humanTaskDto.setStatus("complete");
            humanTaskDto.setCompleteTime(new Date());   // zyl 2017-07-26
            // humanTaskDto.setAction("read");
            humanTaskDto.setAction("已阅");
            humanTaskConnector.saveHumanTask(humanTaskDto);
        }
        // 如果已经处理，则跳转到详情页面||如果是抄送任务直接跳详情
        if (humanTaskDto.getCompleteTime() != null || humanTaskDto.getCatalog().equals("copy")) {
        	Record record = keyValueConnector.findByCode(humanTaskDto.getBusinessKey());
        	
        	String url = record.getUrl();
        	if (url.contains("?")) {
        		url = url + "&processInstanceId=" + humanTaskDto.getProcessInstanceId() +
            			"&isPrint=false&viewBack=true";
        	} else {
        		url = url + "?processInstanceId=" + humanTaskDto.getProcessInstanceId() +
            			"&isPrint=false&viewBack=true";
        	}
        	return "redirect:" + url;
        }
        
        //如果是自定义申请的审批步骤，直接跳转到审批表单
        // if(action.equals("自定义申请等待审批")){   // zyl 2017-11-14    ckx  2018/7/24
        if(humanTaskDto.getAction() != null && (humanTaskDto.getAction().equals("自定义申请等待审批") || humanTaskDto.getAction().equals("请假申请等待审批") || humanTaskDto.getAction().equals("加班申请等待审批") || humanTaskDto.getAction().equals("出差外出申请等待审批")|| humanTaskDto.getAction().equals("特殊考勤说明申请等待审批"))){
        	CustomEntity customEntity=customManager.get(Long.parseLong(humanTaskDto.getProcessInstanceId()));
        	String applyCode = customEntity.getApplyCode();
        	Long id = customEntity.getId();
        	
        	model.addAttribute("customId", id.toString());
        	model.addAttribute("humanTaskId", humanTaskId);
        	model.addAttribute("customEntity", customEntity);
        	//取附件
       	 	model.addAttribute("picUrl", webAPI.getViewUrl());
            List<StoreInfo> list = fileUploadAPI.getStore("operation/CustomApply", humanTaskDto.getProcessInstanceId());
            model.addAttribute("StoreInfos", list);
            
            //审批记录
            List<HumanTaskDTO> logHumanTaskDtos = humanTaskConnector
                    .findHumanTasksForPositionByProcessInstanceId(humanTaskDto.getProcessInstanceId());
            //获得审核时长
            logHumanTaskDtos = operationService.settingAuditDuration(logHumanTaskDtos);
            model.addAttribute("logHumanTaskDtos", logHumanTaskDtos);
            
            boolean isError=false;
            if(isError)
            	return "redirect:/error/error-info.do?error="
            			+java.net.URLEncoder.encode("您的账号对当前页面没有权限","utf-8");
            
            Map<String,Object> approverMap=customConnector.findAuditorHtml(humanTaskDto.getBusinessKey(), currentUserHolder.getUserId());
            model.addAttribute("approver",approverMap.get("approver"));
            model.addAttribute("auditorId",approverMap.get("auditorId"));
            model.addAttribute("auditorName",approverMap.get("auditorName"));
            model.addAttribute("isCanSelect",customConnector.checkCanSelectAuditor(humanTaskDto.getBusinessKey(), currentUserHolder.getUserId()));
            model.addAttribute("auditorSelectIds",approverMap.get("auditorSelectIds"));
            model.addAttribute("initUserId",customEntity.getUserId().toString());
            model.addAttribute("processInstanceId",processInstanceId);
            
          //判断若是花名册的流程，将添加人员或者修改花名册的标识取出来带回页面
        	String personTypeID = "";
        	UpdatePerson updatePerson=updatePersonManager.findUniqueBy("applyCode", applyCode);
      		String personInfoId = "";
      		String partyEntityId = "";
      		if(updatePerson!=null){
  				personTypeID = updatePerson.getTypeID();
  				if((updatePerson.getTypeID().equals("personadd")||updatePerson.getTypeID().equals("personUpdate"))){
	      			Object succesResponse = JSON.parse(updatePerson.getJsonContent());    //先转换成Object
					Map map = (Map)succesResponse;
					PersonInfo personInfo=jsonMapper.fromJson(map.get("personInfo").toString(),PersonInfo.class);
					personInfoId = personInfo.getId()==null?"": personInfo.getId().toString();
					partyEntityId = map.get("parentPartyEntityId")==null?"":map.get("parentPartyEntityId").toString();
					
	        	}
	      		else if(updatePerson!=null&&(updatePerson.getTypeID().equals("changePost"))){
	      			personInfoId = updatePerson.getEmployeeNo();
	      		}
      		}
      	
      		model.addAttribute("personInfoId",personInfoId);
      		model.addAttribute("partyEntityId",partyEntityId);
      		model.addAttribute("personTypeID", personTypeID);
            
            //ckx
            if(null != customEntity.getFormType() && !"".equals(customEntity.getFormType()) && !"null".equals(customEntity.getFormType())){
            	return "operation/custom/custom-work-apply-confirm";
            }else{
            	return "operation/custom-apply-confirm";
            }
        }

        //如果是被驳回到发起人，需要发起人重新调整表单，直接跳转到调整表单
        // if(action.equals("自定义申请等待调整")){ // zyl 2017-11-14   ckx  2018-7-24 humanTaskDto.getAction().equals("请假申请等待调整") ||humanTaskDto.getAction().equals("加班申请等待调整")
        if(humanTaskDto.getAction() != null && (humanTaskDto.getAction().equals("自定义申请等待调整") || humanTaskDto.getAction().equals("请假申请等待调整") || humanTaskDto.getAction().equals("加班申请等待调整") || humanTaskDto.getAction().equals("出差外出申请等待调整") || humanTaskDto.getAction().equals("特殊考勤说明申请等待调整"))){
        	CustomEntity customEntity=customManager.get(Long.parseLong(humanTaskDto.getProcessInstanceId()));
        	String applyCode = customEntity.getApplyCode();
        	
        	//判断若是花名册的流程，将添加人员或者修改花名册的标识取出来带回页面
        	String personTypeID = "";
        	UpdatePerson updatePerson=updatePersonManager.findUniqueBy("applyCode", applyCode);
      		String id = "";
      		String partyEntityId = "";
      		if(updatePerson!=null){
      			personTypeID = updatePerson.getTypeID();
      			if((updatePerson.getTypeID().equals("personadd")||updatePerson.getTypeID().equals("personUpdate"))){
      				Object succesResponse = JSON.parse(updatePerson.getJsonContent());    //先转换成Object
    				Map map = (Map)succesResponse;
    				PersonInfo personInfo=jsonMapper.fromJson(map.get("personInfo").toString(),PersonInfo.class);
    				id = personInfo.getId()==null?"": personInfo.getId().toString();
    				partyEntityId = map.get("parentPartyEntityId")==null?"":map.get("parentPartyEntityId").toString();
      			}
      			else if(updatePerson!=null&&(updatePerson.getTypeID().equals("changePost"))){
          			id = updatePerson.getEmployeeNo();
          		}
      			
      			if(updatePerson.getTypeID().equals("personadd")){
          			customEntity.setTheme(" [花名册]新员工录入");
          		}
      		}
      		
      		String ccnos = customEntity.getCcnos();
      		String ccName = customEntity.getCcName();
      		if(org.apache.commons.lang3.StringUtils.isNotBlank(ccnos)){
      			String str = ccnos.substring(ccnos.length()-1,ccnos.length() );
        		if(",".equals(str)){
        			ccnos = ccnos.substring(0, ccnos.length()-1);
        			customEntity.setCcnos(ccnos);
        		}
      		}
      		/*if(StringUtils.isNotBlank(ccName)){
      			String[] split = ccName.split(",");
      			for (int i = 0; i < split.length; i++) {
      				String str = split[i];
					if(str.contains("-")){
						String[] split2 = str.split("-");
						str = split2[split2.length-1];
						split[i] = str;
					}
				}
      		}*/
      		
      		model.addAttribute("personInfoId",id);
      		model.addAttribute("partyEntityId",partyEntityId);
      		model.addAttribute("personTypeID", personTypeID);
        	model.addAttribute("customEntity", customEntity);
        	model.addAttribute("humanTaskId", humanTaskId);
        	//取附件
       	 	model.addAttribute("picUrl", webAPI.getViewUrl());
            List<StoreInfo> list = fileUploadAPI.getStore("operation/CustomApply", humanTaskDto.getProcessInstanceId());
            model.addAttribute("StoreInfos", list);
            // 审批记录
            List<HumanTaskDTO> logHumanTaskDtos = humanTaskConnector
                    .findHumanTasksForPositionByProcessInstanceId(humanTaskDto.getProcessInstanceId());
            logHumanTaskDtos = operationService.settingAuditDuration(logHumanTaskDtos);
            model.addAttribute("logHumanTaskDtos", logHumanTaskDtos);
            
            Map<String,Object> approverMap=customConnector.findAuditorHtml(humanTaskDto.getBusinessKey(), currentUserHolder.getUserId());
            
            //ckx 显示所有的审核人，用于撤回后变更审核人
            model.addAttribute("allAuditorId",approverMap.get("allAuditorId"));
            model.addAttribute("allAuditorName",approverMap.get("allAuditorName"));
            
            model.addAttribute("approver",approverMap.get("approver"));
            model.addAttribute("auditorId",approverMap.get("auditorId"));
            model.addAttribute("auditorName",approverMap.get("auditorName"));
            model.addAttribute("isCanSelect",customConnector.checkCanSelectAuditor(humanTaskDto.getBusinessKey(), currentUserHolder.getUserId()));
            model.addAttribute("processInstanceId",processInstanceId);
            //ckx  判断跳转表单
            if(null != customEntity.getFormType() && !"".equals(customEntity.getFormType()) && !"null".equals(customEntity.getFormType())){
            	return "operation/custom/custom-work-apply-modify";
            }else{
            	return "operation/custom-apply-modify";
            }
            
        
        }
        
   //如果是自定义申请的抄送人阅读，直接跳转到阅读表单
    //List<TaskInfo> taskInfo = taskInfoManager.findBy("processInstanceId",humanTaskDto.getProcessInstanceId());
        
   	//for (TaskInfo getInfo : taskInfo) {
   		 
   	if( humanTaskDto.getCatalog().equals("copy")
   			&& (humanTaskDto.getSuspendStatus().equals("自定义申请")||
   					humanTaskDto.getSuspendStatus().equals("请假申请")||
   					humanTaskDto.getSuspendStatus().equals("加班申请"))||
   					humanTaskDto.getSuspendStatus().equals("出差外出申请")||
   					humanTaskDto.getSuspendStatus().equals("特殊考勤说明申请")){
   			
   		CustomEntity customEntity=customManager.get(Long.parseLong(humanTaskDto.getProcessInstanceId()));
        	model.addAttribute("customEntity", customEntity);
        	//取附件
       	 	model.addAttribute("picUrl", webAPI.getViewUrl());
            List<StoreInfo> list = fileUploadAPI.getStore("operation/CustomApply", humanTaskDto.getProcessInstanceId());
            model.addAttribute("StoreInfos", list);
            
            //审批记录
            List<HumanTaskDTO> logHumanTaskDtos = humanTaskConnector
                    .findHumanTasksForPositionByProcessInstanceId(humanTaskDto.getProcessInstanceId());
            logHumanTaskDtos = operationService.settingAuditDuration(logHumanTaskDtos);
            model.addAttribute("logHumanTaskDtos", logHumanTaskDtos);
            
            Map<String,Object> approverMap=customConnector.findAuditorHtml(humanTaskDto.getBusinessKey(), currentUserHolder.getUserId());
            
            model.addAttribute("approver",approverMap.get("approver"));
            model.addAttribute("processInstanceId",processInstanceId);
            if((humanTaskDto.getSuspendStatus().equals("请假申请")||
            		humanTaskDto.getSuspendStatus().equals("加班申请")||
   					humanTaskDto.getSuspendStatus().equals("出差外出申请")||
   					humanTaskDto.getSuspendStatus().equals("特殊考勤说明申请"))){
            	Record record = keyValueConnector.findByCode(humanTaskDto.getBusinessKey());
            	
            	String url = record.getUrl();
            	if (url.contains("?")) {
            		url = url + "&processInstanceId=" + humanTaskDto.getProcessInstanceId() +
                			"&isPrint=false&viewBack=true";
            	} else {
            		url = url + "?processInstanceId=" + humanTaskDto.getProcessInstanceId() +
                			"&isPrint=false&viewBack=true";
            	}
            	return "redirect:" + url;
            	//return "operation/custom/custom-work-apply-showForm";
            }
        	return "operation/custom-apply-cc";
        }
   	//}
        
        //表单
        FormDTO formDto = this.findTaskForm(humanTaskDto);
        //如果是外部表单，就直接跳转出去
        if (formDto.isRedirect()) {
        	 Record record = keyValueConnector.findByRef(humanTaskDto.getProcessInstanceId());//获取bpmProcessId
        	 
        	 if(record!=null){
        	
        		 Map<String, Prop> props = record.getProps();
                 String bpmProcessId = "";
                 if (props.containsKey("bpmProcessId")) {
                 	Prop prop = props.get("bpmProcessId");
                 	bpmProcessId = prop.getValue();
                 	int i=3;
                 			
                 }	
        	 }
             
//         return "redirect:/operation/task-operation-viewTaskOutForm.do?url=" + formDto.getUrl() 
//        		    + "&humanTaskId=" + formDto.getTaskId()
//        		    + "&catalog=" + humanTaskDto.getCatalog()
//        		    + "&processInstanceId=" + humanTaskDto.getProcessInstanceId()
//                    + "&processDefinitionId=" + formDto.getProcessDefinitionId() 
//                    + "&activityId=" + formDto.getActivityId()
//                    +"&bpmProcessId="+bpmProcessId
//                    + "&businessKey="+"";
         
         	model.addAttribute("humanTaskId", formDto.getTaskId());
	     	model.addAttribute("catalog", humanTaskDto.getCatalog());
	     	model.addAttribute("processInstanceId", humanTaskDto.getProcessInstanceId());
	     	model.addAttribute("processDefinitionId", formDto.getProcessDefinitionId());
	     	model.addAttribute("activityId", formDto.getActivityId());
	     	model.addAttribute("bpmProcessId", formDto.getActivityId());
	     	model.addAttribute("businessKey","");
	     	
	     	Long id = new Long(0) ;
	     	String url = formDto.getUrl() ;
	     	//得到体系
         	List<DictInfo> dictList=dictConnectorImpl.findDictInfoListByType("OwnedSystem", "1");
         	model.addAttribute("systemlist", dictList);
	    	
	    	// 审批记录
	        List<HumanTaskDTO> logHumanTaskDtos = humanTaskConnector
	                .findHumanTasksForPositionByProcessInstanceId(processInstanceId);
	        logHumanTaskDtos = operationService.settingAuditDuration(logHumanTaskDtos);
	        model.addAttribute("humanTaskId", humanTaskId);
	    	model.addAttribute("processInstanceId", processInstanceId);
	    	model.addAttribute("processDefinitionId",  formDto.getProcessDefinitionId());
	    	model.addAttribute("activityId", formDto.getActivityId());
	    	model.addAttribute("logHumanTaskDtos", logHumanTaskDtos);

			//常规/非常规
	    	if(url.equals("operation/common-operation-confirm") || url.equals("operation/common-operation-modify")){
	    		Apply apply = applyManager.findUniqueBy("processInstanceId",processInstanceId);
	    		if(apply==null){	    		messageHelper.addFlashMessage(redirectAttributes, "未查询到此流程数据！");
	            return "redirect:/humantask/workspace-personalTasks.do";
	    	}
	    		
	    		//获取修改电话的html by lilei at 2019.01.31
	    		String detailHtml="";
	    		String detailId="";
	    		String strSql=String.format("select business_key,businessDetailId from kv_record where applycode='%s'", apply.getApplyCode());
                List<Map<String,Object>> mapRecordList=jdbcTemplate.queryForList(strSql);
	    		if(mapRecordList.size()>0){
	    			detailId=mapRecordList.get(0).get("businessDetailId").toString();
		    		Map<String,Object> mapDetail=applyService.getEditPhoneDetailId();
				     if(mapDetail.containsKey("businessDetailId")){
				    	 if(mapDetail.get("businessDetailId")!=null){
				    		 String businessDetailId=mapDetail.get("businessDetailId").toString();
				    		 if(!StringUtils.isBlank(businessDetailId)){
				    			 if(businessDetailId.equals(detailId)){
				    				 List<Map<String,Object>> mapSystemList=(List<Map<String,Object>>)mapDetail.get("systemList");
				    				 detailHtml+="<tr>"
					    			          		+"<td class=\"f_td\">需修改系统：</td>";
						             detailHtml+="<td style=\"text-align:left;\" class=\"f_r_td\" colspan=\"7\">";
						             if(mapSystemList.size()>0){
						            	 if(url.equals("operation/common-operation-modify")){
						            		 for (Map<String, Object> mapSystem: mapSystemList) {
							            		 detailHtml+="&emsp;<input type=\"checkbox\" name=\"fileName\" ";
							            		 if(apply.getFileName()!=null&&apply.getFileName().contains(mapSystem.get("value").toString())){
							            			 detailHtml+=" checked ";
							            			 detailHtml+=" value=\""+mapSystem.get("value")+"\"/>&nbsp;<span>"+mapSystem.get("title")+"</span>";
	        }
							            		 else
							            			 detailHtml+=" value=\""+mapSystem.get("value")+"\"/>&nbsp;<span>"+mapSystem.get("title")+"</span>";
											 }
						            	 }
						            	 else{
						            		 for (Map<String, Object> mapSystem: mapSystemList) {
							            		 detailHtml+="&emsp;<input type=\"checkbox\" name=\"fileName\" disabled";
							            		 if(apply.getFileName()!=null&&apply.getFileName().contains(mapSystem.get("value").toString())){
							            			 detailHtml+=" checked ";
							            			 detailHtml+=" value=\""+mapSystem.get("value")+"\"/>&nbsp;<span style=\"color:red\">"+mapSystem.get("title")+"</span>";
							            		 }
							            		 else
							            			 detailHtml+=" value=\""+mapSystem.get("value")+"\"/>&nbsp;<span>"+mapSystem.get("title")+"</span>";
											 } 
						            	 }
						             }
						             detailHtml+="</td>"
				    				            +"</tr>";
				    			 }
				    		 }
				    	 }
				     }
	    		}
	    		model.addAttribute("detailHtml", detailHtml);
	        
	    		id=apply.getId();
	        //取附件
	    	 model.addAttribute("picUrl", webAPI.getViewUrl());
	         List<StoreInfo> list = fileUploadAPI.getStore("operation/commApply", Long.toString(id));
	         model.addAttribute("StoreInfos", list);
	    	}
	    	
	    	if(url.equals("operation/process/InvoiceApprovalForm") || url.equals("operation/process/InvoiceAdjustmentForm")){
	    		 Invoice invoice = invoiceManager.findUniqueBy("processInstanceId", processInstanceId);
	    		 if (invoice == null) {
	    	            messageHelper.addFlashMessage(redirectAttributes, "未查询到此流程数据！");
	    	            return "redirect:/humantask/workspace-personalTasks.do";
	    	     }
	        	 id = invoice.getId();
	             //取附件
	             model.addAttribute("picUrl", webAPI.getViewUrl());
	             List<StoreInfo> invoiceList = fileUploadAPI.getStore("OA/process", Long.toString(id));
	             model.addAttribute("StoreInfos", invoiceList);
	           
	    	 }
	    	if(url.equals("operation/process/BusinessApprovalForm") || url.equals("operation/process/BusinessAdjustmentForm")){
	    		Business business = businessManager.findUniqueBy("processInstanceId", processInstanceId);
	    		if (business == null) {
    	            messageHelper.addFlashMessage(redirectAttributes, "未查询到此流程数据");
    	            return "redirect:/humantask/workspace-personalTasks.do";
    	        }
	    		//ckx  2019/9/10  
	    		String copyUserValue = business.getCopyUserValue();
	    		if(org.apache.commons.lang3.StringUtils.isNotBlank(copyUserValue)){
	    			String str = copyUserValue.substring(copyUserValue.length()-1,copyUserValue.length() );
	        		if(",".equals(str)){
	        			copyUserValue = copyUserValue.substring(0, copyUserValue.length()-1);
	        		}
	        		business.setCopyUserValue(copyUserValue);
	    		}
	    		//判断是否大区表单   ckx 2018/1/28
	        	String countSql = "select count(*) from kv_record_condition c left join kv_record k on k.BUSINESS_KEY = c.businessKey where k.ref = ? and conditionName = 'region' and conditionValue = 'region'";
	        	int count = jdbcTemplate.queryForObject(countSql, Integer.class,processInstanceId );
	        	boolean isArea = false;
	        	if(count > 0){
	        		isArea = true;
	        	}
	        	
	        	String title = "";
	        	Record findByRef = keyValueConnector.findByRef(processInstanceId);
	        	BusinessDetailEntity businessDetailEntity=businessDetailManager.get(Long.parseLong(findByRef.getBusinessDetailId()));
	        	if(null != businessDetailEntity){
	        		title = businessDetailEntity.getTitle();
	        	}
	        	model.addAttribute("isArea", isArea);
	        	model.addAttribute("title", title);
	    		model.addAttribute("business", business);
				id = business.getId();
	    		//取附件
	    		model.addAttribute("picUrl", webAPI.getViewUrl());
	    		List<StoreInfo> businessList = fileUploadAPI.getStore("OA/process", Long.toString(id));
	    		model.addAttribute("StoreInfos", businessList);
	    	}
	    	if(url.equals("operation/process/GroupBusinessApprovalForm") || url.equals("operation/process/GroupBusinessAdjustmentForm")){
	    		GroupBusiness groupBusiness = groupBusinessManager.findUniqueBy("processInstanceId", processInstanceId);
	    		if (groupBusiness == null) {
    	            messageHelper.addFlashMessage(redirectAttributes, "未查询到此流程数据");
    	            return "redirect:/humantask/workspace-personalTasks.do";
    	        }
	    		//TODO sjx 18.12.10 将该流程的申请人id传给前台 作为参数请求多分支环节
	    		Long applyUserId = groupBusiness.getUserId();
	    		model.addAttribute("applyUserId", applyUserId);
	    		//添加流分支条件-金额条件 add by lilei at 2018.11.12
	    		Map<String,Object> mapCondition=operationConnector.getProcessIsMoneyByBusinessKey(task.getBusinessKey());
	    		boolean boolIsMoney=Boolean.parseBoolean(mapCondition.get("ismoney").toString());
    			model.addAttribute("ismoney", boolIsMoney?"1":"0");
    			model.addAttribute("money",mapCondition.get("conditionValue"));
    			    			
    			String strSql=String.format("select business_key,businessDetailId from kv_record where applycode='%s'", groupBusiness.getApplyCode());
                List<Map<String,Object>> mapRecordList=jdbcTemplate.queryForList(strSql);
                String title="";
                String strLevel="";
                String isShowAuditStep="1";
                if(mapRecordList.size()>0){
                	String businessDetailId=mapRecordList.get(0).get("businessDetailId").toString();
                	model.addAttribute("businessDetailId", businessDetailId);//将细分id传给前台 作为前台请求多分支审核步骤的参数
                	String processTitle=operationConnector.getProcessTitle(businessDetailId);
                	// ckx 2019/2/19
                	if(StringUtils.isNotBlank(processTitle)){
                		title=processTitle;
                	}
                	
                	List<Map<String,Object>> mapAuditCondition=operationConnector.getBranchConditionList(businessDetailId);
                	if(mapAuditCondition!=null&&mapAuditCondition.size()>0)
                		isShowAuditStep="0";
                	
                	BusinessDetailEntity businessDetailEntity=businessDetailManager.get(Long.parseLong(businessDetailId));
                	if(businessDetailEntity!=null)
                		strLevel=businessDetailEntity.getLevel();
                }
                model.addAttribute("bpmProcessTitle", title);
                model.addAttribute("isShowAuditStep", isShowAuditStep);
                model.addAttribute("level", strLevel);
				
	    		//ckx  2019/9/10  
	    		String copyUserValue = groupBusiness.getCopyUserValue();
	    		if(org.apache.commons.lang3.StringUtils.isNotBlank(copyUserValue)){
	    			String str = copyUserValue.substring(copyUserValue.length()-1,copyUserValue.length() );
	        		if(",".equals(str)){
	        			copyUserValue = copyUserValue.substring(0, copyUserValue.length()-1);
	        		}
	        		groupBusiness.setCopyUserValue(copyUserValue);
	    		}
	    		model.addAttribute("groupBusiness", groupBusiness);
				id = groupBusiness.getId();
	    		//取附件
	    		model.addAttribute("picUrl", webAPI.getViewUrl());
	    		List<StoreInfo> businessList = fileUploadAPI.getStore("OA/process", Long.toString(id));
	    		model.addAttribute("StoreInfos", businessList);
	    	}
	    	if(url.equals("operation/process/FreezeApprovalForm") || url.equals("operation/process/FreezeAdjustmentForm")){
	    		Freeze freeze = freezeManager.findUniqueBy("processInstanceId", processInstanceId);
	    		if (freeze == null) {
    	            messageHelper.addFlashMessage(redirectAttributes, "未查询到此流程数据");
    	            return "redirect:/humantask/workspace-personalTasks.do";
    	        }
	    		model.addAttribute("freeze", freeze);
				id = freeze.getId();
	    		//取附件
	    		model.addAttribute("picUrl", webAPI.getViewUrl());
	    		List<StoreInfo> freezeList = fileUploadAPI.getStore("OA/process", Long.toString(id));
	    		model.addAttribute("StoreInfos", freezeList);
	    	}
	    	if(url.equals("operation/process/LllegalFreezeApprovalForm") || url.equals("operation/process/LllegalFreezeAdjustmentForm")){
	    		LllegalFreeze lllegalFreeze = lllegalFreezeManager.findUniqueBy("processInstanceId", processInstanceId);
	    		if (lllegalFreeze == null) {
    	            messageHelper.addFlashMessage(redirectAttributes, "未查询到此流程数据");
    	            return "redirect:/humantask/workspace-personalTasks.do";
    	        }
	    		//ckx  2019/9/10  
	    		String copyUserValue = lllegalFreeze.getCopyUserValue();
	    		if(org.apache.commons.lang3.StringUtils.isNotBlank(copyUserValue)){
	    			String str = copyUserValue.substring(copyUserValue.length()-1,copyUserValue.length() );
	        		if(",".equals(str)){
	        			copyUserValue = copyUserValue.substring(0, copyUserValue.length()-1);
	        		}
	        		lllegalFreeze.setCopyUserValue(copyUserValue);
	    		}
	    		model.addAttribute("lllegalFreeze", lllegalFreeze);
				id = lllegalFreeze.getId();
	    		//取附件
	    		model.addAttribute("picUrl", webAPI.getViewUrl());
	    		List<StoreInfo> lllegalFreezeList = fileUploadAPI.getStore("OA/process", Long.toString(id));
	    		model.addAttribute("StoreInfos", lllegalFreezeList);
	    	}
	    	if(url.equals("operation/process/ExchangeApprovalForm") || url.equals("operation/process/ExchangeAdjustmentForm")|| url.equals("operation/quality-exchange-confirm")|| url.equals("operation/quality-exchange-modify")){
	    		Exchange exchange = exchangeManager.findUniqueBy("processInstanceId", processInstanceId);
	    		if(exchange != null){
	    			id = exchange.getId();
	    			Record recordByRef = keyValueConnector.findByRef(processInstanceId);
	    			String detailHtml = recordByRef.getDetailHtml();
	    			//获取抄送人 ckx   add 2018/9/8
	    		    String taskCopyNames = "";
	    		    taskCopyNames = customWorkService.getTaskCopyNames(processInstanceId);
	    		    detailHtml = detailHtml.replace("{copyNames}", taskCopyNames);
	    		    detailHtml = detailHtml.replace("{exchangeTable}", "换货审批单");
	    		    detailHtml = detailHtml.replace("{qualityExchangeTable}", "质量换货审批单");
	    			model.addAttribute("detailHtml", detailHtml);
	    			if(detailHtml.contains("<h2>质量换货审批单</h2>")){
	    				model.addAttribute("resource", 1);
	    			}else{
	    				model.addAttribute("resource", 0);
	    			}
	    			//将值传给质量换货
	    			model.addAttribute("copyNames", taskCopyNames);
	    		}else{
    	            messageHelper.addFlashMessage(redirectAttributes, "未查询到此流程数据");
    	            return "redirect:/humantask/workspace-personalTasks.do";
	    		}
	    		
	    		//取附件
	    		model.addAttribute("picUrl", webAPI.getViewUrl());
	    		List<StoreInfo> exchangeStoreInfo = fileUploadAPI.getStore("OA/process", Long.toString(id));
	    		model.addAttribute("StoreInfos", exchangeStoreInfo);
	    	}
            
         
	    	return formDto.getUrl();
         
         
        }

        model.addAttribute("formDto", formDto);
        model.addAttribute("humanTaskId", humanTaskId);
        model.addAttribute("humanTask", humanTaskDto);

        if (humanTaskDto.getParentId() != null) {
            model.addAttribute("parentHumanTask", humanTaskConnector
                    .findHumanTask(humanTaskDto.getParentId()));
        }

        // 表单和数据
        if ((humanTaskId != null) && (!"".equals(humanTaskId))) {
            // 如果是任务草稿，直接通过processInstanceId获得record，更新数据
            // TODO: 分支肯定有问题
            // String processInstanceId = humanTaskDto.getProcessInstanceId();
            String json = this.findTaskFormData(humanTaskDto.getProcessInstanceId());

            if (json != null) {
                model.addAttribute("json", json);
            }

            Record record = keyValueConnector.findByRef(humanTaskDto.getProcessInstanceId());

            Xform xform = new XformBuilder().setStoreConnector(storeConnector)
                    .setUserConnector(userConnector)
                    .setContent(formDto.getContent()).setRecord(record).build();
            model.addAttribute("xform", xform);
        }

        // 操作
        List<ButtonDTO> buttons = new ArrayList<ButtonDTO>();

        for (String button : formDto.getButtons()) {
            buttons.add(buttonHelper.findButton(button.trim()));
        }

        if (buttons.isEmpty()) {
            buttons.add(buttonHelper.findButton("saveDraft"));
            buttons.add(buttonHelper.findButton("completeTask"));
        }

        model.addAttribute("buttons", buttons);

        // 沟通
        List<HumanTaskDTO> children = humanTaskConnector
                .findSubTasks(humanTaskId);
        model.addAttribute("children", children);

        // 审批记录
        List<HumanTaskDTO> logHumanTaskDtos = humanTaskConnector
                .findHumanTasksForPositionByProcessInstanceId(humanTaskDto
                        .getProcessInstanceId());
        model.addAttribute("logHumanTaskDtos", logHumanTaskDtos);

        return "operation/task-operation-viewTaskForm";
    }
    
    /**
     * 跳转到外部表单.
     */
    @RequestMapping("task-operation-viewTaskOutForm")
    public String viewTaskOutForm(
            Model model,
            @RequestParam(value = "url", required = false) String url,
            @RequestParam(value = "humanTaskId", required = false) String humanTaskId,
            @RequestParam(value = "processInstanceId", required = false) String processInstanceId,
            @RequestParam(value = "processDefinitionId", required = false) String processDefinitionId,
            @RequestParam(value = "activityId", required = false) String activityId,
            RedirectAttributes redirectAttributes)
            throws Exception {
    	
    	Long id = new Long(0) ;
    	
    	//得到体系
    	List<DictInfo> dL=dictConnectorImpl.findDictInfoListByType("OwnedSystem", "1");
    	model.addAttribute("systemlist", dL);
    	
    	// 审批记录
        List<HumanTaskDTO> logHumanTaskDtos = humanTaskConnector
                .findHumanTasksForPositionByProcessInstanceId(processInstanceId);
        model.addAttribute("humanTaskId", humanTaskId);
    	model.addAttribute("processInstanceId", processInstanceId);
    	model.addAttribute("processDefinitionId", processDefinitionId);
    	model.addAttribute("activityId", activityId);
    	model.addAttribute("logHumanTaskDtos", logHumanTaskDtos);

    	//取表单的主键ID ，用来取附件
    	List<Apply> apply = applyManager.findBy("processInstanceId",processInstanceId);
    	List<ApplyDTO> applyDtos = new ArrayList<ApplyDTO>();
        for (Apply getInfo : apply) {
        	id = getInfo.getId();
        }
        
        //取附件
    	 model.addAttribute("picUrl", webAPI.getViewUrl());
         List<StoreInfo> list = fileUploadAPI.getStore("operation/commApply", Long.toString(id));
         model.addAttribute("StoreInfos", list);
    	
    	if(url.equals("operation/process/InvoiceApprovalForm") || url.equals("operation/process/InvoiceAdjustmentForm")){
    		 Invoice invoice = invoiceManager.findUniqueBy("processInstanceId", processInstanceId);
        	 id = invoice.getId();
             //取附件
             model.addAttribute("picUrl", webAPI.getViewUrl());
             List<StoreInfo> invoiceList = fileUploadAPI.getStore("OA/process", Long.toString(id));
             model.addAttribute("StoreInfos", invoiceList);
           //得到体系
         	List<DictInfo> dictList=dictConnectorImpl.findDictInfoListByType("OwnedSystem", "1");
         	model.addAttribute("systemlist", dictList);
    	 }
    	if(url.equals("operation/process/BusinessApprovalForm") || url.equals("operation/process/BusinessAdjustmentForm")
    		||url.equals("operation/process/AreaBusinessApprovalForm") || url.equals("operation/process/AreaBusinessAdjustmentForm")){
    		Business business = businessManager.findUniqueBy("processInstanceId", processInstanceId);
    		model.addAttribute("business", business);
			id = business.getId();
    		//取附件
    		model.addAttribute("picUrl", webAPI.getViewUrl());
    		List<StoreInfo> businessList = fileUploadAPI.getStore("OA/process", Long.toString(id));
    		model.addAttribute("StoreInfos", businessList);
    	}
    	if(url.equals("operation/process/GroupBusinessApprovalForm") || url.equals("operation/process/GroupBusinessAdjustmentForm")){
    		
    		TaskInfo task = taskInfoManager.findUniqueBy("id", Long.parseLong(humanTaskId));
    		//添加流分支条件-金额条件 add by lilei at 2018.11.12 
    		Map<String,Object> mapCondition=operationConnector.getProcessIsMoneyByBusinessKey(task.getBusinessKey());
    		boolean boolIsMoney=Boolean.parseBoolean(mapCondition.get("ismoney").toString());
			model.addAttribute("ismoney", boolIsMoney?"1":"0");
			model.addAttribute("money",mapCondition.get("conditionValue"));
			
			GroupBusiness groupBusiness = groupBusinessManager.findUniqueBy("processInstanceId", processInstanceId);
			String strSql=String.format("select business_key,businessDetailId from kv_record where applycode='%s'", groupBusiness.getApplyCode());
            List<Map<String,Object>> mapRecordList=jdbcTemplate.queryForList(strSql);
            
            String title="业务审批单";
            String strLevel="";
            String isShowAuditStep="1";
            if(mapRecordList.size()>0){
            	String businessDetailId=mapRecordList.get(0).get("businessDetailId").toString();
            	String processTitle=operationConnector.getProcessTitle(businessDetailId);
            	if(!processTitle.equals("")){
            		title=processTitle;
            	}
            	
            	List<Map<String,Object>> mapAuditCondition=operationConnector.getBranchConditionList(businessDetailId);
            	if(mapAuditCondition!=null&&mapAuditCondition.size()>0)
            		isShowAuditStep="0";
            	
            	BusinessDetailEntity businessDetailEntity=businessDetailManager.get(Long.parseLong(businessDetailId));
            	if(businessDetailEntity!=null)
            		strLevel=businessDetailEntity.getLevel();
            }
            model.addAttribute("bpmProcessTitle", title);
            model.addAttribute("isShowAuditStep", isShowAuditStep);
            model.addAttribute("level", strLevel);
    		
    		model.addAttribute("groupBusiness", groupBusiness);
			id = groupBusiness.getId();
    		//取附件
    		model.addAttribute("picUrl", webAPI.getViewUrl());
    		List<StoreInfo> businessList = fileUploadAPI.getStore("OA/process", Long.toString(id));
    		model.addAttribute("StoreInfos", businessList);
    	}
    	if(url.equals("operation/process/FreezeApprovalForm") || url.equals("operation/process/FreezeAdjustmentForm")){
    		Freeze freeze = freezeManager.findUniqueBy("processInstanceId", processInstanceId);
    		model.addAttribute("freeze", freeze);
			id = freeze.getId();
    		//取附件
    		model.addAttribute("picUrl", webAPI.getViewUrl());
    		List<StoreInfo> freezeList = fileUploadAPI.getStore("OA/process", Long.toString(id));
    		model.addAttribute("StoreInfos", freezeList);
    		//得到体系
         	List<DictInfo> dictList=dictConnectorImpl.findDictInfoListByType("OwnedSystem", "1");
         	model.addAttribute("systemlist", dictList);
    	}
    	if(url.equals("operation/process/LllegalFreezeApprovalForm") || url.equals("operation/process/LllegalFreezeAdjustmentForm")){
    		LllegalFreeze lllegalFreeze = lllegalFreezeManager.findUniqueBy("processInstanceId", processInstanceId);
    		model.addAttribute("lllegalFreeze", lllegalFreeze);
			id = lllegalFreeze.getId();
    		//取附件
    		model.addAttribute("picUrl", webAPI.getViewUrl());
    		List<StoreInfo> lllegalFreezeList = fileUploadAPI.getStore("OA/process", Long.toString(id));
    		model.addAttribute("StoreInfos", lllegalFreezeList);
    		//得到体系
         	List<DictInfo> dictList=dictConnectorImpl.findDictInfoListByType("OwnedSystem", "1");
         	model.addAttribute("systemlist", dictList);
    	}
        
         
        return url;
       
    }
    
    /**
     * 完成任务.
     */
    @RequestMapping("task-operation-completeTask")
    public String completeTask(HttpServletRequest request,
            RedirectAttributes redirectAttributes) throws Exception {
        String userId = currentUserHolder.getUserId();
        String tenantId = tenantHolder.getTenantId();
        MultipartHandler multipartHandler = new MultipartHandler(
                multipartResolver);
        Record record = null;
        String humanTaskId = null;
        FormParameter formParameter = null;
        HumanTaskDTO humanTaskDto = null;
        FormDTO formDto = null;

        try {
            multipartHandler.handle(request);
            logger.debug("getMultiValueMap : {}",
                    multipartHandler.getMultiValueMap());
            logger.debug("getMultiFileMap : {}",
                    multipartHandler.getMultiFileMap());

            formParameter = this.buildFormParameter(multipartHandler);

            humanTaskId = formParameter.getHumanTaskId();
            operationService.saveDraft(userId, tenantId, formParameter);

            formDto = humanTaskConnector.findTaskForm(humanTaskId);

            humanTaskDto = humanTaskConnector.findHumanTask(humanTaskId);
            
            record = keyValueConnector.findByCode(formParameter.getBusinessKey());
            // TODO zyl 2017-11-16 外部表单不需要保存prop
            /*String processInstanceId = humanTaskDto.getProcessInstanceId();
            record = keyValueConnector.findByRef(processInstanceId);

            record = new RecordBuilder().build(record, multipartHandler,
                    storeConnector, tenantId);

            keyValueConnector.save(record);*/
        } finally {
            multipartHandler.clear();
        }

        Xform xform = new XformBuilder().setStoreConnector(storeConnector)
                .setUserConnector(userConnector)
                .setContent(formDto.getContent()).setRecord(record).build();
        Map<String, Object> taskParameters = xform.getMapData();
        logger.info("taskParameters : {}", taskParameters);

        try {
            // humanTaskConnector.completeTask(humanTaskId,
            // currentUserHolder.getUserId(), formParameter.getAction(),
            // formParameter.getComment(), taskParameters);
            this.operationService.completeTask(humanTaskId, userId,
                    formParameter, taskParameters, record,
                    humanTaskDto.getProcessInstanceId());
        } catch (IllegalStateException ex) {
            logger.error(ex.getMessage(), ex);
            messageHelper.addFlashMessage(redirectAttributes, ex.getMessage());

            return "redirect:/humantask/workspace-personalTasks.do";
        }

        // if (record == null) {
        // record = new Record();
        // }
        // record = new RecordBuilder().build(record, STATUS_RUNNING,
        // humanTaskDto.getProcessInstanceId());
        // keyValueConnector.save(record);
        return "operation/task-operation-completeTask";
    }

    /**
     * 领取任务.
     */
    @RequestMapping("task-operation-claimTask")
    public String claimTask(@RequestParam("humanTaskId") String humanTaskId) {
        String userId = currentUserHolder.getUserId();
        humanTaskConnector.claimTask(humanTaskId, userId);

        return "redirect:/humantask/workspace-personalTasks.do";
    }

    /**
     * 释放任务.
     */
    @RequestMapping("task-operation-releaseTask")
    public String releaseTask(@RequestParam("humanTaskId") String humanTaskId) {
        humanTaskConnector.releaseTask(humanTaskId, "");

        return "redirect:/humantask/workspace-personalTasks.do";
    }

    /**
     * 回退任务，前一个任务.
     */
    @RequestMapping("task-operation-rollbackPrevious")
    public String rollbackPrevious(
            @RequestParam("humanTaskId") String humanTaskId) {
        humanTaskConnector.rollbackPrevious(humanTaskId, "");

        return "redirect:/humantask/workspace-personalTasks.do";
    }

    /**
     * 回退任务，开始事件.
     */
    @RequestMapping("task-operation-rollbackStart")
    public String rollbackStart(@RequestParam("humanTaskId") String humanTaskId) {
        humanTaskConnector.rollbackStart(humanTaskId, "");

        return "redirect:/humantask/workspace-personalTasks.do";
    }

    /**
     * 回退任务，发起人.
     */
    @RequestMapping("task-operation-rollbackInitiator")
    public String rollbackInitiator(
            @RequestParam("humanTaskId") String humanTaskId,
            @RequestParam("_humantask_comment_") String comment) {
        humanTaskConnector.rollbackInitiator(humanTaskId, comment);

        return "redirect:/humantask/workspace-personalTasks.do";
    }

    /**
     * 撤销任务.
     */
    @RequestMapping("task-operation-withdraw")
    public String withdraw(@RequestParam("humanTaskId") String humanTaskId,
            @RequestParam("comment") String comment) {
        humanTaskConnector.withdraw(humanTaskId, comment);

        return "redirect:/humantask/workspace-personalTasks.do";
    }

    /**
     * 转办.
     */
    @RequestMapping("task-operation-transfer")
    public String transfer(@RequestParam("humanTaskId") String humanTaskId,
            @RequestParam("userId") String userId,
            @RequestParam("comment") String comment) {
        humanTaskConnector.transfer(humanTaskId, userId, comment);

        return "redirect:/humantask/workspace-delegatedTasks.do";
    }

    /**
     * 取消.
     */
    @RequestMapping("task-operation-cancel")
    public String cancel(@RequestParam("humanTaskId") String humanTaskId,
            @RequestParam("userId") String userId,
            @RequestParam("comment") String comment) {
        humanTaskConnector.cancel(humanTaskId, userId, comment);

        return "redirect:/humantask/workspace-personalTasks.do";
    }

    /**
     * 协办.
     */
    @RequestMapping("task-operation-delegateTask")
    public String delegateTask(@RequestParam("humanTaskId") String humanTaskId,
            @RequestParam("userId") String userId,
            @RequestParam("comment") String comment) {
        humanTaskConnector.delegateTask(humanTaskId, userId, comment);

        return "redirect:/humantask/workspace-delegatedTasks.do";
    }

    /**
     * 链状协办.
     */
    @RequestMapping("task-operation-delegateTaskCreate")
    public String delegateTaskCreate(
            @RequestParam("humanTaskId") String humanTaskId,
            @RequestParam("userId") String userId,
            @RequestParam("comment") String comment) {
        humanTaskConnector.delegateTaskCreate(humanTaskId, userId, comment);

        return "redirect:/humantask/workspace-delegatedTasks.do";
    }

    /**
     * 沟通.
     */
    @RequestMapping("task-operation-communicate")
    public String communicate(@RequestParam("humanTaskId") String humanTaskId,
            @RequestParam("userId") String userId,
            @RequestParam("comment") String comment) {
        logger.info(
                "communicate : humanTaskId : {}, userId : {}, comment : {}",
                humanTaskId, userId, comment);
        humanTaskConnector.communicate(humanTaskId, userId, comment);

        return "redirect:/humantask/workspace-personalTasks.do";
    }

    /**
     * 反馈.
     */
    @RequestMapping("task-operation-callback")
    public String callback(@RequestParam("humanTaskId") String humanTaskId,
            @RequestParam("comment") String comment) {
        humanTaskConnector.callback(humanTaskId, "", comment);

        return "redirect:/humantask/workspace-personalTasks.do";
    }

    /**
     * 加签.
     */
    @RequestMapping("task-operation-createVote")
    public String createVote(@RequestParam("humanTaskId") String humanTaskId,
            @RequestParam("userIds") String userIds,
            @RequestParam("comment") String comment) {
        HumanTaskDTO parentTask = humanTaskConnector.findHumanTask(humanTaskId);
        parentTask.setOwner(parentTask.getAssignee());
        parentTask.setAssignee("");
        humanTaskConnector.saveHumanTask(parentTask, false);

        for (String userId : userIds.split(",")) {
            HumanTaskDTO childTask = humanTaskConnector.createHumanTask();
            // copy
            childTask.setName(parentTask.getName());
            childTask.setPresentationSubject(parentTask
                    .getPresentationSubject());
            childTask.setForm(parentTask.getForm());
            childTask.setProcessInstanceId(parentTask.getProcessInstanceId());
            childTask.setProcessDefinitionId(parentTask
                    .getProcessDefinitionId());
            childTask.setTaskId(parentTask.getTaskId());
            childTask.setCode(parentTask.getCode());
            childTask.setBusinessKey(parentTask.getBusinessKey());
            childTask.setTenantId(parentTask.getTenantId());
            childTask.setStatus("active");
            childTask.setParentId(humanTaskId);
            childTask.setAssignee(userId);
            childTask.setCatalog(HumanTaskConstants.CATALOG_VOTE);
            humanTaskConnector.saveHumanTask(childTask, false);
        }

        return "redirect:/humantask/workspace-personalTasks.do";
    }

    // ~ ======================================================================
    /**
     * 通过multipart请求构建formParameter.
     */
    public FormParameter buildFormParameter(MultipartHandler multipartHandler) {
        FormParameter formParameter = new FormParameter();
        formParameter.setMultiValueMap(multipartHandler.getMultiValueMap());
        formParameter.setMultiFileMap(multipartHandler.getMultiFileMap());
        formParameter.setBusinessKey(multipartHandler.getMultiValueMap()
                .getFirst("businessKey"));
        formParameter.setBpmProcessId(multipartHandler.getMultiValueMap()
                .getFirst("bpmProcessId"));
        formParameter.setHumanTaskId(multipartHandler.getMultiValueMap()
                .getFirst("humanTaskId"));
        formParameter.setComment(multipartHandler.getMultiValueMap().getFirst(
                "_humantask_comment_"));
        formParameter.setAction(multipartHandler.getMultiValueMap().getFirst(
                "_humantask_action_"));

        return formParameter;
    }

    /**
     * 把数据先保存到keyvalue里.
     */
    public FormParameter doSaveRecord(HttpServletRequest request)
            throws Exception {
        String userId = currentUserHolder.getUserId();
        String tenantId = tenantHolder.getTenantId();
        MultipartHandler multipartHandler = new MultipartHandler(
                multipartResolver);
        FormParameter formParameter = null;

        try {
            multipartHandler.handle(request);
            logger.info("{}", multipartHandler.getMultiValueMap());
            logger.info("{}", multipartHandler.getMultiFileMap());

            formParameter = this.buildFormParameter(multipartHandler);

            String businessKey = operationService.saveDraft(userId, tenantId,
                    formParameter);

            if ((formParameter.getBusinessKey() == null)
                    || "".equals(formParameter.getBusinessKey().trim())) {
                formParameter.setBusinessKey(businessKey);
            }
            // TODO zyl 2017-11-16 外部表单不需要保存prop
            /*Record record = keyValueConnector.findByCode(businessKey);

            record = new RecordBuilder().build(record, multipartHandler,
                    storeConnector, tenantId);

            keyValueConnector.save(record);*/
        } finally {
            multipartHandler.clear();
        }

        return formParameter;
    }

    /**
     * 读取任务对应的表单数据，转换成json.
     */
    public String findTaskFormData(String processInstanceId) throws Exception {
        Record record = keyValueConnector.findByRef(processInstanceId);

        if (record == null) {
            return null;
        }

        Map map = new HashMap();

        for (Prop prop : record.getProps().values()) {
            map.put(prop.getCode(), prop.getValue());
        }

        String json = jsonMapper.toJson(map);

        return json;
    }

    /**
     * 根据taskId获得formDto.
     */
    public FormDTO findTaskForm(HumanTaskDTO humanTaskDto) {
        FormDTO formDto = humanTaskConnector.findTaskForm(humanTaskDto.getId());

        return formDto;
    }

    // ~ ======================================================================
    @Resource
    public void setKeyValueConnector(KeyValueConnector keyValueConnector) {
        this.keyValueConnector = keyValueConnector;
    }

    @Resource
    public void setMessageHelper(MessageHelper messageHelper) {
        this.messageHelper = messageHelper;
    }

    @Resource
    public void setCurrentUserHolder(CurrentUserHolder currentUserHolder) {
        this.currentUserHolder = currentUserHolder;
    }

    @Resource
    public void setOperationService(OperationService operationService) {
        this.operationService = operationService;
    }

    @Resource
    public void setHumanTaskConnector(HumanTaskConnector humanTaskConnector) {
        this.humanTaskConnector = humanTaskConnector;
    }

    @Resource
    public void setMultipartResolver(MultipartResolver multipartResolver) {
        this.multipartResolver = multipartResolver;
    }

    @Resource
    public void setStoreConnector(StoreConnector storeConnector) {
        this.storeConnector = storeConnector;
    }

    @Resource
    public void setTenantHolder(TenantHolder tenantHolder) {
        this.tenantHolder = tenantHolder;
    }

    @Resource
    public void setUserConnector(UserConnector userConnector) {
        this.userConnector = userConnector;
    }
    
    @Resource
    public void setCustomManager(CustomManager customManager) {
        this.customManager = customManager;
    }
    
    @Resource
    public void setFileUploadAPI(FileUploadAPI fileUploadAPI) {
        this.fileUploadAPI = fileUploadAPI;
    }
    
    @Resource
    public void setWebAPI(WebAPI webAPI) {
        this.webAPI = webAPI;
    }
    
    @Resource
    public void setApplyManager(ApplyManager applyManager) {
        this.applyManager = applyManager;
    }
    @Resource
    public void setInvoiceManager(InvoiceManager invoiceManager) {
    	this.invoiceManager = invoiceManager;
    }
    
    @Resource
    public void setBusinessDetailManager(BusinessDetailManager businessDetailManager) {
    	this.businessDetailManager = businessDetailManager;
    }
    
    @Resource
    public void setBusinessManager(BusinessManager businessManager) {
    	this.businessManager = businessManager;
    }
    @Resource
    public void setGroupBusinessManager(GroupBusinessManager groupBusinessManager) {
    	this.groupBusinessManager = groupBusinessManager;
    }
    @Resource
    public void setFreezeManager(FreezeManager freezeManager) {
    	this.freezeManager = freezeManager;
    }
    @Resource
    public void setLllegalFreezeManager(LllegalFreezeManager lllegalFreezeManager) {
    	this.lllegalFreezeManager = lllegalFreezeManager;
    }
    @Resource
    public void setTaskInfoManager(TaskInfoManager taskInfoManager) {
        this.taskInfoManager = taskInfoManager;
    }
    @Resource
    public void setDictConnectorImpl(DictConnectorImpl dictConnectorImpl){
    	this.dictConnectorImpl=dictConnectorImpl;
    }
    
    @Resource
    public void setCustomConnector(CustomConnector customConnector){
    	this.customConnector=customConnector;
    }
    
    @Resource
	public void setUpdatePersonManager(UpdatePersonManager updatePersonManager) {
		this.updatePersonManager = updatePersonManager;
	}
}
