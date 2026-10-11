package com.prizm.campaign.model;

import java.io.Serializable;
import java.util.Objects;

public class UserCampaignQuotaId implements Serializable {

    private String userId;
    private Long campaignId;

    public UserCampaignQuotaId() {}

    public UserCampaignQuotaId(String userId, Long campaignId) {
        this.userId = userId;
        this.campaignId = campaignId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserCampaignQuotaId that = (UserCampaignQuotaId) o;
        return Objects.equals(userId, that.userId) && Objects.equals(campaignId, that.campaignId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, campaignId);
    }
}
