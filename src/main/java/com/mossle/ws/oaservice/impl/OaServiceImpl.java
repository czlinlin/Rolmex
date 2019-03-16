package com.mossle.ws.oaservice.impl;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import javax.annotation.Resource;
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.persistence.Id;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartResolver;

import com.mossle.api.humantask.HumanTaskConnector;
import com.mossle.api.keyvalue.FormParameter;
import com.mossle.api.keyvalue.KeyValueConnector;
import com.mossle.api.keyvalue.Record;
import com.mossle.api.process.ProcessConnector;
import com.mossle.api.process.ProcessDTO;
import com.mossle.api.srmb.SrmbDTO;
import com.mossle.api.store.StoreConnector;
import com.mossle.api.tenant.TenantHolder;
import com.mossle.base.persistence.domain.BusinessDetailEntity;
import com.mossle.base.persistence.manager.BusinessDetailManager;
import com.mossle.common.utils.DateUtils;
import com.mossle.common.utils.WebAPI;
import com.mossle.core.MultipartHandler;
import com.mossle.core.auth.CurrentUserHolder;
import com.mossle.core.id.IdGenerator;
import com.mossle.core.mapper.JsonMapper;
import com.mossle.core.spring.DateConverter;
import com.mossle.core.spring.SignInfo;
import com.mossle.core.util.StringUtils;
import com.mossle.dict.persistence.domain.DictInfo;
import com.mossle.dict.support.DictConnectorImpl;
import com.mossle.humantask.persistence.domain.TaskInfo;
import com.mossle.humantask.persistence.manager.TaskInfoManager;
import com.mossle.keyvalue.persistence.domain.RecordInfo;
import com.mossle.keyvalue.persistence.manager.RecordManager;
import com.mossle.operation.persistence.domain.CodeEntity;
import com.mossle.operation.persistence.manager.CodeManager;
import com.mossle.operation.service.OperationService;
import com.mossle.operation.service.WSApplyService;
import com.mossle.party.PartyConstants;
import com.mossle.ws.oaservice.OaService;
import com.mossle.ws.online.Common;
import com.mossle.ws.persistence.domain.OnLineInfo;
import com.mossle.ws.persistence.manager.OnLineInfoManager;
// import java.time.format.DateTimeFormatter;
//import java.time.format.DateTimeFormatter;

@WebService(endpointInterface = "com.mossle.ws.oaservice.OaService")
@Transactional(readOnly = true)
public class OaServiceImpl implements OaService {
	//private Logger logger=new org.hsqldb.persist.Logger(arg0);
    private static org.slf4j.Logger logger = LoggerFactory.getLogger(OaService.class);
	private OnLineInfoManager onLineInfoManager;
	private WebAPI webAPI;
	//private FileUploadAPI fileUploadAPI;
	private StoreConnector storeConnector;	
	private TenantHolder tenantHolder;
	private KeyValueConnector keyValueConnector;
	private JdbcTemplate jdbcTemplate;
	private JsonMapper jsonMapper = new JsonMapper();
	private CodeManager codeManager;
	private ProcessConnector processConnector;
	private OperationService operationService;
	private BusinessDetailManager businessDetailManager;
	private CurrentUserHolder currentUserHolder;
	private MultipartResolver multipartResolver;
	private DictConnectorImpl dictConnectorImpl ;
    private HumanTaskConnector humanTaskConnector;
    private TaskInfoManager taskInfoManager;
    private String _ShorName="OAJXS";
    private IdGenerator idGenerator;    
    private SignInfo signInfo;
    private WSApplyService wSApplyService;
    private RecordManager recordManager;
	
	@Override
	public String sayHello(String name) throws Exception {
		//String paramKey=signInfo.getParamKey();
		//String strCodeString=Common.Decrypt3DES(name,paramKey);
		//String strTestString=Common.Encrypt3DES("123456",paramKey);
		//String strTestDeString=Common.Decrypt3DES(strTestString,paramKey);
		//return "hello，"+strCodeString+"|"+strTestString+"|"+strTestDeString;
		
		String sqlByName="select * from account_info where display_name='%s' and del_flag='0'";
		String sqlByUserName="select * from account_info where username='%s' and del_flag='0'";
		String sql="";
		String  auditor=name;
		switch(auditor.trim()){
		case "陈娟":
			sql=String.format(sqlByUserName,"zjchenjuan");
			break;
		case "华东湖北":
			sql=String.format(sqlByUserName,"hbchengguangming");
			break;
		case "华北天津":
			sql=String.format(sqlByUserName,"liweifeng");
			break;
		case "华北山东":
			sql=String.format(sqlByUserName,"sdyuchao");
			break;
		case "周靖欣":
			sql=String.format(sqlByUserName,"gzleibangqin");
			break;
		case "四川专员":
			sql=String.format(sqlByUserName,"scyanglin");
			break;
		case "山东专员":
			sql=String.format(sqlByUserName,"sdhuyinyin");
			break;
		case "李明":
			sql=String.format(sqlByUserName,"hbduanyuan");
			break;
		case "江西专员":
			sql=String.format(sqlByUserName,"jxhuyanmei");
			break;
		case "深圳专员":
			sql=String.format(sqlByUserName,"szliaolin");
			break;
		case "辽宁专员":
			sql=String.format(sqlByUserName,"lnchenfeng");
			break;
		case "郭阳洋":
			sql=String.format(sqlByUserName,"cqxiangdongmei");
			break;
		case "陕西":
			sql=String.format(sqlByUserName,"shxzhuqian");
			break;
		case "王晓冉":
			sql=String.format(sqlByUserName,"3moons");
			break;
		default:
			sql=String.format(sqlByName,auditor);
			break;
		}
		return "输出："+sql;
	}
	
	//region 添加申请(包括14钟)
		/// <summary>
	    /// 添加申请
	    /// songjie 2017/03/03 调整至审批流转
	    /// 直系亲属资格替换：营销一分部证件、营销二分部证件、营销三分部证件、亲属证明
	    /// </summary>
	    /// <param name="varOlCode">记录编号</param>
	    /// <param name="varApplyCode">申请人编号</param>
	    /// <param name="varBranch">所属部门编号</param>
	    /// <param name="varApplyName">申请人姓名</param>
	    /// <param name="varApplyIdentity">申请人证件号</param>
	    /// <param name="varWelfareGrade">福利级别(名称)</param>
	    /// <param name="varMobile">短信接收号码(密码初始化)/原手机号(更正手机号)</param>
	    /// <param name="varNewName">新姓名(更正姓名/直系亲属资格替换)</param>
	    /// <param name="varNewIdentity">新证件号(身份证更正/直系亲属资格替换)</param>
	    /// <param name="varBankName">新银行名称</param>
	    /// <param name="varBankAddress">新银行地址(直系亲属资格替换)/预开设地址((非)12万旗舰店)</param>
	    /// <param name="varBankCode">代理人编号(密码初始化)/新手机号(更正手机号)/新银行帐号(直系亲属资格替换)</param>
	    /// <param name="varApplyPic">申请人证件</param>
	    /// <param name="varNewPic">新人证件(直系亲属资格替换)/营业执照(12万旗舰店)</param>
	    /// <param name="varPartPic1">营销一分部证件(直系亲属资格替换)/收件人姓名((非)12万旗舰店)</param>
	    /// <param name="varPartPic2">营销二分部证件(直系亲属资格替换)/收件人电话((非)12万旗舰店)</param>
	    /// <param name="varPartPic3">营销三分部证件(直系亲属资格替换)/授权书邮寄地址((非)12万旗舰店)</param>
	    /// <param name="varRelativesPic">亲属证明</param>
	    /// <param name="chrApplyType">申请类型(1：密码初始化；2：更正姓名；3：身份证更正；4：资格注销；5：直系亲属资格替换；6：更正手机号；7：开通订货及转账权限；8：12万旗舰店申请；9：非12万旗舰店申请；10：12万旗舰店续约；11：非12万旗舰店续约；12：12万旗舰店协议；13：非12万旗舰店协议；14：代理商协议)</param>
	    /// <param name="varReason">申请原因</param>
		/// <param name="strShopLicense">统一社会信用代码</param>
		/// <param name="strEnterpriseName">企业名称</param>
		/// <param name="strLegaler">法定代表人</param>
		/// <param name="strDistributorPhone">联系电话</param>
		/// <param name="strScopeBusiness">经营范围</param>
		/// <param name="strNote">备注</param>
		/// <param name="strPublicAccount">对公账户行号</param>
	    /// <param name="varAccountType">账户类型</param>
	    /// <param name="varOpeningBank">开户行</param>
	    /// <param name="varAccountNumber">账号</param>
	    /// <param name="varStoreArea">实体店面积</param>
	    /// <param name="strMsg">签名</param>
	    /// <returns>返回结果</returns>
		@WebMethod
		@Transactional(readOnly = false)
		public boolean CreateApply( String varOlCode, 
	    		String varApplyCode, 
	    		String varBranch, 
	    		String varApplyName,
	    		String varApplyIdentity, 
	    		String varWelfareGrade, 
	    		String varMobile, 
	    		String varNewName, 
	    		String varNewIdentity,
	    		String varBankName,
	    		String varBankAddress,
	    		String varBankCode, 
	    		String varApplyPic, 
	    		String varNewPic, 
	    		String varPartPic1, 
	    		String varPartPic2,
	    		String varPartPic3, 
	    		String varRelativesPic,
	    		String chrApplyType, 
	    		String varReason,
	    		String strShopLicense,
	    		String strEnterpriseName,
	    		String strLegaler,
	    		String strLegalerIdCard,
	    		String strDistributorPhone,
	    		String strScopeBusiness,
	    		String strNote,
	    		String strPublicAccount,
	    		String varAccountType,
	    		String varOpeningBank,
	    		String varOpeningName,
	    		String varAccountNumber,
	    		String varStoreArea,
	    		String strMsg
	    		){
			try {
				
			String paramKey=signInfo.getParamKey();	
			//解析参数
			varOlCode = Common.Decrypt3DES(varOlCode,paramKey);
	        varApplyCode = Common.Decrypt3DES(varApplyCode,paramKey);
	        varBranch = Common.Decrypt3DES(varBranch,paramKey);
	        varApplyName = Common.Decrypt3DES(varApplyName,paramKey);
	        varApplyIdentity = Common.Decrypt3DES(varApplyIdentity,paramKey);
	        varWelfareGrade = Common.Decrypt3DES(varWelfareGrade,paramKey);
	        varMobile = Common.Decrypt3DES(varMobile,paramKey);
	        varNewName = Common.Decrypt3DES(varNewName,paramKey);
	        varNewIdentity = Common.Decrypt3DES(varNewIdentity,paramKey);
	        varBankName = Common.Decrypt3DES(varBankName,paramKey);
	        varBankAddress = Common.Decrypt3DES(varBankAddress,paramKey);
	        varBankCode = Common.Decrypt3DES(varBankCode,paramKey);
	        varApplyPic = Common.Decrypt3DES(varApplyPic,paramKey);
	        varNewPic = Common.Decrypt3DES(varNewPic,paramKey);
	        varPartPic1 = Common.Decrypt3DES(varPartPic1,paramKey);
	        varPartPic2 = Common.Decrypt3DES(varPartPic2,paramKey);
	        varPartPic3 = Common.Decrypt3DES(varPartPic3,paramKey);
	        varRelativesPic = Common.Decrypt3DES(varRelativesPic,paramKey);
	        chrApplyType = Common.Decrypt3DES(chrApplyType,paramKey);
	        varReason = Common.Decrypt3DES(varReason,paramKey);
	        strShopLicense= Common.Decrypt3DES(strShopLicense,paramKey);
	        strEnterpriseName= Common.Decrypt3DES(strEnterpriseName,paramKey);
	        strLegaler= Common.Decrypt3DES(strLegaler,paramKey);
	        strLegalerIdCard= Common.Decrypt3DES(strLegalerIdCard,paramKey);
	        strDistributorPhone= Common.Decrypt3DES(strDistributorPhone,paramKey);
	        strScopeBusiness= Common.Decrypt3DES(strScopeBusiness,paramKey);
	        strNote= Common.Decrypt3DES(strNote,paramKey);
	        strPublicAccount= Common.Decrypt3DES(strPublicAccount,paramKey);
	        varAccountType= Common.Decrypt3DES(varAccountType,paramKey);
	        varOpeningBank= Common.Decrypt3DES(varOpeningBank,paramKey);
	        varOpeningName= Common.Decrypt3DES(varOpeningName,paramKey);
	        varAccountNumber= Common.Decrypt3DES(varAccountNumber,paramKey);
	        varStoreArea= Common.Decrypt3DES(varStoreArea,paramKey);
	        
			//记录参数info日志
			logger.info("Rolmex.OA.WebService.ToOtherPlatService.cs--CreateApply(直销OA-添加申请) 参数：varOlCode=" 
			+ varOlCode + ",varApplyCode=" + varApplyCode 
			+ ",varBranch=" + varBranch + ",varApplyName=" + varApplyName
			+ ",varApplyIdentity=" + varApplyIdentity + ",varMobile=" + varMobile 
			+ ",varWelfareGrade=" + varWelfareGrade + ",varNewName=" + varNewName 
			+ ",varNewIdentity=" + varNewIdentity + ",varBankName=" + varBankName 
			+ ",varBankAddress=" + varBankAddress + ",varBankCode=" + varBankCode + 
			",varApplyPic=" + varApplyPic + ",varNewPic=" + varNewPic 
			+ ",varPartPic1=" + varPartPic1 + ",varPartPic2=" + varPartPic2 
			+ ",varPartPic3=" + varPartPic3 + ",varRelativesPic=" + varRelativesPic 
			+ ",chrApplyType=" + chrApplyType + ",varReason=" + varReason+",strShopLicense="+strShopLicense
			+ ",strEnterpriseName=" + strEnterpriseName + ",strLegaler=" + strLegaler+",strDistributorPhone="+strDistributorPhone
			+ ",strScopeBusiness=" + strScopeBusiness + ",strNote=" + strNote+",strPublicAccount="+strPublicAccount
			+ ",varAccountType=" + varAccountType + ",varOpeningBank=" + varOpeningBank +",varOpeningName="+ varOpeningName +",varAccountNumber="+varAccountNumber+",varStoreArea="+varStoreArea);
			
			//验证签名
	        String strKey = varOlCode.toUpperCase()
	                        + varApplyCode.toUpperCase()
	                        + varBranch.toUpperCase()
	                        + varApplyName.toUpperCase()
	                        + varApplyIdentity.toUpperCase()
	                        + varWelfareGrade.toUpperCase()
	                        + varMobile.toUpperCase()
	                        + varNewName.toUpperCase()
	                        + varNewIdentity.toUpperCase()
	                        + varBankName.toUpperCase()
	                        + varBankAddress.toUpperCase()
	                        + varBankCode.toUpperCase()
	                        + varApplyPic.toUpperCase()
	                        + varNewPic.toUpperCase()
	                        + varPartPic1.toUpperCase()
	                        + varPartPic2.toUpperCase()
	                        + varPartPic3.toUpperCase()
	                        + varRelativesPic.toUpperCase()
	                        + chrApplyType.toUpperCase()
	                        + varReason.toUpperCase()
	                        + strShopLicense.toUpperCase()
	                        + strEnterpriseName.toUpperCase()
	                        + strLegaler.toUpperCase()
	                        + strLegalerIdCard.toUpperCase()
	                        + strDistributorPhone.toUpperCase()
	                        + strScopeBusiness.toUpperCase()
	                        + strNote.toUpperCase()
	                        + strPublicAccount.toUpperCase()
	                        + varAccountType.toUpperCase()
	                        + varOpeningBank.toUpperCase()
	                        + varOpeningName.toUpperCase()
	                        + varAccountNumber.toUpperCase()
	                        + varStoreArea.toUpperCase();
	        String signKey=signInfo.getSignKey();
	        if (!Common.VerifySign(strKey, strMsg,signKey))
	        {
	        	logger.info("Rolmex.OA.WebService.ToOtherPlatService.cs--CreateApply(添加申请):验证签名失败");
	            return false;
	        }
	        
	        String strBranch="";
	        switch(varBranch.toUpperCase())
	        {
	            case "F01":
	                strBranch="一部";
	                break;
	            case "F02":
	                strBranch="二部";
	                break;
	            case "F04":
	                strBranch="四部";
	                break;
	        }
	        Date addDate=new Date();
	        OnLineInfo onLineInfo=new OnLineInfo();
	        onLineInfo.setUcode(varApplyCode);
	        onLineInfo.setApplycode(varOlCode);
	        onLineInfo.setBranch(strBranch);
	        onLineInfo.setName(varApplyName);
	        onLineInfo.setIdentity(varApplyIdentity);
	        onLineInfo.setWelfaregrade(varWelfareGrade);
	        onLineInfo.setMobile(varMobile);
	        onLineInfo.setNewname(varNewName);
	        onLineInfo.setNewidentity(varNewIdentity);
	        onLineInfo.setBankname(varBankName);
	        onLineInfo.setBankaddress(varBankAddress);
	        onLineInfo.setBankcode(varBankCode);
	        onLineInfo.setApplypic(varApplyPic);
	        onLineInfo.setNewpic(varNewPic);
	        onLineInfo.setPartpic1(varPartPic1);
	        onLineInfo.setPartpic2(varPartPic2);
	        onLineInfo.setPartpic3(varPartPic3);
	        onLineInfo.setRelativespic(varRelativesPic);
	        onLineInfo.setApplytype(chrApplyType);
	        onLineInfo.setReason(varReason);
	        onLineInfo.setRecordid(0L);
	        onLineInfo.setApplytime(addDate);
	        onLineInfo.setShopLicense(strShopLicense);
	        onLineInfo.setEnterpriseName(strEnterpriseName);
	        onLineInfo.setLegaler(strLegaler);
	        onLineInfo.setLegalerIdCard(strLegalerIdCard);
	        onLineInfo.setDistributorPhone(strDistributorPhone);
	        onLineInfo.setScopeBusiness(strScopeBusiness);
	        onLineInfo.setNote(strNote);
	        onLineInfo.setPublicAccount(strPublicAccount);
	        onLineInfo.setAccountType(varAccountType);
	        onLineInfo.setOpeningBank(varOpeningBank);
	        onLineInfo.setOpeningName(varOpeningName);
	        onLineInfo.setAccountNumbr(varAccountNumber);
	        onLineInfo.setStoreArea(varStoreArea);
	        
	        //region 暂时注释，方法挪到Service来控制事务
	        /*onLineInfoManager.save(onLineInfo);
        	//发起流程. chengze 20171028
	        Map<String, Object> processParameters = new HashMap<String, Object>();
	        String userId = PartyConstants.SYSTEM_ROBOT_ID;
	        String businessKey = "";
	        String FormId =Long.toString(onLineInfo.getId());
	        
	        //先获取流程id
	        String hql = "from BusinessDetailEntity where  id=? and formid='oaServicePushProcess'";
	        BusinessDetailEntity businessDetailEntity = businessDetailManager.findUnique(hql, Long.valueOf(chrApplyType));
	    	String bpmProcessId = businessDetailEntity.getBpmProcessId();
	        
	    	//创建formParameter
	    	SimpleDateFormat formatDate= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    	MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<String, String>();
	        multiValueMap.add("ucode", onLineInfo.getUcode());
	        multiValueMap.add("applyCode", onLineInfo.getApplycode());
	        multiValueMap.add("theme", businessDetailEntity.getBusiDetail()+formatDate.format(addDate));
	        multiValueMap.add("busType", String.valueOf(businessDetailEntity.getTypeId()));
	        multiValueMap.add("businessType", businessDetailEntity.getBusinessType());
	        multiValueMap.add("busDetails", String.valueOf(businessDetailEntity.getId()));
	        multiValueMap.add("businessDetail", businessDetailEntity.getBusiDetail());
	        multiValueMap.add("url", "");
	        
	        FormParameter formParameter = this.operationService.saveDraft(userId, "1", "", businessKey, bpmProcessId, multiValueMap);
	        ProcessDTO processDto = processConnector.findProcess(bpmProcessId);
	        String processDefinitionId = processDto.getProcessDefinitionId();
	        Record record = keyValueConnector.findByCode(formParameter.getBusinessKey());
	        businessKey = formParameter.getBusinessKey();
	        //发起流程
	        this.operationService.startProcessInstance(userId, businessKey,
	                processDefinitionId, processParameters, record);
	        
	        Map<String,Object> modelMap=new HashMap<String, Object>();
	        modelMap.put("olcode", onLineInfo.getApplycode());
	        modelMap.put("applycode", onLineInfo.getUcode());
	        modelMap.put("depart", onLineInfo.getBranch());
	        modelMap.put("applyname", onLineInfo.getName());
	        modelMap.put("cardid", onLineInfo.getIdentity());
	        modelMap.put("welfare", onLineInfo.getWelfaregrade());
	        modelMap.put("newname", onLineInfo.getNewname());
	        modelMap.put("newcardid", onLineInfo.getNewidentity());
	        modelMap.put("newbank", onLineInfo.getBankname());
	        modelMap.put("newaccount", onLineInfo.getBankcode());
	        modelMap.put("bankaddress", onLineInfo.getBankaddress());
	        modelMap.put("mobile", onLineInfo.getMobile());
	        modelMap.put("newcardid", onLineInfo.getNewidentity());
	        modelMap.put("agent",onLineInfo.getBankcode());
	        
	        String tenanId = tenantHolder.getTenantId();
	        
	        //证件图
	        modelMap.put("applypic",UploadPicAndGetPicHtml(onLineInfo.getApplypic(), tenanId, onLineInfo.getId().toString()));
	        modelMap.put("newpic", UploadPicAndGetPicHtml(onLineInfo.getNewpic(), tenanId, onLineInfo.getId().toString()));
	        modelMap.put("part1pic", UploadPicAndGetPicHtml(onLineInfo.getPartpic1(), tenanId, onLineInfo.getId().toString()));
	        modelMap.put("part2pic", UploadPicAndGetPicHtml(onLineInfo.getPartpic2(), tenanId, onLineInfo.getId().toString()));
	        modelMap.put("part3pic", UploadPicAndGetPicHtml(onLineInfo.getPartpic3(), tenanId, onLineInfo.getId().toString()));
	        modelMap.put("relativespic", UploadPicAndGetPicHtml(onLineInfo.getRelativespic(), tenanId, onLineInfo.getId().toString()));
	        
	        modelMap.put("applyreason", onLineInfo.getReason());
	        modelMap.put("buslicense", UploadPicAndGetPicHtml(onLineInfo.getNewpic(), tenanId, onLineInfo.getId().toString()));//营业执照编号
	        modelMap.put("address", onLineInfo.getBankaddress());
	        
	        modelMap.put("receivename", onLineInfo.getPartpic1());
	        modelMap.put("reveicemobile", onLineInfo.getPartpic2());
	        modelMap.put("authaddress", onLineInfo.getPartpic3());
	        
	        SaveHtmlForm(onLineInfo,modelMap,onLineInfo.getId().toString(),businessKey);*/
	        //endregion
	        return wSApplyService.CreateApply(onLineInfo);
			} catch (Exception ex) {
				logger.info("CheckApplyStatus(判断在线办公申请是否存在处理中的申请数据)，出现异常："+ex.getMessage()+"\r\n"+ex.getStackTrace());
				return false;
			}
		}
		
