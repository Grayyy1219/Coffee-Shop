package app.ui;

import app.db.AssetService;
import app.db.DashboardDAO;
import app.db.MenuItemDAO;
import app.db.UserDAO;
import app.model.DailySalesRow;
import app.model.DashboardSummary;
import app.model.MenuItem;
import app.model.User;
import app.util.InsertionSort;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.NumberFormat;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class OwnerFrame extends JFrame {

    // Pages
    private static final String PAGE_DASHBOARD = "dashboard";
    private static final String PAGE_USERS = "users";
    private static final String PAGE_MENU = "menu";
    private static final String PAGE_CASHIER = "cashier";
    private static final String PAGE_BARISTA = "barista";
    private static final String PAGE_SETTINGS = "settings";

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel content = new JPanel(cardLayout);

    // DAOs
    private final UserDAO userDAO = new UserDAO();
    private final DashboardDAO dashboardDAO = new DashboardDAO();
    private final MenuItemDAO menuItemDAO = new MenuItemDAO();

    // USERS: table + form
    private DefaultTableModel usersModel;
    private JTable usersTable;
    private JTextField fId;
    private JTextField fUser;
    private JPasswordField fPass;
    private JComboBox<String> fRole;
    private JLabel usersHint;

    // MENU ITEMS: table + form
    private DefaultTableModel menuModel;
    private JTable menuTable;
    private JTextField fItemCode;
    private JTextField fItemName;
    private JTextField fItemCategory;
    private JTextField fItemPrice;
    private JTextField fItemImageUrl;
    private JLabel menuHint;
    private JTextField menuSearchField;
    private JComboBox<String> menuCategoryFilter;
    private final java.util.List<MenuItem> menuItemsCache = new java.util.ArrayList<>();
    private final Map<String, ImageIcon> menuImageCache = new HashMap<>();
    private static final int MENU_IMAGE_SIZE = 56;

    // DASHBOARD: metric labels + table
    private JLabel lblTodaySales;
    private JLabel lblOrdersQueue;
    private JLabel lblCompletedToday;
    private JLabel lblTotalUsers;
    private DefaultTableModel dailySalesModel;
    private JComboBox<String> salesSortMode;

    private final NumberFormat moneyPH = NumberFormat.getCurrencyInstance(new Locale("en", "PH"));

    // System settings inputs
    private JTextField fSystemName;
    private JLabel logoPreview;
    private byte[] selectedLogoBytes;
    private Color selectedAccent;
    private JLabel accentValue;
    private JPanel accentSwatch;

    private CashierPanel cashierPanel;
    private BaristaPanel baristaPanel;

    // Theme
    private static final Color BG = new Color(243, 245, 249);
    private static final Color SURFACE = Color.WHITE;
    private static final Color BORDER = new Color(226, 232, 240);
    private static final Color TEXT = new Color(30, 41, 59);
    private static final Color MUTED = new Color(100, 116, 139);
    private static final Color DANGER = new Color(220, 38, 38);
    private static final Color TABLE_HEADER_BG = new Color(15, 23, 42);
    private static final Color TABLE_HEADER_TEXT = new Color(226, 232, 240);
    private static final Color TABLE_ROW_ALT = new Color(248, 250, 252);
    private final AssetService assetService = new AssetService();
    private final Color primary = assetService.getAccentColorOrDefault();
    private final Color primaryDark = shade(primary, 0.2);
    private final Color primarySoft = tint(primary, 0.86);
    private final Color tableSelection = tint(primary, 0.72);
    private final String ownerUsername;

    public OwnerFrame(String ownerUsername) {
        this.ownerUsername = ownerUsername;
        setTitle("Owner Panel");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        setContentPane(buildUI(ownerUsername));

        // Load data after UI mounts
        SwingUtilities.invokeLater(() -> {
            refreshDashboardSafe();
            refreshUsersTableSafe();
            refreshMenuTableSafe();
        });
    }

    private JComponent buildUI(String ownerUsername) {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);

        root.add(buildTopBar(ownerUsername), BorderLayout.NORTH);
        root.add(buildSidebar(), BorderLayout.WEST);
        root.add(buildMainArea(), BorderLayout.CENTER);

        return root;
    }

    // -------------------- Top Bar --------------------

    private JComponent buildTopBar(String ownerUsername) {
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(SURFACE);
        top.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
                new EmptyBorder(14, 18, 14, 18)
        ));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Owner Panel");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setForeground(TEXT);

        JLabel subtitle = new JLabel("Dashboard overview, user management, and role previews.");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subtitle.setForeground(MUTED);

        left.add(title);
        left.add(Box.createVerticalStrut(2));
        left.add(subtitle);

        JLabel user = new JLabel("Logged in as: " + ownerUsername);
        user.setFont(new Font("SansSerif", Font.PLAIN, 12));
        user.setForeground(MUTED);

        top.add(left, BorderLayout.WEST);
        JButton btnLogout = ghost("Logout");
        btnLogout.addActionListener(e -> doLogout());

        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.X_AXIS));
        right.add(user);
        right.add(Box.createHorizontalStrut(12));
        right.add(btnLogout);

        top.add(right, BorderLayout.EAST);

        return top;
    }

    // -------------------- Sidebar --------------------

    private JComponent buildSidebar() {
        JPanel side = new JPanel();
        side.setBackground(SURFACE);
        side.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER));
        side.setPreferredSize(new Dimension(270, 10));
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));

        side.add(Box.createVerticalStrut(16));
        side.add(sectionLabel("MAIN"));

        NavItem dash = new NavItem("Dashboard", UIManager.getIcon("OptionPane.informationIcon"), primary, primarySoft, primaryDark);
        NavItem users = new NavItem("User Management", UIManager.getIcon("FileView.directoryIcon"), primary, primarySoft, primaryDark);
        NavItem menu = new NavItem("Products", UIManager.getIcon("FileView.fileIcon"), primary, primarySoft, primaryDark);
        NavItem settings = new NavItem("System Settings", UIManager.getIcon("FileView.computerIcon"), primary, primarySoft, primaryDark);

        dash.addActionListener(e -> selectPage(PAGE_DASHBOARD, dash, dash, users, menu, settings, null, null));
        users.addActionListener(e -> selectPage(PAGE_USERS, users, dash, users, menu, settings, null, null));
        menu.addActionListener(e -> selectPage(PAGE_MENU, menu, dash, users, menu, settings, null, null));
        settings.addActionListener(e -> selectPage(PAGE_SETTINGS, settings, dash, users, menu, settings, null, null));

        side.add(dash);
        side.add(Box.createVerticalStrut(8));
        side.add(users);
        side.add(Box.createVerticalStrut(8));
        side.add(menu);
        side.add(Box.createVerticalStrut(8));
        side.add(settings);

        side.add(Box.createVerticalStrut(18));
        side.add(sectionLabel("ROLE PREVIEWS"));

        NavItem cashier = new NavItem("Cashier View", UIManager.getIcon("FileView.fileIcon"), primary, primarySoft, primaryDark);
        NavItem barista = new NavItem("Barista View", UIManager.getIcon("FileView.fileIcon"), primary, primarySoft, primaryDark);

        cashier.addActionListener(e -> selectPage(PAGE_CASHIER, cashier, dash, users, menu, settings, cashier, barista));
        barista.addActionListener(e -> selectPage(PAGE_BARISTA, barista, dash, users, menu, settings, cashier, barista));

        side.add(cashier);
        side.add(Box.createVerticalStrut(8));
        side.add(barista);

        side.add(Box.createVerticalGlue());

        // Default selection
        selectPage(PAGE_DASHBOARD, dash, dash, users, menu, settings, cashier, barista);

        return side;
    }

    private void selectPage(String page, NavItem selected, NavItem... items) {
        for (NavItem item : items) {
            if (item != null) item.setSelected(item == selected);
        }
        selected.setSelected(true);

        cardLayout.show(content, page);

        // IMPORTANT: Only refresh if dashboard components already exist
        if (PAGE_DASHBOARD.equals(page)) {
            if (lblTodaySales != null) refreshDashboardSafe();
        }
        if (PAGE_USERS.equals(page)) {
            if (usersModel != null) refreshUsersTableSafe();
        }
        if (PAGE_MENU.equals(page)) {
            if (menuModel != null) refreshMenuTableSafe();
        }
        if (PAGE_CASHIER.equals(page)) {
            if (cashierPanel != null) cashierPanel.refreshData();
        }
        if (PAGE_BARISTA.equals(page)) {
            if (baristaPanel != null) baristaPanel.refreshData();
        }
    }

    private JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 11));
        l.setForeground(MUTED);
        l.setBorder(new EmptyBorder(0, 18, 10, 18));
        return l;
    }

    // -------------------- Main Area --------------------

    private JComponent buildMainArea() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(BG);
        wrapper.setBorder(new EmptyBorder(18, 18, 18, 18));

        content.setBackground(BG);
        content.add(buildDashboardPage(), PAGE_DASHBOARD);
        content.add(buildUsersPage(), PAGE_USERS);
        content.add(buildMenuItemsPage(), PAGE_MENU);
        content.add(buildSystemPage(), PAGE_SETTINGS);
        content.add(buildCashierPage(), PAGE_CASHIER);
        content.add(buildBaristaPage(), PAGE_BARISTA);

        wrapper.add(content, BorderLayout.CENTER);
        return wrapper;
    }

    // -------------------- DASHBOARD (DB connected) --------------------

    private JComponent buildDashboardPage() {
        JPanel page = new JPanel(new BorderLayout(14, 14));
        page.setOpaque(false);

        JPanel header = pageHeader("Dashboard", "Live overview from database.");
        JButton btnReload = ghost("Reload");
        btnReload.addActionListener(e -> refreshDashboardSafe());

        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);
        headerRow.add(header, BorderLayout.WEST);
        headerRow.add(btnReload, BorderLayout.EAST);

        page.add(headerRow, BorderLayout.NORTH);

        // metric cards
        JPanel cards = new JPanel(new GridLayout(1, 4, 12, 12));
        cards.setOpaque(false);

        lblTodaySales = metricValueLabel("₱ 0.00");
        lblOrdersQueue = metricValueLabel("0");
        lblCompletedToday = metricValueLabel("0");
        lblTotalUsers = metricValueLabel("0");

        cards.add(metricCardWithValue("Today Sales (Paid)", lblTodaySales, "Sum of paid orders today"));
        cards.add(metricCardWithValue("Orders In Queue", lblOrdersQueue, "PENDING / IN_PROGRESS"));
        cards.add(metricCardWithValue("Completed Today", lblCompletedToday, "Status = COMPLETED today"));
        cards.add(metricCardWithValue("Total Users", lblTotalUsers, "All accounts in users table"));

        // recent daily sales table
        JPanel tableBox = surfacePanel(new EmptyBorder(14, 14, 14, 14));
        tableBox.setLayout(new BorderLayout(10, 10));

        JLabel t = new JLabel("Recent Daily Sales");
        t.setFont(new Font("SansSerif", Font.BOLD, 14));
        t.setForeground(TEXT);

        salesSortMode = new JComboBox<>(new String[]{"Date (Newest)", "Total (High to Low)"});
        styleField(salesSortMode);
        salesSortMode.setPreferredSize(new Dimension(220, 50));

        salesSortMode.setMaximumSize(new Dimension(220, 50));
        salesSortMode.addActionListener(e -> refreshDashboardSafe());

        JPanel salesHeader = new JPanel(new BorderLayout());
        salesHeader.setOpaque(false);
        salesHeader.add(t, BorderLayout.WEST);
        salesHeader.add(salesSortMode, BorderLayout.EAST);

        dailySalesModel = new DefaultTableModel(new String[]{"Date", "Gross", "Paid", "Orders"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = new JTable(dailySalesModel);
        table.setRowHeight(28);
        styleTable(table);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(BORDER, 1));

        tableBox.add(salesHeader, BorderLayout.NORTH);
        tableBox.add(sp, BorderLayout.CENTER);

        JPanel center = new JPanel(new BorderLayout(12, 12));
        center.setOpaque(false);
        center.add(cards, BorderLayout.NORTH);
        center.add(tableBox, BorderLayout.CENTER);

        page.add(center, BorderLayout.CENTER);
        return page;
    }

    private void refreshDashboardSafe() {
        try {
            refreshDashboard();
        } catch (Exception ex) {
            showDbError(ex);
        }
    }

    private void refreshDashboard() throws Exception {
        DashboardSummary s = dashboardDAO.loadSummary();

        // Show paid total as “sales”
        lblTodaySales.setText(moneyPH.format(s.todayPaid));
        lblOrdersQueue.setText(String.valueOf(s.ordersInQueue));
        lblCompletedToday.setText(String.valueOf(s.completedToday));
        lblTotalUsers.setText(String.valueOf(s.totalUsers));

        if (dailySalesModel != null) {
            dailySalesModel.setRowCount(0);
            List<DailySalesRow> rows = dashboardDAO.loadRecentDailySales(14);
            if (salesSortMode != null) {
                String mode = String.valueOf(salesSortMode.getSelectedItem());
                Comparator<DailySalesRow> comparator;
                if ("Total (High to Low)".equals(mode)) {
                    comparator = (a, b) -> b.paidTotal.compareTo(a.paidTotal);
                } else {
                    comparator = (a, b) -> b.saleDate.compareTo(a.saleDate);
                }
                // Insertion sort triggered when owner views sorted sales rows (dashboard).
                // The DB rows are loaded into a List first, then sorted in-place.
                InsertionSort.sort(rows, comparator);
            }
            for (DailySalesRow r : rows) {
                dailySalesModel.addRow(new Object[]{
                        r.saleDate,
                        moneyPH.format(r.grossTotal),
                        moneyPH.format(r.paidTotal),
                        r.orderCount
                });
            }
        }
    }

    // -------------------- USERS (DB connected) --------------------

    private JComponent buildUsersPage() {
        JPanel page = new JPanel(new BorderLayout(14, 14));
        page.setOpaque(false);

        JPanel header = pageHeader("User Management", "Create, update, and delete staff accounts.");

        JButton btnRefresh = ghost("Refresh");
        btnRefresh.addActionListener(e -> refreshUsersTableSafe());

        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);
        headerRow.add(header, BorderLayout.WEST);
        headerRow.add(btnRefresh, BorderLayout.EAST);

        page.add(headerRow, BorderLayout.NORTH);

        // Left: table surface
        JPanel left = surfacePanel(new EmptyBorder(14, 14, 14, 14));
        left.setLayout(new BorderLayout(10, 10));

        usersHint = new JLabel("Tip: Select a user to edit. Use New to clear the form.");
        usersHint.setFont(new Font("SansSerif", Font.PLAIN, 12));
        usersHint.setForeground(MUTED);

        usersModel = new DefaultTableModel(new String[]{"ID", "Username", "Role", "Locked"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        usersTable = new JTable(usersModel);
        usersTable.setRowHeight(30);
        styleTable(usersTable);
        usersTable.setDefaultRenderer(Object.class, new LockedUserRenderer());

        JScrollPane sp = new JScrollPane(usersTable);
        sp.setBorder(BorderFactory.createLineBorder(BORDER, 1));

        left.add(usersHint, BorderLayout.NORTH);
        left.add(sp, BorderLayout.CENTER);

        // Right: form surface
        JPanel right = surfacePanel(new EmptyBorder(14, 14, 14, 14));
        right.setLayout(new GridBagLayout());
        right.setPreferredSize(new Dimension(420, 10));

        fId = new JTextField();
        fId.setEditable(false);
        fUser = new JTextField();
        fPass = new JPasswordField();

        // IMPORTANT: These MUST match the exact values stored in your DB users.role
        // If your DB uses OWNER/CASHIER/BARISTA, change these strings.
        fRole = new JComboBox<>(new String[]{"owner", "Cashier", "Barista"});

        styleField(fId);
        styleField(fUser);
        styleField(fPass);
        styleField(fRole);

        JButton btnNew = ghost("New");
        JButton btnAdd = primary("Add User");
        JButton btnUpdate = primaryOutline("Update");
        JButton btnDelete = danger("Delete");
        JButton btnUnlock = ghost("Unlock");

        btnNew.addActionListener(e -> clearUserForm());
        btnAdd.addActionListener(e -> onAddUser());
        btnUpdate.addActionListener(e -> onUpdateUser());
        btnDelete.addActionListener(e -> onDeleteUser());
        btnUnlock.addActionListener(e -> onUnlockUser());

        usersTable.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int row = usersTable.getSelectedRow();
            if (row < 0) return;

            int id = Integer.parseInt(usersModel.getValueAt(row, 0).toString());
            loadUserIntoFormSafe(id);
        });

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.NORTHWEST;

        JLabel formTitle = new JLabel("Create / Edit User");
        formTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        formTitle.setForeground(TEXT);

        g.gridy = 0; g.insets = new Insets(0, 0, 12, 0);
        right.add(formTitle, g);

        g.gridy = 1; g.insets = new Insets(0, 0, 6, 0);
        right.add(fieldLabel("ID"), g);
        g.gridy = 2; g.insets = new Insets(0, 0, 12, 0);
        right.add(fId, g);

        g.gridy = 3; g.insets = new Insets(0, 0, 6, 0);
        right.add(fieldLabel("Username"), g);
        g.gridy = 4; g.insets = new Insets(0, 0, 12, 0);
        right.add(fUser, g);

        g.gridy = 5; g.insets = new Insets(0, 0, 6, 0);
        right.add(fieldLabel("Password"), g);
        g.gridy = 6; g.insets = new Insets(0, 0, 12, 0);
        right.add(fPass, g);

        g.gridy = 7; g.insets = new Insets(0, 0, 6, 0);
        right.add(fieldLabel("Role"), g);
        g.gridy = 8; g.insets = new Insets(0, 0, 14, 0);
        right.add(fRole, g);

        JPanel actions = new JPanel(new GridLayout(1, 5, 10, 10));
        actions.setOpaque(false);
        actions.add(btnNew);
        actions.add(btnAdd);
        actions.add(btnUpdate);
        actions.add(btnDelete);
        actions.add(btnUnlock);

        g.gridy = 9; g.insets = new Insets(0, 0, 0, 0);
        right.add(actions, g);

        g.gridy = 10; g.weighty = 1;
        right.add(Box.createVerticalGlue(), g);

        JPanel center = new JPanel(new BorderLayout(12, 12));
        center.setOpaque(false);
        center.add(left, BorderLayout.CENTER);
        center.add(right, BorderLayout.EAST);

        page.add(center, BorderLayout.CENTER);
        return page;
    }

    private void refreshUsersTableSafe() {
        try {
            refreshUsersTable();
            if (usersHint != null) usersHint.setText("Loaded " + usersModel.getRowCount() + " users from database.");
        } catch (Exception ex) {
            showDbError(ex);
        }
    }

    private void refreshUsersTable() throws Exception {
        if (usersModel == null) return;

        usersModel.setRowCount(0);
        for (User u : userDAO.findAll()) {
            usersModel.addRow(new Object[]{u.getId(), u.getUsername(), u.getRole(), u.isLocked() ? "Yes" : "No"});
        }
    }

    private void loadUserIntoFormSafe(int id) {
        try {
            User u = userDAO.findById(id);
            if (u == null) return;

            fId.setText(String.valueOf(u.getId()));
            fUser.setText(u.getUsername());
            fPass.setText(u.getPassword()); // plain text (matches current AuthService style)
            fRole.setSelectedItem(u.getRole());
        } catch (Exception ex) {
            showDbError(ex);
        }
    }

    private void clearUserForm() {
        if (usersTable != null) usersTable.clearSelection();
        fId.setText("");
        fUser.setText("");
        fPass.setText("");
        if (fRole != null) fRole.setSelectedIndex(0);
        if (usersHint != null) usersHint.setText("Tip: Select a user to edit. Use New to clear the form.");
    }

    private void onAddUser() {
        try {
            String username = fUser.getText().trim();
            String password = new String(fPass.getPassword());
            String role = String.valueOf(fRole.getSelectedItem());

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Username and password are required.");
                return;
            }
            if (userDAO.usernameExists(username, null)) {
                JOptionPane.showMessageDialog(this, "Username already exists.");
                return;
            }

            int newId = userDAO.insert(new User(0, username, password, role));
            refreshUsersTable();
            selectRowById(newId);
            JOptionPane.showMessageDialog(this, "User added.");
        } catch (Exception ex) {
            showDbError(ex);
        }
    }

    private void onUpdateUser() {
        try {
            if (fId.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Select a user to update.");
                return;
            }

            int id = Integer.parseInt(fId.getText().trim());
            String username = fUser.getText().trim();
            String password = new String(fPass.getPassword());
            String role = String.valueOf(fRole.getSelectedItem());

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Username and password are required.");
                return;
            }
            if (userDAO.usernameExists(username, id)) {
                JOptionPane.showMessageDialog(this, "Username already exists.");
                return;
            }

            boolean ok = userDAO.update(new User(id, username, password, role));
            if (!ok) {
                JOptionPane.showMessageDialog(this, "Update failed (user not found).");
                return;
            }

            refreshUsersTable();
            selectRowById(id);
            JOptionPane.showMessageDialog(this, "User updated.");
        } catch (Exception ex) {
            showDbError(ex);
        }
    }

    private void onDeleteUser() {
        try {
            if (fId.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Select a user to delete.");
                return;
            }

            int id = Integer.parseInt(fId.getText().trim());
            int confirm = JOptionPane.showConfirmDialog(this, "Delete this user?", "Confirm Delete",
                    JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;

            boolean ok = userDAO.delete(id);
            if (!ok) {
                JOptionPane.showMessageDialog(this, "Delete failed (user not found).");
                return;
            }

            refreshUsersTable();
            clearUserForm();
            JOptionPane.showMessageDialog(this, "User deleted.");
        } catch (Exception ex) {
            showDbError(ex);
        }
    }

    private void onUnlockUser() {
        try {
            if (fId.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Select a user to unlock.");
                return;
            }

            int id = Integer.parseInt(fId.getText().trim());
            User u = userDAO.findById(id);
            if (u == null) {
                JOptionPane.showMessageDialog(this, "User not found.");
                return;
            }
            if (!u.isLocked()) {
                JOptionPane.showMessageDialog(this, "User is not locked.");
                return;
            }

            userDAO.updateSecurity(id, false, 0);
            refreshUsersTable();
            selectRowById(id);
            JOptionPane.showMessageDialog(this, "User unlocked.");
        } catch (Exception ex) {
            showDbError(ex);
        }
    }

    private void selectRowById(int id) {
        if (usersModel == null || usersTable == null) return;

        for (int r = 0; r < usersModel.getRowCount(); r++) {
            int rowId = Integer.parseInt(usersModel.getValueAt(r, 0).toString());
            if (rowId == id) {
                usersTable.setRowSelectionInterval(r, r);
                usersTable.scrollRectToVisible(usersTable.getCellRect(r, 0, true));
                return;
            }
        }
    }

    private void doLogout() {
        int confirm = JOptionPane.showConfirmDialog(this, "Log out now?", "Confirm Logout",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        new LoginFrame().setVisible(true);
        dispose();
    }

    // -------------------- MENU ITEMS (DB connected) --------------------

    private JComponent buildMenuItemsPage() {
        JPanel page = new JPanel(new BorderLayout(14, 14));
        page.setOpaque(false);

        JPanel header = pageHeader("Products", "Manage menu items, categories, and pricing.");

        JButton btnRefresh = ghost("Refresh");
        btnRefresh.addActionListener(e -> refreshMenuTableSafe());

        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);
        headerRow.add(header, BorderLayout.WEST);
        headerRow.add(btnRefresh, BorderLayout.EAST);

        page.add(headerRow, BorderLayout.NORTH);

        JPanel left = surfacePanel(new EmptyBorder(14, 14, 14, 14));
        left.setLayout(new BorderLayout(10, 10));

        menuHint = new JLabel("Tip: Select an item to edit. Use New to clear the form.");
        menuHint.setFont(new Font("SansSerif", Font.PLAIN, 12));
        menuHint.setForeground(MUTED);

        menuSearchField = new JTextField();
        menuSearchField.setToolTipText("Search by code or name");
        styleField(menuSearchField);

        menuCategoryFilter = new JComboBox<>(new String[]{"All"});
        styleField(menuCategoryFilter);

        menuSearchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { applyMenuFilter(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { applyMenuFilter(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { applyMenuFilter(); }
        });
        menuCategoryFilter.addActionListener(e -> applyMenuFilter());

        JPanel filterRow = new JPanel(new GridLayout(1, 2, 8, 0));
        filterRow.setOpaque(false);
        filterRow.add(labeled("Search", menuSearchField));
        filterRow.add(labeled("Category", menuCategoryFilter));

        menuModel = new DefaultTableModel(new String[]{"Code", "Name", "Category", "Price", "Image"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        menuTable = new JTable(menuModel);
        menuTable.setRowHeight(30);
        menuTable.setRowHeight(MENU_IMAGE_SIZE + 16);
        styleTable(menuTable);
        menuTable.getColumnModel().getColumn(4).setCellRenderer(new MenuImageCellRenderer());
        menuTable.getColumnModel().getColumn(4).setPreferredWidth(MENU_IMAGE_SIZE + 24);

        JScrollPane sp = new JScrollPane(menuTable);
        sp.setBorder(BorderFactory.createLineBorder(BORDER, 1));

        JPanel leftHeader = new JPanel();
        leftHeader.setOpaque(false);
        leftHeader.setLayout(new BoxLayout(leftHeader, BoxLayout.Y_AXIS));
        leftHeader.add(menuHint);
        leftHeader.add(Box.createVerticalStrut(8));
        leftHeader.add(filterRow);

        left.add(leftHeader, BorderLayout.NORTH);
        left.add(sp, BorderLayout.CENTER);

        JPanel right = surfacePanel(new EmptyBorder(14, 14, 14, 14));
        right.setLayout(new GridBagLayout());
        right.setPreferredSize(new Dimension(420, 10));

        fItemCode = new JTextField();
        fItemName = new JTextField();
        fItemCategory = new JTextField();
        fItemPrice = new JTextField();
        fItemImageUrl = new JTextField();

        styleField(fItemCode);
        styleField(fItemName);
        styleField(fItemCategory);
        styleField(fItemPrice);
        styleField(fItemImageUrl);

        JButton btnUploadImage = ghost("Upload");
        btnUploadImage.addActionListener(e -> chooseMenuImage());

        JButton btnNew = ghost("New");
        JButton btnAdd = primary("Add Item");
        JButton btnUpdate = primaryOutline("Update");
        JButton btnDelete = danger("Delete");

        btnNew.addActionListener(e -> clearMenuForm());
        btnAdd.addActionListener(e -> onAddMenuItem());
        btnUpdate.addActionListener(e -> onUpdateMenuItem());
        btnDelete.addActionListener(e -> onDeleteMenuItem());

        menuTable.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int row = menuTable.getSelectedRow();
            if (row < 0) return;
            String code = menuModel.getValueAt(row, 0).toString();
            loadMenuItemIntoFormSafe(code);
        });

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.NORTHWEST;

        JLabel formTitle = new JLabel("Create / Edit Item");
        formTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        formTitle.setForeground(TEXT);

        g.gridy = 0; g.insets = new Insets(0, 0, 12, 0);
        right.add(formTitle, g);

        g.gridy = 1; g.insets = new Insets(0, 0, 6, 0);
        right.add(fieldLabel("Code"), g);
        g.gridy = 2; g.insets = new Insets(0, 0, 12, 0);
        right.add(fItemCode, g);

        g.gridy = 3; g.insets = new Insets(0, 0, 6, 0);
        right.add(fieldLabel("Name"), g);
        g.gridy = 4; g.insets = new Insets(0, 0, 12, 0);
        right.add(fItemName, g);

        g.gridy = 5; g.insets = new Insets(0, 0, 6, 0);
        right.add(fieldLabel("Category"), g);
        g.gridy = 6; g.insets = new Insets(0, 0, 12, 0);
        right.add(fItemCategory, g);

        g.gridy = 7; g.insets = new Insets(0, 0, 6, 0);
        right.add(fieldLabel("Price"), g);
        g.gridy = 8; g.insets = new Insets(0, 0, 14, 0);
        right.add(fItemPrice, g);

        g.gridy = 9; g.insets = new Insets(0, 0, 6, 0);
        right.add(fieldLabel("Image URL / Path"), g);
        g.gridy = 10; g.insets = new Insets(0, 0, 14, 0);
        JPanel imageRow = new JPanel(new BorderLayout(8, 0));
        imageRow.setOpaque(false);
        imageRow.add(fItemImageUrl, BorderLayout.CENTER);
        imageRow.add(btnUploadImage, BorderLayout.EAST);
        right.add(imageRow, g);

        JPanel actions = new JPanel(new GridLayout(1, 4, 10, 10));
        actions.setOpaque(false);
        actions.add(btnNew);
        actions.add(btnAdd);
        actions.add(btnUpdate);
        actions.add(btnDelete);

        g.gridy = 11; g.insets = new Insets(0, 0, 0, 0);
        right.add(actions, g);

        g.gridy = 12; g.weighty = 1;
        right.add(Box.createVerticalGlue(), g);

        JPanel center = new JPanel(new BorderLayout(12, 12));
        center.setOpaque(false);
        center.add(left, BorderLayout.CENTER);
        center.add(right, BorderLayout.EAST);

        page.add(center, BorderLayout.CENTER);
        return page;
    }

    private void chooseMenuImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choose product image");
        chooser.setFileFilter(new FileNameExtensionFilter("Image files", "png", "jpg", "jpeg", "gif", "webp"));
        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;
        File selected = chooser.getSelectedFile();
        if (selected != null) {
            fItemImageUrl.setText(selected.getAbsolutePath());
        }
    }

    private void refreshMenuTableSafe() {
        try {
            refreshMenuTable();
            if (menuHint != null) menuHint.setText("Loaded " + menuModel.getRowCount() + " items from database.");
        } catch (Exception ex) {
            showDbError(ex);
        }
    }

    private void refreshMenuTable() throws Exception {
        if (menuModel == null) return;
        menuItemsCache.clear();
        menuImageCache.clear();
        menuItemsCache.addAll(menuItemDAO.findAll());
        updateMenuCategoryOptions();
        applyMenuFilter();
    }

    private void loadMenuItemIntoFormSafe(String code) {
        try {
            MenuItem item = menuItemDAO.findByCode(code);
            if (item == null) return;
            fItemCode.setText(item.getCode());
            fItemName.setText(item.getName());
            fItemCategory.setText(item.getCategory());
            fItemPrice.setText(item.getPrice().toPlainString());
            fItemImageUrl.setText(item.getImageUrl() == null ? "" : item.getImageUrl());
        } catch (Exception ex) {
            showDbError(ex);
        }
    }

    private void clearMenuForm() {
        if (menuTable != null) menuTable.clearSelection();
        fItemCode.setText("");
        fItemName.setText("");
        fItemCategory.setText("");
        fItemPrice.setText("");
        fItemImageUrl.setText("");
        if (menuHint != null) menuHint.setText("Tip: Select an item to edit. Use New to clear the form.");
    }

    private void updateMenuCategoryOptions() {
        if (menuCategoryFilter == null) return;
        String selected = menuCategoryFilter.getSelectedItem() == null ? "All"
                : menuCategoryFilter.getSelectedItem().toString();
        java.util.Set<String> categories = new java.util.TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (MenuItem item : menuItemsCache) {
            categories.add(item.getCategory());
        }
        menuCategoryFilter.removeAllItems();
        menuCategoryFilter.addItem("All");
        for (String category : categories) {
            menuCategoryFilter.addItem(category);
        }
        menuCategoryFilter.setSelectedItem(categories.contains(selected) ? selected : "All");
    }

    private void applyMenuFilter() {
        if (menuModel == null) return;
        String query = menuSearchField == null ? "" : menuSearchField.getText().trim().toLowerCase(Locale.ROOT);
        String category = menuCategoryFilter == null || menuCategoryFilter.getSelectedItem() == null
                ? "All"
                : menuCategoryFilter.getSelectedItem().toString();

        menuModel.setRowCount(0);
        for (MenuItem item : menuItemsCache) {
            boolean matchesQuery = query.isEmpty()
                    || item.getCode().toLowerCase(Locale.ROOT).contains(query)
                    || item.getName().toLowerCase(Locale.ROOT).contains(query);
            boolean matchesCategory = "All".equalsIgnoreCase(category)
                    || item.getCategory().equalsIgnoreCase(category);
            if (!matchesQuery || !matchesCategory) continue;
            menuModel.addRow(new Object[]{
                    item.getCode(),
                    item.getName(),
                    item.getCategory(),
                    moneyPH.format(item.getPrice()),
                    item.getImageUrl()
            });
        }
    }

    private void onAddMenuItem() {
        try {
            MenuItem item = buildMenuItemFromForm();
            if (item == null) return;
            int inserted = menuItemDAO.insert(item);
            if (inserted == 0) {
                JOptionPane.showMessageDialog(this, "Add failed.");
                return;
            }
            refreshMenuTable();
            selectRowByCode(item.getCode());
            JOptionPane.showMessageDialog(this, "Item added.");
        } catch (Exception ex) {
            showDbError(ex);
        }
    }

    private void onUpdateMenuItem() {
        try {
            if (fItemCode.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Select an item to update.");
                return;
            }
            MenuItem item = buildMenuItemFromForm();
            if (item == null) return;
            int updated = menuItemDAO.update(item);
            if (updated == 0) {
                JOptionPane.showMessageDialog(this, "Update failed (item not found).");
                return;
            }
            refreshMenuTable();
            selectRowByCode(item.getCode());
            JOptionPane.showMessageDialog(this, "Item updated.");
        } catch (Exception ex) {
            showDbError(ex);
        }
    }

    private void onDeleteMenuItem() {
        try {
            String code = fItemCode.getText().trim();
            if (code.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Select an item to delete.");
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(this, "Delete this menu item?", "Confirm Delete",
                    JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;
            int deleted = menuItemDAO.deleteByCode(code);
            if (deleted == 0) {
                JOptionPane.showMessageDialog(this, "Delete failed (item not found).");
                return;
            }
            refreshMenuTable();
            clearMenuForm();
            JOptionPane.showMessageDialog(this, "Item deleted.");
        } catch (Exception ex) {
            showDbError(ex);
        }
    }

    private MenuItem buildMenuItemFromForm() {
        String code = fItemCode.getText().trim();
        String name = fItemName.getText().trim();
        String category = fItemCategory.getText().trim();
        String priceText = fItemPrice.getText().trim();
        String imageUrl = fItemImageUrl.getText().trim();

        if (code.isEmpty() || name.isEmpty() || category.isEmpty() || priceText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required.");
            return null;
        }
        BigDecimal price;
        try {
            price = new BigDecimal(priceText);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Price must be a valid number.");
            return null;
        }
        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            JOptionPane.showMessageDialog(this, "Price must be greater than zero.");
            return null;
        }
        String storedImage = null;
        if (!imageUrl.isEmpty()) {
            try {
                storedImage = storeMenuImage(imageUrl, code);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Unable to save image locally:\n" + ex.getMessage());
                return null;
            }
        }
        return new MenuItem(code, name, category, price, storedImage);
    }

    private void selectRowByCode(String code) {
        if (menuModel == null || menuTable == null) return;
        for (int r = 0; r < menuModel.getRowCount(); r++) {
            String rowCode = menuModel.getValueAt(r, 0).toString();
            if (rowCode.equalsIgnoreCase(code)) {
                menuTable.setRowSelectionInterval(r, r);
                menuTable.scrollRectToVisible(menuTable.getCellRect(r, 0, true));
                return;
            }
        }
    }

    private String storeMenuImage(String source, String code) throws IOException {
        String trimmed = source.trim();
        if (trimmed.isEmpty()) return null;
        Path imagesDir = Paths.get("assets", "menu-images");
        Files.createDirectories(imagesDir);
        String ext = extractImageExtension(trimmed);
        String safeCode = code.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-");
        Path target = imagesDir.resolve(safeCode + ext);
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            URL url = new URL(trimmed);
            try (InputStream stream = url.openStream()) {
                Files.copy(stream, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } else {
            Path sourcePath = Paths.get(trimmed).normalize();
            if (sourcePath.equals(target.normalize())) {
                return imagesDir.resolve(target.getFileName()).toString().replace('\\', '/');
            }
            if (!Files.exists(sourcePath)) {
                throw new IOException("File not found: " + trimmed);
            }
            Files.copy(sourcePath, target, StandardCopyOption.REPLACE_EXISTING);
        }
        return imagesDir.resolve(target.getFileName()).toString().replace('\\', '/');
    }

    private String extractImageExtension(String source) {
        String lower = source.toLowerCase(Locale.ROOT);
        int dot = lower.lastIndexOf('.');
        if (dot > -1) {
            String ext = lower.substring(dot);
            if (ext.matches("\\.(png|jpg|jpeg|gif|webp)")) {
                return ext;
            }
        }
        return ".png";
    }

    private ImageIcon loadMenuImage(String path) {
        if (path == null || path.isBlank()) {
            return null;
        }
        String key = path.trim();
        if (menuImageCache.containsKey(key)) {
            return menuImageCache.get(key);
        }
        ImageIcon icon;
        try {
            if (key.startsWith("http://") || key.startsWith("https://")) {
                icon = new ImageIcon(new URL(key));
            } else {
                icon = new ImageIcon(key);
            }
            if (icon.getIconWidth() > 0) {
                Image scaled = icon.getImage().getScaledInstance(MENU_IMAGE_SIZE, MENU_IMAGE_SIZE, Image.SCALE_SMOOTH);
                icon = new ImageIcon(scaled);
            } else {
                icon = null;
            }
        } catch (Exception ex) {
            icon = null;
        }
        menuImageCache.put(key, icon);
        return icon;
    }

    private class MenuImageCellRenderer extends JLabel implements TableCellRenderer {
        MenuImageCellRenderer() {
            setHorizontalAlignment(SwingConstants.CENTER);
            setVerticalAlignment(SwingConstants.CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            setText(null);
            setIcon(loadMenuImage(value == null ? null : value.toString()));
            if (isSelected) {
                setBackground(tableSelection);
                setForeground(TEXT);
            } else {
                setBackground(row % 2 == 0 ? SURFACE : TABLE_ROW_ALT);
                setForeground(TEXT);
            }
            setOpaque(true);
            setBorder(new EmptyBorder(6, 10, 6, 10));
            return this;
        }
    }

    // -------------------- SYSTEM SETTINGS --------------------

    private JComponent buildSystemPage() {
        JPanel page = new JPanel(new BorderLayout(14, 14));
        page.setOpaque(false);

        JPanel header = pageHeader("System Settings", "Update the logo, shop name, and accent color.");

        JButton btnReload = ghost("Reload");
        btnReload.addActionListener(e -> loadSystemSettings());

        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);
        headerRow.add(header, BorderLayout.WEST);
        headerRow.add(btnReload, BorderLayout.EAST);

        page.add(headerRow, BorderLayout.NORTH);

        JPanel card = surfacePanel(new EmptyBorder(16, 16, 16, 16));
        card.setLayout(new GridBagLayout());

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.NORTHWEST;

        JLabel sectionTitle = new JLabel("Brand & Theme");
        sectionTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        sectionTitle.setForeground(TEXT);

        g.gridy = 0;
        g.insets = new Insets(0, 0, 12, 0);
        card.add(sectionTitle, g);

        fSystemName = new JTextField();
        styleField(fSystemName);

        g.gridy = 1; g.insets = new Insets(0, 0, 6, 0);
        card.add(fieldLabel("System name"), g);
        g.gridy = 2; g.insets = new Insets(0, 0, 16, 0);
        card.add(fSystemName, g);

        logoPreview = new JLabel();
        logoPreview.setPreferredSize(new Dimension(96, 96));
        logoPreview.setHorizontalAlignment(SwingConstants.CENTER);
        logoPreview.setBorder(BorderFactory.createLineBorder(BORDER, 1));

        JButton btnUploadLogo = primaryOutline("Upload Logo");
        btnUploadLogo.addActionListener(e -> chooseLogo());

        JPanel logoRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        logoRow.setOpaque(false);
        logoRow.add(logoPreview);
        logoRow.add(btnUploadLogo);

        g.gridy = 3; g.insets = new Insets(0, 0, 6, 0);
        card.add(fieldLabel("Logo"), g);
        g.gridy = 4; g.insets = new Insets(0, 0, 16, 0);
        card.add(logoRow, g);

        accentSwatch = new JPanel();
        accentSwatch.setPreferredSize(new Dimension(48, 28));
        accentSwatch.setBorder(BorderFactory.createLineBorder(BORDER, 1));

        accentValue = new JLabel("#2055C5");
        accentValue.setFont(new Font("SansSerif", Font.PLAIN, 12));
        accentValue.setForeground(MUTED);

        JButton btnPickAccent = primaryOutline("Pick Color");
        btnPickAccent.addActionListener(e -> chooseAccent());

        JPanel accentRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        accentRow.setOpaque(false);
        accentRow.add(accentSwatch);
        accentRow.add(accentValue);
        accentRow.add(btnPickAccent);

        g.gridy = 5; g.insets = new Insets(0, 0, 6, 0);
        card.add(fieldLabel("Accent color"), g);
        g.gridy = 6; g.insets = new Insets(0, 0, 18, 0);
        card.add(accentRow, g);

        JButton btnSave = primary("Save Settings");
        btnSave.addActionListener(e -> onSaveSystemSettings());

        g.gridy = 7; g.insets = new Insets(0, 0, 0, 0);
        card.add(btnSave, g);

        page.add(card, BorderLayout.CENTER);

        loadSystemSettings();
        return page;
    }

    private void loadSystemSettings() {
        fSystemName.setText(assetService.getShopNameOrDefault());
        selectedAccent = assetService.getAccentColorOrDefault();
        updateAccentPreview(selectedAccent);
        selectedLogoBytes = null;
        ImageIcon logo = assetService.getShopLogoOrNull(92);
        if (logo != null) {
            logoPreview.setIcon(logo);
        } else {
            logoPreview.setIcon(UIManager.getIcon("OptionPane.informationIcon"));
        }
    }

    private void chooseLogo() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choose logo image");
        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;
        try {
            java.nio.file.Path path = chooser.getSelectedFile().toPath();
            selectedLogoBytes = java.nio.file.Files.readAllBytes(path);
            ImageIcon scaled = scaleLogo(selectedLogoBytes, 92);
            if (scaled != null) {
                logoPreview.setIcon(scaled);
            } else {
                JOptionPane.showMessageDialog(this, "Unsupported image format.");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Unable to load logo: " + ex.getMessage());
        }
    }

    private void chooseAccent() {
        Color picked = JColorChooser.showDialog(this, "Pick Accent Color", selectedAccent);
        if (picked == null) return;
        selectedAccent = picked;
        updateAccentPreview(picked);
    }

    private void updateAccentPreview(Color color) {
        accentSwatch.setBackground(color);
        accentValue.setText(String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue()));
    }

    private void onSaveSystemSettings() {
        String name = fSystemName.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "System name cannot be empty.");
            return;
        }
        try {
            assetService.saveShopName(name);
            assetService.saveAccentColor(selectedAccent);
            if (selectedLogoBytes != null) {
                assetService.saveShopLogo(selectedLogoBytes);
            }
            JOptionPane.showMessageDialog(this, "System settings updated. Re-open other views to apply changes.");
        } catch (Exception ex) {
            showDbError(ex);
        }
    }

    private ImageIcon scaleLogo(byte[] data, int size) {
        try {
            java.awt.Image img = javax.imageio.ImageIO.read(new java.io.ByteArrayInputStream(data));
            if (img == null) return null;
            java.awt.Image scaled = img.getScaledInstance(size, size, java.awt.Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        } catch (Exception ex) {
            return null;
        }
    }

    // -------------------- Placeholders --------------------

    private JComponent buildCashierPage() {
        JPanel page = new JPanel(new BorderLayout(14, 14));
        page.setOpaque(false);

        page.add(pageHeader("Cashier View", "Use the cashier workspace with live data."), BorderLayout.NORTH);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        cashierPanel = new CashierPanel(false, ownerUsername + " (Owner)", assetService.getShopNameOrDefault());
        wrap.add(cashierPanel, BorderLayout.CENTER);

        page.add(wrap, BorderLayout.CENTER);
        return page;
    }

    private JComponent buildBaristaPage() {
        JPanel page = new JPanel(new BorderLayout(14, 14));
        page.setOpaque(false);

        page.add(pageHeader("Barista View", "Monitor the active queue and serve orders."), BorderLayout.NORTH);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        baristaPanel = new BaristaPanel(false, ownerUsername + " (Owner)", assetService.getShopNameOrDefault());
        wrap.add(baristaPanel, BorderLayout.CENTER);

        page.add(wrap, BorderLayout.CENTER);
        return page;
    }

    private JComponent buildPlaceholderPage(String title, String body) {
        JPanel page = new JPanel(new BorderLayout(14, 14));
        page.setOpaque(false);
        page.add(pageHeader(title, "Coming soon."), BorderLayout.NORTH);
        page.add(surfacePlaceholder(title, body), BorderLayout.CENTER);
        return page;
    }

    // -------------------- UI helpers --------------------

    private JPanel pageHeader(String title, String subtitle) {
        JPanel head = new JPanel();
        head.setOpaque(false);
        head.setLayout(new BoxLayout(head, BoxLayout.Y_AXIS));

        JLabel t = new JLabel(title);
        t.setFont(new Font("SansSerif", Font.BOLD, 18));
        t.setForeground(TEXT);

        JLabel s = new JLabel(subtitle);
        s.setFont(new Font("SansSerif", Font.PLAIN, 12));
        s.setForeground(MUTED);

        head.add(t);
        head.add(Box.createVerticalStrut(3));
        head.add(s);
        return head;
    }

    private JPanel surfacePanel(EmptyBorder padding) {
        JPanel p = new JPanel();
        p.setBackground(SURFACE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                padding
        ));
        return p;
    }

    private JComponent surfacePlaceholder(String title, String body) {
        JPanel p = surfacePanel(new EmptyBorder(18, 18, 18, 18));
        p.setLayout(new BorderLayout(10, 10));

        JLabel t = new JLabel(title);
        t.setFont(new Font("SansSerif", Font.BOLD, 14));
        t.setForeground(TEXT);

        JTextArea a = new JTextArea(body);
        a.setFont(new Font("SansSerif", Font.PLAIN, 13));
        a.setForeground(new Color(73, 80, 87));
        a.setOpaque(false);
        a.setEditable(false);
        a.setLineWrap(true);
        a.setWrapStyleWord(true);

        p.add(t, BorderLayout.NORTH);
        p.add(a, BorderLayout.CENTER);
        return p;
    }

    private JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.PLAIN, 12));
        l.setForeground(new Color(73, 80, 87));
        return l;
    }

    private JLabel metricValueLabel(String value) {
        JLabel v = new JLabel(value);
        v.setFont(new Font("SansSerif", Font.BOLD, 22));
        v.setForeground(TEXT);
        return v;
    }

    private JPanel labeled(String label, JComponent field) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        JLabel l = new JLabel(label);
        l.setFont(new Font("SansSerif", Font.PLAIN, 12));
        l.setForeground(new Color(73, 80, 87));

        p.add(l);
        p.add(Box.createVerticalStrut(4));
        p.add(field);
        return p;
    }

    private JComponent metricCardWithValue(String title, JLabel valueLabel, String footnote) {
        JPanel card = surfacePanel(new EmptyBorder(14, 14, 14, 14));
        card.setLayout(new BorderLayout(6, 6));

        JLabel t = new JLabel(title);
        t.setFont(new Font("SansSerif", Font.PLAIN, 12));
        t.setForeground(MUTED);

        JLabel f = new JLabel(footnote);
        f.setFont(new Font("SansSerif", Font.PLAIN, 12));
        f.setForeground(new Color(173, 181, 189));

        card.add(t, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        card.add(f, BorderLayout.SOUTH);
        return card;
    }

    private void styleTable(JTable table) {
        table.setBackground(SURFACE);
        table.setForeground(TEXT);
        table.setShowHorizontalLines(false);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setGridColor(BORDER);
        table.setFillsViewportHeight(true);
        table.setSelectionBackground(tableSelection);
        table.setSelectionForeground(TEXT);
        table.setDefaultRenderer(Object.class, new ZebraTableCellRenderer());

        JTableHeader header = table.getTableHeader();
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 34));
        header.setDefaultRenderer(new HeaderRenderer());
        header.setBackground(TABLE_HEADER_BG);
        header.setForeground(TABLE_HEADER_TEXT);
    }

    private void styleField(JComponent c) {
        c.setFont(new Font("SansSerif", Font.PLAIN, 13));
        c.setBackground(new Color(248, 250, 255));
        c.setForeground(TEXT);
        c.setOpaque(true);
        c.setBorder(buildFieldBorder(false));
        c.setPreferredSize(new Dimension(10, 44));
        c.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) { c.setBorder(buildFieldBorder(true)); }
            @Override public void focusLost(java.awt.event.FocusEvent e) { c.setBorder(buildFieldBorder(false)); }
        });
    }

    private Border buildFieldBorder(boolean focused) {
        Color glow = new Color(primary.getRed(), primary.getGreen(), primary.getBlue(), focused ? 160 : 70);
        Color line = focused ? primary : BORDER;
        int thickness = focused ? 2 : 1;
        int pad = focused ? 9 : 10;
        return BorderFactory.createCompoundBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(glow, focused ? 3 : 1),
                        BorderFactory.createLineBorder(line, thickness)
                ),
                new EmptyBorder(pad, 12, pad, 12)
        );
    }

    private JButton primary(String text) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setFont(new Font("SansSerif", Font.BOLD, 12));
        b.setBackground(primary);
        b.setForeground(Color.WHITE);
        b.setBorder(new EmptyBorder(10, 12, 10, 12));
        return b;
    }

    private JButton primaryOutline(String text) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setFont(new Font("SansSerif", Font.BOLD, 12));
        b.setBackground(Color.WHITE);
        b.setForeground(primaryDark);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(primary, 1),
                new EmptyBorder(10, 12, 10, 12)
        ));
        return b;
    }

    private JButton danger(String text) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setFont(new Font("SansSerif", Font.BOLD, 12));
        b.setBackground(DANGER);
        b.setForeground(Color.WHITE);
        b.setBorder(new EmptyBorder(10, 12, 10, 12));
        return b;
    }

    private JButton ghost(String text) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setFont(new Font("SansSerif", Font.BOLD, 12));
        b.setBackground(new Color(248, 249, 250));
        b.setForeground(TEXT);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                new EmptyBorder(10, 12, 10, 12)
        ));
        return b;
    }

    private void showDbError(Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(
                this,
                "Database error:\n" + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    private static Color tint(Color color, double amount) {
        int r = clamp(color.getRed() + (int) ((255 - color.getRed()) * amount));
        int g = clamp(color.getGreen() + (int) ((255 - color.getGreen()) * amount));
        int b = clamp(color.getBlue() + (int) ((255 - color.getBlue()) * amount));
        return new Color(r, g, b);
    }

    private static Color shade(Color color, double amount) {
        int r = clamp(color.getRed() - (int) (255 * amount));
        int g = clamp(color.getGreen() - (int) (255 * amount));
        int b = clamp(color.getBlue() - (int) (255 * amount));
        return new Color(r, g, b);
    }

    private static int clamp(int value) {
        return Math.min(255, Math.max(0, value));
    }

    private class HeaderRenderer implements TableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            JLabel l = new JLabel(value == null ? "" : value.toString());
            l.setOpaque(true);
            l.setBackground(TABLE_HEADER_BG);
            l.setForeground(TABLE_HEADER_TEXT);
            l.setFont(new Font("SansSerif", Font.BOLD, 12));
            l.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
                    new EmptyBorder(6, 10, 6, 10)
            ));
            return l;
        }
    }

    private class ZebraTableCellRenderer extends javax.swing.table.DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (isSelected) {
                c.setBackground(tableSelection);
                c.setForeground(TEXT);
            } else {
                c.setBackground(row % 2 == 0 ? SURFACE : TABLE_ROW_ALT);
                c.setForeground(TEXT);
            }
            if (c instanceof JComponent) {
                ((JComponent) c).setBorder(new EmptyBorder(6, 10, 6, 10));
            }
            return c;
        }
    }

    private class LockedUserRenderer extends javax.swing.table.DefaultTableCellRenderer {
        private final Color lockedBg = new Color(254, 226, 226);
        private final Color lockedFg = new Color(153, 27, 27);

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            boolean locked = false;
            int lockedColumn = 3;
            if (table.getColumnCount() > lockedColumn) {
                Object lockedValue = table.getValueAt(row, lockedColumn);
                if (lockedValue != null) {
                    locked = "Yes".equalsIgnoreCase(lockedValue.toString())
                            || "true".equalsIgnoreCase(lockedValue.toString());
                }
            }

            if (locked && !isSelected) {
                c.setBackground(lockedBg);
                c.setForeground(lockedFg);
            } else if (isSelected) {
                c.setBackground(tableSelection);
                c.setForeground(TEXT);
            } else if (!isSelected) {
                c.setBackground(row % 2 == 0 ? SURFACE : TABLE_ROW_ALT);
                c.setForeground(TEXT);
            }
            if (c instanceof JComponent) {
                ((JComponent) c).setBorder(new EmptyBorder(6, 10, 6, 10));
            }
            return c;
        }
    }

    // -------------------- Nav item component --------------------

    private static class NavItem extends JButton {
        private boolean selected;
        private final Color accent;
        private final Color accentSoft;
        private final Color accentText;

        NavItem(String text, Icon icon, Color accent, Color accentSoft, Color accentText) {
            super(text, icon);
            this.accent = accent;
            this.accentSoft = accentSoft;
            this.accentText = accentText;
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setHorizontalAlignment(SwingConstants.LEFT);
            setIconTextGap(10);
            setFont(new Font("SansSerif", Font.PLAIN, 13));
            setBorder(new EmptyBorder(10, 14, 10, 14));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
            setSelected(false);
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
            if (selected) {
                setBackground(accentSoft);
                setForeground(accentText);
                setOpaque(true);
            } else {
                setBackground(Color.WHITE);
                setForeground(new Color(30, 41, 59));
                setOpaque(true);
            }
        }

        @Override public boolean isSelected() { return selected; }
    }

    // -------------------- Quick test --------------------

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ignored) {}
            new OwnerFrame("owner").setVisible(true);
        });
    }
}
