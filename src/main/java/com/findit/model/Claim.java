package com.findit.model;

import java.time.LocalDateTime;

public class Claim {

    private final int id;
    private final int foundItemId;
    private final int claimantId;
    private final String proofDetails;
    private ClaimStatus status;
    private final LocalDateTime createdAt;

    /** For a brand-new claim: starts PENDING. */
    public Claim(int id, int foundItemId, int claimantId, String proofDetails) {
        this(id, foundItemId, claimantId, proofDetails,
                ClaimStatus.PENDING, LocalDateTime.now());
    }

    /** For a claim loaded from the database. */
    public Claim(int id, int foundItemId, int claimantId, String proofDetails,
                 ClaimStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.foundItemId = foundItemId;
        this.claimantId = claimantId;
        this.proofDetails = proofDetails;
        this.status = status;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public int getFoundItemId() { return foundItemId; }
    public int getClaimantId() { return claimantId; }
    public String getProofDetails() { return proofDetails; }
    public ClaimStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setStatus(ClaimStatus status) { this.status = status; }

    @Override
    public String toString() {
        return "[Claim #" + id + "] Item #" + foundItemId
                + " | Status: " + status
                + " | Proof: " + proofDetails;
    }
}