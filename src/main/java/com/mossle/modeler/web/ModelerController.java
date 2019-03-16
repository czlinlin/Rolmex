package com.mossle.modeler.web;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mossle.api.tenant.TenantHolder;
import com.mossle.bpm.cmd.SyncProcessCmd;
import com.mossle.core.mapper.JsonMapper;
import com.mossle.core.page.Page;
import com.mossle.core.util.StringUtils;

import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ProcessDefinition;
import org.apache.commons.io.IOUtils;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * modeler.
 * 
 * @author Lingo
 */
@Controller
@RequestMapping("modeler")
public class ModelerController {
	
    private static Logger logger = LoggerFactory.getLogger(ModelerController.class);
    private ProcessEngine processEngine;
    private TenantHolder tenantHolder;
    private JdbcTemplate jdbcTemplate;
    private JsonMapper jsonMapper = new JsonMapper();

    @RequestMapping("modeler-list")
    public String list(@ModelAttribute Page page,org.springframework.ui.Model model,
    		@RequestParam(value = "name", required = false) String name) {
    	
    	long count = 0L;
    	//List<Model> models = new  ArrayList<Model>();改模糊查询前数据容器
    	List<Map<String,Object>> modelList = new  ArrayList<Map<String,Object>>();
    	if (StringUtils.isBlank(name)) {
    		/*count = processEngine.getRepositoryService().createModelQuery().count();
    		models = processEngine.getRepositoryService().createModelQuery().listPage((int) page.getStart(), page.getPageSize());*/
    		String sql = "select a.ID_,a.NAME_,a.CREATE_TIME_,a.LAST_UPDATE_TIME_,a.VERSION_,a.DEPLOYMENT_ID_ from act_re_model a limit "+(int)page.getStart()+","+page.getPageSize();
    		modelList = jdbcTemplate.queryForList(sql);
    		String countSql = "select count(*) from act_re_model";
    		count = jdbcTemplate.queryForInt(countSql);
    	} else {
    		//精确查询改模糊查询 sjx 18.09.14
    		/*count = processEngine.getRepositoryService().createModelQuery().modelNameLike(name).count();
    		models = processEngine.getRepositoryService().createModelQuery().modelNameLike(name).listPage((int) page.getStart(), page.getPageSize());*/
    		String sql = "select a.ID_,a.NAME_,a.CREATE_TIME_,a.LAST_UPDATE_TIME_,a.VERSION_,a.DEPLOYMENT_ID_ from act_re_model a where a.NAME_ like ? limit "+(int)page.getStart()+","+page.getPageSize();
    		modelList = jdbcTemplate.queryForList(sql, "%"+name+"%");
    		String countSql = "select count(*) from act_re_model where NAME_ like ?";
    		count = jdbcTemplate.queryForInt(countSql, "%"+name+"%");
    	}
    	page.setResult(modelList);
    	page.setTotalCount(count);
        
        model.addAttribute("page", page);
        model.addAttribute("name", name);
        return "modeler/modeler-list";
    }

    @RequestMapping("modeler-open")
    public String open(@RequestParam(value = "id", required = false) String id)
            throws Exception {
        RepositoryService repositoryService = processEngine
                .getRepositoryService();
        Model model = null;

        if (!StringUtils.isEmpty(id)) {
            model = repositoryService.getModel(id);
        }

        if (model == null) {
            model = repositoryService.newModel();
            repositoryService.saveModel(model);
            id = model.getId();
        }

        // return "redirect:/cdn/modeler/editor.html?id=" + id;
        return "redirect:/cdn/modeler/modeler.html?modelId=" + id;
    }

    @RequestMapping("modeler-remove")
    public String remove(@RequestParam("id") String id) {
        processEngine.getRepositoryService().deleteModel(id);

        return "redirect:/modeler/modeler-list.do";
    }

    @RequestMapping("modeler-deploy")
    public String deploy(@RequestParam("id") String id,
            org.springframework.ui.Model theModel) throws Exception {
    	
        String tenantId = tenantHolder.getTenantId();
        RepositoryService repositoryService = processEngine
                .getRepositoryService();
        Model modelData = repositoryService.getModel(id);
        byte[] bytes = repositoryService
                .getModelEditorSource(modelData.getId());

        if (bytes == null) {
            theModel.addAttribute("message", "模型数据为空，请先设计流程并成功保存，再进行发布。");

            return "modeler/failure";
        }

        JsonNode modelNode = (JsonNode) new ObjectMapper().readTree(bytes);
        byte[] bpmnBytes = null;

        BpmnModel model = new BpmnJsonConverter().convertToBpmnModel(modelNode);
        bpmnBytes = new BpmnXMLConverter().convertToXML(model);

        String processName = modelData.getName() + ".bpmn20.xml";
        Deployment deployment = repositoryService.createDeployment()
                .name(modelData.getName())
                .addString(processName, new String(bpmnBytes, "UTF-8"))
                .tenantId(tenantId).deploy();
        modelData.setDeploymentId(deployment.getId());
        repositoryService.saveModel(modelData);

        List<ProcessDefinition> processDefinitions = repositoryService
                .createProcessDefinitionQuery()
                .deploymentId(deployment.getId()).list();

        for (ProcessDefinition processDefinition : processDefinitions) {
            processEngine.getManagementService().executeCommand(
                    new SyncProcessCmd(processDefinition.getId()));
        }

        return "redirect:/modeler/modeler-list.do";
    }

