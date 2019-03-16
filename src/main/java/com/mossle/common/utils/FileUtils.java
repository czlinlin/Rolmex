/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package com.mossle.common.utils;

import java.io.File;
import java.io.IOException;

import org.apache.commons.fileupload.disk.DiskFileItem;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

/**
 * 字符串工具类, 继承org.apache.commons.lang3.StringUtils类
 * @author zyl
 * @version 2017-09-22
 */
public class FileUtils  {

	 /** 
     * MultipartFile 转换成File 
     *  
     * @param multfile 原文件类型 
     * @return File 
     * @throws IOException 
     */  
	public static File multipartToFile(MultipartFile multfile) throws IOException {  
        CommonsMultipartFile cf = (CommonsMultipartFile)multfile;   
        //这个myfile是MultipartFile的  
        DiskFileItem fi = (DiskFileItem) cf.getFileItem();  
        File file = fi.getStoreLocation();  
        //手动创建临时文件  
        if(file.length() < 2048){  
            File tmpFile = new File(System.getProperty("java.io.tmpdir") + System.getProperty("file.separator") +   
                    file.getName());  
            multfile.transferTo(tmpFile);  
            return tmpFile;  
        }  
        return file;  
    }
	
	public static String getSuffix(String name) {
        int lastIndex = name.lastIndexOf(".");

        if (lastIndex != -1) {
            return name.substring(lastIndex+1);
        } else {
            return "";
        }
    }
	
	public static String getFileKB(long byteFile){  
        if (byteFile==0) {
           return "0KB";  
        }
        long kb=1024;  
        return ""+byteFile/kb+"KB";  
    }  
    
	public static String getFileMB(long byteFile){  
        if (byteFile==0) { 
           return "0MB"; 
        }
        long mb=1024*1024;  
        return ""+byteFile/mb+"MB";  
    } 
}
