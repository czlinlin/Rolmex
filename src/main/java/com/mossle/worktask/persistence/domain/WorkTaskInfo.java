package com.mossle.worktask.persistence.domain;

import com.mossle.internal.store.persistence.domain.StoreInfo;
import com.mossle.project.persistence.domain.WorkProjectTaskbind;
import com.mossle.project.persistence.manager.WorkProjectTaskbindManager;
import org.hibernate.annotations.Persister;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Created by wanghan on 2017\8\30 0030.
 * 任务信息实体类
 */
@Entity
@Table(name = "work_task_info")
public class WorkTaskInfo {
    private Long id;
    private String code;
    private String title;
    private Long uppercode;
    private String content;
    private Long leader;
    private Date starttime;
    private Date plantime;
    private Integer workload;
    private String status;
    private String datastatus;
    private String tasktype;
    private Date committime;
    private String efficiency;
    private Integer hoursnum;
    private Long publisher;
    private Date publishtime;
    private String remarks;
    private String evaluate;
    /**
     * 提交任务的备注
     * **/
    private String annex;
    /*评价分数*/
    private Integer evalscore;
    /*评价时间*/
    private Date evaltime;
    /*执行时间*/
    private Date exectime;

	/*父子任务显示*/
    private int parentshow;
    private int childshow;

    /*任务详情抄送人显示*/
    private String ccshow;

    //项目任务绑定
    private WorkProjectTaskbind workProjectTaskbind;

    //任务下的子任务显示
    List<WorkTaskInfo> workChildTaskInfoList;

    //任务附件
    List<StoreInfo> storeInfos;
    
    List<StoreInfo> storeSubmitInfos;
    
    @Transient
    public List<StoreInfo> getStoreSubmitInfos() {
		return storeSubmitInfos;
	}

	public void setStoreSubmitInfos(List<StoreInfo> storeSubmitInfos) {
		this.storeSubmitInfos = storeSubmitInfos;
	}

	@Transient
    public List<StoreInfo> getStoreInfos() {
        return storeInfos;
    }

    public void setStoreInfos(List<StoreInfo> storeInfos) {
        this.storeInfos = storeInfos;
    }

    //任务个数
    private int tasknum;

    @Transient
    public int getTasknum() {
        return tasknum;
    }

    public void setTasknum(int tasknum) {
        this.tasknum = tasknum;
    }

    @Transient
    public List<WorkTaskInfo> getWorkChildTaskInfoList() {
        return workChildTaskInfoList;
    }

    public void setWorkChildTaskInfoList(List<WorkTaskInfo> workChildTaskInfoList) {
        this.workChildTaskInfoList = workChildTaskInfoList;
    }

    @OneToOne(mappedBy = "workTaskInfo")
    public WorkProjectTaskbind getWorkProjectTaskbind() {
        return workProjectTaskbind;
    }

    public void setWorkProjectTaskbind(WorkProjectTaskbind workProjectTaskbind) {
        this.workProjectTaskbind = workProjectTaskbind;
    }


    @Transient
    public int getParentshow() {
        return parentshow;
    }

    public void setParentshow(int parentshow) {
        this.parentshow = parentshow;
    }

    @Transient
    public int getChildshow() {
        return childshow;
    }

    public void setChildshow(int childshow) {
        this.childshow = childshow;
    }

    @Transient
    public String getCcshow() {
        return ccshow;
    }

    public void setCcshow(String ccshow) {
        this.ccshow = ccshow;
    }

    private Set<WorkTaskCc> workTaskCcs;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "workTaskInfo")
    public Set<WorkTaskCc> getWorkTaskCcs() {
        return workTaskCcs;
    }

    public void setWorkTaskCcs(Set<WorkTaskCc> workTaskCcs) {
        this.workTaskCcs = workTaskCcs;
    }


    @Id
    @Column(name = "id")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    @Column(name = "code")
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }


    @Column(name = "title")
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    @Column(name = "uppercode")
    public Long getUppercode() {
        return uppercode;
    }

    public void setUppercode(Long uppercode) {
        this.uppercode = uppercode;
    }


    @Column(name = "content")
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }


    @Column(name = "leader")
    public Long getLeader() {
        return leader;
    }

    public void setLeader(Long leader) {
        this.leader = leader;
    }


    @Column(name = "starttime")
    public Date getStarttime() {
        return starttime;
    }

    public void setStarttime(Date starttime) {
        this.starttime = starttime;
    }

    @Column(name = "plantime")
    public Date getPlantime() {
        return plantime;
    }

    public void setPlantime(Date plantime) {
        this.plantime = plantime;
    }


    @Column(name = "workload")
    public Integer getWorkload() {
        return workload;
    }

    public void setWorkload(Integer workload) {
        this.workload = workload;
    }

    @Column(name = "status")
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Column(name = "datastatus")
    public String getDatastatus() {
        return datastatus;
    }

    public void setDatastatus(String datastatus) {
        this.datastatus = datastatus;
    }

    @Column(name = "tasktype")
    public String getTasktype() {
        return tasktype;
    }

    public void setTasktype(String tasktype) {
        this.tasktype = tasktype;
    }

    @Column(name = "committime")
    public Date getCommittime() {
        return committime;
    }

    public void setCommittime(Date committime) {
        this.committime = committime;
    }

    @Column(name = "efficiency")
    public String getEfficiency() {
        return efficiency;
    }

    public void setEfficiency(String efficiency) {
        this.efficiency = efficiency;
    }

    @Column(name = "hoursnum")
    public Integer getHoursnum() {
        return hoursnum;
    }

    public void setHoursnum(Integer hoursnum) {
        this.hoursnum = hoursnum;
    }

    @Column(name = "publisher")
    public Long getPublisher() {
        return publisher;
    }

    public void setPublisher(Long publisher) {
        this.publisher = publisher;
    }

    @Column(name = "publishtime")
    public Date getPublishtime() {
        return publishtime;
    }

    public void setPublishtime(Date publishtime) {
        this.publishtime = publishtime;
    }


    @Column(name = "remarks")
    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    @Column(name = "evaluate")
    public String getEvaluate() {
        return evaluate;
    }

    public void setEvaluate(String evaluate) {
        this.evaluate = evaluate;
    }

    @Column(name = "annex")
    public String getAnnex() {
        return annex;
    }

    public void setAnnex(String annex) {
        this.annex = annex;
    }

    @Column(name = "evalscore")
    public Integer getEvalscore() {
        return evalscore;
    }

    public void setEvalscore(Integer evalscore) {
        this.evalscore = evalscore;
    }

    @Column(name = "evaltime")
    public Date getEvaltime() {
        return evaltime;
    }

    public void setEvaltime(Date evaltime) {
        this.evaltime = evaltime;
    }
    
    @Column(name = "exectime")
    public Date getExectime() {
		return exectime;
	}

	public void setExectime(Date exectime) {
		this.exectime = exectime;
	}


}
