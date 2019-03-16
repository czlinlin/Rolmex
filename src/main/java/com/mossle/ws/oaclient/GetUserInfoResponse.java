/**
 * GetUserInfoResponse.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.mossle.ws.oaclient;

public class GetUserInfoResponse  implements java.io.Serializable {
    private com.mossle.ws.oaclient.GetUserInfoResponseGetUserInfoResult getUserInfoResult;

    private java.lang.String error;

    public GetUserInfoResponse() {
    }

    public GetUserInfoResponse(
           com.mossle.ws.oaclient.GetUserInfoResponseGetUserInfoResult getUserInfoResult,
           java.lang.String error) {
           this.getUserInfoResult = getUserInfoResult;
           this.error = error;
    }


    /**
     * Gets the getUserInfoResult value for this GetUserInfoResponse.
     * 
     * @return getUserInfoResult
     */
    public com.mossle.ws.oaclient.GetUserInfoResponseGetUserInfoResult getGetUserInfoResult() {
        return getUserInfoResult;
    }


    /**
     * Sets the getUserInfoResult value for this GetUserInfoResponse.
     * 
     * @param getUserInfoResult
     */
    public void setGetUserInfoResult(com.mossle.ws.oaclient.GetUserInfoResponseGetUserInfoResult getUserInfoResult) {
        this.getUserInfoResult = getUserInfoResult;
    }


    /**
     * Gets the error value for this GetUserInfoResponse.
     * 
     * @return error
     */
    public java.lang.String getError() {
        return error;
    }


    /**
     * Sets the error value for this GetUserInfoResponse.
     * 
     * @param error
     */
    public void setError(java.lang.String error) {
        this.error = error;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof GetUserInfoResponse)) return false;
        GetUserInfoResponse other = (GetUserInfoResponse) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.getUserInfoResult==null && other.getGetUserInfoResult()==null) || 
             (this.getUserInfoResult!=null &&
              this.getUserInfoResult.equals(other.getGetUserInfoResult()))) &&
            ((this.error==null && other.getError()==null) || 
             (this.error!=null &&
              this.error.equals(other.getError())));
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getGetUserInfoResult() != null) {
            _hashCode += getGetUserInfoResult().hashCode();
        }
        if (getError() != null) {
            _hashCode += getError().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(GetUserInfoResponse.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/", ">GetUserInfoResponse"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("getUserInfoResult");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "GetUserInfoResult"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/", ">>GetUserInfoResponse>GetUserInfoResult"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("error");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "Error"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
    }

    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

    /**
     * Get Custom Serializer
     */
    public static org.apache.axis.encoding.Serializer getSerializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanSerializer(
            _javaType, _xmlType, typeDesc);
    }

    /**
     * Get Custom Deserializer
     */
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanDeserializer(
            _javaType, _xmlType, typeDesc);
    }

}
