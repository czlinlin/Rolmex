package com.mossle.user.persistence.domain;

// Generated by Hibernate Tools
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * PersonInfo 个人信息.
 * 
 * @author Lingo
 */
public class PersonInfoDTOForExport implements java.io.Serializable {
    private static final long serialVersionUID = 0L;

    /** 主键. */
    private Long id;

    /** 唯一标识. */
    private String code;

    /** 账号. */
    private String username;

    /** 姓氏. */
    private String familyName;

    /** 名字. */
    private String givenName;

    /** 全名. */
    private String fullName;
    
    private String realName;
    
    /** 曾用名. */
    private String nameBefore;

    /** 联系电话2. */
    private String cellphone;

    /** 联系电话1. */
    private String telephone;

    /** 邮箱. */
    private String email;

    /** 即时通讯. */
    private String im;

    /** 国家. */
    private String country;

    /** 省. */
    private String province;

    /** 市. */
    private String city;

    /** 办公楼. */
    private String building;

    /** 楼层. */
    private String floor;

    /** 工位. */
    private String seat;

    /** 工号-部门缩写. */
    private String employeeNoDepart;
    
    /** 工号-数字部分. */
    private String employeeNoNum;
    
    /** 工号-完整拼接. */
    private String employeeNo;

    /** 员工类型. */
    private String employeeType;

    /** 工卡. */
    private String card;

    /** 公司编码. */
    private String companyCode;

    /** 公司名称. */
    private String companyName;

    /** 部门编码. */
    private String departmentCode;

    /** 部门名称. */
    private String departmentName;

    /** 职位编码. */
    private String positionCode;

    /** 职位名称. */
    private String positionName;

    /** 性别. */
    private String gender;

    /** 生日. */
    private Date birthday;

    /** 证件类型. */
    private String idCardType;

    /** 证件编号. */
    private String idCardValue;

    /** 国籍. */
    private String nationality;

    /** 星座. */
    private String star;

    /** 血型. */
    private String blood;

    /** 衣服大小. */
    private String clothSize;

    /** 租户. */
    private String tenantId;

    /** 现住址. */
    private String address;
    
    /** 停用启用标识. */
    private String stopFlag;
    
    /** 删除标识. */
    private String delFlag;
    
    /** 离职在职标识. */
    private String quitFlag;
    
    /** 添加日期. */
    private Date addTime;
    
    /** 是否兼职. */
    private String jobStatus;
    
    /** 排序. */
    private Integer priority;
    
    private Long partyId;
    
    /** 离职日期. */
    private Date quitTime;
    
    /** 复职日期. */
    private Date resumeTime;
    
    /** 紧急联系人及电话  **/
    private String fax;
    
    /** 微信号  **/
    private String wxNo;
    
    /** QQ **/
    private String qq;
    
    /** 联系电话公开方式 **/ 
    private String secret;
    
    //级别
    private String level ;
    
    //籍贯
    private  String nativePlace;
    
    //户口所在地
    private  String registeredResidence;
    
    //户籍类型
    private  String householdRegisterType;
    
    //政治面貌
    private  String politicalOutlook;
    
    //民族
    private  String nation;
    
    //专业
    private  String major;
    
    //职称
    private  String title;
    
    //学位
    private  String academicDegree;
    //技能特长
    private  String skillSpecialty;
    //用工类型
    private  String laborType;
    //进入方式
    private  String entryMode;
    //学历
    private  String education;
    //婚否
    private  String marriage;
    //生育情况
    private  String fertilityCondition;
    //入职时间
    private  Date entryTime;
    //合同到期时间
    private  Date contractExpirationTime;
    //合同有效期
    private  String contractDeadline;
    
    //合同单位
    private  String contractCompany;
    
    //保险情况
    private  String insurance;
    
    //资料情况
    private  String document;
    
    //身份证号
    private  String identityID;
    
    //备注
    private  String remark;
    
//    //1：流程审核同意   0：流程审核不同意
//    private  String isApproval ;
    
    //家庭成员1
    private  String family_1;
    //家庭成员2
    private  String family_2;
    
    //教育经历1
    private  String educational_experience_1;
    //教育经历2
    private  String educational_experience_2;
    //教育经历3
    private  String educational_experience_3;
    
