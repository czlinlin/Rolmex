package com.mossle.operation.persistence.manager;

import com.mossle.core.hibernate.HibernateEntityDao;
import com.mossle.operation.persistence.domain.Business;

import org.springframework.stereotype.Service;

@Service
public class BusinessManager  extends HibernateEntityDao<Business>{

}
