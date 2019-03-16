package com.mossle.operation.web;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

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
import com.mossle.api.process.ProcessConnector;
import com.mossle.api.process.ProcessDTO;
import com.mossle.api.store.StoreConnector;
import com.mossle.api.store.StoreDTO;
import com.mossle.api.tenant.TenantHolder;
import com.mossle.api.user.UserConnector;
import com.mossle.button.ButtonDTO;
import com.mossle.button.ButtonHelper;
import com.mossle.common.utils.FileUploadAPI;
import com.mossle.common.utils.WebAPI;
import com.mossle.core.MultipartHandler;
import com.mossle.core.annotation.Log;
import com.mossle.core.auth.CurrentUserHolder;
import com.mossle.core.auth.CustomPasswordEncoder;
import com.mossle.core.mapper.JsonMapper;
import com.mossle.core.spring.MessageHelper;
import com.mossle.core.store.MultipartFileDataSource;
import com.mossle.disk.service.DiskService;
import com.mossle.disk.util.FileUtils;
import com.mossle.internal.store.persistence.domain.StoreInfo;
import com.mossle.operation.persistence.domain.TestEntity;
import com.mossle.operation.persistence.domain.TestEntityDTO;
import com.mossle.operation.persistence.domain.Apply;
import com.mossle.operation.persistence.domain.ApplyDTO;
import com.mossle.operation.persistence.domain.Invoice;
import com.mossle.operation.persistence.domain.InvoiceDTO;
import com.mossle.operation.persistence.domain.Return;
import com.mossle.operation.persistence.domain.ReturnDTO;
import com.mossle.operation.persistence.manager.TestEntityManager;
import com.mossle.operation.persistence.manager.InvoiceManager;
import com.mossle.operation.service.InvoiceService;
import com.mossle.operation.service.OperationService;
import com.mossle.user.persistence.domain.AccountCredential;
import com.mossle.user.persistence.domain.AccountInfo;
import com.mossle.user.persistence.manager.AccountCredentialManager;
import com.mossle.user.persistence.manager.AccountInfoManager;
import com.mossle.user.support.ChangePasswordResult;
import com.mossle.xform.Xform;
import com.mossle.xform.XformBuilder;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 流程操作.审批流程发票申请单
 */
@Component
@Controller
@RequestMapping("Invoice")
@Path("Invoice")
public class ProcessOperationInvoiceController {

	private static Logger logger = LoggerFactory.getLogger(ProcessOperationController.class);

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
	private DiskService diskService;
	private WebAPI webAPI;

	private InvoiceManager invoiceManager;
	private InvoiceService invoiceService;
	private FileUploadAPI fileUploadAPI;
	private ProcessOperationController processOperationController;
	private AccountInfoManager accountInfoManager;
	private AccountCredentialManager accountCredentialManager;
	private CustomPasswordEncoder customPasswordEncoder;

	/**
	 * 发起流程.
	 */
	@RequestMapping("process-operationInvoice-startProcessInstance")
	@Log(desc = "发起流程", action = "startProcess", operationDesc = "流程中心-我的流程-发起流程-发票")
	public String startProcessInstance(HttpServletRequest request, @ModelAttribute InvoiceDTO invoiceDTO,
			String areaId,String areaName,String companyId,String companyName,
			@RequestParam("bpmProcessId") String bpmProcessId,
			@RequestParam(value = "files", required = false) MultipartFile[] files,
			@RequestParam("businessKey") String businessKey, Model model) throws Exception {

		// String aa = getUserInfo();

		String userId = currentUserHolder.getUserId();
		String tenantId = tenantHolder.getTenantId();

		
		invoiceService.saveInvoice(request,invoiceDTO,areaId,areaName,companyId,companyName, userId, tenantId, bpmProcessId, businessKey, files);
		
		return "operation/process-operation-startProcessInstance";
	}

