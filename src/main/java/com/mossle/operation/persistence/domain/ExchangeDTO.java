package com.mossle.operation.persistence.domain;

import java.util.List;

public class ExchangeDTO {
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
	List<ExchangeProducts> exchangeProductSub;
	
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
    
	
	public ExchangeDTO(){
		
	}
	public ExchangeDTO( Long id){
		this.id = id;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getApplyCode() {
		return applyCode;
	}
	public void setApplyCode(String applyCode) {
		this.applyCode = applyCode;
	}
	public String getWareHouse() {
		return wareHouse;
	}
	public void setWareHouse(String wareHouse) {
		this.wareHouse = wareHouse;
	}
	public String getExchangeDate() {
		return exchangeDate;
	}
	public void setExchangeDate(String exchangeDate) {
		this.exchangeDate = exchangeDate;
	}
	public String getEmpNo() {
		return empNo;
	}
	public void setEmpNo(String empNo) {
		this.empNo = empNo;
	}
	public String getUcode() {
		return ucode;
	}
	public void setUcode(String ucode) {
		this.ucode = ucode;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getTel() {
		return tel;
	}
	public void setTel(String tel) {
		this.tel = tel;
	}
	public String getOrderNumber() {
		return orderNumber;
	}
	public void setOrderNumber(String orderNumber) {
		this.orderNumber = orderNumber;
	}
	public String getOrderTime() {
		return orderTime;
	}
	public void setOrderTime(String orderTime) {
		this.orderTime = orderTime;
	}
	public String getPayType() {
		return payType;
	}
	public void setPayType(String payType) {
		this.payType = payType;
	}
	public String getOldConsignee() {
		return oldConsignee;
	}
	public void setOldConsignee(String oldConsignee) {
		this.oldConsignee = oldConsignee;
	}
	public String getOldConsigneeTel() {
		return oldConsigneeTel;
	}
	public void setOldConsigneeTel(String oldConsigneeTel) {
		this.oldConsigneeTel = oldConsigneeTel;
	}
	public String getOldConsigneeAddress() {
		return oldConsigneeAddress;
	}
	public void setOldConsigneeAddress(String oldConsigneeAddress) {
		this.oldConsigneeAddress = oldConsigneeAddress;
	}
	public String getZipCode() {
		return zipCode;
	}
	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
	}
	public String getExchangeReason() {
		return exchangeReason;
	}
	public void setExchangeReason(String exchangeReason) {
		this.exchangeReason = exchangeReason;
	}
	public String getNewConsigneeAddress() {
		return newConsigneeAddress;
	}
	public void setNewConsigneeAddress(String newConsigneeAddress) {
		this.newConsigneeAddress = newConsigneeAddress;
	}
	public String getNewConsignee() {
		return newConsignee;
	}
	public void setNewConsignee(String newConsignee) {
		this.newConsignee = newConsignee;
	}
	public String getNewConsigneeTel() {
		return newConsigneeTel;
	}
	public void setNewConsigneeTel(String newConsigneeTel) {
		this.newConsigneeTel = newConsigneeTel;
	}
	public String getProcessInstanceId() {
		return processInstanceId;
	}
	public void setProcessInstanceId(String processInstanceId) {
		this.processInstanceId = processInstanceId;
	}
	public String getInputApplyCode() {
		return inputApplyCode;
	}
	public void setInputApplyCode(String inputApplyCode) {
		this.inputApplyCode = inputApplyCode;
	}
	

    /** @return 福利级别. */
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
    public String getArea() {
        return this.area;
    }

    /**
     * @param 所属大区
     */
    public void setArea(String area) {
        this.area = area;
    }
    
    public void setExchangeProductSub(List<ExchangeProducts> exchangeProductSub) {
		this.exchangeProductSub = exchangeProductSub;
	}
	
}
