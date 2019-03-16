package com.mossle.base.web;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.hql.internal.ast.tree.QueryNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreInvocationAttribute;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mossle.api.keyvalue.KeyValueConnector;
import com.mossle.api.keyvalue.Record;
import com.mossle.api.operation.OperationConnector;
import com.mossle.api.party.PartyConnector;
import com.mossle.api.party.PartyDTO;
import com.mossle.api.tenant.TenantHolder;
import com.mossle.api.user.UserConnector;
import com.mossle.api.user.UserDTO;
import com.mossle.base.persistence.domain.BranchApprovalLinkEntity;
import com.mossle.base.persistence.domain.BusinessDetailEntity;
import com.mossle.base.persistence.domain.DetailPostEntity;
import com.mossle.base.persistence.manager.BranchApprovalLinkEntityManager;
import com.mossle.base.persistence.manager.BusinessDetailManager;
import com.mossle.base.persistence.manager.DetailPostManager;
import com.mossle.base.service.DetailPostService.GetWhole;
import com.mossle.base.service.DetailProcessService;
import com.mossle.bpm.persistence.domain.BpmConfNode;
import com.mossle.bpm.persistence.domain.BpmProcess;
import com.mossle.bpm.persistence.manager.BpmProcessManager;
import com.mossle.common.utils.StringUtils;
import com.mossle.core.export.Exportor;
import com.mossle.core.export.TableModel;
import com.mossle.core.id.IdGenerator;
import com.mossle.core.mapper.BeanMapper;
import com.mossle.core.page.Page;
import com.mossle.core.query.PropertyFilter;
import com.mossle.core.spring.MessageHelper;
import com.mossle.dict.persistence.domain.DictInfo;
import com.mossle.dict.support.DictConnectorImpl;
import com.mossle.keyvalue.support.DatabaseKeyValueConnector;
import com.mossle.operation.persistence.manager.CustomApproverManager;
import com.mossle.operation.web.ProcessOperationController;
import com.mossle.party.persistence.domain.PartyEntity;
import com.mossle.party.persistence.manager.PartyEntityManager;
import com.mossle.util.StringUtil;
import com.mysql.jdbc.jmx.LoadBalanceConnectionGroupManager;
/** 
 * @author  cz 
 * @version 2017年9月7日
 * 类说明 
 */
@Controller
@RequestMapping("dict")
public class BusinessDetailController {

	private static Logger logger = LoggerFactory
            .getLogger(ProcessOperationController.class);
	
	private BeanMapper beanMapper = new BeanMapper();
    private MessageHelper messageHelper;
    private Exportor exportor;
    private TenantHolder tenantHolder;
    private BusinessDetailManager businessDetailManager;
    private PartyEntityManager partyEntityManager;
    private DetailPostManager detailPostManager ;
    private DetailProcessService detailProcessService ;
    private KeyValueConnector keyValueConnector;
    @Autowired
    private BpmProcessManager bpmProcessManager;
    @Autowired
    private CustomApproverManager customApproverManager;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private DictConnectorImpl dictConnectorImpl;
    @Autowired
    private IdGenerator idGenerator;
    @Autowired
    private DatabaseKeyValueConnector databaseKeyValueConnector;
    private OperationConnector operationConnector;
    private BranchApprovalLinkEntityManager branchApprovalLinkEntityManager;
    private PartyConnector partyConnector;
    private UserConnector userConnector;

    @RequestMapping("dict-business-detail-list")
    public String businessTypeList(@ModelAttribute Page page,
            @RequestParam Map<String, Object> parameterMap, Model model) {
        String tenantId = tenantHolder.getTenantId();
        String bpmProcessId = "";
        String bpmProcessName = StringUtil.toString(parameterMap.get("filter_INS_bpmProcessId"));
        if(StringUtils.isNotBlank(bpmProcessName)){
        	String hql = "from BpmProcess where  name like ? ";
            List<BpmProcess> BpmProcessList = bpmProcessManager.find(hql, '%' + bpmProcessName + '%');
            
            for (BpmProcess bpmProcess : BpmProcessList) {
            	bpmProcessId += bpmProcess.getId()+",";
    		}
            
            if(StringUtils.isNotBlank(bpmProcessId)){
            	bpmProcessId = bpmProcessId.substring(0, bpmProcessId.length()-1);
            }
            parameterMap.put("filter_INS_bpmProcessId", bpmProcessId);
        }
        List<PropertyFilter> propertyFilters = PropertyFilter
                .buildFromMap(parameterMap);
        
        propertyFilters.add(new PropertyFilter("EQS_tenantId", tenantId));
        page = businessDetailManager.pagedQuery(page, propertyFilters);
        
        model.addAttribute("page", page);
        return "dict/dict-business-detail-list";
    }
    