		//region 暂时注释，方法挪到Service来控制事务
		/*
		//表单
		private void SaveHtmlForm(OnLineInfo model,Map<String,Object> modelMap,String pkId,String businessKey){
			String strHtml="";
			switch (model.getApplytype())
			{
				case "1":   //密码初始化
					strHtml = "<table class=\"centerdiv\" style=\"line-height: 34px; border: 1px solid;\" cellpadding=\"0\" cellspacing=\"0\"><tr><td width=\"100px\" class=\"f_td\"><span id=\"tag_olCode\">受理编号</span>：</td><td class=\"f_r_td\">{olcode}</td><td style=\"width: 100px\" class=\"f_td\"><span id=\"tag_applyCode\">申请人编号</span>：</td><td class=\"f_r_td\">{applycode}</td></tr><tr><td class=\"f_td\"><span id=\"tag_depart\">所属部门</span>：</td><td class=\"f_r_td\">{depart}</td><td class=\"f_td\"><span id=\"tag_applyName\">申请人姓名</span>：</td><td class=\"f_r_td\">{applyname}</td></tr><tr><td class=\"f_td\"><span id=\"tag_cardID\">申请人证件号</span>：</td><td class=\"f_r_td\">{cardid}</td><td class=\"f_td\"><span id=\"tag_welfare\">申请人福利级别</span>：</td><td class=\"f_r_td\">{welfare}</td></tr><tr><td class=\"f_td\"><span id=\"tag_mobile\">短信接收号码</span>：</td><td class=\"f_r_td\">{mobile}</td><td class=\"f_td\"><span id=\"tag_agent\">代理人编号</span>：</td><td class=\"f_r_td\">{agent}</td></tr><tr><td class=\"f_td\"><span id=\"tag_bustype\">申请业务类型</span>：</td><td class=\"f_r_td\">即时办理业务</td><td class=\"f_td\"><span id=\"tag_busDetails\">业务细分</span>：</td><td class=\"f_r_td\">密码初始化</td></tr><tr><td class=\"f_td\"><span id=\"tag_applyPic\">申请人证件</span>：</td><td class=\"f_r_td\">{applypic}</td><td class=\"f_td\"></td><td class=\"f_r_td\"></td></tr><tr><td colspan=\"4\" align=\"center\" class=\"f_td\">申请原因</td></tr><tr><td colspan=\"4\" style=\"height: 100px\" class=\"f_r_td\">{applyreason}</td></tr></table>";
					break;
				case "2":   //更正姓名
					strHtml = "<table class=\"centerdiv\" style=\"line-height: 34px; border: 1px solid;\" cellpadding=\"0\" cellspacing=\"0\"><tr><td width=\"100px\" class=\"f_td\"><span id=\"tag_olCode\">受理编号</span>：</td><td class=\"f_r_td\">{olcode}</td><td style=\"width: 100px\" class=\"f_td\"><span id=\"tag_applyCode\">申请人编号</span>：</td><td class=\"f_r_td\">{applycode}</td></tr><tr><td class=\"f_td\"><span id=\"tag_depart\">所属部门</span>：</td><td class=\"f_r_td\">{depart}</td><td class=\"f_td\"><span id=\"tag_applyName\">申请人姓名</span>：</td><td class=\"f_r_td\">{applyname}</td></tr><tr><td class=\"f_td\"><span id=\"tag_cardID\">申请人证件号</span>：</td><td class=\"f_r_td\">{cardid}</td><td class=\"f_td\"><span id=\"tag_newName\">新姓名</span>：</td><td class=\"f_r_td\">{newname}</td></tr><tr><td class=\"f_td\"><span id=\"tag_welfare\">申请人福利级别</span>：</td><td class=\"f_r_td\">{welfare}</td><td class=\"f_td\"></td><td class=\"f_r_td\"></td></tr><tr><td class=\"f_td\"><span id=\"tag_bustype\">申请业务类型</span>：</td><td class=\"f_r_td\">即时办理业务</td><td class=\"f_td\"><span id=\"tag_busDetails\">业务细分</span>：</td><td class=\"f_r_td\">更正姓名</td></tr><tr><td class=\"f_td\"><span id=\"tag_applyPic\">申请人证件</span>：</td><td class=\"f_r_td\">{applypic}</td><td class=\"f_td\"></td><td class=\"f_r_td\"></td></tr><tr><td colspan=\"4\" align=\"center\" class=\"f_td\">申请原因</td></tr><tr><td colspan=\"4\" style=\"height: 100px\" class=\"f_r_td\">{applyreason}</td></tr></table>";
					break;
				case "3":   //身份证更正
					strHtml = "<table class=\"centerdiv\" style=\"line-height: 34px; border: 1px solid;\" cellpadding=\"0\" cellspacing=\"0\"><tr><td width=\"100px\" class=\"f_td\"><span id=\"tag_olCode\">受理编号</span>：</td><td class=\"f_r_td\">{olcode}</td><td style=\"width: 100px\" class=\"f_td\"><span id=\"tag_applyCode\">申请人编号</span>：</td><td class=\"f_r_td\">{applycode}</td></tr><tr><td class=\"f_td\"><span id=\"tag_depart\">所属部门</span>：</td><td class=\"f_r_td\">{depart}</td><td class=\"f_td\"><span id=\"tag_applyName\">申请人姓名</span>：</td><td class=\"f_r_td\">{applyname}</td></tr><tr><td class=\"f_td\"><span id=\"tag_cardID\">申请人证件号</span>：</td><td class=\"f_r_td\">{cardid}</td><td class=\"f_td\"><span id=\"tag_newCardID\">新证件号</span>：</td><td class=\"f_r_td\">{newcardid}</td></tr><tr><td class=\"f_td\"><span id=\"tag_welfare\">申请人福利级别</span>：</td><td class=\"f_r_td\">{welfare}</td><td class=\"f_td\"></td><td class=\"f_r_td\"></td></tr><tr><td class=\"f_td\"><span id=\"tag_bustype\">申请业务类型</span>：</td><td class=\"f_r_td\">即时办理业务</td><td class=\"f_td\"><span id=\"tag_busDetails\">业务细分</span>：</td><td class=\"f_r_td\">身份证更正</td></tr><tr><td class=\"f_td\"><span id=\"tag_applyPic\">申请人证件</span>：</td><td class=\"f_r_td\">{applypic}</td><td class=\"f_td\"></td><td class=\"f_r_td\"></td></tr><tr><td colspan=\"4\" align=\"center\" class=\"f_td\">申请原因</td></tr>tr><td colspan=\"4\" style=\"height: 100px\" class=\"f_r_td\">{applyreason}</td></tr></table>";
					break;
				case "4":   //资格注销
					strHtml = "<table class=\"centerdiv\" style=\"line-height: 34px; border: 1px solid;\" cellpadding=\"0\" cellspacing=\"0\"><tr><td width=\"100px\" class=\"f_td\"><span id=\"tag_olCode\">受理编号</span>：</td><td class=\"f_r_td\">{olcode}</td><td style=\"width: 100px\" class=\"f_td\"><span id=\"tag_applyCode\">申请人编号</span>：</td><td class=\"f_r_td\">{applycode}</td></tr><tr><td class=\"f_td\"><span id=\"tag_depart\">所属部门</span>：</td><td class=\"f_r_td\">{depart}</td><td class=\"f_td\"><span id=\"tag_applyName\">申请人姓名</span>：</td><td class=\"f_r_td\">{applyname}</td></tr><tr><td class=\"f_td\"><span id=\"tag_cardID\">申请人证件号</span>：</td><td class=\"f_r_td\">{cardid}</td><td class=\"f_td\"><span id=\"tag_welfare\">申请人福利级别</span>：</td><td class=\"f_r_td\">{welfare}</td></tr><tr><td class=\"f_td\"><span id=\"tag_busType\">申请业务类型</span>：</td><td class=\"f_r_td\">即时办理业务</td><td class=\"f_td\"><span id=\"tag_busDetails\">业务细分</span>：</td><td class=\"f_r_td\">资格注销</td></tr><tr><td class=\"f_td\"><span id=\"tag_applyPic\">申请人证件</span>：</td><td class=\"f_r_td\">{applypic}</td><td class=\"f_td\"></td><td class=\"f_r_td\"></td></tr><tr><td colspan=\"4\" align=\"center\" class=\"f_td\">申请原因</td></tr><tr><td colspan=\"4\" style=\"height: 100px\" class=\"f_r_td\">{applyreason}</td></tr></table>";
					break;
				case "5":   //直系亲属资格替换
					strHtml = "<table class=\"centerdiv\" style=\"line-height: 34px; border: 1px solid;\" cellpadding=\"0\" cellspacing=\"0\"><tr><td width=\"100px\" class=\"f_td\"><span id=\"tag_olCode\">受理编号</span>：</td><td class=\"f_r_td\">{olcode}</td><td style=\"width: 100px\" class=\"f_td\"><span id=\"tag_applyCode\">申请人编号</span>：</td><td class=\"f_r_td\">{applycode}</td></tr><tr><td class=\"f_td\"><span id=\"tag_depart\">所属部门</span>：</td><td class=\"f_r_td\">{depart}</td><td class=\"f_td\"><span id=\"tag_applyName\">申请人姓名</span>：</td><td class=\"f_r_td\">{applyname}</td></tr><tr><td class=\"f_td\"><span id=\"tag_cardID\">申请人证件号</span>：</td><td class=\"f_r_td\">{cardid}</td><td class=\"f_td\"><span id=\"tag_welfare\">申请人福利级别</span>：</td><td class=\"f_r_td\">{welfare}</td></tr><tr><td class=\"f_td\"><span id=\"tag_newName\">新姓名</span>：</td><td class=\"f_r_td\">{newname}</td><td class=\"f_td\"><span id=\"tag_newCardID\">新证件号</span>：</td><td class=\"f_r_td\">{newcardid}</td></tr><tr><td class=\"f_td\"><span id=\"tag_newBank\">新银行名称</span>：</td><td class=\"f_r_td\">{newbank}</td><td class=\"f_td\"><span id=\"tag_newAccount\">新银行账号</span>：</td><td class=\"f_r_td\">{newaccount}</td></tr><tr><td class=\"f_td\"><span id=\"tag_bankAddress\">新银行地址</span>：</td><td class=\"f_r_td\" colspan=\"3\">{bankaddress}</td></tr><tr><td class=\"f_td\"><span id=\"tag_bustype\">申请业务类型</span>：</td><td class=\"f_r_td\">即时办理业务</td><td class=\"f_td\"><span id=\"tag_busDetails\">业务细分</span>：</td><td class=\"f_r_td\">直系亲属资格替换</td></tr><tr><td class=\"f_td\"><span id=\"tag_applyPic\">申请人证件</span>：</td><td class=\"f_r_td\">{applypic}</td><td class=\"f_td\"><span id=\"tag_newPic\">新人证件</span>：</td><td class=\"f_r_td\">{newpic}</td></tr><tr><td class=\"f_td\"><span id=\"tag_part1Pic\">营销一分部证件</span>：</td><td class=\"f_r_td\">{part1pic}</td><td class=\"f_td\"><span id=\"tag_part2Pic\">营销二分部证件</span>：</td><td class=\"f_r_td\">{part2pic}</td></tr><tr><td class=\"f_td\"><span id=\"tag_part3Pic\">营销三分部证件</span>：</td><td class=\"f_r_td\">{part3pic}</td><td class=\"f_td\"><span id=\"tag_relativesPic\">亲属证明</span>：</td><td class=\"f_r_td\">{relativespic}</td></tr><tr><td colspan=\"4\" align=\"center\" class=\"f_td\">申请原因</td></tr><tr><td colspan=\"4\" style=\"height: 100px\" class=\"f_r_td\">{applyreason}</td></tr></table>";
					break;
				case "6":   //更正手机号
					strHtml = "<table class=\"centerdiv\" style=\"line-height: 34px; border: 1px solid;\" cellpadding=\"0\" cellspacing=\"0\"><tr><td width=\"100px\" class=\"f_td\"><span id=\"tag_olCode\">受理编号</span>：</td><td class=\"f_r_td\">{olcode}</td><td style=\"width: 100px\" class=\"f_td\"><span id=\"tag_applyCode\">申请人编号</span>：</td><td class=\"f_r_td\">{applycode}</td></tr><tr><td class=\"f_td\"><span id=\"tag_depart\">所属部门</span>：</td><td class=\"f_r_td\">{depart}</td><td class=\"f_td\"><span id=\"tag_applyName\">申请人姓名</span>：</td><td class=\"f_r_td\">{applyname}</td></tr><tr><td class=\"f_td\"><span id=\"tag_cardID\">申请人证件号</span>：</td><td class=\"f_r_td\">{cardid}</td><td class=\"f_td\"><span id=\"tag_welfare\">申请人福利级别</span>：</td><td class=\"f_r_td\">{welfare}</td></tr><tr><td class=\"f_td\"><span id=\"tag_mobile\">原手机号</span>：</td><td class=\"f_r_td\">{mobile}</td><td class=\"f_td\"><span id=\"tag_newMobile\">新手机号</span>：</td><td class=\"f_r_td\">{newmobile}</td></tr><tr><td class=\"f_td\"><span id=\"tag_bustype\">申请业务类型</span>：</td><td class=\"f_r_td\">即时办理业务</td><td class=\"f_td\"><span id=\"tag_busDetails\">业务细分</span>：</td><td class=\"f_r_td\">更正手机号</td></tr><tr><td class=\"f_td\"><span id=\"tag_applyPic\">申请人证件</span>：</td><td class=\"f_r_td\">{applypic}</td><td class=\"f_td\"></td><td class=\"f_r_td\"></td></tr><tr><td colspan=\"4\" align=\"center\" class=\"f_td\">申请原因</td></tr><tr><td colspan=\"4\" style=\"height: 100px\" class=\"f_r_td\">{applyreason}</td></tr></table>";
					break;
				case "7":   //开通订货及转账权限
					strHtml = "<table class=\"centerdiv\" style=\"line-height: 34px; border: 1px solid;\" cellpadding=\"0\" cellspacing=\"0\"><tr><td width=\"100px\" class=\"f_td\"><span id=\"tag_olCode\">受理编号</span>：</td><td class=\"f_r_td\">{olcode}</td><td style=\"width: 100px\" class=\"f_td\"><span id=\"tag_applyCode\">申请人编号</span>：</td><td class=\"f_r_td\">{applycode}</td></tr><tr><td class=\"f_td\"><span id=\"tag_depart\">所属部门</span>：</td><td class=\"f_r_td\">{depart}</td><td class=\"f_td\"><span id=\"tag_applyName\">申请人姓名</span>：</td><td class=\"f_r_td\">{applyname}</td></tr><tr><td class=\"f_td\"><span id=\"tag_cardID\">申请人证件号</span>：</td><td class=\"f_r_td\">{cardid}</td><td class=\"f_td\"><span id=\"tag_welfare\">申请人福利级别</span>：</td><td class=\"f_r_td\">{welfare}</td></tr><tr><td class=\"f_td\"><span id=\"tag_bustype\">申请业务类型</span>：</td><td class=\"f_r_td\">即时办理业务</td><td class=\"f_td\"><span id=\"tag_busDetails\">业务细分</span>：</td><td class=\"f_r_td\">开通订货及转账权限</td></tr><tr><td class=\"f_td\"><span id=\"tag_applyPic\">申请人证件</span>：</td><td class=\"f_r_td\">{applypic}</td><td class=\"f_td\"></td><td class=\"f_r_td\"></td></tr><tr><td colspan=\"4\" align=\"center\" class=\"f_td\">申请原因</td></tr><tr><td colspan=\"4\" style=\"height: 100px\" class=\"f_r_td\">{applyreason}</td></tr></table>";
					break;
				case "8":   //12万旗舰店申请
					strHtml = "<table class=\"centerdiv\" style=\"line-height: 34px; border: 1px solid;\" cellpadding=\"0\" cellspacing=\"0\"><tr><td width=\"100px\" class=\"f_td\"><span id=\"tag_olCode\">受理编号</span>：</td><td class=\"f_r_td\">{olcode}</td><td style=\"width: 100px\" class=\"f_td\"><span id=\"tag_applyCode\">申请人编号</span>：</td><td class=\"f_r_td\">{applycode}</td></tr><tr><td class=\"f_td\"><span id=\"tag_depart\">所属部门</span>：</td><td class=\"f_r_td\">{depart}</td><td class=\"f_td\"><span id=\"tag_applyName\">申请人姓名</span>：</td><td class=\"f_r_td\">{applyname}</td></tr><tr><td class=\"f_td\"><span id=\"tag_cardID\">申请人证件号</span>：</td><td class=\"f_r_td\">{cardid}</td><td class=\"f_td\"><span id=\"tag_welfare\">申请人福利级别</span>：</td><td class=\"f_r_td\">{welfare}</td></tr><tr><td class=\"f_td\"><span id=\"tag_address\">旗舰店地址</span>：</td><td class=\"f_r_td\" colspan=\"3\">{address}</td></tr><tr><td class=\"f_td\"><span id=\"tag_receiveName\">收件人姓名</span>：</td><td class=\"f_r_td\">{receivename}</td><td class=\"f_td\"><span id=\"tag_receiveMobile\">收件人电话</span>：</td><td class=\"f_r_td\">{reveicemobile}</td></tr><tr><td class=\"f_td\"><span id=\"tag_authAddress\">授权书邮寄地址</span>：</td><td class=\"f_r_td\" colspan=\"3\">{authaddress}</td></tr><tr><td class=\"f_td\"><span id=\"tag_bustype\">申请业务类型</span>：</td><td class=\"f_r_td\">大区协同办理业务</td><td class=\"f_td\"><span id=\"tag_busDetails\">业务细分</span>：</td><td class=\"f_r_td\">12万旗舰店申请</td></tr><tr><td class=\"f_td\"><span id=\"tag_applyPic\">申请人证件</span>：</td><td class=\"f_r_td\">{applypic}</td><td class=\"f_td\"><span id=\"tag_busLicense\">营业执照</span>：</td><td class=\"f_r_td\">{buslicense}</td></tr><tr><td colspan=\"4\" align=\"center\" class=\"f_td\">申请原因</td></tr><tr><td colspan=\"4\" style=\"height: 100px\" class=\"f_r_td\">{applyreason}</td></tr></table>";
					break;
				case "9":   //非12万旗舰店申请
					strHtml = "<table class=\"centerdiv\" style=\"line-height: 34px; border: 1px solid;\" cellpadding=\"0\" cellspacing=\"0\"><tr><td width=\"100px\" class=\"f_td\"><span id=\"tag_olCode\">受理编号</span>：</td><td class=\"f_r_td\">{olcode}</td><td style=\"width: 100px\" class=\"f_td\"><span id=\"tag_applyCode\">申请人编号</span>：</td><td class=\"f_r_td\">{applycode}</td></tr><tr><td class=\"f_td\"><span id=\"tag_depart\">所属部门</span>：</td><td class=\"f_r_td\">{depart}</td><td class=\"f_td\"><span id=\"tag_applyName\">申请人姓名</span>：</td><td class=\"f_r_td\">{applyname}</td></tr><tr><td class=\"f_td\"><span id=\"tag_cardID\">申请人证件号</span>：</td><td class=\"f_r_td\">{cardid}</td><td class=\"f_td\"><span id=\"tag_welfare\">申请人福利级别</span>：</td><td class=\"f_r_td\">{welfare}</td></tr><tr><td class=\"f_td\"><span id=\"tag_address\">旗舰店地址</span>：</td><td class=\"f_r_td\" colspan=\"3\">{address}</td></tr>&nbsp;<tr><td class=\"f_td\"><span id=\"tag_bustype\">申请业务类型</span>：</td><td class=\"f_r_td\">大区协同办理业务</td><td class=\"f_td\"><span id=\"tag_busDetails\">业务细分</span>：</td><td class=\"f_r_td\">非12万旗舰店申请</td></tr><tr><td colspan=\"4\" align=\"center\" class=\"f_td\">申请原因</td></tr><tr><td colspan=\"4\" style=\"height: 100px\" class=\"f_r_td\">{applyreason}</td></tr></table>";
					break;
				default:
					strHtml = "";
					break;
			}
			//strHtml=GetReplaceHtml(strHtml,modelMap);
			//String bussinessKey="";
			SaveHtml(strHtml,modelMap,pkId,businessKey);
			
			String sql = "from CodeEntity where code=? and shortName =? and receiptNumber=?";
	        CodeEntity codeEntity = codeManager.findUnique(sql,Integer.valueOf(model.getApplytype()),_ShorName,model.getApplycode());
	        if(codeEntity!=null)
	        {
	        	codeEntity.setUserID(codeEntity.getUserID()+"_use");
	        	codeManager.save(codeEntity);
	        }
	        	//codeManager.remove(codeEntity);
			//onLineInfoManager.save(model);
		}
		
		*//**
		 * 将图片路径保存，并且拼接html
		 * **//*
		private String UploadPicAndGetPicHtml(Object strPics,String tenanId,String pkId)
		{
			StringBuffer sbPicsBuffer=new StringBuffer("");
	        if(strPics!=null&&strPics.toString().split(",").length>0){
	        	for(String pic:strPics.toString().split(",")){
	    			String filename=pic.substring(pic.lastIndexOf("/")+1, pic.length());
	        		
	    			try {
						storeConnector.saveStore("rolmexoa/pic", filename, tenanId, pkId, pic);
					} catch (Exception e) {
						e.printStackTrace();
					}
	    			String picUrl=webAPI.getViewUrl()+"/"+pic;
	        		sbPicsBuffer.append("<a href=\""+picUrl+"\" target=\"_blank\">"
	        				+ "<img src=\""+picUrl+"\""
	        				+ " style=\"margin-top: 7px; margin-bottom: 7px;\" width=\"84\" height=\"74\""
	        				+ " border=\"0\" alt=\"\" class=\"photo\"/></a>");
	        	}
	        }
	        return sbPicsBuffer.toString();
		}
		
		*//**
		 * 替换字符串得到html,并且保存更新KV
		 * **//*
		private void SaveHtml(String detailHtml,Map<String,Object> modelMap,String pkId,String bussinessKey)
		{
			for(String key:modelMap.keySet()){
				detailHtml=detailHtml.replace("{"+key+"}", (modelMap.get(key)==null?"":modelMap.get(key).toString()));
			}
			
			String sqlRecordUpdate = "update KV_RECORD set detailHtml= '" + detailHtml + "',pk_id ='" + pkId + "' where BUSINESS_KEY= '" + bussinessKey + "'";
	    	keyValueConnector.updateBySql(sqlRecordUpdate);
	    	
			return;
		}*/
		//endregion
		
