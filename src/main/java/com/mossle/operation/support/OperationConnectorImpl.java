package com.mossle.operation.support;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.activiti.engine.impl.cmd.AddCommentCmd;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.mossle.api.form.FormDTO;
import com.mossle.api.humantask.HumanTaskConnector;
import com.mossle.api.humantask.HumanTaskDTO;
import com.mossle.api.keyvalue.KeyValueConnector;
import com.mossle.api.operation.OperationConnector;
import com.mossle.api.party.PartyDTO;
import com.mossle.base.persistence.domain.BusinessDetailEntity;
import com.mossle.base.persistence.manager.BusinessDetailManager;
import com.mossle.core.id.IdGenerator;
import com.mossle.core.util.StringUtils;
import com.mossle.operation.component.OperationConstants;
import com.mossle.operation.persistence.domain.CarApply;
import com.mossle.operation.persistence.manager.CarApplyManager;
import com.mossle.operation.service.CarApplyService;
import com.mossle.party.persistence.domain.PartyEntity;
import com.mossle.party.support.PartyConnectorImpl;
import com.mossle.party.support.PartyOrgConnector;
import com.mossle.user.persistence.domain.PersonInfo;
import com.mossle.user.persistence.manager.PersonInfoManager;
@Service
public class OperationConnectorImpl implements OperationConnector {
	private JdbcTemplate jdbcTemplate;
	@Autowired
	private KeyValueConnector keyValueConnector;
	@Autowired
	private IdGenerator idGenerator;
	@Autowired
	private PersonInfoManager personInfoManager;
	@Autowired
	private PartyOrgConnector partyOrgConnector;
	@Autowired
	private PartyConnectorImpl partyConnectorImpl;
	@Autowired
    private BusinessDetailManager businessDetailManager;
	@Autowired
	private HumanTaskConnector humanTaskConnector;
	@Autowired
	private CarApplyManager carApplyManager;
	@Autowired
	private CarApplyService carApplyService;
	
	/**
	 * 根据流程细分，得到流程条件是否需要输入金额  add by lilei at 2018.11.12
	 * **/
	public Map<String,Object> getProcessIsMoneyByDetailId(String detailId){
		Map<String,Object> mapReturn=new HashMap<String, Object>();
		String strSql=String.format("select * from oa_ba_process_condition where conditionType='audit-setting' and busDetailId=%s and conditionName='money'", detailId);
		List<Map<String,Object>> mapBoolenList=jdbcTemplate.queryForList(strSql);
		if(mapBoolenList.size()<1)
			mapReturn.put("ismoney",false);
		else {
			mapReturn.put("conditionName",mapBoolenList.get(0).get("conditionName"));
			mapReturn.put("ismoney",true);
		}
		return mapReturn;
	}
	
	/**
	 * 根据流程businessKey，得到流程条件是否需要输入金额  add by lilei at 2018.11.12
	 * **/
	public Map<String,Object> getProcessIsMoneyByBusinessKey(String businessKey){
		Map<String,Object> mapReturn=new HashMap<String, Object>();
		String strSql=String.format("select * from kv_record_condition where businessKey=%s and conditionName='money'", businessKey);
		List<Map<String,Object>> mapBoolenList=jdbcTemplate.queryForList(strSql);
		if(mapBoolenList.size()<1){
			mapReturn.put("ismoney",false);
			mapReturn.put("conditionValue","0");
		}
		else {
			mapReturn.put("conditionName",mapBoolenList.get(0).get("conditionName"));
			mapReturn.put("conditionValue",mapBoolenList.get(0).get("conditionValue"));
			mapReturn.put("ismoney",true);
		}
		return mapReturn;
	}
	
	/**
	 * 根据流程细分，得到输出或则选择的必需条件(暂未使用，方便以后扩展)  add by lilei at 2018.11.12
	 * **/
	public List<Map<String,Object>> getProcessInputList(String detailId){
		return null;
	}
	
