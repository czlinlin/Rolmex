package com.mossle.common.web;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.mossle.common.utils.WebAPI;
import com.mossle.internal.store.persistence.domain.StoreInfo;
import com.mossle.internal.store.persistence.manager.StoreInfoManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("downloadAmachment")
public class DownLoadController {
	
    private static Logger logger = LoggerFactory.getLogger(DownLoadController.class);
    
    private WebAPI webAPI;
    private StoreInfoManager storeInfoManager;
    
    @RequestMapping("download")
    public String downloadAmachment(@RequestParam(value = "id", required = true) String id,
    		HttpServletRequest request, HttpServletResponse response) {
    	
        response.setContentType("text/html;charset=UTF-8");  
        try {  
            request.setCharacterEncoding("UTF-8");  
            
            StoreInfo storeInfo = storeInfoManager.get(Long.parseLong(id));
            if (storeInfo == null) {
            	return null;
            }
            BufferedInputStream bis = null;  
            BufferedOutputStream bos = null;  
            //此处使用的配置文件里面取出的文件服务器地址,拼凑成完整的文件服务器上的文件路径  
            String downLoadPath = webAPI.getDownloadUrl() + "/" + storeInfo.getPath();  
  
            response.setContentType("application/octet-stream");  
            response.reset();//清除response中的缓存  
            //根据网络文件地址创建URL  
            URL url = new URL(downLoadPath);  
            //获取此路径的连接  
            URLConnection conn = url.openConnection();  
              
            Long fileLength = conn.getContentLengthLong();//获取文件大小  
            //设置reponse响应头，真实文件名重命名，就是在这里设置，设置编码  
            String iso_filename = parseGBK(storeInfo.getName().replace(" ", "-"));
            response.setHeader("Content-disposition",  
                    "attachment; filename=" + iso_filename);  
            response.setHeader("Content-Length", String.valueOf(fileLength));  
  
            bis = new BufferedInputStream(conn.getInputStream());//构造读取流  
            bos = new BufferedOutputStream(response.getOutputStream());//构造输出流  
            byte[] buff = new byte[1024];  
            int bytesRead;  
            //每次读取缓存大小的流，写到输出流  
            while (-1 != (bytesRead = bis.read(buff, 0, buff.length))) {  
                bos.write(buff, 0, bytesRead);  
            }  
            response.flushBuffer();//将所有的读取的流返回给客户端  
            bis.close();  
            bos.close();  
  
        } catch (IOException e) {
        	logger.info(e.toString());
        }  
        return null;  
    } 
    
    public static String parseGBK(String sIn) {
		if (sIn == null || sIn.equals(""))
			return sIn;
		try {
			return new String(sIn.getBytes("GBK"), "ISO-8859-1");
		} catch (UnsupportedEncodingException usex) {
			return sIn;
		}
	}
    
    // ~ ======================================================================
    @Resource
	public void setWebAPI(WebAPI webAPI) {
		this.webAPI = webAPI;
	}

    @Resource
	public void setStoreInfoManager(StoreInfoManager storeInfoManager) {
		this.storeInfoManager = storeInfoManager;
	}
    
    
}
