package com.mossle.user.web;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.mossle.core.auth.CurrentUserHolder;
import com.mossle.core.export.Exportor;
import com.mossle.core.mapper.BeanMapper;
import com.mossle.core.page.Page;
import com.mossle.core.query.PropertyFilter;
import com.mossle.core.spring.MessageHelper;
import com.mossle.party.PartyConstants;
import com.mossle.user.persistence.domain.AccountCredential;
import com.mossle.user.persistence.domain.AccountInfo;
import com.mossle.user.persistence.domain.AccountLockInfo;
import com.mossle.user.persistence.domain.AccountLockLog;
import com.mossle.user.persistence.manager.AccountCredentialManager;
import com.mossle.user.persistence.manager.AccountInfoManager;
import com.mossle.user.persistence.manager.AccountLockInfoManager;
import com.mossle.user.persistence.manager.AccountLockLogManager;
import com.mossle.user.service.AccountLockService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("user")
public class AccountLockInfoController {
    private AccountLockInfoManager accountLockInfoManager;
    private AccountLockLogManager accountLockLogManager;
    private AccountLockService accountLockService;
    private Exportor exportor;
    private BeanMapper beanMapper = new BeanMapper();
    private MessageHelper messageHelper;
    private AccountInfoManager accountInfoManager;
    private AccountCredentialManager accountCredentialManager;
    private CurrentUserHolder currentUserHolder;

    @RequestMapping("account-lock-info-list")
    public String list(@ModelAttribute Page page,
                       @RequestParam Map<String, Object> parameterMap, Model model) {
        List<PropertyFilter> propertyFilters = PropertyFilter.buildFromMap(parameterMap);
        String locked = "locked";
        String userId = currentUserHolder.getUserId();
        if (userId.equals("2")) {
            propertyFilters.add(new PropertyFilter("EQS_locked", locked));
        } else {
            propertyFilters.add(new PropertyFilter("EQS_locked", locked));
            propertyFilters.add(new PropertyFilter("EQL_addUserId", currentUserHolder.getUserId()));
        }

        page.setDefaultOrder("lockTime", "DESC");
        page = accountInfoManager.pagedQuery(page, propertyFilters);

        model.addAttribute("page", page);

        return "user/account-lock-info-list";
    }

    @RequestMapping("account-lock-info-unlock")
    public String unlock(@RequestParam("id") Long id,
    					@RequestParam("partyEntityId") Long partyEntityId,
                         RedirectAttributes redirectAttributes) {
        AccountInfo accountInfo = accountInfoManager.findUniqueBy("id", id);
        accountInfo.setLocked("unlocked");
        accountInfoManager.save(accountInfo);
        AccountCredential accountCredential = accountCredentialManager.findUniqueBy("accountInfo.id", id);
        accountCredential.setFailedPasswordCount(0);
        accountCredentialManager.save(accountCredential);
        messageHelper.addFlashMessage(redirectAttributes, "account.unlock",
                "解锁成功");
        
        return "redirect:/user/person-info-list-i.do?partyEntityId=" + partyEntityId +
                "&partyStructTypeId=" + PartyConstants.PARTY_STRUCT_TYPE_ORG;
        //return "redirect:/user/account-lock-info-list.do";
    }

    // ~ ======================================================================
    @Resource
    public void setAccountLockInfoManager(
            AccountLockInfoManager accountLockInfoManager) {
        this.accountLockInfoManager = accountLockInfoManager;
    }

    @Resource
    public void setAccountLockLogManager(
            AccountLockLogManager accountLockLogManager) {
        this.accountLockLogManager = accountLockLogManager;
    }

    @Resource
    public void setAccountLockService(AccountLockService accountLockService) {
        this.accountLockService = accountLockService;
    }

    @Resource
    public void setExportor(Exportor exportor) {
        this.exportor = exportor;
    }

    @Resource
    public void setAccountInfoManager(AccountInfoManager accountInfoManager) {
        this.accountInfoManager = accountInfoManager;
    }

    @Resource
    public void setAccountCredentialManager(AccountCredentialManager accountCredentialManager) {
        this.accountCredentialManager = accountCredentialManager;
    }

    @Resource
    public void setCurrentUserHolder(CurrentUserHolder currentUserHolder) {
        this.currentUserHolder = currentUserHolder;
    }

    @Resource
    public void setMessageHelper(MessageHelper messageHelper) {
        this.messageHelper = messageHelper;
    }
}
