/**
 * 
 */
package com.mossle.H5.user;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.google.common.base.Joiner;
import com.mossle.H5.work.WorkTaskUtils;
import com.mossle.user.persistence.domain.PersonInfo;
import com.mossle.user.persistence.manager.PersonInfoManager;
import com.mossle.user.service.UserService;
import com.mossle.util.StringUtil;

/**
 * @author Bing
 *
 */
@Service
public class H5PartyService {

	private JdbcTemplate jdbcTemplate;
	private PersonInfoManager personInfoManager;
	private UserService userService;

	public Map<String, Object> ThePersonnelTreeData(Map<String, Object> decryptedMap) {
		// {strPerCode=2, percode=2, method=ThePersonnelTreeData,
		// sign=3a936656d2d2fd2313197ecb034cd2d9, strIsAll=0, timestamp=}

		// 返回值
		Map<String, Object> returnMap = new HashMap<String, Object>();

		List<Map<String, Object>> listMap = new ArrayList<Map<String, Object>>();
		
		if (decryptedMap.containsKey("except")) {
			listMap = PersonnelTree(1, decryptedMap.get("except"));
		} else {
			listMap = PersonnelTree(1);
		}
		
		returnMap.put("bSuccess", "true");
		returnMap.put("strMsg", "获取成功");
		returnMap.put("PersonnelTree", listMap);
		// System.out.println(returnMap);

		return returnMap;
		/*
		 * {"bSuccess":"true","strMsg":"获取成功","PersonnelTree":[
		 * {"id":"100000","text":"集团总部","rank":"","subcomname":"","ischecked":
		 * "0","state":"closed","children":[
		 * {"id":"100100","text":"董事会","rank":"","subcomname":"","ischecked":"0"
		 * ,"state":"closed","children":[
		 * {"id":"PE1705040101","text":"汪董","rank":"董事长","subcomname":"",
		 * "ischecked":"1"} ]},
		 * {"id":"100300","text":"人事行政部","rank":"","subcomname":"","ischecked":
		 * "0","state":"closed","children":[
		 * {"id":"PE1705080003","text":"人事总监","rank":"总监","subcomname":"",
		 * "ischecked":"1"},
		 * {"id":"PE1706050007","text":"张静","rank":"职员","subcomname":"",
		 * "ischecked":"1"} ]},
		 * {"id":"101200","text":"怀柔客服","rank":"","subcomname":"","ischecked":
		 * "0","state":"closed","children":[
		 * {"id":"PE1704240004","text":"人事总监","rank":"总监","subcomname":"",
		 * "ischecked":"1"},
		 * {"id":"PE1707200041","text":"刘慧霞","rank":"职员","subcomname":"",
		 * "ischecked":"1"} ]}]}]}
		 */
	}

