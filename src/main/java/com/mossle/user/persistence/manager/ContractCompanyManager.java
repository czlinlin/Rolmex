package com.mossle.user.persistence.manager;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import com.mossle.api.dict.DictConnector;
import com.mossle.api.org.OrgConnector;
import com.mossle.api.user.PersonInfoDTO;
import com.mossle.core.auth.CurrentUserHolder;
import com.mossle.core.hibernate.HibernateUtils;
import com.mossle.core.hibernate.HibernateEntityDao;
import com.mossle.core.mapper.BeanMapper;
import com.mossle.core.query.PropertyFilter;
import com.mossle.core.util.StringUtils;
import com.mossle.party.persistence.domain.PartyEntity;
import com.mossle.user.persistence.domain.PersonContractCompanyManage;
import com.mossle.user.persistence.domain.PersonInfo;
import com.mossle.user.persistence.domain.PersonInfoDTOForExport;
import com.mossle.worktask.persistence.domain.WorkTaskInfo;
import com.mossle.worktask.persistence.domain.WorkTaskInfoInstance;

import org.springframework.stereotype.Service;

import com.mossle.core.hibernate.HibernateEntityDao;

@Service
public class ContractCompanyManager extends HibernateEntityDao<PersonContractCompanyManage> {
	
	
}
