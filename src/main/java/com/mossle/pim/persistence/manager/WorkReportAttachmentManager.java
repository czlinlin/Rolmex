package com.mossle.pim.persistence.manager;

import com.mossle.core.hibernate.HibernateEntityDao;

import com.mossle.pim.persistence.domain.WorkReportAttachment;

import org.springframework.stereotype.Service;
/**
 * Created by wanghan on 2017\8\16 0016.
 */
@Service
public class WorkReportAttachmentManager extends
        HibernateEntityDao<WorkReportAttachment> {
}
