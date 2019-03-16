package com.mossle.api.humantask;

import java.util.List;
import java.util.Map;

import com.mossle.api.form.FormDTO;
import com.mossle.core.page.Page;
import com.mossle.core.query.PropertyFilter;
import com.mossle.humantask.persistence.domain.TaskInfo;

public interface HumanTaskConnector {
    /**
     * 创建任务.
     */
    HumanTaskDTO createHumanTask();

    /**
     * 删除任务.
     */
    void removeHumanTask(String humanTaskId);

    void removeHumanTaskByTaskId(String taskId);

    void removeHumanTaskByProcessInstanceId(String processInstanceId);

    /**
     * 更新任务.
     */
    HumanTaskDTO saveHumanTask(HumanTaskDTO humanTaskDto);

    HumanTaskDTO saveHumanTask(HumanTaskDTO humanTaskDto,
                               boolean triggerListener);

    /**
     * 保存任务，同时处理参与者.
     */
    HumanTaskDTO saveHumanTaskAndProcess(HumanTaskDTO humanTaskDto);

    /**
     * 完成任务.
     */
    void completeTask(String humanTaskId, String userId, String action,
                      String comment, Map<String, Object> taskParameters);

    /**
     * 领取任务.
     */
    void claimTask(String humanTaskId, String userId);

    /**
     * 释放任务。
     */
    void releaseTask(String humanTaskId, String comment);

    /**
     * 转发任务.
     */
    void transfer(String humanTaskId, String userId, String comment);

    /**
     * 取消转办.
     */
    void cancel(String humanTaskId, String userId, String comment);

    /**
     * 回退，指定节点，重新分配.
     */
    void rollbackActivity(String humanTaskId, String activityId, String comment);

    /**
     * 回退，指定节点，上个执行人.
     */
    void rollbackActivityLast(String humanTaskId, String activityId,
                              String comment);

    /**
     * 回退，指定节点，指定执行人.
     */
    void rollbackActivityAssignee(String humanTaskId, String activityId,
                                  String userId, String comment);

    /**
     * 回退，上个节点，重新分配.
     */
    void rollbackPrevious(String humanTaskId, String comment);

    /**
     * 回退，上个节点，上个执行人.
     */
    void rollbackPreviousLast(String humanTaskId, String comment);

    /**
     * 回退，上个节点，指定执行人.
     */
    void rollbackPreviousAssignee(String humanTaskId, String userId,
                                  String comment);

    /**
     * 回退，开始事件，流程发起人.
     */
    void rollbackStart(String humanTaskId, String comment);

    /**
     * 回退，流程发起人.
     */
    void rollbackInitiator(String humanTaskId, String comment);

    /**
     * 撤销.
     */
    void withdraw(String humanTaskId, String comment);

    /**
     * 协办.
     */
    void delegateTask(String humanTaskId, String userId, String comment);

    /**
     * 协办，链状.
     */
    void delegateTaskCreate(String humanTaskId, String userId, String comment);

    /**
     * 沟通.
     */
    void communicate(String humanTaskId, String userId, String comment);

    /**
     * 反馈.
     */
    void callback(String humanTaskId, String userId, String comment);

    /**
     * 跳过.
     */
    void skip(String humanTaskId, String userId, String comment);

    void saveParticipant(ParticipantDTO participantDto);

    HumanTaskDTO findHumanTaskByTaskId(String taskId);

    List<HumanTaskDTO> findHumanTasksByProcessInstanceId(
            String processInstanceId);
    
    List<HumanTaskDTO> findHumanTasksForPositionByProcessInstanceId(
            String processInstanceId);

    HumanTaskDTO findHumanTask(String humanTaskId);

    List<HumanTaskDTO> findSubTasks(String parentTaskId);

    FormDTO findTaskForm(String humanTaskId);

    List<HumanTaskDefinition> findHumanTaskDefinitions(
            String processDefinitionId);

    void configTaskDefinitions(String businessKey,
                               List<String> taskDefinitionKeys, List<String> taskAssigness);

    /**
     * 待办任务.
     */
    Page findPersonalTasks(String userId, String tenantId, int pageNo,
                           int pageSize);

    /**
     * 待办任务.
     */
    Page findPersonalTasks(String userId, String tenantId,
                           List<PropertyFilter> propertyFilters, Page page);

    /**
     * 待办任务(首页专用).
     */
    Page findPersonalTasksToPortal(String userId, String tenantId,
                           List<PropertyFilter> propertyFilters, Page page);
    
    /**
     * 抄送任务.
     */
    Page findPersonalCopyTasks(String userId, String tenantId,
                               List<PropertyFilter> propertyFilters, Page page,String status);

    /**
     * 导出抄送任务.
     */
    Page exportPersonalCopyTasks(String userId, String tenantId,
                                 List<PropertyFilter> propertyFilters, Page page);

    /**
     * 全部审批  20171106 chengze.
     */
    Page findAllApproval(String userId, String tenantId,
                         List<PropertyFilter> propertyFilters, Page page);

    /**
     * 导出全部审批   wh
     */
    Page exportAllApproval(String userId, String tenantId,
                           List<PropertyFilter> propertyFilters, Page page);

    /**
     * 定制审批  20171106 chengze.
     */
    Page findSpeicalPeopleApproval(String userId, String tenantId,
                                   List<PropertyFilter> propertyFilters, Page page);

    /**
     * 导出定制审批  wh
     */
    Page exportSpeicalPeopleApproval(String userId, String tenantId,
                                     List<PropertyFilter> propertyFilters, Page page);

