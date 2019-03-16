/**
 * WebServiceToSellSoap.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.mossle.ws.srmbclient;

public interface WebServiceToSellSoap extends java.rmi.Remote {

    /**
     * strUserID:经销商UserID；strActivationStatus：激活状态；strLockStatus：锁定状态；strFrozenStatus：冻结状态；strStatus：结果表示；strSignKey：加密Key
     */
    public boolean updAgencyStatus(java.lang.String strUserID, java.lang.String strActivationStatus, java.lang.String strLockStatus, java.lang.String strFrozenStatus, java.lang.String strStatus, java.lang.String strSignKey) throws java.rmi.RemoteException;

    /**
     * varBranch:部门编号；strSignKey：加密Key
     */
    public com.mossle.ws.srmbclient.GetAgencyDataResponseGetAgencyDataResult getAgencyData(java.lang.String varBranch, java.lang.String strSignKey) throws java.rmi.RemoteException;

    /**
     * dtShares:两列，第一列股权编号，第二列年份，strSignKey：加密Key
     */
    public java.lang.String updSharesStatus(com.mossle.ws.srmbclient.UpdSharesStatusDtShares dtShares, java.lang.String strSignKey) throws java.rmi.RemoteException;

    /**
     * 创建授权书
     */
    public java.lang.String createSAB(java.lang.String jsonStr) throws java.rmi.RemoteException;

    /**
     * 续约授权书
     */
    public java.lang.String renewalSAB(java.lang.String jsonStr) throws java.rmi.RemoteException;

    /**
     * 作废授权书
     */
    public java.lang.String invalidSAB(java.lang.String jsonStr) throws java.rmi.RemoteException;

    /**
     * 删除授权书
     */
    public java.lang.String deleteSAB(java.lang.String jsonStr) throws java.rmi.RemoteException;

    /**
     * 旗舰店授权书续约提醒
     */
    public java.lang.String renewalReminder(java.lang.String jsonStr) throws java.rmi.RemoteException;
}
