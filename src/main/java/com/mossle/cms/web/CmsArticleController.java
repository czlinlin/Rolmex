package com.mossle.cms.web;


import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.mossle.api.notification.NotificationConnector;
import com.mossle.api.party.PartyConnector;
import com.mossle.api.party.PartyDTO;
import com.mossle.api.store.StoreConnector;
import com.mossle.api.tenant.TenantHolder;

import com.mossle.cms.persistence.domain.CmsArticle;
import com.mossle.cms.persistence.domain.CmsCatalog;
import com.mossle.cms.persistence.domain.CmsRange;
import com.mossle.cms.persistence.manager.*;
import com.mossle.cms.service.RenderService;

import com.mossle.common.utils.FileUploadAPI;
import com.mossle.common.utils.WebAPI;
import com.mossle.core.annotation.Log;
import com.mossle.core.auth.CurrentUserHolder;
import com.mossle.core.export.Exportor;

import com.mossle.core.mapper.BeanMapper;
import com.mossle.core.mapper.JsonMapper;
import com.mossle.core.page.Page;
import com.mossle.core.query.PropertyFilter;
import com.mossle.core.spring.MessageHelper;

import com.mossle.internal.store.persistence.domain.StoreInfo;

import com.mossle.msg.MsgConstants;
import com.mossle.party.persistence.domain.PartyEntity;
import com.mossle.party.persistence.manager.PartyEntityManager;
import com.mossle.party.support.PartyOrgConnector;
import com.mossle.user.persistence.domain.PersonInfo;
import com.mossle.user.persistence.manager.PersonInfoManager;
import org.apache.commons.collections.map.HashedMap;
import org.springframework.format.datetime.DateFormatter;
import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 公告.
 */
@Controller
@RequestMapping("cms")
public class CmsArticleController {
    private CmsArticleManager cmsArticleManager;
    private CmsCatalogManager cmsCatalogManager;
    private CmsAttachmentManager cmsAttachmentManager;
    private CmsCommentManager cmsCommentManager;
    private Exportor exportor;
    private BeanMapper beanMapper = new BeanMapper();
    private MessageHelper messageHelper;
    private RenderService renderService;
    private StoreConnector storeConnector;
    private JsonMapper jsonMapper = new JsonMapper();
    private CurrentUserHolder currentUserHolder;
    private TenantHolder tenantHolder;
    private PartyEntityManager partyEntityManager;
    private PartyOrgConnector partyOrgConnector;
    private CmsRangeManager cmsRangeManager;
    private NotificationConnector notificationConnector;//发送消息
    private PersonInfoManager personInfoManager;

    private FileUploadAPI fileUploadAPI;
    private WebAPI webAPI;
    private PartyConnector partyConnector;

    /**
     * 已发列表.
     */
    @RequestMapping("cms-article-list")
    @Log(desc = "已发公告", action = "search", operationDesc = "内容管理-公告管理-已发公告-查看")
    public String list(@ModelAttribute Page page,
                       @RequestParam Map<String, Object> parameterMap, Model model) {
        String userId = currentUserHolder.getUserId();
        List<PropertyFilter> propertyFilters = PropertyFilter
                .buildFromMap(parameterMap);
        propertyFilters.add(new PropertyFilter("EQS_userId", userId));
        propertyFilters.add(new PropertyFilter("EQI_status", "1"));
        page.setDefaultOrder("publishTime", page.DESC);
        page = cmsArticleManager.pagedQuery(page, propertyFilters);
        model.addAttribute("page", page);

        return "cms/cms-article-list";
    }

    /**
     * 新建公告.
     */
    @RequestMapping("cms-article-input")
    @Log(desc = "新建公告", action = "input", operationDesc = "内容管理-公告管理-新建公告")
    public String input(@RequestParam(value = "id", required = false) Long id,
                        Model model) {
        if (id != null) {
            CmsArticle cmsArticle = cmsArticleManager.get(id);
            model.addAttribute("model", cmsArticle);
        }
        return "cms/cms-article-input";
    }

    /**
     * 新建集团公告.
     */
    @RequestMapping("cms-article-allInput")
    @Log(desc = "新建集团公告", action = "allInput", operationDesc = "内容管理-公告管理-新建集团公告")
    public String allInput(@RequestParam(value = "id", required = false) Long id,
                           Model model) {
        if (id != null) {
            CmsArticle cmsArticle = cmsArticleManager.get(id);
            model.addAttribute("model", cmsArticle);
        }
        return "cms/cms-article-allInput";
    }

