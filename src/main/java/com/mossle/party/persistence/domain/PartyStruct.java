package com.mossle.party.persistence.domain;

// Generated by Hibernate Tools
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.mossle.user.persistence.domain.PersonInfo;

/**
 * PartyStruct 组织关系.
 * 
 * @author Lingo
 */
@Entity
@Table(name = "PARTY_STRUCT")
public class PartyStruct implements java.io.Serializable {
    private static final long serialVersionUID = 0L;

    /** 主键. */
    private Long id;

    /** 外键，上级实体. */
    private PartyEntity parentEntity;

    /** 外键，下级实体. */
    private PartyEntity childEntity;

    /** 外键，组织关系类型. */
    private PartyStructType partyStructType;

    /** 排序. */
    private Integer priority;

    /** 租户. */
    private String tenantId;

    /** 兼职. */
    private Integer partTime;

    /** 关联. */
    private Integer link;

    /** 管理. */
    private Integer admin;
    
    /** 录入人. */
    private Long addUserId;

    /** null. */
    private String type;
    
/*    *//** 关联personInfo *//*
    private PersonInfo personInfo;*/
    

    public PartyStruct() {
    }

    public PartyStruct(Long id, PartyEntity childEntity,
            PartyStructType partyStructType) {
        this.id = id;
        this.childEntity = childEntity;
        this.partyStructType = partyStructType;
    }

    public PartyStruct(Long id, PartyEntity parentEntity,
            PartyEntity childEntity, PartyStructType partyStructType,
            Integer priority, String tenantId, Integer partTime, Integer link,
            Integer admin, String type, Long addUserId) {
        this.id = id;
        this.parentEntity = parentEntity;
        this.childEntity = childEntity;
        this.partyStructType = partyStructType;
        this.priority = priority;
        this.tenantId = tenantId;
        this.partTime = partTime;
        this.link = link;
        this.admin = admin;
        this.type = type;
        this.addUserId = addUserId;
    }

    /** @return 主键. */
    @Id
    @Column(name = "ID", unique = true, nullable = false)
    public Long getId() {
        return this.id;
    }

    /**
     * @param id
     *            主键.
     */
    public void setId(Long id) {
        this.id = id;
    }

    
    
 /*   *//** @return personInfo实体. *//*
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID")
    public PersonInfo getPersonInfo() {
		return personInfo;
	}

	public void setPersonInfo(PersonInfo personInfo) {
		this.personInfo = personInfo;
	}*/

	/** @return 外键，上级实体. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PARENT_ENTITY_ID")
    public PartyEntity getParentEntity() {
        return this.parentEntity;
    }

    /**
     * @param parentEntity
     *            外键，上级实体.
     */
    public void setParentEntity(PartyEntity parentEntity) {
        this.parentEntity = parentEntity;
    }

    /** @return 外键，下级实体. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CHILD_ENTITY_ID", nullable = false)
    public PartyEntity getChildEntity() {
        return this.childEntity;
    }

    /**
     * @param childEntity
     *            外键，下级实体.
     */
    public void setChildEntity(PartyEntity childEntity) {
        this.childEntity = childEntity;
    }

    /** @return 外键，组织关系类型. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "STRUCT_TYPE_ID", nullable = false)
    public PartyStructType getPartyStructType() {
        return this.partyStructType;
    }

    /**
     * @param partyStructType
     *            外键，组织关系类型.
     */
    public void setPartyStructType(PartyStructType partyStructType) {
        this.partyStructType = partyStructType;
    }

    /** @return 排序. */
    @Column(name = "PRIORITY")
    public Integer getPriority() {
        return this.priority;
    }

    /**
     * @param priority
     *            排序.
     */
    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    /** @return 租户. */
    @Column(name = "TENANT_ID", length = 50)
    public String getTenantId() {
        return this.tenantId;
    }

    /**
     * @param tenantId
     *            租户.
     */
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    /** @return 兼职. */
    @Column(name = "PART_TIME")
    public Integer getPartTime() {
        return this.partTime;
    }

    /**
     * @param partTime
     *            兼职.
     */
    public void setPartTime(Integer partTime) {
        this.partTime = partTime;
    }

    /** @return 关联. */
    @Column(name = "LINK")
    public Integer getLink() {
        return this.link;
    }

    /**
     * @param link
     *            关联.
     */
    public void setLink(Integer link) {
        this.link = link;
    }

    /** @return 管理. */
    @Column(name = "ADMIN")
    public Integer getAdmin() {
        return this.admin;
    }

    /**
     * @param admin
     *            管理.
     */
    public void setAdmin(Integer admin) {
        this.admin = admin;
    }

    /** @return null. */
    @Column(name = "TYPE", length = 50)
    public String getType() {
        return this.type;
    }

    /**
     * @param type
     *            null.
     */
    public void setType(String type) {
        this.type = type;
    }

    /** @return 录入人. */
    @Column(name = "ADD_USER_ID")
	public Long getAddUserId() {
		return addUserId;
	}
    /**
     * @param addUserId
     *            录入人.
     */
	public void setAddUserId(Long addUserId) {
		this.addUserId = addUserId;
	}
    
    
}
