package app.ui;

import app.db.UserDAO;
import app.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class OwnerFrame extends JFrame {

    // Navigation pages
    private static final String PAGE_DASHBOARD = "dashboard";
    private static final String PAGE_USERS = "users";
    private static final String PAGE_CASHIER = "cashier";
    private static final String PAGE_BARISTA = "barista";

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel content = new JPanel(cardLayout);

    // DAO
    private final UserDAO userDAO = new UserDAO();

    // Users page components
    private DefaultTableModel usersModel;
    private JTable usersTable;
    private JTextField fId;
    private JTextField fUser;
    private JPasswordField fPass;
    private JComboBox<String> fRole;

    public OwnerFrame(String ownerUsername) {
        setTitle("Owner Panel");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setContentPane(buildUI(ownerUsername));
    }

    private JComponent buildUI(String ownerUsername) {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(245, 247, 250));

        // ---------- Top Bar ----------
        JPanel top = new JPanel(new BorderLayout());
        top.setBorder(new EmptyBorder(12, 16, 12, 16));
        top.setBackground(Color.WHITE);

        JLabel title = new JLabel("Owner Panel");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setForeground(new Color(33, 37, 41));

        JLabel who = new JLabel("Logged in as: " + ownerUsername);
        who.setFont(new Font("SansSerif", Font.PLAIN, 12));
        who.setForeground(new Color(108, 117, 125));

        top.add(title, BorderLayout.WEST);
        top.add(who, BorderLayout.EAST);

        // ---------- Sidebar ----------
        JPanel side = new JPanel();
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBorder(new EmptyBorder(16, 12, 16, 12));
        side.setBackground(Color.WHITE);
        side.setPreferredSize(new Dimension(260, 10));

        JLabel navLbl = new JLabel("Navigation");
        navLbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        navLbl.setForeground(new Color(108, 117, 125));
        navLbl.setBorder(new EmptyBorder(0, 8, 10, 8));

        JButton btnDash = navButton("Dashboard");
        JButton btnUsers = navButton("User Management (CRUD)");

        JLabel previewsLbl = sectionLabel("Role Previews");
        JButton btnCashier = navButton("Cashier View (Placeholder)");
        JButton btnBarista = navButton("Barista View (Placeholder)");

        btnDash.addActionListener(e -> show(PAGE_DASHBOARD));
        btnUsers.addActionListener(e -> show(PAGE_USERS));
        btnCashier.addActionListener(e -> show(PAGE_CASHIER));
        btnBarista.addActionListener(e -> show(PAGE_BARISTA));

        side.add(navLbl);
        side.add(btnDash);
        side.add(Box.createVerticalStrut(8));
        side.add(btnUsers);
        side.add(Box.createVerticalStrut(14));
        side.add(previewsLbl);
        side.add(btnCashier);
        side.add(Box.createVerticalStrut(8));
        side.add(btnBarista);
        side.add(Box.createVerticalGlue());

        // ---------- Content Pages ----------
        content.setBackground(new Color(245, 247, 250));
        content.add(buildDashboardPage(), PAGE_DASHBOARD);
        content.add(buildUsersCrudPage(), PAGE_USERS);
        content.add(buildPlaceholderPage(
                "Cashier View (Placeholder)",
                "This area will mirror what a cashier can see and do.\n\nFor now: placeholder."
        ), PAGE_CASHIER);
        content.add(buildPlaceholderPage(
                "Barista View (Placeholder)",
                "This area will mirror what a barista can see and do.\n\nFor now: placeholder."
        ), PAGE_BARISTA);

        // Default page
        show(PAGE_DASHBOARD);

        // ---------- Layout ----------
        root.add(top, BorderLayout.NORTH);
        root.add(side, BorderLayout.WEST);
        root.add(wrapContent(content), BorderLayout.CENTER);

        return root;
    }

    private void show(String page) {
        cardLayout.show(content, page);
    }

    private JComponent wrapContent(JComponent center) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(245, 247, 250));
        p.setBorder(new EmptyBorder(18, 18, 18, 18));
        p.add(center, BorderLayout.CENTER);
        return p;
    }

    // ------------------- Pages -------------------

    private JComponent buildDashboardPage() {
        JPanel page = new JPanel(new BorderLayout(12, 12));
        page.setBackground(new Color(245, 247, 250));

        JLabel h = new JLabel("Dashboard");
        h.setFont(new Font("SansSerif", Font.BOLD, 18));
        h.setForeground(new Color(33, 37, 41));

        JPanel cards = new JPanel(new GridLayout(1, 3, 12, 12));
        cards.setOpaque(false);

        cards.add(statCard("Today Sales", "₱ 0.00", "Placeholder"));
        cards.add(statCard("Active Orders", "0", "Placeholder"));
        cards.add(statCard("Low Stock Items", "0", "Placeholder"));

        JPanel center = new JPanel(new BorderLayout(12, 12));
        center.setOpaque(false);
        center.add(cards, BorderLayout.NORTH);
        center.add(bigPlaceholder("Reports / Charts", "Put charts here later (sales trend, best sellers, etc.)."), BorderLayout.CENTER);

        page.add(h, BorderLayout.NORTH);
        page.add(center, BorderLayout.CENTER);
        return page;
    }

    private JComponent buildUsersCrudPage() {
        JPanel page = new JPanel(new BorderLayout(12, 12));
        page.setBackground(new Color(245, 247, 250));

        JLabel h = new JLabel("User Management (CRUD)");
        h.setFont(new Font("SansSerif", Font.BOLD, 18));
        h.setForeground(new Color(33, 37, 41));

        // Table model (DB-backed)
        String[] cols = {"ID", "Username", "Role"};
        usersModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        usersTable = new JTable(usersModel);
        usersTable.setRowHeight(28);

        JScrollPane sp = new JScrollPane(usersTable);
        sp.setBorder(BorderFactory.createLineBorder(new Color(233, 236, 239), 1));

        // Right form
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(233, 236, 239), 1),
                new EmptyBorder(14, 14, 14, 14)
        ));
        form.setPreferredSize(new Dimension(390, 10));

        fId = new JTextField();
        fId.setEditable(false);

        fUser = new JTextField();
        fPass = new JPasswordField();

        // Match your current role strings. If your DB uses OWNER/CASHIER/BARISTA, change values here.
        fRole = new JComboBox<>(new String[]{"OWNER", "CASHIER", "BARISTA"});

        styleField(fId);
        styleField(fUser);
        styleField(fPass);
        styleField(fRole);

        JButton btnNew = secondary("New");
        JButton btnRefresh = secondary("Refresh");
        JButton btnAdd = primary("Add");
        JButton btnUpdate = secondary("Update");
        JButton btnDelete = danger("Delete");

        btnNew.addActionListener(e -> clearUserForm());
        btnRefresh.addActionListener(e -> refreshUsersTableSafe());
        btnAdd.addActionListener(e -> onAddUser());
        btnUpdate.addActionListener(e -> onUpdateUser());
        btnDelete.addActionListener(e -> onDeleteUser());

        // Selecting row loads the form
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

        JLabel fh = new JLabel("Create / Edit User");
        fh.setFont(new Font("SansSerif", Font.BOLD, 14));
        fh.setForeground(new Color(33, 37, 41));

        g.gridy = 0; g.insets = new Insets(0, 0, 12, 0);
        form.add(fh, g);

        g.gridy = 1; g.insets = new Insets(0, 0, 6, 0);
        form.add(label("ID"), g);
        g.gridy = 2; g.insets = new Insets(0, 0, 12, 0);
        form.add(fId, g);

        g.gridy = 3; g.insets = new Insets(0, 0, 6, 0);
        form.add(label("Username"), g);
        g.gridy = 4; g.insets = new Insets(0, 0, 12, 0);
        form.add(fUser, g);

        g.gridy = 5; g.insets = new Insets(0, 0, 6, 0);
        form.add(label("Password"), g);
        g.gridy = 6; g.insets = new Insets(0, 0, 12, 0);
        form.add(fPass, g);

        g.gridy = 7; g.insets = new Insets(0, 0, 6, 0);
        form.add(label("Role"), g);
        g.gridy = 8; g.insets = new Insets(0, 0, 14, 0);
        form.add(fRole, g);

        JPanel actionsTop = new JPanel(new GridLayout(1, 2, 8, 8));
        actionsTop.setOpaque(false);
        actionsTop.add(btnNew);
        actionsTop.add(btnRefresh);

        JPanel actionsBottom = new JPanel(new GridLayout(1, 3, 8, 8));
        actionsBottom.setOpaque(false);
        actionsBottom.add(btnAdd);
        actionsBottom.add(btnUpdate);
        actionsBottom.add(btnDelete);

        g.gridy = 9; g.insets = new Insets(0, 0, 8, 0);
        form.add(actionsTop, g);

        g.gridy = 10; g.insets = new Insets(0, 0, 0, 0);
        form.add(actionsBottom, g);

        g.gridy = 11; g.weighty = 1;
        form.add(Box.createVerticalGlue(), g);

        JPanel center = new JPanel(new BorderLayout(12, 12));
        center.setOpaque(false);
        center.add(sp, BorderLayout.CENTER);
        center.add(form, BorderLayout.EAST);

        page.add(h, BorderLayout.NORTH);
        page.add(center, BorderLayout.CENTER);

        // Initial load from DB
        refreshUsersTableSafe();

        return page;
    }

    private JComponent buildPlaceholderPage(String title, String text) {
        JPanel page = new JPanel(new BorderLayout(12, 12));
        page.setBackground(new Color(245, 247, 250));

        JLabel h = new JLabel(title);
        h.setFont(new Font("SansSerif", Font.BOLD, 18));
        h.setForeground(new Color(33, 37, 41));

        page.add(h, BorderLayout.NORTH);
        page.add(bigPlaceholder(title, text), BorderLayout.CENTER);
        return page;
    }

    // ------------------- CRUD handlers -------------------

    private void refreshUsersTableSafe() {
        try {
            refreshUsersTable();
        } catch (Exception ex) {
            showDbError(ex);
        }
    }

    private void refreshUsersTable() throws Exception {
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
            fPass.setText(u.getPassword()); // DB currently stores plain text (same as your login)
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

    // ------------------- UI helpers -------------------

    private JButton navButton(String text) {
        JButton b = new JButton(text);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setFont(new Font("SansSerif", Font.PLAIN, 13));
        b.setBackground(new Color(248, 249, 250));
        b.setForeground(new Color(33, 37, 41));
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(233, 236, 239), 1),
                new EmptyBorder(10, 12, 10, 12)
        ));
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        return b;
    }

    private JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 12));
        l.setForeground(new Color(108, 117, 125));
        l.setBorder(new EmptyBorder(0, 8, 10, 8));
        return l;
    }

    private JPanel statCard(String title, String value, String note) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(233, 236, 239), 1),
                new EmptyBorder(14, 14, 14, 14)
        ));

        JLabel t = new JLabel(title);
        t.setFont(new Font("SansSerif", Font.PLAIN, 12));
        t.setForeground(new Color(108, 117, 125));

        JLabel v = new JLabel(value);
        v.setFont(new Font("SansSerif", Font.BOLD, 22));
        v.setForeground(new Color(33, 37, 41));

        JLabel n = new JLabel(note);
        n.setFont(new Font("SansSerif", Font.PLAIN, 12));
        n.setForeground(new Color(173, 181, 189));

        card.add(t, BorderLayout.NORTH);
        card.add(v, BorderLayout.CENTER);
        card.add(n, BorderLayout.SOUTH);
        return card;
    }

    private JPanel bigPlaceholder(String title, String body) {
        JPanel box = new JPanel(new BorderLayout(8, 8));
        box.setBackground(Color.WHITE);
        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(233, 236, 239), 1),
                new EmptyBorder(18, 18, 18, 18)
        ));

        JLabel t = new JLabel(title);
        t.setFont(new Font("SansSerif", Font.BOLD, 14));
        t.setForeground(new Color(33, 37, 41));

        JTextArea a = new JTextArea(body);
        a.setFont(new Font("SansSerif", Font.PLAIN, 13));
        a.setForeground(new Color(73, 80, 87));
        a.setOpaque(false);
        a.setEditable(false);
        a.setLineWrap(true);
        a.setWrapStyleWord(true);

        box.add(t, BorderLayout.NORTH);
        box.add(a, BorderLayout.CENTER);
        return box;
    }

    private JLabel label(String text) {
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
        c.setPreferredSize(new Dimension(10, 42));
    }

    private JButton primary(String text) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setFont(new Font("SansSerif", Font.BOLD, 12));
        b.setBackground(new Color(32, 85, 197));
        b.setForeground(Color.WHITE);
        b.setBorder(new EmptyBorder(10, 12, 10, 12));
        return b;
    }

    private JButton secondary(String text) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setFont(new Font("SansSerif", Font.BOLD, 12));
        b.setBackground(new Color(248, 249, 250));
        b.setForeground(new Color(33, 37, 41));
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(233, 236, 239), 1),
                new EmptyBorder(10, 12, 10, 12)
        ));
        return b;
    }

    private JButton danger(String text) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setFont(new Font("SansSerif", Font.BOLD, 12));
        b.setBackground(new Color(220, 53, 69));
        b.setForeground(Color.WHITE);
        b.setBorder(new EmptyBorder(10, 12, 10, 12));
        return b;
    }

    // ------------------- Quick test -------------------
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            new OwnerFrame("owner").setVisible(true);
        });
    }
}
