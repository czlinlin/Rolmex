/**
 *
 */
package com.mossle.H5.work;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.mossle.api.notification.NotificationConnector;
import com.mossle.api.tenant.TenantHolder;
import com.mossle.api.user.UserConnector;
import com.mossle.api.user.UserDTO;
import com.mossle.common.utils.FileUploadAPI;
import com.mossle.core.query.PropertyFilter;
import com.mossle.internal.store.persistence.domain.StoreInfo;
import com.mossle.msg.MsgConstants;
import com.mossle.project.persistence.domain.WorkProjectInfo;
import com.mossle.project.persistence.domain.WorkProjectTaskbind;
import com.mossle.project.persistence.manager.WorkProjectInfoManager;
import com.mossle.project.persistence.manager.WorkProjectTaskbindManager;
import com.mossle.worktask.persistence.domain.WorkTaskCc;
import com.mossle.worktask.persistence.domain.WorkTaskInfo;
import com.mossle.worktask.persistence.manager.WorkTaskCcManager;
import com.mossle.worktask.persistence.manager.WorkTaskInfoManager;
import com.mossle.worktask.rs.WorkTaskResource;

/**
 * @author chengze 20181106 翻译视图
 */

public class WorkTaskUtils {
	
	 public static String getWorkTaskInfo() {
	 
	        String sqlString = "SELECT "; 
	        sqlString = sqlString +"  `t`.`id` AS `id`, "; 
	        sqlString = sqlString +"  `t`.`id` AS `TK01`, "; 
	        sqlString = sqlString +"  `t`.`code` AS `TK02`, "; 
	        sqlString = sqlString +"  `t`.`title` AS `title`, "; 
	        sqlString = sqlString +"  `t`.`title` AS `TK04`, "; 
	        sqlString = sqlString +"  `t`.`uppercode` AS `uppercode`, "; 
	        sqlString = sqlString +"  `t`.`uppercode` AS `TK03`, "; 
	        sqlString = sqlString +"  `t`.`content` AS `TK05`, "; 
	        sqlString = sqlString +"  `t`.`leader` AS `leader`, "; 
	        sqlString = sqlString +"  `t`.`leader` AS `TK06`, "; 
	        sqlString = sqlString +"  `GET_DISPLAY_NAME_BY_ID` (`t`.`leader`) AS `TK06C`, "; 
	        sqlString = sqlString +"  `t`.`plantime` AS `plantime`, "; 
	        sqlString = sqlString +"  `t`.`plantime` AS `TK07`, "; 
	        sqlString = sqlString +"  `t`.`workload` AS `TK09`, "; 
	        sqlString = sqlString +"  `t`.`status` AS `status`, "; 
	        sqlString = sqlString +"  `t`.`status` AS `TK10`, "; 
	        sqlString = sqlString +"  `t`.`committime` AS `committime`, "; 
	        sqlString = sqlString +"  `t`.`committime` AS `TK12`, "; 
	        sqlString = sqlString +"  `t`.`publisher` AS `publisher`, "; 
	        sqlString = sqlString +"  `t`.`publisher` AS `TK13`, "; 
	        sqlString = sqlString +"  `GET_DISPLAY_NAME_BY_ID` (`t`.`publisher`) AS `TK13C`, "; 
	        sqlString = sqlString +"  `t`.`publishtime` AS `publishtime`, "; 
	        sqlString = sqlString +"  `t`.`publishtime` AS `TK14`, "; 
	        sqlString = sqlString +"  `f_task_workload_rate` (`t`.`id`) AS `TK15`, "; 
	        sqlString = sqlString +"  `f_task_workload_rate_4up` (`t`.`id`) AS `workload_rate`, "; 
	        sqlString = sqlString +"  `t`.`starttime` AS `starttime`, "; 
	        sqlString = sqlString +"  `t`.`datastatus` AS `datastatus`, "; 
	        sqlString = sqlString +"  `t`.`tasktype` AS `tasktype`, "; 
	        sqlString = sqlString +"  `t`.`efficiency` AS `efficiency`, "; 
	        sqlString = sqlString +"  `t`.`hoursnum` AS `hoursnum`, "; 
	        sqlString = sqlString +"  `t`.`remarks` AS `remarks`, "; 
	        sqlString = sqlString +"  `t`.`evaluate` AS `evaluate`, "; 
	        sqlString = sqlString +"  `t`.`evalscore` AS `evalscore`, "; 
	        sqlString = sqlString +"  `t`.`evaltime` AS `evaltime`, "; 
	        sqlString = sqlString +"  `t`.`exectime` AS `exectime`, "; 
	        sqlString = sqlString +"  `t`.`annex` AS `annex`, "; 
	        sqlString = sqlString +"  `f_store_paths` (`t`.`id`, 0, 'OA/workTask') AS `store_paths`, "; 
	        sqlString = sqlString +"  `f_store_paths` (`t`.`id`, 1, 'OA/workTask') AS `annex1`, "; 
	        sqlString = sqlString +"  ( "; 
	        sqlString = sqlString +"  SELECT "; 
	        sqlString = sqlString +"  group_concat(`c`.`ccno` SEPARATOR ',') "; 
	        sqlString = sqlString +"  FROM "; 
	        sqlString = sqlString +" `work_task_cc` `c` "; 
	        sqlString = sqlString +" WHERE "; 
	        sqlString = sqlString +" (`c`.`info_id` = `t`.`id`) "; 
	        sqlString = sqlString +" ) AS `ccnos`, "; 
	        sqlString = sqlString +" ( "; 
	        sqlString = sqlString +" SELECT  "; 
	        sqlString = sqlString +"  group_concat(  "; 
	        sqlString = sqlString +" `GET_DISPLAY_NAME_BY_ID` (`c`.`ccno`) SEPARATOR ',' "; 
	        sqlString = sqlString +" ) "; 
	        sqlString = sqlString +"  FROM  "; 
	        sqlString = sqlString +"  `work_task_cc` `c` "; 
	        sqlString = sqlString +"  WHERE  "; 
	        sqlString = sqlString +" (`c`.`info_id` = `t`.`id`) "; 
	        sqlString = sqlString +" ) AS `ccnames`, "; 
	        sqlString = sqlString +" `b`.`projectcode` AS `projectcode`, "; 
	        sqlString = sqlString +" EXISTS ( "; 
	        sqlString = sqlString +"  SELECT  "; 
	        sqlString = sqlString +" 1 "; 
	        sqlString = sqlString +"  FROM  "; 
	        sqlString = sqlString +"  `work_task_info` `u`  "; 
	        sqlString = sqlString +"  WHERE  "; 
	        sqlString = sqlString +" (`u`.`id` = `t`.`uppercode`) "; 
	        sqlString = sqlString +" ) AS `exists_up` "; 
	        sqlString = sqlString +"  FROM  "; 
	        sqlString = sqlString +" ( "; 
	        sqlString = sqlString +" `work_task_info` `t` "; 
	        sqlString = sqlString +"  LEFT JOIN `work_project_taskbind` `b` ON ((`b`.`taskcode` = `t`.`id`)) ";  
	        sqlString = sqlString +" ) ";  
	        sqlString = sqlString +" ORDER BY "; 
	        sqlString = sqlString +" `t`.`publishtime` DESC,  ";  
	        sqlString = sqlString +" `t`.`id` DESC  ";  
	
	        return sqlString;
	    }
	 
