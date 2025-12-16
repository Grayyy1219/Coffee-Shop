package app.db;

import app.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    // matches your schema: users(id, username, password, role) :contentReference[oaicite:1]{index=1}
    public List<User> findAll() throws SQLException {
        String sql = "SELECT id, username, password, role FROM users ORDER BY id ASC";
        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<User> out = new ArrayList<>();
            while (rs.next()) {
                out.add(new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("role")
                ));
            }
            return out;
        }
    }

    public User findById(int id) throws SQLException {
        String sql = "SELECT id, username, password, role FROM users WHERE id = ?";
        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("role")
                );
            }
        }
    }

    public int insert(User u) throws SQLException {
        String sql = "INSERT INTO users(username, password, role) VALUES(?,?,?)";
        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, u.getUsername());
            ps.setString(2, u.getPassword());
            ps.setString(3, u.getRole());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
            return -1;
        }
    }

    public boolean update(User u) throws SQLException {
        String sql = "UPDATE users SET username=?, password=?, role=? WHERE id=?";
        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, u.getUsername());
            ps.setString(2, u.getPassword());
            ps.setString(3, u.getRole());
            ps.setInt(4, u.getId());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM users WHERE id=?";
        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean usernameExists(String username, Integer excludeId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?"
                + (excludeId != null ? " AND id <> ?" : "");
        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);
            if (excludeId != null) ps.setInt(2, excludeId);

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }
}