    //修改
    @RequestMapping("dict-business-detail-input")
    public String input(@RequestParam(value = "id", required = false) Long id,
            Model model) {
        if (id != null) {
            BusinessDetailEntity businessDetailEntity = businessDetailManager.get(id);
            model.addAttribute("model", businessDetailEntity);
            
            //根据明细的ID去取岗位名称和id
            
           	String postName="" ;
           	String postId = "";
           	
            String hql = "from DetailPostEntity where  detailID=? ";
            List<DetailPostEntity> detailPostEntity = detailPostManager.find(hql, id);
            if (!detailPostEntity.isEmpty()) {
	            for (DetailPostEntity d : detailPostEntity) {
	            	postName = postName + d.getPostName()+",";
	            	postId = postId + d.getPostID() + ",";
	            }
	            postName =postName.substring(0,postName.length()-1);
	            postId =postId.substring(0,postId.length()-1);
            }
            
            model.addAttribute("postName", postName);
            model.addAttribute("postId", postId);
        }

        return "dict/dict-business-detail-input";
    }
    
    //新建业务类型明细
    @RequestMapping("dict-business-detail-new")
    public String newType() {
    	return "dict/dict-business-detail-new";
    }

    //保存新建的明细
    @RequestMapping("dict-business-detail-save")
    public String save(HttpServletRequest request,
    		RedirectAttributes redirectAttributes,
            Model model) throws Exception {
  	
    	String id = request.getParameter("id");
 
    	//先取旧的数据，与新数据做对比，有修改的信息存入日志
    	BusinessDetailEntity businessDetailEntity = new BusinessDetailEntity();
    	String busiDetailModifyContent = "业务类型明细：";
    	if (id != null) {
    		businessDetailEntity = businessDetailManager.get(Long.parseLong(id));
    	
    		if(!request.getParameter("bpmPName1").equals(businessDetailEntity.getBpmProcessId())){
    			busiDetailModifyContent = busiDetailModifyContent+ " 流程由  " + businessDetailEntity.getBpmProcessId() + " 修改为 "+ request.getParameter("bpmPName1");
    		}
    		if(!request.getParameter("formNames").equals(businessDetailEntity.getFormName())){
    			busiDetailModifyContent = busiDetailModifyContent+ " 表单由  " + businessDetailEntity.getFormName() + " 修改为 "+ request.getParameter("formNames");
    		}
    		if(!request.getParameter("businessTypeName").equals(businessDetailEntity.getBusinessType())){
    			busiDetailModifyContent = busiDetailModifyContent+ " 业务类型由  " + businessDetailEntity.getBusinessType() + " 修改为 "+ request.getParameter("businessTypeName");
    		}
    		if(!request.getParameter("businessDetail").equals(businessDetailEntity.getBusiDetail())){
    			busiDetailModifyContent = busiDetailModifyContent+ " 业务细分由  " + businessDetailEntity.getBusiDetail() + " 修改为 "+ request.getParameter("businessDetail");
    		}
    		if(!request.getParameter("level").equals(businessDetailEntity.getLevel())){
    			busiDetailModifyContent = busiDetailModifyContent+ " 业务级别由  " + businessDetailEntity.getLevel() + " 修改为 "+ request.getParameter("level");
    		}
    		
    		System.out.print(busiDetailModifyContent);
    		//编辑修改业务细分 TODO:sjx 18.11.6
    		BusinessDetailEntity detailEntity = businessDetailManager.get(Long.parseLong(id));
    		if(detailEntity != null && !request.getParameter("businessTypeName").equals(businessDetailEntity.getBusinessType())){
    			String typeId = request.getParameter("businessType");
    			String typeName = request.getParameter("businessTypeName");
    			List<Record> recordList = new ArrayList<>();
    			String sql = "select * from kv_record where businessDetailId="+id;
    			List<Map<String,Object>> list = jdbcTemplate.queryForList(sql);
    			for(Map<String,Object> map : list){
    				Record record = databaseKeyValueConnector.convertRecord(map);
    				recordList.add(record);
    			}
    			detailProcessService.updateKvRecordBusinessType(recordList, typeId, typeName);
    		}
		}
    	
    	
    	
    	
    	
    	//若id不为空  说明是修改一条记录，将id一起保存，若为空 是新建记录，不需要保存id
    	
    	if(id != null){
    		businessDetailEntity.setId(Long.parseLong(id));
    	};
    	String tenantId = tenantHolder.getTenantId();
    	
    	businessDetailEntity.setTenantId(tenantId);
    	businessDetailEntity.setBusiDetail(request.getParameter("businessDetail"));
    	businessDetailEntity.setStandFirst(request.getParameter("standFirst"));
    	businessDetailEntity.setStandSecond(request.getParameter("standSecond"));
    	businessDetailEntity.setBusinessType(request.getParameter("businessTypeName"));
    	String strType="0";
    	if(!com.mossle.core.util.StringUtils.isBlank(request.getParameter("businessType")))
    		strType=request.getParameter("businessType");
    	businessDetailEntity.setTypeId(Long.parseLong(strType));
    	businessDetailEntity.setBpmProcessId(request.getParameter("bpmPName1"));
    	businessDetailEntity.setFormName(request.getParameter("formNames"));
    	businessDetailEntity.setFormid(request.getParameter("formid"));
    	businessDetailEntity.setLevel(request.getParameter("level"));
    	//ckx 2019/1/28
    	businessDetailEntity.setTitle(request.getParameter("title"));
    	businessDetailEntity.setEnable("是");
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	String str = sdf.format(new Date());
    	//新建记录  存入创建时间，修改时间与创建时间相同
    	if(id==null){
	        businessDetailEntity.setCreateTime(str);
	        businessDetailEntity.setModifyTime(str);
        }
    	
    	//修改记录  取之前的创建时间再存回去
    	if(id!=null){  
    		businessDetailEntity.setCreateTime(request.getParameter("createTime"));
    		businessDetailEntity.setModifyTime(str);
    	}
        businessDetailManager.save(businessDetailEntity);
        
        //根据该条明细 到 oa_ba_business_detail 表 找对应的明细ID
        Long detailID = businessDetailEntity.getId();
        //修改  明细与岗位的对应关系，都是先删除再创建
       List<DetailPostEntity> d1 = null;
       	if  (id!=null ){  
       		String s = "from DetailPostEntity where detailID=?";

       		List<DetailPostEntity> d = detailPostManager.find(s, Long.parseLong(id));

       		d1=detailPostManager.find(s, Long.parseLong(id)); 
       		
       		detailPostManager.removeAll(d);
       	}
       	//解析岗位，是用逗号隔开的
        String postName = request.getParameter("postName");
        String postId = request.getParameter("postId");
        if(!com.mossle.core.util.StringUtils.isBlank(postId)){
        	String[] postNames = null;  
            String[] postIds = null;  
            postNames = postName.split(","); 
            postIds = postId.split(",");
            
            //遍历所有旧岗位,查看是否有删除某个原来配置的岗位
            if(d1!=null){
            	for(int t=0;t<d1.size();t++){
            		String oldPostIDString = d1.get(t).getPostID().toString();
            		String flagString = "1";//标识
            		
            		for(int i=0;i<postNames.length;i++){
            			if(postIds[i].equals(oldPostIDString)){
            				flagString = "0"; //若原岗位在新的里面也有，更新标识，表示未删除
            				break;
                		}
            		}
            		if (flagString.equals("1")) {
            			busiDetailModifyContent = busiDetailModifyContent + "  删除岗位："+d1.get(t).getPostName().toString();
    				}
            	}
            }
            
          if(postNames!=null){
	        	//遍历所有新岗位,查看是否有新添加的岗位配置，存入日志中
	              for( int i=0;i<postNames.length;i++ ){
	          		String newPostIDString = postIds[i];
	          		String flagString = "1";//标识
	          		if(d1!=null){
	          			for(int t=0;t<d1.size();t++){
		          			if(d1.get(t).getPostID().toString().equals(newPostIDString)){
		          				flagString = "0"; //若原岗位在新的里面也有，更新标识，表示未删除
		          				break;
		              		}
		          		}
		          		if (flagString.equals("1")) {
		          			busiDetailModifyContent = busiDetailModifyContent + "  新配置岗位："+postNames[i];
		  				}
	          		}
	          	}
	              
	              logger.info( busiDetailModifyContent);
	              
	              for(int i=0;i<postNames.length;i++){
	              	
	                 	//存入岗位和明细的对应关系
	                 	DetailPostEntity detailPostEntity = new DetailPostEntity();
	                 	detailPostEntity.setdetailID(detailID);//细分ID
	                 	detailPostEntity.setPostID(Long.parseLong(postIds[i]));//岗位ID
	                 	detailPostEntity.setPostName(postNames[i]);//岗位名称
	                 	detailPostEntity.setTenantId(tenantId);//租户ID
	                 	detailPostManager.save(detailPostEntity);
	              }
	          }
          }
        return "redirect:/dict/dict-business-detail-list.do";
    }

