package com.mossle.pim.persistence.domain;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;

/**
 * Created by wanghan on 2017\8\16 0016.
 * 汇报信息 实体类
 */
@Entity
@Table(name = "work_report_info")
public class WorkReportInfo {
    private Long id;
    private String code;
    private String type;
    private String title;
    private String completed;
    private String dealing;
    private String coordinate;
    private String problems;
    private String datastatus;
    private String status;
    private Long sendee;
    private String remarks;
    private String other;
    private String feedback;
    private Long userId;
    private Date reportDate;
    private Date feedbacktime;
    private Date lastedittime;

    private String showCcStatus;
    
    private String showCcType;

    private String showAttachment;

    @Transient
    public String getShowAttachment() {
        return showAttachment;
    }

    public void setShowAttachment(String showAttachment) {
        this.showAttachment = showAttachment;
    }

    @Transient
    public String getShowCcStatus() {
        return showCcStatus;
    }

    public void setShowCcStatus(String showCcStatus) {
        this.showCcStatus = showCcStatus;
    }
    
    @Transient
    public String getShowCcType() {
		return showCcType;
	}

	public void setShowCcType(String showCcType) {
		this.showCcType = showCcType;
	}

	private WorkReportAttachment workReportAttachment;


    private Set<WorkReportForward> workReportForwards;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "workReportInfo")
    public Set<WorkReportForward> getWorkReportForwards() {
        return workReportForwards;
    }

    public void setWorkReportForwards(Set<WorkReportForward> workReportForwards) {
        this.workReportForwards = workReportForwards;
    }


    private Set<WorkReportCc> workReportCcs;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "workReportInfo")
    public Set<WorkReportCc> getWorkReportCcs() {
        return workReportCcs;
    }

    public void setWorkReportCcs(Set<WorkReportCc> workReportCcs) {
        this.workReportCcs = workReportCcs;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)//id的生成策略 IDENTITY是数据库自己设置
    @Column(name = "ID")
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


    @Column(name = "type")
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


    @Column(name = "title")
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    @Column(name = "completed")
    public String getCompleted() {
        return completed;
    }

    public void setCompleted(String completed) {
        this.completed = completed;
    }


    @Column(name = "dealing")
    public String getDealing() {
        return dealing;
    }

    public void setDealing(String dealing) {
        this.dealing = dealing;
    }


    @Column(name = "coordinate")
    public String getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(String coordinate) {
        this.coordinate = coordinate;
    }


    @Column(name = "problems")
    public String getProblems() {
        return problems;
    }

    public void setProblems(String problems) {
        this.problems = problems;
    }


    @Column(name = "datastatus")
    public String getDatastatus() {
        return datastatus;
    }

    public void setDatastatus(String datastatus) {
        this.datastatus = datastatus;
    }


    @Column(name = "status")
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    @Column(name = "sendee")
    public Long getSendee() {
        return sendee;
    }

    public void setSendee(Long sendee) {
        this.sendee = sendee;
    }

    @Column(name = "remarks")
    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }


    @Column(name = "other")
    public String getOther() {
        return other;
    }

    public void setOther(String other) {
        this.other = other;
    }


    @Column(name = "feedback")
    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }


    @Column(name = "USER_ID")
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }


    @Column(name = "report_date")
    public Date getReportDate() {
        return reportDate;
    }

    public void setReportDate(Date reportDate) {
        this.reportDate = reportDate;
    }


    @Column(name = "feedbacktime")
    public Date getFeedbacktime() {
        return feedbacktime;
    }

    public void setFeedbacktime(Date feedbacktime) {
        this.feedbacktime = feedbacktime;
    }


    @Column(name = "lastedittime")
    public Date getLastedittime() {
        return lastedittime;
    }

    public void setLastedittime(Date lastedittime) {
        this.lastedittime = lastedittime;
    }

    @OneToOne(mappedBy = "workReportInfo")
    public WorkReportAttachment getWorkReportAttachment() {
        return workReportAttachment;
    }

    public void setWorkReportAttachment(WorkReportAttachment workReportAttachment) {
        this.workReportAttachment = workReportAttachment;
    }
}