/**
 * 
 */
package com.mossle.H5.work;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.mossle.H5.cms.MsgService;
import com.mossle.api.notification.NotificationConnector;
import com.mossle.api.tenant.TenantHolder;
import com.mossle.api.user.UserConnector;
import com.mossle.api.user.UserDTO;
import com.mossle.common.utils.FileUploadAPI;
import com.mossle.core.query.PropertyFilter;
import com.mossle.internal.store.persistence.domain.StoreInfo;
import com.mossle.msg.MsgConstants;
import com.mossle.pim.rs.ScheduleResource;
import com.mossle.project.persistence.domain.WorkProjectInfo;
import com.mossle.project.persistence.domain.WorkProjectNotify;
import com.mossle.project.persistence.domain.WorkProjectTaskbind;
import com.mossle.project.persistence.manager.WorkProjectInfoManager;
import com.mossle.project.persistence.manager.WorkProjectNotifyManager;
import com.mossle.project.persistence.manager.WorkProjectTaskbindManager;
import com.mossle.worktask.persistence.domain.WorkTaskInfo;
import com.mossle.worktask.persistence.manager.WorkTaskInfoManager;

/**
 * @author Bing
 *
 */
@Service
public class ProjectService {
	private static Logger logger = LoggerFactory.getLogger(ScheduleResource.class);
	private JdbcTemplate jdbcTemplate;
	private WorkProjectInfoManager workProjectInfoManager;
	private WorkProjectNotifyManager workProjectNotifyManager;
	private WorkProjectTaskbindManager workProjectTaskbindManager;
	private WorkTaskInfoManager workTaskInfoManager;
	private NotificationConnector notificationConnector;// 发送消息
	private TenantHolder tenantHolder;
	private FileUploadAPI fileUploadAPI;
	private UserConnector userConnector;
	private WorkTaskService workTaskService;
	private MsgService msgService;

	public Map<String, Object> ProjectCarryIn(Map<String, Object> decryptedMap) {
		// {strPageSize=5, strPerCode=2, percode=2, method=ProjectCarryIn,
		// sign=feb34b2ca6e9674bb16a376155d33e7d, strPageIndex=1, strOrder=0,
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
			List<Map<String, Object>> list = getProjects(1, percode, offset, row_count);

			// 返回======================================================================
			returnMap.put("bSuccess", "true");
			if (list.size() > 0) {
				returnMap.put("strMsg", "加载成功");
				returnMap.put("CarryInList", list);
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
		 * {"bSuccess":"true","strMsg":"加载成功","CarryInList":[
		 * {"PJ01":"1705220002","PJ02":"便利贴","PJ05":"PE1612210002","PJ05L":
		 * "13545454456","PJ05C":"李伟","PJ06":"\/Date(-62135596800000)\/",
		 * "PJ07C":null,"PJ11":"PE0000000001","PJ14":100,"PJ15":0,"CF02":"0"
		 * ,"IsAbove":0},
		 * {"PJ01":"1705180001","PJ02":"乐乐DSDS","PJ05":"PE1612210002",
		 * "PJ05L":"13545454456","PJ05C":"李伟","PJ06":
		 * "\/Date(-62135596800000)\/","PJ07C":null,"PJ11":"PE0000000001",
		 * "PJ14":100,"PJ15":0,"CF02":"0","IsAbove":0} ]}
		 */
	}

	public Map<String, Object> ProjectComplete(Map<String, Object> decryptedMap) {
		// {strPageSize=5, strPerCode=2, percode=2, method=ProjectComplete,
		// sign=e458da11bb70689df863747cb2020546, strPageIndex=1, timestamp=}

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
			List<Map<String, Object>> list = getProjects(2, percode, offset, row_count);

			// 返回======================================================================
			returnMap.put("bSuccess", "true");
			if (list.size() > 0) {
				returnMap.put("strMsg", "加载成功");
				returnMap.put("CompleteList", list);
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

	public Map<String, Object> ProjectAdd(Map<String, Object> decryptedMap) {
		// {strDescription=描述, strPerCode=2, percode=2, strLeader=3,
		// method=ProjectAdd, strName=名称, strComplete=2017-9-23,
		// sign=9ff8277c155faf4cd468be3ae5b1102c, strAnnex=,
		// AboveAndAbove=796946208931840,796946836160512, timestamp=}

		Map<String, Object> returnMap = new HashMap<String, Object>();

		try {
			// 验证参数================================================================================
			if (!decryptedMap.containsKey("startdate")) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "参数错误");
				return returnMap;
			}

			// 获取参数================================================
			String strLeader = decryptedMap.get("strLeader").toString();
			String strPerCode = decryptedMap.get("strPerCode").toString();
			String AboveAndAbove = decryptedMap.get("AboveAndAbove").toString();

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date plandate = sdf.parse(decryptedMap.get("strComplete").toString());
			Date startdate = sdf.parse(decryptedMap.get("startdate").toString());
			Date now = new Date();
			if (plandate.compareTo(startdate) < 0 || plandate.compareTo(now) < 0) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "计划完成日期不能早于开始日期或当前时间");
				return returnMap;
			}

			// 保存数据================================================
			WorkProjectInfo workProjectInfo = new WorkProjectInfo();
			workProjectInfo.setAnnex(decryptedMap.get("strAnnex").toString());
			workProjectInfo.setContent(decryptedMap.get("strDescription").toString());
			workProjectInfo.setLeader(Long.valueOf(strLeader));
			workProjectInfo.setPublisher(Long.valueOf(strPerCode));
			workProjectInfo.setTitle(decryptedMap.get("strName").toString());
			workProjectInfo.setPlandate(plandate);
			workProjectInfo.setStatus("0");// 0已发布
			workProjectInfo.setDatastatus("0");
			workProjectInfo.setPublishtime(now);
			workProjectInfo.setStartdate(startdate);// 计划开始时间
			workProjectInfoManager.save(workProjectInfo);

			Long projectId = workProjectInfo.getId();
			String strProjectID = projectId.toString();
			// 加知会---------------------------------------------------------------------
			if (!AboveAndAbove.isEmpty()) {
				String[] split_data = AboveAndAbove.split(",");
				for (String notify_userid : split_data) {
					// 如果知会人里也有负责人、当前登陆（发布）人，跳过。
					if (notify_userid.equals(strLeader))
						continue;
					if (notify_userid.equals(strPerCode))
						continue;

					WorkProjectNotify workProjectNotify = new WorkProjectNotify();
					workProjectNotify.setUserid(Long.valueOf(notify_userid));
					workProjectNotify.setWorkProjectInfo(workProjectInfo);
					workProjectNotifyManager.save(workProjectNotify);
				}
			}

			// 附件---------------------------------------------------------------------------------------------------------
			if (decryptedMap.containsKey("strAnnex")) {
				String strAnnex = decryptedMap.get("strAnnex").toString();// 附件
				if (!strAnnex.isEmpty()) {
					String[] split_data = strAnnex.split(",");
					for (String path : split_data) {
						fileUploadAPI.uploadFile(path, "1", strProjectID, "OA/project", "0");// 0项目附件
					}
				}
			}

			// 发消息=================================================
			if (workProjectInfo.getDatastatus().equals("1")) {
				// 项目消息
				String title = "[" + workProjectInfo.getTitle() + "]" + "项目发布提醒";
				String receiver = workProjectInfo.getLeader().toString();
				String bussinessId = strProjectID;
				String tenantId = tenantHolder.getTenantId();
				String sender = workProjectInfo.getPublisher().toString();
				UserDTO userDto = userConnector.findById(sender);
				String content = "[" + userDto.getDisplayName() + "]" + "发布的" + "[" + workProjectInfo.getTitle() + "]"
						+ "项目，由您负责。";// 发布人

				notificationConnector.send(bussinessId, tenantId, sender, receiver, title, content,
						MsgConstants.MSG_TYPE_PROJECT);

				// 知会消息
				List<WorkProjectNotify> workProjectNotifyList = workProjectNotifyManager.findBy("workProjectInfo.id",
						projectId);
				if (workProjectNotifyList != null && workProjectNotifyList.size() > 0) {
					content = "[" + userDto.getDisplayName() + "]" + "向您知会了" + "[" + workProjectInfo.getTitle() + "]"
							+ "项目，请查看。";// 发布人
					for (WorkProjectNotify workProjectNotify : workProjectNotifyList) {
						receiver = workProjectNotify.getUserid().toString();
						notificationConnector.send(strProjectID, tenantId, sender, receiver, title, content,
								MsgConstants.MSG_TYPE_PROJECT);
					}
				}
			}

			// 0草稿，1发布。
			String datastatus = "0";
			if (decryptedMap.containsKey("datastatus"))
				datastatus = decryptedMap.get("datastatus").toString();
			if (datastatus.equals("1")) {
				decryptedMap.put("strProjectID", projectId);
				return ProjectPublish(decryptedMap);// 发布
			}

			// 返回======================================================================
			returnMap.put("bSuccess", "true");
			returnMap.put("strMsg", "保存成功");// 存草稿

		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("bSuccess", "false");
			returnMap.put("strMsg", "保存错误，请联系管理员");
		}

