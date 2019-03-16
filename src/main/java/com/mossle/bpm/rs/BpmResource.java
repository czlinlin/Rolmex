package com.mossle.bpm.rs;

import java.util.*;

import javax.annotation.Resource;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.mossle.api.dict.DictConnector;
import com.mossle.api.humantask.HumanTaskConnector;
import com.mossle.api.humantask.HumanTaskDTO;
import com.mossle.api.keyvalue.KeyValueConnector;
import com.mossle.api.keyvalue.Record;
import com.mossle.api.notification.NotificationConnector;
import com.mossle.api.party.PartyConnector;
import com.mossle.api.party.PartyDTO;
import com.mossle.api.tenant.TenantHolder;
import com.mossle.base.persistence.domain.BusinessDetailEntity;
import com.mossle.base.persistence.manager.BusinessDetailManager;
import com.mossle.bpm.cmd.FindNextActivitiesCmd;
import com.mossle.bpm.cmd.FindPreviousActivitiesCmd;
import com.mossle.bpm.cmd.FindTaskDefinitionsCmd;
import com.mossle.bpm.persistence.domain.BpmConfBase;
import com.mossle.bpm.persistence.domain.BpmConfNode;
import com.mossle.bpm.persistence.domain.BpmProcess;
import com.mossle.bpm.persistence.manager.BpmConfBaseManager;
import com.mossle.bpm.persistence.manager.BpmConfNodeManager;
import com.mossle.bpm.persistence.manager.BpmProcessManager;
import com.mossle.bpm.service.WithDrawService;
import com.mossle.bpm.support.ActivityDTO;
import com.mossle.core.annotation.Log;
import com.mossle.core.auth.CurrentUserHolder;
import com.mossle.core.util.BaseDTO;
import com.mossle.dict.persistence.domain.DictInfo;
import com.mossle.humantask.persistence.domain.TaskInfo;
import com.mossle.humantask.persistence.manager.TaskInfoManager;
import com.mossle.msg.MsgConstants;
import com.mossle.msg.persistence.domain.MsgInfo;
import com.mossle.msg.persistence.manager.MsgInfoManager;
import com.mossle.operation.persistence.domain.CustomEntity;
import com.mossle.operation.persistence.manager.CustomManager;
import com.mossle.party.persistence.domain.PartyEntity;

import com.mossle.spi.process.InternalProcessConnector;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.HistoryService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.bpmn.behavior.BoundaryEventActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.CallActivityBehavior;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.bpmn.parser.ErrorEventDefinition;
import org.activiti.engine.impl.bpmn.parser.EventSubscriptionDeclaration;
import org.activiti.engine.impl.jobexecutor.TimerDeclarationImpl;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmActivity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.delegate.ActivityBehavior;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.Lane;
import org.activiti.engine.impl.pvm.process.LaneSet;
import org.activiti.engine.impl.pvm.process.ParticipantProcess;
import org.activiti.engine.impl.pvm.process.TransitionImpl;
import org.activiti.engine.impl.task.TaskDefinition;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Component
@Path("bpm")
public class BpmResource {
    private static Logger logger = LoggerFactory.getLogger(BpmResource.class);
    private ProcessEngine processEngine;
    private RuntimeService runtimeService;
    private RepositoryServiceImpl repositoryService;
    private HistoryService historyService;
    private String processInstanceId;
    private String processDefinitionId;
    private ProcessInstance processInstance;
    private ProcessDefinitionEntity processDefinition;
    private List<String> highLightedFlows;
    private List<String> highLightedActivities;
    private Map<String, ObjectNode> subProcessInstanceMap;
    private List<String> historicActivityInstanceList;
    private BusinessDetailManager businessDetailManager;
    private TenantHolder tenantHolder;
    private JdbcTemplate jdbcTemplate;
    private PartyConnector partyConnector;
    private NotificationConnector notificationConnector;
    private TaskInfoManager taskInfoManager;
    private CurrentUserHolder currentUserHolder;
    private CustomManager customManager;//shijingxin
    private KeyValueConnector keyValueConnector;
    private InternalProcessConnector internalProcessConnector;
    private HumanTaskConnector humanTaskConnector;

    @Resource
    private BpmProcessManager bpmProcessManager;
    @Resource
    private BpmConfBaseManager bpmConfBaseManager;
    @Resource
    private BpmConfNodeManager bpmConfNodeManager;
    @Resource
    private DictConnector dictConnector;
    @Resource
    private MsgInfoManager msgInfoManager;
    @Resource
    private WithDrawService withDrawService;

    private void init() {
        runtimeService = processEngine.getRuntimeService();
        historyService = processEngine.getHistoryService();
        repositoryService = (RepositoryServiceImpl) processEngine
                .getRepositoryService();
        processInstance = null;
        processDefinition = null;
        highLightedFlows = new ArrayList();
        highLightedActivities = new ArrayList();
        subProcessInstanceMap = new HashMap<String, ObjectNode>();
        // ~
        historicActivityInstanceList = new ArrayList<String>();
    }

