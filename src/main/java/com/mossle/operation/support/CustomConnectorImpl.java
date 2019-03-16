package com.mossle.operation.support;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import com.mossle.api.custom.CustomConnector;
import com.mossle.api.keyvalue.KeyValueConnector;
import com.mossle.api.keyvalue.Record;
import com.mossle.api.notification.NotificationConnector;
import com.mossle.api.notification.NotificationDTO;
import com.mossle.api.user.UserConnector;
import com.mossle.api.user.UserDTO;
import com.mossle.humantask.persistence.domain.TaskInfo;
import com.mossle.humantask.persistence.manager.TaskInfoManager;
import com.mossle.msg.MsgConstants;
import com.mossle.operation.persistence.domain.CustomApprover;
import com.mossle.operation.persistence.manager.CustomApproverManager;
import com.mossle.project.persistence.domain.WorkProjectNotify;
import com.mossle.util.StringUtil;


public class CustomConnectorImpl implements CustomConnector {
	private CustomApproverManager customApproverManager;
	private UserConnector userConnector;
	private TaskInfoManager taskInfoManager;
	private JdbcTemplate jdbcTemplate;
	private NotificationConnector notificationConnector;
	private KeyValueConnector keyValueConnector;
	private String baseUrl;
	
    public Map<String,Object> findAuditorHtml(String businessKey,String strUserId){
    	Map<String,Object> returnMap=new HashMap<String, Object>();
    	CustomApprover currentApprover=null;
    	
        int currentStep=1;
        //add by lilei at 2017.11.27
        List<CustomApprover> approvers=customApproverManager.find(
        		"from CustomApprover where businessKey=? order by approveStep ,opterType",
        		businessKey
        		);
        //获取下一步审批人
        //ckx 查询当前用户
        List<CustomApprover> approverListNow=customApproverManager.find(
				"from CustomApprover where businessKey=? and approverId=? and opterType !='2' and opterType !='3'", 
				businessKey,Long.parseLong(strUserId));
        if(null != approverListNow && approverListNow.size() > 0){
        	CustomApprover customApproverNow = approverListNow.get(0);
        	currentStep = customApproverNow.getApproveStep()+1;
        	//获取所有下一步审批步骤
			List<CustomApprover> nextApproverList=customApproverManager.find(
					"from CustomApprover where businessKey=? and approveStep>? and opterType !='2' and opterType !='3' order by approveStep", 
					businessKey,
					customApproverNow.getApproveStep());
			if(null != nextApproverList && nextApproverList.size() > 0){
				CustomApprover customApprover2 = nextApproverList.get(0);
				currentStep = customApprover2.getApproveStep();
			}
        }
                   
        int maxStep=1;
        StringBuffer strSelectIds=new StringBuffer();
        //getNextApprover(strUserId, approvers,currentStep);
        for(CustomApprover approver:approvers){
        	/*if((!approver.getOpterType().equals("3"))&&(!approver.getOpterType().equals("2"))&&approver.getApproverId()==Long.parseLong(strUserId)){
        		currentStep=approver.getApproveStep()+1;
        		//判断下一步审核人是否已被删除   ckx  add 2018/9/9
        		List<Map<String, Object>> queryForMapList = null;
				try {
					queryForMapList = jdbcTemplate.queryForList("select * from custom_approver where business_Key = '"+businessKey+"' and approveStep = '"+currentStep+"';");
				} catch (DataAccessException e) {
				}
				if(null != queryForMapList){
					for (Map<String, Object> map : queryForMapList) {
						String opetrType = StringUtil.toString(map.get("opterType"));
						if("3".equals(opetrType)){
							currentStep=approver.getApproveStep()+2;
						}
					}
					
					try {
						queryForMap = jdbcTemplate.queryForMap("select * from custom_approver where business_Key = '"+businessKey+"' and approveStep = '"+currentStep+"';");
					} catch (DataAccessException e) {
					}
					opetrType = StringUtil.toString(queryForMap.get("opterType"));
					if("2".equals(opetrType) || "3".equals(opetrType)){
						currentStep=approver.getApproveStep()+3;
					}
					
				}
        	}*/
        	
        	if(approver.getApproveStep()>maxStep)
        		maxStep=approver.getApproveStep();
        }
        
        for(CustomApprover approver:approvers){
        	if((currentStep==approver.getApproveStep()&&!approver.getOpterType().equals("2"))&&!approver.getOpterType().equals("3")){
        		currentApprover=approver;
        	}
        }
        
        String strHtmlString="<li style=\"width:140px;float:left;\">{auditor}</li>";
        StringBuffer sbHtmlBuffer=new StringBuffer();
        for(int i=1;i<=maxStep;i++){
        	List<CustomApprover> parentList=new ArrayList<CustomApprover>();
        	for(CustomApprover child:approvers){
        		if(i==currentStep&&child.getApproveStep()==currentStep&&!child.getOpterType().equals("2")&&!child.getOpterType().equals("3"))
        			currentApprover=child;
        		
        		if(child.getApproveStep()==i)
        			parentList.add(child);
        	}
        	if(parentList.size()>0)
        	{
        		String strAuditor=i+".";
        		if(parentList.size()>1)
        		{
        			for(CustomApprover approverChild:parentList){
        				//add 2018/9/9 ckx
        				if(!approverChild.getOpterType().equals("2") && !approverChild.getOpterType().equals("3")){
        					if(currentApprover!=null){
        						//如此判断，Long类型的地址一样，呵呵
                				//if(!approverChild.getApproverId().equals(currentApprover.getApproverId()));
                				if(approverChild.getApproverId().longValue()!=currentApprover.getApproverId().longValue())
                					strSelectIds.append(approverChild.getApproverId().toString()+",");
                			}
                			else 
                				strSelectIds.append(approverChild.getApproverId().toString()+",");//如果没有找到审批人（既下一步审批人）,直接添加
        					
        					/*if(currentApprover!=null&&(!approverChild.getApproverId().equals(currentApprover.getApproverId())))
        						strSelectIds.append(approverChild.getApproverId().toString()+",");*/
        					
        					UserDTO userDto = userConnector.findByIdAll(approverChild.getApproverId().toString());
            				if (userDto != null) {
            					strAuditor += userDto.getDisplayName();
            				} else {
            					strAuditor += approverChild.getApproverId().toString();
            				}
        				}
            				
        			}
        			
        			for(CustomApprover approverChild:parentList){
        				if(approverChild.getOpterType().equals("3")) {
        					UserDTO userDto = userConnector.findByIdAll(approverChild.getApproverId().toString());
            				if (userDto != null) {
            					strAuditor += "[" + userDto.getDisplayName()+"]";
            				} else {
            					strAuditor += "[" + approverChild.getApproverId().toString() +"]";
            				}
        				}
        				if(approverChild.getOpterType().equals("2")) {
        					UserDTO userDto = userConnector.findByIdAll(approverChild.getApproverId().toString());
            				if (userDto != null) {
            					strAuditor += "[" + userDto.getDisplayName()+"]";
            				} else {
            					strAuditor += "[" + approverChild.getApproverId().toString() +"]";
            				}
        				}
        				
        			}
        			
        			/*if(parentList.get(1).getOpterType().equals("2"))
        				strAuditor+=userConnector.findById(parentList.get(1).getApproverId().toString()).getDisplayName();
        			
        			if(!parentList.get(0).getOpterType().equals("2"))
        				strAuditor+="["+userConnector.findById(parentList.get(0).getApproverId().toString()).getDisplayName()+"]";*/
        		}
        		else
        		{
        			CustomApprover approverChild=parentList.get(0);
        			if(!approverChild.getOpterType().equals("2") && !approverChild.getOpterType().equals("3")){
	        			if(currentApprover!=null){
	        				//如此判断，Long类型的地址一样，呵呵
	        				//if(!approverChild.getApproverId().equals(currentApprover.getApproverId()));
	        				if(approverChild.getApproverId().longValue()!=currentApprover.getApproverId().longValue())
	        					strSelectIds.append(approverChild.getApproverId().toString()+",");
	        			}
	        			else 
	        				strSelectIds.append(approverChild.getApproverId().toString()+",");//如果没有找到审批人（既下一步审批人）,直接添加
        			}
        			/*if(currentApprover!=null&&(!approverChild.getApproverId().equals(currentApprover.getApproverId())))
						strSelectIds.append(approverChild.getApproverId().toString()+",");*/
        			
        			if(approverChild.getOpterType().equals("3")){
        				UserDTO userDto = userConnector.findByIdAll(approverChild.getApproverId().toString());
        				if (userDto != null) {
        					strAuditor += "[" + userDto.getDisplayName()+"]";
        				} else {
        					strAuditor += "[" + approverChild.getApproverId().toString() +"]";
        				}
        				
        			}
        			if(approverChild.getOpterType().equals("2")){
        				UserDTO userDto = userConnector.findByIdAll(approverChild.getApproverId().toString());
        				if (userDto != null) {
        					strAuditor += "[" + userDto.getDisplayName()+"]";
        				} else {
        					strAuditor += "[" + approverChild.getApproverId().toString() +"]";
        				}
        				
        			}
        			
        			if(!approverChild.getOpterType().equals("3") && !approverChild.getOpterType().equals("2")) {
        				UserDTO userDto = userConnector.findByIdAll(approverChild.getApproverId().toString());
        				if (userDto != null) {
        					strAuditor += userDto.getDisplayName();
        				} else {
        					strAuditor += approverChild.getApproverId().toString();
        				}
					}
        		}
        			
        		sbHtmlBuffer.append(strHtmlString.replace("{auditor}", strAuditor));
        	}
        }
        
        String auditorId="";
        String auditorName="";
        if(currentApprover!=null){
        	auditorId=currentApprover.getApproverId().toString();
        	
        	UserDTO userDto = userConnector.findByIdAll(currentApprover.getApproverId().toString());
        	if (userDto != null) {
        		auditorName = userDto.getDisplayName();
        	} else {
        		auditorName = currentApprover.getApproverId().toString();
        	}
        }
        
        returnMap.put("approver", sbHtmlBuffer.toString());
        returnMap.put("auditorId", auditorId);
        returnMap.put("auditorName",auditorName);
        returnMap.put("auditorSelectIds",strSelectIds.toString());
        
        //ckx 显示所有的审核人，用于撤回后变更审核人
        String allAuditorId = "";
        List<CustomApprover> AllapproverList=customApproverManager.find("from CustomApprover where businessKey=? and opterType !='2' and opterType !='3' order by approveStep" ,businessKey);
        for (CustomApprover customApprover : AllapproverList) {
        	allAuditorId += customApprover.getApproverId()+",";
		}
        if(com.mossle.common.utils.StringUtils.isNotBlank(allAuditorId)){
        	allAuditorId = allAuditorId.substring(0, allAuditorId.length()-1);
        }
        String allAuditorName = userConnector.findNamesByIds(allAuditorId);
        returnMap.put("allAuditorId", allAuditorId);
        returnMap.put("allAuditorName",allAuditorName);
        return returnMap;
    }

