package com.mossle.operation.service;


import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;

import com.graphbuilder.math.func.AtanFunction;
import com.mossle.api.form.FormDTO;
import com.mossle.api.humantask.HumanTaskConnector;
import com.mossle.api.humantask.HumanTaskDTO;
import com.mossle.api.keyvalue.FormParameter;
import com.mossle.api.keyvalue.KeyValueConnector;
import com.mossle.api.keyvalue.Prop;
import com.mossle.api.keyvalue.Record;
import com.mossle.api.keyvalue.RecordBuilder;
import com.mossle.api.notification.NotificationConnector;
import com.mossle.api.notification.NotificationDTO;
import com.mossle.api.org.OrgConnector;
import com.mossle.api.party.PartyConnector;
import com.mossle.api.party.PartyDTO;
import com.mossle.api.process.ProcessConnector;
import com.mossle.api.process.ProcessDTO;
import com.mossle.api.user.UserConnector;
import com.mossle.api.user.UserDTO;
import com.mossle.base.persistence.domain.BusinessDetailEntity;
import com.mossle.base.persistence.manager.BusinessDetailManager;
import com.mossle.base.persistence.manager.DetailPostManager;
import com.mossle.bpm.rs.BpmResource;
import com.mossle.bpm.support.ActivityDTO;
import com.mossle.core.auth.CurrentUserHolder;
import com.mossle.humantask.persistence.domain.TaskInfo;
import com.mossle.humantask.persistence.domain.TaskInfoCopy;
import com.mossle.humantask.persistence.manager.TaskInfoCopyManager;
import com.mossle.humantask.persistence.manager.TaskInfoManager;
import com.mossle.humantask.rule.EqualsRuleMatcher;
import com.mossle.humantask.rule.PrefixRuleMatcher;
import com.mossle.keyvalue.persistence.domain.RecordInfo;
import com.mossle.keyvalue.persistence.manager.RecordManager;
import com.mossle.msg.MsgConstants;
import com.mossle.msg.persistence.domain.MsgInfo;
import com.mossle.msg.persistence.manager.MsgInfoManager;
import com.mossle.operation.persistence.domain.CodeEntity;
import com.mossle.operation.persistence.manager.CodeManager;
import com.mossle.party.PartyConstants;
import com.mossle.party.persistence.domain.PartyEntity;
import com.mossle.party.persistence.domain.PartyStruct;
import com.mossle.party.persistence.manager.PartyEntityManager;
import com.mossle.party.persistence.manager.PartyStructManager;
import com.mossle.party.support.PartyOrgConnector;
import com.mossle.spi.humantask.TaskDefinitionConnector;
import com.mossle.spi.humantask.TaskUserDTO;
import com.mossle.spi.process.InternalProcessConnector;
import com.mossle.util.DateUtil;
import com.mossle.ws.persistence.manager.OnLineInfoManager;
import com.mossle.ws.persistence.domain.OnLineInfo;
import com.mossle.keyvalue.persistence.manager.RecordManager;
import com.mossle.keyvalue.persistence.domain.RecordInfo;

@Service
@Transactional(readOnly = true)
public class OperationService {
	
	private static Logger logger = LoggerFactory.getLogger(OperationService.class);
	
	public static final String OPERATION_BUSINESS_KEY = "businessKey";
	public static final String OPERATION_TASK_ID = "taskId";
	public static final String OPERATION_BPM_PROCESS_ID = "bpmProcessId";
	public static final int STATUS_DRAFT_PROCESS = 0;
	public static final int STATUS_DRAFT_TASK = 1;
	public static final int STATUS_RUNNING = 2;
	
	private KeyValueConnector keyValueConnector;
	private HumanTaskConnector humanTaskConnector;
	private ProcessConnector processConnector;
	private PartyConnector partyConnector;
	
	private TaskInfoManager taskInfoManager;
	private InternalProcessConnector internalProcessConnector;
	private UserConnector userConnector;
	private String baseUrl;
	private NotificationConnector notificationConnector;

	@Resource
	private CodeManager codeManager;
	@Resource
	private BusinessDetailManager businessDetailManager;
	@Resource
	private BpmResource bpmResource;
	@Resource
	private PartyEntityManager partyEntityManager;
	@Resource
	private PartyStructManager partyStructManager;

	private OnLineInfoManager onLineInfoManager;

	private JdbcTemplate jdbcTemplate;
	
	private RecordManager recordManager;
	private OrgConnector orgConnector;
	private CurrentUserHolder currentUserHolder;
	@Autowired
	private PartyOrgConnector partyOrgConnector;
	private TaskDefinitionConnector taskDefinitionConnector;
	@Autowired
	private MsgInfoManager msgInfoManager;
    @Autowired
    private TaskInfoCopyManager taskInfoCopyManager;

	/**
	 * 保存草稿.
	 */
	@Transactional(readOnly = false)
	public String saveDraft(String userId, String tenantId, FormParameter formParameter) {
		
		
		String humanTaskId = formParameter.getHumanTaskId();
		String businessKey = formParameter.getBusinessKey();
		String bpmProcessId = formParameter.getBpmProcessId();

		if (StringUtils.isNotBlank(humanTaskId)) {
			// 如果是任务草稿，直接通过processInstanceId获得record，更新数据
			// TODO: 分支肯定有问题
			HumanTaskDTO humanTaskDto = humanTaskConnector.findHumanTask(humanTaskId);

			if (humanTaskDto == null) {
				throw new IllegalStateException("任务不存在");
			}

			String processInstanceId = humanTaskDto.getProcessInstanceId();
			Record record = keyValueConnector.findByRef(processInstanceId);

			if (record != null) {
				record = new RecordBuilder().build(record, STATUS_DRAFT_TASK, formParameter);
				keyValueConnector.save(record);
				businessKey = record.getCode();

				/*if (record.getBusinessKey() == null) {
					record.setBusinessKey(businessKey);
					keyValueConnector.save(record);
				}*/
			}
		} else if (StringUtils.isNotBlank(businessKey)) {
			// 如果是流程草稿，直接通过businessKey获得record，更新数据
			Record record = keyValueConnector.findByCode(businessKey);

			record = new RecordBuilder().build(record, STATUS_DRAFT_PROCESS, formParameter);
			keyValueConnector.save(record);
		} else if (StringUtils.isNotBlank(bpmProcessId)) {
			// 如果是第一次保存草稿，肯定是流程草稿，先初始化record，再保存数据
			Record record = new RecordBuilder().build(bpmProcessId, STATUS_DRAFT_PROCESS, formParameter, userId,
					tenantId);

			// 取流程主题
			if (StringUtils.isBlank(record.getTheme())) {
				String sql ="select p.name from bpm_process p"
		        		+ " where p.id=?";
		        Map<String, Object> map = null;
		        //ckx 增加try catch  防止查询为空报异常  2018/11/12
				try {
					map = jdbcTemplate.queryForMap(sql, bpmProcessId);
				} catch (DataAccessException e) {
				}
		        
		        String userName = currentUserHolder.getName();
		        
		        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");

		        String formatStr =formatter.format(new Date());
		        if(null != map){
		        	// System.out.println(getStringValue(map, "name") + "-" + userName + "-" + formatStr);
		        	record.setTheme(getStringValue(map, "name") + "-" + userName + "-" + formatStr);
		        	record.setName(getStringValue(map, "name"));
		        }
			}
			
			if ("-1".equals(bpmProcessId)) {
				record.setName("自定义申请");
			}

			PartyDTO company = partyConnector.findCompanyById(userId);
			//&& company.getName().indexOf("分公司") >= 0
			if (company != null) {//必须为大区下的分公司才存表
				record.setCompanyId(company.getId());
				record.setCompanyName(company.getName());
			}

			keyValueConnector.save(record);
			businessKey = record.getCode();

			/*if (record.getBusinessKey() == null) {
				record.setBusinessKey(businessKey);
				keyValueConnector.save(record);
			}*/
		} else {
			logger.error("humanTaskId, businessKey, bpmProcessId all null : {}", formParameter.getMultiValueMap());
			throw new IllegalArgumentException("humanTaskId, businessKey, bpmProcessId all null");
		}

		return businessKey;
	}

