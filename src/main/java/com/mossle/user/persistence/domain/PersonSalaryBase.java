package com.mossle.user.persistence.domain;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "person_salary_base")
public class PersonSalaryBase implements java.io.Serializable {
    private static final long serialVersionUID = 0L;
    
	private Long id; //id
	private String salaryDate; //工资日期
	private String salaryYear;//工资日期年
	private String salaryMonth;//工资日期月
	private String employeeNo;//工号
	private String personName;//姓名
	private String contractCompanyId;//单位id
	private String contractCompanyName;//单位名称
	private String personId;//员工id
	private String idcardNum;//身份证号
	private String personLevel;//级别
	private Date entryDate;//入职时间
	private String allAttendanceDays;//应出勤天数
	private String paidTripDays;//出差天数
	private String paidLieuLeaveDays;//调休天数
	private String paidAnnualLeaveDays;//年假天数
	private String paidMaternityLeaveDays;//产假天数
	private String onePointFiveOvertimeDays;//1.5倍加班天数
	private String twoOvertimeDays;//2倍加班天数
	private String threeOvertimeDays;//3倍加班天数
	private String missingAttendanceDays;//缺勤天数
	private String sickLeaveDays;//病假天数
	private String casualLeaveDays;//事假天数
	private String halfAbsentDays;//半天旷工
	private String oneAbsentDays;//一天以上旷工
	private String leaveEarlyLateCount;//迟到早退次数
	private String actualAttendanceDays;//实际出勤
	private String wagesLevelMoney;//工资标准
	private String baseWagesMoney;//基本工资
	private String postWagesMoney;//职务工资
	private String technicalWagesMoney;//技术工资
	private String confidentialityAllowanceMoney;//保密津贴
	private String technicalAllowanceMoney;//技术津贴
	private String achievementBonusMoney;//绩效奖金
	private String correctionDeductionsMoney;//转正扣款
	private String monthWagesMoney;//月工资
	private String entryAgeExpense;//入职年限
	private String carExpenseMoney;//交通补贴
	private String hotelExpenseMoney;//住宿补贴
	private String jobAgeExpenseMoney;//司龄补贴
	private String mealsExpenseMoney;//餐费补贴
	private String communicationExpenseMoney;//通讯补贴
	private String otherExpenseMoney;//其他补贴
	private String overtimePayMoney;//加班费
	private String missingDeductionMoney;//缺勤扣款
	private String sickDeductionMoney;//病假扣款
	private String casualDeductionMoney;//事假扣款
	private String absentDeductionMoney;//旷工扣款
	private String earlyLateDeductionMoney;//迟到早退扣款
	private String supplementItemsMoney;//补杂项
	private String allWagesMoney;//应发工资
	private String socialPensionDeductionMoney;//养老扣款
	private String socialUnemploymentDeductionMoney;//失业扣款
	private String socialMedicalDeductionMoney;//医疗扣款
	private String socialProvidentFundDeductionMoney;//公积金扣款
	private String socialOtherDeductionMoney;//其他项扣款
	private String socialTotalDeductionMoney;//保险扣款合计
	private String grossWagesMoney;//税前工资
	private String specialChildrenEducationMoney;//子女教育
	private String specialContinuEducationMoney;//继续教育
	private String specialHotelInterestMoney;//住房贷款利息
	private String specialHotelRentMoney;//住房租金
	private String specialSupportElderlyMoney;//赡养老人
	private String specialCommercialHealthInsuranceMoney;//商业健康险
	private String taxableWages;//纳税工资
	private String personalIncomeMoney;//个人所得税
	private String realWagesMoney;//实发工资
	private String remark;//备注
	private String delFlag = "0";//删除标识(0:正常 1:删除)
	private String sendCount = "0"; //发送次数
	private String attr1;//备注
	private String attr2;//备注
	private String attr3;//备注
	private String attr4;//备注
	private String attr5;//备注
	private String attr6;//备注
	private PersonSalarySupport personSalarySupport;
	
	public PersonSalaryBase() {
		super();
	}