	public Map<String, Object> AddressBooks(Map<String, Object> decryptedMap) {
		// {strPerCode=2, percode=2, method=AddressBooks,
		// sign=f953b4c3460eb4e790b99538244b5e3b, timestamp=}

		String strPerCode = decryptedMap.get("strPerCode").toString();
		PersonInfo personInfo = personInfoManager.get(Long.valueOf(strPerCode));
		String companyCode = personInfo.getCompanyCode();
		String departmentCode = personInfo.getDepartmentCode();

		// System.out.println("companyCode=" + companyCode);
		// System.out.println("departmentCode=" + departmentCode);

		List<Map<String, Object>> addressBooks = new ArrayList<Map<String, Object>>();// 用于返回
		if (existsRole(4, strPerCode)) {// 角色4为通讯录开放的角色，有这个角色能看所有通讯录。
			addressBooks = getAddressBooks(companyCode);
		} else {// 根据对方情况看本部门或本公司的
			addressBooks = getAddressBooks(companyCode, departmentCode);
		}

		// 返回值
		Map<String, Object> returnMap = new HashMap<String, Object>();

		returnMap.put("bSuccess", "true");
		returnMap.put("strMsg", "加载成功");
		returnMap.put("isOpenOtherName", userService.getOpenOtherNameStatus());//开启“别名”功能状态1是0否
		returnMap.put("AddressBooks", addressBooks);

		return returnMap;
		/*
		 * {"bSuccess":"true","strMsg":"加载成功","AddressBooks":[
		 * {"code":"100000","name":"集团总部","rank":"","subcomname":"0","departid":
		 * "0","departname":"","photo":"","state":"closed","children":[
		 * {"code":"PE1705040101","name":"汪董","rank":"董事长","subcomname":"",
		 * "departid":"100100","departname":"董事会","photo":""},
		 * {"code":"PE1708280002","name":"集团财务总监","rank":"总监","subcomname":"",
		 * "departid":"100200","departname":"财务部","photo":""},
		 * {"code":"PE1704240003","name":"产品部经理","rank":"经理","subcomname":"",
		 * "departid":"101500","departname":"","photo":""} ]},
		 * {"code":"300000","name":"东威创星","rank":"","subcomname":"0","departid":
		 * "0","departname":"","photo":"","state":"closed","children":[
		 * {"code":"PE1609090002","name":"朱越","rank":"经理","subcomname":"",
		 * "departid":"300300","departname":"人事行政","photo":""},
		 * {"code":"PE1612210002","name":"李伟","rank":"经理","subcomname":"",
		 * "departid":"300500","departname":"产品部","photo":""},
		 * {"code":"PE1707070001","name":"郭俊俊","rank":"经理","subcomname":"",
		 * "departid":"300500","departname":"产品部","photo":""},
		 * {"code":"PE1609090001","name":"宋杰","rank":"经理","subcomname":"",
		 * "departid":"300500","departname":"产品部","photo":""},
		 * {"code":"PE1705190149","name":"司志鹏","rank":"职员","subcomname":"",
		 * "departid":"300500","departname":"产品部","photo":""} ]}]}
		 */
	}

	/**
	 * 获取普通通讯录。能获取到谁取决于他本人的权限设置。Bing 2018.1.11
	 * 
	 * @param companyCode
	 * @param departmentCode
	 * @return
	 */
	List<Map<String, Object>> getAddressBooks(String companyCode, String departmentCode) {
		List<Map<String, Object>> addressBooks = new ArrayList<Map<String, Object>>();// 用于返回

		List<Map<String, Object>> listMap = getParties(2, companyCode);// 2公司
		for (Map<String, Object> map : listMap) {
			// System.out.println("code=" + map.get("code").toString());
			List<Map<String, Object>> children = getPersonsByCompany(map.get("code"), companyCode, departmentCode);
			if (children.isEmpty()) {// 如果该子公司下没人可以显示，那么连该公司也不返回。
				continue;
			}

			map.put("subcomname", 0);
			map.put("departid", 0);
			map.put("departname", "");
			map.put("photo", "");
			map.put("state", "closed");
			map.put("children", children);

			addressBooks.add(map);
		}
		return addressBooks;
	}

	/**
	 * 获取所有通讯录。Bing 2018.1.11
	 * 
	 * @param companyCode
	 * @return
	 */
	List<Map<String, Object>> getAddressBooks(String companyCode) {
		List<Map<String, Object>> addressBooks = new ArrayList<Map<String, Object>>();// 用于返回

		List<Map<String, Object>> listMap = getParties(2, companyCode);// 2公司
		for (Map<String, Object> map : listMap) {
			// System.out.println("code=" + map.get("code").toString());
			List<Map<String, Object>> children = getPersonsByCompany(map.get("code"));
			if (children.isEmpty()) {// 如果该子公司下没人可以显示，那么连该公司也不返回。
				continue;
			}

			map.put("subcomname", 0);
			map.put("departid", 0);
			map.put("departname", "");
			map.put("photo", "");
			map.put("state", "closed");
			map.put("children", children);

			addressBooks.add(map);
		}
		return addressBooks;
	}

