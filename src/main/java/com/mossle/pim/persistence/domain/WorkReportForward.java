package com.mossle.pim.persistence.domain;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by wanghan on 2017\8\16 0016.
 * 汇报转发 实体类
 */
@Entity
@Table(name = "work_report_forward")
public class WorkReportForward {
    private Long id;
    private Long sendee;
    private String status;
    private Long forwarder;
    private Date forwardtime;
    private String remarks;
    private Date readtime;
    private String isfeedbackforward;//是否转发反馈内容


    private WorkReportInfo workReportInfo;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="info_id")
    public WorkReportInfo getWorkReportInfo() {
        return workReportInfo;
    }

    public void setWorkReportInfo(WorkReportInfo workReportInfo) {
        this.workReportInfo = workReportInfo;
    }

    @Id
    //@GeneratedValue(strategy= GenerationType.IDENTITY)//id的生成策略 IDENTITY是数据库自己设置
    @Column(name = "id")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    @Column(name = "sendee")
    public Long getSendee() {
        return sendee;
    }

    public void setSendee(Long sendee) {
        this.sendee = sendee;
    }


    @Column(name = "status")
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    @Column(name = "forwarder")
    public Long getForwarder() {
        return forwarder;
    }

    public void setForwarder(Long forwarder) {
        this.forwarder = forwarder;
    }


    @Column(name = "forwardtime")
    public Date getForwardtime() {
        return forwardtime;
    }

    public void setForwardtime(Date forwardtime) {
        this.forwardtime = forwardtime;
    }


    @Column(name = "remarks")
    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }


    @Column(name = "readtime")
    public Date getReadtime() {
        return readtime;
    }

    public void setReadtime(Date readtime) {
        this.readtime = readtime;
    }

    @Column(name = "isfeedbackforward")
	public String getIsfeedbackforward() {
		return isfeedbackforward;
	}

	public void setIsfeedbackforward(String isfeedbackforward) {
		this.isfeedbackforward = isfeedbackforward;
	}
    
    
    
    

}
