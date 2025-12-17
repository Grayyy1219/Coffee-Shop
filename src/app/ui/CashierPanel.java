package app.ui;

import app.db.MenuItemDAO;
import app.db.OrderDAO;
import app.model.MenuItem;
import app.model.Order;
import app.model.OrderItem;
import app.model.OrderQueue;
import app.util.LinearSearch;
import app.util.SelectionSort;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
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
    private final OrderQueue orderQueue = new OrderQueue();
    private final List<Order> currentQueueView = new ArrayList<>();
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

    private int orderCounter = 1000;

    private final MenuItemDAO menuItemDAO = new MenuItemDAO();
    private final OrderDAO orderDAO = new OrderDAO();
    private final Set<String> customizableCategories = Set.of("Coffee", "Tea", "Iced");

    private static final Color BG = new Color(245, 247, 250);
    private static final Color SURFACE = Color.WHITE;
    private static final Color BORDER = new Color(233, 236, 239);
    private static final Color TEXT = new Color(33, 37, 41);
    private static final Color MUTED = new Color(108, 117, 125);
    private static final Color PRIMARY = new Color(32, 85, 197);
    private static final Color SUCCESS = new Color(32, 140, 99);
    private static final Color WARN = new Color(255, 193, 7);

    private static final NumberFormat MONEY_PH = NumberFormat.getCurrencyInstance(new Locale("en", "PH"));

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
        JPanel menuPanel = surface(new EmptyBorder(14, 14, 14, 14));
        menuPanel.setLayout(new BorderLayout(10, 10));
        menuPanel.setPreferredSize(new Dimension(320, 10));

        JLabel menuTitle = new JLabel("Menu");
        menuTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        menuTitle.setForeground(TEXT);

        JTextField search = new JTextField();
        search.setToolTipText("Search menu items");
        styleField(search);

        menuSortMode = new JComboBox<>(new String[]{"Name (A-Z)", "Price (Low-High)"});
        menuSortMode.setToolTipText("Selection sort: name or price");
        styleField(menuSortMode);
        menuSortMode.addActionListener(e -> filterMenu(search.getText()));

        search.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filterMenu(search.getText()); }
            @Override public void removeUpdate(DocumentEvent e) { filterMenu(search.getText()); }
            @Override public void changedUpdate(DocumentEvent e) { filterMenu(search.getText()); }
        });

        JPanel menuHeader = new JPanel();
        menuHeader.setLayout(new BoxLayout(menuHeader, BoxLayout.Y_AXIS));
        menuHeader.setOpaque(false);
        menuHeader.add(menuTitle);
        menuHeader.add(Box.createVerticalStrut(8));
        menuHeader.add(search);
        menuHeader.add(Box.createVerticalStrut(6));
        menuHeader.add(menuSortMode);

        JList<MenuItem> menuList = new JList<>(menuModel);
        menuList.setCellRenderer(new MenuItemRenderer());
        menuList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        menuList.setFixedCellHeight(66);
        menuList.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            MenuItem item = menuList.getSelectedValue();
            if (item != null) addItemToCart(item);
            menuList.clearSelection();
        });

        JScrollPane menuScroll = new JScrollPane(menuList);
        menuScroll.setBorder(BorderFactory.createLineBorder(BORDER, 1));

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
        cartTable.getTableHeader().setReorderingAllowed(false);

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
        JButton btnResetQueue = ghost("Reset Queue");
        btnResetQueue.addActionListener(e -> refreshQueueList());

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
                setStatus("Previewing " + selected.getCode(), PRIMARY);
            }
        });
        JScrollPane queueScroll = new JScrollPane(queueList);
        queueScroll.setBorder(BorderFactory.createLineBorder(BORDER, 1));

        JButton btnServe = ghost("Serve Next");
        btnServe.addActionListener(e -> serveNext());

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
                setStatus("Added another " + item.getName() + " (" + opts.label() + ")", PRIMARY);
                return;
            }
        }
        cart.add(new CartLine(item, opts, 1));
        refreshCartTable();
        setStatus("Added " + item.getName() + " (" + opts.label() + ")", PRIMARY);
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
        setStatus("Updated quantity for " + line.item.getName(), PRIMARY);
    }

    private void removeSelectedItem() {
        int row = cartTable.getSelectedRow();
        if (row < 0 || row >= cart.size()) {
            setStatus("Select an item to remove", WARN);
            return;
        }
        CartLine removed = cart.remove(row);
        refreshCartTable();
        setStatus("Removed " + removed.item.getName(), PRIMARY);
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
        setStatus("Updated options for " + line.item.getName() + " (" + updated.label() + ")", PRIMARY);
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

        Order order = new Order();
        order.setCode(generateOrderCode());
        order.setCustomerName(customerField.getText().isBlank() ? "Walk-in" : customerField.getText().trim());
        order.setSubtotal(BigDecimal.valueOf(subtotalVal));
        order.setTax(BigDecimal.valueOf(taxVal));
        order.setTotal(BigDecimal.valueOf(totalVal));
        order.setStatus("PENDING");
        order.setPaid(false);

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
                orderDAO.insertOrderWithItems(order);
            } catch (Exception ex) {
                dbOk = false;
                setStatus("DB issue while saving order: " + ex.getMessage() + " (queued locally)", WARN);
            }
        }

        boolean accepted = orderQueue.enqueue(order);
        if (!accepted) {
            setStatus("Queue full (50). Serve some orders first.", WARN);
            return;
        }

        refreshQueueList();
        renderReceipt(order);
        cart.clear();
        refreshCartTable();
        setStatus((previewMode ? "Preview: " : "") + "Queued order " + order.getCode() + (dbOk ? "" : " (not saved to DB)"), dbOk ? SUCCESS : WARN);
    }

    private void serveNext() {
        if (orderQueue.isEmpty()) {
            setStatus("No orders in queue", WARN);
            return;
        }
        Order next = orderQueue.peek();
        renderReceipt(next);

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

        orderQueue.dequeue();
        refreshQueueList();
        setStatus("Served " + next.getCode(), SUCCESS);
    }

    private void searchActiveOrders() {
        String customerQ = orderSearchField.getText() == null ? "" : orderSearchField.getText();
        String codeQ = orderCodeSearchField.getText() == null ? "" : orderCodeSearchField.getText();
        // Linear search entry point for active orders in the queue
        List<Order> matches = LinearSearch.search(orderQueue.traverse(), o ->
                o.getCustomerName().toLowerCase(Locale.ROOT).contains(customerQ.trim().toLowerCase(Locale.ROOT))
                        && (codeQ.isBlank() || (o.getCode() != null && o.getCode().toLowerCase(Locale.ROOT).contains(codeQ.trim().toLowerCase(Locale.ROOT))))
        );

        List<Order> historyMatches = new ArrayList<>();
        if (!previewMode) {
            try {
                List<Order> historySource = orderDAO.searchOrders(customerQ.trim(), codeQ.trim(), 50);
                // Linear search entry point for order history (DB-backed)
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
            setStatus("Found " + matches.size() + " matching active order(s)", PRIMARY);
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
        // Linear search entry point for the menu filter
        List<MenuItem> results = LinearSearch.searchMenuByName(allMenuItems, q);
        String mode = menuSortMode == null ? "Name (A-Z)" : String.valueOf(menuSortMode.getSelectedItem());
        if ("Price (Low-High)".equals(mode)) {
            SelectionSort.sort(results, Comparator.comparing(MenuItem::getPrice)); // selection sort entry point for menu price
        } else {
            SelectionSort.sort(results, Comparator.comparing(MenuItem::getName)); // selection sort entry point for menu names
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

    // -------------------- Data seeding --------------------

    private void loadMenuFromDatabaseOrFallback() {
        try {
            allMenuItems.clear();
            allMenuItems.addAll(menuItemDAO.findAll());
            if (allMenuItems.isEmpty()) throw new IllegalStateException("No menu items returned");
        } catch (Exception ex) {
            // fallback seed (local preview) if DB is unavailable
            allMenuItems.clear();
            allMenuItems.add(new MenuItem("CF001", "Espresso", "Coffee", new BigDecimal("95")));
            allMenuItems.add(new MenuItem("CF002", "Latte", "Coffee", new BigDecimal("125")));
            allMenuItems.add(new MenuItem("CF003", "Cappuccino", "Coffee", new BigDecimal("115")));
            allMenuItems.add(new MenuItem("CF004", "Cold Brew", "Coffee", new BigDecimal("150")));
            allMenuItems.add(new MenuItem("TE001", "Matcha Latte", "Tea", new BigDecimal("145")));
            allMenuItems.add(new MenuItem("TE002", "Chai Latte", "Tea", new BigDecimal("130")));
            allMenuItems.add(new MenuItem("PA001", "Blueberry Muffin", "Pastry", new BigDecimal("85")));
            allMenuItems.add(new MenuItem("FD001", "Breakfast Sandwich", "Food", new BigDecimal("185")));
            setStatus("Loaded fallback menu (DB unavailable)", WARN);
        }
    }

    private void loadActiveQueueFromDatabase() {
        if (previewMode) {
            refreshQueueList();
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
            if (!active.isEmpty()) {
                setStatus("Loaded active orders from DB", PRIMARY);
            }
        } catch (Exception ex) {
            refreshQueueList();
            setStatus("Queue fallback (DB unavailable): " + ex.getMessage(), WARN);
        }
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

    private class MenuItemRenderer extends JPanel implements ListCellRenderer<MenuItem> {
        private final JLabel lblName = new JLabel();
        private final JLabel lblPrice = new JLabel();
        private final JLabel lblCategory = new JLabel();

        MenuItemRenderer() {
            setLayout(new BorderLayout());
            setBorder(new EmptyBorder(8, 10, 8, 10));
            setOpaque(true);

            lblName.setFont(new Font("SansSerif", Font.BOLD, 13));
            lblName.setForeground(TEXT);

            lblPrice.setFont(new Font("SansSerif", Font.BOLD, 13));
            lblPrice.setForeground(PRIMARY);

            lblCategory.setFont(new Font("SansSerif", Font.PLAIN, 11));
            lblCategory.setForeground(MUTED);

            add(lblName, BorderLayout.NORTH);
            add(lblCategory, BorderLayout.CENTER);
            add(lblPrice, BorderLayout.EAST);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends MenuItem> list, MenuItem value, int index, boolean isSelected, boolean cellHasFocus) {
            lblName.setText(value.getName());
            lblPrice.setText(MONEY_PH.format(value.getPrice()));
            lblCategory.setText(value.getCategory());

            if (isSelected) {
                setBackground(new Color(232, 240, 255));
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(PRIMARY, 1),
                        new EmptyBorder(8, 10, 8, 10)
                ));
            } else {
                setBackground(Color.WHITE);
                setBorder(new EmptyBorder(8, 10, 8, 10));
            }
            return this;
        }
    }
}
