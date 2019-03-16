package com.mossle.pim.persistence.domain;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;

@Entity
@Table(name = "v_work_report_cc")
public class VWorkReportCcEntity {
    private long id;
    private Long ccno;
    private String ccStatus;
    private Timestamp readtime;
    private Long infoId;
    private String code;
    private String type;
    private String title;
    private String completed;
    private String dealing;
    private String coordinate;
    private String problems;
    private String datastatus;
    private String infoStatus;
    private Long sendee;
    private String remarks;
    private String other;
    private String feedback;
    private Long userId;
    private Timestamp reportDate;
    private Timestamp feedbacktime;
    private Timestamp lastedittime;

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "id")
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Basic
    @Column(name = "ccno")
    public Long getCcno() {
        return ccno;
    }

    public void setCcno(Long ccno) {
        this.ccno = ccno;
    }

    @Basic
    @Column(name = "cc_status")
    public String getCcStatus() {
        return ccStatus;
    }

    public void setCcStatus(String ccStatus) {
        this.ccStatus = ccStatus;
    }

    @Basic
    @Column(name = "readtime")
    public Timestamp getReadtime() {
        return readtime;
    }

    public void setReadtime(Timestamp readtime) {
        this.readtime = readtime;
    }

    @Basic
    @Column(name = "INFO_ID")
    public Long getInfoId() {
        return infoId;
    }

    public void setInfoId(Long infoId) {
        this.infoId = infoId;
    }

    @Basic
    @Column(name = "code")
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Basic
    @Column(name = "type")
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Basic
    @Column(name = "title")
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Basic
    @Column(name = "completed")
    public String getCompleted() {
        return completed;
    }

    public void setCompleted(String completed) {
        this.completed = completed;
    }

    @Basic
    @Column(name = "dealing")
    public String getDealing() {
        return dealing;
    }

    public void setDealing(String dealing) {
        this.dealing = dealing;
    }

    @Basic
    @Column(name = "coordinate")
    public String getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(String coordinate) {
        this.coordinate = coordinate;
    }

    @Basic
    @Column(name = "problems")
    public String getProblems() {
        return problems;
    }

    public void setProblems(String problems) {
        this.problems = problems;
    }

    @Basic
    @Column(name = "datastatus")
    public String getDatastatus() {
        return datastatus;
    }

    public void setDatastatus(String datastatus) {
        this.datastatus = datastatus;
    }

    @Basic
    @Column(name = "info_status")
    public String getInfoStatus() {
        return infoStatus;
    }

    public void setInfoStatus(String infoStatus) {
        this.infoStatus = infoStatus;
    }

    @Basic
    @Column(name = "sendee")
    public Long getSendee() {
        return sendee;
    }

    public void setSendee(Long sendee) {
        this.sendee = sendee;
    }

    @Basic
    @Column(name = "remarks")
    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    @Basic
    @Column(name = "other")
    public String getOther() {
        return other;
    }

    public void setOther(String other) {
        this.other = other;
    }

    @Basic
    @Column(name = "feedback")
    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    @Basic
    @Column(name = "USER_ID")
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    @Basic
    @Column(name = "report_date")
    public Timestamp getReportDate() {
        return reportDate;
    }

    public void setReportDate(Timestamp reportDate) {
        this.reportDate = reportDate;
    }

    @Basic
    @Column(name = "feedbacktime")
    public Timestamp getFeedbacktime() {
        return feedbacktime;
    }

    public void setFeedbacktime(Timestamp feedbacktime) {
        this.feedbacktime = feedbacktime;
    }

    @Basic
    @Column(name = "lastedittime")
    public Timestamp getLastedittime() {
        return lastedittime;
    }

    public void setLastedittime(Timestamp lastedittime) {
        this.lastedittime = lastedittime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VWorkReportCcEntity that = (VWorkReportCcEntity) o;

        if (id != that.id) return false;
        if (ccno != null ? !ccno.equals(that.ccno) : that.ccno != null) return false;
        if (ccStatus != null ? !ccStatus.equals(that.ccStatus) : that.ccStatus != null) return false;
        if (readtime != null ? !readtime.equals(that.readtime) : that.readtime != null) return false;
        if (infoId != null ? !infoId.equals(that.infoId) : that.infoId != null) return false;
        if (code != null ? !code.equals(that.code) : that.code != null) return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;
        if (title != null ? !title.equals(that.title) : that.title != null) return false;
        if (completed != null ? !completed.equals(that.completed) : that.completed != null) return false;
        if (dealing != null ? !dealing.equals(that.dealing) : that.dealing != null) return false;
        if (coordinate != null ? !coordinate.equals(that.coordinate) : that.coordinate != null) return false;
        if (problems != null ? !problems.equals(that.problems) : that.problems != null) return false;
        if (datastatus != null ? !datastatus.equals(that.datastatus) : that.datastatus != null) return false;
        if (infoStatus != null ? !infoStatus.equals(that.infoStatus) : that.infoStatus != null) return false;
        if (sendee != null ? !sendee.equals(that.sendee) : that.sendee != null) return false;
        if (remarks != null ? !remarks.equals(that.remarks) : that.remarks != null) return false;
        if (other != null ? !other.equals(that.other) : that.other != null) return false;
        if (feedback != null ? !feedback.equals(that.feedback) : that.feedback != null) return false;
        if (userId != null ? !userId.equals(that.userId) : that.userId != null) return false;
        if (reportDate != null ? !reportDate.equals(that.reportDate) : that.reportDate != null) return false;
        if (feedbacktime != null ? !feedbacktime.equals(that.feedbacktime) : that.feedbacktime != null) return false;
        if (lastedittime != null ? !lastedittime.equals(that.lastedittime) : that.lastedittime != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (ccno != null ? ccno.hashCode() : 0);
        result = 31 * result + (ccStatus != null ? ccStatus.hashCode() : 0);
        result = 31 * result + (readtime != null ? readtime.hashCode() : 0);
        result = 31 * result + (infoId != null ? infoId.hashCode() : 0);
        result = 31 * result + (code != null ? code.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (completed != null ? completed.hashCode() : 0);
        result = 31 * result + (dealing != null ? dealing.hashCode() : 0);
        result = 31 * result + (coordinate != null ? coordinate.hashCode() : 0);
        result = 31 * result + (problems != null ? problems.hashCode() : 0);
        result = 31 * result + (datastatus != null ? datastatus.hashCode() : 0);
        result = 31 * result + (infoStatus != null ? infoStatus.hashCode() : 0);
        result = 31 * result + (sendee != null ? sendee.hashCode() : 0);
        result = 31 * result + (remarks != null ? remarks.hashCode() : 0);
        result = 31 * result + (other != null ? other.hashCode() : 0);
        result = 31 * result + (feedback != null ? feedback.hashCode() : 0);
        result = 31 * result + (userId != null ? userId.hashCode() : 0);
        result = 31 * result + (reportDate != null ? reportDate.hashCode() : 0);
        result = 31 * result + (feedbacktime != null ? feedbacktime.hashCode() : 0);
        result = 31 * result + (lastedittime != null ? lastedittime.hashCode() : 0);
        return result;
    }
}
