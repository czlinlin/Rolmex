package com.mossle.operation.web;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mossle.api.humantask.HumanTaskConnector;
import com.mossle.api.humantask.HumanTaskDTO;
import com.mossle.common.utils.Exceptions;
import com.mossle.common.utils.StringUtils;
import com.mossle.core.annotation.Log;
import com.mossle.core.auth.CurrentUserHolder;
import com.mossle.core.auth.CustomPasswordEncoder;
import com.mossle.core.spring.MessageHelper;
import com.mossle.operation.persistence.domain.CancelOrder;
import com.mossle.operation.persistence.domain.CancelOrderDTO;
import com.mossle.operation.persistence.domain.CancelOrderSub;
import com.mossle.operation.persistence.domain.CancelOrderSubDTO;
import com.mossle.operation.persistence.manager.CancelOrderManager;
import com.mossle.operation.persistence.manager.CancelOrderSubManager;
import com.mossle.operation.service.CancelOrderService;
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
@RequestMapping("operationCancelOrder")
@Path("operationCancelOrder")
public class ProcessOperationControllerCancelOrder {

private static Logger logger = LoggerFactory.getLogger(ProcessOperationController.class);
    
    public static final int STATUS_DRAFT_PROCESS = 0;
    public static final int STATUS_DRAFT_TASK = 1;
    public static final int STATUS_RUNNING = 2;
    private OperationService operationService;

    private MessageHelper messageHelper;
    private CurrentUserHolder currentUserHolder;

    private HumanTaskConnector humanTaskConnector;

    private CancelOrderManager cancelOrderManager;
    private CancelOrderSubManager cancelOrderSubManager;
    private AccountCredentialManager accountCredentialManager;
    private AccountInfoManager accountInfoManager;
    private CustomPasswordEncoder customPasswordEncoder;

    private CancelOrderService cancelOrderService;
    
    /**
     * 发起流程.
     */
    @RequestMapping("process-operationCancelOrder-startProcessInstance")
    @Log(desc = "发起流程", action = "startProcess", operationDesc = "流程中心-我的流程-发起流程-撤单")
    public String startProcessInstance(HttpServletRequest request, @ModelAttribute CancelOrderDTO cancelOrderDTO,
    		String areaId,String areaName,String companyId,String companyName,
            @RequestParam("bpmProcessId") String bpmProcessId, 
            @RequestParam("businessKey") String businessKey,
            Model model) throws Exception {
        
    	cancelOrderService.StartProcessCancelOrder(request, cancelOrderDTO,areaId,areaName,companyId,companyName, bpmProcessId);
        
        return "operation/process-operation-startProcessInstance";
    }
    // ~ ======================================================================
    //审批环节：先将表单内容取出
    @GET
    @Path("getCancelOrderInfo")
    public List<CancelOrderDTO> getCancelOrderInfo(@QueryParam("id") String id) {
    	//取撤单的主表的数据
    	 List<CancelOrder> cancelOrder = cancelOrderManager.findBy("processInstanceId",id);
         List<CancelOrderDTO> cancelOrderDtos = new ArrayList<CancelOrderDTO>();
         for (CancelOrder getInfo : cancelOrder) {
        	CancelOrderDTO cancelOrderDto = new CancelOrderDTO();
        	cancelOrderDto.setApplyCode(getInfo.getApplyCode());
        	cancelOrderDto.setUcode(getInfo.getUcode());
        	cancelOrderDto.setShopName(getInfo.getShopName());
            cancelOrderDto.setShopMobile(getInfo.getShopMobile());
            cancelOrderDto.setMobile(getInfo.getMobile());
            cancelOrderDto.setRegisterName(getInfo.getRegisterName());
            cancelOrderDto.setRegisterTime(getInfo.getRegisterTime());
            cancelOrderDto.setIsChecked(getInfo.getIsChecked());
            cancelOrderDto.setCancelRemark(getInfo.getCancelRemark());
            cancelOrderDto.setId(getInfo.getId());
            cancelOrderDto.setCreateTime(getInfo.getCreateTime());
            cancelOrderDto.setId(getInfo.getId());
            Long cancelOrderID = getInfo.getId();
            cancelOrderDto.setSubmitTimes(getInfo.getSubmitTimes());
        	cancelOrderDtos.add(cancelOrderDto);
        	
        	//子表有多少行，先记录到这里，传回页面，用于画表单
        	List<CancelOrderSub> cancelOrderSub = cancelOrderSubManager.findBy("cancelOrderID",cancelOrderID);
            int count = cancelOrderSub.size();
            cancelOrderDto.setHidTotal(count);
            cancelOrderDto.setOrdersub(cancelOrderSub);
        }
         return cancelOrderDtos;
    }
    
