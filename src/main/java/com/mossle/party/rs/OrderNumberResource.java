package com.mossle.party.rs;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

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
public class OrderNumberResource {
    private static Logger logger = LoggerFactory.getLogger(OrderNumberResource.class);
    private PartyTypeManager partyTypeManager;
    private PartyEntityManager partyEntityManager;
    private PartyStructManager partyStructManager;
    private AccountInfoManager accountInfoManager;
    private PartyService partyService;
    private UserConnector userConnector;
    private CurrentUserHolder currentUserHolder;
    
   
    
    @GET
    @Path("parentId")
    public PartyEntityDTO getParentId(@QueryParam("userId")long userId) {
    	//long userId = 808573832183808L;
    	PartyEntity vo = partyEntityManager.get(userId);
    	PartyStruct partyStruct = partyStructManager.findUniqueBy("childEntity",vo);
    	//List<PartyType> partyTypes = partyTypeManager.getAll();
    	PartyEntity pvo = partyStruct.getParentEntity();
		PartyEntityDTO partyEntityDto = new PartyEntityDTO();
		partyEntityDto.setShortName(pvo.getShortName());
    	return partyEntityDto;
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



	// ~ ==================================================
   
    public static class PartyEntityDTO {
        private long id;
        private String name;
        private String shortName;
        
       

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

		public String getShortName() {
			return shortName;
		}

		public void setShortName(String shortName) {
			this.shortName = shortName;
		}
        

       
    }
    public static class PartyStructDTO {
    	private long childEntityId;
    	private long parentEntityId;
    	
    	
    	public long getChildEntityId() {
    		return childEntityId;
    	}
    	
    	public void setChildEntityId(long childEntityId) {
    		this.childEntityId = childEntityId;
    	}

		public long getParentEntityId() {
			return parentEntityId;
		}

		public void setParentEntityId(long parentEntityId) {
			this.parentEntityId = parentEntityId;
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
