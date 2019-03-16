package com.mossle.msg.support;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.mossle.api.msg.MsgPushConnector;
import com.mossle.core.mapper.JsonMapper;
import com.mossle.core.spring.SignInfo;
import com.mossle.core.util.StringUtils;
import com.mossle.operation.service.WSApplyService;
import com.rolmex.common.HttpRequester;
import com.rolmex.common.HttpRespons;
import com.sun.xml.bind.v2.TODO;
import com.graphbuilder.math.func.AtanFunction;

/**
 * 华为，小米推送
 * add BY lilei {@link AtanFunction} 2018.09.03
 * **/
@Service
public class MsgPushConnectorImpl implements MsgPushConnector{
	private static Logger logger = LoggerFactory.getLogger(MsgPushConnectorImpl.class);
	@Autowired
	private SignInfo signInfo;
	private JsonMapper jsonMapper = new JsonMapper();
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	/***********
	 * 关联设备
	 * 推送接口关联设备
	 * @throws IOException 
	 * ********/
    public void relationPhoneToekn(Map map) throws IOException{
    	/*user_id	是	

    	用户ID
    	identification	是	目标设备的token/regid
    	to_project	是	麦联项目：0
    	system	是	手机类型：0：Android ；1：IOS
    	channel	是	推送渠道：0:华为；1小米*/
    	String strMenthod="insert_token";
    	//strMenthod="insertToken";//旧的方法名到时候删除
    	try{
    		Map<String,Object> mapReturn=pushSendPost(map,strMenthod);
    		if(mapReturn.containsKey("status")){
    			String strStatus=mapReturn.get("status").toString();
    			if(strStatus.equals("0"))
    				logger.info(String.format("推送关联设备失败，请求方法：%s，请求参数：%s",strMenthod,jsonMapper.toJson(map)));
    		}
    	}
    	catch(Exception ex){
			logger.info(String.format("推送关联设备异常，请求方法：%s，请求参数：%s",strMenthod,jsonMapper.toJson(map)));
    	}
    }
    
    /***********
	 * 单条推送
	 * ********/
    public void sendPushOneMsg(Map map) throws IOException{
    	/*title	是	

    	消息的标题
    	user_id	是	用户ID
    	description	是	消息的描述 
    	to_project	是	麦联：0
    	to_project_name	是	麦联项目：mailian*/
    	String strId="";
    	String strMenthod="push_one";
    	if(map.containsKey("id")){
    		strId=map.get("id").toString();
    		map.remove("id");
    	}
    	
    	//设备为空，则不推送消息，直接更新即可
    	if(map.containsKey("deviceCode")){
    		if(StringUtils.isBlank(map.get("deviceCode").toString())){
    			//更新消息
				String strMsgSql="UPDATE MSG_INFO SET is_sendmsg='1' WHERE ID="+strId;
				jdbcTemplate.update(strMsgSql);
				logger.info(String.format("单推数据不需要推送，请求方法：%s，设备编号为空，请求参数：%s",strMenthod,jsonMapper.toJson(map)));
				return;
    		}
    	}
    	
    	try{
    		Map<String,Object> mapReturn=pushSendPost(map,strMenthod);
    		if(mapReturn.containsKey("status")){
    			String strStatus=mapReturn.get("status").toString();
    			if(strStatus.equals("0"))
    				logger.info(String.format("单推数据失败，请求方法：%s，请求参数：%s",strMenthod,jsonMapper.toJson(map)));
    			if(strStatus.equals("1")){
    				//更新消息为已推送
    				String strMsgSql="UPDATE MSG_INFO SET is_sendmsg='1' WHERE ID="+strId;
    				jdbcTemplate.update(strMsgSql);
    			}
    			else {
    				//更新消息为失败，状态为5 add by lilei at 2018-09-25
    				String strMsgSql="UPDATE MSG_INFO SET is_sendmsg='5' WHERE ID="+strId;
    				jdbcTemplate.update(strMsgSql);
				}
    		}
    	}
    	catch(Exception ex){
			//更新消息为失败，状态为5 add by lilei at 2018-09-25
			String strMsgSql="UPDATE MSG_INFO SET is_sendmsg='5' WHERE ID="+strId;
			jdbcTemplate.update(strMsgSql);
			logger.info(String.format("单推数据异常，请求方法：%s，请求参数：%s",strMenthod,jsonMapper.toJson(map)));
    	}
    }
    
    /***********
	 * 全部推送
	 * 
	 * 
	 * ********/
    public void sendPushAllMsg(Map map) throws IOException{
    	/*title	是	

    	消息的标题
    	description	是	消息的描述 
    	to_project	是	麦联项目：0
    	to_project_name	是	麦联项目：mailian*/
    	String strMenthod="push_all";
    	try{
    		Map<String,Object> mapReturn=pushSendPost(map,strMenthod);
    		if(mapReturn.containsKey("status")){
    			String strStatus=mapReturn.get("status").toString();
    			if(strStatus.equals("0"))
    				logger.info(String.format("单推数据失败，请求方法：%s，请求参数：%s",strMenthod,jsonMapper.toJson(map)));
    		}
    	}
    	catch(Exception ex){
			logger.info(String.format("单推数据异常，请求方法：%s，请求参数：%s",strMenthod,jsonMapper.toJson(map)));
    	}
    }
    
    private Map<String,Object> pushSendPost(Map mapParam,String strMethod){
    	Map<String,Object> mapJson=null;
    	try {
			HttpRequester request = new HttpRequester();
			HttpRespons response = null;
			logger.info(String.format("调用接口：%s，请求参数：%s",strMethod, jsonMapper.toJson(mapParam)));
			try {
				String strUrl=signInfo.getAppPushUrl()+strMethod+".json";
				response = request.sendPost(strUrl, mapParam);
			} catch (Exception e) {
				e.printStackTrace();
			}
			String strReturn = response.getContent();
			mapJson=jsonMapper.fromJson(strReturn,Map.class);
			logger.info(String.format("调用接口：%s，返回参数：%s",strMethod,strReturn));
			return mapJson;
		} catch (IOException e) {
			e.printStackTrace();
		}
    	return mapJson;
    }
}