    //审批环节：先将表单内容取出
    @GET
    @Path("getCancelOrderSubInfo")
    public List<CancelOrderSubDTO> getCancelOrderSubInfo(@RequestParam("id") String  id) {
    	//取撤单的主表的主键
    	 List<CancelOrder> cancelOrder = cancelOrderManager.findBy("processInstanceId",id);
    	 String cancelOrderID = null  ;
    			 for (CancelOrder getInfo : cancelOrder) {
    				 cancelOrderID = Long.toString(getInfo.getId()) ;
    			}
    	//取撤单的子表的数据
         List<CancelOrderSub> cancelOrderSub = cancelOrderSubManager.findBy("cancelOrderID",Long.parseLong(cancelOrderID));
         List<CancelOrderSubDTO> cancelOrderSubDtos = new ArrayList<CancelOrderSubDTO>();
     
         for (CancelOrderSub getInfo : cancelOrderSub) {
        	CancelOrderSubDTO cancelOrderSubDto = new CancelOrderSubDTO();
        	cancelOrderSubDto.setCancelType(getInfo.getCancelType());
        	cancelOrderSubDto.setCancelMoney(getInfo.getCancelMoney());
            cancelOrderSubDto.setSaleID(getInfo.getSaleID());
            cancelOrderSubDto.setUcode(getInfo.getUcode());
            cancelOrderSubDto.setUserName(getInfo.getUserName());
            cancelOrderSubDto.setAddTime(getInfo.getAddTime());

            cancelOrderSubDtos.add(cancelOrderSubDto);
        }
         return cancelOrderSubDtos;
    }
    
	 // ~ ======================================================================
    /**
     * 完成任务.
     */
    @GET
    @RequestMapping("cancelOrder-completeTask")
    @Log(desc = "审批流程", action = "confirmProcess", operationDesc = "流程中心-我的审批-待办审批-撤单")
    public String completeTask(HttpServletRequest request,
            RedirectAttributes redirectAttributes,
            @RequestParam("processInstanceId") String processInstanceId, 
            @RequestParam("humanTaskId") String humanTaskId,
            String flag
           ) throws Exception {

    	 try {
 		     	cancelOrderService.CompleteTaskCancelOrder(request, redirectAttributes,
					processInstanceId, humanTaskId);
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
  
    //验证密码是否正确
    @GET
    @Path("cancelOrder-verifyPassword")
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
     * 申请单详情或打印页
     * */
    @RequestMapping("cancelOrder-detail")
    @Log(desc = "查看详情页", action = "processDetail", operationDesc = "流程中心-撤单-详情")
    public String formDetail( @RequestParam("processInstanceId") String processInstanceId,
    		@RequestParam(value = "isPrint", required = false) boolean isPrint,
    		@RequestParam(value = "viewBack", required = false) boolean viewBack,
    		Model model){
    	//this.getCancelOrderInfo(processInstanceId);
    	//this.getCancelOrderSubInfo(processInstanceId);
    	 //审批记录
        List<HumanTaskDTO> logHumanTaskDtos = humanTaskConnector
                .findHumanTasksForPositionByProcessInstanceId(processInstanceId);
        //获得审核时长
        logHumanTaskDtos = operationService.settingAuditDuration(logHumanTaskDtos);
        model.addAttribute("logHumanTaskDtos", logHumanTaskDtos);
        model.addAttribute("isPrint", isPrint);
        model.addAttribute("viewBack", viewBack);
        operationService.copyMsgUpdate(processInstanceId);

        return "operation/cancel-order-print";
      
        //return "operation/cancel-order-showForm";
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
    public void setOperationService(OperationService operationService) {
        this.operationService = operationService;
    }

    
    @Resource
    public void setHumanTaskConnector(HumanTaskConnector humanTaskConnector) {
        this.humanTaskConnector = humanTaskConnector;
    }

    
 // ~ ======================================================================
    @Resource
    public void setCancelOrderManager(CancelOrderManager cancelOrderManager) {
        this.cancelOrderManager = cancelOrderManager;
    }
    @Resource
    public void setCancelOrderSubManager(CancelOrderSubManager cancelOrderSubManager) {
        this.cancelOrderSubManager = cancelOrderSubManager;
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
	public void setCancelOrderService(CancelOrderService cancelOrderService) {
		this.cancelOrderService = cancelOrderService;
	}  
    
}
