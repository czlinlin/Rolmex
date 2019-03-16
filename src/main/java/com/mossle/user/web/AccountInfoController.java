package com.mossle.user.web;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.mossle.api.store.StoreConnector;
import com.mossle.api.tenant.TenantHolder;
import com.mossle.api.user.UserCache;
import com.mossle.api.user.UserDTO;
import com.mossle.core.auth.CurrentUserHolder;
import com.mossle.core.auth.CustomPasswordEncoder;
import com.mossle.core.export.Exportor;
import com.mossle.core.mapper.BeanMapper;
import com.mossle.core.page.Page;
import com.mossle.core.query.PropertyFilter;
import com.mossle.core.spring.MessageHelper;
import com.mossle.party.PartyConstants;
import com.mossle.party.persistence.domain.PartyEntity;
import com.mossle.party.persistence.manager.PartyEntityManager;
import com.mossle.user.PersonInfoConstants;
import com.mossle.user.persistence.domain.AccountAvatar;
import com.mossle.user.persistence.domain.AccountCredential;
import com.mossle.user.persistence.domain.AccountInfo;
import com.mossle.user.persistence.domain.PersonInfo;
import com.mossle.user.persistence.manager.AccountAvatarManager;
import com.mossle.user.persistence.manager.AccountCredentialManager;
import com.mossle.user.persistence.manager.AccountInfoManager;
import com.mossle.user.persistence.manager.PersonInfoManager;
import com.mossle.user.publish.UserPublisher;

import freemarker.template.utility.StringUtil;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("user")
public class AccountInfoController {
    private AccountInfoManager accountInfoManager;
    private AccountCredentialManager accountCredentialManager;
    private AccountAvatarManager accountAvatarManager;
    private PersonInfoManager personInfoManager;
    private UserCache userCache;
    private MessageHelper messageHelper;
    private Exportor exportor;
    private BeanMapper beanMapper = new BeanMapper();
    private CustomPasswordEncoder customPasswordEncoder;
    private StoreConnector storeConnector;
    private UserPublisher userPublisher;
    private TenantHolder tenantHolder;
    private CurrentUserHolder currentUserHolder;
    private PartyEntityManager partyEntityManager;
    
    @RequestMapping("account-info-list")
    public String list(@ModelAttribute Page page,@RequestParam Map<String, Object> parameterMap, Model model) {
    	
        String tenantId = tenantHolder.getTenantId();
        Long accountId = Long.parseLong(currentUserHolder.getUserId());
        
        List<PropertyFilter> propertyFilters = PropertyFilter.buildFromMap(parameterMap);
        propertyFilters.add(new PropertyFilter("EQS_tenantId", tenantId));
        propertyFilters.add(new PropertyFilter("EQS_delFlag", PersonInfoConstants.DELETE_FLAG_NO));
        
        if (!PartyConstants.ADMIN_USER_ID.equals(accountId)) {
        	propertyFilters.add(new PropertyFilter("EQL_addUserId_OR_id", currentUserHolder.getUserId()));
        }
        
        page = accountInfoManager.pagedQuery(page, propertyFilters);

        model.addAttribute("page", page);

        return "user/account-info-list";
    }

    @RequestMapping("account-info-input")
    public String input(@RequestParam(value = "id", required = false) Long id,
            Model model) {
        AccountInfo accountInfo = null;

        if (id != null) {
            accountInfo = accountInfoManager.get(id);
        } else {
            accountInfo = new AccountInfo();
        }

        model.addAttribute("model", accountInfo);

        return "user/account-info-input";
    }

