package app.ui;

import app.db.AssetService;
import app.db.MenuItemDAO;
import app.db.OrderDAO;
import app.model.MenuItem;
import app.model.Order;
import app.model.OrderItem;
import app.model.OrderQueue;
import app.util.InsertionSort;
import app.util.LinearSearch;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.math.BigDecimal;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Standalone cashier workspace panel (usable for both owner preview and cashier login).
 * The panel is UI-only for now: it keeps sample menu data in-memory, tracks a cart,
 * generates a simple receipt, and simulates an active order queue capped at 50 items.
 */
public class CashierPanel extends JPanel {

    private final boolean previewMode;
    private final String username;
    private final String shopName;

    private final DefaultListModel<MenuItem> menuModel = new DefaultListModel<>();
    private final List<MenuItem> allMenuItems = new ArrayList<>();

    private final List<CartLine> cart = new ArrayList<>();
    private DefaultTableModel cartModel;
    private JTable cartTable;

    private final DefaultListModel<String> queueModel = new DefaultListModel<>();
    private OrderQueue orderQueue = new OrderQueue();
    private final List<Order> currentQueueView = new ArrayList<>();
    private Order editingOrder;
    private JList<String> queueList;
    private JLabel lblSubtotal;
    private JLabel lblTax;
    private JLabel lblTotal;
    private JLabel lblQueueCount;
    private JLabel lblStatus;
    private JTextArea receiptArea;
    private JTextField customerField;
    private JTextField orderSearchField;
    private JTextField orderCodeSearchField;
    private JComboBox<String> menuSortMode;
    private JTextField menuSearchField;
    private int hoveredMenuIndex = -1;

    private int orderCounter = 1000;

    private final MenuItemDAO menuItemDAO = new MenuItemDAO();
    private final OrderDAO orderDAO = new OrderDAO();
    private final Set<String> customizableCategories = Set.of("Coffee", "Tea", "Iced");

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
    private static final Color MENU_PANEL_BG = new Color(247, 249, 252);
    private static final Color MENU_PANEL_BORDER = new Color(226, 232, 240);
    private static final Color MENU_LIST_BG = new Color(242, 246, 251);
    private static final Color MENU_CARD_TOP = new Color(255, 255, 255);
    private static final Color MENU_CARD_BOTTOM = new Color(241, 245, 249);

    private final Color primary = new AssetService().getAccentColorOrDefault();
    private final Color primarySoft = tint(primary, 0.88);
    private final Color tableSelection = tint(primary, 0.74);
    private final Color menuAccent = primary;

    private static final NumberFormat MONEY_PH = NumberFormat.getCurrencyInstance(new Locale("en", "PH"));
    private static final int MENU_IMAGE_SIZE = 54;
    private final Map<String, ImageIcon> menuImageCache = new HashMap<>();

    public CashierPanel(boolean previewMode, String username, String shopName) {
        this.previewMode = previewMode;
        this.username = username == null ? "cashier" : username;
        this.shopName = shopName == null ? "Coffee Shop" : shopName;

        setLayout(new BorderLayout(12, 12));
        setBackground(BG);

        loadMenuFromDatabaseOrFallback();
        add(buildHeader(), BorderLayout.NORTH);
        add(buildWorkspace(), BorderLayout.CENTER);

        loadActiveQueueFromDatabase();
    }

    // -------------------- Header --------------------

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

        JLabel title = new JLabel(previewMode ? "Cashier View (Preview)" : "Cashier Workspace");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setForeground(TEXT);

        JLabel subtitle = new JLabel("Menu, cart, payments, and order queue.");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subtitle.setForeground(MUTED);

        titles.add(title);
        titles.add(Box.createVerticalStrut(2));
        titles.add(subtitle);

        JLabel user = new JLabel(shopName + "  •  Logged in as: " + username + (previewMode ? " (read-only preview)" : ""));
        user.setFont(new Font("SansSerif", Font.PLAIN, 12));
        user.setForeground(MUTED);

