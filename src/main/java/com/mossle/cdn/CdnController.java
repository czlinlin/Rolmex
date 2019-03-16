package com.mossle.cdn;

import com.mossle.core.util.BaseDTO;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("cdn/api")
public class CdnController {
    private static Logger logger = LoggerFactory.getLogger(CdnController.class);
    private String baseUrl = "http://localhost:8080/mossle-app-cdn/cdn/";
    private String baseDir = "mossle.store/cdn";

    /*@RequestMapping("fetch")
    public BaseDTO fetch(
            @RequestParam("url") String url,
            @RequestParam("spaceName") String spaceName,
            @RequestParam(value = "targetFileName", required = false) String targetFileName)
            throws Exception {
        try {
            targetFileName = CdnUtils.copyUrlToFile(baseDir, url, spaceName,
                    targetFileName);

            BaseDTO baseDto = new BaseDTO();
            baseDto.setData(baseUrl + spaceName + "/" + targetFileName);

            return baseDto;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            BaseDTO baseDto = new BaseDTO();
            baseDto.setCode(500);
            baseDto.setMessage(ex.getMessage());

            return baseDto;
        }
    }

    @RequestMapping("upload")
    public BaseDTO upload(
            @RequestParam("file") MultipartFile multipartFile,
            @RequestParam("spaceName") String spaceName,
            @RequestParam(value = "targetFileName", required = false) String targetFileName)
            throws Exception {
        try {
            targetFileName = CdnUtils.copyMultipartFileToFile(baseDir,
                    multipartFile, spaceName, targetFileName);

            BaseDTO baseDto = new BaseDTO();
            baseDto.setData(baseUrl + spaceName + "/" + targetFileName);

            return baseDto;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            BaseDTO baseDto = new BaseDTO();
            baseDto.setCode(500);
            baseDto.setMessage(ex.getMessage());

            return baseDto;
        }
    }*/

    @Value("${cdn.baseUrl}")
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
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
}