	public Map<String, Object> AddressBookDetail(Map<String, Object> decryptedMap) {
		// {strPerCode=796946208931840, percode=2, method=AddressBookDetail,
		// sign=ca6798d90efa15bd5a2c17bc9b434229, timestamp=}

		// 返回值
		Map<String, Object> returnMap = new HashMap<String, Object>();

		returnMap.put("bSuccess", "true");
		returnMap.put("strMsg", "加载成功");
		returnMap.put("isOpenOtherName", userService.getOpenOtherNameStatus());//开启“别名”功能状态1是0否
		returnMap.put("LinkInfo", getPersonsById(decryptedMap.get("strPerCode")));

		return returnMap;
	}

	// 通讯录私有方法===========================================================
	/**
	 * PersonnelTree 要求的格式 Bing 2017.9.11
	 * 
	 * @param parent_id
	 * @return
	 */
	List<Map<String, Object>> PersonnelTree(long parent_id) {
		List<Map<String, Object>> listMap = getSubPartiesByTpye(1, parent_id, 2);// 按“行政组织”关系，第1级只找“公司”。

		return putChildren(listMap);
	}

	List<Map<String, Object>> PersonnelTree(long parent_id, Object except) {
		List<Map<String, Object>> listMap = getSubPartiesByTpye(1, parent_id, 2);// 按“行政组织”关系，第1级只找“公司”。

		return putChildren(listMap, except);
	}
	/**
	 * add 2018/8/28 ckx
	 * @param parent_id
	 * @param except
	 * @return
	 */
	List<Map<String, Object>> StationTree(long parent_id, Object except) {
		List<Map<String, Object>> listMap = getSubPartiesByTpye(1, parent_id, 2);// 按“行政组织”关系，第1级只找“公司”。

		return putChildrenStation(listMap, except);
	}
	
	List<Map<String, Object>> StationTree(long parent_id) {
		List<Map<String, Object>> listMap = getSubPartiesByTpye(1, parent_id, 2);// 按“行政组织”关系，第1级只找“公司”。

		return putChildrenStation(listMap);
	}
	/**
	 * 递归获取子部门，并且放到 children 中。Bing 2017.10.11
	 * 
	 * @param listMap
	 * @return
	 */
	List<Map<String, Object>> putChildren(List<Map<String, Object>> listMap) {
		List<Map<String, Object>> removeList = new ArrayList<Map<String, Object>>();// 记录待删除节点

		for (Map<String, Object> map : listMap) {
			List<Map<String, Object>> subs = getSubParties(1, map.get("CHILD_ENTITY_ID"));// 1行政组织
			if (subs.size() > 0) {
				map.put("children", putChildren(subs));// 递归加子节点
			} else {
				if (!map.get("TYPE_ID").toString().equals("1")) {
					removeList.add(map);// 不是人员，也没有子节点，加入待删除。
					
				}
			}
		}

		listMap.removeAll(removeList);// 批量删除

		return listMap;
	}

