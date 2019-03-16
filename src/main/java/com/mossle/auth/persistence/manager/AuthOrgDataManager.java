package com.mossle.auth.persistence.manager;

import com.mossle.auth.persistence.domain.AuthOrgData;
import com.mossle.core.hibernate.HibernateEntityDao;
import org.springframework.stereotype.Service;

@Service
public class AuthOrgDataManager extends HibernateEntityDao<AuthOrgData> {
}