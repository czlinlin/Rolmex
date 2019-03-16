package com.mossle.api.srmb;

import com.mossle.api.srmb.SrmbDTO;
/**
 * 权益系统API lilei at 2018-03-13
 */
public interface SrmbConnector {
	/*
	 * 创建授权书或者续约授权书
	 * */
	public SrmbDTO CreateOrRenewalSAB(String applycode);
    
}