	/**
	 * 审批各节点获取外部申请单数据
	 * 
	 * @throws Exception
	 */
	@GET
	@Path("getInvoiceInfo")
	public List<InvoiceDTO> getInvoiceById(@QueryParam("id") String id) throws Exception {
		List<Invoice> invoiceInfo = invoiceManager.findBy("processInstanceId", id);
		List<InvoiceDTO> invoiceDTOList = new ArrayList<InvoiceDTO>();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		for (Invoice getInfo : invoiceInfo) {
			InvoiceDTO invoiceDTO = new InvoiceDTO();
			invoiceDTO.setUcode(getInfo.getUcode());
			invoiceDTO.setShopName(getInfo.getShopName());
			invoiceDTO.setShopTel(getInfo.getShopTel());
			invoiceDTO.setInvoiceDate(formatter.format(getInfo.getInvoiceDate()));
			invoiceDTO.setIdNumber(getInfo.getIdNumber());
			invoiceDTO.setOrderNumber(getInfo.getOrderNumber());
			invoiceDTO.setArea(getInfo.getArea());
			invoiceDTO.setSystem(getInfo.getSystem());
			invoiceDTO.setBranchOffice(getInfo.getBranchOffice());
			invoiceDTO.setInvoiceType(getInfo.getInvoiceType());
			invoiceDTO.setCategory(getInfo.getCategory());
			invoiceDTO.setInvoiceTitle(getInfo.getInvoiceTitle());
			invoiceDTO.setInvoiceDetail(getInfo.getInvoiceDetail());
			invoiceDTO.setInvoiceMoney(getInfo.getInvoiceMoney());
			invoiceDTO.setEnterpriseName(getInfo.getEnterpriseName());
			invoiceDTO.setTaxNumber(getInfo.getTaxNumber());
			invoiceDTO.setOpeningBank(getInfo.getOpeningBank());
			invoiceDTO.setAccountNumber(getInfo.getAccountNumber());
			invoiceDTO.setEnterpriseAddress(getInfo.getEnterpriseAddress());
			invoiceDTO.setInvoiceMailAddress(getInfo.getInvoiceMailAddress());
			invoiceDTO.setAddressee(getInfo.getAddressee());
			invoiceDTO.setAddresseeTel(getInfo.getAddresseeTel());
			invoiceDTO.setAddresseeSpareTel(getInfo.getAddresseeSpareTel());
			invoiceDTO.setEnclosure(getInfo.getEnclosure());
			invoiceDTO.setPath(getInfo.getPath());
			invoiceDTO.setId(getInfo.getId());
			invoiceDTO.setUserId(getInfo.getUserId().toString());
			invoiceDTO.setProcessInstanceId(getInfo.getProcessInstanceId());
			// testInvoiceDTO.setPath(getInfo.getPath());

			// 查询附件
			List<StoreInfo> list = fileUploadAPI.getStore("OA/process", getInfo.getId().toString());
			invoiceDTO.setStoreInfos(list);
			invoiceDTOList.add(invoiceDTO);

		}
		return invoiceDTOList;
	}

	/**
	 * 完成任务.
	 */
	@RequestMapping("process-operationInvoiceApproval-completeTask")
	@Log(desc = "审批流程", action = "confirmProcess", operationDesc = "流程中心-我的审批-待办审批-发票")
	public String completeTask(HttpServletRequest request, RedirectAttributes redirectAttributes,
			@RequestParam("processInstanceId") String processInstanceId,
			@RequestParam("humanTaskId") String humanTaskId,
			@RequestParam(value = "files", required = false) MultipartFile[] files,
			@RequestParam(value = "iptdels", required = false) String iptdels, String flag) throws Exception {
		try {
			invoiceService.saveReInvoice(request, redirectAttributes, processInstanceId, humanTaskId, files, iptdels);
		} catch (IllegalStateException ex) {
			logger.error(ex.getMessage(), ex);
			messageHelper.addFlashMessage(redirectAttributes, "任务不存在");
			return "redirect:/humantask/workspace-personalTasks.do";
		}

		return "operation/task-operation-completeTask";
	}

	// ==================================================================================
	// 验证密码是否正确
	@GET
	@Path("invoice-verifyPassword")
	public int VerifyPassword(@QueryParam("pwd") String pwd) {
		Long accountId = Long.parseLong(currentUserHolder.getUserId());
		AccountInfo accountInfo = accountInfoManager.get(accountId);
		String hql = "from AccountCredential where accountInfo=? and catalog='default'";
		AccountCredential accountCredential = accountCredentialManager.findUnique(hql, accountInfo);
		ChangePasswordResult changePasswordResult = new ChangePasswordResult();
		if (!isPasswordValid(pwd, accountCredential.getOperationPassword())) {
			changePasswordResult.setCode("user.user.input.passwordnotcorrect");
			changePasswordResult.setMessage("密码错误");

			return 0;
		} else {
			return 1;
		}
	}

