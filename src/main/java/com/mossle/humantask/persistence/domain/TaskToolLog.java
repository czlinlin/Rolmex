package com.mossle.humantask.persistence.domain;

import java.util.Date;

/**
 * TaskLog 任务日志.
 * 
 * @author ckx
 */
public class TaskToolLog {

	private String id;
	
	private String applyCode;
	
	/** 主题 */
	private String theme;
	
	/** 之前负责人 */
	private String startAssignee;
	
	/** 修改后负责人 */
	private String endAssignee;
	
	/** 岗位及人员ID */
    private String positionId;
    
    /** 类型  1：岗位  2：人员 */
    private String positionType;
    
    /** 新旧数据 1:新，2：旧 **/
    private String dataType;
    
    /** 审批节点的人员/岗位信息 **/
    private String approvePositionName;
    
    /** 业务编号 */
    private String businessKey;
	
	/** 操作人. */
    private String creator;
    
    /** 创建时间 */
    private Date createTime;



	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	
	
	public String getApplyCode() {
		return applyCode;
	}

	public void setApplyCode(String applyCode) {
		this.applyCode = applyCode;
	}

	public String getTheme() {
		return theme;
	}

	public void setTheme(String theme) {
		this.theme = theme;
	}

	public String getStartAssignee() {
		return startAssignee;
	}

	public void setStartAssignee(String startAssignee) {
		this.startAssignee = startAssignee;
	}

	public String getEndAssignee() {
		return endAssignee;
	}

	public void setEndAssignee(String endAssignee) {
		this.endAssignee = endAssignee;
	}

	public String getPositionId() {
		return positionId;
	}

	public void setPositionId(String positionId) {
		this.positionId = positionId;
	}

	public String getPositionType() {
		return positionType;
	}

	public void setPositionType(String positionType) {
		this.positionType = positionType;
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public String getApprovePositionName() {
		return approvePositionName;
	}

	public void setApprovePositionName(String approvePositionName) {
		this.approvePositionName = approvePositionName;
	}

	public String getBusinessKey() {
		return businessKey;
	}

	public void setBusinessKey(String businessKey) {
		this.businessKey = businessKey;
	}

	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	
    
    
	
}
