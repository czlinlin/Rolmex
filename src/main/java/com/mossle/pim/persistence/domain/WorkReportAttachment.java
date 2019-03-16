package com.mossle.pim.persistence.domain;

import javax.persistence.*;

/**
 * Created by wanghan on 2017\8\16 0016.
 * 汇报附件 实体类
 */
@Entity
@Table(name = "work_report_attachment")
public class WorkReportAttachment {
    private Long id;
    private String name;
    private Long fileSize;
    private String ref;
    private String tenantId;
    private WorkReportInfo workReportInfo;

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)//id的生成策略 IDENTITY是数据库自己设置
    @Column(name = "ID")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    @Column(name = "NAME")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    @Column(name = "FILE_SIZE")
    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }


    @Column(name = "REF")
    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }


    @Column(name = "TENANT_ID")
    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    @OneToOne
    @JoinColumn(name = "info_id")
    public WorkReportInfo getWorkReportInfo() {
        return workReportInfo;
    }

    public void setWorkReportInfo(WorkReportInfo workReportInfo) {
        this.workReportInfo = workReportInfo;
    }
}
