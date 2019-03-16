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
@Table(name = "oa_bpm_customProcess")
public class CustomProcessDTO implements java.io.Serializable  {
	 private static final long serialVersionUID = 0L;
	    
	    /** 主键. */
	    private Long id;
	    /** 当前操作人 */
	    private Long commUserID;
	    /** 指定下一个操作人 */
	    private Long nextUserID;
	    /** 操作 */
	    private String action;
	    /** 审批意见 */
	    private String comment;
	    /** 创建时间 */
	    private String createTime;
	    /** 任务完成时间 */
	    private String completeTime;
	
	    
	    
	    public CustomProcessDTO() {
	    }

	    public CustomProcessDTO(Long id) {
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
	    

	    /** @return 当前操作人. */
	    public Long getCommUserID() {
	        return this.commUserID;
	    }

	    /**
	     * @param 当前操作人
	     *   .
	     */
	    public void setCommUserID(Long commUserID) {
	        this.commUserID = commUserID;
	    }    
	    
	    /** @return 指定下一个操作人 . */
	    public Long getNextUserID() {
	        return this.nextUserID;
	    }

	    /**
	     * @param 
	     *   指定下一个操作人 .
	     */
	    public void setNextUserID(Long nextUserID) {
	        this.nextUserID = nextUserID;
	    }   
	    
	    /** @return 操作. */
	    public String getAction() {
	        return this.action;
	    }

	    /**
	     * @param 
	     *   操作
	     */
	    public void setAction(String action) {
	        this.action = action;
	    } 
	    
	    /** @return  审批意见. */
	    public String getComment() {
	        return this.comment;
	    }

	    /**
	     * @param 
	     *   审批意见.
	     */
	    public void setComment(String comment) {
	        this.comment = comment;
	    } 
	    
	    /** @return 创建时间. */
	    public String getCreateTime() {
	        return this.createTime;
	    }

	    /**
	     * @param 
	     *   创建时间.
	     */
	    public void setCreateTime(String createTime) {
	        this.createTime = createTime;
	    } 
	    

	    /** @return  任务完成时间 . */
	    public String getCompleteTime() {
	        return this.completeTime;
	    }

	    /**
	     * @param  任务完成时间
	     *            .
	     */
	    public void setCompleteTime(String completeTime) {
	        this.completeTime = completeTime;
	    }  
	    

	     
}
