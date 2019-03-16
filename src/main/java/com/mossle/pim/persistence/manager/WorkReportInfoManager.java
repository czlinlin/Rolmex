package com.mossle.pim.persistence.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.persistence.criteria.Join;

import org.hibernate.Criteria;
import org.hibernate.SQLQuery;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.google.common.base.Joiner;
import com.mossle.api.party.PartyConnector;
import com.mossle.core.hibernate.HibernateEntityDao;
import com.mossle.core.hibernate.HibernateUtils;
import com.mossle.core.page.Page;
import com.mossle.core.query.PropertyFilter;
import com.mossle.core.util.StringUtils;
import com.mossle.party.PartyConstants;
import com.mossle.party.persistence.domain.PartyEntity;
import com.mossle.party.persistence.manager.PartyEntityManager;
import com.mossle.pim.persistence.domain.WorkReportInfo;
import com.mossle.util.DateUtil;

/**
 * Created by wanghan on 2017\8\16 0016.
 */
@Service
public class WorkReportInfoManager extends HibernateEntityDao<WorkReportInfo> {
	@Autowired
	private JdbcTemplate jdbcTemplate;
	@Autowired
	private PartyEntityManager partyEntityManager;
	@Autowired
	private PartyConnector partyConnector;
	
    public Page pagedQueryByCc(Page page, String userId,String strPostId, List<PropertyFilter> propertyFilters){
        if(propertyFilters==null){
            propertyFilters=new ArrayList<PropertyFilter>();
        }
        //PropertyFilter propertyFilter=new PropertyFilter("EQL_workReportCc.ccno",userId);
        //propertyFilters.add(propertyFilter);
        
        //propertyFilters.add(new PropertyFilter("EQL_addUserId_OR_id", userId,strPostId));

        
        /*Criteria criteria=this.getSession().createCriteria(this.getEntityClass());
        List<Long> postIdList=new ArrayList<Long>();
        if(!StringUtils.isBlank(strPostId)){
        	criteria.add(Restrictions.or(Restrictions.eq("workReportCc.ccno",Long.parseLong(userId)),Restrictions.in("workReportCc.ccno",strPostId.split(","))));
        }
        else {
        	criteria.add(Restrictions.eq("ccno",userId));
		}
        criteria.addOrder(Order.desc("reportDate"));
        criteria.createAlias("workReportCcs","workReportCc");
//        将filter build成hibernate里的查询条件
        Criterion[] criterions= HibernateUtils.buildCriterion(propertyFilters);
//        将查询条件添加打开Criteria
        for(Criterion criterion:criterions){
            criteria.add(criterion);
        }*/
        
        
        /*// StringBuffer sbHql=new StringBuffer("select distinct i.* from WorkReportInfo i inner join fenth i.workReportCcs as workReportCc");
        StringBuffer sbHql=new StringBuffer("select i.userName from WorkReportInfo i, WorkReportCc c where i.id=c.workReportInfo");
        PropertyFilter propertyFilter=new PropertyFilter("EQL_i.ccno",userId);
        propertyFilters.add(propertyFilter);
        if(!StringUtils.isEmpty(strPostId)){
        	sbHql.append(" where (i.ccno="+Long.parseLong(userId)+" or i.ccno in("+strPostId+"))");
        }
        else {
        	sbHql.append(" where i.ccno="+Long.parseLong(userId));
		}
        
        //page= this.pagedQuery(criteria,page.getPageNo(),2);
        
        page.setPageSize(2);
        // page= this.pagedQuery(sbHql.toString(), page, propertyFilters);
*/       
        StringBuffer where = new StringBuffer();
        if(!StringUtils.isEmpty(strPostId)){
        	where.append(" where (workReportCc.ccno="+Long.parseLong(userId)+" or workReportCc.ccno in("+strPostId+"))");
        }
        else {
        	where.append(" where workReportCc.ccno="+Long.parseLong(userId));
		}
        
        boolean isSelectPreSetting=false;	//查询条件是否包含条线
        for (PropertyFilter filter : propertyFilters) {
            // 只有一个属性需要比较的情况.
            if (!filter.hasMultiProperties()) {
            	//System.out.println(filter.getPropertyName() + "----" + filter.getMatchValue() + "----" + filter.getMatchType());
            	//System.out.println(filter.getPropertyClass().toString());
            	switch (filter.getMatchType()) {
                case EQ:
                	String name = "";
                	if("ccPreSettingId".equals(filter.getPropertyName())&&!filter.getMatchValue().equals(""))
                		isSelectPreSetting=true;
                	
                	if ("status".equals(filter.getPropertyName())) {
                		name = "workReportCc.status";
                	}
                	else {
                		name = filter.getPropertyName();
                	}
                	where.append(" and ").append(name).append(" = '").append(filter.getMatchValue()).append("'");
                    break;
                case IN:
                	if("user_id".equals(filter.getPropertyName())&&!filter.getMatchValue().equals("")){
                		Long departOrUserId=Long.parseLong(filter.getMatchValue().toString().replace("[", "").replace("]", ""));
                		PartyEntity partyEntity=partyEntityManager.get(departOrUserId);
                		if(partyEntity!=null){
                			if(partyEntity.getPartyType().getId().equals(PartyConstants.PARTY_TYPE_USER)){
                				where.append(" and ").append(filter.getPropertyName()).append("=").append(departOrUserId);
                			}
                			else{
                				List<String> deparentList=new ArrayList<String>();
                				if(departOrUserId.equals(PartyConstants.ROOT_PARTY_TREE_ID)){
                					deparentList=partyConnector.getUpperCompanyAndLowerDepartment(userId);
                				}
                				else{
                					List<String> upperAndLowerList=partyConnector.getUpperCompanyAndLowerDepartment(userId);
                					List<String> thisDepartLowerList=getAllDeparentById(departOrUserId);
                					for (String departId : thisDepartLowerList) {
										if(upperAndLowerList.contains(departId))
											deparentList.add(departId);
									}
                					//deparentList=getAllDeparentById(departOrUserId);
                				}
                				if(deparentList.size()>0){
                					String deparentIds=Joiner.on(",").join(deparentList);
                            		where.append(" and ").append(filter.getPropertyName())
                						 .append(" in(SELECT CHILD_ENTITY_ID FROM party_struct where PARENT_ENTITY_ID in(").append(deparentIds).append("))");
                				}
                			}
                			
                		}
                	}
                	break;
                case GE:
                	if ("class java.util.Date".equals(filter.getPropertyClass().toString())) {
                		where.append(" and ").append(filter.getPropertyName()).append(" >= '").append(formatDate2(filter.getMatchValue().toString())).append("'");
                	} else {
                		where.append(" and ").append(filter.getPropertyName()).append(" >= '").append(filter.getMatchValue().toString()).append("'");
                	}
                    break;
                case LE:
                	if ("class java.util.Date".equals(filter.getPropertyClass().toString())) {
                		where.append(" and ").append(filter.getPropertyName()).append(" <= '").append(formatDate2(filter.getMatchValue().toString())).append("'");
                	} else {
                		where.append(" and ").append(filter.getPropertyName()).append(" <= '").append(filter.getMatchValue().toString()).append("'");
                	}
                    break;
                case LIKE:
                	if ("user_id".equals(filter.getPropertyName())) {
                		String strSql="select id from party_entity where `NAME` like '%"+filter.getMatchValue().toString()+"%'";
                		List<String> userNameList=jdbcTemplate.queryForList(strSql, String.class);
                		if(userNameList.size()<1){
                			userNameList.add("0");
                		}
                		String userIds=Joiner.on(",").join(userNameList);
                		where.append(" and ").append(filter.getPropertyName()).append(" in(").append(userIds).append(")");
                	}
                	else
                		where.append(" and ").append(filter.getPropertyName()).append(" like '%").append(filter.getMatchValue()).append("%'");
                    break;
                default:
                    break;
                }
            } 
            
        }
        
        StringBuffer sqlCount=new StringBuffer("SELECT distinct i.id FROM work_report_info i");
        if(isSelectPreSetting){
        	sqlCount.append(" INNER JOIN  work_report_cc workReportCc on i.ID = workReportCc.INFO_ID");
        	sqlCount.append(" INNER JOIN  work_report_info_attr attr on i.ID = attr.ID");
        }
        else
        	sqlCount.append(" INNER JOIN  work_report_cc workReportCc on i.ID = workReportCc.INFO_ID");
        
        sqlCount.append(where);
        List listCount=new ArrayList();
        SQLQuery sqlQueryCount=getSession().createSQLQuery(sqlCount.toString());
        listCount = sqlQueryCount.list();
        int totalCount = 0;
        if (listCount != null && listCount.size() > 0) {
        	totalCount = listCount.size();
        } else {
        	return new Page();
        }
        
        StringBuffer sbHql=new StringBuffer("SELECT distinct i.* FROM work_report_info i");
        if(isSelectPreSetting){
        	sbHql.append(" INNER JOIN  work_report_cc workReportCc on i.ID = workReportCc.INFO_ID");
        	sbHql.append(" INNER JOIN  work_report_info_attr attr on i.ID = attr.ID");
        }
        else
        	sbHql.append(" INNER JOIN  work_report_cc workReportCc on i.ID = workReportCc.INFO_ID");
        sbHql.append(where);
       
        sbHql.append(" order by report_date desc,workReportCc.cc_type");
        // page.setPageSize(2);
        sbHql.append(" limit ").append((page.getPageNo()-1) * page.getPageSize()).append(" , ").append(page.getPageSize());
        
        SQLQuery sqlQuery=getSession().createSQLQuery(sbHql.toString()).addEntity(WorkReportInfo.class);
        // sqlQuery.setParameter(0,partyEntityId);
        List<WorkReportInfo> list=new ArrayList<WorkReportInfo>();
        list = sqlQuery.list();
        page.setResult(list);
        page.setTotalCount(totalCount);
        List<WorkReportInfo> infos= (List<WorkReportInfo>) page.getResult();
        // WorkReportCcs在WorkReportInfo中是延迟加载的，所以get一下从数据库中加载出来
        for(WorkReportInfo info:infos){
            info.getWorkReportCcs();
        }
        return page;
    }
    
