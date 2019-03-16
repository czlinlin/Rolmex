package com.mossle.project.persistence.domain;


import com.mossle.worktask.persistence.domain.WorkTaskInfo;
import javax.persistence.*;


/**
 * Created by wanghan on 2017\9\9 0009.
 */
@Entity
@Table(name = "work_project_taskbind")
public class WorkProjectTaskbind {
    private Long id;
    private String bindtype;
    private WorkProjectInfo workProjectInfo;
    private WorkTaskInfo workTaskInfo;

    @OneToOne
    @JoinColumn(name = "taskcode")
    public WorkTaskInfo getWorkTaskInfo() {
        return workTaskInfo;
    }
    public void setWorkTaskInfo(WorkTaskInfo workTaskInfo) {
        this.workTaskInfo = workTaskInfo;
    }

    @ManyToOne
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

    @Column(name = "bindtype")
    public String getBindtype() {
        return bindtype;
    }

    public void setBindtype(String bindtype) {
        this.bindtype = bindtype;
    }

}
