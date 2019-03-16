package com.mossle.user.service;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.annotation.Resource;
import javax.jms.ConnectionFactory;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.core.Request;

import ch.qos.logback.core.net.LoginAuthenticator;

import com.mossle.api.dict.DictConnector;
import com.mossle.api.party.PartyConnector;
import com.mossle.api.party.PartyDTO;
import com.mossle.api.tenant.TenantConnector;
import com.mossle.api.tenant.TenantDTO;
import com.mossle.api.tenant.TenantHolder;
import com.mossle.api.user.UserCache;
import com.mossle.api.user.UserDTO;
import com.mossle.api.userauth.UserAuthCache;
import com.mossle.api.userauth.UserAuthConnector;
import com.mossle.api.userauth.UserAuthDTO;
import com.mossle.auth.component.AuthCache;
import com.mossle.auth.persistence.domain.UserStatus;
import com.mossle.auth.persistence.manager.UserStatusManager;
import com.mossle.auth.service.AuthService;
import com.mossle.common.utils.DateUtils;
import com.mossle.common.utils.StringUtils;
import com.mossle.core.auth.CurrentUserHolder;
import com.mossle.core.auth.CustomPasswordEncoder;
import com.mossle.core.id.IdGenerator;
import com.mossle.core.mapper.BeanMapper;
import com.mossle.core.mapper.JsonMapper;
import com.mossle.dict.persistence.domain.DictInfo;
import com.mossle.dict.persistence.manager.DictInfoManager;
import com.mossle.operation.persistence.domain.CustomEntity;
import com.mossle.operation.persistence.manager.CustomManager;
import com.mossle.operation.service.CustomService;
import com.mossle.party.PartyConstants;
import com.mossle.party.persistence.domain.PartyEntity;
import com.mossle.party.persistence.domain.PartyStruct;
import com.mossle.party.persistence.domain.PartyStructType;
import com.mossle.party.persistence.manager.PartyEntityManager;
import com.mossle.party.persistence.manager.PartyStructManager;
import com.mossle.party.persistence.manager.PartyStructTypeManager;
import com.mossle.party.persistence.manager.PartyTypeManager;
import com.mossle.user.PersonInfoConstants;
import com.mossle.user.persistence.domain.AccountCredential;
import com.mossle.user.persistence.domain.AccountInfo;
import com.mossle.user.persistence.domain.PersonInfo;
import com.mossle.user.persistence.domain.PersonWorkNumber;
import com.mossle.user.persistence.domain.RosterLog;
import com.mossle.user.persistence.domain.UpdatePerson;
import com.mossle.user.persistence.manager.AccountCredentialManager;
import com.mossle.user.persistence.manager.AccountInfoManager;
import com.mossle.user.persistence.manager.PersonInfoManager;
import com.mossle.user.persistence.manager.PersonWorkNumberManager;
import com.mossle.user.persistence.manager.RosterLogManager;
import com.mossle.user.persistence.manager.UpdatePersonManager;
import com.mossle.user.publish.UserPublisher;
import com.mossle.util.DateUtil;

import org.apache.commons.io.filefilter.FalseFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Field;



@Service
@Transactional(rollbackFor = Exception.class,readOnly = true)
public class PersonInfoService {
    private static Logger logger = LoggerFactory.getLogger(PersonInfoService.class);

    private PersonInfoManager personInfoManager;
    private AccountInfoManager accountInfoManager;
    private TenantHolder tenantHolder;
    private CurrentUserHolder currentUserHolder;
    private PartyTypeManager partyTypeManager;
    private PartyEntityManager partyEntityManager;
    private PartyStructTypeManager partyStructTypeManager;
    private PartyStructManager partyStructManager;
    private AccountCredentialManager accountCredentialManager;
    private CustomPasswordEncoder customPasswordEncoder;
    private UserCache userCache;
    private UserPublisher userPublisher;
    private PartyConnector partyConnector;
    private AuthService authService;
    private JdbcTemplate jdbcTemplate;
    private UserStatusManager userStatusManager;
    private AuthCache authCache;
    private IdGenerator idGenerator;
    private TenantConnector tenantConnector;
    private UserAuthConnector userAuthConnector;
    private UserAuthCache userAuthCache;
    private BeanMapper beanMapper = new BeanMapper();
    private JsonMapper jsonMapper=new JsonMapper();
    private UpdatePersonManager updatePersonManager;
    private CustomService customService;
    private PersonWorkNumberManager personWorkNumberManager;
    private CustomManager customManager;
    private DictInfoManager dictInfoManager;
    
    private String removeUserRoleSql = "UPDATE AUTH_USER_ROLE SET DEL_FLAG = '1' WHERE USER_STATUS_ID=?";
    private String removeUserSql = "UPDATE AUTH_USER_STATUS SET DEL_FLAG = '1' WHERE ID=?";

    private String selectUserSql = "SELECT ID FROM AUTH_USER_STATUS WHERE REF=? AND TENANT_ID=? AND DEL_FLAG ='0'";

    private RosterLogManager rosterLogManager ;
    
    private DictConnector dictConnector;
    /**
     * 定时修改离职人员状态
     * ckx 2018/11/12
     */
    @Transactional(readOnly=false)
    public void updateQuit(){
    	String personSql = "update person_info set QUIT_FLAG = 1,DEL_FLAG = 1 where QUIT_TIME <= NOW();";
    	String partySql = "update party_entity set DEL_FLAG = 1 where id IN (select id from person_info where DEL_FLAG = 1);";
    	String accountSql = "update account_info set DEL_FLAG = 1 where ID IN (select id from person_info where DEL_FLAG = 1);";
    	jdbcTemplate.update(personSql);
    	jdbcTemplate.update(partySql);
    	jdbcTemplate.update(accountSql);
    }
    