    //获取下一步审核人
	private int getNextApprover(String strUserId,List<CustomApprover> approvers,int currentStep) {
		for (CustomApprover customApprover : approvers) {
        	if((!customApprover.getOpterType().equals("3"))&&(!customApprover.getOpterType().equals("2"))&&customApprover.getApproverId()==Long.parseLong(strUserId)){
        		currentStep=customApprover.getApproveStep()+1;
        		//判断下一步审核人是否已被删除   ckx  add 2018/9/9
        		List<Map<String, Object>> queryForMapList = null;
				try {
					queryForMapList = jdbcTemplate.queryForList("select * from custom_approver where business_Key = '"+customApprover.getBusinessKey()+"' and approveStep = '"+currentStep+"';");
				} catch (DataAccessException e) {
				}
				if(null != queryForMapList){
					for (Map<String, Object> map : queryForMapList) {
						String opetrType = StringUtil.toString(map.get("opterType"));
						if("3".equals(opetrType)){
							currentStep=customApprover.getApproveStep()+2;
						}
					}
				}
        		
        	}
		}
		return currentStep;
	}
    
    public  boolean checkCanSelectAuditor(String businessKey,String strUserId) throws SQLException
    {
    	boolean isCanSelect=true;
    	String strSql=String.format("select audit_status from kv_record where BUSINESS_KEY=%s"
    			, businessKey);
    	
    	List<Map<String, Object>> kvMapList=jdbcTemplate.queryForList(strSql);
    	if(kvMapList!=null&&kvMapList.size()>0){
    		Map<String, Object> kvMap=kvMapList.get(0);
    		if(kvMap.get("audit_status").toString().equals("7")||kvMap.get("audit_status").toString().equals("8")){
    			isCanSelect=false;
    		}
    		else if(kvMap.get("audit_status").toString().equals("4")||kvMap.get("audit_status").toString().equals("1")){
    			//如果下一步审核人不存在，则可选
    			List<CustomApprover> approverList=customApproverManager.find(
    					"from CustomApprover where businessKey=? and approverId=?", 
    					businessKey,
    					Long.parseLong(strUserId));
    			if(approverList!=null&&approverList.size()>0){
    				List<CustomApprover> approverNextList=customApproverManager.find(
        					"from CustomApprover where opterType='0' and businessKey=? and approveStep=?", 
        					businessKey,
        					approverList.get(0).getApproveStep()+1);
    				if(approverNextList!=null&&approverNextList.size()>0){
    					List<TaskInfo> taskInfoList=taskInfoManager.find(
            	    			"From TaskInfo where action='同意' and status='complete' and businessKey=? and assignee=? ", 
            	    			businessKey,
            	    			approverNextList.get(0).getApproverId().toString());
            			isCanSelect=!(taskInfoList!=null&&taskInfoList.size()>0);
    				// ckx 2018/9/12	 删除下一步审核人后无法获取状态
    				}else{
    					List<CustomApprover> approverNextListAll=customApproverManager.find(
            					"from CustomApprover where businessKey=? and approveStep=?", 
            					businessKey,
            					approverList.get(0).getApproveStep()+1);
    					boolean boo = false;
    					for (CustomApprover customApprover : approverNextListAll) {
    						String opterType = customApprover.getOpterType();
    						if("0".equals(opterType)){
    							boo = true;
    							break;
    						}
						}
    					
    					if(!boo){
    						List<CustomApprover> approverNext2List=customApproverManager.find(
    	        					"from CustomApprover where opterType='0' and businessKey=? and approveStep=?", 
    	        					businessKey,
    	        					approverList.get(0).getApproveStep()+2);
    						if(approverNext2List!=null&&approverNext2List.size()>0){
    							List<TaskInfo> taskInfoList=taskInfoManager.find(
                    	    			"From TaskInfo where action='同意' and status='complete' and businessKey=? and assignee=? ", 
                    	    			businessKey,
                    	    			approverNext2List.get(0).getApproverId().toString());
                    			isCanSelect=!(taskInfoList!=null&&taskInfoList.size()>0);
    	    				}
    					}
    				}
    				
    			}
    		}
    	}
    	return isCanSelect;
    }
    
