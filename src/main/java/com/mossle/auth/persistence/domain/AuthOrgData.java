package com.mossle.auth.persistence.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name="Auth_OrgData")
public class AuthOrgData implements java.io.Serializable
{
	//private static final long serializableVersionUID=0L;
	
	/**
	 * 主键ID
	 * **/
	public long id;
	
	/**
	 * 类型：用户/角色
	 * **/
	public String type;
	
	/**
	 * 用户ID/角色ID
	 * **/
	public long unionId;
	
	/**
	 * 组织结构ID
	 * **/
	public long partyEntityId;

	@Id
	@Column(name="id")
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@Column(name="type")
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Column(name="union_id")
	public long getUnionId() {
		return unionId;
	}

	public void setUnionId(long unionId) {
		this.unionId = unionId;
	}

	@Column(name="partyEntityID")
	public long getPartyEntityId() {
		return partyEntityId;
	}

	public void setPartyEntityId(long partyEntityId) {
		this.partyEntityId = partyEntityId;
	}
}