	/**
	 * 保存草稿.
	 */
	@Transactional(readOnly = false)
	public FormParameter saveDraft(String userId, String tenantId, String humanTaskId, String businessKey,
			String bpmProcessId, MultiValueMap<String, String> multiValueMap) {

		FormParameter formParameter = new FormParameter(multiValueMap);

		formParameter.setHumanTaskId(humanTaskId);
		formParameter.setBusinessKey(businessKey);
		formParameter.setBpmProcessId(bpmProcessId);

		if (StringUtils.isNotBlank(humanTaskId)) {
			// 如果是任务草稿，直接通过processInstanceId获得record，更新数据
			// TODO: 分支肯定有问题
			HumanTaskDTO humanTaskDto = humanTaskConnector.findHumanTask(humanTaskId);

			if (humanTaskDto == null) {
				throw new IllegalStateException("任务不存在");
			}

			String processInstanceId = humanTaskDto.getProcessInstanceId();
			Record record = keyValueConnector.findByRef(processInstanceId);

			if (record != null) {
				record = new RecordBuilder().build(record, STATUS_DRAFT_TASK, formParameter);
				keyValueConnector.save(record);
				businessKey = record.getCode();

				/*if (record.getBusinessKey() == null) {
					record.setBusinessKey(businessKey);
					keyValueConnector.save(record);
				}*/
			}
		} else if (StringUtils.isNotBlank(businessKey)) {
			// 如果是流程草稿，直接通过businessKey获得record，更新数据
			Record record = keyValueConnector.findByCode(businessKey);

			record = new RecordBuilder().build(record, STATUS_DRAFT_PROCESS, formParameter);
			keyValueConnector.save(record);
		} else if (StringUtils.isNotBlank(bpmProcessId)) {
			// 如果是第一次保存草稿，肯定是流程草稿，先初始化record，再保存数据
			Record record = new RecordBuilder().build(bpmProcessId, STATUS_DRAFT_PROCESS, formParameter, userId,
					tenantId);

			if (!"-1".equals(bpmProcessId)) {
				ProcessDTO processDto = processConnector.findProcess(bpmProcessId);
				record.setName(processDto.getProcessDefinitionName());
			} else {
				record.setName("自定义申请");
			}

			PartyDTO company = partyConnector.findCompanyById(userId);

			if (company != null) {
				record.setCompanyId(company.getId());
				record.setCompanyName(company.getName());
			}

			keyValueConnector.save(record);
			businessKey = record.getCode();

			/*if (record.getBusinessKey() == null) {
				record.setBusinessKey(businessKey);
				keyValueConnector.save(record);
			}*/
		} else {
			logger.error("humanTaskId, businessKey, bpmProcessId all null ");
			throw new IllegalArgumentException("humanTaskId, businessKey, bpmProcessId all null");
		}

		formParameter.setBusinessKey(businessKey);
		return formParameter;
	}

	/**
	 * 发起流程.
	 */
	@Transactional(readOnly = false)
	public void startProcessInstance(String userId, String businessKey, String processDefinitionId,
			Map<String, Object> processParameters, Record record) {
		//针对流程发起页面（没有选择细分做出的调整），这里外层是通过流程ID找到细分
		String postId="";
		if(processParameters.containsKey("positionId")){
			postId=processParameters.get("positionId").toString();
			processParameters.remove("positionId");
		}
			
		String processInstanceId = processConnector.startProcess(userId, businessKey, processDefinitionId,
				processParameters);
		
		//设置发起人的岗位
		TaskInfo taskStart=taskInfoManager.findUnique("from TaskInfo where catalog='start' and businessKey=?", businessKey);
		if(taskStart!=null){
			/*if(StringUtils.isNotBlank(record.getBusinessDetailId()))
				strBusinessDetailId=record.getBusinessDetailId();
            String strSql=String.format("SELECT * FROM oa_ba_business_post WHERE detail_id='%s' " 
    				+" AND post_id in(select PARENT_ENTITY_ID from party_struct s WHERE s.CHILD_ENTITY_ID=%s AND s.STRUCT_TYPE_ID=4)",
    						strBusinessDetailId,
    						userId);
    		List<Map<String,Object>> mapPositionList=jdbcTemplate.queryForList(strSql);
    		
    		Long postId=0L;
    		String position_Type="1";
    		if(mapPositionList!=null&&mapPositionList.size()>0){
    			//默认为真实岗位
    			postId=Long.parseLong(mapPositionList.get(0).get("post_id").toString());
        		//查询是否为虚拟岗位
        		strSql="SELECT * FROM PARTY_ENTITY_ATTR WHERE isRealPosition='1' AND ID=?";
        		String strPositionRealIds="";
        		List<Map<String,Object>> mapPostAttrList=jdbcTemplate.queryForList(strSql,postId);
        		if(mapPostAttrList!=null&&mapPostAttrList.size()>0)
        			strPositionRealIds=mapPostAttrList.get(0).get("positionRealIds").toString();
        		
        		//如果有虚拟对应的真实岗位
        		if(!strPositionRealIds.equals("")){
        			String[] positionRealIdArray=strPositionRealIds.split(",");
        			strSql=String.format("SELECT PARENT_ENTITY_ID FROM party_struct WHERE CHILD_ENTITY_ID=%s AND STRUCT_TYPE_ID=4", userId);
        			List<String> postPartyIdList=jdbcTemplate.queryForList(strSql,String.class); 
        			
        			//真实岗位中查询属于发起人的岗位
        			for(String positionRealId:positionRealIdArray){
        				if(postPartyIdList.contains(positionRealId)){
        					postId=Long.parseLong(positionRealId);
        					break;
        				}
        			}
        		}
    		}
    		else {
				//如果此人没有岗位，则 存入此人的ID
    			postId=Long.valueOf(userId);
    			position_Type="2";
			}*/
			String position_Type="1";
			PartyEntity partyEntity=partyEntityManager.get(Long.valueOf(postId));
			if(partyEntity!=null){
				if(partyEntity.getPartyType().getId().equals(PartyConstants.PARTY_TYPE_USER))
					position_Type="2";
			}
			
			Map<String,String> mapPosition=partyOrgConnector.getParentPartyEntityId(postId.toString());
			String strSql=String.format("insert into task_info_approve_position(task_id,position_id,position_type,BUSINESS_KEY,position_parentId,approve_position_name) value(%s,%s,'%s','%s',%s,'%s')", 
				taskStart.getId(),
				postId,
				position_Type,
				businessKey,
				mapPosition.get("parent_id"),
				mapPosition.get("position_name"));
			keyValueConnector.updateBySql(strSql);
        }
		//设置审核人的岗位
		SetProcessPosition(businessKey,userId);
				
		record = new RecordBuilder().build(record, STATUS_RUNNING, processInstanceId);
		record.setSubmitTimes(1);// Bing 2017.11.18
		keyValueConnector.save(record);
	}

