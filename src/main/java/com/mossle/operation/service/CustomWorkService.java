package com.mossle.operation.service;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartResolver;

import com.alibaba.fastjson.JSONObject;
import com.mossle.api.custom.CustomConnector;
import com.mossle.api.form.FormConnector;
import com.mossle.api.humantask.HumanTaskConnector;
import com.mossle.api.keyvalue.FormParameter;
import com.mossle.api.keyvalue.KeyValueConnector;
import com.mossle.api.keyvalue.Record;
import com.mossle.api.notification.NotificationConnector;
import com.mossle.api.org.OrgConnector;
import com.mossle.api.process.ProcessConnector;
import com.mossle.api.store.StoreConnector;
import com.mossle.api.tenant.TenantHolder;
import com.mossle.api.user.UserConnector;
import com.mossle.button.ButtonHelper;
import com.mossle.common.utils.FileUploadAPI;
import com.mossle.common.utils.WebAPI;
import com.mossle.core.MultipartHandler;
import com.mossle.core.auth.CurrentUserHolder;
import com.mossle.core.auth.CustomPasswordEncoder;
import com.mossle.core.mapper.JsonMapper;
import com.mossle.core.spring.MessageHelper;
import com.mossle.core.util.StringUtils;
import com.mossle.humantask.persistence.domain.TaskInfo;
import com.mossle.humantask.persistence.manager.TaskInfoManager;
import com.mossle.keyvalue.persistence.domain.RecordInfo;
import com.mossle.keyvalue.persistence.manager.RecordManager;
import com.mossle.msg.persistence.domain.MsgInfo;
import com.mossle.msg.persistence.manager.MsgInfoManager;
import com.mossle.operation.persistence.domain.CustomApprover;
import com.mossle.operation.persistence.domain.CustomEntity;
import com.mossle.operation.persistence.domain.CustomPre;
import com.mossle.operation.persistence.domain.CustomWorkEntityDTO;
import com.mossle.operation.persistence.manager.CustomApproverManager;
import com.mossle.operation.persistence.manager.CustomManager;
import com.mossle.operation.persistence.manager.CustomPreManager;
import com.mossle.operation.web.ProcessOperationController;
import com.mossle.party.persistence.manager.PartyEntityManager;
import com.mossle.party.persistence.manager.PartyStructManager;
import com.mossle.party.service.OrgService;
import com.mossle.party.support.PartyOrgConnector;
import com.mossle.user.persistence.manager.AccountCredentialManager;
import com.mossle.user.persistence.manager.AccountInfoManager;
import com.mossle.user.persistence.manager.RosterLogManager;
import com.mossle.user.persistence.manager.UpdatePersonManager;
import com.mossle.user.service.PersonInfoService;
import com.mossle.user.service.UserService;
import com.mossle.user.web.PersonInfoController;
import com.mossle.util.DateUtil;
import com.mossle.util.StringUtil;


/**
 * 
 * @author ckx
 *
 */
@Service
@Transactional(readOnly = true)
public class CustomWorkService {

private static Logger logger = LoggerFactory.getLogger(CustomWorkService.class);
    
    public static final int STATUS_DRAFT_PROCESS = 0;
    public static final int STATUS_DRAFT_TASK = 1;
    public static final int STATUS_RUNNING = 2;
    @Autowired
    private OperationService operationService;
    @Autowired
    private KeyValueConnector keyValueConnector;
    @Autowired
    private MessageHelper messageHelper;
    @Autowired
    private CurrentUserHolder currentUserHolder;
    @Autowired
    private ProcessConnector processConnector;
    @Autowired
    private HumanTaskConnector humanTaskConnector;
    @Autowired
    private MultipartResolver multipartResolver;
    @Autowired
    private StoreConnector storeConnector;
    private ButtonHelper buttonHelper = new ButtonHelper();
    @Autowired
    private FormConnector formConnector;
    private JsonMapper jsonMapper = new JsonMapper();
    @Autowired
    private TenantHolder tenantHolder;
    @Autowired
    private AccountCredentialManager accountCredentialManager;
    @Autowired
    private CustomManager customManager;
    @Autowired
    private TaskInfoManager taskInfoManager;
    @Autowired
    private CustomPreManager customPreManager;
    @Autowired
    private FileUploadAPI fileUploadAPI;
    @Autowired
    private WebAPI webAPI;
    @Autowired
    private ProcessOperationController processOperationController;
    @Autowired
    private AccountInfoManager accountInfoManager;
    @Autowired
    private CustomPasswordEncoder customPasswordEncoder;
    @Autowired
    private CustomApproverManager customApproverManager;
    @Autowired
    private OrgConnector orgConnector;
    @Autowired
    private CustomConnector customConnector;
    @Autowired
    private UserConnector userConnector;
    @Autowired
    private NotificationConnector notificationConnector;
    private String baseUrl;
    @Autowired
    private MsgInfoManager msgInfoManager;
    @Autowired
    private PersonInfoController personInfoController ;
    @Autowired
    private UpdatePersonManager updatePersonManager ;
    private PersonInfoService personInfoService;
    @Autowired
    private RosterLogManager rosterLogManager ;
    @Autowired
    private UserService userService;
    @Autowired
    private PartyEntityManager partyEntityManager;
    @Autowired
    private OrgService orgService;
    @Autowired
    private PartyStructManager partyStructManager;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private CustomService customService;
    @Autowired
    private PartyOrgConnector partyOrgConnector;
    @Autowired
    private RecordManager recordManager;
    
