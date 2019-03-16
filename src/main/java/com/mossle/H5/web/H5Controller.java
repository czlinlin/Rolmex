/**
 *
 */
package com.mossle.H5.web;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;

import org.activiti.engine.impl.util.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mossle.H5.cms.BulletinService;
import com.mossle.H5.cms.MsgService;
import com.mossle.H5.device.DeviceService;
import com.mossle.H5.user.AccountService;
import com.mossle.H5.user.H5PartyService;
import com.mossle.H5.user.MyOAService;
import com.mossle.H5.work.ProjectService;
import com.mossle.H5.work.WorkReportService;
import com.mossle.H5.work.WorkTaskService;
import com.mossle.common.utils.AESUtils;
import com.mossle.common.utils.LogUtils;
import com.mossle.operation.service.OperationService;
import com.mossle.operation.service.WSApplyService;
import com.mossle.H5.Approval.ApprovalService;
import com.mossle.H5.bpm.BpmService;
import com.mossle.H5.dic.DicDataService;

/**
 * @author Bing
 */
@RestController
@RequestMapping("H5")
public class H5Controller {
    private static Logger logger = LoggerFactory.getLogger(H5Controller.class);
    private String key;
    private String iv;
    private AccountService accountService;
    private WorkReportService workReportService;
    private MsgService msgService;
    private MyOAService myOAService;
    private H5PartyService h5PartyService;
    private ProjectService projectService;
    private WorkTaskService workTaskService;
    private ApprovalService approvalService;
    private BulletinService bulletinService;
    private DicDataService dicDataService;
    private DeviceService deviceService;
    private BpmService bpmService;
    private WSApplyService wSApplyService;
   
    /*
     * 传入参数：加密的json字符串
     */
    @POST
    @RequestMapping("CallInterface")
    public Map<String, Object> CallInterface(@RequestParam Map<String, Object> requestParam,
                                             HttpServletRequest httpRequest) throws UnknownHostException {
        // System.out.println(httpRequest.getHeader("User-Agent"));
        Map<String, Object> returnMap = new HashMap<String, Object>();// 变量用于返回值

        if (requestParam.containsKey("param")) {
            // System.out.println(requestParam);
            String param = requestParam.get("param").toString();
            // System.out.println(param);

            // 解密
            Map<String, Object> decryptedMap = getDecryptedMap(param);
            if (decryptedMap == null) {
                returnMap.put("bSuccess", "false");
                returnMap.put("strMsg", "参数错误");
                return returnMap;
            }

            //验证token值
			/*String method = decryptedMap.get("method").toString();
			if(!method.toLowerCase().equals("h5logon")
				&&!method.toLowerCase().equals("versionupdate")
				&&!method.toLowerCase().equals("h5searchpicpwdstatus")
				&&!method.toLowerCase().equals("numremind")){
				if(!deviceService.validAPPToken(decryptedMap.get("userId").toString(), decryptedMap.get("token").toString())){
					returnMap.put("bSuccess", "false");
					returnMap.put("strMsg", "非法请求，请重新登录");
					return returnMap;
				}
			}*/

            returnMap = switch_case(decryptedMap, httpRequest);
        } else {
            returnMap.put("bSuccess", "false");
            returnMap.put("strMsg", "参数错误");
        }

        return returnMap;
    }

    /**
     * 验证字符串内容是否包含下列非法字符<br>
     * `~!#%^&*=+\\|{};:'\",<>/?○●★☆☉♀♂※¤╬の〆
     *
     * @param content 字符串内容
     * @return 't'代表不包含非法字符，otherwise代表包含非法字符。
     */
    public static char validateLegalString(String content) {
        String illegal = "`~!#%^&*=+\\|{};:'\",<>/?○●★☆☉♀♂※¤╬の〆";
        char isLegalChar = 't';
        L1:
        for (int i = 0; i < content.length(); i++) {
            for (int j = 0; j < illegal.length(); j++) {
                if (content.charAt(i) == illegal.charAt(j)) {
                    isLegalChar = content.charAt(i);
                    break L1;
                }
            }
        }
        return isLegalChar;
    }