    /**
     * 常规流程撤回.
     */
    @POST
    @Path("workspace-withdraw")
    @Produces(MediaType.APPLICATION_JSON)
    public BaseDTO withdraw(
            @FormParam("processInstanceId") String processInstanceId) {
    	return withDrawService.normalWithDraw(processInstanceId,currentUserHolder.getUserId());
    }
    /**
     * 自定义流程撤回.
     */
    @POST
    @Path("workspace-withdraw-custom")
    @Produces(MediaType.APPLICATION_JSON)
    public BaseDTO customWithdraw(@FormParam("processInstanceId") String processInstanceId) {
    	return withDrawService.customWithDraw(processInstanceId);
    }
    /**
     * 审核人审批时
     * @param processInstanceId
     * @return
     */
    @Path("getStatus")
    @GET
    @ResponseBody
    @Produces(MediaType.APPLICATION_JSON)
    public String checkStatus(@QueryParam("processInstanceId") String processInstanceId,@QueryParam("humanTaskId") String humanTaskId,@QueryParam("userId") String userId,@QueryParam("resource") String resource){
    	String result = "";
    	Record record = keyValueConnector.findByRef(processInstanceId);
    	//查询此次任务对应的代理人比对审核人
    	TaskInfo t = taskInfoManager.findUniqueBy("id", Long.parseLong(humanTaskId));
    	String assignee = t.getAssignee();
    	if(resource == null)
    		resource = "";
    	if(resource.equals("adjustment")){
    		if(record.getAuditStatus().equals("1")||record.getAuditStatus().equals("6")){
    			result = "error";
    		}else if(!assignee.equals(userId)){
    			result = "noAuth";
    		}
    	}else{
    		if(record.getAuditStatus().equals("8")){
        		result = "error";
        	}else if(!assignee.equals(userId)){
        		result = "noAuth";
        	}else{
        		result = "pass";
        	}
    	}
    	return result;
    } 
    /**
     * 流程进入调整环节时判断是正常驳回还是撤回进入的
     * @param processInstanceId
     * @return
     */
    @Path("removeButton")
    @GET
    @ResponseBody
    @Produces(MediaType.APPLICATION_JSON)
    public String removeButton(@QueryParam("processInstanceId") String processInstanceId){
    	String result = "";
    	String hql = "from TaskInfo where action='撤回申请' and status='active' and processInstanceId=?";
    	TaskInfo taskInfo = taskInfoManager.findUnique(hql,processInstanceId);
    	if(taskInfo == null){
    		result = "normalReject";
    	}else{
    		result = "withdraw";
    	}
    	return result;
    } 
    //暂不使用
    @GET
    @Path("confbaseId")
    public List<Process> confbaseId(@QueryParam("bpmProcessId") String bpmProcessId) {
        Process process = new Process();
        List<Process> processs = new ArrayList();
        List<BpmProcess> bpmProcess = bpmProcessManager.findBy("id", Long.parseLong(bpmProcessId));
        Long confbaseId = bpmProcess.get(0).getBpmConfBase().getId();
        List<BpmConfBase> bpmConfBases = processDefinitionId(confbaseId);//查出流程定义id
        for (BpmConfBase bpmConfBase : bpmConfBases) {
            process.setProcessDefinitionId(bpmConfBase.getProcessDefinitionId());
        }
        List<BpmConfNode> bpmConfNodes = activityId(confbaseId);//查出该流程的所有activityId
        process.setActivityId(bpmConfNodes.get(2).getCode());
        processs.add(process);
        return processs;
    }

    /**
     * @param bpmProcessId
     * @return
     * 后台根据流程id获取定义id和activityId(方法用于业务细分获取流程过程，取的是流程图userTask的名字)
     */
    public String getResult(String bpmProcessId) {
        List<BpmProcess> bpmProcess = bpmProcessManager.findBy("id", Long.parseLong(bpmProcessId));
        Long confbaseId = bpmProcess.get(0).getBpmConfBase().getId();
        List<BpmConfBase> bpmConfBases = processDefinitionId(confbaseId);//查出流程定义id
        String processDefinitionId = bpmConfBases.get(0).getProcessDefinitionId();
        List<BpmConfNode> bpmConfNodes = activityId(confbaseId);//查出该流程的所有activityId
        String activityId = bpmConfNodes.get(2).getCode();
        String result = convertActivityDtos(processDefinitionId, activityId);
        return result;
    }

    
    public List<BpmConfBase> processDefinitionId(@QueryParam("processDefinitionId") Long processDefinitionId) {
        List<BpmConfBase> bpmConfBase = bpmConfBaseManager.findBy("id", processDefinitionId);
        return bpmConfBase;
    }

    
    public List<BpmConfNode> activityId(@QueryParam("bpmProcessId") Long bpmProcessId) {
        String hql = "from BpmConfNode where bpmConfBase.id = ?";
        List<BpmConfNode> bpmConfNode = bpmConfNodeManager.find(hql, bpmProcessId);
        return bpmConfNode;
    }

    @GET
    @Path("diagram")
    public JSONPObject diagram(
            @QueryParam("processDefinitionId") String processDefinitionId,
            @QueryParam("processInstanceId") String processInstanceId,
            @QueryParam("callback") String callback) {
        init();
        this.processDefinitionId = processDefinitionId;
        this.processInstanceId = processInstanceId;

        ObjectNode diagram = getDiagram();

        return new JSONPObject(callback, diagram);
    }

    @GET
    @Path("highlighted")
    public JSONPObject highlighted(
            @QueryParam("processInstanceId") String processInstanceId,
            @QueryParam("callback") String callback) {
        init();
        this.processInstanceId = processInstanceId;

        ObjectNode highlighted = getHighlighted();

        return new JSONPObject(callback, highlighted);
    }

