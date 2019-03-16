package com.mossle.cdn;

import java.io.File;
import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.ServletContext;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

public class CdnStrategy {
    private static Logger logger = LoggerFactory.getLogger(CdnStrategy.class);
    private String baseDir;
    private boolean copyDir;
    private ServletContext servletContext;

    @PostConstruct
    public void init() throws Exception {
        if (copyDir) {
            this.copyResources();
        }
    }

    public void copyResources() throws IOException {
        // copy from webapp/cdn to mossle.store/cdn
        File srcDir = new File(servletContext.getRealPath("/") + "/cdn");
        File destDir = new File(baseDir);

        if (!destDir.exists()) {
            destDir.mkdirs();
        }

        logger.info("CDN copy from {} to {}", srcDir, destDir);
        FileUtils.copyDirectory(srcDir, destDir);
    }

    @Value("${cdn.baseDir}")
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

    @Value("${cdn.copyDir}")
    public void setCopyDir(boolean copyDir) {
        this.copyDir = copyDir;
    }

    @Resource
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }
}
