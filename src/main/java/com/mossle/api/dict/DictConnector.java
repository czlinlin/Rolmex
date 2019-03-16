package com.mossle.api.dict;

import java.util.List;

import com.mossle.dict.persistence.domain.DictInfo;

public interface DictConnector {
    DictDTO findDictByName(String dictName, String typeName, String tenantId);

    DictDTO findDictByType(String typeName, String tenantId);
    
    List<DictInfo> findDictInfoListByType(String typeName, String tenantId);
    
    List<DictInfo> findDictInfoListByType(String typeName);
    
    String findDictNameByValue(String typeName,String value);
}
