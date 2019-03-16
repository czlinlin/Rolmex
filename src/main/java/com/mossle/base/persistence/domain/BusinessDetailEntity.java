package com.mossle.base.persistence.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/** 
 * @author  cz 
 * @version 2017年7月31日
 * 类说明 
 */

	@Entity
	@Table(name = "oa_ba_business_detail")
	public class BusinessDetailEntity  implements java.io.Serializable{
		 private static final long serialVersionUID = 0L;

		    /** 主键. */
		    private Long   id;
		    
		    /** 业务类型明细. */
		    private String busiDetail;
		    
		    /** 业务类型ID. */
		    private Long typeId;
		    
		    /** 业务类型名称. */
		    private String businessType;
		    
		    private String level;
		    
		    private String standFirst;
		    
		    private String standSecond;
		    
		    /** 租户ID. */
		    private String tenantId;
	
		    /** 流程定义ID. */
		    private String bpmProcessId;
		    
		    /** 该条创建时间 */
		    private String createTime;
		    
		    /** 末次修改时间 */
		    private String modifyTime;
		    /** 是否启用. */
		    private String enable;
		    
		    /** 表单名称 */
		    private String formName;
		    /** 表单ID. */
		    private String formid;
		    /** 标题 */
		    private String title;
		    
		    
		    
		    /** @return 标题. */
		    @Column(name = "title" )
		    public String getTitle() {
				return title;
			}

			public void setTitle(String title) {
				this.title = title;
			}

			public BusinessDetailEntity() {
		    }

		    public BusinessDetailEntity(Long id) {
		        this.id = id;
		    }
		    
		   // public BusinessDetailEntity(Long typeId) {
		   //     this.typeId = typeId;
		  //  }
		    
		    public BusinessDetailEntity(Long typeid, String busiDetail,String level
		    							,String standFirst,String standSecond) {
		        this.typeId = typeid;
		        this.busiDetail = busiDetail;
		        this.level = level;
		        this.standFirst = standFirst;
		        this.standSecond = standSecond;
		     
		    }
		    
		    /** @return 主键. */
		    @Id
		    @Column(name = "id", unique = true, nullable = false)
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
		    /** @return 业务类型明细. */
		    @Column(name = "busi_detail" )
		    public String getBusiDetail() {
				return busiDetail;
			}

			public void setBusiDetail(String busiDetail) {
				this.busiDetail = busiDetail;
			}

		    /** @return 业务类型. */
		    @Column(name = "type_id" )
		    public Long getTypeId() {
		        return this.typeId;
		    }

		    /**
		     * @param reason
		     *          业务类型.
		     */
		    public void setTypeId(Long typeId) {
		        this.typeId = typeId;
		    }
		    
		    /** @return 业务类型名称. */
		    @Column(name = "businesstype" )
		    public String getBusinessType() {
		        return this.businessType;
		    }

		    /**
		     * @param 
		     *          业务类型名称.
		     */
		    public void setBusinessType(String businessType) {
		        this.businessType = businessType;
		    }
		    
		    /** @return 业务类型. */
		    @Column(name = "level", length = 100)
		    public String getLevel() {
		        return this.level;
		    }

		    /**
		     * @param reason
		     *          业务类型.
		     */
		    public void setLevel(String level) {
		        this.level = level;
		    }
		    
		    
		    /** @return 业务标准（现场办理）. */
		    @Column(name = "stand_first", length = 500)
		    public String getStandFirst() {
		        return this.standFirst;
		    }

		    /**
		     * @param reason
		     *          业务标准.
		     */
		    public void setStandFirst(String standFirst) {
		        this.standFirst = standFirst;
		    }
		    
		   /** @return 业务标准（非现场办理）. */
		    @Column(name = "stand_second", length = 500)
		    public String getStandSecond() {
		        return this.standSecond;
		    }

		    /**
		     * @param reason
		     *          业务标准.
		     */
		    public void setStandSecond(String standSecond) {
		        this.standSecond = standSecond;
		    }
		    
		 
		    /** @return 流程定义ID. */
		    @Column(name = "bpmProcessId" )
		    public String getBpmProcessId() {
		        return this.bpmProcessId;
		    }

		    /**
		     * @param 流程定义ID
		     *          
		     */
		    public void setBpmProcessId(String bpmProcessId) {
		        this.bpmProcessId = bpmProcessId;
		    }

		    /** @return 租户ID. */
		    @Column(name = "tenant_id")
		    public String getTenantId() {
		        return this.tenantId;
		    }

		    /**
		     * @param 
		     *          租户ID.
		     */
		    public void setTenantId(String tenantId) {
		        this.tenantId = tenantId;
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
		    
		    /** @return 是否启用. */
		    @Column(name = "enable")
		    public String getEnable() {
		        return this.enable;
		    }

		    /**
		     * @param 
		     *         是否启用.
		     */
		    public void setEnable(String enable) {
		        this.enable = enable;
		    }
		    
		    
		    /**
		     * @param 
		     *         表单名称.
		     */
		    @Column(name = "formName")
		    public String getFormName() {
				return formName;
			}

			public void setFormName(String formName) {
				this.formName = formName;
			}
			
			/**
		     * @param 
		     *         表单ID.
		     */
			@Column(name = "formid")
			public String getFormid() {
				return formid;
			}

			public void setFormid(String formid) {
				this.formid = formid;
			}
	
}
