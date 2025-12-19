package app.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AuthService {

    public UserAuthResult authenticate(String username, String password) {
        String userSql = "SELECT id, password, role FROM users WHERE username = ?";
        String securitySql = "SELECT failed_attempts, locked FROM user_security WHERE user_id = ?";

        try (Connection c = DB.getConnection();
             PreparedStatement userPs = c.prepareStatement(userSql)) {

            ensureSecurityTable(c);

            userPs.setString(1, username);
            try (ResultSet rs = userPs.executeQuery()) {
                if (!rs.next()) {
                    return UserAuthResult.error("Invalid username or password.");
                }

                int id = rs.getInt("id");
                String storedPassword = rs.getString("password");
                String role = rs.getString("role");
                boolean isOwner = "owner".equalsIgnoreCase(role);

                int failedAttempts = 0;
                boolean locked = false;

                if (!isOwner) {
                    try (PreparedStatement secPs = c.prepareStatement(securitySql)) {
                        secPs.setInt(1, id);
                        try (ResultSet secRs = secPs.executeQuery()) {
                            if (secRs.next()) {
                                failedAttempts = secRs.getInt("failed_attempts");
                                locked = secRs.getBoolean("locked");
                            } else {
                                upsertSecurity(c, id, 0, false);
                            }
                        }
                    }
                }

                if (!isOwner && locked) {
                    return UserAuthResult.error("Account locked. Please contact an admin to unlock.");
                }

                if (storedPassword != null && storedPassword.equals(password)) {
                    if (!isOwner) {
                        upsertSecurity(c, id, 0, false);
                    }
                    return UserAuthResult.success(id, role);
                }

                if (!isOwner) {
                    int nextAttempts = failedAttempts + 1;
                    boolean lockNow = nextAttempts >= 3;
                    upsertSecurity(c, id, nextAttempts, lockNow);
                    if (lockNow) {
                        return UserAuthResult.error("Account locked after 3 failed attempts. Contact an admin to unlock.");
                    }
                    int remaining = 3 - nextAttempts;
                    return UserAuthResult.error("Invalid username or password. " + remaining + " attempt(s) remaining.");
                }

                return UserAuthResult.error("Invalid username or password.");
            }
        } catch (Exception ex) {
            return UserAuthResult.error("Database error: " + ex.getMessage());
        }
    }

    private void ensureSecurityTable(Connection con) throws Exception {
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

    private void upsertSecurity(Connection con, int userId, int failedAttempts, boolean locked) throws Exception {
        String sql = "INSERT INTO user_security (user_id, failed_attempts, locked) VALUES (?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE failed_attempts = VALUES(failed_attempts), locked = VALUES(locked)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, failedAttempts);
            ps.setBoolean(3, locked);
            ps.executeUpdate();
        }
    }

    public static final class UserAuthResult {
        public final boolean ok;
        public final int userId;
        public final String role;
        public final String message;

        private UserAuthResult(boolean ok, int userId, String role, String message) {
            this.ok = ok;
            this.userId = userId;
            this.role = role;
            this.message = message;
        }

        public static UserAuthResult success(int userId, String role) {
            return new UserAuthResult(true, userId, role, "Login successful");
        }

        public static UserAuthResult error(String msg) {
            return new UserAuthResult(false, -1, null, msg);
        }
    }
}