		//endregion
	
	/**
     * 描述 判断在线办公申请是否存在处理中的申请数据 参数：varApplyCode=申请人编号；chrApplyType=申请类型
     * **/
	@Override
    public boolean CheckApplyStatus(String varApplyCode, String chrApplyType, String strMsg){
		try {
			String paramKey=signInfo.getParamKey();
			//参数解密
			varApplyCode = Common.Decrypt3DES(varApplyCode,paramKey);
	        chrApplyType = Common.Decrypt3DES(chrApplyType,paramKey);
	        //记录参数日志
	        logger.info("CheckApplyStatus(判断在线办公申请是否存在处理中的申请数据) 参数：varApplyCode=" + varApplyCode + ",chrApplyType=" + chrApplyType,paramKey);

	        //验证签名
	        String strKey = varApplyCode.toUpperCase() + chrApplyType.toUpperCase();
	        String signKey=signInfo.getSignKey();
	        if (!Common.VerifySign(strKey,strMsg,signKey))
	        {
	        	logger.info("CheckApplyStatus(判断在线办公申请是否存在处理中的申请数据):验证签名失败");
	            return false;
	        }
	        
	        String strSql="";
	        if(chrApplyType.equals("8")||chrApplyType.equals("9")){
	        	strSql="SELECT count(*) countNum FROM (SELECT k.audit_status flowStatus FROM kv_record k"
	        			+" inner JOIN ro_pf_oaonline o on o.id=k.pk_id"
    					+" where (o.chrApplyType='8' or o.chrApplyType='9') and o.ucode='"+varApplyCode+"') ko where ko.flowStatus in('0','1','4','7')";
	        }
	        else if(chrApplyType.equals("10")||chrApplyType.equals("11")){
	        	strSql="SELECT count(*) countNum FROM (SELECT k.audit_status flowStatus FROM kv_record k"
	        			+" inner JOIN ro_pf_oaonline o on o.id=k.pk_id"
    					+" where (o.chrApplyType='10' or o.chrApplyType='11') and o.ucode='"+varApplyCode+"') ko where ko.flowStatus in('0','1','4','7')";
	        }
	        else if(chrApplyType.equals("12")||chrApplyType.equals("13")){
	        	strSql="SELECT count(*) countNum FROM (SELECT k.audit_status flowStatus FROM kv_record k"
	        			+" inner JOIN ro_pf_oaonline o on o.id=k.pk_id"
    					+" where (o.chrApplyType='12' or o.chrApplyType='13') and o.ucode='"+varApplyCode+"') ko where ko.flowStatus in('0','1','4','7')";
	        }
	        else {
	        	strSql="SELECT count(*) countNum FROM (SELECT k.audit_status flowStatus FROM kv_record k"
	        			+" inner JOIN ro_pf_oaonline o on o.id=k.pk_id"
    					+" where (o.chrApplyType='"+chrApplyType+"') and o.ucode='"+varApplyCode+"') ko where ko.flowStatus in('0','1','4','7')";
			}
	        
	        List<Map<String,Object>> mapList=jdbcTemplate.queryForList(strSql);
	        if(mapList!=null&&mapList.size()>0&&Integer.parseInt(mapList.get(0).get("countNum").toString())>0)
	        	return false;
	        
		} catch (Exception ex) {
			logger.debug("CheckApplyStatus(判断在线办公申请是否存在处理中的申请数据)，出现异常："+ex.getMessage()+"\r\n"+ex.getStackTrace());
			return false;
		}
		return true;
    }
	    
