package app.db;

import app.model.Order;
import app.model.OrderItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Expects tables:
 *  - orders(id PK AUTO, code, customer_name, status, subtotal, tax, total, paid, created_at)
 *  - order_items(id PK AUTO, order_id FK -> orders.id, item_code, item_name, options, quantity, unit_price, line_total)
 * Status values follow PENDING / IN_PROGRESS / COMPLETED.
 */
public class OrderDAO {

    public Order insertOrderWithItems(Order order) throws Exception {
        String orderSql = "INSERT INTO orders (code, customer_name, status, subtotal, tax, total, paid, created_at) VALUES (?,?,?,?,?,?,?,NOW())";
        String itemSql = "INSERT INTO order_items (order_id, item_code, item_name, options, quantity, unit_price, line_total) VALUES (?,?,?,?,?,?,?)";

        try (Connection con = DB.getConnection()) {
            con.setAutoCommit(false);

            try (PreparedStatement ps = con.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, order.getCode());
                ps.setString(2, order.getCustomerName());
                ps.setString(3, order.getStatus());
                ps.setBigDecimal(4, order.getSubtotal());
                ps.setBigDecimal(5, order.getTax());
                ps.setBigDecimal(6, order.getTotal());
                ps.setBoolean(7, order.isPaid());
                ps.executeUpdate();

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        order.setId(keys.getInt(1));
                    }
                }
            }

            try (PreparedStatement psItem = con.prepareStatement(itemSql)) {
                for (OrderItem item : order.getItems()) {
                    psItem.setInt(1, order.getId());
                    psItem.setString(2, item.getItemCode());
                    psItem.setString(3, item.getItemName());
                    psItem.setString(4, item.getOptionsLabel());
                    psItem.setInt(5, item.getQuantity());
                    psItem.setBigDecimal(6, item.getUnitPrice());
                    psItem.setBigDecimal(7, item.getLineTotal());
                    psItem.addBatch();
                }
                psItem.executeBatch();
            }

            con.commit();
            return order;
        }
    }

    public void updateStatusToCompleted(int orderId) throws Exception {
        updateStatus(orderId, "COMPLETED", true);
    }

    public void updateStatusToInProgress(int orderId) throws Exception {
        updateStatus(orderId, "IN_PROGRESS", false);
    }

    private void updateStatus(int orderId, String status, boolean paid) throws Exception {
        String sql = "UPDATE orders SET status = ?, paid = ? WHERE id = ?";
        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setBoolean(2, paid);
            ps.setInt(3, orderId);
            ps.executeUpdate();
        }
    }

    public void updateOrderWithItems(Order order) throws Exception {
        if (order.getId() == null) throw new IllegalArgumentException("Order id is required for update");

        String orderSql = "UPDATE orders SET customer_name = ?, status = ?, subtotal = ?, tax = ?, total = ?, paid = ? WHERE id = ?";
        String deleteItems = "DELETE FROM order_items WHERE order_id = ?";
        String itemSql = "INSERT INTO order_items (order_id, item_code, item_name, options, quantity, unit_price, line_total) VALUES (?,?,?,?,?,?,?)";

        try (Connection con = DB.getConnection()) {
            con.setAutoCommit(false);

            try (PreparedStatement ps = con.prepareStatement(orderSql)) {
                ps.setString(1, order.getCustomerName());
                ps.setString(2, order.getStatus());
                ps.setBigDecimal(3, order.getSubtotal());
                ps.setBigDecimal(4, order.getTax());
                ps.setBigDecimal(5, order.getTotal());
                ps.setBoolean(6, order.isPaid());
                ps.setInt(7, order.getId());
                ps.executeUpdate();
            }

            try (PreparedStatement del = con.prepareStatement(deleteItems)) {
                del.setInt(1, order.getId());
                del.executeUpdate();
            }

            try (PreparedStatement psItem = con.prepareStatement(itemSql)) {
                for (OrderItem item : order.getItems()) {
                    psItem.setInt(1, order.getId());
                    psItem.setString(2, item.getItemCode());
                    psItem.setString(3, item.getItemName());
                    psItem.setString(4, item.getOptionsLabel());
                    psItem.setInt(5, item.getQuantity());
                    psItem.setBigDecimal(6, item.getUnitPrice());
                    psItem.setBigDecimal(7, item.getLineTotal());
                    psItem.addBatch();
                }
                psItem.executeBatch();
            }

            con.commit();
        }
    }

    public List<Order> loadActiveOrders(int limit) throws Exception {
        String sql = "SELECT id, code, customer_name, status, subtotal, tax, total, paid, created_at FROM orders WHERE status IN ('PENDING','IN_PROGRESS') ORDER BY created_at ASC LIMIT ?";
        List<Order> out = new ArrayList<>();
        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(mapOrder(rs));
                }
            }
        }

        for (Order order : out) {
            order.getItems().addAll(loadItems(order.getId()));
        }
        return out;
    }

    public List<Order> searchOrders(String customer, String orderCode, int limit) throws Exception {
        customer = customer == null ? "" : customer;
        orderCode = orderCode == null ? "" : orderCode;
        String sql = "SELECT id, code, customer_name, status, subtotal, tax, total, paid, created_at FROM orders WHERE customer_name LIKE ? AND (? = '' OR code LIKE ?) ORDER BY created_at DESC LIMIT ?";
        List<Order> out = new ArrayList<>();
        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, "%" + customer + "%");
            ps.setString(2, orderCode);
            ps.setString(3, "%" + orderCode + "%");
            ps.setInt(4, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(mapOrder(rs));
                }
            }
        }

        for (Order order : out) {
            order.getItems().addAll(loadItems(order.getId()));
        }
        return out;
    }

    public Integer findIdByCode(String code) throws Exception {
        if (code == null || code.isBlank()) return null;
        String sql = "SELECT id FROM orders WHERE code = ? LIMIT 1";
        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }
        return null;
    }

    private Order mapOrder(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setId(rs.getInt("id"));
        order.setCode(rs.getString("code"));
        order.setCustomerName(rs.getString("customer_name"));
        order.setStatus(rs.getString("status"));
        order.setSubtotal(rs.getBigDecimal("subtotal"));
        order.setTax(rs.getBigDecimal("tax"));
        order.setTotal(rs.getBigDecimal("total"));
        order.setPaid(rs.getBoolean("paid"));
        order.setCreatedAt(rs.getTimestamp("created_at"));
        return order;
    }

    private List<OrderItem> loadItems(int orderId) throws SQLException {
        String sql = "SELECT item_code, item_name, options, quantity, unit_price, line_total FROM order_items WHERE order_id = ?";
        List<OrderItem> items = new ArrayList<>();
        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderItem item = new OrderItem();
                    item.setItemCode(rs.getString("item_code"));
                    item.setItemName(rs.getString("item_name"));
                    item.setOptionsLabel(rs.getString("options"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setUnitPrice(rs.getBigDecimal("unit_price"));
                    item.setLineTotal(rs.getBigDecimal("line_total"));
                    items.add(item);
                }
            }
        }
        return items;
    }
}
