package com.mossle.operation.rs;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.mossle.api.store.StoreConnector;
import com.mossle.api.store.StoreDTO;
import com.mossle.api.tenant.TenantHolder;

import com.mossle.core.util.ServletUtils;
import com.mossle.operation.persistence.domain.LllegalFreeze;
import com.mossle.operation.persistence.manager.LllegalFreezeManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;

@Component
@Path("lllegalFreezeOrder")
public class OrderLllegalFreezeResource {
    private static Logger logger = LoggerFactory.getLogger(OrderLllegalFreezeResource.class);
    private StoreConnector storeConnector;
    private TenantHolder tenantHolder;
    private LllegalFreezeManager lllegalFreezeManager;
   
    @GET
    @Path("selectNum")
    public List<LllegalFreeze> result(@QueryParam("param") String date,@QueryParam("param2") String deptName)throws Exception {
    	/*SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    	Date queryDate = sdf.parse(date);*/
    	String hql = "from LllegalFreeze where date = ? and deptName = ?";
    	List<LllegalFreeze> list = lllegalFreezeManager.find(hql, date,deptName);
    	return list;
    }

    @Resource
    public void setStoreConnector(StoreConnector storeConnector) {
        this.storeConnector = storeConnector;
    }

    @Resource
    public void setTenantHolder(TenantHolder tenantHolder) {
        this.tenantHolder = tenantHolder;
    }
    @Resource
    public void setLllegalFreezeManager(LllegalFreezeManager lllegalFreezeManager) {
    	this.lllegalFreezeManager = lllegalFreezeManager;
    }
}
