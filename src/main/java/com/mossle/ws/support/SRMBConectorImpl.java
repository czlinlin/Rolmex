package com.mossle.ws.support;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mossle.common.utils.DateUtils;
import com.mossle.core.mapper.JsonMapper;
import com.mossle.core.spring.SignInfo;
import com.mossle.operation.service.WSApplyService;
import com.mossle.api.srmb.SrmbDTO;
import com.mossle.api.srmb.SrmbConnector;
import com.mossle.ws.persistence.domain.OnLineInfo;
import com.mossle.ws.persistence.manager.OnLineInfoManager;

public class SRMBConectorImpl implements SrmbConnector {
	private static Logger logger = LoggerFactory.getLogger(WSApplyService.class);
	private SignInfo signInfo;
	private OnLineInfoManager onLineInfoManager;
	
	private JsonMapper jsonMapper = new JsonMapper();
	/*
	 * 创建授权书
	 * */
    public SrmbDTO CreateOrRenewalSAB(String applycode){
    	return ProcessSAB(applycode);
    }
    
    private SrmbDTO ProcessSAB(String applycode) {
    	SrmbDTO msgInfoDTO=new SrmbDTO();
    	//12万和非12万申请单审核结束（同意并授权），调用直销接口
		String endPoint=signInfo.getStockPlatUrl();
		com.mossle.ws.srmbclient.WebServiceToSellSoapProxy soapProxy
					=new com.mossle.ws.srmbclient.WebServiceToSellSoapProxy(endPoint);
		
		OnLineInfo onLineInfo=onLineInfoManager.findUniqueBy("applycode", applycode);
		if(onLineInfo!=null){
			String applyType=onLineInfo.getApplytype();
			Map<String,Object> requestMap=new HashMap<String, Object>();
			requestMap.put("userId", onLineInfo.getUcode());
			requestMap.put("userName", onLineInfo.getName());
			
			//部门转化
			String strBranch="F01";
			if(onLineInfo.getBranch().equals("二部"))
				strBranch="F02";
			else if(onLineInfo.getBranch().equals("四部"))
				strBranch="F04";
			requestMap.put("branch", strBranch);
			
			requestMap.put("cardId",onLineInfo.getIdentity());
			requestMap.put("address",onLineInfo.getBankaddress());
			
			/* 旗舰店申请，开始时间为：审核结束时间，
			 * 结束时间：审核结束时间往后顺延一年
			 */
			Date beginDate=new Date();
			
			Calendar calendar=new GregorianCalendar();
			calendar.setTime(beginDate);
			calendar.add(calendar.YEAR, 1);
			calendar.add(calendar.DATE, -1);
			Date enDate=calendar.getTime();
			if(applyType.equals("8")||applyType.equals("9"))
			{
				requestMap.put("beginDate", DateUtils.formatDate(beginDate,"yyyy-MM-dd")+" 00:00:00");
				requestMap.put("endDate", DateUtils.formatDate(enDate,"yyyy-MM-dd")+" 23:59:59");
			}
			else {
				requestMap.put("beginDate", DateUtils.formatDateTime(beginDate));
				requestMap.put("endDate", DateUtils.formatDateTime(enDate));
			}
			requestMap.put("source", "2");
			requestMap.put("remark", "");
			
			try {
				String strRequest=jsonMapper.toJson(requestMap);
				logger.info("applycode:"+onLineInfo.getApplycode()
						+"调用股票权益平台系统，请求json："+strRequest);
				byte[] byteRequest=strRequest.getBytes("gb2312");
				strRequest=org.apache.axis.encoding.Base64.encode(byteRequest);
				logger.info("id:"+onLineInfo.getApplycode()
						+"调用股票权益平台系统，请求base64："+strRequest);
				
				String strReturn="";
				if(applyType.equals("8")||applyType.equals("9"))
				{
					strReturn=soapProxy.createSAB(strRequest);
					logger.info("id:"+onLineInfo.getApplycode()
							+"调用股票权益平台系统-createSAB，返回结果base64："+strReturn);
				}
				else if(applyType.equals("10")||applyType.equals("11")){
					strReturn=soapProxy.renewalSAB(strRequest);
					logger.info("id:"+onLineInfo.getApplycode()
							+"调用股票权益平台系统-renewalSAB，返回结果base64："+strReturn);
				}else {
					msgInfoDTO.setStatus("ok");
					msgInfoDTO.setMsg("continue");
					return msgInfoDTO;
				}
				
				if(strReturn==null||strReturn.equals("")){
					logger.info("调用股票权益平台系统失败，返回结果为null或者为空");
					msgInfoDTO.setStatus("error");
					msgInfoDTO.setMsg("调用股票权益平台系统接口失败");
					return msgInfoDTO;
				}
				else {
					strReturn=new String(org.apache.axis.encoding.Base64.decode(strReturn),"GB2312");
					logger.info("id:"+onLineInfo.getApplycode()
							+"调用股票权益平台系统，返回结果json："+strReturn);
					
					Map<String,Object> returnMap=jsonMapper.fromJson(strReturn, Map.class);
					if(returnMap==null||!returnMap.containsKey("status")){
						logger.info("调用股票权益平台系统出错，返回结果为null或者返回结果不包括status的key值");
						msgInfoDTO.setStatus("error");
						msgInfoDTO.setMsg("调用股票权益平台系统接口出错");
						return msgInfoDTO;
					}	
					else {
						//200：处理成功；413：申请单创建授权书已存在（续约不会返回状态413）
						if(returnMap.get("status").toString().equals("200")){
							msgInfoDTO.setStatus("ok");
    						msgInfoDTO.setMsg("生成/续约授权书成功");
    						return msgInfoDTO;
						}
						else if(returnMap.get("status").toString().equals("413")
								||returnMap.get("status").toString().equals("414")){
							msgInfoDTO.setStatus("exist");
    						msgInfoDTO.setMsg(returnMap.get("message").toString());
    						return msgInfoDTO;	
							//jdbcTemplate.update("delete from time_task where id="+onLineInfo.getApplycode());
						}	
						else {
							logger.info("调用股票权益平台系统失败，返回结果"+strReturn);
							msgInfoDTO.setStatus("error");
    						msgInfoDTO.setMsg(returnMap.get("message").toString());
    						return msgInfoDTO;
						}	
					}	
				}							
			} catch (Exception ex) {
				logger.info("调用股票权益平台系统异常，id："+onLineInfo.getApplycode()
						+"，异常信息："+ex.getMessage()+"\r\n"+ex.getStackTrace());
				msgInfoDTO.setStatus("error");
				msgInfoDTO.setMsg("生成/续约授权书异常，请联系管理员！！！");
				return msgInfoDTO;
				
			}
		}
		return msgInfoDTO;
	}
    @Resource
	public void setOnLineInfoManager(OnLineInfoManager onLineInfoManager) {
		this.onLineInfoManager = onLineInfoManager;
	}
    
    @Resource
	public void setSignInfo(SignInfo signInfo) {
		this.signInfo = signInfo;
	}
}
