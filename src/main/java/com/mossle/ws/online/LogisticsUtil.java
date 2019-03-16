package com.mossle.ws.online;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;

import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.apache.log4j.Logger;

import com.rolmex.common.PropertiesFactory;
import com.rolmex.common.XYUtil;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;


public class LogisticsUtil {
	
	private static final Logger log = Logger.getLogger(LogisticsUtil.class);
	
	private static final String Algorithm = "DESede"; 
	
	private static SecureRandom rnd = new SecureRandom();
	
	static IvParameterSpec iv = new IvParameterSpec(rnd.generateSeed(8));
	/**
	 * 生成订单号 规则 "DP"+年月日时+五位随机数
	 * @return
	 */
	public static String getOrderId(){
		return "DP"+new SimpleDateFormat("yyyyMMddHH").format(new Date())+randomFiveCode();
	}
	
	public static String randomFiveCode(){
		return XYUtil.transformString((int)(Math.random()*10000)+1);
	}
	
	/**
	 * 物流系统md5加密
	 * @param orderCode
	 * @param key
	 * @param money
	 * @return
	 */
	/*public static String getMd5(String orderCode,String key,String money){
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(orderCode).append(key).append(money);
		stringBuilder = new StringBuilder(MD5Util.getMD5String(stringBuilder.toString()));
		return stringBuilder.toString();
	}*/
	/**  
     * Base64编码  
     *   
     * @param bstr  
     * @return String  
	 * @throws UnsupportedEncodingException 
     */    
	public static String encode(String bstr) throws UnsupportedEncodingException {
		return new BASE64Encoder().encode(bstr.getBytes("UTF-8"));
	}   
	/**
	 * url解码
	 * @param str
	 * @return
	 */
	public static String urlDecode(String str) {
		String result = "";
        if (null == str) {
            return "";
        }
        try {
            result = java.net.URLDecoder.decode(str, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
	}
    
    /**  
     * Base64解码  
     *   
     * @param str  
     * @return string  
     */    
	public static String decode(String str) {
		byte[] bt = null;
		try {
			BASE64Decoder decoder = new BASE64Decoder();
			bt = decoder.decodeBuffer(str);
			return new String(bt);
		} catch (IOException e) {
			log.debug("BASE64解码失败");
			e.printStackTrace();
		}	
		return null;
	}    

	/**
	 * 3DES加密方法
	 * 
	 * @param src
	 *            源数据的字节数组
	 * @return
	 */

	public static String encryptMode(byte[] src,String key) {
		try {
			SecretKey deskey = new SecretKeySpec(build3DesKey(key), Algorithm); // 生成密钥
			Cipher c1 = Cipher.getInstance("DESede/CBC/PKCS5Padding"); // 实例化负责加密/解密的Cipher工具类
			c1.init(Cipher.ENCRYPT_MODE, deskey,iv); // 初始化为加密模式
			byte[] result = c1.doFinal(src);
			return new String(result,"UTF-8");
		} catch (java.security.NoSuchAlgorithmException e1) {
			e1.printStackTrace();
			log.debug("3DES加密失败");
		} catch (javax.crypto.NoSuchPaddingException e2) {
			e2.printStackTrace();
			log.debug("3DES加密失败");
		} catch (java.lang.Exception e3) {
			e3.printStackTrace();
			log.debug("3DES加密失败");
		}
		return null;
	}

	/**
	 * 3DES解密函数
	 * 
	 * @param src
	 *            密文的字节数组
	 * @return
	 */
	public static String decryptMode(byte[] src,String key) {
		try {

			SecretKey deskey = new SecretKeySpec(build3DesKey(key), Algorithm);
			Cipher c1 = Cipher.getInstance("DESede/CBC/PKCS5Padding");
			c1.init(Cipher.DECRYPT_MODE, deskey,iv); // 初始化为解密模式
			byte[] b = c1.doFinal(src);
			return new String(b,"UTF-8");
		} catch (java.security.NoSuchAlgorithmException e1) {
			e1.printStackTrace();
			log.debug("3DES解密失败");
		} catch (javax.crypto.NoSuchPaddingException e2) {
			e2.printStackTrace();
			log.debug("3DES解密失败");
		} catch (java.lang.Exception e3) {
			e3.printStackTrace();
			log.debug("3DES解密失败");
		}
		return null;
	}
	
	/**
	 * 根据字符串生成密钥字节数组
	 * 
	 * @param keyStr 密钥字符串
	 * 
	 * @return
	 * 
	 * @throws UnsupportedEncodingException
	 */
	public static byte[] build3DesKey(String keyStr) throws UnsupportedEncodingException {
		byte[] key = new byte[24]; // 声明一个24位的字节数组，默认里面都是0
		byte[] temp = keyStr.getBytes("UTF-8"); // 将字符串转成字节数组

		/*
		 * 执行数组拷贝 System.arraycopy(源数组，从源数组哪里开始拷贝，目标数组，拷贝多少位)
		 */
		if (key.length > temp.length) {
			// 如果temp不够24位，则拷贝temp数组整个长度的内容到key数组中
			System.arraycopy(temp, 0, key, 0, temp.length);
		} else {
			// 如果temp大于24位，则拷贝temp数组24个长度的内容到key数组中
			System.arraycopy(temp, 0, key, 0, key.length);
		}
		return key;
	}
	
    // 3DESECB加密,key必须是长度大于等于 3*8 = 24 位哈
    public static String encryptThreeDESECB(final String src, final String key) throws Exception {
        final DESedeKeySpec dks = new DESedeKeySpec(key.getBytes("UTF-8"));
        final SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DESede");
        final SecretKey securekey = keyFactory.generateSecret(dks);

        final Cipher cipher = Cipher.getInstance("DESede/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, securekey);
        final byte[] b = cipher.doFinal(src.getBytes());
        final BASE64Encoder encoder = new BASE64Encoder();
        return URLEncoder.encode(encoder.encode(b).replaceAll("\r", "").replaceAll("\n", ""), "UTF-8");
    }

    // 3DESECB解密,key必须是长度大于等于 3*8 = 24 位哈
    public static String decryptThreeDESECB(final String src, final String key) throws Exception {
        // --通过base64,将字符串转成byte数组
        final BASE64Decoder decoder = new BASE64Decoder();
        final byte[] bytesrc = decoder.decodeBuffer(src);
        // --解密的key
        final DESedeKeySpec dks = new DESedeKeySpec(key.getBytes("UTF-8"));
        final SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DESede");
        final SecretKey securekey = keyFactory.generateSecret(dks);

        // --Chipher对象解密
        final Cipher cipher = Cipher.getInstance("DESede/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, securekey);
        final byte[] retByte = cipher.doFinal(bytesrc);

        return new String(retByte);
    }
	
	/**
	 * 获取配置文件参数
	 * @param key
	 * @param filename
	 * @return
	 */
	public static String getProp(String key, String filename) {
        String prop = PropertiesFactory.getInstance().getStringValue(String.format("properties/%s.properties", filename), key);
        return prop == null ? "" : prop;
    }
}