    /**
     * 发起流程.  请假申请使用
     * @throws Exception 
     * @throws IOException 
     */
    @Transactional(readOnly = false)
	public void StartProcessCustom(HttpServletRequest request,
			CustomWorkEntityDTO customWorkEntityDTO, String areaName,
			MultipartFile[] files,CustomEntity customEntity) throws IOException, Exception {
   
    	
    	if(areaName == null){
			areaName = "";
		}
		String businessKey;
		String userId = currentUserHolder.getUserId();
        String curentUserName = currentUserHolder.getName();
        
        FormParameter formParameter = this.doSaveRecord(request);
        
        businessKey = formParameter.getBusinessKey();
        
        //抄送人id和姓名
        String ccId = request.getParameter("ccnos");
        String ccNames = request.getParameter("ccName");
   
        //CustomEntity customEntity = new CustomEntity();
        customEntity.setApplyCode(customWorkEntityDTO.getApplyCode());
        customEntity.setUserId(Long.parseLong(userId));
        String tenantId = tenantHolder.getTenantId();
        
        
        customEntity.setFormType(customWorkEntityDTO.getFormType());
        customEntity.setType(customWorkEntityDTO.getType());
        customEntity.setStartTime(customWorkEntityDTO.getStartDate());
        customEntity.setEndTime(customWorkEntityDTO.getEndDate());
        customEntity.setTotalTime(customWorkEntityDTO.getTotalTime());
        customEntity.setDestination(customWorkEntityDTO.getDestination());
        customEntity.setDepartmentName(customWorkEntityDTO.getDepartmentName());
        customEntity.setDate(customWorkEntityDTO.getDate());
        customEntity.setPeerId(customWorkEntityDTO.getTxnos());
        customEntity.setPeerName(customWorkEntityDTO.getTxName());
        customEntity.setTheme(customWorkEntityDTO.getTheme());
        customEntity.setName(customWorkEntityDTO.getName());
        customEntity.setBusinessDetail(customWorkEntityDTO.getBusinessDetail());
        customEntity.setBusinessType(customWorkEntityDTO.getBusinessType());
        
        customEntity.setApplyContent(customWorkEntityDTO.getApplyContent().toString().replace("'", ""));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	String str = sdf.format(new Date());
        customEntity.setCreateTime(str);
        customEntity.setModifyTime(str);
        customEntity.setSubmitTimes(1);
        //customEntity.setCcName(ccName);
        //customEntity.setCcnos(ccId);
        
        String nextId=request.getParameter("nextID");
        String nextUser=request.getParameter("nextUser");
        String strBusiLevel=orgConnector.getPositionMaxGradeByUserIds(nextId);
        customEntity.setBusinessLevel(strBusiLevel);
        customManager.save(customEntity);
        
        //保存自定义请假详情表    方便考勤列表展示  start
        saveLeaveDetails(customWorkEntityDTO, userId, customEntity);
    	//end
        
        //审核人id
        String[] auditList=nextId.split(",");
        //审核人名称
        String[] auditNameList=nextUser.split(",");
        int i=1;
        for(String auditor:auditList){
        	CustomApprover approver=new CustomApprover();
        	approver.setApproverId(Long.valueOf(auditor));
        	approver.setCustomId(customEntity.getId());
        	approver.setBusinessKey(businessKey);
        	approver.setApproveStep(i);
        	approver.setOpterType("0");
        	approver.setAuditComment("");
        	customApproverManager.save(approver);
        	i++;
        }
        //保存表单
        SaveFormHtml(customEntity,customWorkEntityDTO,businessKey);
        
        //处理受理单编号
        operationService.deleteApplyCode(customWorkEntityDTO.getApplyCode());
        
        //保存附件
        fileUploadAPI.uploadFile(files, tenantId, Long.toString(customEntity.getId()), "operation/CustomApply");
        
        //记录表单的主键ID
        Long FormId =customEntity.getId();
        
        //发起流程
       customStartApply(customEntity,curentUserName, userId, auditNameList[0], 
        		auditList[0], FormId, businessKey, ccId, ccNames);
    	
		
	}
    /*
     * //保存申请详情表，用于考勤列表显示
     */
    @Transactional(readOnly = false)
	public void saveLeaveDetails(CustomWorkEntityDTO customWorkEntityDTO,
			String userId, CustomEntity customEntity) throws ParseException {
		if("1".equals(customWorkEntityDTO.getFormType())){
        	//只适用于请假
        	String startDate = customWorkEntityDTO.getStartDate();
        	String endDate = customWorkEntityDTO.getEndDate();
        	
        	String startDateStr = DateUtil.formatDateStrToStr(startDate, "yyyy-MM-dd");
        	String endDateStr = DateUtil.formatDateStrToStr(endDate, "yyyy-MM-dd");
        	
        	//查询当前人员班次时间
        	String weekCSStart = DateUtil.getWeekCS(startDateStr);
        	String weekCSEnd = DateUtil.getWeekCS(endDateStr);
        	
        	String attendanceStart = getAttendance(weekCSStart);
        	String attendanceEnd = getAttendance(weekCSEnd);
        	
        	Map<String, Object> queryForMapStart = null;
    		try {
    			queryForMapStart = jdbcTemplate.queryForMap("select s.start_time , s.end_time from shift s where s.id in (select a."+attendanceStart+" from attendance_records a LEFT JOIN person_attendance_records p ON a.id = p.attendanceRecordsID where p.personID = '"+userId+"')");
    		} catch (Exception e) {
    		}
        	
        	
        	Map<String, Object> queryForMapEnd = null;
    		try {
    			queryForMapEnd = jdbcTemplate.queryForMap("select s.start_time , s.end_time from shift s where s.id in (select a."+attendanceEnd+" from attendance_records a LEFT JOIN person_attendance_records p ON a.id = p.attendanceRecordsID where p.personID = '"+userId+"')");
    		} catch (Exception e) {
    		}
        	//不是同一天
    		boolean startCompare = false;
    		boolean endCompare = false;
        	if(!startDateStr.equals(endDateStr)){
        		//第一天
        		if(null != queryForMapStart){
        			Object objStartTime = queryForMapStart.get("start_time");
        			Object objEndTime = queryForMapStart.get("end_time");
        			//不是休息
        			if(null != objStartTime && !"".equals(objStartTime) && !"null".equals(objStartTime) && null != objEndTime && !"".equals(objEndTime) && !"null".equals(objEndTime)){
        				startCompare = DateUtil.compare(startDate, startDateStr+" "+queryForMapStart.get("end_time"));
        			}
                	
                	//当前用户
                	if(startCompare){
                		jdbcTemplate.execute("insert into leave_details (customFormId,userId,formType,type,startTime,endTime,creater,createdDate) values ('"+customEntity.getApplyCode()+"','"+userId+"','"+customEntity.getFormType()+"','"+customEntity.getType()+"','"+startDate+"','"+startDateStr+" "+queryForMapStart.get("end_time")+"','"+userId+"',now())");
                	}
            	}
        		//最后一天
        		if(null != queryForMapEnd){
        			Object objStartTime = queryForMapEnd.get("start_time");
        			Object objEndTime = queryForMapEnd.get("end_time");
        			//不是休息
        			if(null != objStartTime && !"".equals(objStartTime) && !"null".equals(objStartTime) && null != objEndTime && !"".equals(objEndTime) && !"null".equals(objEndTime)){
        				endCompare = DateUtil.compare(endDateStr+" "+queryForMapEnd.get("start_time"), endDate);
        			}
                	if(endCompare){
                		jdbcTemplate.execute("insert into leave_details (customFormId,userId,formType,type,startTime,endTime,creater,createdDate) values ('"+customEntity.getApplyCode()+"','"+userId+"','"+customEntity.getFormType()+"','"+customEntity.getType()+"','"+endDateStr+" "+queryForMapEnd.get("start_time")+"','"+endDate+"','"+userId+"',now())");
                	}
            	}
        	}else{
        		jdbcTemplate.execute("insert into leave_details (customFormId,userId,formType,type,startTime,endTime,creater,createdDate) values ('"+customEntity.getApplyCode()+"','"+userId+"','"+customEntity.getFormType()+"','"+customEntity.getType()+"','"+startDate+"','"+endDate+"','"+userId+"',now())");
        	}
        	//计算两个时间内有几天
        	long days = DateUtil.getDays(endDate, startDate);
        	for (int i = 0; i < days-1; i++) {
        		
        		String addDate = DateUtil.getAddDate(startDate, i+1);
        		String weekCS= DateUtil.getWeekCS(addDate);
        		String attendanceAdd = getAttendance(weekCS);
            	Map<String, Object> queryForMapAdd = null;
    			try {
    				queryForMapAdd = jdbcTemplate.queryForMap("select s.start_time , s.end_time from shift s where s.id in (select a."+attendanceAdd+" from attendance_records a LEFT JOIN person_attendance_records p ON a.id = p.attendanceRecordsID where p.personID = '"+userId+"')");
    			} catch (Exception e) {
    			}
    			if(null != queryForMapAdd){
    				Object objStartTime = queryForMapAdd.get("start_time");
        			Object objEndTime = queryForMapAdd.get("end_time");
        			//不是休息
        			if(null != objStartTime && !"".equals(objStartTime) && !"null".equals(objStartTime) && null != objEndTime && !"".equals(objEndTime) && !"null".equals(objEndTime)){
        				jdbcTemplate.execute("insert into leave_details (customFormId,userId,formType,type,startTime,endTime,creater,createdDate) values ('"+customEntity.getApplyCode()+"','"+userId+"','"+customEntity.getFormType()+"','"+customEntity.getType()+"','"+addDate+" "+queryForMapAdd.get("start_time")+"','"+addDate+" "+queryForMapAdd.get("end_time")+"','"+userId+"',now())");
        			}
    			}/*else{
    	    		jdbcTemplate.execute("insert into leave_details (customFormId,userId,formType,type,startTime,endTime,creater,createdDate) values ('"+customEntity.getApplyCode()+"','"+userId+"','"+customEntity.getFormType()+"','"+customEntity.getType()+"','"+addDate+" "+"00:00"+"','"+addDate+" "+"24:00"+"','"+userId+"',now())");
    			}*/
    		}
        	
        }else{
        	//非请假
        	String startDate = customWorkEntityDTO.getStartDate();
        	String endDate = customWorkEntityDTO.getEndDate();
        	
        	String startDateStr = DateUtil.formatDateStrToStr(startDate, "yyyy-MM-dd");
        	String endDateStr = DateUtil.formatDateStrToStr(endDate, "yyyy-MM-dd");
        	
        	//不是同一天
        	if(!startDateStr.equals(endDateStr)){
            	jdbcTemplate.execute("insert into leave_details (customFormId,userId,formType,type,startTime,endTime,creater,createdDate) values ('"+customEntity.getApplyCode()+"','"+userId+"','"+customEntity.getFormType()+"','"+customEntity.getType()+"','"+startDate+"','"+startDateStr+" "+"24:00"+"','"+userId+"',now())");
            	jdbcTemplate.execute("insert into leave_details (customFormId,userId,formType,type,startTime,endTime,creater,createdDate) values ('"+customEntity.getApplyCode()+"','"+userId+"','"+customEntity.getFormType()+"','"+customEntity.getType()+"','"+endDateStr+" "+"00:00"+"','"+endDate+"','"+userId+"',now())");
        	}else{
        		jdbcTemplate.execute("insert into leave_details (customFormId,userId,formType,type,startTime,endTime,creater,createdDate) values ('"+customEntity.getApplyCode()+"','"+userId+"','"+customEntity.getFormType()+"','"+customEntity.getType()+"','"+startDate+"','"+endDate+"','"+userId+"',now())");
        	}
        	//计算两个时间内有几天
        	long days = DateUtil.getDays(endDate, startDate);
        	for (int i = 0; i < days-1; i++) {
        		
        		String addDate = DateUtil.getAddDate(startDate, i+1);
    	    	jdbcTemplate.execute("insert into leave_details (customFormId,userId,formType,type,startTime,endTime,creater,createdDate) values ('"+customEntity.getApplyCode()+"','"+userId+"','"+customEntity.getFormType()+"','"+customEntity.getType()+"','"+addDate+" "+"00:00"+"','"+addDate+" "+"24:00"+"','"+userId+"',now())");
    		}
        	//有同行人
        	if( null != customEntity.getPeerId() && !"".equals(customEntity.getPeerId()) && !"null".equals(customEntity.getPeerId())){
        		String peer = customEntity.getPeerId();
        		String[] split = peer.split(",");
        		for (int i = 0; i < split.length; i++) {
        			for (int j = 0; j < days-1; j++) {
        				String addDate = DateUtil.getAddDate(startDate, j+1);
    		    			jdbcTemplate.execute("insert into leave_details (customFormId,userId,formType,type,startTime,endTime,creater,createdDate) values ('"+customEntity.getApplyCode()+"','"+split[i]+"','"+customEntity.getFormType()+"','"+customEntity.getType()+"','"+addDate+" "+"00:00"+"','"+addDate+" "+"24:00"+"','"+userId+"',now())");
        			}
        			//同一天
        			if(!startDateStr.equals(endDateStr)){
                    	jdbcTemplate.execute("insert into leave_details (customFormId,userId,formType,type,startTime,endTime,creater,createdDate) values ('"+customEntity.getApplyCode()+"','"+split[i]+"','"+customEntity.getFormType()+"','"+customEntity.getType()+"','"+startDate+"','"+startDateStr+" "+"24:00"+"','"+userId+"',now())");
                    	jdbcTemplate.execute("insert into leave_details (customFormId,userId,formType,type,startTime,endTime,creater,createdDate) values ('"+customEntity.getApplyCode()+"','"+split[i]+"','"+customEntity.getFormType()+"','"+customEntity.getType()+"','"+endDateStr+" "+"00:00"+"','"+endDate+"','"+userId+"',now())");
        			}else{
        	    		jdbcTemplate.execute("insert into leave_details (customFormId,userId,formType,type,startTime,endTime,creater,createdDate) values ('"+customEntity.getApplyCode()+"','"+split[i]+"','"+customEntity.getFormType()+"','"+customEntity.getType()+"','"+startDate+"','"+endDate+"','"+userId+"',now())");
        			}
        			
    			}
        	}
        }
        
    	//end
        
	}
    
