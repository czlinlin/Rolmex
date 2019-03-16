package com.mossle.cms.web;

import java.io.File;

import javax.annotation.Resource;

import com.mossle.cms.export.NewsExporter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/cms")
public class CmsExportController {
    private JdbcTemplate jdbcTemplate;
    private String baseDir;

    @RequestMapping("doExport")
    public void doExport() throws Exception {
        new NewsExporter().doExport(new File(baseDir), jdbcTemplate);
    }

    @Resource
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Value("${store.baseDir}")
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
