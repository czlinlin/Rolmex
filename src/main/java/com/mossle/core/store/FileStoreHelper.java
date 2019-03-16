package com.mossle.core.store;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.activation.DataSource;
import javax.activation.FileDataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.FileCopyUtils;

public class FileStoreHelper implements StoreHelper {
	
    private static Logger logger = LoggerFactory.getLogger(FileStoreHelper.class);
    private String baseDir;

    public StoreResult getStore(String model, String key) throws Exception {
        if (key == null) {
            logger.info("key cannot be null");

            return null;
        }

        if (key.indexOf("../") != -1) {
            StoreResult storeResult = new StoreResult();
            storeResult.setModel(model);
            storeResult.setKey(key);

            return storeResult;
        }

        File file = new File(baseDir + "/" + model + "/" + key);

        if (!file.exists()) {
            logger.info("cannot find : {}", file);

            return null;
        }

        StoreResult storeResult = new StoreResult();
        storeResult.setModel(model);
        storeResult.setKey(key);
        storeResult.setDataSource(new FileDataSource(file));

        return storeResult;
    }
    
    public void removeStore(String model, String key) throws Exception {
        if (key.indexOf("../") != -1) {
            return;
        }

        File file = new File(baseDir + "/" + model + "/" + key);
        file.delete();
    }

    public StoreResult saveStore(String model, DataSource dataSource)
            throws Exception {
        String prefix = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String suffix = this.getSuffix(dataSource.getName());
        String path = prefix + "/" + UUID.randomUUID() + suffix;
        
        String all_path = baseDir + "/" + model + "/" + prefix;
        // System.out.println("=======================" + all_path + "======================");
        File dir = new File(all_path);
        
        if (!dir.exists()) {
            boolean success = dir.mkdirs();

            if (!success) {
                logger.error("cannot create directory : {}", dir);
            }
        }

        File targetFile = new File(baseDir + "/" + model + "/" + path);
        FileOutputStream fos = new FileOutputStream(targetFile);

        try {
            FileCopyUtils.copy(dataSource.getInputStream(), fos);
            fos.flush();
        } finally {
            fos.close();
        }

        StoreResult storeResult = new StoreResult();
        storeResult.setModel(model);
        storeResult.setKey(path);
        storeResult.setDataSource(new FileDataSource(targetFile));

        return storeResult;
    }

    public StoreResult saveStore(String model, String key, DataSource dataSource)
            throws Exception {
        String path = key;
        File dir = new File(baseDir + "/" + model);
        
        // System.out.println("===========ssss============baseDir/" + model + "======================");
        dir.mkdirs();

        File targetFile = new File(baseDir + "/" + model + "/" + path);
        FileOutputStream fos = new FileOutputStream(targetFile);

        try {
            FileCopyUtils.copy(dataSource.getInputStream(), fos);
            fos.flush();
        } finally {
            fos.close();
        }

        StoreResult storeResult = new StoreResult();
        storeResult.setModel(model);
        storeResult.setKey(path);
        storeResult.setDataSource(new FileDataSource(targetFile));

        return storeResult;
    }

    public void mkdir(String path) {
        File dir = new File(baseDir + "/" + path);
        boolean result = dir.mkdirs();
        logger.info("mkdir : {}", result);
    }

    public String getSuffix(String name) {
        int lastIndex = name.lastIndexOf(".");

        if (lastIndex != -1) {
            return name.substring(lastIndex);
        } else {
            return "";
        }
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
