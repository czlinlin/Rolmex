package com.mossle.project.persistence.domain;

import javax.persistence.*;

/**
 * Created by wanghan on 2017\9\9 0009.
 * 项目知会实体类
 */
@Entity
@Table(name = "work_project_notify")
public class WorkProjectNotify {
    private Long id;
    private Long userid;
    private String status;

    private WorkProjectInfo workProjectInfo;

    @ManyToOne(fetch = FetchType.LAZY,cascade = CascadeType.REFRESH)
    @JoinColumn(name = "projectcode")
    public WorkProjectInfo getWorkProjectInfo() {
        return workProjectInfo;
    }

    public void setWorkProjectInfo(WorkProjectInfo workProjectInfo) {
        this.workProjectInfo = workProjectInfo;
    }

    @Id
    @Column(name = "id")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "userid")
    public Long getUserid() {
        return userid;
    }

    public void setUserid(Long userid) {
        this.userid = userid;
    }

    @Column(name = "status")
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


}
