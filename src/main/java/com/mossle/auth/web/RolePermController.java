package com.mossle.auth.web;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;

import com.mossle.api.tenant.TenantHolder;
import com.mossle.auth.component.AuthCache;
import com.mossle.auth.component.RoleDefChecker;
import com.mossle.auth.persistence.domain.Menu;
import com.mossle.auth.persistence.domain.Perm;
import com.mossle.auth.persistence.domain.PermType;
import com.mossle.auth.persistence.domain.Role;
import com.mossle.auth.persistence.domain.RoleDef;
import com.mossle.auth.persistence.domain.UserStatus;
import com.mossle.auth.persistence.manager.MenuManager;
import com.mossle.auth.persistence.manager.PermManager;
import com.mossle.auth.persistence.manager.PermTypeManager;
import com.mossle.auth.persistence.manager.RoleDefManager;
import com.mossle.auth.persistence.manager.RoleManager;
import com.mossle.auth.support.CheckRoleException;
import com.mossle.common.utils.StringUtils;
import com.mossle.core.annotation.Log;
import com.mossle.core.spring.MessageHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("auth")
public class RolePermController {
    private static Logger logger = LoggerFactory
            .getLogger(RolePermController.class);
    private PermManager permManager;
    private RoleDefManager roleDefManager;
    private PermTypeManager permTypeManager;
    private MessageHelper messageHelper;
    private RoleDefChecker roleDefChecker;
    private RoleManager roleManager;
    private TenantHolder tenantHolder;
    private AuthCache authCache;
    private MenuManager menuManager;
    
    
    @RequestMapping("role-perm-save")
    @Log(desc = "系统配置", action = "save", operationDesc = "系统配置-权限管理-角色管理-权限设置")
    public String save(
            @RequestParam("id") Long id,
            Model model,
            // @RequestParam(value = "selectedItem", required = false) List<Long> selectedItem,
            @RequestParam(value = "selectedItem", required = false) String selectedItem,
            RedirectAttributes redirectAttributes) {
    	
    	List<String> list = Collections.emptyList();; 
        
        if (StringUtils.isBlank(selectedItem)) {
        	list = Collections.emptyList();
        } else {
        	String[] tmp = selectedItem.split(",");
        	list = Arrays.asList(tmp);  
        }
        
        try {
            Role role = roleManager.get(id);
            RoleDef roleDef = role.getRoleDef();
            roleDefChecker.check(roleDef);
            roleDef.getMenus().clear();

            for (String permId : list) {
                // Perm perm = permManager.get(permId);
                Menu menu = menuManager.get(Long.parseLong(permId));
                roleDef.getMenus().add(menu);
            }

            roleDefManager.save(roleDef);
            messageHelper.addFlashMessage(redirectAttributes,
                    "core.success.save", "保存成功");

            for (Role roleInstance : roleDef.getRoles()) {
                for (UserStatus userStatus : roleInstance.getUserStatuses()) {
                    authCache.evictUserStatus(userStatus);
                }
            }
        } catch (CheckRoleException ex) {
            logger.warn(ex.getMessage(), ex);
            messageHelper.addFlashMessage(redirectAttributes, ex.getMessage());

            return input(id, model);
        }

        return "redirect:/auth/role-perm-input.do?id=" + id;
    }

    @RequestMapping("role-perm-input")
    public String input(@RequestParam("id") Long id, Model model) {
        Role role = roleManager.get(id);
        RoleDef roleDef = role.getRoleDef();
        List<Long> selectedItem = new ArrayList<Long>();

//        for (Perm perm : roleDef.getPerms()) {
//            selectedItem.add(perm.getId());
//        }

        String hql = "from PermType where type=0 and tenantId=?";
        List<PermType> permTypes = permTypeManager.find(hql,
                tenantHolder.getTenantId());
        model.addAttribute("permTypes", permTypes);
        model.addAttribute("selectedItem", selectedItem);
        model.addAttribute("id", id);

        return "auth/role-perm-input";
    }
    
    /*@RequestMapping("user-orgdata-input")
    public String orgDataInput( Model model) {
        Role role = roleManager.get(id);
        RoleDef roleDef = role.getRoleDef();
        List<Long> selectedItem = new ArrayList<Long>();

        String hql = "from PermType where type=0 and tenantId=?";
        List<PermType> permTypes = permTypeManager.find(hql,
                tenantHolder.getTenantId());
        model.addAttribute("permTypes", permTypes);
        model.addAttribute("selectedItem", selectedItem);
        //model.addAttribute("id", id);

        return "auth/user-orgdata-input";
    }
    
    @RequestMapping("user-orgdata-input-i")
    public String orgDataInput_i( Model model) {
        Role role = roleManager.get(id);
        RoleDef roleDef = role.getRoleDef();
        List<Long> selectedItem = new ArrayList<Long>();

        String hql = "from PermType where type=0 and tenantId=?";
        List<PermType> permTypes = permTypeManager.find(hql,
                tenantHolder.getTenantId());
        model.addAttribute("permTypes", permTypes);
        model.addAttribute("selectedItem", selectedItem);
        //model.addAttribute("id", id);

        return "auth/user-orgdata-input-i";
    }*/

    // ~ ======================================================================
    @Resource
    public void setPermManager(PermManager permManager) {
        this.permManager = permManager;
    }
    
    @Resource
	public void setMenuManager(MenuManager menuManager) {
		this.menuManager = menuManager;
	}

	@Resource
    public void setRoleDefManager(RoleDefManager roleDefManager) {
        this.roleDefManager = roleDefManager;
    }

    @Resource
    public void setRoleDefChecker(RoleDefChecker roleDefChecker) {
        this.roleDefChecker = roleDefChecker;
    }

    @Resource
    public void setPermTypeManager(PermTypeManager permTypeManager) {
        this.permTypeManager = permTypeManager;
    }

    @Resource
    public void setMessageHelper(MessageHelper messageHelper) {
        this.messageHelper = messageHelper;
    }

    @Resource
    public void setRoleManager(RoleManager roleManager) {
        this.roleManager = roleManager;
    }

    @Resource
    public void setTenantHolder(TenantHolder tenantHolder) {
        this.tenantHolder = tenantHolder;
    }

    @Resource
    public void setAuthCache(AuthCache authCache) {
        this.authCache = authCache;
    }
    
}
