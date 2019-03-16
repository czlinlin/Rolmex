package com.mossle.operation.rs;

import java.io.IOException;
import java.io.StringReader;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.rpc.holders.StringHolder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.graphbuilder.math.func.AtanFunction;
import com.mossle.api.operation.OperationConnector;
import com.mossle.api.org.OrgConnector;
import com.mossle.api.party.PartyConnector;
import com.mossle.api.party.PartyDTO;
import com.mossle.api.party.PartyEntityOrgDTO;
import com.mossle.api.user.UserConnector;
import com.mossle.api.user.UserDTO;
import com.mossle.base.persistence.domain.BusinessDetailEntity;
import com.mossle.base.persistence.manager.BusinessDetailManager;
import com.mossle.bpm.persistence.domain.BpmProcess;
import com.mossle.bpm.persistence.manager.BpmProcessManager;
import com.mossle.common.utils.PasswordUtil;
import com.mossle.core.auth.CurrentUserHolder;
import com.mossle.core.auth.CustomPasswordEncoder;
import com.mossle.core.util.BaseDTO;
import com.mossle.core.util.StringUtils;
import com.mossle.operation.service.ApplyService;
import com.mossle.operation.service.CustomWorkService;
import com.mossle.operation.support.CustomerInfoDTO;
import com.mossle.operation.support.OperationConnectorImpl;
import com.mossle.party.PartyConstants;
import com.mossle.party.persistence.domain.PartyEntity;
import com.mossle.party.persistence.domain.PartyStruct;
import com.mossle.party.persistence.domain.PartyType;
import com.mossle.party.persistence.manager.PartyEntityManager;
import com.mossle.party.persistence.manager.PartyStructManager;
import com.mossle.party.persistence.manager.PartyTypeManager;
import com.mossle.party.service.PartyService;
import com.mossle.user.persistence.domain.AccountCredential;
import com.mossle.user.persistence.domain.AccountInfo;
import com.mossle.user.persistence.manager.AccountCredentialManager;
import com.mossle.user.persistence.manager.AccountInfoManager;
import com.mossle.user.support.ChangePasswordResult;
import com.mossle.util.StringUtil;
import com.mossle.ws.oaclient.GetUserInfoResponseGetUserInfoResult;
import com.mossle.ws.oaclient.ToOAServiceSoapProxy;
import com.mossle.ws.oaclient.holders.GetUserInfoResponseGetUserInfoResultHolder;

import org.activiti.engine.impl.cmd.AddCommentCmd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

@Component
@Path("customer")
public class CustomerInfoResource {
    private static Logger logger = LoggerFactory.getLogger(CustomerInfoResource.class);
   
    private String oaUrl;
	private String oaKey;
	private CurrentUserHolder currentUserHolder;
	private AccountInfoManager accountInfoManager;
    private CustomPasswordEncoder customPasswordEncoder;
    private AccountCredentialManager accountCredentialManager;
    private JdbcTemplate jdbcTemplate;
    private PartyConnector partyConnector;
    private PartyEntityManager partyEntityManager;
    @Autowired
    private CustomWorkService customWorkService;
    @Autowired
    private BusinessDetailManager businessDetailManager;
    @Autowired
    private BpmProcessManager bpmProcessManager;
    @Autowired
    private OperationConnector operationConnector;
    @Autowired
    private ApplyService applyService;
    @GET
    @Path("customerInfo")
    @Produces(MediaType.APPLICATION_JSON)
    public CustomerInfoDTO getAllPartyTypes(@QueryParam("customerInfoId") String customerInfoId) {   
        return getUserInfo(customerInfoId);
    }
    
