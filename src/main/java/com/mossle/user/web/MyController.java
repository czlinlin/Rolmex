package com.mossle.user.web;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
//import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.mossle.api.avatar.AvatarConnector;
import com.mossle.api.avatar.AvatarDTO;
import com.mossle.api.dict.DictConnector;
import com.mossle.api.keyvalue.KeyValueConnector;
import com.mossle.api.store.StoreConnector;
import com.mossle.api.store.StoreDTO;
import com.mossle.api.tenant.TenantHolder;
import com.mossle.api.user.UserConnector;
import com.mossle.common.utils.StringUtils;
import com.mossle.core.annotation.Log;
import com.mossle.core.auth.CurrentUserHolder;
import com.mossle.core.mapper.BeanMapper;
import com.mossle.core.page.Page;
import com.mossle.core.spring.MessageHelper;
import com.mossle.core.store.InputStreamDataSource;
import com.mossle.core.store.MultipartFileDataSource;
import com.mossle.core.util.IoUtils;
import com.mossle.dict.persistence.domain.DictInfo;
import com.mossle.operation.persistence.domain.CustomPresetApprover;
import com.mossle.operation.persistence.manager.CustomPresetApproverManager;
import com.mossle.party.persistence.domain.PartyEntity;
import com.mossle.party.persistence.domain.PartyStruct;
import com.mossle.party.persistence.manager.PartyEntityManager;
import com.mossle.party.persistence.manager.PartyStructManager;
import com.mossle.user.ImageUtils;
import com.mossle.user.persistence.domain.AccountCredential;
import com.mossle.user.persistence.domain.AccountDevice;
import com.mossle.user.persistence.domain.AccountInfo;
import com.mossle.user.persistence.domain.PersonAttendanceMachine;
import com.mossle.user.persistence.domain.PersonInfo;
import com.mossle.user.persistence.manager.AccountCredentialManager;
import com.mossle.user.persistence.manager.AccountDeviceManager;
import com.mossle.user.persistence.manager.AccountInfoManager;
import com.mossle.user.persistence.manager.PersonAttendanceMachineManager;
import com.mossle.user.persistence.manager.PersonInfoManager;
import com.mossle.user.service.ChangePasswordService;
import com.mossle.user.service.PersonInfoService;
import com.mossle.user.support.ChangePasswordResult;
import com.mysql.fabric.xmlrpc.base.Data;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 管理个人信息.
 */
@Controller
@RequestMapping("user")
@Component
@Path("user")
public class MyController {
    private static Logger logger = LoggerFactory.getLogger(MyController.class);
    private AccountInfoManager accountInfoManager;
    private PersonInfoManager personInfoManager;
    private AccountDeviceManager accountDeviceManager;
    private MessageHelper messageHelper;
    private BeanMapper beanMapper = new BeanMapper();
    private CurrentUserHolder currentUserHolder;
    private ChangePasswordService changePasswordService;
    private StoreConnector storeConnector;
    private TenantHolder tenantHolder;
    private AvatarConnector avatarConnector;
    private DictConnector dictConnector;
    private PersonInfoService personInfoService;
    private PartyEntityManager partyEntityManager;
    private AccountCredentialManager accountCredentialManager;
    private CustomPresetApproverManager customPresetApproverManager;
    private UserConnector userConnector;
    private JdbcTemplate jdbcTemplate;
    private PersonAttendanceMachine personAttendanceMachine;
    private PersonAttendanceMachineManager personAttendanceMachineManager;
    private KeyValueConnector keyValueConnector;
    @Autowired
    private PartyStructManager partyStructManager;
    
    /**
     * 显示个人信息.
     */
    @RequestMapping("my-info-input")
    @Log(desc = "个人信息", action = "input", operationDesc = "个人信息-个人信息显示")
    public String infoInput(Model model) {
    	
    	String isResetAnotherName = "0";//是否允许修改别名 0表示可以改  1表示不可以
    	
        Long accountId = Long.parseLong(currentUserHolder.getUserId());
        AccountInfo accountInfo = accountInfoManager.get(accountId);
        
      //控制是否开启别名 :1开启 0关闭
        List<DictInfo> dictInfo_otherName = dictConnector.findDictInfoListByType("isOpenOtherName");
        
      //是否允许修改别名
        String sqlString = "SELECT * FROM person_machine where person_id = "+accountId;
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sqlString);
        if(list!=null&&list.size()>0){
        	if(list.get(0).get("is_modify")!=null)
        		isResetAnotherName = list.get(0).get("is_modify").toString();
        }
        