    /**
     * 读取申请编号
     * @param varUserID 申请人(代理人)编号
     * @param chrApplyType 申请类型 1密码初始化 2更正姓名 3身份证更正 4资格注销 5直系亲属资格替换 6更正手机号 7开通订货及转账权限 8 12万旗舰店 9非12万旗舰店
     * @param strMsg 签名
     * @return 空值：数据异常  非空：编号
     * **/
	@Override
	@Transactional(readOnly = false)
    public String ReadApplyCode(String varUserID, String chrApplyType, String strMsg){
		String code="";
    	try {
    		String paramKey=signInfo.getParamKey();
			//参数解密
            varUserID = Common.Decrypt3DES(varUserID,paramKey);
            chrApplyType = Common.Decrypt3DES(chrApplyType,paramKey);

            logger.info("ReadApplyCode(读取申请编号) 参数：varUserID=" + varUserID + ",chrApplyType=" + chrApplyType,paramKey);

            //验证签名
	        String strKey = varUserID.toUpperCase() + chrApplyType.toUpperCase();
	        String signKey=signInfo.getSignKey();
	        if (!Common.VerifySign(strKey,strMsg,signKey))
	        {
	        	logger.info("ReadApplyCode(读取申请编号):验证签名失败");
	            return "";
	        }
	        //获取编号
	        code=CreateApplyCode(varUserID,chrApplyType);
	        
		} catch (Exception ex) {
			logger.debug("ReadApplyCode(读取申请编号)，出现异常："+ex.getMessage()+"\r\n"+ex.getStackTrace());
			return "";
		}
		return code;
    }
	
	/**
	 * 生成在线办公受理单单号
	 * lilei at 2017-10-26
	 * **/
	private String CreateApplyCode(String userId,String applyType) throws Exception {

    	String code = "";
    	
		SimpleDateFormat formater = new SimpleDateFormat("yyMMdd");
        String codeDate =  formater.format(new Date());
        
        //为保证受理编号从每天日期开始，清除除今日以前的受理单号 add by lilei at 2018-04-24
        String hSqlDelString="from CodeEntity where userID=? and shortName=? and createTime<?";
        List<CodeEntity> oldCodeList= codeManager.find(hSqlDelString,userId,_ShorName,codeDate);
        if(oldCodeList!=null&&oldCodeList.size()>0)
        	codeManager.removeAll(oldCodeList);
        
        //String shortName="OAJXS";
        //查下受理单编码表中是否已经存在当前登录人的编码，如果有，直接用并且等流程发起了再从oa_bpm_code表中删掉，若没有，重新生成一条编码
        String sql = "from CodeEntity where userID=? and code=? and shortName =?";
        
        CodeEntity codeEntity = codeManager.findUnique(sql,userId,Integer.valueOf(applyType),_ShorName);
        
        //已经存在  直接用  等流程发起了再从表中删掉
        if(codeEntity!=null){
        	code = codeEntity.getReceiptNumber();
        }
        //没有，重新生成一条编码
        else{
            	 //查找当天是否存在
            	 sql = "from CodeEntity where  shortName =? and createTime=? order by receiptNumber desc ";
                 List<CodeEntity> c= codeManager.find(sql,_ShorName,codeDate);
                 
                 String strLastNum="";
                 //当天这个已存在 ,取最大的编码再加一就行了
                 //1710260001
                 if(c!=null&&c.size()>0){
                	strLastNum=c.get(0).getReceiptNumber().substring(6, 10);
                	
                	Integer baseNum=Integer.valueOf(strLastNum)+1;
                	strLastNum="";
                	if(baseNum.toString().length()==3)
                		strLastNum="0";
                	else if(baseNum.toString().length()==2)
                		strLastNum="00";
                	else if(baseNum.toString().length()==1)
                		strLastNum="000";
                	
                	strLastNum+=baseNum;
                 }else
                	 strLastNum="0001";
                 
                 code=codeDate+strLastNum;
                 
                 //存入表中
            	 CodeEntity codeEntityTemp = new CodeEntity();
            	 codeEntityTemp.setCode(Integer.valueOf(applyType));
            	 codeEntityTemp.setCreateTime(codeDate);
            	 codeEntityTemp.setShortName(_ShorName);
            	 codeEntityTemp.setUserID(userId);
            	 codeEntityTemp.setReceiptNumber(code);
            	 codeManager.save(codeEntityTemp);
         }
        return code;
     }
    
    /**
     * 直销根据申请人(代理人)编号和类型查询在线办公申请记录
     * @param varApplyCode 申请人(代理人)编号
     * @param chrApplyType 申请类型 1密码初始化 2更正姓名 3身份证更正 4资格注销 5直系亲属资格替换 6更正手机号 7开通订货及转账权限 8 12万旗舰店 9非12万旗舰店
     * @param strPageSize 页大小
     * @param strPageIndex 页码
     * @param strMsg 签名
     * @return 返回JSON
     * @throws IOException 
     * **/
	@Override
    public String PagesSearchApply(
    		String varApplyCode, 
    		String chrApplyType, 
    		String strPageSize, 
    		String strPageIndex,
    		String strMsg) throws IOException{
		Map<String,Object> returnMap=new HashMap<String, Object>();
		try {
			String paramKey=signInfo.getParamKey();
			//参数解密
			varApplyCode = Common.Decrypt3DES(varApplyCode,paramKey);
	        chrApplyType = Common.Decrypt3DES(chrApplyType,paramKey);
	        strPageSize = Common.Decrypt3DES(strPageSize,paramKey);
	        strPageIndex = Common.Decrypt3DES(strPageIndex,paramKey);
	        //记录参数日志
	        logger.info("PagesSearchApply(直销根据申请人(代理人)编号和类型查询在线办公申请记录) 参数："
	        		+ "varApplyCode=" + varApplyCode + ","
				    + "chrApplyType=" + chrApplyType + ","
				    + "strPageSize=" + strPageSize + ","
				    + "strPageIndex=" + strPageIndex,paramKey);

	        //验证签名
	        String strKey = varApplyCode.toUpperCase() 
	        		+ chrApplyType.toUpperCase() 
	        		+ strPageSize.toUpperCase() 
	        		+ strPageIndex.toUpperCase();
	        String signKey=signInfo.getSignKey();
	        if (!Common.VerifySign(strKey,strMsg,signKey))
	        {
	        	logger.info("PagesSearchApply(直销根据申请人(代理人)编号和类型查询在线办公申请记录):验证签名失败");
	        	returnMap.put("result","error");
	        	returnMap.put("msg","签名验证失败");
	        	return jsonMapper.toJson(returnMap);
	        }
	        
	        //暂时注释，等到SQL2注释后放开
	        /*String strSql="SELECT op.*,f_OnlineStatusDesc(op.chrStatus)  as ApplyStatus,f_OnlineAuditRemark(op.chrStatus,op.BUSINESS_KEY) as varAuditRemark FROM"
+" (SELECT o.varApplyCode as varOLCode,o.ucode AS varApplyCode,o.varBranch ,"
+ "o.varApplyName,o.varApplyIdentity ,o.varMobile ,o.varNewName ,o.varNewIdentity,"
+"o.varBankCode ,o.chrApplyType ,date_format(o.dtmApplyTime, '%Y-%m-%d %H:%i:%s') as dtmApplyTime,date_format(o.dtmAuditTime, '%Y-%m-%d %H:%i:%s') as dtmAuditTime,"
+"o.varAuditRemark as auditRemark,r.BUSINESS_KEY,f_OnlineType(o.chrApplyType) as applyType,"
+"o.ucode as  varPerCode,r.audit_status as chrStatus,f_OnlineUploadFlag(o.varApplyCode,o.chrApplyType) as UploadFlag"
+" FROM ro_pf_oaonline o INNER JOIN kv_record r on r.pk_id=o.id ";*/
	        
	        //SQL2:暂时将4，7状态改为1，状态描述不变 add by lilei at 2018-05-09 10:47
	        String strSql="SELECT op.*,f_OnlineStatusDesc(op.chrStatus_2)  as ApplyStatus,f_OnlineAuditRemark(op.chrStatus,op.BUSINESS_KEY) as varAuditRemark FROM"
	        		+" (SELECT o.varApplyCode as varOLCode,o.ucode AS varApplyCode,o.varBranch ,"
	        		+ "o.varApplyName,o.varApplyIdentity ,o.varMobile ,o.varNewName ,o.varNewIdentity,"
	        		+"o.varBankCode ,o.chrApplyType ,date_format(o.dtmApplyTime, '%Y-%m-%d %H:%i:%s') as dtmApplyTime,date_format(o.dtmAuditTime, '%Y-%m-%d %H:%i:%s') as dtmAuditTime,"
	        		+"o.varAuditRemark as auditRemark,r.BUSINESS_KEY,f_OnlineType(o.chrApplyType) as applyType,"
	        		+"o.ucode as  varPerCode,CASE WHEN r.audit_status IN('4','7') THEN '1' ELSE r.audit_status END as chrStatus,r.audit_status as chrStatus_2,f_OnlineUploadFlag(o.varApplyCode,o.chrApplyType) as UploadFlag"
	        		+" FROM ro_pf_oaonline o INNER JOIN kv_record r on r.pk_id=o.id ";
	        
			String strWhere=" WHERE 1=1 ";
			if (!chrApplyType.equals(""))
            {
                if (chrApplyType .equals("1"))
                	strWhere+=" and o.varBankCode='"+varApplyCode+"'";
                else
                	strWhere+=" and o.ucode='"+varApplyCode+"'";
                
                if (chrApplyType.equals("8") || chrApplyType.equals("9"))
                	strWhere+=" And (o.chrApplyType='8' OR o.chrApplyType='9' )";
                else if (chrApplyType.equals("10") || chrApplyType.equals("11"))
                	strWhere+=" And (o.chrApplyType='10' OR o.chrApplyType='11' )";
                else if (chrApplyType.equals("12") || chrApplyType.equals("13"))
                	strWhere+=" And (o.chrApplyType='12' OR o.chrApplyType='13' )";
                else
                	strWhere+=" and o.chrApplyType='"+chrApplyType+"'";
            }
			
			strWhere+=" order by dtmApplyTime DESC ";
			
			
			String strCountSql="SELECT COUNT(*) countNum FROM ro_pf_oaonline o INNER JOIN kv_record r on r.pk_id=o.id "
					+ strWhere;
			strSql+=strWhere+ " LIMIT "+(Long.valueOf(strPageIndex)-1)*Long.valueOf(strPageSize)+","+strPageSize+") op";
	        
			List<Map<String,Object>> mapDataCount=jdbcTemplate.queryForList(strCountSql);
			returnMap.put("recordCount",(mapDataCount!=null&&mapDataCount.size()>0)?mapDataCount.get(0).get("countNum"):0); 
			
	        List<Map<String,Object>> mapDataList=jdbcTemplate.queryForList(strSql);
	        returnMap.put("result","ok");
        	returnMap.put("msg","获取成功");
        	returnMap.put("data",mapDataList);
	        
		} catch (Exception ex) {
			returnMap.put("result","error");
        	returnMap.put("msg","获取异常");
			logger.debug("PagesSearchApply(直销根据申请人(代理人)编号和类型查询在线办公申请记录)，出现异常："+ex.getMessage()+"\r\n"+ex.getStackTrace());
		}
		return jsonMapper.toJson(returnMap);
    }
    
    /**
     * 获取申请记录详情
     * @param strOLID 主键ID
     * @param strMsg 签名
     * @return 返回JSON
     * @throws IOException 
     * **/
	@Override
    public String SearchApplyDetail(String strOLID, String strMsg) throws IOException{
		Map<String,Object> returnMap=new HashMap<String, Object>();
		try {
			String paramKey=signInfo.getParamKey();
			//参数解密
			strOLID = Common.Decrypt3DES(strOLID,paramKey);
	        //记录参数日志
	        logger.info("SearchApplyDetail(获取申请记录详情) 参数："
	        		+ "strOLID=" + strOLID,paramKey);

	        //验证签名
	        String strKey = strOLID.toUpperCase();
	        String signKey=signInfo.getSignKey();
	        if (!Common.VerifySign(strKey,strMsg,signKey))
	        {
	        	logger.info("SearchApplyDetail(获取申请记录详情):验证签名失败");
	        	returnMap.put("result","error");
	        	returnMap.put("msg","签名验证失败");
	        	return jsonMapper.toJson(returnMap);
	        }
	        
	        String strSql="SELECT * FROM KV_RECORD where applyCode='"+strOLID+"'";
	        List<Map<String,Object>> mapKVList=jdbcTemplate.queryForList(strSql);
	        String status=mapKVList.get(0).get("audit_status").toString();
	        /*if(status.equals("2")||status.equals("3"))
	        {
	        	strSql="SELECT * FROM task_info where BUSINESS_KEY='"+mapKVList.get(0).get("BUSINESS_KEY").toString()+"'"
	        			+ " ORDER BY COMPLETE_TIME DESC LIMIT 0,1";
	        	List<Map<String,Object>> mapTaskList=jdbcTemplate.queryForList(strSql);
	        	SimpleDateFormat dateFormat =   new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
	        	Date endDate=dateFormat.parse(mapTaskList.get(0).get("COMPLETE_TIME").toString());
	        	String strComment=mapTaskList.get(0).get("comment").toString();
	        	
	        	OnLineInfo onLineInfo=onLineInfoManager.findUniqueBy("applycode", strOLID);
	        	onLineInfo.setAuditremark(strComment);
	        	onLineInfo.setAudittime(endDate);
	        	onLineInfoManager.save(onLineInfo);
	        }*/
	        //f_OnlineAuditRemark(op.chrStatus,op.BUSINESS_KEY) as 
	        strSql="SELECT op.*,f_OnlineStatusDesc(op.chrStatus) as ApplyStatus,op.varAuditRemark FROM "
	        		+"(SELECT o.varApplyCode as varOLCode,o.ucode as varApplyCode,o.varBranch,o.varApplyName,o.varApplyIdentity,o.varMobile,o.varNewName,"
	        		+"o.varNewIdentity,o.varBankCode,o.varBankName,o.chrApplyType,o.varBankAddress,o.varReason,"
	        		+"date_format(o.dtmApplyTime, '%Y-%m-%d %H:%i:%s') as dtmApplyTime,"
	        		+"date_format(o.dtmAuditTime, '%Y-%m-%d %H:%i:%s') as dtmAuditTime,"
	        		+"f_OnlineType(o.chrApplyType) as ApplyType,o.ucode varPerCode,r.audit_status as chrStatus,"
					+"o.varApplyPic,o.varNewPic,o.varPartPic1,o.varPartPic2,o.varPartPic3,o.varAuditRemark,IFNULL(o.varCompleteRemark,'') as varCompleteRemark,"
					+"o.varRelativesPic,r.BUSINESS_KEY,f_OnlineUploadFlag(o.varApplyCode,o.chrApplyType) as UploadFlag,"
					+ "o.varShopLicense,o.varEnterpriseName,o.varLegaler,o.varLegalerIdCard,o.varDistributorPhone,o.varScopeBusiness,o.varNote,o.varPublicAccount,o.varAccountType,o.varAccountNumber,o.varOpeningName,o.varOpeningBank,o.varStoreArea "
					+" FROM ro_pf_oaonline o INNER JOIN kv_record r on r.pk_id=o.id "
					+" WHERE o.varApplyCode='"+strOLID+"') op";
	        
	        List<Map<String,Object>> mapDataList=jdbcTemplate.queryForList(strSql);
	        returnMap.put("result","ok");
        	returnMap.put("msg","获取成功");
        	returnMap.put("data",mapDataList);
	        
		} catch (Exception ex) {
			returnMap.put("result","error");
        	returnMap.put("msg","获取异常");
			logger.debug("SearchApplyDetail(获取申请记录详情)，出现异常："+ex.getMessage()+"\r\n"+ex.getStackTrace());
		}
		return jsonMapper.toJson(returnMap);
    }
    
