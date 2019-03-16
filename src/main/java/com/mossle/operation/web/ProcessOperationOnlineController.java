package com.mossle.operation.web;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.mossle.api.humantask.HumanTaskConnector;
import com.mossle.api.humantask.HumanTaskDTO;
import com.mossle.api.org.OrgConnector;
import com.mossle.common.utils.DeEnCode;
import com.mossle.common.utils.StringUtils;
import com.mossle.core.auth.CurrentUserHolder;
import com.mossle.dict.persistence.domain.DictInfo;
import com.mossle.dict.support.DictConnectorImpl;
import com.mossle.humantask.persistence.domain.TaskInfo;
import com.mossle.humantask.persistence.manager.TaskInfoManager;
import com.mossle.operation.service.CustomWorkService;
import com.mossle.operation.service.OperationService;
import com.mossle.party.PartyConstants;
import com.mossle.party.persistence.domain.PartyEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** 
 * @author  lilei	
 * @version 2017年10月26日
 * 直销OA受理单流程
 */

@Component
@Controller
@RequestMapping("operationOnlineOrder")
@Path("operationOnlineOrder")
public class ProcessOperationOnlineController {

private static Logger logger = LoggerFactory.getLogger(ProcessOperationController.class);
    private JdbcTemplate jdbcTemplate;
    private HumanTaskConnector humanTaskConnector;
    private OrgConnector orgConnector ;
    private CurrentUserHolder currentUserHolder;
    private DictConnectorImpl dictConnectorImpl ;
    @Autowired
    private CustomWorkService customWorkService;
    @Autowired
    private OperationService operationService;
    private TaskInfoManager taskInfoManager;
    
