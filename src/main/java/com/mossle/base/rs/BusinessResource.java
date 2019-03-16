package com.mossle.base.rs;

import java.util.*;

import javax.annotation.Resource;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.google.common.base.Joiner;
import com.mossle.api.operation.OperationConnector;
import com.mossle.api.org.OrgConnector;
import com.mossle.api.party.PartyConnector;
import com.mossle.api.party.PartyDTO;
import com.mossle.api.process.ProcessDTO;
import com.mossle.base.persistence.domain.BusinessDetailEntity;
import com.mossle.base.persistence.domain.BusinessTypeEntity;
import com.mossle.base.persistence.domain.DetailPostEntity;
import com.mossle.base.persistence.manager.BusinessDetailManager;
import com.mossle.base.persistence.manager.BusinessTypeManager;
import com.mossle.base.persistence.manager.DetailPostManager;
import com.mossle.bpm.cmd.FindNextActivitiesCmd;
import com.mossle.bpm.cmd.FindPreviousActivitiesCmd;
import com.mossle.bpm.cmd.FindTaskDefinitionsCmd;
import com.mossle.bpm.persistence.domain.BpmProcess;
import com.mossle.bpm.persistence.manager.BpmProcessManager;
import com.mossle.bpm.support.ActivityDTO;
import com.mossle.core.auth.CurrentUserHolder;
import com.mossle.core.util.BaseDTO;
import com.mossle.operation.persistence.domain.CodeEntity;
import com.mossle.operation.persistence.manager.CodeManager;
import com.mossle.operation.service.OperationService;
import com.mossle.operation.web.ProcessOperationController;
import com.mossle.party.persistence.domain.PartyEntity;
import com.mossle.party.persistence.domain.PartyStruct;
import com.mossle.party.persistence.manager.PartyEntityManager;
import com.mossle.party.persistence.manager.PartyStructManager;
import com.mossle.party.support.PartyOrgConnector;
import com.mossle.user.persistence.domain.AccountInfo;
import com.mossle.worktask.persistence.domain.WorkTaskCc;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.bpmn.behavior.BoundaryEventActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.CallActivityBehavior;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.bpmn.parser.ErrorEventDefinition;
import org.activiti.engine.impl.bpmn.parser.EventSubscriptionDeclaration;
import org.activiti.engine.impl.jobexecutor.TimerDeclarationImpl;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmActivity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.delegate.ActivityBehavior;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.Lane;
import org.activiti.engine.impl.pvm.process.LaneSet;
import org.activiti.engine.impl.pvm.process.ParticipantProcess;
import org.activiti.engine.impl.pvm.process.TransitionImpl;
import org.activiti.engine.impl.task.TaskDefinition;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;


@Component
@Path("business")
public class BusinessResource {
    private static Logger logger = LoggerFactory.getLogger(BusinessResource.class);
    private BusinessTypeManager businessTypeManager;
    private BusinessDetailManager businessDetailManager;
    private PartyConnector partyConnector;
    private OrgConnector orgConnector;
    private DetailPostManager detailPostManager;
    private PartyStructManager partyStructManager;
    private PartyEntityManager partyEntityManager;
    private JdbcTemplate jdbcTemplate;
    private CurrentUserHolder currentUserHolder;
    private OperationService operationService;
    @Autowired
    private BpmProcessManager bpmProcessManager;
    @Autowired
    private OperationConnector operationConnector;