    // ~ ======================================================================
    public ObjectNode getDiagram() {
        // TODO: do it all with Map and convert at the end to JSON
        if (processInstanceId != null) {
            processInstance = runtimeService.createProcessInstanceQuery()
                    .processInstanceId(processInstanceId).singleResult();

            if (processInstance == null) {
                // TODO: return empty response
                return null;
            }

            processDefinitionId = processInstance.getProcessDefinitionId();

            List<ProcessInstance> subProcessInstances = runtimeService
                    .createProcessInstanceQuery()
                    .superProcessInstanceId(processInstanceId).list();

            for (ProcessInstance subProcessInstance : subProcessInstances) {
                String subDefId = subProcessInstance.getProcessDefinitionId();

                String superExecutionId = ((ExecutionEntity) subProcessInstance)
                        .getSuperExecutionId();
                ProcessDefinitionEntity subDef = (ProcessDefinitionEntity) repositoryService
                        .getDeployedProcessDefinition(subDefId);

                ObjectNode processInstanceJSON = new ObjectMapper()
                        .createObjectNode();
                processInstanceJSON.put("processInstanceId",
                        subProcessInstance.getId());
                processInstanceJSON.put("superExecutionId", superExecutionId);
                processInstanceJSON.put("processDefinitionId", subDef.getId());
                processInstanceJSON
                        .put("processDefinitionKey", subDef.getKey());
                processInstanceJSON.put("processDefinitionName",
                        subDef.getName());

                subProcessInstanceMap
                        .put(superExecutionId, processInstanceJSON);
            }
        }

        if (processDefinitionId == null) {
            throw new ActivitiException("No process definition id provided");
        }

        processDefinition = (ProcessDefinitionEntity) repositoryService
                .getDeployedProcessDefinition(processDefinitionId);

        if (processDefinition == null) {
            throw new ActivitiException("Process definition "
                    + processDefinitionId + " could not be found");
        }

        ObjectNode responseJSON = new ObjectMapper().createObjectNode();

        // Process definition
        JsonNode pdrJSON = getProcessDefinitionResponse(processDefinition);

        if (pdrJSON != null) {
            responseJSON.set("processDefinition", pdrJSON);
        }

        // Highlighted activities
        if (processInstance != null) {
            ArrayNode activityArray = new ObjectMapper().createArrayNode();
            ArrayNode flowsArray = new ObjectMapper().createArrayNode();

            highLightedActivities = runtimeService
                    .getActiveActivityIds(processInstanceId);
            highLightedFlows = getHighLightedFlows();

            for (String activityName : highLightedActivities) {
                activityArray.add(activityName);
            }

            for (String flow : highLightedFlows) {
                flowsArray.add(flow);
            }

            responseJSON.set("highLightedActivities", activityArray);
            responseJSON.set("highLightedFlows", flowsArray);
        }

        // Pool shape, if process is participant in collaboration
        if (processDefinition.getParticipantProcess() != null) {
            ParticipantProcess pProc = processDefinition
                    .getParticipantProcess();

            ObjectNode participantProcessJSON = new ObjectMapper()
                    .createObjectNode();
            participantProcessJSON.put("id", pProc.getId());

            if (StringUtils.isNotEmpty(pProc.getName())) {
                participantProcessJSON.put("name", pProc.getName());
            } else {
                participantProcessJSON.put("name", "");
            }

            participantProcessJSON.put("x", pProc.getX());
            participantProcessJSON.put("y", pProc.getY());
            participantProcessJSON.put("width", pProc.getWidth());
            participantProcessJSON.put("height", pProc.getHeight());

            responseJSON.set("participantProcess", participantProcessJSON);
        }

        // Draw lanes
        if ((processDefinition.getLaneSets() != null)
                && (processDefinition.getLaneSets().size() > 0)) {
            ArrayNode laneSetArray = new ObjectMapper().createArrayNode();

            for (LaneSet laneSet : processDefinition.getLaneSets()) {
                ArrayNode laneArray = new ObjectMapper().createArrayNode();

                if ((laneSet.getLanes() != null)
                        && (laneSet.getLanes().size() > 0)) {
                    for (Lane lane : laneSet.getLanes()) {
                        ObjectNode laneJSON = new ObjectMapper()
                                .createObjectNode();
                        laneJSON.put("id", lane.getId());

                        if (StringUtils.isNotEmpty(lane.getName())) {
                            laneJSON.put("name", lane.getName());
                        } else {
                            laneJSON.put("name", "");
                        }

                        laneJSON.put("x", lane.getX());
                        laneJSON.put("y", lane.getY());
                        laneJSON.put("width", lane.getWidth());
                        laneJSON.put("height", lane.getHeight());

                        List<String> flowNodeIds = lane.getFlowNodeIds();
                        ArrayNode flowNodeIdsArray = new ObjectMapper()
                                .createArrayNode();

                        for (String flowNodeId : flowNodeIds) {
                            flowNodeIdsArray.add(flowNodeId);
                        }

                        laneJSON.set("flowNodeIds", flowNodeIdsArray);

                        laneArray.add(laneJSON);
                    }
                }

                ObjectNode laneSetJSON = new ObjectMapper().createObjectNode();
                laneSetJSON.put("id", laneSet.getId());

                if (StringUtils.isNotEmpty(laneSet.getName())) {
                    laneSetJSON.put("name", laneSet.getName());
                } else {
                    laneSetJSON.put("name", "");
                }

                laneSetJSON.set("lanes", laneArray);

                laneSetArray.add(laneSetJSON);
            }

            if (laneSetArray.size() > 0) {
                responseJSON.set("laneSets", laneSetArray);
            }
        }

        ArrayNode sequenceFlowArray = new ObjectMapper().createArrayNode();
        ArrayNode activityArray = new ObjectMapper().createArrayNode();

        // Activities and their sequence-flows
        for (ActivityImpl activity : processDefinition.getActivities()) {
            getActivity(activity, activityArray, sequenceFlowArray);
        }

        responseJSON.set("activities", activityArray);
        responseJSON.set("sequenceFlows", sequenceFlowArray);

        return responseJSON;
    }

    // TODO: move this method to some 'utils'
    private List<String> getHighLightedFlows() {
        List<String> highLightedFlows = new ArrayList<String>();
        List<HistoricActivityInstance> historicActivityInstances = historyService
                .createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceId)
                .orderByHistoricActivityInstanceStartTime().asc().list();

        List<String> historicActivityInstanceList = new ArrayList<String>();

        for (HistoricActivityInstance hai : historicActivityInstances) {
            historicActivityInstanceList.add(hai.getActivityId());
        }

        // add current activities to list
        highLightedActivities = runtimeService
                .getActiveActivityIds(processInstanceId);
        historicActivityInstanceList.addAll(highLightedActivities);

        // activities and their sequence-flows
        for (ActivityImpl activity : processDefinition.getActivities()) {
            int index = historicActivityInstanceList.indexOf(activity.getId());

            if ((index >= 0)
                    && ((index + 1) < historicActivityInstanceList.size())) {
                List<PvmTransition> pvmTransitionList = activity
                        .getOutgoingTransitions();

                for (PvmTransition pvmTransition : pvmTransitionList) {
                    String destinationFlowId = pvmTransition.getDestination()
                            .getId();

                    if (destinationFlowId.equals(historicActivityInstanceList
                            .get(index + 1))) {
                        highLightedFlows.add(pvmTransition.getId());
                    }
                }
            }
        }

