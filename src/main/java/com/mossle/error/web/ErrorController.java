package com.mossle.error.web;

import java.io.UnsupportedEncodingException;

import javax.annotation.Resource;
import javax.xml.namespace.QName;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.mossle.core.auth.CurrentUserHolder;
import com.mossle.core.page.Page;

@Controller
@RequestMapping("error")
public class ErrorController {
	private static Logger logger = LoggerFactory.getLogger(ErrorController.class);
    private CurrentUserHolder currentUserHolder;

    @RequestMapping("error-info")
    public String submit( Model model,
    		@RequestParam(value="error",required=false) String error) 
    				throws UnsupportedEncodingException {
    	model.addAttribute("error", java.net.URLDecoder.decode(error,"utf-8"));
        return "error/error-info";
    }
    
    @RequestMapping("test")
    public String test(@ModelAttribute Page page, Model model) {
    	String strUserId=currentUserHolder.getUserId();
    	model.addAttribute("test",invokeRemoteFuc());
    	//model.addAllAttributes("date",DateUtils.formatDateTime(new Date());
        return "error/test";
    }
    
    public String invokeRemoteFuc() {
    	String result="请求失败！";
        try {  
        	//调用webservice地址      
            String url = "http://192.168.226.55:5006/WebService1.asmx?wsdl"; 
            //调用方法名
            String method="HelloWorld";
            Service service = new Service();
            //通过service创建call对象     
            Call call = (Call) service.createCall();
            //设置服务地址
            call.setTargetEndpointAddress(new java.net.URL(url)); 
            //设置调用方法
            call.setOperationName(new QName(method));
            call.setUseSOAPAction(true);
            call.setSOAPActionURI("http://tempuri.org/HelloWorld");
            //添加方法的参数，有几个添加几个
            //inLicense是参数名，XSD_STRING是参数类型，IN代表传入
            call.addParameter(new QName("content"), org.apache.axis.encoding.XMLType.XSD_STRING,javax.xml.rpc.ParameterMode.IN); 
            //call.addParameter("arg1", org.apache.axis.encoding.XMLType.XSD_INT,javax.xml.rpc.ParameterMode.IN);
            //设置返回类型  
            call.setReturnType(org.apache.axis.encoding.XMLType.XSD_STRING);
            call.setEncodingStyle("UTF-8");
            String content="test info";
            int num=1;
            try{
                //使用invoke调用方法，Object数据放传入的参数值
            	Object[] objs=new Object[]{content};
            	result =(String) call.invoke(objs);  
            }catch(Exception e){
                e.printStackTrace();
            }
            //输出SOAP请求报文
            System.out.println("--SOAP Request: " + call.getMessageContext().getRequestMessage().getSOAPPartAsString());
            //输出SOAP返回报文
            System.out.println("--SOAP Response: " + call.getResponseMessage().getSOAPPartAsString());
            //输出返回信息
        } catch (Exception e) {  
            e.printStackTrace();  
        }  

        return result;
    }
    
    @Resource
    public void setCurrentUserHolder(CurrentUserHolder currentUserHolder) {
        this.currentUserHolder = currentUserHolder;
    }
}