    //业务类型列表岗位的显示
    @POST
    @Path("bussiness-detail-positionshow")
    @Produces(MediaType.APPLICATION_JSON)
    public BaseDTO positonInfo(@FormParam("id") Long id) {
        BaseDTO result = new BaseDTO();
        try {
            if (id == null || id < 1) {
                result.setCode(500);
                logger.debug("获取岗位-获取参数id错误");
                result.setMessage("获取参数错误");
                return result;
            }

            List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
            List<DetailPostEntity> detailPostEntityList = detailPostManager.findBy("detailID", id);
            if (!CollectionUtils.isEmpty(detailPostEntityList)) {
                for (DetailPostEntity detailPostEntity : detailPostEntityList) {
                    Map<String, Object> map = new HashMap<String, Object>();
                    //岗位id
                    Long postId3 = detailPostEntity.getPostID();
                    PartyStruct partyStruct2 = partyStructManager.findUniqueBy("childEntity.id", postId3);
                    if (partyStruct2 != null) {
                        Long postId2 = partyStruct2.getParentEntity().getId();
                        PartyStruct partyStruct1 = partyStructManager.findUniqueBy("childEntity.id", postId2);
                        if (partyStruct1 != null) {
                            //岗位名称
                            String postName3 = partyStruct2.getChildEntity().getName();
                            //部门名称
                            String postName2 = partyStruct2.getParentEntity().getName();
                            //分公司名称
                            String postName1 = partyStruct1.getParentEntity().getName();
                            map.put("parent", postName1);
                            map.put("child", postName2);
                            map.put("position", postName3);
                        }
                    }
                    list.add(map);
                }
            }

            result.setCode(200);
            result.setData(list);

        } catch (ArithmeticException e) {
            result.setCode(500);
            result.setMessage("查询出错");
            logger.error("查询岗位异常：" + e.getMessage() + "\r\n" + e.fillInStackTrace());
        }
        return result;
    }


    //业务类型列表部门的显示
    @POST
    @Path("bussiness-type-fnshow")
    @Produces(MediaType.APPLICATION_JSON)
    public BaseDTO showInfo(@FormParam("id") Long id) {
        BaseDTO result = new BaseDTO();
        try {
            if (id == null || id < 1) {
                result.setCode(500);
                logger.debug("获取部门-获取参数id错误");
                result.setMessage("获取参数错误");
                return result;
            }
            List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
            BusinessTypeEntity businessTypeEntity = businessTypeManager.findUniqueBy("id", id);
            String department = businessTypeEntity.getDepartmentCode();
            String[] split_data = department.split(",");
            for (int i = 0; i < split_data.length; i++) {
                Map<String, Object> map = new HashMap<String, Object>();
                PartyStruct partyStruct = partyStructManager.findUniqueBy("childEntity.id", Long.parseLong(split_data[i]));
                if (partyStruct != null) {
                    String partyNameDown = partyStruct.getChildEntity().getName();
                    String partyNameUp = partyStruct.getParentEntity().getName();
                    map.put("up", partyNameUp);
                    map.put("down", partyNameDown);
                }
                list.add(map);
            }

            result.setCode(200);
            result.setData(list);

        } catch (ArithmeticException e) {
            result.setCode(500);
            result.setMessage("查询出错");
            logger.error("查询部门异常：" + e.getMessage() + "\r\n" + e.fillInStackTrace());
        }
        return result;
    }

    //取业务类型
    @GET
    @Path("types")
    public List<BusinessTypeDTO> getAllPartyTypes() {
        List<BusinessTypeEntity> businessTypes = businessTypeManager.getAll();

        List<BusinessTypeDTO> businessTypeDtos = new ArrayList<BusinessTypeDTO>();

        for (BusinessTypeEntity businessType : businessTypes) {
            BusinessTypeDTO businessTypeDto = new BusinessTypeDTO();
            businessTypeDto.setId(businessType.getId());
            businessTypeDto.setName(businessType.getBusinesstype());
            businessTypeDtos.add(businessTypeDto);
        }

        return businessTypeDtos;
    }


