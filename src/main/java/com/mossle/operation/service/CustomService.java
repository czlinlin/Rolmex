package com.mossle.operation.service;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartResolver;

import com.alibaba.fastjson.JSON;
import com.mossle.api.custom.CustomConnector;
import com.mossle.api.keyvalue.FormParameter;
import com.mossle.api.keyvalue.KeyValueConnector;
import com.mossle.api.org.OrgConnector;
import com.mossle.api.tenant.TenantHolder;
import com.mossle.common.utils.FileUploadAPI;
import com.mossle.core.MultipartHandler;
import com.mossle.core.auth.CurrentUserHolder;
import com.mossle.core.mapper.JsonMapper;
import com.mossle.core.util.StringUtils;
import com.mossle.humantask.persistence.domain.TaskInfo;
import com.mossle.humantask.persistence.manager.TaskInfoManager;
import com.mossle.keyvalue.persistence.domain.RecordInfo;
import com.mossle.keyvalue.persistence.manager.RecordManager;
import com.mossle.msg.persistence.domain.MsgInfo;
import com.mossle.msg.persistence.manager.MsgInfoManager;
import com.mossle.operation.persistence.domain.CustomApprover;
import com.mossle.operation.persistence.domain.CustomEntity;
import com.mossle.operation.persistence.domain.CustomEntityDTO;
import com.mossle.operation.persistence.domain.CustomPre;
import com.mossle.operation.persistence.domain.CustomWorkEntityDTO;
import com.mossle.operation.persistence.manager.CustomApproverManager;
import com.mossle.operation.persistence.manager.CustomManager;
import com.mossle.operation.persistence.manager.CustomPreManager;
import com.mossle.operation.web.ProcessOperationController;
import com.mossle.party.persistence.domain.PartyEntity;
import com.mossle.party.persistence.domain.PartyStruct;
import com.mossle.party.persistence.manager.PartyEntityManager;
import com.mossle.party.persistence.manager.PartyStructManager;
import com.mossle.party.service.OrgService;
import com.mossle.party.support.PartyOrgConnector;
import com.mossle.user.PersonInfoConstants;
import com.mossle.user.persistence.domain.AccountInfo;
import com.mossle.user.persistence.domain.PersonInfo;
import com.mossle.user.persistence.domain.RosterLog;
import com.mossle.user.persistence.domain.UpdatePerson;
import com.mossle.user.persistence.manager.AccountInfoManager;
import com.mossle.user.persistence.manager.PersonInfoManager;
import com.mossle.user.persistence.manager.RosterLogManager;
import com.mossle.user.persistence.manager.UpdatePersonManager;
import com.mossle.user.service.PersonInfoService;
import com.mossle.user.service.UserService;
/**
 * @author chengze:
 * @version 创建时间：2017年9月14日 下午6:53:56
 * 自定义申请发起流程和审批
 */
@Service
@Transactional(readOnly = true)
public class CustomService {


    private static Logger logger = LoggerFactory.getLogger(ProcessOperationController.class);
    
    public static final int STATUS_DRAFT_PROCESS = 0;
    public static final int STATUS_DRAFT_TASK = 1;
    public static final int STATUS_RUNNING = 2;
    private OperationService operationService;
    private KeyValueConnector keyValueConnector;

    private CurrentUserHolder currentUserHolder;

    private MultipartResolver multipartResolver;
    private JsonMapper jsonMapper = new JsonMapper();
    private TenantHolder tenantHolder;
    private CustomManager customManager;
    private TaskInfoManager taskInfoManager;
    private CustomPreManager customPreManager;
    private FileUploadAPI fileUploadAPI;

    private CustomApproverManager customApproverManager;
    private OrgConnector orgConnector;
    private CustomConnector customConnector;

    @Resource
    private MsgInfoManager msgInfoManager;
    private UpdatePersonManager updatePersonManager ;
    private PersonInfoService personInfoService;
    private RosterLogManager rosterLogManager ;
    private UserService userService;
    private PartyEntityManager partyEntityManager;
    private OrgService orgService;
    private PartyStructManager partyStructManager;
    private RecordManager recordManager;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private CustomWorkService customWorkService;
    private PartyOrgConnector partyOrgConnector;
    @Autowired
    private PersonInfoManager personInfoManager;
    @Autowired
    private AccountInfoManager accountInfoManager;
    
    /**
     * 发起流程.  自定义申请使用
     */
    @Transactional(readOnly = false)
    public void StartProcessCustom(HttpServletRequest request,
			CustomEntityDTO customEntityDTO,String areaName, MultipartFile[] files,CustomEntity customEntity)
			throws Exception, IOException {
		if(areaName == null){
			areaName = "";
		}
		String businessKey;
		String userId = currentUserHolder.getUserId();
        String curentUserName = currentUserHolder.getName();
        
        FormParameter formParameter = this.doSaveRecord(request);
        
        businessKey = formParameter.getBusinessKey();
        
        //抄送人id和姓名
        String ccnos = request.getParameter("ccnos");
        String ccName = request.getParameter("ccName");
        
        
        //CustomEntity customEntity = new CustomEntity();
        customEntity.setTheme(customEntityDTO.getTheme().toString().replace("'", ""));
        customEntity.setApplyCode(customEntityDTO.getApplyCode());
        customEntity.setUserId(Long.parseLong(userId));
        String tenantId = tenantHolder.getTenantId();
        
        customEntity.setBusinessDetail(customEntityDTO.getBusinessDetail());
        customEntity.setName(customEntityDTO.getName());
        customEntity.setApplyContent(customEntityDTO.getApplyContent().toString().replace("'", ""));
        customEntity.setBusinessType(customEntityDTO.getBusinessType());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	String str = sdf.format(new Date());
        customEntity.setCreateTime(str);
        customEntity.setModifyTime(str);
        customEntity.setSubmitTimes(1);
       // customEntity.setCcName(ccName);
        //customEntity.setCcnos(ccnos);
        //add by lilei at 2017.11.27
        String nextId=request.getParameter("nextID");
        String nextUser=request.getParameter("nextUser");
        String strBusiLevel=orgConnector.getPositionMaxGradeByUserIds(nextId);
        customEntity.setBusinessLevel(strBusiLevel);
        customManager.save(customEntity);
        
        //添加审批人add by lilei at 2017.11.27
        String[] auditList=nextId.split(",");
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
        
        SaveFormHtml(customEntity,businessKey);
        
        /*String sqlRecordUpdate = "update KV_RECORD set apply_content= '" + customEntity.getApplyContent() +
        		"',theme ='" + customEntity.getTheme() +
        		"' where id= " + businessKey;
    	keyValueConnector.updateBySql(sqlRecordUpdate);*/
        
        //处理受理单编号
        operationService.deleteApplyCode(customEntityDTO.getApplyCode());
        
        //保存附件
        fileUploadAPI.uploadFile(files, tenantId, Long.toString(customEntity.getId()), "operation/CustomApply");
        
        //记录表单的主键ID
        Long FormId =customEntity.getId();
        //发起流程
        this.customStartApply(customEntity,curentUserName, userId, auditNameList[0], 
        		auditList[0], FormId, businessKey, ccnos, ccName);
	}
    
   //发起流程. 花名册 及 组织架构   使用  cz20180528
   @Transactional(readOnly = false)
   public void StartProcessCustomForPerson(HttpServletRequest request,
			String perinfoUserName,String content,String areaName, MultipartFile[] files
			,String flag)
			throws Exception, IOException {
		if(areaName == null){
			areaName = "";
		}
		String businessKey;
		String userId = currentUserHolder.getUserId();
       String curentUserName = currentUserHolder.getName();
       
       FormParameter formParameter = this.doSaveRecord(request);
       
       businessKey = formParameter.getBusinessKey();
       
       //抄送人id和姓名
       String ccnos ="";
       String ccName = "";
       
       CustomEntity customEntity = new CustomEntity();

       switch (flag) {
		case "1":
			customEntity.setTheme("[花名册]新员工录入 "
//		+perinfoUserName
					);
			break;
		case "2":
			customEntity.setTheme("修改花名册 "
//		+perinfoUserName
					);
			break;
		case "3":
			customEntity.setTheme("职员岗位调整  "
//		+perinfoUserName
		);
			break;
		case "4":
			customEntity.setTheme("新建组织机构 ");
			break;
		case "5":
			customEntity.setTheme("修改组织机构 ");
			break;
		case "6":
			customEntity.setTheme("岗位关联人员 ");
			break;
		}
       /*if(flag.equals("1")){
    	   	customEntity.setTheme("  [花名册]新员工录入： "+perinfoUserName);
       		//customEntity.setApplyType("1");
       }
       else if(flag.equals("2")){
	   		customEntity.setTheme("  修改花名册 ： "+perinfoUserName);
   			//customEntity.setApplyType("2");
       }
       else if(flag.equals("3")){
    	   customEntity.setTheme("  职员岗位调整 ： "+perinfoUserName);
    	   //customEntity.setApplyType("3");
       }
       else if(flag.equals("4")){
    	   customEntity.setTheme(curentUserName +"  新建组织机构 ");
    	   //customEntity.setApplyType("4");
       }
       else if(flag.equals("5")){
    	   customEntity.setTheme(curentUserName +"  修改组织机构 ");
    	   //customEntity.setApplyType("4");
       }
       else if(flag.equals("6")){
    	   customEntity.setTheme(curentUserName +"  岗位关联人员 ");
    	   //customEntity.setApplyType("4");
       }*/
//       else {
//    	   customEntity.setApplyType("0");
//       }
       
       customEntity.setApplyCode(request.getParameter("applyCode"));
       customEntity.setUserId(Long.parseLong(userId));
       String tenantId = tenantHolder.getTenantId();
       
       customEntity.setBusinessDetail("自定义申请");
       customEntity.setName(curentUserName);
       customEntity.setApplyContent(content.replace("'", ""));
       customEntity.setBusinessType("自定义");
       SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
   	   String str = sdf.format(new Date());
       customEntity.setCreateTime(str);
       customEntity.setModifyTime(str);
       customEntity.setSubmitTimes(1);
       customEntity.setCcName(ccName);
       customEntity.setCcnos(ccnos);
       //add by lilei at 2017.11.27
       String nextId=request.getParameter("nextID");
       String nextUser=request.getParameter("nextUser");
       String strBusiLevel=orgConnector.getPositionMaxGradeByUserIds(nextId);
       customEntity.setBusinessLevel(strBusiLevel);
       customManager.save(customEntity);
       
       //添加审批人add by lilei at 2017.11.27
       String[] auditList=nextId.split(",");
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
       
       SaveFormHtml(customEntity,businessKey);
       
   		//原jdbc更新record操作更改如下，更新的属性保持一致  TODO sjx 18.11.23
        RecordInfo recordInfo = recordManager.findUniqueBy("businessKey", businessKey);
        recordInfo.setTheme(customEntity.getTheme().toString().replace("'", ""));
        recordInfo.setApplyContent(customEntity.getApplyContent().toString().replace("'", ""));
    	recordManager.save(recordInfo);
   		
       //处理受理单编号
       operationService.deleteApplyCode(request.getParameter("applyCode"));
       
       //保存附件
       fileUploadAPI.uploadFile(files, tenantId, Long.toString(customEntity.getId()), "operation/CustomApply");
       
       //记录表单的主键ID
       Long FormId =customEntity.getId();
       
       //发起流程
       this.customStartApply(customEntity,curentUserName, userId, auditNameList[0], 
       		auditList[0], FormId, businessKey, ccnos, ccName);
	}
   
