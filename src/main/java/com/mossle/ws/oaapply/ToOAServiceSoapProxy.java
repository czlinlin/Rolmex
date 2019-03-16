package com.mossle.ws.oaapply;

public class ToOAServiceSoapProxy implements com.mossle.ws.oaapply.ToOAServiceSoap {
  private String _endpoint = null;
  private com.mossle.ws.oaapply.ToOAServiceSoap toOAServiceSoap = null;
  
  public ToOAServiceSoapProxy() {
    _initToOAServiceSoapProxy();
  }
  
  public ToOAServiceSoapProxy(String endpoint) {
    _endpoint = endpoint;
    _initToOAServiceSoapProxy();
  }
  
  private void _initToOAServiceSoapProxy() {
    try {
      toOAServiceSoap = (new com.mossle.ws.oaapply.ToOAServiceLocator()).getToOAServiceSoap();
      if (toOAServiceSoap != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)toOAServiceSoap)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)toOAServiceSoap)._getProperty("javax.xml.rpc.service.endpoint.address");
      }
      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (toOAServiceSoap != null)
      ((javax.xml.rpc.Stub)toOAServiceSoap)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }
  
  public com.mossle.ws.oaapply.ToOAServiceSoap getToOAServiceSoap() {
    if (toOAServiceSoap == null)
      _initToOAServiceSoapProxy();
    return toOAServiceSoap;
  }
  
  public java.lang.String updateFlagShopApply(java.lang.String strUserID, java.lang.String strBranch, boolean isSuccess, java.lang.String strName, java.lang.String strAddress, java.lang.String strCreditCode, java.lang.String strPicUrl, java.lang.String strIdentity, java.lang.String strEnterpriseName, java.lang.String strLegaler, java.lang.String strLegalCode, java.lang.String strDistributorPhone, java.lang.String strScopeBusiness, java.lang.String strNote, int isCreateAuth, java.lang.String strType, java.lang.String strShopArea, java.lang.String strMsg) throws java.rmi.RemoteException{
    if (toOAServiceSoap == null)
      _initToOAServiceSoapProxy();
    return toOAServiceSoap.updateFlagShopApply(strUserID, strBranch, isSuccess, strName, strAddress, strCreditCode, strPicUrl, strIdentity, strEnterpriseName, strLegaler, strLegalCode, strDistributorPhone, strScopeBusiness, strNote, isCreateAuth, strType, strShopArea, strMsg);
  }
  
  public java.lang.String updateFlagRenew(java.lang.String strUserID, java.lang.String strBranch, java.lang.String strName, java.lang.String strAddress, java.lang.String strCreditCode, java.lang.String strPicUrl, java.lang.String strIdentity, java.lang.String strEnterpriseName, java.lang.String strLegaler, java.lang.String strLegalCode, java.lang.String strDistributorPhone, java.lang.String strScopeBusiness, java.lang.String strNote, java.lang.String strType, java.lang.String strShopArea, java.lang.String strMsg) throws java.rmi.RemoteException{
    if (toOAServiceSoap == null)
      _initToOAServiceSoapProxy();
    return toOAServiceSoap.updateFlagRenew(strUserID, strBranch, strName, strAddress, strCreditCode, strPicUrl, strIdentity, strEnterpriseName, strLegaler, strLegalCode, strDistributorPhone, strScopeBusiness, strNote, strType, strShopArea, strMsg);
  }
  
  public java.lang.String updateFlagProtocol(java.lang.String strUserID, java.lang.String strBranch, java.lang.String strName, java.lang.String strAddress, java.lang.String strCreditCode, java.lang.String strPicUrl, java.lang.String strIdentity, java.lang.String strEnterpriseName, java.lang.String strLegaler, java.lang.String strLegalCode, java.lang.String strDistributorPhone, java.lang.String strScopeBusiness, java.lang.String strNote, java.lang.String strType, java.lang.String strShopArea, java.lang.String strMsg) throws java.rmi.RemoteException{
    if (toOAServiceSoap == null)
      _initToOAServiceSoapProxy();
    return toOAServiceSoap.updateFlagProtocol(strUserID, strBranch, strName, strAddress, strCreditCode, strPicUrl, strIdentity, strEnterpriseName, strLegaler, strLegalCode, strDistributorPhone, strScopeBusiness, strNote, strType, strShopArea, strMsg);
  }
  
  public java.lang.String updateAgentProtocol(java.lang.String strUserID, java.lang.String strBranch, java.lang.String strName, java.lang.String strAddress, java.lang.String strCreditCode, java.lang.String strPicUrl, java.lang.String strIdentity, java.lang.String strEnterpriseName, java.lang.String strLegaler, java.lang.String strLegalCode, java.lang.String strDistributorPhone, java.lang.String strScopeBusiness, java.lang.String strNote, java.lang.String strPublicAccount, java.lang.String strAccountType, java.lang.String strOpenName, java.lang.String strOpenBank, java.lang.String strAccountCode, java.lang.String strJson, java.lang.String strMsg) throws java.rmi.RemoteException{
    if (toOAServiceSoap == null)
      _initToOAServiceSoapProxy();
    return toOAServiceSoap.updateAgentProtocol(strUserID, strBranch, strName, strAddress, strCreditCode, strPicUrl, strIdentity, strEnterpriseName, strLegaler, strLegalCode, strDistributorPhone, strScopeBusiness, strNote, strPublicAccount, strAccountType, strOpenName, strOpenBank, strAccountCode, strJson, strMsg);
  }
  
  
}