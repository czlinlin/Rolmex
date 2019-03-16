package com.mossle.worktask.rs;

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
import com.mossle.api.notification.NotificationDTO;
import com.mossle.api.tenant.TenantHolder;
import com.mossle.core.annotation.Log;
import com.mossle.core.auth.CurrentUserHolder;
import com.mossle.core.mapper.JsonMapper;
import com.mossle.core.query.PropertyFilter;
import com.mossle.core.util.BaseDTO;
import com.mossle.msg.MsgConstants;
import com.mossle.project.persistence.domain.WorkProjectInfo;
import com.mossle.project.persistence.domain.WorkProjectNotify;
import com.mossle.project.persistence.domain.WorkProjectTaskbind;
import com.mossle.project.persistence.manager.WorkProjectTaskbindManager;
import com.mossle.worktask.persistence.domain.WorkTaskCc;
import com.mossle.worktask.persistence.domain.WorkTaskInfo;
import com.mossle.worktask.persistence.manager.WorkTaskCcManager;
import com.mossle.worktask.persistence.manager.WorkTaskInfoManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mossle.pim.persistence.domain.WorkReportCc;
import com.mossle.pim.persistence.domain.WorkReportForward;
import com.mossle.user.persistence.domain.AccountInfo;
import com.mossle.user.persistence.manager.AccountInfoManager;

@Component
@Path("worktask")
public class WorkTaskResource {
    private static Logger logger = LoggerFactory.getLogger(WorkTaskResource.class);
    private WorkTaskInfo workTaskInfo;
    private WorkTaskInfoManager workTaskInfoManager;
    private JsonMapper jsonMapper = new JsonMapper();
    private CurrentUserHolder currentUserHolder;
    private WorkTaskCcManager workTaskCcManager;
    private AccountInfoManager accountInfoManager;
    private TenantHolder tenantHolder;
    private NotificationConnector notificationConnector;// 发送消息
    private WorkProjectTaskbindManager workProjectTaskbindManager;

    // 当前登录人，存在已发布和进行中的任务 返回false
    public Boolean judgeMethod(Long userId) {
        List<WorkTaskInfo> workTaskInfoList = workTaskInfoManager.findBy("leader", userId);
        for (WorkTaskInfo workTaskInfo : workTaskInfoList) {
            if (workTaskInfo.getDatastatus().equals("1")
                    && (userId.toString()).equals(workTaskInfo.getLeader().toString())) {
                if (workTaskInfo.getStatus().equals("0") || workTaskInfo.getStatus().equals("1")) {
                    return false;
                }
            }
        }
        return true;
    }

    @POST
    @Path("work-task-info-publish")
    @Produces(MediaType.APPLICATION_JSON)
    @Log(desc = "发布", action = "发布", operationDesc = "个人信息-工作中心-任务-草稿任务-发布")
    public BaseDTO TaskPublish(@FormParam("id") Long id) {
        BaseDTO result = new BaseDTO();
        String tenantId = tenantHolder.getTenantId();
        try {
            if (id == null || id < 1) {
                result.setCode(500);
                logger.debug("任务发布操作-获取参数id错误");
                result.setMessage("获取参数错误");
                return result;
            }
            WorkTaskInfo workTaskInfo = workTaskInfoManager.get(id);
            Long upcode = workTaskInfo.getUppercode();
            if (upcode != null && upcode != 0) {
                WorkTaskInfo workTaskInfoParent = workTaskInfoManager.get(upcode);
                String status = workTaskInfoParent.getStatus();
                if (status.equals("2") || status.equals("3") || status.equals("4")) {
                    result.setCode(500);
                    logger.debug("任务发布操作-父任务不是进行中和已发布状态不允许发布子任务");
                    result.setMessage("该任务的上级任务已完成或已关闭，不能发布！");
                    return result;
                }
            }
            workTaskInfo.setStatus("0");// 设置任务为已发布任务
            workTaskInfo.setDatastatus("1");// 设置任务为发布状态
            workTaskInfoManager.save(workTaskInfo);

            String title = "[" + workTaskInfo.getTitle() + "]" + "任务发布提醒";
            String content = "发布人[" + currentUserHolder.getName() + "]" + "发布的" + "[" + workTaskInfo.getTitle() + "]"
                    + "任务，由您负责";
            String receiver = workTaskInfo.getLeader().toString();
            String bussinessId = workTaskInfo.getId().toString();

            // 新建发送消息给负责人和抄送人
            notificationConnector.send(bussinessId, tenantId, currentUserHolder.getUserId().toString(), receiver, title,
                    content, MsgConstants.MSG_TYPE_TASK);

            List<WorkTaskCc> workTaskCcs = workTaskCcManager.findBy("workTaskInfo.id", id);
            if (workTaskCcs != null && workTaskCcs.size() > 0) {
                content = "发布人[" + currentUserHolder.getName() + "]" + "向您抄送了" + "[" + workTaskInfo.getTitle() + "]"
                        + "任务，请查看。";
                for (WorkTaskCc WorkTaskCc : workTaskCcs) {
                    receiver = WorkTaskCc.getCcno().toString();
                    notificationConnector.send(workTaskInfo.getId().toString(), tenantId,
                            currentUserHolder.getUserId().toString(), receiver, title, content,
                            MsgConstants.MSG_TYPE_TASK);
                }
            }
            result.setCode(200);
        } catch (ArithmeticException e) {
            result.setCode(500);
            result.setMessage("发布出错");
            logger.error("任务发布(物理)操作-：" + e.getMessage() + "\r\n" + e.fillInStackTrace());
        }
        return result;
    }

