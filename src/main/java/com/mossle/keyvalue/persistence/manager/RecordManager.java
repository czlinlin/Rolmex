package com.mossle.keyvalue.persistence.manager;

import com.mossle.core.hibernate.HibernateEntityDao;
import com.mossle.keyvalue.persistence.domain.RecordInfo;
import org.springframework.stereotype.Service;

/**
 * Created by lilei at 2017.01.23
 */

@Service
public class RecordManager extends HibernateEntityDao<RecordInfo> {
}
