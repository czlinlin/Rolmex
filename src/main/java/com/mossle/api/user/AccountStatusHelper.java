package com.mossle.api.user;

import com.graphbuilder.math.func.AtanFunction;

public interface AccountStatusHelper {
    boolean isLocked(String username, String application);

    String getAccountStatus(String username, String application);
    
    /**
     * 根据id查询账号锁定状态
     * add by lilei {@link AtanFunction} 2018-06-27
     * **/
    boolean findAccountLocked(String accountId);
}