    /**
     * 通过Oa webservice 获取客户信息
     * @return
     */
    private CustomerInfoDTO getUserInfo(String customerInfoId) {
    	
    	String enpoint = oaUrl;
    	String key = oaKey;
    	
    	CustomerInfoDTO customerInfoDTO = new CustomerInfoDTO();
		ToOAServiceSoapProxy to = new ToOAServiceSoapProxy(enpoint);
		
		// String strUserID = "13061907";
		
		String strMsg = PasswordUtil.md5Hex(customerInfoId + key);  
		StringHolder error = new StringHolder();
		
		GetUserInfoResponseGetUserInfoResultHolder getUserInfoResult = new GetUserInfoResponseGetUserInfoResultHolder();
		
		try {
			to.getUserInfo(customerInfoId, strMsg, getUserInfoResult, error);
			
			GetUserInfoResponseGetUserInfoResult a = getUserInfoResult.value;
			
			if (a == null) {
				return customerInfoDTO;
			}
			String  b = a.get_any()[1].toString();
			
			//1。获取DOM 解析器的工厂实例。  
            DocumentBuilderFactory factory  = DocumentBuilderFactory.newInstance();  
            //2。获得具体的DOM解析器。  
            DocumentBuilder builder = null;
			try {
				builder = factory.newDocumentBuilder();
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}  
              
            //3。获取文件  
            try {
            	StringReader sr = new StringReader(b); 
            	InputSource is = new InputSource(sr);
				Document document = builder.parse(is);
				
				Element root = document.getDocumentElement();  
	            // System.out.println("cat="+root.getAttribute("cat")); 
	            
	            NodeList list = root.getElementsByTagName("DocumentElement");  
	            
	            Map<String,Object> map = new HashMap<String,Object>(); 
	            
	            for (int i = 0; i < list.getLength(); i++) {  
	                Node lan = list.item(i);  
	                // System.out.println("id="+lan.getNodeType());  
	                // System.out.println("---------------");  
	                // Element lan =  (Element) list.item(i);  
	              
	                //获取子节点集合  
	                NodeList clist = lan.getChildNodes();  
	                for (int j = 0; j < clist.getLength(); j++) {  
	                    //获取下标  
	                    Node c = clist.item(j);  
	                    NodeList slist = c.getChildNodes();
	                    
	                    for (int k = 0; k < slist.getLength(); k++) {  
	                    	Node n = slist.item(k);
	                    	
	                    	if (n instanceof Element) {
	                    		// System.out.println(n.getNodeName()+"="+n.getTextContent());  
	                    		
	                    		map.put(n.getNodeName(), n.getTextContent()); 
	                    		if ("varUserID".equals(n.getNodeName())) {
	                    			customerInfoDTO.setId(n.getTextContent());
	                    		}
	                    		if ("varRealName".equals(n.getNodeName())) {
	                    			customerInfoDTO.setName(n.getTextContent());
	                    		}
	                    	}
	                    }
	                    
	                }  
	            } 
	            ObjectMapper mapper = new ObjectMapper(); 
	            
	            String strjson = mapper.writeValueAsString(map); 
	            
	            return customerInfoDTO;
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
    }
    
    //验证密码是否正确
    @GET
    @Path("opteraion-verifyPassword")
    public BaseDTO VerifyPassword(@QueryParam("pwd")  String pwd){
    	BaseDTO baseDTO=new BaseDTO();
    	if(StringUtils.isBlank(pwd)){
    		baseDTO.setMessage("请输入操作密码");
     	    baseDTO.setCode(201);
     	    return baseDTO;
    	}
       if(currentUserHolder.getUserId()==null){
    	   baseDTO.setMessage("登录超时，请重新登录");
    	   baseDTO.setCode(501);
    	   return baseDTO;
       }
       if(currentUserHolder.getUserId().equals("")){
    	   baseDTO.setMessage("用户信息错误，请重新登录");
    	   baseDTO.setCode(502);
    	   return baseDTO;
       }
       
	   Long accountId = Long.parseLong(currentUserHolder.getUserId());
	   AccountInfo accountInfo = accountInfoManager.get(accountId);
	   String hql = "from AccountCredential where accountInfo=? and catalog='default'";
	   AccountCredential accountCredential = accountCredentialManager.findUnique(hql, accountInfo);
	   ChangePasswordResult changePasswordResult = new ChangePasswordResult();
	   
	   if (!isPasswordValid(pwd, accountCredential.getOperationPassword())) {
            changePasswordResult.setCode("user.user.input.passwordnotcorrect");
            changePasswordResult.setMessage("密码错误");{
            	baseDTO.setMessage("操作密码错误");
         	    baseDTO.setCode(201);
         	    return baseDTO;
            }
        }else {
        	baseDTO.setMessage("验证通过");
     	    baseDTO.setCode(200);
     	    return baseDTO;
        }
    }
    
    /**
     * 得到发起人的岗位
     * add by lilei 2018-09-11
     * */
    @GET
    @Path("opteraion-getposition")
    public BaseDTO getStartProcessPosition(@QueryParam("businessDetailId")  String businessDetailId){
    	String userId=currentUserHolder.getUserId();
    	BaseDTO baseDTO=new BaseDTO();
    	try{
    		if(StringUtils.isBlank(businessDetailId))
	        	return null;
    		
    		if(currentUserHolder.getUserId()==null){
	     	   baseDTO.setMessage("登录超时，请重新登录");
	     	   baseDTO.setCode(501);
	     	   return baseDTO;
	        }
	        if(currentUserHolder.getUserId().equals("")){
	     	   baseDTO.setMessage("用户信息错误，请重新登录");
	     	   baseDTO.setCode(502);
	     	   return baseDTO;
	        }
	        
	        BusinessDetailEntity businessDetailEntity=businessDetailManager.get(Long.valueOf(businessDetailId));
	        if(businessDetailEntity==null){
	        	baseDTO.setMessage("未查询到具体流程细分，请检查");
	     	    baseDTO.setCode(502);
	     	    return baseDTO;
	        }
	        
	        if(StringUtils.isBlank(businessDetailEntity.getBpmProcessId())){
	        	baseDTO.setMessage("流程细分未配置具体流程模型，请检查");
	     	    baseDTO.setCode(502);
	     	    return baseDTO;
	        }
	        
	        BpmProcess bpmProcess=bpmProcessManager.get(Long.parseLong(businessDetailEntity.getBpmProcessId()));
	        if(bpmProcess==null){
	        	baseDTO.setMessage("获取具体流程信息错误，请检查流程细分配置");
	     	    baseDTO.setCode(502);
	     	    return baseDTO;
	        }
	        
	        List<Map<String,Object>> mapPositionInfoList=new ArrayList<Map<String,Object>>();
	        if(bpmProcess.getBpmCategory()!=null){
	        	if(bpmProcess.getBpmCategory().getId()!=null&&bpmProcess.getBpmCategory().getId()==3L){
	        		if(operationConnector.IsShowCommonProcess(bpmProcess.getId(), userId)) {
	        			Map<String,Object> map=new HashMap<String, Object>();
						PartyDTO areaPartyDTO=partyConnector.findAreaById(userId);//大区
						PartyEntityOrgDTO companPartyOrgDTO=partyConnector.findCompanyInfoById(userId);//公司
						String areaId="";
						String areaName="";
						String companyId="";
						String companyName="";
						if(areaPartyDTO!=null){
							areaId=areaPartyDTO.getId();
							areaName=areaPartyDTO.getName();
						}
						if(companPartyOrgDTO!=null){
							companyId=companPartyOrgDTO.getId();
							companyName=companPartyOrgDTO.getName();
						}
	        			map.put("areaId", areaId);
		    			map.put("areaName", areaName);
		                map.put("companyId", companyId);
		                map.put("companyName", companyName);
						map.put("positionId", userId);
						mapPositionInfoList.add(map);
						baseDTO.setMessage("one");
						baseDTO.setCode(200);
						baseDTO.setData(mapPositionInfoList);
			     	    return baseDTO;
	        		}
	        	}
	        }
	    	
        	//ckx 2018/9/29 判断是否是特殊细分
        	boolean boo = customWorkService.getBusinessDetail(businessDetailId);
	    	String strSql=String.format("SELECT * FROM oa_ba_business_post WHERE detail_id='%s' " 
					+" AND post_id in(select PARENT_ENTITY_ID from party_struct s WHERE s.CHILD_ENTITY_ID=%s AND s.STRUCT_TYPE_ID=4)",
							businessDetailId,
							userId);
			List<Map<String,Object>> mapPositionList=jdbcTemplate.queryForList(strSql);
			
			
			if(mapPositionList!=null&&mapPositionList.size()>0){
				if(mapPositionList.size()==1){
					Map<String,Object> map=new HashMap<String, Object>();
					Map<String,Object> mapPosition=mapPositionList.get(0);
					//ckx 
					String postId = mapPosition.get("post_id").toString();
					
					String strOldPostName = "";
					String strPostName = "";
					String strNewPostId = "";
					PartyDTO areaPartyDTO= null;
					PartyEntityOrgDTO companPartyOrgDTO=null;
					if(boo){
						strPostName = StringUtil.toString(mapPosition.get("post_name"));
						strOldPostName = strPostName;
						if(strPostName.contains("项目发起人")){
							strPostName = strPostName.substring(0, strPostName.length()-5)+"分公司";
							String sql = "select id from party_entity where name = '"+strPostName+"'";
							List<Map<String,Object>> queryForList = jdbcTemplate.queryForList(sql);
							if(null != queryForList && queryForList.size() > 0){
								strNewPostId = StringUtil.toString(queryForList.get(0).get("id"));
								//大区
								areaPartyDTO=partyConnector.findAreaById(strNewPostId);
								//公司
				    			companPartyOrgDTO=partyConnector.findCompanyInfoById(strNewPostId);
							}
						}
					}
	    			if(!boo){
	    				//大区
						areaPartyDTO=partyConnector.findAreaById(postId);
						//公司
		    			companPartyOrgDTO=partyConnector.findCompanyInfoById(postId);
					}
					if(areaPartyDTO==null)
						areaPartyDTO=new PartyDTO();
						    			
	    			if (companPartyOrgDTO == null) {
	    				companPartyOrgDTO = new PartyEntityOrgDTO();
	    			}
	    			map.put("areaId", areaPartyDTO.getId());
	    			map.put("areaName", areaPartyDTO.getName());
	                map.put("companyId", companPartyOrgDTO.getId());
	                map.put("companyName", companPartyOrgDTO.getName());
					map.put("positionId", mapPositionList.get(0).get("post_id").toString());
					mapPositionInfoList.add(map);
					baseDTO.setMessage("one");
					baseDTO.setCode(200);
					baseDTO.setData(mapPositionInfoList);
		     	    return baseDTO;
				}
				else{
					int i=1;
					for(Map<String,Object> mapPosition:mapPositionList){
						//ckx 
						PartyDTO areaPartyDTO= null;
						PartyEntityOrgDTO companPartyOrgDTO=null;
						String strOldPostName = "";
						String strPostName = "";
						String strNewPostId = "";
						String strPostId = mapPosition.get("post_id").toString();
						if(boo){
							strPostName = StringUtil.toString(mapPosition.get("post_name"));
							strOldPostName = strPostName;
							if("项目发起人".equals(strPostName)){
								continue;
							}else if(strPostName.contains("项目发起人")){
								strPostName = strPostName.substring(0, strPostName.length()-5)+"分公司";
								String sql = "select id from party_entity where name = '"+strPostName+"'";
								List<Map<String,Object>> queryForList = jdbcTemplate.queryForList(sql);
								if(null != queryForList && queryForList.size() > 0){
									strNewPostId = StringUtil.toString(queryForList.get(0).get("id"));
									areaPartyDTO=partyConnector.findAreaById(strNewPostId);
									companPartyOrgDTO=partyConnector.findCompanyInfoById(strNewPostId);
								}
							}
						}
						Map<String,Object> mapNewInfo=new HashMap<String, Object>();
						
						if(!boo){
							//大区
							areaPartyDTO=partyConnector.findAreaById(strPostId);
							//公司
			    			companPartyOrgDTO=partyConnector.findCompanyInfoById(strPostId);
						}
						
						if(areaPartyDTO==null)
							areaPartyDTO=new PartyDTO();
						
		    			//部门
		    			String strDepartInfo= "";
		    			PartyDTO partyDto = partyConnector.findDepartmentById(strPostId);
		    			if (partyDto != null) {
		    				strDepartInfo = partyDto.getName();
		    			}
		            	//岗位
		            	PartyEntity partyEntity=partyEntityManager.findUniqueBy("id",Long.parseLong(strPostId));
		            	String strPositionNo="";
		        		strSql="SELECT * FROM party_entity_attr WHERE ID=%s";
		            	List<Map<String,Object>> mapPostAttrList=jdbcTemplate.queryForList(String.format(strSql, partyEntity.getId()));
		                if(mapPostAttrList!=null&&mapPostAttrList.size()>0){
		                	strPositionNo="("+mapPostAttrList.get(0).get("positionNo").toString()+")";
		                }
		                String strPosition="";
		                
		                if(strDepartInfo.equals(companPartyOrgDTO.getName()))
		                	strPosition=companPartyOrgDTO.getName()+"-"+partyEntity.getName()+strPositionNo;
		            	else
		            		strPosition=companPartyOrgDTO.getName()+"-"+strDepartInfo+"-"+partyEntity.getName()+strPositionNo;
		                
		                mapNewInfo.put("num", i);
		                mapNewInfo.put("areaId", areaPartyDTO.getId());
		                mapNewInfo.put("areaName", areaPartyDTO.getName());
		                mapNewInfo.put("companyId", companPartyOrgDTO.getId());
		                mapNewInfo.put("companyName", companPartyOrgDTO.getName());
		                mapNewInfo.put("positionName", strPosition);
		                mapNewInfo.put("positionId", mapPosition.get("post_id").toString());
		                mapPositionInfoList.add(mapNewInfo);
		                i++;
					}
					
					baseDTO.setMessage("more");
					baseDTO.setCode(200);
					baseDTO.setData(mapPositionInfoList);
		     	    return baseDTO;
				}
				//region 屏蔽 虚拟岗位的业务逻辑
				/*postId=Long.parseLong(mapPositionList.get(0).get("post_id").toString());
				
	    		//查询是否为虚拟岗位
	    		strSql="SELECT * FROM PARTY_ENTITY_ATTR WHERE isRealPosition='1' AND ID=?";
	    		String strPositionRealIds="";
	    		List<Map<String,Object>> mapPostAttrList=jdbcTemplate.queryForList(strSql,postId);
	    		if(mapPostAttrList!=null&&mapPostAttrList.size()>0)
	    			strPositionRealIds=mapPostAttrList.get(0).get("positionRealIds").toString();
	    		
	    		//如果有虚拟对应的真实岗位
	    		if(!strPositionRealIds.equals("")){
	    			String[] positionRealIdArray=strPositionRealIds.split(",");
	    			strSql=String.format("SELECT PARENT_ENTITY_ID FROM party_struct WHERE CHILD_ENTITY_ID=%s AND STRUCT_TYPE_ID=4", userId);
	    			List<String> postPartyIdList=jdbcTemplate.queryForList(strSql,String.class); 
	    			
	    			//真实岗位中查询属于发起人的岗位
	    			for(String positionRealId:positionRealIdArray){
	    				if(postPartyIdList.contains(positionRealId)){
	    					postId=Long.parseLong(positionRealId);
	    					break;
	    				}
	    			}
	    		}*/
				//endregion
			}
			else {
				//如果此人没有岗位，则 存入此人的ID
				baseDTO.setMessage("没有发起该流程的权限，请检查流程细分的配置！");
				baseDTO.setCode(201);
				baseDTO.setData(mapPositionInfoList);
	     	    return baseDTO;
			}
    	}
    	catch(Exception ex){
    		baseDTO.setMessage("获取发起该流程的岗位异常，请联系管理员！");
			baseDTO.setCode(503);
     	    return baseDTO;
    	}
    }
    
    /**
     * 获取修改电话的细分ID
     * add by lilei at 2019.01.30
     * **/
    @GET
    @Path("getEditPhoneDetailId")
    public BaseDTO getEditPhoneDetailId(){
    	BaseDTO baseDTO=new BaseDTO();
    	Map<String,Object> mapReturnInfo=new HashMap<String, Object>();
    	Map<String,Object> mapDict=new HashMap<String, Object>();
    	try{
    		mapDict=applyService.getEditPhoneDetailId();
    		//businessDetailId=applyService.getEditPhoneDetailId();
    	}
    	catch(Exception ex){
    		mapDict=new HashMap<String, Object>();
    		logger.info("获取修改电话的细分id异常："+ex.getMessage()+"\r\n"+ex.getStackTrace());
    	}
    	mapReturnInfo.put("detailInfo", mapDict);
    	baseDTO.setMessage("ok");
		baseDTO.setCode(200);
		baseDTO.setData(mapReturnInfo);
    	return baseDTO;
    }
    
    public boolean isPasswordValid(String rawPassword, String encodedPassword) {
        if (customPasswordEncoder != null) {
            return customPasswordEncoder.matches(rawPassword, encodedPassword);
        } else {
            return rawPassword.equals(encodedPassword);
        }
    }
    
    @Value("${oa.url}")
	public void setOaUrl(String oaUrl) {
		this.oaUrl = oaUrl;
	}

	@Value("${oa.key}")
	public void setOaKey(String oaKey) {
		this.oaKey = oaKey;
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
    public void setCustomPasswordEncoder(CustomPasswordEncoder customPasswordEncoder) {
        this.customPasswordEncoder = customPasswordEncoder;
    }
    
    @Resource
    public void setAccountCredentialManager(AccountCredentialManager accountCredentialManager) {
        this.accountCredentialManager = accountCredentialManager;
    }
    
    @Resource
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    @Resource
    public void setPartyConnector(PartyConnector partyConnector) {
        this.partyConnector = partyConnector;
    }
    
    @Resource
    public void setPartyEntityManager(PartyEntityManager partyEntityManager) {
        this.partyEntityManager = partyEntityManager;
    }
}
