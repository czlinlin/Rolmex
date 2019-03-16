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
 * @version 2017年8月31日
 * 撤单 实体类 
 */
@Entity
@Table(name = "oa_bpm_cancel_order_total")
public class CancelOrder implements java.io.Serializable  {
	 private static final long serialVersionUID = 0L;
	    
	    /** 主键. */
	    private Long id;
	    /** 店编号 */
	    private String ucode;
	    /** 店姓名. */
	    private String shopName; 
	    /** 店电话 */
	    private String shopMobile; 
	    /** 来电电话 */
	    private String mobile;
	    /** 撤单登记时间 */
	    private String registerTime;
	    /** 登记人 */
	    private String registerName;
	    /** 是否核实 */
	    private String isChecked;
	    /** 撤单备注 */
	    private String cancelRemark;
	    /** 流程实例ID */
	    private String processInstanceId;
	    /** 当前登录人ID */
	    private Long userid;
	    /** 该条申请创建时间 */
	    private String createTime;
	    /** 该条申请修改时间 */
	    private String modifyTime;
	    /** 该撤销申请包含几条撤单编号 */
	    private int  hidTotal;
	    /** 提交次数 */
	    private int submitTimes;
	    /** 受理单编号 */
	    private String applyCode;
	    
	    public CancelOrder() {
	    }

	    public CancelOrder(Long id) {
	        this.id = id;
	    }
	    
	    public CancelOrder(Long id, String ucode, String shopMobile,String mobile,String registerTime
    						,Long   userid) {
    	this.id = id;
    	this.ucode = ucode;
    	this.shopMobile = shopMobile;    	
        this.userid = userid;    
        this.mobile = mobile;   
        this.registerTime = registerTime;  
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
	    

	    /** @return 店编号. */
	    @Column(name = "shopCode")
	    public String getUcode() {
	        return this.ucode;
	    }

	    /**
	     * @param content
	     *   店编号.
	     */
	    public void setUcode(String ucode) {
	        this.ucode = ucode;
	    }    
	    
	    /** @return 店姓名. */
	    @Column(name = "shopName")
	    public String getShopName() {
	        return this.shopName;
	    }

	    /**
	     * @param 
	     *   店姓名.
	     */
	    public void setShopName(String shopName) {
	        this.shopName = shopName;
	    }   
	    
	    /** @return 店电话. */
	    @Column(name = "shopMobile")
	    public String getShopMobile() {
	        return this.shopMobile;
	    }

	    /**
	     * @param 
	     *   店电话.
	     */
	    public void setShopMobile(String shopMobile) {
	        this.shopMobile = shopMobile;
	    } 
	    
	    /** @return  来电电话. */
	    @Column(name = "mobile")
	    public String getMobile() {
	        return this.mobile;
	    }

	    /**
	     * @param 
	     *   来电电话.
	     */
	    public void setMobile(String mobile) {
	        this.mobile = mobile;
	    } 
	    
	    /** @return  撤单登记时间. */
	    @Column(name = "registerTime")
	    public String getRegisterTime() {
	        return this.registerTime;
	    }

	    /**
	     * @param content
	     *   撤单登记时间.
	     */
	    public void setRegisterTime(String registerTime) {
	        this.registerTime = registerTime;
	    } 
	    
	    /** @return  登记人. */
	    @Column(name = "registerName")
	    public String getRegisterName() {
	        return this.registerName;
	    }

	    /**
	     * @param 
	     *   登记人.
	     */
	    public void setRegisterName(String registerName) {
	        this.registerName = registerName;
	    } 
	    
	    /** @return  是否核实. */
	    @Column(name = "isChecked")
	    public String getIsChecked() {
	        return this.isChecked;
	    }

	    /**
	     * @param content
	     *  是否核实.
	     */
	    public void setIsChecked(String isChecked) {
	        this.isChecked = isChecked;
	    } 
	    
	    /** @return  撤单备注. */
	    @Column(name = "cancelRemark")
	    public String getCancelRemark() {
	        return this.cancelRemark;
	    }

	    /**
	     * @param content
	     *  撤单备注.
	     */
	    public void setCancelRemark(String cancelRemark) {
	        this.cancelRemark = cancelRemark;
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
	    
	    /** @return 该撤销申请包含几条撤单编号. */
	    @Column(name = "hidTotal")
	    public int getHidTotal() {
	        return this.hidTotal;
	    }
	    /**
	     * @param 
	     *            .
	     */
	    public void setHidTotal(int hidTotal) {
	        this.hidTotal = hidTotal;
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
	    
}
