/**
 * 
 */
package com.mossle.H5.cms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.mossle.api.notification.NotificationConnector;
import com.mossle.api.tenant.TenantHolder;
import com.mossle.api.user.UserDTO;
import com.mossle.humantask.support.HumanTaskConnectorImpl;
import com.mossle.msg.MsgConstants;
import com.mossle.msg.persistence.domain.MsgInfo;
import com.mossle.msg.persistence.manager.MsgInfoManager;
import com.mossle.project.persistence.domain.WorkProjectInfo;

/**
 * @author Bing
 *
 */
@Service
public class MsgService {

	private JdbcTemplate jdbcTemplate;
	private MsgInfoManager msgInfoManager;
	private NotificationConnector notificationConnector;// 发送消息
	private TenantHolder tenantHolder;
	private HumanTaskConnectorImpl humanTaskConnectorImpl;

	public Map<String, Object> NumRemind(Map<String, Object> decryptedMap) {
		// {strPerCode=, percode=, method=NumRemind,
		// sign=aa8d2dfd1d2782dd825f396b40e31409, timestamp=}

		Map<String, Object> returnMap = new HashMap<String, Object>();

		try {
			// 验证参数================================================================================
			// if (!decryptedMap.containsKey("percode") ||
			// !decryptedMap.containsKey("strPageIndex")
			// || !decryptedMap.containsKey("strPageSize")) {
			// returnMap.put("bSuccess", "false");
			// returnMap.put("strMsg", "参数错误");
			// return returnMap;
			// }

			// 获取数据================================================
			Object percode = decryptedMap.get("percode");
			String sql = "";

			sql = "SELECT count(*) FROM work_report_info where datastatus=1 and status=0 and sendee=?";
			int receiveReport = jdbcTemplate.queryForObject(sql, new Object[] { percode }, Integer.class);

			sql = "SELECT count(*) FROM v_h5_work_report_cc where datastatus=1 and status=0 and ccno=?";
			int ccReport = jdbcTemplate.queryForObject(sql, new Object[] { percode }, Integer.class);

			sql = "SELECT count(*) FROM v_h5_work_report_forward where datastatus=1 and status=0 and sendee=?";
			int forwardReport = jdbcTemplate.queryForObject(sql, new Object[] { percode }, Integer.class);

			sql = "SELECT count(*) FROM v_msg_info where status=0 and RECEIVER_ID=?";
			int msg = jdbcTemplate.queryForObject(sql, new Object[] { percode }, Integer.class);

			sql = " SELECT count(*) FROM `task_info` ti " + " join kv_record kvr on ti.BUSINESS_KEY=kvr.BUSINESS_KEY "
					+ " JOIN person_info pi on kvr.USER_ID=pi.ID "
					+ " WHERE ti.STATUS='active' and CATALOG <>'copy' and ASSIGNEE=? "
					+ " and IFNULL(kvr.businessTypeId,'') not in(SELECT di.`VALUE` FROM `dict_type` dy join dict_info di on di.TYPE_ID=dy.ID WHERE dy.`NAME`='filterFlowToDeal') ";
			int task = jdbcTemplate.queryForObject(sql, new Object[] { percode }, Integer.class);

			List<String> partyIds = new ArrayList<String>();
	        String strIds = "";
	        partyIds.addAll(humanTaskConnectorImpl.findGroupIds(percode.toString()));
	        partyIds.addAll(humanTaskConnectorImpl.findUserIds(percode.toString()));
	        for (String str : partyIds) {
	            strIds += "'" + str + "',";
	        }
	        strIds = strIds.substring(0, strIds.length() - 1);
			// sql = "SELECT count(*) FROM v_h5_task_participant WHERE CHILD_ENTITY_ID in ("+strIds+")";
			
			sql = "select count(*) from (select DISTINCT i.id,i.process_instance_id,i.action,r.applycode,i.presentation_subject,"
	                + "r.theme,r.user_id,p.full_name,r.ucode,r.businesstypeid,r.businesstypename,r.businessdetailid,"
	                + "r.businessdetailname,r.systemid,r.systemname,r.areaid,r.areaname,r.companyid,r.companyname,"
	                + "r.create_time,i.catalog,t_p.REF,r.url" + " from task_info i"
	                + " inner join task_participant t_p on i.id = t_p.task_id"
	                + " inner join kv_record r on i.business_key = r.id"
	                + " inner join person_info p on r.user_id = p.id"
	                + " where ifnull(i.ASSIGNEE,'')='' and i.`status`='active') t where REF in (" + strIds + ")";
			
			int claim = jdbcTemplate.queryForObject(sql, Integer.class);

			// 返回======================================================================
			returnMap.put("bSuccess", "true");
			returnMap.put("strMsg", "加载成功");
			returnMap.put("Msg", msg);// 消息总
			returnMap.put("AboutMe", 0);// 与我相关数量
			returnMap.put("Remind", 0);// 提醒数量
			returnMap.put("Issue", 0);// 议题数量

			// @Report=@ReceiveReport+@CCReport+@ForwardReport
			returnMap.put("ReceiveReport", receiveReport);// 接收汇报未读数
			returnMap.put("CCReport", ccReport);// 抄送汇报未读数
			returnMap.put("ForwardReport", forwardReport);// 转发汇报未读数
			returnMap.put("Report", receiveReport + ccReport + forwardReport);// 汇报未读数

			returnMap.put("WaitCheck", task);// 审批未审数
			returnMap.put("WaitClaim", claim);// 审批未领数 Bing 2017.12.7
			returnMap.put("Bpm", task + claim);// 审批流程=待领+待审 Bing 2017.12.7

			// if (list.size() > 0) {
			// returnMap.put("ReportList", list);
			// } else {
			// returnMap.put("strMsg", "暂无数据");
			// }
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("bSuccess", "false");
			returnMap.put("strMsg", "加载错误，请联系管理员");
		}

		return returnMap;

		/*
		 * 
		 * //
		 * {"bSuccess":"true","strMsg":"加载成功","Msg":23,"AboutMe":23,"Remind":0,
		 * "Issue":2,"Report":0,"ReceiveReport":0,"CCReport":0,"ForwardReport":0
		 * ,"WaitCheck":0}
		 */
	}

