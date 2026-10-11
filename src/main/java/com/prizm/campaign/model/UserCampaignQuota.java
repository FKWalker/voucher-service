package com.prizm.campaign.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_campaign_quota")
@IdClass(UserCampaignQuotaId.class)
public class UserCampaignQuota {
	
	@Id
    @Column(name = "user_id", length = 80, nullable = false)
    private String userId;

    @Id
    @Column(name = "campaign_id", nullable = false)
    private Long campaignId;

    @Column(name = "used_count", nullable = false)
    private int usedCount;
    
    public UserCampaignQuota() {}

    public UserCampaignQuota(String userId, Long campaignId, int usedCount) {
        this.userId = userId;
        this.campaignId = campaignId;
        this.usedCount = usedCount;
    }

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public Long getCampaignId() {
		return campaignId;
	}

	public void setCampaignId(Long campaignId) {
		this.campaignId = campaignId;
	}

	public int getUsedCount() {
		return usedCount;
	}

	public void setUsedCount(int usedCount) {
		this.usedCount = usedCount;
	}

}
