package com.mossle.operation.persistence.manager;

import com.mossle.core.hibernate.HibernateEntityDao;
import com.mossle.operation.persistence.domain.Exchange;

import org.springframework.stereotype.Service;

@Service
public class ExchangeManager  extends HibernateEntityDao<Exchange>{

}
