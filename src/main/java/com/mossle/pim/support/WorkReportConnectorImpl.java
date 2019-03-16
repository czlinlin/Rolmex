package com.mossle.pim.support;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.activiti.engine.impl.cmd.AddCommentCmd;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.CollectionUtils;

import com.google.common.base.Joiner;
import com.mossle.api.dict.DictConnector;
import com.mossle.api.party.PartyConnector;
import com.mossle.api.party.PartyDTO;
import com.mossle.api.pim.WorkReportConnector;
import com.mossle.core.util.StringUtils;
import com.mossle.dict.persistence.domain.DictInfo;
import com.mossle.party.PartyConstants;
import com.mossle.party.persistence.domain.PartyEntity;
import com.mossle.party.persistence.manager.PartyEntityManager;
import com.mossle.party.support.PartyOrgConnector;
import com.mossle.pim.persistence.domain.WorkReportCcPresettingNode;
import com.mossle.pim.persistence.manager.WorkReportCcPresettingNodeManager;
import com.mossle.pim.persistence.manager.WorkReportInfoManager;
import com.mossle.user.persistence.domain.PersonInfo;
import com.mossle.user.persistence.manager.PersonInfoManager;
import com.mysql.fabric.xmlrpc.base.Array;

public class WorkReportConnectorImpl implements WorkReportConnector {
	@Autowired
	private JdbcTemplate jdbcTemplate;
	@Autowired
	private PartyOrgConnector partyOrgConnector;
	@Autowired
	private WorkReportCcPresettingNodeManager workReportCcPresettingNodeManager;
	@Autowired
	private PartyEntityManager partyEntityManager;
	@Autowired
	private DictConnector dictConnector;
	@Autowired
	private PartyConnector partyConnectors;
	@Autowired
	private PersonInfoManager personInfoManager;
	
	/**
     * 获取状态为正常使用的汇报条线.
     * add by lilei at 2018.12.12
     */
    public List<Map<String,Object>> getReportPreSetting(){
    	List<Map<String,Object>> returnMapsList=new ArrayList<Map<String,Object>>();
    	String strSql="SELECT pre.*,"
    			+" (SELECT IFNULL(REPLACE(GROUP_CONCAT(n.title ORDER BY n.node_level ASC),',','-'),'')"
    			+ " FROM  work_report_cc_presetting_node n WHERE presetting_id=pre.id) as node_title"
    			+" FROM work_report_cc_presetting pre where pre.status='1'";
    	returnMapsList=jdbcTemplate.queryForList(strSql);
    	return returnMapsList;
    }
    
    /**
     * 得到抄送规则(包括大区和非大区人员)
     * add by lilei at 2018.12.12
     * **/
    public Map<String,Object> getReportCCSetting(String userId){
    	Map<String,Object> returnMap=new HashMap<String, Object>();
    	PartyEntity partyEntity=partyOrgConnector.findPartyAreaByUserId(userId);
    	if(partyEntity==null){
    		returnMap.put("isArea",false);		//非大区人员
    	}
    	else {
    		returnMap.put("isArea",true);		//大区人员
    		List<Map<String,Object>> ccPreSettingMapList=getReportPreSetting();
    		returnMap.put("ccPresetting",ccPreSettingMapList);
		}
    	return returnMap;
    }
    
    /**
     * 是否是大区人员/岗位isArea=true表示是大区人员/岗位
     * ADD BY LILEI AT 2018-12-17
     * **/
    public Map<String,Object> getAreaInfo(String userId){
    	Map<String,Object> returnMap=new HashMap<String, Object>();
    	PartyEntity partyEntity=partyOrgConnector.findPartyAreaByUserId(userId);
    	if(partyEntity==null){
    		returnMap.put("isArea",false);		//非大区人员
    	}
    	else {
    		returnMap.put("isArea",true);		//大区人员
		}
    	return returnMap;
    }
    
