package com.mossle.base.rs;

import java.io.IOException;
import java.text.DateFormat;  
import java.text.ParseException;  
import java.text.SimpleDateFormat;  
import java.util.Date;
import java.io.StringReader;
import java.net.URLDecoder;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.rpc.holders.StringHolder;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mossle.api.user.UserConnector;
import com.mossle.api.user.UserDTO;
import com.mossle.common.utils.DateUtils;
import com.mossle.common.utils.PasswordUtil;
import com.mossle.core.auth.CurrentUserHolder;
import com.mossle.core.util.BaseDTO;
import com.mossle.core.util.Md5Utils;
import com.mossle.operation.persistence.domain.ExchangeProductsDTO;
import com.mossle.operation.service.CustomWorkService;
import com.mossle.base.persistence.domain.CustomerInfoDTO;
import com.mossle.base.persistence.domain.CustomerOrderInfoDTO;
import com.mossle.base.persistence.domain.CustomerStoreInfoDTO;
import com.mossle.party.PartyConstants;
import com.mossle.party.persistence.domain.PartyEntity;
import com.mossle.party.persistence.domain.PartyStruct;
import com.mossle.party.persistence.domain.PartyType;
import com.mossle.party.persistence.manager.PartyEntityManager;
import com.mossle.party.persistence.manager.PartyStructManager;
import com.mossle.party.persistence.manager.PartyTypeManager;
import com.mossle.party.service.PartyService;
import com.mossle.user.persistence.domain.AccountInfo;
import com.mossle.user.persistence.manager.AccountInfoManager;
import com.mossle.util.ExportUtil;
import com.mossle.util.StringUtil;
import com.mossle.ws.oaclient.GetAllSotreResponseGetAllSotreResult;
import com.mossle.ws.oaclient.GetReturnOrderCompanyDataResponseGetReturnOrderCompanyDataResult;
import com.mossle.ws.oaclient.GetUserInfoResponseGetUserInfoResult;
import com.mossle.ws.oaclient.ToOAServiceSoapProxy;
import com.mossle.ws.oaclient.holders.GetUserInfoResponseGetUserInfoResultHolder;
import com.mossle.ws.online.LogisticsUtil;
import com.rolmex.common.HttpRequester;
import com.rolmex.common.HttpRespons;
import com.rolmex.common.MD5Util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

@Component
@Path("varUser")

public class VarUserInfoResource {
    private static Logger logger = LoggerFactory.getLogger(VarUserInfoResource.class);
   
    
    private String oaUrl;
	private String oaKey;
	private String mallProductUrl;
	private String mallMd5Key;
	private String mallDesKey;
	private String mallOrderUrl;
	@Autowired
	private CustomWorkService customWorkService;
	
    @GET
    @Path("userInfo")
    @Produces(MediaType.APPLICATION_JSON)
    public CustomerInfoDTO getAllPartyTypes(@QueryParam("customerInfoId") String customerInfoId) {  
    	
        return getUserInfo(customerInfoId);
    }
    
    @GET
    @Path("orderInfo")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Map<String,Object>> getOrderInfos(@QueryParam("customerInfoId") String customerInfoId,
    		@QueryParam("orderInfoId") String orderInfoId) {   
        return getOrderInfo(customerInfoId,orderInfoId);
    }
    
    @GET
    @Path("orderInfo-forexchange")
    @Produces(MediaType.APPLICATION_JSON)
    public BaseDTO getOrderInfosForExchange(@QueryParam("customerInfoId") String customerInfoId,
    		@QueryParam("orderInfoId") String orderInfoId) {   
        return getOrderInfoForExchange(customerInfoId,orderInfoId);
    }
    
    @GET
    @Path("storeInfo")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Map<String,Object>> getStoreInfos(@QueryParam("customerInfoId") String customerInfoId) {   
        return getStoreInfo(customerInfoId);
    }
    
    @GET
    @Path("storeInfo-forexchange")
    @Produces(MediaType.APPLICATION_JSON)
    public BaseDTO getStoreInfosForExchange(@QueryParam("customerInfoId") String customerInfoId) {   
        return getStoreInfoForExchange(customerInfoId);
    }
    
    @GET
    @Path("productInfo")
    @Produces(MediaType.APPLICATION_JSON)
    public String getProductInfos(@QueryParam("customerInfoId") String customerInfoId) {   
    	return getProductInfo(customerInfoId);
    }
    
    @GET
    @Path("mallOrderInfo")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Map<String, Object>> getMallOrderInfos(@QueryParam("customerInfoId") String customerInfoId,
    		@QueryParam("orderInfoId") String orderInfoId) throws Exception {   
    	return getMallOrderInfo(customerInfoId,orderInfoId);
    	
    }
    
    @GET
    @Path("mallProductInfo")
    @Produces(MediaType.APPLICATION_JSON)
    public String getMallProductInfos() throws Exception {  
    	List<Map<String,Object>> mallProductInfo = getMallProductInfo();
        return JSONObject.toJSONString(mallProductInfo);
    }
    

	@GET
    @Path("mallOrderInfo-forexchange")
    @Produces(MediaType.APPLICATION_JSON)
    public BaseDTO getMallOrderInfosForExchanges(@QueryParam("customerInfoId") String customerInfoId,
    		@QueryParam("orderInfoId") String orderInfoId) throws IOException {   
    	return getMallOrderInfosForExchange(customerInfoId,orderInfoId);
    }
	
	@GET
    @Path("isMoreThanDate")
    @Produces(MediaType.APPLICATION_JSON)
    public CheckResult isMoreThanDate(@QueryParam("customerInfoId") String customerInfoId,
    		@QueryParam("orderNumber") String orderNumber) {   
    	return checkIsMoreThanDate(customerInfoId,orderNumber);
    }
	
