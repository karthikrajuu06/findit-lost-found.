package com.findit.repository;

import com.findit.model.Claim;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ClaimRepository {

    private final List<Claim> claims = new ArrayList<>();
    private int nextId = 1;

    public int generateId() {
        return nextId++;
    }

    public void save(Claim claim) {
        claims.add(claim);
    }

    public Optional<Claim> findById(int id) {
        for (Claim claim : claims) {
            if (claim.getId() == id) {
                return Optional.of(claim);
            }
        }
        return Optional.empty();
    }

    public List<Claim> findByClaimant(int claimantId) {
        List<Claim> result = new ArrayList<>();
        for (Claim claim : claims) {
            if (claim.getClaimantId() == claimantId) {
                result.add(claim);
            }
        }
        return result;
    }

    public List<Claim> findByFoundItem(int foundItemId) {
        List<Claim> result = new ArrayList<>();
        for (Claim claim : claims) {
            if (claim.getFoundItemId() == foundItemId) {
                result.add(claim);
            }
        }
        return result;
    }

    public List<Claim> findAll() {
        return new ArrayList<>(claims);
    }
}