package com.mossle.operation.persistence.domain;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/** 
 * @author  cz 
 * @version 2017年9月15日
 * 自定义申请实体类 
 */
@Entity
@Table(name = "oa_bpm_customForm")
public class CustomEntity implements java.io.Serializable  {
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
	    
	    /**业务级别**/
	    private String businessLevel;
	    
	    //新增  ckx
	    /** 表单类型 1：请假   2：出差  3：加班申请单  4：加班补休  5：漏打卡  */
	    private String formType;
	    /** 类型 */
	    private String type;
	    /** 填表日期  */
	    private String date; 
	    /** 开始时间  */
	    private String startTime;
	    /** 结束时间  */
	    private String endTime;
	    /** 总计时间  */
	    private String totalTime;
	    /** 目的地  */
	    private String destination;
	    /** 同行人id  */
	    private String peerId;
	    /** 同行人姓名  */
	    private String peerName;
	    /** 部门名称  */
	    private String departmentName;
	    
	     
		public CustomEntity() {
	    }

	    public CustomEntity(Long id) {
	        this.id = id;
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
	    

	    /** @return 主题. */
	    @Column(name = "subject")
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
	    @Column(name = "businessType")
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
	    @Column(name = "businessDetail")
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
	    @Column(name = "name")
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
	    
	    /** @return 申请内容. */
	    @Column(name = "content")
	    public String getApplyContent() {
	        return this.applyContent;
	    }

	    /**
	     * @param content
	     *   申请内容.
	     */
	    public void setApplyContent(String applyContent) {
	        this.applyContent = applyContent;
	    } 
	    
	    /** @return  附件的名称. */
	    @Column(name = "fileName")
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
	    @Column(name = "filePath")
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
	    @Column(name = "processInstanceId")
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
	    @Column(name = "createTime" )
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
	    @Column(name = "modifyTime")
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
	    @Column(name = "USERID")
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
	    @Column(name = "submitTimes")
	    public int getSubmitTimes() {
			return submitTimes;
		}

		public void setSubmitTimes(int submitTimes) {
			this.submitTimes = submitTimes;
		}
		
		/** @return 受理单编号. */
	    @Column(name = "applyCode")
	    public String getApplyCode() {
	  		return applyCode;
	  	}

	  	public void setApplyCode(String applyCode) {
	  		this.applyCode = applyCode;
	  	}
	  	
	  	/** @return  抄送人名称. */
	  	 @Column(name = "ccName")
	  	 public String getCcName() {
				return ccName;
		}

		public void setCcName(String ccName) {
			this.ccName = ccName;
		}
		
		/** @return  抄送人id. */
	  	 @Column(name = "ccnos")
		public String getCcnos() {
			return ccnos;
		}

		public void setCcnos(String ccnos) {
			this.ccnos = ccnos;
		}
		
		/** @return 业务级别. */
		@Column(name = "businessLevel")
		public String getBusinessLevel() {
			return businessLevel;
		}
		
		/** @param 业务级别. */
		public void setBusinessLevel(String businessLevel) {
			this.businessLevel = businessLevel;
		}

	
		/** @return 表单申请类型. */
		@Column(name = "formType")
		public String getFormType() {
			return formType;
		}

		public void setFormType(String formType) {
			this.formType = formType;
		}
		/** @return 请假类型. */
		@Column(name = "type")
		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}
		/** @return 填表日期. */
		@Column(name = "date")
		public String getDate() {
			return date;
		}

		public void setDate(String date) {
			this.date = date;
		}
		/** @return 开始时间. */
		@Column(name = "startTime")
		public String getStartTime() {
			return startTime;
		}

		public void setStartTime(String startTime) {
			this.startTime = startTime;
		}
		/** @return 结束时间. */
		@Column(name = "endTime")
		public String getEndTime() {
			return endTime;
		}

		public void setEndTime(String endTime) {
			this.endTime = endTime;
		}
		/** @return 总计时间. */
		@Column(name = "totalTime")
		public String getTotalTime() {
			return totalTime;
		}

		public void setTotalTime(String totalTime) {
			this.totalTime = totalTime;
		}
		/** @return 目的地. */
		@Column(name = "destination")
		public String getDestination() {
			return destination;
		}

		public void setDestination(String destination) {
			this.destination = destination;
		}
		/** @return 同行人id. */
		@Column(name = "peerId")
		public String getPeerId() {
			return peerId;
		}

		public void setPeerId(String peerId) {
			this.peerId = peerId;
		}
		/** @return 同行人姓名. */
		@Column(name = "peerName")
		public String getPeerName() {
			return peerName;
		}

		public void setPeerName(String peerName) {
			this.peerName = peerName;
		}
		/** @return 部门名称. */
		@Column(name = "departmentName")
		public String getDepartmentName() {
			return departmentName;
		}

		

		public void setDepartmentName(String departmentName) {
			this.departmentName = departmentName;
		}


		
		
	     
}
