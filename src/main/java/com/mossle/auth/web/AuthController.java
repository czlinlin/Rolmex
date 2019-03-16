package com.mossle.auth.web;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Joiner;
import com.mchange.v2.cfg.PropertiesConfigSource.Parse;
import com.mossle.api.tenant.TenantHolder;
import com.mossle.api.user.PersonInfoDTO;
import com.mossle.auth.persistence.domain.AttendanceImportSet;
import com.mossle.auth.persistence.domain.AttendanceImportSetDTO;
import com.mossle.auth.persistence.domain.AttendanceRecords;
import com.mossle.auth.persistence.domain.AttendanceRecordsDTO;
import com.mossle.auth.persistence.domain.AuthOrgData;
import com.mossle.auth.persistence.domain.PersonAttendanceRecords;
import com.mossle.auth.persistence.domain.PersonAttendanceRecordsDTO;
import com.mossle.auth.persistence.domain.Shift;
import com.mossle.auth.persistence.domain.SpecialDate;
import com.mossle.auth.persistence.manager.AttendanceImportSetManager;
import com.mossle.auth.persistence.manager.AttendanceRecordsManager;
import com.mossle.auth.persistence.manager.AuthOrgDataManager;
import com.mossle.auth.persistence.manager.PersonAttendanceRecordsManager;
import com.mossle.auth.persistence.manager.ShiftManager;
import com.mossle.auth.persistence.manager.SpecialDateManager;
import com.mossle.auth.service.AuthService;
import com.mossle.core.annotation.Log;
import com.mossle.core.auth.CurrentUserHolder;
import com.mossle.core.id.IdGenerator;
import com.mossle.core.page.Page;
import com.mossle.core.query.PropertyFilter;
import com.mossle.core.query.PropertyFilterUtils;
import com.mossle.core.spring.MessageHelper;
import com.mossle.core.util.StringUtils;
import com.mossle.dict.persistence.domain.DictData;
import com.mossle.dict.persistence.domain.DictInfo;
import com.mossle.dict.persistence.domain.DictType;
import com.mossle.dict.persistence.manager.DictInfoManager;
import com.mossle.dict.persistence.manager.DictTypeManager;
import com.mossle.operation.persistence.domain.ApplyDTO;
import com.mossle.party.PartyConstants;
import com.mossle.party.persistence.domain.PartyEntity;
import com.mossle.party.persistence.manager.PartyEntityManager;
import com.mossle.party.service.ExcelService;
import com.mossle.project.persistence.domain.WorkProjectInfo;
import com.mossle.spi.auth.ResourcePublisher;
import com.mossle.user.persistence.domain.PersonAttendanceMachine;
import com.mossle.user.persistence.manager.PersonAttendanceMachineManager;
import com.mossle.user.web.PersonInfoController;
import com.mossle.util.DateUtil;

import org.apache.commons.io.filefilter.FalseFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@Component
@Path("auth")
@RequestMapping("auth")
public class AuthController {
	
	private static Logger logger = LoggerFactory.getLogger(PersonInfoController.class);
	private Map<String, String> aliasMap = new HashMap<String, String>();
	
    private AuthService authService;
    private ResourcePublisher resourcePublisher;
    private JdbcTemplate jdbcTemplate;
    private PartyEntityManager partyEntityManager;
    private AuthOrgDataManager authOrgDataManager;
    private IdGenerator idGenerator;
    private MessageHelper messageHelper;
    private DictTypeManager dictTypeManager;
    private DictInfoManager dictInfoManager;
    private TenantHolder tenantHolder;
    private CurrentUserHolder currentUserHolder;
    private ShiftManager shiftManager ;
    private AttendanceRecordsDTO attendanceRecordsDTO ;
    private AttendanceRecordsManager attendanceRecordsManager ;
    private PersonAttendanceRecordsManager personAttendanceRecordsManager;
    private SpecialDateManager specialDateManager;
    private AttendanceImportSetManager attendanceImportSetManager ;
    private PersonAttendanceMachineManager personAttendanceMachineManager ;
    @Autowired
    private ExcelService excelService;


    @RequestMapping("auth-list")
    public String list(Model model) throws Exception {
        String text = authService.doExport();
        model.addAttribute("text", text);

        return "auth/auth-list";
    }

    @RequestMapping("auth-save")
    public String save(@RequestParam("text") String text) {
        authService.doImport(text);
        resourcePublisher.publish();

        return "redirect:/auth/auth-list.do";
    }
    
    /**
     * 数据权限列表：查询已经设置数据权限的人员
     * @author lilei
     * @date 2018-05-15
     * **/
    @POST
    @RequestMapping("user-orgdata-list-i")
    public String datalist(@FormParam("iptName") String iptName,Model model) throws Exception{//,@RequestParam("id") Long id
    	String searchName="";
    	if(!StringUtils.isBlank(iptName))
    		searchName=iptName;
    	
    	String strSql="SELECT o.type,o.union_id,i.`NAME` FROM auth_orgdata o "
    					+"INNER JOIN party_entity i on o.union_id=i.ID "
    					+" INNER JOIN party_entity p on o.partyEntityID=p.ID "
    					+ "where type='1' ";
    	if(searchName.length()>0)
    		strSql+=" and i.`NAME`='"+searchName+"' ";
    	strSql+=" GROUP BY o.type,o.union_id,i.`NAME`;";
    	
    	    	
    	List<Map<String,Object>> mapList=jdbcTemplate.queryForList(strSql);
    	model.addAttribute("userList", mapList);
    	
    	model.addAttribute("paramname", searchName);
    	
    	return "auth/user-orgdata-list-i";
    }
    
