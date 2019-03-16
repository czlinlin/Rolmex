package com.mossle.party.service;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.base.Joiner;
import com.mossle.common.utils.StringUtils;
import com.mossle.core.spring.MessageHelper;
import com.mossle.party.PartyConstants;
import com.mossle.party.persistence.domain.AttendanceEntity;
import com.mossle.party.persistence.domain.AttendanceEntityPojo;
import com.mossle.party.persistence.domain.PartyEntity;
import com.mossle.party.persistence.manager.AttendanceEntityManager;
import com.mossle.party.persistence.manager.PartyEntityManager;
import com.mossle.user.persistence.domain.PersonInfo;
import com.mossle.user.persistence.manager.PersonInfoManager;
import com.mossle.user.service.UserService;
import com.mossle.util.DateUtil;
import com.mossle.util.ExportUtil;
import com.mossle.util.StringUtil;


/**
 * 
 * @author ckx
 *
 */
@Service
@Transactional(readOnly = true)
public class ExcelService {

	private static Logger logger = LoggerFactory.getLogger(ExcelService.class);
	
	@Autowired
    private JdbcTemplate jdbcTemplate;
	@Autowired
	private MessageHelper messageHelper;
	@Autowired
	private AttendanceEntityManager attendanceEntityManager;
	@Autowired
	private PersonInfoManager personInfoManager;
	@Autowired
	private PartyEntityManager partyEntityManager;
	@Autowired
	private UserService userService;
	@Autowired
	private PartyService partyService;
	@Autowired
	private OrgService orgService;
	