    @RequestMapping("dict-business-detail-remove")
    public String remove(@RequestParam("selectedItem") List<Long> selectedItem,
            RedirectAttributes redirectAttributes) {
        List<BusinessDetailEntity> dictTypes = businessDetailManager.findByIds(selectedItem);

        businessDetailManager.removeAll(dictTypes);

        messageHelper.addFlashMessage(redirectAttributes,
                "core.success.delete", "删除成功");

        return "redirect:/dict/dict-detail-list.do";
    }

    @RequestMapping("dict-business-detail-export")
    public void export(@ModelAttribute Page page,
            @RequestParam Map<String, Object> parameterMap,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        String tenantId = tenantHolder.getTenantId();
        List<PropertyFilter> propertyFilters = PropertyFilter
                .buildFromMap(parameterMap);
        propertyFilters.add(new PropertyFilter("EQS_tenantId", tenantId));
        page = businessDetailManager.pagedQuery(page, propertyFilters);

        List<BusinessDetailEntity> dictTypes = (List<BusinessDetailEntity>) page.getResult();

        TableModel tableModel = new TableModel();
        tableModel.setName("dict info");
        tableModel.addHeaders("id", "name", "stringValue", "description");
        tableModel.setData(dictTypes);
        exportor.export(request, response, tableModel);
    }