    /**
     * 草稿公告.
     */
    @RequestMapping("cms-article-temp")
    @Log(desc = "草稿公告", action = "search", operationDesc = "内容管理-公告管理-草稿公告")
    public String temp(@ModelAttribute Page page,
                       @RequestParam Map<String, Object> parameterMap,
                       Model model) {
        String userId = currentUserHolder.getUserId();
        List<PropertyFilter> propertyFilters = PropertyFilter
                .buildFromMap(parameterMap);
        propertyFilters.add(new PropertyFilter("EQI_status", "0"));
        propertyFilters.add(new PropertyFilter("EQS_userId", userId));
        page.setDefaultOrder("createTime", page.DESC);
        page = cmsArticleManager.pagedQuery(page, propertyFilters);
        model.addAttribute("page", page);
        return "cms/cms-article-temp";
    }

    /**
     * 接收公告.
     */
    @RequestMapping("cms-article-tome")
    @Log(desc = "接收公告", action = "search", operationDesc = "内容管理-公告管理-接收公告")
    public String tome(@ModelAttribute Page page,
                       @RequestParam Map<String, Object> parameterMap, Model model) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String now = formatter.format(new Date());
        String userId = currentUserHolder.getUserId();
        PartyDTO partyDTO = partyConnector.findDepartmentById(userId);
        //当前登录人直接组织id
        String partyEntityId = partyDTO == null ? "" : partyDTO.getId();
        List<PropertyFilter> propertyFilters = PropertyFilter.buildFromMap(parameterMap);
        propertyFilters.add(new PropertyFilter("EQI_status", "1"));//1：发布状态的公告
        propertyFilters.add(new PropertyFilter("LED_startTime", now));//有效开始日期
        propertyFilters.add(new PropertyFilter("GED_endTime", now));//有效结束日期
        page = cmsArticleManager.pagedQueryByCms(page, partyEntityId, propertyFilters);
        List<CmsArticle> cmsArticles = (List<CmsArticle>) page.getResult();
        for (CmsArticle cms : cmsArticles) {
            Set<CmsRange> cmsRanges = cms.getCmsRanges();
            for (CmsRange range : cmsRanges) {
                if (range.getPartyId() == partyEntityId) {
                    range.getCmsArticle();
                }
            }
        }
        model.addAttribute("page", page);

