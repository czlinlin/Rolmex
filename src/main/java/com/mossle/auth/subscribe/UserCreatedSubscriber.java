package com.mossle.auth.subscribe;

import java.io.IOException;

import javax.annotation.Resource;

import com.mossle.api.user.UserDTO;
import com.mossle.auth.component.AuthCache;
import com.mossle.core.id.IdGenerator;
import com.mossle.core.mapper.JsonMapper;
import com.mossle.core.subscribe.Subscribable;
import com.mossle.party.PartyConstants;
import com.mossle.user.PersonInfoConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Component("com.mossle.auth.component.UserCreatedSubscriber")
public class UserCreatedSubscriber implements Subscribable<String> {
    private static Logger logger = LoggerFactory
            .getLogger(UserCreatedSubscriber.class);
    private String insertSql = "INSERT INTO AUTH_USER_STATUS(ID,USERNAME,REF,STATUS,PASSWORD,USER_REPO_REF,TENANT_ID,ADD_USER_ID,DEL_FLAG) VALUES(?,?,?,1,'',?,?,?,?)";
    private String insertRoleUserSql = "INSERT INTO auth_user_role(USER_STATUS_ID,ROLE_ID,DEL_FLAG) VALUES(?,?,?)";
    private JsonMapper jsonMapper = new JsonMapper();
    private String destinationName = "queue.user.sync.created";
    private JdbcTemplate jdbcTemplate;
    private AuthCache authCache;
    private IdGenerator idGenerator;

    public void handleMessage(String message) {
        try {
            UserDTO userDto = jsonMapper.fromJson(message, UserDTO.class);
            String tenantId = userDto.getUserRepoRef();
            
            Long id = idGenerator.generateId();
            
            jdbcTemplate.update(insertSql, id ,
                    userDto.getUsername(), userDto.getId(), tenantId, tenantId, userDto.getAddUserId(), PersonInfoConstants.DELETE_FLAG_NO);
            
            String adminUserId = Long.toString(PartyConstants.ADMIN_USER_ID);
            
            if (adminUserId.equals(userDto.getAddUserId())) {
            	// 新增用户,系统自动挂角色(系统管理员)     zyl 2017-07-18
	            jdbcTemplate.update(insertRoleUserSql, id, 2, PersonInfoConstants.DELETE_FLAG_NO);
            } else {
	            // 新增用户,系统自动挂角色(普通用户)     zyl 2017-07-18
	            jdbcTemplate.update(insertRoleUserSql, id, 8, PersonInfoConstants.DELETE_FLAG_NO);
            }
            
            logger.info("create user : {}", message);
            authCache.evictUser(userDto.getId());
        } catch (IOException ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    public boolean isTopic() {
        return false;
    }

    public String getName() {
        return destinationName;
    }

    public void setDestinationName(String destinationName) {
        this.destinationName = destinationName;
    }

    @Resource
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Resource
    public void setAuthCache(AuthCache authCache) {
        this.authCache = authCache;
    }

    @Resource
    public void setIdGenerator(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }
}
