package com.mossle.spi.store;

import java.util.List;

import javax.activation.DataSource;

import com.mossle.api.store.StoreDTO;
import com.mossle.internal.store.persistence.domain.StoreInfo;

public interface InternalStoreConnector {
    StoreDTO saveStore(String model, DataSource dataSource, String tenantId)
            throws Exception;

    StoreDTO saveStore(String model, String key, DataSource dataSource, String tenantId) throws Exception;

    StoreDTO getStore(String model, String key, String tenantId) throws Exception;
    
    void removeStore(String model, String key, String tenantId) throws Exception;

    void mkdir(String path);
}
