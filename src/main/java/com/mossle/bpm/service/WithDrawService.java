package com.mossle.bpm.service;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.runtime.ProcessInstance;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mossle.api.dict.DictConnector;
import com.mossle.api.humantask.HumanTaskConnector;
import com.mossle.api.humantask.HumanTaskDTO;
import com.mossle.api.keyvalue.KeyValueConnector;
import com.mossle.api.notification.NotificationConnector;
import com.mossle.bpm.persistence.manager.BpmConfBaseManager;
import com.mossle.bpm.persistence.manager.BpmConfNodeManager;
import com.mossle.bpm.persistence.manager.BpmProcessManager;
import com.mossle.bpm.rs.BpmResource;
import com.mossle.core.auth.CurrentUserHolder;
import com.mossle.core.util.BaseDTO;
import com.mossle.humantask.persistence.domain.TaskInfo;
import com.mossle.humantask.persistence.manager.TaskInfoManager;
import com.mossle.msg.MsgConstants;
import com.mossle.msg.persistence.domain.MsgInfo;
import com.mossle.msg.persistence.manager.MsgInfoManager;
import com.mossle.operation.persistence.domain.CustomEntity;
import com.mossle.operation.persistence.manager.CustomManager;
import com.mossle.operation.service.CustomService;
import com.mossle.operation.service.OperationService;
import com.mossle.spi.process.InternalProcessConnector;

@Service
@Transactional(readOnly=true)
public class WithDrawService {
	
	private static Logger logger = LoggerFactory.getLogger(BpmResource.class);
	@Resource
    private ProcessEngine processEngine;
	@Resource
    private NotificationConnector notificationConnector;
    @Resource
    private TaskInfoManager taskInfoManager;
    @Resource
    private KeyValueConnector keyValueConnector;
    @Resource
    private InternalProcessConnector internalProcessConnector;
    @Resource
    private HumanTaskConnector humanTaskConnector;
    @Resource
    private BpmProcessManager bpmProcessManager;
    @Resource
    private BpmConfBaseManager bpmConfBaseManager;
    @Resource
    private BpmConfNodeManager bpmConfNodeManager;
    @Resource
    private DictConnector dictConnector;
    @Resource
    private MsgInfoManager msgInfoManager;
    @Resource
    private CustomManager customManager;
    
