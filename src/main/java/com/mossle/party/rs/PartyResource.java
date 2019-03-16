package com.mossle.party.rs;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.google.common.base.Joiner;
import com.mossle.api.keyvalue.KeyValueConnector;
import com.mossle.api.org.OrgConnector;
import com.mossle.api.party.PartyConnector;
import com.mossle.api.party.PartyDTO;
import com.mossle.api.party.PartyEntityOrgDTO;
import com.mossle.api.user.PersonInfoDTO;
import com.mossle.api.user.UserConnector;
import com.mossle.api.user.UserDTO;
import com.mossle.auth.support.DatabaseUserAuthConnector;
import com.mossle.common.utils.StringUtils;
import com.mossle.core.annotation.Log;
import com.mossle.core.auth.CurrentUserHolder;
import com.mossle.core.util.BaseDTO;
import com.mossle.party.PartyConstants;
import com.mossle.party.persistence.domain.OrgLogEntity;
import com.mossle.party.persistence.domain.PartyEntity;
import com.mossle.party.persistence.domain.PartyStruct;
import com.mossle.party.persistence.domain.PartyType;
import com.mossle.party.persistence.manager.OrgLogManager;
import com.mossle.party.persistence.manager.PartyEntityManager;
import com.mossle.party.persistence.manager.PartyStructManager;
import com.mossle.party.persistence.manager.PartyTypeManager;
import com.mossle.party.service.PartyService;
import com.mossle.pim.persistence.domain.WorkReportForward;
import com.mossle.pim.persistence.manager.WorkReportForwardManager;
import com.mossle.user.PersonInfoConstants;
import com.mossle.user.persistence.domain.AccountInfo;
import com.mossle.user.persistence.domain.PersonAttendanceMachine;
import com.mossle.user.persistence.domain.PersonInfo;
import com.mossle.user.persistence.manager.AccountInfoManager;
import com.mossle.user.persistence.manager.PersonInfoManager;
import com.sun.mail.handlers.image_gif;
import com.mossle.util.DateUtil;
import com.mossle.util.StringUtil;

import org.apache.poi.hssf.record.PageBreakRecord.Break;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

@Component
@Path("party")
public class PartyResource {
    private static Logger logger = LoggerFactory.getLogger(PartyResource.class);
    private PartyTypeManager partyTypeManager;
    private PartyEntityManager partyEntityManager;
    private PartyStructManager partyStructManager;
    private AccountInfoManager accountInfoManager;
    private PartyService partyService;
    private UserConnector userConnector;
    private CurrentUserHolder currentUserHolder;
    private WorkReportForwardManager workReportForwardManager;
    private JdbcTemplate jdbcTemplate;
    private PartyConnector partyConnector;
    @Autowired
    private PersonInfoManager personInfoManager;
    
    private KeyValueConnector keyValueConnector;
    
    private OrgLogManager orgLogManager;

    @GET
    @Path("types")
    @Produces(MediaType.APPLICATION_JSON)
    public List<PartyTypeDTO> getAllPartyTypes() {
        List<PartyType> partyTypes = partyTypeManager.getAll();

        List<PartyTypeDTO> partyTypeDtos = new ArrayList<PartyTypeDTO>();

        for (PartyType partyType : partyTypes) {
            PartyTypeDTO partyTypeDto = new PartyTypeDTO();
            partyTypeDto.setId(partyType.getId());
            partyTypeDto.setName(partyType.getName());
            partyTypeDtos.add(partyTypeDto);
        }

        return partyTypeDtos;
    }

    @GET
    @Path("entities")
    @Produces(MediaType.APPLICATION_JSON)
    public List<PartyEntityDTO> getPartyEntitiesByType(
            @QueryParam("typeId") long typeId) {
        List<PartyEntity> partyEntities = partyEntityManager.findBy(
                "partyType.id", typeId);

        List<PartyEntityDTO> partyEntityDtos = new ArrayList<PartyEntityDTO>();

        for (PartyEntity partyEntity : partyEntities) {

            // zyl 2017-08-15  判断是否删除
            if (partyEntity.getDelFlag().equals(PersonInfoConstants.DELETE_FLAG_NO)) {
                continue;
            }

            if (partyEntity.getParentStructs().size() == 1) {
                PartyStruct partyStruct = partyEntity.getParentStructs()
                        .iterator().next();

                if (partyStruct.getParentEntity() == null) {
                    logger.info("skip top entity : {}, {}",
                            partyEntity.getId(), partyEntity.getName());

                    continue;
                }
            }

            PartyEntityDTO partyEntityDto = new PartyEntityDTO();
            partyEntityDto.setId(partyEntity.getId());
            partyEntityDto.setName(partyEntity.getName());
            partyEntityDto.setRef(partyEntity.getRef());
            partyEntityDtos.add(partyEntityDto);
        }

        PartyType partyType = partyTypeManager.get(typeId);

        /*if (partyType.getType() != 2) {
            return partyEntityDtos;
        }*/

        // 如果是岗位，按名称去重
        Set<String> names = new HashSet<String>();
        List<PartyEntityDTO> list = new ArrayList<PartyEntityDTO>();

        for (PartyEntityDTO partyEntityDto : partyEntityDtos) {
            if (names.contains(partyEntityDto.getName())) {
                list.add(partyEntityDto);

                continue;
            }

            names.add(partyEntityDto.getName());
        }

        partyEntityDtos.removeAll(list);

        return partyEntityDtos;
    }

    @POST
    @Path("tree")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Map> tree(
            @QueryParam("partyStructTypeId") long partyStructTypeId) {
        List<PartyEntity> partyEntities = partyService
                .getTopPartyEntities(partyStructTypeId);

        return generatePartyEntities(partyEntities, partyStructTypeId);
    }

    @POST
    @Path("treeNoPost")
    @Produces(MediaType.APPLICATION_JSON)
    /**
     * 查询组织机构，不包括岗位 
     * @author zyl
     * @param partyStructTypeId
     * @return
     */
    public List<Map> treeNoPost(
            @QueryParam("partyStructTypeId") long partyStructTypeId) {
        List<PartyEntity> partyEntities = partyService
                .getTopPartyEntities(partyStructTypeId);

        return generatePartyEntities(partyEntities, partyStructTypeId, false);
    }
    
    @POST
    @Path("treeNoPostNoCompanyChecked")
    @Produces(MediaType.APPLICATION_JSON)
    /**
     * 查询组织机构，不包括岗位 
     * @author zyl
     * @param partyStructTypeId
     * @return
     */
    public List<Map> treeNoPostNoCompanyChecked(
    		@RequestParam(value = "id", required = false) String id,
            @QueryParam("partyStructTypeId") long partyStructTypeId) {
        List<PartyEntity> partyEntities = partyService
                .getTopPartyEntities(partyStructTypeId);

        return generatePartyEntitiesNoCompanyChecked(partyEntities, partyStructTypeId, false);
    }
    
    @POST
    @Path("treeNoPostCompanyChecked")
    @Produces(MediaType.APPLICATION_JSON)
    /**
     * 查询组织机构，不包括岗位 
     * @author zyl
     * @param partyStructTypeId
     * @return
     */
    public List<Map> treeNoPostCompanyChecked(
    		@RequestParam(value = "id", required = false) String id,
            @QueryParam("partyStructTypeId") long partyStructTypeId,
            @QueryParam("partyTypeId") long  partyTypeId) {
        List<PartyEntity> partyEntities = partyService
                .getTopPartyEntities(partyStructTypeId);

        return generatePartyEntitiesCompanyChecked(partyEntities, partyStructTypeId,false,partyTypeId);
    }
    