	List<Map<String, Object>> putChildren(List<Map<String, Object>> listMap, Object except) {
		List<Map<String, Object>> removeList = new ArrayList<Map<String, Object>>();// 记录待删除节点

		for (Map<String, Object> map : listMap) {
			
			List<Map<String, Object>> subs = getSubParties(1, map.get("CHILD_ENTITY_ID"), except);// 1行政组织
			
			
			if (subs.size() > 0) {
				map.put("children", putChildren(subs, except));// 递归加子节点
			} else {
				if (!map.get("TYPE_ID").toString().equals("1")) {
					removeList.add(map);// 不是人员，也没有子节点，加入待删除。
			}
			}
		}

		listMap.removeAll(removeList);// 批量删除

		return listMap;
	}
	// ckx
	List<Map<String, Object>> putChildrenStation(List<Map<String, Object>> listMap, Object except) {
		List<Map<String, Object>> removeList = new ArrayList<Map<String, Object>>();// 记录待删除节点

		for (Map<String, Object> map : listMap) {
			List<Map<String, Object>> subs = null;
			//String type = "";
			if(map.get("TYPE_ID").toString().equals("5")){
				subs = getSubPartiesStation(4, map.get("CHILD_ENTITY_ID"), except);// 4岗位关系
			}else{
				subs = getSubPartiesStation(1, map.get("CHILD_ENTITY_ID"), except);// 1行政组织
			}
			if (null != subs && subs.size() > 0) {
				map.put("children", putChildrenStation(subs, except));// 递归加子节点
			} else {
				if (!"5".equals(map.get("TYPE_ID") == null ? "" : map.get("TYPE_ID").toString())) {
					boolean del = isDel(map);
					if(del){
						removeList.add(map);// 不是人员，也没有子节点，加入待删除。
					}
				}
			}
		}

		listMap.removeAll(removeList);// 批量删除
		return listMap;
	}
	private boolean isDel(Map<String, Object> map) {
		String sql = "SELECT * FROM v_h5_party_struct_station where ID = ? and IS_DISPLAY=1 and DEL_FLAG=0";
		Map<String, Object> queryForMap = jdbcTemplate.queryForMap(sql, new Object[] { map.get("PARENT_ENTITY_ID")});
		Object object = queryForMap.get("TYPE_ID");
		if(!"5".equals(StringUtil.toString(object))){
			return true;
		}
		return false;
	}

	/**
	 * 递归获取子部门，并且放到 children 中。ckx
	 * 
	 * @param listMap
	 * @return
	 */
	List<Map<String, Object>> putChildrenStation(List<Map<String, Object>> listMap) {
		List<Map<String, Object>> removeList = new ArrayList<Map<String, Object>>();// 记录待删除节点

		for (Map<String, Object> map : listMap) {
			List<Map<String, Object>> subs = null;
			if(map.get("TYPE_ID").toString().equals("5")){
				subs = getSubPartiesStation(4, map.get("CHILD_ENTITY_ID"));// 4岗位关系
			}else{
				//subs = getSubPartiesStation(1, map.get("CHILD_ENTITY_ID"));// 1行政组织
			}
			if (null != subs && subs.size() > 0) {
				map.put("children", putChildrenStation(subs));// 递归加子节点
			} else {
				if (!"5".equals(map.get("TYPE_ID") == null ? "" : map.get("TYPE_ID").toString()) && 
						!"1".equals(map.get("TYPE_ID") == null ? "" : equals(map.get("TYPE_ID").toString()))) {
						removeList.add(map);// 不是人员，也没有子节点，加入待删除。
				}
			
			}
		}

		listMap.removeAll(removeList);// 批量删除

		return listMap;
	}
	/**
	 * 获取子（部门、人员）数据 Bing 2017.9.11
	 * 
	 * @param struct_type_id
	 *            1行政组织；2管理关系；3上下级；4岗位关系；5岗位人员；6部门岗位；7角色；8群组
	 * @param parent_id
	 * @return
	 */
	List<Map<String, Object>> getSubParties(Object struct_type_id, Object parent_id) {
		String sql = "SELECT * FROM v_h5_party_struct where STRUCT_TYPE_ID=? and PARENT_ENTITY_ID=?";
		return jdbcTemplate.queryForList(sql, new Object[] { struct_type_id, parent_id });
	}

	/**
	 * Bing 2017.10.5
	 * 
	 * @param struct_type_id
	 *            1行政组织；2管理关系；3上下级；4岗位关系；5岗位人员；6部门岗位；7角色；8群组
	 * @param parent_id
	 *            1 人员 2 公司 3 部门 4 小组 5 岗位
	 * @param type_id
	 * @return
	 */
	List<Map<String, Object>> getSubPartiesByTpye(Object struct_type_id, Object parent_id, long entity_type_id) {
		String sql = "SELECT * FROM v_h5_party_struct where "
				+ "STRUCT_TYPE_ID=? and PARENT_ENTITY_ID=? and TYPE_ID=? and IS_DISPLAY=1 and DEL_FLAG=0";
		return jdbcTemplate.queryForList(sql, new Object[] { struct_type_id, parent_id, entity_type_id});
	}

