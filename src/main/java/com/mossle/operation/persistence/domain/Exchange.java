package com.mossle.operation.persistence.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "ENTITY_EXCHANGE")
public class Exchange extends CommonDTO{
	private Long id;
	private String applyCode;
	private String wareHouse;
	private String exchangeDate;
	private String empNo;
	private String ucode;
	private String name;
	private String tel;
	private String orderNumber;
	private String orderTime;
	private String payType;
	private String oldConsignee;
	private String oldConsigneeTel;
	private String oldConsigneeAddress;
	private String zipCode;
	private String exchangeReason;
	private String newConsigneeAddress;
	private String newConsignee;
	private String newConsigneeTel;
	private String processInstanceId;
	private String inputApplyCode;
	
	
	//20181116 chengze 质量问题换货
	 /** 福利级别 */
    private String  welfare  ;
    /** 级别 */
    private String  level  ;
    /** 所属体系 */
    private String  system  ;
    /** 销售人 */
    private String  varFather  ;
    /** 服务人 */
    private String  varRe  ;
    /** 注册时间 */
    private String  addTime  ;
    /** 申请业务类型 */
    private String  businessType  ;
    /** 业务细分 */
    private String  businessDetail  ;

    /** 地址 */
    private String  address  ;
    /** 业务级别 */
    private String  businessLevel  ;
    /** 所属大区 */
    private String  area  ;
    
