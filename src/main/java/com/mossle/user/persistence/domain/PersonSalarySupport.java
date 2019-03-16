package com.mossle.user.persistence.domain;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "person_salary_support")
public class PersonSalarySupport implements java.io.Serializable {
    private static final long serialVersionUID = 0L;
    
    private Long id; //id
	//private String salaryBaseId; //公司基本表id
    private String idcardNumColor = "0";//身份证号是否变色（0：不变色 1：变色）
	private String wagesLevelMoneyColor = "0";//工资标准是否变色（0：不变色 1：变色）
	private String baseWagesMoneyColor = "0";//基本工资是否变色（0：不变色 1：变色）
	private String postWagesMoneyColor = "0";//职务工资是否变色（0：不变色 1：变色）
	private String technicalWagesMoneyColor = "0";//技术工资是否变色（0：不变色 1：变色）
	private String confidentialityAllowanceMoneyColor = "0";//保密津贴是否变色（0：不变色 1：变色）
	private String technicalAllowanceMoneyColor = "0";//技术津贴是否变色（0：不变色 1：变色）
	private String achievementBonusMoneyColor = "0";//绩效奖金是否变色（0：不变色 1：变色）
	private String socialPensionDeductionMoneyColor = "0";//养老保险是否变色（0：不变色 1：变色）
	private String socialUnemploymentDeductionMoneyColor = "0";//失业保险是否变色（0：不变色 1：变色）
	private String socialMedicalDeductionMoneyColor = "0";//医疗保险是否变色（0：不变色 1：变色）
	private String socialProvidentFundDeductionMoneyColor = "0";//公积金是否变色（0：不变色 1：变色）
	private String socialOtherDeductionMoneyColor = "0";//其他项扣款是否变色（0：不变色 1：变色）
	private String socialTotalDeductionMoneyColor = "0";//保险扣款合计是否变色（0：不变色 1：变色）
	private String specialChildrenEducationMoneyColor = "0";//子女教育是否变色（0：不变色 1：变色）
	private String specialContinuEducationMoneyColor = "0";//继续教育是否变色（0：不变色 1：变色）
	private String specialHotelInterestMoneyColor = "0";//住房贷款利息是否变色（0：不变色 1：变色）
	private String specialHotelRentMoneyColor = "0";//住房租金是否变色（0：不变色 1：变色）
	private String specialSupportElderlyMoneyColor = "0";//赡养老人是否变色（0：不变色 1：变色）
	private String specialCommercialHealthInsuranceMoneyColor = "0";//商业健康险是否变色（0：不变色 1：变色）
	private String systemRemark = "";//用户id
	private String attr1;//备注
	private String attr2;//备注
	private String attr3;//备注
	private String attr4;//备注
	private String attr5;//备注
	private String attr6;//备注
	
	/** 外键，id. */
    private PersonSalaryBase personSalaryBase;
	
	
	public PersonSalarySupport() {
		super();
	}
	
