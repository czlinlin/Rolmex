package com.mossle.project.rs;


import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.mossle.api.notification.NotificationConnector;
import com.mossle.api.tenant.TenantHolder;
import com.mossle.common.utils.StringUtils;
import com.mossle.core.annotation.Log;
import com.mossle.core.auth.CurrentUserHolder;
import com.mossle.core.mapper.JsonMapper;
import com.mossle.core.util.BaseDTO;
import com.mossle.msg.MsgConstants;
import com.mossle.project.persistence.domain.WorkProjectInfo;
import com.mossle.project.persistence.domain.WorkProjectTaskbind;
import com.mossle.project.persistence.manager.WorkProjectInfoManager;
import com.mossle.worktask.persistence.domain.WorkTaskCc;
import com.mossle.worktask.persistence.domain.WorkTaskInfo;
import com.mossle.project.persistence.manager.WorkProjectTaskbindManager;
import com.mossle.worktask.persistence.manager.WorkTaskCcManager;
import com.mossle.worktask.persistence.manager.WorkTaskInfoManager;
import com.mossle.project.persistence.manager.WorkProjectNotifyManager;
import com.mossle.project.persistence.domain.WorkProjectNotify;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.mossle.user.persistence.domain.AccountInfo;
import com.mossle.user.persistence.manager.AccountInfoManager;


@Component
@Path("project")
public class ProjectResource {
    private static Logger logger = LoggerFactory
            .getLogger(ProjectResource.class);
    private WorkProjectInfo workProjectInfo;
    private JsonMapper jsonMapper = new JsonMapper();
    private CurrentUserHolder currentUserHolder;
    private WorkProjectInfoManager workProjectInfoManager;
    private AccountInfoManager accountInfoManager;
    private WorkProjectTaskbindManager workProjectTaskbindManager;
    private WorkTaskInfoManager workTaskInfoManager;
    private WorkTaskCcManager workTaskCcManager;
    private TenantHolder tenantHolder;
    private WorkProjectNotifyManager workProjectNotifyManager;
    private NotificationConnector notificationConnector;//发送消息


    //当前登录人，存在已发布和进行中的项目 返回false
    public Boolean judgeMethod(Long userId) {
        List<WorkProjectInfo> workProjectInfoList = workProjectInfoManager.findBy("leader", userId);
        for (WorkProjectInfo workProjectInfo : workProjectInfoList) {
            if (workProjectInfo.getDatastatus().equals("1") && (userId.toString()).equals(workProjectInfo.getLeader().toString())) {
                if (workProjectInfo.getStatus().equals("0") || workProjectInfo.getStatus().equals("1")) {
                    return false;
                }
            }
        }
        return true;
    }

    @POST
    @Path("work-project-info-publish")
    @Produces(MediaType.APPLICATION_JSON)
    @Log(desc = "发布", action = "update", operationDesc = "个人信息-工作中心-项目-草稿项目-发布操作")
    public BaseDTO publishProject(@FormParam("id") Long id) {
        BaseDTO result = new BaseDTO();
        String tenantId = tenantHolder.getTenantId();
        try {
            if (id == null || id < 1) {
                result.setCode(500);
                logger.debug("项目发布操作-获取参数id错误");
                result.setMessage("获取参数错误");
                return result;
            }
            WorkProjectInfo workProjectInfo = workProjectInfoManager.get(id);
            workProjectInfo.setStatus("0");
            workProjectInfo.setDatastatus("1");
            workProjectInfo.setPublishtime(new Date());
            workProjectInfoManager.save(workProjectInfo);

            result.setCode(200);

            if (workProjectInfo.getDatastatus().equals("1")) {
                String title = "[" + workProjectInfo.getTitle() + "]项目发布提醒";
                String content =
                        "[" + currentUserHolder.getName() + "]" +
                                "发布的[" +
                                workProjectInfo.getTitle() +
                                "]项目，由您负责";
                String receiver = workProjectInfo.getLeader().toString();
                String bussinessId = workProjectInfo.getId().toString();

                //新建发送消息给负责人和知会人
                notificationConnector.send(
                        bussinessId,
                        tenantId,
                        currentUserHolder.getUserId().toString(),
                        receiver,
                        title,
                        content, MsgConstants.MSG_TYPE_PROJECT);

                List<WorkProjectNotify> workProjectNotifyList = workProjectNotifyManager.findBy("workProjectInfo.id", id);
                if (workProjectNotifyList != null && workProjectNotifyList.size() > 0) {
                    title = "[" + workProjectInfo.getTitle() + "]项目知会提醒";
                    content = "[" + currentUserHolder.getName() + "]" +
                            "向您知会了" +
                            workProjectInfo.getTitle() +
                            "项目，请查看。";
                    for (WorkProjectNotify workProjectNotify : workProjectNotifyList) {
                        receiver = workProjectNotify.getUserid().toString();
                        notificationConnector.send(
                                workProjectInfo.getId().toString(),
                                tenantId,
                                currentUserHolder.getUserId().toString(),
                                receiver,
                                title,
                                content, MsgConstants.MSG_TYPE_PROJECT);
                    }
                }
            }

        } catch (ArithmeticException e) {
            result.setCode(500);
            result.setMessage("发布出错");
            logger.error("项目发布操作-：" + e.getMessage() + "\r\n" + e.fillInStackTrace());
        }
        return result;
    }