    public String getAttendance(String weekCS){
    	String c = "";
    	if("星期一".equals(weekCS)){
    		c = "mondayShiftID";
    	}else if("星期二".equals(weekCS)){
    		c = "tuesdayShiftID";
    	}else if("星期三".equals(weekCS)){
    		c = "wednesdayShiftID";
    	}else if("星期四".equals(weekCS)){
    		c = "thursdayShiftID";
    	}else if("星期五".equals(weekCS)){
    		c = "fridayShiftID";
    	}else if("星期六".equals(weekCS)){
    		c = "SaturdayShiftID";
    	}else if("星期日".equals(weekCS)){
    		c = "SundayShiftID";
    	}
    	return c;
    }
    
    /**
     * 完成任务 pc端使用.
     */
    @SuppressWarnings({ "unused", "unchecked" })
	@Transactional(readOnly = false)
	public void CompleteTaskCustomPC(HttpServletRequest request,
			CustomWorkEntityDTO customWorkEntityDTO, String processInstanceId,
			MultipartFile[] files, String iptdels, String flag)
			throws Exception, IOException {
		//获得当前登录人的ID和姓名
    	String userId = currentUserHolder.getUserId();
    	String name = currentUserHolder.getName();
    	String userName = currentUserHolder.getUsername();
    	String f = flag;
    	String humanTaskId = "";
    	//将审批信息更新到 task_info表中
    	List<TaskInfo> taskInfo = taskInfoManager.findBy("processInstanceId",processInstanceId);
    
    	 for (TaskInfo getInfo : taskInfo) {
    		String strOpterComment="";
    		if( (getInfo.getStatus().equals("active")) && !(getInfo.getCatalog().equals("copy"))){
    			getInfo.setTenantId("1");
    			getInfo.setStatus("complete");
    			getInfo.setCompleteTime(new Date());
    			humanTaskId = getInfo.getId().toString();//该流程任务处理后，其消息已读
    			if (f.equals("3")) { //重新发起申请
    				//ckx  撤回重新调整审核人  2018/11/6
    	        	//删除审批步骤表
    	        	List<CustomApprover> findBy = customApproverManager.findBy("businessKey", getInfo.getBusinessKey());
    	        	customApproverManager.removeAll(findBy);
    	        	//保存新的审批步骤
    	            //添加审批人
    	        	String allLeaderId = request.getParameter("allLeaderId");
    	        	String oldLeaderId = request.getParameter("oldLeaderId");
    	            String[] auditList=allLeaderId.split(",");
    	            int i=1;
    	            for(String auditor:auditList){
    	            	CustomApprover approver=new CustomApprover();
    	            	approver.setApproverId(Long.valueOf(auditor));
    	            	approver.setCustomId(Long.valueOf(getInfo.getProcessInstanceId()));
    	            	approver.setBusinessKey(getInfo.getBusinessKey());
    	            	approver.setApproveStep(i);
    	            	approver.setOpterType("0");
    	            	approver.setAuditComment("");
    	            	customApproverManager.save(approver);
    	            	i++;
    	            }
    	        	if(!oldLeaderId.equals(allLeaderId)){
    	        		getInfo.setComment("系统：发起人【"+name+"】重新调整了审核人");
    	        	}
    				
    				strOpterComment="重新申请";
    				getInfo.setAction("重新发起申请");
    				//指定的下一步审批人 名字
    				getInfo.setOwner(request.getParameter("leaderName"));
        			//指定的下一步审批人 id
    				getInfo.setTaskId(request.getParameter("leader"));
				
					//生成  下一审批人的一条新记录
    				TaskInfo taskInfo2 = new TaskInfo();
    		    	//下一条审批的负责人
    		    	taskInfo2.setAssignee(request.getParameter("leader"));
    		    	
    		    	//负责人姓名
    		    	taskInfo2.setName(request.getParameter("leaderName")+"审批");
    		    	
    		    	//创建时间
    		    	taskInfo2.setCreateTime(new Date());
    		    	taskInfo2.setAction(customWorkEntityDTO.getTheme()+"等待审批");
    		    	//主题
    		    	taskInfo2.setPresentationSubject(getInfo.getPresentationSubject());
    		    	taskInfo2.setPresentationName(customWorkEntityDTO.getTheme());
    		    	//处理状态
    		    	taskInfo2.setStatus("active");
    		    	//存入上一节点的id
    		    	taskInfo2.setCode(getInfo.getAssignee());
    		    	taskInfo2.setTenantId("1");
    		    	taskInfo2.setSuspendStatus("自定义申请");
    		    	//存入表单ID，否则下个人审批的时候取不到申请单的数据
    		    	taskInfo2.setProcessInstanceId(getInfo.getProcessInstanceId());
    		    	//记录任务发起人
    		    	taskInfo2.setDescription(getInfo.getDescription());
    		    	taskInfo2.setBusinessKey(getInfo.getBusinessKey());
    		    	taskInfo2.setCatalog("normal");
    		    	taskInfoManager.save(taskInfo2);
    		    	
    		    	//向oa_bpm_customPre表中记录同意的记录，用于驳回的时候找上一节点
    				
    		    	//若当前节点在oa_bpm_customPre表中 已经作为上一节点存在，删除，重新插入，也就是只记录最新的一条
    		    	String sql3 = "from CustomPre where  formID =? ";
    		        
    		        List<CustomPre> c3 = customPreManager.find(sql3,getInfo.getProcessInstanceId());
    		        
    		        if(c3.size()>0){
    					customPreManager.removeAll(c3);
    				}
    		        
    		    	CustomPre customPre = new CustomPre();
    				customPre.setAssignee(request.getParameter("leader"));
    				customPre.setPrevious(getInfo.getAssignee());
    				customPre.setFormID(getInfo.getProcessInstanceId());
    				customPre.setCreateTime(new Date());
    				customPreManager.save(customPre);
	    		    	
    				String ccnos = request.getParameter("ccnos");//抄送人id和姓名
     		        String ccName = request.getParameter("ccName");//抄送人id和姓名
		    		
	    		    //用户调整了表单，更新表单内容
     		        String tenantId = tenantHolder.getTenantId();
     		        String applyCode =  request.getParameter("applyCode");
     		        CustomEntity customEntity =customManager.findUniqueBy("applyCode", applyCode);
     		        if(customEntity==null)
     		        	customEntity=new CustomEntity();
  				
     		        customEntity.setApplyContent(customWorkEntityDTO.getApplyContent());
     		        customEntity.setTheme(customWorkEntityDTO.getTheme());
  					
    		        customEntity.setApplyCode(customWorkEntityDTO.getApplyCode());
    		        customEntity.setUserId(Long.parseLong(userId));
    		        customEntity.setName(currentUserHolder.getName());
    		        
    		        customEntity.setFormType(customWorkEntityDTO.getFormType());
    		        customEntity.setType(customWorkEntityDTO.getType());
    		        customEntity.setStartTime(customWorkEntityDTO.getStartDate());
    		        customEntity.setEndTime(customWorkEntityDTO.getEndDate());
    		        customEntity.setTotalTime(customWorkEntityDTO.getTotalTime());
    		        customEntity.setDestination(customWorkEntityDTO.getDestination());
    		        customEntity.setDepartmentName(customWorkEntityDTO.getDepartmentName());
    		        customEntity.setDate(customWorkEntityDTO.getDate());
    		        customEntity.setPeerId(customWorkEntityDTO.getTxnos());
    		        customEntity.setPeerName(customWorkEntityDTO.getTxName());
    		        
    		        customEntity.setBusinessDetail(customWorkEntityDTO.getBusinessDetail());
    		        customEntity.setBusinessType(customWorkEntityDTO.getBusinessType());
    		        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    		    	String str = sdf.format(new Date());
    		        customEntity.setCreateTime(request.getParameter("createTime"));
    		        customEntity.setModifyTime(str);
    		        customEntity.setId(Long.parseLong(processInstanceId));
    		        customEntity.setSubmitTimes( customWorkEntityDTO.getSubmitTimes()+1);
    		        customEntity.setCcName("");
    		        customEntity.setCcnos("");
    		        customEntity.setBusinessLevel(customWorkEntityDTO.getBusinessLevel());
    		        customManager.save(customEntity);
    		        
    		        //保存自定义请假详情表    方便考勤列表展示  start
    		        jdbcTemplate.execute("delete from leave_details where customFormId = '"+customWorkEntityDTO.getApplyCode()+"'");
    		        //保存申请详情表，用于考勤列表显示
    		        saveLeaveDetails(customWorkEntityDTO, userId, customEntity);
    		    	//end
    		        
    		        SaveFormHtml(customEntity,customWorkEntityDTO,getInfo.getBusinessKey());
    		        //发送消息 add by lilei at 2017.11.29
    		    	customConnector.doNotice(taskInfo2, currentUserHolder.getUserId(), tenantHolder.getTenantId(), "audit",null);
    		    	
    		        //处理抄送
    		       //if(!ccnos.isEmpty()){
    		    	 //先将之前抄送人 且待阅的删掉
   					sql3 = "from TaskInfo where   catalog=? and status='active' and processInstanceId=? ";
       		        List<TaskInfo> ta_info = taskInfoManager.find(sql3,"copy",processInstanceId);
       		        for(TaskInfo t:ta_info){
       		        	
       		        	MsgInfo msg = msgInfoManager.findUniqueBy("data", t.getId().toString());
    	        		if(msg != null){
    	        			jdbcTemplate.update("delete from msg_info where id = '"+msg.getId()+"'");
    	        			//msgInfoManager.removeById(msg.getId());//重新申请操作时，将抄送人对应的消息删除 2018-01-19
    	        		}
    	        		jdbcTemplate.update("delete from task_info where id = '"+t.getId()+"'");
    	        		//taskInfoManager.removeById(t.getId());
    	        		//先删除岗位流程关联表
           		        jdbcTemplate.update("delete from task_info_approve_position where task_id = '"+t.getId()+"'");
        		    	
       		        }	
       		        //重新调整页面抄送   add ckx   2018/9/9
       		        //先删除岗位流程关联表
       		        //jdbcTemplate.update("delete from task_info_approve_position where position_parentId = '' and business_key = '"+taskInfo2.getBusinessKey()+"'");
       		     if(!ccnos.isEmpty()){
       		        copySave(processInstanceId, ccnos, ccName, customEntity.getName(), userId);
       		     }
		    	   	
       		     	//jdbcTemplate修改后数据库未修改，更改为HibernateEntityDao  ckx 2018/9/25
    		        // zyl 处理 record 表中的提交次数&&若是撤回，则重新发起后更新审核状态shijingxin
    		        //String sql = "update KV_RECORD set submitTimes=" + customEntity.getSubmitTimes()+",audit_status=1" + " ,theme='"+customWorkEntityDTO.getTheme()+"'  where id=" + getInfo.getBusinessKey();
    				//keyValueConnector.updateBySql(sql);
    				RecordInfo recordInfo=recordManager.findUniqueBy("businessKey", getInfo.getBusinessKey());
    				recordInfo.setSubmitTimes(customEntity.getSubmitTimes());
    				recordInfo.setAuditStatus("1");
    				recordInfo.setTheme(customWorkEntityDTO.getTheme());
    				recordManager.save(recordInfo);
    				
			        if (iptdels != null && !iptdels.equals(""))
			            fileUploadAPI.uploadFileDel(iptdels,processInstanceId);

			       fileUploadAPI.uploadFile(files, tenantId, processInstanceId, "operation/CustomApply");
			     } else if (f.equals("4")) {
			    	strOpterComment="撤销申请";
    				getInfo.setAction("撤销申请");
    				taskInfoManager.save(getInfo);
    				//不用插入下一条审批信息，不同意，流程直接结束
    				// zyl 处理 record 表中的结束时间
    				updateRecordEndTime(getInfo);
    	        }
    			if(getInfo.getComment() == null){
    				getInfo.setComment("");
    			}
    			taskInfoManager.save(getInfo);
    		}
    		
    		//审批状态
          	 customService.SetProcessAuditStatus(strOpterComment,getInfo.getBusinessKey());
          	 //审批岗位
          	 customService.SetAuditPosition(getInfo.getBusinessKey());
    	 }
    	 //审批后将此任务对应的消息置为已读 2018.08.27 sjx
         String updateMsg = "update MsgInfo set status=1 where data=?";
         msgInfoManager.batchUpdate(updateMsg, humanTaskId);
	}
    
    
    /**
     * 
     * @param customEntity
     * @param bussinessKey
     * ckx
     */
    public void SaveFormHtml(CustomEntity customEntity,CustomWorkEntityDTO customWorkEntityDTO , String bussinessKey){
    	
    	String formType = customEntity.getFormType();
    	String type = customEntity.getType();
    	
    	
    	String detailHtml="<table  style=\"line-height:34px;\"  class=\"centerdiv\" cellpadding=\"0\" cellspacing=\"0\" >"
    			+"<input id=\"theme\" name=\"theme\" type=\"hidden\" value=\""+customEntity.getTheme()+"\">" 
    			+"<input id=\"formType\" name=\"formType\" type=\"hidden\" value=\""+customEntity.getFormType()+"\">"
    			+"<tr>";
				if("1".equals(formType)){
					detailHtml += "<td colspan=\"4\" align=\"center\"><h2>请假申请单</h2></td>"; 		
				}else if("2".equals(formType)){
					detailHtml += "<td colspan=\"4\" align=\"center\"><h2>出差外出申请单</h2></td>"; 		
				}else if("3".equals(formType)){
					detailHtml += "<td colspan=\"4\" align=\"center\"><h2>加班申请单</h2></td>"; 		
				}else if("4".equals(formType)){
					detailHtml += "<td colspan=\"4\" align=\"center\"><h2>特殊考勤说明申请单</h2></td>"; 		
				}
				detailHtml += "</td>"
							+"</tr>"
    	        //////////////
    	        +"<tr>"
	            	+"<td class=\"f_td\"><span>部门</span>：</td>"
	            	+"<td class=\"f_r_td\" style=\"w\">"+customEntity.getDepartmentName()+"</td>"
	            	+"<td class=\"f_td\"><span>姓名</span>：</td>"
	            	+"<td class=\"f_r_td\">"+customEntity.getName()+"</td>"
            	+"</tr>";
           	   if("1".equals(formType)){
           		   detailHtml += "<tr>"
           				   			+"<td class=\"f_td\"><span>类别</span>：</td>"
           				   			+"<td class=\"f_r_td\">";
           				   			if("1".equals(type)){
           				   				detailHtml += "<div>病假</div>";
           				   			}else if("2".equals(type)){
           				   				detailHtml += "<div>事假</div>";
           				   			}else if("3".equals(type)){
           				   				detailHtml += "<div>倒休假</div>";
           				   			}else if("4".equals(type)){
           				   				detailHtml += "<div>年假</div>";
           				   			}else if("5".equals(type)){
           				   				detailHtml += "<div>补休假</div>";
           				   			}else if("6".equals(type)){
           				   				detailHtml += "<div>婚假</div>";
           				   			}else if("7".equals(type)){
           				   				detailHtml += "<div>产假</div>";
           				   			}else if("8".equals(type)){
           				   				detailHtml += "<div>丧假</div>";
           				   			}else if("9".equals(type)){
           				   				detailHtml += "<div>其他</div>";
           				   			}
           			detailHtml += "</td>"
           						+"<td class=\"f_td\"><span>时间</span>：</td>"
	           					+"<td class=\"f_r_td\" style=\"text-align:left;\">"
	    	            		+customWorkEntityDTO.getDate()
	    	            		+"</td>"
           					+ "</tr>";
           	   }else if("2".equals(formType)){
        		   detailHtml += "<tr>"
        				   			+"<td class=\"f_td\"><span>类别</span>：</td>"
        				   			+"<td class=\"f_r_td\">";
        				   			if("1".equals(type)){
        				   				detailHtml += "<div>出差</div>";
        				   			}else if("2".equals(type)){
        				   				detailHtml += "<div>因公外出</div>";
        				   			}else if("3".equals(type)){
        				   				detailHtml += "<div>其他</div>";
        				   			}
		   			detailHtml += "</td>"
		   					+"<td class=\"f_td\"><span>时间</span>：</td>"
           					+"<td class=\"f_r_td\" style=\"text-align:left;\">"
    	            		+customWorkEntityDTO.getDate()
    	            		+"</td>"
       					+ "</tr>";
        	   }else if("3".equals(formType)){
        		   detailHtml += "<tr>"
						   			+"<td class=\"f_td\"><span>类别</span>：</td>"
						   			+"<td class=\"f_r_td\">"
						   			+"<div>加班</div>"
									+ "</td>"
									+"<td class=\"f_td\"><span>时间</span>：</td>"
									+"<td class=\"f_r_td\" style=\"text-align:left;\">"
		    	            		+customWorkEntityDTO.getDate()
		    	            		+"</td>"
								+"</tr>";
			   }else if("4".equals(formType)){
				   detailHtml += "<tr>"
				   			+"<td class=\"f_td\"><span>类别</span>：</td>"
				   			+"<td class=\"f_r_td\">";
				   			if("1".equals(type)){
				   				detailHtml += "<div>销假</div>";
				   			}else if("2".equals(type)){
				   				detailHtml += "<div>迟到</div>";
				   			}else if("3".equals(type)){
				   				detailHtml += "<div>临时外出</div>";
				   			}else if("4".equals(type)){
				   				detailHtml += "<div>漏打卡</div>";
				   			}else if("5".equals(type)){
				   				detailHtml += "<div>其他</div>";
				   			}
		  			detailHtml += "</td>"
		  					+"<td class=\"f_td\"><span>时间</span>：</td>"
		  					+"<td class=\"f_r_td\" style=\"text-align:left;\">"
			           		+customWorkEntityDTO.getDate()
			           		+"</td>"
							+ "</tr>";
			   }
           	detailHtml +="<tr>"
		            +"<td class=\"f_td\">抄送：</td>"
		            +"<td class=\"f_r_td\" colspan=\"3\">"
		            +"{copyNames}"
		               	 /*+(customEntity.getCcName()==null?"":customEntity.getCcName())*/
		           	+"</td>"
		   	  +"</tr>"
    	      +"<tr>"
    	      		+"<td class=\"f_td\">业务级别：</td>"
	      			+"<td class=\"f_r_td\">"+customEntity.getBusinessLevel()+"</td>"
    	            +"<td class=\"f_td\">发起人：</td>"
    	            +"<td class=\"f_r_td\" colspan=\"2\">"+customEntity.getName()+"</td>"
             +"</tr>"
             +"<tr>"
	    	        +"<td class=\"f_td\" colspan=\"4\" align=\"center\">申请内容</td>"
    	     +"</tr>"
    	     +"<tr>"
	            +"<td class=\"f_td\"  colspan=\"4\" style=\"text-align:left;vertical-align:top;padding:3px 3px 3px 3px;\">"
	            + "<pre style=\"white-space: pre-wrap;word-wrap: break-word;\">"+(customEntity.getApplyContent()==null?"":(customEntity.getApplyContent().toString().replace("'", "")))+"</pre></td>"
	         +"</tr>";
    	     if("2".equals(formType)){
    	    	 detailHtml += "<tr>"
    	    			 		+"<td class=\"f_td\" ><span>目的地</span>：</td>"
    	    			 		+"<td class=\"f_r_td\" colspan=\"3\">"+customEntity.getDestination()+"</td>"
         						+"</tr>"
         						+"<tr>"
         						+"<td class=\"f_td\" ><span>同行人员</span>：</td>"
         						+"<td class=\"f_r_td\" colspan=\"3\">"
       		               	 	+(customWorkEntityDTO.getTxName()==null?"":customWorkEntityDTO.getTxName())
       		               	 	+"</td>"
       		               	 	+"</tr>";
    	     }
	    	 detailHtml += "<tr>"
	    			 		+"<td class=\"f_td\">"
	    			 		+"<span>时间</span>："
	    			 		+"</td>"
	    			 		+"<td class=\"f_r_td\" colspan=\"2\" align=\"center\">"
	    			 		+"<span> 自 </span>"
	    			 		+customWorkEntityDTO.getStartDate()
	    			 		+"<span> 至 </span>"
	    			 		+customWorkEntityDTO.getEndDate()
	    			 		+"</td>"
	    			 		+"<td class=\"f_r_td\">";
	    			 		
	    	 if(null != customWorkEntityDTO.getTotalTime() && !"null".equals(customWorkEntityDTO.getTotalTime()) && !"".equals(customWorkEntityDTO.getTotalTime())){
	    		 detailHtml +="<span >共</span>"
	    				 	+customWorkEntityDTO.getTotalTime()+"<span >时</span>";
	    	 }
	    			 		
	    	 detailHtml +="</td>"
	    			 		+"</tr>";
    	     detailHtml +="<tr>"
	 	        +"<td class=\"f_td\" colspan=\"4\" align=\"center\">审核人</td>"
		     +"</tr>"
		     +"<tr>"
	            +"<td class=\"f_td\"  colspan=\"4\" >"
	            + "<ul style=\"width:96%;margin:0 auto;list-style:none;\">{auditor}</ul></td>"
             +"</tr>"
    	    +"</table>";
    	//jdbcTemplate修改后数据库未修改，更改为HibernateEntityDao  ckx 2018/9/25
    	/*String sqlRecordUpdate = "update KV_RECORD set submitTimes = '1' , detailHtml= '" + detailHtml + "',pk_id =" + customEntity.getId()+",REF='" + customEntity.getId()+
    			"',apply_content= '" + customEntity.getApplyContent() +
        		"',theme ='" + customEntity.getTheme() +
    			"' where id= " + bussinessKey ;*/
    	RecordInfo recordInfo=recordManager.findUniqueBy("businessKey", bussinessKey);
    	recordInfo.setSubmitTimes(1);
    	recordInfo.setDetailHtml(detailHtml);
    	recordInfo.setPkId(String.valueOf(customEntity.getId()));
    	recordInfo.setRef(String.valueOf(customEntity.getId()));
    	recordInfo.setApplyCode(customEntity.getApplyCode());
    	recordInfo.setTheme(customEntity.getTheme());
    	//keyValueConnector.updateBySql(sqlRecordUpdate);
    	recordManager.save(recordInfo);
    	return;
    }
	
    
    /**
     * 发起自定义流程
     * ckx
     * 
     */
    @Transactional(readOnly = false)
    public void customStartApply(CustomEntity customEntity,String curentUserName,String userId,String nextConfirmUserName 
    								,String nextConfirmUserID,Long FormId, String businessKey
    								,String ccnos,String ccName) 
            throws Exception {
    	
    //插入当前操作人的一条发起申请记录
    	TaskInfo taskInfo = new TaskInfo();
    	//当前操作人 的id
    	taskInfo.setAssignee(userId );
    	//当前操作人 的姓名
    	taskInfo.setName(curentUserName+"-发起"+customEntity.getTheme());
    	//指定的下一步审批人 名字
    	taskInfo.setOwner(nextConfirmUserName );
    	
    	//指定的下一步审批人 id
    	taskInfo.setTaskId(nextConfirmUserID);
    	//记录任务发起人
    	taskInfo.setDescription(userId);
    	//创建时间
    	Date createtime = new Date();
    	createtime = new Date();
    	taskInfo.setCreateTime(createtime);
    	taskInfo.setCompleteTime(createtime);
    	taskInfo.setAction("发起"+customEntity.getTheme());
    	//主题
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	String str = sdf.format(new Date());
    	taskInfo.setPresentationSubject(customEntity.getTheme()+"申请-"+curentUserName+"-"+str);
    	taskInfo.setPresentationName(customEntity.getTheme());
    	//处理状态
    	taskInfo.setStatus("complete");
    	//与表单id关联起来
    	taskInfo.setForm(Long.toString(FormId));
    	taskInfo.setTenantId("1");
    	taskInfo.setSuspendStatus("自定义申请");
    	taskInfo.setBusinessKey(businessKey);
    	taskInfo.setCatalog("start");
    	taskInfo.setProcessInstanceId(Long.toString(FormId));
    	taskInfoManager.save(taskInfo);
    	
        //插入下一个审批人的一条待执行记录
    	TaskInfo taskInfo2 = new TaskInfo();
    	//下一条审批的负责人
    	taskInfo2.setAssignee(nextConfirmUserID);
    	
    	//负责人姓名
    	taskInfo2.setName(nextConfirmUserName+"审批");
    	
    	//记录任务发起人
    	taskInfo2.setDescription(userId);
    	//创建时间
    	taskInfo2.setCreateTime(new Date());
    	taskInfo2.setAction(customEntity.getTheme()+"等待审批");
    	//主题
    	taskInfo2.setPresentationSubject(taskInfo.getPresentationSubject());
    	taskInfo2.setPresentationName(customEntity.getTheme());
    	//处理状态
    	taskInfo2.setStatus("active");
    	//存入上一节点的id
    	taskInfo2.setCode(userId);
    	taskInfo2.setTenantId("1");
    	taskInfo2.setSuspendStatus("自定义申请");
    	taskInfo2.setProcessInstanceId(Long.toString(FormId));
    	taskInfo2.setBusinessKey(businessKey);
    	taskInfo2.setCatalog("normal");
    	
    	taskInfoManager.save(taskInfo2);
    	
    	Map<String, Object> map=new HashMap<String, Object>();
    	
    	map.put("id", customEntity.getId());
    	map.put("theme", customEntity.getTheme());
    	
    	customConnector.doNotice(taskInfo2, userId, tenantHolder.getTenantId(), "create",map);
    		
    	//向oa_bpm_customPre表中记录同意的记录，用于驳回的时候找上一节点
		
		CustomPre customPre = new CustomPre();
		customPre.setAssignee(nextConfirmUserID);
		customPre.setPrevious(userId);
		customPre.setFormID(Long.toString(FormId));
		customPre.setCreateTime(new Date());
		customPreManager.save(customPre);
		
		//处理抄送
		if(!ccnos.isEmpty()){
			copySave(String.valueOf(customEntity.getId()),ccnos,ccName,taskInfo.getPresentationSubject(),userId);
		}
		//审批岗位
		customService.SetAuditPosition(businessKey);
	}
    
