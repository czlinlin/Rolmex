package com.mossle.user.web;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mossle.api.menu.MenuConnector;
import com.mossle.auth.persistence.domain.Menu;
import com.mossle.core.auth.CurrentUserHolder;
import com.mossle.common.utils.StringUtils;
import com.mossle.core.excel.ReadExcel;
import com.mossle.core.excel.ReadSalaryExcel;
import com.mossle.core.spring.MessageHelper;
import com.mossle.dict.persistence.domain.DictInfo;
import com.mossle.dict.persistence.domain.DictType;
import com.mossle.dict.persistence.manager.DictInfoManager;
import com.mossle.dict.persistence.manager.DictTypeManager;
import com.mossle.operation.service.CustomWorkService;
import com.mossle.user.persistence.domain.PersonContractCompanyManage;
import com.mossle.user.persistence.domain.PersonSalaryAccumulationFund;
import com.mossle.user.persistence.domain.PersonSalaryAccumulationFund;
import com.mossle.user.persistence.domain.PersonSalaryBase;
import com.mossle.user.persistence.domain.PersonSalarySocialSecurity;
import com.mossle.user.persistence.manager.ContractCompanyManager;
import com.mossle.user.persistence.manager.PersonSalaryAccumulationFundManager;
import com.mossle.user.persistence.manager.PersonSalaryBaseManager;
import com.mossle.user.service.SalaryService;
import com.mossle.util.MailThread;
import com.mossle.util.ExportUtil;
import com.mossle.util.MailUtil;
import com.mossle.util.StringUtil;

/**
 * 人事工资
 * @author ckx
 *
 */
@Controller
@RequestMapping("user")
public class SalaryController {
	
	private static Logger logger = LoggerFactory.getLogger(PersonInfoController.class);
	
	@Autowired
	private DictInfoManager dictInfoManager;
	@Autowired
	private DictTypeManager dictTypeManager;
	@Autowired
	private MessageHelper messageHelper;
	@Autowired
    private JdbcTemplate jdbcTemplate;
	@Autowired
	private SalaryService salaryService;
	@Autowired
	private CurrentUserHolder currentUserHolder;
	@Autowired
	private MenuConnector menuConnector;
	@Autowired
	private PersonSalaryBaseManager personSalaryBaseManager;
	@Autowired
	private CustomWorkService customWorkService;
	@Autowired
	private ContractCompanyManager contractCompanyManager;

	
	
	/**
	 * 下载工资表模板
	 * @param request
	 * @param response
	 * @param type
	 */
	@RequestMapping("salaryDownload")
	public void salaryDownload(HttpServletRequest request , HttpServletResponse response,@RequestParam(value="type" ,required=true) String type){
		String path = this.getClass().getResource("/").getPath();
		path = path.replace("/WEB-INF/classes/", "");
		String filePath = "";
		String fileName = "";
		if("1".equals(type)){
			filePath = path+"/cdn/salaryMould/salaryBase.xlsx";
			fileName = "工资基本表.xlsx";
		}else if("2".equals(type)){
			filePath = path+"/cdn/salaryMould/salarySocialSecurity.xlsx";
			fileName = "社保扣款明细表.xlsx";
		}else if("3".equals(type)){
			filePath = path+"/cdn/salaryMould/salaryAccumulationFund.xlsx";
			fileName = "公积金扣款明细表.xlsx";
		}
		
		File file = new File(filePath);//获取文件
		if (file.exists()) {
		    try {
		        // 取得文件名。
		       // String fileName = file.getName();
		        // 以流的形式下载文件。
		        InputStream is = new BufferedInputStream(new FileInputStream(file));
		        byte[] buffer = new byte[is.available()];
		        is.read(buffer);
		        is.close();
		        // 清空response
		        response.reset();
		        // 设置response的Header   可定义下载文件名称
			    response.setHeader("Content-Disposition", "attachment; filename=" + new String(fileName.getBytes("gbk"), "iso8859-1"));
		        response.addHeader("Content-Length", "" + file.length());
		        OutputStream os = new BufferedOutputStream(response.getOutputStream());
		        response.setContentType("application/octet-stream");
		        os.write(buffer);
		        os.flush();//释放流
		        os.close();//关闭
		        response.flushBuffer();
		    } catch (IOException ex) {
		        ex.printStackTrace();
		    }
		}
	}
	
	/**
	 * 校验合同单位是否配置了单位邮箱
	 * @return
	 */
	@RequestMapping("checkedCompanyEmail")
	@ResponseBody
	private String checkedCompanyEmail(@RequestParam(value="contractCompanyId" ,required=true) String contractCompanyId){
		Map<String, Object> personContractCompanyMap = jdbcTemplate.queryForMap("select * from person_contract_company where id = '"+contractCompanyId+"'");
		String companyEmail = StringUtil.toString(personContractCompanyMap.get("company_email"));
		return JSONObject.toJSONString(StringUtils.isNotBlank(companyEmail));
	}
	