    //根据用户所属的部门取对应的业务类型
    @SuppressWarnings("unchecked")
	@GET
    @Path("post_types")
    public List<BusinessTypeDTO> getTypesByPost(@QueryParam("bpmProcessId") String bpmProcessId,@QueryParam("userId") String userId, @QueryParam("url") String url) {
    	List<BusinessTypeDTO> businessTypeDtos = new ArrayList<BusinessTypeDTO>();
        //取当前用户的所属部门
        PartyDTO partyDTO = partyConnector.findDepartmentById(userId);
        //根据这个部门取业务类型 （当前是启用状态的，并且所配置的表单与接收到的url参数匹配）
        //String sql = "from BusinessTypeEntity where departmentCode LIKE '%" + (partyDTO == null ? "" : partyDTO.getId()) + "%' and formid LIKE '%" + url + "%' and enable='是'  ";
        
        //查询用户所属岗位
        List<PartyEntity> postList = orgConnector.getPostByUserId(userId);
        String postIds = "";
        for (PartyEntity vo : postList) {
            postIds += vo.getId() + ",";
        }
        if(!"".equals(postIds))
        	postIds=postIds.substring(0, postIds.length() - 1);
        String strDetailSql="select DISTINCT type_id from oa_ba_business_detail d"
        		+ " INNER JOIN oa_ba_business_post p on p.detail_id=d.id ";
        
        List<String> busDetailList=new ArrayList<String>();
        if(!StringUtils.isBlank(bpmProcessId)){
        	List<Map<String,Object>> mapProcessList=jdbcTemplate.queryForList("select * from bpm_process where id=?", bpmProcessId);
        	if(mapProcessList!=null&&mapProcessList.size()>0){
        		Map<String,Object> mapProcess=mapProcessList.get(0);
        		String categroy_id=mapProcess.get("category_id")==null?"":mapProcess.get("category_id").toString();
        		if(categroy_id.equals("3")){
        			String strDetailProcessSql="select d.* from oa_ba_business_detail d "
        						+" INNER JOIN bpm_process b ON b.ID=d.bpmProcessId "
    							+" where b.CATEGORY_ID=3 and d.formid like '%"+url+"%' ";
        			List<Map<String,Object>> mapDetailList=jdbcTemplate.queryForList(strDetailProcessSql);
        			for (Map<String, Object> map : mapDetailList) {
        				if(busDetailList.size()>0&&busDetailList.contains(map.get("type_id").toString())){
        					continue;
        				}
        				
        				if(operationConnector.IsShowCommonProcess(Long.valueOf(map.get("bpmProcessId").toString()), userId)){
        					busDetailList.add(map.get("type_id").toString());
        				}
					}
        			//busDetailList=jdbcTemplate.queryForList(strDetailProcessSql, String.class);
        		}
        		else {
        			String strByName=mapProcess.get("BYNAME")==null?"":(mapProcess.get("BYNAME").toString());
            		if(!"".equals(strByName)){
            			strDetailSql+=String.format(" INNER JOIN bpm_process b ON b.ID=d.bpmProcessId where b.BYNAME='%s' ", strByName);
            		}
            		
            		if(strDetailSql.indexOf("where")==-1)
                    	strDetailSql+=" where ";
                    else 
                    	strDetailSql+=" and ";
            		strDetailSql+=" p.post_id in("+postIds+") and d.formid like '%"+url+"%'";
                    busDetailList=jdbcTemplate.queryForList(strDetailSql, String.class);
				}
        	}
        	else
        		return businessTypeDtos;
        }
        else {
    			/*String strByName=mapProcess.get("BYNAME")==null?"":(mapProcess.get("BYNAME").toString());
        		if(!"".equals(strByName)){
        			strDetailSql+=String.format(" INNER JOIN bpm_process b ON b.ID=d.bpmProcessId where b.BYNAME='%s' ", strByName);
        		}*/
        		
        		if(strDetailSql.indexOf("where")==-1)
                	strDetailSql+=" where ";
                else 
                	strDetailSql+=" and ";
        		strDetailSql+=" p.post_id in("+postIds+") and d.formid like '%"+url+"%'";
                busDetailList=jdbcTemplate.queryForList(strDetailSql, String.class);
        }
        	//return businessTypeDtos;
        
        String strBusinessTypes="";
        if(busDetailList!=null&&busDetailList.size()>0)
        	strBusinessTypes=Joiner.on(",").join(busDetailList);
        
        if (StringUtils.isBlank(strBusinessTypes))
        	return businessTypeDtos;
        
        String sql =String.format("from BusinessTypeEntity where id in(%s)", strBusinessTypes);
        List<BusinessTypeEntity> businessTypeEntity = businessTypeManager.find(sql);
        if (businessTypeEntity.size() >0) {
            for (BusinessTypeEntity businessType : businessTypeEntity) {
                BusinessTypeDTO businessTypeDto = new BusinessTypeDTO();
                businessTypeDto.setId(businessType.getId());
                businessTypeDto.setName(businessType.getBusinesstype());
                businessTypeDtos.add(businessTypeDto);
            }
        }
        return businessTypeDtos;
    }