        PersonInfo personInfo = personInfoManager.get(accountId);
        model.addAttribute("accountInfo", accountInfo);
        model.addAttribute("personInfo", personInfo);
       model.addAttribute("dictInfo_otherName", dictInfo_otherName);
       AccountCredential accountCredential=accountCredentialManager.findUniqueBy("accountInfo.id", accountId);
	   model.addAttribute("accountCredential", accountCredential);
	   model.addAttribute("isResetAnotherName", isResetAnotherName);
	   
	   PartyEntity partyEntity=partyEntityManager.findUniqueBy("id",accountId);
	   String hql="from PartyStruct where partyStructType.id=4 and childEntity=?";
	   List<PartyStruct> partyStructList=partyStructManager.find(hql,partyEntity);
	   	List<Map<String,Object>> postionMapList=new ArrayList<Map<String,Object>>();
	   	if(partyStructList!=null&&partyStructList.size()>0)
	   	{
	   		int i=0;
	   		for(PartyStruct partyStruct:partyStructList){
	   			//部门
	   			String deparmentName= "";
	   			//公司
	   			String companyName="";
	   			//岗位
	   			String positionName=partyStruct.getParentEntity().getName();
	   			
	   			//部门
	   			hql="from PartyStruct where partyStructType.id=1 and childEntity=?";
	   			PartyStruct deparmentPartyStruct=partyStructManager.findUnique(hql, partyStruct.getParentEntity());
	   			if(null != deparmentPartyStruct){
	   				deparmentName=deparmentPartyStruct.getParentEntity().getName();
	   				//公司
		   			hql="from PartyStruct where partyStructType.id=1 and childEntity=?";
		   			PartyStruct companyPartyStruct=partyStructManager.findUnique(hql, deparmentPartyStruct.getParentEntity());
		   			companyName=companyPartyStruct.getParentEntity().getName();
	   			}
	   			Map<String,Object> map=new HashMap<String, Object>();
	   			if(StringUtils.isNotBlank(deparmentName)){
	   				map.put("position", " "+(++i)+"."+companyName+"-"+deparmentName+"-"+positionName);
	   			}else{
	   				map.put("position", " "+(++i)+"."+positionName);
	   			}
	   			map.put("id", partyStruct.getParentEntity().getId());
	   			postionMapList.add(map);
	   		}
	   	}
	   	model.addAttribute("positions", postionMapList);
	   	
