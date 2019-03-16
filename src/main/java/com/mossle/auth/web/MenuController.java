package com.mossle.auth.web;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.mossle.api.menu.MenuDTO;
import com.mossle.api.menu.MenuForDTO;
import com.mossle.auth.persistence.domain.Menu;
import com.mossle.auth.persistence.domain.Perm;
import com.mossle.auth.persistence.manager.MenuManager;
import com.mossle.auth.persistence.manager.PermManager;
import com.mossle.auth.support.MenuCache;
import com.mossle.core.export.Exportor;
import com.mossle.core.export.TableModel;
import com.mossle.core.mapper.BeanMapper;
import com.mossle.core.page.Page;
import com.mossle.core.query.PropertyFilter;
import com.mossle.core.spring.MessageHelper;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("auth")
public class MenuController {
    private MenuManager menuManager;
    private PermManager permManager;
    private MessageHelper messageHelper;
    private Exportor exportor;
    private BeanMapper beanMapper = new BeanMapper();
    private MenuCache menuCache;

    /*@RequestMapping("menu-list")
    public String list(@ModelAttribute Page page,
            @RequestParam Map<String, Object> parameterMap, Model model) {
        List<PropertyFilter> propertyFilters = PropertyFilter
                .buildFromMap(parameterMap);
        propertyFilters.add(new PropertyFilter("EQS_display","true"));
        //page = menuManager.pagedQuery(page, propertyFilters);

        List<Menu> menuList=menuManager.find(propertyFilters);
        List<Menu> menuSortList=GetTreeMenuList(menuList,new Long("21"),0);
        model.addAttribute("menuList", menuSortList);

        return "auth/menu-list";
    }*/
    
    @RequestMapping("menu-list")
    public String list(
            @RequestParam Map<String, Object> parameterMap, Model model) {
        List<PropertyFilter> propertyFilters = PropertyFilter
                .buildFromMap(parameterMap);
        propertyFilters.add(new PropertyFilter("EQS_display","true"));
        //page = menuManager.pagedQuery(page, propertyFilters);
        
        String hqlString="from Menu where display=? order by priority";
        List<Menu> menuList=menuManager.find(hqlString, "true");
        //menuList=menuManager.find(hql, map)
        List<MenuForDTO> menuSortList=GetTreeMenuList(menuList,new Long("21"),0,null);
        model.addAttribute("menuList", menuSortList);

        return "auth/menu-list";
    }
    
    private List<MenuForDTO> GetTreeMenuList(List<Menu> menuList,long parentId,int index,List<Menu> list) {
    	if(list!=null&&list.size()<1) list=null;
		List<MenuForDTO> menuSortList=new ArrayList<MenuForDTO>();
		index++;
		if(menuList!=null&&menuList.size()>0){
			for (Menu menu : menuList) {
				if(list!=null&&list.contains(menu)) continue;
				if(menu.getMenu()==null) continue;
				if(menu.getMenu().getId()==parentId){
					//menu.setTitle(GetBlank(index)+menu.getTitle());
					
					menuSortList.add(convertMenuDto(menu,GetBlank(index)+menu.getTitle()));
					
					//menuSortList.addAll(GetTreeMenuList(menuList,menu.getId(),index));
					if(menu.getMenus()!=null&&menu.getMenus().size()>0){
						for(Menu menuChild:menu.getMenus()){
							//if(!menuChild.getDisplay().equals("true")) continue;
							if(list!=null&&list.contains(menuChild)) continue;
							//menuChild.setTitle(GetBlank(index+1)+menuChild.getTitle());
							
							menuSortList.add(convertMenuDto(menuChild,GetBlank(index+1)+menuChild.getTitle()));
							
							List<MenuForDTO> menuChildList= GetTreeMenuList(menuList,menuChild.getId(),index+1,null);
							if(menuChildList!=null&&menuChildList.size()>0)
								menuSortList.addAll(menuChildList);
						}
					}
					//menuParentList.addAll(menu.getMenus());
				}
			}
		}
		return menuSortList;
	}
    
    public List<MenuForDTO> convertMenuDtos(List<Menu> menus) {
		List<MenuForDTO> menuDtos = new ArrayList<MenuForDTO>();

		for (Menu menu : menus) {

			MenuForDTO menuDto = this.convertMenuDto(menu,menu.getTitle());
			menuDtos.add(menuDto);
		}

		return menuDtos;
	}
    
