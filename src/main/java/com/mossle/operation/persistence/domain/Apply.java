package com.mossle.operation.persistence.domain;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
/** 
 * @author  cz 
 * @version 2017年7月26日
 * 业务受理申请单 
 */
@Entity
@Table(name = "oa_bpm_commapply")
public class Apply implements java.io.Serializable {

    private static final long serialVersionUID = 0L;
    
    /** 主键. */
    private Long id;
    /** 受理单编号 */
    private String applyCode;
  
    /** 给谁申请-经销商编号 */
    private String ucode;
    /** 申请内容. */
    private String content; 
    /**  */
    private String processInstanceId; 
    /** 当前登录人id */
    private Long userid; 
    /** 附件名称 */
    private String fileName;  
    /** 附件路径 */
    private String filePath;
    /** 姓名 */
    private String  userName  ;
    /** 福利级别 */
    private String  welfare  ;
    /** 级别 */
    private String  level  ;
    /** 所属体系 */
    private String  system  ;
    /** 销售人 */
    private String  varFather  ;
    /** 服务人 */
    private String  varRe  ;
    /** 注册时间 */
    private String  addTime  ;
    /** 申请业务类型 */
    private String  businessType  ;
    /** 业务细分 */
    private String  businessDetail  ;
    /** 电话 */
    private String  mobile  ;
    /** 地址 */
    private String  address  ;
    /** 业务级别 */
    private String  businessLevel  ;
    /** 所属大区 */
    private String  area  ;
    /** 业务标准（现场办理） */
    private String  businessStand1  ;
    /** 业务标准（非现场办理） */
    private String  businessStand2  ;
    /** 点位信息 */
    private String  treeInfo  ;
    /** 该条任务的创建时间 */
    private Date  createTime  ;
    /** 该条任务的调整后重新发起时间 */
    private Date  modifyTime  ;
    /** 提交次数 */
    private int submitTimes;
    
    

    

	public Apply() {
    }

    public Apply(Long id) {
        this.id = id;
    }
    
    public Apply(Long id,String ucode, String content, String processInstanceId,Long userid,String fileName
    			,String filePath,Date modifyTime  ) {
    	this.id = id;
    	this.ucode = ucode;
    	this.content = content;
    	this.processInstanceId = processInstanceId;    	
        this.userid = userid;    
        this.fileName = fileName;   
        this.filePath = filePath;   
        this.modifyTime = modifyTime;
    }
    
    
    /** @return 主键. */
    @Id
    @Column(name = "ID", unique = true, nullable = false)
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
    
    /** @return 受理单编号. */
    @Column(name = "applyCode")
    public String getApplyCode() {
  		return applyCode;
  	}

  	public void setApplyCode(String applyCode) {
  		this.applyCode = applyCode;
  	}
    

    /** @return 经销商编号. */
    @Column(name = "ucode")
    public String getUcode() {
        return this.ucode;
    }

    /**
     * @param content
     *            经销商编号.
     */
    public void setUcode(String ucode) {
        this.ucode = ucode;
    }    
    

    /** @return 申请内容. */
    @Column(name = "CONTENT", length = 1000)
    public String getContent() {
        return this.content;
    }

    /**
     * @param content
     *            申请内容.
     */
    public void setContent(String content) {
        this.content = content;
    }
    
    
    /** @return 申请人. */
    @Column(name = "USERID")
    public Long getUserId() {
        return this.userid;
    }
    /**
     * @param id
     *            申请人.
     */
    public void setUserId(Long userId) {
        this.userid = userId;
    }
    
    
    /** @return 附件名称. */
    @Column(name = "filename")
    public String getFileName() {
        return this.fileName;
    }
    /**
     * @param 
     *            附件名称.
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    
    
    /** @return 附件路径. */
    @Column(name = "filepath")
    public String getFilePath() {
        return this.filePath;
    }
    /**
     * @param 
     *            附件路径.
     */
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    
    /** @return . */
    @Column(name = "PROCESS_INSTANCEID", length = 200)
    public String getProcessInstanceId() {
        return this.processInstanceId;
    }