    /*
     * 得到解密后的map 出入参数：加密的json字符串
     */
    Map<String, Object> getDecryptedMap(String encryptedParam) {
        // param =
        // lrcyaE1bDRB+Qa0R3QaarilZqAgAbfdKEnaFtcOsWst3hbFvtK25taWgS3Wjw7qqd2Z1gnh1Ct6i/9OU651eLIXbNmfFZCT9JawFpw1uIH/Bz7RNshY7g8Q43xRqmBgNGMOe1/W/A9G/fU8pXlSDNy9PtNwex0hMNYTLPonXLPJvUSBwDNxCnnbV9bIxrIys

        // 用于返回的变量
        Map<String, Object> map = new HashMap<String, Object>();

        try {
            // 解密
            String paramDecrypt = AESUtils.decryptData(encryptedParam, key, iv);
            // System.out.println("paramDecrypt = " + paramDecrypt);
            // paramDecrypt =
            // {"sign":"222c49da7a0341ab697b189d573c9800","timestamp":"","method":"abOokQOXihH239auU1NEkg==","percode":"","CH02":"Zvf4lPRuoii43RFeG2IVpg=="}

            // json字符串转map
            JSONObject json = new JSONObject(paramDecrypt);
            Iterator iterator = json.keys();
            while (iterator.hasNext()) {
                String jsonKey = (String) iterator.next();
                String jsonVal = json.getString(jsonKey);

                if (!jsonKey.equals("sign") && !jsonVal.equals(""))
                    jsonVal = AESUtils.decryptData(jsonVal, key, iv);

                map.put(jsonKey, jsonVal);
            } 

/*			System.out.println("=================================================================================");
			System.out.println(map);
			System.out.println(
					"--------------------------------------------------------------------------------------------------------------------------------------------------");*/
        } catch (Exception e) {
            System.out.println(e);
        }

        return map;
    }