	/**
	 * 根据流程细分，得到分支流程条件  add by lilei at 2018.11.12
	 * **/
	public List<Map<String,Object>> getBranchConditionList(String detailId){
		List<Map<String,Object>> mapContidionList=new ArrayList<Map<String,Object>>();
		String strSql=String.format("select * from oa_ba_process_condition where conditionType='audit-setting' and busDetailId=%s", detailId);
		mapContidionList=jdbcTemplate.queryForList(strSql);
		return mapContidionList;
	}
	
	/**
	 * 流程发起的分支条件 add lilei 2018.11.12
	 * **/
	public void InsertProcessConditionForMoney(String detailId,String businessKey,String contidionValue,String userId){
		List<Map<String,Object>> mapConditionList=new ArrayList<Map<String,Object>>();
    	mapConditionList=getBranchConditionList(detailId);
    	keyValueConnector.updateBySql(String.format("delete from kv_record_condition where businessKey=%s", businessKey));
    	String strInsertSql="insert into kv_record_condition(id,businessKey,conditionName,conditionValue,note)"
				+" values(%s,%s,'%s','%s','%s')";
    	if(mapConditionList.size()>0){
    		for (Map<String, Object> map : mapConditionList) {
    			String strContidion=map.get("conditionName").toString();
    			if(strContidion.equals("money")){
    				keyValueConnector.updateBySql(String.format(strInsertSql, 
    						idGenerator.generateId(),
    						businessKey,
    						strContidion,
    						contidionValue,
    						""));
    			}
    			else if(strContidion.equals("position")){
    				PersonInfo personInfo=personInfoManager.get(Long.parseLong(userId));
    				keyValueConnector.updateBySql(String.format(strInsertSql, 
    						idGenerator.generateId(),
    						businessKey,
    						strContidion,
    						personInfo.getPositionCode(),
    						""));
    			}
    			else if(strContidion.equals("area")){
    				PartyEntity partyEntity=partyOrgConnector.findPartyAreaByUserId(userId);
    				if(partyEntity==null)
	    				keyValueConnector.updateBySql(String.format(strInsertSql, 
	    						idGenerator.generateId(),
	    						businessKey,
	    						strContidion,
	    						"0",
	    						""));
    				else {
    					keyValueConnector.updateBySql(String.format(strInsertSql, 
	    						idGenerator.generateId(),
	    						businessKey,
	    						strContidion,
	    						"1",
	    						""));
					}
    			}
			}
    		
    		List<Map<String,Object>> mapCommonConditionList=new ArrayList<Map<String,Object>>();
    		String strCommonSql=String.format("select * from oa_ba_process_condition "
    				+ " where conditionType='common-setting' and conditionName='person-probation'"
    				+ " and busDetailId=%s",detailId);
    		mapCommonConditionList=jdbcTemplate.queryForList(strCommonSql);
    		if(mapCommonConditionList.size()>0){
    			Map<String,Object> mapCommonCondition=mapCommonConditionList.get(0);
    			keyValueConnector.updateBySql(String.format(strInsertSql, 
						idGenerator.generateId(),
						businessKey,
						mapCommonCondition.get("conditionName").toString(),
						userId,
						""));
    		}
    	}
    	
    	/**
    	 * 员工转正申请的用于定时任务 add by lilei at 2018.11.22
    	 * 条件限定(oa_ba_process_condition细分)：
    	 * （1）.isNeededData（用于控制是否走定时任务，1：走，0：不走）
    	 * （2）.note记录着值，2表示当前人为 试用期人员
    	 *  具体某个流程（kv_record_condition）
    	 *  （1）.conditionName='person-probation' 并且 conditionValue="1"，则走定时任务。
    	 * **/
    	List<Map<String,Object>> mapContidionCommonList=new ArrayList<Map<String,Object>>();
		String strSql=String.format("select * from oa_ba_process_condition"
				+ " where conditionType='common-setting' and conditionName='person-probation'"
				+ " and isNeededData='1' and note='2' and busDetailId=%s", detailId);
		mapContidionCommonList=jdbcTemplate.queryForList(strSql);
		if(mapContidionCommonList.size()>0){
			keyValueConnector.updateBySql(String.format(strInsertSql, 
					idGenerator.generateId(),
					businessKey,
					"person-probation",
					"1",
					userId));
		}
	}
	
