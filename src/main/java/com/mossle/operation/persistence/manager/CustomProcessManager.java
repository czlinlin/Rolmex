package com.mossle.operation.persistence.manager;
/** 
 * @author  cz 
 * @version 2017年7月27日
 * 类说明 
 */
import com.mossle.core.hibernate.HibernateEntityDao;
import com.mossle.operation.persistence.domain.Apply;
import com.mossle.operation.persistence.domain.CustomProcess;

import org.springframework.stereotype.Service;

@Service
public class CustomProcessManager  extends HibernateEntityDao<CustomProcess>{

}
