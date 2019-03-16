package com.mossle.base.web;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.mossle.api.menu.MenuDTO;
import com.mossle.api.store.StoreDTO;
import com.mossle.api.tenant.TenantHolder;
import com.mossle.auth.persistence.domain.Menu;
import com.mossle.auth.persistence.domain.Role;
import com.mossle.auth.persistence.domain.RoleDef;
import com.mossle.auth.persistence.domain.UserStatus;
import com.mossle.auth.support.CheckRoleException;
import com.mossle.base.persistence.domain.BusinessDetailEntity;
import com.mossle.base.persistence.domain.BusinessTypeEntity;
import com.mossle.base.persistence.domain.DetailPostEntity;
import com.mossle.base.persistence.domain.PostBusiDetailForConfirmEntity;
import com.mossle.base.persistence.manager.BusinessDetailManager;
import com.mossle.base.persistence.manager.BusinessTypeManager;
import com.mossle.base.persistence.manager.DetailPostManager;
import com.mossle.base.persistence.manager.PostBusiDetailForConfirmManager;
import com.mossle.base.rs.BusinessResource.BusinessDetailDTO;
import com.mossle.base.rs.BusinessResource.BusinessTypeDTO;
import com.mossle.base.support.PageForPostBusiDetailConfirm;
import com.mossle.bpm.persistence.domain.BpmProcess;
import com.mossle.bpm.persistence.manager.BpmProcessManager;
import com.mossle.common.utils.StringUtils;
import com.mossle.core.export.Exportor;
import com.mossle.core.export.TableModel;
import com.mossle.core.mapper.BeanMapper;
import com.mossle.core.page.Page;
import com.mossle.core.query.PropertyFilter;
import com.mossle.core.spring.MessageHelper;
import com.mossle.core.store.MultipartFileDataSource;
import com.mossle.core.util.BaseDTO;
import com.mossle.dict.persistence.domain.DictInfo;
import com.mossle.dict.support.DictConnectorImpl;
import com.mossle.operation.persistence.domain.Apply;
import com.mossle.operation.persistence.domain.ApplyDTO;
import com.mossle.party.persistence.domain.PartyEntity;
import com.mossle.party.persistence.domain.PartyStruct;
import com.mossle.party.persistence.manager.PartyEntityManager;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
/** 
 * @author  cz 
 * @version 2017年9月7日
 * 类说明 
 */
@Component
@Path("dict")
@Controller
@RequestMapping("dict")
public class PostBusiDetailForConfirmController {

	private BeanMapper beanMapper = new BeanMapper();
    private MessageHelper messageHelper;
    private Exportor exportor;
    private TenantHolder tenantHolder;
    private PostBusiDetailForConfirmManager postBusiDetailForConfirmManager;
    private PartyEntityManager partyEntityManager;
    private BusinessDetailManager businessDetailManager;
    private BusinessTypeManager businessTypeManager;
    
    private PageForPostBusiDetailConfirm pageForPBC ;

    
   


	@RequestMapping("dict-post-busidetail-list")
    public String postBusiDetailList(@ModelAttribute Page page,
            @RequestParam Map<String, Object> parameterMap, Model model) {
        String tenantId = tenantHolder.getTenantId();
        List<PropertyFilter> propertyFilters = PropertyFilter
                .buildFromMap(parameterMap);
        //propertyFilters.add(new PropertyFilter("EQS_tenantId", tenantId));
        page = pageForPBC.pageForPostBusiDetailConfirm( propertyFilters, page);
        
        model.addAttribute("page", page);
        return "dict/dict-post-busiDetail-list";
    }

    
    //展示页   【查看】  业务类型细分
    @POST
    @Path("bussiness-detail-busiDetailShow")
    @Produces(MediaType.APPLICATION_JSON)
    public BaseDTO positonInfo(@FormParam("id") Long id) {
        BaseDTO result = new BaseDTO();
        try {
            

        	List<PostBusiDetailForConfirmEntity> pList = postBusiDetailForConfirmManager.findBy("postID", id);
        	
        	
        	List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        	
        	for (PostBusiDetailForConfirmEntity p: pList) {
        		
        		Map<String, Object> map = new HashMap<String, Object>();
        		map.put("typeName", p.getTypeName());
                map.put("busiDetailName", p.getBusiDetailName());
                
                list.add(map);
           	}
        	
            result.setCode(200);
            result.setData(list);

        } catch (ArithmeticException e) {
            result.setCode(500);
            result.setMessage("查询出错");
        }
        return result;
    }
    
    
    
    
    
