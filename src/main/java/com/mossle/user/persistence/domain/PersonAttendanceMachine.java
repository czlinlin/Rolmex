package com.mossle.user.persistence.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * PersonWorkNumber 个人信息-工号流水号.
 * 
 * @author lilei
 */
@Entity
@Table(name = "person_machine")
public class PersonAttendanceMachine implements java.io.Serializable {
    private static final long serialVersionUID = 0L;

    /** 主键. */
    private Long id;

    private Long personId;

    private String mach_no;
    
    private String user_no;
    
    private String remark;
    
    private Date create_date;
    
    private Date modify_date;
    private String is_modify;
    private String modify_num;

    public PersonAttendanceMachine() {
    }
    public PersonAttendanceMachine(Long id,Long personId,String mach_no,String user_no,String remark,Date create_date,Date modify_date,String is_modify,String modify_num) {
    	this.id = id;
    	this.personId=personId;
    	this.mach_no=mach_no;
    	this.user_no=user_no;
    	this.remark=remark;
    	this.create_date=create_date;
    	this.modify_date=modify_date;
    	this.is_modify=is_modify;
    	this.modify_num=modify_num;
    }

    @Id
    @Column(name="id")
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name="person_id")
	public Long getPersonId() {
		return personId;
	}

	public void setPersonId(Long personId) {
		this.personId = personId;
	}

	@Column(name="mach_no")
	public String getMach_no() {
		return mach_no;
	}

	public void setMach_no(String mach_no) {
		this.mach_no = mach_no;
	}

	@Column(name="user_no")
	public String getUser_no() {
		return user_no;
	}

	public void setUser_no(String user_no) {
		this.user_no = user_no;
	}

	@Column(name="remark")
	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	@Column(name="create_date")
	public Date getCreate_date() {
		return create_date;
	}

	public void setCreate_date(Date create_date) {
		this.create_date = create_date;
	}
	@Column(name="modify_date")
	public Date getModify_date() {
		return modify_date;
	}
	public void setModify_date(Date modify_date) {
		this.modify_date = modify_date;
	}
	@Column(name="is_modify")
	public String getIs_modify() {
		return is_modify;
	}
	public void setIs_modify(String is_modify) {
		this.is_modify = is_modify;
	}
	@Column(name="modify_num")
	public String getModify_num() {
		return modify_num;
	}
	public void setModify_num(String modify_num) {
		this.modify_num = modify_num;
	}
	
    	
    }