    /**
     * 部门申请 wh.
     */
    Page findDepartmentApplication(String userId, String tenantId, String departmentId, List<PropertyFilter> propertyFilters, Page page,String checkArea);
    /**
     * 导出部门申请 wh.
     */
    Page exportDepartmentApplication(String userId, String tenantId, String departmentId, List<PropertyFilter> propertyFilters, Page page,String checkArea);

    /**
     * 部门审批 wh.
     */
    Page findDepartmentApproval(String userId, String tenantId, String departmentId, List<PropertyFilter> propertyFilters, Page page,String checkArea);
    /**
     * 导出部门审批 wh.
     */
    Page exportDepartmentApproval(String userId, String tenantId, String departmentId, List<PropertyFilter> propertyFilters, Page page,String checkArea);
    /**
     * 公司申请 wh.
     */
    Page findCompanyApplication(String userId, String tenantId, String companyId, List<PropertyFilter> propertyFilters, Page page,String checkArea);
    /**
     * 导出公司申请 wh.
     */
    Page exportCompanyApplication(String userId, String tenantId, String companyId, List<PropertyFilter> propertyFilters, Page page,String checkArea);

    /**
     * 公司审批 wh.
     */
    Page findCompanyApproval(String userId, String tenantId, String companyId, List<PropertyFilter> propertyFilters, Page page,String checkArea);
    /**
     * 导出公司审批 wh.
     */
    Page exportCompanyApproval(String userId, String tenantId, String companyId, List<PropertyFilter> propertyFilters, Page page,String checkArea);
    /**
     * 已办任务.
     */
    Page findFinishedTasks(String userId, String tenantId, int pageNo,
                           int pageSize);

    /**
     * 已办任务.
     */
    Page findFinishedTasks(String userId, String tenantId,
                           List<PropertyFilter> propertyFilters, Page page);

    /**
     * 导出已办任务.
     */
    Page exportFinishedTasks(String userId, String tenantId,
                             List<PropertyFilter> propertyFilters, Page page);

    /**
     * 待领任务.
     */
    Page findGroupTasks(String userId, String tenantId, int pageNo, int pageSize);

    /**
     * 待领任务.
     */
    Page findGroupTasks(String userId, String tenantId, List<PropertyFilter> propertyFilters, Page page);

    /**
     * 经手任务.
     */
    Page findDelegateTasks(String userId, String tenantId, int pageNo,
                           int pageSize);
    /**
     * 导出已办表单数据
     */
    Page exportFinishedTasksBydetail(String userId, String tenantId, List<PropertyFilter> propertyFilters, Page page,String formName);
    /**
     * 导出抄送表单数据
     * 
     */
    Page exportPersonalCopyTasksBydetail(String userId, String tenantId, List<PropertyFilter> propertyFilters, Page page,String formName);
    /**
     * 导出全部审批的主表数据
     * @author sjx
     * @param userId
     * @param tenantId
     * @param propertyFilters
     * @param page
     * @param formName
     * @return
     */
    Page exportAllApprovalBydetail(String userId, String tenantId, List<PropertyFilter> propertyFilters, Page page,String formName);
    /**
     * 导出定制审批的主表数据
     * @author sjx
     * @param userId
     * @param tenantId
     * @param propertyFilters
     * @param page
     * @param formName
     * @return
     */
    Page exportSpeicalPeopleApprovalBydetail(String userId, String tenantId, List<PropertyFilter> propertyFilters, Page page,String formName);
    /**
     * 导出部门申请--new
     *
     */
	Page exportDepartmentApplicationDetail(String userId, String tenantId,String departmentId, List<PropertyFilter> propertyFilters,Page page, String checkArea, String formName);
	 /**
     * 导出部门审批--new
     *
     */
	Page exportDepartmentApprovalDetail(String userId, String tenantId,String departmentId, List<PropertyFilter> propertyFilters,Page page, String checkArea, String formName);

	 /**
     * 导出公司申请--new
     *
     */
	Page exportCompanyApplicationDetail(String userId, String tenantId,String companyId, List<PropertyFilter> propertyFilters, Page page,String checkArea, String formName);
	/**
     * 导出公司审批--new
     *
     */
	Page exportCompanyApprovalDetail(String userId, String tenantId,String companyId, List<PropertyFilter> propertyFilters, Page page,String checkArea, String formName);
	//流程管理--任务管理
	Page findUserTasks(List<PropertyFilter> propertyFilters, Page page);
	//任务管理--保存或修改流程任务负责人
	public int saveTask(Long taskId,String assignee,String strType,Long positionId);
	/**
	 * @param userId
	 * @param tenantId
	 * @param propertyFilters
	 * @param page
	 * @return
	 * 流程中心--我的审批--管理者
	 */
	Page findManageTasks(String userId, String tenantId,List<PropertyFilter> propertyFilters, Page page);
	
	Page exportManageQuery(String userId, String tenantId,List<PropertyFilter> propertyFilters, Page page);
	Page exportManageQueryBydetail(String userId, String tenantId, List<PropertyFilter> propertyFilters, Page page,String formName);

	//历史小工具的查询
	Page findHistoryTool(String searchUserId,String userId, String tenantId,List<PropertyFilter> propertyFilters, Page page);
	//根据菜单查询数据条数
	long queryTaskCount(String title);
	//环节修改日志查询
	Page findHistoryToolLog(String searchUserId, String userId,String tenantId, List<PropertyFilter> propertyFilters, Page page);
}
