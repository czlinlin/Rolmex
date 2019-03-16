package com.mossle.operation.persistence.domain;

// Generated by Hibernate Tools
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.mossle.internal.store.persistence.domain.StoreInfo;

/**
 * 
 * 业务申请 实体类
 * @author sjx
 */
public class GroupBusinessDTO implements java.io.Serializable {
	
    private static final long serialVersionUID = 0L;

    /** 主键. */
    private Long id;

    /** 主题. */
    private String theme;

    /** 抄送. */
    private String cc;
    private String copyUserValue;

    /** 申请的业务类型. */
    private String businessType;

    /** 业务细分. */
    private String businessDetail;

    /** 业务级别. */
    private String businessLevel;

    /** 发起人. */
    private String initiator;
    
    /** 申请内容 */
    private String applyContent;
    
    /** 提交次数 */
    private String submitTimes;
    
    /** 申请单号 */
    private String applyCode;
    
    /** 附件名 */
    private String enclosure;
    
    /** 附件路径 */
    private String path;
    
    /** 申请人. */
    private Long userId;
    
    /** 流程实例Id. */
    private String processInstanceId;
    
    private List<StoreInfo> storeInfos;
    
    public GroupBusinessDTO() {
    }

    public GroupBusinessDTO(Long id) {
        this.id = id;
    }

    
    public GroupBusinessDTO(Long id, String theme, String cc,String copyUserValue,String businessType,String businessDetail,String businessLevel,
    		String initiator, String applyContent,String submitTimes,String applyCode, String enclosure,String path,
    		Long userId, String processInstanceId) {
        this.id = id;
        this.theme = theme;
        this.cc = cc;
        this.copyUserValue = copyUserValue;
        this.businessType = businessType;
        this.businessDetail = businessDetail;
        this.businessLevel = businessLevel;
        this.initiator = initiator;
        this.applyContent = applyContent;
        this.submitTimes = submitTimes;
        this.applyCode = applyCode;
        this.enclosure = enclosure;
        this.path = path;
        this.userId = userId;
        this.processInstanceId = processInstanceId;
    }

    /** @return 主键. */
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


    /** @return 主题. */
    public String getTheme() {
        return this.theme;
    }

    /**
     * @param theme
     * 主题.
     */
    public void setTheme(String theme) {
        this.theme = theme;
    }
    
    /** @return 抄送. */
    public String getCc() {
        return this.cc;
    }

    /**
     * @param cc
     * 抄送.
     */
    public void setCc(String cc) {
        this.cc = cc;
    }
    
    public String getCopyUserValue() {
		return copyUserValue;
	}

	public void setCopyUserValue(String copyUserValue) {
		this.copyUserValue = copyUserValue;
	}

	/** @return 业务类型. */
    public String getBusinessType() {
        return this.businessType;
    }

    /**
     * @param businessType
     * 业务类型.
     */
    public void setBusinessType(String businessType) {
        this.businessType = businessType;
    }

    /** @return 业务细分. */
    public String getBusinessDetail() {
        return this.businessDetail;
    }

    /**
     * @param businessDetail
     * 业务细分.
     */
    public void setBusinessDetail(String businessDetail) {
        this.businessDetail = businessDetail;
    }

    /** @return 业务级别. */
    public String getBusinessLevel() {
        return this.businessLevel;
    }

    /**
     * @param businessLevel
     * 业务级别.
     */
    public void setBusinessLevel(String businessLevel) {
        this.businessLevel = businessLevel;
    }

    /** @return 发起人. */
    public String getInitiator() {
    	return this.initiator;
    }
    
    /**
     * @param initiator
     * 发起人.
     */
    public void setInitiator(String initiator) {
    	this.initiator = initiator;
    }
    

	public String getApplyContent() {
		return applyContent;
	}

	public void setApplyContent(String applyContent) {
		this.applyContent = applyContent;
	}
	
	public String getSubmitTimes() {
		return submitTimes;
	}

	public void setSubmitTimes(String submitTimes) {
		this.submitTimes = submitTimes;
	}
	
	public String getApplyCode() {
		return applyCode;
	}

	public void setApplyCode(String applyCode) {
		this.applyCode = applyCode;
	}

	public String getEnclosure() {
		return enclosure;
	}

	public void setEnclosure(String enclosure) {
		this.enclosure = enclosure;
	}
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	/** @return 申请人. */
    public Long getUserId() {
        return this.userId;
    }
    /**
     * @param userId
     *            申请人.
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    /** @return 流程实例ID. */
    public String getProcessInstanceId() {
        return this.processInstanceId;
    }

    /**
     * @param processInstanceId
     *            流程实例ID.
     */
    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

	public List<StoreInfo> getStoreInfos() {
		return storeInfos;
	}

	public void setStoreInfos(List<StoreInfo> storeInfos) {
		this.storeInfos = storeInfos;
	}
    
}
