package com.mossle.project.web;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.FormParam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mossle.api.notification.NotificationConnector;
import com.mossle.api.store.StoreConnector;
import com.mossle.api.tenant.TenantHolder;
import com.mossle.api.user.UserConnector;
import com.mossle.common.utils.FileUploadAPI;
import com.mossle.common.utils.WebAPI;
import com.mossle.core.annotation.Log;
import com.mossle.core.auth.CurrentUserHolder;
import com.mossle.core.export.ExcelExport;
import com.mossle.core.mapper.BeanMapper;
import com.mossle.core.page.Page;
import com.mossle.core.query.PropertyFilter;
import com.mossle.core.query.PropertyFilterUtils;
import com.mossle.core.spring.MessageHelper;
import com.mossle.core.util.BaseDTO;
import com.mossle.internal.store.persistence.domain.StoreInfo;
import com.mossle.msg.MsgConstants;
import com.mossle.pim.rs.ScheduleResource;
import com.mossle.project.persistence.domain.WorkProjectInfo;
import com.mossle.project.persistence.domain.WorkProjectInfoInstance;
import com.mossle.project.persistence.domain.WorkProjectNotify;
import com.mossle.project.persistence.domain.WorkProjectTaskbind;
import com.mossle.project.persistence.manager.WorkProjectInfoManager;
import com.mossle.project.persistence.manager.WorkProjectNotifyManager;
import com.mossle.project.persistence.manager.WorkProjectTaskbindManager;
import com.mossle.project.utils.ProjectUtils;
import com.mossle.user.persistence.manager.AccountInfoManager;
import com.mossle.worktask.persistence.domain.WorkTaskCc;
import com.mossle.worktask.persistence.domain.WorkTaskInfo;
import com.mossle.worktask.persistence.manager.WorkTaskCcManager;
import com.mossle.worktask.persistence.manager.WorkTaskInfoManager;

/**
 * Created by wanghan on 2017\9\8 0008.
 */
@Controller
@RequestMapping("project")
public class ProjectInfoController {
    private static Logger logger = LoggerFactory.getLogger(ScheduleResource.class);
    private MessageHelper messageHelper;
    private CurrentUserHolder currentUserHolder;
    private TenantHolder tenantHolder;
    private UserConnector userConnector;

    private BeanMapper beanMapper = new BeanMapper();
    private WorkProjectInfoManager workProjectInfoManager;
    private WorkProjectNotifyManager workProjectNotifyManager;
    private WorkProjectTaskbindManager workProjectTaskbindManager;

    private WorkTaskInfoManager workTaskInfoManager;
    private NotificationConnector notificationConnector;// 发送消息
    private AccountInfoManager accountInfoManager;

    private JdbcTemplate jdbcTemplate;
    private FileUploadAPI fileUploadAPI;
    private WebAPI webAPI;
    SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");

    /*
     * 新建项目
     */
    @RequestMapping("work-project-info-input")
    @Log(desc = "项目", action = "input", operationDesc = "个人信息-工作中心-项目-新建")
    public String input(@RequestParam(value = "id", required = false) Long id,
                        @RequestParam Map<String, Object> parameterMap, Model model) {
        return "project/work-project-info-input";
    }