    // 处理 record 表中的结束时间
	private void updateRecordEndTime(TaskInfo getInfo) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String date = sdf.format(new Date());
		String sql = "update KV_RECORD set end_time='" + date + "' where id=" + getInfo.getBusinessKey();
		keyValueConnector.updateBySql(sql);
	}
	
    
    /**
     * 把数据先保存到keyvalue里.
     */
    public FormParameter doSaveRecord(HttpServletRequest request)
            throws Exception {
        String userId = currentUserHolder.getUserId();
        String tenantId = tenantHolder.getTenantId();

        MultipartHandler multipartHandler = new MultipartHandler(
                multipartResolver);
        FormParameter formParameter = null;
        String area = request.getParameter("area");
        if("null".equals(area)){
        	area = "";
        }
        try {
            multipartHandler.handle(request);
            logger.debug("multiValueMap : {}",
                    multipartHandler.getMultiValueMap());
            logger.debug("multiFileMap : {}",
                    multipartHandler.getMultiFileMap());

            formParameter = this.buildFormParameter(multipartHandler);
            formParameter.setArea(area);//sjx
            formParameter.setBpmProcessId("-1");   // 模拟流程
            formParameter.setBusinessKey("");
            String businessKey = operationService.saveDraft(userId, tenantId,
                    formParameter);

            if ((formParameter.getBusinessKey() == null)
                    || "".equals(formParameter.getBusinessKey().trim())) {
                formParameter.setBusinessKey(businessKey);
            }
            
            // TODO zyl 2017-11-16 外部表单不需要保存prop
            /*Record record = keyValueConnector.findByCode(businessKey);

            record = new RecordBuilder().build(record, multipartHandler,
                    storeConnector, tenantId);

            keyValueConnector.save(record);*/
        } finally {
            multipartHandler.clear();
        }

        return formParameter;
    }
    
    /**
     * 通过multipart请求构建formParameter.
     */
    public FormParameter buildFormParameter(MultipartHandler multipartHandler) {
        FormParameter formParameter = new FormParameter();
        formParameter.setMultiValueMap(multipartHandler.getMultiValueMap());
        formParameter.setMultiFileMap(multipartHandler.getMultiFileMap());
        formParameter.setBusinessKey(multipartHandler.getMultiValueMap()
                .getFirst("businessKey"));
        formParameter.setBpmProcessId(multipartHandler.getMultiValueMap()
                .getFirst("bpmProcessId"));
        formParameter.setHumanTaskId(multipartHandler.getMultiValueMap()
                .getFirst("humanTaskId"));
        formParameter.setComment(multipartHandler.getMultiValueMap().getFirst(
                "comment"));

        return formParameter;
    }

	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	@SuppressWarnings("unused")
	@Transactional(readOnly=false)
	public void copySave(String processInstanceId, String copyIds,
			String copyNames,String title,String curentUserId) throws Exception{
        //查询kv_record
		Record record = keyValueConnector.findByRef(processInstanceId);
		String presentationName = "自定义申请";
		Map<String, Object> queryForMap = null;
		//获取主题
		List<Map<String, Object>> titleList = null;
		List<TaskInfo> findBy = taskInfoManager.findBy("processInstanceId", processInstanceId);
		 try {
			titleList = jdbcTemplate.queryForList("select presentation_subject,presentation_name from task_info where process_instance_id='"+processInstanceId+"'");
		} catch (Exception e1) {
		}
		 if(null != titleList && titleList.size() > 0){
        	 Map<String, Object> map = titleList.get(0);
        	 
        	 String strTitle = StringUtil.toString(map.get("presentation_subject"));
        	 if(null != strTitle && !"".equals(strTitle) && !"null".equals(strTitle)){
        		 title = strTitle;
        	 }
        	 String strPresentationName = StringUtil.toString(map.get("presentation_name"));
        	 if(null != strPresentationName && !"".equals(strPresentationName) && !"null".equals(strPresentationName)){
        		 presentationName = strPresentationName;
        	 }
         }
		 String url = "";
		 String userId = "";
		 String businessTypeId ="";
		 String businessKey = "";
		 if(null != record){
			//判断属于哪个表
	        url = record.getUrl();
	        //发起人id
	        userId = record.getUserId();
	        //细分类型，区别自定义流程还是常规流程
	        businessTypeId = record.getBusinessTypeId();
	        businessKey = record.getBusinessKey();
		 }else{
			 try {
				 	Map<String, Object> queryForMap2 = jdbcTemplate.queryForMap("select applyCode from oa_bpm_customform where id = '"+processInstanceId+"'");
				 	String applyCode = queryForMap2.get("applyCode").toString();
				 	queryForMap = jdbcTemplate.queryForMap("select id,url,user_id,theme,businessTypeId,business_key from kv_record where applyCode = '"+applyCode+"'");
				} catch (Exception e1) {
				}
			//判断属于哪个表
	        url = StringUtil.toString(queryForMap.get("url"));
	        //发起人id
	        userId = StringUtil.toString(queryForMap.get("user_id"));
	        //细分类型，区别自定义流程还是常规流程
	        businessTypeId = StringUtil.toString(queryForMap.get("businessTypeId"));
	        businessKey = StringUtil.toString(queryForMap.get("business_key"));
		 }
		String[] copyIdList = null;  
		String[] copyNameList = null; 
		copyIdList = copyIds.split(",");
		copyNameList = copyNames.split(","); 
		Map<String, Object> map = new HashMap<String, Object>();
		//保存表单抄送人的id，name
		String formCopyIds = "";
		String formCopyNames = "";
		//非自定义
		List<Map<String, Object>> queryForListTask = null;
		map.put("businessTypeId", businessTypeId);
    	if(!"9999".equals(businessTypeId)){
    		try {
    			queryForListTask = jdbcTemplate.queryForList("select * from task_info where PROCESS_INSTANCE_ID = '"+processInstanceId+"' and CATALOG <> 'start';");
			} catch (Exception e) {
			}
    		
    	}
        for (int i = 0; i < copyIdList.length; i++) {
        	//1：岗位，2：人员
        	String positionType = "2";
        	
        	//处理抄送，保存进task_info
			String ccUserIds = copyIdList[i];
			String ccUserNames=copyNameList[i];
			String oldUserIds = "";
			if(ccUserIds.contains("岗位:")){
				oldUserIds = ccUserIds;
				positionType = "1";
				ccUserIds = ccUserIds.replaceAll("岗位:", "");
			}
			Map<String, Object> forMap = jdbcTemplate.queryForMap("select count(*) as count from task_info where assignee = '"+ccUserIds+"' and PROCESS_INSTANCE_ID = '"+processInstanceId+"' and catalog ='copy' ");
			if(!"0".equals(StringUtil.toString(forMap.get("count")))){
				continue;
			}
			/*List<TaskInfo> taskInfoList = taskInfoManager.find("from TaskInfo where assignee = ? and processInstanceId = ? and catalog ='copy'", ccUserIds,processInstanceId);
			if(null != taskInfoList && taskInfoList.size() > 0){
				continue;
			}*/
			TaskInfo taskInfo3 = new TaskInfo();
	    	//记录当前负责人id
	    	taskInfo3.setAssignee(ccUserIds);
	    	//记录当前负责人姓名
	    	taskInfo3.setName(ccUserNames+"有抄送待阅");
	    	//记录任务发起人
	    	taskInfo3.setDescription(userId);
	    	//创建时间
	    	taskInfo3.setCreateTime(new Date());
	    	//主题
	    	taskInfo3.setPresentationSubject(title);
	    	if("9999".equals(businessTypeId)){
	    		taskInfo3.setAction("自定义申请待阅");
	    		taskInfo3.setPresentationName(presentationName);
	    		taskInfo3.setSuspendStatus("自定义申请");
	    	}
	    	//处理状态
	    	taskInfo3.setStatus("active");
	    	taskInfo3.setTenantId("1");
	    	taskInfo3.setProcessInstanceId(processInstanceId);
	    	taskInfo3.setBusinessKey(businessKey);
	    	taskInfo3.setCatalog("copy");
	    	//非自定义
	    	if(!"9999".equals(businessTypeId)){
	    		if(null != queryForListTask && queryForListTask.size() > 0){
	    			Map<String, Object> taskMap = queryForListTask.get(0);
	    			taskInfo3.setCode(StringUtil.toString(taskMap.get("CODE")));
	    			taskInfo3.setName(StringUtil.toString(taskMap.get("NAME")));
	    			//taskInfo3.setPriority(Integer.parseInt(StringUtil.toString(taskMap.get("PRIORITY"))));
	    			taskInfo3.setCategory("copy");
	    			taskInfo3.setSuspendStatus(StringUtil.toString(taskMap.get("SUSPEND_STATUS")));
	    			taskInfo3.setDelegateStatus(StringUtil.toString(taskMap.get("DELEGATE_STATUS")));
	    			taskInfo3.setDuration(StringUtil.toString(taskMap.get("DURATION")));
	    			taskInfo3.setTaskId(StringUtil.toString(taskMap.get("TASK_ID")));
	    			taskInfo3.setExecutionId(StringUtil.toString(taskMap.get("EXECUTION_ID")));
	    			taskInfo3.setProcessDefinitionId(StringUtil.toString(taskMap.get("PROCESS_DEFINITION_ID")));
	    			taskInfo3.setProcessStarter(StringUtil.toString(taskMap.get("PROCESS_DEFINITION_ID")));
	    		}	    		
	    	}
	    	taskInfoManager.save(taskInfo3);
	    	
	    	Map<String,String> mapPosition=partyOrgConnector.getParentPartyEntityId(userId);
	    	//保存抄送表
	    	jdbcTemplate.execute("insert into task_info_approve_position (task_id,position_id,position_type,business_key,position_parentId,approve_position_name)"
	    			+ " values ('"+taskInfo3.getId()+"','"+ccUserIds+"','"+positionType+"','"+taskInfo3.getBusinessKey()+"',"+mapPosition.get("parent_id")+",'"+mapPosition.get("position_name")+"')");
	    	
	    	//发送消息
	    	if("1".equals(positionType)){
	    		//岗位查询人员
	    		List<Map<String, Object>> userList = null;
				try {
					userList = jdbcTemplate.queryForList("select e.id,name from party_entity e join party_struct s on e.id=s.CHILD_ENTITY_ID where  e.DEL_FLAG = '0' and s.PARENT_ENTITY_ID= '"+ccUserIds+"'");
				} catch (Exception e) {
				}
	    		if(null != userList && userList.size() > 0){
	    			for (Map<String, Object> userMap : userList) {
		    			String strUserId = StringUtil.toString(userMap.get("id"));
		    			if(curentUserId.equals(strUserId)){
		    				continue;
		    			}
		    			/*Map<String, Object> taskMap = jdbcTemplate.queryForMap("select count(*) as count from task_info where assignee = '"+strUserId+"' and PROCESS_INSTANCE_ID = '"+processInstanceId+"' and catalog ='copy' ");
		    			if("1".equals(taskMap.get("count").toString())){
		    				
		    			}*/
		    			TaskInfo taskInfoCount = taskInfoManager.findUnique("from TaskInfo where assignee = ? and processInstanceId = ? ", strUserId,processInstanceId);
	    				if(null != taskInfoCount){
	    					continue;
	    				}
		    			String strUserName = StringUtil.toString(userMap.get("name"));
		    			//保存抄送人
		    	    	map.put("strUserId", strUserId);
		    			customConnector.doNotice(taskInfo3, userId, tenantHolder.getTenantId(), "copy",map);
					}
	    		}
	    		formCopyIds += oldUserIds+",";
	    		Map<String, Object> queryCopyPostMap = null;
				try {
    	    		queryCopyPostMap = jdbcTemplate.queryForMap("select e.NAME from party_entity e where e.ID = '"+ccUserIds+"'");
				} catch (Exception e) {
				}
	    		String strCopyPostName = StringUtil.toString(queryCopyPostMap.get("NAME") == null ? "" : queryCopyPostMap.get("NAME"));
	    		String lastPost = getLastPost(ccUserIds);
	    		formCopyNames += lastPost+strCopyPostName+",";
	    	}else if("2".equals(positionType)){
	    		boolean boo = true;
	    		//通过用户查询岗位
	    		List<Map<String,Object>> queryForList = jdbcTemplate.queryForList("select e.* from party_entity e join party_struct s on e.id=s.PARENT_ENTITY_ID where  e.DEL_FLAG = '0' and TYPE_ID = '5' and s.CHILD_ENTITY_ID= '"+ccUserIds+"';");
	    		if(null != queryForList && queryForList.size() > 0){
	    			for (Map<String, Object> map2 : queryForList) {
	    				
	    				//查看该岗位是否已经发送过
	    				//Map<String, Object> taskMap = jdbcTemplate.queryForMap("select count(*) as count from task_info where assignee = '"+map2.get("id").toString()+"' and PROCESS_INSTANCE_ID = '"+processInstanceId+"' and catalog ='copy' ");
	    				/*if(!"0".equals(taskMap.get("count"))){
	    					boo = false;
	    				}*/
	    				TaskInfo taskInfoCount = taskInfoManager.findUnique("from TaskInfo where assignee = ? and processInstanceId = ? ", map2.get("id").toString(),processInstanceId);
	    				if(null != taskInfoCount){
	    					boo = false;
	    				}
	    			}
	    		}
	    		//未发送过消息
	    		if(boo){
	    			//保存抄送人
	    			formCopyIds += ccUserIds+",";
	    	    	formCopyNames += ccUserNames+",";
	    	    	//后期修改  ，未上线   
	    	    	map.put("strUserId", ccUserIds);
		    		customConnector.doNotice(taskInfo3, userId, tenantHolder.getTenantId(), "copy",map);
	    		}
	    	}
        }
        //修改表单的抄送人
        Map<String, Object> formCopyMap = null;
        if(!"".equals(url) && (url.contains("/operationCustom/") || url.contains("/workOperationCustom/"))){
        	try {
				formCopyMap = jdbcTemplate.queryForMap("select ccName,ccnos from oa_bpm_customform where id = '"+processInstanceId+"'");
			} catch (Exception e) {
			}
        	if(null != formCopyMap){
        		String ccName = StringUtil.toString(formCopyMap.get("ccName"));
        		String ccnos = StringUtil.toString(formCopyMap.get("ccnos"));
        		ccName = formCopyNames+ccName;
        		ccnos = formCopyIds+ccnos;
        		String strName = ccName.substring(ccName.length()-1,ccName.length() );
        		String strNos = ccnos.substring(ccnos.length()-1,ccnos.length() );
        		if(",".equals(strName)){
        			ccName = ccName.substring(0, ccName.length()-1);
        		}
        		if(",".equals(strNos)){
        			ccnos = ccnos.substring(0, ccnos.length()-1);
        		}
        		jdbcTemplate.execute("update oa_bpm_customform set ccName = '"+ccName+"' , ccnos = '"+ccnos+"' where id = '"+processInstanceId+"'");
        	}
        }else if(!"".equals(url) && url.contains("/operationOnlineOrder/")){
        	//在线办公，表单无抄送人
        	//return "ro_pf_oaonline";
        }else if(!"".equals(url) && url.contains("/processGroupBusiness/")){
        	//集团
        	//entity_group_business
        	try {
				formCopyMap = jdbcTemplate.queryForMap("select cc as ccName,copy_user_value as ccnos from entity_group_business where process_instance_id = '"+processInstanceId+"'");
			} catch (Exception e) {
			}
        	if(null != formCopyMap){
        		String ccName = StringUtil.toString(formCopyMap.get("ccName"));
        		String ccnos = StringUtil.toString(formCopyMap.get("ccnos"));
        		ccName = formCopyNames+ccName;
        		ccnos = formCopyIds+ccnos;
        		String strName = ccName.substring(ccName.length()-1,ccName.length() );
        		String strNos = ccnos.substring(ccnos.length()-1,ccnos.length() );
        		if(",".equals(strName)){
        			ccName = ccName.substring(0, ccName.length()-1);
        		}
        		if(",".equals(strNos)){
        			ccnos = ccnos.substring(0, ccnos.length()-1);
        		}
        		jdbcTemplate.execute("update entity_group_business set cc = '"+ccName+"' , copy_user_value = '"+ccnos+"' where process_instance_id = '"+processInstanceId+"'");
        	}
        }else if(!"".equals(url) && url.contains("/processBusiness/")){
        	//分公司
        	//entity_business
        	try {
				formCopyMap = jdbcTemplate.queryForMap("select cc as ccName,copy_user_value as ccnos from entity_business where process_instance_id = '"+processInstanceId+"'");
			} catch (Exception e) {
			}
        	if(null != formCopyMap){
        		String ccName = StringUtil.toString(formCopyMap.get("ccName"));
        		String ccnos = StringUtil.toString(formCopyMap.get("ccnos"));
        		ccName = formCopyNames+ccName;
        		ccnos = formCopyIds+ccnos;
        		String strName = ccName.substring(ccName.length()-1,ccName.length() );
        		String strNos = ccnos.substring(ccnos.length()-1,ccnos.length() );
        		if(",".equals(strName)){
        			ccName = ccName.substring(0, ccName.length()-1);
        		}
        		if(",".equals(strNos)){
        			ccnos = ccnos.substring(0, ccnos.length()-1);
        		}
        		jdbcTemplate.execute("update entity_business set cc = '"+ccName+"' , copy_user_value = '"+ccnos+"' where process_instance_id = '"+processInstanceId+"'");
        	}
        }else if(!"".equals(url) && url.contains("/operationApply/")){
        	//常规，非常规，表单无抄送人
        	//oa_bpm_commapply
        }else if(!"".equals(url) && url.contains("/operationCancelOrder/")){
        	//撤单
        }else if(!"".equals(url) && url.contains("/processFreeze/")){
        	//冻结解冻，表单无抄送人
        	//entity_freeze
        }else if(!"".equals(url) && url.contains("/Invoice/")){
        	//发票，表单无抄送人
        	//entity_invoice
        }else if(!"".equals(url) && url.contains("/Return/")){
        	//退货，无抄送人
        	//entity_return
        }else if(!"".equals(url) && url.contains("/processLllegalFreeze/")){
        	//违规冻结解冻
        	//entity_lllegal_freeze
        	try {
				formCopyMap = jdbcTemplate.queryForMap("select cc as ccName,copy_user_value as ccnos from entity_lllegal_freeze where process_instance_id = '"+processInstanceId+"'");
			} catch (Exception e) {
			}
        	if(null != formCopyMap){
        		String ccName = StringUtil.toString(formCopyMap.get("ccName"));
        		String ccnos = StringUtil.toString(formCopyMap.get("ccnos"));
        		ccName = formCopyNames+ccName;
        		ccnos = formCopyIds+ccnos;
        		String strName = ccName.substring(ccName.length()-1,ccName.length() );
        		String strNos = ccnos.substring(ccnos.length()-1,ccnos.length() );
        		if(",".equals(strName)){
        			ccName = ccName.substring(0, ccName.length()-1);
        		}
        		if(",".equals(strNos)){
        			ccnos = ccnos.substring(0, ccnos.length()-1);
        		}
        		jdbcTemplate.execute("update entity_lllegal_freeze set cc = '"+ccName+"' , copy_user_value = '"+ccnos+"' where process_instance_id = '"+processInstanceId+"'");
        	}
        }
	}
    /**
     * 查询抄送人
     * @param processInstanceId  流程实例id
     * @param url 
     * @return
     */
