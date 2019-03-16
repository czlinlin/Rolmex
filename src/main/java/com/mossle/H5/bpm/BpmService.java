/**
 * 
 */
package com.mossle.H5.bpm;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.mossle.api.org.OrgConnector;
import com.mossle.api.party.PartyDTO;
import com.mossle.core.auth.CustomPasswordEncoder;
import com.mossle.core.page.Page;
import com.mossle.core.util.StringUtils;
import com.mossle.humantask.persistence.domain.TaskInfo;
import com.mossle.humantask.persistence.manager.TaskInfoManager;
import com.mossle.humantask.support.HumanTaskConnectorImpl;
import com.mossle.operation.service.OperationService;
import com.mossle.party.rs.PartyResource;
import com.mossle.user.persistence.domain.AccountCredential;
import com.mossle.user.persistence.domain.AccountInfo;
import com.mossle.user.persistence.manager.AccountCredentialManager;
import com.mossle.user.persistence.manager.AccountInfoManager;

/**
 * @author Bing
 *
 */
@Service
public class BpmService {
	private JdbcTemplate jdbcTemplate;
	private TaskInfoManager taskInfoManager;
	private AccountInfoManager accountInfoManager;
	private AccountCredentialManager accountCredentialManager;
	private CustomPasswordEncoder customPasswordEncoder;
	private HumanTaskConnectorImpl humanTaskConnectorImpl;
	private OrgConnector orgConnector;
	private PartyResource partyResource;
	@Autowired
	private OperationService operationService;
	
	/**
	 * （参与岗位的）待领审批列表 Bing 2017.11.30
	 * 
	 * @param decryptedMap
	 * @return
	 */
	public Map<String, Object> ParticipantTask(Map<String, Object> decryptedMap) {
		// {strPageSize=5, percode=2, method=BpmParticipantTask, strPageIndex=1}

		Map<String, Object> returnMap = new HashMap<String, Object>();

		try {
			// 验证参数================================================================================
			if (!decryptedMap.containsKey("percode") || !decryptedMap.containsKey("strPageIndex")
					|| !decryptedMap.containsKey("strPageSize")) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "参数错误");
				return returnMap;
			}

			// 获取数据================================================
			
			String userId = decryptedMap.get("userId").toString();
			String percode =decryptedMap.get("percode").toString();
			String strFRCode =decryptedMap.get("strFRCode").toString();
			String strRealName =decryptedMap.get("strRealName").toString();
			// String strStatus =decryptedMap.get("strStatus").toString();
			String strUserID =decryptedMap.get("strUserID").toString();
			String strBusinessType =decryptedMap.get("strBusinessType").toString();
			String strBDID =decryptedMap.get("strBDID").toString();
			String strSystem =decryptedMap.get("strSystem").toString();
			String strArea =decryptedMap.get("strArea").toString();
			String strCompany =decryptedMap.get("strCompany").toString();
			String strStart =decryptedMap.get("strStart").toString();
			String strEnd =decryptedMap.get("strEnd").toString();
			
			
			long pageIndex = Long.valueOf(decryptedMap.get("strPageIndex").toString());
			long row_count = Long.valueOf(decryptedMap.get("strPageSize").toString());
			long offset = row_count * (pageIndex - 1);
			
		    List<String> partyIds = new ArrayList<String>();
	        String strIds = "";
	        partyIds.addAll(humanTaskConnectorImpl.findGroupIds(percode));
	        partyIds.addAll(humanTaskConnectorImpl.findUserIds(percode));


	        for (String str : partyIds) {
	            strIds += "'" + str + "',";
	        }

	        strIds = strIds.substring(0, strIds.length() - 1);
			
