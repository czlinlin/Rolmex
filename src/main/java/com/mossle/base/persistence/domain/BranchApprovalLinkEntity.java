package com.mossle.base.persistence.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="oa_ba_process_condition_node")
public class BranchApprovalLinkEntity implements java.io.Serializable{
	private static final long serialVersionUID = 0L;
	private Long id;
	private String businessDetailId;
	private String conditionName;
	private String conditionType;
	private String conditionNode;
	private String note;
	
	
	public BranchApprovalLinkEntity(){
		
	}
	public BranchApprovalLinkEntity(Long id){
		this.id = id;
	}
	public BranchApprovalLinkEntity(Long id,String businessDetailId,String conditionName,String conditionType,String conditionNode,String note){
		this.id = id;
		this.businessDetailId = businessDetailId;
		this.conditionName = conditionName;
		this.conditionType = conditionType;
		this.conditionNode = conditionNode;
		this.note = note;
	}
	@Id
	@Column(name="id")
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	@Column(name="businessDetail_id")
	public String getBusinessDetailId() {
		return businessDetailId;
	}
	public void setBusinessDetailId(String businessDetailId) {
		this.businessDetailId = businessDetailId;
	}
	@Column(name="condition_name")
	public String getConditionName() {
		return conditionName;
	}
	public void setConditionName(String conditionName) {
		this.conditionName = conditionName;
	}
	@Column(name="condition_type")
	public String getConditionType() {
		return conditionType;
	}
	public void setConditionType(String conditionType) {
		this.conditionType = conditionType;
	}
	@Column(name="condition_node")
	public String getConditionNode() {
		return conditionNode;
	}
	public void setConditionNode(String conditionNode) {
		this.conditionNode = conditionNode;
	}
	@Column(name="note")
	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
	}
	
}