	 public static String getH5WorkProjectInfo() {
		 
		 String sqlString = " SELECT ";
		 sqlString = sqlString +"   `i`.`id` AS `id`, ";
		 sqlString = sqlString +"   `i`.`id` AS `PJ01`, ";
		 sqlString = sqlString +"   `i`.`title` AS `title`, ";
		 sqlString = sqlString +"   `i`.`title` AS `PJ02`, ";
		 sqlString = sqlString +"   `i`.`content` AS `PJ03`, ";
		 sqlString = sqlString +"   `i`.`leader` AS `leader`, ";
		 sqlString = sqlString +"   `i`.`leader` AS `PJ05`, ";
		 sqlString = sqlString +"   `GET_DISPLAY_NAME_BY_ID` (`i`.`leader`) AS `PJ05C`, ";
		 sqlString = sqlString +"   `f_cellphone` (`i`.`leader`) AS `PJ05L`, ";
		 sqlString = sqlString +"   `i`.`plandate` AS `plandate`, ";
		 sqlString = sqlString +"   `i`.`plandate` AS `PJ06`, ";
		 sqlString = sqlString +"   `i`.`status` AS `status`, ";
		 sqlString = sqlString +"   ifnull(`i`.`status`, 0) AS `PJ07`, ";
		 sqlString = sqlString +"   `i`.`datastatus` AS `datastatus`, ";
		 sqlString = sqlString +"   `i`.`efficiency` AS `PJ08`, ";
		 sqlString = sqlString +"   `i`.`publisher` AS `publisher`, ";
		 sqlString = sqlString +"   `i`.`publisher` AS `PJ11`, ";
		 sqlString = sqlString +"   `i`.`publishtime` AS `publishtime`, ";
		 sqlString = sqlString +"   `i`.`publishtime` AS `PJ12`, ";
		 sqlString = sqlString +"   `f_store_paths` (`i`.`id`, 0, 'OA/project') AS `PJ04`, ";
		 sqlString = sqlString +"   `F_PROJECT_PLANNED_VALUE` (`i`.`id`) AS `PJ14`, ";
		 sqlString = sqlString +"   `F_PROJECT_EARNED_VALUE` (`i`.`id`) AS `PJ15`, ";
		 sqlString = sqlString +"   `F_PROJECT_NOTIFY_NAMES` (`i`.`id`) AS `CF02`, ";
		 sqlString = sqlString +"   ( ";
		 sqlString = sqlString +"   SELECT ";
		 sqlString = sqlString +"   group_concat(`n`.`userid` SEPARATOR ',') ";
		 sqlString = sqlString +"   FROM ";
		 sqlString = sqlString +"   `work_project_notify` `n` ";
		 sqlString = sqlString +"   WHERE ";
		 sqlString = sqlString +"   (`n`.`projectcode` = `i`.`id`) ";
		 sqlString = sqlString +"   ) AS `CF03`, ";
		 sqlString = sqlString +"   `i`.`startdate` AS `startdate`, ";
		 sqlString = sqlString +"   `i`.`committime` AS `committime`, ";
		 sqlString = sqlString +"   `i`.`hoursnum` AS `hoursnum`, ";
		 sqlString = sqlString +"   `i`.`remarks` AS `remarks`, ";
		 sqlString = sqlString +"   `i`.`evaluate` AS `evaluate`, ";
		 sqlString = sqlString +"   `i`.`evalscore` AS `evalscore`, ";
		 sqlString = sqlString +"   `i`.`evaltime` AS `evaltime`, ";
		 sqlString = sqlString +"   `i`.`exectime` AS `exectime`, ";
		 sqlString = sqlString +"   `f_store_paths` (`i`.`id`, 1, 'OA/project') AS `annex1` ";
		 sqlString = sqlString +"   FROM ";
		 sqlString = sqlString +"   `work_project_info` `i` ";
		
		return sqlString;
	 }
	 
