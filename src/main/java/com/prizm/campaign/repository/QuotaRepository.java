package com.prizm.campaign.repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.prizm.campaign.model.UserCampaignQuota;
import com.prizm.campaign.model.UserCampaignQuotaId;

@Repository
public interface QuotaRepository extends JpaRepository<UserCampaignQuota, UserCampaignQuotaId> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT q FROM UserCampaignQuota q WHERE q.userId = :userId AND q.campaignId = :campaignId")
    Optional<UserCampaignQuota> findByUserIdAndCampaignIdWithLock(
        @Param("userId") String userId,
        @Param("campaignId") Long campaignId
    );
}