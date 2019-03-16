package com.mossle.user.persistence.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "person_salary_accumulation_fund")
public class PersonSalaryAccumulationFund implements java.io.Serializable {
    private static final long serialVersionUID = 0L;
    
    private Long id; //id
	private String personName; //姓名
	private String personId;//用户id
	private String accumulationFundDate;//公积金日期
	private String accumulationFundYear;//公积金日期年
	private String accumulationFundMonth;//公积金日期月
	private String contractCompanyId;//单位id
	private String contractCompanyName;//单位名称
	private String idcardNum;//身份证号
	private String accumulationFundBaseMoney;//公积金缴存基数
	private String accumulationFundCompanyProportion;//公积金缴存公司比例
	private String accumulationFundCompanyMoney;//公积金缴存公司金额
	private String accumulationFundPersonalProportion;//公积金缴存个人比例
	private String accumulationFundPersonalMoney;//公积金缴存个人金额
	private String totalMoney;//合计
	private String idcardNumColor = "0";//身份证号（0：不变色 1：变色）
	private String accumulationFundBaseMoneyColor = "0";//公积金缴存基数（0：不变色 1：变色）
	private String accumulationFundCompanyProportionColor = "0";//公积金缴存公司比例（0：不变色 1：变色）
	private String accumulationFundCompanyMoneyColor = "0";//公积金缴存公司金额（0：不变色 1：变色）
	private String accumulationFundPersonalProportionColor = "0";//公积金缴存个人比例（0：不变色 1：变色）
	private String accumulationFundPersonalMoneyColor = "0";//公积金缴存个人金额（0：不变色 1：变色）
	private String totalMoneyColor = "0";//合计（0：不变色 1：变色）
	private String quitFlag = "在职";//是否离职
	private String delFlag = "0";//删除标识(0:正常 1:删除)
	private String systemRemark = "";//系统备注
	private String attr1;//备注
	private String attr2;//备注
	private String attr3;//备注
	private String attr4;//备注
	private String attr5;//备注
	private String attr6;//备注



