package com.mossle.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.jdbc.core.JdbcTemplate;

import com.hp.hpl.sparta.Document.Index;
import com.mossle.api.user.UserConnector;
import com.mossle.common.utils.StringUtils;
import com.mossle.core.export.ExcelExport;
import com.mossle.core.page.Page;
import com.mossle.dict.persistence.domain.DictInfo;
import com.mossle.humantask.persistence.domain.UnfinishProcessInstance;
import com.mossle.operation.persistence.domain.ApplyDTO;
import com.mossle.operation.persistence.domain.BusinessDTO;
import com.mossle.operation.persistence.domain.CancelOrderDTO;
import com.mossle.operation.persistence.domain.CustomEntityDTO;
import com.mossle.operation.persistence.domain.Exchange;
import com.mossle.operation.persistence.domain.FreezeDTO;
import com.mossle.operation.persistence.domain.InvoiceDTO;
import com.mossle.operation.persistence.domain.LllegalFreezeDTO;
import com.mossle.operation.persistence.domain.ReturnDTO;
import com.mossle.user.persistence.domain.PersonSalaryAccumulationFund;
import com.mossle.user.persistence.domain.PersonSalaryBase;
import com.mossle.user.persistence.domain.PersonSalarySocialSecurity;
import com.mossle.ws.persistence.domain.OnLineInfoDTO;

import ch.qos.logback.core.joran.spi.ElementSelector;
/**
 * 
 * 导出功能工具类
 *
 */
public class ExportUtil {
	static SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
	static ExcelExport excelExport;
	
