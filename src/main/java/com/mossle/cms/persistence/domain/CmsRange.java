package com.mossle.cms.persistence.domain;

import javax.persistence.*;

/**
 * Created by wanghan on 2017\10\18 0018.
 */
@Entity
@Table(name = "CMS_RANGE")
public class CmsRange {
    private Long id;
    private String partyId;

    private CmsArticle cmsArticle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cms_id")
    public CmsArticle getCmsArticle() {
        return cmsArticle;
    }

    public void setCmsArticle(CmsArticle cmsArticle) {
        this.cmsArticle = cmsArticle;
    }

    @Id
    @Column(name = "ID", unique = true)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "PARTY_ID")
    public String getPartyId() {
        return partyId;
    }

    public void setPartyId(String partyId) {
        this.partyId = partyId;
    }
}
