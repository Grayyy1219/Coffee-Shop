package Admin;

import coffeeshop.db.DatabaseAccess;
import coffeeshop.db.InMemoryDatabaseAccess;
import coffeeshop.db.MySqlDatabaseAccess;
import coffeeshop.order.OrderManager;
import coffeeshop.queue.OrderQueue;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
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
    private static final Color BACKGROUND_COLOR = new Color(246, 247, 249);
    private static final Color CARD_COLOR = new Color(255, 255, 255);
    private static final Color ACCENT_COLOR = new Color(55, 96, 146);
    private static final Color SUCCESS_COLOR = new Color(0, 128, 0);
    private static final Color ERROR_COLOR = new Color(176, 32, 32);
    private final JTextField usernameField = new JTextField(15);
    private final JPasswordField passwordField = new JPasswordField(15);
    private final JButton loginButton = new JButton("Sign In");
    private final JLabel statusLabel = new JLabel("Enter your credentials to access the management console.");

    AdminLoginFrame(AdminLogin adminLogin) {
        this.adminLogin = adminLogin;
        setTitle("Coffee Shop Admin Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        initializeLayout();
        pack();
        setLocationRelativeTo(null);
        getRootPane().setDefaultButton(loginButton);
        setResizable(false);
    }

    private void initializeLayout() {
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(BACKGROUND_COLOR);
        content.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        content.add(createHeaderPanel(), BorderLayout.NORTH);
        content.add(createFormPanel(), BorderLayout.CENTER);
        content.add(createStatusPanel(), BorderLayout.SOUTH);

        add(content);
    }

    private void createLoginButton() {
        loginButton.addActionListener(_ -> handleLogin());
        loginButton.setBackground(ACCENT_COLOR);
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setFont(loginButton.getFont().deriveFont(Font.BOLD, 13f));
        loginButton.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BACKGROUND_COLOR);

        JLabel title = new JLabel("Coffee Shop Admin Portal");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setForeground(ACCENT_COLOR);

        JLabel subtitle = new JLabel("Secure access for authorized personnel only.");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subtitle.setForeground(Color.DARK_GRAY);

        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.SOUTH);
        return header;
    }

    private JPanel createFormPanel() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(CARD_COLOR);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 223, 230)),
                BorderFactory.createEmptyBorder(16, 18, 16, 18)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JLabel usernameLabel = new JLabel("Username");
        usernameLabel.setFont(usernameLabel.getFont().deriveFont(Font.BOLD));
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(passwordLabel.getFont().deriveFont(Font.BOLD));

        gbc.gridx = 0;
        gbc.gridy = 0;
        card.add(usernameLabel, gbc);

        gbc.gridy = 1;
        styleInput(usernameField);
        card.add(usernameField, gbc);

        gbc.gridy = 2;
        card.add(passwordLabel, gbc);

        gbc.gridy = 3;
        styleInput(passwordField);
        card.add(passwordField, gbc);

        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.CENTER;
        createLoginButton();
        card.add(loginButton, gbc);

        return card;
    }

    private JPanel createStatusPanel() {
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBackground(BACKGROUND_COLOR);
        statusLabel.setForeground(Color.DARK_GRAY);
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.PLAIN, 12f));
        statusPanel.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
        statusPanel.add(statusLabel, BorderLayout.CENTER);
        return statusPanel;
    }

    private void styleInput(JTextField field) {
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 214, 220)),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        field.setFont(new Font("SansSerif", Font.PLAIN, 13));
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        char[] passwordChars = passwordField.getPassword();
        String password = new String(passwordChars);
        Arrays.fill(passwordChars, ' ');

        try {
            if (adminLogin.authenticate(username, password)) {
                statusLabel.setForeground(SUCCESS_COLOR);
                statusLabel.setText("Login successful. Active orders: " + adminLogin.getActiveOrderCount());
                LOGGER.log(Level.INFO, "User ''{0}'' logged in successfully.", username);
            } else {
                statusLabel.setForeground(ERROR_COLOR);
                statusLabel.setText("Invalid credentials. Please verify your username and password.");
                LOGGER.log(Level.WARNING, "Login failed for user ''{0}''.", username);
            }
        } catch (SQLException ex) {
            statusLabel.setForeground(ERROR_COLOR);
            statusLabel.setText("Connection error: " + ex.getMessage());
            LOGGER.log(Level.SEVERE, String.format("Database error during login for user '%s'.", username), ex);
        }
    }
}
