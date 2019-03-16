package com.mossle.ws.srmbclient;

public class WebServiceToSellSoapProxy implements com.mossle.ws.srmbclient.WebServiceToSellSoap {
  private String _endpoint = null;
  private com.mossle.ws.srmbclient.WebServiceToSellSoap webServiceToSellSoap = null;
  
  public WebServiceToSellSoapProxy() {
    _initWebServiceToSellSoapProxy();
  }
  
  public WebServiceToSellSoapProxy(String endpoint) {
    _endpoint = endpoint;
    _initWebServiceToSellSoapProxy();
  }
  
  private void _initWebServiceToSellSoapProxy() {
    try {
      webServiceToSellSoap = (new com.mossle.ws.srmbclient.WebServiceToSellLocator()).getWebServiceToSellSoap();
      if (webServiceToSellSoap != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)webServiceToSellSoap)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)webServiceToSellSoap)._getProperty("javax.xml.rpc.service.endpoint.address");
      }
      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (webServiceToSellSoap != null)
      ((javax.xml.rpc.Stub)webServiceToSellSoap)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }
  
  public com.mossle.ws.srmbclient.WebServiceToSellSoap getWebServiceToSellSoap() {
    if (webServiceToSellSoap == null)
      _initWebServiceToSellSoapProxy();
    return webServiceToSellSoap;
  }
  
  public boolean updAgencyStatus(java.lang.String strUserID, java.lang.String strActivationStatus, java.lang.String strLockStatus, java.lang.String strFrozenStatus, java.lang.String strStatus, java.lang.String strSignKey) throws java.rmi.RemoteException{
    if (webServiceToSellSoap == null)
      _initWebServiceToSellSoapProxy();
    return webServiceToSellSoap.updAgencyStatus(strUserID, strActivationStatus, strLockStatus, strFrozenStatus, strStatus, strSignKey);
  }
  
  public com.mossle.ws.srmbclient.GetAgencyDataResponseGetAgencyDataResult getAgencyData(java.lang.String varBranch, java.lang.String strSignKey) throws java.rmi.RemoteException{
    if (webServiceToSellSoap == null)
      _initWebServiceToSellSoapProxy();
    return webServiceToSellSoap.getAgencyData(varBranch, strSignKey);
  }
  
  public java.lang.String updSharesStatus(com.mossle.ws.srmbclient.UpdSharesStatusDtShares dtShares, java.lang.String strSignKey) throws java.rmi.RemoteException{
    if (webServiceToSellSoap == null)
      _initWebServiceToSellSoapProxy();
    return webServiceToSellSoap.updSharesStatus(dtShares, strSignKey);
  }
  
  public java.lang.String createSAB(java.lang.String jsonStr) throws java.rmi.RemoteException{
    if (webServiceToSellSoap == null)
      _initWebServiceToSellSoapProxy();
    return webServiceToSellSoap.createSAB(jsonStr);
  }
  
  public java.lang.String renewalSAB(java.lang.String jsonStr) throws java.rmi.RemoteException{
    if (webServiceToSellSoap == null)
      _initWebServiceToSellSoapProxy();
    return webServiceToSellSoap.renewalSAB(jsonStr);
  }
  
  public java.lang.String invalidSAB(java.lang.String jsonStr) throws java.rmi.RemoteException{
    if (webServiceToSellSoap == null)
      _initWebServiceToSellSoapProxy();
    return webServiceToSellSoap.invalidSAB(jsonStr);
  }
  
  public java.lang.String deleteSAB(java.lang.String jsonStr) throws java.rmi.RemoteException{
    if (webServiceToSellSoap == null)
      _initWebServiceToSellSoapProxy();
    return webServiceToSellSoap.deleteSAB(jsonStr);
  }
  
  public java.lang.String renewalReminder(java.lang.String jsonStr) throws java.rmi.RemoteException{
    if (webServiceToSellSoap == null)
      _initWebServiceToSellSoapProxy();
    return webServiceToSellSoap.renewalReminder(jsonStr);
  }
  
  
}