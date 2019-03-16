package com.mossle.bpm.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.mossle.bpm.persistence.domain.BpmConfNode;
import com.mossle.bpm.persistence.domain.BpmConfOperation;
import com.mossle.bpm.persistence.manager.BpmConfNodeManager;
import com.mossle.bpm.persistence.manager.BpmConfOperationManager;
import com.mossle.bpm.persistence.manager.BpmProcessManager;
import com.mossle.core.mapper.BeanMapper;
import com.mossle.spi.humantask.TaskDefinitionConnector;

import org.activiti.engine.ProcessEngine;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("bpm")
public class BpmConfOperationController {
    private BpmConfNodeManager bpmConfNodeManager;
    private BpmConfOperationManager bpmConfOperationManager;
    private BeanMapper beanMapper = new BeanMapper();
    private ProcessEngine processEngine;
    private BpmProcessManager bpmProcessManager;
    private TaskDefinitionConnector taskDefinitionConnector;

    @RequestMapping("bpm-conf-operation-list")
    public String list(@RequestParam("bpmConfNodeId") Long bpmConfNodeId,
            Model model) {
        // List<String> operations = new ArrayList<String>();
        Map<String, String> map = getMap();
        
        /*
        this.addButton("saveDraft", "保存草稿");
        this.addButton("taskConf", "配置任务");
        this.addButton("confirmStartProcess", "提交数据");
        this.addButton("startProcess", "发起流程");
        this.addButton("completeTask", "完成任务");

        this.addButton("claimTask", "认领任务");
        this.addButton("releaseTask", "释放任务");
        this.addButton("transfer", "转办");
        this.addButton("rollback", "退回");
        this.addButton("rollbackPrevious", "回退（上一步）");
        this.addButton("rollbackAssignee", "回退（指定负责人）");
        this.addButton("rollbackActivity", "回退（指定步骤）");
        this.addButton("rollbackActivityAssignee", "退回（指定步骤，指定负责人）");
        this.addButton("rollbackStart", "回退（开始节点）");
        this.addButton("rollbackInitiator", "回退（发起人）");
        this.addButton("delegateTask", "协办");
        this.addButton("delegateTaskCreate", "协办（链式）");
        this.addButton("resolveTask", "还回");
        this.addButton("endProcess", "终止流程");
        this.addButton("suspendProcess", "暂停流程");
        this.addButton("resumeProcess", "恢复流程");
        this.addButton("viewHistory", "查看流程状态");
        this.addButton("addCounterSign", "加签");
        this.addButton("jump", "自由跳转");
        this.addButton("reminder", "催办");
        this.addButton("withdraw", "撤销");

        this.addButton("communicate", "沟通");
        this.addButton("callback", "反馈");
        */
        /*
        operations.add("saveDraft");
        operations.add("completeTask");
        operations.add("rollbackPrevious");
        operations.add("rollbackInitiator");
        operations.add("transfer");
        operations.add("delegateTask");
        operations.add("delegateTaskCreate");
        operations.add("communicate");
        operations.add("callback");
        operations.add("addCounterSign");
		*/
        BpmConfNode bpmConfNode = bpmConfNodeManager.get(bpmConfNodeId);
        Long bpmConfBaseId = bpmConfNode.getBpmConfBase().getId();
        List<BpmConfOperation> bpmConfOperations = bpmConfOperationManager
                .findBy("bpmConfNode", bpmConfNode);

        for (Iterator<Map.Entry<String, String>> iterator = map.entrySet().iterator(); iterator
                .hasNext();) {
        	Map.Entry<String, String> entry = iterator.next();

            for (BpmConfOperation bpmConfOperation : bpmConfOperations) {
                if (entry.getKey().equals(bpmConfOperation.getValue())) {
                    iterator.remove();
                    break;
                }
            }
        }
        

        model.addAttribute("bpmConfBaseId", bpmConfBaseId);
        model.addAttribute("bpmConfOperations", bpmConfOperations);
        // model.addAttribute("operations", operations);
        model.addAttribute("bpmConfBase", bpmConfNode.getBpmConfBase());
        model.addAttribute("bpmConfNode", bpmConfNode);
        model.addAttribute("map", map);
        return "bpm/bpm-conf-operation-list";
    }

