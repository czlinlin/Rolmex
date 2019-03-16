package com.mossle.operation.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mossle.H5.user.H5PartyService;
import com.mossle.api.custom.CustomConnector;
import com.mossle.api.form.FormConnector;
import com.mossle.api.humantask.HumanTaskConnector;
import com.mossle.api.humantask.HumanTaskDTO;
import com.mossle.api.keyvalue.KeyValueConnector;
import com.mossle.api.keyvalue.Record;
import com.mossle.api.process.ProcessConnector;
import com.mossle.api.store.StoreConnector;
import com.mossle.api.tenant.TenantHolder;
import com.mossle.api.user.UserConnector;
import com.mossle.button.ButtonHelper;
import com.mossle.common.utils.Exceptions;
import com.mossle.common.utils.FileUploadAPI;
import com.mossle.common.utils.StringUtils;
import com.mossle.common.utils.WebAPI;
import com.mossle.core.annotation.Log;
import com.mossle.core.auth.CurrentUserHolder;
import com.mossle.core.auth.CustomPasswordEncoder;
import com.mossle.core.mapper.JsonMapper;
import com.mossle.core.query.PropertyFilter;
import com.mossle.core.spring.MessageHelper;
import com.mossle.humantask.persistence.domain.TaskInfo;
import com.mossle.humantask.persistence.manager.TaskInfoManager;
import com.mossle.internal.store.persistence.domain.StoreInfo;
import com.mossle.keyvalue.persistence.domain.RecordInfo;
import com.mossle.keyvalue.persistence.manager.RecordManager;
import com.mossle.msg.MsgConstants;
import com.mossle.operation.persistence.domain.ApplyDTO;
import com.mossle.operation.persistence.domain.CustomApprover;
import com.mossle.operation.persistence.domain.CustomEntity;
import com.mossle.operation.persistence.domain.CustomEntityDTO;
import com.mossle.operation.persistence.domain.CustomPre;
import com.mossle.operation.persistence.domain.CustomWorkEntityDTO;
import com.mossle.operation.persistence.manager.CustomApproverManager;
import com.mossle.operation.persistence.manager.CustomManager;
import com.mossle.operation.persistence.manager.CustomPreManager;
import com.mossle.operation.service.CustomService;
import com.mossle.operation.service.CustomWorkService;
import com.mossle.operation.service.OperationService;
import com.mossle.project.persistence.domain.WorkProjectInfo;
import com.mossle.project.persistence.domain.WorkProjectNotify;
import com.mossle.user.persistence.domain.AccountCredential;
import com.mossle.user.persistence.domain.AccountInfo;
import com.mossle.user.persistence.domain.PersonInfo;
import com.mossle.user.persistence.domain.UpdatePerson;
import com.mossle.user.persistence.manager.AccountCredentialManager;
import com.mossle.user.persistence.manager.AccountInfoManager;
import com.mossle.user.persistence.manager.UpdatePersonManager;
import com.mossle.user.service.UserService;
import com.mossle.user.support.ChangePasswordResult;
import com.mossle.util.DateUtil;
import com.mossle.util.StringUtil;

/**
 * 
 * @author ckx
 *
 */
@Component
@Controller
@RequestMapping("workOperationCustom")
@Path("WorkOperationCustom")
public class ProcessOperationWorkControllerCustom {
	
	private static Logger logger = LoggerFactory.getLogger(ProcessOperationWorkControllerCustom.class);
    
