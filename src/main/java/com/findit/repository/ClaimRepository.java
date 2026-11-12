package com.findit.repository;

import com.findit.model.Claim;
import com.findit.model.ClaimStatus;
import com.findit.util.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ClaimRepository {

    // ---------- Create ----------

    public Claim save(Claim claim) {
        String sql = "INSERT INTO claims (found_item_id, claimant_id, proof_details) "
                   + "VALUES (?, ?, ?)";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, claim.getFoundItemId());
            ps.setInt(2, claim.getClaimantId());
            ps.setString(3, claim.getProofDetails());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return new Claim(keys.getInt(1), claim.getFoundItemId(),
                            claim.getClaimantId(), claim.getProofDetails());
                }
            }
            throw new DataAccessException("Insert succeeded but no ID returned", null);
        } catch (SQLException e) {
            throw new DataAccessException("Could not save claim", e);
        }
    }

    // ---------- Read ----------

    public Optional<Claim> findById(int id) {
        List<Claim> result = query("SELECT * FROM claims WHERE claim_id = ?", id);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    public List<Claim> findByClaimant(int claimantId) {
        return query("SELECT * FROM claims WHERE claimant_id = ? ORDER BY claim_id", claimantId);
    }

    public List<Claim> findByFoundItem(int foundItemId) {
        return query("SELECT * FROM claims WHERE found_item_id = ? ORDER BY claim_id", foundItemId);
    }

    public List<Claim> findAll() {
        return query("SELECT * FROM claims ORDER BY claim_id", null);
    }

    // ---------- Update ----------

    public void updateStatus(int claimId, ClaimStatus status) {
        String sql = "UPDATE claims SET status = ? WHERE claim_id = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, claimId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Could not update claim", e);
        }
    }

    /**
     * Approves one claim, marks the item CLAIMED, and rejects the other
     * pending claims. All three changes succeed together or not at all.
     */
    public void approve(int claimId, int foundItemId) {
        try (Connection con = DatabaseConnection.getConnection()) {
            con.setAutoCommit(false);                       // start transaction
            try {
                run(con, "UPDATE claims SET status = 'APPROVED' WHERE claim_id = ?",
                        claimId);
                run(con, "UPDATE found_items SET status = 'CLAIMED' WHERE found_item_id = ?",
                        foundItemId);
                run(con, "UPDATE claims SET status = 'REJECTED' "
                       + "WHERE found_item_id = ? AND claim_id <> ? AND status = 'PENDING'",
                        foundItemId, claimId);
                con.commit();                               // make all three permanent
            } catch (SQLException e) {
                con.rollback();                             // undo everything
                throw e;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not approve claim (rolled back)", e);
        }
    }

    // ---------- Helpers ----------

    private void run(Connection con, String sql, int... params) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setInt(i + 1, params[i]);
            }
            ps.executeUpdate();
        }
    }

    private List<Claim> query(String sql, Integer param) {
        List<Claim> claims = new ArrayList<>();
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (param != null) {
                ps.setInt(1, param);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    claims.add(new Claim(
                            rs.getInt("claim_id"),
                            rs.getInt("found_item_id"),
                            rs.getInt("claimant_id"),
                            rs.getString("proof_details"),
                            ClaimStatus.valueOf(rs.getString("status")),
                            rs.getTimestamp("created_at").toLocalDateTime()));
                }
            }
            return claims;
        } catch (SQLException e) {
            throw new DataAccessException("Could not load claims", e);
        }
    }
}