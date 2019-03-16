/**
 * 
 */
package com.mossle.project.utils;

import org.apache.commons.lang3.StringUtils;

/**
 * 项目工具类
 * @author zyl
 */
public class ProjectUtils {

    /**
     * 获取项目进度
     * 
     * @param id
     * @return
     */
    public static String getProjectProgressSql(String userId) {
        StringBuffer sqlBuf = new StringBuffer();
        sqlBuf.append("SELECT CASE WHEN info.status = '3' THEN 100");
        sqlBuf.append(" WHEN date_format(now(), '%Y-%m-%d') < info.startdate THEN 0");
        sqlBuf.append(" WHEN date_format(now(), '%Y-%m-%d') > info.plandate THEN 100");
        sqlBuf.append(" ELSE round((timestampdiff(DAY, info.startdate, date_format(now(), '%Y-%m-%d')) + 1) * 100");
        sqlBuf.append("/(timestampdiff(DAY, info.startdate, info.plandate) + 1),0) END  AS targetPercent,");
        sqlBuf.append("CASE WHEN info.status = '2' OR info.status = '3' OR info.status = '4' THEN 100");
        sqlBuf.append(" ELSE round((SELECT sum(i.workload) FROM work_task_info i");
        sqlBuf.append(" JOIN work_project_taskbind t");
        sqlBuf.append(" JOIN work_project_info  p on t.projectcode = p.id");
		sqlBuf.append(" WHERE i.datastatus = '1' AND (i.status = '2' OR i.status = '4')");
		sqlBuf.append(" AND t.taskcode = i.id AND t.projectcode = info.id");
        sqlBuf.append(" AND p.leader=").append(userId).append(") * 100/");
        sqlBuf.append("(SELECT sum(i.workload) FROM work_task_info i");
        sqlBuf.append(" JOIN work_project_taskbind t");
        sqlBuf.append(" JOIN work_project_info  p on t.projectcode = p.id");
        sqlBuf.append(" WHERE i.datastatus = '1' AND i.status <> '3'");
        sqlBuf.append(" AND t.taskcode = i.id AND t.projectcode = info.id");
        sqlBuf.append(" AND p.leader=").append(userId).append("),0) END AS currentPercent,");
        sqlBuf.append("info.id AS id,info.code AS code,info.title AS title,info.content AS content,");
        sqlBuf.append("info.leader AS leader,info.startdate AS startdate,info.plandate AS plandate,");
        sqlBuf.append("info.status AS status,info.datastatus AS datastatus,info.committime AS committime,");
        sqlBuf.append("info.efficiency AS efficiency,info.hoursnum AS hoursnum,info.publisher AS publisher,");
        sqlBuf.append("info.publishtime AS publishtime,info.remarks AS remarks,info.evaluate AS evaluate,");
        sqlBuf.append("info.annex AS annex,info.evalscore AS evalscore,info.evaltime AS evaltime");
        sqlBuf.append(" FROM work_project_info info");
        
        return sqlBuf.toString();
    }
    
    /**
     * 获取项目信息
     * 
     * @param id
     * @return
     */
    public static String getProjectInfoSql(String userId) {
        StringBuffer sqlBuf = new StringBuffer();
        sqlBuf.append("SELECT project.targetPercent AS targetPercent,project.currentPercent AS currentPercent,");
		sqlBuf.append("project.id AS id,project.code AS code,project.title AS title,project.content AS content,");
		sqlBuf.append("project.leader AS leader,project.startdate AS startdate,project.plandate AS plandate,");
		sqlBuf.append("project.status AS status,project.datastatus AS datastatus,project.committime AS committime,");
		sqlBuf.append("project.efficiency AS efficiency,project.hoursnum AS hoursnum,project.publisher AS publisher,");
		sqlBuf.append("project.publishtime AS publishtime,project.remarks AS remarks,project.evaluate AS evaluate,");
		sqlBuf.append("project.annex AS annex,project.evalscore AS evalscore,project.evaltime AS evaltime,");
		sqlBuf.append("project.currentPercent - project.targetPercent AS diffpercent,");
		sqlBuf.append("CASE WHEN project.status = '3' THEN 'white'");
		sqlBuf.append(" WHEN project.currentPercent - project.targetPercent >= 0 THEN 'green'");
		sqlBuf.append(" WHEN project.currentPercent - project.targetPercent >= -5");
		sqlBuf.append(" AND project.currentPercent - project.targetPercent < 0 THEN 'yellow'");
		sqlBuf.append(" ELSE 'red' END  AS BG FROM").append("(").append(getProjectProgressSql(userId).toString()).append(") project");
        
        return sqlBuf.toString();
    }
}