	public static void errHtml(HttpServletResponse response){
		String title = "导出条件数据异常,请重新选择导出条件。";
		StringBuffer sb = new StringBuffer();
		sb.append("<script language='javascript'>alert('");
		sb.append(title);
		sb.append("');history.go(-1);</script>");
		try {
			response.setContentType("text/html; charset=utf-8");
			response.getWriter().println(sb.toString());
			response.getWriter().flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	//验证细分id是否合法
	public static boolean isNumeric(String str) {
        String bigStr;
        try {
            bigStr = new BigDecimal(str).toString();
        } catch (Exception e) {
            return false;//异常 说明包含非数字。
        }
        return true;
    }
	//过滤文本编辑器存入的html标签
	public static String LostHtml(String strHtml) {
		String regxpForHtml = "<([^>]*)>"; // 过滤所有以<开头以>结尾的标签
		Pattern p_html = Pattern.compile(regxpForHtml, Pattern.CASE_INSENSITIVE);
        Matcher m_html = p_html.matcher(strHtml);
        strHtml = m_html.replaceAll(""); // 过滤html标签
        return strHtml;
	}
	 /**
     * 
     * @param page 
     * @param response
     * @param request
     * @param detaliId 
     * @param formName 表单路径
     * @throws IOException
     */
    public static void export(Page page,HttpServletResponse response,HttpServletRequest request,String detaliId,String formName,String typeName) throws IOException{
    	 if (page.getResultSize() == 0) {
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
         } else if(detaliId.length() != 0 && formName.equals("operation/custom-apply-list")){
        	List<CustomEntityDTO> dataset = (List<CustomEntityDTO>) page.getResult();
        	CustomEntityDTO customEntityDTO = dataset.get(0);
        	String assignee = customEntityDTO.getAssignee();
        	String fileName = typeName + formatter.format(new Date()) + ".xls";
        	if(StringUtils.isNotBlank(assignee)){
             	String[] headers = {"提交次数","申请单号","主题","抄送人", "业务类型", "业务细分", "申请人", "申请内容","申请时间","最后审批时间","审批人"};
             	String[] fieldNames = {"submitTimes","applyCode","theme", "ccName", "businessType", "businessDetail", "name", "applyContent","createTime","completeTime","assignee"};
             	export(page,response,request,headers,fieldNames,fileName); 
        	}else{
             	String[] headers = {"提交次数","申请单号","主题","抄送人", "业务类型", "业务细分", "申请人", "申请内容","申请时间","最后审批时间"};
             	String[] fieldNames = {"submitTimes","applyCode","theme", "ccName", "businessType", "businessDetail", "name", "applyContent","createTime","completeTime"};
             	export(page,response,request,headers,fieldNames,fileName); 
        	}
         }else if(detaliId.length() != 0 && formName.equals("operation/process/GroupBusinessApplyForm")){
        	List<BusinessDTO> dataset = (List<BusinessDTO>) page.getResult();
        	BusinessDTO businessDTO = dataset.get(0);
         	String assignee = businessDTO.getAssignee();
         	String fileName = typeName + formatter.format(new Date()) + ".xls";
         	if(StringUtils.isNotBlank(assignee)){
             	String[] headers = {"提交次数","申请单号","主题", "抄送人","业务类型", "业务细分", "申请人", "申请内容","申请时间","最后审批时间","审批人"};
             	String[] fieldNames = { "submitTimes","applyCode","theme","cc", "businessType", "businessDetail", "initiator", "applyContent","createTime","completeTime","assignee"};
             	export(page,response,request,headers,fieldNames,fileName);
         	}else{
             	String[] headers = {"提交次数","申请单号","主题", "抄送人","业务类型", "业务细分", "申请人", "申请内容","申请时间","最后审批时间"};
             	String[] fieldNames = { "submitTimes","applyCode","theme","cc", "businessType", "businessDetail", "initiator", "applyContent","createTime","completeTime"};
             	export(page,response,request,headers,fieldNames,fileName);
         	}
         }else if(detaliId.length() != 0 && formName.equals("operation/process/BusinessApplyForm")){
        	List<BusinessDTO> dataset = (List<BusinessDTO>) page.getResult();
        	BusinessDTO businessDTO = dataset.get(0);
          	String assignee = businessDTO.getAssignee();
          	String fileName = typeName + formatter.format(new Date()) + ".xls";
          	if(StringUtils.isNotBlank(assignee)){
             	String[] headers = {"提交次数","申请单号","主题", "业务类型", "业务细分", "申请人", "申请内容", "所属大区", "所属分公司","申请时间","最后审批时间","审批人"};
             	String[] fieldNames = {"submitTimes","applyCode","theme", "businessType", "businessDetail", "initiator", "applyContent", "area", "branchOffice","createTime","completeTime","assignee"};
             	export(page,response,request,headers,fieldNames,fileName);
          	}else{
             	String[] headers = {"提交次数","申请单号","主题", "业务类型", "业务细分", "申请人", "申请内容", "所属大区", "所属分公司","申请时间","最后审批时间"};
             	String[] fieldNames = {"submitTimes","applyCode","theme", "businessType", "businessDetail", "initiator", "applyContent", "area", "branchOffice","createTime","completeTime"};
             	export(page,response,request,headers,fieldNames,fileName);
          	}
         }else if(detaliId.length() != 0 && formName.equals("operation/process/FreezeApplyForm")){
        	List<FreezeDTO> dataset = (List<FreezeDTO>) page.getResult();
        	FreezeDTO freezeDTO = dataset.get(0);
           	String assignee = freezeDTO.getAssignee();
           	String fileName = typeName + formatter.format(new Date()) + ".xls";
           	if(StringUtils.isNotBlank(assignee)){
             	String[] headers = {"编号", "姓名", "联系方式", "销售级别", "福利级别", "激活状态", "所属体系", "上属董事", "冻结状态","所属区域", "所属分公司","身份证号","申请受理事项","申请内容","申请时间","最后审批时间","审批人"};
             	String[] fieldNames = {"ucode", "name", "contact", "salesLevel", "welfareLevel", "activationState","system", "aboveBoard", "frozenState","area", "branchOffice", "idNumber","applyMatter","applyContent","createTime","completeTime","assignee"};
             	export(page,response,request,headers,fieldNames,fileName);
           	}else{
             	String[] headers = {"编号", "姓名", "联系方式", "销售级别", "福利级别", "激活状态", "所属体系", "上属董事", "冻结状态","所属区域", "所属分公司","身份证号","申请受理事项","申请内容","申请时间","最后审批时间"};
             	String[] fieldNames = {"ucode", "name", "contact", "salesLevel", "welfareLevel", "activationState","system", "aboveBoard", "frozenState","area", "branchOffice", "idNumber","applyMatter","applyContent","createTime","completeTime"};
             	export(page,response,request,headers,fieldNames,fileName);
           	}
         }else if(detaliId.length() != 0 && formName.equals("operation/process/LllegalFreezeApplyForm")){
        	List<LllegalFreezeDTO> dataset = (List<LllegalFreezeDTO>) page.getResult();
        	LllegalFreezeDTO lllegalFreezeDTO = dataset.get(0);
        	String assignee = lllegalFreezeDTO.getAssignee();
        	String fileName = typeName + formatter.format(new Date()) + ".xls";
        	if(StringUtils.isNotBlank(assignee)){
             	String[] headers = {"提交次数","主题","抄送人", "业务类型", "业务细分", "业务级别", "发起人", "编号", "姓名", "福利级别", "资格状态","所属体系", "联系方式","所属区域","所属分公司","身份证号","上属董事","联系方式","申请受理事项","申请内容","申请时间","最后审批时间","审批人"};
             	String[] fieldNames = {"submitTimes","theme","cc", "businessType", "businessDetail", "businessLevel", "initiator", "ucode","name", "welfareLevel", "qualificationsStatus","system", "contact", "area","company","idNumber","aboveBoard","directorContact","applyMatter","applyContent","createTime","completeTime","assignee"};
             	export(page,response,request,headers,fieldNames,fileName);
        	}else{
             	String[] headers = {"提交次数","主题","抄送人", "业务类型", "业务细分", "业务级别", "发起人", "编号", "姓名", "福利级别", "资格状态","所属体系", "联系方式","所属区域","所属分公司","身份证号","上属董事","联系方式","申请受理事项","申请内容","申请时间","最后审批时间"};
             	String[] fieldNames = {"submitTimes","theme","cc", "businessType", "businessDetail", "businessLevel", "initiator", "ucode","name", "welfareLevel", "qualificationsStatus","system", "contact", "area","company","idNumber","aboveBoard","directorContact","applyMatter","applyContent","createTime","completeTime"};
             	export(page,response,request,headers,fieldNames,fileName);
        	}
         }else if(detaliId.length() != 0 && formName.equals("operation/process/ReturnApplyForm")){
        	List<ReturnDTO> dataset = (List<ReturnDTO>) page.getResult();
        	ReturnDTO returnDTO = dataset.get(0);
         	String assignee = returnDTO.getAssignee();
         	String fileName = typeName + formatter.format(new Date()) + ".xls";
         	if(StringUtils.isNotBlank(assignee)){
             	String[] headers = {"所属仓库","客服工号","专卖店编号", "专卖店姓名", "专卖店电话", "申请退货日期","订单单据号", "退货原因", "店支付库存", "奖励积分库存", "个人钱包库存","手续费","申请时间","最后审批时间","审批人"};
             	String[] fieldNames = {"wareHouse","empNo", "ucode", "shopName", "shopTel", "returnDate", "orderNumber","returnReaon", "shopPayStock", "rewardIntegralStock","personPayStock", "payType","createTime","completeTime","assignee"};
             	export(page,response,request,headers,fieldNames,fileName);
         	}else{
             	String[] headers = {"所属仓库","客服工号","专卖店编号", "专卖店姓名", "专卖店电话", "申请退货日期","订单单据号", "退货原因", "店支付库存", "奖励积分库存", "个人钱包库存","手续费","申请时间","最后审批时间"};
             	String[] fieldNames = {"wareHouse","empNo", "ucode", "shopName", "shopTel", "returnDate", "orderNumber","returnReaon", "shopPayStock", "rewardIntegralStock","personPayStock", "payType","createTime","completeTime"};
             	export(page,response,request,headers,fieldNames,fileName);
         	}
         }else if(detaliId.length() != 0 && formName.equals("operation/process/InvoiceApplyForm")){
        	List<InvoiceDTO> dataset = (List<InvoiceDTO>) page.getResult();
        	InvoiceDTO invoiceDTO = dataset.get(0);
          	String assignee = invoiceDTO.getAssignee();
          	String fileName = typeName + formatter.format(new Date()) + ".xls";
          	if(StringUtils.isNotBlank(assignee)){
             	String[] headers = {"专卖店编号","专卖店姓名","专卖店电话", "申请发票日期", "订单单据号", "所属区域","所属体系", "所属分公司", "发票类型","发票类别", "发票抬头", "发票明细(产品名称、价格、数量)","发票开具总金额","身份证号码","企业名称","税务登记号","开户行","开户行账号","企业地址及电话","发票邮寄地址","收件人姓名","收件人电话","收件人备用电话","申请时间","最后审批时间","审批人"};
             	String[] fieldNames = {"ucode","shopName", "shopTel", "invoiceDate", "orderNumber", "area", "system","branchOffice", "invoiceType","category", "invoiceTitle","invoiceDetail", "invoiceMoney","idNumber","enterpriseName","taxNumber","openingBank","accountNumber","enterpriseAddress","invoiceMailAddress","addressee","addresseeTel","addresseeSpareTel","createTime","completeTime","assignee"};
             	export(page,response,request,headers,fieldNames,fileName);
          	}else{
             	String[] headers = {"专卖店编号","专卖店姓名","专卖店电话", "申请发票日期", "订单单据号", "所属区域","所属体系", "所属分公司", "发票类型","发票类别", "发票抬头", "发票明细(产品名称、价格、数量)","发票开具总金额","身份证号码","企业名称","税务登记号","开户行","开户行账号","企业地址及电话","发票邮寄地址","收件人姓名","收件人电话","收件人备用电话","申请时间","最后审批时间"};
             	String[] fieldNames = {"ucode","shopName", "shopTel", "invoiceDate", "orderNumber", "area", "system","branchOffice", "invoiceType","category", "invoiceTitle","invoiceDetail", "invoiceMoney","idNumber","enterpriseName","taxNumber","openingBank","accountNumber","enterpriseAddress","invoiceMailAddress","addressee","addresseeTel","addresseeSpareTel","createTime","completeTime"};
             	export(page,response,request,headers,fieldNames,fileName);
          	}
         }else if(detaliId.length() != 0 && formName.equals("operation/common-operation")){
        	List<ApplyDTO> dataset = (List<ApplyDTO>) page.getResult();
        	ApplyDTO applyDTO = dataset.get(0);
           	String assignee = applyDTO.getAssignee();
           	String fileName = typeName + formatter.format(new Date()) + ".xls";
           	if(StringUtils.isNotBlank(assignee)){
             	String[] headers = {"提交次数","受理单编号","经销商编号", "经销商姓名", "福利级别", "级别","所属体系", "销售人", "服务人","注册时间", "申请业务类型", "业务细分","联系电话","联系地址","业务级别","所属大区","申请内容","申请时间","最后审批时间","审批人"};
             	String[] fieldNames = {"submitTimes","applyCode","ucode","userName","welfare","level","system","varFather","varRe","addTime","businessType","businessDetail","mobile","address","businessLevel","area","applyContent","createTime","completeTime","assignee"};
             	export(page,response,request,headers,fieldNames,fileName);
           	}else{
             	String[] headers = {"提交次数","受理单编号","经销商编号", "经销商姓名", "福利级别", "级别","所属体系", "销售人", "服务人","注册时间", "申请业务类型", "业务细分","联系电话","联系地址","业务级别","所属大区","申请内容","申请时间","最后审批时间"};
             	String[] fieldNames = {"submitTimes","applyCode","ucode","userName","welfare","level","system","varFather","varRe","addTime","businessType","businessDetail","mobile","address","businessLevel","area","applyContent","createTime","completeTime"};
             	export(page,response,request,headers,fieldNames,fileName);
           	}
         }else if(detaliId.length() != 0 && formName.equals("operation/cancel-order")){
        	List<CancelOrderDTO> dataset = (List<CancelOrderDTO>) page.getResult();
        	CancelOrderDTO cancelOrderDTO = dataset.get(0);
        	String assignee = cancelOrderDTO.getAssignee();
        	String fileName = typeName + formatter.format(new Date()) + ".xls";
        	if(StringUtils.isNotBlank(assignee)){
             	String[] headers = {"提交次数","受理单编号","店编号", "店姓名", "店电话", "来电电话","撤单登记时间", "登记人", "是否核实","撤单备注","申请时间","最后审批时间","审批人"};
             	String[] fieldNames = {"submitTimes","applyCode", "ucode","shopName", "shopMobile", "mobile", "registerTime", "registerName","isChecked", "cancelRemark","createTime","completeTime","assignee"};
             	export(page,response,request,headers,fieldNames,fileName);
        	}else{
             	String[] headers = {"提交次数","受理单编号","店编号", "店姓名", "店电话", "来电电话","撤单登记时间", "登记人", "是否核实","撤单备注","申请时间","最后审批时间"};
             	String[] fieldNames = {"submitTimes","applyCode", "ucode","shopName", "shopMobile", "mobile", "registerTime", "registerName","isChecked", "cancelRemark","createTime","completeTime"};
             	export(page,response,request,headers,fieldNames,fileName);
        	}
         }else if(detaliId.length() != 0 && formName.equals("oaServicePushProcess")){//直销的申请
        	 String fileName = typeName + formatter.format(new Date()) + ".xls";
        	if(detaliId.equals("1")){//密码初始化
        		List<OnLineInfoDTO> dataset = (List<OnLineInfoDTO>) page.getResult();
         	    OnLineInfoDTO onLineInfoDTO = dataset.get(0);
            	String assignee = onLineInfoDTO.getAssignee();;
            	if(StringUtils.isNotBlank(assignee)){
            		String[] headers = {"受理编号","申请人编号","所属部门", "申请人姓名", "申请人证件号", "申请人福利级别","短信接收号码","代理人编号","业务细分","申请原因","申请时间","最后审批时间","审批人"};
            		String[] fieldNames = {"applycode","ucode", "branch", "name", "identity", "welfaregrade","mobile", "bankcode", "applytype","reason","applytime","audittime","assignee"};
            		export(page,response,request,headers,fieldNames,fileName);
            	}else{
            		String[] headers = {"受理编号","申请人编号","所属部门", "申请人姓名", "申请人证件号", "申请人福利级别","短信接收号码","代理人编号","业务细分","申请原因","申请时间","最后审批时间"};
            		String[] fieldNames = {"applycode","ucode", "branch", "name", "identity", "welfaregrade","mobile", "bankcode", "applytype","reason","applytime","audittime"};
            		export(page,response,request,headers,fieldNames,fileName);
            	}
        		
        	}else if(detaliId.equals("2")){//更正姓名
        		List<OnLineInfoDTO> dataset = (List<OnLineInfoDTO>) page.getResult();
         	    OnLineInfoDTO onLineInfoDTO = dataset.get(0);
            	String assignee = onLineInfoDTO.getAssignee();;
            	if(StringUtils.isNotBlank(assignee)){
            		String[] headers = {"受理编号","申请人编号","所属部门", "申请人姓名","新姓名", "申请人证件号", "申请人福利级别","业务细分","申请原因","申请时间","最后审批时间","审批人"};
            		String[] fieldNames = {"applycode","ucode", "branch", "name","newname", "identity", "welfaregrade", "applytype","reason","applytime","audittime","assignee"};
            		export(page,response,request,headers,fieldNames,fileName);
            	}else{
            		String[] headers = {"受理编号","申请人编号","所属部门", "申请人姓名","新姓名", "申请人证件号", "申请人福利级别","业务细分","申请原因","申请时间","最后审批时间"};
            		String[] fieldNames = {"applycode","ucode", "branch", "name","newname", "identity", "welfaregrade", "applytype","reason","applytime","audittime"};
            		export(page,response,request,headers,fieldNames,fileName);
            	}
        	}else if(detaliId.equals("3")){//身份证更正
        		List<OnLineInfoDTO> dataset = (List<OnLineInfoDTO>) page.getResult();
         	    OnLineInfoDTO onLineInfoDTO = dataset.get(0);
            	String assignee = onLineInfoDTO.getAssignee();;
            	if(StringUtils.isNotBlank(assignee)){
            		String[] headers = {"受理编号","申请人编号","所属部门", "申请人姓名", "申请人证件号","新证件号", "申请人福利级别","业务细分","申请原因","申请时间","最后审批时间","审批人"};
            		String[] fieldNames = {"applycode","ucode", "branch", "name", "identity","newidentity", "welfaregrade", "applytype","reason","applytime","audittime","assignee"};
            		export(page,response,request,headers,fieldNames,fileName);
            	}else{
            		String[] headers = {"受理编号","申请人编号","所属部门", "申请人姓名", "申请人证件号","新证件号", "申请人福利级别","业务细分","申请原因","申请时间","最后审批时间"};
            		String[] fieldNames = {"applycode","ucode", "branch", "name", "identity","newidentity", "welfaregrade", "applytype","reason","applytime","audittime"};
            		export(page,response,request,headers,fieldNames,fileName);
            	}
        	}else if(detaliId.equals("4")){//资格注销
        		List<OnLineInfoDTO> dataset = (List<OnLineInfoDTO>) page.getResult();
         	    OnLineInfoDTO onLineInfoDTO = dataset.get(0);
            	String assignee = onLineInfoDTO.getAssignee();;
            	if(StringUtils.isNotBlank(assignee)){
            		String[] headers = {"受理编号","申请人编号","所属部门", "申请人姓名", "申请人证件号", "申请人福利级别","业务细分","申请原因","申请时间","最后审批时间","审批人"};
            		String[] fieldNames = {"applycode","ucode", "branch", "name", "identity", "welfaregrade", "applytype","reason","applytime","audittime","assignee"};
            		export(page,response,request,headers,fieldNames,fileName);
            	}else{
            		String[] headers = {"受理编号","申请人编号","所属部门", "申请人姓名", "申请人证件号", "申请人福利级别","业务细分","申请原因","申请时间","最后审批时间"};
            		String[] fieldNames = {"applycode","ucode", "branch", "name", "identity", "welfaregrade", "applytype","reason","applytime","audittime"};
            		export(page,response,request,headers,fieldNames,fileName);
            	}
        	}else if(detaliId.equals("5")){//直系亲属资格替换
        		List<OnLineInfoDTO> dataset = (List<OnLineInfoDTO>) page.getResult();
         	    OnLineInfoDTO onLineInfoDTO = dataset.get(0);
            	String assignee = onLineInfoDTO.getAssignee();;
            	if(StringUtils.isNotBlank(assignee)){
            		String[] headers = {"受理编号","申请人编号","所属部门", "申请人姓名", "申请人证件号", "申请人福利级别","新姓名","新证件号","新银行名称","新银行账号","新银行地址","业务细分","申请原因","申请时间","最后审批时间","审批人"};
            		String[] fieldNames = {"applycode","ucode", "branch", "name", "identity", "welfaregrade","newname","newidentity","bankname","bankcode","bankaddress", "applytype","reason","applytime","audittime","assignee"};
            		export(page,response,request,headers,fieldNames,fileName);
            	}else{
            		String[] headers = {"受理编号","申请人编号","所属部门", "申请人姓名", "申请人证件号", "申请人福利级别","新姓名","新证件号","新银行名称","新银行账号","新银行地址","业务细分","申请原因","申请时间","最后审批时间","批示内容"};
            		String[] fieldNames = {"applycode","ucode", "branch", "name", "identity", "welfaregrade","newname","newidentity","bankname","bankcode","bankaddress", "applytype","reason","applytime","audittime","comment"};
            		export(page,response,request,headers,fieldNames,fileName);
            	}
        	}else if(detaliId.equals("6")){//更正手机号
        		List<OnLineInfoDTO> dataset = (List<OnLineInfoDTO>) page.getResult();
         	    OnLineInfoDTO onLineInfoDTO = dataset.get(0);
            	String assignee = onLineInfoDTO.getAssignee();;
            	if(StringUtils.isNotBlank(assignee)){
            		String[] headers = {"受理编号","申请人编号","所属部门", "申请人姓名", "申请人福利级别","原手机号","新手机号","业务细分","申请原因","申请时间","最后审批时间","审批人"};
            		String[] fieldNames = {"applycode","ucode", "branch", "name",  "welfaregrade","mobile","newname", "applytype","reason","applytime","audittime","assignee"};
            		export(page,response,request,headers,fieldNames,fileName);
            	}else{
            		String[] headers = {"受理编号","申请人编号","所属部门", "申请人姓名", "申请人福利级别","原手机号","新手机号","业务细分","申请原因","申请时间","最后审批时间"};
            		String[] fieldNames = {"applycode","ucode", "branch", "name",  "welfaregrade","mobile","newname", "applytype","reason","applytime","audittime"};
            		export(page,response,request,headers,fieldNames,fileName);
            	}
            }else if(detaliId.equals("7")){//开通订货及转账权限
            	List<OnLineInfoDTO> dataset = (List<OnLineInfoDTO>) page.getResult();
         	    OnLineInfoDTO onLineInfoDTO = dataset.get(0);
            	String assignee = onLineInfoDTO.getAssignee();
            	if(StringUtils.isNotBlank(assignee)){
            		String[] headers = {"受理编号","申请人编号","所属部门", "申请人姓名","申请人证件号", "申请人福利级别","业务细分","申请原因","申请时间","最后审批时间","审批人"};
	               	String[] fieldNames = {"applycode","ucode", "branch", "name","identity", "welfaregrade", "applytype","reason","applytime","audittime","assignee"};
	               	export(page,response,request,headers,fieldNames,fileName);
            	}else{
            		String[] headers = {"受理编号","申请人编号","所属部门", "申请人姓名","申请人证件号", "申请人福利级别","业务细分","申请原因","申请时间","最后审批时间"};
	               	String[] fieldNames = {"applycode","ucode", "branch", "name","identity", "welfaregrade", "applytype","reason","applytime","audittime"};
	               	export(page,response,request,headers,fieldNames,fileName);
            	}
           }else if(detaliId.equals("8")||detaliId.equals("9")||detaliId.equals("10")||detaliId.equals("11")){//12万旗舰店申请 非12万旗舰店申请 12万申请续约 非12万申请续约
        	   List<OnLineInfoDTO> dataset = (List<OnLineInfoDTO>) page.getResult();
        	   OnLineInfoDTO onLineInfoDTO = dataset.get(0);
           	   String assignee = onLineInfoDTO.getAssignee();
           	   if(StringUtils.isNotBlank(assignee)){
           		 String[] headers = {"受理编号","申请人编号","所属部门", "申请人姓名","申请人证件号", "申请人福利级别","旗舰店地址","实体店面积","营业执照注册号","业务细分","申请原因","申请时间","最后审批时间","审批人"};
             	 String[] fieldNames = {"applycode","ucode", "branch", "name","identity", "welfaregrade","bankaddress","storeArea","completeremark", "applytype","reason","applytime","audittime","assignee"};
             	 export(page,response,request,headers,fieldNames,fileName);
           	   }else{
           		 String[] headers = {"受理编号","申请人编号","所属部门", "申请人姓名","申请人证件号", "申请人福利级别","旗舰店地址","实体店面积","营业执照注册号","业务细分","申请原因","申请时间","最后审批时间"};
             	 String[] fieldNames = {"applycode","ucode", "branch", "name","identity", "welfaregrade","bankaddress","storeArea","completeremark", "applytype","reason","applytime","audittime"};
             	 export(page,response,request,headers,fieldNames,fileName);
           	   }
           }else if(detaliId.equals("12")||detaliId.equals("13")){//12万旗舰店协议申请和非12万旗舰店协议申请
        	   List<OnLineInfoDTO> dataset = (List<OnLineInfoDTO>) page.getResult();
        	   OnLineInfoDTO onLineInfoDTO = dataset.get(0);
           	   String assignee = onLineInfoDTO.getAssignee();
           	   if(StringUtils.isNotBlank(assignee)){
           		 String[] headers = {"受理编号","申请人编号","所属部门", "申请人姓名","申请人证件号", "申请人福利级别","旗舰店地址","实体店面积","统一社会信用代码","企业名称","法定代表人","联系电话","法人身份证号码","经营范围","备注","业务细分","申请原因","申请时间","最后审批时间","审批人"};
             	 String[] fieldNames = {"applycode","ucode", "branch", "name","identity", "welfaregrade","bankaddress","storeArea","shopLicense","enterpriseName","legaler","distributorPhone","legalerIdCard","scopeBusiness","note", "applytype","reason","applytime","audittime","assignee"};
             	 export(page,response,request,headers,fieldNames,fileName);
           	   }else{
           		 String[] headers = {"受理编号","申请人编号","所属部门", "申请人姓名","申请人证件号", "申请人福利级别","旗舰店地址","实体店面积","统一社会信用代码","企业名称","法定代表人","联系电话","法人身份证号码","经营范围","备注","业务细分","申请原因","申请时间","最后审批时间"};
            	 String[] fieldNames = {"applycode","ucode", "branch", "name","identity", "welfaregrade","bankaddress","storeArea","shopLicense","enterpriseName","legaler","distributorPhone","legalerIdCard","scopeBusiness","note", "applytype","reason","applytime","audittime"};
             	 export(page,response,request,headers,fieldNames,fileName);
           	   }
           }else if(detaliId.equals("14")){//代理商协议申请
        	   List<OnLineInfoDTO> dataset = (List<OnLineInfoDTO>) page.getResult();
        	   OnLineInfoDTO onLineInfoDTO = dataset.get(0);
           	   String assignee = onLineInfoDTO.getAssignee();
           	   if(StringUtils.isNotBlank(assignee)){
           		 String[] headers = {"受理编号","申请人编号","所属部门", "申请人姓名","申请人证件号", "申请人福利级别","注册地址","统一社会信用代码","企业名称","法定代表人","联系电话","法人身份证号码","经营范围","代理区域","对公账户行号","企业性质","开户行","开户名","银行账号","业务细分","申请原因","申请时间","最后审批时间","审批人"};
             	 String[] fieldNames = {"applycode","ucode", "branch", "name","identity", "welfaregrade","bankaddress","shopLicense","enterpriseName","legaler","distributorPhone","legalerIdCard","scopeBusiness","note","publicAccount","accountType","openingBank","openingName","accountNumbr", "applytype","reason","applytime","audittime","assignee"};
             	 export(page,response,request,headers,fieldNames,fileName);
           	   }else{
           		 String[] headers = {"受理编号","申请人编号","所属部门", "申请人姓名","申请人证件号", "申请人福利级别","注册地址","统一社会信用代码","企业名称","法定代表人","联系电话","法人身份证号码","经营范围","代理区域","对公账户行号","企业性质","开户行","开户名","银行账号","业务细分","申请原因","申请时间","最后审批时间"};
            	 String[] fieldNames = {"applycode","ucode", "branch", "name","identity", "welfaregrade","bankaddress","shopLicense","enterpriseName","legaler","distributorPhone","legalerIdCard","scopeBusiness","note","publicAccount","accountType","openingBank","openingName","accountNumbr", "applytype","reason","applytime","audittime"};
             	 export(page,response,request,headers,fieldNames,fileName);
           	   }
           }
         }else if(detaliId.length() != 0 && formName.equals("operation/process/ExchangeApplyForm")){//常规换货
        	 List<Exchange> dataset = (List<Exchange>) page.getResult();
        	 Exchange exchange = dataset.get(0);
         	 String assignee = exchange.getAssignee();
         	 String fileName = typeName + formatter.format(new Date()) + ".xls";
         	 if(StringUtils.isNotBlank(assignee)){
         		 String[] headers = {"受理编号","所属仓库","换货日期", "客服编号","经销商编号", "姓名","电话","订单单据号","订货时间","手续费","原收货人","原收货电话","原收货地址","邮编","换货原因","收货地址","收货人","收货电话","手动输入受理单号","审批人"};
             	 String[] fieldNames = {"applyCode","wareHouse", "exchangeDate", "empNo","ucode", "name","tel","orderNumber", "orderTime","payType","oldConsignee","oldConsigneeTel","oldConsigneeAddress","zipCode","exchangeReason","newConsigneeAddress","newConsignee","newConsigneeTel","inputApplyCode","assignee"};
             	 export(page,response,request,headers,fieldNames,fileName);
         	 }else{
         		 String[] headers = {"受理编号","所属仓库","换货日期", "客服编号","经销商编号", "姓名","电话","订单单据号","订货时间","手续费","原收货人","原收货电话","原收货地址","邮编","换货原因","收货地址","收货人","收货电话","手动输入受理单号"};
             	 String[] fieldNames = {"applyCode","wareHouse", "exchangeDate", "empNo","ucode", "name","tel","orderNumber", "orderTime","payType","oldConsignee","oldConsigneeTel","oldConsigneeAddress","zipCode","exchangeReason","newConsigneeAddress","newConsignee","newConsigneeTel","inputApplyCode"};
             	 export(page,response,request,headers,fieldNames,fileName);
         	 }
        	 
         }else if(detaliId.length() != 0 && (formName.equals("operation/quality-exchange-goods")||formName.equals("operation/process/QualityProblemExchangeApplyForm"))){//质量换货
        	 List<Exchange> dataset = (List<Exchange>) page.getResult();
        	 Exchange exchange = dataset.get(0);
         	 String assignee = exchange.getAssignee();
         	 String fileName = "质量问题换货" + formatter.format(new Date()) + ".xls";
         	if(StringUtils.isNotBlank(assignee)){
        		 String[] headers = {"受理编号", "经销商编号","经销商姓名","福利级别","级别","体系","销售人","服务人","注册时间","产品问题/换货原因","业务类型","业务细分","业务级别","联系电话","联系地址","所属大区","所属仓库（旧）","客服编号（旧）","订货时间（旧）","申请换货时间（旧）","订单单据号（旧）","收货地址（旧）","审批人"};
            	 String[] fieldNames = {"applyCode","ucode","name","welfare","level","system","varFather","varRe","addTime","exchangeReason","businessType","businessDetail","businessLevel","tel","address","area","wareHouse","empNo","orderTime","exchangeDate","orderNumber","newConsigneeAddress", "assignee"};
            	 export(page,response,request,headers,fieldNames,fileName);
        	 }else{
        		 String[] headers = {"受理编号", "经销商编号","经销商姓名","福利级别","级别","体系","销售人","服务人","注册时间","产品问题/换货原因","业务类型","业务细分","业务级别","联系电话","联系地址","所属大区","所属仓库（旧）","客服编号（旧）","订货时间（旧）","申请换货时间（旧）","订单单据号（旧）","收货地址（旧）"};
            	 String[] fieldNames = {"applyCode","ucode","name","welfare","level","system","varFather","varRe","addTime","exchangeReason","businessType","businessDetail","businessLevel","tel","address","area","wareHouse","empNo","orderTime","exchangeDate","orderNumber","newConsigneeAddress"};
            	 export(page,response,request,headers,fieldNames,fileName);
        	 }
         }else {
        	 List<UnfinishProcessInstance> dataset = (List<UnfinishProcessInstance>) page.getResult();
        	 UnfinishProcessInstance unfinishProcessInstance = dataset.get(0);
        	 String assignee = unfinishProcessInstance.getAssignee();
        	 String fileName = typeName + formatter.format(new Date()) + ".xls";
        	 String[] headers = null;
        	 String[] fieldNames = null;
        	 //该分支条件是导出列表数据，再有参数typeName确定是导出哪个列表
        	 if("办结流程_".equals(typeName) || "未结流程_".equals(typeName)){
        		 headers = new String[]{"受理单号", "主题","申请人","状态", "经销商编号", "业务类型", "业务细分", "提交次数","申请时间", "最后审批时间"};
        		 fieldNames = new String[]{"applyCode", "theme","applyUserName", "status", "ucode", "businessTypeName", "businessDetailName", "submitTimes","startTime", "completeTime"};
            	 
        	 }else if("经我审批_".equals(typeName)){
        		 headers = new String[]{"申请时间", "最后审批时间","审核时长","受理单号", "状态", "主题","申请人","审批人", "经销商编号", "业务类型", "业务细分", "所属体系", "所属大区", "所属分公司"};
        		 fieldNames = new String[]{"startTime", "completeTime","auditDuration","applyCode","status", "theme", "applyUserName","assignee",  "ucode", "businessTypeName", "businessDetailName", "systemName", "areaName", "companyName"};
        	 }else if("抄送审批_".equals(typeName)){
        		 headers = new String[]{"受理单号","主题","申请人", "经销商编号", "业务类型", "业务细分", "所属体系", "所属大区", "所属分公司","抄送时间","申请时间", "最后审批时间"};
        		 fieldNames = new String[]{"applyCode", "theme", "applyUserName",  "ucode", "businessTypeName", "businessDetailName", "systemName", "areaName", "companyName","ccTime","startTime", "completeTime"};
        	 }else if("定制审批_".equals(typeName)||"申请查询_".equals(typeName)||"审批查询_".equals(typeName)){
        		 headers = new String[]{"受理单号","状态","主题","申请人", "经销商编号", "业务类型", "业务细分", "所属体系", "所属大区", "所属分公司","申请时间", "最后审批时间"};
        		 fieldNames = new String[]{"applyCode","status", "theme", "applyUserName",  "ucode", "businessTypeName", "businessDetailName", "systemName", "areaName", "companyName","startTime", "completeTime"};
        	 }else if("管理审批_".equals(typeName)){
        		 headers = new String[]{"受理单号","状态","主题","申请人","申请时间", "最后审批时间", "经销商编号", "业务类型", "业务细分", "所属体系", "所属大区", "所属分公司"};
        		 fieldNames = new String[]{"applyCode","status", "theme", "applyUserName","startTime", "completeTime", "ucode", "businessTypeName", "businessDetailName", "systemName", "areaName", "companyName"};
        	 }else{
        		 headers = new String[]{"受理单号","状态","主题","申请人", "经销商编号", "业务类型", "业务细分", "所属体系", "所属大区", "所属分公司","申请时间", "最后审批时间"};
        		 fieldNames = new String[]{"applyCode","status", "theme", "applyUserName",  "ucode", "businessTypeName", "businessDetailName", "systemName", "areaName", "companyName","startTime", "completeTime"};
        	 }

        	 if (request.getHeader("User-Agent").toUpperCase().indexOf("MSIE") > 0) {
        		 fileName = URLEncoder.encode(fileName, "UTF-8");
        	 } else {
        		 fileName = new String(fileName.getBytes(), "ISO8859-1");
        	 }
        	 response.setContentType("application/vnd.ms-excel;charset=utf-8");
        	 response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
        	 OutputStream out = response.getOutputStream();
        	 try {
        		 ExcelExport.exportExcel(headers, fieldNames, dataset, out);
        	 }catch (Exception e) {
        		 e.printStackTrace();
        	 }
        	 out.flush();
        	 out.close();
         }
    }
    public static void export(Page page, HttpServletResponse response, HttpServletRequest request,String[] headers,String[] fieldNames,String fileName) throws IOException{
    	List<UnfinishProcessInstance> dataset = (List<UnfinishProcessInstance>) page.getResult();
        /*if (request.getHeader("User-Agent").toUpperCase().indexOf("MSIE") > 0) {
            fileName = URLEncoder.encode(fileName, "UTF-8");
        } else {
            fileName = new String(fileName.getBytes(), "ISO8859-1");
        }*/
        // 设置response参数，可以打开下载页面
        response.setContentType("application/vnd.ms-excel;charset=utf-8");
	    response.setHeader("Content-Disposition", "attachment; filename=" + new String(fileName.getBytes("gbk"), "iso8859-1"));
        OutputStream out = response.getOutputStream();
        try {
			ExcelExport.exportExcel(headers, fieldNames, dataset, out);
		} catch (Exception e) {
			e.printStackTrace();
		}
        out.flush();
        out.close();
    }
    //考勤专用导出方法
    public static void export(List result,HttpServletResponse response, HttpServletRequest request,String year,String month){
    	String fileName = "考勤表" + formatter.format(new Date()) + ".xls";
     	try {
			excelExport.exportExcel(request,response,result,fileName,year,month);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    //管理者查询审批列表的导出
    public static void exportManage(Page page,HttpServletResponse response, HttpServletRequest request){
    	String fileName = "管理审批表" + formatter.format(new Date()) + ".xls";
    	String[] headers = {"受理单号","申请人","状态","主题", "经销商编号", "业务类型", "业务细分", "所属体系", "所属大区", "所属分公司", "申请时间", "最后审批时间", "环节"};
        String[] fieldNames = {"applyCode","applyUserName","status","theme","ucode", "businessTypeName", "businessDetailName", "systemName", "areaName", "companyName", "startTime", "completeTime", "whole"};
        try {
			export(page,response,request,headers,fieldNames,fileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    public static void exportReport(List<Map<String,Object>> list,HttpServletResponse response, HttpServletRequest request,String fileName,String []headers,String []fieldNames,UserConnector userConnector,JdbcTemplate jdbcTemplate){
        try {
			ExcelExport.exportExcel(fileName,headers, fieldNames, list, response,userConnector,jdbcTemplate);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }


    /**
     * 导出考勤统计
     * @param response
     * @param request
     * @param partyEntityId
     * @param startTime
     * @param endTime
     * @throws Exception 
     */
	public static void exportAttendanceStatistics(HttpServletResponse response,
			HttpServletRequest request,List<Map<String,Object>> resultData) throws Exception {
		String fileName = "考勤统计表" + formatter.format(new Date()) + ".xls";
		String[] headers = {"机构","工号","姓名","加班", "病假", "事假", "年假", "婚假", "产假", "丧假", "补休假", "到倒休假", "销假", "其他"};
        String[] fieldNames = {"orgName","userCode","userName","overTime", "sickLeave", "absenceLeave", "annualLeave", "maritalLeave", "maternityLeave", "funeralLeave", "breakOffLeave", "vacationsLeave", "backLeave","other"};
		
		OutputStream out = response.getOutputStream();
	  	/*if (request.getHeader("User-Agent").toUpperCase().indexOf("MSIE") > 0) {
	        fileName = URLEncoder.encode(fileName, "UTF-8");
	    } else {
	        fileName = new String(fileName.getBytes(), "ISO8859-1");
	    }*/
	  	/*String userAgent = request.getHeader("USER-AGENT");
	  	if(StringUtils.contains(userAgent, "MSIE")){//IE浏览器
	  		fileName = URLEncoder.encode(fileName,"UTF8");
        }else if(StringUtils.contains(userAgent, "Mozilla")){//google,火狐浏览器
        	fileName = new String(fileName.getBytes(), "ISO8859-1");
        	//fileName = URLEncoder.encode(fileName,"UTF8");
        }else{
        	fileName = URLEncoder.encode(fileName,"UTF8");//其他浏览器
        }*/
	  	

	    // 设置response参数，可以打开下载页面
	    response.setContentType("application/vnd.ms-excel;charset=utf-8");
	    //response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
	    response.setHeader("Content-Disposition", "attachment; filename=" + new String(fileName.getBytes("gbk"), "iso8859-1"));
	    String title = "导出EXCEL文档";
	    // 声明一个工作薄
	    HSSFWorkbook workbook= new HSSFWorkbook();
	    //创建单元格格式
	    HSSFCellStyle style =workbook.createCellStyle();
	    style.setAlignment(HSSFCellStyle.ALIGN_CENTER);//左右居中      
	    style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);//上下居中  
	    // 生成一个表格
	    HSSFSheet sheet = workbook.createSheet(title);
	    // 设置表格默认列宽度为10个字节
	    sheet.setDefaultColumnWidth(10);
	    sheet.setDefaultRowHeight((short)400);
	    sheet.setColumnWidth(1, 5000);//工号列数据较长
	    //产生表格标题行
	    HSSFRow row = sheet.createRow(0);
	    for (short i = 0; i < headers.length; i++) {
	        HSSFCell cell = row.createCell(i);
	        HSSFRichTextString text = new HSSFRichTextString(headers[i]);
	        cell.setCellStyle(style);//使用样式
	        cell.setCellValue(text);
	    }
	    
	    for (int i = 0; i < resultData.size(); i++) {
	    	row = sheet.createRow(i+1);
	    	Map<String, Object> map = resultData.get(i);
	    	for (int j = 0; j < fieldNames.length; j++) {
	    		HSSFCell cell = row.createCell(j);
	    		Object object = map.get(fieldNames[j]);
	    		String resultStr = StringUtil.toString(object);
	    		//if(StringUtils.isNoneBlank(resultStr)){
	    			cell.setCellStyle(style);//使用样式
	    			cell.setCellValue(resultStr);
	    		/*}else{
	    			cell.setCellStyle(style);//使用样式
	    			cell.setCellValue(resultStr);
	    		}*/
			}
		}
        workbook.write(out);
        out.flush();
        out.close();
	}
	/**
	 * 工资模块导出(公积金)
	 * @param response
	 * @param request
	 * @param resultData
	 * @throws Exception
	 */
	public static void exportAccumulationFund(HttpServletResponse response,
			HttpServletRequest request,List<PersonSalaryAccumulationFund> resultData,String startDate,String endDate,Map<String, Object> infoByContractCompanyId) throws Exception {
		String fileName = "公积金扣款明细" + formatter.format(new Date()) + ".xls";
		String[] headers1 = {"月份","姓名","身份证号", "缴存基数", "单位","","个人","","合计"};
		String[] headers2 = {"","","","","比例","金额","比例","金额"};
        String[] fieldNames = {"accumulationFundDate","personName","idcardNum","accumulationFundBaseMoney","accumulationFundCompanyProportion","accumulationFundCompanyMoney","accumulationFundPersonalProportion","accumulationFundPersonalMoney","totalMoney"};
        OutputStream out = response.getOutputStream();
        // 设置response参数，可以打开下载页面
	    response.setContentType("application/vnd.ms-excel;charset=utf-8");
	    response.setHeader("Content-Disposition", "attachment; filename=" + new String(fileName.getBytes("gbk"), "iso8859-1"));
	    String title = "导出EXCEL文档";
	    // 声明一个工作薄
	    HSSFWorkbook workbook= new HSSFWorkbook();
	  //创建标题格式
	    HSSFCellStyle titleStyle =workbook.createCellStyle();
	    titleStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);//左右居中      
	    titleStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);//上下居中
	    //创建列头格式
	    HSSFCellStyle headStyle =workbook.createCellStyle();
	    headStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);//左右居中      
	    headStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);//上下居中 
	    //创建单元格格式
	    HSSFCellStyle style =workbook.createCellStyle();
	    style.setAlignment(HSSFCellStyle.ALIGN_CENTER);//左右居中      
	    style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);//上下居中  
	    // 生成一个表格
	    HSSFSheet sheet = workbook.createSheet(title);
	    // 设置表格默认列宽度为10个字节
	    sheet.setDefaultColumnWidth(10);
	    sheet.setDefaultRowHeight((short)400);
	    sheet.setColumnWidth(2, 5000);//针对身份证号列加长
	    //产生表格标题行
	    HSSFRow row = sheet.createRow(0);
	    row.setHeightInPoints(35);
	    HSSFCell titleCell = row.createCell(0);
	   
	    //设置列头字体
        HSSFFont titleFont=workbook.createFont();
        titleFont.setColor(HSSFColor.BLACK.index);//HSSFColor.VIOLET.index //字体颜色
        titleFont.setFontHeightInPoints((short)16);
        titleFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);         //字体增粗
        //把字体应用到当前的样式
        titleStyle.setFont(titleFont);
	    titleCell.setCellStyle(titleStyle);//使用样式
	    String excelTitle = "";
	    Object company = infoByContractCompanyId.get("contract_company_name");
	    if(startDate.equals(endDate)){
    		excelTitle =company+"单位"+ startDate+"公积金明细";
    	}else{
    		excelTitle = company+"单位"+startDate+"~"+endDate+"公积金明细";
    	}
	    titleCell.setCellValue(excelTitle);
	    sheet.addMergedRegion(new CellRangeAddress(0,0,0,8));//将首列合并
	    row = sheet.createRow(1);
	    row.setHeightInPoints(25);
	    //设置标题字体
        HSSFFont headFont=workbook.createFont();
        headFont.setColor(HSSFColor.BLACK.index);
        headFont.setFontHeightInPoints((short)10);
        headFont.setBold(true);
        headStyle.setFont(headFont);
	    for (short i = 0; i < headers1.length; i++) {
	    	if(i < 4){
	    		sheet.addMergedRegion(new CellRangeAddress(1,2,i,i));//员工~基数跨两行
	    	}else if(i == 8){
	    		sheet.addMergedRegion(new CellRangeAddress(1,2,i,i));//合计跨两行
	    	}else if(i == 4){
	    		sheet.addMergedRegion(new CellRangeAddress(1,1,i,i+1));//单位和个人跨两列
	    	}else if(i == 6){
	    		sheet.addMergedRegion(new CellRangeAddress(1,1,6,7));//单位和个人跨两列
	    	}
	    	HSSFCell cell = null;
	    	if(i == 5 || i == 7){
	    		continue;
	    	}else{
	    		cell = row.createCell(i);
	    	}
	        
	        HSSFRichTextString text = new HSSFRichTextString(headers1[i]);
	        cell.setCellStyle(headStyle);//使用样式
	        cell.setCellValue(text);
	    }
	    row = sheet.createRow(2);
	    for(short j = 0;j < headers2.length;j++){
	    	if(j < 4){
	    		continue;
	    	}
	    	HSSFCell cell = row.createCell(j);
	        HSSFRichTextString text = new HSSFRichTextString(headers2[j]);
	        cell.setCellStyle(headStyle);//使用样式
	        cell.setCellValue(text);
	    }
	    
	    
	   Iterator it = resultData.iterator();
	   int index = 2;
	   while(it.hasNext()){
	    	index++;
            row = sheet.createRow(index);
            row.setHeightInPoints(20);
            Object obj =  it.next();
            //如果没有传fileNames，则利用反射，根据javabean属性的先后顺序，动态调用getXxx()方法得到属性值
            if (fieldNames == null) {
                Field[] fields = obj.getClass().getDeclaredFields();
                for (short i = 0; i < fields.length; i++) {

                    HSSFCell cell = row.createCell(i);
                    Field field = fields[i];
                    String fieldName = field.getName();
                    cell.setCellStyle(style);
                    writeValue(workbook, cell, obj, fieldName);
                }
            } else {
                //遍历fieldNams，写到cell里
                for (short i = 0; i < fieldNames.length; i++) {
                    HSSFCell cell = row.createCell(i);
                    String fieldName = fieldNames[i];
                    cell.setCellStyle(style);
                    writeValue(workbook, cell,obj, fieldName);
                }
            }
	    }
        workbook.write(out);
        out.flush();
        out.close();
	}
	/**
	 * @param response
	 * @param request
	 * @param resultData
	 * @param formatSalaryColumn
	 * @param startDate
	 * @param endDate
	 * @param dictInfoList
	 * @throws Exception
	 * @author sjx
	 * 导出基本工资
	 */
	public static void exportPersonSalaryBase(HttpServletResponse response,HttpServletRequest request,
			List<PersonSalaryBase> resultData,String[] formatSalaryColumn,
			String startDate,String endDate,String[] salaryColumn,Map<String, Object> infoByContractCompanyId) throws Exception {
		String fileName = "工资基本表" + formatter.format(new Date()) + ".xls";
		List<String> headers1 = new ArrayList<>();
		List<String> headers2 = new ArrayList<>();
		headers1.add("月份");
		headers1.add("工号");
		headers1.add("姓名");
		headers1.add("薪资单位");
		headers1.add("身份证号");
		headers2.add("");
		headers2.add("");
		headers2.add("");
		headers2.add("");
		headers2.add("");
		String[] split = null;
		if (formatSalaryColumn != null) {
			String string = formatSalaryColumn[formatSalaryColumn.length-1];
			split = string.split(",");
			boolean boo = false;
			String globalType = "";
			for(int i=0;i<formatSalaryColumn.length-1;i++){//该数组最后一个元素不做遍历，只用作逻辑运算使用
				if(formatSalaryColumn[i].substring(0, 1).contains("r")){
					int indexOf = formatSalaryColumn[i].indexOf("-");
					headers1.add(formatSalaryColumn[i].substring(1, indexOf));
					headers2.add("");
				}else{
					int lastIndexOf = formatSalaryColumn[i].lastIndexOf("|");
					int indexOf = formatSalaryColumn[i].lastIndexOf("-");
					String termName = formatSalaryColumn[i].substring(1, indexOf);
					String type = formatSalaryColumn[i].substring(lastIndexOf);//获取一级列头分类
					if("".equals(globalType)){
						globalType = type;
					}
					if(!"".equals(globalType)&&!globalType.equals(type)){
						boo = false;
						globalType = type;
					}
					if("|paid".equals(type)){//带薪假期
						if(boo == false){
							headers1.add("带薪假期/天");
							boo = true;
						}else{
							headers1.add("");
						}
						headers2.add(termName);
					}else if("|overtime".equals(type)){
						if(boo == false){
							headers1.add("加班/天");
							boo = true;
						}else{
							headers1.add("");
						}
						headers2.add(termName);
					}else if("|deductionLeave".equals(type)){
						if(boo == false){
							headers1.add("扣款假期/天");
							boo = true;
						}else{
							headers1.add("");
						}
						headers2.add(termName);
					}else if("|miner".equals(type)){
						if(boo == false){
							headers1.add("旷工/天");
							boo = true;
						}else{
							headers1.add("");
						}
						headers2.add(termName);
					}else if("|welfare".equals(type)){
						if(boo == false){
							headers1.add("福利补贴");
							boo = true;
						}else{
							headers1.add("");
						}
						headers2.add(termName);
					}else if("|deductionMoney".equals(type)){
						if(boo == false){
							headers1.add("扣款/元");
							boo = true;
						}else{
							headers1.add("");
						}
						headers2.add(termName);
					}else if("|insurance".equals(type)){
						if(boo == false){
							headers1.add("保险公积金代扣款");
							boo = true;
						}else{
							headers1.add("");
						}
						headers2.add(termName);
					}else if("|special".equals(type)){
						if(boo == false){
							headers1.add("专项附加扣除项目");
							boo = true;
						}else{
							headers1.add("");
						}
						headers2.add(termName);
					}
				}
			}
		}
		List<String> fieldNames = new ArrayList<>();
		fieldNames.add("salaryDate");
		fieldNames.add("employeeNo");
		fieldNames.add("personName");
		fieldNames.add("contractCompanyName");
		fieldNames.add("idcardNum");
		if (salaryColumn != null) {
			for(int k=0;k<salaryColumn.length;k++){
				int index = salaryColumn[k].indexOf("-");
				String field = salaryColumn[k].substring(0, index);
				fieldNames.add(field);
			}
		}
        OutputStream out = response.getOutputStream();
        // 设置response参数，可以打开下载页面
	    response.setContentType("application/vnd.ms-excel;charset=utf-8");
	    response.setHeader("Content-Disposition", "attachment; filename=" + new String(fileName.getBytes("gbk"), "iso8859-1"));
	    String title = "导出EXCEL文档";
	    // 声明一个工作薄
	    HSSFWorkbook workbook= new HSSFWorkbook();
	    //创建标题格式
	    HSSFCellStyle titleStyle =workbook.createCellStyle();
	    titleStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);//左右居中      
	    titleStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);//上下居中 
	    //创建列头格式
	    HSSFCellStyle headStyle =workbook.createCellStyle();
	    headStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);//左右居中      
	    headStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);//上下居中 
	    //创建单元格格式
	    HSSFCellStyle style =workbook.createCellStyle();
	    style.setAlignment(HSSFCellStyle.ALIGN_CENTER);//左右居中      
	    style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);//上下居中  
	    // 生成一个表格
	    HSSFSheet sheet = workbook.createSheet(title);
	    // 设置表格默认列宽度为10个字节
	    sheet.setDefaultColumnWidth(10);
	    sheet.setDefaultRowHeight((short)400);
	    sheet.setColumnWidth(4, 5000);//针对身份证号列加长
	    //产生表格标题行
	    HSSFRow row = sheet.createRow(0);
	    row.setHeightInPoints(35);
	    HSSFCell titleCell = row.createCell(0);
	   
	    //设置标题字体
        HSSFFont titleFont=workbook.createFont();
        titleFont.setColor(HSSFColor.BLACK.index);//HSSFColor.VIOLET.index //字体颜色
        titleFont.setFontHeightInPoints((short)16);
        titleFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);         //字体增粗
        //把字体应用到当前的样式
        titleStyle.setFont(titleFont);
	    titleCell.setCellStyle(titleStyle);//使用样式
	    String excelTitle = "";
	    Object company = infoByContractCompanyId.get("contract_company_name");
	    if(startDate.equals(endDate)){
    		excelTitle =company+"单位"+ startDate+"工资基本表";
    	}else{
    		excelTitle = company+"单位"+startDate+"~"+endDate+"工资基本表";
    	}
	    titleCell.setCellValue(excelTitle);
	    if(formatSalaryColumn != null){
	    	sheet.addMergedRegion(new CellRangeAddress(0,0,0,formatSalaryColumn.length+3));//将首列合并
	    }else{
	    	sheet.addMergedRegion(new CellRangeAddress(0,0,0,4));//将首列合并
	    }
	    
	    row = sheet.createRow(1);
	    row.setHeightInPoints(25);
	    //设置列头字体
        HSSFFont headFont=workbook.createFont();
        headFont.setColor(HSSFColor.BLACK.index);
        headFont.setFontHeightInPoints((short)10);
        headFont.setBold(true);
        headStyle.setFont(headFont);
	    for (short i = 0; i < headers1.size(); i++) {
	    	//String header1Str = headers1.get(i);
	    	if(!headers1.get(i).equals("")
	    	  &&(!headers1.get(i).equals("带薪假期/天")&&!headers1.get(i).equals("加班/天")&&!headers1.get(i).equals("扣款假期/天"))
	    	  &&!headers1.get(i).equals("旷工/天")&&!headers1.get(i).equals("福利补贴")&&!headers1.get(i).equals("扣款/元")
	    	  &&!headers1.get(i).equals("保险公积金代扣款")&&!headers1.get(i).equals("专项附加扣除项目")){
	    		sheet.addMergedRegion(new CellRangeAddress(1,2,i,i));
	    	}else if(headers1.get(i).equals("带薪假期/天")){
	    		sheet.addMergedRegion(new CellRangeAddress(1,1,i,i-1+Integer.parseInt(split[0])));
	    	}else if(headers1.get(i).equals("加班/天")){
	    		sheet.addMergedRegion(new CellRangeAddress(1,1,i,i-1+Integer.parseInt(split[1])));
	    	}else if(headers1.get(i).equals("扣款假期/天")){
	    		sheet.addMergedRegion(new CellRangeAddress(1,1,i,i-1+Integer.parseInt(split[2])));
	    	}else if(headers1.get(i).equals("旷工/天")){
	    		sheet.addMergedRegion(new CellRangeAddress(1,1,i,i-1+Integer.parseInt(split[3])));
	    	}else if(headers1.get(i).equals("福利补贴")){
	    		sheet.addMergedRegion(new CellRangeAddress(1,1,i,i-1+Integer.parseInt(split[4])));
	    	}else if(headers1.get(i).equals("扣款/元")){
	    		sheet.addMergedRegion(new CellRangeAddress(1,1,i,i-1+Integer.parseInt(split[5])));
	    	}else if(headers1.get(i).equals("保险公积金代扣款")){
	    		sheet.addMergedRegion(new CellRangeAddress(1,1,i,i-1+Integer.parseInt(split[6])));
	    	}else if(headers1.get(i).equals("专项附加扣除项目")){
	    		sheet.addMergedRegion(new CellRangeAddress(1,1,i,i-1+Integer.parseInt(split[7])));
	    	}
	    	
	    	HSSFCell cell = row.createCell(i);
	        HSSFRichTextString text = new HSSFRichTextString(headers1.get(i));
	        cell.setCellStyle(headStyle);//使用样式
	        cell.setCellValue(text);
	    }
	    row = sheet.createRow(2);
	    for(short j = 0;j < headers2.size();j++){
	    	if("".equals(headers2.get(j))){
	    		continue;
	    	}
	    	HSSFCell cell = row.createCell(j);
	        HSSFRichTextString text = new HSSFRichTextString(headers2.get(j));
	        cell.setCellStyle(headStyle);//使用样式
	        cell.setCellValue(text);
	    }
	    
	   Iterator it = resultData.iterator();
	   int index = 2;
	   while(it.hasNext()){
	    	index++;
            row = sheet.createRow(index);
            row.setHeightInPoints(20);
            Object obj =  it.next();
            //如果没有传fileNames，则利用反射，根据javabean属性的先后顺序，动态调用getXxx()方法得到属性值
            if (fieldNames == null) {
                Field[] fields = obj.getClass().getDeclaredFields();
                for (short i = 0; i < fields.length; i++) {

                    HSSFCell cell = row.createCell(i);
                    Field field = fields[i];
                    String fieldName = field.getName();
                    cell.setCellStyle(style);
                    writeValue(workbook, cell, obj, fieldName);
                }
            } else {
                //遍历fieldNams，写到cell里
                for (short i = 0; i < fieldNames.size(); i++) {
                    HSSFCell cell = row.createCell(i);
                    String fieldName = fieldNames.get(i);
                    cell.setCellStyle(style);
                    writeValue(workbook, cell,obj, fieldName);
                }
            }
	    }
        workbook.write(out);
        out.flush();
        out.close();
	}
	
	/**
	 * 导出工资条
	 * @param response
	 * @param request
	 * @param resultData
	 * @throws Exception
	 * @author sjx
	 */
	public static void exportPersonSalarySlip(HttpServletResponse response,
			HttpServletRequest request,List<PersonSalaryBase> resultData,String startDate,String endDate,Map<String, Object> infoByContractCompanyId) throws Exception {
		String fileName = "工资条" + formatter.format(new Date()) + ".xls";
		String[] headers1 = {"工号","姓名","应出勤","实际出勤", "月工资", "加班费","请假扣款","","","","","补杂项","应发工资","保险公积金扣款","","","","","","税前工资","专项附加扣除项目","","","","","","个人所得税","实发工资","备注"};
		String[] headers2 = {"","","","","","","缺勤","病假","事假","矿工","迟到早退","","","养老","失业","医疗","公积金","其他项","合计","","子女教育","继续教育","住房贷款利息","住房租金","赡养老人","商业健康险","","",""};
        String[] fieldNames = {"employeeNo","personName","allAttendanceDays","actualAttendanceDays","monthWagesMoney","overtimePayMoney","missingDeductionMoney",
        		"sickDeductionMoney","casualDeductionMoney","absentDeductionMoney","earlyLateDeductionMoney","supplementItemsMoney","allWagesMoney",
        		"socialPensionDeductionMoney","socialUnemploymentDeductionMoney","socialMedicalDeductionMoney","socialProvidentFundDeductionMoney",
        		"socialOtherDeductionMoney","socialTotalDeductionMoney","grossWagesMoney",
        		"specialChildrenEducationMoney","specialContinuEducationMoney","specialHotelInterestMoney","specialHotelRentMoney","specialSupportElderlyMoney","specialCommercialHealthInsuranceMoney",
        		"personalIncomeMoney","realWagesMoney","remark"};
        OutputStream out = response.getOutputStream();
        // 设置response参数，可以打开下载页面
	    response.setContentType("application/vnd.ms-excel;charset=utf-8");
	    response.setHeader("Content-Disposition", "attachment; filename=" + new String(fileName.getBytes("gbk"), "iso8859-1"));
	    String title = "导出EXCEL文档";
	    // 声明一个工作薄
	    HSSFWorkbook workbook= new HSSFWorkbook();
	    //创建标题格式
	    HSSFCellStyle titleStyle =workbook.createCellStyle();
	    titleStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);//左右居中      
	    titleStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);//上下居中
	    //创建列头格式
	    HSSFCellStyle headStyle =workbook.createCellStyle();
	    headStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);//左右居中      
	    headStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);//上下居中 
	    //创建单元格格式
	    HSSFCellStyle style =workbook.createCellStyle();
	    style.setAlignment(HSSFCellStyle.ALIGN_CENTER);//左右居中      
	    style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);//上下居中  
	    // 生成一个表格
	    HSSFSheet sheet = workbook.createSheet(title);
	    // 设置表格默认列宽度为10个字节
	    sheet.setDefaultColumnWidth(10);
	    sheet.setDefaultRowHeight((short)400);
	    //sheet.setColumnWidth(3, 5000);
	    //产生表格标题行
	    HSSFRow row = sheet.createRow(0);
	    row.setHeightInPoints(35);
	    HSSFCell titleCell = row.createCell(0);
	   
	    //设置列头字体
        HSSFFont titleFont=workbook.createFont();
        titleFont.setColor(HSSFColor.BLACK.index);//HSSFColor.VIOLET.index //字体颜色
        titleFont.setFontHeightInPoints((short)16);
        titleFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);         //字体增粗
        //把字体应用到当前的样式
        titleStyle.setFont(titleFont);
	    titleCell.setCellStyle(titleStyle);//使用样式
	    String excelTitle = "";
	    Object company = infoByContractCompanyId.get("contract_company_name");
	    if(startDate.equals(endDate)){
    		excelTitle =company+"单位"+ startDate+"工资条";
    	}else{
    		excelTitle = company+"单位"+startDate+"~"+endDate+"工资条";
    	}
	    titleCell.setCellValue(excelTitle);
	    sheet.addMergedRegion(new CellRangeAddress(0,0,0,28));//将首列合并
	    row = sheet.createRow(1);
	    row.setHeightInPoints(25);
	    //设置标题字体
        HSSFFont headFont=workbook.createFont();
        headFont.setColor(HSSFColor.BLACK.index);
        headFont.setFontHeightInPoints((short)10);
        headFont.setBold(true);
        headStyle.setFont(headFont);
	    for (short i = 0; i < headers1.length; i++) {
	    	if(i < 6){
	    		sheet.addMergedRegion(new CellRangeAddress(1,2,i,i));//工号~加班费
	    	}else if(i == 6){
	    		sheet.addMergedRegion(new CellRangeAddress(1,1,i,i+4));//请假扣款
	    	}else if(i == 11 || i == 12){
	    		sheet.addMergedRegion(new CellRangeAddress(1,2,i,i));
	    	}else if(i == 13){
	    		sheet.addMergedRegion(new CellRangeAddress(1,1,i,i+5));
	    	}else if(i == 19){
	    		sheet.addMergedRegion(new CellRangeAddress(1,2,i,i));
	    	}else if(i == 20){
	    		sheet.addMergedRegion(new CellRangeAddress(1,1,i,i+5));
	    	}else if(i > 25){
	    		sheet.addMergedRegion(new CellRangeAddress(1,2,i,i));
	    	}
	    	HSSFCell cell = null;
	    	if(i == 10 || i == 18 || i == 25){
	    		continue;
	    	}else{
	    		cell = row.createCell(i);
	    	}
	        
	        HSSFRichTextString text = new HSSFRichTextString(headers1[i]);
	        cell.setCellStyle(headStyle);//使用样式
	        cell.setCellValue(text);
	    }
	    row = sheet.createRow(2);
	    for(short j = 0;j < headers2.length;j++){
	    	if(j < 6 || j ==11 || j ==12 || j ==19 || j > 25){
	    		continue;
	    	}
	    	HSSFCell cell = row.createCell(j);
	        HSSFRichTextString text = new HSSFRichTextString(headers2[j]);
	        cell.setCellStyle(headStyle);//使用样式
	        cell.setCellValue(text);
	    }
	    
	    
	   Iterator it = resultData.iterator();
	   int index = 2;
	   while(it.hasNext()){
	    	index++;
            row = sheet.createRow(index);
            row.setHeightInPoints(20);
            Object obj =  it.next();
            //如果没有传fileNames，则利用反射，根据javabean属性的先后顺序，动态调用getXxx()方法得到属性值
            if (fieldNames == null) {
                Field[] fields = obj.getClass().getDeclaredFields();
                for (short i = 0; i < fields.length; i++) {

                    HSSFCell cell = row.createCell(i);
                    Field field = fields[i];
                    String fieldName = field.getName();
                    cell.setCellStyle(style);
                    writeValue(workbook, cell, obj, fieldName);
                }
            } else {
                //遍历fieldNams，写到cell里
                for (short i = 0; i < fieldNames.length; i++) {
                    HSSFCell cell = row.createCell(i);
                    String fieldName = fieldNames[i];
                    cell.setCellStyle(style);
                    writeValue(workbook, cell,obj, fieldName);
                }
            }
	    }
        workbook.write(out);
        out.flush();
        out.close();
	}
	
	/**
	 * 导出社保扣款明细
	 * @param response
	 * @param request
	 * @param resultData
	 * @throws Exception
	 */
	public static void exportPersonSalarySocialSecurity(HttpServletResponse response,
			HttpServletRequest request,List<PersonSalarySocialSecurity> resultData,String startDate,String endDate,Map<String, Object> infoByContractCompanyId) throws Exception {
		String fileName = "社保扣款明细" + formatter.format(new Date()) + ".xls";
		String[] headers1 = {"月份","姓名","身份证号", "户口性质", "养老基数","养老","","","","失业","","","","医疗保险基数","医疗","","","","工伤","","生育",""};
		String[] headers2 = {"","","","","","公司","","个人","","公司","","个人","","","公司","","个人","","公司","","公司",""};
		String[] headers3 = {"","","","","","比例","金额","比例","金额","比例","金额","比例","金额","","比例","金额","比例","金额","比例","金额","比例","金额"};
        String[] fieldNames = {"socialSecurityDate","personName","idcardNum","accountCharacte","pensionBaseMoney",
        		"pensionCompanyProportion","pensionCompanyMoney","pensionPersonalProportion","pensionPersonalMoney",
        		"unemploymentCompanyProportion","unemploymentCompanyMoney","unemploymentPersonalProportion","unemploymentPersonalMoney",
        		"medicalBaseMoney","medicalCompanyProportion","medicalCompanyMoney","medicalPersonalProportion","medicalPersonalMoney",
        		"injuryCompanyProportion","injuryCompanyMoney","birthCompanyProportion","birthCompanyMoney"};
        OutputStream out = response.getOutputStream();
        // 设置response参数，可以打开下载页面
	    response.setContentType("application/vnd.ms-excel;charset=utf-8");
	    response.setHeader("Content-Disposition", "attachment; filename=" + new String(fileName.getBytes("gbk"), "iso8859-1"));
	    String title = "导出EXCEL文档";
	    // 声明一个工作薄
	    HSSFWorkbook workbook= new HSSFWorkbook();
	    //创建标题格式
	    HSSFCellStyle titleStyle =workbook.createCellStyle();
	    titleStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);//左右居中      
	    titleStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);//上下居中
	    //创建列头格式
	    HSSFCellStyle headStyle =workbook.createCellStyle();
	    headStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);//左右居中      
	    headStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);//上下居中 
	    //创建单元格格式
	    HSSFCellStyle style =workbook.createCellStyle();
	    style.setAlignment(HSSFCellStyle.ALIGN_CENTER);//左右居中      
	    style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);//上下居中  
	    // 生成一个表格
	    HSSFSheet sheet = workbook.createSheet(title);
	    // 设置表格默认列宽度为10个字节
	    sheet.setDefaultColumnWidth(10);
	    sheet.setDefaultRowHeight((short)300);
	    sheet.setColumnWidth(2, 5000);//针对身份证号列加长
	    //产生表格标题行
	    HSSFRow row = sheet.createRow(0);
	    row.setHeightInPoints(35);
	    HSSFCell titleCell = row.createCell(0);
	   
	    //设置列头字体
        HSSFFont titleFont=workbook.createFont();
        titleFont.setColor(HSSFColor.BLACK.index);//HSSFColor.VIOLET.index //字体颜色
        titleFont.setFontHeightInPoints((short)16);
        titleFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);         //字体增粗
        //把字体应用到当前的样式
        titleStyle.setFont(titleFont);
	    titleCell.setCellStyle(titleStyle);//使用样式
	    String excelTitle = "";
	    Object company = infoByContractCompanyId.get("contract_company_name");
	    if(startDate.equals(endDate)){
    		excelTitle =company+"单位"+ startDate+"社保扣款明细";
    	}else{
    		excelTitle = company+"单位"+startDate+"~"+endDate+"社保扣款明细";
    	}
	    titleCell.setCellValue(excelTitle);
	    sheet.addMergedRegion(new CellRangeAddress(0,0,0,21));//将首列合并
	    row = sheet.createRow(1);
	    row.setHeightInPoints(25);
	    //设置标题字体
        HSSFFont headFont=workbook.createFont();
        headFont.setColor(HSSFColor.BLACK.index);
        headFont.setFontHeightInPoints((short)10);
        headFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        headStyle.setFont(headFont);
	    for (short i = 0; i < headers1.length; i++) {
	    	if(i < 5){
	    		sheet.addMergedRegion(new CellRangeAddress(1,3,i,i));//月份~养老基数
	    	}else if(i == 5){
	    		sheet.addMergedRegion(new CellRangeAddress(1,1,i,i+3));//养老
	    	}else if(i == 9){
	    		sheet.addMergedRegion(new CellRangeAddress(1,1,i,i+3));//失业
	    	}else if(i == 13){
	    		sheet.addMergedRegion(new CellRangeAddress(1,3,i,i));
	    	}else if(i == 14){
	    		sheet.addMergedRegion(new CellRangeAddress(1,1,i,i+3));
	    	}else if(i == 18){
	    		sheet.addMergedRegion(new CellRangeAddress(1,1,i,i+1));
	    	}else if(i == 20){
	    		sheet.addMergedRegion(new CellRangeAddress(1,1,i,i+1));
	    	}
	    	HSSFCell cell = null;
	    	if(i == 8 || i == 12 || i == 17 || i == 19 || i == 21){
	    		continue;
	    	}else{
	    		cell = row.createCell(i);
	    	}
	        
	        HSSFRichTextString text = new HSSFRichTextString(headers1[i]);
	        cell.setCellStyle(headStyle);//使用样式
	        cell.setCellValue(text);
	    }
	    row = sheet.createRow(2);
	    for(short j = 0;j < headers2.length;j++){
	    	if(j==5 || j==7 || j==9 || j==11 || j==14 || j==16 || j==18 || j == 20){
	    		sheet.addMergedRegion(new CellRangeAddress(2,2,j,j+1));
	    	}
	    	HSSFCell cell = null;
	    	if(j == 6 || j == 8 || j == 10 || j == 12 || j == 15 || j == 17 || j == 19 || j == 21){
	    		continue;
	    	}else{
	    		cell = row.createCell(j);
	    	}
	        HSSFRichTextString text = new HSSFRichTextString(headers2[j]);
	        cell.setCellStyle(headStyle);//使用样式
	        cell.setCellValue(text);
	    }
	    
	    row = sheet.createRow(3);
	    for(short j = 0;j < headers3.length;j++){
	    	HSSFCell cell = row.createCell(j);
	    	HSSFRichTextString text = new HSSFRichTextString(headers3[j]);
	    	cell.setCellStyle(headStyle);//使用样式
	    	cell.setCellValue(text);
	    }
	    
	   Iterator it = resultData.iterator();
	   int index = 3;
	   while(it.hasNext()){
	    	index++;
            row = sheet.createRow(index);
            row.setHeightInPoints(20);
            Object obj =  it.next();
            //如果没有传fileNames，则利用反射，根据javabean属性的先后顺序，动态调用getXxx()方法得到属性值
            if (fieldNames == null) {
                Field[] fields = obj.getClass().getDeclaredFields();
                for (short i = 0; i < fields.length; i++) {

                    HSSFCell cell = row.createCell(i);
                    Field field = fields[i];
                    String fieldName = field.getName();
                    cell.setCellStyle(style);
                    writeValue(workbook, cell, obj, fieldName);
                }
            } else {
                //遍历fieldNams，写到cell里
                for (short i = 0; i < fieldNames.length; i++) {
                    HSSFCell cell = row.createCell(i);
                    String fieldName = fieldNames[i];
                    cell.setCellStyle(style);
                    writeValue(workbook, cell,obj, fieldName);
                }
            }
	    }
        workbook.write(out);
        out.flush();
        out.close();
	}
	private static void writeValue(Workbook workbook, Cell cell, Object obj, String fieldName) {
        try {
            //1、获取字段的get方法
            String getMethodName = "get"
                    + fieldName.substring(0, 1).toUpperCase()
                    + fieldName.substring(1);
            Class tCls = obj.getClass();
            Method getMethod = tCls.getMethod(getMethodName, new Class[]{});
//            2、获取到字段值
            Object value = getMethod.invoke(obj, new Object[]{});
            if (value == null) {
                return;
            }
            //3、以下是对值进行处理 然后写到cell里
            //判断值的类型后进行强制类型转换
            String textValue = null;
            if (value instanceof Boolean) {
                boolean bValue = (Boolean) value;
                textValue = "男";
                if (!bValue) {
                    textValue = "女";
                }
            }else if (value instanceof Date) {
                Date date = (Date) value;
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                textValue = sdf.format(date);
            }else {
                //其它数据类型都当作字符串简单处理
                textValue = value.toString();
            }
            //如果不是图片数据，就利用正则表达式判断textValue是否全部由数字组成
            if (textValue != null) {
                Pattern p = Pattern.compile("^//d+(//.//d+)?$");
                Matcher matcher = p.matcher(textValue);
                if (matcher.matches()) {
                    //是数字当作double处理
                    cell.setCellValue(Double.parseDouble(textValue));
                } else {
                    HSSFRichTextString richString = new HSSFRichTextString(textValue);
                    cell.setCellValue(richString);
                }
            }
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            //清理资源
        }
    }
}
