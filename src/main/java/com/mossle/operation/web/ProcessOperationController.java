package com.mossle.operation.web;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mossle.base.persistence.domain.BusinessDetailEntity;
import com.mossle.base.persistence.manager.BusinessDetailManager;
import com.mossle.base.rs.BusinessResource;
import com.mossle.base.rs.BusinessResource.BusinessTypeDTO;
import com.mossle.bpm.rs.BpmResource;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import com.mossle.api.form.FormConnector;
import com.mossle.api.form.FormDTO;
import com.mossle.api.humantask.HumanTaskConnector;
import com.mossle.api.humantask.HumanTaskDTO;
import com.mossle.api.humantask.HumanTaskDefinition;
import com.mossle.api.keyvalue.FormParameter;
import com.mossle.api.keyvalue.KeyValueConnector;
import com.mossle.api.keyvalue.Prop;
import com.mossle.api.keyvalue.Record;
import com.mossle.api.keyvalue.RecordBuilder;
import com.mossle.api.org.OrgConnector;
import com.mossle.api.process.ProcessConnector;
import com.mossle.api.process.ProcessDTO;
import com.mossle.api.store.StoreConnector;
import com.mossle.api.tenant.TenantHolder;
import com.mossle.api.user.UserConnector;
import com.mossle.api.user.UserDTO;
import com.mossle.bpm.rs.BpmResource;
import com.mossle.bpm.support.ActivityDTO;
import com.mossle.button.ButtonDTO;
import com.mossle.button.ButtonHelper;
import com.mossle.common.utils.DeEnCode;
import com.mossle.core.MultipartHandler;
import com.mossle.core.auth.CurrentUserHolder;
import com.mossle.core.mapper.JsonMapper;
import com.mossle.core.page.Page;
import com.mossle.core.spring.MessageHelper;
import com.mossle.dict.persistence.domain.DictInfo;
import com.mossle.dict.support.DictConnectorImpl;
import com.mossle.operation.persistence.domain.Apply;
import com.mossle.operation.persistence.domain.ApplyDTO;
import com.mossle.operation.persistence.domain.CodeEntity;
import com.mossle.operation.persistence.manager.CodeManager;
import com.mossle.operation.service.OperationService;
import com.mossle.party.persistence.domain.PartyEntity;
import com.mossle.party.persistence.domain.PartyStruct;
import com.mossle.party.persistence.domain.PartyType;
import com.mossle.party.persistence.manager.PartyEntityManager;
import com.mossle.party.persistence.manager.PartyStructManager;
import com.mossle.party.persistence.manager.PartyTypeManager;
import com.mossle.party.rs.OrderNumberResource.PartyEntityDTO;
import com.mossle.xform.Xform;
import com.mossle.xform.XformBuilder;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mossle.dict.support.DictConnectorImpl;
import com.mossle.internal.store.persistence.domain.StoreInfo;
import com.mossle.api.org.OrgConnector;

/**
 * 流程操作.
 * 
 * @author Lingo
 */
@Controller
@RequestMapping("operation")
public class ProcessOperationController {
    private static Logger logger = LoggerFactory
            .getLogger(ProcessOperationController.class);
    public static final int STATUS_DRAFT_PROCESS = 0;
    public static final int STATUS_DRAFT_TASK = 1;
    public static final int STATUS_RUNNING = 2;
    private OperationService operationService;
    private KeyValueConnector keyValueConnector;
    private MessageHelper messageHelper;
    private CurrentUserHolder currentUserHolder;
    private ProcessConnector processConnector;
    private HumanTaskConnector humanTaskConnector;
    private MultipartResolver multipartResolver;
    private StoreConnector storeConnector;
    private ButtonHelper buttonHelper = new ButtonHelper();
    private FormConnector formConnector;
    private JsonMapper jsonMapper = new JsonMapper();
    private TenantHolder tenantHolder;
    private UserConnector userConnector;
    private OrgConnector orgConnector;
    private PartyEntityManager partyEntityManager;
    private PartyStructManager partyStructManager;
    private CodeManager codeManager;
    private PartyTypeManager partyTypeManager;
    private DictConnectorImpl dictConnectorImpl;
    private BpmResource bpmResource;
    private BusinessDetailManager businessDetailManager;
    @Resource
    private BusinessResource businessResource;
    