    @POST
    @Path("work-project-info-submit")
    @Produces(MediaType.APPLICATION_JSON)
    @Log(desc = "提交", action = "update", operationDesc = "个人信息-工作中心-项目-负责项目-提交操作")
    public BaseDTO ProjectSubmit(@FormParam("id") Long id) {
        BaseDTO result = new BaseDTO();
        try {
            if (id == null || id < 1) {
                result.setCode(500);
                logger.debug("项目提交操作-获取参数id错误");
                result.setMessage("获取参数错误");
                return result;
            }

            WorkProjectInfo projectModel = workProjectInfoManager.findUniqueBy("id", id);
            if (projectModel == null) {
                result.setCode(500);
                logger.debug("项目提交操作-没有查询到项目信息");
                result.setMessage("没有查询到项目信息");
                return result;
            } else if (!projectModel.getStatus().equals("1")) {
                result.setCode(500);
                result.setMessage("该项目状态不是进行中或则状态已改变");
                return result;
            }

            List<WorkProjectTaskbind> taskBindList = workProjectTaskbindManager.findBy("workProjectInfo.id", id);
            if (taskBindList != null && taskBindList.size() > 0) {
                for (WorkProjectTaskbind taskbind : taskBindList) {
                    WorkTaskInfo taskInfo = taskbind.getWorkTaskInfo();
                    if (taskInfo.getDatastatus().equals("1")) {
                        if (!taskInfo.getStatus().equals("2") || !taskInfo.getStatus().equals("3") || !taskInfo.getStatus().equals("4")) {
                            result.setCode(500);
                            result.setMessage("该项目中尚有任务未处理，不能提交项目！");
                            return result;
                        }
                    }
                }
            }

            String efficiency = "0";
            Date commitDate = new Date();
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Long times = (commitDate.getTime() - projectModel.getPlandate().getTime()) / 1000;
            if (times > 3600)
                efficiency = "2";//延时
            else if (times < 0)
                efficiency = "1";//提前

            projectModel.setStatus("2");
            projectModel.setCommittime(commitDate);
            projectModel.setEfficiency(efficiency);
            Integer hoursNum = (int) (Math.floor(Math.abs(times) / 3600));
            projectModel.setHoursnum(hoursNum);

            workProjectInfoManager.save(projectModel);

            result.setCode(200);
            result.setMessage("提交成功");

            String title = "[" + projectModel.getTitle() + "]" + "项目完成提醒";
            String content = "[" + currentUserHolder.getName() + "]负责的" +
                    "[" + projectModel.getTitle() + "]" +
                    "项目已提交，请查看。";

            String receiver = projectModel.getPublisher().toString();
            String bussinessId = projectModel.getId().toString();
            String tenantId = tenantHolder.getTenantId();
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
                title = "[" + projectModel.getTitle() + "]" + "项目完成提醒";
                for (WorkProjectNotify workProjectNotify : workProjectNotifyList) {

                    content = "发布人[" + accountInfoManager.findUniqueBy("id", workProjectNotify.getUserid()).getDisplayName() +
                            "]" + "发布的" + "[" + projectModel.getTitle() + "]" + "项目已提交，请查看。";
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
        } catch (ArithmeticException e) {
            result.setCode(500);
            result.setMessage("提交出错");
            logger.error("项目提交操作-：" + e.getMessage() + "\r\n" + e.fillInStackTrace());
        }
        return result;
    }

    @POST
    @Path("work-project-info-del")
    @Produces(MediaType.APPLICATION_JSON)
    @Log(desc = "删除", action = "delete", operationDesc = "个人信息-工作中心-项目-已发项目-删除操作")
    public BaseDTO ProjectLogDel(@FormParam("id") Long id) {
        BaseDTO result = new BaseDTO();
        try {
            if (id == null || id < 1) {
                result.setCode(500);
                logger.debug("项目删除操作-获取参数id错误");
                result.setMessage("获取参数错误");
                return result;
            }

            WorkProjectInfo projectModel = workProjectInfoManager.findUniqueBy("id", id);
            if (projectModel == null) {
                result.setCode(500);
                logger.debug("项目删除操作-没有查询到项目信息");
                result.setMessage("没有查询到项目信息");
                return result;
            } else if (projectModel.getDatastatus().equals("2")) {
                result.setCode(500);
                result.setMessage("该项目已经被删除，刷新查看");
                return result;
            }

            projectModel.setDatastatus("2");
            List<WorkProjectTaskbind> taskBindList = workProjectTaskbindManager.findBy("workProjectInfo.id", id);
            for (WorkProjectTaskbind workProjectTaskbind : taskBindList) {
                WorkTaskInfo workTaskInfo = workProjectTaskbind.getWorkTaskInfo();
                //包括草稿任务
                workTaskInfo.setDatastatus("3");//发布人删除
                workTaskInfoManager.save(workTaskInfo);
                  /*5)删除完成后时，需给任务负责人发送消息，消息格式如下：
                a)标题：[任务名称]任务删除通知；
                内容： [项目发布人姓名]删除了您负责的[任务名称]任务。；*/
                String title = "[" + workTaskInfo.getTitle() + "]" + "任务删除通知";
                String content = "[" + currentUserHolder.getName() + "]删除了您负责的" + "[" + workTaskInfo.getTitle() + "]任务。";
                String receiver = workTaskInfo.getLeader().toString();
                String bussinessId = workTaskInfo.getId().toString();
                String tenantId = tenantHolder.getTenantId();
                notificationConnector.send(
                        bussinessId,
                        tenantId,
                        currentUserHolder.getUserId().toString(),
                        receiver,
                        title,
                        content, MsgConstants.MSG_TYPE_TASK);

                   /*
                    6)删除完成后时，需给任务抄送人发送消息，消息格式如下：
                a)标题：[任务名称]任务删除通知；
                b)内容： [项目发布人姓名]删除了抄送给您的[任务名称]任务。；*/

                List<WorkTaskCc> ccList = workTaskCcManager.findBy("workTaskInfo.id", workTaskInfo.getId());
                if (ccList != null && ccList.size() > 0) {
                    title = "[" + workTaskInfo.getTitle() + "]" + "任务删除通知";
                    for (WorkTaskCc cc : ccList) {
                        content = "[" + currentUserHolder.getName() + "]删除了抄送给您的[" + workTaskInfo.getTitle() + "]任务。";
                        receiver = cc.getCcno().toString();
                        notificationConnector.send(
                                bussinessId,
                                tenantId,
                                currentUserHolder.getUserId().toString(),
                                receiver,
                                title,
                                content, MsgConstants.MSG_TYPE_TASK);
                    }
                }


                List<WorkTaskInfo> workTaskInfoChildlist = workTaskInfoManager.findBy("uppercode", workTaskInfo.getId());
                //包括草稿任务
                for (WorkTaskInfo workTaskInfoChild : workTaskInfoChildlist) {
                    workTaskInfoChild.setDatastatus("3");
                    workTaskInfoManager.save(workTaskInfoChild);
                  /*  6)删除完成后时，需给任务下子任务负责人发送消息，消息格式如下：
                        a)标题：[任务名称]任务删除通知；
                        b)内容： [项目发布人姓名]删除了抄送给您的[任务名称]任务。；*/
                    title = "[" + workTaskInfoChild.getTitle() + "]" + "任务删除通知";
                    content = "[" + currentUserHolder.getName() + "]删除了您负责的" + "[" + workTaskInfoChild.getTitle() + "]任务。";
                    receiver = workTaskInfoChild.getLeader().toString();
                    bussinessId = workTaskInfoChild.getId().toString();
                    notificationConnector.send(
                            bussinessId,
                            tenantId,
                            currentUserHolder.getUserId().toString(),
                            receiver,
                            title,
                            content, MsgConstants.MSG_TYPE_TASK);
          /*
                    6)删除完成后时，需给子任务抄送人发送消息，消息格式如下：
                a)标题：[任务名称]任务删除通知；
                b)内容： [项目发布人姓名]删除了抄送给您的[任务名称]任务。；*/

                    List<WorkTaskCc> ccChildList = workTaskCcManager.findBy("workTaskInfo.id", workTaskInfoChild.getId());
                    if (ccList != null && ccChildList.size() > 0) {
                        title = "[" + workTaskInfoChild.getTitle() + "]" + "任务删除通知";
                        for (WorkTaskCc cc : ccChildList) {
                            content = "[" + currentUserHolder.getName() + "]删除了抄送给您的[" + workTaskInfo.getTitle() + "]任务。";
                            receiver = cc.getCcno().toString();
                            notificationConnector.send(
                                    bussinessId,
                                    tenantId,
                                    currentUserHolder.getUserId().toString(),
                                    receiver,
                                    title,
                                    content, MsgConstants.MSG_TYPE_TASK);
                        }
                    }


                }
            }

            workProjectInfoManager.save(projectModel);
            result.setCode(200);
            /*3)删除完成后时，需给项目负责人发送消息，消息格式如下：
            a)标题：[项目名称]项目删除通知；
            b)内容：[发布人姓名]删除了您负责的[项目名称]项目。；*/
            String title = "[" + projectModel.getTitle() + "]" + "项目删除通知";
            String content = "[" + currentUserHolder.getName() + "]删除了您负责的" + "[" + projectModel.getTitle() + "]项目。";
            String receiver = projectModel.getLeader().toString();
            String bussinessId = projectModel.getId().toString();
            String tenantId = tenantHolder.getTenantId();
            notificationConnector.send(
                    bussinessId,
                    tenantId,
                    currentUserHolder.getUserId().toString(),
                    receiver,
                    title,
                    content, MsgConstants.MSG_TYPE_PROJECT);

                /*4)删除完成后时，需给项目知会人发送消息，消息格式如下：
                a)标题：[项目名称]项目删除通知；
                b)内容：[发布人姓名]删除了知会给您的[项目名称]项目。；*/

            List<WorkProjectNotify> workProjectNotifyList = workProjectNotifyManager.findBy("workProjectInfo.id", id);
            if (workProjectNotifyList != null && workProjectNotifyList.size() > 0) {
                title = "[" + projectModel.getTitle() + "]" + "项目删除通知";
                for (WorkProjectNotify workProjectNotify : workProjectNotifyList) {
                    content = "[" + accountInfoManager.findUniqueBy("id", projectModel.getPublisher()).getDisplayName() +
                            "]删除了知会给您的[" + projectModel.getTitle() + "]项目。";
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


        } catch (ArithmeticException e) {
            result.setCode(500);
            result.setMessage("删除出错");
            logger.error("项目删除操作-：" + e.getMessage() + "\r\n" + e.fillInStackTrace());
        }
        return result;
    }

    @POST
    @Path("work-project-info-realdel")
    @Produces(MediaType.APPLICATION_JSON)
    @Log(desc = "删除(物理)", action = "delete", operationDesc = "个人信息-工作中心-项目-草稿项目-删除(物理)操作")
    public BaseDTO TaskRealDel(@FormParam("id") Long id) {
        BaseDTO result = new BaseDTO();
        try {
            if (id == null || id < 1) {
                result.setCode(500);
                logger.debug("项目删除(物理)操作-获取参数id错误");
                result.setMessage("获取参数错误");
                return result;
            }

            WorkProjectInfo projectModel = workProjectInfoManager.findUniqueBy("id", id);
            if (projectModel == null) {
                result.setCode(500);
                logger.debug("项目删除(物理)操作-没有查询到项目信息");
                result.setMessage("没有查询到项目信息");
                return result;
            }


            workProjectInfoManager.remove(projectModel);
            result.setCode(200);
            result.setMessage("删除成功！");
        } catch (ArithmeticException e) {
            result.setCode(500);
            result.setMessage("删除出错");
            logger.error("项目删除(物理)操作-：" + e.getMessage() + "\r\n" + e.fillInStackTrace());
        }
        return result;
    }

    /*lilei 2017.09.11*/
    @POST
    @Path("work-project-info-close")
    @Produces(MediaType.APPLICATION_JSON)
    @Log(desc = "关闭项目", action = "update", operationDesc = "个人信息-工作中心-项目-已发项目-关闭操作")
    public BaseDTO TaskClosed(@FormParam("id") Long id) {
        BaseDTO result = new BaseDTO();
        try {
            if (id == null || id < 1) {
                result.setCode(500);
                logger.debug("关闭项目操作-获取参数id错误");
                result.setMessage("获取参数错误");
                return result;
            }

            WorkProjectInfo projectModel = workProjectInfoManager.findUniqueBy("id", id);
            if (projectModel == null) {
                result.setCode(500);
                logger.debug("关闭项目操作-没有查询到项目信息");
                result.setMessage("没有查询到项目信息");
                return result;
            } else if (!projectModel.getStatus().equals("0") && !projectModel.getStatus().equals("1")) {
                result.setCode(500);
                result.setMessage("该项目状态不是已发布或进行中状态");
                return result;
            }

            List<WorkProjectTaskbind> workProjectTaskbindList = workProjectTaskbindManager.findBy("workProjectInfo.id", id);
            if (!CollectionUtils.isEmpty(workProjectTaskbindList)) {
                for (WorkProjectTaskbind workProjectTaskbind : workProjectTaskbindList) {
                    WorkTaskInfo workTaskInfo = workProjectTaskbind.getWorkTaskInfo();
                    List<WorkTaskInfo> childTaskInfoList = workTaskInfoManager.findBy("uppercode", workTaskInfo.getId());
                    if (!CollectionUtils.isEmpty(childTaskInfoList)) {
                        for (WorkTaskInfo childTask : childTaskInfoList) {
                            if (childTask.getDatastatus().equals("1")) {
                                if (childTask.getStatus().equals("0") || childTask.getStatus().equals("1")) {
                                    childTask.setStatus("3");
                                    childTask.setCommittime(new Date());
                                    workTaskInfoManager.save(childTask);
                                    String title = "[" + childTask.getTitle() + "]" + "任务关闭通知";
                                    String content = "您负责的" + "[" + childTask.getTitle() + "]" +
                                            "任务，由发布人" + "[" +
                                            currentUserHolder.getName() + "]" + "手动关闭，请查看。";
                                    String receiver = childTask.getLeader().toString();
                                    String bussinessId = childTask.getId().toString();
                                    String tenantId = tenantHolder.getTenantId();
                                    //关闭任务，发送消息给负责人
                                    notificationConnector.send(
                                            bussinessId,
                                            tenantId,
                                            currentUserHolder.getUserId().toString(),
                                            receiver,
                                            title,
                                            content, MsgConstants.MSG_TYPE_TASK);

                                }
                                if (childTask.getStatus().equals("4") || childTask.getStatus().equals("5")) {
                                    String title = "[" + childTask.getTitle() + "]" + "任务关闭通知";
                                    String content = "您负责的" + "[" + childTask.getTitle() + "]" +
                                            "任务的上级任务" + "[" + workTaskInfo.getTitle() + "]" + "，由[" +
                                            currentUserHolder.getName() + "]" + "手动关闭，请查看。";
                                    String receiver = childTask.getLeader().toString();
                                    String bussinessId = childTask.getId().toString();
                                    String tenantId = tenantHolder.getTenantId();
                                    //关闭任务，发送消息给负责人
                                    notificationConnector.send(
                                            bussinessId,
                                            tenantId,
                                            currentUserHolder.getUserId().toString(),
                                            receiver,
                                            title,
                                            content, MsgConstants.MSG_TYPE_TASK);
                                }
                            }
                        }
                    }
                    if (workTaskInfo.getDatastatus().equals("1")) {
                        if (workTaskInfo.getStatus().equals("0") || workTaskInfo.getStatus().equals("1")) {

                            //关闭任务，发送消息给负责人
                            String title = "[" + workTaskInfo.getTitle() + "]" + "任务关闭通知";
                            String content = "您负责的" + "[" + workTaskInfo.getTitle() + "]" +
                                    "任务，由发布人" + "[" +
                                    currentUserHolder.getName() + "]" + "手动关闭，请查看。";
                            String receiver = workTaskInfo.getLeader().toString();
                            String bussinessId = workTaskInfo.getId().toString();
                            String tenantId = tenantHolder.getTenantId();
                            notificationConnector.send(
                                    bussinessId,
                                    tenantId,
                                    currentUserHolder.getUserId().toString(),
                                    receiver,
                                    title,
                                    content, MsgConstants.MSG_TYPE_TASK);
                            /*5)如果项目含有下级任务，则项目关闭时，须向状态为“未完成”的下级任务的抄送人发送消息提醒，消息格式如下：
                            a)标题：[任务标题]任务关闭通知；
                            b)内容：抄送您的[任务标题]任务，由 [操作人姓名]手动关闭，请查看。*/
                            List<WorkTaskCc> workTaskCcs = workTaskCcManager.findBy("workTaskInfo.id", workTaskInfo.getId());
                            if (workTaskCcs != null && workTaskCcs.size() > 0) {
                                title = "[" + workTaskInfo.getTitle() + "]" + "任务关闭通知";
                                content = "抄送您的[" + workTaskInfo.getTitle() + "]任务，由["
                                        + currentUserHolder.getName() + "]" +
                                        "手动关闭，请查看。";
                                for (WorkTaskCc WorkTaskCc : workTaskCcs) {
                                    receiver = WorkTaskCc.getCcno().toString();
                                    notificationConnector.send(
                                            workTaskInfo.getId().toString(),
                                            tenantId,
                                            currentUserHolder.getUserId().toString(),
                                            receiver,
                                            title,
                                            content, MsgConstants.MSG_TYPE_TASK);
                                }
                            }
                        }
                       /* 6)如果项目含有下级任务，则项目关闭时，须向状态为“已完成”的下级任务的负责人发送消息提醒，消息格式如下：
                        a)标题：上级[项目名称]关闭通知；
                        b)内容：您负责的[任务标题]任务的上级[上级项目/任务名称]，由 [操作人姓名]手动关闭，请查看。*/
                        if (workTaskInfo.getStatus().equals("4") || workTaskInfo.getStatus().equals("5")) {
                            String title = "[" + workTaskInfo.getTitle() + "]" + "任务关闭通知";
                            String content = "您负责的" + "[" + workTaskInfo.getTitle() + "]" +
                                    "任务的上级项目" + "[" + workProjectInfo.getTitle() + "]" + "，由" + "[" +
                                    currentUserHolder.getName() + "]" + "手动关闭，请查看。";
                            String receiver = workTaskInfo.getLeader().toString();
                            String bussinessId = workTaskInfo.getId().toString();
                            String tenantId = tenantHolder.getTenantId();
                            notificationConnector.send(
                                    bussinessId,
                                    tenantId,
                                    currentUserHolder.getUserId().toString(),
                                    receiver,
                                    title,
                                    content, MsgConstants.MSG_TYPE_TASK);
                            workTaskInfo.setStatus("3");
                            workTaskInfo.setCommittime(new Date());
                            workTaskInfoManager.save(workTaskInfo);
                        }
                    }
                }
            }
            projectModel.setStatus("3");
            projectModel.setCommittime(new Date());
            workProjectInfoManager.save(projectModel);

            result.setCode(200);
            result.setMessage("关闭项目成功！");


            /*a)标题：[项目名称]项目关闭通知；
            b)内容：您负责的[项目名称]项目，由 发布人[发布人姓名]手动关闭，请查看。*/
            String title = "[" + projectModel.getTitle() + "]" + "项目关闭通知";
            String content = "您负责的" + "[" + projectModel.getTitle() + "]" +
                    "项目，由发布人" +
                    "[" + currentUserHolder.getName() + "]" + "手动关闭，请查看。";

            String receiver = projectModel.getLeader().toString();
            String bussinessId = projectModel.getId().toString();
            String tenantId = tenantHolder.getTenantId();
            notificationConnector.send(
                    bussinessId,
                    tenantId,
                    currentUserHolder.getUserId().toString(),
                    receiver,
                    title,
                    content, MsgConstants.MSG_TYPE_PROJECT);

            /*3)项目关闭后，须向项目知会人发送消息提醒，消息格式如下：
            a)标题：[项目名称]项目关闭通知；
            内容：知会您的[项目名称]项目，由 发布人[发布人姓名]手动关闭，请查看。*/
            List<WorkProjectNotify> workProjectNotifyList = workProjectNotifyManager.findBy("workProjectInfo.id", id);
            if (workProjectNotifyList != null && workProjectNotifyList.size() > 0) {
                title = "[" + projectModel.getTitle() + "]" + "项目关闭通知";
                for (WorkProjectNotify workProjectNotify : workProjectNotifyList) {
                    content = "知会您的[" + projectModel.getTitle() +
                            "]项目，由发布人[" + accountInfoManager.findUniqueBy("id", projectModel.getPublisher()).getDisplayName() + "]手动关闭，请查看。";
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
        } catch (ArithmeticException e) {
            result.setCode(500);
            result.setMessage("关闭项目出错");
            logger.error("关闭项目操作-：" + e.getMessage() + "\r\n" + e.fillInStackTrace());
        }
        return result;
    }

    @POST
    @Path("work-project-info-evaluate")
    @Produces(MediaType.APPLICATION_JSON)
    @Log(desc = "项目评价", action = "update", operationDesc = "个人信息-工作中心-项目-已发项目-项目评价操作")
    public BaseDTO TaskEvaluate(
            @FormParam("id") Long id,
            @FormParam("score") Integer score,
            @FormParam("content") String content
    ) {
        BaseDTO result = new BaseDTO();
        try {
            if (id == null || id < 1) {
                result.setCode(500);
                logger.debug("项目评价操作-获取参数id错误");
                result.setMessage("获取参数错误");
                return result;
            }


            WorkProjectInfo projectModel = workProjectInfoManager.findUniqueBy("id", id);
            if (projectModel == null) {
                result.setCode(500);
                logger.debug("项目评价操作-没有查询到项目信息");
                result.setMessage("没有查询到项目信息");
                return result;
            } else if (!projectModel.getStatus().equals("2")) {
                result.setCode(500);
                result.setMessage("状态不符合！");
                return result;
            } else if (projectModel.getStatus().equals("4")) {
                result.setCode(500);
                result.setMessage("该项目已经被评价，无需重复评价");
                return result;
            }

            String encode = "utf-8";
            String evaluateConent = content;
            try {
                evaluateConent = java.net.URLDecoder.decode(content, encode);
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            if (evaluateConent.length() > 200) {
                result.setCode(500);
                result.setMessage("评价最多200个字");
                return result;
            }

            projectModel.setStatus("4");
            projectModel.setEvalscore(score);
            projectModel.setEvaluate(evaluateConent);
            projectModel.setEvaltime(new Date());
            workProjectInfoManager.save(projectModel);
            result.setCode(200);
            result.setMessage("评价成功");

            /* a)标题：[项目名称]项目评价通知
            b)内容：您负责的[项目名称]项目，由发布人【发布人姓名】评价完成，请查看。*/
            String title = "[" + projectModel.getTitle() + "]" + "项目评价提醒";
            String sendContent = "您负责的[" + projectModel.getTitle() + "]项目，由发布人" +
                    "[" + currentUserHolder.getName() + "]评价完成，请查看。";

            String receiver = projectModel.getLeader().toString();
            String bussinessId = projectModel.getId().toString();
            String tenantId = tenantHolder.getTenantId();
            //提交任务，发送消息给负责人
            notificationConnector.send(
                    bussinessId,
                    tenantId,
                    currentUserHolder.getUserId().toString(),
                    receiver,
                    title,
                    sendContent, MsgConstants.MSG_TYPE_PROJECT);
        } catch (ArithmeticException e) {
            result.setCode(500);
            result.setMessage("项目评价出错");
            logger.error("项目评价操作-：" + e.getMessage() + "\r\n" + e.fillInStackTrace());
        }
        return result;
    }

    @POST
    @Path("work-project-info-notifyshow")
    @Produces(MediaType.APPLICATION_JSON)
    @Log(desc = "查询知会人", action = "search", operationDesc = "个人信息-工作中心-项目-查询知会人")
    public BaseDTO projectNotify(@FormParam("id") Long id) {
        BaseDTO result = new BaseDTO();
        try {
            if (id == null || id < 1) {
                result.setCode(500);
                logger.debug("获取知会人-获取参数id错误");
                result.setMessage("获取参数错误");
                return result;
            }
            List<WorkProjectNotify> workProjectNotifyList = workProjectNotifyManager.findBy("workProjectInfo.id", id);
            List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
            if (workProjectNotifyList != null && workProjectNotifyList.size() > 0) {
                for (WorkProjectNotify workProjectNotify : workProjectNotifyList) {
                    Map<String, Object> map = new HashMap<String, Object>();
                    AccountInfo userInfo = accountInfoManager.findUniqueBy("id", workProjectNotify.getUserid());
                    if (userInfo == null)
                        map.put("name", "");
                    else
                        map.put("name", userInfo.getDisplayName() == null ? "" : userInfo.getDisplayName());
                    list.add(map);
                }
            }
            result.setCode(200);
            result.setData(list);
        } catch (ArithmeticException e) {
            result.setCode(500);
            result.setMessage("查询出错");
            logger.error("查询知会人异常：" + e.getMessage() + "\r\n" + e.fillInStackTrace());
        }
        return result;
    }

    @POST
    @Path("work-project-info-Progress")
    @Produces(MediaType.APPLICATION_JSON)
    public BaseDTO projectProgress(@FormParam("ids") String ids) {
        BaseDTO result = new BaseDTO();
        if (StringUtils.isBlank(ids)) {
            result.setCode(500);
            logger.debug("项目获取当前进度-获取参数错误");
            result.setMessage("获取当前进度参数错误");
            return result;
        }

        String encode = "utf-8";
        try {
            ids = java.net.URLDecoder.decode(ids, encode);
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>();
        Date nowDate = new Date();
        String[] idList = ids.split(",");
        for (String id : idList) {
            WorkProjectInfo projectModel = workProjectInfoManager.findUniqueBy("id", Long.parseLong(id));
            if (projectModel != null) {
                Long totalDays = (projectModel.getPlandate().getTime() - projectModel.getStartdate().getTime()) / (1000 * 3600 * 24) + 1;
                Long daysPassed = (nowDate.getTime() - projectModel.getStartdate().getTime()) / (1000 * 3600 * 24) + 1;

                double doubleTargetResult = (double) daysPassed / (double) totalDays;
                String targetPercentString = new java.text.DecimalFormat("#0.00").format(doubleTargetResult);
                int targetPercentLong = (int) (Float.parseFloat(targetPercentString) * 100);//目标进度

                if (!projectModel.getStatus().equals("0") && !projectModel.getStatus().equals("1"))
                    targetPercentLong = 100;

                List<WorkProjectTaskbind> taskBindList = workProjectTaskbindManager.findBy("workProjectInfo.id", Long.parseLong(id));
                int haveDays = 0;

                String percent;
                Map<String, Object> map = new HashMap<String, Object>();
                if (taskBindList != null && taskBindList.size() > 0) {
                    for (WorkProjectTaskbind taskbind : taskBindList) {
                        WorkTaskInfo taskInfo = taskbind.getWorkTaskInfo();
                        if (taskInfo != null) {
                            if (taskInfo.getStatus().equals("2") || taskInfo.getStatus().equals("3") || taskInfo.getStatus().equals("4")) {
                                haveDays += (taskInfo.getPlantime().getTime() - taskInfo.getStarttime().getTime()) / (1000 * 3600 * 24) + 1;
                            }
                        }
                    }
                }
                double doubleResult = (double) haveDays / (double) totalDays;
                percent = new java.text.DecimalFormat("#0.00").format(doubleResult);
                map.put("id", id);

                int percentLong = (int) (Float.parseFloat(percent) * 100);//当前进度
                if (percentLong > 100) percentLong = 100;
                else if (percentLong < 0) percentLong = 0;
                if (targetPercentLong > 100) targetPercentLong = 100;
                else if (targetPercentLong < 0) targetPercentLong = 0;


                map.put("targetpercent", targetPercentLong + "%");
                map.put("actualpercent", percentLong + "%");


                int diffPercent = percentLong - targetPercentLong;
                if (diffPercent >= 5)
                    map.put("bg", "green");
                else if (diffPercent >= 0 && diffPercent < 5)
                    map.put("bg", "yellow");
                else
                    map.put("bg", "red");

                mapList.add(map);
            }
        }
        result.setCode(200);
        result.setMessage("获取成功");
        result.setData(mapList);
        return result;
    }


    @POST
    @Path("work-project-info-exec")
    @Produces(MediaType.APPLICATION_JSON)
    @Log(desc = "执行", action = "exec", operationDesc = "个人信息-工作中心-项目-负责项目-执行操作")
    public BaseDTO ProjectExec(@FormParam("id") Long id) {
        BaseDTO result = new BaseDTO();
        try {
            if (id == null || id < 1) {
                result.setCode(500);
                logger.debug("项目执行操作-获取参数id错误");
                result.setMessage("获取参数错误");
                return result;
            }

            WorkProjectInfo workProjectInfo = workProjectInfoManager.get(id);
            if (workProjectInfo == null) {
                result.setCode(500);
                logger.debug("项目执行操作-没有查询到项目信息");
                result.setMessage("没有查询到项目信息");
                return result;
            } else if (!workProjectInfo.getStatus().equals("0")) {
                result.setCode(500);
                result.setMessage("该项目状态不是已发布或则状态已改变");
                return result;
            }

            workProjectInfo.setStatus("1");
            workProjectInfo.setExectime(new Date());
            workTaskInfoManager.save(workProjectInfo);
            result.setCode(200);
        } catch (ArithmeticException e) {
            result.setCode(500);
            result.setMessage("执行出错");
            logger.error("项目执行操作-：" + e.getMessage() + "\r\n" + e.fillInStackTrace());
        }
        return result;
    }


    // ~ ======================================================================
    @Resource
    public void setWorkProjectInfoManager(WorkProjectInfoManager workProjectInfoManager) {
        this.workProjectInfoManager = workProjectInfoManager;
    }

    @Resource
    public void setCurrentUserHolder(CurrentUserHolder currentUserHolder) {
        this.currentUserHolder = currentUserHolder;
    }


    @Resource
    public void setAccountInfoManager(AccountInfoManager accountInfoManager) {
        this.accountInfoManager = accountInfoManager;
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
    public void setWorkProjectNotifyManager(WorkProjectNotifyManager workProjectNotifyManager) {
        this.workProjectNotifyManager = workProjectNotifyManager;
    }

    @Resource
    public void setTenantHolder(TenantHolder tenantHolder) {
        this.tenantHolder = tenantHolder;
    }

    @Resource
    public void setNotificationConnector(NotificationConnector notificationConnector) {
        this.notificationConnector = notificationConnector;
    }

    @Resource
    public void setWorkTaskCcManager(WorkTaskCcManager workTaskCcManager) {
        this.workTaskCcManager = workTaskCcManager;
    }
}
