package com.mossle.api.msg;

import java.io.IOException;
import java.util.Map;

import com.graphbuilder.math.func.AtanFunction;

/**
 * 华为，小米推送
 * add BY lilei {@link AtanFunction} 2018.09.03
 * **/
public interface MsgPushConnector{
	/***********
	 * 关联设备
	 * 推送接口关联设备
	 * ********/
    public void relationPhoneToekn(Map map) throws IOException;
    
    /***********
	 * 单条推送
	 * ********/
    public void sendPushOneMsg(Map map) throws IOException;
    
    /***********
	 * 全部推送
	 * ********/
    public void sendPushAllMsg(Map map) throws IOException;
}