	public CheckResult checkIsMoreThanDate(String customerInfoId , String orderNumber){
		CheckResult checkResult = new CheckResult();
		
		String[] array = orderNumber.split(",");
		int length = customerInfoId.length();
		
		if(length == 11){
			String dict = customWorkService.getDict("mallDate");
			int dictTime = Integer.parseInt(dict);
			//商城
			checkResult.setPlatform("mall");
			String orderDateStr="";
	    	//系统日期
	    	Date now = new Date();
	    	Calendar calendar = Calendar.getInstance();//日历对象
	        calendar.setTime(now);//设置当前日期

	        String yearStr = calendar.get(Calendar.YEAR)+"";//获取年份
	        int month = calendar.get(Calendar.MONTH) + 1;//获取月份
	        String monthStr = month < 10 ? "0" + month : month + "";
	        int day = calendar.get(Calendar.DATE);//获取日
	        String dayStr = day < 10 ? "0" + day : day + "";
	    	SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
	    	String nowStr = yearStr + monthStr + dayStr;
	    	//----------------系统日期结束---------------------------
	    	int days = 0;
	    	String orderOutThreeMonth="";
	    	String orderWrongful="";
	    	for(int i=0;i<array.length;i++){
	    		if(StringUtils.isNotBlank(array[i])){
	    			if(array[i].trim().length() != 14){
	    				checkResult.setOrderWrongful("订单输入错误");
		    			return checkResult;
	    			}
	    		}else{
	    			checkResult.setOrderWrongful("订单输入错误");
	    			return checkResult;
	    		}
	    		
	    		orderDateStr = array[i].substring(2, 8);
	    		orderDateStr = "20"+orderDateStr;
	    		//对截取的日期校验是否包含非数字
	    		boolean boo = ExportUtil.isNumeric(orderDateStr);
	    		if(!boo){
	    			orderWrongful += array[i]+",";
	    			continue;
	    		}
				try {
					Date orderDate = format.parse(orderDateStr);
					Date nowDate = format.parse(nowStr);
					days = (int) ((nowDate.getTime()-orderDate.getTime()) / (1000*3600*24));
					if(days > dictTime){
						orderOutThreeMonth += array[i]+",";
					}else if(days < 0){
						orderWrongful += array[i]+",";
					}
				} catch (ParseException e) {
					orderWrongful += array[i];
					checkResult.setOrderOut(orderOutThreeMonth);
			    	checkResult.setOrderWrongful(orderWrongful);
					return checkResult;
				}
	    	}
	    	if(orderOutThreeMonth.length()>0)
	    		checkResult.setOrderOut(orderOutThreeMonth.substring(0, orderOutThreeMonth.length()-1));
	    	if(orderWrongful.length()>0)
	    		checkResult.setOrderWrongful(orderWrongful.substring(0,orderWrongful.length()-1));
	    	return checkResult;
			
		}else{
			String dict = customWorkService.getDict("oaDate");
			int dictTime = Integer.parseInt(dict);
			String orderDateStr="";
	    	//系统日期
	    	Date now = new Date();
	    	Calendar calendar = Calendar.getInstance();//日历对象
	        calendar.setTime(now);//设置当前日期

	        String yearStr = calendar.get(Calendar.YEAR)+"";//获取年份
	        int month = calendar.get(Calendar.MONTH) + 1;//获取月份
	        String monthStr = month < 10 ? "0" + month : month + "";
	        int day = calendar.get(Calendar.DATE);//获取日
	        String dayStr = day < 10 ? "0" + day : day + "";
	    	SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
	    	String nowStr = yearStr + monthStr + dayStr;
	    	//----------------系统日期结束---------------------------
	    	int days = 0;
	    	String orderOutThreeMonth="";
	    	String orderWrongful="";
	    	for(int i=0;i<array.length;i++){
	    		if(StringUtils.isNotBlank(array[i])){
	    			if(array[i].trim().length() != 16){
	    				checkResult.setOrderWrongful("订单输入错误");
		    			return checkResult;
	    			}
	    		}else{
	    			checkResult.setOrderWrongful("订单输入错误");
	    			return checkResult;
	    		}
	    		
	    		
	    		orderDateStr = array[i].substring(5, 11);
	    		orderDateStr = "20"+orderDateStr;
	    		//对截取的日期校验是否包含非数字
	    		boolean boo = ExportUtil.isNumeric(orderDateStr);
	    		if(!boo){
	    			orderWrongful += array[i]+",";
	    			continue;
	    		}
				try {
					Date orderDate = format.parse(orderDateStr);
					Date nowDate = format.parse(nowStr);
					days = (int) ((nowDate.getTime()-orderDate.getTime()) / (1000*3600*24));
					if(days > dictTime){
						orderOutThreeMonth += array[i]+",";
					}else if(days < 0){
						orderWrongful += array[i]+",";
					}
				} catch (ParseException e) {
					orderWrongful += array[i];
					checkResult.setOrderOut(orderOutThreeMonth);
			    	checkResult.setOrderWrongful(orderWrongful);
					return checkResult;
				}
	    	}
	    	if(orderOutThreeMonth.length()>0)
	    		checkResult.setOrderOut(orderOutThreeMonth.substring(0, orderOutThreeMonth.length()-1));
	    	if(orderWrongful.length()>0)
	    		checkResult.setOrderWrongful(orderWrongful.substring(0,orderWrongful.length()-1));
	    	return checkResult;
		}
		
		
    	
    	
    }
    public static class CheckResult{
    	//商城还是oa
    	private String platform; //mall,oa
    	//超出三个月
    	private String orderOut;
    	//错误
    	private String orderWrongful;
		