    /**
     * 直销验证是否存在已审核密码初始化申请
     * @param strApplyCode 申请人编号
     * @param strStartTime 起始时间
     * @param strMsg 签名
     * @return 返回数值
     * **/
	public int SellValidPwdInit(String strApplyCode,String strBranch, String strStartTime, String strMsg){
		try {
			String paramKey=signInfo.getParamKey();
			//参数解密
			strApplyCode = Common.Decrypt3DES(strApplyCode,paramKey);
			strStartTime = Common.Decrypt3DES(strStartTime,paramKey);
			strBranch= Common.Decrypt3DES(strBranch,paramKey);
	        //记录参数日志
	        logger.info("SellValidPwdInit(直销验证是否存在已审核密码初始化申请) 参数："
	        		+ "varApplyCode=" + strApplyCode
	        		+"strStartTime="+strStartTime,paramKey);

	        //验证签名
	        String strKey = strApplyCode.toUpperCase()+strBranch.toUpperCase()+strStartTime.toUpperCase();
	        String signKey=signInfo.getSignKey();
	        if (!Common.VerifySign(strKey,strMsg,signKey))
	        {
	        	logger.info("SellValidPwdInit(直销验证是否存在已审核密码初始化申请):验证签名失败");
	        	return 0;
	        }
	        
	        switch(strBranch.toUpperCase())
	        {
	            case "F01":
	                strBranch="一部";
	                break;
	            case "F02":
	                strBranch="二部";
	                break;
	            case "F04":
	                strBranch="四部";
	                break;
	        }
	        
	        String strSql="SELECT COUNT(op.chrStatus) AS countNum FROM ("
						+"SELECT r.audit_status as chrStatus" 
						+" FROM ro_pf_oaonline o INNER JOIN kv_record r on r.pk_id=o.id"
						+" WHERE o.chrApplyType='1' AND varBranch='"+strBranch+"' AND o.ucode='"+strApplyCode+"' AND  o.dtmApplyTime>='"+strStartTime+"') op where op.chrStatus='2'";
	        
	        Map<String,Object> map=jdbcTemplate.queryForMap(strSql);
	        if(map!=null&&map.containsKey("countNum")){
	        	if(Integer.valueOf(map.get("countNum").toString())>0)
		        	return 1;
	        }
	        
		} catch (Exception ex) {
			logger.debug("SellValidPwdInit(直销验证是否存在已审核密码初始化申请)，出现异常："+ex.getMessage()+"\r\n"+ex.getStackTrace());
		}
		return 0;
    }
        
    /**
     * 取消最后一次旗舰店申请
     * @param strApplyCode 申请人编号
     * @param varBranch 所属部门编号
     * @param chrApplyType 申请类型
     * @param strMsg 签名
     * @return 返回布尔类型
     * **/
	@Override
	@Transactional(readOnly = false)
    public boolean StopApply(String varApplyCode, String varBranch, String chrApplyType, String strMsg){
		boolean isOpter=false;
		try {
			String paramKey=signInfo.getParamKey();
			//参数解密
			varApplyCode = Common.Decrypt3DES(varApplyCode,paramKey);
			varBranch = Common.Decrypt3DES(varBranch,paramKey);
			chrApplyType = Common.Decrypt3DES(chrApplyType,paramKey);
	        //记录参数日志
	        logger.info("SellValidPwdInit(取消最后一次旗舰店申请) 参数："
	        		+ "varApplyCode=" + varApplyCode
	        		+ "varBranch=" + varBranch
	        		+ "chrApplyType=" + chrApplyType,paramKey);

	        //验证签名
	        String strKey = varApplyCode.toUpperCase()+varBranch.toUpperCase()+chrApplyType.toUpperCase();
	        String signKey=signInfo.getSignKey();
	        if (!Common.VerifySign(strKey,strMsg,signKey))
	        {
	        	logger.info("SellValidPwdInit(取消最后一次旗舰店申请):验证签名失败");
	        	return false;
	        }
	        
	        String strBranch="";
	        switch(varBranch.toUpperCase())
	        {
	            case "F01":
	                strBranch="一部";
	                break;
	            case "F02":
	                strBranch="二部";
	                break;
	            case "F04":
	                strBranch="四部";
	                break;
	        }
	        	        
	       
	        
	        String strSql="SELECT * FROM (SELECT r.pk_id as pkid,o.varapplycode,r.audit_status as auditStatus,r.BUSINESS_KEY,r.ID,o.ucode,o.chrApplyType,o.varBranch,r.REF as processId,o.dtmApplyTime "
	        				+ " from kv_record r "
							+" INNER JOIN ro_pf_oaonline o ON o.id=r.pk_id"
							+ ") t"
							+" where t.ucode='"+varApplyCode+"' and t.chrApplyType='"+chrApplyType+"' and t.varBranch='"+strBranch+"'"
							//+ " and t.auditStatus in('0','1','4','6','7')"
						    + " ORDER BY t.dtmApplyTime desc limit 0,1";
	        	        
	        List<Map<String,Object>> mapList=jdbcTemplate.queryForList(strSql);
	        if(mapList==null||mapList.size()<1)
	        	return false;
	        
	        Map<String,Object> map=mapList.get(0);
	        if(map==null)
	        	return false;
	        
	        //todo：这里处理流程结束
	        //取消备注：由于OA办公系统撤单或冻结，取消人：系统
	        if(map.get("auditStatus").equals("3")||map.get("auditStatus").equals("6"))
	        	return true;
	        
	        if(map.get("auditStatus").equals("2")){
	        	String strTaskSql="SELECT i.* FROM TASK_INFO i inner join kv_record r on r.BUSINESS_KEY=i.BUSINESS_KEY"
	        			+ " where i.catalog<>'copy'"
	        			+ " and i.BUSINESS_KEY='"+map.get("BUSINESS_KEY")+"'"
	        			+ " ORDER BY i.complete_time desc limit 0,1";
	        	
	        	List<Map<String,Object>> mapTaskList=jdbcTemplate.queryForList(strTaskSql);
	        	if(mapTaskList==null||mapTaskList.size()<1)
	        		return false;
	        	
	        	Map<String,Object> mapTask=mapTaskList.get(0);
	        	strTaskSql="update task_info set "
	        			+ "action='同意',"
	        			+ "comment='" + mapTask.get("comment") + "<br/><font style=\"color:red\">由于OA办公系统撤单或冻结</font>',"
	        			+ "status='complete',"
	        			+ "COMPLETE_TIME=NOW() "
	        			+ " where id='"+mapTask.get("id")+"'";
	        	jdbcTemplate.update(strTaskSql);
	        	//直销置费业绩单后将这条申请状态置为‘已取消’
	        	String changeStatus = "update kv_record set audit_status=6 where business_key='"+map.get("BUSINESS_KEY")+"'";
	        	jdbcTemplate.update(changeStatus);
	        	OnLineInfo onLineInfo=onLineInfoManager.findUniqueBy("id", Long.parseLong(map.get("pkid").toString()));
	        	
	        	TaskInfo taskInfo = taskInfoManager.get(Long.parseLong(mapTask.get("id").toString()));
				onLineInfo.setAuditremark(taskInfo.getComment());
				onLineInfo.setAudittime(taskInfo.getCompleteTime());
				onLineInfoManager.save(onLineInfo);
	        	
	        	return true;
	        }
	        
	        String processInstanceId=map.get("processId").toString();//流程ID
	        Map<String, Object> processParameters = new HashMap<String, Object>();
	        String userId = PartyConstants.JXS_ID;
	        String businessKey = map.get("business_key").toString();
	        String bpmProcessId = "";
	        String humanTaskId = "";
	       
	        //先获取humanTaskId
	        String hql = "from TaskInfo where processInstanceId=?  and status = 'active' ";
	        List<TaskInfo> taskInfos = taskInfoManager.find(hql, processInstanceId);
	        for(TaskInfo t : taskInfos){
	        	humanTaskId = Long.toString(t.getId()) ;
	        }
	    	
	    	//创建formParameter
	        MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<String, String>();
	        multiValueMap.add("ucode",varApplyCode);
	      
	        FormParameter formParameter = this.operationService.saveDraft(userId, "1", humanTaskId, businessKey, bpmProcessId, multiValueMap);
	        
	        Record record = keyValueConnector.findByCode(businessKey);
	        
	        //完成审批任务
	        processParameters.put("leaderComment", "不同意");
        	formParameter.setAction("不同意");
        	formParameter.setComment("由于OA办公系统撤单或冻结");
        	
        	this.operationService.completeTask(humanTaskId, PartyConstants.JXS_ID,
                    formParameter, processParameters, record,
                    processInstanceId);
	        
        	OnLineInfo onLineInfo=onLineInfoManager.findUniqueBy("id", Long.parseLong(map.get("pkid").toString()));
        	
        	TaskInfo taskInfo = taskInfoManager.get(Long.parseLong(humanTaskId));
			onLineInfo.setAuditremark(taskInfo.getComment());
			onLineInfo.setAudittime(taskInfo.getCompleteTime());
			onLineInfoManager.save(onLineInfo);
        	
        	isOpter=true;
		} catch (Exception ex) {
			isOpter=false;
			logger.debug("SellValidPwdInit(取消最后一次旗舰店申请)，出现异常："+ex.getMessage()+"\r\n"+ex.getStackTrace());
		}
		return isOpter;
    }
    
    /**
     * 非12万旗舰店二次上传资料
     * @param strFRCode 申请编号
     * @param strImages 上传资料
     * @param strUserID 申请人(经销商)
     * @param strReceive 收件人姓名
     * @param strMobile 收件人电话
     * @param strAddress 授权书邮寄地址
     * @param strMsg 签名
     * @return 返回字符串，ok成功 其他失败
     * **/
	@Override
	@Transactional(readOnly = false)
    public String ApplyUploadFile(
    		String strFRCode, 
    		String strImages,
    		String strUserID,
    		String strReceive,
    		String strMobile,
    		String strAddress, 
    		String strMsg){
		/*
		1. 判断申请是否存在
		2. 判断申请状态
		3. 流程数据验证
		4. 审核通过“经销商”审批步骤
		5. 添加下一步审批
		6. 更新资料
	*/
		String  strReutrn="";
		try {
			String paramKey=signInfo.getParamKey();
			//参数解密
			strFRCode = Common.Decrypt3DES(strFRCode,paramKey);
			strImages = Common.Decrypt3DES(strImages,paramKey);
			strUserID = Common.Decrypt3DES(strUserID,paramKey);
			strReceive = Common.Decrypt3DES(strReceive,paramKey);
			strMobile = Common.Decrypt3DES(strMobile,paramKey);
			strAddress = Common.Decrypt3DES(strAddress,paramKey);
			
	        //记录参数日志
	        logger.info("ApplyUploadFile(非12万旗舰店二次上传资料) 参数："
	        		+ "strFRCode=" + strFRCode
	        		+ "strImages=" + strImages
	        		+ "strUserID=" + strUserID
	        		+ "strReceive=" + strReceive
	        		+ "strMobile=" + strMobile
	        		+ "strAddress=" + strAddress);

	        //验证签名
	        String strKey = strFRCode.toUpperCase()+strImages.toUpperCase()+strUserID.toUpperCase()
	        		+strReceive.toUpperCase()+strMobile.toUpperCase()+strAddress.toUpperCase();
	        String signKey=signInfo.getSignKey();
	        if (!Common.VerifySign(strKey,strMsg,signKey))
	        {
	        	logger.info("ApplyUploadFile(非12万旗舰店二次上传资料):验证签名失败");
	        	return "验证签名失败";
	        }
	        
	        String strSql="SELECT * FROM (SELECT o.ID,o.ucode,o.chrApplyType,o.varBranch,"
	        				+"r.REF as processId,o.varApplyCode,i.ASSIGNEE,r.BUSINESS_KEY,r.detailHtml"
	        				+ " from kv_record r "
							+" INNER JOIN ro_pf_oaonline o ON o.id=r.pk_id"
							+" INNER JOIN TASK_INFO i ON r.BUSINESS_KEY=i.BUSINESS_KEY and i.CATALOG='normal' and i.`STATUS`='active') t"
							+" where t.varApplyCode='"+strFRCode+"'";
	        
	        Map<String,Object> map=jdbcTemplate.queryForMap(strSql);
	        if(map==null)
	        	return "数据错误，请联系管理员！";
	        
	        String userId = PartyConstants.JXS_ID;
	        //此时默认发起人为bot，ID为4
	        if(!map.get("ASSIGNEE").equals(userId))
	        	return "该申请已处理或未流转到当前步骤！";
	        
	        //String strAddHrml = "";
	        //放开邮寄地址信息：2018-05-03
	        String strAddHrml =String.format("<tr class=\"formuploadadd\"><td class=\"f_td\"><span id=\"tag_receiveName\">收件人姓名</span>：</td><td class=\"f_r_td\">%s</td><td class=\"f_td\"><span id=\"tag_receiveMobile\">收件人电话</span>：</td><td class=\"f_r_td\">%s</td></tr><tr class=\"formuploadadd\"><td class=\"f_td\"><span id=\"tag_authAddress\">授权书邮寄地址</span>：</td><td class=\"f_r_td\" colspan=\"3\">%s</td></tr>", 
	        		strReceive, strMobile, strAddress);
	        
	        String tenanId = tenantHolder.getTenantId();
	        String strApplyPic="";
	        String strNewPic="";
	        //保存补传的图片信息
	        String strImageTemplate="<tr class=\"formuploadadd\"><td class=\"f_td\">{tdname}</td><td class=\"f_r_td\" colspan=\"3\">{tdimg}</td></tr>";
	        String pkId=map.get("ID").toString();
	        int i=0;
	        storeConnector.removeALLStore("rolmexoa/pic", pkId);//删除图片
	        for(String pic:strImages.toString().split(";")){
	        	i++;
	        	if(i==1){
	        		strApplyPic=pic;
	        		strAddHrml+=strImageTemplate.replace("{tdname}", "证件照：")
	        									.replace("{tdimg}", UploadPicAndGetPicHtml(pic, tenanId, pkId));
	        	}
	        	else if(i==2){
	        		strNewPic=pic;
	        		strAddHrml+=strImageTemplate.replace("{tdname}", "营业执照：")
							.replace("{tdimg}", UploadPicAndGetPicHtml(pic, tenanId, pkId));
	        	}
	        	else if(i==3)
	        		strAddHrml+=strImageTemplate.replace("{tdname}", "租赁合同或房产证：")
							.replace("{tdimg}", UploadPicAndGetPicHtml(pic, tenanId, pkId));
	        	else if(i==4)
	        		strAddHrml+=strImageTemplate.replace("{tdname}", "店铺照：")
							.replace("{tdimg}", UploadPicAndGetPicHtml(pic, tenanId, pkId));
	        }
	        
	        
	        String detailHtml=map.get("detailHtml").toString()
	        		.replaceAll("<tr class=\"formuploadadd\">.*?</tr>", "")
	        		.replace("</table>", strAddHrml+"</table>");
	        //更新资料
	        String strRecordUpSql="update kv_record set detailHtml='"+detailHtml+"' where BUSINESS_KEY= '"+map.get("BUSINESS_KEY")+"'"; 
	        keyValueConnector.updateBySql(strRecordUpSql);
	        
	        //保存上传资料数据
	        OnLineInfo lineModelInfo=onLineInfoManager.findUniqueBy("id", Long.valueOf(map.get("ID").toString()));
	        lineModelInfo.setApplypic(strApplyPic);
	        lineModelInfo.setNewpic(strNewPic);
	        lineModelInfo.setPartpic1(strReceive);
	        lineModelInfo.setPartpic2(strMobile);
	        lineModelInfo.setPartpic3(strAddress);
	        onLineInfoManager.save(lineModelInfo);
	        
	        /**
	         * todo:
	         * 1.当前步骤审核完毕，备注：经销商上传资料完成，自动审核通过
	         * 2.将流程直接改为下个步骤
	         * chengze 20171101
	         * **/
	        String processInstanceId=map.get("processId").toString();//流程ID
	        Map<String, Object> processParameters = new HashMap<String, Object>();
	        
	        String businessKey = map.get("business_key").toString();
	        String bpmProcessId = "";
	        String humanTaskId = "";
	       
	        //先获取humanTaskId
	        String hql = "from TaskInfo where processInstanceId=? and catalog='normal' and status='active' and assignee='"+userId+"'";
	        List<TaskInfo> taskInfos = taskInfoManager.find(hql, processInstanceId);
	        for(TaskInfo t : taskInfos){
	        	humanTaskId = Long.toString(t.getId()) ;
	        }
	    	
	    	//创建formParameter
	        MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<String, String>();
	        multiValueMap.add("ucode", lineModelInfo.getUcode());
	        multiValueMap.add("applyCode", lineModelInfo.getApplycode());
	      
	        FormParameter formParameter = this.operationService.saveDraft(userId, "1", humanTaskId, businessKey, bpmProcessId, multiValueMap);
	        
	        Record record = keyValueConnector.findByCode(businessKey);
	        
	        //完成审批任务
	        processParameters.put("leaderComment", "同意");
        	formParameter.setAction("同意");
        	
        	this.operationService.completeTask(humanTaskId, userId,
                    formParameter, processParameters, record,
                    processInstanceId);
	        //成功返回ok
	        return "ok";
	        
		} catch (Exception ex) {
			logger.debug("ApplyUploadFile(非12万旗舰店二次上传资料)，出现异常："+ex.getMessage()+"\r\n"+ex.getStackTrace());
		}
		return "";
    }
	