    @RequestMapping("user-orgdata-input")
    public String orgDataInput(Model model) {
        return "auth/user-orgdata-input";
    }
    
    /**
     * 进入修改页面
     * */
    @RequestMapping("user-orgdata-input-i")
    public String orgDataInput_i(Model model,@RequestParam(value="id",required=false) Long id) {
    	if(id==null)
    		return "auth/user-orgdata-input-i";
    	
    	if(id<1)
    		return "auth/user-orgdata-input-i";
    	
    	PartyEntity partyEntity=partyEntityManager.findUniqueBy("id", id);
    	String name="";
    	Long idLong=0L;
    	if(partyEntity!=null){
    		idLong=id;
    		name=partyEntity.getName();
    	}
    	
    	model.addAttribute("id", idLong);
    	model.addAttribute("name",name);
    	
    	//List<AuthOrgData> dataList=authOrgDataManager.findBy("unionId", id);
    	//model.addAttribute("dataList",dataList);
    	
    	String strRootNode="";
    	String strSql="Select partyEntityID from auth_orgdata where type='2' and union_id="+id;
    	List<String> rootNodeIdList=jdbcTemplate.queryForList(strSql, String.class);
    	if(rootNodeIdList!=null&&rootNodeIdList.size()>0)
    		strRootNode=rootNodeIdList.get(0);
		
    	model.addAttribute("iptRootNode",strRootNode);
    	List<String> partyIdList=null;
		strSql="Select partyEntityID from auth_orgdata where type='1' and union_id="+id;
		partyIdList=jdbcTemplate.queryForList(strSql, String.class);
		
		StringBuffer strDataIds=new StringBuffer();
		for(String partyId:partyIdList){
			strDataIds.append(partyId+",");
		}
		strDataIds.substring(0, strDataIds.length()-1);
		model.addAttribute("dataIds",strDataIds);
		return "auth/user-orgdata-input-i";
    }
        
    /**
     * 数据权限的提交
     * **/
    @POST
    @RequestMapping("user-orgdata-input-save")
    public String OrgDataSave(
    		@RequestParam("ids") String ids,
    		@RequestParam(value="iptRootNode",required=false) String iptRootNode,
    		@RequestParam("iptDataIds") String dataIds,
    		RedirectAttributes redirectAttributes){
    	try {
    		if(StringUtils.isBlank(ids)){
        		messageHelper.addFlashMessage(redirectAttributes, "core.success.save", "未选择需要分配权限的用户");
            	return "redirect:user-orgdata-list-i.do";
        	}
        	if(StringUtils.isBlank(dataIds))
        	{
        		messageHelper.addFlashMessage(redirectAttributes, "core.success.save", "未选择需要分配的数据");
            	return "redirect:user-orgdata-list-i.do";
        	}
        	
        	authService.batchSaveAuthOrgData(ids,iptRootNode, dataIds);
        	messageHelper.addFlashMessage(redirectAttributes, "core.success.save", "保存成功");
        	return "redirect:user-orgdata-list-i.do";
		} catch (Exception e) {
			e.printStackTrace();
			messageHelper.addFlashMessage(redirectAttributes, "core.success.save", "保存异常");
        	return "redirect:user-orgdata-list-i.do";
		}
    }
    
    @RequestMapping("user-config-set")
    public String userConfigSet() {
    	
		return "auth/user-config-set";
    }
    
    //跳转 考勤-导入时间设置  列表页
    @RequestMapping("attendance-import-set")
    public String attendanceImportSet() {
    	
		return "auth/attendance-import-set";
    }
    
    
    //跳转 考勤-导入时间设置页面
    @RequestMapping("attendance-import-set-i")
    public String attendanceImportSetSub(Model model)  {
    	
    	List<AttendanceImportSet> list = attendanceImportSetManager.getAll();
    	if(list.size()>0){
    	Long idLong = list.get(0).getId();
    	
    	AttendanceImportSet attendanceImportSet = attendanceImportSetManager.get(idLong);

    	model.addAttribute("model",attendanceImportSet);}
    	
    	return "auth/attendance-import-set-i";
    }
    
    //跳转 考勤时间设置列表页
    @RequestMapping("attendance-records-time-set")
    public String attendanceRecordsTimeSet() {
    	
		return "auth/attendance-records-time-set";
    }
    
    //跳转考勤时间设置页面
    @RequestMapping("attendance-records-time-set-i")
    public String attendanceRecordsTimeSetSub(@ModelAttribute Page page,
    		@RequestParam Map<String, Object> parameterMap,
    		@RequestParam(value = "isSearch", required = false) String isSearch,
    		Model model) throws ParseException {
    	
    	Map<String, Object> map = this.convertAlias(parameterMap);
    	List<PropertyFilter> propertyFilters = PropertyFilter.buildFromMap(map);
    	 if (StringUtils.isBlank(isSearch)) {
             isSearch = "0";
         }
        
         String tenantId = tenantHolder.getTenantId();
         
         Map<String,Object>  mapResult= pagedQuery(page, propertyFilters);
         
         page =(Page)mapResult.get("page");
    	
    	model.addAttribute("page", page);
		return "auth/attendance-records-time-set-i";
    }
    

