package com.mossle.base.web;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.mossle.api.tenant.TenantHolder;
import com.mossle.base.persistence.domain.BusinessTypeEntity;
import com.mossle.base.persistence.manager.BusinessTypeManager;
import com.mossle.core.export.Exportor;
import com.mossle.core.export.TableModel;
import com.mossle.core.mapper.BeanMapper;
import com.mossle.core.page.Page;
import com.mossle.core.query.PropertyFilter;
import com.mossle.core.spring.MessageHelper;
import com.mossle.core.util.StringUtils;
import com.mossle.dict.persistence.domain.DictInfo;
import com.mossle.dict.support.DictConnectorImpl;
import com.mossle.operation.web.ProcessOperationController;
import com.mossle.party.persistence.domain.PartyType;
import com.mossle.party.persistence.manager.PartyTypeManager;
import com.mossle.dict.support.DictConnectorImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
/** 
 * @author  cz 
 * @version 2017年9月7日
 * 类说明 
 */
@Controller
@RequestMapping("dict")
public class BusinessTypeController {
	private static Logger logger = LoggerFactory
            .getLogger(ProcessOperationController.class);
	private BeanMapper beanMapper = new BeanMapper();
    private MessageHelper messageHelper;
    private Exportor exportor;
    private TenantHolder tenantHolder;
    private BusinessTypeManager businessTypeManager;
    private DictConnectorImpl dictConnectorImpl ;
    
    @RequestMapping("dict-business-type-list")
    public String businessTypeList(@ModelAttribute Page page,
            @RequestParam Map<String, Object> parameterMap, Model model) {
        String tenantId = tenantHolder.getTenantId();
        List<PropertyFilter> propertyFilters = PropertyFilter
                .buildFromMap(parameterMap);
        propertyFilters.add(new PropertyFilter("EQS_tenantId", tenantId));
        page = businessTypeManager.pagedQuery(page, propertyFilters);

        model.addAttribute("page", page);
        return "dict/dict-business-type-list";
    }
    
    //修改是否启用
    @RequestMapping("dict-business-type-input")
    public String input(@RequestParam(value = "id", required = false) Long id,
            Model model) {
        if (id != null) {
            BusinessTypeEntity dictType = businessTypeManager.get(id);
            model.addAttribute("model", dictType);
        }
        return "dict/dict-business-type-input";
    }
    
    //新建业务类型
    @RequestMapping("dict-business-type-new")
    public String newType() {
       return "dict/dict-business-type-new";
    }

    @RequestMapping("dict-business-type-save")
    public String save(HttpServletRequest request,
    		@ModelAttribute BusinessTypeEntity dictType,
            @RequestParam Map<String, Object> parameterMap,
            RedirectAttributes redirectAttributes) {
    	
    	Long id = dictType.getId();
        String tenantId = tenantHolder.getTenantId();
        BusinessTypeEntity dest = null;
        
        String formid ="";//表单id
        String formName ="";//表单名称
        
        //到数据字典中取表单
        List<DictInfo> dictInfo = this.dictConnectorImpl.findDictInfoListByType("applyForm");
        
        
        //取html中选中的checkbox的值
        String[] formNames =  request.getParameterValues("formNames");
        if(formNames!=null&&formNames.length>0){
        	int length=formNames.length;
        	//解析表单id并翻译表单名称
  	      	for (int i=0;i<formNames.length;i++){
  	      		formid = formid + formNames[i]+",";
  	      		for(DictInfo p:dictInfo){
	  	       		if( formNames[i].equals(p.getValue())){
	  	       			formName +=p.getName()+",";
	  	       			break;
	  	       		}
	  	       	}
	  	    }
	        if(!StringUtils.isBlank(formid))
	        	formid = formid.substring(0, formid.length()-1);
	        if(!StringUtils.isBlank(formName))
	        	formName = formName.substring(0, formName.length()-1);
        }
	      
      
      dictType.setFormid(formid);
      dictType.setFormName(formName);
        if (id != null) {
            dest = businessTypeManager.get(id);
            
            //插入日志
           String logContentString = "业务类型修改： ";
            
            if(!dest.getBusinesstype().equals(dictType.getBusinesstype())){
            	logContentString = logContentString+ dest.getBusinesstype()+" 修改为 "+dictType.getBusinesstype()+" ";
            }           
            if(!dest.getEnable().equals(dictType.getEnable())){
            	String oldEnableString = "";String newEnableString = "";
            	if (dest.getEnable().equals("是")) {	oldEnableString="显示";}else{	oldEnableString="不显示";}
            	if (dictType.getEnable().equals("是")) {	newEnableString="显示";}else{	newEnableString="不显示";}
            	logContentString = logContentString+ oldEnableString+" 修改为 "+newEnableString;
            }
            if(logContentString!=null&&!logContentString.equals("业务类型修改： ")){
            	logger.info( logContentString);
            }
            
         
            beanMapper.copy(dictType, dest);
        } else {
            dest = dictType;
            dest.setTenantId(tenantId);
        }

        businessTypeManager.save(dest);

        messageHelper.addFlashMessage(redirectAttributes, "core.success.save",
                "保存成功");

        return "redirect:/dict/dict-business-type-list.do";
        
    }

    @RequestMapping("dict-business-type-remove")
    public String remove(@RequestParam("selectedItem") List<Long> selectedItem,
            RedirectAttributes redirectAttributes) {
        List<BusinessTypeEntity> dictTypes = businessTypeManager.findByIds(selectedItem);

        businessTypeManager.removeAll(dictTypes);

        messageHelper.addFlashMessage(redirectAttributes,
                "core.success.delete", "删除成功");

        return "redirect:/dict/dict-type-list.do";
    }

    @RequestMapping("dict-business-type-export")
    public void export(@ModelAttribute Page page,
            @RequestParam Map<String, Object> parameterMap,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        String tenantId = tenantHolder.getTenantId();
        List<PropertyFilter> propertyFilters = PropertyFilter
                .buildFromMap(parameterMap);
        propertyFilters.add(new PropertyFilter("EQS_tenantId", tenantId));
        page = businessTypeManager.pagedQuery(page, propertyFilters);

        List<BusinessTypeEntity> dictTypes = (List<BusinessTypeEntity>) page.getResult();

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
    public void setBusinessTypeManager(BusinessTypeManager businessTypeManager) {
        this.businessTypeManager = businessTypeManager;
    }
    
    @Resource
   	public void setDictConnectorImpl(DictConnectorImpl dictConnectorImpl) {
   		this.dictConnectorImpl = dictConnectorImpl;
   	}
}
