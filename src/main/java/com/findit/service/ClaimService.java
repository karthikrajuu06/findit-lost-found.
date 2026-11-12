package com.findit.service;

import com.findit.model.*;
import com.findit.repository.ClaimRepository;
import com.findit.util.Validator;
import java.util.ArrayList;
import java.util.List;

public class ClaimService {

    private final ClaimRepository claimRepo;
    private final ItemService itemService;
    private final UserService userService;
    private final NotificationService notificationService;

    public ClaimService(ClaimRepository claimRepo, ItemService itemService,
                        UserService userService, NotificationService notificationService) {
        this.claimRepo = claimRepo;
        this.itemService = itemService;
        this.userService = userService;
        this.notificationService = notificationService;
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

        Claim saved = claimRepo.save(new Claim(0, foundItemId,
                claimant.getId(), proofDetails.trim()));

        notificationService.notifyUser(item.getReportedBy(),
                "New claim #" + saved.getId() + " on your found item '" + item.getName()
                + "'. Review it in the Claims menu.");
        return saved;
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

        // Remember the competing claims BEFORE the transaction changes them.
        List<Claim> competing = claimRepo.findByFoundItem(item.getId()).stream()
                .filter(c -> c.getId() != claim.getId()
                          && c.getStatus() == ClaimStatus.PENDING)
                .toList();

        claimRepo.approve(claim.getId(), item.getId());   // one transaction

        // Notifications only after the database changes succeeded.
        String contact = userService.findById(item.getReportedBy())
                .map(finder -> finder.getName() + ", phone " + finder.getPhone())
                .orElse("the finder");
        notificationService.notifyUser(claim.getClaimantId(),
                "Your claim #" + claim.getId() + " for '" + item.getName()
                + "' was APPROVED. Contact " + contact + " to collect it.");

        for (Claim other : competing) {
            notificationService.notifyUser(other.getClaimantId(),
                    "Your claim #" + other.getId() + " for '" + item.getName()
                    + "' was REJECTED because the item was given to another claimant.");
        }
    }

    public void reject(User reviewer, int claimId) {
        Claim claim = findReviewable(reviewer, claimId);
        claimRepo.updateStatus(claim.getId(), ClaimStatus.REJECTED);

        String itemName = itemService.findFoundById(claim.getFoundItemId())
                .map(FoundItem::getName).orElse("the item");
        notificationService.notifyUser(claim.getClaimantId(),
                "Your claim #" + claim.getId() + " for '" + itemName + "' was REJECTED.");
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
        itemService.updateFoundStatus(item, ItemStatus.RETURNED);

        for (Claim c : claimRepo.findByFoundItem(foundItemId)) {
            if (c.getStatus() == ClaimStatus.APPROVED) {
                notificationService.notifyUser(c.getClaimantId(),
                        "'" + item.getName() + "' is now marked as RETURNED. Glad it found its way back!");
            }
        }
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