    private Map<String,Object> pagedQuery(Page page, List<PropertyFilter> propertyFilters) throws ParseException {

    	Map<String,Object> mapResult=new HashMap<String,Object>();
        String sqlPagedQuerySelect = "SELECT a.id,a.recordName,a.mondayShiftID,c.start_time AS mondayShiftStartTime,c.end_time as mondayShiftEndTime,a.tuesdayShiftID,d.start_time AS tuesdayStartTime,d.end_time AS tuesdayEndTime"
                + ",a.wednesdayShiftID,e.start_time AS wednesdayStartTime,e.end_time AS wednesdayEndTime,a.thursdayShiftID,f.start_time AS thursdayStartTime,f.end_time AS thursdayEndTime "
                + ",a.fridayShiftID,g.start_time AS fridayStartTime,g.end_time AS fridayEndTime,a.SaturdayShiftID,h.start_time AS SaturdayStartTime,h.end_time AS SaturdayEndTime"
                + ",a.SundayShiftID,i.start_time AS SundayStartTime,i.end_time AS SundayEndTime,b.personNum "
                + " FROM attendance_records a LEFT  JOIN"
                + " (SELECT a.id,COUNT(b.personID) as personNum FROM attendance_records a JOIN person_attendance_records b on a.id =  b.attendanceRecordsID group by a.id ) b ON a.id = b.id"
                + " LEFT JOIN shift c on a.mondayShiftID = c.id"
                + " LEFT JOIN shift d on a.tuesdayShiftID = d.id"
                + " LEFT JOIN shift e on a.wednesdayShiftID = e.id" 
                + " LEFT JOIN shift f on a.thursdayShiftID = f.id"
                + " LEFT JOIN shift g on a.fridayShiftID = g.id"
                + " LEFT JOIN shift h on a.SaturdayShiftID = h.id"
                + " LEFT JOIN shift i on a.SundayShiftID = i.id";
                
                
      
        String sqlPagedQueryCount = "SELECT COUNT(*)"
        + " FROM attendance_records a LEFT  JOIN"
        + "(SELECT a.id,COUNT(b.personID) as personNum FROM attendance_records a JOIN person_attendance_records b on a.id =  b.attendanceRecordsID group by a.id ) b ON a.id = b.id"
        + " LEFT JOIN shift c on a.mondayShiftID = c.id"
        + " LEFT JOIN shift d on a.tuesdayShiftID = d.id"
        + " LEFT JOIN shift e on a.wednesdayShiftID = e.id" 
        + " LEFT JOIN shift f on a.thursdayShiftID = f.id"
        + " LEFT JOIN shift g on a.fridayShiftID = g.id"
        + " LEFT JOIN shift h on a.SaturdayShiftID = h.id"
        + " LEFT JOIN shift i on a.SundayShiftID = i.id";


        StringBuilder buff = new StringBuilder();
        List<Object> paramList = new ArrayList<Object>(); 
        boolean checkWhere = sqlPagedQuerySelect.toLowerCase().indexOf("where") == -1;
        
        PropertyFilterUtils.buildConfigurations(propertyFilters, buff,paramList, checkWhere);
        String sql = buff.toString();
        String countSql = "";
        String selectSql = "";
        
        countSql = sqlPagedQueryCount + " " + sql;
        selectSql = sqlPagedQuerySelect + " " + sql + " ORDER BY a.id limit " + page.getStart() + "," + page.getPageSize();
       

        logger.debug("countSql : {}", countSql);
        logger.debug("selectSql : {}", selectSql);

        Object[] params = paramList.toArray();
        int totalCount = jdbcTemplate.queryForObject(countSql, Integer.class,
                params);
        List<Map<String, Object>> list = jdbcTemplate.queryForList(selectSql,
                params);

        page.setTotalCount(totalCount);
        page.setResult(list);
        mapResult.put("page", page);
      
        return mapResult;
    
    }
    
    
  //跳转【新增/编辑 考勤组】页面
    @RequestMapping("new-attendance-records-list")
    public String newAttendanceRecordsList(@RequestParam(value = "id", required = false) Long id,
    										@ModelAttribute Page page, Model model) {
    	
    	AttendanceRecords attendanceRecords = new AttendanceRecords();//考勤组表
    	List<PersonAttendanceRecords>	personAttendanceRecords =null; //人员和考勤组关联表
    	List<SpecialDate> specialDate = null;//  特殊日期表
    	
    	if(id!=null){
    		//取考勤组表
    		attendanceRecords =attendanceRecordsManager.get(id);
    		//翻译考勤组表的班次的时间
    		List<Map<String, Object>> attendShiftTImeList = attendanceShiftContent(id);
    		
    		//取 特殊日期表的内容
    		specialDate=specialDateManager.findBy("attendanceRecordID", id);
    		if(specialDate!=null&&specialDate.size()>0){
	    		List<Map<String, Object>> specialList = new ArrayList<Map<String, Object>>() ;
	    		int i = 1;
	    		for( SpecialDate sd:specialDate){
	    			Map<String, Object> map = new HashMap<String, Object>();
	    			map.put("speicalDate", sd.getSpecialDate().toString().substring(0, 10));
	    			map.put("speicalDateDate", sd.getSpecialDate());
	    		
	    			//翻译特殊日期表的班次的时间
	    			String shiftTime = "";
	    			if(!sd.getShiftID().equals("")){
	    			Map<String, Object> shiftTimeMap = specialShiftContent(sd.getShiftID());
	    			if(shiftTimeMap.get("start_time")!=null&&shiftTimeMap.get("end_time")!=null){
	    			 shiftTime = shiftTimeMap.get("start_time")+"-"+ shiftTimeMap.get("end_time");
	    			}
	    			map.put("specialShiftTime", shiftTime);
	    			}else {
	    				map.put("specialShiftTime", "");
					}
	    			map.put("specialDateID", sd.getId().toString());
	    			map.put("shiftID", sd.getShiftID());
	    			map.put("i", Integer.toString(i));
	    			specialList.add(map);
	    			i+=1;
	    		} 
	    		model.addAttribute("specialList", specialList);
	    		int totalNum = specialList.size();
	    		int hidNum = specialList.size()+1;
	    		model.addAttribute("hidNum", hidNum);
	    		model.addAttribute("totalNum", totalNum);
    		}
    		
    		//取 人员和考勤组关联表
    		personAttendanceRecords = personAttendanceRecordsManager.findBy("attendanceRecordsID", id);
    		String personAttendanceIDString ="";
    		String personAttendanceNameString ="";
    		for(PersonAttendanceRecords p : personAttendanceRecords){
    			personAttendanceIDString += p.getPersonID()+",";
    			personAttendanceNameString += p.getPersonName()+",";
    		}
    		if(personAttendanceIDString!=null&&personAttendanceIDString.length()>0){
	    		personAttendanceIDString = personAttendanceIDString.substring(0, personAttendanceIDString.length()-1);
	    		personAttendanceNameString = personAttendanceNameString.substring(0, personAttendanceNameString.length()-1);
	    		model.addAttribute("personAttendanceIDString", personAttendanceIDString);
	    		model.addAttribute("personAttendanceNameString", personAttendanceNameString);
    		}
    		model.addAttribute("attendanceRecords", attendanceRecords);
    		model.addAttribute("attendShiftTImeList", attendShiftTImeList);	
    	}

		return "auth/new-attendance-records-list";
    }
    
