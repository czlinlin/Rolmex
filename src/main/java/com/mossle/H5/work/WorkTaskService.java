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

import com.mossle.api.notification.NotificationConnector;
import com.mossle.api.tenant.TenantHolder;
import com.mossle.api.user.UserConnector;
import com.mossle.api.user.UserDTO;
import com.mossle.common.utils.FileUploadAPI;
import com.mossle.core.query.PropertyFilter;
import com.mossle.internal.store.persistence.domain.StoreInfo;
import com.mossle.msg.MsgConstants;
import com.mossle.project.persistence.domain.WorkProjectInfo;
import com.mossle.project.persistence.domain.WorkProjectTaskbind;
import com.mossle.project.persistence.manager.WorkProjectInfoManager;
import com.mossle.project.persistence.manager.WorkProjectTaskbindManager;
import com.mossle.worktask.persistence.domain.WorkTaskCc;
import com.mossle.worktask.persistence.domain.WorkTaskInfo;
import com.mossle.worktask.persistence.manager.WorkTaskCcManager;
import com.mossle.worktask.persistence.manager.WorkTaskInfoManager;
import com.mossle.worktask.rs.WorkTaskResource;

/**
 * @author Bing
 */
@Service
public class WorkTaskService {
	private JdbcTemplate jdbcTemplate;
	private WorkTaskInfoManager workTaskInfoManager;
	private WorkTaskCcManager workTaskCcManager;
	private static Logger logger = LoggerFactory.getLogger(WorkTaskResource.class);
	private TenantHolder tenantHolder;
	private NotificationConnector notificationConnector;// 发送消息
	private FileUploadAPI fileUploadAPI;
	private WorkProjectInfoManager workProjectInfoManager;
	private WorkProjectTaskbindManager workProjectTaskbindManager;
	private UserConnector userConnector;
	
