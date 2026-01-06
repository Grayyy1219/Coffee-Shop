package app.ui;

import app.db.AssetService;
import app.db.OrderDAO;
import app.model.Order;
import app.model.OrderItem;
import app.model.OrderQueue;
import app.util.LinearSearch;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Barista workspace focused on the active order queue.
 */
public class BaristaPanel extends JPanel {

    private final boolean previewMode;
    private final String username;
    private final String shopName;

    private final OrderDAO orderDAO = new OrderDAO();
    private OrderQueue orderQueue = new OrderQueue();

    private final DefaultListModel<String> queueModel = new DefaultListModel<>();
    private final List<Order> currentQueueView = new ArrayList<>();

    private JList<String> queueList;
    private JLabel lblQueueCount;
    private JLabel lblStatus;
    private JLabel lblCode;
    private JLabel lblCustomer;
    private JLabel lblTotal;
    private JLabel lblOrderStatus;
    private JLabel lblCreated;
    private DefaultTableModel itemsModel;
    private JTextField searchCustomer;
    private JTextField searchCode;
    private JButton btnInProgress;
    private JButton btnServe;
    private JButton btnDetails;

    private static final Color BG = new Color(243, 245, 249);
    private static final Color SURFACE = Color.WHITE;
    private static final Color BORDER = new Color(226, 232, 240);
    private static final Color TEXT = new Color(30, 41, 59);
    private static final Color MUTED = new Color(100, 116, 139);
    private static final Color SUCCESS = new Color(22, 163, 74);
    private static final Color WARN = new Color(234, 179, 8);
    private static final Color TABLE_HEADER_BG = new Color(15, 23, 42);
    private static final Color TABLE_HEADER_TEXT = new Color(226, 232, 240);
    private static final Color TABLE_ROW_ALT = new Color(248, 250, 252);

    private final Color primary = new AssetService().getAccentColorOrDefault();
    private final Color tableSelection = tint(primary, 0.74);

    private static final NumberFormat MONEY_PH = NumberFormat.getCurrencyInstance(new Locale("en", "PH"));

    public BaristaPanel(boolean previewMode, String username, String shopName) {
        this.previewMode = previewMode;
        this.username = username == null ? "barista" : username;
        this.shopName = shopName == null ? "Coffee Shop" : shopName;

        setLayout(new BorderLayout(12, 12));
        setBackground(BG);
        setBorder(new EmptyBorder(14, 14, 14, 14));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildWorkspace(), BorderLayout.CENTER);

