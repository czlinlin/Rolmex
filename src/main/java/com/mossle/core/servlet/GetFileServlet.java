package com.mossle.core.servlet;

import java.io.IOException;
import java.net.URLDecoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.mossle.core.util.IoUtils;

public class GetFileServlet extends HttpServlet {
    public static final int DEBAULT_BUFFER_SIZE = 1024;
    private static final long serialVersionUID = 0L;
    private String baseDir = "/home/ckfinder/userfiles/";

    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        String requestUri = request.getRequestURI();
        requestUri = requestUri.substring(request.getContextPath().length());
        requestUri = requestUri.substring("/userfiles".length());

        String fileName = baseDir + URLDecoder.decode(requestUri, "UTF-8");
        IoUtils.copyFileToOutputStream(fileName, response.getOutputStream());
    }

    public void setBaseDir(String baseDir) {
    	
    	/*
    	//获取Tomcat服务器所在的路径
        String tomcat_path = System.getProperty( "user.dir" );  
        //获取Tomcat服务器所在路径的最后一个文件目录  
        String bin_path = tomcat_path.substring(tomcat_path.lastIndexOf("/")+1,tomcat_path.length()); 
        
        String all_path = "";
        if(("bin").equals(bin_path)){//手动启动Tomcat时获取路径为：D:\Software\Tomcat-8.5\bin  
            //获取保存上传图片的文件路径  
            all_path=tomcat_path.substring(0,System.getProperty("user.dir").lastIndexOf("/")) + "/webapps" ;       
        }else{//服务中自启动Tomcat时获取路径为：D:\Software\Tomcat-8.5  
            all_path = tomcat_path + "/webapps";   
        }  
        
        this.baseDir = all_path + "/" + baseDir;
        */
    	this.baseDir = baseDir;

    }
}
