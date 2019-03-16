package com.mossle.base.service;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mossle.api.keyvalue.KeyValueConnector;
import com.mossle.api.keyvalue.Record;
import com.mossle.api.party.PartyConnector;
import com.mossle.api.party.PartyDTO;
import com.mossle.base.persistence.domain.BranchApprovalLinkEntity;
import com.mossle.base.persistence.domain.BusinessDetailEntity;
import com.mossle.base.persistence.manager.BranchApprovalLinkEntityManager;
import com.mossle.base.persistence.manager.BusinessDetailManager;
import com.mossle.bpm.persistence.domain.BpmConfBase;
import com.mossle.bpm.persistence.domain.BpmConfNode;
import com.mossle.bpm.persistence.domain.BpmConfUser;
import com.mossle.bpm.persistence.domain.BpmProcess;
import com.mossle.bpm.persistence.manager.BpmConfBaseManager;
import com.mossle.bpm.persistence.manager.BpmConfNodeManager;
import com.mossle.bpm.persistence.manager.BpmConfUserManager;
import com.mossle.bpm.persistence.manager.BpmProcessManager;
import com.mossle.common.utils.StringUtils;
import com.mossle.party.persistence.domain.PartyEntity;
import com.mossle.party.persistence.manager.PartyEntityManager;

@Service
@Transactional(readOnly=true)
public class DetailProcessService {
	@Resource
	private BpmProcessManager bpmProcessManager;
	@Resource
	private BpmConfBaseManager bpmConfBaseManager;
	@Resource
	private BpmConfNodeManager bpmConfNodeManager;
	@Resource
	private BpmConfUserManager bpmConfUserManager;
	@Resource
	private PartyEntityManager partyEntityManager;
	@Resource
	private JdbcTemplate jdbcTemplate;
	@Resource
	private BusinessDetailManager businessDetailManager;
	@Resource
	private PartyConnector partyConnector;
	@Resource
	private KeyValueConnector keyValueConnector;
	@Resource
	private BranchApprovalLinkEntityManager branchApprovalLinkEntityManager;
	private Logger logger;
	
	/**
	 * @param bpmProcessId
	 * @return
	 * 
	 * 获取流程审核过程详情（以该形式 公司部门岗位）
	 */
	public String getResult(String bpmProcessId,String conditionValue) {
		StringBuffer result = new StringBuffer();
        List<BpmProcess> bpmProcess = bpmProcessManager.findBy("id", Long.parseLong(bpmProcessId));
        Long confbaseId = bpmProcess.get(0).getBpmConfBase().getId();
        //List<BpmConfBase> bpmConfBases = processDefinitionId(confbaseId);//查出流程定义id
        //String processDefinitionId = bpmConfBases.get(0).getProcessDefinitionId();
        List<BpmConfNode> bpmConfNodes = activityId(confbaseId);//查出该流程的所有节点
        //ckx 2018/11/27  判断支出费用细分是否需要董事长审批
        boolean boo = false;
        if(StringUtils.isNotBlank(conditionValue)){
			long parseLongValue = Long.parseLong(conditionValue);
			if(parseLongValue < 3000){
				boo = true;
			}
		}
        int index = 0;
        for(BpmConfNode bpmNode : bpmConfNodes){
        	if(boo){
        		if("董事长".equals(bpmNode.getName())){
        			continue;
        		}
        	}
        	String post = postByNodeId(bpmNode.getId());
        	if(!bpmNode.getType().equals("userTask") || post.contains("常用语:流程发起人")){
        		continue;
        	}else{
        		if(post.contains("常用语:")){
        			result.append(bpmNode.getName());
            		result.append("->");
        			continue;
        		}
        		
        		StringBuffer str = new StringBuffer();
        		if(post.contains("岗位:")){
        			post = post.substring(3);//去掉该值中的岗位：
        		}
        		String postName = "from PartyEntity where id=?";
        		PartyEntity e = partyEntityManager.findUnique(postName, Long.parseLong(post));
        		str.insert(0, e.getName());
        		for(int i = 0;i < 2;i++){
        			String typeId = "3,4";
        			if(i == 1){
        				typeId = "2,3";
        			}
        			String org = "select e.* from party_entity e join party_struct s on e.id=s.PARENT_ENTITY_ID where s.CHILD_ENTITY_ID=? and e.type_id in("+typeId+")";
        			Map<String,Object> orgMap = jdbcTemplate.queryForMap(org, post);
        			
        			str.insert(0, orgMap.get("name").toString());
        			post = orgMap.get("id").toString();
        		}
        		result.append(str);
        		result.append("->");
        		
        	}
        	
        }
        return result.substring(0, result.length()-2);
        //return result;
    } 
	/**
	 * @param businessDetailID
	 * @return
	 * 后台通告业务细分id查询流程步骤（适用于常规流程）
	 */
	public GetWhole bgGetProcessPostInfoByBusinessDetailId(String businessDetailID){
		GetWhole getWhole = new GetWhole();
    	BusinessDetailEntity businessDetailEntity = businessDetailManager.findUniqueBy("id", Long.parseLong(businessDetailID));
    	if(businessDetailEntity != null){
    		String bpmProcessId = businessDetailEntity.getBpmProcessId();
			String result = this.getResult(bpmProcessId,"");
        	getWhole.setBpmProcessId(bpmProcessId);
        	getWhole.setWhole(result);
    	}
    	return getWhole;
	}
	/**
	 * @return
	 * 后台通过流程实例id查询流程步骤（适用于自定流程）
	 */
	public GetWhole bgProcessPersonInfoByProcessInstanceId(String processInstanceId){
		GetWhole getWhole = new GetWhole();
    	if(StringUtils.isEmpty(processInstanceId)){
    		getWhole.setCode("500");
    		getWhole.setWhole("该流程未正确获取到流程实例，请联系管理员！");
    		return getWhole;
    	}
    	StringBuffer sb = new StringBuffer();
    	Record record = keyValueConnector.findByRef(processInstanceId);
    	String businessKey = record.getBusinessKey();
    	String sql = "select * from custom_approver where business_key="+businessKey+" order by approveStep asc";
    	List<Map<String,Object>> customApprovers = jdbcTemplate.queryForList(sql);
    	for(Map<String,Object> customApprover :customApprovers){
    		sb.append(customApprover.get("approverId").toString()+",");
    	}
    	String result = getResultCustom(sb.toString().substring(0,sb.length()-1));
    	getWhole.setCode("200");
    	getWhole.setWhole(result.substring(0, result.length()-2));
    	return getWhole;
	}
	/**
	 * @param approverIds
	 * @return
	 * 自定义流程查看该申请的审核步骤详情
	 */
	public String getResultCustom(String approverIds){
		StringBuffer sb = new StringBuffer();
		String[] approverIdArray = approverIds.split(",");
		for(int i=0;i<approverIdArray.length;i++){
			//通过userid查找其部门
			PartyDTO department = partyConnector.findDepartmentById(approverIdArray[i]);
			PartyDTO company = partyConnector.findCompanyById(approverIdArray[i]);
			PartyDTO user = partyConnector.findById(approverIdArray[i]);
			sb.append(company.getName());
			sb.append(department.getName());
			sb.append(user.getName());
			sb.append("->");
		}
		return sb.toString();
	}
	/**
	 * @param list typeId typeName
	 * 业务细分更换其大类后，将kv_record表中相关流程数据的大类更新（目的：列表页的条件查询）
	 * TODO:sjx 18.11.6
	 */
	@Transactional(readOnly=false)
	public void updateKvRecordBusinessType(List<Record> list,String typeId,String typeName){
		for(Record record : list){
			String sql = "update kv_record set businessTypeId="+typeId+",businessTypeName='"+typeName+"'where id="+record.getCode();
			try{
				keyValueConnector.updateBySql(sql);
			}catch(Exception ex){
				ex.printStackTrace();
				logger.error(ex.getMessage()+"操作：更改业务细分的大类--更新kv_record表数据错误。");
			}
		}
	}
	public List<BpmConfBase> processDefinitionId(Long processDefinitionId) {
        List<BpmConfBase> bpmConfBase = bpmConfBaseManager.findBy("id", processDefinitionId);
        return bpmConfBase;
    }
    
