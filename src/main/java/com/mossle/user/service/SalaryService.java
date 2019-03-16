package com.mossle.user.service;

import java.math.BigDecimal;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Joiner;
import com.mossle.common.utils.StringUtils;
import com.mossle.core.auth.CurrentUserHolder;
import com.mossle.core.mapper.BeanMapper;
import com.mossle.core.page.Page;
import com.mossle.core.spring.MessageHelper;
import com.mossle.dict.persistence.domain.DictInfo;
import com.mossle.dict.persistence.domain.DictType;
import com.mossle.dict.persistence.manager.DictInfoManager;
import com.mossle.dict.persistence.manager.DictTypeManager;
import com.mossle.party.PartyConstants;
import com.mossle.user.persistence.domain.AuthContractCompany;
import com.mossle.user.persistence.domain.PersonSalaryAccumulationFund;
import com.mossle.user.persistence.domain.PersonInfo;
import com.mossle.user.persistence.domain.PersonSalaryBase;
import com.mossle.user.persistence.domain.PersonSalarySocialSecurity;
import com.mossle.user.persistence.domain.PersonSalarySupport;
import com.mossle.user.persistence.manager.AuthContractCompanyManager;
import com.mossle.user.persistence.manager.PersonInfoManager;
import com.mossle.user.persistence.manager.PersonSalaryAccumulationFundManager;
import com.mossle.user.persistence.manager.PersonSalaryBaseManager;
import com.mossle.user.persistence.manager.PersonSalarySocialSecurityManager;
import com.mossle.user.persistence.manager.PersonSalarySupportManager;
import com.mossle.util.DateUtil;
import com.mossle.util.MailThread;
import com.mossle.util.MailUtil;
import com.mossle.util.ReflectUtil;
import com.mossle.util.StringUtil;
import com.mossle.ws.online.LogisticsUtil;

/**
 * 
 * @author ckx
 *
 */
@Service
@Transactional(readOnly = true)
public class SalaryService {
private static Logger logger = LoggerFactory.getLogger(SalaryService.class);
	
	@Autowired
    private JdbcTemplate jdbcTemplate;
	@Autowired
	private MessageHelper messageHelper;
	@Autowired
	private PersonSalaryBaseManager personSalaryBaseManager;
	@Autowired
	private PersonInfoManager personInfoManager;
	@Autowired
	private CurrentUserHolder currentUserHolder;
	@Autowired
	private PersonSalarySupportManager personSalarySupportManager;
	@Autowired
	private DictInfoManager dictInfoManager;
	@Autowired
	private DictTypeManager dictTypeManager;
	@Autowired
	private AuthContractCompanyManager authContractCompanyManager;
	@Autowired
	private PersonSalarySocialSecurityManager personSalarySocialSecurityManager;
	@Autowired
	private PersonSalaryAccumulationFundManager personSalaryAccumulationFundManager;
	private BeanMapper beanMapper = new BeanMapper();
	private String salaryDesKey;
	
	/**
	 * 发送工资条
	 * @param ids
	 * @param emailPassword
	 * @throws Exception 
	 */
	@Transactional(readOnly = false)
	public void sendPayslip(String mailServer,String ids, String emailPassword) throws Exception {
		
		//查询需要发送的工资条
		JSONArray parseArray = JSONObject.parseArray(ids);
		String[] split = parseArray.toJavaObject(String[].class);
		
		for (String id : split) {
			PersonInfo personInfo = null;
			PersonSalaryBase personSalaryBase = personSalaryBaseManager.findUniqueBy("id", Long.parseLong(id));
			String contractCompanyId = personSalaryBase.getContractCompanyId();
			String personId = personSalaryBase.getPersonId();
			if(StringUtils.isBlank(personId)){
				String idcardNum = personSalaryBase.getIdcardNum();
				if(StringUtils.isBlank(idcardNum)){
					continue;
				}else{
					personInfo = personInfoManager.findUniqueBy("identityID", idcardNum);
				}
			}else{
				//查询员工邮箱
				personInfo = personInfoManager.findUniqueBy("id", Long.parseLong(personId));
			}
			
			//无花名册信息，无邮箱
			if(null == personInfo){
				continue;
			}
			String email = personInfo.getEmail();
			//查询公司邮箱用户名
			Map<String, Object> personContractCompanyMap = jdbcTemplate.queryForMap("select * from person_contract_company where id = '"+contractCompanyId+"'");
			String userName = StringUtil.toString(personContractCompanyMap.get("company_email"));
			
			Map<String, String> map = getHtml(personSalaryBase);
			String html = map.get("html");
			String title = map.get("title");
			boolean sendEmail = MailUtil.sendEmail(mailServer, userName, emailPassword, email, null, null, title, html, null);
			if(!sendEmail){
				//错误跳过当前接收人
				continue;
			}else{
				//更新发送次数
				String sendCount = personSalaryBase.getSendCount();
				int parseInt = Integer.parseInt(sendCount);
				personSalaryBase.setSendCount(String.valueOf(parseInt+1));
				personSalaryBaseManager.update(personSalaryBase);
			}
		}
	}
	