    /**
     * 判断是否已存在此审核人
     * **/
    public boolean checkIsExistsAuditor(String businessKey,String strUserId,String strNextAuditorId){
    	/*List<CustomApprover> approvers=customApproverManager.find(
        		"from CustomApprover where opterType<>'2' and businessKey=? order by approveStep",
        		businessKey
        		);
    	if(approvers==null||approvers.size()<1) 
    		return false;*/
    	
    	if(strNextAuditorId=="")
    		return false;
    	
    	List<CustomApprover> currentApprovers=customApproverManager.find(
        		"from CustomApprover where opterType<>'2' and opterType<>'3' and businessKey=? and approverId=? order by approveStep",
        		businessKey,
        		Long.parseLong(strUserId)
        		);
    	if(currentApprovers==null||currentApprovers.size()<1) 
    		return false;
    	else {
    		CustomApprover currentApprover=currentApprovers.get(0);
    		int approveStepNext = currentApprover.getApproveStep()+1;
    		//ckx  获取下一步审核人
    		List<CustomApprover> nextApproverss=customApproverManager.find(
            		"from CustomApprover where opterType<>'2' and opterType<>'3' and businessKey=? and approveStep > ?  order by approveStep",
            		businessKey,
            		currentApprover.getApproveStep()
            		);
    		if(null != nextApproverss && nextApproverss.size()>0){
    			CustomApprover customApprover = nextApproverss.get(0);
    			approveStepNext = customApprover.getApproveStep();
    		}
    		
    		
    		//查询下一步审批人是否是选择的
    		List<CustomApprover> nextApprovers=customApproverManager.find(
            		"from CustomApprover where opterType<>'2' and opterType<>'3' and businessKey=? and approveStep=? and approverId=? order by approveStep",
            		businessKey,
            		approveStepNext,
            		Long.parseLong(strNextAuditorId)
            		);
    		if(nextApprovers==null||nextApprovers.size()<1){
    			List<CustomApprover> existApprovers=customApproverManager.find(
                		"from CustomApprover where opterType<>'2' and opterType<>'3' and businessKey=? and approverId=?",
                		businessKey,
                		Long.parseLong(strNextAuditorId)
                		);
    			return (existApprovers!=null&&existApprovers.size()>0);
    		}
		}
    	
    	return false;
    }
    
