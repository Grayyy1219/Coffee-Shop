package Admin;

import coffeeshop.order.OrderManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Arrays;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

/**
 * GUI form for the role-based workspace defined in docs/system_design.md.
 *
 * <p>
 * The layout is declared through NetBeans-compatible form metadata so it can be
 * edited visually while retaining the role-based visibility rules previously
 * implemented in code. Each tab mirrors the expected workflow for owner,
 * cashier, and barista roles, with an additional fallback for unrecognized
 * roles.
 */
public class RoleFlowFrame extends javax.swing.JFrame {

    private static final Color BACKGROUND_COLOR = new Color(246, 247, 249);
    private static final Color CARD_COLOR = Color.WHITE;
    private static final Color ACCENT_COLOR = new Color(55, 96, 146);
    private static final Color SOFT_BORDER = new Color(220, 223, 230);
    private static final Color SOFT_TEXT = new Color(68, 68, 68);

    private final OrderManager orderManager;
    private final String role;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel actionsBaristaPanel;
    private javax.swing.JPanel actionsCashierPanel;
    private javax.swing.JPanel actionsOwnerMenuPanel;
    private javax.swing.JPanel actionsQueuePanel;
    private javax.swing.JPanel baristaPanel;
    private javax.swing.JPanel baristaSplitContainer;
    private javax.swing.JPanel cashierCartPanel;
    private javax.swing.JPanel cashierMenuPanel;
    private javax.swing.JPanel cashierPanel;
    private javax.swing.JPanel headerPanel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JLabel legendBaristaLabel;
    private javax.swing.JLabel legendCashierLabel;
    private javax.swing.JLabel legendOwnerLabel;
    private javax.swing.JPanel menuManagementPanel;
    private javax.swing.JPanel ownerPanel;
    private javax.swing.JTabbedPane ownerTabs;
    private javax.swing.JPanel queueMonitorPanel;
    private javax.swing.JPanel reportsPanel;
    private javax.swing.JLabel roleBadgeLabel;
    private javax.swing.JTabbedPane roleTabbedPane;
    private javax.swing.JPanel settingsPanel;
    private javax.swing.JLabel subtitleLabel;
    private javax.swing.JPanel titleContainer;
    private javax.swing.JLabel titleLabel;
    private javax.swing.JPanel unknownRolePanel;
    // End of variables declaration//GEN-END:variables

    public RoleFlowFrame(OrderManager orderManager, String role) {
        this.orderManager = orderManager;
        this.role = role == null ? "" : role.trim();
        initComponents();
        configureHeader();
        populateOwnerPanels();
        populateCashierPanels();
        populateBaristaPanels();
        applyRoleVisibility();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        headerPanel = new javax.swing.JPanel();
        titleContainer = new javax.swing.JPanel();
        titleLabel = new javax.swing.JLabel();
        subtitleLabel = new javax.swing.JLabel();
        roleBadgeLabel = new javax.swing.JLabel();
        roleTabbedPane = new javax.swing.JTabbedPane();
        ownerPanel = new javax.swing.JPanel();
        ownerTabs = new javax.swing.JTabbedPane();
        menuManagementPanel = new javax.swing.JPanel();
        actionsOwnerMenuPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        queueMonitorPanel = new javax.swing.JPanel();
        actionsQueuePanel = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        reportsPanel = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        settingsPanel = new javax.swing.JPanel();
        legendOwnerLabel = new javax.swing.JLabel();
        cashierPanel = new javax.swing.JPanel();
        cashierMenuPanel = new javax.swing.JPanel();
        actionsCashierPanel = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        cashierCartPanel = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        legendCashierLabel = new javax.swing.JLabel();
        baristaPanel = new javax.swing.JPanel();
        actionsBaristaPanel = new javax.swing.JPanel();
        baristaSplitContainer = new javax.swing.JPanel();
        jScrollPane6 = new javax.swing.JScrollPane();
        jScrollPane7 = new javax.swing.JScrollPane();
        legendBaristaLabel = new javax.swing.JLabel();
        unknownRolePanel = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Coffee Shop Role Flows");
        setPreferredSize(new java.awt.Dimension(1100, 720));

        headerPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(16, 16, 8, 16));
        headerPanel.setLayout(new java.awt.BorderLayout());

        titleContainer.setOpaque(false);
        titleContainer.setLayout(new java.awt.BorderLayout());

