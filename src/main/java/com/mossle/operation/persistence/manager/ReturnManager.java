package com.mossle.operation.persistence.manager;

import com.mossle.core.hibernate.HibernateEntityDao;
import com.mossle.operation.persistence.domain.Return;

import org.springframework.stereotype.Service;

@Service
public class ReturnManager extends HibernateEntityDao<Return> {
}