    @POST
    @Path("treeForAttendanceRecordSet")
    @Produces(MediaType.APPLICATION_JSON)
    /**
     * 查询组织机构，和人员    考勤时间设置用
     * @author zyl
     * @param partyStructTypeId
     * @return
     */
    public List<Map> treeForAttendanceRecordSet(
    		@RequestParam(value = "id", required = false) String id,
            @QueryParam("partyStructTypeId") long partyStructTypeId,
            @QueryParam("partyTypeId") long  partyTypeId) {
        List<PartyEntity> partyEntities = partyService
                .getTopPartyEntities(partyStructTypeId);

        /*************************************************
		 * 去除虚拟账号
		 * 1.超级管理员虚拟人员-2
		 * 2.系统管理员-对应角色区分
		 * 3.经销商虚拟人员-4
		 * 4.机器人虚拟人员-3
		 * 5.测试用户
		 * ***********************************************/
		String strSerchRemoveId=","+PartyConstants.ADMIN_USER_ID+","
								+PartyConstants.SYSTEM_ROBOT_ID+","
								+PartyConstants.JXS_ID;
		PersonInfo personInfoTest=personInfoManager.findUniqueBy("username", "testuser");
		if(personInfoTest!=null)
			strSerchRemoveId+=","+personInfoTest.getId().toString();
		
		PersonInfo personInfoTest2=personInfoManager.findUniqueBy("username", "releasetest");
		if(personInfoTest2!=null)
			strSerchRemoveId+=","+personInfoTest2.getId().toString();
		
		String strSystemAdminIds="";
		List<String> systemAdminIdList=null;
		//查询属于角色ID为2(系统管理员)的所有用户ID
		String strSql="SELECT US.ref FROM AUTH_USER_ROLE UR "
				+" INNER JOIN AUTH_USER_STATUS US ON UR.USER_STATUS_ID=US.ID"
				+" WHERE ROLE_ID=2";
		systemAdminIdList=jdbcTemplate.queryForList(strSql, String.class);
		if(systemAdminIdList!=null&&systemAdminIdList.size()>0)
			strSystemAdminIds=Joiner.on(",").join(systemAdminIdList);
		
		if(!strSystemAdminIds.equals(""))
			strSerchRemoveId+=","+strSystemAdminIds+",";	
		
        return generatePartyEntitiesForAttendance(partyEntities, partyStructTypeId,false,partyTypeId,strSerchRemoveId);
    } 
   
//    @POST
//    @Path("treeCompanyChecked")
//    @Produces(MediaType.APPLICATION_JSON)
//    /**
//     * 查询组织机构，不包括岗位 
//     * @author zyl
//     * @param partyStructTypeId
//     * @return
//     */
//    public List<Map> treeCompanyChecked(
//    		@RequestParam(value = "id", required = false) String id,
//            @QueryParam("partyStructTypeId") long partyStructTypeId) {
//        List<PartyEntity> partyEntities = partyService
//                .getTopPartyEntities(partyStructTypeId);
//
//        return generatePartyEntitiesCompanyChecked(partyEntities, partyStructTypeId, false);
//    }
    
    

    @POST
    @Path("treeNoAuth")
    @Produces(MediaType.APPLICATION_JSON)
    /**
     * 查询组织机构，不授权限控制 
     * @author zyl
     * @param partyStructTypeId
     * @return
     */
    public List<Map> treeNoAuth(
            @QueryParam("partyStructTypeId") long partyStructTypeId) {
        List<PartyEntity> partyEntities = partyService
                .getTopPartyEntities(partyStructTypeId);

        return generatePartyEntities(partyEntities, partyStructTypeId, false, false, false);
    }


    @POST
    @Path("treeAuth")
    @Produces(MediaType.APPLICATION_JSON)
    /**
     * 查询组织机构，受权限控制
     * @author
     * @param partyStructTypeId
     * @return
     */
    public List<Map> treeAuth(
            @QueryParam("partyStructTypeId") long partyStructTypeId,
            @QueryParam("rootNodeIsCheckbox") boolean rootNodeIsCheckbox) {
        List<PartyEntity> partyEntities = partyService
                .getTopPartyEntities(partyStructTypeId);
			
        
        return generatePartyEntitiesForNotice(partyEntities, partyStructTypeId, false, true, rootNodeIsCheckbox);
    }
    

    public List<Map> generatePartyEntitiesForAttendance(List<PartyEntity> partyEntities,
            long partyStructTypeId, boolean viewPost,Long partyTypeId,String strSerchRemoveId) {
		if (partyEntities == null) {
		return null;
		}
		
		List<Map> list = new ArrayList<Map>();
		
		try {
		for (PartyEntity partyEntity : partyEntities) {
		
		if (viewPost) {
		list.add(generatePartyEntityCompanyCheckedForAttendance(partyEntity, partyStructTypeId, viewPost, false, false, partyTypeId,strSerchRemoveId));
		} else {
		if (PartyConstants.TYPE_POSITION != partyEntity.getPartyType().getType()) {
			Map<String, Object> map = generatePartyEntityCompanyCheckedForAttendance(partyEntity, partyStructTypeId, viewPost, false, false, partyTypeId,strSerchRemoveId);
			//去除虚拟账号
			if(!strSerchRemoveId.contains(","+map.get("id")+",")){
				list.add(map);
			}
			
		}
		}
		
		}
		} catch (Exception ex) {
		logger.error(ex.getMessage(), ex);
		}
		
		return list;
	}
    
