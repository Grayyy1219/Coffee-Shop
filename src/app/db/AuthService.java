package app.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AuthService {

    public UserAuthResult authenticate(String username, String password) {
        String sql = "SELECT id, role FROM users WHERE username = ? AND password = ?";

        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("id");
                    String role = rs.getString("role");
                    return UserAuthResult.success(id, role);
                }
            }
        } catch (Exception ex) {
            return UserAuthResult.error("Database error: " + ex.getMessage());
        }

        return UserAuthResult.error("Invalid username or password.");
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
