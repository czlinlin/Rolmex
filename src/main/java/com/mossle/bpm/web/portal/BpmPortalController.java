package com.mossle.bpm.web.portal;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.mossle.api.humantask.HumanTaskConnector;
import com.mossle.api.humantask.HumanTaskDTO;
import com.mossle.api.operation.OperationConnector;
import com.mossle.api.org.OrgConnector;
import com.mossle.api.process.ProcessConnector;
import com.mossle.api.tenant.TenantHolder;
import com.mossle.bpm.persistence.domain.BpmCategory;
import com.mossle.bpm.persistence.domain.BpmProcess;
import com.mossle.bpm.persistence.manager.BpmProcessManager;
import com.mossle.common.utils.DeEnCode;
import com.mossle.common.utils.StringUtils;
import com.mossle.core.auth.CurrentUserHolder;
import com.mossle.core.page.Page;
import com.mossle.core.query.PropertyFilter;
import com.mossle.humantask.persistence.domain.UnfinishProcessInstance;
import com.mossle.party.persistence.domain.PartyEntity;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.history.HistoricProcessInstance;
import org.omg.CORBA.PRIVATE_MEMBER;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("bpm/portal")
public class BpmPortalController {
    private static Logger logger = LoggerFactory
            .getLogger(BpmPortalController.class);
    private BpmProcessManager bpmProcessManager;
    private CurrentUserHolder currentUserHolder;
    private TenantHolder tenantHolder;
    private ProcessConnector processConnector;
    private OrgConnector orgConnector;
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private OperationConnector operationConnector;

    @RequestMapping("runningProcesses")
    public String runningProcesses() {

        String userId = currentUserHolder.getUserId();
        String tenantId = tenantHolder.getTenantId();

        Page potalPage = new Page(1, 10, "create_time", "ASC");
        List<PropertyFilter> propertyFilters = new ArrayList<PropertyFilter>();
        //Page page = findPersonalTasks = humanTaskConnector.findPersonalTasks(userId, tenantId, 1, 10);

        Page page = processConnector.findRunningProcessInstancesToPortal(potalPage, propertyFilters, userId);
        /*      .processInstanceTenantId(tenantId).startedBy(userId)
                .unfinished().list();
		*/

        StringBuilder buff = new StringBuilder();
        buff.append("<table class='table table-hover'>");
        buff.append("  <thead>");
        buff.append("    <tr>");
        buff.append("      <th>主题</th>");
        buff.append("      <th></th>");
        buff.append("    </tr>");
        buff.append("  </thead>");
        buff.append("  <tbody>");

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        for (UnfinishProcessInstance vo : (List<UnfinishProcessInstance>) page.getResult()) {
            String url = StringUtils.isBlank(vo.getUrl()) ? "" : vo.getUrl().replace("'", "");
        	
            //如果是自定义申请，直接跳转到自定义的详情页
            if (url.indexOf("custom") >= 0) {
            	 buff.append("    <tr>");
                 buff.append("      <td title='" + vo.getTheme() + "'>" + this.substr(vo.getTheme()) + "</td>");
                 buff.append("      <td>");
                 buff.append("        <a href='" + ".."
                         + "/bpm/workspace-viewHistory.do?processInstanceId="
                         + vo.getProcessInstanceId() + "&url=" + url
                         + "' class='btn btn-xs btn-primary' target='_blank'>详情</a>");
                 buff.append("      </td>");
                 buff.append("    </tr>");
            }else{	//不是自定义申请，跳转至普通流程的详情页
            
		            buff.append("    <tr>");
		            buff.append("      <td title='" + vo.getTheme() + "'>" + this.substr(vo.getTheme()) + "</td>");
		            buff.append("      <td>");
		            
		            buff.append("   <a href='" + ".."
		                      		  + vo.getUrl()+"?processInstanceId="
		                              + vo.getProcessInstanceId()
		                              +"&isPrint=false"
		                              + "' class='btn btn-xs btn-primary' target='_blank'>详情</a>   </c:if> ");
		            buff.append("      </td>");
		            buff.append("    </tr>");
		            }
        }

        buff.append("  </tbody>");
        buff.append("</table>");

        return buff.toString();
    }

