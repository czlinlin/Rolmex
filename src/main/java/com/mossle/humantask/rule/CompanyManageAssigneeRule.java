package com.mossle.humantask.rule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mossle.api.org.OrgConnector;
import com.mossle.core.spring.ApplicationContextHelper;
import com.mossle.humantask.persistence.domain.TaskParticipant;
import com.mossle.party.persistence.domain.PartyEntity;
import com.mossle.util.StringUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 获得指定用户的上级领导.
 * 
 */
public class CompanyManageAssigneeRule implements AssigneeRule {
    private static Logger logger = LoggerFactory
            .getLogger(SuperiorAssigneeRule.class);
    private OrgConnector orgConnector;

    public List<String> process(String value, String initiator) {
        // return Collections.singletonList(this.process(initiator));
    	if (orgConnector == null) {
            orgConnector = ApplicationContextHelper.getBean(OrgConnector.class);
        }

        PartyEntity partyEntity =  orgConnector.findPartyCompanyByUserId(initiator); 
        
        List<String> userIds =  orgConnector.getManageIdByParty(partyEntity);
       
        return userIds;
    }

    @Override
	public String process(String initiator) {
		// TODO Auto-generated method stub
		return null;
	}
}
