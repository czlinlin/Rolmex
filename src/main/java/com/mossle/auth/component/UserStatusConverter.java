package com.mossle.auth.component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.mossle.api.tenant.TenantConnector;
import com.mossle.api.tenant.TenantDTO;

import com.mossle.auth.persistence.domain.Role;
import com.mossle.auth.persistence.domain.UserStatus;
import com.mossle.auth.persistence.manager.UserStatusManager;
import com.mossle.auth.support.UserStatusDTO;

import org.springframework.stereotype.Component;

@Component
public class UserStatusConverter {
    private TenantConnector tenantConnector;
    private UserStatusManager userStatusManager;

    public UserStatusDTO createUserStatusDto(UserStatus userStatus,
                                             String userRepoRef, String tenantId) {
        UserStatusDTO userStatusDto = new UserStatusDTO();
        userStatusDto.setId(userStatus.getId());
        userStatusDto.setUsername(userStatus.getUsername());
        userStatusDto.setEnabled(Integer.valueOf(1).equals(
                userStatus.getStatus()));
        userStatusDto.setRef(userStatus.getRef());
        userStatusDto.setDiaplayName(userStatus.getPassword());//别名  chengze  20180719
        userStatusDto.setRealName(userStatus.getUserRepoRef());//姓名 
        StringBuilder buff = new StringBuilder();

        for (Role role : userStatus.getRoles()) {
            if (tenantId.equals(role.getTenantId())) {
                buff.append(role.getName()).append(",");
            } else {
                TenantDTO tenantDto = tenantConnector.findById(role
                        .getTenantId());
                buff.append(role.getName()).append("(")
                        .append(tenantDto.getName()).append("),");
            }
        }

        if (buff.length() > 0) {
            buff.deleteCharAt(buff.length() - 1);
        }

        userStatusDto.setAuthorities(buff.toString());

        return userStatusDto;
    }

    public List<UserStatusDTO> createUserStatusDtos(
            List<Map<String, Object>> userStatuses, String userRepoRef, String tenantId) {
        List<UserStatusDTO> userStatusDtos = new ArrayList<UserStatusDTO>();

        for (Map<String, Object> map : userStatuses) {
            UserStatus userStatus = new UserStatus();
            userStatus.setId(Long.parseLong(convertString(map.get("id"))));
            userStatus.setRef(convertString(map.get("ref")));
            userStatus.setUsername(convertString(map.get("username")));
            userStatus.setAddUserId(convertString(map.get("add_User_Id")));
            userStatus.setUserRepoRef(convertString(map.get("user_Repo_Ref")));
            userStatus.setTenantId(convertString(map.get("tenant_Id")));
            userStatus.setStatus((int) map.get("status"));
            userStatus.setDelFlag(convertString(map.get("del_Flag")));
            userStatus.setPassword(convertString(map.get("display_name")));
            userStatus.setUserRepoRef(convertString(map.get("real_name")));
            UserStatus vo =userStatusManager.findUniqueBy("id",userStatus.getId());
            userStatus.setRoles(vo.getRoles());
            userStatusDtos.add(createUserStatusDto(userStatus, userRepoRef,
                    tenantId));
            
        }

      /*  for (int i = 0; i < userStatuses.size(); i++) {
            UserStatus userStatus = new UserStatus();
           *//* Object[] obj = (Object[])userStatuses.get(i);
            userStatus.setId((Long)obj[0]);
            userStatus.setPassword((String) obj[8]);
            userStatus.setStatus((int) obj[2]);
            userStatus.setRef((String) obj[3]);
            userStatus.setUserRepoRef((String ) obj[4]);
            userStatus.setTenantId((String) obj[5]);
            userStatus.setAddUserId((String) obj[6]);
            userStatus.setDelFlag((String) obj[7]);
            userStatus.setUsername((String) obj[9]);

            UserStatus vo =userStatusManager.findUniqueBy("id",userStatus.getId());
            userStatus.setRoles(vo.getRoles());*//*

            userStatusDtos.add(createUserStatusDto(userStatus, userRepoRef,
                    tenantId));
        }*/

        return userStatusDtos;
    }

    private String convertString(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof String) {
            return (String) value;
        }

        return value.toString();
    }

    @Resource
    public void setUserStatusManager(UserStatusManager userStatusManager) {
        this.userStatusManager = userStatusManager;
    }

    @Resource
    public void setTenantConnector(TenantConnector tenantConnector) {
        this.tenantConnector = tenantConnector;
    }
}
