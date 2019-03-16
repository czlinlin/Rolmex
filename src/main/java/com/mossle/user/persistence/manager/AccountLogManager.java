package com.mossle.user.persistence.manager;

import com.mossle.core.hibernate.HibernateEntityDao;

import com.mossle.user.persistence.domain.AccountLog;

import org.springframework.stereotype.Service;

@Service
public class AccountLogManager extends HibernateEntityDao<AccountLog> {
}
