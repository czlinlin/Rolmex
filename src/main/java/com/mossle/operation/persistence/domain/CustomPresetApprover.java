package com.mossle.operation.persistence.domain;



import java.sql.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.joda.time.DateTime;

/**
 * @author lilei:
 * @date 2018-07-26
 * 预设审批人
 */
@Entity
@Table(name = "Custom_PresetApprover")
public class CustomPresetApprover implements java.io.Serializable {
	private static final long serialVersionUID = 0L;
	private Long id;

	/**
	 * 设置人ID
	 * **/
	private Long userId;
	/**
	 * 审批人ID（多个用逗号隔开）
	 * **/
	private String approverIds;
	/**
	 * 预设审批人的设置名称
	 * **/
	private String name;
	/**
	 * 排序号
	 * **/
	private Integer orderNum;
	
	/**
	 * 添加时间
	 * **/
	private java.util.Date createDate;
	
	/**
	 * 删除状态
	 * **/
	private String delStatus;
	/**
	 * 删除时间
	 * **/
	private java.util.Date delDate;
	/**
	 * 备注
	 * **/
	private String remark;
	
	@Id
	@Column(name="id")
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	/**
	 * 设置人ID
	 * **/
	@Column(name="userId")
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	
	/**
	 * 审批人ID（多个用逗号隔开）
	 * **/
	@Column(name="approverIds")
	public String getApproverIds() {
		return approverIds;
	}
	public void setApproverIds(String approverIds) {
		this.approverIds = approverIds;
	}
	
	/**
	 * 预设审批人的设置名称
	 * **/
	@Column(name="name")
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * 排序号
	 * **/
	@Column(name="orderNum")
	public Integer getOrderNum() {
		return orderNum;
	}
	public void setOrderNum(Integer orderNum) {
		this.orderNum = orderNum;
	}
	
	/**
	 * 添加时间
	 * **/
	@Column(name="createDate")
	public java.util.Date getCreateDate() {
		return createDate;
	}
	public void setCreateDate(java.util.Date createDate) {
		this.createDate = createDate;
	}
	
	/**
	 * 删除状态
	 * **/
	@Column(name="delStatus")
	public String getDelStatus() {
		return delStatus;
	}
	public void setDelStatus(String delStatus) {
		this.delStatus = delStatus;
	}
	
	/**
	 * 删除时间
	 * **/
	@Column(name="delDate")
	public java.util.Date getDelDate() {
		return delDate;
	}
	public void setDelDate(java.util.Date delDate) {
		this.delDate = delDate;
	}
	
	/**
	 * 备注
	 * **/
	@Column(name="remark")
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	
	
}
