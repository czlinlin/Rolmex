/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package com.mossle.core.spring.interceptor;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.servlet.ServletRequestContext;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.mossle.common.utils.FileUtils;

/**
 * 文件上传拦截器
 * @author zyl
 * @version 2014-8-19
 */
public class FileUploadInterceptor implements HandlerInterceptor {

	private long maxSize;  
	  
    @Override  
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {  
        if(request!=null && ServletFileUpload.isMultipartContent(request)) {  
            ServletRequestContext ctx = new ServletRequestContext(request);  
            long requestSize = ctx.contentLength();  
            if (requestSize > maxSize) { 
            	// System.out.println(request.getRequestURI());
            	// System.out.println(request.getContextPath());
            	// System.out.println(request.getServletPath());
            	
            	//response.sendRedirect(request.getContextPath() + "/maxFileUploadException/uploadExecptionHtml.do?maxSize=" + maxSize); 
            	
            	String fileSize = FileUtils.getFileMB(maxSize);
            	String title = "系统只允许上传小于等于" + fileSize + " 的文件！请您重新选择文件。";  
            	
            	StringBuffer sb = new StringBuffer();
                sb.append("<script language='javascript'>alert('");
                sb.append(title);
                
                
                sb.append("');history.go(-1);</script>");
                try {
                    // System.out.println(sb.toString());
                    response.setContentType("text/html; charset=utf-8");  
                    String strHtml = "<body style='background-color: #D0D0D0'>" ;
                    // strHtml += "<img src='image/win.gif' width='35' height='37'>";
                    strHtml += "</body>";
                    response.getWriter().println((strHtml));
                    response.getWriter().println(sb.toString());
                    response.getWriter().flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                
                // Thread.currentThread().sleep(10000); 
                
            	return false;
            	// response.sendRedirect(request.getRequestURI());
                // throw new MaxUploadSizeExceededException(maxSize);  
            }  
        }  
        return true;  
    }  
  
  
    @Override  
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {  
  
    }  
  
    @Override  
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {  
    }  
  
    public void setMaxSize(long maxSize) {  
        this.maxSize = maxSize;  
    }  

}
