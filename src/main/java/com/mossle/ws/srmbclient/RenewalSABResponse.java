/**
 * RenewalSABResponse.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.mossle.ws.srmbclient;

public class RenewalSABResponse  implements java.io.Serializable {
    private java.lang.String renewalSABResult;

    public RenewalSABResponse() {
    }

    public RenewalSABResponse(
           java.lang.String renewalSABResult) {
           this.renewalSABResult = renewalSABResult;
    }


    /**
     * Gets the renewalSABResult value for this RenewalSABResponse.
     * 
     * @return renewalSABResult
     */
    public java.lang.String getRenewalSABResult() {
        return renewalSABResult;
    }


    /**
     * Sets the renewalSABResult value for this RenewalSABResponse.
     * 
     * @param renewalSABResult
     */
    public void setRenewalSABResult(java.lang.String renewalSABResult) {
        this.renewalSABResult = renewalSABResult;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof RenewalSABResponse)) return false;
        RenewalSABResponse other = (RenewalSABResponse) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.renewalSABResult==null && other.getRenewalSABResult()==null) || 
             (this.renewalSABResult!=null &&
              this.renewalSABResult.equals(other.getRenewalSABResult())));
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
        if (getRenewalSABResult() != null) {
            _hashCode += getRenewalSABResult().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(RenewalSABResponse.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/", ">RenewalSABResponse"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("renewalSABResult");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "RenewalSABResult"));
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
