package com.mossle.api.pim;

import java.util.List;
import java.util.Map;

/**
 * add by lilei at 2018.12.12
 * **/
public interface WorkReportConnector {
    /**
     * 获取状态为正常使用的汇报条线.
     * add by lilei at 2018.12.12
     */
    List<Map<String,Object>> getReportPreSetting();
    
    /**
     * 得到抄送规则(包括大区和非大区人员)
     * add by lilei at 2018.12.12
     * **/
    Map<String,Object> getReportCCSetting(String userId);
    
    /**
     * 是否是大区人员/岗位isArea=true表示是大区人员/岗位
     * ADD BY LILEI AT 2018-12-17
     * **/
    Map<String,Object> getAreaInfo(String userId);
    
    /**
     * 得到汇报的岗位信息（如果没有岗位，则返回人员信息）
     * ADD BY LILEI AT 2018.12.17
     * **/
    Map<String,Object> getReportPositionInfo(String userId);

    /**
     * 得到汇报抄送的人（根据设置条线）
     * 当strCCPreSettingId为空，非大区
     * add by lilei at 2018.12.12
     * **/
    List<Map<String,Object>> getReportCCSettingPersonList(String strCCPreSettingId,String userId);
    
    /**
     * 得到上级/条线设置的抄送人员/岗位
     * ADD BY LILEI AT 2018.12.18
     * **/
   String getReportPresettingccNos(String iptPositionId,Long preSettingId);
}
