package com.mossle.operation.persistence.manager;

import com.mossle.core.hibernate.HibernateEntityDao;
import com.mossle.operation.persistence.domain.Product;

import org.springframework.stereotype.Service;

@Service
public class ProductManager extends HibernateEntityDao<Product> {
}