    public List<BusinessTypeEntity> getPost(String userId, String url) {
        List<BusinessTypeEntity> businessTypeEntity = new ArrayList<BusinessTypeEntity>();
        Long id = Long.parseLong(userId);
        PartyEntity vo = partyEntityManager.get(id);
        String hql = "from PartyStruct where childEntity = ?";
        List<PartyStruct> structList = partyStructManager.find(hql, vo);
        for (int i = 0; i < structList.size(); i++) {
            long DSD = structList.get(i).getParentEntity().getId();
            PartyEntity partyEntity = partyEntityManager.findUniqueBy("id", DSD);
            String name = partyEntity.getName();
            if (name.equals("督察员")) {
                PartyEntity voo = partyEntityManager.get(partyEntity.getId());
                List<PartyStruct> structListt = partyStructManager.find(hql, voo);
                Long postId = structListt.get(0).getParentEntity().getId();
                PartyEntity post2 = partyEntityManager.findUniqueBy("id", postId);
                String postName = post2.getName();
                Long postIdd = post2.getId();
                String sql = "from BusinessTypeEntity where departmentCode LIKE '%" + postIdd + "%' and formid LIKE '%" + url + "%' and enable='是'  ";

                businessTypeEntity = businessTypeManager.find(sql);
            }
        }
        return businessTypeEntity;
    }

    //取所有的业务类型明细
    @GET
    @Path("details")
    public List<BusinessDetailDTO> getAllBusinessDetail(@QueryParam("bt") String bt) {

        List<BusinessDetailEntity> businessDetails = businessDetailManager.findBy("typeId", Long.parseLong(bt));
        List<BusinessDetailDTO> businessDetailDtos = new ArrayList<BusinessDetailDTO>();
        for (BusinessDetailEntity businessDetail : businessDetails) {
            BusinessDetailDTO businessDetailDto = new BusinessDetailDTO();
            businessDetailDto.setId(businessDetail.getId());
            businessDetailDto.setDetail(businessDetail.getBusiDetail());
            businessDetailDtos.add(businessDetailDto);
        }

        return businessDetailDtos;
    }


