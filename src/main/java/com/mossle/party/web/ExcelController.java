package com.mossle.party.web;

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
import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.alibaba.fastjson.JSONObject;
import com.mossle.common.utils.StringUtils;
import com.mossle.core.annotation.Log;
import com.mossle.core.excel.ReadExcel;
import com.mossle.core.page.Page;
import com.mossle.core.spring.MessageHelper;
import com.mossle.party.persistence.domain.AttendanceEntity;
import com.mossle.party.persistence.manager.AttendanceEntityManager;
import com.mossle.party.service.ExcelService;
import com.mossle.util.DateUtil;
import com.mossle.util.StringUtil;


@Component
@Controller
@RequestMapping("PartyExcelController")
@Path("PartyExcelController")
public class ExcelController {

	
	private static Logger logger = LoggerFactory.getLogger(ExcelController.class);
	
	@Autowired
    private JdbcTemplate jdbcTemplate;
	@Autowired
	private MessageHelper messageHelper;
	@Autowired
	private AttendanceEntityManager attendanceEntityManager;
	@Autowired 
	private ExcelService excelService;
	
	
	/**
	 *  检查导入设置    不用
	 * @return
	 * @throws ParseException
	 */
	@RequestMapping("check-attendance-excel-import")
	@ResponseBody
	public String checkAttendanceExcelImport() throws ParseException{
		
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
						
						return JSONObject.toJSONString("true");
						
					}else{
						return JSONObject.toJSONString("false");
					}
				}else{
					return JSONObject.toJSONString("false");
				}
			}
		}
		
		
		return JSONObject.toJSONString("true");
	}
	
	/**
	 * 导入数据
	 * @param excelFile
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("attendance-excel-import")
	//@ResponseBody
    public String attendanceExcelImport(@RequestParam(value="excelFile",required = false) MultipartFile excelFile,
    		Model model,RedirectAttributes redirectAttributes) throws Exception {

		
		ReadExcel readExcel = new ReadExcel();
		HashSet<String> allSet = new HashSet<String>();
		HashSet<String> failSet = new HashSet<String>();
		if(null != excelFile){
	    	List<Map<String, Object>> userList = readExcel.getExcelInfo(excelFile);
	    	if(userList!=null&&userList.size()>0){
	    		List<HashMap<String,Object>> importAttendance = excelService.importAttendance(userList,allSet,failSet);
	    		if(null != importAttendance && importAttendance.size() > 0){
	    			model.addAttribute("failList", importAttendance);
	    			model.addAttribute("allSize", allSet.size());
	    			model.addAttribute("failSize", failSet.size());
	    			return "auth/attendance-import-excel-i";
	    		}else{
	    			messageHelper.addFlashMessage(redirectAttributes, "core.success.publish", "导入成功");
	    		}
	    		
	    	}else{
	    		messageHelper.addFlashMessage(redirectAttributes, "core.success.publish", "导入EXCEL没有数据");
	    	}
		}
    	
    	return "redirect:/auth/attendance-import-excel-i.do";
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
	@RequestMapping(value="exportAttendanceStatistics")
	public void exportAttendanceStatistics(HttpServletResponse response,HttpServletRequest request,@RequestParam(value="name",required=false) String name,@RequestParam(value="partyEntityId",required=false) Long partyEntityId,@RequestParam(value="beginDate",required=false) String beginDate,@RequestParam(value="endDate",required=false) String endDate) throws Exception{
		excelService.exportAttendanceStatistics(response,request,name,partyEntityId,beginDate,endDate);
		
	}
	
}