	public PersonSalaryBase(String salaryDate, String salaryYear,
			String salaryMonth, String employeeNo, String personName,
			String contractCompanyId, String contractCompanyName,
			String personId, String idcardNum, String personLevel,
			Date entryDate, String allAttendanceDays, String paidTripDays,
			String paidLieuLeaveDays, String paidAnnualLeaveDays,
			String paidMaternityLeaveDays, String onePointFiveOvertimeDays,
			String twoOvertimeDays, String threeOvertimeDays,
			String missingAttendanceDays, String sickLeaveDays,
			String casualLeaveDays, String halfAbsentDays,
			String oneAbsentDays, String leaveEarlyLateCount,
			String actualAttendanceDays, String wagesLevelMoney,
			String baseWagesMoney, String postWagesMoney,
			String technicalWagesMoney, String confidentialityAllowanceMoney,
			String technicalAllowanceMoney, String achievementBonusMoney,
			String correctionDeductionsMoney, String monthWagesMoney,
			String entryAgeExpense, String carExpenseMoney,
			String hotelExpenseMoney, String jobAgeExpenseMoney,
			String mealsExpenseMoney, String communicationExpenseMoney,
			String otherExpenseMoney, String overtimePayMoney,
			String missingDeductionMoney, String sickDeductionMoney,
			String casualDeductionMoney, String absentDeductionMoney,
			String earlyLateDeductionMoney, String supplementItemsMoney,
			String allWagesMoney, String socialPensionDeductionMoney,
			String socialUnemploymentDeductionMoney,
			String socialMedicalDeductionMoney,
			String socialProvidentFundDeductionMoney,
			String socialOtherDeductionMoney, String socialTotalDeductionMoney,
			String grossWagesMoney, String specialChildrenEducationMoney,
			String specialContinuEducationMoney,
			String specialHotelInterestMoney, String specialHotelRentMoney,
			String specialSupportElderlyMoney,
			String specialCommercialHealthInsuranceMoney, String taxableWages,
			String personalIncomeMoney, String realWagesMoney, String remark) {
		super();
		this.salaryDate = salaryDate;
		this.salaryYear = salaryYear;
		this.salaryMonth = salaryMonth;
		this.employeeNo = employeeNo;
		this.personName = personName;
		this.contractCompanyId = contractCompanyId;
		this.contractCompanyName = contractCompanyName;
		this.personId = personId;
		this.idcardNum = idcardNum;
		this.personLevel = personLevel;
		this.entryDate = entryDate;
		this.allAttendanceDays = allAttendanceDays;
		this.paidTripDays = paidTripDays;
		this.paidLieuLeaveDays = paidLieuLeaveDays;
		this.paidAnnualLeaveDays = paidAnnualLeaveDays;
		this.paidMaternityLeaveDays = paidMaternityLeaveDays;
		this.onePointFiveOvertimeDays = onePointFiveOvertimeDays;
		this.twoOvertimeDays = twoOvertimeDays;
		this.threeOvertimeDays = threeOvertimeDays;
		this.missingAttendanceDays = missingAttendanceDays;
		this.sickLeaveDays = sickLeaveDays;
		this.casualLeaveDays = casualLeaveDays;
		this.halfAbsentDays = halfAbsentDays;
		this.oneAbsentDays = oneAbsentDays;
		this.leaveEarlyLateCount = leaveEarlyLateCount;
		this.actualAttendanceDays = actualAttendanceDays;
		this.wagesLevelMoney = wagesLevelMoney;
		this.baseWagesMoney = baseWagesMoney;
		this.postWagesMoney = postWagesMoney;
		this.technicalWagesMoney = technicalWagesMoney;
		this.confidentialityAllowanceMoney = confidentialityAllowanceMoney;
		this.technicalAllowanceMoney = technicalAllowanceMoney;
		this.achievementBonusMoney = achievementBonusMoney;
		this.correctionDeductionsMoney = correctionDeductionsMoney;
		this.monthWagesMoney = monthWagesMoney;
		this.entryAgeExpense = entryAgeExpense;
		this.carExpenseMoney = carExpenseMoney;
		this.hotelExpenseMoney = hotelExpenseMoney;
		this.jobAgeExpenseMoney = jobAgeExpenseMoney;
		this.mealsExpenseMoney = mealsExpenseMoney;
		this.communicationExpenseMoney = communicationExpenseMoney;
		this.otherExpenseMoney = otherExpenseMoney;
		this.overtimePayMoney = overtimePayMoney;
		this.missingDeductionMoney = missingDeductionMoney;
		this.sickDeductionMoney = sickDeductionMoney;
		this.casualDeductionMoney = casualDeductionMoney;
		this.absentDeductionMoney = absentDeductionMoney;
		this.earlyLateDeductionMoney = earlyLateDeductionMoney;
		this.supplementItemsMoney = supplementItemsMoney;
		this.allWagesMoney = allWagesMoney;
		this.socialPensionDeductionMoney = socialPensionDeductionMoney;
		this.socialUnemploymentDeductionMoney = socialUnemploymentDeductionMoney;
		this.socialMedicalDeductionMoney = socialMedicalDeductionMoney;
		this.socialProvidentFundDeductionMoney = socialProvidentFundDeductionMoney;
		this.socialOtherDeductionMoney = socialOtherDeductionMoney;
		this.socialTotalDeductionMoney = socialTotalDeductionMoney;
		this.grossWagesMoney = grossWagesMoney;
		this.specialChildrenEducationMoney = specialChildrenEducationMoney;
		this.specialContinuEducationMoney = specialContinuEducationMoney;
		this.specialHotelInterestMoney = specialHotelInterestMoney;
		this.specialHotelRentMoney = specialHotelRentMoney;
		this.specialSupportElderlyMoney = specialSupportElderlyMoney;
		this.specialCommercialHealthInsuranceMoney = specialCommercialHealthInsuranceMoney;
		this.taxableWages = taxableWages;
		this.personalIncomeMoney = personalIncomeMoney;
		this.realWagesMoney = realWagesMoney;
		this.remark = remark;
	}

	
	public PersonSalaryBase(String personLevel, String allAttendanceDays,
			String paidTripDays, String paidLieuLeaveDays,
			String paidAnnualLeaveDays, String paidMaternityLeaveDays,
			String onePointFiveOvertimeDays, String twoOvertimeDays,
			String threeOvertimeDays, String missingAttendanceDays,
			String sickLeaveDays, String casualLeaveDays,
			String halfAbsentDays, String oneAbsentDays,
			String leaveEarlyLateCount, String actualAttendanceDays,
			String wagesLevelMoney, String baseWagesMoney,
			String postWagesMoney, String technicalWagesMoney,
			String confidentialityAllowanceMoney,
			String technicalAllowanceMoney, String achievementBonusMoney,
			String correctionDeductionsMoney, String monthWagesMoney,
			String entryAgeExpense, String carExpenseMoney,
			String hotelExpenseMoney, String jobAgeExpenseMoney,
			String mealsExpenseMoney, String communicationExpenseMoney,
			String otherExpenseMoney, String overtimePayMoney,
			String missingDeductionMoney, String sickDeductionMoney,
			String casualDeductionMoney, String absentDeductionMoney,
			String earlyLateDeductionMoney, String supplementItemsMoney,
			String allWagesMoney, String socialPensionDeductionMoney,
			String socialUnemploymentDeductionMoney,
			String socialMedicalDeductionMoney,
			String socialProvidentFundDeductionMoney,
			String socialOtherDeductionMoney, String socialTotalDeductionMoney,
			String grossWagesMoney, String specialChildrenEducationMoney,
			String specialContinuEducationMoney,
			String specialHotelInterestMoney, String specialHotelRentMoney,
			String specialSupportElderlyMoney,
			String specialCommercialHealthInsuranceMoney, String taxableWages,
			String personalIncomeMoney, String realWagesMoney, String remark) {
		super();
		this.personLevel = personLevel;
		this.allAttendanceDays = allAttendanceDays;
		this.paidTripDays = paidTripDays;
		this.paidLieuLeaveDays = paidLieuLeaveDays;
		this.paidAnnualLeaveDays = paidAnnualLeaveDays;
		this.paidMaternityLeaveDays = paidMaternityLeaveDays;
		this.onePointFiveOvertimeDays = onePointFiveOvertimeDays;
		this.twoOvertimeDays = twoOvertimeDays;
		this.threeOvertimeDays = threeOvertimeDays;
		this.missingAttendanceDays = missingAttendanceDays;
		this.sickLeaveDays = sickLeaveDays;
		this.casualLeaveDays = casualLeaveDays;
		this.halfAbsentDays = halfAbsentDays;
		this.oneAbsentDays = oneAbsentDays;
		this.leaveEarlyLateCount = leaveEarlyLateCount;
		this.actualAttendanceDays = actualAttendanceDays;
		this.wagesLevelMoney = wagesLevelMoney;
		this.baseWagesMoney = baseWagesMoney;
		this.postWagesMoney = postWagesMoney;
		this.technicalWagesMoney = technicalWagesMoney;
		this.confidentialityAllowanceMoney = confidentialityAllowanceMoney;
		this.technicalAllowanceMoney = technicalAllowanceMoney;
		this.achievementBonusMoney = achievementBonusMoney;
		this.correctionDeductionsMoney = correctionDeductionsMoney;
		this.monthWagesMoney = monthWagesMoney;
		this.entryAgeExpense = entryAgeExpense;
		this.carExpenseMoney = carExpenseMoney;
		this.hotelExpenseMoney = hotelExpenseMoney;
		this.jobAgeExpenseMoney = jobAgeExpenseMoney;
		this.mealsExpenseMoney = mealsExpenseMoney;
		this.communicationExpenseMoney = communicationExpenseMoney;
		this.otherExpenseMoney = otherExpenseMoney;
		this.overtimePayMoney = overtimePayMoney;
		this.missingDeductionMoney = missingDeductionMoney;
		this.sickDeductionMoney = sickDeductionMoney;
		this.casualDeductionMoney = casualDeductionMoney;
		this.absentDeductionMoney = absentDeductionMoney;
		this.earlyLateDeductionMoney = earlyLateDeductionMoney;
		this.supplementItemsMoney = supplementItemsMoney;
		this.allWagesMoney = allWagesMoney;
		this.socialPensionDeductionMoney = socialPensionDeductionMoney;
		this.socialUnemploymentDeductionMoney = socialUnemploymentDeductionMoney;
		this.socialMedicalDeductionMoney = socialMedicalDeductionMoney;
		this.socialProvidentFundDeductionMoney = socialProvidentFundDeductionMoney;
		this.socialOtherDeductionMoney = socialOtherDeductionMoney;
		this.socialTotalDeductionMoney = socialTotalDeductionMoney;
		this.grossWagesMoney = grossWagesMoney;
		this.specialChildrenEducationMoney = specialChildrenEducationMoney;
		this.specialContinuEducationMoney = specialContinuEducationMoney;
		this.specialHotelInterestMoney = specialHotelInterestMoney;
		this.specialHotelRentMoney = specialHotelRentMoney;
		this.specialSupportElderlyMoney = specialSupportElderlyMoney;
		this.specialCommercialHealthInsuranceMoney = specialCommercialHealthInsuranceMoney;
		this.taxableWages = taxableWages;
		this.personalIncomeMoney = personalIncomeMoney;
		this.realWagesMoney = realWagesMoney;
		this.remark = remark;
	}