/*	public String getCopyName(String processInstanceId){
        String ccName = "";
        ccName = getTaskCopyNames(processInstanceId);
        if(!"".equals(ccName)){
        	String str = ccName.substring(ccName.length()-1,ccName.length() );
    		if(",".equals(str)){
    			ccName = ccName.substring(0, ccName.length()-1);
    		}
        }
        return ccName;
	}*/
/*	*//**
	 * 查询task_info
	 * @param processInstanceId
	 * @param ccName
	 * @return
	 *//*
	private String queryCopyNameByTask(String processInstanceId, String ccName) {
		List<Map<String, Object>> taskInfoList = jdbcTemplate.queryForList("select * from task_info t where t.PROCESS_INSTANCE_ID = '"+processInstanceId+"' and t.CATALOG = 'copy'");
		
		if(null != taskInfoList && taskInfoList.size() > 0){
			for (Map<String, Object> map : taskInfoList) {
				String taskId = StringUtil.toString(map.get("ID"));
				String assigaee = StringUtil.toString(map.get("ASSIGNEE"));
				
				Map<String, Object> queryForMap = jdbcTemplate.queryForMap("select * from task_info_approve_position a where a.task_id = '"+taskId+"'");
				String positionType = StringUtil.toString(queryForMap.get("position_type"));
				if("1".equals(positionType)){
					//岗位
					//Map<String, Object> queryCopyMap = jdbcTemplate.queryForMap("select * from party_entity e LEFT JOIN party_struct s ON e.ID = s.PARENT_ENTITY_ID where s.CHILD_ENTITY_ID = '"+assigaee+"';");
					Map<String, Object> queryCopyPostMap = jdbcTemplate.queryForMap("select e.NAME from party_entity e where e.ID = '"+assigaee+"'");
					//String strCopyName = StringUtil.toString(queryCopyMap.get("NAME"));
					String strCopyPostName = StringUtil.toString(queryCopyPostMap.get("NAME"));
					String lastPost = getLastPost(assigaee);
					ccName += lastPost+strCopyPostName+",";
				}else if("2".equals(positionType)){
					ccName += userConnector.findById(assigaee).getDisplayName()
		            + ",";
				}
			}
		}
		return ccName;
	}*/
	/**
	 * 查询抄送人
	 * @param processInstanceId 流程实例id
	 * @return
	 */
	public String getTaskCopyNames(String processInstanceId) {
		List<Map<String,Object>> queryForList = jdbcTemplate.queryForList("select id, assignee from task_info where process_instance_id='"+processInstanceId+"' and catalog ='copy' order by create_time asc");
        String notifynames = "";
        if (!CollectionUtils.isEmpty(queryForList)) {
            for (Map<String,Object> map : queryForList) {
           	 String taskId = StringUtil.toString(map.get("id"));
           	 String str = StringUtil.toString(map.get("assignee"));
           	 if(null != str && !"".equals(str) && null != taskId && !"".equals(taskId) ){
           		 Map<String, Object> queryForMap = null;
					try {
						queryForMap = jdbcTemplate.queryForMap("select position_type from task_info_approve_position where task_id = '"+taskId+"'");
					} catch (Exception e) {
					}
					if(null != queryForMap){
						 String positionId = StringUtil.toString(queryForMap.get("position_type"));
                   	 if("1".equals(positionId)){
               		 	//岗位
        	    		//Map<String, Object> queryCopyMap = null;
        	    		Map<String, Object> queryCopyPostMap = null;
						try {
							//queryCopyMap = jdbcTemplate.queryForMap("select * from party_entity e LEFT JOIN party_struct s ON e.ID = s.PARENT_ENTITY_ID where s.CHILD_ENTITY_ID = '"+str+"';");
            	    		queryCopyPostMap = jdbcTemplate.queryForMap("select e.NAME from party_entity e where e.ID = '"+str+"'");
						} catch (Exception e) {
						}
						String lastPost = getLastPost(str);
        	    		String strCopyPostName = StringUtil.toString(queryCopyPostMap.get("NAME") == null ? "" : queryCopyPostMap.get("NAME"));
        	    		notifynames += lastPost+strCopyPostName+",";
                   	 }else if("2".equals(positionId)){
                   		 notifynames += userConnector.findById(str).getDisplayName()
                                    + ",";
                   	 }
					}else{
						notifynames += userConnector.findById(str).getDisplayName()
                               + ",";
					}
           	 }else{
           		 continue;
           	 }
          }
        }
        if (!"".equals(notifynames)){
        	String str = notifynames.substring(notifynames.length()-1,notifynames.length() );
    		if(",".equals(str)){
    			notifynames = notifynames.substring(0, notifynames.length()-1);
    		}
        }
        return notifynames;
	}
	/**
	 * 通过当前岗位id获取上一级岗位
	 * @param id
	 * @return
	 */
	public String getLastPost(String id){
		Map<String, Object> queryCopyMap = null;
		try {
			queryCopyMap = jdbcTemplate.queryForMap("select e.NAME from party_entity e LEFT JOIN party_struct s ON e.ID = s.PARENT_ENTITY_ID where s.STRUCT_TYPE_ID = 1 and e.DEL_FLAG = 0 and e.IS_DISPLAY = 1 and s.CHILD_ENTITY_ID = '"+id+"';");
		} catch (DataAccessException e) {
		}
		String strCopyName = "";
		if(null != queryCopyMap){
			if(StringUtils.isNotBlank(StringUtil.toString(queryCopyMap.get("NAME")))){
				strCopyName = StringUtil.toString(queryCopyMap.get("NAME"))+"-";
			}
		}
		if (StringUtils.isNotBlank(strCopyName)){
        	String str = strCopyName.substring(strCopyName.length()-1,strCopyName.length() );
    		if(",".equals(str)){
    			strCopyName = strCopyName.substring(0, strCopyName.length()-1);
    		}
        }
		return strCopyName;
	}
	/**
	 * 查询下下步审核人
	 * @param userId 登陆人id
	 * @param businessKey
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public String getDownAuditor(String userId,String businessKey){
		String DownAuditorName = "";
		
		List<CustomApprover> approverListNow=customApproverManager.find(
				"from CustomApprover where businessKey=? and approverId=? and opterType !='2' and opterType !='3'", 
				businessKey,Long.parseLong(userId));
        if(null != approverListNow && approverListNow.size() > 0){
        	CustomApprover customApproverNow = approverListNow.get(0);
        	//获取所有下一步审批步骤
			List<CustomApprover> nextApproverList=customApproverManager.find(
					"from CustomApprover where businessKey=? and approveStep>? and opterType !='2' and opterType !='3' order by approveStep", 
					businessKey,
					customApproverNow.getApproveStep());
			if(null != nextApproverList && nextApproverList.size() > 1){
				CustomApprover customApprover2 = nextApproverList.get(1);
				String id = String.valueOf(customApprover2.getApproverId());
				DownAuditorName = userConnector.findById(id).getDisplayName();
			}
        }
		return DownAuditorName;
	}
	/**
	 * 获取启明公益的特殊细分
	 * @param businessDetailId
	 * @return
	 */
	public boolean getBusinessDetail(String businessDetailId) {
		boolean boo = false;
    	String sql = "select t.name,t.descn,i.value from dict_type t LEFT JOIN dict_info i ON t.id = i.TYPE_ID where t.`NAME` = 'qimingBusinessDetail';";
    	List<Map<String,Object>> queryForList = jdbcTemplate.queryForList(sql);
    	if(null != queryForList && queryForList.size() > 0){
    		for (Map<String, Object> map2 : queryForList) {
    			String string = StringUtil.toString(map2.get("value"));
    			if(businessDetailId.equals(string)){
    				boo = true;
    				break;
    			}
			}
    	}
    	return boo;
	}
	/**
	 * 查询数据字典
	 * @param dictName
	 */
	public String getDict(String dictName) {
		String value = "";
		String sql = "select t.name,t.descn,i.value from dict_type t LEFT JOIN dict_info i ON t.id = i.TYPE_ID where t.`NAME` = '"+dictName+"';";
    	List<Map<String,Object>> queryForList = jdbcTemplate.queryForList(sql);
    	if(null != queryForList && queryForList.size() > 0){
    		Map<String, Object> map = queryForList.get(0);
    		value = StringUtil.toString(map.get("value"));
    	}
		return value;
	}
	 /**
     * 查询用户是否绑定了考勤组
     * @return
     */
	public boolean checkedAttendanceRecords(String userId) {
    	String sql = "select p.* from person_attendance_records p LEFT JOIN attendance_records a on p.attendanceRecordsID = a.id where p.personID = '"+userId+"';";
    	try {
    		Map<String, Object> queryForMap = jdbcTemplate.queryForMap(sql);
		} catch (DataAccessException e) {
			return false;
		}
    	return true;
	}
    
}
