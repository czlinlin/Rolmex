package com.mossle.worktask.persistence.domain;

import javax.persistence.*;

/**
 * Created by wanghan on 2017\8\30 0030.
 * 任务抄送实体类
 */
@Entity
@Table(name = "work_task_cc")
public class WorkTaskCc {
    private Long id;
    private Long ccno;
    private String status;

    private WorkTaskInfo workTaskInfo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "info_id")
    public WorkTaskInfo getWorkTaskInfo() {
        return workTaskInfo;
    }

    public void setWorkTaskInfo(WorkTaskInfo workTaskInfo) {
        this.workTaskInfo = workTaskInfo;
    }

    @Id
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


}
