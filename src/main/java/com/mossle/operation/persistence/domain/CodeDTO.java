package com.mossle.operation.persistence.domain;



import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Id;

/**
 * @author chengze:
 * @version 创建时间：2017年9月28日 下午6:20:42
 * 受理单编号
 */
public class CodeDTO {
	private Long id;
	//编码排序
	private int code;
	private String userID;
	private String shortName;
	private String createTime;
	//完整的受理单编号
	private String receiptNumber;
	
	
	

	public CodeDTO(Long id) {
		this.id = id;
	}
	
	public CodeDTO(Long id, int code, String userID) {
		this.id = id;
		this.code = code;
		this.userID = userID;
	}
	
	/** @return 主键. */
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
	public String getReceiptNumber() {
		return receiptNumber;
	}


	public void setReceiptNumber(String receiptNumber) {
		this.receiptNumber = receiptNumber;
	}
}