    /**
     * @param.
     */
    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }
    
    /** @return 姓名. */
    @Column(name = "userName", length = 50)
    public String getUserName() {
        return this.userName;
    }

    /**
     * @param 姓名.
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    /** @return 福利级别. */
    @Column(name = "welfare", length = 10)
    public String getWelfare() {
        return this.welfare;
    }

    /**
     * @param 
     *            福利级别.
     */
    public void setWelfare(String welfare) {
        this.welfare = welfare;
    }
    
    /** @return 级别. */
    @Column(name = "level", length = 10)
    public String getLevel() {
        return this.level;
    }

    /**
     * @param 
     *            级别.
     */
    public void setLevel(String level) {
        this.level = level;
    }
    
    /** @return 所属体系 */
    @Column(name = "system", length = 45)
    public String getSystem() {
        return this.system;
    }

    /**
     * @param 所属体系
     */
    public void setSystem(String system) {
        this.system = system;
    }
    
    
    /** @return 销售人 */
    @Column(name = "varFather", length = 100)
    public String getVarFather() {
        return this.varFather;
    }

    /**
     * @param 销售人
     */
    public void setVarFather(String varFather) {
        this.varFather = varFather;
    }
    
    /** @return 服务人 */
    @Column(name = "varRe", length = 100)
    public String getVarRe() {
        return this.varRe;
    }

    /**
     * @param 服务人
     */
    public void setVarRe(String varRe) {
        this.varRe = varRe;
    }
    
    /** @return 注册时间 */
    @Column(name = "addTime" )
    public String getAddTime() {
        return this.addTime;
    }

    /**
     * @param 注册时间
     */
    public void setAddTime(String addTime) {
        this.addTime = addTime;
    }
    
    
    /** @return 申请业务类型 */
    @Column(name = "businessType" )
    public String getBusinessType() {
        return this.businessType;
    }

    /**
     * @param 申请业务类型
     */
    public void setBusinessType(String businessType) {
        this.businessType = businessType;
    }
    
    /** @return 业务细分 */
    @Column(name = "businessDetail" )
    public String getBusinessDetail() {
        return this.businessDetail;
    }

    /**
     * @param 业务细分
     */
    public void setBusinessDetail(String businessDetail) {
        this.businessDetail = businessDetail;
    }
    
    /** @return 电话 */
    @Column(name = "mobile" )
    public String getMobile() {
        return this.mobile;
    }

    /**
     * @param 电话
     */
    public void setMobile(String mobile) {
        this.mobile = mobile;
    }
    
    /** @return 地址 */
    @Column(name = "address" )
    public String getAddress() {
        return this.address;
    }

    /**
     * @param 地址
     */
    public void setAddress(String address) {
        this.address = address;
    }
    
    /** @return 业务级别 */
    @Column(name = "businessLevel" )
    public String getBusinessLevel() {
        return this.businessLevel;
    }

    /**
     * @param 业务级别
     */
    public void setBusinessLevel(String businessLevel) {
        this.businessLevel = businessLevel;
    }
    
    /** @return 所属大区 */
    @Column(name = "area" )
    public String getArea() {
        return this.area;
    }

    /**
     * @param 所属大区
     */
    public void setArea(String area) {
        this.area = area;
    }
    
    /** @return 业务标准（现场办理） */
    @Column(name = "businessStand1" )
    public String getBusinessStand1() {
        return this.businessStand1;
    }

    /**
     * @param 业务标准（现场办理）
     */
    public void setBusinessStand1(String businessStand1) {
        this.businessStand1 = businessStand1;
    }
    
    
    /** @return 业务标准（非现场办理） */
    @Column(name = "businessStand2" )
    public String getBusinessStand2() {
        return this.businessStand2;
    }

    /**
     * @param 业务标准（非现场办理）
     */
    public void setBusinessStand2(String businessStand2) {
        this.businessStand2 = businessStand2;
    }
    
    
    /** @return 业务标准（非现场办理） */
    @Column(name = "treeInfo" )
    public String getTreeInfo() {
        return this.treeInfo;
    }

    /**
     * @param 业务标准（非现场办理）
     */
    public void setTreeInfo(String treeInfo) {
        this.treeInfo = treeInfo;
    }
    
    /** @return  */
    @Column(name = "createTime" )
    public Date getCreateTime() {
        return this.createTime;
    }

    /**
     * @param 
     */
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
    
    /** @return . */
    @Column(name = "modifyTime")
    public Date getModifyTime() {
        return this.modifyTime;
    }

    /**
     * @param content
     *            .
     */
    public void setModifyTime(Date modifyTime) {
        this.modifyTime = modifyTime;
    }  
    
    
    /**
     * @param 提交次数
     *            .
     */
    @Column(name = "submitTimes")
    public int getSubmitTimes() {
		return submitTimes;
	}

	public void setSubmitTimes(int submitTimes) {
		this.submitTimes = submitTimes;
	}
}