	/**
	 * Bing 2017.11.1
	 * 
	 * @param struct_type_id
	 *            1行政组织；2管理关系；3上下级；4岗位关系；5岗位人员；6部门岗位；7角色；8群组
	 * @param parent_id
	 *            1 人员 2 公司 3 部门 4 小组 5 岗位
	 * @param except
	 *            把…除外
	 * @return
	 */
	List<Map<String, Object>> getSubParties(long struct_type_id, Object parent_id, Object except) {
		String sql = "SELECT * FROM v_h5_party_struct where STRUCT_TYPE_ID=? and PARENT_ENTITY_ID=? and CHILD_ENTITY_ID<>? and IS_DISPLAY=1 and DEL_FLAG=0";
		return jdbcTemplate.queryForList(sql, new Object[] { struct_type_id, parent_id, except });
	}


	
	List<Map<String, Object>> getSubParties(long struct_type_id, long parent_id, long entity_type_id, long except) {
		String sql = "SELECT * FROM v_h5_party_struct where STRUCT_TYPE_ID=? and PARENT_ENTITY_ID=? and TYPE_ID=? and CHILD_ENTITY_ID<>? and IS_DISPLAY=1 and DEL_FLAG=0";
		return jdbcTemplate.queryForList(sql, new Object[] { struct_type_id, parent_id, entity_type_id, except });
	}
	
	/**
	 * ckx 
	 * 
	 * @param struct_type_id
	 *            1行政组织；2管理关系；3上下级；4岗位关系；5岗位人员；6部门岗位；7角色；8群组
	 * @param parent_id
	 *            1 人员 2 公司 3 部门 4 小组 5 岗位
	 * @param except
	 *            把…除外
	 * @return
	 */
	List<Map<String, Object>> getSubPartiesStation(long struct_type_id, Object parent_id, Object except) {
		String sql = "SELECT * FROM v_h5_party_struct_station where STRUCT_TYPE_ID=? and PARENT_ENTITY_ID=? and CHILD_ENTITY_ID<>? and IS_DISPLAY=1 and DEL_FLAG=0";
		/*String sql1 = "select CHILD_ENTITY_ID as id, PARENT_ENTITY_ID ,CHILD_ENTITY_ID,STRUCT_TYPE_ID,TYPE_ID ,`NAME` as text ,IS_DISPLAY,DEL_FLAG,`LEVEL`,(case TYPE_ID when 1 then 1 else 0 end) AS `ischecked` "
				+ "from PARTY_STRUCT ps LEFT JOIN party_entity pe on ps.CHILD_ENTITY_ID = pe.ID "
				+ "where PARENT_ENTITY_ID=? and pe.DEL_FLAG = 0 and pe.IS_DISPLAY <> '0' ";*/
		
		
		return jdbcTemplate.queryForList(sql, new Object[] { struct_type_id, parent_id, except });
		//return jdbcTemplate.queryForList(sql1, new Object[] {parent_id});
	}
	
	/**
	 * 获取子（部门、人员）数据 ckx
	 * 
	 * @param struct_type_id
	 *            1行政组织；2管理关系；3上下级；4岗位关系；5岗位人员；6部门岗位；7角色；8群组
	 * @param parent_id
	 * @return
	 */
	List<Map<String, Object>> getSubPartiesStation(Object struct_type_id, Object parent_id) {
		String sql = "SELECT * FROM v_h5_party_struct_station where STRUCT_TYPE_ID=? and PARENT_ENTITY_ID=?";
		return jdbcTemplate.queryForList(sql, new Object[] { struct_type_id, parent_id });
	}