    @Transactional(readOnly=false)
    public void insertPersonInfoForAudit(
    		HttpServletRequest request,
    		PersonInfo personInfo,
    		int partyLevel,
    		Long partyEntityId,
    		Long postId) throws IOException, Exception{
    	
    	Integer numberInteger=Integer.valueOf(personInfo.getEmployeeNoNum());
    	PersonWorkNumber personWrokNumber=personWorkNumberManager.findUniqueBy("numberNo", numberInteger);//.findUniqueBy(hsqlString,numberInteger);
		if(personWrokNumber!=null){
			personWrokNumber.setIsUse("1");
			personWorkNumberManager.save(personWrokNumber);
		}
		
    	//接下来发起流程（修改职员信息走的是自定义申请的流程）--------------------------------------------------
        String content = " [花名册]录入新员工： "+personInfo.getFullName();
        MultipartFile[] files = null;
        customService.StartProcessCustomForPerson(request,personInfo.getFullName(),content,"",files,"1");
    	//JSON记录添加职员需要的信息
        Map<String,Object> jsonMap=new HashMap<String, Object>();
        jsonMap.put("personInfo", personInfo);
        jsonMap.put("partyLevel",partyLevel);
        jsonMap.put("parentPartyEntityId", partyEntityId);
        jsonMap.put("postPartyEntityId", postId);
        jsonMap.put("accountId",Long.parseLong(currentUserHolder.getUserId()));
        String jsonString=jsonMapper.toJson(jsonMap);
        
        UpdatePerson updatePerson = new UpdatePerson();
  		updatePerson.setJsonContent(jsonString);
  		updatePerson.setApplyCode(request.getParameter("applyCode"));
  		updatePerson.setIsApproval("2");//1：流程审核同意   0：流程审核不同意    2:审核中 20180515 cz 新录入数据 初始化为不同意，待审核通过再更新为1
  		updatePerson.setUpdateParameters("");
  		updatePerson.setEmployeeNo("");
  		updatePerson.setTypeID("personadd");
	  	updatePersonManager.save(updatePerson);
	  	
	  	
	  	String curentUserName = currentUserHolder.getName();
	  //记录日志
        RosterLog rosterLog = new RosterLog ();
	      rosterLog.setCode(request.getParameter("applyCode"));     					/** 受理单编号. */
	      rosterLog.setOperationID(curentUserName); 		/** 操作人员id. */
	      rosterLog.setContentBefore("添加新职员");  	/** 修改之前的内容. */
	      rosterLog.setContentNew("添加新职员");			/** 修改后的新内容. */
	      rosterLog.setUpdateColumn("添加新职员"); 		/**被修改的字段名. */
	      rosterLog.setUpdateColumnName("添加新职员");
	      rosterLog.setIsapproval("1");//1：流程审核还未通过   0：流程审核通过    20180515 cz 新录入数据 初始化为未通过，待审核通过再更新为0
	      rosterLog.setUpdateTime(new Date());  	  	/** 修改时间. */
	      rosterLog.setEmployeeNo("");				/**被修改的员工编号. */
	      rosterLogManager.save(rosterLog);
	
	  	
    }
    
    
    //驳回发起人重新申请
    @Transactional(readOnly=false)
    public void insertPersonInfoForModifySave(
		    		HttpServletRequest request,
		    		String typeID,
		    		 String gestureSwitch,
		    		 PersonInfo personInfo,
		    		 int partyLevel,
		    		 Long partyEntityId,
		    		 String applyCode,
		    		Long postId
		  ) throws IOException, Exception{
	    	
    	//离职人员走流程，先存入实际离职日期，和禁止登录系统日期   ckx  2018/11/9
    	String tenantId = tenantHolder.getTenantId();
    	PersonInfo oldPersonInfo = personInfoManager.get(personInfo.getId());
    	//禁止登陆系统时间
    	Date quitTime = personInfo.getQuitTime();
    	if(null != quitTime){
    		boolean boo = DateUtil.comparDate(quitTime, new Date());
    		//DateUtil.compareDate(quitTime, new Date());
    		//判断是否早于当天
    		if(boo){
    			oldPersonInfo.setDelFlag(PersonInfoConstants.DELETE_FLAG_YES); //ckx
    			oldPersonInfo.setQuitFlag(PersonInfoConstants.QUIT_FLAG_YES);    // 离职人员相当于一种特殊的删除
    			//personInfo.setQuitTime(new Date()); //ckx  
    			personInfoManager.update(oldPersonInfo);
    			// 处理party_entity表
    			PartyEntity partyEntity = partyEntityManager.get(personInfo.getId());
    			partyEntity.setDelFlag(PersonInfoConstants.DELETE_FLAG_YES); //ckx
    			partyEntityManager.update(partyEntity);
    			
    			// 处理account_info表
    			AccountInfo accountInfo = accountInfoManager.get(personInfo.getId());
    			accountInfo.setDelFlag(PersonInfoConstants.DELETE_FLAG_YES);
    			accountInfoManager.update(accountInfo);
    			
    			UserDTO userDto = new UserDTO();
    			userDto.setId(Long.toString(accountInfo.getId()));
    			userDto.setUsername(accountInfo.getUsername());
    			userDto.setRef(accountInfo.getCode());
    			userDto.setUserRepoRef(tenantId);
    			userCache.removeUser(userDto);
    			//userPublisher.notifyUserRemoved(this.convertUserDto(accountInfo, oldPersonInfo)); //ckx ？
    			//userPublisher.notifyUserUpdated(userDto);
    		}else{
    			oldPersonInfo.setDelFlag(PersonInfoConstants.DELETE_FLAG_NO); //ckx
    			oldPersonInfo.setQuitFlag(PersonInfoConstants.QUIT_FLAG_NO);
    			personInfoManager.update(oldPersonInfo);
    			
    			// 处理party_entity表
    			PartyEntity partyEntity = partyEntityManager.get(personInfo.getId());
    			partyEntity.setDelFlag(PersonInfoConstants.DELETE_FLAG_NO); //ckx
    			partyEntityManager.update(partyEntity);
    			
    			// 处理account_info表
    			AccountInfo accountInfo = accountInfoManager.get(personInfo.getId());
    			accountInfo.setDelFlag(PersonInfoConstants.DELETE_FLAG_NO);
    			accountInfoManager.update(accountInfo);
    			
    		}
    		oldPersonInfo.setQuitTime(personInfo.getQuitTime());
    		oldPersonInfo.setLeaveDate(personInfo.getLeaveDate());
    		personInfoManager.save(oldPersonInfo);
    	}/*else{
    		oldPersonInfo.setDelFlag(PersonInfoConstants.DELETE_FLAG_NO);
			oldPersonInfo.setQuitFlag(PersonInfoConstants.QUIT_FLAG_NO);
			oldPersonInfo.setQuitTime(null);
			oldPersonInfo.setLeaveDate(null);
			personInfoManager.update(oldPersonInfo);
			
			// 处理party_entity表
			PartyEntity partyEntity = partyEntityManager.get(personInfo.getId());
			partyEntity.setDelFlag(PersonInfoConstants.DELETE_FLAG_NO);
			partyEntityManager.update(partyEntity);
			
			// 处理account_info表
			AccountInfo accountInfo = accountInfoManager.get(personInfo.getId());
			accountInfo.setDelFlag(PersonInfoConstants.DELETE_FLAG_NO);
			accountInfoManager.update(accountInfo);
    	}*/
    	
    	
    	
	    //取当前操作人的名字
        String sqlForFullName = "SELECT FULL_NAME FROM person_info  WHERE id ="+currentUserHolder.getUserId();
        List<Map<String, Object>> listForFullName = jdbcTemplate.queryForList(sqlForFullName);
        String fullName = listForFullName.get(0).get("FULL_NAME").toString();
    	
	    	
	    	//修改后的person实体转为json串 存起来，等审核通过后，用这个json串去更新personInfo对应的那条数据      
				Map<String,Object> jsonMap=new HashMap<String, Object>();
	      UpdatePerson updatePerson=updatePersonManager.findUniqueBy("applyCode", applyCode);
	      if(typeID.equals("personUpdate")){
	  		jsonMap.put("confirmPassword","");
	  		jsonMap.put("gestureSwitch", gestureSwitch);
	  		
	      }else if(typeID.equals("personadd")){
	    	  personInfo.setDelFlag("0");
	    	  personInfo.setQuitFlag("0");
	          jsonMap.put("partyLevel",partyLevel);
	          jsonMap.put("parentPartyEntityId", partyEntityId);
	          jsonMap.put("postPartyEntityId", postId);
		    }
		      jsonMap.put("personInfo", personInfo);
		      jsonMap.put("accountId", Long.valueOf(currentUserHolder.getUserId()));
	      
		      	String jsonString=jsonMapper.toJson(jsonMap);
		        updatePerson.setJsonContent(jsonString);
		  		updatePerson.setIsApproval("2");//1：流程审核同意   0：流程审核不同意    2:审核中 20180515 cz 新录入数据 初始化为不同意，待审核通过再更新为1
		  		updatePerson.setEmployeeNo(personInfo.getId()==null?"":personInfo.getId().toString());
		  		
		  		updatePersonManager.update(updatePerson);

		  	  
		 			
		 			
		 			//若重新调整的是更新花名册  记录到日志中
		 	         if(typeID.equals("personUpdate")){
		 	        	 
		 	        	 //先删除，再创建
		 	        	 List<RosterLog> rLog = rosterLogManager.findBy("code", applyCode);
		 	        	 
		 	        	 for(RosterLog r:rLog){
		 	        		 rosterLogManager.remove(r);
		 	        	 }
		 	        
		 	        //对比两个类的属性，找出 修改了哪些内容
		 		        PersonInfo dest = null;
		 		        dest = personInfoManager.get(personInfo.getId());  
		 			      
		 		        Map<String, String> result = new HashMap<String, String>();
		 			
		 			      Field[] fs = personInfo.getClass().getDeclaredFields();
		 			      for (Field f : fs) {
		 			          f.setAccessible(true);
		 			          Object v1 = f.get(personInfo);
		 			          Object v2 = f.get(dest);
//		 			          if( ! equals(v1, v2)&& !f.getName().equals("addTime")&& !f.getName().equals("partyId")&& !f.getName().equals("isApproval")&& !f.getName().equals("employeeNoDepart")&& !f.getName().equals("positionCode")){
//		 			              result.put(f.getName(), String.valueOf(equals(v1, v2)));
//		 			         
		 			           if( ! equals(v1, v2)&& !f.getName().equals("resumeTime")&& !f.getName().equals("priority")&& !f.getName().equals("quitTime")&& !f.getName().equals("delFlag")&& !f.getName().equals("serialVersionUID")&&!f.getName().equals("addTime")&& !f.getName().equals("postId")&& !f.getName().equals("postName")&& !f.getName().equals("departmentCode")&& !f.getName().equals("companyCode")&& !f.getName().equals("partyId")&& !f.getName().equals("isApproval")&& !f.getName().equals("employeeNoDepart")&& !f.getName().equals("positionCode")&& !f.getName().equals("contractCompanyID")){
		 				              result.put(f.getName(), String.valueOf(equals(v1, v2)));
		 				             
		 			              
		 			              if(
		 			            		  (v1==null||(v1!=null&&v1.equals(""))) 
		 			            		  &&
		 			            		  (v2==null||(v2!=null&&v2.equals("")))
		 			            	)
		 			            		  {
		 			            	  			continue;
		 			            		  }
		 			              
		 			              if(v1!=null){
		 			            	  if(v1.equals("")){
		 		 	            		  v1="无";
		 			            	  }
		 			              }else {
		 			            	  v1 ="无";
		 			              }
		 			              
		 			              if(v2!=null){
		 			            	  if(v2.equals("")){
		 			            		  v2="无";
		 			            	  }
		 			              }else {
		 			            	  v2 ="无";
		 			              }
		 			             
//		 			             if(f.getName().equals("gender")||f.getName().equals("fertilityCondition")||f.getName().equals("marriage")||f.getName().equals("quitFlag")){
//		 			  	            		  
//		 			            	Map<String,String> map =  convertGender(f.getName(),v1.toString(),v2.toString());
//		 			            	
//		 			            	v1 = map.get("v1");
//		 			            	
//		 			            	v2 = map.get("v2");
//		 			              }
		 			              
		 			              RosterLog rosterLog = new RosterLog ();
		 					      
		 			              rosterLog.setCode(request.getParameter("applyCode"));     					/** 受理单编号. */
		 					      rosterLog.setOperationID(fullName); 		/** 操作人员id. */
		 					      rosterLog.setContentBefore(convertGender(f.getName(),v2.toString()));  	/** 修改之前的内容. */
		 					      rosterLog.setContentNew(convertGender(f.getName(),v1.toString()));			/** 修改后的新内容. */
		 					      rosterLog.setUpdateColumn(f.getName()); 		/**被修改的字段名. */
		 					      rosterLog.setUpdateColumnName(convertRosterLog(rosterLog));
		 					      rosterLog.setIsapproval("1");//1：流程审核还未通过   0：流程审核通过    20180515 cz 新录入数据 初始化为未通过，待审核通过再更新为0
		 					      
		 					      rosterLog.setUpdateTime(new Date());  	  	/** 修改时间. */
		 					      rosterLog.setEmployeeNo(personInfo.getId().toString());			/**被修改的员工编号. */
		 			          
		 					      rosterLogManager.save(rosterLog);
		 			          }
		 			      }
		 	         }	
		 			
		 	        //修改了哪些内容，记录到content里面
			         String content = "";
			   		List<RosterLog> rostlog = rosterLogManager.findBy("code",request.getParameter("applyCode"));
			   		
			 		CustomEntity customEntity =	customManager.findUniqueBy("applyCode", request.getParameter("applyCode"));
			 			
			 		if(typeID.equals("personUpdate")){
				 			customEntity.setTheme("  修改花名册  "
//			 			+personInfo.getFullName()
				 					);
				 			for(RosterLog r :rostlog){
				 				 content = "员工编号："+personInfo.getEmployeeNo()+" , 姓名："+personInfo.getFullName() 
				 						+"\n";
								 content = content + " ["+r.getUpdateColumnName() + "] 由  \" "+ (r.getContentBefore().equals("")?"无":r.getContentBefore())+"\"   修改：   \""+r.getContentNew()+"\" \n";	
							}
			 			}else if(typeID.equals("personadd")){
			 	 			customEntity.setTheme("  [花名册]新员工录入 "
//			 			+personInfo.getFullName()
			 	 					);
			 	 			content = " [花名册]录入新员工: "+personInfo.getFullName();	
			 	 		}
			 			customEntity.setApplyContent(content);
			 			customManager.save(customEntity);	
		 
		 	}

    
    
    
    //对比两个类的属性，找出 修改了哪些内容  上面 方法调用这个  cz 201805
    public static boolean equals(Object obj1, Object obj2) {

	    if (obj1 == obj2) {
	        return true;
	    }
	    if (obj1 == null || obj2 == null) {
	        return false;
	    }
	    return obj1.equals(obj2);
	}
    
    
    
    
    //驳回发起人重新申请
    @Transactional(readOnly=false)
    public void insertPersonInfoForUpdate(
		    		HttpServletRequest request,
		    		//String typeID,
		    		 String gestureSwitch,
		    		 PersonInfo personInfo
		    		// int partyLevel,
		    		 //Long partyEntityId,
		    		 //String applyCode,
		    		//Long postId
		  ) throws IOException, Exception{
    	
    	//离职人员走流程，先存入实际离职日期，和禁止登录系统日期   ckx  2018/11/7
    	String tenantId = tenantHolder.getTenantId();
    	PersonInfo oldPersonInfo = personInfoManager.get(personInfo.getId());
    	//禁止登陆系统时间
    	Date quitTime = personInfo.getQuitTime();
    	if(null != quitTime){
    		boolean boo = DateUtil.comparDate(quitTime, new Date());
    		//DateUtil.compareDate(quitTime, new Date());
    		//判断是否早于当天
    		if(boo){
    			oldPersonInfo.setDelFlag(PersonInfoConstants.DELETE_FLAG_YES); //ckx
    			oldPersonInfo.setQuitFlag(PersonInfoConstants.QUIT_FLAG_YES);    // 离职人员相当于一种特殊的删除
    			//personInfo.setQuitTime(new Date()); //ckx  
    			personInfoManager.update(oldPersonInfo);
    			// 处理party_entity表
    			PartyEntity partyEntity = partyEntityManager.get(personInfo.getId());
    			partyEntity.setDelFlag(PersonInfoConstants.DELETE_FLAG_YES); //ckx
    			partyEntityManager.update(partyEntity);
    			
    			// 处理account_info表
    			AccountInfo accountInfo = accountInfoManager.get(personInfo.getId());
    			accountInfo.setDelFlag(PersonInfoConstants.DELETE_FLAG_YES);
    			accountInfoManager.update(accountInfo);
    			
    			UserDTO userDto = new UserDTO();
    			userDto.setId(Long.toString(accountInfo.getId()));
    			userDto.setUsername(accountInfo.getUsername());
    			userDto.setRef(accountInfo.getCode());
    			userDto.setUserRepoRef(tenantId);
    			userCache.removeUser(userDto);
    			//userPublisher.notifyUserRemoved(this.convertUserDto(accountInfo, oldPersonInfo)); //ckx ？
    			//userPublisher.notifyUserUpdated(userDto);
    		}/*else{
    			oldPersonInfo.setQuitTime(personInfo.getQuitTime());
        		oldPersonInfo.setLeaveDate(personInfo.getLeaveDate());
        		oldPersonInfo.setQuitFlag(PersonInfoConstants.DELETE_FLAG_NO);
        		oldPersonInfo.setDelFlag(PersonInfoConstants.DELETE_FLAG_NO);
    			updatePersonInfo("0",oldPersonInfo, "", null,Long.valueOf(currentUserHolder.getUserId()));
    		}*/
    		Date oldQuitTime = oldPersonInfo.getQuitTime();
    		if(null == oldQuitTime){
    			oldPersonInfo.setQuitTime(personInfo.getQuitTime());
        		oldPersonInfo.setLeaveDate(personInfo.getLeaveDate());
        		personInfoManager.save(oldPersonInfo);
    		}
    		
    	}
    	
    	
    	
    	
    	//修改后的person实体转为json串 存起来，等审核通过后，用这个json串去更新personInfo对应的那条数据      
	        //JSON记录添加职员需要的信息
          Map<String,Object> jsonMap=new HashMap<String, Object>();
          jsonMap.put("personInfo", personInfo);
          jsonMap.put("confirmPassword","");
          jsonMap.put("gestureSwitch", gestureSwitch);
          jsonMap.put("accountId",Long.valueOf(currentUserHolder.getUserId()));
          
          String jsonString=jsonMapper.toJson(jsonMap);
          
          UpdatePerson updatePerson = new UpdatePerson();
    		updatePerson.setJsonContent(jsonString);
    		updatePerson.setApplyCode(request.getParameter("applyCode"));
    		updatePerson.setIsApproval("2");//1：流程审核还未通过   0：流程审核通过    20180515 cz 新录入数据 初始化为未通过，待审核通过再更新为0
    		updatePerson.setUpdateParameters("");
    		updatePerson.setEmployeeNo(personInfo.getId().toString());
    		updatePerson.setTypeID("personUpdate");
		
    		updatePersonManager.save(updatePerson);
    	
    	
    	    //取当前操作人的名字
            String sqlForFullName = "SELECT FULL_NAME FROM person_info  WHERE id ="+currentUserHolder.getUserId();
            List<Map<String, Object>> listForFullName = jdbcTemplate.queryForList(sqlForFullName);
            String fullName = listForFullName.get(0).get("FULL_NAME").toString();
    	
    		  //对比两个类的属性，找出 修改了哪些内容
            PersonInfo dest = null;
            dest = personInfoManager.get(personInfo.getId());  
    	      
            Map<String, String> result = new HashMap<String, String>();
    	
    	      Field[] fs = personInfo.getClass().getDeclaredFields();
    	      for (Field f : fs) {
    	          f.setAccessible(true);
    	          Object v1 = f.get(personInfo);
    	          Object v2 = f.get(dest);
    	          if( ! equals(v1, v2)&& !f.getName().equals("resumeTime")&& !f.getName().equals("priority")&& !f.getName().equals("quitTime")&& !f.getName().equals("delFlag")&& !f.getName().equals("serialVersionUID")&& !f.getName().equals("addTime")&& !f.getName().equals("postId")&& !f.getName().equals("postName")&& !f.getName().equals("departmentCode")&& !f.getName().equals("companyCode")&& !f.getName().equals("partyId")&& !f.getName().equals("isApproval")&& !f.getName().equals("employeeNoDepart")&& !f.getName().equals("positionCode")&& !f.getName().equals("contractCompanyID")){
    	              result.put(f.getName(), String.valueOf(equals(v1, v2)));
    	              
    	            
    	              if(
    	            		  (v1==null||(v1!=null&&v1.equals(""))) 
    	            		  &&
    	            		  (v2==null||(v2!=null&&v2.equals("")))
    	            	)
    	            		  {
    	            	  			continue;
    	            		  }
    	              
    	              if(v1!=null){
    	            	  if(v1.equals("")){
    	            		  v1="无";
    	            	  }
    	              }else {
    	            	  v1 ="无";
    	              }
    	              
    	              if(v2!=null){
    	            	  if(v2.equals("")){
    	            		  v2="无";
    	            	  }
    	              }else {
    	            	  v2 ="无";
    	              }
    	             
//    	              if(f.getName().equals("gender")
//    	            		  ||f.getName().equals("fertilityCondition")
//    	            		  ||f.getName().equals("marriage")
//    	            		  ||f.getName().equals("quitFlag")
//    	            		  ||f.getName().equals("politicalOutlook")//政治面貌
//    	            		  ||f.getName().equals("householdRegisterType")//户籍类型
//    	            		  ||f.getName().equals("nation")//民族
//    	            		  ||f.getName().equals("laborType")//用工类型
//    	            		  ||f.getName().equals("entryMode")//进入方式
//    	            		  ||f.getName().equals("education")//学历
//    	            		  ){
//    	            	  
//    	            	Map<String,String> map =  convertGender(f.getName(),v1.toString(),v2.toString());
//    	            	
//    	            	v1 = map.get("v1");
//    	            	
//    	            	v2 = map.get("v2");
//    	              }
    	              
    	              RosterLog rosterLog = new RosterLog ();
    			      
    			      rosterLog.setCode(request.getParameter("applyCode"));     					/** 受理单编号. */
    			      rosterLog.setOperationID(fullName); 		/** 操作人员id. */
    			      rosterLog.setContentBefore(convertGender(f.getName(),v2.toString()));  	/** 修改之前的内容. */
    			      rosterLog.setContentNew(convertGender(f.getName(),v1.toString()));			/** 修改后的新内容. */
    			      rosterLog.setUpdateColumn(f.getName()); 		/**被修改的字段名. */
    			      rosterLog.setUpdateColumnName(convertRosterLog(rosterLog));
    			      rosterLog.setIsapproval("1");//1：流程审核还未通过   0：流程审核通过    20180515 cz 新录入数据 初始化为未通过，待审核通过再更新为0
    			      
    			      rosterLog.setUpdateTime(new Date());  	  	/** 修改时间. */
    			      rosterLog.setEmployeeNo(personInfo.getId().toString());			/**被修改的员工编号. */
    	          
    			      rosterLogManager.save(rosterLog);
    	          }
    	      }
    	    //修改了哪些内容，记录到content里面
    		String content = "员工编号："+personInfo.getEmployeeNo()+" , 姓名："+personInfo.getFullName()+"\n";
    		List<RosterLog> rostlog = rosterLogManager.findBy("code",request.getParameter("applyCode"));
  			for(RosterLog r :rostlog){
  				 content = content + " ["+r.getUpdateColumnName() + "] 由   \""+ (r.getContentBefore().equals("")?"无":r.getContentBefore())+"\"   修改为    \""+r.getContentNew()+"\" \n";	
  			}
  			
  			MultipartFile[] files = null;
  	        //接下来发起流程（修改职员信息走的是自定义申请的流程）--------------------------------------------------
  	        customService.StartProcessCustomForPerson( request,personInfo.getFullName(),content,"",files,"2");
    }
    