	/**
	 * 
	 * @param request
	 * @param request_uri
	 * @return
	 */
	@RequestMapping("salaryOpteration")
	@ResponseBody
	public String salaryOpteration(HttpServletRequest request,@RequestParam(value="url",required=true) String request_uri){
		StringBuffer realOpterNames=new StringBuffer(",");
		List<Menu> opterationMenus = menuConnector.findOpterationMenus(request_uri);
		if(opterationMenus!=null&&opterationMenus.size()>0){
			for(Menu m:opterationMenus){
				realOpterNames.append(m.getTitle()+",");
			}
		}
		return JSONObject.toJSONString(realOpterNames);
	}
	/**
	 * 发送工资条
	 * @param ids
	 * @param emailPassword
	 * @return
	 * @throws Exception 
	 */
	@RequestMapping("sendPayslip")
	@ResponseBody
	public String sendPayslip(@RequestParam(value="ids",required=true) String ids,
			@RequestParam(value="emailPassword",required=true) String emailPassword) throws Exception{
		
		Map<String,Object> checkUrlMap=menuConnector.checkMenuByName("/user/salary-payslip-list-i.do", "发送工资条");
		boolean isHaveAuth=Boolean.parseBoolean(checkUrlMap.get("checkResult").toString());
		if(!isHaveAuth){
			return JSONObject.toJSONString(false);
		}
		
		JSONArray parseArray = JSONObject.parseArray(ids);
		String[] split = parseArray.toJavaObject(String[].class);
		String id = split[0];
		PersonSalaryBase personSalaryBase = personSalaryBaseManager.findUniqueBy("id", Long.parseLong(id));
		String contractCompanyId = personSalaryBase.getContractCompanyId();
		//查询公司邮箱用户名
		PersonContractCompanyManage personContractCompanyManage = contractCompanyManager.get(Long.parseLong(contractCompanyId));
		String userName = personContractCompanyManage.getCompanyEmail();
		
		DictInfo smtpServerDictInfo = personContractCompanyManage.getSmtpServer();
		DictInfo popServerDictInfo = personContractCompanyManage.getPopServer();
		String smtpServer = "";
		String pop3Server = "";
		if(null != smtpServerDictInfo && null != popServerDictInfo){
			smtpServer = smtpServerDictInfo.getValue();
			pop3Server = popServerDictInfo.getValue();
		}else{
			return JSONObject.toJSONString(false);
		}
		
		/*Map<String, Object> personContractCompanyMap = jdbcTemplate.queryForMap("select * from person_contract_company where id = '"+contractCompanyId+"'");
		String userName = StringUtil.toString(personContractCompanyMap.get("company_email"));
		//查询邮箱服务器地址
		String smtpServer = "";
		String pop3Server = "";
		List<Map<String, Object>> queryForList = jdbcTemplate.queryForList("select t.descn,i.name,i.value from dict_type t LEFT JOIN dict_info i ON t.id = i.TYPE_ID where t.`NAME` = 'mailServer';");
		for (Map<String, Object> map : queryForList) {
			String name = StringUtil.toString(map.get("name"));
			if("smtp".equals(name)){
				smtpServer = StringUtil.toString(map.get("value"));
			}else if("pop3".equals(name)){
				pop3Server = StringUtil.toString(map.get("value"));
			}
		}*/
		
		if(StringUtils.isBlank(smtpServer) || StringUtils.isBlank(pop3Server) ){
			return JSONObject.toJSONString(false);
		}
		boolean checkedPassword = false;
		if("smtp.qq.com".equals(smtpServer)){
			checkedPassword = MailUtil.checkEmail(smtpServer, userName, emailPassword);
		}else{
			checkedPassword = MailUtil.checkedPassword(pop3Server,userName, emailPassword);
		}
		
		
		if(checkedPassword){
			MailThread mailThread = new MailThread();
			mailThread.setIds(ids);
			mailThread.setEmailPassword(emailPassword);
			mailThread.setMailServer(smtpServer);
			new Thread(mailThread).start();
		}else{
			return JSONObject.toJSONString(false);
		}
		return JSONObject.toJSONString(true);
	}
	
	@RequestMapping("salary-payslip-list-i")
	public String salaryPayslipListSub(Model model) throws Exception{
		Map<String,Object> checkUrlMap=menuConnector.checkMenuByName("/user/salary-payslip-list-i.do", "查询");
		boolean isHaveAuth=Boolean.parseBoolean(checkUrlMap.get("checkResult").toString());
		if(!isHaveAuth){
			return checkUrlMap.get("url").toString();
		}
		return "user/salary/salary-payslip-list-i";
	}
	
	@RequestMapping("salary-payslip-list")
	public String salaryPayslipList(){
		return "user/salary/salary-payslip-list";
	}
	
	
	/**
	 * 删除某一行数据公积金扣款表
	 * @param id
	 * @return
	 */
	@RequestMapping(value="deleteSalaryAccumulationFundById")
	@ResponseBody
	public String deleteSalaryAccumulationFundById(@RequestParam(value="id") String id,HttpServletRequest request) throws Exception{
		Map<String,Object> checkUrlMap=menuConnector.checkMenuByName("/user/salary-accumulation-fund-list-i.do", "删除");
		boolean isHaveAuth=Boolean.parseBoolean(checkUrlMap.get("checkResult").toString());
		if(!isHaveAuth){
			return checkUrlMap.get("url").toString();
		}
		boolean boo = salaryService.deleteSalaryAccumulationFundById(id);
		return JSONObject.toJSONString(boo);
	}
	
