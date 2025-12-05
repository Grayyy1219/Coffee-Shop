package coffeeshop.db;

import coffeeshop.model.Order;
import coffeeshop.model.OrderItem;
import coffeeshop.model.OrderStatus;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class MySqlDatabaseAccess implements DatabaseAccess {
    private static final String URL = "jdbc:mysql://localhost:3306/coffee_shop";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    protected Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    @Override
    public boolean validateUser(String username, String password) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ? AND password = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    @Override
    public void saveOrder(Order order) throws SQLException {
        String orderSql = "INSERT INTO orders (order_id, customer_name, created_at, status, paid, total) VALUES (?, ?, ?, ?, ?, ?)";
        String itemSql = "INSERT INTO order_items (order_id, item_code, quantity, line_total) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement orderStmt = conn.prepareStatement(orderSql);
                 PreparedStatement itemStmt = conn.prepareStatement(itemSql)) {
                orderStmt.setString(1, order.getOrderId());
                orderStmt.setString(2, order.getCustomerName());
                orderStmt.setTimestamp(3, Timestamp.valueOf(order.getCreatedAt()));
                orderStmt.setString(4, order.getStatus().name());
                orderStmt.setBoolean(5, order.isPaid());
                orderStmt.setDouble(6, order.getTotal());
                orderStmt.executeUpdate();

                for (OrderItem item : order.getItems()) {
                    itemStmt.setString(1, order.getOrderId());
                    itemStmt.setString(2, item.getItem().getCode());
                    itemStmt.setInt(3, item.getQuantity());
                    itemStmt.setDouble(4, item.getLineTotal());
                    itemStmt.addBatch();
                }
                itemStmt.executeBatch();
                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    @Override
    public void updateOrderStatus(String orderId, OrderStatus status, boolean paid) throws SQLException {
        String sql = "UPDATE orders SET status = ?, paid = ? WHERE order_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            stmt.setBoolean(2, paid);
            stmt.setString(3, orderId);
            stmt.executeUpdate();
        }
    }

    @Override
    public List<Order> loadActiveOrders() throws SQLException {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT order_id, customer_name, created_at, status, paid FROM orders WHERE status <> 'PAID' ORDER BY created_at";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                // Only hydrate minimal fields; items can be added later if needed.
                Order order = new Order(
                        rs.getString("order_id"),
                        rs.getString("customer_name"),
                        List.of()
                );
                if (rs.getBoolean("paid")) {
                    order.markPaid();
                }
                orders.add(order);
            }
        }
        return orders;
    }
}
