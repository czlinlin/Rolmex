package com.mossle.api.keyvalue;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.mossle.api.store.StoreConnector;
import com.mossle.core.MultipartHandler;
import com.mossle.core.store.MultipartFileDataSource;
import com.mossle.party.persistence.domain.PartyEntity;
import com.mossle.party.persistence.manager.PartyEntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

/**
 * 构建Record.
 */
public class RecordBuilder {
    private static Logger logger = LoggerFactory.getLogger(RecordBuilder.class);

    
    /**
     * 把status和parameters更新到record里.
     */
    public Record build(Record record, int status, FormParameter formParameter) {
        record.setStatus(status);

        for (Map.Entry<String, List<String>> entry : formParameter
                .getMultiValueMap().entrySet()) {
            String key = entry.getKey();
            List<String> value = entry.getValue();

            if ((value == null) || (value.isEmpty())) {
                continue;
            }
              
            // TODO 外部表单不需要，如果是内部表单此处需要修改    zyl 2017-11-16
            
            if ("activityId".equals(key) || "copyUserValue".equals(key)) {
	            Prop prop = new Prop();
	            prop.setCode(key);
	            prop.setType(0);
	            prop.setValue(this.getValue(value));
	            record.getProps().put(prop.getCode(), prop);
            }
            
            // 主题
            if ("theme".equals(key)) {
            	record.setTheme(this.getValue(value));
            }
            
            // 受理单号
            if ("applyCode".equals(key)) {
            	record.setApplyCode(this.getValue(value));
            }
            
            // 经销商编号
            if ("ucode".equals(key)) {
            	record.setUcode(this.getValue(value));
            }
            
            // 申请业务类型Id
            if ("busType".equals(key)||"businessTypeId".equals(key)) {
            	record.setBusinessTypeId(this.getValue(value));
            }
            
            // 申请业务类型
            if ("businessType".equals(key)) {
            	record.setBusinessTypeName(this.getValue(value));
            }
            
            // 业务细分
            if ("busDetails".equals(key)||"businessDetailId".equals(key)) {
            	record.setBusinessDetailId(this.getValue(value));
            }
            // 业务细分
            if ("businessDetail".equals(key)) {
            	record.setBusinessDetailName(this.getValue(value));
            }
            
            // 提交次数
            if ("submitTimes".equals(key)) {
            	record.setSubmitTimes(Integer.parseInt(this.getValue(value)));
            }
            
            // 所属大区
            if ("area".equals(key)) {
            	record.setAreaName(this.getValue(value));
            }
            
            // 所属Id
            if ("areaId".equals(key)) {
            	record.setAreaId(this.getValue(value));
            }
            // 所属体系
            if ("system".equals(key)) {
            	record.setSystemId(this.getValue(value));
            }
            
            // 所属体系
            if ("systemName".equals(key)) {
            	record.setSystemName(this.getValue(value));
            }
            
            // 表单详情
            if ("url".equals(key)) {
            	record.setUrl(this.getValue(value));
            }
            
            if ("applyContent".equals(key)) {
            	record.setApplyContent(this.getValue(value));
            }
            
            // 发起岗位ID
            if ("iptStartPositionId".equals(key)) {
            	record.setStartPositionId(this.getValue(value));
            }
            
            
        }

        return record;
    }

    /**
     * 创建一个新record
     */
    public Record build(String category, int status,
            FormParameter formParameter, String userId, String tenantId) {
        Record record = new Record();
        record.setCategory(category);
        record.setUserId(userId);
        record.setCreateTime(new Date());
        record.setTenantId(tenantId);
 
        return build(record, status, formParameter);
    }

    /**
     * 更新record的ref属性.
     */
    public Record build(Record record, int status, String ref) {
        if (record == null) {
            record = new Record();
        }

        record.setRef(ref);
        record.setStatus(status);

        return record;
    }

    public Record build(Record record, MultipartHandler multipartHandler,
            StoreConnector storeConnector, String tenantId) throws Exception {
    	
        for (Map.Entry<String, List<String>> entry : multipartHandler
                .getMultiValueMap().entrySet()) {
            String key = entry.getKey();

            if (key == null) {
                continue;
            }

            List<String> value = entry.getValue();

            if ((value == null) || (value.isEmpty())) {
                continue;
            }

            Prop prop = new Prop();
            prop.setCode(key);
            prop.setType(0);
            prop.setValue(this.getValue(value));
            record.getProps().put(prop.getCode(), prop);
        }

        if (multipartHandler.getMultiFileMap() == null) {
            return record;
        }

        for (Map.Entry<String, List<MultipartFile>> entry : multipartHandler
                .getMultiFileMap().entrySet()) {
            String key = entry.getKey();

            if (key == null) {
                continue;
            }

            List<MultipartFile> value = entry.getValue();

            if ((value == null) || (value.isEmpty())) {
                continue;
            }

            MultipartFile multipartFile = value.get(0);

            if ((multipartFile.getName() == null)
                    || "".equals(multipartFile.getName().trim())) {
                continue;
            }

            if (multipartFile.getSize() == 0) {
                logger.info("ignore empty file");

                continue;
            }

            Prop prop = new Prop();
            prop.setCode(key);
            prop.setType(0);
            prop.setValue(storeConnector.saveStore("form",
                    new MultipartFileDataSource(multipartFile), tenantId)
                    .getKey());
            record.getProps().put(prop.getCode(), prop);
        }

        return record;
    }

    public Record build(Record record,
            MultiValueMap<String, String> multiValueMap, String tenantId)
            throws Exception {

        for (Map.Entry<String, List<String>> entry : multiValueMap.entrySet()) {
            String key = entry.getKey();

            if (key == null) {
                continue;
            }

            List<String> value = entry.getValue();

            if ((value == null) || (value.isEmpty())) {
                continue;
            }

            Prop prop = new Prop();
            prop.setCode(key);
            prop.setType(0);
            prop.setValue(this.getValue(value));
            record.getProps().put(prop.getCode(), prop);
        }

        return record;
    }

    /**
     * 主要是获得多值属性，比如checkbox.
     */
    public String getValue(List<String> values) {
        if ((values == null) || (values.isEmpty())) {
            return "";
        }

        if (values.size() == 1) {
            return values.get(0);
        }

        StringBuilder buff = new StringBuilder();

        for (String value : values) {
            buff.append(value).append(",");
        }

        buff.deleteCharAt(buff.length() - 1);

        return buff.toString();
    }
  
}