    public static final int STATUS_DRAFT_PROCESS = 0;
    public static final int STATUS_DRAFT_TASK = 1;
    public static final int STATUS_RUNNING = 2;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private OperationService operationService;
    @Autowired
    private KeyValueConnector keyValueConnector;
    @Autowired
    private MessageHelper messageHelper;
    @Autowired
    private CurrentUserHolder currentUserHolder;
    private ProcessConnector processConnector;
    @Autowired
    private HumanTaskConnector humanTaskConnector;
    private MultipartResolver multipartResolver;
    private StoreConnector storeConnector;
    private ButtonHelper buttonHelper = new ButtonHelper();
    private FormConnector formConnector;
    private TenantHolder tenantHolder;
    private AccountCredentialManager accountCredentialManager;
    @Autowired
    private CustomManager customManager;
    @Autowired
    private TaskInfoManager taskInfoManager;
    private CustomPreManager customPreManager;
    @Autowired
    private FileUploadAPI fileUploadAPI;
    @Autowired
    private WebAPI webAPI;
    private ProcessOperationController processOperationController;
    private AccountInfoManager accountInfoManager;
    private CustomPasswordEncoder customPasswordEncoder;
    @Autowired
    private CustomService customService;
    @Autowired
    private CustomWorkService customWorkService;
    @Autowired
    private CustomConnector customConnector;
    @Autowired
    private UpdatePersonManager updatePersonManager ;
    private JsonMapper jsonMapper = new JsonMapper();
    private UserService userService;
    @Autowired
    private UserConnector userConnector;
    @Autowired
    private CustomApproverManager customApproverManager;
    @Autowired
    private RecordManager recordManager;
    
    /**
     * 查询用户是否绑定了考勤组
     * @return
     */
    @RequestMapping("checkedAttendanceRecords")
    @ResponseBody
    public String checkedAttendanceRecords(){
    	String userId = currentUserHolder.getUserId();
    	boolean checkedAttendanceRecords = customWorkService.checkedAttendanceRecords(userId);
    	return JSONObject.toJSONString(checkedAttendanceRecords);
    	
    }
    
    
    
    /**
     * 跳转到自定义请假申请的发起表单.
     * @throws Exception 
     * ckx
     */
    @RequestMapping("custom-work-apply-list")
    public String customApplyForm(@RequestParam("userName") String userName,@RequestParam("formType") String formType,Model model) throws Exception {

    	String userId = currentUserHolder.getUserId();
    	Map<String, Object> queryForMap = jdbcTemplate.queryForMap("select department_name as departmentName from person_info where id = "+userId);
    	Object departmentName = queryForMap.get("departmentName");
    	System.out.println(departmentName);
    	//生成受理单编号
    	String code =  operationService.CreateApplyCode(userId);
       
    	
    	String date = DateUtil.formatDate(new Date(), " yyyy 年 MM 月 dd 日");
    	model.addAttribute("nowDate", date);
    	model.addAttribute("formType", formType);
    	model.addAttribute("departmentName", departmentName);
    	model.addAttribute("userName",userName);
    	model.addAttribute("code",code);
    	model.addAttribute("presetApproverList", userService.getCustomPresetApprovers());
        return "operation/custom/custom-work-apply-list";
    }
    
    
    /**
     * 发起流程.
     * ckx
     */
    @RequestMapping("custom-work-startProcessInstance")  
    //@ResponseBody
    @Log(desc = "发起流程", action = "startProcess", operationDesc = "流程中心-我的流程-发起流程-请假")
    public String startProcessInstance(HttpServletRequest request, @ModelAttribute CustomWorkEntityDTO customWorkEntityDTO,String area,
            @RequestParam("bpmProcessId") String bpmProcessId, 
            @RequestParam("businessKey") String businessKey, 
            @RequestParam(value = "files", required = false) MultipartFile[] files,
            Model model) throws Exception {
    	//MultipartFile[] files = null;
    	
    	CustomEntity customEntity = new CustomEntity();
       // copyUser(customWorkEntityDTO, customEntity);
    	customWorkService.StartProcessCustom(request, customWorkEntityDTO,area, files,customEntity);
    	
    	return "operation/process-operation-startProcessInstance";
    }