    public void doNotice(TaskInfo info,String userId,String tenantId,String eventName,Map<String, Object> map){
		if(eventName.equals("create")){
			//下一步审核
			String title = "["+map.get("theme")+"]，需要您审批";
			String content="<a href=\""
					+ baseUrl+"/operation/task-operation-viewTaskForm.do"
					+ "?humanTaskId="+info.getId()
					+ "&action="//+(info.getAction()==null?"":info.getAction())
					+ "&catalog="+(info.getCatalog()==null?"":info.getCatalog())
					+ "&processInstanceId="+(info.getProcessInstanceId()==null?"":info.getProcessInstanceId())
					+ "\">"
					+ "您有新的自定义申请【"+map.get("theme")+"】，请审核"
					+ "</a>";
	    	String receiver=info.getAssignee();
	    	notificationConnector.send(info.getId().toString(), tenantId,
	    			userId, receiver, title, content,0);
    	}
		else if(eventName.equals("copy")){
			Record record = keyValueConnector.findByCode(info.getBusinessKey());
    		//抄送
			String title = "自定义申请：["+record.getTheme()+"]，抄送给您";
			String content="<a href=\""
					+ baseUrl+"/operation/task-operation-viewTaskForm.do"
					+ "?humanTaskId="+info.getId()
					+ "&action="+"copy"//(info.getAction()==null?"":info.getAction())
					+ "&catalog="+(info.getCatalog()==null?"":info.getCatalog())
					+ "&processInstanceId="+(info.getProcessInstanceId()==null?"":info.getProcessInstanceId())
					+ "\">"
					+ userConnector.findById(userId).getDisplayName()
					+ "发起的自定义申请【"+record.getTheme()+"】抄送给您，请审阅"
					+ "</a>";
	    	String receiver=info.getAssignee();
	    	//ckx add 2018/8/28
	    	if(null != map){
	    		String strRece = StringUtil.toString(map.get("strUserId"));
	    		String businessTypeId = StringUtil.toString(map.get("businessTypeId"));
		    	if(null != strRece && !"".equals(strRece) && !"null".equals(strRece)){
		    		receiver = strRece;
		    	}
		    	if(!"9999".equals(businessTypeId)){
		    		title = "["+info.getPresentationSubject()+"]，抄送给您";
		    		content="<a href=\""
							+ baseUrl+"/operation/task-operation-viewTaskForm.do"
							+ "?humanTaskId="+info.getId()
							+ "&action="+"copy"//(info.getAction()==null?"":info.getAction())
							+ "&catalog="+(info.getCatalog()==null?"":info.getCatalog())
							+ "&processInstanceId="+(info.getProcessInstanceId()==null?"":info.getProcessInstanceId())
							+ "\">"
							+ "您有新任务需要审阅-----【"+info.getPresentationSubject()+"】"
							+ "</a>";
		    	}
	    	}
	    	
	    	/*String strId="";
	    	if(map.get("id")==null||map.get("id").toString().equals(""))
	    		strId=record.getPkId();
	    	else 
	    		strId=map.get("id").toString();*/
	    	//默认未通过
	    	int isSend = 99;
    		Date endTime = record.getEndTime();
    		String auditStatus = record.getAuditStatus();
    		if(null != endTime || "2".equals(auditStatus)){
    			// 审核以通过
    			isSend = 0;
    		}
	    	notificationConnector.send(info.getId().toString(), tenantId,
	    			userId, receiver, title, content,isSend);
    	}
    	else 
		{
    		String strSql="select o.*,r.audit_status,r.url from oa_bpm_customForm o"
					  +" INNER JOIN kv_record r  on o.id=r.pk_id"
					  +" where r.BUSINESS_KEY='"+info.getBusinessKey()+"'";
		  	List<Map<String, Object>> customMapList=jdbcTemplate.queryForList(strSql);
		  	
		  	if(customMapList==null||customMapList.size()<1) 
		  		return;
		  	Map<String, Object> customMap=customMapList.get(0);
		  	if(eventName.equals("audit")){
		  	//下一步审核
				String title = "["+customMap.get("subject")+"]，需要您审批";
				String content="<a href=\""
						+ baseUrl+"/operation/task-operation-viewTaskForm.do"
						+ "?humanTaskId="+info.getId()
						+ "&action="//+(info.getAction()==null?"":info.getAction())
						+ "&catalog="+(info.getCatalog()==null?"":info.getCatalog())
						+ "&processInstanceId="+(info.getProcessInstanceId()==null?"":info.getProcessInstanceId())
						+ "\">"
						+ "您有新的自定义申请【"+customMap.get("subject")+"】，请审核"
						+ "</a>";
		    	String receiver=info.getAssignee();
		    	notificationConnector.send(info.getId().toString(), tenantId,
		    			userId, receiver, title, content,0);
		  	}
		  	else if(eventName.equals("reject")){
		  		String strRejectSql="select i.* from task_info i INNER JOIN KV_RECORD r on i.BUSINESS_KEY=r.BUSINESS_KEY"
						  +" where i.CATALOG='start' and i.BUSINESS_KEY='"+info.getBusinessKey()+"' order by i.create_time limit 0,1";
			  	List<Map<String, Object>> rejectMapList=jdbcTemplate.queryForList(strRejectSql);
			  	if(rejectMapList==null||rejectMapList.size()<1) 
			  		return;
			  	Map<String, Object> rejectMap=rejectMapList.get(0);
	    		//驳回（包括驳回和驳回发起人）
	    		if(rejectMap.get("ASSIGNEE").toString().equals(info.getAssignee())){
	    			//驳回发起人
	    			String title = "自定义申请，["+customMap.get("subject")+"]，被驳回";
	    			String content="<a href=\""
	    					+ baseUrl+"/operation/task-operation-viewTaskForm.do"
	    					+ "?humanTaskId="+info.getId()
	    					+ "&action="//+(info.getAction()==null?"":info.getAction())
	    					+ "&catalog="+(info.getCatalog()==null?"":info.getCatalog())
	    					+ "&processInstanceId="+(info.getProcessInstanceId()==null?"":info.getProcessInstanceId())
	    					+ "\">"
	    					+ "您发起的【"+customMap.get("subject")+"】被驳回，请查看"
	    					+ "</a>";
	    	    	String receiver=info.getAssignee();
	    	    	notificationConnector.send(info.getId().toString(), tenantId,
	    	    			userId, receiver, title, content,0);
	    		}
	    		else {
					//普通驳回
	    			String title = "自定义申请，["+customMap.get("subject")+"]，被驳回";
	    			String content="<a href=\""
	    					+ baseUrl+"/operation/task-operation-viewTaskForm.do"
	    					+ "?humanTaskId="+info.getId()
	    					+ "&action="//+(info.getAction()==null?"":info.getAction())
	    					+ "&catalog="+(info.getCatalog()==null?"":info.getCatalog())
	    					+ "&processInstanceId="+(info.getProcessInstanceId()==null?"":info.getProcessInstanceId())
	    					+ "\">"
	    					+ "审核人【"
	    					+ userConnector.findById(userId).getDisplayName()
	    					+ "】将【"+customMap.get("subject")+"】驳回给您，请处理。"
	    					+ "</a>";
	    	    	String receiver=info.getAssignee();
	    	    	notificationConnector.send(info.getId().toString(), tenantId,
	    	    			userId, receiver, title, content,0);
				}
	    	}
	    	else if(eventName.equals("complete")){
	    		//完成
	    		String strCompleteSql="select i.* from task_info i"
						  +" where i.catalog='start' AND i.BUSINESS_KEY='"+info.getBusinessKey()+"' order by i.create_time limit 0,1";
	    		List<Map<String, Object>> completeMapList=jdbcTemplate.queryForList(strCompleteSql);
	    		
	    		if(completeMapList==null||completeMapList.size()<1) return;
	    		
	    		Map<String, Object> completeMap=completeMapList.get(0);
	    		
				String title = "自定义申请，["+customMap.get("subject")+"]，已结束";
				String content="<a href=\""
						+ baseUrl+"/bpm/workspace-viewHistoryFrom.do"
						+ "?processInstanceId="+(info.getProcessInstanceId()==null?"":info.getProcessInstanceId())
						+ "&url=${url}"
						+ "&viewBack=true"
						+ "\">"
						+ "您发起的【"+customMap.get("subject")+"】已结束，请查看"
						+ "</a>";
				content=content.replace("${url}",customMap.get("url")==null?"":customMap.get("url").toString());
		    	String receiver=completeMap.get("assignee").toString();
		    	notificationConnector.send(info.getId().toString(), tenantId,
		    			userId, receiver, title, content,0);
	    	}
		}
    }
    
