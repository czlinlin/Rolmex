package com.mossle.version.persistence.manager;

import com.mossle.core.hibernate.HibernateEntityDao;
import com.mossle.version.persistence.domain.VersionInfo;
import org.springframework.stereotype.Service;

/**
 * Created by wanghan on 2017\9\29 0029.
 */
@Service
public class VersionInfoManager extends HibernateEntityDao<VersionInfo>{
}