    /*
     * 修改项目
     */
    @RequestMapping("work-project-info-modify")
    @Log(desc = "项目", action = "modify", operationDesc = "个人信息-工作中心-项目-修改")
    public String modify(@RequestParam(value = "id", required = true) Long id,
                         @RequestParam Map<String, Object> parameterMap, Model model) {
        try {
            if (id != null) {
                List<PropertyFilter> propertyFilters = PropertyFilter.buildFromMap(parameterMap);
                String userId = currentUserHolder.getUserId();
                propertyFilters.add(new PropertyFilter("EQL_publisher", userId));
                WorkProjectInfo workProjectInfo = workProjectInfoManager.get(id);
                List<WorkProjectNotify> workProjectNotifyList = workProjectNotifyManager.findBy("workProjectInfo.id",
                        id);
                String notifynos = "";
                String notifynames = "";
                if (!CollectionUtils.isEmpty(workProjectNotifyList)) {
                    for (WorkProjectNotify workProjectNotify : workProjectNotifyList) {
                        notifynos = notifynos + workProjectNotify.getUserid() + ",";
                        notifynames = notifynames
                                + userConnector.findById(workProjectNotify.getUserid().toString()).getDisplayName()
                                + ",";
                    }
                    notifynos = notifynos.substring(0, notifynos.length() - 1);
                    notifynames = notifynames.substring(0, notifynames.length() - 1);
                }
                // 查询项目附件
                model.addAttribute("picUrl", webAPI.getViewUrl());
                List<StoreInfo> list = fileUploadAPI.getStore("OA/project", Long.toString(workProjectInfo.getId()));
                model.addAttribute("StoreInfos", list);
                model.addAttribute("model", workProjectInfo);
                model.addAttribute("notifynos", notifynos);
                model.addAttribute("notifynames", notifynames);
            }
        } catch (ArithmeticException e) {
            logger.error("个人信息-工作中心-项目-新建/修改-：" + e.getMessage() + "\r\n" + e.fillInStackTrace());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "project/work-project-info-modify";
    }

    /*
     * 保存项目
     */
    @RequestMapping("work-project-info-save")
    @Log(desc = "项目", action = "save", operationDesc = "个人信息-工作中心-项目-保存")
    public String save(@ModelAttribute WorkProjectInfo workProjectInfo,
                       @RequestParam(value = "files", required = false) MultipartFile[] files,
                       @RequestParam(value = "iptdels", required = false) String iptdels,
                       @RequestParam(value = "iptresart", required = false) String iptresart,
                       @RequestParam(value = "iptoldid", required = false) String iptoldid, String notifynos,
                       RedirectAttributes redirectAttributes) throws Exception {
        String userId = currentUserHolder.getUserId();
        String tenantId = tenantHolder.getTenantId();
        Long id = workProjectInfo.getId();
        String strTempName = "";
        String strTempUrl = "";
        String editType = "";// 1：新建发布,2:内容变更（内容，时间，附件）3：修改变更接收人 4:知会人发生变化
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        WorkProjectInfo workProjectInfoDest = workProjectInfo;
        String oldLeader = "";
        String oldContent = "";
        String oldNotifys = "";
        String oldTitle = "";

        List<WorkProjectNotify> notifiesOldList = new ArrayList<>();
        if (id != null) {
            workProjectInfoDest = workProjectInfoManager.get(id);
            notifiesOldList = workProjectNotifyManager.findBy("workProjectInfo.id", id);

            String oldStartdate = formatter.format(workProjectInfoDest.getStartdate());
            String oldPlandate = formatter.format(workProjectInfoDest.getPlandate());
            oldLeader = workProjectInfoDest.getLeader().toString();
            oldContent = workProjectInfoDest.getContent();
            oldTitle = workProjectInfoDest.getTitle();
            String newPlandate = formatter.format(workProjectInfo.getPlandate());
            String newStartdate = formatter.format(workProjectInfo.getStartdate());
            for (WorkProjectNotify workProjectNotify : notifiesOldList) {
                String oldNotify = workProjectNotify.getUserid().toString();
                oldNotifys = oldNotify + "," + oldNotifys;
            }
            if (workProjectInfoDest.getDatastatus().equals("0")) {
                editType = "1";
            } else if (!oldLeader.equals(workProjectInfo.getLeader().toString())) {
                editType = "3";
            } else if (!oldContent.equals(workProjectInfo.getContent()) || !oldPlandate.equals(newPlandate)
                    || !oldStartdate.equals(newStartdate) || !oldTitle.equals(workProjectInfo.getTitle())) {
                editType = "2";
            } else if (!notifynos.equals("") && notifynos != null) {
                String[] split_data = notifynos.split(",");
                for (int i = 0; i < split_data.length; i++) {
                    if (!oldNotifys.contains(split_data[i])) {
                        editType = "4";
                    }
                }
            }

            if (workProjectInfo.getDatastatus().equals("0")) {// 保存草稿
                beanMapper.copy(workProjectInfo, workProjectInfoDest);
                workProjectInfoDest.setPublishtime(new Date());
                workProjectInfoDest.setStatus("0");
                workProjectInfoDest.setPublisher(Long.parseLong(userId));
                strTempName = "草稿保存成功";
                strTempUrl = "redirect:/project/work-project-info-temp.do";
            } else if (workProjectInfo.getDatastatus().equals("1") && workProjectInfoDest.getDatastatus().equals("0")) {// 草稿修改直接发布
                editType = "1";// 新建发布
                workProjectInfo.setStatus("0");
                beanMapper.copy(workProjectInfo, workProjectInfoDest);
                workProjectInfoDest.setPublishtime(new Date());
                strTempName = "发布成功";
                strTempUrl = "redirect:/project/work-project-info-list.do";
            } else if (workProjectInfo.getDatastatus().equals("1") && workProjectInfoDest.getStatus().equals("0")) {// 已发修改发布
                beanMapper.copy(workProjectInfo, workProjectInfoDest);
                strTempName = "发布成功";
                strTempUrl = "redirect:/project/work-project-info-list.do";
            } else if (workProjectInfo.getDatastatus().equals("1") && workProjectInfoDest.getStatus().equals("1")) {// 进行中修改发布
                beanMapper.copy(workProjectInfo, workProjectInfoDest);
                strTempName = "发布成功";
                strTempUrl = "redirect:/project/work-project-info-list.do";
            }
        } else {
            if (workProjectInfo.getDatastatus().equals("0")) {
                beanMapper.copy(workProjectInfo, workProjectInfoDest);
                workProjectInfoDest.setPublisher(Long.parseLong(userId));
                workProjectInfoDest.setPublishtime(new Date());
                strTempName = "草稿保存成功";
                strTempUrl = "redirect:/project/work-project-info-temp.do";
            } else if (workProjectInfo.getDatastatus().equals("1")) {
                editType = "1";// 新建发布
                beanMapper.copy(workProjectInfo, workProjectInfoDest);
                workProjectInfoDest.setPublisher(Long.parseLong(userId));
                workProjectInfoDest.setPublishtime(new Date());
                workProjectInfoDest.setStatus("0");
                strTempName = "发布成功";
                strTempUrl = "redirect:/project/work-project-info-list.do";
            }
        }

        workProjectInfoManager.save(workProjectInfoDest);

        if (iptdels != null && !iptdels.equals("")) {
            fileUploadAPI.uploadFileDel(iptdels, Long.toString(workProjectInfoDest.getId()));
        }

        if (iptresart != null && iptresart.equals("restart") && iptoldid != null && !iptoldid.equals("")) {
            fileUploadAPI.uploadFileCopy(iptoldid, Long.toString(workProjectInfoDest.getId()), "OA/project", iptdels);
        }

        fileUploadAPI.uploadFile(files, tenantId, Long.toString(workProjectInfoDest.getId()), "OA/project");
        if (id != null) {
            List<WorkProjectNotify> workProjectNotifyList = workProjectNotifyManager.findBy("workProjectInfo.id", id);
            if (!CollectionUtils.isEmpty(workProjectNotifyList)) {
                workProjectNotifyManager.removeAll(workProjectNotifyList);
            }
        }
        // 保存知会人
        if (notifynos != null && !notifynos.equals("")) {
            String[] split_data = notifynos.split(",");
            for (int i = 0; i < split_data.length; i++) {
                String projectNotify = workProjectInfoDest.getLeader().toString();
                if (!(projectNotify.equals(split_data[i]))) {
                    WorkProjectNotify workProjectNotify = new WorkProjectNotify();
                    workProjectNotify.setUserid(Long.parseLong(split_data[i]));
                    workProjectNotify.setWorkProjectInfo(workProjectInfoDest);
                    workProjectNotifyManager.save(workProjectNotify);
                }
            }
        }
        workProjectInfoManager.save(workProjectInfoDest);

        // 新建发布消息 editType=1
        if (workProjectInfoDest.getDatastatus().equals("1")) {
            String title = "[" + workProjectInfoDest.getTitle() + "]" + "项目发布提醒";
            String content = "[" + currentUserHolder.getName() + "]" + "发布的" + "[" + workProjectInfoDest.getTitle()
                    + "]" + "项目，由您负责";
            String receiver = workProjectInfoDest.getLeader().toString();
            String bussinessId = workProjectInfoDest.getId().toString();

            // 新建发送消息给负责人和知会人
            if (editType.equals("1")) {
                notificationConnector.send(bussinessId, tenantId, currentUserHolder.getUserId().toString(), receiver,
                        title, content, MsgConstants.MSG_TYPE_PROJECT);

                List<WorkProjectNotify> workProjectNotifyList = workProjectNotifyManager.findBy("workProjectInfo.id",
                        workProjectInfoDest.getId());
                if (workProjectNotifyList != null && workProjectNotifyList.size() > 0) {
                    title = "[" + workProjectInfoDest.getTitle() + "]" + "项目知会提醒";
                    content = "[" + currentUserHolder.getName() + "]" + "向您知会了" + "[" + workProjectInfoDest.getTitle()
                            + "]" + "项目，请查看。";
                    for (WorkProjectNotify workProjectNotify : workProjectNotifyList) {
                        receiver = workProjectNotify.getUserid().toString();
                        notificationConnector.send(workProjectInfoDest.getId().toString(), tenantId,
                                currentUserHolder.getUserId().toString(), receiver, title, content,
                                MsgConstants.MSG_TYPE_PROJECT);
                    }
                }
            }
            // 内容发生变化 editType=2
            if (editType.equals("2") || editType.equals("")) {
                receiver = workProjectInfoDest.getLeader().toString();
                title = "[" + workProjectInfoDest.getTitle() + "]" + "项目变更通知";
                content = "[" + currentUserHolder.getName() + "]编辑了您负责的" + "[" + workProjectInfoDest.getTitle() + "]"
                        + "项目,请查看。";
                notificationConnector.send(workProjectInfoDest.getId().toString(), tenantId,
                        currentUserHolder.getUserId().toString(), receiver, title, content,
                        MsgConstants.MSG_TYPE_PROJECT);
                // 给之前的知会人发更改消息
                List<WorkProjectNotify> workProjectNotifyList = workProjectNotifyManager.findBy("workProjectInfo.id",
                        workProjectInfoDest.getId());
                if (workProjectNotifyList != null && workProjectNotifyList.size() > 0) {
                    content = "[" + currentUserHolder.getName() + "]" + "向您知会了" + "[" + workProjectInfoDest.getTitle()
                            + "]" + "项目，请查看。";
                    for (WorkProjectNotify workProjectNotify : workProjectNotifyList) {
                        receiver = workProjectNotify.getUserid().toString();
                        notificationConnector.send(workProjectInfoDest.getId().toString(), tenantId,
                                currentUserHolder.getUserId().toString(), receiver, title, content,
                                MsgConstants.MSG_TYPE_PROJECT);
                    }
                }
                if (!notifynos.equals("") && notifynos != null) {
                    String[] split_data = notifynos.split(",");
                    for (int i = 0; i < split_data.length; i++) {
                        if (!oldNotifys.contains(split_data[i])) {
                            editType = "4";
                        }
                    }
                }
            }

            // 负责人变更
            if (editType.equals("3")) {
                // 给原负责人发送消息
                title = "[" + workProjectInfoDest.getTitle() + "]" + "项目变更通知";
                content = "您负责的" + "[" + workProjectInfoDest.getTitle() + "]" + "项目，发布人" + "["
                        + currentUserHolder.getName() + "]" + "变更了负责人";
                receiver = oldLeader;
                notificationConnector.send(bussinessId, tenantId, currentUserHolder.getUserId().toString(), receiver,
                        title, content, MsgConstants.MSG_TYPE_PROJECT);

                // 给新负责人发送消息
                title = "[" + workProjectInfoDest.getTitle() + "]" + "项目发布提醒";
                content = "[" + currentUserHolder.getName() + "]发布的" + "[" + workProjectInfoDest.getTitle() + "]"
                        + "项目，由您负责。";
                receiver = workProjectInfoDest.getLeader().toString();
                notificationConnector.send(bussinessId, tenantId, currentUserHolder.getUserId().toString(), receiver,
                        title, content, MsgConstants.MSG_TYPE_PROJECT);
                if (!notifynos.equals("") && notifynos != null) {
                    String[] split_data = notifynos.split(",");
                    for (int i = 0; i < split_data.length; i++) {
                        if (!oldNotifys.contains(split_data[i])) {
                            editType = "4";
                        }
                    }
                }
            }
            // 知会人新增
            if (editType.equals("4")) {
                title = "[" + workProjectInfoDest.getTitle() + "]" + "项目知会提醒";
                content = "[" + currentUserHolder.getName() + "]" + "向您知会了" + "[" + workProjectInfoDest.getTitle() + "]"
                        + "项目，请查看。";
                String[] split_data = notifynos.split(",");
                for (int i = 0; i < split_data.length; i++) {
                    // WorkProjectNotify workProjectNotifys =
                    // workProjectNotifyManager.findUniqueBy("userid",
                    // Long.parseLong(split_data[i]));
                    if (!oldNotifys.contains(split_data[i])) {
                        receiver = split_data[i];
                        String proLeader = workProjectInfoDest.getLeader().toString();
                        if (!receiver.equals(proLeader)) {
                            notificationConnector.send(workProjectInfoDest.getId().toString(), tenantId,
                                    currentUserHolder.getUserId().toString(), receiver, title, content,
                                    MsgConstants.MSG_TYPE_PROJECT);
                        }
                    }
                }
            }
        }
        messageHelper.addFlashMessage(redirectAttributes, "core.success.publish", strTempName);
        return strTempUrl;
    }

    /*
     * 负责项目
     */
    @RequestMapping("work-project-info-chargelist-old")
    @Log(desc = "负责项目", action = "查看", operationDesc = "个人信息-工作中心-项目-负责项目-查看")
    public String chargelist(@ModelAttribute Page page, @RequestParam Map<String, Object> parameterMap, Model model) {
        try {
            List<PropertyFilter> propertyFilters = PropertyFilter.buildFromMap(parameterMap);
            String userId = currentUserHolder.getUserId();
            propertyFilters.add(new PropertyFilter("EQL_leader", userId));
            propertyFilters.add(new PropertyFilter("EQS_datastatus", "1"));

            page.setDefaultOrder("publishtime", page.ASC);
            page = workProjectInfoManager.pagedQuery(page, propertyFilters);

            model.addAttribute("page", page);
        } catch (ArithmeticException e) {
            logger.error("个人信息-工作中心-项目-草稿项目-查看-：" + e.getMessage() + "\r\n" + e.fillInStackTrace());
        }
        return "project/work-project-info-chargelist";
    }

    @RequestMapping("work-project-info-chargelist")
    @Log(desc = "负责项目", action = "查看", operationDesc = "个人信息-工作中心-项目-负责项目-查看")
    public String MyChargeList(@ModelAttribute Page page, @RequestParam Map<String, Object> parameterMap, Model model) {

    	String userId = currentUserHolder.getUserId();
        // String sqlPagedQuerySelect = "SELECT * from V_Project_progress where 1=1";
    	
    	String sqlPagedQuerySelect = ProjectUtils.getProjectInfoSql(userId) + " where 1=1";
    	
        String sqlPagedQueryCount = "select COUNT(*) from work_project_info where 1=1 ";

        List<PropertyFilter> propertyFilters = PropertyFilter.buildFromMap(parameterMap);
        
        propertyFilters.add(new PropertyFilter("EQL_leader", userId));
        propertyFilters.add(new PropertyFilter("EQS_datastatus", "1"));

        StringBuilder buff = new StringBuilder();
        List<Object> paramList = new ArrayList<Object>();
        boolean checkWhere = sqlPagedQuerySelect.toLowerCase().indexOf("where") == -1;
        PropertyFilterUtils.buildConfigurations(propertyFilters, buff, paramList, checkWhere);

        String sql = buff.toString();
        String countSql = "";
        String selectSql = "";

        countSql = sqlPagedQueryCount + " " + sql;
        selectSql = sqlPagedQuerySelect + " " + sql + " ORDER BY   publishtime DESC,diffpercent asc " + " limit "
                + page.getStart() + "," + page.getPageSize();

        logger.debug("countSql : {}", countSql);
        logger.debug("selectSql : {}", selectSql);

        Object[] params = paramList.toArray();
        int totalCount = jdbcTemplate.queryForObject(countSql, Integer.class, params);
        List<Map<String, Object>> list = jdbcTemplate.queryForList(selectSql, params);
        // List<WorkProjectInfo> personDtos = new ArrayList<WorkProjectInfo>();

        // for (Map<String, Object> map : list) {
        // personDtos.add(convertPersonDTO(map));
        // }

        page.setTotalCount(totalCount);
        page.setResult(list);
        model.addAttribute("page", page);
        return "project/work-project-info-chargelist";
    }

    /*
     * 草稿项目
     */
    @RequestMapping("work-project-info-temp")
    @Log(desc = "草稿项目", action = "查看", operationDesc = "个人信息-工作中心-项目-草稿项目-查看")
    public String temp(@ModelAttribute Page page, @RequestParam Map<String, Object> parameterMap, Model model) {
        try {
            List<PropertyFilter> propertyFilters = PropertyFilter.buildFromMap(parameterMap);
            String userId = currentUserHolder.getUserId();
            propertyFilters.add(new PropertyFilter("EQL_publisher", userId));
            propertyFilters.add(new PropertyFilter("EQS_datastatus", "0"));// 0：草稿状态
            page.setDefaultOrder("publishtime", page.DESC);
            page = workProjectInfoManager.pagedQuery(page, propertyFilters);

            model.addAttribute("page", page);
        } catch (ArithmeticException e) {
            logger.error("个人信息-工作中心-项目-草稿项目-查看-：" + e.getMessage() + "\r\n" + e.fillInStackTrace());
        }
        return "project/work-project-info-temp";
    }

    /*
     * 已发项目
     */
    @RequestMapping("work-project-info-list-old")
    @Log(desc = "已发项目", action = "查看", operationDesc = "个人信息-工作中心-项目-已发项目-查看")
    public String sentListOld(@ModelAttribute Page page, @RequestParam Map<String, Object> parameterMap, Model model) {
        try {
            List<PropertyFilter> propertyFilters = PropertyFilter.buildFromMap(parameterMap);
            String userId = currentUserHolder.getUserId();
            propertyFilters.add(new PropertyFilter("EQL_publisher", userId));
            propertyFilters.add(new PropertyFilter("EQS_datastatus", "1"));// 1：发布状态
            page.setDefaultOrder("publishtime", page.DESC);
            page = workProjectInfoManager.pagedQuery(page, propertyFilters);
            model.addAttribute("page", page);
        } catch (ArithmeticException e) {
            logger.error("个人信息-工作中心-项目-已发项目-查看-：" + e.getMessage() + "\r\n" + e.fillInStackTrace());
        }
        return "project/work-project-info-list";
    }

    /*
     * 已发项目
     */
    @RequestMapping("work-project-info-list")
    @Log(desc = "已发项目", action = "查看", operationDesc = "个人信息-工作中心-项目-已发项目-查看")
    public String sentList(@ModelAttribute Page page, @RequestParam Map<String, Object> parameterMap, Model model) {
        try {
            String sqlPagedQuerySelect = "SELECT * from V_Project_progress where 1=1";

            String sqlPagedQueryCount = "select COUNT(*) from work_project_info where 1=1 ";

            List<PropertyFilter> propertyFilters = PropertyFilter.buildFromMap(parameterMap);
            String userId = currentUserHolder.getUserId();
            propertyFilters.add(new PropertyFilter("EQL_publisher", userId));
            propertyFilters.add(new PropertyFilter("EQS_datastatus", "1"));// 1：发布状态;

            StringBuilder buff = new StringBuilder();
            List<Object> paramList = new ArrayList<Object>();
            boolean checkWhere = sqlPagedQuerySelect.toLowerCase().indexOf("where") == -1;
            PropertyFilterUtils.buildConfigurations(propertyFilters, buff, paramList, checkWhere);

            String sql = buff.toString();
            String countSql = "";
            String selectSql = "";

            countSql = sqlPagedQueryCount + " " + sql;
            selectSql = sqlPagedQuerySelect + " " + sql + " ORDER BY plandate desc, diffpercent asc " + " limit "
                    + page.getStart() + "," + page.getPageSize();

            logger.debug("countSql : {}", countSql);
            logger.debug("selectSql : {}", selectSql);

            Object[] params = paramList.toArray();
            int totalCount = jdbcTemplate.queryForObject(countSql, Integer.class, params);
            List<Map<String, Object>> list = jdbcTemplate.queryForList(selectSql, params);
            // List<WorkProjectInfo> personDtos = new
            // ArrayList<WorkProjectInfo>();

            // for (Map<String, Object> map : list) {
            // personDtos.add(convertPersonDTO(map));
            // }

            page.setTotalCount(totalCount);
            page.setResult(list);
            model.addAttribute("page", page);
        } catch (ArithmeticException e) {
            logger.error("个人信息-工作中心-项目-已发项目-查看-：" + e.getMessage() + "\r\n" + e.fillInStackTrace());
        }
        return "project/work-project-info-list";
    }

    /*
     * 知会项目
     */
    @RequestMapping("work-project-info-notify")
    @Log(desc = "知会项目", action = "查看", operationDesc = "个人信息-工作中心-项目-知会项目-查看")
    public String notifyList(@ModelAttribute Page page, @RequestParam Map<String, Object> parameterMap, Model model) {
        try {
            List<PropertyFilter> propertyFilters = PropertyFilter.buildFromMap(parameterMap);
            String userId = currentUserHolder.getUserId();
            propertyFilters.add(new PropertyFilter("EQS_datastatus", "1"));// 1：发布状态
            page.setDefaultOrder("plandate", page.DESC);
            page = workProjectInfoManager.pagedQueryByNotify(page, userId, propertyFilters);
            List<WorkProjectInfo> workProjectInfos = (List<WorkProjectInfo>) page.getResult();
            for (WorkProjectInfo info : workProjectInfos) {
                Set<WorkProjectNotify> workProjectNotifies = info.getWorkProjectNotifies();
                for (WorkProjectNotify workProjectNotify : workProjectNotifies) {
                    if (workProjectNotify.getUserid() == Long.parseLong(userId)) {
                        info.getWorkProjectNotifies();
                    }
                }
            }
            model.addAttribute("page", page);
        } catch (ArithmeticException e) {
            logger.error("个人信息-工作中心-项目-知会项目-查看-：" + e.getMessage() + "\r\n" + e.fillInStackTrace());
        }
        return "project/work-project-info-notify";
    }

    /*
     * 查询项目详情
     */
    @RequestMapping("work-project-info-detail")
    @Log(desc = "项目详情", action = "查看", operationDesc = "个人信息-工作中心-项目-项目详情")
    public String detail(@RequestParam(value = "id", required = false) Long id, RedirectAttributes redirectAttributes,
                         Model model) throws Exception {

        String redirectUrl = "redirect:/project/work-project-info-detail.do";
        try {
            if (id == null || id < 1) {
                logger.debug("查询项目明细-获取参数id错误");
                messageHelper.addFlashMessage(redirectAttributes, "core.success.save", "获取参数错误");
                return redirectUrl;
            }

            WorkProjectInfo projectModel = workProjectInfoManager.findUniqueBy("id", id);
            if (projectModel == null) {
                logger.debug("查询项目明细-没有查询到项目信息");
                messageHelper.addFlashMessage(redirectAttributes, "core.success.save", "没有查询到项目信息");
                return redirectUrl;
            }
            model.addAttribute("model", projectModel);
            // 查询项目附件
            model.addAttribute("picUrl", webAPI.getViewUrl());
            List<StoreInfo> list = fileUploadAPI.getStore("OA/project", Long.toString(projectModel.getId()));
            model.addAttribute("StoreInfos", list);

            List<StoreInfo> submitlist = fileUploadAPI.getStoreByType("OA/project", Long.toString(projectModel.getId()),
                    "1");
            model.addAttribute("StoreSubmitInfos", submitlist);

            // 项目知会人显示详情
            List<WorkProjectNotify> workProjectNotifyList = workProjectNotifyManager.findBy("workProjectInfo.id", id);
            String notifynames = "";
            if (!CollectionUtils.isEmpty(workProjectNotifyList)) {
                for (WorkProjectNotify workProjectNotify : workProjectNotifyList) {
                    notifynames += userConnector.findById(workProjectNotify.getUserid().toString()).getDisplayName()
                            + ",";
                }
            }
            if (!projectModel.getDatastatus().equals("0")) {
                Date nowDate = new Date();
                // Long totalDays = (projectModel.getPlandate().getTime() -
                // projectModel.getStartdate().getTime()) / (1000 * 3600 * 24) +
                // 1;
                // Long daysPassed = (nowDate.getTime() -
                // projectModel.getStartdate().getTime()) / (1000 * 3600 * 24) +
                // 1;

                int totalDays = daysBetween(projectModel.getStartdate(), projectModel.getPlandate()) + 1;
                int daysPassed = daysBetween(projectModel.getStartdate(), nowDate) + 1;

                int targetPercentLong = 0;

                if (projectModel.getStatus().equals("3"))
                    targetPercentLong = 100;
                else if (daysBetween(projectModel.getStartdate(), nowDate) < 0)
                    targetPercentLong = 0;
                else if (daysBetween(projectModel.getPlandate(), nowDate) > 0)
                    targetPercentLong = 100;
                else {
                    double doubleTargetResult = (double) daysPassed / (double) totalDays;
                    String targetPercentString = String.format("%.2f", doubleTargetResult);// new
                    // java.text.DecimalFormat("#0.00").format(doubleTargetResult);
                    targetPercentLong = (int) (Float.parseFloat(targetPercentString) * 100);// 目标进度
                }

                if (targetPercentLong > 100)
                    targetPercentLong = 100;
                else if (targetPercentLong < 0)
                    targetPercentLong = 0;

                int totalWorkLoad = 0;
                int haveWorkLoad = 0;

                // 一级任务详情
                List<WorkProjectTaskbind> workProjectTaskbinds = workProjectTaskbindManager.findBy("workProjectInfo.id",
                        id);
                // 任务集合
                List<WorkTaskInfo> workTaskInfos = new ArrayList<WorkTaskInfo>(workProjectTaskbinds.size());

                // 遍历中间表，获取任务信息
                for (WorkProjectTaskbind workProjectTaskbind : workProjectTaskbinds) {
                    WorkTaskInfo workTaskInfo = workProjectTaskbind.getWorkTaskInfo();

                    if (workTaskInfo.getDatastatus().equals("1") && !workTaskInfo.getStatus().equals("3")) {
                        totalWorkLoad += workTaskInfo.getWorkload();

                        if (workTaskInfo.getDatastatus().equals("1")
                                && (workTaskInfo.getStatus().equals("2") || workTaskInfo.getStatus().equals("4")))
                            haveWorkLoad += workTaskInfo.getWorkload();
                    }

                    // 查询任务附件

                    if (workTaskInfo.getDatastatus().equals("1")) {
                        List<StoreInfo> storeInfoList = fileUploadAPI.getStore("OA/worktask",
                                Long.toString(workTaskInfo.getId()));
                        workTaskInfo.setStoreInfos(storeInfoList);
                        workTaskInfos.add(workTaskInfo);
                    }
                    // 查询任务提交附件
                    if (workTaskInfo.getDatastatus().equals("1") && workTaskInfo.getStatus().equals("2")) {
                        List<StoreInfo> storeSubmitInfoList = fileUploadAPI.getStoreByType("OA/worktask",
                                Long.toString(workTaskInfo.getId()), "1");
                        workTaskInfo.setStoreSubmitInfos(storeSubmitInfoList);
                    }
                    // 一级任务抄送人显示
                    String ccnames = "";
                    Set<WorkTaskCc> workTaskCcSet = workTaskInfo.getWorkTaskCcs();
                    if (!workTaskCcSet.isEmpty()) {
                        for (WorkTaskCc workTaskCc : workTaskCcSet) {
                            ccnames += userConnector.findById(workTaskCc.getCcno().toString()).getDisplayName() + ",";
                        }
                        workTaskInfo.setCcshow(ccnames);
                    }
                }
                int currentPercentLong = 0;
                if (projectModel.getStatus().equals("2") || projectModel.getStatus().equals("3")
                        || projectModel.getStatus().equals("4"))
                    currentPercentLong = 100;
                else if (totalWorkLoad > 0) {
                    double doubleCurrentResult = (double) haveWorkLoad / (double) totalWorkLoad;
                    String currentPercentString = new java.text.DecimalFormat("#0.00").format(doubleCurrentResult);
                    currentPercentLong = (int) (Float.parseFloat(currentPercentString) * 100);// 目标进度
                }

                for (WorkTaskInfo workTaskInfo : workTaskInfos) {
                    // 子任务
                    Long taskId = workTaskInfo.getId();
                    // 子任务显示详情
                    List<WorkTaskInfo> childTaskInfos = workTaskInfoManager.findBy("uppercode", taskId);// 这个已经是子任务了
                    // 遍历子任务集合
                    for (WorkTaskInfo childInfo : childTaskInfos) {
                        if (childInfo.getDatastatus().equals("1")) {
                            List<StoreInfo> storeInfoList = fileUploadAPI.getStore("OA/worktask",
                                    Long.toString(childInfo.getId()));
                            childInfo.setStoreInfos(storeInfoList);
                            // 获取抄送信息集合
                            Set<WorkTaskCc> ccsets = childInfo.getWorkTaskCcs();
                            String childCcshow = "";
                            // 遍历抄送信息集合，获取抄送人
                            for (WorkTaskCc ChildCc : ccsets) {
                                childCcshow += userConnector.findById(ChildCc.getCcno().toString()).getDisplayName()
                                        + ",";
                            }
                            // 设置页面上显示的抄送人
                            childInfo.setCcshow(childCcshow);
                        }
                        List<StoreInfo> storeSubmitInfoList = fileUploadAPI.getStoreByType("OA/worktask",
                                Long.toString(childInfo.getId()), "1");
                        childInfo.setStoreSubmitInfos(storeSubmitInfoList);
                        workTaskInfo.setWorkChildTaskInfoList(childTaskInfos);
                        workTaskInfo.setTasknum(childTaskInfos.size());
                    }
                }
                int fChildNum = 0;
                fChildNum = workTaskInfos.size();
                model.addAttribute("workTaskInfos", workTaskInfos);
                model.addAttribute("fChildNum", fChildNum);

                if (currentPercentLong > 100)
                    currentPercentLong = 100;
                else if (currentPercentLong < 0)
                    currentPercentLong = 0;

                String bg = "white";
                if (projectModel.getStatus().equals("3"))
                    bg = "white";
                else if (currentPercentLong - targetPercentLong >= 0)
                    bg = "green";
                else if (currentPercentLong - targetPercentLong >= -5 && currentPercentLong - targetPercentLong < 0)
                    bg = "yellow";
                else
                    bg = "red";
                model.addAttribute("bg", bg);
                model.addAttribute("target", targetPercentLong);
                model.addAttribute("current", currentPercentLong);
            }
            if (!"".equals(notifynames))
                notifynames = notifynames.substring(0, notifynames.length() - 1);

            model.addAttribute("notifynames", notifynames);
        } catch (ArithmeticException e) {
            logger.error("查询详情-：" + e.getMessage() + "\r\n" + e.fillInStackTrace());
            messageHelper.addFlashMessage(redirectAttributes, "core.success.save", "查询异常");
            return redirectUrl;
        }
        return "project/work-project-info-detail";
    }

    private static int daysBetween(Date smdate, Date bdate) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        smdate = sdf.parse(sdf.format(smdate));
        bdate = sdf.parse(sdf.format(bdate));
        Calendar cal = Calendar.getInstance();
        cal.setTime(smdate);
        long time1 = cal.getTimeInMillis();
        cal.setTime(bdate);
        long time2 = cal.getTimeInMillis();
        long between_days = (time2 - time1) / (1000 * 3600 * 24);

        return Integer.parseInt(String.valueOf(between_days));
    }

