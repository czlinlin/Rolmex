package com.mossle.party.persistence.domain;

import java.util.Date;

public class AttendanceEntityPojo {

	private String id;
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
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getDepartmentId() {
		return departmentId;
	}
	public void setDepartmentId(String departmentId) {
		this.departmentId = departmentId;
	}
	public String getDepartmentName() {
		return departmentName;
	}
	public void setDepartmentName(String departmentName) {
		this.departmentName = departmentName;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getUserCode() {
		return userCode;
	}
	public void setUserCode(String userCode) {
		this.userCode = userCode;
	}
	public String getWorker() {
		return worker;
	}
	public void setWorker(String worker) {
		this.worker = worker;
	}
	public Date getWorkDate() {
		return workDate;
	}
	public void setWorkDate(Date workDate) {
		this.workDate = workDate;
	}
	public String getGoToWork() {
		return goToWork;
	}
	public void setGoToWork(String goToWork) {
		this.goToWork = goToWork;
	}
	public String getGoOffWork() {
		return goOffWork;
	}
	public void setGoOffWork(String goOffWork) {
		this.goOffWork = goOffWork;
	}
	public String getSignIn() {
		return signIn;
	}
	public void setSignIn(String signIn) {
		this.signIn = signIn;
	}
	public String getYear() {
		return year;
	}
	public void setYear(String year) {
		this.year = year;
	}
	public String getMonth() {
		return month;
	}
	public void setMonth(String month) {
		this.month = month;
	}
	public String getDay() {
		return day;
	}
	public void setDay(String day) {
		this.day = day;
	}
	public String getConstraintToWork() {
		return constraintToWork;
	}
	public void setConstraintToWork(String constraintToWork) {
		this.constraintToWork = constraintToWork;
	}
	public String getConstraintOffWork() {
		return constraintOffWork;
	}
	public void setConstraintOffWork(String constraintOffWork) {
		this.constraintOffWork = constraintOffWork;
	}
	public String getMachNo() {
		return machNo;
	}
	public void setMachNo(String machNo) {
		this.machNo = machNo;
	}
	public String getUserNo() {
		return userNo;
	}
	public void setUserNo(String userNo) {
		this.userNo = userNo;
	}
	
	
	
}