	public PersonSalarySupport(Long id, /*String salaryBaseId,*/String idcardNumColor,
			String wagesWevelMoneyColor, String baseWagesMoneyColor,
			String postWagesMoneyColor, String technicalWagesMoneyColor,
			String confidentialityAllowanceMoneyColor,
			String technicalAllowanceMoneyColor,
			String achievementBonusMoneyColor,
			String socialPensionDeductionMoneyColor,
			String socialUnemploymentDeductionMoneyColor,
			String socialMedicalDeductionMoneyColor,
			String socialProvidentFundDeductionMoneyColor,
			String socialOtherDeductionMoneyColor,
			String socialTotalDeductionMoneyColor,
			String specialChildrenEducationMoneyColor,
			String specialContinuEducationMoneyColor,
			String specialHotelInterestMoneyColor,
			String specialHotelRentMoneyColor,
			String specialSupportElderlyMoneyColor,
			String specialCommercialHealthInsuranceMoneyColor,
			String systemRemark, String attr1, String attr2, String attr3,
			String attr4, String attr5, String attr6,
			PersonSalaryBase personSalaryBase) {
		super();
		this.id = id;
		//this.salaryBaseId = salaryBaseId;
		this.idcardNumColor = idcardNumColor;
		this.wagesLevelMoneyColor = wagesLevelMoneyColor;
		this.baseWagesMoneyColor = baseWagesMoneyColor;
		this.postWagesMoneyColor = postWagesMoneyColor;
		this.technicalWagesMoneyColor = technicalWagesMoneyColor;
		this.confidentialityAllowanceMoneyColor = confidentialityAllowanceMoneyColor;
		this.technicalAllowanceMoneyColor = technicalAllowanceMoneyColor;
		this.achievementBonusMoneyColor = achievementBonusMoneyColor;
		this.socialPensionDeductionMoneyColor = socialPensionDeductionMoneyColor;
		this.socialUnemploymentDeductionMoneyColor = socialUnemploymentDeductionMoneyColor;
		this.socialMedicalDeductionMoneyColor = socialMedicalDeductionMoneyColor;
		this.socialProvidentFundDeductionMoneyColor = socialProvidentFundDeductionMoneyColor;
		this.socialOtherDeductionMoneyColor = socialOtherDeductionMoneyColor;
		this.socialTotalDeductionMoneyColor = socialTotalDeductionMoneyColor;
		this.specialChildrenEducationMoneyColor = specialChildrenEducationMoneyColor;
		this.specialContinuEducationMoneyColor = specialContinuEducationMoneyColor;
		this.specialHotelInterestMoneyColor = specialHotelInterestMoneyColor;
		this.specialHotelRentMoneyColor = specialHotelRentMoneyColor;
		this.specialSupportElderlyMoneyColor = specialSupportElderlyMoneyColor;
		this.specialCommercialHealthInsuranceMoneyColor = specialCommercialHealthInsuranceMoneyColor;
		this.systemRemark = systemRemark;
		this.attr1 = attr1;
		this.attr2 = attr2;
		this.attr3 = attr3;
		this.attr4 = attr4;
		this.attr5 = attr5;
		this.attr6 = attr6;
		this.personSalaryBase = personSalaryBase;
	}

	

	public PersonSalarySupport(String idcardNumColor,String wagesWevelMoneyColor,
			String baseWagesMoneyColor, String postWagesMoneyColor,
			String technicalWagesMoneyColor,
			String confidentialityAllowanceMoneyColor,
			String technicalAllowanceMoneyColor,
			String achievementBonusMoneyColor,
			String socialPensionDeductionMoneyColor,
			String socialUnemploymentDeductionMoneyColor,
			String socialMedicalDeductionMoneyColor,
			String socialProvidentFundDeductionMoneyColor,
			String socialOtherDeductionMoneyColor,
			String socialTotalDeductionMoneyColor,
			String specialChildrenEducationMoneyColor,
			String specialContinuEducationMoneyColor,
			String specialHotelInterestMoneyColor,
			String specialHotelRentMoneyColor,
			String specialSupportElderlyMoneyColor,
			String specialCommercialHealthInsuranceMoneyColor,
			String systemRemark, PersonSalaryBase personSalaryBase) {
		super();
		this.idcardNumColor = idcardNumColor;
		this.wagesLevelMoneyColor = wagesLevelMoneyColor;
		this.baseWagesMoneyColor = baseWagesMoneyColor;
		this.postWagesMoneyColor = postWagesMoneyColor;
		this.technicalWagesMoneyColor = technicalWagesMoneyColor;
		this.confidentialityAllowanceMoneyColor = confidentialityAllowanceMoneyColor;
		this.technicalAllowanceMoneyColor = technicalAllowanceMoneyColor;
		this.achievementBonusMoneyColor = achievementBonusMoneyColor;
		this.socialPensionDeductionMoneyColor = socialPensionDeductionMoneyColor;
		this.socialUnemploymentDeductionMoneyColor = socialUnemploymentDeductionMoneyColor;
		this.socialMedicalDeductionMoneyColor = socialMedicalDeductionMoneyColor;
		this.socialProvidentFundDeductionMoneyColor = socialProvidentFundDeductionMoneyColor;
		this.socialOtherDeductionMoneyColor = socialOtherDeductionMoneyColor;
		this.socialTotalDeductionMoneyColor = socialTotalDeductionMoneyColor;
		this.specialChildrenEducationMoneyColor = specialChildrenEducationMoneyColor;
		this.specialContinuEducationMoneyColor = specialContinuEducationMoneyColor;
		this.specialHotelInterestMoneyColor = specialHotelInterestMoneyColor;
		this.specialHotelRentMoneyColor = specialHotelRentMoneyColor;
		this.specialSupportElderlyMoneyColor = specialSupportElderlyMoneyColor;
		this.specialCommercialHealthInsuranceMoneyColor = specialCommercialHealthInsuranceMoneyColor;
		this.systemRemark = systemRemark;
		this.personSalaryBase = personSalaryBase;
	}

