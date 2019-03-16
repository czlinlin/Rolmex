/**
 * ToOAServiceSoap.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.mossle.ws.oaclient;

public interface ToOAServiceSoap extends java.rmi.Remote {
    public com.mossle.ws.oaclient.GetAllSotreResponseGetAllSotreResult getAllSotre(java.lang.String strShopUserId, java.lang.String strRandom, java.lang.String signMsg, javax.xml.rpc.holders.StringHolder strError) throws java.rmi.RemoteException;
    public com.mossle.ws.oaclient.GetReturnOrderCompanyDataResponseGetReturnOrderCompanyDataResult getReturnOrderCompanyData(java.lang.String strShopUserId, java.lang.String strOrderNos, java.lang.String strRandom, java.lang.String signMsg, javax.xml.rpc.holders.StringHolder strError) throws java.rmi.RemoteException;

    /**
     * strUserID:经销商编号；strMsg：签名；Error：返回的错误信息
     */
    public void getUserInfo(java.lang.String strUserID, java.lang.String strMsg, com.mossle.ws.oaclient.holders.GetUserInfoResponseGetUserInfoResultHolder getUserInfoResult, javax.xml.rpc.holders.StringHolder error) throws java.rmi.RemoteException;

    /**
     * 获取订货产品，strUserID:经销商编号；strMsg：签名；Error：返回的错误信息
     */
    public java.lang.String getOrderProducts(java.lang.String strUserID, java.lang.String strMsg) throws java.rmi.RemoteException;
}
