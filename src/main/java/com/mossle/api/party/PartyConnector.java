package com.mossle.api.party;

import java.util.List;

import org.activiti.engine.impl.cmd.AddCommentCmd;

public interface PartyConnector {
	
    PartyDTO findById(String partyId);
    
    /**
     * 通过PartyId 查找所属的大区
     * @author zyl
     * @param partyId
     * @return
     */
    PartyDTO findAreaById(String partyId);
    /**
     * 通过PartyId 查找所属的公司
     * @author zyl
     * @param partyId
     * @return
     */
    PartyDTO findCompanyById(String partyId);
    
    /**
     * 根据partyId查询公司（含缩写信息）
     * **/
    PartyEntityOrgDTO findCompanyInfoById(String partyId);
    
    /**
     * 根据partyId得到工号前缀
     * **/
    String getWorkNumerPrefix(String partyId);
    
    /**
     * 通过PartyId 查找所属的部门
     * @author zyl
     * @param partyId
     * @return
     */
    PartyDTO findDepartmentById(String partyId);
    
    /**
     * 跟据用户id得到公司或则大区信息
     * 循环先找到大区或则公司停止
     * ·add by lilei at 2018-06-29
     * **/
    PartyDTO getCompanyOrAreaByUserId(String partyEntityId);
    
    /**
     * 获取岗位编号
     * add by lilei at 2018-09-12
     * **/
    String getPartyPositionNo(String partyId);
    
    /**
     * 得到系统管理员/虚拟账户   ADD BY LILEI AT 2019.01.08
     * 1.超级管理员虚拟人员-2
	 * 2.系统管理员-对应角色区分
	 * 3.经销商虚拟人员-4
	 * 4.机器人虚拟人员-3
	 * 5.测试用户
     * **/
    List<String> getSystemAcccountIdList();
    
    /**
     * 根据当前userId
     * 查找当前部门（如果为公司则不查找）上级部门直到公司
     * 和获取下级所有部门
     * ADD BY LILEI AT 2019.01.21
     * **/
    public List<String> getUpperCompanyAndLowerDepartment(String userId);
    
    /**
     * 得到部门下所有部门/小组信息
     * ADD BY LILEI AT 2019.01.21
     * **/
    public List<String> getLowerAllDeparentById(Long partyEntityId);
    
    /**
     * 得到上级部门的这条线的partyId，直到公司
     * ADD BY LILEI AT 2019.01.21
     * **/
    public List<String> getUpperIdLineList(String partyEntityId,boolean isArea);
}