        loadActiveQueueFromDatabase();
    }

    private JComponent buildHeader() {
        JPanel head = new JPanel(new BorderLayout());
        head.setBackground(SURFACE);
        head.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
                new EmptyBorder(14, 18, 14, 18)
        ));

        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));

        JLabel title = new JLabel(previewMode ? "Barista View (Preview)" : "Barista Workspace");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setForeground(TEXT);

        JLabel subtitle = new JLabel("Active queue monitor and serve flow.");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subtitle.setForeground(MUTED);

        titles.add(title);
        titles.add(Box.createVerticalStrut(2));
        titles.add(subtitle);

        JLabel user = new JLabel(shopName + "  •  Logged in as: " + this.username + (previewMode ? " (read-only preview)" : ""));
        user.setFont(new Font("SansSerif", Font.PLAIN, 12));
        user.setForeground(MUTED);

        head.add(titles, BorderLayout.WEST);
        head.add(user, BorderLayout.EAST);
        return head;
    }

    private JComponent buildWorkspace() {
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setOpaque(false);

        JPanel queuePanel = surface(new EmptyBorder(14, 14, 14, 14));
        queuePanel.setLayout(new BorderLayout(10, 10));
        queuePanel.setPreferredSize(new Dimension(360, 10));

        JPanel queueHeader = new JPanel(new BorderLayout());
        queueHeader.setOpaque(false);
        JLabel queueTitle = new JLabel("Active Order Queue (FIFO)");
        queueTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        queueTitle.setForeground(TEXT);
        lblQueueCount = new JLabel("0 in queue");
        lblQueueCount.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblQueueCount.setForeground(MUTED);
        queueHeader.add(queueTitle, BorderLayout.WEST);
        queueHeader.add(lblQueueCount, BorderLayout.EAST);

        JPanel queueSearchRow = new JPanel(new GridLayout(2, 2, 8, 6));
        queueSearchRow.setOpaque(false);
        searchCustomer = new JTextField();
        searchCustomer.setToolTipText("Search by customer");
        searchCode = new JTextField();
        searchCode.setToolTipText("Search by order code");
        styleField(searchCustomer);
        styleField(searchCode);
        queueSearchRow.add(labeled("Customer", searchCustomer));
        queueSearchRow.add(labeled("Order Code", searchCode));

        JButton btnSearch = ghost("Search");
        btnSearch.addActionListener(e -> searchActiveOrders());
        JButton btnRefresh = ghost("Refresh queue");
        btnRefresh.addActionListener(e -> loadActiveQueueFromDatabase());

        JPanel queueActions = new JPanel(new GridLayout(1, 2, 8, 8));
        queueActions.setOpaque(false);
        queueActions.add(btnSearch);
        queueActions.add(btnRefresh);

        queueList = new JList<>(queueModel);
        queueList.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        queueList.addListSelectionListener(e -> {
            int idx = queueList.getSelectedIndex();
            if (idx >= 0 && idx < currentQueueView.size()) {
                renderDetails(currentQueueView.get(idx));
            }
        });
        queueList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() >= 2) {
                    int idx = queueList.locationToIndex(e.getPoint());
                    if (idx >= 0 && idx < currentQueueView.size()) {
                        showOrderModal(currentQueueView.get(idx));
                    }
                }
            }
        });

        JScrollPane queueScroll = new JScrollPane(queueList);
        queueScroll.setBorder(BorderFactory.createLineBorder(BORDER, 1));

        btnInProgress = primaryOutline("Mark In Progress");
        btnInProgress.addActionListener(e -> markInProgress());

        btnServe = primary("Serve Next");
        btnServe.addActionListener(e -> serveNext());

        btnDetails = ghost("View Details");
        btnDetails.addActionListener(e -> {
            Order sel = getSelectedOrder();
            if (sel != null) showOrderModal(sel);
            else setStatus("Select an order to view", WARN);
        });

        queuePanel.add(queueHeader, BorderLayout.NORTH);
        queuePanel.add(queueScroll, BorderLayout.CENTER);
        queuePanel.add(buildStack(queueSearchRow, queueActions, buildActionRow()), BorderLayout.SOUTH);

        JPanel detailPanel = surface(new EmptyBorder(14, 14, 14, 14));
        detailPanel.setLayout(new BorderLayout(10, 10));

        detailPanel.add(buildDetailHeader(), BorderLayout.NORTH);
        detailPanel.add(buildItemsTable(), BorderLayout.CENTER);

        lblStatus = new JLabel(" ");
        lblStatus.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblStatus.setForeground(MUTED);

        root.add(queuePanel, BorderLayout.WEST);
        root.add(detailPanel, BorderLayout.CENTER);
        root.add(lblStatus, BorderLayout.SOUTH);
        return root;
    }

    private JComponent buildDetailHeader() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 4, 8));
        panel.setOpaque(false);

        JPanel topRow = new JPanel(new GridLayout(1, 3, 8, 4));
        topRow.setOpaque(false);
        lblCode = mutedValue("Select an order");
        lblCustomer = mutedValue(" ");
        lblOrderStatus = mutedValue(" ");
        topRow.add(labeled("Order", lblCode));
        topRow.add(labeled("Customer", lblCustomer));
        topRow.add(labeled("Status", lblOrderStatus));

        JPanel bottomRow = new JPanel(new GridLayout(1, 2, 8, 4));
        bottomRow.setOpaque(false);
        lblTotal = mutedValue(MONEY_PH.format(0));
        lblCreated = mutedValue(" ");
        bottomRow.add(labeled("Total", lblTotal));
        bottomRow.add(labeled("Created", lblCreated));

        panel.add(topRow);
        panel.add(bottomRow);
        return panel;
    }

    private JComponent buildItemsTable() {
        itemsModel = new DefaultTableModel(new String[]{"Item", "Qty", "Options", "Line Total"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable table = new JTable(itemsModel);
        table.setRowHeight(26);
        styleTable(table);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(BORDER, 1));
        return sp;
    }

    private JPanel buildStack(JComponent searchRow, JComponent actions, JComponent serveButton) {
        JPanel stack = new JPanel();
        stack.setOpaque(false);
        stack.setLayout(new BoxLayout(stack, BoxLayout.Y_AXIS));
        stack.add(searchRow);
        stack.add(Box.createVerticalStrut(6));
        stack.add(actions);
        stack.add(Box.createVerticalStrut(10));
        stack.add(serveButton);
        return stack;
    }

    private JPanel buildActionRow() {
        JPanel row = new JPanel(new GridLayout(1, 3, 8, 8));
        row.setOpaque(false);
        row.add(btnDetails);
        row.add(btnInProgress);
        row.add(btnServe);
        return row;
    }

    // -------------------- Actions --------------------

    private void serveNext() {
        if (orderQueue.isEmpty()) {
            setStatus("No orders to serve", WARN);
            return;
        }

        Order next = orderQueue.peek();
        renderDetails(next);

        String summary = "Serve this order?\n" +
                "Order: " + next.getCode() + "\n" +
                "Customer: " + next.getCustomerName() + "\n" +
                "Total: " + MONEY_PH.format(next.getTotal());
        int choice = JOptionPane.showConfirmDialog(this, summary, "Confirm Serve", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (choice != JOptionPane.OK_OPTION) {
            setStatus("Serve cancelled", WARN);
            return;
        }

        if (!previewMode && next.getId() != null) {
            try {
                orderDAO.updateStatusToCompleted(next.getId());
                next.setStatus("COMPLETED");
                next.setPaid(true);
            } catch (Exception ex) {
                setStatus("DB error while marking order complete: " + ex.getMessage(), WARN);
            }
        }

        // Dequeue happens here after the barista confirms serving the next order.
        // This removes the head of the linked-list queue (FIFO: oldest order first).
        orderQueue.dequeue();
        refreshQueueList();
        setStatus("Served " + (next.getCode() == null ? "order" : next.getCode()), SUCCESS);
    }

    private void markInProgress() {
        Order order = getSelectedOrder();
        if (order == null) {
            setStatus("Select an order to update", WARN);
            return;
        }

        if ("IN_PROGRESS".equalsIgnoreCase(order.getStatus())) {
            setStatus("Order already in progress", primary);
            return;
        }
        if ("COMPLETED".equalsIgnoreCase(order.getStatus())) {
            setStatus("Completed orders cannot change status", WARN);
            return;
        }

        if (!previewMode && order.getId() != null) {
            try {
                orderDAO.updateStatusToInProgress(order.getId());
            } catch (Exception ex) {
                setStatus("DB error while marking in progress: " + ex.getMessage(), WARN);
                return;
            }
        }

        order.setStatus("IN_PROGRESS");
        refreshQueueList();
        setStatus("Marked " + (order.getCode() == null ? "order" : order.getCode()) + " as IN_PROGRESS", SUCCESS);
    }

    private void searchActiveOrders() {
        String customerQ = searchCustomer.getText() == null ? "" : searchCustomer.getText();
        String codeQ = searchCode.getText() == null ? "" : searchCode.getText();

        // Linear search triggered by the Search button in the barista queue panel.
        // Searches the current queue (linked list) for matching customer name/code.
        List<Order> matches = LinearSearch.search(orderQueue.traverse(), o ->
                o.getCustomerName().toLowerCase(Locale.ROOT).contains(customerQ.trim().toLowerCase(Locale.ROOT))
                        && (codeQ.isBlank() || (o.getCode() != null && o.getCode().toLowerCase(Locale.ROOT).contains(codeQ.trim().toLowerCase(Locale.ROOT))))
        );

        if (matches.isEmpty()) {
            setStatus("No matching active orders", WARN);
            return;
        }

        Order first = matches.get(0);
        int idx = currentQueueView.indexOf(first);
        if (idx >= 0) {
            queueList.setSelectedIndex(idx);
            queueList.ensureIndexIsVisible(idx);
        }
        setStatus("Highlighted " + matches.size() + " matching order(s)", primary);
    }

    private void loadActiveQueueFromDatabase() {
        orderQueue = new OrderQueue();
        try {
            List<Order> active = orderDAO.loadActiveOrders(OrderQueue.MAX_SIZE);
            for (Order order : active) {
                // Enqueue during DB sync: each active order is appended to the queue.
                // This preserves the DB order so the UI shows oldest -> newest.
                orderQueue.enqueue(order);
            }
            refreshQueueList();
            setStatus("Queue synced from database" + (previewMode ? " (preview mode)" : ""), primary);
        } catch (Exception ex) {
            refreshQueueList();
            setStatus("Queue fallback (DB unavailable): " + ex.getMessage(), WARN);
        }
    }

    public void refreshData() {
        loadActiveQueueFromDatabase();
    }

    // -------------------- UI updates --------------------

    private void refreshQueueList() {
        // Traversal here rebuilds the UI list from the linked-list queue.
        // The linked nodes are copied into a List so Swing can render them.
        rebuildQueueList(orderQueue.traverse());
    }

    private void rebuildQueueList(List<Order> orders) {
        queueModel.clear();
        currentQueueView.clear();
        currentQueueView.addAll(orders);
        for (Order o : orders) {
            queueModel.addElement(formatQueueLine(o));
        }
        updateQueueBadge();
        if (!orders.isEmpty()) {
            queueList.setSelectedIndex(0);
        } else {
            clearDetails();
        }
    }

    private String formatQueueLine(Order order) {
        String code = order.getCode() == null ? "Order" : order.getCode();
        String status = order.getStatus() == null ? "PENDING" : order.getStatus();
        return code + " • " + order.getCustomerName() + " • " + MONEY_PH.format(order.getTotal()) + " • " + status;
    }

    private void renderDetails(Order order) {
        lblCode.setText(order.getCode() == null ? "Order" : order.getCode());
        lblCustomer.setText(order.getCustomerName());
        lblOrderStatus.setText(order.getStatus() == null ? "PENDING" : order.getStatus());
        lblTotal.setText(MONEY_PH.format(order.getTotal()));
        lblCreated.setText(order.getCreatedAt() == null ? "" : order.getCreatedAt().toString());

        itemsModel.setRowCount(0);
        for (OrderItem item : order.getItems()) {
            itemsModel.addRow(new Object[]{
                    item.getItemName(),
                    item.getQuantity(),
                    item.getOptionsLabel(),
                    MONEY_PH.format(item.getLineTotal())
            });
        }
    }

    private Order getSelectedOrder() {
        int idx = queueList.getSelectedIndex();
        if (idx < 0 || idx >= currentQueueView.size()) return null;
        return currentQueueView.get(idx);
    }

    private void showOrderModal(Order order) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Order Details", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(new EmptyBorder(16, 18, 16, 18));
        root.setBackground(Color.WHITE);

        JPanel header = new JPanel(new GridLayout(2, 2, 10, 8));
        header.setOpaque(false);
        header.add(labeled("Order", valueLabel(order.getCode())));
        header.add(labeled("Customer", valueLabel(order.getCustomerName())));
        header.add(labeled("Status", valueLabel(order.getStatus())));
        header.add(labeled("Total", valueLabel(MONEY_PH.format(order.getTotal()))));

        DefaultTableModel model = new DefaultTableModel(new String[]{"Item", "Qty", "Options", "Line Total"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        for (OrderItem item : order.getItems()) {
            model.addRow(new Object[]{
                    item.getItemName(),
                    item.getQuantity(),
                    item.getOptionsLabel(),
                    MONEY_PH.format(item.getLineTotal())
            });
        }

        JTable table = new JTable(model);
        table.setRowHeight(26);
        styleTable(table);
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(BORDER, 1));

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setOpaque(false);
        JButton close = primary("Close");
        close.addActionListener(e -> dialog.dispose());
        footer.add(close);

        root.add(header, BorderLayout.NORTH);
        root.add(sp, BorderLayout.CENTER);
        root.add(footer, BorderLayout.SOUTH);

        dialog.setContentPane(root);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private JLabel valueLabel(String text) {
        JLabel l = new JLabel(text == null ? "" : text);
        l.setFont(new Font("SansSerif", Font.BOLD, 13));
        l.setForeground(TEXT);
        return l;
    }

    private void clearDetails() {
        lblCode.setText("Select an order");
        lblCustomer.setText(" ");
        lblOrderStatus.setText(" ");
        lblTotal.setText(MONEY_PH.format(0));
        lblCreated.setText(" ");
        itemsModel.setRowCount(0);
    }

    private void updateQueueBadge() {
        lblQueueCount.setText(orderQueue.size() + " in queue");
    }

    private void setStatus(String msg, Color color) {
        lblStatus.setForeground(color);
        lblStatus.setText(msg);
    }

    // -------------------- UI helpers --------------------

    private JPanel labeled(String label, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(6, 4));
        p.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setFont(new Font("SansSerif", Font.PLAIN, 12));
        l.setForeground(MUTED);
        p.add(l, BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private JLabel mutedValue(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 13));
        l.setForeground(TEXT);
        return l;
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

    private void styleField(JComponent field) {
        field.setFont(new Font("SansSerif", Font.PLAIN, 13));
        field.setBackground(new Color(248, 250, 255));
        field.setForeground(TEXT);
        field.setOpaque(true);
        field.setBorder(buildFieldBorder(false));
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) { field.setBorder(buildFieldBorder(true)); }
            @Override public void focusLost(java.awt.event.FocusEvent e) { field.setBorder(buildFieldBorder(false)); }
        });
    }

    private Border buildFieldBorder(boolean focused) {
        Color glow = new Color(primary.getRed(), primary.getGreen(), primary.getBlue(), focused ? 160 : 70);
        Color line = focused ? primary : BORDER;
        int thickness = focused ? 2 : 1;
        int pad = focused ? 8 : 9;
        return BorderFactory.createCompoundBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(glow, focused ? 3 : 1),
                        BorderFactory.createLineBorder(line, thickness)
                ),
                new EmptyBorder(pad, 10, pad, 10)
        );
    }

    private JButton primary(String text) {
        JButton b = new JButton(text);
        b.setBackground(primary);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        return b;
    }

    private JButton ghost(String text) {
        JButton b = new JButton(text);
        b.setBackground(Color.WHITE);
        b.setForeground(TEXT);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                new EmptyBorder(9, 12, 9, 12)
        ));
        return b;
    }

    private JButton primaryOutline(String text) {
        JButton b = new JButton(text);
        b.setBackground(Color.WHITE);
        b.setForeground(primary);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(primary, 1),
                new EmptyBorder(9, 12, 9, 12)
        ));
        return b;
    }

    private JPanel surface(EmptyBorder padding) {
        JPanel p = new JPanel();
        p.setBackground(SURFACE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                padding
        ));
        return p;
    }

    private static Color tint(Color color, double amount) {
        int r = clamp(color.getRed() + (int) ((255 - color.getRed()) * amount));
        int g = clamp(color.getGreen() + (int) ((255 - color.getGreen()) * amount));
        int b = clamp(color.getBlue() + (int) ((255 - color.getBlue()) * amount));
        return new Color(r, g, b);
    }

    private static int clamp(int value) {
        return Math.min(255, Math.max(0, value));
    }
}