	/**
	 * 将图片路径保存，并且拼接html
	 * **/
	private String UploadPicAndGetPicHtml(Object strPics,String tenanId,String pkId)
	{
		StringBuffer sbPicsBuffer=new StringBuffer("");
        if(strPics!=null&&strPics.toString().split(",").length>0){
        	for(String pic:strPics.toString().split(",")){
    			String filename=pic.substring(pic.lastIndexOf("/")+1, pic.length());
        		
    			try {
					storeConnector.saveStore("rolmexoa/pic", filename, tenanId, pkId, pic);
				} catch (Exception e) {
					e.printStackTrace();
				}
    			String picUrl=webAPI.getViewUrl()+"/"+pic;
        		sbPicsBuffer.append("<a href=\""+picUrl+"\" target=\"_blank\">"
        				+ "<img src=\""+picUrl+"\""
        				+ " style=\"margin-top: 7px; margin-bottom: 7px;\" width=\"84\" height=\"74\""
        				+ " border=\"0\" alt=\"\" class=\"photo\"/></a>");
        	}
        }
        return sbPicsBuffer.toString();
	}
    
    //region 给品质365的接口
    /**
     * 金卡商品推荐-添加或更新申请信息
     * @param strFRCode 申请编号(注：添加时，可为空；更新时，必填)
     * @param strUserID 申请用户编号
     * @param strOperateTime 申请时间(注：格式20170505122155)
     * @param strContractNum 合同编号(注：添加时，可为空；更新合同编号必填)
     * @param strComName 公司名称
     * @param strActionType 操作类型(1.添加 2重新提交 3更新邮寄状态 4上传打款凭证)
     * @param strMsg 签名
     * @return 返回字符串，ok成功 其他失败
     * @throws IOException 
     * **/
	@Override
	@Transactional(readOnly = false)
    public String ToPinZhiAddApply(
    		String strFRCode, 
    		String strUserID, 
    		String strOperateTime, 
    		String strContractNum,
    		String strComName, 
    		String strActionType, 
    		String strMsg) throws IOException{
		ResultData result=new ResultData();
		try {
			//参数解密
			/*strFRCode = Common.Decrypt3DES(strFRCode);
			strUserID = Common.Decrypt3DES(strUserID);
			strOperateTime = Common.Decrypt3DES(strOperateTime);
			strContractNum = Common.Decrypt3DES(strContractNum);
			strComName = Common.Decrypt3DES(strComName);
			strActionType = Common.Decrypt3DES(strActionType);*/
			
	        //记录参数日志
	        logger.info("SellValidPwdInit(金卡商品推荐-添加或更新申请信息) 参数："
	        		+ "strFRCode=" + strFRCode
	        		+ "strUserID=" + strUserID
	        		+ "strOperateTime=" + strOperateTime
	        		+ "strContractNum=" + strContractNum
	        		+ "strComName=" + strComName
	        		+ "strActionType=" + strActionType);
	        
	       // DateTimeFormatter fa = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	        
	        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	        
	        int actionType=Integer.valueOf(strActionType);
	        if(actionType<1|| actionType>4){
        		logger.info("ToPinZhiAddApply(金卡商品推荐-添加或更新申请信息):操作类型参数错误");
	        	result= new ResultData("fail", "操作类型参数错误", null,"");
                return jsonMapper.toJson(result);
	        }
	        
	        /*try{
	        	Date opterDate=format.parse(strOperateTime);
	        }catch(Exception ex){
	        	logger.info("ToPinZhiAddApply(金卡商品推荐-添加或更新申请信息):申请时间格式不正确");
	        	result = new ResultData("fail", "申请时间格式不正确", null,"");
                return jsonMapper.toJson(result);
	        }*/

	        //验证签名
	        String strKey = strFRCode.toUpperCase()+strUserID.toUpperCase()+strOperateTime.toUpperCase()
	        		+strContractNum.toUpperCase()+strComName.toUpperCase()+strActionType.toUpperCase();
	        if (!Common.VerifySign(strKey,strMsg,signInfo.getSignKey()))
	        {
	        	logger.info("ToPinZhiAddApply(金卡商品推荐-添加或更新申请信息):验证签名失败");
	        	result = new ResultData("fail", "验证签名失败", null,"");
                return jsonMapper.toJson(result);
	        }
	        
	        /**
	         *	品质365 	发起和审批流程. chengze 20171030
	         */
	        Map<String, Object> processParameters = new HashMap<String, Object>();
	        String userId = PartyConstants.SYSTEM_ROBOT_ID;
	        String businessKey = "";
	        String bpmProcessId = "";
	        String humanTaskId = "";
	        String processInstanceId = "";
	        String task_info_name = "";//当前审批节点的名称
	        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
	        String createTime = sdf.format(new Date());
	        
	        //先获取流程id
	        List<DictInfo> dictInfo = this.dictConnectorImpl.findDictInfoListByType("pinzhi365pbm");
	        for(DictInfo p:dictInfo){
	    		bpmProcessId = p.getValue();
	    	}
	    	
	         //添加
	        if(strActionType.equals("1")){
	        	Long newId=idGenerator.generateId();
	        	//存储资料
	        	String strSql="insert into Ro_PZ_Form(ID,applyCode,contractNum,companyName,ucode)"
	        			+ " values("+newId+",'"+strFRCode+"','"+strContractNum+"','"+strComName+"','"+strUserID+"')";
	        	
	        	keyValueConnector.updateBySql(strSql);
	        	//jdbcTemplate.update(strSql);
	        	
	        	/**
	        	 * TODO:发起一个流程  品质推送过来
	        	 * 流程开启时间，请用strOperateTime这个参数
	        	 * **/
	        	 /**
		         *  品质 365  发起流程. chengze 20171030
		         */
	        	//创建formParameter
		    	MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<String, String>();
		        multiValueMap.add("ucode", strUserID);
		        multiValueMap.add("applyCode", strFRCode);
		        multiValueMap.add("theme","品质365推送申请-"+createTime);
		       
		        multiValueMap.add("businessType", "品质365推送申请");
		       
		        multiValueMap.add("businessDetail", "品质365推送申请");
		        multiValueMap.add("url", "");
		       
		        FormParameter formParameter = this.operationService.saveDraft(userId, "1", "", businessKey, bpmProcessId, multiValueMap);
		        ProcessDTO processDto = processConnector.findProcess(bpmProcessId);
		        String processDefinitionId = processDto.getProcessDefinitionId();
		        Record record = keyValueConnector.findByCode(formParameter.getBusinessKey());
		        businessKey = formParameter.getBusinessKey();
		        
		        //得到岗位，存储add by lilei at 2018-09-12
		        //processParameters.put("positionId", request.getParameter("iptStartPositionId"));
		        //发起流程
		        this.operationService.startProcessInstance(userId, businessKey,
		                processDefinitionId, processParameters, record);
		        
		        //发起流程后，更新kv_record;
	        	Map<String,Object> map=jdbcTemplate.queryForMap(String.format("select ID from Ro_PZ_Form where applyCode='%s'", strFRCode));
	        	//String bussinessKey="";
	        	String sqlRecordUpdate =String.format("update KV_RECORD set pk_id ='%s' where BUSINESS_KEY= '%s'", 
	        			map.get("ID"),businessKey);
		    	keyValueConnector.updateBySql(sqlRecordUpdate);
	        }
	        else if(strActionType.equals("2")){
	        	//更新资料
	        	String strSql=String.format("update Ro_PZ_Form set companyName='%s' where applyCode='%s'",
	        			strComName,
	        			strFRCode);
	        	keyValueConnector.updateBySql(strSql);
	        	/*--5. 判断是否重新提交
	    		IF(EXISTS(SELECT varFRCode FROM RO_PF_FlowRecord WHERE varFRCode=@varFRCode AND chrStatus='7'))
	    		BEGIN
	    			UPDATE RO_PF_FlowRecord
	    			SET chrStatus='4'
	    			WHERE varFRCode=@varFRCode
	    		END*/
	        	/**
	        	 * TODO:如果流程为驳回发起人，则将步骤变为审核中-驳回
	        	 * 不做处理
	        	 * **/
	        }
		    else if(strActionType.equals("3")){
		    	String strSql=String.format("update Ro_PZ_Form set contractNum='%s' where applyCode='%s'",
		    			strContractNum,
	        			strFRCode);
		    	keyValueConnector.updateBySql(strSql);
	        	/**
	        	 * TODO:自动审核，流程审核就去下一个步骤
	        	 * 自动审核备注：金卡用户已邮寄合同，自动审核通过
	        	 * **/
	        	
		    	String strQuerySql="SELECT * FROM (SELECT r.BUSINESS_KEY,r.REF as processId,o.applyCode"
        				 +" from kv_record r "
						 +" INNER JOIN ro_pz_form o ON o.id=r.pk_id) t"
						 +" where t.applyCode='"+strFRCode+"'";
		    	
		    	Map<String,Object> queryMap=jdbcTemplate.queryForMap(strQuerySql);
		    	businessKey = queryMap.get("BUSINESS_KEY").toString();
		    	processInstanceId = queryMap.get("processId").toString();
		    	
		    	//先获取humanTaskId
		        String hql = "from TaskInfo where processInstanceId=? and name = '上传合同资料' and status = 'active'  ";
		        List<TaskInfo> taskInfos = taskInfoManager.find(hql, processInstanceId);
		        if(taskInfos.size()>0){
		        for(TaskInfo t : taskInfos){
		        	humanTaskId = Long.toString(t.getId());
		        }
		    	
		    	//创建formParameter
		        MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<String, String>();
		        multiValueMap.add("ucode", strUserID);
		        multiValueMap.add("applyCode", strFRCode);
		      
		        FormParameter formParameter = this.operationService.saveDraft(userId, "1", humanTaskId, businessKey, bpmProcessId, multiValueMap);
		        
		        Record record = keyValueConnector.findByCode(businessKey);
		        
		        //完成审批任务
		        processParameters.put("leaderComment", "同意");
	        	formParameter.setAction("同意");
	        	
	        	this.operationService.completeTask(humanTaskId, PartyConstants.SYSTEM_ROBOT_ID,
	                    formParameter, processParameters, record,
	                    processInstanceId);
		        
		        }
	        	
	        }
		    else if(strActionType.equals("4")){
		    	/**
		    	 * TODO:自动审核，流程审核就去下一个步骤
		    	 * 自动审核备注：金卡用户已上传打款凭证，自动审核通过
		    	 * **/
		    	
		    	String strQuerySql="SELECT * FROM (SELECT r.BUSINESS_KEY,r.REF as processId,o.applyCode"
       				 +" from kv_record r "
						 +" INNER JOIN ro_pz_form o ON o.id=r.pk_id) t"
						 +" where t.applyCode='"+strFRCode+"'";
		    	
		    	Map<String,Object> queryMap=jdbcTemplate.queryForMap(strQuerySql); 
		    	businessKey = queryMap.get("BUSINESS_KEY").toString();
		    	processInstanceId = queryMap.get("processId").toString();
		    	
		      //先获取humanTaskId
		        String hql = "from TaskInfo where processInstanceId=? and name = '上传打款凭证' and status = 'active'  ";
		        List<TaskInfo> taskInfos = taskInfoManager.find(hql, processInstanceId);
		        if(taskInfos.size()>0){
		        for(TaskInfo t : taskInfos){
		        	humanTaskId = Long.toString(t.getId());
		        }
		    	
		    	//创建formParameter
		        MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<String, String>();
		        multiValueMap.add("ucode", strUserID);
		        multiValueMap.add("applyCode", strFRCode);
		      
		        FormParameter formParameter = this.operationService.saveDraft(userId, "1", humanTaskId, businessKey, bpmProcessId, multiValueMap);
		        
		        Record record = keyValueConnector.findByCode(businessKey);
		        
		        //完成审批任务
		        processParameters.put("leaderComment", "同意");
	        	formParameter.setAction("同意");
	        	
	        	this.operationService.completeTask(humanTaskId, PartyConstants.SYSTEM_ROBOT_ID,
	                    formParameter, processParameters, record,
	                    processInstanceId);
		        
		        }
	        }
	        
	        result=new ResultData("success", "ok", null,"");
	        
		} catch (Exception ex) {
			logger.debug("ToPinZhiAddApply(金卡商品推荐-添加或更新申请信息)，出现异常："+ex.getMessage()+"\r\n"+ex.getStackTrace());
			result = new ResultData("fail", "申请失败", null,"");
			
		}
		return jsonMapper.toJson(result);
    }
	
    
    /**
     * 金卡商品推荐-查询状态及备注信息
     * @param strFRCode 申请编号
     * @param strMsg 签名
     * @return 返回字符串，ok成功 其他失败
     * @throws IOException 
     * **/
	@Override
	@Transactional(readOnly = false)
    public String ToPinZhiQueryStatus(String strFRCode, String strMsg) throws IOException{
		ResultData result=new ResultData();
		try {
			//参数解密
			//strFRCode = Common.Decrypt3DES(strFRCode);
	        //记录参数日志
	        logger.info("ToPinZhiQueryStatus(金卡商品推荐-查询状态及备注信息) 参数："
	        		+ "strFRCode=" + strFRCode);

	        //验证签名
	        String strKey = strFRCode.toUpperCase();
	        if (!Common.VerifySign(strKey,strMsg,signInfo.getSignKey()))
	        {
	        	logger.info("ToPinZhiQueryStatus(金卡商品推荐-查询状态及备注信息):验证签名失败");
	        	result = new ResultData("fail", "验证签名失败", null,"");
	        	return jsonMapper.toJson(result);
	        }
	        
	        //先查询出流程审核状态
	        String strProcessSql=String.format("SELECT f_OnlineStatus(r.BUSINESS_KEY) as varStatus,"
	        		+ "f.* FROM ro_pz_form f"
	        		+ " INNER JOIN kv_record r on r.pk_id=f.ID where  f.applyCode='%s'",strFRCode);
	        Map<String, Object> processMap=jdbcTemplate.queryForMap(strProcessSql);
	        if(processMap==null){
	        	result.setResult("fail");
	        	result.setDescription("申请不存在");
	        	result.setStatus("-1");
	        	jsonMapper.toJson(result);
	        }
	        
	        String strStatus=processMap.get("varStatus").toString();
	        result=getResultDataByStatus(strFRCode,strStatus);
	        int intResult=Integer.valueOf(result.getStatus());
	        if (intResult>=0 && intResult==8)
            {
	        	String strRemark=result.getDescription();
	        	String strOther = strRemark.split("^").length > 1 ? strRemark.split("^")[1] : "";
	        	String strAccount = strRemark.split("^").length > 1 ? strRemark.split("^")[0] : strRemark;

                AccountInfo accInfo = new AccountInfo();
                for(String item:strAccount.split(";"))
                {
                    switch(item.split(":")[0])
                    {
                        case "保证金":
                            accInfo.Bond=item.split(":")[1];
                            break;
                        case "服务费":
                            accInfo.ServiceCharge = item.split(":")[1];
                            break;
                        case "账户银行":
                            accInfo.AccountName = item.split(":")[1];
                            break;
                        case "账户卡号":
                            accInfo.AccountCode = item.split(":")[1];
                            break;
                    }
                }
                result.setAccount(accInfo);
            }
	        
		} catch (Exception ex) {
			logger.debug("ToPinZhiQueryStatus(金卡商品推荐-查询状态及备注信息)，出现异常："+ex.getMessage()+"\r\n"+ex.getStackTrace());
        	result = new ResultData("fail", "获取失败", null,"");
		}
		return jsonMapper.toJson(result);
		
    }
	