        return "cms/cms-article-tome";
    }


    /**
     * 保存公告.
     */
    @RequestMapping("cms-article-save")
    @Log(desc = "公告", action = "save", operationDesc = "内容管理-公告管理-保存公告")
    public String save(@ModelAttribute CmsArticle cmsArticle,
                       @RequestParam(value = "cmsCatalogId") Long cmsCatalogId,
                       @RequestParam(value = "files", required = false) MultipartFile[] files,
                       @RequestParam(value = "iptdels", required = false) String iptdels,
                       RedirectAttributes redirectAttributes) throws Exception {
        String tenantId = tenantHolder.getTenantId();
        String userId = currentUserHolder.getUserId();
        cmsArticle.setUserId(userId);
        cmsArticle.setTenantId(tenantId);
        CmsCatalog cmsCatalog = cmsCatalogManager.findUniqueBy("id", cmsCatalogId);
        cmsArticle.setCmsCatalog(cmsCatalog);
        String partyRange = cmsArticle.getPartyEntityId();
        String[] split_data0 = partyRange.split(",");
        if (cmsArticle.getId() != null) {
            //草稿修改公告直接发布
            List<CmsRange> cmsRangelist = cmsRangeManager.findBy("cmsArticle.id", cmsArticle.getId());
            cmsRangeManager.removeAll(cmsRangelist);
        }

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat formatterNew = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String temp = formatter.format(cmsArticle.getEndTime()) + " 23:59:59";
        Date newDate2 = formatterNew.parse(temp);
        cmsArticle.setEndTime(newDate2);

        cmsArticleManager.save(cmsArticle);
        for (int j = 0; j < split_data0.length; j++) {
            CmsRange cmsRange = new CmsRange();
            cmsRange.setPartyId(split_data0[j].toString());
            cmsRange.setCmsArticle(cmsArticle);
            cmsRangeManager.save(cmsRange);
        }
        //附件保存及修改
        if (iptdels != null && !iptdels.equals("")) {
            fileUploadAPI.uploadFileDel(iptdels, Long.toString(cmsArticle.getId()));
        }
        fileUploadAPI.uploadFile(files, tenantId, Long.toString(cmsArticle.getId()), "OA/cmsArticle");
        if (cmsArticle.getStatus() == 0) {
            cmsArticle.setCreateTime(new Date());
            cmsArticleManager.save(cmsArticle);
            messageHelper.addFlashMessage(redirectAttributes, "core.success.save",
                    "保存成功");
            return "redirect:/cms/cms-article-temp.do";
        }
        cmsArticle.setPublishTime(new Date());
        cmsArticleManager.save(cmsArticle);
        messageHelper.addFlashMessage(redirectAttributes, "core.success.save",
                "发布成功");

        /*公告发布，发送消息*/
      /*  //公告有效开始时间和当前时间的比较
        Date start = cmsArticle.getStartTime();
        Date now = formatterNew.parse(formatterNew.format(new Date()));
        boolean check = false;
        if (start.before(now)) {
            check = true;
        }
        if (cmsArticle.getStatus() == 1 && check == true) {
            *//*公告发布后，须向可查看公告的用户添加提醒消息，消息格式如下：
              标题：[公告标题]公告提醒；
              内容：您有新公告[公告标题]，请查看。；*//*
            for (int j = 0; j < split_data0.length; j++) {
                List<PersonInfo> personInfos = personInfoManager.findBy("departmentCode", split_data0[j]);
                for (PersonInfo personInfo : personInfos) {
                    String title = "[" + cmsArticle.getTitle() + "]公告提醒";
                    String content = "您有公告[" + cmsArticle.getTitle() + "]，请查看。";
                    String receiver = personInfo.getId().toString();
                    notificationConnector.send(
                            cmsArticle.getId().toString(),
                            tenantId,
                            currentUserHolder.getUserId().toString(),
                            receiver,
                            title,
                            content, MsgConstants.MSG_TYPE_NOTICE);
                }
            }
        }else{
            cmsArticle.setWeight(1);
            cmsArticleManager.save(cmsArticle);
        }*/
        return "redirect:/cms/cms-article-list.do";
    }


    /**
     * 删除文章.
     */
    @RequestMapping("cms-article-remove")
    public String remove(@RequestParam("selectedItem") List<Long> selectedItem,
                         RedirectAttributes redirectAttributes) {
        List<CmsArticle> cmsArticles = cmsArticleManager
                .findByIds(selectedItem);

        for (CmsArticle cmsArticle : cmsArticles) {
            cmsCommentManager.removeAll(cmsArticle.getCmsComments());
            cmsArticleManager.remove(cmsArticle);
        }

        messageHelper.addFlashMessage(redirectAttributes,
                "core.success.delete", "删除成功");

        return "redirect:/cms/cms-article-list.do";
    }


    /**
     * 检查重名.
     */
    @RequestMapping("cms-article-checkName")
    @ResponseBody
    public boolean checkName(@RequestParam("name") String name,
                             @RequestParam(value = "id", required = false) Long id)
            throws Exception {
        String tenantId = tenantHolder.getTenantId();
        String hql = "from CmsArticle where name=? and tenantId=?";
        Object[] params = {name, tenantId};

        if (id != null) {
            hql = "from CmsArticle where name=? and tenantId=? and id<>?";
            params = new Object[]{name, tenantId, id};
        }

        CmsArticle cmsArticle = cmsArticleManager.findUnique(hql, params);

        boolean result = (cmsArticle == null);

        return result;
    }

    /**
     * 公告详情.
     */
    @RequestMapping("cms-article-view")
    @Log(desc = "公告详情", action = "detail", operationDesc = "内容管理-公告管理-公告详情（管理员）")
    public String view(@RequestParam("id") Long id, @ModelAttribute Page page,
                       Model model) throws Exception {
        String partyEntityNames = "";
        String partyEntityList = "";
        CmsArticle cmsArticle = cmsArticleManager.get(id);
        String partyEntityId = cmsArticle.getPartyEntityId();
        //公告范围组织机构显示
        partyEntityList = partyEntityManager.partyEntitieName(partyEntityId);


        model.addAttribute("picUrl", webAPI.getViewUrl());
        List<StoreInfo> list = fileUploadAPI.getStore("OA/cmsArticle", Long.toString(cmsArticle.getId()));
        model.addAttribute("StoreInfos", list);
        model.addAttribute("partyEntityNames", partyEntityList);
        model.addAttribute("model", cmsArticle);
        return "cms/cms-article-view";
    }


    /**
     * 公告详情.
     */
    @RequestMapping("cms-article-meview")
    @Log(desc = "公告详情", action = "detail", operationDesc = "内容管理-公告管理-公告详情")
    public String meView(@RequestParam("id") Long id, @ModelAttribute Page page,
                         Model model) throws Exception {
        CmsArticle cmsArticle = cmsArticleManager.get(id);
        model.addAttribute("picUrl", webAPI.getViewUrl());
        List<StoreInfo> list = fileUploadAPI.getStore("OA/cmsArticle", Long.toString(cmsArticle.getId()));
        model.addAttribute("StoreInfos", list);
        model.addAttribute("model", cmsArticle);
        return "cms/cms-article-meview";
    }


    /**
     * 修改公告.
     */
    @RequestMapping("cms-article-modify")
    @Log(desc = "修改公告", action = "modify", operationDesc = "内容管理-公告管理-修改公告")
    public String modify(@RequestParam("id") Long id,
                         Model model) throws Exception {
        CmsArticle cmsArticle = cmsArticleManager.get(id);
        String partyEntityId = cmsArticle.getPartyEntityId();
        String partyEntityNames = "";
        String[] split_data0 = partyEntityId.split(",");
        for (int j = 0; j < split_data0.length; j++) {
            PartyEntity partyEntity = partyEntityManager.findUniqueBy("id", Long.parseLong(split_data0[j]));
            String partyEntityName = partyEntity.getName();
            partyEntityNames = partyEntityNames + partyEntityName;
            if (j < split_data0.length - 1) {
                partyEntityNames = partyEntityNames + ",";
            }
        }

        model.addAttribute("picUrl", webAPI.getViewUrl());
        List<StoreInfo> list = fileUploadAPI.getStore("OA/cmsArticle", Long.toString(cmsArticle.getId()));
        model.addAttribute("StoreInfos", list);
        model.addAttribute("model", cmsArticle);
        model.addAttribute("partyEntityNames", partyEntityNames);
        return "cms/cms-article-modify";
    }

    /* *//**
     * 上传图片.
     *//*
    @RequestMapping("cms-article-uploadImage")
    @ResponseBody
    public String uploadImage(@RequestParam("CKEditorFuncNum") String callback,
                              @RequestParam("upload") MultipartFile attachment) throws Exception {
        String tenantId = tenantHolder.getTenantId();
        StoreDTO storeDto = storeConnector.saveStore("cms/html/r/images",
                new MultipartFileDataSource(attachment), tenantId);

        return "<script type='text/javascript'>"
                + "window.parent.CKEDITOR.tools.callFunction(" + callback
                + ",'" + "r/images/" + storeDto.getKey() + "','')"
                + "</script>";
    }


    *//**
     * 上传.
     *//*
    @RequestMapping("cms-article-upload")
    @ResponseBody
    public String upload(@RequestParam("id") Long id,
                         @RequestParam("files[]") MultipartFile attachment) throws Exception {
        String tenantId = tenantHolder.getTenantId();
        StoreDTO storeDto = storeConnector.saveStore("cms/html/r/image",
                new MultipartFileDataSource(attachment), tenantId);
        CmsArticle cmsArticle = cmsArticleManager.get(id);
        CmsAttachment cmsAttachment = new CmsAttachment();
        cmsAttachment.setCmsArticle(cmsArticle);
        cmsAttachment.setName(attachment.getOriginalFilename());
        cmsAttachment.setPath(storeDto.getKey());
        cmsAttachmentManager.save(cmsAttachment);

        Map<String, Object> data = new HashMap<String, Object>();
        List<Map<String, Object>> files = new ArrayList<Map<String, Object>>();
        data.put("files", files);

        Map<String, Object> map = new HashMap<String, Object>();
        files.add(map);
        map.put("name", attachment.getOriginalFilename());
        map.put("url", "../rs/cms/image?key=" + storeDto.getKey());

        // map.put("thumbnailUrl", "./rs/cms/image?key=" + storeDto.getKey());
        return jsonMapper.toJson(data);
    }

    *//**
     * 图库类文章.
     *//*
    @RequestMapping("cms-article-image")
    public String imageArticle(
            @RequestParam(value = "id", required = false) Long id, Model model) {
        String tenantId = tenantHolder.getTenantId();

        if (id == null) {
            CmsArticle cmsArticle = new CmsArticle();
            cmsArticle.setUserId(currentUserHolder.getUserId());
            cmsArticle.setCreateTime(new Date());
            cmsArticle.setTenantId(tenantId);
            cmsArticle.setContent("");
            cmsArticle.setType(CmsConstants.TYPE_IMAGE);
            cmsArticleManager.save(cmsArticle);
            model.addAttribute("model", cmsArticle);
        } else {
            CmsArticle cmsArticle = cmsArticleManager.get(id);
            model.addAttribute("model", cmsArticle);
        }

        model.addAttribute("cmsCatalogs",
                cmsCatalogManager.findBy("tenantId", tenantId));

        return "cms/cms-article-image";
    }

    *//**
     * 音频类文章.
     *//*
    @RequestMapping("cms-article-audio")
    public String audioArticle(
            @RequestParam(value = "id", required = false) Long id, Model model) {
        String tenantId = tenantHolder.getTenantId();

        if (id == null) {
            CmsArticle cmsArticle = new CmsArticle();
            cmsArticle.setUserId(currentUserHolder.getUserId());
            cmsArticle.setCreateTime(new Date());
            cmsArticle.setTenantId(tenantId);
            cmsArticle.setContent("");
            cmsArticle.setType(CmsConstants.TYPE_AUDIO);
            cmsArticleManager.save(cmsArticle);
            model.addAttribute("model", cmsArticle);
        } else {
            CmsArticle cmsArticle = cmsArticleManager.get(id);
            model.addAttribute("model", cmsArticle);
        }

        model.addAttribute("cmsCatalogs",
                cmsCatalogManager.findBy("tenantId", tenantId));

        return "cms/cms-article-audio";
    }


    @RequestMapping("cms-article-video")
    public String videoArticle(
            @RequestParam(value = "id", required = false) Long id, Model model) {
        String tenantId = tenantHolder.getTenantId();

        if (id == null) {
            CmsArticle cmsArticle = new CmsArticle();
            cmsArticle.setUserId(currentUserHolder.getUserId());
            cmsArticle.setCreateTime(new Date());
            cmsArticle.setTenantId(tenantId);
            cmsArticle.setContent("");
            cmsArticle.setType(CmsConstants.TYPE_VIDEO);
            cmsArticleManager.save(cmsArticle);
            model.addAttribute("model", cmsArticle);
        } else {
            CmsArticle cmsArticle = cmsArticleManager.get(id);
            model.addAttribute("model", cmsArticle);
        }

        model.addAttribute("cmsCatalogs",
                cmsCatalogManager.findBy("tenantId", tenantId));

        return "cms/cms-article-video";
    }


*//**
     * 下载.
     *//*
    @RequestMapping("cms-article-download")
    @ResponseBody
    public String download(@RequestParam("id") Long id) throws Exception {
        List<CmsAttachment> cmsAttachments = cmsAttachmentManager.findBy(
                "cmsArticle.id", id);

        Map<String, Object> data = new HashMap<String, Object>();
        List<Map<String, Object>> files = new ArrayList<Map<String, Object>>();
        data.put("files", files);

        for (CmsAttachment cmsAttachment : cmsAttachments) {
            Map<String, Object> map = new HashMap<String, Object>();
            files.add(map);
            map.put("name", cmsAttachment.getName());
            map.put("url", "../rs/cms/image?key=" + cmsAttachment.getPath());

            // map.put("thumbnailUrl", "./rs/cms/image?key=" + storeDto.getKey());
        }

        return jsonMapper.toJson(data);
    }




    */

    /**
     * 导出
     *//*
    @RequestMapping("cms-article-export")
    public void export(@ModelAttribute Page page,
            @RequestParam Map<String, Object> parameterMap,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        String tenantId = tenantHolder.getTenantId();
        List<PropertyFilter> propertyFilters = PropertyFilter
                .buildFromMap(parameterMap);
        propertyFilters.add(new PropertyFilter("EQS_tenantId", tenantId));
        page = cmsArticleManager.pagedQuery(page, propertyFilters);

        List<CmsArticle> cmsArticles = (List<CmsArticle>) page.getResult();

        TableModel tableModel = new TableModel();
        tableModel.setName("cmsArticle");
        tableModel.addHeaders("id", "name");
        tableModel.setData(cmsArticles);
        exportor.export(request, response, tableModel);
    }*/


    // 图片类型
    private static List<String> fileTypes = new ArrayList<String>();

    static {
        fileTypes.add(".jpg");
        fileTypes.add(".jpeg");
        fileTypes.add(".bmp");
        fileTypes.add(".gif");
        fileTypes.add(".png");
    }

    /**
     * ckeditor图片上传
     *
     * @param request
     * @param response
     * @Title imageUpload
     */
    @RequestMapping("imageUpload")
    @ResponseBody
    public Map<String, String> imageUpload(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String tenantId = tenantHolder.getTenantId();
        String fileName = "";
        String path = "";

        MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
        Iterator<String> iter = multiRequest.getFileNames();
        while (iter.hasNext()) {
            MultipartFile multipartFile = multiRequest.getFile(iter.next());
            fileName = multipartFile.getOriginalFilename();
            if (fileName == null || fileName.trim().equals("")) {
                continue;
            }
            // 针对IE环境下filename是整个文件路径的情况而做以下处理
            Integer index = fileName.lastIndexOf("\\");
            String newStr = "";
            if (index > -1) {
                newStr = fileName.substring(index + 1);
            } else {
                newStr = fileName;
            }
            if (!newStr.equals("")) {
                fileName = newStr;
            }
            path = fileUploadAPI.uploadFile(multipartFile, "cms");
        }
        Map<String, String> map = new HashedMap();
        map.put("state", "SUCCESS");
        map.put("url", "http://oa.dwcx-tech.com:1280/view/" + path);
        map.put("title", fileName);
        map.put("original", fileName);
        return map;
    }

    // ~ ======================================================================
    @Resource
    public void setCmsArticleManager(CmsArticleManager cmsArticleManager) {
        this.cmsArticleManager = cmsArticleManager;
    }

    @Resource
    public void setCmsCatalogManager(CmsCatalogManager cmsCatalogManager) {
        this.cmsCatalogManager = cmsCatalogManager;
    }

    @Resource
    public void setCmsAttachmentManager(
            CmsAttachmentManager cmsAttachmentManager) {
        this.cmsAttachmentManager = cmsAttachmentManager;
    }

    @Resource
    public void setCmsCommentManager(CmsCommentManager cmsCommentManager) {
        this.cmsCommentManager = cmsCommentManager;
    }

    @Resource
    public void setpartyEntityManager(PartyEntityManager partyEntityManager) {
        this.partyEntityManager = partyEntityManager;
    }

    @Resource
    public void setExportor(Exportor exportor) {
        this.exportor = exportor;
    }

    @Resource
    public void setPartyOrgConnector(PartyOrgConnector partyOrgConnector) {
        this.partyOrgConnector = partyOrgConnector;
    }

    @Resource
    public void setMessageHelper(MessageHelper messageHelper) {
        this.messageHelper = messageHelper;
    }

    @Resource
    public void setRenderService(RenderService renderService) {
        this.renderService = renderService;
    }

    @Resource
    public void setStoreConnector(StoreConnector storeConnector) {
        this.storeConnector = storeConnector;
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
    public void setFileUploadAPI(FileUploadAPI fileUploadAPI) {
        this.fileUploadAPI = fileUploadAPI;
    }

    @Resource
    public void setWebAPI(WebAPI webAPI) {
        this.webAPI = webAPI;
    }

    @Resource
    public void setCmsRangeManager(CmsRangeManager cmsRangeManager) {
        this.cmsRangeManager = cmsRangeManager;
    }

    @Resource
    public void setPartyConnector(PartyConnector partyConnector) {
        this.partyConnector = partyConnector;
    }

    @Resource
    public void setNotificationConnector(NotificationConnector notificationConnector) {
        this.notificationConnector = notificationConnector;
    }

    @Resource
    public void setPersonInfoManager(PersonInfoManager personInfoManager) {
        this.personInfoManager = personInfoManager;
    }
}