	 //v_h5_cms_article
	 
	 public static String getH5CmsArticle() {
		 
		 String sqlString = " SELECT ";
		 sqlString = sqlString +"   `a`.`ID` AS `ID`,  ";
		 sqlString = sqlString +"   `a`.`ID` AS `NT01`,  ";
		 sqlString = sqlString +"   `a`.`TITLE` AS `NT02`,  ";
		 sqlString = sqlString +"   `a`.`CONTENT` AS `NT03`,  ";
		 sqlString = sqlString +"   `a`.`PUBLISH_TIME` AS `NT06`,  ";
		 sqlString = sqlString +"   `a`.`USER_ID` AS `NT05`,  ";
		 sqlString = sqlString +"   `GET_DISPLAY_NAME_BY_ID` (`a`.`USER_ID`) AS `NT05C`,  ";
		 sqlString = sqlString +"   `a`.`party_entity_id` AS `party_entity_id`,  ";
		 sqlString = sqlString +"   `a`.`party_entity_id` AS `NT04`,  ";
		 sqlString = sqlString +"   `a`.`start_time` AS `NT07`,  ";
		 sqlString = sqlString +"   `a`.`end_time` AS `NT08`  ";
		 sqlString = sqlString +"   FROM  ";
		 sqlString = sqlString +"   `cms_article` `a`  ";
		 sqlString = sqlString +"   WHERE  ";
		 sqlString = sqlString +"   (`a`.`STATUS` = 1)  ";
		 sqlString = sqlString +"   ORDER BY  ";
		 sqlString = sqlString +"   `a`.`PUBLISH_TIME` DESC  ";
		
		 return sqlString;
	 }
	 