	private ResultData getResultDataByStatus(String strFRCode,String strStatus){
		/*******************************************************************************************************
		 * 一、审核结果值如下：
		 * 【0审核通过 1审核未通过结束 2待审核 3资质待提交 4资质审核中 
		 * 5合同待邮寄 6合同待审核 7合同审核中 8待打款 9财务审核中】
		 * *****************************************************************************************************
		 * 二、审核结果释解：
		 * 1.如果状态为2（通过），则返回0，备注为空
		 * 2.如果审核状态为3（审核未通过） 6（已取消），则返回1，审核批注
		 * 3.如果审核状态为0（未审核），则返回4，备注为空
		 * 4.如果审核状态为7（驳回发起人），则返回3，审批备注
		 * 5.如果审核状态为4（审核中（驳回）），则如下（根据驳回步骤判断）：
		 * ****(1).如果驳回步骤1，则返回4，审批备注（条件：步骤2和审核驳回）
		 * ****(2).如果驳回步骤3，则返回5，审批备注（条件：步骤4、5、6和审核驳回）
		 * ****(3).如果驳回步骤7，则返回8，返回备注（intStep=8 AND chrOption='3'和intStep=6 AND chrOption='1'叠加）
		 * ****(4).如果驳回步骤8，则返回9，审批备注（intStep=9 AND chrOption='3'）
		 * 6.如果是其他状态，则如下（根据驳回步骤判断）：
		 * ****(1).如果驳回步骤1，则返回4，审批为空
		 * ****(2).如果驳回步骤2，则返回4，审批备注（intStep=1 AND chrOption!='0'）
		 * ****(3).如果驳回步骤3，则返回5，审批备注（intStep=2 AND chrOption!='0'）
		 * ****(4).如果驳回步骤4，则返回6，审批备注（intStep=3 AND chrOption!='0'）
		 * ****(5).如果驳回步骤5or6，则返回7，审批备注（intStep=4 AND chrOption!='0'）
		 * ****(6).如果驳回步骤6，则返回7，审批备注（intStep=5 AND chrOption!='0'）
		 * ****(7).如果驳回步骤7，则返回8，审批备注（intStep=6 AND chrOption!='0'）
		 * ****(8).如果驳回步骤8，则返回9，审批备注（intStep=7 AND chrOption!='0'）
		 * ****(9).如果驳回步骤9，则返回9，审批备注（intStep=8 AND chrOption!='0'）
		 * ******************************************************************************************************/
		
		String strBasicSql=String.format("SELECT IFNULL(`COMMENT`,'') AS note,i.`NAME` AS varStatusDesc FROM TASK_INFO i "
						+" INNER JOIN kv_record r on i.BUSINESS_KEY=r.BUSINESS_KEY"
						+" INNER JOIN ro_pz_form f on f.ID=r.pk_id where f.applyCode='%s'",
						strFRCode);
		String strLastSql=" ORDER BY i.CREATE_TIME DESC LIMIT 0,1";
		ResultData result=new ResultData();
		if(strStatus.equals("2"))
        {
			//审核通过
        	result.setStatus("0");
        	result.setDescription("");
        }
		else if(strStatus.equals("3")||strStatus.equals("6")){
			//审核不同意
			String strSql=strBasicSql+" and i.ACTION='不同意'" +strLastSql;
			Map<String,Object> resultMap=jdbcTemplate.queryForMap(strSql);
			
        	result.setStatus("1");
        	result.setDescription(resultMap.get("note").toString());
		}
		else if(strStatus.equals("0")){
			//未审核
			result.setStatus("4");   
        	result.setDescription("");
		}
		else{
			String strSql=strBasicSql+strLastSql;
			Map<String,Object> resultMap=jdbcTemplate.queryForMap(strSql);
			
			String strPZStatusDesc=resultMap.get("varStatusDesc").toString();
			String strPZStatus="0";
			/*0审核通过 1审核未通过结束 2待审核 3资质待提交 4资质审核中 
			 * 5合同待邮寄 6合同待审核 7合同审核中 8待打款 9财务审核中*/
			switch (strPZStatusDesc) {
			case "金卡用户发起":
				strPZStatus="3";
				break;
			case "资料产品专员":
				strPZStatus="4";
				break;
			case "资料招商专员":
				strPZStatus="4";
				break;
			case "上传合同资料":
				strPZStatus="5";
				break;
			case "合同产品专员":
				strPZStatus="6";
				break;
			case "合同招商专员":
				strPZStatus="7";
				break;
			case "合同产品专员二":
				strPZStatus="7";
				break;
			case "上传打款凭证":
				strPZStatus="8";
				break;
			case "财务专员":
				strPZStatus="9";
				break;
			case "财务产品专员":
				strPZStatus="9";
				break;
			}
			result.setStatus(strPZStatus);
			
			String note=resultMap.get("note").toString();
			if(strPZStatus.equals("8")){
				String strOtherSql=strBasicSql+" and i.name='合同产品专员二'"+strLastSql;
				Map<String,Object> resultOtherMap=jdbcTemplate.queryForMap(strSql);
				note=resultOtherMap.get("note").toString()+note;
			}
        	result.setDescription(note);
		}
		result.setResult("success");
		return result;
	}
    //endregion
	
	private class ResultData{
		private String result;
		private String status;
		private String description;
		private AccountInfo account;
		
		public ResultData(){
			
		}
		
        public ResultData(String strResult, String strDesc,AccountInfo acc, String strStatus)
        {
            this.result = strResult;
            this.status = strStatus;
            this.description = strDesc;
            this.account = acc;
        }

		public String getResult() {
			return result;
		}

		public void setResult(String result) {
			this.result = result;
		}

		public String getStatus() {
			return status;
		}

		public void setStatus(String status) {
			this.status = status;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public AccountInfo getAccount() {
			return account;
		}

		public void setAccount(AccountInfo account) {
			this.account = account;
		}
	}
	
	private class AccountInfo
    {
		private String Bond;
		private String ServiceCharge;
		private String AccountName;
		private String AccountCode;
        
        public String getBond() {
			return Bond;
		}
		public void setBond(String bond) {
			Bond = bond;
		}
		public String getServiceCharge() {
			return ServiceCharge;
		}
		public void setServiceCharge(String serviceCharge) {
			ServiceCharge = serviceCharge;
		}
		public String getAccountName() {
			return AccountName;
		}
		public void setAccountName(String accountName) {
			AccountName = accountName;
		}
		public String getAccountCode() {
			return AccountCode;
		}
		public void setAccountCode(String accountCode) {
			AccountCode = accountCode;
		}
    }
	
	/**
     * 通过multipart请求构建formParameter.
     */
    public FormParameter buildFormParameter(MultipartHandler multipartHandler) {
        FormParameter formParameter = new FormParameter();
        formParameter.setMultiValueMap(multipartHandler.getMultiValueMap());
        formParameter.setMultiFileMap(multipartHandler.getMultiFileMap());
        formParameter.setBusinessKey(multipartHandler.getMultiValueMap()
                .getFirst("businessKey"));
        formParameter.setBpmProcessId(multipartHandler.getMultiValueMap()
                .getFirst("bpmProcessId"));
        formParameter.setHumanTaskId(multipartHandler.getMultiValueMap()
                .getFirst("humanTaskId"));
        formParameter.setComment(multipartHandler.getMultiValueMap().getFirst(
                "comment"));

        return formParameter;
    }
	
	 /**
     * 把数据先保存到keyvalue里.
     */
    public FormParameter doSaveRecord(HttpServletRequest request)
            throws Exception {
        String userId = currentUserHolder.getUserId();
        String tenantId = tenantHolder.getTenantId();

        MultipartHandler multipartHandler = new MultipartHandler(
                multipartResolver);
        FormParameter formParameter = null;

        try {
            multipartHandler.handle(request);
            logger.debug("multiValueMap : {}",
                    multipartHandler.getMultiValueMap());
            logger.debug("multiFileMap : {}",
                    multipartHandler.getMultiFileMap());

            formParameter = this.buildFormParameter(multipartHandler);
            
            // formParameter.setBpmProcessId("797146747797504");
            
            String businessKey = operationService.saveDraft(userId, tenantId,
                    formParameter);

            if ((formParameter.getBusinessKey() == null)
                    || "".equals(formParameter.getBusinessKey().trim())) {
                formParameter.setBusinessKey(businessKey);
            }
            // TODO zyl 2017-11-16 外部表单不需要保存prop
            /*Record record = keyValueConnector.findByCode(businessKey);

            record = new RecordBuilder().build(record, multipartHandler,
                    storeConnector, tenantId);

            keyValueConnector.save(record);*/
        } finally {
            multipartHandler.clear();
        }

        return formParameter;
    }
    
    
    //region 用于导入数据调用的接口 add by lilei at 2018-05-03
    
    /**
     * 用于导入数据的的-创建
     * */
    @WebMethod
	@Transactional(readOnly = false)
	public boolean CreateApplyForImportData( String varOlCode, 
    		String varApplyCode, 
    		String varBranch, 
    		String varApplyName,
    		String varApplyIdentity, 
    		String varWelfareGrade, 
    		String varMobile, 
    		String varNewName, 
    		String varNewIdentity,
    		String varBankName,
    		String varBankAddress,
    		String varBankCode, 
    		String varApplyPic, 
    		String varNewPic, 
    		String varPartPic1, 
    		String varPartPic2,
    		String varPartPic3, 
    		String varRelativesPic,
    		String chrApplyType, 
    		String varReason,
    		String strShopLicense,
    		String strApplyDate,
    		String strMsg
    		) throws Exception{
    	DataImportLog importLog=new DataImportLog();
    	String strOlCode=varOlCode;
    	String strApplyCode=varApplyCode;
    	String strBranch=varBranch;
    	try {
    		String paramKey=signInfo.getParamKey();	
			//解析参数
			strOlCode = Common.Decrypt3DES(strOlCode,paramKey);
			strApplyCode = Common.Decrypt3DES(strApplyCode,paramKey);
	        strBranch = Common.Decrypt3DES(strBranch,paramKey);
	        strApplyDate= Common.Decrypt3DES(strApplyDate,paramKey);
			if(CreateApply(varOlCode, 
        		 varApplyCode, 
        		 varBranch, 
        		 varApplyName,
        		 varApplyIdentity, 
        		 varWelfareGrade, 
        		 varMobile, 
        		 varNewName, 
        		 varNewIdentity,
        		 varBankName,
        		 varBankAddress,
        		 varBankCode, 
        		 varApplyPic, 
        		 varNewPic, 
        		 varPartPic1, 
        		 varPartPic2,
        		 varPartPic3, 
        		 varRelativesPic,
        		 chrApplyType, 
        		 varReason,
        		 strShopLicense,
        		 "",
        		 "",
        		 "",
        		 "",
        		 "",
        		 "",
        		 "",
        		 "",
        		 "",
        		 "",
        		 "",
        		 "",
        		 strMsg)){
					
					Date dtmApplyDate=DateUtils.parseDate(strApplyDate);
					RecordInfo record=recordManager.findUniqueBy("applyCode", strOlCode);
					if(record!=null){
						record.setCreateTime(dtmApplyDate);
						recordManager.save(record);
						
						TaskInfo taskInfo=taskInfoManager.findUnique("from TaskInfo where catalog='start' and businessKey=?", record.getBusinessKey());
						if(taskInfo!=null){
							taskInfo.setCreateTime(dtmApplyDate);
							taskInfo.setCompleteTime(dtmApplyDate);
							taskInfoManager.save(taskInfo);
						}
					}
					
					OnLineInfo onLineInfo=onLineInfoManager.findUniqueBy("applycode", strOlCode);
					if(onLineInfo!=null){
						onLineInfo.setApplytime(dtmApplyDate);
						onLineInfoManager.save(onLineInfo);
					}
					
					//jdbcTemplate.update("update kv_record set create_time=? where applyCode=?",dtmApplyDate,strOlCode);
					//jdbcTemplate.update("update ro_pf_oaonline set dtmApplyTime=? where varApplyCode=?",dtmApplyDate,strOlCode);
					//jdbcTemplate.update("update task_info set create_time=?,complete_time=? where catalog='start' And BUSINESS_KEY=(SELECT BUSINESS_KEY FROM kv_record r WHERE r.applyCode=?)",dtmApplyDate,dtmApplyDate,strOlCode);
					importLog.setResult("success");
					importLog.setAction("CreateApplyForImportData");
					importLog.setMsg("申请成功");
					importLog.setNote("申请编号："+strOlCode+",用户编号："+strApplyCode+",部门："+strBranch);
					SaveDataLog(importLog);
					return true;
			}
			else
			{
				importLog.setResult("error");
				importLog.setAction("CreateApplyForImportData");
				importLog.setMsg("申请失败");
				importLog.setNote("申请编号："+varOlCode+",用户编号："+varApplyCode+",部门："+varBranch);
				SaveDataLog(importLog);
				return false;
			}
		} catch (Exception e) {
			
			importLog.setResult("error");
			importLog.setAction("CreateApplyForImportData：添加申请");
			importLog.setMsg("插入数据异常："+e.getMessage());
			importLog.setNote("申请编号："+strOlCode+",用户编号："+strApplyCode+",部门："+strBranch);
			SaveDataLog(importLog);
			return false;
		}
    }
    
    /**
     * 用于导入数据-二次上传资料
     * */
	@Override
	@Transactional(readOnly = false)
    public String ApplyUploadFileForImportData(
    		String strFRCode, 
    		String strImages,
    		String strUserID,
    		String strReceive,
    		String strMobile,
    		String strAddress,
    		String strApplyDate,
    		String strAuditDate,
    		String strMsg){
		DataImportLog importLog=new DataImportLog();
		String strOlCode=strFRCode;
		String strApplyCode=strUserID;
		try {
			String paramKey=signInfo.getParamKey();	
			//解析参数
			strFRCode = Common.Decrypt3DES(strFRCode,paramKey);
			strApplyCode = Common.Decrypt3DES(strApplyCode,paramKey);
			strApplyDate= Common.Decrypt3DES(strApplyDate,paramKey);
			strAuditDate= Common.Decrypt3DES(strAuditDate,paramKey);
			if(ApplyUploadFile(strOlCode, 
    	    		strImages,
    	    		strUserID,
    	    		strReceive,
    	    		strMobile,
    	    		strAddress, 
    	    		strMsg).equals("ok")){
				
						Date dtmApplyDate=DateUtils.parseDate(strApplyDate);
						Date dtmAuditDate=DateUtils.parseDate(strAuditDate);
						RecordInfo record=recordManager.findUniqueBy("applyCode", strFRCode);
						if(record!=null){							
							List<TaskInfo> taskInfoList=taskInfoManager.find("from TaskInfo where catalog='normal' and assignee='4' and businessKey=? order by createTime desc", record.getBusinessKey());
							if(taskInfoList!=null&&taskInfoList.size()>0)
							{
								TaskInfo taskInfo=taskInfoList.get(0);
								if(taskInfo!=null){
									taskInfo.setCreateTime(dtmAuditDate);
									taskInfo.setCompleteTime(dtmAuditDate);
									taskInfoManager.save(taskInfo);
								}
							}
						}
						//jdbcTemplate.update("update task_info set create_time=?,complete_time=? where catalog='normal' and ASSIGNEE='4' And BUSINESS_KEY=(SELECT BUSINESS_KEY FROM kv_record r WHERE r.applyCode=?)",dtmApplyDate,dtmAuditDate,strFRCode);
						importLog.setResult("success");
						importLog.setAction("ApplyUploadFileForImportData：二次资料上传");
						importLog.setMsg("二次资料上传成功");
						importLog.setNote("申请编号："+strFRCode+",用户编号："+strApplyCode);
						SaveDataLog(importLog);
						return "ok";
    		}
			else {
				
				importLog.setResult("error");
				importLog.setAction("ApplyUploadFileForImportData：二次资料上传");
				importLog.setMsg("二次资料上传失败");
				importLog.setNote("申请编号："+strFRCode+",用户编号："+strApplyCode);
				SaveDataLog(importLog);
				return "";
			}
		} catch (Exception e) {
			importLog.setResult("error");
			importLog.setAction("ApplyUploadFileForImportData：二次资料上传");
			importLog.setMsg("二次资料上传异常："+e.getMessage());
			importLog.setNote("申请编号："+strFRCode+",用户编号："+strApplyCode);
			SaveDataLog(importLog);
			return "";
		}
	}
	
