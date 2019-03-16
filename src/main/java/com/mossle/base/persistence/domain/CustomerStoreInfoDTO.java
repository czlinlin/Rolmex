package com.mossle.base.persistence.domain;

import java.text.DateFormat;
import java.util.Date;

public class CustomerStoreInfoDTO {
	private String varStoCode;
	private String nvrStoName;
	
	public String getVarStoCode() {
		return varStoCode;
	}

	public void setVarStoCode(String varStoCode) {
		this.varStoCode = varStoCode;
	}

	public String getNvrStoName() {
		return nvrStoName;
	}

	public void setNvrStoName(String nvrStoName) {
		this.nvrStoName = nvrStoName;
	}
}
