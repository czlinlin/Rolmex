package com.mossle.ws.oaclient;

public class ToOAServiceSoapProxy implements com.mossle.ws.oaclient.ToOAServiceSoap {
  private String _endpoint = null;
  private com.mossle.ws.oaclient.ToOAServiceSoap toOAServiceSoap = null;
  
  public ToOAServiceSoapProxy() {
    _initToOAServiceSoapProxy();
  }
  
  public ToOAServiceSoapProxy(String endpoint) {
    _endpoint = endpoint;
    _initToOAServiceSoapProxy();
  }
  
  private void _initToOAServiceSoapProxy() {
    try {
      toOAServiceSoap = (new com.mossle.ws.oaclient.ToOAServiceLocator()).getToOAServiceSoap();
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
  
  public com.mossle.ws.oaclient.ToOAServiceSoap getToOAServiceSoap() {
    if (toOAServiceSoap == null)
      _initToOAServiceSoapProxy();
    return toOAServiceSoap;
  }
  
  public com.mossle.ws.oaclient.GetAllSotreResponseGetAllSotreResult getAllSotre(java.lang.String strShopUserId, java.lang.String strRandom, java.lang.String signMsg, javax.xml.rpc.holders.StringHolder strError) throws java.rmi.RemoteException{
    if (toOAServiceSoap == null)
      _initToOAServiceSoapProxy();
    return toOAServiceSoap.getAllSotre(strShopUserId, strRandom, signMsg, strError);
  }
  
  public com.mossle.ws.oaclient.GetReturnOrderCompanyDataResponseGetReturnOrderCompanyDataResult getReturnOrderCompanyData(java.lang.String strShopUserId, java.lang.String strOrderNos, java.lang.String strRandom, java.lang.String signMsg, javax.xml.rpc.holders.StringHolder strError) throws java.rmi.RemoteException{
    if (toOAServiceSoap == null)
      _initToOAServiceSoapProxy();
    return toOAServiceSoap.getReturnOrderCompanyData(strShopUserId, strOrderNos, strRandom, signMsg, strError);
  }
  
  public void getUserInfo(java.lang.String strUserID, java.lang.String strMsg, com.mossle.ws.oaclient.holders.GetUserInfoResponseGetUserInfoResultHolder getUserInfoResult, javax.xml.rpc.holders.StringHolder error) throws java.rmi.RemoteException{
    if (toOAServiceSoap == null)
      _initToOAServiceSoapProxy();
    toOAServiceSoap.getUserInfo(strUserID, strMsg, getUserInfoResult, error);
  }
  
  public java.lang.String getOrderProducts(java.lang.String strUserID, java.lang.String strMsg) throws java.rmi.RemoteException{
    if (toOAServiceSoap == null)
      _initToOAServiceSoapProxy();
    return toOAServiceSoap.getOrderProducts(strUserID, strMsg);
  }
  
  
}