    /**
	 * 自定义申请，不同意或则终止，则终止此后所有审批步骤
	 * **/
    public String upadteDelApprover(TaskInfo taskInfo,String currentName){
		String strComment="";
		List<CustomApprover> approverList=customApproverManager.find(
				"from CustomApprover where businessKey=? and approverId=? and opterType='0'", 
				taskInfo.getBusinessKey(),
				Long.valueOf(taskInfo.getAssignee()));
		if(approverList!=null&&approverList.size()>0){
			CustomApprover approver=approverList.get(0);
			//更新当前审核人
			String sql = String.format("update custom_approver set opterType='1' where opterType='0' and business_Key=%s and approveStep=%d",
					taskInfo.getBusinessKey(),
					approver.getApproveStep());
			keyValueConnector.updateBySql(sql);
			
			List<CustomApprover> approverNextList=customApproverManager.find(
					"from CustomApprover where opterType='0' and businessKey=? and approveStep>?",
					taskInfo.getBusinessKey(),
					approver.getApproveStep());
			if(approverNextList!=null&&approverNextList.size()>0){
				CustomApprover customApprover = approverNextList.get(0);
				//系统：审核人【C】将流程结束，后续审核人无需操作
				/*strComment="系统：审核人【"
						+ currentName
						+ "】将流程结束，后续审核人无需操作<br/>";*/
				strComment="系统：审核人【"
						+currentName
						+"】删除了下一步审核人【"
						+ userConnector.findById(String.valueOf(customApprover.getApproverId())).getDisplayName()
						+ "】<br/>";
			}
			
			sql = String.format("update custom_approver set opterType='3' where opterType='0' and business_Key=%s and approveStep>%d",
					taskInfo.getBusinessKey(),
					approver.getApproveStep());
			keyValueConnector.updateBySql(sql);
		}
		return strComment;
	}
    
