package com.mossle.party.rs;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.mossle.api.party.PartyConnector;
import com.mossle.api.party.PartyDTO;
import com.mossle.api.user.UserConnector;
import com.mossle.api.user.UserDTO;
import com.mossle.common.utils.StringUtils;
import com.mossle.core.auth.CurrentUserHolder;
import com.mossle.party.PartyConstants;
import com.mossle.party.persistence.domain.PartyEntity;
import com.mossle.party.persistence.domain.PartyStruct;
import com.mossle.party.persistence.domain.PartyType;
import com.mossle.party.persistence.manager.PartyEntityManager;
import com.mossle.party.persistence.manager.PartyStructManager;
import com.mossle.party.persistence.manager.PartyTypeManager;
import com.mossle.party.rs.OrderNumberResource.PartyEntityDTO;
import com.mossle.party.rs.PartyJsonpResource.PartyTypeDTO;
import com.mossle.party.service.PartyService;
import com.mossle.user.PersonInfoConstants;
import com.mossle.user.persistence.domain.AccountInfo;
import com.mossle.user.persistence.manager.AccountInfoManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@Path("party")
public class RegionBrachOfficeResource {
    private static Logger logger = LoggerFactory.getLogger(RegionBrachOfficeResource.class);
    private PartyTypeManager partyTypeManager;
    private PartyEntityManager partyEntityManager;
    private PartyStructManager partyStructManager;
    private AccountInfoManager accountInfoManager;
    private PartyService partyService;
    private UserConnector userConnector;
    private CurrentUserHolder currentUserHolder;
    private PartyConnector partyConnector;
   
    
    @GET
    @Path("regionId")
    @Produces(MediaType.APPLICATION_JSON)
    public List<PartyEntityDTO> getAllRegion() {
    	String hql ="from PartyEntity where partyType.id = ? and delFlag = 0";
    	List<PartyEntity> partyEntitys = partyEntityManager.find(hql, PartyConstants.PARTY_TYPE_AREA);
    	//List<PartyType> partyTypes = partyTypeManager.getAll();
    	
    	List<PartyEntityDTO> partyEntityDtos = new ArrayList<PartyEntityDTO>();
    	
    	for (PartyEntity partyEntity : partyEntitys) {
    		PartyEntityDTO partyEntityDto = new PartyEntityDTO();
    		//partyEntityDto.setPartyType(partyEntity.getPartyType());
    		partyEntityDto.setName(partyEntity.getName());
    		partyEntityDto.setId(partyEntity.getId());
    		partyEntityDtos.add(partyEntityDto);
    	}
    	
    	return partyEntityDtos;
    }

    @GET
    @Path("childEntityId")
    @Produces(MediaType.APPLICATION_JSON)
    public List<PartyEntityDTO> getChildEntitiesById(
            @QueryParam("bt") long bt) {
    	List<PartyEntityDTO> partyEntityDtos = new ArrayList<PartyEntityDTO>();
    	if(String.valueOf(bt).equals("0")){
    		return partyEntityDtos;
    	}
    	String hql = "from PartyStruct where parentEntity.id=? and parentEntity.delFlag = 0";
        List<PartyStruct> partyStructs = partyStructManager.find(hql, bt);

       
        for (PartyStruct partyStruct : partyStructs) {
        	int typeId = partyStruct.getChildEntity().getPartyType().getType();
        	if(typeId != 0){
        		continue;
        	}
            PartyEntityDTO partyEntityDto = new PartyEntityDTO();
    		//partyEntityDto.setPartyType(partyEntity.getPartyType());
    		partyEntityDto.setName(partyStruct.getChildEntity().getName());
    		partyEntityDto.setId(partyStruct.getChildEntity().getId());
    		partyEntityDtos.add(partyEntityDto);
    		
        }

        return partyEntityDtos;
    }
    
    //=====================================================================================================
    //根据当前登录人id获取其所在区域和分公司
    //步骤：用户id-部门id-分公司id-区域
    @GET
    @Path("branchOfficeName")
    @Produces(MediaType.APPLICATION_JSON)
    public List<PartyDTO> getBranchOfficeNameById(
    		 @QueryParam("userId") long userId) {
    	List<PartyDTO> list = new ArrayList();
    	PartyDTO partyDTO = partyConnector.findCompanyById(String.valueOf(userId));
    	list.add(partyDTO);
    	return list;
    }
    @GET
    @Path("AreaName")
    @Produces(MediaType.APPLICATION_JSON)
    public List<PartyDTO> getAreaNameById(
    		@QueryParam("userId") long userId) {
    	/*PartyEntity vo = partyEntityManager.get(userId);
    	PartyStruct partyStruct = partyStructManager.findUniqueBy("childEntity",vo);
    	PartyEntity pvo = partyStruct.getParentEntity();
    	long id = pvo.getPartyType().getId();
    	//String name = "";
    	List<PartyEntity> list = new ArrayList();
    	PartyEntity party = new PartyEntity();
    	if(id == 6){
    		party.setName(pvo.getName());
    		list.add(party);
    		//name = pvo.getName();
    		
    	}*/
    	
    	List<PartyDTO> list = new ArrayList();
    	PartyDTO partyDTO = partyConnector.findAreaById(String.valueOf(userId));
    	list.add(partyDTO);
    	return list;
    }

    
    // ~ ==================================================
    @Resource
    public void setPartyTypeManager(PartyTypeManager partyTypeManager) {
        this.partyTypeManager = partyTypeManager;
    }

    @Resource
    public void setPartyEntityManager(PartyEntityManager partyEntityManager) {
        this.partyEntityManager = partyEntityManager;
    }

    @Resource
    public void setPartyStructManager(PartyStructManager partyStructManager) {
        this.partyStructManager = partyStructManager;
    }

    @Resource
    public void setPartyService(PartyService partyService) {
        this.partyService = partyService;
    }

    @Resource
    public void setUserConnector(UserConnector userConnector) {
        this.userConnector = userConnector;
    }
    
    @Resource
    public void setCurrentUserHolder(CurrentUserHolder currentUserHolder) {
        this.currentUserHolder = currentUserHolder;
    }
    
    @Resource
	public void setAccountInfoManager(AccountInfoManager accountInfoManager) {
		this.accountInfoManager = accountInfoManager;
	}

    @Resource
	public void setPartyConnector(PartyConnector partyConnector) {
		this.partyConnector = partyConnector;
	}

	// ~ ==================================================
   
    public static class PartyEntityDTO {
        private long id;
        private String name;
        
       

		public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }


       
    }
    public static class PartyStructDTO {
    	private long childEntityId;
    	
    	
    	public long getChildEntityId() {
    		return childEntityId;
    	}
    	
    	public void setChildEntityId(long childEntityId) {
    		this.childEntityId = childEntityId;
    	}
    	
    	
    }
    public static class PartyTypeDTO {
    	private long id;
    	
    	
    	public long getId() {
    		return id;
    	}
    	
    	public void setId(long id) {
    		this.id = id;
    	}
    	
    	
    }
}
