package com.mossle.base.persistence.domain;

import java.text.DateFormat;
import java.util.Date;

public class CustomerInfoDTO {
	private String id;
    private String name;
    private String rank;
    private String level;
    private String varFather;
    private String varRe;
    private String addTime;
    private String varMobile;
    private String varAddress;
    private String mnyTotalA;
    private String mnyTotalB;
    private String varPay;
    private String varFreeze;
    private String varLock;
    
    private String varDirectName;
    private String varDirectMobile;
    private String varCardNO;
    
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }
    
    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }
    
    public String getFather() {
        return varFather;
    }

    public void setFather(String varFather) {
        this.varFather = varFather;
    }
    
    public String getRe() {
        return varRe;
    }

    public void setRe(String varRe) {
        this.varRe = varRe;
    }
    
    public String getAddTime() {
        return addTime;
    }

    public void setAddTime(String addTime) {
        this.addTime = addTime;
    }
    
    public String getMobile() {
        return varMobile;
    }

    public void setMobile(String varMobile) {
        this.varMobile = varMobile;
    }
    
    public String getAddress() {
        return varAddress;
    }

    public void setAddress(String varAddress) {
        this.varAddress = varAddress;
    }
    
    public String getTotalA() {
        return mnyTotalA;
    }

    public void setTotalA(String mnyTotalA) {
        this.mnyTotalA = mnyTotalA;
    }
    
    public String getTotalB() {
        return mnyTotalB;
    }

    public void setTotalB(String mnyTotalB) {
        this.mnyTotalB = mnyTotalB;
    }
    
    public String getPay() {
        return varPay;
    }

    public void setPay(String varPay) {
        this.varPay = varPay;
    }
    
    
    public String getFreeze() {
        return varFreeze;
    }

    public void setFreeze(String varFreeze) {
        this.varFreeze = varFreeze;
    }
    
    
    public String getLock() {
        return varLock;
    }

    public void setLock(String varLock) {
        this.varLock = varLock;
    }

	public String getVarDirectName() {
		return varDirectName;
	}

	public void setVarDirectName(String varDirectName) {
		this.varDirectName = varDirectName;
	}

	public String getVarDirectMobile() {
		return varDirectMobile;
	}

	public void setVarDirectMobile(String varDirectMobile) {
		this.varDirectMobile = varDirectMobile;
	}

	public String getVarCardNO() {
		return varCardNO;
	}

	public void setVarCardNO(String varCardNO) {
		this.varCardNO = varCardNO;
	}
    
    
}
