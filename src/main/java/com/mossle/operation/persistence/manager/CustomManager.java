package com.mossle.operation.persistence.manager;
/** 
 * @author  cz 
 * @version 2017年9月15日
 * 自定义申请 
 */
import com.mossle.core.hibernate.HibernateEntityDao;
import com.mossle.operation.persistence.domain.CustomEntity;
import org.springframework.stereotype.Service;

@Service
public class CustomManager  extends HibernateEntityDao<CustomEntity>{

}
