package com.mossle.auth.persistence.manager;

import com.mossle.auth.persistence.domain.Shift;
import com.mossle.core.hibernate.HibernateEntityDao;

import org.springframework.stereotype.Service;

@Service
public class ShiftManager extends HibernateEntityDao<Shift> {
}
