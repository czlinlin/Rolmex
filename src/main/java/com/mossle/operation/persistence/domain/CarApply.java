package com.mossle.operation.persistence.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "oa_bpm_car_apply")
public class CarApply implements java.io.Serializable {
	private Long id;
	private String applyCode; 			// 受理单编号
	private String processInstanceID; 	// 流程实例ID
	private String ucode;				// 申请人编号
	private Long departmentCode;		//部门编号
	private String departmentName;		//部门名称
	private String businessType;
	private String businessDetail;
	private String destination;			//目的地
	private String content;				//用车事由
	private String plateNumber;			//车牌号
	private String carUser;				//用车人
	private String driver;				//驾驶人
	private String borrowCarMileage;	//借车里程
	private String returnCarMileage;	//还车里程
	private String mileage;				//行驶里程
	private String borrowCarTime;		//用车开始时间
	private String returnCarTime;		//用车结束时间
	private String totalTime;			//用车共计时长
	private String oilMoney;			//加油金额
	private String remainOil;			//剩余油量
	private String remark;				//备注
	
	public CarApply(){
		
	}
	public CarApply( Long id){
		this.id = id;
	}
	
	@Id
    @Column(name = "id")
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	
	
	/** @return 受理单编号. */
	@Column(name="applyCode")
	public String getApplyCode() {
		return applyCode;
	}
	public void setApplyCode(String applyCode) {
		this.applyCode = applyCode;
	}
	
	 /** @return 流程实例id. */
	@Column(name="PROCESS_INSTANCEID")
	public String getProcessInstanceID() {
		return processInstanceID;
	}
	public void setProcessInstanceID(String processInstanceID) {
		this.processInstanceID = processInstanceID;
	}
	
	 /** @return 申请人编号. */
    @Column(name = "ucode")
	public String getUcode() {
		return ucode;
	}
	public void setUcode(String ucode) {
		this.ucode = ucode;
	}
	
	 /** @return 部门编号. */
    @Column(name = "departmentCode")
	public Long getDepartmentCode() {
		return departmentCode;
	}
	public void setDepartmentCode(Long departmentCode) {
		this.departmentCode = departmentCode;
	}
	
	 /** @return 部门名称. */
    @Column(name = "departmentName")
	public String getDepartmentName() {
		return departmentName;
	}
	public void setDepartmentName(String departmentName) {
		this.departmentName = departmentName;
	}
	
	 /** @return 业务类型. */
    @Column(name = "businessType")
	 public String getBusinessType() {
		return businessType;
	}
	public void setBusinessType(String businessType) {
		this.businessType = businessType;
	}
	
	 /** @return 业务细分. */
    @Column(name = "businessDetail")
	public String getBusinessDetail() {
		return businessDetail;
	}
	public void setBusinessDetail(String businessDetail) {
		this.businessDetail = businessDetail;
	}
	
	
	/** @return 目的地. */
    @Column(name = "destination")
	public String getDestination() {
		return destination;
	}
	public void setDestination(String destination) {
		this.destination = destination;
	}
	
	 /** @return 用车事由. */
    @Column(name = "useCarReason")
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	
	 /** @return 车牌号. */
    @Column(name = "plateNumber")
	public String getPlateNumber() {
		return plateNumber;
	}
	public void setPlateNumber(String plateNumber) {
		this.plateNumber = plateNumber;
	}
	
	 /** @return 用车人. */
    @Column(name = "carUser")
	public String getCarUser() {
		return carUser;
	}
	public void setCarUser(String carUser) {
		this.carUser = carUser;
	}
	
	 /** @return 驾驶人. */
    @Column(name = "driver")
	public String getDriver() {
		return driver;
	}
	public void setDriver(String driver) {
		this.driver = driver;
	}
	
	 /** @return 借车里程. */
    @Column(name = "borrowCarMileage")
	public String getBorrowCarMileage() {
		return borrowCarMileage;
	}
	public void setBorrowCarMileage(String borrowCarMileage) {
		this.borrowCarMileage = borrowCarMileage;
	}
	
	 /** @return 还车里程. */
    @Column(name = "returnCarMileage")
	public String getReturnCarMileage() {
		return returnCarMileage;
	}
	public void setReturnCarMileage(String returnCarMileage) {
		this.returnCarMileage = returnCarMileage;
	}
	
	 /** @return 行驶里程. */
    @Column(name = "mileage")
	public String getMileage() {
		return mileage;
	}
	public void setMileage(String mileage) {
		this.mileage = mileage;
	}
	
	 /** @return 用车开始时间. */
    @Column(name = "borrowCarTime")
	public String getBorrowCarTime() {
		return borrowCarTime;
	}
	public void setBorrowCarTime(String borrowCarTime) {
		this.borrowCarTime = borrowCarTime;
	}
	
	 /** @return 用车结束时间. */
    @Column(name = "returnCarTime")
	public String getReturnCarTime() {
		return returnCarTime;
	}
	public void setReturnCarTime(String returnCarTime) {
		this.returnCarTime = returnCarTime;
	}
	
	 /** @return 用车共计时长. */
    @Column(name = "totalTime")
	public String getTotalTime() {
		return totalTime;
	}
	public void setTotalTime(String totalTime) {
		this.totalTime = totalTime;
	}
	
	 /** @return 加油金额. */
    @Column(name = "oilMoney")
	public String getOilMoney() {
		return oilMoney;
	}
	public void setOilMoney(String oilMoney) {
		this.oilMoney = oilMoney;
	}
	
	 /** @return 剩余油量. */
    @Column(name = "remainOil")
	public String getRemainOil() {
		return remainOil;
	}
	public void setRemainOil(String remainOil) {
		this.remainOil = remainOil;
	}
	
	 /** @return 备注. */
    @Column(name = "remark")
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}   
	
}
