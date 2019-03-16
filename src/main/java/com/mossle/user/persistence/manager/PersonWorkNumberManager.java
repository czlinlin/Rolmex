package com.mossle.user.persistence.manager;

import com.mossle.core.hibernate.HibernateEntityDao;
import com.mossle.user.persistence.domain.PersonWorkNumber;

import org.springframework.stereotype.Service;

@Service
public class PersonWorkNumberManager extends HibernateEntityDao<PersonWorkNumber> {
}