    /**
     * 得到汇报的岗位信息（如果没有岗位，则返回人员信息）
     * ADD BY LILEI AT 2018.12.17
     * **/
    public Map<String,Object> getReportPositionInfo(String userId){
    	Map<String,Object> positionMap=new HashMap<String, Object>();
    	List<Map<String,Object>> positionMapList=partyOrgConnector.getPositionInfo(userId);
    	PersonInfo personInfo=personInfoManager.get(Long.parseLong(userId));
    	Long positionCodeLong=0L;
    	if(!StringUtils.isBlank(personInfo.getPositionCode()))
    		positionCodeLong=Long.parseLong(personInfo.getPositionCode());
    	positionMap.put("isManagerAbove", positionCodeLong>-1);//positionCodeLong>2);
    	//没有查询到数据，就是没有岗位信息 add by lilei at 2018.12.12
    	if(CollectionUtils.isEmpty(positionMapList)){
    		//String positionId=positionMapList.get(0).get("id").toString();
    		Map<String,Object> mapArae=getAreaInfo(userId);
    		positionMap.put("position_type", "person");
    		positionMap.put("isArea", Boolean.parseBoolean(mapArae.get("isArea").toString()));
    		positionMap.put("position_value",userId);
    	}
    	else if(positionMapList.size()==1){
    		String positionId=positionMapList.get(0).get("id").toString();
    		Map<String,Object> mapArae=getAreaInfo(positionId);
    		positionMap.put("position_type", "positionOne");
    		positionMap.put("isArea", Boolean.parseBoolean(mapArae.get("isArea").toString()));
    		positionMap.put("position_value",positionId);
		}
    	else{
    		positionMap.put("position_type", "positionList");
    		for (Map<String, Object> map : positionMapList) {
    			Map<String,Object> mapArae=getAreaInfo(map.get("id").toString());
				map.put("isArea", Boolean.parseBoolean(mapArae.get("isArea").toString()));
			}
    		positionMap.put("position_list", positionMapList);
    	}
    	return positionMap;
    }
    
    /**
     * 得到汇报抄送的人（根据设置条线）
     * 当strCCPreSettingId为空，非大区
     * add by lilei at 2018.12.12
     * **/
    public List<Map<String,Object>> getReportCCSettingPersonList(String strCCPreSettingId,String userId){
    	if(StringUtils.isBlank(strCCPreSettingId)){
    		partyOrgConnector.getSuperiorId(userId);
    	}
    	
    	return null;
    }
    
    /**
     * 得到上级/条线设置的抄送人员/岗位
     * ADD BY LILEI AT 2018.12.18
     * **/
    public String getReportPresettingccNos(String iptPositionId,Long preSettingId){
    	List<String> strCCNoList=new ArrayList<String>();
    	PartyEntity partyEntity=partyOrgConnector.findPartyAreaByUserId(iptPositionId);
    	if(partyEntity==null){
    		/**
    	     * 非大区人员获取抄送人员
    	     * 规则：从部门开始找到第一个公司的管理者后结束
    	     * ADD BY LILEI AT 2018.12.18
    	     * **/
    		List<String> noAreaCCNoList=partyOrgConnector.getManagerList(iptPositionId);
    		if(null!=noAreaCCNoList&&noAreaCCNoList.size()>0){
    			strCCNoList.addAll(noAreaCCNoList);
    		}
    	}
    	else {
    		//大区人员，不走条线
    		PartyEntity selfPartyEntity=partyEntityManager.get(Long.parseLong(iptPositionId));
    		if(selfPartyEntity.getPartyType().getId()==PartyConstants.PARTY_TYPE_POST){ //岗位
    			String hql="from WorkReportCcPresettingNode where presettingId=? order by nodeLevel asc";
    			//大区人员
        		List<WorkReportCcPresettingNode> ccPresettingNodeList=workReportCcPresettingNodeManager.find(hql, preSettingId);
        		if(!CollectionUtils.isEmpty(ccPresettingNodeList)){
        			for (WorkReportCcPresettingNode node : ccPresettingNodeList) {
        				//看看是否包含这个人的岗位
    					if((","+node.getPositionId()+",").contains(","+iptPositionId+",")){
    						if(strCCNoList.size()>0){
    							strCCNoList.clear();
    						}
    						continue;
    					}
    					List<String> nodeCCList=getAreaCCNos(node,iptPositionId,partyEntity.getId().toString());
    					if(nodeCCList.size()>0)
    						strCCNoList.addAll(nodeCCList);
    				}
        		}
    		}
		}
    	return Joiner.on(",").join(strCCNoList);
    }
    
