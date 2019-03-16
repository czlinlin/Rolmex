package com.mossle.operation.persistence.domain;

// Generated by Hibernate Tools
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
 * 退货信息实体类.
 * 
 * @author sjx
 */
@Entity
@Table(name = "ENTITY_RETURN")
public class Return implements java.io.Serializable {
	
    private static final long serialVersionUID = 0L;

    /** 主键. */
    private Long id;
    
    /** 仓库地址 */
    private String wareHouse;

    /** 客服工号 */
    private String empNo;
    /** 专卖店编号. */
    private String ucode;

    /** 专卖店姓名. */
    private String shopName;

    /** 专卖店电话. */
    private String shopTel;

    /** 申请退货日期. */
    private Date returnDate;

    /** 订单单据号. */
    private String orderNumber;

    /** 退货原因. */
    private String returnReaon;
    
    /** 店支付库存. */
    private String shopPayStock;
    
    /** 奖励积分库存. */
    private String rewardIntegralStock;
    
    /** 个人钱包库存. */
    private String personPayStock;
    
    /** 钱包50元. */
    private String payType;
    
    
    /** 申请人. */
    private Long userId;
    
    /** 流程实例Id. */
    private String processInstanceId;
    /** 提交次数 */
    private String submitTimes;
    
    /** 减免受理单号*/
    private String inputApplyCode;
    /** 开户行*/
    private String bankDeposit;
    /** 开户名*/
    private String accountName;
    /** 开户账号*/
    private String accountNumber;
    
    
    
    @Column(name = "input_apply_code")
    public String getInputApplyCode() {
		return inputApplyCode;
	}

	public void setInputApplyCode(String inputApplyCode) {
		this.inputApplyCode = inputApplyCode;
	}
	@Column(name = "bank_deposit")
	public String getBankDeposit() {
		return bankDeposit;
	}

	public void setBankDeposit(String bankDeposit) {
		this.bankDeposit = bankDeposit;
	}
	@Column(name = "account_name")
	public String getAccountName() {
		return accountName;
	}

	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}
	@Column(name = "account_number")
	public String getAccountNumber() {
		return accountNumber;
	}

	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	@Column(name = "submit_times")
    public String getSubmitTimes() {
		return submitTimes;
	}

	public void setSubmitTimes(String submitTimes) {
		this.submitTimes = submitTimes;
	}

	public Return() {
    }

    public Return(Long id) {
        this.id = id;
    }

    
    public Return(Long id,String wareHouse,String empNo, String ucode, String shopName,String payType,
    		Date returnDate, String shopTel, String orderNumber, String returnReaon,String shopPayStock,String rewardIntegralStock,String personPayStock,
    		Long userId, String processInstanceId,String submitTimes) {
        this.id = id;
        this.wareHouse = wareHouse;
        this.empNo = empNo;
        this.ucode = ucode;
        this.shopName = shopName;
        this.returnDate = returnDate;
        this.shopTel = shopTel;
        this.orderNumber = orderNumber;
        this.returnReaon = returnReaon;
        this.shopPayStock = shopPayStock;
        this.rewardIntegralStock = rewardIntegralStock;
        this.personPayStock = personPayStock;
        this.payType = payType;
        this.userId = userId;
        this.processInstanceId = processInstanceId;
        this.submitTimes = submitTimes;
    }

    /** @return 主键. */
    @Id
    @Column(name = "id")
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
    /** 仓库地址 */
    @Column(name = "ware_house")
    public String getWareHouse(){
    	return this.wareHouse;
    }
    
    public void setWareHouse(String wareHouse){
    	this.wareHouse = wareHouse;
    }
    /** 客服工号 */
    @Column( name = "emp_no")
    public String getEmpNo() {
		return empNo;
	}

	public void setEmpNo(String empNo) {
		this.empNo = empNo;
	}

	/** @return 专卖店编号. */
    @Column(name = "ucode")
    public String getUcode() {
        return this.ucode;
    }

    /**
     * @param ucode
     *            专卖店编号.
     */
    public void setUcode(String ucode) {
        this.ucode = ucode;
    }
    
    /** @return 专卖店姓名. */
    @Column(name = "shop_name")
    public String getShopName() {
        return this.shopName;
    }

    /**
     * @param shopName
     *            专卖店姓名.
     */
    public void setShopName(String shopName) {
        this.shopName = shopName;
    }
    
    /** @return 专卖店电话. */
    @Column(name = "shop_tel")
    public String getShopTel() {
        return this.shopTel;
    }

    /**
     * @param shopTel
     *            专卖店电话.
     */
    public void setShopTel(String shopTel) {
        this.shopTel = shopTel;
    }

    /** @return 申请退货时间. */
    @Column(name = "return_date")
    public Date getReturnDate() {
        return this.returnDate;
    }

    /**
     * @param returnDate
     *            申请退货时间.
     */
    public void setReturnDate(Date returnDate) {
        this.returnDate = returnDate;
    }

    /** @return 订单单据号. */
    @Column(name = "order_number")
    public String getOrderNumber() {
        return this.orderNumber;
    }

    /**
     * @param orderNumber
     *            订单单据号.
     */
    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    /** @return 退货原因. */
    @Column(name = "return_reaon")
    public String getReturnReaon() {
    	return this.returnReaon;
    }
    
    /**
     * @param returnReaon
     *            退货原因.
     */
    public void setReturnReaon(String returnReaon) {
    	this.returnReaon = returnReaon;
    }
    /** @return 店支付库存. */
    @Column(name = "shop_pay_stock")
    public String getShopPayStock() {
        return this.shopPayStock;
    }

    /**
     * @param shopPayStock
     *            店支付库存.
     */
    public void setShopPayStock(String shopPayStock) {
        this.shopPayStock = shopPayStock;
    }
    
    /** @return 奖励积分库存. */
    @Column(name = "reward_integral_stock")
    public String getRewardIntegralStock() {
    	return this.rewardIntegralStock;
    }
    
    /**
     * @param rewardIntegralStock
     *            奖励积分库存.
     */
    public void setRewardIntegralStock(String rewardIntegralStock) {
    	this.rewardIntegralStock = rewardIntegralStock;
    }
    
    /** @return 个人钱包库存. */
    @Column(name = "person_pay_stock")
    public String getPersonPayStock() {
    	return this.personPayStock;
    }
    
    /**
     * @param personPayStock
     *            个人钱包库存.
     */
    public void setPersonPayStock(String personPayStock) {
    	this.personPayStock = personPayStock;
    }
    
   
    /** @return 钱包50. */
    @Column(name = "pay_type")
    public String getPayType() {
    	return this.payType;
    }
    
    /**
     * @param payType
     *            钱包50.
     */
    public void setPayType(String payType) {
    	this.payType = payType;
    }
    
    
    /** @return 申请人. */
    @Column(name = "user_id")
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
    @Column(name = "process_instance_id")
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
}
