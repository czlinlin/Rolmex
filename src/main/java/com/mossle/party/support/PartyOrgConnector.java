package com.mossle.party.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.ws.rs.GET;

import com.mossle.api.org.OrgConnector;
import com.mossle.api.org.OrgDTO;
import com.mossle.api.party.PartyConnector;
import com.mossle.api.party.PartyDTO;
import com.mossle.api.party.PartyEntityOrgDTO;
import com.mossle.core.page.Page;
import com.mossle.core.util.StringUtils;
import com.mossle.operation.persistence.domain.Return;
import com.mossle.party.PartyConstants;
import com.mossle.party.persistence.domain.PartyEntity;
import com.mossle.party.persistence.domain.PartyStruct;
import com.mossle.party.persistence.manager.PartyEntityManager;
import com.mossle.party.persistence.manager.PartyStructManager;
import com.mossle.party.persistence.manager.PartyStructTypeManager;
import com.mossle.user.PersonInfoConstants;
import com.mossle.user.persistence.domain.AccountInfo;
import com.mossle.user.persistence.domain.PersonInfo;
import com.mossle.user.persistence.manager.AccountInfoManager;
import com.mossle.user.persistence.manager.PersonInfoManager;
import com.mossle.util.StringUtil;

import org.activiti.engine.impl.cmd.AddCommentCmd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 组织机构接口.
 */
public class PartyOrgConnector implements OrgConnector {
    private static Logger logger = LoggerFactory
            .getLogger(PartyOrgConnector.class);
    private PartyEntityManager partyEntityManager;
    private PartyStructManager partyStructManager;
    private PersonInfoManager personInfoManager;
    private List<PartyEntity> partyList = new ArrayList<PartyEntity>();
    private JdbcTemplate jdbcTemplate;
    private PartyConnector partyConnector;
    /**
     * 根据userId获得对应的PartyEntity.
     */
    public PartyEntity findUser(String userId) {
        // 找到userId对应的partyEntity
        String hql = "from PartyEntity where delFlag = '0' and partyType.type=? and ref=?";
        PartyEntity partyEntity = partyEntityManager.findUnique(hql,
                PartyConstants.TYPE_USER, userId);

        return partyEntity;
    }

    /**
     * 获得人员对应的岗位的级别.
     */
    public int getJobLevelByUserId(String userId) {
        // 找到userId对应的partyEntity
        PartyEntity partyEntity = this.findUser(userId);

        if (partyEntity == null) {
            logger.info("cannot find user : {}", userId);

            return -1;
        }

        // 如果直接上级是岗位，就返回岗位级别
        // for (PartyStruct partyStruct : partyEntity.getParentStructs()) {
        // if (partyStruct.getParentEntity().getPartyType().getType() == PartyConstants.TYPE_POSITION) {
        // return partyStruct.getParentEntity().getLevel();
        // }
        // }
        for (PartyStruct partyStruct : partyEntity.getChildStructs()) {
            if ("user-position".equals(partyStruct.getPartyStructType()
                    .getType())) {
                return partyStruct.getChildEntity().getLevel();
            }
        }

        // 如果没有对应的岗位，就返回-1，就是最低的级别
        return -1;
    }
    
    
    /**
     * 获得管理者对应的组织机构.
     */
    public List<Long> getPartyByManageId(String userId) {
    	
    	List<Long> list = new ArrayList<Long>();
    	
        // 找到userId对应的partyEntity
        PartyEntity partyEntity = this.findUser(userId);

        if (partyEntity == null) {
            return list;
        }

        for (PartyStruct partyStruct : partyEntity.getParentStructs()) {
            if ("manage".equals(partyStruct.getPartyStructType().getType())) {
            	// System.out.println(partyStruct.getParentEntity().getName());
            	if (!partyStruct.getParentEntity().getDelFlag().equals(PartyConstants.PARTY_DELETE)) {
            		list.add(partyStruct.getParentEntity().getId());
            	}
            }
        }

        return list;
    }
    
