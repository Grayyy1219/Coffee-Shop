package Admin;

import coffeeshop.model.MenuItem;
import coffeeshop.model.Order;
import coffeeshop.order.OrderManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("MMM d, h:mm a");
    private static final Logger LOGGER = Logger.getLogger(RoleFlowFrame.class.getName());

    private final OrderManager orderManager;
    private final String role;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel actionsBaristaPanel;
    private javax.swing.JPanel actionsCashierPanel;
    private javax.swing.JPanel actionsOwnerMenuPanel;
    private javax.swing.JPanel actionsQueuePanel;
    private javax.swing.JPanel actionsReportsPanel1;
    private javax.swing.JTable baristaDetailArea;
    private javax.swing.JScrollPane baristaDetailScrollPane;
    private javax.swing.JPanel baristaPanel;
    private javax.swing.JScrollPane baristaQueueScrollPane;
    private javax.swing.JSplitPane baristaSplitContainer;
    private javax.swing.JTable baristaTable;
    private javax.swing.JPanel cashierCartPanel;
    private javax.swing.JPanel cashierMenuPanel;
    private javax.swing.JScrollPane cashierMenuPanel1;
    private javax.swing.JScrollPane cashierMenuScrollPane;
    private javax.swing.JPanel cashierPanel;
    private javax.swing.JTable cashierTable1;
    private javax.swing.JPanel headerPanel;
    private javax.swing.JLabel legendBaristaLabel;
    private javax.swing.JLabel legendCashierLabel;
    private javax.swing.JLabel legendOwnerLabel;
    private javax.swing.JPanel menuManagementPanel;
    private javax.swing.JScrollPane menuScrollPane;
    private javax.swing.JTable menuTable;
    private javax.swing.JPanel ownerPanel;
    private javax.swing.JTabbedPane ownerTabs;
    private javax.swing.JPanel queueMonitorPanel;
    private javax.swing.JScrollPane queueScrollPane;
    private javax.swing.JTable queueTable;
    private javax.swing.JPanel reportsPanel;
    private javax.swing.JScrollPane reportsScrollPane;
    private javax.swing.JTable reportsTable;
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
        configureUnknownRolePanel();
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
        menuScrollPane = new javax.swing.JScrollPane();
        menuTable = new javax.swing.JTable();
        queueMonitorPanel = new javax.swing.JPanel();
        actionsQueuePanel = new javax.swing.JPanel();
        queueScrollPane = new javax.swing.JScrollPane();
        queueTable = new javax.swing.JTable();
        reportsPanel = new javax.swing.JPanel();
        actionsReportsPanel1 = new javax.swing.JPanel();
        reportsScrollPane = new javax.swing.JScrollPane();
        reportsTable = new javax.swing.JTable();
        settingsPanel = new javax.swing.JPanel();
        legendOwnerLabel = new javax.swing.JLabel();
        cashierPanel = new javax.swing.JPanel();
        cashierMenuPanel = new javax.swing.JPanel();
        actionsCashierPanel = new javax.swing.JPanel();
        cashierMenuScrollPane = new javax.swing.JScrollPane();
        cashierTable1 = new javax.swing.JTable();
        cashierCartPanel = new javax.swing.JPanel();
        cashierMenuPanel1 = new javax.swing.JScrollPane();
        legendCashierLabel = new javax.swing.JLabel();
        baristaPanel = new javax.swing.JPanel();
        actionsBaristaPanel = new javax.swing.JPanel();
        baristaSplitContainer = new javax.swing.JSplitPane();
        baristaQueueScrollPane = new javax.swing.JScrollPane();
        baristaTable = new javax.swing.JTable();
        baristaDetailScrollPane = new javax.swing.JScrollPane();
        baristaDetailArea = new javax.swing.JTable();
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
        roleBadgeLabel.setText("Role: Unknown");
        roleBadgeLabel.setOpaque(true);
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

        menuTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        menuScrollPane.setViewportView(menuTable);

        menuManagementPanel.add(menuScrollPane, java.awt.BorderLayout.CENTER);

        ownerTabs.addTab("Menu", menuManagementPanel);

        queueMonitorPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        queueMonitorPanel.setLayout(new java.awt.BorderLayout());

        actionsQueuePanel.setOpaque(false);
        queueMonitorPanel.add(actionsQueuePanel, java.awt.BorderLayout.PAGE_START);

        queueTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        queueScrollPane.setViewportView(queueTable);

        queueMonitorPanel.add(queueScrollPane, java.awt.BorderLayout.CENTER);

        ownerTabs.addTab("Queue Monitor", queueMonitorPanel);

        reportsPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        reportsPanel.setLayout(new java.awt.BorderLayout());

        actionsReportsPanel1.setOpaque(false);
        reportsPanel.add(actionsReportsPanel1, java.awt.BorderLayout.PAGE_START);

        reportsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        reportsScrollPane.setViewportView(reportsTable);

        reportsPanel.add(reportsScrollPane, java.awt.BorderLayout.CENTER);

        ownerTabs.addTab("Reports", reportsPanel);

        settingsPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        settingsPanel.setLayout(new java.awt.GridBagLayout());
        ownerTabs.addTab("Settings", settingsPanel);

        ownerPanel.add(ownerTabs, java.awt.BorderLayout.CENTER);

        legendOwnerLabel.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        legendOwnerLabel.setForeground(new java.awt.Color(104, 104, 104));
        legendOwnerLabel.setText("Manager controls for CRUD, queue oversight, reporting, and system settings.");
        ownerPanel.add(legendOwnerLabel, java.awt.BorderLayout.PAGE_END);

        roleTabbedPane.addTab("Owner / Manager", ownerPanel);

        cashierPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 0, 0, 0));
        cashierPanel.setLayout(new java.awt.BorderLayout());

        cashierMenuPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        cashierMenuPanel.setLayout(new java.awt.BorderLayout());

        actionsCashierPanel.setOpaque(false);
        cashierMenuPanel.add(actionsCashierPanel, java.awt.BorderLayout.PAGE_START);

        cashierTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        cashierMenuScrollPane.setViewportView(cashierTable1);

        cashierMenuPanel.add(cashierMenuScrollPane, java.awt.BorderLayout.CENTER);

        cashierPanel.add(cashierMenuPanel, java.awt.BorderLayout.CENTER);

        cashierCartPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        cashierCartPanel.setLayout(new java.awt.BorderLayout());
        cashierCartPanel.add(cashierMenuPanel1, java.awt.BorderLayout.PAGE_END);

        cashierPanel.add(cashierCartPanel, java.awt.BorderLayout.EAST);

        legendCashierLabel.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        legendCashierLabel.setForeground(new java.awt.Color(104, 104, 104));
        legendCashierLabel.setText("Cashier builds cart, validates, writes to MySQL, then enqueues if queue < 50.");
        cashierPanel.add(legendCashierLabel, java.awt.BorderLayout.PAGE_END);

        roleTabbedPane.addTab("Cashier", cashierPanel);

        baristaPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 0, 0, 0));
        baristaPanel.setLayout(new java.awt.BorderLayout());

        actionsBaristaPanel.setOpaque(false);
        baristaPanel.add(actionsBaristaPanel, java.awt.BorderLayout.PAGE_START);

        baristaSplitContainer.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        baristaSplitContainer.setDividerLocation(650);
        baristaSplitContainer.setResizeWeight(0.65);
        baristaSplitContainer.setMaximumSize(new java.awt.Dimension(2, 2));
        baristaSplitContainer.setMinimumSize(new java.awt.Dimension(2, 2));
        baristaSplitContainer.setOpaque(false);

        baristaTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        baristaQueueScrollPane.setViewportView(baristaTable);

        baristaSplitContainer.setRightComponent(baristaQueueScrollPane);

        baristaDetailArea.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        baristaDetailScrollPane.setViewportView(baristaDetailArea);

        baristaSplitContainer.setRightComponent(baristaDetailScrollPane);

        baristaPanel.add(baristaSplitContainer, java.awt.BorderLayout.CENTER);

        legendBaristaLabel.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        legendBaristaLabel.setForeground(new java.awt.Color(104, 104, 104));
        legendBaristaLabel.setText("Queue is FIFO. Serve dequeues head, opens payment dialog, then refreshes.");
        baristaPanel.add(legendBaristaLabel, java.awt.BorderLayout.PAGE_END);

        roleTabbedPane.addTab("Barista", baristaPanel);

        unknownRolePanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        unknownRolePanel.setLayout(new java.awt.BorderLayout());
        roleTabbedPane.addTab("Unknown", unknownRolePanel);

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
        for (MenuItem item : loadMenuItems()) {
            model.addRow(new Object[]{
                item.getCode(),
                item.getName(),
                item.getCategory(),
                formatCurrency(item.getPrice()),
                "ACTIVE"
            });
        }
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
        for (Order order : loadActiveOrders()) {
            model.addRow(new Object[]{
                order.getOrderId(),
                order.getCustomerName(),
                formatCurrency(order.getTotal()),
                order.getStatus().name(),
                order.isPaid() ? "Yes" : "No",
                TIME_FORMAT.format(order.getCreatedAt())
            });
        }
        if (model.getRowCount() == 0) {
            model.addRow(new Object[]{"—", "No active orders found", "—", "—", "—", "—"});
        }
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
        List<Order> allOrders = loadAllOrders();
        double grossTotal = allOrders.stream().mapToDouble(Order::getTotal).sum();
        double paidTotal = allOrders.stream().filter(Order::isPaid).mapToDouble(Order::getTotal).sum();
        summary.add(summaryBadge("Gross Total", formatCurrency(grossTotal)));
        summary.add(summaryBadge("Paid Total", formatCurrency(paidTotal)));
        reportsPanel.add(summary, BorderLayout.SOUTH);

        DefaultTableModel model = new DefaultTableModel(new Object[]{"Order ID", "Customer", "Total", "Payment", "Paid Date"}, 0);
        for (Order order : allOrders) {
            model.addRow(new Object[]{
                order.getOrderId(),
                order.getCustomerName(),
                formatCurrency(order.getTotal()),
                order.isPaid() ? "Paid" : "Unpaid",
                order.isPaid() ? TIME_FORMAT.format(order.getCreatedAt()) : "—"
            });
        }
        if (model.getRowCount() == 0) {
            model.addRow(new Object[]{"—", "No orders available", "—", "—", "—"});
        }
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
        for (MenuItem item : loadMenuItems()) {
            menuModel.addRow(new Object[]{
                item.getCode(),
                item.getName(),
                item.getCategory(),
                formatCurrency(item.getPrice())
            });
        }
        cashierTable1.setModel(menuModel);
        cashierTable1.setRowHeight(26);
        cashierTable1.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        cashierTable1.setFont(new Font("SansSerif", Font.PLAIN, 12));

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

        JPanel actions = new JPanel();
        actions.setOpaque(false);
        JButton addToQueue = primaryButton("Add to Queue");
        JButton clearCart = secondaryButton("Clear Cart");
        actions.add(addToQueue);
        actions.add(clearCart);

        JTextArea tip = tipArea("Add validates non-empty cart, inserts orders + items into MySQL, then enqueues if queue not full.");

    }

    private void populateBaristaPanels() {
        actionsBaristaPanel.removeAll();
        JButton serveNext = primaryButton("Serve Next");
        JButton refresh = secondaryButton("Refresh");
        JTextField search = searchField("Search active orders (linear)");
        Arrays.asList(serveNext, refresh, search).forEach(actionsBaristaPanel::add);

        DefaultTableModel model = new DefaultTableModel(new Object[]{"Order ID", "Customer", "Total", "Status", "Paid"}, 0);
        for (Order order : loadActiveOrders()) {
            model.addRow(new Object[]{
                order.getOrderId(),
                order.getCustomerName(),
                formatCurrency(order.getTotal()),
                order.getStatus().name(),
                order.isPaid() ? "Yes" : "No"
            });
        }
        if (model.getRowCount() == 0) {
            model.addRow(new Object[]{"—", "No active orders found", "—", "—", "—"});
        }
        baristaTable.setModel(model);
        baristaTable.setRowHeight(26);
        baristaTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        baristaTable.setFont(new Font("SansSerif", Font.PLAIN, 12));
        
        baristaDetailArea.setForeground(SOFT_TEXT);
        baristaDetailScrollPane.setPreferredSize(new Dimension(280, 0));
//        actionsBaristaPanel.setText("Detail pane shows selected order items, totals, timestamps for prep.");


        baristaSplitContainer.setOpaque(false);
        baristaSplitContainer.setBorder(BorderFactory.createEmptyBorder());
        baristaSplitContainer.setResizeWeight(0.65);
        baristaSplitContainer.setDividerLocation(0.6);
        baristaSplitContainer.setLeftComponent(baristaQueueScrollPane);
        baristaSplitContainer.setRightComponent(baristaDetailScrollPane);
    }

    private List<MenuItem> loadMenuItems() {
        if (orderManager == null) {
            return List.of();
        }
        try {
            return orderManager.loadMenuItems();
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "Unable to load menu items from database", ex);
            return List.of();
        }
    }

    private List<Order> loadActiveOrders() {
        if (orderManager == null) {
            return List.of();
        }
        try {
            orderManager.loadActiveOrders();
            return orderManager.getActiveOrders();
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "Unable to load active orders from database", ex);
            return List.of();
        }
    }

    private List<Order> loadAllOrders() {
        if (orderManager == null) {
            return List.of();
        }
        try {
            return orderManager.loadAllOrders();
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "Unable to load orders from database", ex);
            return List.of();
        }
    }

    private String formatCurrency(double value) {
        return String.format("$%.2f", value);
    }

    private void applyRoleVisibility() {
        String normalizedRole = role.toLowerCase();
        for (int i = 0; i < roleTabbedPane.getTabCount(); i++) {
            roleTabbedPane.setEnabledAt(i, true);
            roleTabbedPane.setComponentAt(i, switch (i) {
                case 0 -> ownerPanel;
                case 1 -> cashierPanel;
                case 2 -> baristaPanel;
                default -> unknownRolePanel;
            });
        }

        switch (normalizedRole) {
            case "owner" -> roleTabbedPane.setSelectedComponent(ownerPanel);
            case "manager" -> {
                roleTabbedPane.setSelectedComponent(ownerPanel);
                disableTabsExcept(ownerPanel);
            }
            case "cashier" -> {
                roleTabbedPane.setSelectedComponent(cashierPanel);
                disableTabsExcept(cashierPanel);
            }
            case "barista" -> {
                roleTabbedPane.setSelectedComponent(baristaPanel);
                disableTabsExcept(baristaPanel);
            }
            default -> {
                roleTabbedPane.setSelectedComponent(unknownRolePanel);
                disableTabsExcept(unknownRolePanel);
            }
        }
    }

    private void disableTabsExcept(JPanel activePanel) {
        for (int i = 0; i < roleTabbedPane.getTabCount(); i++) {
            boolean isActive = roleTabbedPane.getComponentAt(i) == activePanel;
            roleTabbedPane.setEnabledAt(i, isActive);
        }
    }

    private void configureUnknownRolePanel() {
        unknownRolePanel.removeAll();
        unknownRolePanel.setLayout(new BorderLayout());
        unknownRolePanel.setBackground(CARD_COLOR);
        unknownRolePanel.setBorder(createCardBorder());
        JLabel label = new JLabel("This role is not configured for specific privileges. Please contact an owner.", SwingConstants.CENTER);
        label.setFont(new Font("SansSerif", Font.PLAIN, 14));
        label.setForeground(SOFT_TEXT);
        unknownRolePanel.add(label, BorderLayout.CENTER);
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







