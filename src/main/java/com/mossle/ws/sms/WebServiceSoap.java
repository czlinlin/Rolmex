/**
 * WebServiceSoap.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.mossle.ws.sms;

public interface WebServiceSoap extends java.rmi.Remote {
    public void sendMsg(java.lang.String strPhoneNo, java.lang.String strMsg, java.lang.String strEncryptMsg, java.lang.String strCooperName, javax.xml.rpc.holders.IntHolder sendMsgResult, javax.xml.rpc.holders.StringHolder strReturnDesc) throws java.rmi.RemoteException;
}