    public List<BpmConfNode> activityId(Long bpmProcessId) {
        String hql = "from BpmConfNode where bpmConfBase.id = ? and type='userTask'";
        List<BpmConfNode> bpmConfNode = bpmConfNodeManager.find(hql, bpmProcessId);
        return bpmConfNode;
    }
    public String postByNodeId(Long nodeId) {
    	String hql = "from BpmConfUser where bpmConfNode.id = ?";
    	BpmConfUser bpmConfUser = bpmConfUserManager.findUnique(hql, nodeId);
    	String post = bpmConfUser.getValue();
    	return post;
    }
    public List<BpmConfNode> userTaskNode(String bpmProcessId){
    	BpmProcess bpmProcess = bpmProcessManager.findUniqueBy("id", Long.parseLong(bpmProcessId));
    	Long condBaseId = bpmProcess.getBpmConfBase().getId();
    	List<BpmConfNode> bpmConfNodes = activityId(condBaseId);
    	return bpmConfNodes;
    }
    /**
     * @param entity
     * @param linkResult
     * @param detailId
     * @author sjx
     */
    @Transactional(readOnly=false)
    public String saveBranchSetting(BranchApprovalLinkEntity entity,int linkResult,String detailId){
    	//先进行删除 再保存新的配置信息
    	String hql = "delete from BranchApprovalLinkEntity where businessDetailId=?";
    	int result = branchApprovalLinkEntityManager.batchUpdate(hql, detailId);
    	if(linkResult == 0){
    		return "redirect:/dict/dict-business-detail-list.do";
    	}
    	String conditionName = entity.getConditionName();
    	String conditionType = entity.getConditionType();
    	String conditionNode = entity.getConditionNode();
    	String note = entity.getNote();
    	String[] conditionTypes = conditionType.split(",");
    	String[] conditionNodes = conditionNode.split(",");
    	String[] conditionNames = conditionName.split(",");
    	String[] notes = note.split(",");
    	for(int i=0;i<linkResult;i++){
    		BranchApprovalLinkEntity branch = new BranchApprovalLinkEntity();
    		branch.setConditionType(conditionTypes[i]);
    		branch.setConditionNode(conditionNodes[i]);
    		branch.setBusinessDetailId(detailId);
    		branch.setConditionName(conditionNames[i]);
    		if(notes.length > i){
    			branch.setNote(notes[i]);
    		}
    		branchApprovalLinkEntityManager.save(branch);
    	}
    	return "";
    }
    public static class GetWhole{
    	String code;
    	String bpmProcessId;
    	String whole;
    	
		public String getCode() {
			return code;
		}
		public void setCode(String code) {
			this.code = code;
		}
		public String getBpmProcessId() {
			return bpmProcessId;
		}
		public void setBpmProcessId(String bpmProcessId) {
			this.bpmProcessId = bpmProcessId;
		}
		public String getWhole() {
			return whole;
		}
		public void setWhole(String whole) {
			this.whole = whole;
		}
    }
}
