package com.mossle.ws.persistence.domain;

import java.util.Date;

import javax.persistence.*;

@Entity
@Table(name = "RO_PF_OAOnLine")
public class OnLineInfo {
	
	private Long id;
	
	private String ucode;
	
	/**
	 * 受理单号
	 * **/
	private String applycode;
	
	/**
	 * 部门
	 * **/
	private String branch;
	
	/**
	 * 申请人姓名
	 * **/
	private String name;
	
	/**
	 * 申请人证件号
	 * **/
	private String identity;
	
	/**
	 * 福利级别
	 * **/
	private String welfaregrade;
	
	/**
	 * 申请人手机号
	 * **/
	private String mobile;
	
	/**
	 * 新姓名
	 * **/
	private String newname;
	
	/**
	 * 新证件号
	 * **/
	private String newidentity;
	
	/**
	 * 新银行卡号/代理人编号
	 * **/
	private String bankname;
	
	/**
	 * 新银行地址(包含省/市/区县)
	 * **/
	private String bankaddress;
	
	/**
	 * 新银行账号
	 * **/
	private String bankcode;
	
	/**
	 * 申请人证件(附件)
	 * **/
	private String applypic;
	
	/**
	 * 新人证件(附件)
	 * **/
	private String newpic;
	
	/**
	 * 营销一分部证件
	 * **/
	private String partpic1;
	
	/**
	 * 营销二分部证件
	 * **/
	private String partpic2;
	
	/**
	 * 营销三分部证件
	 * **/
	private String partpic3;
	
	/**
	 * 亲属证明
	 * **/
	private String relativespic;
	
	/**
	 * 流程记录ID
	 * **/
	private Long recordid;
	
	/**
	 * 申请类型：1（密码初始化）、2（姓名更正）、3（更正身份证）、4（资格注销）、5（直系亲属资格替换）
	 * **/
	private String applytype;
	
	/**
	 * 状态：0（已申请）、1（审核中）、2（已取消）、3（已完成）
	 * **/
	private String status;
	
	/**
	 * 申请原因
	 * **/
	private String reason;
	
	/**
	 * 申请时间
	 * **/
	private Date applytime;
	
	/**
	 * 审核/取消人
	 * **/
	private String audtiman;
	
	/**
	 * 审核/取消备注
	 * **/
	private String auditremark;
	
	/**
	 * 审核/取消时间
	 * **/
	private Date audittime;
	
	/**
	 * 执行人
	 * **/
	private String completeman;
	
	/**
	 * 执行备注
	 * **/
	private String completeremark;
	
	/**
	 * 是否生成授权书，1：是，0：否
	 */
	private String isAuthCertificate;
	
	/**执行时间
	 * **/
	private Date completetime;
	
	/*********************
	 * 统一社会信用代码
	 * *******************/
	private String shopLicense;
	
	/*********************
	 * 企业名称
	 * *******************/
	private String enterpriseName;
	
	/*********************
	 * 法定代表人
	 * *******************/
	private String legaler;
	
	/*********************
	 * 法人身份证号码
	 * *******************/
	private String legalerIdCard;
	
	/*********************
	 * 联系电话
	 * *******************/
	private String distributorPhone;
	
	/*********************
	 * 经营范围
	 * *******************/
	private String scopeBusiness;
	
	/*********************
	 * 备注
	 * *******************/
	private String note;
	
	/*********************
	 * 对公账户行号
	 * *******************/
	private String publicAccount;
	
	/*********************
	 * 账户类型
	 * *******************/
	private String accountType;
	
	/*********************
	 * 开户行
	 * *******************/
	private String openingBank;
	/*********************
	 * 开户名
	 * *******************/
	private String openingName;
	
	/*********************
	 * 账号
	 * *******************/
	private String accountNumbr;
	
	/*********************
	 * 实体店面积
	 * *******************/
	private String storeArea;
	
	/**批示内容
	 * **/
	/*private String comment;*/

	public OnLineInfo(){}
	public OnLineInfo(Long id, String ucode, String applycode, String branch,
			String name, String identity, String welfaregrade, String mobile,
			String newname, String newidentity, String bankname,
			String bankaddress, String bankcode, String applypic,
			String newpic, String partpic1, String partpic2, String partpic3,
			String relativespic, Long recordid, String applytype,
			String status, String reason, Date applytime, String audtiman,
			String auditremark, Date audittime, String completeman,
			String completeremark, Date completetime,String isAuthCertificate,
			String shopLicense,
			String enterpriseName,
			String legaler,
			String distributorPhone,
			String scopeBusiness,
			String note,
			String publicAccount) {
		super();
		this.id = id;
		this.ucode = ucode;
		this.applycode = applycode;
		this.branch = branch;
		this.name = name;
		this.identity = identity;
		this.welfaregrade = welfaregrade;
		this.mobile = mobile;
		this.newname = newname;
		this.newidentity = newidentity;
		this.bankname = bankname;
		this.bankaddress = bankaddress;
		this.bankcode = bankcode;
		this.applypic = applypic;
		this.newpic = newpic;
		this.partpic1 = partpic1;
		this.partpic2 = partpic2;
		this.partpic3 = partpic3;
		this.relativespic = relativespic;
		this.recordid = recordid;
		this.applytype = applytype;
		this.status = status;
		this.reason = reason;
		this.applytime = applytime;
		this.audtiman = audtiman;
		this.auditremark = auditremark;
		this.audittime = audittime;
		this.completeman = completeman;
		this.completeremark = completeremark;
		this.completetime = completetime;
		this.isAuthCertificate=isAuthCertificate;
	}