		// System.out.println(returnMap);
		return returnMap;

		// {"bSuccess":"true","strMsg":"保存成功"}
	}

	public Map<String, Object> ProjectDetail(Map<String, Object> decryptedMap) {
		// {strPerCode=2, percode=2, method=ProjectDetail,
		// sign=41847e86615bc430b36f72decac4e952, strProjectID=null,
		// timestamp=}

		Map<String, Object> returnMap = new HashMap<String, Object>();

		try {
			// 验证参数================================================================================
			if (!decryptedMap.containsKey("strProjectID") || !decryptedMap.containsKey("percode")) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "参数错误");
				return returnMap;
			}

			Object percode = decryptedMap.get("percode");
			String strProjectID = decryptedMap.get("strProjectID").toString();

			// 验证权限，不是发布人、负责人，也不是知会人，不让看。
			String sql_check = " SELECT * FROM work_project_info i WHERE i.id=? ";
			sql_check += " and (i.publisher=? or i.leader=? or exists(select * from work_project_notify n where n.projectcode=i.id and n.userid=?)) ";
			List<Map<String, Object>> list_check = jdbcTemplate.queryForList(sql_check,
					new Object[] { strProjectID, percode, percode, percode });
			if (list_check.isEmpty()) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "您无权访问");
				return returnMap;
			}

			// 获取数据================================================
			List<Map<String, Object>> list = getProjects(strProjectID, percode);// 项目数据

			// 任务数据
			//String sql_task = "SELECT * FROM v_h5_work_task_info where projectcode=? and datastatus=1 and status<>3";
			String sql_task = "SELECT * FROM  ( "+WorkTaskUtils.getWorkTaskInfo()+" ) t  where projectcode=? and datastatus=1 and status<>3";
			
			
			List<Map<String, Object>> list_task = jdbcTemplate.queryForList(sql_task, new Object[] { strProjectID });

			// 返回======================================================================
			returnMap.put("bSuccess", "true");
			if (list.size() > 0) {
				returnMap.put("strMsg", "加载成功");
				returnMap.put("ProjectInfo", list);
				returnMap.put("TasksDetail", list_task);
			} else {
				returnMap.put("strMsg", "暂无数据");
			}

			// if (list_task.size() > 0) {
			// }
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("bSuccess", "false");
			returnMap.put("strMsg", "加载错误，请联系管理员");
		}

		return returnMap;

	}

	public Map<String, Object> ProjectEditLoad(Map<String, Object> decryptedMap) {
		// {strPerCode=3, percode=3, method=ProjectEditLoad,
		// sign=b57dcefcf89ce54a485f371cabb71f2a, strProjectID=829507536977920,
		// timestamp=}

		Map<String, Object> returnMap = new HashMap<String, Object>();

		try {
			// 验证参数================================================================================
			if (!decryptedMap.containsKey("strProjectID")) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "参数错误");
				return returnMap;
			}

			String strProjectID = decryptedMap.get("strProjectID").toString();

			// 获取数据================================================
			List<Map<String, Object>> list = getProjects(strProjectID);

			// 返回======================================================================
			returnMap.put("bSuccess", "true");
			if (list.size() > 0) {
				returnMap.put("strMsg", "加载成功");
				returnMap.put("EditLoad", list);
			} else {
				returnMap.put("strMsg", "暂无数据");
			}

			// if (list_task.size() > 0) {
			// }
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("bSuccess", "false");
			returnMap.put("strMsg", "加载错误，请联系管理员");
		}

		return returnMap;

	}

	public Map<String, Object> ProjectEdit(Map<String, Object> decryptedMap) {

		Map<String, Object> returnMap = new HashMap<String, Object>();

		try {
			// 验证参数================================================================================
			if (!decryptedMap.containsKey("strProjectID")) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "参数错误");
				return returnMap;
			}

			String strProjectID = decryptedMap.get("strProjectID").toString();
			String strLeader = decryptedMap.get("strLeader").toString();
			String strPerCode = decryptedMap.get("strPerCode").toString();
			UserDTO perCode = userConnector.findById(strPerCode);// 登录人
			String strComplete = decryptedMap.get("strComplete").toString();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String AboveAndAbove = decryptedMap.get("AboveAndAbove").toString();

			String datastatus = "";
			if (decryptedMap.containsKey("datastatus"))
				datastatus = decryptedMap.get("datastatus").toString();

			if (strProjectID.isEmpty()) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "项目编号不存在");
				return returnMap;
			}

			WorkProjectInfo workProjectInfo = workProjectInfoManager.get(Long.valueOf(strProjectID));
			String old_datastatus = workProjectInfo.getDatastatus();
			Long old_leader = workProjectInfo.getLeader();

			// 保存数据================================================
			workProjectInfo.setAnnex(decryptedMap.get("strAnnex").toString());
			workProjectInfo.setContent(decryptedMap.get("strDescription").toString());
			workProjectInfo.setLeader(Long.valueOf(strLeader));
			workProjectInfo.setPublisher(Long.valueOf(strPerCode));
			workProjectInfo.setTitle(decryptedMap.get("strName").toString());
			workProjectInfo.setPlandate(sdf.parse(strComplete));
			workProjectInfo.setPublishtime(new Date());

			if (decryptedMap.containsKey("startdate")) {
				String startdate = decryptedMap.get("startdate").toString();
				if (!startdate.isEmpty()) {
					workProjectInfo.setStartdate(sdf.parse(startdate));// 计划开始时间
				}
			}

			// if (!datastatus.isEmpty())
			// workProjectInfo.setDatastatus(datastatus);

			if (decryptedMap.containsKey("status"))
				workProjectInfo.setStatus(decryptedMap.get("status").toString());

			workProjectInfoManager.save(workProjectInfo);

			// 加知会---------------------------------------------------------------------
			removeAllNotify(strProjectID);// 编辑项目时如果改了知会人，需要删除原有的。（单独修改知会人，追加。）
			editNotify(workProjectInfo, AboveAndAbove, perCode);

			// 附件---------------------------------------------------------------------------------------------------------
			// 先删除
			List<StoreInfo> originalList = fileUploadAPI.getStoreByType("OA/project", strProjectID, "0");
			fileUploadAPI.removeStore(originalList);
			// 再添加
			if (decryptedMap.containsKey("strAnnex")) {
				String strAnnex = decryptedMap.get("strAnnex").toString();// 附件
				if (!strAnnex.isEmpty()) {
					String[] split_data = strAnnex.split(",");
					for (String path : split_data) {
						fileUploadAPI.uploadFile(path, "1", strProjectID, "OA/project", "0");// 0项目附件
					}
				}
			}

			// 发消息===================================
			msgService.editProject(workProjectInfo, old_leader, perCode);

			// 返回======================================================================
			// 发布返回。原状态不是1，新状态是1.
			if (!old_datastatus.equals("1") && datastatus.equals("1"))
				return ProjectPublish(decryptedMap);// 发布

			returnMap.put("bSuccess", "true");
			if (datastatus.equals("1")) {
				returnMap.put("strMsg", "发布成功");
			} else {
				returnMap.put("strMsg", "保存成功");
			}

		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("bSuccess", "false");
			returnMap.put("strMsg", "保存错误，请联系管理员");
		}

		// System.out.println(returnMap);
		return returnMap;
	}

	public Map<String, Object> ProjectClose(Map<String, Object> decryptedMap) {
		// {strPerCode=2, percode=2, method=ProjectClose,
		// sign=641160f1269b3cb5bc0702ffa299bf34, strProjectID=829471346180096,
		// timestamp=}

		Map<String, Object> returnMap = new HashMap<String, Object>();

		try {
			// 验证参数================================================================================
			if (!decryptedMap.containsKey("strProjectID") || !decryptedMap.containsKey("strPerCode")) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "参数错误");
				return returnMap;
			}

			String strProjectID = decryptedMap.get("strProjectID").toString();
			String strPerCode = decryptedMap.get("strPerCode").toString();
			UserDTO perCode = userConnector.findById(strPerCode);// 操作人

			// 关闭项目=================================================================
			WorkProjectInfo projectModel = workProjectInfoManager.findUniqueBy("id", Long.valueOf(strProjectID));

			if (projectModel == null) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "没有查询到项目信息");
				return returnMap;
			}

			String status = projectModel.getStatus();
			if (status == null)
				status = "0";

			if (!status.equals("0") && !status.equals("1")) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "该项目状态不是已发布或进行中状态");
				return returnMap;
			}

			projectModel.setStatus("3");
			projectModel.setCommittime(new Date());
			workProjectInfoManager.save(projectModel);

			// 关闭“未完成”的下级任务-------------------------------------------------------------------------------------------------------------------
			String tenantId = tenantHolder.getTenantId();
			String receiver = projectModel.getLeader().toString();
			String bussinessId = projectModel.getId().toString();

			List<WorkProjectTaskbind> workProjectTaskbindList = workProjectTaskbindManager.findBy("workProjectInfo.id",
					Long.valueOf(strProjectID));
			for (WorkProjectTaskbind workProjectTaskbind : workProjectTaskbindList) {
				WorkTaskInfo workTaskInfo = workProjectTaskbind.getWorkTaskInfo();
				// “未完成”的下级任务
				if (workTaskInfo.getStatus().equals("0") || workTaskInfo.getStatus().equals("1")) {
					workTaskService.closeTask(workTaskInfo, perCode);
					workTaskService.closeTasksByUp(workTaskInfo, perCode);
				}
			}

			// 发消息===========================================================
			// 向项目负责人发送消息提醒-----------------------------------------------------------------------------------------
			// a)标题：[项目名称]项目关闭通知； b)内容：您负责的[项目名称]项目，由发布人[发布人姓名]手动关闭，请查看。
			String title = "[" + projectModel.getTitle() + "]项目关闭通知";
			String content = "您负责的[" + projectModel.getTitle() + "]项目，由发布人[" + perCode.getDisplayName() + "]手动关闭，请查看。";
			notificationConnector.send(bussinessId, tenantId, strPerCode, receiver, title, content,
					MsgConstants.MSG_TYPE_PROJECT);

			// 向项目知会人发送消息提醒。--------------------------------------------------------------------------------
			// a) 标题：[项目名称]项目关闭通知；b) 内容：知会您的[项目名称]项目，由发布人[发布人姓名]手动关闭，请查看。
			content = "知会您的[" + projectModel.getTitle() + "]项目，由发布人[" + perCode.getDisplayName() + "]手动关闭，请查看。";
			List<WorkProjectNotify> workProjectNotifyList = workProjectNotifyManager.findBy("workProjectInfo.id",
					Long.valueOf(strProjectID));
			for (WorkProjectNotify workProjectNotify : workProjectNotifyList) {
				receiver = workProjectNotify.getUserid().toString();
				notificationConnector.send(bussinessId, tenantId, strPerCode, receiver, title, content,
						MsgConstants.MSG_TYPE_PROJECT);
			}

			// 项目下任务-------------------------------------------------------------------------------------------------------------------
			for (WorkProjectTaskbind workProjectTaskbind : workProjectTaskbindList) {
				WorkTaskInfo workTaskInfo = workProjectTaskbind.getWorkTaskInfo();
				// 向状态为“已完成”的下级任务的负责人发送消息提醒
				// a) 标题：上级[项目名称]关闭通知；
				// b) 内容：您负责的[任务标题]任务的上级[上级项目/任务名称]，由[操作人姓名]手动关闭，请查看。
				if (workTaskInfo.getStatus().equals("2") || workTaskInfo.getStatus().equals("3")
						|| workTaskInfo.getStatus().equals("4")) {
					bussinessId = workTaskInfo.getId().toString();
					receiver = workTaskInfo.getLeader().toString();
					title = "上级[" + projectModel.getTitle() + "]关闭通知";
					content = "您负责的[" + workTaskInfo.getTitle() + "]任务的上级[" + projectModel.getTitle() + "]，由["
							+ perCode.getDisplayName() + "]手动关闭，请查看。";
					notificationConnector.send(bussinessId, tenantId, strPerCode, receiver, title, content,
							MsgConstants.MSG_TYPE_PROJECT);
				}
			}
			// 返回======================================================================
			returnMap.put("bSuccess", "true");
			returnMap.put("strMsg", "关闭项目成功");
		} catch (ArithmeticException e) {
			e.printStackTrace();
			returnMap.put("bSuccess", "false");
			returnMap.put("strMsg", "关闭项目出错");
		}

		// System.out.println(returnMap);
		return returnMap;
	}

	public Map<String, Object> ProjectCommitValidate(Map<String, Object> decryptedMap) {

		Map<String, Object> returnMap = new HashMap<String, Object>();

		try {
			// 验证参数================================================================================
			if (!decryptedMap.containsKey("strProjectID")) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "参数错误");
				return returnMap;
			}

			String strProjectID = decryptedMap.get("strProjectID").toString();
			long projectID = Long.parseLong(strProjectID);

			return commitValidate(projectID);

		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("bSuccess", "false");
			returnMap.put("strMsg", "验证出错");
		}

		// System.out.println(returnMap);
		return returnMap;
	}

	public Map<String, Object> ProjectCommit(Map<String, Object> decryptedMap) {
		// {strPerCode=2, percode=2, method=ProjectCommit,
		// sign=641160f1269b3cb5bc0702ffa299bf34, strProjectID=829471346180096,
		// timestamp=}

		Map<String, Object> returnMap = new HashMap<String, Object>();

		try {
			// 验证参数================================================================================
			if (!decryptedMap.containsKey("strProjectID")) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "参数错误");
				return returnMap;
			}

			String strProjectID = decryptedMap.get("strProjectID").toString();
			long projectID = Long.parseLong(strProjectID);

			Map<String, Object> validateMap = commitValidate(projectID);
			if (validateMap.get("bSuccess").toString().equals("false")) {
				return validateMap;
			}

			// 保存==================================================================
			WorkProjectInfo projectModel = workProjectInfoManager.findUniqueBy("id", projectID);
			String efficiency = "0";
			Date commitDate = new Date();
			// DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Long times = (commitDate.getTime() - projectModel.getPlandate().getTime()) / 1000;
			if (times > 3600)
				efficiency = "2";// 延时
			else if (times < 0)
				efficiency = "1";// 提前

			projectModel.setStatus("2");
			projectModel.setCommittime(commitDate);
			projectModel.setEfficiency(efficiency);
			Integer hoursNum = (int) (Math.floor(Math.abs(times) / 3600));
			projectModel.setHoursnum(hoursNum);

			// 备注---------------------------------------------------------------------------------------------------------
			if (decryptedMap.containsKey("remarks")) {
				String remarks = decryptedMap.get("remarks").toString();
				if (!remarks.isEmpty()) {
					projectModel.setRemarks(remarks);
				}
			}

			workProjectInfoManager.save(projectModel);

			// 附件---------------------------------------------------------------------------------------------------------
			// 先删除
			List<StoreInfo> originalList = fileUploadAPI.getStoreByType("OA/project", strProjectID, "1");
			fileUploadAPI.removeStore(originalList);
			// 再添加
			if (decryptedMap.containsKey("strAnnex")) {
				String strAnnex = decryptedMap.get("strAnnex").toString();// 附件
				if (!strAnnex.isEmpty()) {
					String[] split_data = strAnnex.split(",");
					for (String path : split_data) {
						fileUploadAPI.uploadFile(path, "1", strProjectID, "OA/project", "1");// 1提交附件
					}
				}
			}

			// 发消息==========================================================
			String bussinessId = projectModel.getId().toString();
			String tenantId = tenantHolder.getTenantId();
			String title = "[" + projectModel.getTitle() + "]" + "项目完成提醒";
			String leader = projectModel.getLeader().toString();
			UserDTO leaderDTO = userConnector.findById(leader);// 负责人
			String publisher = projectModel.getPublisher().toString();
			UserDTO publisherDTO = userConnector.findById(publisher);// 发布人

			/*
			 * 4) 项目提交后，给项目的发布人发送消息提醒，消息格式如下： a) 标题：[项目名称]项目完成提醒 b)
			 * 内容：[负责人姓名]负责的 [项目名称]项目已提交，请查看。
			 */
			String content = "[" + leaderDTO.getDisplayName() + "]负责的" + "[" + projectModel.getTitle() + "]"
					+ "项目已提交，请查看。";
			notificationConnector.send(bussinessId, tenantId, leader, publisher, title, content,
					MsgConstants.MSG_TYPE_PROJECT);

			/*
			 * 5) 项目提交后，给项目的知会人发送消息提醒，消息格式如下： a) 标题：[项目名称]项目完成提醒 b)
			 * 内容：发布人[发布人姓名]发布的[项目名称]项目已提交，请查看。
			 */
			List<WorkProjectNotify> workProjectNotifyList = workProjectNotifyManager.findBy("workProjectInfo.id",
					projectID);
			if (workProjectNotifyList != null && workProjectNotifyList.size() > 0) {
				for (WorkProjectNotify workProjectNotify : workProjectNotifyList) {
					content = "发布人[" + publisherDTO.getDisplayName() + "]" + "发布的" + "[" + projectModel.getTitle() + "]"
							+ "项目已提交，请查看。";
					String receiver = workProjectNotify.getUserid().toString();
					notificationConnector.send(bussinessId, tenantId, leader, receiver, title, content,
							MsgConstants.MSG_TYPE_PROJECT);
				}
			}

			// 返回======================================================================
			returnMap.put("bSuccess", "true");
			returnMap.put("strMsg", "提交成功");

		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("bSuccess", "false");
			returnMap.put("strMsg", "关闭项目出错");
		}

		// System.out.println(returnMap);
		return returnMap;
	}

	public Map<String, Object> ProjectList(Map<String, Object> decryptedMap) {
		// {strPageSize=5, strPerCode=2, percode=2, method=ProjectList,
		// sign=feb34b2ca6e9674bb16a376155d33e7d, strPageIndex=1, timestamp=}

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
			List<Map<String, Object>> list = queryProjectList(decryptedMap);

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
	}

	/**
	 * 修改知会人
	 * 
	 * @param decryptedMap
	 * @return
	 */
	public Map<String, Object> AboveEdit(Map<String, Object> decryptedMap) {
		// strProjectID=,PN01S=

		Map<String, Object> returnMap = new HashMap<String, Object>();

		try {
			// 验证参数================================================================================
			if (!decryptedMap.containsKey("strProjectID") || !decryptedMap.containsKey("PN01S")) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "参数错误");
				return returnMap;
			}

			String strPerCode = decryptedMap.get("strPerCode").toString();
			UserDTO perCode = userConnector.findById(strPerCode);// 登录人
			String strProjectID = decryptedMap.get("strProjectID").toString();
			Long projectID = Long.valueOf(strProjectID);

			WorkProjectInfo projectModel = workProjectInfoManager.findUniqueBy("id", projectID);
			if (projectModel == null) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "没有查询到项目信息");
				return returnMap;
			}

			String selectIds = decryptedMap.get("PN01S").toString();

			editNotify(projectModel, selectIds, perCode);// 加知会，同时发消息。

			// 返回======================================================================
			returnMap.put("bSuccess", "true");
			returnMap.put("strMsg", "添加知会人成功");

		} catch (ArithmeticException e) {
			e.printStackTrace();
			returnMap.put("bSuccess", "false");
			returnMap.put("strMsg", "添加知会人出错");
			logger.error("添加知会人-：" + e.getMessage() + "\r\n" + e.fillInStackTrace());
		}

		return returnMap;
	}

	public Map<String, Object> ProjectPublish(Map<String, Object> decryptedMap) {

		Map<String, Object> returnMap = new HashMap<String, Object>();

		try {
			// 验证参数================================================================================
			if (!decryptedMap.containsKey("strProjectID")) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "参数错误");
				return returnMap;
			}

			String strProjectID = decryptedMap.get("strProjectID").toString();
			Long id = Long.valueOf(strProjectID);

			if (id == null || id < 1) {
				logger.debug("项目发布操作-获取参数id错误");
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "参数错误");
				return returnMap;
			}

			WorkProjectInfo workProjectInfo = workProjectInfoManager.get(id);
			if (workProjectInfo == null) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "获取项目失败");
				return returnMap;
			}
			if (workProjectInfo.getDatastatus().equals("1")) {
				returnMap.put("bSuccess", "true");
				returnMap.put("strMsg", "发布成功");
				return returnMap;
			}

			// 改状态=================================
			// workProjectInfo.setStatus("0");
			workProjectInfo.setDatastatus("1");
			workProjectInfo.setPublishtime(new Date());
			workProjectInfoManager.save(workProjectInfo);

			// 发消息===========================================
			String tenantId = tenantHolder.getTenantId();
			String bussinessId = workProjectInfo.getId().toString();
			String publisher = workProjectInfo.getPublisher().toString();
			UserDTO publisherDTO = userConnector.findById(publisher);
			String title = "[" + workProjectInfo.getTitle() + "]项目发布提醒";

			// 给负责人发送消息
			// [项目名称]项目发布提醒
			// 发布人[发布人姓名]发布的[项目名称]项目，由您负责。
			String receiver = workProjectInfo.getLeader().toString();
			String content = "发布人[" + publisherDTO.getDisplayName() + "]发布的[" + workProjectInfo.getTitle()
					+ "]项目，由您负责。";//
			notificationConnector.send(bussinessId, tenantId, publisher, receiver, title, content,
					MsgConstants.MSG_TYPE_PROJECT);

			// 给新知会人发送消息
			// [项目名称]项目发布提醒
			// 发布人[发布人姓名]向您知会了[项目名称]项目，请查看。
			List<WorkProjectNotify> workProjectNotifyList = workProjectNotifyManager.findBy("workProjectInfo.id", id);
			if (workProjectNotifyList !=null && workProjectNotifyList.size() > 0) {
				content = "发布人[" + publisherDTO.getDisplayName() + "]向您知会了[" + workProjectInfo.getTitle() + "]项目，请查看。";//
				for (WorkProjectNotify workProjectNotify : workProjectNotifyList) {
					receiver = workProjectNotify.getUserid().toString();
					notificationConnector.send(workProjectInfo.getId().toString(), tenantId, publisher, receiver, title,
							content, MsgConstants.MSG_TYPE_PROJECT);
				}
			}

			// 返回======================================================================
			returnMap.put("bSuccess", "true");
			returnMap.put("strMsg", "发布成功");

		} catch (ArithmeticException e) {
			logger.error("项目发布操作-：" + e.getMessage() + "\r\n" + e.fillInStackTrace());
			returnMap.put("bSuccess", "false");
			returnMap.put("strMsg", "发布出错");
		}

		// System.out.println(returnMap);
		return returnMap;
	}

	public Map<String, Object> ProjectDel(Map<String, Object> decryptedMap) {

		Map<String, Object> returnMap = new HashMap<String, Object>();

		try {
			// 验证参数================================================================================
			if (!decryptedMap.containsKey("strProjectID")) {
				logger.debug("项目删除操作-获取参数id错误");
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "参数错误");
				return returnMap;
			}

			String strProjectID = decryptedMap.get("strProjectID").toString();
			Long id = Long.valueOf(strProjectID);

			WorkProjectInfo project = workProjectInfoManager.findUniqueBy("id", id);
			if (project == null) {
				logger.debug("项目删除操作-没有查询到任务信息");
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "没有查询到项目信息");
				return returnMap;
			}
			if (project.getDatastatus().equals("2")) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "该项目已经被删除，刷新查看。");
				return returnMap;
			}

			project.setDatastatus("2");
			workProjectInfoManager.save(project);

			// 项目下任务-----------------------------------------------------------------------------------------------
			String publisher = project.getPublisher().toString();
			UserDTO publisherDTO = userConnector.findById(publisher);

			List<WorkProjectTaskbind> workProjectTaskbindList = workProjectTaskbindManager.findBy("workProjectInfo.id",
					Long.valueOf(strProjectID));
			for (WorkProjectTaskbind workProjectTaskbind : workProjectTaskbindList) {
				WorkTaskInfo workTaskInfo = workProjectTaskbind.getWorkTaskInfo();
				workTaskService.delTask(workTaskInfo, publisherDTO);// 删项目下任务
				workTaskService.delTasksByUp(workTaskInfo, publisherDTO);// 删任务下任务
			}

			// 发消息=================================================================
			String bussinessId = project.getId().toString();
			String tenantId = tenantHolder.getTenantId();

			// 给项目负责人发送消息-------------------------------------------------
			// a) 标题：[项目名称]项目删除通知
			// b) 内容：发布人[发布人姓名]删除了您负责的[项目名称]项目。
			String receiver = project.getLeader().toString();
			String title = "[" + project.getTitle() + "]项目删除通知";
			String content = "发布人[" + publisherDTO.getDisplayName() + "]删除了您负责的[" + project.getTitle() + "]项目。";//
			notificationConnector.send(bussinessId, tenantId, publisher, receiver, title, content,
					MsgConstants.MSG_TYPE_PROJECT);

			// 给项目知会人发送消息----------------------------------------------------------------------------------------
			// a) 标题：[项目名称]项目删除通知
			// b) 内容：发布人[发布人姓名]删除了知会给您的[项目名称]项目。
			content = "发布人[" + publisherDTO.getDisplayName() + "]删除了知会给您的[" + project.getTitle() + "]项目。";
			List<WorkProjectNotify> workProjectNotifyList = workProjectNotifyManager.findBy("workProjectInfo.id",
					Long.valueOf(strProjectID));
			for (WorkProjectNotify workProjectNotify : workProjectNotifyList) {
				receiver = workProjectNotify.getUserid().toString();
				notificationConnector.send(bussinessId, tenantId, publisher, receiver, title, content,
						MsgConstants.MSG_TYPE_PROJECT);
			}

			// 返回======================================================================
			returnMap.put("bSuccess", "true");
			returnMap.put("strMsg", "删除成功");
		} catch (ArithmeticException e) {
			returnMap.put("bSuccess", "false");
			returnMap.put("strMsg", "删除错误，请联系管理员");
			logger.error("项目删除操作-：" + e.getMessage() + "\r\n" + e.fillInStackTrace());
		}

		// System.out.println(returnMap);
		return returnMap;
	}

	public Map<String, Object> ProjectRealDel(Map<String, Object> decryptedMap) {

		Map<String, Object> returnMap = new HashMap<String, Object>();

		try {
			// 验证参数================================================================================
			if (!decryptedMap.containsKey("strProjectID")) {
				logger.debug("项目删除(物理)操作-获取参数id错误");
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "参数错误");
				return returnMap;
			}

			String strProjectID = decryptedMap.get("strProjectID").toString();
			Long id = Long.valueOf(strProjectID);

			WorkProjectInfo taskModel = workProjectInfoManager.findUniqueBy("id", id);
			if (taskModel == null) {
				logger.debug("项目删除(物理)操作-没有查询到项目信息");
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "没有查询到项目信息");
				return returnMap;
			}

			workProjectInfoManager.remove(taskModel);

			// 返回======================================================================
			returnMap.put("bSuccess", "true");
			returnMap.put("strMsg", "删除成功");
		} catch (ArithmeticException e) {
			returnMap.put("bSuccess", "false");
			returnMap.put("strMsg", "删除错误，请联系管理员");
			logger.error("项目删除(物理)操作-：" + e.getMessage() + "\r\n" + e.fillInStackTrace());
		}

		// System.out.println(returnMap);
		return returnMap;
	}

	public Map<String, Object> ProjectEvaluation(Map<String, Object> decryptedMap) {
		// strProjectID=;strEvaluation=;strPerCode=;score=

		Map<String, Object> returnMap = new HashMap<String, Object>();

		try {
			// 验证参数================================================================================
			if (!decryptedMap.containsKey("strProjectID") || !decryptedMap.containsKey("strEvaluation")) {
				logger.debug("项目评价操作-获取参数id错误");
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "参数错误");
				return returnMap;
			}

			String evaluateConent = decryptedMap.get("strEvaluation").toString();
			String score = decryptedMap.get("score").toString();
			String strProjectID = decryptedMap.get("strProjectID").toString();
			Long id = Long.valueOf(strProjectID);

			WorkProjectInfo project = workProjectInfoManager.findUniqueBy("id", id);
			if (project == null) {
				logger.debug("项目评价操作-没有查询到项目信息");
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "没有查询到项目信息");
				return returnMap;
			} else if (project.getStatus().equals("4")) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "该项目已经被评价，无需重复评价。");
				return returnMap;
			}

			if (evaluateConent.length() > 200) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "评价最多200个字");
				return returnMap;
			}

			project.setStatus("4");
			project.setEvalscore(Integer.valueOf(score));
			project.setEvaluate(evaluateConent);
			project.setEvaltime(new Date());
			workProjectInfoManager.save(project);

			// 发消息====================================================
			/*
			 * 3) 项目评价后，给项目的负责人发送消息提醒，消息格式如下： a) 标题：[项目名称]项目评价通知 b)
			 * 内容：您负责的[项目名称]项目，由发布人【发布人姓名】评价完成，请查看。
			 */
			String sender = project.getPublisher().toString();
			UserDTO publisherDto = userConnector.findById(sender);
			String receiver = project.getLeader().toString();
			String bussinessId = project.getId().toString();
			String tenantId = tenantHolder.getTenantId();
			String title = "[" + project.getTitle() + "]项目评价通知";
			String sendContent = "您负责的[" + project.getTitle() + "]项目，由发布人【" + publisherDto.getDisplayName()
					+ "】评价完成，请查看。";//
			notificationConnector.send(bussinessId, tenantId, sender, receiver, title, sendContent,
					MsgConstants.MSG_TYPE_PROJECT);

			// 返回======================================================================
			returnMap.put("bSuccess", "true");
			returnMap.put("strMsg", "评价成功");
		} catch (ArithmeticException e) {
			returnMap.put("bSuccess", "false");
			returnMap.put("strMsg", "项目评价出错，请联系管理员");
			logger.error("项目评价操作-：" + e.getMessage() + "\r\n" + e.fillInStackTrace());
		}

		// System.out.println(returnMap);
		return returnMap;
	}

	public Map<String, Object> ProjectExec(Map<String, Object> decryptedMap) {

		Map<String, Object> returnMap = new HashMap<String, Object>();

		try {
			// 验证参数================================================================================
			if (!decryptedMap.containsKey("strProjectID")) {
				logger.debug("项目执行操作-获取参数id错误");
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "参数错误");
				return returnMap;
			}

			String strProjectID = decryptedMap.get("strProjectID").toString();
			Long id = Long.valueOf(strProjectID);

			WorkProjectInfo project = workProjectInfoManager.findUniqueBy("id", id);

			if (project == null) {
				logger.debug("项目删除操作-没有查询到项目信息");
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "没有查询到项目信息");
				return returnMap;
			} else if (project.getDatastatus().equals("0")) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "该项目状态不是已发布或则状态已改变");
				return returnMap;
			}

			project.setStatus("1");
			project.setExectime(new Date());
			workProjectInfoManager.save(project);

			// 返回======================================================================
			returnMap.put("bSuccess", "true");
			returnMap.put("strMsg", "执行成功");
		} catch (ArithmeticException e) {
			returnMap.put("bSuccess", "false");
			returnMap.put("strMsg", "执行错误，请联系管理员");
			logger.error("项目执行操作-：" + e.getMessage() + "\r\n" + e.fillInStackTrace());
		}

		// System.out.println(returnMap);
		return returnMap;
	}

	// 私有方法========================================================================================
	/**
	 * 获取项目列表 Bing 2017.9.20
	 * 
	 * @param status
	 *            状态 1进行中 2已完成
	 * @param percode
	 *            用户id
	 * @param offset
	 * @param row_count
	 * @return
	 */
	List<Map<String, Object>> getProjects(Object status, Object percode, Object offset, Object row_count) {
		String where_definition = " status=? and (leader=? or publisher=? or exists(select * from work_project_notify n where n.projectcode=i.id and n.userid=?)) ";
		//String sql = "SELECT * FROM v_h5_work_project_info i where " + where_definition + " LIMIT ?,?";
		String sql = "SELECT * FROM  ( "+WorkTaskUtils.getH5WorkProjectInfo()+" )  i where " + where_definition + " LIMIT ?,?";
		
		return jdbcTemplate.queryForList(sql, new Object[] { status, percode, percode, percode, offset, row_count });
	}

	List<Map<String, Object>> getProjects(Object projectID) {
		//String sql = "SELECT * FROM `v_h5_work_project_info` WHERE `ID` = ?";
		String sql = "SELECT * FROM ("+WorkTaskUtils.getH5WorkProjectInfo()+") t WHERE `ID` = ?";
		return jdbcTemplate.queryForList(sql, new Object[] { projectID });
	}

	/**
	 * 获取项目详情数据（含是否可编辑） Bing 2017.10.11
	 * 
	 * @param projectID
	 * @param percode
	 *            登陆人id
	 * @return PJ16=是否可以编辑，0不可1可以。发布人登陆，状态不是“已完成”的才可以编辑（或重启）。
	 */
	List<Map<String, Object>> getProjects(Object projectID, Object percode) {
		//String sql = "SELECT *,CASE WHEN status<>2 and publisher=? THEN 1 ELSE 0 END as PJ16 FROM `v_h5_work_project_info` WHERE `ID` = ?";
		
		String sql = "SELECT *,CASE WHEN status<>2 and publisher=? THEN 1 ELSE 0 END as PJ16 FROM ("+WorkTaskUtils.getH5WorkProjectInfo()+") t WHERE `ID` = ?";
		
		return jdbcTemplate.queryForList(sql, new Object[] { percode, projectID });
	}

	List<Map<String, Object>> queryProjectList(Map<String, Object> decryptedMap) {
		Object percode = decryptedMap.get("percode");
		ArrayList<Object> argsList = new ArrayList<Object>();// sql参数列表

		// IsAbove
		// 是否是知会人--------------------------------------------------------------
		String select_expr = " *,case when exists(select * from work_project_notify n where n.projectcode=i.id and n.userid=?) then 1 else 0 end as IsAbove ";
		argsList.add(percode);

		// 查询条件--------------------------------------------------------------------
		String where_definition = " 1=1 ";

		if (decryptedMap.containsKey("status")) {
			if (!decryptedMap.get("status").toString().isEmpty()) {
				where_definition += " and status=? ";
				argsList.add(decryptedMap.get("status"));
			}
		}

		String datastatus = "";
		if (decryptedMap.containsKey("datastatus")) {
			datastatus = decryptedMap.get("datastatus").toString();
			if (!datastatus.isEmpty()) {
				where_definition += " and datastatus=? ";
				argsList.add(datastatus);
			}
		}

		String readertype = "";
		if (decryptedMap.containsKey("readertype")) {
			readertype = decryptedMap.get("readertype").toString().toLowerCase();
			switch (readertype) {
			case "publisher":
				where_definition += " and publisher=? ";
				argsList.add(percode);
				break;
			case "leader":
				where_definition += " and leader=? ";
				argsList.add(percode);
				break;
			case "notify":
				where_definition += " and exists(select * from work_project_notify n where n.projectcode=i.id and n.userid=?) ";
				argsList.add(percode);
				break;
			default:
				break;
			}
		}

		if (decryptedMap.containsKey("title")) {
			String title = decryptedMap.get("title").toString();
			if (!title.isEmpty()) {
				where_definition += " and title like ? ";
				argsList.add("%" + title + "%");
			}
		}

		if (decryptedMap.containsKey("beginPublishTime")) {
			if (!decryptedMap.get("beginPublishTime").toString().isEmpty()) {
				where_definition += " and publishtime>=? ";
				argsList.add(decryptedMap.get("beginPublishTime"));
			}
		}

		if (decryptedMap.containsKey("endPublishTime")) {
			String endPublishTime = decryptedMap.get("endPublishTime").toString();
			if (!endPublishTime.isEmpty()) {
				try {
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
					Date endPublishDate = sdf.parse(endPublishTime);

					Calendar calendar = Calendar.getInstance();
					calendar.setTime(endPublishDate);
					calendar.add(Calendar.DATE, 1);

					where_definition += " and publishtime<? ";
					argsList.add(calendar.getTime());
				} catch (ParseException e) {
					// TODO 自动生成的 catch 块
					e.printStackTrace();
				}
			}
		}

		// 排序-------------------------------------------------------------------------------------------------------------
		String order_by = " order by plandate DESC ";
		if (datastatus.equals("0")) {// 草稿
			order_by = " order by publishtime desc ";
		}
		if (datastatus.equals("1")) {// 已发
			order_by = " order by plandate desc,PJ15-PJ14 ";
		}
		if (readertype.equals("leader")) {// 负责
			order_by = " order by publishtime desc,PJ15-PJ14 ";
		}

		String sqlFrom = "SELECT i.id AS id,i.id AS PJ01,i.title AS title,i.title AS PJ02,i.content AS PJ03,i.leader AS leader," +
						"i.leader AS PJ05,GET_DISPLAY_NAME_BY_ID (i.leader) AS PJ05C,f_cellphone (i.leader) AS PJ05L,i.plandate AS plandate," +
						"i.plandate AS PJ06,i.status AS status,ifnull(i.status, 0) AS PJ07,i.datastatus AS datastatus,i.efficiency AS PJ08," +
						"i.publisher AS publisher,i.publisher AS PJ11,i.publishtime AS publishtime,i.publishtime AS PJ12," +
						"f_store_paths (i.id, 0, 'OA/project') AS PJ04,F_PROJECT_PLANNED_VALUE (i.id) AS PJ14," +
						"F_PROJECT_EARNED_VALUE (i.id) AS PJ15,F_PROJECT_NOTIFY_NAMES (i.id) AS CF02," +
						"(SELECT group_concat(n.userid SEPARATOR ',') FROM work_project_notify n " +
						"WHERE (n.projectcode = i.id)) AS CF03,i.startdate AS startdate," +
						"i.committime AS committime,i.hoursnum AS hoursnum,i.remarks AS remarks," +
						"i.evaluate AS evaluate,i.evalscore AS evalscore,i.evaltime AS evaltime," +
						"i.exectime AS exectime,f_store_paths (i.id, 1, 'OA/project') AS annex1 FROM work_project_info i";
		
		String sql = "SELECT " + select_expr + " FROM (" + sqlFrom + ") i where " + where_definition + order_by
				+ " LIMIT ?,?";

		// v_h5_work_project_info
		long pageIndex = Long.valueOf(decryptedMap.get("strPageIndex").toString());
		long row_count = Long.valueOf(decryptedMap.get("strPageSize").toString());
		long offset = row_count * (pageIndex - 1);
		argsList.add(offset);
		argsList.add(row_count);

		System.out.println(sql);
		System.out.println(argsList);
		return jdbcTemplate.queryForList(sql, argsList.toArray());
	}

	Map<String, Object> commitValidate(long projectID) {

		Map<String, Object> returnMap = new HashMap<String, Object>();

		try {
			// 验证参数================================================================================
			WorkProjectInfo projectModel = workProjectInfoManager.findUniqueBy("id", projectID);
			if (projectModel == null) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "没有查询到项目信息");
				return returnMap;
			} else if (!projectModel.getStatus().equals("1")) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "该项目状态不是进行中或则状态已改变");
				return returnMap;
			}

			List<WorkProjectTaskbind> taskBindList = workProjectTaskbindManager.findBy("workProjectInfo.id", projectID);
			if (taskBindList != null && taskBindList.size() > 0) {
				for (WorkProjectTaskbind taskbind : taskBindList) {
					WorkTaskInfo taskInfo = taskbind.getWorkTaskInfo();
					if (taskInfo != null) {
						if (!taskInfo.getStatus().equals("2") && !taskInfo.getStatus().equals("3")
								&& !taskInfo.getStatus().equals("4")) {
							returnMap.put("bSuccess", "false");
							returnMap.put("strMsg", "该项目中尚有任务未处理，不能提交项目！");
							return returnMap;
						}
					}
				}
			}

			// 返回======================================================================
			returnMap.put("bSuccess", "true");
			returnMap.put("strMsg", "验证成功");

		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("bSuccess", "false");
			returnMap.put("strMsg", "验证出错");
		}

		// System.out.println(returnMap);
		return returnMap;
	}

	void editNotify(WorkProjectInfo projectModel, String selectIds, UserDTO operator) {
		if (projectModel == null)
			return;

		if (selectIds.isEmpty())
			return;

		String[] selectList = selectIds.split(",");
		for (int i = 0; i < selectList.length; i++) {
			String notifyUserid = selectList[i];

			// 获取之前保存的知会人，如果已经有了就不再添加。
			List<PropertyFilter> propertyFilters = new ArrayList<PropertyFilter>();
			propertyFilters.add(new PropertyFilter("EQL_userid", notifyUserid));
			propertyFilters.add(new PropertyFilter("EQL_workProjectInfo.id", projectModel.getId().toString()));

			List<WorkProjectNotify> notifyList = workProjectNotifyManager.find(propertyFilters);
			if (notifyList != null && notifyList.size() > 0)
				continue;

			// 如果所选知会人中有当前项目的发布人、负责人也跳过不添加。
			if (notifyUserid.equals(projectModel.getPublisher().toString()))
				continue;
			if (notifyUserid.equals(projectModel.getLeader().toString()))
				continue;

			// 加知会人--------------------------------------------------------------
			WorkProjectNotify notify = new WorkProjectNotify();
			notify.setWorkProjectInfo(projectModel);
			notify.setUserid(Long.parseLong(notifyUserid));
			notify.setStatus("0");
			workProjectNotifyManager.save(notify);

			// 发消息---------------------------------------------------------------------------------
			// 项目不是正式数据不发
			if (!projectModel.getDatastatus().equals("1"))
				continue;

			String bussinessId = projectModel.getId().toString();
			String tenantId = tenantHolder.getTenantId();
			String title = "[" + projectModel.getTitle() + "]" + "知会提醒";
			String receiver = notify.getUserid().toString();
			String content = "[" + operator.getDisplayName() + "]将[" + projectModel.getTitle() + "]知会给您，您可查看项目的整体情况。";//
			notificationConnector.send(bussinessId, tenantId, projectModel.getPublisher().toString(), receiver, title,
					content, MsgConstants.MSG_TYPE_PROJECT);
		}
	}

	/**
	 * 删除所有知会人。Bing 2017.11.8
	 * 
	 * @param strProjectID
	 */
	void removeAllNotify(String strProjectID) {
		List<WorkProjectNotify> workProjectNotifyList = workProjectNotifyManager.findBy("workProjectInfo.id",
				Long.valueOf(strProjectID));
		workProjectNotifyManager.removeAll(workProjectNotifyList);
	}

	// @Resource=======================================================================================
	@Resource
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Resource
	public void setWorkProjectInfoManager(WorkProjectInfoManager workProjectInfoManager) {
		this.workProjectInfoManager = workProjectInfoManager;
	}

	@Resource
	public void setWorkProjectNotifyManager(WorkProjectNotifyManager workProjectNotifyManager) {
		this.workProjectNotifyManager = workProjectNotifyManager;
	}

	@Resource
	public void setWorkProjectTaskbindManager(WorkProjectTaskbindManager workProjectTaskbindManager) {
		this.workProjectTaskbindManager = workProjectTaskbindManager;
	}

	@Resource
	public void setWorkTaskInfoManager(WorkTaskInfoManager workTaskInfoManager) {
		this.workTaskInfoManager = workTaskInfoManager;
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
	public void setFileUploadAPI(FileUploadAPI fileUploadAPI) {
		this.fileUploadAPI = fileUploadAPI;
	}

	@Resource
	public void setUserConnector(UserConnector userConnector) {
		this.userConnector = userConnector;
	}

	@Resource
	public void setWorkTaskService(WorkTaskService workTaskService) {
		this.workTaskService = workTaskService;
	}

	@Resource
	public void setMsgService(MsgService msgService) {
		this.msgService = msgService;
	}

}
