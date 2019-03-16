package com.mossle.cms.persistence.manager;

import com.mossle.api.party.PartyConnector;
import com.mossle.api.party.PartyDTO;
import com.mossle.cms.persistence.domain.CmsArticle;

import com.mossle.core.hibernate.HibernateEntityDao;

import com.mossle.core.hibernate.HibernateUtils;
import com.mossle.core.page.Page;
import com.mossle.core.query.PropertyFilter;
import com.mossle.party.persistence.manager.PartyEntityManager;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class CmsArticleManager extends HibernateEntityDao<CmsArticle> {
    private PartyConnector partyConnector;
    private PartyEntityManager partyEntityManager;

    public Page pageQuery(Page page,String userId,   List<PropertyFilter> propertyFilters) {
        PartyDTO partyDTO = partyConnector.findDepartmentById(userId);
        String partyEntityId = partyDTO == null ? "" : partyDTO.getId();

//      String hql = "SELECT * FROM cms_article WHERE FIND_IN_SET(party_entity_id,f_party_path(?))";
        String resultlist = partyEntityManager.partyEntitieName(partyEntityId);
        propertyFilters.add(new PropertyFilter("INS_partyEntityId",resultlist));
        propertyFilters.add(new PropertyFilter("EQI_status", "1"));
        page=this.pagedQuery(page,propertyFilters);
        return page;
    }

    public Page pagedQueryByCms(Page page, String partyId, List<PropertyFilter> propertyFilters){
        if(propertyFilters==null){
            propertyFilters=new ArrayList<PropertyFilter>();
        }
        PropertyFilter propertyFilter=new PropertyFilter("EQS_cmsRange.partyId",partyId);
        propertyFilters.add(propertyFilter);
        propertyFilters.add(new PropertyFilter("EQI_status", "1"));

        Criteria criteria=this.getSession().createCriteria(this.getEntityClass());
        criteria.addOrder(Order.desc("publishTime"));
        criteria.createAlias("cmsRanges","cmsRange");
        Criterion[] criterions= HibernateUtils.buildCriterion(propertyFilters);

        for(Criterion criterion:criterions){
            criteria.add(criterion);
        }
        page=this.pagedQuery(criteria,page.getPageNo(), page.getPageSize());
        List<CmsArticle> cmsArticles=(List<CmsArticle>) page.getResult();
        for (CmsArticle cms:cmsArticles){
          cms.getCmsRanges();
        }
        return page;
    }

/*    public void removeCms(Long cmsId){
        String hql="delete from cmsRange where cmsArticle.id=? ";
        Query query=createQuery(hql);
        query.executeUpdate();
    }*/


    @Resource
    public void setPartyConnector(PartyConnector partyConnector) {
        this.partyConnector = partyConnector;
    }

    @Resource
    public void setpartyEntityManager(PartyEntityManager partyEntityManager) {
        this.partyEntityManager = partyEntityManager;
    }

}
