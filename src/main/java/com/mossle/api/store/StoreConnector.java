package com.mossle.api.store;

import java.util.List;

import javax.activation.DataSource;

import com.mossle.internal.store.persistence.domain.StoreInfo;

public interface StoreConnector {
	StoreDTO saveStore(String model, DataSource dataSource, String tenantId) throws Exception;

	StoreDTO saveStore(String model, String key, DataSource dataSource, String tenantId) throws Exception;

	StoreDTO getStore(String model, String key, String tenantId) throws Exception;

	List<StoreInfo> getStore(String pkId) throws Exception;

	List<StoreInfo> getStoreByType(String pkId, String stoType) throws Exception;

	List<StoreInfo> getStore(String model, String pkId) throws Exception;

	List<StoreInfo> getStoreByType(String model, String pkId, String stoType) throws Exception;

	StoreDTO saveStore(String model, DataSource dataSource, String tenantId, String pkId, String path) throws Exception;

	StoreDTO saveStore(String model, DataSource dataSource, String tenantId, String pkId, String path, String stoType)
			throws Exception;

	StoreDTO saveStore(String model, String fileName, String tenantId, String pkId, String path) throws Exception;

	StoreDTO saveUniqueStore(String model, String fileName, String tenantId, String pkId, String path) throws Exception;

	StoreDTO saveStore(String model, String fileName, String tenantId, String pkId, String path, String stoType)
			throws Exception;

	void removeStore(String path, String pkId) throws Exception;
	
	void removeALLStore(String model,String pkId) throws Exception;

	void removeStoreByType(String path, String pkId, String stoType) throws Exception;
	
	String getViewUrl() throws Exception;
}
