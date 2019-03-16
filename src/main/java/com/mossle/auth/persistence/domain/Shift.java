package com.mossle.auth.persistence.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="shift")
public class Shift {
	private long id;
	private String shiftName;
	private String startTime;
	private String endTime;
	private String restStartTime;
	private String restEndTime;
	private int delStatus;
	private int shiftType;
	private int defaultShiftType;
	@Id
	@Column(name="id", unique = true, nullable = false)
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	@Column(name="shift_name")
	public String getShiftName() {
		return shiftName;
	}
	public void setShiftName(String shiftName) {
		this.shiftName = shiftName;
	}
	@Column(name="start_time")
	public String getStartTime() {
		return startTime;
	}
	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}
	@Column(name="end_time")
	public String getEndTime() {
		return endTime;
	}
	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}
	@Column(name="rest_start_time")
	public String getRestStartTime() {
		return restStartTime;
	}
	public void setRestStartTime(String restStartTime) {
		this.restStartTime = restStartTime;
	}
	@Column(name="rest_end_time")
	public String getRestEndTime() {
		return restEndTime;
	}
	public void setRestEndTime(String restEndTime) {
		this.restEndTime = restEndTime;
	}
	@Column(name="del_status")
	public int getDelStatus() {
		return delStatus;
	}
	public void setDelStatus(int delStatus) {
		this.delStatus = delStatus;
	}
	@Column(name="shift_type")
	public int getShiftType() {
		return shiftType;
	}
	public void setShiftType(int shiftType) {
		this.shiftType = shiftType;
	}
	@Column(name="default_shift_type")
	public int getDefaultShiftType() {
		return defaultShiftType;
	}
	public void setDefaultShiftType(int defaultShiftType) {
		this.defaultShiftType = defaultShiftType;
	}
	
}