    // ~ ======================================================================
    @RequestMapping(value="isBranches",method=RequestMethod.POST)
    @ResponseBody
    public List<String> isBranchs(@RequestParam(value="businessDetailID")String businessDetailID,@RequestParam(value="businessKey")String businessKey){
    	List<String> list = new ArrayList<>();
    	String sql = "select * from oa_ba_process_condition c where conditionType='audit-setting' and c.busDetailId="+businessDetailID;
    	List<Map<String, Object>> queryForList = jdbcTemplate.queryForList(sql);
    	if(queryForList.size() == 0){//不是多分支流程
    		list.add("0");
    		list.add("no branchs");
    	}else{
    		list.add("1");
    		String sqlUserId = "select user_id from kv_record where id="+businessKey;
    		Map<String, Object> queryForMap = jdbcTemplate.queryForMap(sqlUserId);
    		if(queryForMap != null){
    			String userId = queryForMap.get("user_id").toString();
    			list.add(userId);
    		}else{
    			list.add("");
    			logger.error("未在kv_record中查询到该流程实例数据");
    		}
    		for(Map map : queryForList){
    			String conditionName = map.get("conditionName").toString();
    			if(!"money".equals(conditionName)){
    				continue;
    			}else{
    				String sqlBybusinessKey = "select * from kv_record_condition where conditionName='money' and businessKey="+businessKey;
    				List<Map<String, Object>> money = jdbcTemplate.queryForList(sqlBybusinessKey);
    				String moneyValue = "";
    				for(Map m : money){
    					moneyValue = (String) money.get(0).get("conditionValue");
    				}
    				list.add(moneyValue);
    			}
    		}
    	}
    	return list;
    }
    /**
     * @return
     * 通过业务细分id获取流程岗位信息(申请页)
     */
    @RequestMapping(value="getProcessPostInfoByBusinessDetailId",method=RequestMethod.POST)
    @ResponseBody
    public GetWhole getProcessPostInfoByBusinessDetailId(@RequestParam(value="businessDetailID")long businessDetailID,@RequestParam(value="conditionValue",required=false) String conditionValue){
    	GetWhole getWhole = new GetWhole();
    	BusinessDetailEntity businessDetailEntity = businessDetailManager.findUniqueBy("id", businessDetailID);
    	if(businessDetailEntity != null){
    		String bpmProcessId = businessDetailEntity.getBpmProcessId();
			String result = detailProcessService.getResult(bpmProcessId,conditionValue);
			getWhole.setCode("200");
        	getWhole.setBpmProcessId(bpmProcessId);
        	getWhole.setWhole(result);
    	}
    	return getWhole;
    }
    /**
     * @param processInstanceId
     * @return 审批页和调整页取流程岗位信息
     */
    @RequestMapping(value="getProcessPostInfoByProcessInstanceId",method=RequestMethod.POST)
    @ResponseBody
    public GetWhole getProcessPostInfoByProcessInstanceId(String processInstanceId){
    	Record record = keyValueConnector.findByRef(processInstanceId);
    	//ckx 获取费用支出细分的金额 2018/11/27
    	String businessKey = record.getBusinessKey();
    	String strSql=String.format("select * from kv_record_condition where businessKey=%s and conditionName='money'", businessKey);
		List<Map<String,Object>> mapBoolenList=jdbcTemplate.queryForList(strSql);
		String conditionValue = "";
		if(null != mapBoolenList && mapBoolenList.size() > 0){
			Map<String, Object> map = mapBoolenList.get(0);
			conditionValue = StringUtil.toString(map.get("conditionValue"));
		}
    	String businessDetailId = record.getBusinessDetailId();
    	return getProcessPostInfoByBusinessDetailId(Long.parseLong(businessDetailId),conditionValue);
    }
    /**
     * @param processInstanceId
     * @return
     * 自定义流程，通过流程实例查找该申请的审核人步骤详情
     */
    @RequestMapping(value="getProcessPostInfoByProcessInstanceIdCustom",method=RequestMethod.POST)
    @ResponseBody
    public GetWhole getProcessPostInfoByProcessInstanceIdCustom(String processInstanceId){
    	GetWhole getWhole = new GetWhole();
    	if(StringUtils.isEmpty(processInstanceId)){
    		getWhole.setCode("500");
    		getWhole.setWhole("该流程未正确获取到流程实例，请联系管理员！");
    		return getWhole;
    	}
    	StringBuffer sb = new StringBuffer();
    	Record record = keyValueConnector.findByRef(processInstanceId);
    	String businessKey = record.getBusinessKey();
    	String sql = "select * from custom_approver where business_key="+businessKey+" order by approveStep asc";
    	List<Map<String,Object>> customApprovers = jdbcTemplate.queryForList(sql);
    	//List<CustomApprover> customApprovers = customApproverManager.findBy("businessKey", businessKey);
    	for(Map<String,Object> customApprover :customApprovers){
    		sb.append(customApprover.get("approverId").toString()+",");
    	}
    	String result = detailProcessService.getResultCustom(sb.toString().substring(0,sb.length()-1));
    	getWhole.setCode("200");
    	getWhole.setWhole(result.substring(0, result.length()-2));
    	return getWhole;
    	
    }
   