    //读取班次表
    @POST
    @Path("shift-list")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Shift> shiftList() {
    	
         List<Shift>  shift =  shiftManager.getAll();
        
		return shift;
    }
    
  //查看考勤组人员
    @POST
    @Path("attend-person-list")
    @Produces(MediaType.APPLICATION_JSON)
    public List<PersonAttendanceRecords> attendPersonList(@FormParam(value = "id" ) Long id) {
    	
    	List<PersonAttendanceRecords>personAttendanceRecords = personAttendanceRecordsManager.findBy("attendanceRecordsID", id);
        
        
		return personAttendanceRecords;
    }
    
    
    //新增考勤组
    @RequestMapping("attendanceRecordSave")
    public String attendanceRecordSave(HttpServletRequest request,
    		@ModelAttribute AttendanceRecordsDTO attendanceRecordsDTO,
    		@RequestParam(value = "attendance_records_person_id" ) String attendance_records_person_id,
    		@RequestParam(value = "personAttendanceNameString" ) String personAttendanceNameString,
    		//@RequestParam(value = "hidNum" ) String hidNum,
    		
    		Model model) throws Exception {	
    	//保存 考勤组表
    	
    	AttendanceRecords attendanceRecords = new AttendanceRecords();
    	attendanceRecords.setMondayShiftID(attendanceRecordsDTO.getMondayShiftID());
    	attendanceRecords.setThursdayShiftID(attendanceRecordsDTO.getThursdayShiftID());
    	attendanceRecords.setWednesdayShiftID(attendanceRecordsDTO.getWednesdayShiftID());
    	attendanceRecords.setTuesdayShiftID(attendanceRecordsDTO.getTuesdayShiftID());
    	attendanceRecords.setFridayShiftID(attendanceRecordsDTO.getFridayShiftID());
    	attendanceRecords.setSaturdayShiftID(attendanceRecordsDTO.getSaturdayShiftID());
    	attendanceRecords.setSundayShiftID(attendanceRecordsDTO.getSundayShiftID());
    	attendanceRecords.setRecordName(attendanceRecordsDTO.getRecordName());
    	
    	attendanceRecordsManager.save(attendanceRecords);
    	
    	//保存 人员和考勤组关联表
    	Long attendance_records_id = attendanceRecords.getId();//取考勤组id
    	String[] personArray = attendance_records_person_id.split(",");//取人员id
    	String[] personNameArray = personAttendanceNameString.split(",");//取人员id
    	//存入 人员和考勤组关联表
    	for(int i=0;i<personArray.length;i++){
    		
    		PersonAttendanceRecords	personAttendanceRecords = new PersonAttendanceRecords();
    		personAttendanceRecords.setAttendanceRecordsID(attendance_records_id);
    		personAttendanceRecords.setPersonID(Long.parseLong(personArray[i]));
    		if(i<personNameArray.length){
        		personAttendanceRecords.setPersonName(personNameArray[i]==null?"":personNameArray[i]);
        		}else {
        			personAttendanceRecords.setPersonName("");
    			}
    		
    		personAttendanceRecordsManager.save(personAttendanceRecords);
    	}
    	
    	Long hidNum =Long.parseLong( request.getParameter("hidNum"));
    	//存入  特殊日期表
    	for(int i=1;i<hidNum;i++){
    		
    	String specialTablehidden =	request.getParameter("specialTablehidden"+i);
    	if(specialTablehidden!=null&&specialTablehidden.length()>0)	{
    	
    		String[] SpecialDateContent = specialTablehidden.split(",");
    		SpecialDate specialDate = new SpecialDate();
    		specialDate.setAttendanceRecordID(attendance_records_id);//  考勤组ID
    		if(SpecialDateContent.length>1&&!SpecialDateContent[1].equals("")){
    		specialDate.setShiftID(SpecialDateContent[1]);//班次id
    		}else {
    			specialDate.setShiftID("");
			}
    		
    		//特殊日期
    		DateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");   
    		specialDate.setSpecialDate(format1.parse(SpecialDateContent[0]));
    		
    		specialDateManager.save(specialDate);
    	}	
    		
    	}
    		
    	return "redirect:/auth/attendance-records-time-set-i.do";
    	
    }
    
    
    //修改考勤组规则
    @RequestMapping("attendanceRecordUpdate")
    public String attendanceRecordUpdate(@RequestParam(value = "id", required = false) Long id,
    		HttpServletRequest request,
    		@ModelAttribute AttendanceRecordsDTO attendanceRecordsDTO,
    		@RequestParam(value = "attendance_records_person_id" ) String attendance_records_person_id,
    		@RequestParam(value = "personAttendanceNameString" ) String personAttendanceNameString,
    		//@RequestParam(value = "hidNum" ) String hidNum,
    		
    		Model model) throws Exception {	
    	
    	//保存 考勤组表
    	AttendanceRecords attendanceRecords = attendanceRecordsManager.get(id);
    	attendanceRecords.setMondayShiftID(attendanceRecordsDTO.getMondayShiftID());
    	attendanceRecords.setThursdayShiftID(attendanceRecordsDTO.getThursdayShiftID());
    	attendanceRecords.setWednesdayShiftID(attendanceRecordsDTO.getWednesdayShiftID());
    	attendanceRecords.setTuesdayShiftID(attendanceRecordsDTO.getTuesdayShiftID());
    	attendanceRecords.setFridayShiftID(attendanceRecordsDTO.getFridayShiftID());
    	attendanceRecords.setSaturdayShiftID(attendanceRecordsDTO.getSaturdayShiftID());
    	attendanceRecords.setSundayShiftID(attendanceRecordsDTO.getSundayShiftID());
    	attendanceRecords.setRecordName(attendanceRecordsDTO.getRecordName());
    	attendanceRecordsManager.save(attendanceRecords);
    	
    	
    	//保存 人员和考勤组关联表
    	Long attendance_records_id = attendanceRecords.getId();//取考勤组id
    	String[] personArray = attendance_records_person_id.split(",");//取人员id
    	String[] personNameArray = personAttendanceNameString.split(",");//取人员id
    	//人员  更新之前先删除已存在的
    	List<PersonAttendanceRecords>	personAttendanceRecordsList =
    	personAttendanceRecordsManager.findBy("attendanceRecordsID", attendance_records_id); 
    	if(personAttendanceRecordsList!=null && personAttendanceRecordsList.size()>0){
    		personAttendanceRecordsManager.removeAll(personAttendanceRecordsList);
    	}
    	//存入 人员和考勤组关联表
    	for(int i=0;i<personArray.length;i++){
    		PersonAttendanceRecords	personAttendanceRecords = new PersonAttendanceRecords();
    		personAttendanceRecords.setAttendanceRecordsID(attendance_records_id);
    		personAttendanceRecords.setPersonID(Long.parseLong(personArray[i]));
    		if(i<personNameArray.length){
    		personAttendanceRecords.setPersonName(personNameArray[i]==null?"":personNameArray[i]);
    		}else {
    			personAttendanceRecords.setPersonName("");
			}
    		personAttendanceRecordsManager.save(personAttendanceRecords);
    	}
    	
    	
    	
    	//存入  特殊日期表
    	List<SpecialDate> specialDateList = specialDateManager.findBy("attendanceRecordID", id);
    	
    	if(specialDateList!=null&&specialDateList.size()>0){
    		specialDateManager.removeAll(specialDateList);
    	}
    	
    	Long hidNum =Long.parseLong( request.getParameter("hidNum"));
    	
    	for(int i=1;i<=hidNum;i++){
	    	String specialTablehidden =	request.getParameter("specialTablehidden"+i);
	    	if(specialTablehidden!=null&&specialTablehidden.length()>0)	{
	    		String[] SpecialDateContent = specialTablehidden.split(",");
	    		SpecialDate specialDate = new SpecialDate();
	    		specialDate.setAttendanceRecordID(attendance_records_id);//  考勤组ID
	    		specialDate.setShiftID(SpecialDateContent[1]);//班次id
	    		//翻译 特殊日期
	    		DateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");   
	    		specialDate.setSpecialDate(format1.parse(SpecialDateContent[0]));
	    		//存入 特殊日期表
	    		specialDateManager.save(specialDate);
	    	}	
    	}
    	return "redirect:/auth/attendance-records-time-set-i.do";
    }
    
    
    //删除考勤组
    @RequestMapping("delete-attendance-record")
    public String deleteAttendanceRecord(@RequestParam(value = "id", required = false) Long id
    		) throws Exception {	
    	
    	
    	AttendanceRecords attendanceRecords = attendanceRecordsManager.get(id);
    	if(attendanceRecords!=null){
    		attendanceRecordsManager.remove(attendanceRecords);
    	}
    	
    	//  删除人员
    	List<PersonAttendanceRecords>	personAttendanceRecordsList =
    	personAttendanceRecordsManager.findBy("attendanceRecordsID", id); 
    	if(personAttendanceRecordsList!=null && personAttendanceRecordsList.size()>0){
    		personAttendanceRecordsManager.removeAll(personAttendanceRecordsList);
    	}
    	
    	//删除 特殊日期
    	List<SpecialDate> specialDateList = specialDateManager.findBy("attendanceRecordID", id);
    	
    	if(specialDateList!=null&&specialDateList.size()>0){
    		specialDateManager.removeAll(specialDateList);
    	}
    	
    	
    	return "redirect:/auth/attendance-records-time-set-i.do";
    	
    }
    
    
    
    
    //保存 考勤-导入 设置  
    @RequestMapping("attendanceImportSetSave")
    public String attendanceImportSetSave(HttpServletRequest request,
    		@ModelAttribute AttendanceImportSetDTO attendanceImportSetDTO,
    		@RequestParam(value = "cutoffdata" ) String cutoffdata,
    		@RequestParam(value = "startdata") String startdata,
    		@RequestParam(value = "enddata") String enddata,
    		RedirectAttributes redirectAttributes,
    		//@RequestParam(value = "hidNum" ) String hidNum,
    		
    		Model model) throws Exception {	
  

    	SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    	
    	attendanceImportSetManager.removeAll();
    	
    	if(cutoffdata!=null&&cutoffdata.length()>0){
	    	AttendanceImportSet attendanceImportSet = new AttendanceImportSet();
	    	attendanceImportSet.setCutOffData(cutoffdata);
	    	
	    	if(startdata!=null&&startdata.length()>0){
	    	attendanceImportSet.setStartData(formatter.parse(startdata));
	    	}
	    	if(enddata!=null&&enddata.length()>0){
	    		attendanceImportSet.setEndData(formatter.parse(enddata));
		    }
	    	
	    	attendanceImportSetManager.save(attendanceImportSet);
    	}
    	messageHelper.addFlashMessage(redirectAttributes, "core.success.save", "设置成功");
        return "redirect:/auth/attendance-import-set-i.do";
    	 
    	
    }
    
 
    //取 考勤组表 周一至周日的班次时间
   public List<Map<String, Object>> attendanceShiftContent (Long id) {
	   
	   if(id!=null){
   		String sqlString="SELECT a.id,a.recordName,a.mondayShiftID,c.shift_name,c.start_time AS mondayShiftStartTime,c.end_time as mondayShiftEndTime,a.tuesdayShiftID,d.shift_name,d.start_time AS tuesdayStartTime,d.end_time AS tuesdayEndTime"
                + ",a.wednesdayShiftID,e.shift_name,e.start_time AS wednesdayStartTime,e.end_time AS wednesdayEndTime,a.thursdayShiftID,f.shift_name,f.start_time AS thursdayStartTime,f.end_time AS thursdayEndTime "
                + ",a.fridayShiftID,g.shift_name,g.start_time AS fridayStartTime,g.end_time AS fridayEndTime,a.SaturdayShiftID,h.shift_name,h.start_time AS SaturdayStartTime,h.end_time AS SaturdayEndTime"
                + ",a.SundayShiftID,i.start_time AS SundayStartTime,i.end_time AS SundayEndTime"
                + " FROM attendance_records a "
                + " LEFT JOIN shift c on a.mondayShiftID = c.id"
                + " LEFT JOIN shift d on a.tuesdayShiftID = d.id"
                + " LEFT JOIN shift e on a.wednesdayShiftID = e.id"  
                + " LEFT JOIN shift f on a.thursdayShiftID = f.id"
                + " LEFT JOIN shift g on a.fridayShiftID = g.id"
                + " LEFT JOIN shift h on a.SaturdayShiftID = h.id"
                + " LEFT JOIN shift i on a.SundayShiftID = i.id"
                + " WHERE a.id="+id;
                
   				

   		List<Map<String, Object>> list = jdbcTemplate.queryForList(sqlString);
   		
   		return list;
   	}else {
   		return null;
	}
} 
    
