package com.mossle.operation.persistence.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author chengze:
 * @version 创建时间：2017年9月28日 下午6:15:12
 * 受理单编号表
 */
@Entity
@Table(name = "oa_bpm_code")
public class CodeEntity  implements java.io.Serializable {
	
	private static final long serialVersionUID = 0L;
	
	private Long id;
	//编码排序
	private int code;
	private String userID;
	private String shortName;
	private String createTime;
	//完整的受理单编号
	private String receiptNumber;
	

	public CodeEntity() {
		
	}
	

	public CodeEntity(Long id) {
		this.id = id;
	}
	
	public CodeEntity(Long id, int code, String userID) {
		super();
		this.id = id;
		this.code = code;
		this.userID = userID;
	}
	
	/** @return 主键. */
    @Id
    @Column(name = "id", unique = true, nullable = false)
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	/**
     * @param 受理单编号
     *         
     */
	@Column(name = "code")
	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
	
	
	/**
     * @param content
     *            经销商编号.
     */
	@Column(name = "userid")
	public String getUserID() {
		return userID;
	}
	public void setUserID(String userID) {
		this.userID = userID;
	}
	
	/**
     * @param content
     *            部门缩写.
     */
	@Column(name = "shortName")
	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}
	
	
	/**
     * @param 创建日期
     *            .
     */
	@Column(name = "createTime")
	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}
	
	
	/**
     * @param 完整的受理单编号
     *            .
     */
	@Column(name = "receiptNumber")
	public String getReceiptNumber() {
		return receiptNumber;
	}


	public void setReceiptNumber(String receiptNumber) {
		this.receiptNumber = receiptNumber;
	}
}
