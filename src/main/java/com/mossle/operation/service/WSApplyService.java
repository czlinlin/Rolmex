package com.mossle.operation.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
//import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;
import javax.ws.rs.core.Request;
import javax.xml.rpc.holders.StringHolder;

import org.apache.commons.collections.map.HashedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import ch.qos.logback.core.joran.conditional.IfAction;

import com.mossle.api.keyvalue.FormParameter;
import com.mossle.api.keyvalue.KeyValueConnector;
import com.mossle.api.keyvalue.Record;
import com.mossle.api.msg.MsgPushConnector;
import com.mossle.api.process.ProcessConnector;
import com.mossle.api.process.ProcessDTO;
import com.mossle.api.store.StoreConnector;
import com.mossle.api.tenant.TenantHolder;
import com.mossle.base.persistence.domain.BusinessDetailEntity;
import com.mossle.base.persistence.manager.BusinessDetailManager;
import com.mossle.common.utils.DateUtils;
import com.mossle.common.utils.PasswordUtil;
import com.mossle.common.utils.WebAPI;
import com.mossle.core.mapper.JsonMapper;
import com.mossle.core.md5.MD5Util;
import com.mossle.core.spring.SignInfo;
import com.mossle.core.util.StringUtils;
import com.mossle.humantask.persistence.domain.TaskInfo;
import com.mossle.humantask.persistence.manager.TaskInfoManager;
import com.mossle.keyvalue.persistence.domain.RecordInfo;
import com.mossle.keyvalue.persistence.domain.TimeTaskInfo;
import com.mossle.keyvalue.persistence.manager.RecordManager;
import com.mossle.keyvalue.persistence.manager.TimeTaskManager;
import com.mossle.msg.persistence.manager.MsgInfoManager;
import com.mossle.operation.persistence.domain.CodeEntity;
import com.mossle.operation.persistence.manager.CodeManager;
import com.mossle.party.PartyConstants;
import com.mossle.user.persistence.domain.PersonInfo;
import com.mossle.user.persistence.manager.PersonInfoManager;
import com.mossle.ws.persistence.domain.OnLineInfo;
import com.mossle.ws.persistence.manager.OnLineInfoManager;
import com.rolmex.common.HttpRequester;
import com.rolmex.common.HttpRespons;

@SuppressWarnings("unused")
@Service
@Transactional(readOnly = true)
public class WSApplyService {
	private static Logger logger = LoggerFactory.getLogger(WSApplyService.class);
	private OnLineInfoManager onLineInfoManager;
	private StoreConnector storeConnector;
	private TenantHolder tenantHolder;
	private KeyValueConnector keyValueConnector;
	private JdbcTemplate jdbcTemplate;

	//private JsonMapper jsonMapper = new JsonMapper();
	private CodeManager codeManager;
	private ProcessConnector processConnector;
	private OperationService operationService;
	private BusinessDetailManager businessDetailManager;
	private WebAPI webAPI;
    private String _ShorName = "OAJXS";
	private SignInfo signInfo;

	private TaskInfoManager taskInfoManager;
	private RecordManager recordManager;
	private TimeTaskManager timeTaskManager;
	private JsonMapper jsonMapper = new JsonMapper();
    @Autowired
	private MsgPushConnector msgPushConnector;
	@Autowired
	private MsgInfoManager msgInfoManager;
	private PersonInfoManager personInfoManager;
	
	//region 创建直销过来申请
	@Transactional(readOnly = false)
	public boolean CreateApply(OnLineInfo onLineInfo) {
        try {
	        //save
	        onLineInfoManager.save(onLineInfo);

	        /**
	         * 发起流程. chengze 20171028
	         */
	        Map<String, Object> processParameters = new HashMap<String, Object>();
	        String userId = PartyConstants.JXS_ID;
	        String businessKey = "";

	        //先获取流程id
	        String hql = "from BusinessDetailEntity where  id=? and formid='oaServicePushProcess'";
	        BusinessDetailEntity businessDetailEntity = businessDetailManager.findUnique(hql, Long.valueOf(onLineInfo.getApplytype()));
	    	String bpmProcessId = businessDetailEntity.getBpmProcessId();

	    	//创建formParameter
	    	SimpleDateFormat formatDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    	MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<String, String>();
	        multiValueMap.add("ucode", onLineInfo.getUcode());
	        multiValueMap.add("applyCode", onLineInfo.getApplycode());
	        multiValueMap.add("theme", businessDetailEntity.getBusiDetail() + formatDate.format(onLineInfo.getApplytime()));
	        multiValueMap.add("busType", String.valueOf(businessDetailEntity.getTypeId()));
	        multiValueMap.add("businessType", businessDetailEntity.getBusinessType());
	        multiValueMap.add("busDetails", String.valueOf(businessDetailEntity.getId()));
	        multiValueMap.add("businessDetail", businessDetailEntity.getBusiDetail());
	        multiValueMap.add("url", "/operationOnlineOrder/operation-OnLine-Form.do");
	        
            multiValueMap.add("iptStartPositionId", userId);
	        FormParameter formParameter = this.operationService.saveDraft(userId, "1", "", businessKey, bpmProcessId, multiValueMap);
	        ProcessDTO processDto = processConnector.findProcess(bpmProcessId);
	        String processDefinitionId = processDto.getProcessDefinitionId();
	        Record record = keyValueConnector.findByCode(formParameter.getBusinessKey());
	        businessKey = formParameter.getBusinessKey();

	        //用于更新areaId和detailHtml
	        Map<String, Object> modelMap = new HashMap<String, Object>();
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
	        modelMap.put("newmobile", onLineInfo.getNewname());
	        modelMap.put("newcardid", onLineInfo.getNewidentity());
	        modelMap.put("agent", onLineInfo.getBankcode());
	        modelMap.put("busDetailId", businessDetailEntity.getId().toString());
	        //modelMap.put("shoplicense",onLineInfo.getCompleteremark());
            modelMap.put("shoplicense", onLineInfo.getShopLicense());
            modelMap.put("enterprisename", onLineInfo.getEnterpriseName());
            modelMap.put("legaler", onLineInfo.getLegaler());
            modelMap.put("legaleridcard", onLineInfo.getLegalerIdCard());
            modelMap.put("distributorphone", onLineInfo.getDistributorPhone());
            modelMap.put("scopebusiness", onLineInfo.getScopeBusiness());
            modelMap.put("note", onLineInfo.getNote());
            modelMap.put("publicaccount", onLineInfo.getPublicAccount());
            modelMap.put("accountType", onLineInfo.getAccountType());
            modelMap.put("openingBank", onLineInfo.getOpeningBank());
            modelMap.put("openingName", onLineInfo.getOpeningName());
            modelMap.put("accountNumber", onLineInfo.getAccountNumbr());
            modelMap.put("storeArea", onLineInfo.getStoreArea());

	        String tenanId = tenantHolder.getTenantId();

	        //证件图
            modelMap.put("applypic", UploadPicAndGetPicHtml(onLineInfo.getApplypic(), tenanId, onLineInfo.getId().toString()));
	        modelMap.put("newpic", UploadPicAndGetPicHtml(onLineInfo.getNewpic(), tenanId, onLineInfo.getId().toString()));
	        modelMap.put("part1pic", UploadPicAndGetPicHtml(onLineInfo.getPartpic1(), tenanId, onLineInfo.getId().toString()));
	        modelMap.put("part2pic", UploadPicAndGetPicHtml(onLineInfo.getPartpic2(), tenanId, onLineInfo.getId().toString()));
	        modelMap.put("part3pic", UploadPicAndGetPicHtml(onLineInfo.getPartpic3(), tenanId, onLineInfo.getId().toString()));
	        modelMap.put("relativespic", UploadPicAndGetPicHtml(onLineInfo.getRelativespic(), tenanId, onLineInfo.getId().toString()));

	        modelMap.put("applyreason", onLineInfo.getReason());
	        modelMap.put("buslicense", UploadPicAndGetPicHtml(onLineInfo.getNewpic(), tenanId, onLineInfo.getId().toString()));//营业执照编号
            modelMap.put("publicaccountpic", UploadPicAndGetPicHtml(onLineInfo.getPartpic1(), tenanId, onLineInfo.getId().toString()));//对公许可证
	        modelMap.put("address", onLineInfo.getBankaddress());

	        modelMap.put("receivename", onLineInfo.getPartpic1());
	        modelMap.put("reveicemobile", onLineInfo.getPartpic2());
	        modelMap.put("authaddress", onLineInfo.getPartpic3());
	        
	        modelMap.put("locationBank", onLineInfo.getPartpic3());
	        
	        //是否同步修改新手机号
	        modelMap.put("synchPhone", onLineInfo.getPartpic3().equals("1")?"<font color='red'>(同时修改麦茂商城手机号)</font>":"");

	        //更新大区和分公司信息
            SetAreaCompanyRecord(modelMap, businessKey);

	        //为了发起流程，通过细分找到岗位，将岗位存入task_info的辅助表中 add by lilei at 2018.09.05
	        processParameters.put("positionId", PartyConstants.JXS_ID);
	        //发起流程
	        this.operationService.startProcessInstance(userId, businessKey,
	                processDefinitionId, processParameters, record);

            SaveHtmlForm(onLineInfo, modelMap, onLineInfo.getId().toString(), businessKey);
            
            if(!StringUtils.isBlank(businessKey)){
            	RecordInfo recordInfo=recordManager.get(Long.valueOf(businessKey));
                if(recordInfo!=null){
                	if(recordInfo.getRef()==null){
                		TaskInfo taskStart=taskInfoManager.findUnique("from TaskInfo where catalog='start' and businessKey=?", businessKey);
                		if(taskStart!=null){
                			recordInfo.setRef(taskStart.getProcessInstanceId());
                			taskInfoManager.save(recordInfo);
                		}
                	}
                }
            }

	        //处理受理单编号
	        operationService.deleteApplyCode(onLineInfo.getApplycode());
	        return true;
       } 
       catch (Exception ex) {
            logger.info("CreateApply(直销OA-添加申请)，出现异常：" + ex.getMessage() + "\r\n" + ex.getStackTrace());
			return false;
		}
	}

