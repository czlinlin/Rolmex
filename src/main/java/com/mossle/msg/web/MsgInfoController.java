package com.mossle.msg.web;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.mossle.api.keyvalue.KeyValueConnector;
import com.mossle.api.keyvalue.Record;
import com.mossle.api.tenant.TenantHolder;
import com.mossle.api.user.UserConnector;
import com.mossle.api.user.UserDTO;
import com.mossle.cms.persistence.domain.CmsArticle;
import com.mossle.cms.persistence.manager.CmsArticleManager;
import com.mossle.core.auth.CurrentUserHolder;
import com.mossle.core.export.Exportor;
import com.mossle.core.export.TableModel;
import com.mossle.core.mapper.BeanMapper;
import com.mossle.core.page.Page;
import com.mossle.core.query.PropertyFilter;
import com.mossle.core.spring.MessageHelper;
import com.mossle.humantask.persistence.domain.TaskInfo;
import com.mossle.humantask.persistence.manager.TaskInfoManager;
import com.mossle.msg.persistence.domain.MsgInfo;
import com.mossle.msg.persistence.manager.MsgInfoManager;

import com.mossle.msg.persistence.manager.VMsgInfoManager;
import com.mossle.pim.persistence.domain.WorkReportCc;
import com.mossle.pim.persistence.domain.WorkReportForward;
import com.mossle.pim.persistence.domain.WorkReportInfo;
import com.mossle.pim.persistence.manager.WorkReportCcManager;
import com.mossle.pim.persistence.manager.WorkReportForwardManager;
import com.mossle.pim.persistence.manager.WorkReportInfoManager;
import com.mossle.project.persistence.domain.WorkProjectInfo;
import com.mossle.project.persistence.domain.WorkProjectNotify;
import com.mossle.project.persistence.manager.WorkProjectInfoManager;
import com.mossle.project.persistence.manager.WorkProjectNotifyManager;
import com.mossle.worktask.persistence.domain.WorkTaskCc;
import com.mossle.worktask.persistence.domain.WorkTaskInfo;
import com.mossle.worktask.persistence.manager.WorkTaskCcManager;
import com.mossle.worktask.persistence.manager.WorkTaskInfoManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("msg")
public class MsgInfoController {
    private static Logger logger = LoggerFactory
            .getLogger(MsgInfoController.class);
    private MsgInfoManager msgInfoManager;
    private Exportor exportor;
    private BeanMapper beanMapper = new BeanMapper();
    private UserConnector userConnector;
    private MessageHelper messageHelper;
    private CurrentUserHolder currentUserHolder;
    private TenantHolder tenantHolder;
    private WorkProjectInfoManager workProjectInfoManager;
    private WorkTaskInfoManager workTaskInfoManager;
    private WorkReportInfoManager workReportInfoManager;
    private CmsArticleManager cmsArticleManager;
    private WorkTaskCcManager workTaskCcManager;
    private WorkProjectNotifyManager workProjectNotifyManager;
    private WorkReportCcManager workReportCcManager;
    private WorkReportForwardManager workReportForwardManager;
    private VMsgInfoManager vMsgInfoManager;
    @Resource
    private TaskInfoManager taskInfoManager;
    @Resource
    private KeyValueConnector keyValueConnector;

    @RequestMapping("msg-info-list")
    public String list(@ModelAttribute Page page,
                       @RequestParam Map<String, Object> parameterMap, Model model) {
        String userId = currentUserHolder.getUserId();
        List<PropertyFilter> propertyFilters = PropertyFilter
                .buildFromMap(parameterMap);
        propertyFilters.add(new PropertyFilter("EQS_senderId", userId));
        page = msgInfoManager.pagedQuery(page, propertyFilters);

        model.addAttribute("page", page);

        return "msg/msg-info-list";
    }

    // 未读消息列表
    @RequestMapping("msg-info-listReceived")
    public String listReceived(@ModelAttribute Page page,
                               @RequestParam Map<String, Object> parameterMap, Model model) {

        String userId = currentUserHolder.getUserId();
        List<PropertyFilter> propertyFilters = PropertyFilter.buildFromMap(parameterMap);
        propertyFilters.add(new PropertyFilter("EQS_receiverId", userId));
        propertyFilters.add(new PropertyFilter("EQI_status", "0"));

        page.setOrder("DESC");
        page.setOrderBy("createTime");
        page = vMsgInfoManager.pagedQuery(page, propertyFilters);

        model.addAttribute("page", page);
        model.addAttribute("userId", userId);

        return "msg/msg-info-listReceived";
    }