    /**
     * 获取多分支流程审核步骤(申请页 审批页 调整页)
     * @author sjx 18.12.7
     */
    @RequestMapping(value="getBranchProcessStep",method=RequestMethod.POST)
    @ResponseBody
    public List<BranchApprovalLinkEntity> getBranchProcessStep(@RequestParam(value="businessDetailID")String businessDetailID,@RequestParam(value="userId")String userId,
    								@RequestParam(value="isMoney")String isMoney,@RequestParam(value="money",required=false)String money){
    	StringBuffer buffer = new StringBuffer();
    	//通过细分id获取该细分的多分支条件
    	String sql = "SELECT * FROM oa_ba_process_condition WHERE conditionType='audit-setting' and busDetailId="+businessDetailID;
    	List<Map<String, Object>> queryForList = jdbcTemplate.queryForList(sql);
    	boolean area = false;
    	boolean position = false;
    	for(Map<String, Object> map : queryForList){
    		if("area".equals(String.valueOf(map.get("conditionName")))){
    			area = true;
    		}
    		if("position".equals(String.valueOf(map.get("conditionName")))){
    			position = true;
    		}
    	}
    	//根据userId获取申请人的岗位职级和是否是大区人员
    	if(area){
    		PartyDTO findAreaById = partyConnector.findAreaById(userId);
        	if(findAreaById == null){//不是大区的人
        		buffer.append("area==0");
        	}else{
        		buffer.append("area==1");
        	}
    	}
    	if(position){
    		UserDTO findById = userConnector.findById(userId);
    		if(area == true){
    			buffer.append("&&");
    		}
        	if(3 <= Integer.parseInt(findById.getPositionCode())){
        		buffer.append("position>=3");
        	}else{
        		buffer.append("position<3");
        	}
    	}
    	if("1".equals(isMoney)){//流程涉及金额
    		if(area == true || position == true){
    			buffer.append("&&");
    		}
    		try {
    			if(3000 <= Double.parseDouble(money)){
        			buffer.append("money>=3000");
        		}else{
        			buffer.append("money<3000");
        		}
			} catch (Exception e) {
				List<BranchApprovalLinkEntity> fail = new ArrayList<>();
				BranchApprovalLinkEntity failEntity = new BranchApprovalLinkEntity();
				failEntity.setNote("请检查金额的输入是否正确！");
				fail.add(failEntity);
				return fail;
			}
    		
    	}
    	String hql = "from BranchApprovalLinkEntity where conditionType ='"+buffer+"' and businessDetailId=?";
    	List<BranchApprovalLinkEntity> find = branchApprovalLinkEntityManager.find(hql, businessDetailID);
    	return find;
    }
    
