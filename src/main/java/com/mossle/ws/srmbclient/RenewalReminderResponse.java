/**
 * RenewalReminderResponse.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.mossle.ws.srmbclient;

public class RenewalReminderResponse  implements java.io.Serializable {
    private java.lang.String renewalReminderResult;

    public RenewalReminderResponse() {
    }

    public RenewalReminderResponse(
           java.lang.String renewalReminderResult) {
           this.renewalReminderResult = renewalReminderResult;
    }


    /**
     * Gets the renewalReminderResult value for this RenewalReminderResponse.
     * 
     * @return renewalReminderResult
     */
    public java.lang.String getRenewalReminderResult() {
        return renewalReminderResult;
    }


    /**
     * Sets the renewalReminderResult value for this RenewalReminderResponse.
     * 
     * @param renewalReminderResult
     */
    public void setRenewalReminderResult(java.lang.String renewalReminderResult) {
        this.renewalReminderResult = renewalReminderResult;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof RenewalReminderResponse)) return false;
        RenewalReminderResponse other = (RenewalReminderResponse) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.renewalReminderResult==null && other.getRenewalReminderResult()==null) || 
             (this.renewalReminderResult!=null &&
              this.renewalReminderResult.equals(other.getRenewalReminderResult())));
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
        if (getRenewalReminderResult() != null) {
            _hashCode += getRenewalReminderResult().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(RenewalReminderResponse.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/", ">RenewalReminderResponse"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("renewalReminderResult");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "RenewalReminderResult"));
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
