package com.mossle.user.persistence.manager;

import com.mossle.core.hibernate.HibernateEntityDao;
import com.mossle.user.persistence.domain.PersonAttendanceMachine;

import org.springframework.stereotype.Service;

@Service
public class PersonAttendanceMachineManager extends HibernateEntityDao<PersonAttendanceMachine> {
}