  //新建页   展开业务类型这颗树
    @POST
    @Path("getBusiDetail")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Map> getBusiDetail() {
    	
    	List<String> permissions = null ;
    	
    	 List<BusinessTypeEntity> businessTypes = businessTypeManager.getAll();

         List<BusinessTypeDTO> businessTypeDtos = new ArrayList<BusinessTypeDTO>();

         for (BusinessTypeEntity businessType : businessTypes) {
             BusinessTypeDTO businessTypeDto = new BusinessTypeDTO();
             businessTypeDto.setId(businessType.getId());
             businessTypeDto.setName(businessType.getBusinesstype());
             businessTypeDtos.add(businessTypeDto);
         }

        return generateMenus(businessTypeDtos
        		, permissions
        		);
    }
    
 
    
    //修改页   展开业务类型这颗树，并且把之前已选择的都勾选上
    @POST
    @Path("getDetail")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Map> getMenus(List<String> permissions) {
    	
    	//List<String> permissions = convertMapListToStringList(roleId);
    	 List<BusinessTypeEntity> businessTypes = businessTypeManager.getAll();

         List<BusinessTypeDTO> businessTypeDtos = new ArrayList<BusinessTypeDTO>();

         for (BusinessTypeEntity businessType : businessTypes) {
             BusinessTypeDTO businessTypeDto = new BusinessTypeDTO();
             businessTypeDto.setId(businessType.getId());
             businessTypeDto.setName(businessType.getBusinesstype());
             businessTypeDtos.add(businessTypeDto);
         }

        return generateMenus(businessTypeDtos
        		, permissions
        		);
    }
    
    public List<Map> generateMenus(List<BusinessTypeDTO> parentList
    		, List<String> permissions
    		) {
        if (parentList == null) {
            return null;
        }

        List<Map> list = new ArrayList<Map>();

        try {
            for (BusinessTypeDTO menu : parentList) {
                list.add(generateMenu(menu
                		, permissions
                		));
            }
        } 
        catch (Exception ex) {
           // logger.error(ex.getMessage(), ex);
        }

        return list;
    }
    
    public Map<String, Object> generateMenu(BusinessTypeDTO menu
    		, List<String> permissions
    		) {
    	
        Map<String, Object> map = new HashMap<String, Object>();

        try {
            map.put("id", menu.getId());
            map.put("name", menu.getName());
           // map.put("code", menu.getCode());
            
            if(permissions != null){
            if (permissions.contains(Long.toString(menu.getId()))) {
            	map.put("checked", true);
		    }
            }
            
            
            List<BusinessDetailEntity> list = businessDetailManager.findBy("typeId", menu.getId());
            List<BusinessDetailDTO> businessDetailDtos = new ArrayList<BusinessDetailDTO>();
            for (BusinessDetailEntity businessDetail : list) {
                BusinessDetailDTO businessDetailDto = new BusinessDetailDTO();
                businessDetailDto.setId(businessDetail.getId());
                businessDetailDto.setDetail(businessDetail.getBusiDetail());
                businessDetailDtos.add(businessDetailDto);
            }
            
            if (list == null || list.size() == 0) {
                map.put("open", false);
            } else {
                map.put("open", true);
                map.put("children", getDetailType(businessDetailDtos
                		, permissions
                		));
            }
        	
            return map;
        } 
        catch (Exception ex) {
            //logger.error(ex.getMessage(), ex);

            return map;
        }
    }
    
    public List<Map> getDetailType(List<BusinessDetailDTO> list
    		, List<String> permissions
    		) {
       
    	List<Map> ListMap = new ArrayList<Map>();
    	 
    	 
    	 try{
    	 
    	 List<BusinessDetailDTO> businessDetailDtos = new ArrayList<BusinessDetailDTO>();
         for (BusinessDetailDTO businessDetail : list) {
        	 Map<String, Object> map = new HashMap<String, Object>();
             BusinessDetailDTO businessDetailDto = new BusinessDetailDTO();
            
             map.put("id",businessDetail.getId());
             map.put("name", businessDetail.getDetail());
             
             
             if(permissions != null){
             if (permissions.contains(Long.toString(businessDetail.getId()))) {
              	map.put("checked", true);
              }
             }
             
             ListMap.add(map);
         }
    	 
             
         return ListMap;
 		 } 
    	 catch (Exception ex) {
             //logger.error(ex.getMessage(), ex);

             return ListMap;
         }
    }
    
    
    //新建保存
    @RequestMapping("post-detail-save")
    public String save(
            //@RequestParam("id") Long id,
    		HttpServletRequest request,
            Model model,
            @RequestParam(value = "selectedItem", required = false) String selectedItem,
            RedirectAttributes redirectAttributes) {
    	
    	List<String> list = Collections.emptyList();
       
    	
    	String postName = request.getParameter("postName");
    	String postID = request.getParameter("postId");
    		
    	//先取用户选择的业务明细，找出对应的中文名称
    	String[] detailID = null;  
    	detailID = selectedItem.split(",");
    	
       for(int i=0;i<detailID.length;i++){
    	   
    	   System.out.print(detailID[i]);
    	   
    	   List<BusinessDetailEntity> businessDetail = businessDetailManager.findBy("id", Long.parseLong(detailID[i]));
    	   
    	   if (businessDetail.size()>0){
    	   
    	   PostBusiDetailForConfirmEntity postBusiDetailForConfirmEntity = new PostBusiDetailForConfirmEntity();
    	   postBusiDetailForConfirmEntity.setPostID(Long.parseLong(postID));
    	   postBusiDetailForConfirmEntity.setPostName(postName);
    	   postBusiDetailForConfirmEntity.setBusiDetailID(businessDetail.get(0).getId());
    	   postBusiDetailForConfirmEntity.setBusiDetailName(businessDetail.get(0).getBusiDetail());
    	   postBusiDetailForConfirmEntity.setTypeID(businessDetail.get(0).getTypeId());
    	   postBusiDetailForConfirmEntity.setTypeName(businessDetail.get(0).getBusinessType());
    	   postBusiDetailForConfirmEntity.setTenantId("1");//租户ID
    	   
    	   SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
       	   String str = sdf.format(new Date());
       	
    	   postBusiDetailForConfirmEntity.setCreateTime(str);
    	   postBusiDetailForConfirmManager.save(postBusiDetailForConfirmEntity);
    	   }
    	}
       return "redirect:/dict/dict-post-busidetail-list.do";
    }
    