			StringBuffer buff = new StringBuffer();
			buff.append("select t.id,t.process_instance_id,t.action,t.applyCode,t.presentation_subject,t.theme,t.user_id,t.full_name as USER_NAME,");
			buff.append("t.ucode,t.businesstypeid,t.businessTypeName,t.businessdetailid,t.businessDetailName,t.systemid,t.systemName,");
			buff.append("t.areaid,t.areaName,t.companyid,t.companyName,t.CREATE_TIME,t.catalog,t.url,t.business_key,t.TASK_ID");
			//buff.append(",(SELECT MAX( COMPLETE_TIME ) FROM `task_info` ti WHERE t.BUSINESS_KEY = ti.BUSINESS_KEY AND `STATUS` = 'complete' AND CATALOG NOT IN ( 'start', 'copy' )) AS COMPLETE_TIME");
			buff.append(" from (select DISTINCT i.id,i.process_instance_id,i.action,r.applycode,i.presentation_subject,");
			buff.append("r.theme,r.user_id,p.full_name,r.ucode,r.businesstypeid,r.businesstypename,r.businessdetailid,");
			buff.append("r.businessdetailname,r.systemid,r.systemname,r.areaid,r.areaname,r.companyid,r.companyname,");
			buff.append("r.create_time,i.catalog,t_p.REF,r.url,i.business_key,r.audit_status as pro_status,i.ID as TASK_ID" + " from task_info i");
			buff.append(" inner join task_participant t_p on i.id = t_p.task_id");
			buff.append(" inner join kv_record r on i.business_key = r.id");
			buff.append(" inner join person_info p on r.user_id = p.id");
			buff.append(" where ifnull(i.ASSIGNEE,'')='' and i.`status`='active') t where REF in (").append(strIds).append(")");
	        
	        
	        //受理单号
			if(strFRCode!=null&&!strFRCode.equals(""))
				buff.append(" and t.applycode like '%"+strFRCode+"%'");
			//申请人
			if(strRealName!=null&&!strRealName.equals(""))
				buff.append(" and t.full_name like '%"+strRealName+"%'");
			//状态
			/*if(strStatus!=null&&!strStatus.equals(""))
				//buff.append(" and t.chrStatus="+strStatus);
				buff.append(" and t.pro_status="+strStatus);*/
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
			/*if(strArea!=null&&!strArea.equals("")) {
				buff.append(" and t.areaid="+strArea);
			}*/
			
			//分公司
			if(strCompany!=null&&!strCompany.equals("")) {
				buff.append(" and t.companyid="+strCompany);
			}

			//申请日期开始
			if(strStart!=null&&!strStart.equals(""))
				buff.append(" and t.create_time>='"+strStart+" 00:00:00'");
			//申请日期结束
			if(strEnd!=null&&!strEnd.equals(""))
				buff.append(" and t.create_time<='"+strEnd+" 23:59:59'");
			
			buff.append(" order by create_time  LIMIT ?,?");;
			
			// 查数据
			// String sql = "SELECT * FROM v_h5_task_participant WHERE CHILD_ENTITY_ID in (" + strIds + ") LIMIT ?,?";
			String sql = buff.toString();
			List<Map<String, Object>> list = jdbcTemplate.queryForList(sql,
					new Object[] {  offset, row_count });

			// 返回======================================================================
			returnMap.put("bSuccess", "true");

