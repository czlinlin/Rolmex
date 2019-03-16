package com.mossle.user.persistence.manager;

import com.mossle.core.hibernate.HibernateEntityDao;

import com.mossle.user.persistence.domain.AccountToken;

import org.springframework.stereotype.Service;

@Service
public class AccountTokenManager extends HibernateEntityDao<AccountToken> {
}