	public Map<String, Object> MsgList(Map<String, Object> decryptedMap) {
		// {strPageSize=5, strPerCode=2, percode=2, method=MsgList,
		// sign=e458da11bb70689df863747cb2020546, strPageIndex=1,
		// timestamp=}

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
			long pageIndex = Long.valueOf(decryptedMap.get("strPageIndex").toString());
			long row_count = Long.valueOf(decryptedMap.get("strPageSize").toString());
			long offset = row_count * (pageIndex - 1);
			Object percode = decryptedMap.get("percode");

			Object status = 0;
			if (decryptedMap.containsKey("status"))
				status = decryptedMap.get("status");

			// 查数据
			String sql = "SELECT *,ID AS IF01,CREATE_TIME AS IF03,NAME AS IF04,STATUS AS IF05 " + "FROM v_msg_info "
					+ "where STATUS=? and RECEIVER_ID=? " + "ORDER BY ID DESC " + "LIMIT ?,?";
			List<Map<String, Object>> list = jdbcTemplate.queryForList(sql,
					new Object[] { status, percode, offset, row_count });

			// 返回======================================================================
			returnMap.put("bSuccess", "true");

			if (list.size() > 0) {
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

		/*
		 * {"bSuccess":"true","strMsg":"加载成功","DataList":[
		 * {"IF01":298,"IF02":null,"IF03":"\/Date(1495594863000)\/","IF04":
		 * "您发布的项目[晚会]，信息发生了变更，请查看。","IF05":"1","IF06":null,"IF07":null,
		 * "IF08":null,"IF08C":"李伟","IF09":0},
		 * {"IF01":252,"IF02":null,"IF03":"\/Date(1489395647000)\/","IF04":
		 * "项目[会刺激到家]已建立，请您关注项目进展情况。","IF05":"1","IF06":null,"IF07":null,
		 * "IF08":null,"IF08C":"王聪","IF09":0} ]}
		 */
	}

	public Map<String, Object> MsgDetails(Map<String, Object> decryptedMap) {
		// strLNewID=1114

		Map<String, Object> returnMap = new HashMap<String, Object>();

		try {
			// 验证参数================================================================================
			if (!decryptedMap.containsKey("strLNewID")) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "参数错误");
				return returnMap;
			}

			if (!decryptedMap.containsKey("strPerCode")) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "参数错误");
				return returnMap;
			}

			// 获取参数================================================
			String strLNewID = decryptedMap.get("strLNewID").toString();
			String strPerCode = decryptedMap.get("strPerCode").toString();

			// 验证登录人是否是消息接收人 Bing 2018.2.9===========================
			MsgInfo msgInfo = msgInfoManager.get(Long.parseLong(strLNewID));
			if (msgInfo != null) {
				if (!msgInfo.getReceiverId().equals(strPerCode)) {
					returnMap.put("bSuccess", "false");
					returnMap.put("strMsg", "无权查看该消息");
					return returnMap;
				}
	
				// 改状态==========================================
				msgInfo.setStatus(1);
				msgInfoManager.save(msgInfo);
			}
			// 查数据========================================================= 视图取出使用（提高查询性能）18.09.18 sjx
			String sql = "SELECT * ,f_datastatus ( msg_type, DATA, RECEIVER_ID ) AS datastatus ,f_bpm_task_active ( DATA ) AS bpm_task_active"
					+ " FROM "
					+ "(select `m`.`ID` AS `ID`,`m`.`ID` AS `IF01`,`m`.`RECEIVER_ID` AS `RECEIVER_ID`,`m`.`RECEIVER_ID` AS `IF02`,`m`.`CREATE_TIME` AS `IF03`,`m`.`NAME` AS `IF04`,`m`.`STATUS` AS `STATUS`,`m`.`STATUS` AS `IF05`,`m`.`TYPE` AS `IF06`,`m`.`SENDER_ID` AS `IF08`,`GET_DISPLAY_NAME_BY_ID`(`m`.`SENDER_ID`) AS `IF08C`,`m`.`msg_type` AS `msg_type`,`m`.`msg_type` AS `IF09`,`m`.`CONTENT` AS `CONTENT`,`m`.`DATA` AS `DATA`,(case `m`.`msg_type` when 0 then (select `kvr`.`applyCode` from (`task_info` `ti` join `kv_record` `kvr` on((`ti`.`BUSINESS_KEY` = `kvr`.`BUSINESS_KEY`))) where (`ti`.`ID` = `m`.`DATA`)) else `m`.`DATA` end) AS `IF07` from `v_msg_info` `m` order by `m`.`ID` desc) as view "
					+ "where id=?";
			List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, new Object[] { strLNewID });

			// 返回==========================================================
			returnMap.put("bSuccess", "true");

			if (list.size() > 0) {
				returnMap.put("strMsg", "加载成功");
				returnMap.put("MsgDetail", list);
			} else {
				returnMap.put("strMsg", "暂无数据");
			}
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("bSuccess", "false");
			returnMap.put("strMsg", "加载错误，请联系管理员");
		}

		return returnMap;

		/*
		 * {"bSuccess":"true","strMsg":"加载成功","MsgDetail":[
		 * {"IF01":1114,"IF02":null,"IF03":"\/Date(-62135596800000)\/","IF04":
		 * "您有新汇报[亟待解决]！","IF05":null,"IF06":null,"IF07":"124","IF08":null,
		 * "IF08C":null,"IF09":0} ]}
		 */
	}

	/**
	 * 发消息，用于编辑项目。Bing 2017.11.7
	 * 
	 * @param workProjectInfo
	 *            新项目
	 * @param old_leader
	 *            原负责人
	 * @param operator
	 *            操作人
	 */
	public void editProject(WorkProjectInfo workProjectInfo, Long old_leader, UserDTO operator) {
		if (workProjectInfo == null)
			return;

		if (!workProjectInfo.getDatastatus().equals("1"))
			return;

		// 发消息=========================================
		String bussinessId = workProjectInfo.getId().toString();
		String tenantId = tenantHolder.getTenantId();
		String sender = operator.getId();
		String receiver = workProjectInfo.getLeader().toString();
		String title = "[" + workProjectInfo.getTitle() + "]" + "项目变更通知";
		String content = "";

		// 没改负责人发：编辑了您负责的。。。-------------------------------------------------------------------
		if (workProjectInfo.getLeader().equals(old_leader)) {
			// 给负责人发送消息
			// e) 标题：[项目名称]项目变更通知
			// f) 内容：发布人[发布人姓名]编辑了您负责的[项目名称]项目，请查看。
			content = "[" + operator.getDisplayName() + "]编辑了您负责的" + "[" + workProjectInfo.getTitle() + "]" + "项目,请查看。";
			notificationConnector.send(bussinessId, tenantId, sender, receiver, title, content,
					MsgConstants.MSG_TYPE_PROJECT);
		} else {// 修改了负责人，给老负责人发“变更了负责人”；给新负责人发“发布提醒”。-----------------------------------
				// 项目负责人发生变化时，需给原负责人发送消息
				// a) 标题：[项目名称]项目变更通知
				// b) 内容：您负责的[项目名称]项目，发布人[发布人姓名]变更了负责人。
			receiver = old_leader.toString();
			content = "您负责的[" + workProjectInfo.getTitle() + "]项目，[" + operator.getDisplayName() + "]变更了负责人。";
			notificationConnector.send(bussinessId, tenantId, sender, receiver, title, content,
					MsgConstants.MSG_TYPE_PROJECT);

			// 项目负责人发生变化时，须给新负责人发送消息
			// c) 标题：[项目名称]项目发布提醒
			// d) 内容：发布人[发布人姓名]发布的[项目名称]项目，由您负责。
			receiver = workProjectInfo.getLeader().toString();
			title = "[" + workProjectInfo.getTitle() + "]项目发布提醒";
			content = "[" + operator.getDisplayName() + "]发布的[" + workProjectInfo.getTitle() + "]项目，由您负责。";
			notificationConnector.send(bussinessId, tenantId, sender, receiver, title, content,
					MsgConstants.MSG_TYPE_PROJECT);
		}

	}

	/**
	 * 设置某人的所有消息状态为已读 Bing 2017.11.8
	 * 
	 * @param decryptedMap
	 * @return
	 */
	public Map<String, Object> SetReadAll(Map<String, Object> decryptedMap) {
		// percode=2
		Map<String, Object> returnMap = new HashMap<String, Object>();

		try {
			// 验证参数========================================
			if (!decryptedMap.containsKey("percode")) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "参数错误");
				return returnMap;
			}

			// 获取数据================================================
			Object percode = decryptedMap.get("percode");

			// 改数据
			// String sql = "update msg_info set STATUS=1 where STATUS=0 and
			// RECEIVER_ID=?;";
			// int rows = jdbcTemplate.update(sql, new Object[] { percode });

			// 查数据
			String sql = "SELECT ID FROM v_msg_info where STATUS=0 and RECEIVER_ID=?";
			List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, new Object[] { percode });

			// 改状态
			for (Map<String, Object> map : list) {
				String msgID = map.get("ID").toString();
				MsgInfo msgInfo = msgInfoManager.get(Long.parseLong(msgID));
				msgInfo.setStatus(1);
				msgInfoManager.save(msgInfo);
			}

			// 返回======================================================================
			returnMap.put("bSuccess", "true");
			returnMap.put("strMsg", "设置成功");
			// returnMap.put("rows", rows);
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("bSuccess", "false");
			returnMap.put("strMsg", "设置错误，请联系管理员");
		}

		return returnMap;
	}

	// @Resource==============================================
	@Resource
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Resource
	public void setMsgInfoManager(MsgInfoManager msgInfoManager) {
		this.msgInfoManager = msgInfoManager;
	}

	@Resource
	public void setNotificationConnector(NotificationConnector notificationConnector) {
		this.notificationConnector = notificationConnector;
	}

	@Resource
	public void setTenantHolder(TenantHolder tenantHolder) {
		this.tenantHolder = tenantHolder;
	}
	
	@Resource
	public void setHumanTaskConnectorImpl(
			HumanTaskConnectorImpl humanTaskConnectorImpl) {
		this.humanTaskConnectorImpl = humanTaskConnectorImpl;
	}
}