        titleLabel.setFont(new java.awt.Font("SansSerif", 1, 22)); // NOI18N
        titleLabel.setText("Coffee Shop Desktop – Role Navigation");
        titleContainer.add(titleLabel, java.awt.BorderLayout.PAGE_START);

        subtitleLabel.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        subtitleLabel.setForeground(new java.awt.Color(104, 104, 104));
        subtitleLabel.setText("Blueprint from docs/system_design.md. Offline Swing + Local MySQL");
        titleContainer.add(subtitleLabel, java.awt.BorderLayout.CENTER);

        headerPanel.add(titleContainer, java.awt.BorderLayout.CENTER);

        roleBadgeLabel.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        roleBadgeLabel.setForeground(new java.awt.Color(104, 104, 104));
        roleBadgeLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        roleBadgeLabel.setOpaque(true);
        roleBadgeLabel.setText("Role: Unknown");
        headerPanel.add(roleBadgeLabel, java.awt.BorderLayout.EAST);

        getContentPane().add(headerPanel, java.awt.BorderLayout.PAGE_START);

        roleTabbedPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 16, 16, 16));
        roleTabbedPane.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N

        ownerPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 0, 0, 0));
        ownerPanel.setLayout(new java.awt.BorderLayout());

        ownerTabs.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N

        menuManagementPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        menuManagementPanel.setLayout(new java.awt.BorderLayout());

        actionsOwnerMenuPanel.setOpaque(false);
        menuManagementPanel.add(actionsOwnerMenuPanel, java.awt.BorderLayout.PAGE_START);
        menuManagementPanel.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        ownerTabs.addTab("tab1", menuManagementPanel);

        queueMonitorPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        queueMonitorPanel.setLayout(new java.awt.BorderLayout());

        actionsQueuePanel.setOpaque(false);
        queueMonitorPanel.add(actionsQueuePanel, java.awt.BorderLayout.PAGE_START);
        queueMonitorPanel.add(jScrollPane2, java.awt.BorderLayout.CENTER);

        ownerTabs.addTab("tab2", queueMonitorPanel);

        reportsPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        reportsPanel.setLayout(new java.awt.BorderLayout());
        reportsPanel.add(jScrollPane3, java.awt.BorderLayout.CENTER);

        ownerTabs.addTab("tab3", reportsPanel);

        settingsPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        settingsPanel.setLayout(new java.awt.GridBagLayout());
        ownerTabs.addTab("tab4", settingsPanel);

        ownerPanel.add(ownerTabs, java.awt.BorderLayout.CENTER);

        legendOwnerLabel.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        legendOwnerLabel.setForeground(new java.awt.Color(104, 104, 104));
        legendOwnerLabel.setText("Manager controls for CRUD, queue oversight, reporting, and system settings.");
        ownerPanel.add(legendOwnerLabel, java.awt.BorderLayout.PAGE_END);

        roleTabbedPane.addTab("tab1", ownerPanel);

        cashierPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 0, 0, 0));
        cashierPanel.setLayout(new java.awt.BorderLayout());

        cashierMenuPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        cashierMenuPanel.setLayout(new java.awt.BorderLayout());

        actionsCashierPanel.setOpaque(false);
        cashierMenuPanel.add(actionsCashierPanel, java.awt.BorderLayout.PAGE_START);
        cashierMenuPanel.add(jScrollPane4, java.awt.BorderLayout.CENTER);

        cashierPanel.add(cashierMenuPanel, java.awt.BorderLayout.CENTER);

        cashierCartPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        cashierCartPanel.setLayout(new java.awt.BorderLayout());
        cashierCartPanel.add(jScrollPane5, java.awt.BorderLayout.CENTER);

        cashierPanel.add(cashierCartPanel, java.awt.BorderLayout.EAST);

        legendCashierLabel.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        legendCashierLabel.setForeground(new java.awt.Color(104, 104, 104));
        legendCashierLabel.setText("Cashier builds cart, validates, writes to MySQL, then enqueues if queue < 50.");
        cashierPanel.add(legendCashierLabel, java.awt.BorderLayout.PAGE_END);

        roleTabbedPane.addTab("tab2", cashierPanel);

        baristaPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 0, 0, 0));
        baristaPanel.setLayout(new java.awt.BorderLayout());

        actionsBaristaPanel.setOpaque(false);
        baristaPanel.add(actionsBaristaPanel, java.awt.BorderLayout.PAGE_START);

        baristaSplitContainer.setOpaque(false);
        baristaSplitContainer.setLayout(new java.awt.BorderLayout());
        baristaSplitContainer.add(jScrollPane6, java.awt.BorderLayout.CENTER);
        baristaSplitContainer.add(jScrollPane7, java.awt.BorderLayout.EAST);

        baristaPanel.add(baristaSplitContainer, java.awt.BorderLayout.CENTER);

        legendBaristaLabel.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        legendBaristaLabel.setForeground(new java.awt.Color(104, 104, 104));
        legendBaristaLabel.setText("Queue is FIFO. Serve dequeues head, opens payment dialog, then refreshes.");
        baristaPanel.add(legendBaristaLabel, java.awt.BorderLayout.PAGE_END);

        roleTabbedPane.addTab("tab3", baristaPanel);

        unknownRolePanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        unknownRolePanel.setLayout(new java.awt.BorderLayout());
        roleTabbedPane.addTab("tab4", unknownRolePanel);

        getContentPane().add(roleTabbedPane, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void configureHeader() {
        roleBadgeLabel.setText("Role: " + (role.isBlank() ? "Unknown" : role));
    }

    private void populateOwnerPanels() {
        populateMenuManagement();
        populateQueueMonitor();
        populateReportsPanel();
        populateSettingsPanel();
    }

    private void populateMenuManagement() {
        actionsOwnerMenuPanel.removeAll();
        JButton addButton = primaryButton("Add Item");
        JButton editButton = primaryButton("Edit Item");
        JButton deleteButton = secondaryButton("Delete Item");
        JTextField searchField = searchField("Linear search by name (in-memory)");
        JButton sortByName = secondaryButton("Sort: Name");
        JButton sortByPrice = secondaryButton("Sort: Price");
        JButton sortByCategory = secondaryButton("Sort: Category");
        Arrays.asList(addButton, editButton, deleteButton, searchField, sortByName, sortByPrice, sortByCategory)
                .forEach(actionsOwnerMenuPanel::add);

        DefaultTableModel model = new DefaultTableModel(new Object[]{"Code", "Name", "Category", "Price", "Status"}, 0);
        model.addRow(new Object[]{"CF001", "Latte", "Coffee", "$4.50", "ACTIVE"});
        model.addRow(new Object[]{"PT101", "Chocolate Croissant", "Pastry", "$3.25", "ACTIVE"});
        menuTable.setModel(model);
        menuTable.setRowHeight(26);
        menuTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        menuTable.setFont(new Font("SansSerif", Font.PLAIN, 12));
    }

    private void populateQueueMonitor() {
        actionsQueuePanel.removeAll();
        JButton refresh = primaryButton("Refresh");
        JButton serveNext = primaryButton("Serve Next");
        JButton markPaid = secondaryButton("Mark Paid");
        JTextField searchField = searchField("Search order id/name (linear)");
        Arrays.asList(refresh, serveNext, markPaid, searchField).forEach(actionsQueuePanel::add);

        DefaultTableModel model = new DefaultTableModel(new Object[]{"Order ID", "Customer", "Total", "Status", "Paid", "Placed"}, 0);
        model.addRow(new Object[]{"ORD-1042", "Amira", "$12.40", "PENDING", "No", "10:04 AM"});
        model.addRow(new Object[]{"ORD-1043", "Lee", "$8.10", "SERVED", "Yes", "10:06 AM"});
        queueTable.setModel(model);
        queueTable.setRowHeight(26);
        queueTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        queueTable.setFont(new Font("SansSerif", Font.PLAIN, 12));

        JTextArea tip = tipArea("Table reflects linked-list queue snapshot. Serve dequeues head then triggers payment.");
        queueMonitorPanel.add(tip, BorderLayout.SOUTH);
    }

    private void populateReportsPanel() {
        JPanel filters = new JPanel();
        filters.setOpaque(false);
        JTextField dateFrom = searchField("From (date)");
        JTextField dateTo = searchField("To (date)");
        JTextField textSearch = searchField("Order/customer search (linear)");
        JButton sortTotal = secondaryButton("Sort: Total");
        JButton sortDate = secondaryButton("Sort: Date");
        Arrays.asList(dateFrom, dateTo, textSearch, sortTotal, sortDate).forEach(filters::add);
        reportsPanel.add(filters, BorderLayout.NORTH);

        JPanel summary = new JPanel();
        summary.setOpaque(false);
        summary.add(summaryBadge("Gross Total", "$1,240.40"));
        summary.add(summaryBadge("Paid Total", "$1,180.10"));
        reportsPanel.add(summary, BorderLayout.SOUTH);

        DefaultTableModel model = new DefaultTableModel(new Object[]{"Order ID", "Customer", "Total", "Payment", "Paid Date"}, 0);
        model.addRow(new Object[]{"ORD-0998", "Chris", "$15.30", "Card", "Sep 20, 9:10 AM"});
        model.addRow(new Object[]{"ORD-0999", "Maria", "$7.80", "Cash", "Sep 20, 9:20 AM"});
        reportsTable.setModel(model);
        reportsTable.setRowHeight(26);
        reportsTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        reportsTable.setFont(new Font("SansSerif", Font.PLAIN, 12));
    }

    private void populateSettingsPanel() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel dbLabel = boldLabel("Database Connection");
        settingsPanel.add(dbLabel, gbc);
        gbc.gridx = 1;
        settingsPanel.add(badgeLabel("Connected", new Color(0, 128, 0)), gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel capacityLabel = boldLabel("Order Queue Capacity");
        settingsPanel.add(capacityLabel, gbc);
        gbc.gridx = 1;
        int activeOrders = orderManager == null ? 0 : orderManager.getActiveOrderCount();
        settingsPanel.add(badgeLabel(activeOrders + " / 50 Active", ACCENT_COLOR), gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        JTextArea tip = tipArea("All screens read/write local MySQL. Queue is capped at 50 active orders.");
        tip.setBackground(CARD_COLOR);
        tip.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        settingsPanel.add(tip, gbc);
    }

    private void populateCashierPanels() {
        actionsCashierPanel.removeAll();
        JTextField search = searchField("Search menu items (linear)");
        JButton sortName = secondaryButton("Sort: Name");
        JButton sortCategory = secondaryButton("Sort: Category");
        JButton sortPrice = secondaryButton("Sort: Price");
        Arrays.asList(search, sortName, sortCategory, sortPrice).forEach(actionsCashierPanel::add);

        DefaultTableModel menuModel = new DefaultTableModel(new Object[]{"Code", "Name", "Category", "Price"}, 0);
        menuModel.addRow(new Object[]{"CF001", "Latte", "Coffee", "$4.50"});
        menuModel.addRow(new Object[]{"CF002", "Espresso", "Coffee", "$3.20"});
        menuModel.addRow(new Object[]{"PT101", "Croissant", "Pastry", "$3.00"});
        cashierTable.setModel(menuModel);
        cashierTable.setRowHeight(26);
        cashierTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        cashierTable.setFont(new Font("SansSerif", Font.PLAIN, 12));

        DefaultTableModel cartModel = new DefaultTableModel(new Object[]{"Item", "Qty", "Line Total"}, 0);
        cartModel.addRow(new Object[]{"Latte", 2, "$9.00"});
        cartModel.addRow(new Object[]{"Croissant", 1, "$3.00"});
        cartTable.setModel(cartModel);
        cartTable.setRowHeight(26);
        cartTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        cartTable.setFont(new Font("SansSerif", Font.PLAIN, 12));

        JPanel totals = new JPanel(new GridBagLayout());
        totals.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.anchor = GridBagConstraints.WEST;
        totals.add(boldLabel("Subtotal"), gbc);
        gbc.gridx = 1;
        totals.add(boldLabel("$12.00"), gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        totals.add(boldLabel("Tax"), gbc);
        gbc.gridx = 1;
        totals.add(boldLabel("$0.96"), gbc);
        gbc.gridx = 0;
        gbc.gridy = 2;
        totals.add(boldLabel("Total"), gbc);
        gbc.gridx = 1;
        totals.add(boldLabel("$12.96"), gbc);
        cashierCartPanel.add(totals, BorderLayout.NORTH);

        JPanel actions = new JPanel();
        actions.setOpaque(false);
        JButton addToQueue = primaryButton("Add to Queue");
        JButton clearCart = secondaryButton("Clear Cart");
        actions.add(addToQueue);
        actions.add(clearCart);
        cashierCartPanel.add(actions, BorderLayout.SOUTH);

        JTextArea tip = tipArea("Add validates non-empty cart, inserts orders + items into MySQL, then enqueues if queue not full.");
        cashierCartPanel.add(tip, BorderLayout.EAST);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, cashierMenuPanel, cashierCartPanel);
        split.setResizeWeight(0.55);
        cashierPanel.removeAll();
        cashierPanel.add(split, BorderLayout.CENTER);
        cashierPanel.add(legendCashierLabel, BorderLayout.SOUTH);
    }

    private void populateBaristaPanels() {
        actionsBaristaPanel.removeAll();
        JButton serveNext = primaryButton("Serve Next");
        JButton refresh = secondaryButton("Refresh");
        JTextField search = searchField("Search active orders (linear)");
        Arrays.asList(serveNext, refresh, search).forEach(actionsBaristaPanel::add);

        DefaultTableModel model = new DefaultTableModel(new Object[]{"Order ID", "Customer", "Total", "Status", "Paid"}, 0);
        model.addRow(new Object[]{"ORD-1042", "Amira", "$12.40", "PENDING", "No"});
        model.addRow(new Object[]{"ORD-1043", "Lee", "$8.10", "SERVED", "Yes"});
        baristaTable.setModel(model);
        baristaTable.setRowHeight(26);
        baristaTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        baristaTable.setFont(new Font("SansSerif", Font.PLAIN, 12));

        baristaDetailArea.setText("Detail pane shows selected order items, totals, timestamps for prep.");
        baristaDetailArea.setForeground(SOFT_TEXT);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(baristaTable), new JScrollPane(baristaDetailArea));
        split.setResizeWeight(0.65);
        split.setBorder(BorderFactory.createEmptyBorder());
        baristaPanel.remove(baristaSplitContainer);
        baristaPanel.add(split, BorderLayout.CENTER);
    }

    private void applyRoleVisibility() {
        String normalizedRole = role.toLowerCase();
        roleTabbedPane.removeAll();
        switch (normalizedRole) {
            case "owner" -> {
                roleTabbedPane.addTab("Owner / Manager", ownerPanel);
                roleTabbedPane.addTab("Cashier", cashierPanel);
                roleTabbedPane.addTab("Barista", baristaPanel);
            }
            case "manager" -> roleTabbedPane.addTab("Owner / Manager", ownerPanel);
            case "cashier" -> roleTabbedPane.addTab("Cashier", cashierPanel);
            case "barista" -> roleTabbedPane.addTab("Barista", baristaPanel);
            default -> roleTabbedPane.addTab("Role", buildUnknownRolePanel());
        }
        revalidate();
        repaint();
    }

    private JPanel buildUnknownRolePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_COLOR);
        panel.setBorder(createCardBorder());
        JLabel label = new JLabel("This role is not configured for specific privileges. Please contact an owner.", SwingConstants.CENTER);
        label.setFont(new Font("SansSerif", Font.PLAIN, 14));
        label.setForeground(SOFT_TEXT);
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }

    private JButton primaryButton(String text) {
        JButton button = new JButton(text);
        styleButton(button, ACCENT_COLOR, Color.WHITE);
        return button;
    }

    private JButton secondaryButton(String text) {
        JButton button = new JButton(text);
        styleButton(button, new Color(238, 241, 245), ACCENT_COLOR.darker());
        return button;
    }

    private void styleButton(JButton button, Color background, Color foreground) {
        button.setBackground(background);
        button.setForeground(foreground);
        button.setFocusPainted(false);
        button.setFont(button.getFont().deriveFont(Font.BOLD, 12f));
        button.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
    }

    private JTextField searchField(String placeholder) {
        JTextField field = new JTextField(14);
        field.putClientProperty("JTextField.placeholderText", placeholder);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(SOFT_BORDER),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        return field;
    }

    private JTextArea tipArea(String text) {
        JTextArea tip = new JTextArea(text);
        tip.setLineWrap(true);
        tip.setWrapStyleWord(true);
        tip.setEditable(false);
        tip.setForeground(SOFT_TEXT);
        tip.setBackground(CARD_COLOR);
        tip.setFont(new Font("SansSerif", Font.PLAIN, 12));
        tip.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        return tip;
    }

    private JLabel summaryBadge(String title, String value) {
        JLabel label = new JLabel(String.format("%s: %s", title, value));
        label.setOpaque(true);
        label.setBackground(new Color(238, 241, 245));
        label.setForeground(SOFT_TEXT);
        label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(SOFT_BORDER),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        return label;
    }

    private JLabel badgeLabel(String text, Color color) {
        JLabel badge = new JLabel(text);
        badge.setOpaque(true);
        badge.setBackground(color);
        badge.setForeground(Color.WHITE);
        badge.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        return badge;
    }

    private JLabel boldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 12));
        label.setForeground(SOFT_TEXT);
        return label;
    }

    private javax.swing.border.Border createCardBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(SOFT_BORDER),
                BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }
}