	/**
	 * 主键ID
	 * **/
	@Id
    @Column(name = "id")
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * 经销商编号
	 * **/
	@Column(name = "ucode")
	public String getUcode() {
		return ucode;
	}

	public void setUcode(String ucode) {
		this.ucode = ucode;
	}

	/**
	 * 受理单号
	 * **/
	@Column(name = "varApplyCode")
	public String getApplycode() {
		return applycode;
	}

	public void setApplycode(String applycode) {
		this.applycode = applycode;
	}

	/**
	 * 部门
	 * **/
	@Column(name = "varBranch")
	public String getBranch() {
		return branch;
	}

	public void setBranch(String branch) {
		this.branch = branch;
	}

	/**
	 * 申请人姓名
	 * **/
	@Column(name = "varApplyName")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * 申请人证件号
	 * **/
	@Column(name = "varApplyIdentity")
	public String getIdentity() {
		return identity;
	}

	public void setIdentity(String identity) {
		this.identity = identity;
	}

	/**
	 * 福利级别
	 * **/
	@Column(name = "varWelfareGrade")
	public String getWelfaregrade() {
		return welfaregrade;
	}

	public void setWelfaregrade(String welfaregrade) {
		this.welfaregrade = welfaregrade;
	}

	/**
	 * 申请人手机号
	 * **/
	@Column(name = "varMobile")
	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	/**
	 * 新姓名
	 * **/
	@Column(name = "varNewName")
	public String getNewname() {
		return newname;
	}

	public void setNewname(String newname) {
		this.newname = newname;
	}

	/**
	 * 新证件号
	 * **/
	@Column(name = "varNewIdentity")
	public String getNewidentity() {
		return newidentity;
	}

	public void setNewidentity(String newidentity) {
		this.newidentity = newidentity;
	}

	/**
	 * 新银行卡号/代理人编号
	 * **/
	@Column(name = "varBankName")
	public String getBankname() {
		return bankname;
	}

	public void setBankname(String bankname) {
		this.bankname = bankname;
	}

	/**
	 * 新银行地址(包含省/市/区县)
	 * **/
	@Column(name = "varBankAddress")
	public String getBankaddress() {
		return bankaddress;
	}

	public void setBankaddress(String bankaddress) {
		this.bankaddress = bankaddress;
	}

	/**
	 * 新银行账号
	 * **/
	@Column(name = "varBankCode")
	public String getBankcode() {
		return bankcode;
	}

	public void setBankcode(String bankcode) {
		this.bankcode = bankcode;
	}
	
	/**
	 * 申请人证件(附件)
	 * **/
	@Column(name = "varApplyPic")
	public String getApplypic() {
		return applypic;
	}

	public void setApplypic(String applypic) {
		this.applypic = applypic;
	}

	/**
	 * 新人证件(附件)
	 * **/
	@Column(name = "varNewPic")
	public String getNewpic() {
		return newpic;
	}

	public void setNewpic(String newpic) {
		this.newpic = newpic;
	}

	/**
	 * 营销一分部证件
	 * **/
	@Column(name = "varPartPic1")
	public String getPartpic1() {
		return partpic1;
	}

	public void setPartpic1(String partpic1) {
		this.partpic1 = partpic1;
	}

	/**
	 * 营销二分部证件
	 * **/
	@Column(name = "varPartPic2")
	public String getPartpic2() {
		return partpic2;
	}

	public void setPartpic2(String partpic2) {
		this.partpic2 = partpic2;
	}

	/**
	 * 营销三分部证件
	 * **/
	@Column(name = "varPartPic3")
	public String getPartpic3() {
		return partpic3;
	}

	public void setPartpic3(String partpic3) {
		this.partpic3 = partpic3;
	}

	/**
	 * 亲属证明
	 * **/
	@Column(name = "varRelativesPic")
	public String getRelativespic() {
		return relativespic;
	}

	public void setRelativespic(String relativespic) {
		this.relativespic = relativespic;
	}

	/**
	 * 流程记录ID
	 * **/
	@Column(name = "intRecordID")
	public Long getRecordid() {
		return recordid;
	}

	public void setRecordid(Long recordid) {
		this.recordid = recordid;
	}

	/**
	 * 申请类型：1（密码初始化）、2（姓名更正）、3（更正身份证）、4（资格注销）、5（直系亲属资格替换）
	 * **/
	@Column(name = "chrApplyType")
	public String getApplytype() {
		return applytype;
	}

	public void setApplytype(String applytype) {
		this.applytype = applytype;
	}

