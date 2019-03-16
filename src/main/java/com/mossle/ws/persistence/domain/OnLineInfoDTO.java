package com.mossle.ws.persistence.domain;

import java.util.Date;

import javax.persistence.*;

import com.mossle.operation.persistence.domain.CommonDTO;

//@Entity
//@Table(name = "RO_PF_OAOnLine")
public class OnLineInfoDTO extends CommonDTO{
	
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
	
	/**批示内容
	 * **/
	private String comment;
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
	public OnLineInfoDTO(){}
	public OnLineInfoDTO(Long id, String ucode, String applycode, String branch,
			String name, String identity, String welfaregrade, String mobile,
			String newname, String newidentity, String bankname,
			String bankaddress, String bankcode, String applypic,
			String newpic, String partpic1, String partpic2, String partpic3,
			String relativespic, Long recordid, String applytype,
			String status, String reason, Date applytime, String audtiman,
			String auditremark, Date audittime, String completeman,
			String completeremark, Date completetime,String isAuthCertificate) {
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
		this.comment=comment;
	}

	/**
	 * 主键ID
	 * **/
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * 经销商编号
	 * **/
	public String getUcode() {
		return ucode;
	}

	public void setUcode(String ucode) {
		this.ucode = ucode;
	}

	/**
	 * 受理单号
	 * **/
	public String getApplycode() {
		return applycode;
	}

	public void setApplycode(String applycode) {
		this.applycode = applycode;
	}

	/**
	 * 部门
	 * **/
	
	public String getBranch() {
		return branch;
	}

	public void setBranch(String branch) {
		this.branch = branch;
	}

	/**
	 * 申请人姓名
	 * **/
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * 申请人证件号
	 * **/
	
	public String getIdentity() {
		return identity;
	}

	public void setIdentity(String identity) {
		this.identity = identity;
	}

	/**
	 * 福利级别
	 * **/
	
	public String getWelfaregrade() {
		return welfaregrade;
	}

	public void setWelfaregrade(String welfaregrade) {
		this.welfaregrade = welfaregrade;
	}

	/**
	 * 申请人手机号
	 * **/
	
	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	/**
	 * 新姓名
	 * **/
	
	public String getNewname() {
		return newname;
	}

	public void setNewname(String newname) {
		this.newname = newname;
	}

	/**
	 * 新证件号
	 * **/
	
	public String getNewidentity() {
		return newidentity;
	}

	public void setNewidentity(String newidentity) {
		this.newidentity = newidentity;
	}

	/**
	 * 新银行卡号/代理人编号
	 * **/
	
	public String getBankname() {
		return bankname;
	}

	public void setBankname(String bankname) {
		this.bankname = bankname;
	}

	/**
	 * 新银行地址(包含省/市/区县)
	 * **/
	
	public String getBankaddress() {
		return bankaddress;
	}

	public void setBankaddress(String bankaddress) {
		this.bankaddress = bankaddress;
	}

	/**
	 * 新银行账号
	 * **/
	
	public String getBankcode() {
		return bankcode;
	}

	public void setBankcode(String bankcode) {
		this.bankcode = bankcode;
	}

	/**
	 * 申请人证件(附件)
	 * **/
	
	public String getApplypic() {
		return applypic;
	}

	public void setApplypic(String applypic) {
		this.applypic = applypic;
	}

	/**
	 * 新人证件(附件)
	 * **/
	
	public String getNewpic() {
		return newpic;
	}

	public void setNewpic(String newpic) {
		this.newpic = newpic;
	}

	/**
	 * 营销一分部证件
	 * **/
	
	public String getPartpic1() {
		return partpic1;
	}

	public void setPartpic1(String partpic1) {
		this.partpic1 = partpic1;
	}

	/**
	 * 营销二分部证件
	 * **/
	
	public String getPartpic2() {
		return partpic2;
	}

	public void setPartpic2(String partpic2) {
		this.partpic2 = partpic2;
	}

	/**
	 * 营销三分部证件
	 * **/
	
	public String getPartpic3() {
		return partpic3;
	}

	public void setPartpic3(String partpic3) {
		this.partpic3 = partpic3;
	}

	/**
	 * 亲属证明
	 * **/
	
	public String getRelativespic() {
		return relativespic;
	}