    /**
     * 保存草稿.
     */
    @RequestMapping("process-operation-saveDraft")
    public String saveDraft(HttpServletRequest request) throws Exception {
        this.doSaveRecord(request);

        return "operation/process-operation-saveDraft";
    }

    /**
     * 列出所有草稿.
     */
    @RequestMapping("process-operation-listDrafts")
    public String listDrafts(@ModelAttribute Page page, Model model)
            throws Exception {
        String userId = currentUserHolder.getUserId();
        String tenantId = tenantHolder.getTenantId();
        page = keyValueConnector.pagedQuery(page, STATUS_DRAFT_PROCESS, userId,tenantId);
        model.addAttribute("page", page);

        return "operation/process-operation-listDrafts";
    }

    /**
     * 删除草稿.
     */
    @RequestMapping("process-operation-removeDraft")
    public String removeDraft(@RequestParam("code") String code) {
        keyValueConnector.removeByCode(code);

        return "redirect:/operation/process-operation-listDrafts.do";
    }

    /**
     * 显示启动流程的表单.
     */
    @RequestMapping("process-operation-viewStartForm")
    public String viewStartForm(
            HttpServletRequest request,
            @RequestParam("bpmProcessId") String bpmProcessId,
            @RequestParam(value = "businessKey", required = false) String businessKey,
            Model model) throws Exception {
    	
    	bpmProcessId = DeEnCode.decode(bpmProcessId);
    	
        String tenantId = tenantHolder.getTenantId();
        String userId=currentUserHolder.getUserId();  
        UserDTO userDto = userConnector.findById(userId);
        String userName = userDto.getDisplayName();
        model.addAttribute("userName", userName);
        FormParameter formParameter = new FormParameter();
        formParameter.setBpmProcessId(bpmProcessId);
        formParameter.setBusinessKey(businessKey);

        ProcessDTO processDto = processConnector.findProcess(bpmProcessId);

        String processDefinitionId = processDto.getProcessDefinitionId();

        FormDTO formDto = this.processConnector.findStartForm(processDefinitionId);
        formParameter.setFormDto(formDto);

        if (StringUtils.isBlank(businessKey)) {
        	businessKey = "";
        }
        
        //生成受理单编号
        //String code = CreateApplyCode(userId);
        if (formDto.isExists()) {
        	
        	// 如果是外部表单，就直接跳转出去
            if (formDto.isRedirect()) {
                //跳转之前先取当前登录人所属大区名称，带回页面，若当前登录人不属于任何大区，就设置一个""
            	String areaName;
            	String areaId = "";
            	PartyEntity partyEntity  =  orgConnector.findPartyAreaByUserId(userId);
            	
            	if ( partyEntity ==  null){
            		areaName = "";
            	}
            	else { areaName = partyEntity.getName();
            			areaId  = Long.toString(partyEntity.getId());
            		}
            	model.addAttribute("areaId", areaId);
            	model.addAttribute("processDefinitionId", formDto.getProcessDefinitionId());
            	model.addAttribute("activityId", formDto.getActivityId());
            	model.addAttribute("bpmProcessId", bpmProcessId);
            	model.addAttribute("businessKey", businessKey);
            	model.addAttribute("userId", userId);
            	model.addAttribute("areaName", areaName);
            	model.addAttribute("url", formDto.getUrl());
            	
            	//得到体系
            	List<DictInfo> dictList=dictConnectorImpl.findDictInfoListByType("OwnedSystem", "1");
            	model.addAttribute("systemlist", dictList);
            	List<BusinessTypeDTO> businessTypeList = businessResource.getTypesByPost(bpmProcessId,userId, formDto.getUrl());
            	model.addAttribute("businessTypeList", businessTypeList);
            	
            	String categoryId="0";
            	if(!com.mossle.core.util.StringUtils.isBlank(request.getParameter("categoryId"))){
            		categoryId=DeEnCode.decode(request.getParameter("categoryId"));
            	}
            	model.addAttribute("categoryId", categoryId);
            	//生成受理单编号
            	String code =  operationService.CreateApplyCode(userId);
               
            	model.addAttribute("code",code);
            	//bpmprocessid获取业务细分id
            	String sql = "from BusinessDetailEntity where  bpmProcessId=? ";
                BusinessDetailEntity businessDetailEntity = businessDetailManager.findUnique(sql, bpmProcessId);
                Long businessDetailId=0L;
                if(businessDetailEntity!=null)
                	businessDetailId = businessDetailEntity.getId();
                model.addAttribute("businessDetailId", businessDetailId);
            	return formDto.getUrl();
            }

            // 如果找到了form，就显示表单
            if (processDto.isConfigTask()) {
                formParameter.setNextStep("taskConf");// 如果需要配置负责人
            } else {
                formParameter.setNextStep("confirmStartProcess");
            }

            return this.doViewStartForm(formParameter, model, tenantId);
        } else if (processDto.isConfigTask()) {
            formParameter.setProcessDefinitionId(processDefinitionId);

            return this.doTaskConf(formParameter, model);// 如果没找到form，就判断是否配置负责人
        } else {
            // 如果也不需要配置任务，就直接进入确认发起流程
            return this.doConfirmStartProcess(formParameter, model);
        }
    }
    
