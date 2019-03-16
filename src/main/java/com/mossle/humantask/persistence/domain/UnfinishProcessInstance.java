package com.mossle.humantask.persistence.domain;

import java.util.Date;

/**
 * @author chengze:
 * @version 创建时间：2017年9月27日 下午1:55:56 存放未结流程
 */
public class UnfinishProcessInstance {

	/** processInstanceId. */
	private String id;

	private String businessKey;

	/** 流程标题. */
	private String name;

	/** 流程定义Id. */
	private String processDefinitionId;

	/** 流程实例Id. */
	private String processInstanceId;

	/** 创建时间. */
	private Date startTime;

	
	/** 抄送时间. */
	private Date ccTime;
	
	
	/** 完成时间. */
	private Date endTime;

	/** 状态. */
	private String status;

	/** 受理单号 */
	private String applyCode;
	/** 主题 */
	private String theme;
	/** 经销商编号 */
	private String ucode;
	/** 申请业务类型ID */
	private String businessTypeId;
	/** 申请业务类型名称 */
	private String businessTypeName;
	/** 业务细分ID */
	private String businessDetailId;

	/** 业务细分名称 */
	private String businessDetailName;
	/** 提交次数 */
	private int submitTimes;

	/** 所属大区ID */
	private String areaId;
	/** 所属大区名称 */
	private String areaName;

	/** 所属公司ID */
	private String companyId;
	/** 所属公司名称 */
	private String companyName;

	/** 所属体系Id */
	private String systemId;

	/** 所属体系名称 */
	private String systemName;

	/** 流程动作 */
	private String action;

	/** 申请人 */
	private String applyUserName;

	/** 表单详情地址 */
	private String url;

	/** 流程状态标识 抄送：copy */
	private String catalog;

	/** 流程标识 普通流程、自定义流程 */
	private String proFlag;

	/** 申请内容 */
	private String applyContent;

	/**
	 * task_info.COMPLETE_TIME
	 */
	private Date completeTime;
	//流程的审核步骤
	private String whole;
	//审批人
	private String assignee;
	//当前审核环节
	private String approvePositionName;
	//人员或岗位id
	private String positionId;
	 /** 新旧数据 1:新，2：旧 **/
    private String dataType;
	//判断请假申请是否变色标识
	private boolean compare;
	//用作经我审批显示审核时长
	private String auditDuration;
	
	//是否已读
	private String isRead; //1:读取   0：未读
	
	
	public String getIsRead() {
		return isRead;
	}

	public void setIsRead(String isRead) {
		this.isRead = isRead;
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

	public String getPositionId() {
		return positionId;
	}

	public void setPositionId(String positionId) {
		this.positionId = positionId;
	}

	public String getAssignee() {
		return assignee;
	}

	public void setAssignee(String assignee) {
		this.assignee = assignee;
	}

	public UnfinishProcessInstance() {

	}

	public UnfinishProcessInstance(String businessKey, String name, String processDefinitionId, Date startTime,
			String status) {
		super();
		this.businessKey = businessKey;
		this.name = name;
		this.processDefinitionId = processDefinitionId;
		this.startTime = startTime;
		this.status = status;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getBusinessKey() {
		return businessKey;
	}

	public void setBusinessKey(String businessKey) {
		this.businessKey = businessKey;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getProcessDefinitionId() {
		return processDefinitionId;
	}

	public void setProcessDefinitionId(String processDefinitionId) {
		this.processDefinitionId = processDefinitionId;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	
	
	public Date getCcTime() {
		return ccTime;
	}

	public void setCcTime(Date ccTime) {
		this.ccTime = ccTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
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

	public String getUcode() {
		return ucode;
	}

	public void setUcode(String ucode) {
		this.ucode = ucode;
	}

	public String getBusinessTypeId() {
		return businessTypeId;
	}

	public void setBusinessTypeId(String businessTypeId) {
		this.businessTypeId = businessTypeId;
	}

	public String getBusinessTypeName() {
		return businessTypeName;
	}

	public void setBusinessTypeName(String businessTypeName) {
		this.businessTypeName = businessTypeName;
	}

	public String getBusinessDetailId() {
		return businessDetailId;
	}

	public void setBusinessDetailId(String businessDetailId) {
		this.businessDetailId = businessDetailId;
	}

	public String getBusinessDetailName() {
		return businessDetailName;
	}

	public void setBusinessDetailName(String businessDetailName) {
		this.businessDetailName = businessDetailName;
	}

	public int getSubmitTimes() {
		return submitTimes;
	}

	public void setSubmitTimes(int submitTimes) {
		this.submitTimes = submitTimes;
	}

	public String getAreaId() {
		return areaId;
	}

	public void setAreaId(String areaId) {
		this.areaId = areaId;
	}

	public String getAreaName() {
		return areaName;
	}

	public void setAreaName(String areaName) {
		this.areaName = areaName;
	}

	public String getSystemId() {
		return systemId;
	}

	public void setSystemId(String systemId) {
		this.systemId = systemId;
	}

	public String getSystemName() {
		return systemName;
	}

	public void setSystemName(String systemName) {
		this.systemName = systemName;
	}

	public String getProcessInstanceId() {
		return processInstanceId;
	}

	public void setProcessInstanceId(String processInstanceId) {
		this.processInstanceId = processInstanceId;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getApplyUserName() {
		return applyUserName;
	}

	public void setApplyUserName(String applyUserName) {
		this.applyUserName = applyUserName;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getCatalog() {
		return catalog;
	}

	public void setCatalog(String catalog) {
		this.catalog = catalog;
	}

	public String getProFlag() {
		return proFlag;
	}

	public void setProFlag(String proFlag) {
		this.proFlag = proFlag;
	}

	public String getApplyContent() {
		return applyContent;
	}

	public void setApplyContent(String applyContent) {
		this.applyContent = applyContent;
	}

	/**
	 * @return completeTime
	 */
	public Date getCompleteTime() {
		return completeTime;
	}

	/**
	 * @param completeTime
	 *            要设置的 completeTime
	 */
	public void setCompleteTime(Date completeTime) {
		this.completeTime = completeTime;
	}

	public String getWhole() {
		return whole;
	}

	public void setWhole(String whole) {
		this.whole = whole;
	}

	public boolean isCompare() {
		return compare;
	}

	public void setCompare(boolean compare) {
		this.compare = compare;
	}

	public String getAuditDuration() {
		return auditDuration;
	}

	public void setAuditDuration(String auditDuration) {
		this.auditDuration = auditDuration;
	}
	

}