    Map<String, Object> switch_case(Map<String, Object> decryptedMap, HttpServletRequest httpRequest)
            throws UnknownHostException {
        // 根据方法名 switch case
        String method = decryptedMap.get("method").toString();

        // 变量用于返回值
        Map<String, Object> returnMap = new HashMap<String, Object>();

        switch (method.toLowerCase()) {

            case "versionupdate":// VersionUpdate
                returnMap = deviceService.VersionUpdate(decryptedMap);
                break;

            case "h5logon":// H5Logon
                returnMap = accountService.H5Logon(decryptedMap, httpRequest);
                break;

		case "thepersonneltreedata":
			returnMap = h5PartyService.ThePersonnelTreeData(decryptedMap); //获取人员树  ThePersonnelTreeData
			break;
		case "stationtreedata":
			returnMap = h5PartyService.StationTreeDataNew(decryptedMap); //获取岗位树StationTreeData
			break;

		case "h5clientid":// H5ClientID
			returnMap = deviceService.H5ClientID(decryptedMap, httpRequest);
			break;


            // 下面是：汇报======================================
            case "workreportfromme":// WorkReportFromMe
                returnMap = workReportService.WorkReportFromMe(decryptedMap);
                break;
            case "workreporttome":// WorkReportToMe
                returnMap = workReportService.WorkReportToMe(decryptedMap);
                break;
            case "workreportdetail":// WorkReportDetail
                returnMap = workReportService.WorkReportDetail(decryptedMap);
                break;
            case "workreportaddinit"://新建汇报初始化页面 ADD BY LILEI AT 2018.12.20
            	returnMap = workReportService.WorkreportAddInit(decryptedMap);
            	break;
            case "workreportadd":
                returnMap = workReportService.WorkreportAdd(decryptedMap);
                break;
            case "workreportcopytome":
                returnMap = workReportService.WorkReportCopyToMe(decryptedMap);
                break;
            case "workreportfeedback":
                returnMap = workReportService.WorkReportFeedback(decryptedMap);
                break;
            case "workreportturntome": // WorkReportTurnToMe
                returnMap = workReportService.WorkReportTurnToMe(decryptedMap);
                break;
            case "reportturn":
                returnMap = workReportService.ReportTurn(decryptedMap);
                break;
            case "workreportturndetail":
                break;
            case "unreadreportdel":
                returnMap = workReportService.UnreadReportDel(decryptedMap);
            case "reportccpresetting":
                returnMap = workReportService.ReportCCPreSetting(decryptedMap);
                break;
            // 上面是：汇报======================================

            // 下面是：任务======================================
            case "tasklist":
			/*Map<String, Object> returnMapTemp = new HashMap<String, Object>();
			returnMapTemp.put("bSuccess", "true");
			returnMapTemp.put("strMsg", "暂无数据");
			returnMap = returnMapTemp;*/
                returnMap = workTaskService.TaskList(decryptedMap);
                break;
            case "taskadd":
                returnMap = workTaskService.TaskAdd(decryptedMap);
                break;
            case "taskdetail":
                returnMap = workTaskService.TaskDetail(decryptedMap);//20181105 chengze  用sql把视图替换掉
                break;
            case "taskeditload":
                returnMap = workTaskService.TaskEditLoad(decryptedMap);//20181105 chengze  用sql把视图替换掉
                break;
            case "taskedit":
                returnMap = workTaskService.TaskEdit(decryptedMap);
                break;
            case "taskdel":
                returnMap = workTaskService.TaskDel(decryptedMap);
                break;
            case "taskrealdel":
                returnMap = workTaskService.TaskRealDel(decryptedMap);
                break;
            case "taskpublish":
                returnMap = workTaskService.TaskPublish(decryptedMap);
                break;
            case "taskclosed":
                returnMap = workTaskService.TaskClosed(decryptedMap);
                break;
            case "taskevaluation":// 评价
                returnMap = workTaskService.TaskEvaluation(decryptedMap);
                break;
            case "taskcommitvalidate":// 提交验证
                returnMap = workTaskService.TaskCommitValidate(decryptedMap);
                break;
            case "taskcommit":// 提交
                returnMap = workTaskService.TaskCommit(decryptedMap);
                break;
            case "taskcomment"://
                returnMap = workTaskService.TaskComment(decryptedMap);
                break;
            case "taskexec"://
                returnMap = workTaskService.TaskExec(decryptedMap);
                break;
            case "tasktoask":
                break;
                
            case "taskcc":// 任务抄送   cz 20181214 add
                returnMap = workTaskService.TaskCC(decryptedMap);
                break;
            // 上面是：任务======================================

            // 下面是：项目======================================
            case "projectcarryin":// ProjectCarryIn
                returnMap = projectService.ProjectCarryIn(decryptedMap); //20181105 chengze  用sql把视图替换掉   
                break;
            case "projectcomplete":
                returnMap = projectService.ProjectComplete(decryptedMap);//20181105 chengze  用sql把视图替换掉
                break;
            case "projectadd":
                returnMap = projectService.ProjectAdd(decryptedMap);
                break;
            case "projectdetail":
                returnMap = projectService.ProjectDetail(decryptedMap);//20181105 chengze  用sql把视图替换掉
                break;
            case "projecteditload":
                returnMap = projectService.ProjectEditLoad(decryptedMap);//20181105 chengze  用sql把视图替换掉
                break;
            case "projectedit":
                returnMap = projectService.ProjectEdit(decryptedMap);
                break;
            case "projectclose":
                returnMap = projectService.ProjectClose(decryptedMap);
                break;
            case "projectcommitvalidate":
                returnMap = projectService.ProjectCommitValidate(decryptedMap);
                break;
            case "projectcommit":
                returnMap = projectService.ProjectCommit(decryptedMap);
                break;
            case "projectlist":// ProjectList
                returnMap = projectService.ProjectList(decryptedMap);
                break;
            case "aboveedit":
                returnMap = projectService.AboveEdit(decryptedMap);
                break;
            case "projectpublish":
                returnMap = projectService.ProjectPublish(decryptedMap);
                break;
            case "projectdel":
                returnMap = projectService.ProjectDel(decryptedMap);
                break;
            case "projectrealdel":
                returnMap = projectService.ProjectRealDel(decryptedMap);
                break;
            case "projectevaluation":
                returnMap = projectService.ProjectEvaluation(decryptedMap);
                break;
            case "projectexec":
                returnMap = projectService.ProjectExec(decryptedMap);
                break;
            case "projecttoask":
                break;
            case "projectloadpersonnels":// ProjectLoadPersonnels
                break;
            case "projectaddreplace":
                break;
            case "aboveandabove":
                break;
            // 上面是：项目======================================

            // 下面是：我的======================================
            case "personnelload":
                returnMap = myOAService.GetMyInfo(decryptedMap);
                break;
            case "setpushonoff":
                returnMap = myOAService.SetPushSwitchStatus(decryptedMap);
                break;
            case "personneleditphoto":
                returnMap = myOAService.ChangeMyHeadImg(decryptedMap);
                break;
            case "personneleditlink":
                returnMap = myOAService.EditMyInfo(decryptedMap);
                break;
            case "personneleditpwd":
                returnMap = myOAService.ChangeMyPwd(decryptedMap);
                break;
            case "personneleditprivatepwd":
                returnMap = myOAService.ChangeMyOpterationPwd(decryptedMap);
                break;
            case "personnelvalidprivatepwd":
                returnMap = myOAService.CheckMyOpterationPwd(decryptedMap);
                break;
            case "h5setpicpwd":
                returnMap = myOAService.AddAndEditPicPwd(decryptedMap);
                break;
            case "h5updatepicpwdstatus":
                returnMap = myOAService.ChangePicPwdStatus(decryptedMap);
                break;
            case "h5searchpicpwdstatus":
                returnMap = myOAService.SearchPicPwdStatus(decryptedMap);
                break;
            case "h5validpicpwd":
                returnMap = myOAService.CheckPicPwd(decryptedMap);
                break;
            case "getpushonoff":
                returnMap = myOAService.SearchPushSwitchStatus(decryptedMap);
                break;
            case "myreplace":
                break;
            case "personneloftenlist":
                break;
            case "personnelcancelpeople":
                break;
            case "personneloftendetail":
                break;
            case "calendaritems":
                // {strPerCode=2, percode=2, method=CalendarItems,
                // sign=17d76983f1c39817d1c717c783e4f1b2, strDate=2017-9-16,
                // timestamp=}
                break;
            case "calendaritemdetail":
                break;
            case "calendaradditem":
                break;
            // 上面是：我的======================================

            // 下面是：通讯录======================================
            case "addressbooks":
                returnMap = h5PartyService.AddressBooks(decryptedMap);
                break;
            case "addressbookdetail":
                returnMap = h5PartyService.AddressBookDetail(decryptedMap);
                break;
            // 上面是：通讯录======================================

            // 下面是：消息======================================
            case "numremind":// NumRemind
                returnMap = msgService.NumRemind(decryptedMap);
                break;
            case "msglist":
                returnMap = msgService.MsgList(decryptedMap);
                break;
            case "msgdetails":
                returnMap = msgService.MsgDetails(decryptedMap);
                break;
            case "setreadall":
                returnMap = msgService.SetReadAll(decryptedMap);
                break;
            case "msgaboutme":
                break;
            case "msgremind":
                break;
            // 上面是：消息======================================

            // 下面是：审批======================================
            case "toh5readbusinesstype":
                returnMap = approvalService.GetFirstBusinessType(decryptedMap);
                break;
            case "toh5myflowrecord":  // 我的申请
                returnMap = approvalService.GetMyApplication(decryptedMap);
                break;
            case "toh5flowtodeal":  // 待我审批
                returnMap = approvalService.GetWaitAuditApproval(decryptedMap);
                break;
            case "toh5flowdealed":  // 经我审批
                returnMap = approvalService.GetMyAuditApproval(decryptedMap);
                break;
            case "approvalcancel":
                returnMap = approvalService.ApprovalStepCancel(decryptedMap);
                break;
            case "toh5allrecord":
                returnMap = approvalService.GetAllApproval(decryptedMap);
                break;
            case "toh5customizedrecord":
                returnMap = approvalService.GetAllSpecialApproval(decryptedMap);
                break;
            case "toh5cctomerecord":  // 抄送审批
                returnMap = approvalService.GetCopyToMeApproval(decryptedMap);
                break;
            case "toh5recorddetail":
                returnMap = approvalService.GetFlowDetail(decryptedMap);
                //手机端查看抄送后其消息置已读 sjx 18.09.12
            	approvalService.copyMsgUpdate(decryptedMap);
                break;
            case "toh5recorddealdetail":
                returnMap = approvalService.GetFlowDetail(decryptedMap);
                break;
            case "toh5authorizationdetail":
                returnMap = approvalService.ApprovalAuthorizationAgree(decryptedMap);
                break;
            case "toh5auditpass":// 审批：同意
                returnMap = approvalService.ApprovalAgree(decryptedMap);
                try {
                    wSApplyService.SetOATimeTask(returnMap.get("businessKey").toString(),
                            returnMap.get("humanTaskId").toString());
                } catch (Exception ex) {
                    logger.info("设置定时任务的数据异常,returnMap:" + returnMap.toString() + "；异常信息："
                            + ex.getMessage() + "\r\n" + ex.getStackTrace());
                }
                break;
            case "toh5auditnopass":// 审批：不同意
                returnMap = approvalService.ApprovalDisagree(decryptedMap);
                try {
                    wSApplyService.SetOATimeTask(returnMap.get("businessKey").toString(),
                            returnMap.get("humanTaskId").toString());
                } catch (Exception e) {
                    // TODO: handle exception
                }
                break;
            case "toh5backtoprevstep":// 审批：驳回
                returnMap = approvalService.ApprovalToPrevStep(decryptedMap);
                try {
                    wSApplyService.SetOATimeTask(returnMap.get("businessKey").toString(),
                            returnMap.get("humanTaskId").toString());
                } catch (Exception e) {
                    // TODO: handle exception
                }
                break;
            case "toh5approvalcustom":// 审批：自定义申请
                returnMap = approvalService.CustomApprovalAgree(decryptedMap);
                break;
            case "toh5loadarea":
                returnMap = approvalService.GetInitAreaList(decryptedMap);
                break;
            case "toh5loadsubcom":
                returnMap = approvalService.GetCompanyByArea(decryptedMap);
                break;
            case "toh5readbusinessdetails":
                returnMap = approvalService.GetSecondBusinessType(decryptedMap);
                break;
            case "toh5readdic":
                returnMap = dicDataService.GetDicDataByType(decryptedMap);
                break;
            case "toh5customapply":
                returnMap = approvalService.CustomApply(decryptedMap);// 发起自定义申请
                break;
            case "toh5customapplywork":
                returnMap = approvalService.CustomApplyWork(decryptedMap);// 发起自定义请假等申请 ckx  add 2018/08/7
                break;
            case "toh5custompresetapprovers":
                returnMap = approvalService.CustomPresetApprovers(decryptedMap);// 获取预设审核人ckx  add 2018/08/7
                break;
            case "bpmparticipanttask":// BpmParticipantTask（参与岗位的）待领审批列表
                returnMap = bpmService.ParticipantTask(decryptedMap);
                break;
            case "bpmclaimtask":// BpmClaimTask 认领
                returnMap = bpmService.ClaimTask(decryptedMap);
                break;
            case "companyrecord":// 公司申请(业务)
                break;
            case "flowrecordcancel":// 我的申请--取消
                break;
            case "bpmprocessapplylist":// BpmProcessApplyList 流程申请查询列表   部门申请
                returnMap = bpmService.ProcessApplyList(decryptedMap);
                break;
            case "bpmprocessauditlist":// BpmProcessAuditList 流程审批查询列表  部门审批
                returnMap = bpmService.ProcessAuditList(decryptedMap);
                break;
            // 上面是：审批======================================

            // 下面是：公告======================================
            case "bulletins":
                returnMap = bulletinService.Bulletins(decryptedMap);
                break;
            case "bulletindetail":
                returnMap = bulletinService.BulletinDetail(decryptedMap);
                break;
            case "bulletinfocus":
                break;
            case "bulletincancelfocus":
                break;
            // 上面是：公告======================================

            // 下面是：议题======================================
            case "majorissueslist":
                break;
            case "majorissuedetail":
                break;
            case "majorissuefocus":
                break;
            case "majorissuecancelfocus":
                break;
            case "majorissueagree":
                break;
            case "majorissueoppose":
                break;
            case "majorissuereply":
                break;
            case "majorissuereplylist":
                break;
            case "majorissuenewreplys":
                break;
            case "majorissueloaddepart":
                break;
            case "majorissuewithinloaddepart":
                break;
            case "majorissuepublish":
                break;
            // 上面是：议题======================================

            // 下面是：工作圈======================================
            case "workcyclelist":
                break;
            case "workcycledetail":
                break;
            case "workcyclereply":
                break;
            case "workcyclezambian":
                break;
            case "workcycleturn":
                break;
            case "workcyclepublish":
                break;
            // 上面是：工作圈======================================

		//随时抄送start
		case "customcopysave":
			returnMap = approvalService.customCopySave(decryptedMap);
			break;
		//随时抄送end
		// case "queryforlist":
		// returnMap = deviceService.queryForList(decryptedMap);
		// break;


            default:
                System.out.println(method);
                break;
        }

        // 保存日志
        try {
            String userId = null;
            if (decryptedMap.get("strPerCode") == null) {
                if (decryptedMap.get("percode") != null)
                    userId = decryptedMap.get("percode").toString();
            } else
                userId = decryptedMap.get("strPerCode").toString();
            LogUtils.saveAPPLog(httpRequest, method, userId, null);
        } catch (Exception e) {
            // TODO Auto-generated catch block

            e.printStackTrace();
        }
/*
		System.out.println(
				"--------------------------------------------------------------------------------------------------------------------------------------------------");
		System.out.println(returnMap);
		System.out.println("=================================================================================");*/
        return returnMap;
    }

