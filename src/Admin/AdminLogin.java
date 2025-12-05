package Admin;

import coffeeshop.db.DatabaseAccess;
import coffeeshop.db.InMemoryDatabaseAccess;
import coffeeshop.db.MySqlDatabaseAccess;
import coffeeshop.order.OrderManager;
import coffeeshop.queue.OrderQueue;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 * Simple authentication entry point for the desktop application.
 * The class delegates credential validation to a {@link DatabaseAccess}
 * implementation so it can work with the local MySQL database or a
 * mock implementation in tests.
 */
public class AdminLogin {

    private final DatabaseAccess databaseAccess;
    private final OrderManager orderManager;
    private static final Logger LOGGER = Logger.getLogger(AdminLogin.class.getName());

    public AdminLogin(DatabaseAccess databaseAccess, OrderManager orderManager) {
        this.databaseAccess = Objects.requireNonNull(databaseAccess, "databaseAccess");
        this.orderManager = Objects.requireNonNull(orderManager, "orderManager");
    }

    /**
     * Convenience constructor that wires the application to the local MySQL
     * database using default connection settings when available, falling back
     * to an in-memory store if the database cannot be reached.
     */
    public AdminLogin() {
        DatabaseAccess selectedDatabase = createPrimaryDatabaseAccess();
        this.databaseAccess = selectedDatabase;
        this.orderManager = new OrderManager(new OrderQueue(), selectedDatabase);
    }

    private static DatabaseAccess createPrimaryDatabaseAccess() {
        MySqlDatabaseAccess mySqlDatabaseAccess = new MySqlDatabaseAccess();
        try {
            if (mySqlDatabaseAccess.canConnect()) {
                LOGGER.info("Using MySQL database for admin login.");
                return mySqlDatabaseAccess;
            }
            LOGGER.warning("MySQL database is reachable but returned no result; switching to in-memory database for login.");
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "Unable to connect to MySQL database, falling back to in-memory login store.", ex);
        }
        return new InMemoryDatabaseAccess();
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
        String trimmedUsername = username == null ? null : username.trim();
        String trimmedPassword = password == null ? null : password.trim();

        if (trimmedUsername == null || trimmedPassword == null || trimmedUsername.isBlank() || trimmedPassword.isBlank()) {
            LOGGER.log(Level.WARNING, "Login attempt rejected due to blank credentials. Username provided: {0}", username);
            return false;
        }

        boolean valid = databaseAccess.validateUser(trimmedUsername, trimmedPassword);
        LOGGER.log(Level.INFO, "Login attempt for user ''{0}'' resulted in: {1}", new Object[]{trimmedUsername, valid ? "SUCCESS" : "INVALID"});
        return valid;
    }

    public int getActiveOrderCount() {
        return orderManager.getActiveOrderCount();
    }

    public static void main(String[] args) {
        DatabaseAccess databaseAccess = createPrimaryDatabaseAccess();
        OrderManager orderManager = new OrderManager(new OrderQueue(), databaseAccess);
        AdminLogin login = new AdminLogin(databaseAccess, orderManager);

        SwingUtilities.invokeLater(() -> {
            AdminLoginFrame frame = new AdminLoginFrame(login);
            frame.setVisible(true);
        });
    }
}

class AdminLoginFrame extends JFrame {

    private final AdminLogin adminLogin;
    private static final Logger LOGGER = Logger.getLogger(AdminLoginFrame.class.getName());
    private final JTextField usernameField = new JTextField(15);
    private final JPasswordField passwordField = new JPasswordField(15);
    private final JButton loginButton = new JButton("Login");
    private final JLabel statusLabel = new JLabel(" ");

    AdminLoginFrame(AdminLogin adminLogin) {
        this.adminLogin = adminLogin;
        setTitle("Coffee Shop Admin Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        initializeLayout();
        pack();
        setLocationRelativeTo(null);
        getRootPane().setDefaultButton(loginButton);
    }

    private void initializeLayout() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Username:"), gbc);

        gbc.gridx = 1;
        panel.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Password:"), gbc);

        gbc.gridx = 1;
        panel.add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        createLoginButton();
        panel.add(loginButton, gbc);

        gbc.gridy = 3;
        statusLabel.setForeground(Color.RED);
        panel.add(statusLabel, gbc);

        add(panel);
    }

    private void createLoginButton() {
        loginButton.addActionListener(_ -> handleLogin());
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        char[] passwordChars = passwordField.getPassword();
        String password = new String(passwordChars);
        Arrays.fill(passwordChars, ' ');

        try {
            if (adminLogin.authenticate(username, password)) {
                statusLabel.setForeground(new Color(0, 128, 0));
                statusLabel.setText("Login successful. Active orders: " + adminLogin.getActiveOrderCount());
                LOGGER.log(Level.INFO, "User ''{0}'' logged in successfully.", username);
            } else {
                statusLabel.setForeground(Color.RED);
                statusLabel.setText("Invalid credentials");
                LOGGER.log(Level.WARNING, "Login failed for user ''{0}''.", username);
            }
        } catch (SQLException ex) {
            statusLabel.setForeground(Color.RED);
            statusLabel.setText("Connection error: " + ex.getMessage());
            LOGGER.log(Level.SEVERE, String.format("Database error during login for user '%s'.", username), ex);
        }
    }
}
