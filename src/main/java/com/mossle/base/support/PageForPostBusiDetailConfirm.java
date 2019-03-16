package com.mossle.base.support;



import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.mossle.api.form.FormConnector;
import com.mossle.api.form.FormDTO;
import com.mossle.api.humantask.HumanTaskConnector;
import com.mossle.api.humantask.HumanTaskConstants;
import com.mossle.api.humantask.HumanTaskDTO;
import com.mossle.api.humantask.HumanTaskDefinition;
import com.mossle.api.humantask.ParticipantDTO;
import com.mossle.core.mapper.BeanMapper;
import com.mossle.core.page.Page;
import com.mossle.core.query.PropertyFilter;
import com.mossle.core.query.PropertyFilterUtils;
import com.mossle.dict.persistence.domain.DictInfo;
import com.mossle.api.dict.DictConnector;
import com.mossle.humantask.listener.HumanTaskListener;
import com.mossle.humantask.persistence.domain.TaskConfUser;
import com.mossle.humantask.persistence.domain.TaskDeadline;
import com.mossle.humantask.persistence.domain.TaskInfo;
import com.mossle.humantask.persistence.domain.TaskParticipant;
import com.mossle.humantask.persistence.domain.UnfinishProcessInstance;
import com.mossle.humantask.persistence.manager.TaskConfUserManager;
import com.mossle.humantask.persistence.manager.TaskDeadlineManager;
import com.mossle.humantask.persistence.manager.TaskInfoManager;
import com.mossle.humantask.persistence.manager.TaskParticipantManager;
import com.mossle.spi.humantask.TaskDefinitionConnector;
import com.mossle.spi.process.InternalProcessConnector;
import com.mossle.spi.process.ProcessTaskDefinition;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("PageForPost")
public class PageForPostBusiDetailConfirm {



//public class HumanTaskConnectorImpl implements HumanTaskConnector {
    private JdbcTemplate jdbcTemplate;
    private TaskInfoManager taskInfoManager;
    private TaskParticipantManager taskParticipantManager;
    private TaskConfUserManager taskConfUserManager;
    private TaskDeadlineManager taskDeadlineManager;
    private InternalProcessConnector internalProcessConnector;
    private TaskDefinitionConnector taskDefinitionConnector;
    private FormConnector formConnector;
    private BeanMapper beanMapper = new BeanMapper();
    private List<HumanTaskListener> humanTaskListeners;
    private DictConnector dictConnector;

    // ~

   


    /**
     * 待办任务.
     */
    public Page pageForPostBusiDetailConfirm( List<PropertyFilter> propertyFilters, Page page) {

        String sqlPagedQuerySelect = "select * from oa_ba_post_busidetail_forconfirm group by post_name ";

        String sqlPagedQueryCount = "select count(*) from (select * from oa_ba_post_busidetail_forconfirm group by post_name) a ";

        StringBuilder buff = new StringBuilder();
        List<Object> paramList = new ArrayList<Object>();
        boolean checkWhere = sqlPagedQuerySelect.toLowerCase().indexOf("where") == -1;
        PropertyFilterUtils.buildConfigurations(propertyFilters, buff, paramList, checkWhere);
        
        String sql = buff.toString();
        String countSql = sqlPagedQueryCount + " " + sql;
        String selectSql = sqlPagedQuerySelect + " " + sql + " order by createTime limit " + page.getStart() + ","
                + page.getPageSize();


        Object[] params = paramList.toArray();
        int totalCount = jdbcTemplate.queryForObject(countSql, Integer.class, params);
        List<Map<String, Object>> list = jdbcTemplate.queryForList(selectSql, params);
      
     

        page.setTotalCount(totalCount);
        page.setResult(list);

        return page;

    }


    // ~ ==================================================
    @Resource
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Resource
    public void setTaskInfoManager(TaskInfoManager taskInfoManager) {
        this.taskInfoManager = taskInfoManager;
    }

    @Resource
    public void setTaskParticipantManager(TaskParticipantManager taskParticipantManager) {
        this.taskParticipantManager = taskParticipantManager;
    }

    @Resource
    public void setTaskConfUserManager(TaskConfUserManager taskConfUserManager) {
        this.taskConfUserManager = taskConfUserManager;
    }

    @Resource
    public void setTaskDeadlineManager(TaskDeadlineManager taskDeadlineManager) {
        this.taskDeadlineManager = taskDeadlineManager;
    }

    @Resource
    public void setInternalProcessConnector(InternalProcessConnector internalProcessConnector) {
        this.internalProcessConnector = internalProcessConnector;
    }

    @Resource
    public void setTaskDefinitionConnector(TaskDefinitionConnector taskDefinitionConnector) {
        this.taskDefinitionConnector = taskDefinitionConnector;
    }

    @Resource
    public void setFormConnector(FormConnector formConnector) {
        this.formConnector = formConnector;
    }

    public void setHumanTaskListeners(List<HumanTaskListener> humanTaskListeners) {
        this.humanTaskListeners = humanTaskListeners;
    }

    @Resource
    public void setDictConnectorImpl(DictConnector dictConnector) {
        this.dictConnector = dictConnector;
    }

}