	public PersonSalaryAccumulationFund() {
		super();
	}
	public PersonSalaryAccumulationFund(Long id, String personName,
			String personId, String accumulationFundDate,
			String accumulationFundYear, String accumulationFundMonth,
			String contractCompanyId, String contractCompanyName,
			String idcardNum, String accumulationFundBaseMoney,
			String accumulationFundCompanyProportion,
			String accumulationFundCompanyMoney,
			String accumulationFundPersonalProportion,
			String accumulationFundPersonalMoney, String totalMoney,
			String idcardNumColor, String accumulationFundBaseMoneyColor,
			String accumulationFundCompanyProportionColor,
			String accumulationFundCompanyMoneyColor,
			String accumulationFundPersonalProportionColor,
			String accumulationFundPersonalMoneyColor, String totalMoneyColor,
			String quitFlag, String delFlag, String systemRemark, String attr1,
			String attr2, String attr3, String attr4, String attr5, String attr6) {
		super();
		this.id = id;
		this.personName = personName;
		this.personId = personId;
		this.accumulationFundDate = accumulationFundDate;
		this.accumulationFundYear = accumulationFundYear;
		this.accumulationFundMonth = accumulationFundMonth;
		this.contractCompanyId = contractCompanyId;
		this.contractCompanyName = contractCompanyName;
		this.idcardNum = idcardNum;
		this.accumulationFundBaseMoney = accumulationFundBaseMoney;
		this.accumulationFundCompanyProportion = accumulationFundCompanyProportion;
		this.accumulationFundCompanyMoney = accumulationFundCompanyMoney;
		this.accumulationFundPersonalProportion = accumulationFundPersonalProportion;
		this.accumulationFundPersonalMoney = accumulationFundPersonalMoney;
		this.totalMoney = totalMoney;
		this.idcardNumColor = idcardNumColor;
		this.accumulationFundBaseMoneyColor = accumulationFundBaseMoneyColor;
		this.accumulationFundCompanyProportionColor = accumulationFundCompanyProportionColor;
		this.accumulationFundCompanyMoneyColor = accumulationFundCompanyMoneyColor;
		this.accumulationFundPersonalProportionColor = accumulationFundPersonalProportionColor;
		this.accumulationFundPersonalMoneyColor = accumulationFundPersonalMoneyColor;
		this.totalMoneyColor = totalMoneyColor;
		this.quitFlag = quitFlag;
		this.delFlag = delFlag;
		this.systemRemark = systemRemark;
		this.attr1 = attr1;
		this.attr2 = attr2;
		this.attr3 = attr3;
		this.attr4 = attr4;
		this.attr5 = attr5;
		this.attr6 = attr6;
	}
	@Id
    @Column(name = "ID", unique = true, nullable = false)
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	@Column(name = "person_name")
	public String getPersonName() {
		return personName;
	}
	public void setPersonName(String personName) {
		this.personName = personName;
	}
	@Column(name = "person_id")
	public String getPersonId() {
		return personId;
	}
	public void setPersonId(String personId) {
		this.personId = personId;
	}
	@Column(name = "accumulation_fund_date")
	public String getAccumulationFundDate() {
		return accumulationFundDate;
	}
	public void setAccumulationFundDate(String accumulationFundDate) {
		this.accumulationFundDate = accumulationFundDate;
	}
	@Column(name = "accumulation_fund_year")
	public String getAccumulationFundYear() {
		return accumulationFundYear;
	}
	public void setAccumulationFundYear(String accumulationFundYear) {
		this.accumulationFundYear = accumulationFundYear;
	}
	@Column(name = "accumulation_fund_month")
	public String getAccumulationFundMonth() {
		return accumulationFundMonth;
	}
	public void setAccumulationFundMonth(String accumulationFundMonth) {
		this.accumulationFundMonth = accumulationFundMonth;
	}
	@Column(name = "contract_company_id")
	public String getContractCompanyId() {
		return contractCompanyId;
	}
	public void setContractCompanyId(String contractCompanyId) {
		this.contractCompanyId = contractCompanyId;
	}
	@Column(name = "contract_company_name")
	public String getContractCompanyName() {
		return contractCompanyName;
	}
	public void setContractCompanyName(String contractCompanyName) {
		this.contractCompanyName = contractCompanyName;
	}
	@Column(name = "idcard_num")
	public String getIdcardNum() {
		return idcardNum;
	}
	public void setIdcardNum(String idcardNum) {
		this.idcardNum = idcardNum;
	}
	@Column(name = "accumulation_fund_base_money")
	public String getAccumulationFundBaseMoney() {
		return accumulationFundBaseMoney;
	}
	public void setAccumulationFundBaseMoney(String accumulationFundBaseMoney) {
		this.accumulationFundBaseMoney = accumulationFundBaseMoney;
	}
	@Column(name = "accumulation_fund_company_proportion")
	public String getAccumulationFundCompanyProportion() {
		return accumulationFundCompanyProportion;
	}
	public void setAccumulationFundCompanyProportion(
			String accumulationFundCompanyProportion) {
		this.accumulationFundCompanyProportion = accumulationFundCompanyProportion;
	}
	@Column(name = "accumulation_fund_company_money")
	public String getAccumulationFundCompanyMoney() {
		return accumulationFundCompanyMoney;
	}
	public void setAccumulationFundCompanyMoney(String accumulationFundCompanyMoney) {
		this.accumulationFundCompanyMoney = accumulationFundCompanyMoney;
	}
	@Column(name = "accumulation_fund_personal_proportion")
	public String getAccumulationFundPersonalProportion() {
		return accumulationFundPersonalProportion;
	}
	public void setAccumulationFundPersonalProportion(
			String accumulationFundPersonalProportion) {
		this.accumulationFundPersonalProportion = accumulationFundPersonalProportion;
	}
	@Column(name = "accumulation_fund_personal_money")
	public String getAccumulationFundPersonalMoney() {
		return accumulationFundPersonalMoney;
	}
	public void setAccumulationFundPersonalMoney(
			String accumulationFundPersonalMoney) {
		this.accumulationFundPersonalMoney = accumulationFundPersonalMoney;
	}
	@Column(name = "total_money")
	public String getTotalMoney() {
		return totalMoney;
	}
	public void setTotalMoney(String totalMoney) {
		this.totalMoney = totalMoney;
	}
	@Column(name = "idcard_num_color")
	public String getIdcardNumColor() {
		return idcardNumColor;
	}
	public void setIdcardNumColor(String idcardNumColor) {
		this.idcardNumColor = idcardNumColor;
	}
	@Column(name = "accumulation_fund_base_money_color")
	public String getAccumulationFundBaseMoneyColor() {
		return accumulationFundBaseMoneyColor;
	}
	public void setAccumulationFundBaseMoneyColor(
			String accumulationFundBaseMoneyColor) {
		this.accumulationFundBaseMoneyColor = accumulationFundBaseMoneyColor;
	}
	@Column(name = "accumulation_fund_company_proportion_color")
	public String getAccumulationFundCompanyProportionColor() {
		return accumulationFundCompanyProportionColor;
	}
	public void setAccumulationFundCompanyProportionColor(
			String accumulationFundCompanyProportionColor) {
		this.accumulationFundCompanyProportionColor = accumulationFundCompanyProportionColor;
	}
	@Column(name = "accumulation_fund_company_money_color")
	public String getAccumulationFundCompanyMoneyColor() {
		return accumulationFundCompanyMoneyColor;
	}
	public void setAccumulationFundCompanyMoneyColor(
			String accumulationFundCompanyMoneyColor) {
		this.accumulationFundCompanyMoneyColor = accumulationFundCompanyMoneyColor;
	}
	@Column(name = "accumulation_fund_personal_proportion_color")
	public String getAccumulationFundPersonalProportionColor() {
		return accumulationFundPersonalProportionColor;
	}
	public void setAccumulationFundPersonalProportionColor(
			String accumulationFundPersonalProportionColor) {
		this.accumulationFundPersonalProportionColor = accumulationFundPersonalProportionColor;
	}
	@Column(name = "accumulation_fund_personal_money_color")
	public String getAccumulationFundPersonalMoneyColor() {
		return accumulationFundPersonalMoneyColor;
	}
	public void setAccumulationFundPersonalMoneyColor(
			String accumulationFundPersonalMoneyColor) {
		this.accumulationFundPersonalMoneyColor = accumulationFundPersonalMoneyColor;
	}
	@Column(name = "total_money_color")
	public String getTotalMoneyColor() {
		return totalMoneyColor;
	}
	public void setTotalMoneyColor(String totalMoneyColor) {
		this.totalMoneyColor = totalMoneyColor;
	}
	@Column(name = "quit_flag")
	public String getQuitFlag() {
		return quitFlag;
	}
	public void setQuitFlag(String quitFlag) {
		this.quitFlag = quitFlag;
	}
	@Column(name = "del_flag")
	public String getDelFlag() {
		return delFlag;
	}
	public void setDelFlag(String delFlag) {
		this.delFlag = delFlag;
	}
	@Column(name = "system_remark")
	public String getSystemRemark() {
		return systemRemark;
	}
	public void setSystemRemark(String systemRemark) {
		this.systemRemark = systemRemark;
	}
	@Column(name = "attr1")
	public String getAttr1() {
		return attr1;
	}
	public void setAttr1(String attr1) {
		this.attr1 = attr1;
	}
	@Column(name = "attr2")
	public String getAttr2() {
		return attr2;
	}
	public void setAttr2(String attr2) {
		this.attr2 = attr2;
	}
	@Column(name = "attr3")
	public String getAttr3() {
		return attr3;
	}
	public void setAttr3(String attr3) {
		this.attr3 = attr3;
	}
	@Column(name = "attr4")
	public String getAttr4() {
		return attr4;
	}
	public void setAttr4(String attr4) {
		this.attr4 = attr4;
	}
	@Column(name = "attr5")
	public String getAttr5() {
		return attr5;
	}
	public void setAttr5(String attr5) {
		this.attr5 = attr5;
	}
	@Column(name = "attr6")
	public String getAttr6() {
		return attr6;
	}
	public void setAttr6(String attr6) {
		this.attr6 = attr6;
	}
    
    
}
