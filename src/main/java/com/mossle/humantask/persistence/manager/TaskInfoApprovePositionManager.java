package com.mossle.humantask.persistence.manager;

import com.mossle.core.hibernate.HibernateEntityDao;
import com.mossle.humantask.persistence.domain.TaskInfoApprovePosition;

import org.springframework.stereotype.Service;

@Service
public class TaskInfoApprovePositionManager extends
        HibernateEntityDao<TaskInfoApprovePosition> {
}
