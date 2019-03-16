package com.mossle.operation.persistence.domain;

public class ExchangeProductsDTO {
	private Long id;
	private String exchangeId;
	private String productNo;
	private String productName;
	private String maxProductNum;
	private String productNum;
	private String pv;
	private String totalPv;
	private String price;
	private String totalPrice;
	private String type;
	private String productionDate;
	private String QualityAssuranceDate;
	
	public ExchangeProductsDTO(){
		
	}
	public ExchangeProductsDTO( Long id){
		this.id = id;
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	public String getExchangeId() {
		return exchangeId;
	}
	public void setExchangeId(String exchangeId) {
		this.exchangeId = exchangeId;
	}
	public String getProductNo() {
		return productNo;
	}
	public void setProductNo(String productNo) {
		this.productNo = productNo;
	}
	public String getProductName() {
		return productName;
	}
	public void setProductName(String productName) {
		this.productName = productName;
	}
	
	public String getMaxProductNum() {
		return maxProductNum;
	}
	public void setMaxProductNum(String maxProductNum) {
		this.maxProductNum = maxProductNum;
	}
	public String getProductNum() {
		return productNum;
	}
	public void setProductNum(String productNum) {
		this.productNum = productNum;
	}
	public String getPv() {
		return pv;
	}
	public void setPv(String pv) {
		this.pv = pv;
	}
	public String getTotalPv() {
		return totalPv;
	}
	public void setTotalPv(String totalPv) {
		this.totalPv = totalPv;
	}
	
	public String getPrice() {
		return price;
	}
	public void setPrice(String price) {
		this.price = price;
	}
	public String getTotalPrice() {
		return totalPrice;
	}
	public void setTotalPrice(String totalPrice) {
		this.totalPrice = totalPrice;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getProductionDate() {
		return productionDate;
	}
	public void setProductionDate(String productionDate) {
		this.productionDate = productionDate;
	}
	public String getQualityAssuranceDate() {
		return QualityAssuranceDate;
	}
	public void setQualityAssuranceDate(String qualityAssuranceDate) {
		QualityAssuranceDate = qualityAssuranceDate;
	}
	
}