    /**
     * 跳转到外部表单.
     */
    @RequestMapping(value ="process-operation-viewForm", method = RequestMethod.POST)
    public String viewTaskOutForm(String userName,
            Model model,
            @RequestParam(value = "url", required = false) String url,
           RedirectAttributes redirectAttributes)
            throws Exception {
    	System.out.println();
    	//得到体系
    	List<DictInfo> dictList=dictConnectorImpl.findDictInfoListByType("OwnedSystem", "1");
    	model.addAttribute("systemlist", dictList);
    	String userId = currentUserHolder.getUserId();
    	List<BusinessTypeDTO> businessTypeList = businessResource.getTypesByPost("",userId, url);
    	model.addAttribute("businessTypeList", businessTypeList);
    	//生成受理单编号
    	String code =  operationService.CreateApplyCode(userId);
       
    	model.addAttribute("userName",userName);
    	model.addAttribute("code",code);
        return url;
       //operation/cancel-order-modify
        
    }

    /**
     * 配置每个任务的参与人.
     * 
     * 如果是执行taskConf，可能是填写表单，也可能是直接进入taskConf。
     */
    @RequestMapping("process-operation-taskConf")
    public String taskConf(HttpServletRequest request, Model model)
            throws Exception {
        FormParameter formParameter = this.doSaveRecord(request);

        ProcessDTO processDto = processConnector.findProcess(formParameter
                .getBpmProcessId());
        String processDefinitionId = processDto.getProcessDefinitionId();
        formParameter.setProcessDefinitionId(processDefinitionId);

        if (processDto.isConfigTask()) {
            // 如果需要配置负责人
            formParameter.setNextStep("confirmStartProcess");

            return this.doTaskConf(formParameter, model);
        } else {
            // 如果不需要配置负责人，就进入确认发起流程的页面
            return this.doConfirmStartProcess(formParameter, model);
        }
    }
  
    /**
     * 确认发起流程.
     */
    @RequestMapping("process-operation-confirmStartProcess")
    public String confirmStartProcess(HttpServletRequest request, Model model)
            throws Exception {
        FormParameter formParameter = this.doSaveRecord(request);
        formParameter.setNextStep("startProcessInstance");
        this.doConfirmStartProcess(formParameter, model);

        return "operation/process-operation-confirmStartProcess";
    }

