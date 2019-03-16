package com.mossle.operation.persistence.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Id;

/**
 * @author cz:
 * @version 创建时间：2017年9月15日 上午10:40
 * 自定义申请
 */
public class CustomEntityDTO extends CommonDTO implements java.io.Serializable  {
	private static final long serialVersionUID = 0L;
    
    /** 主键. */
    private Long id;
    /** 主题 */
    private String theme;
    /** 业务类型. */
    private String businessType; 
    /** 业务明细 */
    private String businessDetail; 
    /** 当前登录人的姓名 */
    private String name;
    /** 申请内容 */
    private String applyContent;
    /** 附件的名称 */
    private String fileName;
    /** 附件路径 */
    private String filePath;
    /** 创建时间 */
    private String createTime;
    /** 流程实例ID */
    private String processInstanceId;
    /** 当前登录人ID */
    private Long userid;
    /** 该条申请修改时间 */
    private String modifyTime;
    /** 提交次数 */
    private int submitTimes;
    /** 受理单编号 */
    private String applyCode;
    /** 抄送人名称 */
    private String ccName;
    /** 抄送人id */
    private String ccnos;
    
    /** 业务级别. **/
    private String businessLevel;
    
    /**用于导出*/
    private String completeTime;
    /**一级审核人批示内容*/
    private String comment;
    
    public CustomEntityDTO() {
    }

    public CustomEntityDTO(Long id) {
        this.id = id;
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
     * @param 主题
     *   .
     */
    public void setTheme(String theme) {
        this.theme = theme;
    }    
    
    /** @return 业务类型. */
    public String getBusinessType() {
        return this.businessType;
    }

    /**
     * @param 
     *   业务类型.
     */
    public void setBusinessType(String businessType) {
        this.businessType = businessType;
    }   
    
    /** @return 业务明细. */
    public String getBusinessDetail() {
        return this.businessDetail;
    }

    /**
     * @param 
     *   业务明细.
     */
    public void setBusinessDetail(String businessDetail) {
        this.businessDetail = businessDetail;
    } 
    
    /** @return  当前登录人的姓名. */
    public String getName() {
        return this.name;
    }

    /**
     * @param 
     *   当前登录人的姓名.
     */
    public void setName(String name) {
        this.name = name;
    } 
    
    /** @return  申请内容. */   
	public String getApplyContent() {
		return applyContent;
	}

	public void setApplyContent(String applyContent) {
		this.applyContent = applyContent;
	}

	/** @return  附件的名称. */
    public String getFileName() {
        return this.fileName;
    }

    /**
     * @param 
     *   附件的名称.
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    } 
    
    /** @return  附件路径. */
    public String getFilePath() {
        return this.filePath;
    }

    /**
     * @param content
     *  附件路径.
     */
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    } 
    
    /** @return  流程实例ID. */
    public String getProcessInstanceId() {
        return this.processInstanceId;
    }

    /**
     * @param content
     *  流程实例ID.
     */
    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    } 
    
    /** @return  */
    public String getCreateTime() {
        return this.createTime;
    }

    /**
     * @param 
     */
    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
    
    /** @return . */
    public String getModifyTime() {
        return this.modifyTime;
    }

    /**
     * @param content
     *            .
     */
    public void setModifyTime(String modifyTime) {
        this.modifyTime = modifyTime;
    }  
    

    /** @return 申请人. */
    public Long getUserId() {
        return this.userid;
    }
    /**
     * @param id
     *            申请人.
     */
    public void setUserId(Long userId) {
        this.userid = userId;
    }
    
    /**
     * @param 提交次数
     *            .
     */
    public int getSubmitTimes() {
		return submitTimes;
	}

	public void setSubmitTimes(int submitTimes) {
		this.submitTimes = submitTimes;
	}
	
	 /** @return 受理单编号. */
    public String getApplyCode() {
  		return applyCode;
  	}

  	public void setApplyCode(String applyCode) {
  		this.applyCode = applyCode;
  	}
  	
	/** @return  抄送人名称. */
 	 public String getCcName() {
			return ccName;
	}

	public void setCcName(String ccName) {
		this.ccName = ccName;
	}
	
	/** @return  抄送人id. */
 	public String getCcnos() {
		return ccnos;
	}

	public void setCcnos(String ccnos) {
		this.ccnos = ccnos;
	}

	/** @return 业务级别. */
	public String getBusinessLevel() {
		return businessLevel;
	}

	/** @param 业务级别. */
	public void setBusinessLevel(String businessLevel) {
		this.businessLevel = businessLevel;
	}

	public String getCompleteTime() {
		return completeTime;
	}

	public void setCompleteTime(String completeTime) {
		this.completeTime = completeTime;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}
	

}