  //编辑页   展开业务类型这颗树
    @POST
    @Path("getChenked")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Map> getChenked(@QueryParam("id") Long id
    						) {
    	
    	//根据当前被编辑行的id，取这一条记录，为了获取岗位id，用岗位id获取这个岗位挂的所有细分
    	
    	List<PostBusiDetailForConfirmEntity> p = postBusiDetailForConfirmManager.findBy("id", id);
    	
    	Long postID =p.get(0).getPostID();
    	
    	List<PostBusiDetailForConfirmEntity> pList = postBusiDetailForConfirmManager.findBy("postID", postID);
    	
    	
    	List<String> permissions = new ArrayList<String>();
        
    	for (PostBusiDetailForConfirmEntity postBusiDetailForConfirmEntity: pList) {
    		permissions.add(postBusiDetailForConfirmEntity.getBusiDetailID().toString()); 
    		if(!(permissions.contains(postBusiDetailForConfirmEntity.getTypeID().toString()))){
    			permissions.add(postBusiDetailForConfirmEntity.getTypeID().toString());
    		}
        }
    	
    	//List<String> permissions = convertMapListToStringList(roleId);
    	//取业务类型的大类
    	 List<BusinessTypeEntity> businessTypes = businessTypeManager.getAll();

         List<BusinessTypeDTO> businessTypeDtos = new ArrayList<BusinessTypeDTO>();

         for (BusinessTypeEntity businessType : businessTypes) {
             BusinessTypeDTO businessTypeDto = new BusinessTypeDTO();
             businessTypeDto.setId(businessType.getId());
             businessTypeDto.setName(businessType.getBusinesstype());
             businessTypeDtos.add(businessTypeDto);
         }

        return generateMenus(businessTypeDtos
        		, permissions
        		);
            
    }
    

    
    //修改页保存
    @RequestMapping("post-detail-input-save")
    public String input_save(
            //@RequestParam("id") Long id,
    		@QueryParam("id") Long id,
    		HttpServletRequest request,
            Model model,
            @RequestParam(value = "selectedItem", required = false) String selectedItem,
            RedirectAttributes redirectAttributes) {
    	
    	List<String> list = Collections.emptyList();
    	
    	String postName = request.getParameter("postName");
    	String postID = request.getParameter("postId");
    	
    	
    	List<PostBusiDetailForConfirmEntity> p = postBusiDetailForConfirmManager.findBy("id", id);
    	
    	Long postIDForRemove =p.get(0).getPostID();
    	
    	List<PostBusiDetailForConfirmEntity> pList = postBusiDetailForConfirmManager.findBy("postID", postIDForRemove);
    	
    	postBusiDetailForConfirmManager.removeAll(pList);
    	
    		
    	//先取用户选择的业务明细，找出对应的中文名称
    	String[] detailID = null;  
    	detailID = selectedItem.split(",");
    	
       for(int i=0;i<detailID.length;i++){
    	   
    	   System.out.print(detailID[i]);
    	   
    	   List<BusinessDetailEntity> businessDetail = businessDetailManager.findBy("id", Long.parseLong(detailID[i]));
    	   
    	   if (businessDetail.size()>0){
    	   
    	   PostBusiDetailForConfirmEntity postBusiDetailForConfirmEntity = new PostBusiDetailForConfirmEntity();
    	   postBusiDetailForConfirmEntity.setPostID(Long.parseLong(postID));
    	   postBusiDetailForConfirmEntity.setPostName(postName);
    	   postBusiDetailForConfirmEntity.setBusiDetailID(businessDetail.get(0).getId());
    	   postBusiDetailForConfirmEntity.setBusiDetailName(businessDetail.get(0).getBusiDetail());
    	   postBusiDetailForConfirmEntity.setTypeID(businessDetail.get(0).getTypeId());
    	   postBusiDetailForConfirmEntity.setTypeName(businessDetail.get(0).getBusinessType());
    	   postBusiDetailForConfirmEntity.setTenantId("1");//租户ID
    	   
    	   SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
       	   String str = sdf.format(new Date());
       	
    	   postBusiDetailForConfirmEntity.setCreateTime(str);
    	   postBusiDetailForConfirmManager.save(postBusiDetailForConfirmEntity);
    	   }
    	}
       return "redirect:/dict/dict-post-busidetail-list.do";
    }
    
//---------------------------------------------------------------------------------------------------------- 
    