        return "user/my-info-input";
    }

    /**
     * 保存个人信息.
     */
    @RequestMapping("my-info-save")
    @Log(desc = "个人信息", action = "save", operationDesc = "个人信息-保存")
    public String infoSave(@ModelAttribute PersonInfo personInfo,
    		@RequestParam("oldSysUserName") String oldSysUserName,
            RedirectAttributes redirectAttributes) throws Exception {
    	
    	String tenantId = tenantHolder.getTenantId();
    	
        Long accountId = Long.parseLong(currentUserHolder.getUserId());
        
        PersonInfo dest = personInfoManager.get(accountId);
        
        if (dest != null) {
            beanMapper.copy(personInfo, dest);
        } else {
            dest = new PersonInfo();
            beanMapper.copy(personInfo, dest);
        }

       

        AccountCredential accountCredential=accountCredentialManager.findUniqueBy("accountInfo.id", accountId);
        accountCredential.setOldSysUserName(oldSysUserName);
        accountCredentialManager.save(accountCredential);
       //控制是否开启别名 :1开启 0关闭
        List<DictInfo> dictInfo_otherName = dictConnector.findDictInfoListByType("isOpenOtherName");
        if(dictInfo_otherName.get(0).getValue().equals("1")){
        	boolean isEdit=false;        	
            
            //更新别名修改次数
            String sqlString = "SELECT * FROM person_machine where person_id = "+accountId;
            List<Map<String, Object>> list = jdbcTemplate.queryForList(sqlString);
            if(list!=null&&list.size()>0){
            	//List<PersonAttendanceMachine> personAttendanceMachine = personAttendanceMachineManager.findBy("personId", accountId);
            	//若修改标识是0，表示可以修改
            	if(list.get(0).get("is_modify").equals("0")){
            		isEdit=true;
	            	//personAttendanceMachine.setModify_date(new Date());
	            	//别名的修改次数 若是空就置为1，非空就加1
	            	Object modifyNum = list.get(0).get("modify_num");
	            	if(modifyNum!=null&& !modifyNum.toString().equals("null")){
	            		modifyNum =String.valueOf(( Integer.parseInt(modifyNum.toString())+1));
	            	}else {
	            		modifyNum = "1";
					}
	            	//personAttendanceMachine.setModify_num(modifyNum);
	            	Date currentTime = new Date();
	            	SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	            	String dateString = formatter.format(currentTime);
	            	
	            	String sqlRecordUpdate = "UPDATE  person_machine SET is_modify = '1'  , modify_date='"+dateString+"' , modify_num='"+modifyNum+"' WHERE  person_id = '"+accountId+"'";
            	    keyValueConnector.updateBySql(sqlRecordUpdate);
            		
	            	//personAttendanceMachineManager.save(personAttendanceMachine);
            	}
            }else {//该用户不存在，新创建一条数据
            	isEdit=true;
            	PersonAttendanceMachine personAttendanceMachine = new PersonAttendanceMachine();
            	personAttendanceMachine.setPersonId(accountId);
            	personAttendanceMachine.setIs_modify("1");
                personAttendanceMachine.setModify_date(new Date());
                personAttendanceMachine.setModify_num("1");
                personAttendanceMachineManager.save(personAttendanceMachine);
			}
            
            AccountInfo accountInfo  = accountInfoManager.get(accountId);
            if(isEdit){
            	accountInfo.setDisplayName(personInfo.getFullName());
            	accountInfoManager.save(accountInfo);
            	
            	// 处理party_entity表
            	
            	Long id = dest.getId();
            	PartyEntity partyEntity  = partyEntityManager.get(id);
            	
            	partyEntity.setName(dest.getFullName());
                partyEntityManager.save(partyEntity);
            }
            else{
            	dest.setFullName(accountInfo.getDisplayName());
            }
            
            personInfoManager.save(dest);
        }
        messageHelper.addFlashMessage(redirectAttributes, "core.success.save",
                "保存成功");

        return "redirect:/user/my-info-input.do";
    }

    /**
     * 准备修改密码.
     */
    @RequestMapping("my-change-password-input")
    public String changepasswordInput() {
        return "user/my-change-password-input";
    }

    /**
     * 准备修改私钥.
     */
    @RequestMapping("my-change-operationpassword-input")
    public String changeoperationpasswordInput() {
        return "user/my-change-operationpassword-input";
    }
    
    /**
     * 修改密码.
     */
    @RequestMapping("my-change-password-save")
    @Log(desc = "个人信息", action = "修改", operationDesc = "个人信息-修改-修改密码")
    public String changePasswordSave(
            @RequestParam("oldPassword") String oldPassword,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            RedirectAttributes redirectAttributes) {
        Long accountId = Long.parseLong(currentUserHolder.getUserId());
        ChangePasswordResult changePasswordResult = changePasswordService
                .changePassword(accountId, oldPassword, newPassword,
                        confirmPassword);

        //if (changePasswordResult.isSuccess()) {
            messageHelper.addFlashMessage(redirectAttributes,
                    changePasswordResult.getCode(),
                    changePasswordResult.getMessage());

            return "redirect:/user/my-change-password-input.do";
        /*} else {
            messageHelper.addFlashMessage(redirectAttributes,
                    changePasswordResult.getCode(),
                    changePasswordResult.getMessage());

            return "redirect:/user/my-change-password-input.do";
        }*/
    }
    
    /**
     * 修改私钥
     */
    @RequestMapping("my-change-operationpassword-save")
    @Log(desc = "个人信息", action = "save", operationDesc = "个人信息-修改私钥")
    public String changeOperationPasswordSave(
            @RequestParam("oldPassword") String oldPassword,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            RedirectAttributes redirectAttributes) {
        Long accountId = Long.parseLong(currentUserHolder.getUserId());
        ChangePasswordResult changePasswordResult = changePasswordService
                .changeOperationPassword(accountId, oldPassword, newPassword,
                        confirmPassword);

        //if (changePasswordResult.isSuccess()) {
            messageHelper.addFlashMessage(redirectAttributes,
                    changePasswordResult.getCode(),
                    changePasswordResult.getMessage());

            return "redirect:/user/my-change-operationpassword-input.do";
        /*} else {
            messageHelper.addFlashMessage(redirectAttributes,
                    changePasswordResult.getCode(),
                    changePasswordResult.getMessage());

            return "redirect:/user/my-change-operationpassword-input.do";
        }*/
    }
    
    /**
     * 显示头像.
     */
    @RequestMapping("my-avatar-input")
    @Log(desc = "个人信息", action = "input", operationDesc = "个人信息-显示头像")
    public String avatarInput(Model model) {
        String userId = currentUserHolder.getUserId();
        Long accountId = Long.parseLong(userId);
        AccountInfo accountInfo = accountInfoManager.get(accountId);

        AvatarDTO avatarDto = avatarConnector.findAvatar(userId);

        model.addAttribute("accountInfo", accountInfo);
        model.addAttribute("avatarDto", avatarDto);

        return "user/my-avatar-input";
    }

    /**
     * 上传头像.
     */
    @RequestMapping("my-avatar-upload")
    @ResponseBody
    @Log(desc = "个人信息", action = "upload", operationDesc = "个人信息-上传头像")
    public String avatarUpload(@RequestParam("avatar") MultipartFile avatar)
            throws Exception {
        String tenantId = tenantHolder.getTenantId();
        StoreDTO storeDto = storeConnector.saveStore("avatar",
                new MultipartFileDataSource(avatar), tenantId);

        String userId = currentUserHolder.getUserId();
        avatarConnector.saveAvatar(userId, storeDto.getKey());

        return "{\"success\":true,\"id\":\"" + userId + "\"}";
    }

    /**
     * 显示头像.
     */
    @RequestMapping("my-avatar-view")
    @ResponseBody
    public void avatarView(OutputStream os) throws Exception {
        String tenantId = tenantHolder.getTenantId();
        String userId = currentUserHolder.getUserId();
        AvatarDTO avatarDto = avatarConnector.findAvatar(userId);

        if (avatarDto == null) {
            return;
        }

        StoreDTO storeDto = storeConnector.getStore("avatar",
                avatarDto.getCode(), tenantId);

        IoUtils.copyStream(storeDto.getDataSource().getInputStream(), os);
    }

    /**
     * 剪切头像.
     */
    @RequestMapping("my-avatar-crop")
    public String avatarCrop(Model model) throws Exception {
        String tenantId = tenantHolder.getTenantId();
        String userId = currentUserHolder.getUserId();
        AvatarDTO avatarDto = avatarConnector.findAvatar(userId);

        Long accountId = Long.parseLong(userId);
        AccountInfo accountInfo = accountInfoManager.get(accountId);
        model.addAttribute("accountInfo", accountInfo);
        model.addAttribute("avatarDto", avatarDto);

        if (avatarDto == null) {
            return "redirect:/user/my-avatar-input.do";
        }

        StoreDTO storeDto = storeConnector.getStore("avatar",
                avatarDto.getCode(), tenantId);
        BufferedImage bufferedImage = ImageIO.read(storeDto.getDataSource()
                .getInputStream());
        int height = bufferedImage.getHeight();
        int width = bufferedImage.getWidth();

        int defaultSize = Math.min(512, Math.min(height, width));

        if (height > width) {
            int h = defaultSize;
            int w = (defaultSize * width) / height;
            int min = w;
            model.addAttribute("h", h);
            model.addAttribute("w", w);
            model.addAttribute("min", min);
        } else {
            int w = defaultSize;
            int h = (defaultSize * height) / width;
            int min = h;
            model.addAttribute("h", h);
            model.addAttribute("w", w);
            model.addAttribute("min", min);
        }

        return "user/my-avatar-crop";
    }

    /**
     * 保存头像.
     */
    @RequestMapping("my-avatar-save")
    @Log(desc = "个人信息", action = "save", operationDesc = "个人信息-保存头像")
    public String avatarSave(@RequestParam("x1") int x1,
            @RequestParam("x2") int x2, @RequestParam("y1") int y1,
            @RequestParam("y2") int y2, @RequestParam("w") int w, Model model)
            throws Exception {
        String tenantId = tenantHolder.getTenantId();
        String userId = currentUserHolder.getUserId();
        AvatarDTO avatarDto = avatarConnector.findAvatar(userId);
        Long accountId = Long.parseLong(userId);
        AccountInfo accountInfo = accountInfoManager.get(accountId);
        String hql = "from AccountAvatar where accountInfo=? and type='default'";
        model.addAttribute("accountInfo", accountInfo);
        model.addAttribute("avatarDto", avatarDto);

        if (avatarDto != null) {
            StoreDTO storeDto = storeConnector.getStore("avatar",
                    avatarDto.getCode(), tenantId);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageUtils.zoomImage(storeDto.getDataSource().getInputStream(),
                    baos, x1, y1, x2, y2);

            storeDto = storeConnector.saveStore("avatar",
                    new InputStreamDataSource(w + ".png",
                            new ByteArrayInputStream(baos.toByteArray())),
                    tenantId);
            avatarConnector.saveAvatar(userId, storeDto.getKey());
        }

        return "user/my-avatar-save";
    }

    /**
     * 设备列表.
     */
    @RequestMapping("my-device-list")
    public String myDeviceList(@ModelAttribute Page page, Model model) {
        Long accountId = Long.parseLong(currentUserHolder.getUserId());
        String hql = "from AccountDevice where accountInfo.id=?";
        page = accountDeviceManager.pagedQuery(hql, page.getPageNo(),
                page.getPageSize(), accountId);
        model.addAttribute("page", page);

        return "user/my-device-list";
    }

    @RequestMapping("my-device-active")
    public String active(@RequestParam("id") Long id,
            RedirectAttributes redirectAttributes) {
        AccountDevice accountDevice = accountDeviceManager.get(id);
        accountDevice.setStatus("active");
        accountDeviceManager.save(accountDevice);
        messageHelper.addFlashMessage(redirectAttributes,
                "core.success.update", "操作成功");

        return "redirect:/user/my-device-list.do";
    }

    @RequestMapping("my-device-disable")
    public String disable(@RequestParam("id") Long id,
            RedirectAttributes redirectAttributes) {
        AccountDevice accountDevice = accountDeviceManager.get(id);
        accountDevice.setStatus("disabled");
        accountDeviceManager.save(accountDevice);
        messageHelper.addFlashMessage(redirectAttributes,
                "core.success.update", "操作成功");

        return "redirect:/user/my-device-list.do";
    }

    @RequestMapping("my-device-remove")
    @Log(desc = "个人信息", action = "remove", operationDesc = "个人信息-删除设备")
    public String remove(@RequestParam("id") Long id,
            RedirectAttributes redirectAttributes) {
        accountDeviceManager.removeById(id);
        messageHelper.addFlashMessage(redirectAttributes,
                "core.success.update", "操作成功");

        return "redirect:/user/my-device-list.do";
    }

    // ~ ======================================================================
    
    //region个人自定义流程设置模块
    @RequestMapping("my-custom-info-setting-list")
    @Log(desc = "个人流程设置", action = "search", operationDesc = "个人流程设置")
    @SuppressWarnings("unchecked")
    public String customSettinglist(Model model) {
    	
        String userId=currentUserHolder.getUserId();
        String hql="from CustomPresetApprover where delStatus<>'2' and userId="+Long.valueOf(userId)+" order by orderNum desc";
		List<CustomPresetApprover> presetApproverList=customPresetApproverManager.find(hql);
        model.addAttribute("presetApproverList", presetApproverList);
        return "user/my-custom-info-setting-list";
    }
    
    @RequestMapping("my-custom-info-setting-input")
    @Log(desc = "个人流程设置-预设信息查询", action = "search", operationDesc = "个人流程设置-预设信息查询")
    public String customSettingInput(@RequestParam(value="id",required=false) Long id,Model model) {
    	if(id!=null){
    		if(id>0L){
    			CustomPresetApprover customPresetApprover=customPresetApproverManager.findUniqueBy("id",id);
    			if(customPresetApprover!=null){
    				model.addAttribute("customPresetApprover", customPresetApprover);
    				model.addAttribute("approverNames",userConnector.findNamesByIds(customPresetApprover.getApproverIds()));
    			}
    		}
    	}
        return "user/my-custom-info-setting-input";
    }
    
    @RequestMapping("my-custom-info-setting-save")
    @Log(desc = "个人流程设置-新增/修改", action = "update", operationDesc = "个人流程设置--新增/修改")
    public String customSettingSave(CustomPresetApprover customPresetApprover,
            RedirectAttributes redirectAttributes) {
    	if(customPresetApprover!=null){
    		if(customPresetApprover.getId()==null||customPresetApprover.getId()<1L){
    			customPresetApprover.setUserId(Long.parseLong(currentUserHolder.getUserId()));
    			//customPresetApprover.setCreateDate(new java.util.Date());
    		}
    		if(customPresetApprover.getCreateDate()==null)
    			customPresetApprover.setCreateDate(new java.util.Date());
    		customPresetApproverManager.save(customPresetApprover);
    	}
    	messageHelper.addFlashMessage(redirectAttributes,
                "core.success.update", "保存成功");
        return "redirect:/user/my-custom-info-setting-list.do";
    }
    
    
   
    
    //endregion
    @Resource
    public void setAccountInfoManager(AccountInfoManager accountInfoManager) {
        this.accountInfoManager = accountInfoManager;
    }

    @Resource
    public void setPersonInfoManager(PersonInfoManager personInfoManager) {
        this.personInfoManager = personInfoManager;
    }

    @Resource
    public void setAccountDeviceManager(
            AccountDeviceManager accountDeviceManager) {
        this.accountDeviceManager = accountDeviceManager;
    }

    @Resource
    public void setMessageHelper(MessageHelper messageHelper) {
        this.messageHelper = messageHelper;
    }

    @Resource
    public void setCurrentUserHolder(CurrentUserHolder currentUserHolder) {
        this.currentUserHolder = currentUserHolder;
    }

    @Resource
    public void setChangePasswordService(
            ChangePasswordService changePasswordService) {
        this.changePasswordService = changePasswordService;
    }

    @Resource
    public void setStoreConnector(StoreConnector storeConnector) {
        this.storeConnector = storeConnector;
    }

    @Resource
    public void setTenantHolder(TenantHolder tenantHolder) {
        this.tenantHolder = tenantHolder;
    }

    @Resource
    public void setAvatarConnector(AvatarConnector avatarConnector) {
        this.avatarConnector = avatarConnector;
    }
    @Resource
    public void setDictConnector(DictConnector dictConnector) {
        this.dictConnector = dictConnector;
    }
    @Resource
    public void setPersonInfoService(PersonInfoService personInfoService) {
        this.personInfoService = personInfoService;
    }
    
    @Resource
    public void setPartyEntityManager(PartyEntityManager partyEntityManager) {
        this.partyEntityManager = partyEntityManager;
    }

    @Resource
    public void setAccountCredentialManager(AccountCredentialManager accountCredentialManager) {
        this.accountCredentialManager = accountCredentialManager;
    }
    
    @Resource
    public void setCustomPresetApproverManager(CustomPresetApproverManager customPresetApproverManager) {
        this.customPresetApproverManager = customPresetApproverManager;
    }
    
    @Resource
    public void setUserConnector(UserConnector userConnector) {
        this.userConnector = userConnector;
    }
    
    @Resource
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Resource
	public void setPersonAttendanceMachineManager(
			PersonAttendanceMachineManager personAttendanceMachineManager) {
		this.personAttendanceMachineManager = personAttendanceMachineManager;
	}
    
    @Resource
    public void setKeyValueConnector(KeyValueConnector keyValueConnector){
    	this.keyValueConnector=keyValueConnector;
    }
    
    
}
