package app.ui;

import app.db.UserDAO;
import app.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class OwnerFrame extends JFrame {

    // Pages
    private static final String PAGE_DASHBOARD = "dashboard";
    private static final String PAGE_USERS = "users";
    private static final String PAGE_CASHIER = "cashier";
    private static final String PAGE_BARISTA = "barista";

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel content = new JPanel(cardLayout);

    // DB
    private final UserDAO userDAO = new UserDAO();

    // Users page components
    private DefaultTableModel usersModel;
    private JTable usersTable;
    private JTextField fId;
    private JTextField fUser;
    private JPasswordField fPass;
    private JComboBox<String> fRole;
    private JLabel usersHint;

    // Theme
    private static final Color BG = new Color(245, 247, 250);
    private static final Color SURFACE = Color.WHITE;
    private static final Color BORDER = new Color(233, 236, 239);
    private static final Color TEXT = new Color(33, 37, 41);
    private static final Color MUTED = new Color(108, 117, 125);
    private static final Color PRIMARY = new Color(32, 85, 197);
    private static final Color DANGER = new Color(220, 53, 69);

    public OwnerFrame(String ownerUsername) {
        setTitle("Owner Panel");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        setContentPane(buildUI(ownerUsername));
        SwingUtilities.invokeLater(this::refreshUsersTableSafe); // load after UI is visible
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

        JLabel subtitle = new JLabel("Manage users, preview staff roles, and view shop status.");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subtitle.setForeground(MUTED);

        left.add(title);
        left.add(Box.createVerticalStrut(2));
        left.add(subtitle);

        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.X_AXIS));

        JLabel user = new JLabel("Logged in as: " + ownerUsername);
        user.setFont(new Font("SansSerif", Font.PLAIN, 12));
        user.setForeground(MUTED);

        right.add(user);

        top.add(left, BorderLayout.WEST);
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

        JLabel nav = sectionLabel("MAIN");
        side.add(nav);

        NavItem dash = new NavItem("Dashboard", UIManager.getIcon("OptionPane.informationIcon"));
        NavItem users = new NavItem("User Management", UIManager.getIcon("FileView.directoryIcon"));

        dash.addActionListener(e -> show(PAGE_DASHBOARD, dash, users, null, null));
        users.addActionListener(e -> show(PAGE_USERS, users, dash, null, null));

        side.add(dash);
        side.add(Box.createVerticalStrut(8));
        side.add(users);

        side.add(Box.createVerticalStrut(18));
        side.add(sectionLabel("ROLE PREVIEWS"));

        NavItem cashier = new NavItem("Cashier View", UIManager.getIcon("FileView.fileIcon"));
        NavItem barista = new NavItem("Barista View", UIManager.getIcon("FileView.fileIcon"));

        cashier.addActionListener(e -> show(PAGE_CASHIER, cashier, dash, users, barista));
        barista.addActionListener(e -> show(PAGE_BARISTA, barista, dash, users, cashier));

        side.add(cashier);
        side.add(Box.createVerticalStrut(8));
        side.add(barista);

        side.add(Box.createVerticalGlue());

        // start selected
        show(PAGE_DASHBOARD, dash, users, cashier, barista);

        return side;
    }

    private void show(String page, NavItem selected, NavItem a, NavItem b, NavItem c) {
        if (a != null) a.setSelected(a == selected);
        if (b != null) b.setSelected(b == selected);
        if (c != null) c.setSelected(c == selected);
        selected.setSelected(true);

        cardLayout.show(content, page);
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
        content.add(buildPlaceholderPage(
                "Cashier View (Placeholder)",
                "This will mirror the cashier’s UI (Menu, Cart, Payments, Receipts, Order Queue).\n\nFor now, this is a placeholder."
        ), PAGE_CASHIER);
        content.add(buildPlaceholderPage(
                "Barista View (Placeholder)",
                "This will mirror the barista’s UI (Queue Monitor, Order Status, Preparation workflow).\n\nFor now, this is a placeholder."
        ), PAGE_BARISTA);

        wrapper.add(content, BorderLayout.CENTER);
        return wrapper;
    }

    // -------------------- Dashboard --------------------

    private JComponent buildDashboardPage() {
        JPanel page = new JPanel(new BorderLayout(14, 14));
        page.setOpaque(false);

        page.add(pageHeader("Dashboard", "High-level overview (placeholders for now)."), BorderLayout.NORTH);

        JPanel cards = new JPanel(new GridLayout(1, 4, 12, 12));
        cards.setOpaque(false);

        cards.add(metricCard("Today Sales", "₱ 0.00", "Placeholder"));
        cards.add(metricCard("Orders In Queue", "0", "Placeholder"));
        cards.add(metricCard("Completed Today", "0", "Placeholder"));
        cards.add(metricCard("Active Staff", "0", "Placeholder"));

        JPanel lower = new JPanel(new GridLayout(1, 2, 12, 12));
        lower.setOpaque(false);

        lower.add(surfacePlaceholder("Sales Trend", "Add chart later (daily/weekly sales)."));
        lower.add(surfacePlaceholder("Top Items", "Add table later (best sellers)."));

        JPanel center = new JPanel(new BorderLayout(12, 12));
        center.setOpaque(false);
        center.add(cards, BorderLayout.NORTH);
        center.add(lower, BorderLayout.CENTER);

        page.add(center, BorderLayout.CENTER);
        return page;
    }

    private JComponent metricCard(String title, String value, String footnote) {
        JPanel card = surfacePanel(new EmptyBorder(14, 14, 14, 14));
        card.setLayout(new BorderLayout(6, 6));

        JLabel t = new JLabel(title);
        t.setFont(new Font("SansSerif", Font.PLAIN, 12));
        t.setForeground(MUTED);

        JLabel v = new JLabel(value);
        v.setFont(new Font("SansSerif", Font.BOLD, 22));
        v.setForeground(TEXT);

        JLabel f = new JLabel(footnote);
        f.setFont(new Font("SansSerif", Font.PLAIN, 12));
        f.setForeground(new Color(173, 181, 189));

        card.add(t, BorderLayout.NORTH);
        card.add(v, BorderLayout.CENTER);
        card.add(f, BorderLayout.SOUTH);

        return card;
    }

    // -------------------- Users CRUD (DB-backed) --------------------

    private JComponent buildUsersPage() {
        JPanel page = new JPanel(new BorderLayout(14, 14));
        page.setOpaque(false);

        JPanel header = pageHeader("User Management", "Create, update, and delete staff accounts.");
        page.add(header, BorderLayout.NORTH);

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

        // If your DB uses OWNER/CASHIER/BARISTA, change these values to match exactly.
        fRole = new JComboBox<>(new String[]{"owner", "Cashier", "Barista"});

        styleField(fId);
        styleField(fUser);
        styleField(fPass);
        styleField(fRole);

        JButton btnNew = ghost("New");
        JButton btnRefresh = ghost("Refresh");
        JButton btnAdd = primary("Add User");
        JButton btnUpdate = primaryOutline("Update");
        JButton btnDelete = danger("Delete");

        btnNew.addActionListener(e -> clearUserForm());
        btnRefresh.addActionListener(e -> refreshUsersTableSafe());
        btnAdd.addActionListener(e -> onAddUser());
        btnUpdate.addActionListener(e -> onUpdateUser());
        btnDelete.addActionListener(e -> onDeleteUser());

        // Select row -> load form
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

        JPanel row1 = new JPanel(new GridLayout(1, 2, 10, 10));
        row1.setOpaque(false);
        row1.add(btnNew);
        row1.add(btnRefresh);

        JPanel row2 = new JPanel(new GridLayout(1, 3, 10, 10));
        row2.setOpaque(false);
        row2.add(btnAdd);
        row2.add(btnUpdate);
        row2.add(btnDelete);

        g.gridy = 9; g.insets = new Insets(0, 0, 10, 0);
        right.add(row1, g);

        g.gridy = 10; g.insets = new Insets(0, 0, 0, 0);
        right.add(row2, g);

        g.gridy = 11; g.weighty = 1;
        right.add(Box.createVerticalGlue(), g);

        // Layout main: left table + right form
        JPanel center = new JPanel(new BorderLayout(12, 12));
        center.setOpaque(false);
        center.add(left, BorderLayout.CENTER);
        center.add(right, BorderLayout.EAST);

        page.add(center, BorderLayout.CENTER);
        return page;
    }

    // -------------------- Placeholders --------------------

    private JComponent buildPlaceholderPage(String title, String body) {
        JPanel page = new JPanel(new BorderLayout(14, 14));
        page.setOpaque(false);

        page.add(pageHeader(title, "Coming soon."), BorderLayout.NORTH);
        page.add(surfacePlaceholder(title, body), BorderLayout.CENTER);
        return page;
    }

    // -------------------- DB CRUD handlers --------------------

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
            fPass.setText(u.getPassword()); // current DB stores plain text
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
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Delete this user?",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION
            );
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

    private void showDbError(Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(
                this,
                "Database error:\n" + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    // -------------------- UI components & styling --------------------

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
