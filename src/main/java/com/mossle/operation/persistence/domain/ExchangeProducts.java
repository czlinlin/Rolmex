package com.mossle.operation.persistence.domain;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "ENTITY_EXCHANGE_PRODUCT")
public class ExchangeProducts   implements java.io.Serializable  {
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
	
	public ExchangeProducts(){
		
	}
	public ExchangeProducts( Long id){
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
	@Column(name="exchange_id")
	public String getExchangeId() {
		return exchangeId;
	}
	public void setExchangeId(String exchangeId) {
		this.exchangeId = exchangeId;
	}
	@Column(name="product_no")
	public String getProductNo() {
		return productNo;
	}
	public void setProductNo(String productNo) {
		this.productNo = productNo;
	}
	@Column(name="product_name")
	public String getProductName() {
		return productName;
	}
	public void setProductName(String productName) {
		this.productName = productName;
	}
	@Column(name="max_product_num")
	public String getMaxProductNum() {
		return maxProductNum;
	}
	public void setMaxProductNum(String maxProductNum) {
		this.maxProductNum = maxProductNum;
	}
	@Column(name="product_num")
	public String getProductNum() {
		return productNum;
	}
	public void setProductNum(String productNum) {
		this.productNum = productNum;
	}
	@Column(name="pv")
	public String getPv() {
		return pv;
	}
	public void setPv(String pv) {
		this.pv = pv;
	}
	@Column(name="total_pv")
	public String getTotalPv() {
		return totalPv;
	}
	public void setTotalPv(String totalPv) {
		this.totalPv = totalPv;
	}
	@Column(name="price")
	public String getPrice() {
		return price;
	}
	public void setPrice(String price) {
		this.price = price;
	}
	@Column(name="total_price")
	public String getTotalPrice() {
		return totalPrice;
	}
	public void setTotalPrice(String totalPrice) {
		this.totalPrice = totalPrice;
	}
	@Column(name="type")
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	@Column(name="production_date")
	public String getProductionDate() {
		return productionDate;
	}
	public void setProductionDate(String productionDate) {
		this.productionDate = productionDate;
	}
	@Column(name="quality_assurance_date")
	public String getQualityAssuranceDate() {
		return QualityAssuranceDate;
	}
	public void setQualityAssuranceDate(String qualityAssuranceDate) {
		QualityAssuranceDate = qualityAssuranceDate;
	}
	
}
