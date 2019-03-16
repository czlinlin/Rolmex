package com.mossle.worktask.web;


import com.mossle.api.notification.NotificationConnector;
import com.mossle.api.store.StoreConnector;
import com.mossle.api.tenant.TenantHolder;
import com.mossle.api.user.UserConnector;
import com.mossle.base.persistence.domain.DetailPostEntity;
import com.mossle.common.utils.FileUploadAPI;
import com.mossle.common.utils.StringUtils;
import com.mossle.common.utils.WebAPI;
import com.mossle.core.annotation.Log;
import com.mossle.core.auth.CurrentUserHolder;
import com.mossle.core.export.ExcelExport;
import com.mossle.core.export.Exportor;
import com.mossle.core.export.TableModel;
import com.mossle.core.mapper.BeanMapper;
import com.mossle.core.page.Page;
import com.mossle.core.query.PropertyFilter;
import com.mossle.core.spring.MessageHelper;
import com.mossle.core.util.BaseDTO;
import com.mossle.internal.store.persistence.domain.StoreInfo;
import com.mossle.msg.MsgConstants;
import com.mossle.pim.rs.ScheduleResource;
import com.mossle.project.persistence.domain.WorkProjectInfo;
import com.mossle.project.persistence.domain.WorkProjectTaskbind;
import com.mossle.project.persistence.manager.WorkProjectInfoManager;
import com.mossle.project.persistence.manager.WorkProjectTaskbindManager;
import com.mossle.user.persistence.manager.AccountInfoManager;
import com.mossle.util.StringUtil;
import com.mossle.worktask.persistence.domain.WorkTaskCc;
import com.mossle.worktask.persistence.domain.WorkTaskInfo;
import com.mossle.worktask.persistence.domain.WorkTaskInfoInstance;
import com.mossle.worktask.persistence.manager.WorkTaskCcManager;
import com.mossle.worktask.persistence.manager.WorkTaskInfoManager;

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

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by wanghan on 2017\8\30 0030.
 */
@Controller
@RequestMapping("worktask")
public class WorkTaskInfoController {
    private static Logger logger = LoggerFactory.getLogger(ScheduleResource.class);
    private BeanMapper beanMapper = new BeanMapper();
    private MessageHelper messageHelper;
    private Exportor exportor;
    private CurrentUserHolder currentUserHolder;
    private TenantHolder tenantHolder;
    private UserConnector userConnector;
    private WorkTaskCcManager workTaskCcManager;
    private WorkTaskInfoManager workTaskInfoManager;
    private StoreConnector storeConnector;
    private WorkProjectInfoManager workProjectInfoManager;
    private WorkProjectTaskbindManager workProjectTaskbindManager;
    private NotificationConnector notificationConnector;// 发送消息
    private AccountInfoManager accountInfoManager;

    private FileUploadAPI fileUploadAPI;
    private WebAPI webAPI;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");

    @RequestMapping("work-task-info-goback")
    @Log(desc = "任务", action = "return", operationDesc = "个人信息-工作中心-任务-返回")
    public String goBack(@RequestParam Long proInfoId, @ModelAttribute Page page) {

        String LL = "";
        return "";
    }

    /*
     * 结束/关闭任务 wanghan 2017.09.04
     */
    @RequestMapping("work-task-info-end")
    @Log(desc = "任务", action = "结束/关闭", operationDesc = "个人信息-工作中心-任务-结束/关闭")
    public String end(@ModelAttribute Page page, @RequestParam Map<String, Object> parameterMap, Model model) {
        try {
            List<PropertyFilter> propertyFilters = PropertyFilter.buildFromMap(parameterMap);
            String userId = currentUserHolder.getUserId();
            propertyFilters.add(new PropertyFilter("EQL_leader", userId));
            propertyFilters.add(new PropertyFilter("EQS_datastatus", "1"));// 1：发布状态
            propertyFilters.add(new PropertyFilter("INS_status", "2,3,4"));// 2、3：已完成、已关闭、已评价状态
            page.setDefaultOrder("committime", page.DESC);
            page = workTaskInfoManager.pagedQuery(page, propertyFilters);
            model.addAttribute("page", page);
        } catch (ArithmeticException e) {
            logger.error("个人信息-工作中心-任务-结束/关闭任务-查看-：" + e.getMessage() + "\r\n" + e.fillInStackTrace());
        }
        return "worktask/work-task-info-end";
    }

    /*
     * 新建任务 wanghan 2017.08.30
     */
    @RequestMapping("work-task-info-input")
    @Log(desc = "任务", action = "新建", operationDesc = "个人信息-工作中心-任务-新建")
    public String input(@RequestParam(value = "id", required = false) Long id,
                        @RequestParam(value = "uppercode", required = false) Long uppercode,
                        @RequestParam(value = "projectcode", required = false) Long projectcode,
                        @RequestParam Map<String, Object> parameterMap, Model model) {
        try {
            // 项目下添加一级任务
            if (projectcode != null) {
                WorkProjectInfo workProjectInfo = workProjectInfoManager.get(projectcode);
                String projectcode_show = workProjectInfo.getTitle();
                model.addAttribute("projectcode_show", projectcode_show);
                model.addAttribute("projectcode", projectcode);
            }
        } catch (ArithmeticException e) {
            logger.error("个人信息-工作中心-任务-新建/修改-：" + e.getMessage() + "\r\n" + e.fillInStackTrace());
        }
        return "worktask/work-task-info-input";
    }