    //工作经历1
    private  String work_experience_1;
    //工作经历2
    private  String work_experience_2;
    
    private  String postName;
    
    
	public PersonInfoDTOForExport() {
    }

    public PersonInfoDTOForExport(Long id) {
        this.id = id;
    }

    public PersonInfoDTOForExport(Long id, String code, String username, String familyName,
            String givenName, String fullName, String cellphone,
            String telephone, String email, String im, String country,
            String province, String city, String building, String floor,
            String seat,  String employeeType, String card,
            String companyCode, String companyName, String departmentCode,
            String departmentName, String positionCode, String positionName,
            String gender, Date birthday, String idCardType,
            String idCardValue, String nationality, String star, String blood,
            String clothSize, String tenantId, String address) {
        this.id = id;
        this.code = code;
        this.username = username;
        this.familyName = familyName;
        this.givenName = givenName;
        this.fullName = fullName;
        this.cellphone = cellphone;
        this.telephone = telephone;
        this.email = email;
        this.im = im;
        this.country = country;
        this.province = province;
        this.city = city;
        this.building = building;
        this.floor = floor;
        this.seat = seat;
        this.employeeType = employeeType;
        this.card = card;
        this.companyCode = companyCode;
        this.companyName = companyName;
        this.departmentCode = departmentCode;
        this.departmentName = departmentName; 
        this.positionCode = positionCode;
        this.positionName = positionName;
        this.gender = gender;
        this.birthday = birthday;
        this.idCardType = idCardType;
        this.idCardValue = idCardValue;
        this.nationality = nationality;
        this.star = star;
        this.blood = blood;
        this.clothSize = clothSize;
        this.tenantId = tenantId;
        this.address = address;
    }

    /** @return 主键. */
    @Id
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

    /** @return 唯一标识. */
    public String getCode() {
        return this.code;
    }

    /**
     * @param code
     *            唯一标识.
     */
    public void setCode(String code) {
        this.code = code;
    }

    /** @return 账号. */
    public String getUsername() {
        return this.username;
    }

    /**
     * @param username
     *            账号.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /** @return 姓氏. */
    public String getFamilyName() {
        return this.familyName;
    }

    /**
     * @param familyName
     *            姓氏.
     */
    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    /** @return 名字. */
    public String getGivenName() {
        return this.givenName;
    }

    /**
     * @param givenName
     *            名字.
     */
    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    /** @return 全名. */
    public String getFullName() {
        return this.fullName;
    }

    /**
     * @param fullName
     *            全名.
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /** @return 手机. */
    public String getCellphone() {
        return this.cellphone;
    }

    /**
     * @param cellphone
     *            手机.
     */
    public void setCellphone(String cellphone) {
        this.cellphone = cellphone;
    }

    /** @return 座机. */
    public String getTelephone() {
        return this.telephone;
    }

    /**
     * @param telephone
     *            座机.
     */
    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    /** @return 邮箱. */
    public String getEmail() {
        return this.email;
    }

    /**
     * @param email
     *            邮箱.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /** @return 即时通讯. */
    public String getIm() {
        return this.im;
    }

    /**
     * @param im
     *            即时通讯.
     */
    public void setIm(String im) {
        this.im = im;
    }

    /** @return 国家. */
    public String getCountry() {
        return this.country;
    }

    /**
     * @param country
     *            国家.
     */
    public void setCountry(String country) {
        this.country = country;
    }

    /** @return 省. */
    public String getProvince() {
        return this.province;
    }

    /**
     * @param province
     *            省.
     */
    public void setProvince(String province) {
        this.province = province;
    }

    /** @return 市. */
    public String getCity() {
        return this.city;
    }

    /**
     * @param city
     *            市.
     */
    public void setCity(String city) {
        this.city = city;
    }

    /** @return 办公楼. */
    public String getBuilding() {
        return this.building;
    }

    /**
     * @param building
     *            办公楼.
     */
    public void setBuilding(String building) {
        this.building = building;
    }

    /** @return 楼层. */
    public String getFloor() {
        return this.floor;
    }

    /**
     * @param floor
     *            楼层.
     */
    public void setFloor(String floor) {
        this.floor = floor;
    }

    /** @return 工位. */
    public String getSeat() {
        return this.seat;
    }

