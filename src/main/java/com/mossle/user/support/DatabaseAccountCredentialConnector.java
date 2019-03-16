package com.mossle.user.support;

import java.util.Calendar;
import java.util.Date;

import javax.annotation.Resource;

import com.mossle.common.utils.StringUtils;
import com.mossle.spi.user.AccountCredentialConnector;
import com.mossle.util.DateUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

public class DatabaseAccountCredentialConnector implements
        AccountCredentialConnector {
    private static Logger logger = LoggerFactory
            .getLogger(DatabaseAccountCredentialConnector.class);
    private JdbcTemplate jdbcTemplate;
    private String sqlFindPassword = "SELECT AC.PASSWORD AS PASSWORD"
            + " FROM ACCOUNT_CREDENTIAL AC,ACCOUNT_INFO AI"
            + " WHERE AC.ACCOUNT_ID=AI.ID AND CATALOG='default' AND AI.DEL_FLAG='0' AND AI.USERNAME=? and AI.TENANT_ID=?";
    // ckx 查询是否离职
    private String sqlFindQuitTime = "SELECT PI.QUIT_TIME AS QUITTIME"
            + " FROM ACCOUNT_CREDENTIAL AC,ACCOUNT_INFO AI,PERSON_INFO PI"
            + " WHERE AC.ACCOUNT_ID=AI.ID AND AI.ID = PI.ID AND CATALOG='default' AND PI.QUIT_FLAG = '1' AND AI.USERNAME=? and AI.TENANT_ID=?";

    
    
    public String findPassword(String username, String tenantId) {
        if (username == null) {
            logger.info("username is null");

            return null;
        }

        username = username.toLowerCase();

        String password = null;
        String quitTime = null;

        try {
        	/*//ckx  增加离职人员禁止登录系统
        	try {
				quitTime = jdbcTemplate.queryForObject(sqlFindQuitTime,
				        String.class, username, tenantId);
			} catch (Exception e) {
			}
        	if(StringUtils.isNoneBlank(quitTime)){
        		//当前时间
        		Calendar calSearch=Calendar.getInstance();
        		calSearch.setTime(new Date());
        		
        		//禁止登录系统时间
        		quitTime = quitTime.substring(0, 10);
        		Date quitDate = DateUtil.formatDateStr(quitTime+" 18:00:00", "");
        		Calendar calQuit=Calendar.getInstance();
				calQuit.setTime(quitDate);
        		
				//如果查询时间大于离职时间
				if(calSearch.after(calQuit)){
					return password;
				}
        		
        	}*/
            password = jdbcTemplate.queryForObject(sqlFindPassword,
                    String.class, username, tenantId);
        } catch (Exception ex) {
            logger.info(ex.getMessage());
            logger.info("cannot find password : {}, {}", username, tenantId);
        }

        return password;
    }

    @Resource
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
}