    // @RequestMapping("test")
    // public Map<String, Object> test(@RequestParam Map<String, Object>
    // requestParam, HttpServletRequest httpRequest) throws UnknownHostException
    // {
    // return switch_case(requestParam, httpRequest);
    // }

    // @Value==========================================
    @Value("${h5.key}")
    public void setKey(String key) {
        this.key = key;
    }

    @Value("${h5.iv}")
    public void setIv(String iv) {
        this.iv = iv;
    }

    // @Resource============================================
    @Resource
    public void setAccountService(AccountService accountService) {
        this.accountService = accountService;
    }

    @Resource
    public void setWorkReportService(WorkReportService workReportService) {
        this.workReportService = workReportService;
    }

    @Resource
    public void setMyOAService(MyOAService myOAService) {
        this.myOAService = myOAService;
    }

    @Resource
    public void setH5PartyService(H5PartyService h5PartyService) {
        this.h5PartyService = h5PartyService;
    }

    @Resource
    public void setProjectService(ProjectService projectService) {
        this.projectService = projectService;
    }

    @Resource
    public void setWorkTaskService(WorkTaskService workTaskService) {
        this.workTaskService = workTaskService;
    }

    @Resource
    public void setApprovalService(ApprovalService approvalService) {
        this.approvalService = approvalService;
    }

    @Resource
    public void setMsgService(MsgService msgService) {
        this.msgService = msgService;
    }

    @Resource
    public void setBulletinService(BulletinService bulletinService) {
        this.bulletinService = bulletinService;
    }

    @Resource
    public void setDicDataService(DicDataService dicDataService) {
        this.dicDataService = dicDataService;
    }

    @Resource
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Resource
    public void setBpmService(BpmService bpmService) {
        this.bpmService = bpmService;
    }

    @Resource
    public void setWSApplyService(WSApplyService wSApplyService) {
        this.wSApplyService = wSApplyService;
    }
    
}