        return highLightedFlows;
    }

    private void getActivity(ActivityImpl activity, ArrayNode activityArray,
                             ArrayNode sequenceFlowArray) {
        ObjectNode activityJSON = new ObjectMapper().createObjectNode();

        // Gather info on the multi instance marker
        String multiInstance = (String) activity.getProperty("multiInstance");

        if (multiInstance != null) {
            if (!"sequential".equals(multiInstance)) {
                multiInstance = "parallel";
            }
        }

        ActivityBehavior activityBehavior = activity.getActivityBehavior();

        // Gather info on the collapsed marker
        Boolean collapsed = (activityBehavior instanceof CallActivityBehavior);
        Boolean expanded = (Boolean) activity
                .getProperty(BpmnParse.PROPERTYNAME_ISEXPANDED);

        if (expanded != null) {
            collapsed = !expanded;
        }

        Boolean isInterrupting = null;

        if (activityBehavior instanceof BoundaryEventActivityBehavior) {
            isInterrupting = ((BoundaryEventActivityBehavior) activityBehavior)
                    .isInterrupting();
        }

        // Outgoing transitions of activity
        for (PvmTransition sequenceFlow : activity.getOutgoingTransitions()) {
            String flowName = (String) sequenceFlow.getProperty("name");
            boolean isHighLighted = (highLightedFlows.contains(sequenceFlow
                    .getId()));
            boolean isConditional = (sequenceFlow
                    .getProperty(BpmnParse.PROPERTYNAME_CONDITION) != null)
                    && !((String) activity.getProperty("type")).toLowerCase()
                    .contains("gateway");
            boolean isDefault = sequenceFlow.getId().equals(
                    activity.getProperty("default"))
                    && ((String) activity.getProperty("type")).toLowerCase()
                    .contains("gateway");

            List<Integer> waypoints = ((TransitionImpl) sequenceFlow)
                    .getWaypoints();
            ArrayNode xPointArray = new ObjectMapper().createArrayNode();
            ArrayNode yPointArray = new ObjectMapper().createArrayNode();

            for (int i = 0; i < waypoints.size(); i += 2) { // waypoints.size()
                // minimally 4: x1, y1,
                // x2, y2
                xPointArray.add(waypoints.get(i));
                yPointArray.add(waypoints.get(i + 1));
            }

            ObjectNode flowJSON = new ObjectMapper().createObjectNode();
            flowJSON.put("id", sequenceFlow.getId());
            flowJSON.put("name", flowName);
            flowJSON.put("flow", "(" + sequenceFlow.getSource().getId() + ")--"
                    + sequenceFlow.getId() + "-->("
                    + sequenceFlow.getDestination().getId() + ")");

            if (isConditional) {
                flowJSON.put("isConditional", isConditional);
            }

            if (isDefault) {
                flowJSON.put("isDefault", isDefault);
            }

            if (isHighLighted) {
                flowJSON.put("isHighLighted", isHighLighted);
            }

            flowJSON.set("xPointArray", xPointArray);
            flowJSON.set("yPointArray", yPointArray);

            sequenceFlowArray.add(flowJSON);
        }

        // Nested activities (boundary events)
        ArrayNode nestedActivityArray = new ObjectMapper().createArrayNode();

        for (ActivityImpl nestedActivity : activity.getActivities()) {
            nestedActivityArray.add(nestedActivity.getId());
        }

        Map<String, Object> properties = activity.getProperties();
        ObjectNode propertiesJSON = new ObjectMapper().createObjectNode();

        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            String key = entry.getKey();
            Object prop = entry.getValue();

            if (prop instanceof String) {
                propertiesJSON.put(key, (String) properties.get(key));
            } else if (prop instanceof Integer) {
                propertiesJSON.put(key, (Integer) properties.get(key));
            } else if (prop instanceof Boolean) {
                propertiesJSON.put(key, (Boolean) properties.get(key));
            } else if ("initial".equals(key)) {
                ActivityImpl act = (ActivityImpl) properties.get(key);
                propertiesJSON.put(key, act.getId());
            } else if ("timerDeclarations".equals(key)) {
                ArrayList<TimerDeclarationImpl> timerDeclarations = (ArrayList<TimerDeclarationImpl>) properties
                        .get(key);
                ArrayNode timerDeclarationArray = new ObjectMapper()
                        .createArrayNode();

                if (timerDeclarations != null) {
                    for (TimerDeclarationImpl timerDeclaration : timerDeclarations) {
                        ObjectNode timerDeclarationJSON = new ObjectMapper()
                                .createObjectNode();

                        timerDeclarationJSON.put("isExclusive",
                                timerDeclaration.isExclusive());

                        if (timerDeclaration.getRepeat() != null) {
                            timerDeclarationJSON.put("repeat",
                                    timerDeclaration.getRepeat());
                        }

                        timerDeclarationJSON.put("retries",
                                String.valueOf(timerDeclaration.getRetries()));
                        timerDeclarationJSON.put("type",
                                timerDeclaration.getJobHandlerType());
                        timerDeclarationJSON.put("configuration",
                                timerDeclaration.getJobHandlerConfiguration());

                        timerDeclarationArray.add(timerDeclarationJSON);
                    }
                }

                if (timerDeclarationArray.size() > 0) {
                    propertiesJSON.set(key, timerDeclarationArray);
                }

                // TODO: implement getting description
            } else if ("eventDefinitions".equals(key)) {
                ArrayList<EventSubscriptionDeclaration> eventDefinitions = (ArrayList<EventSubscriptionDeclaration>) properties
                        .get(key);
                ArrayNode eventDefinitionsArray = new ObjectMapper()
                        .createArrayNode();

                if (eventDefinitions != null) {
                    for (EventSubscriptionDeclaration eventDefinition : eventDefinitions) {
                        ObjectNode eventDefinitionJSON = new ObjectMapper()
                                .createObjectNode();

                        if (eventDefinition.getActivityId() != null) {
                            eventDefinitionJSON.put("activityId",
                                    eventDefinition.getActivityId());
                        }

                        eventDefinitionJSON.put("eventName",
                                eventDefinition.getEventName());
                        eventDefinitionJSON.put("eventType",
                                eventDefinition.getEventType());
                        eventDefinitionJSON.put("isAsync",
                                eventDefinition.isAsync());
                        eventDefinitionJSON.put("isStartEvent",
                                eventDefinition.isStartEvent());
                        eventDefinitionsArray.add(eventDefinitionJSON);
                    }
                }

                if (eventDefinitionsArray.size() > 0) {
                    propertiesJSON.set(key, eventDefinitionsArray);
                }

                // TODO: implement it
            } else if ("errorEventDefinitions".equals(key)) {
                ArrayList<ErrorEventDefinition> errorEventDefinitions = (ArrayList<ErrorEventDefinition>) properties
                        .get(key);
                ArrayNode errorEventDefinitionsArray = new ObjectMapper()
                        .createArrayNode();

                if (errorEventDefinitions != null) {
                    for (ErrorEventDefinition errorEventDefinition : errorEventDefinitions) {
                        ObjectNode errorEventDefinitionJSON = new ObjectMapper()
                                .createObjectNode();

                        if (errorEventDefinition.getErrorCode() != null) {
                            errorEventDefinitionJSON.put("errorCode",
                                    errorEventDefinition.getErrorCode());
                        } else {
                            errorEventDefinitionJSON.putNull("errorCode");
                        }

                        errorEventDefinitionJSON.put("handlerActivityId",
                                errorEventDefinition.getHandlerActivityId());

                        errorEventDefinitionsArray
                                .add(errorEventDefinitionJSON);
                    }
                }

                if (errorEventDefinitionsArray.size() > 0) {
                    propertiesJSON.set(key, errorEventDefinitionsArray);
                }
            }
        }

        if ("callActivity".equals(properties.get("type"))) {
            CallActivityBehavior callActivityBehavior = null;

            if (activityBehavior instanceof CallActivityBehavior) {
                callActivityBehavior = (CallActivityBehavior) activityBehavior;
            }

            if (callActivityBehavior != null) {
                propertiesJSON.put("processDefinitonKey",
                        callActivityBehavior.getProcessDefinitonKey());

                // get processDefinitonId from execution or get last processDefinitonId
                // by key
                ArrayNode processInstanceArray = new ObjectMapper()
                        .createArrayNode();

                if (processInstance != null) {
                    List<Execution> executionList = runtimeService
                            .createExecutionQuery()
                            .processInstanceId(processInstanceId)
                            .activityId(activity.getId()).list();

                    if (executionList.size() > 0) {
                        for (Execution execution : executionList) {
                            ObjectNode processInstanceJSON = subProcessInstanceMap
                                    .get(execution.getId());
                            processInstanceArray.add(processInstanceJSON);
                        }
                    }
                }

                // If active activities nas no instance of this callActivity then add
                // last definition
                if (processInstanceArray.size() == 0) {
                    // Get last definition by key
                    ProcessDefinition lastProcessDefinition = repositoryService
                            .createProcessDefinitionQuery()
                            .processDefinitionKey(
                                    callActivityBehavior
                                            .getProcessDefinitonKey())
                            .latestVersion().singleResult();

                    // TODO: unuseful fields there are processDefinitionName,
                    // processDefinitionKey
                    ObjectNode processInstanceJSON = new ObjectMapper()
                            .createObjectNode();
                    processInstanceJSON.put("processDefinitionId",
                            lastProcessDefinition.getId());
                    processInstanceJSON.put("processDefinitionKey",
                            lastProcessDefinition.getKey());
                    processInstanceJSON.put("processDefinitionName",
                            lastProcessDefinition.getName());
                    processInstanceArray.add(processInstanceJSON);
                }

                if (processInstanceArray.size() > 0) {
                    propertiesJSON.set("processDefinitons",
                            processInstanceArray);
                }
            }
        }

        activityJSON.put("activityId", activity.getId());
        activityJSON.set("properties", propertiesJSON);

        if (multiInstance != null) {
            activityJSON.put("multiInstance", multiInstance);
        }

        if (collapsed) {
            activityJSON.put("collapsed", collapsed);
        }

        if (nestedActivityArray.size() > 0) {
            activityJSON.set("nestedActivities", nestedActivityArray);
        }

        if (isInterrupting != null) {
            activityJSON.put("isInterrupting", isInterrupting);
        }

        activityJSON.put("x", activity.getX());
        activityJSON.put("y", activity.getY());
        activityJSON.put("width", activity.getWidth());
        activityJSON.put("height", activity.getHeight());

        activityArray.add(activityJSON);

        // Nested activities (boundary events)
        for (ActivityImpl nestedActivity : activity.getActivities()) {
            getActivity(nestedActivity, activityArray, sequenceFlowArray);
        }
    }

    private JsonNode getProcessDefinitionResponse(
            ProcessDefinitionEntity processDefinition) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode pdrJSON = mapper.createObjectNode();
        pdrJSON.put("id", processDefinition.getId());
        pdrJSON.put("name", processDefinition.getName());
        pdrJSON.put("key", processDefinition.getKey());
        pdrJSON.put("version", processDefinition.getVersion());
        pdrJSON.put("deploymentId", processDefinition.getDeploymentId());
        pdrJSON.put("isGraphicNotationDefined",
                isGraphicNotationDefined(processDefinition));

        return pdrJSON;
    }

    private boolean isGraphicNotationDefined(
            ProcessDefinitionEntity processDefinition) {
        return ((ProcessDefinitionEntity) repositoryService
                .getDeployedProcessDefinition(processDefinition.getId()))
                .isGraphicalNotationDefined();
    }

    // ~ ======================================================================
    public ObjectNode getHighlighted() {
        if (processInstanceId == null) {
            throw new ActivitiException("No process instance id provided");
        }

        ObjectNode responseJSON = new ObjectMapper().createObjectNode();

        responseJSON.put("processInstanceId", processInstanceId);

        ArrayNode activitiesArray = new ObjectMapper().createArrayNode();
        ArrayNode flowsArray = new ObjectMapper().createArrayNode();

        try {
            processInstance = runtimeService.createProcessInstanceQuery()
                    .processInstanceId(processInstanceId).singleResult();
            processDefinition = (ProcessDefinitionEntity) repositoryService
                    .getDeployedProcessDefinition(processInstance
                            .getProcessDefinitionId());

            responseJSON.put("processDefinitionId",
                    processInstance.getProcessDefinitionId());

            highLightedActivities = runtimeService
                    .getActiveActivityIds(processInstanceId);

            List<String> highLightedFlows = getHighLightedFlows(
                    processDefinition, processInstanceId);

            for (String activityId : highLightedActivities) {
                activitiesArray.add(activityId);
            }

            for (String flow : highLightedFlows) {
                flowsArray.add(flow);
            }

            for (String activityId : highLightedActivities) {
                Execution execution = runtimeService
                        .createExecutionQuery()
                        .processInstanceId(
                                processInstance.getProcessInstanceId())
                        .activityId(activityId).singleResult();
                ExecutionEntity executionEntity = (ExecutionEntity) execution;
                executionEntity.getProcessDefinitionId();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        responseJSON.set("activities", activitiesArray);
        responseJSON.set("flows", flowsArray);

        return responseJSON;
    }

    // TODO: move this method to some 'utils'
    private List<String> getHighLightedFlows(
            ProcessDefinitionEntity processDefinition, String processInstanceId) {
        List<HistoricActivityInstance> historicActivityInstances = historyService
                .createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceId)
                .orderByHistoricActivityInstanceStartTime().asc() /* .orderByActivityId().asc() */
                .list();

        for (HistoricActivityInstance hai : historicActivityInstances) {
            historicActivityInstanceList.add(hai.getActivityId());
        }

        // add current activities to list
        highLightedActivities = runtimeService
                .getActiveActivityIds(processInstanceId);
        historicActivityInstanceList.addAll(highLightedActivities);

        // activities and their sequence-flows
        getHighlightedFlows(processDefinition.getActivities());

        return highLightedFlows;
    }

    private void getHighlightedFlows(List<ActivityImpl> activityList) {
        for (ActivityImpl activity : activityList) {
            if (activity.getProperty("type").equals("subProcess")) {
                // get flows for the subProcess
                getHighlightedFlows(activity.getActivities());
            }

            if (historicActivityInstanceList.contains(activity.getId())) {
                List<PvmTransition> pvmTransitionList = activity
                        .getOutgoingTransitions();

                for (PvmTransition pvmTransition : pvmTransitionList) {
                    String destinationFlowId = pvmTransition.getDestination()
                            .getId();

                    if (historicActivityInstanceList
                            .contains(destinationFlowId)) {
                        highLightedFlows.add(pvmTransition.getId());
                    }
                }
            }
        }
    }

    //供调整单调用(调整页获取流程的审核过程 该方法已被取代)
    @Path("whole")
    @GET
    public List<ActivityDTO> findWhole(
            @QueryParam("processDefinitionId") String processDefinitionId,
            @QueryParam("activityId") String activityId, @QueryParam("isWhole") boolean isWhole) {
        FindNextActivitiesCmd cmd = new FindNextActivitiesCmd(
                processDefinitionId, activityId);
        String str = "";

        str = convertActivityDtos(processDefinitionId, activityId);

        List<ActivityDTO> activityDTOs = new ArrayList<ActivityDTO>();
        ActivityDTO vo = new ActivityDTO();
        if (isWhole == true) {
            int a = str.indexOf("->");
            String strNew = str.substring(a + 2);
            vo.setId("");
            vo.setName(strNew);
            activityDTOs.add(vo);
        } else {
            vo.setId("");
            vo.setName(str);
            activityDTOs.add(vo);
        }
        return activityDTOs;
    }

   //为审批流转中的审批表单实现环节显示(审批页获取流程的审核过程)该方法已被取代
    @Path("getApplyActivity")
    @GET
    @ResponseBody
    @Produces(MediaType.APPLICATION_JSON)
    public String getApplyActivity(@QueryParam("processInstanceId") String processInstanceId){
    	List<TaskInfo> taskInfos = taskInfoManager.findBy("processInstanceId", processInstanceId);
    	String code = "";
    	String processDefinitionId = "";
    	String result = "";
    	if(taskInfos.get(0).getCatalog().equals("start")){
    		code = taskInfos.get(0).getCode();
    		processDefinitionId =  taskInfos.get(0).getProcessDefinitionId();
    		result = this.convertActivityDtos(processDefinitionId, code);
    	}else{
    		for(TaskInfo taskInfo : taskInfos){
    			if(!taskInfo.getCatalog().equals("start")){
    				continue;
    			}else{
    				code = taskInfo.getCode();
    				processDefinitionId = taskInfo.getProcessDefinitionId();
    				result = this.convertActivityDtos(processDefinitionId, code);
    			}
    		}
    	}
    	return result;
    }
    @Path("next")
    @GET
    //申请页获取流程审批环节，已被取代
    public List<ActivityDTO> findNextActivities(
            @QueryParam("processDefinitionId") String processDefinitionId,
            @QueryParam("activityId") String activityId) {
        FindNextActivitiesCmd cmd = new FindNextActivitiesCmd(
                processDefinitionId, activityId);

        return convertActivityDtos(processEngine.getManagementService()
                .executeCommand(cmd));
    }

    public List<ActivityDTO> convertActivityDtos(List<PvmActivity> pvmActivities) {
        List<ActivityDTO> activityDtos = new ArrayList<ActivityDTO>();

        for (PvmActivity pvmActivity : pvmActivities) {
            ActivityDTO activityDto = new ActivityDTO();
            activityDto.setId(pvmActivity.getId());
            activityDto.setName((String) pvmActivity.getProperty("name"));
            activityDtos.add(activityDto);
        }

        return activityDtos;
    }
    
/*
    @Path("previous")
    @GET*/
    /*public List<ActivityDTO> findPreviousActivities(
            @QueryParam("processDefinitionId") String processDefinitionId,
            @QueryParam("activityId") String activityId) {
        FindPreviousActivitiesCmd cmd = new FindPreviousActivitiesCmd(
                processDefinitionId, activityId);

        return convertActivityDtos(processEngine.getManagementService()
                .executeCommand(cmd));
    }*/

    @Path("taskDefinitionKeys")
    @GET
    public List<ActivityDTO> findTaskDefinitionKeys(
            @QueryParam("processDefinitionId") String processDefinitionId) {
        FindTaskDefinitionsCmd cmd = new FindTaskDefinitionsCmd(
                processDefinitionId);

        return this.convertActivityDtoFromTaskDefinitions(processEngine
                .getManagementService().executeCommand(cmd));
    }

    public String convertActivityDtos(String processDefinitionId, String activityId) {//List<PvmActivity> pvmActivities

        String str = "";
        List<DictInfo> dictInfos = dictConnector.findDictInfoListByType("bpmApprovalProcess");
        while (true) {
            FindNextActivitiesCmd cmd = new FindNextActivitiesCmd(processDefinitionId, activityId);

            boolean blnFlag = true;  // 是否添加节点到字符串
            List<PvmActivity> pvmActivities = processEngine.getManagementService().executeCommand(cmd);
            //System.out.println(pvmActivities.get(0).getIncomingTransitions().get(1).getProperty("conditionText"));
            if (pvmActivities.size() == 1) {
                // System.out.print(pvmActivities.get(0).getProperty("name").toString());
                for (DictInfo vo : dictInfos) {
                    String name = vo.getValue();

                    if (pvmActivities.get(0).getProperty("name").toString().equals(name)) { // 判断节点是否在数据字典中
                        blnFlag = false;
                        break;  // 退出循环
                    }
                }

                if (blnFlag) {
                    // 判断字符串是否已就存在节点
                    if (StringUtils.isNoneBlank(str)) {
                        if (str.indexOf(pvmActivities.get(0).getProperty("name").toString() + "√" + pvmActivities.get(0).getId()) == -1) {
                            str += pvmActivities.get(0).getProperty("name").toString() + "√" + pvmActivities.get(0).getId() + "->";
                            activityId = pvmActivities.get(0).getId();
                        } else {
                            break;
                        }
                    } else {
                        str += pvmActivities.get(0).getProperty("name").toString() + "√" + pvmActivities.get(0).getId() + "->";
                        activityId = pvmActivities.get(0).getId();
                    }
                } else {
                    if (StringUtils.isNoneBlank(str)) {
                        break;
                    } else {
                        str += pvmActivities.get(0).getProperty("name").toString() + "√" + pvmActivities.get(0).getId() + "->";
                        activityId = pvmActivities.get(0).getId();
                    }

                }
            }

            if (pvmActivities.size() == 2) {
                for (int j = 0; j < 2; j++) {
                    blnFlag = true;
                    // System.out.println(pvmActivities.get(j).getProperty("name").toString());
                    for (DictInfo vo : dictInfos) {
                        String name = vo.getValue();
                        // System.out.println(name);

                        if (pvmActivities.get(j).getProperty("name").toString().equals(name)) { // 判断节点是否在数据字典中
                            blnFlag = false;
                            break;  // 退出循环
                        }
                    }

                    if (blnFlag) {
                        // 判断字符串是否已就存在节点
                        if (str.indexOf(pvmActivities.get(j).getProperty("name").toString() + "√" + pvmActivities.get(j).getId()) == -1) {
                            str += pvmActivities.get(j).getProperty("name").toString() + "√" + pvmActivities.get(j).getId() + "->";
                            activityId = pvmActivities.get(j).getId();
                        }
                    }
                }
            }

            if (pvmActivities.size() > 2) {
                str = "";
                break;
            }

        }

        if (StringUtils.isNoneBlank(str)) {
            str = str.substring(0, str.length() - 2);
        }

        String[] temp = str.split("->");
        String returnStr = "";
        for (String nodeName : temp) {
            String[] tempNode = nodeName.split("√");
            returnStr += tempNode[0] + "->";
        }

        if (StringUtils.isNoneBlank(returnStr)) {
            returnStr = returnStr.substring(0, returnStr.length() - 2);
        }
        return returnStr;
    }
    
 /*   public String convertActivityDtos(String processDefinitionId, String activityId) {//List<PvmActivity> pvmActivities

        String str ="";
        
        while(true){
        	FindNextActivitiesCmd cmd = new FindNextActivitiesCmd( processDefinitionId, activityId);

        	List<PvmActivity> pvmActivities = processEngine.getManagementService().executeCommand(cmd);
        	//System.out.println(pvmActivities.get(0).getIncomingTransitions().get(1).getProperty("conditionText"));
        	if(pvmActivities.size() == 1 ) {
        		if (str.indexOf(pvmActivities.get(0).getProperty("name").toString())==-1) {
        			str += pvmActivities.get(0).getProperty("name").toString() + "√" + pvmActivities.get(0).getId() + "->";
        			activityId = pvmActivities.get(0).getId();
        		} else {
                	break;
                }
            } 
        	
        	if(pvmActivities.size() == 2){
             	for(int j = 0;j < 2;j++){
             		String [] strNode = str.split("->");
             		//for(int node = strNode.length;node > 0; node--){
             			if(strNode.length == 1){
             				System.out.println(pvmActivities.get(j).getProperty("name").toString());
             				System.out.println(pvmActivities.get(j).getProperty("name").toString().indexOf("调整"));
	             			if(pvmActivities.get(j).getProperty("name").toString().indexOf("调整")==0||
	             					pvmActivities.get(j).getProperty("name").toString().equals("业务客服部")||
	             					pvmActivities.get(j).getProperty("name").toString().indexOf("经理")==0||
	             					pvmActivities.get(j).getProperty("name").toString().indexOf("申请调整人")==0||
	             					pvmActivities.get(j).getProperty("name").toString().indexOf("发起人")==0
	             					){
	             				continue;
	             			}
             			}else if(strNode[strNode.length - 2].equals(pvmActivities.get(j).getProperty("name").toString() + "√" + pvmActivities.get(j).getId())){  // 取当前节点的上一个节点进行比较
             				continue;
             			}else if(strNode[strNode.length - 2] == strNode[0]){//用于调整页面判断
             				if(pvmActivities.get(j).getProperty("name").toString().indexOf("调整")==0||
             						pvmActivities.get(j).getProperty("name").toString().indexOf("发起人")==0||
             						pvmActivities.get(j).getProperty("name").toString().indexOf("申请")==0||
             						pvmActivities.get(j).getProperty("name").toString().indexOf("经理")==0||
             						pvmActivities.get(j).getProperty("name").toString().equals("业务客服部")
             						){
             					continue;
             				}
             			}
             			str += pvmActivities.get(j).getProperty("name").toString() + "√" + pvmActivities.get(j).getId() + "->";
             			activityId = pvmActivities.get(j).getId();
             			break;
             		
             	}
            }
        	if(pvmActivities.size() > 2){
         		str = "";
         		break;
         	}
        	
        }
        if (StringUtils.isNoneBlank(str)) {
        	str = str.substring(0, str.length()-2);
        }
        
        String[] temp = str.split("->");
        String returnStr = "";
        for (String nodeName : temp) {
        	String[] tempNode = nodeName.split("√");
        	returnStr += tempNode[0] + "->";
        }
        
        if (StringUtils.isNoneBlank(returnStr)) {
        	returnStr = returnStr.substring(0, returnStr.length()-2);
        }
        return returnStr;
    }*/


    public List<ActivityDTO> convertActivityDtoFromTaskDefinitions(
            List<TaskDefinition> taskDefinitions) {
        List<ActivityDTO> activityDtos = new ArrayList<ActivityDTO>();

        for (TaskDefinition taskDefinition : taskDefinitions) {
            ActivityDTO activityDto = new ActivityDTO();
            activityDto.setId(taskDefinition.getKey());
            activityDto.setName(taskDefinition.getNameExpression()
                    .getExpressionText());
            activityDtos.add(activityDto);
        }

        return activityDtos;
    }

    /**
     * 根据一级业务类型得到二级业务类型
     * lilei at 2017.09.29
     **/
    @POST
    @Path("bussiness-detail")
    @Log(desc = "查询审批的业务类型（二级）", action = "search", operationDesc = "PC-审批-查询审批的业务类型（二级）")
    public Map<String, Object> GetSecondBusinessType(@FormParam("strBusType") String strBusType) {
        Map<String, Object> returnMap = new HashMap<String, Object>();
        try {
            String tenantid = tenantHolder.getTenantId();

            String hlSql = "from BusinessDetailEntity where typeId=? and tenantId=? and enable='是'";
            List<BusinessDetailEntity> entityList = businessDetailManager.find(hlSql, Long.valueOf(strBusType), tenantid);
            List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
            if (entityList != null && entityList.size() > 0) {
                for (BusinessDetailEntity entity : entityList) {
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("intBDID", entity.getId());
                    map.put("varDetails", entity.getBusiDetail());
                    list.add(map);
                }
                returnMap.put("bSuccess", "true");
                returnMap.put("strMsg", "加载成功");
                returnMap.put("BussinessDetails", list);
            } else {
                returnMap.put("bSuccess", "true");
                returnMap.put("strMsg", "获取成功");
            }


        } catch (Exception ex) {
            returnMap.put("bSuccess", "false");
            returnMap.put("strMsg", "加载错误，请联系管理员");
            logger.debug("手机APP--审批-查询审批的业务类型（二级）-查询异常："
                    + ex.getMessage() + "\r\n" + ex.getStackTrace());
        }
        return returnMap;
    }

    /**
     * 根据一级业务类型得到二级业务类型
     * lilei at 2017.09.29
     **/
    @POST
    @Path("company-info")
    @Log(desc = "根据大区加载分公司数据", action = "search", operationDesc = "手机APP-审批-根据大区加载分公司数据")
    public Map<String, Object> GetCompanyByArea(@FormParam("strAreaID") String strAreaID) {
        Map<String, Object> returnMap = new HashMap<String, Object>();
        try {
            String tenantid = tenantHolder.getTenantId();

			/*String sqlString="SELECT e.* FROM PARTY_STRUCT s "
                    +"INNER JOIN PARTY_ENTITY e on s.CHILD_ENTITY_ID=e.ID "
					+"where e.TYPE_ID=2 and s.PARENT_ENTITY_ID="+strAreaID;*/

            PartyDTO partyDTO = partyConnector.findCompanyById(currentUserHolder.getUserId());

            String sqlString = "SELECT e.* FROM PARTY_STRUCT s "
                    + "INNER JOIN PARTY_ENTITY e on s.CHILD_ENTITY_ID=e.ID "
                    + "where e.TYPE_ID=2 and s.PARENT_ENTITY_ID=" + strAreaID;

            List<Map<String, Object>> maplist = jdbcTemplate.queryForList(sqlString);

            if (partyDTO != null && maplist != null && maplist.size() > 0) {
                for (Map<String, Object> entity : maplist) {
                    if (entity.get("ID").equals(partyDTO.getId())) {
                        maplist.clear();
                        Map<String, Object> mapNew = new HashMap<String, Object>();
                        mapNew.put("ID", partyDTO.getId());
                        mapNew.put("Name", partyDTO.getName());
                        maplist.add(mapNew);
                    }
                }
            }
            if (maplist != null && maplist.size() > 0) {
                returnMap.put("bSuccess", "true");
                returnMap.put("strMsg", "加载成功");
                returnMap.put("companylist", maplist);
            } else {
                returnMap.put("bSuccess", "true");
                returnMap.put("strMsg", "获取成功");
            }


        } catch (Exception ex) {
            returnMap.put("bSuccess", "false");
            returnMap.put("strMsg", "加载错误，请联系管理员");
            logger.debug("手机APP--审批-查询审批的业务类型（二级）-查询异常："
                    + ex.getMessage() + "\r\n" + ex.getStackTrace());
        }
        return returnMap;
    }

    public static class Process {
        String processDefinitionId;
        String activityId;

        public String getProcessDefinitionId() {
            return processDefinitionId;
        }

        public void setProcessDefinitionId(String processDefinitionId) {
            this.processDefinitionId = processDefinitionId;
        }

        public String getActivityId() {
            return activityId;
        }

        public void setActivityId(String activityId) {
            this.activityId = activityId;
        }

    }

    @Resource
    public void setNotificationConnector(NotificationConnector notificationConnector) {
        this.notificationConnector = notificationConnector;
    }

    @Resource
    public void setTaskInfoManager(TaskInfoManager taskInfoManager) {
        this.taskInfoManager = taskInfoManager;
    }

    @Resource
    public void setCustomManager(CustomManager customManager) {
        this.customManager = customManager;
    }
    @Resource
    public void setInternalProcessConnector(InternalProcessConnector internalProcessConnector) {
        this.internalProcessConnector = internalProcessConnector;
    }

    @Resource
    public void setKeyValueConnector(KeyValueConnector keyValueConnector) {
        this.keyValueConnector = keyValueConnector;
    }
    @Resource
    public void setHumanTaskConnector(HumanTaskConnector humanTaskConnector) {
        this.humanTaskConnector = humanTaskConnector;
    }

    @Resource
    public void setProcessEngine(ProcessEngine processEngine) {
        this.processEngine = processEngine;
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
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Resource
    public void setPartyConnector(PartyConnector partyConnector) {
        this.partyConnector = partyConnector;
    }

    @Resource
    public void setCurrentUserHolder(CurrentUserHolder currentUserHolder) {
        this.currentUserHolder = currentUserHolder;
    }
}