    /**
     * @param seat
     *            工位.
     */
    public void setSeat(String seat) {
        this.seat = seat;
    }
    
    
    /** @return 工号-部门缩写. */
    public String getEmployeeNoDepart() {
		return employeeNoDepart;
	}

	public void setEmployeeNoDepart(String employeeNoDepart) {
		this.employeeNoDepart = employeeNoDepart;
	}

	  /** 工号-数字部分. */
	public String getEmployeeNoNum() {
		return employeeNoNum;
	}

	public void setEmployeeNoNum(String employeeNoNum) {
		this.employeeNoNum = employeeNoNum;
	}
	
	  /** 工号-完整拼接. */
		public String getEmployeeNo() {
			return employeeNo;
		}

		public void setEmployeeNo(String employeeNo) {
			this.employeeNo = employeeNo;
		}


	/** @return 员工类型. */
    public String getEmployeeType() {
        return this.employeeType;
    }

    /**
     * @param employeeType
     *            员工类型.
     */
    public void setEmployeeType(String employeeType) {
        this.employeeType = employeeType;
    }

    /** @return 工卡. */
    public String getCard() {
        return this.card;
    }

    /**
     * @param card
     *            工卡.
     */
    public void setCard(String card) {
        this.card = card;
    }

    /** @return 公司编码. */
    public String getCompanyCode() {
        return this.companyCode;
    }

    /**
     * @param companyCode
     *            公司编码.
     */
    public void setCompanyCode(String companyCode) {
        this.companyCode = companyCode;
    }

    /** @return 公司名称. */
    public String getCompanyName() {
        return this.companyName;
    }

    /**
     * @param companyName
     *            公司名称.
     */
    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    /** @return 部门编码. */
    public String getDepartmentCode() {
        return this.departmentCode;
    }

    /**
     * @param departmentCode
     *            部门编码.
     */
    public void setDepartmentCode(String departmentCode) {
        this.departmentCode = departmentCode;
    }

    /** @return 部门名称. */
    public String getDepartmentName() {
        return this.departmentName;
    }

    /**
     * @param departmentName
     *            部门名称.
     */
    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    /** @return 职位编码. */
    public String getPositionCode() {
        return this.positionCode;
    }

    /**
     * @param positionCode
     *            职位编码.
     */
    public void setPositionCode(String positionCode) {
        this.positionCode = positionCode;
    }

    /** @return 职位名称. */
    public String getPositionName() {
        return this.positionName;
    }

    /**
     * @param positionName
     *            职位名称.
     */
    public void setPositionName(String positionName) {
        this.positionName = positionName;
    }

    /** @return 性别. */
    public String getGender() {
        return this.gender;
    }

    /**
     * @param gender
     *            性别.
     */
    public void setGender(String gender) {
        this.gender = gender;
    }

    /** @return 生日. */
    public Date getBirthday() {
        return this.birthday;
    }

    /**
     * @param birthday
     *            生日.
     */
    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    /** @return 证件类型. */
    public String getIdCardType() {
        return this.idCardType;
    }

    /**
     * @param idCardType
     *            证件类型.
     */
    public void setIdCardType(String idCardType) {
        this.idCardType = idCardType;
    }

    /** @return 证件编号. */
    public String getIdCardValue() {
        return this.idCardValue;
    }

    /**
     * @param idCardValue
     *            证件编号.
     */
    public void setIdCardValue(String idCardValue) {
        this.idCardValue = idCardValue;
    }

    /** @return 国籍. */
    public String getNationality() {
        return this.nationality;
    }

    /**
     * @param nationality
     *            国籍.
     */
    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    /** @return 星座. */
    public String getStar() {
        return this.star;
    }

    /**
     * @param star
     *            星座.
     */
    public void setStar(String star) {
        this.star = star;
    }

    /** @return 血型. */
    public String getBlood() {
        return this.blood;
    }

    /**
     * @param blood
     *            血型.
     */
    public void setBlood(String blood) {
        this.blood = blood;
    }

    /** @return 衣服大小. */
    public String getClothSize() {
        return this.clothSize;
    }

    /**
     * @param clothSize
     *            衣服大小.
     */
    public void setClothSize(String clothSize) {
        this.clothSize = clothSize;
    }