	/**
	 * 审批步骤中出现流分支的判断 add by lilei at 2018.11.12
	 * **/
	public Map<String,Object> getBranchConditionMap(Map<String,Object> returnMap,String businessKey){
		List<Map<String,Object>> mapContidionList=new ArrayList<Map<String,Object>>();
		String strSql=String.format("select * from kv_record_condition where conditionName<>'person-probation' and businessKey=%s", businessKey);
		mapContidionList=jdbcTemplate.queryForList(strSql);
		if(mapContidionList.size()>0){
			for (Map<String, Object> map : mapContidionList) {
				//单独处理44059.7，随后可以删除此代码 add by lilei at 2019.01.14
				if(map.get("conditionName").toString().equals("money")&&map.get("conditionValue").toString().equals("44059.7")){
					returnMap.put(map.get("conditionName").toString(), 44059);
				}
				else{
					returnMap.put(map.get("conditionName").toString(), map.get("conditionValue"));
				}
			}
		}
		return returnMap;
	}
	
	/**
	 * 公共流程是否显示(公共流程时为调用，既category_id为3) add by lilei at 2018.11.16
	 * **/
	public boolean IsShowCommonProcess(Long bpmProcessId,String userId){
		if(StringUtils.isBlank(userId))
			return false;
		String strSql=String.format("SELECT c.conditionName FROM oa_ba_business_detail d "
				+" INNER JOIN oa_ba_process_condition c ON d.id=c.busDetailId "
				+" where c.conditionType='common-setting' and c.conditionName in('is-show','person-probation') and d.bpmProcessId=%s", bpmProcessId);
		List<String> mapContidionList=jdbcTemplate.queryForList(strSql,String.class);
		if(mapContidionList.size()>0)
		{
			//设置公共流程显示
			if(mapContidionList.contains("is-show")){
				if(mapContidionList.contains("person-probation")){
					PersonInfo personInfo=personInfoManager.get(Long.valueOf(userId));
					String laborType=personInfo.getLaborType()==null?"":personInfo.getLaborType().toString();
					if(laborType.equals("2")){
						String strCompanySql=String.format("SELECT c.note FROM oa_ba_business_detail d "
								+" INNER JOIN oa_ba_process_condition c ON d.id=c.busDetailId "	
								+" where c.conditionType='common-setting' and c.conditionName='company-value' and d.bpmProcessId=%s", bpmProcessId);
						List<String> mapCompanyList=jdbcTemplate.queryForList(strCompanySql,String.class);
						PartyDTO partyDTO=partyConnectorImpl.findCompanyById(userId);
						if(mapCompanyList.size()>0&&mapCompanyList.contains(partyDTO.getId())){
							return true;
						}
					}
					/*else {
						String strCompanySql=String.format("SELECT c.note FROM oa_ba_business_detail d "
								+" INNER JOIN oa_ba_process_condition c ON d.id=c.busDetailId "
								+" where c.conditionType='common-setting' and c.conditionName='company-value' and d.bpmProcessId=%s", bpmProcessId);
						List<String> mapCompanyList=jdbcTemplate.queryForList(strCompanySql,String.class);
						PartyDTO partyDTO=partyConnectorImpl.findCompanyById(userId);
						if(mapCompanyList.size()>0&&mapCompanyList.contains(partyDTO.getId())){
							return true;
						}
					}*/
				}
				else {
					String strCompanySql=String.format("SELECT c.note FROM oa_ba_business_detail d "
							+" INNER JOIN oa_ba_process_condition c ON d.id=c.busDetailId "
							+" where c.conditionType='common-setting' and c.conditionName='company-value' and d.bpmProcessId=%s", bpmProcessId);
					List<String> mapCompanyList=jdbcTemplate.queryForList(strCompanySql,String.class);
					PartyDTO partyDTO=partyConnectorImpl.findCompanyById(userId);
					if(mapCompanyList.size()>0&&mapCompanyList.contains(partyDTO.getId())){
						return true;
					}
				}
			}
		}
		return false;
	}
	