	/**
	 * 初始化密码表单
	 *      **/
    private void SaveHtmlForm(OnLineInfo model, Map<String, Object> modelMap, String pkId, String businessKey) {
        String strHtml = "";
        String strFormTitle = "在线办公申请";
		modelMap.put("formtitle", strFormTitle);
        /*************************************
         *
         * 注意注意：model.getApplytype()
         * 第一：8、10、12几个html模板是一样的
         * 第二：9、11、13几个html模板是一样的
         *
         * ***********************************/
        switch (model.getApplytype()) {
			case "1":   //密码初始化
				//strHtml = "<table class=\"centerdiv\" style=\"line-height: 34px; border: 1px solid;\" cellpadding=\"0\" cellspacing=\"0\"><tr><td colspan=\"4\" align=\"center\" class=\"f_td\"><h2>{formtitle}</h2></td></tr><tr><td width=\"100px\" class=\"f_td\"><span id=\"tag_olCode\">受理编号</span>：</td><td class=\"f_r_td\">{olcode}</td><td style=\"width: 100px\" class=\"f_td\"><span id=\"tag_applyCode\">申请人编号</span>：</td><td class=\"f_r_td\">{applycode}</td></tr><tr><td class=\"f_td\"><span id=\"tag_depart\">所属部门</span>：</td><td class=\"f_r_td\">{depart}</td><td class=\"f_td\"><span id=\"tag_applyName\">申请人姓名</span>：</td><td class=\"f_r_td\">{applyname}</td></tr><tr><td class=\"f_td\"><span id=\"tag_cardID\">申请人证件号</span>：</td><td class=\"f_r_td\">{cardid}</td><td class=\"f_td\"><span id=\"tag_welfare\">申请人福利级别</span>：</td><td class=\"f_r_td\">{welfare}</td></tr><tr><td class=\"f_td\"><span id=\"tag_mobile\">短信接收号码</span>：</td><td class=\"f_r_td\">{mobile}</td><td class=\"f_td\"><span id=\"tag_agent\">代理人编号</span>：</td><td class=\"f_r_td\">{agent}</td></tr><tr><td class=\"f_td\"><span id=\"tag_bustype\">申请业务类型</span>：</td><td class=\"f_r_td\">即时办理业务</td><td class=\"f_td\"><span id=\"tag_busDetails\">业务细分</span>：</td><td class=\"f_r_td\">密码初始化</td></tr><tr><td class=\"f_td\"><span id=\"tag_applyPic\">申请人证件</span>：</td><td class=\"f_r_td\">{applypic}</td><td class=\"f_td\"></td><td class=\"f_r_td\"></td></tr><tr><td colspan=\"4\" align=\"center\" class=\"f_td\">申请原因</td></tr><tr><td colspan=\"4\" style=\"height: 100px\" class=\"f_r_td\">{applyreason}</td></tr></table>";
				strHtml = "<table class=\"centerdiv\" style=\"line-height: 34px; border: 1px solid;\" cellpadding=\"0\" cellspacing=\"0\"><tr><td colspan=\"4\" align=\"center\" class=\"f_td\"><h2>{formtitle}</h2></td></tr><tr><td width=\"100px\" class=\"f_td\"><span id=\"tag_olCode\">受理编号</span>：</td><td class=\"f_r_td\">{olcode}</td><td style=\"width: 100px\" class=\"f_td\"><span id=\"tag_applyCode\">申请人编号</span>：</td><td class=\"f_r_td\">{applycode}</td></tr><tr><td class=\"f_td\">抄送：</td><td colspan=\"4\" class=\"f_r_td\">{copyNames}</td></tr><tr><td class=\"f_td\"><span id=\"tag_depart\">所属部门</span>：</td><td class=\"f_r_td\">{depart}</td><td class=\"f_td\"><span id=\"tag_applyName\">申请人姓名</span>：</td><td class=\"f_r_td\">{applyname}</td></tr><tr><td class=\"f_td\"><span id=\"tag_cardID\">申请人证件号</span>：</td><td class=\"f_r_td\">{cardid}</td><td class=\"f_td\"><span id=\"tag_welfare\">申请人福利级别</span>：</td><td class=\"f_r_td\">{welfare}</td></tr><tr><td class=\"f_td\"><span id=\"tag_mobile\">短信接收号码</span>：</td><td class=\"f_r_td\">{mobile}</td><td class=\"f_td\"><span id=\"tag_agent\">代理人编号</span>：</td><td class=\"f_r_td\">{agent}</td></tr><tr><td class=\"f_td\"><span id=\"tag_bustype\">申请业务类型</span>：</td><td class=\"f_r_td\">即时办理业务</td><td class=\"f_td\"><span id=\"tag_busDetails\">业务细分</span>：</td><td class=\"f_r_td\">密码初始化</td></tr><tr><td class=\"f_td\"><span id=\"tag_applyPic\">申请人证件</span>：</td><td class=\"f_r_td\">{applypic}</td><td class=\"f_td\"></td><td class=\"f_r_td\"></td></tr><tr><td colspan=\"4\" align=\"center\" class=\"f_td\">申请原因</td></tr><tr><td colspan=\"4\" style=\"height: 100px\" class=\"f_r_td\">{applyreason}</td></tr></table>";
				break;
			case "2":   //更正姓名
				//strHtml = "<table class=\"centerdiv\" style=\"line-height: 34px; border: 1px solid;\" cellpadding=\"0\" cellspacing=\"0\"><tr><td colspan=\"4\" align=\"center\" class=\"f_td\"><h2>{formtitle}</h2></td></tr><tr><td width=\"100px\" class=\"f_td\"><span id=\"tag_olCode\">受理编号</span>：</td><td class=\"f_r_td\">{olcode}</td><td style=\"width: 100px\" class=\"f_td\"><span id=\"tag_applyCode\">申请人编号</span>：</td><td class=\"f_r_td\">{applycode}</td></tr><tr><td class=\"f_td\"><span id=\"tag_depart\">所属部门</span>：</td><td class=\"f_r_td\">{depart}</td><td class=\"f_td\"><span id=\"tag_applyName\">申请人姓名</span>：</td><td class=\"f_r_td\">{applyname}</td></tr><tr><td class=\"f_td\"><span id=\"tag_cardID\">申请人证件号</span>：</td><td class=\"f_r_td\">{cardid}</td><td class=\"f_td\"><span id=\"tag_newName\">新姓名</span>：</td><td class=\"f_r_td\">{newname}</td></tr><tr><td class=\"f_td\"><span id=\"tag_welfare\">申请人福利级别</span>：</td><td class=\"f_r_td\">{welfare}</td><td class=\"f_td\"></td><td class=\"f_r_td\"></td></tr><tr><td class=\"f_td\"><span id=\"tag_bustype\">申请业务类型</span>：</td><td class=\"f_r_td\">即时办理业务</td><td class=\"f_td\"><span id=\"tag_busDetails\">业务细分</span>：</td><td class=\"f_r_td\">更正姓名</td></tr><tr><td class=\"f_td\"><span id=\"tag_applyPic\">申请人证件</span>：</td><td class=\"f_r_td\">{applypic}</td><td class=\"f_td\"></td><td class=\"f_r_td\"></td></tr><tr><td colspan=\"4\" align=\"center\" class=\"f_td\">申请原因</td></tr><tr><td colspan=\"4\" style=\"height: 100px\" class=\"f_r_td\">{applyreason}</td></tr></table>";
				strHtml = "<table class=\"centerdiv\" style=\"line-height: 34px; border: 1px solid;\" cellpadding=\"0\" cellspacing=\"0\"><tr><td colspan=\"4\" align=\"center\" class=\"f_td\"><h2>{formtitle}</h2></td></tr><tr><td width=\"100px\" class=\"f_td\"><span id=\"tag_olCode\">受理编号</span>：</td><td class=\"f_r_td\">{olcode}</td><td style=\"width: 100px\" class=\"f_td\"><span id=\"tag_applyCode\">申请人编号</span>：</td><td class=\"f_r_td\">{applycode}</td></tr><tr><td class=\"f_td\">抄送：</td><td colspan=\"4\" class=\"f_r_td\">{copyNames}</td></tr><tr><td class=\"f_td\"><span id=\"tag_depart\">所属部门</span>：</td><td class=\"f_r_td\">{depart}</td><td class=\"f_td\"><span id=\"tag_applyName\">申请人姓名</span>：</td><td class=\"f_r_td\">{applyname}</td></tr><tr><td class=\"f_td\"><span id=\"tag_cardID\">申请人证件号</span>：</td><td class=\"f_r_td\">{cardid}</td><td class=\"f_td\"><span id=\"tag_newName\">新姓名</span>：</td><td class=\"f_r_td\">{newname}</td></tr><tr><td class=\"f_td\"><span id=\"tag_welfare\">申请人福利级别</span>：</td><td class=\"f_r_td\">{welfare}</td><td class=\"f_td\"></td><td class=\"f_r_td\"></td></tr><tr><td class=\"f_td\"><span id=\"tag_bustype\">申请业务类型</span>：</td><td class=\"f_r_td\">即时办理业务</td><td class=\"f_td\"><span id=\"tag_busDetails\">业务细分</span>：</td><td class=\"f_r_td\">更正姓名</td></tr><tr><td class=\"f_td\"><span id=\"tag_applyPic\">申请人证件</span>：</td><td class=\"f_r_td\">{applypic}</td><td class=\"f_td\"></td><td class=\"f_r_td\"></td></tr><tr><td colspan=\"4\" align=\"center\" class=\"f_td\">申请原因</td></tr><tr><td colspan=\"4\" style=\"height: 100px\" class=\"f_r_td\">{applyreason}</td></tr></table>";
				break;
			case "3":   //身份证更正
				//strHtml = "<table class=\"centerdiv\" style=\"line-height: 34px; border: 1px solid;\" cellpadding=\"0\" cellspacing=\"0\"><tr><td colspan=\"4\" align=\"center\" class=\"f_td\"><h2>{formtitle}</h2></td></tr><tr><td width=\"100px\" class=\"f_td\"><span id=\"tag_olCode\">受理编号</span>：</td><td class=\"f_r_td\">{olcode}</td><td style=\"width: 100px\" class=\"f_td\"><span id=\"tag_applyCode\">申请人编号</span>：</td><td class=\"f_r_td\">{applycode}</td></tr><tr><td class=\"f_td\"><span id=\"tag_depart\">所属部门</span>：</td><td class=\"f_r_td\">{depart}</td><td class=\"f_td\"><span id=\"tag_applyName\">申请人姓名</span>：</td><td class=\"f_r_td\">{applyname}</td></tr><tr><td class=\"f_td\"><span id=\"tag_cardID\">申请人证件号</span>：</td><td class=\"f_r_td\">{cardid}</td><td class=\"f_td\"><span id=\"tag_newCardID\">新证件号</span>：</td><td class=\"f_r_td\">{newcardid}</td></tr><tr><td class=\"f_td\"><span id=\"tag_welfare\">申请人福利级别</span>：</td><td class=\"f_r_td\">{welfare}</td><td class=\"f_td\"></td><td class=\"f_r_td\"></td></tr><tr><td class=\"f_td\"><span id=\"tag_bustype\">申请业务类型</span>：</td><td class=\"f_r_td\">即时办理业务</td><td class=\"f_td\"><span id=\"tag_busDetails\">业务细分</span>：</td><td class=\"f_r_td\">身份证更正</td></tr><tr><td class=\"f_td\"><span id=\"tag_applyPic\">申请人证件</span>：</td><td class=\"f_r_td\">{applypic}</td><td class=\"f_td\"></td><td class=\"f_r_td\"></td></tr><tr><td colspan=\"4\" align=\"center\" class=\"f_td\">申请原因</td></tr><tr><td colspan=\"4\" style=\"height: 100px\" class=\"f_r_td\">{applyreason}</td></tr></table>";
				strHtml = "<table class=\"centerdiv\" style=\"line-height: 34px; border: 1px solid;\" cellpadding=\"0\" cellspacing=\"0\"><tr><td colspan=\"4\" align=\"center\" class=\"f_td\"><h2>{formtitle}</h2></td></tr><tr><td width=\"100px\" class=\"f_td\"><span id=\"tag_olCode\">受理编号</span>：</td><td class=\"f_r_td\">{olcode}</td><td style=\"width: 100px\" class=\"f_td\"><span id=\"tag_applyCode\">申请人编号</span>：</td><td class=\"f_r_td\">{applycode}</td></tr><tr><td class=\"f_td\">抄送：</td><td colspan=\"4\" class=\"f_r_td\">{copyNames}</td></tr><tr><td class=\"f_td\"><span id=\"tag_depart\">所属部门</span>：</td><td class=\"f_r_td\">{depart}</td><td class=\"f_td\"><span id=\"tag_applyName\">申请人姓名</span>：</td><td class=\"f_r_td\">{applyname}</td></tr><tr><td class=\"f_td\"><span id=\"tag_cardID\">申请人证件号</span>：</td><td class=\"f_r_td\">{cardid}</td><td class=\"f_td\"><span id=\"tag_newCardID\">新证件号</span>：</td><td class=\"f_r_td\">{newcardid}</td></tr><tr><td class=\"f_td\"><span id=\"tag_welfare\">申请人福利级别</span>：</td><td class=\"f_r_td\">{welfare}</td><td class=\"f_td\"></td><td class=\"f_r_td\"></td></tr><tr><td class=\"f_td\"><span id=\"tag_bustype\">申请业务类型</span>：</td><td class=\"f_r_td\">即时办理业务</td><td class=\"f_td\"><span id=\"tag_busDetails\">业务细分</span>：</td><td class=\"f_r_td\">身份证更正</td></tr><tr><td class=\"f_td\"><span id=\"tag_applyPic\">申请人证件</span>：</td><td class=\"f_r_td\">{applypic}</td><td class=\"f_td\"></td><td class=\"f_r_td\"></td></tr><tr><td colspan=\"4\" align=\"center\" class=\"f_td\">申请原因</td></tr><tr><td colspan=\"4\" style=\"height: 100px\" class=\"f_r_td\">{applyreason}</td></tr></table>";
				break;
			case "4":   //资格注销
				//strHtml = "<table class=\"centerdiv\" style=\"line-height: 34px; border: 1px solid;\" cellpadding=\"0\" cellspacing=\"0\"><tr><td colspan=\"4\" align=\"center\" class=\"f_td\"><h2>{formtitle}</h2></td></tr><tr><td width=\"100px\" class=\"f_td\"><span id=\"tag_olCode\">受理编号</span>：</td><td class=\"f_r_td\">{olcode}</td><td style=\"width: 100px\" class=\"f_td\"><span id=\"tag_applyCode\">申请人编号</span>：</td><td class=\"f_r_td\">{applycode}</td></tr><tr><td class=\"f_td\"><span id=\"tag_depart\">所属部门</span>：</td><td class=\"f_r_td\">{depart}</td><td class=\"f_td\"><span id=\"tag_applyName\">申请人姓名</span>：</td><td class=\"f_r_td\">{applyname}</td></tr><tr><td class=\"f_td\"><span id=\"tag_cardID\">申请人证件号</span>：</td><td class=\"f_r_td\">{cardid}</td><td class=\"f_td\"><span id=\"tag_welfare\">申请人福利级别</span>：</td><td class=\"f_r_td\">{welfare}</td></tr><tr><td class=\"f_td\"><span id=\"tag_busType\">申请业务类型</span>：</td><td class=\"f_r_td\">即时办理业务</td><td class=\"f_td\"><span id=\"tag_busDetails\">业务细分</span>：</td><td class=\"f_r_td\">资格注销</td></tr><tr><td class=\"f_td\"><span id=\"tag_applyPic\">申请人证件</span>：</td><td class=\"f_r_td\">{applypic}</td><td class=\"f_td\"></td><td class=\"f_r_td\"></td></tr><tr><td colspan=\"4\" align=\"center\" class=\"f_td\">申请原因</td></tr><tr><td colspan=\"4\" style=\"height: 100px\" class=\"f_r_td\">{applyreason}</td></tr></table>";
				strHtml = "<table class=\"centerdiv\" style=\"line-height: 34px; border: 1px solid;\" cellpadding=\"0\" cellspacing=\"0\"><tr><td colspan=\"4\" align=\"center\" class=\"f_td\"><h2>{formtitle}</h2></td></tr><tr><td width=\"100px\" class=\"f_td\"><span id=\"tag_olCode\">受理编号</span>：</td><td class=\"f_r_td\">{olcode}</td><td style=\"width: 100px\" class=\"f_td\"><span id=\"tag_applyCode\">申请人编号</span>：</td><td class=\"f_r_td\">{applycode}</td></tr><tr><td class=\"f_td\">抄送：</td><td colspan=\"4\" class=\"f_r_td\">{copyNames}</td></tr><tr><td class=\"f_td\"><span id=\"tag_depart\">所属部门</span>：</td><td class=\"f_r_td\">{depart}</td><td class=\"f_td\"><span id=\"tag_applyName\">申请人姓名</span>：</td><td class=\"f_r_td\">{applyname}</td></tr><tr><td class=\"f_td\"><span id=\"tag_cardID\">申请人证件号</span>：</td><td class=\"f_r_td\">{cardid}</td><td class=\"f_td\"><span id=\"tag_welfare\">申请人福利级别</span>：</td><td class=\"f_r_td\">{welfare}</td></tr><tr><td class=\"f_td\"><span id=\"tag_busType\">申请业务类型</span>：</td><td class=\"f_r_td\">即时办理业务</td><td class=\"f_td\"><span id=\"tag_busDetails\">业务细分</span>：</td><td class=\"f_r_td\">资格注销</td></tr><tr><td class=\"f_td\"><span id=\"tag_applyPic\">申请人证件</span>：</td><td class=\"f_r_td\">{applypic}</td><td class=\"f_td\"></td><td class=\"f_r_td\"></td></tr><tr><td colspan=\"4\" align=\"center\" class=\"f_td\">申请原因</td></tr><tr><td colspan=\"4\" style=\"height: 100px\" class=\"f_r_td\">{applyreason}</td></tr></table>";
				break;
			case "5":   //直系亲属资格替换
				//strHtml = "<table class=\"centerdiv\" style=\"line-height: 34px; border: 1px solid;\" cellpadding=\"0\" cellspacing=\"0\"><tr><td colspan=\"4\" align=\"center\" class=\"f_td\"><h2>{formtitle}</h2></td></tr><tr><td width=\"100px\" class=\"f_td\"><span id=\"tag_olCode\">受理编号</span>：</td><td class=\"f_r_td\">{olcode}</td><td style=\"width: 100px\" class=\"f_td\"><span id=\"tag_applyCode\">申请人编号</span>：</td><td class=\"f_r_td\">{applycode}</td></tr><tr><td class=\"f_td\"><span id=\"tag_depart\">所属部门</span>：</td><td class=\"f_r_td\">{depart}</td><td class=\"f_td\"><span id=\"tag_applyName\">申请人姓名</span>：</td><td class=\"f_r_td\">{applyname}</td></tr><tr><td class=\"f_td\"><span id=\"tag_cardID\">申请人证件号</span>：</td><td class=\"f_r_td\">{cardid}</td><td class=\"f_td\"><span id=\"tag_welfare\">申请人福利级别</span>：</td><td class=\"f_r_td\">{welfare}</td></tr><tr><td class=\"f_td\"><span id=\"tag_newName\">新姓名</span>：</td><td class=\"f_r_td\">{newname}</td><td class=\"f_td\"><span id=\"tag_newCardID\">新证件号</span>：</td><td class=\"f_r_td\">{newcardid}</td></tr><tr><td class=\"f_td\"><span id=\"tag_newBank\">新银行名称</span>：</td><td class=\"f_r_td\">{newbank}</td><td class=\"f_td\"><span id=\"tag_newAccount\">新银行账号</span>：</td><td class=\"f_r_td\">{newaccount}</td></tr><tr><td class=\"f_td\"><span id=\"tag_bankAddress\">新银行地址</span>：</td><td class=\"f_r_td\" colspan=\"3\">{bankaddress}</td></tr><tr><td class=\"f_td\"><span id=\"tag_bustype\">申请业务类型</span>：</td><td class=\"f_r_td\">即时办理业务</td><td class=\"f_td\"><span id=\"tag_busDetails\">业务细分</span>：</td><td class=\"f_r_td\">直系亲属资格替换</td></tr><tr><td class=\"f_td\"><span id=\"tag_applyPic\">申请人证件</span>：</td><td class=\"f_r_td\">{applypic}</td><td class=\"f_td\"><span id=\"tag_newPic\">新人证件</span>：</td><td class=\"f_r_td\">{newpic}</td></tr><tr><td class=\"f_td\"><span id=\"tag_part1Pic\">营销一分部证件</span>：</td><td class=\"f_r_td\">{part1pic}</td><td class=\"f_td\"><span id=\"tag_part2Pic\">营销二分部证件</span>：</td><td class=\"f_r_td\">{part2pic}</td></tr><tr><td class=\"f_td\"><span id=\"tag_part3Pic\">营销三分部证件</span>：</td><td class=\"f_r_td\">{part3pic}</td><td class=\"f_td\"><span id=\"tag_relativesPic\">亲属证明</span>：</td><td class=\"f_r_td\">{relativespic}</td></tr><tr><td colspan=\"4\" align=\"center\" class=\"f_td\">申请原因</td></tr><tr><td colspan=\"4\" style=\"height: 100px\" class=\"f_r_td\">{applyreason}</td></tr></table>";
				strHtml = "<table class=\"centerdiv\" style=\"line-height: 34px; border: 1px solid;\" cellpadding=\"0\" cellspacing=\"0\"><tr><td colspan=\"4\" align=\"center\" class=\"f_td\"><h2>{formtitle}</h2></td></tr><tr><td width=\"100px\" class=\"f_td\"><span id=\"tag_olCode\">受理编号</span>：</td><td class=\"f_r_td\">{olcode}</td><td style=\"width: 100px\" class=\"f_td\"><span id=\"tag_applyCode\">申请人编号</span>：</td><td class=\"f_r_td\">{applycode}</td></tr><tr><td class=\"f_td\">抄送：</td><td colspan=\"4\" class=\"f_r_td\">{copyNames}</td></tr><tr><td class=\"f_td\"><span id=\"tag_depart\">所属部门</span>：</td><td class=\"f_r_td\">{depart}</td><td class=\"f_td\"><span id=\"tag_applyName\">申请人姓名</span>：</td><td class=\"f_r_td\">{applyname}</td></tr><tr><td class=\"f_td\"><span id=\"tag_cardID\">申请人证件号</span>：</td><td class=\"f_r_td\">{cardid}</td><td class=\"f_td\"><span id=\"tag_welfare\">申请人福利级别</span>：</td><td class=\"f_r_td\">{welfare}</td></tr><tr><td class=\"f_td\"><span id=\"tag_newName\">新姓名</span>：</td><td class=\"f_r_td\">{newname}</td><td class=\"f_td\"><span id=\"tag_newCardID\">新证件号</span>：</td><td class=\"f_r_td\">{newcardid}</td></tr><tr><td class=\"f_td\"><span id=\"tag_newBank\">新银行名称</span>：</td><td class=\"f_r_td\">{newbank}</td><td class=\"f_td\"><span id=\"tag_newAccount\">新银行账号</span>：</td><td class=\"f_r_td\">{newaccount}</td></tr><tr><td class=\"f_td\"><span id=\"tag_bankAddress\">新银行地址</span>：</td><td class=\"f_r_td\" colspan=\"3\">{bankaddress}</td></tr><tr><td class=\"f_td\"><span id=\"tag_bustype\">申请业务类型</span>：</td><td class=\"f_r_td\">即时办理业务</td><td class=\"f_td\"><span id=\"tag_busDetails\">业务细分</span>：</td><td class=\"f_r_td\">直系亲属资格替换</td></tr><tr><td class=\"f_td\"><span id=\"tag_applyPic\">申请人证件</span>：</td><td class=\"f_r_td\">{applypic}</td><td class=\"f_td\"><span id=\"tag_newPic\">新人证件</span>：</td><td class=\"f_r_td\">{newpic}</td></tr><tr><td class=\"f_td\"><span id=\"tag_part1Pic\">营销一分部证件</span>：</td><td class=\"f_r_td\">{part1pic}</td><td class=\"f_td\"><span id=\"tag_part2Pic\">营销二分部证件</span>：</td><td class=\"f_r_td\">{part2pic}</td></tr><tr><td class=\"f_td\"><span id=\"tag_part3Pic\">营销三分部证件</span>：</td><td class=\"f_r_td\">{part3pic}</td><td class=\"f_td\"><span id=\"tag_relativesPic\">亲属证明</span>：</td><td class=\"f_r_td\">{relativespic}</td></tr><tr><td colspan=\"4\" align=\"center\" class=\"f_td\">申请原因</td></tr><tr><td colspan=\"4\" style=\"height: 100px\" class=\"f_r_td\">{applyreason}</td></tr></table>";
				break;
			case "6":   //更正手机号
				//strHtml = "<table class=\"centerdiv\" style=\"line-height: 34px; border: 1px solid;\" cellpadding=\"0\" cellspacing=\"0\"><tr><td colspan=\"4\" align=\"center\" class=\"f_td\"><h2>{formtitle}</h2></td></tr><tr><td width=\"100px\" class=\"f_td\"><span id=\"tag_olCode\">受理编号</span>：</td><td class=\"f_r_td\">{olcode}</td><td style=\"width: 100px\" class=\"f_td\"><span id=\"tag_applyCode\">申请人编号</span>：</td><td class=\"f_r_td\">{applycode}</td></tr><tr><td class=\"f_td\"><span id=\"tag_depart\">所属部门</span>：</td><td class=\"f_r_td\">{depart}</td><td class=\"f_td\"><span id=\"tag_applyName\">申请人姓名</span>：</td><td class=\"f_r_td\">{applyname}</td></tr><tr><td class=\"f_td\"><span id=\"tag_cardID\">申请人证件号</span>：</td><td class=\"f_r_td\">{cardid}</td><td class=\"f_td\"><span id=\"tag_welfare\">申请人福利级别</span>：</td><td class=\"f_r_td\">{welfare}</td></tr><tr><td class=\"f_td\"><span id=\"tag_mobile\">原手机号</span>：</td><td class=\"f_r_td\">{mobile}</td><td class=\"f_td\"><span id=\"tag_newMobile\">新手机号</span>：</td><td class=\"f_r_td\">{newmobile}</td></tr><tr><td class=\"f_td\"><span id=\"tag_bustype\">申请业务类型</span>：</td><td class=\"f_r_td\">即时办理业务</td><td class=\"f_td\"><span id=\"tag_busDetails\">业务细分</span>：</td><td class=\"f_r_td\">更正手机号</td></tr><tr><td class=\"f_td\"><span id=\"tag_applyPic\">申请人证件</span>：</td><td class=\"f_r_td\">{applypic}</td><td class=\"f_td\"></td><td class=\"f_r_td\"></td></tr><tr><td colspan=\"4\" align=\"center\" class=\"f_td\">申请原因</td></tr><tr><td colspan=\"4\" style=\"height: 100px\" class=\"f_r_td\">{applyreason}</td></tr></table>";
				strHtml = "<table class=\"centerdiv\" style=\"line-height: 34px; border: 1px solid;\" cellpadding=\"0\" cellspacing=\"0\"><tr><td colspan=\"4\" align=\"center\" class=\"f_td\"><h2>{formtitle}</h2></td></tr><tr><td width=\"100px\" class=\"f_td\"><span id=\"tag_olCode\">受理编号</span>：</td><td class=\"f_r_td\">{olcode}</td><td style=\"width: 100px\" class=\"f_td\"><span id=\"tag_applyCode\">申请人编号</span>：</td><td class=\"f_r_td\">{applycode}</td></tr><tr><td class=\"f_td\">抄送：</td><td colspan=\"4\" class=\"f_r_td\">{copyNames}</td></tr><tr><td class=\"f_td\"><span id=\"tag_depart\">所属部门</span>：</td><td class=\"f_r_td\">{depart}</td><td class=\"f_td\"><span id=\"tag_applyName\">申请人姓名</span>：</td><td class=\"f_r_td\">{applyname}</td></tr><tr><td class=\"f_td\"><span id=\"tag_cardID\">申请人证件号</span>：</td><td class=\"f_r_td\">{cardid}</td><td class=\"f_td\"><span id=\"tag_welfare\">申请人福利级别</span>：</td><td class=\"f_r_td\">{welfare}</td></tr><tr><td class=\"f_td\"><span id=\"tag_mobile\">原手机号</span>：</td><td class=\"f_r_td\">{mobile}</td><td class=\"f_td\"><span id=\"tag_newMobile\">新手机号</span>：</td><td class=\"f_r_td\">{newmobile}{synchPhone}</td></tr><tr><td class=\"f_td\"><span id=\"tag_bustype\">申请业务类型</span>：</td><td class=\"f_r_td\">即时办理业务</td><td class=\"f_td\"><span id=\"tag_busDetails\">业务细分</span>：</td><td class=\"f_r_td\">更正手机号</td></tr><tr><td class=\"f_td\"><span id=\"tag_applyPic\">申请人证件</span>：</td><td class=\"f_r_td\">{applypic}</td><td class=\"f_td\"></td><td class=\"f_r_td\"></td></tr><tr><td colspan=\"4\" align=\"center\" class=\"f_td\">申请原因</td></tr><tr><td colspan=\"4\" style=\"height: 100px\" class=\"f_r_td\">{applyreason}</td></tr></table>";
				break;
			case "7":   //开通订货及转账权限
				//strHtml = "<table class=\"centerdiv\" style=\"line-height: 34px; border: 1px solid;\" cellpadding=\"0\" cellspacing=\"0\"><tr><td colspan=\"4\" align=\"center\" class=\"f_td\"><h2>{formtitle}</h2></td></tr><tr><td width=\"100px\" class=\"f_td\"><span id=\"tag_olCode\">受理编号</span>：</td><td class=\"f_r_td\">{olcode}</td><td style=\"width: 100px\" class=\"f_td\"><span id=\"tag_applyCode\">申请人编号</span>：</td><td class=\"f_r_td\">{applycode}</td></tr><tr><td class=\"f_td\"><span id=\"tag_depart\">所属部门</span>：</td><td class=\"f_r_td\">{depart}</td><td class=\"f_td\"><span id=\"tag_applyName\">申请人姓名</span>：</td><td class=\"f_r_td\">{applyname}</td></tr><tr><td class=\"f_td\"><span id=\"tag_cardID\">申请人证件号</span>：</td><td class=\"f_r_td\">{cardid}</td><td class=\"f_td\"><span id=\"tag_welfare\">申请人福利级别</span>：</td><td class=\"f_r_td\">{welfare}</td></tr><tr><td class=\"f_td\"><span id=\"tag_bustype\">申请业务类型</span>：</td><td class=\"f_r_td\">即时办理业务</td><td class=\"f_td\"><span id=\"tag_busDetails\">业务细分</span>：</td><td class=\"f_r_td\">开通订货及转账权限</td></tr><tr><td class=\"f_td\"><span id=\"tag_applyPic\">申请人证件</span>：</td><td class=\"f_r_td\">{applypic}</td><td class=\"f_td\"></td><td class=\"f_r_td\"></td></tr><tr><td colspan=\"4\" align=\"center\" class=\"f_td\">申请原因</td></tr><tr><td colspan=\"4\" style=\"height: 100px\" class=\"f_r_td\">{applyreason}</td></tr></table>";
				strHtml = "<table class=\"centerdiv\" style=\"line-height: 34px; border: 1px solid;\" cellpadding=\"0\" cellspacing=\"0\"><tr><td colspan=\"4\" align=\"center\" class=\"f_td\"><h2>{formtitle}</h2></td></tr><tr><td width=\"100px\" class=\"f_td\"><span id=\"tag_olCode\">受理编号</span>：</td><td class=\"f_r_td\">{olcode}</td><td style=\"width: 100px\" class=\"f_td\"><span id=\"tag_applyCode\">申请人编号</span>：</td><td class=\"f_r_td\">{applycode}</td></tr><tr><td class=\"f_td\">抄送：</td><td colspan=\"4\" class=\"f_r_td\">{copyNames}</td></tr><tr><td class=\"f_td\"><span id=\"tag_depart\">所属部门</span>：</td><td class=\"f_r_td\">{depart}</td><td class=\"f_td\"><span id=\"tag_applyName\">申请人姓名</span>：</td><td class=\"f_r_td\">{applyname}</td></tr><tr><td class=\"f_td\"><span id=\"tag_cardID\">申请人证件号</span>：</td><td class=\"f_r_td\">{cardid}</td><td class=\"f_td\"><span id=\"tag_welfare\">申请人福利级别</span>：</td><td class=\"f_r_td\">{welfare}</td></tr><tr><td class=\"f_td\"><span id=\"tag_bustype\">申请业务类型</span>：</td><td class=\"f_r_td\">即时办理业务</td><td class=\"f_td\"><span id=\"tag_busDetails\">业务细分</span>：</td><td class=\"f_r_td\">开通订货及转账权限</td></tr><tr><td class=\"f_td\"><span id=\"tag_applyPic\">申请人证件</span>：</td><td class=\"f_r_td\">{applypic}</td><td class=\"f_td\"></td><td class=\"f_r_td\"></td></tr><tr><td colspan=\"4\" align=\"center\" class=\"f_td\">申请原因</td></tr><tr><td colspan=\"4\" style=\"height: 100px\" class=\"f_r_td\">{applyreason}</td></tr></table>";
				break;
			case "8":   //12万旗舰店申请
				//strHtml = "<table class=\"centerdiv\" style=\"line-height: 34px; border: 1px solid;\" cellpadding=\"0\" cellspacing=\"0\"><tr><td colspan=\"4\" align=\"center\" class=\"f_td\"><h2>{formtitle}</h2></td></tr><tr><td width=\"100px\" class=\"f_td\"><span id=\"tag_olCode\">受理编号</span>：</td><td class=\"f_r_td\">{olcode}</td><td style=\"width: 100px\" class=\"f_td\"><span id=\"tag_applyCode\">申请人编号</span>：</td><td class=\"f_r_td\">{applycode}</td></tr><tr><td class=\"f_td\"><span id=\"tag_depart\">所属部门</span>：</td><td class=\"f_r_td\">{depart}</td><td class=\"f_td\"><span id=\"tag_applyName\">申请人姓名</span>：</td><td class=\"f_r_td\">{applyname}</td></tr><tr><td class=\"f_td\"><span id=\"tag_cardID\">申请人证件号</span>：</td><td class=\"f_r_td\">{cardid}</td><td class=\"f_td\"><span id=\"tag_welfare\">申请人福利级别</span>：</td><td class=\"f_r_td\">{welfare}</td></tr><tr><td class=\"f_td\"><span id=\"tag_address\">旗舰店地址</span>：</td><td class=\"f_r_td\" colspan=\"3\">{address}</td></tr><tr><td class=\"f_td\"><span id=\"tag_address\">营业执照注册号</span>：</td><td class=\"f_r_td\" colspan=\"3\">{shoplicense}</td></tr>"
						//+ "<tr><td class=\"f_td\"><span id=\"tag_receiveName\">收件人姓名</span>：</td><td class=\"f_r_td\">{receivename}</td><td class=\"f_td\"><span id=\"tag_receiveMobile\">收件人电话</span>：</td><td class=\"f_r_td\">{reveicemobile}</td></tr><tr><td class=\"f_td\"><span id=\"tag_authAddress\">授权书邮寄地址</span>：</td><td class=\"f_r_td\" colspan=\"3\">{authaddress}</td></tr>"
						//+ "<tr><td class=\"f_td\"><span id=\"tag_bustype\">申请业务类型</span>：</td><td class=\"f_r_td\">即时办理业务</td><td class=\"f_td\"><span id=\"tag_busDetails\">业务细分</span>：</td><td class=\"f_r_td\">12万旗舰店申请</td></tr><tr><td class=\"f_td\"><span id=\"tag_applyPic\">申请人证件</span>：</td><td class=\"f_r_td\" colspan=\"3\">{applypic}</td></tr><tr><td class=\"f_td\"><span id=\"tag_busLicense\">营业执照</span>：</td><td class=\"f_r_td\" colspan=\"3\">{buslicense}</td></tr><tr><td colspan=\"4\" align=\"center\" class=\"f_td\">申请原因</td></tr><tr><td colspan=\"4\" style=\"height: 100px\" class=\"f_r_td\">{applyreason}</td></tr></table>";
						//+ "<tr><td class=\"f_td\"><span id=\"tag_bustype\">申请业务类型</span>：</td><td class=\"f_r_td\">即时办理业务</td><td class=\"f_td\"><span id=\"tag_busDetails\">业务细分</span>：</td><td class=\"f_r_td\">12万旗舰店申请</td></tr><tr><td class=\"f_td\"><span id=\"tag_applyPic\">申请人证件</span>：</td><td class=\"f_r_td\" colspan=\"3\">{applypic}</td></tr><tr><td class=\"f_td\"><span id=\"tag_busLicense\">营业执照</span>：</td><td class=\"f_r_td\" colspan=\"3\">{buslicense}</td></tr><tr><td colspan=\"4\" align=\"center\" class=\"f_td\">申请原因</td></tr><tr><td colspan=\"4\" style=\"height: 100px\" class=\"f_r_td\">{applyreason}</td></tr></table>";*/
                strHtml = "<table class=\"centerdiv\" style=\"line-height: 34px; border: 1px solid;\" cellpadding=\"0\" cellspacing=\"0\"><tr><td colspan=\"4\" align=\"center\" class=\"f_td\"><h2>{formtitle}</h2></td></tr><tr><td width=\"100px\" class=\"f_td\"><span id=\"tag_olCode\">受理编号</span>：</td><td class=\"f_r_td\">{olcode}</td><td style=\"width: 100px\" class=\"f_td\"><span id=\"tag_applyCode\">申请人编号</span>：</td><td class=\"f_r_td\">{applycode}</td></tr><tr><td class=\"f_td\">抄送：</td><td colspan=\"4\" class=\"f_r_td\">{copyNames}</td></tr><tr><td class=\"f_td\"><span id=\"tag_depart\">所属部门</span>：</td><td class=\"f_r_td\">{depart}</td><td class=\"f_td\"><span id=\"tag_applyName\">申请人姓名</span>：</td><td class=\"f_r_td\">{applyname}</td></tr><tr><td class=\"f_td\"><span id=\"tag_cardID\">申请人证件号</span>：</td><td class=\"f_r_td\">{cardid}</td><td class=\"f_td\"><span id=\"tag_welfare\">申请人福利级别</span>：</td><td class=\"f_r_td\">{welfare}</td></tr><tr><td class=\"f_td\"><span id=\"tag_address\">旗舰店地址</span>：</td><td class=\"f_r_td\" colspan=\"3\">{address}</td></tr><tr><td class=\"f_td\"><span id=\"tag_storeArea\">实体店面积</span>：</td><td class=\"f_r_td\" colspan=\"3\">{storeArea}m<sup>2</sup></td></tr><tr><td class=\"f_td\"><span id=\"tag_address\">统一社会信用代码</span>：</td><td class=\"f_r_td\" colspan=\"3\">{shoplicense}</td></tr><tr><td class=\"f_td\"><span id=\"tag_address\">企业名称</span>：</td><td class=\"f_r_td\" colspan=\"3\">{enterprisename}</td></tr><tr><td class=\"f_td\"><span id=\"tag_bustype\">法定代表人</span>：</td><td class=\"f_r_td\">{legaler}</td><td class=\"f_td\"><span id=\"tag_busDetails\">联系电话</span>：</td><td class=\"f_r_td\">{distributorphone}</td></tr><tr><td class=\"f_td\"><span id=\"tag_address\">法人身份证号码</span>：</td><td class=\"f_r_td\" colspan=\"3\">{legaleridcard}</td></tr><tr><td class=\"f_td\"><span id=\"tag_address\">经营范围</span>：</td><td class=\"f_r_td\" colspan=\"3\">{scopebusiness}</td></tr><tr><td class=\"f_td\"><span id=\"tag_address\">备注</span>：</td><td class=\"f_r_td\" colspan=\"3\">{note}</td></tr><tr><td class=\"f_td\"><span id=\"tag_bustype\">申请业务类型</span>：</td><td class=\"f_r_td\">即时办理业务</td><td class=\"f_td\"><span id=\"tag_busDetails\">业务细分</span>：</td><td class=\"f_r_td\">12万旗舰店申请</td></tr><tr><td class=\"f_td\"><span id=\"tag_applyPic\">申请人证件</span>：</td><td class=\"f_r_td\" colspan=\"3\">{applypic}</td></tr><tr><td class=\"f_td\"><span id=\"tag_busLicense\">营业执照</span>：</td><td class=\"f_r_td\" colspan=\"3\">{buslicense}</td></tr><tr><td colspan=\"4\" align=\"center\" class=\"f_td\">申请原因</td></tr><tr><td colspan=\"4\" style=\"height: 100px\" class=\"f_r_td\">{applyreason}</td></tr></table>";
				break;
			case "9":   //非12万旗舰店申请
				//strHtml = "<table class=\"centerdiv\" style=\"line-height: 34px; border: 1px solid;\" cellpadding=\"0\" cellspacing=\"0\"><tr><td colspan=\"4\" align=\"center\" class=\"f_td\"><h2>{formtitle}</h2></td></tr><tr><td width=\"100px\" class=\"f_td\"><span id=\"tag_olCode\">受理编号</span>：</td><td class=\"f_r_td\">{olcode}</td><td style=\"width: 100px\" class=\"f_td\"><span id=\"tag_applyCode\">申请人编号</span>：</td><td class=\"f_r_td\">{applycode}</td></tr><tr><td class=\"f_td\"><span id=\"tag_depart\">所属部门</span>：</td><td class=\"f_r_td\">{depart}</td><td class=\"f_td\"><span id=\"tag_applyName\">申请人姓名</span>：</td><td class=\"f_r_td\">{applyname}</td></tr><tr><td class=\"f_td\"><span id=\"tag_cardID\">申请人证件号</span>：</td><td class=\"f_r_td\">{cardid}</td><td class=\"f_td\"><span id=\"tag_welfare\">申请人福利级别</span>：</td><td class=\"f_r_td\">{welfare}</td></tr><tr><td class=\"f_td\"><span id=\"tag_address\">旗舰店地址</span>：</td><td class=\"f_r_td\" colspan=\"3\">{address}</td></tr><tr><td class=\"f_td\"><span id=\"tag_address\">营业执照注册号</span>：</td><td class=\"f_r_td\" colspan=\"3\">{shoplicense}</td></tr><tr><td class=\"f_td\"><span id=\"tag_bustype\">申请业务类型</span>：</td><td class=\"f_r_td\">即时办理业务</td><td class=\"f_td\"><span id=\"tag_busDetails\">业务细分</span>：</td><td class=\"f_r_td\">非12万旗舰店申请</td></tr><tr><td colspan=\"4\" align=\"center\" class=\"f_td\">申请原因</td></tr><tr><td colspan=\"4\" style=\"height: 100px\" class=\"f_r_td\">{applyreason}</td></tr></table>";
                  //strHtml = "<table class=\"centerdiv\" style=\"line-height: 34px; border: 1px solid;\" cellpadding=\"0\" cellspacing=\"0\"><tr><td colspan=\"4\" align=\"center\" class=\"f_td\"><h2>{formtitle}</h2></td></tr><tr><td width=\"100px\" class=\"f_td\"><span id=\"tag_olCode\">受理编号</span>：</td><td class=\"f_r_td\">{olcode}</td><td style=\"width: 100px\" class=\"f_td\"><span id=\"tag_applyCode\">申请人编号</span>：</td><td class=\"f_r_td\">{applycode}</td></tr><tr><td class=\"f_td\">抄送：</td><td colspan=\"4\" class=\"f_r_td\">{copyNames}</td></tr><tr><td class=\"f_td\"><span id=\"tag_depart\">所属部门</span>：</td><td class=\"f_r_td\">{depart}</td><td class=\"f_td\"><span id=\"tag_applyName\">申请人姓名</span>：</td><td class=\"f_r_td\">{applyname}</td></tr><tr><td class=\"f_td\"><span id=\"tag_cardID\">申请人证件号</span>：</td><td class=\"f_r_td\">{cardid}</td><td class=\"f_td\"><span id=\"tag_welfare\">申请人福利级别</span>：</td><td class=\"f_r_td\">{welfare}</td></tr><tr><td class=\"f_td\"><span id=\"tag_address\">旗舰店地址</span>：</td><td class=\"f_r_td\" colspan=\"3\">{address}</td></tr><tr><td class=\"f_td\"><span id=\"tag_address\">营业执照注册号</span>：</td><td class=\"f_r_td\" colspan=\"3\">{shoplicense}</td></tr><tr><td class=\"f_td\"><span id=\"tag_bustype\">申请业务类型</span>：</td><td class=\"f_r_td\">即时办理业务</td><td class=\"f_td\"><span id=\"tag_busDetails\">业务细分</span>：</td><td class=\"f_r_td\">非12万旗舰店申请</td></tr><tr><td colspan=\"4\" align=\"center\" class=\"f_td\">申请原因</td></tr><tr><td colspan=\"4\" style=\"height: 100px\" class=\"f_r_td\">{applyreason}</td></tr></table>";
                  strHtml = "<table class=\"centerdiv\" style=\"line-height: 34px; border: 1px solid;\" cellpadding=\"0\" cellspacing=\"0\"><tr><td colspan=\"4\" align=\"center\" class=\"f_td\"><h2>{formtitle}</h2></td></tr><tr><td width=\"100px\" class=\"f_td\"><span id=\"tag_olCode\">受理编号</span>：</td><td class=\"f_r_td\">{olcode}</td><td style=\"width: 100px\" class=\"f_td\"><span id=\"tag_applyCode\">申请人编号</span>：</td><td class=\"f_r_td\">{applycode}</td></tr><tr><td class=\"f_td\">抄送：</td><td colspan=\"4\" class=\"f_r_td\">{copyNames}</td></tr><tr><td class=\"f_td\"><span id=\"tag_depart\">所属部门</span>：</td><td class=\"f_r_td\">{depart}</td><td class=\"f_td\"><span id=\"tag_applyName\">申请人姓名</span>：</td><td class=\"f_r_td\">{applyname}</td></tr><tr><td class=\"f_td\"><span id=\"tag_cardID\">申请人证件号</span>：</td><td class=\"f_r_td\">{cardid}</td><td class=\"f_td\"><span id=\"tag_welfare\">申请人福利级别</span>：</td><td class=\"f_r_td\">{welfare}</td></tr><tr><td class=\"f_td\"><span id=\"tag_address\">旗舰店地址</span>：</td><td class=\"f_r_td\" colspan=\"3\">{address}</td></tr><tr><td class=\"f_td\"><span id=\"tag_storeArea\">实体店面积</span>：</td><td class=\"f_r_td\" colspan=\"3\">{storeArea}m<sup>2</sup></td></tr><tr><td class=\"f_td\"><span id=\"tag_address\">统一社会信用代码</span>：</td><td class=\"f_r_td\" colspan=\"3\">{shoplicense}</td></tr><tr><td class=\"f_td\"><span id=\"tag_address\">企业名称</span>：</td><td class=\"f_r_td\" colspan=\"3\">{enterprisename}</td></tr><tr><td class=\"f_td\"><span id=\"tag_bustype\">法定代表人</span>：</td><td class=\"f_r_td\">{legaler}</td><td class=\"f_td\"><span id=\"tag_busDetails\">联系电话</span>：</td><td class=\"f_r_td\">{distributorphone}</td></tr><tr><td class=\"f_td\"><span id=\"tag_address\">法人身份证号码</span>：</td><td class=\"f_r_td\" colspan=\"3\">{legaleridcard}</td></tr><tr><td class=\"f_td\"><span id=\"tag_address\">经营范围</span>：</td><td class=\"f_r_td\" colspan=\"3\">{scopebusiness}</td></tr><tr><td class=\"f_td\"><span id=\"tag_address\">备注</span>：</td><td class=\"f_r_td\" colspan=\"3\">{note}</td></tr><tr><td class=\"f_td\"><span id=\"tag_bustype\">申请业务类型</span>：</td><td class=\"f_r_td\">即时办理业务</td><td class=\"f_td\"><span id=\"tag_busDetails\">业务细分</span>：</td><td class=\"f_r_td\">非12万旗舰店申请</td></tr><tr><td colspan=\"4\" align=\"center\" class=\"f_td\">申请原因</td></tr><tr><td colspan=\"4\" style=\"height: 100px\" class=\"f_r_td\">{applyreason}</td></tr></table>";
                  break;
			case "10":   //12万申请续约
				//strHtml = "<table class=\"centerdiv\" style=\"line-height: 34px; border: 1px solid;\" cellpadding=\"0\" cellspacing=\"0\"><tr><td colspan=\"4\" align=\"center\" class=\"f_td\"><h2>{formtitle}</h2></td></tr><tr><td width=\"100px\" class=\"f_td\"><span id=\"tag_olCode\">受理编号</span>：</td><td class=\"f_r_td\">{olcode}</td><td style=\"width: 100px\" class=\"f_td\"><span id=\"tag_applyCode\">申请人编号</span>：</td><td class=\"f_r_td\">{applycode}</td></tr><tr><td class=\"f_td\"><span id=\"tag_depart\">所属部门</span>：</td><td class=\"f_r_td\">{depart}</td><td class=\"f_td\"><span id=\"tag_applyName\">申请人姓名</span>：</td><td class=\"f_r_td\">{applyname}</td></tr><tr><td class=\"f_td\"><span id=\"tag_cardID\">申请人证件号</span>：</td><td class=\"f_r_td\">{cardid}</td><td class=\"f_td\"><span id=\"tag_welfare\">申请人福利级别</span>：</td><td class=\"f_r_td\">{welfare}</td></tr><tr><td class=\"f_td\"><span id=\"tag_address\">旗舰店地址</span>：</td><td class=\"f_r_td\" colspan=\"3\">{address}</td></tr><tr><td class=\"f_td\"><span id=\"tag_address\">营业执照注册号</span>：</td><td class=\"f_r_td\" colspan=\"3\">{shoplicense}</td></tr>"
				//+ "<tr><td class=\"f_td\"><span id=\"tag_bustype\">申请业务类型</span>：</td><td class=\"f_r_td\">即时办理业务</td><td class=\"f_td\"><span id=\"tag_busDetails\">业务细分</span>：</td><td class=\"f_r_td\">12万旗舰店申请续约</td></tr><tr><td class=\"f_td\"><span id=\"tag_applyPic\">申请人证件</span>：</td><td class=\"f_r_td\" colspan=\"3\">{applypic}</td></tr><tr><td class=\"f_td\"><span id=\"tag_busLicense\">营业执照</span>：</td><td class=\"f_r_td\" colspan=\"3\">{buslicense}</td></tr><tr><td colspan=\"4\" align=\"center\" class=\"f_td\">申请原因</td></tr><tr><td colspan=\"4\" style=\"height: 100px\" class=\"f_r_td\">{applyreason}</td></tr></table>";
                strHtml = "<table class=\"centerdiv\" style=\"line-height: 34px; border: 1px solid;\" cellpadding=\"0\" cellspacing=\"0\"><tr><td colspan=\"4\" align=\"center\" class=\"f_td\"><h2>{formtitle}</h2></td></tr><tr><td width=\"100px\" class=\"f_td\"><span id=\"tag_olCode\">受理编号</span>：</td><td class=\"f_r_td\">{olcode}</td><td style=\"width: 100px\" class=\"f_td\"><span id=\"tag_applyCode\">申请人编号</span>：</td><td class=\"f_r_td\">{applycode}</td></tr><tr><td class=\"f_td\">抄送：</td><td colspan=\"4\" class=\"f_r_td\">{copyNames}</td></tr><tr><td class=\"f_td\"><span id=\"tag_depart\">所属部门</span>：</td><td class=\"f_r_td\">{depart}</td><td class=\"f_td\"><span id=\"tag_applyName\">申请人姓名</span>：</td><td class=\"f_r_td\">{applyname}</td></tr><tr><td class=\"f_td\"><span id=\"tag_cardID\">申请人证件号</span>：</td><td class=\"f_r_td\">{cardid}</td><td class=\"f_td\"><span id=\"tag_welfare\">申请人福利级别</span>：</td><td class=\"f_r_td\">{welfare}</td></tr><tr><td class=\"f_td\"><span id=\"tag_address\">旗舰店地址</span>：</td><td class=\"f_r_td\" colspan=\"3\">{address}</td></tr><tr><td class=\"f_td\"><span id=\"tag_storeArea\">实体店面积</span>：</td><td class=\"f_r_td\" colspan=\"3\">{storeArea}m<sup>2</sup></td></tr><tr><td class=\"f_td\"><span id=\"tag_storeArea\">实体店面积</span>：</td><td class=\"f_r_td\" colspan=\"3\">240平</td></tr><tr><td class=\"f_td\"><span id=\"tag_address\">统一社会信用代码</span>：</td><td class=\"f_r_td\" colspan=\"3\">{shoplicense}</td></tr><tr><td class=\"f_td\"><span id=\"tag_address\">企业名称</span>：</td><td class=\"f_r_td\" colspan=\"3\">{enterprisename}</td></tr><tr><td class=\"f_td\"><span id=\"tag_bustype\">法定代表人</span>：</td><td class=\"f_r_td\">{legaler}</td><td class=\"f_td\"><span id=\"tag_busDetails\">联系电话</span>：</td><td class=\"f_r_td\">{distributorphone}</td></tr><tr><td class=\"f_td\"><span id=\"tag_address\">法人身份证号码</span>：</td><td class=\"f_r_td\" colspan=\"3\">{legaleridcard}</td></tr><tr><td class=\"f_td\"><span id=\"tag_address\">经营范围</span>：</td><td class=\"f_r_td\" colspan=\"3\">{scopebusiness}</td></tr><tr><td class=\"f_td\"><span id=\"tag_address\">备注</span>：</td><td class=\"f_r_td\" colspan=\"3\">{note}</td></tr><tr><td class=\"f_td\"><span id=\"tag_bustype\">申请业务类型</span>：</td><td class=\"f_r_td\">即时办理业务</td><td class=\"f_td\"><span id=\"tag_busDetails\">业务细分</span>：</td><td class=\"f_r_td\">12万旗舰店申请续约</td></tr><tr><td class=\"f_td\"><span id=\"tag_applyPic\">申请人证件</span>：</td><td class=\"f_r_td\" colspan=\"3\">{applypic}</td></tr><tr><td class=\"f_td\"><span id=\"tag_busLicense\">营业执照</span>：</td><td class=\"f_r_td\" colspan=\"3\">{buslicense}</td></tr><tr><td colspan=\"4\" align=\"center\" class=\"f_td\">申请原因</td></tr><tr><td colspan=\"4\" style=\"height: 100px\" class=\"f_r_td\">{applyreason}</td></tr></table>";				
                break;
			case "11":   //非12万旗舰店申请续约
				//strHtml = "<table class=\"centerdiv\" style=\"line-height: 34px; border: 1px solid;\" cellpadding=\"0\" cellspacing=\"0\"><tr><td colspan=\"4\" align=\"center\" class=\"f_td\"><h2>{formtitle}</h2></td></tr><tr><td width=\"100px\" class=\"f_td\"><span id=\"tag_olCode\">受理编号</span>：</td><td class=\"f_r_td\">{olcode}</td><td style=\"width: 100px\" class=\"f_td\"><span id=\"tag_applyCode\">申请人编号</span>：</td><td class=\"f_r_td\">{applycode}</td></tr><tr><td class=\"f_td\"><span id=\"tag_depart\">所属部门</span>：</td><td class=\"f_r_td\">{depart}</td><td class=\"f_td\"><span id=\"tag_applyName\">申请人姓名</span>：</td><td class=\"f_r_td\">{applyname}</td></tr><tr><td class=\"f_td\"><span id=\"tag_cardID\">申请人证件号</span>：</td><td class=\"f_r_td\">{cardid}</td><td class=\"f_td\"><span id=\"tag_welfare\">申请人福利级别</span>：</td><td class=\"f_r_td\">{welfare}</td></tr><tr><td class=\"f_td\"><span id=\"tag_address\">旗舰店地址</span>：</td><td class=\"f_r_td\" colspan=\"3\">{address}</td></tr><tr><td class=\"f_td\"><span id=\"tag_address\">营业执照注册号</span>：</td><td class=\"f_r_td\" colspan=\"3\">{shoplicense}</td></tr><tr><td class=\"f_td\"><span id=\"tag_bustype\">申请业务类型</span>：</td><td class=\"f_r_td\">即时办理业务</td><td class=\"f_td\"><span id=\"tag_busDetails\">业务细分</span>：</td><td class=\"f_r_td\">非12万旗舰店申请续约</td></tr><tr><td colspan=\"4\" align=\"center\" class=\"f_td\">申请原因</td></tr><tr><td colspan=\"4\" style=\"height: 100px\" class=\"f_r_td\">{applyreason}</td></tr></table>";
                strHtml = "<table class=\"centerdiv\" style=\"line-height: 34px; border: 1px solid;\" cellpadding=\"0\" cellspacing=\"0\"><tr><td colspan=\"4\" align=\"center\" class=\"f_td\"><h2>{formtitle}</h2></td></tr><tr><td width=\"100px\" class=\"f_td\"><span id=\"tag_olCode\">受理编号</span>：</td><td class=\"f_r_td\">{olcode}</td><td style=\"width: 100px\" class=\"f_td\"><span id=\"tag_applyCode\">申请人编号</span>：</td><td class=\"f_r_td\">{applycode}</td></tr><tr><td class=\"f_td\">抄送：</td><td colspan=\"4\" class=\"f_r_td\">{copyNames}</td></tr><tr><td class=\"f_td\"><span id=\"tag_depart\">所属部门</span>：</td><td class=\"f_r_td\">{depart}</td><td class=\"f_td\"><span id=\"tag_applyName\">申请人姓名</span>：</td><td class=\"f_r_td\">{applyname}</td></tr><tr><td class=\"f_td\"><span id=\"tag_cardID\">申请人证件号</span>：</td><td class=\"f_r_td\">{cardid}</td><td class=\"f_td\"><span id=\"tag_welfare\">申请人福利级别</span>：</td><td class=\"f_r_td\">{welfare}</td></tr><tr><td class=\"f_td\"><span id=\"tag_address\">旗舰店地址</span>：</td><td class=\"f_r_td\" colspan=\"3\">{address}</td></tr><tr><td class=\"f_td\"><span id=\"tag_storeArea\">实体店面积</span>：</td><td class=\"f_r_td\" colspan=\"3\">{storeArea}m<sup>2</sup></td></tr><tr><td class=\"f_td\"><span id=\"tag_address\">统一社会信用代码</span>：</td><td class=\"f_r_td\" colspan=\"3\">{shoplicense}</td></tr><tr><td class=\"f_td\"><span id=\"tag_address\">企业名称</span>：</td><td class=\"f_r_td\" colspan=\"3\">{enterprisename}</td></tr><tr><td class=\"f_td\"><span id=\"tag_bustype\">法定代表人</span>：</td><td class=\"f_r_td\">{legaler}</td><td class=\"f_td\"><span id=\"tag_busDetails\">联系电话</span>：</td><td class=\"f_r_td\">{distributorphone}</td></tr><tr><td class=\"f_td\"><span id=\"tag_address\">法人身份证号码</span>：</td><td class=\"f_r_td\" colspan=\"3\">{legaleridcard}</td></tr><tr><td class=\"f_td\"><span id=\"tag_address\">经营范围</span>：</td><td class=\"f_r_td\" colspan=\"3\">{scopebusiness}</td></tr><tr><td class=\"f_td\"><span id=\"tag_address\">备注</span>：</td><td class=\"f_r_td\" colspan=\"3\">{note}</td></tr><tr><td class=\"f_td\"><span id=\"tag_bustype\">申请业务类型</span>：</td><td class=\"f_r_td\">即时办理业务</td><td class=\"f_td\"><span id=\"tag_busDetails\">业务细分</span>：</td><td class=\"f_r_td\">非12万旗舰店申请续约</td></tr><tr><td colspan=\"4\" align=\"center\" class=\"f_td\">申请原因</td></tr><tr><td colspan=\"4\" style=\"height: 100px\" class=\"f_r_td\">{applyreason}</td></tr></table>";	
                break;
            case "12":   //12万申请协议
                strHtml = "<table class=\"centerdiv\" style=\"line-height: 34px; border: 1px solid;\" cellpadding=\"0\" cellspacing=\"0\"><tr><td colspan=\"4\" align=\"center\" class=\"f_td\"><h2>{formtitle}</h2></td></tr><tr><td width=\"100px\" class=\"f_td\"><span id=\"tag_olCode\">受理编号</span>：</td><td class=\"f_r_td\">{olcode}</td><td style=\"width: 100px\" class=\"f_td\"><span id=\"tag_applyCode\">申请人编号</span>：</td><td class=\"f_r_td\">{applycode}</td></tr><tr><td class=\"f_td\">抄送：</td><td colspan=\"4\" class=\"f_r_td\">{copyNames}</td></tr><tr><td class=\"f_td\"><span id=\"tag_depart\">所属部门</span>：</td><td class=\"f_r_td\">{depart}</td><td class=\"f_td\"><span id=\"tag_applyName\">申请人姓名</span>：</td><td class=\"f_r_td\">{applyname}</td></tr><tr><td class=\"f_td\"><span id=\"tag_cardID\">申请人证件号</span>：</td><td class=\"f_r_td\">{cardid}</td><td class=\"f_td\"><span id=\"tag_welfare\">申请人福利级别</span>：</td><td class=\"f_r_td\">{welfare}</td></tr><tr><td class=\"f_td\"><span id=\"tag_address\">旗舰店地址</span>：</td><td class=\"f_r_td\" colspan=\"3\">{address}</td></tr><tr><td class=\"f_td\"><span id=\"tag_storeArea\">实体店面积</span>：</td><td class=\"f_r_td\" colspan=\"3\">{storeArea}m<sup>2</sup></td></tr><tr><td class=\"f_td\"><span id=\"tag_address\">统一社会信用代码</span>：</td><td class=\"f_r_td\" colspan=\"3\">{shoplicense}</td></tr><tr><td class=\"f_td\"><span id=\"tag_address\">企业名称</span>：</td><td class=\"f_r_td\" colspan=\"3\">{enterprisename}</td></tr><tr><td class=\"f_td\"><span id=\"tag_bustype\">法定代表人</span>：</td><td class=\"f_r_td\">{legaler}</td><td class=\"f_td\"><span id=\"tag_busDetails\">联系电话</span>：</td><td class=\"f_r_td\">{distributorphone}</td></tr><tr><td class=\"f_td\"><span id=\"tag_address\">法人身份证号码</span>：</td><td class=\"f_r_td\" colspan=\"3\">{legaleridcard}</td></tr><tr><td class=\"f_td\"><span id=\"tag_address\">经营范围</span>：</td><td class=\"f_r_td\" colspan=\"3\">{scopebusiness}</td></tr><tr><td class=\"f_td\"><span id=\"tag_address\">备注</span>：</td><td class=\"f_r_td\" colspan=\"3\">{note}</td></tr><tr><td class=\"f_td\"><span id=\"tag_bustype\">申请业务类型</span>：</td><td class=\"f_r_td\">即时办理业务</td><td class=\"f_td\"><span id=\"tag_busDetails\">业务细分</span>：</td><td class=\"f_r_td\">12万旗舰店协议申请</td></tr><tr><td class=\"f_td\"><span id=\"tag_applyPic\">申请人证件</span>：</td><td class=\"f_r_td\" colspan=\"3\">{applypic}</td></tr><tr><td class=\"f_td\"><span id=\"tag_busLicense\">营业执照</span>：</td><td class=\"f_r_td\" colspan=\"3\">{buslicense}</td></tr><tr><td colspan=\"4\" align=\"center\" class=\"f_td\">申请原因</td></tr><tr><td colspan=\"4\" style=\"height: 100px\" class=\"f_r_td\">{applyreason}</td></tr></table>";
                break;

            case "13":   //非12万旗舰店协议
                strHtml = "<table class=\"centerdiv\" style=\"line-height: 34px; border: 1px solid;\" cellpadding=\"0\" cellspacing=\"0\"><tr><td colspan=\"4\" align=\"center\" class=\"f_td\"><h2>{formtitle}</h2></td></tr><tr><td width=\"100px\" class=\"f_td\"><span id=\"tag_olCode\">受理编号</span>：</td><td class=\"f_r_td\">{olcode}</td><td style=\"width: 100px\" class=\"f_td\"><span id=\"tag_applyCode\">申请人编号</span>：</td><td class=\"f_r_td\">{applycode}</td></tr><tr><td class=\"f_td\">抄送：</td><td colspan=\"4\" class=\"f_r_td\">{copyNames}</td></tr><tr><td class=\"f_td\"><span id=\"tag_depart\">所属部门</span>：</td><td class=\"f_r_td\">{depart}</td><td class=\"f_td\"><span id=\"tag_applyName\">申请人姓名</span>：</td><td class=\"f_r_td\">{applyname}</td></tr><tr><td class=\"f_td\"><span id=\"tag_cardID\">申请人证件号</span>：</td><td class=\"f_r_td\">{cardid}</td><td class=\"f_td\"><span id=\"tag_welfare\">申请人福利级别</span>：</td><td class=\"f_r_td\">{welfare}</td></tr><tr><td class=\"f_td\"><span id=\"tag_address\">旗舰店地址</span>：</td><td class=\"f_r_td\" colspan=\"3\">{address}</td></tr><tr><td class=\"f_td\"><span id=\"tag_storeArea\">实体店面积</span>：</td><td class=\"f_r_td\" colspan=\"3\">{storeArea}m<sup>2</sup></td></tr><tr><td class=\"f_td\"><span id=\"tag_address\">统一社会信用代码</span>：</td><td class=\"f_r_td\" colspan=\"3\">{shoplicense}</td></tr><tr><td class=\"f_td\"><span id=\"tag_address\">企业名称</span>：</td><td class=\"f_r_td\" colspan=\"3\">{enterprisename}</td></tr><tr><td class=\"f_td\"><span id=\"tag_bustype\">法定代表人</span>：</td><td class=\"f_r_td\">{legaler}</td><td class=\"f_td\"><span id=\"tag_busDetails\">联系电话</span>：</td><td class=\"f_r_td\">{distributorphone}</td></tr><tr><td class=\"f_td\"><span id=\"tag_address\">法人身份证号码</span>：</td><td class=\"f_r_td\" colspan=\"3\">{legaleridcard}</td></tr><tr><td class=\"f_td\"><span id=\"tag_address\">经营范围</span>：</td><td class=\"f_r_td\" colspan=\"3\">{scopebusiness}</td></tr><tr><td class=\"f_td\"><span id=\"tag_address\">备注</span>：</td><td class=\"f_r_td\" colspan=\"3\">{note}</td></tr><tr><td class=\"f_td\"><span id=\"tag_bustype\">申请业务类型</span>：</td><td class=\"f_r_td\">即时办理业务</td><td class=\"f_td\"><span id=\"tag_busDetails\">业务细分</span>：</td><td class=\"f_r_td\">非12万旗舰店协议申请</td></tr><tr><td colspan=\"4\" align=\"center\" class=\"f_td\">申请原因</td></tr><tr><td colspan=\"4\" style=\"height: 100px\" class=\"f_r_td\">{applyreason}</td></tr></table>";
                break;
            case "14":   //代理商协议
                strHtml = "<table class=\"centerdiv\" style=\"line-height: 34px; border: 1px solid;\" cellpadding=\"0\" cellspacing=\"0\"><tr><td colspan=\"4\" align=\"center\" class=\"f_td\"><h2>{formtitle}</h2></td></tr><tr><td width=\"100px\" class=\"f_td\"><span id=\"tag_olCode\">受理编号</span>：</td><td class=\"f_r_td\">{olcode}</td><td style=\"width: 100px\" class=\"f_td\"><span id=\"tag_applyCode\">申请人编号</span>：</td><td class=\"f_r_td\">{applycode}</td></tr><tr><td class=\"f_td\">抄送：</td><td colspan=\"4\" class=\"f_r_td\">{copyNames}</td></tr><tr><td class=\"f_td\"><span id=\"tag_depart\">所属部门</span>：</td><td class=\"f_r_td\">{depart}</td><td class=\"f_td\"><span id=\"tag_applyName\">申请人姓名</span>：</td><td class=\"f_r_td\">{applyname}</td></tr><tr><td class=\"f_td\"><span id=\"tag_cardID\">申请人证件号</span>：</td><td class=\"f_r_td\">{cardid}</td><td class=\"f_td\"><span id=\"tag_welfare\">申请人福利级别</span>：</td><td class=\"f_r_td\">{welfare}</td></tr><tr><td class=\"f_td\"><span id=\"tag_address\">注册地址</span>：</td><td class=\"f_r_td\" colspan=\"3\">{address}</td></tr><tr><td class=\"f_td\"><span id=\"tag_address\">统一社会信用代码</span>：</td><td class=\"f_r_td\" colspan=\"3\">{shoplicense}</td></tr><tr><td class=\"f_td\"><span id=\"tag_address\">企业名称</span>：</td><td class=\"f_r_td\" colspan=\"3\">{enterprisename}</td></tr><tr><td class=\"f_td\"><span id=\"tag_bustype\">法定代表人</span>：</td><td class=\"f_r_td\">{legaler}</td><td class=\"f_td\"><span id=\"tag_busDetails\">联系电话</span>：</td><td class=\"f_r_td\">{distributorphone}</td></tr><tr><td class=\"f_td\"><span id=\"tag_address\">法人身份证号码</span>：</td><td class=\"f_r_td\" colspan=\"3\">{legaleridcard}</td></tr><tr><td class=\"f_td\"><span id=\"tag_address\">经营范围</span>：</td><td class=\"f_r_td\" colspan=\"3\">{scopebusiness}</td></tr><tr><td class=\"f_td\"><span id=\"tag_address\">代理区域</span>：</td><td class=\"f_r_td\">{note}</td><td class=\"f_td\"><span id=\"tag_accountType\">企业性质</span>：</td><td class=\"f_r_td\">{accountType}</td></tr><tr><tr><td class=\"f_td\"><span id=\"tag_openingBank\">开户行</span>：</td><td class=\"f_r_td\">{openingBank}</td><td class=\"f_td\"><span id=\"tag_openingName\">开户名</span>：</td><td class=\"f_r_td\">{openingName}</td></tr><td class=\"f_td\"><span id=\"tag_address\">开户行行号</span>：</td><td class=\"f_r_td\" colspan=\"3\">{publicaccount}</td></tr><tr><td class=\"f_td\"><span id=\"tag_address\">银行账号</span>：</td><td class=\"f_r_td\" colspan=\"3\">{accountNumber}</td></tr><tr><td class=\"f_td\"><span id=\"tag_address\">开户行所在地</span>：</td><td class=\"f_r_td\" colspan=\"3\">{locationBank}</td></tr><tr><td class=\"f_td\"><span id=\"tag_bustype\">申请业务类型</span>：</td><td class=\"f_r_td\">即时办理业务</td><td class=\"f_td\"><span id=\"tag_busDetails\">业务细分</span>：</td><td class=\"f_r_td\">市场推广合作协议资料申请</td></tr><tr><td class=\"f_td\"><span id=\"tag_applyPic\">申请人证件</span>：</td><td class=\"f_r_td\" colspan=\"3\">{applypic}</td></tr><tr><td class=\"f_td\"><span id=\"tag_busLicense\">营业执照</span>：</td><td class=\"f_r_td\" colspan=\"3\">{buslicense}</td></tr><tr><td class=\"f_td\"><span id=\"tag_busLicense\">对公许可证</span>：</td><td class=\"f_r_td\" colspan=\"3\">{publicaccountpic}</td></tr><tr><td colspan=\"4\" align=\"center\" class=\"f_td\">申请原因</td></tr><tr><td colspan=\"4\" style=\"height: 100px\" class=\"f_r_td\">{applyreason}</td></tr></table>";
                break;
			default:
				strHtml = "";
				break;
		}
        SaveHtml(strHtml, modelMap, pkId, businessKey);

		String sql = "from CodeEntity where code=? and shortName =? and receiptNumber=?";
	
        String strAppType = model.getApplytype();
        if (strAppType.equals("9"))
            strAppType = "8";
        else if (strAppType.equals("11"))
            strAppType = "10";
        else if (strAppType.equals("13"))
            strAppType = "12";
		/*if((",9,10,11,").contains(","+strAppType+","))
			strAppType="8";*/
		//String strAppType=model.getApplytype().equals("9")?"8":model.getApplytype();
        CodeEntity codeEntity = codeManager.findUnique(sql, Integer.valueOf(strAppType), _ShorName, model.getApplycode());
        if (codeEntity != null) {
            codeEntity.setUserID(codeEntity.getUserID() + "_use");
        	codeManager.save(codeEntity);
        }
	}

