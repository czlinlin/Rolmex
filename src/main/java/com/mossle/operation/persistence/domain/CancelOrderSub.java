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
 * @version 2017年9月5日
 * 撤单 子表  
 */
@Entity
@Table(name = "oa_bpm_cancel_order_sub")
public class CancelOrderSub  implements java.io.Serializable {

	private static final long serialVersionUID = 0L;
	    
	    /** 主键. */
	    private Long id;
	    /** 撤单编号 */
	    private String ucode;
	    /** 撤单姓名 */
	    private String userName;
	    /** 编号加入日期 */
	    private String addTime;
	    /** 撤单类型 */
	    private String cancelType;
	    /** 撤单金额 */
	    private String cancelMoney;
	    /** 业绩单号*/
	    private String saleID;
	    /** 主表的ID   外键 */
	    private Long cancelOrderID;
	   
	    
	    public CancelOrderSub() {
	    }

	    public CancelOrderSub(Long id) {
	        this.id = id;
	    }
	    
	    public CancelOrderSub(Long id, String cancelType, String cancelMoney,Long cancelOrderID
    						,String ucode,String userName,String addTime) {
    	this.id = id;
    	this.ucode = ucode;
    	this.cancelType = cancelType;      
        this.cancelOrderID = cancelOrderID;   
        this.cancelMoney = cancelMoney;   
        this.ucode = ucode;
        this.userName = userName;
        this.addTime = addTime;
    }
	    
	    
	    /** @return 主键. */
	    @Id
	    @Column(name = "id" , unique = true, nullable = false)
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
	    

	    /** @return 撤单类型. */
	    @Column(name = "cancelType")
	    public String getCancelType() {
	        return this.cancelType;
	    }

	    /**
	     * @param content
	     *   撤单类型.
	     */
	    public void setCancelType(String cancelType) {
	        this.cancelType = cancelType;
	    }    
	    
	    /** @return 撤单金额. */
	    @Column(name = "cancelMoney")
	    public String getCancelMoney() {
	        return this.cancelMoney;
	    }

	    /**
	     * @param 
	     *   撤单金额.
	     */
	    public void setCancelMoney(String cancelMoney) {
	        this.cancelMoney = cancelMoney;
	    }   
	    
	    /** @return 业绩单号. */
	    @Column(name = "saleID")
	    public String getSaleID() {
	        return this.saleID;
	    }

	    /**
	     * @param 
	     *    业绩单号.
	     */
	    public void setSaleID(String saleID) {
	        this.saleID = saleID;
	    } 
	    
	    /** @return  主表ID. */
	    @Column(name = "cancelOrderID")
	    public Long getCancelOrderID() {
	        return this.cancelOrderID;
	    }

	    /**
	     * @param 
	     *   主表ID.
	     */
	    public void setCancelOrderID(Long cancelOrderID) {
	        this.cancelOrderID = cancelOrderID;
	    } 
	    
	 
	    
	    /** @return  撤单编号. */
	    @Column(name = "ucode")
	    public String getUcode() {
	        return this.ucode;
	    }

	    /**
	     * @param 
	     *   撤单编号.
	     */
	    public void setUcode(String ucode) {
	        this.ucode = ucode;
	    } 
	    
	    /** @return  撤单姓名. */
	    @Column(name = "userName")
	    public String getUserName() {
	        return this.userName;
	    }

	    /**
	     * @param content
	     *   撤单姓名.
	     */
	    public void setUserName(String userName) {
	        this.userName = userName;
	    } 
	    
	    /** @return  编号加入日期. */
	    @Column(name = "addTime")
	    public String getAddTime() {
	        return this.addTime;
	    }

	    /**
	     * @param content
	     *  编号加入日期.
	     */
	    public void setAddTime(String addTime) {
	        this.addTime = addTime;
	    } 
	    

	   
	    
}