    public MenuForDTO convertMenuDto(Menu menu,String title) {
    	MenuForDTO menuDto = new MenuForDTO();
		
		menuDto.setId(menu.getId());
		menuDto.setCode(menu.getCode());
		menuDto.setTitle(title);
		menuDto.setUrl(menu.getUrl());
		menuDto.setDisplay(menu.getDisplay());

		if (menu.getPerm() != null) {
			menuDto.setPermission(menu.getPerm().getCode());
		}
		menuDto.setType(menu.getType());
		return menuDto;
	}
    
    private String GetBlank(int index){
    	StringBuffer sbBlankBuffer=new StringBuffer();
    	for(int i=1;i<index;i++){
    		if(i==1)
    			sbBlankBuffer.append("|----");
    		else 
    			sbBlankBuffer.append("----");
    	}
    	return sbBlankBuffer.toString();
    }

    @RequestMapping("menu-input")
    public String input(@RequestParam(value = "id", required = false) Long id,
            Model model) {
    	Menu menu=null;
        if (id != null) {
        	Menu menuNew=new Menu();
            menu = menuManager.get(id);
            model.addAttribute("model", menu);

            //String hql = "from Menu where menu.id!=? and display = 'true'";
            //List<Menu> menus = this.menuManager.find(hql, id);
            //model.addAttribute("menus", menus);
        }
        /*else {
            // List<Menu> menus = this.menuManager.getAll();
            String hql = "from Menu where type !=? and display = 'true' and type != 'system'";
            String type = "page";
            List<Menu> menus = this.menuManager.find(hql, type);
            model.addAttribute("menus", menus);
        }*/
    	List<PropertyFilter> propertyFilters=new ArrayList<PropertyFilter>();
    	propertyFilters.add(new PropertyFilter("EQS_display","true"));
    	
    	List<Menu> menuExistList=new ArrayList<Menu>();
    	//if(menu!=null)
    		//menuExistList.add(menu);
    	
    	this.menuCache.clear();
    	List<Menu> menuList=menuManager.find(propertyFilters);
        List<MenuForDTO> menuSortList=GetTreeMenuList(menuList,new Long("21"),0,null);
        model.addAttribute("menus", menuSortList);
        this.menuCache.clear();
    	
        /*List<Perm> perms = this.permManager.getAll();
        model.addAttribute("perms", perms);*/

        return "auth/menu-input";
    }

    @RequestMapping("menu-save")
    public String save(@ModelAttribute Menu menu,
            @RequestParam(value = "parentId", required = false) Long parentId,
            @RequestParam(value = "permId", required = false) Long permId,
            RedirectAttributes redirectAttributes) {
        Menu dest = null;
        Long id = menu.getId();

        if (id != null) {
            dest = menuManager.get(id);
            beanMapper.copy(menu, dest);
        } else {
            dest = menu;
        }

        if (parentId != null) {
            dest.setMenu(menuManager.get(parentId));
        } else {
            dest.setMenu(null);
        }

        if (permId != null) {
            dest.setPerm(permManager.get(permId));
        } else {
            dest.setPerm(null);
        }

        menuManager.save(dest);

        messageHelper.addFlashMessage(redirectAttributes, "core.success.save",
                "保存成功");
        this.menuCache.clear();

        return "redirect:/auth/menu-list.do";
    }

    @RequestMapping("menu-remove")
    public String remove(@RequestParam("selectedItem") List<Long> selectedItem,
            RedirectAttributes redirectAttributes) {
        List<Menu> menus = menuManager.findByIds(selectedItem);
        menuManager.removeAll(menus);
        messageHelper.addFlashMessage(redirectAttributes, "core.delete.save",
                "删除成功");
        this.menuCache.clear();

        return "redirect:/auth/menu-list.do";
    }

    @RequestMapping("menu-export")
    public void export(@ModelAttribute Page page,
            @RequestParam Map<String, Object> parameterMap,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        List<PropertyFilter> propertyFilters = PropertyFilter
                .buildFromMap(parameterMap);
        page = menuManager.pagedQuery(page, propertyFilters);

        List<Menu> menus = (List<Menu>) page.getResult();
        TableModel tableModel = new TableModel();
        tableModel.setName("menu");
        tableModel.addHeaders("id", "name");
        tableModel.setData(menus);
        exportor.export(request, response, tableModel);
    }

    // ~ ======================================================================
    @Resource
    public void setMenuManager(MenuManager menuManager) {
        this.menuManager = menuManager;
    }

    @Resource
    public void setPermManager(PermManager permManager) {
        this.permManager = permManager;
    }

    @Resource
    public void setMessageHelper(MessageHelper messageHelper) {
        this.messageHelper = messageHelper;
    }

    @Resource
    public void setExportor(Exportor exportor) {
        this.exportor = exportor;
    }

    @Resource
    public void setMenuCache(MenuCache menuCache) {
        this.menuCache = menuCache;
    }
}
