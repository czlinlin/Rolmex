package com.mossle.api.org;

import java.util.List;
import java.util.Map;

import com.mossle.party.persistence.domain.PartyEntity;

/**
 * 试验中的组织机构相关的connector.
 */
public interface OrgConnector {
	
    /**
     * 根据userId获得职位级别.
     */
    int getJobLevelByUserId(String userId);
    
    /**
     * 根据userIds获得字符串中最高的职位等级.
     */
    String getPositionMaxGradeByUserIds(String userIds);
    
    /**
     * 获得userId最近的positionName对应的职位级别.
     */
    int getJobLevelByInitiatorAndPosition(String userId, String positionName);

    /**
     * 获得上级id.
     */
    String getSuperiorId(String userId);

    /**
     * 获得userId最近的positionName下的所有人员.
     */
    List<String> getPositionUserIds(String userId, String positionName);
    
    /**
     * 通过user查找岗位中对应的用户. 同级、上级、上上级即同一个树枝下
     * @author zyl
     */
    List<String> findPositionUserByUserId(String userId, String positionName);

    /**
     * 根据userId获得所有最近的部门或公司.
     */
    List<OrgDTO> getOrgsByUserId(String userId);
    
    /**
     * 通过user查找所属大区
     * @author zyl
     */
    PartyEntity findPartyAreaByUserId(String userId);
    
    /**
     * 通过user查找所属公司
     * @author zyl
     */
    PartyEntity findPartyCompanyByUserId(String userId);
    
    /**
     * 根据userId获得对应的PartyEntity.
     * **/
    PartyEntity findUser(String userId);
    
    /**
     * 获得上级部门.
     */
    PartyEntity findUpperDepartment(PartyEntity child,
            boolean skipAdminDepartment);
    
    /**
    * 获得userId所属的岗位信息.
    */
   List<PartyEntity> getPostByUserId(String userId);
   
   /**
    * 根据userId得到所有岗位（包括已 删除人员）
    * **/
   public List<PartyEntity> getPostContainDELFlagByUserId(String userId);
   
   /**
    * 获得部门下所有人员信息.
    */
   List<PartyEntity> getUsersByDepartmentId(Long departmentId);
   
   /**
    * 获得岗位下所有人员信息.
    */
   List<PartyEntity> getUsersByPostId(Long postId);
   
   /**
    * 通过user查找岗位中对应的管理者(向上逐级查找)
    *
    * @author zyl
    */
   public List<String> findManageUserByUserId(String userId, String postId);
   
   /**
    * 获得管理者对应的组织机构.
    */
   public List<Long> getPartyByManageId(String userId);
   
   /**
    * 获得组织机构对应的管理者.
    */
	public List<String> getManageIdByParty(PartyEntity partyEntity);
	
	/**
     * 获取人的岗位信息
     * add by lilei at 2018.12.12
     * **/
	List<Map<String,Object>> getPositionInfo(String userId);
	
   /*********
    * 得到组织机构上级id
    * ADD BY LILEI AT 2018.09.07
    * *******/
   public Map<String, String> getParentPartyEntityId(String partyId);
}