   /**
    * 设置审批流程的流程状态
    * add BY LILEI AT 2018-08-31
    * 
    * **/
   //@Transactional(readOnly=false)
   @SuppressWarnings("unchecked")
public void SetProcessAuditStatus(String strOpterComment,String strBusinessKey){
	   	/*String strOpterComment=taskParameters.get("leaderComment").toString();
		String strBusinessKey=formParameter.getBusinessKey();*/
		RecordInfo recordInfo=recordManager.findUniqueBy("businessKey", strBusinessKey);
		String hsqlString="FROM TaskInfo WHERE businessKey=? AND status='active' AND catalog='normal'";
		List<TaskInfo> taskList=taskInfoManager.find(hsqlString, 
													strBusinessKey);
		if(strOpterComment.equals("同意")){
			recordInfo.setAuditStatus("1");
			
			/*String hsqlString="FROM TaskInfo WHERE businessKey=? AND status='active' AND catalog='normal'";
			List<TaskInfo> taskList=taskInfoManager.find(hsqlString, 
														strBusinessKey);*/
			if(taskList==null||(taskList!=null&&taskList.size()<1)){
				recordInfo.setAuditStatus("2");
				
				String hsql="FROM TaskInfo WHERE businessKey=?  AND catalog='copy'";
				List<TaskInfo> copyList=taskInfoManager.find(hsql,strBusinessKey);
				for (TaskInfo info : copyList) {
					//String 
					String hsqlMsg = "FROM MsgInfo WHERE data=? and isSendMsg='2'";
					List<MsgInfo> msgList = msgInfoManager.find(hsqlMsg, info.getId().toString());
					for (MsgInfo msgInfo : msgList) {
						msgInfo.setIsSendMsg("0");
						msgInfoManager.update(msgInfo);
					}
				}
			}
		}
		else if(strOpterComment.equals("驳回")){
			recordInfo.setAuditStatus("4");
			
			TaskInfo taskInfo=taskList.get(0);
			hsqlString=" FROM TaskInfo WHERE businessKey=? AND catalog='start' ";
			TaskInfo taskInfoStart=taskInfoManager.findUnique(hsqlString, strBusinessKey);
			if(taskInfo.getAssignee()!=null){
				if(taskInfo.getAssignee().equals(taskInfoStart.getAssignee())){
					/*hsqlString="FROM CustomApprover WHERE businessKey=? and approverId=? and opterType=?";
					List<CustomApprover> customApproverList=taskInfoManager.find(hsqlString,
																		 strBusinessKey,
																		 taskInfo.getAssignee());
					if(customApproverList!=null){
						if(customApproverList.size()>0){
							if(customApproverList.get(0).getApproveStep()==1){
								recordInfo.setAuditStatus("7");
							}
						}
					}*/
					
					recordInfo.setAuditStatus("7");
				}
			}
		}
		else if(strOpterComment.equals("不同意")){
			recordInfo.setAuditStatus("3");
		}
		else if(strOpterComment.equals("撤销申请")){
			recordInfo.setAuditStatus("6");
		}
		else if(strOpterComment.equals("调整申请")
				||strOpterComment.equals("重新申请")
				||strOpterComment.equals("重新调整申请")){
			recordInfo.setAuditStatus("1");
		}
		recordManager.save(recordInfo);
   }
   
   /**
    * 设置自定义审批的岗位信息（实际是人员的信息）
    * **/
   @Transactional(readOnly=false)
   public void SetAuditPosition(String strBusinessKey){
	   // 必须保留此查询，要不然下面得jdbc查询不到结果 。原因未查明
	   List<TaskInfo> taskInfoList=taskInfoManager.findBy("businessKey", strBusinessKey);
	   String strSql=String.format("SELECT i.ID AS TASK_INFO_ID,i.ASSIGNEE FROM task_info i"
					 +"	LEFT JOIN task_info_approve_position p"
					 +"	ON i.ID=p.task_id"
					 +"	WHERE i.BUSINESS_KEY='%s' AND CATALOG<>'copy' AND p.task_id IS NULL",strBusinessKey);
	   List<Map<String,Object>> mapTaskList=jdbcTemplate.queryForList(strSql);
	   if(mapTaskList!=null){
		   if(mapTaskList.size()>0){
			   for(Map<String,Object> mapTask:mapTaskList){
				    if(mapTask.get("ASSIGNEE")!=null){
				    	if(!mapTask.get("ASSIGNEE").toString().equals("")){
				    		Map<String,String> mapPosition=partyOrgConnector.getParentPartyEntityId(mapTask.get("ASSIGNEE").toString());
				    		String strStartInsertSql=String.format("insert into task_info_approve_position(task_id,position_id,position_type,BUSINESS_KEY,position_parentId,approve_position_name) "
									+" values(%s,%s,'%s','%s',%s,'%s')",
									mapTask.get("TASK_INFO_ID"),
									mapTask.get("ASSIGNEE"),
									'2',
									strBusinessKey,
									mapPosition.get("parent_id"),
									mapPosition.get("position_name")
									);
		    				keyValueConnector.updateBySql(strStartInsertSql);
				    	}
				    }
			   }
		   }
	   }
	   
   }
    
    public void SaveFormHtml(CustomEntity customEntity,String bussinessKey){
    	String detailHtml="<table  style=\"line-height:34px;\"  class=\"centerdiv\" cellpadding=\"0\" cellspacing=\"0\" >"
    			 +"<tr>"
    			 +"<td colspan=\"4\" align=\"center\"><h2>申请单</h2></td>"
    			 +"</td>"
    	        +"</tr>"
    	        +"<tr>"
    	            +"<td class=\"f_td\">主题：</td>"
    	            +"<td class=\"f_r_td\" colspan=\"3\">"
    	               	 +customEntity.getTheme().toString().replace("'", "")
	               	+"</td>"
           	   +"</tr>"
           	   +"<tr>"
		            +"<td class=\"f_td\">抄送：</td>"
		            +"<td class=\"f_r_td\" colspan=\"3\">"
		            +"{copyNames}"
		               	 /*+(customEntity.getCcName()==null?"":customEntity.getCcName())*/
		           	+"</td>"
		   	  +"</tr>"
			  +"<tr>"
    	    	    +"<td class=\"f_td\">申请业务类型：</td>"
    	            +"<td class=\"f_r_td\">"+customEntity.getBusinessType()+"</td>"
    	            +"<td class=\"f_td\">业务细分：</td>"
    	            +"<td class=\"f_r_td\">"+customEntity.getBusinessDetail()+"</td>"
    	      +"</tr>"
    	      +"<tr>"
    	      		+"<td class=\"f_td\">业务级别：</td>"
	      			+"<td class=\"f_r_td\">"+customEntity.getBusinessLevel()+"</td>"
    	            +"<td class=\"f_td\">发起人：</td>"
    	            +"<td class=\"f_r_td\">"+customEntity.getName()+"</td>"
             +"</tr>"
             +"<tr>"
	    	        +"<td class=\"f_td\" colspan=\"4\" align=\"center\">申请内容</td>"
    	     +"</tr>"
    	     +"<tr>"
    	            +"<td class=\"f_td\"  colspan=\"4\" style=\"text-align:left;vertical-align:top;padding:3px 3px 3px 3px;\">"
    	            + "<pre style=\"white-space: pre-wrap;word-wrap: break-word;\">"+(customEntity.getApplyContent()==null?"":(customEntity.getApplyContent().toString().replace("'", "")))+"</pre></td>"
    	    +"</tr>"
    	    +"<tr>"
	 	        +"<td class=\"f_td\" colspan=\"4\" align=\"center\">审核人</td>"
		     +"</tr>"
		     +"<tr>"
	            +"<td class=\"f_td\"  colspan=\"4\" >"
	            + "<ul style=\"width:96%;margin:0 auto;list-style:none;\">{auditor}</ul></td>"
             +"</tr>"
    	    +"</table>";
    	
    	RecordInfo recordInfo=recordManager.findUniqueBy("businessKey", bussinessKey);
    	if(recordInfo != null){
    		recordInfo.setPkId(customEntity.getId().toString());
    		recordInfo.setRef(customEntity.getId().toString());
    		recordInfo.setDetailHtml(detailHtml);
    	}
       	recordManager.save(recordInfo);
    }
    
