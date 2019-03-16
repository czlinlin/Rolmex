package com.mossle.msg.persistence.manager;

import com.mossle.core.hibernate.HibernateEntityDao;
import com.mossle.msg.persistence.domain.VMsgInfo;
import org.springframework.stereotype.Service;

@Service
public class VMsgInfoManager extends HibernateEntityDao<VMsgInfo> {
}