	/**
	 * 完成任务.
	 */
	@SuppressWarnings("unchecked")
	@Transactional(readOnly = false)
	public void completeTask(String humanTaskId, String userId, FormParameter formParameter,
			Map<String, Object> taskParameters, Record record, String processInstanceId) {
		try {
			humanTaskConnector.completeTask(humanTaskId, userId, formParameter.getAction(), formParameter.getComment(),
				taskParameters);
		} catch (Exception ex) {
			throw new IllegalStateException(ex);
		}
		
		/*****************************************************************************************************
		 * 
         * 重新写流程的审批流程的状态
         * 0:未审核；1：审核中；2：审核已通过；3：审核未通过；4：审核中（驳回）；6：已取消；7：驳回发起人；8：已撤回
         * add by lilei at 2018-08-27
         * ***************************************************************************************************/
		//region 设置固定流程状态
		String strOpterComment=taskParameters.get("leaderComment").toString();
		String strBusinessKey=formParameter.getBusinessKey();
		RecordInfo recordInfo=recordManager.findUniqueBy("businessKey", strBusinessKey);
		String hsqlString="FROM TaskInfo WHERE businessKey=? AND status='active' AND catalog='normal'";
		List<TaskInfo> taskList=taskInfoManager.find(hsqlString,strBusinessKey);
		if(strOpterComment.equals("同意")){
			recordInfo.setAuditStatus("1");
			
			/*String hsqlString="FROM TaskInfo WHERE businessKey=? AND status='active' AND catalog='normal'";
			List<TaskInfo> taskList=taskInfoManager.find(hsqlString, 
														strBusinessKey);*/
			if(taskList==null||(taskList!=null&&taskList.size()<1)) {
				recordInfo.setAuditStatus("2");
				
				String hsql="FROM TaskInfo WHERE businessKey=?  AND catalog='copy'";
				List<TaskInfo> copyList=taskInfoManager.find(hsql,strBusinessKey);
				for (TaskInfo info : copyList) {
					//String 
					String hsqlMsg = "FROM MsgInfo WHERE data=? and isSendMsg='2'";
					List<MsgInfo> msgList = msgInfoManager.find(hsqlMsg, info.getId().toString());
					for (MsgInfo msgInfo : msgList) {
						msgInfo.setIsSendMsg("0");
						msgInfoManager.update(msgInfo);
					}
				}
			}
		} else if(strOpterComment.equals("驳回")){
			recordInfo.setAuditStatus("4");
			
			TaskInfo taskInfo=taskList.get(0);
			hsqlString=" FROM TaskInfo WHERE businessKey=? AND catalog='start' ";
			TaskInfo taskInfoStart=taskInfoManager.findUnique(hsqlString, strBusinessKey);
			if(taskInfo.getAssignee()!=null){
				if(taskInfo.getAssignee().equals(taskInfoStart.getAssignee())){
					hsqlString="FROM TaskInfo WHERE businessKey=? AND catalog='normal' AND action='同意' AND code=?";
					List<TaskInfo> taskInfoAuditList=taskInfoManager.find(hsqlString,
																		 strBusinessKey,
																		 taskInfo.getCode());
					if(taskInfoAuditList==null)
						recordInfo.setAuditStatus("7");
					else if(taskInfoAuditList.size()<1)
						recordInfo.setAuditStatus("7");
				}
			}
		}
		else if(strOpterComment.equals("不同意")){
			recordInfo.setAuditStatus("3");
		}
		else if(strOpterComment.equals("撤销申请")){
			recordInfo.setAuditStatus("6");
		}
		else if(strOpterComment.equals("调整申请")
				||strOpterComment.equals("重新申请")
				||strOpterComment.equals("重新调整申请")){
			recordInfo.setAuditStatus("1");
		}
		recordManager.save(recordInfo);
		//endregion
		
		//region 设置审批过程中的审批岗位信息
		SetProcessPosition(strBusinessKey,userId);
		//endregion
		
		//keyValueConnector.updateBySql(sql);
		
		// TODO 如果不同意，默认给流程发起人发消息  zyl 2017-11-20
		if ("不同意".equals(taskParameters.get("leaderComment"))) {
			
			TaskInfo taskInfo = taskInfoManager.get(Long.parseLong(humanTaskId));
			String templateCode = "complete";
            String type = "msg";
            UserDTO userDto = null;

            Map<String, Object> data = this.prepareData(taskInfo);
            
            String initiator = internalProcessConnector
                    .findInitiator(taskInfo.getProcessInstanceId());
            userDto = userConnector.findById(initiator);

            if (userDto != null) {
	            NotificationDTO notificationDto = new NotificationDTO();
	            notificationDto.setReceiver(userDto.getId());
	            notificationDto.setReceiverType("userid");
	            notificationDto.setTypes(Arrays.asList(type.split(",")));
	            notificationDto.setData(data);
	            notificationDto.setTemplate(templateCode);
	            notificationDto.setMsgType(MsgConstants.MSG_TYPE_BPM);
	            notificationConnector.send(notificationDto, "1");
            }
		}
		
		//TODO 处理直销OA的单子 lilei at 2018.01.03
		/*record = keyValueConnector.findByCode(formParameter.getBusinessKey());
		String strKVHql=" from Record where businessKey=?";*/
		
		/*String businessKey=formParameter.getBusinessKey();
		RecordInfo recordNew=recordManager.findUniqueBy("businessKey", businessKey);
		
		String busiDetailId=recordNew.getBusinessDetailId();
		//String auditStatus=recordNew.getAuditStatus();
		if((",1,2,3,4,5,6,7,8,9,").contains(","+busiDetailId+",")){
			String strRecordSql="select f_OnlineStatus('"+businessKey+"') as auditStatus";
			List<Map<String,Object>> mapRecordList=jdbcTemplate.queryForList(strRecordSql);
			String auditStatus=mapRecordList.get(0).get("auditStatus").toString();// recordNew.getAuditStatus();
			OnLineInfo onLineInfo=onLineInfoManager.findUniqueBy("id", Long.parseLong(recordNew.getPkId()));
			//更新直销流程审核时间和备注
			if(auditStatus.equals("2")||auditStatus.equals("3")){
				TaskInfo taskInfo = taskInfoManager.get(Long.parseLong(humanTaskId));
				onLineInfo.setAuditremark(taskInfo.getComment());
				onLineInfo.setAudittime(taskInfo.getCompleteTime());
				onLineInfoManager.save(onLineInfo);
			}
			else if(busiDetailId.equals("9")&&auditStatus.equals("1")){
				//初审通过发送短信。非12万旗舰店申请
				String strHql=" from TaskInfo where businessKey=? and status=? and catalog=? and assignee=?";
				List<TaskInfo> taskList=taskInfoManager.find(strHql, 
													businessKey,
													"active",
													"normal",
													"4");
				String strTaskSql=String.format("select * from task_info where BUSINESS_KEY=%s and CATALOG='normal' and `STATUS`='active' and ASSIGNEE='4'",
						record.getBusinessKey());
				List<Map<String,Object>> mapTaskList=jdbcTemplate.queryForList(strTaskSql);
				if(taskList!=null&&taskList.size()>0){
					String strSql="INSERT INTO time_task(taskType,taskContent,taskAddDate,taskNote)"
					 +" VALUES('sendmsg',concat('您编号','"+onLineInfo.getUcode()+"','的【旗舰店申请】初审已通过，请上传资料！！'),now(),'"+onLineInfo.getMobile()+"')";
					jdbcTemplate.update(strSql);
				}
			}
			
			//审核结束发送短信
			if((busiDetailId.equals("8")||busiDetailId.equals("9"))&&auditStatus.equals("2")){
				String strSql="INSERT INTO time_task(taskType,taskContent,taskAddDate,taskNote)"
						 +" VALUES('sendmsg',concat('您编号','"+onLineInfo.getUcode()+"','的【旗舰店申请】已审核通过，请登录系统查看！！'),now(),'"+onLineInfo.getMobile()+"')";
				jdbcTemplate.update(strSql);
			}
			else if((busiDetailId.equals("8")||busiDetailId.equals("9"))
					&&auditStatus.equals("3")){
				String strSql="INSERT INTO time_task(taskType,taskContent,taskAddDate,taskNote)"
						 +" VALUES('sendmsg',concat('您编号','"+onLineInfo.getUcode()+"','的【旗舰店申请】审核未通过，请登录系统查看原因！！'),now(),'"+onLineInfo.getMobile()+"')";
				jdbcTemplate.update(strSql);
			}
			
			//非12万旗舰店申请，调用直销OA的任务
			if((busiDetailId.equals("9")||busiDetailId.equals("8"))&&
				(auditStatus.equals("2")||auditStatus.equals("3"))){
				String strBranch="F01";
				if(onLineInfo.getBranch().equals("二部"))
					strBranch="F02";
				else if(onLineInfo.getBranch().equals("四部"))
					strBranch="F04";
				
				String strSuccess="true";
				if(auditStatus.equals("3"))
					strSuccess="false";
				
				String strSql="INSERT INTO time_task(taskType,taskContent,taskAddDate,taskNote)"
							 +" VALUES('serviceapplyshop','"+onLineInfo.getUcode()+"',now(),'"+strBranch+","+strSuccess+"');";
				jdbcTemplate.update(strSql);
			}
		}*/
	}
	
