package com.mossle.humantask.listener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.mossle.api.dict.DictConnector;
import com.mossle.api.humantask.HumanTaskConstants;
import com.mossle.api.keyvalue.KeyValueConnector;
import com.mossle.api.keyvalue.Record;
import com.mossle.api.org.OrgConnector;
import com.mossle.api.org.OrgDTO;
import com.mossle.api.party.PartyConnector;
import com.mossle.api.party.PartyDTO;
import com.mossle.common.utils.StringUtils;
import com.mossle.dict.persistence.domain.DictInfo;
import com.mossle.humantask.persistence.domain.TaskInfo;
import com.mossle.humantask.persistence.domain.TaskInfoApprovePosition;
import com.mossle.humantask.persistence.domain.TaskParticipant;
import com.mossle.humantask.persistence.manager.TaskInfoManager;
import com.mossle.humantask.persistence.manager.TaskParticipantManager;
import com.mossle.party.PartyConstants;
import com.mossle.party.persistence.domain.PartyEntity;
import com.mossle.party.persistence.domain.PartyStruct;
import com.mossle.party.persistence.manager.PartyEntityManager;
import com.mossle.spi.humantask.TaskDefinitionConnector;
import com.mossle.spi.humantask.TaskUserDTO;
import com.mossle.spi.process.InternalProcessConnector;


















// import org.hsqldb.lib.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

