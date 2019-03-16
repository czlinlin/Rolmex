package com.mossle.workcenter.rs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.mossle.workcenter.persistence.domain.*;
import com.mossle.workcenter.persistence.manager.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@Path("workcenter")
public class WorkCenterSource {
    private static Logger logger = LoggerFactory.getLogger(WorkCenterSource.class);

    private MissionCCInfoManager missionCCInfoManager;
    
    @GET
    @Path("chargeCCInfo-list")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Map<String, Object>> GetCCMan(@QueryParam("id") String id)
    {
    	List<MissionCCInfo> ccInfos= missionCCInfoManager.findBy("taskcode", Long.parseLong(id));
    	
    	List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		if(ccInfos!=null&&ccInfos.size()>0)
		{
			for (MissionCCInfo cc : ccInfos) {
	            Map<String, Object> map = new HashMap<String, Object>();
	            map.put("userid", cc.getUserid());
	            list.add(map);
	        }
			
		}
        return list;
    }
    
    @Resource
    public void setMissionCCInfoManager(MissionCCInfoManager missionCCInfoManager) {
        this.missionCCInfoManager = missionCCInfoManager;
    }
}
