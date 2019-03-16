/**
 * 
 */
package com.mossle.H5.Approval;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.swing.plaf.synth.Region;

import com.alibaba.fastjson.JSONObject;
import com.mossle.operation.component.OperationConstants;
import com.mossle.operation.persistence.domain.Apply;
import com.mossle.operation.persistence.domain.CarApply;
import com.mossle.operation.persistence.domain.CustomApprover;
import com.mossle.operation.persistence.domain.CustomEntity;
import com.mossle.operation.persistence.domain.CustomEntityDTO;
import com.mossle.operation.persistence.domain.CustomPresetApprover;
import com.mossle.operation.persistence.domain.CustomWorkEntityDTO;
import com.mossle.operation.persistence.manager.CarApplyManager;
import com.mossle.operation.persistence.manager.CustomApproverManager;
import com.mossle.operation.persistence.manager.CustomManager;
import com.mossle.operation.rs.WSApiResource;
import com.mossle.operation.service.CustomService;
import com.mossle.operation.service.CustomWorkService;
import com.mossle.operation.service.OperationService;
import com.mossle.operation.service.WSApplyService;

import org.codehaus.janino.Java.NewAnonymousClassInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mossle.api.keyvalue.KeyValueConnector;
import com.mossle.common.utils.FileUploadAPI;
import com.mossle.common.utils.StringUtils;
import com.mossle.common.utils.WebAPI;
import com.mossle.core.MultipartHandler;
import com.mossle.core.annotation.Log;
import com.mossle.core.auth.CurrentUserHolder;
import com.mossle.core.auth.CustomPasswordEncoder;
import com.mossle.core.id.IdGenerator;
import com.mossle.core.mapper.JsonMapper;
import com.mossle.core.page.Page;
import com.mossle.dict.persistence.domain.DictInfo;
import com.mossle.humantask.persistence.domain.TaskInfo;
import com.mossle.humantask.persistence.domain.TaskInfoApprovePosition;
import com.mossle.humantask.persistence.domain.TaskInfoCopy;
import com.mossle.humantask.persistence.manager.TaskInfoApprovePositionManager;
import com.mossle.humantask.persistence.manager.TaskInfoCopyManager;
import com.mossle.humantask.persistence.manager.TaskInfoManager;
import com.mossle.internal.store.persistence.domain.StoreInfo;
import com.mossle.keyvalue.persistence.domain.TimeTaskInfo;
import com.mossle.keyvalue.persistence.manager.TimeTaskManager;
import com.mossle.msg.persistence.manager.MsgInfoManager;
import com.mossle.user.persistence.domain.AccountCredential;
import com.mossle.user.persistence.domain.AccountInfo;
import com.mossle.user.persistence.manager.AccountCredentialManager;
import com.mossle.user.persistence.manager.AccountInfoManager;
import com.mossle.user.persistence.manager.PersonInfoManager;
import com.mossle.user.service.UserService;
import com.mossle.user.support.ChangePasswordResult;
import com.mossle.util.DateUtil;
import com.mossle.util.StringUtil;
import com.mossle.ws.persistence.domain.OnLineInfo;
import com.mossle.ws.persistence.manager.OnLineInfoManager;
import com.mossle.operation.service.OperationService;
import com.mossle.operation.web.ProcessOperationController;
import com.mossle.operation.web.ProcessOperationControllerCustom;
import com.mossle.operation.web.ProcessOperationControllerOa;
import com.mossle.operation.web.ProcessOperationOnlineController;
//import com.mossle.operation.web.Sring;
import com.mossle.party.PartyConstants;
import com.mossle.party.persistence.domain.PartyEntity;
import com.mossle.party.persistence.manager.PartyEntityManager;
import com.mossle.api.custom.CustomConnector;
import com.mossle.api.dict.DictConnector;
import com.mossle.api.humantask.HumanTaskConnector;
import com.mossle.api.humantask.HumanTaskConstants;
import com.mossle.api.humantask.HumanTaskDTO;
import com.mossle.api.keyvalue.FormParameter;
import com.mossle.api.keyvalue.KeyValueConnector;
import com.mossle.api.keyvalue.Record;
import com.mossle.api.operation.OperationConnector;
import com.mossle.api.org.OrgConnector;
import com.mossle.api.party.PartyConnector;
import com.mossle.api.party.PartyDTO;
import com.mossle.base.persistence.manager.BusinessTypeManager;
import com.mossle.base.persistence.domain.BusinessTypeEntity;
import com.mossle.base.persistence.domain.DetailPostEntity;
import com.mossle.base.persistence.manager.BusinessDetailManager;
import com.mossle.base.persistence.domain.BusinessDetailEntity;
import com.mossle.base.rs.BusinessResource.BusinessDetailDTO;
import com.mossle.bpm.web.WorkspaceController;
import com.mossle.api.srmb.SrmbDTO;
import com.mossle.api.store.StoreConnector;
import com.mossle.api.tenant.TenantHolder;
import com.mossle.api.user.UserConnector;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * @author lilei
 *
 */
@Service
@Transactional(readOnly = true)
public class ApprovalService {
	private static Logger logger = LoggerFactory.getLogger(ApprovalService.class);
	private PersonInfoManager personInfoManager;
	private OrgConnector orgConnector;
	private BusinessTypeManager businessTypeManager;
	private BusinessDetailManager businessDetailManager;
	private TenantHolder tenantHolder;
	private JdbcTemplate jdbcTemplate;
	private PartyEntityManager partyEntityManager;
	private PartyConnector partyConnector;
	private HumanTaskConnector humanTaskConnector;
	private DictConnector dictConnector;
	private AccountInfoManager accountInfoManager;
	private FileUploadAPI fileUploadAPI;
	private WebAPI webAPI;
	private StoreConnector storeConnector;
	@Autowired
	private TaskInfoManager	 taskInfoManager;
	private String humanTaskId;
	private KeyValueConnector keyValueConnector;
	private OperationService operationService;
	@Autowired
	private CurrentUserHolder currentUserHolder;
	private ProcessOperationControllerCustom processOperationControllerCustom;
	private ProcessOperationController processOperationController;
	private CustomManager customManager;
	private MultipartResolver multipartResolver;
	private CustomService customService ;
	private IdGenerator idGenerator;
	private AccountCredentialManager accountCredentialManager;
    private CustomPasswordEncoder customPasswordEncoder;
    private CustomApproverManager customApproverManager;
    private CustomConnector customConnector;
    private UserConnector userConnector;
    private WSApplyService wSApplyService;
    @Autowired
    private WorkspaceController workspaceController;
    private ProcessOperationControllerOa processOperationControllerOa;
    private TimeTaskManager timeTaskManager;
    private ProcessOperationOnlineController processOperationOnlineController;
    private WSApiResource wSApiResource;
    private OnLineInfoManager onLineInfoManager;
    @Autowired
    private CustomWorkService customWorkService;
    @Autowired
    private UserService userService;
    @Autowired
    private MsgInfoManager msgInfoManager;
    private TaskInfoApprovePositionManager taskInfoApprovePositionManager;
    @Autowired
    private OperationConnector operationConnector;
    @Autowired
    private TaskInfoCopyManager taskInfoCopyManager;
    @Autowired
    private CarApplyManager carApplyManager;
    
