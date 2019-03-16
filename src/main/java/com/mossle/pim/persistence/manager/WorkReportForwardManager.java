package com.mossle.pim.persistence.manager;

import com.mossle.core.hibernate.HibernateEntityDao;
import com.mossle.core.hibernate.HibernateUtils;
import com.mossle.core.page.Page;
import com.mossle.core.query.PropertyFilter;
import com.mossle.pim.persistence.domain.WorkReportForward;
import com.mossle.user.persistence.domain.AccountInfo;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.transform.Transformers;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by wanghan on 2017\8\16 0016.
 */
@Service
public class WorkReportForwardManager extends HibernateEntityDao<WorkReportForward> {

    public Page pagedQuery(Page page, String userId, List<PropertyFilter> propertyFilters) {
        if (propertyFilters == null) {
            propertyFilters = new ArrayList<PropertyFilter>();
        }
        PropertyFilter propertyFilter = new PropertyFilter("EQL_sendee", userId);
        propertyFilters.add(propertyFilter);

        Criteria criteria = this.getSession().createCriteria(this.getEntityClass());
        criteria.addOrder(Order.desc("forwardtime"));
        criteria.createAlias("workReportInfo", "workReportInfo");
        Criterion[] criterions = HibernateUtils.buildCriterion(propertyFilters);
        for (Criterion criterion : criterions) {
            criteria.add(criterion);
        }
        page = this.pagedQuery(criteria, page.getPageNo(), page.getPageSize());
        return page;
    }

    public List<WorkReportForward> forwardPaths(Long infoId, Long sendee) {
        List<WorkReportForward> workReportForwardList = new ArrayList<WorkReportForward>();
        while (sendee != null) {
            String hql = "from WorkReportForward where workReportInfo.id=? and sendee=?";
            Query query = this.createQuery(hql, new Object[]{infoId, sendee});
            List<WorkReportForward> workReportForwards = query.list();
            if (!CollectionUtils.isEmpty(workReportForwards)) {
                WorkReportForward workReportForward = workReportForwards.get(0);
                workReportForwardList.add(workReportForward);
                sendee = workReportForward.getForwarder();

            } else {
                sendee = null;
            }
        }
        int size = workReportForwardList.size();
        List<WorkReportForward> forwards = new ArrayList<WorkReportForward>(size);
        for (int i = 0; i < workReportForwardList.size(); i++) {
            forwards.add(workReportForwardList.get(size - i - 1));
        }
        return forwards;
    }

    public List<WorkReportForward> sendeePaths(Long infoId, Long forwarder) {
        String hql = "from WorkReportForward where workReportInfo.id=? and forwarder=?";
        Query query = this.createQuery(hql, new Object[]{infoId, forwarder});
        List<WorkReportForward> workReportForwards = query.list();
        int size = workReportForwards.size();
        List<WorkReportForward> sendees = new ArrayList<WorkReportForward>(size);
        for (int i = 0; i < workReportForwards.size(); i++) {
            sendees.add(workReportForwards.get(size - i - 1));
        }
        return sendees;
    }

}