    /**
     * 组织机构对应的管理者.
     */
    @SuppressWarnings("unchecked")
	public List<String> getManageIdByParty(PartyEntity partyEntity) {
    	
    	List<String> userIds = new ArrayList<String>();
    	
        // 组织的负责人可能是岗位，可能是人
        String hql = "from PartyStruct where parentEntity=? and partyStructType=2";

        // 如果没有选中partyEntityId，就啥也不显示
        List<PartyStruct> list= partyStructManager.find(hql, partyEntity);
        
        for (PartyStruct partyStruct : list) {
            
        	if (!partyStruct.getChildEntity().getDelFlag().equals(PartyConstants.PARTY_DELETE)) {
        		userIds.add(partyStruct.getChildEntity().getId().toString());
        	}
        }
        
        return userIds;
    }
    

    
    /**
     * 根据userIds获得字符串中最高的职位等级.
     */
    public String getPositionMaxGradeByUserIds(String userIds) {
    	String strReturnLevel="CG";
    	try {
    		String [] userId = userIds.split(",");
        	List<PersonInfo> list = new ArrayList();
        	for(int i = 0;i<userId.length;i++){
        		PersonInfo person = personInfoManager.findUniqueBy("id", Long.parseLong(userId[i]));
        		list.add(person);
        	}
        	if (list != null  && list.size() >0) {
        		Long maxPosition = (long) 0;
        		for(PersonInfo vo : list){
        			//没有职位，则找下一个人 add at 2018-08-06 14:17
        			if(vo.getPositionCode()==null)
        				continue;
        			if(vo.getPositionCode().equals(""))
        				continue;
        			
        			Long position = Long.parseLong(vo.getPositionCode());
        			if(maxPosition == 0){
        				maxPosition = position;
        			}
        			if(position > maxPosition){
        				maxPosition = position;
        			}
        		}
        		// 董事长
        		if (PersonInfoConstants.POSITION_CHAIRMAN.equals(String.valueOf(maxPosition))) {
        			return "S";
        		}
        		
        		// 总裁
        		if (PersonInfoConstants.POSITION_FIRST_CEO.equals(String.valueOf(maxPosition))) {
        			return "A";
        		}
        		
        		// 副总裁
        		if (PersonInfoConstants.POSITION_SECOND_CEO.equals(String.valueOf(maxPosition))) {
        			return "B";
        		}
        		
        		return "CG";
        	}
		} catch (Exception e) {
			strReturnLevel="CG";
			// TODO: handle exception
		}
    	
    	return strReturnLevel;
    }
    
    /**
     * 根据人员和对应的岗位名称，获得离这个人员最近的岗位的级别.
     * <p>
     * TODO: 这里目前肯定有问题，以后记得研究 2016-07-06
     */
    public int getJobLevelByInitiatorAndPosition(String userId,
                                                 String positionName) {
        // 获得岗位对应的partyEntity
        String hql = "from PartyEntity where partyType.type=? and name=?";
        PartyEntity partyEntity = partyEntityManager.findUnique(hql,
                PartyConstants.TYPE_POSITION, positionName);

        // 直接返回级别
        return partyEntity.getLevel();
    }

    /**
     * 获得上级领导.
     */
    public String getSuperiorId(String userId) {
        logger.debug("user id : {}", userId);

        // zyl 2017-07-17
        PartyEntity partyEntity = this.findUser(userId);
        //AccountInfo accountInfo = accountInfoManager.get(Long.parseLong(userId));
        //PartyEntity partyEntity = partyEntityManager.get(Long.parseLong(accountInfo.getCode()));

        logger.debug("party entity : {}, {}", partyEntity.getId(),
                partyEntity.getName());

        PartyEntity superior = this.findSuperior(partyEntity);

        if (superior == null) {
            logger.info("cannot find superiour : {} {}", partyEntity.getName(),
                    partyEntity.getId());

            return null;
        }

        return superior.getRef();
    }

    /**
     * 获得人员对应的最近的岗位下的所有用户.
     * <p>
     * TODO: 这里目前肯定有问题，以后记得研究 2016-07-06
     */
    public List<String> getPositionUserIds(String userId, String positionName) {
        PartyEntity partyEntity = this.findUser(userId);

        return this.findPositionUserIds(partyEntity, positionName);
    }

    /**
     * 获取这个人的所有的直接部门或者公司.
     */
    public List<OrgDTO> getOrgsByUserId(String userId) {
        PartyEntity partyEntity = this.findUser(userId);

        if (partyEntity == null) {
            return Collections.emptyList();
        }

        List<OrgDTO> orgDtos = new ArrayList<OrgDTO>();

        for (PartyStruct partyStruct : partyEntity.getParentStructs()) {
            PartyEntity parent = partyStruct.getParentEntity();

            if (parent.getPartyType().getType() == PartyConstants.TYPE_ORG) {
                OrgDTO orgDto = new OrgDTO();
                orgDto.setId(Long.toString(parent.getId()));
                orgDto.setName(parent.getName());
                orgDto.setTypeName(parent.getPartyType().getName());
                orgDto.setType(parent.getPartyType().getType());
                orgDto.setRef(parent.getRef());
                orgDtos.add(orgDto);
            }
        }

        return orgDtos;
    }

    // ~ ==================================================

    /**
     * 获得直接上级.
     */
    public PartyEntity findSuperior(PartyEntity child) {
        // 得到上级部门
        PartyEntity partyEntity = this.findUpperDepartment(child, true);

        // 如果存在上级部门
        while (partyEntity != null) {
            logger.debug("partyEntity : {}, {}", partyEntity.getId(),
                    partyEntity.getName());

            // 遍历上级部门的每个叶子
            for (PartyStruct partyStruct : partyEntity.getChildStructs()) {
                if (!"manage"
                        .equals(partyStruct.getPartyStructType().getType())) {
                    continue;
                }

                // 遍历管理关系
                PartyEntity childPartyEntity = partyStruct.getChildEntity();
                logger.debug("child : {}, {}", childPartyEntity.getId(),
                        childPartyEntity.getName());

                if (childPartyEntity.getPartyType().getType() == PartyConstants.TYPE_USER) {
                    // 如果是人员，直接返回
                    return childPartyEntity;
                } else if (childPartyEntity.getPartyType().getType() == PartyConstants.TYPE_POSITION) {
                    // 如果是岗位，继续查找部门下所有岗位对应的人员，返回
                    List<PartyEntity> users = this.findByPosition(partyEntity,
                            childPartyEntity.getName());

                    if (!users.isEmpty()) {
                        return users.get(0);
                    }
                }
            }

            // 递归获取上级部门
            partyEntity = this.findUpperDepartment(partyEntity, true);
        }

        // 找不到上级领导
        return null;
    }