    //已读消息列表
    @RequestMapping("msg-info-listRead")
    public String listRead(@ModelAttribute Page page,
                           @RequestParam Map<String, Object> parameterMap, Model model) {

        String userId = currentUserHolder.getUserId();
        List<PropertyFilter> propertyFilters = PropertyFilter.buildFromMap(parameterMap);
        propertyFilters.add(new PropertyFilter("EQS_receiverId", userId));
        propertyFilters.add(new PropertyFilter("EQI_status", "1"));

        page.setOrder("DESC");
        page.setOrderBy("createTime");
        page = vMsgInfoManager.pagedQuery(page, propertyFilters);

        model.addAttribute("page", page);

        return "msg/msg-info-listRead";
    }

    @RequestMapping("msg-info-listSent")
    public String listSent(@ModelAttribute Page page,
                           @RequestParam Map<String, Object> parameterMap, Model model) {

        String userId = currentUserHolder.getUserId();
        List<PropertyFilter> propertyFilters = PropertyFilter
                .buildFromMap(parameterMap);

        propertyFilters.add(new PropertyFilter("EQS_senderId", userId));
        page.setOrder("DESC");
        page.setOrderBy("createTime");
        page = msgInfoManager.pagedQuery(page, propertyFilters);

        model.addAttribute("page", page);

        return "msg/msg-info-listSent";
    }

    @RequestMapping("msg-info-input")
    public String input(@RequestParam(value = "id", required = false) Long id,
                        Model model) {
        if (id != null) {
            MsgInfo msgInfo = msgInfoManager.get(id);

            model.addAttribute("model", msgInfo);
        }

        return "msg/msg-info-input";
    }

    @RequestMapping("msg-info-save")
    public String save(@ModelAttribute MsgInfo msgInfo,
                       @RequestParam("username") String username,
                       RedirectAttributes redirectAttributes) {

        String userId = currentUserHolder.getUserId();
        String tenantId = tenantHolder.getTenantId();

        MsgInfo dest = null;
        Long id = msgInfo.getId();

        if (id != null) {
            dest = msgInfoManager.get(id);
            beanMapper.copy(msgInfo, dest);
            msgInfoManager.save(dest);
        } else {
            dest = msgInfo;
            dest.setSenderId(userId);

            for (String theUsername : username.split(",")) {
                MsgInfo theMsgInfo = new MsgInfo();
                beanMapper.copy(msgInfo, theMsgInfo);
                theMsgInfo.setSenderId(userId);

                /*
                UserDTO userDto = userConnector
                        .findByUsername(theUsername, "1");

                if (userDto == null) {
                    logger.warn("user not exists : {}", theUsername);

                    continue;
                }

                theMsgInfo.setReceiverId(userDto.getId());
                */

                theMsgInfo.setReceiverId(theUsername);
                theMsgInfo.setCreateTime(new Date());
                theMsgInfo.setStatus(0);
                theMsgInfo.setTenantId(tenantId);
                msgInfoManager.save(theMsgInfo);
            }
        }

        messageHelper.addFlashMessage(redirectAttributes, "core.success.save",
                "保存成功");

        return "redirect:/msg/msg-info-listSent.do";
    }

    @RequestMapping("msg-info-remove")
    public String remove(@RequestParam("selectedItem") List<Long> selectedItem,
                         RedirectAttributes redirectAttributes) {
        List<MsgInfo> msgInfos = msgInfoManager.findByIds(selectedItem);

        msgInfoManager.removeAll(msgInfos);
        messageHelper.addFlashMessage(redirectAttributes,
                "core.success.delete", "删除成功");

        return "redirect:/msg/msg-info-list.do";
    }