	@Id
    @Column(name = "ID", unique = true, nullable = false)
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	 /** @return 外键，DICT_TYPE. */
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "salary_base_id")
	public PersonSalaryBase getPersonSalaryBase() {
		return personSalaryBase;
	}

	public void setPersonSalaryBase(PersonSalaryBase personSalaryBase) {
		this.personSalaryBase = personSalaryBase;
	}
	@Column(name = "idcard_num_color")
	public String getIdcardNumColor() {
		return idcardNumColor;
	}

	public void setIdcardNumColor(String idcardNumColor) {
		this.idcardNumColor = idcardNumColor;
	}

	/*@Column(name = "salary_base_id")
	public String getSalaryBaseId() {
		return salaryBaseId;
	}
	public void setSalaryBaseId(String salaryBaseId) {
		this.salaryBaseId = salaryBaseId;
	}*/
	@Column(name = "wages_level_money_color")
	public String getWagesLevelMoneyColor() {
		return wagesLevelMoneyColor;
	}
	public void setWagesLevelMoneyColor(String wagesLevelMoneyColor) {
		this.wagesLevelMoneyColor = wagesLevelMoneyColor;
	}
	@Column(name = "base_wages_money_color")
	public String getBaseWagesMoneyColor() {
		return baseWagesMoneyColor;
	}
	public void setBaseWagesMoneyColor(String baseWagesMoneyColor) {
		this.baseWagesMoneyColor = baseWagesMoneyColor;
	}
	@Column(name = "post_wages_money_color")
	public String getPostWagesMoneyColor() {
		return postWagesMoneyColor;
	}
	public void setPostWagesMoneyColor(String postWagesMoneyColor) {
		this.postWagesMoneyColor = postWagesMoneyColor;
	}
	@Column(name = "technical_wages_money_color")
	public String getTechnicalWagesMoneyColor() {
		return technicalWagesMoneyColor;
	}
	public void setTechnicalWagesMoneyColor(String technicalWagesMoneyColor) {
		this.technicalWagesMoneyColor = technicalWagesMoneyColor;
	}
	@Column(name = "confidentiality_allowance_money_color")
	public String getConfidentialityAllowanceMoneyColor() {
		return confidentialityAllowanceMoneyColor;
	}
	public void setConfidentialityAllowanceMoneyColor(
			String confidentialityAllowanceMoneyColor) {
		this.confidentialityAllowanceMoneyColor = confidentialityAllowanceMoneyColor;
	}
	@Column(name = "technical_allowance_money_color")
	public String getTechnicalAllowanceMoneyColor() {
		return technicalAllowanceMoneyColor;
	}
	public void setTechnicalAllowanceMoneyColor(String technicalAllowanceMoneyColor) {
		this.technicalAllowanceMoneyColor = technicalAllowanceMoneyColor;
	}
	@Column(name = "achievement_bonus_money_color")
	public String getAchievementBonusMoneyColor() {
		return achievementBonusMoneyColor;
	}
	public void setAchievementBonusMoneyColor(String achievementBonusMoneyColor) {
		this.achievementBonusMoneyColor = achievementBonusMoneyColor;
	}
	@Column(name = "social_pension_deduction_money_color")
	public String getSocialPensionDeductionMoneyColor() {
		return socialPensionDeductionMoneyColor;
	}
	public void setSocialPensionDeductionMoneyColor(
			String socialPensionDeductionMoneyColor) {
		this.socialPensionDeductionMoneyColor = socialPensionDeductionMoneyColor;
	}
	@Column(name = "social_unemployment_deduction_money_color")
	public String getSocialUnemploymentDeductionMoneyColor() {
		return socialUnemploymentDeductionMoneyColor;
	}
	public void setSocialUnemploymentDeductionMoneyColor(
			String socialUnemploymentDeductionMoneyColor) {
		this.socialUnemploymentDeductionMoneyColor = socialUnemploymentDeductionMoneyColor;
	}
	@Column(name = "social_medical_deduction_money_color")
	public String getSocialMedicalDeductionMoneyColor() {
		return socialMedicalDeductionMoneyColor;
	}
	public void setSocialMedicalDeductionMoneyColor(
			String socialMedicalDeductionMoneyColor) {
		this.socialMedicalDeductionMoneyColor = socialMedicalDeductionMoneyColor;
	}
	@Column(name = "social_provident_fund_deduction_money_color")
	public String getSocialProvidentFundDeductionMoneyColor() {
		return socialProvidentFundDeductionMoneyColor;
	}
	public void setSocialProvidentFundDeductionMoneyColor(
			String socialProvidentFundDeductionMoneyColor) {
		this.socialProvidentFundDeductionMoneyColor = socialProvidentFundDeductionMoneyColor;
	}
	@Column(name = "social_other_deduction_money_color")
	public String getSocialOtherDeductionMoneyColor() {
		return socialOtherDeductionMoneyColor;
	}
	public void setSocialOtherDeductionMoneyColor(
			String socialOtherDeductionMoneyColor) {
		this.socialOtherDeductionMoneyColor = socialOtherDeductionMoneyColor;
	}
	@Column(name = "social_total_deduction_money_color")
	public String getSocialTotalDeductionMoneyColor() {
		return socialTotalDeductionMoneyColor;
	}
	public void setSocialTotalDeductionMoneyColor(
			String socialTotalDeductionMoneyColor) {
		this.socialTotalDeductionMoneyColor = socialTotalDeductionMoneyColor;
	}
	@Column(name = "special_children_education_money_color")
	public String getSpecialChildrenEducationMoneyColor() {
		return specialChildrenEducationMoneyColor;
	}
	public void setSpecialChildrenEducationMoneyColor(
			String specialChildrenEducationMoneyColor) {
		this.specialChildrenEducationMoneyColor = specialChildrenEducationMoneyColor;
	}
	@Column(name = "special_continu_education_money_color")
	public String getSpecialContinuEducationMoneyColor() {
		return specialContinuEducationMoneyColor;
	}
	public void setSpecialContinuEducationMoneyColor(
			String specialContinuEducationMoneyColor) {
		this.specialContinuEducationMoneyColor = specialContinuEducationMoneyColor;
	}
	@Column(name = "special_hotel_interest_money_color")
	public String getSpecialHotelInterestMoneyColor() {
		return specialHotelInterestMoneyColor;
	}
	public void setSpecialHotelInterestMoneyColor(
			String specialHotelInterestMoneyColor) {
		this.specialHotelInterestMoneyColor = specialHotelInterestMoneyColor;
	}
	@Column(name = "special_hotel_rent_money_color")
	public String getSpecialHotelRentMoneyColor() {
		return specialHotelRentMoneyColor;
	}
	public void setSpecialHotelRentMoneyColor(String specialHotelRentMoneyColor) {
		this.specialHotelRentMoneyColor = specialHotelRentMoneyColor;
	}
	@Column(name = "special_support_elderly_money_color")
	public String getSpecialSupportElderlyMoneyColor() {
		return specialSupportElderlyMoneyColor;
	}
	public void setSpecialSupportElderlyMoneyColor(
			String specialSupportElderlyMoneyColor) {
		this.specialSupportElderlyMoneyColor = specialSupportElderlyMoneyColor;
	}
	@Column(name = "special_commercial_health_insurance_money_color")
	public String getSpecialCommercialHealthInsuranceMoneyColor() {
		return specialCommercialHealthInsuranceMoneyColor;
	}
	public void setSpecialCommercialHealthInsuranceMoneyColor(
			String specialCommercialHealthInsuranceMoneyColor) {
		this.specialCommercialHealthInsuranceMoneyColor = specialCommercialHealthInsuranceMoneyColor;
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