    //rosterLog 花名册日志 ，修改了哪个字段，翻译字段名称
    protected String convertRosterLog(RosterLog rosterLog) {
    	
    	String columnName = "";
    	
    	if(rosterLog.getUpdateColumn().equals("username")){
    		columnName ="用户名";		
    	}
    	else if(rosterLog.getUpdateColumn().equals("stopFlag")){
    		columnName ="启用状态";		
    	}
    	else if(rosterLog.getUpdateColumn().equals("secret")){
    		columnName ="通讯录权限";		
    	}
    	else if(rosterLog.getUpdateColumn().equals("fullName")){
    		columnName ="姓名(fullName)";		
    	}
    	else if(rosterLog.getUpdateColumn().equals("realName")){
    		columnName ="真实姓名(realName)";		
    	}

    	else if(rosterLog.getUpdateColumn().equals("nameBefore")){
    		columnName ="曾用名";		
    	}
    	else if(rosterLog.getUpdateColumn().equals("email")){
    		columnName ="邮箱";		
    	}
    	else if(rosterLog.getUpdateColumn().equals("cellphone")){
    		columnName ="备用联系电话";		
    	}
    	else if(rosterLog.getUpdateColumn().equals("telephone")){
    		columnName ="联系电话";		
    	}
    	else if(rosterLog.getUpdateColumn().equals("employeeNo")){
    		columnName ="工号";		
    	}
    	else if(rosterLog.getUpdateColumn().equals("companyName")){
    		columnName ="公司";		
    	}
    	else if(rosterLog.getUpdateColumn().equals("departmentName")){
    		columnName ="部门";		
    	}
    	else if(rosterLog.getUpdateColumn().equals("positionName")){
    		columnName ="职位";		
    	}
    	else if(rosterLog.getUpdateColumn().equals("gender")){
    		columnName ="性别";		
    	}
    	else if(rosterLog.getUpdateColumn().equals("address")){
    		columnName ="现住址";		
    	}
    	else if(rosterLog.getUpdateColumn().equals("fax")){
    		columnName ="紧急联系人及电话";		
    	}
    	else if(rosterLog.getUpdateColumn().equals("wxNo")){
    		columnName ="微信号";		
    	}
    	else if(rosterLog.getUpdateColumn().equals("qq")){
    		columnName ="QQ";		
    	}
    	else if(rosterLog.getUpdateColumn().equals("level")){
    		columnName ="级别";		
    	}
    	else if(rosterLog.getUpdateColumn().equals("nativePlace")){
    		columnName ="籍贯";		
    	}
    	else if(rosterLog.getUpdateColumn().equals("registeredResidence")){
    		columnName ="户口所在地";		
    	}
    	else if(rosterLog.getUpdateColumn().equals("householdRegisterType")){
    		columnName ="户籍类型";		
    	}
    	else if(rosterLog.getUpdateColumn().equals("politicalOutlook")){
    		columnName ="政治面貌";		
    	}
    	else if(rosterLog.getUpdateColumn().equals("nation")){
    		columnName ="民族";		
    	}
    	else if(rosterLog.getUpdateColumn().equals("major")){
    		columnName ="专业";		
    	}
    	else if(rosterLog.getUpdateColumn().equals("title")){
    		columnName ="职称";		
    	}
    	else if(rosterLog.getUpdateColumn().equals("academicDegree")){
    		columnName ="学位";		
    	}
    	else if(rosterLog.getUpdateColumn().equals("skillSpecialty")){
    		columnName ="技能特长";		
    	}
    	else if(rosterLog.getUpdateColumn().equals("laborType")){
    		columnName ="用工类型";		
    	}
    	else if(rosterLog.getUpdateColumn().equals("entryMode")){
    		columnName ="进入方式";		
    	}
    	else if(rosterLog.getUpdateColumn().equals("education")){
    		columnName ="学历";		
    	}
    	else if(rosterLog.getUpdateColumn().equals("marriage")){
    		columnName ="婚否";		
    	}
    	else if(rosterLog.getUpdateColumn().equals("fertilityCondition")){
    		columnName ="生育情况";		
    	}
    	else if(rosterLog.getUpdateColumn().equals("entryTime")){
    		columnName ="入职时间";		
    	}
    	else if(rosterLog.getUpdateColumn().equals("contractExpirationTime")){
    		columnName ="合同到期时间";		
    	}
    	else if(rosterLog.getUpdateColumn().equals("contractDeadline")){
    		columnName ="合同有效期";		
    	}
    	
    	else if(rosterLog.getUpdateColumn().equals("contractCompany")){
    		columnName ="合同单位";		
    	}
    	else if(rosterLog.getUpdateColumn().equals("insurance")){
    		columnName ="保险情况";		
    	}else if(rosterLog.getUpdateColumn().equals("document")){
    		columnName ="资料情况";		
    	}else if(rosterLog.getUpdateColumn().equals("identityID")){
    		columnName ="身份证号";		
    	}else if(rosterLog.getUpdateColumn().equals("remark")){
    		columnName ="备注";		
    	}else if(rosterLog.getUpdateColumn().equals("family_1")){
    		columnName ="家庭成员一";		
    	}else if(rosterLog.getUpdateColumn().equals("family_2")){
    		columnName ="家庭成员二";		
    	}else if(rosterLog.getUpdateColumn().equals("educational_experience_1")){
    		columnName ="教育经历一";		
    	}else if(rosterLog.getUpdateColumn().equals("educational_experience_2")){
    		columnName ="教育经历二";		
    	}else if(rosterLog.getUpdateColumn().equals("educational_experience_3")){
    		columnName ="教育经历三";		
    	}else if(rosterLog.getUpdateColumn().equals("work_experience_1")){
    		columnName ="工作经历一";		
    	}else if(rosterLog.getUpdateColumn().equals("work_experience_2")){
    		columnName ="工作经历二";		
    	}else if(rosterLog.getUpdateColumn().toLowerCase().equals("quitflag")){
    		columnName ="在职状态";		
    	}else if(rosterLog.getUpdateColumn().toLowerCase().equals("resumeTime")){
    		columnName ="复职日期";		
    	}
    	
    	return columnName;
    }

