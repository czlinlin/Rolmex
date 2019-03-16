package com.mossle.keyvalue.persistence.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "time_task")
public class TimeTaskInfo implements java.io.Serializable  {
	private static final long serialVersionUID = 0L;
	//private static final long serialVersionUID = 0L;
	
	private Long id;
	private String taskType;
    private String taskContent;
    private Date taskAddDate;
    private String taskNote;
    
    @Id
    @Column(name="id")
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	@Column(name="taskType")
	public String getTaskType() {
		return taskType;
	}
	public void setTaskType(String taskType) {
		this.taskType = taskType;
	}
	
	@Column(name="taskContent")
	public String getTaskContent() {
		return taskContent;
	}
	public void setTaskContent(String taskContent) {
		this.taskContent = taskContent;
	}
	
	@Column(name="taskAddDate")
	public Date getTaskAddDate() {
		return taskAddDate;
	}
	public void setTaskAddDate(Date taskAddDate) {
		this.taskAddDate = taskAddDate;
	}
	
	@Column(name="taskNote")
	public String getTaskNote() {
		return taskNote;
	}
	public void setTaskNote(String taskNote) {
		this.taskNote = taskNote;
	}
    
}