/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package com.mossle.common.utils;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.core.Authentication;
import org.springframework.web.method.HandlerMethod;

import com.mossle.api.audit.AuditConnector;
import com.mossle.api.audit.AuditDTO;
import com.mossle.api.tenant.TenantHolder;
import com.mossle.core.annotation.Log;
import com.mossle.core.spring.ApplicationContextHelper;
import com.mossle.security.impl.SpringSecurityUserAuth;
import com.mossle.security.util.SpringSecurityUtils;

/**
 * 字典工具类
 * @author ThinkGem
 * @version 2014-11-7
 */
public class LogUtils {
	
	private static AuditConnector auditConnector = ApplicationContextHelper.getBean(AuditConnector.class);
	private static TenantHolder tenantHolder = ApplicationContextHelper.getBean(TenantHolder.class);
	
	/**
	 * 保存日志
	 */
	public static void saveLog(HttpServletRequest request) throws Exception{
		saveLog(request, null, null);
	}
	
	/**
	 * 保存日志
	 */
	public static void saveLog(HttpServletRequest request, Object handler, Exception ex) throws Exception{
		
        Authentication authentication = SpringSecurityUtils.getAuthentication();
        if(authentication==null)
        	return;

        Object principal = authentication.getPrincipal();
        String userId = null;
        
        if (principal != null) { 
	        if (principal instanceof SpringSecurityUserAuth) {
	            userId = ((SpringSecurityUserAuth) principal).getId();
	        } else {
	            userId = authentication.getName();
	        }
        }
        // System.out.println("hander = "+handler);
        if (!(handler instanceof HandlerMethod)) {
            return;
        }
        
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method method = handlerMethod.getMethod();
        
        final Log log = method.getAnnotation(Log.class);
        if (log != null) {
        	if (userId != null){
				AuditDTO auditDto = new AuditDTO();
		        auditDto.setUserId(userId);
		        auditDto.setAuditTime(new Date());
		        auditDto.setAction(log.action());
		        auditDto.setResult(ex == null ? "成功" : "失败");
		        auditDto.setApplication("Rolmex");
		        auditDto.setClient(StringUtils.getRemoteAddr(request));
				auditDto.setServer(InetAddress.getLocalHost().getHostAddress());
				auditDto.setResourceType(log.desc());
				
		        auditDto.setTitle(log.operationDesc());
		        auditDto.setType(ex == null ? AuditDTO.TYPE_ACCESS : AuditDTO.TYPE_EXCEPTION);
		        auditDto.setUserAgent(request.getHeader("user-agent"));
		        auditDto.setRequestUri(request.getRequestURI());
		        auditDto.setParams(request.getParameterMap());
		        auditDto.setMethod(request.getMethod());
		        auditDto.setTenantId(tenantHolder.getTenantId());
		        new SaveLogThread(auditDto, handler, ex).start();
	        }
        }
	}
	
	/**
	 * 保存日志
	 */
	public static void saveLog(HttpServletRequest request, Method method, Exception ex) throws Exception{
		
        Authentication authentication = SpringSecurityUtils.getAuthentication();
        if(authentication==null)
        	return;

        Object principal = authentication.getPrincipal();
        String userId = null;
        
        if (principal != null) { 
	        if (principal instanceof SpringSecurityUserAuth) {
	            userId = ((SpringSecurityUserAuth) principal).getId();
	        } else {
	            userId = authentication.getName();
	        }
        }
        // System.out.println("hander = "+handler);
       /* if (!(handler instanceof HandlerMethod)) {
            return;
        }
        
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method method = handlerMethod.getMethod();*/
        
        final Log log = method.getAnnotation(Log.class);
        if (log != null) {
        	if (userId != null){
				AuditDTO auditDto = new AuditDTO();
		        auditDto.setUserId(userId);
		        auditDto.setAuditTime(new Date());
		        auditDto.setAction(log.action());
		        auditDto.setResult(ex == null ? "成功" : "失败");
		        auditDto.setApplication("Rolmex");
		        auditDto.setClient(StringUtils.getRemoteAddr(request));
				auditDto.setServer(InetAddress.getLocalHost().getHostAddress());
				auditDto.setResourceType(log.desc());
				
		        auditDto.setTitle(log.operationDesc());
		        auditDto.setType(ex == null ? AuditDTO.TYPE_ACCESS : AuditDTO.TYPE_EXCEPTION);
		        auditDto.setUserAgent(request.getHeader("user-agent"));
		        auditDto.setRequestUri(request.getRequestURI());
		        auditDto.setParams(request.getParameterMap());
		        auditDto.setMethod(request.getMethod());
		        auditDto.setTenantId(tenantHolder.getTenantId());
		        new SaveLogThread(auditDto, null, ex).start();
	        }
        }
	}
	
	/**
	 * 保存日志
	 */
	public static void saveAPPLog(HttpServletRequest request, String methodaction,String userId, Exception ex) throws Exception{
		
    	if (userId != null){
			AuditDTO auditDto = new AuditDTO();
	        auditDto.setUserId(userId);
	        auditDto.setAuditTime(new Date());
	        auditDto.setAction(methodaction);
	        auditDto.setResult(ex == null ? "成功" : "失败");
	        auditDto.setApplication("Rolmex-app");
	        auditDto.setClient(StringUtils.getRemoteAddr(request));
			auditDto.setServer(InetAddress.getLocalHost().getHostAddress());
			auditDto.setResourceType(methodaction);
			
	        auditDto.setTitle(methodaction);
	        auditDto.setType(ex == null ? AuditDTO.TYPE_ACCESS : AuditDTO.TYPE_EXCEPTION);
	        auditDto.setUserAgent(request.getHeader("user-agent"));
	        auditDto.setRequestUri(request.getRequestURI());
	        auditDto.setParams(request.getParameterMap());
	        auditDto.setMethod(request.getMethod());
	        auditDto.setTenantId(tenantHolder.getTenantId());
	        new SaveLogThread(auditDto, null, ex).start();
        }
	}
	
	/**
	 * 保存日志线程
	 */
	public static class SaveLogThread extends Thread{
		
		private AuditDTO log;
		private Object handler;
		private Exception ex;
		
		public SaveLogThread(AuditDTO log, Object handler, Exception ex){
			super(SaveLogThread.class.getSimpleName());
			this.log = log;
			this.handler = handler;
			this.ex = ex;
		}
		
		@Override
		public void run() {
			
			// 如果有异常，设置异常信息
			log.setException(Exceptions.getStackTraceAsString(ex));
			// 如果无标题并无异常日志，则不保存信息
			if (StringUtils.isBlank(log.getTitle()) && StringUtils.isBlank(log.getException())){
				return;
			}
			// 保存日志信息
			auditConnector.log(log);
		}
	}
}