	/**
	 * 根据类型获取组织 Bing 2017.9.26
	 * 
	 * @param type_id
	 *            类型： '1人员' '2公司' '3部门' '4小组' '5岗位' '6大区'
	 * @param companyCode
	 *            访客所在子公司，该子公司将排在最前面
	 * @return
	 */
	List<Map<String, Object>> getParties(Object type_id, Object companyCode) {
		String sql = "SELECT * FROM v_h5_party_entity where TYPE_ID=?  order by case when id=? then 0 else 1 end,id";
		return jdbcTemplate.queryForList(sql, new Object[] { type_id, companyCode });
	}

	/**
	 * 根据子公司获取职员 Bing 2017.9.27
	 * 
	 * @param companyCode
	 *            职员所在子公司
	 * @param secretCompany
	 *            登陆人所在子公司，对于设置为内部的职员，只能同公司的人能看到
	 * @param secretDepartment
	 *            登陆人所在部门，对于设置为保密的职员，只能同部门的人能看到
	 * @return
	 */
	List<Map<String, Object>> getPersonsByCompany(Object companyCode, Object secretCompany, Object secretDepartment) {
		String where_definition = " COMPANY_CODE=? and (secret=0 or (secret=1 and COMPANY_CODE=?) or (secret=2 and DEPARTMENT_CODE=?)) ";
		
		
		
		
		//20181112 chengze 替换掉视图  v_h5_person_info
//		String sql = "SELECT * FROM v_h5_person_info where " + where_definition
//				+ " order by IFNULL(PARENT_PRIORITY,99),DEPARTMENT_CODE,POSITION_CODE DESC,PRIORITY";
		
		String sql = "SELECT * FROM ("+WorkTaskUtils.getH5PersonInfo()+") t  where " + where_definition
		+ " order by IFNULL(PARENT_PRIORITY,99),DEPARTMENT_CODE,POSITION_CODE DESC,PRIORITY";

		
		Object[] args = new Object[] { companyCode, secretCompany, secretDepartment };
		// System.out.println(sql);
		return jdbcTemplate.queryForList(sql, args);
	}

	/**
	 * 根据子公司获取全部职员 Bing 2018.1.11
	 * 
	 * @param companyCode
	 * @return
	 */
	List<Map<String, Object>> getPersonsByCompany(Object companyCode) {
		
		//20181112 chengze 替换掉视图  v_h5_person_info
		
//		String sql = "SELECT * FROM v_h5_person_info " + "where COMPANY_CODE=? "
//				+ "order by IFNULL(PARENT_PRIORITY,99),DEPARTMENT_CODE,POSITION_CODE DESC,PRIORITY";
		
		String sql = "SELECT * FROM  ("+WorkTaskUtils.getH5PersonInfo()+") t  where COMPANY_CODE=? "
				+ "order by IFNULL(PARENT_PRIORITY,99),DEPARTMENT_CODE,POSITION_CODE DESC,PRIORITY";
		
		
		Object[] args = new Object[] { companyCode };
		// System.out.println(sql);
		return jdbcTemplate.queryForList(sql, args);
	}

	List<Map<String, Object>> getPersonsById(Object id) {
		
		//20181112 chengze 替换掉视图  v_h5_person_info
		//String sql = "SELECT * FROM v_h5_person_info where id=?";
		
		String sql = "SELECT * FROM   ("+WorkTaskUtils.getH5PersonInfo()+") t   where id=?";
		
		List<Map<String,Object>> mapList=jdbcTemplate.queryForList(sql, new Object[] { id });
		List<Map<String,Object>> mapNewList=new ArrayList<Map<String,Object>>();
		for(Map<String,Object> map:mapList){
			PersonInfo personInfo=personInfoManager.findUniqueBy("id", Long.parseLong(id.toString()));
			map.put("realName", personInfo.getRealName());
			mapNewList.add(map);
		}
		return mapNewList;
	}