        head.add(titles, BorderLayout.WEST);
        head.add(user, BorderLayout.EAST);
        return head;
    }

    // -------------------- Workspace --------------------

    private JComponent buildWorkspace() {
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(14, 14, 14, 14));

        // Left: menu + search
        JPanel menuPanel = menuSurface(new EmptyBorder(16, 16, 16, 16));
        menuPanel.setLayout(new BorderLayout(10, 10));
        menuPanel.setPreferredSize(new Dimension(550, 10));

        JLabel menuTitle = new JLabel("Menu");
        menuTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        menuTitle.setForeground(menuAccent);

        menuSearchField = new JTextField();
        menuSearchField.setToolTipText("Search menu items");
        styleMenuField(menuSearchField);

        menuSortMode = new JComboBox<>(new String[]{"Name (A-Z)", "Price (Low-High)"});
        menuSortMode.setToolTipText("Selection sort: name or price");
        styleMenuField(menuSortMode);
        menuSortMode.addActionListener(e -> filterMenu(menuSearchField.getText()));

        menuSearchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filterMenu(menuSearchField.getText()); }
            @Override public void removeUpdate(DocumentEvent e) { filterMenu(menuSearchField.getText()); }
            @Override public void changedUpdate(DocumentEvent e) { filterMenu(menuSearchField.getText()); }
        });

        JPanel menuHeader = new JPanel();
        menuHeader.setLayout(new BoxLayout(menuHeader, BoxLayout.Y_AXIS));
        menuHeader.setOpaque(false);
        menuHeader.add(menuTitle);
        menuHeader.add(Box.createVerticalStrut(8));
        menuHeader.add(menuSearchField);
        menuHeader.add(Box.createVerticalStrut(6));
        menuHeader.add(menuSortMode);

        JList<MenuItem> menuList = new JList<>(menuModel);
        menuList.setCellRenderer(new MenuItemRenderer());
        menuList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        menuList.setFixedCellHeight(90);
        menuList.setFixedCellWidth(250);
        menuList.setLayoutOrientation(JList.VERTICAL);
        menuList.setVisibleRowCount(0);
        menuList.setBackground(MENU_LIST_BG);
        menuList.setForeground(TEXT);
        menuList.setOpaque(true);
        menuList.setBorder(new EmptyBorder(4, 4, 4, 4));
        menuList.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            MenuItem item = menuList.getSelectedValue();
            if (item != null) addItemToCart(item);
            menuList.clearSelection();
        });
        menuList.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(java.awt.event.MouseEvent e) {
                int index = menuList.locationToIndex(e.getPoint());
                if (index != hoveredMenuIndex) {
                    hoveredMenuIndex = index;
                    menuList.repaint();
                }
            }
        });
        menuList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (hoveredMenuIndex != -1) {
                    hoveredMenuIndex = -1;
                    menuList.repaint();
                }
            }
        });

        JScrollPane menuScroll = new JScrollPane(menuList);
        menuScroll.getViewport().setBackground(MENU_LIST_BG);
        menuScroll.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MENU_PANEL_BORDER, 1),
                new EmptyBorder(6, 6, 6, 6)
        ));

        menuPanel.add(menuHeader, BorderLayout.NORTH);
        menuPanel.add(menuScroll, BorderLayout.CENTER);

        // Center: cart + totals
        JPanel cartPanel = surface(new EmptyBorder(14, 14, 14, 14));
        cartPanel.setLayout(new BorderLayout(10, 10));

        JLabel cartTitle = new JLabel("Cart");
        cartTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        cartTitle.setForeground(TEXT);

        cartModel = new DefaultTableModel(new String[]{"Item", "Options", "Qty", "Price", "Line Total"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        cartTable = new JTable(cartModel);
        cartTable.setRowHeight(28);
        styleTable(cartTable);

        JScrollPane cartScroll = new JScrollPane(cartTable);
        cartScroll.setBorder(BorderFactory.createLineBorder(BORDER, 1));

        JPanel cartActions = new JPanel(new GridLayout(1, 4, 8, 8));
        cartActions.setOpaque(false);
        JButton btnMinus = ghost("- Qty");
        JButton btnPlus = ghost("+ Qty");
        JButton btnRemove = ghost("Remove");
        JButton btnCustomize = ghost("Edit Options");

        btnMinus.addActionListener(e -> adjustQuantity(-1));
        btnPlus.addActionListener(e -> adjustQuantity(1));
        btnRemove.addActionListener(e -> removeSelectedItem());
        btnCustomize.addActionListener(e -> customizeSelectedItem());

        cartActions.add(btnMinus);
        cartActions.add(btnPlus);
        cartActions.add(btnRemove);
        cartActions.add(btnCustomize);

        JPanel totals = new JPanel(new GridBagLayout());
        totals.setOpaque(false);
        totals.setPreferredSize(new Dimension(200, 200));
        GridBagConstraints t = new GridBagConstraints();
        t.gridx = 0;
        t.weightx = 1;
        t.fill = GridBagConstraints.HORIZONTAL;

        lblSubtotal = totalRow(totals, t, 0, "Subtotal");
        lblTax = totalRow(totals, t, 1, "VAT (0%)");
        lblTotal = totalRow(totals, t, 2, "Grand Total");

        JButton btnCheckout = primary("Checkout & Queue");
        btnCheckout.addActionListener(e -> checkout());

        t.gridy = 3; t.insets = new Insets(12, 0, 0, 0);
        totals.add(btnCheckout, t);

        JPanel cartFooter = new JPanel();
        cartFooter.setLayout(new BoxLayout(cartFooter, BoxLayout.Y_AXIS));
        cartFooter.setOpaque(false);
        customerField = new JTextField();
        customerField.setToolTipText("Capture customer for receipts/search");
        styleField(customerField);
        JPanel customerBox = labeled("Customer Name (optional)", customerField);
        customerBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        totals.setAlignmentX(Component.LEFT_ALIGNMENT);
        cartActions.setAlignmentX(Component.LEFT_ALIGNMENT);
        cartFooter.add(customerBox);
        cartFooter.add(Box.createVerticalStrut(8));
        cartFooter.add(totals);
        cartFooter.add(Box.createVerticalStrut(10));
        cartFooter.add(cartActions);

        cartPanel.add(cartTitle, BorderLayout.NORTH);
        cartPanel.add(cartScroll, BorderLayout.CENTER);
        cartPanel.add(cartFooter, BorderLayout.SOUTH);

        // Right: queue + receipt
        JPanel right = new JPanel(new BorderLayout(10, 10));
        right.setOpaque(false);
        right.setPreferredSize(new Dimension(360, 10));

        JPanel queuePanel = surface(new EmptyBorder(14, 14, 14, 14));
        queuePanel.setLayout(new BorderLayout(8, 8));
        queuePanel.setPreferredSize(new Dimension(10, 540));

        JPanel queueHeader = new JPanel(new BorderLayout());
        queueHeader.setOpaque(false);
        JLabel queueTitle = new JLabel("Order Queue (max 50)");
        queueTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        queueTitle.setForeground(TEXT);
        lblQueueCount = new JLabel("0 in queue");
        lblQueueCount.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblQueueCount.setForeground(MUTED);
        queueHeader.add(queueTitle, BorderLayout.WEST);
        queueHeader.add(lblQueueCount, BorderLayout.EAST);

        orderSearchField = new JTextField();
        orderSearchField.setToolTipText("Linear search by customer name");
        styleField(orderSearchField);

        orderCodeSearchField = new JTextField();
        orderCodeSearchField.setToolTipText("Optional order # / code filter");
        styleField(orderCodeSearchField);

        JPanel queueSearchRow = new JPanel(new GridLayout(2, 2, 8, 6));
        queueSearchRow.setOpaque(false);
        JLabel lblCust = new JLabel("Customer contains");
        lblCust.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblCust.setForeground(MUTED);
        JLabel lblId = new JLabel("Order # (optional)");
        lblId.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblId.setForeground(MUTED);
        queueSearchRow.add(lblCust);
        queueSearchRow.add(lblId);
        queueSearchRow.add(orderSearchField);
        queueSearchRow.add(orderCodeSearchField);

        JButton btnSearchQueue = ghost("Search Orders");
        btnSearchQueue.addActionListener(e -> searchActiveOrders());
        JButton btnResetQueue = ghost("Refresh Queue");
        btnResetQueue.addActionListener(e -> {
            orderSearchField.setText("");
            orderCodeSearchField.setText("");
            loadActiveQueueFromDatabase();
        });

        JPanel queueSearchButtons = new JPanel(new GridLayout(1, 2, 8, 8));
        queueSearchButtons.setOpaque(false);
        queueSearchButtons.add(btnSearchQueue);
        queueSearchButtons.add(btnResetQueue);

        JPanel queueHeaderWrap = new JPanel();
        queueHeaderWrap.setOpaque(false);
        queueHeaderWrap.setLayout(new BoxLayout(queueHeaderWrap, BoxLayout.Y_AXIS));
        queueHeaderWrap.add(queueHeader);
        queueHeaderWrap.add(Box.createVerticalStrut(8));
        queueHeaderWrap.add(queueSearchRow);
        queueHeaderWrap.add(Box.createVerticalStrut(6));
        queueHeaderWrap.add(queueSearchButtons);

        queueList = new JList<>(queueModel);
        queueList.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        queueList.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int idx = queueList.getSelectedIndex();
            if (idx >= 0 && idx < currentQueueView.size()) {
                Order selected = currentQueueView.get(idx);
                renderReceipt(selected);
                setStatus("Previewing " + selected.getCode(), primary);
            }
        });
        JScrollPane queueScroll = new JScrollPane(queueList);
        queueScroll.setBorder(BorderFactory.createLineBorder(BORDER, 1));

        JButton btnServe = primary("Edit Selected");
        btnServe.addActionListener(e -> editSelectedOrder());

        queuePanel.add(queueHeaderWrap, BorderLayout.NORTH);
        queuePanel.add(queueScroll, BorderLayout.CENTER);
        queuePanel.add(btnServe, BorderLayout.SOUTH);

        JPanel receiptPanel = surface(new EmptyBorder(14, 14, 14, 14));
        receiptPanel.setLayout(new BorderLayout(8, 8));

        JLabel recTitle = new JLabel("Receipt Preview");
        recTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        recTitle.setForeground(TEXT);

        receiptArea = new JTextArea();
        receiptArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        receiptArea.setEditable(false);
        receiptArea.setLineWrap(true);
        receiptArea.setWrapStyleWord(true);

        JScrollPane recScroll = new JScrollPane(receiptArea);
        recScroll.setBorder(BorderFactory.createLineBorder(BORDER, 1));

        receiptPanel.add(recTitle, BorderLayout.NORTH);
        receiptPanel.add(recScroll, BorderLayout.CENTER);

        right.add(queuePanel, BorderLayout.NORTH);
        right.add(receiptPanel, BorderLayout.CENTER);

        root.add(menuPanel, BorderLayout.WEST);
        root.add(cartPanel, BorderLayout.CENTER);
        root.add(right, BorderLayout.EAST);

        lblStatus = new JLabel("Ready");
        lblStatus.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblStatus.setForeground(new Color(32, 85, 197));
        root.add(lblStatus, BorderLayout.SOUTH);

        filterMenu("");
        refreshCartTable();
        updateQueueBadge();
        return root;
    }

    // -------------------- Actions --------------------

    private void addItemToCart(MenuItem item) {
        DrinkOptions opts;
        if (customizableCategories.contains(item.getCategory())) {
            opts = promptOptions(item, null);
            if (opts == null) return;
        } else {
            opts = DrinkOptions.standard();
        }

        for (CartLine line : cart) {
            if (line.item.getCode().equals(item.getCode()) && line.options.equals(opts)) {
                line.qty++;
                refreshCartTable();
        setStatus("Added another " + item.getName() + " (" + opts.label() + ")", primary);
                return;
            }
        }
        cart.add(new CartLine(item, opts, 1));
        refreshCartTable();
        setStatus("Added " + item.getName() + " (" + opts.label() + ")", primary);
    }

    private void adjustQuantity(int delta) {
        int row = cartTable.getSelectedRow();
        if (row < 0 || row >= cart.size()) {
            setStatus("Select an item first", WARN);
            return;
        }
        CartLine line = cart.get(row);
        line.qty = Math.max(1, line.qty + delta);
        refreshCartTable();
        setStatus("Updated quantity for " + line.item.getName(), primary);
    }

    private void removeSelectedItem() {
        int row = cartTable.getSelectedRow();
        if (row < 0 || row >= cart.size()) {
            setStatus("Select an item to remove", WARN);
            return;
        }
        CartLine removed = cart.remove(row);
        refreshCartTable();
        setStatus("Removed " + removed.item.getName(), primary);
    }

    private void customizeSelectedItem() {
        int row = cartTable.getSelectedRow();
        if (row < 0 || row >= cart.size()) {
            setStatus("Select an item to edit options", WARN);
            return;
        }
        CartLine line = cart.get(row);
        if (!customizableCategories.contains(line.item.getCategory())) {
            setStatus("Options not available for this category", WARN);
            return;
        }
        DrinkOptions updated = promptOptions(line.item, line.options);
        if (updated == null) return;

        // merge with any existing line with same item+options
        CartLine target = null;
        for (CartLine other : cart) {
            if (other != line && other.item.getCode().equals(line.item.getCode()) && other.options.equals(updated)) {
                target = other;
                break;
            }
        }

        if (target != null) {
            target.qty += line.qty;
            cart.remove(line);
        } else {
            line.options = updated;
        }

        refreshCartTable();
        setStatus("Updated options for " + line.item.getName() + " (" + updated.label() + ")", primary);
    }

    private void checkout() {
        if (cart.isEmpty()) {
            setStatus("Add at least one item to checkout.", WARN);
            return;
        }

        if (orderQueue.size() >= OrderQueue.MAX_SIZE) {
            setStatus("Queue full (50). Serve some orders first.", WARN);
            return;
        }

        double subtotalVal = cart.stream().mapToDouble(c -> priceWithOptions(c.item, c.options) * c.qty).sum();
        double taxVal = 0;
        double totalVal = subtotalVal;

        boolean editing = editingOrder != null;

        Order order = editing ? editingOrder : new Order();
        if (order.getCode() == null) {
            order.setCode(generateOrderCode());
        }
        order.setCustomerName(customerField.getText().isBlank() ? "Walk-in" : customerField.getText().trim());
        order.setSubtotal(BigDecimal.valueOf(subtotalVal));
        order.setTax(BigDecimal.valueOf(taxVal));
        order.setTotal(BigDecimal.valueOf(totalVal));
        if (order.getStatus() == null || "IN_PROGRESS".equalsIgnoreCase(order.getStatus())) {
            order.setStatus("PENDING");
        }
        order.setPaid(false);

        order.getItems().clear();
        for (CartLine line : cart) {
            OrderItem item = new OrderItem();
            item.setItemCode(line.item.getCode());
            item.setItemName(line.item.getName());
            item.setOptionsLabel(line.options.label());
            item.setQuantity(line.qty);
            BigDecimal unit = BigDecimal.valueOf(priceWithOptions(line.item, line.options));
            item.setUnitPrice(unit);
            item.setLineTotal(unit.multiply(BigDecimal.valueOf(line.qty)));
            order.addItem(item);
        }

        boolean dbOk = true;
        if (!previewMode) {
            try {
                if (editing && order.getId() == null) {
                    Integer existingId = orderDAO.findIdByCode(order.getCode());
                    if (existingId != null) {
                        order.setId(existingId);
                    }
                }
                if (editing && order.getId() != null) {
                    orderDAO.updateOrderWithItems(order);
                } else {
                    orderDAO.insertOrderWithItems(order);
                }
            } catch (Exception ex) {
                dbOk = false;
                logError("Failed to save order " + order.getCode(), ex);
                setStatus("DB issue while saving order: " + ex.getMessage() + " (queued locally)", WARN);
            }
        }

        // Enqueue happens here when the cashier checks out and adds a new order to FIFO.
        // The new order becomes the tail so earlier orders remain at the head.
        boolean accepted = orderQueue.enqueue(order);
        if (!accepted) {
            setStatus("Queue full (50). Serve some orders first.", WARN);
            return;
        }

        refreshQueueList();
        renderReceipt(order);
        cart.clear();
        refreshCartTable();
        editingOrder = null;
        setStatus((previewMode ? "Preview: " : "") + (editing ? "Updated order " : "Queued order ") + order.getCode() + (dbOk ? "" : " (not saved to DB)"), dbOk ? SUCCESS : WARN);
    }

    private void editSelectedOrder() {
        int idx = queueList.getSelectedIndex();
        if (idx < 0 || idx >= currentQueueView.size()) {
            setStatus("Select an order to edit", WARN);
            return;
        }

        Order target = currentQueueView.get(idx);
        String status = target.getStatus() == null ? "PENDING" : target.getStatus();
        if ("IN_PROGRESS".equalsIgnoreCase(status)) {
            setStatus("IN_PROGRESS orders can no longer be edited", WARN);
            return;
        }

        editingOrder = target;
        orderQueue.remove(target);
        refreshQueueList();

        loadOrderIntoCart(target);
        renderReceipt(target);
        setStatus("Editing order " + target.getCode() + " (update then Checkout & Queue)", primary);
    }

    private void loadOrderIntoCart(Order order) {
        cart.clear();
        customerField.setText(order.getCustomerName());

        for (OrderItem item : order.getItems()) {
            addCartLineFromOrderItem(item);
        }
        refreshCartTable();
    }

    private void addCartLineFromOrderItem(OrderItem item) {
        String code = item.getItemCode() == null ? "" : item.getItemCode();
        MenuItem menu = allMenuItems.stream()
                .filter(m -> m.getCode().equalsIgnoreCase(code))
                .findFirst()
                .orElse(new MenuItem(code, item.getItemName(), "Custom", item.getUnitPrice()));

        DrinkOptions options = new DrinkOptions("Standard", "N/A", false, false, false, item.getOptionsLabel());
        cart.add(new CartLine(menu, options, item.getQuantity()));
    }

    private void searchActiveOrders() {
        String customerQ = orderSearchField.getText() == null ? "" : orderSearchField.getText();
        String codeQ = orderCodeSearchField.getText() == null ? "" : orderCodeSearchField.getText();
        // Linear search entry point for active orders (triggered by Search button in queue panel).
        // Uses the in-memory queue traversal to scan each order one-by-one.
        List<Order> matches = LinearSearch.search(orderQueue.traverse(), o ->
                o.getCustomerName().toLowerCase(Locale.ROOT).contains(customerQ.trim().toLowerCase(Locale.ROOT))
                        && (codeQ.isBlank() || (o.getCode() != null && o.getCode().toLowerCase(Locale.ROOT).contains(codeQ.trim().toLowerCase(Locale.ROOT))))
        );

        List<Order> historyMatches = new ArrayList<>();
        if (!previewMode) {
            try {
                List<Order> historySource = orderDAO.searchOrders(customerQ.trim(), codeQ.trim(), 50);
                // Linear search entry point for order history (triggered by Search button, DB-backed).
                // Even after the DB query, we still linearly scan the returned list.
                historyMatches = LinearSearch.search(historySource, o ->
                        o.getCustomerName().toLowerCase(Locale.ROOT).contains(customerQ.trim().toLowerCase(Locale.ROOT))
                                && (codeQ.isBlank() || (o.getCode() != null && o.getCode().toLowerCase(Locale.ROOT).contains(codeQ.trim().toLowerCase(Locale.ROOT))))
                );
            } catch (Exception ex) {
                setStatus("DB search unavailable: " + ex.getMessage(), WARN);
            }
        }

        boolean any = false;
        if (!matches.isEmpty()) {
            rebuildQueueList(matches);
            setStatus("Found " + matches.size() + " matching active order(s)", primary);
            any = true;
        }

        if (!historyMatches.isEmpty()) {
            any = true;
            StringBuilder sb = new StringBuilder();
            sb.append("History results (latest first):\n");
            for (int i = 0; i < Math.min(historyMatches.size(), 5); i++) {
                Order o = historyMatches.get(i);
                sb.append(o.getCode()).append(" • ").append(o.getCustomerName()).append(" • ")
                        .append(MONEY_PH.format(o.getTotal())).append(" • ").append(o.getStatus()).append("\n");
            }
            JOptionPane.showMessageDialog(this, sb.toString(), "Order history", JOptionPane.INFORMATION_MESSAGE);
        }

        if (!any) {
            setStatus("No matching active orders or history.", WARN);
        }
    }

    // -------------------- UI updates --------------------

    private void refreshCartTable() {
        cartModel.setRowCount(0);
        for (CartLine line : cart) {
            double unit = priceWithOptions(line.item, line.options);
            double lineTotal = unit * line.qty;
            cartModel.addRow(new Object[]{
                    line.item.getName(),
                    line.options.label(),
                    line.qty,
                    MONEY_PH.format(unit),
                    MONEY_PH.format(lineTotal)
            });
        }

        double subtotal = cart.stream().mapToDouble(l -> priceWithOptions(l.item, l.options) * l.qty).sum();
        double tax = 0;
        double total = subtotal;

        lblSubtotal.setText(MONEY_PH.format(subtotal));
        lblTax.setText(MONEY_PH.format(tax));
        lblTotal.setText(MONEY_PH.format(total));
    }

    private void refreshQueueList() {
        // Traversal used here to list all current orders in the cashier queue view.
        // The linked-list queue is converted to a List for the Swing JList model.
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
    }

    private String formatQueueLine(Order order) {
        String code = order.getCode() == null ? "Order" : order.getCode();
        String status = order.getStatus() == null ? "PENDING" : order.getStatus();
        return code + " • " + order.getCustomerName() + " • " + MONEY_PH.format(order.getTotal()) + " • " + status;
    }

    private void renderReceipt(Order order) {
        StringBuilder sb = new StringBuilder();
        sb.append(shopName).append("\n");
        sb.append("Order ").append(order.getCode());
        if (order.getId() != null) sb.append(" (ID ").append(order.getId()).append(")");
        sb.append("\n");
        sb.append("Customer: ").append(order.getCustomerName()).append("\n");
        sb.append("Cashier: ").append(username).append("\n");
        sb.append("Status: ").append(order.getStatus()).append("\n");
        sb.append("---------------------------\n");
        for (OrderItem item : order.getItems()) {
            sb.append(String.format("%-16s x%-2d %8s\n", item.getItemName(), item.getQuantity(), MONEY_PH.format(item.getLineTotal())));
            if (!item.getOptionsLabel().isEmpty()) {
                sb.append("   -> ").append(item.getOptionsLabel()).append("\n");
            }
        }
        sb.append("---------------------------\n");
        sb.append(String.format("Subtotal: %s\n", MONEY_PH.format(order.getSubtotal())));
        sb.append(String.format("VAT (0%%): %s\n", MONEY_PH.format(order.getTax())));
        sb.append(String.format("TOTAL: %s\n", MONEY_PH.format(order.getTotal())));
        sb.append("Thank you!\n");
        receiptArea.setText(sb.toString());
    }

    private DrinkOptions promptOptions(MenuItem item, DrinkOptions current) {
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        JComboBox<String> size = new JComboBox<>(new String[]{"Small", "Medium", "Large"});
        JComboBox<String> temp = new JComboBox<>(new String[]{"Hot", "Iced"});
        JCheckBox cbShot = new JCheckBox("Extra shot (+₱20)");
        JCheckBox cbMilk = new JCheckBox("Alt milk (+₱25)");
        JCheckBox cbSugar = new JCheckBox("Less sugar");
        JTextField note = new JTextField();

        styleField(size);
        styleField(temp);
        styleField(note);

        size.setSelectedItem(current == null ? "Medium" : current.size);
        temp.setSelectedItem(current == null ? "Hot" : current.temperature);
        if (current != null) {
            cbShot.setSelected(current.extraShot);
            cbMilk.setSelected(current.altMilk);
            cbSugar.setSelected(current.lessSugar);
            note.setText(current.note);
        }

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.weightx = 1; g.fill = GridBagConstraints.HORIZONTAL;

        g.gridy = 0; g.insets = new Insets(0, 0, 6, 0); form.add(labeled("Size", size), g);
        g.gridy = 1; g.insets = new Insets(0, 0, 6, 0); form.add(labeled("Temperature", temp), g);
        g.gridy = 2; g.insets = new Insets(0, 0, 4, 0); form.add(cbShot, g);
        g.gridy = 3; g.insets = new Insets(0, 0, 4, 0); form.add(cbMilk, g);
        g.gridy = 4; g.insets = new Insets(0, 0, 4, 0); form.add(cbSugar, g);
        g.gridy = 5; g.insets = new Insets(4, 0, 0, 0); form.add(labeled("Notes", note), g);

        int result = JOptionPane.showConfirmDialog(this, form,
                "Customize " + item.getName(),
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return null;

        return new DrinkOptions(
                String.valueOf(size.getSelectedItem()),
                String.valueOf(temp.getSelectedItem()),
                cbShot.isSelected(),
                cbMilk.isSelected(),
                cbSugar.isSelected(),
                note.getText()
        );
    }

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

    private double priceWithOptions(MenuItem item, DrinkOptions options) {
        double price = item.getPrice().doubleValue();
        switch (options.size) {
            case "Medium" -> price += 10;
            case "Large" -> price += 20;
            default -> { /* Small keeps base */ }
        }
        if (options.extraShot) price += 20;
        if (options.altMilk) price += 25;
        return price;
    }

    private String generateOrderCode() {
        orderCounter++;
        return "#" + orderCounter;
    }

    private void bumpOrderCounter(Order order) {
        try {
            if (order.getCode() != null && order.getCode().startsWith("#")) {
                int parsed = Integer.parseInt(order.getCode().replace("#", "").trim());
                orderCounter = Math.max(orderCounter, parsed);
            }
        } catch (NumberFormatException ignored) {
            // keep existing counter when code is non-numeric
        }
    }

    private void updateQueueBadge() {
        lblQueueCount.setText(orderQueue.size() + " in queue");
    }

    private JLabel totalRow(JPanel parent, GridBagConstraints g, int y, String label) {
        g.gridy = y;
        g.insets = new Insets(y == 0 ? 0 : 6, 12, 0, 0);
        JLabel l = new JLabel(label);
        l.setFont(new Font("SansSerif", Font.PLAIN, 12));
        l.setForeground(MUTED);
        parent.add(l, g);

        g.gridy = y;
        g.gridx = 1;
        JLabel v = new JLabel(MONEY_PH.format(0));
        v.setHorizontalAlignment(SwingConstants.RIGHT);
        v.setFont(new Font("SansSerif", Font.BOLD, 14));
        v.setForeground(TEXT);
        parent.add(v, g);
        g.gridx = 0;
        return v;
    }

    private void filterMenu(String q) {
        menuModel.clear();
        // Linear search entry point for the menu filter (triggered when typing/searching menu).
        // Iterates through allMenuItems and keeps those whose code/name/category match.
        List<MenuItem> results = LinearSearch.searchMenuByName(allMenuItems, q);
        String mode = menuSortMode == null ? "Name (A-Z)" : String.valueOf(menuSortMode.getSelectedItem());
        if ("Price (Low-High)".equals(mode)) {
            // Insertion sort triggered when viewing menu sorted by price.
            // The filtered results are sorted in-place before rendering.
            InsertionSort.sort(results, Comparator.comparing(MenuItem::getPrice));
        } else {
            // Insertion sort triggered when viewing menu sorted by name.
            // The filtered results are sorted in-place before rendering.
            InsertionSort.sort(results, Comparator.comparing(MenuItem::getName));
        }
        for (MenuItem m : results) {
            menuModel.addElement(m);
        }
    }

    private void setStatus(String text, Color color) {
        if (lblStatus == null) return;
        lblStatus.setText(text);
        lblStatus.setForeground(color);
    }

    private void logError(String context, Exception ex) {
        System.err.println("[CashierPanel] " + context);
        ex.printStackTrace();
    }

    // -------------------- Data seeding --------------------

    private void loadMenuFromDatabaseOrFallback() {
        try {
            allMenuItems.clear();
            allMenuItems.addAll(menuItemDAO.findAll());
            if (allMenuItems.isEmpty()) throw new IllegalStateException("No menu items returned");
        } catch (Exception ex) {
            // fallback seed (local preview) if DB is unavailable
            allMenuItems.clear();
            allMenuItems.add(new MenuItem("CF001", "Espresso", "Coffee", new BigDecimal("95"),
                    "https://images.unsplash.com/photo-1509042239860-f550ce710b93?auto=format&fit=crop&w=200&q=60"));
            allMenuItems.add(new MenuItem("CF002", "Latte", "Coffee", new BigDecimal("125"),
                    "https://images.unsplash.com/photo-1481391032119-d89fee407e44?auto=format&fit=crop&w=200&q=60"));
            allMenuItems.add(new MenuItem("CF003", "Cappuccino", "Coffee", new BigDecimal("115"),
                    "https://images.unsplash.com/photo-1504753793650-d4a2b783c15e?auto=format&fit=crop&w=200&q=60"));
            allMenuItems.add(new MenuItem("CF004", "Cold Brew", "Coffee", new BigDecimal("150"),
                    "https://images.unsplash.com/photo-1507133750040-4a8f57021571?auto=format&fit=crop&w=200&q=60"));
            allMenuItems.add(new MenuItem("TE001", "Matcha Latte", "Tea", new BigDecimal("145"),
                    "https://images.unsplash.com/photo-1517705008128-361805f42e86?auto=format&fit=crop&w=200&q=60"));
            allMenuItems.add(new MenuItem("TE002", "Chai Latte", "Tea", new BigDecimal("130"),
                    "https://images.unsplash.com/photo-1541167760496-1628856ab772?auto=format&fit=crop&w=200&q=60"));
            allMenuItems.add(new MenuItem("PA001", "Blueberry Muffin", "Pastry", new BigDecimal("85"),
                    "https://images.unsplash.com/photo-1509440159596-0249088772ff?auto=format&fit=crop&w=200&q=60"));
            allMenuItems.add(new MenuItem("FD001", "Breakfast Sandwich", "Food", new BigDecimal("185"),
                    "https://images.unsplash.com/photo-1505253758473-96b7015fcd40?auto=format&fit=crop&w=200&q=60"));
            setStatus("Loaded fallback menu (DB unavailable)", WARN);
        }
    }

    private void loadActiveQueueFromDatabase() {
        orderQueue = new OrderQueue();
        if (previewMode) {
            refreshQueueList();
            setStatus("Queue refreshed (preview mode)", primary);
            return;
        }
        try {
            List<Order> active = orderDAO.loadActiveOrders(OrderQueue.MAX_SIZE);
            for (Order order : active) {
                if (orderQueue.enqueue(order)) {
                    bumpOrderCounter(order);
                }
            }
            refreshQueueList();
            setStatus("Queue synced from database", primary);
        } catch (Exception ex) {
            refreshQueueList();
            setStatus("Queue fallback (DB unavailable): " + ex.getMessage(), WARN);
        }
    }

    public void refreshData() {
        loadMenuFromDatabaseOrFallback();
        String query = menuSearchField == null ? "" : menuSearchField.getText();
        filterMenu(query);
        loadActiveQueueFromDatabase();
    }

    // -------------------- Helpers --------------------

    private JPanel surface(EmptyBorder padding) {
        JPanel p = new JPanel();
        p.setBackground(SURFACE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                padding
        ));
        return p;
    }

    private JPanel menuSurface(EmptyBorder padding) {
        JPanel p = new JPanel();
        p.setBackground(MENU_PANEL_BG);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MENU_PANEL_BORDER, 1),
                padding
        ));
        return p;
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

    private void styleMenuField(JComponent c) {
        c.setFont(new Font("SansSerif", Font.PLAIN, 13));
        c.setBackground(MENU_LIST_BG);
        c.setForeground(TEXT);
        c.setOpaque(true);
        c.setBorder(buildMenuFieldBorder(false));
        c.setPreferredSize(new Dimension(10, 44));
        c.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) { c.setBorder(buildMenuFieldBorder(true)); }
            @Override public void focusLost(java.awt.event.FocusEvent e) { c.setBorder(buildMenuFieldBorder(false)); }
        });
    }

    private Border buildMenuFieldBorder(boolean focused) {
        Color glow = new Color(menuAccent.getRed(), menuAccent.getGreen(), menuAccent.getBlue(), focused ? 160 : 70);
        Color line = focused ? menuAccent : MENU_PANEL_BORDER;
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

    // -------------------- Inner classes --------------------

    private static class CartLine {
        final MenuItem item;
        DrinkOptions options;
        int qty;

        CartLine(MenuItem item, DrinkOptions options, int qty) {
            this.item = item;
            this.options = options;
            this.qty = qty;
        }
    }

    private static class DrinkOptions {
        final String size;           // Small / Medium / Large
        final String temperature;    // Hot / Iced
        final boolean extraShot;
        final boolean altMilk;
        final boolean lessSugar;
        final String note;

        DrinkOptions(String size, String temperature, boolean extraShot, boolean altMilk, boolean lessSugar, String note) {
            this.size = size;
            this.temperature = temperature;
            this.extraShot = extraShot;
            this.altMilk = altMilk;
            this.lessSugar = lessSugar;
            this.note = note == null ? "" : note.trim();
        }

        static DrinkOptions standard() {
            return new DrinkOptions("Standard", "N/A", false, false, false, "");
        }

        String label() {
            StringBuilder sb = new StringBuilder();
            sb.append(size).append(" • ").append(temperature);
            List<String> extras = new ArrayList<>();
            if (extraShot) extras.add("Extra Shot");
            if (altMilk) extras.add("Alt Milk");
            if (lessSugar) extras.add("Less Sugar");
            if (!extras.isEmpty()) sb.append(" • ").append(String.join(", ", extras));
            if (!note.isEmpty()) sb.append(" • Note: ").append(note);
            return sb.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof DrinkOptions that)) return false;
            return extraShot == that.extraShot && altMilk == that.altMilk && lessSugar == that.lessSugar
                    && size.equals(that.size)
                    && temperature.equals(that.temperature)
                    && note.equals(that.note);
        }

        @Override
        public int hashCode() {
            int result = size.hashCode();
            result = 31 * result + temperature.hashCode();
            result = 31 * result + (extraShot ? 1 : 0);
            result = 31 * result + (altMilk ? 1 : 0);
            result = 31 * result + (lessSugar ? 1 : 0);
            result = 31 * result + note.hashCode();
            return result;
        }
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

    private class MenuItemRenderer extends JPanel implements ListCellRenderer<MenuItem> {
        private final JLabel lblImage = new JLabel();
        private final JLabel lblName = new JLabel();
        private final JLabel lblPrice = new JLabel();
        private final JLabel lblCategory = new JLabel();
        private boolean highlighted;

        MenuItemRenderer() {
            setLayout(new BorderLayout(12, 0));
            setBorder(new EmptyBorder(12, 12, 12, 12));
            setOpaque(false);

            lblImage.setPreferredSize(new Dimension(MENU_IMAGE_SIZE, MENU_IMAGE_SIZE));
            lblImage.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(menuAccent.getRed(), menuAccent.getGreen(), menuAccent.getBlue(), 90), 1),
                    new EmptyBorder(2, 2, 2, 2)
            ));

            lblName.setFont(new Font("SansSerif", Font.BOLD, 13));
            lblName.setForeground(TEXT);

            lblPrice.setFont(new Font("SansSerif", Font.BOLD, 13));
            lblPrice.setForeground(menuAccent);

            lblCategory.setFont(new Font("SansSerif", Font.PLAIN, 11));
            lblCategory.setForeground(MUTED);

            JPanel text = new JPanel();
            text.setOpaque(false);
            text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
            text.add(lblName);
            text.add(Box.createVerticalStrut(4));
            text.add(lblCategory);

            add(lblImage, BorderLayout.WEST);
            add(text, BorderLayout.CENTER);
            add(lblPrice, BorderLayout.EAST);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends MenuItem> list, MenuItem value, int index, boolean isSelected, boolean cellHasFocus) {
            lblName.setText(value.getName());
            lblPrice.setText(MONEY_PH.format(value.getPrice()));
            lblCategory.setText(value.getCategory());
            lblImage.setIcon(loadMenuImage(value.getImageUrl()));
            highlighted = index == hoveredMenuIndex;
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            int arc = 20;
            Color top = highlighted ? tint(primary, 0.86) : MENU_CARD_TOP;
            Color bottom = highlighted ? tint(primary, 0.94) : MENU_CARD_BOTTOM;
            GradientPaint paint = new GradientPaint(0, 0, top, 0, h, bottom);
            g2.setPaint(paint);
            g2.fillRoundRect(4, 3, w - 8, h - 6, arc, arc);
            g2.setColor(highlighted ? menuAccent : MENU_PANEL_BORDER);
            g2.setStroke(new BasicStroke(1.4f));
            g2.drawRoundRect(4, 3, w - 8, h - 6, arc, arc);
            if (highlighted) {
                g2.setColor(new Color(menuAccent.getRed(), menuAccent.getGreen(), menuAccent.getBlue(), 90));
                g2.setStroke(new BasicStroke(3f));
                g2.drawRoundRect(2, 1, w - 4, h - 2, arc + 4, arc + 4);
            }
            g2.dispose();
        }
    }

    private ImageIcon loadMenuImage(String url) {
        if (url == null || url.isBlank()) {
            return scaledFallbackIcon();
        }
        String key = url.trim();
        if (menuImageCache.containsKey(key)) {
            return menuImageCache.get(key);
        }
        ImageIcon icon = null;
        try {
            if (key.startsWith("http")) {
                icon = new ImageIcon(new URL(key));
            } else {
                icon = new ImageIcon(key);
            }
            if (icon.getIconWidth() > 0) {
                Image scaled = icon.getImage().getScaledInstance(MENU_IMAGE_SIZE, MENU_IMAGE_SIZE, Image.SCALE_SMOOTH);
                icon = new ImageIcon(scaled);
            } else {
                icon = scaledFallbackIcon();
            }
        } catch (Exception ex) {
            icon = scaledFallbackIcon();
        }
        menuImageCache.put(key, icon);
        return icon;
    }

    private ImageIcon scaledFallbackIcon() {
        Icon fallback = UIManager.getIcon("FileView.directoryIcon");
        if (fallback instanceof ImageIcon imageIcon) {
            Image scaled = imageIcon.getImage().getScaledInstance(MENU_IMAGE_SIZE, MENU_IMAGE_SIZE, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        }
        return null;
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