    /**
     * 在本部门下，查找对应职位的人员.
     */
    public List<PartyEntity> findByPosition(PartyEntity partyEntity,
                                            String positionName) {
        List<PartyEntity> partyEntities = new ArrayList<PartyEntity>();

        for (PartyStruct partyStruct : partyEntity.getChildStructs()) {
            if (!"struct".equals(partyStruct.getPartyStructType().getType())) {
                continue;
            }

            PartyEntity childPartyEntity = partyStruct.getChildEntity();
            logger.debug("child : {}, {}", childPartyEntity.getId(),
                    childPartyEntity.getName());

            if (childPartyEntity.getPartyType().getType() != PartyConstants.TYPE_USER) {
                continue;
            }

            if (this.hasPosition(childPartyEntity, positionName)) {
                partyEntities.add(childPartyEntity);
            }
        }

        return partyEntities;
    }

    /**
     * 判断用户是否包含对应岗位.
     */
    public boolean hasPosition(PartyEntity partyEntity, String positionName) {
        for (PartyStruct partyStruct : partyEntity.getChildStructs()) {
            if (!"user-position".equals(partyStruct.getPartyStructType()
                    .getType())) {
                continue;
            }

            PartyEntity childPartyEntity = partyStruct.getChildEntity();
            logger.debug("child : {}, {}", childPartyEntity.getId(),
                    childPartyEntity.getName());

            if (childPartyEntity.getName().equals(positionName)) {
                return true;
            }
        }

        return false;
    }