    /* 修改知会项目 lilei 2017.09.11 */
    @RequestMapping("work-project-info-notify-edit")
    @Log(desc = "知会项目初始化", action = "search", operationDesc = "个人信息-工作中心-项目-负责项目-修改知会项目")
    public String InitNotifyEdit(@FormParam("id") Long id, RedirectAttributes redirectAttributes, Model model) {

        String redirectUrl = "redirect:/project/work-project-info-notify-edit.do";
        try {
            if (id == null || id < 1) {
                logger.debug("查询项目明细-获取参数id错误");
                messageHelper.addFlashMessage(redirectAttributes, "core.success.save", "获取参数错误");
                return redirectUrl;
            }

            WorkProjectInfo projectModel = workProjectInfoManager.findUniqueBy("id", id);
            if (projectModel == null) {
                logger.debug("查询项目明细-没有查询到项目信息");
                messageHelper.addFlashMessage(redirectAttributes, "core.success.save", "没有查询到项目信息");
                return redirectUrl;
            }
            model.addAttribute("model", projectModel);

            // 项目详情
            List<WorkProjectNotify> workProjectNotifyList = workProjectNotifyManager.findBy("workProjectInfo.id", id);
            String notifynames = "";
            if (!CollectionUtils.isEmpty(workProjectNotifyList)) {
                for (WorkProjectNotify workProjectNotify : workProjectNotifyList) {
                    notifynames += userConnector.findById(workProjectNotify.getUserid().toString()).getDisplayName()
                            + ",";
                }
            }

            if (!"".equals(notifynames))
                notifynames = notifynames.substring(0, notifynames.length() - 1);

            model.addAttribute("notifynames", notifynames);
            model.addAttribute("id", id);
        } catch (ArithmeticException e) {
            logger.error("添加知会人初始化页面-：" + e.getMessage() + "\r\n" + e.fillInStackTrace());
            messageHelper.addFlashMessage(redirectAttributes, "core.success.save", "查询异常");
            return redirectUrl;
        }
        return "project/work-project-info-notify-edit";
    }