    /**
     * 处理抄送人  暂时不用
     * @param customWorkEntityDTO
     * @param customEntity
     */
	private void copyUser(CustomWorkEntityDTO customWorkEntityDTO,
			CustomEntity customEntity) {
		String copyIds = "";
        String copyNames = "";
        String ccnos = customWorkEntityDTO.getCcnos();
        String ccName = customWorkEntityDTO.getCcName();
        //ckx  add 2018/8/30  抄送包含岗位
        if(null != ccnos && !"".equals(ccnos) && !"null".equals(ccnos)){
        	String[] splitCopyId = ccnos.split(",");
            String[] splitCopyName = ccName.split(",");
            for (int i = 0; i < splitCopyId.length; i++) {
            	String strCopyId = splitCopyId[i];
            	String strCopyName = splitCopyName[i];
            	if(strCopyId.contains("岗位:")){
    				//岗位查询人员
            		strCopyId = strCopyId.replaceAll("岗位:", "");
    	    		
            		//查询岗位上一级
            		/*Map<String, Object> queryCopyMap = jdbcTemplate.queryForMap("select * from party_entity e LEFT JOIN party_struct s ON e.ID = s.PARENT_ENTITY_ID where  s.STRUCT_TYPE_ID = 1 and e.DEL_FLAG = 0 and e.IS_DISPLAY = 1 and s.CHILD_ENTITY_ID = '"+strCopyId+"';");
    	    		String strCopyNameS = StringUtil.toString(queryCopyMap.get("NAME"));
    	    		if(null != strCopyNameS && !"".equals(strCopyNameS) && !"null".equals(strCopyNameS)){
    	    			strCopyNameS = strCopyNameS+"-";
    	    		}*/
            		String lastPost = customWorkService.getLastPost(strCopyId);
    	    		copyIds += strCopyId+",";
    				copyNames += lastPost+strCopyName+",";
            		
    			}else{
    				copyIds += strCopyId+",";
    				copyNames += strCopyName+",";
    			}
    		}
        }
        if(StringUtils.isNotBlank(copyIds)){
    		String str = copyIds.substring(copyIds.length()-1,copyIds.length() );
    		if(",".equals(str)){
    			copyIds = copyIds.substring(0, copyIds.length()-1);
    		}
    		String strName = copyNames.substring(copyNames.length()-1,copyNames.length() );
    		if(",".equals(strName)){
    			copyNames = copyNames.substring(0, copyNames.length()-1);
    		}
    		customEntity.setCcnos(copyIds);
            customEntity.setCcName(copyNames);
    	}
        
	}
    
    // ~ ======================================================================
    /**
     * 根据表单中选择的下一步审批人，查下该人是否已经审批过该条申请，避免重复审批
     */
    @GET
    @Path("isConfirm")
    public int isConfirm(@QueryParam("leaderId")  String leaderId,@QueryParam("formID") String formID){
	   
    	String sql = "from CustomPre where previous=? and formID =? ";
        
        List<CustomPre> c = customPreManager.find(sql,leaderId,formID);
        
        if(c.size()>0){
			return 1;
		}else {
			return 0;
		}
    }
    
    // ~ ============================================================================================================================================    
    /**
     * 完成任务.
     */
    @GET
    @RequestMapping("custom-work-completeTask")
    @Log(desc = "审批流程", action = "confirmProcess", operationDesc = "流程中心-我的审批-待办审批-自定义")
    public String completeTask(HttpServletRequest request
    		,@ModelAttribute CustomWorkEntityDTO customWorkEntityDTO,
            RedirectAttributes redirectAttributes,
            @RequestParam("processInstanceId") String processInstanceId, 
            @RequestParam(value = "files", required = false) MultipartFile[] files,
            @RequestParam(value = "iptdels", required = false) String iptdels,
            @RequestParam(value = "iptresart", required = false) String iptresart,
            @RequestParam(value = "iptoldid", required = false) String iptoldid,
            String flag
           ) throws Exception {
    	//isConfirm
    	//messageHelper.addFlashMessage(redirectAttributes, "core.success.publish", strTempName);
    	String hsql="from TaskInfo where processInstanceId=? and status='active' and catalog='normal'";
    	List<TaskInfo> taskInfoList = taskInfoManager.find(hsql,processInstanceId);
    	if(taskInfoList==null||taskInfoList.size()<1){
    		logger.info("查询不到审批步骤");
            messageHelper.addFlashMessage(redirectAttributes, "core.success.save", "找不到该流程或者流程已结束");
            return "redirect:/humantask/workspace-personalTasks.do";
    		//return messageHelper.addFlashMessage(redirectAttributes, "core.success.publish", "");
    	}
    	else{
    		if(!currentUserHolder.getUserId().equals(taskInfoList.get(0).getAssignee())){
    			logger.info("processInstanceId:"+processInstanceId+"，重复操作");
                messageHelper.addFlashMessage(redirectAttributes, "core.success.save", "你无权限审核或则已审核过该流程");
                return "redirect:/humantask/workspace-personalTasks.do";
    		}
    	}
    	
    	try {
    		customWorkService.CompleteTaskCustomPC(request, customWorkEntityDTO,
    				processInstanceId, files, iptdels, flag);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            String errStr = Exceptions.getExceptionAllinformation(ex);
            
            if (StringUtils.inString(errStr, "org.hibernate.exception.LockAcquisitionException") || 
    				StringUtils.inString(errStr, "com.mysql.jdbc.exceptions.jdbc4.MySQLTransactionRollbackException")) {
    			messageHelper.addFlashMessage(redirectAttributes, "系统繁忙或网络异常，请稍后重试！");
    		} else if (StringUtils.inString(errStr, "任务不存在")){
    			messageHelper.addFlashMessage(redirectAttributes, "任务不存在");
    		} else {
    			messageHelper.addFlashMessage(redirectAttributes, "core.success.save", "系统内部发生错误，请联系管理员！");
    		}
    		
            return "redirect:/humantask/workspace-personalTasks.do";
        }
    	
    	return "operation/task-operation-completeTask";
    }
	