	private Map<String, String> getMap() {
		Map<String,String> map = new LinkedHashMap<String, String>();
        
        map.put("saveDraft", "保存草稿");
        map.put("completeTask", "完成任务");
        map.put("rollbackPrevious", "回退（上一步）");
        map.put("rollbackInitiator", "回退（发起人）");
        map.put("transfer", "转办");
        map.put("delegateTask", "协办");
        map.put("delegateTaskCreate", "协办（链式）");
        map.put("communicate", "沟通");
        map.put("callback", "反馈");
        map.put("addCounterSign", "加签");
		return map;
	}

    @RequestMapping("bpm-conf-operation-save")
    public String save(@ModelAttribute BpmConfOperation bpmConfOperation,
            @RequestParam("bpmConfNodeId") Long bpmConfNodeId) {
        if ((bpmConfOperation.getValue() == null)
                || "".equals(bpmConfOperation.getValue())) {
            return "redirect:/bpm/bpm-conf-operation-list.do?bpmConfNodeId="
                    + bpmConfNodeId;
        }
        
        Map<String, String> map = getMap();
        bpmConfOperation.setName(map.get(bpmConfOperation.getValue()));
        bpmConfOperation.setBpmConfNode(bpmConfNodeManager.get(bpmConfNodeId));
        
        bpmConfOperationManager.save(bpmConfOperation);

        BpmConfOperation dest = bpmConfOperation;
        String taskDefinitionKey = dest.getBpmConfNode().getCode();
        String processDefinitionId = dest.getBpmConfNode().getBpmConfBase()
                .getProcessDefinitionId();
        String operation = dest.getValue();
        taskDefinitionConnector.addOperation(taskDefinitionKey,
                processDefinitionId, operation);

        return "redirect:/bpm/bpm-conf-operation-list.do?bpmConfNodeId="
                + bpmConfNodeId;
    }

    @RequestMapping("bpm-conf-operation-remove")
    public String remove(@RequestParam("id") Long id) {
        BpmConfOperation bpmConfOperation = bpmConfOperationManager.get(id);
        Long bpmConfNodeId = bpmConfOperation.getBpmConfNode().getId();
        bpmConfOperationManager.remove(bpmConfOperation);

        BpmConfOperation dest = bpmConfOperation;
        String taskDefinitionKey = dest.getBpmConfNode().getCode();
        String processDefinitionId = dest.getBpmConfNode().getBpmConfBase()
                .getProcessDefinitionId();
        String operation = dest.getValue();
        taskDefinitionConnector.removeOperation(taskDefinitionKey,
                processDefinitionId, operation);

        return "redirect:/bpm/bpm-conf-operation-list.do?bpmConfNodeId="
                + bpmConfNodeId;
    }

    // ~ ======================================================================
    @Resource
    public void setBpmConfNodeManager(BpmConfNodeManager bpmConfNodeManager) {
        this.bpmConfNodeManager = bpmConfNodeManager;
    }

    @Resource
    public void setBpmConfOperationManager(
            BpmConfOperationManager bpmConfOperationManager) {
        this.bpmConfOperationManager = bpmConfOperationManager;
    }

    @Resource
    public void setBpmProcessManager(BpmProcessManager bpmProcessManager) {
        this.bpmProcessManager = bpmProcessManager;
    }

    @Resource
    public void setProcessEngine(ProcessEngine processEngine) {
        this.processEngine = processEngine;
    }

    @Resource
    public void setTaskDefinitionConnector(
            TaskDefinitionConnector taskDefinitionConnector) {
        this.taskDefinitionConnector = taskDefinitionConnector;
    }
}
