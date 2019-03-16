package com.mossle.party.persistence.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
@Entity
@Table(name="attendance")
public class AttendanceEntity implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 0L;
	private Long id;
	private String departmentId;//部门id
	private String departmentName;//部门名称
	private String userId;//用户id
	private String userCode;//用户工号
	private String worker;//姓名
	private Date workDate;
	private String goToWork;//实际上班打卡时间
	private String goOffWork;//实际下班打卡时间
	private String signIn;
	private String year;
	private String month;
	private String day;
	private String constraintToWork;//规定上班打卡时间
	private String constraintOffWork;//规定下班打卡时间
	private String machNo;//机器号
	private String userNo; //员工号
	
	
	
	public AttendanceEntity() {
	}
	public AttendanceEntity(Long id) {
		this.id = id;
	}
	public AttendanceEntity(Long id, String departmentId, String departmentName, String userId, String userCode,
			String worker, Date workDate, String goToWork, String goOffWork, String signIn, String year, String month,
			String day, String constraintToWork, String constraintOffWork, String machNo, String userNo) {
		this.id = id;
		this.departmentId = departmentId;
		this.departmentName = departmentName;
		this.userId = userId;
		this.userCode = userCode;
		this.worker = worker;
		this.workDate = workDate;
		this.goToWork = goToWork;
		this.goOffWork = goOffWork;
		this.signIn = signIn;
		this.year = year;
		this.month = month;
		this.day = day;
		this.constraintToWork = constraintToWork;
		this.constraintOffWork = constraintOffWork;
		this.machNo = machNo;
		this.userNo = userNo;
	}

	@Column(name="mach_no")
	public String getMachNo() {
		return machNo;
	}
	public void setMachNo(String machNo) {
		this.machNo = machNo;
	}
	@Column(name="user_no")
	public String getUserNo() {
		return userNo;
	}
	public void setUserNo(String userNo) {
		this.userNo = userNo;
	}
	@Id
	@Column(name="id")
	public Long getId() {
		return this.id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	@Column(name="user_id")
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	@Column(name="user_code")
	public String getUserCode() {
		return userCode;
	}
	public void setUserCode(String userCode) {
		this.userCode = userCode;
	}
	@Column(name="department_id")
	public String getDepartmentId() {
		return departmentId;
	}
	
	public void setDepartmentId(String departmentId) {
		this.departmentId = departmentId;
	}
	@Column(name="department_name")
	public String getDepartmentName() {
		return departmentName;
	}
	public void setDepartmentName(String departmentName) {
		this.departmentName = departmentName;
	}
	@Column(name="worker")
	public String getWorker() {
		return worker;
	}
	public void setWorker(String worker) {
		this.worker = worker;
	}
	@Column(name="work_date")
	public Date getWorkDate() {
		return workDate;
	}
	public void setWorkDate(Date workDate) {
		this.workDate = workDate;
	}
	@Column(name="go_to_work")
	public String getGoToWork() {
		return goToWork;
	}
	public void setGoToWork(String goToWork) {
		this.goToWork = goToWork;
	}
	@Column(name="go_off_work")
	public String getGoOffWork() {
		return goOffWork;
	}
	public void setGoOffWork(String goOffWork) {
		this.goOffWork = goOffWork;
	}
	@Column(name="sign_in")
	public String getSignIn() {
		return signIn;
	}
	public void setSignIn(String signIn) {
		this.signIn = signIn;
	}
	@Column(name="year")
	public String getYear() {
		return year;
	}
	public void setYear(String year) {
		this.year = year;
	}
	@Column(name="month")
	public String getMonth() {
		return month;
	}
	public void setMonth(String month) {
		this.month = month;
	}
	@Column(name="day")
	public String getDay() {
		return day;
	}
	public void setDay(String day) {
		this.day = day;
	}
	@Column(name="constraint_to_work")
	public String getConstraintToWork() {
		return constraintToWork;
	}
	public void setConstraintToWork(String constraintToWork) {
		this.constraintToWork = constraintToWork;
	}
	@Column(name="constraint_off_work")
	public String getConstraintOffWork() {
		return constraintOffWork;
	}
	public void setConstraintOffWork(String constraintOffWork) {
		this.constraintOffWork = constraintOffWork;
	}
	
}
