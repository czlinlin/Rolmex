package com.mossle.project.persistence.domain;

import com.mossle.worktask.persistence.domain.WorkTaskCc;
import com.mossle.worktask.persistence.domain.WorkTaskInfo;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Created by wanghan on 2017\9\9 0009.
 */
@Entity
@Table(name = "work_project_info")
public class WorkProjectInfo {
    private Long id;
    private String code;
    private String title;
    private String content;
    private Long leader;
    private Date startdate;
    private Date plandate;
    /*状态 0：已发布1：进行中 2：已完成 3：已关闭 4：已评价*/
    private String status;
    /*数据状态 0:草稿 1：发布 2：删除*/
    private String datastatus;
    private Date committime;
    /*效率 0：准时 1：提前 2：延期*/
    private String efficiency;
    private Integer hoursnum;
    private Long publisher;
    private Date publishtime;
    private String remarks;
    private String evaluate;
    private String annex;
    private Integer evalscore;
    private Date evaltime;
    private Date exectime;

    //知会人
    private Set<WorkProjectNotify> workProjectNotifies;

    //项目绑定任务
    private Set<WorkProjectTaskbind> workProjectTaskbinds;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "workProjectInfo")
    public Set<WorkProjectTaskbind> getWorkProjectTaskbind() {
        return workProjectTaskbinds;
    }

    public void setWorkProjectTaskbind(Set<WorkProjectTaskbind> workProjectTaskbinds) {
        this.workProjectTaskbinds = workProjectTaskbinds;
    }


    @OneToMany(fetch = FetchType.LAZY, mappedBy = "workProjectInfo")
    public Set<WorkProjectNotify> getWorkProjectNotifies() {
        return workProjectNotifies;
    }

    public void setWorkProjectNotifies(Set<WorkProjectNotify> workProjectNotifies) {
        this.workProjectNotifies = workProjectNotifies;
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

    @Column(name = "startdate")
    public Date getStartdate() {
        return startdate;
    }

    public void setStartdate(Date startdate) {
        this.startdate = startdate;
    }

    @Column(name = "plandate")
    public Date getPlandate() {
        return plandate;
    }

    public void setPlandate(Date plandate) {
        this.plandate = plandate;
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
