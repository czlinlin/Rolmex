package com.mossle.base.service;

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

import com.mossle.api.form.FormDTO;
import com.mossle.api.org.OrgConnector;
import com.mossle.api.process.ProcessConnector;
import com.mossle.api.process.ProcessDTO;
import com.mossle.base.persistence.domain.BusinessDetailEntity;
import com.mossle.base.persistence.domain.BusinessTypeEntity;
import com.mossle.base.persistence.domain.DetailPostDTO;
import com.mossle.base.persistence.domain.DetailPostEntity;
import com.mossle.base.persistence.manager.BusinessDetailManager;
import com.mossle.base.persistence.manager.BusinessTypeManager;
import com.mossle.base.persistence.manager.DetailPostManager;
import com.mossle.base.rs.BusinessResource.BusinessTypeDTO;
import com.mossle.bpm.persistence.domain.BpmProcess;
import com.mossle.bpm.persistence.manager.BpmProcessManager;
import com.mossle.bpm.rs.BpmResource;
import com.mossle.dict.persistence.domain.DictInfo;
import com.mossle.dict.persistence.manager.DictTypeManager;
import com.mossle.dict.support.DictConnectorImpl;
import com.mossle.party.PartyConstants;
import com.mossle.party.persistence.domain.PartyEntity;
import com.mossle.party.persistence.domain.PartyStructType;
import com.mossle.party.persistence.domain.PartyType;
import com.mossle.party.persistence.manager.PartyEntityManager;
import com.mossle.party.persistence.manager.PartyStructManager;
import com.mossle.party.persistence.manager.PartyStructTypeManager;
import com.mossle.party.persistence.manager.PartyTypeManager;

import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
/** 
 * @author  cz 
 * @version 2017年9月7日
 * 通过业务类型明细取岗位
 */
@Component
@Path("detailPostService")
public class DetailPostService {

    private DetailPostManager detailPostManager;
    
    private BusinessTypeManager businessTypeManager;
    
    private BpmProcessManager bpmProcessManager;
    
    private BusinessDetailManager businessDetailManager;
    
    private DictConnectorImpl dictConnectorImpl ;
    @Resource
    private BpmResource bpmResource ;
    
    private OrgConnector orgConnector;
    
    private ProcessConnector processConnector;
    // ~ ======================================================================
    
  //chengze 20180424 根据大区id取大区名称（自定义申请jsp 中tag用）
	  public String areaName(String userId) {
	    	
		//取用户的所属大区，带回自定义申请页面
	        String areaName = "";
	       
	        PartyEntity partyEntity  =  orgConnector.findPartyAreaByUserId(userId);
	    	
	    	if ( partyEntity ==  null){
	    		areaName = "";
	    		
	    	}
	    	else { areaName = partyEntity.getName();
	    		}
	    	
	    	return areaName;
	    }
	    
    
    
    
    
    
    //业务明细与岗位关系表里  取岗位名称
    public String getEntity(Long detailID) {
    	
    	String postName ="" ;
        String hql = "from DetailPostEntity where  detailID=? ";
        
        List<DetailPostEntity> detailPostEntitys = detailPostManager.find(hql, detailID);
        
        for (DetailPostEntity detailPostEntity : detailPostEntitys) {
        	postName = postName + detailPostEntity.getPostName()+",";
        }
        return  postName.substring(0,postName.length()-1)  ;
    }
    
    
    
    public String getBusinessType(Long id) {
    	
    	String hql = "from BusinessTypeEntity where  id=? ";

    	BusinessTypeEntity businessTypeEntity = businessTypeManager.findUnique(hql, id);
    	String TypeName = businessTypeEntity.getBusinesstype();
    	return TypeName;
    }
    
    public String getBpmProcessID(Long id) {
    	
    	String hql = "from BpmProcess where  id=? ";
    	BpmProcess bpmProcess = bpmProcessManager.findUnique(hql, id);
    	String bpname = bpmProcess.getName();
    	return bpname;
    }
    
    
    public List<BusinessTypeDTO> getAllPartyTypes() {
        List<BusinessTypeEntity> businessTypes = businessTypeManager.getAll();

        List<BusinessTypeDTO> businessTypeDtos = new ArrayList<BusinessTypeDTO>();

        for (BusinessTypeEntity businessType : businessTypes) {
        	BusinessTypeDTO businessTypeDto = new BusinessTypeDTO();
        	businessTypeDto.setId(businessType.getId());
        	businessTypeDto.setName(businessType.getBusinesstype());
        	businessTypeDtos.add(businessTypeDto);
        }

        return businessTypeDtos;
    }
    
    
    //取流程的名称
    @GET
    @Path("bpmAllName")
    public List<Map<String, Object>>  getBpmProcessName() {
    	
    	List<BpmProcess> bpm = bpmProcessManager.getAll();
    	
    	  List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

          for (BpmProcess bpmProcess : bpm) {
              Map<String, Object> map = new HashMap<String, Object>();
              map.put("name", bpmProcess.getName());
              map.put("id", bpmProcess.getId());
              list.add(map);
          }
    	
    	return list;
    }
    