    @RequestMapping("account-info-save")
    public String save(@ModelAttribute AccountInfo accountInfo,
            @RequestParam(value = "password", required = false) String password,
            @RequestParam(value = "confirmPassword", required = false) String confirmPassword,
            RedirectAttributes redirectAttributes) throws Exception {
    	
        String tenantId = tenantHolder.getTenantId();
        Long accountId = Long.parseLong(currentUserHolder.getUserId());
        
        // 先进行校验
        if (password != null) {
            if (!password.equals(confirmPassword)) {
                messageHelper.addFlashMessage(redirectAttributes,
                        "user.user.input.passwordnotequals", "两次输入密码不符");

                // TODO: 还要填充schema
                return "user/account-info-input";
            }
        }

        // 再进行数据复制
        AccountInfo dest = null;
        Long id = accountInfo.getId();

        if (id != null) {
            dest = accountInfoManager.get(id);

            if (accountInfo.getStatus() == null) {
                accountInfo.setStatus("disabled");
            }

            beanMapper.copy(accountInfo, dest);
        } else {
            dest = accountInfo;

            if (accountInfo.getStatus() == null) {
                accountInfo.setStatus("disabled");
            }

            dest.setCreateTime(new Date());
            dest.setTenantId(tenantId);
            
            // zyl 2017-07-11   增加如下代码
            dest.setPasswordRequired("required");
            dest.setLocked("unlocked");
            dest.setNickName(dest.getDisplayName());
            dest.setAddUserId(accountId);
            dest.setDelFlag(PersonInfoConstants.DELETE_FLAG_NO);
            if (StringUtils.isBlank(dest.getType())) {
            	dest.setType("employee");
            }
            // ================== end ===================
        }

        accountInfoManager.save(dest);

        if (dest.getCode() == null) {
            dest.setCode(Long.toString(dest.getId()));
            accountInfoManager.save(dest);
        }
        
        PartyEntity entity = partyEntityManager.get(Long.parseLong(dest.getCode()));
        entity.setRef(Long.toString(dest.getId()));
        partyEntityManager.save(entity);
        
        
        if (password != null) {
            String hql = "from AccountCredential where accountInfo=? and catalog='default'";
            AccountCredential accountCredential = accountCredentialManager
                    .findUnique(hql, accountInfo);

            if (accountCredential == null) {
                accountCredential = new AccountCredential();
                accountCredential.setAccountInfo(accountInfo);
                accountCredential.setType("normal");
                accountCredential.setCatalog("default");
                
                // zyl 2017-07-11
                accountCredential.setTenantId(accountInfo.getTenantId());
                accountCredential.setStatus(accountInfo.getStatus());
                accountCredential.setCouldModify("yes");
                
            }

            if (customPasswordEncoder != null) {
                accountCredential.setPassword(customPasswordEncoder
                        .encode(password));
            } else {
                accountCredential.setPassword(password);
            }

            Calendar calendar = Calendar.getInstance();
            Date now = calendar.getTime();
            // calendar.add(Calendar.MONTH, 3);
            accountCredential.setModifyTime(now);
            accountCredentialManager.save(accountCredential);
        }

        messageHelper.addFlashMessage(redirectAttributes, "core.success.save",
                "保存成功");

        UserDTO userDto = new UserDTO();
        userDto.setId(Long.toString(dest.getId()));
        userDto.setUsername(dest.getUsername());
        userDto.setRef(dest.getCode());
        userDto.setUserRepoRef(tenantId);
        userDto.setAddUserId(Long.toString(accountId));
        userCache.removeUser(userDto);

        if (id != null) {
            userPublisher.notifyUserUpdated(this.convertUserDto(dest));
        } else {
            userPublisher.notifyUserCreated(this.convertUserDto(dest));
        }

        return "redirect:/user/account-info-list.do";
    }

    // 次方法弃用   zyl 2017-08-14
    @RequestMapping("account-info-remove")
    public String remove(@RequestParam("selectedItem") List<Long> selectedItem,
            RedirectAttributes redirectAttributes) {
        String tenantId = tenantHolder.getTenantId();
        List<AccountInfo> accountInfos = accountInfoManager
                .findByIds(selectedItem);

        for (AccountInfo accountInfo : accountInfos) {
            for (AccountCredential accountCredential : accountInfo
                    .getAccountCredentials()) {
                accountCredentialManager.remove(accountCredential);
            }

            for (AccountAvatar accountAvatar : accountInfo.getAccountAvatars()) {
                accountAvatarManager.remove(accountAvatar);
            }

            accountInfoManager.remove(accountInfo);

            UserDTO userDto = new UserDTO();
            userDto.setId(Long.toString(accountInfo.getId()));
            userDto.setUsername(accountInfo.getUsername());
            userDto.setRef(accountInfo.getCode());
            userDto.setUserRepoRef(tenantId);
            userCache.removeUser(userDto);
            userPublisher.notifyUserRemoved(this.convertUserDto(accountInfo));
        }

        messageHelper.addFlashMessage(redirectAttributes,
                "core.success.delete", "删除成功");

        return "redirect:/user/account-info-list.do";
    }

