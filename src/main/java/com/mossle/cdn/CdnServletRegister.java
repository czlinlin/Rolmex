package com.mossle.cdn;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.Filter;

import com.mossle.core.servlet.BeforeInvocationFilter;
import com.mossle.core.servlet.ProxyFilter;
import com.mossle.core.servlet.ServletFilter;
import com.mossle.core.servlet.StaticContentFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CdnServletRegister {
    private static Logger logger = LoggerFactory
            .getLogger(CdnServletRegister.class);
    private BeforeInvocationFilter beforeInvocationFilter;
    private String baseDir;

    @PostConstruct
    public void init() {
        this.addFirstFilter("cdn-o", "/cdn/o/*", new CdnStaticContentFilter(
                baseDir));
        this.addFirstFilter("cdn-r", "/cdn/r/*", new CdnStaticContentFilter(
                baseDir));
        this.addFirstFilter("cdn-public", "/cdn/public/*",
                new CdnStaticContentFilter(baseDir));
    }

    public void addFirstFilter(String name, String urlPattern, Filter filter) {
        logger.info("add first filter : {} {} {} ", name, urlPattern, filter);

        ProxyFilter proxyFilter = new ProxyFilter();
        proxyFilter.setName(name);
        proxyFilter.setFilter(filter);
        proxyFilter.setUrlPattern(urlPattern);

        this.beforeInvocationFilter.addFirstFilter(proxyFilter);
    }

    @Resource
    public void setBeforeInvocationFilter(
            BeforeInvocationFilter beforeInvocationFilter) {
        this.beforeInvocationFilter = beforeInvocationFilter;
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
