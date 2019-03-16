package com.mossle.base.persistence.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
/** 
 * @author  cz 
 * @version 2017年9月7日
 * 类说明 
 */
@Entity
@Table(name = "oa_ba_post_busiDetail_forConfirm")
public class PostBusiDetailForConfirmEntity  implements java.io.Serializable{


		 private static final long serialVersionUID = 0L;

		    /** 主键. */
		    private Long id;
		    
		    /** 业务类型明细 ID. */
		    private Long busiDetailID;
		    
		    /** 业务类型明细  名称. */
		    private String  busiDetailName;
		    
		    /** 岗位ID. */
		    private Long postID;
		    
		    /** 岗位名称. */
		    private String postName;
		    
		    /** 业务类型  ID. */
		    private Long  typeID;
		    
		    /** 业务类型  名称. */
		    private String  typeName;
		    
		    /** 租户ID. */
		    private String tenantId;
		    
		    /** 该条创建时间 */
		    private String createTime;
		    
		    
		    public PostBusiDetailForConfirmEntity() {
		    }

		    public PostBusiDetailForConfirmEntity(Long id) {
		        this.id = id;
		    }
		    
		    public PostBusiDetailForConfirmEntity(Long id, Long busiDetailID,Long postID) {
		        this.id = id;
		        this.busiDetailID = busiDetailID;
		        this.postID = postID;
		     }
		    
		    /** @return 主键. */
		    @Id
		    @Column(name = "id", unique = true, nullable = false)
		    public Long getId() {
		        return this.id;
		    }
		    /**
		     * @param id
		     *            主键.
		     */
		    public void setId(Long id) {
		        this.id = id;
		    }
		    /** @return 业务类型明细  ID. */
		    @Column(name = "busiDetail_id" )
		    public Long getBusiDetailID() {
				return busiDetailID;
			}

			public void setBusiDetailID(Long busiDetailID) {
				this.busiDetailID = busiDetailID;
			}
			
			 /**
		     * @param 
		     *          业务类型明细 名称.
		     */
			@Column(name = "detail_name" )
		    public String getBusiDetailName() {
				return busiDetailName;
			}

			public void setBusiDetailName(String busiDetailName) {
				this.busiDetailName = busiDetailName;
			}
			

		    /** @return 岗位ID. */
		    @Column(name = "post_id" )
		    public Long getPostID() {
		        return this.postID;
		    }

		    /**
		     * @param 
		     *         岗位ID.
		     */
		    public void setPostID(Long postID) {
		        this.postID = postID;
		    }
		    
		    /**
		     * @param 
		     *         岗位名称.
		     */
		    @Column(name = "post_name" )
		    public String getPostName() {
				return this.postName;
			}

			public void setPostName(String postName) {
				this.postName = postName;
			}

		    
		    /** @return 租户ID. */
		    @Column(name = "tenant_id")
		    public String getTenantId() {
		        return this.tenantId;
		    }

		    /**
		     * @param 
		     *          租户ID.
		     */
		    public void setTenantId(String tenantId) {
		        this.tenantId = tenantId;
		    }
		    
		    
		    /**
		     * @param 
		     *          业务类型 id.
		     */
		    @Column(name = "type_id")
			public Long getTypeID() {
				return typeID;
			}

			public void setTypeID(Long typeID) {
				this.typeID = typeID;
			}
			
			
			/**
		     * @param 
		     *          业务类型 名称.
		     */
			@Column(name = "type_name")
			public String getTypeName() {
				return typeName;
			}

			public void setTypeName(String typeName) {
				this.typeName = typeName;
			}
			
			/**
		     * @param 
		     *          创建时间.
		     */
			
			@Column(name = "createTime")
			 public String getCreateTime() {
					return createTime;
				}

			public void setCreateTime(String createTime) {
				this.createTime = createTime;
			}



	
}
