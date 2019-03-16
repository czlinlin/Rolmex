package com.mossle.pim.web;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import javax.print.DocFlavor.STRING;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.FormParam;

import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.crypto.modes.CCMBlockCipher;
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
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.google.common.base.Joiner;
import com.mossle.api.keyvalue.KeyValueConnector;
import com.mossle.api.notification.NotificationConnector;
import com.mossle.api.org.OrgConnector;
import com.mossle.api.party.PartyConnector;
import com.mossle.api.pim.WorkReportConnector;
import com.mossle.api.tenant.TenantHolder;
import com.mossle.api.user.UserConnector;
import com.mossle.common.utils.DeEnCode;
//import com.mossle.common.utils.ExportExcel;
import com.mossle.common.utils.FileUploadAPI;
import com.mossle.common.utils.WebAPI;
import com.mossle.core.annotation.Log;
import com.mossle.core.auth.CurrentUserHolder;
import com.mossle.core.id.IdGenerator;
import com.mossle.core.mapper.BeanMapper;
import com.mossle.core.page.Page;
import com.mossle.core.query.PropertyFilter;
import com.mossle.core.spring.MessageHelper;
import com.mossle.internal.store.persistence.domain.StoreInfo;
import com.mossle.msg.MsgConstants;
import com.mossle.party.PartyConstants;
import com.mossle.party.persistence.domain.PartyEntity;
import com.mossle.party.persistence.manager.PartyEntityManager;
import com.mossle.party.support.PartyOrgConnector;
import com.mossle.pim.persistence.domain.WorkReportAttachment;
import com.mossle.pim.persistence.domain.WorkReportCc;
import com.mossle.pim.persistence.domain.WorkReportCcPresetting;
import com.mossle.pim.persistence.domain.WorkReportCcPresettingNode;
import com.mossle.pim.persistence.domain.WorkReportForward;
import com.mossle.pim.persistence.domain.WorkReportInfo;
import com.mossle.pim.persistence.manager.WorkReportAttachmentManager;
import com.mossle.pim.persistence.manager.WorkReportCcManager;
import com.mossle.pim.persistence.manager.WorkReportCcPresettingManager;
import com.mossle.pim.persistence.manager.WorkReportCcPresettingNodeManager;
import com.mossle.pim.persistence.manager.WorkReportForwardManager;
import com.mossle.pim.persistence.manager.WorkReportInfoManager;
import com.mossle.user.persistence.domain.AccountInfo;
import com.mossle.user.persistence.manager.AccountInfoManager;
import com.mossle.util.ExportUtil;

@Controller
@RequestMapping("pim")
public class WorkReportInfoController implements ServletContextAware {
    private static Logger logger = LoggerFactory.getLogger(WorkReportInfoController.class);
    private WorkReportInfoManager workReportInfoManager;
    private WorkReportAttachmentManager workReportAttachmentManager;
    private WorkReportCcManager workReportCcManager;
    private MessageHelper messageHelper;
    private BeanMapper beanMapper = new BeanMapper();
    private CurrentUserHolder currentUserHolder;
    private TenantHolder tenantHolder;
    private UserConnector userConnector;
    private WorkReportForwardManager workReportForwardManager;
    private AccountInfoManager accountInfoManager;
    private NotificationConnector notificationConnector;//发送消息
    private FileUploadAPI fileUploadAPI;
    private WebAPI webAPI;
    private WorkReportCcPresettingManager workReportCcPresettingManager;
    private WorkReportCcPresettingNodeManager workReportCcPresettingNodeManager;
    @Autowired
    private IdGenerator idGenerator;
    @Autowired
    private PartyOrgConnector partyOrgConnector;
    @Autowired
    private WorkReportConnector workReportConnector;
    @Autowired
    private PartyEntityManager partyEntityManager;
    
    @Autowired
    private PartyConnector partyConnector;
    
    @Resource
    private JdbcTemplate jdbcTemplate;
    private SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
    @Autowired
    private KeyValueConnector keyValueConnector;

    @Autowired
    public void setUserConnector(UserConnector userConnector) {
        this.userConnector = userConnector;
    }

    // 草稿汇报 Bing 2017.8.17
    @RequestMapping("work-report-info-temp")
    @Log(desc = "草稿汇报", action = "Search", operationDesc = "个人信息-工作中心-汇报-草稿任务-查询")
    public String temp(@ModelAttribute Page page, @RequestParam Map<String, Object> parameterMap, Model model) {
        String userId = currentUserHolder.getUserId();
        List<PropertyFilter> propertyFilters = PropertyFilter.buildFromMap(parameterMap);
        propertyFilters.add(new PropertyFilter("EQL_userId", userId));
        propertyFilters.add(new PropertyFilter("EQS_datastatus", "0"));// 0=草稿
        page.setDefaultOrder("reportDate", page.DESC);
        page = workReportInfoManager.pagedQuery(page, propertyFilters);
        model.addAttribute("page", page);

        return "pim/work-report-info-temp";
    }