    @RequestMapping("processes")
    public String processes() {
        String tenantId = tenantHolder.getTenantId();
        String userId = currentUserHolder.getUserId();
        String ids = "";
        List<BpmProcess> bpmProcesses = new ArrayList<BpmProcess>();

        //取用户的所属大区，带回自定义申请页面
        String areaName = "";
        String areaId = "";
        PartyEntity partyEntity  =  orgConnector.findPartyAreaByUserId(userId);
    	
    	if ( partyEntity ==  null){
    		areaName = "";
    		areaId="";
    	}
    	else { areaName = partyEntity.getName();
    			areaId  = Long.toString(partyEntity.getId());
    		}
        
        
        // 查询用户所属岗位
        List<PartyEntity> postList = orgConnector.getPostByUserId(userId);
        if (postList == null || postList.size() == 0) {
            /*String hql = "from BpmProcess where showFlag = 1 and bpmCategory.id = 3 and tenantId=? order by priority";
            bpmProcesses = bpmProcessManager.find(hql, tenantId);*/
            List<BpmProcess> bpms = getCommonBpmProcess();
    		if(bpms.size()>0)
    			bpmProcesses.addAll(bpms);
            ids = "3";
            
        } else {
            String postIds = "";
            String categoryId = "";
            for (PartyEntity vo : postList) {
                postIds += vo.getId() + ",";
            }

            String selectSql = "SELECT DISTINCT b.id as category_id,b.`NAME` as category_name,d.bpmProcessId,"
                    + "bpm.show_flag,bpm.DESCN,bpm.`NAME` as bpm_name,bpm.BYNAME FROM oa_ba_business_post p"
                    + " inner join oa_ba_business_detail d on p.detail_id = d.id"
                    + " inner join oa_ba_businesstype t on d.type_id = t.typeid"
                    + " inner join bpm_process bpm on d.bpmProcessId =bpm.ID"
                    //+ " inner join (select a.* from bpm_process a where a.PRIORITY = (select min(PRIORITY) from bpm_process where BYNAME = a.BYNAME)) bpm on d.bpmProcessId =bpm.ID"
                    + " inner join BPM_CATEGORY b on bpm.CATEGORY_ID = b.ID"
                    + " where bpm.show_flag = 1 and p.post_id in (" + postIds.substring(0, postIds.length() - 1) + ")"
            		+ " and b.id<>3"
        		    + " group by bpm.BYNAME"
                    + " order by b.PRIORITY,bpm.PRIORITY,d.bpmProcessId";
                   
            List<Map<String, Object>> list = jdbcTemplate.queryForList(selectSql);

            for (Map<String, Object> map : list) {
                BpmProcess bpmProcess = new BpmProcess();
                categoryId = StringUtils.convertString(map.get("category_id"));

                bpmProcess.setId(Long.parseLong(StringUtils.convertString(map.get("bpmProcessId"))));
                bpmProcess.setName(StringUtils.convertString(map.get("BYNAME")));
                bpmProcess.setDescn(StringUtils.convertString(map.get("DESCN")));
                bpmProcess.setShowFlag(Integer.parseInt(StringUtils.convertString(map.get("show_flag"))));

                bpmProcesses.add(bpmProcess);

                ids += categoryId + ",";
            }
        	
        	if ("".equals(ids)) {
        		List<BpmProcess> bpms = getCommonBpmProcess();
        		if(bpms.size()>0)
        			bpmProcesses.addAll(bpms);
        		
        	} else {
	        	if (!ids.contains("3")) {
	        		List<BpmProcess> bpms = getCommonBpmProcess();
	        		if(bpms.size()>0)
	        			bpmProcesses.addAll(bpms);
	            }
        	}
        }

        StringBuilder buff = new StringBuilder();
        buff.append("<table class='table table-hover'>");
        buff.append("  <thead>");
        buff.append("    <tr>");
        buff.append("      <th>名称</th>");
        buff.append("      <th width='15%'>&nbsp;</th>");
        buff.append("    </tr>");
        buff.append("  </thead>");
        buff.append("  <tbody>");

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        //String newUserId = DeEnCode.encode(userId);
        
        for (BpmProcess bpmProcess : bpmProcesses) {
        	// TODO　chengze  2017-12-12 
        	 String encryBpmProcessId = DeEnCode.encode(Long.toString(bpmProcess.getId()));
        	 Long categoryId=bpmProcess.getBpmCategory()==null?0L:bpmProcess.getBpmCategory().getId();
        	 buff.append("    <tr>");
             buff.append("      <td>" + bpmProcess.getName() + "</td>");
             //buff.append("      <td title='"+bpmProcess.getName()+"'>" + this.substr(bpmProcess.getName()) + "</td>");
            buff.append("      <td>");
            buff.append("        <a href='"
                    + ".."
                    + "/operation/process-operation-viewStartForm.do?categoryId="
                    + DeEnCode.encode(Long.toString(categoryId))
                    + "&bpmProcessId="
                    + encryBpmProcessId
                    + "' class='btn btn-xs btn-primary'>发起</a>");
            buff.append("      </td>");
            buff.append("    </tr>");
        }

        buff.append("    <tr>");
        buff.append("      <td> 自定义申请   </td>");
        buff.append("      <td>");
        buff.append("        <a href='"
                + ".."

                + "/operationCustom/custom-apply-list.do?userName="+userId
                + "&areaName="+areaName+ "&areaId="+areaId
                + "' class='btn btn-xs btn-primary'>发起</a>");
        buff.append("      </td>");
        buff.append("    </tr>");
        //ckx  2018/7/27
        //请假  
        buff.append("    <tr>");
        buff.append("      <td> 请假申请   </td>");
        buff.append("      <td>");
        buff.append("        <a href='"
                + ".."

                + "/workOperationCustom/custom-work-apply-list.do?userName="+userId
                + "&formType=1"
                + "' class='btn btn-xs btn-primary'>发起</a>");
        buff.append("      </td>");
        buff.append("    </tr>");
        //加班  ckx 2018/7/27
        buff.append("    <tr>");
        buff.append("      <td> 加班申请   </td>");
        buff.append("      <td>");
        buff.append("        <a href='"
                + ".."
                
                + "/workOperationCustom/custom-work-apply-list.do?userName="+userId
                + "&formType=3"
                + "' class='btn btn-xs btn-primary'>发起</a>");
        buff.append("      </td>");
        buff.append("    </tr>");
        //外出出差  ckx  2018/7/27
        buff.append("    <tr>");
        buff.append("      <td> 出差外出申请   </td>");
        buff.append("      <td>");
        buff.append("        <a href='"
                + ".."
                
                + "/workOperationCustom/custom-work-apply-list.do?userName="+userId
                + "&formType=2"
                + "' class='btn btn-xs btn-primary'>发起</a>");
        buff.append("      </td>");
        buff.append("    </tr>");
        //特殊考勤说明  ckx  2018/7/27
        buff.append("    <tr>");
        buff.append("      <td> 特殊考勤说明申请   </td>");
        buff.append("      <td>");
        buff.append("        <a href='"
                + ".."
                
                + "/workOperationCustom/custom-work-apply-list.do?userName="+userId
                + "&formType=4"
                + "' class='btn btn-xs btn-primary'>发起</a>");
        buff.append("      </td>");
        buff.append("    </tr>");
        
        
        buff.append("  </tbody>");
        buff.append("</table>");

        return buff.toString();
    }
    