	/**
	 * 修改某一行公积金数据
	 * @param model
	 * @param personSalaryBase
	 * @param postId
	 * @param contractCompanyId
	 * @param startDate
	 * @param endDate
	 * @param personName
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="editSalaryAccumulationFund")
	@ResponseBody
	public String editSalaryAccumulationFund(Model model,PersonSalaryAccumulationFund personSalaryAccumulationFund,
			@RequestParam(value="postId") String postId,
			@RequestParam(value="contractCompanyId",required=false) String contractCompanyId,
			@RequestParam(value="startDate",required=false) String startDate,
			@RequestParam(value="endDate",required=false) String endDate,
			@RequestParam(value="personNamePar",required=false) String personName,HttpServletRequest request) throws Exception{
		Map<String,Object> checkUrlMap=menuConnector.checkMenuByName("/user/salary-accumulation-fund-list-i.do", "编辑");
		boolean isHaveAuth=Boolean.parseBoolean(checkUrlMap.get("checkResult").toString());
		if(!isHaveAuth){
			return checkUrlMap.get("url").toString();
		}
		salaryService.editSalaryAccumulationFund(personSalaryAccumulationFund);
		String salarySocialSecurityListData = salaryAccumulationFundListData(model, postId, contractCompanyId, startDate, endDate, personName);
		return salarySocialSecurityListData;
	}
	
	/**
	 * 查询所有的数据公积金
	 * @param model
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("salary-accumulation-fund-list-data")
	@ResponseBody
	public String salaryAccumulationFundListData(Model model,@RequestParam(value="postId") String postId,
			@RequestParam(value="contractCompanyId",required=false) String contractCompanyId,
			@RequestParam(value="startDate",required=false) String startDate,
			@RequestParam(value="endDate",required=false) String endDate,
			@RequestParam(value="personNamePar",required=false) String personName) throws Exception{
		Map<String, Object> map = new HashMap<String, Object>();
		List<PersonSalaryAccumulationFund> salaryAccumulationFundListSub = salaryService.salaryAccumulationFundListSub(postId,contractCompanyId,startDate,endDate,personName);
		map.put("rows", salaryAccumulationFundListSub);
		String jsonString = JSONObject.toJSONString(map);
		return jsonString;
	}
	
	
	/**
	 * 生成下一月份的数据 公积金
	 * @param contractCompanyId
	 * @param date
	 * @param year
	 * @param month
	 * @return
	 */
	@RequestMapping("salaryAccumulationFundCreateMonthData")
	@ResponseBody
	public String salaryAccumulationFundCreateMonthData(
			@RequestParam(value="contractCompanyId") String contractCompanyId ,
			@RequestParam(value="currentDate") String currentDate ,
			@RequestParam(value="nextDate") String nextDate ,
			@RequestParam(value="year") String year ,
			@RequestParam(value="month") String month ,HttpServletRequest request) throws Exception{
		Map<String,Object> checkUrlMap=menuConnector.checkMenuByName("/user/salary-accumulation-fund-list-i.do", "生成当月数据");
		boolean isHaveAuth=Boolean.parseBoolean(checkUrlMap.get("checkResult").toString());
		if(!isHaveAuth){
			return JSONObject.toJSONString(false);
		}else{
			try {
				salaryService.salaryAccumulationFundCreateMonthData(contractCompanyId,currentDate,nextDate,year,month);
			} catch (Exception e) {
				e.printStackTrace();
				return JSONObject.toJSONString(false);
			}
			return JSONObject.toJSONString(true);
		}
		
	}
	/**
	 * 查询当前薪资单位下的最大月份 公积金
	 * @param contractCompanyId
	 * @return
	 * @throws Exception 
	 */
	@RequestMapping("queryAccumulationFundMonthByContractCompanyId")
	@ResponseBody
	public String queryAccumulationFundMonthByContractCompanyId(@RequestParam(value="contractCompanyId") String contractCompanyId ) throws Exception{
		Map<String,Object> map = salaryService.queryAccumulationFundMonthByContractCompanyId(contractCompanyId);
		return JSONObject.toJSONString(map);
	}
	
	/**
	 * 导入excel 公积金
	 * @param excelFile
	 * @param salaryColume
	 * @param contractCompanyId
	 * @param model
	 * @param redirectAttributes
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("salary-accumulation-fund-import-excel-do")
	public String salaryAccumulationFundImportExcelDo(@RequestParam(value="excelFile",required = false) MultipartFile excelFile,
    		@RequestParam(value="contractCompanyId") String contractCompanyId ,
    		Model model,RedirectAttributes redirectAttributes) throws Exception{
		
		ReadSalaryExcel readExcel = new ReadSalaryExcel();
		if(null != excelFile){
	    	List<Map<String, Object>> salaryAccumulationFundList = readExcel.getExcelInfo(excelFile,3);
	    	if(salaryAccumulationFundList!=null&&salaryAccumulationFundList.size()>0){
	    		List<Map<String,Object>> importSalaryAccumulationFund = salaryService.importSalaryAccumulationFund(contractCompanyId,salaryAccumulationFundList);
	    		if(null != importSalaryAccumulationFund && importSalaryAccumulationFund.size() > 0){
	    			model.addAttribute("failList", importSalaryAccumulationFund);
	    			model.addAttribute("allSize", salaryAccumulationFundList.size());
	    			model.addAttribute("failSize", importSalaryAccumulationFund.size());
	    			//查询工资导入导出可选项
	    			DictType dictType = dictTypeManager.findUniqueBy("name", "salaryColume");
	    			List<DictInfo> dictInfoList = dictInfoManager.findBy("dictType", dictType);
	    			model.addAttribute("dictInfoList", dictInfoList);
	    			return "user/salary/salary-accumulation-fund-import-excel";
	    		}else{
	    			messageHelper.addFlashMessage(redirectAttributes, "core.success.publish", "导入成功");
	    		}
	    		
	    	}else{
	    		messageHelper.addFlashMessage(redirectAttributes, "core.success.publish", "导入EXCEL没有数据");
	    	}
		}
		return "redirect:/user/salary-accumulation-fund-import-excel.do";
	}
	
	/**
	 * 跳转到导入excel页面 公积金
	 * @param model
	 * @return
	 */
	@RequestMapping("salary-accumulation-fund-import-excel")
	public String salaryAccumulationFundImportExcel(Model model,HttpServletRequest request) throws Exception{
		Map<String,Object> checkUrlMap=menuConnector.checkMenuByName("/user/salary-accumulation-fund-list-i.do", "导入");
		boolean isHaveAuth=Boolean.parseBoolean(checkUrlMap.get("checkResult").toString());
		if(!isHaveAuth){
			return checkUrlMap.get("url").toString();
		}
		return "user/salary/salary-accumulation-fund-import-excel";
	}