    @RequestMapping("msg-info-export")
    public void export(@ModelAttribute Page page,
                       @RequestParam Map<String, Object> parameterMap,
                       HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        List<PropertyFilter> propertyFilters = PropertyFilter
                .buildFromMap(parameterMap);
        page = msgInfoManager.pagedQuery(page, propertyFilters);

        List<MsgInfo> msgInfos = (List<MsgInfo>) page.getResult();

        TableModel tableModel = new TableModel();
        tableModel.setName("msg info");
        tableModel.addHeaders("id", "name");
        tableModel.setData(msgInfos);
        exportor.export(request, response, tableModel);
    }

    //查看已读消息详情
    @RequestMapping("msg-info-readView")
    public String readView(@RequestParam("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        String redirectUrl = "redirect:/msg/msg-info-listRead.do";
        //消息对应详情是否已经被删除
        String datastatus = "";
        //消息关于删除显示
        String viewStatus = "";//2:已删除无法查看
        MsgInfo msgInfo = msgInfoManager.get(id);
        msgInfo.setStatus(1);
        String changeStatus = "";//0:可以查看详情 1：当前登录人不是负责人或抄送/知会人，无法查看详情
        String leader = "";
        String publisher = "";
        String toId = msgInfo.getData();
        Integer viewType = msgInfo.getMsgType();
        if (viewType == 1) {
            //项目 2-删除
            WorkProjectInfo workProjectInfo = workProjectInfoManager.findUniqueBy("id", Long.parseLong(toId));
            if (workProjectInfo != null) {
                datastatus = workProjectInfo.getDatastatus();
                leader = workProjectInfo.getLeader().toString();
                publisher = workProjectInfo.getPublisher().toString();
                List<WorkProjectNotify> workProjectNotifyList = workProjectNotifyManager.findBy("workProjectInfo.id", Long.parseLong(toId));
                if (datastatus.equals("2")) {
                    viewStatus = "2";
                } else {
                    if (currentUserHolder.getUserId().equals(leader) || currentUserHolder.getUserId().equals(publisher)) {
                        changeStatus = "0";
                    } else {
                        changeStatus = "1";
                        for (WorkProjectNotify workProjectNotify : workProjectNotifyList) {
                            if (currentUserHolder.getUserId().equals(workProjectNotify.getUserid().toString())) {
                                changeStatus = "0";
                            }
                        }
                    }
                }
            } else {
                logger.debug("查询项目明细-没有查询到项目信息");
                messageHelper.addFlashMessage(redirectAttributes, "core.success.save", "无法查看具体信息");
                return redirectUrl;
            }
        } else if (viewType == 2) {
            //任务 2-负责人删除、3-发布人删除、4-删除
            WorkTaskInfo workTaskInfo = workTaskInfoManager.findUniqueBy("id", Long.parseLong(toId));
            if (workTaskInfo != null) {
                datastatus = workTaskInfo.getDatastatus();
                leader = workTaskInfo.getLeader().toString();
                publisher = workTaskInfo.getPublisher().toString();
                List<WorkTaskCc> workTaskCcList = workTaskCcManager.findBy("workTaskInfo.id", Long.parseLong(toId));

                if (datastatus.equals("2") || datastatus.equals("3") || datastatus.equals("4")) {
                    viewStatus = "2";
                } else {
                    if (currentUserHolder.getUserId().equals(leader) || currentUserHolder.getUserId().equals(publisher)) {
                        changeStatus = "0";
                    } else {
                        changeStatus = "1";
                        for (WorkTaskCc cc : workTaskCcList) {
                            if (currentUserHolder.getUserId().equals(cc.getCcno().toString())) {
                                changeStatus = "0";
                            }
                        }
                    }
                }
            } else {
                logger.debug("查询任务明细-没有查询到任务信息");
                messageHelper.addFlashMessage(redirectAttributes, "core.success.save", "无法查看具体信息");
                return redirectUrl;
            }
        } else if (viewType == 4) {
            //汇报 2-删除
            WorkReportInfo workReportInfo = workReportInfoManager.findUniqueBy("id", Long.parseLong(toId));
            if (workReportInfo != null) {
                datastatus = workReportInfo.getDatastatus();
                leader = workReportInfo.getSendee().toString();
                publisher = workReportInfo.getUserId().toString();
                List<WorkReportCc> workReportCcList = workReportCcManager.findBy("workReportInfo.id", Long.parseLong(toId));
                List<WorkReportForward> workReportForwardList = workReportForwardManager.findBy("workReportInfo.id", Long.parseLong(toId));
                if (datastatus.equals("2")) {
                    viewStatus = "2";
                } else {
                    if (currentUserHolder.getUserId().equals(leader) || currentUserHolder.getUserId().equals(publisher)) {
                        changeStatus = "0";
                    } else {
                        changeStatus = "1";
                        for (WorkReportCc workReportCc : workReportCcList) {
                            if (currentUserHolder.getUserId().equals(workReportCc.getCcno().toString())) {
                                changeStatus = "0";
                            }
                        }
                        for (WorkReportForward workReportForward : workReportForwardList) {
                            if (currentUserHolder.getUserId().equals(workReportForward.getSendee().toString())) {
                                changeStatus = "0";
                            }
                        }
                    }
                }
            } else {
                logger.debug("查询汇报明细-没有查询到汇报信息");
                messageHelper.addFlashMessage(redirectAttributes, "core.success.save", "无法查看具体信息");
                return redirectUrl;
            }
        } else if (viewType == 5) {
            //公告 2-删除
            CmsArticle cmsArticle = cmsArticleManager.findUniqueBy("id", Long.parseLong(toId));
            if (cmsArticle != null) {
                if (cmsArticle.getStartTime() == null) {

                } else {
                    //公告时效性判断
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
                    String st = cmsArticle.getStartTime().toString();
                    String et = cmsArticle.getEndTime().toString();
                    String nowt = df.format(new Date());

                    if (st != null && et != null) {
                        if (nowt.compareTo(st) < 0 || et.compareTo(nowt) < 0) {
                            viewStatus = "2";
                        }
                    }
                }


                datastatus = cmsArticle.getStatus().toString();
                if (datastatus.equals("2")) {
                    viewStatus = "2";
                } else {
                    changeStatus = "0";
                }
            } else {
                logger.debug("查询公告明细-没有查询到公告信息");
                messageHelper.addFlashMessage(redirectAttributes, "core.success.save", "无法查看具体信息");
                return redirectUrl;
            }
        }/*else if(viewType == 0){
            //对于审核未正常通过的申请，抄送人不得从消息入口进行查看
            msgInfo = msgInfoManager.findUniqueBy("id", id);
            TaskInfo taskInfo = taskInfoManager.findUniqueBy("id", Long.parseLong(msgInfo.getData()));
            if(taskInfo != null){
            	if(taskInfo.getCatalog().equals("copy")){//如果被点击的消息是抄送消息
                	msgInfo.setType(1);
                }
                Record record = keyValueConnector.findByBusinessKey(taskInfo.getBusinessKey());
                model.addAttribute("record", record);
            }
        }*/
        msgInfoManager.save(msgInfo);
        model.addAttribute("viewStatus", viewStatus);
        model.addAttribute("model", msgInfo);
        model.addAttribute("changeStatus", changeStatus);

        return "msg/msg-info-view";
    }

    //查看未读消息详情
    @RequestMapping("msg-info-view")
    public String view(@RequestParam("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        String redirectUrl = "redirect:/msg/msg-info-listReceived.do";
        //消息对应详情是否已经被删除
        String datastatus = "";
        //消息关于删除显示
        String viewStatus = "";//2:已删除无法查看
        MsgInfo msgInfo = msgInfoManager.get(id);
        msgInfo.setStatus(1);
        String changeStatus = "";//0:可以查看详情 1：当前登录人不是负责人或抄送/知会人，无法查看详情
        String leader = "";
        String publisher = "";
        String toId = msgInfo.getData();
        Integer viewType = msgInfo.getMsgType();
        if (viewType == 1) {
            //项目 2-删除
            WorkProjectInfo workProjectInfo = workProjectInfoManager.findUniqueBy("id", Long.parseLong(toId));
            if (workProjectInfo != null) {
                datastatus = workProjectInfo.getDatastatus();
                leader = workProjectInfo.getLeader().toString();
                publisher = workProjectInfo.getPublisher().toString();
                List<WorkProjectNotify> workProjectNotifyList = workProjectNotifyManager.findBy("workProjectInfo.id", Long.parseLong(toId));
                if (datastatus.equals("2")) {
                    viewStatus = "2";
                } else {
                    if (currentUserHolder.getUserId().equals(leader) || currentUserHolder.getUserId().equals(publisher)) {
                        changeStatus = "0";
                    } else {
                        changeStatus = "1";
                        for (WorkProjectNotify workProjectNotify : workProjectNotifyList) {
                            if (currentUserHolder.getUserId().equals(workProjectNotify.getUserid().toString())) {
                                changeStatus = "0";
                            }
                        }
                    }
                }
            } else {
                logger.debug("查询项目明细-没有查询到项目信息");
                messageHelper.addFlashMessage(redirectAttributes, "core.success.save", "无法查看具体信息");
                return redirectUrl;
            }
        } else if (viewType == 2) {
            //任务 2-负责人删除、3-发布人删除、4-删除
            WorkTaskInfo workTaskInfo = workTaskInfoManager.findUniqueBy("id", Long.parseLong(toId));
            if (workTaskInfo != null) {
                datastatus = workTaskInfo.getDatastatus();
                leader = workTaskInfo.getLeader().toString();
                publisher = workTaskInfo.getPublisher().toString();
                List<WorkTaskCc> workTaskCcList = workTaskCcManager.findBy("workTaskInfo.id", Long.parseLong(toId));

                if (datastatus.equals("2") || datastatus.equals("3") || datastatus.equals("4")) {
                    viewStatus = "2";
                } else {
                    if (currentUserHolder.getUserId().equals(leader) || currentUserHolder.getUserId().equals(publisher)) {
                        changeStatus = "0";
                    } else {
                        changeStatus = "1";
                        for (WorkTaskCc cc : workTaskCcList) {
                            if (currentUserHolder.getUserId().equals(cc.getCcno().toString())) {
                                changeStatus = "0";
                            }
                        }
                    }
                }
            } else {
                logger.debug("查询任务明细-没有查询到任务信息");
                messageHelper.addFlashMessage(redirectAttributes, "core.success.save", "无法查看具体信息");
                return redirectUrl;
            }
        } else if (viewType == 4) {
            //汇报 2-删除
            WorkReportInfo workReportInfo = workReportInfoManager.findUniqueBy("id", Long.parseLong(toId));
            if (workReportInfo != null) {
                datastatus = workReportInfo.getDatastatus();
                leader = workReportInfo.getSendee().toString();
                publisher = workReportInfo.getUserId().toString();
                List<WorkReportCc> workReportCcList = workReportCcManager.findBy("workReportInfo.id", Long.parseLong(toId));
                List<WorkReportForward> workReportForwardList = workReportForwardManager.findBy("workReportInfo.id", Long.parseLong(toId));
                if (datastatus.equals("2")) {
                    viewStatus = "2";
                } else {
                    if (currentUserHolder.getUserId().equals(leader) || currentUserHolder.getUserId().equals(publisher)) {
                        changeStatus = "0";
                    } else {
                        changeStatus = "1";
                        for (WorkReportCc workReportCc : workReportCcList) {
                            if (currentUserHolder.getUserId().equals(workReportCc.getCcno().toString())) {
                                changeStatus = "0";
                            }
                        }
                        for (WorkReportForward workReportForward : workReportForwardList) {
                            if (currentUserHolder.getUserId().equals(workReportForward.getSendee().toString())) {
                                changeStatus = "0";
                            }
                        }
                    }
                }
            } else {
                logger.debug("查询汇报明细-没有查询到汇报信息");
                messageHelper.addFlashMessage(redirectAttributes, "core.success.save", "无法查看具体信息");
                return redirectUrl;
            }
        } else if (viewType == 5) {
            //公告 2-删除
            CmsArticle cmsArticle = cmsArticleManager.findUniqueBy("id", Long.parseLong(toId));
            if (cmsArticle != null) {
                //公告时效性判断
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
                String st = cmsArticle.getStartTime().toString();
                String et = cmsArticle.getEndTime().toString();
                String nowt = df.format(new Date());

                if (st != null && et != null) {
                    if (nowt.compareTo(st) < 0 || et.compareTo(nowt) < 0) {
                        viewStatus = "2";
                    }
                }

                datastatus = cmsArticle.getStatus().toString();
                if (datastatus.equals("2")) {
                    viewStatus = "2";
                } else {
                    changeStatus = "0";
                }
            } else {
                logger.debug("查询公告明细-没有查询到公告信息");
                messageHelper.addFlashMessage(redirectAttributes, "core.success.save", "无法查看具体信息");
                return redirectUrl;
            }
        }/*else if(viewType == 0){
            //对于审核未正常通过的申请，抄送人不得从消息入口进行查看
            msgInfo = msgInfoManager.findUniqueBy("id", id);
            TaskInfo taskInfo = taskInfoManager.findUniqueBy("id", Long.parseLong(msgInfo.getData()));
            if(taskInfo != null){
            	if(taskInfo.getCatalog().equals("copy")){//如果被点击的消息是抄送消息
                	msgInfo.setType(1);
                }
                Record record = keyValueConnector.findByBusinessKey(taskInfo.getBusinessKey());
                model.addAttribute("record", record);
            }
        }
        */
        msgInfoManager.save(msgInfo);
        model.addAttribute("viewStatus", viewStatus);
        model.addAttribute("model", msgInfo);
        model.addAttribute("changeStatus", changeStatus);

        return "msg/msg-info-view";
    }

    //消息中查看具体详情
    @RequestMapping("msg-info-toview")
    public String toView(@ModelAttribute MsgInfo msgInfo) {
        String toView = msgInfo.getData();
        Integer viewType = msgInfo.getMsgType();
        String tempUrl = "";
        //0:流程 1:项目  2:任务  3:议题   4:汇报   5:公告
        if (viewType == 1) {
            //项目详情
            tempUrl = "redirect:/project/work-project-info-detail.do?id=" + toView;
        } else if (viewType == 2) {
            //任务详情
            tempUrl = "redirect:/worktask/work-task-info-detail.do?id=" + toView;
        } else if (viewType == 4) {
            //汇报详情
            tempUrl = "redirect:/pim/work-report-info-look.do?id=" + toView;
        } else if (viewType == 5) {
            //公告详情
            tempUrl = "redirect:/cms/cms-article-meview.do?id=" + toView;
        }
        return tempUrl;
    }

    // ~ ======================================================================
    @Resource
    public void setMsgInfoManager(MsgInfoManager msgInfoManager) {
        this.msgInfoManager = msgInfoManager;
    }

    @Resource
    public void setExportor(Exportor exportor) {
        this.exportor = exportor;
    }

    @Resource
    public void setUserConnector(UserConnector userConnector) {
        this.userConnector = userConnector;
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
    public void setWorkProjectInfoManager(WorkProjectInfoManager workProjectInfoManager) {
        this.workProjectInfoManager = workProjectInfoManager;
    }

    @Resource
    public void setWorkTaskInfoManager(WorkTaskInfoManager workTaskInfoManager) {
        this.workTaskInfoManager = workTaskInfoManager;
    }

    @Resource
    public void setWorkReportInfoManager(WorkReportInfoManager workReportInfoManager) {
        this.workReportInfoManager = workReportInfoManager;
    }

    @Resource
    public void setCmsArticleManager(CmsArticleManager cmsArticleManager) {
        this.cmsArticleManager = cmsArticleManager;
    }

    @Resource
    public void setWorkTaskCcManager(WorkTaskCcManager workTaskCcManager) {
        this.workTaskCcManager = workTaskCcManager;
    }

    @Resource
    public void setWorkProjectNotifyManager(WorkProjectNotifyManager workProjectNotifyManager) {
        this.workProjectNotifyManager = workProjectNotifyManager;
    }

    @Resource
    public void setWorkReportCcManager(WorkReportCcManager workReportCcManager) {
        this.workReportCcManager = workReportCcManager;
    }

    @Resource
    public void setvMsgInfoManager(VMsgInfoManager vMsgInfoManager) {
        this.vMsgInfoManager = vMsgInfoManager;
    }

    @Resource
    public void setWorkReportForwardManager(WorkReportForwardManager workReportForwardManager) {
        this.workReportForwardManager = workReportForwardManager;
    }

    @Resource
    public void setTenantHolder(TenantHolder tenantHolder) {
        this.tenantHolder = tenantHolder;
    }
}