    /**
     * 发起自定义流程
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
    	taskInfo.setName(curentUserName+"-发起自定义申请");
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
    	taskInfo.setAction("发起自定义申请");
    	//主题
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	String str = sdf.format(new Date());
    	taskInfo.setPresentationSubject("自定义申请-"+curentUserName+"-"+str);
    	taskInfo.setPresentationName("自定义申请");
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
    	taskInfo2.setAction("自定义申请等待审批");
    	//主题
    	taskInfo2.setPresentationSubject(taskInfo.getPresentationSubject());
    	taskInfo2.setPresentationName("自定义申请");
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
    	//发送消息 add by lilei at 2017.11.29
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
			customWorkService.copySave(String.valueOf(customEntity.getId()), ccnos, ccName, customEntity.getTheme(),userId);
			/*String[] ccUserName = null;  
			String[] ccUserId = null;  
			ccUserId = ccnos.split(","); 
			ccUserName=ccName.split(",");
			for(int i=0;i<ccUserId.length;i++){
			
				String ccUserIds = ccUserId[i];
				String ccUserNames=ccUserName[i];
				
	    	TaskInfo taskInfo3 = new TaskInfo();
	    	
	    	//记录当前负责人id
	    	taskInfo3.setAssignee(ccUserIds);
	    	
	    	//记录当前负责人姓名
	    	taskInfo3.setName(ccUserNames+"有抄送待阅");
	    	
	    	//记录任务发起人
	    	taskInfo3.setDescription(userId);
	    	//创建时间
	    	taskInfo3.setCreateTime(new Date());
	    	taskInfo3.setAction("自定义申请待阅");
	    	//主题
	    	taskInfo3.setPresentationSubject(taskInfo.getPresentationSubject());
	    	taskInfo3.setPresentationName("自定义申请");
	    	//处理状态
	    	taskInfo3.setStatus("active");
	    	
	    	taskInfo3.setTenantId("1");
	    	taskInfo3.setSuspendStatus("自定义申请");
	    	taskInfo3.setProcessInstanceId(Long.toString(FormId));
	    	taskInfo3.setBusinessKey(businessKey);
	    	taskInfo3.setCatalog("copy");
	    	taskInfoManager.save(taskInfo3);
	    	//发送消息 add by lilei at 2017.11.29
	    	customConnector.doNotice(taskInfo3, userId, tenantHolder.getTenantId(), "copy",map);
			}*/
		}
		
		SetAuditPosition(businessKey);
	}
   
    /**
     * 完成任务 pc端使用.
     */
    @Transactional(readOnly = false)
	public void CompleteTaskCustomPC(HttpServletRequest request,
			CustomEntityDTO customEntityDTO,CustomWorkEntityDTO customWorkEntityDTO, String processInstanceId,
			MultipartFile[] files, String iptdels, String flag)
			throws Exception, IOException {
		//获得当前登录人的ID和姓名
    	String userId = currentUserHolder.getUserId();
    	String name = currentUserHolder.getName();
    	
    	String f = flag;
    	
    	String strOpterComment="";
   	 	String strBusinessKey="";
   	 	String humanTaskId = "";
    	//将审批信息更新到 task_info表中
    	List<TaskInfo> taskInfo = taskInfoManager.findBy("processInstanceId",processInstanceId);
    	 for (TaskInfo getInfo : taskInfo) {
    		if( (getInfo.getStatus().equals("active")) && !(getInfo.getCatalog().equals("copy"))){
    			strBusinessKey=getInfo.getBusinessKey();
    			getInfo.setTenantId("1");
    			getInfo.setStatus("complete");
    			getInfo.setCompleteTime(new Date());
    			humanTaskId = getInfo.getId().toString();//该流程任务处理后，设置其消息已读
    		
    			if (f.equals("0")) {
    				strOpterComment="不同意";
    				getInfo.setAction("不同意");
    				//处理审批步骤 add by lilei at 2017.11.27
    				String strComment=customConnector.upadteCancelApprover(getInfo,currentUserHolder.getName());
        			getInfo.setComment(strComment+(request.getParameter("comment")==null?"":request.getParameter("comment")));//审批意见存入
    				taskInfoManager.save(getInfo);//更新当前审批人的状态 不用插入下一条审批信息，不同意，流程直接结束
    				
    				// zyl 处理 record 表中的结束时间
    				// updateRecordEndTime(getInfo);   // 流程优化后不用再处理此字段    zyl 2018-09-14
    				
    				//发送消息 add by lilei at 2017.11.29
    		    	customConnector.doNotice(getInfo, currentUserHolder.getUserId(), tenantHolder.getTenantId(), "complete",null);
    		    	
    		    	//花名册和组织结构流程审批后
    		    	handelProcessNoAgree(request.getParameter("applyCode"));
    			} else if (f.equals("1")) {
    				getInfo.setAction("同意");
    				strOpterComment="同意";
    				//自定义审批业务 add by lilei at 2017.11.27
    				/*******************************************************************************
    				 * 
    				 * 业务说明：
    				 * 1.如果下一位审批人为空（审批步骤还有其他人），则是终止自定义流程
    				 * 2.如果下一位审批人更改，则取消下一步审批原有人，审批步骤中新增一个新的审批人
    				 * 3.如果2成立，则新增消息提醒给新人
    				 * 4.如果为最后一个审批人，则可以新增审批人，新增消息提醒给新人
    				 * 
    				 * *****************************************************************************/
    				getInfo.setOwner(request.getParameter("leaderName"));	//指定的下一步审批人 名字
    				getInfo.setTaskId(request.getParameter("leader"));		//指定的下一步审批人 id
    				String parameterType = request.getParameter("type");	//顺序执行还是增加，删除，替换  审核人  0：顺序执行 1：增加 2：删除  3：替换
    				String first = request.getParameter("leader");
    				if(StringUtils.isBlank(first)){
    					if("1".equals(parameterType) || "3".equals(parameterType)){
    						parameterType = "0";
    					}
    				}
    				String strComment="";
    				//根据parameterType 判断
    				if(!"".equals(parameterType) ){
    					//检测是否变更审核人，如果变更，新增审批步骤 add by lilei at 2017.11.27 ckx 2018/8/29
    					strComment=customConnector.updateChangeAuditor(getInfo,request.getParameter("leader"),currentUserHolder.getName(),parameterType);
    					if(!"".equals(strComment) ){
    						if("0".equals(parameterType)||"00".equals(strComment))
    							strComment = "";
    						if(!"".equals(getInfo.getTaskId())){
    		    				TaskInfo taskInfo2 = new TaskInfo();			//生成  下一审批人的一条新记录
        	    		    	taskInfo2.setAssignee(getInfo.getTaskId());		//下一条审批的负责人
        	    		    	taskInfo2.setName(getInfo.getOwner()+"审批");	//负责人姓名
    		    		    	taskInfo2.setCreateTime(new Date());			//创建时间
    		    		    	taskInfo2.setAction("自定义申请等待审批");
    		    		    	taskInfo2.setPresentationSubject(getInfo.getPresentationSubject());		//主题
    		    		    	taskInfo2.setPresentationName("自定义申请");
    		    		    	taskInfo2.setStatus("active");					//处理状态
    		    		    	taskInfo2.setCode(getInfo.getAssignee());		//存入上一节点的id
    		    		    	taskInfo2.setTenantId("1");
    		    		    	taskInfo2.setSuspendStatus("自定义申请");
    		    		    	
    		    		    	//ckx  审批内容显示问题 2018/07/26
    		    		    	if(null != customWorkEntityDTO.getFormType() && !"".equals(customWorkEntityDTO.getFormType()) && !"null".equals(customWorkEntityDTO.getFormType())){
    		    		    		taskInfo2.setAction(customWorkEntityDTO.getTheme()+"等待审批");
    		    		    		taskInfo2.setPresentationName(customWorkEntityDTO.getTheme());
    		    		    	}
    		    		    	
    		    		    	taskInfo2.setProcessInstanceId(getInfo.getProcessInstanceId());	//存入表单ID，否则下个人审批的时候取不到申请单的数据
    		    		    	taskInfo2.setDescription(getInfo.getDescription());				//记录任务发起人
    		    		    	taskInfo2.setBusinessKey(getInfo.getBusinessKey());
    		    		    	taskInfo2.setCatalog("normal");
    		    		    	
    		    		    	taskInfoManager.save(taskInfo2);
    		    		    	//发送消息 add by lilei at 2017.11.29
    		    		    	customConnector.doNotice(taskInfo2, currentUserHolder.getUserId(), tenantHolder.getTenantId(), "audit",null);
        					
    						}else{
    							//正常执行结束
    							//发送消息 add by lilei at 2017.11.29
    					    	customConnector.doNotice(getInfo, currentUserHolder.getUserId(), tenantHolder.getTenantId(), "complete",null);
    					    	// zyl 处理 record 表中的结束时间
    					    	// updateRecordEndTime(getInfo);   // 流程优化后不用再处理此字段    zyl 2018-09-14
    							
    							//此处，流程审核通过，流程结束，更新personinfo表
    							String applyCode =  request.getParameter("applyCode");
    							//人事管理，人事审批
    							handleProess(applyCode);
    						}
    					}else{
    						/**
        					 * 取消下一步审批人，流程结束
        					 * 处理审批步骤 add by lilei at 2017.11.27
        					 * **/
        					strComment=customConnector.upadteDelApprover(getInfo,currentUserHolder.getName());
        					//发送消息 add by lilei at 2017.11.29
    	    		    	customConnector.doNotice(getInfo, currentUserHolder.getUserId(), tenantHolder.getTenantId(), "complete",null);
    	    		    	// zyl 处理 record 表中的结束时间
    	    		    	// updateRecordEndTime(getInfo);   // 流程优化后不用再处理此字段    zyl 2018-09-14
    	    				
    	    				//此处，流程审核通过，流程结束，更新personinfo表
    	    				String applyCode =  request.getParameter("applyCode");
    	    				//人事管理，人事审批
	    					handleProess(applyCode);
    					}
    				}
    				
    				//审批意见存入
        			getInfo.setComment(strComment+(request.getParameter("comment")==null?"":request.getParameter("comment")));
    				//向oa_bpm_customPre表中记录同意的记录，用于驳回的时候找上一节点
    				String sql2 = "from CustomPre where previous=? and formID =? ";
    		        List<CustomPre> c2 = customPreManager.find(sql2,getInfo.getAssignee(),getInfo.getProcessInstanceId());
    		        
    		        if(c2.size()>0){
    					customPreManager.removeAll(c2);
    				}
    		        sql2 = "from CustomPre where assignee=? and formID =? ";
    		        c2 = customPreManager.find(sql2,request.getParameter("leader"),getInfo.getProcessInstanceId());
    		        if(c2.size()>0){
    					customPreManager.removeAll(c2);
    				}
    				CustomPre customPre = new CustomPre();
    				customPre.setAssignee(getInfo.getTaskId());//daiding
    				customPre.setPrevious(getInfo.getAssignee());
    				customPre.setFormID(getInfo.getProcessInstanceId());
    				customPre.setCreateTime(new Date());
    				customPreManager.save(customPre);
    				
    				
    			} else if (f.equals("2")) {
    				getInfo.setAction("驳回");
    				strOpterComment="驳回";
    				//驳回，改变审批步骤，add by lilei at 2017.11.27
    				customConnector.upadteRejectApprover(getInfo);
    				
    				//审批意见存入
        			getInfo.setComment(request.getParameter("comment")==null?"":request.getParameter("comment"));

        			//生成  下一审批人的一条新记录,就是将上一节点取出，再生成一条新记录
    				TaskInfo taskInfo2 = new TaskInfo();
    		    	//下一条审批的负责人
    		    	//taskInfo2.setAssignee(c);
    		    	
    		    	//要驳回到上一节点，先从oa_bpm_customPre表找出上一节点
    				String sql = "from CustomPre where assignee=? and formID =? ";
    		        
    		        List<CustomPre> customPre2 = customPreManager.find(sql,getInfo.getAssignee(),getInfo.getProcessInstanceId());
    		        
    		        String preNode = customPre2.get(0).getPrevious();
    				
    		        //驳回的时候，将这个人在oa_bpm_customPre表里删除，这样以后再选择此人审批也可以
    		        sql = "from CustomPre where previous=? and formID =? ";
    		        
    		        List<CustomPre> cusPre2 = customPreManager.find(sql,getInfo.getAssignee(),getInfo.getProcessInstanceId());
    		        customPreManager.removeAll(cusPre2);
    		    	
    		    	//创建时间
    		    	taskInfo2.setCreateTime(new Date());
    		    	//如果驳回的上一节点就是该条任务的发起人，那么就是回到发起人调整申请
    		    	if(getInfo.getDescription().equals(preNode)){
    		    		taskInfo2.setAction("自定义申请等待调整");
    		    		//负责人姓名
        		    	taskInfo2.setName("调整申请");
        		    	taskInfo2.setCatalog("normal");
        		    	//ckx  审批内容显示问题 2018/07/26
	    		    	if(null != customWorkEntityDTO.getFormType() && !"".equals(customWorkEntityDTO.getFormType()) && !"null".equals(customWorkEntityDTO.getFormType())){
	    		    		taskInfo2.setAction(customWorkEntityDTO.getTheme()+"等待调整");
	    		    	}
    		    	}else{
    		    		taskInfo2.setAction("自定义申请等待审批");
    		    		taskInfo2.setName("待审批");
    		    		taskInfo2.setCatalog("normal");
    		    		//ckx  审批内容显示问题 2018/07/26
	    		    	if(null != customWorkEntityDTO.getFormType() && !"".equals(customWorkEntityDTO.getFormType()) && !"null".equals(customWorkEntityDTO.getFormType())){
	    		    		taskInfo2.setAction(customWorkEntityDTO.getTheme()+"等待审批");
	    		    	}
    		    	}
    		    	
    		    	//主题
    		    	taskInfo2.setPresentationSubject(getInfo.getPresentationSubject());
    		    	taskInfo2.setPresentationName("自定义申请");
    		    	//ckx  审批内容显示问题 2018/07/26
    		    	if(null != customWorkEntityDTO.getFormType() && !"".equals(customWorkEntityDTO.getFormType()) && !"null".equals(customWorkEntityDTO.getFormType())){
    		    		taskInfo2.setPresentationName(customWorkEntityDTO.getTheme());
    		    	}
    		    	//处理状态
    		    	taskInfo2.setStatus("active");
    		    	//存入上一节点的id 不能是上一步的节点，一层一层驳回，应该找task_info表中
    		    	taskInfo2.setCode(getInfo.getAssignee());
    		    	
    		    	taskInfo2.setAssignee(preNode);
    		    	
    		    	taskInfo2.setTenantId("1");
    		    	taskInfo2.setSuspendStatus("自定义申请");
    		    	//存入表单ID，否则下个人审批的时候取不到申请单的数据
    		    	taskInfo2.setProcessInstanceId(getInfo.getProcessInstanceId());
    		    	//记录任务发起人
    		    	taskInfo2.setDescription(getInfo.getDescription());
    		    	taskInfo2.setBusinessKey(getInfo.getBusinessKey());
    		    	taskInfoManager.save(taskInfo2);
    		    	//发送消息 add by lilei at 2017.11.29
    		    	customConnector.doNotice(taskInfo2, currentUserHolder.getUserId(), tenantHolder.getTenantId(), "reject",null);
    				//return;
    	        } else if (f.equals("3")) { //重新发起申请
    	        	//ckx  撤回重新调整审核人  2018/11/5
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
    		    	taskInfo2.setAction("自定义申请等待审批");
    		    	//主题
    		    	taskInfo2.setPresentationSubject(getInfo.getPresentationSubject());
    		    	taskInfo2.setPresentationName("自定义申请");
    		    	//处理状态
    		    	taskInfo2.setStatus("active");
    		    	//存入上一节点的id
    		    	taskInfo2.setCode(getInfo.getAssignee());
    		    	taskInfo2.setTenantId("1");
    		    	taskInfo2.setSuspendStatus("自定义申请");
    		    	//ckx  审批内容显示问题 2018/07/26
    		    	if(null != customWorkEntityDTO.getFormType() && !"".equals(customWorkEntityDTO.getFormType()) && !"null".equals(customWorkEntityDTO.getFormType())){
    		    		taskInfo2.setAction(customWorkEntityDTO.getTheme()+"等待审批");
    		    		taskInfo2.setPresentationName(customWorkEntityDTO.getTheme());
    		    	}
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
  				
	  			//判断，若是花名册的流程，单独 更新content 申请内容和主题
     		    UpdatePerson updatePerson=updatePersonManager.findUniqueBy("applyCode", applyCode);
     		  	if(updatePerson!=null){//判断是花名册的流程，单独 更新content 申请内容和主题
     		  		if((updatePerson.getTypeID().equals("personadd")|| updatePerson.getTypeID().equals("personUpdate"))){
     		  			Object succesResponse = JSON.parse(updatePerson.getJsonContent());    //先转换成Object
        				Map map = (Map)succesResponse;
        				PersonInfo personInfo=jsonMapper.fromJson(map.get("personInfo").toString(),PersonInfo.class);
        				
        				String content = "员工编号："+personInfo.getEmployeeNo()+" , 姓名："+personInfo.getFullName() +"\n";
         		  		List<RosterLog> rostlog = rosterLogManager.findBy("code",request.getParameter("applyCode"));

    		     			
    		     			if( updatePerson.getTypeID().equals("personadd")){

    		     				content = " [花名册]录入新员工： "+content;
    		     				customEntity.setTheme("[花名册]新员工录入  "
//    		     				+personInfo.getFullName()
    		     						);
    		     				customEntityDTO.setTheme("[花名册]新员工录入   "
//    		     						+personInfo.getFullName()
    		     						);
    		     				
    		     				content =  "[花名册]录入新员工: "+personInfo.getFullName();	
    		     			}else{
    		     				customEntity.setTheme("修改花名册 "
//    		     			+personInfo.getFullName()
    		     			);
    		     				customEntityDTO.setTheme("修改花名册 "
//    		     			+personInfo.getFullName()
    		     			);
    		     				
    		     				for(RosterLog r :rostlog){
     				   				 content = content + " ["+r.getUpdateColumnName() + "] 由  \""+ (r.getContentBefore().equals("")?"无":r.getContentBefore())+"\"   修改为   \""+r.getContentNew()+"\" \n";	
    			     			}
    		     			}
    		  				customEntity.setApplyContent(content);
     		  		}
     		  	}
     		  	else {
	  				 customEntity.setApplyContent(customEntityDTO.getApplyContent());
	  				 customEntity.setTheme(customEntityDTO.getTheme());
  				}
  					
    		        customEntity.setApplyCode(customEntityDTO.getApplyCode());
    		        customEntity.setUserId(Long.parseLong(userId));
    		        customEntity.setName(currentUserHolder.getName());
    		        
    		        customEntity.setBusinessDetail(customEntityDTO.getBusinessDetail());
    		        customEntity.setBusinessType(customEntityDTO.getBusinessType());
    		        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    		    	String str = sdf.format(new Date());
    		        customEntity.setCreateTime(request.getParameter("createTime"));
    		        customEntity.setModifyTime(str);
    		        customEntity.setId(Long.parseLong(processInstanceId));
    		        customEntity.setSubmitTimes( customEntityDTO.getSubmitTimes()+1);
    		        customEntity.setCcName("");
    		        customEntity.setCcnos("");
    		        customEntity.setBusinessLevel(customEntityDTO.getBusinessLevel());
    		        
    		        customManager.save(customEntity);
					 //}
    		        SaveFormHtml(customEntity,getInfo.getBusinessKey());
    		        
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
       		        	//taskInfoManager.removeAll(ta_info);
    	        		//先删除岗位流程关联表
           		        jdbcTemplate.update("delete from task_info_approve_position where task_id = '"+t.getId()+"'");
        		    	
       		        }	
	       		        //重新调整页面抄送   add ckx   2018/9/9
	       		        //先删除岗位流程关联表
	       		        //jdbcTemplate.update("delete from task_info_approve_position where position_parentId = '' and business_key = '"+taskInfo2.getBusinessKey()+"'");
	       		     if(!ccnos.isEmpty()){
	       		        customWorkService.copySave(processInstanceId, ccnos, ccName, customEntity.getName(), userId);
	       		     }
    		    	   /*	String[] ccUserName = null;  
    					String[] ccUserId = null;  
    					ccUserId = ccnos.split(","); 
    					ccUserName=ccName.split(",");
    					for(int i=0;i<ccUserId.length;i++){
	    					String ccUserIds = ccUserId[i];
    						String ccUserNames=ccUserName[i];
    						//若当前这个抄送人已经存在且已阅，跳出去，循环下一个抄送人
    						sql3 = "from TaskInfo where  assignee =? and catalog=? and status='complete' and processInstanceId=? ";
            		        
            		        List<TaskInfo> ta_info1 = taskInfoManager.find(sql3,ccUserIds,"copy",processInstanceId);
            		        
            		        if(ta_info1.size()<=0){
            		        	
    						
    					//重新插入抄送人	
    			    	TaskInfo taskInfo3 = new TaskInfo();
    			    	
    			    	//记录当前负责人id
    			    	taskInfo3.setAssignee(ccUserIds);
    			    	
    			    	//记录当前负责人姓名
    			    	taskInfo3.setName(ccUserNames+"有抄送待阅");
    			    	
    			    	//记录任务发起人
    			    	taskInfo3.setDescription(userId);
    			    	//创建时间
    			    	taskInfo3.setCreateTime(new Date());
    			    	taskInfo3.setAction("自定义申请待阅");
    			    	//主题
    			    	taskInfo3.setPresentationSubject(getInfo.getPresentationSubject());
    			    	taskInfo3.setPresentationName("自定义申请");
    			    	//处理状态
    			    	taskInfo3.setStatus("active");
    			    	
    			    	taskInfo3.setTenantId("1");
    			    	taskInfo3.setSuspendStatus("自定义申请");
    			    	//ckx  审批内容显示问题  2018/07/26
        		    	if(null != customWorkEntityDTO.getFormType() && !"".equals(customWorkEntityDTO.getFormType()) && !"null".equals(customWorkEntityDTO.getFormType())){
        		    		taskInfo3.setAction(customWorkEntityDTO.getTheme()+"待阅");
        		    		taskInfo3.setPresentationName(customWorkEntityDTO.getTheme());
        		    	}
    			    	taskInfo3.setProcessInstanceId(processInstanceId);
    			    	taskInfo3.setBusinessKey(getInfo.getBusinessKey());
    			    	taskInfo3.setCatalog("copy");
    			    	taskInfoManager.save(taskInfo3);
    			    	
    			    	//发送消息 add by lilei at 2017.11.29
        		    	customConnector.doNotice(taskInfo3, currentUserHolder.getUserId(), tenantHolder.getTenantId(), "copy",null);
    					}
    				   }*/
    				 // }
    		       
    		        // zyl 处理 record 表中的提交次数&&若是撤回，则重新发起后更新审核状态shijingxin
    		        String sql = "update KV_RECORD set submitTimes=" + customEntity.getSubmitTimes()+",audit_status=1" + " ,theme='"+customEntityDTO.getTheme()+"'  where id=" + getInfo.getBusinessKey();
    				keyValueConnector.updateBySql(sql);
    		        
    		      //保存附件
			        //fileUploadAPI.uploadFile(files, tenantId, (processInstanceId), "operation/CustomApply");
					 
			        if (iptdels != null && !iptdels.equals("")) {
			            fileUploadAPI.uploadFileDel(iptdels,processInstanceId);
			        }

			       fileUploadAPI.uploadFile(files, tenantId, processInstanceId, "operation/CustomApply");
			       //return;
			        
			     } else if (f.equals("4")) {
			    	 strOpterComment="撤销申请";
    				getInfo.setAction("撤销申请");
    				taskInfoManager.save(getInfo);
    				//不用插入下一条审批信息，不同意，流程直接结束
    				// zyl 处理 record 表中的结束时间
    				updateRecordEndTime(getInfo);
    				//花名册和组织结构流程审批后
    		    	handelProcessNoAgree(request.getParameter("applyCode"));
    				//return;
    	        } else if (f.equals("5")) {
    	        	strOpterComment="同意";
    				//审批意见存入
        			getInfo.setComment(request.getParameter("comment")==null?"":request.getParameter("comment"));
    				getInfo.setAction("同意");
    				//更新当前审批人的状态
    				taskInfoManager.save(getInfo);
    				//不用插入下一条审批信息，不同意，流程直接结束
    				// updateRecordEndTime(getInfo);   // 流程优化后不用再处理此字段    zyl 2018-09-14
    				
    				//自定义审核结束 add by lilei at 2018-06-06
    		    	handleProess(request.getParameter("applyCode"));
    				//return;
    			}
    			if(getInfo.getComment() == null){
    				getInfo.setComment("");
    			}
    			taskInfoManager.save(getInfo);
    		}
    	 }
    	 
    	 //审批状态
    	 SetProcessAuditStatus(strOpterComment,strBusinessKey);
    	 //审批岗位
    	 SetAuditPosition(strBusinessKey);
    	 //审批后将此任务对应的消息置为已读 2018.08.27 sjx
         String updateMsg = "update MsgInfo set status=1 where data=?";
         msgInfoManager.batchUpdate(updateMsg, humanTaskId);
    }
    
    /**
     * 流程结束并同意后处理
     * 主要针对花名册、组织结构操作需要流程审批，审批后的处理
     * add by lilei at 2018-05-25
     * @throws IOException 
     * @throws IllegalAccessException 
     * @throws IllegalArgumentException 
     * @throws ParseException 
     */
    @Transactional(readOnly=false)
    public  void handleProess(String applyCode) throws IOException, IllegalArgumentException, IllegalAccessException, ParseException {
    	if(!StringUtils.isBlank(applyCode)){
    		UpdatePerson opterProcess=updatePersonManager.findUniqueBy("applyCode", applyCode);
    		
    		if(opterProcess!=null)
    		{
    			String opterType=opterProcess.getTypeID();
//    			String jsonString=opterProcess.getJsonContent();
//				Map<String,Object> jsonMap=jsonMapper.fromJson(jsonString, Map.class);
				
				Object succesResponse = JSON.parse(opterProcess.getJsonContent());    //先转换成Object
				Map<String,Object> map = (Map<String,Object>)succesResponse;
				//jsonMapper
    			switch (opterType.toLowerCase()) {
				case "personadd":
					PersonInfo personInfo=jsonMapper.fromJson(map.get("personInfo").toString(),PersonInfo.class);
					int partyLevel=(int) map.get("partyLevel");
					String partyEntityId=map.get("parentPartyEntityId")==null?"":map.get("parentPartyEntityId").toString();
					Long postPartyEntityId=getLongByMap(map, "postPartyEntityId");
					Long accountId=Long.parseLong(map.get("accountId").toString());
					personInfoService.insertPersonInfo(applyCode,personInfo, partyLevel,Long.parseLong(partyEntityId) , postPartyEntityId,accountId);
					
					RosterLog rosterLog = rosterLogManager.findUniqueBy("code", applyCode);
					
					//记录日志
				      rosterLog.setIsapproval("0");//1：流程审核还未通过   0：流程审核通过    20180515 cz 新录入数据 初始化为未通过，待审核通过再更新为0
				      rosterLog.setUpdateTime(new Date());  	  	/** 修改时间. */
				      rosterLog.setEmployeeNo(personInfo.getId().toString());			/**被修改的员工编号. */
				      rosterLogManager.save(rosterLog);
				      break;
				case "personupdate":
					PersonInfo personInfoUpdate=jsonMapper.fromJson(map.get("personInfo").toString(),PersonInfo.class);
					String password = map.get("confirmPassword")==null?"": map.get("confirmPassword").toString();
					String gestureSwitch = map.get("gestureSwitch")==null?"": map.get("gestureSwitch").toString();
					Long updateAccountId= getLongByMap(map,"accountId");
					
					personInfoService.updatePersonInfo(applyCode,personInfoUpdate, password, gestureSwitch,updateAccountId);
					break;
				case "changepost":
					//jsonMap.put("selfPartyEntityId", partyEntity.getId());
	           		//jsonMap.put("OldpartyEntityId", iptCurrentPost);
	           		//jsonMap.put("changePartyEntityId", changePartyEntity.getId());
					
					PartyEntity partyEntity=partyEntityManager.findUniqueBy("id", jsonMapper.fromJson(map.get("selfPartyEntityId").toString(), Long.class)) ;
					PartyEntity changePartyEntity=partyEntityManager.findUniqueBy("id", jsonMapper.fromJson(map.get("changePartyEntityId").toString(), Long.class));
					Long oldPartyEntityId=jsonMapper.fromJson(map.get("OldpartyEntityId").toString(), Long.class);
					userService.changePosition(partyEntity, changePartyEntity, oldPartyEntityId,applyCode);
					break;
				case "orgadd":
					
					/*jsonMap.put("partyStructId", partyStruct.getId());
		        	jsonMap.put("childEntityRef", childEntityRef);
		        	jsonMap.put("childEntityId", childEntityId);
		        	jsonMap.put("childEntityName",childEntityName);
		        	jsonMap.put("shortName", shortName);
		        	jsonMap.put("isDisplay", isDisplay);
		        	jsonMap.put("username", username);
		        	jsonMap.put("partyEntityId",partyEntityId);
		        	jsonMap.put("partyTypeId", partyTypeId);
		        	jsonMap.put("partyStructTypeId",partyStructTypeId);
		        	jsonMap.put("partyLevel",partyLevel);*/
					PartyStruct partyStruct=new PartyStruct();
					if(map.get("partyStructId")!=null){
						Long partyStructId=Long.valueOf(map.get("partyStructId").toString());
						partyStruct=partyStructManager.findUniqueBy("id", partyStructId);
					}
					String childEntityRef=getStringByMap(map,"childEntityRef");
					
					
					/*if(map.get("childEntityRef")!=null)
						childEntityRef=jsonMapper.fromJson(map.get("childEntityRef").toString(),String.class);*/
					
					Long childEntityId=getLongByMap(map,"childEntityId");//jsonMapper.fromJson(map.get("childEntityId").toString(),Long.class);
					String childEntityName=getStringByMap(map,"childEntityName");//jsonMapper.fromJson(map.get("childEntityName").toString(),String.class);
					String shortName=getStringByMap(map,"shortName");//jsonMapper.fromJson(map.get("shortName").toString(),String.class);
					String isDisplay=getStringByMap(map,"isDisplay");//jsonMapper.fromJson(map.get("isDisplay").toString(),String.class);
					String username=getStringByMap(map,"username");//jsonMapper.fromJson(map.get("username").toString(),String.class);
					Long orgPartyEntityId=getLongByMap(map,"partyEntityId");//jsonMapper.fromJson(map.get("partyEntityId").toString(),Long.class);
					Long partyTypeId=getLongByMap(map,"partyTypeId");//jsonMapper.fromJson(map.get("partyTypeId").toString(),Long.class);
					Long partyStructTypeId=getLongByMap(map,"partyStructTypeId");//jsonMapper.fromJson(map.get("partyStructTypeId").toString(),Long.class);
					Integer orgPartyLevel=getIntegerByMap(map,"partyLevel");//jsonMapper.fromJson(map.get("partyLevel").toString(),Integer.class);
					Integer orgPriority=getIntegerByMap(map,"priority");
					Long orgAccountId=getLongByMap(map,"accountId");
					orgService.partyInfoAdd(partyStruct,
							childEntityRef, 
							childEntityId, 
							childEntityName,
							shortName, 
							isDisplay, 
							username, 
							orgPartyEntityId,
							partyTypeId, 
							partyStructTypeId, 
							orgPartyLevel,
							orgPriority,
							orgAccountId);
		        	
					break;
				case"orgupdate":
					/*Long structId,
		            Long childEntityId,
		            String childEntityName,
		            String shortName,
		            String isDisplay,
		            int priority*/
					
					Long orgUpdateStructId=getLongByMap(map, "structId");
					Long orgUpdatechildEntityId=getLongByMap(map, "childEntityId");// jsonMapper.fromJson(map.get("childEntityId").toString(),Long.class);
					String orgUpdateChildEntityName=getStringByMap(map, "childEntityName"); //jsonMapper.fromJson(map.get("childEntityName").toString(),String.class);
					String orgUpdateShortName=getStringByMap(map, "shortName");//jsonMapper.fromJson(map.get("shortName").toString(),String.class);
					String orgUpdateIsDisplay=getStringByMap(map, "isDisplay");//jsonMapper.fromJson(map.get("isDisplay").toString(),String.class);
					int orgUpdatePriority=getIntegerByMap(map, "priority");//jsonMapper.fromJson(map.get("priority").toString(),Integer.class);
					String departmentCode=getStringByMap(map, "departmentCode");
					String departmentName=getStringByMap(map, "departmentName");
					orgService.partyInfoUpdate(
							orgUpdateStructId, 
							orgUpdatechildEntityId, 
							orgUpdateChildEntityName, 
							orgUpdateShortName, 
							orgUpdateIsDisplay, 
							orgUpdatePriority,
							departmentCode,
							departmentName
							);
					
					break;
				case "postwithperson":
					/*PartyStruct partyStruct,
		            String childEntityRef,
		            Long childEntityId,
		            String childEntityName,
		            Long partyEntityId,
		            Long partyTypeId,
		            Long partyStructTypeId*/
					
					Long relationPersonStructId=getLongByMap(map, "structId");//jsonMapper.fromJson(map.get("structId").toString(),Long.class);
					PartyStruct relationPersonPartyStruct=new PartyStruct();
					if(relationPersonStructId!=null)
						relationPersonPartyStruct=partyStructManager.findUniqueBy("id", relationPersonStructId);
					
					String relationPersonChildEntityRef=getStringByMap(map, "childEntityRef");//jsonMapper.fromJson(map.get("childEntityRef").toString(),String.class);
					Long relationPersonChildEntityId=getLongByMap(map, "childEntityId");//jsonMapper.fromJson(map.get("childEntityId").toString(),Long.class);
					String relationPersonChildEntityName=getStringByMap(map, "childEntityName");//jsonMapper.fromJson(map.get("childEntityName").toString(),String.class);
					Long relationPersonPartyEntityId=getLongByMap(map, "partyEntityId");//jsonMapper.fromJson(map.get("partyEntityId").toString(),Long.class);
					Long relationPersonPartyTypeId=getLongByMap(map, "partyTypeId");//jsonMapper.fromJson(map.get("partyTypeId").toString(),Long.class);
					Long relationPersonPartyStructTypeId=getLongByMap(map, "partyStructTypeId");//jsonMapper.fromJson(map.get("partyStructTypeId").toString(),Long.class);
					int relationPersonPriority=getIntegerByMap(map, "partyStructTypeId");
					Long relationPersonAccountId=getLongByMap(map, "accountId");
					orgService.relationPostionPerson(
							relationPersonPartyStruct, 
							relationPersonChildEntityRef, 
							relationPersonChildEntityId, 
							relationPersonChildEntityName,
							relationPersonPartyEntityId, 
							relationPersonPartyTypeId, 
							relationPersonPartyStructTypeId,
							relationPersonPriority,
							relationPersonAccountId,
							applyCode);
							
					break;
				}
    			
    			opterProcess.setIsApproval("1");
    			updatePersonManager.save(opterProcess);
    			//流程审核是否通过，改为0  标识 审核已通过
				List<RosterLog> rostlog = rosterLogManager.findBy("code", applyCode);
				for(RosterLog r :rostlog){
     				  r.setIsapproval("0");	
     				 rosterLogManager.save(r);
     			}
    		}
    	}
	}
    
    /**
	 * 不同意，更新
     * @throws IOException 
     * @throws ParseException 
     * @throws IllegalAccessException 
     * @throws IllegalArgumentException 
     * @throws NumberFormatException 
	 * */
	private void handelProcessNoAgree(String applyCode) throws IOException, NumberFormatException, IllegalArgumentException, IllegalAccessException, ParseException{
		UpdatePerson opterProcess=updatePersonManager.findUniqueBy("applyCode", applyCode);
		
		
		if(opterProcess!=null){
			opterProcess.setIsApproval("0");
			updatePersonManager.save(opterProcess);
			
			//仅编辑职员离职时调用  ckx 2019/2/20
			String opterType=opterProcess.getTypeID().toLowerCase();
			if("personupdate".equals(opterType)){
				//ckx 2018/11/7  当流程不同意时，修改数据库可能已经更改的状态
				Object succesResponse = JSON.parse(opterProcess.getJsonContent());    //先转换成Object
				Map<String,Object> map = (Map<String,Object>)succesResponse;
				//jsonMapper
				PersonInfo personInfo=jsonMapper.fromJson(map.get("personInfo").toString(),PersonInfo.class);
				Long id = personInfo.getId();
				Date quitTime = personInfo.getQuitTime();
				//修改person_info
				PersonInfo oldPersonInfo = personInfoManager.get(id);
				if(null != quitTime){
					oldPersonInfo.setQuitFlag(PersonInfoConstants.QUIT_FLAG_NO);
					oldPersonInfo.setDelFlag(PersonInfoConstants.DELETE_FLAG_NO);
					oldPersonInfo.setQuitTime(null);
					oldPersonInfo.setLeaveDate(null);
				}
				personInfoService.updatePersonInfo("0",oldPersonInfo, "", null,Long.valueOf(currentUserHolder.getUserId()));
			}
		}
	}
    
    private String getStringByMap(Map map,String key) throws IOException{
    	String result=null;
    	if(map.get(key)!=null)
    		result=map.get(key).toString();
    	return result;
    }
    
    private Long getLongByMap(Map map,String key) throws IOException{
    	Long result=null;
    	if(map.get(key)!=null)
    		result=Long.valueOf(map.get(key).toString());
    	return result;
    }
    
    private Integer getIntegerByMap(Map map,String key) throws IOException{
    	Integer result=null;
    	if(map.get(key)!=null)
    		result=Integer.valueOf(map.get(key).toString());
    	return result;
    }

    // 处理 record 表中的结束时间
	private void updateRecordEndTime(TaskInfo getInfo) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String date = sdf.format(new Date());
		String sql = "update KV_RECORD set end_time='" + date + "' where id=" + getInfo.getBusinessKey();
		keyValueConnector.updateBySql(sql);
	}
	
	
	 /**
     * 完成任务  手机端使用.
     */
	//@Transactional(readOnly = true)
	public void CompleteTaskCustomH5(String applyCode,FormParameter formParameter, String processInstanceId,
			 String flag,String userId,String userName,String type)
			throws Exception, IOException {
		
		// ckx  2018/07/26  手机端驳回  显示驳回信息
		//查询是否非自定义申请
		Map<String, Object> applyCodeMap = jdbcTemplate.queryForMap("select formType , subject from oa_bpm_customform where applyCode = '"+applyCode+"'");
		Object formType = applyCodeMap.get("formType");
		Object themeObj = applyCodeMap.get("subject");
		String theme = "";
		if(null != themeObj && !"".equals(themeObj) && !"null".equals(themeObj)){
			theme = themeObj.toString();
		}
		//获得当前登录人的ID和姓名
		
    	String f = flag;
    	String strOpterComment="";
   	 	String strBusinessKey="";
    	//将审批信息更新到 task_info表中
    	List<TaskInfo> taskInfo = taskInfoManager.findBy("processInstanceId",processInstanceId);
    	 for (TaskInfo getInfo : taskInfo) {
    		if( (getInfo.getStatus().equals("active")) && !(getInfo.getCatalog().equals("copy"))){
    			strBusinessKey=getInfo.getBusinessKey();
    			getInfo.setTenantId("1");
    			getInfo.setStatus("complete");
    			getInfo.setCompleteTime(new Date());
    		
    			if (f.equals("0")) {
    				strOpterComment="不同意";
    				getInfo.setAction("不同意");
    				//处理审批步骤 add by lilei at 2017.11.27
    				String strComment=customConnector.upadteCancelApprover(getInfo,userName);
        			getInfo.setComment(strComment+(formParameter.getMultiValueMap().getFirst("comment")==null?"":formParameter.getMultiValueMap().getFirst("comment")));//审批意见存入
    				taskInfoManager.save(getInfo);//更新当前审批人的状态 ,不用插入下一条审批信息，不同意，流程直接结束
    				
    				// zyl 处理 record 表中的结束时间
    				updateRecordEndTime(getInfo);
    				
    				//发送消息 add by lilei at 2017.11.29
    		    	customConnector.doNotice(getInfo,userId, tenantHolder.getTenantId(), "complete",null);
    		    	
    		    	//花名册和组织结构流程审批后
    		    	handelProcessNoAgree(applyCode);
    		        
    			} else if (f.equals("1")) {
    				strOpterComment="同意";
    				getInfo.setAction("同意");
    				    				
    				//指定的下一步审批人 名字
    				getInfo.setOwner(formParameter.getMultiValueMap().getFirst("leaderName"));
    					
        			//指定的下一步审批人 id
    				getInfo.setTaskId(formParameter.getMultiValueMap().getFirst("leader"));
    				String parameterType = type;
    				String strComment="";
    				
    				/*System.out.println("================================= H5下一步审核人Id" + 
    						formParameter.getMultiValueMap().getFirst("leader") + "=========================================");
      				System.out.println("================================= H5下一步审核人名字" + 
      						formParameter.getMultiValueMap().getFirst("leaderName") + "=========================================");
      				*/
    				//当添加或替换时，leader不允许为空
    				String first = formParameter.getMultiValueMap().getFirst("leader");
    				if(StringUtils.isBlank(first)){
    					if("1".equals(parameterType) || "3".equals(parameterType)){
    						parameterType = "0";
    					}
    				}
    				//如果指定了下一步的审批人，插入一条新记录
      				if(!"".equals(parameterType) ){
    					//检测是否变更审核人，如果变更，新增审批步骤 add by lilei at 2017.11.27
    					strComment=customConnector.updateChangeAuditor(getInfo,
    							formParameter.getMultiValueMap().getFirst("leader"),
    							userName,parameterType);
    					if(!"".equals(strComment)){
    						if("0".equals(parameterType)||"00".equals(strComment)){
    							strComment = "";
    						}
    						if(!"".equals(getInfo.getTaskId())){
    							//生成  下一审批人的一条新记录
        	    				TaskInfo taskInfo2 = new TaskInfo();
        	    		    	//下一条审批的负责人
        	    		    	taskInfo2.setAssignee(getInfo.getTaskId());
        	    		    	
        	    		    	//负责人姓名
        	    		    	taskInfo2.setName(getInfo.getOwner()+"审批");
        	    		    	
        	    		    	//创建时间
        	    		    	taskInfo2.setCreateTime(new Date());
        	    		    	taskInfo2.setAction("自定义申请等待审批");
        	    		    	//主题
        	    		    	taskInfo2.setPresentationSubject(getInfo.getPresentationSubject());
        	    		    	taskInfo2.setPresentationName("自定义申请");
        	    		    	//处理状态
        	    		    	taskInfo2.setStatus("active");
        	    		    	//存入上一节点的id
        	    		    	taskInfo2.setCode(getInfo.getAssignee());
        	    		    	taskInfo2.setTenantId("1");
        	    		    	taskInfo2.setSuspendStatus("自定义申请");
        	    		    	
        	    		    	//ckx  审批内容显示问题 2018/07/26
        	    		    	if(null != formType && !"".equals(formType) && !"null".equals(formType)){
        	    		    		taskInfo2.setAction(theme+"等待审批");
        	    		    		taskInfo2.setPresentationName(theme);
        	    		    	}
        	    		    	
        	    		    	//存入表单ID，否则下个人审批的时候取不到申请单的数据
        	    		    	taskInfo2.setProcessInstanceId(getInfo.getProcessInstanceId());
        	    		    	taskInfo2.setDescription(getInfo.getDescription());//记录任务发起人
        	    		    	taskInfo2.setBusinessKey(getInfo.getBusinessKey());
        	    		    	taskInfo2.setCatalog("normal");
        	    		    	taskInfoManager.save(taskInfo2);
        	    		    	
        	    		    	//发送消息 add by lilei at 2017.11.29
        	    		    	customConnector.doNotice(taskInfo2, userId, tenantHolder.getTenantId(), "audit",null);
    						}else{
    							//正常执行结束
    							//发送消息 add by lilei at 2017.11.29
        	    		    	customConnector.doNotice(getInfo, userId, tenantHolder.getTenantId(), "complete",null);
        	    		    	// zyl 处理 record 表中的结束时间
        	    				updateRecordEndTime(getInfo);
        	    				
        	    				handleProess(applyCode);
    						}
    					}else{
    						/**
        					 * 取消下一步审批人，流程结束
        					 * 处理审批步骤 add by lilei at 2017.11.27
        					 * **/
        					strComment=customConnector.upadteDelApprover(getInfo,userName);
        					//发送消息 add by lilei at 2017.11.29
    	    		    	customConnector.doNotice(getInfo, userId, tenantHolder.getTenantId(), "complete",null);
    	    		    	// zyl 处理 record 表中的结束时间
    	    				updateRecordEndTime(getInfo);
    	    				
    	    				handleProess(applyCode);
    					}
	    				
    				}
    				//审批意见存入
        			getInfo.setComment(strComment+(formParameter.getMultiValueMap().getFirst("comment")==null?"":formParameter.getMultiValueMap().getFirst("comment")));
    				//向oa_bpm_customPre表中记录同意的记录，用于驳回的时候找上一节点
    				String sql2 = "from CustomPre where previous=? and formID =? ";
    		        List<CustomPre> c2 = customPreManager.find(sql2,getInfo.getAssignee(),getInfo.getProcessInstanceId());
    		        
    		        if(c2.size()>0){
    					customPreManager.removeAll(c2);
    				}
    		        sql2 = "from CustomPre where assignee=? and formID =? ";
    		        c2 = customPreManager.find(sql2,formParameter.getMultiValueMap().getFirst("leader"),getInfo.getProcessInstanceId());
    		        
    		        if(c2.size()>0){
    					customPreManager.removeAll(c2);
    				}
    		        
    				CustomPre customPre = new CustomPre();
    				customPre.setAssignee(getInfo.getTaskId());
    				customPre.setPrevious(getInfo.getAssignee());
    				customPre.setFormID(getInfo.getProcessInstanceId());
    				customPre.setCreateTime(new Date());
    				customPreManager.save(customPre);
    				
    				
    				
    			} else if (f.equals("2")) {
    				strOpterComment="驳回";
    				getInfo.setAction("驳回");
    				
    				//驳回，改变审批步骤，add by lilei at 2017.11.27
    				customConnector.upadteRejectApprover(getInfo);
    				
    				//审批意见存入
        			getInfo.setComment(formParameter.getMultiValueMap().getFirst("comment")==null?"":formParameter.getMultiValueMap().getFirst("comment"));

        			//生成  下一审批人的一条新记录,就是将上一节点取出，再生成一条新记录
    				TaskInfo taskInfo2 = new TaskInfo();
    		    	//下一条审批的负责人
    		    	//taskInfo2.setAssignee(c);
    		    	
    		    	//要驳回到上一节点，先从oa_bpm_customPre表找出上一节点
    				String sql = "from CustomPre where assignee=? and formID =? ";
    		        List<CustomPre> customPre2 = customPreManager.find(sql,getInfo.getAssignee(),getInfo.getProcessInstanceId());
    		        String preNode = customPre2.get(0).getPrevious();
    				
    		        //驳回的时候，将这个人在oa_bpm_customPre表里删除，这样以后再选择此人审批也可以
    		        sql = "from CustomPre where previous=? and formID =? ";
    		        List<CustomPre> cusPre2 = customPreManager.find(sql,getInfo.getAssignee(),getInfo.getProcessInstanceId());
    		        customPreManager.removeAll(cusPre2);
    		    	
    		    	//创建时间
    		    	taskInfo2.setCreateTime(new Date());
    		    	//如果驳回的上一节点就是该条任务的发起人，那么就是回到发起人调整申请
    		    	if(getInfo.getDescription().equals(preNode)){
    		    		taskInfo2.setAction("自定义申请等待调整");
    		    		//负责人姓名
        		    	taskInfo2.setName("调整申请");
        		    	taskInfo2.setCatalog("normal");
        		    	//ckx  审批内容显示问题 2018/07/26
	    		    	if(null != formType && !"".equals(formType) && !"null".equals(formType)){
	    		    		taskInfo2.setAction(theme+"等待调整");
	    		    	}
    		    	}else{
    		    		taskInfo2.setAction("自定义申请等待审批");
    		    		taskInfo2.setName("待审批");
    		    		taskInfo2.setCatalog("normal");
    		    		//ckx  审批内容显示问题 2018/07/26
	    		    	if(null != formType && !"".equals(formType) && !"null".equals(formType)){
	    		    		taskInfo2.setAction(theme+"等待审批");
	    		    	}
    		    	}
    		    	
    		    	//主题
    		    	taskInfo2.setPresentationSubject(getInfo.getPresentationSubject());
    		    	taskInfo2.setPresentationName("自定义申请");
    		    	//ckx  审批内容显示问题 2018/07/26
    		    	if(null != formType && !"".equals(formType) && !"null".equals(formType)){
    		    		taskInfo2.setPresentationName(theme);
    		    	}
    		    	//处理状态
    		    	taskInfo2.setStatus("active");
    		    	//存入上一节点的id 不能是上一步的节点，一层一层驳回，应该找task_info表中
    		    	taskInfo2.setCode(getInfo.getAssignee());
    		    	taskInfo2.setAssignee(preNode);
    		    	taskInfo2.setTenantId("1");
    		    	taskInfo2.setSuspendStatus("自定义申请");
    		    	//存入表单ID，否则下个人审批的时候取不到申请单的数据
    		    	taskInfo2.setProcessInstanceId(getInfo.getProcessInstanceId());
    		    	//记录任务发起人
    		    	taskInfo2.setDescription(getInfo.getDescription());
    		    	taskInfo2.setBusinessKey(getInfo.getBusinessKey());
    		    	taskInfoManager.save(taskInfo2);
    		    	
    		    	//发送消息 add by lilei at 2017.11.29
    		    	customConnector.doNotice(taskInfo2, userId, tenantHolder.getTenantId(), "reject",null);
    	        } else if (f.equals("4")) {
    	        	strOpterComment="撤销申请";
    				getInfo.setAction("撤销申请");
    				taskInfoManager.save(getInfo);
    				//不用插入下一条审批信息，不同意，流程直接结束
    				// zyl 处理 record 表中的结束时间
    				updateRecordEndTime(getInfo);
    				
    				//花名册和组织结构流程审批后
    		    	handelProcessNoAgree(applyCode);
    				//return;
    	        }
    			else if (f.equals("5")) {
    				strOpterComment="同意";
    				String strComment=customConnector.upadteCancelApprover(getInfo,userName);
    				
    				//审批意见存入
        			getInfo.setComment(strComment+(formParameter.getMultiValueMap().getFirst("comment")==null?"":formParameter.getMultiValueMap().getFirst("comment")));
    				getInfo.setAction("同意");
    				//更新当前审批人的状态
    				taskInfoManager.save(getInfo);
    				//不用插入下一条审批信息，不同意，流程直接结束
    				
    				updateRecordEndTime(getInfo);
    				
    				//发送消息 add by lilei at 2017.11.29
    		    	customConnector.doNotice(getInfo, userId, tenantHolder.getTenantId(), "complete",null);
    		    	
    		    	//自定义审核结束 add by lilei at 2018-06-06
    		    	handleProess(applyCode);
    			}
    			if(getInfo.getComment() == null){
    				getInfo.setComment("");
    			}
    			taskInfoManager.save(getInfo);
    		}
    		
    	 }
    	 
    	 //审批状态
    	 SetProcessAuditStatus(strOpterComment,strBusinessKey);
    	 //审批岗位
    	 SetAuditPosition(strBusinessKey);
	}
	
	/**
	 * 人力资源模块自定义流程的发起
	 * add by lilei at 2018-05-25
	 * */
	/*public void startCustomProcessForHuman(
			String nextId,
			String nextUser,
			String theme,
			String applyCode,
			String content
			
			)
	{
		//region 走流程
        //接下来为发起流程做准备（修改职员信息走的是自定义申请的流程）--------------------------------------------------
        		String userId = currentUserHolder.getUserId();
        		
            	//首先取审批人
        	   	 //String nextId=request.getParameter("nextID");
        	   	 //String nextUser=request.getParameter("nextUser");
          	 
        	   	 String[] auditList=nextId.split(",");
        	     String[] auditNameList=nextUser.split(",");
            	
        		//生成 businessKey
                LinkedMultiValueMap<String,String>  multiValueMap = new LinkedMultiValueMap();
        		multiValueMap.set("theme", theme);//"修改花名册-"+personInfo.getUsername());
        		multiValueMap.set("applyCode",applyCode);// request.getParameter("applyCode"));
        		multiValueMap.set("busType", "9999");
        		multiValueMap.set("businessType", "自定义");
        		multiValueMap.set("busDetails", "8888");
        		multiValueMap.set("businessDetail", "自定义申请");
        		multiValueMap.set("submitTimes", "1");
        		multiValueMap.set("autoCompleteFirstTask", "false");
        		multiValueMap.set("url", "/operationCustom/custom-detail.do?suspendStatus=custom");
        		
        		
        		//修改了哪些内容，记录到content里面
        		String content = "员工编号："+personInfo.getEmployeeNo()+" , 姓名："+personInfo.getUsername() 
							+"\n";
        		List<RosterLog> rostlog = rosterLogManager.findBy("code",request.getParameter("applyCode"));
        		
     			for(RosterLog r :rostlog){
     				   				
     				 content = content + (r.getContentBefore().equals("")?"无":r.getContentBefore())+"   修改为：   "+r.getContentNew()+"\n";	
     			}
     
        		multiValueMap.set("content", content);
        		multiValueMap.set("name", "修改花名册");
        		multiValueMap.set("nextID", auditList[0]);
        		
        		//取所属大区和分公司
                String areaName="";
            	String areaId = "";
            	PartyEntity partyEntity  =  orgConnector.findPartyAreaByUserId(userId);
            	
            	if ( partyEntity !=  null){
            		areaId  = Long.toString(partyEntity.getId());
        			areaName = partyEntity.getName();
            	}
            	multiValueMap.set("areaId",areaId);
        		multiValueMap.set("area",areaName);
        		
        		FormParameter formParameter = new FormParameter();
        		formParameter.setBpmProcessId("-1");
        		formParameter.setMultiValueMap(multiValueMap);
        		
        		String businessKey = this.operationService.saveDraft(userId, "1",formParameter);
        		//1171069254352896
        		 String curentUserName = currentUserHolder.getName();
        		//存入表单内容			
        		CustomEntity customEntity = new CustomEntity();
        		customEntity.setCcName(personInfo.getId().toString()); //存放被修改人的id
        		customEntity.setCcnos(Long.toString(partyEntityId)); //存放被修改人的partyEntityId
                customEntity.setTheme("  修改花名册--"+personInfo.getUsername());
                customEntity.setApplyCode(applyCode);
                customEntity.setUserId(Long.parseLong(userId));
                 tenantId = "1";
                customEntity.setBusinessDetail("修改花名册");
                customEntity.setName(curentUserName);
                customEntity.setApplyContent(content);
                customEntity.setBusinessType("修改花名册");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            	String str = sdf.format(new Date());
                customEntity.setCreateTime(str);
                customEntity.setModifyTime(str);
                customEntity.setSubmitTimes(1);
                String strBusiLevel=orgConnector.getPositionMaxGradeByUserIds(nextId);
                customEntity.setBusinessLevel(strBusiLevel);
                customManager.save(customEntity);
        		
        		
                //添加审批人
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
        		
        		
        	    Long FormId =0L;//记录表单的主键ID
                FormId = customEntity.getId();
               
        		//发起流程
                customStartApply(customEntity,"", userId, auditNameList[0], 
                		auditList[0], FormId, businessKey, "","");
            	
                //this.customService.SaveFormHtml(customEntity,businessKey);
                String sqlRecordUpdate = "update KV_RECORD set apply_content= '" + customEntity.getApplyContent() +
                		"',theme ='" + customEntity.getTheme() +
                		"' where id= '" + businessKey + "'";
            	keyValueConnector.updateBySql(sqlRecordUpdate);
                //处理受理单编号
                operationService.deleteApplyCode(request.getParameter("applyCode"));
              //endregion
	}*/
	
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
    // ~ ======================================================================
    @Resource
    public void setKeyValueConnector(KeyValueConnector keyValueConnector) {
        this.keyValueConnector = keyValueConnector;
    }

   
    @Resource
    public void setCurrentUserHolder(CurrentUserHolder currentUserHolder) {
        this.currentUserHolder = currentUserHolder;
    }

    @Resource
    public void setOperationService(OperationService operationService) {
        this.operationService = operationService;
    }

   
    @Resource
    public void setMultipartResolver(MultipartResolver multipartResolver) {
        this.multipartResolver = multipartResolver;
    }

    @Resource
    public void setTenantHolder(TenantHolder tenantHolder) {
        this.tenantHolder = tenantHolder;
    }

 // ~ ======================================================================
    @Resource
    public void setCustomManager(CustomManager customManager) {
        this.customManager = customManager;
    }

    @Resource
    public void setTaskInfoManager(TaskInfoManager taskInfoManager) {
        this.taskInfoManager = taskInfoManager;
    }
    
    @Resource
    public void setCustomPreManager(CustomPreManager customPreManager) {
        this.customPreManager = customPreManager;
    }
    
    @Resource
    public void setFileUploadAPI(FileUploadAPI fileUploadAPI) {
        this.fileUploadAPI = fileUploadAPI;
    }

    @Resource
	public void setCustomApproverManager(CustomApproverManager customApproverManager) {
		this.customApproverManager = customApproverManager;
	}
    
    @Resource
    public void setOrgConnector(OrgConnector orgConnector) {
		this.orgConnector = orgConnector;
	}
    
    @Resource
    public void setCustomConnector(CustomConnector customConnector){
    	this.customConnector = customConnector;
    }
   
    @Resource
	public void setUpdatePersonManager(UpdatePersonManager updatePersonManager) {
		this.updatePersonManager = updatePersonManager;
	}

    @Resource
	public void setPersonInfoService(PersonInfoService personInfoService) {
		this.personInfoService = personInfoService;
	}
    
    @Resource
   	public void setRosterLogManager(RosterLogManager rosterLogManager) {
   		this.rosterLogManager = rosterLogManager;
   	}
    
    @Resource
   	public void setUserService(UserService userService) {
   		this.userService = userService;
   	}
    
    @Resource
   	public void setPartyEntityManager(PartyEntityManager partyEntityManager) {
   		this.partyEntityManager = partyEntityManager;
   	}
    
    @Resource
    public void setOrgService(OrgService orgService) {
        this.orgService = orgService;
    }
    
    @Resource
    public void setPartyStructManager(PartyStructManager partyStructManager) {
        this.partyStructManager = partyStructManager;
    }
    
    @Resource
    public void setRecordManager(RecordManager recordManager) {
        this.recordManager = recordManager;
    }
    
    @Resource
    public void setPartyOrgConnector(PartyOrgConnector partyOrgConnector) {
        this.partyOrgConnector = partyOrgConnector;
    }
    
}