    @RequestMapping("dict-business-detail-set-contidion")
    public String settingContidion(@ModelAttribute Page page,
    		@RequestParam("id") Long id, Model model) {
    	BusinessDetailEntity businessDetailEntity=businessDetailManager.get(id);
    	model.addAttribute("business", businessDetailEntity);
    	
    	//是否属于公共流程
    	String isCommonProcess="0";
    	BpmProcess bpmProcess=bpmProcessManager.get(com.mossle.core.util.StringUtils.isBlank(businessDetailEntity.getBpmProcessId())?0L:Long.valueOf(businessDetailEntity.getBpmProcessId()));
    	if(bpmProcess.getBpmCategory()!=null&&bpmProcess.getBpmCategory().getId()==3){
    		isCommonProcess="1";
    	}
    	model.addAttribute("isCommonProcess", isCommonProcess);
    	
    	List<DictInfo> dictList=dictConnectorImpl.findDictInfoListByType("multiBranchCondition");
    	List<String> strConditionList=jdbcTemplate.queryForList("select conditionName from oa_ba_process_condition where conditionType='audit-setting' and busDetailId=?", String.class,id);
    	List<Map<String,Object>> mapDictList=new ArrayList<Map<String,Object>>();
    	if(dictList!=null){
    		for (DictInfo dictInfo : dictList) {
    			Map<String,Object> mapDict=new HashMap<String, Object>();
    			mapDict.put("id", dictInfo.getId());
    			String strName=dictInfo.getName();
    			String strValue=dictInfo.getValue();
    			String strType="0";
    			String strNewValue="";
    			String strChecked="";
    			if(strValue.contains("-")){
    				String[] strValueList=strValue.split("\\-");
    				strType=strValueList[0];
    				strNewValue=strValueList[1];
    			}
    			if(strConditionList.size()>0&&!strNewValue.equals("")&&strConditionList.contains(strNewValue)){
    				strChecked="checked";
    			}
    			mapDict.put("type",strType);
    			mapDict.put("name",strName);
    			mapDict.put("value",strNewValue);
    			mapDict.put("checked",strChecked);
    			mapDictList.add(mapDict);
			}
    	}
    	
    	List<String> strCommonCompanyList=jdbcTemplate.queryForList("select note from oa_ba_process_condition "
    			+ "where conditionType='common-setting' and conditionName='company-value' and busDetailId=?", String.class,id);
    	List<Map<String,Object>> mapCompanyList=new ArrayList<Map<String,Object>>();
    	List<PartyEntity> companyPartyEntityList=partyEntityManager.find("from PartyEntity where partyType.id=2 and delFlag='0'");
    	if(companyPartyEntityList!=null&&companyPartyEntityList.size()>0){
    		for(PartyEntity party:companyPartyEntityList){
    			String strChecked="";
    			Map<String,Object> mapCompany=new HashMap<String, Object>();
    			mapCompany.put("type","0");
    			mapCompany.put("name",party.getName());
    			mapCompany.put("value",party.getId());
    			if(strCommonCompanyList.size()>0&&strCommonCompanyList.contains(party.getId().toString())){
    				strChecked="checked";
    			}
    			mapCompany.put("checked",strChecked);
    			mapCompanyList.add(mapCompany);
    		}
    	}
    	model.addAttribute("companylist", mapCompanyList);
    	
    	//是否显示
    	List<String> strCommonShowList=jdbcTemplate.queryForList("select conditionName from oa_ba_process_condition "
    			+ "where conditionType='common-setting' and conditionName in ('is-show','person-probation') and busDetailId=?", String.class,id);
    	String commonChecked="";
    	if(strCommonShowList.size()>0&&strCommonShowList.contains("is-show")){
    		commonChecked="checked";
		}
    	model.addAttribute("commonChecked", commonChecked);
    	
    	//是否离职人员
    	String commonStartChecked="";
    	if(strCommonShowList.size()>0&&strCommonShowList.contains("person-probation")){
    		commonStartChecked="checked";
		}
    	model.addAttribute("commonStartChecked", commonStartChecked);
    	
        model.addAttribute("contidionlist", mapDictList);
        return "dict/dict-business-detail-set-contidion";
    }
    