	    //跳转到修改页
	    @RequestMapping("dict-post-busidetail-input")
	    public String input(@RequestParam(value = "id", required = false) Long id,Model model) {
    	
	    	//需要将岗位名称和岗位id带回页面显示出来
	    	String postName="" ;
	    	String postId ="";
	    	
           	List<PostBusiDetailForConfirmEntity> p = postBusiDetailForConfirmManager.findBy("id", id);   	
	    	
        	postId =Long.toString(p.get(0).getPostID());
        	postName = p.get(0).getPostName();
	    	
	    	
	    model.addAttribute("id",id);
	    model.addAttribute("postName",postName);
	    model.addAttribute("postId",postId);
	    
        return "dict/dict-post-busiDetail-input";
    }
	    
    
    //新建业务类型明细
    @RequestMapping("dict-post-busidetail-new")
    public String newType() {
    	return "dict/dict-post-busiDetail-new";
    }



    @RequestMapping("dict-post-busidetail-remove")
    public String remove(@RequestParam("selectedItem") List<Long> selectedItem,
            RedirectAttributes redirectAttributes) {
        List<PostBusiDetailForConfirmEntity> dictTypes = postBusiDetailForConfirmManager.findByIds(selectedItem);

        postBusiDetailForConfirmManager.removeAll(dictTypes);

        messageHelper.addFlashMessage(redirectAttributes,
                "core.success.delete", "删除成功");

        return "redirect:/dict/dict-detail-list.do";
    }

    @RequestMapping("dict-post-busidetail-export")
    public void export(@ModelAttribute Page page,
            @RequestParam Map<String, Object> parameterMap,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        String tenantId = tenantHolder.getTenantId();
        List<PropertyFilter> propertyFilters = PropertyFilter
                .buildFromMap(parameterMap);
        propertyFilters.add(new PropertyFilter("EQS_tenantId", tenantId));
        page = postBusiDetailForConfirmManager.pagedQuery(page, propertyFilters);

        List<PostBusiDetailForConfirmEntity> dictTypes = (List<PostBusiDetailForConfirmEntity>) page.getResult();

        TableModel tableModel = new TableModel();
        tableModel.setName("dict info");
        tableModel.addHeaders("id", "name", "stringValue", "description");
        tableModel.setData(dictTypes);
        exportor.export(request, response, tableModel);
    }

    // ~ ======================================================================
 

    @Resource
    public void setExportor(Exportor exportor) {
        this.exportor = exportor;
    }

    @Resource
    public void setMessageHelper(MessageHelper messageHelper) {
        this.messageHelper = messageHelper;
    }

    @Resource
    public void setTenantHolder(TenantHolder tenantHolder) {
        this.tenantHolder = tenantHolder;
    }
    
    @Resource
    public void setPostBusiDetailForConfirmManager(PostBusiDetailForConfirmManager postBusiDetailForConfirmManager) {
        this.postBusiDetailForConfirmManager = postBusiDetailForConfirmManager;
    }
    
    @Resource
    public void setPartyEntityManager(PartyEntityManager partyEntityManager) {
        this.partyEntityManager = partyEntityManager;
    }

    @Resource
	public void setBusinessDetailManager(BusinessDetailManager businessDetailManager) {
		this.businessDetailManager = businessDetailManager;
	}
    
    @Resource
    public void setBusinessTypelManager(BusinessTypeManager businessTypeManager) {
        this.businessTypeManager = businessTypeManager;
    }
    
    @Resource
    public void setPageForPBC(PageForPostBusiDetailConfirm pageForPBC) {
		this.pageForPBC = pageForPBC;
	}
    
}