    /**
     * 发起流程.
     */
    @RequestMapping("process-operation-startProcessInstance")
    public String startProcessInstance(HttpServletRequest request, Model model)
            throws Exception {
        FormParameter formParameter = this.doSaveRecord(request);
        this.doConfirmStartProcess(formParameter, model);

        Record record = keyValueConnector.findByCode(formParameter
                .getBusinessKey());
        ProcessDTO processDto = processConnector.findProcess(formParameter
                .getBpmProcessId());
        String processDefinitionId = processDto.getProcessDefinitionId();

        // 获得form的信息
        FormDTO formDto = processConnector.findStartForm(processDefinitionId);

        Xform xform = new XformBuilder().setStoreConnector(storeConnector)
                .setUserConnector(userConnector)
                .setContent(formDto.getContent()).setRecord(record).build();
        Map<String, Object> processParameters = xform.getMapData();
        logger.info("processParameters : {}", processParameters);

        // String processInstanceId = processConnector.startProcess(
        // currentUserHolder.getUserId(), formParameter.getBusinessKey(),
        // processDefinitionId, processParameters);
        // record = new RecordBuilder().build(record, STATUS_RUNNING,
        // processInstanceId);
        // keyValueConnector.save(record);
        String userId = currentUserHolder.getUserId();
        String businessKey = formParameter.getBusinessKey();
        
        //为了发起流程，通过细分找到岗位，将岗位存入task_info的辅助表中 add by lilei at 2018.09.05
        /*String sql = "from BusinessDetailEntity where  bpmProcessId=? ";
        BusinessDetailEntity businessDetailEntity = businessDetailManager.findUnique(sql, formParameter.getBpmProcessId());
        processParameters.put("businessDetailId",businessDetailEntity.getId());*/
        //得到岗位，存储add by lilei at 2018-09-12
        processParameters.put("positionId", request.getParameter("iptStartPositionId"));
        this.operationService.startProcessInstance(userId, businessKey,
                processDefinitionId, processParameters, record);

        return "operation/process-operation-startProcessInstance";
    }

    // ~ ======================================================================
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

    /**
     * 实际确认发起流程.
     */
    public String doConfirmStartProcess(FormParameter formParameter, Model model) {
        humanTaskConnector.configTaskDefinitions(
                formParameter.getBusinessKey(),
                formParameter.getList("taskDefinitionKeys"),
                formParameter.getList("taskAssignees"));

        model.addAttribute("businessKey", formParameter.getBusinessKey());
        model.addAttribute("nextStep", formParameter.getNextStep());
        model.addAttribute("bpmProcessId", formParameter.getBpmProcessId());

        return "operation/process-operation-confirmStartProcess";
    }

    /**
     * 实际显示开始表单.
     */
    public String doViewStartForm(FormParameter formParameter, Model model,
            String tenantId) throws Exception {
        model.addAttribute("formDto", formParameter.getFormDto());
        model.addAttribute("bpmProcessId", formParameter.getBpmProcessId());
        model.addAttribute("businessKey", formParameter.getBusinessKey());
        model.addAttribute("nextStep", formParameter.getNextStep());

        List<ButtonDTO> buttons = new ArrayList<ButtonDTO>();
        buttons.add(buttonHelper.findButton("saveDraft"));
        buttons.add(buttonHelper.findButton(formParameter.getNextStep()));
        model.addAttribute("buttons", buttons);

        model.addAttribute("formDto", formParameter.getFormDto());

        String json = this.findStartFormData(formParameter.getBusinessKey());

        if (json != null) {
            model.addAttribute("json", json);
        }

        Record record = keyValueConnector.findByCode(formParameter
                .getBusinessKey());
        FormDTO formDto = formConnector.findForm(formParameter.getFormDto()
                .getCode(), tenantId);

        if (record != null) {
            Xform xform = new XformBuilder().setStoreConnector(storeConnector)
                    .setUserConnector(userConnector)
                    .setContent(formDto.getContent()).setRecord(record).build();
            model.addAttribute("xform", xform);
        } else {
            Xform xform = new XformBuilder().setStoreConnector(storeConnector)
                    .setUserConnector(userConnector)
                    .setContent(formDto.getContent()).build();
            model.addAttribute("xform", xform);
        }

        return "operation/process-operation-viewStartForm";
    }