	/**
	 * 状态：0（已申请）、1（审核中）、2（已取消）、3（已完成）
	 * **/
	@Column(name = "chrStatus")
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * 申请原因
	 * **/
	@Column(name = "varReason")
	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	/**
	 * 申请时间
	 * **/
	@Column(name = "dtmApplyTime")
	public Date getApplytime() {
		return applytime;
	}

	public void setApplytime(Date applytime) {
		this.applytime = applytime;
	}

	/**
	 * 审核/取消人
	 * **/
	@Column(name = "varAudtiMan")
	public String getAudtiman() {
		return audtiman;
	}

	public void setAudtiman(String audtiman) {
		this.audtiman = audtiman;
	}

	/**
	 * 审核/取消备注
	 * **/
	@Column(name = "varAuditRemark")
	public String getAuditremark() {
		return auditremark;
	}

	public void setAuditremark(String auditremark) {
		this.auditremark = auditremark;
	}

	/**
	 * 审核/取消时间
	 * **/
	@Column(name = "dtmAuditTime")
	public Date getAudittime() {
		return audittime;
	}

	public void setAudittime(Date audittime) {
		this.audittime = audittime;
	}

	/**
	 * 执行人
	 * **/
	@Column(name = "varCompleteMan")
	public String getCompleteman() {
		return completeman;
	}

	public void setCompleteman(String completeman) {
		this.completeman = completeman;
	}

	/**
	 * 执行备注
	 * **/
	@Column(name = "varCompleteRemark")
	public String getCompleteremark() {
		return completeremark;
	}

	public void setCompleteremark(String completeremark) {
		this.completeremark = completeremark;
	}

	/**执行时间
	 * **/
	@Column(name = "dtmCompleteTime")
	public Date getCompletetime() {
		return completetime;
	}

	public void setCompletetime(Date completetime) {
		this.completetime = completetime;
	}
	
	@Column(name = "chrIsAuthCertificate")
	public String getIsAuthCertificate() {
		return isAuthCertificate;
	}
	public void setIsAuthCertificate(String isAuthCertificate) {
		this.isAuthCertificate = isAuthCertificate;
	}
	
	/**
	 * 统一社会信用代码
	 * **/
	@Column(name = "varShopLicense")
	public String getShopLicense() {
		return shopLicense;
	}
	public void setShopLicense(String shopLicense) {
		this.shopLicense = shopLicense;
	}
	
	/**
	 * 企业名称
	 * **/
	@Column(name = "varEnterpriseName")
	public String getEnterpriseName() {
		return enterpriseName;
	}
	public void setEnterpriseName(String enterpriseName) {
		this.enterpriseName = enterpriseName;
	}
	
	/**
	 * 法定代表人
	 * **/
	@Column(name = "varLegaler")
	public String getLegaler() {
		return legaler;
	}
	public void setLegaler(String legaler) {
		this.legaler = legaler;
	}
	
	/*********************
	 * 法人身份证号码
	 * *******************/
	@Column(name = "varLegalerIdCard")
	public String getLegalerIdCard() {
		return legalerIdCard;
	}
	public void setLegalerIdCard(String legalerIdCard) {
		this.legalerIdCard = legalerIdCard;
	}
	
	/**
	 * 联系电话
	 * **/
	@Column(name = "varDistributorPhone")
	public String getDistributorPhone() {
		return distributorPhone;
	}
	public void setDistributorPhone(String distributorPhone) {
		this.distributorPhone = distributorPhone;
	}
	
	/**
	 * 经营范围
	 * **/
	@Column(name = "varScopeBusiness")
	public String getScopeBusiness() {
		return scopeBusiness;
	}
	public void setScopeBusiness(String scopeBusiness) {
		this.scopeBusiness = scopeBusiness;
	}
	
	/**
	 * 备注
	 * **/
	@Column(name = "varNote")
	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
	}
	
	/**
	 * 对公账户行号
	 * **/
	@Column(name = "varPublicAccount")
	public String getPublicAccount() {
		return publicAccount;
	}
	public void setPublicAccount(String publicAccount) {
		this.publicAccount = publicAccount;
	}
	
	/**
	 * 账户类型
	 */
	@Column(name = "varAccountType")
	public String getAccountType() {
		return accountType;
	}
	public void setAccountType(String accountType) {
		this.accountType = accountType;
	}
	/**
	 * 开户行
	 */
	@Column(name = "varOpeningBank")
	public String getOpeningBank() {
		return openingBank;
	}
	public void setOpeningBank(String openingBank) {
		this.openingBank = openingBank;
	}
	@Column(name = "varOpeningName")
	public String getOpeningName() {
		return openingName;
	}
	public void setOpeningName(String openingName) {
		this.openingName = openingName;
	}
	/**
	 * 账号
	 */
	@Column(name = "varAccountNumber")
	public String getAccountNumbr() {
		return accountNumbr;
	}
	public void setAccountNumbr(String accountNumbr) {
		this.accountNumbr = accountNumbr;
	}
	/**
	 * 实体店面积
	 */
	@Column(name = "varStoreArea")
	public String getStoreArea() {
		return storeArea;
	}
	public void setStoreArea(String storeArea) {
		this.storeArea = storeArea;
	}
	
	
	
	/*public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}*/
	
}