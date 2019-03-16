package com.mossle.api.custom;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.mossle.humantask.persistence.domain.TaskInfo;

public interface CustomConnector {
	/**
	 * 拼接审批步骤的html，并且返回审核人ID和name
	 * @author lilei add by liei at 2017.11.28
	 * **/
    Map<String,Object> findAuditorHtml(String businessKey,String strUserId);
    
    /**
	 * 验证审核后驳回，不能重新选人
	 * @author lilei add by liei at 2017.11.28
	 * **/
    boolean checkCanSelectAuditor(String businessKey,String strUserId) throws SQLException;
    
    boolean checkIsExistsAuditor(String businessKey,String strUserId,String strNextAuditorId);
    
    /**
     * 自定义流程发起消息
	 * @author lilei add by liei at 2017.11.29
     * **/
    void doNotice(TaskInfo info,String userId,String tenantId,String eventName,Map<String, Object> map);
    
    /**
	 * 自定义申请，不同意或则终止，则终止此后所有审批步骤
	 * **/
    String upadteCancelApprover(TaskInfo taskInfo,String currentName);
    
    /**
	 * 自定义申请，删除
	 * **/
    String upadteDelApprover(TaskInfo taskInfo,String currentName);
    
    /**
	 * 变更审批人后，比对下一个审批人.
	 * 如果有变更则，返回变更钱意见
	 * @param leader 下一步审批人
	 * @param currentName 当前登陆人姓名
	 * **/
    String updateChangeAuditor(TaskInfo taskInfo,String leader,String currentName,String parameterType);
    //String updateChangeAuditor(TaskInfo taskInfo,HttpServletRequest request,String currentName);
    /**
	 * 驳回后，更改上一个审批人状态
	 * **/
	void upadteRejectApprover(TaskInfo taskInfo);
    
    
}