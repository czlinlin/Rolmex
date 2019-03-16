package com.mossle.operation.persistence.manager;

import com.mossle.core.hibernate.HibernateEntityDao;
import com.mossle.operation.persistence.domain.TestEntity;
import com.mossle.operation.persistence.domain.Invoice;

import org.springframework.stereotype.Service;

@Service
public class InvoiceManager extends HibernateEntityDao<Invoice> {
}
