package com.mossle.workcenter.web;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import com.mossle.core.auth.CurrentUserHolder;
import com.mossle.core.page.Page;
import com.mossle.core.query.PropertyFilter;
import com.mossle.user.persistence.domain.AccountInfo;
import com.mossle.workcenter.persistence.domain.*;
import com.mossle.workcenter.persistence.manager.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 工作中心-我的
 */
@Controller
@RequestMapping("workCenter")
public class WorkCenterChargeInfoController {
	
    private static Logger logger = LoggerFactory.getLogger(WorkCenterController.class);
    
    //任务状态：0-已发布、1-进行中、2-已完成、3-已关闭；
    public static final String STATUS_TASK_LAUCHED = "0";
    public static final String STATUS_TASK_RUNNING = "1";
    public static final String STATUS_TASK_COMPELETED = "2";
    public static final String STATUS_TASK_COLSED = "3";
    
    private MissionInfoManager missionInfoManager;
    //private MissionCCInfoManager missionCCInfoManager;
    //private RoTkTaskinfo charegeTaskInfo;
    private CurrentUserHolder currentUserHolder;
    
    @RequestMapping("workcenter-chargeinfo-list")
    public String chargelist(@ModelAttribute Page page,
    		@RequestParam Map<String, Object> parameterMap, 
    		Model model) {
    	//try{}catch{}
    	String id=currentUserHolder.getUserId();
    	
    	//System.out.print("id等于这个"+id);
    	
    	List<PropertyFilter> propertyFilters = PropertyFilter.buildFromMap(parameterMap);
    	propertyFilters.add(new PropertyFilter("EQL_leader", id));
    	propertyFilters.add(new PropertyFilter("EQS_status", STATUS_TASK_LAUCHED));
    	
    	page=missionInfoManager.pagedQuery(page,propertyFilters);
    		
    	model.addAttribute("page",page);
        return "workcenter/workcenter-chargeinfo-list";
    }
    
    @Resource
    public void setCurrentUserHolder(CurrentUserHolder currentUserHolder) {
        this.currentUserHolder = currentUserHolder;
    }
    
    @Resource
    public void setMissionInfoManager(MissionInfoManager missionInfoManager) {
        this.missionInfoManager = missionInfoManager;
    }
}
