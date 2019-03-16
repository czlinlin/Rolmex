package com.mossle.project.persistence.domain;

import java.util.Date;

/**
 * Created by wanghan on 2017\12\4 0004.
 * 项目导出信息存放类
 */
public class WorkProjectInfoInstance {
    /**
     * 标题
     */
    private String title;
    /**
     * 负责人
     */
    private String  leaderName;
    /**
     * 发布人
     */
    private String  publisherName;
    /**
     * 状态
     */
    /* 0：已发布1：进行中 2：已完成 3：已关闭 4：已评价*/
    private String status;
    /**
     * 计划开始日期
     */
    private Date startdate;
    /**
     * 计划完成日期
     */
    private Date plandate;
    /**
     * 实际开始时间
     */
    private Date exectime;
    /**
     * 完成或关闭时间
     */
    private Date committime;
    /**
     * 效率
     */
    /*效率 0：准时 1：提前 2：延期*/
    private String efficiency;
    /**
     * 评级
     */
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getStartdate() {
        return startdate;
    }

    public void setStartdate(Date startdate) {
        this.startdate = startdate;
    }

    public Date getPlandate() {
        return plandate;
    }

    public void setPlandate(Date plandate) {
        this.plandate = plandate;
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
