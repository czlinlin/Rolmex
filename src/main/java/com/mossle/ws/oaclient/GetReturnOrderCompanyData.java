/**
 * GetReturnOrderCompanyData.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.mossle.ws.oaclient;

public class GetReturnOrderCompanyData  implements java.io.Serializable {
    private java.lang.String strShopUserId;

    private java.lang.String strOrderNos;

    private java.lang.String strRandom;

    private java.lang.String signMsg;

    private java.lang.String strError;

    public GetReturnOrderCompanyData() {
    }

    public GetReturnOrderCompanyData(
           java.lang.String strShopUserId,
           java.lang.String strOrderNos,
           java.lang.String strRandom,
           java.lang.String signMsg,
           java.lang.String strError) {
           this.strShopUserId = strShopUserId;
           this.strOrderNos = strOrderNos;
           this.strRandom = strRandom;
           this.signMsg = signMsg;
           this.strError = strError;
    }


    /**
     * Gets the strShopUserId value for this GetReturnOrderCompanyData.
     * 
     * @return strShopUserId
     */
    public java.lang.String getStrShopUserId() {
        return strShopUserId;
    }


    /**
     * Sets the strShopUserId value for this GetReturnOrderCompanyData.
     * 
     * @param strShopUserId
     */
    public void setStrShopUserId(java.lang.String strShopUserId) {
        this.strShopUserId = strShopUserId;
    }


    /**
     * Gets the strOrderNos value for this GetReturnOrderCompanyData.
     * 
     * @return strOrderNos
     */
    public java.lang.String getStrOrderNos() {
        return strOrderNos;
    }


    /**
     * Sets the strOrderNos value for this GetReturnOrderCompanyData.
     * 
     * @param strOrderNos
     */
    public void setStrOrderNos(java.lang.String strOrderNos) {
        this.strOrderNos = strOrderNos;
    }


    /**
     * Gets the strRandom value for this GetReturnOrderCompanyData.
     * 
     * @return strRandom
     */
    public java.lang.String getStrRandom() {
        return strRandom;
    }


    /**
     * Sets the strRandom value for this GetReturnOrderCompanyData.
     * 
     * @param strRandom
     */
    public void setStrRandom(java.lang.String strRandom) {
        this.strRandom = strRandom;
    }


    /**
     * Gets the signMsg value for this GetReturnOrderCompanyData.
     * 
     * @return signMsg
     */
    public java.lang.String getSignMsg() {
        return signMsg;
    }


    /**
     * Sets the signMsg value for this GetReturnOrderCompanyData.
     * 
     * @param signMsg
     */
    public void setSignMsg(java.lang.String signMsg) {
        this.signMsg = signMsg;
    }


    /**
     * Gets the strError value for this GetReturnOrderCompanyData.
     * 
     * @return strError
     */
    public java.lang.String getStrError() {
        return strError;
    }


    /**
     * Sets the strError value for this GetReturnOrderCompanyData.
     * 
     * @param strError
     */
    public void setStrError(java.lang.String strError) {
        this.strError = strError;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof GetReturnOrderCompanyData)) return false;
        GetReturnOrderCompanyData other = (GetReturnOrderCompanyData) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.strShopUserId==null && other.getStrShopUserId()==null) || 
             (this.strShopUserId!=null &&
              this.strShopUserId.equals(other.getStrShopUserId()))) &&
            ((this.strOrderNos==null && other.getStrOrderNos()==null) || 
             (this.strOrderNos!=null &&
              this.strOrderNos.equals(other.getStrOrderNos()))) &&
            ((this.strRandom==null && other.getStrRandom()==null) || 
             (this.strRandom!=null &&
              this.strRandom.equals(other.getStrRandom()))) &&
            ((this.signMsg==null && other.getSignMsg()==null) || 
             (this.signMsg!=null &&
              this.signMsg.equals(other.getSignMsg()))) &&
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
        if (getStrShopUserId() != null) {
            _hashCode += getStrShopUserId().hashCode();
        }
        if (getStrOrderNos() != null) {
            _hashCode += getStrOrderNos().hashCode();
        }
        if (getStrRandom() != null) {
            _hashCode += getStrRandom().hashCode();
        }
        if (getSignMsg() != null) {
            _hashCode += getSignMsg().hashCode();
        }
        if (getStrError() != null) {
            _hashCode += getStrError().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(GetReturnOrderCompanyData.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/", ">GetReturnOrderCompanyData"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("strShopUserId");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "strShopUserId"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("strOrderNos");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "strOrderNos"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("strRandom");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "strRandom"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("signMsg");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "signMsg"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
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
