/**
 * ToOAServiceSoap.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.mossle.ws.oaapply;

public interface ToOAServiceSoap extends java.rmi.Remote {
    public java.lang.String updateFlagShopApply(java.lang.String strUserID, java.lang.String strBranch, boolean isSuccess, java.lang.String strName, java.lang.String strAddress, java.lang.String strCreditCode, java.lang.String strPicUrl, java.lang.String strIdentity, java.lang.String strEnterpriseName, java.lang.String strLegaler, java.lang.String strLegalCode, java.lang.String strDistributorPhone, java.lang.String strScopeBusiness, java.lang.String strNote, int isCreateAuth, java.lang.String strType, java.lang.String strShopArea, java.lang.String strMsg) throws java.rmi.RemoteException;
    public java.lang.String updateFlagRenew(java.lang.String strUserID, java.lang.String strBranch, java.lang.String strName, java.lang.String strAddress, java.lang.String strCreditCode, java.lang.String strPicUrl, java.lang.String strIdentity, java.lang.String strEnterpriseName, java.lang.String strLegaler, java.lang.String strLegalCode, java.lang.String strDistributorPhone, java.lang.String strScopeBusiness, java.lang.String strNote, java.lang.String strType, java.lang.String strShopArea, java.lang.String strMsg) throws java.rmi.RemoteException;
    public java.lang.String updateFlagProtocol(java.lang.String strUserID, java.lang.String strBranch, java.lang.String strName, java.lang.String strAddress, java.lang.String strCreditCode, java.lang.String strPicUrl, java.lang.String strIdentity, java.lang.String strEnterpriseName, java.lang.String strLegaler, java.lang.String strLegalCode, java.lang.String strDistributorPhone, java.lang.String strScopeBusiness, java.lang.String strNote, java.lang.String strType, java.lang.String strShopArea, java.lang.String strMsg) throws java.rmi.RemoteException;
    public java.lang.String updateAgentProtocol(java.lang.String strUserID, java.lang.String strBranch, java.lang.String strName, java.lang.String strAddress, java.lang.String strCreditCode, java.lang.String strPicUrl, java.lang.String strIdentity, java.lang.String strEnterpriseName, java.lang.String strLegaler, java.lang.String strLegalCode, java.lang.String strDistributorPhone, java.lang.String strScopeBusiness, java.lang.String strNote, java.lang.String strPublicAccount, java.lang.String strAccountType, java.lang.String strOpenName, java.lang.String strOpenBank, java.lang.String strAccountCode, java.lang.String strJson, java.lang.String strMsg) throws java.rmi.RemoteException;
}