    //获取表单名称
    @GET
    @Path("formName")
    public List<Map<String, Object>>  getFormName() {
    	
    	List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
    	//到数据字典中取表单
         List<DictInfo> dictInfo = this.dictConnectorImpl.findDictInfoListByType("applyForm");
         
     	for(DictInfo p:dictInfo){
     		Map<String, Object> map = new HashMap<String, Object>();
     		map.put("formName",p.getName());
     		map.put("formid",p.getValue());
     		list.add(map);
     	}
     	return list;
    }
    
    // 通过流程获取启动表单
    @GET
    @Path("getStartFromByProcessId")
    public List<Map<String, Object>> getStartFromByBpmProcessId(@QueryParam("bpmProcessId") String bpmProcessId) {
    	
    	List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
    	
    	ProcessDTO processDto = processConnector.findProcess(bpmProcessId);

    	String processDefinitionId = processDto.getProcessDefinitionId();
    	FormDTO formDto = processConnector.findStartForm(processDefinitionId);
    	if (formDto != null) {
	    	Map<String, Object> map = new HashMap<String, Object>();
	 		map.put("formName",formDto.getUrl());
	 		list.add(map);
    	} else {
    		Map<String, Object> map = new HashMap<String, Object>();
	 		map.put("formName","");
	 		list.add(map);
    	}
 		
    	return list;
    }
    
    //实现不同业务细分挂不同流程：根据用户选择的业务类型和业务细分去oa_ba_business_detail取流程的ID
    @GET
    @Path("BpmProcessID")
    public String  getBpmProcessID(@QueryParam("t1") String t1,@QueryParam("t2") String t2) {
    	String hql = "from BusinessDetailEntity where  busiDetail=? and businessType=?";
    	BusinessDetailEntity businessDetailEntity = businessDetailManager.findUnique(hql, t2,t1);
    	String bpmProcessId = "";
    	if(businessDetailEntity != null) {
    		bpmProcessId = businessDetailEntity.getBpmProcessId();
    	}
    	return bpmProcessId;
    }
    @GET
    @Path("BpmProcessIDByBusinessDetail")
    @ResponseBody
    @Produces(MediaType.APPLICATION_JSON)
    //取流程图userTask的名称过程
    public List<GetWhole>  getBpmProcessIDByBusinessDetail(@QueryParam("businessDetailID") long businessDetailID) {
    	GetWhole getWhole = new GetWhole();
    	List<GetWhole> getWholes = new ArrayList();
    	BusinessDetailEntity businessDetailEntity = businessDetailManager.findUniqueBy("id", businessDetailID);
    	String bpmProcessId = businessDetailEntity.getBpmProcessId();
    	String result = bpmResource.getResult(bpmProcessId);
    	getWhole.setBpmProcessId(bpmProcessId);
    	getWhole.setWhole(result);
    	getWholes.add(getWhole);
    	return getWholes;
    }

 

    // ~ ======================================================================
    public void save(Object o) {
        detailPostManager.save(o);
    }

    public void remove(Object o) {
        detailPostManager.remove(o);
    }
    public static class GetWhole{
    	String code;
    	String bpmProcessId;
    	String whole;
    	
		public String getCode() {
			return code;
		}
		public void setCode(String code) {
			this.code = code;
		}
		public String getBpmProcessId() {
			return bpmProcessId;
		}
		public void setBpmProcessId(String bpmProcessId) {
			this.bpmProcessId = bpmProcessId;
		}
		public String getWhole() {
			return whole;
		}
		public void setWhole(String whole) {
			this.whole = whole;
		}
    	
    }
    // ~ ======================================================================
   
    // ~ ======================================================================
    @Resource
    public void setDetailPostManager(DetailPostManager detailPostManager) {
        this.detailPostManager = detailPostManager;
    }
    
    @Resource
    public void setBusinessTypeManager(BusinessTypeManager businessTypeManager) {
        this.businessTypeManager = businessTypeManager;
    }
    
    @Resource
    public void setBpmProcessManager(BpmProcessManager bpmProcessManager) {
        this.bpmProcessManager = bpmProcessManager;
    }
    
    @Resource
    public void setBusinessDetailManager(BusinessDetailManager businessDetailManager) {
        this.businessDetailManager = businessDetailManager;
    }
    
    @Resource
	public void setDictConnectorImpl(DictConnectorImpl dictConnectorImpl) {
		this.dictConnectorImpl = dictConnectorImpl;
	}

    
    @Resource
    public void setOrgConnector(OrgConnector orgConnector) {
        this.orgConnector = orgConnector;
    }

    @Resource
    public void setProcessConnector(ProcessConnector processConnector) {
        this.processConnector = processConnector;
    }
}
