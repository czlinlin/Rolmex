package com.mossle.operation.web;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mossle.api.humantask.HumanTaskConnector;
import com.mossle.api.humantask.HumanTaskDTO;
import com.mossle.common.utils.Exceptions;
import com.mossle.common.utils.FileUploadAPI;
import com.mossle.common.utils.StringUtils;
import com.mossle.common.utils.WebAPI;
import com.mossle.core.annotation.Log;
import com.mossle.core.auth.CurrentUserHolder;
import com.mossle.core.auth.CustomPasswordEncoder;
import com.mossle.core.spring.MessageHelper;
import com.mossle.internal.store.persistence.domain.StoreInfo;
import com.mossle.operation.persistence.domain.Apply;
import com.mossle.operation.persistence.domain.ApplyDTO;
import com.mossle.operation.persistence.manager.ApplyManager;
import com.mossle.operation.service.ApplyService;
import com.mossle.operation.service.CustomWorkService;
import com.mossle.operation.service.OperationService;
import com.mossle.user.persistence.domain.AccountCredential;
import com.mossle.user.persistence.domain.AccountInfo;
import com.mossle.user.persistence.manager.AccountCredentialManager;
import com.mossle.user.persistence.manager.AccountInfoManager;
import com.mossle.user.support.ChangePasswordResult;

/** 
 * @author  cz 
 * @version 2017年7月26日
 * 业务受理申请单流程 
 */

@Component
@Controller
@RequestMapping("operationApply")
@Path("operationApply")
public class ProcessOperationControllerApply {

    private static Logger logger = LoggerFactory.getLogger(ProcessOperationController.class);
    
    public static final int STATUS_DRAFT_PROCESS = 0;
    public static final int STATUS_DRAFT_TASK = 1;
    public static final int STATUS_RUNNING = 2;

    private MessageHelper messageHelper;
    private CurrentUserHolder currentUserHolder;

    private HumanTaskConnector humanTaskConnector;

    private ApplyManager applyManager;
    private AccountInfoManager accountInfoManager;
    private AccountCredentialManager accountCredentialManager;
    private CustomPasswordEncoder customPasswordEncoder;
    private FileUploadAPI fileUploadAPI;
    private WebAPI webAPI;
    private ApplyService applyService;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private CustomWorkService customWorkService;
    @Autowired
    private OperationService operationService;
    
    /**
     * 发起流程.
     */
    @RequestMapping("process-operationApply-startProcessInstance")
    @Log(desc = "发起流程", action = "startProcess", operationDesc = "流程中心-我的流程-发起流程-常规非常规")
    public String startProcessInstance(HttpServletRequest request, @ModelAttribute ApplyDTO applyDTO,
    		String areaId,String areaName,String companyId,String companyName,
            @RequestParam("bpmProcessId") String bpmProcessId, 
            @RequestParam("businessKey") String businessKey, 
            @RequestParam(value = "files", required = false) MultipartFile[] files,
            Model model) throws Exception {
        
    	applyService.saveApplty(request, applyDTO,areaId,areaName,companyId,companyName, bpmProcessId, files);
        
        return "operation/process-operation-startProcessInstance";
    }

