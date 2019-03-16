package com.mossle.project.persistence.manager;

import com.mossle.api.user.UserConnector;
import com.mossle.core.hibernate.HibernateEntityDao;
import com.mossle.core.hibernate.HibernateUtils;
import com.mossle.core.mapper.BeanMapper;
import com.mossle.core.page.Page;
import com.mossle.core.query.PropertyFilter;
import com.mossle.project.persistence.domain.WorkProjectInfo;
import com.mossle.project.persistence.domain.WorkProjectInfoInstance;
import com.mossle.worktask.persistence.domain.WorkTaskInfo;
import com.mossle.worktask.persistence.domain.WorkTaskInfoInstance;
import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wanghan on 2017\9\9 0009.
 */
@Service
public class WorkProjectInfoManager extends HibernateEntityDao<WorkProjectInfo> {
    private BeanMapper beanMapper = new BeanMapper();
    private UserConnector userConnector;

    public Page pagedQueryByNotify(Page page, String userId, List<PropertyFilter> propertyFilters) {
        if (propertyFilters == null) {
            propertyFilters = new ArrayList<PropertyFilter>();
        }
        PropertyFilter propertyFilter = new PropertyFilter("EQL_workProjectNotify.userid", userId);
        propertyFilters.add(propertyFilter);

        Criteria criteria = this.getSession().createCriteria(this.getEntityClass());
        criteria.addOrder(Order.desc("publishtime"));
        criteria.createAlias("workProjectNotifies", "workProjectNotify");
        Criterion[] criterions = HibernateUtils.buildCriterion(propertyFilters);

        for (Criterion criterion : criterions) {
            criteria.add(criterion);
        }
        page = this.pagedQuery(criteria, page.getPageNo(), page.getPageSize());
        List<WorkProjectInfo> infos = (List<WorkProjectInfo>) page.getResult();
        for (WorkProjectInfo info : infos) {
            info.getWorkProjectNotifies();
        }
        return page;
    }

    /**
     * 项目导出数据转换
     */
    public List<WorkProjectInfoInstance> exportProjectInfo(List<WorkProjectInfo> workProjectInfos) {
        List<WorkProjectInfoInstance> listResult = new ArrayList<WorkProjectInfoInstance>();
        for (WorkProjectInfo workProjectInfo : workProjectInfos) {
            WorkProjectInfoInstance workProjectInfoInstance = new WorkProjectInfoInstance();
            beanMapper.copy(workProjectInfo, workProjectInfoInstance);
            String status = workProjectInfo.getStatus();
            if (!status.equals("")) {
                if (status.equals("0")) {
                    workProjectInfoInstance.setStatus("已发布");
                } else if (status.equals("1")) {
                    workProjectInfoInstance.setStatus("进行中");
                } else if (status.equals("2")) {
                    workProjectInfoInstance.setStatus("已完成");
                } else if (status.equals("3")) {
                    workProjectInfoInstance.setStatus("已关闭");
                } else if (status.equals("4")) {
                    workProjectInfoInstance.setStatus("已评价");
                }
            } else {
                workProjectInfoInstance.setStatus("");
            }
            String efficiency = workProjectInfo.getEfficiency();
            if (efficiency != null) {
                if (efficiency.equals("0")) {
                    workProjectInfoInstance.setEfficiency("准时");
                } else if (efficiency.equals("1")) {
                    workProjectInfoInstance.setEfficiency("提前");
                } else if (efficiency.equals("2")) {
                    workProjectInfoInstance.setEfficiency("延期");
                }
            } else {
                workProjectInfoInstance.setEfficiency("");
            }
            workProjectInfoInstance.setLeaderName(userConnector.findById(workProjectInfo.getLeader().toString()).getDisplayName());
            workProjectInfoInstance.setPublisherName(userConnector.findById(workProjectInfo.getPublisher().toString()).getDisplayName());
            listResult.add(workProjectInfoInstance);
        }
        return listResult;
    }

    /**
     * 项目导出知会数据转换
     */
    public List<WorkProjectInfoInstance> exportNotifyProjectInfo(String userId, List<PropertyFilter> propertyFilters) {
        if (propertyFilters == null) {
            propertyFilters = new ArrayList<PropertyFilter>();
        }
        List<WorkProjectInfoInstance> listResult = new ArrayList<WorkProjectInfoInstance>();
        PropertyFilter propertyFilter = new PropertyFilter("EQL_workProjectNotify.userid", userId);
        propertyFilters.add(propertyFilter);

        Criteria criteria = this.getSession().createCriteria(this.getEntityClass());
        criteria.addOrder(Order.desc("publishtime"));
        criteria.createAlias("workProjectNotifies", "workProjectNotify");
        Criterion[] criterions = HibernateUtils.buildCriterion(propertyFilters);

        for (Criterion criterion : criterions) {
            criteria.add(criterion);
        }
        List<WorkProjectInfo> workProjectInfos =criteria.list();
        listResult=this.exportProjectInfo(workProjectInfos);
        return listResult;
    }


    @Autowired
    public void setUserConnector(UserConnector userConnector) {
        this.userConnector = userConnector;
    }

}
