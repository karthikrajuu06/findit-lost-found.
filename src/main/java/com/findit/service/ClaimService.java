package com.findit.service;

import com.findit.model.*;
import com.findit.repository.ClaimRepository;
import com.findit.util.Validator;
import java.util.ArrayList;
import java.util.List;

public class ClaimService {

    private final ClaimRepository claimRepo;
    private final ItemService itemService;

    public ClaimService(ClaimRepository claimRepo, ItemService itemService) {
        this.claimRepo = claimRepo;
        this.itemService = itemService;
    }

    public Claim submitClaim(User claimant, int foundItemId, String proofDetails) {
        if (!Validator.isNotBlank(proofDetails)) {
            throw new IllegalArgumentException("Proof details are required.");
        }

        FoundItem item = itemService.findFoundById(foundItemId)
                .orElseThrow(() -> new IllegalArgumentException("Found item not found."));

        if (item.getStatus() != ItemStatus.OPEN) {
            throw new IllegalArgumentException("This item is no longer available for claims.");
        }
        if (item.getReportedBy() == claimant.getId()) {
            throw new IllegalArgumentException("You cannot claim an item you reported.");
        }
        for (Claim existing : claimRepo.findByFoundItem(foundItemId)) {
            if (existing.getClaimantId() == claimant.getId()
                    && existing.getStatus() == ClaimStatus.PENDING) {
                throw new IllegalArgumentException("You already have a pending claim on this item.");
            }
        }

        Claim claim = new Claim(claimRepo.generateId(), foundItemId,
                claimant.getId(), proofDetails.trim());
        claimRepo.save(claim);
        return claim;
    }

    public List<Claim> getMyClaims(User user) {
        return claimRepo.findByClaimant(user.getId());
    }

    public List<Claim> getClaimsForMyItems(User user) {
        List<Claim> result = new ArrayList<>();
        for (FoundItem item : itemService.getMyFound(user)) {
            result.addAll(claimRepo.findByFoundItem(item.getId()));
        }
        return result;
    }

    public List<Claim> getAllClaims(User admin) {
        requireAdmin(admin);
        return claimRepo.findAll();
    }

    public void approve(User reviewer, int claimId) {
        Claim claim = findReviewable(reviewer, claimId);
        FoundItem item = itemService.findFoundById(claim.getFoundItemId()).get();

        claim.setStatus(ClaimStatus.APPROVED);
        item.setStatus(ItemStatus.CLAIMED);

        for (Claim other : claimRepo.findByFoundItem(item.getId())) {
            if (other.getId() != claim.getId()
                    && other.getStatus() == ClaimStatus.PENDING) {
                other.setStatus(ClaimStatus.REJECTED);
            }
        }
    }

    public void reject(User reviewer, int claimId) {
        Claim claim = findReviewable(reviewer, claimId);
        claim.setStatus(ClaimStatus.REJECTED);
    }

    public void markReturned(User reviewer, int foundItemId) {
        FoundItem item = itemService.findFoundById(foundItemId)
                .orElseThrow(() -> new IllegalArgumentException("Found item not found."));
        boolean isFinder = item.getReportedBy() == reviewer.getId();
        boolean isAdmin = reviewer.getRole() == Role.ADMIN;
        if (!isFinder && !isAdmin) {
            throw new SecurityException("Only the finder or an admin can mark an item returned.");
        }
        if (item.getStatus() != ItemStatus.CLAIMED) {
            throw new IllegalArgumentException("Only a CLAIMED item can be marked returned.");
        }
        item.setStatus(ItemStatus.RETURNED);
    }

    // ---------- helpers ----------

    private Claim findReviewable(User reviewer, int claimId) {
        Claim claim = claimRepo.findById(claimId)
                .orElseThrow(() -> new IllegalArgumentException("Claim not found."));

        FoundItem item = itemService.findFoundById(claim.getFoundItemId())
                .orElseThrow(() -> new IllegalArgumentException("Item for this claim no longer exists."));

        boolean isFinder = item.getReportedBy() == reviewer.getId();
        boolean isAdmin = reviewer.getRole() == Role.ADMIN;
        if (!isFinder && !isAdmin) {
            throw new SecurityException("You can only review claims on your own found items.");
        }
        if (claim.getStatus() != ClaimStatus.PENDING) {
            throw new IllegalArgumentException("This claim was already reviewed.");
        }
        return claim;
    }

    private void requireAdmin(User user) {
        if (user.getRole() != Role.ADMIN) {
            throw new SecurityException("Admin access required.");
        }
    }
}