	/**
	 * 将图片路径保存，并且拼接html
	//private String UploadPicAndGetPicHtml(Object strPics,String tenanId,String pkId)
	//{
	//	StringBuffer sbPicsBuffer=new StringBuffer("");
    //    if(strPics!=null&&strPics.toString().split(",").length>0){
    //    	for(String pic:strPics.toString().split(",")){
    //			String filename=pic.substring(pic.lastIndexOf("/")+1, pic.length());
    //    		
    // **/
    private String UploadPicAndGetPicHtml(Object strPics, String tenanId, String pkId) {
        StringBuffer sbPicsBuffer = new StringBuffer("");
        if (strPics != null && strPics.toString().split(",").length > 0) {
            for (String pic : strPics.toString().split(",")) {
                String filename = pic.substring(pic.lastIndexOf("/") + 1, pic.length());

    			try {
					storeConnector.saveStore("rolmexoa/pic", filename, tenanId, pkId, pic);
				} catch (Exception e) {
					e.printStackTrace();
				}
                String picUrl = webAPI.getViewUrl() + "/" + pic;
                sbPicsBuffer.append("<a href=\"" + picUrl + "\" target=\"_blank\">"
                        + "<img src=\"" + picUrl + "\""
        				+ " style=\"margin-top: 7px; margin-bottom: 7px;\" width=\"84\" height=\"74\""
        				+ " border=\"0\" alt=\"\" class=\"photo\"/></a>");
        	}
        }
        return sbPicsBuffer.toString();
	}