	//拼接发送工资条
	private Map<String, String> getHtml(PersonSalaryBase personSalaryBase) throws Exception{
		String title = "";
		Map<String, String> map = new HashMap<String, String>();
		//1.拼接html
		String contractCompanyName = personSalaryBase.getContractCompanyName();
		String salaryYear = personSalaryBase.getSalaryYear();
		String salaryMonth = personSalaryBase.getSalaryMonth();
		String personName = personSalaryBase.getPersonName();
		String employeeNo = personSalaryBase.getEmployeeNo();
		String allAttendanceDays = decrypt(personSalaryBase.getAllAttendanceDays());//应出勤
		String actualAttendanceDays = decrypt(personSalaryBase.getActualAttendanceDays());//实际出勤
		String monthWagesMoney = decrypt(personSalaryBase.getMonthWagesMoney());//月工资
		String overtimePayMoney = decrypt(personSalaryBase.getOvertimePayMoney());//加班费
		String missingDeductionMoney = decrypt(personSalaryBase.getMissingDeductionMoney());//缺勤扣款
		String sickDeductionMoney = decrypt(personSalaryBase.getSickDeductionMoney());//病假扣款
		String casualDeductionMoney = decrypt(personSalaryBase.getCasualDeductionMoney());//事假扣款
		String absentDeductionMoney = decrypt(personSalaryBase.getAbsentDeductionMoney());//旷工扣款
		String earlyLateDeductionMoney = decrypt(personSalaryBase.getEarlyLateDeductionMoney());//迟到早退扣款
		String supplementItemsMoney = decrypt(personSalaryBase.getSupplementItemsMoney());//补杂项
		String allWagesMoney = decrypt(personSalaryBase.getAllWagesMoney());//应发工资
		String socialPensionDeductionMoney = decrypt(personSalaryBase.getSocialPensionDeductionMoney());//养老扣款
		String socialUnemploymentDeductionMoney = decrypt(personSalaryBase.getSocialUnemploymentDeductionMoney());//失业扣款
		String socialMedicalDeductionMoney = decrypt(personSalaryBase.getSocialMedicalDeductionMoney());//医疗扣款
		String socialProvidentFundDeductionMoney = decrypt(personSalaryBase.getSocialProvidentFundDeductionMoney());//公积金扣款
		String socialOtherDeductionMoney = decrypt(personSalaryBase.getSocialOtherDeductionMoney());//其他项扣款
		String socialTotalDeductionMoney = decrypt(personSalaryBase.getSocialTotalDeductionMoney());//保险合计
		String grossWagesWages = decrypt(personSalaryBase.getGrossWagesMoney());//税前工资
		String specialChildrenEducationMoney = decrypt(personSalaryBase.getSpecialChildrenEducationMoney());//子女教育
		String specialContinuEducationMoney = decrypt(personSalaryBase.getSpecialContinuEducationMoney());//继续教育
		String specialHotelInterestMoney = decrypt(personSalaryBase.getSpecialHotelInterestMoney());//住房贷款利息
		String specialHotelRentMoney = decrypt(personSalaryBase.getSpecialHotelRentMoney());//住房租金
		String specialSupportElderlyMoney = decrypt(personSalaryBase.getSpecialSupportElderlyMoney());//赡养老人
		String specialCommercialHealthInsuranceMoney = decrypt(personSalaryBase.getSpecialCommercialHealthInsuranceMoney());//商业健康险
		String personalIncomeMoney = decrypt(personSalaryBase.getPersonalIncomeMoney());//个人所得税
		String realWagesMoney = decrypt(personSalaryBase.getRealWagesMoney());//实发工资
		String remark = decrypt(personSalaryBase.getRemark());//备注
		
		title = contractCompanyName+"公司 "+personName+" "+salaryYear+"年"+salaryMonth+"月工资条";
		String strTdStyle="border:1px solid #7c7c7c;text-align:center;white-space:nowrap;padding:0 1em 0;";
		String strTdHeadStyle=strTdStyle+"font-weight:bold;background:#d9d9d9;";
		String html = "<div style='overflow: auto; width: 100%;'><table border='0' style='line-height:34px;border-collapse:collapse' cellpadding='0' cellspacing='0' >"
				+"<tr>"
					+"<th colspan='29' style='"+strTdHeadStyle+"' align='center'>"+title+"</th>"
				+"</tr>"
				+"<tr>"
					+"<td rowspan='2' style='"+strTdHeadStyle+"' align='center'>工号</td>"
					+"<td rowspan='2' style='"+strTdHeadStyle+"' align='center'>姓名</td>"
					+"<td rowspan='2' style='"+strTdHeadStyle+"' align='center'>应出勤</td>"
					+"<td rowspan='2' style='"+strTdHeadStyle+"' align='center'>实际出勤</td>"
					+"<td rowspan='2' style='"+strTdHeadStyle+"' align='center'>月工资</td>"
					+"<td rowspan='2' style='"+strTdHeadStyle+"' align='center'>加班费</td>"
					+"<td colspan='5' style='"+strTdHeadStyle+"' align='center'>请假扣款</td>"
					+"<td rowspan='2' style='"+strTdHeadStyle+"' align='center'>补杂项</td>"
					+"<td rowspan='2' style='"+strTdHeadStyle+"' align='center'>应发工资</td>"
					+"<td colspan='6' style='"+strTdHeadStyle+"' align='center'>保险公积金扣款</td>"
					+"<td rowspan='2' style='"+strTdHeadStyle+"' align='center'>税前工资</td>"
					+"<td colspan='6' style='"+strTdHeadStyle+"' align='center'>专项附加扣除项目</td>"
					+"<td rowspan='2' style='"+strTdHeadStyle+"' align='center'>个人所得税</td>"
					+"<td rowspan='2' style='"+strTdHeadStyle+"' align='center'>实发工资</td>"
					+"<td rowspan='2' style='"+strTdHeadStyle+"' align='center'>备注</td>"
				+"</tr>"
				+"<tr>"
					+"<td style='"+strTdHeadStyle+"' align='center'>缺勤</td>"
					+"<td style='"+strTdHeadStyle+"' align='center'>病假</td>"
					+"<td style='"+strTdHeadStyle+"' align='center'>事假</td>"
					+"<td style='"+strTdHeadStyle+"' align='center'>旷工</td>"
					+"<td style='"+strTdHeadStyle+"' align='center'>迟到早退</td>"
					+"<td style='"+strTdHeadStyle+"' align='center'>养老</td>"
					+"<td style='"+strTdHeadStyle+"' align='center'>失业</td>"
					+"<td style='"+strTdHeadStyle+"' align='center'>医疗</td>"
					+"<td style='"+strTdHeadStyle+"' align='center'>公积金</td>"
					+"<td style='"+strTdHeadStyle+"' align='center'>其他项 </td>"
					+"<td style='"+strTdHeadStyle+"' align='center'>合计</td>"
					+"<td style='"+strTdHeadStyle+"' align='center'>子女教育</td>"
					+"<td style='"+strTdHeadStyle+"' align='center'>继续教育</td>"
					+"<td style='"+strTdHeadStyle+"' align='center'>住房贷款利息</td>"
					+"<td style='"+strTdHeadStyle+"' align='center'>住房租金</td>"
					+"<td style='"+strTdHeadStyle+"' align='center'>赡养老人</td>"
					+"<td style='"+strTdHeadStyle+"' align='center'>商业健康险</td>"
				+"</tr>"
				+"<tr>"
					+"<td style='"+strTdStyle+"' align='center'>"+employeeNo+"</td>"
					+"<td style='"+strTdStyle+"' align='center'>"+personName+"</td>"
					+"<td style='"+strTdStyle+"' align='center'>"+allAttendanceDays+"</td>"
					+"<td style='"+strTdStyle+"' align='center'>"+actualAttendanceDays+"</td>"
					+"<td style='"+strTdStyle+"' align='center'>"+monthWagesMoney+"</td>"
					+"<td style='"+strTdStyle+"' align='center'>"+overtimePayMoney+"</td>"
					+"<td style='"+strTdStyle+"' align='center'>"+missingDeductionMoney+"</td>"
					+"<td style='"+strTdStyle+"' align='center'>"+sickDeductionMoney+"</td>"
					+"<td style='"+strTdStyle+"' align='center'>"+casualDeductionMoney+"</td>"
					+"<td style='"+strTdStyle+"' align='center'>"+absentDeductionMoney+"</td>"
					+"<td style='"+strTdStyle+"' align='center'>"+earlyLateDeductionMoney+"</td>"
					+"<td style='"+strTdStyle+"' align='center'>"+supplementItemsMoney+"</td>"
					+"<td style='"+strTdStyle+"' align='center'>"+allWagesMoney+"</td>"
					+"<td style='"+strTdStyle+"' align='center'>"+socialPensionDeductionMoney+"</td>"
					+"<td style='"+strTdStyle+"' align='center'>"+socialUnemploymentDeductionMoney+"</td>"
					+"<td style='"+strTdStyle+"' align='center'>"+socialMedicalDeductionMoney+"</td>"
					+"<td style='"+strTdStyle+"' align='center'>"+socialProvidentFundDeductionMoney+"</td>"
					+"<td style='"+strTdStyle+"' align='center'>"+socialOtherDeductionMoney+"</td>"
					+"<td style='"+strTdStyle+"' align='center'>"+socialTotalDeductionMoney+"</td>"
					+"<td style='"+strTdStyle+"' align='center'>"+grossWagesWages+"</td>"
					+"<td style='"+strTdStyle+"' align='center'>"+specialChildrenEducationMoney+"</td>"
					+"<td style='"+strTdStyle+"' align='center'>"+specialContinuEducationMoney+"</td>"
					+"<td style='"+strTdStyle+"' align='center'>"+specialHotelInterestMoney+"</td>"
					+"<td style='"+strTdStyle+"' align='center'>"+specialHotelRentMoney+"</td>"
					+"<td style='"+strTdStyle+"' align='center'>"+specialSupportElderlyMoney+"</td>"
					+"<td style='"+strTdStyle+"' align='center'>"+specialCommercialHealthInsuranceMoney+"</td>"
					+"<td style='"+strTdStyle+"' align='center'>"+personalIncomeMoney+"</td>"
					+"<td style='"+strTdStyle+"' align='center'>"+realWagesMoney+"</td>"
					+"<td style='"+strTdStyle+"' align='center'>"+remark+"</td>"
				+"</tr>"
			+"</table></div>";
		map.put("title", title);
		map.put("html", html);
		return map;
		
	}
	
	
	/**
	 * 删除公积金扣款表数据
	 * @param id
	 * @return
	 * @author ckx
	 */
	@Transactional(readOnly = false)
	public boolean deleteSalaryAccumulationFundById(String id) {
		String name = currentUserHolder.getName();
		try {
			PersonSalaryAccumulationFund personSalaryAccumulationFund = personSalaryAccumulationFundManager.findUniqueBy("id", Long.parseLong(id));
			String systemRemark = personSalaryAccumulationFund.getSystemRemark();
			systemRemark += name+"删除该条数据,";
			personSalaryAccumulationFund.setSystemRemark(systemRemark);
			personSalaryAccumulationFund.setDelFlag("1");
			personSalarySocialSecurityManager.update(personSalaryAccumulationFund);
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	
	/**
	 * 编辑公积金扣款表
	 * @param personSalaryBase
	 * @throws Exception 
	 * @author ckx
	 */
	@Transactional(readOnly = false)
	public void editSalaryAccumulationFund(PersonSalaryAccumulationFund personSalaryAccumulationFund) throws Exception {
		String name = currentUserHolder.getName();
		PersonSalaryAccumulationFund oldPersonSalaryAccumulationFund = personSalaryAccumulationFundManager.findUniqueBy("id", personSalaryAccumulationFund.getId());
		oldPersonSalaryAccumulationFund.setIdcardNum(personSalaryAccumulationFund.getIdcardNum());
		oldPersonSalaryAccumulationFund.setAccumulationFundBaseMoney(LogisticsUtil.encryptThreeDESECB(getNumber(personSalaryAccumulationFund.getAccumulationFundBaseMoney()), salaryDesKey));
		oldPersonSalaryAccumulationFund.setAccumulationFundCompanyProportion(LogisticsUtil.encryptThreeDESECB(personSalaryAccumulationFund.getAccumulationFundCompanyProportion(), salaryDesKey));
		oldPersonSalaryAccumulationFund.setAccumulationFundCompanyMoney(LogisticsUtil.encryptThreeDESECB(getNumber(personSalaryAccumulationFund.getAccumulationFundCompanyMoney()), salaryDesKey));
		oldPersonSalaryAccumulationFund.setAccumulationFundPersonalProportion(LogisticsUtil.encryptThreeDESECB(personSalaryAccumulationFund.getAccumulationFundPersonalProportion(), salaryDesKey));
		oldPersonSalaryAccumulationFund.setAccumulationFundPersonalMoney(LogisticsUtil.encryptThreeDESECB(getNumber(personSalaryAccumulationFund.getAccumulationFundPersonalMoney()), salaryDesKey));
		oldPersonSalaryAccumulationFund.setTotalMoney(LogisticsUtil.encryptThreeDESECB(getNumber(personSalaryAccumulationFund.getTotalMoney()), salaryDesKey));

		//查询当前导入上一月份的数据
		String lastMonth = DateUtil.getLastMonth(oldPersonSalaryAccumulationFund.getAccumulationFundDate(), 0, -1, 0);
		PersonSalaryAccumulationFund personSalaryAccumulationFundLast = personSalaryAccumulationFundManager.findUnique("from PersonSalaryAccumulationFund where delFlag = '0' and idcardNum = ? and accumulationFundDate = ?",oldPersonSalaryAccumulationFund.getIdcardNum(),lastMonth);
		//对比是否有数据需要进行标识颜色
		setAccumulationFundColor(name,oldPersonSalaryAccumulationFund,personSalaryAccumulationFundLast,"edit");
		String idcardNum = oldPersonSalaryAccumulationFund.getIdcardNum();
		//查询person_info
		PersonInfo personInfo = personInfoManager.findUniqueBy("identityID", idcardNum);
		if(null != personInfo){
			oldPersonSalaryAccumulationFund.setIdcardNumColor("0");
		}else{
			oldPersonSalaryAccumulationFund.setIdcardNumColor("1");
		}
		personSalarySocialSecurityManager.update(oldPersonSalaryAccumulationFund);
	}
	
	/**
	 * 公积金扣款基本表展示
	 * @return
	 * @throws Exception 
	 * @author ckx
	 */
	public List<PersonSalaryAccumulationFund> salaryAccumulationFundListSub(String postId,String contractCompanyId,
			String startDate, String endDate,String personName) throws Exception {
		//判断当前人员是否有权限查看数据
		boolean isCompany = false;
		List<Map<String, Object>> allContractCompanyName = getAllContractCompanyName();
		if(null == allContractCompanyName || allContractCompanyName.size() == 0){
			return null;
		}else{
			for (Map<String, Object> map : allContractCompanyName) {
				String companyId = StringUtil.toString(map.get("contract_company_id"));
				if(contractCompanyId.equals(companyId)){
					isCompany = true;
				}
			}
		}
		if(!isCompany){
			return null;
		}
		List<String> allPersonList=new ArrayList<String>();
		boolean isSearch = false;
		String hql = "from PersonSalaryAccumulationFund s where ";
		hql += " s.delFlag = ? ";
		
		if(StringUtils.isNotBlank(postId)){
			String[] split = postId.split(",");
			for (String id : split) {
				List<String> allPersonById = getAllPersonById(Long.parseLong(id));
				allPersonList.addAll(allPersonById);
			}
			String personIds=Joiner.on(",").join(allPersonList);
			hql = " select s from PersonSalaryAccumulationFund s , PersonInfo p  where  s.idcardNum = p.identityID and s.delFlag = ? and p.id in ("+personIds+")";
			isSearch = true;
		}
		if(StringUtils.isNotBlank(contractCompanyId)){
			hql += " and s.contractCompanyId = '"+contractCompanyId+"' ";
			isSearch = true;
		}
		if(StringUtils.isNotBlank(startDate)){
			hql += " and s.accumulationFundDate >= '"+startDate+"' ";
			isSearch = true;
		}
		if(StringUtils.isNotBlank(endDate)){
			hql += " and s.accumulationFundDate <= '"+endDate+"' ";
			isSearch = true;
		}
		if(StringUtils.isNotBlank(personName)){
			hql += " and s.personName like '%"+personName+"%' ";
			isSearch = true;
		}
		hql += " order by s.id ";
		//默认加载不查询
		if(!isSearch){
			return null;
		}
		List<PersonSalaryAccumulationFund> personSalaryAccumulationFundList = personSalaryAccumulationFundManager.find(hql, "0");
		for (PersonSalaryAccumulationFund personSalaryAccumulationFund : personSalaryAccumulationFundList) {
			//解密参数
			personSalaryAccumulationFund.setAccumulationFundBaseMoney(decrypt(personSalaryAccumulationFund.getAccumulationFundBaseMoney()));
			personSalaryAccumulationFund.setAccumulationFundCompanyProportion(decrypt(personSalaryAccumulationFund.getAccumulationFundCompanyProportion()));
			personSalaryAccumulationFund.setAccumulationFundCompanyMoney(decrypt(personSalaryAccumulationFund.getAccumulationFundCompanyMoney()));
			personSalaryAccumulationFund.setAccumulationFundPersonalProportion(decrypt(personSalaryAccumulationFund.getAccumulationFundPersonalProportion()));
			personSalaryAccumulationFund.setAccumulationFundPersonalMoney(decrypt(personSalaryAccumulationFund.getAccumulationFundPersonalMoney()));
			personSalaryAccumulationFund.setTotalMoney(decrypt(personSalaryAccumulationFund.getTotalMoney()));
			//String idcardNum = personSalarySocialSecurity.getIdcardNum();
			//查询person_info
			/*PersonInfo personInfo = personInfoManager.findUniqueBy("identityID", idcardNum);
			if(null == personInfo){
			}*/
		}
		return personSalaryAccumulationFundList;
	}
	
	/**
	 * 生成下一月份的数据 公积金
	 * @param contractCompanyId
	 * @param date
	 * @param year
	 * @param month
	 * @return
	 */
	@Transactional(readOnly = false)
	public void salaryAccumulationFundCreateMonthData(
			String contractCompanyId,String currentDate, String nextDate, String year, String month) throws Exception{
		 String name = currentUserHolder.getName();
		 List<PersonSalaryAccumulationFund> personSalaryAccumulationFundList = personSalaryAccumulationFundManager.find("from PersonSalaryAccumulationFund where delFlag = '0' and contractCompanyId = ? and accumulationFundDate = ? ", contractCompanyId,currentDate);
		 for (PersonSalaryAccumulationFund personSalaryAccumulationFund : personSalaryAccumulationFundList) {
			 //判断在时间段内，人员是否离职
			 String idcardNum = personSalaryAccumulationFund.getIdcardNum();
			 PersonInfo personInfo = personInfoManager.findUniqueBy("identityID", idcardNum);
			 if(null != personInfo){
				 String quitFlag = personInfo.getQuitFlag();
				 if("1".equals(quitFlag)){
					//拼接年月日为离职人员做
			    	int intYear=Integer.parseInt(year);
			 	    int intMonth=Integer.parseInt(month);
			 	    Calendar calSearch=Calendar.getInstance();
			 	    //下个月的第一天
			 	    calSearch.set(intYear, intMonth, 1);
					Date quitDate = personInfo.getLeaveDate();
					String formatDate = DateUtil.formatDate(quitDate, "");
					formatDate = formatDate.substring(0, 10);
					quitDate = DateUtil.formatDateStr(formatDate+" 18:00:00", "");
					Calendar calQuit=Calendar.getInstance();
					calQuit.setTime(quitDate);
					int intQuitYear=calQuit.get(Calendar.YEAR);
					int intQuitMonth=calQuit.get(Calendar.MONTH)+1;
					if(intQuitMonth>11){
						intQuitYear+=1;
						intQuitMonth=0;
					}
					calQuit.set(intQuitYear,intQuitMonth,1);
					//如果生成数据时间大于离职时间
					if(calSearch.after(calQuit)){
						continue;
					}else{
						personSalaryAccumulationFund.setQuitFlag("离职");
					}
				 } 
				 personSalaryAccumulationFund.setIdcardNumColor("0");
			 }else{
				 personSalaryAccumulationFund.setIdcardNumColor("1");
			 }
			 PersonSalaryAccumulationFund personSalaryAccumulationFundNew = new PersonSalaryAccumulationFund();
			 beanMapper.copy(personSalaryAccumulationFund, personSalaryAccumulationFundNew);
			 personSalaryAccumulationFundNew.setId(null);
			 personSalaryAccumulationFundNew.setAccumulationFundDate(nextDate);
			 personSalaryAccumulationFundNew.setAccumulationFundYear(year);
			 personSalaryAccumulationFundNew.setAccumulationFundMonth(month);
			 setAccumulationFundColor(name,personSalaryAccumulationFundNew,personSalaryAccumulationFund,"create");
			 personSalarySocialSecurityManager.save(personSalaryAccumulationFundNew);
		}
	}
	
	/**
	 * 查询当前薪资单位下的最大月份 公积金
	 * @param contractCompanyId
	 * @return
	 * @throws Exception 
	 * ckx
	 */
	public Map<String, Object> queryAccumulationFundMonthByContractCompanyId(String contractCompanyId) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		Page pagedQuery = personSalaryAccumulationFundManager.pagedQuery("from PersonSalaryAccumulationFund where delFlag = '0' and contractCompanyId = ? order by accumulationFundDate desc",1,1, contractCompanyId);
		List<PersonSalaryAccumulationFund> personSalaryAccumulationFundList = (List<PersonSalaryAccumulationFund>) pagedQuery.getResult();
		if(null != personSalaryAccumulationFundList && personSalaryAccumulationFundList.size() > 0){
			String accumulationFundDate = personSalaryAccumulationFundList.get(0).getAccumulationFundDate();
			String nextMonth = DateUtil.getLastMonth(accumulationFundDate, 0, 1, 0);
			String year = nextMonth.substring(0, 4);
			String month = nextMonth.substring(nextMonth.length()-2, nextMonth.length());
			map.put("currentDate", accumulationFundDate);
			map.put("nextDate", nextMonth);
			map.put("year", year);
			map.put("month", month);
			map.put("boo", true);
		}else{
			map.put("boo", false);
		}
		return map;
	}
	
	/**
	 * 导入公积金扣款明细
	 * @param contractCompanyId
	 * @param salaryFundList
	 * @param failSize
	 * @return
	 * @throws Exception 
	 */
	@Transactional(readOnly = false)
	public List<Map<String, Object>> importSalaryAccumulationFund(String contractCompanyId,
			List<Map<String, Object>> salaryAccumulationFundList) throws Exception {
		Map<String, Object> queryForMap = jdbcTemplate.queryForMap("select * from person_contract_company where id = '"+contractCompanyId+"'");
		String contractCompanyName = StringUtil.toString(queryForMap.get("contract_company_name"));
		
		String name = currentUserHolder.getName();
		List<Map<String, Object>> failList = new ArrayList<Map<String,Object>>();
		
		for (Map<String, Object> salaryAccumulationFundMap : salaryAccumulationFundList) {
			String accumulationFundDate = StringUtil.toString(salaryAccumulationFundMap.get("0"));
			String accumulationFundYear = "";
			String accumulationFundMonth = "";
			if(StringUtils.isNotBlank(accumulationFundDate)){
				accumulationFundYear = accumulationFundDate.substring(0, 4);
				accumulationFundMonth = accumulationFundDate.substring(accumulationFundDate.length()-2, accumulationFundDate.length());
			}
			String personName = StringUtil.toString(salaryAccumulationFundMap.get("1"));
			String idcardNum = StringUtil.toString(salaryAccumulationFundMap.get("2"));
			String accumulationFundBaseMoney = getNumber(StringUtil.toString(salaryAccumulationFundMap.get("3")));
			String accumulationFundCompanyProportion = StringUtil.toString(salaryAccumulationFundMap.get("4"));
			String accumulationFundCompanyMoney = getNumber(StringUtil.toString(salaryAccumulationFundMap.get("5")));
			String accumulationFundPersonalProportion = StringUtil.toString(salaryAccumulationFundMap.get("6"));
			String accumulationFundPersonalMoney = getNumber(StringUtil.toString(salaryAccumulationFundMap.get("7")));
			String totalMoney = getNumber(StringUtil.toString(salaryAccumulationFundMap.get("8")));
			
			//查询person_info
			PersonInfo personInfo = personInfoManager.findUniqueBy("identityID", idcardNum);
			//查询当前导入月份的数据
			PersonSalaryAccumulationFund personSalaryAccumulationFund = personSalaryAccumulationFundManager.findUnique("from PersonSalaryAccumulationFund where delFlag = '0' and idcardNum = ? and accumulationFundDate = ?",idcardNum,accumulationFundDate);
			//查询当前导入上一月份的数据
			String lastMonth = DateUtil.getLastMonth(accumulationFundDate, 0, -1, 0);
			PersonSalaryAccumulationFund personSalaryAccumulationFundLast = personSalaryAccumulationFundManager.findUnique("from PersonSalaryAccumulationFund where delFlag = '0' and idcardNum = ? and accumulationFundDate = ?",idcardNum,lastMonth);
			//查询当前导入上上一月份的数据
			String lastMonths = DateUtil.getLastMonth(lastMonth, 0, -1, 0);
			PersonSalaryAccumulationFund personSalaryAccumulationFundLasts = personSalaryAccumulationFundManager.findUnique("from PersonSalaryAccumulationFund where delFlag = '0' and idcardNum = ? and accumulationFundDate = ?",idcardNum,lastMonths);
			
			
			//判断当前导入数据是否隔月，如果隔月，则不允许导入
			if(null != personSalaryAccumulationFundLasts && null == personSalaryAccumulationFundLast && null != personInfo){
				//导入失败的数据
				Map<String,Object> failMap = new HashMap<String,Object>();
				
				failMap.put("userCode", personInfo.getEmployeeNo());
				failMap.put("userName", personInfo.getFullName());
				failMap.put("departmentName", personInfo.getDepartmentName());
				failMap.put("salaryDate", accumulationFundDate);
				failMap.put("failReason","导入数据隔月");
				failList.add(failMap);
				continue;
			}
			
			if(null == personSalaryAccumulationFund){
				personSalaryAccumulationFund = new PersonSalaryAccumulationFund();
			}
			//取花名册数据
			if(null != personInfo){
				personSalaryAccumulationFund.setPersonId(String.valueOf(personInfo.getId()));
				if("1".equals(personSalaryAccumulationFund.getQuitFlag())){
					personSalaryAccumulationFund.setQuitFlag("离职");
				}else{
					personSalaryAccumulationFund.setQuitFlag("在职");
				}
			}else{
				personSalaryAccumulationFund.setIdcardNumColor("1");
			}
			personSalaryAccumulationFund.setPersonName(personName);
			personSalaryAccumulationFund.setAccumulationFundDate(accumulationFundDate);
			personSalaryAccumulationFund.setAccumulationFundYear(accumulationFundYear);
			personSalaryAccumulationFund.setAccumulationFundMonth(accumulationFundMonth);
			personSalaryAccumulationFund.setContractCompanyId(contractCompanyId);
			personSalaryAccumulationFund.setContractCompanyName(contractCompanyName);
			personSalaryAccumulationFund.setIdcardNum(idcardNum);
			personSalaryAccumulationFund.setAccumulationFundBaseMoney(LogisticsUtil.encryptThreeDESECB(accumulationFundBaseMoney, salaryDesKey));
			personSalaryAccumulationFund.setAccumulationFundCompanyProportion(LogisticsUtil.encryptThreeDESECB(accumulationFundCompanyProportion, salaryDesKey));
			personSalaryAccumulationFund.setAccumulationFundCompanyMoney(LogisticsUtil.encryptThreeDESECB(accumulationFundCompanyMoney, salaryDesKey));
			personSalaryAccumulationFund.setAccumulationFundPersonalProportion(LogisticsUtil.encryptThreeDESECB(accumulationFundPersonalProportion, salaryDesKey));
			personSalaryAccumulationFund.setAccumulationFundPersonalMoney(LogisticsUtil.encryptThreeDESECB(accumulationFundPersonalMoney, salaryDesKey));
			personSalaryAccumulationFund.setTotalMoney(LogisticsUtil.encryptThreeDESECB(totalMoney, salaryDesKey));
			//对比不同数据，颜色标识
			setAccumulationFundColor(name,personSalaryAccumulationFund,personSalaryAccumulationFundLast,"import");
			
			personSalaryAccumulationFundManager.save(personSalaryAccumulationFund);
		}
		return failList;
	}
	//公积金与上个月对比，不同数据标识
	private void setAccumulationFundColor(String name,PersonSalaryAccumulationFund personSalaryAccumulationFund,
			PersonSalaryAccumulationFund personSalaryAccumulationFundLast,String type) {
		String remarkStr = "";
		if("import".equals(type)){
			remarkStr = "导入"+personSalaryAccumulationFund.getAccumulationFundYear()+"年"+personSalaryAccumulationFund.getAccumulationFundMonth()+"月数据,";
		}else if("edit".equals(type)){
			remarkStr = "修改"+personSalaryAccumulationFund.getAccumulationFundYear()+"年"+personSalaryAccumulationFund.getAccumulationFundMonth()+"月数据,";
		}else if("create".equals(type)){
			remarkStr = "生成"+personSalaryAccumulationFund.getAccumulationFundYear()+"年"+personSalaryAccumulationFund.getAccumulationFundMonth()+"月数据,";
		}
		if(null != personSalaryAccumulationFundLast){
			if(!personSalaryAccumulationFundLast.getAccumulationFundBaseMoney().equals(personSalaryAccumulationFund.getAccumulationFundBaseMoney())){
				personSalaryAccumulationFund.setAccumulationFundBaseMoneyColor("1");
			}else{
				personSalaryAccumulationFund.setAccumulationFundBaseMoneyColor("0");
			}
			if(!personSalaryAccumulationFundLast.getAccumulationFundCompanyProportion().equals(personSalaryAccumulationFund.getAccumulationFundCompanyProportion())){
				personSalaryAccumulationFund.setAccumulationFundCompanyProportionColor("1");
			}else{
				personSalaryAccumulationFund.setAccumulationFundCompanyProportionColor("0");
			}
			if(!personSalaryAccumulationFundLast.getAccumulationFundCompanyMoney().equals(personSalaryAccumulationFund.getAccumulationFundCompanyMoney())){
				personSalaryAccumulationFund.setAccumulationFundCompanyMoneyColor("1");
			}else{
				personSalaryAccumulationFund.setAccumulationFundCompanyMoneyColor("0");
			}
			if(!personSalaryAccumulationFundLast.getAccumulationFundPersonalProportion().equals(personSalaryAccumulationFund.getAccumulationFundPersonalProportion())){
				personSalaryAccumulationFund.setAccumulationFundPersonalProportionColor("1");
			}else{
				personSalaryAccumulationFund.setAccumulationFundPersonalProportionColor("0");
			}
			if(!personSalaryAccumulationFundLast.getAccumulationFundPersonalMoney().equals(personSalaryAccumulationFund.getAccumulationFundPersonalMoney())){
				personSalaryAccumulationFund.setAccumulationFundPersonalMoneyColor("1");
			}else{
				personSalaryAccumulationFund.setAccumulationFundPersonalMoneyColor("0");
			}
		}/*else{
			personSalaryAccumulationFund.setAccumulationFundBaseMoneyColor("0");
			personSalaryAccumulationFund.setAccumulationFundCompanyProportionColor("0");
			personSalaryAccumulationFund.setAccumulationFundCompanyMoneyColor("0");
			personSalaryAccumulationFund.setAccumulationFundPersonalProportionColor("0");
			personSalaryAccumulationFund.setAccumulationFundPersonalMoneyColor("0");
		}*/
		String systemRemark = personSalaryAccumulationFund.getSystemRemark();
		systemRemark += name+remarkStr;
		personSalaryAccumulationFund.setSystemRemark(systemRemark);
	}
	
	/**
	 * 删除社保扣款表数据
	 * @param id
	 * @return
	 * @author ckx
	 */
	@Transactional(readOnly = false)
	public boolean deleteSalarySocialSecurityById(String id) {
		String name = currentUserHolder.getName();
		try {
			PersonSalarySocialSecurity personSalarySocialSecurity = personSalarySocialSecurityManager.findUniqueBy("id", Long.parseLong(id));
			String systemRemark = personSalarySocialSecurity.getSystemRemark();
			systemRemark += name+"删除该条数据,";
			personSalarySocialSecurity.setSystemRemark(systemRemark);
			personSalarySocialSecurity.setDelFlag("1");
			personSalarySocialSecurityManager.update(personSalarySocialSecurity);
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	
	/**
	 * 编辑社保扣款表
	 * @param personSalaryBase
	 * @throws Exception 
	 * @author ckx
	 */
	@Transactional(readOnly = false)
	public void editSalarySocialSecurity(PersonSalarySocialSecurity personSalarySocialSecurity) throws Exception {
		String name = currentUserHolder.getName();
		PersonSalarySocialSecurity oldPersonSalarySocialSecurity = personSalarySocialSecurityManager.findUniqueBy("id", personSalarySocialSecurity.getId());
		oldPersonSalarySocialSecurity.setIdcardNum(personSalarySocialSecurity.getIdcardNum());
		oldPersonSalarySocialSecurity.setAccountCharacte(personSalarySocialSecurity.getAccountCharacte());
		oldPersonSalarySocialSecurity.setPensionBaseMoney(LogisticsUtil.encryptThreeDESECB(getNumber(personSalarySocialSecurity.getPensionBaseMoney()), salaryDesKey));
		oldPersonSalarySocialSecurity.setPensionCompanyProportion(LogisticsUtil.encryptThreeDESECB(personSalarySocialSecurity.getPensionCompanyProportion(), salaryDesKey));
		oldPersonSalarySocialSecurity.setPensionCompanyMoney(LogisticsUtil.encryptThreeDESECB(getNumber(personSalarySocialSecurity.getPensionCompanyMoney()), salaryDesKey));
		oldPersonSalarySocialSecurity.setPensionPersonalProportion(LogisticsUtil.encryptThreeDESECB(personSalarySocialSecurity.getPensionPersonalProportion(), salaryDesKey));
		oldPersonSalarySocialSecurity.setPensionPersonalMoney(LogisticsUtil.encryptThreeDESECB(getNumber(personSalarySocialSecurity.getPensionPersonalMoney()), salaryDesKey));
		oldPersonSalarySocialSecurity.setUnemploymentPersonalProportion(LogisticsUtil.encryptThreeDESECB(personSalarySocialSecurity.getUnemploymentCompanyProportion(), salaryDesKey));
		oldPersonSalarySocialSecurity.setUnemploymentPersonalMoney(LogisticsUtil.encryptThreeDESECB(getNumber(personSalarySocialSecurity.getUnemploymentCompanyMoney()), salaryDesKey));
		oldPersonSalarySocialSecurity.setUnemploymentPersonalProportion(LogisticsUtil.encryptThreeDESECB(personSalarySocialSecurity.getUnemploymentPersonalProportion(), salaryDesKey));
		oldPersonSalarySocialSecurity.setUnemploymentPersonalMoney(LogisticsUtil.encryptThreeDESECB(getNumber(personSalarySocialSecurity.getUnemploymentPersonalMoney()), salaryDesKey));
		oldPersonSalarySocialSecurity.setMedicalBaseMoney(LogisticsUtil.encryptThreeDESECB(getNumber(personSalarySocialSecurity.getMedicalBaseMoney()), salaryDesKey));
		oldPersonSalarySocialSecurity.setMedicalCompanyProportion(LogisticsUtil.encryptThreeDESECB(personSalarySocialSecurity.getMedicalCompanyProportion(), salaryDesKey));
		oldPersonSalarySocialSecurity.setMedicalCompanyMoney(LogisticsUtil.encryptThreeDESECB(getNumber(personSalarySocialSecurity.getMedicalCompanyMoney()), salaryDesKey));
		oldPersonSalarySocialSecurity.setMedicalPersonalProportion(LogisticsUtil.encryptThreeDESECB(personSalarySocialSecurity.getMedicalPersonalProportion(), salaryDesKey));
		oldPersonSalarySocialSecurity.setMedicalPersonalMoney(LogisticsUtil.encryptThreeDESECB(getNumber(personSalarySocialSecurity.getMedicalPersonalMoney()), salaryDesKey));
		oldPersonSalarySocialSecurity.setInjuryCompanyProportion(LogisticsUtil.encryptThreeDESECB(personSalarySocialSecurity.getInjuryCompanyProportion(), salaryDesKey));
		oldPersonSalarySocialSecurity.setInjuryCompanyMoney(LogisticsUtil.encryptThreeDESECB(getNumber(personSalarySocialSecurity.getInjuryCompanyMoney()), salaryDesKey));
		oldPersonSalarySocialSecurity.setBirthCompanyProportion(LogisticsUtil.encryptThreeDESECB(personSalarySocialSecurity.getBirthCompanyProportion(), salaryDesKey));
		oldPersonSalarySocialSecurity.setBirthCompanyMoney(LogisticsUtil.encryptThreeDESECB(getNumber(personSalarySocialSecurity.getBirthCompanyMoney()), salaryDesKey));

		//查询当前导入上一月份的数据
		String lastMonth = DateUtil.getLastMonth(oldPersonSalarySocialSecurity.getSocialSecurityDate(), 0, -1, 0);
		PersonSalarySocialSecurity personSalarySocialSecurityLast = personSalarySocialSecurityManager.findUnique("from PersonSalarySocialSecurity where idcardNum = ? and socialSecurityDate = ?",oldPersonSalarySocialSecurity.getIdcardNum(),lastMonth);
		//对比是否有数据需要进行标识颜色
		setSocialSecurityColor(name,oldPersonSalarySocialSecurity,personSalarySocialSecurityLast,"edit");
		String idcardNum = oldPersonSalarySocialSecurity.getIdcardNum();
		//查询person_info
		PersonInfo personInfo = personInfoManager.findUniqueBy("identityID", idcardNum);
		if(null != personInfo){
			oldPersonSalarySocialSecurity.setIdcardNumColor("0");
		}else{
			oldPersonSalarySocialSecurity.setIdcardNumColor("1");
		}
		personSalarySocialSecurityManager.update(oldPersonSalarySocialSecurity);
	}
	
	/**
	 * 社保基本表展示
	 * @return
	 * @throws Exception 
	 * @author ckx
	 */
	public List<PersonSalarySocialSecurity> salarySocialSecurityListSub(String postId,String contractCompanyId,
			String startDate, String endDate,String personName) throws Exception {
		//判断当前人员是否有权限查看数据
		boolean isCompany = false;
		List<Map<String, Object>> allContractCompanyName = getAllContractCompanyName();
		if(null == allContractCompanyName || allContractCompanyName.size() == 0){
			return null;
		}else{
			for (Map<String, Object> map : allContractCompanyName) {
				String companyId = StringUtil.toString(map.get("contract_company_id"));
				if(contractCompanyId.equals(companyId)){
					isCompany = true;
				}
			}
		}
		if(!isCompany){
			return null;
		}
		
		List<String> allPersonList=new ArrayList<String>();
		boolean isSearch = false;
		String hql = "from PersonSalarySocialSecurity s where ";
		hql += " s.delFlag = ? ";
		if(StringUtils.isNotBlank(postId)){
			String[] split = postId.split(",");
			for (String id : split) {
				List<String> allPersonById = getAllPersonById(Long.parseLong(id));
				allPersonList.addAll(allPersonById);
			}
			String personIds=Joiner.on(",").join(allPersonList);
			hql = " select s from PersonSalarySocialSecurity s , PersonInfo p  where  s.idcardNum = p.identityID and s.delFlag = ? and p.id in ("+personIds+")";
			isSearch = true;
		}
		if(StringUtils.isNotBlank(contractCompanyId)){
			hql += " and s.contractCompanyId = '"+contractCompanyId+"' ";
			isSearch = true;
		}
		if(StringUtils.isNotBlank(startDate)){
			hql += " and s.socialSecurityDate >= '"+startDate+"' ";
			isSearch = true;
		}
		if(StringUtils.isNotBlank(endDate)){
			hql += " and s.socialSecurityDate <= '"+endDate+"' ";
			isSearch = true;
		}
		if(StringUtils.isNotBlank(personName)){
			hql += " and s.personName like '%"+personName+"%' ";
			isSearch = true;
		}
		hql += " order by s.id ";
		//默认加载不查询
		if(!isSearch){
			return null;
		}
		List<PersonSalarySocialSecurity> personSalarySocialSecurityList = personSalarySocialSecurityManager.find(hql, "0");
		for (PersonSalarySocialSecurity personSalarySocialSecurity : personSalarySocialSecurityList) {
			//解密参数
			personSalarySocialSecurity.setPensionBaseMoney(decrypt(personSalarySocialSecurity.getPensionBaseMoney()));
			personSalarySocialSecurity.setPensionCompanyProportion(decrypt(personSalarySocialSecurity.getPensionCompanyProportion()));
			personSalarySocialSecurity.setPensionCompanyMoney(decrypt(personSalarySocialSecurity.getPensionCompanyMoney()));
			personSalarySocialSecurity.setPensionPersonalProportion(decrypt(personSalarySocialSecurity.getPensionPersonalProportion()));
			personSalarySocialSecurity.setPensionPersonalMoney(decrypt(personSalarySocialSecurity.getPensionPersonalMoney()));
			personSalarySocialSecurity.setUnemploymentCompanyProportion(decrypt(personSalarySocialSecurity.getUnemploymentCompanyProportion()));
			personSalarySocialSecurity.setUnemploymentCompanyMoney(decrypt(personSalarySocialSecurity.getUnemploymentCompanyMoney()));
			personSalarySocialSecurity.setUnemploymentPersonalProportion(decrypt(personSalarySocialSecurity.getUnemploymentPersonalProportion()));
			personSalarySocialSecurity.setUnemploymentPersonalMoney(decrypt(personSalarySocialSecurity.getUnemploymentPersonalMoney()));
			personSalarySocialSecurity.setMedicalBaseMoney(decrypt(personSalarySocialSecurity.getMedicalBaseMoney()));
			personSalarySocialSecurity.setMedicalCompanyProportion(decrypt(personSalarySocialSecurity.getMedicalCompanyProportion()));
			personSalarySocialSecurity.setMedicalCompanyMoney(decrypt(personSalarySocialSecurity.getMedicalCompanyMoney()));
			personSalarySocialSecurity.setMedicalPersonalProportion(decrypt(personSalarySocialSecurity.getMedicalPersonalProportion()));
			personSalarySocialSecurity.setMedicalPersonalMoney(decrypt(personSalarySocialSecurity.getMedicalPersonalMoney()));
			personSalarySocialSecurity.setInjuryCompanyProportion(decrypt(personSalarySocialSecurity.getInjuryCompanyProportion()));
			personSalarySocialSecurity.setInjuryCompanyMoney(decrypt(personSalarySocialSecurity.getInjuryCompanyMoney()));
			personSalarySocialSecurity.setBirthCompanyProportion(decrypt(personSalarySocialSecurity.getBirthCompanyProportion()));
			personSalarySocialSecurity.setBirthCompanyMoney(decrypt(personSalarySocialSecurity.getBirthCompanyMoney()));
			//String idcardNum = personSalarySocialSecurity.getIdcardNum();
			//查询person_info
			/*PersonInfo personInfo = personInfoManager.findUniqueBy("identityID", idcardNum);
			if(null == personInfo){
			}*/
		}
		return personSalarySocialSecurityList;
	}
	
	/**
	 * 生成下一月份的数据 社保
	 * @param contractCompanyId
	 * @param date
	 * @param year
	 * @param month
	 * @return
	 */
	@Transactional(readOnly = false)
	public void salarySocialSecurityCreateMonthData(
			String contractCompanyId,String currentDate, String nextDate, String year, String month) throws Exception{
		 String name = currentUserHolder.getName();
		 List<PersonSalarySocialSecurity> personSalarySocialSecurityList = personSalarySocialSecurityManager.find("from PersonSalarySocialSecurity where delFlag = '0' and contractCompanyId = ? and socialSecurityDate = ? ", contractCompanyId,currentDate);
		 for (PersonSalarySocialSecurity personSalarySocialSecurity : personSalarySocialSecurityList) {
			 //判断在时间段内，人员是否离职
			 String idcardNum = personSalarySocialSecurity.getIdcardNum();
			 PersonInfo personInfo = personInfoManager.findUniqueBy("identityID", idcardNum);
			 if(null != personInfo){
				 String quitFlag = personInfo.getQuitFlag();
				 if("1".equals(quitFlag)){
					//拼接年月日为离职人员做
			    	int intYear=Integer.parseInt(year);
			 	    int intMonth=Integer.parseInt(month);
			 	    Calendar calSearch=Calendar.getInstance();
			 	    //下个月的第一天
			 	    calSearch.set(intYear, intMonth, 1);
					Date quitDate = personInfo.getLeaveDate();
					String formatDate = DateUtil.formatDate(quitDate, "");
					formatDate = formatDate.substring(0, 10);
					quitDate = DateUtil.formatDateStr(formatDate+" 18:00:00", "");
					Calendar calQuit=Calendar.getInstance();
					calQuit.setTime(quitDate);
					int intQuitYear=calQuit.get(Calendar.YEAR);
					int intQuitMonth=calQuit.get(Calendar.MONTH)+1;
					if(intQuitMonth>11){
						intQuitYear+=1;
						intQuitMonth=0;
					}
					calQuit.set(intQuitYear,intQuitMonth,1);
					//如果生成数据时间大于离职时间
					if(calSearch.after(calQuit)){
						continue;
					}else{
						personSalarySocialSecurity.setQuitFlag("离职");
					}
				 } 
				 personSalarySocialSecurity.setIdcardNumColor("0");
			 }else{
				 personSalarySocialSecurity.setIdcardNumColor("1");
			 }
			 PersonSalarySocialSecurity personSalarySocialSecurityNew = new PersonSalarySocialSecurity();
			 beanMapper.copy(personSalarySocialSecurity, personSalarySocialSecurityNew);
			 personSalarySocialSecurityNew.setId(null);
			 personSalarySocialSecurityNew.setSocialSecurityDate(nextDate);
			 personSalarySocialSecurityNew.setSocialSecurityYear(year);
			 personSalarySocialSecurityNew.setSocialSecurityMonth(month);
			 setSocialSecurityColor(name,personSalarySocialSecurityNew,personSalarySocialSecurity,"create");
			 personSalarySocialSecurityManager.save(personSalarySocialSecurityNew);
		}
	}
	
	
	/**
	 * 查询当前薪资单位下的最大月份 社保
	 * @param contractCompanyId
	 * @return
	 * @throws Exception 
	 */
	public Map<String, Object> querySocialSecurityMonthByContractCompanyId(String contractCompanyId) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		Page pagedQuery = personSalarySocialSecurityManager.pagedQuery("from PersonSalarySocialSecurity where delFlag = '0' and contractCompanyId = ? order by socialSecurityDate desc",1,1, contractCompanyId);
		List<PersonSalarySocialSecurity> personSalarySocialSecurityList = (List<PersonSalarySocialSecurity>) pagedQuery.getResult();
		if(null != personSalarySocialSecurityList && personSalarySocialSecurityList.size() > 0){
			String socialSecurityDate = personSalarySocialSecurityList.get(0).getSocialSecurityDate();
			String nextMonth = DateUtil.getLastMonth(socialSecurityDate, 0, 1, 0);
			String year = nextMonth.substring(0, 4);
			String month = nextMonth.substring(nextMonth.length()-2, nextMonth.length());
			map.put("currentDate", socialSecurityDate);
			map.put("nextDate", nextMonth);
			map.put("year", year);
			map.put("month", month);
			map.put("boo", true);
		}else{
			map.put("boo", false);
		}
		return map;
	}
	
	/**
	 * 导入社保扣款明细
	 * @param contractCompanyId
	 * @param salarySocialSecurityList
	 * @param failSize
	 * @return
	 * @throws Exception 
	 */
	@Transactional(readOnly = false)
	public List<Map<String, Object>> importSocialSecurity(String contractCompanyId,
			List<Map<String, Object>> salarySocialSecurityList) throws Exception {
		Map<String, Object> queryForMap = jdbcTemplate.queryForMap("select * from person_contract_company where id = '"+contractCompanyId+"'");
		String contractCompanyName = StringUtil.toString(queryForMap.get("contract_company_name"));
		
		String name = currentUserHolder.getName();
		List<Map<String, Object>> failList = new ArrayList<Map<String,Object>>();
		
		for (Map<String, Object> salarySocialSecurityMap : salarySocialSecurityList) {
			String socialSecurityDate = StringUtil.toString(salarySocialSecurityMap.get("0"));
			String socialSecurityYear = "";
			String socialSecurityMonth = "";
			if(StringUtils.isNotBlank(socialSecurityDate)){
				socialSecurityYear = socialSecurityDate.substring(0, 4);
				socialSecurityMonth = socialSecurityDate.substring(socialSecurityDate.length()-2, socialSecurityDate.length());
			}
			String personName = StringUtil.toString(salarySocialSecurityMap.get("1"));
			String idcardNum = StringUtil.toString(salarySocialSecurityMap.get("2"));
			String accountCharacte = StringUtil.toString(salarySocialSecurityMap.get("3"));
			String pensionBaseMoney = getNumber(StringUtil.toString(salarySocialSecurityMap.get("4")));
			String pensionCompanyProportion = StringUtil.toString(salarySocialSecurityMap.get("5"));
			String pensionCompanyMoney = getNumber(StringUtil.toString(salarySocialSecurityMap.get("6")));
			String pensionPersonalProportion = StringUtil.toString(salarySocialSecurityMap.get("7"));
			String pensionPersonalMoney = getNumber(StringUtil.toString(salarySocialSecurityMap.get("8")));
			String unemploymentCompanyProportion = StringUtil.toString(salarySocialSecurityMap.get("9"));
			String unemploymentCompanyMoney = getNumber(StringUtil.toString(salarySocialSecurityMap.get("10")));
			String unemploymentPersonalProportion = StringUtil.toString(salarySocialSecurityMap.get("11"));
			String unemploymentPersonalMoney = getNumber(StringUtil.toString(salarySocialSecurityMap.get("12")));
			String medicalBaseMoney = getNumber(StringUtil.toString(salarySocialSecurityMap.get("13")));
			String medicalCompanyProportion = StringUtil.toString(salarySocialSecurityMap.get("14"));
			String medicalCompanyMoney = getNumber(StringUtil.toString(salarySocialSecurityMap.get("15")));
			String medicalPersonalProportion = StringUtil.toString(salarySocialSecurityMap.get("16"));
			String medicalPersonalMoney = getNumber(StringUtil.toString(salarySocialSecurityMap.get("17")));
			String injuryCompanyProportion = StringUtil.toString(salarySocialSecurityMap.get("18"));
			String injuryCompanyMoney = getNumber(StringUtil.toString(salarySocialSecurityMap.get("19")));
			String birthCompanyProportion = StringUtil.toString(salarySocialSecurityMap.get("20"));
			String birthCompanyMoney = getNumber(StringUtil.toString(salarySocialSecurityMap.get("21")));
			
			//查询person_info
			PersonInfo personInfo = personInfoManager.findUniqueBy("identityID", idcardNum);
			//查询当前导入月份的数据
			PersonSalarySocialSecurity personSalarySocialSecurity = personSalarySocialSecurityManager.findUnique("from PersonSalarySocialSecurity where delFlag = '0' and idcardNum = ? and socialSecurityDate = ?",idcardNum,socialSecurityDate);
			//查询当前导入上一月份的数据
			String lastMonth = DateUtil.getLastMonth(socialSecurityDate, 0, -1, 0);
			PersonSalarySocialSecurity personSalarySocialSecurityLast = personSalarySocialSecurityManager.findUnique("from PersonSalarySocialSecurity where delFlag = '0' and idcardNum = ? and socialSecurityDate = ?",idcardNum,lastMonth);
			//查询上上个月
			String lastMonths = DateUtil.getLastMonth(lastMonth, 0, -1, 0);
			PersonSalarySocialSecurity personSalarySocialSecurityLasts = personSalarySocialSecurityManager.findUnique("from PersonSalarySocialSecurity where delFlag = '0' and idcardNum = ? and socialSecurityDate = ?",idcardNum,lastMonths);

			//判断当前导入数据是否隔月，如果隔月，则不允许导入
			if(null != personSalarySocialSecurityLasts && null == personSalarySocialSecurityLast && null != personInfo){
				//导入失败的数据
				Map<String,Object> failMap = new HashMap<String,Object>();
				
				failMap.put("userCode", personInfo.getEmployeeNo());
				failMap.put("userName", personInfo.getFullName());
				failMap.put("departmentName", personInfo.getDepartmentName());
				failMap.put("salaryDate", socialSecurityDate);
				failMap.put("failReason","导入数据隔月");
				failList.add(failMap);
				continue;
			}
			
			if(null == personSalarySocialSecurity){
				personSalarySocialSecurity = new PersonSalarySocialSecurity();
			}
			//取花名册数据
			if(null != personInfo){
				personSalarySocialSecurity.setPersonId(String.valueOf(personInfo.getId()));
				personSalarySocialSecurity.setIdcardNumColor("0");
				if("1".equals(personSalarySocialSecurity.getQuitFlag())){
					personSalarySocialSecurity.setQuitFlag("离职");
				}
			}else{
				personSalarySocialSecurity.setIdcardNumColor("1");
			}
			
			personSalarySocialSecurity.setPersonName(personName);
			personSalarySocialSecurity.setSocialSecurityDate(socialSecurityDate);
			personSalarySocialSecurity.setSocialSecurityYear(socialSecurityYear);
			personSalarySocialSecurity.setSocialSecurityMonth(socialSecurityMonth);
			personSalarySocialSecurity.setContractCompanyId(contractCompanyId);
			personSalarySocialSecurity.setContractCompanyName(contractCompanyName);
			personSalarySocialSecurity.setIdcardNum(idcardNum);
			personSalarySocialSecurity.setAccountCharacte(accountCharacte);
			personSalarySocialSecurity.setPensionBaseMoney(LogisticsUtil.encryptThreeDESECB(pensionBaseMoney, salaryDesKey));
			personSalarySocialSecurity.setPensionCompanyProportion(LogisticsUtil.encryptThreeDESECB(pensionCompanyProportion, salaryDesKey));
			personSalarySocialSecurity.setPensionCompanyMoney(LogisticsUtil.encryptThreeDESECB(pensionCompanyMoney, salaryDesKey));
			personSalarySocialSecurity.setPensionPersonalProportion(LogisticsUtil.encryptThreeDESECB(pensionPersonalProportion, salaryDesKey));
			personSalarySocialSecurity.setPensionPersonalMoney(LogisticsUtil.encryptThreeDESECB(pensionPersonalMoney, salaryDesKey));
			personSalarySocialSecurity.setUnemploymentCompanyProportion(LogisticsUtil.encryptThreeDESECB(unemploymentCompanyProportion, salaryDesKey));
			personSalarySocialSecurity.setUnemploymentCompanyMoney(LogisticsUtil.encryptThreeDESECB(unemploymentCompanyMoney, salaryDesKey));
			personSalarySocialSecurity.setUnemploymentPersonalProportion(LogisticsUtil.encryptThreeDESECB(unemploymentPersonalProportion, salaryDesKey));
			personSalarySocialSecurity.setUnemploymentPersonalMoney(LogisticsUtil.encryptThreeDESECB(unemploymentPersonalMoney, salaryDesKey));
			personSalarySocialSecurity.setMedicalBaseMoney(LogisticsUtil.encryptThreeDESECB(medicalBaseMoney, salaryDesKey));
			personSalarySocialSecurity.setMedicalCompanyProportion(LogisticsUtil.encryptThreeDESECB(medicalCompanyProportion, salaryDesKey));
			personSalarySocialSecurity.setMedicalCompanyMoney(LogisticsUtil.encryptThreeDESECB(medicalCompanyMoney, salaryDesKey));
			personSalarySocialSecurity.setMedicalPersonalProportion(LogisticsUtil.encryptThreeDESECB(medicalPersonalProportion, salaryDesKey));
			personSalarySocialSecurity.setMedicalPersonalMoney(LogisticsUtil.encryptThreeDESECB(medicalPersonalMoney, salaryDesKey));
			personSalarySocialSecurity.setInjuryCompanyProportion(LogisticsUtil.encryptThreeDESECB(injuryCompanyProportion, salaryDesKey));
			personSalarySocialSecurity.setInjuryCompanyMoney(LogisticsUtil.encryptThreeDESECB(injuryCompanyMoney, salaryDesKey));
			personSalarySocialSecurity.setBirthCompanyProportion(LogisticsUtil.encryptThreeDESECB(birthCompanyProportion, salaryDesKey));
			personSalarySocialSecurity.setBirthCompanyMoney(LogisticsUtil.encryptThreeDESECB(birthCompanyMoney, salaryDesKey));
			//对比不同数据，颜色标识
			setSocialSecurityColor(name,personSalarySocialSecurity,personSalarySocialSecurityLast,"import");
			
			personSalarySocialSecurityManager.save(personSalarySocialSecurity);
		}
		return failList;
	}

	//社保与上个月对比，不同数据标识
	private void setSocialSecurityColor(String name,PersonSalarySocialSecurity personSalarySocialSecurity,
			PersonSalarySocialSecurity personSalarySocialSecurityLast,String type) {
		String remarkStr = "";
		if("import".equals(type)){
			remarkStr = "导入"+personSalarySocialSecurity.getSocialSecurityYear()+"年"+personSalarySocialSecurity.getSocialSecurityMonth()+"月数据,";
		}else if("edit".equals(type)){
			remarkStr = "修改"+personSalarySocialSecurity.getSocialSecurityYear()+"年"+personSalarySocialSecurity.getSocialSecurityMonth()+"月数据,";
		}else if("create".equals(type)){
			remarkStr = "生成"+personSalarySocialSecurity.getSocialSecurityYear()+"年"+personSalarySocialSecurity.getSocialSecurityMonth()+"月数据,";
		}
		if(null != personSalarySocialSecurityLast){
			if(!personSalarySocialSecurityLast.getAccountCharacte().equals(personSalarySocialSecurity.getAccountCharacte())){
				personSalarySocialSecurity.setAccountCharacteColor("1");
			}else{
				personSalarySocialSecurity.setAccountCharacteColor("0");
			}
			if(!personSalarySocialSecurityLast.getPensionBaseMoneyColor().equals(personSalarySocialSecurity.getPensionBaseMoneyColor())){
				personSalarySocialSecurity.setPensionBaseMoneyColor("1");
			}else{
				personSalarySocialSecurity.setPensionBaseMoneyColor("0");
			}
			if(!personSalarySocialSecurityLast.getPensionCompanyProportion().equals(personSalarySocialSecurity.getPensionCompanyProportion())){
				personSalarySocialSecurity.setPensionCompanyProportionColor("1");
			}else{
				personSalarySocialSecurity.setPensionCompanyProportionColor("0");
			}
			if(!personSalarySocialSecurityLast.getPensionCompanyMoney().equals(personSalarySocialSecurity.getPensionCompanyMoney())){
				personSalarySocialSecurity.setPensionCompanyMoneyColor("1");
			}else{
				personSalarySocialSecurity.setPensionCompanyMoneyColor("0");
			}
			if(!personSalarySocialSecurityLast.getPensionPersonalProportion().equals(personSalarySocialSecurity.getPensionPersonalProportion())){
				personSalarySocialSecurity.setPensionPersonalProportionColor("1");
			}else{
				personSalarySocialSecurity.setPensionPersonalProportionColor("0");
			}
			if(!personSalarySocialSecurityLast.getPensionPersonalMoney().equals(personSalarySocialSecurity.getPensionPersonalMoney())){
				personSalarySocialSecurity.setPensionPersonalMoneyColor("1");
			}else{
				personSalarySocialSecurity.setPensionPersonalMoneyColor("0");
			}
			if(!personSalarySocialSecurityLast.getUnemploymentCompanyProportion().equals(personSalarySocialSecurity.getUnemploymentCompanyProportion())){
				personSalarySocialSecurity.setUnemploymentCompanyProportionColor("1");
			}else{
				personSalarySocialSecurity.setUnemploymentCompanyProportionColor("0");
			}
			if(!personSalarySocialSecurityLast.getUnemploymentCompanyMoney().equals(personSalarySocialSecurity.getUnemploymentCompanyMoney())){
				personSalarySocialSecurity.setUnemploymentCompanyMoneyColor("1");
			}else{
				personSalarySocialSecurity.setUnemploymentCompanyMoneyColor("0");
			}
			if(!personSalarySocialSecurityLast.getUnemploymentPersonalProportion().equals(personSalarySocialSecurity.getUnemploymentPersonalProportion())){
				personSalarySocialSecurity.setUnemploymentPersonalProportionColor("1");
			}else{
				personSalarySocialSecurity.setUnemploymentPersonalProportionColor("0");
			}
			if(!personSalarySocialSecurityLast.getUnemploymentPersonalMoney().equals(personSalarySocialSecurity.getUnemploymentPersonalMoney())){
				personSalarySocialSecurity.setUnemploymentPersonalMoneyColor("1");
			}else{
				personSalarySocialSecurity.setUnemploymentPersonalMoneyColor("0");
			}
			if(!personSalarySocialSecurityLast.getMedicalBaseMoney().equals(personSalarySocialSecurity.getMedicalBaseMoney())){
				personSalarySocialSecurity.setMedicalBaseMoneyColor("1");
			}else{
				personSalarySocialSecurity.setMedicalBaseMoneyColor("0");
			}
			if(!personSalarySocialSecurityLast.getMedicalCompanyProportion().equals(personSalarySocialSecurity.getMedicalCompanyProportion())){
				personSalarySocialSecurity.setMedicalCompanyProportionColor("1");
			}else{
				personSalarySocialSecurity.setMedicalCompanyProportionColor("0");
			}
			if(!personSalarySocialSecurityLast.getMedicalCompanyMoney().equals(personSalarySocialSecurity.getMedicalCompanyMoney())){
				personSalarySocialSecurity.setMedicalCompanyMoneyColor("1");
			}else{
				personSalarySocialSecurity.setMedicalCompanyMoneyColor("0");
			}
			if(!personSalarySocialSecurityLast.getMedicalPersonalProportion().equals(personSalarySocialSecurity.getMedicalPersonalProportion())){
				personSalarySocialSecurity.setMedicalPersonalProportionColor("1");
			}else{
				personSalarySocialSecurity.setMedicalPersonalProportionColor("0");
			}
			if(!personSalarySocialSecurityLast.getMedicalPersonalMoney().equals(personSalarySocialSecurity.getMedicalPersonalMoney())){
				personSalarySocialSecurity.setMedicalPersonalMoneyColor("1");
			}else{
				personSalarySocialSecurity.setMedicalPersonalMoneyColor("0");
			}
			if(!personSalarySocialSecurityLast.getInjuryCompanyProportion().equals(personSalarySocialSecurity.getInjuryCompanyProportion())){
				personSalarySocialSecurity.setInjuryCompanyProportionColor("1");
			}else{
				personSalarySocialSecurity.setInjuryCompanyProportionColor("0");
			}
			if(!personSalarySocialSecurityLast.getInjuryCompanyMoney().equals(personSalarySocialSecurity.getInjuryCompanyMoney())){
				personSalarySocialSecurity.setInjuryCompanyMoneyColor("1");
			}else{
				personSalarySocialSecurity.setInjuryCompanyMoneyColor("0");
			}
			if(!personSalarySocialSecurityLast.getBirthCompanyProportion().equals(personSalarySocialSecurity.getBirthCompanyProportion())){
				personSalarySocialSecurity.setBirthCompanyProportionColor("1");
			}else{
				personSalarySocialSecurity.setBirthCompanyProportionColor("0");
			}
			if(!personSalarySocialSecurityLast.getBirthCompanyMoney().equals(personSalarySocialSecurity.getBirthCompanyMoney())){
				personSalarySocialSecurity.setBirthCompanyMoneyColor("1");
			}else{
				personSalarySocialSecurity.setBirthCompanyMoneyColor("0");
			}
		}/*else{
			personSalarySocialSecurity.setAccountCharacteColor("0");
			personSalarySocialSecurity.setPensionBaseMoneyColor("0");
			personSalarySocialSecurity.setPensionCompanyProportionColor("0");
			personSalarySocialSecurity.setPensionCompanyMoneyColor("0");
			personSalarySocialSecurity.setPensionPersonalProportionColor("0");
			personSalarySocialSecurity.setPensionPersonalMoneyColor("0");
			personSalarySocialSecurity.setUnemploymentCompanyProportionColor("0");
			personSalarySocialSecurity.setUnemploymentCompanyMoneyColor("0");
			personSalarySocialSecurity.setUnemploymentPersonalProportionColor("0");
			personSalarySocialSecurity.setUnemploymentPersonalMoneyColor("0");
			personSalarySocialSecurity.setMedicalBaseMoneyColor("0");
			personSalarySocialSecurity.setMedicalCompanyProportionColor("0");
			personSalarySocialSecurity.setMedicalCompanyMoneyColor("0");
			personSalarySocialSecurity.setMedicalPersonalProportionColor("0");
			personSalarySocialSecurity.setMedicalPersonalMoneyColor("0");
			personSalarySocialSecurity.setInjuryCompanyProportionColor("0");
			personSalarySocialSecurity.setInjuryCompanyMoneyColor("0");
			personSalarySocialSecurity.setBirthCompanyProportionColor("0");
			personSalarySocialSecurity.setBirthCompanyMoneyColor("0");
		}*/
		String systemRemark = personSalarySocialSecurity.getSystemRemark();
		systemRemark += name+remarkStr;
		personSalarySocialSecurity.setSystemRemark(systemRemark);
	}
	
	
	/**
	 * 编辑工资基本表
	 * @param personSalaryBase
	 * @throws Exception 
	 * @author ckx
	 */
	@Transactional(readOnly = false)
	public void editSalatyBase(PersonSalaryBase personSalaryBase) throws Exception {
		String name = currentUserHolder.getName();
		PersonSalaryBase oldPersonSalaryBase = personSalaryBaseManager.findUniqueBy("id", personSalaryBase.getId());
		DictType dictType = dictTypeManager.findUniqueBy("name", "salaryColume");
		List<DictInfo> dictInfoList = dictInfoManager.findBy("dictType", dictType);
		for (DictInfo dictInfo : dictInfoList) {
			String fieldName = dictInfo.getValue();
			String getMethod = StringUtil.toString(ReflectUtil.getGetMethod(personSalaryBase, fieldName));
			//排除级别和入职时间
			if(!"entryDate".equals(fieldName) ){
				if(!"personLevel".equals(fieldName)){
					if(fieldName.contains("Money") || "taxableWages".equals(fieldName) || "entryAgeExpense".equals(fieldName)){
						if(StringUtils.isNotBlank(getMethod)){
							int length=getMethod.length();
							if (length>=2) {  //这里大于等于2是防止有些列只有一个字符，到下面会报错
								if(getMethod.contains(".")){
									int lastIndexOf = getMethod.lastIndexOf(".");
									String substring = getMethod.substring(lastIndexOf+1, length);
									if(substring.length()<2){
										getMethod = getMethod+"0";
									}
								}else{
									getMethod = getMethod+".00";
								}
							}else{
								getMethod = getMethod+".00";
							}
						}
					}else{
						if(StringUtils.isNotBlank(getMethod)){
							int length=getMethod.length();
							if (length>=2) {  //这里大于等于2是防止有些列只有一个字符，到下面会报错
								if(getMethod.contains(".")){
									int lastIndexOf = getMethod.lastIndexOf(".");
									String substring = getMethod.substring(lastIndexOf+1, length);
									if("00".equals(substring)){
										getMethod = getMethod.substring(0, lastIndexOf);
									}else{
										getMethod = getMethod.substring(0, lastIndexOf+2);
									}
								}
							}
						}
						String fieldValue = LogisticsUtil.encryptThreeDESECB(StringUtil.toString(getMethod), salaryDesKey);
						ReflectUtil.setValue(oldPersonSalaryBase, oldPersonSalaryBase.getClass(), fieldName, PersonSalaryBase.class.getDeclaredField(fieldName).getType(), fieldValue);
					}
				}else{
					//des加密
					String fieldValue = LogisticsUtil.encryptThreeDESECB(getMethod, salaryDesKey);
					ReflectUtil.setValue(oldPersonSalaryBase, oldPersonSalaryBase.getClass(), fieldName, PersonSalaryBase.class.getDeclaredField(fieldName).getType(), fieldValue);
				}
			}
		}
		oldPersonSalaryBase.setIdcardNum(personSalaryBase.getIdcardNum());
		String salaryDate = oldPersonSalaryBase.getSalaryDate();
		String idcardNum = oldPersonSalaryBase.getIdcardNum();
		//查询当前导入上一月份的数据
		String lastMonth = DateUtil.getLastMonth(salaryDate, 0, -1, 0);
		PersonSalaryBase personSalaryBaseLast = personSalaryBaseManager.findUnique("from PersonSalaryBase where idcardNum = ? and salaryDate = ?",idcardNum,lastMonth);
		//对比是否有数据需要进行标识颜色
		savePersonSalarySupport(name,oldPersonSalaryBase,personSalaryBaseLast,"edit");
		personSalaryBaseManager.update(oldPersonSalaryBase);
	}
	
	/**
	 * 删除工资基本表数据
	 * @param id
	 * @return
	 * @author ckx
	 */
	@Transactional(readOnly = false)
	public boolean deleteSalaryBaseById(String id) {
		String name = currentUserHolder.getName();
		try {
			PersonSalaryBase personSalaryBase = personSalaryBaseManager.findUniqueBy("id", Long.parseLong(id));
			PersonSalarySupport personSalarySupport = personSalaryBase.getPersonSalarySupport();
			//personSalaryBaseManager.removeById(Long.parseLong(id));
			//personSalarySupportManager.remove(personSalarySupport);
			String systemRemark = personSalarySupport.getSystemRemark();
			systemRemark += name+"删除该条数据,";
			personSalarySupport.setSystemRemark(systemRemark);
			personSalaryBase.setDelFlag("1");
			personSalaryBaseManager.update(personSalaryBase);
			personSalarySupportManager.update(personSalarySupport);
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	
	/**
	 * 工资基本表展示
	 * @return
	 * @throws Exception 
	 * @author ckx
	 */
	public List<PersonSalaryBase> salaryBaseListSub(String postId,String contractCompanyId,
			String startDate, String endDate,String personName) throws Exception {
		//判断当前人员是否有权限查看数据
		boolean isCompany = false;
		List<Map<String, Object>> allContractCompanyName = getAllContractCompanyName();
		if(null == allContractCompanyName || allContractCompanyName.size() == 0){
			return null;
		}else{
			for (Map<String, Object> map : allContractCompanyName) {
				String companyId = StringUtil.toString(map.get("contract_company_id"));
				if(contractCompanyId.equals(companyId)){
					isCompany = true;
				}
			}
		}
		if(!isCompany){
			return null;
		}
		
		List<String> allPersonList=new ArrayList<String>();
		boolean isSearch = false;
		DictType dictType = dictTypeManager.findUniqueBy("name", "salaryColume");
		List<DictInfo> dictInfoList = dictInfoManager.findBy("dictType", dictType);
		String hql = "from PersonSalaryBase s where ";
		hql += " s.delFlag = ? ";
		if(StringUtils.isNotBlank(postId)){
			//获取所有子节点
			//获取所有人员数据
			String[] split = postId.split(",");
			for (String id : split) {
				List<String> allPersonById = getAllPersonById(Long.parseLong(id));
				allPersonList.addAll(allPersonById);
			}
			String personIds=Joiner.on(",").join(allPersonList);
			hql = " select s from PersonSalaryBase s , PersonInfo p  where  s.idcardNum = p.identityID and s.delFlag = ? and p.id in ("+personIds+")";
			isSearch = true;
		}
		if(StringUtils.isNotBlank(contractCompanyId)){
			hql += " and s.contractCompanyId = '"+contractCompanyId+"' ";
			isSearch = true;
		}
		if(StringUtils.isNotBlank(startDate)){
			hql += " and s.salaryDate >= '"+startDate+"' ";
			isSearch = true;
		}
		if(StringUtils.isNotBlank(endDate)){
			hql += " and s.salaryDate <= '"+endDate+"' ";
			isSearch = true;
		}
		if(StringUtils.isNotBlank(personName)){
			hql += " and s.personName like '%"+personName+"%' ";
			isSearch = true;
		}
		hql += " order by s.id ";
		//默认加载不查询
		if(!isSearch){
			return null;
		}
		List<PersonSalaryBase> personSalaryBaseList = personSalaryBaseManager.find(hql, "0");
		for (PersonSalaryBase personSalaryBase : personSalaryBaseList) {
			
			for (DictInfo dictInfo : dictInfoList) {
				String fieldName = dictInfo.getValue();
				if(!"entryDate".equals(fieldName)){
					Object getMethod = ReflectUtil.getGetMethod(personSalaryBase, fieldName);
					String fieldValue = decrypt(getMethod);
					ReflectUtil.setValue(personSalaryBase, personSalaryBase.getClass(), fieldName, PersonSalaryBase.class.getDeclaredField(fieldName).getType(), fieldValue);
				}else{
					Date entryDate = personSalaryBase.getEntryDate();
					if(null != entryDate){
						String formatDate = DateUtil.formatDate(entryDate, "yyyy-MM-dd");
						personSalaryBase.setAttr1(formatDate);
					}else{
						personSalaryBase.setAttr1("");
					}
				}
			}
			String idcardNum = personSalaryBase.getIdcardNum();
			//查询person_info
			PersonInfo personInfo = personInfoManager.findUniqueBy("identityID", idcardNum);
			if(null == personInfo){
				personSalaryBase.setEmployeeNo("");
				personSalaryBase.setPersonLevel("");
				personSalaryBase.setAttr1("");
			}
		}
		return personSalaryBaseList;
	}
	
	/**
	 * 查询所有人员id
	 * @param partyEntityId
	 * @return
	 */
	private List<String> getAllPersonById(Long partyEntityId){
		String strChildPartyIds="";
		List<String> childAllList = getAllDeparentById(partyEntityId);
		if(childAllList!=null){
			strChildPartyIds=Joiner.on(",").join(childAllList);
		}
		String sql = "select p.id from person_info as p left join party_entity as e on p.PARTY_ID = e.id left join party_struct as s on e.id =s.CHILD_ENTITY_ID "
        		+ " where s.STRUCT_TYPE_ID = " + PartyConstants.PARTY_STRUCT_TYPE_ORG;
		if(!com.mossle.core.util.StringUtils.isBlank(strChildPartyIds))
			sql+=" and s.PARENT_ENTITY_ID in ("+strChildPartyIds+")";
		List<String> personIdList = jdbcTemplate.queryForList(String.format(sql), String.class);
        return personIdList;
	}
	
	
    /**
     * 得到组织关系下所有子节点ID
     * @param partyEntityId
     * @return
     */
    private List<String> getAllDeparentById(Long partyEntityId) {
		
    	List<String> childAllList=new ArrayList<String>();
    	childAllList.add(partyEntityId.toString());    	
    	
    	String sqlString="select s.CHILD_ENTITY_ID from party_struct s join party_entity e on s.CHILD_ENTITY_ID = e.id where s.STRUCT_TYPE_ID=1 and s.PARENT_ENTITY_ID in(%s)";
    	List<String> partyIdList=jdbcTemplate.queryForList(String.format(sqlString, partyEntityId), String.class);
    	if(partyIdList!=null&&partyIdList.size()>0){
    		childAllList.addAll(partyIdList);
    		String strPartyIds=Joiner.on(",").join(partyIdList);
    		while(true){
    			List<String> childPartyIdList=jdbcTemplate.queryForList(String.format(sqlString,strPartyIds), String.class);
    			if(childPartyIdList!=null&&childPartyIdList.size()>0)
    			{
    				childAllList.addAll(childPartyIdList);
    				strPartyIds=Joiner.on(",").join(childPartyIdList);
    			}
    			else {
					break;
				}
        	}
    	}
    	return childAllList;
	}
	
	//解密参数 @author ckx
	public String decrypt(Object salaryParam) throws Exception{
		String decode = URLDecoder.decode(StringUtil.toString(salaryParam),"utf-8");
		String decryptThreeDESECB = LogisticsUtil.decryptThreeDESECB(decode, salaryDesKey);
		return decryptThreeDESECB;
	}
	
	/**
	 * 导入工资基本表
	 * @param salaryColume
	 * @param salaryBaseList
	 * @param failSize
	 * @return
	 * @throws Exception
	 * @author ckx
	 */
	@Transactional(readOnly = false)
	public List<Map<String, Object>> importSalaryBase(String salaryColume,String contractCompanyId,List<Map<String, Object>> salaryBaseList) throws Exception {
		String name = currentUserHolder.getName();
		//DictType dictType = dictTypeManager.findUniqueBy("name", "salaryColume");
		//List<DictInfo> dictInfoList = dictInfoManager.findBy("dictType", dictType);
		List<Map<String, Object>> failList = new ArrayList<Map<String,Object>>();
		for (Map<String, Object> salaryBaseMap : salaryBaseList) {
			
			String salaryDate = StringUtil.toString(salaryBaseMap.get("0"));
			String salaryYear = "";
			String salaryMonth = "";
			if(StringUtils.isNotBlank(salaryDate)){
				salaryYear = salaryDate.substring(0, 4);
				salaryMonth = salaryDate.substring(salaryDate.length()-2, salaryDate.length());
			}
			String employeeNo = StringUtil.toString(salaryBaseMap.get("1"));
			String personName = StringUtil.toString(salaryBaseMap.get("2"));
			String contractCompanyName = StringUtil.toString(salaryBaseMap.get("3"));
			String idcardNum = StringUtil.toString(salaryBaseMap.get("4"));
			//身份证号不允许为空
			if(StringUtils.isBlank(idcardNum)){
				continue;
			}
			//查询person_info
			PersonInfo personInfo = personInfoManager.findUniqueBy("identityID", idcardNum);
			//查询当前导入月份的数据
			PersonSalaryBase personSalaryBase = personSalaryBaseManager.findUnique("from PersonSalaryBase where delFlag = '0' and idcardNum = ? and salaryDate = ?",idcardNum,salaryDate);
			//查询当前导入上一月份的数据
			String lastMonth = DateUtil.getLastMonth(salaryDate, 0, -1, 0);
			PersonSalaryBase personSalaryBaseLast = personSalaryBaseManager.findUnique("from PersonSalaryBase where delFlag = '0' and idcardNum = ? and salaryDate = ?",idcardNum,lastMonth);
			//查询当前上上月份的数据
			String lastMonths = DateUtil.getLastMonth(lastMonth, 0, -1, 0);
			PersonSalaryBase personSalaryBaseLasts = personSalaryBaseManager.findUnique("from PersonSalaryBase where delFlag = '0' and idcardNum = ? and salaryDate = ?",idcardNum,lastMonths);
			
			
			//判断当前导入数据是否隔月，如果隔月，则不允许导入
			if(null != personSalaryBaseLasts && null == personSalaryBaseLast && null != personInfo){
				//导入失败的数据
				Map<String,Object> failMap = new HashMap<String,Object>();
				
				failMap.put("userCode", personInfo.getEmployeeNo());
				failMap.put("userName", personInfo.getFullName());
				failMap.put("departmentName", personInfo.getDepartmentName());
				failMap.put("salaryDate", salaryDate);
				failMap.put("failReason","导入数据隔月");
				failList.add(failMap);
				continue;
			}
			
			if(null == personSalaryBase){
				//personSalaryBase = new PersonSalaryBase();
				String nullField = LogisticsUtil.encryptThreeDESECB("", salaryDesKey);
				personSalaryBase = new PersonSalaryBase(nullField, nullField, nullField, nullField, nullField, nullField, nullField,
						nullField, nullField, nullField, nullField, nullField, nullField, nullField, nullField, 
						nullField, nullField, nullField, nullField, nullField, nullField, nullField, nullField, 
						nullField, nullField, nullField, nullField, nullField, nullField, nullField, nullField, 
						nullField, nullField, nullField, nullField, nullField, nullField, nullField, nullField, 
						nullField, nullField, nullField, nullField, nullField, nullField, nullField, nullField, 
						nullField, nullField, nullField, nullField, nullField, nullField, nullField, nullField, 
						nullField, nullField);
				//默认可选项为0
				/*for (DictInfo dictInfo : dictInfoList) {
					String fieldName = dictInfo.getValue();
					if(!"entryDate".equals(fieldName)){
						String fieldValue = LogisticsUtil.encryptThreeDESECB("0", salaryDesKey);
						ReflectUtil.setValue(personSalaryBase, personSalaryBase.getClass(), fieldName, PersonSalaryBase.class.getDeclaredField(fieldName).getType(), fieldValue);
					}else{
						personSalaryBase.setEntryDate(null);
					}
				}*/
			}
			//取花名册数据
			if(null != personInfo){
				personSalaryBase.setPersonId(String.valueOf(personInfo.getId()));
			}
			personSalaryBase.setSalaryDate(salaryDate);
			personSalaryBase.setSalaryYear(salaryYear);
			personSalaryBase.setSalaryMonth(salaryMonth);
			personSalaryBase.setEmployeeNo(employeeNo);
			personSalaryBase.setPersonName(personName);
			personSalaryBase.setContractCompanyId(contractCompanyId);
			personSalaryBase.setContractCompanyName(contractCompanyName);
			personSalaryBase.setIdcardNum(idcardNum);
			if(StringUtils.isNotBlank(salaryColume)){
				String[] split = salaryColume.split(",");
				for (int i = 0; i < split.length; i++) {
					String string = split[i];
					String[] split2 = string.split("-");
					String fieldName = split2[0];
					String fieldIndex = split2[1];
					String fieldValue = StringUtil.toString(salaryBaseMap.get(fieldIndex));
					if(!"entryDate".equals(fieldName) ){
						if(!"personLevel".equals(fieldName)){
							if(fieldName.contains("Money") || "taxableWages".equals(fieldName) || "entryAgeExpense".equals(fieldName)){
								if(StringUtils.isNotBlank(fieldValue)){
									int length=fieldValue.length();
									if (length>=2) {  //这里大于等于2是防止有些列只有一个字符，到下面会报错
										if(fieldValue.contains(".")){
											int lastIndexOf = fieldValue.lastIndexOf(".");
											String substring = fieldValue.substring(lastIndexOf+1, length);
											if(substring.length()<2){
												fieldValue = fieldValue+"0";
											}
										}else{
											fieldValue = fieldValue+".00";
										}
									}else{
										fieldValue = fieldValue+".00";
									}
								}
								//des加密
								fieldValue = LogisticsUtil.encryptThreeDESECB(fieldValue, salaryDesKey);
								ReflectUtil.setValue(personSalaryBase, personSalaryBase.getClass(), fieldName, PersonSalaryBase.class.getDeclaredField(fieldName).getType(), fieldValue);
							}else{
								if(StringUtils.isNotBlank(fieldValue)){
									int length=fieldValue.length();
									if (length>=2) {  //这里大于等于2是防止有些列只有一个字符，到下面会报错
										if(fieldValue.contains(".")){
											int lastIndexOf = fieldValue.lastIndexOf(".");
											String substring = fieldValue.substring(lastIndexOf+1, length);
											if("00".equals(substring)){
												fieldValue = fieldValue.substring(0, lastIndexOf);
											}else{
												fieldValue = fieldValue.substring(0, lastIndexOf+2);
											}
										}
									}
								}
								//des加密
								fieldValue = LogisticsUtil.encryptThreeDESECB(fieldValue, salaryDesKey);
								ReflectUtil.setValue(personSalaryBase, personSalaryBase.getClass(), fieldName, PersonSalaryBase.class.getDeclaredField(fieldName).getType(), fieldValue);
							}
						}else{
							//des加密
							fieldValue = LogisticsUtil.encryptThreeDESECB(fieldValue, salaryDesKey);
							ReflectUtil.setValue(personSalaryBase, personSalaryBase.getClass(), fieldName, PersonSalaryBase.class.getDeclaredField(fieldName).getType(), fieldValue);
						}
					}else{
						personSalaryBase.setEntryDate(DateUtil.formatDateStr(fieldValue, "yyyy-MM-dd"));
					}
				}
			}
			
			personSalaryBaseManager.save(personSalaryBase);
			//对比是否有数据需要进行标识颜色
			savePersonSalarySupport(name, personSalaryBase,personSalaryBaseLast,"import");
		}
		return failList;
	}

	/**对比是否有数据需要进行标识颜色
	 * //说明当前人员不是新员工
	 * @author ckx
	 */
	private void savePersonSalarySupport(String name,PersonSalaryBase personSalaryBase,PersonSalaryBase personSalaryBaseLast,String type) {
		String remarkStr = "";
		if("import".equals(type)){
			remarkStr = "导入数据,";
		}else if("edit".equals(type)){
			remarkStr = "修改数据,";
		}
		
		PersonSalarySupport personSalarySupport = personSalaryBase.getPersonSalarySupport();
		if(null == personSalarySupport){
			personSalarySupport = personSalarySupportManager.findUniqueBy("personSalaryBase", personSalaryBase);
			if(null == personSalarySupport){
				personSalarySupport = new PersonSalarySupport();
				personSalarySupport.setPersonSalaryBase(personSalaryBase);
			}
		}
		PersonInfo personInfo = personInfoManager.findUniqueBy("identityID", personSalaryBase.getIdcardNum());
		if(null == personInfo){
			personSalarySupport.setIdcardNumColor("1");
		}else{
			personSalarySupport.setIdcardNumColor("0");
		}
		if(null != personSalaryBaseLast){
			if(!personSalaryBase.getWagesLevelMoney().equals(personSalaryBaseLast.getWagesLevelMoney())){
				personSalarySupport.setWagesLevelMoneyColor("1");
			}else{
				personSalarySupport.setWagesLevelMoneyColor("0");
			}
			if(!personSalaryBase.getBaseWagesMoney().equals(personSalaryBaseLast.getBaseWagesMoney())){
				personSalarySupport.setBaseWagesMoneyColor("1");
			}else{
				personSalarySupport.setBaseWagesMoneyColor("0");
			}
			if(!personSalaryBase.getPostWagesMoney().equals(personSalaryBaseLast.getPostWagesMoney())){
				personSalarySupport.setPostWagesMoneyColor("1");
			}else{
				personSalarySupport.setPostWagesMoneyColor("0");
			}
			if(!personSalaryBase.getTechnicalWagesMoney().equals(personSalaryBaseLast.getTechnicalWagesMoney())){
				personSalarySupport.setTechnicalWagesMoneyColor("1");
			}else{
				personSalarySupport.setTechnicalWagesMoneyColor("0");
			}
			if(!personSalaryBase.getConfidentialityAllowanceMoney().equals(personSalaryBaseLast.getConfidentialityAllowanceMoney())){
				personSalarySupport.setConfidentialityAllowanceMoneyColor("1");
			}else{
				personSalarySupport.setConfidentialityAllowanceMoneyColor("0");
			}
			if(!personSalaryBase.getTechnicalAllowanceMoney().equals(personSalaryBaseLast.getTechnicalAllowanceMoney())){
				personSalarySupport.setTechnicalAllowanceMoneyColor("1");
			}else{
				personSalarySupport.setTechnicalAllowanceMoneyColor("0");
			}
			if(!personSalaryBase.getAchievementBonusMoney().equals(personSalaryBaseLast.getAchievementBonusMoney())){
				personSalarySupport.setAchievementBonusMoneyColor("1");
			}else{
				personSalarySupport.setAchievementBonusMoneyColor("0");
			}
			if(!personSalaryBase.getSocialPensionDeductionMoney().equals(personSalaryBaseLast.getSocialPensionDeductionMoney())){
				personSalarySupport.setSocialPensionDeductionMoneyColor("1");
			}else{
				personSalarySupport.setSocialPensionDeductionMoneyColor("0");
			}
			if(!personSalaryBase.getSocialUnemploymentDeductionMoney().equals(personSalaryBaseLast.getSocialUnemploymentDeductionMoney())){
				personSalarySupport.setSocialUnemploymentDeductionMoneyColor("1");
			}else{
				personSalarySupport.setSocialUnemploymentDeductionMoneyColor("0");
			}
			if(!personSalaryBase.getSocialMedicalDeductionMoney().equals(personSalaryBaseLast.getSocialMedicalDeductionMoney())){
				personSalarySupport.setSocialUnemploymentDeductionMoneyColor("1");
			}else{
				personSalarySupport.setSocialUnemploymentDeductionMoneyColor("0");
			}
			if(!personSalaryBase.getSocialProvidentFundDeductionMoney().equals(personSalaryBaseLast.getSocialProvidentFundDeductionMoney())){
				personSalarySupport.setSocialProvidentFundDeductionMoneyColor("1");
			}else{
				personSalarySupport.setSocialProvidentFundDeductionMoneyColor("0");
			}
			if(!personSalaryBase.getSocialOtherDeductionMoney().equals(personSalaryBaseLast.getSocialOtherDeductionMoney())){
				personSalarySupport.setSocialOtherDeductionMoneyColor("1");	
			}else{
				personSalarySupport.setSocialOtherDeductionMoneyColor("0");
			}
			if(!personSalaryBase.getSocialTotalDeductionMoney().equals(personSalaryBaseLast.getSocialTotalDeductionMoney())){
				personSalarySupport.setSocialTotalDeductionMoneyColor("1");
			}else{
				personSalarySupport.setSocialTotalDeductionMoneyColor("0");
			}
			if(!personSalaryBase.getSpecialChildrenEducationMoney().equals(personSalaryBaseLast.getSpecialChildrenEducationMoney())){
				personSalarySupport.setSpecialChildrenEducationMoneyColor("1");
			}else{
				personSalarySupport.setSpecialChildrenEducationMoneyColor("0");
			}
			if(!personSalaryBase.getSpecialContinuEducationMoney().equals(personSalaryBaseLast.getSpecialContinuEducationMoney())){
				personSalarySupport.setSpecialContinuEducationMoneyColor("1");
			}else{
				personSalarySupport.setSpecialContinuEducationMoneyColor("0");
			}
			if(!personSalaryBase.getSpecialHotelInterestMoney().equals(personSalaryBaseLast.getSpecialHotelInterestMoney())){
				personSalarySupport.setSpecialHotelInterestMoneyColor("1");
			}else{
				personSalarySupport.setSpecialHotelInterestMoneyColor("0");
			}
			if(!personSalaryBase.getSpecialHotelRentMoney().equals(personSalaryBaseLast.getSpecialHotelRentMoney())){
				personSalarySupport.setSpecialHotelRentMoneyColor("1");
			}else{
				personSalarySupport.setSpecialHotelRentMoneyColor("0");
			}
			if(!personSalaryBase.getSpecialSupportElderlyMoney().equals(personSalaryBaseLast.getSpecialSupportElderlyMoney())){
				personSalarySupport.setSpecialSupportElderlyMoneyColor("1");
			}else{
				personSalarySupport.setSpecialSupportElderlyMoneyColor("0");
			}
			if(!personSalaryBase.getSpecialCommercialHealthInsuranceMoney().equals(personSalaryBaseLast.getSpecialCommercialHealthInsuranceMoney())){
				personSalarySupport.setSpecialCommercialHealthInsuranceMoneyColor("1");
			}else{
				personSalarySupport.setSpecialCommercialHealthInsuranceMoneyColor("0");
			}
		}/*else{
			personSalarySupport.setWagesLevelMoneyColor("0");
			personSalarySupport.setBaseWagesMoneyColor("0");
			personSalarySupport.setPostWagesMoneyColor("0");
			personSalarySupport.setTechnicalWagesMoneyColor("0");
			personSalarySupport.setConfidentialityAllowanceMoneyColor("0");
			personSalarySupport.setTechnicalAllowanceMoneyColor("0");
			personSalarySupport.setAchievementBonusMoneyColor("0");
			personSalarySupport.setSocialPensionDeductionMoneyColor("0");
			personSalarySupport.setSocialUnemploymentDeductionMoneyColor("0");
			personSalarySupport.setSocialUnemploymentDeductionMoneyColor("0");
			personSalarySupport.setSocialProvidentFundDeductionMoneyColor("0");
			personSalarySupport.setSocialOtherDeductionMoneyColor("0");
			personSalarySupport.setSocialTotalDeductionMoneyColor("0");
			personSalarySupport.setSpecialChildrenEducationMoneyColor("0");
			personSalarySupport.setSpecialContinuEducationMoneyColor("0");
			personSalarySupport.setSpecialHotelInterestMoneyColor("0");
			personSalarySupport.setSpecialHotelRentMoneyColor("0");
			personSalarySupport.setSpecialSupportElderlyMoneyColor("0");
			personSalarySupport.setSpecialCommercialHealthInsuranceMoneyColor("0");
		}*/
		String systemRemark = personSalarySupport.getSystemRemark();
		systemRemark += name+remarkStr;
		personSalarySupport.setSystemRemark(systemRemark);
		personSalarySupportManager.save(personSalarySupport);
	}
	/**
	 * 获取当前人员负责的公司
	 * @return
	 * @author ckx
	 */
	public List<Map<String, Object>> getAllContractCompanyName() {
		String userId = currentUserHolder.getUserId();
		String sql = "select ac.union_id,ac.contract_company_id,pcc.contract_company_name,pcc.company_email from auth_contractdata ac left join person_contract_company pcc on ac.contract_company_id = pcc.id where pcc.isenable = '是' and ac.union_id = '"+userId+"';";
		List<Map<String,Object>> contractCompanyList = jdbcTemplate.queryForList(sql);
		return contractCompanyList;
	}
	/**
	 * @param name
	 * @author sjx
	 * 18.12.11
	 * 合同数据配置列表查询业务
	 * @return
	 */
	@Transactional(readOnly=true)
	public List<Map<String, Object>> queryAuthContractdata(String name){
		String sql = "";
		if(StringUtils.isBlank(name)){
			sql = "select ac.type,ac.union_id,GROUP_CONCAT(pcc.contract_company_name) as company_ids,pe.name,ac.note "
					+ "from person_contract_company pcc join auth_contractdata ac on pcc.id=ac.contract_company_id join party_entity pe on pe.ID=ac.union_id"
					+" GROUP BY ac.union_id";
		}else{
			sql = "select ac.type,ac.union_id,GROUP_CONCAT(pcc.contract_company_name) as company_ids,pe.name,ac.note" 
					+" from person_contract_company pcc join auth_contractdata ac on pcc.id=ac.contract_company_id join party_entity pe on pe.ID=ac.union_id where pe.`NAME` like'%"+name  
					+"%' GROUP BY ac.union_id";
		}
		List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);
		return list;
	}
	/**
	 * @author sjx
	 * 18.12.11
	 * 合同数据配置新建和编辑加载所有合同单位
	 * @return
	 */
	public List<Map<String, Object>> queryContractCompanyList(){
		String sql = "select * from person_contract_company";
		List<Map<String, Object>> contractCompanyList = jdbcTemplate.queryForList(sql);
		return contractCompanyList;
	}
	public Map<String, Object> queryContractByUnionId(String unionId){
		String sql = "SELECT a.union_id,a.type,a.note,"
					+"GROUP_CONCAT(a.contract_company_id) AS companyIds,"
					+"GROUP_CONCAT(pcc.contract_company_name) AS companyNames"
					+" FROM"
					+" auth_contractdata a"
					+" JOIN party_entity pe ON a.union_id = pe.ID"
					+" JOIN person_contract_company pcc ON pcc.id=a.contract_company_id"
					+" WHERE"
					+" a.union_id = "+unionId
					+" GROUP BY a.union_id";
	
		Map<String, Object> queryForMap = jdbcTemplate.queryForMap(sql);
		return queryForMap;
	}
	/**
	 * @param unionId
	 * @param contractCompany
	 * @param note
	 * @author sjx
	 * 18.12.11
	 * 保存合同单位配置信息
	 */
	@Transactional(readOnly=false)
	public void saveContractSetting(String unionId,String contractCompany,String note){
		String[] companyArray = contractCompany.split(",");
		for(String company:companyArray){
			AuthContractCompany authContract = new AuthContractCompany();
			authContract.setUnionId(unionId);
			authContract.setContractCompanyId(company);
			authContract.setType(1);
			authContract.setNote(note);
			authContractCompanyManager.save(authContract);
		}
	}
	/**
	 * @param unionId
	 * @author sjx
	 * 18.12.11
	 * 删除配置的数据（编辑保存也同样先删除再添加）
	 */
	@Transactional(readOnly=false)
	public void delContractSetting(String unionId){
		String sql = "delete from auth_contractdata where union_id="+unionId;
		jdbcTemplate.update(sql);
	}
	
	/**
	 * @param salaryColumn
	 * @return
	 * @author sjx
	 * 格式化用户选择的导出列，为逻辑运算使用
	 */
	public String[] forMatExportTerm(String[] salaryColumn){
		//为以及列头添加旗下子列头个数索引
		int paid = 0;//带薪假期
		int overtime = 0;//加班
		int deductionLeave = 0;//扣款假期
		int miner = 0;//矿工
		int welfare = 0;//福利补贴
		int deductionMoney = 0;//扣款元
		int insurance = 0;//保险公积金
		int special = 0;//专项附加扣除项目
		
		int num = salaryColumn.length;
		String[] forMatSalaryColumn = new String[num+1];
		for(int i=0;i<num;i++){
			if(salaryColumn[i].contains("级别")){
				forMatSalaryColumn[i] = "r级别-"+(i+5);//r表示该列需要跨行，c为跨列。(i+5)为排序
			}else if(salaryColumn[i].contains("入职时间")){
				forMatSalaryColumn[i] = "r入职时间-"+(i+5);
			}else if(salaryColumn[i].contains("应出勤天数")){
				forMatSalaryColumn[i] = "r应出勤天数-"+(i+5);
			}else if(salaryColumn[i].contains("出差天数")){
				paid++;
				forMatSalaryColumn[i] = "c出差天数-"+(i+5)+"|paid";
			}else if(salaryColumn[i].contains("调休天数")){
				paid++;
				forMatSalaryColumn[i] = "c调休天数-"+(i+5)+"|paid";
			}else if(salaryColumn[i].contains("年假天数")){
				paid++;
				forMatSalaryColumn[i] = "c年假天数-"+(i+5)+"|paid";
			}else if(salaryColumn[i].contains("产假天数")){
				paid++;
				forMatSalaryColumn[i] = "c产假天数-"+(i+5)+"|paid";
			}else if(salaryColumn[i].contains("1.5倍加班天数")){
				overtime++;
				forMatSalaryColumn[i] = "c1.5倍加班天数-"+(i+5)+"|overtime";
			}else if(salaryColumn[i].contains("2倍加班天数")){
				overtime++;
				forMatSalaryColumn[i] = "c2倍加班天数-"+(i+5)+"|overtime";
			}else if(salaryColumn[i].contains("3倍加班天数")){
				overtime++;
				forMatSalaryColumn[i] = "c3倍加班天数-"+(i+5)+"|overtime";
			}else if(salaryColumn[i].contains("缺勤天数")){
				forMatSalaryColumn[i] = "r缺勤天数-"+(i+5);
			}else if(salaryColumn[i].contains("病假天数")){
				deductionLeave++;
				forMatSalaryColumn[i] = "c病假天数-"+(i+5)+"|deductionLeave";
			}else if(salaryColumn[i].contains("事假天数")){
				deductionLeave++;
				forMatSalaryColumn[i] = "c事假天数-"+(i+5)+"|deductionLeave";
			}else if(salaryColumn[i].contains("半天旷工")){
				miner++;
				forMatSalaryColumn[i] = "c半天旷工-"+(i+5)+"|miner";
			}else if(salaryColumn[i].contains("一天以上旷工")){
				miner++;
				forMatSalaryColumn[i] = "c一天以上旷工-"+(i+5)+"|miner";
			}else if(salaryColumn[i].contains("迟到早退次数")){
				forMatSalaryColumn[i] = "r迟到早退次数-"+(i+5);
			}else if(salaryColumn[i].contains("实际出勤")){
				forMatSalaryColumn[i] = "r实际出勤-"+(i+5);
			}else if(salaryColumn[i].contains("工资标准")){
				forMatSalaryColumn[i] = "r工资标准-"+(i+5);
			}else if(salaryColumn[i].contains("基本工资")){
				forMatSalaryColumn[i] = "r基本工资-"+(i+5);
			}else if(salaryColumn[i].contains("职务工资")){
				forMatSalaryColumn[i] = "r职务工资-"+(i+5);
			}else if(salaryColumn[i].contains("技术工资")){
				forMatSalaryColumn[i] = "r技术工资-"+(i+5);
			}else if(salaryColumn[i].contains("保密津贴")){
				forMatSalaryColumn[i] = "r保密津贴-"+(i+5);
			}else if(salaryColumn[i].contains("技术津贴")){
				forMatSalaryColumn[i] = "r技术津贴-"+(i+5);
			}else if(salaryColumn[i].contains("绩效奖金")){
				forMatSalaryColumn[i] = "r绩效奖金-"+(i+5);
			}else if(salaryColumn[i].contains("转正扣款")){
				forMatSalaryColumn[i] = "r转正扣款-"+(i+5);
			}else if(salaryColumn[i].contains("月工资")){
				forMatSalaryColumn[i] = "r月工资-"+(i+5);
			}else if(salaryColumn[i].contains("入职年限")){
				welfare++;
				forMatSalaryColumn[i] = "c入职年限-"+(i+5)+"|welfare";
			}else if(salaryColumn[i].contains("交通补贴")){
				welfare++;
				forMatSalaryColumn[i] = "c交通补贴-"+(i+5)+"|welfare";
			}else if(salaryColumn[i].contains("住宿补贴")){
				welfare++;
				forMatSalaryColumn[i] = "c住宿补贴-"+(i+5)+"|welfare";
			}else if(salaryColumn[i].contains("司龄补贴")){
				welfare++;
				forMatSalaryColumn[i] = "c司龄补贴-"+(i+5)+"|welfare";
			}else if(salaryColumn[i].contains("餐费补贴")){
				welfare++;
				forMatSalaryColumn[i] = "c餐费补贴-"+(i+5)+"|welfare";
			}else if(salaryColumn[i].contains("通讯补贴")){
				welfare++;
				forMatSalaryColumn[i] = "c通讯补贴-"+(i+5)+"|welfare";
			}else if(salaryColumn[i].contains("其他补贴")){
				welfare++;
				forMatSalaryColumn[i] = "c其他补贴-"+(i+5)+"|welfare";
			}else if(salaryColumn[i].contains("加班费")){
				forMatSalaryColumn[i] = "r加班费-"+(i+5);
			}else if(salaryColumn[i].contains("缺勤扣款")){
				deductionMoney++;
				forMatSalaryColumn[i] = "c缺勤扣款-"+(i+5)+"|deductionMoney";
			}else if(salaryColumn[i].contains("病假扣款")){
				deductionMoney++;
				forMatSalaryColumn[i] = "c病假扣款-"+(i+5)+"|deductionMoney";
			}else if(salaryColumn[i].contains("事假扣款")){
				deductionMoney++;
				forMatSalaryColumn[i] = "c事假扣款-"+(i+5)+"|deductionMoney";
			}else if(salaryColumn[i].contains("旷工扣款")){
				deductionMoney++;
				forMatSalaryColumn[i] = "c旷工扣款-"+(i+5)+"|deductionMoney";
			}else if(salaryColumn[i].contains("迟到早退扣款")){
				deductionMoney++;
				forMatSalaryColumn[i] = "c迟到早退扣款-"+(i+5)+"|deductionMoney";
			}else if(salaryColumn[i].contains("补杂项")){
				forMatSalaryColumn[i] = "r补杂项-"+(i+5);
			}else if(salaryColumn[i].contains("应发工资")){
				forMatSalaryColumn[i] = "r应发工资-"+(i+5);
			}else if(salaryColumn[i].contains("养老扣款")){
				insurance++;
				forMatSalaryColumn[i] = "c养老扣款-"+(i+5)+"|insurance";
			}else if(salaryColumn[i].contains("失业扣款")){
				insurance++;
				forMatSalaryColumn[i] = "c失业扣款-"+(i+5)+"|insurance";
			}else if(salaryColumn[i].contains("医疗扣款")){
				insurance++;
				forMatSalaryColumn[i] = "c医疗扣款-"+(i+5)+"|insurance";
			}else if(salaryColumn[i].contains("公积金扣款")){
				insurance++;
				forMatSalaryColumn[i] = "c公积金扣款-"+(i+5)+"|insurance";
			}else if(salaryColumn[i].contains("其他项扣款")){
				insurance++;
				forMatSalaryColumn[i] = "c其他项扣款-"+(i+5)+"|insurance";
			}else if(salaryColumn[i].contains("保险扣款合计")){
				insurance++;
				forMatSalaryColumn[i] = "c保险扣款合计-"+(i+5)+"|insurance";
			}else if(salaryColumn[i].contains("税前工资")){
				forMatSalaryColumn[i] = "r税前工资-"+(i+5);
			}else if(salaryColumn[i].contains("子女教育")){
				special++;
				forMatSalaryColumn[i] = "c子女教育-"+(i+5)+"|special";
			}else if(salaryColumn[i].contains("继续教育")){
				special++;
				forMatSalaryColumn[i] = "c继续教育-"+(i+5)+"|special";
			}else if(salaryColumn[i].contains("住房贷款利息")){
				special++;
				forMatSalaryColumn[i] = "c住房贷款利息-"+(i+5)+"|special";
			}else if(salaryColumn[i].contains("住房租金")){
				special++;
				forMatSalaryColumn[i] = "c住房租金-"+(i+5)+"|special";
			}else if(salaryColumn[i].contains("赡养老人")){
				special++;
				forMatSalaryColumn[i] = "c赡养老人-"+(i+5)+"|special";
			}else if(salaryColumn[i].contains("商业健康险")){
				special++;
				forMatSalaryColumn[i] = "c商业健康险-"+(i+5)+"|special";
			}else if(salaryColumn[i].contains("纳税工资")){
				forMatSalaryColumn[i] = "r纳税工资-"+(i+5);
			}else if(salaryColumn[i].contains("个人所得税")){
				forMatSalaryColumn[i] = "r个人所得税-"+(i+5);
			}else if(salaryColumn[i].contains("实发工资")){
				forMatSalaryColumn[i] = "r实发工资-"+(i+5);
			}else if(salaryColumn[i].contains("备注")){
				forMatSalaryColumn[i] = "r备注-"+(i+5);
			}
		}
		String indexParam = paid+","+overtime+","+deductionLeave+","+miner+","+welfare+","+deductionMoney+","+insurance+","+special;
		forMatSalaryColumn[num] = indexParam;
		
		return forMatSalaryColumn;
	}
	/**
	 * @param contractCompanyId
	 * @param source
	 * @return
	 * @author sjx
	 * 通过薪资合同单位Id查询
	 */
	@Transactional(readOnly=true)
	public Map<String,Object> infoByContractCompanyId(String contractCompanyId,int source){
		String sql = "select * from person_contract_company where id="+contractCompanyId;
		Map<String, Object> info = jdbcTemplate.queryForMap(sql);
		return info;
	}
	
	private String getNumber(String fieldValue){
		if(StringUtils.isBlank(fieldValue)){
			return fieldValue;
		}
		int length=fieldValue.length();
		if (length>=2) {  //这里大于等于2是防止有些列只有一个字符，到下面会报错
			if(fieldValue.contains(".")){
				int lastIndexOf = fieldValue.lastIndexOf(".");
				String substring = fieldValue.substring(lastIndexOf+1, length);
				if(substring.length()<2){
					fieldValue = fieldValue+"0";
				}
			}else{
				fieldValue = fieldValue+".00";
			}
		}else{
			fieldValue = fieldValue+".00";
		}
		return fieldValue;
	}
	@Value("${salary.des.key}")
	public void setSalaryDesKey(String salaryDesKey) {
		this.salaryDesKey = salaryDesKey;
	}














}
