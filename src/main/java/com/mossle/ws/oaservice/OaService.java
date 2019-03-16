package com.mossle.ws.oaservice;

import java.io.IOException;

import javax.jws.WebMethod;
import javax.jws.WebService;

/** 
 * 定义服务接口 
 * @author zyl 
 * 
 */  
@WebService
public interface OaService {
	
	//定义服务方法  
    public String sayHello(String name) throws Exception;  
    
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
    /// <param name="chrApplyType">申请类型(1：密码初始化；2：更正姓名；3：身份证更正；4：资格注销；5：直系亲属资格替换；6：更正手机号；7：开通订货及转账权限；8：12万旗舰店申请；9：非12万旗舰店申请；)</param>
    /// <param name="varReason">申请原因</param>
    /// <param name="strMsg">签名</param>
    /// <returns>返回结果</returns>
    //[WebMethod(Description = "描述 添加申请 参数：varOlCode=记录编号；varApplyCode=申请人编号；varBranch=所属部门编号； varApplyName=申请人姓名；varApplyIdentity=申请人证件号；varWelfareGrade=福利级别(名称)；varMobile=申请人手机号；varNewName=新姓名；varNewIdentity=新证件号；varBankName=新银行卡号/代理人编号；varBankAddress=新银行地址(包含省/市/区县)；varBankCode=新银行账号；varApplyPic=申请人证件；  varNewPic=新人证件；varPartPic1=营销一分部证件；varPartPic2=营销二分部证件；varPartPic3=营销三分部证件；varRelativesPic=亲属证明；chrApplyType=申请类型；varReason=申请原因")]
    public boolean CreateApply(String varOlCode, 
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
    		String strMsg);
    
    /**
     * 描述 判断在线办公申请是否存在处理中的申请数据 参数：varApplyCode=申请人编号；chrApplyType=申请类型
     * **/
    public boolean CheckApplyStatus(String varAapplyCode, String chrApplyType, String strMsg);
    
    /**
     * 读取申请编号
     * @param varUserID 申请人(代理人)编号
     * @param chrApplyType 申请类型 1密码初始化 2更正姓名 3身份证更正 4资格注销 5直系亲属资格替换 6更正手机号 7开通订货及转账权限 8 12万旗舰店 9非12万旗舰店
     * @param strMsg 签名
     * @return 空值：数据异常  非空：编号
     * **/
    public String ReadApplyCode(String varUserID, String chrApplyType, String strMsg);
    
    
    /**
     * 直销根据申请人(代理人)编号和类型查询在线办公申请记录
     * @param varApplyCode 申请人(代理人)编号
     * @param chrApplyType 申请类型 1密码初始化 2更正姓名 3身份证更正 4资格注销 5直系亲属资格替换 6更正手机号 7开通订货及转账权限 8 12万旗舰店 9非12万旗舰店
     * @param strPageSize 页大小
     * @param strPageIndex 页码
     * @param strMsg 签名
     * @return 返回JSON
     * **/
    public String PagesSearchApply(
    		String varApplyCode, 
    		String chrApplyType, 
    		String strPageSize, 
    		String strPageIndex,
    		String strMsg) throws IOException;
    
    /**
     * 获取申请记录详情
     * @param strOLID 主键ID
     * @param strMsg 签名
     * @return 返回JSON
     * **/
    public String SearchApplyDetail(String strOLID, String strMsg) throws IOException;
    
    /**
     * 直销验证是否存在已审核密码初始化申请
     * @param strApplyCode 申请人编号
     * @param strStartTime 起始时间
     * @param strMsg 签名
     * @return 返回数值
     * **/
    public int SellValidPwdInit(String strApplyCode,String strBranch, String strStartTime, String strMsg);
        
    /**
     * 取消最后一次旗舰店申请
     * @param strApplyCode 申请人编号
     * @param varBranch 所属部门编号
     * @param chrApplyType 申请类型
     * @param strMsg 签名
     * @return 返回布尔类型
     * **/
    public boolean StopApply(String varApplyCode, String varBranch, String chrApplyType, String strMsg);
    
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
    public String ApplyUploadFile(
    		String strFRCode, 
    		String strImages,
    		String strUserID,
    		String strReceive,
    		String strMobile,
    		String strAddress, 
    		String strMsg);
    
    //region 给品质365的接口
    /**
     * 金卡商品推荐-添加或更新申请信息
     * @param strFRCode 申请编号(注：添加时，可为空；更新时，必填)
     * @param strUserID 申请用户编号
     * @param strOperateTime 申请时间(注：格式20170505122155)
     * @param strContractNum 合同编号(注：添加时，可为空；更新合同编号必填)
     * @param strComName 收件人电话
     * @param strAddress 公司名称
     * @param strActionType 操作类型(1.添加 2重新提交 3更新邮寄状态 4上传打款凭证)
     * @param strMsg 签名
     * @return 返回字符串，ok成功 其他失败
     * **/
    public String ToPinZhiAddApply(
    		String strFRCode, 
    		String strUserID, 
    		String strOperateTime, 
    		String strContractNum,
    		String strComName, 
    		String strActionType, 
    		String strMsg) throws IOException;
    
    /**
     * 金卡商品推荐-查询状态及备注信息
     * @param strFRCode 申请编号
     * @param strMsg 签名
     * @return 返回字符串，ok成功 其他失败
     * **/
    public String ToPinZhiQueryStatus(String strFRCode, String strMsg)  throws IOException;
    
    
    //region 用于导入数据调用的接口方法
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
    		) throws Exception;
    
    public String ApplyUploadFileForImportData(
    		String strFRCode, 
    		String strImages,
    		String strUserID,
    		String strReceive,
    		String strMobile,
    		String strAddress,
    		String strApplyDate,
    		String strAuditDate,
    		String strMsg);
    
    public String SearchApplyDetailForImportData(String strOLID, String strMsg) throws IOException;
    
    /**
     * 
     * */
    public String ApplyAuditForImportData(
    		String varOlCode,
    		String strAuditors,
    		String strApplyDate,
    		String strAuditDate,
    		String strAuditNote,
    		String strAuditType,
    		String strMsg
    		) throws Exception;
    //endregion
    
    //endregion
}  