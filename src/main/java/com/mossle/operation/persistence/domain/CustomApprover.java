package com.mossle.operation.persistence.domain;

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
 * @author  lilei
 * @version 2017.11.27
 * 自定义申请审批步骤实体
 */
@Entity
@Table(name = "custom_approver")
public class CustomApprover implements java.io.Serializable  {
	 private static final long serialVersionUID = 0L;
	    
	    /** 主键. */
	    private Long id;
	    
	    /**自定义表单ID**/
	    private Long customId;
	    
	    /**业务ID**/
	    private String businessKey;
	    
	    /**审批人ID**/
	    private Long approverId;
	    
	    /**操作类型（0:未操作，1:已操作，2:已取消，3:无需操作结束）**/
	    private String opterType;
	    
	    /**审批步骤**/
	    private int approveStep;
	    
	    /**审核批示**/
	    private String auditComment;

	    
	    public CustomApprover(){
	    	
	    }
	    
	    /** @return 主键ID. */
	    @Id
	    @Column(name = "id", unique = true, nullable = false)
		public Long getId() {
			return id;
		}
	    
	    /**
	     * @param 主键ID
	     * **/
		public void setId(Long id) {
			this.id = id;
		}
		
		/** 
		 * @return 自定义表单ID. 
		 * **/
		@Column(name = "customId")
		public Long getCustomId() {
			return customId;
		}

		/** 
		 * @param 自定义表单ID. 
		 * **/
		public void setCustomId(Long customId) {
			this.customId = customId;
		}
		
		/** 
		 * @return 业务ID. 
		 * **/
		@Column(name = "business_Key")
		public String getBusinessKey() {
			return businessKey;
		}
		
		/** 
		 * @param 业务ID. 
		 * **/
		public void setBusinessKey(String businessKey) {
			this.businessKey = businessKey;
		}
		
		/** 
		 * @return 审批人ID. 
		 * **/
		@Column(name = "approverId")
		public Long getApproverId() {
			return approverId;
		}
		
		/** 
		 * @param 审批人ID. 
		 * **/
		public void setApproverId(Long approverId) {
			this.approverId = approverId;
		}
		
		/** 
		 * @return 操作类型（0:未操作，1:已操作，2:已取消，3:无需操作结束）. 
		 * **/
		@Column(name = "opterType")
		public String getOpterType() {
			return opterType;
		}
		
		/** 
		 * @param 操作类型（0:未操作，1:已操作，2:已取消，3:无需操作结束）. 
		 * **/
		public void setOpterType(String opterType) {
			this.opterType = opterType;
		}
		
		/** 
		 * @return 审批步骤. 
		 * **/
		@Column(name = "approveStep")
		public int getApproveStep() {
			return approveStep;
		}
		
		/** 
		 * @param 审批步骤. 
		 * **/
		public void setApproveStep(int approveStep) {
			this.approveStep = approveStep;
		}
		
		/** 
		 * @return 审批批示. 
		 * **/
		@Column(name = "auditComment")
		public String getAuditComment() {
			return auditComment;
		}
		
		/** 
		 * @param 审批批示. 
		 * **/
		public void setAuditComment(String auditComment) {
			this.auditComment = auditComment;
		}
	    
	    
}
