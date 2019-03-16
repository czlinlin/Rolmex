package com.mossle.party.persistence.manager;

import com.mossle.core.hibernate.HibernateEntityDao;

import com.mossle.party.persistence.domain.PartyEntity;

import org.hibernate.SQLQuery;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
public class PartyEntityManager extends HibernateEntityDao<PartyEntity> {

/*    public String partyEntitiePath(Long partyEntityId) {
        String result="";
        String hql = "select f_party_path(?)";
        SQLQuery sqlQuery=getSession().createSQLQuery(hql);
        sqlQuery.setParameter(0,partyEntityId);
        result= (String) sqlQuery.uniqueResult();
        return result;
    }*/


    public String partyEntitieName(String partyEntityId) {
        String result="";
        List list=new ArrayList();
        String hql = "SELECT f_party_path_name(ID,'-') FROM `party_entity`WHERE FIND_IN_SET(ID,?) ORDER BY f_party_path_name(ID,'-') ";
        SQLQuery sqlQuery=getSession().createSQLQuery(hql);
        sqlQuery.setParameter(0,partyEntityId);
        list=sqlQuery.list();
        for(Object object:list){
          result=result+object+"<br>";
        }
        return result;
    }

}
