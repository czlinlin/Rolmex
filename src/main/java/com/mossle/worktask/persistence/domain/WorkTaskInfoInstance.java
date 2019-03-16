package com.mossle.worktask.persistence.domain;

import java.util.Date;

/**
 * Created by wanghan on 2017\12\1 0001.
 * 任务导出信息存放类
 */
public class WorkTaskInfoInstance {
    /** 标题 */
    private String title;
    /** 负责人 */
    private String leaderName;
    /** 发布人 */
    private String publisherName;
    /** 状态 */
    private String statusName;
    /** 计划开始时间 */
    private Date starttime;
    /** 计划完成时间 */
    private Date plantime;
    /** 实际开始时间 */
    private Date exectime;
    /** 完成或关闭时间 */
    private Date committime;
    /** 效率 */
    private String efficiency;
    /** 评级 */
    private Integer evalscore;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLeaderName() {
        return leaderName;
    }

    public void setLeaderName(String leaderName) {
        this.leaderName = leaderName;
    }

    public String getPublisherName() {
        return publisherName;
    }

    public void setPublisherName(String publisherName) {
        this.publisherName = publisherName;
    }

    public String getStatusName() {
        return statusName;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }

    public Date getStarttime() {
        return starttime;
    }

    public void setStarttime(Date starttime) {
        this.starttime = starttime;
    }

    public Date getPlantime() {
        return plantime;
    }

    public void setPlantime(Date plantime) {
        this.plantime = plantime;
    }

    public Date getExectime() {
        return exectime;
    }

    public void setExectime(Date exectime) {
        this.exectime = exectime;
    }

    public Date getCommittime() {
        return committime;
    }

    public void setCommittime(Date committime) {
        this.committime = committime;
    }

    public String getEfficiency() {
        return efficiency;
    }

    public void setEfficiency(String efficiency) {
        this.efficiency = efficiency;
    }

    public Integer getEvalscore() {
        return evalscore;
    }

    public void setEvalscore(Integer evalscore) {
        this.evalscore = evalscore;
    }
}