    private OperationService operationService;
    @Autowired
    private CustomService customService;
    /**
     * 常规流程的撤回
     * @param processInstanceId
     * @return
     */
    @Transactional(readOnly=false)
	public BaseDTO normalWithDraw(String processInstanceId,String userId){
		BaseDTO result = new BaseDTO();
        logger.debug("processInstanceId : {}", processInstanceId);
        try {
        	//此消息发送是只给流程的待办人发送消息
        	List<TaskInfo> taskInfoss = taskInfoManager.findBy("processInstanceId", processInstanceId);
        	//TaskInfo taskInfo = taskInfoss.get(taskInfoss.size()-1);
        	
        	//若流程中有撤回，那么将抄送人的消息删除
        	String assignee = "";
        	for(TaskInfo t:taskInfoss){
        		//避免撤回出错消息已发送
                if(t.getStatus().equals("active")&&!t.getCatalog().equals("copy")){
                	assignee = t.getAssignee();
                }
        		String catalog = t.getCatalog();
        		if(catalog == null){
        			continue;
        		}
        		if(catalog.equals("copy")){
        			MsgInfo m = msgInfoManager.findUniqueBy("data", t.getId().toString());
        			if(m != null){
        				msgInfoManager.removeById(m.getId());
        			}else{
        				continue;
        			}
            	}
        	}
        	
        	//流程撤回后将旧消息的类型置为0
        	String sql = "from TaskInfo where (action='同意' or action='驳回' or action is null) and businessKey=? and catalog='normal'";
        	List<TaskInfo> approvaltaskInfos = taskInfoManager.find(sql, taskInfoss.get(0).getBusinessKey());
        	for(TaskInfo TaskInfo:approvaltaskInfos){
        		MsgInfo msgInfo = msgInfoManager.findUniqueBy("data", TaskInfo.getId().toString());
        		if(msgInfo == null){//流程中的审核环节可能是认领的审核人，他没有消息提示
        			continue;
        		}
        		msgInfo.setType(0);
        		msgInfo.setStatus(1);//已读
        	}
            ProcessInstance processInstance = processEngine.getRuntimeService()
                    .createProcessInstanceQuery()
                    .processInstanceId(processInstanceId).singleResult();
            String initiator = "";
            String firstUserTaskActivityId = internalProcessConnector
                    .findFirstUserTaskActivityId(
                            processInstance.getProcessDefinitionId(), initiator);

            logger.debug("firstUserTaskActivityId : {}", firstUserTaskActivityId);

            List<HistoricTaskInstance> historicTaskInstances = processEngine
                    .getHistoryService().createHistoricTaskInstanceQuery()
                    .processInstanceId(processInstanceId)
                    .taskDefinitionKey(firstUserTaskActivityId).list();
            //2017-12-29 shijingxin 撤销次数大于1时调用
            if (historicTaskInstances.size() == 0) {
                String hql = "from TaskInfo where (action = '重新申请' or action = '重新调整申请') and processInstanceId=?";
                List<TaskInfo> taskInfos = taskInfoManager.find(hql, processInstanceId);
                firstUserTaskActivityId = taskInfos.get(0).getCode();

                historicTaskInstances = processEngine
                        .getHistoryService().createHistoricTaskInstanceQuery()
                        .processInstanceId(processInstanceId)
                        .taskDefinitionKey(firstUserTaskActivityId).list();
            }
            String taskId = "";
            if(historicTaskInstances.size()!=0){
            	HistoricTaskInstance historicTaskInstance = historicTaskInstances
                        .get(0);
                taskId = historicTaskInstance.getId();
                
            }else{
            	taskId = taskInfoss.get(0).getTaskId();
            }
            HumanTaskDTO humanTaskDto = humanTaskConnector
                    .findHumanTaskByTaskId(taskId);
            String comment = "";
            humanTaskConnector.withdraw(humanTaskDto.getId(), comment);
            
            //给审核人发送消息（剔除重复审核人id） shijingxin 2018-01-09
            //List<TaskInfo> taskInfos = taskInfoManager.findByLike("processInstanceId", processInstanceId);
            
            /*for (int k = 1; k < taskInfos.size() - 1; k++) {
                if (taskInfos.get(k).getAction().equals("重新申请")) {
                    set.clear();
                }
                if (taskInfos.get(k).getStatus().equals("complete") && (taskInfos.get(k).getAction().equals("同意") || taskInfos.get(k).getAction().equals("驳回"))) {
                    appId = taskInfos.get(k).getAssignee();
                    set.add(appId);
                }
            }
            for (String userId : set) {
                notificationConnector.send(taskInfos.get(0).getBusinessKey(), "1", taskInfos.get(0).getAssignee(), userId, taskInfos.get(0).getPresentationSubject() + "流程已撤回", taskInfos.get(0).getPresentationSubject() + "流程已撤回", MsgConstants.MSG_TYPE_BPM);
            }*/
            //return "redirect:/bpm/workspace-listRunningProcessInstances.do";
            String sqlRecordUpdate = "update KV_RECORD set audit_status = '8' where BUSINESS_KEY= '" + taskInfoss.get(0).getBusinessKey() + "'";
            keyValueConnector.updateBySql(sqlRecordUpdate);
            //发送消息
            if(StringUtils.isNotBlank(assignee)){
            	notificationConnector.send(taskInfoss.get(0).getBusinessKey(), "1", taskInfoss.get(0).getAssignee(), assignee,"【"+ taskInfoss.get(0).getPresentationSubject()+"】" + "流程已撤回", "【"+ taskInfoss.get(0).getPresentationSubject()+"】" + "流程已撤回", MsgConstants.MSG_TYPE_BPM);
            }
            String hql = "from TaskInfo where name='调整人' and status='active' and businessKey=?";
            TaskInfo task = taskInfoManager.findUnique(hql, taskInfoss.get(0).getBusinessKey());
            task.setAction("撤回申请");
            taskInfoManager.save(task);
            //设置审批过程中的审批岗位信息
            operationService.SetProcessPosition(taskInfoss.get(0).getBusinessKey(),userId);
            
            result.setCode(200);
        } catch (ArithmeticException e) {
            result.setCode(500);
            result.setMessage("撤回出错");
            logger.error("撤回操作-：" + e.getMessage() + "\r\n" + e.fillInStackTrace());
        }
        return result;
	}
	/**
	 * 自定义流程的撤回
	 * @param processInstanceId
	 * @return
	 */
    @Transactional(readOnly=false)
	public BaseDTO customWithDraw(String processInstanceId){
		 BaseDTO result = new BaseDTO();
	        //撤回时将待审核人的任务置为complete
	        String hql = "from TaskInfo where processInstanceId=? and status='active' and catalog='normal'";
	        String hqlForm = "from TaskInfo where processInstanceId=? and catalog='start'";
	        try {
	            TaskInfo taskInfo = taskInfoManager.findUnique(hql, processInstanceId);
	            List<TaskInfo> taskInfos = taskInfoManager.findBy("processInstanceId", processInstanceId);
	            
	            TaskInfo taskForm = taskInfoManager.findUnique(hqlForm, processInstanceId);
	            String formId = taskForm.getForm();
	            CustomEntity customEntity = customManager.findUniqueBy("id", Long.parseLong(formId));
	            //给流程待办人发送消息
	            notificationConnector.send(taskInfo.getBusinessKey(), "1", taskInfos.get(0).getAssignee(), taskInfo.getAssignee(), "【"+customEntity.getName()+"】" + "将流程" + "【"+customEntity.getTheme()+"】" + "撤回", "【"+customEntity.getName()+"】" + "将流程" + "【"+customEntity.getTheme()+"】" + "撤回", MsgConstants.MSG_TYPE_BPM);
	            taskInfo.setStatus("complete");
	            taskInfo.setAction("已被发起人撤回");
	            taskInfo.setAssignee(taskForm.getAssignee());
	            taskInfo.setCompleteTime(new Date());
	            //taskInfoManager.remove(taskInfo);

	            //List<TaskInfo> taskInfos = taskInfoManager.findBy("processInstanceId", processInstanceId);
	            //查询自定义实体表获取流程主题和发起人姓名
	            //String formId = taskInfos.get(0).getForm();
	            //CustomEntity customEntity = customManager.findUniqueBy("id", Long.parseLong(formId));
	          //若流程中有撤回，那么将抄送人的消息删除
	        	for(TaskInfo t:taskInfos){
	        		String catalog = t.getCatalog();
	        		if(catalog == null){
	        			continue;
	        		}
	        		if(catalog.equals("copy")){
	        			MsgInfo m = msgInfoManager.findUniqueBy("data", t.getId().toString());
	        			if(m != null){
	        				msgInfoManager.removeById(m.getId());
	        			}else{
	        				continue;
	        			}
	            	}
	        	}
	          //流程撤回后将旧消息的类型置为0
	        	String sql = "from TaskInfo where (name like '%审批') and businessKey=?";
	        	List<TaskInfo> approvaltaskInfos = taskInfoManager.find(sql, taskInfo.getBusinessKey());
	        	for(TaskInfo TaskInfo:approvaltaskInfos){
	        		MsgInfo msgInfo = msgInfoManager.findUniqueBy("data", TaskInfo.getId().toString());
	        		if(msgInfo == null){
	        			continue;
	        		}
	        		msgInfo.setType(0);
	        		msgInfo.setStatus(1);//已读
	        	}
	            String businessKey = taskInfos.get(0).getBusinessKey();
	            String startUserId = taskInfos.get(0).getDescription();
	            String owner = taskInfos.get(0).getOwner();
	            String taskId = taskInfos.get(0).getTaskId();
	            String presentationSubject = taskInfos.get(0).getPresentationSubject();
	            TaskInfo task = new TaskInfo();
	            task.setBusinessKey(businessKey);
	            task.setCode(startUserId);
	            //ckx  2018/7/24
	        	if(null != customEntity.getFormType() && !"".equals(customEntity.getFormType()) && !"null".equals(customEntity.getFormType())){
	        		task.setName(customEntity.getName()+"-撤回"+customEntity.getTheme());	
	        		task.setPresentationName(customEntity.getTheme());
	        		task.setAction(customEntity.getTheme()+"等待调整");
	        	}else{
	        		task.setName(customEntity.getName()+"-撤回自定义申请");
	        		task.setPresentationName("自定义申请");
	        		task.setAction("自定义申请等待调整");
	        	}
	            //task.setName(customEntity.getName() + "-撤回自定义申请");
	            task.setDescription(startUserId);
	            task.setTenantId("1");
	            task.setStatus("active");
	            //task.setSuspendStatus("自定义申请");
	            task.setPresentationSubject(presentationSubject);
	            Date date = new Date();
	            task.setCreateTime(date);
	            task.setAssignee(startUserId);
	            task.setOwner(owner);
	            task.setTaskId(taskId);
	            task.setProcessInstanceId(processInstanceId);
	            task.setCatalog("normal");
	            taskInfoManager.save(task);
	            //流程被撤回后，将待审人下的审核消息置为已读
	            String updateMsg = "update MsgInfo set status=1 where data=?";
	            msgInfoManager.batchUpdate(updateMsg, task.getId().toString());
	            //获取审核人的id
	            /*String appId = "";
	            Set<String> set = new HashSet<String>();
	            //循环遍历审核人的id，避免一条撤回给同一人发送多条消息
	            for (TaskInfo taskInfoApp : taskInfos) {
	                if (taskInfoApp.getAction().equals("重新申请")) {
	                    set.clear();
	                }
	                if (taskInfoApp.getStatus().equals("complete") && taskInfoApp.getName().indexOf("审批") > 0) {
	                    appId = taskInfoApp.getAssignee();
	                    set.add(appId);
	                }
	            }
	            //消息发送
	            for (String userId : set) {
	                notificationConnector.send(businessKey, "1", taskInfos.get(0).getAssignee(), userId, customEntity.getName() + "将流程" + customEntity.getTheme() + "撤回", customEntity.getName() + "将流程" + customEntity.getTheme() + "撤回", MsgConstants.MSG_TYPE_BPM);
	            }*/
	            //撤回时将我的未结流程状态置未已撤回
	            String sqlRecordUpdate = "update KV_RECORD set audit_status = '8' where BUSINESS_KEY= '" + taskInfos.get(0).getBusinessKey() + "'";
	            keyValueConnector.updateBySql(sqlRecordUpdate);
	            String url = "workspace-personalTasks.do";
	            
	          	 //审批岗位
	          	 customService.SetAuditPosition(taskInfos.get(0).getBusinessKey());
	            
	            result.setCode(200);
	        } catch (ArithmeticException e) {
	            result.setCode(500);
	            result.setMessage("撤回出错");
	            logger.error("撤回操作：" + processInstanceId + "-" + e.getMessage() + "\r\n" + e.fillInStackTrace());
	        } catch (Exception ex) {
	        	result.setCode(500);
	            result.setMessage("撤回出错");
	            logger.error("撤回操作：" + processInstanceId + "-" + ex.getMessage() + "\r\n" + ex.fillInStackTrace());
	        }
	        return result;
	}
    
    @Resource
    public void setOperationService(OperationService operationService) {
        this.operationService = operationService;
    }
}
