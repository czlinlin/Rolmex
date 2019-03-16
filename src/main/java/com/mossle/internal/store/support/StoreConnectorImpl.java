package com.mossle.internal.store.support;

import java.util.List;

import javax.activation.DataSource;
import javax.annotation.Resource;

import com.mossle.api.store.StoreConnector;
import com.mossle.api.store.StoreDTO;
import com.mossle.common.utils.WebAPI;
import com.mossle.internal.store.persistence.domain.StoreInfo;
import com.mossle.internal.store.persistence.manager.StoreInfoManager;
import com.mossle.internal.store.service.StoreService;
import com.mossle.spi.store.InternalStoreConnector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StoreConnectorImpl implements StoreConnector {
    private Logger logger = LoggerFactory.getLogger(StoreConnectorImpl.class);
    private StoreService storeService;
    private StoreInfoManager storeInfoManager;
    private InternalStoreConnector internalStoreConnector;
    private WebAPI webAPI;
    
    public String getViewUrl() throws Exception {
    	return webAPI.getViewUrl();
    }
    
    public StoreDTO saveStore(String model, DataSource dataSource,
            String tenantId) throws Exception {
        return storeService.saveStore(model, dataSource, tenantId);
    }

    public StoreDTO saveStore(String model, String key, DataSource dataSource,
            String tenantId) throws Exception {
        return storeService.saveStore(model, key, dataSource, tenantId);
    }

    public StoreDTO saveStore(String model, DataSource dataSource,
            String tenantId, String pkId,String path) throws Exception  {
    	return storeService.saveStore(model, dataSource, tenantId, pkId, path);
    }
    
    public StoreDTO saveStore(String model, DataSource dataSource,
            String tenantId, String pkId,String path,String stoType) throws Exception  {
    	return storeService.saveStore(model, dataSource, tenantId, pkId, path,stoType);
    }
    
    public StoreDTO saveStore(String model, String fileName,
            String tenantId, String pkId,String path) throws Exception  {
    	return storeService.saveStore(model, fileName, tenantId, pkId, path);
    }
    
    public StoreDTO saveUniqueStore(String model, String fileName,
            String tenantId, String pkId,String path) throws Exception  {
    	return storeService.saveUniqueStore(model, fileName, tenantId, pkId, path,"0");
    }
    
    public StoreDTO saveStore(String model, String fileName,
            String tenantId, String pkId,String path,String stoType) throws Exception  {
    	return storeService.saveStore(model, fileName, tenantId, pkId, path,stoType);
    }
    
    public void removeStore(String path,String pkId) throws Exception {
    	storeService.removeStore(path, pkId);
    }
    
    public void removeALLStore(String model,String pkId) throws Exception{
    	storeService.removeALLStore(model,pkId);
    }
    
    public void removeStoreByType(String path,String pkId,String stoType) throws Exception {
    	storeService.removeStore(path, pkId,stoType);
    }
    
    public StoreDTO getStore(String model, String key, String tenantId) throws Exception{
        return getStoreByType(model,key,tenantId,"0");
    }
    
    public StoreDTO getStoreByType(String model, String key, String tenantId,String stoType)
            throws Exception {
        StoreDTO storeDto = internalStoreConnector.getStore(model, key,
                tenantId);

        if (storeDto == null) {
            return null;
        }

        StoreInfo storeInfo = storeInfoManager.findUnique(
                "from StoreInfo where path=? and tenantId=? and stoType=?", key, tenantId,stoType);

        if (storeInfo == null) {
            storeDto.setDisplayName(key);
        } else {
            storeDto.setDisplayName(storeInfo.getName());
        }

        return storeDto;
    }

    public List<StoreInfo> getStore(String model, String pkId) throws Exception {
    	return getStoreByType(model, pkId,"0");
    }
    
    @SuppressWarnings("unchecked")
	public List<StoreInfo> getStoreByType(String model, String pkId,String stoType) throws Exception {
    	String hql = "from StoreInfo where model=? and stoType=? and pkId=? ";
		List<StoreInfo> list = storeInfoManager.find(hql,model, stoType, pkId);
    	return list;
    }
    
    public List<StoreInfo> getStore(String pkId) throws Exception {
    	return getStoreByType(pkId,"0");
    }
    
    public List<StoreInfo> getStoreByType(String pkId,String stoType) throws Exception {
    	String hql = "from StoreInfo where pkId=? and stoType=?";
		List<StoreInfo> list = storeInfoManager.find(hql, pkId,stoType);
    	return list;
    }
    
    public void removeStore(String model, String key, String tenantId)
            throws Exception {
        internalStoreConnector.removeStore(model, key, tenantId);
    }

    @Resource
    public void setStoreService(StoreService storeService) {
        this.storeService = storeService;
    }

    @Resource
    public void setStoreInfoManager(StoreInfoManager storeInfoManager) {
        this.storeInfoManager = storeInfoManager;
    }

    @Resource
    public void setInternalStoreConnector(
            InternalStoreConnector internalStoreConnector) {
        this.internalStoreConnector = internalStoreConnector;
    }
    
    @Resource
    public void setWebAPI(WebAPI webAPI) {
        this.webAPI = webAPI;
    }
}
