package com.mossle.spi.store;

import javax.annotation.PostConstruct;

import com.mossle.core.store.FileStoreHelper;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Value;

public class LocalInternalStoreConnectorFactoryBean implements FactoryBean {
    private String baseDir;
    private InternalStoreConnector internalStoreConnector;

    @PostConstruct
    public void afterPropertiesSet() {
        FileStoreHelper fileStoreHelper = new FileStoreHelper();
        fileStoreHelper.setBaseDir(baseDir);

        LocalInternalStoreConnector localInternalStoreConnector = new LocalInternalStoreConnector();
        localInternalStoreConnector.setStoreHelper(fileStoreHelper);
        this.internalStoreConnector = localInternalStoreConnector;
    }

    public Object getObject() {
        return internalStoreConnector;
    }

    public Class getObjectType() {
        return InternalStoreConnector.class;
    }

    public boolean isSingleton() {
        return true;
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
