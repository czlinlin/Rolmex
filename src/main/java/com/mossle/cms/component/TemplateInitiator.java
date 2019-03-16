package com.mossle.cms.component;

import java.io.File;
import java.io.FileOutputStream;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

@Component
public class TemplateInitiator implements ApplicationContextAware {
    private static Logger logger = LoggerFactory
            .getLogger(TemplateInitiator.class);
    private String baseDir;
    private ApplicationContext applicationContext;

    @PostConstruct
    public void init() throws Exception {
        File dir = new File(baseDir + "/cms/template/default");

        if (dir.exists()) {
            return;
        }

        dir.mkdirs();

        Resource[] resources = applicationContext
                .getResources("classpath:/cms/template/default/*");

        if (resources == null) {
            logger.info("cannot find default template for cms.");

            return;
        }

        for (Resource resource : resources) {
            File file = new File(dir, resource.getFilename());
            FileOutputStream fos = new FileOutputStream(file);

            try {
                FileCopyUtils.copy(resource.getInputStream(), fos);
                fos.flush();
            } finally {
                fos.close();
            }
        }
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

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
}