    //审批环节：先将表单内容取出
    @GET
    @Path("getApplyInfo")
    @RequestMapping("getApplyInfo")
    public List<ApplyDTO> getLevel(@QueryParam("id") String id) {
    	 List<Apply> apply = applyManager.findBy("processInstanceId",id);
         List<ApplyDTO> applyDtos = new ArrayList<ApplyDTO>();
         for (Apply getInfo : apply) {
        	ApplyDTO applyDto = new ApplyDTO();
        	applyDto.setApplyCode(getInfo.getApplyCode());
        	applyDto.setUcode(getInfo.getUcode());
        	applyDto.setApplyContent(getInfo.getContent());
        	applyDto.setUserName(getInfo.getUserName());
        	applyDto.setWelfare(getInfo.getWelfare());
        	applyDto.setLevel(getInfo.getLevel());
        	applyDto.setSystem(getInfo.getSystem());
        	applyDto.setVarFather(getInfo.getVarFather());
        	applyDto.setVarRe(getInfo.getVarRe());
        	applyDto.setAddTime(getInfo.getAddTime());
        	applyDto.setBusinessType(getInfo.getBusinessType());
        	applyDto.setBusinessDetail(getInfo.getBusinessDetail());
        	applyDto.setMobile(getInfo.getMobile());
        	applyDto.setAddress(getInfo.getAddress());
        	applyDto.setBusinessLevel(getInfo.getBusinessLevel());
        	applyDto.setArea(getInfo.getArea());
        	applyDto.setBusinessStand1(getInfo.getBusinessStand1());
        	applyDto.setBusinessStand2(getInfo.getBusinessStand2());
        	applyDto.setTreeInfo(getInfo.getTreeInfo());
        	applyDto.setCreateTime(getInfo.getCreateTime());
        	applyDto.setId(getInfo.getId());
        	applyDto.setFileName(getInfo.getFileName());
        	applyDto.setFilePath(getInfo.getFilePath());
        	applyDto.setSubmitTimes(getInfo.getSubmitTimes());
        	
        	//获取修改电话的html by lilei at 2019.01.31
    		String detailHtml="";
    		String detailId="";
    		String strSql=String.format("select business_key,businessDetailId from kv_record where applycode='%s'", applyDto.getApplyCode());
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
					            	 for (Map<String, Object> mapSystem: mapSystemList) {
					            		 detailHtml+="&emsp;<input type=\"checkbox\" name=\"fileName\" disabled";
					            		 if(applyDto.getFileName()!=null&&applyDto.getFileName().contains(mapSystem.get("value").toString())){
					            			 detailHtml+=" checked ";
					            			 detailHtml+=" value=\""+mapSystem.get("value")+"\"/>&nbsp;<span style=\"color:red\">"+mapSystem.get("title")+"</span>";
					            		 }
					            		 else
					            			 detailHtml+=" value=\""+mapSystem.get("value")+"\"/>&nbsp;<span>"+mapSystem.get("title")+"</span>";
									 }
					             }
					             detailHtml+="</td>"
			    				            +"</tr>";
			    			 }
			    		 }
			    	 }
			     }
    		}
    		applyDto.setFileName(detailHtml);
        	
        	applyDtos.add(applyDto);
        }
        return applyDtos;
    }

    /**
     * 完成任务.
     */
    @GET
    @RequestMapping("apply-completeTask")
    @Log(desc = "审批流程", action = "confirmProcess", operationDesc = "流程中心-我的审批-待办审批-常规非常规")
    public String completeTask(HttpServletRequest request,
            RedirectAttributes redirectAttributes,
            @RequestParam("processInstanceId") String processInstanceId, 
            @RequestParam("humanTaskId") String humanTaskId,
            @RequestParam(value = "files", required = false) MultipartFile[] files,
            @RequestParam(value = "iptdels", required = false) String iptdels,
            @RequestParam(value = "iptresart", required = false) String iptresart,
            @RequestParam(value = "iptoldid", required = false) String iptoldid,
            String flag
           ) throws Exception {

    	try {
    		applyService.saveEndApply(request, redirectAttributes, processInstanceId, humanTaskId, files, iptdels);
        } catch (IllegalStateException ex) {
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

	
// ~ ======================================================================
	    
    //验证密码是否正确
    @GET
    @Path("commonApply-verifyPassword")
    public int VerifyPassword(@QueryParam("pwd")  String pwd){
	   Long accountId = Long.parseLong(currentUserHolder.getUserId());
	   AccountInfo accountInfo = accountInfoManager.get(accountId);
	   String hql = "from AccountCredential where accountInfo=? and catalog='default'";
	   AccountCredential accountCredential = accountCredentialManager.findUnique(hql, accountInfo);
	   ChangePasswordResult changePasswordResult = new ChangePasswordResult();
	   if (!isPasswordValid(pwd, accountCredential.getOperationPassword())) {
            changePasswordResult.setCode("user.user.input.passwordnotcorrect");
            changePasswordResult.setMessage("密码错误");

            return 0;
        }else {
        	return 1;
        }
    }
    
    public boolean isPasswordValid(String rawPassword, String encodedPassword) {
        if (customPasswordEncoder != null) {
            return customPasswordEncoder.matches(rawPassword, encodedPassword);
        } else {
            return rawPassword.equals(encodedPassword);
        }
    }
    
    /**
     * 申请单详情页
     * */
    @RequestMapping("commApplyFrom-detail")
    @Log(desc = "查看详情页", action = "processDetail", operationDesc = "流程中心-常规非常规-详情")
    public String formDetail(Model model, @RequestParam("processInstanceId") String processInstanceId,
    		@RequestParam(value = "isPrint", required = false) boolean isPrint,
    		@RequestParam(value = "viewBack", required = false) boolean viewBack){
    	//取表单内容
	    	this.getLevel(processInstanceId);
	    	Long id = this.getLevel(processInstanceId).get(0).getId();
	    	
	    //取附件
	    	 model.addAttribute("picUrl", webAPI.getViewUrl());
	         List<StoreInfo> list;
			try {
				list = fileUploadAPI.getStore("operation/commApply", Long.toString(id));
				model.addAttribute("StoreInfos", list);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			 //审批记录
            List<HumanTaskDTO> logHumanTaskDtos = humanTaskConnector
                    .findHumanTasksForPositionByProcessInstanceId(processInstanceId);
			//获得审核时长
            logHumanTaskDtos = operationService.settingAuditDuration(logHumanTaskDtos);            model.addAttribute("logHumanTaskDtos", logHumanTaskDtos);
            model.addAttribute("isPrint", isPrint);
            model.addAttribute("viewBack", viewBack);
            operationService.copyMsgUpdate(processInstanceId);
	        return "operation/common-operation-showForm";
    }

    // ~ ======================================================================
    @Resource
    public void setMessageHelper(MessageHelper messageHelper) {
        this.messageHelper = messageHelper;
    }

    @Resource
    public void setCurrentUserHolder(CurrentUserHolder currentUserHolder) {
        this.currentUserHolder = currentUserHolder;
    }

    @Resource
    public void setHumanTaskConnector(HumanTaskConnector humanTaskConnector) {
        this.humanTaskConnector = humanTaskConnector;
    }

 // ~ ======================================================================
    @Resource
    public void setApplyManager(ApplyManager applyManager) {
        this.applyManager = applyManager;
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
    public void setAccountInfoManager(AccountInfoManager accountInfoManager) {
        this.accountInfoManager = accountInfoManager;
    }
    
    @Resource
    public void setAccountCredentialManager(AccountCredentialManager accountCredentialManager) {
        this.accountCredentialManager = accountCredentialManager;
    }
    @Resource
	public void setCustomPasswordEncoder(CustomPasswordEncoder customPasswordEncoder) {
		this.customPasswordEncoder = customPasswordEncoder;
	}

	@Resource
	public void setApplyService(ApplyService applyService) {
		this.applyService = applyService;
	}
    
    
}