    /**
	 * 自定义申请，不同意或则终止，则终止此后所有审批步骤
	 * **/
    public String upadteCancelApprover(TaskInfo taskInfo,String currentName){
		String strComment="";
		List<CustomApprover> approverList=customApproverManager.find(
				"from CustomApprover where businessKey=? and approverId=? and opterType='0'", 
				taskInfo.getBusinessKey(),
				Long.valueOf(taskInfo.getAssignee()));
		if(approverList!=null&&approverList.size()>0){
			CustomApprover approver=approverList.get(0);
			//更新当前审核人
			String sql = String.format("update custom_approver set opterType='1' where opterType='0' and business_Key=%s and approveStep=%d",
					taskInfo.getBusinessKey(),
					approver.getApproveStep());
			keyValueConnector.updateBySql(sql);
			
			List<CustomApprover> approverNextList=customApproverManager.find(
					"from CustomApprover where opterType='0' and businessKey=? and approveStep>?",
					taskInfo.getBusinessKey(),
					approver.getApproveStep());
			if(approverNextList!=null&&approverNextList.size()>0){
				CustomApprover customApprover = approverNextList.get(0);
				//系统：审核人【C】将流程结束，后续审核人无需操作
				strComment="系统：审核人【"
						+ currentName
						+ "】将流程结束，后续审核人无需操作<br/>";
				
			}
			
			sql = String.format("update custom_approver set opterType='3' where opterType='0' and business_Key=%s and approveStep>%d",
					taskInfo.getBusinessKey(),
					approver.getApproveStep());
			keyValueConnector.updateBySql(sql);
		}
		return strComment;
	}
	
	/**
	 * 变更审批人后，比对下一个审批人.
	 * 如果有变更则，返回变更钱意见
	 * **/
	public String updateChangeAuditorNew(TaskInfo taskInfo,String leader,String currentName){
		String strComment="";
		//当前审批步骤
		List<CustomApprover> approverList=customApproverManager.find(
				"from CustomApprover where businessKey=? and approverId=?", 
				taskInfo.getBusinessKey(),
				Long.valueOf(taskInfo.getAssignee()));
		if(approverList!=null&&approverList.size()>0){
			CustomApprover approver=approverList.get(0);
			
			//下一步审批步骤
			List<CustomApprover> nextApproverList=customApproverManager.find(
					"from CustomApprover where businessKey=? and approveStep=? and opterType='0'", 
					taskInfo.getBusinessKey(),
					approver.getApproveStep()+1);
			if(nextApproverList!=null&&nextApproverList.size()>0){
				CustomApprover nextApprover=nextApproverList.get(0);
				
				if(nextApprover.getApproverId()!=Long.parseLong(leader)){
					String sql = String.format("update custom_approver set opterType='2' where opterType='0' and business_Key=%s and approverId=%d",
							taskInfo.getBusinessKey(),
							nextApprover.getApproverId());
					keyValueConnector.updateBySql(sql);
					
					//审核人【A】将下一步审核人由【B】变更为【M】
					strComment="系统：审核人【"
								+currentName+"】"
								+"将下一步审核人由【"
								+userConnector.findById(nextApprover.getApproverId().toString()).getDisplayName()
								+"】变更为【"
								+userConnector.findById(leader).getDisplayName()+"】<br/>";
					//taskInfo.setComment("");
					
					//变更，新增审批人
					CustomApprover newApprover=new CustomApprover();
					newApprover.setApproverId(Long.parseLong(leader));
					newApprover.setCustomId(nextApprover.getCustomId());
					newApprover.setBusinessKey(nextApprover.getBusinessKey());
					newApprover.setApproveStep(nextApprover.getApproveStep());
					newApprover.setOpterType("0");
					newApprover.setAuditComment("");
		        	customApproverManager.save(newApprover);
				}
			}
			else {
				//系统：审核人【D】添加了下一步审核人【E】
				strComment="系统：审核人【"
							+currentName
							+"】添加了下一步审核人【"
							+ userConnector.findById(leader).getDisplayName()
							+ "】<br/>";
				//如果查询不出，则没下一步审批人，新增审批人
				CustomApprover newApprover=new CustomApprover();
				newApprover.setApproverId(Long.valueOf(leader));
				newApprover.setCustomId(approver.getCustomId());
				newApprover.setBusinessKey(approver.getBusinessKey());
				newApprover.setApproveStep(approver.getApproveStep()+1);
				newApprover.setOpterType("0");
				newApprover.setAuditComment("");
	        	customApproverManager.save(newApprover);
			}
			
			//同意后，更改操作状态
			if(approver.getOpterType().equals("0")){
				approver.setOpterType("1");
				customApproverManager.save(approver);
			}
		}
		return strComment;
	}
    