	private List<String> getAllDeparentById(Long partyEntityId) {
		
    	List<String> childAllList=new ArrayList<String>();
    	childAllList.add(partyEntityId.toString());    	
    	/*List<String> partyIdList=null;
		String strSql="Select partyEntityID from auth_orgdata where type='1' and union_id="+id;
		partyIdList=jdbcTemplate.queryForList(strSql, String.class);*/
    	
    	String sqlString="select s.CHILD_ENTITY_ID from party_struct s"
    			+ " inner join party_entity c on c.id=s.CHILD_ENTITY_ID"
    			+ " where c.type_id not in(1,5) and s.STRUCT_TYPE_ID=1 and s.PARENT_ENTITY_ID in(%s)";
    	List<String> partyIdList=jdbcTemplate.queryForList(String.format(sqlString, partyEntityId), String.class);
    	if(partyIdList!=null&&partyIdList.size()>0){
    		childAllList.addAll(partyIdList);
    		String strPartyIds=Joiner.on(",").join(partyIdList);
    		while(true){
    			List<String> childPartyIdList=jdbcTemplate.queryForList(String.format(sqlString,strPartyIds), String.class);
    			if(childPartyIdList!=null&&childPartyIdList.size()>0)
    			{
    				childAllList.addAll(childPartyIdList);
    				strPartyIds=Joiner.on(",").join(childPartyIdList);
    			}
    			else {
					break;
				}
        	}
    	}
    	return childAllList;
	}
    
