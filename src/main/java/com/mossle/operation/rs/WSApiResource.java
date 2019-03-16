package com.mossle.operation.rs;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.mossle.api.srmb.SrmbDTO;
import com.mossle.api.srmb.SrmbConnector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@Path("wsapi")
public class WSApiResource {
    private static Logger logger = LoggerFactory.getLogger(WSApiResource.class);
    private SrmbConnector srmbConnector;
   
    @GET
    @Path("checksab")
    @Produces(MediaType.APPLICATION_JSON)
    public SrmbDTO createorRenewSAB(@QueryParam("applycode") String applycode)
            throws Exception {
    	return srmbConnector.CreateOrRenewalSAB(applycode);
    }
    
    @Resource
	public void setSrmbConnector(SrmbConnector srmbConnector) {
		this.srmbConnector = srmbConnector;
	}
}
