package com.mossle.common.rs;

import java.io.InputStream;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;

@Component
@Path("download")
public class DownLoadResource {
    private static Logger logger = LoggerFactory.getLogger(DownLoadResource.class);
    private StoreConnector storeConnector;
    private TenantHolder tenantHolder;

   
    @GET
    @Path("downloadfile")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public InputStream enclosures(
    		@QueryParam("key") String key,
    		@QueryParam("modelname") String modelname,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        String tenantId = tenantHolder.getTenantId();
        StoreDTO storeDto = storeConnector.getStore(modelname,
                key, tenantId);
        ServletUtils.setFileDownloadHeader(request, response,
                storeDto.getDisplayName());

        return storeDto.getDataSource().getInputStream();
    }

    @Resource
    public void setStoreConnector(StoreConnector storeConnector) {
        this.storeConnector = storeConnector;
    }

    @Resource
    public void setTenantHolder(TenantHolder tenantHolder) {
        this.tenantHolder = tenantHolder;
    }
}
