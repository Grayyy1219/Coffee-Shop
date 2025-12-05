package Admin;

import coffeeshop.db.DatabaseAccess;
import coffeeshop.db.MySqlDatabaseAccess;
import coffeeshop.order.OrderManager;
import coffeeshop.queue.OrderQueue;
import java.sql.SQLException;
import java.util.Objects;

/**
 * Simple authentication entry point for the desktop application.
 * The class delegates credential validation to a {@link DatabaseAccess}
 * implementation so it can work with the local MySQL database or a
 * mock implementation in tests.
 */
public class AdminLogin {

    private final DatabaseAccess databaseAccess;
    private final OrderManager orderManager;

    public AdminLogin(DatabaseAccess databaseAccess, OrderManager orderManager) {
        this.databaseAccess = Objects.requireNonNull(databaseAccess, "databaseAccess");
        this.orderManager = Objects.requireNonNull(orderManager, "orderManager");
    }

    /**
     * Convenience constructor that wires the application to the local MySQL
     * database using default connection settings.
     */
    public AdminLogin() {
        this(new MySqlDatabaseAccess(), new OrderManager(new OrderQueue(), new MySqlDatabaseAccess()));
    }

    /**
     * Verifies the username and password against the database.
     *
     * @param username supplied username
     * @param password supplied password
     * @return true if credentials are valid
     * @throws SQLException when the database cannot be reached
     */
    public boolean authenticate(String username, String password) throws SQLException {
        if (username == null || password == null || username.isBlank() || password.isBlank()) {
            return false;
        }
        return databaseAccess.validateUser(username, password);
    }

    /**
     * Runs a minimal demonstration by attempting login and printing the queue
     * size to show the linked-list-based queue is wired.
     */
    public static void main(String[] args) {
        try {
            AdminLogin login = new AdminLogin();
            boolean authenticated = login.authenticate("admin", "admin123");
            if (authenticated) {
                System.out.println("Login successful. Active orders: " + login.orderManager.getActiveOrderCount());
            } else {
                System.out.println("Invalid credentials");
            }
        } catch (SQLException ex) {
            System.err.println("Failed to authenticate: " + ex.getMessage());
        }
    }
}
