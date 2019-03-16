/**
 * InvalidSABResponse.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.mossle.ws.srmbclient;

public class InvalidSABResponse  implements java.io.Serializable {
    private java.lang.String invalidSABResult;

    public InvalidSABResponse() {
    }

    public InvalidSABResponse(
           java.lang.String invalidSABResult) {
           this.invalidSABResult = invalidSABResult;
    }


    /**
     * Gets the invalidSABResult value for this InvalidSABResponse.
     * 
     * @return invalidSABResult
     */
    public java.lang.String getInvalidSABResult() {
        return invalidSABResult;
    }


    /**
     * Sets the invalidSABResult value for this InvalidSABResponse.
     * 
     * @param invalidSABResult
     */
    public void setInvalidSABResult(java.lang.String invalidSABResult) {
        this.invalidSABResult = invalidSABResult;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof InvalidSABResponse)) return false;
        InvalidSABResponse other = (InvalidSABResponse) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.invalidSABResult==null && other.getInvalidSABResult()==null) || 
             (this.invalidSABResult!=null &&
              this.invalidSABResult.equals(other.getInvalidSABResult())));
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
        if (getInvalidSABResult() != null) {
            _hashCode += getInvalidSABResult().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(InvalidSABResponse.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/", ">InvalidSABResponse"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("invalidSABResult");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "InvalidSABResult"));
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
