/**
 * UpdSharesStatus.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.mossle.ws.srmbclient;

public class UpdSharesStatus  implements java.io.Serializable {
    private com.mossle.ws.srmbclient.UpdSharesStatusDtShares dtShares;

    private java.lang.String strSignKey;

    public UpdSharesStatus() {
    }

    public UpdSharesStatus(
           com.mossle.ws.srmbclient.UpdSharesStatusDtShares dtShares,
           java.lang.String strSignKey) {
           this.dtShares = dtShares;
           this.strSignKey = strSignKey;
    }


    /**
     * Gets the dtShares value for this UpdSharesStatus.
     * 
     * @return dtShares
     */
    public com.mossle.ws.srmbclient.UpdSharesStatusDtShares getDtShares() {
        return dtShares;
    }


    /**
     * Sets the dtShares value for this UpdSharesStatus.
     * 
     * @param dtShares
     */
    public void setDtShares(com.mossle.ws.srmbclient.UpdSharesStatusDtShares dtShares) {
        this.dtShares = dtShares;
    }


    /**
     * Gets the strSignKey value for this UpdSharesStatus.
     * 
     * @return strSignKey
     */
    public java.lang.String getStrSignKey() {
        return strSignKey;
    }


    /**
     * Sets the strSignKey value for this UpdSharesStatus.
     * 
     * @param strSignKey
     */
    public void setStrSignKey(java.lang.String strSignKey) {
        this.strSignKey = strSignKey;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof UpdSharesStatus)) return false;
        UpdSharesStatus other = (UpdSharesStatus) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.dtShares==null && other.getDtShares()==null) || 
             (this.dtShares!=null &&
              this.dtShares.equals(other.getDtShares()))) &&
            ((this.strSignKey==null && other.getStrSignKey()==null) || 
             (this.strSignKey!=null &&
              this.strSignKey.equals(other.getStrSignKey())));
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
        if (getDtShares() != null) {
            _hashCode += getDtShares().hashCode();
        }
        if (getStrSignKey() != null) {
            _hashCode += getStrSignKey().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(UpdSharesStatus.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/", ">UpdSharesStatus"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("dtShares");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "dtShares"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/", ">>UpdSharesStatus>dtShares"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("strSignKey");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "strSignKey"));
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
