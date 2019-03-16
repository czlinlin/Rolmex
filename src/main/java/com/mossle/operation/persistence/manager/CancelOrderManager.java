package com.mossle.operation.persistence.manager;
/** 
 * @author  cz 
 * @version 2017年7月27日
 * 撤单 
 */
import com.mossle.core.hibernate.HibernateEntityDao;
import com.mossle.operation.persistence.domain.CancelOrder;

import org.springframework.stereotype.Service;

@Service
public class CancelOrderManager  extends HibernateEntityDao<CancelOrder>{

}
