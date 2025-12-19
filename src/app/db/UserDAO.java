package app.db;

import app.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    private void ensureSecurityTable(Connection con) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS user_security ("
                + "user_id INT PRIMARY KEY, "
                + "failed_attempts INT NOT NULL DEFAULT 0, "
                + "locked BOOLEAN NOT NULL DEFAULT FALSE, "
                + "CONSTRAINT fk_user_security_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE"
                + ")";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.executeUpdate();
        }
    }

    // matches your schema: users(id, username, password, role) :contentReference[oaicite:1]{index=1}
    public List<User> findAll() throws SQLException {
        String sql = "SELECT u.id, u.username, u.password, u.role, "
                + "COALESCE(s.failed_attempts, 0) AS failed_attempts, "
                + "COALESCE(s.locked, FALSE) AS locked "
                + "FROM users u "
                + "LEFT JOIN user_security s ON s.user_id = u.id "
                + "ORDER BY u.id ASC";
        try (Connection con = DB.getConnection()) {
            ensureSecurityTable(con);
            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                List<User> out = new ArrayList<>();
                while (rs.next()) {
                    out.add(new User(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("role"),
                            rs.getInt("failed_attempts"),
                            rs.getBoolean("locked")
                    ));
                }
                return out;
            }
        }
    }

    public User findById(int id) throws SQLException {
        String sql = "SELECT u.id, u.username, u.password, u.role, "
                + "COALESCE(s.failed_attempts, 0) AS failed_attempts, "
                + "COALESCE(s.locked, FALSE) AS locked "
                + "FROM users u "
                + "LEFT JOIN user_security s ON s.user_id = u.id "
                + "WHERE u.id = ?";
        try (Connection con = DB.getConnection()) {
            ensureSecurityTable(con);
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return null;
                    return new User(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("role"),
                            rs.getInt("failed_attempts"),
                            rs.getBoolean("locked")
                    );
                }
            }
        }
    }

    public int insert(User u) throws SQLException {
        String sql = "INSERT INTO users(username, password, role) VALUES(?,?,?)";
        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ensureSecurityTable(con);
            ps.setString(1, u.getUsername());
            ps.setString(2, u.getPassword());
            ps.setString(3, u.getRole());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    upsertSecurity(con, id, 0, false);
                    return id;
                }
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
            ensureSecurityTable(con);
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean usernameExists(String username, Integer excludeId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?"
                + (excludeId != null ? " AND id <> ?" : "");
        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ensureSecurityTable(con);
            ps.setString(1, username);
            if (excludeId != null) ps.setInt(2, excludeId);

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    public void updateSecurity(int userId, boolean locked, int failedAttempts) throws SQLException {
        try (Connection con = DB.getConnection()) {
            ensureSecurityTable(con);
            upsertSecurity(con, userId, failedAttempts, locked);
        }
    }

    private void upsertSecurity(Connection con, int userId, int failedAttempts, boolean locked) throws SQLException {
        String sql = "INSERT INTO user_security (user_id, failed_attempts, locked) VALUES (?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE failed_attempts = VALUES(failed_attempts), locked = VALUES(locked)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, failedAttempts);
            ps.setBoolean(3, locked);
            ps.executeUpdate();
        }
    }
}
