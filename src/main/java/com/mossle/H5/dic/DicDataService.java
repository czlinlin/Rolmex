/**
 * 
 */
package com.mossle.H5.dic;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.codehaus.janino.Java.NewAnonymousClassInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.mossle.common.utils.StringUtils;
import com.mossle.core.annotation.Log;
import com.mossle.dict.persistence.domain.DictInfo;
import com.mossle.dict.support.DictConnectorImpl;
import com.mossle.api.dict.DictConnector;
import com.mossle.api.tenant.TenantHolder;

import org.springframework.jdbc.core.JdbcTemplate;

/**
 * @author lilei
 *
 */
@Service
public class DicDataService {
	private static Logger logger = LoggerFactory.getLogger(DicDataService.class);
	private DictConnector dictConnector;
	
	/**
	 * 
	 * 通过字典类型得到字典数据
	 * lilei at 2017.10.12
	 * 
	 * **/
	@Log(desc = "通过字典类型得到字典数据", action = "search", operationDesc = "手机APP-获取字典数据-通过字典类型得到字典数据")
	public Map<String, Object> GetDicDataByType(Map<String, Object> decryptedMap){
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			//orgConnector.
			//验证参数================================================================================
			if (!decryptedMap.containsKey("strDTCode") || StringUtils.isBlank(decryptedMap.get("strDTCode").toString())) {
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "参数错误");
				logger.debug("手机APP-获取字典数据-通过字典类型得到字典数据-手机没有传入strDTCode参数");
				return returnMap;
			}
			
			//获取数据================================================
			String dictTypeCode =decryptedMap.get("strDTCode").toString();
			
			logger.info("手机APP-获取字典数据-通过字典类型得到字典数据-传入参数：strDTCode="+dictTypeCode);
			
			List<Map<String,Object>> list=new ArrayList<Map<String,Object>>();
			List<DictInfo> dictList=dictConnector.findDictInfoListByType(dictTypeCode);
			if(dictList!=null&&dictList.size()>0)
			{
				/*{"bSuccess":"true","strMsg":"加载成功",
					"AreaData":[{"intDicID":"字典ID",
				"varDTCode":"字典类型编号","varDicCode":"字典编号",
				"varDicName":"字典名称","varDicValue":"字典值",
				"varRemark":"备注","varOperateMan":"操作人",
				"dtmAddTime":"操作时间","dtmUpdTime":"更新时间"},{…}]}*/
				
				for(DictInfo dictInfo:dictList){
					Map<String,Object> map=new HashMap<String, Object>();
					map.put("intDicID",dictInfo.getId().toString());
					map.put("varDTCode",dictInfo.getDictType().getId().toString());
					map.put("varDicCode",dictInfo.getId().toString());
					map.put("varDicName",dictInfo.getName().toString());
					map.put("varDicValue",dictInfo.getValue().toString());
					map.put("varOperateMan","");
					map.put("dtmAddTime",new Date());
					map.put("dtmUpdTime",new Date());
					
					list.add(map);
				}
				returnMap.put("bSuccess", "true");
				returnMap.put("strMsg", "加载成功");
				returnMap.put("DicData", list);
			}
			else{
				returnMap.put("bSuccess", "false");
				returnMap.put("strMsg", "获取成功");
			}
		} catch (Exception ex) {
			returnMap.put("bSuccess", "false");
			returnMap.put("strMsg", "加载错误，请联系管理员");
			logger.debug("手机APP-获取字典数据-通过字典类型得到字典数据-查询异常："
					+ex.getMessage()+"\r\n"+ex.getStackTrace());
		}
		return returnMap;
	}
	
	@Resource
    public void setDictConnector(DictConnector dictConnector){
    	this.dictConnector=dictConnector;
    }
}
