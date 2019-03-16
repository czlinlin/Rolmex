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
@Table(name = "oa_bpm_customPre")
public class CustomPre implements java.io.Serializable  {
	 private static final long serialVersionUID = 0L;
	    
	    /** 主键. */
	    private Long id;
	    /** 当前节点ID */
	    private String assignee;
	    /** 上一节点ID */
	    private String previous;
	    /** 表单ID */
	    private String formID;
	    /** 该条记录生成的时间 CREATETIME*/
	    private Date createTime;
	 
	    public CustomPre() {
	    }

	    public CustomPre(Long id) {
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
	    

	    
	    /** @return 当前节点ID. */
	    @Column(name = "ASSIGNEE")
	    public String getAssignee() {
	        return this.assignee;
	    }

	    /**
	     * @param 
	     *   当前节点ID
	     */
	    public void setAssignee(String assignee) {
	        this.assignee = assignee;
	    } 
	    
	    /** @return  上一节点ID. */
	    @Column(name = "PREVIOUS")
	    public String getPrevious() {
	        return this.previous;
	    }

	    /**
	     * @param 
	     *   上一节点ID.
	     */
	    public void setPrevious(String previous) {
	        this.previous = previous;
	    } 
	    
	    /** @return 表单ID . */
	    @Column(name = "FORMID")
	    public String getFormID() {
	        return this.formID;
	    }

	    /**
	     * @param 
	     *   表单ID .
	     */
	    public void setFormID(String formID) {
	        this.formID = formID;
	    } 
	    
	    
	    /** @return 该条记录生成的时间 . */
	    @Column(name = "CREATETIME")
	    public Date getCreateTime() {
	        return this.createTime;
	    }

	    /**
	     * @param 
	     *   该条记录生成的时间 
	     */
	    public void setCreateTime(Date createTime) {
	        this.createTime = createTime;
	    } 
	    
     
}