    // ~ ======================================================================
    //审批环节：先将表单内容取出
    @GET
    @Path("getOnLineForm")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> getOnLineForm(@QueryParam("id") String id) {
    	
    	Map<String, Object> returnMap=new HashMap<String, Object>();
    	if (!StringUtils.isNumeric(id)){
    		id = DeEnCode.decode(id);
    	}
    	/*String strSql="SELECT f_OnlineType(o.chrApplyType) AS appType,r.detailHtml,r.BUSINESS_KEY,r.audit_status AS auditStatus from kv_record r "
						+" INNER JOIN ro_pf_oaonline o ON o.id=r.pk_id"
						+" where r.ref='"+id+"'";
        
    	returnMap=jdbcTemplate. queryForMap(strSql);*/
    	
    	//Map<String, Object> returnMap=new HashMap<String, Object>();
    	String strSql="SELECT f_OnlineType(o.chrApplyType) AS appType,r.detailHtml,r.applyCode,r.businessDetailId,r.BUSINESS_KEY,r.audit_status AS auditStatus from kv_record r "
				+" INNER JOIN ro_pf_oaonline o ON o.id=r.pk_id ";
						
    	String strOnlineSql=strSql+" where r.ref='"+id+"'";
    	List<Map<String, Object>> returnMapList=jdbcTemplate.queryForList(strOnlineSql);
    	if(returnMapList.size()>0)
    		returnMap=returnMapList.get(0);
    	else {
    		TaskInfo taskStart=taskInfoManager.findUnique("from TaskInfo where catalog='start' and PROCESS_INSTANCE_ID=?", id);
    		String strKVSql=strSql+" where r.id='"+taskStart.getBusinessKey()+"'";
    		returnMap=jdbcTemplate.queryForMap(strKVSql);
		}
    	
    	//ckx  获取抄送人
    	String taskCopyNames = customWorkService.getTaskCopyNames(id);
    	String detailHtml = returnMap.get("detailHtml").toString();
    	if(StringUtils.isNotBlank(detailHtml)){
    		detailHtml = detailHtml.replace("{copyNames}", taskCopyNames);
    	}
    	returnMap.put("detailHtml", detailHtml);
    	
    	String status=returnMap.get("auditStatus").toString();
    	String isShow="1";//是否显示驳回按钮， 1：是，0：否
    	//如果是审核中（驳回）
    	if(status!=null&&status.equals("4"))
    	{
    		//取得第一审核人
    		strSql="SELECT ASSIGNEE FROM task_info"
    				+ " where catalog<>'copy' AND status='complete' AND catalog<>'start'"
    				+ " and BUSINESS_KEY='"+returnMap.get("BUSINESS_KEY").toString()+"'"
    				+ " ORDER BY COMPLETE_TIME ASC limit 0,1";
    		List<Map<String, Object>> taskMapList=jdbcTemplate.queryForList(strSql);
    		String strFirstAuditor=taskMapList.get(0).get("ASSIGNEE").toString();
    		
    		//取得当前审核人
    		strSql="SELECT ASSIGNEE FROM task_info"
    				+ " where catalog<>'copy' AND status='active' AND catalog<>'start'"
    				+ " and BUSINESS_KEY='"+returnMap.get("BUSINESS_KEY").toString()+"'"
    				+ " ORDER BY COMPLETE_TIME ASC limit 0,1";
    		
    		List<Map<String, Object>> taskActiveMapList=jdbcTemplate.queryForList(strSql);
    		String strCurrentAuditor=taskActiveMapList.get(0).get("ASSIGNEE").toString();
    		
    		if(strFirstAuditor.equals(strCurrentAuditor)){
        		//判断是否有经销商审核过
        		strSql="SELECT ASSIGNEE FROM task_info"
        				+ " where catalog<>'copy' AND status='complete' AND catalog='normal'"
        				+ " and ASSIGNEE='"+PartyConstants.JXS_ID+"'"
        				+ " and BUSINESS_KEY='"+returnMap.get("BUSINESS_KEY").toString()+"'"
        				+ " ORDER BY COMPLETE_TIME ASC limit 0,1";
        		List<Map<String, Object>> taskJXSMapList=jdbcTemplate.queryForList(strSql);
        		if(taskJXSMapList==null||taskJXSMapList.size()<1)
        			isShow="0";
    		}
    	}
    	
    	//判断是否要显示 【同意并授权】按钮   chengze 20180302
    	//“12万旗舰店申请”或“非12万旗舰店申请” 审核人为“罗麦科技-业务客服部-在线专员” 时 显示 【同意并授权】按钮 
    	String userId = currentUserHolder.getUserId();
    	String post_id = "";
    	String isAuthorization = "0"; //是否显示【同意并授权】按钮   的标识  ，0是不显示
    	
        List<DictInfo> dictInfo = this.dictConnectorImpl.findDictInfoListByType("onLinePostID");	//到数据字典中取 在线专员的岗位ID
        String dictValue = dictInfo.get(0).getValue();
    	List<PartyEntity> partyDTO = orgConnector.getPostByUserId(userId);							//取当前登录人的所属岗位的ID
         if(partyDTO.size() > 0) {
             for (int i = 0; i < partyDTO.size(); i++) {
            	  post_id =Long.toString( partyDTO.get(i).getId());//若当前登录人岗位就是在线专员岗
            	  if(post_id.equals(dictValue)){
					  if(",8,9,".contains(","+returnMap.get("businessDetailId").toString()+",")){//再确定业务类型细分是【 “12万旗舰店申请”或“非12万旗舰店申请”】
						isAuthorization = "1";//显示，是1
						break;
					  }
            	 }
             }
         }
    	
    	returnMap.put("isAuthorization", isAuthorization); 
    	returnMap.put("isshow", isShow);    	
    	return returnMap;
    }
    
    
    //判断是否12万/非12万 最后一步审批，是 返回1，否 返回0
    @GET
    @Path("isAuth")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> isAuth(@QueryParam("id") String id) {
    	Map<String, Object> returnMap=new HashMap<String, Object>();
    	String isAuth = "0";
    	String userId = currentUserHolder.getUserId();
    	if (!StringUtils.isNumeric(id)){
    		id = DeEnCode.decode(id);
    	}
    	
    	//先取该条流程的businesskey
        String strSql = "SELECT * from task_info where process_instance_id = "+id+" and `STATUS` = 'active' ";
        List<Map<String, Object>> mapList = jdbcTemplate.queryForList(strSql);
        Map<String, Object> map = mapList.get(0);
    	
        String sql = "select * from kv_record where business_key = "+map.get("business_key");
    	List<Map<String, Object>>   kvRecordList = jdbcTemplate.queryForList(sql);
    	if(kvRecordList!=null&&kvRecordList.size()>0)
    	{
    		
    		Map<String, Object> kvRecord=kvRecordList.get(0);
    		String businessDetailId=kvRecord.get("businessDetailId").toString();
    		String applyCode=kvRecord.get("applyCode").toString();
    		if(isAuthPositon(userId,businessDetailId)){
    			//非12万续约申请，和非12万续约申请
        		if(",10,11,".contains(","+businessDetailId+","))
        			isAuth="1";
        		else if(",8,9,".contains(","+businessDetailId+",")){
        			String sSql="select ifnull(chrIsAuthCertificate,'0') as chrIsAuthCertificate from  ro_pf_oaonline  WHERE varApplyCode = "+applyCode;
        			String chrIsAuthCertificate = jdbcTemplate.queryForList(sSql).get(0).get("chrIsAuthCertificate").toString();
        			if(chrIsAuthCertificate.equals("1"))
        				isAuth="1";
        		}
    		}
    	}
        
    	/*//先取该条流程的applyCode
    	 //String Sql="SELECT applyCode from kv_record where BUSINESS_KEY ="+map.get("business_key");
         //String applyCode = jdbcTemplate. queryForMap(strSql).toString();
    	 
    	 //取是否调用生成授权书的标识
    	 String sSql="select chrIsAuthCertificate from  ro_pf_oaonline  WHERE varApplyCode = "+applyCode;
         String chrIsAuthCertificate = jdbcTemplate. queryForMap(sSql).toString();
		 
    	 
    	//到数据字典中取      
    	 
    	 //12万申请流程，最后一步审核岗位ID
        List<DictInfo> dictInfo = this.dictConnectorImpl.findDictInfoListByType("onLinePostID12");
        String dictValue12 = dictInfo.get(0).getValue();
        
        //非12万申请流程，最后一步审核岗位ID
         dictInfo = this.dictConnectorImpl.findDictInfoListByType("onLinePostIDNot12");
        String dictValueNo12 = dictInfo.get(0).getValue();
    	
    	//确定业务类型细分是【 “12万旗舰店申请”或“12万续约”】
	    	String sql = "select * from kv_record where business_key = "+map.get("business_key")+" and  businessDetailId in ('8','10')";
	    	List<Map<String, Object>>   s = jdbcTemplate.queryForList(sql);
	    	if(s!=null&&s.size()>0){
    	
	    		if (partyDTO.size() > 0) {
		             for (int i = 0; i < partyDTO.size(); i++) {
		            	  post_id =Long.toString( partyDTO.get(i).getId());
		            	  
		            	  //若当前登录人岗位     12万申请流程，最后一步审核岗位ID
		            	  if(post_id.equals(dictValue12)&&chrIsAuthCertificate.equals("1")){
		            		  //是 返回1
		            		  isAuth = "1";
      		    	break;	 
      		    	}
            	  }
            	 
             }
         }
	    	
	    	//确定业务类型细分是【 “非12万旗舰店申请”或“非12万续约”】
	    	sql = "select * from kv_record where business_key = "+map.get("business_key")+" and  businessDetailId in ('9','11')";
	    	List<Map<String, Object>>   busidetail = jdbcTemplate.queryForList(sql);
	    	if(busidetail!=null&&busidetail.size()>0){
	    			if (partyDTO.size() > 0) {
		             for (int i = 0; i < partyDTO.size(); i++) {
		            	  post_id =Long.toString( partyDTO.get(i).getId());
		            	  
		            	  //若当前登录人岗位    非12万申请流程，最后一步审核岗位
		            	  if(post_id.equals(dictValueNo12)){
		            		  if(busidetail.get(0).get("businessDetailId").equals("9")&&chrIsAuthCertificate.equals("1")){
		            		  //是 返回1
		            		  isAuth = "1";
		            		  } 		  else if(busidetail.get(0).get("businessDetailId").equals("11")){
		            			  isAuth = "1";  
		            		  }
		            		  break;	 
		            	  }
		             }
	    		}
	    	}*/
    	returnMap.put("isauth", isAuth);
    	return returnMap;
    }
    
