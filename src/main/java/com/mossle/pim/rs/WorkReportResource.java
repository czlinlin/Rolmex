package com.mossle.pim.rs;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.mossle.api.notification.NotificationConnector;
import com.mossle.api.pim.WorkReportConnector;
import com.mossle.api.tenant.TenantHolder;
import com.mossle.core.annotation.Log;
import com.mossle.core.auth.CurrentUserHolder;
import com.mossle.core.mapper.JsonMapper;
import com.mossle.core.query.PropertyFilter;
import com.mossle.core.util.BaseDTO;
import com.mossle.core.util.StringUtils;
import com.mossle.msg.MsgConstants;
import com.mossle.pim.persistence.domain.WorkReportCc;
import com.mossle.pim.persistence.domain.WorkReportCcPresetting;
import com.mossle.pim.persistence.domain.WorkReportCcPresettingNode;
import com.mossle.pim.persistence.domain.WorkReportForward;
import com.mossle.pim.persistence.domain.WorkReportInfo;
import com.mossle.pim.persistence.manager.WorkReportCcManager;
import com.mossle.pim.persistence.manager.WorkReportCcPresettingManager;
import com.mossle.pim.persistence.manager.WorkReportCcPresettingNodeManager;
import com.mossle.pim.persistence.manager.WorkReportForwardManager;
import com.mossle.pim.persistence.manager.WorkReportInfoManager;
import com.mossle.user.persistence.domain.AccountInfo;
import com.mossle.user.persistence.manager.AccountInfoManager;

@Component
@Path("pim")
public class WorkReportResource {
    private static Logger logger = LoggerFactory
            .getLogger(ScheduleResource.class);
    private WorkReportInfo workReportInfo;
    private WorkReportInfoManager workReportInfoManager;
    private JsonMapper jsonMapper = new JsonMapper();
    private CurrentUserHolder currentUserHolder;
    private WorkReportCcManager workReportCcManager;
    private AccountInfoManager accountInfoManager;
    private WorkReportForwardManager workReportForwardManager;
    private TenantHolder tenantHolder;
    private NotificationConnector notificationConnector;//发送消息
    @Autowired
    private WorkReportCcPresettingManager workReportCcPresettingManager;
    @Autowired
    private WorkReportCcPresettingNodeManager workReportCcPresettingNodeManager;
    @Autowired
    private WorkReportConnector workReportConnector;

    @POST
    @Path("work-report-feeback")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> save(@FormParam("id") Long id, @FormParam("content") String content) {
        Map<String, Object> mapList = new HashMap<String, Object>();
        try {
            if (StringUtils.isBlank(content)) {
                mapList.put("result", "error");
                mapList.put("content", "反馈内容不能为空");
                return mapList;
            }

            if (id == null || id < 1) {
                logger.debug("汇报反馈-获取参数id错误");
                mapList.put("result", "error");
                mapList.put("content", "获取参数错误");
                return mapList;
            }

            WorkReportInfo workReportInfoModel = workReportInfoManager.findUniqueBy("id", id);
            Long userId = Long.parseLong(currentUserHolder.getUserId());
            if (workReportInfoModel == null) {
                mapList.put("result", "error");
                mapList.put("content", "查询汇报信息错误");
                return mapList;
            }


            if (workReportInfoModel.getStatus().equals("0")) {
                mapList.put("result", "error");
                mapList.put("content", "未读汇报不能反馈");
                return mapList;
            } else if (workReportInfoModel.getStatus().equals("2")) {
                mapList.put("result", "error");
                mapList.put("content", "此汇报已经被反馈过了^_^");
                return mapList;
            } else if (!workReportInfoModel.getSendee().equals(userId)) {
                mapList.put("result", "error");
                mapList.put("content", "您不是此汇报的接收人");
                return mapList;
            }

            String encode = "utf-8";
            String feeBackConent = "";
            try {
                feeBackConent = java.net.URLDecoder.decode(content, encode);
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }//  java.net.URLDecoder.decode(content, "utf-8");
            if (feeBackConent.length() > 200) {
                mapList.put("result", "error");
                mapList.put("content", "请输入200字以内的内容");
                return mapList;
            }
            //赋值
            workReportInfoModel.setFeedback(feeBackConent);
            workReportInfoModel.setFeedbacktime(new Date());
            workReportInfoModel.setStatus("2");//已反馈

            workReportInfoManager.save(workReportInfoModel);
            mapList.put("result", "ok");
            mapList.put("content", "反馈提交成功");

        /*3)反馈完成后，须给汇报的发布人发送消息提醒，消息格式如下：
        a)标题：[汇报标题]汇报反馈提醒；
        b)内容：您的[汇报标题]汇报已反馈，请查看。*/
            String title = "[" + workReportInfoModel.getTitle() + "]汇报反馈提醒";
            String contentn = "您的[" +
                    workReportInfoModel.getTitle() + "]汇报已反馈，请查看。";
            String receiver = workReportInfoModel.getUserId().toString();
            String tenantId = tenantHolder.getTenantId();
            notificationConnector.send(
                    workReportInfoModel.getId().toString(),
                    tenantId,
                    currentUserHolder.getUserId().toString(),
                    receiver,
                    title,
                    contentn, MsgConstants.MSG_TYPE_REPORT);


        } catch (ArithmeticException e) {
            logger.error("汇报反馈异常：" + e.getMessage() + "\r\n" + e.fillInStackTrace());
        }
        return mapList;
    }