    //rosterLog 花名册日志 ，翻译性别名称
    protected  String convertGender(String fName,String v) throws ParseException {
    	
    	Map<String, String> map = new HashMap<String, String>();
    	
    	if (fName.equals("gender")){
    		if(v.equals("1")) v = "男";
    		else v ="女";
    	}else if (fName.equals("marriage")){
    		if(v.equals("1")) v = "已婚";
    		else if(v.equals("3")) v = "离异";
    		else v ="未婚";
    	}else if (fName.equals("fertilityCondition")){
    		if(v.equals("1")) v = "已育";
    		else v ="未育";
    	}else if (fName.toLowerCase().equals("quitflag")){
    		if(v.equals("0")) v = "在职";
    		else v ="离职";
    	}else if (fName.equals("stopFlag")){
    		if(v.equals("active")) v = "启用";
    		else v ="停用";
    	}else if (fName.equals("secret")){
    		if(v.equals("0")) v = "公开";
    		else if(v.equals("1")) v = "内部";
    		else v ="保密";
    	}else if (fName.equals("nation")){ 						//民族
    		 v = dictConnector.findDictNameByValue("nation",v).equals("")?"无":dictConnector.findDictNameByValue("nation",v);
    	}else if (fName.equals("householdRegisterType")){ 		//户籍类型
   		 v = dictConnector.findDictNameByValue("householdRegisterType",v).equals("")?"无":dictConnector.findDictNameByValue("householdRegisterType",v);
	   	}else if (fName.equals("politicalOutlook")){			//政治面貌
			 v = dictConnector.findDictNameByValue("politicalOutlook",v).equals("")?"无":dictConnector.findDictNameByValue("politicalOutlook",v);
		}else if (fName.equals("academicDegree")){  			 //学位
			 v = dictConnector.findDictNameByValue("academicDegree",v).equals("")?"无":dictConnector.findDictNameByValue("academicDegree",v);
		}else if (fName.equals("laborType")){					//用工类型
			 v = dictConnector.findDictNameByValue("laborType",v).equals("")?"无":dictConnector.findDictNameByValue("laborType",v);
		}else if (fName.equals("entryMode")){					//进入方式
			 v = dictConnector.findDictNameByValue("entryMode",v).equals("")?"无":dictConnector.findDictNameByValue("entryMode",v);
		}else if (fName.equals("education")){					//学历
			 v = dictConnector.findDictNameByValue("education",v).equals("")?"无":dictConnector.findDictNameByValue("education",v);
		}else if (fName.equals("level")){ 						
			 v =	v.contains("级-")?v:(v.replace("-","级-"));
	 	}else if (fName.equals("entryTime")){ 
	 		if (!v.equals("")&&!v.equals("无")&&v.contains("CST")){
			 SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);  
			 Date d = sdf.parse(v);  
			 v = new SimpleDateFormat("yyyy-MM-dd").format(d); 
	 		}
	 		if (!v.equals("")&&!v.equals("无")&&!v.contains("CST")){
				  v = v.substring(0, 10);  
		 		}
		}else if (fName.equals("contractExpirationTime")){ 
	 		if (!v.equals("")&&!v.equals("无")&&v.contains("CST")){
			 SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);  
			 Date d = sdf.parse(v);  
			 v = new SimpleDateFormat("yyyy-MM-dd").format(d); 
	 		}
	 		if (!v.equals("")&&!v.equals("无")&&!v.contains("CST")){
	 			v = v.substring(0, 10); 
		 		} 
		}

    	return v;    	
    }
    
    
    /**
     * 添加职员.
     * @param partyEntityPostId 岗位ID（主要是岗位关系）
     * @param partyEntityId 上级PartyEntity的id（主要是组织关系）
     **/
    @Transactional(readOnly=false)
    public void insertPersonInfo(String applyCode,PersonInfo personInfo, int partyLevel, Long partyEntityId,Long postPartyEntityId,Long accountId) {
    	
    	//工号修改
    	Integer numberInteger=Integer.valueOf(personInfo.getEmployeeNoNum()==null?"0":personInfo.getEmployeeNoNum());
    	if (numberInteger>0){
	    	PersonWorkNumber personWrokNumber=personWorkNumberManager.findUniqueBy("numberNo", numberInteger);//.findUniqueBy(hsqlString,numberInteger);
			if(personWrokNumber!=null){
				if(personWrokNumber.getIsUse().equals("0")){
					personWrokNumber.setIsUse("1");
					personWorkNumberManager.save(personWrokNumber);
				}
			}
    	}
    	//Long accountId = Long.parseLong(currentUserHolder.getUserId());
    	String tenantId = tenantHolder.getTenantId();
        //1：流程审核同意   0：流程审核不同意 20180515 cz 新录入数据 初始化为不同意，待审核通过再更新为1
        //personInfo.setIsApproval("0");
        
    	//当前id的一个实体
        PartyEntity parent = partyEntityManager.get(partyEntityId);
        
        personInfoManager.save(personInfo);

        // 修改PartyId字段  目前 Party_Enity、account_info、person_info 三表主键保持一致
        personInfo.setPartyId(personInfo.getId());
        personInfoManager.update(personInfo);


        // 处理party_entity表
        insertPartyEntity(personInfo, partyLevel, partyEntityId, tenantId, accountId, parent,postPartyEntityId);

        //初始化密码，默认123456，add by lilei at 2018-05-24
        String password="123456";
        // 处理account_info表
        insertAccountInfo(personInfo, password, tenantId, accountId);
        // 设置用户权限
        AccountInfo accountInfo = accountInfoManager.get(personInfo.getId());
        configUserRole(this.convertUserDto(accountInfo, personInfo));
        if(applyCode.equals("0")){//没有受理单编号，说明没有走流程
        	 String curentUserName = currentUserHolder.getName();
        	  //记录日志
                RosterLog rosterLog = new RosterLog ();
        	      rosterLog.setCode(applyCode);     					/** 受理单编号. */
        	      rosterLog.setOperationID(curentUserName); 		/** 操作人员id. */
        	      rosterLog.setContentBefore("添加新职员");  	/** 修改之前的内容. */
        	      rosterLog.setContentNew("添加新职员");			/** 修改后的新内容. */
        	      rosterLog.setUpdateColumn("添加新职员"); 		/**被修改的字段名. */
        	      rosterLog.setUpdateColumnName("添加新职员");
        	      rosterLog.setIsapproval("0");//1：流程审核还未通过   0：流程审核通过    20180515 cz 新录入数据 初始化为未通过，待审核通过再更新为0
        	      rosterLog.setUpdateTime(new Date());  	  	/** 修改时间. */
        	      rosterLog.setEmployeeNo(personInfo.getId().toString());				/**被修改的员工编号. */
        	      rosterLogManager.save(rosterLog);
        }
    }  
    
    /**
     * 编辑职员.
     * @throws IllegalAccessException 
     * @throws IllegalArgumentException 
     * @throws ParseException 
     */
    @Transactional(readOnly=false)
    public void updatePersonInfo(String applyCode,PersonInfo personInfo, String password, String gestureSwitch,Long accountId) throws IllegalArgumentException, IllegalAccessException, ParseException {
    	//更新员工编号
    	//将工号的部门和数字部分拼接起来 存入数据库
        String employeeNo = personInfo.getEmployeeNoDepart()+personInfo.getEmployeeNoNum();
        personInfo.setEmployeeNo(employeeNo);
	
        String tenantId = tenantHolder.getTenantId();
        
        //String userIdString=currentUserHolder.getUserId();
        //Long accountId = Long.valueOf(userIdString);
        
        PersonInfo dest = null;
        Long id = personInfo.getId();

        if (id != null) {
            dest = personInfoManager.get(id);

            if (personInfo.getStopFlag() == null) {
                personInfo.setStopFlag("disabled");
            }

            if (personInfo.getSecret() == null) {
                personInfo.setSecret("0");
            }
        }
       
        String partyEntityId = personInfo.getDepartmentCode();
        PartyDTO partyDTO = partyConnector.findCompanyById(partyEntityId);

        // 处理party_entity表
        updatePartyEntity(personInfo, tenantId, accountId);
        // 处理account_info表
        updateAccountInfo(personInfo, password, tenantId, accountId, gestureSwitch);
        // 编辑用户权限
        AccountInfo accountInfo = accountInfoManager.get(personInfo.getId());
        configUserUpdateRole(this.convertUserDto(accountInfo, personInfo));

        //当前id的一个实体
        PartyEntity parent = partyEntityManager.get(Long.parseLong(partyEntityId));
        //当前id的父点
        PartyStruct partyStructParent = partyStructManager.findUniqueBy("childEntity.id", Long.parseLong(partyEntityId));
        //查找当前部门id大区
        PartyDTO partyDTOArea = partyConnector.findAreaById(partyEntityId);
        //除分公司外添加职员
        if (partyDTOArea == null) {
            personInfo.setDepartmentCode(Long.toString(parent.getId()));
            personInfo.setDepartmentName(parent.getName());

            PartyDTO partyDTOShow = partyConnector.findCompanyById(Long.toString(parent.getId()));
            personInfo.setCompanyCode(partyDTOShow.getId());
            personInfo.setCompanyName(partyDTOShow.getName());
        } else {
            //分公司添加职员
            PartyEntity parentArea = partyEntityManager.findUniqueBy("id", Long.parseLong(partyDTOArea.getId()));
            PartyDTO partyDTOShow = partyConnector.findCompanyById(Long.toString(parentArea.getId()));
            personInfo.setCompanyCode(partyDTOShow.getId());
            personInfo.setCompanyName(partyDTOShow.getName());
            String parentId = Long.toString(partyStructParent.getParentEntity().getId());
            if (parentId.equals(partyDTOArea.getId())) {
                //上个点就是大区
                personInfo.setDepartmentCode(Long.toString(parent.getId()));
                personInfo.setDepartmentName(parent.getName());
            } else {
                //当前id的一个实体
                PartyEntity parentEntity = partyEntityManager.get(Long.parseLong(parentId));
                //上个点不是大区是分公司
                personInfo.setDepartmentCode(Long.toString(parentEntity.getId()));
                personInfo.setDepartmentName(parentEntity.getName());
            }
        }
     
        if(applyCode.equals("0")){
		    //取当前操作人的名字
	        String sqlForFullName = "SELECT FULL_NAME FROM person_info  WHERE id ="+currentUserHolder.getUserId();
	        List<Map<String, Object>> listForFullName = jdbcTemplate.queryForList(sqlForFullName);
	        String fullName = listForFullName.get(0).get("FULL_NAME").toString();
		
			  //对比两个类的属性，找出 修改了哪些内容
	        dest = null;
	        dest = personInfoManager.get(personInfo.getId());
	        Map<String, String> result = new HashMap<String, String>();
		      Field[] fs = personInfo.getClass().getDeclaredFields();
		      for (Field f : fs) {
		          f.setAccessible(true);
		          Object v1 = f.get(personInfo);
		          Object v2 = f.get(dest);
		          if( ! equals(v1, v2)&& !f.getName().equals("resumeTime")&& !f.getName().equals("priority")&& !f.getName().equals("quitTime")&& !f.getName().equals("delFlag")&& !f.getName().equals("serialVersionUID")&& !f.getName().equals("addTime")&& !f.getName().equals("postId")&& !f.getName().equals("postName")&& !f.getName().equals("departmentCode")&& !f.getName().equals("companyCode")&& !f.getName().equals("partyId")&& !f.getName().equals("isApproval")&& !f.getName().equals("employeeNoDepart")&& !f.getName().equals("positionCode")&& !f.getName().equals("contractCompanyID")){
		              result.put(f.getName(), String.valueOf(equals(v1, v2)));
		              if(
	            		  (v1==null||(v1!=null&&v1.equals(""))) 
	            		  &&
	            		  (v2==null||(v2!=null&&v2.equals("")))
            		  )
	            		  {
	            	  			continue;
	            		  }
		              
		              if(v1!=null){
		            	  if(v1.equals("")){
		            		  v1="无";
		            	  }
		              }else {
		            	  v1 ="无";
		              }
		              
		              if(v2!=null){
		            	  if(v2.equals("")){
		            		  v2="无";
		            	  }
		              }else {
		            	  v2 ="无";
		              }
		             
//		              if(f.getName().equals("gender")||f.getName().equals("fertilityCondition")||f.getName().equals("marriage")||f.getName().equals("quitFlag")){
//		            	  
//		            	Map<String,String> map =  convertGender(f.getName(),v1.toString(),v2.toString());
//		            	
//		            	v1 = map.get("v1");
//		            	
//		            	v2 = map.get("v2");
//		              }
		              
		              RosterLog rosterLog = new RosterLog ();
				      
				      rosterLog.setCode(applyCode);     					/** 受理单编号. */
				      rosterLog.setOperationID(fullName); 		/** 操作人员id. */
				      rosterLog.setContentBefore(convertGender(f.getName(),v2.toString()));  	/** 修改之前的内容. */
				      rosterLog.setContentNew(convertGender(f.getName(),v1.toString()));			/** 修改后的新内容. */
				      rosterLog.setUpdateColumn(f.getName()); 		/**被修改的字段名. */
				      rosterLog.setUpdateColumnName(convertRosterLog(rosterLog));
				      rosterLog.setIsapproval("0");//1：流程审核还未通过   0：流程审核通过    20180515 cz 新录入数据 初始化为未通过，待审核通过再更新为0
				      
				      rosterLog.setUpdateTime(new Date());  	  	/** 修改时间. */
				      rosterLog.setEmployeeNo(personInfo.getId().toString());			/**被修改的员工编号. */
		          
				      rosterLogManager.save(rosterLog);
		          }
		      }
        }
                
        if (id != null) {
            beanMapper.copy(personInfo, dest);
            //离职 add by lilei at 2018-05-30
            if(personInfo.getQuitFlag().equals("1"))
            	quitPersonInfo(personInfo.getId());
            else {
            	//复职 add by lilei at 2018-06-08
				PersonInfo oldPersonInfo=personInfoManager.findUniqueBy("id", personInfo.getId());
				if(oldPersonInfo.getQuitFlag().equals("1")
						&&personInfo.getQuitFlag().equals("0")){
					password="123456";
					gestureSwitch="123456";
					resumePersonInfo(personInfo.getId(), personInfo, password, gestureSwitch);
				}
				//ckx
				dest.setLeaveDate(null);
				dest.setQuitTime(null);
			}
        }
        personInfoManager.save(dest);
          
    }
    
    /**
     * 调整岗位走流程
     * add by lilei at 2018-06-05
     * **/
    @Transactional(readOnly=false)
    public void changePositionForAudit(HttpServletRequest request,String applyCode,Long selfPartyEntityId,Long iptCurrentPost,Long postId) throws IOException, Exception{
    	String oldPosition=getPositionCompany(iptCurrentPost);
    	String newPostion=getPositionCompany(postId);
    	RosterLog rosterLog = new RosterLog ();
		rosterLog.setCode(applyCode);     								//受理单编号.
		rosterLog.setOperationID(currentUserHolder.getName()); 		   	//操作人员id. 
		rosterLog.setContentBefore(oldPosition);  						//修改之前的内容. 
		rosterLog.setContentNew(newPostion);							//修改后的新内容. 
		rosterLog.setUpdateColumn(""); 									//被修改的字段名. 
		rosterLog.setUpdateColumnName("岗位");
		rosterLog.setIsapproval("1");									//1：流程审核还未通过   0：流程审核通过 
		rosterLog.setUpdateTime(new Date());  	  						//修改时间. 
		rosterLog.setEmployeeNo(selfPartyEntityId.toString());			//被修改的员工编号. */
	    rosterLogManager.save(rosterLog);
	      
     	//将修改记录存入 UpdatePerson
 		UpdatePerson updatePerson=new UpdatePerson();
 		Map<String,Object> jsonMap=new HashMap<String, Object>();
 		jsonMap.put("selfPartyEntityId", selfPartyEntityId);
 		jsonMap.put("OldpartyEntityId", iptCurrentPost);
 		jsonMap.put("changePartyEntityId", postId);
 		updatePerson.setJsonContent(jsonMapper.toJson(jsonMap));
     	updatePerson.setApplyCode(applyCode);
     	updatePerson.setEmployeeNo(selfPartyEntityId.toString());		//记录被调整人的id
     	updatePerson.setIsApproval("2");								//1：流程审核同意   0：流程审核不同意    2:审核中 
     	updatePerson.setTypeID("changePost");
     	updatePerson.setUpdateParameters("");
     	updatePersonManager.save(updatePerson);	
     	
     	PartyEntity partyEntity =partyEntityManager.findUniqueBy("id", selfPartyEntityId);
     	String personUserName =partyEntity.getName();					//被调整人的姓名

	    //接下来发起流程（修改职员信息走的是自定义申请的流程）--------------------------------------------------
     	StringBuffer contentBuffer=new StringBuffer();
     	contentBuffer.append(String.format("职员姓名：%s\n",partyEntity.getName()));
     	contentBuffer.append(String.format("岗位调整：由“%s”调整为“%s”",oldPosition,newPostion));
	    MultipartFile[] files = null;
	    customService.StartProcessCustomForPerson(request,personUserName,contentBuffer.toString(),"",files,"3");
    }
    
    /**
     * 调整岗位，重新调整
     * add by lilei at 2018-06-05
     * **/
    @Transactional(readOnly=false)
    public void changePositionEditForAudit(String applyCode,Long selfPartyEntityId,Long iptCurrentPost,Long postId) throws IOException, Exception{
    	String oldPosition=getPositionCompany(iptCurrentPost);
    	String newPostion=getPositionCompany(postId);
    	
    	String hqlString="from RosterLog where isapproval='1' and code=?";
    	RosterLog rosterLog =rosterLogManager.findUnique(hqlString,applyCode);
    	if(rosterLog!=null){
    		rosterLog.setContentBefore(oldPosition);  			//修改之前的内容.
  	      	rosterLog.setContentNew(newPostion);				//修改后的新内容.
  	      	rosterLogManager.save(rosterLog);
    	}
	      
		PartyEntity partyEntity =partyEntityManager.findUniqueBy("id", selfPartyEntityId);
    	StringBuffer contentBuffer=new StringBuffer();
    	contentBuffer.append(String.format("职员姓名：%s\n",partyEntity.getName()));
     	contentBuffer.append(String.format("岗位调整：由“%s”调整为“%s”",oldPosition,newPostion));
     	CustomEntity customEntity=customManager.findUniqueBy("applyCode", applyCode);
     	if(customEntity!=null){
     		customEntity.setApplyContent(contentBuffer.toString());
     		customManager.save(customEntity);
     	}
     	
		//将修改记录存入 UpdatePerson
		hqlString="from UpdatePerson where isApproval='2' and applyCode=?";
		UpdatePerson updatePerson=updatePersonManager.findUnique(hqlString, applyCode);
		if(updatePerson!=null){
			Map<String,Object> jsonMap=new HashMap<String, Object>();
			jsonMap.put("selfPartyEntityId", selfPartyEntityId);
			jsonMap.put("OldpartyEntityId", iptCurrentPost);
			jsonMap.put("changePartyEntityId", postId);
			updatePerson.setJsonContent(jsonMapper.toJson(jsonMap));
		   	updatePersonManager.save(updatePerson);
		}
    }
    
    /**
     * 根据岗位
     * add by lilei at 2018-06-05
     * **/
     public String getPositionCompany(Long partyEntityId){
    	 	
    	 	PartyEntity partyEntity=partyEntityManager.findUniqueBy("id", partyEntityId);
    	 	//岗位
			String positionName=partyEntity.getName();
			
			//部门
			String hql="from PartyStruct where partyStructType.id=1 and childEntity=?";
			PartyStruct deparmentPartyStruct=partyStructManager.findUnique(hql, partyEntity);
			String deparmentName=deparmentPartyStruct.getParentEntity().getName();
			
			//公司
			hql="from PartyStruct where partyStructType.id=1 and childEntity=?";
			PartyStruct companyPartyStruct=partyStructManager.findUnique(hql, deparmentPartyStruct.getParentEntity());
			String companyName=companyPartyStruct.getParentEntity().getName();
			
			return companyName+"-"+deparmentName+"-"+positionName;
			//map.put("position", companyName+"-"+deparmentName+"-"+positionName);
     }

    /**
     * 启用/停用职员.
     */
    public void updatePersonInfoStopFlag(PersonInfo personInfo) {

        PersonInfo dest = null;
        Long id = personInfo.getId();

        if (id != null) {
            dest = personInfoManager.get(id);

            beanMapper.copy(personInfo, dest);
        }
        personInfoManager.save(dest);

        // 处理account_info表
        updateAccountInfoStopFlag(personInfo);

    }

    /**
     * 通讯录设置.
     */
    public void updatePersonInfoSecret(PersonInfo personInfo) {

        PersonInfo dest = null;
        Long id = personInfo.getId();

        if (id != null) {
            dest = personInfoManager.get(id);

            beanMapper.copy(personInfo, dest);
        }
        personInfoManager.save(dest);
    }

    /**
     * 删除.
     */
    public void deletePersonInfo(List<Long> selectedItem) {

        String tenantId = tenantHolder.getTenantId();

        List<PersonInfo> personInfos = personInfoManager.findByIds(selectedItem);

        for (PersonInfo personInfo : personInfos) {

            personInfo.setDelFlag(PersonInfoConstants.DELETE_FLAG_YES);
            personInfoManager.update(personInfo);

            // 处理party_entity表
            PartyEntity partyEntity = partyEntityManager.get(personInfo.getId());
            partyEntity.setDelFlag(PersonInfoConstants.DELETE_FLAG_YES);
            partyEntityManager.update(partyEntity);

            // 处理account_info表
            AccountInfo accountInfo = accountInfoManager.get(personInfo.getId());
            accountInfo.setDelFlag(PersonInfoConstants.DELETE_FLAG_YES);
            accountInfoManager.update(accountInfo);

            UserDTO userDto = new UserDTO();
            userDto.setId(Long.toString(accountInfo.getId()));
            userDto.setUsername(accountInfo.getUsername());
            userDto.setRef(accountInfo.getCode());
            userDto.setUserRepoRef(tenantId);
            userCache.removeUser(userDto);

            // 处理用户权限
            Long entityId = this.getAuthUserStatusId(userDto.getId(), tenantId);
            if (entityId != null) {
                jdbcTemplate.update(removeUserRoleSql, entityId);
                jdbcTemplate.update(removeUserSql, entityId);
            }

            // 用户权限删除
            configUserDeleteRole(this.convertUserDto(accountInfo, personInfo));
        }
    }

    private Long getAuthUserStatusId(String ref, String tenantId) {
        try {
            return jdbcTemplate.queryForObject(selectUserSql, Long.class, ref,
                    tenantId);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            return null;
        }
    }

    /**
     * 离职.
     */
    public void quitPersonInfo(Long id) {

        String tenantId = tenantHolder.getTenantId();

        PersonInfo personInfo = personInfoManager.get(id);
        //禁止登陆系统时间
        Date quitTime = personInfo.getQuitTime();
        boolean boo = DateUtil.comparDate(quitTime, new Date());
        //DateUtil.compareDate(quitTime, new Date());
        // TODO 此处应该判断是否允许离职。例如是否存在未处理的流程等
        //判断是否早于当天
        if(boo){
        	personInfo.setDelFlag(PersonInfoConstants.DELETE_FLAG_YES); //ckx
            personInfo.setQuitFlag(PersonInfoConstants.QUIT_FLAG_YES);    // 离职人员相当于一种特殊的删除
            //personInfo.setQuitTime(new Date()); //ckx  
            personInfoManager.update(personInfo);
            // 处理party_entity表
            PartyEntity partyEntity = partyEntityManager.get(personInfo.getId());
            partyEntity.setDelFlag(PersonInfoConstants.DELETE_FLAG_YES); //ckx
            partyEntityManager.update(partyEntity);
            
            // 处理account_info表
            AccountInfo accountInfo = accountInfoManager.get(personInfo.getId());
            accountInfo.setDelFlag(PersonInfoConstants.DELETE_FLAG_YES);
            accountInfoManager.update(accountInfo);
            
            UserDTO userDto = new UserDTO();
            userDto.setId(Long.toString(accountInfo.getId()));
            userDto.setUsername(accountInfo.getUsername());
            userDto.setRef(accountInfo.getCode());
            userDto.setUserRepoRef(tenantId);
            userCache.removeUser(userDto);
            userPublisher.notifyUserRemoved(this.convertUserDto(accountInfo, personInfo)); //ckx ？
        }else{
        	personInfo.setQuitFlag(PersonInfoConstants.QUIT_FLAG_NO);
        }
        

    }

    /**
     * 复职.
     */
    public void resumePersonInfo(Long id, PersonInfo personInfo, String password, String gestureSwitch) {

        String tenantId = tenantHolder.getTenantId();
        Long accountId = Long.parseLong(currentUserHolder.getUserId());

        PersonInfo vo = personInfoManager.get(id);

        // TODO 此处应该判断是否允许离职。例如是否存在未处理的流程等
        vo.setDelFlag(PersonInfoConstants.DELETE_FLAG_NO);
        vo.setQuitFlag(PersonInfoConstants.QUIT_FLAG_NO);
        if (vo.getStopFlag() == null) {
            vo.setStopFlag("disabled");
        }
        vo.setDepartmentCode(personInfo.getDepartmentCode());
        vo.setDepartmentName(personInfo.getDepartmentName());
        vo.setEmployeeNo(personInfo.getEmployeeNo());
        vo.setPositionCode(personInfo.getPositionCode());
        vo.setJobStatus(personInfo.getJobStatus());
        vo.setEmail(personInfo.getEmail());
        vo.setCellphone(personInfo.getCellphone());
        vo.setAddress(personInfo.getAddress());
        vo.setPriority(personInfo.getPriority());
        vo.setUsername(personInfo.getUsername());//保证管理员处显示职员的登录名与实际登录名一致

        vo.setResumeTime(new Date());     // 复职日期
        personInfoManager.update(vo);

        // 处理party_entity表
        updatePartyEntity(personInfo, tenantId, accountId);

        // 处理account_info表
        updateAccountInfo(personInfo, password, tenantId, accountId, gestureSwitch);
        AccountInfo accountInfo = accountInfoManager.get(personInfo.getId());
        configUserUpdateRole(this.convertUserDto(accountInfo, personInfo));

    }

    private void configUserRole(UserDTO userDto) {
        String insertSql = "INSERT INTO AUTH_USER_STATUS(ID,USERNAME,REF,STATUS,PASSWORD,USER_REPO_REF,TENANT_ID,ADD_USER_ID,DEL_FLAG) VALUES(?,?,?,1,'',?,?,?,?)";
        String insertRoleUserSql = "INSERT INTO auth_user_role(USER_STATUS_ID,ROLE_ID,DEL_FLAG) VALUES(?,?,?)";
        String tenantId = userDto.getUserRepoRef();

        Long id = idGenerator.generateId();
        jdbcTemplate.update(insertSql, id,
                userDto.getUsername(), userDto.getId(), tenantId, tenantId, userDto.getAddUserId(), PersonInfoConstants.DELETE_FLAG_NO);

        String adminUserId = Long.toString(PartyConstants.ADMIN_USER_ID);
        if (adminUserId.equals(userDto.getAddUserId())) {
            // 新增用户,系统自动挂角色(系统管理员)
            jdbcTemplate.update(insertRoleUserSql, id, 2, PersonInfoConstants.DELETE_FLAG_NO);
        } else {
            // 新增用户,系统自动挂角色(普通用户)
            jdbcTemplate.update(insertRoleUserSql, id, 8, PersonInfoConstants.DELETE_FLAG_NO);
        }
        // authCache.evictUser(userDto.getId());
    }

    private void configUserUpdateRole(UserDTO userDto) {
        String updateSql = "UPDATE AUTH_USER_STATUS SET USERNAME=? , DEL_FLAG='0' WHERE REF=? AND TENANT_ID=?";
        String updateUserRoleSql = "UPDATE AUTH_USER_ROLE SET DEL_FLAG = '0' WHERE USER_STATUS_ID=?";
        String selectUserSql = "SELECT ID FROM AUTH_USER_STATUS WHERE REF=? AND TENANT_ID=?";
        JsonMapper jsonMapper = new JsonMapper();
        String tenantId = userDto.getUserRepoRef();

        tenantId = userDto.getUserRepoRef();
        Long entityId = jdbcTemplate.queryForObject(selectUserSql, Long.class, userDto.getId(),
                tenantId);
        if (entityId == null) {
            return;
        }
        jdbcTemplate.update(updateSql, userDto.getUsername(),
                userDto.getId(), tenantId);

        jdbcTemplate.update(updateUserRoleSql, entityId);

        if (userDto != null) {
            String hql = "from UserStatus where username=? and tenantId=?";
            UserStatus userStatus = userStatusManager.findUnique(hql,
                    userDto.getUsername(), tenantId);
            if (userStatus != null) {
                authCache.evictUserStatus(userStatus);
            } else {
                authCache.evictUser(userDto.getId());
            }
        }
    }

    private void configUserDeleteRole(UserDTO userDto) {
        userCache.removeUser(userDto);
        for (TenantDTO tenantDto : tenantConnector.findAll()) {
            UserAuthDTO userAuthDto = userAuthConnector.findByUsername(
                    userDto.getUsername(), tenantDto.getId());
            userAuthCache.removeUserAuth(userAuthDto);
        }
    }

    /**
     * 处理账号相关的表
     *
     * @param personInfo
     * @param password
     * @param tenantId
     * @param accountId
     */
    private void insertAccountInfo(PersonInfo personInfo, String password,
                                   String tenantId, Long accountId) {

        AccountInfo accountInfo = new AccountInfo();

        accountInfo.setId(personInfo.getId());
        accountInfo.setUsername(personInfo.getUsername());
        accountInfo.setCode(Long.toString(personInfo.getId()));
        if (personInfo.getStopFlag() == null) {
            accountInfo.setStatus("disabled");
        } else {
            accountInfo.setStatus("active");
        }

        accountInfo.setCreateTime(new Date());
        accountInfo.setTenantId(tenantId);

        // zyl 2017-07-11   增加如下代码
        accountInfo.setPasswordRequired("required");
        accountInfo.setLocked("unlocked");
        accountInfo.setNickName(personInfo.getFullName());
        accountInfo.setDisplayName(personInfo.getFullName());
        accountInfo.setAddUserId(accountId);
        accountInfo.setType("employee");
        accountInfo.setDelFlag(PersonInfoConstants.DELETE_FLAG_NO);
        accountInfoManager.save(accountInfo, null);

        if (password != null) {
            // String hql = "from AccountCredential where accountInfo=? and catalog='default'";
            // AccountCredential accountCredential = accountCredentialManager.findUnique(hql, accountInfo);

            //if (accountCredential == null) {
            AccountCredential accountCredential = new AccountCredential();
            accountCredential.setAccountInfo(accountInfo);
            accountCredential.setType("normal");
            accountCredential.setCatalog("default");

            // zyl 2017-07-11
            accountCredential.setTenantId(accountInfo.getTenantId());
            accountCredential.setStatus(accountInfo.getStatus());
            accountCredential.setCouldModify("yes");
            accountCredential.setExpireStatus("normal");

            accountCredential.setFailedPasswordCount(0);
            // 计算失效日期
            Calendar calendar = Calendar.getInstance();
            Date date = new Date(System.currentTimeMillis());
            calendar.setTime(date);
            // calendar.add(Calendar.WEEK_OF_YEAR, -1);
            calendar.add(Calendar.YEAR, 20);
            date = calendar.getTime();
            // System.out.println(date);

            accountCredential.setExpireTime(date);
            accountCredential.setGestureSwitch("close");
            //}

            if (customPasswordEncoder != null) {
                accountCredential.setPassword(customPasswordEncoder.encode(password));
                accountCredential.setOperationPassword(accountCredential.getPassword());   // 操作密码
            } else {
                accountCredential.setPassword(password);
            }

            accountCredentialManager.save(accountCredential);
        }

        UserDTO userDto = new UserDTO();
        userDto.setId(Long.toString(accountInfo.getId()));
        userDto.setUsername(accountInfo.getUsername());
        userDto.setRef(accountInfo.getCode());
        userDto.setUserRepoRef(tenantId);
        userDto.setAddUserId(Long.toString(accountId));
        userCache.removeUser(userDto);

        // 通过消息处理 AUTH_USER_STATUS 表及人员和角色关系
        // userPublisher.notifyUserCreated(this.convertUserDto(accountInfo, personInfo));
//        userPublisher.authUserCreated(this.convertUserDto(accountInfo, personInfo));
        // configUserRole(this.convertUserDto(accountInfo, personInfo));
    }


    /**
     * 处理账号相关的表
     *
     * @param personInfo
     * @param password
     * @param tenantId
     * @param accountId
     */
    private void updateAccountInfo(PersonInfo personInfo, String password,
                                   String tenantId, Long accountId, String gestureSwitch) {

    	 //控制是否开启别名 :1开启 0关闭
        List<DictInfo> dictInfo_otherName = dictConnector.findDictInfoListByType("isOpenOtherName");

        AccountInfo dest = null;
        Long id = personInfo.getId();

        if (id != null) {
            dest = accountInfoManager.get(id);

            if (personInfo.getStopFlag() == null) {
                dest.setStatus("disabled");
            }
            dest.setUsername(personInfo.getUsername());
            dest.setNickName(personInfo.getFullName());
            
            //别名开启，更新accountInfo表时，姓名存入realname
            if(dictInfo_otherName.get(0).getValue().equals("1")){
            	dest.setRealName(personInfo.getRealName());
            }else{
            dest.setDisplayName(personInfo.getFullName());
            }
            dest.setDelFlag(PersonInfoConstants.DELETE_FLAG_NO);   // 复职时用到
        }

        accountInfoManager.save(dest);

        String hql = "from AccountCredential where accountInfo=? and catalog='default'";
        AccountCredential accountCredential = accountCredentialManager.findUnique(hql, dest);

        if (StringUtils.isNotBlank(password)) {   // 复职时用到，普通修改password为null

            // 计算失效日期
            Calendar calendar = Calendar.getInstance();
            Date date = new Date(System.currentTimeMillis());
            calendar.setTime(date);
            // calendar.add(Calendar.WEEK_OF_YEAR, -1);
            calendar.add(Calendar.YEAR, 20);
            date = calendar.getTime();
            // System.out.println(date);

            accountCredential.setExpireTime(date);


            if (customPasswordEncoder != null) {
                accountCredential.setPassword(customPasswordEncoder.encode(password));
            } else {
                accountCredential.setPassword(password);
            }

            accountCredentialManager.save(accountCredential);
        }

//        if (gestureSwitch == null) {
//        	gestureSwitch = "close";
//        }
        accountCredential.setGestureSwitch(gestureSwitch);
        accountCredentialManager.save(accountCredential);

        UserDTO userDto = new UserDTO();
        userDto.setId(Long.toString(dest.getId() == null ? -1 : dest.getId()));
        userDto.setUsername(dest.getUsername());
        userDto.setRef(dest.getCode());
        userDto.setUserRepoRef(tenantId);
        userDto.setAddUserId(accountId==null?"":accountId.toString());
        userCache.removeUser(userDto);

        // 通过消息处理 AUTH_USER_STATUS 表及人员和角色关系
        // userPublisher.notifyUserUpdated(this.convertUserDto(dest, personInfo));
        // userPublisher.authUserUpdate(this.convertUserDto(dest, personInfo));
    }

    /**
     * 停用启用账号
     *
     * @param personInfo
     */
    private void updateAccountInfoStopFlag(PersonInfo personInfo) {

        AccountInfo dest = null;
        Long id = personInfo.getId();

        if (id != null) {
            dest = accountInfoManager.get(id);
            dest.setStatus(personInfo.getStopFlag());
        }

        accountInfoManager.save(dest);

        // 通过消息处理 AUTH_USER_STATUS 表及人员和角色关系
        userPublisher.notifyUserUpdated(this.convertUserDto(dest, personInfo));
    }

    /**
     * 添加组织机构中类型为人员的
     *
     * @param personInfo
     * @param partyLevel
     * @param partyEntityId
     * @param tenantId
     * @param accountId
     * @param postPartyEntityId 岗位ID
     */
    private void insertPartyEntity(PersonInfo personInfo, int partyLevel,
                                   Long partyEntityId, String tenantId, Long accountId, PartyEntity parent,Long postPartyEntityId) {

        PartyEntity child = new PartyEntity();

        child.setId(personInfo.getId());
        child.setName(personInfo.getFullName());
        child.setTenantId(tenantId);
        child.setLevel(partyLevel + 1);
        child.setDelFlag(PersonInfoConstants.DELETE_FLAG_NO);
        child.setRef(Long.toString(personInfo.getId()));
        child.setPartyType(partyTypeManager.get((long) PartyConstants.TYPE_USER));
        child.setShortName(personInfo.getEmployeeNo());
        partyEntityManager.save(child, null);

        PartyStruct partyStruct = new PartyStruct();

        partyStruct.setPartTime(Integer.parseInt(personInfo.getJobStatus()==null?"0":personInfo.getJobStatus()));
        partyStruct.setPartyStructType(partyStructTypeManager.get(PartyConstants.PARTY_STRUCT_TYPE_ORG));
        partyStruct.setParentEntity(parent);
        partyStruct.setChildEntity(child);
        partyStruct.setTenantId(tenantId);
        partyStruct.setAddUserId(accountId);
        partyStruct.setPriority(personInfo.getPriority());
        partyStructManager.save(partyStruct);
        
        //添加岗位 add by lilei at 2018-05-24
        if(postPartyEntityId!=null && postPartyEntityId>0){
        	PartyEntity postPartyEntity=partyEntityManager.findUniqueBy("id", postPartyEntityId);
        	if(postPartyEntity!=null){
        		PartyStruct postPartyStruct = new PartyStruct();

        		postPartyStruct.setPartTime(Integer.parseInt(personInfo.getJobStatus()));
                postPartyStruct.setPartyStructType(partyStructTypeManager.get(PartyConstants.PARTY_STRUCT_TYPE_POSITION_USER));
                postPartyStruct.setParentEntity(postPartyEntity);
                postPartyStruct.setChildEntity(child);
                postPartyStruct.setTenantId(tenantId);
                postPartyStruct.setAddUserId(accountId);
                postPartyStruct.setPriority(personInfo.getPriority());
                partyStructManager.save(postPartyStruct);
        	}
        }
    }

    /**
     * 修改组织机构中类型为人员的
     *
     * @param personInfo
     * @param tenantId
     * @param accountId
     * @param postPartyEntityId
     */
    private void updatePartyEntity(PersonInfo personInfo, String tenantId, Long accountId) {



		//控制是否开启别名 :1开启 0关闭
		List<DictInfo> dictInfo_otherName = dictConnector.findDictInfoListByType("isOpenOtherName");


        PartyEntity dest = null;
        Long id = personInfo.getId();

        PartyEntity parent = null;
        if (id != null) {
            dest = partyEntityManager.get(id);

            parent = partyEntityManager.get(Long.parseLong(personInfo.getDepartmentCode()));

          //别名开启，更新accountInfo表时，姓名存入realname
            if(dictInfo_otherName.get(0).getValue().equals("1")){
            	dest.setRealName(personInfo.getRealName());
            }else {
            	dest.setName(personInfo.getFullName());
			}
            dest.setLevel(parent.getLevel() + 1);
            dest.setShortName(personInfo.getEmployeeNo());
            dest.setDelFlag(PersonInfoConstants.DELETE_FLAG_NO);   // 复职时用到
        }
        partyEntityManager.save(dest);


        String hql = "from PartyStruct where childEntity = ? and partyStructType = ?";

        PartyStructType partyStructType = partyStructTypeManager.get(PartyConstants.PARTY_STRUCT_TYPE_ORG);
        PartyStruct partyStruct = partyStructManager.findUnique(hql, dest, partyStructType);

        if (partyStruct != null) {
            partyStruct.setPartTime(Integer.parseInt(personInfo.getJobStatus()));
            partyStruct.setPriority(personInfo.getPriority());
            partyStruct.setParentEntity(parent);
            partyStructManager.save(partyStruct);
        }
    }

    private String getStringValue(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof String) {
            return (String) value;
        }

        return value.toString();
    }

    private UserDTO convertUserDto(AccountInfo accountInfo, PersonInfo personInfo) {

        // PersonInfo personInfo = personInfoManager.get(accountInfo.getId());

        UserDTO userDto = new UserDTO();
        userDto.setId(Long.toString(accountInfo.getId()));
        userDto.setUsername(accountInfo.getUsername());
        userDto.setDisplayName(accountInfo.getDisplayName());
        userDto.setNickName(accountInfo.getNickName());
        userDto.setUserRepoRef(accountInfo.getTenantId());
        userDto.setAddUserId(Long.toString(accountInfo.getAddUserId()));
        if (personInfo != null) {
            userDto.setEmail(personInfo.getEmail());
            userDto.setMobile(personInfo.getCellphone());
        }

        return userDto;
    }
    
    @Transactional(readOnly=false)
    public List<Map<String, Object>> importDataWrokNumber(List<Map<String, Object>> userList){
    	//物理删除特定人员
    	//delPersonAccount();
    	
    	List<Map<String, Object>>  failMap = new ArrayList<Map<String,Object>>() ;
    	
    	String hqlString="from PersonInfo where fullName=? and companyName=?";
    	//处理EXCEL的数据
    	for(Map<String, Object> map:userList){
    		String workNumber=map.get("0").toString().replaceAll("rm", "");//工号
    		String company=map.get("3").toString();
    		String userName=map.get("2").toString();
    		String fullName=map.get("1").toString();
    		String departmentName=map.get("4").toString();
    	
    		PersonInfo personInfo=null;
    		personInfo=personInfoManager.findUnique(hqlString, fullName,company);

    		if(personInfo!=null){
    			//处理人员的工号
    			String strWorkNumberPrefix=partyConnector.getWorkNumerPrefix(personInfo.getId().toString());
        		personInfo.setEmployeeNoDepart(strWorkNumberPrefix);
        		personInfo.setEmployeeNoNum(workNumber);
        		personInfo.setEmployeeNo(strWorkNumberPrefix+workNumber);
        		personInfoManager.save(personInfo);
        		
        		//处理规则工号
        		PersonWorkNumber personWrokNumber=personWorkNumberManager.findUniqueBy("numberNo",Integer.valueOf(workNumber));//.findUniqueBy(hsqlString,numberInteger);
        		if(personWrokNumber!=null){
        			personWrokNumber.setIsUse("1");
        			personWorkNumberManager.save(personWrokNumber);
        		}
        		
    		}else {//这个员工不存在，创建一条新数据
    			String companyID = "";
    			String departmentID = "";
    			
    			PartyEntity companyPartyEntity = partyEntityManager.findUniqueBy("name", company);
    			if(companyPartyEntity!=null&&companyPartyEntity.toString().length()>0){
    			companyID = companyPartyEntity.getId().toString();
    			
    			String hql = "from PartyStruct where childEntity.name = ? and parentEntity = ? ";
    			
    			PartyStruct departPartyStruct = partyStructManager.findUnique(hql, departmentName,companyPartyEntity);
    			if(departPartyStruct!=null&&departPartyStruct.toString().length()>0){
    				
    				departmentID = departPartyStruct.getChildEntity().getId().toString();    				
    			}
    			if(departmentID!=null&&departmentID.length()>0){
	    			PersonInfo newPerson=new PersonInfo ();
	    			newPerson.setFullName(fullName);
	    			newPerson.setUsername(userName);
	    			newPerson.setCompanyName(company);
	    			newPerson.setCompanyCode(companyID);
	    			newPerson.setDepartmentName(departmentName);
	    			newPerson.setDepartmentCode(departmentID);
	    			newPerson.setJobStatus("1");
	    			newPerson.setLaborType("1");
	    			newPerson.setPriority(0);
	    			newPerson.setDelFlag("0");
	    			newPerson.setQuitFlag("0");
	    			newPerson.setStopFlag("active");
	    			newPerson.setTenantId("1");
	    			newPerson.setSecret("1");
	    			insertPersonInfo("0",newPerson, 3, Long.parseLong(departmentID),0l,Long.parseLong(currentUserHolder.getUserId()));
	    			
	    			String strWorkNumberPrefix=partyConnector.getWorkNumerPrefix(newPerson.getId().toString());
	    			newPerson.setEmployeeNoDepart(strWorkNumberPrefix);
	    			newPerson.setEmployeeNoNum(workNumber);
	    			newPerson.setEmployeeNo(strWorkNumberPrefix+workNumber);
	    			
	    			personInfoManager.save(newPerson);
	    			
	    			
	    			//处理规则工号
	        		PersonWorkNumber personWrokNumber=personWorkNumberManager.findUniqueBy("numberNo",Integer.valueOf(workNumber));//.findUniqueBy(hsqlString,numberInteger);
	        		if(personWrokNumber!=null){
	        			personWrokNumber.setIsUse("1");
	        			personWorkNumberManager.save(personWrokNumber);
	        		}
    			}else {
    				
    				Map<String, Object> mmap = new HashMap<String, Object> ();
    				mmap.put("workNumber", workNumber);
    				mmap.put("fullName", fullName);
    				mmap.put("company", company);
    				mmap.put("userName", userName);
    				mmap.put("departmentName", departmentName);
    				mmap.put("failReason", "失败原因：部门名称不正确");
    				
    				failMap.add(mmap);
    			}
    			
    		}else {
				
    			Map<String, Object> mmap = new HashMap<String, Object> ();
				mmap.put("workNumber", workNumber);
				mmap.put("fullName", fullName);
				mmap.put("company", company);
				mmap.put("userName", userName);
				mmap.put("departmentName", departmentName);
				mmap.put("failReason", "失败原因：公司名称不正确");
				
				failMap.add(mmap);}
	
    	}
    		
    		
   }
    	
    	//字典控制导入，修改值为0，防止再次导入
    	String hql="from DictInfo where dictType.name=?";
    	DictInfo dictInfo=dictInfoManager.findUnique(hql, "personDataWrokNumberImport");
    	if(dictInfo!=null){
    		dictInfo.setValue("0");
    		dictInfoManager.save(dictInfo);	
    	}
    	
    	return failMap;
    }
    
    /**
     * 删除特定人员账号
     * add by lilei at 2016-06-20
     * **/
    private void delPersonAccount(){
    	/*叶子辰	yezichen	易麦通	客服部		×	直接删除
    	吴强	wuqiang	易麦通	客服部		×	直接删除
    	张学玲	zhangxueling	易麦通	客服部	同一个人	×	直接删除
    	张维	zhangwei	易麦通	客服部		×	直接删除
    	徐连芝	xulianzhi	易麦通	客服部		×	直接删除
    	杨梦蝶	yangmengdie	易麦通	客服部		×	直接删除
    	王微倩	wangweiqian	易麦通	客服部	同一个人	×	直接删除
    	王慧	wanghui	易麦通	客服部	同一个人	×	直接删除
    	王洋	kfwangyang	易麦通	客服部	同一个人	×	直接删除
    	许烁	xushuo	易麦通	客服部	同一个人	×	直接删除
    	贾晓黎	jiaxiaoli	易麦通	客服部	同一个人	×	直接删除
    	赵媛媛	zhaoyuanyuan	易麦通	客服部		×	直接删除*/
    	
    	String strNames="叶子辰,吴强,张学玲,张维,徐连芝,杨梦蝶,王微倩,王慧,王洋,许烁,贾晓黎,赵媛媛";
    	String company="易麦通";
    	String[] strNameList=strNames.split(",");
    	String hqlString="from PersonInfo where fullName=? and companyName=?";
    	for(String strName:strNameList){
    		PersonInfo personInfo=personInfoManager.findUnique(hqlString, strName,company);
    		if(personInfo!=null){
    			if(personInfo.getDelFlag().equals("0"))
    				delPersonInfo(personInfo);
    		}
    	}
    }
    
    public void delPersonInfo(PersonInfo personInfo) {

        String tenantId = tenantHolder.getTenantId();

        //PersonInfo personInfo = personInfoManager.get(id);

        // TODO 此处应该判断是否允许离职。例如是否存在未处理的流程等
        //personInfo.setDelFlag(PersonInfoConstants.DELETE_FLAG_YES);
        personInfo.setDelFlag(PersonInfoConstants.DELETE_FLAG_YES);
        personInfoManager.update(personInfo);

        // 处理party_entity表
        PartyEntity partyEntity = partyEntityManager.get(personInfo.getId());
        partyEntity.setDelFlag(PersonInfoConstants.DELETE_FLAG_YES);
        partyEntityManager.update(partyEntity);

        // 处理account_info表
        AccountInfo accountInfo = accountInfoManager.get(personInfo.getId());
        accountInfo.setDelFlag(PersonInfoConstants.DELETE_FLAG_YES);
        accountInfoManager.update(accountInfo);

        UserDTO userDto = new UserDTO();
        userDto.setId(Long.toString(accountInfo.getId()));
        userDto.setUsername(accountInfo.getUsername());
        userDto.setRef(accountInfo.getCode());
        userDto.setUserRepoRef(tenantId);
        userCache.removeUser(userDto);

        // 处理用户权限
        Long entityId = this.getAuthUserStatusId(userDto.getId(), tenantId);
        if (entityId != null) {
            jdbcTemplate.update(removeUserRoleSql, entityId);
            jdbcTemplate.update(removeUserSql, entityId);
        }

        // 用户权限删除
        configUserDeleteRole(this.convertUserDto(accountInfo, personInfo));
    }

    @Resource
    public void setAccountInfoManager(AccountInfoManager accountInfoManager) {
        this.accountInfoManager = accountInfoManager;
    }

    @Resource
    public void setPersonInfoManager(PersonInfoManager personInfoManager) {
        this.personInfoManager = personInfoManager;
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
    public void setPartyTypeManager(PartyTypeManager partyTypeManager) {
        this.partyTypeManager = partyTypeManager;
    }

    @Resource
    public void setPartyEntityManager(PartyEntityManager partyEntityManager) {
        this.partyEntityManager = partyEntityManager;
    }

    @Resource
    public void setPartyStructTypeManager(PartyStructTypeManager partyStructTypeManager) {
        this.partyStructTypeManager = partyStructTypeManager;
    }

    @Resource
    public void setPartyStructManager(PartyStructManager partyStructManager) {
        this.partyStructManager = partyStructManager;
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
    public void setUserCache(UserCache userCache) {
        this.userCache = userCache;
    }

    @Resource
    public void setUserPublisher(UserPublisher userPublisher) {
        this.userPublisher = userPublisher;
    }

    @Resource
    public void setPartyConnector(PartyConnector partyConnector) {
        this.partyConnector = partyConnector;
    }

    @Resource
    public void setAuthService(AuthService authService) {
        this.authService = authService;
    }

    @Resource
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Resource
    public void setUserStatusManager(UserStatusManager userStatusManager) {
        this.userStatusManager = userStatusManager;
    }

    @Resource
    public void setAuthCache(AuthCache authCache) {
        this.authCache = authCache;
    }

    @Resource
    public void setIdGenerator(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    @Resource
    public void setTenantConnector(TenantConnector tenantConnector) {
        this.tenantConnector = tenantConnector;
    }

    @Resource
    public void setUserAuthConnector(UserAuthConnector userAuthConnector) {
        this.userAuthConnector = userAuthConnector;
    }

    @Resource
    public void setUserAuthCache(UserAuthCache userAuthCache) {
        this.userAuthCache = userAuthCache;
    }
    
    @Resource
   	public void setRosterLogManager(RosterLogManager rosterLogManager) {
   		this.rosterLogManager = rosterLogManager;
   	}
    
    @Resource
   	public void setUpdatePersonManager(UpdatePersonManager updatePersonManager) {
   		this.updatePersonManager = updatePersonManager;
   	}
    
    @Resource
    public void setPersonWorkNumberManager(PersonWorkNumberManager personWorkNumberManager) {
		this.personWorkNumberManager = personWorkNumberManager;
	}
    
    @Resource
   	public void setCustomService(CustomService customService) {
   		this.customService = customService;
   	}
    
    @Resource
   	public void setCustomManager(CustomManager customManager) {
   		this.customManager = customManager;
   	}
    
    @Resource
    public void setDictConnector(DictConnector dictConnector) {
        this.dictConnector = dictConnector;
    }
 
    @Resource
   	public void setDictInfoManager(DictInfoManager dictInfoManager) {
   		this.dictInfoManager = dictInfoManager;
   	}

}
