package com.mossle.user.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.mossle.api.user.UserDTO;
import com.mossle.core.auth.CurrentUserHolder;
import com.mossle.core.id.IdGenerator;
import com.mossle.core.util.StringUtils;
import com.mossle.dict.persistence.domain.DictInfo;
import com.mossle.dict.persistence.manager.DictInfoManager;
import com.mossle.operation.persistence.domain.CustomPresetApprover;
import com.mossle.operation.persistence.manager.CustomPresetApproverManager;
import com.mossle.party.PartyConstants;
import com.mossle.party.persistence.domain.PartyEntity;
import com.mossle.party.persistence.domain.PartyStruct;
import com.mossle.party.persistence.manager.PartyStructManager;
import com.mossle.user.persistence.domain.RosterLog;
import com.mossle.user.persistence.domain.UserAttr;
import com.mossle.user.persistence.domain.UserBase;
import com.mossle.user.persistence.domain.UserSchema;
import com.mossle.user.persistence.manager.RosterLogManager;
import com.mossle.user.persistence.manager.UserAttrManager;
import com.mossle.user.persistence.manager.UserBaseManager;
import com.mossle.user.persistence.manager.UserRepoManager;
import com.mossle.user.persistence.manager.UserSchemaManager;
import com.mossle.user.publish.UserPublisher;

import org.activiti.engine.impl.cmd.AddCommentCmd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Exception.class)
public class UserService {
    private static Logger logger = LoggerFactory.getLogger(UserService.class);
    private UserBaseManager userBaseManager;
    private UserRepoManager userRepoManager;
    private UserAttrManager userAttrManager;
    private UserSchemaManager userSchemaManager;
    private UserPublisher userPublisher;
    private JdbcTemplate jdbcTemplate;
    private IdGenerator idGenerator;
    private DictInfoManager dictInfoManager;
    private PartyStructManager partyStructManager;
    private RosterLogManager rosterLogManager;
    private PersonInfoService personInfoService;
    private CurrentUserHolder currentUserHolder;
    private CustomPresetApproverManager customPresetApproverManager;
    
    /**
     * 添加用户.
     */
    public void insertUser(UserBase userBase, Long userRepoId,
            Map<String, Object> parameters) {
        // user repo
        userBase.setUserRepo(userRepoManager.get(userRepoId));

        // userBase.setTenantId(TenantHolder.getTenantId());
        userBaseManager.save(userBase);

        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            String key = entry.getKey();
            String value = (String) entry.getValue();

            UserSchema userSchema = userSchemaManager.findUnique(
                    "from UserSchema where code=? and userRepo.id=?", key,
                    userRepoId);

            if (userSchema == null) {
                logger.debug("skip : {}", key);

                continue;
            }

            UserAttr userAttr = new UserAttr();
            userAttr.setUserSchema(userSchema);
            userAttr.setUserBase(userBase);
            // userAttr.setTenantId(TenantHolder.getTenantId());
            userAttrManager.save(userAttr);

            String type = userSchema.getType();

            if ("boolean".equals(type)) {
                userAttr.setBooleanValue(Integer.parseInt(value));
            } else if ("date".equals(type)) {
                try {
                    userAttr.setDateValue(new SimpleDateFormat("yyyy-MM-dd")
                            .parse(value));
                } catch (ParseException ex) {
                    logger.info(ex.getMessage(), ex);
                }
            } else if ("long".equals(type)) {
                userAttr.setLongValue(Long.parseLong(value));
            } else if ("double".equals(type)) {
                userAttr.setDoubleValue(Double.parseDouble(value));
            } else if ("string".equals(type)) {
                userAttr.setStringValue(value);
            } else {
                throw new IllegalStateException("illegal type: "
                        + userSchema.getType());
            }

            userAttrManager.save(userAttr);
        }