	/**
	 * 得到多分支的流程级别 ADD BY LILEI AT 2018.11.26
	 * **/
	public String GetBusinessLevel(String detailId,String userId){
		String strLevel="";
		List<Map<String,Object>> mapContidionList=getBranchConditionList(detailId);
		if(mapContidionList.size()>0){
			for (Map<String, Object> map : mapContidionList) {
				String strContidion=map.get("conditionName").toString();
				if(strContidion.equals("position")){
					PersonInfo personInfo=personInfoManager.get(Long.parseLong(userId));
					if(personInfo.getPositionCode()!=null){
						if(Integer.parseInt(personInfo.getPositionCode())>=3){
							strLevel="S";
						}
					}
				}
			}
		}
		return strLevel;
	}
	
	/**
	 * 获取流程标题 [add by lilei at 2018.11.19][暂时用于通用表单，其他表单以后也可以用此查询]
	 * */
	public String getProcessTitle(String detailId){
		if(StringUtils.isBlank(detailId))
			return "";
		String bpmProcessTitle="";
    	BusinessDetailEntity businessDetail=businessDetailManager.get(Long.parseLong(detailId));
    	/*Long bpmProcessId=0L;
		if(businessDetail!=null&&businessDetail.getBpmProcessId()!=null){
			try {
				bpmProcessId=Long.parseLong(businessDetail.getBpmProcessId());
			} catch (Exception e) {
				// TODO: handle exception
				bpmProcessId=0L;
			}
			
		}
        if(bpmProcessId!=null){
        	//获取流程标题
        	List<Map<String,Object>> mapBpmProcessAttrList
        				=jdbcTemplate.queryForList(String.format("SELECT * FROM bpm_process_attr WHERE bpmProcessId=%s",bpmProcessId));
        	if(mapBpmProcessAttrList!=null&&mapBpmProcessAttrList.size()>0){
        		Map<String,Object> mapBpmAttr=mapBpmProcessAttrList.get(0);
        		bpmProcessTitle=mapBpmAttr.get("bpmProcessTitle")==null?"":mapBpmAttr.get("bpmProcessTitle").toString();
        	}
        }*/
    	//ckx 2019/2/18  修改获取流程标题
    	if(null != businessDetail){
    		if(com.mossle.common.utils.StringUtils.isNotBlank(businessDetail.getTitle())){
    			bpmProcessTitle = businessDetail.getTitle();
    		}
    	}
        return bpmProcessTitle;
	}
	
