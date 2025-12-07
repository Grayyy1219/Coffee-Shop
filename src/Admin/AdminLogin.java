package Admin;

import coffeeshop.db.DatabaseAccess;
import coffeeshop.db.InMemoryDatabaseAccess;
import coffeeshop.db.MySqlDatabaseAccess;
import coffeeshop.model.Order;
import coffeeshop.order.OrderManager;
import coffeeshop.queue.OrderQueue;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.format.DateTimeFormatter;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

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

    public OrderManager getOrderManager() {
        return orderManager;
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
        return authenticateWithRole(username, password).isPresent();
    }

    public Optional<String> authenticateWithRole(String username, String password) throws SQLException {
        String trimmedUsername = username == null ? null : username.trim();
        String trimmedPassword = password == null ? null : password.trim();

        if (trimmedUsername == null || trimmedPassword == null || trimmedUsername.isBlank() || trimmedPassword.isBlank()) {
            LOGGER.log(Level.WARNING, "Login attempt rejected due to blank credentials. Username provided: {0}", username);
            return Optional.empty();
        }

        boolean valid = databaseAccess.validateUser(trimmedUsername, trimmedPassword);
        LOGGER.log(Level.INFO, "Login attempt for user ''{0}'' resulted in: {1}", new Object[]{trimmedUsername, valid ? "SUCCESS" : "INVALID"});
        if (!valid) {
            return Optional.empty();
        }

        String role = databaseAccess.getUserRole(trimmedUsername);
        if (role == null || role.isBlank()) {
            LOGGER.log(Level.INFO, "No explicit role found for user ''{0}''; defaulting to owner.", trimmedUsername);
            role = "owner";
        }
        return Optional.of(role);
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
            Optional<String> roleResult = adminLogin.authenticateWithRole(username, password);
            if (roleResult.isPresent()) {
                statusLabel.setForeground(SUCCESS_COLOR);
                statusLabel.setText("Login successful. Opening role-based workspace...");
                LOGGER.log(Level.INFO, "User ''{0}'' logged in successfully with role ''{1}''.", new Object[]{username, roleResult.get()});
                SwingUtilities.invokeLater(() -> {
                    RoleFlowFrame workspace = new RoleFlowFrame(adminLogin.getOrderManager(), roleResult.get());
                    workspace.setVisible(true);
                    dispose();
                });
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

class AdminDashboardFrame extends JFrame {

    private static final Color BACKGROUND_COLOR = new Color(246, 247, 249);
    private static final Color CARD_COLOR = new Color(255, 255, 255);
    private static final Color ACCENT_COLOR = new Color(55, 96, 146);
    private static final Color SUCCESS_COLOR = new Color(0, 128, 0);
    private static final Color ERROR_COLOR = new Color(176, 32, 32);
    private static final DateTimeFormatter ORDER_DATE_FORMAT = DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a");

    private final AdminLogin adminLogin;
    private final OrderManager orderManager;
    private final DefaultTableModel orderTableModel;
    private final JTable orderTable;
    private final JLabel statusLabel = new JLabel("Loading orders...");
    private final JLabel headerLabel = new JLabel("Order Management");

    AdminDashboardFrame(AdminLogin adminLogin) {
        this.adminLogin = adminLogin;
        this.orderManager = adminLogin.getOrderManager();
        this.orderTableModel = new DefaultTableModel(new Object[]{
            "Order ID", "Customer", "Items", "Total", "Status", "Paid", "Placed"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        this.orderTable = new JTable(orderTableModel);

        setTitle("Coffee Shop Admin Console");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        initializeLayout();
        setSize(900, 520);
        setLocationRelativeTo(null);
        refreshOrders();
    }

    private void initializeLayout() {
        JPanel content = new JPanel(new BorderLayout(14, 14));
        content.setBackground(BACKGROUND_COLOR);
        content.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        content.add(createHeaderPanel(), BorderLayout.NORTH);
        content.add(createTablePanel(), BorderLayout.CENTER);
        content.add(createFooterPanel(), BorderLayout.SOUTH);

        add(content);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BACKGROUND_COLOR);

        headerLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        headerLabel.setForeground(ACCENT_COLOR);

        JLabel subtitle = new JLabel("Track, serve, and settle orders in real time.");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subtitle.setForeground(Color.DARK_GRAY);

        JPanel actions = createActionBar();

        header.add(headerLabel, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.CENTER);
        header.add(actions, BorderLayout.SOUTH);
        return header;
    }

    private JPanel createActionBar() {
        JPanel actionBar = new JPanel();
        actionBar.setBackground(BACKGROUND_COLOR);
        actionBar.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

        JButton refreshButton = new JButton("Refresh Orders");
        refreshButton.addActionListener(_ -> refreshOrders());

        JButton processButton = new JButton("Process Next");
        processButton.addActionListener(_ -> processNextOrder());

        JButton markPaidButton = new JButton("Mark as Paid");
        markPaidButton.addActionListener(_ -> markSelectedOrderPaid());

        stylePrimaryButton(refreshButton);
        stylePrimaryButton(processButton);
        styleSecondaryButton(markPaidButton);

        actionBar.add(refreshButton);
        actionBar.add(processButton);
        actionBar.add(markPaidButton);

        return actionBar;
    }

    private JScrollPane createTablePanel() {
        orderTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        orderTable.setFillsViewportHeight(true);
        orderTable.setRowHeight(28);
        orderTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));

        JScrollPane scrollPane = new JScrollPane(orderTable);
        scrollPane.getViewport().setBackground(CARD_COLOR);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 223, 230)),
                BorderFactory.createEmptyBorder(6, 6, 6, 6)));

        return scrollPane;
    }

    private JPanel createFooterPanel() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(BACKGROUND_COLOR);
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        statusLabel.setForeground(Color.DARK_GRAY);
        footer.add(statusLabel, BorderLayout.WEST);
        return footer;
    }

    private void refreshOrders() {
        try {
            orderManager.loadActiveOrders();
            List<Order> orders = orderManager.getActiveOrders();
            updateTable(orders);
            updateStatus("Active orders: " + orders.size(), SUCCESS_COLOR);
        } catch (SQLException ex) {
            updateStatus("Unable to load orders: " + ex.getMessage(), ERROR_COLOR);
            Logger.getLogger(AdminDashboardFrame.class.getName()).log(Level.SEVERE, "Failed to load active orders", ex);
        }
    }

    private void processNextOrder() {
        try {
            Order servedOrder = orderManager.processNextOrder();
            updateTable(orderManager.getActiveOrders());
            updateStatus("Served order " + servedOrder.getOrderId() + " for " + servedOrder.getCustomerName(), SUCCESS_COLOR);
        } catch (IllegalStateException ex) {
            updateStatus(ex.getMessage(), ERROR_COLOR);
        } catch (SQLException ex) {
            updateStatus("Unable to process next order: " + ex.getMessage(), ERROR_COLOR);
            Logger.getLogger(AdminDashboardFrame.class.getName()).log(Level.SEVERE, "Failed to process next order", ex);
        }
    }

    private void markSelectedOrderPaid() {
        int selectedRow = getSelectedRow();
        if (selectedRow < 0) {
            updateStatus("Select an order to mark it as paid.", ERROR_COLOR);
            return;
        }

        String orderId = (String) orderTableModel.getValueAt(selectedRow, 0);
        Optional<Order> order = orderManager.findOrderById(orderId);
        if (order.isEmpty()) {
            updateStatus("Order no longer available.", ERROR_COLOR);
            return;
        }

        try {
            orderManager.recordPayment(order.get());
            updateTable(orderManager.getActiveOrders());
            updateStatus("Payment recorded for order " + orderId, SUCCESS_COLOR);
        } catch (SQLException ex) {
            updateStatus("Unable to record payment: " + ex.getMessage(), ERROR_COLOR);
            Logger.getLogger(AdminDashboardFrame.class.getName()).log(Level.SEVERE, "Failed to record payment", ex);
        }
    }

    private int getSelectedRow() {
        return orderTable.getSelectedRow();
    }

    private void updateTable(List<Order> orders) {
        orderTableModel.setRowCount(0);
        for (Order order : orders) {
            orderTableModel.addRow(new Object[]{
                order.getOrderId(),
                order.getCustomerName(),
                summarizeItems(order),
                String.format("$%.2f", order.getTotal()),
                order.getStatus(),
                order.isPaid() ? "Yes" : "No",
                ORDER_DATE_FORMAT.format(order.getCreatedAt())
            });
        }
    }

    private String summarizeItems(Order order) {
        String summary = order.getItems().stream()
                .map(item -> item.getQuantity() + "x " + item.getItem().getName())
                .collect(Collectors.joining(", "));
        return summary.isBlank() ? "No items" : summary;
    }

    private void updateStatus(String message, Color color) {
        statusLabel.setText(message);
        statusLabel.setForeground(color);
        headerLabel.setText("Order Management (" + orderManager.getActiveOrderCount() + " active)");
    }

    private void stylePrimaryButton(JButton button) {
        button.setBackground(ACCENT_COLOR);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(button.getFont().deriveFont(Font.BOLD, 12f));
        button.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
    }

    private void styleSecondaryButton(JButton button) {
        button.setBackground(new Color(238, 241, 245));
        button.setForeground(ACCENT_COLOR.darker());
        button.setFocusPainted(false);
        button.setFont(button.getFont().deriveFont(Font.BOLD, 12f));
        button.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
    }
}