    public Map<String, Object> generatePartyEntityCompanyCheckedForAttendance(PartyEntity partyEntity,
            long partyStructTypeId, boolean viewPost, boolean auth, boolean rootNodeIsCheckbox
            ,Long partyTypeId,String strSerchRemoveId) {
			Map<String, Object> map = new HashMap<String, Object>();
			
			// zyl 2017-07-12
			Long accountId = Long.parseLong(currentUserHolder.getUserId());
			
			try {
			map.put("id", partyEntity.getId());
			map.put("name", partyEntity.getName());
			map.put("ref", partyEntity.getRef());
			map.put("typeid", partyEntity.getPartyType().getType());
//			if(partyEntity.getPartyType().getId().equals(PartyConstants.PARTY_TYPE_COMPANY)){
//				map.put("nocheck",true);
//			}
			
//			if (!rootNodeIsCheckbox) {
//				if (PartyConstants.ROOT_PARTY_TREE_ID.equals(partyEntity.getId())) {
//					map.put("nocheck", true);
//				}
//			}
			List<PartyStruct> partyStructs = partyStructManager.find(
			"from PartyStruct where parentEntity=? order by priority",
			partyEntity);
			List<PartyEntity> partyEntities = new ArrayList<PartyEntity>();
		
			for (PartyStruct partyStruct : partyStructs) {
			if (partyStruct.getPartyStructType().getId() == partyStructTypeId) {
			PartyEntity childPartyEntity = partyStruct.getChildEntity();
			
			if (childPartyEntity == null) {
				logger.info("child party entity is null");
				continue;
			}
			
			if (childPartyEntity.getDelFlag().equals(PersonInfoConstants.DELETE_FLAG_YES)) {
				// logger.info("child party entity is delete");
				continue;
			}
		
			//if (childPartyEntity.getPartyType().getType() != PartyConstants.TYPE_USER) {
			
			if (auth) {
			
			//if (accountId.equals(PartyConstants.ADMIN_USER_ID)) {   // 超级管理员
			//partyEntities.add(childPartyEntity);
			//} else {
			if (!PartyConstants.ROOT_PARTY_TREE_ID.equals(partyStruct.getParentEntity().getId())) {
				
			partyEntities.add(childPartyEntity);
			} else {
			AccountInfo accountInfo = accountInfoManager.get(accountId);
			
			PartyEntity vo = partyEntityManager.get(Long.parseLong(accountInfo.getCode()));
			
			List<PartyStruct> list = partyStructManager.find(
			     "from PartyStruct where childEntity=?", vo);
			
			for (PartyStruct item : list) {
			 if ("1".equals(item.getChildEntity().getId())) {
			     partyEntities.add(childPartyEntity);
			 }
			
			 // 判断是否是分公司管理员的公司
			 if (childPartyEntity.getId().equals(item.getParentEntity().getId())) {
			     partyEntities.add(childPartyEntity);
				 }
				 break;
			}
			
			}
			//}
			} else {
				String name1 = 	childPartyEntity.getName();
				Long cid1 = childPartyEntity.getPartyType().getId();
				partyEntities.add(childPartyEntity);
				}
			//}
			
			}
			}
			
			if (partyEntities.isEmpty()) {
			map.put("open", false);
			} else {
			if (PartyConstants.ROOT_PARTY_TREE_ID.equals(partyEntity.getId())) {
			map.put("open", true);
			} else {
			map.put("open", false);
			}
			map.put("children",
					generatePartyEntitiesForAttendance(partyEntities, partyStructTypeId, viewPost, partyTypeId,strSerchRemoveId));
			}
			
			return map;
			
			} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			
			return map;
			}
}
    
    
    
    @GET
    @Path("getWorkNumber")
    @Produces(MediaType.APPLICATION_JSON)
    /**
     * 查询组织机构，受权限控制
     * @author
     * @param partyStructTypeId
     * @return
     */
    public Map getWorkNumber(
            @QueryParam("partyEntityId") long partyEntityId) {
        Map<String,Object> map=new HashMap<String, Object>();
    	String strWorkNumberPrefix=partyConnector.getWorkNumerPrefix(String.valueOf(partyEntityId));
    	map.put("workNumberPrefix", strWorkNumberPrefix);
    	return map;
    }

    public List<Map> generatePartyEntities(List<PartyEntity> partyEntities,
                                           long partyStructTypeId) {
        if (partyEntities == null) {
            return null;
        }

        List<Map> list = new ArrayList<Map>();

        try {
            for (PartyEntity partyEntity : partyEntities) {
                list.add(generatePartyEntity(partyEntity, partyStructTypeId, true, true, false));
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }

        return list;
    }
    
    @GET
    @Path("person-info-getpositionno")
    @Produces(MediaType.APPLICATION_JSON)
    @Log(desc = "获取岗位编号信息", action = "search", operationDesc = "组织结构-获取岗位编号信息")
    @SuppressWarnings("unchecked")
    public BaseDTO getPositionNo(
            @QueryParam("postId") Long postId
    ) {
        BaseDTO result = new BaseDTO();
        try {
            if (postId == null || postId < 1) {
                result.setCode(500);
                logger.debug("操作失败-获取参数id错误");
                result.setMessage("获取参数错误");
                return result;
            }
            
            String strSql=String.format("SELECT * FROM party_entity_attr WHERE ID=%s",postId);
            Map<String,Object> mapAttr=new HashMap<String, Object>();
            List<Map<String,Object>> mapAttrList=jdbcTemplate.queryForList(strSql);
            
        	if(mapAttrList!=null){
        		if(mapAttrList.size()>0){
        			mapAttr=mapAttrList.get(0);
        			String strPositionRealIds=mapAttr.get("positionRealIds").toString();
        			String strPositionRealNames="";
        			if(!com.mossle.core.util.StringUtils.isBlank(strPositionRealIds)){
                		String[] strIds=strPositionRealIds.split(",");
                		for(String id:strIds){
                			PartyEntity postPartyEntity=partyEntityManager.findUniqueBy("id",Long.parseLong(id));
                			strPositionRealNames+=postPartyEntity.getName()+",";
                		}
                		if(!strPositionRealNames.equals("")){
                			strPositionRealNames=strPositionRealNames.substring(0, strPositionRealNames.length()-1);
                		}
                	}
        			mapAttr.put("postName", strPositionRealNames);
        		}
        	}
            result.setData(mapAttr);
            result.setCode(200);
            result.setMessage("操作成功！");

        } catch (Exception e) {
            result.setCode(500);
            result.setMessage("获取数据出错");
            logger.error("获取岗位编号数据操作操作-：" + e.getMessage() + "\r\n" + e.fillInStackTrace());
        }
        return result;
    }
    
    @POST
    @Path("person-info-setpositionno")
    @Produces(MediaType.APPLICATION_JSON)
    @Log(desc = "设置岗位编号信息", action = "update", operationDesc = "组织结构-设置岗位编号信")
    @SuppressWarnings("unchecked")
    public BaseDTO setPositionNo(
            @FormParam("postId") Long postId,
            //@FormParam("positionNo") String positionNo,
            @FormParam("isRealPosition") String isRealPosition,
            @FormParam("positionRealIds") String positionRealIds
    ) throws UnsupportedEncodingException {
        BaseDTO result = new BaseDTO();
        try {
            if (postId == null || postId < 1) {
                result.setCode(500);
                logger.debug("操作失败-获取参数id错误");
                result.setMessage("获取参数错误");
                return result;
            }
            
            /*if (StringUtils.isBlank(positionNo)) {
                result.setCode(500);
                result.setMessage("请输入编号编号！");
                return result;
            }*/
            
            if(isRealPosition.equals("1")){
            	if (StringUtils.isBlank(positionRealIds)) {
                    result.setCode(500);
                    result.setMessage("设置岗位为虚拟岗，请选择对应真实岗位！");
                    return result;
                }
            }
            
            //AJAX过来的数据中有%，呵呵，替换了
            //if(positionRealIds.contains("%"))
        	positionRealIds=URLDecoder.decode(positionRealIds, "UTF-8");
            
            /*List<Map<String,Object>> mapAttrListExists=jdbcTemplate.queryForList("select * from party_entity_attr where ID<>? and positionNo=?",
            												postId,
            												positionNo);
            if(mapAttrListExists!=null&&mapAttrListExists.size()>0){
            	result.setCode(500);
                result.setMessage("岗位编号已存在，请重新输入设置！");
                return result;
            }*/
            
            List<Map<String,Object>> mapAttrList=jdbcTemplate.queryForList("select * from party_entity_attr where ID=?",
					postId);
            if(mapAttrList!=null&&mapAttrList.size()>0){
            	String strSql=String.format("update party_entity_attr set isRealPosition='%s',positionRealIds='%s' where ID=%s", 
            								isRealPosition,
            								positionRealIds,
            								postId);
            	keyValueConnector.updateBySql(strSql);
            	//jdbcTemplate.update(strSql);
            }
            /*else{
            	String strSql=String.format("insert into party_entity_attr(ID,positionNo,isRealPosition,positionRealIds) values(%s,'%s','%s','%s')",
            								postId,
            								positionNo,
            								isRealPosition,
            								positionRealIds);
            	//jdbcTemplate.update(strSql);
            	keyValueConnector.updateBySql(strSql);
            }*/
            result.setCode(200);
            result.setMessage("操作成功！");

        } catch (ArithmeticException e) {
            result.setCode(500);
            result.setMessage("操作出错");
            logger.error("设置岗位编号操作-：" + e.getMessage() + "\r\n" + e.fillInStackTrace());
        }
        return result;
    }

    //generatePartyEntityNoComanyChecked
    public List<Map> generatePartyEntitiesCompanyChecked(List<PartyEntity> partyEntities,
            long partyStructTypeId, boolean viewPost,Long partyTypeId) {
		if (partyEntities == null) {
		return null;
		}
		
		List<Map> list = new ArrayList<Map>();
		
		try {
		for (PartyEntity partyEntity : partyEntities) {
		
		if (viewPost) {
		list.add(generatePartyEntityCompanyChecked(partyEntity, partyStructTypeId, viewPost, false, false, partyTypeId));
		} else {
		if (PartyConstants.TYPE_POSITION != partyEntity.getPartyType().getType()) {
		list.add(generatePartyEntityCompanyChecked(partyEntity, partyStructTypeId, viewPost, false, false, partyTypeId));
		}
		}
		
		}
		} catch (Exception ex) {
		logger.error(ex.getMessage(), ex);
		}
		
		return list;
	}

    
    public List<Map> generatePartyEntitiesNoCompanyChecked(List<PartyEntity> partyEntities,
            long partyStructTypeId, boolean viewPost) {
		if (partyEntities == null) {
		return null;
		}
		
		List<Map> list = new ArrayList<Map>();
		
		try {
		for (PartyEntity partyEntity : partyEntities) {
		
		if (viewPost) {
		list.add(generatePartyEntityNoCompanyChecked(partyEntity, partyStructTypeId, viewPost, false, false));
		} else {
		if (PartyConstants.TYPE_POSITION != partyEntity.getPartyType().getType()) {
		list.add(generatePartyEntityNoCompanyChecked(partyEntity, partyStructTypeId, viewPost, false, false));
		}
		}
		
		}
		} catch (Exception ex) {
		logger.error(ex.getMessage(), ex);
		}
		
		return list;
	}
    
    
    
    
    
    
    
    /**
     * 在树中是否显示岗位
     *
     * @param partyEntities
     * @param partyStructTypeId
     * @param viewPost          是否包含岗位
     * @return
     */
    public List<Map> generatePartyEntities(List<PartyEntity> partyEntities,
                                           long partyStructTypeId, boolean viewPost) {
        if (partyEntities == null) {
            return null;
        }

        List<Map> list = new ArrayList<Map>();

        try {
            for (PartyEntity partyEntity : partyEntities) {

                if (viewPost) {
                    list.add(generatePartyEntity(partyEntity, partyStructTypeId, viewPost, false, false));
                } else {
                    if (PartyConstants.TYPE_POSITION != partyEntity.getPartyType().getType()) {
                        list.add(generatePartyEntity(partyEntity, partyStructTypeId, viewPost, false, false));
                    }
                }

            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }

        return list;
    }

    /**
     * 在树中是否显示岗位
     *
     * @param partyEntities
     * @param partyStructTypeId
     * @param viewPost          是否包含岗位
     * @param auth              是否控制权限
     * @return
     */
    public List<Map> generatePartyEntities(List<PartyEntity> partyEntities,
                                           long partyStructTypeId, boolean viewPost, boolean auth, boolean rootNodeIsCheckbox) {
        if (partyEntities == null) {
            return null;
        }

        List<Map> list = new ArrayList<Map>();
        try {
            for (PartyEntity partyEntity : partyEntities) {

                if (viewPost) {
                    list.add(generatePartyEntity(partyEntity, partyStructTypeId, viewPost, auth, rootNodeIsCheckbox));
                } else {
                    if (PartyConstants.TYPE_POSITION != partyEntity.getPartyType().getType()) {
                        list.add(generatePartyEntity(partyEntity, partyStructTypeId, viewPost, auth, rootNodeIsCheckbox));
                    }
                }
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }

        return list;
    }
    
    /**
     * 在树中是否显示岗位
     *
     * @param partyEntities
     * @param partyStructTypeId
     * @param viewPost          是否包含岗位
     * @param auth              是否控制权限
     * @return
     */
    public List<Map> generatePartyEntitiesForNotice(List<PartyEntity> partyEntities,
                                           long partyStructTypeId, boolean viewPost, boolean auth, boolean rootNodeIsCheckbox) {
        if (partyEntities == null) {
            return null;
        }
        
        List<Map> list = new ArrayList<Map>();
        try {
            for (PartyEntity partyEntity : partyEntities) {
            	Map<String,Object> map=new HashMap<String,Object>();
                map.put("id", partyEntity.getId());
        		map.put("name", partyEntity.getName());
        		map.put("ref", partyEntity.getRef());
        		
        		if (!rootNodeIsCheckbox) {
        			if (PartyConstants.ROOT_PARTY_TREE_ID.equals(partyEntity.getId())) {
        				map.put("nocheck", true);
        			}
        		}
        		if (partyEntities.isEmpty()) {
        			map.put("open", false);
        		} else {
        			if (PartyConstants.ROOT_PARTY_TREE_ID.equals(partyEntity.getId())) {
        				map.put("open", true);
        			} else {
        				map.put("open", false);
        			}
        			/*map.put("children",
        				generatePartyEntitiesForNotice(partyEntities, partyStructTypeId, viewPost));*/
        		}
        		list.add(map);
        		PartyDTO partyDTO=partyConnector.getCompanyOrAreaByUserId(currentUserHolder.getUserId());
        		
        		partyEntity=partyEntityManager.findUniqueBy("id", Long.valueOf(partyDTO.getId()));
            	
        		List<Map> childList = new ArrayList<Map>();
                if (viewPost) {
                	//map.put("children",
                			//generatePartyEntityForNotice(partyEntity, partyStructTypeId, viewPost, auth, rootNodeIsCheckbox));
                	childList.add(generatePartyEntityForNotice(partyEntity, partyStructTypeId, viewPost, auth, rootNodeIsCheckbox));
                } else {
                    if (PartyConstants.TYPE_POSITION != partyEntity.getPartyType().getType()) {
                    	//map.put("children",
                    			//generatePartyEntityForNotice(partyEntity, partyStructTypeId, viewPost, auth, rootNodeIsCheckbox));
                    	childList.add(generatePartyEntityForNotice(partyEntity, partyStructTypeId, viewPost, auth, rootNodeIsCheckbox));
                    }
                }
                map.put("children",childList);
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
        return list;
    }
    
    
    public Map<String, Object> generatePartyEntityForNotice(PartyEntity partyEntity,
            long partyStructTypeId, boolean viewPost, boolean auth, boolean rootNodeIsCheckbox) {
		Map<String, Object> map = new HashMap<String, Object>();
		
		// zyl 2017-07-12
		Long accountId = Long.parseLong(currentUserHolder.getUserId());
		try {
			map.put("id", partyEntity.getId());
			map.put("name", partyEntity.getName());
			map.put("ref", partyEntity.getRef());
			
			if (!rootNodeIsCheckbox) {
				if (PartyConstants.ROOT_PARTY_TREE_ID.equals(partyEntity.getId())) {
					map.put("nocheck", true);
				}
			}
			List<PartyStruct> partyStructs = partyStructManager.find(
			"from PartyStruct where parentEntity=? order by priority",
			partyEntity);
			List<PartyEntity> partyEntities = new ArrayList<PartyEntity>();
			
			for (PartyStruct partyStruct : partyStructs) {
				if (partyStruct.getPartyStructType().getId() == partyStructTypeId) {
					PartyEntity childPartyEntity = partyStruct.getChildEntity();
					if (childPartyEntity == null) {
						logger.info("child party entity is null");
						continue;
					}
					
					if (childPartyEntity.getDelFlag().equals(PersonInfoConstants.DELETE_FLAG_YES)) {
						// logger.info("child party entity is delete");
						continue;
					}
					
					if (childPartyEntity.getPartyType().getType() != PartyConstants.TYPE_USER) {
						if (auth) {
							//if (accountId.equals(PartyConstants.ADMIN_USER_ID)) {   // 超级管理员
							//partyEntities.add(childPartyEntity);
							//} else {
							if (!PartyConstants.ROOT_PARTY_TREE_ID.equals(partyStruct.getParentEntity().getId())) {
								partyEntities.add(childPartyEntity);
							} else {
								AccountInfo accountInfo = accountInfoManager.get(accountId);
								
								PartyEntity vo = partyEntityManager.get(Long.parseLong(accountInfo.getCode()));
								
								List<PartyStruct> list = partyStructManager.find(
								     "from PartyStruct where childEntity=?", vo);
								
								for (PartyStruct item : list) {
									 if ("1".equals(item.getChildEntity().getId())) {
									     partyEntities.add(childPartyEntity);
									 }
									
									 // 判断是否是分公司管理员的公司
									 if (childPartyEntity.getId().equals(item.getParentEntity().getId())) {
									     partyEntities.add(childPartyEntity);
									 }
									 break;
								}
							}
						} else {
							partyEntities.add(childPartyEntity);
						}
					}
				}
			}
			
			if (partyEntities.isEmpty()) {
				map.put("open", false);
			} else {
				if (PartyConstants.ROOT_PARTY_TREE_ID.equals(partyEntity.getId())) {
					map.put("open", true);
				} else {
					map.put("open", false);
				}
				map.put("children",
					generatePartyEntitiesForNotice(partyEntities, partyStructTypeId, viewPost));
			}
			return map;
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			return map;
		}
	}
    
    /**
     * 在树中是否显示岗位
     *
     * @param partyEntities
     * @param partyStructTypeId
     * @param viewPost          是否包含岗位
     * @return
     */
    public List<Map> generatePartyEntitiesForNotice(List<PartyEntity> partyEntities,
                                           long partyStructTypeId, boolean viewPost) {
        if (partyEntities == null) {
            return null;
        }

        List<Map> list = new ArrayList<Map>();

        try {
            for (PartyEntity partyEntity : partyEntities) {

                if (viewPost) {
                    list.add(generatePartyEntity(partyEntity, partyStructTypeId, viewPost, false, false));
                } else {
                    if (PartyConstants.TYPE_POSITION != partyEntity.getPartyType().getType()) {
                        list.add(generatePartyEntity(partyEntity, partyStructTypeId, viewPost, false, false));
                    }
                }

            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }

        return list;
    }
    
    	

    public Map<String, Object> generatePartyEntity(PartyEntity partyEntity,
                                                   long partyStructTypeId, boolean viewPost, boolean auth, boolean rootNodeIsCheckbox) {
        Map<String, Object> map = new HashMap<String, Object>();

        // zyl 2017-07-12
        Long accountId = Long.parseLong(currentUserHolder.getUserId());

        try {
            map.put("id", partyEntity.getId());
            map.put("name", partyEntity.getName());
            map.put("ref", partyEntity.getRef());

            if (!rootNodeIsCheckbox) {
                if (PartyConstants.ROOT_PARTY_TREE_ID.equals(partyEntity.getId())) {
                    map.put("nocheck", true);
                }
            }
            List<PartyStruct> partyStructs = partyStructManager.find(
                    "from PartyStruct where parentEntity=? order by priority",
                    partyEntity);
            List<PartyEntity> partyEntities = new ArrayList<PartyEntity>();

            for (PartyStruct partyStruct : partyStructs) {
                if (partyStruct.getPartyStructType().getId() == partyStructTypeId) {
                    PartyEntity childPartyEntity = partyStruct.getChildEntity();

                    if (childPartyEntity == null) {
                        logger.info("child party entity is null");

                        continue;
                    }

                    if (childPartyEntity.getDelFlag().equals(PersonInfoConstants.DELETE_FLAG_YES)) {
                        // logger.info("child party entity is delete");

                        continue;
                    }

                    if (childPartyEntity.getPartyType().getType() != PartyConstants.TYPE_USER) {


                        if (auth) {

                            //if (accountId.equals(PartyConstants.ADMIN_USER_ID)) {   // 超级管理员
                                //partyEntities.add(childPartyEntity);
                            //} else {
                                if (!PartyConstants.ROOT_PARTY_TREE_ID.equals(partyStruct.getParentEntity().getId())) {
                                    partyEntities.add(childPartyEntity);
                                } else {
                                    AccountInfo accountInfo = accountInfoManager.get(accountId);

                                    PartyEntity vo = partyEntityManager.get(Long.parseLong(accountInfo.getCode()));

                                    List<PartyStruct> list = partyStructManager.find(
                                            "from PartyStruct where childEntity=?", vo);

                                    for (PartyStruct item : list) {
                                        if ("1".equals(item.getChildEntity().getId())) {
                                            partyEntities.add(childPartyEntity);
                                        }

                                        // 判断是否是分公司管理员的公司
                                        if (childPartyEntity.getId().equals(item.getParentEntity().getId())) {
                                            partyEntities.add(childPartyEntity);
                                        }
                                        break;
                                    }

                                }
                            //}
                        } else {
                            partyEntities.add(childPartyEntity);
                        }
                    }

                }
            }

            if (partyEntities.isEmpty()) {
                map.put("open", false);
            } else {
                if (PartyConstants.ROOT_PARTY_TREE_ID.equals(partyEntity.getId())) {
                    map.put("open", true);
                } else {
                    map.put("open", false);
                }
                map.put("children",
                        generatePartyEntities(partyEntities, partyStructTypeId, viewPost));
            }

            return map;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            return map;
        }
    }
    
    public Map<String, Object> generatePartyEntityCompanyChecked(PartyEntity partyEntity,
            long partyStructTypeId, boolean viewPost, boolean auth, boolean rootNodeIsCheckbox
            ,Long partyTypeId) {
    	Map<String, Object> map = new HashMap<String, Object>();
			
		// zyl 2017-07-12
		Long accountId = Long.parseLong(currentUserHolder.getUserId());
			
		try {
			map.put("id", partyEntity.getId());
			map.put("name", partyEntity.getName());
			map.put("ref", partyEntity.getRef());
//			if(partyEntity.getPartyType().getId().equals(PartyConstants.PARTY_TYPE_COMPANY)){
//				map.put("nocheck",true);
//			}
			
//			if (!rootNodeIsCheckbox) {
//				if (PartyConstants.ROOT_PARTY_TREE_ID.equals(partyEntity.getId())) {
//					map.put("nocheck", true);
//				}
//			}
			List<PartyStruct> partyStructs = partyStructManager.find(
			"from PartyStruct where parentEntity=? order by priority",
			partyEntity);
			List<PartyEntity> partyEntities = new ArrayList<PartyEntity>();
			
			for (PartyStruct partyStruct : partyStructs) {
			if (partyStruct.getPartyStructType().getId() == partyStructTypeId) {
			PartyEntity childPartyEntity = partyStruct.getChildEntity();
			
			if (childPartyEntity == null) {
				logger.info("child party entity is null");
				
				continue;
			}
			
			if (childPartyEntity.getDelFlag().equals(PersonInfoConstants.DELETE_FLAG_YES)) {
				// logger.info("child party entity is delete");
				
				continue;
			}
			
			//若编辑的是部门，上级机构  只显示公司和大区
			if ((partyTypeId==3)&& (childPartyEntity.getPartyType().getId()==3||childPartyEntity.getPartyType().getId()==4)) {
					continue;
			}
			
			//若编辑的是公司，上级机构  只显示公司和大区
			if (partyTypeId==2&& (childPartyEntity.getPartyType().getId()==3||childPartyEntity.getPartyType().getId()==4)) {
					continue;
			}
			
			//若编辑的是大区，上级机构  只显示公司
			if (partyTypeId==6&& (childPartyEntity.getPartyType().getId()==3||childPartyEntity.getPartyType().getId()==4||childPartyEntity.getPartyType().getId()==6)) {
					continue;
			}
			
			if (childPartyEntity.getPartyType().getType() != PartyConstants.TYPE_USER) {
				if (auth) {
					if (!PartyConstants.ROOT_PARTY_TREE_ID.equals(partyStruct.getParentEntity().getId())) {
						partyEntities.add(childPartyEntity);
					} else {
						AccountInfo accountInfo = accountInfoManager.get(accountId);
				
						PartyEntity vo = partyEntityManager.get(Long.parseLong(accountInfo.getCode()));
				
						List<PartyStruct> list = partyStructManager.find("from PartyStruct where childEntity=?", vo);
				
						for (PartyStruct item : list) {
							if ("1".equals(item.getChildEntity().getId())) {
								partyEntities.add(childPartyEntity);
							}
				
							// 判断是否是分公司管理员的公司
							if (childPartyEntity.getId().equals(item.getParentEntity().getId())) {
								partyEntities.add(childPartyEntity);
							}
							break;
						}
				
					}
				} else {
					// String name1 = 	childPartyEntity.getName();
					// Long cid1 = childPartyEntity.getPartyType().getId();
					partyEntities.add(childPartyEntity);
				}
			}
			
			}
			}
			
			if (partyEntities.isEmpty()) {
				map.put("open", false);
			} else {
				if (PartyConstants.ROOT_PARTY_TREE_ID.equals(partyEntity.getId())) {
					map.put("open", true);
				} else {
					map.put("open", false);
				}
				map.put("children",
					generatePartyEntitiesCompanyChecked(partyEntities, partyStructTypeId, viewPost, partyTypeId));
			}
			
			return map;
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			
			return map;
		}
    }

    public Map<String, Object> generatePartyEntityNoCompanyChecked(PartyEntity partyEntity,
            long partyStructTypeId, boolean viewPost, boolean auth, boolean rootNodeIsCheckbox) {
			Map<String, Object> map = new HashMap<String, Object>();
			
			// zyl 2017-07-12
			Long accountId = Long.parseLong(currentUserHolder.getUserId());
			
			try {
			map.put("id", partyEntity.getId());
			map.put("name", partyEntity.getName());
			map.put("ref", partyEntity.getRef());
			if(partyEntity.getPartyType().getId().equals(PartyConstants.PARTY_TYPE_COMPANY)){
				map.put("nocheck",true);
			}
			
			if (!rootNodeIsCheckbox) {
				if (PartyConstants.ROOT_PARTY_TREE_ID.equals(partyEntity.getId())) {
					map.put("nocheck", true);
				}
			}
			List<PartyStruct> partyStructs = partyStructManager.find(
			"from PartyStruct where parentEntity=? order by priority",
			partyEntity);
			List<PartyEntity> partyEntities = new ArrayList<PartyEntity>();
			
			for (PartyStruct partyStruct : partyStructs) {
			if (partyStruct.getPartyStructType().getId() == partyStructTypeId) {
			PartyEntity childPartyEntity = partyStruct.getChildEntity();
			
			if (childPartyEntity == null) {
			logger.info("child party entity is null");
			
			continue;
			}
			
			if (childPartyEntity.getDelFlag().equals(PersonInfoConstants.DELETE_FLAG_YES)) {
				// logger.info("child party entity is delete");
				
				continue;
			}
			
			if (childPartyEntity.getPartyType().getType() != PartyConstants.TYPE_USER) {
			
			
			if (auth) {
			
			//if (accountId.equals(PartyConstants.ADMIN_USER_ID)) {   // 超级管理员
			//partyEntities.add(childPartyEntity);
			//} else {
			if (!PartyConstants.ROOT_PARTY_TREE_ID.equals(partyStruct.getParentEntity().getId())) {
			partyEntities.add(childPartyEntity);
			} else {
			AccountInfo accountInfo = accountInfoManager.get(accountId);
			
			PartyEntity vo = partyEntityManager.get(Long.parseLong(accountInfo.getCode()));
			
			List<PartyStruct> list = partyStructManager.find(
			     "from PartyStruct where childEntity=?", vo);
			
			for (PartyStruct item : list) {
			 if ("1".equals(item.getChildEntity().getId())) {
			     partyEntities.add(childPartyEntity);
			 }
			
			 // 判断是否是分公司管理员的公司
			 if (childPartyEntity.getId().equals(item.getParentEntity().getId())) {
			     partyEntities.add(childPartyEntity);
			 }
			 break;
			}
			
			}
			//}
			} else {
			partyEntities.add(childPartyEntity);
			}
			}
			
			}
			}
			
			if (partyEntities.isEmpty()) {
			map.put("open", false);
			} else {
			if (PartyConstants.ROOT_PARTY_TREE_ID.equals(partyEntity.getId())) {
			map.put("open", true);
			} else {
			map.put("open", false);
			}
			map.put("children",
					generatePartyEntitiesNoCompanyChecked(partyEntities, partyStructTypeId, viewPost));
			}
			
			return map;
			} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			
			return map;
			}
}
    
    
    
    
    
    
    
    
    

    @GET
    @Path("search")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> search(@QueryParam("name") String name,
                               @QueryParam("partyTypeId") long partyTypeId) {
        List<String> names = partyEntityManager
                .find("select name from PartyEntity where delFlag = '0' and name like ? and partyType.id=?",
                        "%" + name + "%", partyTypeId);

        return names;
    }

    @GET
    @Path("searchUser")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Map<String, String>> searchUser(@QueryParam("parentId") Long parentId,@QueryParam(value="isshow") String isshow) {

        Long accountId = Long.parseLong(currentUserHolder.getUserId());

        List<Map<String, String>> list = new ArrayList<Map<String, String>>();

        // 只有超级管理员才可以点击树的根节点   zyl 2017-07-12
        if (!accountId.equals(PartyConstants.ADMIN_USER_ID) && parentId.equals(PartyConstants.ROOT_PARTY_TREE_ID)) {

            return list;
        }

        String selectSql = "SELECT p.*,e.ID  FROM party_entity e"
                + " inner JOIN party_struct s ON s.CHILD_ENTITY_ID=e.ID"
                + " inner JOIN person_info p ON e.id=p.ID"
                + " WHERE p.DEL_FLAG='0' AND e.TYPE_ID='1' and (s.STRUCT_TYPE_ID='1' or s.STRUCT_TYPE_ID='4') AND s.PARENT_ENTITY_ID = ?"
                + " ORDER BY p.POSITION_CODE DESC";

        List<Map<String, Object>> mapList = jdbcTemplate.queryForList(selectSql, parentId);
        //任务管理组织树查询，当点击的节点不是岗位，就不查询数据了 sjx 18.09.26
        if("task".equals(isshow)){
        	String sql = "select * from party_entity where id="+parentId;
        	Map<String,Object> map = jdbcTemplate.queryForMap(sql);
        	if(map.size() != 0){
        		String typeId = map.get("type_id").toString();
        		if(!typeId.equals("5")){
        			return list;
        		}
        	}
        }
        for (Map<String, Object> maptemp : mapList) {
            Map<String, String> map = new HashMap<String, String>();
            PartyEntity partyEntity = partyEntityManager.findUniqueBy("id", Long.parseLong(convertString(maptemp.get("ID"))));
            if (partyService.findRoleByRef(partyEntity.getId())) {
            	/*//ckx 根据禁止登陆时间显示
            	if("1".equals(StringUtil.toString(maptemp.get("QUIT_FLAG")))){
            		String quitTime = StringUtil.toString(maptemp.get("QUIT_TIME"));
            		if(StringUtils.isNoneBlank(quitTime)){
                		//当前时间
                		Calendar calSearch=Calendar.getInstance();
                		calSearch.setTime(new Date());
                		
                		//禁止登录系统时间
                		quitTime = quitTime.substring(0, 10);
                		Date quitDate = DateUtil.formatDateStr(quitTime+" 18:00:00", "");
                		Calendar calQuit=Calendar.getInstance();
        				calQuit.setTime(quitDate);
                		
        				//如果查询时间大于离职时间
        				if(calSearch.after(calQuit)){
        					continue;
        				}
                		
                	}
            	}*/
                map.put("id", convertString(maptemp.get("ID")));
                map.put("userName", convertString(maptemp.get("FULL_NAME")));
                String postName = "";

                Set<PartyStruct> partyStructs = partyEntity.getParentStructs();
                for (PartyStruct vo : partyStructs) {
                    if (vo.getParentEntity().getPartyType().getId().equals(PartyConstants.PARTY_TYPE_POST)) {
                    	String v =  vo.getParentEntity().getIsDisplay();
                        if(v!=null && v.equals("1")){
                        postName += vo.getParentEntity().getName() + ",";
                        }
                    }
                }
                if (StringUtils.isNotBlank(postName)) {
                    postName = postName.substring(0, postName.length() - 1);
                }
                map.put("displayName", "");
                map.put("postName", postName);
                list.add(map);
            }
        }

        return list;
    }
    
    @GET
    @Path("searchUserWithAdmin")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Map<String, String>> searchUserWithAdmin(@QueryParam("parentId") Long parentId) {

        Long accountId = Long.parseLong(currentUserHolder.getUserId());

        List<Map<String, String>> list = new ArrayList<Map<String, String>>();

        // 只有超级管理员才可以点击树的根节点   zyl 2017-07-12
        if (!accountId.equals(PartyConstants.ADMIN_USER_ID) && parentId.equals(PartyConstants.ROOT_PARTY_TREE_ID)) {

            return list;
        }

        String selectSql = "SELECT p.FULL_NAME,e.ID  FROM party_entity e"
                + " inner JOIN party_struct s ON s.CHILD_ENTITY_ID=e.ID"
                + " inner JOIN person_info p ON e.id=p.ID"
                + " WHERE p.DEL_FLAG='0' AND e.TYPE_ID='1' and s.STRUCT_TYPE_ID='1' AND s.PARENT_ENTITY_ID = ?"
                + " ORDER BY p.POSITION_CODE DESC";

        List<Map<String, Object>> mapList = jdbcTemplate.queryForList(selectSql, parentId);

        for (Map<String, Object> maptemp : mapList) {
            Map<String, String> map = new HashMap<String, String>();
            PartyEntity partyEntity = partyEntityManager.findUniqueBy("id", Long.parseLong(convertString(maptemp.get("ID"))));
            if (partyService.findRoleByRefNoSuperAdmin(partyEntity.getId())) {
                map.put("id", convertString(maptemp.get("ID")));
                map.put("userName", convertString(maptemp.get("FULL_NAME")));
                String postName = "";

                Set<PartyStruct> partyStructs = partyEntity.getParentStructs();
                for (PartyStruct vo : partyStructs) {
                    if (vo.getParentEntity().getPartyType().getId().equals(PartyConstants.PARTY_TYPE_POST)) {
                    	String v =  vo.getParentEntity().getIsDisplay();
                        if(v!=null && v.equals("1")){
                        postName += vo.getParentEntity().getName() + ",";
                        }
                    }
                }
                if (StringUtils.isNotBlank(postName)) {
                    postName = postName.substring(0, postName.length() - 1);
                }
                map.put("displayName", "");
                map.put("postName", postName);
                list.add(map);
            }
        }

        return list;
    }

    //去除管理员显示，去除当前登录人显示
    @GET
    @Path("searchUserNoMe")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Map<String, String>> searchUserNoMe(@QueryParam("parentId") Long parentId) {

        Long accountId = Long.parseLong(currentUserHolder.getUserId());

        List<Map<String, String>> list = new ArrayList<Map<String, String>>();

        // 只有超级管理员才可以点击树的根节点   zyl 2017-07-12
        if (!accountId.equals(PartyConstants.ADMIN_USER_ID) && parentId.equals(PartyConstants.ROOT_PARTY_TREE_ID)) {

            return list;
        }

        String selectSql = "SELECT p.FULL_NAME,e.ID  FROM party_entity e"
                + " inner JOIN party_struct s ON s.CHILD_ENTITY_ID=e.ID"
                + " inner JOIN person_info p ON e.id=p.ID"
                + " WHERE p.DEL_FLAG='0' AND e.TYPE_ID='1' and s.STRUCT_TYPE_ID='1' AND s.PARENT_ENTITY_ID = ?"
                + " ORDER BY p.POSITION_CODE DESC";

        List<Map<String, Object>> mapList = jdbcTemplate.queryForList(selectSql, parentId);

        for (Map<String, Object> maptemp : mapList) {
            Map<String, String> map = new HashMap<String, String>();
            PartyEntity partyEntity = partyEntityManager.findUniqueBy("id", Long.parseLong(convertString(maptemp.get("ID"))));
            if (!partyEntity.getId().equals(accountId)) {
                if (partyService.findRoleByRef(partyEntity.getId())) {
                    map.put("id", convertString(maptemp.get("ID")));
                    map.put("userName", convertString(maptemp.get("FULL_NAME")));
                    String postName = "";

                    Set<PartyStruct> partyStructs = partyEntity.getParentStructs();
                    for (PartyStruct vo : partyStructs) {
                        if (vo.getParentEntity().getPartyType().getId().equals(PartyConstants.PARTY_TYPE_POST)) {
                            
                           String v =  vo.getParentEntity().getIsDisplay();
                           if(v!=null && v.equals("1")){
                        	   postName += vo.getParentEntity().getName() + ",";
                           }
                        }
                    }
                    if (StringUtils.isNotBlank(postName)) {
                        postName = postName.substring(0, postName.length() - 1);
                    }
                    map.put("displayName", "");
                    map.put("postName", postName);
                    list.add(map);
                }
            }
        }


        return list;
    }

    private String convertString(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof String) {
            return (String) value;
        }

        return value.toString();
    }

    //汇报转发去除当前登陆人和已经被转发过的人
    @GET
    @Path("searchUserNoMeRepeat")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Map<String, String>> searchUserNoMeRepeat(@QueryParam("parentId") Long
                                                                  parentId, @RequestParam("id") Long id) {
        Long accountId = Long.parseLong(currentUserHolder.getUserId());
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        // 只有超级管理员才可以点击树的根节点   zyl 2017-07-12
        if (!accountId.equals(PartyConstants.ADMIN_USER_ID) && parentId.equals(PartyConstants.ROOT_PARTY_TREE_ID)) {
            return list;
        }
        List<Long> reportSendee = new ArrayList();
        List<WorkReportForward> workReportForwardList = workReportForwardManager.findBy("workReportInfo.id", id);
        for (WorkReportForward workReportForward : workReportForwardList) {
            reportSendee.add(workReportForward.getSendee());
        }

        String selectSql = "SELECT p.FULL_NAME,e.ID  FROM party_entity e"
                + " inner JOIN party_struct s ON s.CHILD_ENTITY_ID=e.ID"
                + " inner JOIN person_info p ON e.id=p.ID"
                + " WHERE p.DEL_FLAG='0' AND e.TYPE_ID='1' and s.STRUCT_TYPE_ID='1' AND s.PARENT_ENTITY_ID = ?"
                + " ORDER BY p.POSITION_CODE DESC";

        List<Map<String, Object>> mapList = jdbcTemplate.queryForList(selectSql, parentId);

        for (Map<String, Object> maptemp : mapList) {
            Map<String, String> map = new HashMap<String, String>();
            PartyEntity partyEntity = partyEntityManager.findUniqueBy("id", Long.parseLong(convertString(maptemp.get("ID"))));
            if (!partyEntity.getId().equals(accountId)) {
                for (Long sendee : reportSendee) {
                    if (!partyEntity.getId().equals(sendee))
                        if (partyService.findRoleByRef(partyEntity.getId())) {
                            map.put("id", convertString(maptemp.get("ID")));
                            map.put("userName", convertString(maptemp.get("FULL_NAME")));
                            String postName = "";

                            Set<PartyStruct> partyStructs = partyEntity.getParentStructs();
                            for (PartyStruct vo : partyStructs) {
                                if (vo.getParentEntity().getPartyType().getId().equals(PartyConstants.PARTY_TYPE_POST)) {
                                    postName += vo.getParentEntity().getName() + ",";
                                }
                            }

                            if (StringUtils.isNotBlank(postName)) {
                                postName = postName.substring(0, postName.length() - 1);
                            }
                            map.put("displayName", "");
                            map.put("postName", postName);
                            list.add(map);
                        }
                }
            }
        }


        return list;
    }

    @GET
    @Path("searchPost")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Map<String, String>> searchPost(@QueryParam("parentId") Long parentId) {

        //Long accountId = Long.parseLong(currentUserHolder.getUserId());

        List<Map<String, String>> list = new ArrayList<Map<String, String>>();

        // 只有超级管理员才可以点击树的根节点   zyl 2017-07-12
        /*if (!accountId.equals(id) && parentId.equals(id)) {

    		return list;
    	}*/

        String hql = "select child from PartyEntity child join child.parentStructs parent"
                + " where child.partyType.id=5 and parent.partyStructType = 1 and child.delFlag = '0' and child.isDisplay != '0' and parent.parentEntity.id=?";
        List<PartyEntity> partyEntities = partyEntityManager.find(hql, parentId);


        for (PartyEntity partyEntity : partyEntities) {
            Map<String, String> map = new HashMap<String, String>();
            /*
            UserDTO userDto = userConnector.findById(partyEntity.getRef());
            map.put("id", userDto.getId());
            map.put("username", userDto.getUsername());
            map.put("displayName", userDto.getDisplayName());
            */
            if (partyEntity.getPartyType().getType() == PartyConstants.TYPE_POSITION) {
                map.put("id", Long.toString(partyEntity.getId()));
                map.put("userName", partyEntity.getName());
                map.put("displayName", "");
                //通过岗位查询人员 ckx add 2018/8/28
                String userNames = "";
	    		List<Map<String, Object>> userList = jdbcTemplate.queryForList("select e.id,name from party_entity e join party_struct s on e.id=s.CHILD_ENTITY_ID where DEL_FLAG = '0' and s.PARENT_ENTITY_ID= '"+partyEntity.getId()+"'");
	    		for (Map<String, Object> userMap : userList) {
					String userName = StringUtil.toString(userMap.get("name"));
					userNames += userName +",";
				}
	    		if(StringUtils.isNotBlank(userNames)){
	        		String str = userNames.substring(userNames.length()-1,userNames.length() );
	        		if(",".equals(str)){
	        			userNames = userNames.substring(0, userNames.length()-1);
	        		}
	    		}
	    		map.put("userNames", userNames);
                list.add(map);
            }
        }

        return list;
    }

    @GET
    @Path("removeParty")
    public Map<String, String> removeParty(@QueryParam("selectedItem") Long selectedItem) {

    	Long accountId = Long.parseLong(currentUserHolder.getUserId());
    	
        String name = "";
        String returnStr = "";
        Map<String, String> map = new HashMap<String, String>();

        PartyStruct partyStruct = partyStructManager.get(selectedItem);
        
        String strSql=String.format("select task_id from task_info_approve_position where position_id=%s", partyStruct.getChildEntity().getId());
        List<Map<String,Object>> mapApproveList=jdbcTemplate.queryForList(strSql);
        if(mapApproveList!=null&&mapApproveList.size()>0){
        	returnStr = "由于 " + name + " 存在发起或审批的信息，不允许删除！";
        	map.put("id", partyStruct.getChildEntity().getId().toString());
            map.put("pid", Long.toString(partyStruct.getParentEntity().getId()));
            map.put("name", returnStr);
            return map;
        }

        String hql = "from PartyStruct where childEntity.delFlag = '0' and parentEntity=?";
        PartyEntity vo = partyStruct.getChildEntity();

        // 如果没有选中partyEntityId，就啥也不显示
        List<PartyStruct> listChild = partyStructManager.find(hql, vo);

        if (listChild.size() > 0) {
            name = partyStruct.getChildEntity().getName();
        }

        // zyl 2017-07-12
        if (StringUtils.isBlank(name)) {
            if (PartyConstants.PARTY_STRUCT_TYPE_ORG.equals(partyStruct.getPartyStructType().getId())) {
                PartyEntity partyEntity = partyEntityManager.get(partyStruct.getChildEntity().getId());

                // 如果是岗位，需要判断岗位是否已经配置到流程中
                if (partyEntity.getPartyType().getId().equals(PartyConstants.PARTY_TYPE_POST)) {
                    String sql = "SELECT u.ID,u.`VALUE`,u.`NAME`,u.NODE_ID,p.`NAME` as bpmName from BPM_CONF_USER u"
                            + " inner join bpm_conf_node n on u.node_id = n.id"
                            + " inner join bpm_conf_base b on n.CONF_BASE_ID = b.ID"
                            + " inner join bpm_process p on p.CONF_BASE_ID =b.ID"
                            + " where u.value LIKE '%" + partyEntity.getId() + "%'";
                    List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);
                    if (list.size() > 0) {
                        for (Map<String, Object> postMap : list) {
                            returnStr += postMap.get("bpmName") + ",";
                        }
                    } else {
                        partyEntity.setDelFlag(PersonInfoConstants.DELETE_FLAG_YES);
                        // partyEntityManager.remove(partyEntity);
                        partyStructManager.remove(partyStruct);
                        partyEntityManager.update(partyEntity);
                    }
                } else {
                    partyEntity.setDelFlag(PersonInfoConstants.DELETE_FLAG_YES);
                    // partyEntityManager.remove(partyEntity);
                    partyStructManager.remove(partyStruct);
                    partyEntityManager.update(partyEntity);
                }
            }

            if (PartyConstants.PARTY_STRUCT_TYPE_MANAGE.equals(partyStruct.getPartyStructType().getId())) {
                partyStructManager.remove(partyStruct);
            }
        }


        if (StringUtils.isBlank(name)) {
            if (StringUtils.isBlank(returnStr)) {
                returnStr = "删除成功";
                
                //chengze 20181016 修改内容,存入日志
                OrgLogEntity orgLogEntity  = new OrgLogEntity();
                orgLogEntity.setModifyContent("删除");
                orgLogEntity.setOrgID(partyStruct.getChildEntity().getId());
                orgLogEntity.setOperationID(accountId);
                orgLogEntity.setOperationTime(new Date());
                orgLogManager.save(orgLogEntity);
                
              
            } else {
                returnStr = "由于" + returnStr.substring(0, returnStr.length() - 1) + " 流程中存在该岗位信息，不允许删除！";
            }
        } else {
            returnStr = "由于 " + name + " 存在下级节点，不允许删除！";
        }
        map.put("id", Long.toString(vo.getId()));
        map.put("pid", Long.toString(partyStruct.getParentEntity().getId()));
        map.put("name", returnStr);
        return map;

    }

    
    
    
    /**
     * 查询组织机构:某个节点下的所有组织机构，不包括人
     * @author cz 20180831
     * @param 
     * @return
     */
    
    public List<String> getPartyEntityNoPerson(@QueryParam("parentEntityId") String parentEntityId) {
        
    	if (parentEntityId == null) {return null;}
    	
    	List<Map> list = new ArrayList<Map>();
    	
    	List<String> resultIDString = null ;
    	
    	if(parentEntityId!=null&&parentEntityId.length()>0){
    		//Map<String, Object> map =getPartyEntityNoPersonSubQuery(parentEntityId);
    		//list.add(map);
    		
    		resultIDString =getPartyEntityNoPersonSubQuery(parentEntityId);
    	}
    	 
        return resultIDString;
    }
    
    
    public List<String> getPartyEntityNoPersonSubQuery(@QueryParam("parentEntityId") String parentEntityId) {
        
    	List<String> resultIDString =  new ArrayList(); ;
    	
    	String parentEntityIdSub = "";
    	//找出这个节点下面的组织机构、不包括人
    	String sqlString="SELECT b.id AS id ,b.`NAME` "
    					+ "	FROM party_struct a JOIN party_entity b "
    					+ " ON a.CHILD_ENTITY_ID = b.ID "
    					+ "WHERE  b.TYPE_ID <> 1 AND b.TYPE_ID <>5  AND PARENT_ENTITY_ID in ("+parentEntityId+")";
    	
    	 List<Map<String, Object>> list = jdbcTemplate.queryForList(sqlString); 
    	 if(list.size()>0){
    		 for(int i = 0; i<list.size(); i++){
    			 //map.put("child"+i, list.get(i).get("id")) ;
    			 resultIDString.add(list.get(i).get("id").toString());
    			 parentEntityIdSub = parentEntityIdSub  + "'"+ list.get(i).get("id")+ "'" +",";
    		 }
    		 if(parentEntityIdSub!=null&&parentEntityIdSub.length()>0){
    			 parentEntityIdSub = parentEntityIdSub.substring(0, parentEntityIdSub.length()-1);
    			 List<String> resultIDStringSubList  = getPartyEntityNoPersonSubQuery(parentEntityIdSub);
    			 if (resultIDStringSubList!=null&&resultIDStringSubList.size()>0) { 
    				 for(int t = 0; t<resultIDStringSubList.size(); t++){
    					 resultIDString.add(resultIDStringSubList.get(t));
    				 }
    				
				}
    		}
    	 }
    	 
    	 return resultIDString;
    }
    
    
    
    
    
    
    
    
    
    
    
    // ~ ==================================================
    @Resource
    public void setPartyTypeManager(PartyTypeManager partyTypeManager) {
        this.partyTypeManager = partyTypeManager;
    }

    @Resource
    public void setPartyEntityManager(PartyEntityManager partyEntityManager) {
        this.partyEntityManager = partyEntityManager;
    }

    @Resource
    public void setPartyStructManager(PartyStructManager partyStructManager) {
        this.partyStructManager = partyStructManager;
    }

    @Resource
    public void setPartyService(PartyService partyService) {
        this.partyService = partyService;
    }

    @Resource
    public void setUserConnector(UserConnector userConnector) {
        this.userConnector = userConnector;
    }

    @Resource
    public void setCurrentUserHolder(CurrentUserHolder currentUserHolder) {
        this.currentUserHolder = currentUserHolder;
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
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    @Resource
    public void setPartyConnector(PartyConnector partyConnector) {
        this.partyConnector = partyConnector;
    }
    
    //private KeyValueConnector keyValueConnector;
    @Resource
    public void setKeyValueConnector(KeyValueConnector keyValueConnector) {
        this.keyValueConnector = keyValueConnector;
    }
    
    @Resource
    public void setOrgLogManager(OrgLogManager orgLogManager) {
        this.orgLogManager = orgLogManager;
    }

    // ~ ==================================================
    public static class PartyTypeDTO {
        private long id;
        private String name;

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class PartyEntityDTO {
        private long id;
        private String name;
        private String ref;

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getRef() {
            return ref;
        }

        public void setRef(String ref) {
            this.ref = ref;
        }
    }
}