    //根据用户的岗位取对应的业务明细
    @GET
    @Path("post_details")
    public List<Map<Long, String>> getDetailByPost(@QueryParam("bt") String bt,
    		@QueryParam("userId") String userId, 
    		@QueryParam("url") String url,
    		@QueryParam(value="categoryId") String categoryId) {
        List<Map<Long, String>> businessDetailDtos = new ArrayList<Map<Long, String>>();
        //如果用户业务类型选择了“请选择”，直接返回
        if (bt.equals("请选择")) {
            return businessDetailDtos;
        }

        if (StringUtils.isBlank(bt)) {
            return businessDetailDtos;
        }
        
        boolean isCommonProcess=false;
        if(!com.mossle.core.util.StringUtils.isBlank(categoryId)){
        	isCommonProcess=categoryId.equals("3");
        }

        //先根据  userId 取岗位
        List<PartyEntity> partyDTO = orgConnector.getPostByUserId(userId);
        HashMap<Long, String> map = new LinkedHashMap<Long, String>();
        if (partyDTO.size() > 0) {
            for (int i = 0; i < partyDTO.size(); i++) {
                Long post_id = partyDTO.get(i).getId();
                String hql = "from BusinessDetailEntity where typeId=? and formid=? order by busiDetail desc";
                List<BusinessDetailEntity> businessDetails = businessDetailManager.find(hql, Long.parseLong(bt), url);
                for (BusinessDetailEntity businessDetail : businessDetails) {
                	Long processId=StringUtils.isBlank(businessDetail.getBpmProcessId())?0L:Long.valueOf(businessDetail.getBpmProcessId().toString());
                	BpmProcess bpmProcess=bpmProcessManager.get(processId);
                	if(bpmProcess!=null){
                		//增加公共流程的判断条件 add by lilei at 2018.11.15
                		Long bpmProcessId=StringUtils.isBlank(businessDetail.getBpmProcessId())?0L:Long.parseLong(businessDetail.getBpmProcessId());
                		if(isCommonProcess){
                			 if(bpmProcessId>0){
                    			 if(operationConnector.IsShowCommonProcess(bpmProcessId, userId)){
                    				 map.put(businessDetail.getId(), businessDetail.getBusiDetail());
                    			 }
                			 }
                		}
                		else {
                			Long detail_id = businessDetail.getId();
                			String sql = "from DetailPostEntity where detailID=? and postID=? ";
                            List<DetailPostEntity> detailPostEntity = detailPostManager.find(sql, detail_id, post_id);
                            if (detailPostEntity.size() > 0) {
                                map.put(detail_id, businessDetail.getBusiDetail());
                            }
						}
                	}
                }
            }
            businessDetailDtos.add(map);
            return businessDetailDtos;
        } else {
        	String hql = "from BusinessDetailEntity where typeId=? and formid=? order by busiDetail desc";
            List<BusinessDetailEntity> businessDetails = businessDetailManager.find(hql, Long.parseLong(bt), url);
            for (BusinessDetailEntity businessDetail : businessDetails) {
            	Long processId=StringUtils.isBlank(businessDetail.getBpmProcessId())?0L:Long.valueOf(businessDetail.getBpmProcessId().toString());
            	BpmProcess bpmProcess=bpmProcessManager.get(processId);
            	if(bpmProcess!=null){
            		//增加公共流程的判断条件 add by lilei at 2018.11.15
            		Long bpmProcessId=StringUtils.isBlank(businessDetail.getBpmProcessId())?0L:Long.parseLong(businessDetail.getBpmProcessId());
            		if(isCommonProcess){
            			 if(bpmProcessId>0){
                			 if(operationConnector.IsShowCommonProcess(bpmProcessId, userId)){
                				 map.put(businessDetail.getId(), businessDetail.getBusiDetail());
                			 }
            			 }
            		}
            	}
            }
            businessDetailDtos.add(map);
            //Map<Long,String> otherMap = new HashMap<Long,String>();
            return businessDetailDtos;
            /*List<Map<Long,String>> businessOtherDetailDtos = new ArrayList<Map<Long,String>> ();
            return businessOtherDetailDtos;*/
        }
    }


    @GET
    @Path("level")
    public List<BusinessDetailDTO> getLevel(@QueryParam("bd") String bd) {
        List<BusinessDetailDTO> businessLevelDtos = new ArrayList<BusinessDetailDTO>();
        //如果用户业务细分选择了“请选择”或空串，直接返回
        if (bd.equals("请选择") || bd.equals("")) {
            return businessLevelDtos;
        }
        List<BusinessDetailEntity> businessLevels = businessDetailManager.findBy("id", Long.parseLong(bd));

        for (BusinessDetailEntity level : businessLevels) {
            BusinessDetailDTO businessLevelDto = new BusinessDetailDTO();
            businessLevelDto.setId(level.getId());
            businessLevelDto.setLevel(level.getLevel());
            businessLevelDto.setStandFirst(level.getStandFirst());
            businessLevelDto.setStandSecond(level.getStandSecond());
            businessLevelDtos.add(businessLevelDto);
        }

        return businessLevelDtos;
    }

