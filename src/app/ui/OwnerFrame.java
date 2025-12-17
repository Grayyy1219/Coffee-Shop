package app.ui;

import app.db.AssetService;
import app.db.DashboardDAO;
import app.db.UserDAO;
import app.model.DailySalesRow;
import app.model.DashboardSummary;
import app.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class OwnerFrame extends JFrame {

    // Pages
    private static final String PAGE_DASHBOARD = "dashboard";
    private static final String PAGE_USERS = "users";
    private static final String PAGE_CASHIER = "cashier";
    private static final String PAGE_BARISTA = "barista";

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

    private final NumberFormat moneyPH = NumberFormat.getCurrencyInstance(new Locale("en", "PH"));

    // Theme
    private static final Color BG = new Color(245, 247, 250);
    private static final Color SURFACE = Color.WHITE;
    private static final Color BORDER = new Color(233, 236, 239);
    private static final Color TEXT = new Color(33, 37, 41);
    private static final Color MUTED = new Color(108, 117, 125);
    private static final Color PRIMARY = new Color(32, 85, 197);
    private static final Color DANGER = new Color(220, 53, 69);

    private final AssetService assetService = new AssetService();
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

        NavItem dash = new NavItem("Dashboard", UIManager.getIcon("OptionPane.informationIcon"));
        NavItem users = new NavItem("User Management", UIManager.getIcon("FileView.directoryIcon"));

        dash.addActionListener(e -> selectPage(PAGE_DASHBOARD, dash, users, null, null));
        users.addActionListener(e -> selectPage(PAGE_USERS, users, dash, null, null));

        side.add(dash);
        side.add(Box.createVerticalStrut(8));
        side.add(users);

        side.add(Box.createVerticalStrut(18));
        side.add(sectionLabel("ROLE PREVIEWS"));

        NavItem cashier = new NavItem("Cashier View", UIManager.getIcon("FileView.fileIcon"));
        NavItem barista = new NavItem("Barista View", UIManager.getIcon("FileView.fileIcon"));

        cashier.addActionListener(e -> selectPage(PAGE_CASHIER, cashier, dash, users, barista));
        barista.addActionListener(e -> selectPage(PAGE_BARISTA, barista, dash, users, cashier));

        side.add(cashier);
        side.add(Box.createVerticalStrut(8));
        side.add(barista);

        side.add(Box.createVerticalGlue());

        // Default selection
        selectPage(PAGE_DASHBOARD, dash, users, cashier, barista);

        return side;
    }

    private void selectPage(String page, NavItem selected, NavItem a, NavItem b, NavItem c) {
        if (a != null) a.setSelected(a == selected);
        if (b != null) b.setSelected(b == selected);
        if (c != null) c.setSelected(c == selected);
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
        content.add(buildCashierPage(), PAGE_CASHIER);
        content.add(buildPlaceholderPage(
                "Barista View (Placeholder)",
                "This will mirror the barista’s UI (Queue Monitor, Order Status, Preparation workflow).\n\nFor now, this is a placeholder."
        ), PAGE_BARISTA);

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

        dailySalesModel = new DefaultTableModel(new String[]{"Date", "Gross", "Paid", "Orders"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = new JTable(dailySalesModel);
        table.setRowHeight(28);
        table.getTableHeader().setReorderingAllowed(false);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(BORDER, 1));

        tableBox.add(t, BorderLayout.NORTH);
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
        b.setBackground(PRIMARY);
        b.setForeground(Color.WHITE);
        b.setBorder(new EmptyBorder(10, 12, 10, 12));
        return b;
    }

    private JButton primaryOutline(String text) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setFont(new Font("SansSerif", Font.BOLD, 12));
        b.setBackground(new Color(248, 249, 250));
        b.setForeground(TEXT);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 225, 230), 1),
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

    // -------------------- Nav item component --------------------

    private static class NavItem extends JButton {
        private boolean selected;

        NavItem(String text, Icon icon) {
            super(text, icon);
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
                setBackground(new Color(232, 240, 255));
                setForeground(new Color(18, 54, 120));
                setOpaque(true);
            } else {
                setBackground(Color.WHITE);
                setForeground(new Color(33, 37, 41));
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