	 public static String getH5PersonInfo() {
	 
		 String sqlString = " SELECT ";
		 sqlString = sqlString +"   `pe`.`IS_DISPLAY` AS `IS_DISPLAY_depart`,  ";
		 sqlString = sqlString +"   `pi`.`ID` AS `ID`,  ";
		 sqlString = sqlString +"   cast(`pi`.`ID` AS CHAR charset utf8) AS `code`,  ";
		 sqlString = sqlString +"   cast(`pi`.`ID` AS CHAR charset utf8) AS `PL01`,  ";
		 sqlString = sqlString +"   `pi`.`FULL_NAME` AS `PN06`,  ";
		 sqlString = sqlString +"   `pi`.`FULL_NAME` AS `name`,  ";
		 sqlString = sqlString +"   `pi`.`CELLPHONE` AS `PL02`,  ";
		 sqlString = sqlString +"   `pi`.`TELEPHONE` AS `PL03`,  ";
		 sqlString = sqlString +"   `pi`.`EMAIL` AS `PL05`,  ";
		 sqlString = sqlString +"   `pi`.`COMPANY_CODE` AS `COMPANY_CODE`,  ";
		 sqlString = sqlString +"   `pi`.`COMPANY_NAME` AS `subcomname`,  ";
		 sqlString = sqlString +"   `pi`.`COMPANY_NAME` AS `PN19C`,  ";
		 sqlString = sqlString +"   `pi`.`DEPARTMENT_CODE` AS `DEPARTMENT_CODE`,  ";
		 sqlString = sqlString +"   `pi`.`DEPARTMENT_CODE` AS `departid`,  ";
		 sqlString = sqlString +"   `pi`.`DEPARTMENT_NAME` AS `departname`,  ";
		 sqlString = sqlString +"   `pi`.`DEPARTMENT_NAME` AS `PN20C`,  ";
		 sqlString = sqlString +"   `pi`.`POSITION_CODE` AS `POSITION_CODE`,  ";
		 sqlString = sqlString +"   `GET_DICT_NAME_BY_TYPE_NAME` (  ";
		 sqlString = sqlString +"   'StaffPosition',  ";
		 sqlString = sqlString +"   `pi`.`POSITION_CODE`  ";
		 sqlString = sqlString +"   ) AS `rank`,  ";
		 sqlString = sqlString +"   `GET_DICT_NAME_BY_TYPE_NAME` (  ";
		 sqlString = sqlString +"   'StaffPosition',  ";
		 sqlString = sqlString +"   `pi`.`POSITION_CODE`  ";
		 sqlString = sqlString +"   ) AS `PN22C`,  ";
		 sqlString = sqlString +"   `pi`.`PRIORITY` AS `PRIORITY`,  ";
		 sqlString = sqlString +"   `pi`.`ADDRESS` AS `PL08`,  ";
		 sqlString = sqlString +"   `pi`.`FAX` AS `PL04`,  ";
		 sqlString = sqlString +"   `pi`.`WXNO` AS `PL06`,  ";
		 sqlString = sqlString +"   `pi`.`QQ` AS `PL07`,  ";
		 sqlString = sqlString +"   ifnull(`pi`.`secret`, 1) AS `secret`,  ";
		 sqlString = sqlString +"   `si`.`PATH` AS `photo`,  ";
		 sqlString = sqlString +"   `si`.`PATH` AS `PN09`,  ";
		 sqlString = sqlString +"   `f_party_parent_entity_name` (`pi`.`ID`) AS `PN21C`,  ";
		 sqlString = sqlString +"   `psp`.`PRIORITY` AS `PARENT_PRIORITY`  ";
		 sqlString = sqlString +"   FROM  ";
		 sqlString = sqlString +"   (  ";
		 sqlString = sqlString +"   (  ";
		 sqlString = sqlString +"   (  ";
		 sqlString = sqlString +"   (  ";
		 sqlString = sqlString +"   `person_info` `pi`  ";
		 sqlString = sqlString +"   LEFT JOIN `store_info` `si` ON (  ";
		 sqlString = sqlString +"   (  ";
		 sqlString = sqlString +"   (`pi`.`ID` = `si`.`PK_ID`)  ";
		 sqlString = sqlString +"   AND (`si`.`MODEL` = 'avatar')  ";
		 sqlString = sqlString +"   )  ";
		 sqlString = sqlString +"   )  ";
		 sqlString = sqlString +"   )  ";
		 sqlString = sqlString +"   LEFT JOIN `party_struct` `psc` ON (  ";
		 sqlString = sqlString +"   (  ";
		 sqlString = sqlString +"   (  ";
		 sqlString = sqlString +"   `psc`.`CHILD_ENTITY_ID` = `pi`.`ID`  ";
		 sqlString = sqlString +"   )  ";
		 sqlString = sqlString +"   AND (`psc`.`STRUCT_TYPE_ID` = 1)  ";
		 sqlString = sqlString +"   )  ";
		 sqlString = sqlString +"   )  ";
		 sqlString = sqlString +"   )  ";
		 sqlString = sqlString +"   LEFT JOIN `party_struct` `psp` ON (  ";
		 sqlString = sqlString +"   (  ";
		 sqlString = sqlString +"   (  ";
		 sqlString = sqlString +"   `psp`.`CHILD_ENTITY_ID` = `psc`.`PARENT_ENTITY_ID`  ";
		 sqlString = sqlString +"   )  ";
		 sqlString = sqlString +"   AND (`psp`.`STRUCT_TYPE_ID` = 1)  ";
		 sqlString = sqlString +"   )  ";
		 sqlString = sqlString +"   )  ";
		 sqlString = sqlString +"   )  ";
		 sqlString = sqlString +"   JOIN `party_entity` `pe` ON (  ";
		 sqlString = sqlString +"   (  ";
		 sqlString = sqlString +"   `psc`.`PARENT_ENTITY_ID` = `pe`.`ID`  ";
		 sqlString = sqlString +"   )  ";
		 sqlString = sqlString +"   )  ";
		 sqlString = sqlString +"   )  ";
		 sqlString = sqlString +"   WHERE  ";
		 sqlString = sqlString +"   (  ";
		 sqlString = sqlString +"   (`pe`.`IS_DISPLAY` = '1')  ";
		 sqlString = sqlString +"   AND (`pi`.`ID` > 9)  ";
		 sqlString = sqlString +"   AND (  ";
		 sqlString = sqlString +"   ifnull(`pi`.`STOP_FLAG`, 'active') = 'active'  ";
		 sqlString = sqlString +"   )  ";
		 sqlString = sqlString +"   AND (  ";
		 sqlString = sqlString +"   ifnull(`pi`.`DEL_FLAG`, 0) = 0  ";
		 sqlString = sqlString +"   )  ";
		 sqlString = sqlString +"   AND (  ";
		 sqlString = sqlString +"   ifnull(`pi`.`QUIT_FLAG`, 0) = 0  ";
		 sqlString = sqlString +"   )  ";
		 sqlString = sqlString +"   AND (  ";
		 sqlString = sqlString +"   NOT (  ";
		 sqlString = sqlString +"   EXISTS (  ";
		 sqlString = sqlString +"   SELECT  ";
		 sqlString = sqlString +"   1  ";
		 sqlString = sqlString +"   FROM  ";
		 sqlString = sqlString +"   (  ";
		 sqlString = sqlString +"   `auth_user_status` `aus`  ";
		 sqlString = sqlString +"   JOIN `auth_user_role` `aur` ON (  ";
		 sqlString = sqlString +"   (  ";
		 sqlString = sqlString +"   `aus`.`ID` = `aur`.`USER_STATUS_ID`  ";
		 sqlString = sqlString +"   )  ";
		 sqlString = sqlString +"   )  ";
		 sqlString = sqlString +"   )  ";
		 sqlString = sqlString +"   WHERE  ";
		 sqlString = sqlString +"   (  ";
		 sqlString = sqlString +"   (`aur`.`ROLE_ID` IN(1, 2))  ";
		 sqlString = sqlString +"   AND (`pi`.`ID` = `aus`.`ref`)  ";
		 sqlString = sqlString +"   )  ";
		 sqlString = sqlString +"   )  ";
		 sqlString = sqlString +"   )  ";
		 sqlString = sqlString +"   )  ";
		 sqlString = sqlString +"   )  ";
		 
		 return sqlString;
	 }

}
