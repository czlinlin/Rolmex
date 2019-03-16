/**
 * GetReturnOrderCompanyDataResponse.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.mossle.ws.oaclient;

public class GetReturnOrderCompanyDataResponse  implements java.io.Serializable {
    private com.mossle.ws.oaclient.GetReturnOrderCompanyDataResponseGetReturnOrderCompanyDataResult getReturnOrderCompanyDataResult;

    private java.lang.String strError;

    public GetReturnOrderCompanyDataResponse() {
    }

    public GetReturnOrderCompanyDataResponse(
           com.mossle.ws.oaclient.GetReturnOrderCompanyDataResponseGetReturnOrderCompanyDataResult getReturnOrderCompanyDataResult,
           java.lang.String strError) {
           this.getReturnOrderCompanyDataResult = getReturnOrderCompanyDataResult;
           this.strError = strError;
    }


    /**
     * Gets the getReturnOrderCompanyDataResult value for this GetReturnOrderCompanyDataResponse.
     * 
     * @return getReturnOrderCompanyDataResult
     */
    public com.mossle.ws.oaclient.GetReturnOrderCompanyDataResponseGetReturnOrderCompanyDataResult getGetReturnOrderCompanyDataResult() {
        return getReturnOrderCompanyDataResult;
    }


    /**
     * Sets the getReturnOrderCompanyDataResult value for this GetReturnOrderCompanyDataResponse.
     * 
     * @param getReturnOrderCompanyDataResult
     */
    public void setGetReturnOrderCompanyDataResult(com.mossle.ws.oaclient.GetReturnOrderCompanyDataResponseGetReturnOrderCompanyDataResult getReturnOrderCompanyDataResult) {
        this.getReturnOrderCompanyDataResult = getReturnOrderCompanyDataResult;
    }


    /**
     * Gets the strError value for this GetReturnOrderCompanyDataResponse.
     * 
     * @return strError
     */
    public java.lang.String getStrError() {
        return strError;
    }


    /**
     * Sets the strError value for this GetReturnOrderCompanyDataResponse.
     * 
     * @param strError
     */
    public void setStrError(java.lang.String strError) {
        this.strError = strError;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof GetReturnOrderCompanyDataResponse)) return false;
        GetReturnOrderCompanyDataResponse other = (GetReturnOrderCompanyDataResponse) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.getReturnOrderCompanyDataResult==null && other.getGetReturnOrderCompanyDataResult()==null) || 
             (this.getReturnOrderCompanyDataResult!=null &&
              this.getReturnOrderCompanyDataResult.equals(other.getGetReturnOrderCompanyDataResult()))) &&
            ((this.strError==null && other.getStrError()==null) || 
             (this.strError!=null &&
              this.strError.equals(other.getStrError())));
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
        if (getGetReturnOrderCompanyDataResult() != null) {
            _hashCode += getGetReturnOrderCompanyDataResult().hashCode();
        }
        if (getStrError() != null) {
            _hashCode += getStrError().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(GetReturnOrderCompanyDataResponse.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/", ">GetReturnOrderCompanyDataResponse"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("getReturnOrderCompanyDataResult");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "GetReturnOrderCompanyDataResult"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/", ">>GetReturnOrderCompanyDataResponse>GetReturnOrderCompanyDataResult"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("strError");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "strError"));
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