	/**
     * 申请单详情页
	 * @throws IOException 
	 * ckx
     * */
    @RequestMapping("custom-work-detail")
    @Log(desc = "查看详情页", action = "processDetail", operationDesc = "流程中心-自定义-详情")
    public String formDetail( @RequestParam("processInstanceId") String processInstanceId,
    		@RequestParam(value = "isPrint", required = false) boolean isPrint,
    		@RequestParam(value = "viewBack", required = false) boolean viewBack,
    		 Model model) throws IOException{
    	CustomEntity customEntity=customManager.get(Long.parseLong(processInstanceId));
    	model.addAttribute("customEntity", customEntity);
    	
    	//取附件
   	 	model.addAttribute("picUrl", webAPI.getViewUrl());
        List<StoreInfo> list;
		try {
			list = fileUploadAPI.getStore("operation/CustomApply", processInstanceId);
			model.addAttribute("StoreInfos", list);
		} catch (Exception e) {
			e.printStackTrace();
		}
       
    	// 审批记录
        List<HumanTaskDTO> logHumanTaskDtos = humanTaskConnector
                .findHumanTasksForPositionByProcessInstanceId(processInstanceId);
        //获得审核时长
        logHumanTaskDtos = operationService.settingAuditDuration(logHumanTaskDtos);
        model.addAttribute("logHumanTaskDtos", logHumanTaskDtos);
        model.addAttribute("isPrint", isPrint);
        model.addAttribute("viewBack", viewBack);
        
        HumanTaskDTO humanTaskDto=logHumanTaskDtos.get(0);
        Map<String,Object> approverMap=customConnector.findAuditorHtml(humanTaskDto.getBusinessKey(), currentUserHolder.getUserId());
        
        model.addAttribute("approver",approverMap.get("approver"));
    	
      //判断若是花名册的流程，将添加人员或者修改花名册的标识取出来带回页面
    	String personTypeID = "";
    	UpdatePerson updatePerson=updatePersonManager.findUniqueBy("applyCode", customEntity.getApplyCode());
  		String id = "";
  		String partyEntityId = "";
  		
  		if(updatePerson!=null){
  			//model.addAttribute("applyCode", updatePerson.getApplyCode());
  			personTypeID = updatePerson.getTypeID();
  			if((updatePerson.getTypeID().equals("personadd")||updatePerson.getTypeID().equals("personUpdate"))){
  	  			Object succesResponse = JSON.parse(updatePerson.getJsonContent());    //先转换成Object
  				Map map = (Map)succesResponse;
  				PersonInfo personInfo=jsonMapper.fromJson(map.get("personInfo").toString(),PersonInfo.class);
  				id = personInfo.getId()==null?"": personInfo.getId().toString();
  				partyEntityId = map.get("parentPartyEntityId")==null?"":map.get("parentPartyEntityId").toString();
  				
  	    	}
  	  		else if((updatePerson.getTypeID().equals("changePost"))){
  	  			id = updatePerson.getEmployeeNo();
  	  			//model.addAttribute("personInfoId", id);
  	  		}
  		}
  	
  		model.addAttribute("personInfoId",id);
  		model.addAttribute("partyEntityId",partyEntityId);
  		model.addAttribute("personTypeID", personTypeID);
  		operationService.copyMsgUpdate(processInstanceId);
   
    	return "operation/custom/custom-work-apply-showForm";
    }
    