	/************************************
	 * 
	 * 设置审批过程中的审批岗位信息
	 * add by lilei at 2017-08-30
	 * 
	 * **********************************/
	public void SetProcessPosition(String strBusinessKey,String userId){
		String strTaskInfoSql=String.format("SELECT i.ID AS TASK_INFO_ID,ifnull(i.assignee,'') AS assignee,u.*,i.`code`,i.PROCESS_DEFINITION_ID AS processInstanceId FROM task_info i "
				+" INNER JOIN bpm_conf_base b ON i.PROCESS_DEFINITION_ID=b.PROCESS_DEFINITION_ID"
				+" INNER JOIN bpm_conf_node n ON n.CONF_BASE_ID=b.ID AND n.`CODE`=i.`CODE`"
				+" INNER JOIN bpm_conf_user u ON u.NODE_ID=n.ID"
				+" LEFT JOIN task_info_approve_position p ON i.ID=p.task_id"
				+" WHERE i.BUSINESS_KEY='%s' AND i.CATALOG<>'copy' AND p.task_id is NULL",strBusinessKey);
		
		List<Map<String,Object>> mapTaskList=jdbcTemplate.queryForList(strTaskInfoSql);
		if(mapTaskList!=null){
			if(mapTaskList.size()>0){
				for(Map<String,Object> mapTask:mapTaskList){
					//String strTaskPartSql=String.format("", args);
					String strBpmUserValue=mapTask.get("VALUE").toString();
					//类似发起人，调整人等等
					if(strBpmUserValue.equals("常用语:流程发起人")){
						//第一个发起人的SQL
						String strStartSql=String.format("SELECT P.* FROM task_info i"
								+ " INNER JOIN task_info_approve_position p ON i.ID=p.task_id"
								+ " WHERE i.BUSINESS_KEY='%s'"
								+ " ORDER BY ID ASC LIMIT 0,1",strBusinessKey);
						List<Map<String,Object>> mapStartList=jdbcTemplate.queryForList(strStartSql);
						if(mapStartList!=null)
						{
							if(mapStartList.size()>0){
								Map<String,Object> mapStart=mapStartList.get(0);
								Map<String,String> mapPosition=partyOrgConnector.getParentPartyEntityId(mapStart.get("position_id").toString());
								String strStartInsertSql=String.format("insert into task_info_approve_position(task_id,position_id,position_type,BUSINESS_KEY,position_parentId,approve_position_name) "
													+" values(%s,%s,'%s','%s',%s,'%s')",
													mapTask.get("TASK_INFO_ID"),
													mapStart.get("position_id"),
													'1',
													strBusinessKey,
													mapPosition.get("parent_id"),
													mapPosition.get("position_name")
													);
								keyValueConnector.updateBySql(strStartInsertSql);
							}
						}
					}
					else {
						//如果task_info有审批人
						if(!mapTask.get("assignee").equals(""))
							userId=mapTask.get("assignee").toString();
						else{
							//如果没有审批人则去待领找
							String strTaskPartSql=String.format("SELECT * FROM task_participant WHERE TASK_ID=%s",mapTask.get("TASK_INFO_ID").toString());
							List<Map<String,Object>> mapTaskPartList=jdbcTemplate.queryForList(strTaskPartSql);
							if(mapTaskPartList!=null&&mapTaskPartList.size()>0){
								/****************************************************
								 * 走待领规则：
								 * 1.如果待领只有一条信息则：是岗位则存入；是人员则赋值
								 * 2.如果是多条说明，说明存入的可定不是岗位
								 * **************************************************/
								if(mapTaskPartList.size()==1){
									Map<String,Object> mapTaskPart=mapTaskPartList.get(0);
									PartyEntity partyEntity=partyEntityManager.findUniqueBy("id", Long.parseLong(mapTaskPart.get("REF").toString()));
									if(partyEntity!=null){
										//如果是岗位
										if(partyEntity.getPartyType().getId().equals(PartyConstants.PARTY_TYPE_POST)){
											//如果是岗位插入进去，继续循环下一个
											Map<String,String> mapPosition=partyOrgConnector.getParentPartyEntityId(mapTaskPart.get("REF").toString());
											String strStartInsertSql=String.format("insert into task_info_approve_position(task_id,position_id,position_type,BUSINESS_KEY,position_parentId,approve_position_name) "
													+" values(%s,%s,'%s','%s',%s,'%s')",
													mapTask.get("TASK_INFO_ID"),
													mapTaskPart.get("REF").toString(),
													'1',
													strBusinessKey,
													mapPosition.get("parent_id"),
													mapPosition.get("position_name")
													);
						    				keyValueConnector.updateBySql(strStartInsertSql);
						    				continue;
										}
										else if(partyEntity.getPartyType().getId().equals(PartyConstants.PARTY_TYPE_USER)){
											//如果是人员，则赋值
											userId=partyEntity.getId().toString();
										}
										else{
											//既不是岗位，也不是人员，则不处理，等待下次审批处理
											continue;
										}
									}
									else {
										//在party_entity没有找到数据？没法处理^_^
										continue;
									}
								}
								else{
									//如果是多个人，都取出来
									userId="";
									for(Map<String,Object> mapTaskPart:mapTaskPartList){
										userId+=mapTaskPart.get("REF").toString()+",";
									}
									userId=userId.substring(0, userId.length()-1);
								}
							}
							else{
								//如果待领表没有数据，则不处理，等待下次审批处理
								continue;
							}
						}
						
						if(strBpmUserValue.indexOf("岗位:")>-1){
							//默认为审批的岗位
							String postId = strBpmUserValue.split(":")[1];
							
							//查询是否为虚拟岗位，的另行处理
				    		String strSql="SELECT * FROM PARTY_ENTITY_ATTR WHERE isRealPosition='1' AND ID=?";
				    		String strPositionRealIds="";
				    		List<Map<String,Object>> mapPostAttrList=jdbcTemplate.queryForList(strSql,postId);
				    		if(mapPostAttrList!=null&&mapPostAttrList.size()>0)
				    			strPositionRealIds=mapPostAttrList.get(0).get("positionRealIds").toString();
				    		//如果有虚拟对应的真实岗位
				    		if(!strPositionRealIds.equals("")){
				    			//postId="";
				    			String[] positionRealIdArray=strPositionRealIds.split(",");
				    			strSql=String.format("SELECT DISTINCT PARENT_ENTITY_ID FROM party_struct WHERE CHILD_ENTITY_ID in(%s) AND STRUCT_TYPE_ID=4", userId);
				    			List<String> postPartyIdList=jdbcTemplate.queryForList(strSql,String.class);
				    			
				    			List<String> userPostIdList=new ArrayList<String>();
				    			//真实岗位中查询属于发起人的岗位
				    			for(String positionRealId:positionRealIdArray){
				    				if(postPartyIdList.contains(positionRealId)){
				    					userPostIdList.add(positionRealId);
				    				}
				    			}
				    			
				    			//postId=userPostIdList.get(0);
				    			
				    			if(userPostIdList.size()==1){
				    				postId=userPostIdList.get(0);
				    			}
				    			else if(userPostIdList.size()>1){
				    			
		    				    String taskDefinitionKey = mapTask.get("code").toString();
		    			        String processDefinitionId = mapTask.get("processInstanceId").toString();
		    				    List<TaskUserDTO> taskUsers = taskDefinitionConnector.findTaskUsers(taskDefinitionKey, processDefinitionId);
		    				    TaskUserDTO taskUserDTO=taskUsers.get(0);
		    				    String strComOrAreaId=""; 
		    				    String strStartSql=String.format("SELECT P.* FROM task_info i"
										+ " INNER JOIN task_info_approve_position p ON i.ID=p.task_id"
										+ " WHERE i.BUSINESS_KEY='%s'"
										+ " ORDER BY ID ASC LIMIT 0,1",strBusinessKey);
								List<Map<String,Object>> mapStartList=jdbcTemplate.queryForList(strStartSql);
								if(mapStartList!=null)
								{
									if(mapStartList.size()>0){
										String strStartPostId=mapStartList.get(0).get("position_id").toString();
										if(taskUserDTO.getCatalog().equals("sameareacandidate")){//同一大区
											//如果是经销商，找kv_record里面的大区
											if(strStartPostId.equals(PartyConstants.JXS_ID)){
												Record record = keyValueConnector.findByCode(strBusinessKey);
												if(record!=null){
													strComOrAreaId=record.getAreaId();
												}
											}
											else {
												PartyEntity partyEntityArea=orgConnector.findPartyAreaByUserId(strStartPostId);
					    				    	if(partyEntityArea!=null){
					    				    		strComOrAreaId=partyEntityArea.getId().toString();
					    				    	}
											}
				    				    }
				    				    else if(taskUserDTO.getCatalog().equals("samecompanycandidate")){//同一分公司
				    				    	//如果是经销商，找kv_record里面的分公司
			    				    		if(strStartPostId.equals(PartyConstants.JXS_ID)){
			    				    			Record record = keyValueConnector.findByCode(strBusinessKey);
												if(record!=null){
													strComOrAreaId=record.getCompanyId();
												}
											}
			    				    		else {
			    				    			PartyEntity partyEntityCompany=orgConnector.findPartyCompanyByUserId(strStartPostId);
					    				    	if(partyEntityCompany!=null){
					    				    		strComOrAreaId=partyEntityCompany.getId().toString();
					    				    	}
											}
				    				    }
									}
								}
		    				    
								String strSamePostId="";
			    				for(String positionRealId:userPostIdList){
			    					 //* 同一大区
			    					 //* 同一分公司
			    					if(taskUserDTO.getCatalog().equals("sameareacandidate")){//同一大区
			    				    	PartyEntity partyEntityArea=orgConnector.findPartyAreaByUserId(positionRealId);
			    				    	if(partyEntityArea!=null){
			    				    		if(partyEntityArea.getId().toString().equals(strComOrAreaId)){
			    				    			strSamePostId=positionRealId;
			    				    			break;
			    				    		}
			    				    			
			    				    	}
			    				    }
			    				    else if(taskUserDTO.getCatalog().equals("samecompanycandidate")){//同一分公司
			    				    	PartyEntity partyEntityCompany=orgConnector.findPartyCompanyByUserId(positionRealId);
			    				    	if(partyEntityCompany.getId().toString().equals(strComOrAreaId)){
			    				    		strSamePostId=positionRealId;
		    				    			break;
		    				    		}
			    				    }
				    			}
			    				
			    				if(strSamePostId.equals(""))
			    					strSamePostId=userPostIdList.get(0);
			    				
			    				postId=strSamePostId;
							}
				    	}
				    		
				    		if(!postId.equals("")){
				    			Map<String,String> mapPosition=partyOrgConnector.getParentPartyEntityId(postId.toString());
					    		String strStartInsertSql=String.format("insert into task_info_approve_position(task_id,position_id,position_type,BUSINESS_KEY,position_parentId,approve_position_name) "
										+" values(%s,'%s','%s','%s',%s,'%s')",
										mapTask.get("TASK_INFO_ID"),
										postId,
										'1',
										strBusinessKey,
										mapPosition.get("parent_id"),
										mapPosition.get("position_name")
										);
			    				keyValueConnector.updateBySql(strStartInsertSql);
				    		}
						}
						else if(strBpmUserValue.equals("常用语:直接上级")||strBpmUserValue.equals("常用语:公司管理者")){
							if(!com.mossle.core.util.StringUtils.isBlank(userId)){
								String strPositionSql=String.format("SELECT DISTINCT PARENT_ENTITY_ID FROM party_struct WHERE CHILD_ENTITY_ID in(%s) AND STRUCT_TYPE_ID=4", userId);
				    			List<String> postPartyIdList=jdbcTemplate.queryForList(strPositionSql,String.class);
				    			if(postPartyIdList.size()>0){
				    				String postId=postPartyIdList.get(0);
					    			Map<String,String> mapPosition=partyOrgConnector.getParentPartyEntityId(postId.toString());
						    		String strStartInsertSql=String.format("insert into task_info_approve_position(task_id,position_id,position_type,BUSINESS_KEY,position_parentId,approve_position_name) "
											+" values(%s,'%s','%s','%s',%s,'%s')",
											mapTask.get("TASK_INFO_ID"),
											postId,
											'2',
											strBusinessKey,
											mapPosition.get("parent_id"),
											mapPosition.get("position_name")
											);
				    				keyValueConnector.updateBySql(strStartInsertSql);
				    			}
							}
						}
					}
				}
			}
		}
	}
	
