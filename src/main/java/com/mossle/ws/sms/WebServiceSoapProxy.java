package com.mossle.ws.sms;

public class WebServiceSoapProxy implements com.mossle.ws.sms.WebServiceSoap {
  private String _endpoint = null;
  private com.mossle.ws.sms.WebServiceSoap webServiceSoap = null;
  
  public WebServiceSoapProxy() {
    _initWebServiceSoapProxy();
  }
  
  public WebServiceSoapProxy(String endpoint) {
    _endpoint = endpoint;
    _initWebServiceSoapProxy();
  }
  
  private void _initWebServiceSoapProxy() {
    try {
      webServiceSoap = (new com.mossle.ws.sms.WebServiceLocator()).getWebServiceSoap();
      if (webServiceSoap != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)webServiceSoap)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)webServiceSoap)._getProperty("javax.xml.rpc.service.endpoint.address");
      }
      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (webServiceSoap != null)
      ((javax.xml.rpc.Stub)webServiceSoap)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }
  
  public com.mossle.ws.sms.WebServiceSoap getWebServiceSoap() {
    if (webServiceSoap == null)
      _initWebServiceSoapProxy();
    return webServiceSoap;
  }
  
  public void sendMsg(java.lang.String strPhoneNo, java.lang.String strMsg, java.lang.String strEncryptMsg, java.lang.String strCooperName, javax.xml.rpc.holders.IntHolder sendMsgResult, javax.xml.rpc.holders.StringHolder strReturnDesc) throws java.rmi.RemoteException{
    if (webServiceSoap == null)
      _initWebServiceSoapProxy();
    webServiceSoap.sendMsg(strPhoneNo, strMsg, strEncryptMsg, strCooperName, sendMsgResult, strReturnDesc);
  }
  
  
}