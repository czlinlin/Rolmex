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
@Table(name = "oa_ba_business_post")
public class DetailPostEntity  implements java.io.Serializable{


		 private static final long serialVersionUID = 0L;

		    /** 主键. */
		    private Long id;
		    
		    /** 业务类型ID. */
		    private Long detailID;
		    
		    /** 岗位ID. */
		    private Long postID;
		    
		    /** 岗位名称. */
		    private String postName;
		    
		    /** 租户ID. */
		    private String tenantId;
		    
		    
		    public DetailPostEntity() {
		    }

		    public DetailPostEntity(Long id) {
		        this.id = id;
		    }
		    
		    public DetailPostEntity(Long id, Long detailID,Long postID,String postName) {
		        this.id = id;
		        this.detailID = detailID;
		        this.postID = postID;
		        this.postName = postName;
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
		    /** @return 业务类型ID. */
		    @Column(name = "detail_id" )
		    public Long getdetailID() {
				return detailID;
			}

			public void setdetailID(Long detailID) {
				this.detailID = detailID;
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
		    
		    /** @return 岗位名称. */
		    @Column(name = "post_name" )
		    public String getPostName() {
		        return this.postName;
		    }

		    /**
		     * @param 
		     *          岗位名称.
		     */
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

	
}
