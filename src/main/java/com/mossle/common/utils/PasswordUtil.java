
package com.mossle.common.utils;

import java.security.MessageDigest;
import org.apache.commons.codec.binary.Hex;


/**
 * 
 */
public class PasswordUtil {

	/** 
     * 获取十六进制字符串形式的MD5摘要 
     */  
    public static String md5Hex(String src) {  
        try {  
            MessageDigest md5 = MessageDigest.getInstance("MD5");  
            byte[] bs = md5.digest(src.getBytes());  
            return new String(new Hex().encode(bs));  
        } catch (Exception e) {  
            return null;  
        }  
    }
    
    /** 
     * 获取十六进制字符串形式的MD5摘要 
     */  
    public static String getMD5(String src) {  
        try {  
            MessageDigest md5 = MessageDigest.getInstance("MD5");  
            byte[] bs = md5.digest(src.getBytes("utf-8"));  
            return new String(new Hex().encode(bs));  
        } catch (Exception e) {  
            return null;  
        }  
    }
}