    @RequestMapping("account-info-active")
    public String active(@RequestParam("id") Long id,
            RedirectAttributes redirectAttributes) {
        AccountInfo accountInfo = accountInfoManager.get(id);
        accountInfo.setStatus("active");
        accountInfoManager.save(accountInfo);
        messageHelper.addFlashMessage(redirectAttributes,
                "core.success.update", "操作成功");

        userPublisher.notifyUserUpdated(this.convertUserDto(accountInfo));

        return "redirect:/user/account-info-list.do";
    }

    @RequestMapping("account-info-disable")
    public String disable(@RequestParam("id") Long id,
            RedirectAttributes redirectAttributes) {
        AccountInfo accountInfo = accountInfoManager.get(id);
        accountInfo.setStatus("disabled");
        accountInfoManager.save(accountInfo);
        messageHelper.addFlashMessage(redirectAttributes,
                "core.success.update", "操作成功");

        userPublisher.notifyUserUpdated(this.convertUserDto(accountInfo));

        return "redirect:/user/account-info-list.do";
    }

    @RequestMapping("account-info-checkUsername")
    @ResponseBody
    public boolean checkUsername(@RequestParam("username") String username,
            @RequestParam(value = "id", required = false) Long id)
            throws Exception {
        String tenantId = tenantHolder.getTenantId();
        String hql = "from AccountInfo where delFlag = '0' and username=? and tenantId=?";
        Object[] params = { username, tenantId };

        if (id != null) {
            hql = "from AccountInfo where delFlag = '0'and username=? and tenantId=? and id<>?";
            params = new Object[] { username, tenantId, id };
        }

        boolean result = accountInfoManager.findUnique(hql, params) == null;

        return result;
    }

    public UserDTO convertUserDto(AccountInfo accountInfo) {
        String hql = "from PersonInfo where code=? and tenantId=?";
        PersonInfo personInfo = personInfoManager.findUnique(hql,
                accountInfo.getCode(), accountInfo.getTenantId());

        UserDTO userDto = new UserDTO();
        userDto.setId(Long.toString(accountInfo.getId()));
        userDto.setUsername(accountInfo.getUsername());
        userDto.setDisplayName(accountInfo.getDisplayName());
        userDto.setNickName(accountInfo.getNickName());
        userDto.setUserRepoRef(accountInfo.getTenantId());
        userDto.setAddUserId(Long.toString(accountInfo.getAddUserId()));
        if (personInfo != null) {
            userDto.setEmail(personInfo.getEmail());
            userDto.setMobile(personInfo.getCellphone());
        }

        return userDto;
    }

    // ~ ======================================================================
    @Resource
    public void setAccountInfoManager(AccountInfoManager accountInfoManager) {
        this.accountInfoManager = accountInfoManager;
    }

    @Resource
    public void setAccountCredentialManager(
            AccountCredentialManager accountCredentialManager) {
        this.accountCredentialManager = accountCredentialManager;
    }

    @Resource
    public void setAccountAvatarManager(
            AccountAvatarManager accountAvatarManager) {
        this.accountAvatarManager = accountAvatarManager;
    }

    @Resource
    public void setPersonInfoManager(PersonInfoManager personInfoManager) {
        this.personInfoManager = personInfoManager;
    }

    @Resource
    public void setUserCache(UserCache userCache) {
        this.userCache = userCache;
    }

    @Resource
    public void setMessageHelper(MessageHelper messageHelper) {
        this.messageHelper = messageHelper;
    }

    @Resource
    public void setCustomPasswordEncoder(
            CustomPasswordEncoder customPasswordEncoder) {
        this.customPasswordEncoder = customPasswordEncoder;
    }

    @Resource
    public void setExportor(Exportor exportor) {
        this.exportor = exportor;
    }

    @Resource
    public void setStoreConnector(StoreConnector storeConnector) {
        this.storeConnector = storeConnector;
    }

    @Resource
    public void setUserPublisher(UserPublisher userPublisher) {
        this.userPublisher = userPublisher;
    }

    @Resource
    public void setTenantHolder(TenantHolder tenantHolder) {
        this.tenantHolder = tenantHolder;
    }
    
    @Resource
	public void setCurrentUserHolder(CurrentUserHolder currentUserHolder) {
		this.currentUserHolder = currentUserHolder;
	}

    @Resource
	public void setPartyEntityManager(PartyEntityManager partyEntityManager) {
		this.partyEntityManager = partyEntityManager;
	}
    
    
}
