/**
 * GetOrderProducts.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.mossle.ws.oaclient;

public class GetOrderProducts  implements java.io.Serializable {
    private java.lang.String strUserID;

    private java.lang.String strMsg;

    public GetOrderProducts() {
    }

    public GetOrderProducts(
           java.lang.String strUserID,
           java.lang.String strMsg) {
           this.strUserID = strUserID;
           this.strMsg = strMsg;
    }


    /**
     * Gets the strUserID value for this GetOrderProducts.
     * 
     * @return strUserID
     */
    public java.lang.String getStrUserID() {
        return strUserID;
    }


    /**
     * Sets the strUserID value for this GetOrderProducts.
     * 
     * @param strUserID
     */
    public void setStrUserID(java.lang.String strUserID) {
        this.strUserID = strUserID;
    }


    /**
     * Gets the strMsg value for this GetOrderProducts.
     * 
     * @return strMsg
     */
    public java.lang.String getStrMsg() {
        return strMsg;
    }


    /**
     * Sets the strMsg value for this GetOrderProducts.
     * 
     * @param strMsg
     */
    public void setStrMsg(java.lang.String strMsg) {
        this.strMsg = strMsg;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof GetOrderProducts)) return false;
        GetOrderProducts other = (GetOrderProducts) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.strUserID==null && other.getStrUserID()==null) || 
             (this.strUserID!=null &&
              this.strUserID.equals(other.getStrUserID()))) &&
            ((this.strMsg==null && other.getStrMsg()==null) || 
             (this.strMsg!=null &&
              this.strMsg.equals(other.getStrMsg())));
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
        if (getStrUserID() != null) {
            _hashCode += getStrUserID().hashCode();
        }
        if (getStrMsg() != null) {
            _hashCode += getStrMsg().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(GetOrderProducts.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/", ">GetOrderProducts"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("strUserID");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "strUserID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("strMsg");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "strMsg"));
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
