package com.mossle.party;

public class PartyConstants {
	
	// 组织类型
	/**
	 * 公司、部门、小组
	 */
    public static final int TYPE_ORG = 0;
    /**
     * 人员
     */
    public static final int TYPE_USER = 1;
    /**
     * 岗位
     */
    public static final int TYPE_POSITION = 2;
    
    // 组织结构类型
    /**
     * 组织关系
     */
    public static final Long PARTY_STRUCT_TYPE_ORG = 1L;
    
    /**
     * 管理关系
     */
    public static final Long PARTY_STRUCT_TYPE_MANAGE = 2L;
    
    /**
     * 岗位关系
     */
    public static final Long PARTY_STRUCT_TYPE_POSITION_USER = 4L;
    
    /**
     * 超级系统管理员Id
     */
    public static final Long ADMIN_USER_ID = 2L;
    
    /**
     * 组织机构根节点ID
     */
    public static final Long ROOT_PARTY_TREE_ID = 1L;
    
    // 机构类型
    
    /**
	 * 机构类型 人员
	 */
   public static final Long PARTY_TYPE_USER = 1L;
    /**
	 * 机构类型 公司
	 */
    public static final Long PARTY_TYPE_COMPANY = 2L;
    
    /**
	 * 机构类型 部门
	 */
    public static final Long PARTY_TYPE_DEPARTMENT = 3L;
    
    /**
	 * 机构类型 小组
	 */
    public static final Long PARTY_TYPE_GROUP = 4L;
    
    /**
	 * 机构类型 岗位
	 */
    public static final Long PARTY_TYPE_POST = 5L;
    
    /**
	 * 机构类型 大区
	 */
    public static final Long PARTY_TYPE_AREA = 6L;
    
    /**
	 * 正常
	 */
    public static final String PARTY_NORMAL = "0";
    
    /**
	 * 删除
	 */
    public static final String PARTY_DELETE = "1";
    
    /**
   	 * 系统机器人id
   	 */
    public static final String SYSTEM_ROBOT_ID = "3";
    
    /**
   	 * 系统机器人名称
   	 */
    public static final String SYSTEM_ROBOT_NAME = "系统机器人";
    
    /**
     * 发起流程经销商ID
     * **/
    public static final String JXS_ID="4";
    
    
}