    @RequestMapping("dict-business-detail-contidion-save")
    public String settingContidion(@ModelAttribute Page page,
    		@RequestParam("detailId") Long detailId,
    		@RequestParam(value="chkContidion",required=false) String chkContidion,
    		@RequestParam(value="conditionType",required=false) String conditionType,
    		@RequestParam(value="dataType",required=false) String dataType,
    		@RequestParam(value="note",required=false) String note,
    		Model model) throws UnsupportedEncodingException {
    	String strInsertSql="insert into oa_ba_process_condition(id,conditionType,busDetailId,conditionName,isNeededData,note) values(%s,'%s',%s,'%s','%s','%s')";
    	keyValueConnector.updateBySql(String.format("delete from oa_ba_process_condition where busDetailId=%s", detailId));
    	if(!com.mossle.core.util.StringUtils.isBlank(chkContidion)){
    		String[] strContidionList=chkContidion.split(",");
    		String[] dataTypeList=dataType.split(",");
    		String[] NoteList=note.split(",",-1);
    		String[] conditionTypeList=conditionType.split(",",-1);
    		int i=0;
    		for (String strContidion : strContidionList) {
    			keyValueConnector.updateBySql(String.format(strInsertSql
						,idGenerator.generateId()
						,conditionTypeList[i]
						,detailId
						,strContidion
						,dataTypeList[i]
						,NoteList[i]));
				i++;
			}
    	}
    	
    	return "redirect:/user/close-popwin-dialog.do?msgTip="+java.net.URLEncoder.encode(java.net.URLEncoder.encode("修改成功","utf-8"),"utf-8");
    }
    /**
     * @param id
     * @param model
     * @return
     * @author sjx 18.12.5
     * 单个细分配置情况查询显示
     */
    @RequestMapping("dict-business-detail-linkSetting")
    public String linkSetting(@RequestParam(value="id") Long id, Model model){
    	StringBuffer condition = new StringBuffer();
    	String sql = "SELECT conditionName FROM oa_ba_process_condition WHERE conditionType='audit-setting' and busDetailId="+id;
    	List<Map<String, Object>> conditionList = jdbcTemplate.queryForList(sql);
    	for(Map<String, Object> map : conditionList){
    		condition.append(String.valueOf(map.get("conditionName")));
    		condition.append("  ");
    	}
    	model.addAttribute("condition", condition);
    	BusinessDetailEntity businessDetailEntity=businessDetailManager.get(id);
    	model.addAttribute("business", businessDetailEntity);
    	List<BranchApprovalLinkEntity> branchApprovalLinkEntitys = branchApprovalLinkEntityManager.findBy("businessDetailId", String.valueOf(id));
    	model.addAttribute("branchApprovalLinkEntitys", branchApprovalLinkEntitys);
    	return "dict/dict-business-detail-set-link";
    }
    /**
     * @param queryCondition
     * @param approvalLink
     * @param isBranch
     * @author sjx 18.12.5
     * 保存分支审批环节设置
     */
    @RequestMapping("dict-business-detail-link-save")
    public String saveLinkSetting(BranchApprovalLinkEntity entity,int linkResult,RedirectAttributes redirectAttributes){
    	String detailId = entity.getBusinessDetailId();
    	detailProcessService.saveBranchSetting(entity,linkResult,detailId);
    	
    	messageHelper.addFlashMessage(redirectAttributes,"保存成功");
    	return "redirect:/dict/dict-business-detail-list.do";
    }
    /**
     * 通过细分id去查询流程的所有人工节点，供配置人员选择
     * @param businessDetailId
     * @author sjx
     */
    @RequestMapping("dict-business-detail-node-choose")
    @ResponseBody
    public String queryNode(long businessDetailId,Model model){
		BusinessDetailEntity businessDetailEntity = businessDetailManager.findUniqueBy("id", businessDetailId);
		List<BpmConfNode> userTaskNodes = null;
		List<UserNode> nodeResult = new ArrayList<>();
		if(businessDetailEntity != null){
			String bpmProcessId = businessDetailEntity.getBpmProcessId();
			userTaskNodes = detailProcessService.userTaskNode(bpmProcessId);
		}
		for(BpmConfNode node : userTaskNodes){
			UserNode userNode = new UserNode();
			long nodeId = node.getId();
			String postId = detailProcessService.postByNodeId(nodeId);
			if(postId.contains("常用语:流程发起人")){
				continue;
			}else {
				if(postId.contains("常用语:")){
					userNode.setId(node.getId());
					userNode.setName(node.getName());
					userNode.setPriority(node.getPriority());
					nodeResult.add(userNode);
					continue;
				}
				StringBuffer str = new StringBuffer();
				if(postId.contains("岗位:")){
					postId = postId.substring(3);//去掉该值中的岗位：
        		}
				String postName = "from PartyEntity where id=?";
        		PartyEntity e = partyEntityManager.findUnique(postName, Long.parseLong(postId));
        		str.insert(0, e.getName());
        		for(int i = 0;i < 2;i++){
        			String typeId = "3,4";
        			if(i == 1){
        				typeId = "2,3";
        			}
        			String org = "select e.* from party_entity e join party_struct s on e.id=s.PARENT_ENTITY_ID where s.CHILD_ENTITY_ID=? and e.type_id in("+typeId+")";
        			Map<String,Object> orgMap = jdbcTemplate.queryForMap(org, postId);
        			
        			str.insert(0, orgMap.get("name").toString());
        			postId = orgMap.get("id").toString();
        		}
        		userNode.setId(node.getId());
        		userNode.setName(str.toString());
        		userNode.setPriority(node.getPriority());
			}
			nodeResult.add(userNode);
		}
		String text = JSONObject.toJSONString(nodeResult);
		return text;
    }
    