	@RequestMapping("salary-accumulation-fund-list-i")
	public String salaryAccumulationFundListSub(Model model,HttpServletRequest request) throws Exception{
		Map<String,Object> checkUrlMap=menuConnector.checkMenuByName("/user/salary-accumulation-fund-list-i.do", "查询");
		boolean isHaveAuth=Boolean.parseBoolean(checkUrlMap.get("checkResult").toString());
		if(!isHaveAuth){
			return checkUrlMap.get("url").toString();
		}
		return "user/salary/salary-accumulation-fund-list-i";
	}
	
	@RequestMapping("salary-accumulation-fund-list")
	public String salaryAccumulationFundList(){
		return "user/salary/salary-accumulation-fund-list";
	}
	
	/**
	 * 删除某一行数据 社保扣款表
	 * @param id
	 * @return
	 */
	@RequestMapping(value="deleteSalarySocialSecurityById")
	@ResponseBody
	public String deleteSalarySocialSecurityById(@RequestParam(value="id") String id,HttpServletRequest request) throws Exception{
		Map<String,Object> checkUrlMap=menuConnector.checkMenuByName("/user/salary-socialSecurity-list-i.do", "删除");
		boolean isHaveAuth=Boolean.parseBoolean(checkUrlMap.get("checkResult").toString());
		if(!isHaveAuth){
			return checkUrlMap.get("url").toString();
		}
		boolean boo = salaryService.deleteSalarySocialSecurityById(id);
		return JSONObject.toJSONString(boo);
	}
	
	/**
	 * 修改某一行社保数据
	 * @param model
	 * @param personSalaryBase
	 * @param postId
	 * @param contractCompanyId
	 * @param startDate
	 * @param endDate
	 * @param personName
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="editSalarySocialSecurity")
	@ResponseBody
	public String editSalarySocialSecurity(Model model,PersonSalarySocialSecurity personSalarySocialSecurity,
			@RequestParam(value="postId") String postId,
			@RequestParam(value="contractCompanyId",required=false) String contractCompanyId,
			@RequestParam(value="startDate",required=false) String startDate,
			@RequestParam(value="endDate",required=false) String endDate,
			@RequestParam(value="personNamePar",required=false) String personName,HttpServletRequest request) throws Exception{
		Map<String,Object> checkUrlMap=menuConnector.checkMenuByName("/user/salary-socialSecurity-list-i.do", "编辑");
		boolean isHaveAuth=Boolean.parseBoolean(checkUrlMap.get("checkResult").toString());
		if(!isHaveAuth){
			return checkUrlMap.get("url").toString();
		}
		salaryService.editSalarySocialSecurity(personSalarySocialSecurity);
		String salarySocialSecurityListData = salarySocialSecurityListData(model, postId, contractCompanyId, startDate, endDate, personName);
		return salarySocialSecurityListData;
	}
	
	/**
	 * 查询所有的数据社保
	 * @param model
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("salary-socialSecurity-list-data")
	@ResponseBody
	public String salarySocialSecurityListData(Model model,@RequestParam(value="postId",required=false) String postId,
			@RequestParam(value="contractCompanyId",required=false) String contractCompanyId,
			@RequestParam(value="startDate",required=false) String startDate,
			@RequestParam(value="endDate",required=false) String endDate,
			@RequestParam(value="personNamePar",required=false) String personName) throws Exception{
		Map<String, Object> map = new HashMap<String, Object>();
		List<PersonSalarySocialSecurity> salarySocialSecurityListSub = salaryService.salarySocialSecurityListSub(postId,contractCompanyId,startDate,endDate,personName);
		map.put("rows", salarySocialSecurityListSub);
		String jsonString = JSONObject.toJSONString(map);
		return jsonString;
	}
	
	
	/**
	 * 生成下一月份的数据 社保
	 * @param contractCompanyId
	 * @param date
	 * @param year
	 * @param month
	 * @return
	 */
	@RequestMapping("salarySocialSecurityCreateMonthData")
	@ResponseBody
	public String salarySocialSecurityCreateMonthData(
			@RequestParam(value="contractCompanyId") String contractCompanyId ,
			@RequestParam(value="currentDate") String currentDate ,
			@RequestParam(value="nextDate") String nextDate ,
			@RequestParam(value="year") String year ,
			@RequestParam(value="month") String month ,HttpServletRequest request) throws Exception{
		Map<String,Object> checkUrlMap=menuConnector.checkMenuByName("/user/salary-socialSecurity-list-i.do", "生成当月数据");
		boolean isHaveAuth=Boolean.parseBoolean(checkUrlMap.get("checkResult").toString());
		if(!isHaveAuth){
			return JSONObject.toJSONString(false);
		}else{
			try {
				salaryService.salarySocialSecurityCreateMonthData(contractCompanyId,currentDate,nextDate,year,month);
			} catch (Exception e) {
				e.printStackTrace();
				return JSONObject.toJSONString(false);
			}
			return JSONObject.toJSONString(true);
		}
		
	}
	
