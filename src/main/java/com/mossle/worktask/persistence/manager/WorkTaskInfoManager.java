package com.mossle.worktask.persistence.manager;


import com.mossle.api.user.UserConnector;
import com.mossle.core.hibernate.HibernateEntityDao;
import com.mossle.core.hibernate.HibernateUtils;
import com.mossle.core.mapper.BeanMapper;
import com.mossle.core.page.Page;
import com.mossle.core.query.PropertyFilter;
import com.mossle.worktask.persistence.domain.WorkTaskInfo;
import com.mossle.worktask.persistence.domain.WorkTaskInfoInstance;
import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by wanghan on 2017\8\30 0030.
 */
@Service
public class WorkTaskInfoManager extends HibernateEntityDao<WorkTaskInfo> {
    private BeanMapper beanMapper = new BeanMapper();
    private UserConnector userConnector;

    public Page pagedQueryByCc(Page page, String userId, List<PropertyFilter> propertyFilters) {
        if (propertyFilters == null) {
            propertyFilters = new ArrayList<PropertyFilter>();
        }
        PropertyFilter propertyFilter = new PropertyFilter("EQL_workTaskCc.ccno", userId);
        propertyFilters.add(propertyFilter);
        Criteria criteria = this.getSession().createCriteria(this.getEntityClass());
        criteria.addOrder(Order.desc("publishtime"));
        criteria.createAlias("workTaskCcs", "workTaskCc");
        Criterion[] criterions = HibernateUtils.buildCriterion(propertyFilters);

        for (Criterion criterion : criterions) {
            criteria.add(criterion);
        }
        page = this.pagedQuery(criteria, page.getPageNo(), page.getPageSize());
        List<WorkTaskInfo> infos = (List<WorkTaskInfo>) page.getResult();
        for (WorkTaskInfo info : infos) {
            info.getWorkTaskCcs();
        }
        return page;
    }


    /**
     * 导出任务数据转换
     */
    public List<WorkTaskInfoInstance> exportInfo(List<WorkTaskInfo> workTaskInfoList) {
        List<WorkTaskInfoInstance> listResult = new ArrayList<WorkTaskInfoInstance>();

        for (WorkTaskInfo workTaskInfo : workTaskInfoList) {
            WorkTaskInfoInstance workTaskInfoInstance = new WorkTaskInfoInstance();
            beanMapper.copy(workTaskInfo, workTaskInfoInstance);
            String status = workTaskInfo.getStatus();
            if (!status.equals("")) {
                if (status.equals("0")) {
                    workTaskInfoInstance.setStatusName("已发布");
                } else if (status.equals("1")) {
                    workTaskInfoInstance.setStatusName("进行中");
                } else if (status.equals("2")) {
                    workTaskInfoInstance.setStatusName("已完成");
                } else if (status.equals("3")) {
                    workTaskInfoInstance.setStatusName("已关闭");
                } else if (status.equals("4")) {
                    workTaskInfoInstance.setStatusName("已评价");
                }
            }
            String efficiency = workTaskInfo.getEfficiency();
            if (efficiency != null) {
                if (efficiency.equals("0")) {
                    workTaskInfoInstance.setEfficiency("准时");
                }
                if (efficiency.equals("1")) {
                    workTaskInfoInstance.setEfficiency("提前");
                }
                if (efficiency.equals("2")) {
                    workTaskInfoInstance.setEfficiency("延期");
                }
            } else {
                workTaskInfoInstance.setEfficiency("");
            }
            workTaskInfoInstance.setLeaderName(userConnector.findById(workTaskInfo.getLeader().toString()).getDisplayName());
            workTaskInfoInstance.setPublisherName(userConnector.findById(workTaskInfo.getPublisher().toString()).getDisplayName());

            listResult.add(workTaskInfoInstance);
        }
        return listResult;
    }

    @Autowired
    public void setUserConnector(UserConnector userConnector) {
        this.userConnector = userConnector;
    }

}
