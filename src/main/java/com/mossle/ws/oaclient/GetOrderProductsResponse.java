/**
 * GetOrderProductsResponse.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.mossle.ws.oaclient;

public class GetOrderProductsResponse  implements java.io.Serializable {
    private java.lang.String getOrderProductsResult;

    public GetOrderProductsResponse() {
    }

    public GetOrderProductsResponse(
           java.lang.String getOrderProductsResult) {
           this.getOrderProductsResult = getOrderProductsResult;
    }


    /**
     * Gets the getOrderProductsResult value for this GetOrderProductsResponse.
     * 
     * @return getOrderProductsResult
     */
    public java.lang.String getGetOrderProductsResult() {
        return getOrderProductsResult;
    }


    /**
     * Sets the getOrderProductsResult value for this GetOrderProductsResponse.
     * 
     * @param getOrderProductsResult
     */
    public void setGetOrderProductsResult(java.lang.String getOrderProductsResult) {
        this.getOrderProductsResult = getOrderProductsResult;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof GetOrderProductsResponse)) return false;
        GetOrderProductsResponse other = (GetOrderProductsResponse) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.getOrderProductsResult==null && other.getGetOrderProductsResult()==null) || 
             (this.getOrderProductsResult!=null &&
              this.getOrderProductsResult.equals(other.getGetOrderProductsResult())));
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
        if (getGetOrderProductsResult() != null) {
            _hashCode += getGetOrderProductsResult().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(GetOrderProductsResponse.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/", ">GetOrderProductsResponse"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("getOrderProductsResult");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "GetOrderProductsResult"));
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
