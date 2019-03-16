package com.mossle.user.persistence.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "person_salary_social_security")
public class PersonSalarySocialSecurity implements java.io.Serializable {
    private static final long serialVersionUID = 0L;
    
    private Long id; //id
	private String personName; //姓名
	private String personId;//用户id
	private String socialSecurityDate;//'社保日期
	private String socialSecurityYear;//社保日期年
	private String socialSecurityMonth;//社保日期月
	private String contractCompanyId;//单位id
	private String contractCompanyName;//单位名称
	private String idcardNum;//身份证号
	private String accountCharacte;//户口性质
	private String pensionBaseMoney;//养老基数
	private String pensionCompanyProportion;//养老公司比例
	private String pensionCompanyMoney;//养老公司金额
	private String pensionPersonalProportion;//养老个人比例
	private String pensionPersonalMoney;//养老个人金额
	private String unemploymentCompanyProportion;//失业公司比例
	private String unemploymentCompanyMoney;//失业公司金额
	private String unemploymentPersonalProportion;//失业个人比例
	private String unemploymentPersonalMoney;//失业个人金额
	private String medicalBaseMoney;//医疗保险基数
	private String medicalCompanyProportion;//医疗公司比例
	private String medicalCompanyMoney;//医疗公司金额
	private String medicalPersonalProportion;//医疗个人比例
	private String medicalPersonalMoney;//医疗个人金额
	private String injuryCompanyProportion;//工伤公司比例
	private String injuryCompanyMoney;//工伤公司金额
	private String birthCompanyProportion;//生育公司比例
	private String birthCompanyMoney;//生育公司金额
	private String idcardNumColor = "0";//身份证号是否变色（0：不变色 1：变色）
	private String accountCharacteColor = "0";//户口性质是否变色（0：不变色 1：变色）
	private String pensionBaseMoneyColor = "0";//养老基数是否变色（0：不变色 1：变色）
	private String pensionCompanyProportionColor = "0";//养老公司比例是否变色（0：不变色 1：变色）
	private String pensionCompanyMoneyColor = "0";//养老公司金额是否变色（0：不变色 1：变色）
	private String pensionPersonalProportionColor = "0";//养老个人比例是否变色（0：不变色 1：变色）
	private String pensionPersonalMoneyColor = "0";//养老个人金额是否变色（0：不变色 1：变色）
	private String unemploymentCompanyProportionColor = "0";//失业公司比例是否变色（0：不变色 1：变色）
	private String unemploymentCompanyMoneyColor = "0";//失业公司金额是否变色（0：不变色 1：变色）
	private String unemploymentPersonalProportionColor = "0";//失业个人比例是否变色（0：不变色 1：变色）
	private String unemploymentPersonalMoneyColor = "0";//失业个人金额是否变色（0：不变色 1：变色）
	private String medicalBaseMoneyColor = "0";//医疗保险基数是否变色（0：不变色 1：变色）
	private String medicalCompanyProportionColor = "0";//医疗公司比例是否变色（0：不变色 1：变色）
	private String medicalCompanyMoneyColor = "0";//医疗公司金额是否变色（0：不变色 1：变色）
	private String medicalPersonalProportionColor = "0";//医疗个人比例是否变色（0：不变色 1：变色）
	private String medicalPersonalMoneyColor = "0";//医疗个人金额是否变色（0：不变色 1：变色）
	private String injuryCompanyProportionColor = "0";//工伤公司比例是否变色（0：不变色 1：变色）
	private String injuryCompanyMoneyColor = "0";//工伤公司金额是否变色（0：不变色 1：变色）
	private String birthCompanyProportionColor = "0";//生育公司比例是否变色（0：不变色 1：变色）
	private String birthCompanyMoneyColor = "0";//生育公司金额是否变色（0：不变色 1：变色）
	private String quitFlag = "在职";//是否离职
	private String delFlag = "0";//删除标识(0:正常 1:删除)
	private String systemRemark = "";//系统备注
	private String attr1;//备注
	private String attr2;//备注
	private String attr3;//备注
	private String attr4;//备注
	private String attr5;//备注
	private String attr6;//备注
	public PersonSalarySocialSecurity() {
		super();
	}