    /**
     * 大区人员获取抄送人员
     * 规则：从部门开始找到第一个公司的管理者后结束
     * ADD BY LILEI AT 2018.12.18
     * node：条线节点实体
     * @param strAreaId：大区ID
     * **/
    private List<String> getAreaCCNos(WorkReportCcPresettingNode node,String iptPositionId,String strAreaId){
    	List<String> nodeCCList=new ArrayList<String>();
    	/**
		 * 抄送组策略：0：候选组，1：大区对接人，2：同一大区，3：同一分公司
		 * **/
    	if(!StringUtils.isBlank(node.getPositionId())){
			if(node.getPresettingType().equals("0")){//候选组
				nodeCCList=getRealPositionIds(node);
			} 
			else if(node.getPresettingType().equals("1")){//大区对接人
				List<String> positionList=getRealPositionIds(node);
        		List<DictInfo> dictInfoList = dictConnector.findDictInfoListByType("dataAuthority");//取得user负责的大区
        		
        		List<String> userIdList=new ArrayList<String>();
        		for (DictInfo dictInfo : dictInfoList) {
        			String[] strValues=dictInfo.getValue().split("-");
        			String strPositionId=strValues[1];
        			if(strPositionId.contains(strAreaId)){
        				String userId=strValues[0];
        				if(!userIdList.contains(userId))
        					userIdList.add(userId);
        			}
    			}
        		
        		if(userIdList.size()>0){
        			String strDictUseId=Joiner.on(",").join(userIdList);
        			String strPositionId=Joiner.on(",").join(positionList);
        			String strSql="SELECT DISTINCT s.PARENT_ENTITY_ID from party_struct s INNER JOIN party_entity p"
        					+ " ON p.ID=s.PARENT_ENTITY_ID AND p.DEL_FLAG='0'"
        					+ " where s.STRUCT_TYPE_ID=4 AND s.CHILD_ENTITY_ID in (%s) and  s.PARENT_ENTITY_ID in(%s);";
        			nodeCCList=jdbcTemplate.queryForList(String.format(strSql,strDictUseId,strPositionId),String.class);
        		}
			}
			else if(node.getPresettingType().equals("2")){//同一分公司
				PartyDTO companyPartyDTO=partyConnectors.findCompanyById(iptPositionId);
				List<String> positionList=getRealPositionIds(node);
				for (String positionId : positionList) {
					PartyDTO positionCompanyPartyDTO=partyConnectors.findCompanyById(positionId);
					if(companyPartyDTO.getId().equals(positionCompanyPartyDTO.getId())){
						if(!nodeCCList.contains(companyPartyDTO.getId().toString()))
							nodeCCList.add(positionId);
					}
				}
			}
			else if(node.getPresettingType().equals("3")){//同一大区
				List<String> positionList=getRealPositionIds(node);
				for (String positionId : positionList) {
					PartyDTO positionCompanyPartyDTO=partyConnectors.findAreaById(positionId);
					if(positionCompanyPartyDTO!=null){
						if(strAreaId.equals(positionCompanyPartyDTO.getId().toString())){
							if(!nodeCCList.contains(positionCompanyPartyDTO.getId().toString()))
								nodeCCList.add(positionId);
						}
					}
				}
			}
    	}
    	return nodeCCList;
    }
    
    private  List<String> getRealPositionIds(WorkReportCcPresettingNode node) {
    	//查找虚岗对应的真实岗位
		List<String> positionList=new ArrayList<String>();//.asList(node.getPositionId().split(","));
		String strSql="select * from party_entity_attr where ID in(%s)";// isRealPosition='1' and 
		List<Map<String,Object>> mapList=jdbcTemplate.queryForList(String.format(strSql, node.getPositionId()));
		if(null!=mapList&&mapList.size()>0){
			for (Map<String, Object> map : mapList) {
				/*Iterator<String> it=positionList.iterator();
				while(it.hasNext()){
					if(it.next().equals(map.get("ID").toString())){
						//it.remove();
						positionList.remove(it);
					}
				}*/
				//positionList.remove(map.get("ID").toString());
				String isRealPosition="0";
				if(map.get("isRealPosition")!=null)
					isRealPosition=map.get("isRealPosition").toString();
				if(isRealPosition.equals("0"))
					positionList.add(map.get("ID").toString());
				else{
					List<String> strRealPostionList=Arrays.asList(map.get("positionRealIds").toString().split(","));
					positionList.addAll(strRealPostionList);
				}
			}
		}
		return positionList;
	}
}