 //取 考勤组表 周一至周日的班次时间
   public Map<String, Object> specialShiftContent (String shiftID) {
	   
	   if(shiftID!=null){
   		String sqlString="SELECT start_time,end_time  FROM shift WHERE id="+shiftID;
   		Map<String, Object> list = jdbcTemplate.queryForMap(sqlString);
   		
   		return list;
   	}else {
   		return null;
	}
} 
    
    
    @RequestMapping("user-config-set-i")
    public String userConfigSet_i(Model model) {
    	
    	String strOpenStatus="1";
    	String hql="from DictInfo where dictType.name=?";
    	DictInfo dictInfo=dictInfoManager.findUnique(hql, "personMasterAudit");//dictInfoManager.findUniqueBy("dictType.name", "personMasterAudit");
    	if(dictInfo==null)
    	{
    		DictType dictType=new DictType();
    		
    		String tenantId = tenantHolder.getTenantId();
    		//dictType.setId(idGenerator.generateId());
    		dictType.setName("personMasterAudit");
    		dictType.setType("string");
    		dictType.setDescn("控制人事流程开启");
    		dictType.setTenantId(tenantId);
    		dictTypeManager.save(dictType);
    		
    		dictInfo=new DictInfo();
    		dictInfo.setDictType(dictType);
    		dictInfo.setValue("1");
    		//dictInfo.setId(idGenerator.generateId());
    		dictInfo.setPriority(1);
    		dictInfo.setTenantId(tenantId);
    		dictInfo.setName("控制人事流程开启/关闭");
    		dictInfoManager.save(dictInfo);
    	}
    	else {
    		strOpenStatus=dictInfo.getValue();
		}
    	
    	model.addAttribute("status", strOpenStatus);
    	
    	//控制是否导入
    	String isImport="0";
    	hql="from DictInfo where dictType.name=?";
    	dictInfo=dictInfoManager.findUnique(hql, "personDataWrokNumberImport");
    	if(dictInfo!=null)
    		isImport=dictInfo.getValue();
		model.addAttribute("isImport", isImport);
		
		//控制人事验证
		String isValidate="1";
		hql="from DictInfo where dictType.name=?";
		dictInfo=dictInfoManager.findUnique(hql, "personAddAndUPdateValidate");
    	if(dictInfo!=null)
    		isValidate=dictInfo.getValue();
		model.addAttribute("isValidate", isValidate);
		
		//控制开启“别名”功能
		String isOtherNameOpen="0";
		hql="from DictInfo where dictType.name=?";
		dictInfo=dictInfoManager.findUnique(hql, "isOpenOtherName");
    	if(dictInfo!=null)
    		isOtherNameOpen=dictInfo.getValue();
		model.addAttribute("isOtherNameOpen", isOtherNameOpen);
		
		model.addAttribute("isOpenOtherNameOpter", currentUserHolder.getUserId().equals("2")?"1":"0");
		
		return "auth/user-config-set-i";
    }
    