	/****************
	 * 监听触发后执行
	 * 
	 * *************/
	/*public void SetProcessPosition(TaskInfo taskInfo){
		//有审批人，则找岗
		if(com.mossle.core.util.StringUtils.isNotBlank(taskInfo.getAssignee())){
			String strTaskInfoSql=String.format("SELECT i.ID AS TASK_INFO_ID,u.* FROM task_info i "
					+" INNER JOIN bpm_conf_base b ON i.PROCESS_DEFINITION_ID=b.PROCESS_DEFINITION_ID"
					+" INNER JOIN bpm_conf_node n ON n.CONF_BASE_ID=b.ID AND n.`CODE`=i.`CODE`"
					+" INNER JOIN bpm_conf_user u ON u.NODE_ID=n.ID"
					//+" LEFT JOIN task_info_approve_position p ON i.ID=p.task_id"
					+" WHERE i.BUSINESS_KEY='%s' AND i.CATALOG<>'copy' AND i.task_id=%s",taskInfo.getBusinessKey(),taskInfo.getId());
		}
		else {
			//如果assignee为空，则分两种情况，1：待领表中存入岗位，2：待领表中存入人员
		}
	}*/
	
	/**
     * 生成受理单编号
     */
	@Transactional(readOnly = false)
    public String CreateApplyCode(String userId) throws Exception {

    	String code = "";
        //取当前登录人所属公司的缩写
        PartyEntity vo = partyEntityManager.get(Long.parseLong(userId));
    	PartyStruct partyStruct = partyStructManager.findUniqueBy("childEntity",vo);
    	PartyEntity pvo = partyStruct.getParentEntity();
    	
    	if(pvo.getShortName()==null)
    		pvo.setShortName("");
    	
		SimpleDateFormat formater = new SimpleDateFormat("yyyyMMdd");
        String codeDate =  formater.format(new Date());
        
        //查下受理单编码表中是否已经存在当前登录人的编码，如果有，直接用并且等流程发起了再从oa_bpm_code表中删掉，若没有，重新生成一条编码
        String sql = "from CodeEntity where userID=? and shortName =? and createTime=? ";
        
        CodeEntity codeEntity = codeManager.findUnique(sql,userId,pvo.getShortName(),codeDate);
        //已经存在  直接用  等流程发起了再从表中删掉
        if(codeEntity!=null){
        	code = codeEntity.getReceiptNumber();
        	boolean isExits = keyValueConnector.checkApplyCodeIsUsed(codeEntity.getReceiptNumber());
        	if (isExits) {
        		this.deleteApplyCode(codeEntity.getReceiptNumber());
        		code = getNewApplyCode(userId, pvo, codeDate);
        	}
        	
        }
        //没有，重新生成一条编码
        else{
        	code = getNewApplyCode(userId, pvo, codeDate);
         }
        return code;
     }
    