    //判断受理单编号是否已存在,若不存在，返回当前受理单号，若存在，生成一个新的受理单号返回页面
    @GET
    @Path("applyCodeIfExist")
    public String applyCodeIfExist(@QueryParam("applyCode") String applyCode) throws Exception {

        String applyCodeNew = applyCode;
        String userId = currentUserHolder.getUserId();

        //到 kv_record 表中查找当前受理单号
        String sql = "select  * from   kv_record where applycode='" + applyCode + "'";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);

        //若当前受理单号已存在，重新获取一个受理单号
        if (list.size() > 0) {
            operationService.deleteApplyCodeByUserId(userId);
            applyCodeNew = operationService.CreateApplyCode(userId);

            if (applyCodeNew.equals(applyCode)) {
                return "-1";
            }
        }

        return applyCodeNew;
    }
    
    
    //取第一级的 级别（新员工录入） 20180511  cz
    @GET
    @Path("levels")
    public List<Map<String, Object>> getLevel() {
    	String sql="SELECT  level , levelName FROM  oa_ba_level";
		
		List<Map<String, Object>> list=jdbcTemplate.queryForList(sql);		
        return list;
    }
    
  //取 子级别（新员工录入） 20180511  cz
    @GET
    @Path("levelSub")
    public List<Map<String, Object>> getLevelSub(@QueryParam("t") String t) {
    	String sql="SELECT level_sub from oa_ba_level_sub WHERE parent_level_id = ?";
		
		List<Map<String, Object>> list=jdbcTemplate.queryForList(sql,t);
				
        return list;
    }
    

    // ~ ==================================================
    public static class BusinessTypeDTO {
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

    @Resource
    public void setBusinessTypeManager(BusinessTypeManager businessTypeManager) {
        this.businessTypeManager = businessTypeManager;
    }

    public static class BusinessDetailDTO {
        private long Id;
        private long typeId;
        private String detail;
        private String level;
        private String standFirst;
        private String standSecond;

        public long getId() {
            return Id;
        }

        public void setId(long Id) {
            this.Id = Id;
        }

        public long getTypeId() {
            return typeId;
        }

        public void setTypeId(long typeId) {
            this.typeId = typeId;
        }

        public String getDetail() {
            return detail;
        }

        public void setDetail(String detail) {
            this.detail = detail;
        }

        public String getLevel() {
            return level;
        }

        public void setLevel(String level) {
            this.level = level;
        }

        public String getStandFirst() {
            return standFirst;
        }

        public void setStandFirst(String standFirst) {
            this.standFirst = standFirst;
        }

        public String getStandSecond() {
            return standSecond;
        }

        public void setStandSecond(String standSecond) {
            this.standSecond = standSecond;
        }
    }

    @Resource
    public void setBusinessTypelManager(BusinessTypeManager businessTypeManager) {
        this.businessTypeManager = businessTypeManager;
    }

    @Resource
    public void setBusinessDetailManager(BusinessDetailManager businessDetailManager) {
        this.businessDetailManager = businessDetailManager;
    }

    @Resource
    public void setPartyConnector(PartyConnector partyConnector) {
        this.partyConnector = partyConnector;
    }

    @Resource
    public void setOrgConnector(OrgConnector orgConnector) {
        this.orgConnector = orgConnector;
    }

    @Resource
    public void setPartyStructManager(PartyStructManager partyStructManager) {
        this.partyStructManager = partyStructManager;
    }

    @Resource
    public void setPartyEntityManager(PartyEntityManager partyEntityManager) {
        this.partyEntityManager = partyEntityManager;
    }


    @Resource
    public void setDetailPostManager(DetailPostManager detailPostManager) {
        this.detailPostManager = detailPostManager;
    }

    @Resource
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Resource
    public void setCurrentUserHolder(CurrentUserHolder currentUserHolder) {
        this.currentUserHolder = currentUserHolder;
    }

    @Resource
    public void setOperationService(OperationService operationService) {
        this.operationService = operationService;
    }


}