public class TaskDefUserHumanTaskListener implements HumanTaskListener {
    private static Logger logger = LoggerFactory
            .getLogger(TaskDefUserHumanTaskListener.class);
    private TaskDefinitionConnector taskDefinitionConnector;
    private InternalProcessConnector internalProcessConnector;
    private TaskParticipantManager taskParticipantManager;
    private OrgConnector orgConnector;
    private PartyEntityManager partyEntityManager;
    private DictConnector dictConnector;
    private KeyValueConnector keyValueConnector;

    
    @Override
    public void onCreate(TaskInfo taskInfo) throws Exception {
        if (HumanTaskConstants.CATALOG_COPY.equals(taskInfo.getCatalog())) {
            return;
        }

        String taskDefinitionKey = taskInfo.getCode();
        String processDefinitionId = taskInfo.getProcessDefinitionId();
        List<TaskUserDTO> taskUsers = taskDefinitionConnector.findTaskUsers(taskDefinitionKey, processDefinitionId);
        
       /* // 判断流程中是否已经存在完成的ActivityId，如果存在，说明这次任务是驳回任务。驳回时直接给次任务的操作人，避免出现待领发生
        String hql ="from TaskInfo where status='complete' and CATALOG <>'copy' and code =? and businessKey=? order by completeTime desc";
        List<TaskInfo> taskInfoList = taskInfoManager.find(hql, taskInfo.getCode(),taskInfo.getBusinessKey());
        if (taskInfoList !=null && taskInfoList.size() >0) {
        	TaskInfo historyTaskInfo = taskInfoList.get(0);
        	taskInfo.setAssignee(historyTaskInfo.getAssignee());
        } else {   // 正常流程
*/	        for (TaskUserDTO taskUser : taskUsers) {
	            String catalog = taskUser.getCatalog();
	            String type = taskUser.getType();
	            String value = taskUser.getValue();
	            //assignee：负责人，candidate：候选人/候选组，areacandidate：大区对接人 大区候选组
	            //sameareacandidate：同一大区
	            if ("assignee".equals(catalog)) { 
	            	
	            	String postId = "";
	            	boolean blnTask = true;   // 判断是否走待领还是直接分配任务
	            	List<PartyEntity> userList = new ArrayList<PartyEntity>();
	            	
            		if(value.indexOf("岗位:")>-1){
            			postId = value.split(":")[1];
	            		
	            		userList = orgConnector.getUsersByPostId(Long.parseLong(postId));
	            		if (userList.size() == 1) {
	            			blnTask = false;
	            		}
	            		
	            		//走待领列表
	            		if (blnTask) {
			                TaskParticipant taskParticipant = new TaskParticipant();
			                taskParticipant.setCategory("candidate");
			                
			                // zyl 2017-07-25 
			                if (value.indexOf("岗位:")!=-1) {  
			                	taskParticipant.setRef(value.split(":")[1]);
			                } else { 
			                	taskParticipant.setRef(value);
			                } 
			                
			                taskParticipant.setType(type);
			                taskParticipant.setTaskInfo(taskInfo);
			                taskParticipantManager.save(taskParticipant);
		            	} else {
		            		taskInfo.setAssignee(Long.toString(userList.get(0).getId()));
		            	}
            		} else {
            			taskInfo.setAssignee(value);
            		}
            		
	            } else if ("candidate".equals(catalog)) {   // 候选人
	            	String postId = "";
	            	boolean blnTask = true;   // 判断是否走待领还是直接分配任务
	            	List<PartyEntity> userList = new ArrayList<PartyEntity>();
	            	
	            	if (value.indexOf("岗位:")!=-1) {
	            		postId = value.split(":")[1];
	            		
	            		userList = orgConnector.getUsersByPostId(Long.parseLong(postId));
	            		if (userList.size() == 1) {
	            			blnTask = false;
	            		}
	            	}
	            	
	            	if (blnTask) {
		                TaskParticipant taskParticipant = new TaskParticipant();
		                taskParticipant.setCategory(catalog);
		                
		                // zyl 2017-07-25 
		                if (value.indexOf("岗位:")!=-1) {  
		                	taskParticipant.setRef(value.split(":")[1]);
		                } else { 
		                	taskParticipant.setRef(value);
		                } 
		                
		                taskParticipant.setType(type);
		                taskParticipant.setTaskInfo(taskInfo);
		                taskParticipantManager.save(taskParticipant);
	            	} else {
	            		taskInfo.setAssignee(Long.toString(userList.get(0).getId()));
	            	}
	            } else if ("areacandidate".equals(catalog)) {    //大区对接人  大区候选组
	            	String postId = "";
	            	String userId = "";
	            	boolean blnTask = true;   // 判断是否走待领还是直接分配任务
	            	List<PartyEntity> userList = new ArrayList<PartyEntity>();
	            	Map<String,String> map = new HashMap<String,String>();
	            	
	            	if (value.indexOf("岗位:")!=-1) {
	            		postId = value.split(":")[1];
	            		
	            		userList = orgConnector.getUsersByPostId(Long.parseLong(postId));
	            		// 取得user负责的大区
	            		List<DictInfo> dictInfoList = dictConnector.findDictInfoListByType("dataAuthority");
	            		for (PartyEntity vo : userList) {
	            			for (DictInfo dictInfo : dictInfoList) {
	            				if (Long.toString(vo.getId()).equals(dictInfo.getValue().split("-")[0])) {
	            					map.put(Long.toString(vo.getId()), dictInfo.getValue().split("-")[1]);
	            				}
	            			}
	            		}
	            		
	            		/*// 取得流程发起人
	            		String startUserId = internalProcessConnector.findInitiator(processInstanceId);
	            		// 发起人对应的大区
	            		PartyEntity partyEntity = orgConnector.findPartyAreaByUserId(startUserId);*/
	            		
	            		Record record = keyValueConnector.findByCode(taskInfo.getBusinessKey());
	            		
	            		// 流程 对应的大区
	            		PartyEntity partyEntity = partyEntityManager.get(Long.parseLong(record.getAreaId()));
	            		
	            		if (partyEntity != null) {
		            		Iterator<Map.Entry<String, String>> entries = map.entrySet().iterator();  
		            		while (entries.hasNext()) {  
		            		  
		            		    Map.Entry<String, String> entry = entries.next();  
		            		    
		            		    if (entry.getValue().indexOf(Long.toString(partyEntity.getId())) != -1) {
		            		    	userId += entry.getKey() + ",";
		            		    	blnTask = false ;
		            		    }
		            		  
		            		}  
	            		}
	            		
	            		if(blnTask) {
	    	                TaskParticipant taskParticipant = new TaskParticipant();
	    	                taskParticipant.setCategory(catalog);
	    	                
	    	                // zyl 2017-07-25 
	    	                if (value.indexOf("岗位:")!=-1) {  
	    	                	taskParticipant.setRef(value.split(":")[1]);
	    	                } else { 
	    	                	taskParticipant.setRef(value);
	    	                } 
	    	                
	    	                taskParticipant.setType(type);
	    	                taskParticipant.setTaskInfo(taskInfo);
	    	                taskParticipantManager.save(taskParticipant);
	                	} else {
	                		String[] strTemp = userId.split(",");
	                		if (strTemp.length == 1) {
	                			taskInfo.setAssignee(strTemp[0]);
	                		} else {
	                			for (String userid : strTemp) {
	                				TaskParticipant taskParticipant = new TaskParticipant();
	            	                taskParticipant.setCategory(catalog);
	
	        	                	taskParticipant.setRef(userid);
	
	            	                taskParticipant.setType(type);
	            	                taskParticipant.setTaskInfo(taskInfo);
	            	                taskParticipantManager.save(taskParticipant);
	                			}
	                		}
	                	}
	            	}
	            } else if ("sameareacandidate".equals(catalog)) {    //同一大区
	            	String postId = "";
	            	String userId = "";
	            	boolean blnTask = true;   // 判断是否走待领还是直接分配任务
	            	List<PartyEntity> userList = new ArrayList<PartyEntity>();
	            	// Map<String,String> map = new HashMap<String,String>();
	            	
	            	if (value.indexOf("岗位:")!=-1) {
	            		postId = value.split(":")[1];
	            		
	            		userList = orgConnector.getUsersByPostId(Long.parseLong(postId));
	            		
	            		// 获取发起人所在大区
	            		PartyEntity partyEntity = getStartCompanyOrArea(taskInfo,"1");
	            		
	            		if (partyEntity != null) {
		            		for(PartyEntity vo : userList) {  
		            			PartyEntity areaVo = orgConnector.findPartyAreaByUserId(Long.toString(vo.getId()));

		            		    if (areaVo != null && areaVo.getId().equals(partyEntity.getId())) {
		            		    	userId += Long.toString(vo.getId()) + ",";
		            		    	blnTask = false ;
		            		    }
		            		}
		            		
		            		// 兼职情况   zyl 2018-03-29
	            			if (blnTask) {
		            			userlist:for(PartyEntity vo : userList) {
				            		// 通过人找岗位
		            				for (PartyStruct partyStruct : vo.getParentStructs()) {
		            					if (partyStruct.getPartyStructType().getId() == 4) {
		            						PartyEntity areaVo = orgConnector.findPartyAreaByUserId(Long.toString(vo.getId()));

		    		            		    if (areaVo != null && areaVo.getId().equals(partyEntity.getId())) {
		    		            		    	userId += Long.toString(vo.getId()) + ",";
		    		            		    	blnTask = false ;
		    		            		    	break userlist;
		    		            		    }
		            					}
		            				}
		            		    }
	            			}
	            		} else {   // 总部的人发起
	            			Record record = keyValueConnector.findByCode(taskInfo.getBusinessKey());
	                		
	                		// 流程 对应的大区
	                		partyEntity = partyEntityManager.get(Long.parseLong(record.getAreaId()));
	                		if (partyEntity != null) {
	    	            		for(PartyEntity vo : userList) {  
	    	            			// System.out.println("partyEntity," + partyEntity.getId());
	    	            			// System.out.println(vo.getName()+ "," + vo.getId());
	    	            			PartyEntity areaVo = orgConnector.findPartyAreaByUserId(Long.toString(vo.getId()));
	    	            		    if (areaVo != null && areaVo.getId().equals(partyEntity.getId())) {
	    	            		    	userId += Long.toString(vo.getId()) + ",";
	    	            		    	blnTask = false ;
	    	            		    }
	    	            		}  
	                		}
	                		
	                		// 兼职情况   zyl 2018-03-29
	            			if (blnTask) {
		            			userlist:for(PartyEntity vo : userList) {
				            		// 通过人找岗位
		            				for (PartyStruct partyStruct : vo.getParentStructs()) {
		            					if (partyStruct.getPartyStructType().getId() == 4) {
		            						PartyEntity areaVo = orgConnector.findPartyAreaByUserId(Long.toString(vo.getId()));

		    		            		    if (areaVo != null && areaVo.getId().equals(partyEntity.getId())) {
		    		            		    	userId += Long.toString(vo.getId()) + ",";
		    		            		    	blnTask = false ;
		    		            		    	break userlist;
		    		            		    }
		            					}
		            				}
		            		    }
	            			}
	            		}
	            		
	            		if (blnTask) {
	    	                TaskParticipant taskParticipant = new TaskParticipant();
	    	                taskParticipant.setCategory(catalog);
	    	                
	    	                // zyl 2017-07-25 
	    	                if (value.indexOf("岗位:")!=-1) {  
	    	                	taskParticipant.setRef(value.split(":")[1]);
	    	                } else { 
	    	                	taskParticipant.setRef(value);
	    	                } 
	    	                
	    	                taskParticipant.setType(type);
	    	                taskParticipant.setTaskInfo(taskInfo);
	    	                taskParticipantManager.save(taskParticipant);
	                	} else {
	                		String[] strTemp = userId.split(",");
	                		if (strTemp.length == 1) {
	                			taskInfo.setAssignee(strTemp[0]);
	                		} else {
	                			for (String userid : strTemp) {
	                				TaskParticipant taskParticipant = new TaskParticipant();
	            	                taskParticipant.setCategory(catalog);
	
	        	                	taskParticipant.setRef(userid);
	
	            	                taskParticipant.setType(type);
	            	                taskParticipant.setTaskInfo(taskInfo);
	            	                taskParticipantManager.save(taskParticipant);
	                			}
	                		}
	                	}
	            	}
	            } else if ("samecompanycandidate".equals(catalog)) {    //同一分公司
	            	String postId = "";
	            	String userId = "";
	            	boolean blnTask = true;   // 判断是否走待领还是直接分配任务
	            	List<PartyEntity> userList = new ArrayList<PartyEntity>();
	            	
	            	if (value.indexOf("岗位:")!=-1) {
	            		postId = value.split(":")[1];
	            		
	            		userList = orgConnector.getUsersByPostId(Long.parseLong(postId));
	            		
	            		// 获取发起人所在公司
	            		PartyEntity partyEntity =getStartCompanyOrArea(taskInfo,"0");
	            		
	            		if (partyEntity != null &&(!partyEntity.getName().equals("罗麦科技") && !partyEntity.getName().equals("罗麦集团"))) {
	            			
	            			// 隶属关系
	            			for(PartyEntity vo : userList) {
		            		  
		            			//PartyDTO companyVo = partyConnector.findCompanyById(Long.toString(vo.getId()));
		            			PartyEntity companyVo = orgConnector.findPartyCompanyByUserId(Long.toString(vo.getId()));
		            		    if (partyEntity.getId().equals(companyVo.getId())) {
		            		    	
		            		    	userId += Long.toString(vo.getId()) + ",";
		            		    	blnTask = false;
		            		    }
	            		    }

	            			// 兼职情况   zyl 2018-03-29
	            			if (blnTask) {
		            			userlist:for(PartyEntity vo : userList) {
				            		// 通过人找岗位
		            				for (PartyStruct partyStruct : vo.getParentStructs()) {
		            					
		            					// System.out.println("=====" + vo.getName() + "," + partyStruct.getParentEntity().getName());
		            					if (partyStruct.getPartyStructType().getId() == 4) {
		            						//PartyDTO companyVo = partyConnector.findCompanyById(Long.toString(partyStruct.getParentEntity().getId()));
					            			PartyEntity companyVo = orgConnector.findPartyCompanyByUserId(Long.toString(partyStruct.getParentEntity().getId()));
					            		    if (partyEntity.getId().equals(companyVo.getId())) {
					            		    	
					            		    	userId += Long.toString(vo.getId()) + ",";
					            		    	blnTask = false;
					            		    	break userlist;
					            		    }
		            					}
		            				}
		            		    }
	            			}
	            		} else {//科技总部的人发起的流程
	            			Record record = keyValueConnector.findByCode(taskInfo.getBusinessKey());
	                		// 流程 对应的分公司
	                		PartyEntity company = partyEntityManager.get(Long.parseLong(record.getCompanyId()));

	                		if (company != null) {
	                			// 组织关系
	    	            		for(PartyEntity vo : userList) {
	    	            			//PartyDTO partyDTO =  partyConnector.findCompanyById(Long.toString(vo.getId()));
	    	            			PartyEntity partyDTO = orgConnector.findPartyCompanyByUserId(Long.toString(vo.getId()));
    	            				if (partyDTO != null && company.getId().equals(partyDTO.getId())) {
	    	            		    	userId += Long.toString(vo.getId()) + ",";
	    	            		    	blnTask = false ;
	    	            		    }
	    	            		}
	    	            		
	    	            		// 管理关系     找不到组织关系，在查找管理关系
	    	            		if (blnTask) {
	    	            			for(PartyEntity vo : userList) {
		    	            			List<Long> list =  orgConnector.getPartyByManageId(Long.toString(vo.getId()));
	    	            				if (list != null && list.size() > 0) {
	    	            					for(Long id : list) {
	    	            						if (company.getId().equals(id)) {
	    	            							userId += Long.toString(vo.getId()) + ",";
	    	            							blnTask = false ;
	    	            							break;
	    	            						}
	    	            					}
		    	            		    }
		    	            		}
	    	            		}
	    	            		
	    	            		// 兼职情况   zyl 2018-03-29
		            			if (blnTask) {
			            			userlist:for(PartyEntity vo : userList) {
					            		// 通过人找岗位
			            				for (PartyStruct partyStruct : vo.getParentStructs()) {
			            					
			            					if (partyStruct.getPartyStructType().getId() == 4) {
			            						//PartyDTO companyVo = partyConnector.findCompanyById(Long.toString(partyStruct.getParentEntity().getId()));
						            			PartyEntity companyVo = orgConnector.findPartyCompanyByUserId(Long.toString(partyStruct.getParentEntity().getId()));
						            		    if (company.getId().equals(companyVo.getId())) {
						            		    	
						            		    	userId += Long.toString(vo.getId()) + ",";
						            		    	blnTask = false;
						            		    	break userlist;
						            		    }
			            					}
			            				}
			            		    }
		            			}
	    	            		
	                		}
	                		
	                		
	            		}
	            		
	            		if (blnTask) {
	    	                TaskParticipant taskParticipant = new TaskParticipant();
	    	                taskParticipant.setCategory(catalog);
	    	                
	    	                // zyl 2017-07-25 
	    	                if (value.indexOf("岗位:")!=-1) {  
	    	                	taskParticipant.setRef(value.split(":")[1]);
	    	                } else { 
	    	                	taskParticipant.setRef(value);
	    	                } 
	    	                
	    	                taskParticipant.setType(type);
	    	                taskParticipant.setTaskInfo(taskInfo);
	    	                taskParticipantManager.save(taskParticipant);
	                	} else {
	                		String[] strTemp = userId.split(",");
	                		if (strTemp.length == 1) {
	                			taskInfo.setAssignee(strTemp[0]);
	                		} else {
	                			for (String userid : strTemp) {
	                				TaskParticipant taskParticipant = new TaskParticipant();
	            	                taskParticipant.setCategory(catalog);
	
	        	                	taskParticipant.setRef(userid);
	
	            	                taskParticipant.setType(type);
	            	                taskParticipant.setTaskInfo(taskInfo);
	            	                taskParticipantManager.save(taskParticipant);
	                			}
	                		}
	                	}
	            	}
	            }
	        }
        // }
    }

