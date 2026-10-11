package com.prizm.campaign.service;

import com.prizm.campaign.dto.RedeemResponse;
import com.prizm.campaign.model.Campaign;
import com.prizm.campaign.model.Redemption;
import com.prizm.campaign.model.UserCampaignQuota;
import com.prizm.campaign.model.Voucher;
import com.prizm.campaign.repository.CampaignRepository;
import com.prizm.campaign.repository.QuotaRepository;
import com.prizm.campaign.repository.RedemptionRepository;
import com.prizm.campaign.repository.VoucherRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class VoucherService {

    private static final Logger log = LoggerFactory.getLogger(VoucherService.class);

    @Autowired
    private VoucherRepository voucherRepository;

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private RedemptionRepository redemptionRepository;
    
    @Autowired
    private QuotaRepository quotaRepository;

    @Autowired
    private AuditClient auditClient;

    @Transactional
    public RedeemResponse redeem(String code, String userId) {

        // 1. Find voucher
        Voucher voucher = voucherRepository.findByCode(code);
        if (voucher == null) {
            return RedeemResponse.fail("Voucher not found");
        }

        // 2. Find campaign (needed for limit check)
        Campaign campaign = campaignRepository.findById(voucher.getCampaignId())
                .orElseThrow(() -> new RuntimeException("Campaign not found"));

        if (!campaign.isActive()) {
            return RedeemResponse.fail("Campaign is not active");
        }
        
        if (campaign.getRemainingStock() <= 0) {
            return RedeemResponse.fail("Campaign out of stock");
        }

        // 3. Lock and check user quota first
        UserCampaignQuota quota;
        try {
            quota = quotaRepository.findByUserIdAndCampaignIdWithLock(userId, campaign.getId())
                .orElseGet(() -> quotaRepository.save(new UserCampaignQuota(userId, campaign.getId(), 0)));
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            quota = quotaRepository.findByUserIdAndCampaignIdWithLock(userId, campaign.getId())
                .orElseThrow(() -> new RuntimeException("Failed to initialize user quota"));
        }

        if (quota.getUsedCount() >= campaign.getUserVoucherLimit()) {
            return RedeemResponse.fail("User limit reached for this campaign");
        }

        // 4. ATOMIC CLAIM VOUCHER FIRST
        int updatedVoucherRows = voucherRepository.claimVoucher(voucher.getId(), userId, new Date());
        if (updatedVoucherRows == 0) {
            // Since NO stock or quota has been changed yet, we can return a clean response 
            // directly WITHOUT needing a rollback! Re-fetch to see why it failed.
            Voucher currentVoucher = voucherRepository.findById(voucher.getId()).orElse(voucher);
            
            if ("REDEEMED".equals(currentVoucher.getStatus())) {
                return RedeemResponse.fail("Voucher has already been redeemed");
            } else if ("VOID".equals(currentVoucher.getStatus())) {
                return RedeemResponse.fail("Voucher is void");
            }
            return RedeemResponse.fail("Voucher is invalid");
        }

        // 5. ATOMICALLY DECREMENT CAMPAIGN STOCK SECOND
        int updatedCampaignRows = campaignRepository.decrementStock(campaign.getId());
        if (updatedCampaignRows == 0) {
        	// Explicitly tell Spring to roll back the voucher claim & quota increment we just did!
            org.springframework.transaction.interceptor.TransactionAspectSupport
                .currentTransactionStatus()
                .setRollbackOnly();

            // Now we can safely return a clean response to the user
            return RedeemResponse.fail("Campaign out of stock");
        }

        // 6. Increment user quota and save
        quota.setUsedCount(quota.getUsedCount() + 1);
        quotaRepository.save(quota);

        // 7. Save redemption history
        Redemption redemption = new Redemption(
                voucher.getId(), campaign.getId(), userId, new Date());
        redemptionRepository.save(redemption);

        // 8. Audit log (Async/Safe)
        try {
            auditClient.recordRedemption(campaign.getClientCode(), voucher.getCode(), userId);
        } catch (Exception e) {
            log.error("audit call failed", e);
        }

        return RedeemResponse.ok(voucher.getCode(), campaign.getRemainingStock() - 1);
    }

    public RedeemResponse voidVoucher(String code) {
        Voucher voucher = voucherRepository.findByCode(code);
        if (voucher == null) {
            return RedeemResponse.fail("Voucher not found");
        }
        voucher.setStatus("VOID");
        voucherRepository.save(voucher);
        return RedeemResponse.ok(voucher.getCode(), null);
    }
}