    /**
     * 是否为验证岗位（岗位是否有权限操作，同意并授权按钮） add by lilei at 2018.04.02
     * @param userId
     * @param strBusinessDetailId
     * @return
     */
    private boolean isAuthPositon(String userId,String strBusinessDetailId) {
		boolean isThisAuthPositon=false;
		String strCheckPositionId="";
		if((",8,10,").contains(","+strBusinessDetailId+",")){
			//12万和12万续约申请流程，最后一步审核岗位ID
	        List<DictInfo> dictInfo = this.dictConnectorImpl.findDictInfoListByType("onLinePostID12");
	        strCheckPositionId = dictInfo.get(0).getValue();
		}
		else if((",9,11,").contains(","+strBusinessDetailId+",")){
			//非12万和非12万续约申请流程，最后一步审核岗位ID
			List<DictInfo> dictInfo = this.dictConnectorImpl.findDictInfoListByType("onLinePostIDNot12");
			strCheckPositionId = dictInfo.get(0).getValue();
		}
        
		if(strCheckPositionId!=null&&!strCheckPositionId.equals("")){
			//取当前登录人的所属岗位的ID
	    	List<PartyEntity> partyDTO = orgConnector.getPostByUserId(userId);
	    	if (partyDTO!=null&&partyDTO.size() > 0) {
	    		for (int i = 0; i < partyDTO.size(); i++) {
		       	  //若当前登录人岗位    12万和非12万申请流程，审核岗位
		       	  if(Long.toString(partyDTO.get(i).getId()).equals(strCheckPositionId)){
		       		  isThisAuthPositon=true;
		       		  break;	 
	       	  		} 
	       	  	}
	        }
		}
    	
    	return isThisAuthPositon;
	}
    
    
  //手机端使用    ---判断是否12万/非12万 最后一步审批，是 返回1，否 返回0 ---手机端使用    
    @GET
    @Path("isAuthForPhone")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> isAuthForPhone(@QueryParam("id") String id,@QueryParam("userId") String userId) {
    	Map<String, Object> returnMap=new HashMap<String, Object>();
    	String isAuth = "0";
    	//if (!StringUtils.isNumeric(id)){
    		//id = DeEnCode.decode(id);
    	//}
    	
    	//先取该条流程的businesskey
        String strSql = "SELECT * from task_info where process_instance_id = "+id+" and `STATUS` = 'active' ";
        List<Map<String, Object>> mapList = jdbcTemplate.queryForList(strSql);
        Map<String, Object> map = mapList.get(0);
    	
        String sql = "select * from kv_record where business_key = "+map.get("business_key");
    	List<Map<String, Object>>   kvRecordList = jdbcTemplate.queryForList(sql);
    	if(kvRecordList!=null&&kvRecordList.size()>0)
    	{
    		
    		Map<String, Object> kvRecord=kvRecordList.get(0);
    		String businessDetailId=kvRecord.get("businessDetailId").toString();
    		String applyCode=kvRecord.get("applyCode").toString();
    		if(isAuthPositon(userId,businessDetailId)){
    			//非12万续约申请，和非12万续约申请
        		if(",10,11,".contains(","+businessDetailId+","))
        			isAuth="1";
        		else if(",8,9,".contains(","+businessDetailId+",")){
        			String sSql="select  ifnull(chrIsAuthCertificate,'0') as chrIsAuthCertificate from  ro_pf_oaonline  WHERE varApplyCode = "+applyCode;
        			String chrIsAuthCertificate = jdbcTemplate.queryForList(sSql).get(0).get("chrIsAuthCertificate").toString();
        			if(chrIsAuthCertificate.equals("1"))
        				isAuth="1";
        		}
    		}
    	}
    	
    	/*//取当前登录人的所属岗位的ID
    	List<PartyEntity> partyDTO = orgConnector.getPostByUserId(userId);
    	
    	//先取该条流程的businesskey
        String strSql = "SELECT * from task_info where process_instance_id = "+id+" and `STATUS` = 'active' ";
    	 List<Map<String, Object>> mapList = jdbcTemplate.queryForList(strSql);
    	 Map<String, Object> map = mapList.get(0);
    	
    	//先取该条流程的applyCode
    	 String Sql="SELECT applyCode from kv_record where BUSINESS_KEY ="+map.get("business_key");
         String applyCode = jdbcTemplate. queryForMap(strSql).toString();
    	 
         
    	 //取是否调用生成授权书的标识
    	 String sSql="select chrIsAuthCertificate from  ro_pf_oaonline  WHERE varApplyCode = "+applyCode;
         String chrIsAuthCertificate = jdbcTemplate. queryForMap(sSql).toString();
		 
    	 
    	 
    	//到数据字典中取      
    	 
    	 //12万申请流程，最后一步审核岗位ID
        List<DictInfo> dictInfo = this.dictConnectorImpl.findDictInfoListByType("onLinePostID12");
        String dictValue12 = dictInfo.get(0).getValue();
        
        //非12万申请流程，最后一步审核岗位ID
         dictInfo = this.dictConnectorImpl.findDictInfoListByType("onLinePostIDNot12");
        String dictValueNo12 = dictInfo.get(0).getValue();
    	
    	//确定业务类型细分是【 “12万旗舰店申请”或“12万续约”】
	    	String sql = "select * from kv_record where business_key = "+map.get("business_key")+" and  businessDetailId in ('8','9','10','11')";
	    	List<Map<String, Object>>   s = jdbcTemplate.queryForList(sql);
	    	if(s!=null&&s.size()>0){
    	
	    		if (partyDTO.size() > 0) {
		             for (int i = 0; i < partyDTO.size(); i++) {
		            	  post_id =Long.toString( partyDTO.get(i).getId());
		            	  
		            	  //若当前登录人岗位     12万申请流程，最后一步审核岗位ID
		            	  if(post_id.equals(dictValue12)&&chrIsAuthCertificate=="1"){
		            		  //是 返回1
		            		  isAuth = "1";
      		    	break;	 
      		    	}
            	  }
            	 
             }
         }
	    	
	    	//确定业务类型细分是【 “非12万旗舰店申请”或“非12万续约”】
	    	sql = "select * from kv_record where business_key = "+map.get("business_key")+" and  businessDetailId in ('9','11')";
	    	List<Map<String, Object>>   busidetail = jdbcTemplate.queryForList(sql);
	    	if(busidetail!=null&&busidetail.size()>0){
	    			if (partyDTO.size() > 0) {
		             for (int i = 0; i < partyDTO.size(); i++) {
		            	  post_id =Long.toString( partyDTO.get(i).getId());
		            	  
		            	  //若当前登录人岗位    非12万申请流程，最后一步审核岗位
		            	  if(post_id.equals(dictValueNo12)&&chrIsAuthCertificate=="1"){
		            		  //是 返回1
		            		  isAuth = "1";
		            		  break;	 
		            	  }
		             }
	    		}
	    	}*/
    	returnMap.put("isauth", isAuth);
    	return returnMap;
    }
    
    
    @GET
    @RequestMapping("operation-OnLine-Form")
    @Produces(MediaType.APPLICATION_JSON)
    public String getOnLineFormDetail(
    		Model model, 
    		@RequestParam("processInstanceId") String processInstanceId,
    		@RequestParam(value = "isPrint", required = false) boolean isPrint,
    		@RequestParam(value = "viewBack", required = false) boolean viewBack) {
    	
    	Map<String, Object> returnMap=new HashMap<String, Object>();
    	String strSql="SELECT f_OnlineType(o.chrApplyType) AS appType,ifnull(r.detailHtml,'') detailHtml,r.audit_status AS auditStatus from kv_record r "
						+" INNER JOIN ro_pf_oaonline o ON o.id=r.pk_id ";
						
    	String strOnlineSql=strSql+" where r.ref='"+processInstanceId+"'";
    	List<Map<String, Object>> returnMapList=jdbcTemplate.queryForList(strOnlineSql);
    	if(returnMapList.size()>0)
    		returnMap=returnMapList.get(0);
    	else {
    		TaskInfo taskStart=taskInfoManager.findUnique("from TaskInfo where catalog='start' and PROCESS_INSTANCE_ID=?", processInstanceId);
    		String strKVSql=strSql+" where r.id='"+taskStart.getBusinessKey()+"'";
    		returnMap=jdbcTemplate.queryForMap(strKVSql);
		}
    	
    	String detailHtml = returnMap.get("detailHtml").toString();
    	String taskCopyNames = customWorkService.getTaskCopyNames(processInstanceId); //ckx 获取抄送人
    	if(org.apache.commons.lang3.StringUtils.isNoneBlank(detailHtml)){
    		detailHtml = detailHtml.replace("{copyNames}", taskCopyNames);
    	}
    	model.addAttribute("detailHtml", detailHtml);
    	//审批记录
        List<HumanTaskDTO> logHumanTaskDtos = humanTaskConnector
                .findHumanTasksForPositionByProcessInstanceId(processInstanceId);
        //获得审核时长
        logHumanTaskDtos = operationService.settingAuditDuration(logHumanTaskDtos);
        model.addAttribute("logHumanTaskDtos", logHumanTaskDtos);
        model.addAttribute("isPrint", isPrint);
        model.addAttribute("viewBack", viewBack);
        operationService.copyMsgUpdate(processInstanceId);
    	return "operation/process-operation-onlineDetail";
    }
    
    @Resource
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    @Resource
    public void setHumanTaskConnector(HumanTaskConnector humanTaskConnector) {
        this.humanTaskConnector = humanTaskConnector;
    }
    
    @Resource
    public void setCurrentUserHolder(CurrentUserHolder currentUserHolder) {
        this.currentUserHolder = currentUserHolder;
    }
	
    @Resource
	public void setOrgConnector(OrgConnector orgConnector) {
		this.orgConnector = orgConnector;
	}
    
    @Resource
   	public void setDictConnectorImpl(DictConnectorImpl dictConnectorImpl) {
   		this.dictConnectorImpl = dictConnectorImpl;
   	}
    
    @Resource
	public void setTaskInfoManager(TaskInfoManager taskInfoManager) {
		this.taskInfoManager = taskInfoManager;
	}
}
