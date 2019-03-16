package com.mossle.ws.online;



import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.Key;    

import javax.annotation.Resource;
import javax.crypto.Cipher;    
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;    
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.DESedeKeySpec;    
import javax.crypto.spec.IvParameterSpec;    
import javax.crypto.spec.SecretKeySpec;

import sun.misc.BASE64Decoder;    
import sun.misc.BASE64Encoder;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;

import com.mossle.common.utils.MD5Util;
import com.mossle.common.utils.PasswordUtil;

import com.mossle.core.spring.SignInfo;
import java.util.Properties;

public class Common
{
	/**
	 * 3DES解密的KEY，从直销OA从得到
	 * **/
	/*private static String key;
	private static String iv;
	private static String strSignKey="bService";*/
    
  //解密数据   
    public static String Decrypt3DES(String message,String key) throws Exception {
    	int keyLength=key.length();
    	if(keyLength>8)
    	{
    		key=key.substring(0,8);
    	}
    	else if(keyLength<8)
    	{
    		for (int i = 0; i < keyLength; i++) {
    			key+="0";
			}
    	}
        byte[] bytesrc =convertHexString(message);      
        Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");       
        DESKeySpec desKeySpec = new DESKeySpec(key.getBytes("UTF-8"));      
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");      
        SecretKey secretKey = keyFactory.generateSecret(desKeySpec);      
        IvParameterSpec iv = new IvParameterSpec(key.getBytes("UTF-8"));
               
        cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);         
         
        byte[] retByte = cipher.doFinal(bytesrc);
        String strReturnString=new String(retByte);
        return URLDecoder.decode(strReturnString,"utf-8");    
    } 
    
    public static String Encrypt3DES(String message,String key)   
            throws Exception {   
    	int keyLength=key.length();
    	if(keyLength>8)
    	{
    		key=key.substring(0,8);
    	}
    	else if(keyLength<8)
    	{
    		for (int i = 0; i < keyLength; i++) {
    			key+="0";
			}
    	}
	    Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");     
	    
        DESKeySpec desKeySpec = new DESKeySpec(key.getBytes("UTF-8"));     
    
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");     
        SecretKey secretKey = keyFactory.generateSecret(desKeySpec);     
        IvParameterSpec iv = new IvParameterSpec(key.getBytes("UTF-8"));     
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);     
    
        return toHexString(cipher.doFinal(message.getBytes("UTF-8")));   
    }

    /**
     * 加密
     * */
    public static String toHexString(byte b[]) {     
        StringBuffer hexString = new StringBuffer();     
        for (int i = 0; i < b.length; i++) {     
            String plainText = Integer.toHexString(0xff & b[i]);     
            if (plainText.length() < 2)     
                plainText = "0" + plainText;     
            hexString.append(plainText);     
        }     
             
        return hexString.toString();     
    }     
    
    /**
     * 解密
     * */
    public static byte[] convertHexString(String ss)    
    {    
	    byte digest[] = new byte[ss.length() / 2];    
	    for(int i = 0; i < digest.length; i++)    
	    {    
	    String byteString = ss.substring(2 * i, 2 * i + 2);    
	    int byteValue = Integer.parseInt(byteString, 16);    
	    digest[i] = (byte)byteValue;    
	    }    
	  
	    return digest;    
    }     
	
	/**
	 * MD5加密
	 * **/
	public static String Md5(String value)
	{
		value=PasswordUtil.getMD5(value);
		return value;
	}
	
	/// <summary>
    /// 验证签名
    /// </summary>
    /// <param name="signStr">参与编码字符串</param>
    /// <param name="signMsg">编码后字符串</param>
    /// <returns>验证结果（true：成功；false：失败）</returns>
    public static Boolean VerifySign(String signStr, String signMsg,String strSignKey)
    {
    	Boolean flag = false;
        signStr = signStr + strSignKey;
        signStr = Md5(signStr);
        if (signStr.toUpperCase().equals(signMsg.toUpperCase()))
            flag = true;
        return flag;
    }

}