	public PersonSalarySocialSecurity(Long id, String personName,
			String personId, String socialSecurityDate,
			String socialSecurityYear, String socialSecurityMonth,
			String contractCompanyId, String contractCompanyName,
			String idcardNum, String accountCharacte, String pensionBaseMoney,
			String pensionCompanyProportion, String pensionCompanyMoney,
			String pensionPersonalProportion, String pensionPersonalMoney,
			String unemploymentCompanyProportion,
			String unemploymentCompanyMoney,
			String unemploymentPersonalProportion,
			String unemploymentPersonalMoney, String medicalBaseMoney,
			String medicalCompanyProportion, String medicalCompanyMoney,
			String medicalPersonalProportion, String medicalPersonalMoney,
			String injuryCompanyProportion, String injuryCompanyMoney,
			String birthCompanyProportion, String birthCompanyMoney,
			String idcardNumColor, String accountCharacteColor,
			String pensionBaseMoneyColor, String pensionCompanyProportionColor,
			String pensionCompanyMoneyColor,
			String pensionPersonalProportionColor,
			String pensionPersonalMoneyColor,
			String unemploymentCompanyProportionColor,
			String unemploymentCompanyMoneyColor,
			String unemploymentPersonalProportionColor,
			String unemploymentPersonalMoneyColor,
			String medicalBaseMoneyColor, String medicalCompanyProportionColor,
			String medicalCompanyMoneyColor,
			String medicalPersonalProportionColor,
			String medicalPersonalMoneyColor,
			String injuryCompanyProportionColor,
			String injuryCompanyMoneyColor, String birthCompanyProportionColor,
			String birthCompanyMoneyColor, String quitFlag, String delFlag,
			String systemRemark, String attr1, String attr2, String attr3,
			String attr4, String attr5, String attr6) {
		super();
		this.id = id;
		this.personName = personName;
		this.personId = personId;
		this.socialSecurityDate = socialSecurityDate;
		this.socialSecurityYear = socialSecurityYear;
		this.socialSecurityMonth = socialSecurityMonth;
		this.contractCompanyId = contractCompanyId;
		this.contractCompanyName = contractCompanyName;
		this.idcardNum = idcardNum;
		this.accountCharacte = accountCharacte;
		this.pensionBaseMoney = pensionBaseMoney;
		this.pensionCompanyProportion = pensionCompanyProportion;
		this.pensionCompanyMoney = pensionCompanyMoney;
		this.pensionPersonalProportion = pensionPersonalProportion;
		this.pensionPersonalMoney = pensionPersonalMoney;
		this.unemploymentCompanyProportion = unemploymentCompanyProportion;
		this.unemploymentCompanyMoney = unemploymentCompanyMoney;
		this.unemploymentPersonalProportion = unemploymentPersonalProportion;
		this.unemploymentPersonalMoney = unemploymentPersonalMoney;
		this.medicalBaseMoney = medicalBaseMoney;
		this.medicalCompanyProportion = medicalCompanyProportion;
		this.medicalCompanyMoney = medicalCompanyMoney;
		this.medicalPersonalProportion = medicalPersonalProportion;
		this.medicalPersonalMoney = medicalPersonalMoney;
		this.injuryCompanyProportion = injuryCompanyProportion;
		this.injuryCompanyMoney = injuryCompanyMoney;
		this.birthCompanyProportion = birthCompanyProportion;
		this.birthCompanyMoney = birthCompanyMoney;
		this.idcardNumColor = idcardNumColor;
		this.accountCharacteColor = accountCharacteColor;
		this.pensionBaseMoneyColor = pensionBaseMoneyColor;
		this.pensionCompanyProportionColor = pensionCompanyProportionColor;
		this.pensionCompanyMoneyColor = pensionCompanyMoneyColor;
		this.pensionPersonalProportionColor = pensionPersonalProportionColor;
		this.pensionPersonalMoneyColor = pensionPersonalMoneyColor;
		this.unemploymentCompanyProportionColor = unemploymentCompanyProportionColor;
		this.unemploymentCompanyMoneyColor = unemploymentCompanyMoneyColor;
		this.unemploymentPersonalProportionColor = unemploymentPersonalProportionColor;
		this.unemploymentPersonalMoneyColor = unemploymentPersonalMoneyColor;
		this.medicalBaseMoneyColor = medicalBaseMoneyColor;
		this.medicalCompanyProportionColor = medicalCompanyProportionColor;
		this.medicalCompanyMoneyColor = medicalCompanyMoneyColor;
		this.medicalPersonalProportionColor = medicalPersonalProportionColor;
		this.medicalPersonalMoneyColor = medicalPersonalMoneyColor;
		this.injuryCompanyProportionColor = injuryCompanyProportionColor;
		this.injuryCompanyMoneyColor = injuryCompanyMoneyColor;
		this.birthCompanyProportionColor = birthCompanyProportionColor;
		this.birthCompanyMoneyColor = birthCompanyMoneyColor;
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
	@Column(name = "social_security_date")
	public String getSocialSecurityDate() {
		return socialSecurityDate;
	}
	public void setSocialSecurityDate(String socialSecurityDate) {
		this.socialSecurityDate = socialSecurityDate;
	}
	@Column(name = "social_security_year")
	public String getSocialSecurityYear() {
		return socialSecurityYear;
	}
	public void setSocialSecurityYear(String socialSecurityYear) {
		this.socialSecurityYear = socialSecurityYear;
	}
	@Column(name = "social_security_month")
	public String getSocialSecurityMonth() {
		return socialSecurityMonth;
	}
	public void setSocialSecurityMonth(String socialSecurityMonth) {
		this.socialSecurityMonth = socialSecurityMonth;
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
	@Column(name = "account_characte")
	public String getAccountCharacte() {
		return accountCharacte;
	}
	public void setAccountCharacte(String accountCharacte) {
		this.accountCharacte = accountCharacte;
	}
	@Column(name = "pension_base_money")
	public String getPensionBaseMoney() {
		return pensionBaseMoney;
	}
	public void setPensionBaseMoney(String pensionBaseMoney) {
		this.pensionBaseMoney = pensionBaseMoney;
	}
	@Column(name = "pension_company_proportion")
	public String getPensionCompanyProportion() {
		return pensionCompanyProportion;
	}
	public void setPensionCompanyProportion(String pensionCompanyProportion) {
		this.pensionCompanyProportion = pensionCompanyProportion;
	}
	@Column(name = "pension_company_money")
	public String getPensionCompanyMoney() {
		return pensionCompanyMoney;
	}
	public void setPensionCompanyMoney(String pensionCompanyMoney) {
		this.pensionCompanyMoney = pensionCompanyMoney;
	}
	@Column(name = "pension_personal_proportion")
	public String getPensionPersonalProportion() {
		return pensionPersonalProportion;
	}
	public void setPensionPersonalProportion(String pensionPersonalProportion) {
		this.pensionPersonalProportion = pensionPersonalProportion;
	}
	@Column(name = "pension_personal_money")
	public String getPensionPersonalMoney() {
		return pensionPersonalMoney;
	}
	public void setPensionPersonalMoney(String pensionPersonalMoney) {
		this.pensionPersonalMoney = pensionPersonalMoney;
	}
	@Column(name = "unemployment_company_proportion")
	public String getUnemploymentCompanyProportion() {
		return unemploymentCompanyProportion;
	}
	public void setUnemploymentCompanyProportion(
			String unemploymentCompanyProportion) {
		this.unemploymentCompanyProportion = unemploymentCompanyProportion;
	}
	@Column(name = "unemployment_company_money")
	public String getUnemploymentCompanyMoney() {
		return unemploymentCompanyMoney;
	}
	public void setUnemploymentCompanyMoney(String unemploymentCompanyMoney) {
		this.unemploymentCompanyMoney = unemploymentCompanyMoney;
	}
	@Column(name = "unemployment_personal_proportion")
	public String getUnemploymentPersonalProportion() {
		return unemploymentPersonalProportion;
	}
	public void setUnemploymentPersonalProportion(
			String unemploymentPersonalProportion) {
		this.unemploymentPersonalProportion = unemploymentPersonalProportion;
	}
	@Column(name = "unemployment_personal_money")
	public String getUnemploymentPersonalMoney() {
		return unemploymentPersonalMoney;
	}
	public void setUnemploymentPersonalMoney(String unemploymentPersonalMoney) {
		this.unemploymentPersonalMoney = unemploymentPersonalMoney;
	}
	@Column(name = "medical_base_money")
	public String getMedicalBaseMoney() {
		return medicalBaseMoney;
	}
	public void setMedicalBaseMoney(String medicalBaseMoney) {
		this.medicalBaseMoney = medicalBaseMoney;
	}
	@Column(name = "medical_company_proportion")
	public String getMedicalCompanyProportion() {
		return medicalCompanyProportion;
	}
	public void setMedicalCompanyProportion(String medicalCompanyProportion) {
		this.medicalCompanyProportion = medicalCompanyProportion;
	}
	@Column(name = "medical_company_money")
	public String getMedicalCompanyMoney() {
		return medicalCompanyMoney;
	}
	public void setMedicalCompanyMoney(String medicalCompanyMoney) {
		this.medicalCompanyMoney = medicalCompanyMoney;
	}
	@Column(name = "medical_personal_proportion")
	public String getMedicalPersonalProportion() {
		return medicalPersonalProportion;
	}
	public void setMedicalPersonalProportion(String medicalPersonalProportion) {
		this.medicalPersonalProportion = medicalPersonalProportion;
	}
	@Column(name = "medical_personal_money")
	public String getMedicalPersonalMoney() {
		return medicalPersonalMoney;
	}
	public void setMedicalPersonalMoney(String medicalPersonalMoney) {
		this.medicalPersonalMoney = medicalPersonalMoney;
	}
	@Column(name = "injury_company_proportion")
	public String getInjuryCompanyProportion() {
		return injuryCompanyProportion;
	}
	public void setInjuryCompanyProportion(String injuryCompanyProportion) {
		this.injuryCompanyProportion = injuryCompanyProportion;
	}
	@Column(name = "injury_company_money")
	public String getInjuryCompanyMoney() {
		return injuryCompanyMoney;
	}
	public void setInjuryCompanyMoney(String injuryCompanyMoney) {
		this.injuryCompanyMoney = injuryCompanyMoney;
	}
	@Column(name = "birth_company_proportion")
	public String getBirthCompanyProportion() {
		return birthCompanyProportion;
	}
	public void setBirthCompanyProportion(String birthCompanyProportion) {
		this.birthCompanyProportion = birthCompanyProportion;
	}
	@Column(name = "birth_company_money")
	public String getBirthCompanyMoney() {
		return birthCompanyMoney;
	}
	public void setBirthCompanyMoney(String birthCompanyMoney) {
		this.birthCompanyMoney = birthCompanyMoney;
	}
	@Column(name = "idcard_num_color")
	public String getIdcardNumColor() {
		return idcardNumColor;
	}

	public void setIdcardNumColor(String idcardNumColor) {
		this.idcardNumColor = idcardNumColor;
	}

	@Column(name = "account_characte_color")
	public String getAccountCharacteColor() {
		return accountCharacteColor;
	}
	public void setAccountCharacteColor(String accountCharacteColor) {
		this.accountCharacteColor = accountCharacteColor;
	}
	@Column(name = "pension_base_money_color")
	public String getPensionBaseMoneyColor() {
		return pensionBaseMoneyColor;
	}
	public void setPensionBaseMoneyColor(String pensionBaseMoneyColor) {
		this.pensionBaseMoneyColor = pensionBaseMoneyColor;
	}
	@Column(name = "pension_company_proportion_color")
	public String getPensionCompanyProportionColor() {
		return pensionCompanyProportionColor;
	}
	public void setPensionCompanyProportionColor(
			String pensionCompanyProportionColor) {
		this.pensionCompanyProportionColor = pensionCompanyProportionColor;
	}
	@Column(name = "pension_company_money_color")
	public String getPensionCompanyMoneyColor() {
		return pensionCompanyMoneyColor;
	}
	public void setPensionCompanyMoneyColor(String pensionCompanyMoneyColor) {
		this.pensionCompanyMoneyColor = pensionCompanyMoneyColor;
	}
	@Column(name = "pension_personal_proportion_color")
	public String getPensionPersonalProportionColor() {
		return pensionPersonalProportionColor;
	}
	public void setPensionPersonalProportionColor(
			String pensionPersonalProportionColor) {
		this.pensionPersonalProportionColor = pensionPersonalProportionColor;
	}
	@Column(name = "pension_personal_money_color")
	public String getPensionPersonalMoneyColor() {
		return pensionPersonalMoneyColor;
	}
	public void setPensionPersonalMoneyColor(String pensionPersonalMoneyColor) {
		this.pensionPersonalMoneyColor = pensionPersonalMoneyColor;
	}
	@Column(name = "unemployment_company_proportion_color")
	public String getUnemploymentCompanyProportionColor() {
		return unemploymentCompanyProportionColor;
	}
	public void setUnemploymentCompanyProportionColor(
			String unemploymentCompanyProportionColor) {
		this.unemploymentCompanyProportionColor = unemploymentCompanyProportionColor;
	}
	@Column(name = "unemployment_company_money_color")
	public String getUnemploymentCompanyMoneyColor() {
		return unemploymentCompanyMoneyColor;
	}
	public void setUnemploymentCompanyMoneyColor(
			String unemploymentCompanyMoneyColor) {
		this.unemploymentCompanyMoneyColor = unemploymentCompanyMoneyColor;
	}
	@Column(name = "unemployment_personal_proportion_color")
	public String getUnemploymentPersonalProportionColor() {
		return unemploymentPersonalProportionColor;
	}
	public void setUnemploymentPersonalProportionColor(
			String unemploymentPersonalProportionColor) {
		this.unemploymentPersonalProportionColor = unemploymentPersonalProportionColor;
	}
	@Column(name = "unemployment_personal_money_color")
	public String getUnemploymentPersonalMoneyColor() {
		return unemploymentPersonalMoneyColor;
	}
	public void setUnemploymentPersonalMoneyColor(
			String unemploymentPersonalMoneyColor) {
		this.unemploymentPersonalMoneyColor = unemploymentPersonalMoneyColor;
	}
	@Column(name = "medical_base_money_color")
	public String getMedicalBaseMoneyColor() {
		return medicalBaseMoneyColor;
	}
	public void setMedicalBaseMoneyColor(String medicalBaseMoneyColor) {
		this.medicalBaseMoneyColor = medicalBaseMoneyColor;
	}
	@Column(name = "medical_company_proportion_color")
	public String getMedicalCompanyProportionColor() {
		return medicalCompanyProportionColor;
	}
	public void setMedicalCompanyProportionColor(
			String medicalCompanyProportionColor) {
		this.medicalCompanyProportionColor = medicalCompanyProportionColor;
	}
	@Column(name = "medical_company_money_color")
	public String getMedicalCompanyMoneyColor() {
		return medicalCompanyMoneyColor;
	}
	public void setMedicalCompanyMoneyColor(String medicalCompanyMoneyColor) {
		this.medicalCompanyMoneyColor = medicalCompanyMoneyColor;
	}
	@Column(name = "medical_personal_proportion_color")
	public String getMedicalPersonalProportionColor() {
		return medicalPersonalProportionColor;
	}
	public void setMedicalPersonalProportionColor(
			String medicalPersonalProportionColor) {
		this.medicalPersonalProportionColor = medicalPersonalProportionColor;
	}
	@Column(name = "medical_personal_money_color")
	public String getMedicalPersonalMoneyColor() {
		return medicalPersonalMoneyColor;
	}

	public void setMedicalPersonalMoneyColor(String medicalPersonalMoneyColor) {
		this.medicalPersonalMoneyColor = medicalPersonalMoneyColor;
	}
	@Column(name = "Injury_company_proportion_color")
	public String getInjuryCompanyProportionColor() {
		return injuryCompanyProportionColor;
	}

	public void setInjuryCompanyProportionColor(String injuryCompanyProportionColor) {
		this.injuryCompanyProportionColor = injuryCompanyProportionColor;
	}
	@Column(name = "Injury_company_money_color")
	public String getInjuryCompanyMoneyColor() {
		return injuryCompanyMoneyColor;
	}

	public void setInjuryCompanyMoneyColor(String injuryCompanyMoneyColor) {
		this.injuryCompanyMoneyColor = injuryCompanyMoneyColor;
	}
	@Column(name = "birth_company_proportion_color")
	public String getBirthCompanyProportionColor() {
		return birthCompanyProportionColor;
	}
	public void setBirthCompanyProportionColor(String birthCompanyProportionColor) {
		this.birthCompanyProportionColor = birthCompanyProportionColor;
	}
	@Column(name = "birth_company_money_color")
	public String getBirthCompanyMoneyColor() {
		return birthCompanyMoneyColor;
	}
	public void setBirthCompanyMoneyColor(String birthCompanyMoneyColor) {
		this.birthCompanyMoneyColor = birthCompanyMoneyColor;
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
