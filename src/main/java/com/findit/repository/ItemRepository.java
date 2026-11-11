package com.findit.repository;

import com.findit.model.Category;
import com.findit.model.Item;
import com.findit.model.ItemStatus;
import com.findit.util.DatabaseConnection;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class ItemRepository<T extends Item> {

    private final String table;
    private final String idColumn;
    private final String dateColumn;

    protected ItemRepository(String table, String idColumn, String dateColumn) {
        this.table = table;
        this.idColumn = idColumn;
        this.dateColumn = dateColumn;
    }

    /** Each child builds its own type (LostItem or FoundItem). */
    protected abstract T create(int id, String name, Category category,
                                String description, String location,
                                LocalDate date, String imagePath, int reportedBy);

    // ---------- Create ----------

    public T save(T item) {
        String sql = "INSERT INTO " + table + " (name, description, location, "
                + dateColumn + ", image_path, status, user_id, category_id) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, "
                + "(SELECT category_id FROM categories WHERE name = ?))";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, item.getName());
            ps.setString(2, item.getDescription());
            ps.setString(3, item.getLocation());
            ps.setDate(4, Date.valueOf(item.getDate()));
            ps.setString(5, item.getImagePath());
            ps.setString(6, item.getStatus().name());
            ps.setInt(7, item.getReportedBy());
            ps.setString(8, item.getCategory().name());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    T saved = create(keys.getInt(1), item.getName(), item.getCategory(),
                            item.getDescription(), item.getLocation(), item.getDate(),
                            item.getImagePath(), item.getReportedBy());
                    saved.setStatus(item.getStatus());
                    return saved;
                }
            }
            throw new DataAccessException("Insert succeeded but no ID returned", null);
        } catch (SQLException e) {
            throw new DataAccessException("Could not save item", e);
        }
    }

    // ---------- Read ----------

    public Optional<T> findById(int id) {
        List<T> result = queryList(selectSql() + " WHERE i." + idColumn + " = ?", id);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    public List<T> findAll() {
        return queryList(selectSql() + " ORDER BY i." + idColumn, null);
    }

    public List<T> findByUser(int userId) {
        return queryList(selectSql() + " WHERE i.user_id = ? ORDER BY i." + idColumn, userId);
    }

    // ---------- Update / delete ----------

    public void update(T item) {
        String sql = "UPDATE " + table
                + " SET name = ?, description = ?, location = ?, status = ? WHERE "
                + idColumn + " = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, item.getName());
            ps.setString(2, item.getDescription());
            ps.setString(3, item.getLocation());
            ps.setString(4, item.getStatus().name());
            ps.setInt(5, item.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Could not update item", e);
        }
    }

    public void delete(T item) {
        String sql = "DELETE FROM " + table + " WHERE " + idColumn + " = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, item.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Could not delete item", e);
        }
    }

    // ---------- Helpers ----------

    private String selectSql() {
        return "SELECT i.*, c.name AS category_name FROM " + table
                + " i JOIN categories c ON i.category_id = c.category_id";
    }

    private List<T> queryList(String sql, Integer param) {
        List<T> items = new ArrayList<>();
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            if (param != null) {
                ps.setInt(1, param);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(mapRow(rs));
                }
            }
            return items;
        } catch (SQLException e) {
            throw new DataAccessException("Could not load items", e);
        }
    }

    private T mapRow(ResultSet rs) throws SQLException {
        String description = rs.getString("description");
        String image = rs.getString("image_path");

        T item = create(
                rs.getInt(idColumn),
                rs.getString("name"),
                Category.valueOf(rs.getString("category_name")),
                description == null ? "" : description,
                rs.getString("location"),
                rs.getDate(dateColumn).toLocalDate(),
                image == null ? "" : image,
                rs.getInt("user_id"));
        item.setStatus(ItemStatus.valueOf(rs.getString("status")));
        return item;
    }
}