    /*@RequestMapping("shift-manage")
    public String shiftManage1(){
    	return "auth/timeSet-shiftManage";
    }*/
    /**
     * 返回时间设置-班次管理
     * @return
     */
    @RequestMapping("timeSet-shiftManage-i")
    public String shiftManage(Page page,Model model,String shiftName,Map<String, Object> parameterMap){
    	List<PropertyFilter> propertyFilters = PropertyFilter
                .buildFromMap(parameterMap);
    	propertyFilters.add(new PropertyFilter("EQI_delStatus","0"));
    	if(shiftName!=null&&!shiftName.equals("")){
    		propertyFilters.add(new PropertyFilter("LIKES_shiftName",shiftName));
    	}
    	page = shiftManager.pagedQuery(page, propertyFilters);
    	model.addAttribute("page", page);
    	return "auth/timeSet-shiftManage-i";
    }
    /**
     * 删除班次
     * @return
     */
    @RequestMapping("remove-shift-i")
    public String removeShift(Long id,Model model,RedirectAttributes redirectAttributes){
    	String hql = "update Shift set delStatus=1 where id=?";
    	int result = shiftManager.batchUpdate(hql, id);
    	if(result>0){
    		messageHelper.addFlashMessage(redirectAttributes,
                    "core.success.delete", "删除成功");
    	}
    	return "redirect:/auth/timeSet-shiftManage-i.do";
    }
    /**
     * 新增班次
     * @return
     */
    @RequestMapping("new-shift-i")
    public String newShift(Shift shift,Model model){
    	List<String> listH = new ArrayList<String>();
    	List<String> listM = new ArrayList<String>();
    	for(int i = 0;i<=23;i++){
    		if(i<10){
    			listH.add("0"+i);
    		}else{
    			listH.add(String.valueOf(i));
    		}
    	}
    	for(int j = 0;j<60;j++){
    		if(j<10){
    			listM.add("0"+j);
    		}else{
    			listM.add(String.valueOf(j));
    		}
    	}
    	model.addAttribute("hour", listH);
    	model.addAttribute("minute", listM);
    	return "auth/timeSet-new-shiftManage-i";
    }
    /**
     * 保存班次
     * @return
     */
    @RequestMapping("save-shift-i")
    public String saveShift(Long id,String shiftName,String startTimeH,String startTimeM,String endTimeH,String endTimeM,String restStartTimeH,String restStartTimeM,String restEndTimeH,String restEndTimeM,RedirectAttributes redirectAttributes){
    	if(id != null && id != 0){//编辑更新
    		Shift shift = shiftManager.get(id);
    		shift.setShiftName(shiftName);
        	shift.setStartTime(startTimeH+":"+startTimeM);
        	shift.setEndTime(endTimeH+":"+endTimeM);
        	shift.setRestStartTime(restStartTimeH+":"+restStartTimeM);
        	shift.setRestEndTime(restEndTimeH+":"+restEndTimeM);
        	shiftManager.save(shift);
    	}else{//新增
    		Shift shift = new Shift();
        	shift.setShiftName(shiftName);
        	shift.setStartTime(startTimeH+":"+startTimeM);
        	shift.setEndTime(endTimeH+":"+endTimeM);
        	shift.setRestStartTime(restStartTimeH+":"+restStartTimeM);
        	shift.setRestEndTime(restEndTimeH+":"+restEndTimeM);
        	shiftManager.insert(shift);
    	}
    	messageHelper.addFlashMessage(redirectAttributes,
                "core.success.save", "保存成功");
    	return "redirect:/auth/timeSet-shiftManage-i.do";
    }
    /**
     * 编辑更新班次
     * @return
     */
    @RequestMapping("update-shift-i")
    public String updateShift(Long id,Model model,RedirectAttributes redirectAttributes){
    	if(id != null){
    		Shift shift = shiftManager.get(id);
    		model.addAttribute("shift", shift);
    	}
    	List<String> listH = new ArrayList<String>();
    	List<String> listM = new ArrayList<String>();
    	for(int i = 1;i<=24;i++){
    		if(i<10){
    			listH.add("0"+i);
    		}else{
    			listH.add(String.valueOf(i));
    		}
    	}
    	for(int j = 0;j<60;j++){
    		if(j<10){
    			listM.add("0"+j);
    		}else{
    			listM.add(String.valueOf(j));
    		}
    	}
    	model.addAttribute("hour", listH);
    	model.addAttribute("minute", listM);
    	return "auth/timeSet-new-shiftManage-i";
    }
    protected Map<String, Object> convertAlias(Map<String, Object> parameters) {
        logger.debug("parameters : {}", parameters);

        Map<String, Object> parameterMap = new HashMap<String, Object>();

        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            String key = entry.getKey();

            for (Map.Entry<String, String> aliasEntry : aliasMap.entrySet()) {
                String aliasKey = "_" + aliasEntry.getKey();
                String aliasValue = "_" + aliasEntry.getValue();

                if (key.indexOf(aliasKey) != -1) {
                    key = key.replace(aliasKey, aliasValue);

                    break;
                }
            }

            parameterMap.put(key, entry.getValue());
        }