	// 生成受理单号
	private String getNewApplyCode(String userId, PartyEntity pvo, String codeDate) {
		String code;
		String sql;
		//查找部门
		 sql = "from CodeEntity where  shortName =?   ";
		 List<CodeEntity> c = codeManager.find(sql,pvo.getShortName());
		 
		 //如果这个部门存在
		 if(c.size()>0)
		 {
			 //查找当天是否存在
			 sql = "from CodeEntity where  shortName =? and createTime=? order by code desc ";
		     c = codeManager.find(sql,pvo.getShortName(),codeDate);
		     
		     //当天这个部门已存在 ,取最大的编码再加一就行了
		     if(c.size()>0){
		    	 
		    	 String zero ="";
		     	int n = 6- Integer.toString(c.get(0).getCode()+1).length();
		     	for (int i =1;i<=n;i++){
		     		zero=zero+"0";
		     	}
		    	 code=pvo.getShortName()+codeDate+zero+ (c.get(0).getCode()+1);
		    	 
		    	//存入表中
		    	 CodeEntity codeEntityTemp = new CodeEntity();
		    	 codeEntityTemp.setCode(c.get(0).getCode()+1);
		    	 codeEntityTemp.setCreateTime(codeDate);
		    	 codeEntityTemp.setShortName(pvo.getShortName());
		    	 codeEntityTemp.setUserID(userId);
		    	 codeEntityTemp.setReceiptNumber(code);
		    	 codeManager.save(codeEntityTemp);
		    	 
		     }else{
		    	 //这个部门还不存在，或虽然部门存在但是当天不存在，这里是生成第一条,并存入表中
		    	 code=pvo.getShortName()+codeDate+"000001";
		    	 
		    	 //存入表中
		    	 CodeEntity codeEntityTemp = new CodeEntity();
		    	 codeEntityTemp.setCode(1);
		    	 codeEntityTemp.setCreateTime(codeDate);
		    	 codeEntityTemp.setShortName(pvo.getShortName());
		    	 codeEntityTemp.setUserID(userId);
		    	 codeEntityTemp.setReceiptNumber(code);
		    	 codeManager.save(codeEntityTemp);
		     }
		 }else{
			 //这个部门还不存在，或虽然部门存在但是当天不存在，这里是生成第一条,并存入表中
			 code=pvo.getShortName()+codeDate+"000001";
			 
			 //存入表中
			 CodeEntity codeEntityTemp = new CodeEntity();
			 codeEntityTemp.setCode(1);
			 codeEntityTemp.setCreateTime(codeDate);
			 codeEntityTemp.setShortName(pvo.getShortName());
			 codeEntityTemp.setUserID(userId);
			 codeEntityTemp.setReceiptNumber(code);
			 codeManager.save(codeEntityTemp);
		 }
		return code;
	}
	
	
	/**
     * 处理受理单编号:用户发起流程后，将oa_bpm_code表中userid清除
     */
	@Transactional(readOnly = false)
	public void deleteApplyCodeByUserId(String userId) throws Exception {

		String sql = "from CodeEntity where userID =?";

		List<CodeEntity> codeEntity =  codeManager.find(sql,userId);

		for(CodeEntity c :codeEntity){
			c.setUserID("");
			codeManager.save(c);
		}
	}
	
