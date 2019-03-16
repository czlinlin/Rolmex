package com.mossle.workcenter.persistence.domain;

import org.activiti.engine.impl.transformer.StringToInteger;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by wanghan on 2017\8\9 0009.
 *
 * 任务实体类
 */
@Entity
@Table(name = "RO_TK_TASKINFO")
public class MissionInfo implements java.io.Serializable {
    private static final long serialVersionUID = 0L;

    /**
     * 流水号
     */
    private Long id;

    /**
     * 编号
     */
    private String code;

    /**
     * 标题
     */
    private String title;

    /**
     * 上级任务编号
     */
    private long uppercode;

    /**
     * 内容描述
     */
    private String content;

    /**
     * 负责人编号/部门编号
     */
    private Long leader;

    /**
     * 开始时间
     */
    @DateTimeFormat(pattern ="yyyy-MM-dd HH:mm:ss")
    private Date starttime;

    /**
     * 计划完成时间
     */
    @DateTimeFormat(pattern ="yyyy-MM-dd HH:mm:ss")
    private Date plantime;

    /**
     * 工作量
     */
    private int workload;

    /**
     * 任务状态
     */
    private String status;

    /**
     * 数据状态
     */
    private String datastatus;

    /**
     * 任务类型
     */
    private String tasktype;

    /**
     * 提交或关闭时间
     */
    private Date committime;

    /**
     * 完成效率
     */
    private String efficiency;

    /**
     * 提前或延期小时数
     */
    private int hoursnum;

    /**
     * 发布人编号
     */
    private Long publisher;

    /**
     * 发布时间
     */
    private Date publishtime;

    /**
     * 备注
     */
    private String remarks;

    /**
     * 完成评价情况
     */
    private String evaluate;

    /**
     * 附件
     */
    private String annex;

    public MissionInfo() {
    }

    public MissionInfo(Long id) {
        this.id = id;
    }

    public MissionInfo(Long id, String code, String title, Long uppercode, String content, Long leader,
                       Date starttime, Date plantime, int workload, String status, String datastatus,
                       String tasktype, Date committime, String efficiency, int hoursnum, Long publisher,
                       Date publishtime, String remarks, String evaluate, String annex) {
        this.id = id;
        this.code = code;
        this.title = title;
        this.uppercode = uppercode;
        this.content = content;
        this.leader = leader;
        this.starttime = starttime;
        this.plantime = plantime;
        this.workload = workload;
        this.status = status;
        this.datastatus = datastatus;
        this.tasktype = tasktype;
        this.committime = committime;
        this.efficiency = efficiency;
        this.hoursnum = hoursnum;
        this.publisher = publisher;
        this.publishtime = publishtime;
        this.remarks = remarks;
        this.evaluate = evaluate;
        this.annex = annex;
    }

    @Id
    @Column(name = "ID", unique = true, nullable = false, length = 16)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "CODE",unique = true)
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Column(name = "TITLE", length = 100)
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Column(name = "UPPERCODE", length = 16)
    public Long getUppercode() {
        return uppercode;
    }

    public void setUppercode(Long uppercode) {
        this.uppercode = uppercode;
    }

    @Column(name = "CONTENT", length = 5000)
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Column(name = "LEADER")
    public Long getLeader() {
        return leader;
    }

    public void setLeader(Long leader) {
        this.leader = leader;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "STARTTIME")
    public Date getStarttime() {
        return starttime;
    }

    public void setStarttime(Date starttime) {
        this.starttime = starttime;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "PLANTIME")
    public Date getPlantime() {
        return plantime;
    }

    public void setPlantime(Date plantime) {
        this.plantime = plantime;
    }

    @Column(name = "WORKLOAD", length = 11)
    public int getWorkload() {
        return workload;
    }

    public void setWorkload(int workload) {
        this.workload = workload;
    }

    @Column(name = "STATUS", length = 1)
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Column(name = "DATASTATUS", length = 1)
    public String getDatastatus() {
        return datastatus;
    }

    public void setDatastatus(String datastatus) {
        this.datastatus = datastatus;
    }

    @Column(name = "TASKTYPE", length = 1)
    public String getTasktype() {
        return tasktype;
    }

    public void setTasktype(String tasktype) {
        this.tasktype = tasktype;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "COMMITTIME")
    public Date getCommittime() {
        return committime;
    }

    public void setCommittime(Date committime) {
        this.committime = committime;
    }

    @Column(name = "EFFICIENCY", length = 1)
    public String getEfficiency() {
        return efficiency;
    }

    public void setEfficiency(String efficiency) {
        this.efficiency = efficiency;
    }

    @Column(name = "HOURSNUM", length = 11)
    public int getHoursnum() {
        return hoursnum;
    }

    public void setHoursnum(int hoursnum) {
        this.hoursnum = hoursnum;
    }

    @Column(name = "PUBLISHER")
    public Long getPublisher() {
        return publisher;
    }

    public void setPublisher(Long publisher) {
        this.publisher = publisher;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "PUBLISHTIME")
    public Date getPublishtime() {
        return publishtime;
    }

    public void setPublishtime(Date publishtime) {
        this.publishtime = publishtime;
    }

    @Column(name = "REMARKS", length = 5000)
    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    @Column(name = "EVALUATE", length = 1000)
    public String getEvaluate() {
        return evaluate;
    }

    public void setEvaluate(String evaluate) {
        this.evaluate = evaluate;
    }

    @Column(name = "ANNEX", length = 2000)
    public String getAnnex() {
        return annex;
    }

    public void setAnnex(String annex) {
        this.annex = annex;
    }


}