        logger.debug("parameterMap : {}", parameterMap);

        return parameterMap;
    }
    
    /**
     * 跳转到excel导入页面    ckx  2018/08/13
     * @return
     */
    @RequestMapping("attendance-import-excel")
    public String attendanceImportExcel() {
    	
		return "auth/attendance-import-excel";
    }
    
    
    /**
     * 考勤导入页面   ckx  2018/08/13
     * @param model
     * @return
     * @throws ParseException
     */
    @RequestMapping("attendance-import-excel-i")
    public String attendanceImportExcelSub(Model model) throws ParseException  {
    	
    	boolean boo = true;
    	//查询导入设置
		Map<String, Object> importSettingMap = null;
		try {
			importSettingMap = jdbcTemplate.queryForMap("select cutoffdata,startdata,enddata from oa_ba_attendance_importSet ");
		} catch (DataAccessException e) {
		}
		if(null != importSettingMap){
			//截止日期
			Object cutoffdata = importSettingMap.get("cutoffdata");
			//起始时间
			Object startdata = importSettingMap.get("startdata");
			//结束时间
			Object enddata = importSettingMap.get("enddata");
			if(null != startdata && !"".equals(startdata) && !"null".equals(startdata) && null != enddata && !"".equals(enddata) && !"null".equals(enddata)){
				String dateStr = DateUtil.formatDate(new Date(), "yyyy-MM-dd");
				boolean comparDate = DateUtil.comparDate(startdata.toString(), dateStr);
				if (comparDate) {
					if(DateUtil.comparDate(dateStr, enddata.toString())){
						
						boo = true;
						
					}else{
						boo = false;
					}
				}else{
					boo = false;
				}
			}
		}
		model.addAttribute("ifSetting", boo);
		
    	return "auth/attendance-import-excel-i";
    }
    
    
    @RequestMapping("personAlias")
    public String personAlias(String userId,RedirectAttributes redirectAttributes) {
    	String sql = "update PersonAttendanceMachine p set p.is_modify=? where p.personId in("+userId+")";
    	int result = personAttendanceMachineManager.batchUpdate(sql, "0");
    	if(result > 0){
    		messageHelper.addFlashMessage(redirectAttributes,
                    "core.success.save", "保存成功");
    	}else{
    		messageHelper.addFlashMessage(redirectAttributes,
                    "core.success.save", "已开启别名功能，无需重复操作。");
    	}
    	return "redirect:/auth/user-config-set-i.do";
    }
    
    
    // ~ ======================================================================
    @Resource
    public void setAuthService(AuthService authService) {
        this.authService = authService;
    }

    @Resource
    public void setResourcePublisher(ResourcePublisher resourcePublisher) {
        this.resourcePublisher = resourcePublisher;
    }
    
    /*private JdbcTemplate jdbcTemplate;
    private PartyEntityManager partyEntityManager;
    private AuthOrgDataManager authOrgDataManager;*/
    @Resource
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    

    
    @Resource
    public void setPartyEntityManager(PartyEntityManager partyEntityManager) {
        this.partyEntityManager = partyEntityManager;
    }
    
    @Resource
    public void setAuthOrgDataManager(AuthOrgDataManager authOrgDataManager) {
        this.authOrgDataManager = authOrgDataManager;
    }
    
    @Resource
    public void setMessageHelper(MessageHelper messageHelper) {
        this.messageHelper = messageHelper;
    }
    
    @Resource
    public void setIdGenerator(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }
    
    @Resource
    public void setDictTypeManager(DictTypeManager dictTypeManager) {
        this.dictTypeManager = dictTypeManager;
    }
    
    @Resource
    public void setDictInfoManager(DictInfoManager dictInfoManager) {
        this.dictInfoManager = dictInfoManager;
    }
    
    @Resource
    public void setTenantHolder(TenantHolder tenantHolder) {
        this.tenantHolder = tenantHolder;
    }
    
    @Resource
    public void setCurrentUserHolder(CurrentUserHolder currentUserHolder) {
        this.currentUserHolder = currentUserHolder;
    }
	@Resource
	public void setAttendanceRecordsManager(
			AttendanceRecordsManager attendanceRecordsManager) {
		this.attendanceRecordsManager = attendanceRecordsManager;
	}

	@Resource
	public void setPersonAttendanceRecordsManager(
			PersonAttendanceRecordsManager personAttendanceRecordsManager) {
		this.personAttendanceRecordsManager = personAttendanceRecordsManager;
	}

	@Resource
	public void setSpecialDateManager(SpecialDateManager specialDateManager) {
		this.specialDateManager = specialDateManager;
	}

	@Resource
	public void setAttendanceImportSetManager(
			AttendanceImportSetManager attendanceImportSetManager) {
		this.attendanceImportSetManager = attendanceImportSetManager;
	}

    @Resource
    public void setShiftManager(ShiftManager shiftManager) {
    	this.shiftManager = shiftManager;
    }
    @Resource
    public void setPersonAttendanceMachineManager(PersonAttendanceMachineManager personAttendanceMachineManager) {
    	this.personAttendanceMachineManager = personAttendanceMachineManager;
    }

}