	/**
	 * 变更审批人后，比对下一个审批人.
	 * 如果有变更则，返回变更钱意见
	 * 
	 * parameterType：1:增加 2：删除 3：替换
	 * opterType：0:未审核，1：已审核，2：被替换，3：已删除
	 * **/
	public String updateChangeAuditor(TaskInfo taskInfo,String leader,String currentName,String parameterType){
		String strComment="";
		//如果
		/*if(com.mossle.core.util.StringUtils.isBlank(leader)&&(parameterType.equals("1")||parameterType.equals("3")))
			parameterType="0";*/
		if("1".equals(parameterType)){//新增审核人
			taskInfo.setOwner(userConnector.findById(leader).getDisplayName());
			//当前审批步骤
			List<CustomApprover> approverList=customApproverManager.find(
					"from CustomApprover where businessKey=? and approverId=?", 
					taskInfo.getBusinessKey(),
					Long.valueOf(taskInfo.getAssignee()));
			if(approverList!=null&&approverList.size()>0){
				CustomApprover approver=approverList.get(0);
				//获取下一步审批步骤
				List<CustomApprover> list=customApproverManager.find(
						"from CustomApprover where businessKey=? and approveStep=? and opterType='3'", 
						taskInfo.getBusinessKey(),
						approver.getApproveStep()+1);
				
				//获取所有下一步审批步骤
				List<CustomApprover> nextApproverList=customApproverManager.find(
						"from CustomApprover where businessKey=? and approveStep>? and opterType != '2' and opterType != '3'", 
						taskInfo.getBusinessKey(),
						approver.getApproveStep());
				if(nextApproverList!=null&&nextApproverList.size()>0){
					CustomApprover customApprover2 = nextApproverList.get(0);
					//如果新增的审核人，为下一步审核人，则不处理
					if(!customApprover2.getApproverId().toString().equals(leader)){
						if(null == list || list.size() == 0){
							//其余所有审批步骤全部+1
							for (CustomApprover customApprover : nextApproverList) {
								int approveStep = customApprover.getApproveStep()+1;
								Long id = customApprover.getId();
								jdbcTemplate.execute("update custom_approver set approveStep='"+approveStep+"' where opterType != '2' and opterType != '3' and id = '"+id+"'");
							}
						}
						
						//新增审核人【A】
						strComment="系统：审核人【"
								+currentName
								+"】添加了下一步审核人【"
								+ userConnector.findById(leader).getDisplayName()
								+ "】<br/>";
						
						//变更，新增审批人
						CustomApprover newApprover=new CustomApprover();
						newApprover.setApproverId(Long.parseLong(leader));
						newApprover.setCustomId(approver.getCustomId());
						newApprover.setBusinessKey(approver.getBusinessKey());
						newApprover.setApproveStep(approver.getApproveStep()+1);
						newApprover.setOpterType("0");
						newApprover.setAuditComment("");
			        	customApproverManager.save(newApprover);
					}
					else {
						strComment="00";
					}
				}else{
					//无下一步审核人
					//系统：审核人【D】添加了下一步审核人【E】
					strComment="系统：审核人【"
								+currentName
								+"】添加了下一步审核人【"
								+ userConnector.findById(leader).getDisplayName()
								+ "】<br/>";
					//如果查询不出，则没下一步审批人，新增审批人
					CustomApprover newApprover=new CustomApprover();
					newApprover.setApproverId(Long.valueOf(leader));
					newApprover.setCustomId(approver.getCustomId());
					newApprover.setBusinessKey(approver.getBusinessKey());
					newApprover.setApproveStep(approver.getApproveStep()+1);
					newApprover.setOpterType("0");
					newApprover.setAuditComment("");
		        	customApproverManager.save(newApprover);
				}
				//同意后，更改操作状态
				if(approver.getOpterType().equals("0")){
					approver.setOpterType("1");
					customApproverManager.save(approver);
				}
			}
		}else if("2".equals(parameterType)){//删除审核人
			//当前审批步骤
			List<CustomApprover> approverList=customApproverManager.find(
					"from CustomApprover where businessKey=? and approverId=?", 
					taskInfo.getBusinessKey(),
					Long.valueOf(taskInfo.getAssignee()));
			if(approverList!=null&&approverList.size()>0){
				CustomApprover approver=approverList.get(0);
				
				//获取所有下一步审批步骤
				List<CustomApprover> nextApproverList=customApproverManager.find(
						"from CustomApprover where businessKey=? and approveStep>? and opterType !='2' and opterType !='3' order by approveStep", 
						taskInfo.getBusinessKey(),
						approver.getApproveStep());
				
				//查询剩下共有多少步骤
				if(null != nextApproverList && nextApproverList.size() > 1){
					CustomApprover customApprover2 = nextApproverList.get(0);
					int approveStepDel = customApprover2.getApproveStep();
					int approveStep2 = nextApproverList.get(1).getApproveStep();
					
					//int approveStep2 = approver.getApproveStep()+1;
					jdbcTemplate.execute("update custom_approver set opterType = '3' where opterType !='2' and opterType !='3' and approveStep = '"+approveStepDel+"' and business_key = '"+taskInfo.getBusinessKey()+"'");

					//jdbcTemplate.execute("delete from custom_approver where opterType='0' and approveStep = '"+approveStep2+"' and business_key = '"+taskInfo.getBusinessKey()+"'");
					for (CustomApprover customApprover : nextApproverList) {
						//去除删除的步骤
						if(approveStepDel==customApprover.getApproveStep()){
							strComment="系统：审核人【"
									+currentName
									+"】删除了下一步审核人【"
									+ userConnector.findById(String.valueOf(customApprover.getApproverId())).getDisplayName()
									+ "】<br/>";
						}
						//保存下一步审核人
						if(approveStep2==customApprover.getApproveStep()){
							Long approverId = customApprover.getApproverId();
							String displayName = userConnector.findById(String.valueOf(approverId)).getDisplayName();
							taskInfo.setTaskId(String.valueOf(approverId));
							taskInfo.setOwner(displayName);
						}
					}
					//同意后，更改操作状态
					if(approver.getOpterType().equals("0")){
						approver.setOpterType("1");
						customApproverManager.save(approver);
					}
				}//余下步骤只剩自己或没有，流程结束	
			}	
		}else if("3".equals(parameterType)){//替换审核人
			//当前审批步骤
			List<CustomApprover> approverList=customApproverManager.find(
					"from CustomApprover where businessKey=? and approverId=?", 
					taskInfo.getBusinessKey(),
					Long.valueOf(taskInfo.getAssignee()));
			if(approverList!=null&&approverList.size()>0){
				CustomApprover approver=approverList.get(0);
				//下一步审批步骤
				List<CustomApprover> nextApproverList=customApproverManager.find(
						"from CustomApprover where businessKey=? and approveStep>? and opterType !='2' and opterType !='3' order by approveStep", 
						taskInfo.getBusinessKey(),
						approver.getApproveStep());
				if(nextApproverList!=null&&nextApproverList.size()>0){
					CustomApprover nextApprover=nextApproverList.get(0);
					if(!nextApprover.getApproverId().toString().equals(leader)){
						String sql = String.format("update custom_approver set opterType='2' where opterType !='2' and opterType !='3' and business_Key=%s and approverId=%d",
								taskInfo.getBusinessKey(),
								nextApprover.getApproverId());
						keyValueConnector.updateBySql(sql);
						
						//审核人【A】将下一步审核人由【B】变更为【M】
						strComment="系统：审核人【"
									+currentName+"】"
									+"将下一步审核人由【"
									+userConnector.findById(nextApprover.getApproverId().toString()).getDisplayName()
									+"】变更为【"
									+userConnector.findById(leader).getDisplayName()+"】<br/>";
						//taskInfo.setComment("");
						
						//变更，新增审批人
						CustomApprover newApprover=new CustomApprover();
						newApprover.setApproverId(Long.parseLong(leader));
						newApprover.setCustomId(nextApprover.getCustomId());
						newApprover.setBusinessKey(nextApprover.getBusinessKey());
						newApprover.setApproveStep(nextApprover.getApproveStep());
						newApprover.setOpterType("0");
						newApprover.setAuditComment("");
			        	customApproverManager.save(newApprover);
					}
					else {
						strComment="00";
					}
				}else {
					//系统：审核人【D】添加了下一步审核人【E】
					strComment="系统：审核人【"
								+currentName
								+"】添加了下一步审核人【"
								+ userConnector.findById(leader).getDisplayName()
								+ "】<br/>";
					//如果查询不出，则没下一步审批人，新增审批人
					CustomApprover newApprover=new CustomApprover();
					newApprover.setApproverId(Long.valueOf(leader));
					newApprover.setCustomId(approver.getCustomId());
					newApprover.setBusinessKey(approver.getBusinessKey());
					newApprover.setApproveStep(approver.getApproveStep()+1);
					newApprover.setOpterType("0");
					newApprover.setAuditComment("");
		        	customApproverManager.save(newApprover);
				}
				//同意后，更改操作状态
				if(approver.getOpterType().equals("0")){
					approver.setOpterType("1");
					customApproverManager.save(approver);
				}
			}
		}else if("0".equals(parameterType)){
			//当前审批步骤
			List<CustomApprover> approverList=customApproverManager.find(
					"from CustomApprover where businessKey=? and approverId=?", 
					taskInfo.getBusinessKey(),
					Long.valueOf(taskInfo.getAssignee()));
			if(approverList!=null&&approverList.size()>0){
				CustomApprover approver=approverList.get(0);
				//同意后，更改操作状态
				if(approver.getOpterType().equals("0")){
					approver.setOpterType("1");
					customApproverManager.save(approver);
				}
			}
			strComment = "0";
		}
		return strComment;
	}
	