    /* 修改知会项目-保存 lilei 2017.09.11 */
    @RequestMapping("work-project-info-notify-edit-save")
    @Log(desc = "添加知会项目保存", action = "update", operationDesc = "个人信息-工作中心-项目-负责项目-修改知会项目保存")
    public String NotifyEditSave(@FormParam("id") Long id, @FormParam("selectIds") String selectIds,
                                 RedirectAttributes redirectAttributes) {

        String redirectUrl = "redirect:/project/work-project-info-chargelist.do";
        try {
            if (id == null || id < 1) {
                logger.debug("查询项目明细-获取参数id错误");
                messageHelper.addFlashMessage(redirectAttributes, "core.success.save", "获取参数错误");
                return redirectUrl;
            }

            WorkProjectInfo projectModel = workProjectInfoManager.findUniqueBy("id", id);
            if (projectModel == null) {
                logger.debug("查询项目明细-没有查询到项目信息");
                messageHelper.addFlashMessage(redirectAttributes, "core.success.save", "没有查询到项目信息");
                return redirectUrl;
            }

            String[] selectList = selectIds.split(",");
            for (int i = 0; i < selectList.length; i++) {

                List<PropertyFilter> propertyFilters = new ArrayList<PropertyFilter>();
                propertyFilters.add(new PropertyFilter("EQL_userid", selectList[i]));
                propertyFilters.add(new PropertyFilter("EQL_workProjectInfo.id", id.toString()));

                List<WorkProjectNotify> notifyList = workProjectNotifyManager.find(propertyFilters);
                if (notifyList != null && notifyList.size() > 0)
                    continue;

                WorkProjectNotify notify = new WorkProjectNotify();
                notify.setWorkProjectInfo(projectModel);
                notify.setUserid(Long.parseLong(selectList[i]));
                notify.setStatus("0");
                workProjectNotifyManager.save(notify);

                String title = "[" + projectModel.getTitle() + "]" + "知会提醒";
                String tenantId = tenantHolder.getTenantId();

                String bussinessId = projectModel.getId().toString();
                // 提交任务，发送消息给知会人
                List<WorkProjectNotify> workProjectNotifyList = workProjectNotifyManager.findBy("workProjectInfo.id",
                        id);
                if (workProjectNotifyList != null && workProjectNotifyList.size() > 0) {
                    for (WorkProjectNotify workProjectNotify : workProjectNotifyList) {
                        String content = "负责人["
                                + accountInfoManager.findUniqueBy("id", projectModel.getLeader()).getDisplayName()
                                + "]将[" + projectModel.getTitle() + "]知会给您，您可查看项目的整体情况。";
                        String receiver = workProjectNotify.getUserid().toString();

                        notificationConnector.send(projectModel.getId().toString(), tenantId,
                                currentUserHolder.getUserId().toString(), receiver, title, content,
                                MsgConstants.MSG_TYPE_PROJECT);
                    }
                }
            }
            messageHelper.addFlashMessage(redirectAttributes, "core.success.save", "添加知会人成功");
            return redirectUrl;

        } catch (ArithmeticException e) {
            logger.error("添加知会人初始化页面-：" + e.getMessage() + "\r\n" + e.fillInStackTrace());
            messageHelper.addFlashMessage(redirectAttributes, "core.success.save", "查询异常");
            return redirectUrl;
        }
    }