	public boolean isPasswordValid(String rawPassword, String encodedPassword) {
		if (customPasswordEncoder != null) {
			return customPasswordEncoder.matches(rawPassword, encodedPassword);
		} else {
			return rawPassword.equals(encodedPassword);
		}
	}

	/**
	 * 申请单详情页
	 */
	@RequestMapping("form-detail")
	@Log(desc = "查看详情页", action = "processDetail", operationDesc = "流程中心-发票-详情")
	public String formDetail(@RequestParam("processInstanceId") String processInstanceId,
			@RequestParam(value = "isPrint", required = false) boolean isPrint, 
			@RequestParam(value = "viewBack", required = false) boolean viewBack,
			Model model) {
		try {
			this.getInvoiceById(processInstanceId);
			Invoice invoice = invoiceManager.findUniqueBy("processInstanceId", processInstanceId);
			Long id = invoice.getId();
			// 取附件
			model.addAttribute("picUrl", webAPI.getViewUrl());
			List<StoreInfo> invoiceList = fileUploadAPI.getStore("OA/process", Long.toString(id));
			model.addAttribute("StoreInfos", invoiceList);
			// 审批记录
			List<HumanTaskDTO> logHumanTaskDtos = humanTaskConnector
					.findHumanTasksForPositionByProcessInstanceId(processInstanceId);
			//获得审核时长
			logHumanTaskDtos = operationService.settingAuditDuration(logHumanTaskDtos);
			model.addAttribute("logHumanTaskDtos", logHumanTaskDtos);
			model.addAttribute("isPrint", isPrint);
			model.addAttribute("viewBack", viewBack);
			operationService.copyMsgUpdate(processInstanceId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "operation/process/InvoiceFormDetail";
	}

	// ~ ======================================================================
	
	/**
	 * 实际确认发起流程.
	 */
	public String doConfirmStartProcess(FormParameter formParameter, Model model) {
		humanTaskConnector.configTaskDefinitions(formParameter.getBusinessKey(),
				formParameter.getList("taskDefinitionKeys"), formParameter.getList("taskAssignees"));

		model.addAttribute("businessKey", formParameter.getBusinessKey());
		model.addAttribute("nextStep", formParameter.getNextStep());
		model.addAttribute("bpmProcessId", formParameter.getBpmProcessId());

		return "operation/process-operation-confirmStartProcess";
	}

	/**
	 * 实际显示开始表单.
	 */
	public String doViewStartForm(FormParameter formParameter, Model model, String tenantId) throws Exception {
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

		Record record = keyValueConnector.findByCode(formParameter.getBusinessKey());
		FormDTO formDto = formConnector.findForm(formParameter.getFormDto().getCode(), tenantId);

		if (record != null) {
			Xform xform = new XformBuilder().setStoreConnector(storeConnector).setUserConnector(userConnector)
					.setContent(formDto.getContent()).setRecord(record).build();
			model.addAttribute("xform", xform);
		} else {
			Xform xform = new XformBuilder().setStoreConnector(storeConnector).setUserConnector(userConnector)
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
				.findHumanTaskDefinitions(formParameter.getProcessDefinitionId());
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

	// ~ ======================================================================
	@Resource
	public void setWebAPI(WebAPI webAPI) {
		this.webAPI = webAPI;
	}

	@Resource
	public void setInvoiceManager(InvoiceManager invoiceManager) {
		this.invoiceManager = invoiceManager;
	}

	@Resource
	public void setInvoiceService(InvoiceService invoiceService) {
		this.invoiceService = invoiceService;
	}

	@Resource
	public void setFileUploadAPI(FileUploadAPI fileUploadAPI) {
		this.fileUploadAPI = fileUploadAPI;
	}

	@Resource
	public void setProcessOperationController(ProcessOperationController processOperationController) {
		this.processOperationController = processOperationController;
	}

	@Resource
	public void setAccountInfoManager(AccountInfoManager accountInfoManager) {
		this.accountInfoManager = accountInfoManager;
	}

	@Resource
	public void setAccountCredentialManager(AccountCredentialManager accountCredentialManager) {
		this.accountCredentialManager = accountCredentialManager;
	}

	@Resource
	public void setCustomPasswordEncoder(CustomPasswordEncoder customPasswordEncoder) {
		this.customPasswordEncoder = customPasswordEncoder;
	}
}