    //转发给我 wanghan 2017.08.24
    @RequestMapping("work-report-info-forwardtome")
    @Log(desc = "转发给我查询", action = "Search", operationDesc = "个人信息-工作中心-汇报-转发给我-查询")
    public String forwardToMe(Model model, @ModelAttribute Page page, @RequestParam Map<String, Object> parameterMap) {
        String userId = currentUserHolder.getUserId();
        List<PropertyFilter> propertyFilters = PropertyFilter.buildFromMap(parameterMap);
        propertyFilters.add(new PropertyFilter("EQS_workReportInfo.datastatus", "1"));

        page = workReportForwardManager.pagedQuery(page, userId.toString(), propertyFilters);
        model.addAttribute("page", page);
        return "pim/work-report-info-forwardtome";
    }
    //转发给我 sjx 2018.11.8
    @RequestMapping("work-report-info-forwardtome-export")
    @Log(desc = "转发给我导出", action = "Search", operationDesc = "个人信息-工作中心-汇报-转发给我-导出")
    public void forwardToMeExport(@ModelAttribute Page page, @RequestParam Map<String, Object> parameterMap,
    								HttpServletRequest request,HttpServletResponse response) {
    	String userId = currentUserHolder.getUserId();
    	List<PropertyFilter> propertyFilters = PropertyFilter.buildFromMap(parameterMap);
    	propertyFilters.add(new PropertyFilter("EQS_workReportInfo.datastatus", "1"));
    	//先获取数据条数，将其赋值给pageSize
    	String countSql = "select count(*) from work_report_forward where sendee="+userId;
    	int count = jdbcTemplate.queryForInt(countSql);
    	page.setPageSize(count);
    	page = workReportForwardManager.pagedQuery(page, userId.toString(), propertyFilters);
    	List<WorkReportForward> WorkReportInfos = (List<WorkReportForward>) page.getResult();
    	List<Map<String,Object>> list = new ArrayList<>();
    	for(WorkReportForward forwarInfo : WorkReportInfos){
    		Map<String,Object> map = new HashMap<>();
    		WorkReportInfo info = forwarInfo.getWorkReportInfo();
    		map.put("dealing", info.getDealing());
    		map.put("completed", info.getCompleted());
    		map.put("coordinate", info.getCoordinate());
    		if(StringUtils.isNotBlank(info.getDealing())){
    			String dealing = ExportUtil.LostHtml(info.getDealing());
    			map.put("dealing", dealing);
    		}
    		if(StringUtils.isNotBlank(info.getCompleted())){
    			String completed = ExportUtil.LostHtml(info.getCompleted());
    			map.put("completed", completed);
    		}
    		if(StringUtils.isNotBlank(info.getCoordinate())){
    			String coordinate = ExportUtil.LostHtml(info.getCoordinate());
    			map.put("coordinate", coordinate);
    		}
    		map.put("id", info.getId());
    		map.put("title", info.getTitle());
    		map.put("type", info.getType());
    		
    		
    		map.put("status", forwarInfo.getStatus());
    		map.put("feedback", info.getFeedback());
    		map.put("feedbacktime", info.getFeedbacktime());
    		map.put("remarks", info.getRemarks());
    		map.put("sendee", info.getSendee());
    		map.put("user_id", info.getUserId());
    		list.add(map);
    	}
    	if (list.size() == 0) {
            String title = "暂无数据需要导出！";
            StringBuffer sb = new StringBuffer();
            sb.append("<script language='javascript'>alert('");
            sb.append(title);
            sb.append("');history.go(-1);</script>");
            try {
                response.setContentType("text/html; charset=utf-8");
                /*String strHtml = "<body style='background-color: #D0D0D0'>";
                strHtml += "</body>";*/
                //response.getWriter().println((strHtml));
                response.getWriter().println(sb.toString());
                response.getWriter().flush();
                response.getWriter().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else{
        	String fileName = String.valueOf(parameterMap.get("reportCatalog")) + formatter.format(new Date()) + ".xls";
        	String []headers = {"标题","汇报类型","进行中工作","已完成工作","需协调工作","状态","反馈内容","反馈时间","备注","汇报人","接收人","附件","抄送人"};
        	String []fieldNames = {"title","type","dealing","completed","coordinate","status","feedback","feedbacktime","remarks","user_id","sendee","storeName","ccNames"};
        	try {
    			ExportUtil.exportReport(list, response, request,fileName,headers,fieldNames,userConnector,jdbcTemplate);
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
        }
    }

    // 转发汇报详情 wanghan 2017.08.25
    @RequestMapping("work-report-info-forwardview")
    @Log(desc = "转发详情", action = "Search", operationDesc = "个人信息-工作中心-汇报-转发详情")
    public String ccViewReportInfo(@RequestParam(value = "id", required = true) Long id, Model model) throws Exception {
        Long userId = Long.parseLong(currentUserHolder.getUserId());
        WorkReportInfo workReportInfo = workReportInfoManager.get(id);
        List<WorkReportForward> workReportForwardList = workReportForwardManager.findBy("workReportInfo.id", id);
        List<WorkReportCc> workReportCcList = workReportCcManager.findBy("workReportInfo.id", id);
        List<WorkReportAttachment> workReportAttachmentList = workReportAttachmentManager.findBy("workReportInfo.id",
                id);
        // 查询附件
        List<StoreInfo> list = fileUploadAPI.getStore("OA/report", Long.toString(workReportInfo.getId()));
        model.addAttribute("StoreInfos", list);
        String ccnos = "";
        String ccnames = "";
        if (!CollectionUtils.isEmpty(workReportCcList)) {
            for (WorkReportCc cc : workReportCcList) {
                ccnos = ccnos + cc.getCcno() + ",";
                ccnames = ccnames + userConnector.findById(cc.getCcno().toString()).getDisplayName() + ",";
            }
        }
        String forwarders = "";
        String sendees = "";
        if (!CollectionUtils.isEmpty(workReportForwardList)) {
            for (WorkReportForward wf : workReportForwardList) {
                forwarders = userConnector.findById(wf.getForwarder().toString()).getDisplayName();
                sendees = sendees + userConnector.findById(wf.getSendee().toString()).getDisplayName() + ",";
                if (workReportInfo.getId().equals(id)) {
                    wf.setStatus("1");
                    workReportForwardManager.save(wf);
                }
            }
        }

        model.addAttribute("model", workReportInfo);
        model.addAttribute("ccnos", ccnos);
        model.addAttribute("ccnames", ccnames);
        model.addAttribute("forwarders", forwarders);
        model.addAttribute("sendees", sendees);

        return "pim/work-report-info-forwardview";
    }

    // 新建汇报 wanghan 2017.08.15
    @RequestMapping("work-report-info-input")
    @Log(desc = "新建汇报页面初始化", action = "Search", operationDesc = "个人信息-工作中心-汇报-新建汇报页面-查询")
    public String input(@RequestParam(value = "id", required = false) Long id, Model model) {
    	//add by lilei at 2018.12.12
    	String userId=currentUserHolder.getUserId();
    	Map<String,Object> positionMap=workReportConnector.getReportPositionInfo(userId);    	
    	model.addAttribute("positionInfo", positionMap);
    	model.addAttribute("ccPresetting",workReportConnector.getReportPreSetting());
        return "pim/work-report-info-input";
    }

    // 修改汇报
    @RequestMapping("work-report-info-modify")
    @Log(desc = "修改汇报页面初始化", action = "Search", operationDesc = "个人信息-工作中心-汇报-修改汇报页面-查询")
    public String modify(@RequestParam(value = "id", required = false) Long id, Model model) throws Exception {
        if (id != null) {
            WorkReportInfo workReportInfo = workReportInfoManager.get(id);
            workReportInfoManager.save(workReportInfo);
            List<WorkReportCc> workReportCcList = workReportCcManager.findBy("workReportInfo.id", id);
            // 查询附件
            model.addAttribute("picUrl", webAPI.getViewUrl());
            List<StoreInfo> list = fileUploadAPI.getStore("OA/report", Long.toString(workReportInfo.getId()));
            model.addAttribute("StoreInfos", list);
            String ccnos = "";
            String ccnames = "";
            if (!CollectionUtils.isEmpty(workReportCcList)) {
                for (WorkReportCc cc : workReportCcList) {
                    String cc_type=cc.getCcType();
                    if(StringUtils.isBlank(cc_type))
                    	cc_type="1";
                    if(cc.getCcType().equals("1")){
                    	ccnos +=cc.getCcno() + ",";
                    	ccnames +=userConnector.findById(cc.getCcno().toString()).getDisplayName() + ",";
                    }
                }
                if(!ccnos.equals(""))
                	ccnos = ccnos.substring(0, ccnos.length() - 1);
                if(!ccnames.equals(""))
                	ccnames = ccnames.substring(0, ccnames.length() - 1);
            }
            model.addAttribute("model", workReportInfo);
            model.addAttribute("ccnos", ccnos);
            model.addAttribute("ccnames", ccnames);
            
            List<Map<String,Object>> mapReportAttrList=jdbcTemplate
            									.queryForList("select * from  work_report_info_attr where id=?",workReportInfo.getId());
            if(mapReportAttrList.size()>0){
            	model.addAttribute("reportAttr",mapReportAttrList.get(0));
            }
            model.addAttribute("ccPresetting",workReportConnector.getReportPreSetting());
        }
        return "pim/work-report-info-modify";
    }

    // 保存汇报 wanghan 2017.08.15
    @RequestMapping("work-report-info-save")
    @Log(desc = "汇报保存", action = "insert,update", operationDesc = "个人信息-工作中心-汇报-新建/修改汇报保存")
    public String save(@ModelAttribute WorkReportInfo workReportInfo,
                       @RequestParam(value = "files", required = false) MultipartFile[] files,
                       @RequestParam(value = "iptdels", required = false) String iptdels,
                       RedirectAttributes redirectAttributes,
                       HttpServletRequest request, String ccnos,
                       @RequestParam(value = "iptStartPosition", required = false) String iptStartPosition,
                       @RequestParam(value = "preSettingId", required = false) Long preSettingId) throws Exception {
        String userId = currentUserHolder.getUserId();
        String tenantId = tenantHolder.getTenantId();
        WorkReportInfo workReportInfoDest;
        Long id = workReportInfo.getId();
        String editType = "1";//1：新建,2:修改无变更接收人，3：修改变更接收人
        Long oldSendee = workReportInfo.getSendee();
        // 更新
        if (id != null) {
            workReportInfoDest = workReportInfoManager.get(id);

            //接收人改变
            if (!workReportInfoDest.getSendee().equals(oldSendee))
                editType = "3";
            else
                editType = "2";
            if ("4".equals(workReportInfo.getType())) {
                workReportInfo.setCoordinate("");
                workReportInfo.setRemarks("");
                //workReportInfo.setDealing("");
                workReportInfo.setCompleted("");
            } else {
                workReportInfo.setProblems("");
            }
            workReportInfo.setLastedittime(new Date());
            beanMapper.copy(workReportInfo, workReportInfoDest);
            workReportInfoDest.setUserId(Long.parseLong(userId));
            workReportInfoDest.setReportDate(new Date());
            workReportInfoDest.setStatus("0");// 0:未读状态            
        } else {
            workReportInfoDest = workReportInfo;
            workReportInfoDest.setUserId(Long.parseLong(userId));
            workReportInfoDest.setReportDate(new Date());
            workReportInfoDest.setStatus("0");// 0:未读状态
        }
        workReportInfoManager.save(workReportInfoDest);

        if (iptdels != null && !iptdels.equals("")) {
            fileUploadAPI.uploadFileDel(iptdels, Long.toString(workReportInfoDest.getId()));
        }

        fileUploadAPI.uploadFile(files, tenantId, Long.toString(workReportInfoDest.getId()), "OA/report");


        // 保存抄送信息
        if (id != null) {
            List<WorkReportCc> workReportCcList = workReportCcManager.findBy("workReportInfo.id", id);
            if (!CollectionUtils.isEmpty(workReportCcList)) {
                workReportCcManager.removeAll(workReportCcList);
            }
        }
        
        String strCCDatas="";	//用于抄送消息，add by lilei at 2018.12.18
        strCCDatas=ccnos;
        //ADD BY LILEI AT 2018.12.19 处理上级/条线抄送
    	if(!StringUtils.isBlank(iptStartPosition)){
    		if(preSettingId==null) preSettingId=0L;
    		
    		//非大区人员没有条线
    		PartyEntity areaAartyEntity=partyOrgConnector.findPartyAreaByUserId(iptStartPosition);
    		if(areaAartyEntity==null) preSettingId=0L;
    		
        	String position_type="1";
        	PartyEntity partyEntity=partyEntityManager.get(Long.parseLong(iptStartPosition));
        	if(partyEntity.getPartyType().getId()==PartyConstants.PARTY_TYPE_POST)
        		position_type="2";
        	String strSql="delete from work_report_info_attr where id=%s";
        	keyValueConnector.updateBySql(String.format(strSql,workReportInfoDest.getId()));
        	strSql="insert into work_report_info_attr(id,positionId,position_type,ccPreSettingId,note)"
        			+ " values(%s,%s,'%s','%s','%s')";
        	keyValueConnector.updateBySql(String.format(strSql,workReportInfoDest.getId(),iptStartPosition,position_type,preSettingId,""));
        	String strPresettingCCNos=workReportConnector.getReportPresettingccNos(iptStartPosition, preSettingId);
        	if(!com.mossle.core.util.StringUtils.isBlank(strPresettingCCNos)){
        		/*if(com.mossle.core.util.StringUtils.isBlank(ccnos))
        			ccnos=strPresettingCCNos;
        		else 
        			ccnos+=","+strPresettingCCNos;*/
        		String[] split_data = strPresettingCCNos.split(",");
        		for (int i = 0; i < split_data.length; i++) {
                    WorkReportCc workReportCc = new WorkReportCc();
                    String reportleader = workReportInfo.getSendee().toString();
                    if (!(reportleader.equals(split_data[i]))) {
                        workReportCc.setStatus("0");
                        workReportCc.setCcno(Long.parseLong(split_data[i]));
                        workReportCc.setWorkReportInfo(workReportInfoDest);
                        workReportCc.setCcType("2");	//走上级/条线的抄送 add by lilei at 2018.12.19
                        workReportCcManager.save(workReportCc);
                    }
                }
        	}
    	}
        if (ccnos != null && !ccnos.equals("")) {
            String[] split_data = ccnos.split(",");
            for (int i = 0; i < split_data.length; i++) {
                WorkReportCc workReportCc = new WorkReportCc();
                String reportleader = workReportInfo.getSendee().toString();
                if (!(reportleader.equals(split_data[i]))) {
                    workReportCc.setStatus("0");
                    workReportCc.setCcno(Long.parseLong(split_data[i]));
                    workReportCc.setWorkReportInfo(workReportInfoDest);
                    workReportCc.setCcType("1");	//走自主选择的抄送  add by lilei at 2018.12.19
                    workReportCcManager.save(workReportCc);
                }
            }
        }

        if (workReportInfoDest.getDatastatus().equals("1")) {
            /*
             * editType:1.新建汇报发送
	         * editType:3.修改变更接收人发送
	         * */
            /*a)标题：[汇报标题]汇报提醒；
                b)内容：[汇报人姓名]向您提交了[汇报标题]汇报，请查看。*/
            if (editType.equals("1") || editType.equals("3") || editType.equals("2")) {
                String title = "[" + workReportInfoDest.getTitle() + "]汇报提醒";
                String content = "汇报人[" +
                        currentUserHolder.getName() + "]" +
                        "向您提交了" + "[" +
                        workReportInfoDest.getTitle() + "]汇报，请查看。";

                String receiver = workReportInfoDest.getSendee().toString();

                /*if (editType == "3") {
                    title = workReportInfoDest.getTitle() + "变更提醒";
                    content = workReportInfoDest.getTitle() + "由汇报人变更了接收人";
                    receiver = oldSendee.toString();
                }*/

                notificationConnector.send(
                        workReportInfoDest.getId().toString(),
                        tenantId,
                        currentUserHolder.getUserId().toString(),
                        receiver,
                        title,
                        content, MsgConstants.MSG_TYPE_REPORT);

                //新建发送消息给抄送人
                /*a)标题：[汇报标题]汇报提醒；
                    b)内容：[汇报人姓名]给您抄送了[汇报标题]汇报，请查看。*/
                if (editType == "1") {
                    //List<WorkReportCc> ccList = workReportCcManager.findBy("workReportInfo.id", workReportInfoDest.getId());
                    //if (ccList != null && ccList.size() > 0) {
                	if (strCCDatas != null && !strCCDatas.equals("")) {
                        String[] split_data = strCCDatas.split(",");
                        title = "[" + workReportInfoDest.getTitle() + "]" + "汇报提醒";
                        content = "[" + currentUserHolder.getName() + "]" +
                                "给您抄送了" + "[" +
                                workReportInfoDest.getTitle() + "]汇报，请查看。";
                        for (String ccNo : split_data) {
                            receiver = ccNo;
                            notificationConnector.send(
                                    workReportInfoDest.getId().toString(),
                                    tenantId,
                                    currentUserHolder.getUserId().toString(),
                                    receiver,
                                    title,
                                    content, MsgConstants.MSG_TYPE_REPORT);
                        }
                    }
                }
            }
        }

        if (workReportInfoDest.getDatastatus().equals("0")) {
            messageHelper.addFlashMessage(redirectAttributes, "core.success.save", "保存草稿成功");
            return "redirect:/pim/work-report-info-temp.do";
        } else
            messageHelper.addFlashMessage(redirectAttributes, "core.success.save", "提交成功");
        return "redirect:/pim/work-report-info-list.do";

    }

    // 查看汇报 wanghan 2017.08.18
    @RequestMapping("work-report-info-look")
    @Log(desc = "汇报详情-look", action = "search", operationDesc = "个人信息-工作中心-汇报-汇报详情")
    public String lookReportInfo(@ModelAttribute Page page, @RequestParam Map<String, Object> parameterMap, @RequestParam(value = "id", required = true) Long id, Model model) throws Exception {
        
    	Long userId = Long.parseLong(currentUserHolder.getUserId());
    	
        WorkReportInfo workReportInfo = workReportInfoManager.get(id);
        //查看汇报人为汇报接收人，status为已读
        if (userId.equals(workReportInfo.getSendee())
                && !workReportInfo.getStatus().equals("1")
                && !workReportInfo.getStatus().equals("2")) {
            workReportInfo.setStatus("1");
            workReportInfoManager.save(workReportInfo);
        }

        List<PartyEntity> postPartyEntityList=partyOrgConnector.getPostByUserId(currentUserHolder.getUserId());
        List<String> postIdList=new ArrayList<String>();
        postIdList.add(currentUserHolder.getUserId());
        for (PartyEntity postPartyEntity : postPartyEntityList) {
        	postIdList.add(postPartyEntity.getId().toString());
		}
        String strPostId=Joiner.on(",").join(postIdList);
        
        // 抄送阅读
        List<PropertyFilter> propertyFilters = new ArrayList<PropertyFilter>();
        propertyFilters.add(new PropertyFilter("INL_ccno", strPostId));
        //propertyFilters.add(new PropertyFilter("EQS_ccType", "1"));
        //抄送阅读
        /*List<PropertyFilter> propertyFilters = new ArrayList<PropertyFilter>();
        propertyFilters.add(new PropertyFilter("EQL_ccno", currentUserHolder.getUserId()));*/
        propertyFilters.add(new PropertyFilter("EQL_workReportInfo.id", id.toString()));
        propertyFilters.add(new PropertyFilter("EQS_status", "0"));
        List<WorkReportCc> ccList = workReportCcManager.find(propertyFilters);
        if (!CollectionUtils.isEmpty(ccList)) {
        	for (WorkReportCc workReportCc : ccList) {
        		 //WorkReportCc cc = ccList.get(0);
        		 workReportCc.setStatus("1");
                 workReportCcManager.save(workReportCc);
			}
           
        }

        //转发阅读
        List<PropertyFilter> turnPropertyFilters = new ArrayList<PropertyFilter>();
        turnPropertyFilters.add(new PropertyFilter("EQL_sendee", currentUserHolder.getUserId()));
        turnPropertyFilters.add(new PropertyFilter("EQL_workReportInfo.id", id.toString()));
        turnPropertyFilters.add(new PropertyFilter("EQS_status", "0"));
        List<WorkReportForward> forwardList = workReportForwardManager.find(turnPropertyFilters);
        if (!CollectionUtils.isEmpty(forwardList)) {
            WorkReportForward forward = forwardList.get(0);
            forward.setStatus("1");
            workReportForwardManager.save(forward);
        }
        // 查询附件
        List<StoreInfo> list = fileUploadAPI.getStore("OA/report", Long.toString(workReportInfo.getId()));
        model.addAttribute("StoreInfos", list);
        String ccnos = "";
        String ccnames = "";
        List<WorkReportCc> workReportCcList = workReportCcManager.findBy("workReportInfo.id", id);
        if (!CollectionUtils.isEmpty(workReportCcList)) {
            for (WorkReportCc cc : workReportCcList) {
            	ccnos +=cc.getCcno() + ",";
            	String cc_type=cc.getCcType();
                if(StringUtils.isBlank(cc_type))
                	cc_type="1";
                //cc.getCcType()
                if("1".equals(cc_type)){
                	if(null==cc.getCcno())
                		continue;
                	
                	PartyEntity partyEntity=partyEntityManager.get(cc.getCcno());
                	if(null!=partyEntity)
                		ccnames +=partyEntity.getName() + ",";
                }
                if(!StringUtils.isBlank(ccnames)){
                	ccnames=ccnames.substring(0, ccnames.length()-1);
                }
                /*else {
                	if(null==cc.getCcno())
                		continue;
                	
                	PartyEntity partyEntity=partyEntityManager.get(cc.getCcno());
                	if(null!=partyEntity)
                		ccnames +=partyEntity.getName() + ",";
				}*/
            }
        }
        //转发给我的小列表详情
        List<WorkReportForward> workReportForwards = workReportForwardManager.forwardPaths(id, userId);
        model.addAttribute("workReportForwards", workReportForwards);
        //转发至的人详情
        List<WorkReportForward> workReportSendees = workReportForwardManager.sendeePaths(id, userId);
        model.addAttribute("workReportSendees", workReportSendees);

        model.addAttribute("model", workReportInfo);
        model.addAttribute("ccnos", ccnos);
        model.addAttribute("ccnames", ccnames);
        model.addAttribute("picUrl", webAPI.getViewUrl());

        return "pim/work-report-info-look";
    }

    @RequestMapping("work-report-info-cctome")
    @Log(desc = "抄送给我列表", action = "search", operationDesc = "个人信息-工作中心-汇报-抄送给我")
    public String ccToMe_i(Model model, @ModelAttribute Page page, @RequestParam Map<String, Object> parameterMap) {
    	String userId=currentUserHolder.getUserId();
    	List<String> partyIdList=partyConnector.getUpperCompanyAndLowerDepartment(userId);
    	model.addAttribute("partyIdList", partyIdList);
    	return "pim/work-report-info-cctome";
    }
    //抄送给我 wanghan 2017.08.21
    @RequestMapping("work-report-info-cctome-i")
    @Log(desc = "抄送给我列表", action = "search", operationDesc = "个人信息-工作中心-汇报-抄送给我")
    public String ccToMe(Model model, @ModelAttribute Page page, @RequestParam Map<String, Object> parameterMap) {
        Long userId = Long.parseLong(currentUserHolder.getUserId());
        List<PropertyFilter> propertyFilters = PropertyFilter.buildFromMap(parameterMap);
        propertyFilters.add(new PropertyFilter("EQS_datastatus", "1"));
        
        List<PartyEntity> postPartyEntityList=partyOrgConnector.getPostByUserId(currentUserHolder.getUserId());
        List<Long> postIdList=new ArrayList<Long>();
        for (PartyEntity postPartyEntity : postPartyEntityList) {
        	postIdList.add(postPartyEntity.getId());
		}
        String strPostId=Joiner.on(",").join(postIdList);

        page = workReportInfoManager.pagedQueryByCc(page, userId.toString(),strPostId, propertyFilters);
        List<WorkReportInfo> workReportInfos = (List<WorkReportInfo>) page.getResult();
        postIdList.add(userId);
        for (WorkReportInfo info : workReportInfos) {
            Set<WorkReportCc> ccs = info.getWorkReportCcs();
            for (WorkReportCc cc : ccs) {
                if (postIdList.contains(cc.getCcno())) {
                	info.setShowCcType(cc.getCcType());
                    info.setShowCcStatus(cc.getStatus());
                }
            }
        }
        
        model.addAttribute("ccPresetting",workReportConnector.getReportPreSetting());
        
        model.addAttribute("page", page);
        return "pim/work-report-info-cctome-i";
    }
    
    @RequestMapping("work-report-info-cctome-export")
    @Log(desc = "抄送给我列表", action = "search", operationDesc = "个人信息-工作中心-汇报-抄送给我-导出")
    public void ccToMeExport(Model model, @ModelAttribute Page page, @RequestParam Map<String, Object> parameterMap,
    		                 HttpServletRequest request,HttpServletResponse response) {
    	Long userId = Long.parseLong(currentUserHolder.getUserId());
    	List<PropertyFilter> propertyFilters = PropertyFilter.buildFromMap(parameterMap);
    	propertyFilters.add(new PropertyFilter("EQS_datastatus", "1"));
    	
    	List<PartyEntity> postPartyEntityList=partyOrgConnector.getPostByUserId(currentUserHolder.getUserId());
        List<Long> postIdList=new ArrayList<Long>();
        for (PartyEntity postPartyEntity : postPartyEntityList) {
        	postIdList.add(postPartyEntity.getId());
		}
        String strPostId=Joiner.on(",").join(postIdList);
        
    	//先获取数据条数，将其赋值给pageSize
    	String countSql = String.format("select count(*) from work_report_cc where ccno=%s or ccno in(%s)",userId,strPostId);
    	int count = jdbcTemplate.queryForInt(countSql);
    	page.setPageSize(count);
    	
    	page = workReportInfoManager.pagedQueryByCc(page, userId.toString(),strPostId, propertyFilters);
    	List<WorkReportInfo> workReportInfos = (List<WorkReportInfo>) page.getResult();
    	List<Map<String,Object>> list = new ArrayList<>();
    	for (WorkReportInfo info : workReportInfos) {
    		Map<String,Object> map = new HashMap<>();
    		Set<WorkReportCc> ccs = info.getWorkReportCcs();
            for (WorkReportCc cc : ccs) {
                if (postIdList.contains(cc.getCcno())||cc.getCcno().equals(userId)) {
                    info.setShowCcStatus(cc.getStatus());
                }
            }
    		map.put("id", info.getId());
    		map.put("title", info.getTitle());
    		map.put("type", info.getType());
    		map.put("dealing", info.getDealing());
    		map.put("completed", info.getCompleted());
    		map.put("coordinate", info.getCoordinate());
    		map.put("status", info.getStatus());
    		map.put("feedback", info.getFeedback());
    		map.put("feedbacktime", info.getFeedbacktime());
    		map.put("remarks", info.getRemarks());
    		map.put("sendee", info.getSendee());
    		map.put("user_id", info.getUserId());
    		if(StringUtils.isNotBlank(info.getDealing())){
    			String dealing = ExportUtil.LostHtml(info.getDealing());
    			map.put("dealing", dealing);
    		}
    		if(StringUtils.isNotBlank(info.getCoordinate())){
    			String coordinate = ExportUtil.LostHtml(info.getCoordinate());
    			map.put("coordinate", coordinate);
    		}
    		if(StringUtils.isNotBlank(info.getCompleted())){
    			String completed = ExportUtil.LostHtml(info.getCompleted());
    			map.put("completed", completed);
    		}
    		list.add(map);
    	}
    	if (list.size() == 0) {
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
                response.getWriter().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else{
        	String fileName = String.valueOf(parameterMap.get("reportCatalog")) + formatter.format(new Date()) + ".xls";
        	String []headers = {"标题","汇报类型","进行中工作","已完成工作","需协调工作","状态","反馈内容","反馈时间","备注","汇报人","接收人","附件","抄送人"};
        	String []fieldNames = {"title","type","dealing","completed","coordinate","status","feedback","feedbacktime","remarks","user_id","sendee","storeName","ccNames"};
        	try {
    			ExportUtil.exportReport(list, response, request,fileName,headers,fieldNames,userConnector,jdbcTemplate);
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
        }
    }


    /*已发汇报  by lilei at 2017-08-16*/
    @RequestMapping("work-report-info-list")
    @Log(desc = "已发汇报列表", action = "search", operationDesc = "个人信息-工作中心-汇报-已发汇报")
    public String sendReportInfoList(
            @ModelAttribute Page page,
            @RequestParam Map<String, Object> parameterMap,
            Model model) {
        //将parameterMap中的参数 build成PropertyFilter
        List<PropertyFilter> propertyFilters = PropertyFilter.buildFromMap(parameterMap);
        String userId = currentUserHolder.getUserId();
        propertyFilters.add(new PropertyFilter("EQL_userId", userId));
        propertyFilters.add(new PropertyFilter("EQS_datastatus", "1"));
        page.setDefaultOrder("reportDate", page.DESC);
        page = workReportInfoManager.pagedQuery(page, propertyFilters);
        model.addAttribute("page", page);
        return "/pim/work-report-info-list";
    }
    /*汇报  by sjx at 2018-11-06  已发和接收汇报通用导出方法*/
    @RequestMapping("work-report-info-list-export")
    @Log(desc = "汇报列表", action = "export", operationDesc = "个人信息-工作中心-汇报-导出")
    public void reportInfoListExport(@RequestParam Map<String, Object> parameterMap,
    									HttpServletRequest request,HttpServletResponse response) {
    	String userId = currentUserHolder.getUserId();
    	String sql = "";
    	if("已发汇报".equals(String.valueOf(parameterMap.get("reportCatalog")))){
    		sql = "select * from work_report_info where user_id="+userId+" and datastatus=1";
    	}else if("接收汇报".equals(String.valueOf(parameterMap.get("reportCatalog")))){
    		sql = "select * from work_report_info where sendee="+userId+" and datastatus=1";
    	}
    	
    	StringBuffer buff = new StringBuffer();
    	for(Map.Entry<String, Object> entry : parameterMap.entrySet()){
    		String key = entry.getKey();
    		String val = (String) entry.getValue();
    		if(StringUtils.isEmpty(val))
    			continue;
    		if("filter_LIKES_title".equals(key)){
    			key = "title";
    			buff.append(" and "+key+" like '%"+val+"%'");
    		}else if("filter_EQS_type".equals(key)){
    			key = "type";
    			buff.append(" and "+key+" ="+val);
    		}else if("filter_EQS_status".equals(key)){
	    		key = "status";
	    		buff.append(" and "+key+" ="+val);
	    	}else if("filter_GED_reportDate".equals(key)){
	    		key = "report_date";
	    		buff.append(" and "+key+" >='"+val+"'");
	    	}else if("filter_LED_reportDate".equals(key)){
	    		key = "report_date";
	    		buff.append(" and "+key+" <='"+val+"'");
	    	}
    	}
    	sql = buff.insert(0, sql).toString()+" order by report_date desc";
    	List<Map<String,Object>> list = jdbcTemplate.queryForList(sql);
    	//去掉个别字段的html标签
    	for(Map<String,Object> map : list){
    		for(Map.Entry<String, Object> entry : map.entrySet()){
    			if("completed".equals(String.valueOf(entry.getKey())) && StringUtils.isNotBlank(entry.getValue().toString())){
    				String completed = ExportUtil.LostHtml(entry.getValue().toString());
    				map.put("completed", completed);
    			}
    			if("dealing".equals(String.valueOf(entry.getKey())) && StringUtils.isNotBlank(entry.getValue().toString())){
    				String dealing = ExportUtil.LostHtml(entry.getValue().toString());
    				map.put("dealing", dealing);
    			}
    			if("coordinate".equals(String.valueOf(entry.getKey())) && StringUtils.isNotBlank(entry.getValue().toString())){
    				String coordinate = ExportUtil.LostHtml(entry.getValue().toString());
    				map.put("coordinate", coordinate);
    			}
    		}
    	}
    	if (list.size() == 0) {
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
                response.getWriter().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else{
        	String fileName = String.valueOf(parameterMap.get("reportCatalog")) + formatter.format(new Date()) + ".xls";
        	String []headers = {"标题","汇报类型","进行中工作","已完成工作","需协调工作","状态","反馈内容","反馈时间","备注","汇报人","接收人","附件","抄送人"};
        	String []fieldNames = {"title","type","dealing","completed","coordinate","status","feedback","feedbacktime","remarks","user_id","sendee","storeName","ccNames"};
        	try {
    			ExportUtil.exportReport(list, response, request,fileName,headers,fieldNames,userConnector,jdbcTemplate);
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
        }
    }
    
    /*接收汇报  by lilei at 2017-08-16*/
    @RequestMapping("work-report-tome-list")
    @Log(desc = "接收汇报列表", action = "search", operationDesc = "个人信息-工作中心-汇报-接收汇报")
    public String ReportTomeList(
            @ModelAttribute Page page,
            @RequestParam Map<String, Object> parameterMap,
            Model model) {
        List<PropertyFilter> propertyFilters = PropertyFilter.buildFromMap(parameterMap);

        String userId = currentUserHolder.getUserId();
        propertyFilters.add(new PropertyFilter("EQL_sendee", userId));
        propertyFilters.add(new PropertyFilter("EQS_datastatus", "1"));
        page.setDefaultOrder("reportDate", page.DESC);
        page = workReportInfoManager.pagedQuery(page, propertyFilters);
        model.addAttribute("page", page);
        return "/pim/work-report-tome-list";
    }

    /*转发初始化页面 by lilei at 2017-08-21*/
    @RequestMapping("work-report-info-turn")
    @Log(desc = "转发初始化页面", action = "search", operationDesc = "个人信息-工作中心-汇报-转发初始化页面")
    public String Turn(@RequestParam(value = "id", required = true) Long
                               id, @RequestParam(value = "type", required = true) String turntype, Model model, RedirectAttributes
                               redirectAttributes) {
        String strReturnRedirect = "redirect:/pim/work-report-info-list.do";
        if (turntype.equals("1"))
            strReturnRedirect = "redirect:/pim/work-report-tome-list.do";
        else if (turntype.equals("2"))
            strReturnRedirect = "redirect:/pim/work-report-info-cctome.do";
        else if (turntype.equals("3"))
            strReturnRedirect = "redirect:/pim/work-report-info-forwardtome.do";
        else if (turntype.equals("4"))
            strReturnRedirect = "redirect:/pim/work-report-info-list.do";

        String strUserId = currentUserHolder.getUserId();
        WorkReportInfo reportModel = workReportInfoManager.findUniqueBy("id", id);
        if (reportModel == null) {
            messageHelper.addFlashMessage(redirectAttributes, "core.success.save", "查询不到汇报数据");
            return strReturnRedirect;
        }
        String sendeename = userConnector.findById(reportModel.getSendee().toString()).getDisplayName();
        //接收汇报转发
        if (turntype.equals("1")) {
            if (!reportModel.getSendee().equals(Long.parseLong(strUserId))) {
                messageHelper.addFlashMessage(redirectAttributes, "core.success.save", "此汇报不是你的接收汇报");
                return strReturnRedirect;
            }
        }

        //抄送汇报转发
        if (turntype.equals("2")) {
        	List<PartyEntity> postPartyEntityList=partyOrgConnector.getPostByUserId(currentUserHolder.getUserId());
            List<String> postIdList=new ArrayList<String>();
            postIdList.add(currentUserHolder.getUserId());
            for (PartyEntity postPartyEntity : postPartyEntityList) {
            	postIdList.add(postPartyEntity.getId().toString());
    		}
            String strPostId=Joiner.on(",").join(postIdList);
            
            List<PropertyFilter> propertyFilters = new ArrayList<PropertyFilter>();
            propertyFilters.add(new PropertyFilter("INL_ccno", strPostId));
            propertyFilters.add(new PropertyFilter("EQL_workReportInfo.id", id.toString()));
            
            List<WorkReportCc> ccList = workReportCcManager.find(propertyFilters);
            if (ccList == null || ccList.size() < 1) {
                messageHelper.addFlashMessage(redirectAttributes, "core.success.save", "查询不到抄送信息");
                return strReturnRedirect;
            }
        }

        //转发汇报转发
        if (turntype.equals("3")) {
            List<PropertyFilter> propertyFilters = new ArrayList<PropertyFilter>();
            propertyFilters.add(new PropertyFilter("EQL_sendee", strUserId));
            propertyFilters.add(new PropertyFilter("EQL_workReportInfo.id", id.toString()));
            
            List<WorkReportForward> forwardList = workReportForwardManager.find(propertyFilters);
            if (forwardList == null || forwardList.size() < 1) {
                messageHelper.addFlashMessage(redirectAttributes, "core.success.save", "查询不到此汇报的转发给您的信息");
                return strReturnRedirect;
            }
        }

        //已发汇报转发
        if (turntype.equals("4")) {
            if (!reportModel.getUserId().equals(Long.parseLong(strUserId))) {
                messageHelper.addFlashMessage(redirectAttributes, "core.success.save", "此汇报不是您发布的信息");
                return strReturnRedirect;
            }
        }


        //else if(!reportModel.getSendee().equals(id))
        //{
        //messageHelper.addFlashMessage(redirectAttributes, "core.success.save","没有权限转发");
        //return "redirect:/pim/work-report-info-list.do";
        //}
        model.addAttribute("sendeename", sendeename);
        model.addAttribute("id", id);
        model.addAttribute("turntype", turntype);
        model.addAttribute("model", reportModel);
        return "/pim/work-report-info-turn";
    }

    /*转发保存页面 by lilei at 2017-08-21
     * turntype：（1：接收汇报，2：抄送汇报，3：转发汇报，4：已发汇报）
     *
     * */
    @RequestMapping("work-report-info-turnsave")
    @Log(desc = "转发保存页面", action = "update", operationDesc = "个人信息-工作中心-汇报-转发保存")
    public String TurnSave(@FormParam("reportid") Long reportid, @FormParam("turntype") String
            turntype, @FormParam("remarks") String remarks,
            @FormParam("isFeedBackForward") String isFeedBackForward, @FormParam("selectIds") String selectIds, RedirectAttributes
                                   redirectAttributes) {
        String strReturnRedirect = "redirect:/pim/work-report-info-list.do";
        if (turntype.equals("1"))
            strReturnRedirect = "redirect:/pim/work-report-tome-list.do";
        else if (turntype.equals("2"))
            strReturnRedirect = "redirect:/pim/work-report-info-cctome.do";
        else if (turntype.equals("3"))
            strReturnRedirect = "redirect:/pim/work-report-info-forwardtome.do";
        else if (turntype.equals("4"))
            strReturnRedirect = "redirect:/pim/work-report-info-list.do";

        try {
            String strUserId = currentUserHolder.getUserId();
            WorkReportInfo reportModel = workReportInfoManager.findUniqueBy("id", reportid);
            if (reportModel == null) {
                messageHelper.addFlashMessage(redirectAttributes, "core.success.save", "查询不到汇报数据");
                return strReturnRedirect;
            }
            //接收汇报转发
            if (turntype.equals("1")) {
                if (!reportModel.getSendee().equals(Long.parseLong(strUserId))) {
                    messageHelper.addFlashMessage(redirectAttributes, "core.success.save", "此汇报不是你的接收汇报");
                    return strReturnRedirect;
                }
            }

            //抄送汇报转发
            if (turntype.equals("2")) {
            	List<PartyEntity> postPartyEntityList=partyOrgConnector.getPostByUserId(currentUserHolder.getUserId());
                List<String> postIdList=new ArrayList<String>();
                postIdList.add(currentUserHolder.getUserId());
                for (PartyEntity postPartyEntity : postPartyEntityList) {
                	postIdList.add(postPartyEntity.getId().toString());
        		}
                String strPostId=Joiner.on(",").join(postIdList);
                
                List<PropertyFilter> propertyFilters = new ArrayList<PropertyFilter>();
                propertyFilters.add(new PropertyFilter("INL_ccno", strPostId));
                propertyFilters.add(new PropertyFilter("EQL_workReportInfo.id", reportid.toString()));

                List<WorkReportCc> ccList = workReportCcManager.find(propertyFilters);
                if (ccList == null || ccList.size() < 1) {
                    messageHelper.addFlashMessage(redirectAttributes, "core.success.save", "查询不到抄送信息");
                    return strReturnRedirect;
                }
            }

            //转发汇报转发
            if (turntype.equals("3")) {
                List<PropertyFilter> propertyFilters = new ArrayList<PropertyFilter>();
                propertyFilters.add(new PropertyFilter("EQL_sendee", strUserId));
                propertyFilters.add(new PropertyFilter("EQL_workReportInfo.id", reportid.toString()));

                List<WorkReportForward> forwardList = workReportForwardManager.find(propertyFilters);
                if (forwardList == null || forwardList.size() < 1) {
                    messageHelper.addFlashMessage(redirectAttributes, "core.success.save", "查询不到此汇报的转发给您的信息");
                    return strReturnRedirect;
                }
            }

            //判断是否已转发(一个汇报只能被转发一次）
            StringBuilder existsName = new StringBuilder();
            WorkReportInfo workReportInfo = workReportInfoManager.findUniqueBy("id", reportid);
            Long reportPublisher = workReportInfo.getUserId();
            String publisherName = userConnector.findById(reportPublisher.toString()).getDisplayName();
            if (selectIds.contains(reportPublisher.toString())) {
                messageHelper.addFlashMessage(redirectAttributes,
                        "core.success.save",
                        publisherName + "是汇报发送人，不能转发");
                return strReturnRedirect;
            }
            List<WorkReportForward> workReportForwardList = workReportForwardManager.findBy("workReportInfo.id", reportid);
            for (WorkReportForward workReportForward : workReportForwardList) {
                if (selectIds.contains(workReportForward.getSendee().toString())) {

                    AccountInfo userInfo = accountInfoManager.findUniqueBy("id", workReportForward.getSendee());
                    if (userInfo != null)
                        existsName.append((userInfo.getDisplayName() == null ? "" : userInfo.getDisplayName()) + "，");
                }

                if (!existsName.toString().equals("")) {
                    messageHelper.addFlashMessage(redirectAttributes,
                            "core.success.save",
                            existsName.substring(0, existsName.length() - 1) + "；已经被转发过此汇报");
                    return strReturnRedirect;
                }
            }
            // SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String[] selectList = selectIds.split(",");
            for (int i = 0; i < selectList.length; i++) {
                //WorkReportForward modelExists=workReportForwardManager.findUniqueBy("","")
                if (strUserId.equals(selectList[i])) continue;
                WorkReportForward workModel = new WorkReportForward();
                workModel.setStatus("0");
                workModel.setSendee(Long.parseLong(selectList[i]));
                workModel.setForwarder(Long.parseLong(currentUserHolder.getUserId()));
                workModel.setWorkReportInfo(reportModel);
                workModel.setForwardtime(new Date());
                workModel.setIsfeedbackforward(isFeedBackForward);//是否转发反馈内容  1是   2否
                String userName = userConnector.findById(currentUserHolder.getUserId().toString()).getDisplayName();
                if (!remarks.equals("")) {
                    // String str = userName + "于" + formatter.format(workModel.getForwardtime()) + "添加转发备注：" + remarks + ";";
                    workModel.setRemarks(remarks);
                }
                workReportForwardManager.save(workModel);
                String tenantId = tenantHolder.getTenantId();
                String title = "[" + workReportInfo.getTitle() + "]汇报转发提醒";
                String content = "[" +
                        currentUserHolder.getName() + "]" +
                        "将[" +
                        workReportInfo.getTitle() + "]汇报转发给您，请查看。";

                String receiver = workModel.getSendee().toString();
                notificationConnector.send(
                        workReportInfo.getId().toString(),
                        tenantId,
                        currentUserHolder.getUserId().toString(),
                        receiver,
                        title,
                        content, MsgConstants.MSG_TYPE_REPORT);
            }
            messageHelper.addFlashMessage(redirectAttributes, "core.success.save",
                    "转发成功");
        } catch (ArithmeticException e) {
            logger.error("汇报反馈异常：" + e.getMessage() + "\r\n" + e.fillInStackTrace());
        }

        return strReturnRedirect;
    }

    @RequestMapping("work-report-info-print")
    @Log(desc = "汇报打印页面", action = "search", operationDesc = "个人信息-工作中心-汇报-打印")
    public String ReportPrint(@RequestParam(value = "id", required = true) Long id, Model model) {
        WorkReportInfo reportModel = workReportInfoManager.findUniqueBy("id", id);
        if (reportModel == null)
            model.addAttribute("error", "没有查询到可打印的数据");
        else {
            model.addAttribute("model", reportModel);

            AccountInfo userpublicMan = accountInfoManager.findUniqueBy("id", reportModel.getUserId());
            if (userpublicMan != null)
                model.addAttribute("publicMan", userpublicMan.getDisplayName());

            AccountInfo userreceiveMan = accountInfoManager.findUniqueBy("id", reportModel.getSendee());
            if (userreceiveMan != null)
                model.addAttribute("receiveMan", userreceiveMan.getDisplayName());

            List<WorkReportCc> ccList = workReportCcManager.findBy("workReportInfo.id", id);
            StringBuilder strCCMans = new StringBuilder();
            if (ccList != null && ccList.size() > 0) {
                for (WorkReportCc cc : ccList) {
                	String cc_type=cc.getCcType();
                    if(StringUtils.isBlank(cc_type))
                    	cc_type="1";
                    if("1".equals(cc_type)){
	                    AccountInfo userInfo = accountInfoManager.findUniqueBy("id", cc.getCcno());
	                    if (userInfo != null)
	                        strCCMans.append(userInfo.getDisplayName() + "，");
                    }
                }
                String strCCManNames="";
                if(strCCMans.length()>0)
                	strCCManNames=strCCMans.substring(0, strCCMans.length() - 1);
                model.addAttribute("ccmans", strCCManNames);
            }
        }

        return "/pim/work-report-info-print";
    }

    @RequestMapping("work-report-info-remove")
    @Log(desc = "汇报删除", action = "delete", operationDesc = "个人信息-工作中心-汇报-汇报删除操作")
    public String remove(@RequestParam("selectedItem") List<Long> selectedItem,
                         RedirectAttributes redirectAttributes) {
        List<WorkReportInfo> workReportInfos = workReportInfoManager
                .findByIds(selectedItem);
        workReportInfoManager.removeAll(workReportInfos);
        messageHelper.addFlashMessage(redirectAttributes,
                "core.success.delete", "删除成功");

        return "/pim/work-report-tome-list";
    }
    
    @RequestMapping("work-report-cc-presetting-init")
    @Log(desc = "汇报条线", action = "search", operationDesc = "系统设置-汇报条线-汇报条线-汇报条线管理")
    public String ccPresettingInit(RedirectAttributes redirectAttributes,Model model) {
    	String strSql="SELECT pre.*,"
    			+" (SELECT IFNULL(REPLACE(GROUP_CONCAT(n.title ORDER BY n.node_level ASC),',','-'),'')"
    			+ " FROM  work_report_cc_presetting_node n WHERE presetting_id=pre.id) as node_title"
    			+" FROM work_report_cc_presetting pre";
    	
    	List<Map<String,Object>> ccMapList=jdbcTemplate.queryForList(strSql);
    	model.addAttribute("ccPresettingList", ccMapList);
    	return "/pim/work-report-cc-presetting";
    }
    
    @RequestMapping("work-report-cc-presetting-node-input")
    @Log(desc = "汇报条线新增/修改", action = "search", operationDesc = "系统设置-汇报条线-汇报条线-汇报条线新增/修改")
    public String ccPresettingInput(@RequestParam(value = "id", required = false) Long id,RedirectAttributes redirectAttributes,Model model) {
        if(id!=null){
        	WorkReportCcPresetting workReportCcPresetting=workReportCcPresettingManager.get(id);
        	if(workReportCcPresetting!=null){
        		model.addAttribute("model",workReportCcPresetting);
        		String strSql="select * from work_report_cc_presetting_node where presetting_id=%s and status='1'";
        		List<Map<String,Object>> ccPresettingNodeList=jdbcTemplate.queryForList(String.format(strSql, id));
        		if(ccPresettingNodeList!=null&&ccPresettingNodeList.size()>0){
        			String strPartySql="SELECT IFNULL(GROUP_CONCAT(p.`NAME`),'') AS party_NAME FROM party_entity p WHERE p.id in(%s)";
        			for (Map<String, Object> map : ccPresettingNodeList) {
        				String strNames="";
        				String strIds=map.get("positionId")==null?"":map.get("positionId").toString();
        				if(!strIds.equals("")){
        					List<Map<String,Object>> partyMapList=jdbcTemplate.queryForList(String.format(strPartySql, map.get("positionId")));
            				if(null!=partyMapList&&partyMapList.size()>0){
            					strNames=partyMapList.get(0).get("party_NAME").toString();
            				}
        				}
        				map.put("positionName", strNames);
					}
        			model.addAttribute("nodelist",ccPresettingNodeList);
        		}
        	}
        }
    	
        model.addAttribute("code", DeEnCode.decode("SVTU[PTT[QV[VUZV"));
    	return "/pim/work-report-cc-presetting-node";
    }
    
    @RequestMapping("work-report-cc-presetting-node-save")
    @Log(desc = "汇报条线新增/修改-保存", action = "update", operationDesc = "系统设置-汇报条线-汇报条线-汇报条线新增/修改-保存")
    //@Transactional(readOnly = false)
    public String ccPresettingSave(
    						HttpServletRequest request,
    						@RequestParam(value = "id", required = false) Long id,
    						@RequestParam(value = "title", required = false) String title,
    						@RequestParam(value = "status", required = false) String status,
							@RequestParam(value = "note", required = false) String note,
							@RequestParam(value = "presetting_title", required = false) String[] presetting_title,
							@RequestParam(value = "presetting_type", required = false) String[] presetting_type,
							@RequestParam(value = "ipt_postId", required = false) String[] ipt_postId,
    						RedirectAttributes redirectAttributes,
    						Model model) {
        if(id==null){
        	WorkReportCcPresetting ccPresetting=new WorkReportCcPresetting();
        	ccPresetting.setTitle(title);
        	ccPresetting.setStatus(status);
        	ccPresetting.setNote(note);
        	workReportCcPresettingManager.save(ccPresetting);
        	
        	if(null!=presetting_title&&presetting_title.length>0){
        		for (int i=0;i<presetting_title.length;i++) {
					WorkReportCcPresettingNode ccPresettingNode=new WorkReportCcPresettingNode();
					ccPresettingNode.setPresettingId(ccPresetting.getId());
					ccPresettingNode.setTitle(presetting_title[i]);
					ccPresettingNode.setPresettingType(presetting_type[i]);
					ccPresettingNode.setPositionId(ipt_postId[i]);
					ccPresettingNode.setStatus("1");
					ccPresettingNode.setNodeLevel((i+1));
					workReportCcPresettingNodeManager.save(ccPresettingNode);
				}
        	}
        }
        else{
        	WorkReportCcPresetting ccPresetting=workReportCcPresettingManager.get(id);
        	ccPresetting.setTitle(title);
        	ccPresetting.setStatus(status);
        	ccPresetting.setNote(note);
        	workReportCcPresettingManager.save(ccPresetting);
        	
        	List<WorkReportCcPresettingNode> ccPresettingNodeList=workReportCcPresettingNodeManager.findBy("presettingId", id);
        	workReportCcPresettingNodeManager.removeAll(ccPresettingNodeList);
        	
        	if(null!=presetting_title&&presetting_title.length>0){
        		for (int i=0;i<presetting_title.length;i++) {
					WorkReportCcPresettingNode ccPresettingNode=new WorkReportCcPresettingNode();
					ccPresettingNode.setPresettingId(ccPresetting.getId());
					ccPresettingNode.setTitle(presetting_title[i]);
					ccPresettingNode.setPresettingType(presetting_type[i]);
					ccPresettingNode.setPositionId(ipt_postId[i]);
					ccPresettingNode.setStatus("1");
					ccPresettingNode.setNodeLevel((i+1));
					workReportCcPresettingNodeManager.save(ccPresettingNode);
				}
        	}
        }
    	
        messageHelper.addFlashMessage(redirectAttributes, "core.success.save",
                "保存成功");
        return "redirect:/pim/work-report-cc-presetting-init.do";
    }

    // ~ ======================================================================
    @Resource
    public void setWorkReportInfoManager(	
            WorkReportInfoManager workReportInfoManager) {
        this.workReportInfoManager = workReportInfoManager;
    }

    @Resource
    public void setWorkReportAttachmentManager(
            WorkReportAttachmentManager workReportAttachmentManager) {
        this.workReportAttachmentManager = workReportAttachmentManager;
    }

    @Resource
    public void setWorkReportCcManager(WorkReportCcManager workReportCcManager) {
        this.workReportCcManager = workReportCcManager;
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
    public void setTenantHolder(TenantHolder tenantHolder) {
        this.tenantHolder = tenantHolder;
    }

    @Resource
    public void setWorkReportForwardManager(WorkReportForwardManager workReportForwardManager) {
        this.workReportForwardManager = workReportForwardManager;
    }

    @Resource
    public void setAccountInfoManager(AccountInfoManager accountInfoManager) {
        this.accountInfoManager = accountInfoManager;
    }

    @Resource
    public void setNotificationConnector(NotificationConnector notificationConnector) {
        this.notificationConnector = notificationConnector;
    }

    @Override
    public void setServletContext(ServletContext servletContext) {

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
    public void setWorkReportCcPresettingManager(WorkReportCcPresettingManager workReportCcPresettingManager) {
        this.workReportCcPresettingManager = workReportCcPresettingManager;
    }
    
    @Resource
    public void setWorkReportCcPresettingNodeManager(WorkReportCcPresettingNodeManager workReportCcPresettingNodeManager) {
        this.workReportCcPresettingNodeManager = workReportCcPresettingNodeManager;
    }
}