	public void setRelativespic(String relativespic) {
		this.relativespic = relativespic;
	}

	/**
	 * 流程记录ID
	 * **/
	
	public Long getRecordid() {
		return recordid;
	}

	public void setRecordid(Long recordid) {
		this.recordid = recordid;
	}

	/**
	 * 申请类型：1（密码初始化）、2（姓名更正）、3（更正身份证）、4（资格注销）、5（直系亲属资格替换）
	 * **/
	
	public String getApplytype() {
		return applytype;
	}

	public void setApplytype(String applytype) {
		this.applytype = applytype;
	}

	/**
	 * 状态：0（已申请）、1（审核中）、2（已取消）、3（已完成）
	 * **/
	
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * 申请原因
	 * **/
	
	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	/**
	 * 申请时间
	 * **/
	
	public Date getApplytime() {
		return applytime;
	}

	public void setApplytime(Date applytime) {
		this.applytime = applytime;
	}

	/**
	 * 审核/取消人
	 * **/
	
	public String getAudtiman() {
		return audtiman;
	}

	public void setAudtiman(String audtiman) {
		this.audtiman = audtiman;
	}

	/**
	 * 审核/取消备注
	 * **/
	
	public String getAuditremark() {
		return auditremark;
	}

	public void setAuditremark(String auditremark) {
		this.auditremark = auditremark;
	}

	/**
	 * 审核/取消时间
	 * **/
	
	public Date getAudittime() {
		return audittime;
	}

	public void setAudittime(Date audittime) {
		this.audittime = audittime;
	}

	/**
	 * 执行人
	 * **/
	
	public String getCompleteman() {
		return completeman;
	}

	public void setCompleteman(String completeman) {
		this.completeman = completeman;
	}

	/**
	 * 执行备注
	 * **/
	
	public String getCompleteremark() {
		return completeremark;
	}

	public void setCompleteremark(String completeremark) {
		this.completeremark = completeremark;
	}

	/**执行时间
	 * **/
	
	public Date getCompletetime() {
		return completetime;
	}

	public void setCompletetime(Date completetime) {
		this.completetime = completetime;
	}
	
	
	public String getIsAuthCertificate() {
		return isAuthCertificate;
	}
	public void setIsAuthCertificate(String isAuthCertificate) {
		this.isAuthCertificate = isAuthCertificate;
	}
	
	public String getShopLicense() {
		return shopLicense;
	}
	public void setShopLicense(String shopLicense) {
		this.shopLicense = shopLicense;
	}
	public String getEnterpriseName() {
		return enterpriseName;
	}
	public void setEnterpriseName(String enterpriseName) {
		this.enterpriseName = enterpriseName;
	}
	public String getLegaler() {
		return legaler;
	}
	public void setLegaler(String legaler) {
		this.legaler = legaler;
	}
	public String getLegalerIdCard() {
		return legalerIdCard;
	}
	public void setLegalerIdCard(String legalerIdCard) {
		this.legalerIdCard = legalerIdCard;
	}
	public String getDistributorPhone() {
		return distributorPhone;
	}
	public void setDistributorPhone(String distributorPhone) {
		this.distributorPhone = distributorPhone;
	}
	public String getScopeBusiness() {
		return scopeBusiness;
	}
	public void setScopeBusiness(String scopeBusiness) {
		this.scopeBusiness = scopeBusiness;
	}
	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
	}
	public String getPublicAccount() {
		return publicAccount;
	}
	public void setPublicAccount(String publicAccount) {
		this.publicAccount = publicAccount;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public String getAccountType() {
		return accountType;
	}
	public void setAccountType(String accountType) {
		this.accountType = accountType;
	}
	public String getOpeningBank() {
		return openingBank;
	}
	public void setOpeningBank(String openingBank) {
		this.openingBank = openingBank;
	}
	
	public String getOpeningName() {
		return openingName;
	}
	public void setOpeningName(String openingName) {
		this.openingName = openingName;
	}
	public String getAccountNumbr() {
		return accountNumbr;
	}
	public void setAccountNumbr(String accountNumbr) {
		this.accountNumbr = accountNumbr;
	}
	public String getStoreArea() {
		return storeArea;
	}
	public void setStoreArea(String storeArea) {
		this.storeArea = storeArea;
	}
	
}