	public Map<String, Object> TaskList(Map<String, Object> decryptedMap) {
		// {strPageSize=5, strPerCode=2, percode=2, method=TasktList,
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
			List<Map<String, Object>> list = queryTaskList(decryptedMap);

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

	public Map<String, Object> TaskAdd(Map<String, Object> decryptedMap) {

		Map<String, Object> returnMap = new HashMap<String, Object>();

		try {
			// 验证参数================================================================================
			// if (!decryptedMap.containsKey("strReportID")) {
			// returnMap.put("bSuccess", "false");
			// returnMap.put("strMsg", "参数错误");
			// return returnMap;
			// }

			// 获取参数
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			String strLeader = decryptedMap.get("strLeader").toString();
			String strPerCode = decryptedMap.get("strPerCode").toString();
			String strComplete = decryptedMap.get("strComplete").toString();
			Date plantime = sdf.parse(strComplete);
			String strWorkLoad = decryptedMap.get("strWorkLoad").toString();
			Integer workload = Integer.valueOf(strWorkLoad);
			String cc = decryptedMap.get("cc").toString();
			Date starttime = sdf.parse(decryptedMap.get("starttime").toString());
			String datastatus = decryptedMap.get("datastatus").toString();
			String strParentID = decryptedMap.get("strParentID").toString();// 父任务id
			if (strParentID.isEmpty())
				strParentID = "0";

			if (workload < 1) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "工作量太少");
				return returnMap;
			}

			if (plantime.compareTo(new Date()) <= 0) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "完成时间必须晚于当前时间");
				return returnMap;
			}

			if (plantime.compareTo(starttime) <= 0) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "完成时间必须晚于开始时间");
				return returnMap;
			}

			// 判断是否是第3级任务，判断父级的父级是否存在
			String sql_task = "SELECT * FROM `work_task_info` c JOIN work_task_info p on c.uppercode=p.id WHERE c.id=?";
			List<Map<String, Object>> list_task = jdbcTemplate.queryForList(sql_task, new Object[] { strParentID });
			if (list_task.size() > 0) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "子任务下不能继续创建子任务");
				return returnMap;
			}

			// 验证任务的时间范围在项目的时间范围内
			if (decryptedMap.containsKey("strProjectID")) {
				String strProjectID = decryptedMap.get("strProjectID").toString();// 父项目id
				if (!strProjectID.isEmpty()) {
					// 获取项目
					WorkProjectInfo workProjectInfo = workProjectInfoManager.get(Long.valueOf(strProjectID));
					// 比较时间
					// 项目完成日期应该是到当天24点，日期默认是0点，所以+1天。
					Calendar c = Calendar.getInstance();
					c.setTime(workProjectInfo.getPlandate());
					c.add(Calendar.DATE, 1);
					Date plandate = c.getTime();

					if (starttime.compareTo(workProjectInfo.getStartdate()) < 0) {
						returnMap.put("bSuccess", "false");
						returnMap.put("strMsg", "任务的开始时间不能早于项目开始日期");
						return returnMap;
					}
					if (starttime.compareTo(plandate) > 0) {
						returnMap.put("bSuccess", "false");
						returnMap.put("strMsg", "任务的开始时间不能晚于项目完成日期");
						return returnMap;
					}
					if (plantime.compareTo(workProjectInfo.getStartdate()) < 0) {
						returnMap.put("bSuccess", "false");
						returnMap.put("strMsg", "任务的完成时间不能早于项目开始日期");
						return returnMap;
					}
					if (plantime.compareTo(plandate) > 0) {
						returnMap.put("bSuccess", "false");
						returnMap.put("strMsg", "任务的完成时间不能晚于项目完成日期");
						return returnMap;
					}
				}
			}

			// 验证任务的时间范围在父任务的时间范围内
			if (!strParentID.isEmpty() && !strParentID.equals("0")) {
				// 获取父任务
				WorkTaskInfo task = workTaskInfoManager.get(Long.valueOf(strParentID));
				// 比较时间
				Date plandate = task.getPlantime();

				if (starttime.compareTo(task.getStarttime()) < 0) {
					returnMap.put("bSuccess", "false");
					returnMap.put("strMsg", "任务的开始时间不能早于父任务开始日期");
					return returnMap;
				}
				if (starttime.compareTo(plandate) > 0) {
					returnMap.put("bSuccess", "false");
					returnMap.put("strMsg", "任务的开始时间不能晚于父任务完成日期");
					return returnMap;
				}
				if (plantime.compareTo(task.getStarttime()) < 0) {
					returnMap.put("bSuccess", "false");
					returnMap.put("strMsg", "任务的完成时间不能早于父任务开始日期");
					return returnMap;
				}
				if (plantime.compareTo(plandate) > 0) {
					returnMap.put("bSuccess", "false");
					returnMap.put("strMsg", "任务的完成时间不能晚于父任务完成日期");
					return returnMap;
				}
			}

			// 保存数据================================================
			WorkTaskInfo workTaskInfo = new WorkTaskInfo();
			workTaskInfo.setTitle(decryptedMap.get("strName").toString());
			workTaskInfo.setContent(decryptedMap.get("strDesc").toString());
			workTaskInfo.setLeader(Long.valueOf(strLeader));
			workTaskInfo.setPublisher(Long.valueOf(strPerCode));
			workTaskInfo.setPlantime(plantime);
			workTaskInfo.setStatus("0");
			workTaskInfo.setDatastatus("0");
			workTaskInfo.setWorkload(workload);
			workTaskInfo.setUppercode(Long.valueOf(strParentID));
			workTaskInfo.setStarttime(starttime);
			workTaskInfo.setPublishtime(new Date());//
			workTaskInfoManager.save(workTaskInfo);

			String strTaskID = workTaskInfo.getId().toString();

			// 加抄送---------------------------------------------------------------------
			if (!cc.isEmpty()) {
				String[] split_data = cc.split(",");
				for (String c : split_data) {
					// 如果抄送人里也有负责人，跳过。
					if (c.equals(strLeader))
						continue;

					WorkTaskCc workTaskCc = new WorkTaskCc();
					workTaskCc.setCcno(Long.parseLong(c));
					workTaskCc.setWorkTaskInfo(workTaskInfo);
					workTaskCcManager.save(workTaskCc);
				}
			}

			// 附件---------------------------------------------------------------------------------------------------------
			if (decryptedMap.containsKey("strAnnex")) {
				String strAnnex = decryptedMap.get("strAnnex").toString();// 附件
				if (!strAnnex.isEmpty()) {
					String[] split_data = strAnnex.split(",");
					for (String path : split_data) {
						fileUploadAPI.uploadFile(path, "1", strTaskID, "OA/workTask", "0");// 0任务附件
					}
				}
			}

			// 项目下的一级任务保存
			if (decryptedMap.containsKey("strProjectID")) {
				String strProjectID = decryptedMap.get("strProjectID").toString();// 父项目id
				if (!strProjectID.isEmpty()) {
					Long projectcode = Long.valueOf(strProjectID);
					WorkProjectTaskbind workProjectTaskbind = new WorkProjectTaskbind();
					workProjectTaskbind.setBindtype("1");
					WorkProjectInfo workProjectInfo = workProjectInfoManager.get(projectcode);
					workProjectTaskbind.setWorkProjectInfo(workProjectInfo);
					workProjectTaskbind.setWorkTaskInfo(workTaskInfo);
					workProjectTaskbindManager.save(workProjectTaskbind);
				}
			}

			// 发布===================================================
			if (datastatus.equals("1")) {
				decryptedMap.put("strTaskID", strTaskID);
				return TaskPublish(decryptedMap);
			}

			// 返回======================================================================
			returnMap.put("bSuccess", "true");
			returnMap.put("strMsg", "保存成功");

		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("bSuccess", "false");
			returnMap.put("strMsg", "保存错误，请联系管理员");
		}

		// System.out.println(returnMap);
		return returnMap;

		// {"bSuccess":"true","strMsg":"保存成功"}
	}

	
	
	// 任务抄送   cz 20181214 add
	public Map<String, Object> TaskCC(Map<String, Object> decryptedMap) {

		Map<String, Object> returnMap = new HashMap<String, Object>();

		try {
			// 验证参数================================================================================
			if (!decryptedMap.containsKey("strTaskID") ) {
				logger.debug("任务抄送操作-获取参数id错误");
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "参数错误");
				return returnMap;
			}

			String userId = decryptedMap.get("strPerCode").toString();
			String strTaskID = decryptedMap.get("strTaskID").toString();
			Long id = Long.valueOf(strTaskID);
			String cc = decryptedMap.get("cc").toString();

			UserDTO publisherDTO = userConnector.findById(userId);// 当前登录人
			
//			Map<String, Object> validateMap = commitValidate(id);
//			if (validateMap.get("bSuccess").toString().equals("false"))
//				return validateMap;
			
			String tenantId = tenantHolder.getTenantId();
	    	//取出要抄送的任务内容
			WorkTaskInfo workTaskInfo = workTaskInfoManager.get(id);
	    
		     if(!cc.isEmpty()){
            	String[] copyIdList = null;  
 				//String[] copyNameList = null; 
 				copyIdList = cc.split(",");
 				
 				for (int i = 0; i < copyIdList.length; i++) {
 					//查看是否已经抄送过
 					String ccUserIds = copyIdList[i];
 					//String ccUserNames=copyNameList[i];
 		
 					 String hql = "from WorkTaskCc where  ccno=? and info_id=?";
 		            List<WorkTaskCc> wTCc = workTaskCcManager.find(hql, Long.parseLong(ccUserIds),id);
 					
	            	if(null != wTCc && wTCc.size()>0){
	            		//抄送过
	            		continue;
	            	}else{
	            		//没抄送过的人，加入抄送表
		 				String taskLeader = workTaskInfo.getLeader().toString();
	            		String publisher = workTaskInfo.getPublisher().toString();
	                    if (!(taskLeader.equals(ccUserIds)) && !(publisher.equals(ccUserIds))) {
	                        WorkTaskCc workTaskCc = new WorkTaskCc();
	                        workTaskCc.setCcno(Long.parseLong(ccUserIds));
	                        workTaskCc.setWorkTaskInfo(workTaskInfo);
	                        workTaskCcManager.save(workTaskCc);
	                        
	                      //发消息
		                    String content = "[" + publisherDTO.getDisplayName() + "]抄送给您的[" + workTaskInfo.getTitle() + "]任务已提交，请查看。";
		        					
		        			notificationConnector.send(id.toString(), tenantId, userId, ccUserIds, workTaskInfo.getTitle(), content,
		        					MsgConstants.MSG_TYPE_TASK);
		        		}
		        	}
	             }
	       }
			// 返回======================================================================
			returnMap.put("bSuccess", "true");
			returnMap.put("strMsg", "抄送成功");

		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("bSuccess", "false");
			returnMap.put("strMsg", "抄送错误，请联系管理员");
		}
		return returnMap;
	}
	
	
	public Map<String, Object> TaskDetail(Map<String, Object> decryptedMap) {

		Map<String, Object> returnMap = new HashMap<String, Object>();

		try {
			// 验证参数================================================================================
			if (!decryptedMap.containsKey("strTaskID") || !decryptedMap.containsKey("strPerCode")) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "参数错误");
				return returnMap;
			}

			String strTaskID = decryptedMap.get("strTaskID").toString();

			// 验证权限，不是发布人、负责人，也不是抄送人，不让看。
			// Object percode = decryptedMap.get("strPerCode");
			// String sql_check = " SELECT * FROM work_task_info i WHERE
			// i.id=?";
			// sql_check += " and (i.publisher=? or i.leader=? or exists(select
			// * from work_task_cc c where c.info_id=i.id and c.ccno=?)) ";
			// List<Map<String, Object>> list_check =
			// jdbcTemplate.queryForList(sql_check,
			// new Object[] { strTaskID, percode, percode, percode });
			// if (list_check.isEmpty()) {
			// returnMap.put("bSuccess", "false");
			// returnMap.put("strMsg", "您无权访问");
			// return returnMap;
			// }

			// 获取数据================================================
			//String sql_task = "SELECT * FROM v_h5_work_task_info where id=?";
			String sql_task ="SELECT * FROM  ( "+WorkTaskUtils.getWorkTaskInfo()+" )t  where id=?";
			
			List<Map<String, Object>> list_task = jdbcTemplate.queryForList(sql_task, new Object[] { strTaskID });

			//String sql_sub_task = "SELECT * FROM v_h5_work_task_info where uppercode=?";
			String sql_sub_task = "SELECT * FROM   ( "+WorkTaskUtils.getWorkTaskInfo()+" )t   where uppercode=?";
			List<Map<String, Object>> list_sub_task = jdbcTemplate.queryForList(sql_sub_task,
					new Object[] { strTaskID });
			// 返回======================================================================
			returnMap.put("bSuccess", "true");
			if (list_task.size() > 0) {
				returnMap.put("strMsg", "加载成功");
				returnMap.put("TasksDetail", list_task);
				returnMap.put("subTasks", list_sub_task);
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

	public Map<String, Object> TaskEditLoad(Map<String, Object> decryptedMap) {

		Map<String, Object> returnMap = new HashMap<String, Object>();

		try {
			// 验证参数================================================================================
			if (!decryptedMap.containsKey("strTaskID")) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "参数错误");
				return returnMap;
			}

			String strTaskID = decryptedMap.get("strTaskID").toString();

			// 获取数据================================================
			//String sql_task = "SELECT * FROM v_h5_work_task_info where id=?";
			String sql_task = "SELECT * FROM  ( "+WorkTaskUtils.getWorkTaskInfo()+" ) t  where id=?";
			List<Map<String, Object>> list_task = jdbcTemplate.queryForList(sql_task, new Object[] { strTaskID });

			// 返回======================================================================
			returnMap.put("bSuccess", "true");
			if (list_task.size() > 0) {
				returnMap.put("strMsg", "加载成功");
				returnMap.put("TaskLoad", list_task);
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

	public Map<String, Object> TaskEdit(Map<String, Object> decryptedMap) {

		Map<String, Object> returnMap = new HashMap<String, Object>();

		try {
			// 验证参数================================================================================
			if (!decryptedMap.containsKey("strTaskID") || !decryptedMap.containsKey("datastatus")) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "参数错误");
				return returnMap;
			}

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			String datastatus = decryptedMap.get("datastatus").toString();
			String strLeader = decryptedMap.get("strLeader").toString();
			String strPerCode = decryptedMap.get("strPerCode").toString();
			String strWorkLoad = decryptedMap.get("strWorkLoad").toString();
			String strParentID = decryptedMap.get("strParentID").toString();
			String cc = decryptedMap.get("cc").toString();
			String strTaskID = decryptedMap.get("strTaskID").toString();
			Long taskID = Long.valueOf(strTaskID);

			String strComplete = decryptedMap.get("strComplete").toString();
			Date plantime = new Date();
			try {
				plantime = sdf.parse(strComplete);
			} catch (Exception e) {
				// TODO: handle exception
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "完成时间格式错误");
				return returnMap;
			}

			Date starttime = new Date();
			try {
				starttime = sdf.parse(decryptedMap.get("starttime").toString());
			} catch (Exception e) {
				// TODO: handle exception
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "开始时间格式错误");
				return returnMap;
			}

			WorkTaskInfo workTaskInfo = workTaskInfoManager.get(taskID);

			if (workTaskInfo == null) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "获取任务失败");
				return returnMap;
			}
			String old_datastatus = workTaskInfo.getDatastatus();

			// 保存数据================================================
			workTaskInfo.setTitle(decryptedMap.get("strName").toString());
			workTaskInfo.setContent(decryptedMap.get("strDesc").toString());
			workTaskInfo.setLeader(Long.valueOf(strLeader));
			workTaskInfo.setPublisher(Long.valueOf(strPerCode));
			workTaskInfo.setStarttime(starttime);
			workTaskInfo.setPlantime(plantime);
			// workTaskInfo.setStatus("0");
			// workTaskInfo.setDatastatus(decryptedMap.get("datastatus").toString());
			workTaskInfo.setWorkload(Integer.valueOf(strWorkLoad));
			workTaskInfo.setUppercode(Long.valueOf(strParentID));
			workTaskInfo.setPublishtime(new Date());//
			workTaskInfoManager.save(workTaskInfo);

			// 加抄送---------------------------------------------------------------------
			if (!cc.isEmpty()) {
				if (taskID != null) {
					List<WorkTaskCc> workTaskCcList = workTaskCcManager.findBy("workTaskInfo.id", taskID);
					workTaskCcManager.removeAll(workTaskCcList);
				}

				String[] split_data = cc.split(",");
				for (String c : split_data) {
					// 如果抄送人里也有负责人，跳过。
					if (c.equals(strLeader))
						continue;

					WorkTaskCc workTaskCc = new WorkTaskCc();
					workTaskCc.setCcno(Long.parseLong(c));
					workTaskCc.setWorkTaskInfo(workTaskInfo);
					workTaskCcManager.save(workTaskCc);
				}
			}

			// 附件---------------------------------------------------------------------------------------------------------
			// 先删除
			List<StoreInfo> originalList = fileUploadAPI.getStoreByType("OA/workTask", strTaskID, "0");
			fileUploadAPI.removeStore(originalList);
			// 再添加
			if (decryptedMap.containsKey("strAnnex")) {
				String strAnnex = decryptedMap.get("strAnnex").toString();// 附件
				if (!strAnnex.isEmpty()) {
					String[] split_data = strAnnex.split(",");
					for (String path : split_data) {
						fileUploadAPI.uploadFile(path, "1", strTaskID, "OA/workTask", "0");
					}
				}
			}

			// 返回======================================================================
			// 发布返回。原状态不是1，新状态是1.
			if (!old_datastatus.equals("1") && datastatus.equals("1"))
				return TaskPublish(decryptedMap);

			// 保存返回
			returnMap.put("bSuccess", "true");
			if (datastatus.equals("1"))
				returnMap.put("strMsg", "发布成功");
			else
				returnMap.put("strMsg", "保存成功");

		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("bSuccess", "false");
			returnMap.put("strMsg", "保存错误，请联系管理员");
		}

		// System.out.println(returnMap);
		return returnMap;

		// {"bSuccess":"true","strMsg":"保存成功"}
	}

	/**
	 * 草稿任务-删除(物理)操作 Bing 2017.10.2
	 *
	 * @param decryptedMap
	 * @return
	 */
	public Map<String, Object> TaskRealDel(Map<String, Object> decryptedMap) {

		Map<String, Object> returnMap = new HashMap<String, Object>();

		try {
			// 验证参数================================================================================
			if (!decryptedMap.containsKey("strTaskID")) {
				logger.debug("任务删除(物理)操作-获取参数id错误");
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "参数错误");
				return returnMap;
			}

			String strTaskID = decryptedMap.get("strTaskID").toString();
			Long id = Long.valueOf(strTaskID);

			WorkTaskInfo taskModel = workTaskInfoManager.findUniqueBy("id", id);
			if (taskModel == null) {
				logger.debug("任务删除(物理)操作-没有查询到任务信息");
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "没有查询到任务信息");
				return returnMap;
			}

			List<WorkTaskInfo> taskChildList = workTaskInfoManager.findBy("uppercode", taskModel.getId());
			if (!CollectionUtils.isEmpty(taskChildList)) {
				for (WorkTaskInfo taskChild : taskChildList) {
					workTaskInfoManager.remove(taskChild);
				}
			}

			workTaskInfoManager.remove(taskModel);

			// 返回======================================================================
			returnMap.put("bSuccess", "true");
			returnMap.put("strMsg", "删除成功");
		} catch (ArithmeticException e) {
			returnMap.put("bSuccess", "false");
			returnMap.put("strMsg", "删除错误，请联系管理员");
			logger.error("任务删除(物理)操作-：" + e.getMessage() + "\r\n" + e.fillInStackTrace());
		}

		// System.out.println(returnMap);
		return returnMap;
	}

	/**
	 * 草稿任务-发布 Bing 2017.10.2
	 *
	 * @param decryptedMap
	 * @return
	 */
	public Map<String, Object> TaskPublish(Map<String, Object> decryptedMap) {

		Map<String, Object> returnMap = new HashMap<String, Object>();

		try {
			// 验证参数================================================================================
			if (!decryptedMap.containsKey("strTaskID")) {
				logger.debug("任务发布操作-获取参数id错误");
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "参数错误");
				return returnMap;
			}

			String strTaskID = decryptedMap.get("strTaskID").toString();
			Long id = Long.valueOf(strTaskID);
			WorkTaskInfo workTaskInfo = workTaskInfoManager.get(id);

			if (workTaskInfo == null) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "获取任务失败");
				return returnMap;
			}

			// 如果任务本来就是发布状态，直接返回。
			if (workTaskInfo.getDatastatus().equals("1")) {
				returnMap.put("bSuccess", "true");
				returnMap.put("strMsg", "发布成功");
				return returnMap;
			}

			// 验证上级任务状态
			Long upcode = workTaskInfo.getUppercode();
			if (upcode != null && upcode != 0) {
				WorkTaskInfo workTaskInfoParent = workTaskInfoManager.get(upcode);
				if (workTaskInfoParent != null) {
					String status = workTaskInfoParent.getStatus();
					if (status.equals("2") || status.equals("3") || status.equals("4")) {
						logger.debug("任务发布操作-父任务不是进行中和已发布状态不允许发布子任务");
						returnMap.put("bSuccess", "false");
						returnMap.put("strMsg", "该任务的上级任务已完成或已关闭，不能发布！");
						return returnMap;
					}
				}
			}

			// 改状态====================================================
			// workTaskInfo.setStatus("0");// 设置任务为已发布任务
			workTaskInfo.setDatastatus("1");// 设置任务为发布状态
			workTaskInfo.setPublishtime(new Date());
			workTaskInfoManager.save(workTaskInfo);

			// 发消息==================================================
			String tenantId = tenantHolder.getTenantId();
			String bussinessId = workTaskInfo.getId().toString();
			String publisher = workTaskInfo.getPublisher().toString();
			UserDTO publisherDTO = userConnector.findById(publisher);

			// 向负责人发送消息
			// a) 标题：[任务标题]任务发布提醒
			// b) 内容：发布人[发布人姓名]发布的[任务标题]任务，由您负责。
			String receiver = workTaskInfo.getLeader().toString();
			String title = "[" + workTaskInfo.getTitle() + "]任务发布提醒";
			String content = "发布人[" + publisherDTO.getDisplayName() + "]发布的[" + workTaskInfo.getTitle() + "]任务，由您负责。";//
			notificationConnector.send(bussinessId, tenantId, publisher, receiver, title, content,
					MsgConstants.MSG_TYPE_TASK);

			// 向抄送人发送消息
			// a) 标题：[任务标题]任务发布提醒
			// b) 内容：发布人[发布人姓名]向您抄送了[任务标题]任务，请查看。
			List<WorkTaskCc> workTaskCcs = workTaskCcManager.findBy("workTaskInfo.id", id);
			if (workTaskCcs != null && workTaskCcs.size() > 0) {
				content = "发布人[" + publisherDTO.getDisplayName() + "]向您抄送了[" + workTaskInfo.getTitle() + "]任务，请查看。";//
				for (WorkTaskCc WorkTaskCc : workTaskCcs) {
					receiver = WorkTaskCc.getCcno().toString();
					notificationConnector.send(workTaskInfo.getId().toString(), tenantId, publisher, receiver, title,
							content, MsgConstants.MSG_TYPE_TASK);
				}
			}

			// 返回======================================================================
			returnMap.put("bSuccess", "true");
			returnMap.put("strMsg", "发布成功");
		} catch (ArithmeticException e) {
			returnMap.put("bSuccess", "false");
			returnMap.put("strMsg", "发布错误，请联系管理员");
			logger.error("任务删除(物理)操作-：" + e.getMessage() + "\r\n" + e.fillInStackTrace());
		}

		// System.out.println(returnMap);
		return returnMap;
	}

	/**
	 * 已发任务-（逻辑）删除 Bing 2017.10.2
	 *
	 * @param decryptedMap
	 * @return
	 */
	public Map<String, Object> TaskDel(Map<String, Object> decryptedMap) {

		Map<String, Object> returnMap = new HashMap<String, Object>();

		try {
			// 验证参数================================================================================
			if (!decryptedMap.containsKey("strTaskID")) {
				logger.debug("任务删除操作-获取参数id错误");
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "参数错误");
				return returnMap;
			}

			String strTaskID = decryptedMap.get("strTaskID").toString();
			Long id = Long.valueOf(strTaskID);

			WorkTaskInfo taskModel = workTaskInfoManager.findUniqueBy("id", id);
			if (taskModel == null) {
				logger.debug("任务删除操作-没有查询到任务信息");
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "没有查询到任务信息");
				return returnMap;
			}
			if (taskModel.getDatastatus().equals("2")) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "该任务已经被删除，刷新查看。");
				return returnMap;
			}

			UserDTO publisherDTO = userConnector.findById(taskModel.getPublisher().toString());
			delTask(taskModel, publisherDTO);
			delTasksByUp(taskModel, publisherDTO);

			// 返回======================================================================
			returnMap.put("bSuccess", "true");
			returnMap.put("strMsg", "删除成功");
		} catch (ArithmeticException e) {
			returnMap.put("bSuccess", "false");
			returnMap.put("strMsg", "删除错误，请联系管理员");
			logger.error("任务删除操作-：" + e.getMessage() + "\r\n" + e.fillInStackTrace());
		}

		// System.out.println(returnMap);
		return returnMap;
	}

	/**
	 * 已发任务-关闭操作 Bing 2017.10.2
	 *
	 * @param decryptedMap:{strTaskID=,strPerCode=}
	 * @return
	 */
	public Map<String, Object> TaskClosed(Map<String, Object> decryptedMap) {

		Map<String, Object> returnMap = new HashMap<String, Object>();

		try {
			// 验证参数================================================================================
			if (!decryptedMap.containsKey("strTaskID") || !decryptedMap.containsKey("strPerCode")) {
				logger.debug("关闭任务操作-获取参数id错误");
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "参数错误");
				return returnMap;
			}

			String strTaskID = decryptedMap.get("strTaskID").toString();
			Long id = Long.valueOf(strTaskID);

			String strPerCode = decryptedMap.get("strPerCode").toString();
			UserDTO operator = userConnector.findById(strPerCode);// 操作人

			WorkTaskInfo taskModel = workTaskInfoManager.findUniqueBy("id", id);
			if (taskModel == null) {
				logger.debug("关闭任务操作-没有查询到任务信息");
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "没有查询到任务信息");
				return returnMap;
			}

			if (!taskModel.getStatus().equals("0") && !taskModel.getStatus().equals("1")) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "该任务状态不是已发布或进行中状态");
				return returnMap;
			}

			closeTask(taskModel, operator);
			closeTasksByUp(taskModel, operator);

			// 返回======================================================================
			returnMap.put("bSuccess", "true");
			returnMap.put("strMsg", "关闭成功");
		} catch (ArithmeticException e) {
			returnMap.put("bSuccess", "false");
			returnMap.put("strMsg", "关闭错误，请联系管理员");
			logger.error("关闭任务操作-：" + e.getMessage() + "\r\n" + e.fillInStackTrace());
		}

		// System.out.println(returnMap);
		return returnMap;
	}

	/**
	 * 已办任务-任务评价操作 Bing 2017.10.2
	 *
	 * @param decryptedMap
	 * @return
	 */
	public Map<String, Object> TaskEvaluation(Map<String, Object> decryptedMap) {
		// strTaskID=;strEvaluation=;strPerCode=;score=

		Map<String, Object> returnMap = new HashMap<String, Object>();

		try {
			// 验证参数================================================================================
			if (!decryptedMap.containsKey("strTaskID") || !decryptedMap.containsKey("strEvaluation")) {
				logger.debug("任务评价操作-获取参数id错误");
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "参数错误");
				return returnMap;
			}

			String evaluateConent = decryptedMap.get("strEvaluation").toString();
			String score = decryptedMap.get("score").toString();
			String strPerCode = decryptedMap.get("strPerCode").toString();
			String strTaskID = decryptedMap.get("strTaskID").toString();
			Long id = Long.valueOf(strTaskID);

			WorkTaskInfo taskModel = workTaskInfoManager.findUniqueBy("id", id);
			if (taskModel == null) {
				logger.debug("任务评价操作-没有查询到任务信息");
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "没有查询到任务信息");
				return returnMap;
			} else if (taskModel.getStatus().equals("4")) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "该任务已经被评价，无需重复评价。");
				return returnMap;
			}

			// String encode = "utf-8";
			// try {
			// evaluateConent = java.net.URLDecoder.decode(content, encode);
			// } catch (UnsupportedEncodingException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }

			if (evaluateConent.length() > 200) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "评价最多200个字");
				return returnMap;
			}

			taskModel.setStatus("4");
			taskModel.setEvalscore(Integer.valueOf(score));
			taskModel.setEvaluate(evaluateConent);
			taskModel.setEvaltime(new Date());
			workTaskInfoManager.save(taskModel);

			String title = taskModel.getTitle() + "任务评价提醒";
			// [XXX]对您负责的[XXX]项目完成了评价，敬请查看。
			String sendContent = "对您负责的" + taskModel.getTitle() + "完成了评价，" + "敬请查看。";// currentUserHolder.getName()+

			String receiver = taskModel.getLeader().toString();
			String bussinessId = taskModel.getId().toString();
			String tenantId = tenantHolder.getTenantId();
			// 提交任务，发送消息给负责人
			notificationConnector.send(bussinessId, tenantId, strPerCode, receiver, title, sendContent,
					MsgConstants.MSG_TYPE_TASK);

			// 返回======================================================================
			returnMap.put("bSuccess", "true");
			returnMap.put("strMsg", "评价成功");
		} catch (ArithmeticException e) {
			returnMap.put("bSuccess", "false");
			returnMap.put("strMsg", "任务评价出错，请联系管理员");
			logger.error("任务评价操作-：" + e.getMessage() + "\r\n" + e.fillInStackTrace());
		}

		// System.out.println(returnMap);
		return returnMap;
	}

	/**
	 * 提交 Bing 2017.10.5
	 *
	 * @param decryptedMap
	 * @return
	 */
	public Map<String, Object> TaskCommit(Map<String, Object> decryptedMap) {
		// strTaskID=;remarks=;strPerCode=;

		Map<String, Object> returnMap = new HashMap<String, Object>();

		try {
			// 验证参数================================================================================
			if (!decryptedMap.containsKey("strTaskID") || !decryptedMap.containsKey("remarks")) {
				logger.debug("任务提交操作-获取参数id错误");
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "参数错误");
				return returnMap;
			}

			String strPerCode = decryptedMap.get("strPerCode").toString();
			String remarks = decryptedMap.get("remarks").toString();
			String strTaskID = decryptedMap.get("strTaskID").toString();
			Long id = Long.valueOf(strTaskID);

			Map<String, Object> validateMap = commitValidate(id);
			if (validateMap.get("bSuccess").toString().equals("false"))
				return validateMap;

			// 保存===============================================
			WorkTaskInfo taskModel = workTaskInfoManager.findUniqueBy("id", id);
			String efficiency = "0";
			Date commitDate = new Date();
			// DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Long times = commitDate.getTime() - taskModel.getPlantime().getTime();
			if (times > 3600)
				efficiency = "2";// 延时
			else if (times < 0)
				efficiency = "1";// 提前

			taskModel.setStatus("2");
			taskModel.setCommittime(commitDate);
			taskModel.setEfficiency(efficiency);
			Integer hoursNum = (int) (Math.floor(Math.abs(times) / 3600));
			taskModel.setHoursnum(hoursNum);
			taskModel.setAnnex(remarks);
			workTaskInfoManager.save(taskModel);

			// 发消息========================================
			String tenantId = tenantHolder.getTenantId();
			String bussinessId = taskModel.getId().toString();
			String title = "[" + taskModel.getTitle() + "]任务提交通知";
			String publisher = taskModel.getPublisher().toString();
			UserDTO publisherDTO = userConnector.findById(publisher);// 发布人
			String leader = taskModel.getLeader().toString();
			UserDTO leaderDTO = userConnector.findById(leader);// 负责人

			// 给任务的发布人发送消息
			// a) 标题：[任务标题]任务提交通知
			// b) 内容：[任务负责人姓名]负责的[任务标题]任务已提交，请查看。
			String content = "[" + leaderDTO.getDisplayName() + "]负责的[" + taskModel.getTitle() + "]任务已提交，请查看。";//
			notificationConnector.send(bussinessId, tenantId, strPerCode, publisher, title, content,
					MsgConstants.MSG_TYPE_TASK);

			// 给任务的抄送人发送消息提醒
			// a) 标题：[任务标题]任务提交通知
			// b) 内容：发布人[任务发布人姓名]抄送给您的[任务标题]任务已提交，请查看。
			List<WorkTaskCc> ccList = workTaskCcManager.findBy("workTaskInfo.id", id);
			if (ccList != null && ccList.size() > 0) {
				for (WorkTaskCc cc : ccList) {
					content = "发布人[" + publisherDTO.getDisplayName() + "]抄送给您的[" + taskModel.getTitle() + "]任务已提交，请查看。";
					String ccno = cc.getCcno().toString();
					notificationConnector.send(bussinessId, tenantId, strPerCode, ccno, title, content,
							MsgConstants.MSG_TYPE_TASK);
				}
			}

			// 附件---------------------------------------------------------------------------------------------------------
			// 先删除
			List<StoreInfo> originalList = fileUploadAPI.getStoreByType("OA/workTask", strTaskID, "1");
			fileUploadAPI.removeStore(originalList);
			// 再添加
			if (decryptedMap.containsKey("strAnnex")) {
				String strAnnex = decryptedMap.get("strAnnex").toString();// 附件
				if (!strAnnex.isEmpty()) {
					String[] split_data = strAnnex.split(",");
					for (String path : split_data) {
						fileUploadAPI.uploadFile(path, "1", strTaskID, "OA/workTask", "1");// 1提交附件
					}
				}
			}

			// 返回======================================================================
			returnMap.put("bSuccess", "true");
			returnMap.put("strMsg", "提交成功");
		} catch (Exception e) {
			logger.error("任务提交操作-：" + e.getMessage() + "\r\n" + e.fillInStackTrace());
			returnMap.put("bSuccess", "false");
			returnMap.put("strMsg", "提交出错");
		}

		// System.out.println(returnMap);
		return returnMap;
	}

	public Map<String, Object> TaskCommitValidate(Map<String, Object> decryptedMap) {
		// strTaskID=;remarks=;strPerCode=;

		Map<String, Object> returnMap = new HashMap<String, Object>();

		try {
			// 验证参数================================================================================
			if (!decryptedMap.containsKey("strTaskID")) {
				logger.debug("任务提交操作-获取参数id错误");
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "参数错误");
				return returnMap;
			}

			String strTaskID = decryptedMap.get("strTaskID").toString();
			Long id = Long.valueOf(strTaskID);
			return commitValidate(id);

		} catch (Exception e) {
			logger.error("任务验证操作-：" + e.getMessage() + "\r\n" + e.fillInStackTrace());
			returnMap.put("bSuccess", "false");
			returnMap.put("strMsg", "验证出错");
		}

		// System.out.println(returnMap);
		return returnMap;
	}

	/**
	 * 备注 Bing 2017.10.5
	 *
	 * @param decryptedMap
	 * @return
	 */
	public Map<String, Object> TaskComment(Map<String, Object> decryptedMap) {
		// strTaskID=;content=;strPerCode=;

		Map<String, Object> returnMap = new HashMap<String, Object>();

		try {
			// 验证参数================================================================================
			if (!decryptedMap.containsKey("strTaskID") || !decryptedMap.containsKey("content")) {
				logger.debug("任务备注操作-获取参数id错误");
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "参数错误");
				return returnMap;
			}

			String strPerCode = decryptedMap.get("strPerCode").toString();
			UserDTO perCode = userConnector.findById(strPerCode);
			String content = decryptedMap.get("content").toString();
			String strTaskID = decryptedMap.get("strTaskID").toString();
			Long id = Long.valueOf(strTaskID);

			if (content.equals("")) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "请填写备注内容");
				return returnMap;
			}

			WorkTaskInfo taskModel = workTaskInfoManager.findUniqueBy("id", id);
			if (taskModel == null) {
				logger.debug("任务备注保存-没有查询到任务信息");
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "没有查询到任务信息");
				return returnMap;
			}

			if (content.length() > 200) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "备注内容太多");
				return returnMap;
			}

			String oldContent = taskModel.getRemarks() == null ? "" : taskModel.getRemarks();
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			content = "[" + perCode.getDisplayName() + "]于" + formatter.format(new Date()) + "添加备注：" + content;//
			taskModel.setRemarks(oldContent + content + "；");

			workTaskInfoManager.save(taskModel);

			// 返回======================================================================
			returnMap.put("bSuccess", "true");
			returnMap.put("strMsg", "备注保存成功");
		} catch (ArithmeticException e) {
			logger.error("任务备注保存-：" + e.getMessage() + "\r\n" + e.fillInStackTrace());
			returnMap.put("bSuccess", "false");
			returnMap.put("strMsg", "备注保存失败");
		}

		// System.out.println(returnMap);
		return returnMap;
	}

	public Map<String, Object> TaskExec(Map<String, Object> decryptedMap) {

		Map<String, Object> returnMap = new HashMap<String, Object>();

		try {
			// 验证参数================================================================================
			if (!decryptedMap.containsKey("strTaskID")) {
				logger.debug("执行任务操作-获取参数id错误");
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "参数错误");
				return returnMap;
			}

			String strTaskID = decryptedMap.get("strTaskID").toString();
			Long id = Long.valueOf(strTaskID);

			if (id == null || id < 1) {
				logger.debug("任务执行操作-获取参数id错误");
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "参数错误");
				return returnMap;
			}

			WorkTaskInfo taskModel = workTaskInfoManager.findUniqueBy("id", id);
			if (taskModel == null) {
				logger.debug("任务执行操作-没有查询到任务信息");
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "没有查询到任务信息");
				return returnMap;
			} else if (!taskModel.getStatus().equals("0")) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "该任务状态不是已发布或则状态已改变");
				return returnMap;
			}
			// 子任务执行，一级任务和项目都变成进行中状态
			taskModel.setStatus("1");
			taskModel.setExectime(new Date());
			if (taskModel.getUppercode() != null && taskModel.getUppercode() != 0) {
				WorkTaskInfo FTaskInfo = workTaskInfoManager.findUniqueBy("id", taskModel.getUppercode());
				if (FTaskInfo != null) {
					FTaskInfo.setStatus("1");
					FTaskInfo.setExectime(new Date());
					workTaskInfoManager.save(FTaskInfo);

					WorkProjectTaskbind workProjectTaskbind = workProjectTaskbindManager.findUniqueBy("workTaskInfo.id",
							FTaskInfo.getId());
					if (workProjectTaskbind != null) {
						WorkProjectInfo workProjectInfo = workProjectTaskbind.getWorkProjectInfo();
						workProjectInfo.setStatus("1");
						workProjectInfo.setExectime(new Date());
					}
				}
			}
			// 任务执行，项目变成进行中状态
			WorkProjectTaskbind workProjectTaskbind = workProjectTaskbindManager.findUniqueBy("workTaskInfo.id",
					taskModel.getId());
			if (workProjectTaskbind != null) {
				WorkProjectInfo workProjectInfo = workProjectTaskbind.getWorkProjectInfo();
				workProjectInfo.setStatus("1");
				workProjectInfo.setExectime(new Date());
			}
			workTaskInfoManager.save(taskModel);

			// 返回======================================================================
			returnMap.put("bSuccess", "true");
			returnMap.put("strMsg", "执行成功");
		} catch (ArithmeticException e) {
			returnMap.put("bSuccess", "false");
			returnMap.put("strMsg", "执行错误，请联系管理员");
			logger.error("执行任务操作-：" + e.getMessage() + "\r\n" + e.fillInStackTrace());
		}

		// System.out.println(returnMap);
		return returnMap;
	}

	List<Map<String, Object>> queryTaskList(Map<String, Object> decryptedMap) {
		ArrayList<Object> argsList = new ArrayList<Object>();
		String where_definition = " 1=1 ";

		String status = "";
		if (decryptedMap.containsKey("status")) {
			status = decryptedMap.get("status").toString();
			if (!status.isEmpty()) {
				where_definition += " and FIND_IN_SET(status,?) ";// in(?)
				argsList.add(decryptedMap.get("status"));
			}
		}

		if (decryptedMap.containsKey("datastatus")) {
			if (!decryptedMap.get("datastatus").toString().isEmpty()) {
				where_definition += " and datastatus=? ";
				argsList.add(decryptedMap.get("datastatus"));
			}
		}

		String readertype = "";
		if (decryptedMap.containsKey("readertype")) {
			Object percode = decryptedMap.get("percode");
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
			case "cc":
				where_definition += " and exists(select * from work_task_cc c where c.info_id=i.id and c.ccno=?) ";
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
		String order_by = "";
		if (readertype.equals("leader")) {// 负责
			if (status.indexOf("0") != -1)
				order_by = " order by plantime ";
			if (status.indexOf("1") != -1)
				order_by = " order by plantime ";
			if (status.indexOf("2") != -1)
				order_by = " order by committime desc ";
			if (status.indexOf("3") != -1)
				order_by = " order by committime desc ";
			if (status.indexOf("4") != -1)
				order_by = " order by committime desc ";
		}

		//String sql = "SELECT * FROM v_h5_work_task_info i where " + where_definition + order_by + " LIMIT ?,?";
		String sql = "SELECT * FROM (SELECT "+
						 "t.id,"+
						 "t.id AS TK01,"+
						 "t.code AS TK02,"+
						 "t.title AS title,"+
						 "t.title AS TK04,"+
						 "t.uppercode AS uppercode,"+
						 "t.uppercode AS TK03,"+
						 "t.content AS TK05,"+
						 "t.leader AS leader,"+
						 "t.leader AS TK06,"+
						 "GET_DISPLAY_NAME_BY_ID(t.leader) AS TK06C,"+
						 "t.plantime AS plantime,"+
						 "t.plantime AS TK07,"+
						 "t.workload AS TK09,"+
						 "t.status AS status,"+
						 "t.status AS TK10,"+
						 "t.committime AS committime,"+
						 "t.committime AS TK12,"+
						 "t.publisher AS publisher,"+
						 "t.publisher AS TK13,"+
						 "GET_DISPLAY_NAME_BY_ID(t.publisher) AS TK13C,"+
						 "t.publishtime AS publishtime,"+
						 "t.publishtime AS TK14,"+
						 "f_task_workload_rate(t.id) AS TK15,"+
						 "f_task_workload_rate_4up(t.id) AS workload_rate,"+
						 "t.starttime AS starttime,"+
						 "t.datastatus AS datastatus,"+
						 "t.tasktype AS tasktype,"+
						 "t.efficiency AS efficiency,"+
						 "t.hoursnum AS hoursnum,"+
						 "t.remarks AS remarks,"+
						 "t.evaluate AS evaluate,"+
						 "t.evalscore AS evalscore,"+
						 "t.evaltime AS evaltime,"+
						 "t.exectime AS exectime,"+
						 "t.annex AS annex,"+
						 "f_store_paths(t.id, 0, 'OA/workTask') AS store_paths,"+
						 "f_store_paths( t.id, 1, 'OA/workTask') AS annex1,"+
						"(SELECT group_concat( c.ccno  SEPARATOR ',') FROM work_task_cc  c WHERE c.info_id  =  t.id ) AS  ccnos,"+
						"(SELECT group_concat(GET_DISPLAY_NAME_BY_ID  ( c.ccno ) SEPARATOR ',') FROM work_task_cc  c WHERE c.info_id  =  t.id ) AS  ccnames,"+
						 "b.projectcode AS projectcode,"+
						"EXISTS (SELECT 1 FROM work_task_info   u WHERE u.id  =  t.uppercode ) AS  exists_up FROM  work_task_info  t "+ 
						"LEFT JOIN  work_project_taskbind   b  ON b.taskcode  =  t.id) i where " + where_definition + order_by + " LIMIT ?,?";
		System.out.println(sql);

		long pageIndex = Long.valueOf(decryptedMap.get("strPageIndex").toString());
		long row_count = Long.valueOf(decryptedMap.get("strPageSize").toString());
		long offset = row_count * (pageIndex - 1);
		argsList.add(offset);
		argsList.add(row_count);
		System.out.println(argsList);

		return jdbcTemplate.queryForList(sql, argsList.toArray());
	}

	Map<String, Object> commitValidate(Long taskID) {
		// strTaskID=;remarks=;strPerCode=;

		Map<String, Object> returnMap = new HashMap<String, Object>();

		try {
			// 验证参数================================================================================
			WorkTaskInfo taskModel = workTaskInfoManager.findUniqueBy("id", taskID);
			if (taskModel == null) {
				logger.debug("任务执行操作-没有查询到任务信息");
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "查询不到任务数据");
				return returnMap;
			} else if (!taskModel.getStatus().equals("1")) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "该任务状态不是进行中或状态已改变");
				return returnMap;
			}

			List<PropertyFilter> propertyFilters = new ArrayList<PropertyFilter>();
			propertyFilters.add(new PropertyFilter("EQL_uppercode", taskID.toString()));
			propertyFilters.add(new PropertyFilter("INS_datastatus", "1"));
			propertyFilters.add(new PropertyFilter("INS_status", "0,1"));

			List<WorkTaskInfo> childList = workTaskInfoManager.find(propertyFilters);
			if (childList != null && childList.size() > 0) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "该任务下有子任务尚未提交");
				return returnMap;
			}

			// 返回======================================================================
			returnMap.put("bSuccess", "true");
			returnMap.put("strMsg", "验证成功");
		} catch (Exception e) {
			logger.error("任务提交操作-：" + e.getMessage() + "\r\n" + e.fillInStackTrace());
			returnMap.put("bSuccess", "false");
			returnMap.put("strMsg", "验证出错");
		}

		// System.out.println(returnMap);
		return returnMap;
	}

	/**
	 * 关闭任务。改状态，同时发消息。Bing 2017.11.4
	 * 
	 * @param taskModel
	 *            要关闭的任务
	 * @param operator
	 *            操作人
	 */
	public void closeTask(WorkTaskInfo taskModel, UserDTO operator) {
		if (taskModel == null)
			return;

		// 关闭状态为“未完成”（“已发布”或“进行中”）的任务
		if (!taskModel.getStatus().equals("0") && !taskModel.getStatus().equals("1"))
			return;

		taskModel.setStatus("3");
		taskModel.setCommittime(new Date());
		workTaskInfoManager.save(taskModel);

		// 发消息=========================================================
		String tenantId = tenantHolder.getTenantId();

		// 向任务负责人发送消息提醒
		// a) 标题：[任务标题]任务关闭通知； b) 内容：您负责的[任务标题]任务，由 发布人[发布人姓名]手动关闭，请查看。
		String bussinessId = taskModel.getId().toString();
		String sender = operator.getId();
		String title = "[" + taskModel.getTitle() + "]任务关闭通知";

		String receiver = taskModel.getLeader().toString();// 负责人
		String content = "您负责的[" + taskModel.getTitle() + "]任务，由[" + operator.getDisplayName() + "]手动关闭，请查看。";
		notificationConnector.send(bussinessId, tenantId, sender, receiver, title, content, MsgConstants.MSG_TYPE_TASK);

		// 向任务抄送人发送消息提醒
		// a) 标题：[任务标题]任务关闭通知； b) 内容：抄送您的[任务标题]任务，由 发布人[发布人姓名]手动关闭，请查看。
		List<WorkTaskCc> ccList = workTaskCcManager.findBy("workTaskInfo.id", taskModel.getId());
		for (WorkTaskCc cc : ccList) {
			receiver = cc.getCcno().toString();// 抄送人
			content = "抄送您的[" + taskModel.getTitle() + "]任务，由[" + operator.getDisplayName() + "]手动关闭，请查看。";
			notificationConnector.send(bussinessId, tenantId, sender, receiver, title, content,
					MsgConstants.MSG_TYPE_TASK);
		}
	}

	/**
	 * 关闭子任务。改状态，同时发消息。Bing 2017.11.4
	 * 
	 * @param upTask
	 *            要关闭的父任务
	 * @param operator
	 *            操作人
	 */
	public void closeTasksByUp(WorkTaskInfo upTask, UserDTO operator) {
		List<WorkTaskInfo> taskChildList = upTask.getWorkChildTaskInfoList();
		if (taskChildList == null)
			return;

		for (WorkTaskInfo taskChild : taskChildList) {
			// 关闭“未完成”的任务
			if (taskChild.getStatus().equals("0") || taskChild.getStatus().equals("1"))
				closeTask(taskChild, operator);

			// 向状态为“已完成”的下级任务的负责人发送消息提醒
			// a) 标题：上级任务[上级任务标题]关闭通知；
			// b) 内容：您负责的[子任务标题]任务的上级任务[上级任务标题]，由[操作人姓名]手动关闭，请查看。
			if (taskChild.getStatus().equals("2") || taskChild.getStatus().equals("3")
					|| taskChild.getStatus().equals("4")) {
				String tenantId = tenantHolder.getTenantId();
				String bussinessId = taskChild.getId().toString();
				String sender = operator.getId();
				String receiver = taskChild.getLeader().toString();// 负责人
				String title = "上级任务[" + upTask.getTitle() + "]关闭通知";
				String content = "您负责的[" + taskChild.getTitle() + "]任务的上级任务[" + upTask.getTitle() + "]，由["
						+ operator.getDisplayName() + "]手动关闭，请查看。";
				notificationConnector.send(bussinessId, tenantId, sender, receiver, title, content,
						MsgConstants.MSG_TYPE_TASK);
			}
		}
	}

	public void delTask(WorkTaskInfo taskModel, UserDTO operator) {
		if (taskModel == null)
			return;

		if (taskModel.getDatastatus().equals("2"))
			return;

		// 删除状态为“已发布/已关闭”的。如果不是这2个状态，直接返回。
		if (!taskModel.getStatus().equals("0") && !taskModel.getStatus().equals("3"))
			return;

		taskModel.setDatastatus("2");
		workTaskInfoManager.save(taskModel);

		// 发消息=========================================================
		// 给任务负责人发送消息
		// i. 标题：[任务标题]删除通知
		// ii. 内容：您负责的[任务标题]，发布人【发布人姓名】已删除。
		String bussinessId = taskModel.getId().toString();
		String tenantId = tenantHolder.getTenantId();
		String publisher = taskModel.getPublisher().toString();
		String leader = taskModel.getLeader().toString();
		String title = "[" + taskModel.getTitle() + "]删除通知";
		String content = "您负责的[" + taskModel.getTitle() + "]任务，[" + operator.getDisplayName() + "]已删除。";//
		notificationConnector.send(bussinessId, tenantId, publisher, leader, title, content,
				MsgConstants.MSG_TYPE_TASK);

		// 向任务抄送人发送消息提醒
		// a) [任务名称]任务删除通知； b) 内容：[项目发布人姓名]删除了抄送给您的[任务名称]任务。
		List<WorkTaskCc> ccList = workTaskCcManager.findBy("workTaskInfo.id", taskModel.getId());
		for (WorkTaskCc cc : ccList) {
			String ccno = cc.getCcno().toString();// 抄送人
			content = "[" + operator.getDisplayName() + "]删除了抄送给您的[" + taskModel.getTitle() + "]任务。";
			notificationConnector.send(bussinessId, tenantId, publisher, ccno, title, content,
					MsgConstants.MSG_TYPE_TASK);
		}
	}

	public void delTasksByUp(WorkTaskInfo upTask, UserDTO operator) {
		List<WorkTaskInfo> taskChildList = upTask.getWorkChildTaskInfoList();
		if (taskChildList == null)
			return;

		for (WorkTaskInfo taskChild : taskChildList) {
			delTask(taskChild, operator);
		}
	}

	// @Resource======================================================
	@Resource
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Resource
	public void setWorkTaskInfoManager(WorkTaskInfoManager workTaskInfoManager) {
		this.workTaskInfoManager = workTaskInfoManager;
	}

	@Resource
	public void setWorkTaskCcManager(WorkTaskCcManager workTaskCcManager) {
		this.workTaskCcManager = workTaskCcManager;
	}

	@Resource
	public void setTenantHolder(TenantHolder tenantHolder) {
		this.tenantHolder = tenantHolder;
	}

	@Resource
	public void setNotificationConnector(NotificationConnector notificationConnector) {
		this.notificationConnector = notificationConnector;
	}

	@Resource
	public void setFileUploadAPI(FileUploadAPI fileUploadAPI) {
		this.fileUploadAPI = fileUploadAPI;
	}

	@Resource
	public void setWorkProjectInfoManager(WorkProjectInfoManager workProjectInfoManager) {
		this.workProjectInfoManager = workProjectInfoManager;
	}

	@Resource
	public void setWorkProjectTaskbindManager(WorkProjectTaskbindManager workProjectTaskbindManager) {
		this.workProjectTaskbindManager = workProjectTaskbindManager;
	}

	@Resource
	public void setUserConnector(UserConnector userConnector) {
		this.userConnector = userConnector;
	}

}