    /** @return 租户. */
    public String getTenantId() {
        return this.tenantId;
    }

    /**
     * @param tenantId
     *            租户.
     */
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
    
    /** @return 联系地址. */
    public String getAddress() {
        return this.address;
    }

    /**
     * @param address
     *            联系地址.
     */
    public void setAddress(String address) {
        this.address = address;
    }
    
    /** @return 停用启用标识. */
	public String getStopFlag() {
		return stopFlag;
	}

	public void setStopFlag(String stopFlag) {
		this.stopFlag = stopFlag;
	}

	/** @return 删除标识. */
	public String getDelFlag() {
		return delFlag;
	}

	public void setDelFlag(String delFlag) {
		this.delFlag = delFlag;
	}

	/** @return 离职在职标识. */
	public String getQuitFlag() {
		return quitFlag;
	}

	public void setQuitFlag(String quitFlag) {
		this.quitFlag = quitFlag;
	}
	
	/** @return 添加时间. */
	public Date getAddTime() {
		return addTime;
	}

	public void setAddTime(Date addTime) {
		this.addTime = addTime;
	}

	/** @return 是否兼职. */
	public String getJobStatus() {
		return jobStatus;
	}

	public void setJobStatus(String jobStatus) {
		this.jobStatus = jobStatus;
	}

