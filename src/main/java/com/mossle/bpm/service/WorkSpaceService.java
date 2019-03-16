package com.mossle.bpm.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.ProcessEngine;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mossle.api.keyvalue.KeyValueConnector;
import com.mossle.bpm.persistence.domain.BpmConfUser;
import com.mossle.bpm.persistence.manager.BpmConfUserManager;
import com.mossle.humantask.persistence.domain.TaskInfo;
import com.mossle.humantask.persistence.manager.TaskInfoManager;
import com.mossle.msg.persistence.manager.MsgInfoManager;

@Service
@Transactional(readOnly=true)
public class WorkSpaceService {
	@Resource
	private TaskInfoManager taskInfoManager;
	@Resource
	private MsgInfoManager msgInfoManager;
	@Resource
	private KeyValueConnector keyValueConnector;
	@Resource
	private ProcessEngine processEngine;
	@Resource
	private BpmConfUserManager bpmConfUserManager;
	@Resource
	private JdbcTemplate jdbcTemplate;
	
	
	/**
	 * @param processInstanceId
	 * @param humanTaskId
	 * 流程撤回后的撤销申请 sjx
	 */
	@Transactional(readOnly=false)
	public void cancel(String processInstanceId,Long humanTaskId){
		String sql = "select * from act_ru_execution e where e.PROC_INST_ID_="+processInstanceId;
		Map<String, Object> instanceObj = null;
		try {
			instanceObj = jdbcTemplate.queryForMap(sql);
		} catch (DataAccessException e) {
		}
		if(null != instanceObj){
			processEngine.getRuntimeService().deleteProcessInstance(processInstanceId, "人工终止");
		}
		cancelTaskInfo(processInstanceId, humanTaskId);
		
	}
	
	// 结束taskInfo
	private void cancelTaskInfo(String processInstanceId, Long humanTaskId) {
		TaskInfo task = taskInfoManager.findUniqueBy("id", humanTaskId);
		task.setCompleteTime(new Date());
		task.setStatus("complete");
		task.setAction("撤销申请");
		taskInfoManager.save(task);
		//撤销申请后对应的消息置为已读 2018.08.27 sjx
		String updateMsg = "update MsgInfo set status=1 where data=?";
		msgInfoManager.batchUpdate(updateMsg, humanTaskId.toString());
		//终止流程后将kv_record表的流程状态置为已取消 2018-01-11 shijingxin
		String sqlRecordUpdate = "update KV_RECORD set audit_status = '6' where REF= '" + processInstanceId + "'";
		keyValueConnector.updateBySql(sqlRecordUpdate);
	}
	/**
	 * 方法说明
	 * 当组织机构的岗位名称变更，要同步修改该岗位已经配置的流程节点名称
	 */
	@Transactional(readOnly=false)
	public void modifyPostUpdateProcessConfig(String postId,String newPostName){
		List<BpmConfUser> bpmConfUserList = bpmConfUserManager.findBy("value", "岗位:"+postId);
		for(BpmConfUser bpmConfUser : bpmConfUserList){
			bpmConfUser.setName(newPostName);
			bpmConfUserManager.save(bpmConfUser);
		}
	}
}
