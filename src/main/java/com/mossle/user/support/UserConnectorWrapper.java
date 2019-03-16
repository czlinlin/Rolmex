package com.mossle.user.support;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.criteria.Join;

import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.google.common.base.Joiner;
import com.mossle.api.user.UserCache;
import com.mossle.api.user.UserConnector;
import com.mossle.api.user.UserDTO;
import com.mossle.core.page.Page;

public class UserConnectorWrapper implements UserConnector {
    private UserConnector userConnector;
    private UserCache userCache;

    public UserDTO findById(String id) {
        UserDTO userDto = userCache.findById(id);

        if (userDto == null) {
            synchronized (userCache) {
                userDto = userCache.findById(id);

                if (userDto == null) {
                    userDto = userConnector.findById(id);

                    if (userDto != null) {
                        userCache.updateUser(userDto);
                    }
                }
            }
        }

        return userDto;
    }

    @Override
    public UserDTO findByIdAll(String id) {
        UserDTO userDto = userCache.findByIdAll(id);

        if (userDto == null) {
            synchronized (userCache) {
                userDto = userCache.findByIdAll(id);

                if (userDto == null) {
                    userDto = userConnector.findByIdAll(id);

                    if (userDto != null) {
                        userCache.updateUser(userDto);
                    }
                }
            }
        }

        return userDto;
    }

    public UserDTO findByUsername(String username, String userRepoRef) {
        UserDTO userDto = userCache.findByUsername(username, userRepoRef);

        if (userDto == null) {
            synchronized (userCache) {
                userDto = userCache.findByUsername(username, userRepoRef);

                if (userDto == null) {
                    userDto = userConnector.findByUsername(username,
                            userRepoRef);

                    if (userDto != null) {
                        userCache.updateUser(userDto);
                    }
                }
            }
        }

        return userDto;
    }

    public UserDTO findByRef(String ref, String userRepoRef) {
        UserDTO userDto = userCache.findByRef(ref, userRepoRef);

        if (userDto == null) {
            synchronized (userCache) {
                userDto = userCache.findByRef(ref, userRepoRef);

                if (userDto == null) {
                    userDto = userConnector.findByRef(ref, userRepoRef);

                    if (userDto != null) {
                        userCache.updateUser(userDto);
                    }
                }
            }
        }

        return userDto;
    }

    public Page pagedQuery(String userRepoRef, Page page,
            Map<String, Object> parameters) {
        return userConnector.pagedQuery(userRepoRef, page, parameters);
    }

    public UserDTO findByNickName(String nickName, String userRepoRef) {
        UserDTO userDto = userCache.findByNickName(nickName);

        if (userDto == null) {
            synchronized (userCache) {
                userDto = userCache.findByNickName(nickName);

                if (userDto == null) {
                    userDto = userConnector.findByNickName(nickName,
                            userRepoRef);

                    if (userDto != null) {
                        userCache.updateUser(userDto);
                    }
                }
            }
        }

        return userDto;
    }
    
    /**
     * 
     * 根据ids查询姓名（多个用逗号隔开）
     * add by lilei at 2018-07-26
     * **/
    public String findNamesByIds(String Ids){
    	StringBuffer sbReturn=new StringBuffer();
    	if(Ids==null)
    		return sbReturn.toString();
    	if(Ids.equals(""))
    		return sbReturn.toString();
    	
    	for (String id : Ids.split(",")) {
    		UserDTO userDTO=findById(id);
    		if(userDTO!=null)
    			sbReturn.append(userDTO.getDisplayName()+",");
		}
    	if(sbReturn.length()>0)
    		return sbReturn.toString().substring(0, sbReturn.length()-1);
		else 
			return sbReturn.toString();
    }

    public void setUserConnector(UserConnector userConnector) {
        this.userConnector = userConnector;
    }

    public void setUserCache(UserCache userCache) {
        this.userCache = userCache;
    }
}