	/**
	 * 驳回后，更改上一个审批人状态
	 * **/
	public void upadteRejectApprover(TaskInfo taskInfo){
		List<CustomApprover> approverList=customApproverManager.find(
				"from CustomApprover where businessKey=? and approverId=? and opterType != '2' and opterType != '3'", 
				taskInfo.getBusinessKey(),
				Long.valueOf(taskInfo.getAssignee()));
		if(approverList!=null&&approverList.size()>0){
			CustomApprover approver=approverList.get(0);
			//ckx   删除审核人后驳回  ，应修改上上步审核人状态  2018/9/16
			int approveStep = approver.getApproveStep()-1;
			//处理上一部审核人是否删除或替换
			approveStep = getApproveStep(taskInfo, approveStep);
			
			String sql = String.format("update custom_approver set opterType='0' where opterType='1' and business_Key=%s and approveStep=%d",
					taskInfo.getBusinessKey(),
					approveStep);
			keyValueConnector.updateBySql(sql);
			
			
		}
	}
	//获取上一部审核人
	private int getApproveStep(TaskInfo taskInfo, int approveStep) {
		
		List<Map<String,Object>> queryForList = jdbcTemplate.queryForList("select opterType from custom_approver where business_Key = '"+taskInfo.getBusinessKey()+"' and approveStep = '"+approveStep+"'");
		if(null == queryForList || queryForList.size() == 0){
			return approveStep;
		}
		boolean boo = false;
		for (Map<String, Object> map : queryForList) {
			String opterType = map.get("opterType").toString();
			if("1".equals(opterType)){
				boo = true;
			}
		}
		if(!boo){
			approveStep = getApproveStep(taskInfo,approveStep-1);
		}
		return approveStep;
	}
    
    @Resource
    public void setCustomApproverManager(CustomApproverManager customApproverManager){
    	this.customApproverManager=customApproverManager;
    }
    
    @Resource
    public void setUserConnector(UserConnector userConnector){
    	this.userConnector=userConnector;
    }
    
    @Resource
    public void setTaskInfoManager(TaskInfoManager taskInfoManager){
    	this.taskInfoManager=taskInfoManager;
    }
    
    @Resource
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate){
    	this.jdbcTemplate=jdbcTemplate;
    }
    
    @Resource
    public void setNotificationConnector(NotificationConnector notificationConnector){
    	this.notificationConnector=notificationConnector;
    }
    
    @Resource
    public void setKeyValueConnector(KeyValueConnector keyValueConnector){
    	this.keyValueConnector=keyValueConnector;
    }
    
    @Value("${application.baseUrl}")
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
}