	/**
	 * 得到步骤审批表单
	 * ADD BY LILEI AT 2019.02.15
	 * **/
	public String getStepAuditForm(String humanTaskId){
		String strAuditForm="";
		try{
			FormDTO formDto = humanTaskConnector.findTaskForm(humanTaskId);
			if(formDto!=null){
				if(!StringUtils.isBlank(formDto.getUrl())){
					strAuditForm=formDto.getUrl();
				}
			}
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
		return strAuditForm;
	}
	
	/**
	 * 获取特殊表单信息
	 * ADD BY LILEI AT 2019.02.18
	 * **/
	public Map<String,String> getSpecialFormInfo(String strPerCode,String applyCode,String pk_id){
		Map<String,String> mapFormInfo=new HashMap<String, String>();
		try{
			List<Map<String,Object>> mapActiveTaskList=jdbcTemplate.queryForList(
					"select i.ID from kv_record r inner join task_info i on r.BUSINESS_KEY=i.BUSINESS_KEY"
					+" where i.CATALOG<>'copy' AND i.status='active' AND i.ASSIGNEE="+strPerCode+" and r.applycode='"+applyCode+"'"
					+ " order by r.CREATE_TIME ASC LIMIT 0,1" );
	        if(mapActiveTaskList!=null&&mapActiveTaskList.size()>0){
	        	String strHumanTaskId=mapActiveTaskList.get(0).get("ID").toString();
	    		String url=getStepAuditForm(strHumanTaskId);
	    		if(url.equals(OperationConstants.Form_CAR_START_STRING)){
	    			String strPlateNumber="";
	    			String strDriver="";
	    			CarApply modelCarApply=carApplyManager.get(Long.valueOf(pk_id));
	    			if(modelCarApply!=null){
	    				strPlateNumber=getNotNullString(modelCarApply.getPlateNumber());
	    				strDriver=getNotNullString(modelCarApply.getDriver());
	    			}
	    			mapFormInfo.put("isSpecialForm", "true");
	    			mapFormInfo.put("formType","car");
	    			mapFormInfo.put("step","start");
	    			mapFormInfo.put("plateNumber",strPlateNumber);
	    			mapFormInfo.put("driver",strDriver);
	    		}
	    		else if(url.equals(OperationConstants.Form_CAR_END_STRING)){
	    			String borrowCarMileage="";
	    			String returnCarMileage="";
	    			String mileage="";
	    			String oilMoney="";
	    			String remainOil="";
	    			CarApply modelCarApply=carApplyManager.get(Long.valueOf(pk_id));
	    			if(modelCarApply!=null){
	    				borrowCarMileage=getNotNullString(modelCarApply.getBorrowCarMileage());
	    				returnCarMileage=getNotNullString(modelCarApply.getReturnCarMileage());
	    				mileage=getNotNullString(modelCarApply.getMileage());
	    				oilMoney=getNotNullString(modelCarApply.getOilMoney());
	    				remainOil=getNotNullString(modelCarApply.getRemainOil());
	    			}
	    			mapFormInfo.put("isSpecialForm", "true");
	    			mapFormInfo.put("formType","car");
	    			mapFormInfo.put("step","end");
	    			mapFormInfo.put("borrowCarMileage",borrowCarMileage);
	    			mapFormInfo.put("returnCarMileage",returnCarMileage);
	    			mapFormInfo.put("mileage",mileage);
	    			mapFormInfo.put("oilMoney",oilMoney);
	    			mapFormInfo.put("remainOil",remainOil);
	    		}
	    		else {
	    			mapFormInfo.put("isSpecialForm", "false");
	    			mapFormInfo.put("formType","");
	    			mapFormInfo.put("step","");
				}
	        }
		}
        catch(Exception ex){
        	ex.printStackTrace();
        }
        return mapFormInfo;
	}
	
	/**
	 * 设置特殊审批表单的实体和存储html
	 * ADD BY LILEI AT 2019.02.18
	 * **/
	public void setSpecialFormInfo(String pk_id,String bussinessKey,Map<String, Object> decryptedMap){
		if(decryptedMap.containsKey("formType")){
			String formType=decryptedMap.get("formType").toString();
			if(formType.equals("carStart")){
				CarApply modelCarApply=carApplyManager.get(Long.valueOf(pk_id));
				if(modelCarApply!=null){
					modelCarApply.setPlateNumber(getObjectNotNull(decryptedMap.get("plateNumber")));
					modelCarApply.setDriver(getObjectNotNull(decryptedMap.get("driver")));
					carApplyManager.save(modelCarApply);
					carApplyService.saveFormHtml(modelCarApply, bussinessKey);
				}
			}
			else if(formType.equals("carEnd")){
				CarApply modelCarApply=carApplyManager.get(Long.valueOf(pk_id));
				if(modelCarApply!=null){
					modelCarApply.setBorrowCarMileage(getObjectNotNull(decryptedMap.get("borrowCarMileage")));
					modelCarApply.setReturnCarMileage(getObjectNotNull(decryptedMap.get("returnCarMileage")));
					modelCarApply.setMileage(getObjectNotNull(decryptedMap.get("mileage")));
					modelCarApply.setOilMoney(getObjectNotNull(decryptedMap.get("oilMoney")));
					modelCarApply.setRemainOil(getObjectNotNull(decryptedMap.get("remainOil")));
					carApplyManager.save(modelCarApply);
					carApplyService.saveFormHtml(modelCarApply, bussinessKey);
				}
			}
		}
	}
	
	private String getNotNullString(String strValue){
		return strValue==null?"":strValue;
	}
	
	private String getObjectNotNull(Object strValue){
		return strValue==null?"":strValue.toString();
	}
	
	@Resource
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate){
		this.jdbcTemplate=jdbcTemplate;
	}
}