			if (list.size() > 0) {
				returnMap.put("strMsg", "加载成功");
				// 处理主题
				for (Map<String, Object> map : list) {
					if (StringUtils.isNotBlank(map.get("theme").toString())) {
						map.put("businessDetailName", map.get("theme").toString());
					}
				}
				returnMap.put("list", list);
			} else {
				returnMap.put("strMsg", "暂无数据");
			}
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("bSuccess", "false");
			returnMap.put("strMsg", "加载错误，请联系管理员");
		}

		return returnMap;
	}

	/**
	 * 认领 Bing 2017.11.30
	 * 
	 * @param decryptedMap
	 * @return
	 */
	public Map<String, Object> ClaimTask(Map<String, Object> decryptedMap) {
		// {method=BpmClaimTask,strPerCode=2,taskId=}

		Map<String, Object> returnMap = new HashMap<String, Object>();

		try {
			// 验证参数================================================================================
			if (!decryptedMap.containsKey("strPerCode") || !decryptedMap.containsKey("taskId")) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "参数错误");
				return returnMap;
			}

			// 获取数据================================================
			String strPerCode = decryptedMap.get("strPerCode").toString();
			String taskId = decryptedMap.get("taskId").toString();
			String pwd = decryptedMap.get("strPrivateKey").toString();

			// 验证操作密码================================================
			AccountInfo accountInfo = accountInfoManager.get(Long.parseLong(strPerCode));
			String hql = "from AccountCredential where accountInfo=? and catalog='default'";
			AccountCredential accountCredential = accountCredentialManager.findUnique(hql, accountInfo);
			if (!isPasswordValid(pwd, accountCredential.getOperationPassword())) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "操作密码错误");
				return returnMap;
			}

			// 处理数据
			// TaskService taskService = processEngine.getTaskService();
			// taskService.claim(taskId, strPerCode);
			TaskInfo taskInfo = taskInfoManager.get(Long.parseLong(taskId));
			if(!StringUtils.isBlank(taskInfo.getAssignee())){
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "该流程已被认领");
				return returnMap;
			}
			
			taskInfo.setAssignee(strPerCode);
			taskInfoManager.save(taskInfo);
			
			try {
				operationService.setClaimPosition(taskId,strPerCode);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// 返回======================================================================
			returnMap.put("bSuccess", "true");
			returnMap.put("strMsg", "认领成功");
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("bSuccess", "false");
			returnMap.put("strMsg", "认领失败");
		}

		return returnMap;
	}

	/**
	 * 公司或部门流程申请查询 Bing 2017.12.19
	 * 
	 * @param decryptedMap
	 * @return
	 */
	public Map<String, Object> ProcessApplyList(Map<String, Object> decryptedMap) {

		Map<String, Object> returnMap = new HashMap<String, Object>();

		try {
			// 验证参数================================================================================
			if (!decryptedMap.containsKey("strPageIndex") || !decryptedMap.containsKey("strPageSize")) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "参数错误");
				return returnMap;
			}

			String code = "";
			if (decryptedMap.containsKey("COMPANY_CODE")) {
				code += decryptedMap.get("COMPANY_CODE").toString();
			}
			if (decryptedMap.containsKey("DEPARTMENT_CODE")) {
				code += decryptedMap.get("DEPARTMENT_CODE").toString();
			}
			if (code.isEmpty()) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "参数错误");
				return returnMap;
			}

			// 获取数据================================================
			List<Map<String, Object>> list = queryProcessApplyList(decryptedMap);

			// 返回======================================================================
			returnMap.put("bSuccess", "true");
			if (list.size() > 0) {
				for (Map<String, Object> map : list) {
					if (StringUtils.isBlank(map.get("theme").toString())) {
						map.put("theme", map.get("businessDetailName").toString());
					}
				}
				returnMap.put("strMsg", "加载成功");
				returnMap.put("DataList", list);
			} else {
				returnMap.put("strMsg", "暂无数据");
			}
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("bSuccess", "false");
			returnMap.put("strMsg", "加载错误，请联系管理员");
		}

		return returnMap;
	}

	/**
	 * 公司或部门流程审批查询 Bing 2017.12.20
	 * 
	 * @param decryptedMap
	 * @return
	 */
	public Map<String, Object> ProcessAuditList(Map<String, Object> decryptedMap) {

		Map<String, Object> returnMap = new HashMap<String, Object>();

		try {
			// 验证参数================================================================================
			if (!decryptedMap.containsKey("strPageIndex") || !decryptedMap.containsKey("strPageSize")) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "参数错误");
				return returnMap;
			}

			String code = "";
			if (decryptedMap.containsKey("COMPANY_CODE")) {
				code += decryptedMap.get("COMPANY_CODE").toString();
			}
			if (decryptedMap.containsKey("DEPARTMENT_CODE")) {
				code += decryptedMap.get("DEPARTMENT_CODE").toString();
			}
			if (code.isEmpty()) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "参数错误");
				return returnMap;
			}

			// 获取数据================================================
			List<Map<String, Object>> list = queryProcessAuditList(decryptedMap);

			// 返回======================================================================
			returnMap.put("bSuccess", "true");
			if (list.size() > 0) {
				for (Map<String, Object> map : list) {
					if (StringUtils.isBlank(map.get("theme").toString())) {
						map.put("theme", map.get("businessDetailName").toString());
					}
				}
				returnMap.put("strMsg", "加载成功");
				returnMap.put("DataList", list);
			} else {
				returnMap.put("strMsg", "暂无数据");
			}
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("bSuccess", "false");
			returnMap.put("strMsg", "加载错误，请联系管理员");
		}

		return returnMap;
	}

	List<Map<String, Object>> queryProcessApplyList(Map<String, Object> decryptedMap) {
		ArrayList<Object> argsList = new ArrayList<Object>();// sql参数列表

		// 查询条件--------------------------------------------------------------------
		String where_definition = " 1=1 ";
/*
		// 公司id
		if (decryptedMap.containsKey("COMPANY_CODE")) {
			if (!decryptedMap.get("COMPANY_CODE").toString().isEmpty()) {
				where_definition += " and COMPANY_CODE=? ";
				argsList.add(decryptedMap.get("COMPANY_CODE"));
			}
		}

		// 部门id
		if (decryptedMap.containsKey("DEPARTMENT_CODE")) {
			if (!decryptedMap.get("DEPARTMENT_CODE").toString().isEmpty()) {
				where_definition += " and DEPARTMENT_CODE=? ";
				argsList.add(decryptedMap.get("DEPARTMENT_CODE"));
			}
		}*/

		// 申请单号
		if (decryptedMap.containsKey("applyCode")) {
			if (!decryptedMap.get("applyCode").toString().isEmpty()) {
				where_definition += " and applyCode=? ";
				argsList.add(decryptedMap.get("applyCode"));
			}
		}

		// 主题
		if (decryptedMap.containsKey("theme")) {
			String theme = decryptedMap.get("theme").toString();
			if (!theme.isEmpty()) {
				where_definition += " and theme like ? ";
				argsList.add("%" + theme + "%");
			}
		}

		// 申请人
		if (decryptedMap.containsKey("FULL_NAME")) {
			String FULL_NAME = decryptedMap.get("FULL_NAME").toString();
			if (!FULL_NAME.isEmpty()) {
				where_definition += " and FULL_NAME like ? ";
				argsList.add("%" + FULL_NAME + "%");
			}
		}

		// 经销商编号
		if (decryptedMap.containsKey("ucode")) {
			if (!decryptedMap.get("ucode").toString().isEmpty()) {
				where_definition += " and ucode=? ";
				argsList.add(decryptedMap.get("ucode"));
			}
		}

		// 业务类型
		if (decryptedMap.containsKey("businessTypeId")) {
			if (!decryptedMap.get("businessTypeId").toString().isEmpty()) {
				where_definition += " and businessTypeId=? ";
				argsList.add(decryptedMap.get("businessTypeId"));
			}
		}

		// 业务细分
		if (decryptedMap.containsKey("businessDetailId")) {
			if (!decryptedMap.get("businessDetailId").toString().isEmpty()) {
				where_definition += " and businessDetailId=? ";
				argsList.add(decryptedMap.get("businessDetailId"));
			}
		}

		// 体系
		if (decryptedMap.containsKey("systemId")) {
			if (!decryptedMap.get("systemId").toString().isEmpty()) {
				where_definition += " and systemId=? ";
				argsList.add(decryptedMap.get("systemId"));
			}
		}

		// 状态
		if (decryptedMap.containsKey("audit_status")) {
			if (!decryptedMap.get("audit_status").toString().isEmpty()) {
				where_definition += " and audit_status=? ";
				argsList.add(decryptedMap.get("audit_status"));
			}
		}

		// 申请时间
		if (decryptedMap.containsKey("begin_CREATE_TIME")) {
			if (!decryptedMap.get("begin_CREATE_TIME").toString().isEmpty()) {
				where_definition += " and CREATE_TIME>=? ";
				argsList.add(decryptedMap.get("begin_CREATE_TIME"));
			}
		}

		if (decryptedMap.containsKey("end_CREATE_TIME")) {
			String end_CREATE_TIME = decryptedMap.get("end_CREATE_TIME").toString();
			if (!end_CREATE_TIME.isEmpty()) {
				try {
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
					Date endPublishDate = sdf.parse(end_CREATE_TIME);

					Calendar calendar = Calendar.getInstance();
					calendar.setTime(endPublishDate);
					calendar.add(Calendar.DATE, 1);

					where_definition += " and CREATE_TIME<? ";
					argsList.add(calendar.getTime());
				} catch (ParseException e) {
					// TODO 自动生成的 catch 块
					e.printStackTrace();
				}
			}
		}
		
		/**
         * 获得管理者对应的组织机构.
         */
		String orgID = "";
		String departmentId="";
		String percode =decryptedMap.get("percode").toString();
        List<Long> isManager =  orgConnector.getPartyByManageId(percode);
        if(isManager!=null&&isManager.size()>0){
        	for(int i =0;i<isManager.size();i++){
        		orgID = orgID + isManager.get(i).toString() + ",";
        	}
        	orgID = orgID.substring(0, orgID.length()-1);	
        
	        //寻找该 组织机构 下面所有的组织机构
	    	List<String> list = partyResource.getPartyEntityNoPerson(orgID);
	    	List<String> newList = removeDuplicateWithOrder(list);
	    	//遍历结果，将这些组织机构的id放入一个字符串
	    	for(int i =0;i<newList.size();i++){
	    		departmentId= departmentId + newList.get(i)+",";
	    	}
	    	if (StringUtils.isNotBlank(departmentId)) {
	    		departmentId = departmentId.substring(0, departmentId.length()-1);	
	    		departmentId = departmentId + "," + orgID;
	    	} else {
	    		departmentId = orgID;
	    	}
        } else {
        	List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        	return list;  
        }
        
    	if(StringUtils.isBlank(departmentId)) {
    		departmentId = "9999";
    	}
    	
		StringBuffer sqlFrom = new StringBuffer();
		sqlFrom.append("SELECT r.ID AS ID,r.CATEGORY AS CATEGORY,r.STATUS AS STATUS,r.REF AS REF,");
		sqlFrom.append("r.USER_ID AS USER_ID,r.CREATE_TIME AS CREATE_TIME,r.NAME AS NAME,r.FORM_TEMPLATE_CODE AS FORM_TEMPLATE_CODE,");
		sqlFrom.append("r.TENANT_ID AS TENANT_ID,r.BUSINESS_KEY AS BUSINESS_KEY,r.theme,r.applyCode AS applyCode,r.ucode AS ucode,");
		sqlFrom.append("r.businessTypeId AS businessTypeId,r.businessTypeName AS businessTypeName,r.businessDetailId AS businessDetailId,");
		sqlFrom.append("r.businessDetailName AS businessDetailName,r.systemId AS systemId,r.systemName AS systemName,");
		sqlFrom.append("r.areaId AS areaId,r.areaName AS areaName,r.submitTimes AS submitTimes,r.end_time AS end_time,");
		sqlFrom.append("r.companyId AS companyId,r.companyName AS companyName,r.url AS url,r.detailHtml AS detailHtml,");
		sqlFrom.append("r.pk_id AS pk_id,r.audit_status AS audit_status,");
		sqlFrom.append("GET_DICT_NAME_BY_TYPE_NAME ('RecordStatus',r.audit_status) AS audit_status_dict,");
		sqlFrom.append("r.apply_content AS apply_content,p.FULL_NAME AS FULL_NAME,p.COMPANY_CODE AS COMPANY_CODE,");
		sqlFrom.append("p.COMPANY_NAME AS COMPANY_NAME,p.DEPARTMENT_CODE AS DEPARTMENT_CODE,p.DEPARTMENT_NAME AS DEPARTMENT_NAME,");
		sqlFrom.append("p.POSITION_CODE AS POSITION_CODE,p.POSITION_NAME AS POSITION_NAME");
		sqlFrom.append(" FROM kv_record r");
		sqlFrom.append(" JOIN person_info p ON p.ID = r.USER_ID");
		sqlFrom.append(" JOIN task_info t ON r.id = t.BUSINESS_KEY");
		sqlFrom.append(" JOIN task_info_approve_position tap ON t.ID = tap.task_id");
		sqlFrom.append(" jOIN party_entity pe ON pe.id = tap.position_parentId");
		sqlFrom.append(" where t.CATALOG = 'start' AND pe.id IN (").append(departmentId).append(")");
		
		String sql = "SELECT * FROM (" + sqlFrom + ") r where " + where_definition
				+ " order by CREATE_TIME DESC LIMIT ?,?";// 按“申请时间倒序”排列

		long pageIndex = Long.valueOf(decryptedMap.get("strPageIndex").toString());
		long row_count = Long.valueOf(decryptedMap.get("strPageSize").toString());
		long offset = row_count * (pageIndex - 1);
		argsList.add(offset);
		argsList.add(row_count);

		// System.out.println(sql);
		// System.out.println(argsList);
		return jdbcTemplate.queryForList(sql, argsList.toArray());
		
	}
	
	// 去重
    private List<String> removeDuplicateWithOrder(List<String> list) {    
        Set set = new HashSet();    
        List newList = new ArrayList();    
       for (Iterator iter = list.iterator(); iter.hasNext();) {    
             Object element = iter.next();    
             if (set.add(element))    
                newList.add(element);    
          }     
         list.clear();    
         list.addAll(newList);    
         return list;
     }  
    
	List<Map<String, Object>> queryProcessAuditList(Map<String, Object> decryptedMap) {
		ArrayList<Object> argsList = new ArrayList<Object>();// sql参数列表

		// 查询条件====================================
		// 子条件-------------------------------------------------------------
		// String where_definition_sub = " t.BUSINESS_KEY = r.BUSINESS_KEY and t.CATALOG not in('copy','start') and t.ASSIGNEE <> r.USER_ID and t.STATUS='complete' ";

		/*// 公司id
		if (decryptedMap.containsKey("COMPANY_CODE")) {
			if (!decryptedMap.get("COMPANY_CODE").toString().isEmpty()) {
				where_definition_sub += " and COMPANY_CODE=? ";
				argsList.add(decryptedMap.get("COMPANY_CODE"));
			}
		}

		// 部门id
		if (decryptedMap.containsKey("DEPARTMENT_CODE")) {
			if (!decryptedMap.get("DEPARTMENT_CODE").toString().isEmpty()) {
				where_definition_sub += " and DEPARTMENT_CODE=? ";
				argsList.add(decryptedMap.get("DEPARTMENT_CODE"));
			}
		}

		String sql_sub = " SELECT * FROM task_info t JOIN person_info p ON t.ASSIGNEE = p.ID  WHERE  "
				+ where_definition_sub;

		// 总条件：不是申请，存在审批（不是抄送、发起，申请人也不能是审批人）---------------------------------------------------------------------
		String where_definition = " EXISTS ( " + sql_sub + ")  ";// r.audit_status<>0
        */
		String where_definition = "";
		// 申请单号
		if (decryptedMap.containsKey("applyCode")) {
			if (!decryptedMap.get("applyCode").toString().isEmpty()) {
				where_definition += " and applyCode=? ";
				argsList.add(decryptedMap.get("applyCode"));
			}
		}

		// 主题
		if (decryptedMap.containsKey("theme")) {
			String theme = decryptedMap.get("theme").toString();
			if (!theme.isEmpty()) {
				where_definition += " and theme like ? ";
				argsList.add("%" + theme + "%");
			}
		}

		// 申请人
		if (decryptedMap.containsKey("FULL_NAME")) {
			String FULL_NAME = decryptedMap.get("FULL_NAME").toString();
			if (!FULL_NAME.isEmpty()) {
				where_definition += " and FULL_NAME like ? ";
				argsList.add("%" + FULL_NAME + "%");
			}
		}

		// 经销商编号
		if (decryptedMap.containsKey("ucode")) {
			if (!decryptedMap.get("ucode").toString().isEmpty()) {
				where_definition += " and ucode=? ";
				argsList.add(decryptedMap.get("ucode"));
			}
		}

		// 业务类型
		if (decryptedMap.containsKey("businessTypeId")) {
			if (!decryptedMap.get("businessTypeId").toString().isEmpty()) {
				where_definition += " and businessTypeId=? ";
				argsList.add(decryptedMap.get("businessTypeId"));
			}
		}

		// 业务细分
		if (decryptedMap.containsKey("businessDetailId")) {
			if (!decryptedMap.get("businessDetailId").toString().isEmpty()) {
				where_definition += " and businessDetailId=? ";
				argsList.add(decryptedMap.get("businessDetailId"));
			}
		}

		// 体系
		if (decryptedMap.containsKey("systemId")) {
			if (!decryptedMap.get("systemId").toString().isEmpty()) {
				where_definition += " and systemId=? ";
				argsList.add(decryptedMap.get("systemId"));
			}
		}

		// 状态
		if (decryptedMap.containsKey("audit_status")) {
			if (!decryptedMap.get("audit_status").toString().isEmpty()) {
				where_definition += " and audit_status=? ";
				argsList.add(decryptedMap.get("audit_status"));
			}
		}

		// 申请时间
		if (decryptedMap.containsKey("begin_CREATE_TIME")) {
			if (!decryptedMap.get("begin_CREATE_TIME").toString().isEmpty()) {
				where_definition += " and CREATE_TIME>=? ";
				argsList.add(decryptedMap.get("begin_CREATE_TIME"));
			}
		}

		if (decryptedMap.containsKey("end_CREATE_TIME")) {
			String end_CREATE_TIME = decryptedMap.get("end_CREATE_TIME").toString();
			if (!end_CREATE_TIME.isEmpty()) {
				try {
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
					Date endPublishDate = sdf.parse(end_CREATE_TIME);

					Calendar calendar = Calendar.getInstance();
					calendar.setTime(endPublishDate);
					calendar.add(Calendar.DATE, 1);

					where_definition += " and CREATE_TIME<? ";
					argsList.add(calendar.getTime());
				} catch (ParseException e) {
					// TODO 自动生成的 catch 块
					e.printStackTrace();
				}
			}
		}

		/**
         * 获得管理者对应的组织机构.
         */
		String orgID = "";
		String departmentId="";
		String percode =decryptedMap.get("percode").toString();
        List<Long> isManager =  orgConnector.getPartyByManageId(percode);
        if(isManager!=null&&isManager.size()>0){
        	for(int i =0;i<isManager.size();i++){
        		orgID = orgID + isManager.get(i).toString() + ",";
        	}
        	orgID = orgID.substring(0, orgID.length()-1);	
        
	        //寻找该 组织机构 下面所有的组织机构
	    	List<String> list = partyResource.getPartyEntityNoPerson(orgID);
	    	List<String> newList = removeDuplicateWithOrder(list);
	    	//遍历结果，将这些组织机构的id放入一个字符串
	    	for(int i =0;i<newList.size();i++){
	    		departmentId= departmentId + newList.get(i)+",";
	    	}
	    	if (StringUtils.isNotBlank(departmentId)) {
	    		departmentId = departmentId.substring(0, departmentId.length()-1);	
	    		departmentId = departmentId + "," + orgID;
	    	} else {
	    		departmentId = orgID;
	    	}
        } else {
        	List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        	return list;  
        }
        
    	if(StringUtils.isBlank(departmentId)) {
    		departmentId = "9999";
    	}
    	
		StringBuffer sqlFrom = new StringBuffer();
		sqlFrom.append("SELECT distinct r.ID AS ID,r.CATEGORY AS CATEGORY,r.STATUS AS STATUS,r.REF AS REF,");
		sqlFrom.append("r.USER_ID AS USER_ID,r.CREATE_TIME AS CREATE_TIME,r.NAME AS NAME,r.FORM_TEMPLATE_CODE AS FORM_TEMPLATE_CODE,");
		sqlFrom.append("r.TENANT_ID AS TENANT_ID,r.BUSINESS_KEY AS BUSINESS_KEY,r.theme,r.applyCode AS applyCode,r.ucode AS ucode,");
		sqlFrom.append("r.businessTypeId AS businessTypeId,r.businessTypeName AS businessTypeName,r.businessDetailId AS businessDetailId,");
		sqlFrom.append("r.businessDetailName AS businessDetailName,r.systemId AS systemId,r.systemName AS systemName,");
		sqlFrom.append("r.areaId AS areaId,r.areaName AS areaName,r.submitTimes AS submitTimes,r.end_time AS end_time,");
		sqlFrom.append("r.companyId AS companyId,r.companyName AS companyName,r.url AS url,r.detailHtml AS detailHtml,");
		sqlFrom.append("r.pk_id AS pk_id,r.audit_status AS audit_status,");
		sqlFrom.append("GET_DICT_NAME_BY_TYPE_NAME ('RecordStatus',r.audit_status) AS audit_status_dict,");
		sqlFrom.append("r.apply_content AS apply_content,p.FULL_NAME AS FULL_NAME,p.COMPANY_CODE AS COMPANY_CODE,");
		sqlFrom.append("p.COMPANY_NAME AS COMPANY_NAME,p.DEPARTMENT_CODE AS DEPARTMENT_CODE,p.DEPARTMENT_NAME AS DEPARTMENT_NAME,");
		sqlFrom.append("p.POSITION_CODE AS POSITION_CODE,p.POSITION_NAME AS POSITION_NAME");
		sqlFrom.append(" FROM kv_record r");
		sqlFrom.append(" JOIN person_info p ON p.ID = r.USER_ID");
		sqlFrom.append(" JOIN task_info t ON r.id = t.BUSINESS_KEY");
		sqlFrom.append(" JOIN task_info_approve_position tap ON t.ID = tap.task_id");
		sqlFrom.append(" jOIN party_entity pe ON pe.id = tap.position_parentId");
		sqlFrom.append(" where  t.CATALOG NOT IN ('copy', 'start') AND t. STATUS = 'complete' AND pe.id IN (").append(departmentId).append(")");
		
		String sql = "SELECT * FROM (" + sqlFrom + ") r where 1=1 " + where_definition
				+ " order by CREATE_TIME DESC LIMIT ?,?";// 按“申请时间倒序”排列

		long pageIndex = Long.valueOf(decryptedMap.get("strPageIndex").toString());
		long row_count = Long.valueOf(decryptedMap.get("strPageSize").toString());
		long offset = row_count * (pageIndex - 1);
		argsList.add(offset);
		argsList.add(row_count);

		// System.out.println(sql);
		// System.out.println(argsList);
		return jdbcTemplate.queryForList(sql, argsList.toArray());
	}

	boolean isPasswordValid(String rawPassword, String encodedPassword) {
		if (customPasswordEncoder != null) {
			return customPasswordEncoder.matches(rawPassword, encodedPassword);
		} else {
			return rawPassword.equals(encodedPassword);
		}
	}

	@Resource
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Resource
	public void setAccountInfoManager(AccountInfoManager accountInfoManager) {
		this.accountInfoManager = accountInfoManager;
	}

	@Resource
	public void setTaskInfoManager(TaskInfoManager taskInfoManager) {
		this.taskInfoManager = taskInfoManager;
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
	public void setHumanTaskConnectorImpl(
			HumanTaskConnectorImpl humanTaskConnectorImpl) {
		this.humanTaskConnectorImpl = humanTaskConnectorImpl;
	}

	@Resource
    public void setOrgConnector(OrgConnector orgConnector) {
        this.orgConnector = orgConnector;
    }
	
	@Resource
	public void setPartyResource(PartyResource partyResource) {
		this.partyResource = partyResource;
	}
}
