package com.mossle.pim.persistence.domain;

import org.hibernate.persister.walking.internal.FetchStrategyHelper;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Date;

/**
 * Created by wanghan on 2017\8\16 0016.
 * 汇报抄送 实体类
 */
@Entity
@Table(name = "work_report_cc")
public class WorkReportCc {
    private Long id;
    private Long ccno;
    private String status;
    private Date readtime;
    private String ccType;

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
    @GeneratedValue(strategy= GenerationType.IDENTITY)//id的生成策略 IDENTITY是数据库自己设置
    @Column(name = "id")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    @Column(name = "ccno")
    public Long getCcno() {
        return ccno;
    }

    public void setCcno(Long ccno) {
        this.ccno = ccno;
    }


    @Column(name = "status")
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    @Column(name = "readtime")
    public Date getReadtime() {
        return readtime;
    }

    public void setReadtime(Date readtime) {
        this.readtime = readtime;
    }

    @Column(name = "cc_type")
	public String getCcType() {
		return ccType;
	}

	public void setCcType(String ccType) {
		this.ccType = ccType;
	}

    
}
