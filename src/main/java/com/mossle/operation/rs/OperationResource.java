package com.mossle.operation.rs;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.mossle.api.operation.OperationConnector;
import com.mossle.base.persistence.domain.BusinessDetailEntity;
import com.mossle.base.persistence.manager.BusinessDetailManager;
import com.mossle.core.auth.CurrentUserHolder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Path("operation")
public class OperationResource {
    private static Logger logger = LoggerFactory.getLogger(OperationResource.class);
    private OperationConnector operationConnector;
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private BusinessDetailManager businessDetailManager;
    @Autowired
    private CurrentUserHolder currentUserHolder;
   
    /**
     * 用户获取是否需要输入money的input [add by lilei 2018.11.12]
     * 添加流程标题,add by lilei at 2018.11.19
     * **/
    @GET
    @Path("getProcessContidionIsMoney")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> getProcessContidionIsMoney(@QueryParam("detailId") String detailId)
            throws Exception {
    	Map<String, Object> mapReturn=operationConnector.getProcessIsMoneyByDetailId(detailId);
    	boolean boolIsMoney=Boolean.parseBoolean(mapReturn.get("ismoney").toString());
    	if(boolIsMoney)
    		mapReturn.put("ismoney","1");
    	else 
    		mapReturn.put("ismoney","0");
    	
    	String strLevel=operationConnector.GetBusinessLevel(detailId, currentUserHolder.getUserId());
    	mapReturn.put("level",strLevel);
    	
    	
    	List<Map<String,Object>> mapConditionList=operationConnector.getBranchConditionList(detailId);
    	if(mapConditionList!=null&&mapConditionList.size()>0)
            mapReturn.put("loadStep","0");//多分支不加载流程步骤
    	else
			mapReturn.put("loadStep","1");
    	
    	mapReturn.put("bpmProcessTitle",operationConnector.getProcessTitle(detailId));//流程标题
    	return mapReturn;
    }
    
    /**
     * 得到表单输入或则选择的流程条件（此方法，暂未使用，方便以后扩展使用）add by lilei 2018.11.12
     * **/
    @GET
    @Path("getNeedContidionList")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> getNeedContidionList(@QueryParam("detailId") String detailId)
            throws Exception {
    	return null;
    }
    
    @Resource
	public void setOperationConnector(OperationConnector operationConnector) {
		this.operationConnector = operationConnector;
	}
    
    @Resource
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
}
