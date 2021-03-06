/**
 * ToOAServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.mossle.ws.oaclient;

public class ToOAServiceLocator extends org.apache.axis.client.Service implements com.mossle.ws.oaclient.ToOAService {

    public ToOAServiceLocator() {
    }


    public ToOAServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public ToOAServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for ToOAServiceSoap
    private java.lang.String ToOAServiceSoap_address = "http://192.168.226.53:7300/ToOAService.asmx";

    public java.lang.String getToOAServiceSoapAddress() {
        return ToOAServiceSoap_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String ToOAServiceSoapWSDDServiceName = "ToOAServiceSoap";

    public java.lang.String getToOAServiceSoapWSDDServiceName() {
        return ToOAServiceSoapWSDDServiceName;
    }

    public void setToOAServiceSoapWSDDServiceName(java.lang.String name) {
        ToOAServiceSoapWSDDServiceName = name;
    }

    public com.mossle.ws.oaclient.ToOAServiceSoap getToOAServiceSoap() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(ToOAServiceSoap_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getToOAServiceSoap(endpoint);
    }

    public com.mossle.ws.oaclient.ToOAServiceSoap getToOAServiceSoap(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            com.mossle.ws.oaclient.ToOAServiceSoapStub _stub = new com.mossle.ws.oaclient.ToOAServiceSoapStub(portAddress, this);
            _stub.setPortName(getToOAServiceSoapWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setToOAServiceSoapEndpointAddress(java.lang.String address) {
        ToOAServiceSoap_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (com.mossle.ws.oaclient.ToOAServiceSoap.class.isAssignableFrom(serviceEndpointInterface)) {
                com.mossle.ws.oaclient.ToOAServiceSoapStub _stub = new com.mossle.ws.oaclient.ToOAServiceSoapStub(new java.net.URL(ToOAServiceSoap_address), this);
                _stub.setPortName(getToOAServiceSoapWSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        java.lang.String inputPortName = portName.getLocalPart();
        if ("ToOAServiceSoap".equals(inputPortName)) {
            return getToOAServiceSoap();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://tempuri.org/", "ToOAService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://tempuri.org/", "ToOAServiceSoap"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("ToOAServiceSoap".equals(portName)) {
            setToOAServiceSoapEndpointAddress(address);
        }
        else 
{ // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}
