package app.ui;

import app.db.AssetService;
import app.db.DashboardDAO;
import app.db.UserDAO;
import app.model.DailySalesRow;
import app.model.DashboardSummary;
import app.model.User;
import app.util.SelectionSort;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class OwnerFrame extends JFrame {

    // Pages
    private static final String PAGE_DASHBOARD = "dashboard";
    private static final String PAGE_USERS = "users";
    private static final String PAGE_CASHIER = "cashier";
    private static final String PAGE_BARISTA = "barista";
    private static final String PAGE_SETTINGS = "settings";

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel content = new JPanel(cardLayout);

    // DAOs
    private final UserDAO userDAO = new UserDAO();
    private final DashboardDAO dashboardDAO = new DashboardDAO();

    // USERS: table + form
    private DefaultTableModel usersModel;
    private JTable usersTable;
    private JTextField fId;
    private JTextField fUser;
    private JPasswordField fPass;
    private JComboBox<String> fRole;
    private JLabel usersHint;

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

    // Theme
    private static final Color BG = new Color(243, 245, 249);
    private static final Color SURFACE = Color.WHITE;
    private static final Color BORDER = new Color(226, 232, 240);
    private static final Color TEXT = new Color(30, 41, 59);
    private static final Color MUTED = new Color(100, 116, 139);
    private static final Color DANGER = new Color(220, 38, 38);
    private final AssetService assetService = new AssetService();
    private final Color primary = assetService.getAccentColorOrDefault();
    private final Color primaryDark = shade(primary, 0.2);
    private final Color primarySoft = tint(primary, 0.86);
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
        top.add(user, BorderLayout.EAST);

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
        NavItem settings = new NavItem("System Settings", UIManager.getIcon("FileView.computerIcon"), primary, primarySoft, primaryDark);

        dash.addActionListener(e -> selectPage(PAGE_DASHBOARD, dash, dash, users, settings, null, null));
        users.addActionListener(e -> selectPage(PAGE_USERS, users, dash, users, settings, null, null));
        settings.addActionListener(e -> selectPage(PAGE_SETTINGS, settings, dash, users, settings, null, null));

        side.add(dash);
        side.add(Box.createVerticalStrut(8));
        side.add(users);
        side.add(Box.createVerticalStrut(8));
        side.add(settings);

        side.add(Box.createVerticalStrut(18));
        side.add(sectionLabel("ROLE PREVIEWS"));

        NavItem cashier = new NavItem("Cashier View", UIManager.getIcon("FileView.fileIcon"), primary, primarySoft, primaryDark);
        NavItem barista = new NavItem("Barista View", UIManager.getIcon("FileView.fileIcon"), primary, primarySoft, primaryDark);

        cashier.addActionListener(e -> selectPage(PAGE_CASHIER, cashier, dash, users, settings, cashier, barista));
        barista.addActionListener(e -> selectPage(PAGE_BARISTA, barista, dash, users, settings, cashier, barista));

        side.add(cashier);
        side.add(Box.createVerticalStrut(8));
        side.add(barista);

        side.add(Box.createVerticalGlue());

        // Default selection
        selectPage(PAGE_DASHBOARD, dash, dash, users, settings, cashier, barista);

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
        table.getTableHeader().setReorderingAllowed(false);

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
                SelectionSort.sort(rows, comparator); // selection sort entry point for sales rows
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

        usersModel = new DefaultTableModel(new String[]{"ID", "Username", "Role"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        usersTable = new JTable(usersModel);
        usersTable.setRowHeight(30);
        usersTable.setShowHorizontalLines(true);
        usersTable.setGridColor(new Color(241, 243, 245));
        usersTable.getTableHeader().setReorderingAllowed(false);

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

        btnNew.addActionListener(e -> clearUserForm());
        btnAdd.addActionListener(e -> onAddUser());
        btnUpdate.addActionListener(e -> onUpdateUser());
        btnDelete.addActionListener(e -> onDeleteUser());

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

        JPanel actions = new JPanel(new GridLayout(1, 4, 10, 10));
        actions.setOpaque(false);
        actions.add(btnNew);
        actions.add(btnAdd);
        actions.add(btnUpdate);
        actions.add(btnDelete);

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
            usersModel.addRow(new Object[]{u.getId(), u.getUsername(), u.getRole()});
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

        page.add(pageHeader("Cashier View (Preview)", "Interact with the cashier UI using in-memory data."), BorderLayout.NORTH);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(new CashierPanel(true, ownerUsername + " (Owner)", assetService.getShopNameOrDefault()), BorderLayout.CENTER);

        page.add(wrap, BorderLayout.CENTER);
        return page;
    }

    private JComponent buildBaristaPage() {
        JPanel page = new JPanel(new BorderLayout(14, 14));
        page.setOpaque(false);

        page.add(pageHeader("Barista View (Preview)", "Monitor the active queue and serve orders."), BorderLayout.NORTH);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(new BaristaPanel(true, ownerUsername + " (Owner)", assetService.getShopNameOrDefault()), BorderLayout.CENTER);

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

    private void styleField(JComponent c) {
        c.setFont(new Font("SansSerif", Font.PLAIN, 13));
        c.setBackground(Color.WHITE);
        c.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(222, 226, 230), 1),
                new EmptyBorder(10, 12, 10, 12)
        ));
        c.setPreferredSize(new Dimension(10, 44));
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