    @RequestMapping("model/{modelId}/json")
    @ResponseBody
    public String openModel(@PathVariable("modelId") String modelId)
            throws Exception {
        RepositoryService repositoryService = processEngine
                .getRepositoryService();
        Model model = repositoryService.getModel(modelId);

        if (model == null) {
            logger.info("model({}) is null", modelId);
            model = repositoryService.newModel();
            repositoryService.saveModel(model);
        }

        Map root = new HashMap();
        root.put("modelId", model.getId());
        root.put("name", "name");
        root.put("revision", 1);
        root.put("description", "description");

        byte[] bytes = repositoryService.getModelEditorSource(model.getId());

        if (bytes != null) {
            String modelEditorSource = new String(bytes, "utf-8");
            logger.info("modelEditorSource : {}", modelEditorSource);

            Map modelNode = jsonMapper.fromJson(modelEditorSource, Map.class);
            root.put("model", modelNode);
        } else {
            Map modelNode = new HashMap();
            modelNode.put("id", "canvas");
            modelNode.put("resourceId", "canvas");

            Map stencilSetNode = new HashMap();
            stencilSetNode.put("namespace",
                    "http://b3mn.org/stencilset/bpmn2.0#");
            modelNode.put("stencilset", stencilSetNode);

            model.setMetaInfo(jsonMapper.toJson(root));
            model.setName("name");
            model.setKey("key");

            root.put("model", modelNode);
        }

        logger.info("model : {}", root);

        return jsonMapper.toJson(root);
    }

    @RequestMapping("editor/stencilset")
    @ResponseBody
    public String stencilset() throws Exception {
        InputStream stencilsetStream = this.getClass().getClassLoader()
                .getResourceAsStream("stencilset.json");

        try {
            return IOUtils.toString(stencilsetStream, "utf-8");
        } catch (Exception e) {
            throw new RuntimeException("Error while loading stencil set", e);
        }
    }

    @RequestMapping("model/{modelId}/save")
    @ResponseBody
    public String modelSave(@PathVariable("modelId") String modelId,
            @RequestParam("description") String description,
            @RequestParam("json_xml") String jsonXml,
            @RequestParam("name") String name,
            @RequestParam("svg_xml") String svgXml) throws Exception {
        RepositoryService repositoryService = processEngine
                .getRepositoryService();
        Model model = repositoryService.getModel(modelId);
        model.setName(name);
        // model.setMetaInfo(root.toString());
        logger.info("jsonXml : {}", jsonXml);
        repositoryService.saveModel(model);
        repositoryService.addModelEditorSource(model.getId(),
                jsonXml.getBytes("utf-8"));

        return "{}";
    }

    @RequestMapping("xml2json")
    public String xml2json(
            @RequestParam("processDefinitionId") String processDefinitionId)
            throws Exception {
        RepositoryService repositoryService = processEngine
                .getRepositoryService();

        ProcessDefinition processDefinition = repositoryService
                .getProcessDefinition(processDefinitionId);

        Model model = repositoryService.newModel();
        model.setName(processDefinition.getName());
        model.setDeploymentId(processDefinition.getDeploymentId());
        repositoryService.saveModel(model);

        BpmnModel bpmnModel = repositoryService
                .getBpmnModel(processDefinitionId);
        ObjectNode objectNode = new BpmnJsonConverter()
                .convertToJson(bpmnModel);

        String json = objectNode.toString();

        repositoryService.addModelEditorSource(model.getId(),
                json.getBytes("utf-8"));

        return "redirect:/modeler/modeler-list.do";
    }

    // ~ ==================================================
    @Resource
    public void setProcessEngine(ProcessEngine processEngine) {
        this.processEngine = processEngine;
    }

    @Resource
    public void setTenantHolder(TenantHolder tenantHolder) {
        this.tenantHolder = tenantHolder;
    }
    @Resource
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
    	this.jdbcTemplate = jdbcTemplate;
    }
}