    /*
     * 新建/修改项目
     */
    @RequestMapping("work-project-info-restart")
    @Log(desc = "项目重启初始化", action = "search", operationDesc = "个人信息-工作中心-项目-重启")
    public String ProjectRestart(@RequestParam(value = "id", required = false) Long id,
                                 @RequestParam Map<String, Object> parameterMap, Model model) throws Exception {
        try {
            if (id != null) {
                List<PropertyFilter> propertyFilters = PropertyFilter.buildFromMap(parameterMap);
                String userId = currentUserHolder.getUserId();
                propertyFilters.add(new PropertyFilter("EQL_publisher", userId));
                WorkProjectInfo workProjectInfo = workProjectInfoManager.get(id);
                List<WorkProjectNotify> workProjectNotifyList = workProjectNotifyManager.findBy("workProjectInfo.id",
                        id);
                model.addAttribute("picUrl", webAPI.getViewUrl());
                List<StoreInfo> list = fileUploadAPI.getStore("OA/project", Long.toString(workProjectInfo.getId()));
                String notifynos = "";
                String notifynames = "";
                if (!CollectionUtils.isEmpty(workProjectNotifyList)) {
                    for (WorkProjectNotify workProjectNotify : workProjectNotifyList) {
                        notifynos = notifynos + workProjectNotify.getUserid() + ",";
                        notifynames = notifynames
                                + userConnector.findById(workProjectNotify.getUserid().toString()).getDisplayName()
                                + ",";
                    }
                }
                model.addAttribute("StoreInfos", list);
                model.addAttribute("model", workProjectInfo);
                model.addAttribute("notifynos", notifynos);
                model.addAttribute("notifynames", notifynames);
            }
        } catch (ArithmeticException e) {
            logger.error("个人信息-工作中心-项目-重启初始化页面-：" + e.getMessage() + "\r\n" + e.fillInStackTrace());
        }
        return "project/work-project-info-restart";
    }