	public PersonSalaryBase(Long id, String salaryDate, String salaryYear,
			String salaryMonth, String employeeNo, String personName,
			String contractCompanyId, String contractCompanyName,
			String personId, String idcardNum, String personLevel,
			Date entryDate, String allAttendanceDays, String paidTripDays,
			String paidLieuLeaveDays, String paidAnnualLeaveDays,
			String paidMaternityLeaveDays, String onePointFiveOvertimeDays,
			String twoOvertimeDays, String threeOvertimeDays,
			String missingAttendanceDays, String sickLeaveDays,
			String casualLeaveDays, String halfAbsentDays,
			String oneAbsentDays, String leaveEarlyLateCount,
			String actualAttendanceDays, String wagesLevelMoney,
			String baseWagesMoney, String postWagesMoney,
			String technicalWagesMoney, String confidentialityAllowanceMoney,
			String technicalAllowanceMoney, String achievementBonusMoney,
			String correctionDeductionsMoney, String monthWagesMoney,
			String entryAgeExpense, String carExpenseMoney,
			String hotelExpenseMoney, String jobAgeExpenseMoney,
			String mealsExpenseMoney, String communicationExpenseMoney,
			String otherExpenseMoney, String overtimePayMoney,
			String missingDeductionMoney, String sickDeductionMoney,
			String casualDeductionMoney, String absentDeductionMoney,
			String earlyLateDeductionMoney, String supplementItemsMoney,
			String allWagesMoney, String socialPensionDeductionMoney,
			String socialUnemploymentDeductionMoney,
			String socialMedicalDeductionMoney,
			String socialProvidentFundDeductionMoney,
			String socialOtherDeductionMoney, String socialTotalDeductionMoney,
			String grossWagesMoney, String specialChildrenEducationMoney,
			String specialContinuEducationMoney,
			String specialHotelInterestMoney, String specialHotelRentMoney,
			String specialSupportElderlyMoney,
			String specialCommercialHealthInsuranceMoney, String taxableWages,
			String personalIncomeMoney, String realWagesMoney, String remark,
			String attr1, String attr2, String attr3, String attr4,
			String attr5, String attr6, PersonSalarySupport personSalarySupport) {
		super();
		this.id = id;
		this.salaryDate = salaryDate;
		this.salaryYear = salaryYear;
		this.salaryMonth = salaryMonth;
		this.employeeNo = employeeNo;
		this.personName = personName;
		this.contractCompanyId = contractCompanyId;
		this.contractCompanyName = contractCompanyName;
		this.personId = personId;
		this.idcardNum = idcardNum;
		this.personLevel = personLevel;
		this.entryDate = entryDate;
		this.allAttendanceDays = allAttendanceDays;
		this.paidTripDays = paidTripDays;
		this.paidLieuLeaveDays = paidLieuLeaveDays;
		this.paidAnnualLeaveDays = paidAnnualLeaveDays;
		this.paidMaternityLeaveDays = paidMaternityLeaveDays;
		this.onePointFiveOvertimeDays = onePointFiveOvertimeDays;
		this.twoOvertimeDays = twoOvertimeDays;
		this.threeOvertimeDays = threeOvertimeDays;
		this.missingAttendanceDays = missingAttendanceDays;
		this.sickLeaveDays = sickLeaveDays;
		this.casualLeaveDays = casualLeaveDays;
		this.halfAbsentDays = halfAbsentDays;
		this.oneAbsentDays = oneAbsentDays;
		this.leaveEarlyLateCount = leaveEarlyLateCount;
		this.actualAttendanceDays = actualAttendanceDays;
		this.wagesLevelMoney = wagesLevelMoney;
		this.baseWagesMoney = baseWagesMoney;
		this.postWagesMoney = postWagesMoney;
		this.technicalWagesMoney = technicalWagesMoney;
		this.confidentialityAllowanceMoney = confidentialityAllowanceMoney;
		this.technicalAllowanceMoney = technicalAllowanceMoney;
		this.achievementBonusMoney = achievementBonusMoney;
		this.correctionDeductionsMoney = correctionDeductionsMoney;
		this.monthWagesMoney = monthWagesMoney;
		this.entryAgeExpense = entryAgeExpense;
		this.carExpenseMoney = carExpenseMoney;
		this.hotelExpenseMoney = hotelExpenseMoney;
		this.jobAgeExpenseMoney = jobAgeExpenseMoney;
		this.mealsExpenseMoney = mealsExpenseMoney;
		this.communicationExpenseMoney = communicationExpenseMoney;
		this.otherExpenseMoney = otherExpenseMoney;
		this.overtimePayMoney = overtimePayMoney;
		this.missingDeductionMoney = missingDeductionMoney;
		this.sickDeductionMoney = sickDeductionMoney;
		this.casualDeductionMoney = casualDeductionMoney;
		this.absentDeductionMoney = absentDeductionMoney;
		this.earlyLateDeductionMoney = earlyLateDeductionMoney;
		this.supplementItemsMoney = supplementItemsMoney;
		this.allWagesMoney = allWagesMoney;
		this.socialPensionDeductionMoney = socialPensionDeductionMoney;
		this.socialUnemploymentDeductionMoney = socialUnemploymentDeductionMoney;
		this.socialMedicalDeductionMoney = socialMedicalDeductionMoney;
		this.socialProvidentFundDeductionMoney = socialProvidentFundDeductionMoney;
		this.socialOtherDeductionMoney = socialOtherDeductionMoney;
		this.socialTotalDeductionMoney = socialTotalDeductionMoney;
		this.grossWagesMoney = grossWagesMoney;
		this.specialChildrenEducationMoney = specialChildrenEducationMoney;
		this.specialContinuEducationMoney = specialContinuEducationMoney;
		this.specialHotelInterestMoney = specialHotelInterestMoney;
		this.specialHotelRentMoney = specialHotelRentMoney;
		this.specialSupportElderlyMoney = specialSupportElderlyMoney;
		this.specialCommercialHealthInsuranceMoney = specialCommercialHealthInsuranceMoney;
		this.taxableWages = taxableWages;
		this.personalIncomeMoney = personalIncomeMoney;
		this.realWagesMoney = realWagesMoney;
		this.remark = remark;
		this.attr1 = attr1;
		this.attr2 = attr2;
		this.attr3 = attr3;
		this.attr4 = attr4;
		this.attr5 = attr5;
		this.attr6 = attr6;
		this.personSalarySupport = personSalarySupport;
	}




