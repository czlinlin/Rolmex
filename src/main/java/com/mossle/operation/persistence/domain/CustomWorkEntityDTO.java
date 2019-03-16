package com.mossle.operation.persistence.domain;

public class CustomWorkEntityDTO implements java.io.Serializable{

	   /** 主键. */
    private Long id;
    /** 主题 */
    private String theme;
    /** 业务类型. */
    private String businessType; 
    /** 业务明细 */
    private String businessDetail; 
    /** 当前登录人的姓名 */
    private String name;
    /** 自定义表单类型 */
    private String formType;
    /** 请假类型 */
    private String type;
    /** 部门名称 */
    private String departmentName;
    /** 日期年 */
    private String year;
    /** 日期月 */
    private String mouth;
    /** 日期日 */
    private String day;
    /** 填表日期  */
    private String date;
    /** 开始日期年 */
    private String startYear;
    /** 开始日期月 */
    private String startMouth;
    /** 开始日期日 */
    private String startDay;
    /** 开始日期小时 */
    private String startHour;
    /** 开始日期分钟 */
    private String startMinute;
    /** 开始日期 */
    private String startDate;
    /** 结束日期年 */
    private String endYear;
    /** 结束日期月 */
    private String endMouth;
    /** 结束日期日 */
    private String endDay;
    /** 结束日期小时 */
    private String endHour;
    /** 结束日期分钟 */
    private String endMinute;
    /** 结束日期 */
    private String endDate;
    /** 总计/小时 */
    private String totalTime;
    /** 同行人id */
    private String txnos;
    /** 同行人姓名 */
    private String txName;
    /** 目的地 */
    private String destination;
    /** 申请内容 */
    private String applyContent;
    /** 附件的名称 */
    private String fileName;
    /** 附件路径 */
    private String filePath;
    /** 创建时间 */
    private String createTime;
    /** 流程实例ID */
    private String processInstanceId;
    /** 当前登录人ID */
    private Long userid;
    /** 该条申请修改时间 */
    private String modifyTime;
    /** 提交次数 */
    private int submitTimes;
    /** 受理单编号 */
    private String applyCode;
    /** 抄送人名称 */
    private String ccName;
    /** 抄送人id */
    private String ccnos;
    
    /** 业务级别. **/
    private String businessLevel;
    
    /**用于导出*/
    private String completeTime;
    /**一级审核人批示内容*/
    private String comment;
    
    

    
    
	public void setDate(String date) {
		this.date = date;
	}
	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}
	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}
	public String getBusinessType() {
		return businessType;
	}
	public void setBusinessType(String businessType) {
		this.businessType = businessType;
	}
	public String getBusinessDetail() {
		return businessDetail;
	}
	public void setBusinessDetail(String businessDetail) {
		this.businessDetail = businessDetail;
	}
	public String getTheme() {
		return theme;
	}
	public void setTheme(String theme) {
		this.theme = theme;
	}
	public String getStartHour() {
		return startHour;
	}
	public void setStartHour(String startHour) {
		this.startHour = startHour;
	}
	public String getStartMinute() {
		return startMinute;
	}
	public void setStartMinute(String startMinute) {
		this.startMinute = startMinute;
	}
	public String getEndHour() {
		return endHour;
	}
	public void setEndHour(String endHour) {
		this.endHour = endHour;
	}
	public String getEndMinute() {
		return endMinute;
	}
	public void setEndMinute(String endMinute) {
		this.endMinute = endMinute;
	}
	public String getDate() {
		return date;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getStartDate() {
		return startDate;
	}
	public String getEndDate() {
		return endDate;
	}
	public String getFormType() {
		return formType;
	}
	public void setFormType(String formType) {
		this.formType = formType;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDepartmentName() {
		return departmentName;
	}
	public void setDepartmentName(String departmentName) {
		this.departmentName = departmentName;
	}
	public String getYear() {
		return year;
	}
	public void setYear(String year) {
		this.year = year;
	}
	public String getMouth() {
		return mouth;
	}
	public void setMouth(String mouth) {
		this.mouth = mouth;
	}
	public String getDay() {
		return day;
	}
	public void setDay(String day) {
		this.day = day;
	}
	public String getStartYear() {
		return startYear;
	}
	public void setStartYear(String startYear) {
		this.startYear = startYear;
	}
	public String getStartMouth() {
		return startMouth;
	}
	public void setStartMouth(String startMouth) {
		this.startMouth = startMouth;
	}
	public String getStartDay() {
		return startDay;
	}
	public void setStartDay(String startDay) {
		this.startDay = startDay;
	}
	public String getEndYear() {
		return endYear;
	}
	public void setEndYear(String endYear) {
		this.endYear = endYear;
	}
	public String getEndMouth() {
		return endMouth;
	}
	public void setEndMouth(String endMouth) {
		this.endMouth = endMouth;
	}
	public String getEndDay() {
		return endDay;
	}
	public void setEndDay(String endDay) {
		this.endDay = endDay;
	}
	public String getTotalTime() {
		return totalTime;
	}
	public void setTotalTime(String totalTime) {
		this.totalTime = totalTime;
	}
	public String getTxnos() {
		return txnos;
	}
	public void setTxnos(String txnos) {
		this.txnos = txnos;
	}
	public String getTxName() {
		return txName;
	}
	public void setTxName(String txName) {
		this.txName = txName;
	}
	public String getDestination() {
		return destination;
	}
	public void setDestination(String destination) {
		this.destination = destination;
	}
	public String getApplyContent() {
		return applyContent;
	}
	public void setApplyContent(String applyContent) {
		this.applyContent = applyContent;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getFilePath() {
		return filePath;
	}
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	public String getCreateTime() {
		return createTime;
	}
	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}
	public String getProcessInstanceId() {
		return processInstanceId;
	}
	public void setProcessInstanceId(String processInstanceId) {
		this.processInstanceId = processInstanceId;
	}
	public Long getUserid() {
		return userid;
	}
	public void setUserid(Long userid) {
		this.userid = userid;
	}
	public String getModifyTime() {
		return modifyTime;
	}
	public void setModifyTime(String modifyTime) {
		this.modifyTime = modifyTime;
	}
	public int getSubmitTimes() {
		return submitTimes;
	}
	public void setSubmitTimes(int submitTimes) {
		this.submitTimes = submitTimes;
	}
	public String getApplyCode() {
		return applyCode;
	}
	public void setApplyCode(String applyCode) {
		this.applyCode = applyCode;
	}
	public String getCcName() {
		return ccName;
	}
	public void setCcName(String ccName) {
		this.ccName = ccName;
	}
	public String getCcnos() {
		return ccnos;
	}
	public void setCcnos(String ccnos) {
		this.ccnos = ccnos;
	}
	public String getBusinessLevel() {
		return businessLevel;
	}
	public void setBusinessLevel(String businessLevel) {
		this.businessLevel = businessLevel;
	}
	public String getCompleteTime() {
		return completeTime;
	}
	public void setCompleteTime(String completeTime) {
		this.completeTime = completeTime;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
    
    
    
    
	
}
