package com.mossle.pim.persistence.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "work_report_cc_presetting")
public class WorkReportCcPresetting implements java.io.Serializable{
	private static final long serialVersionUID = 0L;
	
	//汇报抄送条线设置表（适用于分公司的汇报）
    private  Long id;
   
	 //名称
    private  String title;
   
	 //状态(1：正常，2：禁用，0：删除)
    private  String status;
   
	 //备注
    private  String note;
    
    public WorkReportCcPresetting(){
    }
    
    public WorkReportCcPresetting(Long id){
    	this.id=id;
    }

    public WorkReportCcPresetting(Long id,String title,String status,String note){
    	this.id=id;
    	this.title=title;
    	this.status=status;
    	this.note=note;
    }
    
    @Id
    @Column(name="id")
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name="title")
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Column(name="status")
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@Column(name="note")
	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}
    
    
}