	@Id
    @Column(name = "ID", unique = true, nullable = false)
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	@Column(name = "taxable_wages")
	public String getTaxableWages() {
		return taxableWages;
	}
	
	public void setTaxableWages(String taxableWages) {
		this.taxableWages = taxableWages;
	}
	
	@Column(name = "del_flag")
	public String getDelFlag() {
		return delFlag;
	}

	public void setDelFlag(String delFlag) {
		this.delFlag = delFlag;
	}
	@Column(name = "send_count")
	public String getSendCount() {
		return sendCount;
	}

	public void setSendCount(String sendCount) {
		this.sendCount = sendCount;
	}

	@OneToOne(cascade = CascadeType.ALL, mappedBy = "personSalaryBase")
	public PersonSalarySupport getPersonSalarySupport() {
		return personSalarySupport;
	}

	public void setPersonSalarySupport(PersonSalarySupport personSalarySupport) {
		this.personSalarySupport = personSalarySupport;
	}

	@Column(name = "salary_date")
	public String getSalaryDate() {
		return salaryDate;
	}
	public void setSalaryDate(String salaryDate) {
		this.salaryDate = salaryDate;
	}
	@Column(name = "salary_year")
	public String getSalaryYear() {
		return salaryYear;
	}
	public void setSalaryYear(String salaryYear) {
		this.salaryYear = salaryYear;
	}
	@Column(name = "salary_month")
	public String getSalaryMonth() {
		return salaryMonth;
	}
	public void setSalaryMonth(String salaryMonth) {
		this.salaryMonth = salaryMonth;
	}
	@Column(name = "employee_no")
	public String getEmployeeNo() {
		return employeeNo;
	}
	public void setEmployeeNo(String employeeNo) {
		this.employeeNo = employeeNo;
	}
	@Column(name = "person_name")
	public String getPersonName() {
		return personName;
	}
	public void setPersonName(String personName) {
		this.personName = personName;
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
	@Column(name = "person_id")
	public String getPersonId() {
		return personId;
	}
	public void setPersonId(String personId) {
		this.personId = personId;
	}
	@Column(name = "idcard_num")
	public String getIdcardNum() {
		return idcardNum;
	}
	public void setIdcardNum(String idcardNum) {
		this.idcardNum = idcardNum;
	}
	@Column(name = "person_level")
	public String getPersonLevel() {
		return personLevel;
	}
	public void setPersonLevel(String personLevel) {
		this.personLevel = personLevel;
	}
	@Column(name = "entry_date")
	public Date getEntryDate() {
		return entryDate;
	}
	public void setEntryDate(Date entryDate) {
		this.entryDate = entryDate;
	}
	@Column(name = "all_attendance_days")
	public String getAllAttendanceDays() {
		return allAttendanceDays;
	}
	public void setAllAttendanceDays(String allAttendanceDays) {
		this.allAttendanceDays = allAttendanceDays;
	}
	@Column(name = "paid_trip_days")
	public String getPaidTripDays() {
		return paidTripDays;
	}
	public void setPaidTripDays(String paidTripDays) {
		this.paidTripDays = paidTripDays;
	}
	@Column(name = "paid_lieu_leave_days")
	public String getPaidLieuLeaveDays() {
		return paidLieuLeaveDays;
	}
	public void setPaidLieuLeaveDays(String paidLieuLeaveDays) {
		this.paidLieuLeaveDays = paidLieuLeaveDays;
	}
	@Column(name = "paid_annual_leave_days")
	public String getPaidAnnualLeaveDays() {
		return paidAnnualLeaveDays;
	}
	public void setPaidAnnualLeaveDays(String paidAnnualLeaveDays) {
		this.paidAnnualLeaveDays = paidAnnualLeaveDays;
	}
	@Column(name = "paid_maternity_leave_days")
	public String getPaidMaternityLeaveDays() {
		return paidMaternityLeaveDays;
	}
	public void setPaidMaternityLeaveDays(String paidMaternityLeaveDays) {
		this.paidMaternityLeaveDays = paidMaternityLeaveDays;
	}
	@Column(name = "one_point_five_overtime_days")
	public String getOnePointFiveOvertimeDays() {
		return onePointFiveOvertimeDays;
	}
	public void setOnePointFiveOvertimeDays(String onePointFiveOvertimeDays) {
		this.onePointFiveOvertimeDays = onePointFiveOvertimeDays;
	}
	@Column(name = "two_overtime_days")
	public String getTwoOvertimeDays() {
		return twoOvertimeDays;
	}
	public void setTwoOvertimeDays(String twoOvertimeDays) {
		this.twoOvertimeDays = twoOvertimeDays;
	}
	@Column(name = "three_overtime_days")
	public String getThreeOvertimeDays() {
		return threeOvertimeDays;
	}
	public void setThreeOvertimeDays(String threeOvertimeDays) {
		this.threeOvertimeDays = threeOvertimeDays;
	}
	@Column(name = "missing_attendance_days")
	public String getMissingAttendanceDays() {
		return missingAttendanceDays;
	}
	public void setMissingAttendanceDays(String missingAttendanceDays) {
		this.missingAttendanceDays = missingAttendanceDays;
	}
	@Column(name = "sick_leave_days")
	public String getSickLeaveDays() {
		return sickLeaveDays;
	}
	public void setSickLeaveDays(String sickLeaveDays) {
		this.sickLeaveDays = sickLeaveDays;
	}
	@Column(name = "casual_leave_days")
	public String getCasualLeaveDays() {
		return casualLeaveDays;
	}
	public void setCasualLeaveDays(String casualLeaveDays) {
		this.casualLeaveDays = casualLeaveDays;
	}
	@Column(name = "half_absent_days")
	public String getHalfAbsentDays() {
		return halfAbsentDays;
	}
	public void setHalfAbsentDays(String halfAbsentDays) {
		this.halfAbsentDays = halfAbsentDays;
	}
	@Column(name = "one_absent_days")
	public String getOneAbsentDays() {
		return oneAbsentDays;
	}
	public void setOneAbsentDays(String oneAbsentDays) {
		this.oneAbsentDays = oneAbsentDays;
	}
	@Column(name = "leave_early_late_count")
	public String getLeaveEarlyLateCount() {
		return leaveEarlyLateCount;
	}
	public void setLeaveEarlyLateCount(String leaveEarlyLateCount) {
		this.leaveEarlyLateCount = leaveEarlyLateCount;
	}
	@Column(name = "actual_attendance_days")
	public String getActualAttendanceDays() {
		return actualAttendanceDays;
	}
	public void setActualAttendanceDays(String actualAttendanceDays) {
		this.actualAttendanceDays = actualAttendanceDays;
	}
	@Column(name = "wages_level_money")
	public String getWagesLevelMoney() {
		return wagesLevelMoney;
	}
	public void setWagesLevelMoney(String wagesLevelMoney) {
		this.wagesLevelMoney = wagesLevelMoney;
	}
	@Column(name = "base_wages_money")
	public String getBaseWagesMoney() {
		return baseWagesMoney;
	}
	public void setBaseWagesMoney(String baseWagesMoney) {
		this.baseWagesMoney = baseWagesMoney;
	}
	@Column(name = "post_wages_money")
	public String getPostWagesMoney() {
		return postWagesMoney;
	}
	public void setPostWagesMoney(String postWagesMoney) {
		this.postWagesMoney = postWagesMoney;
	}
	@Column(name = "technical_wages_money")
	public String getTechnicalWagesMoney() {
		return technicalWagesMoney;
	}
	public void setTechnicalWagesMoney(String technicalWagesMoney) {
		this.technicalWagesMoney = technicalWagesMoney;
	}
	@Column(name = "confidentiality_allowance_money")
	public String getConfidentialityAllowanceMoney() {
		return confidentialityAllowanceMoney;
	}
	public void setConfidentialityAllowanceMoney(
			String confidentialityAllowanceMoney) {
		this.confidentialityAllowanceMoney = confidentialityAllowanceMoney;
	}
	@Column(name = "technical_allowance_money")
	public String getTechnicalAllowanceMoney() {
		return technicalAllowanceMoney;
	}
	public void setTechnicalAllowanceMoney(String technicalAllowanceMoney) {
		this.technicalAllowanceMoney = technicalAllowanceMoney;
	}
	@Column(name = "achievement_bonus_money")
	public String getAchievementBonusMoney() {
		return achievementBonusMoney;
	}
	public void setAchievementBonusMoney(String achievementBonusMoney) {
		this.achievementBonusMoney = achievementBonusMoney;
	}
	@Column(name = "correction_deductions_money")
	public String getCorrectionDeductionsMoney() {
		return correctionDeductionsMoney;
	}
	public void setCorrectionDeductionsMoney(String correctionDeductionsMoney) {
		this.correctionDeductionsMoney = correctionDeductionsMoney;
	}
	@Column(name = "month_wages_money")
	public String getMonthWagesMoney() {
		return monthWagesMoney;
	}
	public void setMonthWagesMoney(String monthWagesMoney) {
		this.monthWagesMoney = monthWagesMoney;
	}
	@Column(name = "entry_age_expense")
	public String getEntryAgeExpense() {
		return entryAgeExpense;
	}
	public void setEntryAgeExpense(String entryAgeExpense) {
		this.entryAgeExpense = entryAgeExpense;
	}
	@Column(name = "car_expense_money")
	public String getCarExpenseMoney() {
		return carExpenseMoney;
	}
	public void setCarExpenseMoney(String carExpenseMoney) {
		this.carExpenseMoney = carExpenseMoney;
	}
	@Column(name = "hotel_expense_money")
	public String getHotelExpenseMoney() {
		return hotelExpenseMoney;
	}
	public void setHotelExpenseMoney(String hotelExpenseMoney) {
		this.hotelExpenseMoney = hotelExpenseMoney;
	}
	@Column(name = "job_age_expense_money")
	public String getJobAgeExpenseMoney() {
		return jobAgeExpenseMoney;
	}
	public void setJobAgeExpenseMoney(String jobAgeExpenseMoney) {
		this.jobAgeExpenseMoney = jobAgeExpenseMoney;
	}
	@Column(name = "meals_expense_money")
	public String getMealsExpenseMoney() {
		return mealsExpenseMoney;
	}
	public void setMealsExpenseMoney(String mealsExpenseMoney) {
		this.mealsExpenseMoney = mealsExpenseMoney;
	}
	@Column(name = "communication_expense_money")
	public String getCommunicationExpenseMoney() {
		return communicationExpenseMoney;
	}
	public void setCommunicationExpenseMoney(String communicationExpenseMoney) {
		this.communicationExpenseMoney = communicationExpenseMoney;
	}
	@Column(name = "other_expense_money")
	public String getOtherExpenseMoney() {
		return otherExpenseMoney;
	}
	public void setOtherExpenseMoney(String otherExpenseMoney) {
		this.otherExpenseMoney = otherExpenseMoney;
	}
	@Column(name = "overtime_pay_money")
	public String getOvertimePayMoney() {
		return overtimePayMoney;
	}
	public void setOvertimePayMoney(String overtimePayMoney) {
		this.overtimePayMoney = overtimePayMoney;
	}
	@Column(name = "missing_deduction_money")
	public String getMissingDeductionMoney() {
		return missingDeductionMoney;
	}
	public void setMissingDeductionMoney(String missingDeductionMoney) {
		this.missingDeductionMoney = missingDeductionMoney;
	}
	@Column(name = "sick_deduction_money")
	public String getSickDeductionMoney() {
		return sickDeductionMoney;
	}
	public void setSickDeductionMoney(String sickDeductionMoney) {
		this.sickDeductionMoney = sickDeductionMoney;
	}
	@Column(name = "casual_deduction_money")
	public String getCasualDeductionMoney() {
		return casualDeductionMoney;
	}
	public void setCasualDeductionMoney(String casualDeductionMoney) {
		this.casualDeductionMoney = casualDeductionMoney;
	}
	@Column(name = "absent_deduction_money")
	public String getAbsentDeductionMoney() {
		return absentDeductionMoney;
	}
	public void setAbsentDeductionMoney(String absentDeductionMoney) {
		this.absentDeductionMoney = absentDeductionMoney;
	}
	@Column(name = "early_late_deduction_money")
	public String getEarlyLateDeductionMoney() {
		return earlyLateDeductionMoney;
	}
	public void setEarlyLateDeductionMoney(String earlyLateDeductionMoney) {
		this.earlyLateDeductionMoney = earlyLateDeductionMoney;
	}
	@Column(name = "supplement_items_money")
	public String getSupplementItemsMoney() {
		return supplementItemsMoney;
	}
	public void setSupplementItemsMoney(String supplementItemsMoney) {
		this.supplementItemsMoney = supplementItemsMoney;
	}
	@Column(name = "all_wages_money")
	public String getAllWagesMoney() {
		return allWagesMoney;
	}
	public void setAllWagesMoney(String allWagesMoney) {
		this.allWagesMoney = allWagesMoney;
	}
	@Column(name = "social_pension_deduction_money")
	public String getSocialPensionDeductionMoney() {
		return socialPensionDeductionMoney;
	}
	public void setSocialPensionDeductionMoney(String socialPensionDeductionMoney) {
		this.socialPensionDeductionMoney = socialPensionDeductionMoney;
	}
	@Column(name = "social_unemployment_deduction_money")
	public String getSocialUnemploymentDeductionMoney() {
		return socialUnemploymentDeductionMoney;
	}
	public void setSocialUnemploymentDeductionMoney(
			String socialUnemploymentDeductionMoney) {
		this.socialUnemploymentDeductionMoney = socialUnemploymentDeductionMoney;
	}
	@Column(name = "social_medical_deduction_money")
	public String getSocialMedicalDeductionMoney() {
		return socialMedicalDeductionMoney;
	}
	public void setSocialMedicalDeductionMoney(String socialMedicalDeductionMoney) {
		this.socialMedicalDeductionMoney = socialMedicalDeductionMoney;
	}
	@Column(name = "social_provident_fund_deduction_money")
	public String getSocialProvidentFundDeductionMoney() {
		return socialProvidentFundDeductionMoney;
	}
	public void setSocialProvidentFundDeductionMoney(
			String socialProvidentFundDeductionMoney) {
		this.socialProvidentFundDeductionMoney = socialProvidentFundDeductionMoney;
	}
	@Column(name = "social_other_deduction_money")
	public String getSocialOtherDeductionMoney() {
		return socialOtherDeductionMoney;
	}
	public void setSocialOtherDeductionMoney(String socialOtherDeductionMoney) {
		this.socialOtherDeductionMoney = socialOtherDeductionMoney;
	}
	@Column(name = "social_total_deduction_money")
	public String getSocialTotalDeductionMoney() {
		return socialTotalDeductionMoney;
	}
	public void setSocialTotalDeductionMoney(String socialTotalDeductionMoney) {
		this.socialTotalDeductionMoney = socialTotalDeductionMoney;
	}
	@Column(name = "gross_wages_money")
	public String getGrossWagesMoney() {
		return grossWagesMoney;
	}
	public void setGrossWagesMoney(String grossWagesMoney) {
		this.grossWagesMoney = grossWagesMoney;
	}
	@Column(name = "special_children_education_money")
	public String getSpecialChildrenEducationMoney() {
		return specialChildrenEducationMoney;
	}
	public void setSpecialChildrenEducationMoney(
			String specialChildrenEducationMoney) {
		this.specialChildrenEducationMoney = specialChildrenEducationMoney;
	}
	@Column(name = "special_continu_education_money")
	public String getSpecialContinuEducationMoney() {
		return specialContinuEducationMoney;
	}
	public void setSpecialContinuEducationMoney(String specialContinuEducationMoney) {
		this.specialContinuEducationMoney = specialContinuEducationMoney;
	}
	@Column(name = "special_hotel_interest_money")
	public String getSpecialHotelInterestMoney() {
		return specialHotelInterestMoney;
	}
	public void setSpecialHotelInterestMoney(String specialHotelInterestMoney) {
		this.specialHotelInterestMoney = specialHotelInterestMoney;
	}
	@Column(name = "special_hotel_rent_money")
	public String getSpecialHotelRentMoney() {
		return specialHotelRentMoney;
	}
	public void setSpecialHotelRentMoney(String specialHotelRentMoney) {
		this.specialHotelRentMoney = specialHotelRentMoney;
	}
	@Column(name = "special_support_elderly_money")
	public String getSpecialSupportElderlyMoney() {
		return specialSupportElderlyMoney;
	}
	public void setSpecialSupportElderlyMoney(String specialSupportElderlyMoney) {
		this.specialSupportElderlyMoney = specialSupportElderlyMoney;
	}
	@Column(name = "special_commercial_health_insurance_money")
	public String getSpecialCommercialHealthInsuranceMoney() {
		return specialCommercialHealthInsuranceMoney;
	}
	public void setSpecialCommercialHealthInsuranceMoney(
			String specialCommercialHealthInsuranceMoney) {
		this.specialCommercialHealthInsuranceMoney = specialCommercialHealthInsuranceMoney;
	}
	@Column(name = "personal_income_money")
	public String getPersonalIncomeMoney() {
		return personalIncomeMoney;
	}
	public void setPersonalIncomeMoney(String personalIncomeMoney) {
		this.personalIncomeMoney = personalIncomeMoney;
	}
	@Column(name = "real_wages_money")
	public String getRealWagesMoney() {
		return realWagesMoney;
	}
	public void setRealWagesMoney(String realWagesMoney) {
		this.realWagesMoney = realWagesMoney;
	}
	@Column(name = "remark")
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
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