	/**
	 * 查询当前薪资单位下的最大月份 社保
	 * @param contractCompanyId
	 * @return
	 * @throws Exception 
	 */
	@RequestMapping("querySocialSecurityMonthByContractCompanyId")
	@ResponseBody
	public String querySocialSecurityMonthByContractCompanyId(@RequestParam(value="contractCompanyId") String contractCompanyId ) throws Exception{
		Map<String,Object> map = salaryService.querySocialSecurityMonthByContractCompanyId(contractCompanyId);
		return JSONObject.toJSONString(map);
	}
	/**
	 * 导入excel 社保
	 * @param excelFile
	 * @param salaryColume
	 * @param contractCompanyId
	 * @param model
	 * @param redirectAttributes
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("salary-socialSecurity-import-excel-do")
	public String salarySocialSecurityImportExcelDo(@RequestParam(value="excelFile",required = false) MultipartFile excelFile,
    		@RequestParam(value="contractCompanyId") String contractCompanyId ,
    		Model model,RedirectAttributes redirectAttributes) throws Exception{
		
		ReadSalaryExcel readExcel = new ReadSalaryExcel();
		if(null != excelFile){
	    	List<Map<String, Object>> salarySocialSecurityList = readExcel.getExcelInfo(excelFile,4);
	    	if(salarySocialSecurityList!=null&&salarySocialSecurityList.size()>0){
	    		List<Map<String,Object>> importSocialSecurity = salaryService.importSocialSecurity(contractCompanyId,salarySocialSecurityList);
	    		if(null != importSocialSecurity && importSocialSecurity.size() > 0){
	    			model.addAttribute("failList", importSocialSecurity);
	    			model.addAttribute("allSize", salarySocialSecurityList.size());
	    			model.addAttribute("failSize", importSocialSecurity.size());
	    			//查询工资导入导出可选项
	    			DictType dictType = dictTypeManager.findUniqueBy("name", "salaryColume");
	    			List<DictInfo> dictInfoList = dictInfoManager.findBy("dictType", dictType);
	    			model.addAttribute("dictInfoList", dictInfoList);
	    			return "user/salary/salary-socialSecurity-import-excel";
	    		}else{
	    			messageHelper.addFlashMessage(redirectAttributes, "core.success.publish", "导入成功");
	    		}
	    		
	    	}else{
	    		messageHelper.addFlashMessage(redirectAttributes, "core.success.publish", "导入EXCEL没有数据");
	    	}
		}
		return "redirect:/user/salary-socialSecurity-import-excel.do";
	}
	
	
	/**
	 * 跳转到导入excel页面
	 * @param model
	 * @return
	 */
	@RequestMapping("salary-socialSecurity-import-excel")
	public String salarySocialSecurityImportExcel(Model model,HttpServletRequest request) throws Exception{
		Map<String,Object> checkUrlMap=menuConnector.checkMenuByName("/user/salary-socialSecurity-list-i.do", "导入");
		boolean isHaveAuth=Boolean.parseBoolean(checkUrlMap.get("checkResult").toString());
		if(!isHaveAuth){
			return checkUrlMap.get("url").toString();
		}
		return "user/salary/salary-socialSecurity-import-excel";
	}
	
	@RequestMapping("salary-socialSecurity-list")
	public String salarySocialSecurityList(){
		return "user/salary/salary-socialSecurity-list";
	}
	
	@RequestMapping("salary-socialSecurity-list-i")
	public String salarySocialSecurityListSub(Model model,HttpServletRequest request) throws Exception{
		Map<String,Object> checkUrlMap=menuConnector.checkMenuByName("/user/salary-socialSecurity-list-i.do", "查询");
		boolean isHaveAuth=Boolean.parseBoolean(checkUrlMap.get("checkResult").toString());
		if(!isHaveAuth){
			return checkUrlMap.get("url").toString();
		}
		return "user/salary/salary-socialSecurity-list-i";
	}
	

	/**
	 * 修改某一行工资数据
	 * @param model
	 * @param personSalaryBase
	 * @param postId
	 * @param contractCompanyId
	 * @param startDate
	 * @param endDate
	 * @param personName
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="editSalatyBase")
	@ResponseBody
	public String editSalatyBase(Model model,PersonSalaryBase personSalaryBase,
			@RequestParam(value="postId") String postId,
			@RequestParam(value="contractCompanyId",required=false) String contractCompanyId,
			@RequestParam(value="startDate",required=false) String startDate,
			@RequestParam(value="endDate",required=false) String endDate,
			@RequestParam(value="personNamePar",required=false) String personName,HttpServletRequest request) throws Exception{
		Map<String,Object> checkUrlMap=menuConnector.checkMenuByName("/user/salary-base-list-i.do", "编辑");
		boolean isHaveAuth=Boolean.parseBoolean(checkUrlMap.get("checkResult").toString());
		if(!isHaveAuth){
			return checkUrlMap.get("url").toString();
		}
		salaryService.editSalatyBase(personSalaryBase);
		String salaryBaseListData = salaryBaseListData(model, postId, contractCompanyId, startDate, endDate, personName);
		return salaryBaseListData;
	}
	
	/**
	 * 删除某一行数据 基本工资表
	 * @param id
	 * @return
	 */
	@RequestMapping(value="deleteSalaryBaseById")
	@ResponseBody
	public String deleteSalaryBaseById(@RequestParam(value="id") String id,HttpServletRequest request) throws Exception{
		Map<String,Object> checkUrlMap=menuConnector.checkMenuByName("/user/salary-base-list-i.do", "删除");
		boolean isHaveAuth=Boolean.parseBoolean(checkUrlMap.get("checkResult").toString());
		if(!isHaveAuth){
			return checkUrlMap.get("url").toString();
		}
		boolean boo = salaryService.deleteSalaryBaseById(id);
		return JSONObject.toJSONString(boo);
	}
	
