package com.mossle.bpm.service;

import java.util.List;

import javax.annotation.Resource;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mossle.bpm.persistence.domain.BpmConfBase;
import com.mossle.bpm.persistence.domain.BpmConfForm;
import com.mossle.bpm.persistence.domain.BpmConfNode;
import com.mossle.bpm.persistence.manager.BpmConfBaseManager;
import com.mossle.bpm.persistence.manager.BpmConfFormManager;
import com.mossle.bpm.persistence.manager.BpmConfNodeManager;
import com.mossle.humantask.persistence.domain.TaskDefBase;
import com.mossle.humantask.persistence.manager.TaskDefBaseManager;
import com.mossle.spi.humantask.TaskDefinitionConnector;


@Service
@Transactional(readOnly=true)
public class BpmConfFormService {
	private static org.slf4j.Logger logger = LoggerFactory.getLogger(BpmConfFormService.class);
	private BpmConfFormManager bpmConfFormManager;
	private BpmConfNodeManager bpmConfNodeManager;
	private TaskDefinitionConnector taskDefinitionConnector;
	private BpmConfBaseManager bpmConfBaseManager;
	private TaskDefBaseManager taskDefBaseManager;
	@Transactional(readOnly=false)
	public void batchSaveForm(String bpmConfBaseId,String formValue,Long[] checkNodes){
		String codes = "";
		//先删除已经配置的节点
    	String hql = "delete from BpmConfForm where bpmConfNode.id=?";
    	for(Long node : checkNodes){
    		bpmConfFormManager.batchUpdate(hql, node);
    		BpmConfNode bpmConfNode = bpmConfNodeManager.get(node);
    		String code = bpmConfNode.getCode();
    		codes += "'"+code+"',";
    		BpmConfForm bpmConfForm = new BpmConfForm();
    		bpmConfForm.setValue(formValue);
    		bpmConfForm.setType(1);
    		bpmConfForm.setStatus(1);
    		bpmConfForm.setBpmConfNode(bpmConfNode);
    		bpmConfFormManager.save(bpmConfForm);
    	}
    	codes = codes.substring(0, codes.length()-1);
    	
		BpmConfBase bpmConfBase = bpmConfBaseManager.get(Long.parseLong(bpmConfBaseId));
		
		if(bpmConfBase != null){
			String processDefinitionId = bpmConfBase.getProcessDefinitionId();
			String hqlTaskDelBase = "from TaskDefBase where processDefinitionId=? and code in("+codes+")";
			List<TaskDefBase> taskDefBaseList = taskDefBaseManager.find(hqlTaskDelBase, processDefinitionId);
			logger.debug("批量配置表单，查询task_def_base表结果"+taskDefBaseList.size());
			for(TaskDefBase taskDefBase : taskDefBaseList){
				taskDefBase.setFormKey(formValue);
				taskDefBase.setFormType("external");
				taskDefBaseManager.save(taskDefBase);
			}
		}else{
			logger.error("批量配置表单操作:bpmConfBase对象为null");
		}
		
	}
	//------------------------------------------------------------------------------
	@Resource
    public void setBpmConfNodeManager(BpmConfNodeManager bpmConfNodeManager) {
        this.bpmConfNodeManager = bpmConfNodeManager;
    }
    @Resource
    public void setBpmConfFormManager(BpmConfFormManager bpmConfFormManager) {
        this.bpmConfFormManager = bpmConfFormManager;
    }
    @Resource
    public void setTaskDefinitionConnector(TaskDefinitionConnector taskDefinitionConnector) {
    	this.taskDefinitionConnector = taskDefinitionConnector;
    }
    @Resource
    public void setBpmConfBaseManager(BpmConfBaseManager bpmConfBaseManager) {
    	this.bpmConfBaseManager = bpmConfBaseManager;
    }
    @Resource
    public void setTaskDefBaseManager(TaskDefBaseManager taskDefBaseManager) {
    	this.taskDefBaseManager = taskDefBaseManager;
    }
}