    /*
     * 添加项目下任务 wanghan
     */
    @RequestMapping("work-task-info-protaskinput")
    @Log(desc = "任务", action = "添加", operationDesc = "个人信息-工作中心-任务-添加项目下任务")
    public String addProTask(@RequestParam(value = "id", required = false) Long id,
                             @RequestParam(value = "uppercode", required = false) Long uppercode,
                             @RequestParam(value = "projectcode", required = false) Long projectcode,
                             @RequestParam Map<String, Object> parameterMap, Model model) {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            // 项目下添加一级任务
            if (projectcode != null) {
                WorkProjectInfo workProjectInfo = workProjectInfoManager.get(projectcode);
                String start = simpleDateFormat.format(workProjectInfo.getStartdate());
                String plan = simpleDateFormat.format(workProjectInfo.getPlandate());
                String projectcode_show = workProjectInfo.getTitle();
                model.addAttribute("start", start + " 00:00:00");
                model.addAttribute("plan", plan + " 23:00:00");
                model.addAttribute("projectcode_show", projectcode_show);
                model.addAttribute("projectcode", projectcode);
            }
        } catch (ArithmeticException e) {
            logger.error("个人信息-工作中心-任务-添加项目下任务-：" + e.getMessage() + "\r\n" + e.fillInStackTrace());
        }
        return "worktask/work-task-info-protaskinput";
    }

    /*
     * 修改任务
     */
    @RequestMapping("work-task-info-modify")
    @Log(desc = "任务", action = "修改", operationDesc = "个人信息-工作中心-任务-修改")
    public String modify(@RequestParam(value = "id", required = true) Long id,
                         @RequestParam(value = "uppercode", required = false) Long uppercode,
                         @RequestParam(value = "projectcode", required = false) Long projectcode,
                         @RequestParam Map<String, Object> parameterMap, Model model) throws Exception {
        try {
            if (id != null) {
                WorkProjectTaskbind workProjectTaskbind = workProjectTaskbindManager.findUniqueBy("workTaskInfo.id",
                        id);
                String tenantId = tenantHolder.getTenantId();
                List<PropertyFilter> propertyFilters = PropertyFilter.buildFromMap(parameterMap);
                String userId = currentUserHolder.getUserId();
                propertyFilters.add(new PropertyFilter("EQL_publisher", userId));
                WorkTaskInfo workTaskInfo = workTaskInfoManager.get(id);
                List<WorkTaskCc> workTaskCcs = workTaskCcManager.findBy("workTaskInfo.id", id);
                // 查询任务附件
                model.addAttribute("picUrl", webAPI.getViewUrl());
                List<StoreInfo> list = fileUploadAPI.getStore("OA/worktask", Long.toString(workTaskInfo.getId()));
                model.addAttribute("StoreInfos", list);
                String ccnos = "";
                String ccnames = "";
                if (!CollectionUtils.isEmpty(workTaskCcs)) {
                    for (WorkTaskCc workTaskCc : workTaskCcs) {
                        ccnos = ccnos + workTaskCc.getCcno() + ",";
                        ccnames = ccnames + userConnector.findById(workTaskCc.getCcno().toString()).getDisplayName()
                                + ",";
                    }
                    ccnos = ccnos.substring(0, ccnos.length() - 1);
                    ccnames = ccnames.substring(0, ccnames.length() - 1);
                }
                model.addAttribute("uppro", workProjectTaskbind);
                model.addAttribute("model", workTaskInfo);
                model.addAttribute("ccnos", ccnos);
                model.addAttribute("ccnames", ccnames);
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd ");
                if (workProjectTaskbind != null) {
                    WorkProjectInfo workProjectInfo = workProjectTaskbind.getWorkProjectInfo();
                    String startDate = simpleDateFormat.format(workProjectInfo.getStartdate());
                    String planDate = simpleDateFormat.format(workProjectInfo.getPlandate());
                    model.addAttribute("start", startDate + "00:00");
                    model.addAttribute("plan", planDate + "23:50");
                }
            }

            // 子任务添加
            if (uppercode != null) {
                WorkTaskInfo workTaskInfo = workTaskInfoManager.get(uppercode);
                String uppercode_show = workTaskInfo.getTitle();
                model.addAttribute("uppercode_show", uppercode_show);
                model.addAttribute("uppercode", uppercode);
            }
            // 项目下添加一级任务
            if (projectcode != null) {
                WorkProjectInfo workProjectInfo = workProjectInfoManager.get(projectcode);
                String projectcode_show = workProjectInfo.getTitle();
                model.addAttribute("projectcode_show", projectcode_show);
                model.addAttribute("projectcode", projectcode);
            }
        } catch (ArithmeticException e) {
            logger.error("个人信息-工作中心-任务-新建/修改-：" + e.getMessage() + "\r\n" + e.fillInStackTrace());
        }
        return "worktask/work-task-info-modify";
    }

    /*
     * 添加子任务 wanghan 2017.08.30
     */
    @RequestMapping("work-task-info-appendtask")
    @Log(desc = "任务", action = "添加", operationDesc = "个人信息-工作中心-任务-添加子任务")
    public String append(@RequestParam(value = "id", required = false) Long id,
                         @RequestParam(value = "uppercode", required = false) Long uppercode,
                         @RequestParam(value = "projectcode", required = false) Long projectcode,
                         @RequestParam Map<String, Object> parameterMap, Model model) {
        try {
            // 子任务添加
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:00:00");
            if (uppercode != null) {
                // 查找父任务
                WorkTaskInfo workTaskInfo = workTaskInfoManager.get(uppercode);
                String uppercode_show = workTaskInfo.getTitle();
                String startTime = simpleDateFormat.format(workTaskInfo.getStarttime());
                String planTime = simpleDateFormat.format(workTaskInfo.getPlantime());
                model.addAttribute("uppercode_show", uppercode_show);
                model.addAttribute("uppercode", uppercode);
                model.addAttribute("start", startTime);
                model.addAttribute("plan", planTime);
            }
        } catch (ArithmeticException e) {
            logger.error("个人信息-工作中心-任务-添加子任务-：" + e.getMessage() + "\r\n" + e.fillInStackTrace());
        }
        /* model.addAttribute("time","2017-09-18 10:00:00"); */
        return "worktask/work-task-info-appendtask";
    }

    /*
     * 保存任务 wanghan 2017.08.30
     */
    @RequestMapping("work-task-info-save")
    @Log(desc = "任务", action = "save", operationDesc = "个人信息-工作中心-任务-保存任务")
    public String save(@ModelAttribute WorkTaskInfo workTaskInfo,
                       @RequestParam(value = "uppercode", required = false) Long uppercode,
                       @RequestParam(value = "projectcode", required = false) Long projectcode,
                       @RequestParam(value = "files", required = false) MultipartFile[] files,
                       @RequestParam(value = "iptdels", required = false) String iptdels,
                       @RequestParam(value = "iptresart", required = false) String iptresart,
                       @RequestParam(value = "iptoldid", required = false) String iptoldid, RedirectAttributes redirectAttributes,
                       String ccnos) throws Exception {
        String tenantId = tenantHolder.getTenantId();
        String userId = currentUserHolder.getUserId();
        Long id = workTaskInfo.getId();
        String strTempName = "";
        String strTempUrl = "";
        String editType = "";// 1：新建,2:内容变更（内容，时间，工作量，附件）3：修改变更接收人 4:抄送人发生变化
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        WorkTaskInfo workTaskInfoDest = workTaskInfo;
        String oldLeader = "";
        String oldContent = "";
        String oldWorkload = "";
        String oldCcs = "";

        List<WorkTaskCc> ccOldList = new ArrayList<>();
        if (id != null) {
            workTaskInfoDest = workTaskInfoManager.get(id);
            ccOldList = workTaskCcManager.findBy("workTaskInfo.id", workTaskInfoDest.getId());

            String oldPlantime = formatter.format(workTaskInfoDest.getPlantime());
            String oldStarttime = formatter.format(workTaskInfoDest.getStarttime());
            oldLeader = workTaskInfoDest.getLeader().toString();
            oldContent = workTaskInfoDest.getContent();
            oldWorkload = workTaskInfoDest.getWorkload().toString();
            String newPlantime = formatter.format(workTaskInfo.getPlantime());
            String newStarttime = formatter.format(workTaskInfo.getStarttime());
            for (WorkTaskCc workTaskCc : ccOldList) {
                String oldCc = workTaskCc.getCcno().toString();
                oldCcs = oldCcs + "," + oldCc;
            }
            if (workTaskInfoDest.getDatastatus().equals("0")) {
                editType = "1";
            } else if (!oldLeader.equals(workTaskInfo.getLeader().toString())) {
                editType = "3";
            } else if (!oldContent.equals(workTaskInfo.getContent()) || !oldPlantime.equals(newPlantime)
                    || !oldStarttime.equals(newStarttime)
                    || !oldWorkload.equals(workTaskInfo.getWorkload().toString())) {
                editType = "2";
            } else if (ccnos != null && !ccnos.equals("")) {
                if (ccOldList.size()==0) {
                    editType = "4";
                } else {
                    String[] split_data = ccnos.split(",");
                    for (int i = 0; i < split_data.length; i++) {
                        if (!oldCcs.contains(split_data[i])) {
                            editType = "4";
                        }
                    }
                }
            }

            if (workTaskInfo.getDatastatus().equals("0")) { // 保存草稿
                beanMapper.copy(workTaskInfo, workTaskInfoDest);
                workTaskInfoDest.setPublisher(Long.parseLong(userId));
                workTaskInfoDest.setPublishtime(new Date());
                if (workTaskInfoDest.getUppercode() == null) {
                    workTaskInfoDest.setUppercode((long) 0);
                }
                strTempName = "草稿保存成功";
                strTempUrl = "redirect:/worktask/work-task-info-temp.do";
            } else if (workTaskInfo.getDatastatus().equals("1") && workTaskInfoDest.getDatastatus().equals("0")) { // 草稿直接发布
                editType = "1";// 新建发布
                beanMapper.copy(workTaskInfo, workTaskInfoDest);
                workTaskInfoDest.setStatus("0");
                workTaskInfoDest.setPublishtime(new Date());
                strTempName = "发布成功";
                strTempUrl = "redirect:/worktask/work-task-info-sent-list.do";
            } else if (workTaskInfo.getDatastatus().equals("1") && workTaskInfoDest.getStatus().equals("0")) { // 已发修改发布
                beanMapper.copy(workTaskInfo, workTaskInfoDest);
                strTempName = "发布成功";
                strTempUrl = "redirect:/worktask/work-task-info-sent-list.do";
            } else if (workTaskInfo.getDatastatus().equals("1") && workTaskInfoDest.getStatus().equals("1")) { // 进行中修改发布
                beanMapper.copy(workTaskInfo, workTaskInfoDest);
                strTempName = "发布成功";
                strTempUrl = "redirect:/worktask/work-task-info-sent-list.do";
            }
        } else {
            if (workTaskInfo.getDatastatus().equals("0")) {
                beanMapper.copy(workTaskInfo, workTaskInfoDest);
                workTaskInfoDest.setPublisher(Long.parseLong(userId));
                workTaskInfoDest.setPublishtime(new Date());
                workTaskInfoDest.setStatus("0");
                if (uppercode != null) {
                    workTaskInfoDest.setUppercode(uppercode);
                } else if (uppercode == null) {
                    workTaskInfoDest.setUppercode((long) 0);
                }
                strTempName = "保存草稿成功";
                strTempUrl = "redirect:/worktask/work-task-info-temp.do";
            } else if (workTaskInfo.getDatastatus().equals("1")) {
                editType = "1";// 新建发布
                beanMapper.copy(workTaskInfo, workTaskInfoDest);
                workTaskInfoDest.setPublisher(Long.parseLong(userId));
                workTaskInfoDest.setPublishtime(new Date());
                workTaskInfoDest.setStatus("0");
                if (uppercode != null) {
                    workTaskInfoDest.setUppercode(uppercode);
                } else if (uppercode == null) {
                    workTaskInfoDest.setUppercode((long) 0);
                }
                strTempName = "发布成功";
                strTempUrl = "redirect:/worktask/work-task-info-sent-list.do";
            }
        }
        workTaskInfoManager.save(workTaskInfoDest);

        if (iptdels != null && !iptdels.equals("")) {
            fileUploadAPI.uploadFileDel(iptdels, Long.toString(workTaskInfoDest.getId()));
        }

        if (iptresart != null && iptresart.equals("restart") && iptoldid != null && !iptoldid.equals("")) {
            fileUploadAPI.uploadFileCopy(iptoldid, Long.toString(workTaskInfoDest.getId()), "OA/worktask", iptdels);
        }

        fileUploadAPI.uploadFile(files, tenantId, Long.toString(workTaskInfoDest.getId()), "OA/worktask");
        if (uppercode == null) {
            if (id != null) {
                List<WorkTaskCc> workTaskCcList = workTaskCcManager.findBy("workTaskInfo.id", id);
                if (!CollectionUtils.isEmpty(workTaskCcList)) {
                    workTaskCcManager.removeAll(workTaskCcList);
                }
            }
        }
        if (ccnos != null && !ccnos.equals("")) {
            String[] split_data = ccnos.split(",");
            for (int i = 0; i < split_data.length; i++) {
                String taskLeader = workTaskInfoDest.getLeader().toString();
                if (!(taskLeader.equals(split_data[i]))) {
                    WorkTaskCc workTaskCc = new WorkTaskCc();
                    workTaskCc.setCcno(Long.parseLong(split_data[i]));
                    workTaskCc.setWorkTaskInfo(workTaskInfoDest);
                    workTaskCcManager.save(workTaskCc);
                }
            }
        }

        // 项目下的一级任务保存
        if (projectcode != null) {
            WorkProjectTaskbind workProjectTaskbind = new WorkProjectTaskbind();
            workProjectTaskbind.setBindtype("1");
            WorkProjectInfo workProjectInfo = workProjectInfoManager.get(projectcode);
            workProjectTaskbind.setWorkProjectInfo(workProjectInfo);
            workProjectTaskbind.setWorkTaskInfo(workTaskInfoDest);
            workProjectTaskbindManager.save(workProjectTaskbind);
        }

        // 新建消息 editType=1
        if (workTaskInfoDest.getDatastatus().equals("1")) {
            String title = "[" + workTaskInfoDest.getTitle() + "]" + "任务发布提醒";
            String content = "[" + currentUserHolder.getName() + "]" + "发布的" + "[" + workTaskInfoDest.getTitle() + "]"
                    + "任务，由您负责";
            String receiver = workTaskInfoDest.getLeader().toString();
            String bussinessId = workTaskInfoDest.getId().toString();
            // 新建发送消息给负责人和抄送人
            if (editType.equals("1")) {
                notificationConnector.send(bussinessId, tenantId, currentUserHolder.getUserId().toString(), receiver,
                        title, content, MsgConstants.MSG_TYPE_TASK);
                
               
                List<WorkTaskCc> ccListInput = workTaskCcManager.findBy("workTaskInfo.id", workTaskInfo.getId());
                if (ccListInput != null && ccListInput.size() > 0) {
                    content = "发布人 [" + currentUserHolder.getName() + "]" + "向您抄送了" + "[" + workTaskInfoDest.getTitle()
                            + "]" + "任务，请查看。";
                    for (WorkTaskCc cc : ccListInput) {
                        receiver = cc.getCcno().toString();
                        notificationConnector.send(workTaskInfoDest.getId().toString(), tenantId,
                                currentUserHolder.getUserId().toString(), receiver, title, content,
                                MsgConstants.MSG_TYPE_TASK);
                    }
                }
            }
            // editType:2.内容变更,
            if (editType.equals("2")) {
                title = "[" + workTaskInfoDest.getTitle() + "]" + "任务变更通知";
                content = "[" + currentUserHolder.getName() + "]" + "编辑了您负责的" + "[" + workTaskInfoDest.getTitle() + "]"
                        + "任务，请查看。";
                receiver = workTaskInfoDest.getLeader().toString();
                notificationConnector.send(workTaskInfoDest.getId().toString(), tenantId,
                        currentUserHolder.getUserId().toString(), receiver, title, content, MsgConstants.MSG_TYPE_TASK);
                if (ccnos != null && !ccnos.equals("")) {
                    String[] split_data = ccnos.split(",");
                    for (int i = 0; i < split_data.length; i++) {
                        if (oldCcs.contains(split_data[i])) {
                            editType = "4";
                        }
                    }
                }
            }

            // 给原负责人发送消息
            // b)内容：您负责的[任务标题]任务，发布人[发布人姓名]变更了负责人。；
            // editType:3.修改变更接收人发送
            if (editType.equals("3")) {
                title = "[" + workTaskInfoDest.getTitle() + "]" + "任务变更通知";
                content = "您负责的" + "[" + workTaskInfoDest.getTitle() + "]" + "任务，发布人" + "["
                        + currentUserHolder.getName() + "]" + "变更了负责人";
                receiver = oldLeader.toString();

                notificationConnector.send(bussinessId, tenantId, currentUserHolder.getUserId().toString(), receiver,
                        title, content, MsgConstants.MSG_TYPE_TASK);

                // 负责人变更，给新负责人发送消息
                title = "[" + workTaskInfoDest.getTitle() + "]" + "任务发布提醒";
                content = "[" + currentUserHolder.getName() + "]" + "发布的[" + workTaskInfoDest.getTitle() + "]任务，由您负责";
                receiver = workTaskInfoDest.getLeader().toString();

                notificationConnector.send(bussinessId, tenantId, currentUserHolder.getUserId().toString(), receiver,
                        title, content, MsgConstants.MSG_TYPE_TASK);
                if (ccnos != null && !ccnos.equals("")) {
                    String[] split_data = ccnos.split(",");
                    for (int i = 0; i < split_data.length; i++) {
                        if (oldCcs.contains(split_data[i])) {
                            editType = "4";
                        }
                    }
                }
            }

            // 抄送人修改
            if (editType.equals("4")) {
                title = "[" + workTaskInfoDest.getTitle() + "]" + "任务发布提醒";
                content = "[" + currentUserHolder.getName() + "]" + "向您抄送了" + "[" + workTaskInfoDest.getTitle() + "]"
                        + "任务，请查看。";
                String[] split_data = ccnos.split(",");
                for (int i = 0; i < split_data.length; i++) {
                    if (!oldCcs.contains(split_data[i])) {
                        receiver = split_data[i];
                        String taskleader = workTaskInfoDest.getLeader().toString();
                        if (!receiver.contains(taskleader)) {
                            notificationConnector.send(workTaskInfoDest.getId().toString(), tenantId,
                                    currentUserHolder.getUserId().toString(), receiver, title, content,
                                    MsgConstants.MSG_TYPE_TASK);
                        }
                    }
                }
            }
        }
        messageHelper.addFlashMessage(redirectAttributes, "core.success.publish", strTempName);
        return strTempUrl;
    }

    /*
     * 已发任务 wanghan 2017.08.31
     */
    @RequestMapping("work-task-info-sent-list")
    @Log(desc = "已发任务", action = "查看", operationDesc = "个人信息-工作中心-任务-已发任务-查看")
    public String sentList(@ModelAttribute Page page, @RequestParam Map<String, Object> parameterMap, Model model) {
        try {
            List<PropertyFilter> propertyFilters = PropertyFilter.buildFromMap(parameterMap);
            String userId = currentUserHolder.getUserId();
            propertyFilters.add(new PropertyFilter("EQL_publisher", userId));
            propertyFilters.add(new PropertyFilter("EQS_datastatus", "1"));// 1：发布状态
            page.setDefaultOrder("publishtime", page.DESC);
            page = workTaskInfoManager.pagedQuery(page, propertyFilters);
            model.addAttribute("page", page);
        } catch (ArithmeticException e) {
            logger.error("个人信息-工作中心-任务-已发任务-查看-：" + e.getMessage() + "\r\n" + e.fillInStackTrace());
        }
        return "worktask/work-task-info-sent-list";
    }

    /*
     * 草稿任务 wanghan 2017.08.31
     */
    @RequestMapping("work-task-info-temp")
    @Log(desc = "草稿任务", action = "查看", operationDesc = "个人信息-工作中心-任务-草稿任务-查看")
    public String temp(@ModelAttribute Page page, @RequestParam Map<String, Object> parameterMap, Model model) {
        try {
            List<PropertyFilter> propertyFilters = PropertyFilter.buildFromMap(parameterMap);
            String userId = currentUserHolder.getUserId();
            propertyFilters.add(new PropertyFilter("EQL_publisher", userId));
            propertyFilters.add(new PropertyFilter("EQS_datastatus", "0"));// 0：草稿状态
            page.setDefaultOrder("publishtime", page.DESC);
            page = workTaskInfoManager.pagedQuery(page, propertyFilters);

            List<WorkTaskInfo> workTaskInfos = (List<WorkTaskInfo>) page.getResult();
            for (WorkTaskInfo info : workTaskInfos) {
                List<WorkTaskInfo> workTaskInfosons = workTaskInfoManager.findBy("uppercode", info.getId());
                info.setChildshow(workTaskInfosons.size());
                if (info.getUppercode() == 0) {
                    info.setParentshow(0);
                } else {
                    info.setParentshow(1);
                }
            }

            model.addAttribute("page", page);
        } catch (ArithmeticException e) {
            logger.error("个人信息-工作中心-任务-草稿任务-查看-：" + e.getMessage() + "\r\n" + e.fillInStackTrace());
        }
        return "worktask/work-task-info-temp";
    }

    /*
     * 抄送任务 wanghan 2017.09.01
     */
    @RequestMapping("work-task-info-cclist")
    @Log(desc = "抄送任务", action = "查看", operationDesc = "个人信息-工作中心-任务-抄送任务-查看")
    public String ccList(@ModelAttribute Page page, @RequestParam Map<String, Object> parameterMap, Model model) {
        try {
            Long userId = Long.parseLong(currentUserHolder.getUserId());
            List<PropertyFilter> propertyFilters = PropertyFilter.buildFromMap(parameterMap);
            propertyFilters.add(new PropertyFilter("EQS_datastatus", "1"));// 1：发布状态

            page = workTaskInfoManager.pagedQueryByCc(page, userId.toString(), propertyFilters);
            List<WorkTaskInfo> workTaskInfos = (List<WorkTaskInfo>) page.getResult();
            for (WorkTaskInfo info : workTaskInfos) {
                Set<WorkTaskCc> ccsets = info.getWorkTaskCcs();
                for (WorkTaskCc cc : ccsets) {
                    if (cc.getCcno() == userId) {
                        info.getWorkTaskCcs();
                    }
                }
            }
            model.addAttribute("page", page);
        } catch (ArithmeticException e) {
            logger.error("个人信息-工作中心-任务-抄送任务-查看-：" + e.getMessage() + "\r\n" + e.fillInStackTrace());
        }
        return "worktask/work-task-info-cclist";
    }

    /* 待办任务 by lilei at 2017-08-16 */
    @RequestMapping("work-task-charge-list")
    @Log(desc = "待办任务", action = "search", operationDesc = "个人信息-工作中心-任务-待办任务-查询")
    public String TaskChargeList(@ModelAttribute Page page, @RequestParam Map<String, Object> parameterMap,
                                 Model model) {
        try {
            // 将parameterMap中的参数 build成PropertyFilter
            List<PropertyFilter> propertyFilters = PropertyFilter.buildFromMap(parameterMap);
            String userId = currentUserHolder.getUserId();
            propertyFilters.add(new PropertyFilter("EQL_leader", userId));
            propertyFilters.add(new PropertyFilter("EQS_datastatus", "1"));
            propertyFilters.add(new PropertyFilter("INS_status", "0,1"));
            page.setDefaultOrder("plantime", page.ASC);
            page = workTaskInfoManager.pagedQuery(page, propertyFilters);
            model.addAttribute("page", page);

        } catch (ArithmeticException e) {
            logger.error("个人信息-工作中心-任务-待办任务-查询-：" + e.getMessage() + "\r\n" + e.fillInStackTrace());
        }
        return "/worktask/work-task-charge-list";
    }

    @POST
    @RequestMapping("work-task-info-comment")
    @Log(desc = "备注", action = "update", operationDesc = "个人信息-工作中心-任务-待办任务-备注提交操作")
    public BaseDTO TaskComment(@FormParam("id") Long id, @FormParam("content") String content,
                               RedirectAttributes redirectAttributes) {
        BaseDTO result = new BaseDTO();
        try {
            if (id == null || id < 1) {
                result.setCode(500);
                logger.debug("任务备注操作-获取参数id错误");
                result.setMessage("获取参数错误");
                return result;
            }

            WorkTaskInfo taskModel = workTaskInfoManager.findUniqueBy("id", id);
            if (taskModel == null) {
                result.setCode(500);
                logger.debug("任务备注操作-没有查询到任务信息");
                result.setMessage("没有查询到任务信息");
                return result;
            }

            String oldContent = taskModel.getRemarks();
            taskModel.setRemarks(oldContent + ";" + content);

            workTaskInfoManager.save(taskModel);
            result.setCode(200);
        } catch (ArithmeticException e) {
            result.setCode(500);
            result.setMessage("备注出错");
            logger.error("任务删除操作-：" + e.getMessage() + "\r\n" + e.fillInStackTrace());
        }
        return result;
    }

    @RequestMapping("work-task-info-export")
    public void export(@ModelAttribute Page page, @RequestParam Map<String, Object> parameterMap,
                       HttpServletRequest request, HttpServletResponse response) throws Exception {
        String userId = currentUserHolder.getUserId();
        List<PropertyFilter> propertyFilters = PropertyFilter.buildFromMap(parameterMap);
        page = workTaskInfoManager.pagedQuery(page, propertyFilters);

        List<WorkTaskInfo> workTaskInfos = (List<WorkTaskInfo>) page.getResult();

        TableModel tableModel = new TableModel();
        tableModel.setName("task-info");
        tableModel.addHeaders("title", "status", "leader", "starttime", "plantime", "committime", "efficiency",
                "publisher", "publishtime");
        tableModel.setData(workTaskInfos);
        exportor.export(request, response, tableModel);
    }

    /* 查询任务明细 add by lilei at 2017-09-04 */
    @RequestMapping("work-task-info-detail")
    @Log(desc = "查询任务明细", action = "search", operationDesc = "个人信息-工作中心-任务-待办任务-查询任务明细")
    public String TaskDetail(@FormParam("id") Long id, @RequestParam Map<String, Object> parameterMap,
                             RedirectAttributes redirectAttributes, Model model) throws Exception {
        // BaseDTO result=new BaseDTO();
        String redirectUrl = "redirect:/worktask/work-task-info-detail.do";
        try {
            if (id == null || id < 1) {
                logger.debug("查询任务明细-获取参数id错误");
                messageHelper.addFlashMessage(redirectAttributes, "core.success.save", "获取参数错误");
                return redirectUrl;
            }
            WorkTaskInfo taskModel = workTaskInfoManager.findUniqueBy("id", id);
            String uppercode_show = "";
            if (taskModel.getUppercode() != null && taskModel.getUppercode() != 0) {
                WorkTaskInfo upW = workTaskInfoManager.findUniqueBy("id", taskModel.getUppercode());
                uppercode_show = upW.getTitle();
            }
            if (taskModel == null) {
                logger.debug("查询任务明细-没有查询到任务信息");
                messageHelper.addFlashMessage(redirectAttributes, "core.success.save", "没有查询到任务信息");
                return redirectUrl;
            }
            WorkProjectTaskbind workProjectTaskbind = workProjectTaskbindManager.findUniqueBy("workTaskInfo.id", id);
            if (workProjectTaskbind != null) {
                WorkProjectInfo workProjectInfo = workProjectTaskbind.getWorkProjectInfo();
                Long proInfoId = workProjectInfo.getId();
                String proInfoTitle = workProjectInfo.getTitle();
                model.addAttribute("proInfoId", proInfoId);
                model.addAttribute("proInfoTitle", proInfoTitle);
            }
            model.addAttribute("uppercode_show", uppercode_show);
            model.addAttribute("model", taskModel);

            // 查询任务附件
            model.addAttribute("picUrl", webAPI.getViewUrl());
            List<StoreInfo> list = fileUploadAPI.getStore("OA/worktask", Long.toString(taskModel.getId()));
            model.addAttribute("StoreInfos", list);
            // model.addAttribute("uploadUrl", webAPI.getDownloadUrl());

            List<StoreInfo> submitlist = fileUploadAPI.getStoreByType("OA/worktask", Long.toString(taskModel.getId()),
                    "1");
            model.addAttribute("StoreSubmitInfos", submitlist);

            List<WorkTaskCc> workTaskCcs = workTaskCcManager.findBy("workTaskInfo.id", id);
            String ccnames = "";
            if (!CollectionUtils.isEmpty(workTaskCcs)) {
                for (WorkTaskCc workTaskCc : workTaskCcs) {
                    ccnames += userConnector.findById(workTaskCc.getCcno().toString()).getDisplayName() + ",";
                }
            }

            // 子任务详情显示
            List<PropertyFilter> propertyFilters = PropertyFilter.buildFromMap(parameterMap);
            // 剔除草稿和删除状态的数据
            propertyFilters.add(new PropertyFilter("EQS_datastatus", "1"));
            propertyFilters.add(new PropertyFilter("EQL_uppercode", id.toString()));
            List<WorkTaskInfo> childInfos = workTaskInfoManager.find(propertyFilters);
            for (WorkTaskInfo childInfo : childInfos) {
                List<StoreInfo> storeInfoList = fileUploadAPI.getStore("OA/worktask", Long.toString(childInfo.getId()));
                childInfo.setStoreInfos(storeInfoList);

                List<StoreInfo> storeSubmitInfoList = fileUploadAPI.getStoreByType("OA/worktask",
                        Long.toString(childInfo.getId()), "1");
                childInfo.setStoreSubmitInfos(storeSubmitInfoList);

                String childCcshow = "";
                if (!CollectionUtils.isEmpty(childInfos)) {
                    Set<WorkTaskCc> ccsets = childInfo.getWorkTaskCcs();
                    for (WorkTaskCc ChildCc : ccsets) {
                        childInfo.getWorkTaskCcs();
                        childCcshow += userConnector.findById(ChildCc.getCcno().toString()).getDisplayName() + ",";
                    }
                }
                if (!"".equals(childCcshow))
                    childCcshow = childCcshow.substring(0, childCcshow.length() - 1);
                childInfo.setCcshow(childCcshow);
            }
            int childNum = 0;
            if (!CollectionUtils.isEmpty(childInfos))
                childNum = childInfos.size();

            model.addAttribute("childnum", childNum);

            if (!"".equals(ccnames))
                ccnames = ccnames.substring(0, ccnames.length() - 1);

            model.addAttribute("ccnames", ccnames);
            model.addAttribute("childlist", childInfos);

        } catch (ArithmeticException e) {
            logger.error("查询任务明细-：" + e.getMessage() + "\r\n" + e.fillInStackTrace());
            messageHelper.addFlashMessage(redirectAttributes, "core.success.save", "查询异常");
            return redirectUrl;
        }
        return "worktask/work-task-info-detail";
    }

    /*
     * 重启任务 by lilei at 2017.09.05
     */
    @RequestMapping("work-task-info-restart")
    @Log(desc = "任务", action = "add", operationDesc = "个人信息-工作中心-任务-重启任务")
    public String TaskReStart(@RequestParam(value = "id", required = false) Long id,
                              @RequestParam Map<String, Object> parameterMap, Model model) throws Exception {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:00:00");
            if (id != null) {
                List<PropertyFilter> propertyFilters = PropertyFilter.buildFromMap(parameterMap);
                String userId = currentUserHolder.getUserId();
                propertyFilters.add(new PropertyFilter("EQL_publisher", userId));
                WorkTaskInfo workTaskInfo = workTaskInfoManager.get(id);
                // 查询任务附件
                model.addAttribute("picUrl", webAPI.getViewUrl());
                List<StoreInfo> list = fileUploadAPI.getStore("OA/worktask", Long.toString(workTaskInfo.getId()));
                model.addAttribute("StoreInfos", list);
                if (workTaskInfo.getUppercode() != 0 && workTaskInfo.getUppercode() != null) {
                    WorkTaskInfo FInfo = workTaskInfoManager.get(workTaskInfo.getUppercode());
                    String FStatus = FInfo.getStatus();
                    String uppercode_show = FInfo.getTitle();
                    String startTime = simpleDateFormat.format(FInfo.getStarttime());
                    String planTime = simpleDateFormat.format(FInfo.getPlantime());
                    model.addAttribute("uppercode_show", uppercode_show);
                    model.addAttribute("uppercode", workTaskInfo.getUppercode());
                    model.addAttribute("start", startTime);
                    model.addAttribute("plan", planTime);
                    model.addAttribute("FStatus", FStatus);
                }
                WorkProjectTaskbind workProjectTaskbind = workProjectTaskbindManager.findUniqueBy("workTaskInfo.id",
                        id);
                if (workProjectTaskbind != null) {
                    WorkProjectInfo workProjectInfo = workProjectTaskbind.getWorkProjectInfo();
                    String proStatus = workProjectInfo.getStatus();
                    String proTitle = workProjectInfo.getTitle();
                    Long proId = workProjectInfo.getId();
                    model.addAttribute("proStatus", proStatus);
                    model.addAttribute("proTitle", proTitle);
                    model.addAttribute("proId", proId);
                }
                if (workProjectTaskbind == null) {
                    List<WorkTaskCc> workTaskCcs = workTaskCcManager.findBy("workTaskInfo.id", id);
                    String ccnos = "";
                    String ccnames = "";
                    if (!CollectionUtils.isEmpty(workTaskCcs)) {
                        for (WorkTaskCc workTaskCc : workTaskCcs) {
                            ccnos = ccnos + workTaskCc.getCcno() + ",";
                            ccnames = ccnames + userConnector.findById(workTaskCc.getCcno().toString()).getDisplayName()
                                    + ",";
                        }
                        ccnos = ccnos.substring(0, ccnos.length() - 1);
                        ccnames = ccnames.substring(0, ccnames.length() - 1);
                    }

                    model.addAttribute("ccnos", ccnos);
                    model.addAttribute("ccnames", ccnames);
                }
                model.addAttribute("model", workTaskInfo);
            }
        } catch (ArithmeticException e) {
            logger.error("个人信息-工作中心-任务-新建/修改-：" + e.getMessage() + "\r\n" + e.fillInStackTrace());
        }
        return "worktask/work-task-info-restart";
    }

    /*
     * 任务重启保存 by lilei at 2017.0.08
     */
    @RequestMapping("work-task-restart-save")
    @Log(desc = "任务重启保存", action = "add", operationDesc = "个人信息-工作中心-任务-任务重启保存")
    public String TaskReStartSave(@ModelAttribute WorkTaskInfo workTaskInfo, RedirectAttributes redirectAttributes,
                                  String ccnos, String projectcode) {
        try {
            String userId = currentUserHolder.getUserId();
            String tenantId = tenantHolder.getTenantId();
            Long id = workTaskInfo.getId();
            workTaskInfo.setPublishtime(new Date());
            workTaskInfo.setPublisher(Long.parseLong(userId));
            workTaskInfo.setStatus("0");
            workTaskInfoManager.save(workTaskInfo);
            List<WorkTaskCc> workTaskCcs = workTaskCcManager.findBy("workTaskInfo.id", id);
            workTaskCcManager.removeAll(workTaskCcs);
            if (projectcode == null) {
                if (!ccnos.isEmpty()) {
                    if (id != null) {
                        List<WorkTaskCc> workTaskCcList = workTaskCcManager.findBy("workTaskInfo.id", id);
                        workTaskCcManager.removeAll(workTaskCcList);
                    }
                    String[] split_data = ccnos.split(",");
                    for (int i = 0; i < split_data.length; i++) {
                        String taskLeader = workTaskInfo.getLeader().toString();
                        if (!(taskLeader.equals(split_data[i]))) {
                            WorkTaskCc workTaskCc = new WorkTaskCc();
                            workTaskCc.setCcno(Long.parseLong(split_data[i]));
                            workTaskCc.setWorkTaskInfo(workTaskInfo);
                            workTaskCcManager.save(workTaskCc);
                        }
                    }
                }
            }
            workTaskInfoManager.save(workTaskInfo);
            if (projectcode != null) {
                WorkProjectInfo workProjectInfo = workProjectInfoManager.findUniqueBy("id",
                        Long.parseLong(projectcode));
                WorkProjectTaskbind workProjectTaskbind = new WorkProjectTaskbind();
                workProjectTaskbind.setWorkProjectInfo(workProjectInfo);
                workProjectTaskbind.setWorkTaskInfo(workTaskInfo);
                workProjectTaskbindManager.save(workProjectTaskbind);
            }

            String title = "[" + workTaskInfo.getTitle() + "]" + "任务发布提醒";
            String content = "发布人 [" + currentUserHolder.getName() + "]" + "发布的" + "[" + workTaskInfo.getTitle() + "]"
                    + "任务，由您负责";
            String receiver = workTaskInfo.getLeader().toString();
            String bussinessId = workTaskInfo.getId().toString();
            // 发送消息给负责人和抄送人
            notificationConnector.send(bussinessId, tenantId, currentUserHolder.getUserId().toString(), receiver, title,
                    content, MsgConstants.MSG_TYPE_TASK);
            List<WorkTaskCc> ccListInput = workTaskCcManager.findBy("workTaskInfo.id", workTaskInfo.getId());
            if (ccListInput != null && ccListInput.size() > 0) {
                content = "发布人 [" + currentUserHolder.getName() + "]" + "向您抄送了" + "[" + workTaskInfo.getTitle() + "]"
                        + "任务，请查看。";
                for (WorkTaskCc cc : ccListInput) {
                    receiver = cc.getCcno().toString();
                    notificationConnector.send(workTaskInfo.getId().toString(), tenantId,
                            currentUserHolder.getUserId().toString(), receiver, title, content,
                            MsgConstants.MSG_TYPE_TASK);
                }
            }
        } catch (ArithmeticException e) {
            logger.error("个人信息-工作中心-任务-保存-：" + e.getMessage() + "\r\n" + e.fillInStackTrace());
        }

        if (workTaskInfo.getDatastatus().equals("0")) {
            messageHelper.addFlashMessage(redirectAttributes, "core.success.save", "草稿保存成功");
            return "redirect:/worktask/work-task-info-temp.do";
        } else if (workTaskInfo.getDatastatus().equals("1"))
            workTaskInfo.setStatus("0");// 0:任务发布状态
        workTaskInfoManager.save(workTaskInfo);
        messageHelper.addFlashMessage(redirectAttributes, "core.success.publish", "发布成功");
        return "redirect:/worktask/work-task-info-sent-list.do";

    }

    /**
     * 任务提交页面初始化
     **/
    @RequestMapping("work-task-info-submit")
    @Log(desc = "任务提交页面初始化", action = "search", operationDesc = "个人信息-工作中心-任务-任务提交页面初始化")
    public String Turn(@RequestParam(value = "id", required = true) Long id, Model model,
                       RedirectAttributes redirectAttributes) {
        String strReturnRedirect = "redirect:/worktask/work-worktask-charge-list.do";

        String strUserId = currentUserHolder.getUserId();
        WorkTaskInfo taskModel = workTaskInfoManager.findUniqueBy("id", id);
        if (taskModel == null) {
            messageHelper.addFlashMessage(redirectAttributes, "core.success.save", "查询不到任务数据");
            return strReturnRedirect;
        }

        if (!taskModel.getLeader().equals(Long.parseLong(strUserId))) {
            messageHelper.addFlashMessage(redirectAttributes, "core.success.save", "此项目不是你负责的项目");
            return strReturnRedirect;
        }

        // model.addAttribute("sendeename", sendeename);
        model.addAttribute("id", id);
        model.addAttribute("model", taskModel);
        return "/worktask/work-task-info-submit";
    }

    /**
     * 任务提交
     *
     * @throws Exception
     * @throws IOException
     **/
    @RequestMapping("work-task-info-submit-save")
    @Log(desc = "任务提交", action = "update", operationDesc = "个人信息-工作中心-任务-待办任务-提交页面-提交操作")
    public String TaskSubmit(@RequestParam("id") Long id, @RequestParam("remarks") String remarks,
                             @RequestParam(value = "files") MultipartFile[] files, RedirectAttributes redirectAttributes)
            throws IOException, Exception {
        String strReturnRedirect = "redirect:/worktask/work-task-charge-list.do";
        BaseDTO result = new BaseDTO();
        try {
            if (id == null || id < 1) {
                logger.debug("任务提交操作-获取参数id错误");
                messageHelper.addFlashMessage(redirectAttributes, "core.success.save", "获取参数错误");
                return strReturnRedirect;
            }

            strReturnRedirect = "redirect:/worktask/work-task-info-submit.do?id=" + id;

            WorkTaskInfo taskModel = workTaskInfoManager.findUniqueBy("id", id);
            if (taskModel == null) {
                logger.debug("任务执行操作-没有查询到任务信息");
                messageHelper.addFlashMessage(redirectAttributes, "core.success.save", "查询不到任务数据");
                return strReturnRedirect;
                // result.setCode(500);
            } else if (!taskModel.getStatus().equals("1")) {
                messageHelper.addFlashMessage(redirectAttributes, "core.success.save", "该任务状态不是进行中或状态已改变");
                return strReturnRedirect;
            }

            List<PropertyFilter> propertyFilters = new ArrayList<PropertyFilter>();
            propertyFilters.add(new PropertyFilter("EQL_uppercode", id.toString()));
            propertyFilters.add(new PropertyFilter("INS_datastatus", "1"));
            propertyFilters.add(new PropertyFilter("INS_status", "0,1"));

            List<WorkTaskInfo> childList = workTaskInfoManager.find(propertyFilters);
            if (childList!=null && childList.size() > 0) {
                messageHelper.addFlashMessage(redirectAttributes, "core.success.save", "该任务下有子任务尚未提交");
                return strReturnRedirect;
            }

            String efficiency = "0";
            Date commitDate = new Date();
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Long times = commitDate.getTime() - taskModel.getPlantime().getTime();
            if (times > 3600)
                efficiency = "2";// 延时
            else if (times < 0)
                efficiency = "1";// 提前

            taskModel.setStatus("2");
            taskModel.setCommittime(commitDate);
            taskModel.setEfficiency(efficiency);
            Integer hoursNum = (int) (Math.floor(Math.abs(times) / 3600));
            taskModel.setHoursnum(hoursNum);

            taskModel.setAnnex(remarks);

            String tenantId = tenantHolder.getTenantId();
            fileUploadAPI.uploadFile(files, tenantId, Long.toString(taskModel.getId()), "OA/worktask", "1");

            workTaskInfoManager.save(taskModel);

            String title = "[" + taskModel.getTitle() + "]" + "任务提交通知";
            String content = "[" + currentUserHolder.getName() + "]" + "负责的" + "[" + taskModel.getTitle() + "]"
                    + "任务已提交，请查看。";

            String receiver = taskModel.getPublisher().toString();
            String bussinessId = taskModel.getId().toString();
            // 提交任务，发送消息给负责人
            notificationConnector.send(bussinessId, tenantId, currentUserHolder.getUserId().toString(), receiver, title,
                    content, MsgConstants.MSG_TYPE_TASK);

            // 提交任务，发送消息给抄送人
            /*
             * 5)任务提交后，给任务的抄送人发送消息提醒，消息格式如下： a)标题：[任务标题]任务提交通知
			 * b)内容：发布人[任务发布人姓名]抄送给您的[任务标题]任务已提交，请查看。
			 */
            List<WorkTaskCc> ccList = workTaskCcManager.findBy("workTaskInfo.id", id);
            if (ccList != null && ccList.size() > 0) {
                title = "[" + taskModel.getTitle() + "]" + "任务提交通知";
                for (WorkTaskCc cc : ccList) {
                    content = "发布人[" + accountInfoManager.findUniqueBy("id", taskModel.getPublisher()).getDisplayName()
                            + "]" + "抄送给您的[" + taskModel.getTitle() + "]任务已提交，敬请查看。";
                    receiver = cc.getCcno().toString();
                    notificationConnector.send(bussinessId, tenantId, currentUserHolder.getUserId().toString(),
                            receiver, title, content, MsgConstants.MSG_TYPE_TASK);
                }
            }
            messageHelper.addFlashMessage(redirectAttributes, "core.success.save", "提交成功");
            return "redirect:/worktask/work-task-charge-list.do";
        } catch (ArithmeticException e) {
            logger.error("任务提交操作-：" + e.getMessage() + "\r\n" + e.fillInStackTrace());
            messageHelper.addFlashMessage(redirectAttributes, "core.success.save", "提交出错");
            return strReturnRedirect;
        }
    }

    //导出已发任务
    @RequestMapping("sent-export")
    @Log(desc = "任务", action = "export", operationDesc = "个人信息-工作中心-任务-导出已发任务")
    public void historyExport(@ModelAttribute Page page,
                              @RequestParam Map<String, Object> parameterMap, HttpServletResponse response, HttpServletRequest request)
            throws Exception {
        String userId = currentUserHolder.getUserId();
        List<PropertyFilter> propertyFilters = PropertyFilter.buildFromMap(parameterMap);
        propertyFilters.add(new PropertyFilter("EQL_publisher", userId));
        propertyFilters.add(new PropertyFilter("EQS_datastatus", "1"));// 1：发布状态
        // page.setDefaultOrder("publishtime", page.DESC);
        List<WorkTaskInfo> workTaskInfoList = workTaskInfoManager.find(propertyFilters);
        List<WorkTaskInfoInstance> workTaskInfoInstanceList = workTaskInfoManager.exportInfo(workTaskInfoList);
        if (workTaskInfoInstanceList.size() == 0) {
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
            String fileName = "已发任务_" + formatter.format(new Date()) + ".xls";
            String[] headers = {"标题", "负责人", "发布人", "状态", "计划开始时间", "计划完成时间", "实际开始时间", "完成或关闭时间", "效率", "评级"};
            String[] fieldNames = {"title", "leaderName", "publisherName", "statusName", "starttime", "plantime", "exectime", "committime", "efficiency", "evalscore"};
            List<WorkTaskInfoInstance> dataset = workTaskInfoInstanceList;
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

    //导出待办任务
    @RequestMapping("charge-export")
    @Log(desc = "任务", action = "export", operationDesc = "个人信息-工作中心-任务-导出待办任务")
    public void chargeExport(@ModelAttribute Page page,
                             @RequestParam Map<String, Object> parameterMap, HttpServletResponse response, HttpServletRequest request)
            throws Exception {
        String userId = currentUserHolder.getUserId();
        List<PropertyFilter> propertyFilters = PropertyFilter.buildFromMap(parameterMap);
        propertyFilters.add(new PropertyFilter("EQL_leader", userId));
        propertyFilters.add(new PropertyFilter("EQS_datastatus", "1"));
        propertyFilters.add(new PropertyFilter("INS_status", "0,1"));
        List<WorkTaskInfo> workTaskInfoList = workTaskInfoManager.find(propertyFilters);
        List<WorkTaskInfoInstance> workTaskInfoInstanceList = workTaskInfoManager.exportInfo(workTaskInfoList);
        if (workTaskInfoInstanceList.size() == 0) {
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
            String fileName = "待办任务_" + formatter.format(new Date()) + ".xls";
            String[] headers = {"标题", "负责人", "发布人", "状态", "计划开始时间", "计划完成时间", "实际开始时间"};
            String[] fieldNames = {"title", "leaderName", "publisherName", "statusName", "starttime", "plantime", "exectime"};
            List<WorkTaskInfoInstance> dataset = workTaskInfoInstanceList;
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

    //导出结束任务
    @RequestMapping("end-export")
    @Log(desc = "任务", action = "export", operationDesc = "个人信息-工作中心-任务-导出结束任务")
    public void endExport(@ModelAttribute Page page,
                          @RequestParam Map<String, Object> parameterMap, HttpServletResponse response, HttpServletRequest request)
            throws Exception {
        String userId = currentUserHolder.getUserId();
        List<PropertyFilter> propertyFilters = PropertyFilter.buildFromMap(parameterMap);
        propertyFilters.add(new PropertyFilter("EQL_leader", userId));
        propertyFilters.add(new PropertyFilter("EQS_datastatus", "1"));// 1：发布状态
        propertyFilters.add(new PropertyFilter("INS_status", "2,3,4"));// 2、3：已完成、已关闭、已评价状态

        List<WorkTaskInfo> workTaskInfoList = workTaskInfoManager.find(propertyFilters);
        List<WorkTaskInfoInstance> workTaskInfoInstanceList = workTaskInfoManager.exportInfo(workTaskInfoList);
        if (workTaskInfoInstanceList.size() == 0) {
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
            String fileName = "结束任务_" + formatter.format(new Date()) + ".xls";
            String[] headers = {"标题", "负责人", "发布人", "状态", "计划开始时间", "计划完成时间", "实际开始时间", "完成或关闭时间", "效率", "评级"};
            String[] fieldNames = {"title", "leaderName", "publisherName", "statusName", "starttime", "plantime", "exectime", "committime", "efficiency", "evalscore"};
            List<WorkTaskInfoInstance> dataset = workTaskInfoInstanceList;
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

    
    
    /**
     * 任务模块的抄送功能
     * 跳转到抄送的发起表单.
     * @throws Exception 
     * cz 20181212
     */
    @RequestMapping("worktask-CC-input-new")
    public String worktaskCCInputNew(@RequestParam("id") Long id , RedirectAttributes redirectAttributes, Model model,HttpServletRequest request) throws Exception {
    	
    	String notifynames = "";
    	
    	 try {
	             if (id == null || "".equals(id)) {
	                 messageHelper.addFlashMessage(redirectAttributes, "core.success.save", "获取参数错误");
	                 return "worktask/work-task-info-sent-list";
	             }
    	 	
			//取出要抄送的任务内容
			WorkTaskInfo workTaskInfo = workTaskInfoManager.get(id);
			
			//取出该任务已经抄送过的人
			 List<WorkTaskCc> ccListInput = workTaskCcManager.findBy("workTaskInfo.id", workTaskInfo.getId());
             if (ccListInput != null && ccListInput.size() > 0) {
            	 for(WorkTaskCc wtc:ccListInput){
	            	 Map<String, Object> queryForMap = null;
	            	 queryForMap = jdbcTemplate.queryForMap("SELECT FULL_NAME FROM person_info WHERE id=  '"+wtc.getCcno()+"'");
	            	 if(null != queryForMap){
	            		String fullNameString =  StringUtil.toString(queryForMap.get("FULL_NAME"));
	            		notifynames = notifynames +fullNameString + ",";
	            	 }
            	}
             }
             if (!"".equals(notifynames))
                 notifynames = notifynames.substring(0, notifynames.length() - 1);
             
             model.addAttribute("notifynames", notifynames);
             model.addAttribute("workTaskInfo", workTaskInfo);
             model.addAttribute("id", workTaskInfo.getId());
             
         } catch (ArithmeticException e) {
             logger.error("添加抄送人人初始化页面-：" + e.getMessage() + "\r\n" + e.fillInStackTrace());
             messageHelper.addFlashMessage(redirectAttributes, "core.success.save", "查询异常");
             return "worktask/work-task-info-sent-list";
         }
         return "worktask/work-task-cc-input";
    }
    
    
    /**
     * 任务模块的抄送功能
     * 跳转到抄送的发起表单.
     * @throws Exception 
     * cz 20181212
     */
    @RequestMapping("worktask-CC-input-save")
    public String worktaskCCInputSave(
    					@RequestParam("id") String id
			    		,@RequestParam(value = "sendee") String sendee
			    		//,@RequestParam(value = "copyNames") String copyNames
			    		//,@RequestParam(value = "url") String url
			    		,@RequestParam(value = "title",required=false) String title
			    		, RedirectAttributes redirectAttributes, Model model
	) throws Exception {
    	String tenantId = tenantHolder.getTenantId();
    	//取出要抄送的任务内容
		WorkTaskInfo workTaskInfo = workTaskInfoManager.get(Long.parseLong(id));
    	
    	 try {
	             if (id == null || "".equals(id)) {
	                 messageHelper.addFlashMessage(redirectAttributes, "core.success.save", "获取参数错误");
	                 return "worktask/work-task-info-sent-list";
	             }
	             
	             if (null == currentUserHolder ) {
	 			    logger.error("请重新登录");
	 			    messageHelper.addFlashMessage(redirectAttributes, "core.success.save", "请重新登录");
	 			    return "worktask/work-task-info-sent-list";
	 			}
	             if(!sendee.isEmpty()){
	            	 
	 				String userId = currentUserHolder.getUserId();
	 				
	 				String[] copyIdList = null;  
	 				String[] copyNameList = null; 
	 				copyIdList = sendee.split(",");
	 				//copyNameList = copyNames.split(","); 
	 				
	 				for (int i = 0; i < copyIdList.length; i++) {
	 					//查看是否已经抄送过
	 					String ccUserIds = copyIdList[i];
	 					//String ccUserNames=copyNameList[i];
	 		
	 					 String hql = "from WorkTaskCc where  ccno=? and info_id=?";
	 		            List<WorkTaskCc> wTCc = workTaskCcManager.find(hql, Long.parseLong(ccUserIds),workTaskInfo.getId());
	 					
		            	if(null != wTCc && wTCc.size()>0){
		            		//抄送过
		            		continue;
		            	}else{
		            		//没抄送过的人，加入抄送表
			 				String taskLeader = workTaskInfo.getLeader().toString();
		            		String publisher = workTaskInfo.getPublisher().toString();
		                    if (!(taskLeader.equals(ccUserIds)) && !(publisher.equals(ccUserIds))) {
		                        WorkTaskCc workTaskCc = new WorkTaskCc();
		                        workTaskCc.setCcno(Long.parseLong(ccUserIds));
		                        workTaskCc.setWorkTaskInfo(workTaskInfo);
		                        workTaskCcManager.save(workTaskCc);
		                        
		                      //发消息
			                    String content = " [" + currentUserHolder.getName() + "]" + "向您抄送了" + "[" + workTaskInfo.getTitle()
			                            + "]" + "任务，请查看。";
			                    
			                    notificationConnector.send(workTaskInfo.getId().toString(), tenantId,
			                             currentUserHolder.getUserId().toString(), ccUserIds, title, content,
			                             MsgConstants.MSG_TYPE_TASK);
		                    }
		            	 }
		          	}
	 			}
	      } catch (ArithmeticException e) {
             logger.error("添加抄送人人初始化页面-：" + e.getMessage() + "\r\n" + e.fillInStackTrace());
             messageHelper.addFlashMessage(redirectAttributes, "core.success.save", "查询异常");
             return "redirect:/worktask/work-task-charge-list.do";
         }
    	 
    	 messageHelper.addFlashMessage(redirectAttributes, "core.success.save",
	                "抄送成功");
    	 
    	 return "redirect:/worktask/work-task-charge-list.do";
    }
    // ~ ======================================================================
    @Autowired
    public void setUserConnector(UserConnector userConnector) {
        this.userConnector = userConnector;
    }

    @Resource
    public void setWorkTaskInfoManager(WorkTaskInfoManager workTaskInfoManager) {
        this.workTaskInfoManager = workTaskInfoManager;
    }

    @Resource
    public void setWorkTaskCcManager(WorkTaskCcManager workTaskCcManager) {
        this.workTaskCcManager = workTaskCcManager;
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
    public void setStoreConnector(StoreConnector storeConnector) {
        this.storeConnector = storeConnector;
    }

    @Resource
    public void setExportor(Exportor exportor) {
        this.exportor = exportor;
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
    public void setNotificationConnector(NotificationConnector notificationConnector) {
        this.notificationConnector = notificationConnector;
    }

    @Resource
    public void setFileUploadAPI(FileUploadAPI fileUploadAPI) {
        this.fileUploadAPI = fileUploadAPI;
    }

    @Resource
    public void setWebAPI(WebAPI webAPI) {
        this.webAPI = webAPI;
    }

    @Resource
    public void setAccountInfoManager(AccountInfoManager accountInfoManager) {
        this.accountInfoManager = accountInfoManager;
    }

}
