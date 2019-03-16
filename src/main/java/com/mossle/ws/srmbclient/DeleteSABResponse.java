/**
 * DeleteSABResponse.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.mossle.ws.srmbclient;

public class DeleteSABResponse  implements java.io.Serializable {
    private java.lang.String deleteSABResult;

    public DeleteSABResponse() {
    }

    public DeleteSABResponse(
           java.lang.String deleteSABResult) {
           this.deleteSABResult = deleteSABResult;
    }


    /**
     * Gets the deleteSABResult value for this DeleteSABResponse.
     * 
     * @return deleteSABResult
     */
    public java.lang.String getDeleteSABResult() {
        return deleteSABResult;
    }


    /**
     * Sets the deleteSABResult value for this DeleteSABResponse.
     * 
     * @param deleteSABResult
     */
    public void setDeleteSABResult(java.lang.String deleteSABResult) {
        this.deleteSABResult = deleteSABResult;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof DeleteSABResponse)) return false;
        DeleteSABResponse other = (DeleteSABResponse) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.deleteSABResult==null && other.getDeleteSABResult()==null) || 
             (this.deleteSABResult!=null &&
              this.deleteSABResult.equals(other.getDeleteSABResult())));
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
        if (getDeleteSABResult() != null) {
            _hashCode += getDeleteSABResult().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(DeleteSABResponse.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/", ">DeleteSABResponse"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("deleteSABResult");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "DeleteSABResult"));
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
