package com.mossle.base.persistence.domain;

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
 * @version 2017年7月31日
 * 业务类型明细
 */
@Entity
@Table(name = "oa_ba_businesstype")
public class BusinessTypeEntity  implements java.io.Serializable {
	 private static final long serialVersionUID = 0L;

	    /** 主键. */
	    private Long   id;
	    
	    /** 业务类型. */
	    private String businesstype;

	    /** 部门名称. */
	    private String department;
	    
	    /** 部门id. */
	    private String departmentCode;
	    
	    /** 租户ID. */
	    private String tenantId;
	    
	    /** 是否启用. */
	    private String enable;
	    
	    /** 表单名称 */
	    private String formName;
	    /** 表单ID. */
	    private String formid;
	    
	    
	    

	    public BusinessTypeEntity() {
	    }

	    public BusinessTypeEntity(Long id) {
	        this.id = id;
	    }
	    
	    public BusinessTypeEntity(Long id, String businesstype, String department) {
	        this.id = id;
	        this.businesstype = businesstype;
	        this.department = department;
	    }
	    
	    /** @return 主键. */
	    @Id
	    @Column(name = "typeid", unique = true, nullable = false)
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

	    /** @return 业务类型. */
	    @Column(name = "businesstype", length = 100)
	    public String getBusinesstype() {
	        return this.businesstype;
	    }

	    /**
	     * @param reason
	     *          业务类型.
	     */
	    public void setBusinesstype(String businesstype) {
	        this.businesstype = businesstype;
	    }

	    /** @return 部门名称. */
	    @Column(name = "department")
	    public String getDepartment() {
	        return this.department;
	    }

	    /**
	     * @param 
	     *         部门名称.
	     */
	    public void setDepartment(String department) {
	        this.department = department;
	    }
	    
	    
	    /**
	     * @param 
	     *         部门id.
	     */
	    @Column(name = "departmentCode")
	    public String getDepartmentCode() {
			return departmentCode;
		}

		public void setDepartmentCode(String departmentCode) {
			this.departmentCode = departmentCode;
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