    public static String formatDate2(String dateStr) {
        String[] aStrings = dateStr.split(" ");
        // 5
        if (aStrings[1].equals("Jan")) {
            aStrings[1] = "01";
        }
        if (aStrings[1].equals("Feb")) {
            aStrings[1] = "02";
        }
        if (aStrings[1].equals("Mar")) {
            aStrings[1] = "03";
        }
        if (aStrings[1].equals("Apr")) {
            aStrings[1] = "04";
        }
        if (aStrings[1].equals("May")) {
            aStrings[1] = "05";
        }
        if (aStrings[1].equals("Jun")) {
            aStrings[1] = "06";
        }
        if (aStrings[1].equals("Jul")) {
            aStrings[1] = "07";
        }
        if (aStrings[1].equals("Aug")) {
            aStrings[1] = "08";
        }
        if (aStrings[1].equals("Sep")) {
            aStrings[1] = "09";
        }
        if (aStrings[1].equals("Oct")) {
            aStrings[1] = "10";
        }
        if (aStrings[1].equals("Nov")) {
            aStrings[1] = "11";
        }
        if (aStrings[1].equals("Dec")) {
            aStrings[1] = "12";
        }
        String date = aStrings[5] + "-" + aStrings[1] + "-" + aStrings[2] + " " + aStrings[3];
        
        return date;
    }
}
