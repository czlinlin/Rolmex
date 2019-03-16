package com.mossle.user.persistence.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author sjx
 * 18.12.10
 * 合同单位数据权限
 *
 */
@Entity
@Table(name="auth_contractdata")
public class AuthContractCompany implements java.io.Serializable{
	private static final long serialVersionUID = 0L;
	private Long id;
	//配置对象的id
	private String unionId;
	//合同单位的id
	private String contractCompanyId;
	//配置对象的类型
	private Integer type;
	//备注
	private String note;
	
	public AuthContractCompany() {
		
	}
	public AuthContractCompany(Long id){
		this.id = id;
	}
	@Id
	@Column(name="id")
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	@Column(name="union_id")
	public String getUnionId() {
		return unionId;
	}
	public void setUnionId(String unionId) {
		this.unionId = unionId;
	}
	@Column(name="contract_company_id")
	public String getContractCompanyId() {
		return contractCompanyId;
	}
	public void setContractCompanyId(String contractCompanyId) {
		this.contractCompanyId = contractCompanyId;
	}
	@Column(name="type")
	public Integer getType() {
		return type;
	}
	public void setType(Integer type) {
		this.type = type;
	}
	@Column(name="note")
	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
	}
	
	
	
}
