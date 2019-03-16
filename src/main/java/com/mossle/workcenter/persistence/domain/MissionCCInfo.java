package com.mossle.workcenter.persistence.domain;

import org.activiti.engine.impl.transformer.StringToInteger;

import javax.persistence.*;

import java.util.Date;

/**
 * Created by lilei on 2017-08-11
 *
 * 任务抄送实体类
 */
@Entity
@Table(name = "RO_TK_CC")
public class MissionCCInfo implements java.io.Serializable {
    private static final long serialVersionUID = 0L;
    /**
     * 自增编号
     */
    private Long id;

    /**
     * 任务编号id
     */
    private Long taskcode;
    
    /**
     * 抄送编号
     */
    @SuppressWarnings("unused")
	private Long userid;

    /**
     * 删除状态，默认0，备注1
     */
    private String status;


    public MissionCCInfo() {
    }

    public MissionCCInfo(Long id) {
        this.id = id;
    }

    public MissionCCInfo(Long id, Long taskcode, Long userid, String status) {
        this.id = id;
        this.taskcode = taskcode;
        this.userid = userid;
        this.status = status;
    }

    @Id
    @Column(name = "id", unique = true, nullable = false, length = 16)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "taskcode", length = 100)
    public long getTaskcode() {
        return taskcode;
    }

    public void setTaskcode(long taskcode) {
        this.taskcode = taskcode;
    }
    
    @Column(name = "userid")
    public Long getUserid() {
        return userid;
    }

    public void setUserid(Long userid) {
        this.userid = userid;
    }

    @Column(name = "status", length = 2)
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}