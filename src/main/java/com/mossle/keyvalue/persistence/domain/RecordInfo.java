package com.mossle.keyvalue.persistence.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "kv_record")
public class RecordInfo implements java.io.Serializable  {
	private static final long serialVersionUID = 0L;
	
	private Long id;
	private String businessKey;
    private String name;
    private String formTemplateCode;
    //private String code;
    private String category;
    private int status;
    private String ref;
    private Date createTime;
    private String userId;
    private String tenantId;
    
    /** 受理单号 */
    private String applyCode;   
    /** 主题 */
    private String theme;       
    /** 经销商编号 */
    private String ucode; 
    /** 申请业务类型ID */
    private String businessTypeId;
    /** 申请业务类型名称 */
    private String  businessTypeName;
    /** 业务细分ID */
    private String  businessDetailId;
    
    /** 业务细分名称 */
    private String  businessDetailName;
    /** 提交次数 */
    private int submitTimes;
    
    /** 所属大区ID */
    private String areaId;
    /** 所属大区名称 */
    private String areaName;
    /** 所属体系Id */
    private String systemId;
    /** 所属体系名称 */
    private String systemName;
    
    /** 所属公司ID */
    private String companyId;
    /** 所属公司名称 */
    private String companyName;
    
    /** 完成时间 */
    private Date endTime;
    
    /** 详情页面地址 */
    private String url;
    
    /** 详情页面html字符串 */
    private String detailHtml;
    
    /** 业务主键 */
    private String pkId;
    
    /** 流程状态 */
    private String auditStatus;
    
    /**  申请内容 */
    private String applyContent;

    /** 发起流程的岗位ID */
    private String startPositionId;
    
    @Id
    @Column(name = "id")
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name = "BUSINESS_KEY")
	public String getBusinessKey() {
		return businessKey;
	}

	public void setBusinessKey(String businessKey) {
		this.businessKey = businessKey;
	}

	@Column(name = "NAME")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "FORM_TEMPLATE_CODE")
	public String getFormTemplateCode() {
		return formTemplateCode;
	}

	public void setFormTemplateCode(String formTemplateCode) {
		this.formTemplateCode = formTemplateCode;
	}

	
	/*public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}*/

	@Column(name = "CATEGORY")
	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	@Column(name = "STATUS")
	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	@Column(name = "REF")
	public String getRef() {
		return ref;
	}

	public void setRef(String ref) {
		this.ref = ref;
	}

	@Column(name = "CREATE_TIME")
	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	@Column(name = "USER_ID")
	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	@Column(name = "TENANT_ID")
	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	@Column(name = "applyCode")
	public String getApplyCode() {
		return applyCode;
	}

	public void setApplyCode(String applyCode) {
		this.applyCode = applyCode;
	}

	@Column(name = "theme")
	public String getTheme() {
		return theme;
	}

	public void setTheme(String theme) {
		this.theme = theme;
	}

	@Column(name = "ucode")
	public String getUcode() {
		return ucode;
	}

	public void setUcode(String ucode) {
		this.ucode = ucode;
	}

	@Column(name = "businessTypeId")
	public String getBusinessTypeId() {
		return businessTypeId;
	}

	public void setBusinessTypeId(String businessTypeId) {
		this.businessTypeId = businessTypeId;
	}

	@Column(name = "businessTypeName")
	public String getBusinessTypeName() {
		return businessTypeName;
	}

	public void setBusinessTypeName(String businessTypeName) {
		this.businessTypeName = businessTypeName;
	}

	@Column(name = "businessDetailId")
	public String getBusinessDetailId() {
		return businessDetailId;
	}

	public void setBusinessDetailId(String businessDetailId) {
		this.businessDetailId = businessDetailId;
	}

	@Column(name = "businessDetailName")
	public String getBusinessDetailName() {
		return businessDetailName;
	}

	public void setBusinessDetailName(String businessDetailName) {
		this.businessDetailName = businessDetailName;
	}

	@Column(name = "submitTimes")
	public int getSubmitTimes() {
		return submitTimes;
	}

	public void setSubmitTimes(int submitTimes) {
		this.submitTimes = submitTimes;
	}

	@Column(name = "areaId")
	public String getAreaId() {
		return areaId;
	}

	public void setAreaId(String areaId) {
		this.areaId = areaId;
	}

	@Column(name = "areaName")
	public String getAreaName() {
		return areaName;
	}

	public void setAreaName(String areaName) {
		this.areaName = areaName;
	}

	@Column(name = "systemId")
	public String getSystemId() {
		return systemId;
	}

	public void setSystemId(String systemId) {
		this.systemId = systemId;
	}

	@Column(name = "systemName")
	public String getSystemName() {
		return systemName;
	}

	public void setSystemName(String systemName) {
		this.systemName = systemName;
	}

	@Column(name = "companyId")
	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

	@Column(name = "companyName")
	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	@Column(name = "end_time")
	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	@Column(name = "url")
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@Column(name = "detailHtml")
	public String getDetailHtml() {
		return detailHtml;
	}

	public void setDetailHtml(String detailHtml) {
		this.detailHtml = detailHtml;
	}

	@Column(name = "pk_id")
	public String getPkId() {
		return pkId;
	}

	public void setPkId(String pkId) {
		this.pkId = pkId;
	}

	@Column(name = "audit_status")
	public String getAuditStatus() {
		return auditStatus;
	}

	public void setAuditStatus(String auditStatus) {
		this.auditStatus = auditStatus;
	}

	@Column(name = "apply_content")
	public String getApplyContent() {
		return applyContent;
	}

	public void setApplyContent(String applyContent) {
		this.applyContent = applyContent;
	}
	@Column(name = "start_positionid")
	public String getStartPositionId() {
		return startPositionId;
	}

	public void setStartPositionId(String startPositionId) {
		this.startPositionId = startPositionId;
	}
	
	
}