	@Transactional(readOnly = false)
	public void deleteApplyCode(String receiptNumber) throws Exception {

		String sql = "from CodeEntity where  receiptNumber =?";

		List<CodeEntity> codeEntity =  codeManager.find(sql,receiptNumber);

		for(CodeEntity c :codeEntity){
			c.setUserID("");
			codeManager.save(c);
		}
	}
	
	/**
     * 更新下一节点ID，用于抄送
     * @param bpmProcessId
     * @param formParameter
     */
	public void updateNextActivityId(String bpmProcessId, FormParameter formParameter, Record record, String processDefinitionId) {
		
		String activityId = "";
		
		// Record record = keyValueConnector.findByCode(formParameter.getBusinessKey());
		
		if (StringUtils.isBlank(bpmProcessId)) {
	        BusinessDetailEntity businessDetailEntity = businessDetailManager.get(Long.parseLong(record.getBusinessDetailId()));
	        bpmProcessId = businessDetailEntity.getBpmProcessId();
		}
		
		if (StringUtils.isBlank(processDefinitionId)) {
			ProcessDTO processDto = processConnector.findProcess(bpmProcessId);
			processDefinitionId = processDto.getProcessDefinitionId();
		}
        FormDTO formDto = this.processConnector.findStartForm(processDefinitionId);
        
        if (formDto != null && formDto.getActivityId() != "") { 
        	try {
		        List<ActivityDTO> nextActivity = bpmResource.findNextActivities(processDefinitionId, formDto.getActivityId());
		        if (nextActivity != null && nextActivity.size() > 0) {
		        	activityId = nextActivity.get(0).getId();
		        }
        	} catch (Exception e) {
        		logger.info("============ ActivityId:" + formDto.getActivityId());
        		e.printStackTrace();
        	}
        }
        Map<String, Prop> map = record.getProps();
        Prop p =map.get("activityId");
        if (p != null) {
        	String sqlRecordUpdate = "update KV_PROP set value= '" + activityId + "' where code = 'activityId' and record_id= '" + record.getCode() + "'";
        	keyValueConnector.updateBySql(sqlRecordUpdate);
        }
	}
	
	public Map<String, Object> prepareData(TaskInfo taskInfo) {
        String assignee = taskInfo.getAssignee();
        String initiator = internalProcessConnector.findInitiator(taskInfo
                .getProcessInstanceId());
        UserDTO assigneeUser = null;

        if (StringUtils.isNotBlank(assignee)) {
            assigneeUser = userConnector.findById(assignee);
        }

        UserDTO initiatorUser = userConnector.findById(initiator);

        Map<String, Object> data = new HashMap<String, Object>();

        Map<String, Object> taskEntity = new HashMap<String, Object>();
        taskEntity.put("id", taskInfo.getId());
        taskEntity.put("name", taskInfo.getName());

        if (assigneeUser != null) {
            taskEntity.put("assignee", assigneeUser.getDisplayName());
        }
        Record record = keyValueConnector.findByCode(taskInfo.getBusinessKey());
        String theme = taskInfo.getPresentationSubject();
        if (record != null) {
        	data.put("url", record.getUrl() == null ? "" : record.getUrl());
        	
        	if (!StringUtils.isBlank(record.getTheme())) {
        		theme = record.getTheme();
        	}
        }
        data.put("theme", theme);
        data.put("task", taskEntity);
        data.put("initiator", initiatorUser.getDisplayName());
        data.put("humanTask", taskInfo);
        data.put("baseUrl", baseUrl);
        data.put("humanTaskId", Long.toString(taskInfo.getId()));
        data.put("processInstanceId", taskInfo.getProcessInstanceId() == null ? "" : taskInfo.getProcessInstanceId());
        data.put("action", taskInfo.getAction() == null ? "" : taskInfo.getAction());
        data.put("catalog", taskInfo.getCatalog() == null ? "" : taskInfo.getCatalog());
        return data;
    }
	
	public String getParameter(Map<String, String[]> parameters, String name) {
		String[] value = parameters.get(name);

		if ((value == null) || (value.length == 0)) {
			return null;
		}

		return value[0];
	}

	public List<String> getParameterValues(Map<String, String[]> parameters, String name) {
		String[] value = parameters.get(name);

		if ((value == null) || (value.length == 0)) {
			return Collections.EMPTY_LIST;
		}

		return Arrays.asList(value);
	}

	public Map<String, Object> getVariables(Map<String, String[]> parameters) {
		Map<String, Object> variables = new HashMap<String, Object>();

		for (Map.Entry<String, String[]> entry : parameters.entrySet()) {
			String key = entry.getKey();
			String[] value = entry.getValue();

			if ((value == null) || (value.length == 0)) {
				variables.put(key, null);
			} else {
				variables.put(key, value[0]);
			}
		}

		return variables;
	}
	/**
	 * @param processInstanceId
	 * 抄送人通过详情或主题查看了抄送任务，则对应的消息已读
	 * @author sjx
	 */
	@Transactional(readOnly=false)
	public void copyMsgUpdate(String processInstanceId){
		String userId = currentUserHolder.getUserId();
		String hql = "from TaskInfo where processInstanceId = ? and assignee = ? and catalog = ? ";
		TaskInfo taskInfo = taskInfoManager.findUnique(hql, processInstanceId,userId,"copy");
		//抄送岗位
		List<Map<String,Object>> queryForList = jdbcTemplate.queryForList("select e.* from party_entity e join party_struct s on e.id=s.PARENT_ENTITY_ID where  e.DEL_FLAG = '0' and TYPE_ID = '5' and s.CHILD_ENTITY_ID= '"+userId+"';");
		if(null == taskInfo){
			if(queryForList.size() > 0){
				for (Map<String, Object> map : queryForList) {
					String postId = map.get("id").toString();
					taskInfo = taskInfoManager.findUnique(hql, processInstanceId,postId,"copy");
					if(null != taskInfo){
						continue;
					}
				}
			}
		}
		
		if( null != taskInfo){
			taskInfo.setStatus("complete");
			taskInfo.setCompleteTime(new Date());
			taskInfoManager.save(taskInfo);
			
			String data = String.valueOf(taskInfo.getId());
			String updateMsg = "update MsgInfo set status=1 where data=? and receiver_id='"+userId+"'";
			msgInfoManager.batchUpdate(updateMsg, data);
			
			//ckx 2019/2/11  抄送已读
			String taskInfoCopyHql = "from TaskInfoCopy where businessKey = ? and userId = ?";
			TaskInfoCopy findUnique = taskInfoCopyManager.findUnique(taskInfoCopyHql, taskInfo.getBusinessKey(),userId);
			if(null == findUnique){
				 TaskInfoCopy taskInfoCopy = new TaskInfoCopy();
			 	 taskInfoCopy.setBusinessKey(taskInfo.getBusinessKey());
			 	 taskInfoCopy.setUserId(userId);
			 	 taskInfoCopyManager.save(taskInfoCopy);
			}
		}
		
		/*String sql = "select * from task_info where process_instance_id='"+processInstanceId+"'";
		List<Map<String,Object>> taskInfos = jdbcTemplate.queryForList(sql);
		for(Map<String,Object> task : taskInfos){
			if(!task.get("catalog").equals("copy")){
				continue;
			}
			String data = task.get("id").toString();
			String updateMsg = "update MsgInfo set status=1 where data=? and receiver_id='"+userId+"'";
			msgInfoManager.batchUpdate(updateMsg, data);
		}*/
	}
	