	/**
	 * 替换字符串得到html,并且保存更新KV
	 * 
     **/
    private void SaveHtml(String detailHtml, Map<String, Object> modelMap, String pkId, String bussinessKey) {
        for (String key : modelMap.keySet()) {
            String strContent = (modelMap.get(key) == null ? "" : modelMap.get(key).toString().replace("'", ""));
            if (key.equals("address") || key.equals("bankaddress") || key.equals("authaddress"))
                strContent = strContent.replace(",", "");
            detailHtml = detailHtml.replace("{" + key + "}", strContent);
		}

		/*String sqlRecordUpdate = "update KV_RECORD set detailHtml= '" + detailHtml
               + "',pk_id ='" + pkId
				+ "' where BUSINESS_KEY= '" + bussinessKey + "'";
    	keyValueConnector.updateBySql(sqlRecordUpdate);
		return;*/
        RecordInfo recordInfo=recordManager.findUniqueBy("businessKey", bussinessKey);
    	if(recordInfo != null){
    		recordInfo.setPkId(pkId);
    		recordInfo.setDetailHtml(detailHtml);
    	}
       	recordManager.save(recordInfo);
	}

    private void SetAreaCompanyRecord(Map<String, Object> modelMap, String bussinessKey) {
        String strAddress = modelMap.get("bankaddress").toString().replace(",", "");
        String busDetailId = modelMap.get("busDetailId").toString();
        String areaId = "";
        String areaName = "";
        String companyId = "";
        String companyName = "";

        if ((busDetailId.equals("8") || busDetailId.equals("9")
                || busDetailId.equals("10") || busDetailId.equals("11")
                || busDetailId.equals("12") || busDetailId.equals("13")
                || busDetailId.equals("14"))
                && !strAddress.equals("")) {
            String subProvice = strAddress.substring(0, 2);
            String subCity = strAddress.substring(3, 5);

            if ((subProvice.equals("广东") && !subCity.equals("深圳")) || subProvice.equals("海南"))
                subProvice = "广东";
            else if (subProvice.equals("广东") && subCity.equals("深圳"))
            	subProvice = "广东";//edit by lilei at 2019-01-23 subProvice = "深圳";
            else if (subProvice.equals("青海") || subProvice.equals("宁夏")
                    || subProvice.equals("西藏") || subProvice.equals("新疆"))
                subProvice = "甘肃";
            else if (subProvice.equals("黑龙"))
                subProvice = "黑龙江";
            else if (subProvice.equals("内蒙"))
                subProvice = "内蒙古";

            String strSql = "SELECT `NAME`,ID FROM party_entity "
                    + "WHERE `NAME`='" + subProvice + "分公司' and DEL_FLAG<>'1' and `LEVEL`=4;";
            List<Map<String, Object>> companyMapList = jdbcTemplate.queryForList(strSql);
            if (companyMapList != null && companyMapList.size() > 0) {

                Map<String, Object> companyMap = companyMapList.get(0);
                companyId = companyMap.get("ID").toString();
                companyName = companyMap.get("NAME").toString();

                strSql = "SELECT e.ID,e.`NAME` FROM party_struct s"
                        + " INNER JOIN party_entity e on s.PARENT_ENTITY_ID=e.ID"
                        + " WHERE s.CHILD_ENTITY_ID=" + companyMap.get("ID");
                List<Map<String, Object>> areaMapList = jdbcTemplate.queryForList(strSql);
                if (areaMapList != null && areaMapList.size() > 0) {
                    Map<String, Object> areaMap = areaMapList.get(0);
                    areaId = areaMap.get("ID").toString();
                    areaName = areaMap.get("NAME").toString();
				}
			}
		}

    	//原jdbc更新record操作更改如下，更新的属性保持一致  TODO sjx 18.11.23
        RecordInfo recordInfo = recordManager.findUniqueBy("businessKey", bussinessKey);
        recordInfo.setAreaId(areaId);
        recordInfo.setAreaName(areaName);
        recordInfo.setCompanyId(companyId);
        recordInfo.setCompanyName(companyName);
    	recordManager.save(recordInfo);
	}
	//endregion