        userPublisher.notifyUserCreated(this.convertUserDto(userBase));
    }

    /**
     * 更新用户.
     */
    public void updateUser(UserBase userBase, Long userRepoId,
            Map<String, Object> parameters) {
        // user repo
        userBase.setUserRepo(userRepoManager.get(userRepoId));
        userBaseManager.save(userBase);

        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            String key = entry.getKey();
            String value = this.getStringValue(entry.getValue());

            UserSchema userSchema = userSchemaManager.findUnique(
                    "from UserSchema where code=? and userRepo.id=?", key,
                    userRepoId);

            if (userSchema == null) {
                logger.debug("skip : {}", key);

                continue;
            }

            UserAttr userAttr = userAttrManager.findUnique(
                    "from UserAttr where userSchema=? and userBase=?",
                    userSchema, userBase);

            if (userAttr == null) {
                userAttr = new UserAttr();
                userAttr.setUserSchema(userSchema);
                userAttr.setUserBase(userBase);

                // userAttr.setTenantId(TenantHolder.getTenantId());
            }

            String type = userSchema.getType();

            if ("boolean".equals(type)) {
                userAttr.setBooleanValue(Integer.parseInt(value));
            } else if ("date".equals(type)) {
                try {
                    userAttr.setDateValue(new SimpleDateFormat("yyyy-MM-dd")
                            .parse(value));
                } catch (ParseException ex) {
                    logger.info(ex.getMessage(), ex);
                }
            } else if ("long".equals(type)) {
                userAttr.setLongValue(Long.parseLong(value));
            } else if ("double".equals(type)) {
                userAttr.setDoubleValue(Double.parseDouble(value));
            } else if ("string".equals(type)) {
                userAttr.setStringValue(value);
            } else {
                throw new IllegalStateException("illegal type: "
                        + userSchema.getType());
            }

            userAttrManager.save(userAttr);
        }

        userPublisher.notifyUserUpdated(this.convertUserDto(userBase));
    }

    /**
     * 删除用户.
     */
    public void removeUser(UserBase userBase) {
        userBaseManager.removeAll(userBase.getUserAttrs());
        userBaseManager.remove(userBase);
        userPublisher.notifyUserRemoved(this.convertUserDto(userBase));
    }

    public String getStringValue(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof String) {
            return (String) value;
        }

        return value.toString();
    }

    public UserDTO convertUserDto(UserBase userBase) {
        UserDTO userDto = new UserDTO();
        userDto.setId(Long.toString(userBase.getId()));
        userDto.setUsername(userBase.getUsername());
        userDto.setDisplayName(userBase.getNickName());
        userDto.setEmail(userBase.getEmail());
        userDto.setMobile(userBase.getMobile());

        return userDto;
    }
    
    /**
     * 花名册-生成工号
     * add by lilei at 2018.05.28
     * */
    public String getWorkNumber() {
		String strNum="";
		try {
			String strSql="select numberNo from person_WorkNumber where isUse='0' order by numberNo limit 0,1";
			List<String> numList=jdbcTemplate.queryForList(strSql, String.class);
			if(numList!=null&&numList.size()>0)
			{
				strNum=numList.get(0);
			}
			if(StringUtils.isBlank(strNum))
			{
				String strInsert="insert into person_WorkNumber(id,numberNo,isUse) values(%s,%s,'%s')";
				strSql="select numberNo from person_WorkNumber order by numberNo desc limit 0,1";
				numList=jdbcTemplate.queryForList(strSql, String.class);
				if(numList!=null&&numList.size()>0)
				{
					strNum=numList.get(0);
				}
				//num=jdbcTemplate.queryForObject(strSql, String.class);
				if(StringUtils.isBlank(strNum)) strNum="0";
				for(int i=1;i<100;i++)
				{
					jdbcTemplate.update(String.format(strInsert,idGenerator.generateId(),Integer.valueOf(strNum)+i,"0"));
				}
				strNum=getWorkNumber();
			}
			else{
				//strNum=num;
				if(strNum.length()==1)
					strNum="000"+strNum;
				else if(strNum.length()==2) 
					strNum="00"+strNum;
				else if(strNum.length()==3)
					strNum="0"+strNum;
			}
		} catch (Exception e) {
			
		}
		
		return strNum;
	}
    
    /**
     * 得到流程开启状态，1：开启，0：关闭
     * **/
    public String getAuditOpenStatus(){
    	String isAudit="1";
        String hql="from DictInfo where dictType.name=?";
    	DictInfo dictInfo=dictInfoManager.findUnique(hql, "personMasterAudit");
        if(dictInfo!=null){
        	isAudit=dictInfo.getValue();
        }
        return isAudit;
    }
    
    /**
     * 是否开启花名册验证；1：是，0：否
     * **/
    public String getValidateStatus(){
    	String isValidate="1";
        String hql="from DictInfo where dictType.name=?";
    	DictInfo dictInfo=dictInfoManager.findUnique(hql, "personAddAndUPdateValidate");
        if(dictInfo!=null){
        	isValidate=dictInfo.getValue();
        }
        return isValidate;
    }

  /**

     * 查询是否开启别名（1：开启，0：关闭）
     * **/
    public String getOpenOtherNameStatus(){
    	String isOpen="0";
        String hql="from DictInfo where dictType.name=?";
    	DictInfo dictInfo=dictInfoManager.findUnique(hql, "isOpenOtherName");
        if(dictInfo!=null){
        	isOpen=dictInfo.getValue();
        }
        return isOpen;
    }

    /**
     * 根据姓名得到别名
     * **/
    public String getOtherName(String strName){
    	/*if(IFNULL(realName,'')='') THEN
		RETURN '';
	end if;
	SET realName=TRIM(BOTH ' ' FROM realName);
	SET @length=char_length(realName);
	SET @surName=Left(realName,1);
	set @i=0;
	SET @lastName='';
	while @i<@length-1 DO
		SET @lastName=CONCAT(@lastName,'*');
		SET @i=@i+1;
	end while;
	return CONCAT(@surName,@lastName);*/
    	if(strName==null)
    		return "";
    	else if(strName.equals(""))
    		return "";
    	
    	strName=strName.trim();
    	int nameLength=strName.length();
    	String strSurName=strName.substring(0, 1);
    	for (int i = 0; i < nameLength-1; i++) {
    		strSurName+="*";
		}
    	return strSurName;
    }
 /**

     * 判断是否是管理员角色（包括系统管理员）
     * 
     * **/
    public String getIsAdminRole(String userId){
    	String isSystemRole="0";
    	if(currentUserHolder.getUserId().equals(PartyConstants.ADMIN_USER_ID.toString()))
    		isSystemRole="1";
    	else {
    		String strSql="SELECT ROLE_ID FROM auth_user_status s "
    		    	+" INNER JOIN auth_user_role r ON s.ID=r.USER_STATUS_ID "
    		    	+" where s.ref=%s";
	    	List<String> userRoleIds=jdbcTemplate.queryForList(String.format(strSql,userId), String.class);
	    	if(userRoleIds.contains("2")){
	    		isSystemRole="1";
	    	}
		}
    	return isSystemRole;
    }
    

    /** 调整岗位 add by lilei at 2018.05.3
     * @param partyEntity 要修改人员的实体
    * @param changePartyEntity 新的岗位实体
    * @param partyEntityId 要修改数据老岗位实体ID
    * **/
    @Transactional(readOnly=false)
    public void changePosition(PartyEntity partyEntity,PartyEntity changePartyEntity,Long partyEntityId,String applyCode){
    	
    	if(applyCode.equals("0")){
    		String oldPosition=personInfoService.getPositionCompany(partyEntityId);
        	String newPostion=personInfoService.getPositionCompany(changePartyEntity.getId());
        	RosterLog rosterLog = new RosterLog ();
    		rosterLog.setCode(applyCode);     								//受理单编号.
    		rosterLog.setOperationID(currentUserHolder.getName()); 		   	//操作人员id. 
    		rosterLog.setContentBefore(oldPosition);  						//修改之前的内容. 
    		rosterLog.setContentNew(newPostion);							//修改后的新内容. 
    		rosterLog.setUpdateColumn(""); 									//被修改的字段名. 
    		rosterLog.setUpdateColumnName("岗位");
    		rosterLog.setIsapproval("0");									//1：流程审核还未通过   0：流程审核通过 
    		rosterLog.setUpdateTime(new Date());  	  						//修改时间. 
    		rosterLog.setEmployeeNo(partyEntity.getId().toString());			//被修改的员工编号. */
    	    rosterLogManager.save(rosterLog);
    	}
    	
	    
    	String hql="from PartyStruct where partyStructType.id=4 and childEntity=? and parentEntity.id=?";
       	PartyStruct partyStruct=partyStructManager.findUnique(hql,partyEntity,partyEntityId);
       	partyStruct.setParentEntity(changePartyEntity);
       	partyStructManager.save(partyStruct);
    }
    
    public List<CustomPresetApprover> getCustomPresetApprovers(){
    	String userId=currentUserHolder.getUserId();
    	String hql="from CustomPresetApprover where delStatus='0' and userId="+Long.valueOf(userId)+" order by orderNum desc";
		List<CustomPresetApprover> presetApproverList=customPresetApproverManager.find(hql);
        return presetApproverList;
        
    }
    public List<CustomPresetApprover> getCustomPresetApproversH5(String userId) {
    	String hql="from CustomPresetApprover where delStatus='0' and userId="+Long.valueOf(userId)+" order by orderNum desc";
		List<CustomPresetApprover> presetApproverList=customPresetApproverManager.find(hql);
        return presetApproverList;
	}
    /*public void recordChangeLog(){
    	//记录日志
   		RosterLog rosterLog = new RosterLog ();
	      rosterLog.setCode(request.getParameter("applyCode"));     					*//** 受理单编号. *//*
	      rosterLog.setOperationID(curentUserName); 		*//** 操作人员id. *//*
	      rosterLog.setContentBefore("添加新职员");  	*//** 修改之前的内容. *//*
	      rosterLog.setContentNew("添加新职员");			*//** 修改后的新内容. *//*
	      rosterLog.setUpdateColumn("添加新职员"); 		*//**被修改的字段名. *//*
	      rosterLog.setUpdateColumnName("添加新职员");
	      rosterLog.setIsapproval("1");//1：流程审核还未通过   0：流程审核通过    20180515 cz 新录入数据 初始化为未通过，待审核通过再更新为0
	      rosterLog.setUpdateTime(new Date());  	  	*//** 修改时间. *//*
	      rosterLog.setEmployeeNo("");				*//**被修改的员工编号. *//*
	      rosterLogManager.save(rosterLog);
    }*/

    @Resource
    public void setUserBaseManager(UserBaseManager userBaseManager) {
        this.userBaseManager = userBaseManager;
    }
    
    @Resource
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Resource
    public void setUserRepoManager(UserRepoManager userRepoManager) {
        this.userRepoManager = userRepoManager;
    }

    @Resource
    public void setUserAttrManager(UserAttrManager userAttrManager) {
        this.userAttrManager = userAttrManager;
    }

    @Resource
    public void setUserSchemaManager(UserSchemaManager userSchemaManager) {
        this.userSchemaManager = userSchemaManager;
    }

    @Resource
    public void setUserPublisher(UserPublisher userPublisher) {
        this.userPublisher = userPublisher;
    }
    
    @Resource
	public void setIdGenerator(IdGenerator idGenerator) {
		this.idGenerator = idGenerator;
	}
    
    @Resource
    public void setDictInfoManager(DictInfoManager dictInfoManager) {
		this.dictInfoManager = dictInfoManager;
	}
    
    @Resource
    public void setPartyStructManager(PartyStructManager partyStructManager) {
        this.partyStructManager = partyStructManager;
    }
    
    @Resource
    public void setRosterLogManager(RosterLogManager rosterLogManager) {
        this.rosterLogManager = rosterLogManager;
    }
    
    @Resource
    public void setPersonInfoService(PersonInfoService personInfoService) {
        this.personInfoService = personInfoService;
    }
    
    @Resource
    public void setCurrentUserHolder(CurrentUserHolder currentUserHolder) {
        this.currentUserHolder = currentUserHolder;
    }
    
    @Resource
    public void setCustomPresetApproverManager(CustomPresetApproverManager customPresetApproverManager) {
        this.customPresetApproverManager = customPresetApproverManager;
    }

	
}