    /**
     * 项目提交页面初始化
     **/
    @RequestMapping("work-project-info-submit")
    @Log(desc = "项目提交页面初始化", action = "search", operationDesc = "个人信息-工作中心-项目-项目提交页面初始化")
    public String Turn(@RequestParam(value = "id", required = true) Long id, Model model,
                       RedirectAttributes redirectAttributes) {
        String strReturnRedirect = "redirect:/project/work-project-chargelist.do";

        String strUserId = currentUserHolder.getUserId();
        WorkProjectInfo workProjectInfo = workProjectInfoManager.findUniqueBy("id", id);
        if (workProjectInfo == null) {
            messageHelper.addFlashMessage(redirectAttributes, "core.success.save", "查询不到项目数据");
            return strReturnRedirect;
        }
        if (!workProjectInfo.getLeader().equals(Long.parseLong(strUserId))) {
            messageHelper.addFlashMessage(redirectAttributes, "core.success.save", "此项目不是你负责的项目");
            return strReturnRedirect;
        }
        model.addAttribute("id", id);
        model.addAttribute("model", workProjectInfo);
        return "/project/work-project-info-submit";
    }

    /**
     * 项目提交
     *
     * @throws Exception
     * @throws IOException
     **/
    @RequestMapping("work-project-info-submit-save")
    @Log(desc = "项目提交", action = "update", operationDesc = "个人信息-工作中心-项目-待办项目-提交页面-提交操作")
    public String ProjectSubmit(@RequestParam("id") Long id, @RequestParam("remarks") String remarks,
                                @RequestParam(value = "files") MultipartFile[] files, RedirectAttributes redirectAttributes)
            throws IOException, Exception {
        String strReturnRedirect = "redirect:/project/work-project-chargelist.do";
        BaseDTO result = new BaseDTO();
        try {
            if (id == null || id < 1) {
                logger.debug("项目提交操作-获取参数id错误");
                messageHelper.addFlashMessage(redirectAttributes, "core.success.save", "获取参数错误");
                return strReturnRedirect;
            }

            strReturnRedirect = "redirect:/project/work-project-info-submit.do?id=" + id;

            WorkProjectInfo workProjectInfo = workProjectInfoManager.findUniqueBy("id", id);
            List<WorkProjectTaskbind> taskBindList = workProjectTaskbindManager.findBy("workProjectInfo.id", id);
            if (taskBindList != null && taskBindList.size() > 0) {
                for (WorkProjectTaskbind taskbind : taskBindList) {
                    WorkTaskInfo taskInfo = taskbind.getWorkTaskInfo();
                    if (taskInfo.getDatastatus().equals("1")) {
                        if (taskInfo.getStatus().equals("1") || taskInfo.getStatus().equals("0")) {
                            messageHelper.addFlashMessage(redirectAttributes, "core.success.save",
                                    "该项目中尚有任务未处理，不能提交项目！");
                            return strReturnRedirect;
                        }
                    }
                }
            }
            if (workProjectInfo == null) {
                logger.debug("项目执行操作-没有查询到任务信息");
                messageHelper.addFlashMessage(redirectAttributes, "core.success.save", "查询不到项目数据");
                return strReturnRedirect;
            } else if (!workProjectInfo.getStatus().equals("1")) {
                messageHelper.addFlashMessage(redirectAttributes, "core.success.save", "该任务状态不是进行中或状态已改变");
                return strReturnRedirect;
            }

            workProjectInfo.setRemarks(remarks);

            String tenantId = tenantHolder.getTenantId();
            fileUploadAPI.uploadFile(files, tenantId, Long.toString(workProjectInfo.getId()), "OA/project", "1");

            String efficiency = "0";
            Date commitDate = new Date();
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Long times = (commitDate.getTime() - workProjectInfo.getPlandate().getTime()) / 1000;
            if (times > 3600)
                efficiency = "2";// 延时
            else if (times < 0)
                efficiency = "1";// 提前

            workProjectInfo.setStatus("2");
            workProjectInfo.setCommittime(commitDate);
            workProjectInfo.setEfficiency(efficiency);
            Integer hoursNum = (int) (Math.floor(Math.abs(times) / 3600));
            workProjectInfo.setHoursnum(hoursNum);

            workProjectInfoManager.save(workProjectInfo);
            String title = "[" + workProjectInfo.getTitle() + "]" + "项目完成提醒";
            String content = "[" + currentUserHolder.getName() + "]负责的" +
                    "[" + workProjectInfo.getTitle() + "]" +
                    "项目已提交，请查看。";

            String receiver = workProjectInfo.getPublisher().toString();
            String bussinessId = workProjectInfo.getId().toString();
            //提交项目，发送消息给负责人
            notificationConnector.send(
                    bussinessId,
                    tenantId,
                    currentUserHolder.getUserId().toString(),
                    receiver,
                    title,
                    content, MsgConstants.MSG_TYPE_PROJECT);

            //提交项目，发送消息给知会
            List<WorkProjectNotify> workProjectNotifyList = workProjectNotifyManager.findBy("workProjectInfo.id", id);
            if (workProjectNotifyList != null && workProjectNotifyList.size() > 0) {
                title = "[" + workProjectInfo.getTitle() + "]" + "项目完成提醒";
                for (WorkProjectNotify workProjectNotify : workProjectNotifyList) {

                    content = "发布人[" + accountInfoManager.findUniqueBy("id", workProjectInfo.getPublisher()).getDisplayName() +
                            "]" + "发布的" + "[" + workProjectInfo.getTitle() + "]" + "项目已提交，请查看。";
                    receiver = workProjectNotify.getUserid().toString();
                    notificationConnector.send(
                            bussinessId,
                            tenantId,
                            currentUserHolder.getUserId().toString(),
                            receiver,
                            title,
                            content, MsgConstants.MSG_TYPE_PROJECT);
                }
            }
            messageHelper.addFlashMessage(redirectAttributes, "core.success.save", "提交成功");
            return "redirect:/project/work-project-info-chargelist.do";
        } catch (ArithmeticException e) {
            logger.error("项目提交操作-：" + e.getMessage() + "\r\n" + e.fillInStackTrace());
            messageHelper.addFlashMessage(redirectAttributes, "core.success.save", "提交出错");
            return strReturnRedirect;
        }
    }