	/** @return 排序. */
	public Integer getPriority() {
		return priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	/** @return 组织机构Id. */
	public Long getPartyId() {
		return partyId;
	}

	public void setPartyId(Long partyId) {
		this.partyId = partyId;
	}

	/** @return 离职日期. */
	public Date getQuitTime() {
		return quitTime;
	}

	public void setQuitTime(Date quitTime) {
		this.quitTime = quitTime;
	}

	/** @return 复职日期. */
	public Date getResumeTime() {
		return resumeTime;
	}

	public void setResumeTime(Date resumeTime) {
		this.resumeTime = resumeTime;
	}
	
    public String getFax() {
		return fax;
	}

	public void setFax(String fax) {
		this.fax = fax;
	}
	
	public String getWxNo() {
		return wxNo;
	}
	
	public void setWxNo(String wxNo) {
		this.wxNo = wxNo;
	}
	
	public String getQq() {
		return qq;
	}

	public void setQq(String qq) {
		this.qq = qq;
	}

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	/** @return 曾用名. */
	public String getNameBefore() {
		return nameBefore;
	}

    /** @return 曾用名. */
	public void setNameBefore(String nameBefore) {
		this.nameBefore = nameBefore;
	}

    /** @return 级别. */
	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	/** @return 籍贯. */
	public String getNativePlace() {
		return nativePlace;
	}

	public void setNativePlace(String nativePlace) {
		this.nativePlace = nativePlace;
	}

	/** @return 户口所在地. */
	public String getRegisteredResidence() {
		return registeredResidence;
	}

	public void setRegisteredResidence(String registeredResidence) {
		this.registeredResidence = registeredResidence;
	}

	/** @return 户籍类型. */
	public String getHouseholdRegisterType() {
		return householdRegisterType;
	}

	public void setHouseholdRegisterType(String householdRegisterType) {
		this.householdRegisterType = householdRegisterType;
	}

	/** @return 政治面貌. */
	public String getPoliticalOutlook() {
		return politicalOutlook;
	}

	public void setPoliticalOutlook(String politicalOutlook) {
		this.politicalOutlook = politicalOutlook;
	}

	/** @return 民族. */
	public String getNation() {
		return nation;
	}

	public void setNation(String nation) {
		this.nation = nation;
	}

	/** @return 专业. */
	public String getMajor() {
		return major;
	}

    /** @return 专业. */
	public void setMajor(String major) {
		this.major = major;
	}

	/** @return 学位. */
	public String getAcademicDegree() {
		return academicDegree;
	}

	public void setAcademicDegree(String academicDegree) {
		this.academicDegree = academicDegree;
	}

	/** @return 技能特长. */
	public String getSkillSpecialty() {
		return skillSpecialty;
	}

	public void setSkillSpecialty(String skillSpecialty) {
		this.skillSpecialty = skillSpecialty;
	}

	/** @return 用工类型. */
	public String getLaborType() {
		return laborType;
	}

	public void setLaborType(String laborType) {
		this.laborType = laborType;
	}

	/** @return 进入方式. */
	public String getEntryMode() {
		return entryMode;
	}

	public void setEntryMode(String entryMode) {
		this.entryMode = entryMode;
	}

	/** @return 学历. */
	public String getEducation() {
		return education;
	}

	public void setEducation(String education) {
		this.education = education;
	}

	/** @return 婚否. */
	public String getMarriage() {
		return marriage;
	}

	public void setMarriage(String marriage) {
		this.marriage = marriage;
	}

	/** @return 生育情况. */
	public String getFertilityCondition() {
		return fertilityCondition;
	}

	public void setFertilityCondition(String fertilityCondition) {
		this.fertilityCondition = fertilityCondition;
	}

	/** @return 入职时间. */
	public Date getEntryTime() {
		return entryTime;
	}

	public void setEntryTime(Date entryTime) {
		this.entryTime = entryTime;
	}

	/** @return 合同到期时间. */
	public Date getContractExpirationTime() {
		return contractExpirationTime;
	}

	public void setContractExpirationTime(Date contractExpirationTime) {
		this.contractExpirationTime = contractExpirationTime;
	}

	/** @return 合同有效期. */
	public String getContractDeadline() {
		return contractDeadline;
	}

	public void setContractDeadline(String contractDeadline) {
		this.contractDeadline = contractDeadline;
	}

	/** @return 合同单位. */
	public String getContractCompany() {
		return contractCompany;
	}

	public void setContractCompany(String contractCompany) {
		this.contractCompany = contractCompany;
	}

	/** @return 保险情况. */
	public String getInsurance() {
		return insurance;
	}

	public void setInsurance(String insurance) {
		this.insurance = insurance;
	}

	/** @return 资料情况. */
	public String getDocument() {
		return document;
	}

	public void setDocument(String document) {
		this.document = document;
	}

	/** @return 身份证号. */
	public String getIdentityID() {
		return identityID;
	}

	public void setIdentityID(String identityID) {
		this.identityID = identityID;
	}

	/** @return 备注. */
	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	/** @return 职称. */
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	
//	/** @return 1：流程审核同意   0：流程审核不同意. */
//    @Column(name = "IS_APPROVAL")
//	public String getIsApproval() {
//		return isApproval;
//	}
//
//	public void setIsApproval(String isApproval) {
//		this.isApproval = isApproval;
//	}

	
	/** @return 家庭成员 1 . */
	public String getFamily_1() {
		return family_1;
	}

	public void setFamily_1(String family_1) {
		this.family_1 = family_1;
	}

	
	/** @return 家庭成员 2 . */
	public String getFamily_2() {
		return family_2;
	}

	public void setFamily_2(String family_2) {
		this.family_2 = family_2;
	}

	
	/** @return 教育经历1 . */
	public String getEducational_experience_1() {
		return educational_experience_1;
	}

	public void setEducational_experience_1(String educational_experience_1) {
		this.educational_experience_1 = educational_experience_1;
	}

	
	/** @return 教育经历2 . */
	public String getEducational_experience_2() {
		return educational_experience_2;
	}

	public void setEducational_experience_2(String educational_experience_2) {
		this.educational_experience_2 = educational_experience_2;
	}

	
	/** @return 教育经历3 . */
	public String getEducational_experience_3() {
		return educational_experience_3;
	}

	public void setEducational_experience_3(String educational_experience_3) {
		this.educational_experience_3 = educational_experience_3;
	}

	
	/** @return 工作经历1 . */
	public String getWork_experience_1() {
		return work_experience_1;
	}

	public void setWork_experience_1(String work_experience_1) {
		this.work_experience_1 = work_experience_1;
	}

	/** @return 工作经历2 . */
	public String getWork_experience_2() {
		return work_experience_2;
	}

	public void setWork_experience_2(String work_experience_2) {
		this.work_experience_2 = work_experience_2;
	}

	public String getPostName() {
		return postName;
	}

	public void setPostName(String postName) {
		this.postName = postName;
	}
	
	
	@Column(name = "REAL_NAME")
	public String getRealName() {
		return realName;
	}

	public void setRealName(String realName) {
		this.realName = realName;
	}
}