	/**
	 * 设置认领的岗位（针对虚拟岗位的处理） 
	 * add by lilei at 2018.11.26
	 * **/
	@Transactional(readOnly=false)
	public void setClaimPosition(String taskId,String userId) throws Exception,
	IOException{
		try{
			if(com.mossle.core.util.StringUtils.isBlank(taskId)||com.mossle.core.util.StringUtils.isBlank(userId))
				return;
			String strSql=String.format("SELECT p.* FROM task_info_approve_position t "
					+ " INNER JOIN party_entity_attr p ON p.ID=t.position_id "
					+ " where t.task_id=%s", taskId);
			List<Map<String,Object>> mapPositionAttrList=jdbcTemplate.queryForList(strSql);
			if(mapPositionAttrList.size()>0){
				Map<String,Object> mapPositionAttr=mapPositionAttrList.get(0);
				String isRealPosition=mapPositionAttr.get("isRealPosition")==null?"0":mapPositionAttr.get("isRealPosition").toString();
				if(isRealPosition.equals("1")){
					String strPositionRealIds=mapPositionAttr.get("positionRealIds")==null?"":mapPositionAttr.get("positionRealIds").toString();
					if(!strPositionRealIds.equals("")){
						String strPositionSql=String.format("SELECT DISTINCT PARENT_ENTITY_ID FROM party_struct"
								+ " WHERE CHILD_ENTITY_ID in(%s)"
								+ " AND PARENT_ENTITY_ID in(%s)"
								+ " AND STRUCT_TYPE_ID=4", userId,strPositionRealIds);
		    			List<String> postPartyIdList=jdbcTemplate.queryForList(strPositionSql,String.class);
		    			if(postPartyIdList.size()>0){
		    				String postId=postPartyIdList.get(0);
			    			Map<String,String> mapPosition=partyOrgConnector.getParentPartyEntityId(postId.toString());
		    				String strStartInsertSql=String.format("update task_info_approve_position set "
		    						+ "position_id=%s,position_type='%s',position_parentId='%s',approve_position_name='%s' "
									+ " where task_id=%s",
									postId,
									'1',
									mapPosition.get("parent_id"),
									mapPosition.get("position_name"),
									taskId
									);
		    				keyValueConnector.updateBySql(strStartInsertSql);
		    			}
					}
					
				}
			}
		}
		catch(Exception ex){
			
		}
	}
	
	/**
   	 * 获得string值.
   	 */
   	public String getStringValue(Map<String, Object> map, String name) {
   		Object value = map.get(name);

   		if (value == null) {
   			return null;
   		}

   		if (value instanceof String) {
   			return (String) value;
   		}

   		return value.toString();
   	}
   	/**
     * @comment 页面环节显示审核时长使用 18.01.14
     * @param logHumanTaskDtos
     * @return
     * @author sjx
     */
   	@Transactional(readOnly=true)
    public List<HumanTaskDTO> settingAuditDuration(List<HumanTaskDTO> logHumanTaskDtos){
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	List<HumanTaskDTO> result = new ArrayList<HumanTaskDTO>();
    	for(HumanTaskDTO humanTaskDTO : logHumanTaskDtos){
        	Date createTime = humanTaskDTO.getCreateTime();
        	Date completeTime = humanTaskDTO.getCompleteTime();
        	String startTime = sdf.format(createTime);
        	String endTime = "";
        	if(completeTime != null){
        		endTime = sdf.format(completeTime);
        	}
        	if(com.mossle.common.utils.StringUtils.isBlank(endTime)){
        		humanTaskDTO.setAuditDuration("--");
        		result.add(humanTaskDTO);
        		continue;
        	}
        	long seconds = DateUtil.getTimeDifference(endTime, startTime);
        	String auditDuration = DateUtil.secondsToTime(seconds);
        	humanTaskDTO.setAuditDuration(auditDuration);
        	result.add(humanTaskDTO);
        }
    	return result;
    }
	@Resource
	public void setKeyValueConnector(KeyValueConnector keyValueConnector) {
		this.keyValueConnector = keyValueConnector;
	}

	@Resource
	public void setHumanTaskConnector(HumanTaskConnector humanTaskConnector) {
		this.humanTaskConnector = humanTaskConnector;
	}

	@Resource
	public void setProcessConnector(ProcessConnector processConnector) {
		this.processConnector = processConnector;
	}

	@Resource
	public void setPartyConnector(PartyConnector partyConnector) {
		this.partyConnector = partyConnector;
	}

	@Resource
	public void setTaskInfoManager(TaskInfoManager taskInfoManager) {
		this.taskInfoManager = taskInfoManager;
	}

	@Resource
	public void setInternalProcessConnector(
			InternalProcessConnector internalProcessConnector) {
		this.internalProcessConnector = internalProcessConnector;
	}

	@Resource
	public void setUserConnector(UserConnector userConnector) {
		this.userConnector = userConnector;
	}

	@Resource
	public void setNotificationConnector(NotificationConnector notificationConnector) {
		this.notificationConnector = notificationConnector;
	}
	
	@Value("${application.baseUrl}")
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
	
	@Resource
	public void setOnLineInfoManager(OnLineInfoManager onLineInfoManager) {
		this.onLineInfoManager = onLineInfoManager;
	}


	@Resource
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
	
	@Resource
	public void setRecordManager(RecordManager recordManager) {
		this.recordManager = recordManager;
	}
	
	
	@Resource
	public void setOrgConnector(OrgConnector orgConnector) {
		this.orgConnector = orgConnector;
	}
	
	@Resource
	public void setCurrentUserHolder(CurrentUserHolder currentUserHolder) {
		this.currentUserHolder = currentUserHolder;
	}
	
	@Resource
    public void setTaskDefinitionConnector(
            TaskDefinitionConnector taskDefinitionConnector) {
        this.taskDefinitionConnector = taskDefinitionConnector;
    }
}