    //导出负责项目
    @RequestMapping("procharge-export")
    @Log(desc = "负责项目", action = "export", operationDesc = "个人信息-工作中心-项目-导出负责项目")
    public void proChargeExport(@ModelAttribute Page page,
                                @RequestParam Map<String, Object> parameterMap, HttpServletResponse response, HttpServletRequest request)
            throws Exception {
        String userId = currentUserHolder.getUserId();
        List<PropertyFilter> propertyFilters = PropertyFilter.buildFromMap(parameterMap);
        propertyFilters.add(new PropertyFilter("EQL_leader", userId));
        propertyFilters.add(new PropertyFilter("EQS_datastatus", "1"));
        List<WorkProjectInfo> workProjectInfolist = workProjectInfoManager.find(propertyFilters);
        List<WorkProjectInfoInstance> workProjectInfoInstanceList = workProjectInfoManager.exportProjectInfo(workProjectInfolist);

        if (workProjectInfoInstanceList.size() == 0) {
            String title = "暂无数据需要导出！";
            StringBuffer sb = new StringBuffer();
            sb.append("<script language='javascript'>alert('");
            sb.append(title);
            sb.append("');history.go(-1);</script>");
            try {
                response.setContentType("text/html; charset=utf-8");
                String strHtml = "<body style='background-color: #D0D0D0'>";
                strHtml += "</body>";
                response.getWriter().println((strHtml));
                response.getWriter().println(sb.toString());
                response.getWriter().flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            String fileName = "负责项目_" + formatter.format(new Date()) + ".xls";
            String[] headers = {"标题", "负责人", "发布人", "状态", "计划开始日期", "计划完成日期", "实际开始时间"};
            String[] fieldNames = {"title", "leaderName", "publisherName", "status", "startdate", "plandate", "exectime"};
            List<WorkProjectInfoInstance> dataset = workProjectInfoInstanceList;
            if (request.getHeader("User-Agent").toUpperCase().indexOf("MSIE") > 0) {
                fileName = URLEncoder.encode(fileName, "UTF-8");
            } else {
                fileName = new String(fileName.getBytes(), "ISO8859-1");
            }
            // 设置response参数，可以打开下载页面
            response.setContentType("application/vnd.ms-excel;charset=utf-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
            OutputStream out = response.getOutputStream();
            ExcelExport.exportExcel(headers, fieldNames, dataset, out);
            out.flush();
        }
    }


    //导出已发项目
    @RequestMapping("prosent-export")
    @Log(desc = "已发项目", action = "export", operationDesc = "个人信息-工作中心-项目-导出已发项目")
    public void proSentExport(@ModelAttribute Page page,
                                @RequestParam Map<String, Object> parameterMap, HttpServletResponse response, HttpServletRequest request)
            throws Exception {
        String userId = currentUserHolder.getUserId();
        List<PropertyFilter> propertyFilters = PropertyFilter.buildFromMap(parameterMap);
        propertyFilters.add(new PropertyFilter("EQL_publisher", userId));
        propertyFilters.add(new PropertyFilter("EQS_datastatus", "1"));// 1：发布状态
        List<WorkProjectInfo> workProjectInfolist = workProjectInfoManager.find(propertyFilters);
        List<WorkProjectInfoInstance> workProjectInfoInstanceList = workProjectInfoManager.exportProjectInfo(workProjectInfolist);

        if (workProjectInfoInstanceList.size() == 0) {
            String title = "暂无数据需要导出！";
            StringBuffer sb = new StringBuffer();
            sb.append("<script language='javascript'>alert('");
            sb.append(title);
            sb.append("');history.go(-1);</script>");
            try {
                response.setContentType("text/html; charset=utf-8");
                String strHtml = "<body style='background-color: #D0D0D0'>";
                strHtml += "</body>";
                response.getWriter().println((strHtml));
                response.getWriter().println(sb.toString());
                response.getWriter().flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            String fileName = "已发项目_" + formatter.format(new Date()) + ".xls";
            String[] headers = {"标题", "负责人", "发布人", "状态", "计划开始日期", "计划完成日期", "实际开始时间","完成或关闭时间","效率","评级"};
            String[] fieldNames = {"title", "leaderName", "publisherName", "status", "startdate", "plandate", "exectime","committime","efficiency","evalscore"};
            List<WorkProjectInfoInstance> dataset = workProjectInfoInstanceList;
            if (request.getHeader("User-Agent").toUpperCase().indexOf("MSIE") > 0) {
                fileName = URLEncoder.encode(fileName, "UTF-8");
            } else {
                fileName = new String(fileName.getBytes(), "ISO8859-1");
            }
            // 设置response参数，可以打开下载页面
            response.setContentType("application/vnd.ms-excel;charset=utf-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
            OutputStream out = response.getOutputStream();
            ExcelExport.exportExcel(headers, fieldNames, dataset, out);
            out.flush();
        }
    }


    //导出知会项目
    @RequestMapping("pronotify-export")
    @Log(desc = "知会项目", action = "export", operationDesc = "个人信息-工作中心-项目-导出知会项目")
    public void proNotifyExport(@ModelAttribute Page page,
                              @RequestParam Map<String, Object> parameterMap, HttpServletResponse response, HttpServletRequest request)
            throws Exception {
        String userId = currentUserHolder.getUserId();
        List<PropertyFilter> propertyFilters = PropertyFilter.buildFromMap(parameterMap);
       // propertyFilters.add(new PropertyFilter("EQL_publisher", userId));
        propertyFilters.add(new PropertyFilter("EQS_datastatus", "1"));// 1：发布状态
       // List<WorkProjectInfo> workProjectInfolist = workProjectInfoManager.find(propertyFilters);
        List<WorkProjectInfoInstance> workProjectInfoInstanceList = workProjectInfoManager.exportNotifyProjectInfo(userId,propertyFilters);

        if (workProjectInfoInstanceList.size() == 0) {
            String title = "暂无数据需要导出！";
            StringBuffer sb = new StringBuffer();
            sb.append("<script language='javascript'>alert('");
            sb.append(title);
            sb.append("');history.go(-1);</script>");
            try {
                response.setContentType("text/html; charset=utf-8");
                String strHtml = "<body style='background-color: #D0D0D0'>";
                strHtml += "</body>";
                response.getWriter().println((strHtml));
                response.getWriter().println(sb.toString());
                response.getWriter().flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            String fileName = "已发项目_" + formatter.format(new Date()) + ".xls";
            String[] headers = {"标题", "负责人", "发布人", "状态", "计划开始日期", "计划完成日期", "实际开始时间","完成或关闭时间","效率","评级"};
            String[] fieldNames = {"title", "leaderName", "publisherName", "status", "startdate", "plandate", "exectime","committime","efficiency","evalscore"};
            List<WorkProjectInfoInstance> dataset = workProjectInfoInstanceList;
            if (request.getHeader("User-Agent").toUpperCase().indexOf("MSIE") > 0) {
                fileName = URLEncoder.encode(fileName, "UTF-8");
            } else {
                fileName = new String(fileName.getBytes(), "ISO8859-1");
            }
            // 设置response参数，可以打开下载页面
            response.setContentType("application/vnd.ms-excel;charset=utf-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
            OutputStream out = response.getOutputStream();
            ExcelExport.exportExcel(headers, fieldNames, dataset, out);
            out.flush();
        }
    }

    // ~ ======================================================================
    @Autowired
    public void setUserConnector(UserConnector userConnector) {
        this.userConnector = userConnector;
    }

    @Resource
    public void setTenantHolder(TenantHolder tenantHolder) {
        this.tenantHolder = tenantHolder;
    }

    @Resource
    public void setMessageHelper(MessageHelper messageHelper) {
        this.messageHelper = messageHelper;
    }

    @Resource
    public void setCurrentUserHolder(CurrentUserHolder currentUserHolder) {
        this.currentUserHolder = currentUserHolder;
    }

    @Resource
    public void setWorkProjectNotifyManager(WorkProjectNotifyManager workProjectNotifyManager) {
        this.workProjectNotifyManager = workProjectNotifyManager;
    }

    @Resource
    public void setWorkProjectInfoManager(WorkProjectInfoManager workProjectInfoManager) {
        this.workProjectInfoManager = workProjectInfoManager;
    }

    @Resource
    public void setWorkProjectTaskbindManager(WorkProjectTaskbindManager workProjectTaskbindManager) {
        this.workProjectTaskbindManager = workProjectTaskbindManager;
    }

    @Resource
    public void setWorkTaskInfoManager(WorkTaskInfoManager workTaskInfoManager) {
        this.workTaskInfoManager = workTaskInfoManager;
    }

    @Resource
    public void setAccountInfoManager(AccountInfoManager accountInfoManager) {
        this.accountInfoManager = accountInfoManager;
    }

    @Resource
    public void setNotificationConnector(NotificationConnector notificationConnector) {
        this.notificationConnector = notificationConnector;
    }

    @Resource
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Resource
    public void setFileUploadAPI(FileUploadAPI fileUploadAPI) {
        this.fileUploadAPI = fileUploadAPI;
    }

    @Resource
    public void setWebAPI(WebAPI webAPI) {
        this.webAPI = webAPI;
    }

}