    /**
     * 实际展示配置任务的配置.
     */
    public String doTaskConf(FormParameter formParameter, Model model) {
        model.addAttribute("bpmProcessId", formParameter.getBpmProcessId());

        model.addAttribute("businessKey", formParameter.getBusinessKey());
        model.addAttribute("nextStep", formParameter.getNextStep());

        List<HumanTaskDefinition> humanTaskDefinitions = humanTaskConnector
                .findHumanTaskDefinitions(formParameter
                        .getProcessDefinitionId());
        model.addAttribute("humanTaskDefinitions", humanTaskDefinitions);

        return "operation/process-operation-taskConf";
    }

    /**
     * 读取草稿箱中的表单数据，转换成json.
     */
    public String findStartFormData(String businessKey) throws Exception {
        Record record = keyValueConnector.findByCode(businessKey);

        if (record == null) {
            return null;
        }

        Map map = new HashMap();

        for (Prop prop : record.getProps().values()) {
            map.put(prop.getCode(), prop.getValue());
        }

        String json = jsonMapper.toJson(map);

        return json;
    }

    // ~ ======================================================================
    @Resource
    public void setKeyValueConnector(KeyValueConnector keyValueConnector) {
        this.keyValueConnector = keyValueConnector;
    }

    @Resource
    public void setMessageHelper(MessageHelper messageHelper) {
        this.messageHelper = messageHelper;
    }

    @Resource
    public void setCurrentUserHolder(CurrentUserHolder currentUserHolder) {
        this.currentUserHolder = currentUserHolder;
    }

    @Resource
    public void setOperationService(OperationService operationService) {
        this.operationService = operationService;
    }

    @Resource
    public void setProcessConnector(ProcessConnector processConnector) {
        this.processConnector = processConnector;
    }

    @Resource
    public void setHumanTaskConnector(HumanTaskConnector humanTaskConnector) {
        this.humanTaskConnector = humanTaskConnector;
    }

    @Resource
    public void setMultipartResolver(MultipartResolver multipartResolver) {
        this.multipartResolver = multipartResolver;
    }

    @Resource
    public void setStoreConnector(StoreConnector storeConnector) {
        this.storeConnector = storeConnector;
    }

    @Resource
    public void setFormConnector(FormConnector formConnector) {
        this.formConnector = formConnector;
    }

    @Resource
    public void setTenantHolder(TenantHolder tenantHolder) {
        this.tenantHolder = tenantHolder;
    }

    @Resource
    public void setUserConnector(UserConnector userConnector) {
        this.userConnector = userConnector;
    }
    
    @Resource
	public void setOrgConnector(OrgConnector orgConnector) {
		this.orgConnector = orgConnector;
	}
    @Resource
    public void setPartyEntityManager(PartyEntityManager partyEntityManager) {
        this.partyEntityManager = partyEntityManager;
    }

    @Resource
    public void setPartyStructManager(PartyStructManager partyStructManager) {
        this.partyStructManager = partyStructManager;
    }
    
    @Resource
    public void setCodeManager(CodeManager codeManager) {
        this.codeManager = codeManager;
    }
    
    @Resource
    public void setPartyTypeManager(PartyTypeManager partyTypeManager) {
        this.partyTypeManager = partyTypeManager;
    }
    @Resource
    public void setDictConnectorImpl(DictConnectorImpl dictConnectorImpl){
    	this.dictConnectorImpl=dictConnectorImpl;
    }
    @Resource
	public void setBpmResource(BpmResource bpmResource) {
		this.bpmResource = bpmResource;
	}

	@Resource
	public void setBusinessDetailManager(BusinessDetailManager businessDetailManager) {
		this.businessDetailManager = businessDetailManager;
	}
}