    @POST
    @Path("work-report-info-cc")
    @Produces(MediaType.APPLICATION_JSON)
    public BaseDTO ReportCCInfo(@FormParam("id") Long id) {
        BaseDTO result = new BaseDTO();
        //Map<String,Object> mapList=new HashMap<String,Object>();
        try {
            if (id == null || id < 1) {
                result.setCode(500);
                logger.debug("获取抄送人-获取参数id错误");
                result.setMessage("获取参数错误");
                return result;
            }

            List<WorkReportCc> ccList = workReportCcManager.findBy("workReportInfo.id", id);
            List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
            if (ccList != null && ccList.size() > 0) {
                for (WorkReportCc cc : ccList) {
                	String cc_type=cc.getCcType();
                    if(StringUtils.isBlank(cc_type))
                    	cc_type="1";
                    if(cc_type.equals("1")){
                    	Map<String, Object> map = new HashMap<String, Object>();
                        AccountInfo userInfo = accountInfoManager.findUniqueBy("id", cc.getCcno());
                        if (userInfo == null)
                            map.put("name", "");
                        else
                            map.put("name", userInfo.getDisplayName() == null ? "" : userInfo.getDisplayName());
                        map.put("status", cc.getStatus().equals("0") ? "未读" : "已读");
                        list.add(map);
                    }
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

    @POST
    @Path("work-report-info-realdel")
    @Produces(MediaType.APPLICATION_JSON)
    @Log(desc = "删除(物理)", action = "delete", operationDesc = "个人信息-工作中心-汇报-草稿汇报-删除(物理)操作")
    public BaseDTO TaskRealDel(@FormParam("id") Long id) {
        BaseDTO result = new BaseDTO();
        try {
            if (id == null || id < 1) {
                result.setCode(500);
                logger.debug("任务删除(物理)操作-获取参数id错误");
                result.setMessage("获取参数错误");
                return result;
            }

            WorkReportInfo reportModelInfo = workReportInfoManager.findUniqueBy("id", id);
            if (reportModelInfo == null) {
                result.setCode(500);
                logger.debug("任务删除(物理)操作-没有查询到汇报信息");
                result.setMessage("没有查询到此草稿汇报信息");
                return result;
            }

            List<WorkReportCc> ccList = workReportCcManager.findBy("workReportInfo.id", reportModelInfo.getId());
            if (!CollectionUtils.isEmpty(ccList)) {
                for (WorkReportCc cc : ccList) {
                    workReportCcManager.remove(cc);
                }
            }

            workReportCcManager.remove(reportModelInfo);
            result.setCode(200);
            result.setMessage("删除成功");
        } catch (ArithmeticException e) {
            result.setCode(500);
            result.setMessage("删除出错");
            logger.error("汇报删除(物理)操作-：" + e.getMessage() + "\r\n" + e.fillInStackTrace());
        }
        return result;
    }

    @POST
    @Path("work-report-info-drawback")
    @Produces(MediaType.APPLICATION_JSON)
    @Log(desc = "删除(物理)", action = "delete", operationDesc = "个人信息-工作中心-汇报-已发汇报-删除(物理)操作")
    public BaseDTO TaskDrawback(@FormParam("id") Long id) {
        BaseDTO result = new BaseDTO();
        try {
            if (id == null || id < 1) {
                result.setCode(500);
                logger.debug("汇报删除(物理)操作-获取参数id错误");
                result.setMessage("获取参数错误");
                return result;
            }

            WorkReportInfo reportModelInfo = workReportInfoManager.findUniqueBy("id", id);
            if (reportModelInfo == null) {
                result.setCode(500);
                logger.debug("任务删除(物理)操作-没有查询到汇报信息");
                result.setMessage("没有查询到此草稿汇报信息");
                return result;
            }
            if (!reportModelInfo.getStatus().equals("0")) {
                result.setCode(500);
                logger.debug("汇报删除(物理)操作-汇报状态已改变");
                result.setMessage("汇报状态已更改，请刷新后查看。");
                return result;
            }


            List<WorkReportCc> ccList = workReportCcManager.findBy("workReportInfo.id", reportModelInfo.getId());
            if (!CollectionUtils.isEmpty(ccList)) {
                for (WorkReportCc cc : ccList) {
                    workReportCcManager.remove(cc);
                }
            }

            workReportCcManager.remove(reportModelInfo);
            result.setCode(200);
            result.setMessage("删除成功");
        } catch (ArithmeticException e) {
            result.setCode(500);
            result.setMessage("删除出错");
            logger.error("汇报删除(物理)操作-：" + e.getMessage() + "\r\n" + e.fillInStackTrace());
        }
        return result;
    }

    @POST
    @Path("work-report-info-public")
    @Produces(MediaType.APPLICATION_JSON)
    @Log(desc = "发布", action = "update", operationDesc = "个人信息-工作中心-汇报-草稿汇报-发布操作")
    public BaseDTO TaskExec(@FormParam("id") Long id) {
        BaseDTO result = new BaseDTO();
        String tenantId = tenantHolder.getTenantId();
        try {
            if (id == null || id < 1) {
                result.setCode(500);
                logger.debug("任务执行操作-获取参数id错误");
                result.setMessage("获取参数错误");
                return result;
            }

            WorkReportInfo reportModel = workReportInfoManager.findUniqueBy("id", id);
            if (reportModel == null) {
                result.setCode(500);
                logger.debug("草稿汇报发布操作-没有查询到草稿信息");
                result.setMessage("没有查询到汇报信息");
                return result;
            } else if (!reportModel.getDatastatus().equals("0")) {
                result.setCode(500);
                result.setMessage("该汇报不是草稿状态");
                return result;
            }

            reportModel.setDatastatus("1");
            reportModel.setReportDate(new Date());

            workReportInfoManager.save(reportModel);
            String title = "[" + reportModel.getTitle() + "]汇报提醒";
            String content = "汇报人[" +
                    currentUserHolder.getName() + "]" +
                    "向您提交了" + "[" +
                    reportModel.getTitle() + "]汇报，请查看。";

            String receiver = reportModel.getSendee().toString();
            notificationConnector.send(
                    reportModel.getId().toString(),
                    tenantId,
                    currentUserHolder.getUserId().toString(),
                    receiver,
                    title,
                    content, MsgConstants.MSG_TYPE_REPORT);

            //新建发送消息给负责人和知会人
            List<WorkReportCc> workReportCcs = workReportCcManager.findBy("workReportInfo.id", id);

            if (workReportCcs != null && workReportCcs.size() > 0) {
                title = "[" + reportModel.getTitle() + "]" + "汇报提醒";
                content = "[" + currentUserHolder.getName() + "]" +
                        "给您抄送了" + "[" +
                        reportModel.getTitle() + "]汇报，请查看。";
                for (WorkReportCc cc : workReportCcs) {
                    receiver = cc.getCcno().toString();
                    notificationConnector.send(
                            reportModel.getId().toString(),
                            tenantId,
                            currentUserHolder.getUserId().toString(),
                            receiver,
                            title,
                            content, MsgConstants.MSG_TYPE_REPORT);
                }
            }

            result.setCode(200);
            result.setMessage("提交成功");
        } catch (ArithmeticException e) {
            result.setCode(500);
            result.setMessage("执行出错");
            logger.error("任务执行操作-：" + e.getMessage() + "\r\n" + e.fillInStackTrace());
        }
        return result;
    }

    @POST
    @Path("work-report-info-turnto")
    @Produces(MediaType.APPLICATION_JSON)
    public BaseDTO ReportTurnToInfo(@FormParam("id") Long id) {
        BaseDTO result = new BaseDTO();
        String tenantId = tenantHolder.getTenantId();
        //Map<String,Object> mapList=new HashMap<String,Object>();
        try {
            if (id == null || id < 1) {
                result.setCode(500);
                logger.debug("获取转发信息-获取参数id错误");
                result.setMessage("获取参数错误");
                return result;
            }
            WorkReportInfo workReportInfo = workReportInfoManager.findUniqueBy("id", id);

            String strUserId = currentUserHolder.getUserId();
            List<PropertyFilter> propertyFilters = new ArrayList<PropertyFilter>();
            propertyFilters.add(new PropertyFilter("EQL_forwarder", strUserId));
            propertyFilters.add(new PropertyFilter("EQL_workReportInfo.id", id.toString()));
            List<WorkReportForward> forwardList = workReportForwardManager.find(propertyFilters);

            List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
            if (!CollectionUtils.isEmpty(forwardList)) {
                for (WorkReportForward forward : forwardList) {
                    Map<String, Object> map = new HashMap<String, Object>();
                    AccountInfo userInfo = accountInfoManager.findUniqueBy("id", forward.getSendee());
                    if (userInfo == null)
                        map.put("name", "");
                    else
                        map.put("name", userInfo.getDisplayName() == null ? "" : userInfo.getDisplayName());
                    map.put("status", forward.getStatus().equals("0") ? "未读" : "已读");
                    map.put("remarks", StringUtils.isEmpty(forward.getRemarks()) ? "" : forward.getRemarks());
                    list.add(map);
                }
            }

            result.setCode(200);
            result.setData(list);
        /*3)转发后，须给转发接收人发送消息提醒，消息格式如下：
        a)标题：[汇报标题]汇报转发提醒；
        b)内容：[汇报转发人]将[汇报标题]汇报转发给您，请查看。*/
            String title = "[" + workReportInfo.getTitle() + "]汇报转发提醒";
            String content = "汇报转发人将[" +
                    workReportInfo.getTitle() + "]汇报转发给您，请查看。";
            String receiver = workReportInfo.getSendee().toString();
            notificationConnector.send(
                    workReportInfo.getId().toString(),
                    tenantId,
                    currentUserHolder.getUserId().toString(),
                    receiver,
                    title,
                    content, MsgConstants.MSG_TYPE_REPORT);

        } catch (ArithmeticException e) {
            result.setCode(500);
            result.setMessage("查询出错");
            logger.error("查询转发人异常：" + e.getMessage() + "\r\n" + e.fillInStackTrace());
        }
        return result;
    }

    @POST
    @Path("work-report-cc-presetting-remove")
    @Produces(MediaType.APPLICATION_JSON)
    @Log(desc = "删除条线设置", action = "delete", operationDesc = "系统设置-汇报条线-汇报条线管理-删除操作")
    public BaseDTO removeCCPreSetting(@FormParam("id") Long id){
    	BaseDTO result = new BaseDTO();
        try {
            if (id == null || id < 1) {
                result.setCode(500);
                logger.debug("获取删除条线设置信息-获取参数id错误");
                result.setMessage("获取参数错误");
                return result;
            }
            
            WorkReportCcPresetting workReportCcPresetting=workReportCcPresettingManager.get(id);
            if(workReportCcPresetting!=null){
            	workReportCcPresettingManager.remove(workReportCcPresetting);
            }
            
            List<WorkReportCcPresettingNode> ccPresettingNodeList=workReportCcPresettingNodeManager.findBy("presettingId", id);
            if(null!=ccPresettingNodeList&&ccPresettingNodeList.size()>0)
            	workReportCcPresettingNodeManager.removeAll(ccPresettingNodeList);
            
            result.setCode(200);
            result.setMessage("删除成功");
            return result;
            

        } catch (ArithmeticException e) {
            result.setCode(500);
            result.setMessage("查询出错");
            logger.error("查询转发人异常：" + e.getMessage() + "\r\n" + e.fillInStackTrace());
        }
        return result;
    }
    
    @GET
    @Path("work-report-getCCPresetting")
    @Produces(MediaType.APPLICATION_JSON)
    @Log(desc = "获取抄送信息", action = "search", operationDesc = "新建汇报-获取抄送信息（抄送上级/抄送条线）")
    public BaseDTO removeCCPreSetting(@QueryParam("positionId") String positionId){
    	BaseDTO result = new BaseDTO();
        try {        	
        	//String userId=currentUserHolder.getUserId();
        	if(StringUtils.isBlank(positionId)){
        		result.setCode(501);
                result.setMessage("参数错误");
        	}
        	
        	Map<String,Object> mapData=workReportConnector.getAreaInfo(positionId);
        	result.setCode(200);
        	result.setData(mapData);
            result.setMessage("ok");

        } catch (ArithmeticException e) {
            result.setCode(500);
            result.setMessage("查询出错");
            logger.error("查询转发人异常：" + e.getMessage() + "\r\n" + e.fillInStackTrace());
        }
        return result;
    }
    
    // ~ ======================================================================
    @Resource
    public void setWorkReportInfoManager(WorkReportInfoManager workReportInfoManager) {
        this.workReportInfoManager = workReportInfoManager;
    }

    @Resource
    public void setCurrentUserHolder(CurrentUserHolder currentUserHolder) {
        this.currentUserHolder = currentUserHolder;
    }

    @Resource
    public void setWorkReportCcManager(WorkReportCcManager workReportCcManager) {
        this.workReportCcManager = workReportCcManager;
    }

    @Resource
    public void setAccountInfoManager(AccountInfoManager accountInfoManager) {
        this.accountInfoManager = accountInfoManager;
    }

    @Resource
    public void setWorkReportForwardManager(WorkReportForwardManager workReportForwardManager) {
        this.workReportForwardManager = workReportForwardManager;
    }

    @Resource
    public void setTenantHolder(TenantHolder tenantHolder) {
        this.tenantHolder = tenantHolder;
    }

    @Resource
    public void setNotificationConnector(NotificationConnector notificationConnector) {
        this.notificationConnector = notificationConnector;
    }

}
