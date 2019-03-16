package com.mossle.internal.store.service;

import java.util.Date;
import java.util.List;

import javax.activation.DataSource;
import javax.annotation.Resource;

import com.mossle.api.store.StoreDTO;
import com.mossle.common.utils.FileUtils;
import com.mossle.internal.store.persistence.domain.StoreInfo;
import com.mossle.internal.store.persistence.manager.StoreInfoManager;
import com.mossle.spi.store.InternalStoreConnector;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class StoreService {
    private StoreInfoManager storeInfoManager;
    private InternalStoreConnector internalStoreConnector;

    public StoreDTO saveStore(String model, String key, DataSource dataSource,
            String tenantId) throws Exception {
        StoreDTO storeDto = this.internalStoreConnector.saveStore(model, key,
                dataSource, tenantId);

        StoreInfo storeInfo = new StoreInfo();
        storeInfo.setName(dataSource.getName());
        storeInfo.setModel(model);
        storeInfo.setPath(storeDto.getKey());
        storeInfo.setCreateTime(new Date());
        storeInfo.setTenantId(tenantId);
        storeInfoManager.save(storeInfo);

        return storeDto;
    }

    public StoreDTO saveStore(String model, DataSource dataSource,
            String tenantId) throws Exception {
        StoreDTO storeDto = this.internalStoreConnector.saveStore(model,
                dataSource, tenantId);

        StoreInfo storeInfo = new StoreInfo();
        storeInfo.setName(dataSource.getName());
        storeInfo.setModel(model);
        storeInfo.setPath(storeDto.getKey());
        storeInfo.setCreateTime(new Date());
        storeInfo.setTenantId(tenantId);
        storeInfoManager.save(storeInfo);

        return storeDto;
    }

    public StoreDTO saveStore(String model, DataSource dataSource,
            String tenantId, String pkId,String path) throws Exception {
    	
        StoreDTO storeDto = null;

        StoreInfo storeInfo = new StoreInfo();
        storeInfo.setName(dataSource.getName());
        storeInfo.setModel(model);
        storeInfo.setPath(path);
        storeInfo.setCreateTime(new Date());
        storeInfo.setTenantId(tenantId);
        storeInfo.setPkId(pkId);
        
        String suffix = FileUtils.getSuffix(dataSource.getName());
        storeInfo.setType(suffix);
        
        storeInfoManager.save(storeInfo);

        return storeDto;
    }
    
    public StoreDTO saveStore(String model, DataSource dataSource,
            String tenantId, String pkId,String path,String stoType) throws Exception {
    	
        StoreDTO storeDto = null;

        StoreInfo storeInfo = new StoreInfo();
        storeInfo.setName(dataSource.getName());
        storeInfo.setModel(model);
        storeInfo.setPath(path);
        storeInfo.setCreateTime(new Date());
        storeInfo.setTenantId(tenantId);
        storeInfo.setPkId(pkId);
        storeInfo.setStoType(stoType);
        
        String suffix = FileUtils.getSuffix(dataSource.getName());
        storeInfo.setType(suffix);
        
        storeInfoManager.save(storeInfo);

        return storeDto;
    }
    
    public StoreDTO saveStore(String model, String fileName,
            String tenantId, String pkId,String path,String stoType) throws Exception {
    	
        StoreDTO storeDto = null;
        StoreInfo storeInfo = new StoreInfo();
        storeInfo.setName(fileName);
        storeInfo.setModel(model);
        storeInfo.setPath(path);
        storeInfo.setCreateTime(new Date());
        storeInfo.setTenantId(tenantId);
        storeInfo.setPkId(pkId);
        if(stoType==null||stoType.equals(""))
        	stoType="0";
        storeInfo.setStoType(stoType);
        
        String suffix = FileUtils.getSuffix(fileName);
        storeInfo.setType(suffix);
        
        storeInfoManager.save(storeInfo);

        return storeDto;
    }
    
    /**
     * 保存文档信息（唯一性）
     * @author  lilei
     * @date 2017-10-11
     * **/
    public StoreDTO saveUniqueStore(String model, String fileName,
            String tenantId, String pkId,String path,String stoType) throws Exception {
    	
        StoreDTO storeDto = null;
        //StoreInfo storeInfo = new StoreInfo();
        
        StoreInfo storeInfo = storeInfoManager.findUnique(
                "from StoreInfo where model=? and pkId=?",model , pkId);
        if(storeInfo==null)
        {
        	storeInfo=new StoreInfo();
        	storeInfo.setPkId(pkId);
        	storeInfo.setCreateTime(new Date());
        	storeInfo.setModel(model);
        	storeInfo.setTenantId(tenantId);
        }
        if(stoType==null||stoType.equals("")){
        	stoType="0";
        }
        
        storeInfo.setStoType(stoType);
        storeInfo.setName(fileName);
        storeInfo.setPath(path);
        
        String suffix = FileUtils.getSuffix(fileName);
        storeInfo.setType(suffix);
        
        storeInfoManager.save(storeInfo);

        return storeDto;
    }
    
    public StoreDTO saveStore(String model, String fileName,
            String tenantId, String pkId,String path) throws Exception {
    	
        StoreDTO storeDto = null;

        StoreInfo storeInfo = new StoreInfo();
        storeInfo.setName(fileName);
        storeInfo.setModel(model);
        storeInfo.setPath(path);
        storeInfo.setCreateTime(new Date());
        storeInfo.setTenantId(tenantId);
        storeInfo.setPkId(pkId);
        
        String suffix = FileUtils.getSuffix(fileName);
        storeInfo.setType(suffix);
        
        storeInfoManager.save(storeInfo);

        return storeDto;
    }
    
    /**
     * 删除图片信息
     * **/
    public void removeStore(String path,String pkId) throws Exception {
    	StoreInfo storeInfo = storeInfoManager.findUnique(
        "from StoreInfo where path=? and pkId=?",path , pkId);
        
    	if(storeInfo!=null)
    		storeInfoManager.remove(storeInfo);
    	return;
    }
    
    /**
     * 根据MODEL和PKID删除图片信息
     * lilei at 2018.01.04
     * */
    public void removeALLStore(String model,String pkId) throws Exception {
    	List<StoreInfo> storeInfoList = storeInfoManager.find(
        "from StoreInfo where path=? and pkId=?",model , pkId);
        
    	if(storeInfoList!=null&storeInfoList.size()>0){
    		for(StoreInfo storeInfo:storeInfoList){
    			storeInfoManager.remove(storeInfo);
    		}
    	}
    	return;
    }
    
    /**
     * 删除图片信息（根据类型）
     * **/
    public void removeStore(String path,String pkId,String stoType) throws Exception {
    	StoreInfo storeInfo = storeInfoManager.findUnique(
        "from StoreInfo where path=? and pkId=? and stoType=?",path , pkId,stoType);
        
    	if(storeInfo!=null)
    		storeInfoManager.remove(storeInfo);
    	return;
    }
    
    @Resource
    public void setInternalStoreConnector(
            InternalStoreConnector internalStoreConnector) {
        this.internalStoreConnector = internalStoreConnector;
    }

    @Resource
    public void setStoreInfoManager(StoreInfoManager storeInfoManager) {
        this.storeInfoManager = storeInfoManager;
    }
}