	/**
	 * 用于导入数据的-查询单子详情
	 * 
	 * */
	@Override
    public String SearchApplyDetailForImportData(String strOLID, String strMsg) throws IOException{
		return SearchApplyDetail(strOLID,strMsg);
	}
	
   	/**
     * 用于申请单的审核
     * @param varOlCode 申请编号
     * @param strAuditors 审核人（多个用逗号(,)隔开）
     * @param strMsg 签名（用varOlCode+key）
     * @param strAuditType 审核类型，1：同意，0：驳回
     * @return 返回字符串，ok成功 其他失败
     * **/
    @Transactional(readOnly = false)
    public String ApplyAuditForImportData(
    		String varOlCode,
    		String strAuditors,
    		String strApplyDate,
    		String strAuditDate,
    		String strAuditNote,
    		String strAuditType,
    		String strMsg
    		) throws Exception{
    	//处理规则
    	//Map<String, Object> returnMap=new HashMap<String, Object>();
    	DataImportLog importLog=new DataImportLog();
		//参数解密
		String strOlCode =varOlCode;
		String str_Auditors =strAuditors;
		//strAudit_Type=strAuditType;
    	try {
    		String paramKey=signInfo.getParamKey();
			//参数解密
    		strOlCode = Common.Decrypt3DES(strOlCode,paramKey);
    		str_Auditors = Common.Decrypt3DES(str_Auditors,paramKey);
    		strApplyDate=Common.Decrypt3DES(strApplyDate,paramKey);
    		strAuditDate= Common.Decrypt3DES(strAuditDate,paramKey);
    		strAuditNote= Common.Decrypt3DES(strAuditNote,paramKey);
    		strAuditType= Common.Decrypt3DES(strAuditType,paramKey);
    		
	        //记录参数日志
	        logger.info("ApplyAuditForImportData(申请单的审核) 参数："
	        		+ "varOlCode=" + strOlCode
	        		+ "strAuditors=" + str_Auditors
	        		+ "strAuditType="+strAuditType);

	        //验证签名
	        String strKey = strOlCode.toUpperCase();
	        String signKey=signInfo.getSignKey();
	        if (!Common.VerifySign(strKey,strMsg,signKey))
	        {
	        	logger.info("ApplyUploadFile(申请单的审核):验证签名失败");
	        	return "验证签名失败";
	        }
    		
    		if(!StringUtils.isBlank(str_Auditors)){
    			
    			String frCode=strOlCode;
    			String sql="select * from kv_record where applycode='" +frCode+"'";
    			List<Map<String, Object>> list=jdbcTemplate.queryForList(sql);
    			//受理单号不存在
    			if(list==null)
    			{
    				importLog.setResult("error");
    				importLog.setAction("ApplyAuditForImportData：申请单的审核");
    				importLog.setMsg("受理单号不存在，kv_record");
    				importLog.setNote("申请编号："+strOlCode+",审核人："+str_Auditors+ "，审核类型(1:同意,0:驳回)strAuditType="+strAuditType);
    				SaveDataLog(importLog);
        			
        			/*returnMap.put("result", "error");
        			returnMap.put("msg", "受理单号不存在");
        			return jsonMapper.toJson(returnMap);*/
        			return "受理单号不存在";
    			}
    			//受理单存在多个
    			if(list!=null&&list.size()>1){
    				importLog.setResult("error");
    				importLog.setAction("ApplyAuditForImportData：申请单的审核");
    				importLog.setMsg("存在多个受理单号，kv_record");
    				importLog.setNote("申请编号："+strOlCode+",审核人："+str_Auditors+ "，审核类型(1:同意,0:驳回)strAuditType="+strAuditType);
    				SaveDataLog(importLog);
        			
        			/*returnMap.put("result", "error");
        			returnMap.put("msg", "存在多个受理单号");
        			return jsonMapper.toJson(returnMap);*/
        			return "存在多个受理单号";
    			}

    			Map<String, Object> map = list.get(0);
    			String businessKey =(String) map.get("business_Key");
    			String userId="";
    			String[] strDateArray=strAuditDate.split(",");
    			String[] strNoteArray=strAuditNote.split(",");
    			String[] strApplyDateArray=strApplyDate.split(",");
    			int i=0;
    			String sqlByName="select * from account_info where display_name='%s' and del_flag='0'";
    			String sqlByUserName="select * from account_info where username='%s' and del_flag='0'";
    			for(String auditor:str_Auditors.split(",")){
    				if(StringUtils.isBlank(auditor)) continue;
    				switch(auditor.trim()){
    				case "陈娟":
    					sql=String.format(sqlByUserName,"zjchenjuan");
    					break;
    				case "华东湖北":
    					sql=String.format(sqlByUserName,"hbchengguangming");
    					break;
    				case "华北天津":
    					sql=String.format(sqlByUserName,"liweifeng");
    					break;
    				case "华北山东":
    					sql=String.format(sqlByUserName,"sdyuchao");
    					break;
    				case "周靖欣":
    					sql=String.format(sqlByUserName,"gzleibangqin");
    					break;
    				case "四川专员":
    					sql=String.format(sqlByUserName,"scyanglin");
    					break;
    				case "山东专员":
    					sql=String.format(sqlByUserName,"sdhuyinyin");
    					break;
    				case "李明":
    					sql=String.format(sqlByUserName,"hbduanyuan");
    					break;
    				case "江西专员":
    					sql=String.format(sqlByUserName,"jxhuyanmei");
    					break;
    				case "深圳专员":
    					sql=String.format(sqlByUserName,"szliaolin");
    					break;
    				case "辽宁专员":
    					sql=String.format(sqlByUserName,"lnchenfeng");
    					break;
    				case "郭阳洋":
    					sql=String.format(sqlByUserName,"cqxiangdongmei");
    					break;
    				case "陕西":
    					sql=String.format(sqlByUserName,"shxzhuqian");
    					break;
    				case "王晓冉":
    					sql=String.format(sqlByUserName,"3moons");
    					break;
    				default:
    					sql=String.format(sqlByName,auditor);
    					break;
    				}
    				
    				//sql="select * from account_info where display_name='" +auditor+"' and del_flag='0'";
        			List<Map<String, Object>> accountList=jdbcTemplate.queryForList(sql);
        			if(accountList!=null&&accountList.size()>0){
        				//存在多个审核人
        				if(accountList.size()>1){
    	    				importLog.setResult("error");
    	    				importLog.setAction("ApplyAuditForImportData：申请单的审核");
    	    				importLog.setMsg("“"+auditor+"”存在多个人，account_info");
    	    				importLog.setNote("申请编号："+strOlCode+",审核人："+str_Auditors);
    	    				SaveDataLog(importLog);
    	        			
    	        			/*returnMap.put("result", "error");
    	        			returnMap.put("msg", "“"+auditor+"”存在多个人，account_info");
    	        			return jsonMapper.toJson(returnMap);*/
    	    				return "“"+auditor+"”存在多个人，account_info";
        				}
        				
        				Map<String, Object> accountMap=accountList.get(0);
        				userId=accountMap.get("id").toString();
        				
        				sql="from TaskInfo where status ='active' and catalog<>'copy' and catalog<>'start' and businessKey=?";
            			List<TaskInfo> taskInfoList = taskInfoManager.find(sql,businessKey);
            			if(taskInfoList!=null&&taskInfoList.size()>0){
            				//存在多个active的taskinfo
            				if(accountList.size()>1){
        	    				importLog.setResult("error");
        	    				importLog.setAction("ApplyAuditForImportData：申请单的审核");
        	    				importLog.setMsg("存在多个active的taskinfo");
        	    				importLog.setNote("申请编号："+strOlCode+",审核人："+str_Auditors+ "，审核类型(1:同意,0:驳回)strAuditType="+strAuditType);
        	    				SaveDataLog(importLog);
        	        			
        	        			/*returnMap.put("result", "error");
        	        			returnMap.put("msg", "存在多个审核任务");
        	        			return jsonMapper.toJson(returnMap);*/
        	        			return "存在多个审核任务";
            				}
            				
            				//更新审核人
            				TaskInfo taskInfo=taskInfoList.get(0);
            				taskInfo.setAssignee(userId);
            				taskInfoManager.save(taskInfo);
            				
            				String tempHumanTaskId = Long.toString(taskInfo.getId());
            				String processInstanceId = taskInfo.getProcessInstanceId();
            				String strAction="同意";
            				if(strAuditType.equals("0"))
            					strAction="驳回";
            				
            				Map<String, Object> processParameters = new HashMap<String, Object>();
            				FormParameter formParameter = new FormParameter();
            				String strComment=strNoteArray[i];
            				formParameter.setAction(strAction);
                			formParameter.setBusinessKey(businessKey);
                			formParameter.setComment(strComment);
                			 
                			processParameters.put("leaderComment", strAction);
                			 
                			Record record = keyValueConnector.findByCode(formParameter.getBusinessKey());
                			 
                			// 驳回操作，清空抄送人
                        	String sqlRecordUpdate = "update KV_PROP set value= '' where code = 'activityId' and record_id= '" + record.getCode() + "'";
                        	keyValueConnector.updateBySql(sqlRecordUpdate);
                	        	
                			this.operationService.completeTask(tempHumanTaskId, userId,
                		                    formParameter, processParameters, record,
                		                    processInstanceId);
                			
                			//更新审核时间
                			taskInfo.setCreateTime(DateUtils.parseDate(strDateArray[i]));
                			taskInfo.setCompleteTime(DateUtils.parseDate(strDateArray[i]));
                			taskInfoManager.save(taskInfo);
                			
                			i++;
            			}
        			}
        			else {
        				importLog.setResult("error");
        				importLog.setAction("ApplyAuditForImportData：申请单的审核");
        				importLog.setMsg("找不到“"+auditor+"”这个人的账号");
        				importLog.setNote("申请编号："+strOlCode+",审核人："+auditor+ "，审核类型(1:同意,0:驳回)strAuditType="+strAuditType);
        				SaveDataLog(importLog);
        				return "找不到“"+auditor+"”这个人的账号";
    				}
    			}
    			
				importLog.setResult("success");
				importLog.setAction("ApplyAuditForImportData：申请单的审核");
				importLog.setMsg("审核成功");
				importLog.setNote("申请编号："+strOlCode+",审核人："+str_Auditors+ "，审核类型(1:同意,0:驳回)strAuditType="+strAuditType);
				SaveDataLog(importLog);
    			
    			/*returnMap.put("result", "ok");
    			returnMap.put("msg", "成功");*/
    			return "ok";
    		}
		} catch (Exception e) {
			importLog.setResult("error");
			importLog.setAction("ApplyAuditForImportData：申请单的审核");
			importLog.setMsg("审核异常："+e.getMessage());
			importLog.setNote("申请编号："+strOlCode+",审核人："+str_Auditors+ "，审核类型(1:同意,0:驳回)strAuditType="+strAuditType);
			SaveDataLog(importLog);
			
			/*returnMap.put("result", "error");
			returnMap.put("msg", "失败了，"+e.getMessage());*/
			return "失败了，"+e.getMessage();
		}
    	//return jsonMapper.toJson(returnMap);
    	return "";
    }
	
    
    private class DataImportLog  implements java.io.Serializable {
    	private static final long serialVersionUID = 0L;
    	
    	/**
    	 * 主键ID
    	 * **/
		private long id;
		/**
    	 * 结果
    	 * **/
		private String result;
		/**
    	 * 动作/方法
    	 * **/
		private String action;
		/**
    	 * 消息记录
    	 * **/
		private String msg;
		/**
    	 * 备注
    	 * **/
		private String note;
		
		@Id
		public long getId() {
			return id;
		}
		public void setId(long id) {
			this.id = id;
		}
		public String getResult() {
			return result;
		}
		public void setResult(String result) {
			this.result = result;
		}
		public String getAction() {
			return action;
		}
		public void setAction(String action) {
			this.action = action;
		}
		public String getMsg() {
			return msg;
		}
		public void setMsg(String msg) {
			this.msg = msg;
		}
		public String getNote() {
			return note;
		}
		public void setNote(String note) {
			this.note = note;
		}
	}
    
    private void SaveDataLog(DataImportLog log)
    {
    	try {
    		//log.setId(new Random(1000000000).nextLong());
    		log.setId(idGenerator.generateId());
			String sqlString="insert into DataImport_Log(id,result,action,msg,note) values(?,?,?,?,?)";
			jdbcTemplate.update(sqlString, 
								log.getId(),
								log.getResult(),
								log.getAction(),
								log.getMsg(),
								log.getNote());
		} catch (Exception e) {
			throw e;
			// TODO: handle exception
		}
    }
    
    //endregion
    
    //region 资源注入
	@Resource
    public void setWebAPI(WebAPI webAPI){
    	this.webAPI=webAPI;
    }
	
	@Resource
	public void setOnLineInfoManager(OnLineInfoManager onLineInfoManager) {
		this.onLineInfoManager = onLineInfoManager;
	}
	
	@Resource
    public void setStoreConnector(StoreConnector storeConnector){
    	this.storeConnector=storeConnector;
    }
	
	@Resource
	public void setTenantHolder(TenantHolder tenantHolder) {
		this.tenantHolder = tenantHolder;
	}
	
	@Resource
    public void setKeyValueConnector(KeyValueConnector keyValueConnector) {
        this.keyValueConnector = keyValueConnector;
    }
	
	@Resource
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
	
	@Resource
    public void setCodeManager(CodeManager codeManager) {
        this.codeManager = codeManager;
    }
	
	@Resource
    public void setProcessConnector(ProcessConnector processConnector) {
        this.processConnector = processConnector;
    }
	
	@Resource
    public void setOperationService(OperationService operationService) {
        this.operationService = operationService;
    }
	
	@Resource
    public void setBusinessDetailManager(BusinessDetailManager businessDetailManager) {
        this.businessDetailManager = businessDetailManager;
    }
	
	@Resource
    public void setCurrentUserHolder(CurrentUserHolder currentUserHolder) {
        this.currentUserHolder = currentUserHolder;
    }

	@Resource
    public void setMultipartResolver(MultipartResolver multipartResolver) {
        this.multipartResolver = multipartResolver;
    }
	
	@Resource
    public void setHumanTaskConnector(HumanTaskConnector humanTaskConnector) {
        this.humanTaskConnector = humanTaskConnector;
    }
	
	@Resource
	public void setTaskInfoManager(TaskInfoManager taskInfoManager) {
		this.taskInfoManager = taskInfoManager;
	}

	
	@Resource
	public void setDictConnectorImpl(DictConnectorImpl dictConnectorImpl) {
		this.dictConnectorImpl = dictConnectorImpl;
	}
	
	@Resource
    public void setIdGenerator(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }
	
	@Resource
	public void setSignInfo(SignInfo signInfo) {
		this.signInfo = signInfo;
	}
	
	@Resource
	public void setWSApplyService(WSApplyService wSApplyService) {
		this.wSApplyService = wSApplyService;
	}
	
	@Resource
	public void setRecordManager(RecordManager recordManager) {
		this.recordManager = recordManager;
	}
	//endregion
}
