/**
 *
 */
package com.mossle.H5.work;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.google.common.base.Joiner;
import com.mossle.api.keyvalue.KeyValueConnector;
import com.mossle.api.notification.NotificationConnector;
import com.mossle.api.pim.WorkReportConnector;
import com.mossle.api.tenant.TenantHolder;
import com.mossle.api.user.UserConnector;
import com.mossle.api.user.UserDTO;
import com.mossle.common.utils.FileUploadAPI;
import com.mossle.core.query.PropertyFilter;
import com.mossle.msg.MsgConstants;
import com.mossle.party.PartyConstants;
import com.mossle.party.persistence.domain.PartyEntity;
import com.mossle.party.persistence.manager.PartyEntityManager;
import com.mossle.party.support.PartyOrgConnector;
import com.mossle.pim.persistence.domain.WorkReportCc;
import com.mossle.pim.persistence.domain.WorkReportForward;
import com.mossle.pim.persistence.domain.WorkReportInfo;
import com.mossle.pim.persistence.manager.WorkReportCcManager;
import com.mossle.pim.persistence.manager.WorkReportForwardManager;
import com.mossle.pim.persistence.manager.WorkReportInfoManager;
import com.mossle.user.persistence.domain.AccountInfo;
import com.mossle.user.persistence.manager.AccountInfoManager;

/**
 * @author Bing
 */
@Service
public class WorkReportService {
    private static Logger logger = LoggerFactory.getLogger(WorkReportService.class);
    private JdbcTemplate jdbcTemplate;
    private WorkReportInfoManager workReportInfoManager;
    private WorkReportForwardManager workReportForwardManager;
    private WorkReportCcManager workReportCcManager;
    private FileUploadAPI fileUploadAPI;
    private AccountInfoManager accountInfoManager;
    private UserConnector userConnector;
    private NotificationConnector notificationConnector;// 发送消息
    private TenantHolder tenantHolder;
    @Autowired
    private WorkReportConnector workReportConnector;
    @Autowired
    private PartyOrgConnector partyOrgConnector;
    @Autowired
    private PartyEntityManager partyEntityManager;
    @Autowired
    private KeyValueConnector keyValueConnector;

    public Map<String, Object> WorkReportFromMe(Map<String, Object> decryptedMap) {
        // {strPageSize=5, strPerCode=2, percode=2, method=WorkReportFromMe,
        // sign=e458da11bb70689df863747cb2020546, strPageIndex=1, timestamp=}

        Map<String, Object> returnMap = new HashMap<String, Object>();

        try {
            // 验证参数================================================================================
            if (!decryptedMap.containsKey("percode") || !decryptedMap.containsKey("strPageIndex")
                    || !decryptedMap.containsKey("strPageSize")) {
                returnMap.put("bSuccess", "false");
                returnMap.put("strMsg", "参数错误");
                return returnMap;
            }

            // 获取数据================================================
            String where_definition = " datastatus=1 ";
            ArrayList<Object> argsList = new ArrayList<Object>();// sql参数列表

            if (decryptedMap.containsKey("percode")) {
                if (!decryptedMap.get("percode").toString().isEmpty()) {
                    where_definition += " and USER_ID=? ";
                    argsList.add(decryptedMap.get("percode"));
                }
            }
            
            //add by lilei at 2019.01.18
            if (decryptedMap.containsKey("title")) {
                if (!decryptedMap.get("title").toString().isEmpty()) {
                    where_definition += " and title like '%"+decryptedMap.get("title")+"%' ";
                    //argsList.add("'%"+decryptedMap.get("title")+"%'");
                }
            }
            
            /*if (decryptedMap.containsKey("userName")) {
                if (!decryptedMap.get("userName").toString().isEmpty()) {
                	String strSql="select id from party_entity where `NAME` like '%"+decryptedMap.get("userName").toString()+"%'";
            		List<String> userNameList=jdbcTemplate.queryForList(strSql, String.class);
            		if(userNameList.size()<1){
            			userNameList.add("0");
            		}
            		String userIds=Joiner.on(",").join(userNameList);
                    where_definition += " and USER_ID in(?) ";
                    argsList.add(userIds);
                }
            }*/
            
            if (decryptedMap.containsKey("type")) {
                if (!decryptedMap.get("type").toString().isEmpty()) {
                    where_definition += " and type=? ";
                    argsList.add(decryptedMap.get("type"));
                }
            }

            if (decryptedMap.containsKey("status")) {
                if (!decryptedMap.get("status").toString().isEmpty()) {
                    where_definition += " and status=? ";
                    argsList.add(decryptedMap.get("status"));
                }
            }

            if (decryptedMap.containsKey("strStart")) {
                if (!decryptedMap.get("strStart").toString().isEmpty()) {
                    where_definition += " and report_date>=? ";
                    argsList.add(decryptedMap.get("strStart"));
                }
            }

            if (decryptedMap.containsKey("strEnd")) {
                String end_report_date = decryptedMap.get("strEnd").toString();
                if (!end_report_date.isEmpty()) {
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        Date endPublishDate = sdf.parse(end_report_date);

                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(endPublishDate);
                        calendar.add(Calendar.DATE, 1);

                        where_definition += " and report_date<? ";
                        argsList.add(calendar.getTime());
                    } catch (Exception e) {
                        // TODO 自动生成的 catch 块
                        e.printStackTrace();
                    }
                }
            }
            
           
            List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
            // String sql = "SELECT * FROM `v_h5_work_report_info` WHERE " + where_definition + " LIMIT ?,?";
            String sql = getSql() + " WHERE " + where_definition + "ORDER BY r.ID DESC LIMIT ?,?";
            // System.out.println(sql);
            
            long pageIndex = Long.valueOf(decryptedMap.get("strPageIndex").toString());
            long row_count = Long.valueOf(decryptedMap.get("strPageSize").toString());
            long offset = row_count * (pageIndex - 1);
            argsList.add(offset);
            argsList.add(row_count);

            list = jdbcTemplate.queryForList(sql, argsList.toArray());

            // 返回======================================================================
            returnMap.put("bSuccess", "true");
            if (list.size() > 0) {
                returnMap.put("strMsg", "加载成功");
                returnMap.put("ReportList", list);
            } else {
                returnMap.put("strMsg", "暂无数据");
            }
        } catch (Exception e) {
            e.printStackTrace();
            returnMap.put("bSuccess", "false");
            returnMap.put("strMsg", "加载错误，请联系管理员");
        }

        return returnMap;