	/**
	 * 查询所有的数据 基本工资
	 * @param model
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("salary-base-list-data")
	@ResponseBody
	public String salaryBaseListData(Model model,@RequestParam(value="postId",required=false) String postId,
			@RequestParam(value="contractCompanyId",required=false) String contractCompanyId,
			@RequestParam(value="startDate",required=false) String startDate,
			@RequestParam(value="endDate",required=false) String endDate,
			@RequestParam(value="personNamePar",required=false) String personName) throws Exception{
		Map<String, Object> map = new HashMap<String, Object>();
		List<PersonSalaryBase> salaryBaseListSub = salaryService.salaryBaseListSub(postId,contractCompanyId,startDate,endDate,personName);
		map.put("rows", salaryBaseListSub);
		String jsonString = JSONObject.toJSONString(map);
		return jsonString;
	}

	/**
	 * 导入excel
	 * @param excelFile
	 * @param salaryColume
	 * @param contractCompanyId
	 * @param model
	 * @param redirectAttributes
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("salary-base-import-excel-do")
	public String salaryBaseImportExcelDo(@RequestParam(value="excelFile",required = false) MultipartFile excelFile,
    		@RequestParam(value="salaryColume",required = false ) String salaryColume ,
    		@RequestParam(value="contractCompanyId") String contractCompanyId ,
    		Model model,RedirectAttributes redirectAttributes) throws Exception{
		
		ReadSalaryExcel readExcel = new ReadSalaryExcel();
		if(null != excelFile){
	    	List<Map<String, Object>> salaryBaseList = readExcel.getExcelInfo(excelFile,3);
	    	if(salaryBaseList!=null&&salaryBaseList.size()>0){
	    		List<Map<String,Object>> importSalaryBase = salaryService.importSalaryBase(salaryColume,contractCompanyId,salaryBaseList);
	    		if(null != importSalaryBase && importSalaryBase.size() > 0){
	    			model.addAttribute("failList", importSalaryBase);
	    			model.addAttribute("allSize", salaryBaseList.size());
	    			model.addAttribute("failSize", importSalaryBase.size());
	    			//查询工资导入导出可选项
	    			DictType dictType = dictTypeManager.findUniqueBy("name", "salaryColume");
	    			List<DictInfo> dictInfoList = dictInfoManager.findBy("dictType", dictType);
	    			model.addAttribute("dictInfoList", dictInfoList);
	    			return "user/salary/salary-base-import-excel";
	    		}else{
	    			messageHelper.addFlashMessage(redirectAttributes, "core.success.publish", "导入成功");
	    		}
	    		
	    	}else{
	    		messageHelper.addFlashMessage(redirectAttributes, "core.success.publish", "导入EXCEL没有数据");
	    	}
		}
		
		
		return "redirect:/user/salary-base-import-excel.do";
	}
	/**
	 * 查询当前人员负责哪些公司
	 * @return
	 */
	@RequestMapping("getAllContractCompanyName")
	@ResponseBody
	public String getAllContractCompanyName(){
		List<Map<String,Object>> contractCompanyList = salaryService.getAllContractCompanyName();
		
		return JSONObject.toJSONString(contractCompanyList);
	}
	