	/**
	 * 某人是否存在某角色 Bing 2018.1.11
	 * 
	 * @param ROLE_ID
	 * @param account_id
	 * @return
	 */
	boolean existsRole(Object ROLE_ID, Object account_id) {
		String sql = "SELECT count(*) " + "FROM auth_role r " + "join auth_user_role ur on ur.ROLE_ID=r.ID "
				+ "join auth_user_status u on ur.USER_STATUS_ID=u.ID " + "WHERE r.ID=? AND u.ref=?";
		int count = jdbcTemplate.queryForObject(sql, new Object[] { ROLE_ID, account_id }, Integer.class);
		if (count > 0) {
			return true;
		} else {
			return false;
		}
	}

	// @Resource==========================================================
	@Resource
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Resource
	public void setPersonInfoManager(PersonInfoManager personInfoManager) {
		this.personInfoManager = personInfoManager;
	}
	
	@Resource
	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	/**
	 * 获取岗位树
	 * @param decryptedMap
	 * @return
	 */
	public Map<String, Object> StationTreeData(Map<String, Object> decryptedMap) {
		// 返回值
		Map<String, Object> returnMap = new HashMap<String, Object>();
		List<Map<String, Object>> listMap = new ArrayList<Map<String, Object>>();
		if (decryptedMap.containsKey("except")) {
			listMap = StationTree(1, decryptedMap.get("except"));
		} else {
			listMap = StationTree(1);
		}
		returnMap.put("bSuccess", "true");
		returnMap.put("strMsg", "获取成功");
		returnMap.put("PersonnelTree", listMap);
		return returnMap;
	}