    /**
     * 获取大区或则公司
     * strType，0：公司，1：大区
     * **/
	private PartyEntity getStartCompanyOrArea(TaskInfo taskInfo,String strType) {
		String processInstanceId = taskInfo.getProcessInstanceId();
		
		// 获取发起人岗位
		PartyEntity partyEntity = null; // getStartCompany(taskInfo,processInstanceId);
		
		
		Record record = keyValueConnector.findByCode(taskInfo.getBusinessKey());
		if (record != null) {
			String tempPostId = record.getStartPositionId();
			if (StringUtils.isNotBlank(tempPostId)) {   // 通过岗位查找发起公司
				if(strType.equals("0"))
					partyEntity = orgConnector.findPartyCompanyByUserId(tempPostId);
				else {
					partyEntity = orgConnector.findPartyAreaByUserId(tempPostId);
				}
			} else {
				// 取得流程发起人
				String startUserId = internalProcessConnector.findInitiator(processInstanceId);
				// 发起人对应的公司
				//PartyDTO partyEntity = partyConnector.findCompanyById(startUserId);
				
				if(strType.equals("0"))
					partyEntity = orgConnector.findPartyCompanyByUserId(startUserId);
				else {
					partyEntity = orgConnector.findPartyAreaByUserId(startUserId);
				}
			}
			
		} else {
			// 取得流程发起人
			String startUserId = internalProcessConnector.findInitiator(processInstanceId);
			// 发起人对应的公司
			//PartyDTO partyEntity = partyConnector.findCompanyById(startUserId);
			if(strType.equals("0"))
				partyEntity = orgConnector.findPartyCompanyByUserId(startUserId);
			else {
				partyEntity = orgConnector.findPartyAreaByUserId(startUserId);
			}
		}
		return partyEntity;
	}

    @Override
    public void onComplete(TaskInfo taskInfo) throws Exception {
    }

    @Resource
    public void setTaskDefinitionConnector(
            TaskDefinitionConnector taskDefinitionConnector) {
        this.taskDefinitionConnector = taskDefinitionConnector;
    }

    @Resource
    public void setTaskParticipantManager(
            TaskParticipantManager taskParticipantManager) {
        this.taskParticipantManager = taskParticipantManager;
    }
    
    @Resource
    public void setPartyEntityManager(
    		PartyEntityManager partyEntityManager) {
        this.partyEntityManager = partyEntityManager;
    }

	@Resource
	public void setOrgConnector(OrgConnector orgConnector) {
		this.orgConnector = orgConnector;
	}
    
	@Resource
    public void setInternalProcessConnector(
            InternalProcessConnector internalProcessConnector) {
        this.internalProcessConnector = internalProcessConnector;
    }

	@Resource
	public void setDictConnector(DictConnector dictConnector) {
		this.dictConnector = dictConnector;
	}

	@Resource
	public void setKeyValueConnector(KeyValueConnector keyValueConnector) {
		this.keyValueConnector = keyValueConnector;
	}
}