	/**
	 * 跳转到导入excel页面
	 * @param model
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	@RequestMapping("salary-base-import-excel")
	public String salaryBaseImportExcel(Model model,HttpServletRequest request) throws Exception{
		Map<String,Object> checkUrlMap=menuConnector.checkMenuByName("/user/salary-base-list-i.do", "导入");
		boolean isHaveAuth=Boolean.parseBoolean(checkUrlMap.get("checkResult").toString());
		if(!isHaveAuth){
			return checkUrlMap.get("url").toString();
		}
		
		//查询工资导入导出可选项
		DictType dictType = dictTypeManager.findUniqueBy("name", "salaryColume");
		List<DictInfo> dictInfoList = dictInfoManager.findBy("dictType", dictType);
		model.addAttribute("dictInfoList", dictInfoList);
		return "user/salary/salary-base-import-excel";
	}
	
	@RequestMapping("salary-base-list-i")
	public String salaryBaseListSub(Model model,HttpServletRequest request) throws Exception{
		Map<String,Object> checkUrlMap=menuConnector.checkMenuByName("/user/salary-base-list-i.do" , "查询");
		boolean isHaveAuth=Boolean.parseBoolean(checkUrlMap.get("checkResult").toString());
		if(!isHaveAuth){
			return checkUrlMap.get("url").toString();
		}
		return "user/salary/salary-base-list-i";
	}
	@RequestMapping("salary-base-list")
	public String salaryBaseList(){
		return "user/salary/salary-base-list";
	}
	

	
	@RequestMapping(value="auth-contractdata")
	public String authContractdata(Model model){
		return "user/contractConfig/user-contractdata-list";
	}
	/**
	 * @param model
	 * @param name
	 * @return
	 * @author sjx
	 * 工资模块合同单位数据权限查询列表
	 * 18.12.10
	 */
	@RequestMapping(value="auth-contractdata-i")
	public String authContractdata(String name,Model model){
		List<Map<String, Object>> authContractdataList = salaryService.queryAuthContractdata(name);
		model.addAttribute("authContractdataList", authContractdataList);
		if(!StringUtils.isBlank(name)){
			model.addAttribute("name", name);
		}
		return "user/contractConfig/user-contractdata-list-i";
	}
	/**
	 * @param id
	 * @author sjx
	 * @return
	 * 合同数据权限新建或编辑修改
	 */
	@RequestMapping(value="newBuildOrEdit-contractdata")
	public String newBuildContractdata(String id,Model model){
		//查询出所有合同单位
		List<Map<String, Object>> contractCompanyList = salaryService.queryContractCompanyList();
		model.addAttribute("contractCompanyList", contractCompanyList);
		if("0".equals(id)){
			return "user/contractConfig/user-contractdata-newBuildOrEdit";
		}
		Map<String, Object> ontractByUnionId = salaryService.queryContractByUnionId(id);
		model.addAttribute("contractByUnionId", ontractByUnionId);
		
		return "user/contractConfig/user-contractdata-newBuildOrEdit";
	}
	/**
	 * @param unionId
	 * @param contractCompany
	 * @param note
	 * @author sjx
	 * 18.12.11
	 * 保存合同单位配置
	 */
	@RequestMapping(value="auth-contractdata-save")
	public String saveContractCompanySetting(String unionId,String contractCompany,String note,RedirectAttributes redirectAttributes){
		salaryService.delContractSetting(unionId);
		salaryService.saveContractSetting(unionId, contractCompany, note);
		messageHelper.addFlashMessage(redirectAttributes, "保存成功");
		return "redirect:/user/auth-contractdata-i.do";
	}
	/**
	 * @param unionId
	 * @param redirectAttributes
	 * @author sjx
	 * 18.12.11
	 * 删除配置数据
	 * @return
	 */
	@RequestMapping(value="auth-contractdata-del")
	public String delContract(String unionId,RedirectAttributes redirectAttributes){
		salaryService.delContractSetting(unionId);
		messageHelper.addFlashMessage(redirectAttributes, "删除成功");
		return "redirect:/user/auth-contractdata-i.do";
	}
	/**
	 * 导出公积金
	 * @param response
	 * @param request
	 * @throws Exception
	 * @author sjx
	 */
	@RequestMapping(value="accumulation-fund-export")
	public void exportAccumulationFund(HttpServletResponse response,HttpServletRequest request,
			@RequestParam(value="postId",required=false) String postId,
			@RequestParam(value="contractCompanyId",required=false) String contractCompanyId,
			@RequestParam(value="startDate",required=false) String startDate,
			@RequestParam(value="endDate",required=false) String endDate,
			@RequestParam(value="personName",required=false) String personName) throws Exception{
		//判断是否有导出权限
		Map<String,Object> checkUrlMap=menuConnector.checkMenuByName("/user/salary-accumulation-fund-list-i.do", "导出");
		boolean isHaveAuth=Boolean.parseBoolean(checkUrlMap.get("checkResult").toString());
		if(!isHaveAuth){
			String title = "非法操作！";
            StringBuffer sb = new StringBuffer();
            sb.append("<script language='javascript'>alert('");
            sb.append(title);
            sb.append("');history.go(-1);</script>");
            try {
                response.setContentType("text/html; charset=utf-8");
                String strHtml = "<body style='background-color: #D0D0D0'>";
                strHtml += "</body>";
                response.getWriter().println((strHtml));
                response.getWriter().println(sb.toString());
                response.getWriter().flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
		}else{
			List<PersonSalaryAccumulationFund> resultData = salaryService.salaryAccumulationFundListSub(postId,contractCompanyId,startDate,endDate,personName);
			if(resultData == null || resultData.size() == 0){
				String title = "暂无数据需要导出！";
				StringBuffer sb = new StringBuffer();
				sb.append("<script language='javascript'>alert('");
				sb.append(title);
				sb.append("');history.go(-1);</script>");
				try {
					response.setContentType("text/html; charset=utf-8");
					String strHtml = "<body style='background-color: #D0D0D0'>";
					strHtml += "</body>";
					response.getWriter().println((strHtml));
					response.getWriter().println(sb.toString());
					response.getWriter().flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}else{
				Map<String, Object> infoByContractCompanyId = salaryService.infoByContractCompanyId(contractCompanyId, 3);
				ExportUtil.exportAccumulationFund(response, request, resultData,startDate,endDate,infoByContractCompanyId);
			}
		}
	}
	/**
	 * 导出工资基本表数据
	 * @param response
	 * @param request
	 * @throws Exception
	 * @author sjx
	 */
	@RequestMapping(value="person_salary_base-export")
	public void exportPersonSalaryBase(HttpServletResponse response,HttpServletRequest request,String[] salaryColumn,
			@RequestParam(value="postId",required=false) String postId,
			@RequestParam(value="contractCompanyId",required=false) String contractCompanyId,
			@RequestParam(value="startDate",required=false) String startDate,
			@RequestParam(value="endDate",required=false) String endDate,
			@RequestParam(value="personName",required=false) String personName) throws Exception{
		
		//判断是否有导出权限
		Map<String,Object> checkUrlMap=menuConnector.checkMenuByName("/user/salary-accumulation-fund-list-i.do", "导出");
		boolean isHaveAuth=Boolean.parseBoolean(checkUrlMap.get("checkResult").toString());
		if(!isHaveAuth){
			String title = "非法操作！";
            StringBuffer sb = new StringBuffer();
            sb.append("<script language='javascript'>alert('");
            sb.append(title);
            sb.append("');history.go(-1);</script>");
            try {
                response.setContentType("text/html; charset=utf-8");
                String strHtml = "<body style='background-color: #D0D0D0'>";
                strHtml += "</body>";
                response.getWriter().println((strHtml));
                response.getWriter().println(sb.toString());
                response.getWriter().flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
		}else{
			//将前台选择的导出项做格式化处理
			String[] formatSalaryColumn =  null;
			if(salaryColumn != null){
				formatSalaryColumn = salaryService.forMatExportTerm(salaryColumn);
			}
			List<PersonSalaryBase> resultData = salaryService.salaryBaseListSub(postId, contractCompanyId, startDate, endDate, personName);
			if(resultData == null || resultData.size() == 0 ){
				String title = "暂无数据需要导出！";
				StringBuffer sb = new StringBuffer();
				sb.append("<script language='javascript'>alert('");
				sb.append(title);
				sb.append("');history.go(-1);</script>");
				try {
					response.setContentType("text/html; charset=utf-8");
					String strHtml = "<body style='background-color: #D0D0D0'>";
					strHtml += "</body>";
					response.getWriter().println((strHtml));
					response.getWriter().println(sb.toString());
					response.getWriter().flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}else{
				Map<String, Object> infoByContractCompanyId = salaryService.infoByContractCompanyId(contractCompanyId, 1);
				ExportUtil.exportPersonSalaryBase(response, request, resultData,formatSalaryColumn,startDate,endDate,salaryColumn,infoByContractCompanyId);
			}
		}
	}
	/**
	 * 导出工资条数据
	 * @param response
	 * @param request
	 * @throws Exception
	 * @author sjx
	 * 
	 */
	@RequestMapping(value="person_salary_slip-export")
	public void exportPersonSalarySlip(HttpServletResponse response,HttpServletRequest request,
			@RequestParam(value="postId",required=false) String postId,
			@RequestParam(value="contractCompanyId",required=false) String contractCompanyId,
			@RequestParam(value="startDate",required=false) String startDate,
			@RequestParam(value="endDate",required=false) String endDate,
			@RequestParam(value="personName",required=false) String personName) throws Exception{
		
		//判断是否有导出权限
		Map<String,Object> checkUrlMap=menuConnector.checkMenuByName("/user/salary-accumulation-fund-list-i.do", "导出");
		boolean isHaveAuth=Boolean.parseBoolean(checkUrlMap.get("checkResult").toString());
		if(!isHaveAuth){
			String title = "非法操作！";
            StringBuffer sb = new StringBuffer();
            sb.append("<script language='javascript'>alert('");
            sb.append(title);
            sb.append("');history.go(-1);</script>");
            try {
                response.setContentType("text/html; charset=utf-8");
                String strHtml = "<body style='background-color: #D0D0D0'>";
                strHtml += "</body>";
                response.getWriter().println((strHtml));
                response.getWriter().println(sb.toString());
                response.getWriter().flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
		}else{
			List<PersonSalaryBase> resultData = salaryService.salaryBaseListSub(postId, contractCompanyId, startDate, endDate, personName);
			if(resultData == null || resultData.size() == 0){
				String title = "暂无数据需要导出！";
				StringBuffer sb = new StringBuffer();
				sb.append("<script language='javascript'>alert('");
				sb.append(title);
				sb.append("');history.go(-1);</script>");
				try {
					response.setContentType("text/html; charset=utf-8");
					String strHtml = "<body style='background-color: #D0D0D0'>";
					strHtml += "</body>";
					response.getWriter().println((strHtml));
					response.getWriter().println(sb.toString());
					response.getWriter().flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}else{
				Map<String, Object> infoByContractCompanyId = salaryService.infoByContractCompanyId(contractCompanyId, 4);
				ExportUtil.exportPersonSalarySlip(response, request, resultData,startDate,endDate,infoByContractCompanyId);
			}
		}
		
	}
	/**
	 * 导出社保扣款明细
	 * @param response
	 * @param request
	 * @throws Exception
	 * @author sjx
	 */
	@RequestMapping(value="person-salary-social-security-export")
	public void exportPersonSalarySocialSecurity(HttpServletResponse response,HttpServletRequest request,
			@RequestParam(value="postId",required=false) String postId,
			@RequestParam(value="contractCompanyId",required=false) String contractCompanyId,
			@RequestParam(value="startDate",required=false) String startDate,
			@RequestParam(value="endDate",required=false) String endDate,
			@RequestParam(value="personName",required=false) String personName) throws Exception{
		
		//判断是否有导出权限
		Map<String,Object> checkUrlMap=menuConnector.checkMenuByName("/user/salary-accumulation-fund-list-i.do", "导出");
		boolean isHaveAuth=Boolean.parseBoolean(checkUrlMap.get("checkResult").toString());
		if(!isHaveAuth){
			String title = "非法操作！";
            StringBuffer sb = new StringBuffer();
            sb.append("<script language='javascript'>alert('");
            sb.append(title);
            sb.append("');history.go(-1);</script>");
            try {
                response.setContentType("text/html; charset=utf-8");
                String strHtml = "<body style='background-color: #D0D0D0'>";
                strHtml += "</body>";
                response.getWriter().println((strHtml));
                response.getWriter().println(sb.toString());
                response.getWriter().flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
		}else{
			List<PersonSalarySocialSecurity> resultData = salaryService.salarySocialSecurityListSub(postId,contractCompanyId,startDate,endDate,personName);
			if(resultData == null || resultData.size() == 0){
				String title = "暂无数据需要导出！";
				StringBuffer sb = new StringBuffer();
				sb.append("<script language='javascript'>alert('");
				sb.append(title);
				sb.append("');history.go(-1);</script>");
				try {
					response.setContentType("text/html; charset=utf-8");
					String strHtml = "<body style='background-color: #D0D0D0'>";
					strHtml += "</body>";
					response.getWriter().println((strHtml));
					response.getWriter().println(sb.toString());
					response.getWriter().flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}else{
				Map<String, Object> infoByContractCompanyId = salaryService.infoByContractCompanyId(contractCompanyId, 2);
				ExportUtil.exportPersonSalarySocialSecurity(response, request, resultData,startDate,endDate,infoByContractCompanyId);
			}
		}
	}
	/**
	 * @param model
	 * @param org
	 * @param company
	 * @param startDate
	 * @param endDate
	 * @param personName
	 * @return
	 * @author sjx
	 * @throws Exception 
	 */
	@RequestMapping(value="person-salary-base-choose-trim")
	public String personSalaryBaseChooseTrim(HttpServletRequest request, Model model, 
			@RequestParam(value="postId",required=false)String postId,
			@RequestParam(value="contractCompanyId",required=false)String contractCompanyId,
			@RequestParam(value="startDate",required=false)String startDate,
			@RequestParam(value="endDate",required=false)String endDate,
			@RequestParam(value="personName",required=false)String personName) throws Exception{
		Map<String, String> param = new HashMap<>();
		param.put("postId", postId);
		param.put("contractCompanyId", contractCompanyId);
		param.put("startDate", startDate);
		param.put("endDate", endDate);
		param.put("personName", personName);
		
		Map<String,Object> checkUrlMap=menuConnector.checkMenuByName("/user/salary-accumulation-fund-list-i.do", "导出");
		boolean isHaveAuth=Boolean.parseBoolean(checkUrlMap.get("checkResult").toString());
		if(!isHaveAuth){
			return checkUrlMap.get("url").toString();
		}
		
		//查询工资导入导出可选项
		DictType dictType = dictTypeManager.findUniqueBy("name", "salaryColume");
		List<DictInfo> dictInfoList = dictInfoManager.findBy("dictType", dictType);
		model.addAttribute("dictInfoList", dictInfoList);
		model.addAttribute("param", param);
		return "user/salary/salary-base-export-excel";
	}
}
