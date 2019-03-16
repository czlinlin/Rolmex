package com.mossle.api.operation;

import java.util.List;
import java.util.Map;

public interface OperationConnector {
	/**
	 * 根据流程细分，得到流程条件是否需要输入金额  add by lilei at 2018.11.12
	 * **/
	Map<String,Object> getProcessIsMoneyByDetailId(String detailId);
	
	/**
	 * 根据流程businessKey，得到流程条件是否需要输入金额  add by lilei at 2018.11.12
	 * **/
	Map<String,Object> getProcessIsMoneyByBusinessKey(String businessKey);
	
	/**
	 * 根据流程细分，得到输出或则选择的必需条件  add by lilei at 2018.11.12
	 * **/
	List<Map<String,Object>> getProcessInputList(String detailId);
	
	/**
	 * 根据流程细分，得到分支流程条件  add by lilei at 2018.11.12
	 * **/
	List<Map<String,Object>> getBranchConditionList(String detailId);
	
	/**
	 * 流程发起的分支条件 add lilei 2018.11.12
	 * **/
	void InsertProcessConditionForMoney(String detailId,String businessKey,String contidionValue,String userId);
	
	/**
	 * 审批步骤中出现流分支的判断 add by lilei at 2018.11.12
	 * **/
	Map<String,Object> getBranchConditionMap(Map<String,Object> returnMap,String detailId);
	
	/**
	 * 公共流程是否显示(公共流程时为调用，既category_id为3) add by lilei at 2018.11.16
	 * **/
	boolean IsShowCommonProcess(Long bpmProcessId,String userId);
	
	/**
	 * 得到多分支的流程级别 ADD BY LILEI AT 2018.11.26
	 * **/
	String GetBusinessLevel(String detailId,String userId);
	
	/**
	 * 获取流程标题 [add by lilei at 2018.11.19][暂时用于通用表单，其他表单以后也可以用此查询]
	 * */
	String getProcessTitle(String detailId);
	
	/**
	 * 得到步骤审批表单
	 * ADD BY LILEI AT 2019.02.15
	 * **/
	String getStepAuditForm(String humanTaskId); 
	
	/**
	 * 获取特殊表单信息
	 * ADD BY LILEI AT 2019.02.18
	 * **/
	Map<String,String> getSpecialFormInfo(String strPerCode,String applyCode,String pk_id);
	
	/**
	 * 设置特殊审批表单的实体和存储html
	 * ADD BY LILEI AT 2019.02.18
	 * **/
	void setSpecialFormInfo(String pk_id,String bussinessKey,Map<String, Object> decryptedMap);
}