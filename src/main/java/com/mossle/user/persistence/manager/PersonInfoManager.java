package com.mossle.user.persistence.manager;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import com.mossle.api.dict.DictConnector;
import com.mossle.api.org.OrgConnector;
import com.mossle.api.user.PersonInfoDTO;
import com.mossle.core.auth.CurrentUserHolder;
import com.mossle.core.hibernate.HibernateUtils;
import com.mossle.core.hibernate.HibernateEntityDao;
import com.mossle.core.mapper.BeanMapper;
import com.mossle.core.query.PropertyFilter;
import com.mossle.core.util.StringUtils;
import com.mossle.party.persistence.domain.PartyEntity;
import com.mossle.user.persistence.domain.PersonInfo;
import com.mossle.user.persistence.domain.PersonInfoDTOForExport;
import com.mossle.worktask.persistence.domain.WorkTaskInfo;
import com.mossle.worktask.persistence.domain.WorkTaskInfoInstance;

import org.springframework.stereotype.Service;

@Service
public class PersonInfoManager extends HibernateEntityDao<PersonInfo> {
	
	private CurrentUserHolder currentUserHolder;
	
	private OrgConnector orgConnector ;
	
	private BeanMapper beanMapper = new BeanMapper();
	
	private DictConnector dictConnector;
	
	 /**
     * 导出任务数据转换
     */
    public List<PersonInfoDTOForExport> exportInfo(List<PersonInfo> workTaskInfoList) {
    
        List<PersonInfoDTOForExport> listResult = new ArrayList<PersonInfoDTOForExport>();

        for (PersonInfo workTaskInfo : workTaskInfoList) {
        	
        	PersonInfoDTOForExport personInfoDTOForExport = new PersonInfoDTOForExport();
        	
        	beanMapper.copy(workTaskInfo, personInfoDTOForExport);
        	
        	
        	if(personInfoDTOForExport.getMarriage()!=null&&personInfoDTOForExport.getMarriage().equals("1")){
        		personInfoDTOForExport.setMarriage("已婚");
        	}else if(personInfoDTOForExport.getMarriage()!=null&&personInfoDTOForExport.getMarriage().equals("2")){
        		personInfoDTOForExport.setMarriage("未婚");
        	}
        	
        	if(personInfoDTOForExport.getFertilityCondition()!=null&&personInfoDTOForExport.getFertilityCondition().equals("1")){
        		personInfoDTOForExport.setFertilityCondition("已育");
        	}else if(personInfoDTOForExport.getFertilityCondition()!=null&&personInfoDTOForExport.getFertilityCondition().equals("2")){
        		personInfoDTOForExport.setFertilityCondition("未育");
        	}
        	
        	if(personInfoDTOForExport.getGender()!=null&&personInfoDTOForExport.getGender().equals("1")){
        		personInfoDTOForExport.setGender("男");
        	}else if(personInfoDTOForExport.getGender()!=null&&personInfoDTOForExport.getGender().equals("2")){
        		personInfoDTOForExport.setGender("女");
        	}
        	
        	if(!StringUtils.isBlank(personInfoDTOForExport.getStopFlag())){
        			if(personInfoDTOForExport.getStopFlag().equals("active"))
        				personInfoDTOForExport.setStopFlag("启用");
    				else if(personInfoDTOForExport.getStopFlag().equals("disabled"))
    					personInfoDTOForExport.setStopFlag("禁用");
        	}
        	
        	String postName = "";
        	List<PartyEntity> partyDTO = orgConnector.getPostContainDELFlagByUserId(personInfoDTOForExport.getId().toString());
	        if (partyDTO.size() > 0) {
	           for (int i = 0; i < partyDTO.size(); i++) {
	        	   postName = postName + partyDTO.get(i).getName()+"  ";
	           }
	        } 
	        personInfoDTOForExport.setPostName(postName);
	        
	        if(!StringUtils.isBlank(personInfoDTOForExport.getPositionCode()))
	        	personInfoDTOForExport.setPositionName(dictConnector.findDictNameByValue("StaffPosition", personInfoDTOForExport.getPositionCode()));
	        
	        if(!StringUtils.isBlank(personInfoDTOForExport.getLevel()))
	        	personInfoDTOForExport.setLevel(personInfoDTOForExport.getLevel().contains("级-")?personInfoDTOForExport.getLevel():(personInfoDTOForExport.getLevel().replace("-","级-")));
	        
	        //民族
	        personInfoDTOForExport.setNation(dictConnector.findDictNameByValue("nation",personInfoDTOForExport.getNation()));
	        //户籍类型
	        personInfoDTOForExport.setHouseholdRegisterType(dictConnector.findDictNameByValue("householdRegisterType",personInfoDTOForExport.getHouseholdRegisterType()));
	        //政治面貌
	        personInfoDTOForExport.setPoliticalOutlook(dictConnector.findDictNameByValue("politicalOutlook",personInfoDTOForExport.getPoliticalOutlook()));
	        //学位
	        personInfoDTOForExport.setAcademicDegree(dictConnector.findDictNameByValue("academicDegree",personInfoDTOForExport.getAcademicDegree()));
	        //用工类型
	        personInfoDTOForExport.setLaborType(dictConnector.findDictNameByValue("laborType",personInfoDTOForExport.getLaborType()));
	        //进入方式
	        personInfoDTOForExport.setEntryMode(dictConnector.findDictNameByValue("entryMode",personInfoDTOForExport.getEntryMode()));
	        //学历
	        personInfoDTOForExport.setEducation(dictConnector.findDictNameByValue("education",personInfoDTOForExport.getEducation()));
	        listResult.add(personInfoDTOForExport);
        }
        return listResult;
    }
	
    @Resource
	public void setOrgConnector(OrgConnector orgConnector) {
		this.orgConnector = orgConnector;
	}	

    @Resource
    public void setCurrentUserHolder(CurrentUserHolder currentUserHolder) {
        this.currentUserHolder = currentUserHolder;
    }
	
    @Resource
    public void setDictConnector(DictConnector dictConnector) {
        this.dictConnector = dictConnector;
    }
}