    public boolean isAdmin(PartyStruct partyStruct) {
        if (partyStruct == null) {
            return false;
        }

        // if (partyStruct.getAdmin() == null) {
        // return false;
        // }
        // return partyStruct.getAdmin() == 1;
        PartyEntity department = partyStruct.getParentEntity();
        PartyEntity user = partyStruct.getChildEntity();

        logger.info("department : {} {}", department.getName(),
                department.getId());

        // 遍历上级部门的每个叶子
        for (PartyStruct childPartyStruct : department.getChildStructs()) {
            if (!"manage".equals(childPartyStruct.getPartyStructType()
                    .getType())) {
                continue;
            }

            // 遍历管理关系
            PartyEntity childPartyEntity = childPartyStruct.getChildEntity();
            logger.debug("child : {}, {}", childPartyEntity.getId(),
                    childPartyEntity.getName());

            if (childPartyEntity.getPartyType().getType() == PartyConstants.TYPE_USER) {
                // 如果是人员，直接返回
                if (childPartyEntity.getId().equals(user.getId())) {
                    return true;
                }
            } else if (childPartyEntity.getPartyType().getType() == PartyConstants.TYPE_POSITION) {
                // 如果是岗位，继续查找部门下所有岗位对应的人员，返回
                List<PartyEntity> users = this.findByPosition(department,
                        childPartyEntity.getName());

                for (PartyEntity userPartyEntity : users) {
                    if (userPartyEntity.getId().equals(user.getId())) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public boolean isNotAdmin(PartyStruct partyStruct) {
        return !this.isAdmin(partyStruct);
    }

    /**
     * 获取岗位的管理者.
     */
    public PartyEntity findAdministrator(PartyEntity parent) {
        for (PartyStruct partyStruct : parent.getChildStructs()) {
            PartyEntity partyEntity = null;
            PartyEntity child = partyStruct.getChildEntity();
            logger.debug("child : {}, {}", child.getId(), child.getName());

            // 完全不考虑岗位下面有其他组织或者岗位的情况
            // 认为岗位下直接就是人员
            if (child.getPartyType().getType() == PartyConstants.TYPE_USER) {
                // 首先岗位必须是管理岗位
                // 如果岗位下是一个人，这个人就是部门的管理者
                // 在想，如果不是管理者，应该也可以是上级吧？比如管理岗位下面的所有人都应该是管理者
                return child;
            }
        }

        return null;
    }

    /**
     * 找到离parent最近的岗位下的人员.
     */
    public List<String> findPositionUserIds(PartyEntity parent,
                                            String positionName) {
        List<String> userIds = new ArrayList<String>();

        // 获得上级部门
        PartyEntity partyEntity = this.findUpperDepartment(parent, false);

        while (partyEntity != null) {
            // 如果是组织，部门或公司
            if (partyEntity.getPartyType().getType() == PartyConstants.TYPE_ORG) {
                for (PartyStruct partyStruct : partyEntity.getChildStructs()) {
                    PartyEntity child = partyStruct.getChildEntity();

                    // 遍历组织下属所有员工
                    if (child.getPartyType().getType() != PartyConstants.TYPE_USER) {
                        continue;
                    }

                    // 如果员工拥有对应的岗位，就放到userIds里
                    for (PartyStruct ps : child.getChildStructs()) {
                        // 只搜索人员岗位关系
                        if (ps.getPartyStructType().getId() != 5) {
                            continue;
                        }

                        if (ps.getChildEntity().getName().equals(positionName)) {
                            // 拥有对应的岗位，就放到userIds里
                            userIds.add(child.getRef());
                        }
                    }
                }
            }

            /*
             * else if ((parent.getPartyType().getType() == PartyConstants.TYPE_POSITION) &&
             * parent.getName().equals(positionName)) { // 如果parent已经是岗位了，而且名字与期望的positionName一致 for (PartyStruct
             * partyStruct : parent.getChildStructs()) { PartyEntity child = partyStruct.getChildEntity();
             * 
             * // 就把岗位下的人直接附加到userIds里 if (child.getPartyType().getType() == PartyConstants.TYPE_USER) {
             * userIds.add(child.getRef()); } } }
             */
            if (userIds.isEmpty()) {
                // 如果没找到userIds，递归到更上一级的部门，继续找
                partyEntity = this.findUpperDepartment(partyEntity, false);
            } else {
                break;
            }
        }

        return userIds;
    }

    /**
     * 通过user查找岗位中对应的用户. 同级、上级、上上级即同一个树枝下
     *
     * @author zyl
     */
    public List<String> findPositionUserByUserId(String userId,
                                                 String positionName) {

        PartyEntity partyEntity = this.findUser(userId);

        List<String> userIds = new ArrayList<String>();

        // 获取岗位下的人员信息
        PartyEntity post = partyEntityManager.get(Long.parseLong(positionName));
        for (PartyStruct partyStruct : post.getChildStructs()) {
            logger.info("{} userIds is empty", partyStruct.getChildEntity().getName());

            String hql = "from PartyStruct where childEntity=? and partyStructType.id=" + PartyConstants.PARTY_STRUCT_TYPE_ORG;

            PartyStruct vo = partyStructManager.findUnique(hql, partyStruct.getChildEntity());

            if (findUpperDepartment(partyEntity, vo.getParentEntity().getId())) {
                userIds.add(partyStruct.getChildEntity().getRef());
            }

        }

        return userIds;
    }
    
    /**
     * 通过user查找岗位中对应的管理者(向上逐级查找)
     *
     * @author zyl
     */
    public List<String> findManageUserByUserId(String userId, String postId) {

        List<String> userIds = new ArrayList<String>();
        List<PartyEntity> userList = new ArrayList<PartyEntity>();
        
        String hql = "from PartyStruct where childEntity=? and childEntity.delFlag='" + PartyConstants.PARTY_NORMAL 
        		+ "' and partyStructType.id=" + PartyConstants.PARTY_STRUCT_TYPE_ORG; 
        
        // 获取岗位下的人员信息
        userList = this.getUsersByPostId(Long.parseLong(postId));
        
        //String tempUserId = userId;
        //for(PartyEntity vo : userList) {
            //userId = tempUserId;
        	while(true){ //循环条件中直接为TRUE 
        		
        		PartyEntity partyEntity = partyEntityManager.get(Long.parseLong(userId));
        		PartyStruct partyStruct = partyStructManager.findUnique(hql, partyEntity);
                PartyEntity parent = partyStruct.getParentEntity();
	        	if (parent != null) {
	        		// 查找本部门的管理者
	        		// System.out.println(parent.getId() + "  " + parent.getName());
	                String hqlm = "from PartyStruct where parentEntity = ? and partyStructType.id=" + PartyConstants.PARTY_STRUCT_TYPE_MANAGE;
	                List<PartyStruct> list = partyStructManager.find(hqlm, parent);
	                
	                if (list != null && list.size() >0) {  // 本部门没有管理者
	        	        for (PartyStruct child : list) {
	        	        	PartyEntity manageUser = child.getChildEntity();
	        	        	// System.out.println(manageUser.getId() + "  " + manageUser.getName());
	        	        	if (manageUser.getDelFlag().equals(PartyConstants.PARTY_NORMAL)) {
	        	        		for(PartyEntity vo : userList) {
		        	        		if (vo.getId() ==  manageUser.getId()) {
		        	        			userIds.add(Long.toString(vo.getId()));
		        	        			break;
			        	        	}
	        	        		}
	        	        	}
	        	        }
	        	        if (userIds.size() > 0) {
	        	        	break;
	        	        } else {
	        	        	if (parent.getId() == PartyConstants.ROOT_PARTY_TREE_ID) {   // 找到根节点，退出循环
	    	        			break;
	    	        		}
	        	        	userId = Long.toString(parent.getId());
	        	        }
	                } else {
	                	userId = Long.toString(parent.getId());
	                }
	        	} else {
	        		break;
	        	}
	        }
        // }

        return userIds;
    }
    
    /**
     * 通过PartyEntityId查找岗位上级、上上级即同一个树枝下
     *
     * @author zyl
     */
    public String findPositionByPartyEntityId(Long PartyEntityId) {

        PartyEntity partyEntity = partyEntityManager.get(PartyEntityId);
        String PartyEntityNames = partyEntity.getName();
        PartyEntity post = partyEntityManager.get(PartyEntityId);
        PartyEntity partyEntityUp=findUpperDepartment(partyEntity,true);
        for (PartyStruct partyStruct : post.getChildStructs()){

            String hql = "from PartyStruct where childEntity=? and partyStructType.id=1";
            PartyStruct vo = partyStructManager.findUnique(hql, partyStruct.getChildEntity());

            if (findUpperDepartment(partyEntity, vo.getParentEntity().getId())) {
                PartyEntityNames = PartyEntityNames + partyStruct.getChildEntity().getName();
            }
        }

        return PartyEntityNames;
    }

    /**
     * 通过user查找所属大区
     *
     * @author zyl
     */
    public PartyEntity findPartyAreaByUserId(String userId) {
    	
    	PartyEntity area = null;
        
        
        while(true){ //循环条件中直接为TRUE 
        	
        	PartyEntity partyEntity = partyEntityManager.get(Long.parseLong(userId));
        	if (partyEntity != null) {
        		if (partyEntity.getId() == PartyConstants.ROOT_PARTY_TREE_ID) {   // 找到根节点，退出循环
        			break;
        		}
	        	String hql = "from PartyStruct where childEntity=? and childEntity.delFlag='" + PartyConstants.PARTY_NORMAL 
	            		+ "' and partyStructType.id=" + PartyConstants.PARTY_STRUCT_TYPE_ORG;
	
	            
	            PartyStruct vo = partyStructManager.findUnique(hql, partyEntity);
	            PartyEntity parent = vo.getParentEntity();
	        	if (parent.getPartyType().getId().equals(PartyConstants.PARTY_TYPE_AREA)) {
	        		area = parent;
	        		break;
	            } else {
	            	userId = Long.toString(parent.getId());
	            }
        	} else {
        		break;
        	}
        }
        
        return area;
        // return findUpperArea(partyEntity, vo.getParentEntity().getId());
    }

    /**
     * 通过user查找所属公司
     * @author zyl
     */
    public PartyEntity findPartyCompanyByUserId(String userId) {
    	
    	PartyEntity company = null;
        
        
        while(true){ //循环条件中直接为TRUE 
        	
        	PartyEntity partyEntity = partyEntityManager.get(Long.parseLong(userId));
        	if (partyEntity != null) {
        		if (partyEntity.getId() == PartyConstants.ROOT_PARTY_TREE_ID) {   // 找到根节点，退出循环
        			break;
        		}
	        	String hql = "from PartyStruct where childEntity=? and childEntity.delFlag='" + PartyConstants.PARTY_NORMAL 
	            		+ "' and partyStructType.id=" + PartyConstants.PARTY_STRUCT_TYPE_ORG;
	
	            
	            PartyStruct vo = partyStructManager.findUnique(hql, partyEntity);
	            PartyEntity parent = vo.getParentEntity();
	        	if (parent.getPartyType().getId().equals(PartyConstants.PARTY_TYPE_COMPANY)) {
	        		company = parent;
	        		break;
	            } else {
	            	userId = Long.toString(parent.getId());
	            }
        	} else {
        		break;
        	}
        }
        
        return company;
    	
    }
    
    /**
     * 获得上级部门.
     */
    public PartyEntity findUpperDepartment(PartyEntity child,
                                           boolean skipAdminDepartment) {
        if (child == null) {
            logger.info("child is null");

            return null;
        }

        for (PartyStruct partyStruct : child.getParentStructs()) {
            PartyEntity parent = partyStruct.getParentEntity();

            if (parent == null) {
                logger.info("parent is null, child : {} {}", child.getName(),
                        child.getId());

                continue;
            }

            if (parent.getDelFlag().equals(PersonInfoConstants.DELETE_FLAG_YES)) {
                logger.info("parent is delete, child : {} {}", child.getName(),
                        child.getId());

                continue;
            }
            
            if(partyStruct.getPartyStructType().getId()==PartyConstants.PARTY_STRUCT_TYPE_MANAGE){
            	continue;
            }
            

            logger.debug("parent : {}, child : {}", parent.getName(),
                    child.getName());
            logger.debug("admin : [{}]", partyStruct.getAdmin());
            
            System.out.println(String.format("parent : %s, child : %s", parent.getName(),
                    child.getName()));
            System.out.println("admin : "+ partyStruct.getAdmin());

            if (parent.getPartyType().getType() == PartyConstants.TYPE_ORG) {
                if (skipAdminDepartment && this.isAdmin(partyStruct)) {
                    return this
                            .findUpperDepartment(parent, skipAdminDepartment);
                } else {
                    // 不是当前部门负责人才会返回这个部门实体，否则返回再上一级部门
                    logger.debug("upper department : {}, admin : [{}]",
                            parent.getName(), partyStruct.getAdmin());

                    return parent;
                }
            }
        }

        logger.info("cannot find parent department : {} {}", child.getName(),
                child.getId());

        return null;
    }

    /**
     * 获得上级大区.
     */
    private PartyEntity findUpperArea(PartyEntity child, Long id) {
        if (child == null) {
            return null;
        }

        for (PartyStruct partyStruct : child.getParentStructs()) {
            PartyEntity parent = partyStruct.getParentEntity();

            if (parent == null) {
                continue;
            }

            if (parent.getDelFlag().equals(PersonInfoConstants.DELETE_FLAG_YES)) {
                continue;
            }
            // System.out.println(parent.getName() + "-" + parent.getPartyType().getId());
            if (parent.getPartyType().getId() == PartyConstants.PARTY_TYPE_AREA) {
                return parent;
            } else {
                // System.out.println(parent.getName());
                return this.findUpperArea(parent, id);
            }
        }
        return null;
    }

    /**
     * 获得上级部门.
     *
     * @param child 部门
     * @param id    需要匹配的部门
     * @return boolean
     */
    public boolean findUpperDepartment(PartyEntity child, Long id) {

        if (child == null) {
            logger.info("child is null");
            return false;
        }

        for (PartyStruct partyStruct : child.getParentStructs()) {
            PartyEntity parent = partyStruct.getParentEntity();

            if (parent == null) {
                logger.info("parent is null, child : {} {}", child.getName(), child.getId());
                continue;
            }

            logger.debug("id : {}", id);
            logger.debug("parent : {}, child : {}", parent.getName() + parent.getId(),
                    child.getName());
            logger.debug("admin : [{}]", partyStruct.getAdmin());

            if (parent.getPartyType().getType() == PartyConstants.TYPE_ORG) {
                if (parent.getId().equals(id)) {
                    return true;
                } else {
                    // 不是当前部门负责人才会返回这个部门实体，否则返回再上一级部门
                    return this.findUpperDepartment(parent, id);
                }
            }
        }

        logger.info("cannot find parent department : {} {}", child.getName(), child.getId());

        return false;
    }
    
    /**
     * 获得上级部门管理者.
     *
     * @param child 上一节点审批人
     * @param userId    需要匹配的管理者
     * @return boolean
     */
    public boolean findUpperDepartmentManage(PartyEntity child, Long userId) {

        if (child == null) {
            return false;
        }
        // System.out.println("本部门：" + child.getName());
        PartyEntity parent = null;
        // 取的上级部门
        for (PartyStruct partyStruct : child.getParentStructs()) {
        	PartyEntity vo = partyStruct.getParentEntity();
        	if (vo != null) {
	        	if (vo.getPartyType().getType() == PartyConstants.TYPE_ORG) {
	        		// System.out.println(vo.getName());
	        		parent = vo;
	        	}
        	}
        }
        // 查找本部门的管理者
        String hql = "from PartyStruct where parentEntity = ? and partyStructType.id=" + PartyConstants.PARTY_STRUCT_TYPE_MANAGE;
        List<PartyStruct> list = partyStructManager.find(hql, child);
        
        if (list != null && list.size() >0) {  // 本部门没有管理者
	        for (PartyStruct partyStruct : list) {
	        	PartyEntity manageUser = partyStruct.getChildEntity();
	        	// System.out.println(manageUser.getName());
	        	if (userId ==  manageUser.getId()) {
	        		return true;  // 找到管理者
	        	}
	        }
	        // 查找上级部门管理者
	        if (parent != null) {
	        	return this.findUpperDepartmentManage(parent, userId);
	        } else {
	        	return false;
	        }
        } else {
        	// 查找上级部门管理者
        	if (parent != null) {
	        	return this.findUpperDepartmentManage(parent, userId);
	        } else {
	        	return false;
	        }
        }
    }
    
    /**
     * 获得userId所属的岗位信息.
     */
    public List<PartyEntity> getPostByUserId(String userId) {

        List<PartyEntity> postList = new ArrayList<PartyEntity>();

        PartyEntity partyEntity = this.findUser(userId);

        String hql = "from PartyStruct where childEntity=? and partyStructType.id=" + PartyConstants.PARTY_STRUCT_TYPE_POSITION_USER;
        List<PartyStruct> list = partyEntityManager.find(hql, partyEntity);

        for (PartyStruct vo : list) {
        	if (vo.getParentEntity().getDelFlag().equals(PersonInfoConstants.DELETE_FLAG_NO)) {
        		postList.add(vo.getParentEntity());
        	}
        }
        return postList;

    }
    
    public List<PartyEntity> getPostContainDELFlagByUserId(String userId) {

        List<PartyEntity> postList = new ArrayList<PartyEntity>();

        String hql = "from PartyEntity where partyType.type=? and ref=?";
        PartyEntity partyEntity = partyEntityManager.findUnique(hql,
                PartyConstants.TYPE_USER, userId);
        //PartyEntity partyEntity = this.findUser(userId);

        hql = "from PartyStruct where childEntity=? and partyStructType.id=" + PartyConstants.PARTY_STRUCT_TYPE_POSITION_USER;
        List<PartyStruct> list = partyEntityManager.find(hql, partyEntity);

        for (PartyStruct vo : list) {
        	//if (vo.getParentEntity().getDelFlag().equals(PersonInfoConstants.DELETE_FLAG_NO)) {
        		postList.add(vo.getParentEntity());
        	//}
        }
        return postList;

    }

    /**
     * 获得岗位下所有人员信息.
     */
    public List<PartyEntity> getUsersByPostId(Long postId) {
    	List<PartyEntity> userList = new ArrayList<PartyEntity>();

    	PartyEntity partyEntity = partyEntityManager.get(postId);

        String hql = "from PartyStruct where parentEntity=? and partyStructType.id=" + PartyConstants.PARTY_STRUCT_TYPE_POSITION_USER;
        List<PartyStruct> list = partyEntityManager.find(hql, partyEntity);

        for (PartyStruct vo : list) {
        	if (vo.getChildEntity().getDelFlag().equals(PartyConstants.PARTY_NORMAL)) {
        		userList.add(vo.getChildEntity());
        	}
        }
        return userList;
    }
    /**
     * 获得部门下所有人员信息.
     */
    public List<PartyEntity> getUsersByDepartmentId(Long departmentId) {
    	
    	if (departmentId == null) {
            return null;
        }

    	partyList = new ArrayList<PartyEntity>();
        
        PartyEntity partyEntity = partyEntityManager.get(departmentId);
        
        try {
        	generatePartyEntity(partyEntity, PartyConstants.PARTY_STRUCT_TYPE_ORG);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }

        return partyList;
    }
    
    /*********
     * 得到组织机构上级id
     * ADD BY LILEI AT 2018.09.07
     * *******/
    public Map<String,String> getParentPartyEntityId(String partyId) {
    	Map<String,String> mapReturn=new HashMap<String, String>();
    	try{
        	if(StringUtils.isBlank(partyId)){
        		mapReturn.put("parent_id", "0");
        		mapReturn.put("position_name", "");
        		return mapReturn;
        	}
        	
        	String strParentPartyId="";
        	List<PartyStruct> partyStructs = partyStructManager.find(
                    "from PartyStruct where childEntity.id=? and partyStructType.id = ?",
                    Long.parseLong(partyId), PartyConstants.PARTY_STRUCT_TYPE_ORG);
        	if(partyStructs!=null&&partyStructs.size()>0){
        		PartyStruct partyStruct=partyStructs.get(0);
        		mapReturn.put("parent_id",String.valueOf(partyStruct.getParentEntity().getId()));
        		
        		PartyEntity partyEntity=partyEntityManager.findUniqueBy("id",Long.parseLong(partyId));
            	String strPositionNo="";
            	if(partyEntity.getPartyType().getId().equals(PartyConstants.PARTY_TYPE_POST)){
            		String strSql="SELECT * FROM party_entity_attr WHERE ID=%s";
                	List<Map<String,Object>> mapPostAttrList=jdbcTemplate.queryForList(String.format(strSql, partyEntity.getId()));
                    if(mapPostAttrList!=null&&mapPostAttrList.size()>0){
                    	strPositionNo="("+mapPostAttrList.get(0).get("positionNo").toString()+")";
                    }
            	}
            	
            	String strDepartInfo=partyConnector.findDepartmentById(partyId).getName();
            	if(strDepartInfo.equals("罗麦科技")){
            		PartyDTO partyDTO=partyConnector.findAreaById(partyId);
            		if(partyDTO!=null)
            			strDepartInfo=partyDTO.getName();
            		
            	}
            	String strCompanyInfo=partyConnector.findCompanyInfoById(partyId).getName();
            	
            	String strPositionNameInfo="";
            	if(strCompanyInfo.equals(strDepartInfo))
            		strPositionNameInfo=strCompanyInfo+"-"+partyEntity.getName()+strPositionNo;
            	else
            		strPositionNameInfo=strCompanyInfo+"-"+strDepartInfo+"-"+partyEntity.getName()+strPositionNo;
            	
            	mapReturn.put("position_name", strPositionNameInfo);
        	}
        	else {
        		mapReturn.put("parent_id", "0");
        		mapReturn.put("position_name", "");
        		
    		}
        	return mapReturn;
    	}
    	catch(Exception ex){
    		logger.info("通过人员/岗位得到上级及公司-部门-人员/岗位(岗位编号)的信息异常："+ex.getMessage()+"\r\n"+ex.getStackTrace());
    		mapReturn.put("parent_id", "0");
    		mapReturn.put("position_name", "");
    		return mapReturn;
    	}
    }
    
    /**
     * 获取人的岗位信息
     * add by lilei at 2018.12.12
     * **/
    public List<Map<String,Object>> getPositionInfo(String userId){
       List<Map<String,Object>> postionMapList=new ArrayList<Map<String,Object>>();
       if(StringUtils.isBlank(userId)){
    	   return postionMapList;
       }
       String hql="from PartyStruct where partyStructType.id=4 and childEntity.id=?";
 	   List<PartyStruct> partyStructList=partyStructManager.find(hql,Long.parseLong(userId));
 	   	if(partyStructList!=null&&partyStructList.size()>0)
 	   	{
 	   		int i=0;
 	   		for(PartyStruct partyStruct:partyStructList){
	 	   		String strSql="select * from party_entity_attr where id=%s and isRealPosition='1'";
	 	   		List<Map<String,Object>> partyAttrMapList=jdbcTemplate.queryForList(String.format(strSql,partyStruct.getParentEntity().getId()));
	 	   		if(null!=partyAttrMapList&&partyAttrMapList.size()>0){
	 	   			continue;
	 	   		}
 	   			String companyName="";		//公司
 	   			String deparmentName= ""; 	//部门
 	   			String positionName=partyStruct.getParentEntity().getName();	//岗位
 	   			
 	   			String postId=partyStruct.getParentEntity().getId().toString();
 	   			PartyEntityOrgDTO companPartyOrgDTO=partyConnector.findCompanyInfoById(postId);	//公司
 	   			if(companPartyOrgDTO!=null)
 	   				companyName=companPartyOrgDTO.getName();
 	   			
    			PartyDTO partyDto = partyConnector.findDepartmentById(postId);//部门
    			if (partyDto != null)
    				deparmentName = partyDto.getName();
 	   			
 	   			Map<String,Object> map=new HashMap<String, Object>();
 	   			if(StringUtils.isNotBlank(deparmentName)){
 	   				map.put("position", " "+(++i)+"."+companyName+"-"+deparmentName+"-"+positionName);
 	   			}else{
 	   				map.put("position", " "+(++i)+"."+positionName);
 	   			}
 	   			map.put("id", partyStruct.getParentEntity().getId());
 	   			postionMapList.add(map);
 	   		}
 	   	}
 	   	return postionMapList;
    }
    
    @SuppressWarnings("unchecked")
    private void generatePartyEntity(PartyEntity partyEntity,long partyStructTypeId) {

        try {
			List<PartyStruct> partyStructs = partyStructManager.find(
                    "from PartyStruct where parentEntity=? and partyStructType.id = ?",
                    partyEntity, partyStructTypeId);
            List<PartyEntity> partyEntities = new ArrayList<PartyEntity>();

            for (PartyStruct partyStruct : partyStructs) {
                
                PartyEntity childPartyEntity = partyStruct.getChildEntity();

                if (childPartyEntity == null) {
                    //logger.info("child party entity is null");
                    continue;
                }

                if (childPartyEntity.getDelFlag().equals(PersonInfoConstants.DELETE_FLAG_YES)) {
                    //logger.info("child party entity is delete");
                    continue;
                }
                
                if (childPartyEntity.getPartyType().getId() == PartyConstants.PARTY_TYPE_POST) {
                	continue;
                }
                
                if (childPartyEntity.getPartyType().getId() == PartyConstants.PARTY_TYPE_USER) {
                	partyList.add(childPartyEntity);
                	continue;    
                } else {
                	partyEntities.add(childPartyEntity);
                }
            }

            if (!partyEntities.isEmpty()) {
                generatePartyEntities(partyEntities, partyStructTypeId);
            }

        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
    }
    
    /**
     * 根据岗位ID/人员ID获取上级所有管理者（找到第一个公司为止）
     * ADD BY LILEI AT 2018.12.18
     * **/
    public List<String> getManagerList(String strUserOrPositionId){
    	List<String> managerIdList=new ArrayList<String>();
    	
    	if(!StringUtils.isBlank(strUserOrPositionId)){
    		//为防止死循环，我们最多查找20层（已是极限）ADD BY LILEI AT 2018.12.18
    		Long userOrPositionId=Long.parseLong(strUserOrPositionId);
    		int i=0;
    		while(i++<20){
    			String hql = "from PartyStruct where childEntity.id=? and childEntity.delFlag='" + PartyConstants.PARTY_NORMAL 
	            		+ "' and partyStructType.id=" + PartyConstants.PARTY_STRUCT_TYPE_ORG;
	            PartyStruct partyStruct = partyStructManager.findUnique(hql, userOrPositionId);
	            //找不到，直接结束
	            if(partyStruct==null){
	            	break;
	            }
	            
	            //管理关系
	            userOrPositionId=partyStruct.getParentEntity().getId();
	            String strHql="from PartyStruct where parentEntity.id=? and childEntity.delFlag='" + PartyConstants.PARTY_NORMAL
	            		+ "' and partyStructType.id=" + PartyConstants.PARTY_STRUCT_TYPE_MANAGE;
	            List<PartyStruct> manangerPartyStructList=partyStructManager.find(strHql, partyStruct.getParentEntity().getId());
	            if(null!=manangerPartyStructList&&manangerPartyStructList.size()>0){
	            	for (PartyStruct managerPartyStruct : manangerPartyStructList) {
	            		String partyEntityId=managerPartyStruct.getChildEntity().getId().toString();
	            		if(!managerIdList.contains(partyEntityId))
	            			managerIdList.add(managerPartyStruct.getChildEntity().getId().toString());
					}
	            }
	            
	            //找到第一个公司，结束
	            if(partyStruct.getParentEntity().getPartyType().getId()==PartyConstants.PARTY_TYPE_COMPANY){
	            	break;
	            }
    		}
    		
    	}
    	return managerIdList;
    }
    
    private void generatePartyEntities(List<PartyEntity> partyEntities, long partyStructTypeId) {

        try {
            for (PartyEntity partyEntity : partyEntities) {
               generatePartyEntity(partyEntity, partyStructTypeId);
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }

    }
    
    // ~ ==================================================
    @Resource
    public void setPartyEntityManager(PartyEntityManager partyEntityManager) {
        this.partyEntityManager = partyEntityManager;
    }
    
    @Resource
    public void setPartyStructManager(PartyStructManager partyStructManager) {
        this.partyStructManager = partyStructManager;
    }

    @Resource
	public void setPersonInfoManager(PersonInfoManager personInfoManager) {
		this.personInfoManager = personInfoManager;
	}
    
    @Resource
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
    
    @Resource
	public void setPartyConnector(PartyConnector partyConnector) {
		this.partyConnector = partyConnector;
	}
    
}