	@Transactional(readOnly = false)
	public List<HashMap<String, Object>> importAttendance(List<Map<String, Object>> userList,HashSet<String> allSet,HashSet<String> failSet) throws ParseException{
		List<HashMap<String, Object>> failList = new ArrayList<HashMap<String,Object>>();
		ArrayList<AttendanceEntity> attendanceList = new ArrayList<AttendanceEntity>();
		
		for (Map<String, Object> map : userList) {
			
    		boolean b = true;
    		AttendanceEntity attendanceEntity = new AttendanceEntity();
    		
			String userCode = StringUtil.toString(map.get("0"));
			String userName = StringUtil.toString(map.get("1"));
			String departmentName = StringUtil.toString(map.get("2"));
			String machNo = StringUtil.toString(map.get("3")); //机器号
			String userNo = StringUtil.toString(map.get("4"));//员工流水号
			String dateTime = StringUtil.toString(map.get("5"));
			
			
			
			if("".equals(departmentName) && "".equals(userCode) && "".equals(userName) && "".equals(machNo) && "".equals(userNo) && "".equals(dateTime)){
				continue;
			}else if("".equals(machNo) || "".equals(userNo) || "".equals(dateTime)){
				allSet.add(machNo+userNo);
				HashMap<String,Object> failMap = new HashMap<String,Object>();
				failMap.put("userCode", userCode);
				failMap.put("userName", userName);
				failMap.put("departmentName", departmentName);
				failMap.put("machNo", machNo);
				failMap.put("userNo", userNo);
				failMap.put("dateTime", dateTime);
				failMap.put("failReason", "机器号，编号，打卡时间为空");
				failList.add(failMap);
				failSet.add(machNo+userNo);
				continue;
			}else{
				//去除开头的0
				machNo = machNo.replaceFirst("^0*", ""); 
				userNo = userNo.replaceFirst("^0*", ""); 
				allSet.add(machNo+userNo);
			}
			
			/*if("".equals(userCode) || "".equals(userName) || "".equals(dateTime)){
				break;
			}
			//当不足四位时前面补零
			if(userCode.length() < 4){
				userCode = String.format("%04d", Integer.parseInt(userCode));
			}*/
			//格式化年月日
			String workDateStr = DateUtil.formatTime(dateTime);
			String[] split = workDateStr.split("-");
			String year = split[0];
			String month = split[1];
			String day = split[2];
			//获取时间
			String time = DateUtil.getTime(dateTime);
		
			
			
			for (AttendanceEntity str : attendanceList) {
				//机器号
				String machNoStr = str.getMachNo();
				//员工流水号
				String userNoStr = str.getUserNo();
				//日期
				Date workDate = str.getWorkDate();
				//实际上班时间
				String goToWork = str.getGoToWork();
				//实际下班时间
				String goOffWork = str.getGoOffWork();
				String formatDate = DateUtil.formatDate(workDate, "yyyy-MM-dd");
				if(workDateStr.equals(formatDate) && machNo.equals(machNoStr) && userNo.equals(userNoStr)){
					b = false;
					if(null != goToWork && !"".equals(goToWork) && !"null".equals(goToWork)){
						//与之进行比较
						boolean booTo = DateUtil.comparTime(time, goToWork);
						if(booTo){
							str.setGoToWork(time);
							if(null != goOffWork && !"".equals(goOffWork) && !"null".equals(goOffWork)){
								//与之进行比较
								boolean booOff = DateUtil.comparTime(goOffWork,goToWork);
								if(booOff){
									str.setGoOffWork(goToWork);
								}
							}else{
								str.setGoOffWork(goToWork);
							}
						}else{
							if(null != goOffWork && !"".equals(goOffWork) && !"null".equals(goOffWork)){
								//与之进行比较
								boolean booOff = DateUtil.comparTime(goOffWork,time);
								if(booOff){
									str.setGoOffWork(time);
								}
							}else{
								str.setGoOffWork(time);
							}
						}
					}else{
						str.setGoToWork(time);
					}
				}
			}
			//此时集合里面没有当前时间的用户考勤记录
			if(b){
				//根据用户id获取部门id
				Map<String, Object> departmentMap = null;
				try {
					departmentMap = jdbcTemplate.queryForMap("select i.department_code,i.id,i.department_name from person_info i left join person_machine m on i.id = m.person_id where m.mach_no = '"+machNo+"' and m.user_no = '"+userNo+"'");
				} catch (DataAccessException e2) {
					HashMap<String,Object> failMap = new HashMap<String,Object>();
					failMap.put("userCode", userCode);
					failMap.put("userName", userName);
					failMap.put("departmentName", departmentName);
					failMap.put("machNo", machNo);
					failMap.put("userNo", userNo);
					failMap.put("dateTime", dateTime);
					failMap.put("failReason", "当前人员未绑定机器号，编号");
					failList.add(failMap);
					failSet.add(machNo+userNo);
				}
				if(null == departmentMap){
					continue;
				}
				String userId = StringUtil.toString(departmentMap.get("id"));
				attendanceEntity.setUserId(userId);
				attendanceEntity.setDepartmentId(StringUtil.toString(departmentMap.get("department_code")));
				attendanceEntity.setWorker(userName);
				attendanceEntity.setWorkDate(DateUtil.formatDateStr(workDateStr, "yyyy-MM-dd"));
				attendanceEntity.setGoToWork(time);
				attendanceEntity.setYear(year);
				attendanceEntity.setMonth(month);
				attendanceEntity.setDay(day);
				attendanceEntity.setSignIn("签到");
				attendanceEntity.setDepartmentName(StringUtil.toString(departmentMap.get("department_name")));
				attendanceEntity.setUserCode(userCode);
				attendanceEntity.setMachNo(machNo);
				attendanceEntity.setUserNo(userNo);
				
				//获取用户班次时间
				//获取星期
				String weekCS = DateUtil.getWeekCS(workDateStr);
				//获取字段
				String attendance = StringUtil.getAttendance(weekCS);
				
				Map<String, Object> queryForMapCount = jdbcTemplate.queryForMap("select count(*) count from attendance_records a LEFT JOIN person_attendance_records p ON a.id = p.attendanceRecordsID where p.personID = '"+userId+"'");
				Object objCount = queryForMapCount.get("count");
				int intCount = Integer.parseInt(objCount.toString());
				if(intCount > 1){
					HashMap<String,Object> failMap = new HashMap<String,Object>();
					failMap.put("userCode", userCode);
					failMap.put("userName", userName);
					failMap.put("departmentName", departmentName);
					failMap.put("machNo", machNo);
					failMap.put("userNo", userNo);
					failMap.put("dateTime", dateTime);
					failMap.put("failReason", "当前人员绑定了多个考勤组");
					failList.add(failMap);
				}
		    	Map<String, Object> queryForMapStart = null;
				try {
					queryForMapStart = jdbcTemplate.queryForMap("select s.start_time , s.end_time from shift s where s.id in (select a."+attendance+" from attendance_records a LEFT JOIN person_attendance_records p ON a.id = p.attendanceRecordsID where p.personID = '"+userId+"')");
				} catch (DataAccessException e) {
				}

				if(null != queryForMapStart){
					if(null != queryForMapStart.get("start_time") && !"".equals(queryForMapStart.get("start_time")) && !"null".equals(queryForMapStart.get("start_time"))){
						attendanceEntity.setConstraintToWork(StringUtil.toString(queryForMapStart.get("start_time")));
					}else{
						attendanceEntity.setConstraintToWork("");
					}

					if(null != queryForMapStart.get("end_time") && !"".equals(queryForMapStart.get("end_time")) && !"null".equals(queryForMapStart.get("end_time"))){
						attendanceEntity.setConstraintOffWork(StringUtil.toString(queryForMapStart.get("end_time")));
					}else{
						attendanceEntity.setConstraintOffWork("");
					}
					
				}else{
					attendanceEntity.setConstraintToWork("");
					attendanceEntity.setConstraintOffWork("");
				}
				//特殊考勤日期
				List<Map<String, Object>> queryForMapSpecialList = null;
				try {
					queryForMapSpecialList = jdbcTemplate.queryForList("select a.shiftID,a.specialDate from special_date a LEFT JOIN person_attendance_records p ON a.attendanceRecordsID = p.attendanceRecordsID where p.personID = '"+userId+"'");
				} catch (DataAccessException e1) {
				}
				if(null != queryForMapSpecialList && queryForMapSpecialList.size() > 0){
					for (Map<String, Object> map2 : queryForMapSpecialList) {
						Object shiftID = map2.get("shiftID");
						Object specialDate = map2.get("specialDate");
						
						Map<String, Object> queryForMap = null;
						try {
							queryForMap = jdbcTemplate.queryForMap("select s.start_time , s.end_time from shift s where s.id = '"+shiftID+"'");
						} catch (DataAccessException e) {
						}
						if(null != queryForMap){
							if(null != queryForMap.get("start_time") && !"".equals(queryForMap.get("start_time")) && !"null".equals(queryForMap.get("start_time"))){
								if(workDateStr.equals(specialDate)){
									attendanceEntity.setConstraintToWork(StringUtil.toString(queryForMap.get("start_time")));
								}
							}
							
							if(null != queryForMap.get("end_time") && !"".equals(queryForMap.get("end_time")) && !"null".equals(queryForMap.get("end_time"))){
								if(workDateStr.equals(specialDate)){
									attendanceEntity.setConstraintOffWork(StringUtil.toString(queryForMap.get("end_time")));
								}
							}
						}
						
					}
				}
				
		    	
				attendanceList.add(attendanceEntity);
			}
			
			
		}
		
		//保存考勤表数据
		if(null != attendanceList && attendanceList.size() > 0){
			//查询导入设置
    		Map<String, Object> importSettingMap = null;
			try {
				importSettingMap = jdbcTemplate.queryForMap("select cutoffdata,startdata,enddata from oa_ba_attendance_importSet ");
			} catch (DataAccessException e) {
			}
			if(null != importSettingMap){
				//截止日期
	    		Object cutoffdata = importSettingMap.get("cutoffdata");
	    		
	    		
	    		//获取当前的日期
				Calendar calendar = Calendar.getInstance();
				//当前年
				int yearInt = calendar.get(Calendar.YEAR);
				//上个月
				int monthInt = calendar.get(Calendar.MONTH);
				//当前日期
				int dateInt = calendar.get(Calendar.DATE);
				//设置的日期
				int cutoffdataInt = Integer.parseInt(cutoffdata.toString());
				
	    		
				for (AttendanceEntity attendanceEntity2 : attendanceList) {
					//考勤数据的月份
					String year = attendanceEntity2.getYear();
					String month = attendanceEntity2.getMonth();
	    			String day = attendanceEntity2.getDay();
	    			Date workDate = attendanceEntity2.getWorkDate();
	    			String formatDate = DateUtil.formatDate(workDate, "yyyy-MM-dd");
	    			
	    			//打卡时间
	    			//上班
	    			String goToWork = attendanceEntity2.getGoToWork();
	    			//下班
	    			String goOffWork = attendanceEntity2.getGoOffWork();
	    			String userCode = attendanceEntity2.getUserCode();
	    			String userName = attendanceEntity2.getWorker();
	    			String departmentName = attendanceEntity2.getDepartmentName();
	    			String machNo = attendanceEntity2.getMachNo();
	    			String userNo = attendanceEntity2.getUserNo();
	    			
	    			//上个月以前的数据不允许导入
					if(dateInt > cutoffdataInt){
						//同一年
	    				if(yearInt == Integer.parseInt(year)){
	    					if(monthInt >= Integer.parseInt(month)){
	    						failSet.add(machNo+userNo);
	    						if(null != goToWork && !"".equals(goToWork) && !"null".equals(goToWork)){
	    							HashMap<String,Object> failMap = new HashMap<String,Object>();
		    						failMap.put("userCode", userCode);
		    						failMap.put("userName", userName);
		    						failMap.put("departmentName", departmentName);
		    						failMap.put("machNo", machNo);
		    						failMap.put("userNo", userNo);
		    						failMap.put("dateTime", year+"/"+month+"/"+day+" "+goToWork);
		    						failMap.put("failReason", month+"月数据未导入，请查看导入设置");
		    						failList.add(failMap);
	    						}
	    						
	    						if(null != goOffWork && !"".equals(goOffWork) && !"null".equals(goOffWork)){
	    							HashMap<String,Object> failMap = new HashMap<String,Object>();
		    						failMap.put("userCode", userCode);
		    						failMap.put("userName", userName);
		    						failMap.put("departmentName", departmentName);
		    						failMap.put("machNo", machNo);
		    						failMap.put("userNo", userNo);
		    						failMap.put("dateTime", year+"/"+month+"/"+day+" "+goOffWork);
		    						failMap.put("failReason", month+"月数据未导入，请查看导入设置");
		    						failList.add(failMap);
	    						}
	    						
	    						
	    						
		    					continue;
		    				}else{
		    					try {
									jdbcTemplate.execute("delete from attendance where user_id = '"+attendanceEntity2.getUserId()+"' and work_date = '"+formatDate+"'");
								} catch (DataAccessException e) {
								}
		    					Map<String, Object> queryForMap = jdbcTemplate.queryForMap("select count(*) as count from attendance where user_id = '"+attendanceEntity2.getUserId()+"' and work_date = '"+formatDate+"'");
		    					Object count = queryForMap.get("count");
		    					if("0".equals(count.toString())){
		    						/*String goToWork = "";
		    						String goOffWork = "";
		    						if(null != attendanceEntity2.getGoToWork() && !"null".equals(attendanceEntity2.getGoToWork()) && !"null".equals(attendanceEntity2.getGoToWork())){
		    							goToWork = attendanceEntity2.getGoToWork();
		    						}
		    						if(null != attendanceEntity2.getGoOffWork() && !"null".equals(attendanceEntity2.getGoOffWork()) && !"null".equals(attendanceEntity2.getGoOffWork())){
		    							goOffWork = attendanceEntity2.getGoOffWork();
		    						}*/
		    						//jdbcTemplate.execute("insert into attendance (department_id,user_id,worker,work_date,go_to_work,go_off_work,year,month,day,constraint_to_work,constraint_off_work,sign_in,department_name,user_code) values ('"+attendanceEntity2.getDepartmentId()+"','"+attendanceEntity2.getUserId()+"','"+attendanceEntity2.getWorker()+"','"+formatDate+"','"+goToWork+"','"+goOffWork+"','"+attendanceEntity2.getYear()+"','"+attendanceEntity2.getMonth()+"','"+attendanceEntity2.getDay()+"','"+attendanceEntity2.getConstraintToWork()+"','"+attendanceEntity2.getConstraintOffWork()+"','"+attendanceEntity2.getSignIn()+"','"+attendanceEntity2.getDepartmentName()+"','"+attendanceEntity2.getUserCode()+"')");
		    						attendanceEntityManager.save(attendanceEntity2);
		    					}
		    				}
	    				}
	    			}else{
	    				try {
							jdbcTemplate.execute("delete from attendance where user_id = '"+attendanceEntity2.getUserId()+"' and work_date = '"+formatDate+"'");
						} catch (DataAccessException e) {
						}
	    				Map<String, Object> queryForMap = jdbcTemplate.queryForMap("select count(*) as count from attendance where user_id = '"+attendanceEntity2.getUserId()+"' and work_date = '"+formatDate+"'");
						Object count = queryForMap.get("count");
						if("0".equals(count.toString())){
							/*String goToWork = "";
							String goOffWork = "";
							if(null != attendanceEntity2.getGoToWork() && !"null".equals(attendanceEntity2.getGoToWork()) && !"null".equals(attendanceEntity2.getGoToWork())){
								goToWork = attendanceEntity2.getGoToWork();
							}
							if(null != attendanceEntity2.getGoOffWork() && !"null".equals(attendanceEntity2.getGoOffWork()) && !"null".equals(attendanceEntity2.getGoOffWork())){
								goOffWork = attendanceEntity2.getGoOffWork();
							}*/
							//jdbcTemplate.execute("insert into attendance (department_id,user_id,worker,work_date,go_to_work,go_off_work,year,month,day,constraint_to_work,constraint_off_work,sign_in,department_name,user_code) values ('"+attendanceEntity2.getDepartmentId()+"','"+attendanceEntity2.getUserId()+"','"+attendanceEntity2.getWorker()+"','"+formatDate+"','"+goToWork+"','"+goOffWork+"','"+attendanceEntity2.getYear()+"','"+attendanceEntity2.getMonth()+"','"+attendanceEntity2.getDay()+"','"+attendanceEntity2.getConstraintToWork()+"','"+attendanceEntity2.getConstraintOffWork()+"','"+attendanceEntity2.getSignIn()+"','"+attendanceEntity2.getDepartmentName()+"','"+attendanceEntity2.getUserCode()+"')");
							attendanceEntityManager.save(attendanceEntity2);
						}
	    			}
	    			
	    			
	    			
				}
			}else{
				StringBuffer stringBuffer = new StringBuffer();
				for (AttendanceEntity attendanceEntity3 : attendanceList) {
					Date workDate = attendanceEntity3.getWorkDate();
	    			String formatDate = DateUtil.formatDate(workDate, "yyyy-MM-dd");
	    			try {
						jdbcTemplate.execute("delete from attendance where user_id = '"+attendanceEntity3.getUserId()+"' and work_date = '"+formatDate+"'");
					} catch (DataAccessException e) {
					}
					Map<String, Object> queryForMap = jdbcTemplate.queryForMap("select count(*) as count from attendance where user_id = '"+attendanceEntity3.getUserId()+"' and work_date = '"+formatDate+"'");
					Object count = queryForMap.get("count");
					if("0".equals(count.toString())){
						/*String goToWork = "";
						String goOffWork = "";
						if(null != attendanceEntity3.getGoToWork() && !"null".equals(attendanceEntity3.getGoToWork()) && !"null".equals(attendanceEntity3.getGoToWork())){
							goToWork = attendanceEntity3.getGoToWork();
						}
						if(null != attendanceEntity3.getGoOffWork() && !"null".equals(attendanceEntity3.getGoOffWork()) && !"null".equals(attendanceEntity3.getGoOffWork())){
							goOffWork = attendanceEntity3.getGoOffWork();
						}*/
						//jdbcTemplate.execute("insert into attendance (department_id,user_id,worker,work_date,go_to_work,go_off_work,year,month,day,constraint_to_work,constraint_off_work,sign_in,department_name,user_code) values ('"+attendanceEntity3.getDepartmentId()+"','"+attendanceEntity3.getUserId()+"','"+attendanceEntity3.getWorker()+"','"+formatDate+"','"+goToWork+"','"+goOffWork+"','"+attendanceEntity3.getYear()+"','"+attendanceEntity3.getMonth()+"','"+attendanceEntity3.getDay()+"','"+attendanceEntity3.getConstraintToWork()+"','"+attendanceEntity3.getConstraintOffWork()+"','"+attendanceEntity3.getSignIn()+"','"+attendanceEntity3.getDepartmentName()+"','"+attendanceEntity3.getUserCode()+"');");
						attendanceEntityManager.save(attendanceEntity3);
					}
				}
			}
    		
			
			
		}
		return failList;
		
	}
	/**
	 * 导出考勤统计
	 * @param response
	 * @param request
	 * @param partyEntityId 组织机构id
	 * @param startTime 开始时间
	 * @param endTime 结束时间
	 * @throws Exception 
	 */
	public void exportAttendanceStatistics(HttpServletResponse response,
			HttpServletRequest request,String name, Long partyEntityId, String startTime,String endTime) throws Exception {
		//获取数据
		List<Map<String,Object>> resultData = orgService.getAttrndanceStatisticeList(name, partyEntityId, startTime, endTime);
		//导出数据
		ExportUtil.exportAttendanceStatistics(response,request,resultData);
		
	}
	
	
}