	/**
	 * 旗舰店申请发送短信
     *
	 * @author lilei
	 * @date 2017.12.28
     **/
	@Transactional(readOnly = false)
	public void SendMsg(){
        try {
			List<Map<String, Object>> mapTaskList= jdbcTemplate.queryForList("select * from time_task");
            String cooperKey = signInfo.getMessageCooperKey().trim();//"ceshi1";
            String strCooperName = signInfo.getMessageCooperName().trim();//"ceshi";

            if (mapTaskList != null && mapTaskList.size() > 0) {
                for (Map<String, Object> mapTask : mapTaskList) {
                    if (mapTask.get("taskType") != null && mapTask.get("taskType").toString().equals("sendmsg")) {
                        String strMsg = mapTask.get("taskContent").toString();
                        String strPhoneNo = mapTask.get("taskNote").toString();
                        String strEncryptMsg = strPhoneNo + "|" + cooperKey + "|" + strMsg;
                        strEncryptMsg = PasswordUtil.getMD5(strEncryptMsg);
						try {
							//String endPoint=signInfo.getMessageRequestUrl();//"http://202.70.12.18/SmsService.asmx?wsdl";
							//com.mossle.ws.sms.WebServiceSoapProxy sProxy=new com.mossle.ws.sms.WebServiceSoapProxy(endPoint);
                            String endPoint = signInfo.getMessageRequestUrl();//"http://202.70.12.18/SmsService.asmx?wsdl";
                            com.mossle.ws.sms.WebServiceSoapProxy sProxy = new com.mossle.ws.sms.WebServiceSoapProxy(endPoint);
							StringHolder strReturnDesc = new StringHolder();
							javax.xml.rpc.holders.IntHolder sendMsgResult = new javax.xml.rpc.holders.IntHolder();
							sProxy.sendMsg(strPhoneNo, strMsg, strEncryptMsg, strCooperName, sendMsgResult, strReturnDesc);
							//System.out.println("短信发完了: " + new Date());
							//处理完后，删除数据
							//jdbcTemplate.update("delete from time_task where id="+mapTask.get("id").toString());
                            jdbcTemplate.update("delete from time_task where id=" + mapTask.get("id").toString());
						} catch (RemoteException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
                    } else if (mapTask.get("taskType") != null
                            && (mapTask.get("taskType").toString().equals("serviceapplyshop")   //老接口type
                            || mapTask.get("taskType").toString().equals("applyshoprenew")		//老接口type
                            || mapTask.get("taskType").toString().equals("serviceshopapply")
                            || mapTask.get("taskType").toString().equals("shopapplyrenew")
                            || mapTask.get("taskType").toString().equals("serviceprotocol")
                            || mapTask.get("taskType").toString().equals("agentagreement"))) {
                        //审核结束，调用直销接口
                        String endPoint = signInfo.getOaApplyUrl();
                        com.mossle.ws.oaapply.ToOAServiceSoapProxy soapProxy = new com.mossle.ws.oaapply.ToOAServiceSoapProxy(endPoint);
                        //传回审核后申请资料数据
                        
                        Map map = new HashedMap();
                        String strUserID = "";            //经销商编号
                        String strName = "";                //姓名
                        String strAddress = "";           //旗舰店地址
                        String strCreditCode = "";        //统一信用代码
                        String strPicUrl = "";                //营业执照图片
                        String strType = "";                //单子类型
                        String strIdentity = "";                //证件号
                        String strEnterpriseName = "";       //企业名称
                        String strLegaler = "";                //法定代表人
                        String strLegalerIdCard="";
                        String strDistributorPhone = "";       //联系电话
                        String strScopeBusiness = "";         //经营范围
                        String strNote = "";                //备注

                        String strApplyCode = "";          //受理编号
                        String strReason = "";                //申请原因
                        String strPublicAccount = "";         //公户
                        String strAccountType = "";         //账户类型
                        String strOpeningBank = "";         //开户行
                        String strOpeningName = "";         //开户名
                        String strAccountNumber = "";         //账号
                        String strStoreArea = "";         //店面积
                        String locationBank="";			//开户行地址
                        //if (mapTask.get("taskContent").toString().contains("|")) {
                        if(mapTask.get("taskType").toString().equals("serviceapplyshop")
                          ||mapTask.get("taskType").toString().equals("applyshoprenew")){  
	                            //老数据处理
	                            String[] strMainParms = mapTask.get("taskContent").toString().split("\\|");
	                            strUserID = strMainParms[0];            //经销商编号
	                            strName = strMainParms[1];                //姓名
	                            strAddress = strMainParms[2];            //旗舰店气质
	                            strCreditCode = strMainParms[3];        //统一信用代码
	                            strPicUrl = strMainParms[4];                //营业执照图片
	                            strType = strMainParms[5];                //单子类型
	                        } else {
	                            //2018.07.30 接口修改
	                            map = jsonMapper.fromJson(mapTask.get("taskContent").toString(), Map.class);
	                            strUserID = map.get("ucode").toString();            			//经销商编号
	                            strName = map.get("name").toString();                			//姓名
	                            strAddress = map.get("bankaddress").toString();           		//旗舰店地址
	                            strCreditCode = map.get("shopLicense").toString();        		//统一信用代码
	                            strPicUrl = map.get("newpic").toString();                		//营业执照图片
	                            strType = map.get("applytype").toString();                		//单子类型
	                            strIdentity = map.get("identity").toString();              		//证件号
	                            strEnterpriseName = map.get("enterpriseName").toString();   	//企业名称
	                            strLegaler = map.get("legaler").toString();                 	//法定代表人
	                            strLegalerIdCard=map.get("legalericcard").toString();			//法人身份证号码
	                            strDistributorPhone = map.get("distributorPhone").toString();   //联系电话
	                            strScopeBusiness = map.get("scopeBusiness").toString();         //经营范围
	                            strNote = map.get("note").toString();                			//备注
	                            strApplyCode = map.get("applycode").toString();          		//受理编号
	                            strReason = map.get("reason").toString();                		//申请原因
	                            strPublicAccount = map.get("publicAccount").toString();         //公户
	                            strAccountType = map.get("accountType").toString();             //账户类型
	                            strOpeningBank = map.get("openingBank").toString();             //开户行
	                            if(map.containsKey("openingName")){
	                            	strOpeningName = map.get("openingName").toString();             //开户名
	                            }
	                            strAccountNumber = map.get("accountNumber").toString();         //账号
	                            strStoreArea = map.get("storeArea").toString();                 //店面积
	                            if(map.containsKey("locationBank"))
	                            	locationBank=map.get("locationBank").toString(); 
	                    }

                        String[] strParms = mapTask.get("taskNote").toString().split(",");
                        String strBranch = strParms[0];
                        boolean isSuccess = Boolean.parseBoolean(strParms[1]);
                        String strMsg = signInfo.getOaApplySignMsg();
                        strMsg = strUserID + strMsg;
                        strMsg = PasswordUtil.getMD5(strMsg);

                        String strReturn = "";
						
                        /*//timeTaskEndService.setTaskType("serviceapplyshop");//旗舰店申请-第一次调直销接口type名称
                    	timeTaskEndService.setTaskType("serviceshopapply");//旗舰店申请-第二次调直销接口type名称
	                    }
	                    else if (busiDetailId.equals("10") || busiDetailId.equals("11")){
                    	//timeTaskEndService.setTaskType("applyshoprenew");//旗舰店续约-第一次调直销接口type名称
                    	timeTaskEndService.setTaskType("shopapplyrenew");//旗舰店续约-第二次调直销接口type名称*/
						
                        //旗舰店申请
                        if (mapTask.get("taskType").toString().equals("serviceapplyshop")
                    		||mapTask.get("taskType").toString().equals("serviceshopapply")) {
                            int isCreateAuth = 0;
                            if (mapTask.get("taskType").toString().equals("serviceshopapply"))
                                isCreateAuth = Integer.parseInt(map.get("isAuthCertificate").toString());      //是否生成授权书
						
                            logger.info("调用直销接口：updateFlagShopApply" + ";strUserID:" + strUserID + ";isSuccess:" + isSuccess + ";strApplyCode:" + strApplyCode);
                            strReturn = soapProxy.updateFlagShopApply(strUserID,
                                    strBranch,
									isSuccess,
									strName,
									strAddress,
									strCreditCode,
                                    strPicUrl,
                                    strIdentity,
                                    strEnterpriseName,
                                    strLegaler,
                                    strLegalerIdCard,
                                    strDistributorPhone,
                                    strScopeBusiness,
                                    strNote,
                                    isCreateAuth,
									strType,
									strStoreArea,
									strMsg);
						}
                        //旗舰店续约申请
                        else if (mapTask.get("taskType").toString().equals("applyshoprenew")
                        		||mapTask.get("taskType").toString().equals("shopapplyrenew")) {
                            logger.info("调用直销接口：updateFlagRenew" + ";strUserID:" + strUserID + ";isSuccess:" + isSuccess + ";strApplyCode:" + strApplyCode);
                            strReturn = soapProxy.updateFlagRenew(strUserID,
                                    strBranch,
									strName,
									strAddress,
									strCreditCode,
                                    strPicUrl,
                                    strIdentity,
                                    strEnterpriseName,
                                    strLegaler,
                                    strLegalerIdCard,
                                    strDistributorPhone,
                                    strScopeBusiness,
                                    strNote,
									strType,
									strStoreArea,
									strMsg);
						}
                        //旗舰店协议资料申请
                        else if (mapTask.get("taskType").toString().equals("serviceprotocol")) {
                            logger.info("调用直销接口：updateFlagProtocol" + ";strUserID:" + strUserID + ";isSuccess:" + isSuccess + ";strApplyCode:" + strApplyCode);
                            strReturn = soapProxy.updateFlagProtocol(strUserID,
                                    strBranch,
                                    strName,
                                    strAddress,
                                    strCreditCode,
                                    strPicUrl,
                                    strIdentity,
                                    strEnterpriseName,
                                    strLegaler,
                                    strLegalerIdCard,
                                    strDistributorPhone,
                                    strScopeBusiness,
                                    strNote,
                                    strType,
                                    strStoreArea,
                                    strMsg);
					}
                    //代理商协议资料申请
                    else if (mapTask.get("taskType").toString().equals("agentagreement")) {
                        logger.info("调用直销接口：updateAgentProtocol" + ";strUserID:" + strUserID + ";isSuccess:" + isSuccess + ";strApplyCode:" + strApplyCode);
                        Map<String,Object> jsonMap=new HashMap<String, Object>();
                        jsonMap.put("locationBank", locationBank);
                        strReturn = soapProxy.updateAgentProtocol(strUserID,
                                strBranch,
                                strName,
                                strAddress,
                                strCreditCode,
                                strPicUrl,
                                strIdentity,
                                strEnterpriseName,
                                strLegaler,
                                strLegalerIdCard,
                                strDistributorPhone,
                                strScopeBusiness,
                                strNote,
                                strPublicAccount,
                                strAccountType,
                                strOpeningName,
                                strOpeningBank,
                                strAccountNumber,
                                jsonMapper.toJson(jsonMap),
                                strMsg);
				}
                logger.info("调用直销接口返回：strReturn=" + strReturn);
                if (strReturn.toLowerCase().equals("ok"))
                    jdbcTemplate.update("delete from time_task where id=" + mapTask.get("id").toString());
                else
                    logger.info(new Date() + "调用直销接口失败了，返回结果：" + strReturn);
			}
            else if(mapTask.get("taskType") != null
                    && (mapTask.get("taskType").toString().equals("personprobation"))){
            	try{
            		String probationPersonId=mapTask.get("taskContent")==null?"":mapTask.get("taskContent").toString();
            		//员工转正 add by lilei at 2018.11.22
            		if(!StringUtils.isBlank(probationPersonId)){
            			PersonInfo personInfo=personInfoManager.get(Long.parseLong(probationPersonId));
            			personInfo.setLaborType("1");
            			personInfoManager.save(personInfo);
            			
            			jdbcTemplate.update("delete from time_task where id=" + mapTask.get("id").toString());
            		}
            	}
            	catch(Exception ex){
            		throw new Exception(ex);
            	}
            }
		}
            }
        }catch (Exception ex) {
            System.out.println("调用任务出异常了" + ex.getMessage() + ": " + new Date());
        }
    }

	@Transactional(readOnly = false)
    public void SetOATimeTask(String businessKey, String humanTaskId) {
		try {
            RecordInfo recordNew = recordManager.findUniqueBy("businessKey", businessKey);
            String busiDetailId = recordNew.getBusinessDetailId();
            String auditStatus = recordNew.getAuditStatus();
            if ((",1,2,3,4,5,6,7,8,9,10,11,12,13,14,").contains("," + busiDetailId + ",")) {
                OnLineInfo onLineInfo = onLineInfoManager.findUniqueBy("id", Long.parseLong(recordNew.getPkId()));
				//更新直销流程审核时间和备注
				//if(auditStatus.equals("2")||auditStatus.equals("3")){
                if (auditStatus.equals("2") || auditStatus.equals("3")) {
					TaskInfo taskInfo = taskInfoManager.get(Long.parseLong(humanTaskId));
					onLineInfo.setAuditremark(taskInfo.getComment());
					onLineInfo.setAudittime(taskInfo.getCompleteTime());
					onLineInfoManager.save(onLineInfo);
                } else if ((busiDetailId.equals("9") || busiDetailId.equals("11") || busiDetailId.equals("13")) && auditStatus.equals("1")) {
					//初审通过发送短信。非12万旗舰店申请
                    String strHql = " from TaskInfo where businessKey=? and status=? and catalog=? and assignee=?";
                    List<TaskInfo> taskList = taskInfoManager.find(strHql,
														businessKey,
														"active",
														"normal",
														"4");
                    if (taskList != null && taskList.size() > 0) {
                        String strTaskContent = "您编号" + onLineInfo.getUcode() + "的【旗舰店申请】初审已通过，请上传资料！！";
                        if (busiDetailId.equals("11"))
                            strTaskContent = "您编号" + onLineInfo.getUcode() + "的【旗舰店申请续约】初审已通过，请上传资料！！";
                        else if (busiDetailId.equals("13"))
                            strTaskContent = "您编号" + onLineInfo.getUcode() + "的【旗舰店协议资料申请】初审已通过，请上传资料！！";
                        TimeTaskInfo timeTaskFirstInfo = new TimeTaskInfo();
						timeTaskFirstInfo.setTaskType("sendmsg");
						timeTaskFirstInfo.setTaskContent(strTaskContent);
						timeTaskFirstInfo.setTaskAddDate(new Date());
						timeTaskFirstInfo.setTaskNote(onLineInfo.getMobile());
						timeTaskManager.save(timeTaskFirstInfo);
						logger.info(String.format("TimeTaskInfo插入内容；taskType：%s；TaskContent：%s；TaskNote：%s；TaskAddDate：%s"
                               , timeTaskFirstInfo.getTaskType()
                                , timeTaskFirstInfo.getTaskContent()
                                , timeTaskFirstInfo.getTaskNote()
                                , DateUtils.formatDateTime(timeTaskFirstInfo.getTaskAddDate())));
					}
				}

				//审核结束发送短信
                if ((busiDetailId.equals("8") || busiDetailId.equals("9")
                        || busiDetailId.equals("10") || busiDetailId.equals("11")
                        || busiDetailId.equals("12") || busiDetailId.equals("13")
                        || busiDetailId.equals("14"))
                        && auditStatus.equals("2")) {
                    String strTaskContent = "您编号" + onLineInfo.getUcode() + "的【旗舰店申请】已审核通过，请登录系统查看！！";
                    if (busiDetailId.equals("10") || busiDetailId.equals("11"))
                        strTaskContent = "您编号" + onLineInfo.getUcode() + "的【旗舰店申请续约】已审核通过，请登录系统查看！！";
                    else if (busiDetailId.equals("12") || busiDetailId.equals("13"))
                        strTaskContent = "您编号" + onLineInfo.getUcode() + "的【旗舰店协议资料申请】已审核通过，请登录系统查看！！";
                    else if(busiDetailId.equals("14"))
                    	strTaskContent = "您编号" + onLineInfo.getUcode() + "的【市场推广合作协议资料申请】已审核通过，请登录系统查看！！";
                    TimeTaskInfo timeTaskEnd = new TimeTaskInfo();
					timeTaskEnd.setTaskType("sendmsg");
					timeTaskEnd.setTaskContent(strTaskContent);
					timeTaskEnd.setTaskAddDate(new Date());
					timeTaskEnd.setTaskNote(onLineInfo.getMobile());
					timeTaskManager.save(timeTaskEnd);
					logger.info(String.format("TimeTaskInfo插入内容；taskType：%s；TaskContent：%s；TaskNote：%s；TaskAddDate：%s"
                            , timeTaskEnd.getTaskType()
                            , timeTaskEnd.getTaskContent()
                            , timeTaskEnd.getTaskNote()
                            , DateUtils.formatDateTime(timeTaskEnd.getTaskAddDate())));
                } else if ((busiDetailId.equals("8") || busiDetailId.equals("9")
                        || busiDetailId.equals("10") || busiDetailId.equals("11")
                        || busiDetailId.equals("12") || busiDetailId.equals("13")
                        || busiDetailId.equals("14"))
                        && auditStatus.equals("3")) {
                    String strTaskContent = "您编号" + onLineInfo.getUcode() + "的【旗舰店申请】审核未通过，请登录系统查看原因！！";
                    if (busiDetailId.equals("10") || busiDetailId.equals("11"))
                        strTaskContent = "您编号" + onLineInfo.getUcode() + "的【旗舰店申请续约】审核未通过，请登录系统查看原因！！";
                    else if (busiDetailId.equals("12") || busiDetailId.equals("13"))
                        strTaskContent = "您编号" + onLineInfo.getUcode() + "的【旗舰店协议资料申请】审核未通过，请登录系统查看原因！！";
                    else if(busiDetailId.equals("14"))
                    	strTaskContent = "您编号" + onLineInfo.getUcode() + "的【市场推广合作协议资料申请】审核未通过，请登录系统查看原因！！";
                    TimeTaskInfo timeTaskEnd = new TimeTaskInfo();
					timeTaskEnd.setTaskType("sendmsg");
					timeTaskEnd.setTaskContent(strTaskContent);
					timeTaskEnd.setTaskAddDate(new Date());
					timeTaskEnd.setTaskNote(onLineInfo.getMobile());
					timeTaskManager.save(timeTaskEnd);
					logger.info(String.format("TimeTaskInfo插入内容；taskType：%s；TaskContent：%s；TaskNote：%s；TaskAddDate：%s"
                            , timeTaskEnd.getTaskType()
                            , timeTaskEnd.getTaskContent()
                            , timeTaskEnd.getTaskNote()
                            , DateUtils.formatDateTime(timeTaskEnd.getTaskAddDate())));
				}
                //调用直销OA的任务
                if (((busiDetailId.equals("8") || busiDetailId.equals("9")) && (auditStatus.equals("2") || auditStatus.equals("3")))
                        || ((busiDetailId.equals("10") || busiDetailId.equals("11")) && auditStatus.equals("2"))
                        || ((busiDetailId.equals("12") || busiDetailId.equals("13") || busiDetailId.equals("14")) && auditStatus.equals("2"))) {
                    String strBranch = "F01";
                    if (onLineInfo.getBranch().equals("二部"))
                        strBranch = "F02";
                    else if (onLineInfo.getBranch().equals("四部"))
                        strBranch = "F04";

                    String strSuccess = "true";
                    if (auditStatus.equals("3"))
                        strSuccess = "false";

                    TimeTaskInfo timeTaskEndService = new TimeTaskInfo();
                    if (busiDetailId.equals("8") || busiDetailId.equals("9")){
                    	//timeTaskEndService.setTaskType("serviceapplyshop");//旗舰店申请-第一次调直销接口type名称
                    	timeTaskEndService.setTaskType("serviceshopapply");//旗舰店申请-第二次调直销接口type名称
                    }
                    else if (busiDetailId.equals("10") || busiDetailId.equals("11")){
                    	//timeTaskEndService.setTaskType("applyshoprenew");//旗舰店续约-第一次调直销接口type名称
                    	timeTaskEndService.setTaskType("shopapplyrenew");//旗舰店续约-第二次调直销接口type名称
                    }
                    else if (busiDetailId.equals("12") || busiDetailId.equals("13"))
                        timeTaskEndService.setTaskType("serviceprotocol");//旗舰店协议资料申请
                    else if (busiDetailId.equals("14"))
                        timeTaskEndService.setTaskType("agentagreement");//代理商协议资料申请

                    Map<String, Object> jsonMap = new HashMap<String, Object>();
                    jsonMap.put("ucode", onLineInfo.getUcode() == null ? "" : onLineInfo.getUcode());
                    jsonMap.put("name", onLineInfo.getName() == null ? "" : onLineInfo.getName());
                    jsonMap.put("bankaddress", onLineInfo.getBankaddress() == null ? "" : onLineInfo.getBankaddress());
                    jsonMap.put("shopLicense", onLineInfo.getShopLicense() == null ? "" : onLineInfo.getShopLicense());
                    jsonMap.put("newpic", onLineInfo.getNewpic() == null ? "" : onLineInfo.getNewpic());
                    jsonMap.put("applycode", onLineInfo.getApplycode() == null ? "" : onLineInfo.getApplycode());
                    jsonMap.put("applytype", onLineInfo.getApplytype() == null ? "" : onLineInfo.getApplytype());
                    jsonMap.put("identity", onLineInfo.getIdentity() == null ? "" : onLineInfo.getIdentity());
                    jsonMap.put("enterpriseName", onLineInfo.getEnterpriseName() == null ? "" : onLineInfo.getEnterpriseName());
                    jsonMap.put("legaler", onLineInfo.getLegaler() == null ? "" : onLineInfo.getLegaler());
                    jsonMap.put("legalericcard", onLineInfo.getLegalerIdCard() == null ? "" : onLineInfo.getLegalerIdCard());
                    jsonMap.put("distributorPhone", onLineInfo.getDistributorPhone() == null ? "" : onLineInfo.getDistributorPhone());
                    jsonMap.put("scopeBusiness", onLineInfo.getScopeBusiness() == null ? "" : onLineInfo.getScopeBusiness());
                    jsonMap.put("note", onLineInfo.getNote() == null ? "" : onLineInfo.getNote());
                    jsonMap.put("reason", onLineInfo.getReason() == null ? "" : onLineInfo.getReason());
                    jsonMap.put("publicAccount", onLineInfo.getPublicAccount() == null ? "" : onLineInfo.getPublicAccount());
                    jsonMap.put("accountType", onLineInfo.getAccountType() == null ? "" : onLineInfo.getAccountType());
                    jsonMap.put("openingBank", onLineInfo.getOpeningBank() == null ? "" : onLineInfo.getOpeningBank());
                    jsonMap.put("openingName", onLineInfo.getOpeningName() == null ? "" : onLineInfo.getOpeningName());
                    jsonMap.put("accountNumber", onLineInfo.getAccountNumbr() == null ? "" : onLineInfo.getAccountNumbr());
                    jsonMap.put("storeArea", onLineInfo.getStoreArea() == null ? "" : onLineInfo.getStoreArea());
                    jsonMap.put("isAuthCertificate", onLineInfo.getIsAuthCertificate() == null ? "" : onLineInfo.getIsAuthCertificate());
                    jsonMap.put("locationBank",onLineInfo.getPartpic3()==null?"":onLineInfo.getPartpic3().toString());
                    timeTaskEndService.setTaskContent(jsonMapper.toJson(jsonMap));
					timeTaskEndService.setTaskAddDate(new Date());
                    timeTaskEndService.setTaskNote(strBranch + "," + strSuccess);
					timeTaskManager.save(timeTaskEndService);
					logger.info(String.format("TimeTaskInfo插入内容；taskType：%s；TaskContent：%s；TaskNote：%s；TaskAddDate：%s"
                            , timeTaskEndService.getTaskType()
                            , timeTaskEndService.getTaskContent()
                            , timeTaskEndService.getTaskNote()
                            , DateUtils.formatDateTime(timeTaskEndService.getTaskAddDate())));
				}
            	//审批后将此任务对应的消息置为已读 2018.08.27 sjx
			    String updateMsg = "update MsgInfo set status=1 where data=?";
			    msgInfoManager.batchUpdate(updateMsg, humanTaskId);				//处理同意并授权的数据
			}
            
            
            try{
            	//试用期人员员工申请走定时任务 add by lilei at 2018.11.22
            	if(auditStatus.equals("2")){
            		List<Map<String,Object>> mapContidionList=new ArrayList<Map<String,Object>>();
            		String strSql=String.format("select * from kv_record_condition where conditionName='person-probation' and businessKey=%s", businessKey);
            		mapContidionList=jdbcTemplate.queryForList(strSql);
            		if(mapContidionList.size()>0){
            			for (Map<String, Object> map : mapContidionList) {
            				TimeTaskInfo timeTaskEnd = new TimeTaskInfo();
        					timeTaskEnd.setTaskType("personprobation");
        					timeTaskEnd.setTaskContent(map.get("conditionValue")==null?"":map.get("conditionValue").toString());//map.get("note")==null?"":map.get("note").toString()
        					timeTaskEnd.setTaskAddDate(new Date());
        					timeTaskEnd.setTaskNote("试用期员工转正");
        					timeTaskManager.save(timeTaskEnd);
        					logger.info(String.format("TimeTaskInfo插入内容；taskType：%s；TaskContent：%s；TaskNote：%s；TaskAddDate：%s"
                                    , timeTaskEnd.getTaskType()
                                    , timeTaskEnd.getTaskContent()
                                    , timeTaskEnd.getTaskNote()
                                    , DateUtils.formatDateTime(timeTaskEnd.getTaskAddDate())));
            			}
            		}
            	}
            }
            catch(Exception ex){
            	throw new RuntimeException(ex);
            }
		} catch (Exception e) {
			// TODO: handle exception
			throw new RuntimeException(e);
		}
	}

	/**
	 * APP推送消息 add by lilei at 2018.02.07
     **/
	@Transactional(readOnly = false)
	//public void PushAPPMsg()
	//{
    public void PushAPPMsg() {
		try {
			/*mqParam参数：user_id：用户id，user_type  用户类别，title   标题 ，content  内容*/
			String strDeviceSql="SELECT d.ATTRIBUTE1 AS mobilePlat,ifnull(d.`CODE`,'') AS deviceCode,m.id,m.RECEIVER_ID,m.`NAME`,m.msg_type,m.CONTENT FROM ACCOUNT_DEVICE d "
					+" INNER JOIN msg_info m on d.ACCOUNT_ID=m.RECEIVER_ID"
					+" WHERE m.is_sendmsg in ('0','5') AND lower(d.type)='mobile' AND m.`STATUS`='0'  ORDER BY is_sendmsg  LIMIT 0,30";
			List<Map<String,Object>> mapDeviceList=jdbcTemplate.queryForList(strDeviceSql);
			logger.info("APP推送消息条数："+mapDeviceList.size());
			System.out.println(mapDeviceList.size());
			if(mapDeviceList!=null&&mapDeviceList.size()>0){		
				for(Map<String,Object> map:mapDeviceList){
					Map<String,String> mapParam=new HashMap<String, String>();					mapParam.put("title", map.get("NAME").toString());
					mapParam.put("user_id", map.get("RECEIVER_ID").toString());
					String content=LostHtml(map.get("CONTENT").toString());
					mapParam.put("description", content);
					mapParam.put("to_project", "0");
					mapParam.put("to_project_name","mailian");
					Map<String,String> mapExtra=new HashMap<String, String>();
					mapExtra.put("msg_type", map.get("msg_type").toString());
					mapExtra.put("msg_id", map.get("id").toString());
					mapParam.put("extra",jsonMapper.toJson(mapExtra));
					mapParam.put("id",map.get("id").toString());
					mapParam.put("deviceCode",map.get("deviceCode").toString());
					msgPushConnector.sendPushOneMsg(mapParam);
					
					//logger.info("APP推送消息JSON："+jsonMapper.toJson(mapParam));

					//region 旧推送调用接口（友盟）
					/*Map<String,String> mapParam=new HashMap<String, String>();
					mapParam.put("user_id", map.get("RECEIVER_ID").toString());
					mapParam.put("user_type", strUserType);
					mapParam.put("title", map.get("NAME").toString());
					//String content=LostHtml(map.get("CONTENT").toString());
                    String content = LostHtml(map.get("CONTENT").toString());
					mapParam.put("content", content);
					mapParam.put("msg_type", map.get("msg_type").toString());
					mapParam.put("msg_id", map.get("id").toString());
					//接口那边：1(安卓)，2(ios)
					mapParam.put("PUSH_APP_TYPE", map.get("mobilePlat").toString());
					//String strMd5=MD5Util.getMd5(mapParam,strKey);
                    String strMd5 = MD5Util.getMd5(mapParam, strKey);
					mapParam.put("md5", strMd5);
					//处理业务
					try {
						HttpRequester request = new HttpRequester();
						HttpRespons response = null;
						try {
							response = request.sendPost(strUrl, mapParam);
						} catch (Exception e) {
							e.printStackTrace();
						}
						String strReturn = response.getContent();
						//Map<String,Object> mapJson=jsonMapper.fromJson(strReturn,Map.class);
						//if(mapJson.get("status")!=null
							//&&mapJson.get("status").toString().equals("1")){
                        Map<String, Object> mapJson = jsonMapper.fromJson(strReturn, Map.class);
                        if (mapJson.get("status") != null
                                && mapJson.get("status").toString().equals("1")) {
							//更新消息
							//String strMsgSql="UPDATE MSG_INFO SET is_sendmsg='1' WHERE ID="+map.get("id");
                            String strMsgSql = "UPDATE MSG_INFO SET is_sendmsg='1' WHERE ID=" + map.get("id");
							jdbcTemplate.update(strMsgSql);
							logger.info("APP推送消息定时任务推送完成，msg_id="
							//+map.get("id")+",RECEIVER_ID="
							//+map.get("RECEIVER_ID").toString());
                                    + map.get("id") + ",RECEIVER_ID="
                                    + map.get("RECEIVER_ID").toString());
					}
				} catch (IOException e) {
					e.printStackTrace();
				//}*/
				//endregion
				}
			}
		} catch (Exception e) {
            logger.info("APP推送消息定时任务异常：" + e.getMessage() + "\r\n" + e.getStackTrace());
		}
	}
	//private String LostHtml(String strHtml){

    private String LostHtml(String strHtml) {
		String regxpForHtml = "<([^>]*)>"; // 过滤所有以<开头以>结尾的标签
		Pattern p_html = Pattern.compile(regxpForHtml, Pattern.CASE_INSENSITIVE);
        Matcher m_html = p_html.matcher(strHtml);
        strHtml = m_html.replaceAll(""); // 过滤html标签
        return strHtml;
	}

	/**
	 * 发送一个POST请求
    //private String sendPost(String url,String Params)throws IOException{
 */
    private String sendPost(String url, String Params) throws IOException {
	        OutputStreamWriter out = null;
	        BufferedReader reader = null;
	        //String response="";
        String response = "";
	        try {
	            URL httpUrl = null; //HTTP URL类 用这个类来创建连接
	            //创建URL
	            httpUrl = new URL(url);
	            //建立连接
	            HttpURLConnection conn = (HttpURLConnection) httpUrl.openConnection();
	            conn.setRequestMethod("POST");
	            conn.setRequestProperty("Content-Type", "application/json");
	            conn.setRequestProperty("connection", "keep-alive");
	            conn.setUseCaches(false);//设置不要缓存
	            conn.setInstanceFollowRedirects(true);
	            conn.setDoOutput(true);
	            conn.setDoInput(true);
	            conn.connect();
	            //POST请求
	            out = new OutputStreamWriter(
	                    //conn.getOutputStream());	            
                    conn.getOutputStream());
	            out.write(Params);
	            out.flush();
	            //读取响应
	            reader = new BufferedReader(new InputStreamReader(
	                    conn.getInputStream()));
	            String lines;
	            while ((lines = reader.readLine()) != null) {
	                lines = new String(lines.getBytes(), "utf-8");
	                //response+=lines;
                response += lines;
	            }
	            reader.close();
	            // 断开连接
	            conn.disconnect();

	            //log.info(response.toString());
	        } catch (Exception e) {
            logger.info("发送 POST 请求出现异常！" + e);
            System.out.println("发送 POST 请求出现异常！" + e);
		        e.printStackTrace();
	        }
        finally {
            try {
                if (out != null)
	                out.close();
                if (reader != null)
	                reader.close();
            } catch (IOException ex) {
	            ex.printStackTrace();
	        }
	    }
        return response;
    }

	//region 资源注入
	@Resource
	public void setOnLineInfoManager(OnLineInfoManager onLineInfoManager) {
		this.onLineInfoManager = onLineInfoManager;
	}

	@Resource
    //public void setStoreConnector(StoreConnector storeConnector){
    	//this.storeConnector=storeConnector;
    public void setStoreConnector(StoreConnector storeConnector) {
        this.storeConnector = storeConnector;
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
    //public void setWebAPI(WebAPI webAPI){
    	//this.webAPI=webAPI;
    public void setWebAPI(WebAPI webAPI) {
        this.webAPI = webAPI;
    }

	@Resource
	public void setSignInfo(SignInfo signInfo) {
		this.signInfo = signInfo;
	}

	@Resource
	public void setTaskInfoManager(TaskInfoManager taskInfoManager) {
		this.taskInfoManager = taskInfoManager;
	}

	@Resource
	public void setRecordManager(RecordManager recordManager) {
		this.recordManager = recordManager;
	}

	@Resource
    public void setTimeTaskManager(TimeTaskManager timeTaskManager) {
		this.timeTaskManager = timeTaskManager;
	}
	
	@Resource
    public void setPersonInfoManager(PersonInfoManager personInfoManager) {
		this.personInfoManager = personInfoManager;
	}
	//endregion
}