    @POST
    @Path("work-task-info-exec")
    @Produces(MediaType.APPLICATION_JSON)
    @Log(desc = "执行", action = "exec", operationDesc = "个人信息-工作中心-任务-待办任务-执行操作")
    public BaseDTO TaskExec(@FormParam("id") Long id) {
        BaseDTO result = new BaseDTO();
        try {
            if (id == null || id < 1) {
                result.setCode(500);
                logger.debug("任务执行操作-获取参数id错误");
                result.setMessage("获取参数错误");
                return result;
            }

            WorkTaskInfo taskModel = workTaskInfoManager.findUniqueBy("id", id);
            if (taskModel == null) {
                result.setCode(500);
                logger.debug("任务执行操作-没有查询到任务信息");
                result.setMessage("没有查询到任务信息");
                return result;
            } else if (!taskModel.getStatus().equals("0")) {
                result.setCode(500);
                result.setMessage("该任务状态不是已发布或则状态已改变");
                return result;
            }
            // 子任务执行，一级任务和项目都变成进行中状态
            taskModel.setStatus("1");
            taskModel.setExectime(new Date());
            if (taskModel.getUppercode() != null && taskModel.getUppercode() != 0) {
                WorkTaskInfo FTaskInfo = workTaskInfoManager.findUniqueBy("id", taskModel.getUppercode());
                FTaskInfo.setStatus("1");
                FTaskInfo.setExectime(new Date());
                workTaskInfoManager.save(FTaskInfo);

                WorkProjectTaskbind workProjectTaskbind = workProjectTaskbindManager.findUniqueBy("workTaskInfo.id",
                        FTaskInfo.getId());
                if (workProjectTaskbind != null) {
                    WorkProjectInfo workProjectInfo = workProjectTaskbind.getWorkProjectInfo();
                    workProjectInfo.setStatus("1");
                    workProjectInfo.setExectime(new Date());
                }
            }
            // 任务执行，项目变成进行中状态
            WorkProjectTaskbind workProjectTaskbind = workProjectTaskbindManager.findUniqueBy("workTaskInfo.id",
                    taskModel.getId());
            if (workProjectTaskbind != null) {
                WorkProjectInfo workProjectInfo = workProjectTaskbind.getWorkProjectInfo();
                workProjectInfo.setStatus("1");
                workProjectInfo.setExectime(new Date());
            }
            workTaskInfoManager.save(taskModel);
            result.setCode(200);
        } catch (ArithmeticException e) {
            result.setCode(500);
            result.setMessage("执行出错");
            logger.error("任务执行操作-：" + e.getMessage() + "\r\n" + e.fillInStackTrace());
        }
        return result;
    }