    /**
     * 跳转到抄送的发起表单.
     * @throws Exception 
     * ckx
     */
    @RequestMapping("custom-copy-edit")
    public String copyEdit(@RequestParam("processInstanceId") String processInstanceId,@RequestParam(value = "title", required = false) String title, RedirectAttributes redirectAttributes, Model model,HttpServletRequest request) throws Exception {
    	String retUrl = request.getHeader("Referer");  
    	
    	 String redirectUrl = "redirect:/workOperationCustom/custom-copy-edit.do";
         try {
             if (processInstanceId == null || "".equals(processInstanceId)) {
                 
                 messageHelper.addFlashMessage(redirectAttributes, "core.success.save", "获取参数错误");
                 return redirectUrl;
             }

             //抄送人
			List<Map<String,Object>> queryForList = null;
			Map<String, Object> queryForMapKv = null;
			List<Map<String, Object>> titleList = null;
			try {
				queryForMapKv = jdbcTemplate.queryForMap("select id,theme,url,user_id,name from kv_record where ref = '"+processInstanceId+"'");

				 queryForList = jdbcTemplate.queryForList("select id, assignee from task_info where process_instance_id='"+processInstanceId+"' and catalog ='copy' order by create_time asc");
				 titleList = jdbcTemplate.queryForList("select presentation_subject from task_info where process_instance_id='"+processInstanceId+"' and CATALOG='start'");
			} catch (Exception e1) {
			}
             if(null != titleList && titleList.size() > 0){
            	 Map<String, Object> map = titleList.get(0);
            	 
            	 String strTitle = StringUtil.toString(map.get("presentation_subject"));
            	 if(null != strTitle && !"".equals(strTitle) && !"null".equals(strTitle)){
            		 title = strTitle;
            	 }
             }
             if(null != queryForMapKv){
            	 String theme = StringUtil.toString(queryForMapKv.get("theme"));
            	 if(StringUtils.isNotBlank(theme)){
            		 title = theme;
            	 }
             }
             String notifynames = "";
             if (null != queryForList && !CollectionUtils.isEmpty(queryForList)) {
                 for (Map<String,Object> map : queryForList) {
                	 String taskId = StringUtil.toString(map.get("id"));
                	 String str = StringUtil.toString(map.get("assignee"));
                	 if(null != str && !"".equals(str) && null != taskId && !"".equals(taskId) ){
                		 Map<String, Object> queryForMap = null;
						try {
							queryForMap = jdbcTemplate.queryForMap("select position_type from task_info_approve_position where task_id = '"+taskId+"'");
						} catch (Exception e) {
						}
						if(null != queryForMap){
							 String positionId = StringUtil.toString(queryForMap.get("position_type"));
	                    	 if("1".equals(positionId)){
	                    		 //岗位
	                    		 //根据岗位查询人员id
	                    		/*List<Map<String, Object>> queryForUserList = null;
								try {
									queryForUserList = jdbcTemplate.queryForList("select e.id,name from party_entity e join party_struct s on e.id=s.CHILD_ENTITY_ID where e.DEL_FLAG = '0' and  s.PARENT_ENTITY_ID='"+str+"';");
								} catch (Exception e) {
								}
								if(null != queryForUserList && queryForUserList.size() > 0){
									for (Map<String, Object> map2 : queryForUserList) {
										notifynames += StringUtil.toString(map2.get("name")+",");
									}
								}*/
	                    		
	             	    		Map<String, Object> queryCopyMap = null;
	             	    		Map<String, Object> queryCopyPostMap = null;
								try {
									//通过岗位查询上一级
									queryCopyMap = jdbcTemplate.queryForMap("select * from party_entity e LEFT JOIN party_struct s ON e.ID = s.PARENT_ENTITY_ID where s.STRUCT_TYPE_ID = 1 and e.DEL_FLAG = 0 and e.IS_DISPLAY = 1 and s.CHILD_ENTITY_ID = '"+str+"';");
		            	    		queryCopyPostMap = jdbcTemplate.queryForMap("select * from party_entity e where e.ID = '"+str+"'");
								} catch (Exception e) {
								}
								String strCopyName = "";
								if(StringUtils.isNotBlank(StringUtil.toString(queryCopyMap.get("NAME"))) && !"null".equals(StringUtil.toString(queryCopyMap.get("NAME")))){
									strCopyName = StringUtil.toString(queryCopyMap.get("NAME"))+"-";
								}
	             	    		
	             	    		String strCopyPostName = StringUtil.toString(queryCopyPostMap.get("NAME") == null ? "" : queryCopyPostMap.get("NAME"));
	            	    		//formCopyNames += strCopyName+"-"+ccUserNames+",";
	            	    		notifynames += strCopyName+strCopyPostName+",";
	                    		 
	                    		 
	                    	 }else if("2".equals(positionId)){
	                    		 notifynames += userConnector.findById(str).getDisplayName()
	                                     + ",";
	                    	 }
						}else{
							notifynames += userConnector.findById(str).getDisplayName()
                                    + ",";
						}
                    	
                	 }else{
                		 continue;
                	 }
                     
                 }
             }

             if (!"".equals(notifynames))
                 notifynames = notifynames.substring(0, notifynames.length() - 1);
             
             model.addAttribute("url", retUrl);
             model.addAttribute("notifynames", notifynames);
             model.addAttribute("title", title);
             model.addAttribute("id", processInstanceId);
         } catch (ArithmeticException e) {
             logger.error("添加抄送人人初始化页面-：" + e.getMessage() + "\r\n" + e.fillInStackTrace());
             messageHelper.addFlashMessage(redirectAttributes, "core.success.save", "查询异常");
             return redirectUrl;
         }
         return "operation/custom/custom-copy-edit";
    }
    
    
    /**
     * 保存抄送的发起表单.
     * @throws Exception 
     * ckx
     */
    @RequestMapping("custom-copy-save")
    public String copySave(@RequestParam("processInstanceId") String processInstanceId,@RequestParam(value = "copyIds") String copyIds,@RequestParam(value = "copyNames") String copyNames,@RequestParam(value = "url") String url,@RequestParam(value = "title",required=false) String title, RedirectAttributes redirectAttributes, Model model){

    	String redirectUrl = "redirect:"+url;
    	
    	try {
			if (null == processInstanceId || "".equals(processInstanceId)) {
			    logger.error("查询项目明细-获取参数id错误");
			    messageHelper.addFlashMessage(redirectAttributes, "core.success.save", "获取参数错误");
			    return redirectUrl;
			}
			if (null == currentUserHolder ) {
			    logger.error("请重新登录");
			    messageHelper.addFlashMessage(redirectAttributes, "core.success.save", "请重新登录");
			    return redirectUrl;
			}

			Record record = keyValueConnector.findByRef(processInstanceId);
			if (record == null) {
			    logger.debug("查询项目明细-没有查询到项目信息");
			    messageHelper.addFlashMessage(redirectAttributes, "core.success.save", "没有查询到项目信息");
			    return redirectUrl;
			}
			if(!copyIds.isEmpty()){
				String userId = currentUserHolder.getUserId();
				customWorkService.copySave(processInstanceId,copyIds,copyNames,title,userId);
			}
		} catch (Exception e) {
			logger.error("添加知会人初始化页面-：" + e.getMessage() + "\r\n" + e.fillInStackTrace());
            messageHelper.addFlashMessage(redirectAttributes, "core.success.save", "查询异常");
            return redirectUrl;
		}
        messageHelper.addFlashMessage(redirectAttributes, "core.success.save", "添加抄送人成功");
        return redirectUrl;
        
    	
    }
    /**
     * 根据实例id获取表单的抄送人
     * @param id
     * @return
     */
    @RequestMapping("getFormCopyName")
    @ResponseBody
    public String getFormCopyName(@RequestParam("id") String id) {
    	String taskCopyNames = customWorkService.getTaskCopyNames(id);
    	return JSONObject.toJSONString(taskCopyNames);
    }
    /**
     * 获取下下步审核人
     * @param processInstanceId
     * @param userId
     * @return
     */
    @RequestMapping("getNextApprover")
    @ResponseBody
    public String getNextApprover(@RequestParam("processInstanceId") String processInstanceId , @RequestParam("userId") String userId){
    	/*List<Map<String,Object>> queryForList = null;
    	String userName = "";
    	String id = "";
    	try {
			queryForList = jdbcTemplate.queryForList("select * from task_info where PROCESS_INSTANCE_ID = '"+processInstanceId+"' ");
			if(null != queryForList && queryForList.size() > 0){
				Object businessKey = queryForList.get(0).get("BUSINESS_KEY");
				Map<String, Object> queryForMap = jdbcTemplate.queryForMap("select * from custom_approver c where c.opterType != '2' and c.opterType != '3' and c.business_Key = '"+businessKey+"' and c.approveStep = ("
						+"select a.approveStep + 1 from custom_approver a where a.approverId = '"+userId+"' and a.business_Key = '"+businessKey+"' and opterType !='2' and opterType !='3') ");
				 
				Map<String, Object> userMap = jdbcTemplate.queryForMap("select * from party_entity where id = '"+queryForMap.get("approverId").toString()+"';");
				userName = StringUtil.toString(userMap.get("NAME"));
				id = StringUtil.toString(userMap.get("id"));
			}
			
		} catch (DataAccessException e) {
		}*/
    	
    	//ckx 查询当前用户
    	String userName = "";
    	String id = "";
    	List<Map<String,Object>> queryForList = null;
    	queryForList = jdbcTemplate.queryForList("select * from task_info where PROCESS_INSTANCE_ID = '"+processInstanceId+"' ");
		if(null != queryForList && queryForList.size() > 0){
			Object businessKey = queryForList.get(0).get("BUSINESS_KEY");
			
			List<CustomApprover> approverListNow=customApproverManager.find(
					"from CustomApprover where businessKey=? and approverId=? and opterType !='2' and opterType !='3'", 
					businessKey,Long.parseLong(userId));
	        if(null != approverListNow && approverListNow.size() > 0){
	        	CustomApprover customApproverNow = approverListNow.get(0);
	        	//获取所有下一步审批步骤
				List<CustomApprover> nextApproverList=customApproverManager.find(
						"from CustomApprover where businessKey=? and approveStep>? and opterType !='2' and opterType !='3' order by approveStep", 
						businessKey,
						customApproverNow.getApproveStep());
				if(null != nextApproverList && nextApproverList.size() > 0){
					CustomApprover customApprover2 = nextApproverList.get(0);
					id = String.valueOf(customApprover2.getApproverId());
					Map<String, Object> userMap = jdbcTemplate.queryForMap("select * from party_entity where id = '"+id+"';");
					userName = StringUtil.toString(userMap.get("NAME"));
				}
	        }
		}
        
    	HashMap<String, Object> map = new HashMap<String, Object>();
    	map.put("id", id);
    	map.put("userName", userName);
    	
    	return JSONObject.toJSONString(map);
    }
    
    /**
     * 获取启明公益的特殊细分
     * @return
     */
    @RequestMapping("getBusinessDetail")
    @ResponseBody
    public String getBusinessDetail(@RequestParam(value="businessDetailId",required = false) String businessDetailId,
    		@RequestParam(value="applyCode" ,required = false) String applyCode){
    	if(StringUtils.isNotBlank(applyCode)){
    		RecordInfo recode = recordManager.findUniqueBy("applyCode", applyCode);
        	businessDetailId = recode.getBusinessDetailId();
    	}
    	boolean boo = customWorkService.getBusinessDetail(businessDetailId);
    	
    	return JSONObject.toJSONString(boo);
    }
    /**
     * 获取数据字典
     * @param dictName
     * @return
     */
    @RequestMapping("getDict")
    @ResponseBody
    public String getDict(@RequestParam(value="dictName") String dictName){
    	String dict = customWorkService.getDict(dictName);
    	return dict;
    }
    
    @Resource
	public void setUserService(UserService userService) {
		this.userService = userService;
	}
}
