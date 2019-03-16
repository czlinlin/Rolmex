package com.mossle.user.persistence.manager;

import com.mossle.core.hibernate.HibernateEntityDao;
import com.mossle.user.persistence.domain.PersonInfo;
import com.mossle.user.persistence.domain.UpdatePerson;

import org.springframework.stereotype.Service;

@Service
public class UpdatePersonManager extends HibernateEntityDao<UpdatePerson> {
}
