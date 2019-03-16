package com.mossle.user.persistence.manager;

import com.mossle.core.hibernate.HibernateEntityDao;
import com.mossle.user.persistence.domain.PersonInfo;
import com.mossle.user.persistence.domain.RosterLog;

import org.springframework.stereotype.Service;

@Service
public class RosterLogManager extends HibernateEntityDao<RosterLog> {
}