		public String getPlatform() {
			return platform;
		}
		public void setPlatform(String platform) {
			this.platform = platform;
		}
		public String getOrderOut() {
			return orderOut;
		}
		public void setOrderOut(String orderOut) {
			this.orderOut = orderOut;
		}
		public String getOrderWrongful() {
			return orderWrongful;
		}
		public void setOrderWrongful(String orderWrongful) {
			this.orderWrongful = orderWrongful;
		}
    	
    }
	
	
    /**
     * 获取商城所有产品信息
     * @return
     * @throws Exception 
     */
	private List<Map<String, Object>> getMallProductInfo() throws Exception {
		HttpRequester http = new HttpRequester();
    	String productUrl = mallProductUrl;
    	String desKey = mallDesKey;
    	HttpRespons sendPost = http.sendPost(productUrl);
    	String content = sendPost.getContent();
    	JSONObject parseObject = JSONObject.parseObject(content);
    	String object = parseObject.get("args").toString();
    	object = URLDecoder.decode(object,"utf-8");
    	String decryptMode = LogisticsUtil.decryptThreeDESECB(object, desKey);
    	List<Map> resultList = JSONObject.parseArray(decryptMode, Map.class);
    	ArrayList<Map<String, Object>> dataList = new ArrayList<Map<String,Object>>();
		for (Map map2 : resultList) {
			HashMap<String, Object> dataMap = new HashMap<String, Object>();
			dataMap.put("varProductNO", map2.get("c_no"));
			dataMap.put("mnyPV", map2.get("pv"));
			dataMap.put("mnyPrice", map2.get("price"));
			dataMap.put("varProductName", map2.get("name"));
			dataMap.put("varCateNO", map2.get("c_id"));
			dataMap.put("varCateName", "");
			dataList.add(dataMap);
		}
		return dataList;
	}
	
	
    /**
     * 换货获取订单信息
     * @param customerInfoId
     * @param orderInfoId
     * @return
     */
    private BaseDTO getMallOrderInfosForExchange(String customerInfoId,
			String orderInfoId) {
    	BaseDTO baseDTO = new BaseDTO();
    	try {
    		
			List<Map<String,Object>> data = getMallOrderInfo(customerInfoId,orderInfoId);
			baseDTO.setData(data);
			baseDTO.setCode(200);
			baseDTO.setMessage("获取数据成功");
			return baseDTO;
		} catch (Exception e) {
			baseDTO.setCode(500);
			baseDTO.setMessage("获取订单信息错误，请检查专卖店编号和订单号后，重新获取");
			return baseDTO;
		}
	}