    /**
     * 修改大修设置
     * @param page
     * @param id
     * @param model
     * @return
     * ckx
     */
    @RequestMapping("dict-business-detail-set-contidion-area")
    public String settingContidionArea(@ModelAttribute Page page,
    		@RequestParam("id") Long id, Model model) {
    	BusinessDetailEntity businessDetailEntity=businessDetailManager.get(id);
    	model.addAttribute("business", businessDetailEntity);
    	String commonChecked = "0";
    	String countSql = "select count(*) from oa_ba_process_condition where busDetailId = ? and conditionType = 'common-setting-area' and conditionName = 'common-setting-area'";
    	int count = jdbcTemplate.queryForObject(countSql, Integer.class, businessDetailEntity.getId());
    	if(count > 0){
    		commonChecked = "1";
    	}
    	
    	model.addAttribute("commonChecked", commonChecked);
    	
        return "dict/dict-business-detail-set-contidion-area";
    }
    /**
     * 保存修改大区设置
     * @param page
     * @param detailId
     * @param isOpen
     * @param model
     * @return
     * @throws Exception
     * ckx
     */
    @RequestMapping("dict-business-detail-contidion-area-save")
    public String saveSettingContidionArea(@ModelAttribute Page page,
    		@RequestParam(value="detailId",required=true) Long detailId,
    		@RequestParam(value="isOpen",required=true) String isOpen,
    		Model model) throws Exception {
    	
    	if("0".equals(isOpen)){
    		keyValueConnector.updateBySql(String.format("delete from oa_ba_process_condition where conditionType = 'common-setting-area' and conditionName = 'common-setting-area' and busDetailId=%s", detailId));
    	}else{
    		keyValueConnector.updateBySql(String.format("delete from oa_ba_process_condition where conditionType = 'common-setting-area' and conditionName = 'common-setting-area' and busDetailId=%s", detailId));
        	String strInsertSql="insert into oa_ba_process_condition(id,conditionType,busDetailId,conditionName) values(%s,'%s',%s,'%s')";
        	keyValueConnector.updateBySql(String.format(strInsertSql
					,idGenerator.generateId()
					,"common-setting-area"
					,detailId
					,"common-setting-area"
					));
    	}
 
    	
    	return "redirect:/user/close-popwin-dialog.do?msgTip="+java.net.URLEncoder.encode(java.net.URLEncoder.encode("修改成功","utf-8"),"utf-8");
    }
    
    
    
    
    
    
    
    public static class UserNode{
    	private long id;
    	private String name;
    	private int priority;
		public long getId() {
			return id;
		}
		public void setId(long id) {
			this.id = id;
		}
		
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public int getPriority() {
			return priority;
		}
		public void setPriority(int priority) {
			this.priority = priority;
		}
    	
    }
    @Resource
    public void setExportor(Exportor exportor) {
        this.exportor = exportor;
    }
    
    @Resource
    public void setMessageHelper(MessageHelper messageHelper) {
        this.messageHelper = messageHelper;
    }

    @Resource
    public void setTenantHolder(TenantHolder tenantHolder) {
        this.tenantHolder = tenantHolder;
    }
    
    @Resource
    public void setBusinessDetailManager(BusinessDetailManager businessDetailManager) {
        this.businessDetailManager = businessDetailManager;
    }
    
    @Resource
    public void setPartyEntityManager(PartyEntityManager partyEntityManager) {
        this.partyEntityManager = partyEntityManager;
    }
    
    @Resource
    public void setDetailPostManager(DetailPostManager detailPostManager) {
        this.detailPostManager = detailPostManager;
    }
    @Resource
    public void setDetailProcessService(DetailProcessService detailProcessService) {
    	this.detailProcessService = detailProcessService;
    }
    @Resource
    public void setKeyValueConnector(KeyValueConnector keyValueConnector) {
    	this.keyValueConnector = keyValueConnector;
    }
    @Resource
    public void setOperationConnector(OperationConnector operationConnector) {
    	this.operationConnector = operationConnector;
    }
    @Resource
    public void setBranchApprovalLinkEntityManager(BranchApprovalLinkEntityManager branchApprovalLinkEntityManager) {
    	this.branchApprovalLinkEntityManager = branchApprovalLinkEntityManager;
    }
    @Resource
    public void setPartyConnector(PartyConnector partyConnector) {
    	this.partyConnector = partyConnector;
    }
    @Resource
    public void setUserConnector(UserConnector userConnector) {
    	this.userConnector = userConnector;
    }
    
}
