package com.mossle.user.persistence.domain;

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
@Table(name = "person_worknumber")
public class PersonWorkNumber implements java.io.Serializable {
    private static final long serialVersionUID = 0L;

    /** 主键. */
    private Long id;

    /** 修改花名册实体类的内容. */
    private Integer numberNo;

    /** 1：流程审核同意   0：流程审核不同意. */
    private String isUse;

    
	public PersonWorkNumber() {
    }

	
    public PersonWorkNumber(Long id) {
        this.id = id;
    }

    @Id
    @Column(name="ID")
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name="numberNo")
	public Integer getNumberNo() {
		return numberNo;
	}

	public void setNumberNo(Integer numberNo) {
		this.numberNo = numberNo;
	}

	@Column(name="isUse")
	public String getIsUse() {
		return isUse;
	}

	public void setIsUse(String isUse) {
		this.isUse = isUse;
	}

    	
    }