    @POST
    @Path("work-task-info-submit")
    @Produces(MediaType.APPLICATION_JSON)
    @Log(desc = "提交", action = "submit", operationDesc = "个人信息-工作中心-任务-待办任务-提交操作")
    public BaseDTO TaskSubmit(@FormParam("id") Long id) {
        BaseDTO result = new BaseDTO();
        try {
            if (id == null || id < 1) {
                result.setCode(500);
                logger.debug("任务提交操作-获取参数id错误");
                result.setMessage("获取参数错误");
                return result;
            }

            WorkTaskInfo taskModel = workTaskInfoManager.findUniqueBy("id", id);
            if (taskModel == null) {
                result.setCode(500);
                logger.debug("任务执行操作-没有查询到任务信息");
                result.setMessage("没有查询到任务信息");
                return result;
            } else if (!taskModel.getStatus().equals("1")) {
                result.setCode(500);
                result.setMessage("该任务状态不是进行中或状态已改变");
                return result;
            }

            List<PropertyFilter> propertyFilters = new ArrayList<PropertyFilter>();
            propertyFilters.add(new PropertyFilter("EQL_uppercode", id.toString()));
            propertyFilters.add(new PropertyFilter("INS_datastatus", "1"));
            propertyFilters.add(new PropertyFilter("INS_status", "0,1"));

            List<WorkTaskInfo> childList = workTaskInfoManager.find(propertyFilters);
            if (childList != null && childList.size() > 0) {
                result.setCode(500);
                result.setMessage("该任务下有子任务尚未提交");
                return result;
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

            workTaskInfoManager.save(taskModel);

            String title = "[" + taskModel.getTitle() + "]" + "任务提交通知";
            String content = "[" + currentUserHolder.getName() + "]" + "负责的" + "[" + taskModel.getTitle() + "]"
                    + "任务已提交，请查看。";

            String receiver = taskModel.getPublisher().toString();
            String bussinessId = taskModel.getId().toString();
            String tenantId = tenantHolder.getTenantId();
            // 提交任务，发送消息给发布人
            notificationConnector.send(bussinessId, tenantId, currentUserHolder.getUserId().toString(), receiver, title,
                    content, MsgConstants.MSG_TYPE_TASK);

            // 提交任务，发送消息给抄送人
            List<WorkTaskCc> ccList = workTaskCcManager.findBy("workTaskInfo.id", id);
            if (ccList != null && ccList.size() > 0) {
                title = "[" + taskModel.getTitle() + "]" + "任务提交通知";
                for (WorkTaskCc cc : ccList) {

                    content = "发布人[" + accountInfoManager.findUniqueBy("id", taskModel.getPublisher().toString()) + "]"
                            + "抄送给您的" + "[" + taskModel.getTitle() + "]" + "任务已提交" + "，请查看。";
                    receiver = cc.getCcno().toString();
                    notificationConnector.send(bussinessId, tenantId, currentUserHolder.getUserId().toString(),
                            receiver, title, content, MsgConstants.MSG_TYPE_TASK);
                }
            }

            result.setCode(200);
        } catch (ArithmeticException e) {
            result.setCode(500);
            result.setMessage("提交出错");
            logger.error("任务提交操作-：" + e.getMessage() + "\r\n" + e.fillInStackTrace());
        }
        return result;
    }

    @POST
    @Path("work-task-info-close")
    @Produces(MediaType.APPLICATION_JSON)
    @Log(desc = "关闭任务", action = "close", operationDesc = "个人信息-工作中心-任务-已办任务-关闭操作")
    public BaseDTO TaskClosed(@FormParam("id") Long id) {
        BaseDTO result = new BaseDTO();
        String childStatus = "";
        try {
            if (id == null || id < 1) {
                result.setCode(500);
                logger.debug("关闭任务操作-获取参数id错误");
                result.setMessage("获取参数错误");
                return result;
            }

            WorkTaskInfo taskModel = workTaskInfoManager.findUniqueBy("id", id);
            if (taskModel == null) {
                result.setCode(500);
                logger.debug("关闭任务操作-没有查询到任务信息");
                result.setMessage("没有查询到任务信息");
                return result;
            } else if (!taskModel.getStatus().equals("0") && !taskModel.getStatus().equals("1")) {
                result.setCode(500);
                result.setMessage("该任务状态不是已发布或进行中状态");
                return result;
            }

            taskModel.setStatus("3");
            taskModel.setCommittime(new Date());

            workTaskInfoManager.save(taskModel);

            List<WorkTaskInfo> taskChildList = workTaskInfoManager.findBy("uppercode", taskModel.getId());
            if (!CollectionUtils.isEmpty(taskChildList)) {
                for (WorkTaskInfo taskChild : taskChildList) {
                    childStatus = taskChild.getStatus();
                    if (!childStatus.equals("2") && !childStatus.equals("4")) {
                        taskChild.setStatus("3");
                        taskChild.setCommittime(new Date());
                        workTaskInfoManager.save(taskChild);
                    }
                }
            }
            result.setCode(200);

            String title = "[" + taskModel.getTitle() + "]" + "任务关闭通知";
            String content = "您负责的" + "[" + taskModel.getTitle() + "]" + "任务，由发布人" + "[" + currentUserHolder.getName()
                    + "]" + "手动关闭，请查看。";

            String receiver = taskModel.getLeader().toString();
            String bussinessId = taskModel.getId().toString();
            String tenantId = tenantHolder.getTenantId();
            // 关闭任务，发送消息给负责人
            notificationConnector.send(bussinessId, tenantId, currentUserHolder.getUserId().toString(), receiver, title,
                    content, MsgConstants.MSG_TYPE_TASK);

            // 关闭任务，发送消息给抄送人
            List<WorkTaskCc> ccList = workTaskCcManager.findBy("workTaskInfo.id", id);
            if (ccList != null && ccList.size() > 0) {
                title = "[" + taskModel.getTitle() + "]" + "任务关闭通知";
                for (WorkTaskCc cc : ccList) {

                    content = "抄送给您的" + "[" + taskModel.getTitle() + "]" + "由发布人[" + currentUserHolder.getName() + "]"
                            + "手动关闭，请查看。";
                    receiver = cc.getCcno().toString();
                    notificationConnector.send(bussinessId, tenantId, currentUserHolder.getUserId().toString(),
                            receiver, title, content, MsgConstants.MSG_TYPE_TASK);
                }
            }

            // 任务状态是发布中或者进行中状态
            // 给每个子任务的负责人发送消息
            if (!CollectionUtils.isEmpty(taskChildList)) {
                for (WorkTaskInfo taskChild : taskChildList) {
                    if (childStatus.equals("0") || childStatus.equals("1")) {
                        String titleChild = "[" + taskChild.getTitle() + "]" + "任务关闭通知";
                        String contentChild = "您负责的" + "[" + taskChild.getTitle() + "]" + "任务，由" + "["
                                + currentUserHolder.getName() + "]" + "手动关闭，请查看。";
                        String receiverChild = taskChild.getLeader().toString();
                        String bussinessIdChild = taskChild.getId().toString();
                        notificationConnector.send(bussinessIdChild, tenantId, currentUserHolder.getUserId().toString(),
                                receiverChild, titleChild, contentChild, MsgConstants.MSG_TYPE_TASK);
                    }
                    // 关闭任务，发送消息给子任务的抄送人
                    List<WorkTaskCc> ccChildList = workTaskCcManager.findBy("workTaskInfo.id", taskChild.getId());
                    if (ccChildList != null && ccChildList.size() > 0) {
                        title = "[" + taskModel.getTitle() + "]" + "任务关闭通知";
                        for (WorkTaskCc cc : ccChildList) {
                            content = "抄送给您的" + "[" + taskModel.getTitle() + "]" + "[" + currentUserHolder.getName()
                                    + "]" + "手动关闭，请查看。";
                            receiver = cc.getCcno().toString();
                            notificationConnector.send(bussinessId, tenantId, currentUserHolder.getUserId().toString(),
                                    receiver, title, content, MsgConstants.MSG_TYPE_TASK);
                        }
                    }
                }
            }
            // 任务状态是已完成、已评价状态的
            // 给每个子任务的负责人发送消息
            if (!CollectionUtils.isEmpty(taskChildList)) {
                for (WorkTaskInfo taskChild : taskChildList) {
                    if (taskChild.getStatus().equals("2") || taskChild.getStatus().equals("4")) {
                        String titleChild = "上级任务[" + taskModel.getTitle() + "]" + "关闭通知";
                        String contentChild = "您负责的" + "[" + taskChild.getTitle() + "]任务的上级任务" + "["
                                + taskModel.getTitle() + "]" + "由" + "[" + currentUserHolder.getName() + "]"
                                + "手动关闭，请查看。";
                        String receiverChild = taskChild.getLeader().toString();
                        String bussinessIdChild = taskChild.getId().toString();
                        notificationConnector.send(bussinessIdChild, tenantId, currentUserHolder.getUserId().toString(),
                                receiverChild, titleChild, contentChild, MsgConstants.MSG_TYPE_TASK);
                    }
                }
            }

        } catch (ArithmeticException e) {
            result.setCode(500);
            result.setMessage("关闭任务出错");
            logger.error("关闭任务操作-：" + e.getMessage() + "\r\n" + e.fillInStackTrace());
        }
        return result;
    }

    @POST
    @Path("work-task-info-del")
    @Produces(MediaType.APPLICATION_JSON)
    @Log(desc = "删除", action = "del", operationDesc = "个人信息-工作中心-任务-已发任务-删除操作")
    public BaseDTO TaskDel(@FormParam("id") Long id) {
        BaseDTO result = new BaseDTO();
        try {
            if (id == null || id < 1) {
                result.setCode(500);
                logger.debug("任务删除操作-获取参数id错误");
                result.setMessage("获取参数错误");
                return result;
            }

            WorkTaskInfo taskModel = workTaskInfoManager.findUniqueBy("id", id);
            if (taskModel == null) {
                result.setCode(500);
                logger.debug("任务删除操作-没有查询到任务信息");
                result.setMessage("没有查询到任务信息");
                return result;
            } else if (taskModel.getDatastatus().equals("2")) {
                result.setCode(500);
                result.setMessage("该任务已经被删除，刷新查看");
                return result;
            }

            taskModel.setDatastatus("2");
            List<WorkTaskInfo> taskChildList = workTaskInfoManager.findBy("uppercode", taskModel.getId());
            if (!CollectionUtils.isEmpty(taskChildList)) {
                for (WorkTaskInfo taskChild : taskChildList) {
                    taskChild.setDatastatus("2");
                    workTaskInfoManager.save(taskChild);
                    String title = "[" + taskChild.getTitle() + "]" + "任务删除通知";
                    String content = "[" + currentUserHolder.getName() + "]删除了您负责的" + "[" + taskChild.getTitle() + "]"
                            + "任务。";
                    String receiver = taskChild.getLeader().toString();
                    String bussinessId = taskChild.getId().toString();
                    String tenantId = tenantHolder.getTenantId();
                    //删除任务，发送消息给负责人
                    notificationConnector.send(bussinessId, tenantId, currentUserHolder.getUserId().toString(),
                            receiver, title, content, MsgConstants.MSG_TYPE_TASK);

					/*
                     * 6)删除完成后时，需给子任务的抄送人发送消息，消息格式如下： a)标题：[任务名称]任务删除通知； b)内容：
					 * [项目发布人姓名]删除了抄送给您的[任务名称]任务。；
					 */
                    List<WorkTaskCc> ccChildList = workTaskCcManager.findBy("workTaskInfo.id", taskChild.getId());
                    if (ccChildList != null && ccChildList.size() > 0) {
                        content = " [" + currentUserHolder.getName() + "]" + "删除了抄送给您的" + "[" + taskChild.getTitle()
                                + "]" + "任务。";
                        for (WorkTaskCc cc : ccChildList) {
                            receiver = cc.getCcno().toString();
                            notificationConnector.send(taskChild.getId().toString(), tenantId,
                                    currentUserHolder.getUserId().toString(), receiver, title, content,
                                    MsgConstants.MSG_TYPE_TASK);
                        }
                    }
                }
            }

            workTaskInfoManager.save(taskModel);
            result.setCode(200);
			/*
			 * 5)删除完成后时，需给任务负责人发送消息，消息格式如下： a)标题：[任务名称]任务删除通知； b)内容：
			 * [项目发布人姓名]删除了您负责的[任务名称]任务。；
			 */

            String title = "[" + taskModel.getTitle() + "]" + "任务删除通知";
            String content = "[" + currentUserHolder.getName() + "]删除了您负责的" + "[" + taskModel.getTitle() + "]" + "任务。";

            String receiver = taskModel.getLeader().toString();
            String bussinessId = taskModel.getId().toString();
            String tenantId = tenantHolder.getTenantId();
            notificationConnector.send(bussinessId, tenantId, currentUserHolder.getUserId().toString(), receiver, title,
                    content, MsgConstants.MSG_TYPE_TASK);

			/*
			 * 6)删除完成后时，需给任务的抄送人发送消息，消息格式如下： a)标题：[任务名称]任务删除通知； b)内容：
			 * [项目发布人姓名]删除了抄送给您的[任务名称]任务。；
			 */
            List<WorkTaskCc> ccList = workTaskCcManager.findBy("workTaskInfo.id", taskModel.getId());
            if (ccList != null && ccList.size() > 0) {
                content = " [" + currentUserHolder.getName() + "]" + "删除了抄送给您的" + "[" + taskModel.getTitle() + "]"
                        + "任务。";
                for (WorkTaskCc cc : ccList) {
                    receiver = cc.getCcno().toString();
                    notificationConnector.send(taskModel.getId().toString(), tenantId,
                            currentUserHolder.getUserId().toString(), receiver, title, content,
                            MsgConstants.MSG_TYPE_TASK);
                }
            }
        } catch (ArithmeticException e) {
            result.setCode(500);
            result.setMessage("删除出错");
            logger.error("任务删除操作-：" + e.getMessage() + "\r\n" + e.fillInStackTrace());
        }
        return result;
    }

    @POST
    @Path("work-task-info-realdel")
    @Produces(MediaType.APPLICATION_JSON)
    @Log(desc = "删除(物理)", action = "delete", operationDesc = "个人信息-工作中心-任务-草稿任务-删除(物理)操作")
    public BaseDTO TaskRealDel(@FormParam("id") Long id) {
        BaseDTO result = new BaseDTO();
        try {
            if (id == null || id < 1) {
                result.setCode(500);
                logger.debug("任务删除(物理)操作-获取参数id错误");
                result.setMessage("获取参数错误");
                return result;
            }

            WorkTaskInfo taskModel = workTaskInfoManager.findUniqueBy("id", id);
            if (taskModel == null) {
                result.setCode(500);
                logger.debug("任务删除(物理)操作-没有查询到任务信息");
                result.setMessage("没有查询到任务信息");
                return result;
            }

            List<WorkTaskInfo> taskChildList = workTaskInfoManager.findBy("uppercode", taskModel.getId());
            if (!CollectionUtils.isEmpty(taskChildList)) {
                for (WorkTaskInfo taskChild : taskChildList) {
                    workTaskInfoManager.remove(taskChild);
                }
            }

            workTaskInfoManager.remove(taskModel);
            result.setCode(200);
        } catch (ArithmeticException e) {
            result.setCode(500);
            result.setMessage("删除出错");
            logger.error("任务删除(物理)操作-：" + e.getMessage() + "\r\n" + e.fillInStackTrace());
        }
        return result;
    }

    @POST
    @Path("work-task-info-evaluate")
    @Produces(MediaType.APPLICATION_JSON)
    @Log(desc = "任务评价", action = "update", operationDesc = "个人信息-工作中心-任务-已办任务-任务评价操作")
    public BaseDTO TaskEvaluate(@FormParam("id") Long id, @FormParam("score") Integer score,
                                @FormParam("content") String content) {
        BaseDTO result = new BaseDTO();
        try {
            if (id == null || id < 1) {
                result.setCode(500);
                logger.debug("任务评价操作-获取参数id错误");
                result.setMessage("获取参数错误");
                return result;
            }

            WorkTaskInfo taskModel = workTaskInfoManager.findUniqueBy("id", id);
            if (taskModel == null) {
                result.setCode(500);
                logger.debug("任务评价操作-没有查询到任务信息");
                result.setMessage("没有查询到任务信息");
                return result;
            } else if (taskModel.getStatus().equals("4")) {
                result.setCode(500);
                result.setMessage("该任务已经被评价，无需重复评价");
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

            taskModel.setStatus("4");
            taskModel.setEvalscore(score);
            taskModel.setEvaluate(evaluateConent);
            taskModel.setEvaltime(new Date());
            workTaskInfoManager.save(taskModel);
            result.setCode(200);
            result.setMessage("评价成功");

			/*
			 * a)须给负责人发送消息，消息格式如下： i.标题：[任务标题]任务评价通知
			 * ii.内容：您负责的[任务标题]，由发布人【发布人姓名】评价完成，请查看。
			 */
            String title = "[" + taskModel.getTitle() + "]" + "任务评价通知";
            String sendContent = "您负责的[" + taskModel.getTitle() + "]由发布人[" + currentUserHolder.getName() + "]"
                    + "评价完成，请查看。";
            String receiver = taskModel.getLeader().toString();
            String bussinessId = taskModel.getId().toString();
            String tenantId = tenantHolder.getTenantId();
            // 提交任务，发送消息给负责人
            notificationConnector.send(bussinessId, tenantId, currentUserHolder.getUserId().toString(), receiver, title,
                    sendContent, MsgConstants.MSG_TYPE_TASK);
        } catch (ArithmeticException e) {
            result.setCode(500);
            result.setMessage("任务评价出错");
            logger.error("任务评价操作-：" + e.getMessage() + "\r\n" + e.fillInStackTrace());
        }
        return result;
    }

    @POST
    @Path("work-task-info-cc")
    @Produces(MediaType.APPLICATION_JSON)
    public BaseDTO ReportCCInfo(@FormParam("id") Long id) {
        BaseDTO result = new BaseDTO();
        try {
            if (id == null || id < 1) {
                result.setCode(500);
                logger.debug("获取抄送人-获取参数id错误");
                result.setMessage("获取参数错误");
                return result;
            }

            List<WorkTaskCc> ccList = workTaskCcManager.findBy("workTaskInfo.id", id);
            List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
            if (ccList != null && ccList.size() > 0) {
                for (WorkTaskCc cc : ccList) {
                    Map<String, Object> map = new HashMap<String, Object>();
                    AccountInfo userInfo = accountInfoManager.findUniqueBy("id", cc.getCcno());
                    if (userInfo == null)
                        map.put("name", "");
                    else
                        map.put("name", userInfo.getDisplayName() == null ? "" : userInfo.getDisplayName());
                    // map.put("status", cc.getStatus().equals("0")?"未读":"已读");
                    list.add(map);
                }
            }

            result.setCode(200);
            result.setData(list);

        } catch (ArithmeticException e) {
            result.setCode(500);
            result.setMessage("查询出错");
            logger.error("查询抄送人异常：" + e.getMessage() + "\r\n" + e.fillInStackTrace());
        }
        return result;
    }

    /* 任务备注保存 add by lilei at 2017-09-05 */
    @POST
    @Path("work-task-comment-save")
    @Produces(MediaType.APPLICATION_JSON)
    @Log(desc = "备注", action = "update", operationDesc = "个人信息-工作中心-任务-待办任务-备注提交操作")
    public BaseDTO TaskCommentSave(@FormParam("id") Long id, @FormParam("content") String content) {
        BaseDTO result = new BaseDTO();
        try {
            if (id == null || id < 1) {
                result.setCode(500);
                logger.debug("任务备注操作-获取参数id错误");
                result.setMessage("获取参数错误");
                return result;
            }

            if (content.equals("")) {
                result.setCode(400);
                result.setMessage("请填写备注内容");
                return result;
            }

            WorkTaskInfo taskModel = workTaskInfoManager.findUniqueBy("id", id);
            if (taskModel == null) {
                result.setCode(500);
                logger.debug("任务备注保存-没有查询到任务信息");
                result.setMessage("没有查询到任务信息");
                return result;
            }

            String encode = "utf-8";
            String commentConent = content;
            try {
                commentConent = java.net.URLDecoder.decode(content, encode);
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            if (commentConent.length() > 200) {
                result.setCode(400);
                result.setMessage("请输入200字以内的内容");
                return result;
            }

            String oldContent = taskModel.getRemarks() == null ? "" : taskModel.getRemarks();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            commentConent = "[" + currentUserHolder.getName() + "]" + "于[" + formatter.format(new Date()) + "]添加备注：<br>"
                    + commentConent;
            taskModel.setRemarks(oldContent + commentConent + "；<br>");

            workTaskInfoManager.save(taskModel);
            result.setCode(200);
            result.setMessage("备注保存成功");
        } catch (ArithmeticException e) {
            logger.error("任务备注保存-：" + e.getMessage() + "\r\n" + e.fillInStackTrace());
            result.setCode(500);
            result.setMessage("备注保存失败");
        }
        return result;
    }

    // ~ ======================================================================
    @Resource
    public void setWorkTaskInfoManager(WorkTaskInfoManager workTaskInfoManager) {
        this.workTaskInfoManager = workTaskInfoManager;
    }

    @Resource
    public void setCurrentUserHolder(CurrentUserHolder currentUserHolder) {
        this.currentUserHolder = currentUserHolder;
    }

    @Resource
    public void setWorkTaskCcManager(WorkTaskCcManager workTaskCcManager) {
        this.workTaskCcManager = workTaskCcManager;
    }

    @Resource
    public void setAccountInfoManager(AccountInfoManager accountInfoManager) {
        this.accountInfoManager = accountInfoManager;
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
    public void setWorkProjectTaskbindManager(WorkProjectTaskbindManager workProjectTaskbindManager) {
        this.workProjectTaskbindManager = workProjectTaskbindManager;
    }
}
