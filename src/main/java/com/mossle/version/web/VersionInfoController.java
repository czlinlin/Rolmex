package com.mossle.version.web;

import com.mossle.api.tenant.TenantHolder;
import com.mossle.common.utils.FileUploadAPI;
import com.mossle.common.utils.WebAPI;
import com.mossle.core.auth.CurrentUserHolder;
import com.mossle.core.page.Page;
import com.mossle.core.query.PropertyFilter;
import com.mossle.core.spring.MessageHelper;
import com.mossle.internal.store.persistence.domain.StoreInfo;
import com.mossle.version.persistence.domain.VersionInfo;
import com.mossle.version.persistence.manager.VersionInfoManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by wanghan on 2017\9\29 0029.
 */
@Controller
@RequestMapping("version")
public class VersionInfoController {
    private VersionInfoManager versionInfoManager;
    private MessageHelper messageHelper;
    private CurrentUserHolder currentUserHolder;
    private TenantHolder tenantHolder;
    private FileUploadAPI fileUploadAPI;
    private WebAPI webAPI;

    //版本列表
    @RequestMapping("version-info-list")
    public String manage(@ModelAttribute Page page, @RequestParam Map<String, Object> parameterMap, Model model) {
        List<PropertyFilter> propertyFilters = PropertyFilter.buildFromMap(parameterMap);
        if (page != null) {
            page.setDefaultOrder("savetime", page.DESC);
        }
        page = versionInfoManager.pagedQuery(page, propertyFilters);
        model.addAttribute("page", page);
        return "version/version-info-list";
    }

    //新建版本
    @RequestMapping("version-info-input")
    public String input() {

        return "version/version-info-input";
    }

    //修改版本（只有最后一次添加的才可以修改）
    @RequestMapping("version-info-modify")
    public String modeify(@RequestParam(value = "id", required = true) Long id, Model model) throws Exception {
        VersionInfo versionInfo = versionInfoManager.get(id);
        //历史附件
        model.addAttribute("picUrl", webAPI.getViewUrl());

        List<StoreInfo> list = fileUploadAPI.getStore("OA/version", Long.toString(versionInfo.getId()));
        model.addAttribute("StoreInfos", list);
        model.addAttribute("versionInfo", versionInfo);
        return "version/version-info-modify";
    }

    //版本保存
    @RequestMapping("version-info-save")
    public String save(@ModelAttribute VersionInfo versionInfo,
                       @RequestParam(value = "id", required = false) Long id,
                       RedirectAttributes redirectAttributes,
                       @RequestParam(value = "iptdels", required = false) String iptdels,
                       @RequestParam(value = "files", required = false) MultipartFile[] files) throws Exception {
        String tenantId = tenantHolder.getTenantId();
        versionInfo.setSavetime(new Date());
        versionInfoManager.save(versionInfo);
        if (iptdels != null && !iptdels.equals("")) {
            fileUploadAPI.uploadFileDel(iptdels, Long.toString(versionInfo.getId()));
        }
        fileUploadAPI.uploadFile(files, tenantId, Long.toString(versionInfo.getId()), "OA/version");

        messageHelper.addFlashMessage(redirectAttributes, "core.success.save", "保存成功");
        return "redirect:/version/version-info-list.do";
    }

    //版本详情
    @RequestMapping("version-info-detail")
    public String detail(@RequestParam(value = "id", required = true) Long id, Model model) throws Exception {
        VersionInfo versionInfo = versionInfoManager.get(id);
        // 查询附件
        model.addAttribute("picUrl", webAPI.getViewUrl());
        List<StoreInfo> list = fileUploadAPI.getStore("OA/version", Long.toString(versionInfo.getId()));
        model.addAttribute("StoreInfos", list);
        model.addAttribute("versionInfo", versionInfo);
        return "version/version-info-detail";
    }

    @Resource
    public void setFileUploadAPI(FileUploadAPI fileUploadAPI) {
        this.fileUploadAPI = fileUploadAPI;
    }

    @Resource
    public void setWebAPI(WebAPI webAPI) {
        this.webAPI = webAPI;
    }

    @Resource
    public void setVersionInfoManager(VersionInfoManager versionInfoManager) {
        this.versionInfoManager = versionInfoManager;
    }

    @Resource
    public void setCurrentUserHolder(CurrentUserHolder currentUserHolder) {
        this.currentUserHolder = currentUserHolder;
    }
    @Resource
    public void setTenantHolder(TenantHolder tenantHolder) {
        this.tenantHolder = tenantHolder;
    }

    @Resource
    public void setMessageHelper(MessageHelper messageHelper) {
        this.messageHelper = messageHelper;
    }
}