	public Exchange(){
		
	}
	public Exchange( Long id){
		this.id = id;
	}
	@Id
    @Column(name = "id")
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	@Column(name="apply_code")
	public String getApplyCode() {
		return applyCode;
	}
	public void setApplyCode(String applyCode) {
		this.applyCode = applyCode;
	}
	@Column(name="ware_house")
	public String getWareHouse() {
		return wareHouse;
	}
	public void setWareHouse(String wareHouse) {
		this.wareHouse = wareHouse;
	}
	@Column(name="apply_exchange_date")
	public String getExchangeDate() {
		return exchangeDate;
	}
	public void setExchangeDate(String exchangeDate) {
		this.exchangeDate = exchangeDate;
	}
	@Column(name="emp_no")
	public String getEmpNo() {
		return empNo;
	}
	public void setEmpNo(String empNo) {
		this.empNo = empNo;
	}
	@Column(name = "ucode")
	public String getUcode() {
		return ucode;
	}
	public void setUcode(String ucode) {
		this.ucode = ucode;
	}
	@Column(name="name")
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	@Column(name="tel")
	public String getTel() {
		return tel;
	}
	public void setTel(String tel) {
		this.tel = tel;
	}
	@Column(name="order_number")
	public String getOrderNumber() {
		return orderNumber;
	}
	public void setOrderNumber(String orderNumber) {
		this.orderNumber = orderNumber;
	}
	@Column(name="order_time")
	public String getOrderTime() {
		return orderTime;
	}
	public void setOrderTime(String orderTime) {
		this.orderTime = orderTime;
	}
	@Column(name="pay_type")
	public String getPayType() {
		return payType;
	}
	public void setPayType(String payType) {
		this.payType = payType;
	}
	@Column(name="old_consignee")
	public String getOldConsignee() {
		return oldConsignee;
	}
	public void setOldConsignee(String oldConsignee) {
		this.oldConsignee = oldConsignee;
	}
	@Column(name="old_consignee_tel")
	public String getOldConsigneeTel() {
		return oldConsigneeTel;
	}
	public void setOldConsigneeTel(String oldConsigneeTel) {
		this.oldConsigneeTel = oldConsigneeTel;
	}
	@Column(name="old_consignee_address")
	public String getOldConsigneeAddress() {
		return oldConsigneeAddress;
	}
	public void setOldConsigneeAddress(String oldConsigneeAddress) {
		this.oldConsigneeAddress = oldConsigneeAddress;
	}
	@Column(name="zip_code")
	public String getZipCode() {
		return zipCode;
	}
	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
	}
	@Column(name="exchange_reason")
	public String getExchangeReason() {
		return exchangeReason;
	}
	public void setExchangeReason(String exchangeReason) {
		this.exchangeReason = exchangeReason;
	}
	@Column(name="new_consignee_address")
	public String getNewConsigneeAddress() {
		return newConsigneeAddress;
	}
	public void setNewConsigneeAddress(String newConsigneeAddress) {
		this.newConsigneeAddress = newConsigneeAddress;
	}
	@Column(name="new_consignee")
	public String getNewConsignee() {
		return newConsignee;
	}
	public void setNewConsignee(String newConsignee) {
		this.newConsignee = newConsignee;
	}
	@Column(name="new_consignee_tel")
	public String getNewConsigneeTel() {
		return newConsigneeTel;
	}
	public void setNewConsigneeTel(String newConsigneeTel) {
		this.newConsigneeTel = newConsigneeTel;
	}
	@Column(name="process_instance_id")
	public String getProcessInstanceId() {
		return processInstanceId;
	}
	public void setProcessInstanceId(String processInstanceId) {
		this.processInstanceId = processInstanceId;
	}
	@Column(name="input_apply_code")
	public String getInputApplyCode() {
		return inputApplyCode;
	}
	public void setInputApplyCode(String inputApplyCode) {
		this.inputApplyCode = inputApplyCode;
	}
	
	 /** @return 福利级别. */
    @Column(name = "welfare", length = 10)
    public String getWelfare() {
        return this.welfare;
    }

    /**
     * @param 
     *            福利级别.
     */
    public void setWelfare(String welfare) {
        this.welfare = welfare;
    }
    
    /** @return 级别. */
    @Column(name = "level", length = 10)
    public String getLevel() {
        return this.level;
    }

    /**
     * @param 
     *            级别.
     */
    public void setLevel(String level) {
        this.level = level;
    }
    
    /** @return 所属体系 */
    @Column(name = "system", length = 45)
    public String getSystem() {
        return this.system;
    }

    /**
     * @param 所属体系
     */
    public void setSystem(String system) {
        this.system = system;
    }
    
    
    /** @return 销售人 */
    @Column(name = "varFather", length = 100)
    public String getVarFather() {
        return this.varFather;
    }

    /**
     * @param 销售人
     */
    public void setVarFather(String varFather) {
        this.varFather = varFather;
    }
    
    /** @return 服务人 */
    @Column(name = "varRe", length = 100)
    public String getVarRe() {
        return this.varRe;
    }

    /**
     * @param 服务人
     */
    public void setVarRe(String varRe) {
        this.varRe = varRe;
    }
    
    /** @return 注册时间 */
    @Column(name = "addTime" )
    public String getAddTime() {
        return this.addTime;
    }

    /**
     * @param 注册时间
     */
    public void setAddTime(String addTime) {
        this.addTime = addTime;
    }
    
    
    /** @return 申请业务类型 */
    @Column(name = "businessType" )
    public String getBusinessType() {
        return this.businessType;
    }

    /**
     * @param 申请业务类型
     */
    public void setBusinessType(String businessType) {
        this.businessType = businessType;
    }
    
    /** @return 业务细分 */
    @Column(name = "businessDetail" )
    public String getBusinessDetail() {
        return this.businessDetail;
    }

    /**
     * @param 业务细分
     */
    public void setBusinessDetail(String businessDetail) {
        this.businessDetail = businessDetail;
    }
       
    /** @return 地址 */
    @Column(name = "address" )
    public String getAddress() {
        return this.address;
    }

    /**
     * @param 地址
     */
    public void setAddress(String address) {
        this.address = address;
    }
    
    /** @return 业务级别 */
    @Column(name = "businessLevel" )
    public String getBusinessLevel() {
        return this.businessLevel;
    }

    /**
     * @param 业务级别
     */
    public void setBusinessLevel(String businessLevel) {
        this.businessLevel = businessLevel;
    }
    
    /** @return 所属大区 */
    @Column(name = "area" )
    public String getArea() {
        return this.area;
    }

    /**
     * @param 所属大区
     */
    public void setArea(String area) {
        this.area = area;
    }
    
	
}