        /*
         * {"bSuccess":"true","strMsg":"加载成功","ReportList":[
         *
         * {"RP01":119,"RP02":"hb1","RP05C":"报告","RP06":"0","RP06C":"未读","RP07":
         * "PE1609090001","RP07C":"宋杰","RP10":"PE0000000001","RP10C":"管理员",
         * "RP11":"\/Date(1504603290000)\/","IsCC":null,"ISRead":null},
         *
         * {"RP01":118,"RP02":"hb","RP05C":"报告","RP06":"0","RP06C":"未读","RP07":
         * "PE1609090001","RP07C":"宋杰","RP10":"PE0000000001","RP10C":"管理员",
         * "RP11":"\/Date(1504603225000)\/","IsCC":null,"ISRead":null},
         *
         * ]}
         */
    }

    public Map<String, Object> WorkReportToMe(Map<String, Object> decryptedMap) {
        // {strPageSize=5, strPerCode=2, percode=2, method=WorkReportToMe,
        // sign=e458da11bb70689df863747cb2020546, strPageIndex=1, timestamp=}

        Map<String, Object> returnMap = new HashMap<String, Object>();

        try {
            // 验证参数================================================================================
            if (!decryptedMap.containsKey("percode") || !decryptedMap.containsKey("strPageIndex")
                    || !decryptedMap.containsKey("strPageSize")) {
                returnMap.put("bSuccess", "false");
                returnMap.put("strMsg", "参数错误");
                return returnMap;
            }

            // 获取数据================================================
            String where_definition = " datastatus=1 ";
            ArrayList<Object> argsList = new ArrayList<Object>();// sql参数列表

            if (decryptedMap.containsKey("percode")) {
                if (!decryptedMap.get("percode").toString().isEmpty()) {
                    where_definition += " and sendee=? ";
                    argsList.add(decryptedMap.get("percode"));
                }
            }

            if (decryptedMap.containsKey("title")) {
                if (!decryptedMap.get("title").toString().isEmpty()) {
                    where_definition += " and title like '%"+decryptedMap.get("title")+"%' ";
                    //argsList.add("'%"+decryptedMap.get("title")+"%'");
                }
            }
            
            if (decryptedMap.containsKey("userName")) {
                if (!decryptedMap.get("userName").toString().isEmpty()) {
                	String strSql="select id from party_entity where `NAME` like '%"+decryptedMap.get("userName").toString()+"%'";
            		List<String> userNameList=jdbcTemplate.queryForList(strSql, String.class);
            		if(userNameList.size()<1){
            			userNameList.add("0");
            		}
            		String userIds=Joiner.on(",").join(userNameList);
                    where_definition += String.format(" and USER_ID in(%s) ", userIds);
                    //argsList.add(userIds);
                }
            }
            
            if (decryptedMap.containsKey("type")) {
                if (!decryptedMap.get("type").toString().isEmpty()) {
                    where_definition += " and type=? ";
                    argsList.add(decryptedMap.get("type"));
                }
            }

            if (decryptedMap.containsKey("status")) {
                if (!decryptedMap.get("status").toString().isEmpty()) {
                    where_definition += " and status=? ";
                    argsList.add(decryptedMap.get("status"));
                }
            }

            if (decryptedMap.containsKey("strStart")) {
                if (!decryptedMap.get("strStart").toString().isEmpty()) {
                    where_definition += " and report_date>=? ";
                    argsList.add(decryptedMap.get("strStart"));
                }
            }

            if (decryptedMap.containsKey("strEnd")) {
                String end_report_date = decryptedMap.get("strEnd").toString();
                if (!end_report_date.isEmpty()) {
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        Date endPublishDate = sdf.parse(end_report_date);

                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(endPublishDate);
                        calendar.add(Calendar.DATE, 1);

                        where_definition += " and report_date<? ";
                        argsList.add(calendar.getTime());
                    } catch (Exception e) {
                        // TODO 自动生成的 catch 块
                        e.printStackTrace();
                    }
                }
            }

            List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
            // String sql = "SELECT * FROM `v_h5_work_report_info` WHERE " + where_definition + " LIMIT ?,?";
            String sql = getSql() + " WHERE " + where_definition + "ORDER BY r.ID DESC LIMIT ?,?";
            
            long pageIndex = Long.valueOf(decryptedMap.get("strPageIndex").toString());
            long row_count = Long.valueOf(decryptedMap.get("strPageSize").toString());
            long offset = row_count * (pageIndex - 1);
            argsList.add(offset);
            argsList.add(row_count);

            list = jdbcTemplate.queryForList(sql, argsList.toArray());

            // 返回======================================================================
            returnMap.put("bSuccess", "true");
            if (list.size() > 0) {
                returnMap.put("strMsg", "加载成功");
                returnMap.put("ReportList", list);
            } else {
                returnMap.put("strMsg", "暂无数据");
            }
        } catch (Exception e) {
            e.printStackTrace();
            returnMap.put("bSuccess", "false");
            returnMap.put("strMsg", "加载错误，请联系管理员");
        }

        return returnMap;

        /*
         * {"bSuccess":"true","strMsg":"加载成功","ReportList":[
         *
         * {"RP01":56,"RP02":"DDDDD","RP05C":"报告","RP06":"2","RP06C":"已反馈",
         * "RP07":"PE0000000001","RP07C":"管理员","RP10":"PE0000000001","RP10C"
         * :"管理员","RP11":"\/Date(1487925099000)\/","IsCC":"0","ISRead":"2"},
         *
         * {"RP01":38,"RP02":"FFFF","RP05C":"报告","RP06":"1","RP06C":"已读",
         * "RP07":"PE0000000001","RP07C":"管理员","RP10":"PE0000000001","RP10C"
         * :"管理员","RP11":"\/Date(1487225757000)\/","IsCC":"0","ISRead":"1"},
         *
         * ] }
         */
    }

    public Map<String, Object> WorkReportDetail(Map<String, Object> decryptedMap) {
        // {strPerCode=2, percode=2, strReportID=800028036366356,
        // method=WorkReportDetail, sign=9a5ab73b22fe21f907013bf65511ed6b,
        // timestamp=}

        Map<String, Object> returnMap = new HashMap<String, Object>();

        try {
            // 验证参数================================================================================
            if (!decryptedMap.containsKey("strReportID") || !decryptedMap.containsKey("percode")) {
                returnMap.put("bSuccess", "false");
                returnMap.put("strMsg", "参数错误");
                return returnMap;
            }

            String strPerCode = decryptedMap.get("percode").toString();
            Long userId = Long.parseLong(strPerCode);

            String strReportID = decryptedMap.get("strReportID").toString();
            WorkReportInfo workReportInfo = workReportInfoManager.get(Long.parseLong(strReportID));

            // 查看汇报人为汇报接收人，status为已读
            if (workReportInfo.getSendee().equals(userId) && workReportInfo.getStatus().equals("0")) {
                workReportInfo.setStatus("1");
                workReportInfoManager.save(workReportInfo);
            }

            List<PartyEntity> postPartyEntityList=partyOrgConnector.getPostByUserId(strPerCode);
            List<String> postIdList=new ArrayList<String>();
            postIdList.add(strPerCode);
            for (PartyEntity postPartyEntity : postPartyEntityList) {
            	postIdList.add(postPartyEntity.getId().toString());
    		}
            String strPostId=Joiner.on(",").join(postIdList);

            // 抄送阅读
            List<PropertyFilter> propertyFilters = new ArrayList<PropertyFilter>();
            propertyFilters.add(new PropertyFilter("INL_ccno", strPostId));
            propertyFilters.add(new PropertyFilter("EQL_workReportInfo.id", strReportID));
            propertyFilters.add(new PropertyFilter("EQS_status", "0"));
            List<WorkReportCc> ccList = workReportCcManager.find(propertyFilters);
            if (!CollectionUtils.isEmpty(ccList)) {
            	for (WorkReportCc workReportCc : ccList) {
            		//WorkReportCc cc = ccList.get(0);
            		workReportCc.setStatus("1");
                    workReportCcManager.save(workReportCc);
				}
            }

            // 转发阅读
            List<PropertyFilter> turnPropertyFilters = new ArrayList<PropertyFilter>();
            turnPropertyFilters.add(new PropertyFilter("EQL_sendee", strPerCode));
            turnPropertyFilters.add(new PropertyFilter("EQL_workReportInfo.id", strReportID));
            turnPropertyFilters.add(new PropertyFilter("EQS_status", "0"));
            List<WorkReportForward> forwardList = workReportForwardManager.find(turnPropertyFilters);
            if (!CollectionUtils.isEmpty(forwardList)) {
                WorkReportForward forward = forwardList.get(0);
                forward.setStatus("1");
                workReportForwardManager.save(forward);
            }

            // 获取数据================================================
            // CR06 0=登陆人是接收人；1=登陆人不是接收人（是转发或抄送的）
            StringBuffer sqlBuf = new StringBuffer();
            sqlBuf.append("SELECT r.ID AS ID,r.ID AS RP01,r.`code` AS CODE,r.type AS RP05,");
            sqlBuf.append("GET_DICT_VALUE_BY_TYPE_NAME ('work_report_type',r.type) AS RP05C,");
            sqlBuf.append("r.title AS title,r.title AS RP02,r.completed AS completed,r.dealing AS dealing,");
            sqlBuf.append("(CASE r.type WHEN 4 THEN r.problems ELSE r.dealing END ) AS RP03,");
            sqlBuf.append("r.coordinate AS coordinate,r.problems AS problems,r.datastatus AS datastatus,");
            sqlBuf.append("r.`status` AS status,r.`status` AS RP06,");
            sqlBuf.append("GET_DICT_VALUE_BY_TYPE_NAME ('work_report_status',r.`status`) AS RP06C,");
            sqlBuf.append("r.sendee AS sendee,r.sendee AS RP07,GET_DISPLAY_NAME_BY_ID (r.sendee) AS RP07C,");
            sqlBuf.append("r.remarks AS remarks,r.other AS other,r.feedback AS RP08,r.USER_ID AS USER_ID,");
            sqlBuf.append("r.USER_ID AS RP10,GET_DISPLAY_NAME_BY_ID (r.USER_ID) AS RP10C,r.report_date AS report_date,");
            sqlBuf.append("r.report_date AS RP11,r.feedbacktime AS RP09,r.lastedittime AS lastedittime,");
            sqlBuf.append("GET_WORK_REPORT_CC_NAMES_BY_INFO_ID (r.ID) AS CR03,f_store_paths (r.ID, 0, 'OA/report') AS RP04,");
            sqlBuf.append("CASE WHEN sendee=? THEN '0' ELSE '1' END AS  CR06");
            sqlBuf.append(" FROM work_report_info r");
            
            // String sql = "SELECT *,CASE WHEN sendee=? THEN '0' ELSE '1' END AS  CR06 FROM `v_h5_work_report_info` WHERE `ID` = ?";
            String sql = sqlBuf.toString() + " WHERE ID = ?";
            
            /**
             * 判断是否是转发的汇报
             * 1.汇报的接收人不是此人
             * 2.汇报的转发信息中的接收人包括此人
             * **/
            String isTurnReport="0";
            String isShowFeeback="1";
            List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, new Object[]{strPerCode, strReportID});
            List<Map<String, Object>> reportList=new ArrayList<Map<String,Object>>();
            for (Map<String, Object> mapReport : list) {
				if(!mapReport.get("sendee").toString().equals(strPerCode)){
					String hql="from WorkReportForward where sendee=? and workReportInfo.id=?";
					List<WorkReportForward> reportForwardList=workReportForwardManager.find(hql,userId,Long.parseLong(strReportID));
					if(reportForwardList!=null){
						if(reportForwardList.size()>0){
							isTurnReport="1";
							WorkReportForward reportForward=reportForwardList.get(0);
							isShowFeeback=reportForward.getIsfeedbackforward();
						}
					}
				}
				mapReport.put("isTurnReport",isTurnReport);
				mapReport.put("isShowFeeback",isShowFeeback);
            	reportList.add(mapReport);
			}

            // String fromsql = "SELECT * FROM `v_h5_work_report_forward` WHERE `ID` = ? and forwarder=?";
            String fromsql = getReportForwardSql() + " WHERE r.`ID` = ? and f.forwarder=? order by r.id DESC";
            List<Map<String, Object>> frommlist = jdbcTemplate.queryForList(fromsql,
                    new Object[]{strReportID, strPerCode});

            // String tosql = "SELECT * FROM `v_h5_work_report_forward` WHERE `ID` = ? and sendee=?";
            String tosql = getReportForwardSql() + " WHERE r.`ID` = ? and f.sendee=? order by r.id DESC";
            List<Map<String, Object>> tolist = jdbcTemplate.queryForList(tosql,
                    new Object[]{strReportID, strPerCode});

            // 返回======================================================================
            returnMap.put("bSuccess", "true");
            if (reportList.size() > 0) {
                returnMap.put("strMsg", "加载成功");
                returnMap.put("ReportDetail", reportList);
            } else {
                returnMap.put("strMsg", "暂无数据");
            }

            if (frommlist.size() > 0) {
                returnMap.put("frommlist", frommlist);
            }

            if (tolist.size() > 0) {
                returnMap.put("tolist", tolist);
            }
        } catch (Exception e) {
            e.printStackTrace();
            returnMap.put("bSuccess", "false");
            returnMap.put("strMsg", "加载错误，请联系管理员");
        }

        return returnMap;

        // {"ReportDetail":[{"RP01":120,"RP02":"1","RP03":"1","RP04":null,"RP05C":"报告","RP06":"0","RP06C":"未读","RP07":"PE1509100001","RP07C":"123123","RP08":null,"RP09":"0001-01-01T00:00:00","RP10":"PE0000000001","RP10C":"管理员","RP11":"2017-09-09T16:16:15","CR03":null,"CR06":"1"}],"bSuccess":"true","strMsg":"加载成功","frommlist":null,"tolist":null,"RP01":0,"RP02":null,"RP03":null,"RP04":null,"RP05C":null,"RP06":null,"RP06C":null,"RP07":null,"RP07C":null,"RP08":null,"RP09":"0001-01-01T00:00:00","RP10":null,"RP10C":null,"RP11":"0001-01-01T00:00:00","CR03":null,"CR06":null}
    }
    
    /**
     * 新建汇报初始化页面
     * ADD BY LILEI AT 2018.12.20
     * **/
    public Map<String, Object> WorkreportAddInit(Map<String, Object> decryptedMap) {
        Map<String, Object> returnMap = new HashMap<String, Object>();

        try {
            // 验证参数================================================================================
            if (!decryptedMap.containsKey("strPerCode")) {
                returnMap.put("bSuccess", "false");
                returnMap.put("strMsg", "参数错误");
                return returnMap;
            }
            
            String strPerCode=decryptedMap.get("strPerCode").toString();
            Map<String,Object> positionMap=workReportConnector.getReportPositionInfo(strPerCode);
            returnMap.put("positionInfo", positionMap);
            returnMap.put("ccPresetting",workReportConnector.getReportPreSetting());
            
            returnMap.put("bSuccess", "true");
            returnMap.put("strMsg", "获取成功");

        } catch (Exception e) {
            e.printStackTrace();
            logger.debug("WorkreportAdd Exception=" + e.toString());
            returnMap.put("bSuccess", "false");
            returnMap.put("strMsg", "获取数据错误，请联系管理员");
        }
        return returnMap;
    }

    public Map<String, Object> WorkreportAdd(Map<String, Object> decryptedMap) {
        // {strTeamNos=797118359601152,808573832183808,
        // strReceiveCode=796936873328640, strPerCode=2, percode=2,
        // method=WorkreportAdd, strTitle=标题, strType=1,
        // sign=ac5d40b92b536fbd3d0929cc831ec4fd, strAnnex=, strContent=内容,
        // timestamp=}

        Map<String, Object> returnMap = new HashMap<String, Object>();

        try {
            // 验证参数================================================================================
            if (!decryptedMap.containsKey("strTitle") || !decryptedMap.containsKey("strContent")
                    || !decryptedMap.containsKey("strReceiveCode") || !decryptedMap.containsKey("strPerCode")) {
                returnMap.put("bSuccess", "false");
                returnMap.put("strMsg", "参数错误");
                return returnMap;
            }

            // 获取参数=================================================
            String strTeamNos = decryptedMap.get("strTeamNos").toString();// 抄送人
            String strReceiveCode = decryptedMap.get("strReceiveCode").toString();// 接收人
            String strAnnex = decryptedMap.get("strAnnex").toString();// 附件
            String strContent = decryptedMap.get("strContent").toString();// 进行中工作
            String strTitle = decryptedMap.get("strTitle").toString();
            String strPerCode = decryptedMap.get("strPerCode").toString();
            UserDTO percodeDTO = userConnector.findById(strPerCode);

            if (strTitle.length() > 100) {
                returnMap.put("bSuccess", "false");
                returnMap.put("strMsg", "标题太长");
                return returnMap;
            }

            // 已完成工作
            String completed = "";
            if (decryptedMap.containsKey("completed"))
                completed = decryptedMap.get("completed").toString();

            if (strContent.isEmpty() && completed.isEmpty()) {
                returnMap.put("bSuccess", "false");
                returnMap.put("strMsg", "请输入进行中或已完成工作");
                return returnMap;
            }

            if (strReceiveCode.equals(strPerCode)) {
                returnMap.put("bSuccess", "false");
                returnMap.put("strMsg", "接收人不能是发送人");
                return returnMap;
            }

            // 数据状态 0草稿；1发布。默认0
            String datastatus = "0";
            if (decryptedMap.containsKey("datastatus"))
                datastatus = decryptedMap.get("datastatus").toString();

            // 需协调工作
            String coordinate = "";
            if (decryptedMap.containsKey("coordinate"))
                coordinate = decryptedMap.get("coordinate").toString();

            // 备注
            String remarks = "";
            if (decryptedMap.containsKey("remarks"))
                remarks = decryptedMap.get("remarks").toString();
            

            // 保存数据================================================
            WorkReportInfo workReportInfoDest = new WorkReportInfo();
            workReportInfoDest.setType(decryptedMap.get("strType").toString());
            workReportInfoDest.setTitle(strTitle);
            workReportInfoDest.setDealing(strContent);// 进行中的
            workReportInfoDest.setSendee(Long.valueOf(strReceiveCode));
            workReportInfoDest.setUserId(Long.valueOf(strPerCode));
            workReportInfoDest.setReportDate(new Date());
            workReportInfoDest.setDatastatus(datastatus);
            workReportInfoDest.setStatus("0");// 0:未读状态
            workReportInfoDest.setCompleted(completed);
            workReportInfoDest.setCoordinate(coordinate);
            workReportInfoDest.setRemarks(remarks);
            workReportInfoDest.setProblems(strContent);// 专项，存Problems。Bing，2017.10.24
            workReportInfoManager.save(workReportInfoDest);
            
            String iptStartPosition=decryptedMap.get("iptStartPosition").toString();
            Long preSettingId=0L;//Long.parseLong(decryptedMap.get("preSettingId").toString());
            String strCCDatas="";	//用于抄送消息，add by lilei at 2018.12.18
            //ADD BY LILEI AT 2018.12.19 处理上级/条线抄送
        	if(!StringUtils.isBlank(iptStartPosition)){
        		if(decryptedMap.get("preSettingId")==null) 
        			preSettingId=0L;
        		else if(decryptedMap.get("preSettingId").toString().equals(""))
        			preSettingId=0L;
        		else {
        			preSettingId=Long.parseLong(decryptedMap.get("preSettingId").toString());
				}
        		
        		//非大区人员没有条线
        		PartyEntity areaAartyEntity=partyOrgConnector.findPartyAreaByUserId(iptStartPosition);
        		if(areaAartyEntity==null) preSettingId=0L;
        		
            	String position_type="1";
            	PartyEntity partyEntity=partyEntityManager.get(Long.parseLong(iptStartPosition));
            	if(partyEntity.getPartyType().getId()==PartyConstants.PARTY_TYPE_POST)
            		position_type="2";
            	String strSql="delete from work_report_info_attr where id=%s";
            	keyValueConnector.updateBySql(String.format(strSql,workReportInfoDest.getId()));
            	strSql="insert into work_report_info_attr(id,positionId,position_type,ccPreSettingId,note)"
            			+ " values(%s,%s,'%s','%s','%s')";
            	keyValueConnector.updateBySql(String.format(strSql,workReportInfoDest.getId(),iptStartPosition,position_type,preSettingId,""));
            	String strPresettingCCNos=workReportConnector.getReportPresettingccNos(iptStartPosition, preSettingId);
            	if(!com.mossle.core.util.StringUtils.isBlank(strPresettingCCNos)){
            		String[] split_data = strPresettingCCNos.split(",");
            		for (int i = 0; i < split_data.length; i++) {
                        WorkReportCc workReportCc = new WorkReportCc();
                        String reportleader = workReportInfoDest.getSendee().toString();
                        if (!(reportleader.equals(split_data[i]))) {
                            workReportCc.setStatus("0");
                            workReportCc.setCcno(Long.parseLong(split_data[i]));
                            workReportCc.setWorkReportInfo(workReportInfoDest);
                            workReportCc.setCcType("2");	//走上级/条线的抄送 add by lilei at 2018.12.19
                            workReportCcManager.save(workReportCc);
                        }
                    }
            	}
        	}

            // 抄送------------------------------------------------------------------------------------------------
            if (!strTeamNos.isEmpty()) {
                String[] split_data = strTeamNos.split(",");
                for (String ccno : split_data) {
                    // 如果抄送人里也有接收人，跳过，不再加抄送。
                    if (ccno.equals(strReceiveCode) || ccno.equals(strPerCode))
                        continue;

                    WorkReportCc workReportCc = new WorkReportCc();
                    workReportCc.setStatus("0");
                    workReportCc.setCcno(Long.parseLong(ccno));
                    workReportCc.setWorkReportInfo(workReportInfoDest);
                    workReportCcManager.save(workReportCc);
                }
            }

            // 附件---------------------------------------------------------------------------------------------------------
            if (!strAnnex.isEmpty()) {
                String[] split_data = strAnnex.split(",");
                for (String path : split_data) {
                    fileUploadAPI.uploadFile(path, "1", Long.toString(workReportInfoDest.getId()), "OA/report", "0");
                }
            }

            // 发消息===========================================================
            String bussinessId = workReportInfoDest.getId().toString();
            String tenantId = tenantHolder.getTenantId();
            /*
             * 向汇报接收人发送提醒消息 a)标题：[汇报标题]汇报提醒； b)内容：[汇报人姓名]向您提交了[汇报标题]汇报，请查看。
             */
            String title = "[" + workReportInfoDest.getTitle() + "]汇报提醒";
            String content = "汇报人[" + percodeDTO.getDisplayName() + "]" + "向您提交了" + "[" + workReportInfoDest.getTitle()
                    + "]汇报，请查看。";
            String receiver = workReportInfoDest.getSendee().toString();
            notificationConnector.send(bussinessId, tenantId, strPerCode, receiver, title, content,
                    MsgConstants.MSG_TYPE_REPORT);

            // 新建发送消息给抄送人
            /*
             * a)标题：[汇报标题]汇报提醒； b)内容：[汇报人姓名]给您抄送了[汇报标题]汇报，请查看。
             */
            List<WorkReportCc> ccList = workReportCcManager.findBy("workReportInfo.id", workReportInfoDest.getId());
            if (ccList != null && ccList.size() > 0) {
                content = "[" + percodeDTO.getDisplayName() + "]" + "给您抄送了" + "[" + workReportInfoDest.getTitle()
                        + "]汇报，请查看。";
                for (WorkReportCc cc : ccList) {
                    receiver = cc.getCcno().toString();
                    notificationConnector.send(bussinessId, tenantId, strPerCode, receiver, title, content,
                            MsgConstants.MSG_TYPE_REPORT);
                }
            }

            // 返回======================================================================
            returnMap.put("bSuccess", "true");
            returnMap.put("strMsg", "提交成功");

        } catch (Exception e) {
            e.printStackTrace();
            logger.debug("WorkreportAdd Exception=" + e.toString());
            returnMap.put("bSuccess", "false");
            returnMap.put("strMsg", "提交错误，请联系管理员");
        }

        // System.out.println(returnMap);
        return returnMap;

        // {"bSuccess":"true","strMsg":"保存成功"}
    }

    public Map<String, Object> WorkReportCopyToMe(Map<String, Object> decryptedMap) {
        Map<String, Object> returnMap = new HashMap<String, Object>();

        try {
            // 验证参数================================================================================
            if (!decryptedMap.containsKey("percode") || !decryptedMap.containsKey("strPageIndex")
                    || !decryptedMap.containsKey("strPageSize")) {
                returnMap.put("bSuccess", "false");
                returnMap.put("strMsg", "参数错误");
                return returnMap;
            }

            // 获取数据================================================
            String where_definition = " datastatus=1 ";
            ArrayList<Object> argsList = new ArrayList<Object>();// sql参数列表

            if (decryptedMap.containsKey("percode")) {
                if (!decryptedMap.get("percode").toString().isEmpty()) {
                	//add by lilei at 2018.12.20
                    List<PartyEntity> postPartyEntityList=partyOrgConnector.getPostByUserId(decryptedMap.get("percode").toString());
                    List<String> postIdList=new ArrayList<String>();
                    for (PartyEntity postPartyEntity : postPartyEntityList) {
                    	postIdList.add(postPartyEntity.getId().toString());
            		}
                    if(postIdList.size()<1){
                    	where_definition += " and ccno=? ";
                        argsList.add(decryptedMap.get("percode"));
                    }
                    else{
                        postIdList.add(decryptedMap.get("percode").toString());
                    	String strPostId=Joiner.on(",").join(postIdList);
                    	where_definition += " and ccno in("+strPostId+")";
                    	//argsList.add(strPostId);
                    }
                }
            }

            if (decryptedMap.containsKey("title")) {
                if (!decryptedMap.get("title").toString().isEmpty()) {
                    where_definition += " and title like '%"+decryptedMap.get("title")+"%' ";
                    //argsList.add("'%"+decryptedMap.get("title")+"%'");
                }
            }
            
            if (decryptedMap.containsKey("userName")) {
                if (!decryptedMap.get("userName").toString().isEmpty()) {
                	String strSql="select id from party_entity where `NAME` like '%"+decryptedMap.get("userName").toString()+"%'";
            		List<String> userNameList=jdbcTemplate.queryForList(strSql, String.class);
            		if(userNameList.size()<1){
            			userNameList.add("0");
            		}
            		String userIds=Joiner.on(",").join(userNameList);
                    where_definition += String.format(" and USER_ID in(%s) ", userIds);
                    //argsList.add(userIds);
                }
            }
            
            
            if (decryptedMap.containsKey("type")) {
                if (!decryptedMap.get("type").toString().isEmpty()) {
                    where_definition += " and type=? ";
                    argsList.add(decryptedMap.get("type"));
                }
            }
            
            boolean isSelectPreSetting=false;	//查询条件是否包含条线
            if (decryptedMap.containsKey("ccPreSettingId")) {
                if (!decryptedMap.get("ccPreSettingId").toString().isEmpty()) {
                	isSelectPreSetting=true;
                    where_definition += " and ccPreSettingId=? ";
                    argsList.add(decryptedMap.get("ccPreSettingId"));
                }
            }

            if (decryptedMap.containsKey("status")) {
                if (!decryptedMap.get("status").toString().isEmpty()) {
                    where_definition += " and c.status=? ";
                    argsList.add(decryptedMap.get("status"));
                }
            }

            if (decryptedMap.containsKey("strStart")) {
                if (!decryptedMap.get("strStart").toString().isEmpty()) {
                    where_definition += " and report_date>=? ";
                    argsList.add(decryptedMap.get("strStart"));
                }
            }

            if (decryptedMap.containsKey("strEnd")) {
                String end_report_date = decryptedMap.get("strEnd").toString();
                if (!end_report_date.isEmpty()) {
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        Date endPublishDate = sdf.parse(end_report_date);

                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(endPublishDate);
                        calendar.add(Calendar.DATE, 1);

                        where_definition += " and report_date<? ";
                        argsList.add(calendar.getTime());
                    } catch (Exception e) {
                        // TODO 自动生成的 catch 块
                        e.printStackTrace();
                    }
                }
            }

            List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
            // String sql = "SELECT * FROM `v_h5_work_report_cc` WHERE " + where_definition + " LIMIT ?,?";
            String sql = getReportCcSql(isSelectPreSetting) + " WHERE " + where_definition + " group by r.id order by r.id desc,c.cc_type LIMIT ?,?";
            
            long pageIndex = Long.valueOf(decryptedMap.get("strPageIndex").toString());
            long row_count = Long.valueOf(decryptedMap.get("strPageSize").toString());
            long offset = row_count * (pageIndex - 1);
            argsList.add(offset);
            argsList.add(row_count);

            list = jdbcTemplate.queryForList(sql, argsList.toArray());

            // 返回======================================================================
            returnMap.put("bSuccess", "true");
            if (list.size() > 0) {
                returnMap.put("strMsg", "加载成功");
                returnMap.put("datalist", list);
            } else {
                returnMap.put("strMsg", "暂无数据");
            }
        } catch (Exception e) {
            e.printStackTrace();
            returnMap.put("bSuccess", "false");
            returnMap.put("strMsg", "加载错误，请联系管理员");
        }

        return returnMap;

        // {"bSuccess":"true","strMsg":"暂无数据"}
    }

    public Map<String, Object> WorkReportTurnToMe(Map<String, Object> decryptedMap) {
        // {strPageSize=5, strPerCode=2, percode=2, method=WorkReportTurnToMe,
        // sign=e458da11bb70689df863747cb2020546, strPageIndex=1, timestamp=}

        Map<String, Object> returnMap = new HashMap<String, Object>();

        try {
            // 验证参数================================================================================
            if (!decryptedMap.containsKey("percode") || !decryptedMap.containsKey("strPageIndex")
                    || !decryptedMap.containsKey("strPageSize")) {
                returnMap.put("bSuccess", "false");
                returnMap.put("strMsg", "参数错误");
                return returnMap;
            }

            // 获取数据================================================
            String where_definition = " datastatus=1 ";
            ArrayList<Object> argsList = new ArrayList<Object>();// sql参数列表

            if (decryptedMap.containsKey("percode")) {
                if (!decryptedMap.get("percode").toString().isEmpty()) {
                    where_definition += " and f.sendee=? ";
                    argsList.add(decryptedMap.get("percode"));
                }
            }

            if (decryptedMap.containsKey("title")) {
                if (!decryptedMap.get("title").toString().isEmpty()) {
                    where_definition += " and title like '%"+decryptedMap.get("title")+"%' ";
                    //argsList.add("'%"+decryptedMap.get("title")+"%'");
                }
            }
            
            if (decryptedMap.containsKey("userName")) {
                if (!decryptedMap.get("userName").toString().isEmpty()) {
                	String strSql="select id from party_entity where `NAME` like '%"+decryptedMap.get("userName").toString()+"%'";
            		List<String> userNameList=jdbcTemplate.queryForList(strSql, String.class);
            		if(userNameList.size()<1){
            			userNameList.add("0");
            		}
            		String userIds=Joiner.on(",").join(userNameList);
                    where_definition += String.format(" and USER_ID in(%s) ", userIds);
                    //argsList.add(userIds);
                }
            }
            
            if (decryptedMap.containsKey("type")) {
                if (!decryptedMap.get("type").toString().isEmpty()) {
                    where_definition += " and type=? ";
                    argsList.add(decryptedMap.get("type"));
                }
            }

            if (decryptedMap.containsKey("status")) {
                if (!decryptedMap.get("status").toString().isEmpty()) {
                    where_definition += " and status=? ";
                    argsList.add(decryptedMap.get("status"));
                }
            }

            if (decryptedMap.containsKey("strStart")) {
                if (!decryptedMap.get("strStart").toString().isEmpty()) {
                    where_definition += " and report_date>=? ";
                    argsList.add(decryptedMap.get("strStart"));
                }
            }

            if (decryptedMap.containsKey("strEnd")) {
                String end_report_date = decryptedMap.get("strEnd").toString();
                if (!end_report_date.isEmpty()) {
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        Date endPublishDate = sdf.parse(end_report_date);

                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(endPublishDate);
                        calendar.add(Calendar.DATE, 1);

                        where_definition += " and report_date<? ";
                        argsList.add(calendar.getTime());
                    } catch (Exception e) {
                        // TODO 自动生成的 catch 块
                        e.printStackTrace();
                    }
                }
            }

            List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
            // String sql = "SELECT * FROM `v_h5_work_report_forward` WHERE " + where_definition + " LIMIT ?,?";
            
            String sql = getReportForwardSql() + " WHERE " + where_definition + " order by r.id DESC LIMIT ?,?";
            
            long pageIndex = Long.valueOf(decryptedMap.get("strPageIndex").toString());
            long row_count = Long.valueOf(decryptedMap.get("strPageSize").toString());
            long offset = row_count * (pageIndex - 1);
            argsList.add(offset);
            argsList.add(row_count);

            list = jdbcTemplate.queryForList(sql, argsList.toArray());

            // 返回======================================================================
            returnMap.put("bSuccess", "true");
            if (list.size() > 0) {
                returnMap.put("strMsg", "加载成功");
                returnMap.put("datalist", list);
            } else {
                returnMap.put("strMsg", "暂无数据");
            }
        } catch (Exception e) {
            e.printStackTrace();
            returnMap.put("bSuccess", "false");
            returnMap.put("strMsg", "加载错误，请联系管理员");
        }

        return returnMap;

        // {"bSuccess":"true","strMsg":"暂无数据"}
    }

    public Map<String, Object> WorkReportFeedback(Map<String, Object> decryptedMap) {
        // {strPerCode=3, percode=3, strReportID=800028036366378,
        // method=WorkReportFeedback, sign=12c4d8c82be9ca168cab35dc2e421ae1,
        // strContent=反馈, timestamp=}

        String content = decryptedMap.get("strContent").toString();

        String strReportID = decryptedMap.get("strReportID").toString();
        Long id = Long.valueOf(strReportID);

        String strPerCode = decryptedMap.get("strPerCode").toString();
        Long userId = Long.parseLong(strPerCode);

        Map<String, Object> mapList = new HashMap<String, Object>();
        try {
            if (content == null || content == "") {
                mapList.put("bSuccess", "false");
                mapList.put("strMsg", "反馈内容不能为空");
                return mapList;
            }

            if (id == null || id < 1) {
                mapList.put("bSuccess", "false");
                mapList.put("strMsg", "获取参数错误");
                // logger.debug("汇报反馈-获取参数id错误");
                return mapList;
            }

            WorkReportInfo workReportInfoModel = workReportInfoManager.findUniqueBy("id", id);

            if (workReportInfoModel == null) {
                mapList.put("bSuccess", "false");
                mapList.put("strMsg", "查询汇报信息错误");
                return mapList;
            }

            if (workReportInfoModel.getStatus().equals("0")) {
                mapList.put("bSuccess", "false");
                mapList.put("strMsg", "未读汇报不能反馈");
                return mapList;
            } else if (workReportInfoModel.getStatus().equals("2")) {
                mapList.put("bSuccess", "false");
                mapList.put("strMsg", "此汇报已经被反馈过了^_^");
                return mapList;
            } else if (!workReportInfoModel.getSendee().equals(userId)) {
                mapList.put("bSuccess", "false");
                mapList.put("strMsg", "您不是此汇报的接收人");
                return mapList;
            }

            // 赋值
            workReportInfoModel.setFeedback(content);
            workReportInfoModel.setFeedbacktime(new Date());
            workReportInfoModel.setStatus("2");// 已反馈

            workReportInfoManager.save(workReportInfoModel);
            mapList.put("bSuccess", "true");
            mapList.put("strMsg", "反馈提交成功");

        } catch (ArithmeticException e) {
            mapList.put("bSuccess", "false");
            mapList.put("strMsg", "汇报反馈异常");
            // logger.error("汇报反馈异常："+e.getMessage()+"\r\n"+e.fillInStackTrace());
        }
        return mapList;

        // {"bSuccess":"true","strMsg":"保存成功"}
    }

    public Map<String, Object> ReportTurn(Map<String, Object> decryptedMap) {
        // {strPerCode=2, percode=2, strReportID=800028036366356,
        // strIds=796929107165184,796936873328640, method=ReportTurn,
        // sign=951884d4f3997bd39d2ac48f71b404da, strNote=转发, timestamp=}

        Map<String, Object> returnMap = new HashMap<String, Object>();

        try {
            // 验证参数================================================================================
            if (!decryptedMap.containsKey("strReportID")) {
                returnMap.put("bSuccess", "false");
                returnMap.put("strMsg", "参数错误");
                return returnMap;
            }

            String selectIds = decryptedMap.get("strIds").toString();// 接收人编号，逗号分割
            String[] selectArrays = selectIds.split(",");
            ArrayList<String> selectList = new ArrayList<String>(Arrays.asList(selectArrays));
            String strNote = decryptedMap.get("strNote").toString();// 转发内容
            String strPerCode = decryptedMap.get("strPerCode").toString();// 转发人编号
            UserDTO percodeDTO = userConnector.findById(strPerCode);
            String strReportID = decryptedMap.get("strReportID").toString();// 汇报id
            Long reportid = Long.valueOf(strReportID);
            String tenantId = tenantHolder.getTenantId();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            WorkReportInfo reportModel = workReportInfoManager.findUniqueBy("id", reportid);
            if (reportModel == null) {
                returnMap.put("bSuccess", "false");
                returnMap.put("strMsg", "查询不到汇报数据");
                return returnMap;
            }

            // 不能转发给发布人
            if (selectIds.contains(reportModel.getUserId().toString())) {
                AccountInfo userInfo = accountInfoManager.findUniqueBy("id", reportModel.getUserId());
                returnMap.put("bSuccess", "false");
                returnMap.put("strMsg", userInfo.getDisplayName() + " 是汇报发送人，不能转发。");
                return returnMap;
            }

            // 之前已经转发了的，合并备注。
            List<WorkReportForward> workReportForwardList = workReportForwardManager.findBy("workReportInfo.id",
                    reportid);
            for (WorkReportForward workReportForward : workReportForwardList) {
                String sendee = workReportForward.getSendee().toString();
                if (selectIds.contains(sendee)) {
                    String remarks = workReportForward.getRemarks();
                    remarks += " [" + percodeDTO.getDisplayName() + "]于" + formatter.format(new Date()) + "转发："
                            + strNote;
                    workReportForward.setRemarks(remarks);
                    workReportForwardManager.save(workReportForward);
                    selectList.remove(sendee);// 合并后删除新转发列表项
                }
            }

            // 保存数据，发消息================================================
            // 给转发接收人发送消息提醒
            String title = "[" + reportModel.getTitle() + "]汇报转发提醒";// 标题：[汇报标题]汇报转发提醒
            String content = "[" + percodeDTO.getDisplayName() + "]将[" + reportModel.getTitle() + "]汇报转发给您，请查看。";// 内容：[汇报转发人]将[汇报标题]汇报转发给您，请查看。

            //添加是否转发反馈内容的信息 add by lilei at 2018.11.21
            String isTurnFeeback="1";
            if(decryptedMap.containsKey("isTurnFeeback")){
            	isTurnFeeback=decryptedMap.get("isTurnFeeback")==null?"1":decryptedMap.get("isTurnFeeback").toString();
            }
            
            for (String receiver : selectList) {
                WorkReportForward workModel = new WorkReportForward();
                workModel.setStatus("0");
                workModel.setSendee(Long.parseLong(receiver));
                workModel.setForwarder(Long.parseLong(strPerCode));
                workModel.setWorkReportInfo(reportModel);
                workModel.setForwardtime(new Date());
                workModel.setRemarks(strNote);
                workModel.setIsfeedbackforward(isTurnFeeback);
                workReportForwardManager.save(workModel);

                // 发消息=========================================
                notificationConnector.send(strReportID, tenantId, strPerCode, receiver, title, content,
                        MsgConstants.MSG_TYPE_REPORT);
            }

            // 返回======================================================================
            returnMap.put("bSuccess", "true");
            returnMap.put("strMsg", "转发成功");

        } catch (Exception e) {
            e.printStackTrace();
            returnMap.put("bSuccess", "false");
            returnMap.put("strMsg", "转发错误，请联系管理员");
        }

        // System.out.println(returnMap);
        return returnMap;

        // {"bSuccess":"true","strMsg":"保存成功"}
    }


    /**
     * 未读汇报可以删除  by wanghan
     *
     * @param decryptedMap
     * @return
     */
    public Map<String, Object> UnreadReportDel(Map<String, Object> decryptedMap) {

        Map<String, Object> returnMap = new HashMap<String, Object>();

        try {
            // 验证参数================================================================================
            if (!decryptedMap.containsKey("strReportID")) {
                logger.debug("汇报删除-获取参数id错误");
                returnMap.put("bSuccess", "false");
                returnMap.put("strMsg", "参数错误");
                return returnMap;
            }

            String strReportID = decryptedMap.get("strReportID").toString();
            Long id = Long.valueOf(strReportID);

            WorkReportInfo reportModel = workReportInfoManager.findUniqueBy("id", id);
            if (reportModel == null) {
                logger.debug("汇报删除-没有查询到汇报信息");
                returnMap.put("bSuccess", "false");
                returnMap.put("strMsg", "没有查询到汇报信息");
                return returnMap;
            }

            if (!reportModel.getStatus().equals("0")) {
                logger.debug("汇报删除(物理)操作-汇报状态已改变");
                returnMap.put("bSuccess", "false");
                returnMap.put("strMsg", "汇报状态已更改，请刷新后查看。");
                return returnMap;
            }
            workReportInfoManager.remove(reportModel);
            // 返回======================================================================
            returnMap.put("bSuccess", "true");
            returnMap.put("strMsg", "删除成功");
        } catch (ArithmeticException e) {
            returnMap.put("bSuccess", "false");
            returnMap.put("strMsg", "删除错误，请联系管理员");
            logger.error("汇报删除(物理)操作-：" + e.getMessage() + "\r\n" + e.fillInStackTrace());
        }

        // System.out.println(returnMap);
        return returnMap;
    }

    /**
     * 汇报查询条件 ADD BY LILEI AT 2019.01.16
     * **/
    public Map<String, Object>  ReportCCPreSetting(Map<String, Object> decryptedMap){
    	Map<String, Object> returnMap = new HashMap<String, Object>();
    	try {
    		if (!decryptedMap.containsKey("strPerCode") || StringUtils.isBlank(decryptedMap.get("strPerCode").toString())) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "参数错误");
				logger.debug("手机APP-获取汇报条线-手机没有传入strPerCode参数");
				return returnMap;
			}
    		
    		List<Map<String,Object>> ccPresettingList=workReportConnector.getReportPreSetting();
    		returnMap.put("bSuccess", "true");
    		returnMap.put("strMsg", "加载成功");
    		returnMap.put("ccPreSetting", ccPresettingList);
    		
	    } catch (Exception e) {
	        returnMap.put("bSuccess", "false");
	        returnMap.put("strMsg", "获取数据错误，请联系管理员");
	        logger.error("获取汇报查询初始化数据错误-：" + e.getMessage() + "\r\n" + e.fillInStackTrace());
	    }
    	return returnMap;
    } 
    
    private String getSql() {
    	
    	StringBuffer sqlBuf = new StringBuffer();
        sqlBuf.append("SELECT r.ID AS ID,r.ID AS RP01,r.`code` AS CODE,r.type AS RP05,");
        sqlBuf.append("GET_DICT_VALUE_BY_TYPE_NAME ('work_report_type',r.type) AS RP05C,");
        sqlBuf.append("r.title AS title,r.title AS RP02,r.completed AS completed,r.dealing AS dealing,");
        sqlBuf.append("(CASE r.type WHEN 4 THEN r.problems ELSE r.dealing END ) AS RP03,");
        sqlBuf.append("r.coordinate AS coordinate,r.problems AS problems,r.datastatus AS datastatus,");
        sqlBuf.append("r.`status` AS status,r.`status` AS RP06,");
        sqlBuf.append("GET_DICT_VALUE_BY_TYPE_NAME ('work_report_status',r.`status`) AS RP06C,");
        sqlBuf.append("r.sendee AS sendee,r.sendee AS RP07,GET_DISPLAY_NAME_BY_ID (r.sendee) AS RP07C,");
        sqlBuf.append("r.remarks AS remarks,r.other AS other,r.feedback AS RP08,r.USER_ID AS USER_ID,");
        sqlBuf.append("r.USER_ID AS RP10,GET_DISPLAY_NAME_BY_ID (r.USER_ID) AS RP10C,r.report_date AS report_date,");
        sqlBuf.append("r.report_date AS RP11,r.feedbacktime AS RP09,r.lastedittime AS lastedittime,");
        sqlBuf.append("GET_WORK_REPORT_CC_NAMES_BY_INFO_ID (r.ID) AS CR03,f_store_paths (r.ID, 0, 'OA/report') AS RP04");
        sqlBuf.append(" FROM work_report_info r");
        
        return sqlBuf.toString();
    }
    
    private String getReportForwardSql() {
    	
    	StringBuffer sqlBuf = new StringBuffer();
        sqlBuf.append("SELECT r.ID AS ID,r.ID AS RP01,r.`code` AS CODE,r.type AS RP05,");
        sqlBuf.append("GET_DICT_VALUE_BY_TYPE_NAME ('work_report_type',r.type) AS RP05C,");
        sqlBuf.append("r.title AS title,r.title AS RP02,r.datastatus AS datastatus,f.sendee AS sendee,");
        sqlBuf.append("f.sendee AS TR03,GET_DISPLAY_NAME_BY_ID (f.sendee) AS PN06,f.`status` AS status,");
        sqlBuf.append("f.`status` AS TR05,f.forwarder AS forwarder,f.forwarder AS TR04,f.forwardtime AS TR07,");
        sqlBuf.append("GET_DISPLAY_NAME_BY_ID (f.forwarder) AS TR04C,f.forwardtime AS forwardtime,f.remarks AS TR06");
        sqlBuf.append(" FROM work_report_forward f");
        sqlBuf.append(" JOIN work_report_info r ON f.info_id = r.ID");
        
        return sqlBuf.toString();
    }

    private String getReportCcSql(boolean isSelectPreSetting) {
    	
    	StringBuffer sqlBuf = new StringBuffer();
        sqlBuf.append("SELECT r.ID AS ID,r.ID AS RP01,r.`code` AS `CODE`,r.type AS RP05,");
        sqlBuf.append("GET_DICT_VALUE_BY_TYPE_NAME('work_report_type',r.type) AS RP05C,");
        sqlBuf.append("r.title AS RP02,r.completed AS RP03,r.dealing AS dealing,r.coordinate AS coordinate,");
        sqlBuf.append("r.problems AS problems,r.datastatus AS datastatus,r.status AS RP06,");
        sqlBuf.append("GET_DICT_VALUE_BY_TYPE_NAME('work_report_status',r.`status`) AS RP06C,");
        sqlBuf.append("r.sendee AS sendee,r.sendee AS RP07,GET_DISPLAY_NAME_BY_ID (r.sendee) AS RP07C,");
        sqlBuf.append("r.remarks AS remarks,r.other AS other,r.feedback AS RP08,r.USER_ID AS USER_ID,r.USER_ID AS RP10,");
        sqlBuf.append("GET_DISPLAY_NAME_BY_ID (r.USER_ID) AS RP10C,r.report_date AS report_date,r.report_date AS RP11,");
        sqlBuf.append("r.feedbacktime AS RP09,r.lastedittime AS lastedittime,c.ccno AS ccno,c.`status` AS `status`,");
        sqlBuf.append("GET_DICT_VALUE_BY_TYPE_NAME ('work_report_status',c.`status`) AS CR04,min(ifnull(c.cc_type,'1')) as cc_type");
        sqlBuf.append(" FROM work_report_cc c");
        sqlBuf.append(" JOIN work_report_info r ON c.INFO_ID = r.ID");
        
        if(isSelectPreSetting){
        	sqlBuf.append(" INNER JOIN  work_report_info_attr attr on r.ID = attr.ID");
        }
        
        return sqlBuf.toString();
    }

    @Resource
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Resource
    public void setWorkReportInfoManager(WorkReportInfoManager workReportInfoManager) {
        this.workReportInfoManager = workReportInfoManager;
    }

    @Resource
    public void setWorkReportForwardManager(WorkReportForwardManager workReportForwardManager) {
        this.workReportForwardManager = workReportForwardManager;
    }

    @Resource
    public void setWorkReportCcManager(WorkReportCcManager workReportCcManager) {
        this.workReportCcManager = workReportCcManager;
    }

    @Resource
    public void setFileUploadAPI(FileUploadAPI fileUploadAPI) {
        this.fileUploadAPI = fileUploadAPI;
    }

    @Resource
    public void setAccountInfoManager(AccountInfoManager accountInfoManager) {
        this.accountInfoManager = accountInfoManager;
    }

    @Resource
    public void setUserConnector(UserConnector userConnector) {
        this.userConnector = userConnector;
    }

    @Resource
    public void setNotificationConnector(NotificationConnector notificationConnector) {
        this.notificationConnector = notificationConnector;
    }

    @Resource
    public void setTenantHolder(TenantHolder tenantHolder) {
        this.tenantHolder = tenantHolder;
    }

}