    private List<BpmProcess> getCommonBpmProcess(){
    	List<BpmProcess> bpmProcesses=new ArrayList<BpmProcess>();
    	String hql = "from BpmProcess where showFlag = 1 and bpmCategory.id = 3 and tenantId=? order by priority";
		List<BpmProcess> bpms = bpmProcessManager.find(hql,tenantHolder.getTenantId());
		List<String> commonProcessByNameList=new ArrayList<String>();
		for (BpmProcess bpmProcess : bpms) {
			if(operationConnector.IsShowCommonProcess(bpmProcess.getId(), currentUserHolder.getUserId())){
				if(commonProcessByNameList.size()>0&&commonProcessByNameList.contains(bpmProcess.getByName())){
        			continue;
        		}
    			BpmProcess bpmProcess_common = new BpmProcess();
    			bpmProcess_common.setId(bpmProcess.getId());
    			bpmProcess_common.setName(bpmProcess.getByName());
    			bpmProcess_common.setDescn(bpmProcess.getDescn());
    			bpmProcess_common.setShowFlag(bpmProcess.getShowFlag());
    			bpmProcess_common.setBpmCategory(bpmProcess.getBpmCategory());
    			bpmProcesses.add(bpmProcess_common);
    			
    			commonProcessByNameList.add(bpmProcess.getByName());
			}
		}
		return bpmProcesses;
    }

    public String substr(String text) {
        if (org.apache.commons.lang3.StringUtils.isBlank(text)) {
            return "";
        }

        if (text.trim().length() < 25) {
            return text.trim();
        }

        return text.trim().substring(0, 25) + "...";
    }

    @Resource
    public void setBpmProcessManager(BpmProcessManager bpmProcessManager) {
        this.bpmProcessManager = bpmProcessManager;
    }

    @Resource
    public void setCurrentUserHolder(CurrentUserHolder currentUserHolder) {
        this.currentUserHolder = currentUserHolder;
    }

    @Resource
    public void setTenantHolder(TenantHolder tenantHolder) {
        this.tenantHolder = tenantHolder;
    }

    @Resource
    public void setProcessConnector(ProcessConnector processConnector) {
        this.processConnector = processConnector;
    }

    @Resource
    public void setOrgConnector(OrgConnector orgConnector) {
        this.orgConnector = orgConnector;
    }

    @Resource
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


}