	/**
     * 通过手机号，订单号获取商城订单
     * @param phoneNum
     * @param orderInfoId
     * @return
     * @throws Exception 
     */
    private List<Map<String, Object>> getMallOrderInfo(String phoneNum,String orderInfoId) throws Exception{
    	
    	HttpRequester http = new HttpRequester();
    	String orderUrl = mallOrderUrl;
    	String md5Key = mallMd5Key;
    	String desKey = mallDesKey;
    	
    	String[] split = orderInfoId.split(",");
    	 
    	HashSet<String> set = new HashSet<String>();
    	for (String str : split) {
    		set.add(str.trim());
		}
    	orderInfoId = "";
    	for (String string : set) {
    		orderInfoId += string+",";
		}
    	//拼接参数
    	HashMap<String, Object> params = new HashMap<String, Object>();
    	params.put("mobile", phoneNum);
    	params.put("orderIds", orderInfoId);
    	params.put("str_sign", com.mossle.ws.online.MD5Util.getMd5(params, md5Key));
    	//des加密
    	String encryptMode = LogisticsUtil.encryptThreeDESECB(JSONObject.toJSONString(params), desKey);
    	HashMap<String, String> map = new HashMap<String, String>();
    	map.put("args", encryptMode);
    	//方法调用
    	HttpRespons sendPost = http.sendPost(orderUrl, map);
    	//返回结果集
    	String content = sendPost.getContent();
    	JSONObject parseObject = JSONObject.parseObject(content);
    	String object = parseObject.get("args").toString();
    	object = URLDecoder.decode(object,"utf-8");
    	//解密结果
    	String decryptMode = LogisticsUtil.decryptThreeDESECB(object, desKey);
    	List<Map> resultList = JSONObject.parseArray(decryptMode, Map.class);
    	
    	ArrayList<Map<String, Object>> dataList = new ArrayList<Map<String,Object>>();
    	int resultCount = 0;
    	
		for (Map map2 : resultList) {
			HashMap<String, Object> dataMap = new HashMap<String, Object>();
			boolean boo = true;
			//获取返回值的订单编号
			String resultNo = StringUtil.toString(map2.get("c_no"));
			//返回值的数量
			resultCount = Integer.parseInt(map2.get("goods_count").toString());
			//保存的结果集
			for (Map mapData : dataList) {
				String dataNo = StringUtil.toString(mapData.get("proNo"));
				if(resultNo.equals(dataNo)){
					int dataCount = Integer.parseInt(mapData.get("shopPVNum").toString());
					resultCount = resultCount+dataCount;
					mapData.put("shopPVNum", resultCount);
					boo = false;
					break;
				}
			}
			if(boo){
				dataMap.put("proNo", map2.get("c_no"));
				dataMap.put("shopPVNum", resultCount);
				dataMap.put("proName", map2.get("name"));
				dataMap.put("proPrice", map2.get("price"));
				dataMap.put("proPV", map2.get("pv"));
				dataMap.put("shopName", "0");
				dataMap.put("shopTel", "0");
				dataMap.put("shopRewardNum", "0");
				dataMap.put("shopRewardPV", "0");
				dataMap.put("shopWalletNum", "0");
				dataMap.put("shopWalletPV", "0");
				dataList.add(dataMap);
			}
			
		}
    	
    	return dataList;
    }
    /**
     * 通过Oa webservice 获取客户信息
     * @return
     */
    private CustomerInfoDTO getUserInfo(String customerInfoId) {
    	
    	String enpoint = oaUrl;
    	String key = oaKey;
    	
    	// System.out.println("---------------  远程服务地址:" + enpoint + " ------------------------"); 
    	CustomerInfoDTO customerInfoDTO = new CustomerInfoDTO();
		ToOAServiceSoapProxy to = new ToOAServiceSoapProxy(enpoint);
		
		System.out.println("---------------  服务连接成功 ------------------------"); 
		
		// String strUserID = "13061907";
		
		String strMsg = PasswordUtil.md5Hex(customerInfoId + key);  
		StringHolder error = new StringHolder();
		
		GetUserInfoResponseGetUserInfoResultHolder getUserInfoResult = new GetUserInfoResponseGetUserInfoResultHolder();
		
		
		try {
			//System.out.println("---------------  开始调用webservice ------------------------");  
			to.getUserInfo(customerInfoId, strMsg, getUserInfoResult, error);
			
			GetUserInfoResponseGetUserInfoResult a = getUserInfoResult.value;
			
			if (a == null) {
				return customerInfoDTO;
			}
			//System.out.println("---------------  开始获取数据 ------------------------");
			String  b = a.get_any()[1].toString();
			
			System.out.println("---------------  获取数据  " + b + " ------------------------");
			
			//1。获取DOM 解析器的工厂实例。  
            DocumentBuilderFactory factory  = DocumentBuilderFactory.newInstance();  
            //2。获得具体的DOM解析器。  
            DocumentBuilder builder = null;
			try {
				builder = factory.newDocumentBuilder();
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}  
              
            //3。获取文件  
            try {
            	StringReader sr = new StringReader(b); 
            	InputSource is = new InputSource(sr);
				Document document = builder.parse(is);
				
				//System.out.println("---------------  开始遍历返回数据 ------------------------");
				
				Element root = document.getDocumentElement();  
	            // System.out.println("--------------------- cat="+root.getAttribute("cat")); 
	            
	            NodeList list = root.getElementsByTagName("DocumentElement");  
	            
	            Map<String,Object> map = new HashMap<String,Object>(); 
	            
	            for (int i = 0; i < list.getLength(); i++) {  
	                Node lan = list.item(i);  
	                //System.out.println("id="+lan.getNodeType());  
	                //System.out.println("---------------");  
	                // Element lan =  (Element) list.item(i);  
	              
	                //获取子节点集合  
	                NodeList clist = lan.getChildNodes();  
	                for (int j = 0; j < clist.getLength(); j++) {  
	                    //获取下标  
	                    Node c = clist.item(j);  
	                    NodeList slist = c.getChildNodes();
	                    
	                    for (int k = 0; k < slist.getLength(); k++) {  
	                    	Node n = slist.item(k);
	                    	
	                    	if (n instanceof Element) {
	                    		// System.out.println(n.getNodeName()+"="+n.getTextContent());  
	                    		
	                    		map.put(n.getNodeName(), n.getTextContent()); 
	                    		if ("varUserID".equals(n.getNodeName())) {
	                    			customerInfoDTO.setId(n.getTextContent());
	                    		}
	                    		if ("varRealName".equals(n.getNodeName())) {
	                    			customerInfoDTO.setName(n.getTextContent());
	                    		}
	                    		if ("varRank".equals(n.getNodeName())) {
	                    			customerInfoDTO.setRank(n.getTextContent());
	                    		}
	                    		if ("varLevel".equals(n.getNodeName())) {
	                    			customerInfoDTO.setLevel(n.getTextContent());
	                    		}
	                    		if ("varFather".equals(n.getNodeName())) {
	                    			customerInfoDTO.setFather(n.getTextContent());
	                    		}
	                    		if ("varRe".equals(n.getNodeName())) {
	                    			customerInfoDTO.setRe(n.getTextContent());
	                    		}	
	                   
		                    	// DateFormat dFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S");        
	                    	
	                    		// DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	                    		
	                    		if ("dtmAddTime".equals(n.getNodeName())) {
	                    			
	                    			try {
	                    				// System.out.println("--------------- 注册日期：" + n.getTextContent());  
	                    				if(!StringUtils.isBlank(n.getTextContent())) {
											// customerInfoDTO.setAddTime(sdf.format(dFormat.parse(n.getTextContent())));
											
											// customerInfoDTO.setAddTime(dealDateFormat(n.getTextContent()));
		
											//if (StringUtils.isBlank(customerInfoDTO.getAddTime())) {  // 线上环境，无法取到值。通过字符截取的方式获取
												String strDate = n.getTextContent().substring(0,10) + " " + n.getTextContent().substring(11,19);
												
												// System.out.println("--------------- 截取注册日期:" + strDate + " ---------------------------");
												customerInfoDTO.setAddTime(strDate);
												// System.out.println("--------------- 截取日期注入实体类:" + customerInfoDTO.getAddTime() + " ---------------------------");
											//}
	                    				} else {
	                    					System.out.println("--------------- 无法取到注册日期 --------------- ");  
	                    				}
									} catch (DOMException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									} 
	                    			
	                    		}
	                    		
	                    		
	                    		if ("varMobile".equals(n.getNodeName())) {
	                    			customerInfoDTO.setMobile(n.getTextContent());
	                    		}
	                    		if ("varAddress".equals(n.getNodeName())) {
	                    			customerInfoDTO.setAddress(n.getTextContent());
	                    		}
	                    		if ("mnyTotal_A".equals(n.getNodeName())) {
	                    			customerInfoDTO.setTotalA(n.getTextContent());
	                    		}
	                    		if ("mnyTotal_B".equals(n.getNodeName())) {
	                    			customerInfoDTO.setTotalB(n.getTextContent());
	                    		}
	                    		if ("varPay".equals(n.getNodeName())) {
	                    			customerInfoDTO.setPay(n.getTextContent());
	                    		}
	                    		if ("varFreeze".equals(n.getNodeName())) {
	                    			customerInfoDTO.setFreeze(n.getTextContent());
	                    		}
	                    		if ("varLock".equals(n.getNodeName())) {
	                    			customerInfoDTO.setLock(n.getTextContent());
	                    		}
	                    		if ("varDirectName".equals(n.getNodeName())) {
	                    			customerInfoDTO.setVarDirectName(n.getTextContent());
	                    		}
	                    		if ("varDirectMobile".equals(n.getNodeName())) {
	                    			customerInfoDTO.setVarDirectMobile(n.getTextContent());
	                    		}
	                    		if ("varCardNO".equals(n.getNodeName())) {
	                    			customerInfoDTO.setVarCardNO(n.getTextContent());
	                    		}
	                    	}
	                    }
	                    
	                }  
	            } 
	            ObjectMapper mapper = new ObjectMapper(); 
	            
	            String strjson = mapper.writeValueAsString(map); 
	            System.out.println("---------------  返回数据：" + strjson + " ------------------------");
	            return customerInfoDTO;
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				System.out.println("---------------  错误信息：" + e.toString() + " ------------------------");
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("---------------  错误信息：" + e.toString() + " ------------------------");
				e.printStackTrace();
			}

		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
    }
    
    /**
     * 日期格式转换yyyy-MM-dd'T'HH:mm:ss.SSSXXX  (yyyy-MM-dd'T'HH:mm:ss.SSSZ) TO  yyyy-MM-dd HH:mm:ss
     * @throws ParseException
     */
    private String dealDateFormat(String oldDateStr) throws ParseException{
	     //此格式只有  jdk 1.7才支持  yyyy-MM-dd'T'HH:mm:ss.SSSXXX         
	     DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");  //yyyy-MM-dd'T'HH:mm:ss.SSSZ
	     Date  date = df.parse(oldDateStr);
	     SimpleDateFormat df1 = new SimpleDateFormat ("EEE MMM dd HH:mm:ss Z yyyy", Locale.UK);
	     Date date1 =  df1.parse(date.toString());
	     DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	   //  Date date3 =  df2.parse(date1.toString());
	     return df2.format(date1);
    }
    
    /**
     * 通过Oa webservice 获取仓库信息
     * @return
     */
    private List<Map<String,Object>> getStoreInfo(String strShopUserId) {
    	
    	String enpoint = oaUrl;
    	String key = oaKey;
    	
    	List<Map<String,Object>> customerInfoDTO = new ArrayList<Map<String,Object>>();
    	CustomerStoreInfoDTO customerInfo = new CustomerStoreInfoDTO();
		ToOAServiceSoapProxy to = new ToOAServiceSoapProxy(enpoint);
		
	
		StringHolder error = new StringHolder();
		
		GetUserInfoResponseGetUserInfoResultHolder getStoreInfoResult = new GetUserInfoResponseGetUserInfoResultHolder();
		
		try {
			Random rand=new Random();
	        int sj=(int)(Math.random()*100000);       //  生成0-100的随机数
	        
			String strRandom = Integer.toString(sj);
			String strSign = PasswordUtil.md5Hex(strShopUserId + strRandom + key);
			String  b = "";
			try {
				GetAllSotreResponseGetAllSotreResult result = to.getAllSotre(strShopUserId, strRandom, strSign, error);
				if (result == null) {
					return customerInfoDTO;
				}
				b = result.get_any()[1].toString();
			} catch (Exception ex) {
				ex.printStackTrace();
				return customerInfoDTO;
			}
			
			//1。获取DOM 解析器的工厂实例。  
            DocumentBuilderFactory factory  = DocumentBuilderFactory.newInstance();  
            //2。获得具体的DOM解析器。  
            DocumentBuilder builder = null;
			try {
				builder = factory.newDocumentBuilder();
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			}  
              
            //3。获取文件  
            try {
            	StringReader sr = new StringReader(b); 
            	InputSource is = new InputSource(sr);
				Document document = builder.parse(is);
				
				Element root = document.getDocumentElement();  
	            //System.out.println("cat="+root.getAttribute("cat")); 
	            
	            NodeList list = root.getElementsByTagName("DocumentElement");  
	            
	            
	            
	            for (int i = 0; i < list.getLength(); i++) {  
	                Node lan = list.item(i);  
	                //System.out.println("id="+lan.getNodeType());  
	                //System.out.println("---------------");  
	                // Element lan =  (Element) list.item(i);  
	                
	               
	                
	                //获取子节点集合  
	                NodeList clist = lan.getChildNodes();  
	                for (int j = 0; j < clist.getLength(); j++) {  
	                    //获取下标  
	                    Node c = clist.item(j);  
	                    NodeList slist = c.getChildNodes();
	                    
	                    Map<String,Object> map = new HashMap<String,Object>(); 
	                    
	                    for (int k = 0; k < slist.getLength(); k++) {  
	                    	Node n = slist.item(k);
	                    	
	                    	if (n instanceof Element) {
	                    		//System.out.println(n.getNodeName()+"="+n.getTextContent());  
	                    		
	                    		
	                    		if ("varStoCode".equals(n.getNodeName())) {
	                    			customerInfo.setVarStoCode(n.getTextContent());
	                    			map.put(n.getNodeName(), n.getTextContent()); 
	                    		}
	                    		if ("nvrStoName".equals(n.getNodeName())) {
	                    			customerInfo.setNvrStoName(n.getTextContent());
	                    			map.put(n.getNodeName(), n.getTextContent()); 
	                    		}
	                    		
	                    	}
	                    	 
	                    }
	                    customerInfoDTO.add(j,map);
	                } 
	            } 
	            ObjectMapper mapper = new ObjectMapper(); 
	            
	           //  String strjson = mapper.writeValueAsString(map); 
	            
	            return customerInfoDTO;
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
    }
    
    
    /**
     * 通过Oa webservice 获取订单信息
     * @return
     */
    private List<Map<String,Object>> getOrderInfo(String strShopUserId, String strOrderId) {
    	
    	String enpoint = oaUrl;
    	String key = oaKey;
    	
    	List<Map<String,Object>> customerInfoDTO = new ArrayList<Map<String,Object>>();
    	CustomerOrderInfoDTO customerInfo = new CustomerOrderInfoDTO();
		ToOAServiceSoapProxy to = new ToOAServiceSoapProxy(enpoint);
		
		// String strUserID = "13061907";
		
		String strMsg = PasswordUtil.md5Hex(strShopUserId + key);  
		StringHolder error = new StringHolder();
		
		try {
			
			Random rand=new Random();
	        int sj=(int)(Math.random()*100000);       //  生成0-100的随机数
	        //System.out.println("i:"+i); // 分别输出两个随机数
	        
			String strRandom = Integer.toString(sj);
			String strSign = PasswordUtil.md5Hex(strShopUserId  + strOrderId + strRandom + key); 
			String  b = "";
			try {
				GetReturnOrderCompanyDataResponseGetReturnOrderCompanyDataResult result = 
						to.getReturnOrderCompanyData(strShopUserId, strOrderId, strRandom, strSign, error);
				if (result == null) {
					return customerInfoDTO;
				}
				b = result.get_any()[1].toString();
			} catch (Exception ex) {
				ex.printStackTrace();
				return customerInfoDTO;
			}
			
			//1。获取DOM 解析器的工厂实例。  
            DocumentBuilderFactory factory  = DocumentBuilderFactory.newInstance();  
            //2。获得具体的DOM解析器。  
            DocumentBuilder builder = null;
			try {
				builder = factory.newDocumentBuilder();
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			}  
              
            //3。获取文件  
            try {
            	StringReader sr = new StringReader(b); 
            	InputSource is = new InputSource(sr);
				Document document = builder.parse(is);
				
				Element root = document.getDocumentElement();  
	            //System.out.println("cat="+root.getAttribute("cat")); 
	            
	            NodeList list = root.getElementsByTagName("DocumentElement");  
	            
	            
	            
	            for (int i = 0; i < list.getLength(); i++) {  
	                Node lan = list.item(i);  
	                //System.out.println("id="+lan.getNodeType());  
	                //System.out.println("---------------");  
	                // Element lan =  (Element) list.item(i);  
	              
	                //获取子节点集合  
	                NodeList clist = lan.getChildNodes();  
	                for (int j = 0; j < clist.getLength(); j++) {  
	                    //获取下标  
	                    Node c = clist.item(j);  
	                    NodeList slist = c.getChildNodes();
	                    Map<String,Object> map = new HashMap<String,Object>();
	                    for (int k = 0; k < slist.getLength(); k++) {  
	                    	Node n = slist.item(k);
	                    	
	                    	if (n instanceof Element) {
	                    		//System.out.println(n.getNodeName()+"="+n.getTextContent()); 
	                    		
	                    		if ("proNo".equals(n.getNodeName())) {
	                    			customerInfo.setProNo(n.getTextContent());
	                    			map.put(n.getNodeName(), n.getTextContent());
	                    		}
	                    		if ("shopName".equals(n.getNodeName())) {
	                    			customerInfo.setShopName(n.getTextContent());
	                    			map.put(n.getNodeName(), n.getTextContent());
	                    		}
	                    		if ("shopTel".equals(n.getNodeName())) {
	                    			customerInfo.setShopTel(n.getTextContent());
	                    			map.put(n.getNodeName(), n.getTextContent());
	                    		}
	                    		if ("proName".equals(n.getNodeName())) {
	                    			customerInfo.setProName(n.getTextContent());
	                    			map.put(n.getNodeName(), n.getTextContent());
	                    		}
	                    		if ("shopPVNum".equals(n.getNodeName())) {
	                    			customerInfo.setShopPVNum(n.getTextContent());
	                    			map.put(n.getNodeName(), n.getTextContent());
	                    		}
	                    		if ("proPV".equals(n.getNodeName())) {
	                    			customerInfo.setProPV(n.getTextContent());
	                    			map.put(n.getNodeName(), n.getTextContent());
	                    		}
	                    		if ("shopRewardNum".equals(n.getNodeName())) {
	                    			customerInfo.setShopRewardNum(n.getTextContent());
	                    			map.put(n.getNodeName(), n.getTextContent());
	                    		}
	                    		if ("shopRewardPV".equals(n.getNodeName())) {
	                    			customerInfo.setShopRewardPV(n.getTextContent());
	                    			map.put(n.getNodeName(), n.getTextContent());
	                    		}
	                    		if ("shopWalletNum".equals(n.getNodeName())) {
	                    			customerInfo.setShopWalletNum(n.getTextContent());
	                    			map.put(n.getNodeName(), n.getTextContent());
	                    		}
	                    		if ("shopWalletPV".equals(n.getNodeName())) {
	                    			customerInfo.setShopWalletPV(n.getTextContent());
	                    			map.put(n.getNodeName(), n.getTextContent());
	                    		}
	                    		if ("proPrice".equals(n.getNodeName())) {
	                    			customerInfo.setProPrice(n.getTextContent());
	                    			map.put(n.getNodeName(), n.getTextContent());
	                    		}
	                    	}
	                    }
	                    customerInfoDTO.add(j,map);
	                }  
	            } 
	            //ObjectMapper mapper = new ObjectMapper(); 
	            
	            //String strjson = mapper.writeValueAsString(map); 
	            
	            return customerInfoDTO;
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
    }
    
    /**
     * 通过Oa webservice 获取仓库信息(针对换货)
     * @return
     */
    private BaseDTO getStoreInfoForExchange(String strShopUserId) {
    	
    	String enpoint = oaUrl;
    	String key = oaKey;
    	
    	BaseDTO baseDTO=new BaseDTO();
    	List<Map<String,Object>> customerInfoDTO = new ArrayList<Map<String,Object>>();
    	CustomerStoreInfoDTO customerInfo = new CustomerStoreInfoDTO();
		ToOAServiceSoapProxy to = new ToOAServiceSoapProxy(enpoint);
		
	
		StringHolder error = new StringHolder();
		
		GetUserInfoResponseGetUserInfoResultHolder getStoreInfoResult = new GetUserInfoResponseGetUserInfoResultHolder();
		
		try {
			Random rand=new Random();
	        int sj=(int)(Math.random()*100000);       //  生成0-100的随机数
	        
			String strRandom = Integer.toString(sj);
			String strSign = PasswordUtil.md5Hex(strShopUserId + strRandom + key);
			String  b = "";
			try {
				GetAllSotreResponseGetAllSotreResult result = to.getAllSotre(strShopUserId, strRandom, strSign, error);
				if(StringUtils.isNotBlank(error.value)){
					baseDTO.setCode(501);
					baseDTO.setMessage(error.value);
					return baseDTO;
				}
				if (result == null) {
					baseDTO.setCode(501);
					baseDTO.setMessage("获取仓库信息错误，请检查专卖店编号后，重新获取");
					return baseDTO;
				}
				b = result.get_any()[1].toString();
			} catch (Exception ex) {
				ex.printStackTrace();
				baseDTO.setCode(500);
				baseDTO.setMessage("获取仓库信息异常，请检查专卖店编号后，重新获取");
				return baseDTO;
			}
			
			//1。获取DOM 解析器的工厂实例。  
            DocumentBuilderFactory factory  = DocumentBuilderFactory.newInstance();  
            //2。获得具体的DOM解析器。  
            DocumentBuilder builder = null;
			try {
				builder = factory.newDocumentBuilder();
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			}  
              
            //3。获取文件  
            try {
            	StringReader sr = new StringReader(b); 
            	InputSource is = new InputSource(sr);
				Document document = builder.parse(is);
				
				Element root = document.getDocumentElement();  
	            //System.out.println("cat="+root.getAttribute("cat")); 
	            
	            NodeList list = root.getElementsByTagName("DocumentElement");
	            for (int i = 0; i < list.getLength(); i++) {  
	                Node lan = list.item(i);
	                //获取子节点集合  
	                NodeList clist = lan.getChildNodes();  
	                for (int j = 0; j < clist.getLength(); j++) {  
	                    //获取下标  
	                    Node c = clist.item(j);  
	                    NodeList slist = c.getChildNodes();
	                    
	                    Map<String,Object> map = new HashMap<String,Object>(); 
	                    
	                    for (int k = 0; k < slist.getLength(); k++) {  
	                    	Node n = slist.item(k);
	                    	if (n instanceof Element) {
	                    		if ("varStoCode".equals(n.getNodeName())) {
	                    			customerInfo.setVarStoCode(n.getTextContent());
	                    			map.put(n.getNodeName(), n.getTextContent()); 
	                    		}
	                    		if ("nvrStoName".equals(n.getNodeName())) {
	                    			customerInfo.setNvrStoName(n.getTextContent());
	                    			map.put(n.getNodeName(), n.getTextContent()); 
	                    		}
	                    	}
	                    	 
	                    }
	                    customerInfoDTO.add(j,map);
	                } 
	            } 
	            ObjectMapper mapper = new ObjectMapper();
	            
	            baseDTO.setCode(200);
	            baseDTO.setMessage("获取数据成功");
	            baseDTO.setData(customerInfoDTO);
				return baseDTO;
			} catch (SAXException e) {
				e.printStackTrace();
				baseDTO.setCode(500);
				baseDTO.setMessage("获取仓库信息错误，请检查专卖店编号后，重新获取");
				return baseDTO;
			} catch (IOException e) {
				e.printStackTrace();
				baseDTO.setCode(500);
				baseDTO.setMessage("获取仓库信息错误，请检查专卖店编号后，重新获取");
				return baseDTO;
			}

		} catch (Exception e) {
			e.printStackTrace();
			baseDTO.setCode(500);
			baseDTO.setMessage("获取仓库信息错误，请检查专卖店编号后，重新获取");
			return baseDTO;
		}
    }
    
    /**
     * 通过Oa webservice 获取订单信息(针对换货)
     * @return
     */
    private BaseDTO getOrderInfoForExchange(String strShopUserId, String strOrderId) {
    	String enpoint = oaUrl;
    	String key = oaKey;
    	BaseDTO baseDTO=new BaseDTO();
    	List<Map<String,Object>> customerInfoDTO = new ArrayList<Map<String,Object>>();
    	CustomerOrderInfoDTO customerInfo = new CustomerOrderInfoDTO();
		ToOAServiceSoapProxy to = new ToOAServiceSoapProxy(enpoint);
		
		// String strUserID = "13061907";
		
		String strMsg = PasswordUtil.md5Hex(strShopUserId + key);  
		StringHolder error = new StringHolder();
		
		try {
			
			Random rand=new Random();
	        int sj=(int)(Math.random()*100000);       //  生成0-100的随机数
	        //System.out.println("i:"+i); // 分别输出两个随机数
	        
			String strRandom = Integer.toString(sj);
			String strSign = PasswordUtil.md5Hex(strShopUserId  + strOrderId + strRandom + key); 
			String  b = "";
			try {
				GetReturnOrderCompanyDataResponseGetReturnOrderCompanyDataResult result = 
						to.getReturnOrderCompanyData(strShopUserId, strOrderId, strRandom, strSign, error);
				
				if(StringUtils.isNotBlank(error.value)){
					baseDTO.setCode(501);
					baseDTO.setMessage(error.value);
					return baseDTO;
				}
				if (result == null) {
					baseDTO.setCode(501);
					baseDTO.setMessage("获取订单信息错误，请检查专卖店编号和订单号后，重新获取");
					return baseDTO;
				}
				b = result.get_any()[1].toString();
			} catch (Exception ex) {
				ex.printStackTrace();
				baseDTO.setCode(500);
				baseDTO.setMessage("获取订单信息错误，请检查专卖店编号和订单号后，重新获取");
				return baseDTO;
			}
			
			//1。获取DOM 解析器的工厂实例。  
            DocumentBuilderFactory factory  = DocumentBuilderFactory.newInstance();  
            //2。获得具体的DOM解析器。  
            DocumentBuilder builder = null;
			try {
				builder = factory.newDocumentBuilder();
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			}  
              
            //3。获取文件  
            try {
            	StringReader sr = new StringReader(b); 
            	InputSource is = new InputSource(sr);
				Document document = builder.parse(is);
				
				Element root = document.getDocumentElement();  
	            //System.out.println("cat="+root.getAttribute("cat")); 
	            
	            NodeList list = root.getElementsByTagName("DocumentElement");  
	            
	            for (int i = 0; i < list.getLength(); i++) {  
	                Node lan = list.item(i);  
	                //System.out.println("id="+lan.getNodeType());  
	                //System.out.println("---------------");  
	                // Element lan =  (Element) list.item(i);  
	              
	                //获取子节点集合  
	                NodeList clist = lan.getChildNodes();  
	                for (int j = 0; j < clist.getLength(); j++) {  
	                    //获取下标  
	                    Node c = clist.item(j);  
	                    NodeList slist = c.getChildNodes();
	                    Map<String,Object> map = new HashMap<String,Object>();
	                    for (int k = 0; k < slist.getLength(); k++) {  
	                    	Node n = slist.item(k);
	                    	
	                    	if (n instanceof Element) {
	                    		//System.out.println(n.getNodeName()+"="+n.getTextContent()); 
	                    		
	                    		if ("proNo".equals(n.getNodeName())) {
	                    			customerInfo.setProNo(n.getTextContent());
	                    			map.put(n.getNodeName(), n.getTextContent());
	                    		}
	                    		if ("shopName".equals(n.getNodeName())) {
	                    			customerInfo.setShopName(n.getTextContent());
	                    			map.put(n.getNodeName(), n.getTextContent());
	                    		}
	                    		if ("shopTel".equals(n.getNodeName())) {
	                    			customerInfo.setShopTel(n.getTextContent());
	                    			map.put(n.getNodeName(), n.getTextContent());
	                    		}
	                    		if ("proName".equals(n.getNodeName())) {
	                    			customerInfo.setProName(n.getTextContent());
	                    			map.put(n.getNodeName(), n.getTextContent());
	                    		}
	                    		if ("shopPVNum".equals(n.getNodeName())) {
	                    			customerInfo.setShopPVNum(n.getTextContent());
	                    			map.put(n.getNodeName(), n.getTextContent());
	                    		}
	                    		if ("proPV".equals(n.getNodeName())) {
	                    			customerInfo.setProPV(n.getTextContent());
	                    			map.put(n.getNodeName(), n.getTextContent());
	                    		}
	                    		if ("shopRewardNum".equals(n.getNodeName())) {
	                    			customerInfo.setShopRewardNum(n.getTextContent());
	                    			map.put(n.getNodeName(), n.getTextContent());
	                    		}
	                    		if ("shopRewardPV".equals(n.getNodeName())) {
	                    			customerInfo.setShopRewardPV(n.getTextContent());
	                    			map.put(n.getNodeName(), n.getTextContent());
	                    		}
	                    		if ("shopWalletNum".equals(n.getNodeName())) {
	                    			customerInfo.setShopWalletNum(n.getTextContent());
	                    			map.put(n.getNodeName(), n.getTextContent());
	                    		}
	                    		if ("shopWalletPV".equals(n.getNodeName())) {
	                    			customerInfo.setShopWalletPV(n.getTextContent());
	                    			map.put(n.getNodeName(), n.getTextContent());
	                    		}
	                    		if ("proPrice".equals(n.getNodeName())) {
	                    			customerInfo.setProPrice(n.getTextContent());
	                    			map.put(n.getNodeName(), n.getTextContent());
	                    		}
	                    	}
	                    }
	                    customerInfoDTO.add(j,map);
	                }  
	            } 
	            //ObjectMapper mapper = new ObjectMapper(); 
	            
	            //String strjson = mapper.writeValueAsString(map); 
	            
	            baseDTO.setCode(200);
	            baseDTO.setMessage("获取数据成功");
	            baseDTO.setData(customerInfoDTO);
				return baseDTO;
			} catch (SAXException e) {
				e.printStackTrace();
				baseDTO.setCode(500);
				baseDTO.setMessage("获取订单信息错误，请检查专卖店编号和订单号后，重新获取");
				return baseDTO;
			} catch (IOException e) {
				e.printStackTrace();
				baseDTO.setCode(500);
				baseDTO.setMessage("获取订单信息错误，请检查专卖店编号和订单号后，重新获取");
				return baseDTO;
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			baseDTO.setCode(500);
			baseDTO.setMessage("获取订单信息错误，请检查专卖店编号和订单号后，重新获取");
			return baseDTO;
		}
    }
    
    public String getProductInfo(String customerInfoId){
    	String enpoint = oaUrl;
    	String key = oaKey;
    	
    	List<Map<String,Object>> exchangeProducts = new ArrayList<Map<String,Object>>();
    	ExchangeProductsDTO productsInfo = new ExchangeProductsDTO();
    	Object productListObj;
    	String products = "";
		ToOAServiceSoapProxy to = new ToOAServiceSoapProxy(enpoint);
		
		String strMsg = PasswordUtil.md5Hex(customerInfoId + key);  
		//StringHolder error = new StringHolder();
		try {
			String result = to.getOrderProducts(customerInfoId, strMsg);
			//System.out.println(result);
			System.out.println("-----------------------------------------------------------------");
			JSONObject obj = JSONObject.parseObject(result);
			productListObj = obj.get("ProductList");
			products = productListObj.toString();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
    	return products;
    }
    @Value("${oa.url}")
	public void setOaUrl(String oaUrl) {
		this.oaUrl = oaUrl;
	}

	@Value("${oa.key}")
	public void setOaKey(String oaKey) {
		this.oaKey = oaKey;
	}
	@Value("${mall.md5.key}")
	public void setMallMd5Key(String mallMd5Key) {
		this.mallMd5Key = mallMd5Key;
	}
	@Value("${mall.des.key}")
	public void setMallDesKey(String mallDesKey) {
		this.mallDesKey = mallDesKey;
	}
	@Value("${mall.product.url}")
	public void setMallProductUrl(String mallProductUrl) {
		this.mallProductUrl = mallProductUrl;
	}
	@Value("${mall.order.url}")
	public void setMallOrderUrl(String mallOrderUrl) {
		this.mallOrderUrl = mallOrderUrl;
	}
	
	
	
}