	/**
	 * 
	 * 得到业务类型一级类型
	 * lilei at 2017.09.29
	 * 
	 * **/
	@Log(desc = "查询审批的业务类型（一级）", action = "search", operationDesc = "手机APP-审批-查询审批的业务类型（一级）")
	public Map<String, Object> GetFirstBusinessType(Map<String, Object> decryptedMap){
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			
		
		//orgConnector.
		//验证参数================================================================================
		if (!decryptedMap.containsKey("strPerCode") || StringUtils.isBlank(decryptedMap.get("strPerCode").toString())) {
			returnMap.put("bSuccess", "false");
			returnMap.put("strMsg", "参数错误");
			logger.info("手机APP-审批-查询审批的业务类型（一级）-手机没有传入strPerCode参数");
			return returnMap;
		}
		
		//获取数据================================================
		String percode =decryptedMap.get("strPerCode").toString();
		
		logger.info("手机APP-我的传入参数：strPerCode="+percode);
		
		
		
		//String department=personInfo.getDepartmentCode();
		//PartyOrgConnector partyOrgConnector=new PartyOrgConnector();
		
		PartyEntity partyEntity=orgConnector.findUser(percode);
		if(partyEntity==null)
		{
			returnMap.put("bSuccess", "false");
			returnMap.put("strMsg", "未查询到您的信息");
			logger.info("手机APP-审批-查询审批的业务类型（一级）-为查询到组织个人（PartyEntity）");
			return returnMap;
		}
		
		//String comCodeString=GetCompanyPartyCode(partyEntity);
		//if(comCodeString.equals("")){
			//comCodeString="0";
		//}
		String tenantid=tenantHolder.getTenantId();
		
		// PartyDTO partyDTO= partyConnector.findDepartmentById(percode);
		
		//String hlSql="from BusinessTypeEntity where departmentCode like '%"+partyDTO.getId()+"%' and tenantId="+tenantid+" and enable='是'";
		String hlSql = "from BusinessTypeEntity where tenantId=" + tenantid + " and enable='是'";
		List<BusinessTypeEntity> entityList=businessTypeManager.find(hlSql);
		
		List<Map<String,Object>> list=new ArrayList<Map<String,Object>>();
		if(entityList!=null&&entityList.size()>0){
			for(BusinessTypeEntity entity:entityList){
				Map<String,Object> map=new HashMap<String, Object>();
				map.put("intBTID", entity.getId().toString());
				map.put("varBusinessType", entity.getBusinesstype());
				list.add(map);
			}
		}
		
		returnMap.put("bSuccess", "true");
		returnMap.put("strMsg", "加载成功");
		returnMap.put("BussinessType", list);
		} catch (Exception ex) {
			returnMap.put("bSuccess", "false");
			returnMap.put("strMsg", "加载错误，请联系管理员");
			logger.info("手机APP--审批-查询审批的业务类型（一级）-查询异常："
					+ex.getMessage()+"\r\n"+ex.getStackTrace());
		}
		return returnMap;
	}
	
	/**
	 * 根据人得到公司编号
	 * @param PartyEntity 人的组织实体
	 * **/
	private String GetCompanyPartyCode(PartyEntity party)
	{
		String comCodeString="";
		int i=1;
		//最多循环20层，组织结构最多也就20层了把，hehe，可防止死循环
		while(party!=null&&!party.getPartyType().getId().equals(PartyConstants.PARTY_TYPE_DEPARTMENT)&&i<21)
		{
			party=orgConnector.findUpperDepartment(party, true);
			if(party!=null&&party.getPartyType().getId().equals(PartyConstants.PARTY_TYPE_DEPARTMENT)){
				comCodeString=party.getId().toString();
				break;
			}
			i++;
		}
		return comCodeString;
	}
	
	/**
	 * 
	 * 根据一级业务类型得到二级业务类型
	 * lilei at 2017.09.29
	 * 
	 * **/
	@Log(desc = "查询审批的业务类型（二级）", action = "search", operationDesc = "手机APP-审批-查询审批的业务类型（二级）")
	public Map<String, Object> GetSecondBusinessType(Map<String, Object> decryptedMap){
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			//orgConnector.
			//验证参数================================================================================
			if (!decryptedMap.containsKey("strPerCode") || StringUtils.isBlank(decryptedMap.get("strPerCode").toString())
				||!decryptedMap.containsKey("strBusType") || StringUtils.isBlank(decryptedMap.get("strBusType").toString())) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "参数错误");
				logger.info("手机APP-审批-查询审批的业务类型（二级）-手机没有传入strPerCode或者strBusType参数");
				return returnMap;
			}
			
			//获取数据================================================
			String percode =decryptedMap.get("strPerCode").toString();
			String strBusType=decryptedMap.get("strBusType").toString();
			
			logger.info("手机APP-我的传入参数：strPerCode="+percode+"；strBusType="+strBusType);
			PartyEntity partyEntity=orgConnector.findUser(percode);
			if(partyEntity==null)
			{
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "未查询到您的信息");
				logger.info("手机APP-审批-查询审批的业务类型（二级）-为查询到组织个人（PartyEntity）");
				return returnMap;
			}
			
			/*String comCodeString=GetCompanyPartyCode(partyEntity);
			if(comCodeString.equals("")){
				comCodeString="0";
			}*/
			String tenantid=tenantHolder.getTenantId();
			
			String hlSql="from BusinessDetailEntity where typeId=? and tenantId=? and enable='是'";
			List<BusinessDetailEntity> entityList=businessDetailManager.find(hlSql,Long.valueOf(strBusType),tenantid);
			List<Map<String,Object>> list=new ArrayList<Map<String,Object>>();
			if(entityList!=null&&entityList.size()>0){
				for(BusinessDetailEntity entity:entityList){
					Map<String,Object> map=new HashMap<String, Object>();
					map.put("intBDID", entity.getId());
					map.put("varDetails", entity.getBusiDetail());
					list.add(map);
				}
				returnMap.put("bSuccess", "true");
				returnMap.put("strMsg", "加载成功");
				returnMap.put("BussinessDetails", list);
			}
			else {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "获取成功");
			}
			
			
			} catch (Exception ex) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "加载错误，请联系管理员");
				logger.info("手机APP--审批-查询审批的业务类型（二级）-查询异常："
						+ex.getMessage()+"\r\n"+ex.getStackTrace());
			}
			return returnMap;
	}
	
	/**
	 * 
	 * 初始化大区数据
	 * lilei at 2017.10.09
	 * 
	 * **/
	@Log(desc = "初始化大区数据", action = "search", operationDesc = "手机APP-审批-初始化大区数据")
	public Map<String, Object> GetInitAreaList(Map<String, Object> decryptedMap){
		// {strPerCode=2, percode=2, method=ToH5LoadArea,
		// sign=f953b4c3460eb4e790b99538244b5e3b, timestamp=}
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			//orgConnector.
			//验证参数================================================================================
			if (!decryptedMap.containsKey("strPerCode") || StringUtils.isBlank(decryptedMap.get("strPerCode").toString())) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "参数错误");
				logger.info("手机APP-审批-初始化大区数据-手机没有传入strPerCode");
				return returnMap;
			}
			
			//获取数据================================================
			String percode =decryptedMap.get("strPerCode").toString();
			
			logger.info("手机APP-我的传入参数：strPerCode="+percode);
			PartyEntity partyEntity=orgConnector.findUser(percode);
			if(partyEntity==null)
			{
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "未查询到您的信息");
				logger.info("手机APP-审批-初始化大区数据-为查询到组织个人（PartyEntity）");
				return returnMap;
			}
			
			List<PartyEntity> partylist=GetAreaEntityList(percode);
			
			/*{"intDicID":"字典ID",
			"varDTCode":"字典类型编号",
			"varDicCode":"字典编号",
			"varDicName":"字典名称",
			"varDicValue":"字典值",
			"varRemark":"备注",
			"varOperateMan":"操作人",
			"dtmAddTime":"操作时间",
			"dtmUpdTime":"更新时间"
			}*/
			
			List<Map<String,Object>> list=new ArrayList<Map<String,Object>>();
			if(partylist!=null&&partylist.size()>0){
				for(PartyEntity entity:partylist){
					Map<String,Object> map=new HashMap<String, Object>();
					map.put("intDicID", entity.getId().toString());
					map.put("varDTCode", "");
					map.put("varDicCode",entity.getId().toString());
					map.put("varDicName",entity.getName());
					map.put("varDicValue", entity.getId().toString());
					map.put("varRemark", "");
					map.put("varOperateMan", "");
					map.put("dtmAddTime", new Date());
					map.put("dtmUpdTime",  new Date());
					list.add(map);
				}
			}
			
			returnMap.put("bSuccess", "true");
			returnMap.put("strMsg", "加载成功");
			returnMap.put("AreaData", list);
			} catch (Exception ex) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "加载错误，请联系管理员");
				logger.info("手机APP-审批-初始化大区数据-查询异常："
						+ex.getMessage()+"\r\n"+ex.getStackTrace());
			}
			return returnMap;
	}
	
	/**
	 * 
	 * 根据大区加载分公司数据
	 * lilei at 2017.10.09
	 * 
	 * **/
	@Log(desc = "根据大区加载分公司数据", action = "search", operationDesc = "手机APP-审批-根据大区加载分公司数据")
	public Map<String, Object> GetCompanyByArea(Map<String, Object> decryptedMap){
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			//orgConnector.
			//验证参数================================================================================
			if (!decryptedMap.containsKey("strPerCode")||decryptedMap.get("strPerCode")==(null)||decryptedMap.get("strPerCode").equals("")||
				!decryptedMap.containsKey("strAreaID")||decryptedMap.get("strAreaID")==(null)||decryptedMap.get("strAreaID").equals("")) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "参数错误");
				logger.info("手机APP-审批-根据大区加载分公司数据-手机没有传入strPerCode或者strAreaID没有传入");
				return returnMap;
			}
			
			//获取数据================================================
			String percode =decryptedMap.get("strPerCode").toString();
			String strAreaID =decryptedMap.get("strAreaID").toString();
			
			logger.info("手机APP-我的传入参数：strPerCode="+percode);
			PartyEntity partyEntity=orgConnector.findUser(percode);
			if(partyEntity==null)
			{
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "未查询到您的信息");
				logger.info("手机APP-审批-根据大区加载分公司数据-未查询到组织个人（PartyEntity）");
				return returnMap;
			}
			
			PartyDTO partyDTO=partyConnector.findCompanyById(percode);
			
			String sqlString="SELECT e.* FROM PARTY_STRUCT s "
							+"INNER JOIN PARTY_ENTITY e on s.CHILD_ENTITY_ID=e.ID "
							+"where e.TYPE_ID=2 and s.PARENT_ENTITY_ID="+strAreaID;
			
			List<Map<String,Object>> maplist=jdbcTemplate.queryForList(sqlString);
			
			if(partyDTO!=null&&maplist!=null&&maplist.size()>0)
			{
				for(Map<String,Object> entity:maplist){
					if(entity.get("ID").equals(partyDTO.getId()))
					{
						maplist.clear();
						Map<String,Object> mapNew=new HashMap<String, Object>();
						mapNew.put("ID", partyDTO.getId());
						mapNew.put("Name", partyDTO.getName());
						maplist.add(mapNew);
					}
				}
			}
			
			List<PartyEntity> partylist=GetAreaEntityList(percode);
			
			List<Map<String,Object>> list=new ArrayList<Map<String,Object>>();
			if(partylist!=null&&partylist.size()>0){
				for(Map<String,Object> entity:maplist){
					Map<String,Object> map=new HashMap<String, Object>();
					map.put("intDicID", "");
					map.put("varDTCode", "");
					map.put("varDicCode",entity.get("ID").toString());
					map.put("varDicName",entity.get("Name").toString());
					map.put("varDicValue", entity.get("ID").toString());
					map.put("varRemark", "");
					map.put("varOperateMan", "");
					map.put("dtmAddTime", new Date());
					map.put("dtmUpdTime",  new Date());
					list.add(map);
				}
				returnMap.put("strMsg", "加载成功");
				returnMap.put("SubCom", list);
			}
			else {
				returnMap.put("strMsg", "获取成功");
			}
			returnMap.put("bSuccess", "true");
			
			
			} catch (Exception ex) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "加载错误，请联系管理员");
				logger.info("手机APP-审批-根据大区加载分公司数-查询异常："
						+ex.getMessage()+"\r\n"+ex.getStackTrace());
			}
			return returnMap;
	}
	
	/**
	 * 
	 * 根据人大区（无绑定则，返回所有大区）
	 * @param PartyEntity 人的组织实体
	 * 
	 * **/
	private List<PartyEntity> GetAreaEntityList(String userId)
	{
		List<PartyEntity> arealist=new ArrayList<PartyEntity>();
		
		PartyEntity party=orgConnector.findPartyAreaByUserId(userId);
		if(party!=null)
			arealist.add(party);
		else {
			String hsql=" from PartyEntity where partyType.id=?";
			arealist=partyEntityManager.findBy("partyType.id", 6L);
		}
		
		return arealist;
	}
	
	/**
	 * 
	 * 查询我的申请
	 * lilei at 2017.10.09
	 * 
	 * **/
	@Log(desc = "查询我的申请", action = "search", operationDesc = "手机APP-审批-查询我的申请")
	public Map<String, Object> GetMyApplication(Map<String, Object> decryptedMap){
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			//orgConnector.h)	strPageSize
			//验证参数================================================================================
			if (!decryptedMap.containsKey("strPerCode") || StringUtils.isBlank(decryptedMap.get("strPerCode").toString())
				||!decryptedMap.containsKey("strPageSize") || StringUtils.isBlank(decryptedMap.get("strPageSize").toString())) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "参数错误");
				logger.info("手机APP-审批-查询我的申请-手机没有传入strPerCode或者strPageSize参数");
				return returnMap;
			}
			
			//获取数据================================================
			String percode =decryptedMap.get("strPerCode").toString();
			String strStatus =decryptedMap.get("strStatus").toString();
			String strUserID =decryptedMap.get("strUserID").toString();
			String strBusinessType =decryptedMap.get("strBusinessType").toString();
			String strBDID =decryptedMap.get("strBDID").toString();
			String strStart =decryptedMap.get("strStart").toString();
			String strEnd =decryptedMap.get("strEnd").toString();
			String strPageSize =decryptedMap.get("strPageSize").toString();
			String strPageIndex=decryptedMap.get("strPageIndex").toString();
			
			StringBuffer sbBuffer=new StringBuffer();
			sbBuffer.append("；strStatus="+strStatus);
			sbBuffer.append("；strUserID="+strUserID);
			sbBuffer.append("；strBusinessType="+strBusinessType);
			sbBuffer.append("；strBDID="+strBDID);
			sbBuffer.append("；strStart="+strStart);
			sbBuffer.append("；strEnd="+strEnd);
			sbBuffer.append("；strPageSize="+strPageSize);
			sbBuffer.append("；strPageIndex="+strPageIndex);
			logger.info("手机APP-审批-查询我的申请-传入参数：strPerCode="+percode+sbBuffer.toString());
			
			PartyEntity partyEntity=orgConnector.findUser(percode);
			if(partyEntity==null)
			{
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "未查询到您的信息");
				logger.info("手机APP-审批-查询我的申请-为查询到组织个人（PartyEntity）");
				return returnMap;
			}
					
			StringBuffer postIDBuffer = new StringBuffer("");
	        List<PartyEntity> postlist = orgConnector.getPostByUserId(percode);
			if (postlist != null && postlist.size() > 0) {
				for (PartyEntity party : postlist) {
					postIDBuffer.append(party.getId() + ",");
				}
				postIDBuffer.delete(postIDBuffer.length() - 1, postIDBuffer.length());
			}
	        String 	postID = postIDBuffer.toString();
	        if (StringUtils.isBlank(postID)) {
	        	postID = percode;
	        } else {
	        	postID = postID + "," + percode;
	        }
	        
			//List<Map<String, Object>> list = jdbcTemplate.queryForList(selectSql,params);
			
			/*String hlSql="select * from (select hi.id_ as id,hi.proc_inst_id_ as proc_inst_id,"
    	 		 +" hi.business_key_ as business_key,hi.proc_def_id_ as proc_def_id,r.create_time as start_time,hi.end_time_ as end_time,"
    	 		+" hi.name_ as pro_name,hi.start_user_id_ as start_user_id,r.id as record_id,r.category,"
    	 		 +" r.`status`,r.`name` as record_name,r.theme,r.applyCode,r.ucode,"
    	 		 +" IFNULL(r.businessTypeName,'') as businessTypeName,r.businessDetailName,r.submitTimes,r.businessTypeId,r.businessDetailId,r.url,'普通流程' as pro_flag,"
            +" r.audit_status as pro_status,p.full_name"
    	 		  +" from act_hi_procinst hi"
    	 		  +" inner join kv_record r on hi.business_key_ = r.business_key"
    	 		 +" inner join person_info p on r.USER_ID=p.ID"
    	 		  +" where 1=1 and hi.start_user_id_ ="+percode
    	 		  +" union all"
    	 		  +" select k.id,k.process_instance_id,k.business_key,'自定义申请',r.create_time,r.end_time,k.presentation_subject,r.user_id,r.id,"
    	 		 +" r.category,r.`status`,r.`name`,r.theme,r.applyCode,r.ucode,"
    	 		 +" IFNULL(r.businessTypeName,'') as businessTypeName,r.businessDetailName,r.submitTimes,r.businessTypeId,r.businessDetailId,r.url,k.suspend_Status,"
           +" r.audit_status as pro_status,p.full_name"
    	 		  +" from task_info k"
    	 		  +" inner join kv_record r on k.business_key = r.business_key and k.ASSIGNEE = r.user_id"
    	 		 +" inner join person_info p on r.USER_ID=p.ID"
    	 		  +" where 1=1 and k.suspend_Status = '自定义申请' and  k.catalog='start'"
    	 		  + " and r.user_id = "+percode
    	 		  + ") t"
    	 		  +" where t.start_user_id="+percode;*/
			
			/*StringBuilder buff = new StringBuilder();
			buff.append("SELECT * FROM (SELECT k.id AS id,k.process_instance_id AS proc_inst_id,k.business_key AS business_key,");
			buff.append("'' AS proc_def_id,r.create_time AS start_time,r.end_time AS end_time,k.presentation_subject AS pro_name,");
			buff.append("r.user_id AS start_user_id,r.id AS record_id,r.category,r.`status`,r.`name` AS record_name,r.theme,");
			buff.append("r.applyCode,r.ucode,IFNULL(r.businessTypeName, '') AS businessTypeName,r.businessDetailName,");
			buff.append("r.submitTimes,r.businessTypeId,r.businessDetailId,r.url,'普通流程' AS pro_flag,r.audit_status AS pro_status,p.full_name");
			buff.append(" FROM task_info k");
			buff.append(" INNER JOIN kv_record r ON k.business_key = r.business_key AND k.ASSIGNEE = r.user_id");
			buff.append(" inner JOIN task_info_approve_position tap ON k.ID = tap.task_id");
			buff.append(" INNER JOIN person_info p ON r.USER_ID = p.ID");
			buff.append(" WHERE r.businessTypeId <> '9999' AND k.catalog = 'start' and tap.position_id in(").append(postID).append(")");
			buff.append(" UNION ALL");
			buff.append(" SELECT k.id,k.process_instance_id,k.business_key,'自定义申请',r.create_time,r.end_time,k.presentation_subject,");
			buff.append("r.user_id,r.id,r.category,r.`status`,r.`name`,r.theme,r.applyCode,r.ucode,IFNULL(r.businessTypeName, '') AS businessTypeName,");
			buff.append("r.businessDetailName,r.submitTimes,r.businessTypeId,r.businessDetailId,r.url,k.suspend_Status,r.audit_status AS pro_status,p.full_name");
			buff.append(" FROM task_info k");
			buff.append(" INNER JOIN kv_record r ON k.business_key = r.business_key AND k.ASSIGNEE = r.user_id");
			buff.append(" INNER JOIN person_info p ON r.USER_ID = p.ID");
			buff.append(" inner JOIN task_info_approve_position tap ON k.ID = tap.task_id");
			buff.append(" WHERE r.businessTypeId = '9999' AND k.catalog = 'start' and tap.position_id in(").append(postID).append(",").append(percode).append(")");
			buff.append(") t WHERE 1=1");*/
			
			// 未结
			StringBuilder buffRun = new StringBuilder();
			buffRun.append("SELECT hi.id_ AS id,hi.proc_inst_id_ AS proc_inst_id,hi.business_key_ AS business_key,hi.proc_def_id_ AS proc_def_id,");
			buffRun.append("r.CREATE_TIME AS start_time,hi.end_time_ AS end_time,hi.name_ AS pro_name,hi.start_user_id_ AS start_user_id,");
			buffRun.append("r.id AS record_id,r.category,r.`status`,r.`name` AS record_name,r.theme,r.applyCode,r.ucode,r.audit_status pro_status,");
			buffRun.append("IFNULL(r.businessTypeName, '') AS businessTypeName,r.businessDetailName,r.submitTimes,r.businessTypeId,");
			buffRun.append("r.businessDetailId,r.url,'普通流程' AS pro_flag,p.full_name");
			buffRun.append(" FROM act_hi_procinst hi");
			buffRun.append(" INNER JOIN kv_record r ON hi.business_key_ = r.id");
			buffRun.append(" INNER JOIN person_info p ON r.USER_ID = p.ID");
			buffRun.append(" WHERE hi.start_user_id_ = '").append(percode).append("' AND hi.end_time_ IS NULL");
			buffRun.append(" UNION ALL");
			buffRun.append(" SELECT k.id,k.process_instance_id,k.business_key,'自定义申请',r.create_time,r.end_time,k.presentation_subject,");
			buffRun.append("r.user_id,r.id,r.category,r.`status`,r.`name`,r.theme,r.applyCode,r.ucode,r.audit_status pro_status,");
			buffRun.append("IFNULL(r.businessTypeName, '') AS businessTypeName,r.businessDetailName,r.submitTimes,");
			buffRun.append("r.businessTypeId,r.businessDetailId,r.url,k.suspend_Status,p.full_name");
			buffRun.append(" FROM task_info k");
			buffRun.append(" INNER JOIN kv_record r ON k.business_key = r.id AND k.ASSIGNEE = r.user_id");
			buffRun.append(" INNER JOIN person_info p ON r.USER_ID = p.ID");
			buffRun.append(" WHERE r.user_id = '").append(percode).append("'");
			buffRun.append(" AND (r.audit_status != '2' AND r.audit_status != '3' AND r.audit_status != '6')");
			buffRun.append(" AND r.businessTypeId = '9999' AND k.catalog = 'start'");
			
			// 已结
			StringBuilder buffFinish = new StringBuilder();
			buffFinish.append("SELECT k.id,k.process_instance_id,k.business_key,'',r.create_time,r.end_time,k.presentation_subject,");
			buffFinish.append("r.user_id,r.id,r.category,r.`status`,r.`name`,r.theme,r.applyCode,r.ucode,r.audit_status pro_status,");
			buffFinish.append("IFNULL(r.businessTypeName, '') AS businessTypeName,r.businessDetailName,r.submitTimes,r.businessTypeId,");
			buffFinish.append("r.businessDetailId,r.url,k.suspend_Status,p.full_name");
			buffFinish.append(" FROM task_info k");
			buffFinish.append(" INNER JOIN kv_record r ON k.business_key = r.id AND k.assignee = r.user_id");
			buffFinish.append(" JOIN task_info_approve_position tap ON k.BUSINESS_KEY = tap.BUSINESS_KEY AND k.ID = tap.task_id");
			buffFinish.append(" INNER JOIN person_info p ON r.USER_ID = p.ID");
			buffFinish.append(" WHERE tap.position_id IN (").append(postID).append(")");
			buffFinish.append(" AND (r.audit_status = '2' OR r.audit_status = '3' OR r.audit_status = '6')");
			buffFinish.append(" AND k.catalog = 'start'");
			
			StringBuilder buff = new StringBuilder();
			buff.append("SELECT DISTINCT t.* FROM (").append(buffRun).append(" union all ").append(buffFinish).append(") t where 1=1");
			
			//状态	
			if(strStatus!=null&&!strStatus.equals(""))
				buff.append(" and t.pro_status="+strStatus);
			//经销商编号
			if(strUserID!=null&&!strUserID.equals(""))
				buff.append(" and t.ucode="+strUserID);
			//业务类型
			if(strBusinessType!=null&&!strBusinessType.equals(""))
				buff.append(" and t.businessTypeId="+strBusinessType);
			//业务细分
			if(strBDID!=null&&!strBDID.equals(""))
				buff.append(" and t.businessDetailId="+strBDID);
			
			//String string = "2016-10-24 21:59:06";
			//SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			//System.out.println(sdf.parse(string));
			//申请日期开始
			if(strStart!=null&&!strStart.equals(""))
				buff.append(" and t.start_time>='"+strStart+"'");
			//申请日期结束
			if(strEnd!=null&&!strEnd.equals(""))
				buff.append(" and t.start_time<='"+strEnd+"'");
			
			buff.append(" order by t.start_time desc limit "+(Long.valueOf(strPageIndex)-1)*Long.valueOf(strPageSize)+","+strPageSize);
			String sql = buff.toString();
			
			List<Map<String, Object>> list=jdbcTemplate.queryForList(sql);
			
			returnMap.put("bSuccess", "true");
			if(list==null||list.size()<1)
				returnMap.put("strMsg", "暂无数据");
			else {
				
				List<Map<String, Object>> returnList=new ArrayList<Map<String,Object>>();
				for(Map<String, Object> map:list)
				{
					Map<String, Object> mapNew=new HashMap<String, Object>();
					mapNew.put("varFRCode",map.get("applycode")==null?"":map.get("applycode").toString());
					mapNew.put("varRealName",map.get("full_name")==null?"":map.get("full_name").toString());
					mapNew.put("chrStatus",map.get("pro_status")==null?"":map.get("pro_status").toString());// add by lilei at 2018-11-05
					mapNew.put("varStatus",GetProcessStatusDesc(map.get("pro_status")==null?"":map.get("pro_status").toString()));
					mapNew.put("varUserID",map.get("ucode")==null?"":map.get("ucode").toString());
					mapNew.put("varBusinessType",map.get("businessTypeName")==null?"":map.get("businessTypeName").toString());
					// mapNew.put("varDetails",map.get("businessDetailName")==null?"":map.get("businessDetailName").toString());
					mapNew.put("varArea","");
					mapNew.put("varSystem","");
					mapNew.put("varCompany","");
					mapNew.put("dtmOperateTime",map.get("start_time"));
					if (StringUtils.isBlank(map.get("theme").toString())) {
						mapNew.put("varDetails",map.get("businessDetailName")==null?"":map.get("businessDetailName").toString());
					} else {
						mapNew.put("varDetails",map.get("theme"));
					}
					
					returnList.add(mapNew);
				}
				returnMap.put("strMsg", "加载成功");
				returnMap.put("MyRecord", returnList);
			}
			} catch (Exception ex) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "加载错误，请联系管理员");
				logger.info("手机APP-审批-查询我的申请-查询异常："
						+ex.getMessage()+"\r\n"+ex.getStackTrace());
			}
			return returnMap;
	}
	
	/**
	 * 
	 * 查询待我审批
	 * lilei at 2017.10.09
	 * 
	 * **/
	@Log(desc = "查询待我审批", action = "search", operationDesc = "手机APP-审批-查询待我审批")
	public Map<String, Object> GetWaitAuditApproval(Map<String, Object> decryptedMap){
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			//orgConnector.h)	strPageSize
			//验证参数================================================================================
			if (!decryptedMap.containsKey("strPerCode") || StringUtils.isBlank(decryptedMap.get("strPerCode").toString())
				||!decryptedMap.containsKey("strPageSize") || StringUtils.isBlank(decryptedMap.get("strPageSize").toString())) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "参数错误");
				logger.info("手机APP-审批-查询待我审批-手机没有传入strPerCode或者strPageSize参数");
				return returnMap;
			}
			
			//获取数据================================================
			String percode =decryptedMap.get("strPerCode").toString();
			String strFRCode =decryptedMap.get("strFRCode").toString();
			String strRealName =decryptedMap.get("strRealName").toString();
			//String strStatus =decryptedMap.get("strStatus").toString();
			String strUserID =decryptedMap.get("strUserID").toString();
			String strBusinessType =decryptedMap.get("strBusinessType").toString();
			String strBDID =decryptedMap.get("strBDID").toString();
			String strSystem =decryptedMap.get("strSystem").toString();
			String strArea =decryptedMap.get("strArea").toString();
			String strCompany =decryptedMap.get("strCompany").toString();
			String strStart =decryptedMap.get("strStart").toString();
			String strEnd =decryptedMap.get("strEnd").toString();
			String strPageSize =decryptedMap.get("strPageSize").toString();
			String strPageIndex=decryptedMap.get("strPageIndex").toString();
			
			StringBuffer sbBuffer=new StringBuffer();
			//sbBuffer.append("；strStatus="+strStatus);
			sbBuffer.append("；strUserID="+strUserID);
			sbBuffer.append("；strBusinessType="+strBusinessType);
			sbBuffer.append("；strBDID="+strBDID);
			sbBuffer.append("；strStart="+strStart);
			sbBuffer.append("；strEnd="+strEnd);
			sbBuffer.append("；strPageSize="+strPageSize);
			sbBuffer.append("；strPageIndex="+strPageIndex);
			logger.info("手机APP-审批-查询待我审批-传入参数：strPerCode="+percode+sbBuffer.toString());
			
			PartyEntity partyEntity=orgConnector.findUser(percode);
			if(partyEntity==null)
			{
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "未查询到您的信息");
				logger.info("手机APP-审批-查询待我审批-为查询到组织个人（PartyEntity）");
				return returnMap;
			}
				
			//Page page = humanTaskConnector.findPersonalTasks(userId, tenantId, propertyFilters, page);
			
			//List<Map<String, Object>> list = jdbcTemplate.queryForList(selectSql,params);
			String strBusinessId=GetFilterBusinessId();
			String hlSql="select * from (select i.id,i.process_instance_id,i.action,r.applycode,i.presentation_subject,"
	    			+ "r.theme,r.user_id,p.full_name,r.ucode,r.businesstypeid,IFNULL(r.businesstypename,'') as businesstypename,r.businessdetailid,"
	    			+ "r.businessdetailname,r.systemid,r.systemname,r.areaid,IFNULL(r.areaname,'') as areaname,r.companyid,IFNULL(r.companyname,'') as companyname,"
	    			+ "r.create_time,i.catalog,i.complete_time,i.assignee,r.audit_status as chrStatus"
	    			+ " from task_info i"
	    			+ " inner join kv_record r on i.business_key = r.business_key"
	    			+ " inner join person_info p on r.user_id = p.id"
	    			+ " where IFNULL(r.businesstypeid,'') not in("+strBusinessId+") and i.`status` = 'active' and i.CATALOG <>'copy' and i.assignee  ='" + percode + "') t where assignee  ='" + percode + "'";
			
			StringBuilder buff = new StringBuilder(hlSql);
			
			/*a)	strPerCode：登录用户编号（必填）；
			b)	strFRCode：受理单编号；
			c)	strRealName：申请人；
			d)	strUserID：经销商编号；
			e)	strBusinessType：业务类型；
			f)	strBDID：业务细分；
			g)	strSystem：所属体系；
			h)	strArea：所属大区；
			i)	strCompany：所属分公司；
			j)	strStart：申请起始日期；
			k)	strEnd：申请结束日期；
			l)	strPageSize：页大小（必填）；
			m)	strPageIndex：页码（必填）；*/
			
			//受理单号
			if(strFRCode!=null&&!strFRCode.equals(""))
				buff.append(" and t.applycode like '%"+strFRCode+"%'");
			//申请人
			if(strRealName!=null&&!strRealName.equals(""))
				buff.append(" and t.full_name like '%"+strRealName+"%'");
			//状态
			//if(strStatus!=null&&!strStatus.equals(""))
				//buff.append(" and t.chrStatus="+strStatus);
			//经销商编号
			if(strUserID!=null&&!strUserID.equals(""))
				buff.append(" and t.ucode="+strUserID);
			//业务类型
			if(strBusinessType!=null&&!strBusinessType.equals(""))
				buff.append(" and t.businesstypeid="+strBusinessType);
			//业务细分
			if(strBDID!=null&&!strBDID.equals(""))
				buff.append(" and t.businessdetailid="+strBDID);
			//体系
			if(strSystem!=null&&!strSystem.equals(""))
				buff.append(" and t.systemid="+strSystem);
			//大区
			if(strArea!=null&&!strArea.equals("")) {
				
				PartyDTO vo = partyConnector.findAreaById(percode);
				
				if (vo != null && vo.getId().equals(strArea)) {
					// 审批人和选择的大区为同一大区，则默认查全部    zyl 2017-10-26
				} else {
					buff.append(" and t.areaid="+strArea);
				}
			}
				
			//分公司
			if(strCompany!=null&&!strCompany.equals(""))
			{
				PartyDTO companyId = partyConnector.findCompanyById(percode);
				
				if (companyId != null && companyId.getId().equals(strArea)) {
					// 审批人和选择的大区为同一公司，则默认查全部    zyl 2017-10-26
				} else {
					buff.append(" and t.companyid="+strCompany);
				}
				
			}
				
			
			//String string = "2016-10-24 21:59:06";
			//SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			//System.out.println(sdf.parse(string));
			//申请日期开始
			if(strStart!=null&&!strStart.equals(""))
				buff.append(" and t.create_time>='"+strStart+"'");
			//申请日期结束
			if(strEnd!=null&&!strEnd.equals(""))
				buff.append(" and t.create_time<='"+strEnd+"'");
			
			buff.append(" order by t.create_time asc limit "+(Long.valueOf(strPageIndex)-1)*Long.valueOf(strPageSize)+","+strPageSize);//待审改为按时间正序排 Bing 2017.12.27
			String sql = buff.toString();
			System.out.println(sql);
			List<Map<String, Object>> list=jdbcTemplate.queryForList(sql);
			
			List<Map<String, Object>> returnList=new ArrayList<Map<String,Object>>();
			
			returnMap.put("bSuccess", "true");
			if(list==null||list.size()<1)
				returnMap.put("strMsg", "暂无数据");
			else {
				for(Map<String, Object> map:list)
				{
					/*{"bSuccess":"true","strMsg":"加载成功"," "
							"ToDealRecord":[{"varFRCode":"受理单编号",
								"varRealName":"申请人"," chrStatus":"状态(标识) ",
								" varStatus":"状态(中文)"," varUserID ":"经销商编号",
								"varBusinessType":"业务类型"," varDetails":"业务细分",
								"varSystem":"所属体系","varArea":"所属大区",
								"varCompany":"所属公司"," dtmOperateTime ":"操作时间"},{…}]}*/
					
					/*String hlSql="select * from (select i.id,i.process_instance_id,i.action,r.applycode,i.presentation_subject,"
			    			+ "r.theme,r.user_id,p.full_name,r.ucode,r.businesstypeid,r.businesstypename,r.businessdetailid,"
			    			+ "r.businessdetailname,r.systemid,r.systemname,r.areaid,r.areaname,r.companyid,r.companyname,"
			    			+ "r.create_time,i.catalog,i.complete_time,i.assignee,r.status chrStatus"
			    			+ " from task_info i"
			    			+ " inner join kv_record r on i.business_key = r.business_key"
			    			+ " inner join person_info p on r.user_id = p.id"
			    			+ " where i.`status` = 'active') t where assignee  ='" + percode + "'";*/
					
					Map<String, Object> mapNew=new HashMap<String, Object>();
					mapNew.put("varFRCode",map.get("applycode")==null?"":map.get("applycode").toString());
					mapNew.put("varRealName",map.get("full_name")==null?"":map.get("full_name").toString());
					mapNew.put("varStatus",GetProcessStatusDesc(map.get("chrStatus")==null?"":map.get("chrStatus").toString()));
					mapNew.put("varUserID",map.get("ucode")==null?"":map.get("ucode").toString());
					mapNew.put("varBusinessType",map.get("businesstypename")==null?"":map.get("businesstypename").toString());
					// mapNew.put("varDetails",map.get("businessdetailname")==null?"":map.get("businessdetailname").toString());
					mapNew.put("varSystem",map.get("systemname")==null?"":map.get("systemname").toString());
					mapNew.put("varArea",TrimNull(map.get("areaname")));
					mapNew.put("varCompany",map.get("companyname")==null?"":map.get("companyname").toString());
					mapNew.put("dtmOperateTime",map.get("create_time"));
					
					if(StringUtils.isBlank(map.get("theme").toString())) {
						mapNew.put("varDetails",map.get("businessdetailname")==null?"":map.get("businessdetailname").toString());
					} else {
						mapNew.put("varDetails",map.get("theme")==null?"":map.get("theme").toString());
					}
					returnList.add(mapNew);
				}
				returnMap.put("strMsg", "加载成功");
				returnMap.put("ToDealRecord", returnList);
				JsonMapper jsonMapper = new JsonMapper();
				System.out.println("================================== 待办审核" + percode);
				System.out.println(jsonMapper.toJson(returnList));
				System.out.println("================================== ");
			}
			} catch (Exception ex) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "加载错误，请联系管理员");
				logger.info("手机APP-审批-查询待我审批-查询异常："
						+ex.getMessage()+"\r\n"+ex.getStackTrace());
			}
			return returnMap;
	}
	
	@Log(desc = "查询经我审批", action = "search", operationDesc = "手机APP-审批-查询经我审批")
	public Map<String, Object> GetMyAuditApproval(Map<String, Object> decryptedMap){
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			//orgConnector.h)	strPageSize
			//验证参数================================================================================
			if (!decryptedMap.containsKey("strPerCode") || StringUtils.isBlank(decryptedMap.get("strPerCode").toString())
				||!decryptedMap.containsKey("strPageSize") || StringUtils.isBlank(decryptedMap.get("strPageSize").toString())) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "参数错误");
				logger.info("手机APP-审批-查询经我审批-手机没有传入strPerCode或者strPageSize参数");
				return returnMap;
			}
			
			//获取数据================================================
			String userId = decryptedMap.get("userId").toString();
			String percode =decryptedMap.get("strPerCode").toString();
			String strFRCode =decryptedMap.get("strFRCode").toString();
			String strRealName =decryptedMap.get("strRealName").toString();
			String strStatus =decryptedMap.get("strStatus").toString();
			String strUserID =decryptedMap.get("strUserID").toString();
			String strBusinessType =decryptedMap.get("strBusinessType").toString();
			String strBDID =decryptedMap.get("strBDID").toString();
			String strSystem =decryptedMap.get("strSystem").toString();
			String strArea =decryptedMap.get("strArea").toString();
			String strCompany =decryptedMap.get("strCompany").toString();
			String strStart =decryptedMap.get("strStart").toString();
			String strEnd =decryptedMap.get("strEnd").toString();
			String strPageSize =decryptedMap.get("strPageSize").toString();
			String strPageIndex=decryptedMap.get("strPageIndex").toString();
			
			StringBuffer sbBuffer=new StringBuffer();
			//sbBuffer.append("；strStatus="+strStatus);
			sbBuffer.append("；strUserID="+strUserID);
			sbBuffer.append("；strBusinessType="+strBusinessType);
			sbBuffer.append("；strBDID="+strBDID);
			sbBuffer.append("；strStart="+strStart);
			sbBuffer.append("；strEnd="+strEnd);
			sbBuffer.append("；strPageSize="+strPageSize);
			sbBuffer.append("；strPageIndex="+strPageIndex);
			logger.info("手机APP-审批-查询经我审批-传入参数：strPerCode="+percode+sbBuffer.toString());
			
			PartyEntity partyEntity=orgConnector.findUser(percode);
			if(partyEntity==null)
			{
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "未查询到您的信息");
				logger.info("手机APP-审批-查询经我审批-为查询到组织个人（PartyEntity）");
				return returnMap;
			}
				
			StringBuffer postIDBuffer = new StringBuffer("");
	        List<PartyEntity> postlist = orgConnector.getPostByUserId(percode);
			if (postlist != null && postlist.size() > 0) {
				for (PartyEntity party : postlist) {
					postIDBuffer.append(party.getId() + ",");
				}
				postIDBuffer.delete(postIDBuffer.length() - 1, postIDBuffer.length());
			}
	        String 	postID = postIDBuffer.toString();
	        if (StringUtils.isBlank(postID)) {
	        	postID = "99999";
	        }
	        
			//Page page = humanTaskConnector.findPersonalTasks(userId, tenantId, propertyFilters, page);
			
			//List<Map<String, Object>> list = jdbcTemplate.queryForList(selectSql,params);
			String strBusinessId=GetFilterBusinessId();
			/*String hlSql="select * from (select  DISTINCT i.process_instance_id,r.applycode,i.presentation_subject,"
	    			+ "r.theme,r.user_id,p.full_name,r.ucode,r.businesstypeid,IFNULL(r.businesstypename,'') as businesstypename,r.businessdetailid,"
	    			+ "r.businessdetailname,r.systemid,r.systemname,r.areaid,IFNULL(r.areaname,'') as areaname,r.companyid,IFNULL(r.companyname,'') as companyname,"
	    			+ "r.create_time,i.catalog,i.assignee,r.url,i.suspend_Status as pro_falg,r.audit_status chrStatus"
	    			+ " from task_info i"
	    			+ " inner join kv_record r on i.business_key = r.business_key"
	    			+ " inner join person_info p on r.user_id = p.id"
	    			+ " where IFNULL(r.businesstypeid,'') not in("+strBusinessId+") and  i.`status` = 'complete' and i.CATALOG <>'copy' and i.CATALOG<>'start') t where  t.assignee  ='" + percode + "'";
*/
			
			StringBuilder buff = new StringBuilder();
			buff.append("SELECT *,ti.COMPLETE_TIME FROM (");
			buff.append(" SELECT * ");
	        buff.append(" FROM ( SELECT  DISTINCT k.*,a.DISPLAY_NAME as full_name,s.process_instance_id ,s.presentation_subject,s.suspend_Status ,k.audit_status as pro_status ");
	        buff.append(" from (select  DISTINCT b.BUSINESS_KEY,b.PROCESS_INSTANCE_ID,b.presentation_subject ,b.suspend_Status,min(b.TASK_ID)");
	        buff.append(" FROM task_info b JOIN task_info_approve_position tap ON b.ID = tap.task_id");
	        buff.append(" inner JOIN kv_record r ON b.BUSINESS_KEY = r.id");
	        buff.append(" WHERE tap.position_id in (").append(postID).append(",").append(userId).append(")");
	        buff.append(" and b.CATALOG <>'start' and b.CATALOG <>'copy' AND b.`STATUS`='complete' and r.businessTypeId <> '9999' GROUP BY b.BUSINESS_KEY");
	        buff.append(" union all");
	        buff.append(" select  DISTINCT b.BUSINESS_KEY,b.PROCESS_INSTANCE_ID,b.presentation_subject ,b.suspend_Status,min(b.TASK_ID)");
	        buff.append(" FROM task_info b JOIN task_info_approve_position tap ON b.ID = tap.task_id");
	        buff.append(" inner JOIN kv_record r ON b.BUSINESS_KEY = r.id");
	        buff.append(" WHERE tap.position_id in (").append(postID).append(",").append(userId).append(")");
	        buff.append(" and b.CATALOG <>'start' and b.CATALOG <>'copy' AND b.`STATUS`='complete' and r.businessTypeId = '9999' GROUP BY b.BUSINESS_KEY) s");
	        buff.append(" join kv_record k on s.BUSINESS_KEY = k.id " + " join account_info  a on k.USER_ID = a.ID) t where 1=1 ");
	        buff.append(" ) t");
	        buff.append(" left join (SELECT MAX(ti.COMPLETE_TIME) as COMPLETE_TIME,ti.BUSINESS_KEY FROM task_info ti");
	        buff.append(" WHERE ti.`STATUS` = 'complete' and ti.CATALOG = 'normal' group by ti.BUSINESS_KEY ) ti on t.business_key = ti.BUSINESS_KEY");
	        buff.append(" where 1=1");
			
			//受理单号
			if(strFRCode!=null&&!strFRCode.equals(""))
				buff.append(" and t.applycode like '%"+strFRCode+"%'");
			//申请人
			if(strRealName!=null&&!strRealName.equals(""))
				buff.append(" and t.full_name like '%"+strRealName+"%'");
			//状态
			if(strStatus!=null&&!strStatus.equals(""))
				//buff.append(" and t.chrStatus="+strStatus);
				buff.append(" and t.pro_status="+strStatus);
			//经销商编号
			if(strUserID!=null&&!strUserID.equals(""))
				buff.append(" and t.ucode="+strUserID);
			//业务类型
			if(strBusinessType!=null&&!strBusinessType.equals(""))
				buff.append(" and t.businesstypeid="+strBusinessType);
			//业务细分
			if(strBDID!=null&&!strBDID.equals(""))
				buff.append(" and t.businessdetailid="+strBDID);
			//体系
			if(strSystem!=null&&!strSystem.equals(""))
				buff.append(" and t.systemid="+strSystem);
			//大区
			/*if(strArea!=null&&!strArea.equals(""))
				buff.append(" and t.areaid="+strArea);*/
			if(strArea!=null&&!strArea.equals("")) {
				PartyDTO vo = partyConnector.findAreaById(percode);
				
				if (vo != null && vo.getId().equals(strArea)) {
					// 审批人和选择的大区为同一大区，则默认查全部    zyl 2017-10-26
				} else {
					buff.append(" and t.areaid="+strArea);
				}
			}
			
			//分公司
			if(strCompany!=null&&!strCompany.equals(""))
			{
				PartyDTO companyId = partyConnector.findCompanyById(percode);
				
				if (companyId != null && companyId.getId().equals(strArea)) {
					// 审批人和选择的大区为同一公司，则默认查全部    zyl 2017-10-26
				} else {
					buff.append(" and t.companyid="+strCompany);
				}
				
			}
			
			//String string = "2016-10-24 21:59:06";
			//SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			//System.out.println(sdf.parse(string));
			//申请日期开始
			if(strStart!=null&&!strStart.equals(""))
				buff.append(" and t.create_time>='"+strStart+"'");
			//申请日期结束
			if(strEnd!=null&&!strEnd.equals(""))
				buff.append(" and t.create_time<='"+strEnd+"'");
			
			buff.append(" order by COMPLETE_TIME DESC limit "+(Long.valueOf(strPageIndex)-1)*Long.valueOf(strPageSize)+","+strPageSize);
			String sql = buff.toString();
			
			List<Map<String, Object>> list=jdbcTemplate.queryForList(sql);
			
			List<Map<String, Object>> returnList=new ArrayList<Map<String,Object>>();
			
			returnMap.put("bSuccess", "true");
			if(list==null||list.size()<1)
				returnMap.put("strMsg", "暂无数据");
			else {
				for(Map<String, Object> map:list)
				{
					/*{"bSuccess":"true","strMsg":"加载成功"," "
							"ToDealRecord":[{"varFRCode":"受理单编号",
								"varRealName":"申请人"," chrStatus":"状态(标识) ",
								" varStatus":"状态(中文)"," varUserID ":"经销商编号",
								"varBusinessType":"业务类型"," varDetails":"业务细分",
								"varSystem":"所属体系","varArea":"所属大区",
								"varCompany":"所属公司"," dtmOperateTime ":"操作时间"},{…}]}*/
					
					/*String hlSql="select * from (select i.id,i.process_instance_id,i.action,r.applycode,i.presentation_subject,"
			    			+ "r.theme,r.user_id,p.full_name,r.ucode,r.businesstypeid,r.businesstypename,r.businessdetailid,"
			    			+ "r.businessdetailname,r.systemid,r.systemname,r.areaid,r.areaname,r.companyid,r.companyname,"
			    			+ "r.create_time,i.catalog,i.complete_time,i.assignee,r.status chrStatus"
			    			+ " from task_info i"
			    			+ " inner join kv_record r on i.business_key = r.business_key"
			    			+ " inner join person_info p on r.user_id = p.id"
			    			+ " where i.`status` = 'active') t where assignee  ='" + percode + "'";*/
					
					Map<String, Object> mapNew=new HashMap<String, Object>();
					mapNew.put("varFRCode",map.get("applycode")==null?"":map.get("applycode").toString());
					mapNew.put("varRealName",map.get("full_name")==null?"":map.get("full_name").toString());
					mapNew.put("chrStatus",map.get("pro_status")==null?"":map.get("pro_status").toString());// add by lilei at 2018-11-05
					mapNew.put("varStatus",GetProcessStatusDesc(map.get("pro_status")==null?"":map.get("pro_status").toString()));
					mapNew.put("varUserID",map.get("ucode")==null?"":map.get("ucode").toString());
					mapNew.put("varBusinessType",map.get("businesstypename")==null?"":map.get("businesstypename").toString());
					// mapNew.put("varDetails",map.get("businessdetailname")==null?"":map.get("businessdetailname").toString());
					mapNew.put("varSystem",map.get("systemname")==null?"":map.get("systemname").toString());
					mapNew.put("varArea",TrimNull(map.get("areaname")));
					mapNew.put("varCompany",map.get("companyname")==null?"":map.get("companyname").toString());
					//mapNew.put("dtmOperateTime",map.get("create_time"));
					mapNew.put("dtmOperateTime",map.get("complete_time"));
					if(StringUtils.isBlank(map.get("theme").toString())) {
						mapNew.put("varDetails",map.get("businessdetailname")==null?"":map.get("businessdetailname").toString());
					} else {
						mapNew.put("varDetails",map.get("theme")==null?"":map.get("theme").toString());
					}
					returnList.add(mapNew);
				}
				returnMap.put("strMsg", "加载成功");
				returnMap.put("DealedRecord", returnList);
			}
			} catch (Exception ex) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "加载错误，请联系管理员");
				logger.info("手机APP-审批-查询经我审批-查询异常："
						+ex.getMessage()+"\r\n"+ex.getStackTrace());
			}
			return returnMap;
	}

	@Log(desc = "查询抄送审批", action = "search", operationDesc = "手机APP-审批-查询抄送审批")
	public Map<String, Object> GetCopyToMeApproval(Map<String, Object> decryptedMap){
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			//orgConnector.h)	strPageSize
			//验证参数================================================================================
			if (!decryptedMap.containsKey("strPerCode") || StringUtils.isBlank(decryptedMap.get("strPerCode").toString())
				||!decryptedMap.containsKey("strPageSize") || StringUtils.isBlank(decryptedMap.get("strPageSize").toString())) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "参数错误");
				logger.info("手机APP-审批-抄送审批-手机没有传入strPerCode或者strPageSize参数");
				return returnMap;
			}
			
			//获取数据================================================
			String percode =decryptedMap.get("strPerCode").toString();
			String strFRCode =decryptedMap.get("strFRCode").toString();
			String strRealName =decryptedMap.get("strRealName").toString();
			String strStatus =decryptedMap.get("strStatus").toString();
			if(strStatus.equals("")){
				strStatus = "2";
			}else if(!strStatus.equals("2")&&!strStatus.equals("")){
				strStatus = "999";
			}
			//String strUserID =decryptedMap.get("strUserID").toString();
			String strBusinessType =decryptedMap.get("strBusinessType").toString();
			String strBDID =decryptedMap.get("strBDID").toString();
			//String strSystem =decryptedMap.get("strSystem").toString();
			String strArea =decryptedMap.get("strArea").toString();
			String strCompany =decryptedMap.get("strCompany").toString();
			String strStart =decryptedMap.get("strStart").toString();
			String strEnd =decryptedMap.get("strEnd").toString();
			String strPageSize =decryptedMap.get("strPageSize").toString();
			String strPageIndex=decryptedMap.get("strPageIndex").toString();
			
			StringBuffer sbBuffer=new StringBuffer();
			//sbBuffer.append("；strStatus="+strStatus);
			//sbBuffer.append("；strUserID="+strUserID);
			sbBuffer.append("；strBusinessType="+strBusinessType);
			sbBuffer.append("；strBDID="+strBDID);
			sbBuffer.append("；strStart="+strStart);
			sbBuffer.append("；strEnd="+strEnd);
			sbBuffer.append("；strPageSize="+strPageSize);
			sbBuffer.append("；strPageIndex="+strPageIndex);
			logger.info("手机APP-审批-查询抄送审批-传入参数：strPerCode="+percode+sbBuffer.toString());
			
			PartyEntity partyEntity=orgConnector.findUser(percode);
			if(partyEntity==null)
			{
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "未查询到您的信息");
				logger.info("手机APP-审批-查询抄送审批-为查询到组织个人（PartyEntity）");
				return returnMap;
			}
			
			StringBuffer postIDBuffer = new StringBuffer("");
	        List<PartyEntity> postlist = orgConnector.getPostByUserId(percode);
			if (postlist != null && postlist.size() > 0) {
				for (PartyEntity party : postlist) {
					postIDBuffer.append(party.getId() + ",");
				}
				postIDBuffer.delete(postIDBuffer.length() - 1, postIDBuffer.length());
			}
	        String 	postID = postIDBuffer.toString();
	        if (StringUtils.isBlank(postID)) {
	        	postID = "99999";
	        }
	        
			//Page page = humanTaskConnector.findPersonalTasks(userId, tenantId, propertyFilters, page);
			
			//List<Map<String, Object>> list = jdbcTemplate.queryForList(selectSql,params);
			String strBusinessId=GetFilterBusinessId();//i.assignee,
			String hlSql="select * from (select DISTINCT i.CREATE_TIME as cc_time, i.process_instance_id,r.applycode,i.presentation_subject,"
	    			+ "r.theme,r.user_id,p.full_name,r.ucode,r.businesstypeid,IFNULL(r.businesstypename,'') as businesstypename,r.businessdetailid,"
	    			+ "r.businessdetailname,r.systemid,r.systemname,r.areaid,IFNULL(r.areaname,'') as areaname,r.companyid,IFNULL(r.companyname,'') as companyname,"
	    			+ "r.create_time,i.catalog,r.url,i.suspend_Status as pro_falg,r.audit_status as pro_status"
	    			+ " from task_info i"
	    			+ " inner join kv_record r on i.business_key = r.business_key"
	    			+ " inner JOIN task_info_approve_position tap ON i.ID = tap.task_id" 
	    			+ " inner join person_info p on r.user_id = p.id"
	    			+ " where IFNULL(r.businesstypeid,'') not in("+strBusinessId+") and i.CATALOG='copy'" 
	    			+ " AND tap.position_id IN (" + postID + "," + percode + ")) t where 1=1";
			
			StringBuilder buff = new StringBuilder(hlSql);
			
			//受理单号
			if(strFRCode!=null&&!strFRCode.equals(""))
				buff.append(" and t.applycode like '%"+strFRCode+"%'");
			//申请人
			if(strRealName!=null&&!strRealName.equals(""))
				buff.append(" and t.full_name like '%"+strRealName+"%'");
			//状态
			if(strStatus!=null&&!strStatus.equals(""))
				buff.append(" and t.pro_status="+strStatus);
			//经销商编号
			//if(strUserID!=null&&!strUserID.equals(""))
				//buff.append(" and t.ucode="+strUserID);
			//业务类型
			if(strBusinessType!=null&&!strBusinessType.equals(""))
				buff.append(" and t.businesstypeid="+strBusinessType);
			//业务细分
			if(strBDID!=null&&!strBDID.equals(""))
				buff.append(" and t.businessdetailid="+strBDID);
			//体系
			//if(strSystem!=null&&!strSystem.equals(""))
				//buff.append(" and t.systemid="+strSystem);
			//大区
			if(strArea!=null&&!strArea.equals("")) {
				PartyDTO vo = partyConnector.findAreaById(percode);
				
				if (vo != null && vo.getId().equals(strArea)) {
					// 审批人和选择的大区为同一大区，则默认查全部    zyl 2017-10-26
				} else {
					buff.append(" and t.areaid="+strArea);
				}
			}
			//分公司
			//分公司
			if(strCompany!=null&&!strCompany.equals(""))
			{
				PartyDTO companyId = partyConnector.findCompanyById(percode);
				
				if (companyId != null && companyId.getId().equals(strArea)) {
					// 审批人和选择的大区为同一公司，则默认查全部    zyl 2017-10-26
				} else {
					buff.append(" and t.companyid="+strCompany);
				}
				
			}
			
			//String string = "2016-10-24 21:59:06";
			//SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			//System.out.println(sdf.parse(string));
			//申请日期开始
			if(strStart!=null&&!strStart.equals(""))
				buff.append(" and t.create_time>='"+strStart+"'");
			//申请日期结束
			if(strEnd!=null&&!strEnd.equals(""))
				buff.append(" and t.create_time<='"+strEnd+"'");
			
			buff.append(" order by cc_time DESC limit "+(Long.valueOf(strPageIndex)-1)*Long.valueOf(strPageSize)+","+strPageSize);//TODO 18.10.16 sjx 抄送审批接口排序改为按抄送时间倒序
			String sql = buff.toString();
			
			List<Map<String, Object>> list=jdbcTemplate.queryForList(sql);
			
			List<Map<String, Object>> returnList=new ArrayList<Map<String,Object>>();
			
			returnMap.put("bSuccess", "true");
			if(list==null||list.size()<1)
				returnMap.put("strMsg", "暂无数据");
			else {
				for(Map<String, Object> map:list)
				{
					Map<String, Object> mapNew=new HashMap<String, Object>();
					mapNew.put("varFRCode",map.get("applycode")==null?"":map.get("applycode").toString());
					mapNew.put("varRealName",map.get("full_name")==null?"":map.get("full_name").toString());
					mapNew.put("varStatus",GetProcessStatusDesc(map.get("chrStatus")==null?"":map.get("chrStatus").toString()));
					mapNew.put("varUserID",map.get("ucode")==null?"":map.get("ucode").toString());
					mapNew.put("varBusinessType",map.get("businesstypename")==null?"":map.get("businesstypename").toString());
					// mapNew.put("varDetails",map.get("businessdetailname")==null?"":map.get("businessdetailname").toString());
					mapNew.put("varSystem",map.get("systemname")==null?"":map.get("systemname").toString());
					mapNew.put("varArea",TrimNull(map.get("areaname")));
					mapNew.put("varCompany",map.get("companyname")==null?"":map.get("companyname").toString());
					mapNew.put("dtmOperateTime",map.get("cc_time"));//因手机端抄送排序更改，故显示的时间也相应调整 sjx 18.10.19
					if(StringUtils.isBlank(map.get("theme").toString())) {
						mapNew.put("varDetails",map.get("businessdetailname")==null?"":map.get("businessdetailname").toString());
					} else {
						mapNew.put("varDetails",map.get("theme")==null?"":map.get("theme").toString());
					}
					returnList.add(mapNew);
				}
				returnMap.put("strMsg", "加载成功");
				returnMap.put("CCRecord", returnList);
			}
			} catch (Exception ex) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "加载错误，请联系管理员");
				logger.info("手机APP-审批-查询抄送审批-查询异常："
						+ex.getMessage()+"\r\n"+ex.getStackTrace());
			}
			return returnMap;
	}
	
	@Log(desc = "查询全部审批", action = "search", operationDesc = "手机APP-审批-查询全部审批")
	public Map<String, Object> GetAllApproval(Map<String, Object> decryptedMap){
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			//orgConnector.h)	strPageSize
			//验证参数================================================================================
			if (!decryptedMap.containsKey("strPerCode") || StringUtils.isBlank(decryptedMap.get("strPerCode").toString())
				||!decryptedMap.containsKey("strPageSize") || StringUtils.isBlank(decryptedMap.get("strPageSize").toString())) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "参数错误");
				logger.info("手机APP-审批-查询全部审批-手机没有传入strPerCode或者strPageSize参数");
				return returnMap;
			}
			
			//获取数据================================================
			String percode =decryptedMap.get("strPerCode").toString();
			String strFRCode =decryptedMap.get("strFRCode").toString();
			String strRealName =decryptedMap.get("strRealName").toString();
			String strStatus =decryptedMap.get("strStatus").toString();
			String strUserID =decryptedMap.get("strUserID").toString();
			String strBusinessType =decryptedMap.get("strBusinessType").toString();
			String strBDID =decryptedMap.get("strBDID").toString();
			String strSystem =decryptedMap.get("strSystem").toString();
			String strArea =decryptedMap.get("strArea").toString();
			String strCompany =decryptedMap.get("strCompany").toString();
			String strStart =decryptedMap.get("strStart").toString();
			String strEnd =decryptedMap.get("strEnd").toString();
			String strPageSize =decryptedMap.get("strPageSize").toString();
			String strPageIndex=decryptedMap.get("strPageIndex").toString();
			
			StringBuffer sbBuffer=new StringBuffer();
			sbBuffer.append("；strStatus="+strStatus);
			sbBuffer.append("；strUserID="+strUserID);
			sbBuffer.append("；strBusinessType="+strBusinessType);
			sbBuffer.append("；strBDID="+strBDID);
			sbBuffer.append("；strStart="+strStart);
			sbBuffer.append("；strEnd="+strEnd);
			sbBuffer.append("；strPageSize="+strPageSize);
			sbBuffer.append("；strPageIndex="+strPageIndex);
			logger.info("手机APP-审批-查询全部审批-传入参数：strPerCode="+percode+sbBuffer.toString());
			
			PartyEntity partyEntity=orgConnector.findUser(percode);
			if(partyEntity==null)
			{
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "未查询到您的信息");
				logger.info("手机APP-审批-查询全部审批-为查询到组织个人（PartyEntity）");
				return returnMap;
			}
				
			//Page page = humanTaskConnector.findPersonalTasks(userId, tenantId, propertyFilters, page);
			
			//List<Map<String, Object>> list = jdbcTemplate.queryForList(selectSql,params);
			String strBusinessId=GetFilterBusinessId();
			String hlSql="select * from (select DISTINCT i.process_instance_id,r.applycode,i.presentation_subject,"
	    			+ "r.theme,r.user_id,p.full_name,r.ucode,r.businesstypeid,IFNULL(r.businesstypename,'') as businesstypename,r.businessdetailid,"
	    			+ "r.businessdetailname,r.systemid,r.systemname,r.areaid,IFNULL(r.areaname,'') as areaname,r.companyid,IFNULL(r.companyname,'') as companyname,"
	    			+ "r.create_time,i.catalog,i.assignee,r.url,i.suspend_Status as pro_falg,r.audit_status as chrStatus"
	    			/*+ "case  when i.`STATUS`='active' then '1'" 
					+" when i.`STATUS`='complete' then '2' else '' end as chrStatus"*/
	    			+ " from task_info i"
	    			+ " inner join kv_record r on i.business_key = r.business_key"
	    			+ " inner join person_info p on r.user_id = p.id"
	    			+ " where IFNULL(r.businesstypeid,'') not in("+strBusinessId+") and i.CATALOG='start') t where  1=1";
			
			StringBuilder buff = new StringBuilder(hlSql);
			
			//受理单号
			if(strFRCode!=null&&!strFRCode.equals(""))
				buff.append(" and t.applycode like '%"+strFRCode+"%'");
			//申请人
			if(strRealName!=null&&!strRealName.equals(""))
				buff.append(" and t.full_name like '%"+strRealName+"%'");
			//状态
			if(strStatus!=null&&!strStatus.equals(""))
				buff.append(" and t.chrStatus="+strStatus);
			//经销商编号
			if(strUserID!=null&&!strUserID.equals(""))
				buff.append(" and t.ucode="+strUserID);
			//业务类型
			if(strBusinessType!=null&&!strBusinessType.equals(""))
				buff.append(" and t.businesstypeid="+strBusinessType);
			//业务细分
			if(strBDID!=null&&!strBDID.equals(""))
				buff.append(" and t.businessdetailid="+strBDID);
			//体系
			if(strSystem!=null&&!strSystem.equals(""))
				buff.append(" and t.systemid="+strSystem);
			//大区
			if(strArea!=null&&!strArea.equals("")) {
				PartyDTO vo = partyConnector.findAreaById(percode);
				
				if (vo != null && vo.getId().equals(strArea)) {
					// 审批人和选择的大区为同一大区，则默认查全部    zyl 2017-10-26
				} else {
					buff.append(" and t.areaid="+strArea);
				}
			}
			//分公司
			//分公司
			if(strCompany!=null&&!strCompany.equals(""))
			{
				PartyDTO companyId = partyConnector.findCompanyById(percode);
				
				if (companyId != null && companyId.getId().equals(strArea)) {
					// 审批人和选择的大区为同一公司，则默认查全部    zyl 2017-10-26
				} else {
					buff.append(" and t.companyid="+strCompany);
				}
				
			}
			
			//String string = "2016-10-24 21:59:06";
			//SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			//System.out.println(sdf.parse(string));
			//申请日期开始
			if(strStart!=null&&!strStart.equals(""))
				buff.append(" and t.create_time>='"+strStart+"'");
			//申请日期结束
			if(strEnd!=null&&!strEnd.equals(""))
				buff.append(" and t.create_time<='"+strEnd+"'");
			
			buff.append(" order by t.create_time DESC limit "+(Long.valueOf(strPageIndex)-1)*Long.valueOf(strPageSize)+","+strPageSize);
			String sql = buff.toString();
			
			List<Map<String, Object>> list=jdbcTemplate.queryForList(sql);
			
			List<Map<String, Object>> returnList=new ArrayList<Map<String,Object>>();
			
			returnMap.put("bSuccess", "true");
			if(list==null||list.size()<1)
				returnMap.put("strMsg", "暂无数据");
			else {
				for(Map<String, Object> map:list)
				{
					Map<String, Object> mapNew=new HashMap<String, Object>();
					mapNew.put("varFRCode",map.get("applycode")==null?"":map.get("applycode").toString());
					mapNew.put("varRealName",map.get("full_name")==null?"":map.get("full_name").toString());
					mapNew.put("chrStatus",map.get("chrStatus")==null?"":map.get("chrStatus").toString());// add by lilei at 2018-11-05
					mapNew.put("varStatus",GetProcessStatusDesc(map.get("chrStatus")==null?"":map.get("chrStatus").toString()));
					mapNew.put("varUserID",map.get("ucode")==null?"":map.get("ucode").toString());
					mapNew.put("varBusinessType",map.get("businesstypename")==null?"":map.get("businesstypename").toString());
					mapNew.put("varDetails",map.get("businessdetailname")==null?"":map.get("businessdetailname").toString());
					mapNew.put("varSystem",map.get("systemname")==null?"":map.get("systemname").toString());
					mapNew.put("varArea",TrimNull(map.get("areaname")));
					mapNew.put("varCompany",map.get("companyname")==null?"":map.get("companyname").toString());
					mapNew.put("dtmOperateTime",map.get("create_time"));
					
					returnList.add(mapNew);
				}
				returnMap.put("strMsg", "加载成功");
				returnMap.put("AllRecord", returnList);
			}
			} catch (Exception ex) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "加载错误，请联系管理员");
				logger.info("手机APP-审批-查询全部审批-查询异常："
						+ex.getMessage()+"\r\n"+ex.getStackTrace());
			}
			return returnMap;
	}
	
	@Log(desc = "查询定制审批", action = "search", operationDesc = "手机APP-审批-查询定制审批")
	public Map<String, Object> GetAllSpecialApproval(Map<String, Object> decryptedMap){
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			//orgConnector.h)	strPageSize
			//验证参数================================================================================
			if (!decryptedMap.containsKey("strPerCode") || StringUtils.isBlank(decryptedMap.get("strPerCode").toString())
				||!decryptedMap.containsKey("strPageSize") || StringUtils.isBlank(decryptedMap.get("strPageSize").toString())) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "参数错误");
				logger.info("手机APP-审批-查询全部审批-手机没有传入strPerCode或者strPageSize参数");
				return returnMap;
			}
			
			//获取数据================================================
			String percode =decryptedMap.get("strPerCode").toString();
			String strFRCode =decryptedMap.get("strFRCode").toString();
			String strRealName =decryptedMap.get("strRealName").toString();
			String strStatus =decryptedMap.get("strStatus").toString();
			String strUserID =decryptedMap.get("strUserID").toString();
			String strBusinessType =decryptedMap.get("strBusinessType").toString();
			String strBDID =decryptedMap.get("strBDID").toString();
			String strSystem =decryptedMap.get("strSystem").toString();
			String strArea =decryptedMap.get("strArea").toString();
			String strCompany =decryptedMap.get("strCompany").toString();
			String strStart =decryptedMap.get("strStart").toString();
			String strEnd =decryptedMap.get("strEnd").toString();
			String strPageSize =decryptedMap.get("strPageSize").toString();
			String strPageIndex=decryptedMap.get("strPageIndex").toString();
			
			StringBuffer sbBuffer=new StringBuffer();
			sbBuffer.append("；strStatus="+strStatus);
			sbBuffer.append("；strUserID="+strUserID);
			sbBuffer.append("；strBusinessType="+strBusinessType);
			sbBuffer.append("；strBDID="+strBDID);
			sbBuffer.append("；strStart="+strStart);
			sbBuffer.append("；strEnd="+strEnd);
			sbBuffer.append("；strPageSize="+strPageSize);
			sbBuffer.append("；strPageIndex="+strPageIndex);
			logger.info("手机APP-审批-查询定制审批-传入参数：strPerCode="+percode+sbBuffer.toString());
			
			PartyEntity partyEntity=orgConnector.findUser(percode);
			if(partyEntity==null)
			{
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "未查询到您的信息");
				logger.info("手机APP-审批-查询定制审批-为查询到组织个人（PartyEntity）");
				return returnMap;
			}
				
			//Page page = humanTaskConnector.findPersonalTasks(userId, tenantId, propertyFilters, page);
			
			//List<Map<String, Object>> list = jdbcTemplate.queryForList(selectSql,params);
			
			String specialPeopleString="";
			List<DictInfo> dictList=dictConnector.findDictInfoListByType("speicalPeopleCode");
			if(dictList!=null&&dictList.size()>0)
			{
				specialPeopleString=dictList.get(0).getValue();
			}
			
			String strBusinessId=GetFilterBusinessId();
			String hlSql="select * from (select DISTINCT i.process_instance_id,r.applycode,i.presentation_subject,"
	    			+ "r.theme,r.user_id,p.full_name,r.ucode,IFNULL(r.businesstypeid,'') as businessTypeId,IFNULL(r.businesstypename,'') as businesstypename,r.businessdetailid,"
	    			+ "r.businessdetailname,r.systemid,r.systemname,r.areaid,r.areaname,r.companyid,IFNULL(r.companyname,'') as companyname,"
	    			+ "r.create_time,i.catalog,i.assignee,r.url,i.suspend_Status as pro_falg,r.audit_status as chrStatus"
	    			/*+ "case  when i.`STATUS`='active' then '1'" 
					+" when i.`STATUS`='complete' then '2' else '' end as chrStatus"*/
	    			+ " from task_info i"
	    			+ " inner join kv_record r on i.business_key = r.business_key"
	    			+ " inner join person_info p on r.user_id = p.id"
	    			+ " where IFNULL(r.businesstypeid,'') not in("+strBusinessId+") and i.CATALOG='start') t where  t.assignee  in(" + specialPeopleString + ")";
			
			StringBuilder buff = new StringBuilder(hlSql);
			
			//受理单号
			if(strFRCode!=null&&!strFRCode.equals(""))
				buff.append(" and t.applycode like '%"+strFRCode+"%'");
			//申请人
			if(strRealName!=null&&!strRealName.equals(""))
				buff.append(" and t.full_name like '%"+strRealName+"%'");
			//状态
			if(strStatus!=null&&!strStatus.equals(""))
				buff.append(" and t.chrStatus="+strStatus);
			//经销商编号
			if(strUserID!=null&&!strUserID.equals(""))
				buff.append(" and t.ucode="+strUserID);
			//业务类型
			if(strBusinessType!=null&&!strBusinessType.equals(""))
				buff.append(" and t.businesstypeid="+strBusinessType);
			//业务细分
			if(strBDID!=null&&!strBDID.equals(""))
				buff.append(" and t.businessdetailid="+strBDID);
			//体系
			if(strSystem!=null&&!strSystem.equals(""))
				buff.append(" and t.systemid="+strSystem);
			//大区
			if(strArea!=null&&!strArea.equals("")) {
				PartyDTO vo = partyConnector.findAreaById(percode);
				
				if (vo != null && vo.getId().equals(strArea)) {
					// 审批人和选择的大区为同一大区，则默认查全部    zyl 2017-10-26
				} else {
					buff.append(" and t.areaid="+strArea);
				}
			}
			//分公司
			//分公司
			if(strCompany!=null&&!strCompany.equals(""))
			{
				PartyDTO companyId = partyConnector.findCompanyById(percode);
				
				if (companyId != null && companyId.getId().equals(strArea)) {
					// 审批人和选择的大区为同一公司，则默认查全部    zyl 2017-10-26
				} else {
					buff.append(" and t.companyid="+strCompany);
				}
				
			}
			
			//String string = "2016-10-24 21:59:06";
			//SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			//System.out.println(sdf.parse(string));
			//申请日期开始
			if(strStart!=null&&!strStart.equals(""))
				buff.append(" and t.create_time>='"+strStart+"'");
			//申请日期结束
			if(strEnd!=null&&!strEnd.equals(""))
				buff.append(" and t.create_time<='"+strEnd+"'");
			
			buff.append(" order by t.create_time DESC limit "+(Long.valueOf(strPageIndex)-1)*Long.valueOf(strPageSize)+","+strPageSize);
			String sql = buff.toString();
			
			List<Map<String, Object>> list=jdbcTemplate.queryForList(sql);
			
			List<Map<String, Object>> returnList=new ArrayList<Map<String,Object>>();
			
			returnMap.put("bSuccess", "true");
			if(list==null||list.size()<1)
				returnMap.put("strMsg", "暂无数据");
			else {
				for(Map<String, Object> map:list)
				{
					Map<String, Object> mapNew=new HashMap<String, Object>();
					mapNew.put("varFRCode",map.get("applycode")==null?"":map.get("applycode").toString());
					mapNew.put("varRealName",map.get("full_name")==null?"":map.get("full_name").toString());
					mapNew.put("chrStatus",map.get("chrStatus")==null?"":map.get("chrStatus").toString());// add by lilei at 2018-11-05
					mapNew.put("varStatus",GetProcessStatusDesc(map.get("chrStatus")==null?"":map.get("chrStatus").toString()));
					mapNew.put("varUserID",map.get("ucode")==null?"":map.get("ucode").toString());
					mapNew.put("varBusinessType",map.get("businesstypename")==null?"":map.get("businesstypename").toString());
					mapNew.put("varDetails",map.get("businessdetailname")==null?"":map.get("businessdetailname").toString());
					mapNew.put("varSystem",map.get("systemname")==null?"":map.get("systemname").toString());
					mapNew.put("varArea",TrimNull(map.get("areaname")==null));
					mapNew.put("varCompany",map.get("companyname")==null?"":map.get("companyname").toString());
					mapNew.put("dtmOperateTime",map.get("create_time"));
					
					returnList.add(mapNew);
				}
				returnMap.put("strMsg", "加载成功");
				returnMap.put("CustomizedRecord", returnList);
			}
			} catch (Exception ex) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "加载错误，请联系管理员");
				logger.info("手机APP-审批-查询定制审批-查询异常："
						+ex.getMessage()+"\r\n"+ex.getStackTrace());
			}
			return returnMap;
	}
	
	private String GetFilterBusinessId()
	{
		String strBusinessId="";
		//filterFlowToDeal
		List<DictInfo> dictList=dictConnector.findDictInfoListByType("filterFlowToDeal");
		if(dictList!=null&&dictList.size()>0)
		{
			DictInfo dictInfo=dictList.get(0);
			strBusinessId=dictInfo.getValue().replace("，", ",");
		}
		return strBusinessId;
	}
	
	private String TrimNull(Object value) {
		if (value == null || value.equals(null))
			return "";
		else if(value.toString().equals("null")){
			return "";
		}
		else {
			return value.toString();
		}
	}
	
	private List<DictInfo> dictList=null;
	private String GetProcessStatusDesc(String strStatus)
	{
		String strDesc="";
		if(strStatus==null||strStatus.equals(""))
			return strDesc;
		if(dictList==null||dictList.size()<1)
			dictList=dictConnector.findDictInfoListByType("RecordStatus");
		
		if(dictList != null && dictList.size()>0){
			for(DictInfo dic:dictList){
				if(dic.getValue().equals(strStatus)){
					strDesc=dic.getName();
				}
			}
		}
		return strDesc;
	}
	
	/**
	 * 
	 * 查询我的申请的明细-审批详情
	 * lilei at 2017.09.29
	 * 
	 * **/
	@Log(desc = "查询我的申请的明细", action = "search", operationDesc = "手机APP-审批-查询我的申请的明细")
	public Map<String, Object> GetFlowDetail(Map<String, Object> decryptedMap){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
		//验证参数================================================================================
		if (!decryptedMap.containsKey("strFRCode") || StringUtils.isBlank(decryptedMap.get("strFRCode").toString())) {
			returnMap.put("bSuccess", "false");
			returnMap.put("strMsg", "参数错误");
			logger.info("手机APP-审批-查询我的申请的明细-手机没有传入strFRCode参数");
			return returnMap;
		}
		
		//获取数据================================================
		String frCode =decryptedMap.get("strFRCode").toString();
		String strPerCode =decryptedMap.get("percode").toString();
		
		logger.info("手机APP-我的传入参数：strFRCode="+frCode+"；strPerCode="+strPerCode);
		
		
		/*{"bSuccess":"true","strMsg":"加载成功",
			" RecordInfo":[{"varFRName":"申请名称/主题","varDetail":"申请表单信息"
				,"varImages":"图片信息用“,”号分割"}], 
				"DetailData":[{"varCompany":"公司名称",
					"varDepart":"部门名称","varPosition":"职位名称",
					"varRealName":"操作人姓名"
						,"chrOption":"当前步骤签署状态 未处理 1审核通过 2审核未通过 3驳回"
							,"varOption":"备注","dtmOperateTime":"操作时间"},{…}]}*/
		
		
		List<Map<String,Object>> mapList=new ArrayList<Map<String,Object>>();
		
		List<Map<String,Object>> mapModelMapList=jdbcTemplate.queryForList(
				"select r.*,i.PROCESS_INSTANCE_ID as proessId from kv_record r inner join task_info i on r.BUSINESS_KEY=i.BUSINESS_KEY"
				+" where i.CATALOG='start' and r.applycode='"+frCode+"' order by r.CREATE_TIME ASC LIMIT 0,1" );
		Map<String,Object> mapModelMap=mapModelMapList.get(0);
		Map<String,Object> map=new HashMap<String, Object>();
		map.put("varFRName",mapModelMap.get("name")==null?"":mapModelMap.get("name").toString());
		
		//add 2018/9/10 ckx   获取下下步审核人
		String downAuditor = customWorkService.getDownAuditor(strPerCode, mapModelMap.get("BUSINESS_KEY").toString());
		map.put("downAuditor", downAuditor);
		//查询表单的抄送人  ckx add 2018/9/7
        String copyName = customWorkService.getTaskCopyNames(StringUtil.toString(mapModelMap.get("proessId")));
        
		if(mapModelMap.get("businessTypeName")!=null
			&&mapModelMap.get("businessTypeName").toString().contains("自定义")){
			Map<String,Object> approverMap=customConnector.findAuditorHtml(
										mapModelMap.get("BUSINESS_KEY").toString(),
										strPerCode
										);
	        //model.addAttribute("approver",approverMap.get("approver"));
	        map.put("auditorId",approverMap.get("auditorId"));
	        map.put("auditorName",approverMap.get("auditorName"));
	        map.put("isCanSelect",customConnector.checkCanSelectAuditor(
	        						mapModelMap.get("BUSINESS_KEY").toString(),
	        						strPerCode)?"1":"0");
			logger.info("手机app-审批详情页面后台返回数据={{{{}}}}auditorId："+approverMap.get("auditorId")+"auditorName:"+approverMap.get("auditorName"));
	        //ckx add 2018/9/7  替换抄送人
	        Object objHtml = mapModelMap.get("detailHtml");
	        String detailHtml = "";
	        if(null != objHtml){
	        	detailHtml = objHtml.toString().replace("{auditor}",approverMap.get("approver").toString());
	        	detailHtml = detailHtml.replace("{copyNames}", copyName);
	        }
	        map.put("varDetail", detailHtml);
			/*map.put("varDetail", mapModelMap.get("detailHtml")==null
								?"":mapModelMap.get("detailHtml").toString()
								.replace("{auditor}",approverMap.get("approver").toString()));*/
		}
		else {
			/*map.put("varDetail", mapModelMap.get("detailHtml")==null
					?"":mapModelMap.get("detailHtml").toString());*/
			//ckx add 2018/9/10  替换抄送人
			Object objHtml = mapModelMap.get("detailHtml");
	        String detailHtml = "";
	        if(null != objHtml){
	        	detailHtml = objHtml.toString().replace("{copyNames}", copyName);
	        	detailHtml = detailHtml.replace("{exchangeTable}", "换货单");
	        	detailHtml = detailHtml.replace("{qualityExchangeTable}", "质量换货单");
	        }
	        map.put("varDetail",detailHtml);
		}
		
		//region 经销商过来流程-处理驳回按钮
		boolean isProcessFromOA=false;		//是否来至经销商的流程
		String isShow="1";					//是否显示驳回按钮， 1：是，0：否
		if(mapModelMap.get("businessDetailId")!=null
				&&!mapModelMap.get("businessDetailId").toString().equals("")
				&&",1,2,3,4,5,6,7,8,9,10,11,12,13,14,".contains(","+mapModelMap.get("businessDetailId").toString()+",")){
			isProcessFromOA=true;
			String status=mapModelMap.get("audit_status").toString();
	    	//如果是审核中（驳回）
			if(status!=null&&status.equals("0"))
				isShow="0";
			else if(status!=null&&status.equals("4"))
	    	{
	    		//取得第一审核人
	    		String strSql="SELECT IFNULL(ASSIGNEE,'') AS ASSIGNEE FROM task_info"
	    				+ " where catalog<>'copy' AND status='complete' AND catalog<>'start'"
	    				+ " and BUSINESS_KEY='"+mapModelMap.get("BUSINESS_KEY").toString()+"'"
	    				+ " ORDER BY COMPLETE_TIME ASC limit 0,1";
	    		List<Map<String, Object>> taskMapList=jdbcTemplate.queryForList(strSql);
	    		String strFirstAuditor=taskMapList.get(0).get("ASSIGNEE").toString();
	    		
	    		//取得当前审核人
	    		strSql="SELECT IFNULL(ASSIGNEE,'') AS ASSIGNEE FROM task_info"
	    				+ " where catalog<>'copy' AND status='active' AND catalog<>'start'"
	    				+ " and BUSINESS_KEY='"+mapModelMap.get("BUSINESS_KEY").toString()+"'"
	    				+ " ORDER BY COMPLETE_TIME ASC limit 0,1";
	    		
	    		List<Map<String, Object>> taskActiveMapList=jdbcTemplate.queryForList(strSql);
	    		String strCurrentAuditor=taskActiveMapList.get(0).get("ASSIGNEE").toString();
	    		if(strFirstAuditor.equals(strCurrentAuditor)){
	    			//判断是否有经销商审核过
	        		strSql="SELECT IFNULL(ASSIGNEE,'') AS ASSIGNEE FROM task_info"
	        				+ " where catalog<>'copy' AND status='complete' AND catalog='normal'"
	        				+ " and ASSIGNEE='"+PartyConstants.JXS_ID+"'"
	        				+ " and BUSINESS_KEY='"+mapModelMap.get("BUSINESS_KEY").toString()+"'"
	        				+ " ORDER BY COMPLETE_TIME ASC limit 0,1";
	        		List<Map<String, Object>> taskJXSMapList=jdbcTemplate.queryForList(strSql);
	        		if(taskJXSMapList==null||taskJXSMapList.size()<1)
	        			isShow="0";
	    		}
	    	}
		}
		
		//20180307 chengze
		String userId  = strPerCode;//decryptedMap.get("strPerCode").toString();//当前登陆人
		
		String isAuthorization = "0";
		
		 //验证前登录人岗位是否在线专员岗，是 返回1，否 返回0
		//TODO 暂时屏蔽 2018-03-14
		isAuthorization=processOperationControllerOa.isAuthorization(userId,frCode,mapModelMap.get("BUSINESS_KEY").toString());
 
		map.put("isShowReject", isShow);
		map.put("isAuthorization", isAuthorization);
		//endregion
		map.put("businessTypeId",mapModelMap.get("businessTypeName")==null?"":mapModelMap.get("businessTypeName"));
		map.put("auditStatus",mapModelMap.get("audit_status"));
		
		//region 接口返回特殊审批表单信息 add by lilei at 2019.02.15
        Map<String,String> mapFormInfo=operationConnector.getSpecialFormInfo(strPerCode,frCode,mapModelMap.get("pk_id").toString());
        map.put("formInfo", mapFormInfo);
		//endregion
		
		/*String strIsReject="0";//记录是否驳回，1：是，0：否
		//返回是否是驳回的
		List<Map<String,Object>> mapRejectList=jdbcTemplate.queryForList(
				"select r.*,i.PROCESS_INSTANCE_ID as proessId from kv_record r inner join task_info i on r.BUSINESS_KEY=i.BUSINESS_KEY"
				+" where i.CATALOG='active' and r.applycode='"+frCode+"' order by r.CREATE_TIME ASC LIMIT 0,1" );
		
		if(mapRejectList!=null&&mapRejectList.size()>0){
			Map<String,Object> mapReject=mapRejectList.get(0);
			if(mapReject!=null
				&&mapReject.get("ASSIGNEE").toString().equals(mapModelMap.get("ASSIGNEE").toString())
				&&(mapReject.get("ACTION").toString().equals("自定义申请等待调整") ||mapReject.get("ACTION").toString().equals("重新调整申请"))){
					strIsReject="1";
				}
		}
		
		map.put("isReject",strIsReject);*/
		
		StringBuffer sbFilePathBuffer=new StringBuffer("");
		Object pk_Id=mapModelMap.get("pk_Id");
		if(pk_Id!=null){
			List<StoreInfo> storeInfos=storeConnector.getStore(pk_Id.toString());
			if(storeInfos!=null&&storeInfos.size()>0){
				for(StoreInfo storeInfo:storeInfos){
					sbFilePathBuffer.append(storeInfo.getPath()+",");
				}
				if(!sbFilePathBuffer.toString().equals(""))
					sbFilePathBuffer.delete( sbFilePathBuffer.length()-1, sbFilePathBuffer.length());
			}
		}
		map.put("varImages", sbFilePathBuffer.toString());
		
		List<Map<String,Object>> mapTaskList=jdbcTemplate.queryForList(
				"select i.ID from kv_record r inner join task_info i on r.BUSINESS_KEY=i.BUSINESS_KEY"
				+" where i.CATALOG='copy' AND i.ASSIGNEE="+strPerCode+" and r.applycode='"+frCode+"' order by r.CREATE_TIME ASC LIMIT 0,1" );
		
		if(mapTaskList!=null&&mapTaskList.size()>0)
		{
			Map<String,Object> mapTask=mapTaskList.get(0);
			//抄送已阅读
			HumanTaskDTO humanTask = humanTaskConnector
	                .findHumanTask(mapTask.get("ID").toString());
	        if (humanTask == null) {
	        	returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "审批流程不存在");
				logger.info("手机APP-审批-查询我的申请的明细-手审批流程不存在");
				return returnMap;
	        }
	        // 处理转发抄送任务，设置为已读
	        if (HumanTaskConstants.CATALOG_COPY.equals(humanTask.getCatalog())&&!humanTask.getStatus().equals("complete")) {
	        	humanTask.setStatus("complete");
	        	humanTask.setCompleteTime(new Date());
	        	humanTask.setAction("已阅");
	            humanTaskConnector.saveHumanTask(humanTask);
	        }
		}
		
		//获取流程对应的所有人工任务（目前还没有区分历史）
		List<Map<String,Object>> mapChildList=new ArrayList<Map<String,Object>>();
        List<HumanTaskDTO> humanTasks = humanTaskConnector
                .findHumanTasksForPositionByProcessInstanceId(mapModelMap.get("proessId").toString());
        for (HumanTaskDTO humanTaskDto : humanTasks) {
            if (humanTaskDto.getParentId() != null
        		||(humanTaskDto.getCompleteTime()==null)||humanTaskDto.getAction()==null) {
                continue;
            }
            Map<String,Object> mapChild=new HashMap<String, Object>();
            /*"DetailData":[{"varCompany":"公司名称","varDepart":"部门名称",
            	"varPosition":"职位名称","varRealName":"操作人姓名",
            	"chrOption":"当前步骤签署状态 未处理 1审核通过 2审核未通过 3驳回",
            	"varOption":"备注","dtmOperateTime":"操作时间"*/
            
            /*if(isProcessFromOA&&humanTaskDto.getAssignee().equals(PartyConstants.JXS_ID))
            {
            	//来至直销流程的处理
            	List<Map<String,Object>> mapFirstAuditorList
            						=jdbcTemplate.queryForList("select * from ro_pf_oaonline where varApplyCode='"+frCode+"'");
            	mapChild.put("varCompany","");
        		mapChild.put("varDepart","");
        		mapChild.put("varPosition", "");
        		AccountInfo accountInfo=accountInfoManager.findUniqueBy("id", Long.valueOf(humanTaskDto.getAssignee()));
        		mapChild.put("varRealName", accountInfo.getDisplayName()+"("+mapFirstAuditorList.get(0).get("ucode")+")");
            }
            else {
            	//普通流程处理
            	String strCompanyName = "";
            	String allName = humanTaskDto.getName();
            	String []name = allName.split("-");
            	strCompanyName = name[0];
            	String strDepartName = "";
        		//修改手机接口审批步骤 edit by lilei at 2018.09.07 
        		String strPosition=GetPositionsName(humanTaskDto.getAssignee());
        		if(mapModelMap.get("businessTypeId").toString()!=null){
        			if(!mapModelMap.get("businessTypeId").toString().equals("9999")){
        				String strSql="SELECT * FROM task_info_approve_position WHERE task_id=%s";
        				List<Map<String,Object>> mapPositionList=jdbcTemplate.queryForList(String.format(strSql,humanTaskDto.getId()));
        	            if(mapPositionList!=null&&mapPositionList.size()>0){
        	            	Map<String,Object> mapPosition=mapPositionList.get(0);
        	            	if(mapPosition.get("position_id")==null){
        	            		strPosition="";
        	            	}
        	            	else{
        	            		if(mapPosition.get("position_id").toString().equals("")){
        	            			strPosition="";
        	            		}
        	            		else {
        	            			PartyDTO companyDTO = partyConnector.findCompanyById(mapPosition.get("position_id").toString());
                	            	if (companyDTO != null) {
                	            		strCompanyName = companyDTO.getName();
                	            	}
                	            	PartyDTO departDTO =  partyConnector.findDepartmentById(mapPosition.get("position_id").toString());
                	            	if (departDTO != null) {
                	            		strDepartName = departDTO.getName();
                	            	}
        	            			PartyEntity partyEntity=partyEntityManager.findUniqueBy("id",Long.parseLong(mapPosition.get("position_id").toString()));
                	            	if(partyEntity!=null){
                	            		if(partyEntity.getPartyType().getId().equals(PartyConstants.PARTY_TYPE_POST)){
                	            			strPosition=partyEntity.getName();
            	                    		String strPositionSql="SELECT * FROM party_entity_attr WHERE ID=%s";
            	                        	List<Map<String,Object>> mapPostAttrList=jdbcTemplate.queryForList(String.format(strPositionSql, partyEntity.getId()));
            	                            if(mapPostAttrList!=null&&mapPostAttrList.size()>0){
            	                            	strPosition+="("+mapPostAttrList.get(0).get("positionNo").toString()+")";
            	                            }
                    	            		
                    	            	}
                	            	}
                	            	else {
            	            			strPosition="";
    								}
								}
        	            	}
        	            }
        			}
        		}
        		mapChild.put("varCompany",strCompanyName);
        		mapChild.put("varDepart",strDepartName);
        		mapChild.put("varPosition", strPosition);
        		AccountInfo accountInfo=accountInfoManager.findUniqueBy("id", Long.valueOf(humanTaskDto.getAssignee()));
        		mapChild.put("varRealName", accountInfo.getDisplayName());
			}*/
            
            TaskInfoApprovePosition taskInfoApprovePosition=taskInfoApprovePositionManager.findUniqueBy("taskId", Long.valueOf(humanTaskDto.getId()));
            if(taskInfoApprovePosition!=null){
            	if(taskInfoApprovePosition.getPositionType().equals("1"))
            	{
            		mapChild.put("varCompany","");
            		mapChild.put("varDepart","");
            		mapChild.put("varPosition", humanTaskDto.getName());
            		AccountInfo accountInfo=accountInfoManager.findUniqueBy("id", Long.valueOf(humanTaskDto.getAssignee()));
            		mapChild.put("varRealName",accountInfo.getDisplayName());
            	}
            	else{
            		mapChild.put("varCompany","");
            		mapChild.put("varDepart","");
            		mapChild.put("varPosition", "");
            		mapChild.put("varRealName",humanTaskDto.getName());
            	}	
            }
            else {
            	mapChild.put("varCompany","");
        		mapChild.put("varDepart","");
        		mapChild.put("varPosition", "");
        		mapChild.put("varRealName",humanTaskDto.getName());
			}
            
    		String actionStatus="未审核";
    		if(!humanTaskDto.getStatus().equals("active"))
    			actionStatus=humanTaskDto.getAction();
    		
    		mapChild.put("chrOption", actionStatus);
    		mapChild.put("varOption",humanTaskDto.getComment()==null?"":humanTaskDto.getComment());
    		mapChild.put("dtmOperateTime",humanTaskDto.getCompleteTime());
    		String strCreateTime = sdf.format(humanTaskDto.getCreateTime());
    		//TODO:添加审核时长 sjx 18.01.18
    		if(humanTaskDto.getCompleteTime() != null){
    			String strCompleteTime = sdf.format(humanTaskDto.getCompleteTime());
        		long seconds = DateUtil.getTimeDifference(strCompleteTime, strCreateTime);
        		String auditDuration = DateUtil.secondsToTime(seconds);
        		mapChild.put("auditDuration",auditDuration);
        		//System.out.println("审核时长："+auditDuration);
    		}
    		mapChildList.add(mapChild);
        }
		mapList.add(map);
		
		returnMap.put("bSuccess", "true");
		returnMap.put("strMsg", "加载成功");
		returnMap.put("RecordInfo", mapList);
		returnMap.put("DetailData", mapChildList);
		} catch (Exception ex) {
			returnMap.put("bSuccess", "false");
			returnMap.put("strMsg", "加载错误，请联系管理员");
			logger.info("手机APP--审批-查询我的申请的明细-查询异常："
					+ex.getMessage()+"\r\n"+ex.getStackTrace());
		}
		return returnMap;
	}
	
	private String GetPositionsName(String userId)
	{
		StringBuffer  strReurn=new StringBuffer("");
		List<PartyEntity> list=orgConnector.getPostByUserId(userId);
		if(list!=null&&list.size()>0){
			for(PartyEntity party:list){
				strReurn.append(party.getName()+",");
			}
			strReurn.delete(strReurn.length()-1,strReurn.length());
		}
    	return strReurn.toString();
	}
	
	

	/**
	 * 
	 * 1.9.	审批--审核通过--普通流程 同意并授权（ToH5AuditPass）
	 * chengze at 2017.10.18
	 * 
	 * **/
	@SuppressWarnings("unchecked")
	@Log(desc = "审批--审核通过--普通流程同意并授权", action = "approvalAuthorizationAgree", operationDesc = "手机APP-审批-审核通过--普通流程普通流程同意并授权")
	@Transactional(readOnly = false)
	public Map<String, Object> ApprovalAuthorizationAgree(Map<String, Object> decryptedMap){
		Map<String, Object> returnMap=new HashMap<String, Object>();
		try {
			//验证参数================================================================================
			if (!decryptedMap.containsKey("strPerCode") || StringUtils.isBlank(decryptedMap.get("strPerCode").toString())
				||!decryptedMap.containsKey("strFRCode") || StringUtils.isBlank(decryptedMap.get("strFRCode").toString())) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "参数错误");
				logger.info("手机APP-审批--审核通过--普通流程同意并授权-手机没有传入strPerCode或者strFRCode");
				return returnMap;
			}
			String isAuthorization = "0";
			String applyCode = decryptedMap.get("strFRCode").toString();
			String userId  = decryptedMap.get("strPerCode").toString();//当前登陆人
			
			String sql="select * from kv_record where applycode='" +applyCode+"'";
			List<Map<String, Object>> list=jdbcTemplate.queryForList(sql);
			Map<String, Object> map = list.get(0);
			String businessKey =(String)map.get("business_Key"); 
			 
			//验证前登录人岗位是否在线专员岗，是 返回1，否 返回0
			isAuthorization=processOperationControllerOa.isAuthorization(userId,applyCode,businessKey);
			if (isAuthorization.equals("0")){
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "没有权限‘同意并授权’");
				logger.info("手机APP-审批--审核通过--普通流程同意并授权-手机没有传入strPerCode或者strFRCode");
				return returnMap;
			}
			
			//验证前登录人岗位是否在线专员岗，是 返回1，否 返回0
			//String isAuthorization=processOperationControllerOa.isAuthorization(userId,frCode,businessKey);
			//if (isAuthorization.equals("1"))
				//jdbcTemplate.update("update ro_pf_oaonline set chrIsAuthCertificate='0' where varApplyCode='"+frCode+"'");
			
			decryptedMap.put("isAppendComment", "1");
			//调用审核通过的方法，完成审批
			returnMap = ApprovalAgree(decryptedMap);
			
			OnLineInfo onLineInfo=onLineInfoManager.findUnique("from OnLineInfo where applycode=?", applyCode);
        	if(onLineInfo!=null)
        	{
        		onLineInfo.setIsAuthCertificate("1");
        		onLineInfoManager.save(onLineInfo);
        	}
			
			//jdbcTemplate.update("update ro_pf_oaonline set chrIsAuthCertificate='1' where varApplyCode='"+applyCode+"'");
			/*if(returnMap.get("bSuccess")=="true"){
		        //再创建一条标识 这个流程时同意并授权的
		        TimeTaskInfo timeTaskEndService=new TimeTaskInfo();
				timeTaskEndService.setTaskType("createsab");
				timeTaskEndService.setTaskContent(applyCode);
				timeTaskEndService.setTaskAddDate(new Date());
				timeTaskEndService.setTaskNote("0");//0 表示该条流程在专员岗这里同意并授权了，但是该条流程还没有完成全部审核
				timeTaskManager.save(timeTaskEndService);
			}*/
		}
		catch(Exception ex){
			if(!returnMap.containsKey("bSuccess")){
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "审核失败，请联系管理员");
			}
			logger.info("手机APP-审批--审核通过--普通流程同意并授权异常："+ex.getMessage()+"\r\n"+ex.getStackTrace());
		}
		return returnMap;
	}
	/**
	 * 
	 * 1.9.	审批--审核通过--普通流程（ToH5AuditPass）
	 * chengze at 2017.10.18
	 * 
	 * **/
	@SuppressWarnings("unchecked")
	@Log(desc = "审批--审核通过--普通流程", action = "approvalAgree", operationDesc = "手机APP-审批-审核通过--普通流程")
	@Transactional(readOnly = false)
	public Map<String, Object> ApprovalAgree(Map<String, Object> decryptedMap){
		
		String businessKey = "";
		String strRemark  = "";//审批意见
		String processInstanceId = "";
		Map<String, Object> processParameters = new HashMap<String, Object>();
		FormParameter formParameter = new FormParameter();
       
        Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			//验证参数================================================================================
			if (!decryptedMap.containsKey("strPerCode") || StringUtils.isBlank(decryptedMap.get("strPerCode").toString())
				||!decryptedMap.containsKey("strFRCode") || StringUtils.isBlank(decryptedMap.get("strFRCode").toString())) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "参数错误");
				logger.info("手机APP-审批--审核通过--普通流程-手机没有传入strPerCode");
				return returnMap;
			}
			
			//验证操作密码================================================
			String pwd = decryptedMap.get("strPrivateKey").toString();
			String userId  = decryptedMap.get("strPerCode").toString();//当前登陆人
			
			AccountInfo accountInfo = accountInfoManager.get(Long.parseLong(userId));
			String hql = "from AccountCredential where accountInfo=? and catalog='default'";
			AccountCredential accountCredential = accountCredentialManager.findUnique(hql, accountInfo);
			ChangePasswordResult changePasswordResult = new ChangePasswordResult();
			if (!isPasswordValid(pwd, accountCredential.getOperationPassword())) {
			   	returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "操作密码错误");
				logger.info("手机APP-审批--审核通过--普通流程-操作密码错误");
				return returnMap;
	        }

			//若有审批意见　　存起来
			if (!(!decryptedMap.containsKey("strRemark") || StringUtils.isBlank(decryptedMap.get("strRemark").toString()))) {
				strRemark =  decryptedMap.get("strRemark").toString();
				if(strRemark.length()>200){
					returnMap.put("bSuccess", "false");
					returnMap.put("strMsg", "批示不能超过200个字");
					return returnMap;
				}
			}
			
			String frCode =decryptedMap.get("strFRCode").toString();//受理单编号		
			System.out.println("====================待办审核 手机APP-我的传入参数：受理单编号="+frCode);
			
			String sql="select * from kv_record where applycode='" +frCode+"'";
			List<Map<String, Object>> list=jdbcTemplate.queryForList(sql);

			if( list == null || list.size() == 0) {
				returnMap.put("strMsg", "暂无数据");
				return returnMap;
			} else {
				if (list.size() > 1) {
					returnMap.put("bSuccess", "false");
					returnMap.put("strMsg", "受理单号(" + frCode + ")重复，请联系管理员");
					return returnMap;
				} else {
					Map<String, Object> map = list.get(0);
					businessKey =(String) map.get("business_Key"); 
				}
			}
			
			sql="from TaskInfo where status ='active' and catalog<>'copy' and catalog<>'start' and businessKey=? and assignee=?";
			List<TaskInfo> taskInfo = taskInfoManager.find(sql,businessKey,userId);
			
			System.out.println("==================== 待办审核 from TaskInfo where businessKey=" + businessKey + " and assignee=" + userId);
			
			String tempHumanTaskId = "";
			if (taskInfo == null || taskInfo.size() == 0) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "你无权限审核或者已审核过该流程");
				return returnMap;
			} else {
				if (taskInfo.size() > 1) {
					returnMap.put("bSuccess", "false");
					returnMap.put("strMsg", "此任务异常，请联系管理员");
					return returnMap;
				} else {
					TaskInfo vo = taskInfo.get(0);
					tempHumanTaskId = Long.toString(vo.getId());
					processInstanceId = vo.getProcessInstanceId();
				}
			}
			
			System.out.println("==================== 待办审核 humanTaskId " + tempHumanTaskId);
			
			//验证前登录人岗位是否在线专员岗，是 返回1，否 返回0
			String isAuthorization=processOperationControllerOa.isAuthorization(userId,frCode,businessKey);
			if (isAuthorization.equals("1"))
			{
				OnLineInfo onLineInfo=onLineInfoManager.findUnique("from OnLineInfo where applycode=?", frCode);
	        	if(onLineInfo!=null)
	        	{
	        		onLineInfo.setIsAuthCertificate("0");
	        		onLineInfoManager.save(onLineInfo);
	        	}
	        	//jdbcTemplate.update("update ro_pf_oaonline set chrIsAuthCertificate='0' where varApplyCode='"+frCode+"'");
			}
				
			
			//网签功能去除，最后一步生成授权书的功能 add by lilei at 2018-07-24 16:08
			Map<String, Object> rMap=new HashMap<String, Object>();
			rMap = processOperationOnlineController.isAuthForPhone(processInstanceId,userId);
			
			if(rMap.get("isauth").toString().equals("1")){
				SrmbDTO s =  wSApiResource.createorRenewSAB(frCode);
				if(s.getStatus() =="error" ){
					returnMap.put("bSuccess", "false");
					returnMap.put("strMsg", s.getMsg());
					return returnMap;
				}
				else if(s.getStatus() =="exist" ){
					returnMap.put("bSuccess", "false");
					returnMap.put("strMsg", s.getMsg()+"\r\n"+"请点击'不同意'结束您的流程！");
					return returnMap;
				}
			}
			 
			formParameter.setAction("同意");
			formParameter.setBusinessKey(businessKey);
			
			if(decryptedMap.containsKey("isAppendComment")&&decryptedMap.get("isAppendComment").toString().equals("1"))
				strRemark+=" <br/><font style='color:red'>同意旗舰店申请，并允许发放旗舰店授权书</font>";
			formParameter.setComment(strRemark);
			 
			processParameters.put("leaderComment", "同意");
			//审批过程中的流分支条件 add by lilei at 2018.11.12
	        processParameters=operationConnector.getBranchConditionMap(processParameters, businessKey);
			 
			Record record = keyValueConnector.findByCode(formParameter.getBusinessKey());
			 
			// 驳回操作，清空抄送人
        	String sqlRecordUpdate = "update KV_PROP set value= '' where code = 'activityId' and record_id= '" + record.getCode() + "'";
        	keyValueConnector.updateBySql(sqlRecordUpdate);
	        	
			this.operationService.completeTask(tempHumanTaskId, userId,
		                    formParameter, processParameters, record,
		                    processInstanceId);
			//审批后将此任务对应的消息置为已读 2018.09.07 sjx
            String updateMsg = "update MsgInfo set status=1 where data=?";
            msgInfoManager.batchUpdate(updateMsg, tempHumanTaskId);
            
            operationConnector.setSpecialFormInfo(list.get(0).get("pk_id").toString(), businessKey, decryptedMap);
			
			returnMap.put("businessKey", formParameter.getBusinessKey());
			returnMap.put("humanTaskId", tempHumanTaskId);
			returnMap.put("bSuccess", "true");
			returnMap.put("strMsg", "审核成功");
		}catch (Exception ex) {
			returnMap.put("bSuccess", "false");
			returnMap.put("strMsg", "审核失败，请联系管理员");
			System.out.println("==================== 待办审核 手机APP--审批--审核通过--普通流程-异常："
					+ex.getMessage()+"\r\n"+ex.getStackTrace());
		}
		return returnMap;
	}

	//验证密码
	public boolean isPasswordValid(String rawPassword, String encodedPassword) {
	    if (customPasswordEncoder != null) {
	        return customPasswordEncoder.matches(rawPassword, encodedPassword);
	    } else {
	        return rawPassword.equals(encodedPassword);
	    }
	}
	
	/**
	 * 
	 * 	审批--审核--自定义流程（ToH5ApprovalCustom）
	 * chengze at 2017.10.18
	 * 
	 * **/
	@Log(desc = "审批--审核--自定义流程", action = "CustomApprovalAgree", operationDesc = "手机APP-审批-审核--自定义流程")
	@Transactional(readOnly = false)
	public Map<String, Object> CustomApprovalAgree(Map<String, Object> decryptedMap){
		
		String strRemark  = "";//审批意见
		String businessKey  = "";
		Long processInstanceId = 0L;
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			
			//orgConnector.
			//验证参数================================================================================
			if (!decryptedMap.containsKey("strPerCode") || StringUtils.isBlank(decryptedMap.get("strPerCode").toString())
				||!decryptedMap.containsKey("strFRCode") || StringUtils.isBlank(decryptedMap.get("strFRCode").toString())) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "参数错误");
				logger.info("手机APP-审批--审核通过--普通流程-手机没有传入strPerCode");
				return returnMap;
			}
			
			//验证操作密码================================================
			 String pwd = decryptedMap.get("strPrivateKey").toString();
	
			String userId  = decryptedMap.get("strPerCode").toString();//当前登陆人
			
			AccountInfo accountInfo = accountInfoManager.get(Long.parseLong(userId));
			   String hql = "from AccountCredential where accountInfo=? and catalog='default'";
			   AccountCredential accountCredential = accountCredentialManager.findUnique(hql, accountInfo);
			   ChangePasswordResult changePasswordResult = new ChangePasswordResult();
			   if (!isPasswordValid(pwd, accountCredential.getOperationPassword())) {
		            
				   	returnMap.put("bSuccess", "false");
					returnMap.put("strMsg", "操作密码错误");
					logger.info("手机APP-审批--审核通过--普通流程-操作密码错误");
					return returnMap;
		        }
			   
	
			//获取数据================================================
			
			//若有审批意见 存起来
			if (!(!decryptedMap.containsKey("strRemark") || StringUtils.isBlank(decryptedMap.get("strRemark").toString()))) {
				strRemark =  decryptedMap.get("strRemark").toString();
				if(strRemark.length()>200){
					returnMap.put("bSuccess", "false");
					returnMap.put("strMsg", "批示不能超过200个字");
					return returnMap;
				}
			}
			
			String frCode =decryptedMap.get("strFRCode").toString();//受理单编号	
			logger.info("手机APP-我的传入参数：strFRCode="+frCode);
			
			//取申请单的 流程实例ID
			String sql="select  c.*,r.BUSINESS_KEY from   oa_bpm_customform c INNER JOIN" 
						+" kv_record r on c.id=r.pk_id where c.applycode='" +frCode+"'";
			List<Map<String, Object>> list=jdbcTemplate.queryForList(sql);
			if(list==null||list.size()<1)
				returnMap.put("strMsg", "暂无数据");
			else {
				List<Map<String, Object>> returnList=new ArrayList<Map<String,Object>>();
				for(Map<String, Object> map:list)
				{
					processInstanceId =(Long) (map.get("id"))  ; 
				}
			}
			
			//当前登陆人
			 userId  = decryptedMap.get("strPerCode").toString();
			//审核标识
			String flag = decryptedMap.get("flag").toString();
			//类型
			String type = decryptedMap.get("type").toString();
			// ckx add 2018/8/8
			String hsql="from TaskInfo where processInstanceId=? and status='active' and catalog='normal'";
	    	List<TaskInfo> taskInfoList = taskInfoManager.find(hsql,String.valueOf(processInstanceId));
	    	if(taskInfoList==null||taskInfoList.size()<1){
	    		logger.info("查询不到审批步骤");
	            returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "找不到该流程或者流程已结束");
				return returnMap;
	    	}
	    	else{
	    		if(!userId.equals(taskInfoList.get(0).getAssignee())){
	    			logger.info("processInstanceId:"+processInstanceId+"，重复操作");
	    			returnMap.put("bSuccess", "false");
					returnMap.put("strMsg", "你无权限审核或者已审核过该流程");
					return returnMap;
	    		}
	    	}
	    	
	    	//先获取humanTaskId
	         hql = "from TaskInfo where processInstanceId=? and catalog<>'copy' and catalog<>'start'  and status = 'active' ";
	        List<TaskInfo> taskInfos = taskInfoManager.find(hql, Long.toString(processInstanceId));
	        for(TaskInfo t : taskInfos){
	        	humanTaskId = Long.toString(t.getId()) ;
	        	businessKey = t.getBusinessKey();
	        }
			
			if(flag.equals("1")){
				Map<String, Object> customMap=list.get(0);
				
				String nextUserId=TrimNull(decryptedMap.get("leader"));
				if(!nextUserId.equals("")){
					if(nextUserId.equals(customMap.get("userid").toString())){
						returnMap.put("bSuccess", "false");
						returnMap.put("strMsg", "下一步审核人不能是发起人");
						return returnMap;
					}
					
					if(customConnector.checkIsExistsAuditor(
							customMap.get("BUSINESS_KEY").toString(), 
							userId, 
							nextUserId)){
						returnMap.put("bSuccess", "false");
						returnMap.put("strMsg", "下一步审核人不能是流程中已有的审核人");
						return returnMap;
					}
				}
				else{
					if(type.equals("1")||type.equals("3")){
				        //获取下一步审批人
				        //ckx 查询当前用户
				        List<CustomApprover> approverListNow=customApproverManager.find(
								"from CustomApprover where businessKey=? and approverId=? and opterType !='2' and opterType !='3'", 
								businessKey,Long.parseLong(userId));
				        if(null != approverListNow && approverListNow.size() > 0){
				        	CustomApprover customApproverNow = approverListNow.get(0);
				        	int currentStep = customApproverNow.getApproveStep();
				        	//获取所有下一步审批步骤
							List<CustomApprover> nextApproverList=customApproverManager.find(
									"from CustomApprover where businessKey=? and approveStep>? and opterType !='2' and opterType !='3' order by approveStep", 
									businessKey,
									customApproverNow.getApproveStep());
							if(null != nextApproverList && nextApproverList.size() > 0){
								returnMap.put("bSuccess", "false");
								returnMap.put("strMsg", "下一步审核人不能为空");
								return returnMap;
							}
				        }				
					}
				}
			}
			
	    	
	    	//创建formParameter
	        MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<String, String>();
	        multiValueMap.add("ucode",decryptedMap.get("percode").toString());
	        multiValueMap.add("comment",strRemark);
	        multiValueMap.add("leaderName",decryptedMap.get("leaderName").toString());
	        multiValueMap.add("leader",decryptedMap.get("leader").toString());
	       // multiValueMap.add("type",decryptedMap.get("type").toString());
	        //FormParameter formParameter = this.operationService.saveDraft(userId, "1", humanTaskId, businessKey, "", multiValueMap);
		    //优化   ckx   
	        FormParameter formParameter = new FormParameter(multiValueMap);

			formParameter.setHumanTaskId(humanTaskId);
			formParameter.setBusinessKey(businessKey);
			formParameter.setBpmProcessId("");
			
			logger.info("手机APP-我的传入参数：类型{{{"+type+"}}}"+JSONObject.toJSONString(multiValueMap));
			
	       //完成审批
			this.customService.CompleteTaskCustomH5(
										frCode,
										formParameter, 
										Long.toString(processInstanceId),
										flag,
										userId,
										userConnector.findById(userId).getDisplayName(),type); 
			//审批后将此任务对应的消息置为已读 2018.09.07 sjx
            String updateMsg = "update MsgInfo set status=1 where data=?";
            msgInfoManager.batchUpdate(updateMsg, humanTaskId);
			returnMap.put("bSuccess", "true");
			returnMap.put("strMsg", "审核成功");
		}catch (Exception ex) {
			returnMap.put("bSuccess", "false");
			returnMap.put("strMsg", "审核失败，请联系管理员");
			logger.info("手机APP--审批--审核--自定义-异常："
					+ex.getMessage()+"\r\n"+ex.getStackTrace());
		}
		return returnMap;
	}
	
	/**
	 * 
	 * 1.10.审批--审核不通过（ToH5AuditPass）
	 * chengze at 2017.10.19
	 * 
	 * **/
	@Log(desc = "审批--审核不通过--普通流程", action = "ApprovalDisagree", operationDesc = "手机APP-审批-审核不通过--普通流程")
	@Transactional(readOnly = false)
	public Map<String, Object> ApprovalDisagree(Map<String, Object> decryptedMap){
		
		String businessKey = "";
		String strRemark  = "";//审批意见
		String processInstanceId = "";
		Map<String, Object> processParameters = new HashMap<String, Object>();
		FormParameter formParameter = new FormParameter();
        
        Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			//orgConnector.
			//验证参数================================================================================
			if (!decryptedMap.containsKey("strPerCode") || StringUtils.isBlank(decryptedMap.get("strPerCode").toString())
				||!decryptedMap.containsKey("strFRCode") || StringUtils.isBlank(decryptedMap.get("strFRCode").toString())
				||!decryptedMap.containsKey("strRemark") || StringUtils.isBlank(decryptedMap.get("strRemark").toString())) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "参数错误");
				logger.info("手机APP-审批--审核通过--普通流程-手机没有传入strPerCode");
				return returnMap;
			}
			
			//验证操作密码================================================
			String pwd = decryptedMap.get("strPrivateKey").toString();
			String userId  = decryptedMap.get("strPerCode").toString();//当前登陆人
			
		   AccountInfo accountInfo = accountInfoManager.get(Long.parseLong(userId));
		   String hql = "from AccountCredential where accountInfo=? and catalog='default'";
		   AccountCredential accountCredential = accountCredentialManager.findUnique(hql, accountInfo);
		   ChangePasswordResult changePasswordResult = new ChangePasswordResult();
		   if (!isPasswordValid(pwd, accountCredential.getOperationPassword())) {
	            
			   	returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "操作密码错误");
				logger.info("手机APP-审批--审核通过--普通流程-操作密码错误");
				return returnMap;
	        }
	
			//获取数据================================================
			String frCode =decryptedMap.get("strFRCode").toString();//受理单编号
			strRemark  =decryptedMap.get("strRemark").toString(); //审批意见
			if(strRemark.length()>200){
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "批示不能超过200个字");
				return returnMap;
			}
			
			logger.info("手机APP-我的传入参数：strFRCode="+frCode);
			String sql="select  * from   kv_record where applycode='" +frCode+"'";
			
			List<Map<String, Object>> list=jdbcTemplate.queryForList(sql);
			Map<String, Object> mapNew=new HashMap<String, Object>();
			if(list==null||list.size()<1){
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "没有查询到此流程数据");
				return returnMap;
			}
			else if(list.size()>1){
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "查询到流程数据重复，无法审批");
				logger.info("手机审批，流程单号重复，具体单号："+frCode);
				return returnMap;
			}
			
			Map<String, Object> kvMap=list.get(0);
			
			mapNew.put("businessKey",kvMap.get("businessKey")==null?"":kvMap.get("businessKey").toString());
			businessKey =(String)kvMap.get("business_Key"); 
			
			/*List<Map<String, Object>> returnList=new ArrayList<Map<String,Object>>();
			for(Map<String, Object> map:list)
			{
				
			}*/
			
			String strApplyType=kvMap.get("businessDetailId").toString();
        	if(strApplyType.equals("8")||strApplyType.equals("9"))
        	{
        		OnLineInfo onLineInfo=onLineInfoManager.findUnique("from OnLineInfo where applycode=?", frCode);
	        	if(onLineInfo!=null)
	        	{
	        		onLineInfo.setIsAuthCertificate("0");
	        		onLineInfoManager.save(onLineInfo);
	        	}
        	}
			
			sql="from TaskInfo where status ='active' and catalog<>'copy' and catalog<>'start' and businessKey=? and assignee=?";
			List<TaskInfo> taskInfo = taskInfoManager.find(sql,businessKey,userId);
			if(taskInfo.size() == 0){
				logger.info("processInstanceId:"+processInstanceId+"，重复操作");
    			returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "你无权限审核或者已审核过该流程");
				return returnMap;
			}
			for (TaskInfo t1 : taskInfo) {
				humanTaskId =Long.toString(t1.getId())  ;
				processInstanceId = t1.getProcessInstanceId();
	        }
			
			 formParameter.setAction("不同意");
			 formParameter.setBusinessKey(businessKey);
			 formParameter.setComment(strRemark);
			 
			 processParameters.put("leaderComment", "不同意");
			 //审批过程中的流分支条件 add by lilei at 2018.11.12
			 processParameters=operationConnector.getBranchConditionMap(processParameters, businessKey);
	        
			 Record record = keyValueConnector.findByCode(formParameter.getBusinessKey());
			 
			 this.operationService.completeTask(humanTaskId, userId,
		                    formParameter, processParameters, record,
		                    processInstanceId);
			//审批后将此任务对应的消息置为已读 2018.09.07 sjx
            String updateMsg = "update MsgInfo set status=1 where data=?";
            msgInfoManager.batchUpdate(updateMsg, humanTaskId);
			 //wSApplyService.SetOATimeTask(formParameter.getBusinessKey(), humanTaskId);
		     
			returnMap.put("businessKey", formParameter.getBusinessKey());
			returnMap.put("humanTaskId", humanTaskId);
			returnMap.put("bSuccess", "true");
			returnMap.put("strMsg", "审核成功");
		}catch (Exception ex) {
			returnMap.put("bSuccess", "false");
			returnMap.put("strMsg", "审核失败，请联系管理员");
			logger.info("手机APP--审批--审核通过--普通流程-异常："
					+ex.getMessage()+"\r\n"+ex.getStackTrace());
		}
		return returnMap;
	}
	
	/**
	 * 
	 * 1.11.审批--驳回（ToH5AuditPass）
	 * chengze at 2017.10.20
	 * 
	 * **/
	@Log(desc = "审批--驳回--普通流程", action = "ApprovalToPrevStep", operationDesc = "手机APP-审批-驳回")
	@Transactional(readOnly = false)
	public Map<String, Object> ApprovalToPrevStep(Map<String, Object> decryptedMap){
		
		String businessKey = "";
		String strRemark  = "";//审批意见
		String processInstanceId = "";
		Map<String, Object> processParameters = new HashMap<String, Object>();
		FormParameter formParameter = new FormParameter();
        
        Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			//orgConnector.
			//验证参数================================================================================
			if (!decryptedMap.containsKey("strPerCode") || StringUtils.isBlank(decryptedMap.get("strPerCode").toString())
				||!decryptedMap.containsKey("strFRCode") || StringUtils.isBlank(decryptedMap.get("strFRCode").toString())
				||!decryptedMap.containsKey("strRemark") || StringUtils.isBlank(decryptedMap.get("strRemark").toString())) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "参数错误");
				logger.info("手机APP-审批--审核通过--普通流程-手机没有传入strPerCode或strFRCode或strRemark参数");
				return returnMap;
			}
			//验证操作密码================================================
			 String pwd = decryptedMap.get("strPrivateKey").toString();
	
			String userId  = decryptedMap.get("strPerCode").toString();//当前登陆人
			
			AccountInfo accountInfo = accountInfoManager.get(Long.parseLong(userId));
			   String hql = "from AccountCredential where accountInfo=? and catalog='default'";
			   AccountCredential accountCredential = accountCredentialManager.findUnique(hql, accountInfo);
			   ChangePasswordResult changePasswordResult = new ChangePasswordResult();
			   if (!isPasswordValid(pwd, accountCredential.getOperationPassword())) {
		            
				   	returnMap.put("bSuccess", "false");
					returnMap.put("strMsg", "操作密码错误");
					logger.info("手机APP-审批--审核通过--普通流程-操作密码错误");
					return returnMap;
		        }
	
			//获取数据================================================
			
			String frCode =decryptedMap.get("strFRCode").toString();//受理单编号
			strRemark  =decryptedMap.get("strRemark").toString(); //审批意见
			if(strRemark.length()>200){
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "批示不能超过200个字");
				return returnMap;
			}
			
			logger.info("手机APP-我的传入参数：strFRCode="+frCode);
			
			String sql="select  * from   kv_record where applycode='" +frCode+"'";
			
			
			List<Map<String, Object>> list=jdbcTemplate.queryForList(sql);
			Map<String, Object> mapNew=new HashMap<String, Object>();
			//returnMap.put("bSuccess", "true");
			if(list==null||list.size()<1)
				returnMap.put("strMsg", "暂无数据");
			else {
				List<Map<String, Object>> returnList=new ArrayList<Map<String,Object>>();
				for(Map<String, Object> map:list)
				{
					mapNew.put("businessKey",map.get("businessKey")==null?"":map.get("businessKey").toString());
					businessKey =(String) map.get("business_Key"); 
				}
			}
			
			sql="from TaskInfo where status ='active' and catalog<>'copy' and catalog<>'start' and businessKey=? and assignee=?";
			List<TaskInfo> taskInfo = taskInfoManager.find(sql,businessKey,userId);
			if(taskInfo.size() == 0){
				logger.info("processInstanceId:"+processInstanceId+"，重复操作");
    			returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "你无权限审核或者已审核过该流程");
				return returnMap;
			}
			for (TaskInfo t1 : taskInfo) {
		        
				humanTaskId =Long.toString(t1.getId())  ;
				processInstanceId = t1.getProcessInstanceId();
	        
	        }
			 formParameter.setAction("驳回");
			 formParameter.setBusinessKey(businessKey);
			 formParameter.setComment(strRemark);
			 
			 processParameters.put("leaderComment", "驳回");
			//审批过程中的流分支条件 add by lilei at 2018.11.12
			 processParameters=operationConnector.getBranchConditionMap(processParameters, businessKey);
	        
			 Record record = keyValueConnector.findByCode(formParameter.getBusinessKey());
			 
			 this.operationService.completeTask(humanTaskId, userId,
		                    formParameter, processParameters, record,
		                    processInstanceId);
			//审批后将此任务对应的消息置为已读 2018.09.07 sjx
            String updateMsg = "update MsgInfo set status=1 where data=?";
            msgInfoManager.batchUpdate(updateMsg, humanTaskId);
			//wSApplyService.SetOATimeTask(formParameter.getBusinessKey(), humanTaskId);
            
            operationConnector.setSpecialFormInfo(list.get(0).get("pk_id").toString(), businessKey, decryptedMap);
		    
		    returnMap.put("businessKey", formParameter.getBusinessKey());
			returnMap.put("humanTaskId", humanTaskId);
			returnMap.put("bSuccess", "true");
			returnMap.put("strMsg", "审核成功");
		}catch (Exception ex) {
			returnMap.put("bSuccess", "false");
			returnMap.put("strMsg", "审核失败，请联系管理员");
			logger.info("手机APP--审批--审核通过--普通流程-异常："
					+ex.getMessage()+"\r\n"+ex.getStackTrace());
		}
		return returnMap;
	}
	
	@Log(desc = "审批--撤销申请--普通流程", action = "update", operationDesc = "手机APP-审批-撤销申请")
	@Transactional(readOnly = false)
	public Map<String, Object> ApprovalStepCancel(Map<String, Object> decryptedMap){
		String businessKey = "";
		String strRemark  = "";//审批意见
		String processInstanceId = "";
		Map<String, Object> processParameters = new HashMap<String, Object>();
		FormParameter formParameter = new FormParameter();
        
        Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			//orgConnector.
			//验证参数================================================================================
			if (!decryptedMap.containsKey("strPerCode") || StringUtils.isBlank(decryptedMap.get("strPerCode").toString())
				||!decryptedMap.containsKey("strFRCode") || StringUtils.isBlank(decryptedMap.get("strFRCode").toString())) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "参数错误");
				logger.info("手机APP-审批--审核通过--普通流程-手机没有传入strPerCode或strFRCode参数");
				return returnMap;
			}
			
			//验证操作密码================================================
			 String pwd = decryptedMap.get("strPrivateKey").toString();
	
			String userId  = decryptedMap.get("strPerCode").toString();//当前登陆人
			
			AccountInfo accountInfo = accountInfoManager.get(Long.parseLong(userId));
			   String hql = "from AccountCredential where accountInfo=? and catalog='default'";
			   AccountCredential accountCredential = accountCredentialManager.findUnique(hql, accountInfo);
			   ChangePasswordResult changePasswordResult = new ChangePasswordResult();
			   if (!isPasswordValid(pwd, accountCredential.getOperationPassword())) {
		            
				   	returnMap.put("bSuccess", "false");
					returnMap.put("strMsg", "操作密码错误");
					logger.info("手机APP-审批--审核通过--普通流程-操作密码错误");
					return returnMap;
		        }
	
			//获取数据================================================
			
			String frCode =decryptedMap.get("strFRCode").toString();//受理单编号
			//strRemark  =decryptedMap.get("strRemark").toString(); //审批意见
			
			logger.info("手机APP-我的传入参数：strFRCode="+frCode+"strPerCode="+userId);
			
			String sql="select i.*,r.audit_status from  kv_record r "
					+ " inner join task_info i on i.BUSINESS_KEY=r.BUSINESS_KEY"
					+ " where assignee="+userId+" and  applycode='"+frCode+"' order by CREATE_TIME DESC LIMIT 0,1";
			
			List<Map<String, Object>> list=jdbcTemplate.queryForList(sql);
			Map<String, Object> mapNew=new HashMap<String, Object>();
			//returnMap.put("bSuccess", "true");
			String status = "";
			if(list==null||list.size()<1){
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "未查询到流程数据");
				return returnMap;
			}
			else {
				Map<String, Object> taskMap=list.get(0);
				status = taskMap.get("audit_status").toString();
				if(taskMap.get("status").toString().equals("complete")||(!taskMap.get("audit_status").toString().equals("7")&&!taskMap.get("audit_status").toString().equals("8"+ "")))
				{
					returnMap.put("bSuccess", "false");
					returnMap.put("strMsg", "流程已被审批，请返回查看");
					return returnMap;
				}
				businessKey =taskMap.get("business_Key").toString();
				
				/*List<Map<String, Object>> returnList=new ArrayList<Map<String,Object>>();
				for(Map<String, Object> map:list)
				{
					mapNew.put("businessKey",map.get("businessKey")==null?"":map.get("businessKey").toString());
					businessKey =(String) map.get("business_Key"); 
				}*/
				
				humanTaskId=taskMap.get("ID").toString();
				processInstanceId=taskMap.get("PROCESS_INSTANCE_ID").toString();
			}
			
			/*sql="from TaskInfo where businessKey=? and assignee=?";
			List<TaskInfo> taskInfo = taskInfoManager.find(sql,businessKey,userId);
			for (TaskInfo t1 : taskInfo) {
		        
				humanTaskId =Long.toString(t1.getId())  ;
				processInstanceId = t1.getProcessInstanceId();
	        
	        }*/
			 formParameter.setAction("撤销申请");
			 formParameter.setBusinessKey(businessKey);
			 processParameters.put("leaderComment", "撤销申请");
			 //formParameter.setComment(strRemark);
	        
			 Record record = keyValueConnector.findByCode(formParameter.getBusinessKey());
			 //判断撤销申请时是驳回后撤销还是撤回后撤销 2018-01-27 shijingxin
			 if(status.equals("7")){
				 this.operationService.completeTask(humanTaskId, userId,
		                    formParameter, processParameters, record,
		                    processInstanceId);
				//审批后将此任务对应的消息置为已读 2018.09.07 sjx
	            String updateMsg = "update MsgInfo set status=1 where data=?";
	            msgInfoManager.batchUpdate(updateMsg, humanTaskId);
			 }else if(status.equals("8")){
				 workspaceController.endProcessInstance(processInstanceId, humanTaskId);
			 }
			 
		     
			returnMap.put("bSuccess", "true");
			returnMap.put("strMsg", "撤销成功");
		}catch (Exception ex) {
			returnMap.put("bSuccess", "false");
			returnMap.put("strMsg", "流程撤销失败，请联系管理员");
			logger.info("手机APP--审批--撤销申请--普通流程-异常："
					+ex.getMessage()+"\r\n"+ex.getStackTrace());
		}
		return returnMap;
	}
	
	/**
	 * 
	 * 1.18.审批--发起自定义申请（ToH5CustomApply） 
	 * chengze at 2017.10.21
	 * 
	 * **/
	@Log(desc = "审批--发起自定义申请", action = "CustomApply", operationDesc = "手机APP-审批-自定义申请")
	@Transactional(readOnly = false)
	public Map<String, Object> CustomApply(Map<String, Object> decryptedMap){
		
		Long FormId =0L;//记录表单的主键ID
        String ccnos = "";//抄送人编号集合，使用“,”分割
        String ccName = "";//抄送人姓名集合，使用“,”分割
        String nextID = "";//指定下一步审批人编号
        String nextUser= "";//指定下一步审批人姓名
        String strAnnex = "";
        String copyId = "";
        String copyName = "";
        
        Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			//orgConnector.
			//验证参数================================================================================
			if (!decryptedMap.containsKey("strPerCode") || StringUtils.isBlank(decryptedMap.get("strPerCode").toString())
				||!decryptedMap.containsKey("strRealName") ||StringUtils.isBlank(decryptedMap.get("strRealName").toString())
				||!decryptedMap.containsKey("strFRName") || StringUtils.isBlank(decryptedMap.get("strFRName").toString())
				||!decryptedMap.containsKey("strApplyContent") ||StringUtils.isBlank(decryptedMap.get("strApplyContent").toString())
				||!decryptedMap.containsKey("strAuditors") || StringUtils.isBlank(decryptedMap.get("strAuditors").toString())) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "参数错误");
				logger.info("手机APP-发起自定义申请-参数错误");
				return returnMap;
			}
			
			//获取数据================================================
			String curentUserName  =decryptedMap.get("strRealName").toString(); //申请人姓名
			String userId  = decryptedMap.get("strPerCode").toString();//申请人id
			String theme = decryptedMap.get("strFRName").toString();//申请主题
			String content = decryptedMap.get("strApplyContent").toString();//申请内容
			nextID = decryptedMap.get("strAuditors").toString();//指定审批人编号
			nextUser  = decryptedMap.get("strAuditorsName").toString();//指定审批人姓名
			
			if (theme.length()>100) {
					returnMap.put("bSuccess", "false");
					returnMap.put("strMsg", "主题不要超过100字");
					logger.info("手机APP-发起自定义申请-主题超过100字");
					return returnMap;
				}
			
			if (content.length()>4000) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "申请内容不要超过4000字");
				logger.info("手机APP-发起自定义申请-申请内容超过4000字");
				return returnMap;
			}
			
			//if(checkEmoji(theme+content))
			//{
				//returnMap.put("bSuccess", "false");
				//returnMap.put("strMsg", "申请标题或者内容中有非法字符！");
				//return returnMap;
			//}
			//content=emojiChange(content);
			
			//抄送人编号集合，使用“,”分割
			if (!(!decryptedMap.containsKey("strCC") || StringUtils.isBlank(decryptedMap.get("strCC").toString()))) {
					 ccnos =  decryptedMap.get("strCC").toString();
					 ccName=  decryptedMap.get("strCCName").toString();
					 copyId = decryptedMap.get("strCC").toString();
					 copyId = copyId.replaceAll("jobs", "岗位:");
					 copyName = decryptedMap.get("strCCName").toString();
					/* String copyIds = "";
					 String copyNames = "";
					 //ckx  add 2018/8/31  抄送包含岗位
					 if(null != ccnos && !"".equals(ccnos) && !"null".equals(ccnos)){
			        	String[] splitCopyId = ccnos.split(",");
			            String[] splitCopyName = ccName.split(",");
			            for (int i = 0; i < splitCopyId.length; i++) {
			            	String strCopyId = splitCopyId[i];
			            	String strCopyName = splitCopyName[i];
			            	if(strCopyId.contains("jobs")){
			    				//岗位查询人员
			            		strCopyId = strCopyId.replaceAll("jobs", "");
			    	    		
			            		Map<String, Object> queryCopyMap = jdbcTemplate.queryForMap("select * from party_entity e LEFT JOIN party_struct s ON e.ID = s.PARENT_ENTITY_ID where s.CHILD_ENTITY_ID = '"+strCopyId+"';");
			    	    		String strCopyNameS = StringUtil.toString(queryCopyMap.get("NAME"));
			            		String lastPost = customWorkService.getLastPost(strCopyId);
			    	    		copyIds += strCopyId+",";
			    				copyNames += lastPost+strCopyName+",";
			    			}else{
			    				copyIds += strCopyId+",";
			    				copyNames += strCopyName+",";
			    			}
			    		}
			            
			            ccnos = copyIds.substring(0, copyIds.length()-1);
			            ccName = copyNames.substring(0, copyNames.length()-1);
					 }*/
			}
			
			//生成受理单编号
	        String code = operationService.CreateApplyCode(userId);
			//生成 businessKey
	        LinkedMultiValueMap<String,String>  multiValueMap = new LinkedMultiValueMap();
			multiValueMap.set("theme", theme);
			multiValueMap.set("applyCode", code);
			multiValueMap.set("busType", "9999");
			multiValueMap.set("businessType", "自定义");
			multiValueMap.set("busDetails", "8888");
			multiValueMap.set("businessDetail", "自定义申请");
			multiValueMap.set("submitTimes", "1");
			multiValueMap.set("applyCode", code);
			multiValueMap.set("autoCompleteFirstTask", "false");
			multiValueMap.set("url", "/operationCustom/custom-detail.do?suspendStatus=custom");
			multiValueMap.set("content", content);
			multiValueMap.set("name", curentUserName);
			multiValueMap.set("nextID", nextID.split(",")[0]);
			
			//取所属大区和分公司
	        String areaName="";
        	String areaId = "";
        	PartyEntity partyEntity  =  orgConnector.findPartyAreaByUserId(userId);
        	
        	if ( partyEntity !=  null){
        		areaId  = Long.toString(partyEntity.getId());
				areaName = partyEntity.getName();
        	}
        	multiValueMap.set("areaId", areaId);
			multiValueMap.set("area", areaName);
			
			FormParameter formParameter = new FormParameter();
			formParameter.setBpmProcessId("-1");
			formParameter.setMultiValueMap(multiValueMap);
			
			String businessKey = this.operationService.saveDraft(userId, "1",formParameter);
			
			//logger.info("手机APP-我的传入参数：strFRCode="+frCode);
			
			//存入表单内容			
			CustomEntity customEntity = new CustomEntity();
	        customEntity.setTheme(theme);
	        customEntity.setApplyCode(code);
	        customEntity.setUserId(Long.parseLong(userId));
	        String tenantId = "1";
	        customEntity.setBusinessDetail("自定义申请");
	        customEntity.setName(curentUserName);
	        customEntity.setApplyContent(content);
	        customEntity.setBusinessType("自定义");
	        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    	String str = sdf.format(new Date());
	        customEntity.setCreateTime(str);
	        customEntity.setModifyTime(str);
	        customEntity.setSubmitTimes(1);
	        //customEntity.setCcName(ccName);
	        //customEntity.setCcnos(ccnos);
	        String strBusiLevel=orgConnector.getPositionMaxGradeByUserIds(nextID);
	        customEntity.setBusinessLevel(strBusiLevel);
	        customManager.save(customEntity);
	        
	        //添加审批人 add by lilei at 2017.11.27
	        String[] auditList=nextID.split(",");
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
			
	        FormId = customEntity.getId();
	        
	        // 附件---------------------------------------------------------------------------------------------------------
 			if (!(!decryptedMap.containsKey("strFiles") || StringUtils.isBlank(decryptedMap.get("strFiles").toString()))) {
 				strAnnex =  decryptedMap.get("strFiles").toString();
 			}
 			
 			if (!strAnnex.isEmpty()) {
 				String[] split_data = strAnnex.split(",");
 				for (String path : split_data) {
 					fileUploadAPI.uploadFile(path, "1", Long.toString(FormId), "operation/CustomApply", "0");
 				}
 			}
	        
			//发起流程
	        this.customService.customStartApply(customEntity,curentUserName, userId, auditNameList[0], 
	        		auditList[0], FormId, businessKey, copyId,copyName);
	        
	        //处理受理单编号
	        operationService.deleteApplyCode(code);
	               	
	        this.customService.SaveFormHtml(customEntity,businessKey);
	        
	        //SaveFormHtml(customEntity,businessKey);
	        
			returnMap.put("bSuccess", "true");
			returnMap.put("strMsg", "申请成功");
		}catch (Exception ex) {
			returnMap.put("bSuccess", "false");
			returnMap.put("strMsg", "申请失败，请联系管理员");
			logger.info("手机APP--审批--自定义申请-异常："
					+ex.getMessage()+"\r\n"+ex.getStackTrace());
		}
		return returnMap;
	}
    
	/**
	 * 请假，加班，出差外出，特殊考勤说明提交申请
	 * @param decryptedMap
	 * @return
	 * ckx 2018/08/7
	 */
	@Transactional(readOnly = false)
	public Map<String, Object> CustomApplyWork(Map<String, Object> decryptedMap) {
		Long FormId =0L;//记录表单的主键ID
        String ccnos = "";//抄送人编号集合，使用“,”分割
        String ccName = "";//抄送人姓名集合，使用“,”分割
        String nextID = "";//指定下一步审批人编号
        String nextUser= "";//指定下一步审批人姓名
        String strAnnex = "";
        String txnos = "";//同行人编号集合，使用,分割
        String txName = "";//抄送人姓名集合，使用“,”分割
        String destination = "";//目的地
        String busDetails = "";//业务细分
        String businessDetail = "";//业务细分名称
        String theme = "";//主题
        String totalTime = ""; //总计时间
        String copyId = "";
        String copyName = "";
        
        Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			//orgConnector.
			//验证参数================================================================================
			if (!decryptedMap.containsKey("strPerCode") || StringUtils.isBlank(decryptedMap.get("strPerCode").toString())
				||!decryptedMap.containsKey("strRealName") || StringUtils.isBlank(decryptedMap.get("strRealName").toString())
				||!decryptedMap.containsKey("strFormType") || StringUtils.isBlank(decryptedMap.get("strFormType").toString())
				||!decryptedMap.containsKey("strType") || StringUtils.isBlank(decryptedMap.get("strType").toString())
				||!decryptedMap.containsKey("strStartTime") || StringUtils.isBlank(decryptedMap.get("strStartTime").toString())
				||!decryptedMap.containsKey("strEndTime") || StringUtils.isBlank(decryptedMap.get("strEndTime").toString())
				/*||!decryptedMap.containsKey("strTotalTime")||decryptedMap.get("strTotalTime").equals(null)||decryptedMap.get("strTotalTime").equals("")*/
				||!decryptedMap.containsKey("strApplyContent") || StringUtils.isBlank(decryptedMap.get("strApplyContent").toString())
				||!decryptedMap.containsKey("strAuditors") || StringUtils.isBlank(decryptedMap.get("strAuditors").toString())) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "参数错误");
				logger.info("手机APP-发起自定义申请-参数错误");
				return returnMap;
			}
			
			//获取数据================================================
			String curentUserName  =decryptedMap.get("strRealName").toString(); //申请人姓名
			String userId  = decryptedMap.get("strPerCode").toString();//申请人id
			//decryptedMap.get("strFRName").toString();//申请主题
			String content = decryptedMap.get("strApplyContent").toString();//申请内容
			String formType = decryptedMap.get("strFormType").toString();//表单类型  1：请假  2：出差外出  3：加班  4：特殊考勤说明
			String type = decryptedMap.get("strType").toString(); //表单具体类型
			String startTime = decryptedMap.get("strStartTime").toString(); //开始时间
			String endTime = decryptedMap.get("strEndTime").toString(); //结束时间
			
			nextID = decryptedMap.get("strAuditors").toString();//指定审批人编号
			nextUser  = decryptedMap.get("strAuditorsName").toString();//指定审批人姓名
			//总计时间
			if(!(!decryptedMap.containsKey("strTotalTime") || StringUtils.isBlank(decryptedMap.get("strTotalTime").toString()))){
				totalTime = decryptedMap.get("strTotalTime").toString();
			}
			//是否绑定考勤组
			boolean checkedAttendanceRecords = customWorkService.checkedAttendanceRecords(userId);
			if(!checkedAttendanceRecords){
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "未绑定考勤组");
				logger.info("手机APP-发起自定义申请-用户未绑定考勤组-"+userId);
				return returnMap;
			}
			
			if("1".equals(formType)){
				theme = "请假申请";
				busDetails = "8001";
				businessDetail = "请假申请";
			}else if("2".equals(formType)){
				theme = "出差外出申请";
				busDetails = "8002";
				businessDetail = "出差外出申请";
			}else if("3".equals(formType)){
				theme = "加班申请";
				busDetails = "8003";
				businessDetail = "加班申请";
			}else if("4".equals(formType)){
				theme = "特殊考勤说明申请";
				busDetails = "8004";
				businessDetail = "特殊考勤说明申请";
			}
			//查询部门名称
			Map<String, Object> queryForMap = jdbcTemplate.queryForMap("select department_name as departmentName from person_info where id = "+userId);
			String departmentName = queryForMap.get("departmentName").toString();
			//申请日期
			String date = DateUtil.formatDate(new Date(), " yyyy 年 MM 月 dd 日");
			
			if (content.length()>4000) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "申请内容不要超过4000字");
				logger.info("手机APP-发起自定义申请-申请内容超过4000字");
				return returnMap;
			}
			
			//if(checkEmoji(theme+content))
			//{
				//returnMap.put("bSuccess", "false");
				//returnMap.put("strMsg", "申请标题或者内容中有非法字符！");
				//return returnMap;
			//}
			//content=emojiChange(content);
			
			//抄送人编号集合，使用“,”分割
			if (!(!decryptedMap.containsKey("strCC") || StringUtils.isBlank(decryptedMap.get("strCC").toString()))) {
					 ccnos =  decryptedMap.get("strCC").toString();
					 ccName=  decryptedMap.get("strCCName").toString();
					 copyId = decryptedMap.get("strCC").toString();
					 copyId = copyId.replaceAll("jobs", "岗位:");
					 copyName = decryptedMap.get("strCCName").toString();
					 /*String copyIds = "";
					 String copyNames = "";
					 //ckx  add 2018/8/31  抄送包含岗位
					 if(null != ccnos && !"".equals(ccnos) && !"null".equals(ccnos)){
			        	String[] splitCopyId = ccnos.split(",");
			            String[] splitCopyName = ccName.split(",");
			            for (int i = 0; i < splitCopyId.length; i++) {
			            	String strCopyId = splitCopyId[i];
			            	String strCopyName = splitCopyName[i];
			            	if(strCopyId.contains("jobs")){
			    				//岗位查询人员
			            		strCopyId = strCopyId.replaceAll("jobs", "");
			    	    		
			            		Map<String, Object> queryCopyMap = jdbcTemplate.queryForMap("select * from party_entity e LEFT JOIN party_struct s ON e.ID = s.PARENT_ENTITY_ID where s.CHILD_ENTITY_ID = '"+strCopyId+"';");
			    	    		String strCopyNameS = StringUtil.toString(queryCopyMap.get("NAME"));
			            		String lastPost = customWorkService.getLastPost(strCopyId);
			    	    		copyIds += strCopyId+",";
			    				copyNames += lastPost+strCopyName+",";
			    			}else{
			    				copyIds += strCopyId+",";
			    				copyNames += strCopyName+",";
			    			}
			    		}
			            
			            ccnos = copyIds.substring(0, copyIds.length()-1);
			            ccName = copyNames.substring(0, copyNames.length()-1);
					 }*/
					 
			}
			//同行人
			if (!(!decryptedMap.containsKey("strTX") || StringUtils.isBlank(decryptedMap.get("strTX").toString()))) {
				 txnos =  decryptedMap.get("strTX").toString();
				 txName=  decryptedMap.get("strTXName").toString();
			}
			//目的地
			if (!(!decryptedMap.containsKey("strDestination") || StringUtils.isBlank(decryptedMap.get("strDestination").toString()))) {
				destination =  decryptedMap.get("strDestination").toString();
			}
			
			//生成受理单编号
	        String code = operationService.CreateApplyCode(userId);
			//生成 businessKey
	        LinkedMultiValueMap<String,String>  multiValueMap = new LinkedMultiValueMap();
			multiValueMap.set("theme", theme);
			multiValueMap.set("applyCode", code);
			multiValueMap.set("busType", "9999");
			multiValueMap.set("businessType", "自定义");
			multiValueMap.set("busDetails", busDetails);
			multiValueMap.set("businessDetail", businessDetail);
			multiValueMap.set("submitTimes", "1");
			multiValueMap.set("applyCode", code);
			multiValueMap.set("autoCompleteFirstTask", "false");
			multiValueMap.set("url", "/workOperationCustom/custom-work-detail.do?suspendStatus=custom");
			multiValueMap.set("content", content);
			multiValueMap.set("name", curentUserName);
			multiValueMap.set("nextID", nextID.split(",")[0]);
			
			//取所属大区和分公司
	        String areaName="";
        	String areaId = "";
        	PartyEntity partyEntity  =  orgConnector.findPartyAreaByUserId(userId);
        	
        	if ( partyEntity !=  null){
        		areaId  = Long.toString(partyEntity.getId());
				areaName = partyEntity.getName();
        	}
        	multiValueMap.set("areaId", areaId);
			multiValueMap.set("area", areaName);
			
			FormParameter formParameter = new FormParameter();
			formParameter.setBpmProcessId("-1");
			formParameter.setMultiValueMap(multiValueMap);
			
			String businessKey = this.operationService.saveDraft(userId, "1",formParameter);
			
			//logger.info("手机APP-我的传入参数：strFRCode="+frCode);
			
			//存入表单内容			
			CustomEntity customEntity = new CustomEntity();
	        customEntity.setTheme(theme);
	        customEntity.setApplyCode(code);
	        customEntity.setUserId(Long.parseLong(userId));
	        String tenantId = "1";
	       // customEntity.setBusinessDetail("自定义申请");
	        customEntity.setName(curentUserName);
	        customEntity.setApplyContent(content);
	        customEntity.setBusinessType("自定义");
	        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    	String str = sdf.format(new Date());
	        customEntity.setCreateTime(str);
	        customEntity.setModifyTime(str);
	        customEntity.setSubmitTimes(1);
	        //customEntity.setCcName(ccName);
	        //customEntity.setCcnos(ccnos);
	        String strBusiLevel=orgConnector.getPositionMaxGradeByUserIds(nextID);
	        customEntity.setBusinessLevel(strBusiLevel);
	        
	        //
	        customEntity.setFormType(formType);
	        customEntity.setType(type);
	        customEntity.setStartTime(startTime);
	        customEntity.setEndTime(endTime);
	        customEntity.setTotalTime(totalTime);
	        customEntity.setDestination(destination);
	        customEntity.setDepartmentName(departmentName);
	        customEntity.setDate(date);
	        customEntity.setPeerId(txnos);
	        customEntity.setPeerName(txName);
	        customEntity.setBusinessDetail(businessDetail);
	        customEntity.setBusinessType("自定义");
	        
	        customManager.save(customEntity);
	        
	        //构造实体类
	        CustomWorkEntityDTO customWorkEntityDTO = new CustomWorkEntityDTO();
	        customWorkEntityDTO.setDate(date);
	        customWorkEntityDTO.setStartDate(startTime);
	        customWorkEntityDTO.setEndDate(endTime);
	        customWorkEntityDTO.setTxName(txName);
	        customWorkEntityDTO.setTotalTime(totalTime);
	        customWorkEntityDTO.setDestination(destination);
	        customWorkEntityDTO.setFormType(formType);
	        customWorkEntityDTO.setStartDate(startTime);
	        customWorkEntityDTO.setEndDate(endTime);
	        
	        //添加审批人 add by lilei at 2017.11.27
	        String[] auditList=nextID.split(",");
	        String[] auditNameList=nextUser.split(",");
	        int q=1;
	        for(String auditor:auditList){
	        	CustomApprover approver=new CustomApprover();
	        	approver.setApproverId(Long.valueOf(auditor));
	        	approver.setCustomId(customEntity.getId());
	        	approver.setBusinessKey(businessKey);
	        	approver.setApproveStep(q);
	        	approver.setOpterType("0");
	        	approver.setAuditComment("");
	        	customApproverManager.save(approver);
	        	q++;
	        }
			
	        FormId = customEntity.getId();
	        //保存自定义请假详情表    方便考勤列表展示  start
	        customWorkService.saveLeaveDetails(customWorkEntityDTO, userId, customEntity);
	        // 附件---------------------------------------------------------------------------------------------------------
 			/*if (!(!decryptedMap.containsKey("strFiles")||decryptedMap.get("strFiles").equals(null)||decryptedMap.get("strFiles").equals(""))) {
 				strAnnex =  decryptedMap.get("strFiles").toString();
 			}
 			
 			if (!strAnnex.isEmpty()) {
 				String[] split_data = strAnnex.split(",");
 				for (String path : split_data) {
 					fileUploadAPI.uploadFile(path, "1", Long.toString(FormId), "operation/CustomApply", "0");
 				}
 			}*/
	        
			//发起流程
	        this.customWorkService.customStartApply(customEntity,curentUserName, userId, auditNameList[0], 
	        		auditList[0], FormId, businessKey, copyId,copyName);
	        //处理受理单编号
	        operationService.deleteApplyCode(code);
	        this.customWorkService.SaveFormHtml(customEntity,customWorkEntityDTO,businessKey);
			returnMap.put("bSuccess", "true");
			returnMap.put("strMsg", "申请成功");
		}catch (Exception ex) {
			returnMap.put("bSuccess", "false");
			returnMap.put("strMsg", "申请失败，请联系管理员");
			logger.info("手机APP--审批--自定义申请-异常："
					+ex.getMessage()+"\r\n"+ex.getStackTrace());
		}
		return returnMap;
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
	 * 获取预设置审核人   ckx add 2018/8/7
	 * @param decryptedMap
	 * @return
	 */
	public Map<String, Object> CustomPresetApprovers(
			Map<String, Object> decryptedMap) {
		String userId  = decryptedMap.get("strPerCode").toString();//当前登陆人
		// 返回值
		Map<String, Object> returnMap = new HashMap<String, Object>();
		
		List<Map<String, Object>> arrayList = new ArrayList<Map<String,Object>>();
		List<CustomPresetApprover> customPresetApprovers = userService.getCustomPresetApproversH5(userId);
		for (CustomPresetApprover customPresetApprover : customPresetApprovers) {
			//保存数据
			Map<String, Object> dataMap = new HashMap<String, Object>();
			dataMap.put("id", customPresetApprover.getId());
			dataMap.put("approverIds", customPresetApprover.getApproverIds());
			dataMap.put("name", customPresetApprover.getName());
			String findNamesByIds = userConnector.findNamesByIds(customPresetApprover.getApproverIds());
			dataMap.put("userName", findNamesByIds);
			arrayList.add(dataMap);
		}
		returnMap.put("bSuccess", "true");
		returnMap.put("strMsg", "获取成功");
		returnMap.put("customPresetApproversData", arrayList);
		return returnMap;
	}
	/**
	 * 保存抄送
	 * @param decryptedMap
	 * @return
	 */
	@Transactional(readOnly = false)
	public Map<String, Object> customCopySave(Map<String, Object> decryptedMap) {
		
		Map<String, Object> returnMap = new HashMap<String, Object>();
		//验证参数================================================================================
		try {
			if (!decryptedMap.containsKey("strPerCode") || StringUtils.isBlank(decryptedMap.get("strPerCode").toString())
				||!decryptedMap.containsKey("processInstanceId") || StringUtils.isBlank(decryptedMap.get("processInstanceId").toString())
				||!decryptedMap.containsKey("strRealName") || StringUtils.isBlank(decryptedMap.get("strRealName").toString())
				||!decryptedMap.containsKey("copyIds") || StringUtils.isBlank(decryptedMap.get("copyIds").toString())
				||!decryptedMap.containsKey("copyNames") || StringUtils.isBlank(decryptedMap.get("copyNames").toString())) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "参数错误");
				logger.info("手机APP-随时抄送-参数错误");
				return returnMap;
			}
			
			String userId  = decryptedMap.get("strPerCode").toString();//当前登陆人
			String userName  = decryptedMap.get("strRealName").toString();//当前登陆人
			String processInstanceId  = decryptedMap.get("processInstanceId").toString();//流程实例id
			String copyIds  = decryptedMap.get("copyIds").toString();//抄送人id
			String copyNames  = decryptedMap.get("copyNames").toString();//抄送人姓名
			String title  = decryptedMap.get("title").toString();//主题
			
			//取申请单的 流程实例ID
			String sql="select  r.* from kv_record r where r.applycode='" +processInstanceId+"'";
			List<Map<String, Object>> list=jdbcTemplate.queryForList(sql);
			if(list==null||list.size()<1){
				returnMap.put("strMsg", "流程实例id不存在");
				returnMap.put("bSuccess", "false");
				logger.info("手机APP--随时抄送---异常：流程实例id不存在");
				return returnMap;
			}else {
				for(Map<String, Object> map:list){
					processInstanceId =StringUtil.toString(map.get("ref")); 
				}
			}
			copyIds = copyIds.replaceAll("jobs", "岗位:");
			customWorkService.copySave(processInstanceId, copyIds, copyNames, title,userId);
			returnMap.put("bSuccess", "true");
			returnMap.put("strMsg", "抄送成功");
			logger.info("抄送成功");
		} catch (Exception e) {
			returnMap.put("bSuccess", "false");
			returnMap.put("strMsg", "提交失败，请联系管理员");
			logger.info("手机APP--随时抄送---异常："+e.getMessage()+"\r\n"+e.getStackTrace());
		}
		return returnMap;
	}
	/**
	 * @param processInstanceId
	 * 抄送人通过详情或主题查看了抄送任务，则对应的消息已读
	 * @author sjx
	 */
	@Transactional(readOnly=false)
	public void copyMsgUpdate(Map<String,Object> param){
		String applyCode = param.get("strFRCode").toString();
		String userId = param.get("userId").toString();
		String processInstanceIdByCode = "select * from kv_record where applyCode='"+applyCode+"'";
		Map<String,Object> record = jdbcTemplate.queryForMap(processInstanceIdByCode);
		if(record != null&&record.get("audit_status").toString().equals("2")){
			/*String processInstanceId = record.get("ref").toString();
			String sql = "select * from task_info where process_instance_id='"+processInstanceId+"'";*/
			String businessKey = StringUtil.toString(record.get("business_key"));
			String sql = "select * from task_info where business_key='"+businessKey+"' and catalog = 'copy' and assignee = '"+userId+"'";
			List<Map<String,Object>> taskInfos = jdbcTemplate.queryForList(sql);
			
			//抄送岗位
			List<Map<String,Object>> queryForList = jdbcTemplate.queryForList("select e.* from party_entity e join party_struct s on e.id=s.PARENT_ENTITY_ID where  e.DEL_FLAG = '0' and TYPE_ID = '5' and s.CHILD_ENTITY_ID= '"+userId+"';");
			if(null == taskInfos || taskInfos.size() == 0){
				if(queryForList.size() > 0){
					for (Map<String, Object> map : queryForList) {
						String postId = map.get("id").toString();
						sql = "select * from task_info where business_key='"+businessKey+"' and catalog = 'copy' and assignee = '"+postId+"'";
						taskInfos = jdbcTemplate.queryForList(sql);
						if(taskInfos.size() > 0){
							continue;
						}
					}
				}
			}
			
			if(taskInfos.size() > 0){
				Map<String, Object> task = taskInfos.get(0);
				String data = task.get("id").toString();
				String updateMsg = "update MsgInfo set status=1 where data=? and receiver_id='"+userId+"'";
				msgInfoManager.batchUpdate(updateMsg, data);
				//ckx 2019/2/11  抄送已读
				String taskInfoCopyHql = "from TaskInfoCopy where businessKey = ? and userId = ?";
				TaskInfoCopy findUnique = taskInfoCopyManager.findUnique(taskInfoCopyHql, businessKey,userId);
				if(null == findUnique){
					 TaskInfoCopy taskInfoCopy = new TaskInfoCopy();
				 	 taskInfoCopy.setBusinessKey(businessKey);
				 	 taskInfoCopy.setUserId(userId);
				 	 taskInfoCopyManager.save(taskInfoCopy);
				}
			}
		}
	}
	
	private String getNotNullString(String strValue){
		return strValue==null?"":strValue;
	}
	
	//region 资源注入
	@Resource
	public void setPersonInfoManager(PersonInfoManager personInfoManager){
		this.personInfoManager=personInfoManager;
	}
	
	@Resource
	public void setOrgConnector(OrgConnector orgConnector){
		this.orgConnector=orgConnector;
	}
	
	@Resource
	public void setBusinessTypeManager(BusinessTypeManager businessTypeManager){
		this.businessTypeManager=businessTypeManager;
	}
	
	@Resource
	public void setTenantHolder(TenantHolder tenantHolder){
		this.tenantHolder=tenantHolder;
	}
	
	@Resource
	public void setBusinessDetailManager(BusinessDetailManager businessDetailManager){
		this.businessDetailManager=businessDetailManager;
	}
	
	@Resource
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
	
	@Resource
	public void setPartyEntityManager(PartyEntityManager partyEntityManager){
		this.partyEntityManager = partyEntityManager;
	}
	
	@Resource
	public void setPartyConnector(PartyConnector partyConnector)
	{
		this.partyConnector=partyConnector;
	}
	
	@Resource
    public void setHumanTaskConnector(HumanTaskConnector humanTaskConnector) {
        this.humanTaskConnector = humanTaskConnector;
    }
	
	@Resource
    public void setDictConnector(DictConnector dictConnector){
    	this.dictConnector=dictConnector;
    }
	
	@Resource
    public void setAccountInfoManager(AccountInfoManager accountInfoManager){
    	this.accountInfoManager=accountInfoManager;
    }
	
	@Resource
    public void setFileUploadAPI(FileUploadAPI fileUploadAPI){
    	this.fileUploadAPI=fileUploadAPI;
    }
	
	@Resource
    public void setWebAPI(WebAPI webAPI){
    	this.webAPI=webAPI;
    }
	
	@Resource
    public void setStoreConnector(StoreConnector storeConnector){
    	this.storeConnector=storeConnector;
    }

	
	@Resource
	public void setTaskInfoManager(TaskInfoManager taskInfoManager) {
		this.taskInfoManager = taskInfoManager;
	}
	
	@Resource
    public void setKeyValueConnector(KeyValueConnector keyValueConnector) {
        this.keyValueConnector = keyValueConnector;
    }
	@Resource
    public void setOperationService(OperationService operationService) {
        this.operationService = operationService;
    }
	@Resource
	public void setProcessOperationControllerCustom(
			ProcessOperationControllerCustom processOperationControllerCustom) {
		this.processOperationControllerCustom = processOperationControllerCustom;
	}

	@Resource
	public void setProcessOperationController(
			ProcessOperationController processOperationController) {
		this.processOperationController = processOperationController;
	}
	
	@Resource
	public void setCustomManager(CustomManager customManager) {
		this.customManager = customManager;
	}
	
	@Resource
    public void setMultipartResolver(MultipartResolver multipartResolver) {
        this.multipartResolver = multipartResolver;
    }

	
	@Resource
	public void setCustomService(CustomService customService) {
		this.customService = customService;
	}

	@Resource
    public void setIdGenerator(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }
	
	@Resource
    public void setAccountCredentialManager(AccountCredentialManager accountCredentialManager) {
        this.accountCredentialManager = accountCredentialManager;
    }
	
   @Resource
	public void setCustomPasswordEncoder(CustomPasswordEncoder customPasswordEncoder) {
		this.customPasswordEncoder = customPasswordEncoder;
	}
   	
   @Resource
	public void setCustomApproverManager(CustomApproverManager customApproverManager) {
		this.customApproverManager = customApproverManager;
	}
   
   @Resource
   public void setCustomConnector(CustomConnector customConnector){
	   this.customConnector = customConnector;
   }
   
   @Resource
   public void setUserConnector(UserConnector userConnector){
   	this.userConnector = userConnector;
   }
   
   @Resource
	public void setWSApplyService(WSApplyService wSApplyService) {
		this.wSApplyService = wSApplyService;
	}
   //endregion

	
	@Resource
	public void setProcessOperationControllerOa(
			ProcessOperationControllerOa processOperationControllerOa) {
		this.processOperationControllerOa = processOperationControllerOa;
	}
	
	@Resource
	public void setTimeTaskManager(TimeTaskManager timeTaskManager){
		this.timeTaskManager = timeTaskManager;
	}

	@Resource
	public void setProcessOperationOnlineController(
			ProcessOperationOnlineController processOperationOnlineController) {
		this.processOperationOnlineController = processOperationOnlineController;
	}
	
	@Resource
	public void setwSApiResource(WSApiResource wSApiResource) {
		this.wSApiResource = wSApiResource;
	}
   
	@Resource
	public void setOnLineInfoManager(OnLineInfoManager onLineInfoManager) {
		this.onLineInfoManager = onLineInfoManager;
	}

	@Resource
	public void setTaskInfoApprovePositionManager(TaskInfoApprovePositionManager taskInfoApprovePositionManager) {
		this.taskInfoApprovePositionManager = taskInfoApprovePositionManager;
	}
}