	public Map<String, Object> StationTreeDataNew(
			Map<String, Object> decryptedMap) {
		// 返回值
		Map<String, Object> returnMap = new HashMap<String, Object>();
		List<Map<String, Object>> listMap = new ArrayList<Map<String, Object>>();
		if (decryptedMap.containsKey("except")) {
			listMap = StationTreePost(1, decryptedMap.get("except"));
		} else {
			//listMap = StationTree(1);
		}
		returnMap.put("bSuccess", "true");
		returnMap.put("strMsg", "获取成功");
		returnMap.put("PersonnelTree", listMap);
		return returnMap;
	}
	/**
	 * 查询第一层
	 * @param i
	 * @param object
	 * @return
	 * ckx
	 */
	private List<Map<String, Object>> StationTreePost(int i, Object object) {
		String sql = "select e.id,s.CHILD_ENTITY_ID ,s.PARENT_ENTITY_ID,s.STRUCT_TYPE_ID,e.TYPE_ID,e.`NAME` as text ,e.IS_DISPLAY,e.DEL_FLAG,0 as ischecked,'' as rank,'' as subcomname,'' as job  from party_entity e LEFT JOIN party_struct s on e.ID = s.CHILD_ENTITY_ID where s.STRUCT_TYPE_ID = 1 and s.PARENT_ENTITY_ID = 1 and e.DEL_FLAG = 0 and e.IS_DISPLAY = 1 and e.TYPE_ID = 2;";
		List<Map<String,Object>> queryForList = jdbcTemplate.queryForList(sql);
		
		//查找所有没有岗位的人员
		String strSql="SELECT p.id FROM party_entity p"
					+" LEFT JOIN party_struct s ON p.ID=s.CHILD_ENTITY_ID AND s.STRUCT_TYPE_ID=4"
					+" INNER JOIN party_struct sc ON p.id=sc.CHILD_ENTITY_ID AND sc.STRUCT_TYPE_ID=1"
					+" INNER JOIN party_entity pc ON sc.PARENT_ENTITY_ID=pc.ID AND pc.TYPE_ID<>2  AND ifnull(p.`NAME`,'')<>'测试账户'"
					+" where p.TYPE_ID=1 AND p.del_flag='0' and ifnull(p.IS_DISPLAY,'1')='1' AND s.ID is NULL";
		List<String> personNoPositionList=jdbcTemplate.queryForList(strSql,String.class);
		String strPersonNoPositionIds="";
		if(null!=personNoPositionList&&personNoPositionList.size()>0)
			strPersonNoPositionIds=Joiner.on(",").join(personNoPositionList);
		//递归查询下面每一层
		queryForList = getChild(queryForList,object,strPersonNoPositionIds);
		return queryForList;
	}
	/**
	 * 递归查询子节点
	 * @param queryForList
	 * @param object
	 * @return
	 * ckx
	 */
	private List<Map<String, Object>> getChild(
			List<Map<String, Object>> queryForList, Object object,String strPersonNoPositionIds) {
		System.out.print("getChild："+new Date());
		for (Map<String, Object> map : queryForList) {
			String typeId = StringUtil.toString(map.get("TYPE_ID"));
			String id = StringUtil.toString(map.get("ID"));
			List<Map<String, Object>> childList = null;
			if("5".equals(typeId)){
				map.put("ischecked", "1");
				//岗位下面为人员，查询人员
				String sql = "select e.id,s.CHILD_ENTITY_ID,"
						+ "s.PARENT_ENTITY_ID,s.STRUCT_TYPE_ID,e.TYPE_ID,e.`NAME` as text,"
						+ "e.IS_DISPLAY,e.DEL_FLAG,0 as ischecked,'' as rank,'' as subcomname,'' as job"
						+ " from party_entity e join party_struct s on e.id=s.CHILD_ENTITY_ID"
						+ " where e.DEL_FLAG = '0' and  s.PARENT_ENTITY_ID= ? ";
				childList = jdbcTemplate.queryForList(sql, new Object[] {id});
				if(null != childList){
					for (Map<String, Object> map2 : childList) {
						map2.put("ischecked", "1");
					}
					map.put("children", childList);// 添加子节点
				}
				System.out.print("5_id："+id+"  "+new Date());
			}else{
				//查询下一级
				String strBaseSql = "select e.id,s.CHILD_ENTITY_ID"
						+ ",s.PARENT_ENTITY_ID,s.STRUCT_TYPE_ID,e.TYPE_ID,e.`NAME` as text,"
						+ "e.IS_DISPLAY,e.DEL_FLAG,%s as ischecked,'' as rank,'' as subcomname,'' as job "
						+ " from party_entity e join party_struct s on e.id=s.CHILD_ENTITY_ID"
						+ " where e.DEL_FLAG = '0'";
				
				List<Map<String, Object>> childAllPersonList=new ArrayList<Map<String,Object>>();
				Object[] objs=new Object[] {id};
				if(!"2".equals(typeId)){
					if(!strPersonNoPositionIds.equals("")){
						String strPersonSql=String.format(strBaseSql, 1)+String.format("  and ifnull(e.IS_DISPLAY,'1')= '1' and s.PARENT_ENTITY_ID= ? and e.id in(%s)",strPersonNoPositionIds);
						List<Map<String, Object>> childPersonList=jdbcTemplate.queryForList(strPersonSql,objs);
						if(null!=childPersonList&&childPersonList.size()>0){
							childAllPersonList.addAll(childPersonList);
						}
					}
				}
				
				System.out.print("no_5_id："+id+"  "+new Date());
				
				String sql=String.format(strBaseSql, 0)+"  and e.IS_DISPLAY = '1' and s.PARENT_ENTITY_ID= ?  and TYPE_ID <> 1 ";
				childList = jdbcTemplate.queryForList(sql,objs);
				if(null != childList){
					List<Map<String, Object>> childPositionPersonList=getChild(childList, object,strPersonNoPositionIds);
					if(null!=childPositionPersonList&&childPositionPersonList.size()>0){
						childAllPersonList.addAll(childPositionPersonList);
					}
				}
				map.put("children", childAllPersonList);// 递归加子节点
			}
		}
		return queryForList;
	}

}
